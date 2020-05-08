 package sk.stuba.fiit.perconik.core.java.dom;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.annotation.Nonnull;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ForwardingObject;
 
 public final class AstFunctions
 {
 	private AstFunctions()
 	{
 		throw new AssertionError();
 	}
 	
 	private static enum StringLiteralToEscapedValue implements Function<StringLiteral, String>
 	{
 		INSTANCE;
 		
 		public final String apply(@Nonnull final StringLiteral literal)
 		{
 			return literal.getEscapedValue();
 		}
 	}
 
 	private static enum StringLiteralToLiteralValue implements Function<StringLiteral, String>
 	{
 		INSTANCE;
 		
 		public final String apply(@Nonnull final StringLiteral literal)
 		{
 			return literal.getLiteralValue();
 		}
 	}
 	
 	private static final <N extends StringLiteral, T> Function<N, T> cast(final Function<?, ?> function)
 	{
 		// only for stateless internal singletons shared across all types
 		@SuppressWarnings("unchecked")
 		Function<N, T> result = (Function<N, T>) function;
 		
 		return result;
 	}
 
 	public static final <N extends StringLiteral> Function<N, String> stringLiteralToEscapedValue()
 	{
 		return cast(StringLiteralToEscapedValue.INSTANCE);
 	}
 	
 	public static final <N extends StringLiteral> Function<N, String> stringLiteralToLiteralValue()
 	{
 		return cast(StringLiteralToLiteralValue.INSTANCE);
 	}
 	
 	static final class AstCollectorFunction<N extends ASTNode, R extends ASTNode> extends ForwardingObject implements Function<N, List<R>>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final AstCollector<N, R> collector;
 		
 		AstCollectorFunction(final AstCollector<N, R> collector)
 		{
 			this.collector = Preconditions.checkNotNull(collector);
 		}
 
 		@Override
 		protected final AstCollector<N, R> delegate()
 		{
 			return this.collector;
 		}
 
 		@Override
 		public final List<R> apply(final N node)
 		{
 			return this.collector.collect(node);
 		}
 	}
 
 	static final class AstFlattenerFunction<N extends ASTNode> extends ForwardingObject implements Function<N, CharSequence>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final AstFlattener<N> flattener;
 		
 		AstFlattenerFunction(final AstFlattener<N> flattener)
 		{
 			this.flattener = Preconditions.checkNotNull(flattener);
 		}
 
 		@Override
 		protected final AstFlattener<N> delegate()
 		{
 			return this.flattener;
 		}
 
 		@Override
 		public final CharSequence apply(final N node)
 		{
 			return this.flattener.flatten(node);
 		}
 	}
 	
 	static final class AstFilterFunction<N extends ASTNode> extends ForwardingObject implements Function<N, Boolean>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final AstFilter<N> filter;
 		
 		AstFilterFunction(final AstFilter<N> filter)
 		{
 			this.filter = Preconditions.checkNotNull(filter);
 		}
 
 		@Override
 		protected final AstFilter<N> delegate()
 		{
 			return this.filter;
 		}
 
 		@Override
 		public final Boolean apply(final N node)
 		{
 			return this.filter.accept(node);
 		}
 	}
 
 	static final class AstTokenizerFunction<N extends ASTNode> extends ForwardingObject implements Function<N, List<String>>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final AstTokenizer<N> tokenizer;
 		
 		AstTokenizerFunction(final AstTokenizer<N> tokenizer)
 		{
 			this.tokenizer = Preconditions.checkNotNull(tokenizer);
 		}
 
 		@Override
 		protected final AstTokenizer<N> delegate()
 		{
 			return this.tokenizer;
 		}
 
 		@Override
 		public final List<String> apply(final N node)
 		{
 			return this.tokenizer.tokenize(node);
 		}
 	}
 	
 	static final class AstTransformerFunction<N extends ASTNode, R> extends ForwardingObject implements Function<N, R>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final AstTransformer<N, R> transformer;
 		
 		AstTransformerFunction(final AstTransformer<N, R> transformer)
 		{
 			this.transformer = Preconditions.checkNotNull(transformer);
 		}
 
 		@Override
 		protected final AstTransformer<N, R> delegate()
 		{
 			return this.transformer;
 		}
 
 		@Override
 		public final R apply(final N node)
 		{
 			return this.transformer.transform(node);
 		}
 	}
 
 	public static final <N extends ASTNode, R extends ASTNode> AstCollector<N, R> asCollector(final Function<N, List<R>> function)
 	{
 		if (function instanceof AstCollectorFunction)
 		{
 			return ((AstCollectorFunction<N, R>) function).delegate();
 		}
 		
 		return new FunctionAstCollector<>(function);
 	}
 
 	public static final <N extends ASTNode> AstFlattener<N> asFlattener(final Function<N, CharSequence> function)
 	{
 		if (function instanceof AstFlattenerFunction)
 		{
 			return ((AstFlattenerFunction<N>) function).delegate();
 		}
 		
 		return new FunctionAstFlattener<>(function);
 	}
 
 	public static final <N extends ASTNode> AstFilter<N> asFilter(final Function<N, Boolean> function)
 	{
 		if (function instanceof AstFilterFunction)
 		{
 			return ((AstFilterFunction<N>) function).delegate();
 		}
 		
 		return new FunctionAstFilter<>(function);
 	}
 
 	public static final <N extends ASTNode> AstTokenizer<N> asTokenizer(final Function<N, List<String>> function)
 	{
 		if (function instanceof AstTokenizerFunction)
 		{
 			return ((AstTokenizerFunction<N>) function).delegate();
 		}
 		
 		return new FunctionAstTokenizer<>(function);
 	}
 
 	public static final <N extends ASTNode, R> AstTransformer<N, R> asTransformer(final Function<N, R> function)
 	{
 		if (function instanceof AstTransformerFunction)
 		{
 			return ((AstTransformerFunction<N, R>) function).delegate();
 		}
 		
 		return new FunctionAstTransformer<>(function);
 	}
 
 	static final class FunctionAstCollector<N extends ASTNode, R extends ASTNode> extends ForwardingObject implements AstCollector<N, R>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final Function<N, List<R>> function;
 		
 		FunctionAstCollector(final Function<N, List<R>> function)
 		{
 			this.function = Preconditions.checkNotNull(function);
 		}
 		
 		@Override
 		protected final Function<N, List<R>> delegate()
 		{
 			return this.function;
 		}
 
 		@Override
 		public final List<R> collect(N node)
 		{
 			return this.function.apply(node);
 		}
 	}
 
 	static final class FunctionAstFlattener<N extends ASTNode> extends ForwardingObject implements AstFlattener<N>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final Function<N, CharSequence> function;
 		
 		FunctionAstFlattener(final Function<N, CharSequence> function)
 		{
 			this.function = Preconditions.checkNotNull(function);
 		}
 		
 		@Override
 		protected final Function<N, CharSequence> delegate()
 		{
 			return this.function;
 		}
 	
 		@Override
 		public final CharSequence flatten(N node)
 		{
 			return this.function.apply(node);
 		}
 	}
 	
 	static final class FunctionAstFilter<N extends ASTNode> extends ForwardingObject implements AstFilter<N>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final Function<N, Boolean> function;
 		
 		FunctionAstFilter(final Function<N, Boolean> function)
 		{
 			this.function = Preconditions.checkNotNull(function);
 		}
 		
 		@Override
 		protected final Function<N, Boolean> delegate()
 		{
 			return this.function;
 		}
 	
 		@Override
 		public final boolean accept(N node)
 		{
 			return this.function.apply(node);
 		}
 	}
 
 	static final class FunctionAstTokenizer<N extends ASTNode> extends ForwardingObject implements AstTokenizer<N>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final Function<N, List<String>> function;
 		
 		FunctionAstTokenizer(final Function<N, List<String>> function)
 		{
 			this.function = Preconditions.checkNotNull(function);
 		}
 		
 		@Override
 		protected final Function<N, List<String>> delegate()
 		{
 			return this.function;
 		}
 
 		@Override
 		public final List<String> tokenize(N node)
 		{
 			return this.function.apply(node);
 		}
 	}
 	
 	static final class FunctionAstTransformer<N extends ASTNode, R> extends ForwardingObject implements AstTransformer<N, R>, Serializable
 	{
		private static final long serialVersionUID = 0L;
 		
 		private final Function<N, R> function;
 		
 		FunctionAstTransformer(final Function<N, R> function)
 		{
 			this.function = Preconditions.checkNotNull(function);
 		}
 		
 		@Override
 		protected final Function<N, R> delegate()
 		{
 			return this.function;
 		}
 
 		@Override
 		public final R transform(N node)
 		{
 			return this.function.apply(node);
 		}
 	}
 
 	public static final <N extends ASTNode, R extends ASTNode> Function<N, List<R>> from(final AstCollector<N, R> collector)
 	{
 		if (collector instanceof FunctionAstCollector)
 		{
 			return ((FunctionAstCollector<N, R>) collector).delegate();
 		}
 		
 		return new AstCollectorFunction<>(collector);
 	}
 
 	public static final <N extends ASTNode> Function<N, CharSequence> from(final AstFlattener<N> flattener)
 	{
 		if (flattener instanceof FunctionAstFlattener)
 		{
 			return ((FunctionAstFlattener<N>) flattener).delegate();
 		}
 		
 		return new AstFlattenerFunction<>(flattener);
 	}
 	
 	public static final <N extends ASTNode> Function<N, Boolean> from(final AstFilter<N> filter)
 	{
 		if (filter instanceof FunctionAstFilter)
 		{
 			return ((FunctionAstFilter<N>) filter).delegate();
 		}
 		
 		return new AstFilterFunction<>(filter);
 	}
 
 	public static final <N extends ASTNode> Function<N, List<String>> from(final AstTokenizer<N> tokenizer)
 	{
 		if (tokenizer instanceof FunctionAstTokenizer)
 		{
 			return ((FunctionAstTokenizer<N>) tokenizer).delegate();
 		}
 		
 		return new AstTokenizerFunction<>(tokenizer);
 	}
 	
 	public static final <N extends ASTNode, R> Function<N, R> from(final AstTransformer<N, R> transformer)
 	{
 		if (transformer instanceof FunctionAstTransformer)
 		{
 			return ((FunctionAstTransformer<N, R>) transformer).delegate();
 		}
 		
 		return new AstTransformerFunction<>(transformer);
 	}
 }
