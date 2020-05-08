 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ve.com.fsjv.devsicodetv.utilitarios.otros;
 
 /**
  *
  * @author franklin
  */
 public class Procesos {
     
     public Procesos(){
         
     }
     
    public String generarIdentificadorRelacional (String acronimo, String tabla, String indice){
         String identificador = null;
        int cantidad = 1;
        identificador = this.concatenarelementos(acronimo, indice, cantidad);
         return identificador;
     }
     
    public String concatenarelementos (String acronimo, String indice, int cantidad){
         String concatenar = null;
         concatenar = acronimo + indice + cantidad;
         return concatenar;
     }
     
    public int chequearIdentificadorRelacional (String identificador){
         int chequeado = 0;
         return chequeado;
     }
 }
