 package com.redshape.ascript.language.impl;
 
 import com.redshape.ascript.language.*;
 import com.redshape.ascript.language.ast.*;
 import com.redshape.utils.IFilter;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class Lexer implements ILexer {
 	private static final Logger log = Logger.getLogger( Lexer.class );
 	
 	private ITokenizer tokenizer;
 	private TokenType grammarType = TokenType.T_NONE;
 	private LexicalType lexicalType = LexicalType.NONE;
 	private String data;
 	private int currentOffset;
 	private Queue<ISyntaxTreeNode> syntaxStack
 						= new LinkedBlockingQueue<ISyntaxTreeNode>();
 	private StackWalker walker = new StackWalker();
 	
 	private Map<GrammarRule, Collection<LexicalRule>> rules
 								= new HashMap<GrammarRule, Collection<LexicalRule>>();
 	
 	public Lexer( String data, ITokenizer tokenizer ) {
 		this.data = data;
 		this.tokenizer = tokenizer;
 		
 		this.init();
 	}
 	
 	/**
 	 * (do x y z (d) )
 	 * (SCOPE(FUNCTION(do) VARIABLE(#x) LITERAL(y)))END(.) 
 	 */
 	protected void init() {
 		this.addChainRule( 
 			new GrammarRule( TokenType.T_CONTEXT_START, TokenType.T_SEPARATOR ),
 			new LexicalRule( LexicalType.NONE, LexicalType.GSCOPE )
 		);
 		
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_COLON ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.DECLARATION ),
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.DECLARATION ),
 				new LexicalRule( LexicalType.FUNCTION, LexicalType.DECLARATION )
 			}
 		);
 
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_STRING ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.NONE, LexicalType.STRING ),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.STRING ),
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.STRING ),
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.STRING ),
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.STRING ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.STRING ),
 				new LexicalRule( LexicalType.STRING, LexicalType.FSCOPE ),
 				new LexicalRule( LexicalType.NONE,  LexicalType.STRING ),
                 new LexicalRule( LexicalType.ENDSCOPE, LexicalType.STRING ),
 				new LexicalRule( LexicalType.DECLARATION, LexicalType.STRING )
 			}
 		);
 
 		this.addChainRule(
 			new GrammarRule( TokenType.T_COLON, TokenType.T_LITERAL ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.DECLARATION, LexicalType.DECLARATION ),
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.DECLARATION ),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.DECLARATION )
 			}
 		);
 		
 		this.addChainRule( 
 			new GrammarRule( TokenType.T_SHARP, TokenType.T_LITERAL ),
 			new LexicalRule( LexicalType.VARIABLE, LexicalType.VARIABLE )
 		);
 		
 		this.addChainRule(
 			new GrammarRule( TokenType.T_CONTEXT_END, TokenType.T_END ),
 			new LexicalRule( LexicalType.GSCOPE, LexicalType.NONE )
 		);
 		
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_CONTEXT_END ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.NONE, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.FUNCTION, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.ENDSCOPE),
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.DECLARATION, LexicalType.ENDSCOPE ),
 				new LexicalRule( LexicalType.STRING, LexicalType.ENDSCOPE ),
                 new LexicalRule( LexicalType.ENDSCOPE, LexicalType.ENDSCOPE )
 			}
 		);
 		
 		this.addChainRule( 
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_CONTEXT_START ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.NONE, LexicalType.GSCOPE ),				
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.FUNCTION ),
                new LexicalRule( LexicalType.FUNCTION, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.STRING, LexicalType.STRING ),
 				new LexicalRule( LexicalType.ENDSCOPE, LexicalType.FUNCTION )
 			}
 		);
 		
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_PATH ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.FUNCTION, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.LITERAL )
 			}
 		);
 		
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_SHARP ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.NONE, LexicalType.VARIABLE )
 			}
 		);
 
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_LITERAL ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.FUNCTION, LexicalType.FUNCTION ),
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.VARIABLE ),
 				new LexicalRule( LexicalType.DECLARATION, LexicalType.DECLARATION ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.LITERAL ),
 				new LexicalRule( LexicalType.STRING, LexicalType.STRING ),
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.LITERAL ),
 				new LexicalRule( LexicalType.NONE, LexicalType.LITERAL),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.LITERAL )
 			}
 		);
 
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_END ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.NONE ),
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.NONE )
 			}
 		);
 
 
 		this.addChainRule(
 			new GrammarRule( TokenType.T_GENERIC, TokenType.T_SEPARATOR ),
 			new LexicalRule[] {
 				new LexicalRule( LexicalType.FUNCTION, LexicalType.FSCOPE ),
 				new LexicalRule( LexicalType.VARIABLE, LexicalType.FSCOPE ),
 				new LexicalRule( LexicalType.DECLARATION, LexicalType.DECLSCOPE),
 				new LexicalRule( LexicalType.NONE, LexicalType.NONE ),
 				new LexicalRule( LexicalType.STRING, LexicalType.NONE ),
 				new LexicalRule( LexicalType.LITERAL, LexicalType.FSCOPE ),
 				new LexicalRule( LexicalType.FSCOPE, LexicalType.FSCOPE ),
 				new LexicalRule( LexicalType.GSCOPE, LexicalType.GSCOPE ),
 				new LexicalRule( LexicalType.ENDSCOPE, LexicalType.FSCOPE ),
 				new LexicalRule( LexicalType.DECLSCOPE, LexicalType.DECLSCOPE )
 			}
 		);
 		
 	}
 	
 	public void addChainRule( GrammarRule rule, LexicalRule[] lexicalRule ) {
 		if ( this.rules.get(rule) == null ) {
 			this.rules.put( rule, new HashSet<LexicalRule>() );
 		}
 		
 		this.rules.get(rule).addAll( Arrays.asList( lexicalRule ) );
 	}
 	
 	public void addChainRule( GrammarRule rule, LexicalRule lexicalRule ) {
 		this.addChainRule( rule, new LexicalRule[] { lexicalRule } );
 	}
 	
 	protected StackWalker getStackWalker() {
 		return this.walker;
 	}
 	
 	public void setStackWalker( StackWalker walker ) {
 		this.walker = walker;
 	}
 	
 	protected ITokenizer getTokenizer() {
 		return this.tokenizer;
 	}
 	
 	protected LexicalType getLexicalType() {
 		return this.lexicalType;
 	}
 	
 	protected TokenType getGrammarType() {
 		return this.grammarType;
 	}
 	
 	protected int offset() {
 		return this.currentOffset;
 	}
 	
 	protected char current() {
 		return this.data.charAt( this.offset() );
 	}
 	
 	protected char back() {
 		this.currentOffset -= 1;
 		
 		return this.current();
 	}
 
 	protected void rewind() {
 		this.currentOffset--;
 	}
 
 	protected char seek() {
 		return this.data.charAt( this.currentOffset++ );
 	}
 	
 	protected boolean start() {
 		return this.currentOffset == 0;
 	}
 	
 	protected boolean end() {
 		return (this.data.length() - 1) < this.currentOffset;
 	}
 
 	protected LexicalRule findMatchLRule( GrammarRule rule ) {
 		for ( LexicalRule lexicalRule : this.rules.get( rule ) ) {
 			if ( lexicalRule.getFrom().equals( this.getLexicalType() ) ) {
 				return lexicalRule;
 			}
 		}
 
 		return null;
 	}
 
 	protected GrammarRule findMatchGRule( IToken token, boolean doGeneric ) {
 		GrammarRule result = null;
 		for ( GrammarRule rule : this.rules.keySet() ) {
 			boolean genericFromMatch = ( doGeneric && rule.getFrom().equals( TokenType.T_GENERIC ) );
 			boolean fullFromMatch = rule.getFrom().equals( this.getGrammarType() );
 			boolean toMatch = rule.getTo().equals( token.getType() );
 			
 			if ( toMatch ) {
 				if ( fullFromMatch ) {
 					result = rule;
 					break;
 				} else if ( doGeneric && genericFromMatch ) {
 					result = rule;
 					break;
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	protected boolean isScopeType( LexicalType type ) {
 		return type.equals( LexicalType.DECLSCOPE )
 				|| type.equals( LexicalType.FSCOPE )
 					|| type.equals( LexicalType.GSCOPE );
     }
 
 	@Override
 	public ISyntaxTree process() throws LexerException {
 		while ( !this.end() ) {
 			IToken token = this.getTokenizer().process( this.seek() );
 			if ( token.getType().equals( TokenType.T_UNKNOWN ) ) {
 				continue;
 			}
 
 			GrammarRule matchGRule = this.findMatchGRule(token, false);
 			if ( matchGRule == null ) {
 				matchGRule = this.findMatchGRule(token, true);
 			}
 			
 			if ( matchGRule == null ) {
 				this.onSyntaxException(token);
 			}
 			
 			LexicalRule matchLRule = this.findMatchLRule( matchGRule );
 			if ( matchLRule == null ) {
 				this.onUnexpectedToken( token );
 			}
 
 			if ( matchLRule.getTo().equals( LexicalType.NONE) ) {
 				continue;
 			}
 
 			this.lexicalType = matchLRule.getTo();
 			this.grammarType = matchGRule.getTo();
 
 			try {
 				switch ( this.lexicalType ) {
 				case DECLARATION:
 					this.onDeclaration( token );
 				break;
 				case FUNCTION:
 					this.onFunction( token, LexicalType.FSCOPE );
 				break;
 				case VARIABLE:
 					this.onVariable( token, LexicalType.FSCOPE );
 				break;
 				case LITERAL:
 					this.onLiteral( token, LexicalType.FSCOPE  );
 				break;
 				case ENDSCOPE:
 					this.onFScope(token);
 				break;
 				case STRING:
 					this.onString( token, LexicalType.STRING );
 				break;
 				case GSCOPE:
 					this.onGScope(token);
 				break;
 				}
 			} catch ( LexerException e ) {
 				throw new LexerException("Syntax exception on " + this.offset() + ": " + e.getMessage(), e );
 			}
 		}
 		
 		return this.getStackWalker().walk( this.syntaxStack );
 	}
 
 	protected void onSyntaxException( IToken token ) throws LexerException {
 		throw new LexerException("Syntax exception at: " + this.offset() + " on " +
 				"type " + token.getType() + " " + this.extractAnnotation() +
 				"\n Current lexical type: " + this.lexicalType.name()
 						+ "\n Current grammar type: " + this.grammarType.name() );
 	}
 
 	protected void onUnexpectedToken( IToken token ) throws LexerException {
 		throw new LexerException("Unexpected token at: " + this.offset() + " on " +
 				"type " + token.getType() + " " + this.extractAnnotation() +
 				"\n Current lexical type: " + this.lexicalType.name()
 						+ "\n Current grammar type: " + this.grammarType.name() );
 	}
 
 	protected String extractAnnotation() {
 		StringBuilder builder = new StringBuilder();
 
 		int start = this.offset() > 20 ? this.offset() - 20 : 0;
 		int end = ( this.offset() + 20 < this.data.length() ) ? this.offset() + 20 : this.offset();
 		for ( int i = start; i < end; i++ ) {
 			builder.append( this.data.substring( i, i+1 ) );
 		}
 
 		return builder.toString();
 	}
 
 	protected void onString( IToken token, LexicalType type ) {
 		String value = this.readLiteral( token, type, new StringFilter() ).replaceAll("\"", "");
 		log.info("String: " + value );
 		this.syntaxStack.add( new LiteralTreeNode(value) );
 	}
 
 	protected void onDeclScope( IToken token ) {
 		log.info("Declaration scope");
 		this.syntaxStack.add( new DeclarationScopeTreeNode() );
 	}
 
 	protected void onDeclaration( IToken token ) throws LexerException {
 		String name = this.readLiteral( token, LexicalType.DECLSCOPE, new IdentifierFilter()  ).toUpperCase();
 		if ( name.isEmpty() ) {
 			throw new LexerException("Empty declaration type");
 		}
 
 		log.info("Declaration: " + name );
 		DeclarationType type = DeclarationType.valueOf( name );
 		
 		this.syntaxStack.add( new DeclarationTreeNode( type ) );
 	}
 	
 	protected void onLiteral( IToken token, LexicalType type ) {
 		Object result;
 		String name = this.readLiteral(token, type, new IdentifierFilter() );
 		try {
 		   	result = Double.valueOf( name );
 		} catch ( NumberFormatException e ) {
 			try {
 				result = Integer.valueOf(name);
 			} catch ( NumberFormatException e1 ) {
 				if ( name.equals( "true") || name.equals( "false" ) ) {
 					result = Boolean.valueOf(name);
 				} else {
 					result = name;
 				}
 			}
 		}
 
 		log.info("Literal: " + name );
 		this.syntaxStack.add( new LiteralTreeNode(result) );
 	}
 	
 	protected void onFunction( IToken token, LexicalType type ) {
 		String name = this.readLiteral(token, type, new FunctionIdentifierFilter() );
 		if ( name.isEmpty() ) {
 			return;
 		}
 
 		log.info("Function: " + name );
 		this.syntaxStack.add( new FunctionTreeNode(name) );
 	}
 	
 	protected void onFScope( IToken token ) {
 		log.info("Functional scope...");
 		this.syntaxStack.add( new FunctionScopeTreeNode() );
 	}
 	
 	protected void onGScope( IToken token ) {
 		log.info("Global scope...");
 		this.syntaxStack.add( new ScopeTreeNode() );
 	}
 	
 	protected void onVariable( IToken token, LexicalType type ) {
 		String name = this.readLiteral(token, type, new IdentifierFilter() );
 		log.info("Variable: " + name );
 		this.syntaxStack.add( new VariableTreeNode(name) );
 	}
 	
 	protected String readLiteral( IToken token, LexicalType parentType, IFilter<TokenType> filter ) {
 		StringBuffer buff = new StringBuffer();
 		if ( token.getValue() != null ) {
 			buff.append( token.getValue() );
 		}
 		
 		char c;
 		TokenType type = token.getType();
 		while ( !this.end() ) {
 			c = this.seek();
 			
 			type = this.getTokenizer().process( c ).getType();
 			if( filter.filter( type ) ) {
 				buff.append(c);
 			} else {
 				this.rewind();
 				break;
 			}
 		} ;
 		
 		switch ( parentType ) {
 			case FUNCTION:
 				this.lexicalType = LexicalType.FSCOPE;
 			break;
 			case DECLARATION:
 				this.lexicalType = LexicalType.DECLSCOPE;
 			break;
 			default:
 				this.lexicalType = parentType;
 		}
 
 		this.grammarType = type;
 		
 		return buff.toString();
 	}
 	
 	protected static class StackWalker {
 
 		protected boolean isRelatedScope( IComposedSyntaxTreeNode node, IScopeTreeNode scope ) {
 			for ( Class<? extends IScopeTreeNode> related : node.getRelatedScopes() ) {
 				if ( related.isAssignableFrom( scope.getClass() ) ) {
 					return true;
 				}
 			}
 
 			return false;
 		}
 
 		protected boolean isObject( ISyntaxTreeNode node ) {
 			return node instanceof VariableTreeNode
 				&& node instanceof LiteralTreeNode;
 		}
 		
 		public ISyntaxTree walk( Queue<ISyntaxTreeNode> stack ) {
 			SyntaxTree tree = new SyntaxTree();
 
 			ISyntaxTreeNode node;
 			while( !stack.isEmpty() ) {
 				node = stack.poll();
 				if ( node instanceof IScopeTreeNode ) {
 					continue;
 				}
 
 				if ( node.isComposed() ) {
 					tree.insert( this.processElement( node.asComposedNode(), stack ) );
 				} else {
 					tree.insert( node );
 				}
 			}
 
 			return tree;
 		}
 		
 		public ISyntaxTreeNode processElement( IComposedSyntaxTreeNode node, final Queue<ISyntaxTreeNode> stack ) {
 			while ( !stack.isEmpty() ) {
 				ISyntaxTreeNode childNode = stack.poll();
 				if ( childNode instanceof IScopeTreeNode ) {
 					if ( this.isRelatedScope( node, (IScopeTreeNode) childNode ) ) {
 						break;
 					} else {
 						continue;
 					}
 				}
 				
 				if ( !childNode.isComposed() ) {
 					if ( !this.isObject(childNode) ) {
 						node.addArgument( childNode );
 					}
 				} else {
 					node.addArgument( this.processElement( childNode.asComposedNode(), stack ) );
 				}
 			}
 			
 			return node;
 		}
 	}
 
 	public static class StringFilter implements IFilter<TokenType> {
 		@Override
 		public boolean filter(TokenType filterable) {
 			return !filterable.equals( TokenType.T_STRING );
 		}
 	}
 
 	public static class FunctionIdentifierFilter extends IdentifierFilter {
 
 		@Override
 		public boolean filter( TokenType type ) {
 			return super.filter(type);
 		}
 
 	}
 
 	public static class IdentifierFilter implements IFilter<TokenType> {
 		@Override
 		public boolean filter(TokenType type ) {
 			return type.equals( TokenType.T_LITERAL )
 					|| type.equals( TokenType.T_PATH );
 		}
 	}
 	
 }
