 package org.jboss.pressgangccms.rest.v1.entities;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTTranslatedTopicStringCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityV1;
 
 public class RESTTranslatedTopicStringV1 extends RESTBaseEntityV1<RESTTranslatedTopicStringV1, RESTTranslatedTopicStringCollectionV1>
 {
 	public static final String ORIGINALSTRING_NAME = "originalString";
 	public static final String TRANSLATEDSTRING_NAME = "translatedString";
 	public static final String TRANSLATEDTOPIC_NAME = "translatedTopic";
 	public static final String FUZZY_TRANSLATION_NAME = "fuzzyTranslation";
 	
 	private RESTTranslatedTopicV1 translatedTopic = null;
 	private String originalString = null;
 	private String translatedString = null;
 	private Boolean fuzzyTranslation = null;
 	/** A list of the Envers revision numbers */
 	private RESTTranslatedTopicStringCollectionV1 revisions = null;
 	
 	@Override
 	public RESTTranslatedTopicStringCollectionV1 getRevisions()
 	{
 		return revisions;
 	}
 
 	@Override
 	public void setRevisions(final RESTTranslatedTopicStringCollectionV1 revisions)
 	{
 		this.revisions = revisions;
 	}
 	
 	@Override
 	public RESTTranslatedTopicStringV1 clone(final boolean deepCopy)
 	{
 		final RESTTranslatedTopicStringV1 retValue = new RESTTranslatedTopicStringV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.setOriginalString(this.originalString);
 		retValue.setTranslatedString(this.translatedString);
 		
 		if (deepCopy)
 		{
 			retValue.setTranslatedTopic(translatedTopic != null ? this.translatedTopic.clone(deepCopy) : null);
 			
 			if (this.revisions != null)
 			{
 				retValue.revisions = new RESTTranslatedTopicStringCollectionV1();
 				this.revisions.cloneInto(retValue.revisions, deepCopy);
 			}
 		}
 		else
 		{
 			retValue.setTranslatedTopic(this.translatedTopic);
 			retValue.setRevisions(this.revisions);
 		}
 		return retValue;
 	}
 
 	public RESTTranslatedTopicV1 getTranslatedTopic()
 	{
 		return translatedTopic;
 	}
 
 	public void setTranslatedTopic(final RESTTranslatedTopicV1 translatedTopic)
 	{
 		this.translatedTopic = translatedTopic;
 	}
 
 	public String getOriginalString()
 	{
 		return originalString;
 	}
 
 	public void setOriginalString(final String originalString)
 	{
 		this.originalString = originalString;
 	}
 	
 	public void explicitSetOriginalString(final String originalString)
 	{
 		this.originalString = originalString;
 		this.setParameterToConfigured(ORIGINALSTRING_NAME);
 	}
 
 	public String getTranslatedString()
 	{
 		return translatedString;
 	}
 
 	public void setTranslatedString(final String translatedString)
 	{
 		this.translatedString = translatedString;
 	}
 	
 	public void explicitSetTranslatedString(final String translatedString)
 	{
 		this.translatedString = translatedString;
 		this.setParameterToConfigured(TRANSLATEDSTRING_NAME);
 	}
 
     public Boolean getFuzzyTranslation()
     {
         return fuzzyTranslation;
     }
 
     public void setFuzzyTranslation(final Boolean fuzzyTranslation)
     {
         this.fuzzyTranslation = fuzzyTranslation;
     }
     
     public void explicitSetFuzzyTranslation(final Boolean fuzzyTranslation)
     {
         this.fuzzyTranslation = fuzzyTranslation;
         this.setParameterToConfigured(FUZZY_TRANSLATION_NAME);
     }
 }
