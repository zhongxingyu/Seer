 package compilationunit;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 
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
     TablaSimbolos ambito_global;	// guardamos el ambito_global para MarcoDir
     private int dirGlobal=65535;			// comienzo del MarcoPila para ambitoGlobal y resto de programas
 
 
 public GenFinal(LinkedList<tupla_Tercetos> colaTercetos, Tablas tabla, String fichero) {
     
     int desp_total;  //variable para el desplazamiento total de las tablas de simbolos
     archiEscri= new File(fichero);
     tupla_Tercetos tupla_actual;
     //String terceto_actual;
     //TablaSimbolos ambito_actual;
     //cola para ir metiendo los metodos a los que se llama
     //LinkedList<String> colaMetodos = new LinkedList<String> (); 
     Simbolo simbolo;
     c_etiqueta = 0;
     
     Parser.salidadep("Comienza la fase de generacion de codigo objeto");
     //preparamos el fichero que contendra el codigo objeto
     try	{
         bw= new BufferedWriter(new FileWriter(fichero));
     } catch (IOException e) {
          System.err.println("Error fichero de salida para Codigo Objeto ("+fichero+")");
     }
     
 
     //inicializamos el codigo objeto y lo dejamos todo preparado para leer los
     //tercetos del main
     try {
         bw.write("ORG 0\n");
         // Inicializamos la pila al maximo puesto que es decreciente
         // y la guardamos en el puntero de pila
         bw.write ("MOVE #"+dirGlobal+", .SP\n");	// ambito_global
         bw.write ("MOVE .SP, .IX\n");		
         
         /* creamos el RA de la clase que contiene el metodo principal, dejando
          * hueco para todos sus atributos, despues guardamos el IX, que apuntará
          * al primer atributo de la clase que contiene el metodo main
          * para luego poder acceder cogiendo el desplazamiento de la tabla
          * de simbolos */
         ambito_global = tabla.GetAmbitoGlobal();  //buscamos la tabla de la clase del metodo principal
         desp_total = ambito_global.GetDesplazamiento(); //cogemos el desp de la tabla de simbolos global
         bw.write ("ADD #-" + desp_total + ", .SP\n"); //sumamos desp_total de la tabla de simbolos padre al SP
         bw.write("MOVE .A, .SP\n"); //actualizamos SP
         
         // Saltamos a ejecutar el main
 		bw.write("PUSH .SR\n");        
         bw.write("PUSH .IX\n");  	//guardamos el IX para saber donde empiezan los atributos de la tabla de simbolos padre
         //Vamos a buscar el main para que el PC
         //Si el analisis semantico ha validado el codigo, dentro del ambito global deberia estar el objeto main
         simbolo = ambito_global.GetSimbolo("main");
         String etiqueta_main;
         etiqueta_main = simbolo.GetEtiqueta();
         bw.write("CALL /" + etiqueta_main + " ; VAMOS AL MAIN\n");
         bw.write("POP .IX ; Recuperamos el marco de pila\n");
         bw.write("POP .SR\n");
         bw.write("MOVE .IX, .SP\n");
         bw.write("HALT ;Cuando se vuelva del Main se terminara la ejecucion\n");
                 
         /*
          * Bucle para imprimir toda la cola de tercetos del resto de ambitos
          */
         Parser.salidadep("-----------------------------------");
         Parser.salidadep("Tamano de la lista:"+colaTercetos.size());
         Iterator<tupla_Tercetos> it = colaTercetos.iterator();
         while (it.hasNext()) {
             //this.separar(it.next().GetTerceto());
         	tupla_actual = it.next();
             Parser.salidadep("Terceto: "+tupla_actual.GetTerceto());
             //Parser.salidadep("Ambito_actual: "+it.next().GetAmbitoActual());
             ProcesarTerceto(tupla_actual, tabla);
         }
         Parser.salidadep("-----------------------------------");
         
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
     } catch (IOException e) {
     	System.err.println("Tranquilo vaquero");
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
 		ComienzoSubprograma(op1, ambitoterceto); // op1: etiqueta
 	} else if (operacion.equals("ETIQUETA_METODO")) {	// Comienzo Metodo
 		// TODO simplemente llamados a ComienzoSubrograma
 		ComienzoMetodo(ambitoterceto); // op1: etiqueta
 		//ComienzoSubprograma(op1, ambitoterceto);
 	} else if (operacion.equals("ASIGNACION_CADENA")) {	// ETI: data "HOLA"
 		EjecutarAsignaCad(ambitoterceto);
 	} else if (operacion.equals("ASIGNA")){				// asignamos a un temp el valor de otro tmp
 		EjecutarAsigna(ambitoterceto);
 	} else if (operacion.equals("METE_EN_ARRAY")) {		// Asignar valor en posicion del vector
 		AsignaValorVector(ambitoterceto);				// pe: v[2]=23
 	} else if (operacion.equals("SACA_DE_ARRAY")){
 		// TODO revisar-hacer
 		ObtenerValorVector(ambitoterceto);
 	} else if (operacion.equals("IF")) 	{		// If
 		OpCondicional(ambitoterceto);
 	} else if (operacion.equals("ETIQUETA")) {	// Etiqueta
 		EtiquetaIf();
 	} else if (operacion.equals("GOTO")) {		// GOTO
 		OpGoto();
 	} else if (operacion.equals("INIT_PARAM")) {	// SP+desplz
 		InitParam();
 	} else if (operacion.equals("APILAR_PARAM")) {	// Apilar Parametros
 		// TODO revisar-hacer
 		PushParam(ambitoterceto);
 	} else if (operacion.equals("FIN_PARAM")) {		// SP-desplz
 		FinParam();
 	} else if (operacion.equals("CALL")) {		// Llamada a Funcion!
 		LlamadaProg(ambitoterceto);
 	} else if (operacion.equals("CALL_METODO")) {		// Llamada a Funcion!
 		LlamadaMetodo(ambitoterceto);
 	} else if (operacion.equals("RETURN")) {	// return Valor;
 		// TODO hacer-comprobrar
 		ReturnOp(ambitoterceto);
 	} else if (operacion.equals("RET_METODO")) {// Retornar de un Metodo
 		// TODO actualmente RetonoMetodo llama a RetornoProg
 		RetornoMetodo(ambitoterceto);
 		// RetornoProg(ambitoterceto);
 	} else if (operacion.equals("RET")) {		// Retornar de una Funcion
 		// TODO	no comprobada
 		RetornoProg(ambitoterceto);
 	} else if (operacion.equals("DIR_RETORNO")) {	// Push DirRetorno donde dev el valor Retornado	
 		PushDirRetorno(ambitoterceto);
 	} else if (operacion.equals("DIR_RETORNO_METODO")) {	// Apilamos la dir del objeto
 		PushObjetoDirRetorno(ambitoterceto);				// Push DirRetorno donde dev el valor Retornado
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
 	} else if ((operacion.equals("==")) ||
 					(operacion.equals("!="))) {		// OpRel.
 		// TODO Solo hemos hecho estos operadores Relacionales!
 		nemonico ="CMP";	// nemonico para todos los relacionales¿?
 		OpRelacional(ambitoterceto);
 	} else if (operacion.equals("READ")) {		// CIN
 		nemonico = "ININT";
 		GetEntero(ambitoterceto);
 	} else if (operacion.equals("PUT_BOOLEANO")) {	// PRINT Boolean COUT
 		nemonico="WRSTR";
 		PutBool(ambitoterceto);
 	} else if (operacion.equals("PUT_CADENA")) {	// PRINT CADENA	COUT
 		nemonico="WRSTR";
 		PutCadena(ambitoterceto);
 	} else if (operacion.equals("PUT_ENTERO")) {	// PRINT ENTERO COUT
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
  * OpCondicional
  * Condicional IF. Si no se cumple op1 saltamos a op2
  * Usamos las etiquetas de v_cierto y v_falso declaradas en memoria
  */
 private void OpCondicional(TablaSimbolos ambito_terceto) {
 	try {
 		int Despla1=0;
 		String Atributo1= "";
 		TablaSimbolos tabla_op_lejano=null;
 
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		
 		if (ambito_terceto.Esta(op1)) {	// op1 local
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write("CMP #-"+Despla1+"[.IX], /v_cierto \n");
 			bw.write("BNZ /"+op2+"\n");	// salto si el resultado no es cierto
 		} else if (ambito_terceto.Esta(op1)) {	// op1 no local	
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("CMP #-"+Despla1+"[.IY], /v_cierto \n");
 			bw.write("BNZ /"+op2+"\n");	// salto si el resultado no es cierto
 		} else {
 			System.err.println("Error: OpCondicional. Caso no contemplado.");
 		}
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar Operacion If.");
 	}
 }
 
 /*
  * OpGoto
  * Hacemos un salto incondicional a dicha etiqueta
  */
 private void OpGoto () {
 	try {
 		bw.write("br /"+op1+" ;Etiqueta IFs\n");
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar Insertar Etiqua IF.");
 	}
 }
 
 /*
  * EtiquetaIf
  * Simplemente escritibremos en el fichero el nombre de la etiqueta pasada como 1º argumento, op1
  */
 private void EtiquetaIf () {
 	try {
 		bw.write(op1+": ;Etiqueta IFs\n");
 		bw.write("NOP\n");
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar Insertar Etiqua IF.");
 	}
 }
 
 /*
  * ObtenerValorVector
  * Obtenemos el valor de la posicion (op3) del vector (op1) y lo guardamos en un resultado (op2)
  * uso .R9 
  */
 private void ObtenerValorVector (TablaSimbolos ambito_terceto) {
 	try {
 		//Simbolo simbolo_vector = ambito_terceto.GetSimbolo(op1);	// op1= vector
 		Simbolo simbolo_resultado = ambito_terceto.GetSimbolo(op2);	// op2= resultado	siempre_local
 		//Simbolo simbolo_indice = null;								// op3= indice	(unico caso atributo objeto)
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="", Atributo3="";
 		int Despla1=0, Despla3=0;
 		
 		if (EsObjeto(op3)) {	// Es objeto op3?
 			Atributo3 = NombreAtributo(op3);	// ahora tenemos el nombre del atributo
 			op3 = NombreObjeto(op3);			// ahora tenemos el nombre del objeto en op1
 		}
 		//simbolo_indice = ambito_terceto.GetSimbolo(op3);	// op3= indice	(unico caso atributo objeto)
 		
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op3)) {			// todo local
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			// Objeto3? indice
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			// Obtengo el desplzamiento total hasta el elemento ->.A
 			bw.write("ADD #-"+Despla3+"[.IX], #"+Despla1+"\n");
 			bw.write("SUB .IX, .A\n");	// Tengo en .A la direccion al elemento del vector
 			bw.write("MOVE [.A], #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && ambito_terceto.Esta(op3)) { // op1 no local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Objeto3? indice
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			// Obtengo el desplzamiento total hasta el elemento ->.A
 			bw.write("ADD #-"+Despla3+"[.IX], #"+Despla1+"\n");
 			bw.write("SUB .IY, .A\n");	// Tengo en .A la direccion al elemento del vector(respecto de IY)
 			bw.write("MOVE [.A], #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op3)) { // op3 no local
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// Objeto3? indice
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			// Obtengo el desplzamiento total hasta el elemento ->.A
 			bw.write("ADD #-"+Despla3+"[.IY], #"+Despla1+"\n");
 			bw.write("SUB .IX, .A\n");	// Tengo en .A la direccion al elemento del vector
 			bw.write("MOVE [.A], #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op3)) { // NADA local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("MOVE .IY, .R9\n");
 			bw.write("SUB .R9, #"+Despla1+"\n");	// R9=direccion base del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// Objeto3? indice
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			// busco y muevo
 			bw.write("SUB .A, #"+Despla3+"[.IY]\n");	// .A=direccion al elemento del vector
 			bw.write("MOVE [.A], #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else {
 			System.err.println("Error: ObtenerValorVector. Caso no contemplado.");
 		}
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar ObtenerValorVector.");
 	}
 }
 
 /*
  * PushParam
  * Apilamos a partir de SP, que se movio en INIT_PARAM, los argumentos que tendra la funcion llamada.
  * Cuando la funcion llamada pase a ejecutar tendra en su marco de pila todos loa valores ya colocados
  * NO PODEMOS TOCAR R2 !!!
  */
 private void PushParam (TablaSimbolos ambito_terceto) {
 	try {
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		int total=0;
 		int Despla1=0, tamanio=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		
 		if (ambito_terceto.Esta(op1)) {	// op1 Local
 			// obtenemos el desplazamiento de op1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			tamanio = TamSimbolo(ambito_terceto, op1, Atributo1);
 			total = Despla1 + tamanio;
 			for (int i=Despla1; i<total; i++) {
 				bw.write("PUSH #-"+i+"[.IX]\n");	//apilo valores
 			}
 		} else {
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			tamanio = TamSimbolo(tabla_op_lejano, op1, Atributo1);
			total = Despla1 + tamanio;
 			for (int i=Despla1; i<total; i++) {
 				bw.write("PUSH #-"+i+"[.IY]\n");	//apilo valores
 			}
 		}
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar PushParam.");
 	}
 }
 
 /*
  * ReturnOp
  * Guardaremos el valor del simbolo pasado como argumento, op1, en la direccion apilada antes de llamar
  * a la funcion. Para saber mas mirar PushDirRetorno
  * Es decir, al acabar una funcion colocamos el valor de retorno a partir de la direccion especificada
  * usamos R9
  */
 private void ReturnOp (TablaSimbolos ambito_terceto)  {
 	try {
 		// TODO
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		int Despla1=0;
 		int tamanio=1;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		if (ambito_terceto.Esta(op1)) {			// op1 Local
 			// Movemos la direccion de Retorno a un Registro
 			bw.write("MOVE #4[.IX], .IY; ReturnFuncion\n");	// IY tiene la dir donde se guardara el valorRetorno
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			tamanio = TamSimbolo(ambito_terceto, op1, Atributo1);
 			CopiaBloqMem(".IX", Despla1, ".IY", 0, tamanio);
 		} else if (!ambito_terceto.Esta(op1)) {	// op1 no local
 			// TODO NO FUNCIONA
 			// Tenemos en R9 la direccion a partir de la cual debemos dejar el valor de retorno
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			bw.write("MOVE .IY, .R9\n");
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("SUB .R9, #"+Despla1+"\n");
 			bw.write("MOVE .A, .R9\n");
 			tamanio = TamSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Movemos la direccion de Retorno a un Registro
 			bw.write("MOVE #4[.IX], .IY; ReturnFuncion\n");	// IY tiene la dir donde se guardara el valorRetorno
 			// Necesitamo, en caso de objeto el simbolo
 			CopiaBloqMem(".R9", 0, ".IY", 0, tamanio);
 		} else {
 			System.err.println("Error: ReturnOp. Caso no contemplado.");
 		}
 
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar ReturnOp.");
 	}
 }
 
 /*
  * InitParam
  * Movera el SP tantas posiciones como sean necesarias para poder ir colocando los parametros-argumentos
  * en el ambito de la funcion a la que se va a llamar. 
  * Nota: Para saber cuantas posiciones has de desplazar el SP puedes mirar LlamadaProg y PushDIrRetorno
  * Usamos R2
  */
 private void InitParam () {
 	try {
 		// Movemos el SP tantas posiciones como sean necesarias.
 		bw.write("MOVE .SP, .R2\n");
 		bw.write("SUB .SP, #3\n");	//	Atento a los elem q apilas antes de CALL
 		bw.write("MOVE .A, .SP\n");
 		// Ahora tendran que venir PARAM para apilar
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar InitParam.");
 	}
 }
 
 /*
  * FinParam
  * Movera el SP tantas posiciones hacia abajo como haya movido la funcion InitParam. 
  * Nota: Para saber cuantas posiciones has de desplazar el SP puedes mirar LlamadaProg y PushDIrRetorno. y InitParam
  * Usamos R2: Contenia el antiguo valor de SP, metido ahi por InitParam
  */
 private void FinParam () {
 	try {
 		// Movemos el SP tantas posiciones como sean necesarias.
 		bw.write("MOVE .R2, .SP\n");
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar InitParam.");
 	}	
 }
 
 /*
  * PushDirRetorno
  * Apilamos la direccion donde dejaremos el valor de retorno. Esta direccion sera la 
  * direccion ABSOLUTA.
  * Nota: En caso NO devolver nada, void, la funcion se apilara con una direccion IX
  * SIEMPRE SE LLAMA A ESTA FUNCION HAYA O NO VALOR DEVUELTO
  */
 private void PushDirRetorno (TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_return = ambito_terceto.GetSimbolo(op1);	// Simbolo op1
 		// Resto a IX el desplazamiento para llegar al temporal
 		bw.write("SUB .IX,#"+simbolo_return.GetDesplazamiento()+"\n");
 		// Apilo dicha direccion en la cima
 		bw.write("PUSH .A; Apilando donde se guardara el retorno funcion\n");
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar PushDirRetorno.");
 	}
 }
 
 /*
  * PushObjetoDirRetorno
  * Apilamos la direccion del objeto al que se ha llamado para aplicar un metodo
  * Almacenamos en el temporal la direccion de objeto
  * op1 = temporal
  * op2 = objeto
  */
 private void PushObjetoDirRetorno (TablaSimbolos ambito_terceto) {
 	try {
 		int Despla2=0;
 		String Atributo2="";
 		TablaSimbolos tabla_op_lejano = null;
 		// Buscamos la direccion del objeto y la apilamos
 		if (ambito_terceto.Esta(op2)) {	// Solo puede ser un objeto
 			// operando1
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// Sumo IX mas el desplazamiento y lo apilo->dir_objeto
 			bw.write("SUB .IX, #"+Despla2+"; Objeto: "+op2+"\n");
 		} else {
 			// Busco el operando 1.
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// operando1
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			// Sumo IY mas el desplazamiento y lo apilo->dir_objeto
 			bw.write("SUB .IY, #"+Despla2+"\n");
 		}
 		// Apilo el resultado .A que contiene la direccion del objeto
 		bw.write("PUSH .A; Apilando dir del objeto\n");
 		// a partir de aki igual q PushDirRetorno
 		Simbolo simbolo_return = ambito_terceto.GetSimbolo(op1);	// Simbolo op1
 		// Resto a IX el desplazamiento para llegar al temporal
 		bw.write("SUB .IX,#"+simbolo_return.GetDesplazamiento()+"\n");
 		// Apilo dicha direccion en la cima
 		bw.write("PUSH .A; Apilando donde se guardara el retorno funcion\n");
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar ObjetoPushDirRetorno.");
 	}
 }
 
 /*
  * LlamadaProg
  * Realizamos la llamada a una funcion. Hay q tener mucho cuidado con lo que apilamos. Luego hay 
  * q desapilar lo mismo
  */
 private void LlamadaProg (TablaSimbolos ambito_terceto) {
 	try {
 		// Apilamos todo lo necesario para la vuelta
 		bw.write("PUSH .SR\n");
 		bw.write("PUSH .IX\n");
 		// Salto a etiqueta
 		bw.write("CALL /"+op1+"; SALTO A PROGRAMA\n");
 		// Desapilamos lo mismo que apilamos
 		bw.write("POP .IX\n");
 		bw.write("POP .SR\n");
 		// Hay otro valor en la pila. PushDirRetorno lo metio!
 		bw.write("POP .R0; Sacando el DIR VALOR retorno\n"); // siempre se apila
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar Llamada a Programa.");
 	}
 }
 
 /*
  * LlamadaMetodo
  * op1= contiene el nombre del objeto
  * op2= contiene la etiqueta a saltar
  */
 private void LlamadaMetodo (TablaSimbolos ambito_terceto) {
 	try {
 		// Apilamos todo lo necesario para la vuelta
 		bw.write("PUSH .SR\n");
 		bw.write("PUSH .IX\n");
 		// Salto a etiqueta
 		bw.write("CALL /"+op2+"; SALTO A METODO\n");
 		// Desapilamos lo mismo que apilamos
 		bw.write("POP .IX\n");
 		bw.write("POP .SR\n");
 		// Hay otro valor en la pila. PushDirRetorno lo metio!
 		bw.write("POP .R0; Sacando el DIR VALOR retorno\n"); // siempre se apila
 		bw.write("POP .R0; Sacando el DIR OBJETO\n"); // siempre se apila
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar Llamada a Programa.");
 	}
 }
 
 /*
  * ReturnMetodo
  * Simplemente llamado a RetornoFuncion
  */
 private void RetornoMetodo (TablaSimbolos ambito_terceto) {
 	try {
 		RetornoProg(ambito_terceto);
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar RetornoPrograma.");
 	}
 }
 
 /*
  * RetornoProg
  * Volveremos a la funcion llamante, dejando igual q cuando nos lo dieron
  */
 private void RetornoProg (TablaSimbolos ambito_terceto) {
 	try {
 		// Dejamos el .SP igual que cuando nos lo dieron
 		bw.write("MOVE .IX, .SP\n");
 		// Ahora debe estar en la cima de la pila la direccion de retorno
 		bw.write("RET\n");
 	} catch (Exception e) {
 		System.err.println("Error: Ejecutar RetornoPrograma.");
 	}
 }
 
 /*
  * OpRelacional
  * Realizaremos dependiendo de la operacion el calculo. < > == =! <= >= 
  */
 private void OpRelacional (TablaSimbolos ambito_terceto) {
 	try {
 		//Simbolo simbolo_op1 = null;	// Simbolo op1
 		//Simbolo simbolo_op2 = null;	// Simbolo op2
 		Simbolo simbolo_res = ambito_terceto.GetSimbolo(op3);	// Simbolo Resultado. Siempre local=temp
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		String Atributo2="";
 		int Despla1=0, Despla2=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}		
 		if (EsObjeto(op2)) {	// Es objeto op2?
 			Atributo2 = NombreAtributo(op2);	// ahora tenemos el nombre del atributo
 			op2 = NombreObjeto(op2);
 		}
 		// En cualquier caso hago esto, he mnodificado op* en caso de Objeto
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);	// Simbolo op1
 		//simbolo_op2 = ambito_terceto.GetSimbolo(op2);	// Simbolo op2
 
 		// Nemonico en todos los casos: CMP
 		// Recuerda que CMP solo actualiza el SR con op1-op2.	
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {			// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// Comparo el contenido de memoria de los dos operandos.
 			bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IX]\n");
 			// Necesito saber el resultado del CMP dejado en SR, e concreto si es cero o no
 			bw.write("AND .SR, #1\n");	// Solo si esta activo el bit Z son iguales o no
 			if (operacion.equals("!=")) {
 				bw.write("XOR .A, #1\n");
 			}//  else if (operacion.equals("==")) { // nada en este caso }
 			// Muevo la operacion relacional
 			bw.write("MOVE .A, #-" + simbolo_res.GetDesplazamiento() + "[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) { 	//op1 No local
 			// Busco el operando 1.
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// Comparo el contenido de memoria de los dos operandos.
 			bw.write(nemonico+" #-"+Despla1+"[.IY], #-"+Despla2+"[.IX]\n");
 			// Necesito saber el resultado del CMP dejado en SR, e concreto si es cero o no
 			bw.write("AND .SR, #1\n");	// Solo si esta activo el bit Z son iguales o no
 			if (operacion.equals("!=")) {
 				bw.write("XOR .A, #1\n");
 			}//  else if (operacion.equals("==")) { // nada en este caso }
 			// Muevo la operacion relacional
 			bw.write("MOVE .A, #-" + simbolo_res.GetDesplazamiento() + "[.IX]\n");
 		} else if (ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { 	//op2 No local
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// operando2
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			// Comparo el contenido de memoria de los dos operandos.
 			bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IY]\n");
 			// Necesito saber el resultado del CMP dejado en SR, e concreto si es cero o no
 			bw.write("AND .SR, #1\n");	// Solo si esta activo el bit Z son iguales o no
 			if (operacion.equals("!=")) {
 				bw.write("XOR .A, #1\n");
 			}//  else if (operacion.equals("==")) { // nada en este caso }
 			// Muevo la operacion relacional
 			bw.write("MOVE .A, #-" + simbolo_res.GetDesplazamiento() + "[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { 	//NADA local
 			// Busco el operando 1.
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Ya q op2 usara el registro IY muevo este a R9
 			//bw.write("MOVE .IY, .R9\n");
 			bw.write("SUB .IY, #"+Despla1+"\n");
 			bw.write("MOVE .A, .R9\n");
 			// Busco el operando 2.
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// operando2
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			bw.write(nemonico+" [.R9], #-"+Despla2+"[.IY]\n");
 			// Necesito saber el resultado del CMP dejado en SR, e concreto si es cero o no
 			bw.write("AND .SR, #1\n");	// Solo si esta activo el bit Z son iguales o no
 			if (operacion.equals("!=")) {
 				bw.write("XOR .A, #1\n");
 			}//  else if (operacion.equals("==")) { // nada en este caso }
 			// Muevo la operacion relacional
 			bw.write("MOVE .A, #-" + simbolo_res.GetDesplazamiento() + "[.IX]\n");
 		} else {
 			System.err.println("Op "+nemonico+". Caso no contemplado");			
 		}		
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar OpRelacional.");
 	}
 }
 
 /*
  * AsignaValorVector
  * Asignamos un valor a una posicion (segun el indice) del vector.
  * uso de .R9 y .R8
  */
 private void AsignaValorVector (TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolo_vector = null;	// Simbolo del Vector.
 //		Simbolo simbolo_valor = null;	// Simbolo del valor a meter en el vector
 //		Simbolo simbolo_indice = null;	// Simbolo del indice del vector
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		String Atributo2="";
 		String Atributo3="";
 		int Despla1=0, Despla2=0, Despla3=0;
 		
 		// Son objetos?
 		// solo podran serlo valor(op2) e indice(op3). NBo tiene sentido un tipo vector como atributo de objeto! o eso creo...
 		if (EsObjeto(op2)) {	// Es objeto?
 			Atributo2 = NombreAtributo(op2);	// ahora tenemos el nombre del atributo
 			op2 = NombreObjeto(op2);			// ahora tenemos el nombre del objeto en op1
 		}		
 		if (EsObjeto(op3)) {	// Es objeto?
 			Atributo3 = NombreAtributo(op3);	// ahora tenemos el nombre del atributo
 			op3 = NombreObjeto(op3);
 		}
 		// En cualquier caso hago esto, he mnodificado op* en caso de Objeto
 		simbolo_vector = ambito_terceto.GetSimbolo(op1);	// Simbolo del Vector
 //		simbolo_valor = ambito_terceto.GetSimbolo(op2);		// Simbolo del valor a meter en el vector
 //		simbolo_indice= ambito_terceto.GetSimbolo(op3);		// Simbolo del indice del vector
 		
 		// Tenemos tres simbolos a dos posibilidades cada uno de estar o no en el ambito local -> 2 * 2 * 2 = 8 posibilidades
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2) && ambito_terceto.Esta(op3)) {	// todo local!
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// operando3
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			// BUscamos el valor del indice y asignamos el valor a dicha posicion
 			bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+simbolo_vector.GetDesplazamiento()+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+Despla2+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if ((!ambito_terceto.Esta(op1)) && ambito_terceto.Esta(op2) && ambito_terceto.Esta(op3))	{	//solo op1 No local
 			// No cambia nada, en este caso solo el simbolo vector esta fuera
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// operando3
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("ADD #"+Despla1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IY, .A\n");	// BuscaMarcoDir ha dejado en IY la direccion del marco del vector
 			bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+Despla2+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if (ambito_terceto.Esta(op1) && (!ambito_terceto.Esta(op2)) && ambito_terceto.Esta(op3))	{	//solo op2 No local
 			// operando3
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+Despla1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// Objeto2? valor
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			bw.write("MOVE #-"+Despla2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2) && (!ambito_terceto.Esta(op3)))	{	//solo op3 No local
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// Objeto3?	indice
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+Despla1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .IY\n");	// IY = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+Despla2+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if ((!ambito_terceto.Esta(op1)) && (!ambito_terceto.Esta(op2)) && ambito_terceto.Esta(op3))	{	//op1 y op2 No local
 			// Objeto3?	indice
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			bw.write("MOVE #-"+Despla3+"[.IX],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("ADD #"+Despla1+", .R9\n");	// desplazamiento del ector mas indice
 			bw.write("MOVE .IY, .R8\n");	// contenido que deja la función a R7
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			// int despl_op1 = tabla_op_lejano.GetSimbolo(op1).GetDesplazamiento();
 			bw.write("SUB .R8, .A\n");	// BuscaMarcoDir ha dejado en IY la direccion del marco del vector
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector
 			// A BUSCAR OP2
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// Objeto2? valor
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			bw.write("MOVE #-"+Despla2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else if ((!ambito_terceto.Esta(op1)) && ambito_terceto.Esta(op2) && (!ambito_terceto.Esta(op3)))	{	//op1 y op3 No local
 			// Objeto2? valor
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// Objeto3?	indice
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("ADD #"+Despla1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IY, .A\n");
 			bw.write("MOVE .A, .IY\n");	// R8 = Desplzamiento total hasta elemento del vector
 			bw.write("MOVE #-"+Despla2+"[.IX], [.IY]\n");	// Muevo el valor del elemento al vector
 		} else if (ambito_terceto.Esta(op1) && (!ambito_terceto.Esta(op2)) && (!ambito_terceto.Esta(op3)))	{	//op2 y op3 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// Objeto3?	indice
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
 			bw.write("ADD #"+Despla1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IX, .A\n");
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// Objeto2? valor
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			bw.write("MOVE #-"+Despla2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
 		} else if (!ambito_terceto.Esta(op1) && (!ambito_terceto.Esta(op2)) && (!ambito_terceto.Esta(op3))) {	// NADA LOCAL!
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// Objeto3?	indice
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			bw.write("MOVE #-"+Despla3+"[.IY],.R9\n");	// R9=valor del indice
 			// Busco el desplazamiento del vector
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write("ADD #"+Despla1+", .R9\n");	// .A=deplazamiento resp IX del elem vector
 			bw.write("SUB .IY, .A\n");
 			bw.write("MOVE .A, .R8\n");	// R8 = Desplzamiento total hasta elemento del vector			
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// Objeto2? valor
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			bw.write("MOVE #-"+Despla2+"[.IY], [.R8]\n");	// Muevo el valor del elemento al vector
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
 		//Simbolo simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		TablaSimbolos tabla_op_lejano = null;	// En caso de ser variable local.
 		int Despla1=0;
 		String Atributo1="";
 
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			System.err.println("Estas aki para recoger un valor en local: "+op1+" "+Atributo1);
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write(nemonico+" #-"+Despla1 + "[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write(nemonico + " #-"+Despla1+"[.IY]\n");	
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
 		//Simbolo simbolo_op1 = null;
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		int Despla1=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		// recuperamos el simbolo a imprimir
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write(nemonico + "#-"+Despla1+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			bw.write(nemonico + "#-"+Despla1+"[.IY]\n");		
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
 		//Simbolo simbolo_op1 = null;
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		int Despla1=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		// recuperamos el simbolo a imprimir
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write("MOVE #-"+Despla1+"[.IX],.R9\n");
 			bw.write(nemonico + " [.R9]\n");	// imprime a partir de la etiqueta
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);		
 			bw.write("MOVE #-"+Despla1+"[.IY],.R9\n");
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
 		//Simbolo simbolo_op1 = null;
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		int Despla1=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		// recuperamos el simbolo a imprimir
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);	
 			bw.write("CMP #-"+Despla1+"[.IX], /v_cierto\n");
 			bw.write("BZ $4\n");	// Es cierto? Sí -> salto!
 			bw.write(nemonico + " /cad_falso\n");	// imprime-> "false"
 			bw.write("BR $2\n");
 			bw.write(nemonico + " /cad_cierto\n");	// imprime-> "cierto"
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Imprimimos lacadena q representa a dicho valor
 			bw.write("CMP #-" + Despla1 + "[.IY], /v_cierto\n");
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
 		//Simbolo simbolo_op1 = null;
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		int Despla1=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}
 		// recuperamos el simbolo a imprimir
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		// Recordamos que op2 en OpUnaria es siempre 1.
 		Simbolo simbolo_resultado = ambito_terceto.GetSimbolo(op3);	// siempre local (temp)
 		
 		// Recuerda q "nemonico" fue ya asignado en la llamada a esta funcion
 		if (ambito_terceto.Esta(op1)) {			// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			bw.write(nemonico+" #-"+Despla1+"[.IX], #"+op2+"\n");
 			bw.write("MOVE .A, #-"+simbolo_resultado.GetDesplazamiento()+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// La operacion unaria se queda en el acumulador, luego la llevamos a la dir de mem
 			bw.write(nemonico+" #-"+Despla1+"[.IY], #"+op2+"\n");
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
  * OpMUL(Operancion, op1, op2, resultado, ambitoterceto); a temporal!
  * Correccion, en caso de ser una operacion normal se cumple lo de arribe,
  * en caso de ser op= (Operacion con asignacion) el simbolo resultado puede no ser local
  */
 private void OpBinaria (TablaSimbolos ambito_terceto) {
 	try {
 		//Simbolo simbolo_op1 = null;
 		//Simbolo simbolo_op2 = null;
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		String Atributo2="";
 		String Atributo3="";
 		int Despla1=0, Despla2=0, Despla3=0;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}		
 		if (EsObjeto(op2)) {	// Es objeto op2?
 			Atributo2 = NombreAtributo(op2);	// ahora tenemos el nombre del atributo
 			op2 = NombreObjeto(op2);
 		}
 		if (EsObjeto(op3)) {	// Es objeto op3?
 			Atributo3 = NombreAtributo(op3);	// ahora tenemos el nombre del atributo
 			op3 = NombreObjeto(op3);
 		}
 		// En cualquier caso hago esto, he mnodificado op* en caso de Objeto
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		//simbolo_op2 = ambito_terceto.GetSimbolo(op2);
 		//Simbolo simbolo_resultado = ambito_terceto.GetSimbolo(op3);	// siempre local (temp)
 		// Recuerda q "nemonico" fue ya asignado en la llamada a esta funcion
 		
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {			// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// operando3
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IX]\n");
 			bw.write("MOVE .A, #-"+Despla3+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) { 	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
 			bw.write(nemonico+" #-"+Despla1+"[.IY], #-"+Despla2+"[.IX]\n");
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// operando1
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			bw.write("MOVE .A, #-"+Despla3+"[.IY]\n");
 		} else if (ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { 	//op2 No local
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// operando2
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			// operando3
 			Despla3 = DesplzSimbolo(ambito_terceto, op3, Atributo3);
 			// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
 			bw.write(nemonico+" #-"+Despla1+"[.IX], #-"+Despla2+"[.IY]\n");
 			bw.write("MOVE .A, #-"+Despla3+"[.IX]\n");
 		} else if (!ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { 	//NADA local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Puesto q op2 tambien usara esta func. muevo el IY a otro reg
 			bw.write("MOVE .IY, .R9\n");	// DIR del MARCO de OP1 en R9!!!!
 			bw.write("ADD #-"+Despla1+",.R9\n");	//Dejo en R9 la direccion exacta del dato
 			bw.write("MOVE .A, .R9\n");
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// operando2
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			// La suma se queda en el Acumulador, luego lo muevo al simbolo_resultado
 			bw.write(nemonico+" [.R9], #-"+Despla2+"[.IY]\n");
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op3, ambito_terceto);
 			// operando1
 			Despla3 = DesplzSimbolo(tabla_op_lejano, op3, Atributo3);
 			bw.write("MOVE .A, #-"+Despla3+"[.IY]\n");
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
  */
 private void EjecutarAsignaCad (TablaSimbolos ambito_terceto) {
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
  * op1 y op2 pueden ser atributos
  */
 private void EjecutarAsigna (TablaSimbolos ambito_terceto) {
 	try {
 		//Simbolo simbolo_op1 = null;
 		//Simbolo simbolo_op2 = null;
 		TablaSimbolos tabla_op_lejano = null;
 		String Atributo1="";
 		String Atributo2="";
 		int Despla1=0, Despla2=0;
 		int tamanio=1;
 		
 		if (EsObjeto(op1)) {	// Es objeto op1?
 			Atributo1 = NombreAtributo(op1);	// ahora tenemos el nombre del atributo
 			op1 = NombreObjeto(op1);			// ahora tenemos el nombre del objeto en op1
 		}		
 		if (EsObjeto(op2)) {	// Es objeto op2?
 			Atributo2 = NombreAtributo(op2);	// ahora tenemos el nombre del atributo
 			op2 = NombreObjeto(op2);
 		}
 		// En cualquier caso hago esto, he mnodificado op* en caso de Objeto
 		//simbolo_op1 = ambito_terceto.GetSimbolo(op1);
 		//simbolo_op2 = ambito_terceto.GetSimbolo(op2);
 		
 		if (ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {	// todo local!
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			tamanio = TamSimbolo(ambito_terceto, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			tamanio = TamSimbolo(ambito_terceto, op2, Atributo2);
 			// Caso todo en LOCAL - MOVE #-op2.desp[.IX], #-op1.desp[.IX]
 			CopiaBloqMem(".IX", Despla2, ".IX", Despla1, tamanio);
 		} else if (!ambito_terceto.Esta(op1) && ambito_terceto.Esta(op2)) {	//op1 No local
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// operando1
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			tamanio = TamSimbolo(tabla_op_lejano, op1, Atributo1);
 			// operando2
 			Despla2 = DesplzSimbolo(ambito_terceto, op2, Atributo2);
 			tamanio = TamSimbolo(ambito_terceto, op2, Atributo2);
 			// Pongo el valor local en el hueco ajeno
 			CopiaBloqMem(".IX", Despla2, ".IY", Despla1, tamanio);
 		} else if (ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) { //op2 No local	
 			// operando1
 			Despla1 = DesplzSimbolo(ambito_terceto, op1, Atributo1);
 			tamanio = TamSimbolo(ambito_terceto, op1, Atributo1);
 			// operando2
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			tamanio = TamSimbolo(tabla_op_lejano, op2, Atributo2);
 			// Pongo el valor ajeno en el hueco local
 			//bw.write("MOVE #-"+ Despla2 +"[.IY], #-"+Despla1+"[.IX]\n");
 			CopiaBloqMem(".IY", Despla2, ".IX", Despla1, tamanio);
 		} else if (!ambito_terceto.Esta(op1) && !ambito_terceto.Esta(op2)) {	// NADA LOCAL!
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op1, ambito_terceto);
 			// si son objetos
 			Despla1 = DesplzSimbolo(tabla_op_lejano, op1, Atributo1);
 			tamanio = TamSimbolo(tabla_op_lejano, op1, Atributo1);
 			// Puesto q op2 tambien usara esta func. muevo el IY a otro reg
 			bw.write("MOVE .IY, .R9\n");	// DIR del MARCO de OP1 en R9!!!!
 			bw.write("ADD #-"+Despla1+",.R9\n");	//Dejo en R9 la direccion exacta del dato
 			bw.write("MOVE .A, .R9\n");
 			// Dejará en IY el marco de pila para acceder al simbolo op.
 			tabla_op_lejano = BuscaMarcoDir(op2, ambito_terceto);
 			// si son objetos
 			Despla2 = DesplzSimbolo(tabla_op_lejano, op2, Atributo2);
 			tamanio = TamSimbolo(tabla_op_lejano, op2, Atributo2);
 			// Pongo el valor local en el hueco ajeno
 			//bw.write("MOVE #-"+ Despla2 +"[.IY], [.R9]\n");	// RECUERDA R9!!
 			CopiaBloqMem(".IY", Despla2, ".R9", 0, tamanio);
 		} else {
 			System.err.println("Ejecutar Asigna. Caso no contemplado");
 		}
 
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar Asigna.");		
     }
 }
 
 /*
  * Obtener desplazamiento
  * Devuelve un entero con el desplazamiento desde la base donde esta declarado ese simbolo hasta el omienzo de dicho simbolo
  */
 private int DesplzSimbolo (TablaSimbolos ambitoSimbolo, String operando, String Atributo) {
 	int Desplazamiento=0;
 	try {
 		if (Atributo.isEmpty()) {	//	Todo menos un atributo de objeto, tambien un atributo dentro de un metodo
 			// obtenemos el desplazamiento del simbolo introducido en dicho ambito
 			Desplazamiento = ambitoSimbolo.GetSimbolo(operando).GetDesplazamiento();
 		} else {	// atributo de clase
 			// Obtengo el desplazamiento
 			Desplazamiento = ambitoSimbolo.GetSimbolo(operando).GetAtributo(ambitoSimbolo.GetSimbolo(operando).GetNombre()+"."+Atributo).GetDesplazamiento();
 			Desplazamiento  = Desplazamiento  + ambitoSimbolo.GetSimbolo(operando).GetDesplazamiento();
 		}
 	} catch (Exception e) {
 		System.err.println("Error: DesplzSimbolo.Operando vale:"+operando+", terceto:"+operacion);
 	}
 	
 	return Desplazamiento;
 }
 
 /*
  * TamSimbolo
  * Devuelve un entero con el tamanio del simbolo, esto se hace en caso de tener que asignar un bloque de datos a otro
  */
 private int TamSimbolo (TablaSimbolos ambitoSimbolo, String operando, String Atributo) {
 	int Tamano=1;
 	try {
 		if (Atributo.isEmpty()) {	// Todo menos un atributo de objeto
 			// tamano del objeto en el simbolo
 			Tamano = ambitoSimbolo.GetSimbolo(operando).Actualiza_Tamano();
 		} else {
 			// tamanio del simbolo;
 			Tamano = ambitoSimbolo.GetSimbolo(operando).GetAtributo(ambitoSimbolo.GetSimbolo(operando).GetNombre()+"."+Atributo).Actualiza_Tamano();
 		}
 	} catch (Exception e) {
 		System.err.println("Error: TamSimbolo.");
 	}
 	return Tamano;
 }
 
 /*
  * Asigna un bloque de memoria a otro
  * Copiamos a partir de la direccion Base con desplazamiento base en la direccion edstino con desplzamiento destino
  * tantas posiciones como tamanio diga
  * Si llega con R9 el desplazamiento tiene que haberse aplicado antes, es decir, sera cero!
  */
 private void CopiaBloqMem (String dirBase, int DesplBase, String dirDest, int DesplDest, int tamanio) {
 	try {
 		int despl1=DesplBase, despl2=DesplDest;
 		for (int i=0; i<tamanio;i++) {
 			if (dirDest.equals(".R9")) {
 				bw.write("MOVE #-"+despl1+"["+dirBase+"], ["+dirDest+"]; Moviendo bloque\n");
 				//decremento el valor de .R9
 				bw.write("SUB .R9, #1\n");
 				bw.write("MOVE .A, .R9\n");
 			} else if (dirBase.equals(".R9")) {
 				bw.write("MOVE ["+dirBase+"], ["+dirDest+"]; Moviendo bloque\n");
 				//decremento el valor de .R9
 				bw.write("SUB .R9, #1\n");
 				bw.write("MOVE .A, .R9\n");
 			} else {
 				bw.write("MOVE #-"+despl1+"["+dirBase+"], #-"+despl2+"["+dirDest+"]; Moviendo bloque\n");
 			}
 			despl1++;
 			despl2++;
 		}
 		if (tamanio==0) {
 			System.err.println("ERROR en CopiaBloqMem");
 		}
 	} catch (Exception e) {
         System.err.println("Error: Ejecutar CopiaBloqMem.");
 	}
 }
 
 /*
  * Ejecutar Asignacion es para casos donde el valor a asignar sea un ENTERO!
  * Luego guardo a partir del IX el valor de dicho elemento
  * Siempre es un valor a un temporal.
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
  *	Comienzo Metodo
  */
 private void ComienzoMetodo (TablaSimbolos ambito_terceto) {
 	ComienzoSubprograma(op1, ambito_terceto);
 }
 
 /*
  * Busca la direccion de un elemento-simbolo(String) que no este en el ambito dado
  * como parametro. Sólo hay dos posibilidades en esta funcion:
  * 1- El simbolo es local, por lo que no se ha necesitado esta funcion
  * 2- En caso de no ser local es variable global y se devuelve el ambito global y la
  * direccion del IX global, almacenado en una variable al inicio global
  */
 private TablaSimbolos BuscaMarcoDir (String Nombre, TablaSimbolos ambito_terceto) {
 	try {
 		if (!ambito_terceto.Esta(Nombre)) {	// Esta en ambito global
 			if (ambito_global.Esta(Nombre)) {
 				bw.write("MOVE #"+dirGlobal+",.IY\n");
 				return ambito_global;	// ambito_global -> dec al comienzo
 			} else { // No esta en local ni en global!, atributo de clase
 				// TODO simbolo que no esta en local ni global es atributo de objeto
 				// bw.write("MOVE #"+dirGlobal+",.IY\n");
 				// Sacamos de la pila con desplazamiendo positivola direcion base del objeto
 				bw.write("MOVE #5[.IX], .IY; AtributoDentroMetodo, dir a Objeto\n");
 				// Obtenemos el ambito padre del metodo, esto nos dara el desplzamiento en ese ambito
 				return ambito_terceto.Ambito_Padre();
 			}
 		} else {
 			System.err.println("Error: BuscaMarcoDir caso no contemplado.");
 			return null;
 		}
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
     op1=linea.substring(0,u);	// Tenemos op1
     linea=linea.substring(u+1);
 
     // Problemas con cadenas
     if (linea.contains("\"")) {	// Terceto con cadena de texto
     	//System.err.println("Es algo con cadenas!"+linea);
     	//aklinea=linea.substring(u+1);
     	u= linea.indexOf("\",")+1;
     	//System.err.println("Es algo con cadenas:"+linea.substring(0,u));
 	    op2=linea.substring(0,u);
     } else {
 	    u= linea.indexOf(",");
 	    op2=linea.substring(0,u);
     }
     linea=linea.substring(u+1);
 
     op3=linea.substring(0,linea.indexOf("\n"));
 }
 
 /*
  * Devuelve Cierto si es una llamada a objeto
  */
 private boolean EsObjeto (String operando) {
 	try {
 		return operando.contains(".");			
 	} catch (Exception e) {
 		System.err.println("Error: EsObjeto.");
 		return false;	// hay q poner algo...
 	}
 }
 
 /*
  * Devuelve Nombre del objeto
  */
 private String NombreObjeto (String operando) {
 	try {
 		return operando.substring(0, operando.indexOf("."));
 	} catch (Exception e) {
 		System.err.println("Error: NombreObjeto.");
 		return null;
 	}
 }
 
 private String NombreAtributo (String operando) {
 	try {
 	    return operando.substring(operando.indexOf(".")+1, operando.length());
 	} catch (Exception e) {
 		System.err.println("Error: NombreAtributo.");
 		return null;
 	}
 }
 
 
 private void daInformacion (String operando, TablaSimbolos ambito_terceto) {
 	try {
 		Simbolo simbolito = ambito_terceto.GetSimbolo(operando);
 		Parser.salidadep("-> Dando informacion acerca del operando: "+operando);
 		Parser.salidadep("Nombre: "+simbolito.GetNombre());
 		Parser.salidadep("Etiqueta: "+simbolito.GetEtiqueta());
 	} catch (Exception e) {
 		System.err.println("Fallo en impresion de informacion de un operando.");
 	}
 }
     
 }
