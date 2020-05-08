 package org.geworkbench.util.network;
 
public class CellularNetworkPreference implements java.io.Serializable {
	private static final long serialVersionUID = -3049737574513104023L;
 	private String title = null;
 	private String context = null;
 	private String version = null;
 	
 	public CellularNetworkPreference(String title)
 	{
 		this.title = title;
 	}
 	
 	public CellularNetworkPreference(String title, String context, String version)
 	{
 		this.title = title;
 		this.context = context;
 		this.version = version;
 	}
 	
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 	
 	public String getTitle()
 	{
 		return this.title;
 	}
 	
 	public void setContext(String context)
 	{
 		this.context = context;
 	}
 	
 	public String getContext()
 	{
 		return this.context;
 	}
 	
 	public void setVersion(String version)
 	{
 		this.version = version;
 	}
 	
 	public String getVersion()
 	{
 		return this.version;
 	}
 
 	
 }
