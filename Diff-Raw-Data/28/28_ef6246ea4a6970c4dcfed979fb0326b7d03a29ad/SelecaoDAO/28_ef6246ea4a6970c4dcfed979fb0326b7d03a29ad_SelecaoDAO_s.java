 package sgcmf.model.dao;
 
 import java.util.ArrayList;
 import sgcmf.hibernate.SGCMFSessionManager;
 import sgcmf.model.hibernate.Selecao;
 
 public class SelecaoDAO
 {
     private static SelecaoDAO instance;
     
     private SelecaoDAO()
     {
         
     }
     
     public static SelecaoDAO getInstance()
     {
         if (instance == null)
         {
             instance = new SelecaoDAO();
         }
         return instance;
     }
     
     public ArrayList<Selecao> listaTodos()
     {
         return (ArrayList<Selecao>) SGCMFSessionManager.getCurrentSession().createQuery("from Selecao s order by s.id").list();
     }
 
     public ArrayList<Selecao> querySelecaoByNomePais(String pais)
     {
         String hql;
 
         hql = "from Selecao s "
                 + "where lower(s.pais) like lower('%" + pais + "%')";
 
         return (ArrayList<Selecao>) SGCMFSessionManager.getCurrentSession().createQuery(hql).list();
     }
 
     public ArrayList<Selecao> querySelecaoByNomeTecnico(String tecnico)
     {
         String hql;
 
         hql = "from Selecao s "
                 + "where lower(s.usuario.nome) like lower('%" + tecnico + "%')";
 
         return (ArrayList<Selecao>) SGCMFSessionManager.getCurrentSession().createQuery(hql).list();
     }
 
     public ArrayList queryIdSelecao(String selecao)
     {
         String hql;
         hql = "select s.id from Selecao s where s.pais = '" + selecao + "'";
         return (ArrayList) SGCMFSessionManager.getCurrentSession().createQuery(hql).list();
     }
 
     public String queryNomeSelecao(Short idSelecao)
     {
         String hql;
         String nomeSelecao;
         hql = "select s.pais from Selecao s where s.id = " + idSelecao;
         nomeSelecao = SGCMFSessionManager.getCurrentSession().createQuery(hql).uniqueResult().toString();
         return nomeSelecao;
     }
     
     public ArrayList<Selecao> querySelecaoByGrupo(String grupo)
     {
         String hql;
         
         hql = "from Selecao s "
                 + "where s.grupo = '" +grupo+"'";
         return (ArrayList<Selecao>) SGCMFSessionManager.getCurrentSession().createQuery(hql).list();
     }
 }
