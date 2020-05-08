 /*******************************************************************************
  * Copyright (c) 2013 MCForge.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.ep.ggs.system.updater;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import com.ep.ggs.server.Server;
 import com.ep.ggs.system.Serializer;
 import com.ep.ggs.system.Serializer.SaveType;
 
 
 public class UpdateManager {
     private ArrayList<Updatable> updatelist = new ArrayList<Updatable>();
     private static final Serializer<UpdateHolder> update = new Serializer<UpdateHolder>(SaveType.JSON, 0L);
 
     /**
      * This method can only be called by the {@link Updateable} object being
      * removed or by the {@link PluginHandler}
      * @param object
      * @throws IllegalAccessException
      */
     public void remove(Updatable object) throws IllegalAccessException {
         StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
         try {
             StackTraceElement e = stacks[2];
             Class<?> class_ = Class.forName(e.getClassName());
             class_.asSubclass(Updatable.class);
             if (object.getClass() == class_) {
                 if (updatelist.contains(object))
                     updatelist.remove(object);
                 return;
             }
         } catch (ClassNotFoundException e1) { }
         catch (ClassCastException e2) { }
         catch (ArrayIndexOutOfBoundsException e3) { }
         
         if (stacks[2].getClassName().equals("com.ep.ggs.API.plugin.PluginHandler")) {
             if (updatelist.contains(object))
                 updatelist.remove(object);
             return;
         }
 
         throw new IllegalAccessException("You can only call this method by the Updatable object being removed or through the PluginHandler");
     }
 
     /**
      * Add an object that can be updated
      * @param object
      *              The updatable object.
      */
     public void add(Updatable object) {
         if (!updatelist.contains(object))
             updatelist.add(object);
     }
 
     /**
      * Get a list of objects that will be updating
      * @return
      *        An {@link ArrayList} of udpatable objects
      */
     public ArrayList<Updatable> getUpdateObjects() {
         return updatelist;
     }
     
     /**
      * Get a list of updates from a URL by reading the json from the URL.
      * @param url
      *           The URL to get the updates from.
      * @return
      *       An array of {@link Update} objects.
      */
     public Update[] getUpdates(String URL) {
         try {
             return getUpdates(new URL(URL));
         } catch (MalformedURLException e) {
             e.printStackTrace();
             return null;
         }
     }
     
     /**
      * Check to see if the {@link Update} object <b>update</b> is a higher
      * version than the {@link Updatable} object <b>u</b>
      * @param u
      *         The {@link Updatable} object to compare
      * @param update
      *             The update to compare
      * @return
      *        Whether the update is a higher version than the {@link Updatable} object
      */
     public boolean isUpdate(Updatable u, Update update) {
        if (higherVersion(u.getCurrentVersion(), update.getVersion()).equals(update.getVersion())) {
            if (higherVersion(update.getCoreVersion(), Server.CORE_VERSION).equals("")) //If both are equal, the method will return "", so its ok to update
                return true;
            else if (higherVersion(update.getCoreVersion(), Server.CORE_VERSION, 0, false).equals(Server.CORE_VERSION)) //If the major is higher in the server than the update, then its not safe to update most likely.
                return false;
            else if (higherVersion(update.getCoreVersion(), Server.CORE_VERSION, 1, false).equals(Server.CORE_VERSION)) //If the minor is higher in the server than the update, then its not safe to update most likely.
                return false;
            else
                return true; //I guess its safe to update?
        }
        return false;
     }
     
     /**
      * Get the highest version this {@link Updatable} object has to offer. </br>
      * If the {@link Updatable} object is already at its latest, then the same version will be return. If no {@link Update} representation is available for the same update, then null will be returned.
      * @param object
      *              The {@link Updatable} object.
      * @return
      *        Returns the latest update
      */
     public Update getHighestUpdate(Updatable object) {
         Update[] updates = getUpdates(object.getInfoURL());
         Update highest = null;
         for (Update update : updates) {
             if (highest == null)
                 highest = update;
             else if (higherVersion(update.getVersion(), highest.getVersion()).equals(update.getVersion()))
                 highest = update;
         }
         if (highest == null)
             return null;
         else if (higherVersion(object.getCurrentVersion(), highest.getVersion()).equals(""))
             return highest;
         else if (higherVersion(object.getCurrentVersion(), highest.getVersion()).equals(object.getCurrentVersion()))
             return null;
         else
             return highest;
     }
     
     /**
      * Get a list of updates from a URL by reading the json from the URL.
      * @param url
      *           The URL to get the updates from.
      * @return
      *       An array of {@link Update} objects.
      */
     public Update[] getUpdates(URL url) {
         try {
             return update.getObject(url.openConnection().getInputStream(), UpdateHolder.class).updates;
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public boolean checkUpdateServer(Updatable object) {
         try {
             URL url = new URL(object.getInfoURL());
 
             HttpURLConnection con = (HttpURLConnection)url.openConnection();
             con.connect();
             if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
                 return false;
         } catch (MalformedURLException e) {
            e.printStackTrace();
             return false;
         } catch (IOException e) {
            e.printStackTrace();
             return false;
         }
         return true;
     }
     
     private String higherVersion(String version1, String version2) {
         return higherVersion(version1, version2, 0, true);
     }
     
     private String higherVersion(String ve, String ve2, int index, boolean recurse) {
         String version1 = ve.replaceAll("b", ".");
         String version2 = ve2.replaceAll("b", ".");
         int v1 = Integer.parseInt(version1.split("\\.")[index]);
         int v2 = Integer.parseInt(version2.split("\\.")[index]);
         if (v1 > v2)
             return version1;
         if (v1 < v2)
             return version2;
         
         if (version1.split("\\.").length >= index + 2 && version2.split("\\.").length >= index + 2 && recurse)
             return higherVersion(ve, ve2, index + 1, true);
         else if (version1.split("\\.").length >= index + 2 && version2.split("\\.").length < index + 2) {
             if (ve.indexOf("b") != -1 && ve.indexOf("b") == -1)
                 return version2;
             else
                 return version1; //Assume version1 is higher than version2
         }
         else if (version1.split("\\.").length < index + 2 && version2.split("\\.").length >= index + 2) {
             if (ve.indexOf("b") == -1 && ve2.indexOf("b") != -1)
                 return version1;
             else
                 return version2; //Assume version2 is higher than version1
         }
             return "";
     }
     
     private class UpdateHolder {
         public Update[] updates;
         private UpdateHolder() { }
     }
 }
 
