 
 package edu.wustl.cab2b.common.queryengine.result;
 
 import java.io.Serializable;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 
 public class RecordId implements Serializable
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -369614319664848819L;
 
 	/**
 	 * The "identifier" is the value for the id attribute as obtained from
 	 * {@link edu.wustl.cab2b.common.util.Utility#getIdAttribute(EntityInterface)}.
 	 */
	private String identifier;
 
 	private String url;
 
	public RecordId(String identifier, String url)
 	{
		if (identifier == null || url == null)
 		{
 			throw new IllegalArgumentException();
 		}
		this.identifier = identifier;
 		this.url = url;
 	}
 
 	public String getId()
 	{
		return identifier;
 	}
 
 	public String getUrl()
 	{
 		return url;
 	}
 }
