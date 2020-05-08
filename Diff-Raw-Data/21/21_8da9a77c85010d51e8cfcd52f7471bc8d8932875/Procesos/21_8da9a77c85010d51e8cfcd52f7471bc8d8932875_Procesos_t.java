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
     
     public String generarCodigo(String acronimo, String tabla, String indice, int tipo){
         String identificador = null;
        String sql = "";
        int cantidad = this.obtenerCantidad(sql);
        identificador = this.concatenarCodigo(acronimo, indice, cantidad, tipo);
        int bandera = this.chequearCodigo(identificador);
        if(bandera > 0){
            cantidad++;
            identificador = this.concatenarCodigo(acronimo, indice, cantidad, tipo);
        }
         return identificador;
     }
     
     public String concatenarCodigo(String acronimo, String indice, int cantidad, int tipo){
         String concatenar = null;
         concatenar = acronimo + indice + cantidad;
         return concatenar;
     }
     
     public int chequearCodigo(String identificador){
         int chequeado = 0;
         return chequeado;
     }
     
     public int obtenerCantidad(String sql){
         int cantidad = 0;
        cantidad++;
         return cantidad;
     }
     
 }
