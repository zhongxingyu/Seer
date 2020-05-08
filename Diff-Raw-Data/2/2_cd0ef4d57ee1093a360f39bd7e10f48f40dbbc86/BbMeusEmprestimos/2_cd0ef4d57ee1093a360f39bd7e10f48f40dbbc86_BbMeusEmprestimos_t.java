 package br.com.erudio.suport;
 
 import br.com.erudio.model.dao.HibernateDAO;
 import br.com.erudio.model.dao.InterfaceDAO;
 import br.com.erudio.model.entities.Emprestimo;
 import br.com.erudio.util.FacesContextUtil;
 import java.io.Serializable;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import org.hibernate.Session;
 
 @ManagedBean(name = "bbEmprestimo")
 @ViewScoped
 public class BbMeusEmprestimos implements Serializable {
 
     private static final long serialVersionUID = 1L;
     
     private Integer idPessoa = BbUsuarioLogado.procuraPessoa().getIdPessoa();
     
    private String stringQuery = "from Emprestimo as f where f.usuario = " + idPessoa;
     
     public List<Emprestimo> getEmprestimos() {
         Session session = FacesContextUtil.getRequestSession();
         InterfaceDAO<Emprestimo> emprestimoDAO = new HibernateDAO<Emprestimo>(Emprestimo.class, session);
         List<Emprestimo> emprestimos = emprestimoDAO.getListByHQLQuery(stringQuery);
         return emprestimos;
     }
 }
