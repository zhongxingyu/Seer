 package sk.stuba.fiit.perconik.core.java.dom;
 
 import java.util.List;
 import javax.annotation.Nullable;
 import org.eclipse.jdt.core.dom.ASTNode;
 import com.google.common.base.Preconditions;
 
 public abstract class AstFilteringCollector<N extends ASTNode, R extends ASTNode> implements AstCollector<N, R>
 {
 	AstFilteringCollector()
 	{
 	}
 	
 	public static final <N extends ASTNode> AstFilteringCollector<N, N> using(final AstFilter<N> filter)
 	{
 		return new Generic<>(filter);
 	}
 	
 	public static final <N extends ASTNode, R extends ASTNode> AstFilteringCollector<N, R> using(final AstTypeFilter<N, R> filter)
 	{
 		return new Type<>(filter);
 	}
 	
 	private static final class Generic<N extends ASTNode> extends AstFilteringCollector<N, N>
 	{
 		final AstFilter<N> filter;
 		
 		Generic(final AstFilter<N> filter)
 		{
 			this.filter = Preconditions.checkNotNull(filter);
 		}
 
 		@Override
 		public final List<N> collect(@Nullable final N node)
 		{
			if (node == null)
			{
				return null;
			}

 			return new Processor().perform(node);
 		}
 		
 		private final class Processor extends AbstractCollectingVisitor<N, N>
 		{
 			Processor()
 			{
 			}
 			
 			@Override
 			public final void preVisit(final ASTNode node)
 			{
 				// this cast is unsafe, therefore the try block
 				@SuppressWarnings("unchecked")
 				N casted = (N) node;
 				
 				try
 				{
 					if (Generic.this.filter.accept(casted))
 					{
 						this.result.add(casted);
 					}
 				}
 				catch (ClassCastException e)
 				{
 				}
 			}
 		}
 	}
 	
 	private static final class Type<N extends ASTNode, R extends ASTNode> extends AstFilteringCollector<N, R>
 	{
 		final AstTypeFilter<N, R> filter;
 
 		Type(final AstTypeFilter<N, R> filter)
 		{
 			this.filter = Preconditions.checkNotNull(filter);
 		}
 		
 		@Override
 		public final List<R> collect(@Nullable final N node)
 		{
			if (node == null)
			{
				return null;
			}

 			return new Processor().perform(node);
 		}
 		
 		private final class Processor extends AbstractCollectingVisitor<N, R>
 		{
 			Processor()
 			{
 			}
 			
 			@Override
 			public final void preVisit(final ASTNode node)
 			{
 				for (Class<? extends R> type: Type.this.filter.getAcceptedTypes())
 				{
 					if (type.isInstance(node))
 					{
 						boolean accepted = false;
 						
 						// this cast is unsafe, therefore the try block
 						@SuppressWarnings("unchecked")
 						N casted = (N) node;
 						
 						try
 						{
 							accepted = Type.this.filter.accept(casted);
 						}
 						catch (ClassCastException e)
 						{
 							continue;
 						}
 						
 						if (accepted)
 						{
 							this.result.add(type.cast(node));
 						}
 						
 						return;
 					}
 				}
 			}
 		}
 	}
 }
