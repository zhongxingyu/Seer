 package AST;
 


 public interface Visitor<T>
 {
 	T visit(Loop loop);
 
 	T visit(Branch branch);
 
 	T visit(Block block);
 
 	T visit(Assign assign);
 
 	T visit(Id id);
 
 	T visit(Operator op);
 
 	T visit(Plus op);
 
 	T visit(Minus op);
 
 	T visit(Times op);
 
 	T visit(Divide op);
 
 	T visit(Number num);
 	
 	T visit(Chars str);
 }
