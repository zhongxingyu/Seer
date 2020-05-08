 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.validation.*;
 
 
 import static play.mvc.Controller.session;
 
 
 /**
  * User's short information
  *
  * @author Валерий
  */
 @Entity
 @Table(name = "users")
 public class User extends Model {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
      * Список ролей форума
      */
     public enum Role {
         /**
          * пользователь - может создавать посты и писать коментарии
          */
         USER,
         /**
          * модератор - может баать юзеров и сильно выпендриваться
          */
         MODERATOR,
         /**
          * может всё
          */
         ADMIN,
         /**
          * визитор человек который может писать посты только, но темы создавать не может *
          */
         VISITOR,
     }
 
     /**
      * Код пользователя
      */
     @Id
     public Long id;
 
     /**
      * Полное имя пользователя
      */
     @Constraints.Required
     @Constraints.MinLength(3)
     @Constraints.MaxLength(64)
     public String fullName;
 
     /**
      * Адрес почты юзера
      */
     @Constraints.Email
     @Column(unique = true)
     public String email;
 
     /**
      * Пароль пользователя
      */
     @Constraints.Required
     @Constraints.MinLength(6)
     public String password;
 
 
     /**
      * Роль пользователя
      */
     @Enumerated(EnumType.ORDINAL)
     public Role role = Role.USER;
 
     /**
      * Дата регистрации
      */
     @Column(nullable = false)
     public Date registredAt = new Date();
 
     /**
      * Количество авторизаций на сйте
      */
     @Column(nullable = false)
     public Integer visitCount = 0;
 
     /**
      * Дата и время последнего посещения сайта
      */
     public Date lastLogin;
 
     /**
      * Специальный ключ для автоматического логина пользователя
      * (галка "запомнить меня")
      */
     public String token;
 
     /**
      * Специальный ключ, используется для подтверждения восстановления пароля (часто) и
      * регистрации (в некоторы случаях)
      */
     public String serviceLink;
 
     /**
      * темы созданные пользователем
      */
     @OneToMany
     public List <Topic> topics;
     
     /**
      * сообщения написанные пользователем
      */
     @OneToMany
     public List <Post> post;
     
     /**
      * коменты пользвателя
      */
     @OneToMany
     public List <Comment> comments;
     
     public static Finder<Long, User> find = new Finder<Long, User>(Long.class, User.class);
 
     /**
      * Текущий залогиненный юзер
      */
     private static User currentUser = null;
    
 
     /**
      * Авторизация пользователя.
      *
      * @return В случае успеха возвращается объект пользователя, в противном случае null
      */
     public static User authorize(String email, String password) {
         return find.where()
                 .eq("email", email)
                 .eq("password", password)
                 .findUnique();
     }
 
     public static boolean isUniqEmail(String email) {
         return find.where().eq("email", email).findUnique() == null;
     }
 
     /**
      * Возвращает текущего залогиненного юзверя
      *
      * @return объект пользователя если всё ок, или null в случае отсутвия авторизации и др. ошибок
      */
     public static User loggedUser() {
     	Long userId = null;
         // если мы еще не загрузили текущего юзера (для экономии ресурсов)
         if(currentUser == null) {
             // загрузим его, если он есть в сессии
         	if (session("userId") != null) {
         		userId = Long.parseLong(session("userId"));
                 currentUser = (userId == null ? null : User.find.byId(userId));
         	}
         }
         return currentUser;
     }
     
     /**
      * метод делает currentUser нужно для логаута т.к на view нужно что бы 
      *  метод loggedUser() вернул null 
      */
     public static void setLoggedToNull() {
     	currentUser = null;
     }
     
     public boolean equals(Object o) {
     	return o instanceof User && ((User)o).id.equals(this.id);
     }
 
     
     public boolean isFirstVisit() {
     	return visitCount == 1;
     }
     
     
     public static boolean isMatch(String pass, String repeat) {
     	return pass.equals(repeat);
     }
     
     public static boolean wrongPass(String pass) {
     	if (pass == null) return true;
     	if (pass.length() <= 5) return true;
     	return false;
     }
     
     public void setPassword(String password) { this.password = password; }
     public String getPassword() { return password; }
     public Long getId() { return id; }
 }
