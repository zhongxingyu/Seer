 /**
  * @(#)Linea.java
  * Validacion sintaxis ensamblador
  *
  * @author Delgado Meza Julieta jocelyne
  * @version 1.00 2013/8/27
  */
 
 import java.util.Scanner;
 import java.util.*;
 import java.io.*;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.util.regex.*; // Para expresiones regulares
 import java.util.StringTokenizer; //para separar los elementos de la linea
 
 public class Linea {
         
     //atributos
 	String etq;
 	String codop;
 	String oper;
 
     //constructor
     public Linea (String e, String c, String o) {
     	//Inicialice atributos
     	etq = e;
     	codop = c;
     	oper = o;
     }
     
     public boolean validarEtiqueta(StringTokenizer str, Linea linea_ens, boolean edoERROR, FileWriter archErr,int numeroLinea){
     	String contenido = new String (str.nextToken());
     	System.out.println(contenido);
     	Pattern pat = Pattern.compile("[a-zA-Z0-9_]+");
      	Matcher mat = pat.matcher(contenido);
     	if(contenido.length()<=8){
     		if(mat.matches()){
     			System.out.println("Si es una etiqueta");
     			linea_ens.etq = contenido;
     			while (str.hasMoreTokens() && edoERROR == false) {
                 				contenido = str.nextToken();
                 				if(contenido.matches("^[a-zA-Z].*")){
                 					System.out.println(contenido);
                 					System.out.println("VAS PARA CODOP");
                 					edoERROR = linea_ens.validarCodigo(str,contenido,linea_ens,edoERROR,archErr,numeroLinea);
                 				}
                 				else{
                 					edoERROR = true;
     								linea_ens.guardarError(archErr,"ERROR la Etiqueta solo puede empezar con letra",numeroLinea);
                 				} 
                 			}
     		}
     		else{
     			edoERROR = true;
     			linea_ens.guardarError(archErr,"ERROR la Etiqueta contiene caracteres no alfanumericos",numeroLinea);
     		}
     				
     	}
     	else{
     		edoERROR = true;
     		linea_ens.guardarError(archErr,"ERROR la Etiqueta contiene mas de 8 caracteres",numeroLinea);
     	}
     	return edoERROR;	
     }
     
     public boolean validarCodigo(StringTokenizer str, String contenido, Linea linea_ens, boolean edoERROR, FileWriter archErr,int numeroLinea){
     	System.out.println(contenido);
     	Pattern pat = Pattern.compile("^[a-zA-Z].*");
      	Matcher mat = pat.matcher(contenido);
     	if(contenido.length()<=5 && edoERROR == false){
     		if(mat.matches()){
     			System.out.println("Va por buen camino en codop");
     			StringTokenizer palCODOP = new StringTokenizer(contenido,".",false); //con false omiti tomar como token el punto
  				System.out.println("Hay un total de: "+palCODOP.countTokens()+" tokens.");
 				int vecespunto;
 				vecespunto = contarPuntos(contenido, '.');
 				System.out.println("vecespunto: " + vecespunto);
 				if(vecespunto == 1 || vecespunto == 0)
  				if((palCODOP.countTokens() == 1) || (palCODOP.countTokens() == 2)){
  					switch (palCODOP.countTokens()){
  						case 1: if((palCODOP.nextToken()).matches("[a-zA-Z]+")){
  									linea_ens.codop = contenido;
  								}
  								else{
  									edoERROR = true;
  									linea_ens.guardarError(archErr,"ERROR el codigo de operacion solo puede contener caracteres alfanumericos y/o un punto",numeroLinea);
  								}
  									
  							break;
  						
  						case 2: if((palCODOP.nextToken()).matches("[a-zA-Z]+")){
  									if((palCODOP.nextToken()).matches("[a-zA-Z]+")){
  										linea_ens.codop = contenido;
  									}
  								}
  								else{
  									edoERROR = true;
  									linea_ens.guardarError(archErr,"ERROR el codigo de operacion solo puede contener caracteres alfanumericos y/o un punto",numeroLinea);
  								}
  									
  							break; 						
  					}
  						
  				}
  				else{
  					edoERROR = true;
  					linea_ens.guardarError(archErr,"ERROR el codigo de operacion solo puede contener letras y/o un punto",numeroLinea);
 				}
 				else {// este es de si hay ms puntos en vecespuntos
 					edoERROR = true;
 					linea_ens.guardarError(archErr,"ERROR el codigo de operacion solo puede contener letras y/o un punto",numeroLinea);	
 				}	
     		}
     		else{
     			edoERROR = true;
     			linea_ens.guardarError(archErr,"ERROR el codigo de operacion solo puede empezar con letras mayusculas o minusculas",numeroLinea);
     		}
     		while (str.hasMoreTokens() && edoERROR == false) {
     			contenido = str.nextToken();
     			linea_ens.validarOperando(contenido,linea_ens);
        		}
     	}
     	else{
     		edoERROR = true;
     		linea_ens.guardarError(archErr,"ERROR Codigo de Operacion invalido",numeroLinea);
     	}
     	return edoERROR;
     }
     
    public int contarPuntos(String cadena, char caracter){
		int veces=0;
		int i;
		for (i=0;i<cadena.length();i++){
			if (cadena.charAt(i)==caracter){
				veces++;
			}
		}
		return veces;
	}
    
     public void validarOperando(String contenido,Linea linea_ens){
     	System.out.println("jajajajajaa");
     	System.out.println(contenido);
     	System.out.println("jajajajajaa");
     	if(contenido.matches("^;.*")){
     		System.out.println(contenido);
     		System.out.println("VAS PARA Comentario");
     		linea_ens.validarComentario(linea_ens);
         }
         else 
         	if(contenido.matches(".+")){
         		System.out.println(contenido);
         		System.out.println("Es un oper");
         		linea_ens.oper = contenido;
         	} 
     }
     
     public void validarComentario(Linea linea_ens){
     	linea_ens = new Linea("NULL","NULL","NULL");
     }
     
     public void guardarError(FileWriter archErr, String error, int numeroLinea){
     	try{
     		archErr.write("Linea "+numeroLinea+": "+error+"\r\n");
     	}
     	catch (Exception e){ //Catch de excepciones
             	System.err.println("Ocurrio un error: " + e.getMessage());
               }
     //	archErr.write("Linea "+numeroLinea+": "+error+"\r\n");	
     }
     
     public void errorEnd(FileWriter archErr, String error){
     	try{
     		archErr.write(error+"\r\n");
     	}
     	catch (Exception e){ //Catch de excepciones
             	System.err.println("Ocurrio un error: " + e.getMessage());
               }
     //	archErr.write("Linea "+numeroLinea+": "+error+"\r\n");	
     }
     
     public boolean AbrirArchivo(File archivo){
     	
 		String nombre_fichero = archivo.getName(); //obtengo el nombre del fichero
 		int pos = nombre_fichero.lastIndexOf('.'); //me posiciono en el punto y obtengo la posicion
 		String extension = nombre_fichero.substring(pos+1); //me posiciono despus del punto
 
     	if(archivo.exists() && extension.equalsIgnoreCase("ASM")){
         	return true;
    		}
    		else
    			return false;
     }
     
     public static void main(String[] args) {
        	Linea linea_ens=new Linea("NULL","NULL","NULL");
        	String ruta = new String();
        	boolean existeono, banderaEnd = false, edoERROR = false;
        	int numeroLinea = 0;
     
     	Scanner teclado=new Scanner(System.in);
     	
     	System.out.println("Etiqueta: " + linea_ens.etq);
     	System.out.print("Escribe la ruta del archivo *.ASM: ");
         ruta = teclado.next();
     	
     	File fichero=new File(ruta);
         existeono = linea_ens.AbrirArchivo(fichero);
         
         File archivoInst=new File((fichero.getName()).substring(0,(fichero.getName()).lastIndexOf('.'))+".INST");
         if (archivoInst.exists()) {
         	archivoInst.delete();
         }
         
         File archivoErrores=new File((fichero.getName()).substring(0,(fichero.getName()).lastIndexOf('.'))+".ERR");
         if (archivoErrores.exists()) {
         	archivoErrores.delete();
         }
         
         
         if(existeono){
         	/*System.out.println("Nombre del archivo "+fichero.getName());
         	System.out.println("Camino             "+fichero.getPath());
         	System.out.println("Camino absoluto    "+fichero.getAbsolutePath());
         	System.out.println("Se puede escribir  "+fichero.canRead());
         	System.out.println("Se puede leer      "+fichero.canWrite());
         	System.out.println("Tamao             "+fichero.length());*/
         	
         	try{
         		RandomAccessFile archivo = new RandomAccessFile(ruta,"rw");
     			archivo.seek(0);
             	
             	String strLinea;
             	
             	//Para archivo Errores
             	FileWriter archErr = new FileWriter(archivoErrores,true);
         		archErr.write("ERRORES---------------\r\n\r\n");
         		
         		//Para archivo INST
             	FileWriter archInst = new FileWriter(archivoInst,true);
         		archInst.write("LINEA\t\tETQ\t\tCODOP\t\tOPER\r\n");
         		archInst.write("--------------------------------------------------------\r\n");
             	
             	// Leer el archivo linea por linea
             	while (archivo.getFilePointer()!=archivo.length() && banderaEnd == false){
             		strLinea=archivo.readLine();
             		
             		numeroLinea++;
             		edoERROR = false;
             		
                 	linea_ens.etq = "NULL";
             		linea_ens.codop = "NULL";
             		linea_ens.oper = "NULL";
             		
             		// Imprimimos la lnea por pantalla
                 	System.out.println (strLinea);
                 	StringTokenizer st = new StringTokenizer(strLinea," \t",false); //con false omiti tomar como token el espacio y el tabulador
  					System.out.println("Hay un total de: "+st.countTokens()+" tokens.");
  					
                 	/*while (st.hasMoreTokens()) {
                 		System.out.println(st.nextToken()); 
                 	}*/
 
         
                 	//Veo con que empieza la linea para ir a los metodos
                 	Pattern pat = Pattern.compile("^;.*"); //Espresion regular
      				Matcher mat = pat.matcher(strLinea); //Coincidencia a partir del patron
      			  if(st.countTokens() != 0){	
                 	if(mat.matches()){
                 		//La linea es un comentario
                 		System.out.println ("Empezo conCOMENTARIO");
                 		linea_ens.validarComentario(linea_ens);		
                 	}
                 	else{
                 		pat = Pattern.compile("^[a-zA-Z].*");
      					mat = pat.matcher(strLinea);
                 		if(mat.matches()){
                 			//Empezo con ETIQUETA  C:\Users\PCX\J_Repositorio\Practica_1\Archivo.ASM
                 			
                 			System.out.println ("Empezo con ETIQUETA");
                 			if (st.countTokens()== 1){
                 				edoERROR = true;
                 				linea_ens.guardarError(archErr,"ERROR la linea solo contiene etiqueta",numeroLinea);
                 			}
                 			else
                 				edoERROR = linea_ens.validarEtiqueta(st,linea_ens,edoERROR,archErr,numeroLinea);
                 			
                 			System.out.println(linea_ens.etq +" " + " " + linea_ens.codop);
                 			
                 		}
                 		else{
                 			pat = Pattern.compile("^[\t ].*"); //Espresion regular
      						mat = pat.matcher(strLinea); //Coincidencia a partir del patron
      						String contenido = new String (st.nextToken());
                 			if(mat.matches() && contenido.matches("^[a-zA-Z].*")){
                 				//Empezo con CODOP
                 				System.out.println ("Empezo con  CODOP");
                 				edoERROR = linea_ens.validarCodigo(st,contenido,linea_ens,edoERROR,archErr,numeroLinea);	
                 			}
                 			else{
                 				edoERROR = true;
     							linea_ens.guardarError(archErr,"ERROR la Etiqueta debe empezar con letra",numeroLinea);
                 			}
                 				
                 		}			
                 	}
                 	
                 	System.out.println("jajaaaa\n"+edoERROR+"jajajaaj\n\n");
                 	if((linea_ens.codop).equalsIgnoreCase("END")){
                 		banderaEnd = true;
                 	}
                   }
                 	//C:\Users\PCX\J_Repositorio\Practica_1\Archivo.ASM
                 	if ((linea_ens.etq.equals("NULL") && linea_ens.codop.equals("NULL") && linea_ens.oper.equals("NULL")) || banderaEnd == true || edoERROR == true){
                 		//no imp
                 	}
                 	else{
                 		
                 		System.out.println("Entro a escribir");
                 		/*System.out.println();
                 		System.out.println(numeroLinea + " " + linea_ens.etq +" " + " " + linea_ens.codop +" " + " " + linea_ens.oper );
                 		System.out.println();*/
                 		archInst.write(numeroLinea+"\t\t"+linea_ens.etq+"\t\t"+linea_ens.codop+"\t\t"+linea_ens.oper+"\r\n");
                 	}
             	  }		
                 
                 if (banderaEnd == false){
                 	edoERROR = true;
                 	linea_ens.errorEnd(archErr,"ERROR no se encuentra la directiva END");
                 }
            		// Cerrar archivos
             	archivo.close();
             	archErr.close();
             	archInst.close();
             }catch (Exception e){ //Catch de excepciones
             	System.err.println("Ocurrio un error: " + e.getMessage());
               }
         }
         else{
         	System.out.println("\nEl archivo no existe o tiene la extension incorrecta, VERIFIQUELO...");
         } 
         
         
     }
 }
