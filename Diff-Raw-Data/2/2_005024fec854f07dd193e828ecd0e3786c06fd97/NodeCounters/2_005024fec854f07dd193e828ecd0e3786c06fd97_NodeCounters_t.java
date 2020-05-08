 package sk.stuba.fiit.perconik.core.java.dom;
 
 import javax.annotation.Nullable;
 import org.eclipse.jdt.core.dom.ASTNode;
 import sk.stuba.fiit.perconik.utilities.MoreStrings;
 import sk.stuba.fiit.perconik.utilities.function.Numerate;
 import com.google.common.base.Predicate;
 
 public final class NodeCounters
 {
 	private NodeCounters()
 	{
 		throw new AssertionError();
 	}
 	
 	private static enum NodeCounter implements Numerate<ASTNode>
 	{
 		INSTANCE;
 
 		public final int apply(@Nullable final ASTNode node)
 		{
 			AbstractCountingVisitor<ASTNode> visitor = new AbstractCountingVisitor<ASTNode>()
 			{
 				@Override
 				public final void preVisit(final ASTNode node)
 				{
 					this.count ++;
 				}
 			};
 			
 			return visitor.perform(node);
 		}
 		
 		@Override
 		public final String toString()
 		{
 			return "nodes";
 		}
 	}
 	
 	private static enum LineCounter implements Numerate<ASTNode>
 	{
 		INSTANCE;
 
 		public final int apply(@Nullable final ASTNode node)
 		{
 			if (node == null || !Nodes.hasSource(node))
 			{
 				return 0;
 			}
 			
 			String source = Nodes.source(node, NodeRangeType.STANDARD);
 			
 			return source != null ? source.split("\r?\n|\r").length : 0;
 		}
 		
 		@Override
 		public final String toString()
 		{
 			return "lines(?)";
 		}
 	}
 	
 	private static enum CharacterCounter implements Numerate<ASTNode>
 	{
 		INSTANCE;
 
 		public final int apply(@Nullable final ASTNode node)
 		{
 			return node != null ? node.getLength() : 0;
 		}
 		
 		@Override
 		public final String toString()
 		{
 			return "characters";
 		}
 	}
 
 	private static enum MemoryCounter implements Numerate<ASTNode>
 	{
 		INSTANCE;
 	
 		public final int apply(@Nullable final ASTNode node)
 		{
 			return node != null ? node.subtreeBytes() : 0;
 		}
 		
 		@Override
 		public final String toString()
 		{
 			return "memory";
 		}
 	}
 
 	private static final <N extends ASTNode> Numerate<N> cast(final Numerate<?> numerate)
 	{
 		// only for stateless internal singletons shared across all types
 		@SuppressWarnings("unchecked")
 		Numerate<N> result = (Numerate<N>) numerate;
 		
 		return result;
 	}
 	
 	public static final <N extends ASTNode> Numerate<N> nodes()
 	{
 		return cast(NodeCounter.INSTANCE);
 	}
 	
 	public static final <N extends ASTNode> Numerate<N> lines()
 	{
 		return cast(LineCounter.INSTANCE);
 	}
 	
 	public static final <N extends ASTNode> Numerate<N> lines(final String source)
 	{
 		return new Numerate<N>()
 		{
 			public final int apply(final N node)
 			{
 				int index;
 				
 				if (node == null || (index = node.getStartPosition()) == -1)
 				{
 					return 0;
 				}
 				
				return MoreStrings.lines(source.substring(index, index + node.getLength())).size();
 			}
 
 			@Override
 			public String toString()
 			{
 				return "lines(source)";
 			}
 		};
 	}
 	
 	public static final <N extends ASTNode> Numerate<N> characters()
 	{
 		return cast(CharacterCounter.INSTANCE);
 	}
 
 	public static final <N extends ASTNode> Numerate<N> memory()
 	{
 		return cast(MemoryCounter.INSTANCE);
 	}
 
 	public static final <N extends ASTNode> Numerate<N> usingFilter(final Predicate<ASTNode> filter)
 	{
 		return NodeFilteringCounter.using(filter);
 	}
 }
