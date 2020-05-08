 package edina.eframework.gefcdemo.domain;
 
 /**
  * This class simply retrieves properties from the Apache Commons Configurations
  * and makes these properties available to JSP pages. JSTL isn't really up to
  * this as you can't use do, eg, functions directly on the config object. This
  * class also does any required manipulations to properties, eg, combining them
  * with other properties.
  * 
  * @author Andrew Seales
  */
 public class ConfigParameters {
   private String rainfallUrl;
   private String landCoverUrl;
   private String landCoverPreviewUrl;
   private String erosionUrl;
   private String resultUrl;
   private String placesUrl;
   
   public String getRainfallUrl() {
     return rainfallUrl;
   }
   
   public void setRainfallUrl( String rainfallUrl ) {
     this.rainfallUrl = rainfallUrl;
   }
   
   public String getLandCoverUrl() {
     return landCoverUrl;
   }
   
   public void setLandCoverUrl( String landCoverUrl ) {
     this.landCoverUrl = landCoverUrl;
   }
   
   public String getLandCoverPreviewUrl() {
     return landCoverPreviewUrl;
   }
   
   public void setLandCoverPreviewUrl( String landCoverPreviewUrl ) {
     this.landCoverPreviewUrl = landCoverPreviewUrl;
   }
   
   public String getErosionUrl() {
     return erosionUrl;
   }
   
   public void setErosionUrl( String erosionUrl ) {
     this.erosionUrl = erosionUrl;
   }
   
   public String getResultUrl() {
     return resultUrl;
   }
   
   public void setResultUrl( String resultUrl ) {
     this.resultUrl = resultUrl;
   }
 
   public String getPlacesUrl() {
     return placesUrl;
   }
 
   public void setPlacesUrl( String placesUrl ) {
     this.placesUrl = placesUrl;
   }
 }
