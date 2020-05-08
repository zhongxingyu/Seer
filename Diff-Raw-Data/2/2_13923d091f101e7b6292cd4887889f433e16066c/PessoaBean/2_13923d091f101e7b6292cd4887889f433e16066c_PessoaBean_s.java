 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 /**
  *
  * @author Thalita
  */
 import dao.PessoaDAO;
 import dao.impl.PessoaDAOImpl;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import model.Pessoa;
 import seguridad.Seguridad;
 import utils.Mensagens;
 
 @ManagedBean
@ViewScoped
 public class PessoaBean {
 
     Pessoa pessoa;
     PessoaDAOImpl daoPessoa;
 
     public Pessoa getPessoa() {
         return pessoa;
     }
 
     public void setPessoa(Pessoa pessoa) {
         this.pessoa = pessoa;
     }
 
     public PessoaBean() {
         pessoa = new Pessoa();
         daoPessoa = new PessoaDAOImpl();
     }
 
     public void testar() {
         daoPessoa = new PessoaDAOImpl();
         FacesContext context = FacesContext.getCurrentInstance();
         //removeMascara();
         pessoa.setSenha(Seguridad.criptografar(pessoa.getEmail(), pessoa.getSenha()));
         pessoa = daoPessoa.save(pessoa);
         if (!pessoa.getNome().isEmpty()) {
             context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso", "Salvo"));
         } else {
             context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso", "Não Salvou"));
         }
 
     }
 
 
     public int tamanho() {
         return pessoa.getSenha().length();
     } 
     
     private void criaHash()
     {
         String hash = Seguridad.criptografar(pessoa.getEmail(), pessoa.getSenha());
         pessoa.setSenhaBanco(hash);
     }
     
     public String salvar() 
     {
         //this.removeMascara();
       
         this.criaHash();
         pessoa = daoPessoa.save(pessoa);
         if (!pessoa.getNome().isEmpty())
         {   
             Mensagens.aviso("Usuário incluído com sucesso!");
             FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);//put("pessoaBean", this);
             return "index?faces-redirect=true";
         }
         else
         {
             Mensagens.avisoErro("Falha ao cadastrar usuário."); 
             return null;
         }    
     }
 }
