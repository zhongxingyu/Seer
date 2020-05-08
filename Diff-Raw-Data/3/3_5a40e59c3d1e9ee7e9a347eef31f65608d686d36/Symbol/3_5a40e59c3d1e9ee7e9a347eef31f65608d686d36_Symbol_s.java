 package xhl.core;
 
 public class Symbol {
     private final String name;
 
     public Symbol(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Symbol) {
             Symbol sym = (Symbol) obj;
            if (sym.name == this.name)
                return true;
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 
     @Override
     public String toString() {
         return name;
     }
 }
