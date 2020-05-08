 package mjc.gc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class CLASSE extends DTYPE {
     private CLASSE classeMere;
     private TDS tds;
     private boolean isclass;
     private ArrayList<CLASSE> acceptedSuperClasses;
     private INFOMET constructor;
     private StringBuffer buf;
 
     public CLASSE(boolean isclass,TDS globaltds) {
         super("classe",0);
         classeMere = null;
         this.tds = new TDS(globaltds);
         this.isclass=isclass;
         if(!isclass) {
             super.setNom("interface");
         }
     }
 
     public CLASSE(CLASSE cl) {
         super("classe",0);        
        this.tds = (TDS)cl.getTDS().clone();
         classeMere = cl;
     }
 
     public boolean equals(DTYPE t) {
         return (t==this);
     }
 
     public boolean isAClass() {
         return isclass;
     }
 
     public void addSuperClass(CLASSE cl) {
         acceptedSuperClasses.add(cl);
     }
 
     public INFOMET getConstructor() {
         return constructor;
     }
 
     public void setConstructor(INFOMET met) {
         constructor=met;
     }
 
     public boolean isASuperClass(CLASSE cl) {
         if(cl==this) {
             return true;
         }
 
         for(CLASSE c : acceptedSuperClasses) {
             if (cl == c ) {
                 return true;
             }
                         
         }
 
         if(classeMere==null) {
             return false;
         } else {
             return classeMere.isASuperClass(cl);
         }
     }
 
     public boolean implementsCorrectly(CLASSE inter) {
         buf = new StringBuffer();
 
         if(!isclass) {
             buf.append("Une interface ne peut implémenter une autre interface");
             return false;
         } else if(inter.isAClass()) {
             buf.append("Une classe ne peut en implémenter une autre");
             return false;
         }
         
         boolean implementCorrect=true;
         TDS intertds = inter.getTDS();
         Set<Map.Entry<String,INFO>> esi = intertds.entrySet();
 
         for (Map.Entry<String,INFO> e : esi) {
 
             // si l'entrée courante dans la TDS interface est bien une méthode
             if (e.getValue() instanceof INFOMET ) {
 
                 ARGLIST eargs = ((INFOMET)(e.getValue())).getArgs();
                 DTYPE etype = e.getValue().getType();
 
                 INFO ret = this.tds.chercherLocalement(e.getKey());
                 
                 // si, dans la classe, une entrée à le même nom et est une méthode aussi
                 if( (ret != null) && (ret instanceof INFOMET )) {
           
                     // et si cette méthode à les mêmes arguments et le même type de retour
                     if( ((INFOMET)ret).getArgs().equals(eargs) & ret.getType().equals(etype)) {
                         // la méthode de l'interface est implémentée dans la classe
 
                     } else {
                         buf.append("La méthode " + e.getKey() + "n'est pas correctement implémentée dans la classe");
                         implementCorrect=false;
                     }
 
                 } else {
                     buf.append("La méthode " + e.getKey() + "n'est pas implémentée dans la classe");
                     implementCorrect=false;
                 }
                 
             } 
         }
         
         return implementCorrect;
     }
 
     public String implementGetError() {
         return buf.toString();
     }
 
     public TDS getTDS() {
         return tds;
     }
 
 }
