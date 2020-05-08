 package sk.stuba.fiit.perconik.core.java.dom;
 
 import java.util.List;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import com.google.common.collect.Lists;
 
 abstract class AbstractCollectingVisitor<N extends ASTNode, R extends ASTNode> extends ASTVisitor
 {
 	final List<R> result;
 	
 	AbstractCollectingVisitor()
 	{
 		super(true);
 		
 		this.result = Lists.newLinkedList(); 
 	}
 	
 	final List<R> perform(final N node)
 	{
 		node.accept(this);
 		
 		return this.result;
 	}
 }
