 package controllers;
 
 import java.util.List;
 import java.util.Map;
 
 import org.h2.expression.ExpressionList;
 
 import play.*;
 import play.data.*;
 import play.mvc.*;
 import play.mvc.Http.Session;
 import views.html.index;
 import models.Usuario;
 
 public class Users extends Controller {
 
 	static Form<Usuario> userForm = form(Usuario.class);
   
   /*
    * Elegir acción
    */
 
   public static Result index() {
     System.out.println("INDEX---_");
     // Comprobar si está logueado
     if(!isLogged()){
       // Mostrar la vista principal
       return all();
     }else{
       // Mostrar la vista de usuario logueado
       return show();
     }
   }
 
   /*
    * Mostrar la lista de usuarios y formulario para registro/logueo.
    */
 
   public static Result all(){
 	System.out.println("INICIO");
   	return ok(views.html.index.render(Usuario.all(), userForm));
   }
 
 
   /*
    * Cargar sesión a partir de objeto usuario.
    */
   public static void loadSession(Usuario user){
     String uuid = java.util.UUID.randomUUID().toString();
     session("uuid",uuid);
     session("cuuid",uuid);
     session("codigo",user.getCodigo());
     session("password",user.getPassword());
   }
   
   /*
    * Logueo de usuario
    */
 
   public static Result login(){
     Form<Usuario> filledForm = userForm.bindFromRequest();
     Map<String, String[]> formData = request().body().asFormUrlEncoded();
     String code = "";
     String password = "";
 
     if(code == null || password == null){
       code = filledForm.get().codigo;
       password = filledForm.get().password;
     }else{
       code = formData.get("codigo")[0];
       password = formData.get("password")[0];
     }
 
     Usuario user = Usuario.getUserByCodigo(code);
     
     // Comprobar password y cargar sesión.
     if(password.equals(user.getPassword())){
     	System.out.println("Válido, entrar");
     	loadSession(user);
     }
     
     // Redireccionar a página inicial (con o sin carga de sesión)
     return redirect(routes.Application.index());
   }
 
   /*
    * Buscar un usuario por su codigo
    */ 
 
   public static Result show(String codigo){
     return TODO;
   }
 
   /*
    * Mostrar usuario de la sesión
    */
 
   public static Result show(){
 	  System.out.println("Entra aquí");
       Usuario user = Usuario.getUserByCodigo(session("codigo"));
       return ok(views.html.user.render(user));
   }
 
   /*
    * Comprobar formulario y crear nuevo usuario
    */
 
   public static Result create(){
   	Form<Usuario> filledForm = userForm.bindFromRequest();
     // Determinar si los datos del formulario tienen errores
   	if(filledForm.hasErrors()){
   		return badRequest(views.html.index.render(Usuario.all(), filledForm));
   	}else{
       // Es usuario --> Loguear
       if(isUser(filledForm.get().codigo)){
     	  return login();
       // No es usuario --> Registrar
       }else{
     	  Usuario user;
     	try{  
     		user = Usuario.create(filledForm.get());
     	}catch(Exception e){
    		flash("Error","Existen problemas con el usuario");
     		return redirect(routes.Application.index());
     	}
     	loadSession(user);
         return redirect(routes.Application.index());
       }
   	}
   }
 
   /*
    * Determinar si el usuario existe
    */
 
   public static boolean isUser(String codigo){
     Usuario user = Usuario.getUserByCodigo(codigo);
     if(user == null)
       return false;
     else
       return true;
   }
 
   /*
    * Buscar el usuario por id
    */
   
   public static Result find(Long id){
 	  return TODO;
   }
 
   /*
    * Borrar usuario
    */
 
   public static Result delete(Long id){
     return TODO;
   }
   
   /*
    * Comprobar si usuario está logueado
    */
   
   public static boolean isLogged(){
     if(validSessionInfo()){
       return true;
     }else{
       session().clear();
       return false;
     }
   }
 
   /*
    * Validar usuario y password, si existen y son válidos
    */
 
   public static boolean validSessionInfo(){
     String codigo = session("codigo");
     String password = session("password");
 
     System.out.println(codigo + " " + password + " " + session("uuid") + " " + session("cuuid"));
     Usuario user = Usuario.getUserByCodigo(codigo);
     if(user == null){
       return false;
     }else if(!session("uuid").toString().equals(session("cuuid").toString())){
       return false;
     }else{
       if(user.getPassword().equals(password)){
         return true;
       }else{
         return false;
       }
     }
   }
 
   public static Usuario getUserSession(){
     String codigo = session("codigo");
     String password = session("password");
     Usuario user = Usuario.getUserByCodigo(codigo);
     return user;
   }
   
   public static Result logout(){
 	  session().clear();
 	  return redirect(routes.Application.index());
   }
   
 }
