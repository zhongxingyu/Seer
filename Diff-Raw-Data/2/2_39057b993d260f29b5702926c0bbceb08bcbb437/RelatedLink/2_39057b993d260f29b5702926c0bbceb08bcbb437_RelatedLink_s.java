 package org.atlasapi.media.entity;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.common.base.Objects;
 
 public class RelatedLink {
     
     public static Builder facebookLink(String url) {
         return relatedLink(LinkType.FACEBOOK, url);
     }
     
     public static Builder twitterLink(String url) {
         return relatedLink(LinkType.TWITTER, url);
     }
     
     public static Builder flickLink(String url) {
         return relatedLink(LinkType.FLICKR, url);
     }
     
     public static Builder unknownTypeLink(String url) {
         return relatedLink(LinkType.UNKNOWN, url);
     }
     
     public static Builder relatedLink(LinkType type, String url) {
         return new Builder(checkNotNull(type), checkNotNull(url));
     }
     
     public static Builder linkBasedOn(RelatedLink link) {
         return relatedLink(link.getType(), link.getUrl())
                 .withSourceId(link.getSourceId())
                 .withShortName(link.getShortName())
                 .withTitle(link.getTitle())
                 .withDescription(link.getDescription())
                 .withImage(link.getImage())
                 .withThumbnail(link.getThumbnail());
     }
 
     public static class Builder {
         
         private final LinkType type;
         private final String url;
         
         private String sourceId;
         private String shortName;
         private String title;
         private String description;
         private String image;
         private String thumbnail;
 
         public Builder(LinkType type, String url) {
             this.type = type;
             this.url = url;
         }
 
         public Builder withSourceId(String sourceId) {
             this.sourceId = sourceId;
             return this;
         }
 
         public Builder withShortName(String shortName) {
             this.shortName = shortName;
             return this;
         }
 
         public Builder withTitle(String title) {
             this.title = title;
             return this;
         }
 
         public Builder withDescription(String description) {
             this.description = description;
             return this;
         }
 
         public Builder withImage(String image) {
             this.image = image;
             return this;
         }
 
         public Builder withThumbnail(String thumbnail) {
             this.thumbnail = thumbnail;
             return this;
         }
         
         public RelatedLink build() {
             RelatedLink rl = new RelatedLink(type, url);
             rl.sourceId = sourceId;
             rl. shortName = shortName;
             rl.title = title;
             rl.description = description;
             rl.image = image;
             rl.thumbnail = thumbnail;
             return rl;
         }
         
     }
     
     public enum LinkType {
         FACEBOOK,
         TWITTER,
         FLICKR,
         UNKNOWN
     }
     
     private final String url;
     private final LinkType type;
     
     private String sourceId;
     private String shortName;
     private String title;
     private String description;
     private String image;
     private String thumbnail;
     
     private RelatedLink(LinkType type, String url) {
         this.type = type;
         this.url = url;
     }
 
     public String getUrl() {
         return this.url;
     }
 
     public LinkType getType() {
         return this.type;
     }
     
     public String getSourceId() {
         return this.sourceId;
     }
 
     public String getShortName() {
         return this.shortName;
     }
 
     public String getTitle() {
         return this.title;
     }
 
     public String getDescription() {
         return this.description;
     }
 
     public String getImage() {
         return this.image;
     }
 
     public String getThumbnail() {
         return this.thumbnail;
     }
 
     @Override
     public boolean equals(Object that) {
         if (this == that) {
             return true;
         }
         if (that instanceof RelatedLink) {
             RelatedLink other = (RelatedLink) that;
            return type == other.type && url.equals(url);
         }
         return false;
     }
     
     @Override
     public int hashCode() {
         return Objects.hashCode(type, url);
     }
     
     @Override
     public String toString() {
         return String.format("%s: %s", type, url);
     }
 }
