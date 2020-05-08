 package dtool.ast.expressions;
 
 import java.util.List;
 
 import dtool.ast.ASTNeoNode;
 import dtool.descentadapter.DescentASTConverter;
 
 public abstract class Initializer extends ASTNeoNode{
 
 	public static Initializer convert(descent.internal.compiler.parser.Initializer initializer) {
 		return (Initializer) DescentASTConverter.convertElem(initializer);
 	}
 
 	
 	public static Initializer[] convertMany(List<descent.internal.compiler.parser.Initializer> elements) {
 		Initializer[] rets = new Initializer[elements.size()];
 		DescentASTConverter.convertMany(elements, rets);
 		return rets;
 	}
 }
