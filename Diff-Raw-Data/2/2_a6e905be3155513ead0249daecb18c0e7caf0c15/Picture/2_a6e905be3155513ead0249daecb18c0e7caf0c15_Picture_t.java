 /**
  * JMBS: Java Micro Blogging System
  *
  * Copyright (C) 2012
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY. See the GNU General Public License for more details. You should
  * have received a copy of the GNU General Public License along with this
  * program. If not, see <http://www.gnu.org/licenses/>.
  *
  * @author Younes CstaticHEIKH http://cyounes.com
  * @author Benjamin Babic http://bbabic.com
  *
  */
 package jmbs.server;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 public class Picture {
 
     private final static String DEFAULT_SEPARATOR = "/";
     private final static String DEFAULT_PATH = ".";
     private final static String DEFAULT_AVATAR_NAME = "avatar.jpg";
     private final static String DEFAULT_FORMAT = "jpg";
     private final static String DEFAULT_AVATAR_PATH = "./avatar.jpg";
 
     public static boolean createUserRepertory(int userid) {
         File f = new File(getRepertoryPath(userid));
         boolean ret = false;
 
         if (!f.exists()) {
             ret = f.mkdir();
         }
 
         return ret;
     }
 
     public static boolean deleteUserRepertory(int userid) {
         boolean ret = false;
         File f = new File(getRepertoryPath(userid));
 
         if (f.isDirectory()) {
             File[] subfiles = f.listFiles();
             String subfileName;
 
             for (File i : subfiles) {
                 subfileName = i.getName();
                 i.delete();
                 System.out.println(subfileName + " deleted.");
             }
             if (ret = f.delete()) {
                 System.out.println(f.getName() + " deleted.");
             }
         } else {
             System.err.println("This user has no avatar repertory.");
         }
 
         return ret;
     }
 
     private static String getRepertoryPath(int userid) {
         return (DEFAULT_PATH + DEFAULT_SEPARATOR + String.valueOf(userid) + DEFAULT_SEPARATOR);
     }
 
     private static String getAvatarPath(int userid) {
         return (getRepertoryPath(userid) + DEFAULT_AVATAR_NAME);
     }
 
     private static BufferedImage getPicture(String path) {
         BufferedImage img = null;
 
         try {
             img = ImageIO.read(new File(path));
         } catch (IOException e) {
             try {
                 img = ImageIO.read(new File(DEFAULT_AVATAR_PATH));
             } catch (IOException ex) {
                 System.err.println("Error on filepath no files named: " + path);
             }
         }
 
         return img;
     }
 
     private static boolean setPicture(int userid, BufferedImage img, String format) {
        String nom = DEFAULT_AVATAR_NAME;
         String path = getRepertoryPath(userid) + nom;
         boolean ret;
 
         createUserRepertory(userid);
         File f = new File(path);
 
         try {
             ImageIO.write(img, format, f);
             ret = true;
         } catch (IOException e) {
             ret = false;
             System.out.println("Image cannot be saved.");
         }
 
         return ret;
     }
 
     private static byte[] convert(BufferedImage im) {
         byte[] imageInByte = null;
         if (im != null) {
             try {
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ImageIO.write(im, DEFAULT_FORMAT, baos);
 
                 baos.flush();
                 imageInByte = baos.toByteArray();
                 baos.close();
             } catch (IOException e) {
                 //Logger.getLogger(PictureDAO.class.getName()).log(Level.SEVERE, null, e);
             }
         }
         return imageInByte;
     }
 
     private static BufferedImage convert(byte[] ib) {
         BufferedImage im = null;
         try {
             im = ImageIO.read(new ByteArrayInputStream(ib));
         } catch (IOException e) {
             //Logger.getLogger(PictureDAO.class.getName()).log(Level.SEVERE, null, e);
         }
         return im;
     }
 
     public static byte[] getUserPicture(int userId) {
         return convert(getPicture(getAvatarPath(userId)));
     }
 
     public static boolean setUserPicture(int userId, byte[] pic) {
         return setPicture(userId, convert(pic), DEFAULT_FORMAT);
 
     }
     
 
 }
