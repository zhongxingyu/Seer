 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.entrecine4.business.SessionStateService;
 import com.entrecine4.infraestructure.*;
 import com.entrecine4.*;
 
 import play.*;
 import play.api.libs.Crypto;
 import play.mvc.*;
 import play.data.Form;
 import models.*;
 
 import views.html.*;
 
 
 public class Application extends Controller {
 	
 	static Form<User> userForm = Form.form(User.class);
 	static Form<PaymentData> paymentForm = Form.form(PaymentData.class);
     static Form<LockSeat> lockSeatForm = Form.form(LockSeat.class);
     public static int aux;
   
     public static Result index() {
         String username = getLoggedUser();
         List<Movie> movies = Factories.services.createMoviesService().getMovies();
         if(Factories.services.createUserService().get(username) == null) // user !exists?
             return ok(index.render(null,movies, userForm));
         return ok(index.render(username,movies, userForm));
     }
     
     private static String getLoggedUser()
     {
     	String user = null;
     	
     	//Get cookie and decrypt it
     	Http.Cookie cookie = request().cookies().get("user");
         if(cookie != null) {
             user = Crypto.decryptAES(cookie.value()); //if not null decrypt
         }
          
          return user;
     }
 
     public static Result registro() 
     {
     	if(Factories.services.createUserService().get(getLoggedUser()) != null)
     		return redirect(routes.Application.index());
         return ok(registro.render(userForm));
     }
     
     public static Result register()
     {
     	Form<User> filledForm = userForm.bindFromRequest();
     	if(filledForm.hasErrors()) 
     	{
     		return redirect(routes.Application.registro());
     	}
     	else
     	{
     		//Getting form data
     		String name=filledForm.field("txt_Nombre").value();
     		String surnames=filledForm.field("txt_Apellidos").value();
     		String username=filledForm.field("txt_NombredeUsuario").value();
     		String email=filledForm.field("email").value();
     		String password=filledForm.field("pwd_Contraseña").value();
     		String repass=filledForm.field("pwd_Repitalacontraseña").value();
     		
     		if(!password.equals(repass))
     			return redirect(routes.Application.registro());
     		else
     		{
     			User user=new User(0, username, password, name, surnames, email);
     			if(Factories.services.createReservationService()
     					.validateUserData(user)==null)
     				return redirect(routes.Application.registro());
     			Factories.services.createUserService().save(user);
     		}
     	}
     	return redirect(routes.Application.index());
     }
 
     public static Result pelicula(Long id) 
     {
     	//Get the movie
         Movie movie = Factories.services.createMoviesService().findById(id);
         if(movie==null)
         	return redirect(routes.Application.error());
         //Get the sessions
         List<Session> sessions=Factories.services
         		.createSessionService().findByMovie(movie.getName());
         
         Session s=null;
         Session s2=null;
         for(int i=0;i<sessions.size();i++)
         {
         	s=sessions.get(i);
         	for(int j=0;j<sessions.size();j++)
         	{
         		s2=sessions.get(j);
         		if(s.getDay().equals(s2.getDay()) 
         				&& s.getMovieTitle().equals(s2.getMovieTitle()) 
         				&& s.getTime()==s2.getTime() 
         				&& s.getId()!=s2.getId())
         		{
         			sessions.remove(j--);
         		}
         	}
         }
         
         return ok(pelicula.render(getLoggedUser(),movie, userForm, sessions));
     }
     
     public static Result login() {
     	Form<User> filledForm = userForm.bindFromRequest();
         if(filledForm.hasErrors()) {
             filledForm.reject("password", "Los datos de login están mal");
         } else {
             String username = filledForm.field("username").value();
             String password = filledForm.field("password").value();
             User user = Factories.services.createUserService().login(username, password);
             if(user == null) {
                 filledForm.reject("password", "Los datos de login están mal");
             } else {
       //          session().put("user", username); // this doesn´t work, FUCKING BUG
                 response().setCookie("user", Crypto.encryptAES(username), -1);
                 List<Movie> movies = Factories.services.createMoviesService().getMovies();
                 return redirect(routes.Application.index());
             }
         } return redirect(routes.Application.index());
     }
 
     public static Result logout() {
         response().discardCookie("user");
         return redirect(routes.Application.index());
     }
     
     public static Result plataformaPago(){
         Form filledForm = paymentForm.bindFromRequest();
         Long sessionId = Long.valueOf(filledForm.field("sessionId").value());
         int row = Integer.valueOf(filledForm.field("row").value());
         int column = Integer.valueOf(filledForm.field("column").value());
         Session session = Factories.services.createSessionService().findById(sessionId);
         SessionState sessionState = new SessionState(session.getRoomId(),
                 row, column, session.getDay(), sessionId);
         Factories.services.createSessionStateService().saveSessionState(sessionState); //lock seat
        return ok(plataformaPago.render(getLoggedUser(), userForm));
     }
     
     public static Result pay(){
     	Form filledForm = paymentForm.bindFromRequest();
     //	System.out.println("FORMULARIO:\n" + filledForm.toString());
     	/*Ahora debo llamar al método de pasarela de pago de la API que me devuelve
     	si ha sido posible completar la transacción o no, en función de lo cual
     	redirijo a la página de error o a la de agradecimiento */
     	String numeroTarjeta = filledForm.field("numeroTarjeta").value();
 //    	System.out.println("Numero Tarjeta: "+ numeroTarjeta);
     	String tipoTarjeta  = filledForm.field("tipoTarjeta").value();
 //    	System.out.println("Tipo Tarjeta: "+  tipoTarjeta);
     	String codigoSeguridad = filledForm.field("codigoSeguridad").value();
 //    	System.out.println("Codigo de seguridad: "+ codigoSeguridad);
     	String fechaCaducidad = filledForm.field("fechaCaducidad").value();
 //    	System.out.println("Fecha de caducidad: "+ fechaCaducidad);
 
     	/*ESTE CONDICIONAL NO FUNCIONA: Debería funcionar, pero salta error en tiempo de ejecución,
     	 * como que no existe la función a la que se está llamando. Revistar qué es lo que no funciona.*/
     	if(PaymentGateway.pay(numeroTarjeta, tipoTarjeta, codigoSeguridad, fechaCaducidad))
     		return redirect(routes.Application.finReservaOk());
     	else
             return redirect(routes.Application.finReservaWrong());
 
     }
     
     public static Result finReservaOk(){
    	return ok(finReservaOk.render(getLoggedUser(), userForm));
     }
     
     public static Result finReservaWrong(){
    	return ok(finReservaWrong.render(getLoggedUser(), userForm));
     }
 
     public static Result butacas(Long date, Long session, String nombre) {
         List<Session> sessions = Factories.services.createSessionService().findByDateTimeAndFilmName(new Date(date), session, nombre);
         List<Room> rooms = new ArrayList<Room>();
         List<SessionStateHelper> states = new ArrayList<SessionStateHelper>();
         for(Session s : sessions) {
             rooms.add(Factories.services.createRoomService().findById(s.getRoomId()));
         }
         if(rooms.size()>0) {
             SessionStateHelper ssh = new SessionStateHelper();
             return ok(butacas.render(getLoggedUser(), rooms, sessions, userForm, ssh, lockSeatForm));
         }
         else
             return error();
     }
     
     public static Result error(){
    	return ok(error404.render(getLoggedUser(), userForm));
     }
 }
