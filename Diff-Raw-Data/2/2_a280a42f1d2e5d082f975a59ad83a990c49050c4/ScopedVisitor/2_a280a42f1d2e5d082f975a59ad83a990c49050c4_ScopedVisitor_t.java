 package uk.ac.ic.doc.gander.model.build;
 
 import java.util.Stack;
 
 import org.python.pydev.parser.jython.SimpleNode;
 import org.python.pydev.parser.jython.ast.ClassDef;
 import org.python.pydev.parser.jython.ast.FunctionDef;
 import org.python.pydev.parser.jython.ast.Module;
 import org.python.pydev.parser.jython.ast.VisitorBase;
 
 /**
 * Maintain a stack of scopes while visiting a module's AST.
  * 
  * This abstract base class allows subclasses to visit nodes while always have
  * access to the node's containing scope (namespace).
  * 
  * @param <T>
  *            Type of the scope objects.
  */
 public abstract class ScopedVisitor<T> extends VisitorBase {
 
 	private Stack<T> scopes = new Stack<T>();
 
 	/**
 	 * Construct with no initial scope.
 	 * 
 	 * This is equivalent to constructing with a {@code null} initial scope as
 	 * calling {@code getScope()} will return {@code null} in either case.
 	 */
 	public ScopedVisitor() {
 	}
 
 	/**
 	 * Construct with an initial scope already set up on the stack.
 	 * 
 	 * @param initialScope
 	 *            First scope on the stack.
 	 */
 	public ScopedVisitor(T initialScope) {
 		scopes.push(initialScope);
 	}
 
 	/**
 	 * Return parent scope of last namespace created.
 	 */
 	protected T getScope() {
 		return (!scopes.empty()) ? scopes.peek() : null;
 	}
 
 	protected abstract T atScope(Module node);
 
 	protected abstract T atScope(FunctionDef node);
 
 	protected abstract T atScope(ClassDef node);
 
 	@Override
 	public final Object visitModule(
 			org.python.pydev.parser.jython.ast.Module node) throws Exception {
 		traverseScope(atScope(node), node);
 		return null;
 	}
 
 	@Override
 	public final Object visitClassDef(ClassDef node) throws Exception {
 		traverseScope(atScope(node), node);
 		return null;
 	}
 
 	@Override
 	public final Object visitFunctionDef(FunctionDef node) throws Exception {
 		traverseScope(atScope(node), node);
 		return null;
 	}
 
 	private void traverseScope(T scope, SimpleNode node) throws Exception {
 		scopes.push(scope);
 		node.traverse(this);
 		scopes.pop();
 	}
 }
