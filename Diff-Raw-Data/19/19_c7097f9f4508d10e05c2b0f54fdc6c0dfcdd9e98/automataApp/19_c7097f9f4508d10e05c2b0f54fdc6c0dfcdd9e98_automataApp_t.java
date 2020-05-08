 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package javaapplication3;
 
 /**
  *
  * @author Administrador
  */
 public class automataApp {
     
     protected int[][] matrizTrans;
     protected int estado;
 
     public automataApp(){
         //Inicializar variables
         matrizTrans[0][0] = 1; matrizTrans[0][1] = 4; matrizTrans[0][2] = -1;
         matrizTrans[1][0] = 4; matrizTrans[1][1] = 3; matrizTrans[1][2] = -1;
         matrizTrans[2][0] = -1; matrizTrans[2][1] = -1; matrizTrans[2][2] = -1;
         matrizTrans[3][0] = 3; matrizTrans[3][1] = -1; matrizTrans[3][2] = -1;
        estado = -1;
    }
    
    private void asumiendoValores(int val){
        estado = matrizTrans[estado][val]; 
        switch(estado){
            case -1:{
                System.out.println("Error!");
                break;
            }
            case 3:{
                System.out.println("Refresco!");
                break;
            }
        }
            
     }
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
        
     }
 }
