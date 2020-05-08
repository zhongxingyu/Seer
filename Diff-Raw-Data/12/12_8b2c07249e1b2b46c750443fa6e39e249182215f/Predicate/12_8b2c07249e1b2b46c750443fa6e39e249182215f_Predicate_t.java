 package lois.lab1.datastructure;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Predicate.
  *
  * @author Svet
  */
 public class Predicate extends AtomSign {
 
     /**
      * List of the predicate arguments.
      */
     private List<AtomSign> arguments = new ArrayList<AtomSign>();
 
     public Predicate(String sign) {
         setSign(sign);
     }
 
     public Predicate(String sign, List<? extends AtomSign> arguments) {
         setSign(sign);
 
         handleArgumentList(arguments);
         this.arguments.addAll(arguments);
     }
 
     @Override
     public AtomSignType getType() {
         return AtomSignType.PREDICATE;
     }
 
     public List<AtomSign> getArgumentList() {
         return arguments;
     }
 
     /**
      * Determines if the predicate logically equivalent to the other.
      *
      * <p>
      * For example F(X, Y) equivalent to the F(a, b).
      * </p>
      *
      * @param otherPredicate other predicate
      * @return true - if predicate equivalent, false in the other case
      */
     public boolean isLogicallyEquivalent(Predicate otherPredicate) {
         if (this.equals(otherPredicate)) {
             return true;
         }
 
         if (!this.getSign().equals(otherPredicate.getSign())) {
             return false;
         }
 
         int minArgumentListSize = Math.min(this.arguments.size(), otherPredicate.getArgumentList().size());
 
         for (int i = 0; i < minArgumentListSize; i++) {
 
             if (this.arguments.get(i).getType() == AtomSignType.CONST
                 && otherPredicate.getArgumentList().get(i).getType() == AtomSignType.CONST
                 && !this.arguments.get(i).equals(otherPredicate.getArgumentList().get(i))) {
 
                 return false;
             }
         }
 
         return true;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         Predicate predicate = (Predicate) o;
 
         if (!this.getSign().equals(predicate.getSign())
             || (arguments != null ? !arguments.equals(predicate.arguments) : predicate.arguments != null)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return (arguments != null ? arguments.hashCode() : 0) + getSign().hashCode();
     }
 
     @Override
     public String toString() {
         return "Predicate{" +
             "sign=" + getSign() + ";" +
             "arguments=" + arguments +
             '}';
     }
 
     private void handleArgumentList(List<? extends AtomSign> arguments) {
 
         for (int i = 0; i < arguments.size(); i++) {
 
             if (arguments.get(i).getType() == AtomSignType.VAR && arguments.get(i).getSign().equals("?")) {
                 arguments.get(i).setSign(arguments.get(i).getSign() + "_" + i);
             }
         }
     }
 }
