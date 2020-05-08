 package edina.eframework.gefcdemo.controllers;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.commons.configuration.Configuration;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import edina.eframework.gefcdemo.domain.ConfigParameters;
 import edina.eframework.gefcdemo.domain.SoilErosionWps;
 
 @Controller
 @RequestMapping(value="/gefcdemo")
 public class MapController {
   
   private Configuration config;
   
   public void setConfig( Configuration config ) {
     this.config = config;
   }
   
   @RequestMapping(method=RequestMethod.GET)
   public String handleMap( SoilErosionWps params,
                            @ModelAttribute ConfigParameters configParameters )
     throws MalformedURLException {
     
     params.setRainfallExponent( config.getDouble( "rainfallExponent.default" ) );
     params.setGrowFactor( config.getDouble( "growFactor.default" ) );
     params.setStreamConnectivity( config.getDouble( "streamConnectivity.default" ) );
     params.setRainfallWcs( new URL( config.getString( "rainfallWcs.default" ) ) );
     params.setLandcoverWcs( new URL( config.getString( "landcoverWcs.default" ) ) );
     params.setErosionWcs( new URL( config.getString( "erosionWcs.default" ) ) );
     
     configParameters.setRainfallUrl( config.getString( "rainfall.url" ) );
     configParameters.setLandCoverUrl( config.getString( "landcover.url" ) );
     configParameters.setErosionUrl( config.getString( "erosion.url" ) );
     configParameters.setLandCoverPreviewUrl( config.getString( "result.url" ) + "map=" +
                                              config.getString( "wps.output.location" ) +
                                              "aseales/landcoverPreview.map&" );
     configParameters.setResultUrl( config.getString( "result.url" ) + "map=" +
                                    config.getString( "wps.output.location" ) +
                                    "aseales/result.map&" );
     configParameters.setPlacesUrl( config.getString( "places.url" ) );
     
     return "gefcdemo";
   }
 }
