 package primitives;
 
 import global.AttrOperator;
 import global.AttrType;
 
 /**
  * Internal representation of simple SQL expressions. Operators are referenced
  * by constants in global.AttrOperator, and operands can be constants or field
  * numbers (i.e. if referring to an attribute).
  */
 public class Predicate {
 
   /** The operator. */
   protected int oper;
 
   /** Type of left operand. */
   protected int ltype;
 
   /** Left operand. */
   protected Object left;
 
   /** Type of right operand. */
   protected int rtype;
 
   /** Right operand. */
   protected Object right;
 
   // --------------------------------------------------------------------------
 
   /**
    * Constructs a predicate, given the expression.
    * 
    * @param oper AttrOperator constant
    * @param ltype AttrType of the field
    * @param left operand value, field number, or column name
    * @param rtype AttrType of the field
    * @param right operand value, field number, or column name
    */
   public Predicate(int oper, int ltype, Object left, int rtype, Object right) {
     this.oper = oper;
     this.ltype = ltype;
     this.rtype = rtype;
     this.left = left;
     this.right = right;
   }
 
   /**
    * Validates AttrType.COLNAME operands against the given schema.
    * 
    * @return true if the predicate is valid, false otherwise
    */
   public boolean validate(Schema schema) {
 
     // if column name, make sure it exists
     int type1 = ltype;
     if (type1 == AttrType.COLNAME) {
       int fldno = schema.fieldNumber((String) left);
       if (fldno < 0) {
         return false;
       }
       type1 = schema.fieldType(fldno);
     }
     int type2 = rtype;
     if (type2 == AttrType.COLNAME) {
       int fldno = schema.fieldNumber((String) right);
       if (fldno < 0) {
         return false;
       }
       type2 = schema.fieldType(fldno);
     }

     // test left and right type compatibility
     return (type1 == type2);
 
   } // public boolean validate(Schema schema)
 
   /**
    * Evaluates the predicate on the given tuple and returns true if it passes.
    * 
    * @throws IllegalStateException if member data lead to an invalid operation
    */
   public boolean evaluate(Tuple tuple) {
 
     // if necessary, resolve column names to field numbers
     if (ltype == AttrType.COLNAME) {
       left = tuple.schema.fieldNumber((String) left);
       ltype = AttrType.FIELDNO;
     }
     if (rtype == AttrType.COLNAME) {
       right = tuple.schema.fieldNumber((String) right);
       rtype = AttrType.FIELDNO;
     }
 
     // get the values to compare
     int type = ltype;
     Object lval = left;
     if (ltype == AttrType.FIELDNO) {
       type = tuple.schema.fieldType((Integer) lval);
       lval = tuple.getField((Integer) lval);
     }
     Object rval = right;
     if (rtype == AttrType.FIELDNO) {
       rval = tuple.getField((Integer) rval);
     }
 
     // compare the values
     int comp = 0;
     switch (type) {
 
       case AttrType.INTEGER:
         comp = ((Integer) lval).compareTo((Integer) rval);
         break;
 
       case AttrType.FLOAT:
         comp = ((Float) lval).compareTo((Float) rval);
         break;
 
       case AttrType.STRING:
         comp = ((String) lval).compareTo((String) rval);
         break;
 
       default:
         throw new IllegalStateException("unknown types to compare");
 
     } // switch (type)
 
     // evaluate the operator
     switch (oper) {
 
       case AttrOperator.EQ:
         return (comp == 0);
 
       case AttrOperator.NEQ:
         return (comp != 0);
 
       case AttrOperator.GT:
         return (comp > 0);
 
       case AttrOperator.GTE:
         return (comp >= 0);
 
       case AttrOperator.LT:
         return (comp < 0);
 
       case AttrOperator.LTE:
         return (comp <= 0);
 
       default:
         throw new IllegalStateException("unknown operator to evaluate");
 
     } // switch (oper)
 
   } // public boolean evaluate(Tuple tuple)
 
   /**
    * Returns a string representation of the Predicate.
    */
   public String toString() {
 
     return opString(ltype, left) + ' ' + AttrOperator.toString(oper) + ' '
         + opString(rtype, right);
 
   } // public String toString()
 
   /**
    * Returns a string representation of an operand.
    */
   protected String opString(int type, Object operand) {
 
     // get the string version
     String str = operand.toString();
 
     // wrap strings in single quotes
     if (type == AttrType.STRING) {
       return "'" + str + "'";
     }
 
     // wrap column numbers in braces
     if (type == AttrType.FIELDNO) {
       return "{" + str + "}";
     }
 
     // otherwise, just return the string
     return str;
 
   } // protected String opString(int type, Object obj)
 
 } // public class Predicate
