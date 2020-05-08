 package controllers;
 
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         List<Agrupacion> masOfertados = Tema.getMasOfertados();
         List<Agrupacion> masBuscados = Busqueda.getMasBuscados();
         
         render(masOfertados,masBuscados);
     }
 
     public static void register() {
         render();
     }
    
    public static void quienes_somos() {
        render();
    }
    
    public static void como_funciona() {
        render();
    }
 
     public static void search(String terms) {
         Busqueda busqueda = new Busqueda();
         busqueda.texto = terms;
         params.flash();
 
         busqueda.save();
 
         List<Tema> temas = Tema.findOfertados(terms);
         render(temas, busqueda);
     }
 
     public static void signup(@Valid Usuario usuario, String repetirPassword) {
         flash.clear();
         validation.equals(usuario.password, repetirPassword).message("Las contraseñas no coinciden");
 
         if (validation.hasErrors()) {
             params.flash();
             render("Application/register.html");
         }
 
         Usuario registrado = usuario.register();
 
         signupConfirm(registrado.email, registrado.username);
     }
 
     public static void lost_password() {
         render();
     }
 
     public static void recover_password(
             @Email
             @Required(message="tienes que escribir el correo electrónico que diste en el proceso de registro")
             String email) {
 
         flash.clear();
         if (validation.hasErrors()) {
             params.flash();
         } else {
             Usuario foundUser = Usuario.find("email", email).first();
             if (foundUser == null) {
                 params.flash();
                 flash.error("el correo electrónico no pertenece a ningún usuario registrado");
             } else {
                 foundUser.resetPassword();
                 flash.success("Se ha enviado tu nueva contraseña a tu dirección de correo electrónico");
             }
         }
 
         render("Application/lost_password.html");
     }
 
     public static void signupConfirm(String email, String username) {
         render(email, username);
     }
 
     public static void confirm(String uuid) {
         Usuario confirmado = Usuario.confirm(uuid);
         if (confirmado != null) {
             try {
                 Security.authenticate(confirmado.email, confirmado.password);
                 Admin.index();
             } catch (Throwable e) {
                 e.printStackTrace();
                 error("No se puede entrar en 120minutos");
             }
         } else {
             notFound("El código de confirmación no es válido");
         }
     }
 
 }
