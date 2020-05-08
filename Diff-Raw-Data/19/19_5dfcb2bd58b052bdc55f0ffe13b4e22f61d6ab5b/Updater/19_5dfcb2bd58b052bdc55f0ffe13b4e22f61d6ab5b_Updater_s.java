 package com.weibogrep.user;
 
 import java.io.*;
 import com.weibogrep.util.*;
 
 public class Updater {
 
     public static void main(String []args) {
         updateAll();
     }
 
     public static void updateAll() {
         File baseDir = new File(UserMgmt.BASE_DIR);
        String[] users = baseDir.list();
        for (int i = 0; i < users.length; i++) {
            long uid = Long.parseLong(users[i]);
            UserMgmt um = new UserMgmt(uid);
            ZLog.info("updating " + uid + " , last post id is " + um.getLastPost());
            um.update();
         }
     }
 }
 
