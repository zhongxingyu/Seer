 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DBMS;
 
 import java.sql.Connection;
 import Clases.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.List;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author smaf
  */
 public class DBMS {
 
     static private Connection conexion;
 
     protected DBMS() {
     }
     static private DBMS instance = null;
 
     static public DBMS getInstance() {
         if (null == DBMS.instance) {
             DBMS.instance = new DBMS();
         }
         conectar();
         return DBMS.instance;
     }
 
     public static boolean conectar() {
         try {
             Class.forName("org.postgresql.Driver");
             conexion = DriverManager.getConnection(
                     "jdbc:postgresql://localhost/DSI_USB",
                     "postgres",
                     "postgres");
             return true;
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
         return false;
     }
 
     /*Para el Login se consulta el usuario con la base de datos
      y si existe, entra al sitio. */
     public Boolean consultarUsuario(Usuario u) {
         String sqlquery = "SELECT * FROM \"PREPAS\".usuario"
                 + " WHERE usuario ='" + u.getUsuario() + "' " + " AND "
                 + "password ='" + u.getPassword() + "'";
 
         System.out.println(sqlquery);
         try {
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sqlquery);
             Boolean b = rs.next();
             return b;
         } catch (SQLException ex) {
             Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
         }
         return false;
     }
 
     /* Consulta si al momento de registrarse el nombre de usuario ya se 
      * encuentra en uso. */
     public Boolean existeUsuario(Usuario u) {
 
         String sqlquery = "SELECT * FROM \"PREPAS\".usuario"
                 + " WHERE usuario ='" + u.getUsuario() + "'";
 
         System.out.println(sqlquery);
         try {
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sqlquery);
             Boolean b = rs.next();
             return b;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public Boolean agregarEquipo(Equipo e) {
         try {
             String sqlquery;
             sqlquery = "INSERT INTO \"PREPAS\".equipo (tipo, imagen, nombre_vista, funcionalidad)  VALUES "
                     + "('" + e.getTipo() + "' , '" + e.getImagen()
                     + "' , '" + e.getNombre_vista() + "' , '" + e.getFuncionalidad() + "');";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public Boolean agregarNoticia(Noticia n) {
         try {
             String sqlquery;
             sqlquery = "INSERT INTO \"PREPAS\".noticia VALUES "
                     + "('" + n.getUsuario() + "' , '" + n.getTitulo()
                     + "' , '" + n.getContenido() + "' , (SELECT CURRENT_DATE))";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public ArrayList<Noticia> obtenerNoticias() {
         ArrayList<Noticia> noticias = new ArrayList<Noticia>(0);
         try {
             String sqlquery;
             sqlquery = "SELECT * FROM \"PREPAS\".noticia";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 Noticia n = new Noticia();
                 n.setUsuario(rs.getString("usuario"));
                 n.setTitulo(rs.getString("titulo"));
                 n.setContenido(rs.getString("contenido"));
                 n.setFechaNoticia(rs.getString("fechaNoticia"));
                 noticias.add(n);
             }
             return noticias;
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return noticias;
     }
 
     public ArrayList<Equipo> obtenerEquipos() {
         ArrayList<Equipo> equipos = new ArrayList<Equipo>(0);
         try {
             String sqlquery;
             sqlquery = "SELECT * FROM \"PREPAS\".equipo";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 Equipo e = new Equipo();
                 e.setNombre_vista(rs.getString("nombre_vista"));
                 e.setImagen(rs.getString("imagen"));
                 e.setTipo(rs.getString("tipo"));
                 e.setCantidad(Integer.parseInt(rs.getString("cantidad")));
                 e.setEvaluacion(Double.parseDouble(rs.getString("evaluacion")));
                 e.setFuncionalidad(rs.getString("funcionalidad"));
                 e.setSerial(Integer.parseInt(rs.getString("serial")));
                 equipos.add(e);
             }
             return equipos;
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return equipos;
     }
 
     public void editarNoticia(Noticia n) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".noticia SET "
                     + "titulo = '" + n.getTitulo() + "' , "
                     + "contenido = '" + n.getContenido() + "'"
                     + " WHERE usuario = '" + n.getUsuario() + "'"
                     + " AND titulo = '" + n.getTituloAnterior() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void editarEquipo(Equipo e) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".equipo SET "
                     + "nombre_vista = '" + e.getNombre_vista() + "' , "
                     + "tipo = '" + e.getTipo() + "' , "
                     + "imagen = '" + e.getImagen() + "' , "
                     + "cantidad = '" + e.getCantidad() + "' , "
                     + "evaluacion = '" + e.getEvaluacion() + "' , "
                     + "funcionalidad = '" + e.getFuncionalidad() + "'"
                     + " WHERE serial = '" + e.getSerial() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void eliminarNoticia(Noticia n) {
         try {
             String sqlquery = "DELETE FROM \"PREPAS\".noticia WHERE "
                     + "usuario = '" + n.getUsuario() + "' "
                     + "AND titulo = '" + n.getTitulo() + "' ";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void eliminarEquipo(Equipo e) {
         try {
             String sqlquery = "DELETE FROM \"PREPAS\".equipo WHERE "
                     + "serial = '" + e.getSerial() + "' ;";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     /* Retorna todos los atributos del usuario a consultar. */
     public Usuario atributosUsuario(Usuario us) {
 
         Usuario u = new Usuario();
         try {
             String sqlquery = "SELECT * FROM \"PREPAS\".usuario "
                     + "WHERE usuario = '" + us.getUsuario() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             Boolean b = rs.next();
             u.setUsuario(rs.getString("usuario"));
             u.setPassword(rs.getString("password"));
             u.setTelefono(rs.getString("telefono"));
             u.setNombre(rs.getString("nombre"));
             u.setApellido(rs.getString("apellido"));
             u.setEmail(rs.getString("email"));
             u.setFecha(rs.getString("fecha"));
             u.setSexo(rs.getString("sexo"));
             u.setDireccion(rs.getString("direccion"));
             u.setTalla_mascara(rs.getString("talla_mascara"));
             u.setTalla_camisa(rs.getString("talla_camisa"));
             u.setTalla_pantalon(rs.getString("talla_pantalon"));
             u.setTalla_guantes(rs.getString("talla_guantes"));
             u.setTalla_zapato(rs.getString("talla_zapato"));
             u.setHabilitado(rs.getString("habilitado"));
             u.setAdministrador(rs.getString("administrador"));
             u.setArea_laboral(rs.getString("area_laboral"));
 
             return u;
 
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return u;
     }
 
     /*Agrega un usuario a la base de datos. */
     public Boolean agregarUsuario(Usuario u) {
         try {
             String sqlquery;
             sqlquery = "INSERT INTO \"PREPAS\".usuario VALUES "
                     + "('" + u.getUsuario() + "' , '" + u.getPassword()
                     + "' , '" + u.getEmail() + "' , '" + u.getNombre()
                     + "' , '" + u.getApellido() + "' , '" + u.getFecha()
                     + "' , '" + u.getTelefono() + "' , '" + u.getDireccion()
                     + "' , '" + u.getSexo() + "' , '" + u.getTalla_mascara()
                     + "' , '" + u.getTalla_camisa() + "' , '" + u.getTalla_pantalon()
                     + "' , '" + u.getTalla_guantes()
                     + "' , '" + u.getTalla_zapato()
                     + "' , '" + "false"
                     + "' , '" + "false"
                     + "' , '" + u.getArea_laboral()+"')";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     /* Elimina un usuario de la base de datos */
     public Boolean eliminarUsuario(Usuario u) {
         try {
             String sqlquery = "DELETE FROM \"PREPAS\".solicitud WHERE "
                     + "usuario = '" + u.getUsuario() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
 
 
             sqlquery = "DELETE FROM \"PREPAS\".usuario WHERE "
                     + "usuario = '" + u.getUsuario() + "'";
             stmt = conexion.createStatement();
             System.out.println(sqlquery);
             i = stmt.executeUpdate(sqlquery);
 
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     /*Consulta todos los usuarios habilitado existentes en la base de datos*/
     public ArrayList<Usuario> consultarUsuariosHabilitados(Usuario admin) {
         ArrayList<Usuario> usrs = new ArrayList<Usuario>(0);
         try {
             String sqlquery = "SELECT * FROM \"PREPAS\".usuario "
                     + "EXCEPT "
                     + "(SELECT * FROM \"PREPAS\".usuario "
                     + "WHERE usuario = '" + admin.getUsuario() + "')";
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
             while (rs.next()) {
                 if (rs.getBoolean("habilitado")) {
                     Usuario u = new Usuario();
                     u.setUsuario(rs.getString("usuario"));
                     u.setPassword(rs.getString("password"));
                     u.setHabilitado(rs.getString("habilitado"));
                     usrs.add(u);
                 }
             }
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return usrs;
     }
 
     /*Consulta todos los usuarios habilitado existentes en la base de datos*/
     public ArrayList<Usuario> consultarUsuariosNoHabilitados(Usuario admin) {
         ArrayList<Usuario> usrs = new ArrayList<Usuario>(0);
         try {
             String sqlquery = "SELECT * FROM \"PREPAS\".usuario "
                     + "EXCEPT "
                     + "(SELECT * FROM \"PREPAS\".usuario "
                     + "WHERE usuario = '" + admin.getUsuario() + "')";
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
             while (rs.next()) {
                 if (!rs.getBoolean("habilitado")) {
                     Usuario u = new Usuario();
                     u.setUsuario(rs.getString("usuario"));
                     u.setPassword(rs.getString("password"));
                     u.setHabilitado(rs.getString("habilitado"));
                     usrs.add(u);
                 }
             }
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return usrs;
     }
 
     /*Habilita un usuario. */
     public Boolean habilitar(Usuario u) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".usuario SET "
                     + "habilitado = 'true' "
                     + " WHERE usuario = '" + u.getUsuario() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     /*Deshabilita un usuario. */
     public Boolean deshabilitar(Usuario u) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".usuario SET "
                     + "habilitado = 'false' "
                     + " WHERE usuario = '" + u.getUsuario() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     /*Modifica un usuario existente en la base de datos. */
     public Boolean modificarUsuario(Usuario u) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".usuario SET "
                     + "password = '" + u.getPassword() + "' , "
                     + "telefono = '" + u.getTelefono() + "' , "
                     + "email = '" + u.getEmail() + "' , "
                     + "direccion = '" + u.getDireccion() + "' , "
                     + "talla_mascara = '" + u.getTalla_mascara() + "' , "
                     + "talla_camisa = '" + u.getTalla_camisa() + "' , "
                     + "talla_pantalon = '" + u.getTalla_pantalon() + "' , "
                     + "talla_guantes = '" + u.getTalla_guantes() + "' , "
                    + "talla_zapato = '" + u.getTalla_zapato() + "' , "
                    + "area_laboral = '" + u.getArea_laboral() + "'"
                     + "WHERE usuario = '" + u.getUsuario() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     /*Agrega a la base de datos la solicitud de un pedido por parte de 
      un usuario. */
     public Boolean agregarSolicitud(Pedido p) {
 
         String sqlquery = null;
         Statement stmt;
         Integer i = 0;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LOS CASCOS*/
         try {
             if (p.getCascoSeguridad() != null && p.getCascoSeguridad()) {
                 if (!existeSolicitud(p.getUsuario(), "cascoSeguridad")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'cascoSeguridad'"
                             + " , (SELECT CURRENT_DATE), 'Casco de seguridad'"
                             + " , '" + p.getC1() + "' , '" + p.getF1() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC1() + "' , frecuencia = '"
                             + p.getF1() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'cascoSeguridad'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getCascoMotorizado() != null && p.getCascoMotorizado()) {
                 if (!existeSolicitud(p.getUsuario(), "cascoMotorizado")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'cascoMotorizado'"
                             + " , (SELECT CURRENT_DATE), 'Casco de motorizado'"
                             + " ,'" + p.getC2() + "' , '" + p.getF2() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC2() + "' , frecuencia = '"
                             + p.getF2() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'cascoMotorizado'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LOS LENTES.*/
         sqlquery = null;
         try {
             if (p.getLentesCopa() != null && p.getLentesCopa()) {
                 if (!existeSolicitud(p.getUsuario(), "lentesCopa")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'lentesCopa'"
                             + " , (SELECT CURRENT_DATE), 'Lentes de copa'"
                             + " , '" + p.getC4() + "' , '" + p.getF4() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC4() + "' , frecuencia = '"
                             + p.getF4() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'lentesCopa'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getLentesSeguridad() != null && p.getLentesSeguridad()) {
                 if (!existeSolicitud(p.getUsuario(), "lentesSeguridad")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'lentesSeguridad'"
                             + " , (SELECT CURRENT_DATE), 'Lentes de seguridad' "
                             + " , '" + p.getC3() + "' , '" + p.getF3() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC3() + "' , frecuencia = '"
                             + p.getF3() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'lentesSeguridad'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LOS RESPIRADORES. */
         try {
             if (p.getRespiradorCaraCompleta() != null && p.getRespiradorCaraCompleta()) {
                 if (!existeSolicitud(p.getUsuario(), "respiradorCaraCompleta")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'respiradorCaraCompleta'"
                             + " , (SELECT CURRENT_DATE), 'Respirador de cara completa'"
                             + " , '" + p.getC5() + "' , '" + p.getF5() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC5() + "' , frecuencia = '"
                             + p.getF5() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'respiradorCaraCompleta'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getRespiradorMedioRostro() != null && p.getRespiradorMedioRostro()) {
                 if (!existeSolicitud(p.getUsuario(), "respiradorMedioRostro")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'respiradorMedioRostro'"
                             + " , (SELECT CURRENT_DATE), 'respirador de medio rostro' "
                             + " , '" + p.getC6() + "' , '" + p.getF6() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC6() + "' , frecuencia = '"
                             + p.getF6() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'respiradorMedioRostro'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LAS MASCARILLAS*/
         try {
             if (p.getMascarilla() != null && p.getMascarilla()) {
                 if (!existeSolicitud(p.getUsuario(), "mascarilla")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'mascarilla'"
                             + " , (SELECT CURRENT_DATE), 'Mascarilla'"
                             + " , '" + p.getC7() + "' , '" + p.getF7() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC7() + "' , frecuencia = '"
                             + p.getF7() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'mascarilla'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE AL GORRO. */
         try {
             if (p.getGorro() != null && p.getGorro()) {
                 if (!existeSolicitud(p.getUsuario(), "gorro")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'gorro'"
                             + " , (SELECT CURRENT_DATE), 'Gorro'"
                             + " , '" + p.getC8() + "' , '" + p.getF8() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC8() + "' , frecuencia = '"
                             + p.getF8() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'gorro'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LA CARETA PARA SOLDAR. */
         try {
             if (p.getCaretaSoldar() != null && p.getCaretaSoldar()) {
                 if (!existeSolicitud(p.getUsuario(), "caretaSoldar")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'caretaSoldar'"
                             + " , (SELECT CURRENT_DATE), 'Careta para soldar'"
                             + " , '" + p.getC9() + "' , '" + p.getF9() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC9() + "' , frecuencia = '"
                             + p.getF9() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'caretaSoldar'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LAS GORRAS Y SOMBREROS. */
         try {
             if (p.getGorraAzul() != null && p.getGorraAzul()) {
                 if (!existeSolicitud(p.getUsuario(), "gorraAzul")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'gorraAzul'"
                             + " , (SELECT CURRENT_DATE), 'Gorra azul'"
                             + " , '" + p.getC12() + "' , '" + p.getF12() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC12() + "' , frecuencia = '"
                             + p.getF12() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'gorraAzul'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGorraGrisLogo() != null && p.getGorraGrisLogo()) {
                 if (!existeSolicitud(p.getUsuario(), "gorraGrisLogo")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'gorraGrisLogo'"
                             + " , (SELECT CURRENT_DATE), 'Gorra gris logo USB'"
                             + " , '" + p.getC11() + "' , '" + p.getF11() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC11() + "' , frecuencia = '"
                             + p.getF11() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'gorraGrisLogo'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getSombreroTipoPava() != null && p.getSombreroTipoPava()) {
                 if (!existeSolicitud(p.getUsuario(), "sombreroTipoPava")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'sombreroTipoPava'"
                             + " , (SELECT CURRENT_DATE), 'Sombrero tipo pava'"
                             + " , '" + p.getC10() + "' , '" + p.getF10() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC10() + "' , frecuencia = '"
                             + p.getF10() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'sombreroTipoPava'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO QUE SE REFIERE A LAS BOTAS */
         try {
             if (p.getBotasTipoMilitar() != null && p.getBotasTipoMilitar()) {
                 if (!existeSolicitud(p.getUsuario(), "botasTipoMilitar")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasTipoMilitar'"
                             + " , (SELECT CURRENT_DATE), 'Botas tipo militar'"
                             + " , '" + p.getC16() + "' , '" + p.getF16() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC16() + "' , frecuencia = '"
                             + p.getF16() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasTipoMilitar'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getBotasVaquetaCanaLarga() != null && p.getBotasVaquetaCanaLarga()) {
                 if (!existeSolicitud(p.getUsuario(), "botasVaquetaCanaLarga")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasVaquetaCanaLarga'"
                             + " , (SELECT CURRENT_DATE), 'Botas vaqueta caña larga'"
                             + " , '" + p.getC17() + "' , '" + p.getF17() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC17() + "' , frecuencia = '"
                             + p.getF17() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasVaquetaCanaLarga'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getBotasMontanera() != null && p.getBotasMontanera()) {
                 if (!existeSolicitud(p.getUsuario(), "botasMontanera")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasMontanera'"
                             + " , (SELECT CURRENT_DATE), 'Botas montañera'"
                             + " , '" + p.getC18() + "' , '" + p.getF18() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC18() + "' , frecuencia = '"
                             + p.getF18() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasMontanera'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getBotasDielectricas() != null && p.getBotasDielectricas()) {
                 if (!existeSolicitud(p.getUsuario(), "botasDielectricas")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasDielectricas'"
                             + " , (SELECT CURRENT_DATE), 'Botas dielectricas'"
                             + " , '" + p.getC13() + "' , '" + p.getF13() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC13() + "' , frecuencia = '"
                             + p.getF13() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasDielectricas'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getBotasPHierro() != null && p.getBotasPHierro()) {
                 if (!existeSolicitud(p.getUsuario(), "botasPHierro")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasPHierro'"
                             + " , (SELECT CURRENT_DATE), 'Botas punta de hierro'"
                             + " , '" + p.getC14() + "' , '" + p.getF14() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC14() + "' , frecuencia = '"
                             + p.getF14() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasPHierro'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getBotasCueroVulcanizTactica() != null && p.getBotasCueroVulcanizTactica()) {
                 if (!existeSolicitud(p.getUsuario(), "botasCueroVulcanizTactica")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasCueroVulcanizTactica'"
                             + " , (SELECT CURRENT_DATE), 'Botas de cuero vulcanizadas'"
                             + " , '" + p.getC19() + "' , '" + p.getF19() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC19() + "' , frecuencia = '"
                             + p.getF19() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasCueroVulcanizTactica'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getBotasCanaLarga() != null && p.getBotasCanaLarga()) {
                 if (!existeSolicitud(p.getUsuario(), "botasCanaLarga")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasCanaLarga'"
                             + " , (SELECT CURRENT_DATE), 'Botas caña larga'"
                             + " , '" + p.getC15() + "' , '" + p.getF15() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC15() + "' , frecuencia = '"
                             + p.getF15() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasCanaLarga'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE LOS GUANTES */
         try {
             if (p.getGuantesCarnaza() != null && p.getGuantesCarnaza()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesCarnaza")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesCarnaza'"
                             + " , (SELECT CURRENT_DATE), 'Guantes de carnaza'"
                             + " , '" + p.getC20() + "' , '" + p.getF20() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC20() + "' , frecuencia = '"
                             + p.getF20() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesCarnaza'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesCarnazaSoldador() != null && p.getGuantesCarnazaSoldador()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesCarnazaSoldador")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesCarnazaSoldador'"
                             + " , (SELECT CURRENT_DATE), 'Guantes de carnaza soldador'"
                             + " , '" + p.getC21() + "' , '" + p.getF21() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC21() + "' , frecuencia = '"
                             + p.getF21() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesCarnazaSoldador'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesAntiAcido18() != null && p.getGuantesAntiAcido18()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesAntiAcido18")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesAntiAcido18'"
                             + " , (SELECT CURRENT_DATE), 'Guantes antiacido 18'"
                             + " , '" + p.getC22() + "' , '" + p.getF22() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC22() + "' , frecuencia = '"
                             + p.getF22() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesAntiAcido18'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesLatex() != null && p.getGuantesLatex()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesLatex")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesLatex'"
                             + " , (SELECT CURRENT_DATE), 'Guantes de latex'"
                             + " , '" + p.getC23() + "' , '" + p.getF23() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC23() + "' , frecuencia = '"
                             + p.getF23() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesLatex'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesKevlar14() != null && p.getGuantesKevlar14()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesKevlar14")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesKevlar14'"
                             + " , (SELECT CURRENT_DATE), 'Guantes Kevlar 14'"
                             + " , '" + p.getC24() + "' , '" + p.getF24() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC24() + "' , frecuencia = '"
                             + p.getF24() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesKevlar14'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesKevlar20() != null && p.getGuantesKevlar20()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesKevlar20")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesKevlar20'"
                             + " , (SELECT CURRENT_DATE), 'Guantes Kevlar 20'"
                             + " , '" + p.getC25() + "' , '" + p.getF25() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC25() + "' , frecuencia = '"
                             + p.getF25() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesKevlar20'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesVaqueta() != null && p.getGuantesVaqueta()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesVaqueta")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesVaqueta'"
                             + " , (SELECT CURRENT_DATE), 'Guantes vaqueta'"
                             + " , '" + p.getC26() + "' , '" + p.getF26() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC26() + "' , frecuencia = '"
                             + p.getF26() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesVaqueta'";
                 }
 
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getGuantesNitrilo() != null && p.getGuantesNitrilo()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesNitrilo")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesNitrilo'"
                             + " , (SELECT CURRENT_DATE), 'Guantes nitrilo'"
                             + " , '" + p.getC27() + "' , '" + p.getF27() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC27() + "' , frecuencia = '"
                             + p.getF27() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesNitrilo'";
                 }
 
             }
             if (sqlquery
                     != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         try {
             if (p.getGuantesAntideslizantes() != null && p.getGuantesAntideslizantes()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesAntideslizantes")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesAntideslizantes'"
                             + " , (SELECT CURRENT_DATE), 'Guantes antideslizantes'"
                             + " , '" + p.getC29() + "' , '" + p.getF29() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC29() + "' , frecuencia = '"
                             + p.getF29() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesAntideslizantes'";
                 }
 
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getGuantesNeopreno() != null && p.getGuantesNeopreno()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesNeopreno")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesNeopreno'"
                             + " , (SELECT CURRENT_DATE), 'Guantes de neopreno'"
                             + " , '" + p.getC30() + "' , '" + p.getF30() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC30() + "' , frecuencia = '"
                             + p.getF30() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesNeopreno'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getGuantesMotorizado() != null && p.getGuantesMotorizado()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesMotorizado")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesMotorizado'"
                             + " , (SELECT CURRENT_DATE), 'Guantes para motorizados'"
                             + " , '" + p.getC28() + "' , '" + p.getF28() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC28() + "' , frecuencia = '"
                             + p.getF28() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesMotorizado'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS FILTROS */
 
         try {
             if (p.getFiltroMercurio() != null && p.getFiltroMercurio()) {
                 if (!existeSolicitud(p.getUsuario(), "filtroMercurio")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'filtroMercurio'"
                             + " , (SELECT CURRENT_DATE), 'Filtro para mercurio'"
                             + " , '" + p.getC33() + "' , '" + p.getF33() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC33() + "' , frecuencia = '"
                             + p.getF33() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'filtroMercurio'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getFiltroCloro() != null && p.getFiltroCloro()) {
                 if (!existeSolicitud(p.getUsuario(), "filtroCloro")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'filtroCloro'"
                             + " , (SELECT CURRENT_DATE), 'Filtro para cloro'"
                             + " , '" + p.getC32() + "' , '" + p.getF32() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC32() + "' , frecuencia = '"
                             + p.getF32() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'filtroCloro'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getFiltroVaporOrganico() != null && p.getFiltroVaporOrganico()) {
                 if (!existeSolicitud(p.getUsuario(), "filtroVaporOrganico")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'filtroVaporOrganico'"
                             + " , (SELECT CURRENT_DATE), 'Filtro para vapor organico'"
                             + " , '" + p.getC31() + "' , '" + p.getF31() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC31() + "' , frecuencia = '"
                             + p.getF31() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'filtroVaporOrganico'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getArnesSeguridad() != null && p.getArnesSeguridad()) {
                 if (!existeSolicitud(p.getUsuario(), "arnesSeguridad")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'arnesSeguridad'"
                             + " , (SELECT CURRENT_DATE), 'Arnés de seguridad'"
                             + " , '" + p.getC34() + "' , '" + p.getF34() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC34() + "' , frecuencia = '"
                             + p.getF34() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'arnesSeguridad'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS DELANTALES */
 
         try {
             if (p.getDelantalCocinero() != null && p.getDelantalCocinero()) {
                 if (!existeSolicitud(p.getUsuario(), "delantalCocinero")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'delantalCocinero'"
                             + " , (SELECT CURRENT_DATE), 'Delantal de cocinero'"
                             + " , '" + p.getC35() + "' , '" + p.getF35() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC35() + "' , frecuencia = '"
                             + p.getF35() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'delantalCocinero'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getDelantalSoldadura() != null && p.getDelantalSoldadura()) {
                 if (!existeSolicitud(p.getUsuario(), "delantalSoldadura")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'delantalSoldadura'"
                             + " , (SELECT CURRENT_DATE), 'Delantal para soldadura'"
                             + " , '" + p.getC36() + "' , '" + p.getF36() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC36() + "' , frecuencia = '"
                             + p.getF36() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'delantalSoldadura'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getDelantalNeopreno() != null && p.getDelantalNeopreno()) {
                 if (!existeSolicitud(p.getUsuario(), "delantalNeopreno")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'delantalNeopreno'"
                             + " , (SELECT CURRENT_DATE), 'Delantal de neopreno'"
                             + " , '" + p.getC37() + "' , '" + p.getF37() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC37() + "' , frecuencia = '"
                             + p.getF37() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'delantalNeopreno'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getDelantalPVC() != null && p.getDelantalPVC()) {
                 if (!existeSolicitud(p.getUsuario(), "delantalPVC")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'delantalPVC'"
                             + " , (SELECT CURRENT_DATE), 'Delantal PVC'"
                             + " , '" + p.getC38() + "' , '" + p.getF38() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC38() + "' , frecuencia = '"
                             + p.getF38() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'delantalPVC'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS DELANTALES */
 
         try {
             if (p.getCalzadoAntideslizante() != null && p.getCalzadoAntideslizante()) {
                 if (!existeSolicitud(p.getUsuario(), "calzadoAntideslizante")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'calzadoAntideslizante'"
                             + " , (SELECT CURRENT_DATE), 'Calzado antideslizante'"
                             + " , '" + p.getC39() + "' , '" + p.getF39() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC39() + "' , frecuencia = '"
                             + p.getF39() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'calzadoAntideslizante'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LA CHAQUETA DE BOMBERO */
 
         try {
             if (p.getChaquetaBombero() != null && p.getChaquetaBombero()) {
                 if (!existeSolicitud(p.getUsuario(), "chaquetaBombero")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chaquetaBombero'"
                             + " , (SELECT CURRENT_DATE), 'Chaqueta bombero'"
                             + " , '" + p.getC42() + "' , '" + p.getF42() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC42() + "' , frecuencia = '"
                             + p.getF42() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chaquetaBombero'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS PANTALONES DE BOMBERO*/
 
         try {
             if (p.getPantalonTipoBombero() != null && p.getPantalonTipoBombero()) {
                 if (!existeSolicitud(p.getUsuario(), "pantalonTipoBombero")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'pantalonTipoBombero'"
                             + " , (SELECT CURRENT_DATE), 'Pantalón tipo bombero'"
                             + " , '" + p.getC41() + "' , '" + p.getF41() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC41() + "' , frecuencia = '"
                             + p.getF41() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'pantalonTipoBombero'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getPantalonBomberoRescateAzul() != null && p.getPantalonBomberoRescateAzul()) {
                 if (!existeSolicitud(p.getUsuario(), "pantalonBomberoRescateAzul")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'pantalonBomberoRescateAzul'"
                             + " , (SELECT CURRENT_DATE), 'Pantalón bombero rescate azul'"
                             + " , '" + p.getC40() + "' , '" + p.getF40() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC40() + "' , frecuencia = '"
                             + p.getF40() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'pantalonBomberoRescateAzul'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE AL UNTRY{ IFORME. */
 
         try {
             if (p.getUniformeTipoMilitar() != null && p.getUniformeTipoMilitar()) {
                 if (!existeSolicitud(p.getUsuario(), "uniformeTipoMilitar")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'uniformeTipoMilitar'"
                             + " , (SELECT CURRENT_DATE), 'Uniforme tipo militar'"
                             + " , '" + p.getC43() + "' , '" + p.getF43() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC43() + "' , frecuencia = '"
                             + p.getF43() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'uniformeTipoMilitar'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LAS BOTAS DE BOMBERO*/
 
         try {
             if (p.getBotasCombateIncendio() != null && p.getBotasCombateIncendio()) {
                 if (!existeSolicitud(p.getUsuario(), "botasCombateIncendio")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'botasCombateIncendio'"
                             + " , (SELECT CURRENT_DATE), 'Botas combate contra incendio'"
                             + " , '" + p.getC44() + "' , '" + p.getF44() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC44() + "' , frecuencia = '"
                             + p.getF44() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'botasCombateIncendio'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS GUANTES DE BOMBERO*/
 
         try {
             if (p.getGuantesCombateIncendio() != null && p.getGuantesCombateIncendio()) {
                 if (!existeSolicitud(p.getUsuario(), "guantesCombateIncendio")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'guantesCombateIncendio'"
                             + " , (SELECT CURRENT_DATE), 'Guantes combate contra incendio'"
                             + " , '" + p.getC45() + "' , '" + p.getF45() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC45() + "' , frecuencia = '"
                             + p.getF45() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'guantesCombateIncendio'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS IMPERMEABLES*/
 
         try {
             if (p.getImpermeableMotorizado() != null && p.getImpermeableMotorizado()) {
                 if (!existeSolicitud(p.getUsuario(), "impermeableMotorizado")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'impermeableMotorizado'"
                             + " , (SELECT CURRENT_DATE), 'Impermeable motorizado'"
                             + " , '" + p.getC46() + "' , '" + p.getF46() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC46() + "' , frecuencia = '"
                             + p.getF46() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'impermeableMotorizado'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LAS BRAGAS*/
 
         try {
             if (p.getBragaGrisDobleCostura() != null && p.getBragaGrisDobleCostura()) {
                 if (!existeSolicitud(p.getUsuario(), "bragaGrisDobleCostura")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'bragaGrisDobleCostura'"
                             + " , (SELECT CURRENT_DATE), 'Braga gris doble costura'"
                             + " , '" + p.getC47() + "' , '" + p.getF47() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC47() + "' , frecuencia = '"
                             + p.getF47() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'bragaGrisDobleCostura'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getBragaNegraDobleCostura() != null && p.getBragaNegraDobleCostura()) {
                 if (!existeSolicitud(p.getUsuario(), "bragaNegraDobleCostura")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'bragaNegraDobleCostura'"
                             + " , (SELECT CURRENT_DATE), 'Braga negra doble costura'"
                             + " , '" + p.getC48() + "' , '" + p.getF48() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC48() + "' , frecuencia = '"
                             + p.getF48() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'bragaNegraDobleCostura'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LAS BATAS*/
 
         try {
             if (p.getBataAzul() != null && p.getBataAzul()) {
                 if (!existeSolicitud(p.getUsuario(), "bataAzul")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'bataAzul'"
                             + " , (SELECT CURRENT_DATE), 'Bata azul'"
                             + " , '" + p.getC49() + "' , '" + p.getF49() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC49() + "' , frecuencia = '"
                             + p.getF49() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'bataAzul'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getBataGris() != null && p.getBataGris()) {
                 if (!existeSolicitud(p.getUsuario(), "bataGris")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'bataGris'"
                             + " , (SELECT CURRENT_DATE), 'Bata gris'"
                             + " , '" + p.getC50() + "' , '" + p.getF50() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC50() + "' , frecuencia = '"
                             + p.getF50() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'bataGris'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getBataNegra() != null && p.getBataNegra()) {
                 if (!existeSolicitud(p.getUsuario(), "bataNegra")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'bataNegra'"
                             + " , (SELECT CURRENT_DATE), 'Bata negra'"
                             + " , '" + p.getC51() + "' , '" + p.getF51() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC51() + "' , frecuencia = '"
                             + p.getF51() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'bataNegra'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getBataBlanca() != null && p.getBataBlanca()) {
                 if (!existeSolicitud(p.getUsuario(), "bataBlanca")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'bataBlanca'"
                             + " , (SELECT CURRENT_DATE), 'Bata blanca'"
                             + " , '" + p.getC52() + "' , '" + p.getF52() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC52() + "' , frecuencia = '"
                             + p.getF52() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'bataBlanca'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LAS CHEMISSES*/
 
         try {
             if (p.getChemisseAzul() != null && p.getChemisseAzul()) {
                 if (!existeSolicitud(p.getUsuario(), "chemisseAzul")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chemisseAzul'"
                             + " , (SELECT CURRENT_DATE), 'Cemisse azul'"
                             + " , '" + p.getC53() + "' , '" + p.getF53() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC53() + "' , frecuencia = '"
                             + p.getF53() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chemisseAzul'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getChemisseBeige() != null && p.getChemisseBeige()) {
                 if (!existeSolicitud(p.getUsuario(), "chemisseBeige")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chemisseBeige'"
                             + " , (SELECT CURRENT_DATE), 'Chemisse beige'"
                             + " , '" + p.getC54() + "' , '" + p.getF54() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC54() + "' , frecuencia = '"
                             + p.getF54() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chemisseBeige'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getChemisseVerde() != null && p.getChemisseVerde()) {
                 if (!existeSolicitud(p.getUsuario(), "chemisseVerde")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chemisseVerde'"
                             + " , (SELECT CURRENT_DATE), 'Chemisse verde'"
                             + " , '" + p.getC55() + "' , '" + p.getF55() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC55() + "' , frecuencia = '"
                             + p.getF55() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chemisseVerde'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getChemisseBlanca() != null && p.getChemisseBlanca()) {
                 if (!existeSolicitud(p.getUsuario(), "chemisseBlanca")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chemisseBlanca'"
                             + " , (SELECT CURRENT_DATE), 'Chemisse blanca'"
                             + " , '" + p.getC56() + "' , '" + p.getF56() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC56() + "' , frecuencia = '"
                             + p.getF56() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chemisseBlanca'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getChemisseGris() != null && p.getChemisseGris()) {
                 if (!existeSolicitud(p.getUsuario(), "chemisseGris")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chemisseGris'"
                             + " , (SELECT CURRENT_DATE), 'Chemisse gris'"
                             + " , '" + p.getC57() + "' , '" + p.getF57() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC57() + "' , frecuencia = '"
                             + p.getF57() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chemisseGris'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LAS CAMISAS*/
 
         try {
             if (p.getCamisaBlanca() != null && p.getCamisaBlanca()) {
                 if (!existeSolicitud(p.getUsuario(), "camisaBlanca")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'camisaBlanca'"
                             + " , (SELECT CURRENT_DATE), 'Camisa blanca'"
                             + " , '" + p.getC60() + "' , '" + p.getF60() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC60() + "' , frecuencia = '"
                             + p.getF60() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'camisaBlanca'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getCamisaAzul() != null && p.getCamisaAzul()) {
                 if (!existeSolicitud(p.getUsuario(), "camisaAzul")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'camisaAzul'"
                             + " , (SELECT CURRENT_DATE), 'Camisa azul'"
                             + " , '" + p.getC61() + "' , '" + p.getF61() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC61() + "' , frecuencia = '"
                             + p.getF61() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'camisaAzul'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getCamisaBeige() != null && p.getCamisaBeige()) {
                 if (!existeSolicitud(p.getUsuario(), "camisaBeige")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'camisaBeige'"
                             + " , (SELECT CURRENT_DATE), 'Camisa beige'"
                             + " , '" + p.getC62() + "' , '" + p.getF62() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC62() + "' , frecuencia = '"
                             + p.getF62() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'camisaBeige'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS BLAZER*/
 
         try {
             if (p.getBlazerAzul() != null && p.getBlazerAzul()) {
                 if (!existeSolicitud(p.getUsuario(), "blazerAzul")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'blazerAzul'"
                             + " , (SELECT CURRENT_DATE), 'Blazer azul'"
                             + " , '" + p.getC58() + "' , '" + p.getF58() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC58() + "' , frecuencia = '"
                             + p.getF58() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'blazerAzul'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A LOS PANTALONES*/
 
         try {
             if (p.getBlueJeans() != null && p.getBlueJeans()) {
                 if (!existeSolicitud(p.getUsuario(), "blueJeans")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'blueJeans'"
                             + " , (SELECT CURRENT_DATE), 'Blue jeans'"
                             + " , '" + p.getC59() + "' , '" + p.getF59() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC59() + "' , frecuencia = '"
                             + p.getF59() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'blueJeans'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
         /*PARTE DEL PEDIDO REFERENTE A OTROS*/
 
         try {
             if (p.getParaguas() != null && p.getParaguas()) {
                 if (!existeSolicitud(p.getUsuario(), "paraguas")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'paraguas'"
                             + " , (SELECT CURRENT_DATE), 'Paraguas'"
                             + " , '" + p.getC63() + "' , '" + p.getF63() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC63() + "' , frecuencia = '"
                             + p.getF63() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'paraguas'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getPantallaAtalaje() != null && p.getPantallaAtalaje()) {
                 if (!existeSolicitud(p.getUsuario(), "pantallaAtalaje")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'pantallaAtalaje'"
                             + " , (SELECT CURRENT_DATE), 'Pantalla de atalaje'"
                             + " , '" + p.getC64() + "' , '" + p.getF64() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC64() + "' , frecuencia = '"
                             + p.getF64() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'pantallaAtalaje'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getMaletin() != null && p.getMaletin()) {
                 if (!existeSolicitud(p.getUsuario(), "maletin")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'maletin'"
                             + " , (SELECT CURRENT_DATE), 'Maletín'"
                             + " , '" + p.getC65() + "' , '" + p.getF65() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC65() + "' , frecuencia = '"
                             + p.getF65() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'maletin'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getCamisaVestir() != null && p.getCamisaVestir()) {
                 if (!existeSolicitud(p.getUsuario(), "camisaVestir")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'camisaVestir'"
                             + " , (SELECT CURRENT_DATE), 'Camisa de vestir'"
                             + " , '" + p.getC66() + "' , '" + p.getF66() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC66() + "' , frecuencia = '"
                             + p.getF66() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'camisaVestir'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getPantalonVestir() != null && p.getPantalonVestir()) {
                 if (!existeSolicitud(p.getUsuario(), "pantalonVestir")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'pantalonVestir'"
                             + " , (SELECT CURRENT_DATE), 'Pantalón de vestir'"
                             + " , '" + p.getC67() + "' , '" + p.getF67() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC67() + "' , frecuencia = '"
                             + p.getF67() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'pantalonVestir'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getZapatosVestir() != null && p.getZapatosVestir()) {
                 if (!existeSolicitud(p.getUsuario(), "zapatoVestir")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'zapatoVestir'"
                             + " , (SELECT CURRENT_DATE), 'Zapatos de vestir'"
                             + " , '" + p.getC68() + "' , '" + p.getF68() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC68() + "' , frecuencia = '"
                             + p.getF68() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'zapatoVestir'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
 
 
         try {
             if (p.getChaquetaVestir() != null && p.getChaquetaVestir()) {
                 if (!existeSolicitud(p.getUsuario(), "chaquetaVestir")) {
                     sqlquery = "INSERT INTO \"PREPAS\".solicitud VALUES "
                             + "('" + p.getUsuario() + "' , 'chaquetaVestir'"
                             + " , (SELECT CURRENT_DATE), 'Chaqueta de vestir'"
                             + " , '" + p.getC69() + "' , '" + p.getF69() + "' )";
                 } else {
                     sqlquery = "UPDATE \"PREPAS\".solicitud SET "
                             + "cantidad = '" + p.getC69() + "' , frecuencia = '"
                             + p.getF69() + "'"
                             + " WHERE usuario = '" + p.getUsuario() + "'"
                             + " AND equipo = 'chaquetaVestir'";
                 }
             }
             if (sqlquery != null) {
                 stmt = conexion.createStatement();
                 System.out.println(sqlquery);
                 i = stmt.executeUpdate(sqlquery);
             }
         } catch (Exception e) {
         }
         sqlquery = null;
         return i
                 > 0;
     }
 
     public ArrayList<Solicitud> obtenerSolicitudes(Usuario u) {
         ArrayList<Solicitud> solicitudes = new ArrayList<Solicitud>(0);
         try {
             String sqlquery;
             sqlquery = "SELECT * FROM \"PREPAS\".solicitud, \"PREPAS\".equipo"
                     + " WHERE usuario = '" + u.getUsuario()
                     + "' AND  equipo.equipo = solicitud.equipo";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 Solicitud n = new Solicitud();
                 n.setNombre_usuario(rs.getString("usuario"));
                 n.setNombre_equipo(rs.getString("equipo"));
                 n.setNombre_vista(rs.getString("nombre_vista"));
                 n.setCantidad(rs.getString("cantidad"));
                 n.setFrecuencia(rs.getString("frecuencia"));
                 n.setImagen(rs.getString("imagen"));
                 solicitudes.add(n);
             }
             return solicitudes;
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return solicitudes;
     }
 
     /*Elimina ciertos elementos de la solicitud. */
     public Boolean eliminarSolicitud(Solicitud s) {
         try {
             String sqlquery;
             sqlquery = "DELETE FROM \"PREPAS\".solicitud"
                     + " WHERE usuario = '" + s.getNombre_usuario()
                     + "' AND equipo ='" + s.getNombre_equipo() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
 
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return false;
     }
 
     public Boolean eliminarSolicitudCompleta(String usuario) {
         try {
             String sqlquery;
             sqlquery = "DELETE FROM \"PREPAS\".solicitud"
                     + " WHERE usuario = '" + usuario
                     + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
 
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return false;
     }
 
     public ArrayList<Solicitud> obtenerPedido(String usuario) {
         ArrayList<Solicitud> pedido = new ArrayList<Solicitud>(0);
         try {
             String sqlquery;
             sqlquery = "SELECT * FROM \"PREPAS\".solicitud"
                     + " WHERE usuario = '" + usuario
                     + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 Solicitud eps = new Solicitud();
                 eps.setNombre_vista(rs.getString("nombre_vista"));
                 eps.setCantidad(rs.getString("cantidad"));
                 pedido.add(eps);
             }
             return pedido;
 
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return null;
     }
 
     public ArrayList<String> obtenerPedidoCompleto(String usuario) {
         ArrayList<String> pedido = new ArrayList<String>(0);
         try {
             String sqlquery;
             sqlquery = "SELECT nombre_vista, talla, cantidad FROM ("
                     + "SELECT nombre_vista, talla_pantalon AS talla, SUM(cantidad) AS cantidad, tipo "
                     + "    FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario NATURAL JOIN \"PREPAS\".equipo "
                     + "    WHERE tipo = 'pantalon' "
                     + "    GROUP BY nombre_vista, talla_pantalon, tipo "
                     + "UNION "
                     + "SELECT nombre_vista, talla_camisa AS talla, SUM(cantidad) AS cantidad, tipo "
                     + "   FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario NATURAL JOIN \"PREPAS\".equipo "
                     + "   WHERE tipo = 'camisa'"
                     + "   GROUP BY nombre_vista, talla_camisa, tipo "
                     + "UNION "
                     + "SELECT nombre_vista, '' AS talla, SUM(cantidad) AS cantidad, tipo "
                     + "    FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario NATURAL JOIN \"PREPAS\".equipo "
                     + "    WHERE tipo = 'otros' "
                     + "    GROUP BY nombre_vista, tipo "
                     + "UNION "
                     + "SELECT nombre_vista, talla_mascara AS talla, SUM(cantidad) AS cantidad, tipo "
                     + "    FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario NATURAL JOIN \"PREPAS\".equipo "
                     + "    WHERE tipo = 'cabeza' "
                     + "    GROUP BY nombre_vista, talla_mascara, tipo "
                     + "UNION "
                     + "SELECT nombre_vista, talla_guantes AS talla, SUM(cantidad) AS cantidad, tipo "
                     + "    FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario NATURAL JOIN \"PREPAS\".equipo "
                     + "    WHERE tipo = 'guantes' "
                     + "    GROUP BY nombre_vista, talla_guantes, tipo "
                     + "UNION "
                     + "SELECT nombre_vista, talla_zapato AS talla, SUM(cantidad) AS cantidad, tipo "
                     + "    FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario NATURAL  JOIN \"PREPAS\".equipo "
                     + "    WHERE tipo = 'zapato' "
                     + "    GROUP BY nombre_vista, talla_zapato, tipo ) d "
                     + " GROUP BY tipo, talla, cantidad, nombre_vista ORDER BY tipo, talla";
 
             System.out.println(sqlquery);
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 String eps;
                 eps = rs.getString("nombre_vista");
                 pedido.add(eps);
                 eps = rs.getString("talla");
                 pedido.add(eps);
                 eps = rs.getString("cantidad");
                 pedido.add(eps);
             }
 
 
             return pedido;
 
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return null;
     }
 
     public Boolean existeSolicitud(String usuario, String equipo) {
 
         String sqlquery = "SELECT * FROM \"PREPAS\".solicitud"
                 + " WHERE usuario ='" + usuario + "' "
                 + "AND equipo = '" + equipo + "'";
 
         System.out.println(sqlquery);
         try {
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sqlquery);
             Boolean b = rs.next();
             return b;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public String obtenerFecha() {
 
         String sqlquery = "SELECT CURRENT_DATE";
 
         System.out.println(sqlquery);
         try {
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sqlquery);
             if (rs.next()) {
                 String f = rs.getString("date");
                 return f;
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public ArrayList<Integer> usuarioPedido(String usuario) {
 
         ArrayList<Integer> cantidades = new ArrayList();
         try {
             String sqlquery = "SELECT cantidad FROM ("
                     + "SELECT usuario, nombre_vista, cantidad, tipo "
                     + "FROM \"PREPAS\".solicitud NATURAL JOIN \"PREPAS\".usuario "
                     + "NATURAL JOIN \"PREPAS\".equipo "
                     + "GROUP BY usuario, nombre_vista, cantidad, tipo "
                     + "ORDER BY tipo, nombre_vista) AS d";
 
             System.out.println(sqlquery);
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 Integer eps;
                 eps = Integer.parseInt(rs.getString("cantidad"));
                 cantidades.add(eps);
             }
 
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     public ArrayList<Equipo> consultarMateriales() {
 
         ArrayList<Equipo> equipos = new ArrayList();
 
         try {
             String sqlquery = "SELECT nombre_vista FROM \"PREPAS\".equipo";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return equipos;
 
     }
 
     public Boolean agregarProveedor(Proveedor p) {
         try {
             String sqlquery;
             sqlquery = "INSERT INTO \"PREPAS\".proveedor VALUES "
                     + "('" + p.getRif() + "' , '" + p.getNombre()
                     + "' , '" + p.getTelefono() + "' , '" + p.getEmail()
                     + "' , '" + p.getContacto() + "' , '" + p.getDireccion()
                     + "',  '" + p.getHabilitado() + "')";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public ArrayList<Proveedor> obtenerProveedores() {
         ArrayList<Proveedor> proveedores = new ArrayList<Proveedor>(0);
         try {
             String sqlquery;
             sqlquery = "SELECT * FROM \"PREPAS\".proveedor WHERE habilitado = 'true'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             ResultSet rs = stmt.executeQuery(sqlquery);
 
             while (rs.next()) {
                 Proveedor p = new Proveedor();
                 p.setRif(rs.getString("rif"));
                 p.setNombre(rs.getString("nombre"));
                 p.setTelefono(rs.getString("telefono"));
                 p.setEmail(rs.getString("email"));
                 p.setDireccion(rs.getString("direccion"));
                 p.setContacto(rs.getString("contacto"));
                 proveedores.add(p);
             }
             return proveedores;
         } catch (SQLException ex) {
             System.out.println("EXCEPCION");
             ex.printStackTrace();
         }
         return proveedores;
     }
 
     /*Deshabilita un proveedor. */
     public Boolean eliminarProveedor(Proveedor p) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".proveedor SET "
                     + "habilitado = 'false' "
                     + " WHERE rif = '" + p.getRif() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public void editarProveedor(Proveedor p) {
         try {
             String sqlquery = "UPDATE \"PREPAS\".proveedor SET "
                     + "rif = '" + p.getRif() + "' , "
                     + "nombre = '" + p.getNombre() + "' , "
                     + "telefono = '" + p.getTelefono() + "' , "
                     + "email = '" + p.getEmail() + "' , "
                     + "contacto = '" + p.getContacto() + "' , "
                     + "direccion = '" + p.getDireccion() + "'"
                     + " WHERE rif = '" + p.getRifAnterior() + "'";
 
             Statement stmt = conexion.createStatement();
             System.out.println(sqlquery);
             Integer i = stmt.executeUpdate(sqlquery);
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public Boolean existeProveedor(Proveedor p) {
 
         String sqlquery = "SELECT * FROM \"PREPAS\".proveedor"
                 + " WHERE rif ='" + p.getRif() + "' ";
 
         System.out.println(sqlquery);
         try {
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sqlquery);
             Boolean b = rs.next();
             return b;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 }
