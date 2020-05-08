 package controllers;
 
 
 import models.Look;
 import models.UserLook;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.*;
 
 
 public class Mobile extends Controller {
 	
 	public static Result index() {
		return ok(mobileIndex.render("", null));
 	}
 	public static Result otherUserLook(Long id) {
 		Look look = Look.find.byId(id);
 		
 		return ok(mobileOtherLook.render(id, look.getUserLooks()));
 	}
	public static Result coupon() {
		return ok(mobileCoupon.render());
 	}
 	
 	public static Result myLook(String hash){
 		
 //		String imageUrl;
 		
		UserLook userLook = UserLook.find.where().ilike("imageFileName", hash).findUnique();
		userLook.getId();
 		
 //		if(userLook.isImageToS3()){
 //			imageUrl = AMAZON_S3_PATH + userLook.getImageFileName();
 //		}else{
 //			imageUrl = LOCAL_IMAGE_PATH + userLook.getImageFileName();
 //		}
 		
		return ok(mobileIndex.render(userLook.getImageUrl(), userLook.getLook().getId()));
 	}
 	public static Result facebook() {
 		String imgUrl = request().body().asFormUrlEncoded().get("facebook-uplaod")[0];
 		return ok(mobileFacebook.render(imgUrl));
 	}
 }
