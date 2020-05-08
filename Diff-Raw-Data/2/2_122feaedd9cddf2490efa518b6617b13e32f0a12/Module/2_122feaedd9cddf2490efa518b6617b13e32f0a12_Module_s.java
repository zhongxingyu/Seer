 package org.hazelwire.modules;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.hazelwire.main.FileName;
 import org.hazelwire.main.Generator;
 
 /**
  * 
  * @author Tim Strijdhorst
  * This class represents a module in the system and is 'linked' with the actual module on the filesystem
  */
 public class Module
 {
 	private String name, author, deployPath;
 	private HashMap<Integer,Flag> flags; //<id,amountOfPoints>
 	private HashMap<Integer,Option> options; //<id,Option>
 	private ArrayList<String> tags; 
 	private Date date;
 	private ModulePackage modulePackage;
 	private int id;
 	private FileName filePath;
 	
 	public Module(String name, String filePath)
 	{
 		this.name = name;
 		this.filePath = new FileName(filePath,Generator.getInstance().getFileSeperator(),'.');
 		this.flags = new HashMap<Integer,Flag>();
 		this.options = new HashMap<Integer,Option>();
 		this.tags = new ArrayList<String>();
 	}
 	
 	public Module()
 	{
 		this.flags = new HashMap<Integer,Flag>();
 		this.options = new HashMap<Integer,Option>();
 		this.tags = new ArrayList<String>();
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	public ArrayList<String> getTags()
 	{
 		return this.tags;
 	}
 	
 	public void addTag(String tag)
 	{
 		this.tags.add(tag);
 	}
 	
 	public void removeTag(String tag)
 	{
 		this.tags.remove(tag);
 	}
 
 	public String getFilePath()
 	{
 		return filePath.getPath();
 	}
 	
 	public String getFileName()
 	{
 		return filePath.getFileName()+filePath.getExtensionSeparator()+filePath.getExtension();
 	}
 	
 	public String getFileNameWithoutExtension()
 	{
 		return filePath.getFileName();
 	}
 	
 	public String getFullPath()
 	{
 		return filePath.getFullPath();
 	}
 
 	public void setFilePath(String filePath)
 	{
 		this.filePath = new FileName(filePath,Generator.getInstance().getFileSeperator(),'.');
 	}
 	
 	public void setFileName(String fileName)
 	{
 		if(this.filePath != null) filePath = new FileName(filePath.getPath()+fileName,Generator.getInstance().getFileSeperator(),'.');
 	}
 	
 	public void addFlag(Flag flag)
 	{
 		flag.setId(this.flags.size());
		this.flags.put(id, flag);
 	}
 	
 	public void removeFlag(int id)
 	{
 		this.flags.remove(id);
 	}
 	
 	public void addOption(Option option)
 	{
 		option.setId(this.options.size());
 		this.options.put(option.getId(),option);
 	}
 	
 	public void removeOption(int id)
 	{
 		this.options.remove(id);
 	}
 
 	public String getAuthor()
 	{
 		return author;
 	}
 
 	public void setAuthor(String author)
 	{
 		this.author = author;
 	}
 
 	public Date getDate()
 	{
 		return date;
 	}
 
 	public void setDate(Date date)
 	{
 		this.date = date;
 	}
 
 	public void setModulePackage(ModulePackage modulePackage)
 	{
 		this.modulePackage = modulePackage;
 	}
 
 	public ModulePackage getModulePackage()
 	{
 		return modulePackage;
 	}
 	
 	public String toString()
 	{
 		return "id: "+String.valueOf(id)+" name: "+name+" flags: "+String.valueOf(flags.size())+" options: "+String.valueOf(options.size());
 	}
 
 	public int getId()
 	{
 		return id;
 	}
 
 	public void setId(int id)
 	{
 		this.id = id;
 	}
 
 	public String getDeployPath()
 	{
 		return deployPath;
 	}
 
 	public void setDeployPath(String deployFileName)
 	{
 		this.deployPath = deployFileName;
 	}
 	
 	/**
 	 * Completely replace the optionlist
 	 * @param options
 	 */
 	public void setOptions(ArrayList<Option> options)
 	{
 		this.options.clear(); //just to be sure
 		
 		for(Option option : options)
 		{
 			this.options.put(option.getId(), option); //Add them with the original IDs
 		}
 	}
 
 	public Collection<Flag> getFlags()
 	{
 		return flags.values();
 	}
 
 	public Collection<Option> getOptions()
 	{
 		return options.values();
 	}
 }
