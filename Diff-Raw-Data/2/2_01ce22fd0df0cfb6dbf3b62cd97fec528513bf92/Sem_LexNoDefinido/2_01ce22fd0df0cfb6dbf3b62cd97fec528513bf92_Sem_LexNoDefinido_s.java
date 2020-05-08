 
 /**
  *
  * @author victor
  */
 public class Sem_LexNoDefinido extends Exception{
     
     String coderror = "Error 2 (";
     String mensaje1 = "' no ha sido declarado";
     String lexema;
     int fila,columna;
     
     public Sem_LexNoDefinido(String lex, int _fila, int _columna){
         lexema = lex;
         fila = _fila;
         columna = _columna;
     }
     
     public void setFilaColumna(int _fila, int _columna){
 		fila = _fila;
 		columna = _columna;
 	}
     
     @Override
     public String toString(){
        return coderror + fila + "," + (columna+1) + "): '" + lexema + mensaje1;
     }
     
     
 }
