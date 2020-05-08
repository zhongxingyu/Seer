 package com.adde.webbapp_model;
 
 import com.adde.webbapp_model_dao.CommentDAO;
 import com.adde.webbapp_model_dao.TodoPostDAO;
 import com.adde.webbapp_model_dao.WallPostDAO;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import org.junit.Ignore;
 
 public class PostsTest {
     private Person person1;
     private Person person2;
     private Comment comment1;
     private Comment comment2;
     private WallPost wallPost1;
     private TodoPost todoPost1;
     private GregorianCalendar time1;
     private Project project1;
     private ProjectPlatform platform;
     
     private static final String STR1 = "Content";
     private static final String STR2 = "EditedContent";
     
     @Ignore
     @Before
     public void setup(){
         person1 = new Person("person1", "email1");
         person2 = new Person("person2", "email2");
         time1 = new GregorianCalendar();
         project1 = new Project();
         todoPost1 = new TodoPost(project1, person1, STR1);
         TodoPostDAO.newInstance().add(todoPost1);
         wallPost1 = new WallPost(project1, person1, STR1);
         WallPostDAO.newInstance().add(wallPost1);
         comment1 = new Comment(wallPost1, person1, STR1);
         comment2 = new Comment(wallPost1, person2, STR2);
         CommentDAO.newInstance().add(comment1);
         CommentDAO.newInstance().add(comment2);
         platform = ProjectPlatformFactory.getProjectPlatform(ProjectPlatform.PU);
         platform.addUser(person1);
         platform.addUser(person2);
         platform.addProject(project1);
     }
     
     @Ignore
     @Test
     public void CommentTest(){
         //equals, storage in database
         List<Comment> commentsTmp = wallPost1.getComments();
         assertFalse(commentsTmp == null);
         assertTrue(commentsTmp.size() == 2);
         Comment commentTmp = commentsTmp.get(0);
         assertTrue(commentTmp.equals(comment1) || commentTmp.equals(comment2));
         assertTrue(comment1.equals(comment1));
         
         //toString (used later on)
         String tmp1 = comment1.toString();
         
         //calendar init values
         GregorianCalendar cal1 = comment1.getDateCreated();
         GregorianCalendar cal2 = comment2.getDateModified();
         assertTrue(cal1.equals(cal2));
         
         //calendar update on content edit
         //wait 1ms so calendar value isn't the same...
         try{
             Thread.sleep(1);
         } catch(InterruptedException e){
             System.out.println("PostsTest.CommentTest: "
                     + "Interrupted on Thread.sleep");
         }
         comment1.setMsg(STR2);
         assertFalse(cal1.equals(comment1.getDateModified()));
         assertFalse(cal2.equals(comment1.getDateModified()));
         assertTrue(cal1.equals(comment1.getDateCreated()));
         
         //content check
         assertTrue(comment1.getMsg().equals(STR2));
         assertFalse(tmp1.equals(comment1.toString()));
         assertTrue(comment1.getAuthor().equals(person1));
     }
     
     @Ignore
     @Test
     public void TodoPostTest(){
         //storage in database
         List<TodoPost> tps = TodoPostDAO.newInstance()
                 .getTodoPostsByProject(project1);
         assertFalse(tps == null);
         assertTrue(tps.size() == 1);
         assertTrue(tps.get(0).equals(todoPost1));
         
         //initial values
         assertTrue(todoPost1.getAssignedTo() == null);
         assertTrue(todoPost1.getDeadline() == null);
         assertTrue(todoPost1.getPriority() == null);
         
         //set priority
         todoPost1.setPriority(TodoPost.Priority.LOW);
         assertTrue(todoPost1.getPriority().equals(TodoPost.Priority.LOW));
         
         //add with list
         List<Person> list = new LinkedList<>();
         list.add(person1);
         list.add(person2);
         System.out.println("\n\n" + list + "\n\n");
         todoPost1.assignTo(list);
         assertFalse(todoPost1.getAssignedTo() == null);
         assertTrue(todoPost1.getAssignedTo().size() == 2);
         
         //unassign
         todoPost1.unassign(person2);
         assertTrue(todoPost1.getAssignedTo().size() == 1);
         
         //clear assigned to
         todoPost1.clearAssignedTo();
         assertTrue(todoPost1.getAssignedTo() == null);
         
         //duplicates
         list.add(person1);
         todoPost1.assignTo(list);
         assertTrue(todoPost1.getAssignedTo().size() == 2);
         todoPost1.assignTo(person2);
         assertTrue(todoPost1.getAssignedTo().size() == 2);
         
         //deadline
         todoPost1.setDeadline(time1);
         assertTrue(todoPost1.getDeadline().equals(time1));
         
         //context
         assertTrue(todoPost1.getContext().equals(project1));
     }
     
     @Ignore
     @Test
     public void WallPostTest(){
         //database storage
         List<WallPost> wps = WallPostDAO.newInstance().getWallPostByProject(project1);
         assertFalse(wps == null);
         assertTrue(wps.size() == 1);
         assertTrue(wps.get(0).equals(wallPost1));
         
         //init value
         assertTrue(wallPost1.getComments().isEmpty());
         
         //add methods
         wallPost1.addComment(person1, STR1);
         wallPost1.addComment(comment1);
         wallPost1.addComment(person2, STR2);
         assertTrue(wallPost1.getComments().size() == 3);
         
         //remove methods
         wallPost1.removeCommentsByUser(person1);
         assertTrue(wallPost1.getComments().size() == 1);
         wallPost1.addComment(person2, STR2);
         wallPost1.removeComment(comment1);
         assertTrue(wallPost1.getComments().size() == 1);
         
         //duplicates (same id)
         wallPost1.addComment(comment2);
         wallPost1.addComment(comment2);
         assertTrue(wallPost1.getComments().size() == 2);
     }
 }
