 package edina.eframework.gefcdemo.portrayal;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 
 /**
  * Generates a Mapserver .map file which allows the portrayal of the result of
  * the soil erosion algorithm for a specific user.
  * 
  * @author Andrew Seales
  */
 public class MapserverGenerator implements WmsGenerator {
   
   private VelocityEngine velocityEngine;
   
   public void setVelocityEngine( VelocityEngine velocityEngine ) {
     this.velocityEngine = velocityEngine;
   }
   
   private String templateLocation;
   
   public void setTemplateLocation( String templateLocation ) {
     this.templateLocation = templateLocation;
   }
   
   private String growTemplateLocation;
   
   public void setGrowTemplateLocation( String growTemplateLocation ) {
     this.growTemplateLocation = growTemplateLocation;
   }
   
   private String wpsOutputDir;
   
   public void setWpsOutputDir( String wpsOutputDir ) {
     this.wpsOutputDir = wpsOutputDir;
   }
 
   @Override
   public void generateResultConfiguration( String user ) throws IOException { // TODO change this to Spring Security Principal
     generateConfiguration( user, "result.map", templateLocation );
   }
   
   @Override
   public void generateLandcoverConfiguration( String user ) throws IOException {
     generateConfiguration( user, "landcoverPreview.map", growTemplateLocation );
   }
   
   private void generateConfiguration( String user, String mapName, String template )
     throws IOException {
     
     File wmsTemplateFile = new File( wpsOutputDir + user + "/" + mapName );
     wmsTemplateFile.getParentFile().mkdirs();
     
     Writer wpsRequest = null;
     try {
       wpsRequest = new FileWriter( wmsTemplateFile );
       Map<String, Object> velocityMap = new HashMap<String,Object>();
       
       velocityMap.put( "dataFolder", wpsOutputDir );
       velocityMap.put( "user", user );
       
       VelocityEngineUtils.mergeTemplate( velocityEngine, template, velocityMap, wpsRequest );
     }
     finally {
       try { wpsRequest.close(); } catch ( Exception e ) {}
     };
   }
 }
