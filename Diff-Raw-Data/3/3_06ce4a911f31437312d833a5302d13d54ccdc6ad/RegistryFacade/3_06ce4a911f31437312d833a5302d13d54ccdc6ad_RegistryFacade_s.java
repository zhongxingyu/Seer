 package org.vamdc.portal.registry;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.vamdc.registry.client.Registry.Service;
 import org.vamdc.registry.client.RegistryCommunicationException;
 
 @Scope(ScopeType.APPLICATION)
 @Name("registry")
 public class RegistryFacade
 {
     public int getCount(){
     	if (Client.INSTANCE.get()!=null)
     		try {
     			return Client.INSTANCE.get().getIVOAIDs(Service.VAMDC_TAP).size();
     		} catch (RegistryCommunicationException e) {
     			return -1;
     		}
     	return -1;
     }
     
     public String getObject(){
     	if (Client.INSTANCE.get()!=null)
     		return Client.INSTANCE.get().toString();
     	return "null";
     }
     
   	public RegistryFacade(){
   		if (Client.INSTANCE.get()!=null)
   			System.out.println(Client.INSTANCE.get());
 		
   	}
     
 }
