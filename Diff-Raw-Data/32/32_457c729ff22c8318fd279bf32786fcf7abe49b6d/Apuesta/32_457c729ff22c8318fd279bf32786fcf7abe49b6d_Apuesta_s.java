 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Logica;
 
 import java.util.Date;
 
 /**
  *
  * @author Usuario
  */
 public class Apuesta {
     private String nombreApostador;
     private String apellidoApostador;
     private int cedulaApostador;
     private float montoApuesta;
    private Date fechaApuesta;
     private int idEvento;
     private int idParticipante;
     private int idMaquina;
 
    public Apuesta(String nombreApostador, String apellidoApostador, int cedulaApostador, float montoApuesta, Date fechaApuesta, int idEvento, int idParticipante) {
         this.nombreApostador = nombreApostador;
         this.apellidoApostador = apellidoApostador;
         this.cedulaApostador = cedulaApostador;
         this.montoApuesta = montoApuesta;
        this.fechaApuesta = fechaApuesta;
         this.idEvento = idEvento;
         this.idParticipante = idParticipante;
         this.idMaquina = Logica.dameLogica().getID();
     }
 
     public String getApellidoApostador() {
         return apellidoApostador;
     }
 
     public void setApellidoApostador(String apellidoApostador) {
         this.apellidoApostador = apellidoApostador;
     }
 
     public int getCedulaApostador() {
         return cedulaApostador;
     }
 
     public void setCedulaApostador(int cedulaApostador) {
         this.cedulaApostador = cedulaApostador;
     }
 
    public Date getFechaApuesta() {
         return fechaApuesta;
     }
     
    public void setFechaApuesta(Date fechaApuesta) {
        this.fechaApuesta = fechaApuesta;
     }
 
     public float getMontoApuesta() {
         return montoApuesta;
     }
 
     public void setMontoApuesta(float montoApuesta) {
         this.montoApuesta = montoApuesta;
     }
 
     public String getNombreApostador() {
         return nombreApostador;
     }
 
     public void setNombreApostador(String nombreApostador) {
         this.nombreApostador = nombreApostador;
     }
     
     
 }
