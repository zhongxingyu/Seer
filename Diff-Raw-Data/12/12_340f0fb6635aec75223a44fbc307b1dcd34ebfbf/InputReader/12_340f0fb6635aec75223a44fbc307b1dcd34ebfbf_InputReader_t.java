 package LexicalAnalyzer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 /**
  * Lectura del archivo fuente.
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class InputReader {
 
     private String filename, currentLine;
     private File file;
     private InputStreamReader reader;
     private BufferedReader buffer;
     private InputStream in;
     private int mark;
 
     /**
      * Constructor de la clase InputReader.
      *
      * @param filename Path del archivo con el código fuente
      */
     public InputReader(String filename) {
         this.filename = filename;
         open();
     }
 
     /**
      * Apertura del archivo con el código fuente.
      *
      * Inicialización de variables de control para el recorrido del mismo.
      */
     private void open() {
         try {
             file = new File(filename);
             in = new FileInputStream(file);
             reader = new InputStreamReader(in);
             buffer = new BufferedReader(reader);
             currentLine = buffer.readLine();
         } catch (FileNotFoundException ex) {
             System.err.println("El sistema no puede encontrar el archivo especificado.");
         } catch (IOException ex) {
             System.err.println("Error al leer el archivo de entrada.");
         }
     }
 
     /**
      * Cierre del archivo con el código fuente.
      */
     private void close() {
         try {
             buffer.close();
         } catch (IOException ex) {
             System.err.println("Error al cerrar el archivo de entrada.");
         }
     }
 
     /**
      * Lectura de caracteres.
      *
     * Se procesa el archivo línea por linea
     *  - Si la línea a procesar es nula, entonces se alcanzó el fin de archivo
     *  - Si el marcador de lectura (mark) es mayor al tamaño de la línea a procesar,
     *    se procede a leer una línea nueva y a reiniciar la marca. 
     *  - Si la marcador de lectura (mark) es menor al tamaño de la línea a procesar,
     *    se procede a leer el caracter que apunta la marca y a incrementar la marca.
      *
      * @return c, caracter leído
      */
     public char readChar() {
         char c = ' ';
 
         try {
             if (currentLine == null) {
                 c = '\0'; // null byte - EOF
                 close();
             } else if (mark >= currentLine.length()) { // /n
                 currentLine = buffer.readLine();
                 c = '\n';
                 mark = 0;
             } else {
                 c = currentLine.charAt(mark);
                 mark++;
             }
 
         } catch (IOException ex) {
             System.err.println("Error al leer caracter de buffer.");
         }
 
         return c;
     }
 
     /**
      * Retorna el valor actual del marcador.
      *
      * @return mark, valor actual del marcador
      */
     public int getMark() {
         return mark;
     }
 
     /**
      * Vuelve el marcador una posición hacia atrás
      */
     public void resetMark() {
         mark--;
     }
 }
