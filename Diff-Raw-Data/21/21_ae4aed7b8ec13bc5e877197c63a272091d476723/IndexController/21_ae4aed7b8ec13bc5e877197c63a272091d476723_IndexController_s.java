 package biz.letsweb.lukasfloorspring;
 
 import biz.letsweb.lukasfloorspring.dataaccess.dao.JdbcPriceLineDao;
 import biz.letsweb.lukasfloorspring.dataaccess.dao.JdbcUsersDao;
 import biz.letsweb.lukasfloorspring.dataaccess.model.PriceLine;
 import biz.letsweb.lukasfloorspring.dataaccess.model.User;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.Callable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.MessageSource;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.async.WebAsyncTask;
 
 @Controller
 @RequestMapping(value = {"/"})
 public class IndexController {
 
   final Logger logger = LoggerFactory.getLogger(IndexController.class);
   public static final String VIEW_NAME = "index";
   @Autowired
   private JdbcUsersDao usersDao;
   @Autowired
   private JdbcPriceLineDao pricesDao;
   @Autowired
   private MessageSource messageSource;
   // @Autowired
   // private SessionContainer sessionContainer;
   private String requestString;
   @Autowired
   private Message myMessage;
 
   @Value("${spring.social.facebook.appId}")
   private String facebookClientId;
 
   @Value("${spring.social.facebook.appSecret}")
   private String facebookClientSecret;
 
   // @Autowired
   // private Facebook facebook;
 
   @RequestMapping(method = RequestMethod.GET)
   public String sayHello(ModelMap model, Locale locale) {
     model.addAttribute("date", new Date());
     // model.addAttribute("user", user);
    final String message = messageSource.getMessage("welcome", new Object[] {}, locale);
     logger.info("message: {}", message);
     logger.info("facebookClientId: {}, secret: {}", facebookClientId, facebookClientSecret);
     myMessage.setMessage("myMessage");
     myMessage.setType(message);
     model.addAttribute("message", myMessage);
     // sessionContainer.setSessionString("tomasz");
     requestString = "set in index";
    return "index";
   }
 
   @PreAuthorize("isAuthenticated()")
   @RequestMapping(value = "/admin", method = RequestMethod.GET)
   public String goAdmin(ModelMap model) {
     final User user = usersDao.findUserById(1);
     // logger.info("In method ... ");
     model.addAttribute("date", new Date());
     model.addAttribute("user", user);
     return "index/admin";
   }
 
   @RequestMapping(value = "/cennik", method = RequestMethod.GET)
   public String goPricelist(ModelMap model) {
     List<PriceLine> prices = pricesDao.findAll();
     model.addAttribute("prices", prices);
     logger.info("message: {}" + prices);
     return "cennik";
   }
 
   @RequestMapping(value = "/kontakt", method = RequestMethod.GET)
   public String goContact(ModelMap model) {
     return "kontakt";
   }
 
   // @RequestMapping(value = "/fb", method = RequestMethod.GET)
   // public String goFb(ModelMap model) {
   // if (!facebook.isAuthorized()) {
   // return "redirect:/connect/facebook";
   // }
   // model.addAttribute(facebook.userOperations().getUserProfile());
   // PagedList<Post> homeFeed = facebook.feedOperations().getHomeFeed();
   // model.addAttribute("feed", homeFeed);
   //
   // return "kontakt";
   // }
 
   @RequestMapping(value = "/firma", method = RequestMethod.GET)
   public String goFirm(ModelMap model) {
     return "firma";
   }
 
   @RequestMapping(value = "/404", method = RequestMethod.GET)
   public String error404(ModelMap model) {
     System.out.println("404");
     return "404";
   }
 
   // @RequestMapping(value = "/ajax", method = RequestMethod.GET)
   @Async
   @RequestMapping(value = "/ajax", method = {RequestMethod.GET, RequestMethod.HEAD}, headers = "x-requested-with=XMLHttpRequest")
   public @ResponseBody
   AjaxModel ajax(ModelMap model) throws InterruptedException {
     User user = usersDao.findUserById(1);
     AjaxModel am = new AjaxModel();
     // Thread.sleep(3000);
     am.name = user.getFname() + " " + user.getLname();
     am.staff = new String[] {"kibol1", "kibol2"};
     return am;
   }
 
   @Async
   @RequestMapping(value = "/ajax/prices", method = {RequestMethod.GET, RequestMethod.HEAD}, headers = "x-requested-with=XMLHttpRequest")
   public @ResponseBody
   List<PriceLine> ajaxPriceslo(ModelMap model) {
     List<PriceLine> prices = pricesDao.findAll();
     return prices;
   }
 
   @RequestMapping(value = "/login", method = RequestMethod.GET)
   public String goLogin(ModelMap model) {
     final User user = usersDao.findUserById(1);
     // logger.info("In method ... ");
     model.addAttribute("date", new Date());
     model.addAttribute("user", user);
     // System.out.println(sessionContainer.getSessionString());
     System.out.println(requestString);
 
     return "index/login";
   }
 
   @RequestMapping(value = "/logout", method = RequestMethod.GET)
   public String goLogout(ModelMap model) {
     final User user = usersDao.findUserById(1);
     // logger.info("In method ... ");
     model.addAttribute("date", new Date());
     model.addAttribute("user", user);
     return "index/logout";
   }
 
   @RequestMapping(value = "/login/failed", method = RequestMethod.GET)
   public String goLoginFailed(ModelMap model) {
     final User user = usersDao.findUserById(1);
     // logger.info("In method ... ");
     model.addAttribute("date", new Date());
     model.addAttribute("user", user);
     return "forward:/index/login";
   }
 
   @RequestMapping(value = "/locale/{lang}")
   public void changeLocale(@PathVariable(value = "lang") String lang) {
     logger.info(lang);
   }
 
   // tutorial
   // https://github.com/spring-projects/spring-mvc-showcase/tree/master/src/main/java/org/springframework/samples/mvc/async
   @RequestMapping("/custom-timeout-handling")
   public @ResponseBody
   WebAsyncTask<String> callableWithCustomTimeoutHandling() {
 
     Callable<String> callable = new Callable<String>() {
       @Override
       public String call() throws Exception {
         Thread.sleep(2000);
         return "Callable result";
       }
     };
     WebAsyncTask<String> webAsyncTask = new WebAsyncTask<String>(3000, callable);
     webAsyncTask.onTimeout(new Callable<String>() {
 
       @Override
       public String call() throws Exception {
         return "time out sorry";
       }
     });
     return webAsyncTask;
   }
 
   @RequestMapping("/view")
   public Callable<String> callableWithView(final Model model) {
 
     return new Callable<String>() {
       @Override
       public String call() throws Exception {
         Thread.sleep(3000);
         model.addAttribute("foo", "bar");
         model.addAttribute("fruit", "apple");
         return "firma";
       }
     };
   }
 
   @ExceptionHandler
  @ResponseBody
   public String handleException(Exception ex) {
    return "Handled exception: " + ex.getMessage();
   }
 
   public void setMyMessage(Message myMessage) {
     this.myMessage = myMessage;
   }
 }
