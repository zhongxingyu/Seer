 package net.slimeslurp.tcgrowl;
 
 import javax.servlet.http.HttpServletRequest;
 
 import jetbrains.buildServer.web.openapi.SimplePageExtension;
 import jetbrains.buildServer.web.openapi.PagePlaces;
 import jetbrains.buildServer.web.openapi.PlaceId;
 
 public class TCGrowlSettingsExtension extends SimplePageExtension {
     
     public TCGrowlSettingsExtension(PagePlaces pagePlaces) {    
        super(pagePlaces);
        
        System.out.println("### in constructor..." + pagePlaces);
        
        //pagePlaces.getPlaceById(PlaceId.MY_SETTINGS_NOTIFIER_SECTION).addExtension(this);
         setIncludeUrl("tcgrowlSettings.jsp");
         setPlaceId(PlaceId.NOTIFIER_SETTINGS_FRAGMENT);
         setPluginName("tcgrowl");
         register();
     }
     
     public boolean isAvailable(HttpServletRequest request) {
         return super.isAvailable(request);
        //System.out.println("### isAvailable..." + request.toString());
        //return true;
    }
    
 }
