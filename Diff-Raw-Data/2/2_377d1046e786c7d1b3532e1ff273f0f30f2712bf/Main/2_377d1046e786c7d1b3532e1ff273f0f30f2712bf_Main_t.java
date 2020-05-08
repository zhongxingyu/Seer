 package zinara.ast;
 
import zinara.ast.instructions.CodeBlock;

 public class Main {
     CodeBlock code;
     
     public Main(CodeBlock c) {
 	this.code = c;
     }
     
     public CodeBlock getCode(){
 	return this.code;
     }
 }
