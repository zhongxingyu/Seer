 /*
  * This class transcribes some given tokens into Syntax Database format.
  */
 package business;
 import deliver.Deliver;
import controller.Controller;
 import java.util.ArrayList;
 
 /**
  *
  * @author Kiwi
  */
 public class BBDD {
     
     /**
      * Method that creates a select-type phrase based on the given input.
      * 
      * @param conexion: true if there is conexion field, false in other cases.
      * @param usuarioBD: User of the DB.
      * @param passwdBD: Passwd of the DB.
      * @param conexionBD: Conexion field of the DB.
      * @param resultadoSelect: Results from the select conection.
      * @param tablaSelect: Table in which the select will be used.
      * @param condicionSelect: Conndition under the selec
      */
   
 public static void selectBD(Object[] what) {
     	// TODO: en la interfaz debe preguntarse qué partes quiere poner.
 	
 	//Object[] what;
 	boolean conexion = (boolean) what[0];
 	String usuarioBD = (String) what[1];
 	String passwdBD = (String) what[2];
 	String conexionBD = (String) what[3];
 	ArrayList resultadoSelect = (ArrayList) what[4];
 	ArrayList tablaSelect = (ArrayList) what[5];
 	String condicionSelect = (String) what[6];
 	
     	String phraseA = "";
     	if(conexion) {
     		phraseA += "%^S_CONEXION_SELECT.\"" + usuarioBD + "," + passwdBD;
     		if (conexionBD.compareTo("") != 0) phraseA += "," + conexionBD;
    		phraseA += "\"";
     	}
     	String phraseB = "%^S_RESULTADO_SELECT.\"";
     	for (int i = 0; i < resultadoSelect.size() - 1; i++) {
     		phraseB += resultadoSelect.get(i) + " , ";
     	}
    	phraseB += resultadoSelect.get(resultadoSelect.size() - 1) + "\"";
     	String phraseC = "%^S_TABLA_SELECT.\"";
     	for (int i = 0; i < tablaSelect.size() - 1; i++) {
     		phraseC += tablaSelect.get(i) + " , ";
     	}
    	phraseC += tablaSelect.get(tablaSelect.size() - 1) + "\"";
     	String phraseD = "%^S_CONDICION_SELECT.\"" + condicionSelect + "\"";
    	String phrase = phraseA + "\n" + phraseB + "\n" + phraseC + "\n" + phraseD + "\n";
     	Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
     
     /**
      * Method that creates a delete-type phrase based on the given input.
      * 
      * @param conexion: true if there is conexion field, false in other cases.
      * @param usuarioBD: user of the database.
      * @param passwdBD: password of the database.
      * @param conexionBD: conexion field of the database.
      * @param tablaDelete: target table of the operation.
      * @param condicionDelete 
      */
     public static void deleteBD(Object[] what) {
         // TODO: en la interfaz debe preguntarse que partes quiere poner.
         // Object[] what;
 	boolean conexion = (boolean) what[0];
 	String usuarioBD = (String) what[1];
 	String passwdBD = (String) what[2];
 	String conexionBD = (String) what[3];
 	String tablaDelete = (String) what[4];
 	String condicionDelete = (String) what[5];
         String phraseA = "";
         if (conexion) {
             phraseA += "%^D_CONEXION_DELETE.\"" + usuarioBD + "," + passwdBD;
             if (conexionBD.compareTo("") != 0) phraseA += "," + conexionBD;
             phraseA += "\"";
         }
         String phraseB = "%^D_TABLA_DELETE.\"" + tablaDelete + "\"";
         String phraseC = "%^D_CONDICION_DELETE.\"" + condicionDelete + "\"";
         String phrase = phraseA + "\n" + phraseB + "\n" + phraseC + "\n";
         Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 
     /**
      * Method that creates a update-type phrase based on the given input.
      * 
      * @param conexion: true if there is conexion field, false in other cases.
      * @param usuarioBD: user of the database.
      * @param passwdBD: password of the database.
      * @param conexionBD: conexion field of the database.
      * @param tablaUpdate: Target of the update operation.
      * @param camposUpdate: Fields which will we modified with the update.
      * @param condicionUpdate: Condition under which the update will run.
      */
     public static void updateBD(Object[] what) {
     	// TODO: en la interfaz debe preguntarse qué partes quiere poner.
     	//Object[] what;
 	boolean conexion = (boolean) what[0];
 	String usuarioBD = (String) what[1];
 	String passwdBD = (String) what[2];
 	String conexionBD = (String) what[3];
 	ArrayList tablaUpdate = (ArrayList) what[4];
 	ArrayList camposUpdate = (ArrayList) what[5];
 	String condicionUpdate = (String) what[6];
 	String phraseA = "";
     	if(conexion) {
     		phraseA += "%^U_CONEXION_UPDATE.\"" + usuarioBD + "," + passwdBD;
     		if (conexionBD.compareTo("") != 0) phraseA += "," + conexionBD;
     		phraseA += "\"";
     	}
     	String phraseB = "%^U_TABLA_UPDATE.\"";
     	for (int i = 0; i < tablaUpdate.size() - 1; i++) {
     		phraseB += tablaUpdate.get(i) + " , ";
     	}
     	phraseB += tablaUpdate.get(tablaUpdate.size() - 1) + "\"";
     	String phraseC = "%^U_CAMPOS_UPDATE.\"";
     	for (int i = 0; i < camposUpdate.size() - 1; i++) {
     		phraseC += camposUpdate.get(i) + " , ";
     	}
     	phraseC += camposUpdate.get(camposUpdate.size() - 1) + "\"";
     	String phraseD = "%^U_CONDICION_UPDATE.\"" + condicionUpdate + "\"";
     	String phrase = phraseA + "\n" + phraseB + "\n" + phraseC + "\n" + phraseD + "\n";
     	Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
     
     /**
      * 
      * @param conexion: true if there is conexion field, false in other cases.
      * @param usuarioBD: user of the database.
      * @param passwdBD: password of the database.
      * @param conexionBD: conexion field of the database.
      * @param sentenciaQuery: Query sentence.
      */
     public static void queryBD(Object[] what) {
     	//Object[] what;
 	boolean conexion = (boolean) what[0];
 	String usuarioBD = (String) what[1];
 	String passwdBD = (String) what[2];
 	String conexionBD = (String) what[3];
 	String sentenciaQuery = (String) what[4];
 	String phraseA = "";
     	if(conexion) {
     		phraseA += "%^X_CONEXION_QUERY.\"" + usuarioBD + "," + passwdBD;
     		if (conexionBD.compareTo("") != 0) phraseA += "," + conexionBD;
     		phraseA += "\"";
     	}
     	String phraseB = "%^X_SENTENCIA_QUERY.\"" + sentenciaQuery + "\"";
     	String phrase = phraseA + "\n" + phraseB + "\n";
     	Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 
     /**
      * 
      * @param conexion: true if there is conexion field, false in other cases.
      * @param usuarioBD: user of the database.
      * @param passwdBD: password of the database.
      * @param conexionBD: conexion field of the database.
      * @param camposInsert: Desired fields of the insert operation.
      * @param tablaInsert: Target table of the insert operation.
      * @param columnasInsert: Target column of the insert operation.
      */
     public static void insertBD(Object[] what) {
     	//Object[] what;
 	boolean conexion = (boolean) what[0];
 	String usuarioBD = (String) what[1];
 	String passwdBD = (String) what[2];
 	String conexionBD = (String) what[3];
 	ArrayList camposInsert = (ArrayList) what[4];
 	String tablaInsert = (String) what[5];
 	ArrayList columnasInsert = (ArrayList) what[6];
 	String phraseA = "";
     	if(conexion) {
     		phraseA += "%^I_CONEXION_UPDATE.\"" + usuarioBD + "," + passwdBD;
     		if (conexionBD.compareTo("") != 0) phraseA += "," + conexionBD;
     		phraseA += "\"";
     	}
         String phraseB = "%^I_CAMPOS_INSERT.\"";
     	for (int i = 0; i < camposInsert.size() - 1; i++) {
     		phraseB += camposInsert.get(i) + " , ";
     	}
     	phraseB += camposInsert.get(camposInsert.size() - 1) + "\"";
         String phraseC = "%^I_TABLA_INSERT.\"" + tablaInsert + "\"";
         String phraseD = "%^I_COLUMNAS_INSERT.\"";
     	for (int i = 0; i < columnasInsert.size() - 1; i++) {
     		phraseD += columnasInsert.get(i) + " , ";
     	}
     	phraseD += columnasInsert.get(columnasInsert.size() - 1) + "\"";
         String phrase = phraseA + "\n" + phraseB + "\n" + phraseC + "\n" + phraseD + "\n";
       	Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
 
     }
 }
