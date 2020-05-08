 package cz.cuni.mff.odcleanstore.webfrontend.bo.en;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import cz.cuni.mff.odcleanstore.webfrontend.bo.EntityWithSurrogateKey;
 
 public class Pipeline extends EntityWithSurrogateKey
 {
 	private static final long serialVersionUID = 1L;
 
 	private String label;
 	private String description;
 	private Boolean isDefault;
 	private Boolean isLocked;
 	private List<TransformerInstance> transformers;
 	
 	/**
 	 * 
 	 * @param id
 	 * @param label
 	 * @param description
 	 * @param isDefault
 	 */
 	public Pipeline(Integer id, String label, String description, Boolean isDefault, Boolean isLocked) 
 	{
 		super(id);
 		
 		this.transformers = new LinkedList<TransformerInstance>();
 		
 		this.label = label;
 		this.description = description;
 		this.isDefault = isDefault;
 		this.isLocked = isLocked;
 	}
 	
 	/**
 	 * 
 	 */
 	public Pipeline()
 	{
 		transformers = new LinkedList<TransformerInstance>();
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getLabel() 
 	{
 		return label;
 	}
	
	public void setLabel(String label) 
	{
		this.label = label;
	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getDescription() 
 	{
 		return description;
 	}
	
	public void setDescription(String description) 
	{
		this.description = description;
	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Boolean isDefault() 
 	{
 		return isDefault;
 	}
 	
 	/**
 	 * 
 	 * @param isDefault
 	 */
 	public void setDefault(Boolean isDefault)
 	{
 		this.isDefault = isDefault;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Boolean isLocked() 
 	{
 		return isLocked;
 	}
 	
 	/**
 	 * 
 	 * @param isLocked
 	 */
 	public void setLocked(Boolean isLocked)
 	{
 		this.isLocked = isLocked;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public List<TransformerInstance> getTransformers()
 	{
 		return transformers;
 	}
 	
 	/**
 	 * 
 	 * @param transformers
 	 */
 	public void setTransformers(List<TransformerInstance> transformers)
 	{
 		this.transformers = transformers;
 	}
 }
