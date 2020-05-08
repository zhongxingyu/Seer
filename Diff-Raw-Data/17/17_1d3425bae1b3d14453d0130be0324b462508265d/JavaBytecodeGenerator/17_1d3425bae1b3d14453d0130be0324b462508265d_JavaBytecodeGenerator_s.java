 package ch.unibe.iam.scg.minijava.bcel.generator;
 
 import org.apache.bcel.Constants;
 import org.apache.bcel.classfile.Method;
 import org.apache.bcel.generic.ClassGen;
 import org.apache.bcel.generic.InstructionList;
 import org.apache.bcel.generic.MethodGen;
 
 import ch.unibe.iam.scg.minijava.ast.Node;
 
 /**
  * Change at will!
  * (http://funnyasduck.net/wp-content/uploads/2013/01/funny-star-
  * trek-picard-tv-fire-will-pics.jpg)
  * 
  * @author kursjan
  * 
  */
 public class JavaBytecodeGenerator {
 
 	private InstructionList il = new InstructionList();
 	private ClassGen cg = createTemporaryClass();
 	private MethodGen mg;
 
 	private GeneratorVisitor generator;
 
 	private ClassGen createTemporaryClass() {
 		String className = "BcelGenerated";
 		ClassGen cg = new ClassGen(className, "java.lang.Object", className,
 				Constants.ACC_PUBLIC, new String[0]);
 		cg.addEmptyConstructor(Constants.ACC_PUBLIC);
 		return cg;
 	}
 
 	public void generate(Object node) {
 		Node n = (Node) node;
 		this.generator = new GeneratorVisitor(il, cg, mg);
 		n.accept(generator);
 		this.cg = generator.getClassGen();
 		this.mg = generator.getMethodGen();
 		this.il = generator.getInstructionList();
 
 	}
 
 	public Method getMethod() {
 		return this.mg.getMethod();
 	}
 
 	public InstructionList getInstructionList() {
 		return this.il;
 	}
 
 	public ClassGen getClassGen() {
 		return this.cg;
 	}
 
 }
