 package edu.agh.lroza.synchronize;
 
 import edu.agh.lroza.common.*;
 import scala.Either;
 import scala.Option;
 import scala.collection.JavaConversions;
 import scala.collection.Set;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.UUID;
 
 import static edu.agh.lroza.common.UtilsJ.*;
 
 public class SynchronizedServerJava implements NoticeBoardServer {
     private java.util.Set<UUID> loggedUsers = Collections.synchronizedSet(new HashSet<UUID>());
     private java.util.Map<Id, Notice> notices = Collections.synchronizedMap(new HashMap<Id, Notice>());
 
     private static class TitleId implements Id {
         private final String title;
 
         public TitleId(String title) {
             this.title = title;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof TitleId) {
                 return ((TitleId) obj).title.equals(title);
             } else {
                 return false;
             }
         }
 
         @Override
         public int hashCode() {
             return title.hashCode();
         }
 
         @Override
         public String toString() {
             return title;
         }
     }
 
     public Either<Problem, UUID> login(String username, String password) {
         if (username.equals(password)) {
             UUID token = UUID.randomUUID();
             loggedUsers.add(token);
             return UtilsJ.right(token);
         } else {
             return left(newProblem("Wrong password"));
         }
     }
 
     public Option<Problem> logout(UUID token) {
         if (loggedUsers.remove(token)) {
             return Option.empty();
         } else {
             return UtilsJ.some(newProblem("Invalid token"));
         }
     }
 
     public Either<Problem, Set<Id>> listNoticesIds(UUID token) {
         if (loggedUsers.contains(token)) {
            return UtilsJ.right((Set<Id>) JavaConversions.asScalaSet(notices.keySet()));
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Either<Problem, Id> addNotice(UUID token, String title, String message) {
         if (loggedUsers.contains(token)) {
             Id id = new TitleId(title);
             synchronized (notices) {
                 if (notices.get(id) == null) {
                     notices.put(id, new NoticeJ(title, message));
                     return UtilsJ.right(id);
                 } else {
                     return left(newProblem("Topic with title '" + title + "' already exists"));
                 }
             }
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Either<Problem, Notice> getNotice(UUID token, Id id) {
         if (loggedUsers.contains(token)) {
             Notice notice = notices.get(id);
             if (notice == null) {
                 return left(newProblem("There is no such notice '" + id + "'"));
             } else {
                 return UtilsJ.right(notice);
             }
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Either<Problem, Id> updateNotice(UUID token, Id id, String title, String message) {
         if (loggedUsers.contains(token)) {
             synchronized (notices) {
                 if (!notices.containsKey(id)) {
                     return left(newProblem("There is no such notice '" + id + "'"));
                 } else {
                     Id newId = new TitleId(title);
                     if (notices.containsKey(newId)) {
                         return left(newProblem("Topic with title '" + title + "' already exists"));
                     } else {
                         notices.remove(id);
                         notices.put(newId, new NoticeJ(title, message));
                         return right(newId);
                     }
                 }
             }
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Option<Problem> deleteNotice(UUID token, Id id) {
         if (loggedUsers.contains(token)) {
             if (notices.remove(id) == null) {
                 return some(newProblem("There is no such notice '" + id + "'"));
             } else {
                 return none();
             }
         } else {
             return some(newProblem("Invalid token"));
         }
     }
 }
