 package com.github.russ4stall.reciperoots.utilities;
 
 import java.io.File;
 
 /**
  * Date: 7/9/13
  * Time: 10:32 AM
  *
  * @author Russ Forstall
  */
 public class PictureToolsImpl {
 
 
     public static String getPicture(int id) {
         String fileName = "C:\\Users\\russellf\\Documents\\GitHub\\recipe-roots\\src\\main\\webapp\\images\\recipeimages\\dishpic_" + id + ".jpg";
 
         File file = new File(fileName);
         if (file.exists()) {

             return "/images/recipeimages/dishpic_" + id + ".jpg";
         } else {

             return "/images/recipeimages/dishpic_generic.jpg";
         }
     }
 }
