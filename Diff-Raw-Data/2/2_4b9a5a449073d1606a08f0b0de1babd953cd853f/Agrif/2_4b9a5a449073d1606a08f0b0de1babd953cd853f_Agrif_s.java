 package gr.agroknow.metadata.agrif ;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 public class Agrif
 {
 	public JSONObject agrif ;
 	public JSONArray creators ;
 	public JSONArray rights ;
 	public JSONArray relations ;
 	public JSONArray expressions ;
     
 	
 	public Agrif()
 	{
 		agrif = new JSONObject() ;
 		creators = new JSONArray() ;
 		rights = new JSONArray() ;
 		relations = new JSONArray() ;
 		expressions = new JSONArray() ;
 	}
     
 	@SuppressWarnings("unchecked")
 	public void setAgrifIdentifier( int agrifIdentifier )
 	{
 		agrif.put( "agrifIdentifier", agrifIdentifier ) ;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setSet( String set )
 	{
 		agrif.put( "set", set ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setStatus( String status )
 	{
 		agrif.put( "status", status ) ;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setCreationDate( String date )
 	{
 		agrif.put( "creationDate", date ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setLastUpdateDate( String date )
 	{
 		agrif.put( "lastUpdateDate", date ) ;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setOrigin( String providerId, String arn )
 	{
 		JSONObject origins ;
 		if ( agrif.containsKey( "origin" ) )
 		{
 			origins = (JSONObject) agrif.get( "origin" ) ;
 		}
 		else
 		{
 			origins = new JSONObject() ;
 		}
 		origins.put( providerId, arn ) ;
 		agrif.put( "origin", origins ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setRights( Rights rights )
 	{
 		this.rights.add( rights.toJSONObject() ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setCreator( Creator creator )
 	{
 		creators.add( creator.toJSONObject() ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setRelation( Relation relation )
 	{
 		relations.add( relation.toJSONObject() ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setLanguageBlocks( LanguageBlock lblock )
 	{
 		JSONObject languageBlocks ; 
 		if ( agrif.containsKey( "languageBlocks" ) )
 		{
 			languageBlocks = (JSONObject)agrif.get( "languageBlocks" ) ;
 		}
 		else
 		{
 			languageBlocks = new JSONObject() ;
 		}
 		languageBlocks.putAll( lblock.toJSONObject() ) ;
 		agrif.put( "languageBlocks", languageBlocks ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setControlled( ControlledBlock block )
 	{
 		agrif.put( "controlled" , block.toJSONObject() ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setExpression( Expression expression )
 	{
 		expressions.add( expression.toJSONObject() ) ;
 	}
 	
 	public JSONObject toJSONObject()
 	{
 		assemble() ;
 		return agrif ;
 	}
 	
 	public String toJSONString()
 	{
 		assemble() ;
 		return agrif.toJSONString() ;
 	}
      
 	@SuppressWarnings("unchecked")
 	private void assemble()
 	{
 		if ( !creators.isEmpty() )
 		{
 			agrif.put( "creator" , creators ) ;
 		}
 		if ( !rights.isEmpty() )
 		{
 			agrif.put( "rights", rights ) ;
 		}
 		if ( !relations.isEmpty() )
 		{
 			agrif.put( "relation", relations ) ;
 		}
 		if ( !expressions.isEmpty() )
 		{
			agrif.put( "expression", expressions ) ;
 		}
 	}
 }
