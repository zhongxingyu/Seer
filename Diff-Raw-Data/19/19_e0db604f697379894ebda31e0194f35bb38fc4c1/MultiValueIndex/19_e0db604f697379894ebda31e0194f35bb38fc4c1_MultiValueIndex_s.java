 package org.neo4j.util.index;
 
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.util.btree.KeyEntry;
 
 /**
  * A "multi" index implementation using {@link org.neo4j.util.btree.BTree BTree}
  * that can index multiple nodes per key. They key is checked for equality 
  * using both <CODE>hashCode</CODE> and <CODE>equal</CODE> methods. 
  * <p>
  * Note: this implementation is not thread safe (yet).
  */
 // not thread safe yet
 public class MultiValueIndex extends AbstractIndex
 {
 	/**
 	 * Creates/loads a index. The <CODE>underlyingNode</CODE> can either
 	 * be a new (just created) node or a node that already represents a 
 	 * previously created index.
 	 *
 	 * @param name The unique name of the index or null if index already
 	 * created (using specified underlying node)
 	 * @param underlyingNode The underlying node representing the index
 	 * @param neo The embedded neo instance
 	 * @throws IllegalArgumentException if the underlying node is a index with
 	 * a different name set.
 	 */
 	public MultiValueIndex( String name, Node underlyingNode, NeoService neo )
 	{
 		super( name, underlyingNode, neo );
 	}
 	
 	@Override
 	protected void addOrReplace( KeyEntry entry, long nodeId )
 	{
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			long[] newValues = new long[values.length + 1];
 			boolean addNewValues = true;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] == nodeId )
 				{
 					addNewValues = false;
 					break;
 				}
 				newValues[i] = values[i];
 			}
 			if ( addNewValues )
 			{
 				newValues[newValues.length - 1] = nodeId;
 				entry.setValue( newValues );
 			}
 		}
         else
         {
     		long currentId = (Long) value;
     		if ( currentId != nodeId )
     		{
     			long[] newValues = new long[2];
     			newValues[0] = currentId;
     			newValues[1] = nodeId;
     			entry.setValue( newValues );
     		}
         }
 	}
 	
 	@Override
 	protected void addOrReplace( Node node, long nodeId )
 	{
 		Object value = node.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			long[] newValues = new long[values.length + 1];
 			boolean addNewValues = true;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] == nodeId )
 				{
 					addNewValues = false;
 					break;
 				}
 				newValues[i] = values[i];
 			}
 			if ( addNewValues )
 			{
 				newValues[newValues.length - 1] = nodeId;
 				node.setProperty( INDEX_VALUES, newValues );
 			}
 		}
		long currentId = (Long) value;
		if ( currentId != nodeId )
		{
			long[] newValues = new long[2];
			newValues[0] = currentId;
			newValues[1] = nodeId;
			node.setProperty( INDEX_VALUES, newValues );
		}
 	}
 	
 	@Override
 	protected boolean removeAllOrOne( Node node, long nodeId )
 	{
 		Object value = node.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			if ( values.length == 1 )
 			{
 				if ( values[0] == nodeId )
 				{
 					return true;
 				}
 				return false;
 			}
 			long[] newValues = new long[values.length - 1];
 			int j = 0;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] != nodeId )
 				{
 					newValues[j++] = values[i];
 				}
 			}
 			node.setProperty( INDEX_VALUES, newValues );
 			return false;
 		}
 		long currentId = (Long) value;
 		if ( currentId == nodeId )
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected boolean removeAllOrOne( KeyEntry entry, long nodeId )
 	{
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			long[] values = (long[]) value;
 			if ( values.length == 1 )
 			{
 				if ( values[0] == nodeId )
 				{
 					return true;
 				}
 				return false;
 			}
 			long[] newValues = new long[values.length - 1];
 			int j = 0;
 			for ( int i = 0; i < values.length; i++ )
 			{
 				if ( values[i] != nodeId )
 				{
 					newValues[j++] = values[i];
 				}
 			}
 			entry.setValue( newValues );
 			return false;
 		}
 		long currentId = (Long) value;
 		if ( currentId == nodeId )
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected long[] getValues( KeyEntry entry )
 	{
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			return (long[]) value;
 		}
 		long values[] = new long[1];
 		values[0] = (Long) value;
 		return values;
 	}
 
 	@Override
 	protected long[] getValues( Node node )
 	{
 		Object value = node.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			return (long[]) value;
 		}
 		long values[] = new long[1];
 		values[0] = (Long) value;
 		return values;
 	}
 
 	@Override
     protected String getIndexType()
     {
 		return "multi";
     }
 
 	@Override
     protected long getSingleValue( KeyEntry entry )
     {
 		Object value = entry.getValue();
 		if ( value.getClass().isArray() )
 		{
 			long[] ids = (long[]) value;
 			if ( ids.length > 1 )
 			{
 				throw new RuntimeException( "Multiple values found" );
 			}
 			return ids[0];
 		}
 		return (Long) value;
     }
 
 	@Override
     protected long getSingleValue( Node entry )
     {
 		Object value = entry.getProperty( INDEX_VALUES );
 		if ( value.getClass().isArray() )
 		{
 			long[] ids = (long[]) value;
 			if ( ids.length > 1 )
 			{
 				throw new RuntimeException( "Multiple values found" );
 			}
 			return ids[0];
 		}
 		return (Long) value;
     }	
 }
