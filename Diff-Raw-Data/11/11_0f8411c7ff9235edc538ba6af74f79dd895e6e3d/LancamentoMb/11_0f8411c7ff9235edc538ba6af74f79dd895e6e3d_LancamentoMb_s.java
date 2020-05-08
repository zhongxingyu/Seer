 package br.com.unifacs.view;
 
 import java.awt.event.ActionEvent;
 import java.sql.Time;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import br.com.unifacs.bo.BoException;
 import br.com.unifacs.bo.CategoriaBoImpl;
 import br.com.unifacs.bo.ContatoBoImpl;
 import br.com.unifacs.bo.FormaDePgtoBoImpl;
 import br.com.unifacs.bo.FrequenciaBoImpl;
 import br.com.unifacs.bo.HistoricoBo;
 import br.com.unifacs.bo.HistoricoBoImpl;
 import br.com.unifacs.bo.LancamentoBo;
 import br.com.unifacs.bo.LancamentoBoImpl;
 import br.com.unifacs.bo.UsuarioPojetoBoImpl;
 import br.com.unifacs.model.Categoria;
 import br.com.unifacs.model.Contato;
 import br.com.unifacs.model.Historico;
 import br.com.unifacs.model.Lancamento;
 import br.com.unifacs.model.UsuarioProjeto;
 import br.com.unifacs.utils.RdlUtils;
 
 @ManagedBean(name="lancamentoMb")
 @SessionScoped
 public class LancamentoMb {
 	
 	private LancamentoBo bo;
 	private Lancamento lancamento;
 	private List<Lancamento> lancamentos;
 	private HistoricoBo historicoBo;
 	
 	private static String TIPO_OP;
 	
 	public LancamentoMb(){
 		bo = new LancamentoBoImpl();
 		setLancamento(new Lancamento());
 		this.setLancamentos(this.bo.obterTodos());
 		historicoBo = new HistoricoBoImpl();
 	}
 	
 	public void atualizar(ActionEvent actionEvent){
 		this.setLancamentos(this.bo.obterTodos()); 	
 	}
 	
 	public String visualizar(){ 
 		if(this.lancamento == null){
 			FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Selecione um lancamento", null));
 			return null;
 		}else{
 			return "visualizarLancamento";
 		}
 	} 
 
 	public List<Lancamento> getLancamentos() {
 		return lancamentos;
 	}
 
 	public void setLancamentos(List<Lancamento> lancamentos) {
 		this.lancamentos = lancamentos;
 	}
 
 	public Lancamento getLancamento() {
 		return this.lancamento;
 	}
 
 	public void setLancamento(Lancamento lancamento) {
 		this.lancamento = lancamento;
 	}
 	public String novo(){
 		
		if (!RdlUtils.isUsuarioLogado())
			return "novoLogin";
 		
 		this.lancamento = new Lancamento();
 		atualizar(null);
 		LancamentoMb.TIPO_OP = "Insero";
 		return "novoLancamento";
 	}
 	
 	public String editar(){
 		
 		if(this.lancamento.getCategoria() == null)
 			this.lancamento.setCategoria(new Categoria());
 		
 		if(this.lancamento.getContato() == null)
 			this.lancamento.setContato(new Contato());		
 		
 		if(this.lancamento == null){
 			FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Selecione uma lanamento", null));
 			return null;
 		}else{
 			LancamentoMb.TIPO_OP = "Edio";
 			return "editarLancamento";
 		}		
 	}
 	
 	public String excluir(){ 
 		if(this.lancamento == null){
 			FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Selecione um lanamento", null));
 		}else{
 			try {
 				
 				Historico historico = new Historico();
 				historico.setDataAlteracao(new Date());
 				historico.setHoraAlteracao(new Time(new Date().getTime()));
 				historico.setLancamento(lancamento);
 				historico.setUsuario(RdlUtils.getUsuarioLogado());
 				historico.setTipoOperacao("Excluso");
 				
 				this.bo.excluir(lancamento);
 				
 				try{
 					this.historicoBo.salvar(historico);
 				}
 				catch(BoException e){
 					e.printStackTrace();
 				}
 				
 				FacesContext.getCurrentInstance().addMessage("Ateno",  new FacesMessage(FacesMessage.SEVERITY_WARN, "Item excludo com sucesso!", null));	
 				atualizar(null);
 				
 			} catch (BoException e) { 
 				e.printStackTrace();
 				FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage("Erro ao excluir registro", e.getMessage()));
 			} 
 		}
 		
 		return null;
 	} 
 	
 	public String salvar(){
 		try {
 			UsuarioProjeto e = new UsuarioPojetoBoImpl().obter(2);
 			this.lancamento.setUsuarioProjeto(e);
 			this.lancamento.setContato(new ContatoBoImpl().obter(this.lancamento.getContato().getId()));
 			this.lancamento.setCategoria(new CategoriaBoImpl().obter(this.lancamento.getCategoria().getId()));
 			this.lancamento.setFormaDePgto(new FormaDePgtoBoImpl().obter(this.lancamento.getFormaDePgto().getId()));
 			this.lancamento.setFrequencia(new FrequenciaBoImpl().obter(this.lancamento.getFrequencia().getId()));			
 			bo.salvar(this.lancamento);
 			
 			Historico historico = new Historico();
 			historico.setDataAlteracao(new Date());
 			historico.setHoraAlteracao(new Time(new Date().getTime()));
 			historico.setLancamento(lancamento);
 			historico.setTipoOperacao(LancamentoMb.TIPO_OP);
 			historico.setUsuario(RdlUtils.getUsuarioLogado());
 
 			try{
 				this.historicoBo.salvar(historico);
 			}
 			catch(BoException ex){
 				ex.printStackTrace();
 			}			
 			
 			FacesContext.getCurrentInstance().addMessage("Ateno",  new FacesMessage(FacesMessage.SEVERITY_WARN, "Operao realizada com sucesso!", "teste"));
 			atualizar(null);
 		} catch (BoException e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
 		}
 		
 		return "listaLancamento";
 	}	
 	
 	public String cancelar(){
 		atualizar(null);
 		return "listaLancamento";
 	}
 	
 		
 }
