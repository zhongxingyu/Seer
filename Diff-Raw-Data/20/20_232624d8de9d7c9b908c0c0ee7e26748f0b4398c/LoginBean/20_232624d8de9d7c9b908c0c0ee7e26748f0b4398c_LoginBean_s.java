 package br.jus.trems.estagioaprendizado.numberquiz.controller;
 
 import br.jus.trems.estagioaprendizado.numberquiz.daoimpl.UserDaoImpl;
 import br.jus.trems.estagioaprendizado.numberquiz.entities.User;
 import br.jus.trems.estagioaprendizado.numberquiz.utils.Constants;
 import br.jus.trems.estagioaprendizado.numberquiz.utils.FacesUtil;
 import br.jus.trems.estagioaprendizado.numberquiz.utils.SessionUtil;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 /**
  * Bean gerencivel utilizado no controle do login de usurios na aplicao.
  * Este controle envolve principalmente:
  *                          verificao do login e senha;
  *                          login e logout;
  *                          cadastro de um novo usurio;
  *                          verificar se o usurio est autenticado e logado;
  *
  * @author heverson.vasconcelos
  */
 @ManagedBean(name = "loginBean")
 @RequestScoped
 public class LoginBean implements Serializable {
 
     /**
      * Armazena o usurio corrente
      */
     private User user;
     /**
      * Armazena o usurio corrente aps a autenticao, ou seja, o usurio
      * logado na sesso.
      */
     private User authenticatedUser;
     /**
      * Varivel utilizada nos mtodos que iro inserir ou consultar alguma
      * informao relativa aos usurios.
      */
     private UserDaoImpl userDaoImpl;
 
     @PostConstruct
     public void init() {
         user = new User();
         userDaoImpl = new UserDaoImpl();
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User userToauthenticate) {
         this.user = userToauthenticate;
     }
 
     /**
      * Mtodo que retorna o usurio que foi previamente autenticado e
      * est logado na sesso.
      *
      * @return Usurio autenticado.
      */
     public User getAuthenticatedUser() {
        authenticatedUser = (User) SessionUtil.getAttribute("authenticatedUser");
 
         return (authenticatedUser != null) ? authenticatedUser : null;
     }
 
     /**
      * Mtodo para verificar o login do usurio.
      * Consulta um determinado a partir do nome de login.
      *
      * @return Usurio encontrado. Null caso o usurio no seja encontrado.
      */
     private User verifyLogin() {
 
         return (userDaoImpl.getUserByName(user.getName()));
     }
 
     /**
      * Mtodo para verificar a senha do usurio.
      *
      * @return True caso a senha esteja correta.
      */
     private boolean verifyPassword() {
         if ((authenticatedUser.getPassword().compareTo(user.getPassword()) == 0)) {
             return true;
         }
 
         return false;
     }
 
     /**
      * Mtodo para autenticar o usurio. Verifica o login e a senha deste.
      *
      * @return String contendo o endereo de redirecionamento caso o usurio foi
      *          corretamente autenticado. Neste caso ser redirecionado para o
      *          incio do jogo (numberquiz.xhtml) <br>
      *          Null caso houver um erro na autenticao.
      */
     public String login() {
         authenticatedUser = verifyLogin();
 
         if (authenticatedUser == null) {
             FacesUtil.mensErro(Constants.MSG_INVALID_USER);
             return null;
         } else if (!verifyPassword()) {
             authenticatedUser = null;
             FacesUtil.mensErro(Constants.MSG_INVALID_PASSWORD);
             return null;
         }
 
        SessionUtil.setAttribute("authenticatedUser", authenticatedUser);
 
         return Constants.PAGE_NUMBERQUIZ;
     }
 
     /**
      * Mtodo para executar o logout da sesso.
      *
      * @return String contendo o endereo de redirecionamento para a pgina
      *          inicial (index.xhtml).
      */
     public String logout() {
         user = new User();
         authenticatedUser = null;
         SessionUtil.destroySession();
 
         userDaoImpl.finalizeAccess();
 
         return Constants.PAGE_INDEX;
     }
 
     /**
      * Mtodo para cadastrar um novo usurio.
      *
      * @return String contendo o endereo de redirecionamento para incio do
      *          jogo (numberquiz.xhtml). <br>
      *          Null caso exista um usurio previamente cadastrado com o mesmo
      *          nome de login que o usurio corrente.
      */
     public String newUser() {
 
         if (userDaoImpl.verifyIfUserNameExists(user.getName())) {
             FacesUtil.mensErro(Constants.MSG_USER_ALREADY_EXISTS);
             return null;
         }
 
         userDaoImpl.create(user);
         authenticatedUser = user;
        SessionUtil.setAttribute("authenticatedUser", authenticatedUser);
 
         return Constants.PAGE_NUMBERQUIZ;
     }
 
     /**
      * Mtodo para verificar se o usurio foi autenticado e est logado.
      * 
      * @return String contendo o endereo de redirecionamento para a pgina
      *          informando ao (userisnotlogged.xhtml). <br>
      *          Null caso exista um usurio previamente cadastrado com o mesmo
      *          nome de login que o usurio corrente.
      */
     public String verifyAuthenticatedUser() {
        authenticatedUser = (User) SessionUtil.getAttribute("authenticatedUser");
 
         if (authenticatedUser == null) {
             FacesUtil.mensErro(Constants.MSG_INVALID_USER);
             return Constants.PAGE_USER_ISNOT_LOGGED;
         }
 
         return null;
     }
 }
