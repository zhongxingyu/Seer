 
 package controllers;
 
 import java.util.Date;
 
 import models.Statement;
 
 import models.Suser;
 import models.Upvote;
 import play.Logger;
 import play.cache.Cache;
 import play.libs.Codec;
 import play.libs.Images;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Auth.class)
 public class Application extends Controller {
 
     public static void index(Long id,int positive) {
         Statement parent;
     	boolean isFirst = false;
     	if(id == null ){
     		parent = Statement.find("order by id asc").first();
     		isFirst = true;
     	}else{
     		parent = Statement.findById(id);
     	}
         render(parent,positive,isFirst);
     }
 
     public static void addStatement(Long parent, int positive, String text){
     	Suser suser = Auth.connectedUser();
         if(suser == null){
             flash.put("method","addStatement");
             flash.put("param1",parent);
             flash.put("param2",positive);
             flash.put("param3",text);
             login();
         }
     	if(text.equals("")){
     		index(parent,positive);
     	}
     	Statement newStatement = new Statement();
     	Statement orig = Statement.findById(parent);
     	newStatement.parent = orig;
     	newStatement.positive = positive == 1 ? Boolean.TRUE : Boolean.FALSE;
     	String statementText = Util.getStatementText(text);
     	newStatement.st_text = statementText.replace("\r\n", "<br>");
     	newStatement.owner = suser;
     	newStatement.entryDate = new Date();
     	newStatement.save();
     	flash.put("successMessage","Gönderildi");
         index(parent,positive);
     }
 
     public static void deleteStatement(Long id){
         Suser suser = Auth.connectedUser();
         if(suser == null){
             flash.put("method","deleteStatement");
             flash.put("param1",id);
             flash.put("param2","");
             flash.put("param3","");
             login();
         }
         Statement st = Statement.findById(id);
         if(st==null){
             flash.put("successMessage","Yorum bulunamadi");
             index(null,0);
         }
         if(suser != st.owner){
             flash.put("successMessage","Silme hakkin yok");
            index(st.parent.id,st.positive ? 1 : -1);
         }
         Statement parent = st.parent;
         int pos = st.positive ? 1 : -1;
         st.delete();
         index(parent.id,pos);
     }
 
     public static void editStatement(Long id,String text){
         Suser suser = Auth.connectedUser();
         if(suser == null){
             flash.put("method","editStatement");
             flash.put("param1",id);
             flash.put("param2",text);
             flash.put("param3","");
             login();
         }
         Statement st = Statement.findById(id);
         if(st==null){
             flash.put("successMessage","Yorum bulunamadi");
             index(null,0);
         }
         if(suser != st.owner){
             flash.put("successMessage","Degisiklik hakkin yok");
             index(st.id,0);
         }
         String statementText = Util.getStatementText(text);
         st.st_text = statementText.replace("\r\n", "<br>");
         st.save();
         index(st.parent.id,st.positive ? 1 : -1);
     }
 
     public static void upvoteStatement(Long id){
         Suser suser = Auth.connectedUser();
         if(suser == null){
             flash.put("method","upvoteStatement");
             flash.put("param1",id);
             flash.put("param2","");
             flash.put("param3","");
             login();
         }
     	Statement s = Statement.findById(id);
     	Upvote upvote = Upvote.find("suser = ? and statement = ?",suser,s ).first();
     	if(upvote == null){
     		new Upvote(suser,s).save();
     	}
     	long count = upvote.count("statement=?",s);
     	s.point =(int)count;
     	s.save();
         index(s.parent.id,s.positive ? 1 : -1);
     }
     
     public static void captcha(String id) {
         Images.Captcha captcha = Images.captcha();
         String code = captcha.getText("#424449");
         Cache.set(id, code, "10mn");
         renderBinary(captcha);
     }
     
     public static void login(){
         flash.keep();
         if(!flash.contains("method")){
         	flash.put("method", "");
         	flash.put("param1","");
             flash.put("param2","");
             flash.put("param3","");
         }     
         String randomID = Codec.UUID();
         render(randomID);
     }
     
     public static void logout() {              
         session.clear();
         index(null,0);
     }
 
 }
