 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import java.text.SimpleDateFormat;
 import java.io.File;
 import java.util.Date;
 
 public class Utilitaire
 {
    
     private static final int SUCCESS = 0;
     private static final int COMMANDE_ERRONEE = 100;
     private static final int NOMBRE_PARAM_LESS = 200; 
     private static final int NOMBRE_PARAM_SUP = 201;
     private static final int PARAM_INCORRECTE = 202;
     private static final int IMAGE_INEXISTANTE = 203;
     private static final int COULEUR_INEXISTANTE = 204;
     
     
     /**
      *  Test si la chaîne de caractère est un entier
      *  @param s Chaîne de caractère
      *  @return Si la chaîne est un entier
      */
     public static boolean isInt(String s){
         try{
             Integer.parseInt(s);        
     	}
     	catch(NumberFormatException e){
             StockageDonnee.setParamErreur(s, true);
     		return false;
     	}
         return true;
     }
     
     /**
      *  Test si le tableau de chaîne de caractère ne contient que des entiers
      *  @param s Tableau de chaîne de caractère
      *  @return Si toutes les cases du tableau sont des entiers
      */
     public static boolean isInt(String[] s){
         int i = 0;
         try{
             for ( String string_to_parseint : s )
             {
                 Integer.parseInt(string_to_parseint);
                 i++;
             }
     	}
     	catch(NumberFormatException e){
             StockageDonnee.setParamErreur(s[i], true);
     		return false;
     	}
         return true;
     }
 
     /**
      *  Parseur spécial pour la fonction REPEAT
      *  @param args Correspond aux arguments donnés par la fonction
      *  @return Un tableau d'argument
      */
     public static String[] parseRepeat(String args)
     {
         int first_index = args.indexOf("[");
         int last_index = args.lastIndexOf("]");
 
         if ( first_index >= 0 && last_index >= first_index )
         {
             args = args.substring(first_index+1, last_index);
         }
         
         first_index = args.indexOf("[");
         last_index = args.lastIndexOf("]");
         String tmp = "";
 
         if ( first_index >= 0 && last_index >= first_index )
         {
             tmp = args.substring(first_index, last_index+1).replaceAll(";", "x00AB");
             args = args.substring(0, first_index) + tmp;
         }
 
         String[] args_split = args.split(";");
         int i = 0;
         while ( i < args_split.length )
         {
             if ( args_split[i].indexOf("x00AB") >= 0 )
             {
                 args_split[i] = args_split[i].replaceAll("x00AB", ";");
             }
             i++;
         }
 
         return args_split;
 
     }
 
     /**
      *  Renvoie la date courante selon le format : yy-MM-yy_H-mm-ss
      *  @return La date courante
      */
     public static String getCurDate()
     {
         String format = "yy-MM-yy_H-mm-ss";
         SimpleDateFormat formater = new SimpleDateFormat(format);
         Date date = new java.util.Date();
 
         return formater.format(date);
     }
 
     /**
      *  Renvoie un JFileChooser selon les règles édictées par les paramètres
      *  @param description Description des choix possibles
      *  @param regex Expression régulière correspondant aux choix possibles
      */
     public static JFileChooser getChooser(String description, String[] regex)
     {
         JFileChooser chooser = new JFileChooser();
         chooser.setCurrentDirectory( new File( System.getProperty("user.dir") ).getParentFile() );
 
         ExtensionFileFilter filter = new ExtensionFileFilter(description, regex);
         chooser.setFileFilter(filter);
         chooser.addChoosableFileFilter(filter);
        
         return chooser;
     }
 
     /**
      *  Renvoie la réponse de l'utilisateur
      *  @param msg_dialog Le message qui sera afficher à l'écran
      *  @param title Titre de la fenêtre de dialogue
      */
     public static int getOptionPane(String msg_dialog, String title)
     {
         JOptionPane option_pane = new JOptionPane();
             
         return  option_pane.showConfirmDialog(null,
                 msg_dialog,
                 title,
                 JOptionPane.YES_NO_CANCEL_OPTION,
                 JOptionPane.QUESTION_MESSAGE);
     }
 
     public static boolean isACommand(String command)
     {
         return StockageDonnee.getNumeroFonction(command) != -1; 
     }
 
     public static int correctArguments(String command, String args)
     {
         String[] splited_args = args.split(" ");
 
         switch( StockageDonnee.getNumeroFonction(command) )
         {
             /*  Commande ne demandant aucun argument    */
             case 0:
             case 1:
             case 2:
             case 3:
             case 4:
             case 5:
             case 6:
             case 7:
             case 8:
             case 21:
             case 28:
             case 29:
             case 31:
                 return ( splited_args[0] == "" ? SUCCESS : NOMBRE_PARAM_SUP );
        
             /*  Commande requierant un seul paramètre devant être un entier */
             case 9:
             case 12:
             case 13:
             case 15:
             case 19:
             case 20:
                 if ( splited_args.length > 1 )
                 {
                     return NOMBRE_PARAM_SUP;
                 }
                 if ( splited_args[0].equals("") )
                 {
                     return NOMBRE_PARAM_LESS;
                 }
 
                 if ( !isInt( splited_args[0] ) )
                 {
                     return PARAM_INCORRECTE;
                 }
 
                 return SUCCESS;
 
             /*  Commande requierant un seul paramètre entier, ou fonctionnant sans [valeur par défaut : 1]  */
             case 10:
             case 11:
                 if ( splited_args.length > 1 )
                 {
                     return NOMBRE_PARAM_SUP;
                 }
                 if ( !isInt( splited_args[0] ) && !splited_args[0].equals("") )
                 {
                     return PARAM_INCORRECTE;
                 }
 
                 return SUCCESS;
 
             /*  Commande requierant un paramètre en chaîne de caractère ou sans. [Peut utiliser les guillemets] */
             case 22:
             case 23:
             case 24:
             case 25:
             case 26:
             case 30:
                 if ( splited_args.length > 1 )
                 {
                     return NOMBRE_PARAM_SUP;
                 }
 
                 return SUCCESS;
 
             /*  Commande requierant deux paramètres entier  */
             case 14:
                 if ( splited_args.length > 2 )
                 {
                     return NOMBRE_PARAM_SUP;
                 }
                 else if ( splited_args.length < 2 )
                 {
                     return NOMBRE_PARAM_LESS;
                 }
 
                 if ( !isInt( new String[]{ splited_args[0], splited_args[1] } ) )
                 {
                     return PARAM_INCORRECTE;
                 }
 
                 return SUCCESS;
     
             /*  Commande requierant 3 paramètres entier ou 1 chaîne de caractère */
             case 16:
             case 17:
                 if ( (splited_args.length > 3) )
                 {
                     return NOMBRE_PARAM_SUP;
                 }
                 
                 if ( splited_args.length == 3 )
                 {
                     if ( !isInt( new String[]{ splited_args[0], splited_args[1], splited_args[2] } ) )
                     {
                         return PARAM_INCORRECTE;
                     }
                 }
                 else if ( splited_args.length == 1 );
                 else
                 {
                     return PARAM_INCORRECTE; // A CHANGER
                 }
 
                 return SUCCESS;
 
            /*  Cas particulier pour la fonction REPEAT */
             case 27:
                if ( splited_args.length < 3 )
                {
                    return NOMBRE_PARAM_LESS;
                }

                 String[] command_list = parseRepeat(args);
 
                 for ( String cmd : command_list )
                 {
                     String[] tmp = cmd.trim().split(" ", 2);
 
                     int retour = correctArguments(tmp[0], (tmp.length > 1 ? tmp[1] : ""));
 
                     if ( retour != SUCCESS )
                     {
                         return retour;
                     }
                 }
 
                 return SUCCESS;
    
            /*  Cas particulier pour la fonction DOFIGURE   */
            case 18:


 
             default:
                 return COMMANDE_ERRONEE;
         }
 
     }
 }
