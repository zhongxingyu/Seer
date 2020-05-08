 package entities;
 
 import java.sql.Timestamp;
 
 /**
  * The IP Entity Class
  *
  * @author alexhughes
  */
 public class Ip extends Entity implements Comparable {
 
     private String ip;
     private String agent;
     private String domain;
     private int processed = NIL;
     private int hits = NIL;
     private int request;
     private String requestSource;
     private String hostname;
     private String city;
     private String region;
     private String country;
     private String countryCode;
     private double latitude;
     private double longitude;
     private String postCode;
     private String timezone;
     private Timestamp dateCreated;
     private Timestamp dateModified;
 
     public Ip(String ip, String agent, String domain, int processed, int hits, int request, String requestSource, String hostname, String city, String region, String country, String countryCode, double latitude, double longitude, String postCode, String timezone, Timestamp dateCreated, Timestamp dateModified) {
         this.ip = ip;
         this.agent = agent;
         this.domain = domain;
         this.hits = hits;
         this.request = request;
         this.requestSource = requestSource;
         this.processed = processed;
         this.hostname = hostname;
         this.city = city;
         this.region = region;
         this.country = country;
         this.countryCode = countryCode;
         this.latitude = latitude;
         this.longitude = longitude;
         this.postCode = postCode;
         this.timezone = timezone;
         this.dateCreated = dateCreated;
         this.dateModified = dateModified;
     }
 
     public Ip() {
     }
 
     public String getIp() {
         return ip;
     }
 
     public void setIp(String ip) {
         this.ip = ip;
     }
 
     public String getAgent() {
         return agent;
     }
 
     public void setAgent(String agent) {
         this.agent = agent;
     }
 
     public String getDomain() {
         return domain;
     }
 
     public void setDomain(String domain) {
         this.domain = domain;
     }
 
     public int getHits() {
         return hits;
     }
 
     public void setHits(int hits) {
         this.hits = hits;
     }
 
     public int getRequest() {
         return request;
     }
 
     public void setRequest(int request) {
         this.request = request;
     }
 
     public String getRequestSource() {
         return requestSource;
     }
 
     public void setRequestSource(String requestSource) {
         this.requestSource = requestSource;
     }
 
     public int getProcessed() {
         return processed;
     }
 
     public void setProcessed(int processed) {
         this.processed = processed;
     }
 
     public String getHostname() {
         return hostname;
     }
 
     public void setHostname(String hostname) {
         this.hostname = hostname;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getRegion() {
         return region;
     }
 
     public void setRegion(String region) {
         this.region = region;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public String getCountryCode() {
         return countryCode;
     }
 
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     public String getPostCode() {
         return postCode;
     }
 
     public void setPostCode(String postCode) {
         this.postCode = postCode;
     }
 
     public String getTimezone() {
         return timezone;
     }
 
     public void setTimezone(String timezone) {
         this.timezone = timezone;
     }
 
     public Timestamp getDateCreated() {
         return dateCreated;
     }
 
     public void setDateCreated(Timestamp dateCreated) {
         this.dateCreated = dateCreated;
     }
 
     public Timestamp getDateModified() {
         return dateModified;
     }
 
     public void setDateModified(Timestamp dateModified) {
         this.dateModified = dateModified;
     }
 
     @Override
     public int compareTo(Object o) {
         Ip ipE = (Ip) o;
 
         int result;
         result = ipE.getIp().compareToIgnoreCase(this.getIp());
        result += ipE.getDomain().compareToIgnoreCase(this.getDomain());
        result += ipE.getAgent().compareToIgnoreCase(this.getAgent());
 
         return result;
     }
 }
