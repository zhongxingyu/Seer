 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cl.uv.security.openam;
 
 import cl.uv.model.base.core.beans.AtributosFuncionario;
 import cl.uv.model.base.core.ejb.AuthEJBBeanLocal;
 import cl.uv.proyecto.persistencia.ejb.FuncionarioFacadeLocal;
 import cl.uv.security.openid.OpenIdSession;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 /**
  *
  * @author Alejandro
  */
 public class OpenAMUserDetailsService implements AuthenticationUserDetailsService<Authentication> {
     private OpenIdSession openIdSession = null;
     private FuncionarioFacadeLocal funcionarioFacade = lookupFuncionarioFacadeLocal();
     private AuthEJBBeanLocal authEJBBean = lookupAuthEJBBeanLocal();
     
     @Override
     public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
         String tokenOpenAM = (String) token.getCredentials();
         String emailOpenId = (String) token.getPrincipal();
         
         if (!tokenOpenAM.equals("N/A") && !tokenOpenAM.equals("TEST") && authEJBBean.validateToken(tokenOpenAM)) {
             return loadUserWithOpenAMToken(tokenOpenAM);
         }else if (!emailOpenId.equals("N/A")){
             return loadUserWithOpenIdEmail(emailOpenId);
         }else if (tokenOpenAM.equals("TEST") ){
             return loadUserTest();
         }else{
             System.out.println("Error: Usuario no encontrado");
            throw new UsernameNotFoundException("No existe validación previa.");
         }
     }
 
     
     private OpenAMUserDetails loadUserWithOpenAMToken(String token){
         System.out.println("Load User With OpenAM Token:"+token);
         openIdSession.setUserAuthenticatedWithSSO(true);
         AtributosFuncionario funcionario = authEJBBean.getAtributosFuncionarios(token);
         return createUser(funcionario, token);
     }
     
     private OpenAMUserDetails loadUserWithOpenIdEmail(String email){
         System.out.println("Load User With OpenId Email:"+email);
         openIdSession.setUserAuthenticatedWithSSO(false);
         Integer rut = funcionarioFacade.buscarRutPorEmail(email);
         AtributosFuncionario funcionario = authEJBBean.readFuncionarios(rut);
         return createUser(funcionario, "N/A");
     }
     
     private OpenAMUserDetails loadUserTest(){
         System.out.println("Load User Test");
         openIdSession.setUserAuthenticatedWithSSO(true);
         AtributosFuncionario funcionario = OpenAMUtil.createFalseUser();
         return createUser(funcionario, "N/A");
     }
     
     private OpenAMUserDetails createUser(AtributosFuncionario attr, String token) { 
         OpenAMUserDetails user;
         if(attr != null){
             user = new OpenAMUserDetails(attr.getUid(), token,
                                          true, true, true, true,
                                          createGrantedAuthority(attr.getListaRoles()));
         }else{
            user = new OpenAMUserDetails("", token);
         }
         user.setFuncionario(attr);
         return user;
         
     }
 
     private Set<GrantedAuthority> createGrantedAuthority(List<String> roles) {
         Set<GrantedAuthority> authoritys = new HashSet<GrantedAuthority>();
         for (String rol : OpenAMUtil.parseRoles(roles)) {
             authoritys.add(new SimpleGrantedAuthority(rol));
             System.out.println("ROL="+rol);
         }
         return authoritys;
     }
 
     private AuthEJBBeanLocal lookupAuthEJBBeanLocal() {
         try {
             Context c = new InitialContext();
             return (AuthEJBBeanLocal) c.lookup("java:global/Proyecto/Proyecto-ejb/AuthEJBBean!cl.uv.model.base.core.ejb.AuthEJBBeanLocal");
         } catch (NamingException ne) {
             Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
             throw new RuntimeException(ne);
         }
     }
 
     private FuncionarioFacadeLocal lookupFuncionarioFacadeLocal() {
         try {
             Context c = new InitialContext();
             return (FuncionarioFacadeLocal) c.lookup("java:global/Proyecto/Proyecto-ejb/FuncionarioFacade!cl.uv.proyecto.persistencia.ejb.FuncionarioFacadeLocal");
         } catch (NamingException ne) {
             Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
             throw new RuntimeException(ne);
         }
     }
 
     public OpenIdSession getOpenIdSession() {
         return openIdSession;
     }
 
     public void setOpenIdSession(OpenIdSession openIdSession) {
         this.openIdSession = openIdSession;
     }
     
     
 }
