 package org.apache.hadoop.chukwa.datastore;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import org.apache.hadoop.chukwa.conf.ChukwaConfiguration;
 import org.apache.hadoop.chukwa.hicc.HiccWebServer;
 import org.apache.hadoop.chukwa.rest.bean.UserBean;
 import org.apache.hadoop.chukwa.util.ExceptionUtil;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 public class UserStore {
   private String uid = null;
   private UserBean profile = null;
   private static Log log = LogFactory.getLog(UserStore.class);
   private static Configuration config = new Configuration();
   private static ChukwaConfiguration chukwaConf = new ChukwaConfiguration();
   private static String hiccPath = config.get("fs.default.name")+File.separator+chukwaConf.get("chukwa.data.dir")+File.separator+"hicc"+File.separator+"users";
   
   public UserStore() throws IllegalAccessException {
     UserStore.config = HiccWebServer.getConfig();
   }
 
   public UserStore(String uid) throws IllegalAccessException {
     this.uid = uid;
     init(uid);
   }
 
   public void init(String uid) throws IllegalAccessException {
     StringBuilder profilePath = new StringBuilder();
     profilePath.append(hiccPath);
     profilePath.append(File.separator);
     profilePath.append(uid);
     profilePath.append(".profile");
     Path profileFile = new Path(profilePath.toString());
     FileSystem fs;
     try {
       fs = FileSystem.get(config);
       if(fs.exists(profileFile)) {
         FileStatus[] fstatus = fs.listStatus(profileFile);
         long size = fstatus[0].getLen();
         FSDataInputStream viewStream = fs.open(profileFile);
         byte[] buffer = new byte[(int)size];
         viewStream.readFully(buffer);
         viewStream.close();
         try {
           JSONObject json = new JSONObject(new String(buffer));
           profile = new UserBean(json);
         } catch (Exception e) {
           log.error(ExceptionUtil.getStackTrace(e));
           throw new IllegalAccessException("Unable to access user profile database.");
         }
       } else {
         profile = new UserBean();
         profile.setId(uid);
         JSONArray ja = new JSONArray();
         profile.setViews(ja);
         JSONObject json = new JSONObject();
        profile.setProperties(json.toString());
       }
     } catch (IOException ex) {
       log.error(ExceptionUtil.getStackTrace(ex));
     }
   }
   
   public UserBean get() throws IllegalAccessException {
     if(profile==null) {
       init(uid);
     }
     return profile;
   }
   
   public void set(UserBean profile) throws IllegalAccessException {
     StringBuilder profilePath = new StringBuilder();
     profilePath.append(hiccPath);
     profilePath.append(File.separator);
     profilePath.append(profile.getId());
     profilePath.append(".profile");
     Path profileFile = new Path(profilePath.toString());
     FileSystem fs;
     try {
       fs = FileSystem.get(config);
       FSDataOutputStream out = fs.create(profileFile,true);
       out.write(profile.deserialize().toString().getBytes());
       out.close();
     } catch (IOException ex) {
       log.error(ExceptionUtil.getStackTrace(ex));
       throw new IllegalAccessException("Unable to access user profile database.");
     }
     this.profile = profile;
   }
   
   public static JSONArray list() throws IllegalAccessException {
     StringBuilder profilePath = new StringBuilder();
     profilePath.append(hiccPath);
     profilePath.append(File.separator);
     profilePath.append("*.profile");
     Path viewFile = new Path(profilePath.toString());
     FileSystem fs;
     JSONArray list = new JSONArray();
     try {
       fs = FileSystem.get(config);
       FileStatus[] fstatus = fs.listStatus(viewFile);
       if(fstatus!=null) {
         for(int i=0;i<fstatus.length;i++) {
           long size = fstatus[i].getLen();
           FSDataInputStream profileStream = fs.open(fstatus[i].getPath());
           byte[] buffer = new byte[(int)size];
           profileStream.readFully(buffer);
           profileStream.close();
           try {
             UserBean user = new UserBean(new JSONObject(new String(buffer)));
             list.put(user.getId());
           } catch (Exception e) {
             log.error(ExceptionUtil.getStackTrace(e));
           }
         }
       }
     } catch (IOException ex) {
       log.error(ExceptionUtil.getStackTrace(ex));
       throw new IllegalAccessException("Unable to access user profile database."); 
     }
     return list;    
   }
 }
            
