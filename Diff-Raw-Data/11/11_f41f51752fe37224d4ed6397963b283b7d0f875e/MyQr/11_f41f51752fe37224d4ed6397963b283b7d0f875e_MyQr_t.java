 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.libs.Json;
 
 import views.html.*;
 import models.*;
 
 import play.data.*;
 import play.data.validation.Constraints.*;
 import java.util.*;
 import java.io.File;
 import play.mvc.Http.MultipartFormData.*;
 import play.mvc.Http.MultipartFormData;
 
 import com.mongodb.MongoException;
 
 public class MyQr extends Controller {
 
 	static Form<Qrcode> qrForm = form(Qrcode.class);
 	
 	public static Result viewQr(String id) {
 		MonDataBase db = MonDataBase.getInstance();
 		
 		try {
 			Qrcode qr = db.getQrCode(id);
 			
 			try {
 				Stats stats = db.getQrStats(id);
 				return ok(myQr.render(Login.getCustSession(), qr, qrForm.fill(qr), stats, InfoDisplay.INFO, stats.getInfo()));
 			}
 			catch (Exception e) {
 				return ok(myQr.render(Login.getCustSession(), qr, qrForm.fill(qr), null, InfoDisplay.INFO, " " + e));
 			}
 
 			
 		}
 		catch (Exception e) {
 			try {
				return ok(myQrTable.render(Login.getCustSession(), QrTable.deleteForm, db.getCustomersQrs(0), InfoDisplay.ERROR, "Cannot view this QrCode. " + e));
 			}
 			catch (Exception f) {
				return badRequest(myQrTable.render(Login.getCustSession(), QrTable.deleteForm, null, InfoDisplay.ERROR, "Impossible to get your Qrcodes." + f));
 			}
 		}
 	}
 	
 	public static Result submit() {
 
 /**
   *TODO: Implement the MonDataBase method updateQrcode
  **/		
 		Form<Qrcode> filledForm = qrForm.bindFromRequest();
 		MonDataBase MongoDB = MonDataBase.getInstance();
 		HashMap<String, String> hmap = new HashMap<String, String>();
 		
 		try{
 			if(!filledForm.field("redirection").value().isEmpty() || !filledForm.field("title").value().isEmpty() || !filledForm.field("place").value().isEmpty()){
 				hmap.put("id",filledForm.field("id").value());
 				// Insert the redirection field if not empty
 				if(!filledForm.field("redirection").value().isEmpty()) {
 					hmap.put("redirection", filledForm.field("redirection").value());
 				}
 
 				// Insert the title field if not empty
 				if(!filledForm.field("title").value().isEmpty()) {
 					hmap.put("title", filledForm.field("title").value());
 				}
 
 				// Insert the place field if not empty
 				if(!filledForm.field("place").value().isEmpty()) {
 					hmap.put("place", filledForm.field("place").value());
 				}
 
 		
 				MongoDB.updateQRCode(hmap);
 				Qrcode qr = MongoDB.getQrCode(filledForm.field("id").value());
 
 				return ok(myQr.render(Login.getCustSession(), qr,qrForm.fill(qr), null, InfoDisplay.SUCCESS, "Your changes have been updated."));
 			} else {
 				Qrcode qr = MongoDB.getQrCode(filledForm.field("id").value());
 				return ok(myQr.render(Login.getCustSession(), qr,qrForm.fill(qr), null, InfoDisplay.ERROR, "Please fill at least one field."));
 			}
 		}catch(Exception e){
 			return badRequest("not implemented");
 		}
 	}
 
 	public static Result removeQr(String id){
 		MonDataBase db = MonDataBase.getInstance();
 		
 		try {
 			db.removeQRCode(id);
 			QrArray qrs =  db.getCustomersQrs(0);
			return ok(myQrTable.render(Login.getCustSession(), QrTable.deleteForm, qrs, InfoDisplay.SUCCESS, "Qr code deleted" ));
 		}
 		catch (Exception e){
			return ok(myQrTable.render(Login.getCustSession(), QrTable.deleteForm, null ,InfoDisplay.ERROR, "Problem when trying to delete " + e ));
 		}
 	}
 }
