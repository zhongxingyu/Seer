 package gr.agroknow.metadata.agrif;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 public class Expression 
 {
 
 	private JSONObject expression ;
 	
 	public Expression()
 	{
 		expression = new JSONObject() ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setLanguage( String language )
 	{
 		JSONArray languages ;
 		if ( expression.containsKey( "language" ) )
 		{
 			languages = (JSONArray)expression.get( "language" ) ;
 		}
 		else
 		{
 			languages = new JSONArray() ;
 		}
 		languages.add( language ) ;
 		expression.put( "language" , languages ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setManifestation( Manifestation manifestation )
 	{
 		JSONArray manifestations ;
 		if ( expression.containsKey( "manifestations" ) )
 		{
 			manifestations = (JSONArray)expression.get( "manifestations" ) ;
 		}
 		else
 		{
 			manifestations = new JSONArray() ;
 		}
 		manifestations.add( manifestation.toJSONObject() ) ;
 		expression.put( "manifestations" , manifestations ) ;
 	}	
 
 	@SuppressWarnings("unchecked")
 	public void setCitation( Citation citation )
 	{
 		JSONArray citations ;
 		if ( expression.containsKey( "citation" ) )
 		{
 			citations = (JSONArray)expression.get( "citation" ) ;
 		}
 		else
 		{
 			citations = new JSONArray() ;
 		}
 		citations.add( citation.toJSONObject() ) ;
 		expression.put( "citation" , citations ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setDateIssued( String date )
 	{
 		JSONArray publishers = new JSONArray() ;
 		if ( expression.containsKey( "publisher" ) )
 		{
 			JSONArray oldpublishers = (JSONArray)expression.get( "publisher" ) ;
 			for (Object pub: oldpublishers)
 			{
 				JSONObject publisher = (JSONObject) pub ;
 				publisher.put( "date", date ) ;
 				publishers.add( publisher ) ;
 			}
 		}
 		else
 		{
 			JSONObject publisher = new JSONObject() ;
 			publisher.put( "date", date ) ;
 			publishers.add( publisher ) ;
 		}
 		expression.put( "publisher" , publishers ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setPublisher( Publisher publisher )
 	{
 		JSONArray publishers ;
 		if ( expression.containsKey( "publisher" ) )
 		{
 			publishers = (JSONArray)expression.get( "publisher" ) ;
 		}
 		else
 		{
 			publishers = new JSONArray() ;
 		}
 		publishers.add( publisher.toJSONObject() ) ;
 		expression.put( "publisher" , publishers ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setPublisher( String name, String date, String location )
 	{
 		JSONArray publishers ;
 		if ( expression.containsKey( "publisher" ) )
 		{
 			publishers = (JSONArray)expression.get( "publisher" ) ;
 		}
 		else
 		{
 			publishers = new JSONArray() ;
 		}
 		JSONObject publisher = new JSONObject() ;
 		if ( (name != null) && !name.isEmpty() )
 		{
 			publisher.put( "name", name ) ;
 		}
 		if ( (date != null) && !date.isEmpty() )
 		{
 			publisher.put( "date", date ) ;
 		}
 		if ( (location != null) && !location.isEmpty() )
 		{
 			publisher.put( "location", location ) ;
 		}
 		if ( !publisher.isEmpty() )
 		{
 			publishers.add( publisher ) ;
 			expression.put( "publisher" , publishers ) ;
 		}
 	}	
 	
 	@SuppressWarnings("unchecked")
 	public void setFullCitation( String fullCitation )
 	{
 		JSONArray fullCitations ;
 		if ( expression.containsKey( "fullCitation" ) )
 		{
 			fullCitations = (JSONArray)expression.get( "fullCitation" ) ;
 		}
 		else
 		{
 			fullCitations = new JSONArray() ;
 		}
 		fullCitations.add( fullCitation ) ;
		expression.put( "fullCitation" , fullCitations ) ;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setDescriptionEdition( String descriptionEdition )
 	{
 		JSONArray descriptionEditions ;
 		if ( expression.containsKey( "descriptionEdition" ) )
 		{
 			descriptionEditions = (JSONArray)expression.get( "descriptionEdition" ) ;
 		}
 		else
 		{
 			descriptionEditions = new JSONArray() ;
 		}
 		descriptionEditions.add( descriptionEdition ) ;
 		expression.put( "descriptionEdition" , descriptionEditions ) ;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public void setPublicationStatus( String source, String value )
 	{
 		JSONObject status = new JSONObject() ;
 		status.put( "source", source ) ;
 		status.put( "value", value ) ;
 		expression.put( "publicationStatus", status ) ;
 	}
 	
 	public JSONObject toJSONObject()
 	{
 		return expression ;
 	}
 	
 	public String toJSONString()
 	{
 		return expression.toJSONString() ;
 	}
 	
 }
