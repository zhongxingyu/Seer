 package compilationunit;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.StringTokenizer;
 
 public class GenFinal {
     BufferedWriter bw;
     File archiEscri=null;
     String temporal;
     String operacion,op1,op2,op3;
     String etiquetasputs="";
     int num_param_actual = 0;
     int c_etiqueta;
     LinkedList<String> lista_data = new LinkedList();
     int lista_ini=12000;	// comienzo en memoria de la lista_data
     int count_char=lista_ini;	// Numero de characters emitidos en lista data
     int true_false = lista_ini - 200;	// En esta direccion se guarda cadena "true" y "false" y valor 1 y 0 y mas constantes
 	String nemonico = new String();		
     
 
 
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
         bw.write("MOVE .A, .SP\n"); //actualizamos SP
         bw.write("PUSH .IX\n");  	//guardamos el IX para saber donde empiezan los atributos de la tabla de simbolos padre
         
         //Vamos a buscar el main para que el PC
         //Si el analisis semantico ha validado el codigo, dentro del ambito global deberia estar el objeto main
         simbolo = tabla_aux.GetSimbolo("main");
         String etiqueta_main;
         etiqueta_main = simbolo.GetEtiqueta();
         bw.write("CALL /" + etiqueta_main + " ; VAMOS AL MAIN\n");
         bw.write("POP .IX ; Recuperamos el marco de pila\n");
         bw.write("MOVE .IX, .SP\n");
         bw.write("HALT ;Cuando se vuelva del Main se terminara la ejecucion\n");
                 
         /*
          * Bucle para imprimir toda la cola de tercetos!
          */
         System.out.println("-----------------------------------");
         //System.out.println("Tamano de la lista:"+colaTercetos.size());
         Iterator<tupla_Tercetos> it = colaTercetos.iterator();
         tupla_Tercetos tupla_temp;
         while (it.hasNext()) {
             //this.separar(it.next().GetTerceto());
         	tupla_actual = it.next();
             System.out.println("Terceto: "+tupla_actual.GetTerceto());
             //System.out.println("Ambito_actual: "+it.next().GetAmbitoActual());
             ProcesarTerceto(tupla_actual, tabla);
         }
         System.out.println("-----------------------------------");
         
         /*
          * Ponemos un HALT, si se acaban los tercetos es final de MAIN
          * Nota:podemos hacer un RET pero ahorramos problemas ocn un HALT
          */
         bw.write("MOVE .IX, .SP\n");		// Devuelvo la pila SP al commienzo 
         bw.write("RET; final de main\n");
 
         /*
          * Almacenamos constantes, sí o sí
          */
         bw.write("ORG "+true_false+"\n");
         bw.write("cad_cierto: DATA \"true\"\n");
         bw.write("cad_falso: DATA \"false\"\n");
         bw.write("v_cierto: DATA 1\n");
         bw.write("v_falso: DATA 0\n");
         bw.write("salto_lin: DATA \"\\n\"\n");
 
         /*
          * Tenemos en "lista_data" las posibles cadenas que se guardan a partir de una dir de memoria 
          */
         if (!lista_data.isEmpty()) {
         	Iterator<String> iterador = lista_data.iterator();
         	bw.write("\nORG "+lista_ini+"\n");	// A partir de aqui las cadenas
         	while (iterador.hasNext()) {
         		bw.write(iterador.next());
         	}
         } // else No hay ninguna cadena en el codigo
 
         // Importante! sino no se guarda nada en el fichero!
         bw.close();
     }
        
     catch (IOException e)
     	{
     	System.out.println("Tranquilo vaquero");
     	}
     }
 
 
 private void ProcesarTerceto (tupla_Tercetos tupla_actual, Tablas tabla) {	
 	// Obtenemos los dos valores de la tupla
 	String terceto_actual= tupla_actual.GetTerceto();	// Almacenara el String emitido por el GCI
 	TablaSimbolos ambitoterceto = tupla_actual.GetAmbitoActual();

 	
 	// Separamos los operando del terceto. operador, op1, op2...
 	this.separar(terceto_actual);
 
 	if (operacion.equals("ASIGNACION")) {				// caso de asignar un entero a algo
     	EjecutarAsignacion(op1, op2, ambitoterceto);	// paso el destino(op1) y el valor(op2)
 	} else if (operacion.equals("ETIQUETA_SUBPROGRAMA")) {
 		ComienzoSubprograma(op1, ambitoterceto);		// op1: nombre de la etiqueta
 	} else if (operacion.equals("ASIGNACION_CADENA")) {	// ETI: data "HOLA"
 		EjecutarAsignaCad(op1, op2, ambitoterceto);
 	} else if (operacion.equals("ASIGNA")){				// asignamos a un temp el valor de otro tmp
 		EjecutarAsigna(op1, op2, ambitoterceto);
 	} else if (operacion.equals("METE_EN_ARRAY")) {		// Asignar valor en posicion del vector
 		// TODO
 		// METE_EN_ARRAY,caracola,temporal2,temporal1
 		AsignaValorVector(ambitoterceto);				// pe: v[2]=23
 	} else if (operacion.equals("SUMA")) {		// Suma
 		nemonico = "ADD";
 		OpBinaria(ambitoterceto);
 	} else if (operacion.equals("RESTA")) {		// Restamos
 		nemonico = "SUB";
 		OpBinaria(ambitoterceto);
 	} else if (operacion.equals("MUL")) {		// Multiplamos
 		nemonico = "MUL";
 		OpBinaria(ambitoterceto);
 	} else if (operacion.equals("DIV")) {		// División
 		nemonico = "DIV";
 		OpBinaria(ambitoterceto);
 	} else if (operacion.equals("OR")) {		// OR lógico
 		nemonico = "OR";
 		OpBinaria(ambitoterceto);
 	} else if (operacion.equals("AND")) {		// AND lógico
 		nemonico = "AND";
 		OpBinaria(ambitoterceto);
 	} else if (operacion.equals("NEG_LOG")) {	// NOT lógico
 		nemonico = "XOR";
 		OpUnaria(ambitoterceto);	// opUnaria op2=1
 	} else if (operacion.equals("READ")) {		// CIN
 		nemonico = "ININT";
 		GetEntero(ambitoterceto);
 	} else if (operacion.equals("PUT_BOOLEANO")) {	// PRINT Boolean
 		nemonico="WRSTR";
 		PutBool(ambitoterceto);
 	} else if (operacion.equals("PUT_CADENA")) {	// PRINT CADENA
 		nemonico="WRSTR";
 		PutCadena(ambitoterceto);
 	} else if (operacion.equals("PUT_ENTERO")) {	// PRINT ENTERO
 		nemonico="WRINT";
 		PutEntero(ambitoterceto);
 	} else if (operacion.equals("PUT_SALTO_LINEA")) {// PRINT SALTO_LINEA
 		// No es una instruccion al uso. Solo en cada cout se emite esto
 		try { bw.write("WRSTR /salto_lin\n"); }	// etiqueta ya guardada! 
 		catch (IOException e) 
 			{ System.err.println("Error: SaltoLinea"); }
 	// } else if () {PUT_BOOLEANO
 	} else {
 		System.err.println("Operacion Terceto no contemplado->"+tupla_actual.GetTerceto());
 	}
 
 }
 
 //***********************************************************************************************
 
 
 /*
  * AsignaValorVector
  * Asignamos un valor a una posicion-indice del vector
  * uso de .R9 y .R8
  */
 private void AsignaValorVector (TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_vector = ambito_terceto.GetSimbolo(op1);	// Simbolo del Vector
 		Simbolo simbolo_valor = ambito_terceto.GetSimbolo(op2);	// Simbolo del valor a meter en el vector
 		Simbolo simbolo_indice= ambito_terceto.GetSimbolo(op3);	// Simbolo del indice del vector
 		TablaSimbolos tabla_op_lejano = null;
 		
 		// TODO : Comprobrar que el indice no es mayor-menor al tamaño del vector
 		// TODO : Seguir haciendo cositas del if faltan casos!!
 
 		// Tenemos tres simbolos a dos posibilidades cada uno de estar o no en el ambito local -> 2 * 2 * 2 = 8 posibilidades
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2) && ambito_terceto.Esta(op3)) {	// todo local!
 			bw.write("MOVE #-"+simbolo_indice.GetDesplazamiento()+"[.IX],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+simbolo_vector.GetDesplazamiento()+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+simbolo_valor.GetDesplazamiento()+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if ((!ambito_terceto.Esta(op1)) && ambito_terceto.Esta(op2) && ambito_terceto.Esta(op3))	{	//solo op1 No local
 			bw.write("MOVE #-"+simbolo_indice.GetDesplazamiento()+"[.IX],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write("ADD #"+despl_op1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IY, .A\n");	// BuscaMarcoDir ha dejado en IY la direccion del marco del vector
 			bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+simbolo_valor.GetDesplazamiento()+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if (ambito_terceto.Esta(op1) && (!ambito_terceto.Esta(op2)) && ambito_terceto.Esta(op3))	{	//solo op2 No local
 			bw.write("MOVE #-"+simbolo_indice.GetDesplazamiento()+"[.IX],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+simbolo_vector.GetDesplazamiento()+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_op2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2) && (!ambito_terceto.Esta(op3)))	{	//solo op3 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_indice = tabla_op_lejano.GetSimbolo(op3).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_indice+"[.IY],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+simbolo_vector.GetDesplazamiento()+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+simbolo_valor.GetDesplazamiento()+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if ((!ambito_terceto.Esta(op1)) && (!ambito_terceto.Esta(op2)) && ambito_terceto.Esta(op3))	{	//op1 y op2 No local
 			bw.write("MOVE #-"+simbolo_indice.GetDesplazamiento()+"[.IX],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			bw.write("MOVE .IY, .R8\n");	// contenido que deja la función a R7
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write("ADD #"+despl_op1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .R8, .A\n");	// BuscaMarcoDir ha dejado en IY la direccion del marco del vector
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector
 			// A BUSCAR OP2
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_op2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else if ((!ambito_terceto.Esta(op1)) && ambito_terceto.Esta(op2) && (!ambito_terceto.Esta(op3)))	{	//op1 y op3 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_indice = tabla_op_lejano.GetSimbolo(op3).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_indice+"[.IY],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write("ADD #"+despl_op1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IY, .A\n");
 			bw.write("MOVE .A, .IY\n");	// R8 = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+simbolo_valor.GetDesplazamiento()+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if (ambito_terceto.Esta(op1) && (!ambito_terceto.Esta(op2)) && (!ambito_terceto.Esta(op3)))	{	//op2 y op3 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_indice = tabla_op_lejano.GetSimbolo(op3).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_indice+"[.IY],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+simbolo_vector.GetDesplazamiento()+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_op2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else if (!ambito_terceto.Esta(op1) && (!ambito_terceto.Esta(op2)) && (!ambito_terceto.Esta(op3))) {	// NADA LOCAL!
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_indice = tabla_op_lejano.GetSimbolo(op3).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_indice+"[.IY],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write("ADD #"+despl_op1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IY, .A\n");
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector			
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_op2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else {
 			System.err.println("Ejecutar Asigna. Caso no contemplado");
 		}
 		
 		
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar AsignaValorVector.");
 	}
 }
 
 /*
  * GetEntero
  * Captura por consola una ristra de caracteres que luego convertira a entero y colocara en op1
  */
 private void GetEntero (TablaSimbolos ambito_terceto) {
 	try {
 		// recuperamos el simbolo a imprimir
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		TablaSimbolos tabla_op_lejano = null;	// En caso de ser variable local.
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			bw.write(nemonico+" #-"+simbolo_op1.GetDesplazamiento() + "[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write(nemonico + "#-"+despl_op1+"[.IY]\n");	
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar PutEntero.");
 	}
 }
 
 /*
  * PutEntero
  * Imprime por pantalla un valor entero
  */
 private void PutEntero (TablaSimbolos ambito_terceto) {
 	try {
 		// recuperamos el simbolo a imprimir
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		TablaSimbolos tabla_op_lejano = null;	// En caso de ser variable local.
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			bw.write(nemonico + "#-"+simbolo_op1.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write(nemonico + "#-"+despl_op1+"[.IY]\n");		
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar PutEntero.");
 	}
 }
 
 /*
  * PutCadena
  * Imprime por pantalla el valor de una cadena que previamente se almaceno en un espacio de memoria
  * y del q se sabe la direccion de comienzo, almacenada en el ambito (local-padre...)
  * Se usa .R9
  */
 private void PutCadena (TablaSimbolos ambito_terceto) {
 	try {
 		// recuperamos el simbolo a imprimir
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		TablaSimbolos tabla_op_lejano = null;	// En caso de ser variable local.
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			bw.write("MOVE #-"+simbolo_op1.GetDesplazamiento()+"[.IX],.R9\n");
 			bw.write(nemonico + " [.R9]\n");	// imprime a partir de la etiqueta
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write("MOVE #-"+despl_op1+"[.IY],.R9\n");
 			bw.write(nemonico + " [.R9]\n");	// imprime a partir de la etiqueta
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar PutCadena.");
 	}
 }
 
 /*
  * PutBool
  * Imprime por pantalla la secuencia de String: true o false. Dependiendo del valor op1
  */
 private void PutBool (TablaSimbolos ambito_terceto) {
 	try {
 		// recuperamos el simbolo a imprimir
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		TablaSimbolos tabla_op_lejano = null;	// En caso de ser variable local.
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			bw.write("CMP #-"+simbolo_op1.GetDesplazamiento()+"[.IX], /v_cierto\n");
 			bw.write("BZ $3\n");	// Es cierto? Sí -> salto!
 			bw.write(nemonico + " /cad_falso\n");	// imprime-> "false"
 			bw.write("BR $2\n");
 			bw.write(nemonico + " /cad_cierto\n");	// imprime-> "cierto"
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			// Imprimimos lacadena q representa a dicho valor
 			bw.write("CMP #-" + despl_op1 + "[.IY], /v_cierto\n");
 			bw.write("BZ $4\n");	// Es cierto? Sí -> salto!
 			bw.write(nemonico + " /cad_falso\n");	// imprime-> "false"
 			bw.write("BR $2\n");
 			bw.write(nemonico + " /cad_cierto\n");	// imprime-> "cierto"
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar PutBool.");
 	}
 }
 
 /*
  * OpUnaria
  * Aplico al op1 la operacion pasada en el Nemonico, dejando el resultado en op3
  * pe: NEG_LOG, var_boolean, resultado
  */
 private void OpUnaria (TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		// Recordamos que op2 en OpUnaria es siempre 1.
 		Simbolo simbolo_resultado = ambito_terceto.GetSimbolo(op3);	// siempre local (temp)
 		TablaSimbolos tabla_op_lejano = null;
 		// Recuerda q "nemonico" fue ya asignado en la llamada a esta funcion
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			bw.write(nemonico+" #-"+simbolo_op1.GetDesplazamiento()+"[.IX], #"+op2+"\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			// La operacion unaria se queda en el acumulador, luego la llevamos a la dir de mem
 			bw.write(nemonico+" #-"+simbolo_op1.GetDesplazamiento()+"[.IX], #"+op2+"\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar OpUnaria.");
 	}
 }
 
 /*
  * Operacion Binaria
  * OpMUL(Operancion, op1, op2, resultado, ambitoterceto);
  */
 private void OpBinaria (TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		Simbolo simbolo_op2 = ambito_terceto.GetSimbolo(op2);
 		Simbolo simbolo_resultado = ambito_terceto.GetSimbolo(op3);	// siempre local (temp)
 		TablaSimbolos tabla_op_lejano = null;
 		// Recuerda q "nemonico" fue ya asignado en la llamada a esta funcion
 		
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {			// todo local!
 			bw.write(nemonico+" #-"+simbolo_op1.GetDesplazamiento()+"[.IX], #-"+simbolo_op2.GetDesplazamiento()+"[.IX]\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
 			bw.write(nemonico+" #-"+despl_op1+"[.IY], #-"+simbolo_op2.GetDesplazamiento()+"[.IX]\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { 	//op2 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
 			bw.write(nemonico+" #-"+despl_op2+"[.IY], #-"+simbolo_op1.GetDesplazamiento()+"[.IX]\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { 	//NADA local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);	// Nos deja en IY la dir del marco
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			// Puesto q op2 tambien usara esta func. muevo el IY a otro reg
 			bw.write("MOVE .IY, .R9\n");	// DIR del MARCO de OP1 en R9!!!!
 			bw.write("ADD #-"+despl_op1+",.R9\n");	//Dejo en R9 la direccion exacta del dato
 			bw.write("MOVE .A, .R9\n");
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
 			bw.write(nemonico+" #-"+despl_op2+"[.IY], [.R9]\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar OpBinaria.");
     }
 }
 
 /* 
  * Asignar temporal cadena
  * 1- Anadimos a una cola de cadenas otro dato que sera guardado a partir de una direccion de mem. accesible
  * por la etiqueta dada. pe: temporal20: DATA "HOLA"
  * 2- Apilamos la direccion a partir de la cual empieza la cadena.
  * 
  */
 private void EjecutarAsignaCad (String op1, String op2, TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		// 1- Anadimos a la lista de DATA esta etiqueta con su valor
 		lista_data.add(simbolo_op1.GetNombre()+": DATA "+ op2 + "\n");
 		// Elimino las comillas que envuelven al string
 		op2=op2.substring(1, op2.length()-1);
 		// 2- Guardo la direccion a la cadena en el marco de pila actual
 		bw.write("MOVE #"+ count_char +",#-" + simbolo_op1.GetDesplazamiento() + "[.IX]\n");
 		// Cuento el numero de elem del string para mover el desplazamiento
 	    // Texto que vamos a buscar
 	    String sTextoBuscado = "\\n";	// solo ocupa un espacio pero son 2 char
 	    // Contador de ocurrencias 
 	    int contador = 0;	// Numero de veces que aparece la cadena
 	    while (op2.indexOf(sTextoBuscado) > -1) {
 	      op2 = op2.substring(op2.indexOf(sTextoBuscado)+sTextoBuscado.length(),op2.length());
 	      contador++;
 	    }
 		// Ajustamos le desplazamiento teniendo en cuenta todo
 	    if ((op2.length()==0) && (contador!=0)) {		// caso "\n"
 			count_char= count_char + contador + 1;
 	    } else {
 			count_char= count_char + op2.length() + contador + 1;	
 	    }
 		// prueba impresion
 		//bw.write("MOVE #-" + simbolo_op1.GetDesplazamiento() + "[.IX], .IY\n");
 		//bw.write("WRSTR [.IY]\n");
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar AsignaCadena.");		
     }
 }
 
 /*
  * Asignamos el valor de op2 a op1 
  * op1 y op2 pueden ser o no variables locales
  */
 private void EjecutarAsigna (String op1, String op2, TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		Simbolo simbolo_op2 = ambito_terceto.GetSimbolo(op2);
 		TablaSimbolos tabla_op_lejano = null;
 
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {	// todo local!
 			// Caso todo en LOCAL - MOVE #-op2.desp[.IX], #-op1.desp[.IX]
 			bw.write("MOVE #-" + simbolo_op2.GetDesplazamiento() + "[.IX], #-"+simbolo_op1.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			// Pongo el valor local en el hueco ajeno
 			bw.write("MOVE #-"+ simbolo_op2.GetDesplazamiento() +"[.IX], #-"+ despl_op1+"[.IY]\n");
 		} else if (ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { //op2 No local			
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			// Pongo el valor ajeno en el hueco local
 			bw.write("MOVE #-"+ despl_op2 +"[.IY], #-"+simbolo_op1.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) {	// NADA LOCAL!
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);	// Nos deja en IY la dir del marco
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			// Puesto q op2 tambien usara esta func. muevo el IY a otro reg
 			bw.write("MOVE .IY, .R9\n");	// DIR del MARCO de OP1 en R9!!!!
 			bw.write("ADD #-"+despl_op1+",.R9\n");	//Dejo en R9 la direccion exacta del dato
 			bw.write("MOVE .A, .R9\n");
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			int despl_op2 = tabla_op_lejano.GetSimbolo(op2).GetDesplazamiento();
 			// Pongo el valor local en el hueco ajeno
 			bw.write("MOVE #-"+ despl_op2 +"[.IY], [.R9]\n");	// RECUERDA R9!!
 		} else {
 			System.err.println("Ejecutar Asigna. Caso no contemplado");
 		}
 
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar Asigna.");		
     }
 }
 
 /*
  * Ejecutar Asignacion es para casos donde el valor a asignar sea un ENTERO!
  * Luego guardo a partir del IX el valor de dicho elemento
  */
 private void EjecutarAsignacion(String op1, String op2, TablaSimbolos ambito_terceto)	{
 	try {
 		Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		bw.write("MOVE #"+op2+",#-" + simbolo_op1.GetDesplazamiento() + "[.IX]\n");
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar Asignacion.");		
     }
 }
 
 /*
  *	Crear el nuevo marco de pila, añade la etiqueta al codigo ensamblador 
  */
 private void ComienzoSubprograma (String subprograma, TablaSimbolos ambito_terceto) {
 	try {
 		// Recuperamos el desplazamiento para le Marco de pila
 		int despl_local=ambito_terceto.GetDesplazamiento();
 		// Escribimos la etiqueta
 		bw.write(subprograma.toLowerCase() +":\n");		// tiene q ser en minusculas!!
 		bw.write("MOVE .SP, .IX\n");					// Base del marco de pila
 		bw.write("ADD #-" + despl_local + ", .SP\n");	// Techo del Marco de pila
 		bw.write("MOVE .A, .SP\n");
 	} catch (Exception e) {
 		System.err.println("Error: Comienzo Subprograma.");
 	}
 }
 
 /*
  * Busca la direccion de un elemento-simbolo(String) que no este en el ambito dado
  * como parametro. Ademas va escribiendo en la salida del fichero el codigo necesario
  * para dejar en IY el valor del MARCO DE PILA del ambito donde esta declarado
  */
 private TablaSimbolos BuscaMarcoDir (String Nombre, TablaSimbolos ambito_terceto) {
 	try {
 		bw.write("MOVE .IX, .IY\n");	// Nos moveremos sobre este registro
 		TablaSimbolos ambito_simbolo = ambito_terceto;	// Variable para moverme por tablas
 		// op1 SI LOCAL. op2 NO LOCAL
		
 		while (!ambito_simbolo.Esta(Nombre)) {
			System.out.println("Simbolo con nombre:"+Nombre);
 			bw.write("MOVE #2[.IY],.IY\n");
			ambito_simbolo = ambito_simbolo.Ambito_Padre();	// esta en el padre?
 		}
 		return ambito_simbolo;
 	} catch (Exception e) {
 		System.err.println("Error: Buscar Direccion simbolo error.");
 		return null;	// si va mal, va mal!
 	}
 }
 
 /*
  * Operacion q dado un terceto-> ASIGNACION, temp0, 10-> separa cada uno en un operando global
  */
 private void separar(String linea)	{
     int u= linea.indexOf(",");
     this.operacion=linea.substring(0,u); //cogemos la operación
     linea=linea.substring(u+1);
     
     u= linea.indexOf(",");
     op1=linea.substring(0,u);
     linea=linea.substring(u+1);
 
     u= linea.indexOf(",");
     op2=linea.substring(0,u);
     linea=linea.substring(u+1);
 
     op3=linea.substring(0,linea.indexOf("\n"));
 }
 
 private void daInformacion (String operando, TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolito = ambito_terceto.GetSimbolo(operando);
 		System.out.println("-> Dando informacion acerca del operando: "+operando);
 		System.out.println("Nombre: "+simbolito.GetNombre());
 		System.out.println("Etiqueta: "+simbolito.GetEtiqueta());
 	} catch (Exception e) {
		System.err.println("Fallo en impresion de informacion de un operando.");
 	}
 }
     
 }
