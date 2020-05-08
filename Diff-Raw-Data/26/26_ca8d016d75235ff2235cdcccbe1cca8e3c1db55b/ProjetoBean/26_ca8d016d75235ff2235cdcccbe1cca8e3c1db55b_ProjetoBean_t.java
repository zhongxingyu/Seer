 package br.edu.eseg.brproject.control;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Create;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.international.StatusMessage.Severity;
 import org.jboss.seam.international.StatusMessages;
 import org.jboss.seam.log.Log;
 
 import br.edu.eseg.brproject.control.transactions.ProjetoTx;
 import br.edu.eseg.brproject.model.Licao;
 import br.edu.eseg.brproject.model.Nota;
 import br.edu.eseg.brproject.model.Notastakeholder;
 import br.edu.eseg.brproject.model.Projeto;
 import br.edu.eseg.brproject.model.Solicitacaomudanca;
 import br.edu.eseg.brproject.model.Stakeholder;
 import br.edu.eseg.brproject.model.action.ArquivoList;
 import br.edu.eseg.brproject.model.action.NotaList;
 import br.edu.eseg.brproject.model.action.NotastakeholderList;
 import br.edu.eseg.brproject.model.action.ProjetoHome;
 import br.edu.eseg.brproject.model.action.StakeholderHome;
 import br.edu.eseg.brproject.model.action.StatusprojetoHome;
 import br.edu.eseg.brproject.model.action.UsuarioHome;
 
 @Name("projeto")
 @Scope(ScopeType.CONVERSATION)
 public class ProjetoBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Logger
 	Log log;
 	@In
 	StatusMessages statusMessages;
 	@In(create = true)
 	NotaList notaList;
 	@In(create = true)
 	ProjetoHome projetoHome;
 	@In(create = true)
 	StatusprojetoHome statusprojetoHome;
 	@In(create = true)
 	NotastakeholderList notastakeholderList;
 	@In(create = true)
 	StakeholderHome stakeholderHome;
 	@In(create = true)
 	UsuarioHome usuarioHome;
 	@In(create = true)
 	ProjetoTx projetoTx;
 	@In(create = true)
 	ArquivoList arquivoList;
 
 	private Long solicitacaoId;
 	private Long changeStatusId;
 	private Projeto projeto;
 	private List<Stakeholder> stakeholders;
 	private List<Solicitacaomudanca> solicitacaomudancas;
 	private List<Licao> licoes;
 	private List<List<Notastakeholder>> matriz;
 	private List<Nota> legenda;
 
 	@Create
 	public void start() {
 		log.info("Carregando projeto: "+ projetoHome.getProjetoId());
 		if (projetoHome.getProjetoId() != null) {
 			projetoHome.load();
 			projeto = projetoHome.getInstance();
 			stakeholders = new ArrayList<Stakeholder>(projeto.getStakeholders());
 			solicitacaomudancas = new ArrayList<Solicitacaomudanca>(
 					projeto.getSolicitacaomudancas());
 			licoes = new ArrayList<Licao>(projeto.getLicoes());
 			legenda = notaList.getResultList();
 
 			int size = projeto.getNotastakeholders().size();
 			log.info("Numero de notas '{0}'", size);
 
 			ArrayList<Notastakeholder> notastakeholders = new ArrayList<Notastakeholder>(
 					projeto.getNotastakeholders());
 			Collections.sort(notastakeholders, new NotastakeholderComparator());
 			matriz = new ArrayList<List<Notastakeholder>>();
 			ArrayList<Notastakeholder> linha = null;
 			Long idAnt = new Long(0);
 			System.out.println(notastakeholders.size());
 			for (Notastakeholder ns : notastakeholders) {
 				Long id = ns.getStakeholderavaliado().getId();
 				if (!idAnt.equals(id)) {
 					linha = new ArrayList<Notastakeholder>();
 					matriz.add(linha);
 					idAnt = id;
 				}
 				linha.add(ns);
 			}
 			Collections.sort(licoes);
 			Collections.sort(solicitacaomudancas,new SolicitacaoComparator());
 		}
 	}
 
 	public void updateStakeholder(Long id) {
 		stakeholderHome.setStakeholderId(id);
 		for (Stakeholder s : projeto.getStakeholders()) {
 			if (id == s.getId()) {
 				stakeholderHome.getInstance().setPapel(s.getPapel());
 				stakeholderHome.update();
 				stakeholderHome.clearInstance();
 				statusMessages.clear();
 				return;
 			}
 		}
 	}
 
 	public void removeStakeholder(Stakeholder s) {
 		stakeholderHome.setInstance(s);
 		stakeholderHome.load();
 		stakeholderHome.remove();
 		projetoHome.getInstance().getStakeholders().remove(s);
 	}
 
 	public void addStakeholder(Long usuarioId) {
 		for (Stakeholder s : projeto.getStakeholders()) {
 			if (s.getUsuario().getId() == usuarioId) {
 				statusMessages.add(Severity.INFO, "Stakeholder j adicionado!");
 				return;
 			}
 		}
 		Stakeholder s = stakeholderHome.getInstance();
 		usuarioHome.setUsuarioId(usuarioId);
 		s.setProjeto(projeto);
 		s.setUsuario(usuarioHome.find());
 		projeto.getStakeholders().add(s);
 		stakeholderHome.persist();
 		stakeholderHome.clearInstance();
 	}
 
 	
 	public void encerrarProjeto(){
 		changeStatusId = new Long(5);
 		changeStatus();
 		
 	}
 	
 	public double calculaTotal(Long idAvaliado) {
 
 		double resultado = 0;
 		for (int i = 0; i < matriz.size(); i++) {
 			Long id = matriz.get(i).get(0).getStakeholderavaliado().getId();
 			if (id.equals(idAvaliado)) {
 				for (int j = 0; j < matriz.get(i).size(); j++) {
 					resultado += matriz.get(i).get(j).getNota().getValor();
 				}
 				break;
 			}
 		}
 		return resultado;
 	}
 
 	public int calculaClassificacao(Long idAvaliado) {
 
 		List<TotalNota> totais = new ArrayList<TotalNota>();
 
 		for (int i = 0; i < matriz.size(); i++) {
 			Long id = matriz.get(i).get(0).getStakeholderavaliado().getId();
 			double total = calculaTotal(id);
 			totais.add(new TotalNota(id, total));
 		}
 		Collections.sort(totais);
 		for (int i = 0; i < totais.size(); i++) {
 			Long id = totais.get(i).id;
 			if (id.equals(idAvaliado)) {
 				return i + 1;
 			}
 
 		}
 		return 0;
 	}
 
 	public void changeStatus() {
 		statusprojetoHome.setStatusprojetoId(changeStatusId);
 		projetoHome.getInstance().setStatusprojeto(
 				statusprojetoHome.find());
		projetoHome.getInstance().setFim(new Date());
		StringBuffer sb = new StringBuffer("update projeto set statusprojetoid = ?");
		if(changeStatusId==5){
			sb.append(", fim = current_timestamp");
		}
		sb.append(" where id = ?");
		Query q =projetoHome.getEntityManager().createNativeQuery(sb.toString());
		q.setParameter(1, changeStatusId);
		q.setParameter(2, projeto.getId());
		q.executeUpdate();
 		statusMessages.add(Severity.INFO, "Status alterado com sucesso!");
 	}
 
 	public void setSolicitacaoId(Long solicitacaoId) {
 		this.solicitacaoId = solicitacaoId;
 	}
 
 	public Long getSolicitacaoId() {
 		return solicitacaoId;
 	}
 
 	public Projeto getProjeto() {
 		return projeto;
 	}
 
 	public void setProjeto(Projeto projeto) {
 		this.projeto = projeto;
 	}
 
 	public List<Stakeholder> getStakeholders() {
 		return stakeholders;
 	}
 
 	public void setStakeholders(List<Stakeholder> stakeholders) {
 		this.stakeholders = stakeholders;
 	}
 
 	public List<Solicitacaomudanca> getSolicitacaomudancas() {
 		return solicitacaomudancas;
 	}
 
 	public void setSolicitacaomudancas(
 			List<Solicitacaomudanca> solicitacaomudancas) {
 		this.solicitacaomudancas = solicitacaomudancas;
 	}
 
 	public List<Licao> getLicoes() {
 		return licoes;
 	}
 
 	public void setLicoes(List<Licao> licoes) {
 		this.licoes = licoes;
 	}
 
 	public List<List<Notastakeholder>> getMatriz() {
 		return matriz;
 	}
 
 	public void setMatriz(List<List<Notastakeholder>> matriz) {
 		this.matriz = matriz;
 	}
 
 	public List<Nota> getLegenda() {
 		return legenda;
 	}
 
 	public void setLegenda(List<Nota> legenda) {
 		this.legenda = legenda;
 	}
 
 	public Long getChangeStatusId() {
 		return changeStatusId;
 	}
 
 	public void setChangeStatusId(Long changeStatusId) {
 		this.changeStatusId = changeStatusId;
 	}
 
 	private class TotalNota implements Comparable<TotalNota> {
 		Long id;
 		Double totalNota;
 
 		public TotalNota(Long id, Double totalNota) {
 			super();
 			this.id = id;
 			this.totalNota = totalNota;
 		}
 
 		@Override
 		public int compareTo(TotalNota o) {
 			return o.totalNota.compareTo(totalNota);
 		}
 	}
 
 	private class NotastakeholderComparator implements
 			Comparator<Notastakeholder> {
 
 		@Override
 		public int compare(Notastakeholder o1, Notastakeholder o2) {
 			int c = o1.getStakeholderavaliado().getId()
 					.compareTo(o2.getStakeholderavaliado().getId());
 
 			return c != 0 ? c : o1.getStakeholderavaliador().getId()
 					.compareTo(o2.getStakeholderavaliador().getId());
 		}
 	}
 	
 	private class SolicitacaoComparator implements Comparator<Solicitacaomudanca>{
 
 		@Override
 		public int compare(Solicitacaomudanca o1, Solicitacaomudanca o2) {
 			// TODO Auto-generated method stub
 			return o1.getStatusmudanca().getId().compareTo(o2.getStatusmudanca().getId());
 		}
 		
 	}
 }
