 package com.windh.util.neo;
 
 import java.lang.reflect.Constructor;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.impl.core.NodeManager;
 import org.neo4j.impl.core.NotFoundException;
 
 public abstract class NodeWrapper
 {
 	private Node node;
 	
 	protected NodeWrapper( Node node )
 	{
 		this.node = node;
 	}
 	
 	public Node getUnderlyingNode()
 	{
 		return node;
 	}
 	
 	public static <T extends NodeWrapper> T newInstance(
 		Class<T> instanceClass, long nodeId ) throws NotFoundException
 	{
 		Transaction tx = Transaction.begin();
 		try
 		{
 			Node node = NodeManager.getManager().getNodeById( ( int ) nodeId );
 			return newInstance( instanceClass, node );
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	public static <T extends NodeWrapper> T newInstance(
 		Class<T> instanceClass, Node node )
 	{
 		try
 		{
 			Constructor<T> constructor =
 				instanceClass.getConstructor( Node.class );
 			T result = constructor.newInstance( node );
 			return result;
 		}
 		catch ( RuntimeException e )
 		{
 			throw e;
 		}
 		catch ( Exception e )
 		{
 			throw new RuntimeException( e );
 		}
 	}
 	
 	@Override
 	public boolean equals( Object o )
 	{
		if ( o == null || !getClass().equals( o.getClass() ) )
 		{
 			return false;
 		}
 		return getUnderlyingNode().equals(
 			( ( NodeWrapper ) o ).getUnderlyingNode() );
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		return getUnderlyingNode().hashCode();
 	}
 }
