 package controllers;
 
 import static play.data.Form.*;
 
 import java.util.List;
 
 import models.MyData;
 
 import play.*;
 import play.mvc.*;
 import play.data.Form;
 import views.html.*;
 
 public class Application extends Controller {
 
 	public static class MyForm {
 		public String name;
 		public String mail;
 		public String tel;
 
 		@Override
 		public String toString() {
 			return String.format("NAME:%s MAIL:%s TEL:%s", name, mail, tel);
 		}
 	}
 
 	public static Result index() {
		String title = "あなたはどこに行きたい？？？？？";
 		String msg = "フォーム";
 
 		Form<MyData> mydata = form(MyData.class);
 		List<MyData> mydatas = MyData.find.all();
 
 		return ok(index.render(title, msg, mydatas, mydata));
 	}
 
 	public static Result add() {
 		Form<MyData> mydata = form(MyData.class).bindFromRequest();
 		if (mydata.hasErrors() == false) {
 			mydata.get().save();
 			flash();
 		}
 		return redirect(routes.Application.index());
 	}
 }
