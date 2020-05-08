 package analizador;
 import java.io.*;
 //import org.antlr.runtime.CommonTokenStream;
 import antlr.collections.AST;
 import antlr.ANTLRException;
 import antlr.RecognitionException;
 import antlr.Token;
 import antlr.TokenStreamException;
 
 public class Compilador {
 	
 	public static void main (String args[])   {
 		try {
 			if (args.length>=1) {
				int longitud = args[0].length();
		        String ext = args[0].substring(longitud - 4);
 		        if(ext.equalsIgnoreCase(".cpp")) {
		        	if (args.length < 2) {
			        		System.err.println("Introduzca fichero destino");
		        	}
		        	else{
			        	PrintStream fichero = new PrintStream(new File(args[1]));
						FileInputStream fis = new FileInputStream(args[0]);
						analizador Analizador = new analizador(fis);
						try {
							Token token = Analizador.nextToken();
							while(token.getType() != Token.EOF_TYPE) {
								fichero.println(token);
								token = Analizador.nextToken();
							}
						} catch (ANTLRException Ex) {				
								fichero.println("Error en token desconocido.");
								fichero.println(Ex.getMessage());
 						}
 					}
 		        } else {
 					System.err.println("No es un fichero con extensión correcta .cpp");
 		        }
 			} else {
 				System.err.println("No ha introducido ningún argumento [fichero fuente con extensión .cpp]");
 			}
 		} catch(FileNotFoundException fnfe) {
 			System.err.println("No se encontró el fichero especificado.");
 		}
 	}
 }
