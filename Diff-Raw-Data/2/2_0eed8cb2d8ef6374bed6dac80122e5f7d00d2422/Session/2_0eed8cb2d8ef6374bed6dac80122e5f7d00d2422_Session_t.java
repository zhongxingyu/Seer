 /*
  * This file is part of anycook. The new internet cookbook
  * Copyright (C) 2014 Jan Graßegger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see [http://www.gnu.org/licenses/].
  */
 
 package de.anycook.session;
 
 
 import de.anycook.conf.Configuration;
 import de.anycook.db.mysql.DBSaveRecipe;
 import de.anycook.db.mysql.DBUser;
 import de.anycook.news.life.Lifes;
 import de.anycook.social.facebook.FacebookHandler;
 import de.anycook.user.User;
 import org.apache.commons.lang3.RandomStringUtils;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.WebApplicationException;
 import java.io.IOException;
 import java.sql.SQLException;
 
 
 /**
  * Fuegt Daten zur Session hinzu und gibt sie zurueck. In der Session werden Filterparameter und Logindaten gespeichert.
  *
  * @author Jan Grassegger
  */
 public class Session {
     private static final String adminMail;
     private static final String adminPwd;
 
     static {
         adminMail = Configuration.getPropertyAdminMail();
         adminPwd = Configuration.getPropertyAdminPassword();
     }
 
     private User login;
     private final Logger logger;
 
     /**
      * Konstruktor initiert Variablen
      */
     private Session() {
         logger = Logger.getLogger(getClass());
         login = null;
 
     }
 
     /**
      * Ueberprueft, ob bereits eine INstanz von Sessionhandler in der Session gespeichert ist, wenn wird eine neue erzeugt.
      *
      * @param session HttpSession des Users
      * @return instanz von Sessionhandler
      */
     static public Session init(HttpSession session) {
         if (session.getAttribute("shandler") != null)
             return (Session) session.getAttribute("shandler");
 
         Session shandler = new Session();
         session.setAttribute("shandler", shandler);
 
         return shandler;
     }
 
     static public Session init(HttpServletRequest request){
         Session session = init(request.getSession(true));
         if(session.login == null){
             try {
                 session.loginWithCookies(request.getCookies());
             } catch (IOException | SQLException e) {
                 Logger.getLogger(Session.class).error(e, e);
             }
         }
 
         return session;
     }
 
 	/*@SuppressWarnings("unused")
 	private void checkSessiontime(){
 		long oldtime = session.getLastAccessedTime();
 		long akttime = System.currentTimeMillis();
 		if(akttime-oldtime > 1800000)
 			clearSession();
 	}*/
 
     // Login
 
     /**
      * ueberprueft ob login vorhanden
      *
      * @return true, wenn login gesetzt, sonst false
      */
     public boolean checkLogin() {
         if (login == null)
             throw new WebApplicationException(401);
         return true;
     }
 
     public void checkAdminLogin(){
         if(login != null && login.isAdmin()) return;
         throw new WebApplicationException(401);
     }
 
     public boolean checkLoginWithoutException(){
         return login != null;
     }
 
     public void loginWithCookies(javax.servlet.http.Cookie[] cookies) throws IOException, SQLException {
        if(cookies == null) return;

         for (javax.servlet.http.Cookie cookie : cookies) {
             if (cookie.getName().equals("anycook")) {
                 String cookieId = cookie.getValue();
                 try {
                     login(cookieId);
                 } catch (DBUser.CookieNotFoundException | DBUser.UserNotFoundException e) {
                     logger.warn(e, e);
                 }
             }
 
             String fbCookieKey = "fbsr_" + FacebookHandler.APP_ID;
             if (cookie.getName().equals(fbCookieKey)) {
                 String cookieValue = cookie.getValue();
                 FacebookHandler.FacebookRequest request = FacebookHandler.decode(cookieValue);
                 Long uid = Long.parseLong(request.user_id);
                 try {
                     facebookLogin(uid);
                 } catch (User.LoginException | DBUser.UserNotFoundException e) {
                     logger.warn(e, e);
                 }
             }
         }
     }
 
     /**
      * loggt einen user ein, wenn pwd und mail korrekt
      */
     public void login(int userId, String password) throws SQLException, DBUser.UserNotFoundException, IOException, User.LoginException {
         if (userId == -1) {
             if (password.equals(adminPwd)) {
                 login = User.initAdmin();
                 logger.info("logged in as admin");
             } else
                 logger.warn("admin login failed");
         } else {
             login = User.login(userId, password);
         }
 
 
         logger.info(login.getName() + " logged in");
 
     }
 
     public void login(String cookieId) throws SQLException, IOException, DBUser.UserNotFoundException, DBUser.CookieNotFoundException {
         login = User.login(cookieId);
     }
 
     public void facebookLogin(String signedRequest) throws IOException, User.LoginException, SQLException, DBUser.UserNotFoundException {
         FacebookHandler.FacebookRequest request = FacebookHandler.decode(signedRequest);
         facebookLogin(Long.parseLong(request.user_id));
     }
 
     public void facebookLogin(Long uid) throws SQLException, IOException, User.LoginException, DBUser.UserNotFoundException {
         login = User.facebookLogin(uid);
     }
 
     public String makePermanentCookieId(int userid) throws SQLException {
         try(DBUser dbuser = new DBUser()){
             String newId;
 
             do {
                 newId = RandomStringUtils.randomAlphanumeric(20);
             } while (dbuser.checkCookieId(newId));
             dbuser.setCookieId(newId, userid);
             return newId;
         }
     }
 
     /**
      * loescht logindaten und loggt damit aus
      */
     public void logout() {
         logger.info(login + " logged out");
         login = null;
     }
 
     public User getUser() {
         checkLogin();
         return login;
     }
 
     //schmeckt
     public boolean makeSchmeckt(String gericht) throws SQLException {
         if (login != null && !checkSchmeckt(gericht)) {
             DBSaveRecipe savegericht = new DBSaveRecipe();
             savegericht.makeTasty(gericht, login.getId());
             Lifes.addLife(Lifes.Case.TASTES, login.getId(), gericht);
             savegericht.close();
 
             //send to recipemaker
             //login.sendSchmecktMail(gericht);
 
 //			if(login.getFacebookID() > 0 && login.getLevel() == 2){
 //				WallPoster wp = new WallPoster(login.getFacebookID(), login.getFaceBookAccessToken());
 //				wp.postSchmeckt(gericht);
 //			}
 
             return true;
         }
         return false;
     }
 
     public boolean removeSchmeckt(String gericht) throws SQLException {
         if (login != null && checkSchmeckt(gericht)) {
             DBSaveRecipe savegericht = new DBSaveRecipe();
             savegericht.unmakeTasty(gericht, login.getId());
             savegericht.close();
             return true;
         }
         return false;
     }
 
     public boolean checkSchmeckt(String gericht) throws SQLException {
         if (login == null) return true;
         DBSaveRecipe savegericht = new DBSaveRecipe();
         boolean check = savegericht.isTasty(gericht, login.getId());
         savegericht.close();
         return check;
 
     }
 
 //	public void discuss(String text, String gericht){
 //		Discussion.discuss(text, login.getId(), gericht);
 //	}
 //	
 //	public void answerDiscuss(String text, String gericht, String pid){
 //		Discussion.answer(text, Integer.parseInt(pid), login.getId(), gericht);
 //	}
 //	
 //	public int like(String gericht, String id){
 //		
 //		return Discussion.like(login.getId(), gericht, id);
 //	} 
 
     public void addCookieID(String id, int userid) throws SQLException {
         DBUser dbuser = new DBUser();
         dbuser.setCookieId(id, userid);
         dbuser.close();
         logger.info("added persistent cookieid: " + id + " to " + userid);
     }
 
     public void deleteCookieID(String id) throws SQLException {
         DBUser dbuser = new DBUser();
         dbuser.deleteCookieId(id);
         dbuser.close();
         logger.info("deleted persistent cookieid: " + id);
     }
 
     public void reloadUser() throws SQLException, IOException, DBUser.UserNotFoundException {
         login = User.init(login.getId());
     }
 
     public static class UserAuth {
         public String username;
         public String password;
         public boolean stayLoggedIn;
         public int appId;
     }
 
 }
