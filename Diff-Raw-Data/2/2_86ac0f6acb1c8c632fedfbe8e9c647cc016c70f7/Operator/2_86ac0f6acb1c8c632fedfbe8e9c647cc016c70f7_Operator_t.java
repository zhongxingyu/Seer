 package sintaxtree.Nodes.Operators;
 
 public class Operator {
 
     public static final Operator add = new Operator("add", '+', OperatorType.BINARY);
     public static final Operator minus = new Operator("minus", '-', OperatorType.BINARY);
    public static final Operator mult = new Operator("mult", '*', OperatorType.BINARY);
     public static final Operator division = new Operator("division", '/', OperatorType.BINARY);
     
     private final String name;
     private final char operator;
     private final OperatorType operatorType;  
     
     public Operator(String name, char operator, OperatorType operatortype) {
         this.name = name;
         this.operator = operator;
         this.operatorType = operatortype;
     }
 
     public String getName() {
         return name;
     }
 
     public String getOperator() {
         return Character.toString(operator);
     }
 
     public OperatorType getOperatorType() {
         return operatorType;
     }
     
 }
