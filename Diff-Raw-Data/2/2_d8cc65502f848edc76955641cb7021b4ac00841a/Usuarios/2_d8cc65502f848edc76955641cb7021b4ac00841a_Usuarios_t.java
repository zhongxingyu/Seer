 package controllers;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.data.Form;
 import models.Cliente;
 import models.Empleado;
 import models.Pelicula;
 import views.html.index;
 import java.util.*;
 
 public class Usuarios extends Controller {
 
 	private static Form<Cliente> formCliente = Form.form(Cliente.class);
 
   
     public static Result index() {
         return ok(index.render(Pelicula.findAll(), formCliente));
     }
 
     public static Result login() {
     	Form<Cliente> formularioRecibido = formCliente.bindFromRequest();
     	
     	String login = formularioRecibido.field("login").value();
     	String password = formularioRecibido.field("password").value();
     	
         Cliente cliente = null;
         for (Cliente c: Cliente.findAll()) {
             if (c.getLogin().equals(login))
                 cliente = c;
         }
     	if (cliente == null || !password.equals(cliente.getPassword())) {
    		return badRequest(index.render(Pelicula.findAll(), formCliente));
     	} else {
     		return redirect(routes.Usuarios.index());
     	}
     }
     
     public static Result rellenarDb() {
     	Cliente cliente = new Cliente();
     	cliente.setPassword("pass");
     	cliente.setLogin("login");
     	cliente.save();
     	Empleado empleado = new Empleado();
     	empleado.setPassword("pass");
     	empleado.setLogin("empleado");
     	empleado.save();
     	Empleado admin = new Empleado();
     	admin.setPassword("pass");
     	admin.setLogin("admin");
     	admin.save();
     	return redirect(routes.Usuarios.index());
     }
   
 }
