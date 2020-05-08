 package br.ufrn.cerescaico.bsi.sigest.web;
 
 import br.ufrn.cerescaico.bsi.sigest.Sigest;
 import br.ufrn.cerescaico.bsi.sigest.bo.NegocioException;
 import br.ufrn.cerescaico.bsi.sigest.model.Curso;
 import br.ufrn.cerescaico.bsi.sigest.model.Professor;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 @ManagedBean(name = "cursoBean")
 @SessionScoped
 public class CursoBean extends AbstractBean implements Serializable {
 
     /**
 	 * serialVersionUID.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private Sigest sigest = Sigest.getInstance();
 	
 	private String nome = null;
 	
 	private Professor coordenador = null;
     
     private List<Curso> cursos = null;
     
     private List<Professor> professores = null;
     
     public String manter() {
         return "/curso/manter";
     }
     
     private Curso getCurso() {
     	Curso curso = new Curso();
     	curso.setNome(getNome());
     	return curso;
     }
     
     public String incluir() {
     	FacesContext context = FacesContext.getCurrentInstance();
     	
         ResourceBundle bundle = FacesContext.getCurrentInstance().getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");  
         String messageSucesso = bundle.getString("cursoBean.incluir");  
     	
         try {
         	//O método retirna o curso com o código
             sigest.inserirCurso(getCurso());
            context.addMessage("cursoBean.incluir", new FacesMessage(FacesMessage.SEVERITY_INFO, messageSucesso, messageSucesso));
         } catch (NegocioException ex) {
             cursos = new ArrayList<Curso>();
             Logger.getLogger(CursoBean.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
             context.addMessage("cursoBean.listar", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar a lista de cursos", ex.getMessage()));
         }
         return "/curso/manter";
     }
     
     public String listar() {
         try {
             cursos = sigest.listarCursos();
         } catch (NegocioException ex) {
             cursos = new ArrayList<Curso>();
             Logger.getLogger(CursoBean.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
             FacesContext context = FacesContext.getCurrentInstance();
             context.addMessage("cursoBean.listar", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar a lista de cursos", ex.getMessage()));
         }
         return "index";
     }
     
     public String listarProfessores() {
         try {
             professores = sigest.listarProfessores();
         } catch (NegocioException ex) {
             professores = new ArrayList<Professor>();
             Logger.getLogger(CursoBean.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
             FacesContext context = FacesContext.getCurrentInstance();
             context.addMessage("cursoBean.listarProfessores", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar a lista de professores", ex.getMessage()));
         }
         return "curso/manter";
     }
     
     public String getNome() {
 		return nome;
 	}
     
     public void setNome(String nome) {
 		this.nome = nome;
 	}
     
     public Professor getCoordenador() {
 		return coordenador;
 	}
     
     public void setCoordenador(Professor coordenador) {
 		this.coordenador = coordenador;
 	}
 
     public List<Curso> getCursos() {
     	listar();
         return cursos;
     }
 
     public void setCursos(List<Curso> cursos) {
         this.cursos = cursos;
     }
     
     public List<Professor> getProfessores() {
     	listarProfessores();
         return professores;
     }
 
     public void setProfessores(List<Professor> professores) {
         this.professores = professores;
     }
 }
