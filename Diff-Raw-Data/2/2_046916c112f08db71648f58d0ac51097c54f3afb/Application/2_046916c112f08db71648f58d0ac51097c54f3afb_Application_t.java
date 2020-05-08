 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.DynamicForm;
 
 import java.util.ArrayList;
 import java.util.Map;
 import views.html.*;
 import java.lang.String;
 import java.sql.*;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.FieldPosition;
 import conf.VariablesAmbiente;
 import Cliente.ClienteSQLfi;
 import Cliente.ClienteSQLfiImpl;
 import Despachador.ConjuntoResultado;
 import Despachador.ResultadoSQLfi;
 import Despachador.Mu;
 import Despachador.ObjetoSQLfi;
 import ObjetoDDL.PredicadoDifusoConjunto;
 import ObjetoDDL.TerminoDifuso;
 import ObjetoExpresion.ConjuntoDifuso;
 import ObjetoExpresion.ConjuntoDifusoExpresion;
 import ObjetoExpresion.ConjuntoDifusoExtension;
 import ObjetoExpresion.ConjuntoDifusoExtensionEscalar;
 import ObjetoExpresion.ConjuntoDifusoTrapecio;
 import ObjetoExpresion.Dominio;
 import ObjetoExpresion.DominioEscalar;
 import ObjetoExpresion.DominioFecha;
 import ObjetoExpresion.DominioHora;
 import ObjetoExpresion.DominioNumerico;
 import ObjetoExpresion.DominioTupla;
 import ObjetoExpresion.ElemExtEscalar;
 import ObjetoExpresion.ElemExtNum;
 import ObjetoExpresion.ElementoExtension;
 import Traductor.ConvertirExpresion;
 
 public class Application extends Controller {
 	
 	private static double DOMINIO_INFERIOR;
 	private static double DOMINIO_SUPERIOR;
 	
 	private static ConvertirExpresion traductor = new ConvertirExpresion();
 	
 	// M�todo para imprimir el dominio de un conjunto difuso.
 	private static void imprimirDominio(Dominio dominio) {
 
 		if (dominio instanceof DominioEscalar) {
 			System.out.println("Dominio Escalar: " + ((DominioEscalar) dominio).obtenerEscalar());
 		} else if (dominio instanceof DominioNumerico) {
 			System.out.print(((DominioNumerico) dominio).getInferior());
 			System.out.print("..");
 			System.out.println(((DominioNumerico) dominio).getSuperior());			
 			DOMINIO_INFERIOR = ((DominioNumerico) dominio).getInferior();
 			DOMINIO_SUPERIOR = ((DominioNumerico) dominio).getSuperior();
 		} else if (dominio instanceof DominioFecha) {
 			System.out.println("Dominio Fecha: " + ((DominioFecha) dominio).getFecha());
 		} else if (dominio instanceof DominioHora) { // Fecha - hora
 			System.out.println("Dominio Hora: " + ((DominioHora) dominio).getDominioHora());
 		} else if(dominio instanceof DominioTupla) {
 			System.out.println("No implementado.");
 		}
 
 	}
 
 	// M�todo para imprimir un conjunto difuso.
 	private static void imprimirConjunto(ConjuntoDifuso conjunto) {
 
 		if (conjunto instanceof ConjuntoDifusoExtension) {
 
 			int longitud = ((ConjuntoDifusoExtension)conjunto).numeroElementos();
 			ArrayList<ElementoExtension> elementos = ((ConjuntoDifusoExtension)conjunto).getElementos();
 			System.out.print("Conjunto Extension --> {");
 			if (conjunto instanceof ConjuntoDifusoExtensionEscalar) {
 				ElemExtEscalar elemento = null;
 				for (int cont = 0; cont < longitud - 1; cont++) {
 					elemento = (ElemExtEscalar) elementos.get(cont);
 					System.out.print(elemento.getEscalar());
 					System.out.print("/");
 					System.out.print(elemento.getMembresia());
 					System.out.print(", ");
 				}
 				elemento = (ElemExtEscalar) elementos.get(longitud - 1);
 				System.out.print(elemento.getEscalar());
 				System.out.print("/");
 				System.out.print(elemento.getMembresia());
 				System.out.print("}");
 			} else {
 				ElemExtNum elemento = null;
 				for (int cont = 0; cont < longitud - 1; cont++) {
 					elemento = (ElemExtNum) elementos.get(cont);
 					System.out.print(elemento.getNum());
 					System.out.print("/");
 					System.out.print(elemento.getMembresia());
 					System.out.print(", ");					
 				}
 				elemento = (ElemExtNum) elementos.get(longitud - 1);
 				System.out.print(elemento.getNum());
 				System.out.print("/");
 				System.out.print(elemento.getMembresia());
 				System.out.println("}");
 			}
 
 		} else if (conjunto instanceof ConjuntoDifusoTrapecio) {
 
 			double coordA = ((ConjuntoDifusoTrapecio)conjunto).getCoordA();
 			double coordB = ((ConjuntoDifusoTrapecio)conjunto).getCoordB();
 			double coordC = ((ConjuntoDifusoTrapecio)conjunto).getCoordC();
 			double coordD = ((ConjuntoDifusoTrapecio)conjunto).getCoordD();
 
 			System.out.print("Trapecio --> (");
 			if ((coordA == coordB) && (coordB == DOMINIO_SUPERIOR)) {
 				System.out.print("INFINITO");
 				System.out.print(", ");
 				System.out.print("INFINITO");
 			} else if ((coordA == coordB) && (coordB == DOMINIO_INFERIOR)) {
 				System.out.print("INFINITO");
 				System.out.print(", ");
 				System.out.print("INFINITO");
 			} else if (coordA == DOMINIO_INFERIOR) {
 				System.out.print("INFINITO");
 				System.out.print(", ");
 				System.out.print(((ConjuntoDifusoTrapecio)conjunto).getCoordB());
 			} else {
 				System.out.print(((ConjuntoDifusoTrapecio)conjunto).getCoordA());
 				System.out.print(", ");
 				System.out.print(((ConjuntoDifusoTrapecio)conjunto).getCoordB());
 			}
 			System.out.print(", ");
 			if ((coordC == coordD) && (coordD == DOMINIO_SUPERIOR)) {
 				System.out.print("INFINITO");
 				System.out.print(", ");
 				System.out.print("INFINITO");
 			} else if (coordD == DOMINIO_SUPERIOR) {
 				System.out.print(((ConjuntoDifusoTrapecio)conjunto).getCoordC());
 				System.out.print(", ");
 				System.out.print("INFINITO");
 			} else {
 				System.out.print(((ConjuntoDifusoTrapecio)conjunto).getCoordC());
 				System.out.print(", ");
 				System.out.print(((ConjuntoDifusoTrapecio)conjunto).getCoordD());
 			}			
 			System.out.println(")");
 
 		} else {	// Conjunto expresion		
 			System.out.println("Conjunto por Expresion --> " + traductor.expresionXCadena(((ConjuntoDifusoExpresion)conjunto).getExpresion()));
 		}
 	}
   
     public static String EjecutarConsulta(String consulta){
 
         
         ClienteSQLfi app = new ClienteSQLfiImpl();
 
         try{
             app.cargarConfiguracion();
             app.conectarUsuarios();
         }
         
         catch (Exception e){
 
             e.printStackTrace();
             return ("<h1>Error</h1><p>No se pudo establecer conexión con el repositorio de datos, "+
                     "por favor intente más tarde, en caso de que el problema persista, "+
                     "comuniquese con nuestro administrador mediante el correo wm@consulta.dii.usb.ve</p>");
         }
 
 
         ObjetoSQLfi obj;
 
         try{
             obj = app.ejecutarSentencia(consulta);
         }
         catch (Exception e){
         
             e.printStackTrace();
             return ("<h1>Error</h1><p>No se pudo procesar su pregunta de manera exitosa, "+
                     "por favor intente más tarde, en caso de que el problema persista, "+
                     "comuniquese con nuestro administrador mediante el correo wm@consulta.dii.usb.ve</p>");
         }   
         
         StringBuffer resultado= new StringBuffer("No puedo manejar una respuesta de tipo ");
         
         if(obj instanceof ConjuntoResultado){
         	resultado= ConjuntoRes(obj);
         }else if(obj instanceof ResultadoSQLfi){
         	
         	// Tipo de t�rmino difuso.
 			int tipoInstruccion = ((ResultadoSQLfi) obj).getTipo();	
 			
 			// Resultado de la ejecuci�n de la sentencia difusa.
 			int resultadoInstruccion = ((ResultadoSQLfi) obj).getResultado();
 			
 			System.out.println("AHHHHHHHHHH "+tipoInstruccion+" "+resultadoInstruccion);
 			
 			switch (tipoInstruccion) {
 
 			case 1:
 				System.out.println("Predicado Difuso Conjunto Creado.\n");
 				break;
 
 			case 2:
 				System.out.println("Predicado Difuso Condicion Creado.\n");
 				break;
 
 			case 3:
 				System.out.println("Cuantificador Difuso Creado.\n");
 				break;
 
 			case 4:
 				System.out.println("Modificador Difuso Potencia Creado.\n");
 				break;
 
 			case 5:
 				System.out.println("Modificador Difuso Traslacion Creado.\n");
 				break;
 
 			case 6:
 				System.out.println("Modificador Difuso Norma Creado.\n");
 				break;
 
 			case 7:
 				System.out.println("Conector Difuso Creado.\n");
 				break;
 
 			case 8:
 				System.out.println("Comparador Difuso Conjunto Creado.\n");
 				break;
 
 			case 9:
 				System.out.println("Comparador Difuso Relacion Creado.\n");
 				break;
 
 			case 10:
 				System.out.println("Tabla Precisa Creada.\n");
 				break;
 
 			case 11:
 				System.out.println("Tabla Difusa Creada.\n");
 				break;
 
 			case 12:
 				System.out.println("Vista Precisa Creada.\n");
 				break;
 
 			case 13:
 				System.out.println("Vista Difusa Creada.");
 				break;
 
 			case 14:
 				System.out.println("Vista Difusa IDB Creada.");
 				break;
 
 			case 15:
 				System.out.println("Asercion Difusa Creada.\n");
 				break;
 
 			case 16:
 				System.out.println("TAD Creado.");
 				break;
 
 			case 17:
 				System.out.println("Insertado " + resultadoInstruccion + " filas(s) en la tabla.");
 				break;
 
 			case 18:
 				System.out.println("Eliminando " + resultadoInstruccion + " filas(s) en la tabla.");
 				break;
 
 			case 19:
 				System.out.println("Actualizando " + resultadoInstruccion + " fila(s) en la tabla.");
 				break;
 
 			case 20:
 				System.out.println("Funcion Difusa Creada.");
 				break;
 
 			case 21:
 				System.out.println("Procedimiento Difuso Creado.");
 				break;
 
 			case 22:
 				System.out.println("Disparador Difuso Creado.");
 				break;
 
 			case 23:
 				System.out.println("Cabecera de Paquete Difuso Creado.");
 				break;
 
 			case 24:
 				System.out.println("Cuerpo de Paquete Difuso Creado.");
 				break;
 
 			case 25:
 				System.out.println("PL/SQL Difuso Executado.");
 				break;
 
 			case 51:
 				System.out.println("Predicado Difuso Eliminado.");
 				break;
 
 			case 52:
 				System.out.println("Modificador Difuso Eliminado.");
 				break;
 
 			case 53:
 				System.out.println("Conector Difuso Eliminado.");
 				break;
 
 			case 54:
 				System.out.println("Comparador Difuso Eliminado.");
 				break;
 
 			case 55:
 				System.out.println("Cuantificador Difuso Eliminado.");
 				break;
 
 			case 56:
 				System.out.println("Asercion Difusa Eliminada.");
 				break;
 
 			case 57:
 				System.out.println("Tabla Precisa Eliminada.");
 				break;
 
 			case 58:
 				System.out.println("Vista Eliminada.");
 				break;
 
 			case 59:
 				System.out.println("Vista Eliminada.");
 				break;
 
 			case 60:
 				System.out.println("Tabla Difusa Eliminada.");
 				break;
 
 			case 61:
 				System.out.println("Vista Difusa Eliminada.");
 				break;
 
 			case 71:
 				System.out.println("Funcion Difusa Eliminada.");
 				break;
 
 			case 72:
 				System.out.println("Procedimiento Difuso Eliminado.");
 				break;
 
 			case 73:
 				System.out.println("Disparador Difuso Eliminado.");
 				break;
 
 			case 74:
 				System.out.println("Cabecera de Paquete Difuso Eliminado.");
 				break;
 
 			case 75:
 				System.out.println("Cuerpo de Paquete Difuso Eliminado.");
 				break;
 
 			default:
 				System.out.println("Sentencia difusa no esperada.");
 			}
 			
         }else if(obj instanceof TerminoDifuso){
         	
         	TerminoDifuso objeto = (TerminoDifuso)obj;
         	
         	if (objeto instanceof PredicadoDifusoConjunto) {
 				PredicadoDifusoConjunto predicado = (PredicadoDifusoConjunto) objeto;
 				
 				System.out.println("Predicado --> " + predicado.getId());
 				imprimirDominio(predicado.getDominio());
 				imprimirConjunto(predicado.getConjunto());
 				
 				return predicado.getId()+" "+predicado.getDominio()+" "+predicado.getConjunto();
 
 			}else{
 				return "No puedo manejar una respuesta de tipo "+objeto.getClass();
 			}
         }else{
         	return "Dio null";
         }
         
         return (resultado.toString());
         
     }
 
 
 
     public static boolean verificarPredicado(String predicado){
         
         try {
             
             Class.forName("org.postgresql.Driver");
             
         }
         catch (ClassNotFoundException e) {
  
             System.out.println("Error, No se pudo conseguir el driver de postgres");
             e.printStackTrace();
             return false;
  
         }
         
          
         Connection connection = null;
         PreparedStatement pst = null;
         ResultSet rs = null;
         
         try {
             
             connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/sqlfi_leonid", "sqlfi","sqlfi");
  
         }catch (SQLException e) {
  
             System.out.println("No se puedo establecer conexion con la base de datos");
             e.printStackTrace();
             return false;
             
         }
         
 
         int existe;
         
         try{
             CallableStatement verificarP = connection.prepareCall("{ ? = call existe_predicado( ? ) }");
             verificarP.registerOutParameter(1, Types.INTEGER);
             verificarP.setString(2,predicado);
             verificarP.execute();
             existe = verificarP.getInt(1);
             connection.close();
         
         }
         catch(SQLException e){
 
             System.out.println("Hola soy bata");
             e.printStackTrace();
             return false;
             
         }
 
         if (existe == 0){
             return false;
         }
         else{
             return true;
         }
         
     }
 
     public static StringBuffer ConjuntoRes(ObjetoSQLfi obj){
 	
         StringBuffer resultado = new StringBuffer();
       
         DecimalFormatSymbols dfs = new DecimalFormatSymbols();
         dfs.setDecimalSeparator('.');
         DecimalFormat df = new DecimalFormat(VariablesAmbiente.FUZZY_MU_DECIMAL_FORMAT,dfs);
         
         int numCols = ((ConjuntoResultado) obj).obtenerMetaDatos().obtenerNumeroColumnas();
         ((ConjuntoResultado) obj).primero();
         
         resultado.append("<table>");
         
         // Se imprime el encabezado
 
         resultado.append("<thead><tr>");
         for (int cont = 2; cont <= numCols; cont++) {
             resultado.append("<th>");
             resultado.append(((ConjuntoResultado) obj).obtenerMetaDatos().obtenerEtiquetaColumna(cont).replace('_',' '));
             resultado.append("</th>");
         }
 
         resultado.append("<th>");
         resultado.append("puntuacion");
         resultado.append("</th>");
         
         resultado.append("</tr></thead>");
         
 
         Object objetoResultado;
         ((ConjuntoResultado) obj).antesDelPrimero();
         // Se imprimen las filas
         while (((ConjuntoResultado) obj).proximo()) {
             resultado.append("<tr>");
             for (int cont = 2; cont <= numCols; cont++) {
                 resultado.append("<td>");
                 objetoResultado = ((ConjuntoResultado) obj).obtenerObjeto(cont);
                 resultado.append(objetoResultado);
                 resultado.append("</td>");
             }
             resultado.append("<td>");
             objetoResultado = ((ConjuntoResultado) obj).obtenerObjeto(1);
             resultado.append(df.format(((Mu) objetoResultado).obtenerValorMu(),new StringBuffer(""),new FieldPosition(0)));
             resultado.append("</td>");
             resultado.append("</tr>");
             
 	            
         }
 
         resultado.append("</table>");
         return resultado;
     }
 
 
     public static Result index() {
         return ok(index.render());
     }
   
     public static Result consultaC(){
 	  
         return ok(questions.render());
     }
   
     public static Result consultaS(){
 	  
         return ok(questions2.render());
     }
     
     public static Result preguntaUnoS(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
         
         // ASUMO QUE ME PASARAN EL SESSION MEDIANTE UN STRING ASI
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado= sesion+"_"+values.get("p1")[0];
         String consulta;
         
         
         // SI EXISTE UN PREDICADO PERSONALIZADO EN LA BD USARLO
 
         if(verificarPredicado(predicado)){
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vmat_dificultad  " +
                 "WHERE promedio = " + predicado +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
             
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vmat_dificultad  " +
                 "WHERE promedio = " + values.get("p1")[0] +" ;";
             
         }
         
         
         
         String respuesta=EjecutarConsulta(consulta);
         return ok(respuestas.render(respuesta));
     }
 
     public static Result preguntaDosS(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
  
         // ASUMO QUE ME PASARAN EL SESSION MEDIANTE UN STRING ASI
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado= sesion+"_"+values.get("p2")[0];
         String consulta;
         
         		
         if(verificarPredicado(predicado)){
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vmat_esfuerzo  " +
                 "WHERE promedio = " + predicado +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
             
             consulta = "SELECT cod_materia, nombre_materia " +
             "FROM vmat_esfuerzo  " +
             "WHERE promedio = " + values.get("p2")[0] +" ;";
         
         }
         
         String respuesta=EjecutarConsulta(consulta);
         
         
         return ok(respuestas.render(respuesta));
     }
 
     public static Result preguntaTresS(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
  
         
         // ASUMO QUE ME PASARAN EL SESSION MEDIANTE UN STRING ASI
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado= sesion+"_"+values.get("p3")[0];
         String consulta;
         
         		
         if(verificarPredicado(predicado)){
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vmat_utilidad  " +
                 "WHERE promedio = " + predicado +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
         
             consulta = "SELECT cod_materia, nombre_materia " +
                 "FROM vmat_utilidad  " +
                 "WHERE promedio = " + values.get("p3")[0] +" ;";
         }        
 
         String respuesta=EjecutarConsulta(consulta);
         
         
         return ok(respuestas.render(respuesta));
     }
 
     public static Result preguntaCuatroS(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
         
         		
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado= sesion+"_"+values.get("p4")[0];
         String consulta;
         
         		
         if(verificarPredicado(predicado)){
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vprof_calidad  " +
                 "WHERE promedio = " + predicado +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
             
             consulta = "SELECT nombre_profesor " +
             "FROM vprof_calidad  " +
             "WHERE promedio = " + values.get("p4")[0] +" ;";
         
         }
         String respuesta=EjecutarConsulta(consulta);
         
         
         return ok(respuestas.render(respuesta));
     }
 
     public static Result preguntaCincoS(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
         
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado= sesion+"_"+values.get("p5")[0];
         String consulta;
         
         		
         if(verificarPredicado(predicado)){
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vmat_expectativa  " +
                 "WHERE promedio = " + predicado +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
         
             consulta = "SELECT cod_materia, nombre_materia " +
                 "FROM vmat_expectativa  " +
                 "WHERE promedio = " + values.get("p5")[0] +" ;";
             
         }
         
         String respuesta=EjecutarConsulta(consulta);
                 
         return ok(respuestas.render(respuesta));
     }
     
     public static Result preguntaSeisS(){
 	  
      
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
         
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado= sesion+"_"+values.get("p6")[0];
         String consulta;
         
         		
         if(verificarPredicado(predicado)){
             consulta="SELECT cod_materia, nombre_materia " +
                 "FROM vmat_preparacion  " +
                 "WHERE promedio = " + predicado +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
         
             consulta = "SELECT cod_materia, nombre_materia " +
                 "FROM vmat_preparacion  " +
                 "WHERE promedio = " + values.get("p6")[0] +" ;";
         
         }
         
         String respuesta=EjecutarConsulta(consulta);
         
         return ok(respuestas.render(respuesta));
  
     }
 
     public static Result preguntaUnoC(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
  
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado1= sesion+"_"+values.get("p1parte1")[0];
         String predicado2= sesion+"_"+values.get("p1parte2")[0];
         String consulta;		
 
         
         if(verificarPredicado(predicado1) && verificarPredicado(predicado2)){
             consulta="SELECT p.nombre_profesor, pa.codigo, d.nombre_materia " +
             "FROM profesor_asignatura pa, vprof_calidad p , vmat_dificultad d " +
             "WHERE pa.prof_cedula = p.ci_profesor and " + 
             "pa.codigo = d.cod_materia and " + 
             "p.promedio = " + predicado2 + " and d.promedio = " + predicado1 +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
         
             consulta = "SELECT p.nombre_profesor, pa.codigo, d.nombre_materia " +
                 "FROM profesor_asignatura pa, vprof_calidad p , vmat_dificultad d " +
                 "WHERE pa.prof_cedula = p.ci_profesor and " + 
                 "pa.codigo = d.cod_materia and " + 
                 "p.promedio = " + values.get("p1parte2")[0] + " and d.promedio = " + values.get("p1parte1")[0] +" ;";
         }
         
         String respuesta=EjecutarConsulta(consulta);
         
         
         return ok(respuestas.render(respuesta));
     }
 
     public static Result preguntaDosC(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
  
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado1= sesion+"_"+values.get("p2parte1")[0];
         String predicado2= sesion+"_"+values.get("p2parte2")[0];
         String consulta;		
 
         
         if(verificarPredicado(predicado1) && verificarPredicado(predicado2)){
             consulta="SELECT e.cod_materia, e.nombre_materia " + 
                 "FROM vmat_esfuerzo e, vmat_utilidad u " +
                 "WHERE e.cod_materia = u.cod_materia and " +
                 "e.promedio = " + predicado2 + " and u.promedio = " + predicado1 +" ;";
         }
         // DE LO CONTRARIO USAR LOS DEFAULTS QUE NO TIENEN UN PREFIJO CON EL ID DE UN USUARIO
         else{
         
             consulta = "SELECT e.cod_materia, e.nombre_materia " + 
                 "FROM vmat_esfuerzo e, vmat_utilidad u " +
                 "WHERE e.cod_materia = u.cod_materia and " +
                 "e.promedio = " + values.get("p2parte2")[0] + " and u.promedio = " + values.get("p2parte1")[0] +" ;";
         
             
         }
         String respuesta=EjecutarConsulta(consulta);
         
         return ok(respuestas.render(respuesta));
     }
 
     public static Result preguntaTresC(){
 	  
         final Map<String, String[]> values = request().body().asFormUrlEncoded();
  
         String sesion="0741051";
 
         // LOS PREDICADOS PERSONALIZADOS SON DE LA FORMA id_predicado, EJ: 0741051_dificultad_alto
         String predicado1= sesion+"_"+values.get("p3parte1")[0];
         String predicado2= sesion+"_"+values.get("p3parte2")[0];
         String consulta;		
 
         
         if(verificarPredicado(predicado1) && verificarPredicado(predicado2)){
             consulta="SELECT p.nombre_profesor, pa.codigo, u.nombre_materia " +
                 "FROM profesor_asignatura pa, vprof_calidad p , vmat_utilidad u " +
                 "WHERE pa.prof_cedula = p.ci_profesor and " + 
                 "pa.codigo = u.cod_materia and " + 
                 "p.promedio = " + predicado2 + " and u.promedio = " + predicado1 + " ;";
         }
         //PROFESOR VS UTILIDAD
         else{
             consulta = "SELECT p.nombre_profesor, pa.codigo, u.nombre_materia " +
                 "FROM profesor_asignatura pa, vprof_calidad p , vmat_utilidad u " +
                 "WHERE pa.prof_cedula = p.ci_profesor and " + 
                 "pa.codigo = u.cod_materia and " + 
                 "p.promedio = " + values.get("p3parte2")[0] + " and u.promedio = " + values.get("p3parte1")[0] + " ;";
         
         }
         String respuesta=EjecutarConsulta(consulta);
         
         return ok(respuestas.render(respuesta));
     }
   
 
     //Funcion que actualiza los predicados
     public static Result confighandle(){
     	
 	        final Map<String, String[]> values = request().body().asFormUrlEncoded();
 	        
 	        String pred = values.get("pred")[0];
 	        
	        String sesion=session("id");
 	        String respuesta="";
 	        
 	        String predicados []= { "dificultad_"+pred+"_"+sesion,
 	                                "calidad_prof_"+pred+"_"+sesion,
 	                                "utilidad_"+pred+"_"+sesion,
 	                                "esfuerzo_"+pred+"_"+sesion,
 	                                "preparacion_"+pred+"_"+sesion,
 	                                "expectativa_"+pred+"_"+sesion};
 	        
 	        System.out.println(predicados[0]);
 	        
 	        for(int i=0; i<predicados.length; i++){
 	        	
 	        	respuesta=EjecutarConsulta("DESC "+predicados[i]+" ;");
 	 	        System.out.println("PRED ORIGINAL "+ respuesta);
 	        	
 	        	respuesta=EjecutarConsulta("DROP PREDICATE " + predicados[i] +" ;");
 	        	
 	        	System.out.println("CREATE FUZZY PREDICATE "+ predicados[i] + " ON 1 .. 5 AS " + 
 	        			"( "+ values.get("p1")[0] +" , "+
 	        			values.get("p2")[0] +" , "+
 	        			values.get("p3")[0] +" , "+
 	        			values.get("p4")[0] +" ) ; ");
 	        	
 	        	respuesta=EjecutarConsulta("CREATE FUZZY PREDICATE "+ predicados[i] + " ON 1 .. 5 AS " + 
 	        			"( "+ values.get("p1")[0] +" , "+
 	        			values.get("p2")[0] +" , "+
 	        			values.get("p3")[0] +" , "+
 	        			values.get("p4")[0] +" ) ; ");
 	        	
 	        	respuesta=EjecutarConsulta("DESC "+predicados[i]+" ;");
 		        System.out.println("PRED MODIFICADO "+ respuesta);
 	        }
 
 	        return ok(respuestas.render("<h1 style='margin-top: 50px;text-align: center;'>Su configuración ha sido procesada</h1>"));     
     }
     
     public static Result config(){
     	
     	return ok(config.render());
     }
   
     public static Result updatePredicate(){
         return TODO;
     }
   
 }
