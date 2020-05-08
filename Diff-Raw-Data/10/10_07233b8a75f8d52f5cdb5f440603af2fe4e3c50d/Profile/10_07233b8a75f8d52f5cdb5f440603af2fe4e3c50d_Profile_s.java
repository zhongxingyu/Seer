 package org.lisak.pguide.model;
 
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Index;
 import geo.gps.Coordinates;
 
 import java.text.DecimalFormat;
 import java.util.Formatter;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lisak
  * Date: 16.08.13
  * Time: 17:55
  */
 @EntitySubclass(index = true)
 public class Profile extends Content {
     private String name;
     private String url;
     private Boolean urlCzechOnly;
     private String gpsCoords;
     private List<DayOpeningHours> openingHours;
     private String address;
     private String perex;
     private String text;
     @Index private String category;
     private String prices;
     private ProfileAttributes attributes;
     private String profileImg;
     private List<String> gallery;
     private String staticMapImg;
 
     public Profile() {
         attributes = new ProfileAttributes();
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public void setCategory(String category) {
         this.category = category;
     }
 
     public String getCategory() {
         return this.category;
     }
 
     public String getGpsCoords() {
         return gpsCoords;
     }
 
     public String getFormattedGpsCoords() {
         Coordinates coords = new Coordinates();
         coords.parse(gpsCoords);
 
         long degreesLat = Math.round(coords.getLatitude());
         long degreesLon = Math.round(coords.getLongitude());
 
         double minLat = (coords.getLatitude() - degreesLat)*60;
         double minLon = (coords.getLongitude() - degreesLon)*60;
 
         DecimalFormat df = new DecimalFormat("#.000");
 
         return "N " + degreesLat + "° " + df.format(minLat) + "', " + "E " + degreesLon + "° " + df.format(minLon) + "'";
     }
 
     public void setGpsCoords(String strCoords) {
         //parse and normalize gps coords to decimal form
         //it is not possible to store Coordinates directly with Objectify
         Coordinates coords = new Coordinates();
         //remove NE completely - it fucks with the default format
         String _buf =  strCoords.replaceAll("[NE]", "");
         //replace all chars except for [0-9,.] with spaces
         _buf = _buf.replaceAll("[^\\d,.]", " ");
 
         coords.parse(_buf);
 
         StringBuilder sb = new StringBuilder();
         Formatter fmt = new Formatter(sb);
         DecimalFormat df = new DecimalFormat("#.00000");
 
         fmt.format("%1$s %2$s", df.format(coords.getLatitude()), df.format(coords.getLongitude()));
         this.gpsCoords = fmt.toString();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<DayOpeningHours> getOpeningHours() {
         return openingHours;
     }
 
     public void setOpeningHours(List<DayOpeningHours> openingHours) {
         this.openingHours = openingHours;
     }
 
     public String getPerex() {
         return perex;
     }
 
     public void setPerex(String perex) {
         this.perex = perex;
     }
 
     public ProfileAttributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(ProfileAttributes attributes) {
         this.attributes = attributes;
     }
 
     public String getText() {
         return text;
     }
 
     public String getFormattedText() {
         //returns formatted text, ie. paragraphs enclosed in <div>
         String adjusted = text.replaceAll("(?m)^[ \t]*\r?\n", "</div>\n<div>");
 
         return "<div>" + adjusted + "</div>";
     }
 
     public void setText(String text) {
         this.text = text;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getPrices() {
         return prices;
     }
 
     public String getFormatedPrices() {
         //returns formatted text (\n replaced by <br/>)
         return prices.replaceAll("(?m)\r?\n", "<br/>\n");
     }
 
     public void setPrices(String prices) {
         this.prices = prices;
     }
 
     public List<String> getGallery() {
         return gallery;
     }
 
     public void setGallery(List<String> gallery) {
         this.gallery = gallery;
     }
 
     public String getProfileImg() {
         return profileImg;
     }
 
     public void setProfileImg(String profileImg) {
         this.profileImg = profileImg;
     }
 
     public String getStaticMapImg() {
         return staticMapImg;
     }
 
     public void setStaticMapImg(String staticMapImg) {
         this.staticMapImg = staticMapImg;
     }
 
     public Boolean getUrlCzechOnly() {
         return urlCzechOnly;
     }
 
     public void setUrlCzechOnly(Boolean urlCzechOnly) {
         this.urlCzechOnly = urlCzechOnly;
     }
 }
