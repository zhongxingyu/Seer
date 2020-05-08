 package au.edu.labshare.schedserver.scormpackager.manifest;
 
 import java.util.ArrayList;
 
 import au.edu.labshare.schedserver.scormpackager.manifest.Dependency;
 import au.edu.labshare.schedserver.scormpackager.manifest.ResourceFile;
 import au.edu.labshare.schedserver.scormpackager.manifest.MetaData;
 
 public class Resource 
 {
 	/* Unique to the adlscp xsd schema types that are defined by . Can be extended.*/
 	public static final String SCORMTYPE_SCO = "SCO";
 	public static final String SCORMTYPE_ASSET = "ASSET";
 	
 	/* Technically can be any path, but we are going to have a unique default path specific 
 	 * to LiLa but will allow for different paths */
 	public static final String RESOURCE_LILA_PATH = "lib";
 	
 	/* Type is set to webcontent and there is no reference where this is otherwise. */
 	public static final String LILA_TYPE = "webcontent";
 	
 	String id;
 	String type; //According to the adlcp any type is allowed but needs to be specified. 
 	String scormtype; //Can be either SCO or asset as designated by adlscp:scormtype
 	String href;
 	MetaData metadata;
 	ArrayList<ResourceFile> fileList;
 	ArrayList<Dependency> dependencyList;

	public Resource()
	{
		new Resource("GenericSCO");
	}
 	
 	public Resource(String identifier)
 	{
 		id = identifier;
 		type = LILA_TYPE;
 		
 		fileList = new ArrayList<ResourceFile>();
 		dependencyList = new ArrayList<Dependency>();
 	}
 	
 	public String getIdentifier()
 	{
 		return id;
 	}
 	
 	public void setIdentifier(String identifier)
 	{
 		id = identifier;
 	}
 	
 	public String getType()
 	{
 		return type;
 	}
 	
 	public void setType(String type)
 	{
 		this.type = type;
 	}
 	
 	public String getScormType()
 	{
 		return scormtype;
 	}
 	
 	public void setScormType(String assetType)
 	{	
 		if(assetType.equals(SCORMTYPE_SCO) || assetType.equals(SCORMTYPE_ASSET))
 			scormtype = assetType; 
 		else //Set it to most generic type - asset. Cannot be underfined
 			scormtype = SCORMTYPE_ASSET;
 	}
 	
 	public String getHRef()
 	{
 		return href;
 	}
 	
 	public void setHRef(String href)
 	{
 		if(href.length() <= ResourceFile.STRING_HREF_MAX_LENGTH)
 			this.href = href;
 	}
 	
 	public MetaData getMetaData()
 	{
 		return metadata;
 	}
 	
 	public void setMetaData(MetaData metadata)
 	{
 		this.metadata = metadata;
 	}
 	
 	public ResourceFile getPath(int index)
 	{
 		return fileList.get(index);
 	}
 	
 	public void addPath(ResourceFile file)
 	{
 		fileList.add(file);
 	}
 	
 	public Dependency getDepedency(int index)
 	{
 		return dependencyList.get(index);
 	}
 	
 	public void addDependency(Dependency dependency)
 	{
 		dependencyList.add(dependency);
 	}
 }
