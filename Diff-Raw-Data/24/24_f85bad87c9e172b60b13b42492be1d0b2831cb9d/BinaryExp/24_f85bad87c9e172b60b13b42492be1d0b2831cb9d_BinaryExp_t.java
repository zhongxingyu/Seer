 package zinara.ast.expression;
 
 public class BinaryExp extends Expression {
   public int operator;
    public Expression left;
    public Expression right;
 
   public BinaryExp (int o, Expression l, Expression r) { operator=o; left=l; right=r; }
 }
