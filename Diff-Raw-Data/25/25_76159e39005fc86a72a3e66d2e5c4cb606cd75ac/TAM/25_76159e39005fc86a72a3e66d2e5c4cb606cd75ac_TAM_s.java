 package mjc.gc;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 public class TAM extends ABSTRACTMACHINE {
 /*  private String nom; */
 /* A FAIRE : Génération FIN, NEW ...
 /*  public TAM(String fname) {
 if (fname.endsWith(".mjava")) {
 nom = fname.substring(0, fname.length() - 5);
 } else {
 nom = fname;
 }
 } */
    
     // compteur pour le generateur d'etiquettes OK
     private static int cpt = 0;
    
     public TAM() {
 
     }
     // genere une etiquette differente des autres OK
     public String genEtiq() {
         return "X" + cpt++;
     }
 
     //**** LES VARIABLES ******//
     public String genCst(String v) {
         return "\tLOADL " + v + "\n";
     }
 
     //genere le code pour un attribut lol
     public String genAtt(String s, VAR i, int depvt) {
         return "; appel variable "+s+"\n"
             +"\tLOADL " + i.getDep() + depvt +"\n"
             +"\tSUBR IAdd\n";
     }
 
     // genere le code pour les decl de var locales OK
     public String genVarLoc(String n, VAR i, String affx) {
         int taille = i.getType().getTaille();
         if (affx.equals("")) {
             return "     ; decl de var loc sans init " + n + " en " + i.getDep() + "/" + i.getReg() + " taille = "
                 + taille + "\n"
                 + "\tPUSH " + taille + "\n";
         } else {
             return "     ;   decl de var loc avec init " + n + " en " + i.getDep() + "/" + i.getReg() + " taille = "
                 + taille + "\n"
                 + affx;
         }
     }
 
     // genere le code pour une declaration d'attributs OK
     public String genDeclAtt(String n, VAR i) {
         int taille = i.getType().getTaille();
         return "   ; decl d'att " + n + " en " + i.getDep() + "/" + i.getReg() + " taille = "
             + taille + "\n";
     }
 
     //**** METHODES *****//
 
     // genere le code pour une declaration de constructeur
     public String genDeclCons(String n, METHODE i) {
         int taille = i.getReturnType().getTaille();
         i.setLabel("X" + cpt);
         return genEtiq() + " ; decl de cons " + n +" taille " + taille + "\n"; 
     }
 
 
     // genere le code pour une declaration de methode
     public String genDeclMet(String n, METHODE i) {
         int taille = i.getReturnType().getTaille();
         i.setLabel("X" + cpt);
         if (n.equals("main")) {
             return "main ; decl de met main taille " + taille + "\n";
         } else {
             return genEtiq() + " ; decl de met " + n +" taille " + taille + "\n";
         }
     }
 
     // generation Call de methode
     public  String genCall(int MetNum, String metname) {
 	return  "   ; call de fonction " + metname + "\n"
                 +"\tLOADL " + MetNum + "\n"                                      //deplacement de la méthode voulue
                 +"\tSUBR IAdd\n"                                                 //adresse finale de la methode voulue
                 +"\tLOADI (1)\n"                                                 //chargement de l'etiquette de la methode en sommet de pile
                 +"\tCALLI\n";                                                    //appel à la methode	
     }
 
     // RETURN String : nom de fonction,  Liste d'arg, Variable retour
     public String genReturn(String code, ARGLIST ltype, DTYPE ret) {
         return "     ; return de fonction \n"
             + code +"\tRETURN " + "(" + ret.getTaille() + ") " + ltype.getTaille() +"\n";
     }
   
     //**** CLASSES *****//
 
     // genere le code pour une declaration de classe OK
     public String genClasse(String nom) {
         return " ; decl de classe " + nom +"\n";
     }
 
     //**** MEMOIRE *****//  
     // generation d'adresse OK
     public String genAdr(String nom, int dep, String reg) {
         return "\tLOADA " + dep + "[" + reg + "]"
             +"   ; chargement de l'adresse de " + nom + " de dep " + dep + " et de registre " + reg + "\n";   
     }
 
 
     public String genMalloc(int taille) {
         return "\tLOADL " + taille + "\n" + "\tSUBR Malloc\n";
     }
 
     public String genVTables(CLASSE pointedclass, CLASSE realclass) {
         // cette fonction s'appelle après le genMalloc, donc l'adresse est à -1[ST]
         StringBuffer buf = new StringBuffer();
         ArrayList<String> vtable = pointedclass.createVtable(realclass);
         int incr=0;
         for(String met : vtable) {
             buf.append("\tLOADL \""+met+"\"\n");
             buf.append("\tLOAD (1) -2[ST]\n");
             buf.append("\tLOADL "+incr+"\n");
             buf.append("\tSUBR IAdd\n");
             buf.append("\tSTOREI (1)\n");
             incr++;
         }
         return "      ; creation de Vtable\n"
 	    + buf.toString();
     }
 
     public String genFree(int i) {
         return "     ; liberation des var locales\n"
             +"\tPOP(0)" + i + "\n";
     }
 
    public String genMem(int dep, int taille) {
        return "\tLOAD(" + taille + ") " + dep + "[SB]\n";
    }

     public String genReadMem(int taille) {
         return "\tLOADI(" + taille + ")" + "      ; lecture de l'adresse\n";
     }
 
     public String genWriteMem(int taille) {
         return "\tSTOREI(" + taille + ")" + "     ; ecriture a l'adresse\n";
     }
 
 
  
     // STUFFS OK
     public String genFin() {
         return "\tHALT\n";
     }
   
     public String genDeb() {
         return "\tCALL (LB) main\n";
     }
 
     public String genComment(String c){
         return "; " + c + "\n";
     }
 
 
     // OPERATEURS OK
     public String genIf(String code, String code2, String code3) {
         String sinon = genEtiq();
         String fin = genEtiq();
         return "\t; if\n" + code + "\n" + "\tJUMPIF(0) " + sinon + "\n" + code2
             + "\n" + "\tJUMP " + fin + "\n" + sinon + "\n" + code3 + "\n"
             + fin + "\n" + "\t; fin if\n";
     }
 
 
     public String genOpAdd() {
         return "\tSUBR IAdd\n";
     }
 
     public String genOpSub() {
         return "\tSUBR ISub\n";
     }
 
     public String genOpMul() {
         return "\tSUBR IMul\n";
     }
 
     public String genOpDiv() {
         return "\tSUBR IDiv\n";
     }
 
     public String genOpMod() {
         return "\tSUBR IMod\n";
     }
 
     public String genOpEq() {
         return "\tSUBR IEq\n";
     }
 
     public String genOpNeq() {
         return "\tSUBR INeq\n";
     }
 
     public String genOpLss() {
         return "\tSUBR ILss\n";
     }
 
     public String genOpLeq() {
         return "\tSUBR ILeq\n";
     }
 
     public String genOpGtr() {
         return "\tSUBR IGtr\n";
     }
 
     public String genOpGeq() {
         return "\tSUBR IGeq\n";
     }
 
     public String genOpNeg() {
         return "\tSUBR BNeg\n";
     }
 
     public String genOpOr() {
         return "\tSUBR BOr\n";
     }
 
     public String genOpAnd() {
         return "\tSUBR BAnd\n";
     }
 
 }
