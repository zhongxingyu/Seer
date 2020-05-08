 package business;
//import controller.Controller;
 import deliver.Deliver;
 /*
  * This class parses the given input and output an operation type-based syntax
  * form.
  */
 
 /**
  *
  * @author Kiwi
  */
 public class Operaciones {
 
     public static void orderFormat(Object[] ordersin) {
     	String phrase = "%^X_ORDEN_SINTAX.\"";
         
         String orders = (String) ordersin[0];
         
         String[] splitOrders = orders.split(" ");   
         for (int i = 0; i < splitOrders.length - 1; i++) {
             phrase += splitOrders[i] + ", ";
         }
         phrase += splitOrders[splitOrders.length- 1];
         
         phrase += "\"";
         Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 
     public static void commandFormatExe(Object[] opcionesin) {
     	// TODO: En la interfaz hay que poner un ejemplo de esto, para recordar las posibles operaciones.
     	String opciones = (String) opcionesin[0];
         String phrase = "%^C_EJECUTA_COMANDO.\"" + opciones + "\"";
         Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 
     public static void commandFormatSintax(Object[] sintaxisin) {
         // TODO: En la interfaz hay que poner un ejemplo de esto, para recordar las posibles operaciones.
     	String sintaxis = (String) sintaxisin[0];
         String phrase = "%^C_SINTAXIS_COMANDO.\"" + sintaxis + "\"";
     	Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 
     public static void failureManagerFormat(Object[] variablesin) {
         // Ej: variables = S:NUM_EQU|X:REMOTA|Campo5|E:SOC
         String variables = (String) variablesin[0];
         
         String phrase = "%^CAMPOS_GF.\"";
         
         String[] splitvars = variables.split(" ");   
         for (int i = 0; i < splitvars.length - 1; i++) {
             phrase += splitvars[i] + ",";
         }
         phrase += splitvars[splitvars.length- 1];
         
         phrase += "\"";
     	Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
     
     public static void concatOperationSintax(Object[] variablesin) {
         // Ej: Variables0 = Input1 = [X:|S:|C:|E:][N:|H:]NAME_op1
         // Ej: Variables1 = Input2 = [X:|S:|C:|E:][N:|H:]NAME_op2
         // Ej: Variables2 = Output = [X:|S:|C:|E:][N:|H:]NAME_res
         String variables = (String) variablesin[0];
         
        String phrase;
        int where;
         
         try {
             where = Deliver.SYNTAX_AREA;
             phrase = "%^X_OPERACION_SINTAX.\"";
             String[] splitvars = variables.split(" "); 
             phrase += splitvars[0] + " + " + splitvars[1] + 
                     " = " + splitvars[2];
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
             phrase = "var1 var2 var3@%^X_OPERACION_SINTAX"
                     + ".\"var1 + var2 = var3\"";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     public static void compAsigOperationSintax(Object[] Fieldsin) {
         //Field0 = VariableComp1 = [X:|S:|C:|E:][N:|H:]NAME_op1
         //Field1 = operation = ‘==’, ‘!=’, ‘>=’, ‘<=’, ‘>’ , ‘<’ , ‘$’ y ‘!!’
         //Field2 = VariableComp2 = [X:|S:|C:|E:][N:|H:]NAME_op2
         //Field3 = VariableHit = [X:|S:|C:|E:][N:|H:]NAME_Hit
         //Field4 = VariableMiss = [X:|S:|C:|E:][N:|H:]NAME_Miss
         //Field5 = VariableAsig =[X:|S:|C:|E:][N:|H:]NAME_Asig
         String Fields = (String) Fieldsin[0];
         
        String phrase;
        int where;
         try {
             where = Deliver.SYNTAX_AREA;
             phrase = "%^X_OPERACION_SINTAX.\"";
             String[] splitfields = Fields.split(" "); 
             phrase += splitfields[0] + " " + splitfields[1] + " " + splitfields[2];        
             for (int i = 0; i < 2; i++) {
                 phrase += " # " + splitfields[3+i] + " = " + splitfields[5];
             }
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
             phrase = "var1 var2 var3 var4 var5 var6@%^X_OPERACION_SINTAX"
                     + ".\"var1 var2 var3 # var4 = var6 # var5 = var6\"";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     public static void basicProcessingFormat(Object[] Fieldsin) {
         //  %^_TRATAR_.” | %^I_TRATAR_INSERT.”
         //Field(s%3=0) = VariableComp1 = [X:|S:|C:|E:][N:|H:]NAME_op1
         //Field(s%3=1) = operation = ',' ',,'
         //Field(s%3=2) = VariableComp2 = [X:|S:|C:|E:][N:|H:]NAME_op2
         String Fields = (String) Fieldsin[0];
         
        String phrase;
        int where;
         
         try {
             where = Deliver.SYNTAX_AREA;
             phrase = "%^_TRATAR_.\"";
             String[] splitfields = Fields.split(" "); 
             for (int i = 0; i < splitfields.length - 3; i += 3) {
                 phrase += splitfields[i] + " " + splitfields[i+1] + " " 
                         + splitfields[i+2] + " # ";
             }
             phrase += splitfields[splitfields.length - 3] + " " 
                     + splitfields[splitfields.length - 2] + " " 
                     + splitfields[splitfields.length - 1];
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
             phrase = "var1 var2 var3 var4 var5 var6@"
                     + "%^_TRATAR_.\"var1 var2 var3 # var4 var5 var6\"";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     public static void newProcessingFormat(Object[] Fieldsin) {
         //Field(s%4=0) = VariableComp1 = [X:|S:|C:|E:][N:|H:]NAME_op1
         //Field(s%4=1) = operation = '==' '!=' '<' '>' '>=' '<='
         //Field(s%4=2) = VariableComp2 = [X:|S:|C:|E:][N:|H:]NAME_op2
         //Field(s%4=3) = VariableAsig = [A|B|C|...]
         //Field(s-1) = VariableForm = Ej: !(A|B)^C
         String Fields = (String) Fieldsin[0];
         
        String phrase;
        int where;
         
         try {
             where = Deliver.SYNTAX_AREA;
             phrase = "%^_TRATAR_.\"";
             String[] splitfields = Fields.split(" "); 
             for (int i = 0; i < splitfields.length - 5; i += 4) {
                 phrase += splitfields[i] + " " + splitfields[i+1] + " " 
                         + splitfields[i+2] + " = " + splitfields[i+3] + " ; ";
             }
             phrase += splitfields[splitfields.length - 5] + " " 
                     + splitfields[splitfields.length - 4] + " " 
                     + splitfields[splitfields.length - 3] + " = " 
                     + splitfields[splitfields.length - 2] + " # " 
                     + splitfields[splitfields.length - 1];
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
             phrase = "var1 var2 var3 var4 var5@"
                     + "%^_TRATAR_.\"var1 var2 var3 = var4 # var5\"";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     //before 2 ArrayList (ArrayList ReplaceVars, ArrayList NewVars)
     public static void associationFormat(Object[] varsin) {
         // ReplaceVars = variables que se van a sustituir
         // NewVars = Variables nuevas que cogen el contenido de las ReplaceVars
         String vars = (String) varsin[0];
         
        String phrase;
        int where;
         try{
             where = Deliver.SYNTAX_AREA;
             phrase = "%^X_ASOCIAR_SINTAX.\"";
             String[] parts = vars.split(" ; "); 
             String[] ReplaceVars = parts[0].split(" ");
             String[] NewVars = parts[1].split(" "); 
             for(int i = 0; i < ReplaceVars.length - 1; i++) {
                 phrase += ReplaceVars[i] + ", ";
             }
             phrase += ReplaceVars[ReplaceVars.length - 1] + " # ";
             for(int i = 0; i < NewVars.length - 1; i++) {
                 phrase += NewVars[i] + ", ";
             }
             phrase += NewVars[NewVars.length - 1];
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
             phrase = "var1 var2 ; var3 var4@"
                     + "%^X_ASOCIAR_SINTAX.\"var1, var2 # var3, var4\"";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     public static void specialEventFormat(Object[] templatein) {
         String template = (String) templatein[0];
         String phrase = "_SPECIAL_.\"" + template +"??\"";
         Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 }
