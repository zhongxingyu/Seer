 package de.tuberlin.dima.presslufthammer.query;
 
 /**
  * Representation of one term in where clause of our query. Where clauses can
  * only be of the form: <code>
  *    column-name <op> value
  * </code> where <op> is one of "==" or "!=". Several where clauses are
  * separated by comma, the resulting where clause is a disjunction of the simple
  * where clauses. (Again, too lazy to do this properly ... :D
  * 
  * @author Aljoscha Krettek
  * 
  */
 public class WhereClause {
     private final String column;
     private final Op op;
     private final String value;
 
     public WhereClause(String column, Op op, String value) {
         super();
         this.column = column;
         this.op = op;
         this.value = value;
     }
 
     public String getColumn() {
         return column;
     }
 
     public Op getOp() {
         return op;
     }
 
     public String getValue() {
         return value;
     }
 
     public static enum Op {
         EQ, NEQ
     }
 
     @Override
     public String toString() {
         if (op == Op.EQ) {
             return column + " == \"" + value + "\"";
         } else if (op == Op.NEQ) {
            return column + " == \"" + value + "\"";
         } else {
             throw new RuntimeException("Should not happen.");
         }
     }
 }
