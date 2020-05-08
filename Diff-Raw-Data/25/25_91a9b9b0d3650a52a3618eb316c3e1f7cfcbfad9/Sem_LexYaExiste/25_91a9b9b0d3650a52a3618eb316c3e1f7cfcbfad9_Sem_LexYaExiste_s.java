 
 /**
  *
  * @author victor
  */
 public class Sem_LexYaExiste extends Exception{
     
    String coderror = "Error 5 (";
    String mensaje1 = "' ya existe en este ambito ===== HAY QUE REVISARLO =====";
     String lexema;
     int fila,columna;
     
     public Sem_LexYaExiste(String lex, int _fila, int _columna){
         lexema = lex;
         fila = _fila;
         columna = _columna;
     }
     
     @Override
     public String toString(){
         return coderror + fila + "," + (columna+1) + "): '" + lexema + mensaje1;
     }
     
     
 }
