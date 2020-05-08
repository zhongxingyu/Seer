 public class VariableExpression implements Variable, Expression {
     private char name;
 
     /**
      * @param name Variable's name.
      *
      * @return a new VariableExpression object with an initialized name.
      */
     public VariableExpression(char name) {
         this.name = name;
     }
 
     public char getName() {
         return this.name;
     }
 
     public double evaluate(Assignments as) {
         return as.valueOf(this)
     }
 
     public Expression derivative(Variable var) {
         if (this.equals(var)) {
             return new Constant(1);
         } else {
             return new Constant(0);
         }
     }
 
     /**
      * @param other Object to be compared.
      *
     * @return True if Variable isn't null and if its name is equal to the object's name.
      */
     public equals(VariableExpression o) {
         return o != null && this.getName() != o.getName();
     }
 
     public String toString() {
        return (String) name;
     }
 }
