 package Global.WebPages;
 
 import Global.AccountService;
 import Global.Imps.UserSession;
 import Global.SessionService;
 import Global.WebPage;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kislenko Maksim
  * Date: 12.10.13
  * Time: 11:02
  */
 
 /**
  * Страница авторизации пользователя. Для своей работы требует ссылку на класс SessionServiceImp.
  * Наследует абстрактную веб-страницу.
  */
 
 public class AuthPage extends WebPageImp implements WebPage {
     /**
      * Подзадачи обработки запроса на авторизацию.
      */
     private static final int FIRST_LOOK = 0;    // Первый вход на сайт
     private static final int ENTRY = 1;         // Вход с существующей сессией
     private static final int CHECK_AUTH = 2;    // Отправка данных авторизации
 
     // Осведомленность. Используется для выборки данных пользователь--сессия
     private final SessionService sessionService;
 
     // Параметры-связки между анализом и исполнением запроса
     private HttpSession session;
     private Long sessionId;
     private UserSession userSession = null;
     private String userName;
     private Map<String, Object> pageVariables;
 
     // конструктор
     public AuthPage(SessionService sessionService) {
         super();
         this.sessionService = sessionService;
     }
 
     /**
      * Анализ GET запроса и выборка параметров к его обработке.
      * @param request объект исследуемого запроса
      * @return необходимый вариант обработки
      */
     @Override
     protected int analyzeRequestGET(HttpServletRequest request) {
         this.session = request.getSession();
         this.sessionId = (Long) this.session.getAttribute("sessionId");
 
         if (this.sessionId == null) {
             // первый заход пользователя на сайт
             return FIRST_LOOK;
         } else {
             this.userSession = this.sessionService.getUserInfo(this.sessionId);
             return ENTRY;
         }
     }
 
     /**
      * Анализ GET запроса и выборка параметров к его обработке.
      * @param request объект исследуемого запроса
      * @return необходимый вариант обработки
      */
     @Override
     protected int analyzeRequestPOST(HttpServletRequest request) {
         // Пользователь послал свои данные на авторизацию
         HttpSession session = request.getSession();
         this.userName = request.getParameter("name");
         this.sessionId = (Long) session.getAttribute("sessionId");
 
         return CHECK_AUTH;
     }
 
     /**
      * Обработка первого входа.
      * @return Будет возвращена страница с формочкой авторизации
      */
 
     @CaseHandler(routine = FIRST_LOOK, reqType = RequestType.GET)
     public String handleFirstLook() {
         //если это первый заход пользователя на сайт, присваиваем ему уникальный sessionId
         this.sessionId = this.sessionService.getNewSessionId();
 
         //передаем новый sessionId пользователю
         this.session.setAttribute("sessionId", this.sessionId);
 
         // Пользователь не авторизован
        this.pageVariables = dataToKey(new String[] {"PageTitle", "Location"},
                "Authetication page","Enter your account name");
         return generatePage("auth.tml", this.pageVariables);
     }
 
     /**
      * Обработчик попытки входа на сайт с уже существующей сессией.
      * Внутри опрос userSession о готовности.
      * @return либо главная страница, либо ошибка, либо ожидание
      */
 
     @CaseHandler(routine = ENTRY, reqType = RequestType.GET)
     public String handleEntry() {
         if (this.userSession != null) {
             //ожидаем пока AccountService вернет данные
             if (this.userSession.isComplete()) {
                 //проверяем, что пользователь существует
                 if (! this.userSession.getUserId().equals(AccountService.USER_NOT_EXIST)) {
                     System.out.println("Session Id: " + this.sessionId);
 
                     // Заполняем контекст
                    this.pageVariables = dataToKey(new String[] {"PageTitle", "Location","UserId", "UserName", "Time", "Session"},
                            "Shtaa?","Your data",this.userSession.getUserId(), this.userSession.getName(), getTime(), this.sessionId);
                    return generatePage("temp.tml", this.pageVariables);
 
                 } else {
                     this.sessionService.closeSession(this.sessionId); //удаляем текущую сессию
                     return "Ошибка. Такого пользователя нет";
                 }
             } else {
                 //просим пользователя подождать
                 return generatePage("wait.tml", new HashMap<String, Object>());
                 //TODO: отправлять тут код ошибки с пустым телом или JSON, сообщающий, что идет поиск,
                 //TODO: сделать на клиенте циклический опрос ограниченное кол-во раз, если ответа нет - сообщение
                 //TODO: пользователю об ошибке/
             }
         } else {
            this.pageVariables = dataToKey(new String[] {"PageTitle", "Location"},
                     "Authetication page","Enter your account name");
             return generatePage("auth.tml", this.pageVariables);
         }
     }
 
     /**
      * Проверка введенных пользователем данных.
      * @return страница ожидания
      */
 
     @CaseHandler(routine = CHECK_AUTH, reqType = RequestType.POST)
     public String handleCheckAuth() {
         // Отправляем sessionId для построения нового объекта UserSession
         //TODO: здесь надо проверить пароль
         this.sessionService.createUserSession(this.sessionId, this.userName);
 
         // Отдаем страницу ожидания.
         return generatePage("wait.tml");
     }
 }
