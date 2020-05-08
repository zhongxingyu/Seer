 package controllers;
 
 import java.util.Date;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import models.Guestbook;
 import play.*;
 import play.data.Form;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
 	final static Result INDEX = redirect(routes.Application.index());
 	
 	final static Form<Guestbook> guestbookForm = form(Guestbook.class);
 	
 	public static Result index() {
		return ok(
				index.render(
						Guestbook.find.all()));
 	}
 
 	public static Result write() {
 		return ok(write.render(guestbookForm));
 	}
 
 	public static Result doDelete(Long id, String password) {
 		Guestbook gb = Guestbook.findbyIDPW(id, password);
 		if (gb != null){
 			gb.delete();
 			flash("info", "방명록에 삭제 되었습니다.");
 		}else{
 			flash("error", "해당하는 방명록이 없거나 패스워드가 틀립니다.");
 		}
 		return INDEX;
 	}
 
 	public static Result doWrite() {
 		Form<Guestbook> guestbookForm = form(Guestbook.class).bindFromRequest();
 		Guestbook gb = guestbookForm.get();
 		gb.password = DigestUtils.md5Hex( gb.password );
 		gb.postAt = new Date();
 		gb.save();
 		flash("info", "방명록에 등록 되었습니다.");
 		return INDEX;
 	}
 
 }
