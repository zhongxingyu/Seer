 package controllers;
 
 import com.shopservice.domain.ClientSettings;
 import com.shopservice.domain.Site;
 import com.shopservice.domain.Site2Product;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class SiteController extends Controller {
 
     public static Result getSites(String clientId)
     {
         return ok( Json.toJson( Site.get(clientId) ) );
     }
 
     public static Result addSite( String clientId )
     {
         ClientSettings settings = ClientSettings.findById(clientId);
         if (settings == null)
             return badRequest("Client with id "+clientId+" doesn't exist ");
 
         Site site = Json.fromJson( request().body().asJson(), Site.class );
 
         if (site.name.isEmpty() )
             return badRequest("Name cannot be empty");
 
         if (Site.exist(clientId, site.name))
             return badRequest("Category with specified name already exists");
 
         settings.sites.add(site);
 
         settings.save();
 
         return ok( Json.toJson(site) );
     }
 
    public static Result removeSite(String clientId, String siteId)
     {
        Site.remove(siteId);
 
         return ok();
     }
 
     public static Result updateSite(String clientId, Long basicSiteId, Long resourceSiteId, String operationName )
     {
         Operation operation = Operation.valueOf(operationName);
         switch (operation)
         {
             case merge:
                 Site2Product.merge(basicSiteId.intValue(), resourceSiteId.intValue());
             case difference:
                 Site2Product.difference(basicSiteId.intValue(), resourceSiteId.intValue());
         }
 
         return ok();
     }
 
     private static enum Operation
     {
         merge, difference
     }
 }
