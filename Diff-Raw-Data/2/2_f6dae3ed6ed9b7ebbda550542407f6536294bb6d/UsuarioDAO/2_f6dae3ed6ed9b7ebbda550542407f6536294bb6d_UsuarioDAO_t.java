 package sgcmf.model.dao;
 import java.util.ArrayList;
 import sgcmf.model.hibernate.Usuario;
 
 
 /**
  *
  * @author Thatiane
  */
 public class UsuarioDAO extends GeneralDAO{
 
     public ArrayList<Usuario> listaTodos()
     {
         return (ArrayList<Usuario>) sessao.createQuery("from Usuario u order by u.perfil").list();
     }
 
     public ArrayList<Usuario> queryUsuarioByLogin(String login, String senha)
     {
         String hql;
 
         hql = "from Usuario u "
                + "where login = '" + login + "' and senha = '" + senha + "' ";
 
         return (ArrayList<Usuario>) sessao.createQuery(hql).list();
     }
 
     public ArrayList<Usuario> queryUsuarioByNome(String nome)
     {
         String hql;
 
         hql = "from Usuario u "
                 + "where lower(u.nome) like lower('%" + nome + "%') order by u.id";
 
         return (ArrayList<Usuario>) sessao.createQuery(hql).list();
     }
 
      public ArrayList<Usuario> queryUsuarioByPerfil(String perfil)
     {
         String hql;
 
         hql = "from Usuario u "
                 + "where lower(u.perfil) like lower('%" + perfil + "%') order by u.id";
         return (ArrayList<Usuario>) sessao.createQuery(hql).list();
     }
 
 }
