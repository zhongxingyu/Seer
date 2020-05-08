 package org.neo4j.rdf.sparql;
 
 import org.neo4j.meta.model.MetaModel;
 import org.neo4j.meta.model.MetaModelClass;
 import org.neo4j.rdf.store.representation.AbstractNode;
 
 /**
  * An implementation of the {@link MetaModelProxy} interface which is used in
 * the SPARQL engine for neo.
  */
 public class MetaModelProxyImpl implements MetaModelProxy
 {
     private MetaModel model;
     
     public MetaModelProxyImpl( MetaModel model )
     {
         this.model = model;
     }
     
 	/**
 	 * @return the property key representing the rdf:about property.
 	 */
 //	public String getAboutKey()
 //	{
 //        throw new UnsupportedOperationException();
 //	}
 	
 //	public String getNodeTypeNameKey()
 //	{
 //        throw new UnsupportedOperationException();
 //	}
 
 //	/**
 //	 * @return the underlying {@link Node} for the {@link NodeType} with the URI
 //	 * <code>uri</code>. 
 //	 */
 //	public Node getClassNode( String uri )
 //	{
 //        throw new UnsupportedOperationException();
 //	}
 
 //	/**
 //	 * @return the number of instances of the {@link NodeType} with URI
 //	 * <code>uri</code>.
 //	 */
 	public int getCount( AbstractNode node )
 	{
 		int count = Integer.MAX_VALUE;
 		if ( node.getUriOrNull() != null )
 		{
 			String uri = node.getUriOrNull().getUriAsString();
 		    MetaModelClass cls =
 		        model.getGlobalNamespace().getMetaClass(
 		        	node.getUriOrNull().getUriAsString(), false );
 	//	    if ( cls == null )
 	//	    {
 	//	        cls = meta.getGlobalNamespace().getMetaProperty( uri, false );
 	//	    }
 		    if ( cls == null )
 		    {
 		        throw new RuntimeException( "Not found '" + uri + "'" );
 		    }
 		    count = cls.getInstances().size();
 		}
 	    
 	    return count;
 	}
 	
 	/**
 	 * @param subjectUri not used yet
 	 * @param predicateUri the name of the property.
 	 * @param objectUri not used yet
 	 * @return a property definition in the meta model.
 	 */
 //	public OwlProperty getOwlProperty( String subjectUri, String predicateUri,
 //		String objectUri )
 //	{
 //	    throw new UnsupportedOperationException();
 //	}
 
 	/**
 	 * @return the {@link RelationshipType} used for connecting
 	 * {@link OwlInstance} object to {@link NodeType} objects.
 	 */
 //	public RelationshipType getTypeRelationship()
 //	{
 //        throw new UnsupportedOperationException();
 //	}
 
 	/**
 	 * @return <code>true</code> if <code>uri</code> is the URI representing
 	 * an RDF type.
 	 */
 	public boolean isTypeProperty( String uri )
 	{
         throw new UnsupportedOperationException();
 	}
 	
 //	public Object convertCriteriaStringValueToRealValue( String propertyKey,
 //		String value )
 //	{
 //        throw new UnsupportedOperationException();
 //	}
 
 //	public String getObjectType( String subjectUri,
 //		String predicateUri )
 //	{
 //        throw new UnsupportedOperationException();
 //	}
 	
 //	public String[] getSubTypes( String type, boolean includeMyself )
 //	{
 //	    MetaStructureClass cls =
 //	        meta.getGlobalNamespace().getMetaClass( type, false );
 //	    if ( cls == null )
 //	    {
 //	        throw new RuntimeException();
 //	    }
 //	    Set<String> classes = new HashSet<String>();
 //	    addType( classes, cls );
 //	    return classes.toArray( new String[ classes.size() ] );
 //	}
 	
 //	private void addType( Collection<String> result, MetaStructureClass cls )
 //	{
 //		result.add( cls.getName() );
 //		for ( MetaStructureClass sub : cls.getDirectSubs() )
 //		{
 //			addType( result, sub );
 //		}
 //	}
 }
