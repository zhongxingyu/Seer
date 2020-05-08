 package compilationunit;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 
 public class GenFinal {
     BufferedWriter bw;
     File archiEscri=null;
     String temporal;
     String operacion,op1,op2,op3;
     String etiquetasputs="";
     int num_param_actual = 0;
     int c_etiqueta;
 
 
 public GenFinal(LinkedList<tupla_Tercetos> colaTercetos, Tablas tabla, String fichero) {
     
     int desp_total;  //variable para el desplazamiento total de las tablas de simbolos
     archiEscri= new File(fichero);
     tupla_Tercetos tupla_actual;
     String terceto_actual;
     TablaSimbolos ambito_actual;
     //cola para ir metiendo los metodos a los que se llama
     LinkedList<String> colaMetodos = new LinkedList<String> (); 
     Simbolo simbolo;
     TablaSimbolos tabla_aux;
     c_etiqueta = 0;
     
     System.out.println("Comienza la fase de generacion de codigo objeto");
     //preparamos el fichero que contendra el codigo objeto
     try
     	{
         bw= new BufferedWriter(new FileWriter(fichero));
     	}
     catch (IOException e) 
     	{
         System.out.println("Error fichero de salida para Codigo Objeto.");
     	}
     
 
     //inicializamos el codigo objeto y lo dejamos todo preparado para leer los
     //tercetos del main
     try {
         bw.write("ORG 0\n");
         // Inicializamos la pila al maximo puesto que es decreciente
         // y la guardamos en el puntero de pila
         bw.write ("MOVE #65535, .SP\n");
         bw.write ("MOVE .SP, .IX\n");
         
         /* creamos el RA de la clase que contiene el metodo principal, dejando
          * hueco para todos sus atributos, despues guardamos el IX, que apuntará
          * al primer atributo de la clase que contiene el metodo main
          * para luego poder acceder cogiendo el desplazamiento de la tabla
          * de simbolos */
         tabla_aux = tabla.GetAmbitoGlobal();  //buscamos la tabla de la clase del metodo principal
         desp_total = tabla_aux.GetDesplazamiento(); //cogemos el desp de la tabla de simbolos global
         bw.write ("ADD #-" + desp_total + ", .SP\n"); //sumamos desp_total de la tabla de simbolos padre al SP
         System.out.println("guarrilla");
         bw.write("MOVE .A, .SP\n"); //actualizamos SP
         bw.write("PUSH .IX\n");  //guardamos el IX para saber donde empiezan los atributos de la tabla de simbolos padre
         bw.write ("MOVE .SP, .IX\n");  //actualizamos IX
         
         //Vamos a buscar el main para que el PC
         //Si el analisis semantico ha validado el codigo, dentro del ambito global deberia estar el objeto main
         simbolo = tabla_aux.GetSimbolo("main");
         String etiqueta_main;
         etiqueta_main = simbolo.GetEtiqueta();
         bw.write("CALL /" + etiqueta_main);
         bw.close();
     }
        
     catch (IOException e)
     	{
     	System.out.println("Tranquilo vaquero");
     	}
         
         
         
         
  
     }
 
 }
