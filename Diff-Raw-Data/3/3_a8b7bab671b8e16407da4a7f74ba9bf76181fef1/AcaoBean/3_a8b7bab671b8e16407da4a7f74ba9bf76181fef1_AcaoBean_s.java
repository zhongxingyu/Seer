 package financeiro.web;
 
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 import financeiro.bolsa.acao.Acao;
 import financeiro.bolsa.acao.AcaoBO;
 import financeiro.bolsa.acao.AcaoVirtual;
 import financeiro.util.BOException;
 import financeiro.web.util.ContextoUtil;
 import financeiro.web.util.YahooFinanceUtil;
 
 @ManagedBean(name = "acaoBean")
 @RequestScoped
 public class AcaoBean {
 
 	private AcaoVirtual selecionada = new AcaoVirtual();
 	private List<AcaoVirtual> lista = null;
 	private String linkCodigoAcao = null;
 	
 	public void salvar() {
 		ContextoBean contextoBean = ContextoUtil.getContextoBean();
 		AcaoBO acaoBO = new AcaoBO();
 		
 		Acao acao = selecionada.getAcao();
 		acao.setSigla(acao.getSigla().toUpperCase());
 		acao.setUsuario(contextoBean.getUsuarioLogado());
 		acaoBO.salvar(acao);
 		
 		selecionada = new AcaoVirtual();
 		lista = null;
 	}
 	
 	public void exluir() {
 		AcaoBO acaoBO = new AcaoBO();
 		acaoBO.excluir(selecionada.getAcao());
 		selecionada = new AcaoVirtual();
 		lista = null;
 	}
 	
 	public List<AcaoVirtual> getLista() {
 		if (lista == null) {
 			try {
 				ContextoBean contextoBean = ContextoUtil.getContextoBean();
 				lista = new AcaoBO().listarAcaoVirtual(contextoBean.getUsuarioLogado());
 			} catch (BOException e) {
 				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
 			}
 		}
 		return lista;
 	}
 	
 	public String getLinkCodigoAcao() {
 		AcaoBO acaoBO = new AcaoBO();
 		if (selecionada != null) {
 			linkCodigoAcao = acaoBO.montaLinkAcao(selecionada.getAcao());
 		} else {
 			linkCodigoAcao = YahooFinanceUtil.INDICE_BOVESPA;
 		}
 		return linkCodigoAcao;
 	}
 	
 	public AcaoVirtual getSelecionada() {
 		return selecionada;
 	}
 
 	public void setSelecionada(AcaoVirtual selecionada) {
 		this.selecionada = selecionada;
 	}
 }
