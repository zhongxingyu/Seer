 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Expr;
 
 import play.data.format.Formats;
 import play.db.ebean.Model;
 
 @Entity
 @Table(name="usuario")
 public class Usuario extends Model {
 	
 	@Id
 	@Column(length=50, nullable=false)
 	public String correo;
 	
     @Formats.NonEmpty
     @Column(length=60, nullable=false)
 	public String nombre;
 	
     @Formats.NonEmpty
     @Column(length=20, nullable=false)
 	public String password;
 	
     @Formats.NonEmpty
     @Column(length=60, nullable=false)
 	public String ciudad;
     
     @Formats.NonEmpty
     @Column(nullable=true)
 	public Integer telefono;
 	
     @Formats.NonEmpty
     @Column(length=300, nullable=true)
 	public String leyenda;
 	
     @Formats.NonEmpty
     @Column(length=350, nullable=false)
 	public String imagen;
     
     @Formats.NonEmpty
     @Column(length=7, nullable=false)
 	public String colorTareaAlta;
     
     @Formats.NonEmpty
     @Column(length=7, nullable=false)
 	public String colorTareaMedia;
     
     @Formats.NonEmpty
     @Column(length=7, nullable=false)
 	public String colorTareaBaja;    
 	
     @Formats.NonEmpty
     @Column(nullable=false)
 	public Integer id_verificador;
 	
 	@Formats.NonEmpty
     @Column(length=11, nullable=false)
 	public String estado;	
 	
 	@Formats.DateTime(pattern="dd/MM/yyyy")
 	@Column(nullable=true)
 	public Date inicioSesion;
 	
 	// Consultas
 	
 	public static Finder<String,Usuario> find = new Finder<String,Usuario>(String.class, Usuario.class);
 	
 	/**
 	 * Verifica si el usuario esta registrado en la BD.
 	 * 
 	 * @param correo 
 	 * @param password
 	 * @return un objeto con los datos si se encuentra en la BD.
 	 */
 	public static Usuario authenticate(String correo, String password) {
         return find.where()
             .eq("correo", correo)
             .eq("password", password)
             .findUnique();
     }
 
 	/**
 	 * Verifica si el usuario que se va a registrar existe en la BD.
 	 * 
 	 * @param correo
 	 * @return si es mayor que cero, el usuario ya esta registrado.
 	 */
     public static boolean esMiembro(String correo) {
         return find.where()
             .eq("correo", correo)
             .findRowCount() > 0;
     }
     
     /**
      * Comprueba que el id_verificador que se le asigna a una cuenta
      * no exista en la BD, para asignarselo a otro usuario.
      * 
      * @param idVerificador
      * 		Parametro que es generado desde Application.java
      * @return si es mayor que cero el id_verificador ya se encuentra
      * en la BD, por lo tanto se genera otro en el Controlador.
      */
     public static boolean estaIdVerificador(Integer idVerificador) {
     	return find.where()
     		.eq("id_verificador", idVerificador)
     		.findRowCount() > 0;
     }
     
     /**
      * Comprueba la cuenta para activarla, verificando que el id
      * llegado en la url este en la BD y si es unico.
      * 
      * @param correo
      * @param idVerificador
      * 		llega desde la url enviada al correo del usuario registrado
      * @return si es unico retorna un 1.
      */
     public static boolean verificaCuenta(String correo, Integer idVerificador) {
     	return find.where()
     		.eq("correo", correo)
     		.eq("id_verificador", idVerificador)
     		.findRowCount() == 1;
     }
 
     /**
      * Actualiza el estado de la cuenta del usuario despues de
      * haber activado la cuenta.
      * 
      * @param correo
      * @param idVerificador
      */
     public static void actualizaEstado(String correo, Integer idVerificador) {
     	Ebean.createSqlUpdate(
     			"update usuario set estado = 'Activada' where " +
     			"correo = '"+correo+"' and id_verificador = '"+idVerificador+"'"
     			).execute();
     }
 
     /**
      * Verifica que la cuenta este activada, sino al usuario no
      * se le dejara ingresar al sistema.
      * 
      * @param correo
      * @return si esta "Activada" retorna un 1.
      */
     public static boolean cuentaActivada(String correo) {
     	return find.where()
     			.eq("correo", correo)
     			.eq("estado", "Activada")
     			.findRowCount() == 1;
     }
     
     /**
      * Obtiene la password de un determinado usuario.
      * 
      * @param correo
      * @return si la password se encuentra en la Bd la retorna,
      * en caso contrario retorna vacio ("").
      */
     public static String getPassword(String correo) {
     	Usuario usuario = Ebean.find(Usuario.class)  
     	        .select("password")  
     	        .where().eq("correo", correo)  
     	        .findUnique();
     	try {
 	    	if(usuario.password.isEmpty()) {
 	    		return "";
 	    	} else {
 	    		return usuario.password;
 	    	}
     	} catch(Exception e) {}
 		return "";
     }
 
     /**
      * Actualiza el nombre del usuario.
      * 
      * @param nombre
      * 		Nuevo nombre del usuario.
      * @param correo
      */
     public void setNombre(String nombre, String correo) {
         Usuario usuario = find.ref(correo);
         usuario.nombre = nombre;
         usuario.update();    
     }
 
     /**
      * Actualiza el nombre y la imagen del usuario.
      * 
      * @param correo
      * @param nombre
      * 		Nuevo nombre.
      * @param imagen
      * 		Nueva imagen.
      */
     public void setImagen(String correo, String nombre, String imagen) {
         Usuario usuario = find.ref(correo);
         usuario.nombre = nombre;
         usuario.imagen = imagen;
         usuario.update();    
     }
     
     /**
      * Actualiza la password del usuario.
      * 
      * @param correo
      * @param password
      * 		Nueva password.
      */
     public void setPassword(String correo, String password) {
     	Usuario usuario = find.ref(correo);
     	usuario.password = password;
     	usuario.update();
     }
     
     /**
      * Actualiza los colores de las tareas del usuario.
      * 
      * @param correo
      * @param colorA Color de la tarea alta.
      * @param colorM Color de la tarea media.
      * @param colorB Color de la tarea baja.
      */
     public void setColores(String correo, String colorA, String colorM, String colorB) {
 	    Usuario usuario = find.ref(correo);
 	    usuario.colorTareaAlta = colorA;
 	    usuario.colorTareaMedia = colorM;
 	    usuario.colorTareaBaja = colorB;
 	    usuario.update();
     }
 
     /**
      * Obtiene una lista de los usuario donde sea igual
      * o parecido al nombre o correo.
      * 
      * @param nombre
      * 		busca usuarios por el nombre.
      * @return una lista de todos los usuarios encontrados con el nombre
      */
     public static List<Usuario> listaUsuarios(String nombre){
     	return find.where()
     			.or(Expr.like("nombre", "%"+nombre+"%"),  Expr.like("correo", "%"+nombre+"%"))
     			.findList();  	
     }
     
     /**
      * Obtiene el nombre de usuario.
      * 
      * @param email
      * 		Busca el nombre por el correo del usuario.
      * @return el nombre de usuario.
      */
 	public static String getNombre(String email){
 	    Usuario usuario = Ebean.find(Usuario.class)
 	    		.select("nombre")
 	    		.where()
 	    		.eq("correo", email)
 	    		.findUnique();
 	    return usuario.nombre;
 	}
 	
 	/**
 	 * Obtiene el telefono del usuario.
 	 * 
 	 * @param email
 	 * @return
 	 */
 	public static String getTelefono(String email) {
 		Usuario usuario = Ebean.find(Usuario.class)
 	    		.select("telefono")
 	    		.where()
 	    		.eq("correo", email)
 	    		.findUnique();
 		try {
 			if (usuario.telefono.toString().isEmpty())
 				return "";
 			else
 				return usuario.telefono.toString();
 		} catch(Exception e) {}
 		return "";
 	}
 
 	/**
 	 * Obtiene la imagen del usuario.
 	 * 
 	 * @param email
 	 * 		Busca una imagen por el correo del usuario.
 	 * @return la imagen del usuario.
 	 */
 	public static String getImagen(String email) {
 		Usuario usuario = Ebean.find(Usuario.class)
 				.select("imagen")
 				.where()
 				.eq("correo", email)
 				.findUnique();
 		return usuario.imagen;
 	}
 	
 	/**
 	 * Obtiene el correo de un usuario por el nombre.
 	 * @param nombre
 	 * @return el nombre de usuario
 	 */
 	public static String getCorreo(String nombre) {
 		Usuario usuario = Ebean.find(Usuario.class)
 				.select("correo")
 				.where()
 				.eq("nombre", nombre)
 				.findUnique();
 		return usuario.correo;
 	}
 	
 	/**
 	 * Obtiene todos los usuarios.
 	 * @return
 	 */
     public static List<Usuario> listarUsuarios(){
     	return find.all();
   	}
     
 	/**
 	 * Obtiene la ciudad del contacto a traves del correo
 	 */
 	public static String getCiudad(String email) {
 		Usuario usuario = Ebean.find(Usuario.class)
 				.select("ciudad")
 				.where()
 				.eq("correo", email)
 				.findUnique();
 		return usuario.ciudad;
 	}
 	
 	/**
 	 * Elimina una cuenta por completo.
 	 * 
 	 * @param correo
 	 */
 	public static void desactivarCuenta(String correo) {
 		Usuario usuario = Ebean.find(Usuario.class, correo);
 		usuario.estado = "Desactivada";
 		usuario.update();
 	}	
 	
 	/**
 	 * Bloquea una cuenta por completo.
 	 * 
 	 * @param correo
 	 */
 	public static void bloquearCuenta(String correo) {
 		Usuario usuario = Ebean.find(Usuario.class, correo);
 		usuario.estado = "Bloqueada";
 		usuario.update();
 	}	
 	
 	/**
 	 * Activa una cuenta.
 	 * 
 	 * @param correo
 	 */
 	public static void activarCuenta(String correo) {
 		Usuario usuario = Ebean.find(Usuario.class, correo);
 		usuario.estado = "Activada";
 		usuario.update();
 	}	
 }
