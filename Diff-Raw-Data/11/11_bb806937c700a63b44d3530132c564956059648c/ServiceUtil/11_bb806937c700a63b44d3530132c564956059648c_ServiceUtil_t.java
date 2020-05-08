 /**
  * 
  */
 package org.mklab.taskit.server.domain;
 
 import org.mklab.taskit.server.auth.InvocationEntrance;
 import org.mklab.taskit.server.auth.Invoker;
 import org.mklab.taskit.shared.UserType;
 
import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
 
 import de.novanic.eventservice.client.event.Event;
 import de.novanic.eventservice.client.event.domain.Domain;
 import de.novanic.eventservice.service.registry.EventRegistry;
 import de.novanic.eventservice.service.registry.EventRegistryFactory;
 import de.novanic.eventservice.service.registry.user.UserInfo;
 import de.novanic.eventservice.service.registry.user.UserManager;
 import de.novanic.eventservice.service.registry.user.UserManagerFactory;
 
 
 /**
  * サービスで共通に利用する処理を定義したユーティリティクラスです。
  * <p>
  * テストのために、実装を切り替えられるようにしています。
  * 
  * @author ishikura
  */
 public class ServiceUtil {
 
  static Logger logger = Logger.getLogger(ServiceUtil.class);
 
   static final String IS_LOGGED_IN_KEY = "loggedIn"; //$NON-NLS-1$
   /** セッション中のユーザーオブジェクトのキーです。 */
   public static final String USER_KEY = "user"; //$NON-NLS-1$
   private static ServiceUtilImplementation impl = new DefaultServiceUtilImplementation();
 
   static ServiceUtilImplementation getImplementation() {
     return impl;
   }
 
   static void setImplementation(ServiceUtilImplementation implementation) {
     ServiceUtil.impl = implementation;
   }
 
   /**
    * 与えられたエンティティのクラスのテーブルを参照し、IDが一致するエンティティを取得します。
    * 
    * @param clazz エンティティのクラス
    * @param id エンティティのID
    * @return エンティティ
    */
   public static <T, I> T findEntity(Class<? extends T> clazz, I id) {
     final EntityManager em = EMF.get().createEntityManager();
     try {
       return em.find(clazz, id);
     } catch (Throwable e) {
       logger.error("Failed to find entity:" + clazz, e); //$NON-NLS-1$
       throw new RuntimeException(e);
     } finally {
       em.close();
     }
   }
 
   /**
    * 与えられたテーブル名のエンティティをすべて取得します。
    * 
    * @param tableName テーブル名
    * @return すべてのエンティティ
    */
   @SuppressWarnings({"nls", "unchecked"})
   public static <T> List<T> getAllEntities(String tableName) {
     final EntityManager em = EMF.get().createEntityManager();
     final Query q = em.createQuery("select o from " + tableName + " o");
     try {
       return q.getResultList();
     } catch (Throwable e) {
       logger.error("Failed to get all entities.", e); //$NON-NLS-1$
       throw new RuntimeException(e);
     } finally {
       em.close();
     }
   }
 
   /**
    * 与えられたユーザーでログインします。
    * <p>
    * すでにログインしていた場合は一旦ログアウトしてから改めてログインします。
    * 
    * @param user ログインさせるユーザー
    */
   public static void login(User user) {
     if (isLoggedIn()) {
       logout();
     }
     impl.login(user);
   }
 
   /**
    * ログアウトします。
    * <p>
    * ログインしていない状態であれば何も行いません。
    */
   public static void logout() {
     impl.logout();
   }
 
   /**
    * ログインユーザーを取得します。
    * 
    * @return ログインユーザー。ログインしていない場合はnull
    */
   public static User getLoginUser() {
     return impl.getLoginUser();
   }
 
   /**
    * ログインしているかどうか調べます。
    * 
    * @return ログインしているかどうか
    */
   public static boolean isLoggedIn() {
     return impl.isLoggedIn();
   }
 
   /**
    * 特定のクライアントに対しイベントを配信します。
    * <p>
    * 配信先ユーザーがアクティブではなかった場合には何も行いません。
    * 
    * @param domain ドメイン
    * @param event イベント
    * @param accountId TASKitにおけるアカウントID
    * @throws IllegalArgumentException 与えられたアカウントIDが存在しない場合
    */
   public static void fireEvent(Domain domain, Event event, String accountId) {
     User user = User.getUserByAccountId(accountId);
     if (user == null) throw new IllegalArgumentException("No such user : " + accountId); //$NON-NLS-1$
     fireEvent(domain, event, user);
   }
 
   /**
    * 特定のクライアントに対しイベントを配信します。
    * <p>
    * 配信先ユーザーがアクティブではなかった場合には何も行いません。
    * 
    * @param domain ドメイン
    * @param event イベント
    * @param user ユーザー
    */
   public static void fireEvent(Domain domain, Event event, User user) {
     impl.fireEvent(domain, event, user);
   }
 
   /**
    * domainに興味のあるクライアントに対しイベントを配信します。
    * 
    * @param domain ドメイン
    * @param event イベント
    */
   public static void fireEvent(Domain domain, Event event) {
     impl.fireEvent(domain, event);
   }
 
   /**
    * gwteventserviceで利用するクライアントIDを登録します。
    * 
    * @param clientId クライアントID
    * @param user ユーザー
    */
   public static void registerClient(String clientId, User user) {
     if (user == null) {
       throw new IllegalStateException("Not logged in."); //$NON-NLS-1$
     }
     impl.registerClient(clientId, user);
   }
 
   /**
    * クライアントIDの登録を抹消します。
    * 
    * @param clientId クライアントID
    */
   public static void unregisterClient(String clientId) {
     impl.unregisterClient(clientId);
   }
 
   static interface ServiceUtilImplementation {
 
     boolean isLoggedIn();
 
     User getLoginUser();
 
     void login(User user);
 
     void logout();
 
     void registerClient(String clientId, User user);
 
     void unregisterClient(String clientId);
 
     /**
      * 特定のクppppライアントに対しイベントを配信します。
      * <p>
      * 配信先ユーザーがアクティブではなかった場合には何も行いません。
      * 
      * @param domain ドメイン
      * @param event イベント
      * @param user ユーザー
      */
     void fireEvent(Domain domain, Event event, User user);
 
     /**
      * domainに興味のあるクライアントに対しイベントを配信します。
      * 
      * @param domain ドメイン
      * @param event イベント
      */
     void fireEvent(Domain domain, Event event);
 
   }
 
   static class DefaultServiceUtilImplementation implements ServiceUtilImplementation {
 
     /** gwteventserviceで利用するクライアントIDからユーザーへのマップです。 */
     private Map<String, User> clientIdToUser = new HashMap<String, User>();
 
     @Override
     public void login(User user) {
       final HttpSession session = getSession();
       session.setAttribute(IS_LOGGED_IN_KEY, Boolean.TRUE);
       session.setAttribute(USER_KEY, user);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void logout() {
       HttpSession session = getSession();
       if (session != null) {
         session.invalidate();
       }
     }
 
     @Override
     public User getLoginUser() {
       final HttpSession session = getSession();
       if (session == null) return null;
 
       final User user = (User)session.getAttribute(USER_KEY);
       return user;
     }
 
     @Override
     public boolean isLoggedIn() {
       final HttpSession session = getSession();
       if (session == null) return false;
 
       final Boolean loggedIn = (Boolean)session.getAttribute(IS_LOGGED_IN_KEY);
       return loggedIn == null ? false : loggedIn.booleanValue();
     }
 
     private static final HttpSession getSession() {
       return getThreadLocalRequest().getSession();
     }
 
     private static HttpServletRequest getThreadLocalRequest() {
       HttpServletRequest req = RequestFactoryServlet.getThreadLocalRequest();
       if (req == null) throw new NullPointerException("Thread local request was null."); //$NON-NLS-1$
       return req;
     }
 
     /**
      * 与えられたユーザーに対応するGWTEventServiceにおけるクライアントIDを取得します。
      * 
      * @param user ユーザー
      * @return GWTEventServiceにおけるクライアントID
      */
     private String getClientIdOfGwtEventService(User user) {
       for (Entry<String, User> entry : this.clientIdToUser.entrySet()) {
         if (entry.getValue().getAccount().getId().equals(user.getAccount().getId()) == false) continue;
         return entry.getKey();
       }
       return null;
     }
 
     /**
      * 特定のクライアントに対しイベントを配信します。
      * <p>
      * 配信先ユーザーがアクティブではなかった場合には何も行いません。
      * 
      * @param domain ドメイン
      * @param event イベント
      * @param user ユーザー
      */
     @Override
     public void fireEvent(Domain domain, Event event, User user) {
       final Invoker invoker = event.getClass().getAnnotation(Invoker.class);
       if (invoker == null) {
         System.err.println("Invoker annotation was not set to " + event.getClass()); //$NON-NLS-1$
         throw new IllegalStateException("Invoker annotation was not set to " + event.getClass()); //$NON-NLS-1$
       }
 
       final EventRegistry registory = EventRegistryFactory.getInstance().getEventRegistry();
       final UserManager userManager = UserManagerFactory.getInstance().getUserManager();
       final String clientId = getClientIdOfGwtEventService(user);
       if (clientId == null) return;
 
       final Class<? extends InvocationEntrance> entranceClass = invoker.entrance();
       if (entranceClass == InvocationEntrance.class) {
         for (final UserType userType : invoker.value()) {
           if (user.getType() == userType) {
             final UserInfo userInGwtEventService = userManager.getUser(clientId);
            if (userInGwtEventService == null) {
              logger.error(MessageFormat.format("User {0} is not managed.", clientId)); //$NON-NLS-1$
            } else {
              userInGwtEventService.addEvent(domain, event);
            }
           }
         }
       } else {
         final InvocationEntrance entrance;
         try {
           entrance = entranceClass.newInstance();
         } catch (Throwable e) {
           throw new RuntimeException(e);
         }
         if (entrance.accept(user)) {
           registory.addEventUserSpecific(clientId, event);
         }
       }
     }
 
     /**
      * domainに興味のあるクライアントに対しイベントを配信します。
      * 
      * @param domain ドメイン
      * @param event イベント
      */
     @Override
     public void fireEvent(Domain domain, Event event) {
       final Invoker invoker = event.getClass().getAnnotation(Invoker.class);
       if (invoker == null) {
         System.err.println("Invoker annotation was not set to " + event.getClass()); //$NON-NLS-1$
         throw new IllegalStateException("Invoker annotation was not set to " + event.getClass()); //$NON-NLS-1$
       }
 
       final EventRegistry registory = EventRegistryFactory.getInstance().getEventRegistry();
       final UserManager userManager = UserManagerFactory.getInstance().getUserManager();
 
       final Class<? extends InvocationEntrance> entranceClass = invoker.entrance();
       if (entranceClass == InvocationEntrance.class) {
         for (final String userId : registory.getRegisteredUserIds(domain)) {
           final User eventTargetUser = this.clientIdToUser.get(userId);
           if (eventTargetUser == null) continue;
 
           for (final UserType userType : invoker.value()) {
             if (eventTargetUser.getType() == userType) {
               final UserInfo userInGwtEventService = userManager.getUser(userId);
               userInGwtEventService.addEvent(domain, event);
             }
           }
         }
       } else {
         final InvocationEntrance entrance;
         try {
           entrance = entranceClass.newInstance();
         } catch (Throwable e) {
           throw new RuntimeException(e);
         }
         for (final String userId : registory.getRegisteredUserIds(domain)) {
           final User eventTargetUser = this.clientIdToUser.get(userId);
           if (eventTargetUser == null) continue;
 
           if (entrance.accept(eventTargetUser)) {
             registory.addEventUserSpecific(userId, event);
           }
         }
       }
     }
 
     /**
      * gwteventserviceで利用するクライアントIDを登録します。
      * 
      * @param clientId クライアントID
      * @param user ユーザー
      */
     @Override
     public void registerClient(String clientId, User user) {
       if (user == null) {
         throw new IllegalStateException("Not logged in."); //$NON-NLS-1$
       }
       this.clientIdToUser.put(clientId, user);
     }
 
     /**
      * クライアントIDの登録を抹消します。
      * 
      * @param clientId クライアントID
      */
     @Override
     public void unregisterClient(String clientId) {
       this.clientIdToUser.remove(clientId);
     }
 
   }
 }
