 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import models.CropImg;
 import models.Picture;
 import models.Stylist;
 import models.UploadImg;
 import play.mvc.Controller;
 
 public class Save extends Controller{
 	
 	public static void stylist(Stylist stylist, String password1, String password2){
 			
 		if(password1.equals(password2)){
 			if(Stylist.loginCheck(stylist.email)){
 				stylist.password=password1;
 				stylist.save();
 				Do.login(stylist.email, stylist.password);
 			}
 			else{
 			}
 		}
 		else{
 		}
 		Application.index();
 	}
 	
 	public static void picture(File[] files, String milliTime) throws IOException{
 		
 		for(int i=0;i<files.length;i++){
System.out.println(files[i]);			
			String filename = milliTime+"-"
			+files[i].getName();			
 System.out.println(filename);
 			UploadImg fileToUpload = new UploadImg(files[i], filename);
 				fileToUpload.upload("");
 //			CropImg fileToCrop = new CropImg(files[i], filename);
 //			UploadImg uploadCroppedImg = new UploadImg(fileToCrop.cropAsSquare(), filename);	
 //				uploadCroppedImg.upload("square/");
 				double ratio = fileToUpload.calculateAspectRatio();
 			Picture pic = new Picture(filename, ratio);
 				pic.stylistId = session.get("id");
 				pic.save();
 		}
		Application.show();
 	}
 }
