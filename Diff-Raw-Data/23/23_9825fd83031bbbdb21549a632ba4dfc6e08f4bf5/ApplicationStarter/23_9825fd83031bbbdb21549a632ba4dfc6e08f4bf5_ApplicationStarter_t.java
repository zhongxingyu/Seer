 package org.test.bugtracker.starter;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.test.bugtracker.bo.BugBO;
import org.test.bugtracker.bo.CommentBO;
 import org.test.bugtracker.bo.UserBO;
import org.test.bugtracker.impl.model.BugImpl;
import org.test.bugtracker.impl.model.CommentImpl;
 import org.test.bugtracker.impl.model.UserImpl;
import org.test.bugtracker.model.Bug;
import org.test.bugtracker.model.Comment;
 import org.test.bugtracker.model.User;
 
 public class ApplicationStarter {
     private ApplicationContext context;
 
     public ApplicationStarter(ApplicationContext context) {
         this.context = context;
     }
 
     public void start() {
         UserBO userBO = context.getBean(UserBO.class);
         User user = new UserImpl();
         user.setLogin("login");
         user.setPass("pass");
         userBO.save(user);
        Bug bug = new BugImpl();
        bug.setAuthor(user);
        bug.setTitle("title");
        bug.setMessage("message");
        BugBO bugBO = context.getBean(BugBO.class);
        bugBO.save(bug);
        Comment comment = new CommentImpl();
        comment.setAuthor(user);
        comment.setBug(bug);
        comment.setMessage("Commnet message");
        CommentBO commentBO = context.getBean(CommentBO.class);
        commentBO.save(comment);
     }
 
     public static void main(String[] args) {
         new ApplicationStarter(new ClassPathXmlApplicationContext(
                 "spring/context.xml")).start();
     }
 }
