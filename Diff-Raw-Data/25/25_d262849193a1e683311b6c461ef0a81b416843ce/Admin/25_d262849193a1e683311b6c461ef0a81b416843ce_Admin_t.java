 package controllers;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import extender.RoleAwareAction;
 
 import models.Child;
 import models.Home;
 import models.Interview;
 import models.Photo;
 import models.Transfer;
 import play.Logger;
 import play.data.Form;
 import play.mvc.*;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import utils.ImageUtils;
 
 @Security.Authenticated(Secured.class)
 public class Admin extends Controller{
 
 	@With(RoleAwareAction.class)
 	public static Result index()
 	{
 		return ok(views.html.admin.home.render());
 	}
 	
 	public static Result newChild()
 	{
 		return ok(views.html.admin.child.render(form(Child.class),Home.all()));
 	}
 	
 	public static Result photo(Long childId)
 	{	
 		// stream from db as blob
 		
 		Photo thumbnail = Photo.findByChildId(childId);
 		if(thumbnail == null)
 		{
 			Logger.info("Thumbnain is null");
 			thumbnail = new Photo(-1L, new byte[]{ });
 		}
 		return ok(thumbnail.image);
 	}
 	
 	public static Result addChild()
 	{
 		//Form<Child> addChildForm = form(Child.class).bindFromRequest();		
 		
 		String name = form().bindFromRequest().get("name");
 		String age = form().bindFromRequest().get("age");
 		String dob = form().bindFromRequest().get("dob");
 		String gender = form().bindFromRequest().get("gender");
 		String home = form().bindFromRequest().get("home");
 		
 		String cwcId = form().bindFromRequest().get("cwcId");
 		String homeAdmissionId = form().bindFromRequest().get("homeAdmissionId");
 		String parent = form().bindFromRequest().get("parent");
 		String nativeTown = form().bindFromRequest().get("nativeTown");
 		String state = form().bindFromRequest().get("state");
 		
 		Child c = Child.create(name,
 							   Integer.valueOf(age), 
 							   new Date(), 
 							   gender,
 							   Home.findById(Long.valueOf(home)),
 							   cwcId,
 							   homeAdmissionId,
 							   parent,
 							   nativeTown,
 							   state
 							  );
 		
 		MultipartFormData formdata = request().body().asMultipartFormData();
 		FilePart photo = formdata.getFile("photo");
 		
 		if(photo!=null)
 		{	
 			File _f = photo.getFile();
 			try
 			{
 				BufferedImage  _i = ImageIO.read(_f);
 				
 				Dimension actualSize = new Dimension(_i.getWidth(),_i.getHeight());
 				Dimension preferedSize = new Dimension(200,200);
 				
 				Dimension size = ImageUtils.resizePreservingAspectRatio(actualSize, preferedSize);				
 				
 				BufferedImage imageBuff = new BufferedImage((int)size.width, (int)size.height, BufferedImage.TYPE_INT_RGB);
 				
 		        Graphics g = imageBuff.createGraphics();
 		        g.drawImage(_i.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH), 0, 0, new Color(0,0,0), null);
 		        g.dispose();
 		        
 		        ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				ImageIO.write(imageBuff, "png", bos);
 				
 				byte[] tn = bos.toByteArray();
 				
 				Logger.info(tn.length +  " size of image for " + c.id);
 				
 				
 				Photo childThumbnail = new Photo(c.id, tn);
 				childThumbnail.save();
 							
 			}catch(Exception e){
 				Logger.error(e.getMessage() + "");
 			}
 		}
 		return ok(views.html.admin.interview.render(form(Interview.class),c));
 		
 	}
 	
 	public static Result addInterview(Long childId)
 	{
 		Child c = Child.find.byId(childId);
 		return ok(views.html.admin.interview.render(form(Interview.class),c));
 	}
 	
 	public static Result beginTransfer(Long childId)
 	{
 		Child c = Child.find.byId(childId);
 		c.fill();
 		return ok(
 					views.html.admin.transfer.render(form(Transfer.class),Home.all(),c)
 				);
 	}
 	
 	public static Result showTransfer(Long transferId)
 	{
 		Transfer t = Transfer.find.byId(transferId);
 		t.fill();
 		
 		return ok(
 					views.html.transfer.summary.render(t)
 				);
 	}
 	
 	public static Result allTransfers()
 	{	
 		return ok(
 					views.html.transfer.all.render(Transfer.all())
 				);
 	}
 
 	public static Result doTransfer(Long childId)
 	{
 		// toHome, approvedBy, reason
 		
 		String toHome = form().bindFromRequest().get("toHome");
 		String approvedBy = form().bindFromRequest().get("approvedBy");
 		String reason = form().bindFromRequest().get("reason");
 		
 		Child c = Child.find.byId(childId);
 		c.fill(); // stupid play. I need to fix this.
 		
 		// this needs to be validated
 		// if we get botched up values
 		// this will fail flat on.
 		Transfer t = new Transfer(c, c.home,
 				Home.findById(Long.valueOf(toHome)),
 				reason, approvedBy);
 		t.save();
 		t.fill();
 		
 		return ok(
 					views.html.transfer.summary.render(t)
 				);
 	}
 	
 	public static Result newInterview(Long childId)
 	{
 		Child c = Child.find.byId(childId);
 		/// all validation here.
 		String transcript = form().bindFromRequest().get("transcript");	
 		Interview i = Interview.create(new Date(), transcript, c);
 		/*List<Object> additionalInfo = new ArrayList<Object>();
 		additionalInfo.add(i);
 		c.addInformation(additionalInfo);*/
 		c.fill();
 		return redirect(routes.Admin.childSummary(c.id));
 	}
 	
 	public static Result allChildren()
 	{
 		return ok(views.html.admin.all.render());
 	}
 	
 	public static Result childSummary(Long id){
 		Child c = Child.find.byId(id);
 		c.fill();
 		return ok(views.html.admin.summary.render(c));
 	}
 }
