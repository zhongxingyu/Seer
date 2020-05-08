 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pkgfinal;
 import java.util.Stack;
 import java.io.*;
 import java.util.Scanner;
 /**
  *
  * @author root
  */
 public class Evaluador {
     
     Stack<Character> p = new Stack();
     String archivo;
     
     /**
      * Método encargado de hacer el trabajo duro, encontrar la posición en donde 
      * un paréntesis,corchete, llave, cierre o abra mal. Utilizando una pila o stack.
      * La idea es seguir el algoritmo ya conocido de ir insertando cada vez que se encuentre
      * algo que abra y cuando se encuentre algo que cierre sacar de la pila su par, si no es 
      * así simplemente marcas un error.
      * @param k Cadena a evaluar
      * @param nombre Nombre de quien quieres evaluar
      * @param nick El nick del usuario que desea evaluar
      * @param pow Si es admin o no
      */
    public void parentesis(String k,String nombre,String nick,String pow){
        int size = k.length();
        int position = 0;
        Character c = '*';
        Boolean ev = true;
       
         
        for(int i = 0;i<size;i++){ // Leemos cada caractere de la cadena "k" 
        position = i;            // Seteamos la posición en la que vamos recorriendo
            c = k.charAt(i);     
            if(c == '('){        // si es un paréntesis que abre
         p.push(c); }            
            else if(c == '['){  // si es un corchete que abre
                 p.push(c);
            }
            else if(c == '{'){ // si es una llave que abre
                 p.push(c);
            }
            
            // S.P.G empezamos con el paréntesis!
            
            else if(c == ')'){  // Primer caso cuando se encuentre un paréntesis que cierre
                
                if(p.isEmpty()){  
                    if(pow.compareTo("admin") == 0){
                    despliega(nombre,nick,k,i,c,false); System.exit(0);
                    } else { log("Mal balanceado con parentesis"); System.exit(1);
                    }
                } 
                if(p.peek() == '('){
                    p.pop();
                   // System.out.println("Un parentesis bien balacenado");
                    
                } else { System.out.println("Parentesis mal balanceado"); 
                ev = false;
                         if(pow.compareTo("admin") == 0){
                                               despliega(nombre,nick,k,i,c,false); System.exit(0);
                         } }
                
                
                
            }
            else if(c == ']'){   // Segundo caso cuando se espera que sea un corchete que cierre
            
               if(p.isEmpty()){ 
                    if(pow.compareTo("admin") == 0){
                   despliega(nombre,nick,k,i,c,false); System.exit(0);
               
               } else { log("Mal balanceado con corchetes"); System.exit(1); } 
            }
                if(p.peek() == '['){
                    p.pop();
                    //System.out.println("Un corchete bien balacenado");
                } else { System.out.println("Corchete mal balanceado");   
                ev = false;
                if(pow.compareTo("admin") == 0){
                                              despliega(nombre,nick,k,i,c,false); System.exit(0);} }
            }
            
            
            else if(c == '}'){   // Tercer y último caso cuando se espera que sea una llave que cierre
                
                
              if(p.isEmpty()){ 
                                     
                  if(pow.compareTo("admin") == 0){
                  despliega(nombre,nick,k,i,c,false); System.exit(0);
 
              }  else { log("Mal balanceado con llaves"); System.exit(0);
                  }
              }
             
             
                if(p.peek() == '{'){
                    p.pop();
                    //System.out.println("Una llave bien balacenado");
                } else { System.out.println("Llave mal balanceado");   
                ev = false;
                if(pow.compareTo("admin") == 0){
                                              despliega(nombre,nick,k,i,c,false); System.exit(0);} }
            }
            else { System.out.println("Mal mal eso no es una cadena valida");  despliega(nombre,nick,k,i,c,false); System.exit(0);}
            
            
         
        }
        
  
        
        // Verificamos si la pila se encuentra vacia o no [Qué está por demás]
        if(p.isEmpty()){
             if(pow.compareTo("admin") == 0){ // La pila está vacía pero necesitamos saber si es o no admin 
            despliega(nombre,nick,k,ev); // Es un admin y vamoss al metodo cuando la pila está vacia 
            
             } else {  
         // Aquí entro por que no es admin y no tenemos que escribir ningún archivo        
         String h = (p.isEmpty())? "Parentesis bien balanceados" : ""; //más vale volver a checar :D
        System.out.println(h); System.exit(1);  
             }
       } else { log("Mal balanceada la cadena");
       
       int tons = pow.compareTo("admin");
       
       switch(tons){
           case 0 :
                    despliega(nombre,nick,k,position,c,false);  
               break;
       }
       
       
        }
        
    
    }
    
    
    
    
 /**
  * Método encargado de escribir en un archivo .txt el reporte de lo sucedido durante 
  * la evaluacion, además de que este método es solo para admin's
  * @param name Nombre del usuario que envio la evaluación
  * @param nick Nick del usuario que envio la evaluación
  * @param bal Expresión que se deseó evaluar
  * @param pos Posición en la que se encontro error si es que lo hubo.
  * @param p El caracter que causo el problema
  * @param f boolean de si fallo o no. 
  */       
    public void despliega(String name,String nick,String bal,int pos,Character p,Boolean f){
         String archivo = "salida"+name+".txt";
    File file = new File("salida"+name+".txt");
  
 		try {
  
 			if (file.exists()) {
  
 				file.createNewFile();
 			}
  
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			writer.write(name+":"+nick +"\n");
                         writer.write("Expresion: "+bal+"\n");
                         
                           writer.write("Expresion Apareada: " +f+"\n");    
                         
                         
                         if(!f){ // 
                                    
                             writer.write("Posicion: "+ pos+"\n");
                             if(p == ')'){
                             writer.write("motivo: " + "Se encontro un "+p+ " y no cerro bien se esperaba un " +'(');
                         }
                             else if(p == ']'){
                             writer.write("motivo: " + "Se encontro un "+p+ " y no cerro bien se esperaba un " +'[');
 
                             }
                              else if(p == '}'){
                             writer.write("motivo: " + "Se encontro un "+p+ " y no cerro bien se esperaba un " +'{');
 
                             }
                              else if(p == '('){
                             writer.write("motivo: " + "Se encontro un "+p+ " y no cerro bien se esperaba un " +')');
 
                             }
                              else if(p == '['){
                             writer.write("motivo: " + "Se encontro un "+p+ " y no cerro bien se esperaba un " +']');
 
                             }
                             else if(p == '{'){
                             writer.write("motivo: " + "Se encontro un "+p+ " y no cerro bien se esperaba un " +'}');
 
                             }
                         }
                             
 			writer.close();
 			writer = null;
                      
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
                 show(archivo);  System.exit(1);  
    } // end despliga() - para admin's que la regaron en algo o no!
    
    
    
    
    
     public void despliega(String name,String nick,String bal,Boolean f){
        archivo = "salida"+name+".txt";
    File file = new File(archivo);
  
 		try {
  
 			if (file.exists()) {
  
 				file.createNewFile();
 			}
  
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			writer.write(name+":"+nick+"\n");
                         writer.write("Expresion: "+bal+"\n");
                         writer.write("Expresion Apareada: "+f+"\n");                      
 			writer.close();
 			writer = null;
                 } catch (IOException e) {
 			e.printStackTrace();
 		}
                 show(archivo); System.exit(1);
    } // end despliega() - para admin's que no la regaron con la evaluación
 
     
     
 void show(String f){
     log("#######################################");
     log("Mostrando el archivo "+"\""+f+"\""+"\n");
         try {
             
           //StringBuilder text = new StringBuilder();
           String sp = System.getProperty("line.separator");
              
             /*for(int i = 0; i< System.getProperties().stringPropertyNames().toArray().length;i++){
                 log((String) System.getProperties().stringPropertyNames().toArray()[i]);
             }*/
           Scanner showsc = new Scanner(new FileInputStream(f),"ISO-8859-1");
          
           try {
               while(showsc.hasNextLine()){
               //text.append(showsc.nextLine() + sp);
                   log(showsc.nextLine() + sp);
               }
           }
           finally{
               showsc.close();
           }          
         } 
         catch (FileNotFoundException ex) {
             log("Murio! no se encontro "+f );
         }
         
 } // End of show()    
 
 private void log(String msg){
     System.out.println(msg);
 } // end of log()
 
 }    
      
        
        
        
     
     
 
