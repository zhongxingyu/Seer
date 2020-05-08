 package domainControllers;
 
 import java.util.Set;
 
abstract public class trObtenirPoblacions extends Transaccio {
 
     private Set<String> poblacions;
     
     @Override
     public void executa() {
         // TODO
     }
     
     public Set<String> obtenirResultat() {
         // TODO
         poblacions = null;
         return poblacions;
     }
 }
