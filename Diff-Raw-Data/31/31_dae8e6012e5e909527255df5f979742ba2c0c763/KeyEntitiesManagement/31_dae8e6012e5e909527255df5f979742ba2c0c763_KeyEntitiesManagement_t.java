 package org.wennsoft.web.rest;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 import org.exoplatform.container.component.RequestLifeCycle;
 import org.exoplatform.services.organization.OrganizationService;
 import org.exoplatform.services.organization.UserProfile;
 import org.exoplatform.services.organization.UserProfileHandler;
 import org.exoplatform.services.rest.resource.ResourceContainer;
 import org.exoplatform.container.PortalContainer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.core.MediaType;
 
 @Path("/keyEntitiesManagement/")
 public class KeyEntitiesManagement implements ResourceContainer 
 {
 	
     @GET
     @Path("getKeyEntities/{name}")
     public String getKeyEntities(@Context HttpServletRequest request, @PathParam("name") String name) 
     {    
     	HttpSession httpSession = request.getSession();
     	String keyEntities = "";
     	if (httpSession.getAttribute("keyEntities") == null)
     	{	
     	    try
             {
         	    OrganizationService organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
                 UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
                 UserProfile userProfile = userProfileHandler.findUserProfileByName(name);
                 keyEntities = userProfile.getAttribute("keyEntities");
             } 
             catch (Exception e) 
             {
                 e.printStackTrace();
             }
     	}
     	else 
     	{
     		keyEntities = httpSession.getAttribute("keyEntities").toString();	
     	}
     	return keyEntities;
     }
 	
     @POST
     @Path("saveKeyEntities/{name}")
     @Consumes(MediaType.APPLICATION_JSON)
    public void saveKeyEntities(@Context HttpServletRequest request, @PathParam("name") String name, List<KeyEntity> listKeyEntities)
     {    
         try
         {
         	RequestLifeCycle.begin(PortalContainer.getInstance());
             OrganizationService organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
             UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
             UserProfile userProfile = userProfileHandler.findUserProfileByName(name);
             userProfile.setAttribute("keyEntities", getListKeyEntities(listKeyEntities));
             userProfileHandler.saveUserProfile(userProfile, false);
             RequestLifeCycle.end();
         } 
         catch (Exception e) 
         {
             e.printStackTrace();
         }
      HttpSession httpSession = request.getSession();
      httpSession.setAttribute("keyEntities", null);
     }
     
     @POST
    @Path("setKeyEntities")
     @Consumes(MediaType.APPLICATION_JSON)
    public void setKeyEntities(@Context HttpServletRequest request, List<KeyEntity> listKeyEntities)
     {    
       HttpSession httpSession = request.getSession();
      if (listKeyEntities!=null)
      {
          httpSession.setAttribute("keyEntities", getListKeyEntities(listKeyEntities));
      }
      else
      {
          httpSession.setAttribute("keyEntities", null);
      }
     }
     
     private String getListKeyEntities (List<KeyEntity> listKeyEntities)
     {
     	String keyEntities = "";
         for (KeyEntity keyEntity : listKeyEntities)
         {
         	keyEntities += "@" + keyEntity.getConnectId() + "/" + keyEntity.getKey();
         }
         return keyEntities != "" ? keyEntities.substring(1):"";
     }
 }
