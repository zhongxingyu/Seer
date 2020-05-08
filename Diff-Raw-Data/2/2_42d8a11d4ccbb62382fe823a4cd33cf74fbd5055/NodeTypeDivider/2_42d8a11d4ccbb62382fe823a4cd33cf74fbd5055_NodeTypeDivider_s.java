 package sk.stuba.fiit.perconik.core.java.dom;
 
 import javax.annotation.Nullable;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.NodeType;
 import sk.stuba.fiit.perconik.utilities.function.MultimapCollector;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Multimap;
 
 public final class NodeTypeDivider<N extends ASTNode> implements MultimapCollector<N, NodeType, ASTNode>
 {
 	private static final NodeTypeDivider<ASTNode> instance = new NodeTypeDivider<>();
 	
 	NodeTypeDivider()
 	{
 	}
 
 	public static final <N extends ASTNode> NodeTypeDivider<N> create()
 	{
 		// stateless internal singleton is shared across all types
 		@SuppressWarnings("unchecked")
 		NodeTypeDivider<N> casted = (NodeTypeDivider<N>) instance;
 		
 		return casted;
 	}
 	
 	public final Multimap<NodeType, ASTNode> apply(final N node)
 	{
 		return new Processor().perform(node);
 	}
 
 	private final class Processor extends ASTVisitor
 	{
 		final Multimap<NodeType, ASTNode> result;
 		
 		Processor()
 		{
 			super(true);
 			
 			this.result = LinkedListMultimap.create(NodeType.count());
 		}
 		
 		final Multimap<NodeType, ASTNode> perform(final N node)
 		{
 			if (node == null)
 			{
 				return ImmutableMultimap.of();
 			}
 			
 			node.accept(this);
 			
 			return this.result;
 		}
 
 		@Override
 		public final void preVisit(final ASTNode node)
 		{
 			this.result.put(NodeType.valueOf(node), node);
 		}
 	}
 	
 	@Override
 	public final boolean equals(@Nullable final Object o)
 	{
 		return o == this;
 	}
 
 	@Override
 	public final int hashCode()
 	{
		return this.hashCode();
 	}
 	
 	@Override
 	public final String toString()
 	{
 		return "divider";
 	}
 }
