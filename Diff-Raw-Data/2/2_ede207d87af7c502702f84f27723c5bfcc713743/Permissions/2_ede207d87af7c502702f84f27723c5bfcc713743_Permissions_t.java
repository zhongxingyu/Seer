 package fm.audiobox.core.models;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IEntity;
 
 public class Permissions  extends AbstractEntity implements Serializable {
 
  private static final long serialVersionUID = 3196523340942887761L;

   public static final String NAMESPACE = "permissions";
   public static final String TAGNAME = NAMESPACE;
   
   
   private boolean cloud;
   private boolean local;
   private boolean dropbox;
   private boolean soundcloud;
   private boolean youtube;
   
   
   public Permissions(IConfiguration config) {
     super(config);
   }
 
   @Override
   public String getNamespace() {
     return NAMESPACE;
   }
 
   @Override
   public String getTagName() {
     return TAGNAME;
   }
 
   
   public boolean isCloud() {
     return cloud;
   }
 
   public void setCloud(boolean cloud) {
     this.cloud = cloud;
   }
 
   public boolean isLocal() {
     return local;
   }
 
   public void setLocal(boolean local) {
     this.local = local;
   }
 
   public boolean isDropbox() {
     return dropbox;
   }
 
   public void setDropbox(boolean dropbox) {
     this.dropbox = dropbox;
   }
 
   public boolean isSoundcloud() {
     return soundcloud;
   }
 
   public void setSoundcloud(boolean soundcloud) {
     this.soundcloud = soundcloud;
   }
 
   public boolean isYoutube() {
     return youtube;
   }
 
   public void setYoutube(boolean youtube) {
     this.youtube = youtube;
   }
   
   
   
   
   @Override
   public Method getSetterMethod(String tagName) throws SecurityException, NoSuchMethodException {
     
     if ( tagName.equals("cloud") ){
       return this.getClass().getMethod("setCloud", boolean.class);
 
     } else if ( tagName.equals("local") ){
       return this.getClass().getMethod("setLocal", boolean.class);
 
     } else if ( tagName.equals("dropbox") ){
       return this.getClass().getMethod("setDropbox", boolean.class);
 
     } else if ( tagName.equals("soundcloud") ){
       return this.getClass().getMethod("setSoundcloud", boolean.class);
 
     } else if ( tagName.equals("youtube") ){
       return this.getClass().getMethod("setYoutube", boolean.class);
 
     } 
     
     return null;
   }
 
   @Override
   public String getApiPath() {
     return null;
   }
 
   @Override
   public void setParent(IEntity parent) { }
 
   @Override
   protected void copy(IEntity entity) { }
   
   
   
   
 }
