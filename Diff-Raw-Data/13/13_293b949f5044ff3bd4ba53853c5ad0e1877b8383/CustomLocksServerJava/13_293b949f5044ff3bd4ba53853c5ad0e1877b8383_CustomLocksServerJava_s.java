 package edu.agh.lroza.locks;
 
 import edu.agh.lroza.common.*;
 import scala.Either;
 import scala.Option;
 import scala.collection.JavaConversions;
 import scala.collection.Set;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.UUID;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import static edu.agh.lroza.common.UtilsJ.*;
 
 public class CustomLocksServerJava implements NoticeBoardServer {
     private java.util.Set<UUID> loggedUsers = new HashSet<UUID>();
     private java.util.Map<Id, Notice> notices = new HashMap<Id, Notice>();
 
     private ReadWriteLock loggedUsersLock = new ReentrantReadWriteLock();
     private ReadWriteLock noticesLock = new ReentrantReadWriteLock();
 
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
 
     private boolean isValid(UUID token) {
         loggedUsersLock.readLock().lock();
         try {
             return loggedUsers.contains(token);
         } finally {
             loggedUsersLock.readLock().unlock();
         }
     }
 
     public Either<Problem, UUID> login(String username, String password) {
         if (username.equals(password)) {
             UUID token = UUID.randomUUID();
             loggedUsersLock.writeLock().lock();
             try {
                 loggedUsers.add(token);
             } finally {
                 loggedUsersLock.writeLock().unlock();
             }
             return UtilsJ.right(token);
         } else {
             return left(newProblem("Wrong password"));
         }
     }
 
     public Option<Problem> logout(UUID token) {
         loggedUsersLock.writeLock().lock();
         try {
             if (loggedUsers.remove(token)) {
                 return Option.empty();
             } else {
                 return UtilsJ.some(newProblem("Invalid token"));
             }
         } finally {
             loggedUsersLock.writeLock().unlock();
         }
     }
 
     public Either<Problem, Set<Id>> listNoticesIds(UUID token) {
         if (isValid(token)) {
             noticesLock.readLock().lock();
             try {
                return UtilsJ.right((Set<Id>) JavaConversions.asScalaSet(notices.keySet()));
             } finally {
                 noticesLock.readLock().unlock();
             }
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Either<Problem, Id> addNotice(UUID token, String title, String message) {
         if (isValid(token)) {
             Id id = new TitleId(title);
             noticesLock.writeLock().lock();
             try {
                 if (notices.get(id) == null) {
                     notices.put(id, new NoticeJ(title, message));
                     return UtilsJ.right(id);
                 } else {
                     return left(newProblem("Topic with title '" + title + "' already exists"));
                 }
             } finally {
                 noticesLock.writeLock().unlock();
             }
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Either<Problem, Notice> getNotice(UUID token, Id id) {
         if (isValid(token)) {
             Notice notice;
             noticesLock.readLock().lock();
             try {
                 notice = notices.get(id);
             } finally {
                 noticesLock.readLock().unlock();
             }
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
         if (isValid(token)) {
             noticesLock.writeLock().lock();
             try {
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
             } finally {
                 noticesLock.writeLock().unlock();
             }
         } else {
             return left(newProblem("Invalid token"));
         }
     }
 
     @Override
     public Option<Problem> deleteNotice(UUID token, Id id) {
         if (isValid(token)) {
             Notice notice;
             noticesLock.writeLock().lock();
             try {
                 notice = notices.remove(id);
             } finally {
                 noticesLock.writeLock().unlock();
             }
             if (notice == null) {
                 return some(newProblem("There is no such notice '" + id + "'"));
             } else {
                 return none();
             }
         } else {
             return some(newProblem("Invalid token"));
         }
     }
 }
