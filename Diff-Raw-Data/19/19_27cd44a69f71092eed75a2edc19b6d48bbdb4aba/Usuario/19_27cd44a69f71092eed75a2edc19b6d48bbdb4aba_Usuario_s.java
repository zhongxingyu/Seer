 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Modelo;
 
 import Helper.Error;
 import Helper.Fecha;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import net.sf.oval.constraint.DateRange;
 import net.sf.oval.constraint.Email;
 import net.sf.oval.constraint.MatchPattern;
 import net.sf.oval.constraint.MaxLength;
 import net.sf.oval.constraint.MinLength;
 import net.sf.oval.constraint.NotBlank;
 import net.sf.oval.constraint.NotNull;
 
 /**
  *
  * @author juanjo
  */
 public class Usuario {
     public static final String NOMBRE_TABLA = "Usuario";
     public static final String COL_ID = "id";
     public static final String COL_NOMBRE_USUARIO = "username";
     public static final String COL_PASSWORD = "password";
     public static final String COL_NOMBRE = "nombre";
     public static final String COL_APELLIDO_P = "apellidoP";
     public static final String COL_APELLIDO_M = "apellidoM";
     public static final String COL_EMAIL = "email";
     public static final String COL_TIPO = "tipo";
     public static final String CAMPOS_TABLA = String.format("(%s,%s,%s,%s,%s,%s,%s)",
             COL_NOMBRE_USUARIO, COL_PASSWORD, COL_NOMBRE, COL_APELLIDO_P, COL_APELLIDO_M, COL_EMAIL, COL_TIPO); 
     
     private int id;
     @NotNull(message=COL_NOMBRE_USUARIO+": "+Error.EN_BLANCO)
     @NotBlank(message=COL_NOMBRE_USUARIO+": "+Error.EN_BLANCO)
     @MaxLength(value=32, message=COL_NOMBRE_USUARIO+": "+Error.TAM_MIN+": 32")
     @MinLength(value=4, message=COL_NOMBRE_USUARIO+": "+Error.TAM_MIN+": 4")
     private String nombreUsuario;
     @NotNull(message=COL_PASSWORD+": "+Error.EN_BLANCO)
     @NotBlank(message=COL_PASSWORD+": "+Error.EN_BLANCO)
     @MaxLength(value=32, message=COL_PASSWORD+": "+Error.TAM_MAX+": 32")
     @MinLength(value=6, message=COL_PASSWORD+": "+Error.TAM_MIN+": 6")
     private String password;
     @NotNull(message=COL_NOMBRE+": "+Error.EN_BLANCO)
     @NotBlank(message=COL_NOMBRE+": "+Error.EN_BLANCO)
     @MaxLength(value=32, message=COL_NOMBRE+": "+Error.TAM_MAX+": 32")
     private String nombre;
     @NotNull(message=COL_APELLIDO_P+": "+Error.EN_BLANCO)
     @NotBlank(message=COL_APELLIDO_P+": "+Error.EN_BLANCO)
     @MaxLength(value=32, message=COL_APELLIDO_P+": "+Error.TAM_MAX+": 32")
     private String apellidoP;
     @NotNull(message=COL_APELLIDO_M+": "+Error.EN_BLANCO)
     @NotBlank(message=COL_APELLIDO_M+": "+Error.EN_BLANCO)
     @MaxLength(value=32, message=COL_APELLIDO_M+": "+Error.TAM_MAX+": 32")
     private String apellidoM;
     @NotNull(message=COL_EMAIL+": "+Error.EN_BLANCO)
     @NotBlank(message=COL_EMAIL+": "+Error.EN_BLANCO)
     @Email(message=COL_EMAIL+": "+Error.EMAIL)
     @MaxLength(value=64, message=COL_EMAIL+": "+Error.TAM_MAX+": 64")
     private String email;
     private int tipo;
 
     public Usuario(int id, String nombreUsuario, String password, String nombre, String apellidoP, String apellidoM, String email, int tipo) {
         this.id = id;
         this.nombreUsuario = nombreUsuario;
         this.password = password;
         this.nombre = nombre;
         this.apellidoP = apellidoP;
         this.apellidoM = apellidoM;
         this.email = email;
         this.tipo = tipo;
     }
     
     public Usuario(int id, String password, String nombre, String apellidoP, String apellidoM, String email, int tipo) {
         this.id = id;
        this.nombreUsuario = "";
         this.password = password;
         this.nombre = nombre;
         this.apellidoP = apellidoP;
         this.apellidoM = apellidoM;
         this.email = email;
         this.tipo = tipo;
     }
     
     public Usuario(String nombreUsuario, String password, String nombre, String apellidoP, String apellidoM, String email, int tipo) {
         this.nombreUsuario = nombreUsuario;
         this.password = password;
         this.nombre = nombre;
         this.apellidoP = apellidoP;
         this.apellidoM = apellidoM;
         this.email = email;
         this.tipo = tipo;
     }
     
     public Usuario(ResultSet rs){
         try{
             this.id = rs.getInt(COL_ID);
             this.nombreUsuario = rs.getString(COL_NOMBRE_USUARIO);
             this.password = rs.getString(COL_PASSWORD);
             this.nombre = rs.getString(COL_NOMBRE);
             this.apellidoP = rs.getString(COL_APELLIDO_P);
             this.apellidoM = rs.getString(COL_APELLIDO_M);
             this.email = rs.getString(COL_EMAIL);
             this.tipo = rs.getInt(COL_TIPO);
 
         } catch (SQLException e){
             e.printStackTrace();
         }
     }
 
     public Usuario() {
             this.id = -1;
             this.nombreUsuario = "";
             this.password = "";
             this.nombre = "";
             this.apellidoP = "";
             this.apellidoM = "";
             this.email = "";
             this.tipo = -1;
 
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
     
 
     public String getNombreUsuario() {
         return nombreUsuario;
     }
 
     public void setNombreUsuario(String nombreUsuario) {
         this.nombreUsuario = nombreUsuario;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
     
     public String getNombre() {
         return nombre;
     }
 
     public void setNombre(String nombre) {
         this.nombre = nombre;
     }
 
     public String getApellidoP() {
         return apellidoP;
     }
 
     public void setApellidoP(String apellidoP) {
         this.apellidoP = apellidoP;
     }
 
     public String getApellidoM() {
         return apellidoM;
     }
 
     public void setApellidoM(String apellidoM) {
         this.apellidoM = apellidoM;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
    
     public int getTipo() {
         return tipo;
     }
 
     public void setTipo(int tipo) {
         this.tipo = tipo;
     }
     
     
 }
