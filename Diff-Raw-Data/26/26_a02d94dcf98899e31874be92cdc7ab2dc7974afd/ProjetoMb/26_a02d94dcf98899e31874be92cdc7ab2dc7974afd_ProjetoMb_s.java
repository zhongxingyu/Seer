 package br.com.unifacs.view;
 
 import java.awt.event.ActionEvent;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext; 
 
 import org.hibernate.exception.ConstraintViolationException;
 
 import br.com.unifacs.bo.BoException;
 import br.com.unifacs.bo.CategoriaBo;
 import br.com.unifacs.bo.CategoriaBoImpl;
 import br.com.unifacs.bo.ProjetoBo;
 import br.com.unifacs.bo.ProjetoBoImpl;
 import br.com.unifacs.model.Categoria;
 import br.com.unifacs.model.Projeto;
 
 @ManagedBean(name="projetoMb")
 @SessionScoped
 public class ProjetoMb {
 	
 	private ProjetoBo bo;
 	private Projeto projeto;
 	private List<Projeto> projetos; 
 	
 	public ProjetoMb() {
 		this.projeto = new Projeto();
 		this.bo = new ProjetoBoImpl();
 		this.setProjetos(this.bo.obterTodos()); 
 	}
 	
 	public void atualizar(ActionEvent actionEvent){
 		this.setProjetos(this.bo.obterTodos()); 	
 	}
 	
 	public String novo(){
 		this.projeto = new Projeto();
 		return "novoProjeto";
 	}
 	
 	public String editar(){ 
 		if(this.projeto == null){
 			FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage(FacesMessage.SEVERITY_WARN,"Selecione um projeto", null));
 			return null;
 		}else{
 			return "editarProjeto";
 		}
 	} 
 	
 	public String excluir(){ 
 		if(this.projeto == null){
 			FacesContext.getCurrentInstance().addMessage("Ateno", new FacesMessage(FacesMessage.SEVERITY_WARN,"Selecione um projeto", null));
 		}else{
 			try {
 				this.bo.excluir(projeto);
 				FacesContext.getCurrentInstance().addMessage("Ateno",  new FacesMessage(FacesMessage.SEVERITY_INFO, "Item excludo com sucesso!", null));
 				atualizar(null);
 			} catch (BoException e) { 
 				e.printStackTrace();
 				FacesContext.getCurrentInstance().addMessage("Erro", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Erro ao excluir registro", e.getMessage()));
 			} 
 		}
 		
 		return null;
 	} 
 	
 	public String salvar(){
 		try {
 			bo.salvar(projeto);
 			FacesContext.getCurrentInstance().addMessage("Ateno",  new FacesMessage(FacesMessage.SEVERITY_INFO, "Operao realizada com sucesso!", ""));
 			atualizar(null);
 		} catch (BoException e) {
 			e.printStackTrace();
 			FacesContext.getCurrentInstance().addMessage("Erro", new FacesMessage(FacesMessage.SEVERITY_ERROR,"J existe projeto com esse nome!", ""));
 			return null;
 		}
 		
 		return "listaProjeto";
 	}
 	
 	public String cancelar(){
 		atualizar(null);
 		return "listaProjeto";
 	}
 	
 	/**
 	 * @return the projeto
 	 */
 	public Projeto getProjeto() {
 		return projeto;
 	}
 	/**
 	 * @param projeto the projeto to set
 	 */
 	public void setProjeto(Projeto projeto) {
 		this.projeto = projeto;
 	}
 
 	public List<Projeto> getProjetos() {
 		return projetos;
 	}
 
 	public void setProjetos(List<Projeto> projetos) {
 		this.projetos = projetos;
 	}
 	
 	
 	
 
 }
