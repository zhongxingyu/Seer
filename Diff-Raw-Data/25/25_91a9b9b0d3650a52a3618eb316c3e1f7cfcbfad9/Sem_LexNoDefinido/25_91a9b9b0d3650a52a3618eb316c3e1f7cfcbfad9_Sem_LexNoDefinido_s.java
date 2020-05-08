 
 /**
  *
  * @author victor
  */
 public class Sem_LexNoDefinido extends Exception{
     
    String coderror = "Error 6 (";
    String mensaje1 = "' no ha sido declarado  ===== HAY QUE REVISARLO =====";
     String lexema;
     int fila,columna;
     
     public Sem_LexNoDefinido(String lex, int _fila, int _columna){
         lexema = lex;
         fila = _fila;
         columna = _columna;
     }
     
     @Override
     public String toString(){
         return coderror + fila + "," + (columna+1) + "): '" + lexema + mensaje1;
     }
     
     
 }
