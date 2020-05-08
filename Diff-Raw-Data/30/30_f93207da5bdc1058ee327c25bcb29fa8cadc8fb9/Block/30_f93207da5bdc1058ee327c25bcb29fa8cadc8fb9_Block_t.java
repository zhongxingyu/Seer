 package kaygan.ast;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import kaygan.Scope;
 
 public abstract class Block extends Exp implements Iterable<Exp>
 {
 	protected final Map<Symbol, ASTNode> bindings 
 										= new LinkedHashMap<Symbol, ASTNode>();
 	
 	protected final Exp[] exps;
 	
 	public Block( Exp[] exps )
 	{
 		this.exps = exps;
 		
 		for( Exp exp : exps )
 		{
 			if( exp instanceof Bind )
 			{
 				Bind bind = (Bind)exp;
 				bindings.put( bind.symbol, bind.exp );
 			}
 		}
 	}
 	
 	public int size()
 	{
 		return exps.length;
 	}
 	
 	public Exp get(int index)
 	{
 		return exps[index];
 	}
 	
 	@Override
 	public Iterator<Exp> iterator()
 	{
 		return Arrays.asList(exps).iterator();
 	}
 	
 	@Override
 	public int getOffset()
 	{
 		return exps.length > 0 ? exps[0].getOffset() : 0;
 	}
 
 	@Override
 	public int getLength()
 	{
 		if( exps.length > 0 )
 		{
 			Exp first = exps[0];
 			Exp last = exps[ exps.length - 1 ];
 			return last.getOffset() - first.getOffset() + last.getLength();
 		}
 		return 0;
 	}
 	
 	@Override
 	public ASTNode findNode(int offset)
 	{
 		for( Exp exp : exps )
 		{
 			if( exp.overlaps(offset) )
 			{
 				return exp.findNode(offset);
 			}
 		}
 		return overlaps(offset) ? this : null;
 	}
 	
 	@Override
 	public void link(Scope scope)
 	{
 		for( Exp e : this )
 		{
 			if( e instanceof Bind )
 			{
 				Bind bind = (Bind)e;
				
				String key = bind.symbol.symbol();
				
				Object bound = scope.getLocal(key);
				
				if( bound != null )
				{
					e.error("Symbol " + key + " already bound to " + bound);
				}
				else
				{
					scope.set( key, bind.exp );
				}
 			}
 		}
 		
 		for( Exp e : this )
 		{
 			e.link(scope);
 		}
 	}
 	
 }
