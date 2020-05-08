 package Server;
 
 import Domain.Article;
 import Domain.BulletinBoard;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.util.*;
 
 /**
  * Slave node in the BulletinBoard cluster. Replicates writes and responds to
  * quorum read requests.
  */
 public interface Slave extends BulletinBoard, Remote
 {
     void replicateWrite(Article article) throws RemoteException;
 
    Article getLocalArticle(int id) throws RemoteException;
 
     List<Article> getLocalArticles() throws RemoteException;
 }
