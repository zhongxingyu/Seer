 package controllers;
 
 import java.sql.Date;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.Empleado;
 import models.Pelicula;
 import models.Sala;
 import models.Sesion;
 import models.TipoSesion;
 import play.data.DynamicForm;
 import play.data.Form;
import play.db.ebean.Transactional;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.With;
 import views.html.admin;
 import views.html.adminSesionesDePelicula;
 import views.html.adminSesionesDeSala;
 import views.html.adminlogin;
 import controllers.filters.FiltroAdministrador;
 
 public class Administracion extends Controller {
 
 	private static Form<Pelicula> formPelicula = Form.form(Pelicula.class);
 
 	private static Form<Empleado> formEmpleado = Form.form(Empleado.class);
 
 	private static Form<TipoSesion> formTipoSesion = Form
 			.form(TipoSesion.class);
 
 	private static Form<Sala> formSala = Form.form(Sala.class);
 
 	// NAVEGACION MENU SUPERIOR
 
 	@With(FiltroAdministrador.class)
 	public static Result index() {
 		return ok(admin.render(Pelicula.findAll(), Sala.findAll(),
 				TipoSesion.findAll(), formPelicula, formSala, formTipoSesion));
 	}
 
 	// CRUD PELICULAS
 
 	@With(FiltroAdministrador.class)
 	public static Result borrarPelicula(Long id) {
 		Pelicula.findById(id).delete();
 		return redirect(routes.Administracion.index());
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result nuevaPelicula() {
 		Form<Pelicula> formularioRecibido = formPelicula.bindFromRequest();
 		if (formularioRecibido.hasErrors()) {
 			return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 					TipoSesion.findAll(), formularioRecibido, formSala, formTipoSesion));
 		} else {
 			Pelicula p = formularioRecibido.get();
 			String id = formularioRecibido.data().get("id");
 			if (!id.isEmpty()) {
 				p.setId(Long.parseLong(id));
 				p.update();
 			}
 			else
 				p.save();
 			return redirect(routes.Administracion.index());
 		}
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result editarPelicula(Long id) {
 		Pelicula p = Pelicula.findById(id);
 		Form<Pelicula> f = Form.form(Pelicula.class).fill(p);
 		return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 				TipoSesion.findAll(), f, formSala, formTipoSesion));
 	}
 
 	// CRUD TIPOS DE SESION
 	@With(FiltroAdministrador.class)
 	public static Result borrarTipoSesion(Long id) {
 		TipoSesion.findById(id).delete();
 		return redirect(routes.Administracion.index());
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result nuevoTipoSesion() {
 		Form<TipoSesion> formularioRecibido = formTipoSesion.bindFromRequest();
 		if (formularioRecibido.hasErrors()) {
 			return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 					TipoSesion.findAll(), formPelicula, formSala, formularioRecibido));
 		} else {
 			TipoSesion tp = formularioRecibido.get();
 			String id = formularioRecibido.data().get("id");
 			if (!id.isEmpty()) {
 				tp.setId(Long.parseLong(id));
 				tp.update();
 			}
 			else
 				tp.save();
 			return redirect(routes.Administracion.index());
 		}
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result editarTipoSesion(Long id) {
 		TipoSesion t = TipoSesion.findById(id);
 		Form<TipoSesion> f = Form.form(TipoSesion.class).fill(t);
 		return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 				TipoSesion.findAll(), formPelicula, formSala, f));
 	}
 
 	// CRUD SESIONES
	@SuppressWarnings("unused")
 	@With(FiltroAdministrador.class)
 	public static Result borrarSesion(Long id) {
 		Sesion s = Sesion.findById(id);
 		Sala sala = s.getSala();
		Date fecha = s.getFecha();
 		s.delete();
		List<Sesion> sesiones = Sesion.findBySalaAndFecha(sala, fecha);
 		return redirect(routes.Administracion.getSesionesDeSala(sala.getId()));
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result editarSesion(Long id) {
 		Sesion s = Sesion.findById(id);
 		Sala sala = s.getSala();
 		Date fecha = s.getFecha();
 		DynamicForm formularioRecibido = Form.form();
 		Map<String, String> datos = new HashMap<String, String>();
		datos.put("hora", s.getHora().toString());
		datos.put("tipo", s.getTipo().toString());
 		formularioRecibido.bind(datos);
 		return badRequest(adminSesionesDeSala.render(s.getSala(), fecha,
 				Sesion.findBySalaAndFecha(sala, fecha)));
 	}
 
 	@SuppressWarnings("unchecked")
	@Transactional
 	@With(FiltroAdministrador.class)
 	public static Result nuevaSesion(Long sala_id, String fecha_s) {
 		Date fecha = Date.valueOf(fecha_s);
 		Sala sala = Sala.findById(sala_id);
 		List<Sesion> sesiones = Sesion.findBySalaAndFecha(sala, fecha);
 		if (sesiones == null)
 			sesiones = new ArrayList<Sesion>();
 		DynamicForm formularioRecibido = Form.form().bindFromRequest();
 		if (formularioRecibido.hasErrors()) {
 			return badRequest(adminSesionesDeSala.render(sala, fecha, sesiones));
 		} else {
 			Map<String, String> form = formularioRecibido.get().getData();
 			Sesion sesion = new Sesion();
 			sesion.setTipo(TipoSesion.findById(Long.parseLong(form.get("tipo"))));
 			sesion.setSala(sala);
 			sesion.setFecha(fecha);
 			sesion.setHora(Time.valueOf(form.get("hora") + ":00"));
 			sesion.save();
 			return redirect(routes.Administracion.getSesionesDeSala(sala_id));
 		}
 	}
 	
 	// RELACIONAR SESIONES Y PELICULAS
 	
 	@With(FiltroAdministrador.class)
 	public static Result asignarPelicula(Long pelicula_id, Long sesion_id) {
 		Pelicula peli = Pelicula.findById(pelicula_id);
 		Sesion sesion = Sesion.findById(sesion_id);
 		sesion.setPelicula(peli);
 		sesion.save();
 		return redirect(routes.Administracion.index());
 	}
 	
 	@With(FiltroAdministrador.class)
 	public static Result desasignarPelicula(Long pelicula_id, Long sesion_id) {
 		Pelicula peli = Pelicula.findById(pelicula_id);
 		Sesion sesion = Sesion.findById(sesion_id);
 		peli.removeSesion(sesion);
 		sesion.save();
 		return redirect(routes.Administracion.index());
 	}
 
 	// CRUD SALAS
 
 	@With(FiltroAdministrador.class)
 	public static Result nuevaSala() {
 		Form<Sala> formularioRecibido = formSala.bindFromRequest();
 		if (formularioRecibido.hasErrors()) {
 			return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 					TipoSesion.findAll(), formPelicula, formSala, formTipoSesion));
 		} else {
 			Sala s = formularioRecibido.get();
 			String id = formularioRecibido.data().get("id");
 			if (!id.isEmpty()) {
 				s.setId(Long.parseLong(id));
 				s.update();
 			}
 			else
 				s.save();
 			return redirect(routes.Administracion.index());
 		}
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result editarSala(Long id) {
 		Sala s = Sala.findById(id);
 		Form<Sala> f = Form.form(Sala.class).fill(s);
 		return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 				TipoSesion.findAll(), formPelicula, f, formTipoSesion));
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result borrarSala(Long id) {
 		Sala.findById(id).delete();
 		return redirect(routes.Administracion.index());
 	}
 
 	// VISTA SESIONES
 
 	@With(FiltroAdministrador.class)
 	public static Result getSesionesDePelicula(Long peli_id) {
 		Pelicula peli = Pelicula.findById(peli_id);
 		DynamicForm formularioRecibido = Form.form().bindFromRequest();
 		if (formularioRecibido.hasErrors()) {
 			return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 					TipoSesion.findAll(), formPelicula, formSala, formTipoSesion));
 		} else {
 			try {
 				Date fecha = Date.valueOf(formularioRecibido.data().get(
 						"fechaPeli"));
 				List<Sesion> sesionesDePeli = Sesion.findByPeliculaAndFecha(peli, fecha);
 				if (sesionesDePeli == null)
 					sesionesDePeli = new ArrayList<Sesion>();
 				List<Sesion> sesionesLibres = Sesion.findByPeliculaAndFecha(null, fecha);
 				if (sesionesLibres == null)
 					sesionesLibres = new ArrayList<Sesion>();
 				return ok(adminSesionesDePelicula.render(peli, fecha, sesionesDePeli, sesionesLibres));
 			} catch (IllegalArgumentException e) {
 				return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 						TipoSesion.findAll(), formPelicula, formSala, formTipoSesion));
 			}
 		}
 	}
 
 	@With(FiltroAdministrador.class)
 	public static Result getSesionesDeSala(Long sala_id) {
 		Sala sala = Sala.findById(sala_id);
 		DynamicForm formularioRecibido = Form.form().bindFromRequest();
 		if (formularioRecibido.hasErrors()) {
 			return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 					TipoSesion.findAll(), formPelicula, formSala, formTipoSesion));
 		} else {
 			try {
 				Date fecha = Date.valueOf(formularioRecibido.data().get(
 						"fechaSala"));
 				List<Sesion> sesiones = Sesion.findBySalaAndFecha(sala, fecha);
 				if (sesiones == null)
 					sesiones = new ArrayList<Sesion>();
 				return ok(adminSesionesDeSala.render(sala, fecha, sesiones));
 			} catch (IllegalArgumentException e) {
 				return badRequest(admin.render(Pelicula.findAll(), Sala.findAll(),
 						TipoSesion.findAll(), formPelicula, formSala, formTipoSesion));
 			}
 		}
 	}
 
 	// LOGIN Y REDIRECCIONES EN SEGUNDO PLANO
 	@With(FiltroAdministrador.class)
 	public static Result logout() {
 		// Sacamos a cliente de sesion
 		session().remove("empleado");
 		return redirect(routes.Clientes.index());
 	}
 	
 	public static Result irALogin() {
 		return ok(adminlogin.render(formEmpleado));
 	}
 
 	public static Result doLogin() {
 		Form<Empleado> formularioRecibido = formEmpleado.bindFromRequest();
 
 		String login = formularioRecibido.field("login").value();
 		String pass = formularioRecibido.field("password").value();
 
 		Empleado empleado = Empleado.findByLogin(login);
 
 		if (empleado == null || !empleado.getAdmin()
 				|| !empleado.getPassword().equals(pass)) {
 			// Solamente mostramos el error en login, asi no se sabe si el error
 			// lo dio porque no existe el usuario o porque la contraseña no
 			// coincide
 			formularioRecibido.reject("login",
 					"El usuario o contraseña no es correcto");
 			return badRequest(adminlogin.render(formularioRecibido));
 		}
 
 		// Lo metemos en sesion
 		session().put("empleado", login);
 
 		// Si todo ha sido correcto vamos al index
 		return redirect(routes.Administracion.index());
 	}
 
 }
