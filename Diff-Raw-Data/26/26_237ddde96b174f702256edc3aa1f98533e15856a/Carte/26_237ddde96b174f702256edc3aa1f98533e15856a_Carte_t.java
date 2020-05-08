 package models;
 
import play.data.validation.Required;

 public class Carte {
 
     public String              uuid;
     public final static String UUID            = "UUID";
 
    @Required
     public String              name;
     public final static String NAME            = "NAME";
 
     public String              description;
     public final static String DESCRIPTION     = "DESCRIPTION";
 
    @Required
     public Float               centerLng;
     public final static String CENTER_LNG      = "CENTER_LONGITUDE";
 
    @Required
     public Float               centerLat;
     public final static String CENTER_LAT      = "CENTER_LATITUDE";
 
    @Required
     public Integer             centerZoom;
     public final static String CENTER_ZOOM     = "CENTER_ZOOM";
 
    @Required
     public Integer             leafPileZoom;
     public final static String LEAFT_PILE_ZOOM = "LEAFT_PILE_ZOOM";
 
     public String              user_uuid;
     public final static String USER_UUID       = "USER_UUID";
 
     public String              pattern;
     public final static String PATTERN         = "PATTERN";
 
 }
