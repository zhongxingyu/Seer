 package ai.ilikeplaces.entities;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.util.EntityLifeCycleListener;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
  * Okay, we need to
  * <p/>
  * 1. Remember that entries to this entity will be foreign keys. That is, so that anybody can use this entity.
  * <p/>
  * 2. This entity should be super efficient since it will be used everywhere.
  * <p/>
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jan 25, 2010
  * Time: 1:01:22 PM
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Entity
 public class Mute extends HumanEquals implements Serializable {
 
     public Long muteId;
     public static final String muteIdCOL = "muteId";
 
     public Integer muteType;//Wall, Album
 
     public String muteContent;
 
     public String muteMetadata;//Anybody can store relevant metadata here
 
 
     final static public int muteTypeHUMAN = 1;
     final static public int muteTypeMISC = 0;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getMuteId() {
         return muteId;
     }
 
     public void setMuteId(final Long muteId) {
         this.muteId = muteId;
     }
 
     public Mute setMuteIdR(final Long muteId) {
         this.muteId = muteId;
         return this;
 
     }
 
     public Integer getMuteType() {
         return muteType;
     }
 
     public void setMuteType(final Integer muteType) {
         this.muteType = muteType;
     }
 
     public Mute setMuteTypeR(final Integer muteType) {
         this.muteType = muteType;
         return this;
     }
 
     public String getMuteContent() {
         return muteContent;
     }
 
     public void setMuteContent(final String muteContent) {
         this.muteContent = muteContent;
     }
 
     public Mute setMuteContentR(final String muteContent) {
         this.muteContent = muteContent;
         return this;
     }
 
     public String getMuteMetadata() {
         return muteMetadata;
     }
 
     public void setMuteMetadata(final String muteMetadata) {
         this.muteMetadata = muteMetadata;
     }
 
     public Mute setMuteMetadataR(final String muteMetadata) {
         this.muteMetadata = muteMetadata;
         return this;
 
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
 
         if (o == null) {
             return false;
         }
 
         if (getClass() == o.getClass()) {
            final Mute that = (Mute) o;
             return (!(this.getHumanId() == null || that.getHumanId() == null)) && this.getHumanId().equals(that.getHumanId());
         } else {
             return matchHumanId(o);
         }
     }
 
     /**
      *
      * @return getMuteContent() being optimistic that it contains the humanId
      */
     @Override
     public String getHumanId() {
         return getMuteContent();
     }
 }
