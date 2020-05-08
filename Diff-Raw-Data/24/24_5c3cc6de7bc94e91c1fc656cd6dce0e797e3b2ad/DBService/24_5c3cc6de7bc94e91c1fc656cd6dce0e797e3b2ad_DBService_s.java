 package Global;
 
 import Global.MsgSystem.Abonent;
 import Global.mechanics.SingleTicket;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: MiST
  * Date: 23.11.13
  * Time: 12:15
  */
 
 public interface DBService extends Abonent, Runnable {
 
     Long USER_NOT_EXIST = -1L;
 
     public MessageSystem getMessageSystem();
 
     //Получение ID пользователя по логину и паролю
     //Если логина нет в БД или пароль не верен, возвращается USER_NOT_EXIST
     Long getUserIdByUserName(String login, String password);
 
     //создание нового пользователя
    boolean createUser(String login, String password);
 
     //удаление пользователя по User ID
     boolean deleteUser(Long userId);
 
     //проверка, что пользователь существует
     boolean checkIsUserExist(String login);
 
     //получение списка SingleTicket по зданным праметрам
     //поиск доступных рейсов
     ArrayList<SingleTicket> findSingleTickets(Map<String, String> params);
 
     ArrayList<Lot> findLots(Map<String,String> params);
 
     /**
      * Puts to database all singleTicket objects of Ticket, puts to database Ticket object (all in one transaction).
      * @param ticket
      * @param user
      * @return true if success, false otherwise(including if lot for this ticket exists)
      */
     boolean buyTicket(Ticket ticket, User user);
 
     boolean buyLot(Lot lot, User user);
 
     /**
      * Creates lot from ticket
      *
      * @param ticket
      * @param startPrice
      *@param closeDate @return true if success, false otherwise
      */
     boolean createLot(Ticket ticket, int startPrice, Date closeDate);
 
     /**
      * Rises Lot price for User
      * @param lot
      * @param user
      * @param newPrice
      * @return
      */
     boolean riseLotPrice(Lot lot, User user, int newPrice);
 }
