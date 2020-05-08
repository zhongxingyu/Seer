 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ve.com.fsjv.devsicodetv.modelos;
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import static javax.persistence.GenerationType.IDENTITY;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author FAMILIA-SJ
  */
 @Entity
@Table(name="persona", catalog="sicodetdev")
 public class Persona implements Serializable {
     private Integer IdPersona;
     private int codigoRelacional;
     private String nacionalidad;
     private int cedulaIdentidad;
     private String nombres;
     private String apellidos;
     private String sexo;
     private java.util.Date fechaNacimiento;
     
     private String lugarNacimiento; public Persona(Integer IdPersona, int codigoRelacional, String nacionalidad, int cedulaIdentidad, String nombres, String apellidos, String sexo, Date fechaNacimiento, String lugarNacimiento, String estadoCivil, int estatus) {
         this.IdPersona = IdPersona;
         this.codigoRelacional = codigoRelacional;
         this.nacionalidad = nacionalidad;
         this.cedulaIdentidad = cedulaIdentidad;
         this.nombres = nombres;
         this.apellidos = apellidos;
         this.sexo = sexo;
         this.fechaNacimiento = fechaNacimiento;
         this.lugarNacimiento = lugarNacimiento;
         this.estadoCivil = estadoCivil;
         this.estatus = estatus;
     }
     private String estadoCivil;
     private int estatus;
     
     
     
     public Persona(){}
 
     @Id
     @GeneratedValue(strategy=IDENTITY)
     @Column(name="idpersona", unique=true, nullable=true)
     public int getIdPersona() {
         return IdPersona;
     }
 
     public void setIdPersona(int IdPersona) {
         this.IdPersona = IdPersona;
     }
 
     @Column(name = "codigo_relacional", nullable = false)
     public int getCodigoRelacional() {
         return codigoRelacional;
     }
 
     public void setCodigoRelacional(int codigoRelacional) {
         this.codigoRelacional = codigoRelacional;
     }
 
     @Column(name = "nacionalidad", nullable = false)
     public String getNacionalidad() {
         return nacionalidad;
     }
 
     public void setNacionalidad(String nacionalidad) {
         this.nacionalidad = nacionalidad;
     }
 
     @Column(name = "cedula_identidad", nullable = false)
     public int getCedulaIdentidad() {
         return cedulaIdentidad;
     }
 
     public void setCedulaIdentidad(int cedulaIdentidad) {
         this.cedulaIdentidad = cedulaIdentidad;
     }
 
     @Column(name = "nombres", nullable = false)
     public String getNombres() {
         return nombres;
     }
 
     public void setNombres(String nombres) {
         this.nombres = nombres;
     }
 
     @Column(name = "apellidos", nullable = false)
     public String getApellidos() {
         return apellidos;
     }
 
     public void setApellidos(String apellidos) {
         this.apellidos = apellidos;
     }
 
     @Column(name = "sexo", nullable = false)
     public String getSexo() {
         return sexo;
     }
 
     public void setSexo(String sexo) {
         this.sexo = sexo;
     }
 
     @Column(name = "fecha_nacimiento", nullable = false)
     public java.util.Date getFechaNacimiento() {
         return fechaNacimiento;
     }
 
     public void setFechaNacimiento(java.util.Date fechaNacimiento) {
         this.fechaNacimiento = fechaNacimiento;
     }
 
     @Column(name = "lugar_nacimiento", nullable = false)
     public String getLugarNacimiento() {
         return lugarNacimiento;
     }
 
     public void setLugarNacimiento(String lugarNacimiento) {
         this.lugarNacimiento = lugarNacimiento;
     }
 
     @Column(name = "estado_civil", nullable = false)
     public String getEstadoCivil() {
         return estadoCivil;
     }
 
     public void setEstadoCivil(String estadoCivil) {
         this.estadoCivil = estadoCivil;
     }
 
     @Column(name = "estatus", nullable = false)
     public int getEstatus() {
         return estatus;
     }
 
     public void setEstatus(int estatus) {
         this.estatus = estatus;
     }
     
 }
