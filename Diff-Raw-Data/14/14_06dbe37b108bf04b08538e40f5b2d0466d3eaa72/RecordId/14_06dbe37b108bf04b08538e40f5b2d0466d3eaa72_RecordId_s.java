 
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
	private String id;
 
 	private String url;
 
	public RecordId(String id, String url)
 	{
		if (id == null || url == null)
 		{
 			throw new IllegalArgumentException();
 		}
		this.id = id;
 		this.url = url;
 	}
 
 	public String getId()
 	{
		return id;
 	}
 
 	public String getUrl()
 	{
 		return url;
 	}
 }
