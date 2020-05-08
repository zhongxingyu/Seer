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
 
     public String genEtiqIf() {
         return "I" + cpt++;
     }
 
     //**** LES VARIABLES ******//
     public String genCst(String v) {
         return "\tLOADL " + v + "\n";
     }
     
     public String genCurrentClassPointer() {
        return "\tLOAD (1) -1[LB]\n";
     }
 
     //genere le code pour un attribut lol
     public String genAtt(String s, VAR i) {
         return "; appel attribut (adresse deja empilee) "+s+"\n"
             +"\tLOADL " + i.getDep() +"\n"
             +"\tSUBR IAdd\n";
     }
 
     public String genPopArgs(int argsize) {
         return "; pop des arguments avant le genCall\n"
            +"\tLOAD (1) -" + (argsize+1) + "[ST]\n"
            +"\tPOP ("+ (argsize+1)+") 1";
     }
 
     public String genGetMallocPointer(int argsize) {
         return "; get du malloc pointer au dessus des arguments avant le genCall\n"
             +"\tLOAD (2) -" + (argsize+2) + "[ST]\n";
     }
 
     public String genVar(int dep, int taille,String reg) {
         return "; empilation de pointeur\n"
             +"\tLOAD(" + taille + ") " + dep + "[" + reg + "]\n";
     }
 
     // genere le code pour les decl de var locales OK
     public String genVarLoc(String n, VAR i, String affx) {
         int taille = i.getType().getTaille();
         if (affx.equals("")) {
             return "; decl de var loc sans init " + n + " en " + i.getDep() + "/" + i.getReg() + " taille = "
                 + taille + "\n"
                 + "\tPUSH " + taille + "\n";
         } else {
             return ";   decl de var loc avec init " + n + " en " + i.getDep() + "/" + i.getReg() + " taille = "
                 + taille + "\n"
                 + affx;
         }
     }
 
     // genere le code pour une declaration d'attributs OK
     public String genDeclAtt(String n, VAR i) {
         int taille = i.getType().getTaille();
         return "; decl d'att " + n + " en " + i.getDep() + "/" + i.getReg() + " taille = "
             + taille + "\n";
     }
 
     //**** METHODES *****//
 
     // genere le code pour une declaration de constructeur
     public String genDeclCons(String n, METHODE i) {
         int taille = i.getReturnType().getTaille();
         return i.getLabel() + "; decl de cons " + n +" taille " + taille + "\n"; 
     }
 
 
     // genere le code pour une declaration de methode
     public String genDeclMet(String n, METHODE i) {
         int taille = i.getReturnType().getTaille();
         return i.getLabel() + "; decl de met " + n +" taille " + taille + "\n";
     }
 
     // generation Call de methode
     public  String genCall(int MetNum, String metname) {
         // actuellement en haut de la stack: l'adresse de l'instance (milieu entre vtable/attributs)
         // genCall gère automatiquement l'appel de l'adresse du haut de la stack en tant que premier paramètre.
 	return  "; call de fonction " + metname + "\n"
             +"\tPOP (1) 1\n"
             +"\tLOAD (1) -1[ST]\n"
             +"\tLOADL " + (MetNum+1) + "\n"                                  //deplacement de la méthode voulue
             +"\tSUBR ISub\n"                                                 //adresse finale de la methode voulue
             +"\tLOADI (1)\n"                                                 //chargement de l'etiquette de la methode en sommet de pile
             +"\tLOADL 0\n"
             +"\tLOAD (1) -2[ST]\n"
             +"\tPOP (2) 1\n"
             +"\tCALLI\n";                                                    //appel à la methode	
     }
 
     // generation Call de methode
     public  String genCallI(int MetNum, String metname) {
         // actuellement en haut de la stack: l'adresse de l'instance (milieu entre vtable/attributs)
         // genCall gère automatiquement l'appel de l'adresse du haut de la stack en tant que premier paramètre.
 	return  "; call de fonction d'interface " + metname + "\n"
            +"\tLOAD (1) -2[ST]\n"
            +"\tPOP (2) 1\n"
             +"\tLOADL " + MetNum + "\n"                                  //deplacement de la méthode voulue
             +"\tSUBR IAdd\n"                                                 //adresse finale de la methode voulue
             +"\tLOADI (1)\n"                                                 //chargement de l'etiquette de la methode en sommet de pile
             +"\tLOADL 1\n"
             +"\tSUBR IAdd\n"
             +"\tSUBR ISub\n"                                                 //adresse finale de la methode voulue
             +"\tLOADI (1)\n"                                                 //chargement de l'etiquette de la methode en sommet de pile
             +"\tLOADL 0\n"
             +"\tLOAD (1) -2[ST]\n"
             +"\tPOP (2) 1\n"
             +"\tCALLI\n";  
     }
 
     // RETURN String : nom de fonction,  Liste d'arg, Variable retour
     public String genReturn(String code, ARGLIST ltype, DTYPE ret) {
         return "; return de fonction \n"
             + code +"\tRETURN " + "(" + ret.getTaille() + ") " + (ltype.getTaille()+1) +"\n";
     }
   
     //**** CLASSES *****//
 
     // genere le code pour une declaration de classe OK
     public String genClasse(String nom) {
         return "; decl de classe " + nom +"\n";
     }
 
     public String genMalloc(int taille) {
         return "\tLOADL " + taille + "\n" + "\tSUBR Malloc\n";
     }
 
     public String genVTables(CLASSE pointedclass, CLASSE realclass) {
         // cette fonction s'appelle après le genMalloc, donc l'adresse est à -1[ST]
         StringBuffer buf = new StringBuffer();
         ArrayList<String> vtable = pointedclass.createVtable(realclass);
         int i=0;
         for(i=0;i<vtable.size();i++) {
             String met = vtable.get(vtable.size()-(i+1));
             buf.append("\tLOADA "+met+"\n");
             buf.append("\tLOAD (1) -2[ST]\n");
             buf.append("\tLOADL "+i+"\n");
             buf.append("\tSUBR IAdd\n");
             buf.append("\tSTOREI (1)\n");
         }
         
         // ajout du nombre de méthodes pour placer le pointeur entre la vtable et les attrs
         buf.append("\tLOADL " + i + "\n");
         buf.append("\tSUBR IAdd\n");
         return "; creation de Vtable\n" + buf.toString();
     }
 
     public String genIVTables(CLASSE pointedinter, CLASSE realclass) {
         // cette fonction s'appelle après le genMalloc, donc l'adresse est à -1[ST]
         StringBuffer buf = new StringBuffer();
         ArrayList<Integer> vtable = pointedinter.createIVtable(realclass);
         int i=0;
         for(i=0;i<vtable.size();i++) {
             Integer met = vtable.get(i);
             buf.append("\tLOADL "+met+"\n");
             buf.append("\tLOAD (1) -2[ST]\n");
             buf.append("\tLOADL "+i+"\n");
             buf.append("\tSUBR IAdd\n");
             buf.append("\tSTOREI (1)\n");
         }
         
         return "; creation de Vtable\n" + buf.toString();
     }
 
     public String genSwapVTables() {
         return "\tSTORE (1) -3[ST]\n";
     }
 
     public String genFree(int i) {
         return "; liberation des var locales\n"
             +"\tPOP(0)" + i + "\n";
     }
 
     public String genReadMemRAM(int taille) {
         return "\tLOADI(" + taille + ")" + "     ; lecture a l'adresse\n";
     }
 
     public String genWriteMemRAM(int taille) {
         return "\tSTOREI(" + taille + ")" + "     ; ecriture a l'adresse\n";
     }
 
     public String genWriteMemStack(VAR i) {
         return "\tSTORE(" + i.getTaille() + ")" + i.getDep()+"["+i.getReg()+"]     ; ecriture a l'adresse\n";
     }
 
     // STUFFS OK
     public String genFin() {
         return "\tHALT\n";
     }
   
     //geter l'etiquette de main
     public String genDeb() {
         return "\tCALL (LB) main\n";
     }
 
     public String genComment(String c){
         return "; " + c + "\n";
     }
 
 
     // OPERATEURS OK
     public String genIf(String code, String code2, String code3) {
         String sinon = genEtiqIf();
         String fin = genEtiqIf();
         return "; if\n" + code + "\n" + "\tJUMPIF(0) " + sinon + "\n" + code2
             + "\n" + "\tJUMP " + fin + "\n" + sinon + "\n" + code3 + "\n"
             + fin + "\n" + "; fin if\n";
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
