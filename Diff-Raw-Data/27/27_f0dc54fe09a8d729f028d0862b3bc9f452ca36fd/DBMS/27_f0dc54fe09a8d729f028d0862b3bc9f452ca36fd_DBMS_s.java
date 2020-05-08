 package DBMS;
 
 import Clases.Coordinacion;
 import Forms.CreateUserForm;
 import Clases.*;
 import Forms.EliminarUserForm;
 import Sistemas.*;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Langtech
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
                     "jdbc:postgresql://localhost:5432/evalprof",
                     "langtech",
                     "evalprof");
             return true;
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
         return false;
     }
 
     /* Esta funcion se utiliza cuando un usuario inicia sesion
      * para verificar que su nombre y contrasena sean correctos
      * 
      */
     public Usuario verificarCas(Usuario u) {
 
         PreparedStatement psConsultar = null;
         try {
 
             psConsultar = conexion.prepareStatement("SELECT * FROM usuario"
                     + " WHERE usbid = ? AND contrasena = ?");
 
             psConsultar.setString(1, u.getUsbid());
             psConsultar.setString(2, u.getContrasena());
             System.out.println(psConsultar.toString());
             ResultSet rs = psConsultar.executeQuery();
 
             while (rs.next()) {
                 u.setUsbid(rs.getString("usbid"));
                 u.setTipousuario(rs.getString("tipousuario"));
 
                 return u;
             }
 
         } catch (SQLException ex) {
             Logger.getLogger(DBMS.class.getName()).log(Level.SEVERE, null, ex);
         }
         u.setUsbid(null);
 
         return u;
     }
 
     public boolean agregarUsuario(CreateUserForm u) {
 
         PreparedStatement psAgregar1 = null;
         PreparedStatement psAgregar2 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("INSERT INTO usuario(usbid, contrasena) VALUES (?, ?);");
             psAgregar1.setString(1, u.getUsbid());
             psAgregar1.setString(2, u.getContrasena1());
             System.out.println(psAgregar1.toString());
             Integer i = psAgregar1.executeUpdate();
 
             psAgregar2 = conexion.prepareStatement("INSERT INTO profesor(usbid,email) VALUES (?, ?);");
             psAgregar2.setString(1, u.getUsbid());
             psAgregar2.setString(2, u.getUsbid() + "@usb.ve");
             Integer j = psAgregar2.executeUpdate();
 
             return i > 0 && j > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public ArrayList<Usuario> listarUsuarios() {
 
         ArrayList<Usuario> usrs = new ArrayList<Usuario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM usuario ORDER BY usbid");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Usuario u = new Usuario();
                 u.setUsbid(rs.getString("usbid"));
                 u.setTipousuario(rs.getString("tipousuario"));
                 u.setDepartamento(rs.getString("departamento"));
                 usrs.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return usrs;
     }
 
     public ArrayList[] mostrarUsuarios() {
 
         ArrayList[] total = new ArrayList[4];
         ArrayList<Usuario> profesores = new ArrayList<Usuario>(0);
         ArrayList<Usuario> decanatos = new ArrayList<Usuario>(0);
         ArrayList<Usuario> departamentos = new ArrayList<Usuario>(0);
         ArrayList<Usuario> coordinaciones = new ArrayList<Usuario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM usuario ORDER BY usbid");
             ResultSet rs = ps.executeQuery();
             String tipousuario = null;
             while (rs.next()) {
                 Usuario u = new Usuario();
                 u.setUsbid(rs.getString("usbid"));
                 u.setDepartamento(rs.getString("departamento"));
                 tipousuario = rs.getString("tipousuario");
                 if (tipousuario.equals("profesor")) {
                     profesores.add(u);
                 } else if (tipousuario.equals("decanato")) {
                     decanatos.add(u);
                 } else if (tipousuario.equals("departamento")) {
                     departamentos.add(u);
                 } else if (tipousuario.equals("coordinacion")) {
                     coordinaciones.add(u);
                 }
             }
             total[0] = profesores;
             total[1] = decanatos;
             total[2] = departamentos;
             total[3] = coordinaciones;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return total;
     }
 
     public ArrayList<Usuario> listarProfesores() {
 
         ArrayList<Usuario> usrs = new ArrayList<Usuario>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM usuario WHERE TIPOUSUARIO = 'profesor' ORDER BY usbid");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Usuario u = new Usuario();
                 u.setUsbid(rs.getString("usbid"));
                 u.setTipousuario(rs.getString("tipousuario"));
                 u.setDepartamento(rs.getString("departamento"));
                 usrs.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return usrs;
     }
 
     public ArrayList<Decanato> listarDecanatos() {
 
         ArrayList<Decanato> dcnto = new ArrayList(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM decanato ORDER BY codigo");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Decanato u = new Decanato();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 dcnto.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return dcnto;
     }
 
     public ArrayList<Coordinacion> listarCoordinaciones() {
 
         ArrayList<Coordinacion> coords = new ArrayList<Coordinacion>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM COORDINACION ORDER BY CODIGO");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Coordinacion u = new Coordinacion();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 coords.add(u);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return coords;
     }
 
     public ArrayList<Departamento> listarDepartamentos() {
 
         ArrayList<Departamento> dptos = new ArrayList<Departamento>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM DEPARTAMENTO ORDER BY CODIGO");
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Departamento u = new Departamento();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 dptos.add(u);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return dptos;
     }
 
     public Profesor obtenerInfoProfesor(String usbid) {
 
         Profesor u = new Profesor();
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM profesor WHERE usbid = ?");
             ps.setString(1, usbid);
 
             ResultSet rs = ps.executeQuery();
 
             while (rs.next()) {
                 u.setUsbid(rs.getString("usbid"));
                 u.setNombre(rs.getString("nombre"));
                 u.setApellido(rs.getString("apellido"));
                 u.setCedula(rs.getString("cedula"));
                 u.setGenero(rs.getString("genero"));
                 u.setEmail(rs.getString("email"));
                 u.setEmail_personal(rs.getString("email_personal"));
                 u.setNivel(rs.getString("nivel"));
                 u.setJubilado(rs.getString("jubilado"));
                 u.setLapso_contractual_inicio(rs.getString("lapso_contractual_inicio"));
                 u.setLapso_contractual_fin(rs.getString("lapso_contractual_fin"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public boolean eliminarUsuario(EliminarUserForm u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("DELETE FROM usuario WHERE ( usbid = ? )");
 
             ps.setString(1, u.getUsbid());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean actualizarInfoProfesor(Profesor u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("UPDATE profesor SET email = ?, email_personal = ?, nivel = ?, jubilado = ?, lapso_contractual_inicio = ?, lapso_contractual_fin = ? WHERE usbid = ? ");
             psAgregar1.setString(1, u.getEmail());
             psAgregar1.setString(2, u.getEmail_personal());
             psAgregar1.setString(3, u.getNivel());
             psAgregar1.setString(4, u.getJubilado());
             psAgregar1.setString(5, u.getLapso_contractual_inicio());
             psAgregar1.setString(6, u.getLapso_contractual_fin());
             psAgregar1.setString(7, u.getUsbid());
 
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean resetInfoProfesor(Profesor u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("UPDATE profesor SET email_personal = ?, nivel = ?, jubilado = ?, lapso_contractual_inicio = ?, lapso_contractual_fin = ? WHERE usbid = ? ");
             psAgregar1.setString(1, "");
             psAgregar1.setString(2, "");
             psAgregar1.setString(3, "");
             psAgregar1.setString(4, "");
             psAgregar1.setString(5, "");
             psAgregar1.setString(6, u.getUsbid());
 
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarRolUsuario(EliminarUserForm u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE usuario SET tipousuario = ?, departamento = ? WHERE ( usbid = ? )");
 
             ps.setString(1, u.getTipousuario());
             ps.setString(2, u.getDepartamento());
             ps.setString(3, u.getUsbid());
 
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
 
     }
 
     public boolean eliminarCoordinacion(Coordinacion u) {
 
         PreparedStatement psEliminar1 = null;
         try {
             psEliminar1 = conexion.prepareStatement("DELETE FROM coordinacion AS c WHERE (c.codigo = ?);");
             psEliminar1.setString(1, u.getCodigo());
 
             Integer i = psEliminar1.executeUpdate();
 
             System.out.println("retorna: " + (i > 0));
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean eliminarDepartamento(Departamento u) {
 
         PreparedStatement psEliminar1 = null;
         try {
             psEliminar1 = conexion.prepareStatement("DELETE FROM DEPARTAMENTO AS d WHERE (d.codigo = ?);");
             psEliminar1.setString(1, u.getCodigo());
 
             Integer i = psEliminar1.executeUpdate();
 
             System.out.println("retorna: " + (i > 0));
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean registrarDecanato(Decanato u) {
 
         PreparedStatement psAgregar1 = null;
 
         try {
             psAgregar1 = conexion.prepareStatement("INSERT INTO decanato(codigo, nombre) VALUES (?, ?);");
             psAgregar1.setString(1, u.getCodigo());
             psAgregar1.setString(2, u.getNombre());
 
             Integer i = psAgregar1.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean registrarCoordinacion(Coordinacion c) {
         PreparedStatement psAgregar = null;
 
         try {
             psAgregar = conexion.prepareStatement("INSERT INTO COORDINACION(codigo, nombre) VALUES (?, ?);");
             psAgregar.setString(1, c.getCodigo());
             psAgregar.setString(2, c.getNombre());
 
             Integer i = psAgregar.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarNombreDecanato(Decanato u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE decanato SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getNombre());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public boolean actualizarNombreCoordinacion(Coordinacion u) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE COORDINACION SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, u.getNombre());
             ps.setString(2, u.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
 
     public Decanato obtenerNombreDecanato(Decanato u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM decanato WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public Coordinacion obtenerNombreCoordinacion(Coordinacion u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM COORDINACION WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public Departamento obtenerNombreDepartamento(Departamento u) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM departamento WHERE codigo = ?");
             ps.setString(1, u.getCodigo());
 
             ResultSet rs = ps.executeQuery();
 
 
 
             while (rs.next()) {
                 u.setNombre(rs.getString("nombre"));
             }
 
             return u;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return u;
     }
 
     public ArrayList<SINAI> listarSINAI(String usbid) {
 
         ArrayList<SINAI> sinai = new ArrayList<SINAI>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM SINAI WHERE usbid = ?");
             ps.setString(1, usbid);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 SINAI u = new SINAI();
                 u.setNombre(rs.getString("nombre"));
                 u.setFecha_inic(rs.getString("Fecha_inic"));
                 u.setFecha_fin(rs.getString("Fecha_fin"));
                 sinai.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return sinai;
     }
 
     public ArrayList<CCT> listarCCT(String usbid) {
 
         ArrayList<CCT> cct = new ArrayList<CCT>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM CCT WHERE usbid = ?");
             ps.setString(1, usbid);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 CCT u = new CCT();
                 u.setIdent(rs.getString("ident"));
                 u.setTitulo(rs.getString("titulo"));
                 u.setFecha_inic(rs.getString("Fecha_inic"));
                 u.setFecha_fin(rs.getString("Fecha_fin"));
                 u.setCarrera(rs.getString("carrera"));
                 u.setTipo(rs.getString("tipo"));
                 cct.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return cct;
     }
 
     public ArrayList<DACE> listarDACE(String usbid, int ano, String trim) {
 
         ArrayList<DACE> dace = new ArrayList<DACE>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT * FROM DACE WHERE usbid = ? AND ano = ? AND trimestre = ?");
             ps.setString(1, usbid);
             ps.setInt(2, ano);
             ps.setString(3, trim);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 DACE u = new DACE();
                 u.setCodigo(rs.getString("codigo"));
                 u.setNombre(rs.getString("nombre"));
                 u.setUno(rs.getInt("uno"));
                 u.setDos(rs.getInt("dos"));
                 u.setTres(rs.getInt("tres"));
                 u.setCuatro(rs.getInt("cuatro"));
                 u.setCinco(rs.getInt("cinco"));
                 u.setRetirados(rs.getInt("retirados"));
                 dace.add(u);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return dace;
     }
 
     public ArrayList<Materia> listarMateriasOfertadas(String id_departamento) {
 
         ArrayList<Materia> materias = new ArrayList<Materia>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT codigo, nombre, estado FROM oferta, materia WHERE codigo_departamento = ? AND codigo_materia = codigo;");
             ps.setString(1, id_departamento);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Materia m = new Materia();
                 m.setCodigo(rs.getString("codigo"));
                 m.setNombre(rs.getString("nombre"));
                 m.setEstado(rs.getString("estado"));
                 materias.add(m);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return materias;
     }
 
     public ArrayList<Profesor> listarProfesoresDepartamento(String id_departamento) {
 
         ArrayList<Profesor> profesores = new ArrayList<Profesor>(0);
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT usbid, lapso_contractual_inicio, nombre, apellido FROM pertenece, profesor WHERE codigo_departamento = ? AND usbid_profesor = usbid ORDER BY lapso_contractual_inicio;");
             ps.setString(1, id_departamento);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Profesor p = new Profesor();
                 p.setUsbid(rs.getString("usbid"));
                 p.setNombre(rs.getString("nombre"));
                 p.setApellido(rs.getString("apellido"));
                 p.setLapso_contractual_inicio(rs.getString("lapso_contractual_inicio"));
                 profesores.add(p);
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return profesores;
     }
 
     public void enviarMemoEvaluarProfesor(String usbid_prof) {
         PreparedStatement ps = null;
         PreparedStatement ps2 = null;
         try {
             ps = conexion.prepareStatement("SELECT dicta.codigo_materia, usbid_profesor, codigo_coordinacion FROM dicta, maneja WHERE usbid_profesor = ? AND dicta.codigo_materia = maneja.codigo_materia;");
             ps.setString(1, usbid_prof);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 String materia = rs.getString("codigo_materia");
                 String profesor = rs.getString("usbid_profesor");
                 String coordinacion = rs.getString("codigo_coordinacion");
                 ps2 = conexion.prepareStatement("INSERT INTO evaluar VALUES (?,?,?);");
                 ps2.setString(1, coordinacion);
                 ps2.setString(2, profesor);
                 ps2.setString(3, materia);
                 ps2.executeUpdate();
             }
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
     }
 
     public void enviarMemoEvaluar(String[] id_profesores) {
         for (int i = 0; i < id_profesores.length; i++) {
             enviarMemoEvaluarProfesor(id_profesores[i]);
         }
     }
 
     public boolean eliminarMateria(String id_materia) {
         PreparedStatement ps = null;
 
         try {
             ps = conexion.prepareStatement("DELETE FROM MATERIA WHERE (codigo = ?);");
             ps.setString(1, id_materia);
 
             Integer i = ps.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public boolean modificarMateria(Materia materia) {
         PreparedStatement ps = null;
 
         try {
             ps = conexion.prepareStatement("UPDATE MATERIA SET codigo = ?, nombre = ?, creditos = ? WHERE ( codigo = ? );");
             ps.setString(1, materia.getCodigo());
             ps.setString(2, materia.getNombre());
             ps.setString(3, materia.getCreditos());
             ps.setString(4, materia.getViejoCodigo());
 
             Integer i = ps.executeUpdate();
 
             return i > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 
     public Materia obtenerDatosMateria(Materia materia) {
         PreparedStatement ps = null;
 
         try {
             ps = conexion.prepareStatement("SELECT * FROM MATERIA WHERE codigo = ?;");
             ps.setString(1, materia.getCodigo());
 
             ResultSet rs = ps.executeQuery();
             Materia m = null;
 
             while (rs.next()) {
                 m = new Materia();
                 m.setCodigo(rs.getString("codigo"));
                 m.setNombre(rs.getString("nombre"));
                 m.setCreditos(rs.getString("creditos"));
             }
 
             return m;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
     
     public String obtenerDatosDepartamento(String departamento) {
         PreparedStatement ps = null;
 
         try {
             ps = conexion.prepareStatement("SELECT * FROM DEPARTAMENTO WHERE codigo = ?;");
             ps.setString(1, departamento);
 
             ResultSet rs = ps.executeQuery();
             rs.next();
             
             return rs.getString("codigo_materias");
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public boolean vincularMateriaCoordinacion(String cod_coord, String cod_materia) {
         PreparedStatement ps = null;
 
         try {
             ps = conexion.prepareStatement("INSERT INTO maneja VALUES (?,?)");
             ps.setString(1, cod_coord);
             ps.setString(2, cod_materia);
             Integer i = ps.executeUpdate();
 
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
 
     }
     
     public boolean desvincularMateriaCoordinacion(String cod_coord, String cod_materia) {
         PreparedStatement ps = null;
 
         try {
             ps = conexion.prepareStatement("DELETE FROM maneja WHERE codigo_coordinacion = ? AND codigo_materia = ?");
             ps.setString(1, cod_coord);
             ps.setString(2, cod_materia);
             Integer i = ps.executeUpdate();
 
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
 
     }
 
     public ArrayList<Materia> listarMateriasCoordinacion(String cod_coord) {
         PreparedStatement ps = null;
         ArrayList<Materia> materias = new ArrayList<Materia>(0);
 
         try {
             ps = conexion.prepareStatement("SELECT codigo, nombre FROM maneja, materia WHERE codigo_coordinacion = ? and codigo_materia = codigo");
             ps.setString(1, cod_coord);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 Materia m = new Materia();
                 m.setCodigo(rs.getString("codigo"));
                 m.setNombre(rs.getString("nombre"));
                 materias.add(m);
             }
             return materias;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public int contarEvaluacionesPendientes(String id_coordinacion) {
 
         PreparedStatement ps = null;
         try {
             ps = conexion.prepareStatement("SELECT Count(usbid_profesor) FROM evaluar WHERE codigo_coordinacion = ?;");
             ps.setString(1, id_coordinacion);
 
             ResultSet rs = ps.executeQuery();
             rs.next();
             return Integer.parseInt(rs.getString("count"));
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return 0;
     }
 
     public ArrayList<dicta> listarEvaluacionesPendientes(String id_coordinacion) {
 
         PreparedStatement ps = null;
         PreparedStatement ps2 = null;
         ArrayList<dicta> dicta_materia = new ArrayList(0);
         String codigoMateria = "";
         try {
             ps = conexion.prepareStatement("SELECT DISTINCT codigo_materia, count(codigo_materia) FROM evaluar WHERE codigo_coordinacion = ? GROUP BY codigo_materia;");
             ps.setString(1, id_coordinacion);
 
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
                 dicta d = new dicta();
                 d.setNumeroMateria(rs.getString("count"));
                 codigoMateria = rs.getString("codigo_materia");
                 d.setCodigoMateria(codigoMateria);
 
                 System.out.println(codigoMateria + " " + d.getNumeroMateria());
 
                 ps2 = conexion.prepareStatement("SELECT codigo_materia, usbid, nombre, apellido FROM evaluar, profesor WHERE codigo_coordinacion = ? AND usbid = usbid_profesor AND codigo_materia= ?;");
                 ps2.setString(1, id_coordinacion);
                 ps2.setString(2, codigoMateria);
 
                 ResultSet rs2 = ps2.executeQuery();
 
                 while (rs2.next()) {
                     Profesor p = new Profesor();
                     p.setUsbid(rs2.getString("usbid"));
                     p.setNombre(rs2.getString("nombre"));
                     p.setApellido(rs2.getString("apellido"));
                     d.addProfesor(p);
                 }
                 d.setPrimerProfesor();
                 System.out.println("primer profesor: " + d.getPrimerProfesor().getNombre());
                 dicta_materia.add(d);
             }
             return dicta_materia;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
     
     public boolean registrarMateria(Materia m, String id_departamento) {
         
         PreparedStatement ps1;
         PreparedStatement ps2;
         
         try {
             ps1 = conexion.prepareStatement("INSERT INTO MATERIA(codigo, nombre, creditos, estado) VALUES (?, ?, ?, ?);");
             ps1.setString(1, m.getCodigo());
             ps1.setString(2, m.getNombre());
             ps1.setString(3, m.getCreditos());
             ps1.setString(4, "visible");
             
             ps2 = conexion.prepareStatement("INSERT INTO OFERTA(codigo_materia, codigo_departamento) VALUES (?, ?);");
             ps2.setString(1, m.getCodigo());
             ps2.setString(2, id_departamento);
             
             Integer i = ps1.executeUpdate();
             Integer j = ps2.executeUpdate();
             
             
             return i > 0 && j > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
     public boolean actualizarDepartamento(Departamento d) {
         PreparedStatement ps = null;
         try {
 
             ps = conexion.prepareStatement("UPDATE departamento SET nombre = ? WHERE ( codigo = ? )");
 
             ps.setString(1, d.getNombre());
             ps.setString(2, d.getCodigo());
             Integer s = ps.executeUpdate();
 
             return s > 0;
 
         } catch (SQLException ex) {
             ex.printStackTrace();
             return false;
         }
     }
     
      public boolean registrarDepartamento(Departamento d) {
         PreparedStatement ps;
 
         try {
             ps = conexion.prepareStatement("INSERT INTO departamento VALUES (?,?,?)");
             ps.setString(1, d.getCodigo());
             ps.setString(2, d.getNombre());
             ps.setString(3, "XXX");
             Integer i = ps.executeUpdate();
 
             return i > 0;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return false;
 
     }   
 }
