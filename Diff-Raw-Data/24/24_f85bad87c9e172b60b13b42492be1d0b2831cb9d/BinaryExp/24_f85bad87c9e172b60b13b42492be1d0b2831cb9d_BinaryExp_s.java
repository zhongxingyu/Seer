 package zinara.ast.expression;
 
 public class BinaryExp extends Expression {
   public String operator;
    public Expression left;
    public Expression right;
 
   public BinaryExp (String o, Expression l, Expression r) { operator=o; left=l; right=r; }
 }
