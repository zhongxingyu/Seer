 /*****************************************************************************
  * Copyright (c) 2011 [OpenGisTeam]                                           *
  ******************************************************************************/
 package code.google.com.opengis.gestionDAO;
 
 import java.sql.*;
 
 import javax.swing.JOptionPane;
 
 import pruebasToni.AccesoBBDD;
 
 /**
  * @author Danico & Pipepito Clase que establece conexin con la base de datos y
  *         realiza las diferentes funciones con esta Ya sea Querys o Updates
  *         ltima modificacin 01/12/11
  * 
  */
 
 public class ConectarDBA {
 
 	/** Objeto del tipo conexin */
 	static Connection conexion;
 	/** Objeto del tipo Statement */
 	static Statement st;
 	static boolean existe;
 	static boolean activo;
 	static String resultado;
 
 	/**
 	 * Mtodo que nos conecta con la base de datos
 	 */
 	public static void acceder() {
 
 		// Asignamos a variables todos los datos requeridos para la conexion a
 		// la base de datos.
 		String nombreDriver = "com.mysql.jdbc.Driver";
 		// String nombreServidor = "db4free.net";
		String nombreServidor = "79.108.245.167";
		//String nombreServidor = "10.2.1.43";
 		String numeroPuerto = "3306";
 		String miBaseDatos = "dai2opengis";
 		String url = "jdbc:mysql://" + nombreServidor + ":" + numeroPuerto
 				+ "/" + miBaseDatos;
 		// String dbuser = "ivanserrano";
 		// String dbpwd = "dai20112012";
 		String dbuser = "dai2proyecto";
 		String dbpwd = "dai20112012";
 
 		try {
 			Class.forName(nombreDriver).newInstance();
 			conexion = DriverManager.getConnection(url, dbuser, dbpwd);
 			st = conexion.createStatement();
 			// System.out.println("conectaBD");
 		} catch (IllegalAccessException e) {
 			JOptionPane.showMessageDialog(null, "Error De Conexin, usuario o contrasea incorrectos");
 
 			System.err.println(e.getMessage());
 		} catch (InstantiationException e) {
 			JOptionPane.showMessageDialog(null, "Error De Conexin");
 
 			System.err.println(e.getMessage());
 		} catch (ClassNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "Error De Conexin, no se encuentra la clase");
 
 			System.err.println(e.getMessage());
 		} catch (SQLException e) {
 			System.out.println("Aqu falla");
 			JOptionPane.showMessageDialog(null, "Error De Conexin, fallo en la sintaxis SQL");
 
 			System.err.println(e.getMessage());
 
 		}
 	}
 
 	/**
 	 * Mtodo para realizar consultas a la base de datos.
 	 * 
 	 * @param sentenciaSQL
 	 *            String consulta que vamos a ejecutar
 	 * @return ResultSet devuelve el resultado de la consulta.
 	 * @throws SQLException
 	 *             devuelve un error en caso de producirse
 	 */
 	public static ResultSet consulta(String sentenciaSQL) throws SQLException {
 		// Creamos un tipo Statement que maneja las consultas
 
 		// Retorno la consulta especifica...
 		return st.executeQuery(sentenciaSQL);
 
 	}
 
 	/**
 	 * 
 	 * @param sentenciaSQL
 	 *            String con el insert/update que vamos a ejecutar
 	 * @throws SQLException
 	 *             devuelve un error en caso de producirse
 	 */
 	public static void modificar(String sentenciaSQL) throws SQLException {
 		// Creamos un tipo Statement que maneja las consultas
 
 		// Retorno la consulta especifica...
 		st.executeUpdate(sentenciaSQL);
 
 	}
 
 	/**
 	 * Mtodo que cierra la conexin con la base de datos y lo statements.
 	 * 
 	 * @throws SQLException
 	 */
 	public static void cerrarCon() throws SQLException {
 		st.close();
 		conexion.close();
 		// System.out.println("Conexin cerrada");
 
 	}
 
 	/**
 	 * Mtodo para buscar en una tabla un campo que cumpla el criterio.
 	 * 
 	 * @param tabla
 	 *            Indicamos en que tabla vamos a buscar
 	 * @param campo
 	 *            Indicamos que campo queremos comparar
 	 * @param criterio
 	 *            Indicamos el criterio de bsqueda.
 	 * @param buscarActivo
 	 *            Indicamos si queremos comprobar si est activo.
 	 * @return
 	 * @throws SQLException
 	 *             Nos devuelve error en caso de que exista.
 	 */
 	public static boolean comprobarExiste(String tabla, String campo,
 			String criterio, boolean buscarActivo) throws SQLException {
 
 		acceder();
 		String sentencia = "SELECT `" + campo + "` FROM `" + tabla
 				+ "` WHERE `" + campo + "` LIKE '" + criterio + "'";
 		ResultSet rs = consulta(sentencia);
 		resultado = new String();
 		activo = false;
 		while (rs.next()) {
 			System.out.println("Ejecuto el while");
 
 			resultado = rs.getString(1);
 			System.out.println(resultado);
 
 		}
 
 		System.out.println("Enviado: " + criterio + " esperado: "
 				+ resultado.toString());
 		if (resultado == null) {
 			existe = false;
 			System.out.println("El estado de existe es: " + existe);
 		} else if (resultado.equals(criterio)) {
 			existe = true;
 
 			System.out.println("El estado de existe es: " + existe);
 
 			rs.close();
 			if (buscarActivo) {
 				String sentencia2 = "SELECT `activo` FROM `" + tabla
 						+ "` WHERE `" + campo + "` LIKE '" + criterio + "'";
 				ResultSet rs2 = consulta(sentencia2);
 				activo = false;
 				while (rs2.next()) {
 					System.out.println("Ejecuto el segundo While");
 					activo = rs2.getBoolean(1);
 					return activo;
 				}
 				if (activo == false) {
 					activo = true;
 					return activo;
 				}
 				rs2.close();
 				System.out.println("El estado de activo es: " + activo);
 				// System.out.println("Ejecutada sentencia "+sentencia);
 
 			}
 			cerrarCon();
 		}
 		return existe;
 	}
 
 	/**
 	 * Mtodo que nos devolver un ResultSet con todos los campos de la tabla
 	 * que le indiquemos que cumplan los criterios que se establecen con los
 	 * parmetros.
 	 * 
 	 * @param tabla
 	 *            Indicamos en que tabla vamos a buscar
 	 * @param campo
 	 *            Indicamos que campo queremos comparar
 	 * @param criterio
 	 *            Indicamos el criterio de bsqueda.
 	 * @param buscarActivo
 	 *            Indicamos si queremos comprobar si est activo.
 	 * @param activo
 	 *            Indicamos el estado de activo, true o false
 	 * @return Nos devuelve el resultado de la bsqueda.
 	 * @throws SQLException
 	 */
 	public static ResultSet buscar(String tabla, String campo, String criterio,
 			boolean buscarActivo, boolean activo) throws SQLException {
 		ResultSet rs = null;
 		acceder();
 		String sentencia;
 		if (buscarActivo == false) {
 			sentencia = "SELECT * FROM `" + tabla + "` WHERE  `" + campo
 					+ "` LIKE '%" + criterio + "%'";
 		} else {
 			sentencia = "SELECT * FROM `" + tabla + "` WHERE  `" + campo
 					+ "` LIKE '%" + criterio + "%' AND `activo` = '" + activo
 					+ "'";
 		}
 
 		try {
 			rs = consulta(sentencia);
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		return rs;
 
 		// System.out.println("Ejecutada sentencia "+sentencia);
 
 	}
 	
 	/**
 	 * Mtodo que activa un campo segn el criterio que le pasemos.
 	 * @param tabla en la que se va a activar.
 	 * @param campo campo que vamos a utilizar para comparar con el criterio
 	 * @param criterio criterio que debe cumplir.
 	 * @throws SQLException
 	 */
 	public static void activar(String tabla, String campo, String criterio)
 			throws SQLException {
 		comprobarExiste(tabla, campo, criterio, true);
 		if (existe == true && activo == false) {
 			String sentencia = "UPDATE `" + tabla
 					+ "` SET `activo` = '1' WHERE `" + campo + "` LIKE '"
 					+ criterio + "'";
 			ConectarDBA.modificar(sentencia);
 			JOptionPane.showMessageDialog(null,
 					"Se ha activado correctamente");
 		}
 
 	}
 	
 	/**
 	 * Mtodo que desactiva un campo segn el criterio que le pasemos.
 	 * @param tabla en la que se va a activar.
 	 * @param campo campo que vamos a utilizar para comparar con el criterio
 	 * @param criterio criterio que debe cumplir.
 	 * @throws SQLException
 	 */
 	public static void desactivar(String tabla, String campo, String criterio)
 			throws SQLException {
 		comprobarExiste(tabla, campo, criterio, true);
 		if (existe == true && activo == true) {
 			String sentencia = "UPDATE `" + tabla
 					+ "` SET `activo` = '0' WHERE `" + campo + "` LIKE '"
 					+ criterio + "'";
 			ConectarDBA.modificar(sentencia);
 			JOptionPane.showMessageDialog(null,
 					"Se ha desactivado correctamente");
 		}
 
 	}
 
 	
 	/**
 	 * Mtodo que nos devolver un ResultSet con todos los campos de la consulta
 	 * que pasamos por parmetros.
 	 * 
 	 * @param sentencia
 	 *            Consulta que realizar el mtodo en formato SQL.
 	 * @return Nos devuelve el resultado de la bsqueda.
 	 * @throws SQLException
 	 */
 	public static ResultSet buscar(String sentencia) throws SQLException {
 		ResultSet rs = null;
 		acceder();
 
 		try {
 			rs = st.executeQuery(sentencia);
 			
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		return rs;
 
 		// System.out.println("Ejecutada sentencia "+sentencia);
 
 	}
 	
 	public static char validarLogin(String usuario, String pass) throws SQLException
 	{
 		ResultSet rs;
 		char t = 0;
 		rs=buscar("SELECT tipo FROM usuario WHERE dni LIKE '" + usuario + "'AND password LIKE '" + pass + "' AND `activo` = '0'");
 		if(rs.next())
 		{
 			t=rs.getString(1).charAt(0);
 			//System.out.println("Tipo de usuario: "+t);
 		}
 		return t;	
 	}
 	
 	
 	public static String idiomaDefecto(String usuario){
 		
 		ResultSet rs;
 		
 		ConectarDBA.acceder();
 		
 		String idioma = "";
 		
 		String sentencia= "SELECT idioma FROM usuario WHERE dni LIKE '"+usuario+"'";
 		
 		try {
 			rs = ConectarDBA.consulta(sentencia);
 			
 			rs.next();
 			
 			idioma = rs.getString(1);
 			
 			ConectarDBA.cerrarCon();
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		if(idioma.equals("english")){
 			
 			idioma = "resources.lang_en_US";
 			
 		}
 		
 		if(idioma.equals("espaol")){
 			
 			
 			idioma = "resources.lang_es_ES";
 			
 		}
 		
 		
 		if(idioma.equals("catalan")){
 			
 			
 			idioma = "resources.lang_es_CA";
 			
 		}
 		
 		return idioma;
 		
 		
 		
 	}
 
 
 
 }
