 package controllers;
 
 
 import java.util.List;
 
 import models.Look;
 import models.User;
 import models.UserLook;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.mobileCoupon;
 import views.html.mobileFacebook;
 import views.html.mobileIndex;
 import views.html.mobileMe;
 import views.html.mobileOtherLook;
 import views.html.mobileProduct;
 
 
 public class Mobile extends Controller {
 	
 	public static Result index() {
 		return ok(mobileIndex.render(null));
 	}
 	public static Result otherUserLook(Long id) {
 		Look look = Look.find.byId(id);
 		
 		return ok(mobileOtherLook.render(id, look.getUserLooks()));
 	}
 	public static Result coupon(String hash) {
 		return ok(mobileCoupon.render(hash));
 	}
 	
 	public static Result personalProduct(Long id){
 		Look look = Look.find.byId(id);	
 		return ok(mobileProduct.render(look));
 	}
 	
	public static Result me() {
		//UserLook userLook = UserLook.find.byId(new Long(9));
		//userLook.get
		
//		String hash = null;
//		UserLook userLook = UserLook.find.where().ilike("imageFileName", hash).findUnique();
//		User user = userLook.getUser();
//		List<UserLook> userLooks = user.getUserLooks();
//		Route나 POST hash 받고, userLooks 넘기면됨.
		return ok(mobileMe.render());
 	}
 	
 	public static Result myLook(String hash){
 		
 //		String imageUrl;
 		
 		UserLook userLook = UserLook.find.where().ilike("imageFileName", hash).findUnique();		
 		
 //		if(userLook.isImageToS3()){
 //			imageUrl = AMAZON_S3_PATH + userLook.getImageFileName();
 //		}else{
 //			imageUrl = LOCAL_IMAGE_PATH + userLook.getImageFileName();
 //		}
 		
 		return ok(mobileIndex.render(userLook));
 	}
 	public static Result facebook(String hash) {
 		UserLook userLook = UserLook.find.where().ilike("imageFileName", hash).findUnique();
 		return ok(mobileFacebook.render(userLook.getImageUrl(), hash));
 	}
 }
