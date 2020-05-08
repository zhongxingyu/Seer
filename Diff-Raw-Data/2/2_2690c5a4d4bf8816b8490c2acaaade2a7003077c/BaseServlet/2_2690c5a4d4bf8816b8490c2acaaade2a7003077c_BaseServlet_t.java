 package com.wit.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.template.soy.SoyFileSet;
 import com.google.template.soy.data.SoyMapData;
 import com.google.template.soy.shared.SoyGeneralOptions.CssHandlingScheme;
 import com.google.template.soy.tofu.SoyTofu;
 import com.google.template.soy.tofu.SoyTofu.Renderer;
 import com.wit.base.BaseConstants;
 import com.wit.base.CssRenamingMap;
 import com.wit.base.DetectMobileBrowsers;
 import com.wit.base.Log;
 import com.wit.base.NotLoggedInException;
 import com.wit.base.UserManager;
 import com.wit.base.UserVerifier;
 import com.wit.base.model.Session;
 import com.wit.base.model.User;
 import com.wit.base.model.UserEmail;
 import com.wit.base.model.UserUname;
 
 @SuppressWarnings("serial")
 public class BaseServlet extends HttpServlet {
 
     private static final Logger logger = Logger.getLogger(BaseServlet.class.getName());
 
     // URI
     public static final String HOME_URI = "/";
     public static final String ABOUT_URI = "/about";
     public static final String TERMS_URI = "/terms";
     public static final String FEEDBACK_URI = "/feedback";
     public static final String CONFIRM_EMAIL_URI = "/emailverification";
     public static final String RESET_PASSWORD_URI = "/resetpassword";
     public static final String LOG_OUT_URI = "/logout";
     public static final String USER_URI = "/user";
 
     // Request parameters
     public static final String METHOD = "method";
     public static final String CONTENT = "content";
 
     // Query string ?mode=debug
     public static final String MODE = "mode";
     public static final int PRODUCTION = 0;
     public static final int DEBUG = 1;
 
     // Users choose their devices as desktop or mobile stored in cookie named ST.
     public static final String DEVICE_TYPE = "device";
     public static final int DESKTOP = 0;
     public static final int MOBILE = 1;
 
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
 
         String uri = req.getRequestURI();
         int mode = getMode(req);
         int device = getDeviceType(req);
 
         if (uri.equals(HOME_URI)) {
             Cookie sSIDCookie = getSSIDCookie(req);
             Cookie sIDCookie = getSIDCookie(req);
             if (sSIDCookie != null && sIDCookie != null){
                 try {
                     User user = UserManager.checkLoggedInAndGetUser(
                             sSIDCookie.getValue(), sIDCookie.getValue());
 
                     String page = getTemplate(
                             getWorkSoyFileList(),
                             getWorkSoyMethod(device, mode),
                             getWorkCssRenamingMapFile(device, mode),
                             getWorkSoyMapData(user, mode, device));
                     response(resp, page);
                     return;
                 } catch (JSONException e) {
                     writeExceptionToLogger(logger, e);
                     resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     return;
                 } catch (NotLoggedInException e) {
                     // Not logged in, return home page below.
                 }
             }
 
             //TODO: Save page to Memcache
             String page = getTemplate(
                     getHomeSoyFileList(),
                     getHomeSoyMethod(device, mode),
                     getHomeCssRenamingMapFile(device, mode),
                     getHomeSoyMapData());
             response(resp, page);
         } else if (uri.equals(ABOUT_URI)) {
             String page = getTemplate(
                     getAboutSoyFileList(),
                     getAboutSoyMethod(device, mode),
                     getAboutCssRenamingMapFile(device, mode),
                     getAboutSoyMapData());
             response(resp, page);
         } else if (uri.equals(TERMS_URI)) {
             String page = getTemplate(
                     getTermsSoyFileList(),
                     getTermsSoyMethod(device, mode),
                     getTermsCssRenamingMapFile(device, mode),
                     getTermsSoyMapData());
             response(resp, page);
         } else if (uri.equals(FEEDBACK_URI)) {
             String page = getTemplate(
                     getFeedbackSoyFileList(),
                     getFeedbackSoyMethod(device, mode),
                     getFeedbackCssRenamingMapFile(device, mode),
                     getFeedbackSoyMapData());
             response(resp, page);
         } else if (uri.equals(CONFIRM_EMAIL_URI)) {
             // Confirm the email
             String sessionKeyString = req.getParameter(BaseConstants.SSID);
             String vID = req.getParameter(BaseConstants.VID);
             if (sessionKeyString != null && vID != null) {
                 try {
                     boolean didEmailConfirm = UserManager.confirmEmail(sessionKeyString, vID);
                     String page = getTemplate(
                             getConfirmEmailSoyFileList(),
                             getConfirmEmailSoyMethod(device, mode),
                             getConfirmEmailCssRenamingMapFile(device, mode),
                             getConfirmEmailSoyMapData(didEmailConfirm));
                     response(resp, page);
                 } catch (EntityNotFoundException e) {
                     writeExceptionToLogger(logger, e);
                     resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 }
             } else {
                 logger.warning("doGet: : confirm email url invalid " +
                         req.getRequestURL());
                 resp.sendError(HttpServletResponse.SC_NOT_FOUND);
             }
         } else if (uri.equals(RESET_PASSWORD_URI)) {
             // Response reset password page
             String sessionKeyString = req.getParameter(BaseConstants.SSID);
             String fID = req.getParameter(BaseConstants.FID);
             if (sessionKeyString != null && fID != null) {
 
                 Session session = UserManager.getSession(sessionKeyString,
                         Session.RESET_PASSWORD, fID);
 
                 String page = getTemplate(
                         getResetPasswordSoyFileList(),
                         getResetPasswordSoyMethod(device, mode),
                         getResetPasswordCssRenamingMapFile(device, mode),
                         getResetPasswordSoyMapData(session != null));
                 response(resp, page);
             } else {
                 logger.warning("doGet: : reset password url invalid " +
                         req.getRequestURL());
                 resp.sendError(HttpServletResponse.SC_NOT_FOUND);
             }
         } else if (uri.equals(LOG_OUT_URI)) {
             Cookie sSIDCookie = getSSIDCookie(req);
             Cookie sIDCookie = getSIDCookie(req);
             if (sSIDCookie != null && sIDCookie != null){
 
                 // Delete login sessions
                 UserManager.deleteLoginSession(sSIDCookie.getValue(), sIDCookie.getValue());
 
                 // Delete cookies
                 sSIDCookie.setMaxAge(0);
                 resp.addCookie(sSIDCookie);
 
                 sIDCookie.setMaxAge(0);
                 resp.addCookie(sIDCookie);
             }
             resp.sendRedirect("/");
         } else if (uri.equals(USER_URI)) {
             Cookie sSIDCookie = getSSIDCookie(req);
             Cookie sIDCookie = getSIDCookie(req);
             if (sSIDCookie != null && sIDCookie != null) {
                 try {
                     // Response user page
                     User user = UserManager.checkLoggedInAndGetUser(
                             sSIDCookie.getValue(), sIDCookie.getValue());
 
                     UserUname userUname = UserManager.getUserUname(
                             user.getUserUnameGrpKey(), user.getKey());
 
                     UserEmail userEmail = UserManager.getUserEmail(
                             user.getUserEmailGrpKey(), user.getKey());
 
                     String page = getTemplate(
                             getUserSoyFileList(),
                             getUserSoyMethod(device, mode),
                             getUserCssRenamingMapFile(device, mode),
                             getUserSoyMapData(user, userUname.getUsername(),
                                     userEmail.getKeyName(), user.didConfirmEmail()));
                     response(resp, page);
                     return;
                 } catch (EntityNotFoundException e) {
                     writeExceptionToLogger(logger, e);
                     resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     return;
                 } catch (NotLoggedInException e) {
                     // Not logged in, redirect to home page (below).
                 }
             }
 
             // No cookie, redirect to home page.
             resp.sendRedirect("/");
         } else if (uri.charAt(uri.length() - 1) == '/') {
             // Redirect it without a trailing slash.
             logger.warning("doGet: trailing slash path: " + uri);
             resp.sendRedirect(uri.substring(0, uri.length() - 1));
         } else {
             logger.warning("doGet: incorrect path: " + req.getRequestURI());
             resp.sendError(HttpServletResponse.SC_NOT_FOUND);
         }
     }
 
     public void doPost(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         if (req.getRequestURI().equals(HOME_URI) || req.getRequestURI().equals(USER_URI)) {
 
             String methodName = req.getParameter(METHOD);
             String content = req.getParameter(CONTENT);
 
             if (methodName == null || content == null) {
                 logger.severe("Request parameters missing: methodName = " +
                         methodName + ", content = " + content);
                 resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 return;
             }
 
             @SuppressWarnings("rawtypes")
             Class[] argClasses = {HttpServletResponse.class, String.class};
             Object[] argObjects = {resp, content};
 
            if (req.getRequestURI().equals(USER_URI)) {
                 // Check logging in from sessionID in request parameter.
                 String sessionKeyString = req.getParameter(BaseConstants.SSID);
                 String sessionID = req.getParameter(BaseConstants.SID);
 
                 if (sessionKeyString == null || sessionID == null) {
                     logger.severe("Request parameters missing: sessionKeyString = "
                             + sessionKeyString + ", sessionID = " + sessionID
                             + ", methodName = " + methodName + ", content = " + content);
                     resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     return;
                 }
 
                 User user = null;
                 try {
                     user = UserManager.checkLoggedInAndGetUser(sessionKeyString,
                             sessionID);
                 } catch (NotLoggedInException e) {
                     try {
                         Log log = new Log();
                         log.addLogInfo(BaseConstants.DID_LOG_IN, false, null, null);
                         response(resp, log.getJSONString());
                     } catch (JSONException e1) {
                         writeExceptionToLogger(logger, e);
                         resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     }
                     return;
                 }
 
                 @SuppressWarnings("rawtypes")
                 Class[] argClassesTemp = {HttpServletResponse.class, User.class, String.class,
                                           String.class};
                 Object[] argObjectsTemp = {resp, user, sessionKeyString, content};
                 argClasses = argClassesTemp;
                 argObjects = argObjectsTemp;
             }
 
             try {
                 Method method = getClass().getMethod(methodName, argClasses);
                 method.invoke(this, argObjects);
             } catch (NoSuchMethodException e) {
                 writeExceptionToLogger(logger, e);
                 resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             } catch (IllegalAccessException e) {
                 writeExceptionToLogger(logger, e);
                 resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             } catch (InvocationTargetException e) {
                 // All exceptions thrown by invoked methods will be wrapped
                 // in this exception.
                 writeExceptionToLogger(logger, e);
                 resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             }
         } else {
             logger.warning("doPost: incorrect path: " + req.getRequestURI());
             resp.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
     }
 
     public void logIn(HttpServletResponse resp, String content)
             throws IOException, JSONException, EntityNotFoundException {
         JSONObject jsonObject = new JSONObject(content);
         String usernameOrEmail = jsonObject.getString(BaseConstants.USERNAME);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
 
         Log log = new Log();
 
         User user = UserManager.checkUsernameOrEmailAndPassword(usernameOrEmail,
                 password, log);
         if (user != null) {
             Session session = UserManager.addLoginSession(user.getKey());
 
             resp.addCookie(createSSIDCookie(session.getKeyString()));
             resp.addCookie(createSIDCookie(session.getSessionID()));
         }
 
         response(resp, log.getJSONString());
     }
 
     public void logInAndGetUser(HttpServletResponse resp, String content)
             throws IOException, JSONException, EntityNotFoundException {
         JSONObject jsonObject = new JSONObject(content);
         String usernameOrEmail = jsonObject.getString(BaseConstants.USERNAME);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
 
         Log log = new Log();
 
         User user = UserManager.checkUsernameOrEmailAndPassword(usernameOrEmail,
                 password, log);
         if (user != null) {
             UserUname userUname = UserManager.getUserUname(user.getUserUnameGrpKey(),
                     user.getKey());
             UserEmail userEmail = UserManager.getUserEmail(user.getUserEmailGrpKey(),
                     user.getKey());
 
             Session session = UserManager.addLoginSession(user.getKey());
 
             log.addLogInfo(BaseConstants.USERNAME, true, userUname.getUsername(), null);
             log.addLogInfo(BaseConstants.EMAIL, true, userEmail.getKeyName(), null);
             log.addLogInfo(BaseConstants.SSID, true, session.getKeyString(), null);
             log.addLogInfo(BaseConstants.SID, true, session.getSessionID(), null);
         }
 
         response(resp, log.getJSONString());
     }
 
     public void signUp(HttpServletResponse resp, String content)
             throws IOException, JSONException, MessagingException {
 
         JSONObject jsonObject = new JSONObject(content);
         String username = jsonObject.getString(BaseConstants.USERNAME);
         String email = jsonObject.getString(BaseConstants.EMAIL);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
         String repeatPassword = jsonObject.getString(BaseConstants.REPEAT_PASSWORD);
 
         Log log = new Log();
 
         User user = UserManager.signUp(username, email, password,
                 repeatPassword, log);
         if (user != null) {
 
             // Verify the email!
             UserManager.sendEmailConfirmEmail(user.getKey(), getWebUrl(), getFromEmail(),
                     getFromName(), email, username, getConfirmEmailSubject(),
                     getConfirmEmailMsgBody(), log);
 
             Session session = UserManager.addLoginSession(user.getKey());
 
             resp.addCookie(createSSIDCookie(session.getKeyString()));
             resp.addCookie(createSIDCookie(session.getSessionID()));
         }
 
         response(resp, log.getJSONString());
     }
 
     public void sendEmailResetPassword(HttpServletResponse resp, String content)
             throws IOException, JSONException, MessagingException,
             EntityNotFoundException {
 
         JSONObject jsonObject = new JSONObject(content);
         String usernameOrEmail = jsonObject.getString(BaseConstants.USERNAME);
 
         Log log = new Log();
 
         UserManager.sendEmailResetPassword(usernameOrEmail, getWebUrl(), getFromEmail(),
                 getFromName(), getResetPasswordEmailSubject(), getResetPasswordEmailMsgBody(), log);
 
         response(resp, log.getJSONString());
     }
 
     public void resetPassword(HttpServletResponse resp, String content)
             throws IOException, JSONException {
 
         JSONObject jsonObject = new JSONObject(content);
         String sessionKeyString = jsonObject.getString(BaseConstants.SSID);
         String sessionID = jsonObject.getString(BaseConstants.FID);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
         String repeatPassword = jsonObject.getString(BaseConstants.REPEAT_PASSWORD);
 
         Log log = new Log();
 
         UserManager.resetPassword(sessionKeyString, sessionID, password,
                 repeatPassword, log);
         response(resp, log.getJSONString());
     }
 
     public void changeUsername(HttpServletResponse resp, User user,
             String sessionKeyString, String content) throws IOException,
             JSONException, EntityNotFoundException {
 
         JSONObject jsonObject = new JSONObject(content);
         String username = jsonObject.getString(BaseConstants.USERNAME);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
 
         Log log = new Log();
 
         UserManager.changeUsername(user, username, password, log);
 
         response(resp, log.getJSONString());
     }
 
     public void changeEmail(HttpServletResponse resp, User user,
             String sessionKeyString, String content) throws IOException,
             JSONException, EntityNotFoundException, MessagingException {
 
         JSONObject jsonObject = new JSONObject(content);
         String email = jsonObject.getString(BaseConstants.EMAIL);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
 
         Log log = new Log();
 
         UserManager.changeEmail(user, email, password, log);
         if (log.isValid()) {
             // Reset didConfirmEmail
             UserManager.setUserDidConfirmEmail(user, false);
 
             // Updating User to Memcache done in setUserDidConfirmEmail
 
             // Verify the new email!
             UserUname userUname = UserManager.getUserUname(user.getUserUnameGrpKey(),
                     user.getKey());
             UserManager.sendEmailConfirmEmail(user.getKey(), getWebUrl(), getFromEmail(),
                     getFromName(), email, userUname.getUsername(), getConfirmEmailSubject(),
                     getConfirmEmailMsgBody(), log);
         }
 
         response(resp, log.getJSONString());
     }
 
     public void changePassword(HttpServletResponse resp, User user,
             String sessionKeyString, String content) throws IOException,
             JSONException {
 
         JSONObject jsonObject = new JSONObject(content);
 
         String newPassword = jsonObject.getString(BaseConstants.NEW_PASSWORD);
         String repeatPassword = jsonObject.getString(BaseConstants.REPEAT_PASSWORD);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
 
         Log log = new Log();
 
         UserManager.changePassword(user, sessionKeyString, newPassword,
                 repeatPassword, password, log);
 
         response(resp, log.getJSONString());
     }
 
     public void changeLang(HttpServletResponse resp, User user,
             String sessionKeyString, String content) throws IOException,
             JSONException, EntityNotFoundException {
 
         JSONObject jsonObject = new JSONObject(content);
         String lang = jsonObject.getString(BaseConstants.LANG);
 
         Log log = new Log();
 
         UserManager.setUserLang(user, lang, log);
 
         response(resp, log.getJSONString());
     }
 
     public void deleteAccount(HttpServletResponse resp, User user,
             String sessionKeyString, String content) throws IOException,
             JSONException, EntityNotFoundException {
 
         JSONObject jsonObject = new JSONObject(content);
         String password = jsonObject.getString(BaseConstants.PASSWORD);
 
         Log log = new Log();
 
         UserManager.deleteAccount(user, password, log);
 
         if (log.isValid()) {
             // Delete user's business data
             deleteAllUserContents(user);
 
             // Delete user's cookies
             Cookie sSIDCookie = createSSIDCookie("");
             sSIDCookie.setMaxAge(0);
             resp.addCookie(sSIDCookie);
 
             Cookie sIDCookie = createSIDCookie("");
             sIDCookie.setMaxAge(0);
             resp.addCookie(sIDCookie);
         }
 
         response(resp, log.getJSONString());
     }
 
     public void resendEmailConfirm(HttpServletResponse resp, User user,
             String sessionKeyString, String content) throws IOException,
             JSONException, MessagingException, EntityNotFoundException {
 
         Log log = new Log();
 
         Key userKey = user.getKey();
         UserUname userUname = UserManager.getUserUname(user.getUserUnameGrpKey(), userKey);
         UserEmail userEmail = UserManager.getUserEmail(user.getUserEmailGrpKey(), userKey);
         UserManager.sendEmailConfirmEmail(userKey, getWebUrl(), getFromEmail(), getFromName(),
                 userEmail.getKeyName(), userUname.getUsername(), getConfirmEmailSubject(),
                 getConfirmEmailMsgBody(), log);
         response(resp, log.getJSONString());
     }
 
     public void validateUsername(HttpServletResponse resp, String content)
             throws IOException, JSONException {
 
         JSONObject jsonObject = new JSONObject(content);
         String username = jsonObject.getString(BaseConstants.USERNAME);
 
         Log log = new Log();
 
         UserVerifier.isUsernameValid(username, true, log);
 
         response(resp, log.getJSONString());
     }
 
     public void validateEmail(HttpServletResponse resp, String content)
             throws IOException, JSONException {
 
         JSONObject jsonObject = new JSONObject(content);
         String email = jsonObject.getString(BaseConstants.EMAIL);
 
         Log log = new Log();
 
         UserVerifier.isEmailValid(email, true, log);
 
         response(resp, log.getJSONString());
     }
 
     public String getWebUrl() {
         return "http://www.example.com";
     }
 
     public String getWebName() {
         return "Example.com";
     }
 
     public String getFromEmail() {
         return "admin@example.com";
     }
 
     public String getFromName() {
         return "Example admin";
     }
 
     public void deleteAllUserContents(User user) {
 
     }
 
     public String getResetPasswordEmailSubject() {
         return "Reset password - " + getWebName();
     }
 
     public String getResetPasswordEmailMsgBody() {
         return "<p>Please click the link below to reset password at " + getWebName() + ".</p>"
                 + "<a href='%1$s'>Link to reset password</a>"
                 + "<p>Please ignore this email if you didn't request to reset the password.</p>"
                 + "<p>If the above link is not clickable, copy and paste this link into your web browser's address bar:</p>"
                 + "<p>%1$s</p>";
     }
 
     public String getConfirmEmailSubject() {
         return "Email verification - " + getWebName();
     }
 
     public String getConfirmEmailMsgBody() {
         return "<p>To make sure that you can reset your password. We need to "
                 + "confirm your email address. All it takes is a single click.</p>"
                 + "<p><a href='%1$s'>Link to verify your email address</a></p>"
                 + "<p>If the above link is not clickable, copy and paste this link into your web browser's address bar:</p>"
                 + "<p>%1$s</p>";
     }
 
     public String getBaseSoyPath() {
         return "closure/wit/base/soy/base.soy";
     }
 
     public String getHomeSoyPath() {
         return "closure/wit/home/soy/home.soy";
     }
 
     public String[] getHomeSoyFileList() {
         return new String[]{getBaseSoyPath(), getHomeSoyPath()};
     }
 
     public String getHomeSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.home.soy.home.initMbDebug";
             } else {
                 return "wit.home.soy.home.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.home.soy.home.initDebug";
             } else {
                 return "wit.home.soy.home.init";
             }
         }
     }
 
     public String getHomeCssRenamingMapFile(int device, int mode) {
         return null;
     }
 
     public SoyMapData getHomeSoyMapData() throws JSONException, IOException {
         return null;
     }
 
     public String getWorkSoyPath() {
         return "closure/wit/work/soy/work.soy";
     }
 
     public String[] getWorkSoyFileList() {
         return new String[]{getBaseSoyPath(), getWorkSoyPath()};
     }
 
     public String getWorkSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.work.soy.work.initMbDebug";
             } else {
                 return "wit.work.soy.work.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.work.soy.work.initDebug";
             } else {
                 return "wit.work.soy.work.init";
             }
         }
     }
 
     public String getWorkCssRenamingMapFile(int device, int mode) {
         return null;
     }
 
     public SoyMapData getWorkSoyMapData(User user, int device, int mode) throws JSONException,
             IOException {
         return new SoyMapData(BaseConstants.CONTENT, "");
     }
 
     public String getAboutSoyPath() {
         return "closure/wit/home/soy/about.soy";
     }
 
     public String[] getAboutSoyFileList() {
         return new String[]{getBaseSoyPath(), getHomeSoyPath(), getAboutSoyPath()};
     }
 
     public String getAboutSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.home.soy.about.initMbDebug";
             } else {
                 return "wit.home.soy.about.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.home.soy.about.initDebug";
             } else {
                 return "wit.home.soy.about.init";
             }
         }
     }
 
     public String getAboutCssRenamingMapFile(int device, int mode) {
         return getHomeCssRenamingMapFile(device, mode);
     }
 
     public SoyMapData getAboutSoyMapData() throws JSONException, IOException {
         return null;
     }
 
     public String getTermsSoyPath() {
         return "closure/wit/home/soy/terms.soy";
     }
 
     public String[] getTermsSoyFileList() {
         return new String[]{getBaseSoyPath(), getHomeSoyPath(), getTermsSoyPath()};
     }
 
     public String getTermsSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.home.soy.terms.initMbDebug";
             } else {
                 return "wit.home.soy.terms.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.home.soy.terms.initDebug";
             } else {
                 return "wit.home.soy.terms.init";
             }
         }
     }
 
     public String getTermsCssRenamingMapFile(int device, int mode) {
         return getHomeCssRenamingMapFile(device, mode);
     }
 
     public SoyMapData getTermsSoyMapData() throws JSONException, IOException {
         return null;
     }
 
     public String getFeedbackSoyPath() {
         return "closure/wit/home/soy/feedback.soy";
     }
 
     public String[] getFeedbackSoyFileList() {
         return new String[]{getBaseSoyPath(), getHomeSoyPath(), getFeedbackSoyPath()};
     }
 
     public String getFeedbackSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.home.soy.feedback.initMbDebug";
             } else {
                 return "wit.home.soy.feedback.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.home.soy.feedback.initDebug";
             } else {
                 return "wit.home.soy.feedback.init";
             }
         }
     }
 
     public String getFeedbackCssRenamingMapFile(int device, int mode) {
         return getHomeCssRenamingMapFile(device, mode);
     }
 
     public SoyMapData getFeedbackSoyMapData() throws JSONException, IOException {
         return null;
     }
 
     public String getConfirmEmailSoyPath() {
         return "closure/wit/home/soy/confirmemail.soy";
     }
 
     public String[] getConfirmEmailSoyFileList() {
         return new String[]{getBaseSoyPath(), getHomeSoyPath(), getConfirmEmailSoyPath()};
     }
 
     public String getConfirmEmailSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.home.soy.confirmEmail.initMbDebug";
             } else {
                 return "wit.home.soy.confirmEmail.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.home.soy.confirmEmail.initDebug";
             } else {
                 return "wit.home.soy.confirmEmail.init";
             }
         }
     }
 
     public String getConfirmEmailCssRenamingMapFile(int device, int mode) {
         return getHomeCssRenamingMapFile(device, mode);
     }
 
     public SoyMapData getConfirmEmailSoyMapData(boolean didEmailConfirm) throws JSONException,
             IOException {
         return new SoyMapData(BaseConstants.DID_CONFIRM_EMAIL, didEmailConfirm);
     }
 
     public String getResetPasswordSoyPath() {
         return "closure/wit/home/soy/resetpassword.soy";
     }
 
     public String[] getResetPasswordSoyFileList() {
         return new String[]{getBaseSoyPath(), getHomeSoyPath(), getResetPasswordSoyPath()};
     }
 
     public String getResetPasswordSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.home.soy.resetPassword.initMbDebug";
             } else {
                 return "wit.home.soy.resetPassword.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.home.soy.resetPassword.initDebug";
             } else {
                 return "wit.home.soy.resetPassword.init";
             }
         }
     }
 
     public String getResetPasswordCssRenamingMapFile(int device, int mode) {
         return getHomeCssRenamingMapFile(device, mode);
     }
 
     public SoyMapData getResetPasswordSoyMapData(boolean isSessionValid) throws JSONException,
             IOException {
         return new SoyMapData(
                 BaseConstants.IS_SESSION_VALID, isSessionValid,
                 BaseConstants.ERR_MSG, BaseConstants.RESET_PASSWORD_FAILURE);
     }
 
     public String getUserSoyPath() {
         return "closure/wit/user/soy/user.soy";
     }
 
     public String[] getUserSoyFileList() {
         return new String[]{getBaseSoyPath(), getUserSoyPath()};
     }
 
     public String getUserSoyMethod(int device, int mode) {
         if (device == 1) {
             if (mode == 1) {
                 return "wit.user.soy.user.initMbDebug";
             } else {
                 return "wit.user.soy.user.initMb";
             }
         } else {
             if (mode == 1) {
                 return "wit.user.soy.user.initDebug";
             } else {
                 return "wit.user.soy.user.init";
             }
         }
     }
 
     public String getUserCssRenamingMapFile(int device, int mode) {
         return null;
     }
 
     public SoyMapData getUserSoyMapData(User user, String username, String email,
             boolean didConfirmEmail) throws JSONException, IOException {
         return new SoyMapData(
                 BaseConstants.USERNAME, username,
                 BaseConstants.EMAIL, email,
                 BaseConstants.DID_CONFIRM_EMAIL, didConfirmEmail);
     }
 
     public static int getMode(HttpServletRequest req){
         String value = req.getParameter(MODE);
         if (value != null) {
             try {
                 int mode = Integer.parseInt(value.trim());
                 if (mode == DEBUG) {
                     return DEBUG;
                 } else if (mode == PRODUCTION) {
                     return PRODUCTION;
                 } else {
                     logger.severe("The mode's value not reconized.");
                 }
             } catch (NumberFormatException e) {
                 writeExceptionToLogger(logger, e);
             }
         }
         return PRODUCTION;
     }
 
     public static int getDeviceType(HttpServletRequest req){
         // Check from cookie first
         Cookie sTCookie = getSTCookie(req);
         if (sTCookie != null) {
             String value = getCookieQueryValue(sTCookie, DEVICE_TYPE);
             if (value != null) {
                 try {
                     int device = Integer.parseInt(value.trim());
                     if (device == MOBILE) {
                         return MOBILE;
                     } else if (device == DESKTOP) {
                         return DESKTOP;
                     } else {
                         logger.severe("The device's value not reconized.");
                     }
                 } catch (NumberFormatException e) {
                     writeExceptionToLogger(logger, e);
                 }
             }
         }
         return DetectMobileBrowsers.isMobile(req) ? MOBILE : DESKTOP;
     }
 
     public static void response(HttpServletResponse resp, String content)
             throws IOException {
         // Prevent XSRF - not allow in iframe
         resp.addHeader("X-Frame-Options", "DENY");
         resp.setContentType("text/html; charset=UTF-8");
         resp.setCharacterEncoding("UTF-8");
         PrintWriter out = resp.getWriter();
         out.print(content);
         out.flush();
         out.close();
     }
 
     public static void writeExceptionToLogger(Logger logger, Exception e) {
         StringWriter sw = new StringWriter();
         e.printStackTrace(new PrintWriter(sw));
         logger.severe(sw.toString());
     }
 
     public static String getTemplate(String[] soyFileList, String soyMethod,
             String cssRenamingMapFile, SoyMapData soyMapData)
             throws IOException {
 
         // Bundle the Soy files for your project into a SoyFileSet.
         SoyFileSet.Builder builder = new SoyFileSet.Builder();
         for (int i = 0; i < soyFileList.length; i++) {
             builder.add(new File(soyFileList[i]));
         }
         if (cssRenamingMapFile != null) {
             builder.setCssHandlingScheme(CssHandlingScheme.BACKEND_SPECIFIC);
         }
         SoyFileSet sfs = builder.build();
 
         // Compile the template into a SoyTofu object.
         // SoyTofu's newRenderer method returns an object that can render any
         // template in file set.
         SoyTofu tofu = sfs.compileToTofu();
 
         Renderer renderer = tofu.newRenderer(soyMethod);
         if (cssRenamingMapFile != null) {
             renderer.setCssRenamingMap(new CssRenamingMap(cssRenamingMapFile));
         }
         if (soyMapData != null) {
             renderer.setData(soyMapData);
         }
 
         // Call the template.
         return renderer.render();
     }
 
     public static String getCookieQueryValue(Cookie cookie, String name) {
         String nameEQ = name + "=";
         String[] ca = cookie.getValue().split(";");
         for (int i = 0; i < ca.length; i++) {
             String c = ca[i];
             while (c.length() > 0 && c.charAt(0) == ' ') {
                 c = c.substring(1);
             }
             if (c.indexOf(nameEQ) == 0) {
                 return c.substring(nameEQ.length());
             }
         }
         return null;
     }
 
     public static Cookie getSSIDCookie(HttpServletRequest req) {
         return getCookie(req, BaseConstants.SSID);
     }
 
     public static Cookie getSIDCookie(HttpServletRequest req) {
         return getCookie(req, BaseConstants.SID);
     }
 
     public static Cookie getSTCookie(HttpServletRequest req) {
         return getCookie(req, BaseConstants.ST);
     }
 
     public static Cookie createSSIDCookie(String cookieValue) {
         return createCookie(BaseConstants.SSID, cookieValue, 20, "/");
     }
 
     public static Cookie createSIDCookie(String cookieValue) {
         return createCookie(BaseConstants.SID, cookieValue, 20, "/");
     }
 
     /*public static Cookie createSTCookie(String cookieValue) {
 
         //TODO: append value, not replace!
         //TODO: create at client side??
 
         return createCookie(ST_COOKIE, cookieValue, 365, "/");
     }*/
 
     public static Cookie getCookie(HttpServletRequest req, String cookieName){
         Cookie[] cookies = req.getCookies();
         if (cookies != null){
             for(int i = 0; i < cookies.length; i++){
                 Cookie cookie = cookies[i];
                 if(cookie.getName().equals(cookieName)){
                     return cookie;
                 }
             }
         }
         return null;
     }
 
     public static Cookie createCookie(String cookieName, String cookieValue,
             int days, String path){
         Cookie cookie = new Cookie(cookieName, cookieValue);
         cookie.setPath(path);
         cookie.setMaxAge(days * 24 * 60 * 60);
         return cookie;
     }
 }
