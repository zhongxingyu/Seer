 package xhl.core.elements;
 
 import java.util.Arrays;
 
 import com.google.common.base.Joiner;
 
 public class Symbol extends Expression {
     private final String name[];
 
     public Symbol(String name) {
         this(new String[]{name}, null);
     }
 
     public Symbol(String name, Position position) {
         this(new String[]{name}, position);
     }
 
     public Symbol(String[] name, Position position) {
         super(position);
         this.name = name;
     }
 
     public String getName() {
         return name[name.length-1];
     }
 
     /** Check if symbol has specified name. */
     public boolean isNamed(String n) {
        return name.equals(n);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Symbol) {
             Symbol sym = (Symbol) obj;
             return Arrays.equals(sym.name, this.name);
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return Arrays.hashCode(name);
     }
 
     @Override
     public String toString() {
         return Joiner.on('.').join(name);
     }
 
     @Override
     public <R> R accept(ElementVisitor<R> visitor) {
         return visitor.visit(this);
     }
 }
