 package controllers;
 
 import impl.entrecine4.business.SimplePurchasesService;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 import com.entrecine4.business.PurchasesService;
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
     static Form auxForm;
     static User userForPayment;
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
        if(user.equals(""))
            return null; //if someone enters one time as a logged user when system remove that, the cookie is an empty string
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
     	Form filledForm = userForm.bindFromRequest();
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
         
         Form filledForm = userForm.bindFromRequest();
         if(getLoggedUser() == null){
         userForPayment=new User();
         userForPayment.setEmail(filledForm.field("email").value());}
         else  {
         	userForPayment = Factories.services.createUserService().get(getLoggedUser());
         }
 
         // Si el email no es válido se devuelve a la página de datosUsuarioPago indicando el error
         
         if(userForPayment.getEmail() == null || !userForPayment.getEmail().contains("@")){
         	User user = Factories.services.createUserService().get(getLoggedUser());
         	return ok(datosUsuarioPago.render(getLoggedUser(), userForm, user, "Email incorrecto"));
         }
         
         
         
         	// Como tiene todos los datos supongo que le ha dado a Registrar y Continuar.
 //        	if(filledForm.hasErrors()) 
 //        	{
 //        		return redirect(routes.Application.datosUsuarioPago());
 //        	}
 //        	else
 //        	{
         		//Getting form data
         		userForPayment.setName(filledForm.field("txt_Nombre").value());
         		userForPayment.setSurnames(filledForm.field("txt_Apellidos").value());
         		userForPayment.setUsername(filledForm.field("txt_NombredeUsuario").value());
         		userForPayment.setEmail(filledForm.field("email").value());
         		userForPayment.setPassword(filledForm.field("pwd_Contraseña").value());
         		String repass=filledForm.field("pwd_Repitalacontraseña").value();
         		
         		// There's no need to check the name since it was already checked and returned error in case there was one.
                 if(!userForPayment.getName().equals("") && !userForPayment.getSurnames().equals("") && !userForPayment.getPassword().equals("")){
         		
         		if(!userForPayment.getPassword().equals(repass)){
         			User user = Factories.services.createUserService().get(getLoggedUser());
             		return ok(datosUsuarioPago.render(getLoggedUser(), userForm, user, "Las contraseñas no coinciden"));
         		} else
         		{
         			System.out.println("Las contraseñas no son iguales y entra");
         			if(Factories.services.createReservationService()
         					.validateUserData(userForPayment)==null)
         				return redirect(routes.Application.datosUsuarioPago());
 //        			Aquí debemos comprobar que el usuario no exista, en cuyo caso volvemos a la página de datosUsuariosPago con el error
         			System.out.println("AQUÍ COMPROBAMOS QUE EL USUARIO NO EXISTA YA");
         			if(Factories.services.createUserService().get(userForPayment.getUsername()) != null){
         				User user = Factories.services.createUserService().get(getLoggedUser());
                 		return ok(datosUsuarioPago.render(getLoggedUser(), userForm, user, "El usuario " + userForPayment.getUsername() + " ya existe"));
         			}
         				
         			// Since the user does not exist, everything is OK. Therefore, we're going to create it.
         			Factories.services.createUserService().save(userForPayment);
         			response().setCookie("user", Crypto.encryptAES(userForPayment.getUsername()), -1);
         		}
 //        	}
         	return ok(plataformaPago.render(userForPayment.getUsername(), userForm));        	
         }
         
         Long sessionId = Long.valueOf(auxForm.field("sessionId").value());
         int row = Integer.valueOf(auxForm.field("row").value());
         int column = Integer.valueOf(auxForm.field("column").value());
         
         Session session = Factories.services.createSessionService().findById(sessionId);
         SessionState sessionState = new SessionState(session.getRoomId(),
                 row, column, session.getDay(), sessionId);
         Factories.services.createSessionStateService().saveSessionState(sessionState); //lock seat
         return ok(plataformaPago.render(getLoggedUser(), userForm));
     }
     
     public static Result pay(){
     	Form filledForm = paymentForm.bindFromRequest();
 
     	//Default values to prevent exceptions
     	String numeroTarjeta;//="1";
     	String tipoTarjeta;//="Visa";
     	String codigoSeguridad;//="55";
     	String fechaCaducidad;//="01/01/2101";
     	
     	int sessionID = Integer.parseInt(auxForm.field("sessionId").value());
     	
     	int row = Integer.parseInt(auxForm.field("row").value());
     	int column = Integer.parseInt(auxForm.field("column").value());
     	
     	List<Map<String, Integer>> seatsList = new ArrayList<Map<String, Integer>>();
     	Map map = new HashMap<String, Integer>();
     	map.put("ROW", row);
     	map.put("COLUMN", column);
     	seatsList.add(map);
     	
     	System.out.println("ID DE USUARIO: " + userForPayment.getId());
     	
     	List<Session> sessions = Factories.services.createSessionService().getSessions();
     	
     	String movie_title = null;
     	Long movie_id = 0L;
     	
     	for(Session session : sessions)
     		if(session.getId() == sessionID)
     			movie_title = session.getMovieTitle();
     	
     	
     	if(movie_title != null){
     		List<Movie> movies = Factories.services.createMoviesService().getMovies();
     		for(Movie movie : movies)
     			if(movie.getName().equals(movie_title))
     				movie_id = movie.getId();
     	}
     		
     	
     	
    
     	numeroTarjeta = filledForm.field("numeroTarjeta").value();
     	tipoTarjeta  = filledForm.field("tipoTarjeta").value();
     	codigoSeguridad = filledForm.field("codigoSeguridad").value();
     	fechaCaducidad = filledForm.field("fechaCaducidad").value();
 
     	if(numeroTarjeta == null || tipoTarjeta == null || codigoSeguridad == null || fechaCaducidad == null)
         	return ok(plataformaPago.render(userForPayment.getUsername(), userForm));        	
     	
     	if(PaymentGateway.pay(numeroTarjeta, tipoTarjeta, codigoSeguridad, fechaCaducidad))
     	{
     		String ticket_id_code = TicketIDCodeManager.generateCode(sessionID, seatsList);
     		Purchase p = new Purchase(0L, userForPayment.getId(), movie_id, ticket_id_code, 1, 0);
     		/*  Generamos el código QR y enviamos el correo */
     		//GenerateQR.generate(ticket_id_code);
     		//SendEmail.sendNewMail(userForPayment.getEmail(), "Imagen.png");
     		System.out.println(p.getId() + "   " + p.getMovie_id() + "   " + p.getTicket_id_code() + "   " + p.getPaid() + "   " + p.getCollected() + "   "+ p.getUser_id());
     		PurchasesService ps = Factories.services.createPurchasesService();
     		/* DA EXCEPCIÓN. SI SE PASAN TODOS LOS PARÁMETROS A MANO TAMBIÉN DA. SI SE PONEN ESTAS 3 LÍNEAS EN UNA CLASE DENTRO DE LA API
     		 * EN EL PUBLIC STATIC VOID MAIN FUNCIONA.
     		 */
     		ps.savePurchase(p);
     		return redirect(routes.Application.finReservaOk());
     	}
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
             return ok(butacas.render(getLoggedUser(), rooms, sessions, userForm, Factories.services.createSessionStateService().getSessionStates(), lockSeatForm));
         }
         else
             return error();
     }
     
     public static Result error(){
     	return ok(error404.render(getLoggedUser(), userForm));
     }
     
     
     public static Result datosUsuarioPago()
     {
     	auxForm=lockSeatForm.bindFromRequest();
     	User user = Factories.services.createUserService().get(getLoggedUser());
     	return ok(datosUsuarioPago.render(getLoggedUser(), userForm, user, ""));
     }
     
     public static boolean checkFree(List<SessionState> sst, int row, int column, Long session, Long room)
     {
     	for(SessionState s: sst)
     	{
     		if(s.getRow() == row && s.getColumn() == column &&
     				s.getSession() == session && s.getRoomId() == room)
     			return false;
     	}
     	
     	return true;
     }
    
 }
