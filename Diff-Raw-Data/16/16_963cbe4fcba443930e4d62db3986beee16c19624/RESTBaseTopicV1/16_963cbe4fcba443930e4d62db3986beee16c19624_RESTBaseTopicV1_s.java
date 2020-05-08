 package org.jboss.pressgangccms.rest.v1.entities.base;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.base.RESTBaseCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTagV1;
 
 public abstract class RESTBaseTopicV1<T extends RESTBaseTopicV1<T, U, V>, U extends RESTBaseCollectionV1<T, U, V>, V extends RESTBaseCollectionItemV1<T, U, V>>
     extends RESTBaseEntityWithPropertiesV1<T, U, V>
 {
 	public static final String TITLE_NAME = "title";
 	public static final String XML_NAME = "xml";
 	public static final String XML_ERRORS_NAME = "xmlErrors";
 	public static final String HTML_NAME = "html";
 	public static final String TAGS_NAME = "tags";
 	public static final String OUTGOING_NAME = "outgoingRelationships";
 	public static final String INCOMING_NAME = "incomingRelationships";
 	public static final String LOCALE_NAME = "locale";
 	public static final String SOURCE_URLS_NAME = "sourceUrls_OTM";
 	
 	protected String title = null;
 	protected String xml = null;
 	protected String xmlErrors = null;
 	protected String html = null;
	protected Integer revision = null;
 	protected String locale = null;
 	protected RESTTagCollectionV1 tags = null;
 	protected RESTTopicSourceUrlCollectionV1 sourceUrls_OTM = null;
 	
 	abstract public U getIncomingRelationships();
 	abstract public void setIncomingRelationships(final U incomingRelationships);
 	abstract public U getOutgoingRelationships();
 	abstract public void setOutgoingRelationships(final U outgoingRelationships);
 
 	public void cloneInto(final RESTBaseTopicV1<?, ?, ?> clone, final boolean deepCopy)
 	{
 		super.cloneInto(clone, deepCopy);
 		
 		clone.title = this.title;
 		clone.xml = this.xml;
 		clone.xmlErrors = this.xmlErrors;
 		clone.html = this.html;
		clone.revision = this.revision;
 		clone.locale = this.locale;
 		
 		if (deepCopy)
 		{
 			if (this.tags != null)
 			{
 				clone.tags = new RESTTagCollectionV1();
 				this.tags.cloneInto(clone.tags, deepCopy);
 			}
 						
 			if (this.sourceUrls_OTM != null)
 			{
 				clone.sourceUrls_OTM = new RESTTopicSourceUrlCollectionV1();
 				this.sourceUrls_OTM.cloneInto(clone.sourceUrls_OTM, deepCopy);
 			}
 		}
 		else
 		{
 			clone.tags = this.tags;
 			clone.sourceUrls_OTM = this.sourceUrls_OTM;
 		}
 	}
	
	@Override
	public Integer getRevision()
	{
		return revision;
	}

	@Override
	public void setRevision(final Integer revision)
	{
		this.revision = revision;
	}
 
 	public String getTitle()
 	{
 		return title;
 	}
 
 	public void setTitle(final String title)
 	{
 		this.title = title;
 	}
 	
 	public String getXml()
 	{
 		return xml;
 	}
 	
 	public void setXml(final String xml)
 	{
 		this.xml = xml;
 	}
 	
 	public String getHtml()
 	{
 		return html;
 	}
 
 	public void setHtml(final String html)
 	{
 		this.html = html;
 	}
 	
 	public String getLocale()
 	{
 		return locale;
 	}
 
 	public void setLocale(final String locale)
 	{
 		this.locale = locale;
 	}
 
 	public String getXmlErrors()
 	{
 		return xmlErrors;
 	}
 
 	public void setXmlErrors(final String xmlErrors)
 	{
 		this.xmlErrors = xmlErrors;
 	}
 	
 	public RESTTagCollectionV1 getTags()
 	{
 		return tags;
 	}
 
 	public void setTags(final RESTTagCollectionV1 tags)
 	{
 		this.tags = tags;
 	}
 	
 	public void addTag(final RESTTagV1 tag)
 	{
 		if (this.tags == null)
 			this.tags = new RESTTagCollectionV1();
 
 		this.tags.addItem(tag);
 	}	
 
 	public RESTTopicSourceUrlCollectionV1 getSourceUrls_OTM()
 	{
 		return sourceUrls_OTM;
 	}
 
 	public void setSourceUrls_OTM(final RESTTopicSourceUrlCollectionV1 sourceUrls)
 	{
 		this.sourceUrls_OTM = sourceUrls;		
 	}
 }
