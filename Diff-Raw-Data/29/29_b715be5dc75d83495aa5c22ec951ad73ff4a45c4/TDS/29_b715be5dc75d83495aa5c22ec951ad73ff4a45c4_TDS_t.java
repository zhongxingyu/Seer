 package mjc.gc;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.ArrayList;
 
 public class TDS {
     public HashMap<String, LMETHODE> hmMethode;
     public HashMap<String, VAR> hmVar;
     public HashMap<String, CLASSE> hmClasse;
 
     private TDS parente;
 
     public TDS() {
         this.hmMethode=new HashMap<String,LMETHODE>();
         this.hmVar=new HashMap<String,VAR>();
         this.hmClasse=new HashMap<String,CLASSE>();
         this.parente=null;
     }
 
     public TDS(TDS p) {
         this.hmMethode=new HashMap<String,LMETHODE>();
         this.hmVar=new HashMap<String,VAR>();
         this.hmClasse=new HashMap<String,CLASSE>();
         this.parente = p;
     }
 
     public void setParente(TDS p) {
         parente=p;
     }
 
     public HashMap<String,LMETHODE> getMethodHM() {
         return this.hmMethode;
     }
 
     public VAR chercherLocalementVar(String n) {
         return this.hmVar.get(n);
     }
 
     public int getVariableTaille() {
        Set<Map.Entry<String,VAR>> esi = hmVar.entrySet();
	int finaltaille=0;
        for (Map.Entry<String,VAR> e : esi) {
	    finaltaille=finaltaille+e.getValue().getTaille();
	}   
	return finaltaille;
     }
 
     public VAR chercherGlobalementVar(String n) {
         VAR i = this.chercherLocalementVar(n);
         if (i == null)
             if (this.parente != null)
                 return this.parente.chercherGlobalementVar(n);
         return i;
     }
 
     public void inserer(String n, VAR i) {
         this.hmVar.put(n, i);
     }
 
     public ArrayList<VTABLEENTRY> getAllAccessibleMethods() {
         // utilisé pour la génération des vtables
         ArrayList<VTABLEENTRY> retList;
         if(parente==null) {
             retList = new ArrayList<VTABLEENTRY>();
         } else {
             retList = parente.getAllAccessibleMethods();
         }
 
         Set<Map.Entry<String,LMETHODE>> esi = hmMethode.entrySet();
         for (Map.Entry<String,LMETHODE> e : esi) {
             for(METHODE met : e.getValue()) {
 		// couple e.getKey(), met
 		boolean toadd=true;
 		for(VTABLEENTRY ventry : retList) {
 		    if(ventry.getName().equals(e.getKey()) && ventry.getMethod().getArgs().equals(met.getArgs())) {
 			toadd=false;
 			break;
 		    }
 		}
 		if(toadd) {
 		    retList.add(new VTABLEENTRY(e.getKey(),met));
 		}
             }
         }
         return retList;
     }
 
     public int getTailleAccMeth() {
 	return this.getAllAccessibleMethods().size();
     }
 
     public METHODE chercherLocalementMethod(String n,ARGLIST arg) {
         LMETHODE lm = this.hmMethode.get(n);
         if(lm==null) {
             return null;
         } else {
             return lm.getMethod(arg);
         }
     }
 
     public METHODE chercherGlobalementMethod(String n,ARGLIST arg) {
         METHODE i = chercherLocalementMethod(n,arg);
         if (i == null)
             if (this.parente != null)
                 return parente.chercherGlobalementMethod(n,arg);
         return i;
     }
 
     public void inserer(String n, METHODE i) {
         LMETHODE lm = this.hmMethode.get(n);
         if(lm==null) {
             LMETHODE newlm = new LMETHODE();
             this.hmMethode.put(n, newlm);
             newlm.addMethod(i);
         } else {
             lm.addMethod(i);            
         }
     }
 
     public CLASSE chercherLocalementClasse(String n) {
         return this.hmClasse.get(n);
     }
 
     public CLASSE chercherGlobalementClasse(String n) {
         CLASSE i = this.chercherLocalementClasse(n);
         if (i == null)
             if (this.parente != null)
                 return this.parente.chercherGlobalementClasse(n);
         return i;
     }
 
     public void inserer(String n, CLASSE i) {
         this.hmClasse.put(n, i);
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
 
         Set<Map.Entry<String, VAR>> sv = hmVar.entrySet();
         sb.append("VARIABLES:\n");
         for (Map.Entry<String, VAR> e : sv){
             sb.append("; " + e.getKey() + " : " + e.getValue() + '\n');
 
         }
 
         Set<Map.Entry<String, LMETHODE>> sm = hmMethode.entrySet();
         sb.append("METHODES:\n");
         for (Map.Entry<String, LMETHODE> e : sm){
             sb.append("; " + e.getKey() + " : " + e.getValue() + '\n');
 
         }
 
         Set<Map.Entry<String, CLASSE>> sc = hmClasse.entrySet();
         sb.append("CLASSE:\n");
         for (Map.Entry<String, CLASSE> e : sc){
             sb.append("; " + e.getKey() + " : " + e.getValue() + '\n');
 
         }
 
         if(parente==null) {
             return sb.toString();
         } else {
             return parente.toString() + sb.toString();
         }
     }
 
 }
