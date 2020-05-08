 package kaygan.ast;
 
 import kaygan.Scope;
 import kaygan.type.Type;
 
 public class Bind extends Exp
 {
 	public final Symbol symbol;
 	
 	public final ASTNode exp;
 	
 	public Bind(Symbol symbol, ASTNode exp)
 	{
 		this.symbol = symbol;
 		this.exp = exp;
 	}
 	
 	
 	
 	@Override
 	public int getOffset()
 	{
 		return symbol.getOffset();
 	}
 
 	@Override
 	public int getLength()
 	{
 		// exp.offset will be zero if the bound node is synthetic
 		if( exp.getOffset() > 0 )
 		{
 			return exp.getOffset() - symbol.getOffset() + exp.getLength();
 		}
 		else
 		{
 			return symbol.getLength();
 		}
 	}
 
 	@Override
 	public ASTNode findNode(int offset)
 	{
 		// if this node has errors, we want
 		// it to 'cover' its contents
 		if( hasErrors() && overlaps(offset) )
 		{
 			return this;
 		}
 		
 		if( symbol.overlaps(offset) )
 		{
 			return symbol;
 		}
 		
 		if( exp.overlaps(offset) )
 		{
 			return exp.findNode(offset);
 		}
 		
 		return overlaps(offset) ? this : null;
 	}
 	
 	@Override
 	public void link(Scope scope)
 	{
 		if( this.exp instanceof Bind )
 		{
 			this.exp.error("Cannot bind to a bind");
 		}
 		else
 		{
 			// link the expression before we set the scope
 			// that way it is not seeing its own value
 			this.exp.link( scope );
 			
 			String key = this.symbol.symbol();
 			
			scope.set( key, this.exp );
 			
 			this.symbol.ref = this.exp;
 		}
 	}
 	
 	@Override
 	public Type inferType()
 	{
 		super.inferType();
 		
 		if( this.type != null )
 		{
 			return this.type;
 		}
 		
 		exp.inferType();
 		
 		this.type = this.symbol.type = (this.exp instanceof Type) 
 										? (Type)this.exp 
 										: this.exp.type;
 		
 		return this.type;
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("{ Bind: ")
 			.append(symbol).append(':').append(exp).append('}');
 		return sb.toString();
 	}
 }
