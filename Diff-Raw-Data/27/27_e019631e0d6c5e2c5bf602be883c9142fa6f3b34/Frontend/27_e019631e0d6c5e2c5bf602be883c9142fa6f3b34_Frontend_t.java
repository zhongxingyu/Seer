 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import static java.lang.Thread.sleep;
 
 /**
  * Created with IntelliJ IDEA.
  * User: artemlobachev
  * Date: 21.09.13
  * Time: 10:22
  * To change this template use File | Settings | File Templates.
  */
 public class Frontend extends HttpServlet implements Runnable {
 
     /**
      * Выводим количество обращений каждые 5 секунд.
      */
     @Override
     public void run() {
         try{
             while (true){
                 //System.out.println("HandleCount = " + handleCount.get() + " ThreadID=" + Thread.currentThread().getId());
                 sleep(5000);
             }
         }
         catch (InterruptedException e){
            e.printStackTrace();
         }
     }
 
 
     /**
      * Обрабатываем GET запрос.
      * @param request запрос
      * @param response ответ
      * @throws IOException TODO написать откуда может появиться!
      * @throws ServletException TODO написать откуда может появиться!
      */
     public void doGet(HttpServletRequest request,
                       HttpServletResponse response)
             throws IOException, ServletException {
 
         response.setContentType("text/html;charset=utf-8");
 
         //если пользователь пришел на страницу авторизации
         if (request.getPathInfo().equals(ADDRESS_AUTH))
         {
             //получаем sessionId
             HttpSession session = request.getSession();
             Long sessionId = (Long) session.getAttribute("sessionId");
 
             //если пользователь не авторизован
             if (sessionId == null)
             {
                 //создаем новый sessionId
                 sessionId = sessionIdCounter.getAndIncrement();
                 UserSession userSession = new UserSession();
                 //добавляем в sessionIdToUserSession
                 sessionIdToUserSession.put(sessionId, userSession);
                 //передаем sessionId пользователю
                 session.setAttribute("sessionId", sessionId);
                 //TODO: тут что-то было раньше :)
                 response.getWriter().println(PageGenerator.getPage("auth.tml", new HashMap<String, Object>()));
             }
             //пользователь авторизован (не факт, может он просто кукисы подделал)
             else
             {
                if (sessionIdToUserSession.get(sessionId).userId != -1L)
                {
                    Map<String, Object> pageVariables = new HashMap<>();
                    pageVariables.put("UserId", sessionIdToUserSession.get(sessionId).userId);
                    pageVariables.put("UserName", sessionIdToUserSession.get(sessionId).userName);
                    response.getWriter().println(PageGenerator.getPage("test.tml", pageVariables));
                }
                else
                    response.getWriter().println("Такого пользователя нету");
             }
 
             response.setStatus(HttpServletResponse.SC_OK);
         }
         //пользователь пришел на необрабатываемый адрес
         else{
             //TODO сделать красивую статическую страничку 404
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
     }
 
     /**
      * Обрабатываем POST запрос.
      * @param request
      * @param response
      * @throws IOException TODO написать откуда может появиться!
      * @throws ServletException  TODO написать откуда может появиться!
      */
     public void doPost(HttpServletRequest request,
                        HttpServletResponse response)
             throws IOException, ServletException
     {
 
         //FIXME: можно обратиться к странице не обращаеясь предварительно через GET и все упадет ^^
         //получаем sessionId
         HttpSession session = request.getSession();
         Long sessionId = (Long) session.getAttribute("sessionId");
 
         response.setContentType("text/html;charset=utf-8");
 
         //пользователь пытается авторизоваться
         if (request.getPathInfo().equals(ADDRESS_AUTH))
         {
             String userName = (String) request.getParameter("name");
 
             if (sessionIdToUserSession.containsKey(sessionId))
             {
                 //FIXME: это должно работать не так, сначало запрос у AccountService, а затем отдача страницы
 
                 sessionIdToUserSession.get(sessionId).userName = userName;
 
                 //делаем запрос у AccountService и зависаем
                 try {
                     sessionIdToUserSession.get(sessionId).userId = accountService.getUserIdByUserName(userName);
                     System.out.println("HandleCount = " + sessionIdToUserSession.get(sessionId).userId);
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
                 response.getWriter().println(PageGenerator.getPage("wait.tml", new HashMap<String, Object>()));
             }
             //пользователя не существует
             else
             {
                 //FIXME: а что делать иначе, я не знаю.
             }
 
             response.setStatus(HttpServletResponse.SC_OK);
         }
         //обращение к несуществующему адресу
         else{
             response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         }
 
     }
 
     //FIXME: определится со стилем, либо переменные в начале файла, либо в конце
     protected static String ADDRESS_AUTH = "/auth";
 
     private Map<String, Long> users;
 
     private AccountService accountService = new AccountService();
 
     private AtomicLong sessionIdCounter = new AtomicLong();
 
     //FIXME: разобраться как это правильно делается в говноJave
    private class UserSession
     {
         public String userName;
         public Long userId;
     }
     private Map<Long, UserSession> sessionIdToUserSession = new HashMap<>();
 }
