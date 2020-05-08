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
     	String opciones = (String) opcionesin[0];
         String phrase = "%^C_EJECUTA_COMANDO.\"" + opciones + "\"";
         Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 
     public static void commandFormatSintax(Object[] sintaxisin) {
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
             phrase = "XH:VAR_DATE SH:FECHA XH:CASI_FECHA"
                     + "@%^X_OPERACION_SINTAX.”XH:VAR_DATE "
                     + "+ SH:FECHA = XH:CASI_FECHA”";
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
             phrase = "XH:VAR_UNO != EH:ORIGEN XH:VAR_TRES XH:VAR_DOS XH:ESLOGAN@"
                     + "%^X_OPERACION_SINTAX.”XH:VAR_UNO != EH:ORIGEN # XH:VAR_TRES"
                     + " = XH:ESLOGAN # XH:VAR_DOS = XH:ESLOGAN”";
         }
         //XH:VAR_UNO EH:ORIGEN XH:VAR_TRES XH:VAR_DOS XH:ESLOGAN
         Deliver.deliver(where, phrase);
     }
     
     public static void basicProcessingFormat(Object[] Fieldsin) {
         //  %^_TRATAR_.” | %^I_TRATAR_INSERT.”
         //Field(0) = Nombre de instruccion
         //Field(s%3=0) = VariableComp1 = [X:|S:|C:|E:][N:|H:]NAME_op1
         //Field(s%3=1) = operation = ',' ',,'
         //Field(s%3=2) = VariableComp2 = [X:|S:|C:|E:][N:|H:]NAME_op2
         String Fields = (String) Fieldsin[0];
         
         String phrase;
         int where;
         
         try {
             where = Deliver.SYNTAX_AREA;
             String[] splitfields = Fields.split(" ");
             String inicial = splitfields[0].substring(0, 1);
             
             if (inicial.equalsIgnoreCase("Q")) {inicial = "X";}
             
             phrase = "%^" + inicial.toUpperCase() + "_TRATAR_"
                     + splitfields[0].toUpperCase() + ".\"";
             
             for (int i = 1; i < splitfields.length - 3; i += 3) {
                 phrase += splitfields[i] + " " + splitfields[i+1] + " " 
                         + splitfields[i+2] + " # "; 
             }
             phrase += splitfields[splitfields.length - 3] + " " 
                     + splitfields[splitfields.length - 2] + " "
                     + splitfields[splitfields.length - 1];
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
             phrase = "insert XH:VAR_VACIA , EH:ORIGEN XH:CONDI , FALSE@"
                     + "%^I_TRATAR_INSERT.”XH:VAR_VACIA , EH:ORIGEN # XH:CONDI , FALSE”";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     public static void newProcessingFormat(Object[] Fieldsin) {
         //Filed(0)     = Nombre de campo
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
             String[] splitfields = Fields.split(" ; ");
             String[] splitfields1 = splitfields[0].split(" ");
             
             String inicial = splitfields1[0].substring(0, 1);
             
             if (inicial.equalsIgnoreCase("Q")) {inicial = "X";}
             
             phrase = "%^" + inicial.toUpperCase() + "_TRATAR_"
                     + splitfields1[0].toUpperCase() + ".\"";
 
             for (int i = 1; i < splitfields1.length - 4; i += 4) {
                 phrase += splitfields1[i] + " " + splitfields1[i+1] + " " 
                         + splitfields1[i+2] + " = " + splitfields1[i+3].toUpperCase() + " ; ";
             }
             
             phrase += splitfields1[splitfields1.length-4] 
                     + " " + splitfields1[splitfields1.length-3] 
                     + " " + splitfields1[splitfields1.length-2]
                     + " = " + splitfields1[splitfields1.length-1].toUpperCase();
             
             phrase += " # " + splitfields[1];
             phrase += "\"";
         } catch (Exception e) {
             where = Deliver.ERROR;
            phrase = "insert (EH:ORIGEN == ‘MI_CENTRAL’ A)<sup>N</sup> !(C | B) & A@"
                     + "%^I_TRATAR_INSERT.”EH:ORIGEN == ‘MI_CENTRAL’ = A ;"
                     + " XN:NUMERO >= EN:SECUENCIA = B ; XN:NUM > 100 = C #"
                     + " !(C | B) & A”";
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
             phrase = "REGIS_A ; CAUSA_A@"
                     + "%^X_ASOCIAR_SINTAX.\"REGIS_A # CAUSA_A\"";
         }
         
         Deliver.deliver(where, phrase);
     }
     
     public static void specialEventFormat(Object[] templatein) {
         String template = (String) templatein[0];
         String phrase = "_SPECIAL_.\"" + template +"\"";
         Deliver.deliver(Deliver.SYNTAX_AREA, phrase);
     }
 }
