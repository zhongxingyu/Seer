 /*****************************************************************************
  * Copyright (c) 2011 [OpenGisTeam]                                           *
  ******************************************************************************/
 package code.google.com.opengis.gestion;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 import javax.swing.JOptionPane;
 
 import code.google.com.opengis.gestionDAO.ConectarDBA;
 import code.google.com.opengis.gestionDAO.UsuariosDAO;
 
 /**
  * @author Juan Carlos Garca Clase que valida los datos de los usuarios que
  *         acabamos de insertar desde nuestra clase visual. Adems, realiza las
  *         llamadas a los mtodos del paquete DAO.
  * 
  *         ltima modificacin 14/11/11
  * 
  */
 
 public class Usuarios {
 
 	private String dni;
 	private String nombre;
 	private String apellidos;
 	private String telefono;
 	private String direccion;
 	private String poblacion;
 	private String fechaNac;
 	private String password;
 	private String provincia;
 	private String cp;
 	private String tipo;
 	private Boolean valido;
 	private String email;
 	private UsuariosDAO enlace;
 
 	private boolean existe;
 	private boolean activo;
 
 	/**
 	 * Constructor de la clase Usuarios
 	 * 
 	 * En el momento que llamamos a la clase Usuario debemos pasarle los
 	 * siguientes parametros
 	 * 
 	 * @param Dni
 	 *            El DNI del usuario que vamos a introducir
 	 * @param Nombre
 	 *            El Nombre del Usuario que vamos a introducir
 	 * @param Apellidos
 	 *            Los Apellidos del Usuario que vamos a introducir
 	 * @param Telefono
 	 *            El Telefono del usuario que vamos a introducir
 	 * @param Direccion
 	 *            La direccin del Usuario que vamos a introducir
 	 * @param Poblacion
 	 *            La poblacin del Usuario que vamos a introducir
 	 * @param fecha_nac
 	 *            La fecha de Nacimiento del Usuario que vamos a introducir
 	 */
 
 	public Usuarios(String Dni, String Nombre, String Apellidos,
 			String Telefono, String Direccion, String Poblacion,
 			String Provincia, String Cp, String fecha_nac, String contrasea,
 			String tipo, String email) {
 
 		this.dni = Dni;
 		this.nombre = Nombre;
 		this.apellidos = Apellidos;
 		this.telefono = Telefono;
 		this.direccion = Direccion;
 		this.poblacion = Poblacion;
 		this.fechaNac = fecha_nac;
 		this.password = contrasea;
 		this.cp = Cp;
 		this.tipo = tipo;
 		this.provincia = Provincia;
 		this.email = email;
 		this.valido = false;
 
 	}
 
 	/**
 	 * Getter de la clase Usuario.
 	 * 
 	 * @return Devuelve True si los datos son correctos. Devuelve False si los
 	 *         datos son incorrectos.
 	 */
 
 	public Boolean getValido() {
 
 		return valido;
 
 	}
 
 	/**
 	 * Este mtodo validar los datos que el usuario introduzca en la clase
 	 * visual. En el caso de que algn dato no sea correcto mostrar un mensaje
 	 * de error y cargar valido con False. A la hora de llamar los siguientes
 	 * mtodos comprobaremos si este nos ha dado FALSE, en ese caso no podremos
 	 * hacer nada hasta que no corrijamos el error.
 	 * 
 	 */
 
 	public String calcularDNI(String dni) {
 		int pletra;
 		String aux = "";
 		String[] arrayLetra = { "T", "R", "W", "A", "G", "M", "Y", "F", "P",
 				"D", "X", "B", "N", "J", "Z", "S", "Q", "V", "H", "L", "C",
 				"K", "E", "T" };
 		for (int x = 0; x < 8; x++) {// este for nos cojera los primeros
 										// carcteres y los guardara en el
 										// string aux
 			aux = aux + dni.charAt(x);
 		}
 		pletra = Integer.parseInt(aux);
 		pletra = pletra % 23;
 		aux = aux + arrayLetra[pletra];
 
 		return dni;
 	}
 
 	/**
 	 * Clase que valida el DNI, y lo coloca en formato 00000000L
 	 * 
 	 * @param dni
 	 * @return boolean que nos determinar si el dni introducido es verdadero o
 	 *         falso.
 	 */
 	public boolean validarDni(String dni) {
 		int pletra;
 		String aux = "";
 		String aux2 = "";
 		String[] arrayLetra = { "T", "R", "W", "A", "G", "M", "Y", "F", "P",
 				"D", "X", "B", "N", "J", "Z", "S", "Q", "V", "H", "L", "C",
 				"K", "E", "T" };
 		if (dni.length() != 9) {
 			JOptionPane.showMessageDialog(null,
 					"Error. El DNI solo puede contener 9 caracteres");
 
 			this.valido = false;
 			return false;
 		} else {
 			for (int x = 0; x < 8; x++) {// este for nos cojera los 8 primeros
 											// carcteres y los guardar en el
 											// string aux
 				aux = aux + dni.charAt(x);
 			}
 			try {
 				pletra = Integer.parseInt(aux); // si no fueran enteros
 												// saldriamos
 												// del metodo con un false
 			} catch (NumberFormatException ex) {
 				JOptionPane.showMessageDialog(null,
 						"LOS 8 PRIMEROS DIGITOS HAN DE SER ENTEROS");
 				valido = false;
 				return false;
 			}
 			pletra = pletra % 23;
 			aux2 = dni.charAt(dni.length() - 1) + "";
 
 			if (arrayLetra[pletra].equalsIgnoreCase(aux2)) {
 				dni = aux + arrayLetra[pletra];// lo colocamos en formato de 9
 												// carcteres
 				return true;
 			} else {
 				JOptionPane
 						.showMessageDialog(null,
 								"El nmero de DNI no se corresponde con la letra introducida.");
 				valido = false;
 				return false;
 			}
 		}
 	}
 	/**
 	 * Mtodo que comprueba si el texto que le pasamos es numrico o est vacio.
 	 * @param texto Texto a comprobar
 	 * @return devuelve si el texto es vlido o no.
 	 */
	public boolean validarTexto(String texto) {
 		Boolean r = isInteger(texto);
 
 		if (r.equals(true) || texto.length() < 2) {
 
			JOptionPane.showMessageDialog(null, "Error. El campo " + texto
 					+ "no puede ser numrico ni esta vacos");
 			this.valido = false;
 			return false;
 		} else {
 			return true;
 
 		}
 	}
 
 	public void validarDatos() {
 
 		if (validarDni(dni) == false) {
 			this.valido = false;
 		} else {
 
 			Boolean r = isInteger(this.nombre);
 
			if (validarTexto(this.nombre) || validarTexto(this.apellidos)
					|| validarTexto(this.direccion)
					|| validarTexto(this.poblacion)
					|| validarTexto(this.provincia) || validarTexto(this.email)) {
 
 				this.valido = false;
 
 			} else {
 
 				r = isInteger(this.telefono);
 
 				if (this.telefono.length() != 9 || r.equals(false)) {
 
 					JOptionPane
 							.showMessageDialog(null,
 									"Error. El nmero de telefono tiene que tener 9 dgitos");
 					this.valido = false;
 
 				} else {
 
 					Date fechaAhora = new Date();
 
 					if (this.fechaNac.equals("")) {
 
 						JOptionPane
 								.showMessageDialog(null,
 										"La fecha de nacimiento no puede estar en blanco");
 						this.valido = false;
 
 					} else {
 
 						@SuppressWarnings("deprecation")
 						Date fechaNac = new Date(this.fechaNac);
 
 						if (this.fechaNac.length() != 10
 								|| fechaNac.getTime() > fechaAhora.getTime()) {
 
 							JOptionPane.showMessageDialog(null,
 									"Error. La fecha indicada no es correcta");
 							this.valido = false;
 
 						} else {
 
 							r = isInteger(this.cp);
 
 							if (this.cp.length() != 5 || r.equals(false)) {
 
 								JOptionPane
 										.showMessageDialog(null,
 												"Error. El Cdigo Postal debe tener 5 cifras y ser solo numrico");
 
 							} else {
 
 								r = isInteger(this.password);
 
 								if (this.password.length() == 0
 										|| r.equals(true)) {
 
 									JOptionPane
 											.showMessageDialog(null,
 													"Error. La Contrasea es obligatoria y debe ser alfanumrica");
 
 								} else {
 
 									this.valido = true; // En
 														// el
 														// caso
 														// de
 														// que
 														// todos
 														// los
 														// datos
 														// sean
 														// correctos
 														// devolveremos
 														// True
 
 								}
 							}
 
 						}
 
 					}
 
 				}
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * Este mtodo utiliza la clase UsuariosDAO, creando un objeto del mismo con
 	 * los parametros indicados en su constructor. Despus, ejecuta el mtodo
 	 * crear Usuario, definido en la clase UsuariosDAO.
 	 * 
 	 * @throws SQLException
 	 * 
 	 */
 
 	public void crearUsuario() throws SQLException {
 
 		existe = false;
 		activo = false;
 		ConectarDBA.comprobarExiste("usuario", "dni", this.dni, true);
 
 		if (existe == true) {
 
 			if (activo == true) {
 				JOptionPane.showMessageDialog(null,
 						"El DNI ya existe y se encuentra Activado");
 
 			} else {
 				JOptionPane.showMessageDialog(null,
 						"El DNI ya existe y se encuentra Inactivo");
 
 			}
 
 		} else {
 			String sentencia = "INSERT INTO `dai2opengis`.`usuario` (`dni`, `nombre`, `apellidos`, `email`, `password`, `tipo`, `veces`, `telfono`, `direccin`, `poblacin`, `provincia`, `cp`, `fecha_nacimiento`, `activo`) VALUES ('"
 					+ this.dni
 					+ "', '"
 					+ this.nombre
 					+ "', '"
 					+ this.apellidos
 					+ "', '"
 					+ this.email
 					+ "', '"
 					+ this.password
 					+ "', '"
 					+ this.tipo
 					+ "', '0', '"
 					+ this.telefono
 					+ "', '"
 					+ this.direccion
 					+ "', '"
 					+ this.poblacion
 					+ "', '"
 					+ this.provincia
 					+ "', '"
 					+ this.cp
 					+ "', '"
 					+ this.fechaNac + "', '0')";
 			ConectarDBA.modificar(sentencia);
 
 			JOptionPane.showMessageDialog(null,
 					"Se ha dado de alta el nuevo usuario");
 
 		}
 
 		ConectarDBA.cerrarCon();
 
 	}
 
 	/**
 	 * Este mtodo utiliza la clase UsuariosDAO, creando un objeto del mismo con
 	 * los parametros indicados en su constructor. Despus, ejecuta el mtodo
 	 * borrar Usuario, definido en la clase UsuariosDAO.
 	 * 
 	 * @throws SQLException
 	 * 
 	 */
 
 	public void borrarUsuario() throws SQLException {
 
 		existe = false;
 
 		ConectarDBA.comprobarExiste("usuario", "dni", this.dni, false);
 
 		if (existe == true) {
 
 			String sentencia = "DELETE FROM `usuario` WHERE `dni` = '"
 					+ this.dni + "'";
 			ConectarDBA.modificar(sentencia);
 
 			JOptionPane.showMessageDialog(null,
 					"Usuario eliminado correctamente");
 
 		} else {
 
 			JOptionPane.showMessageDialog(null, "El usuario no existe");
 		}
 
 		ConectarDBA.cerrarCon();
 	}
 
 	public void modificarUsuario() {
 		enlace = new UsuariosDAO(this.dni, this.nombre, this.apellidos,
 				this.telefono, this.direccion, this.poblacion, this.provincia,
 				this.cp, this.fechaNac, this.password, this.tipo, this.email);
 		try {
 
 			enlace.MoficicarUsuario();
 
 		} catch (SQLException e) {
 
 			JOptionPane
 					.showMessageDialog(null, "Error al Modificar el usuario");
 		}
 
 	}
 
 	public void desactivarUsuario(String dni) throws SQLException {
 		// System.out.println(dni);
 		ConectarDBA.desactivar("usuario", "dni", dni);
 	}
 
 	public void activarUsuario(String dni) throws SQLException {
 		// System.out.println(dni);
 		ConectarDBA.activar("usuario", "dni", dni);
 	}
 	
 
 
 	/**
 	 * Este mtodo comprueba si una cadena String introducida es un Nmero. Si
 	 * es un nmero, devuelve true.
 	 * 
 	 * @param input
 	 *            Cadena de texto que queremos comprobar si es un nmero
 	 * @return Devolvemos True si es un nmero, devolvemos False si no lo s.
 	 * 
 	 */
 
 	public boolean isInteger(String input) {
 		try {
 			Integer.parseInt(input);
 			return true;
 		} catch (Exception e2) {
 			return false;
 		}
 
 	}
 
 }
