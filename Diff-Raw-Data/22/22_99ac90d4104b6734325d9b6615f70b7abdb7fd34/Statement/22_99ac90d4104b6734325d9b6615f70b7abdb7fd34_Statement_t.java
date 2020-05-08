 package org.swordapp.server;
 
 import java.io.IOException;
 import java.io.Writer;
import java.util.ArrayList;
 import java.util.Date;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class Statement
 {
    // common elements with some default values
    protected String contentType = null;
    protected List<OriginalDeposit> originalDeposits = new ArrayList<OriginalDeposit>();
    protected Map<String, String> states = new HashMap<String, String>();
    protected List<ResourcePart> resources = new ArrayList<ResourcePart>();
	protected Date lastModified = new Date();
 
     public abstract void writeTo(Writer out) throws IOException;
 
     public String getContentType()
     {
         return contentType;
     }
 
     public void setOriginalDeposits(List<OriginalDeposit> originalDeposits)
     {
         this.originalDeposits = originalDeposits;
     }
 
     public void addOriginalDeposit(OriginalDeposit originalDeposit)
     {
         this.originalDeposits.add(originalDeposit);
     }
 
     public void setResources(List<ResourcePart> resources)
     {
         this.resources = resources;
     }
 
     public void addResource(ResourcePart resource)
     {
         this.resources.add(resource);
     }
 
     public void setStates(Map<String, String> states)
     {
         this.states = states;
     }
 
     public void setState(String state, String description)
     {
         this.states.clear();
         this.states.put(state, description);
     }
 
     public void addState(String state, String description)
     {
         this.states.put(state, description);
     }
 
 	public Date getLastModified()
 	{
 		return lastModified;
 	}
 
 	public void setLastModified(Date lastModified)
 	{
 		this.lastModified = lastModified;
 	}
 }
