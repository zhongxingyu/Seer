 /**
  * @author Ahn Hyeok Jun
  */
 
 package controllers;
 
 import java.io.File;
 
 import models.*;
 import models.enumeration.*;
 import play.data.Form;
 import play.mvc.*;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Http.Request;
 import utils.*;
 import views.html.board.*;
 
 public class BoardApp extends Controller {
     
     //TODO 이 클래스는 원래 따로 존재해야 함.
     public static class SearchCondition{
         public final static String ORDERING_KEY_ID = "id";
         public final static String ORDERING_KEY_TITLE = "title";
         public final static String ORDERING_KEY_AGE = "date";
         
         public SearchCondition() {
             this.order = Direction.DESC.direction();
             this.key = ORDERING_KEY_ID;
             this.filter = "";
             this.pageNum = 1;
         }
 
         public String order;
         public String key;
         public String filter;
         public int pageNum;
     }
     
 
     public static Result posts(String userName, String projectName) {
 
         Form<SearchCondition> postParamForm = new Form<SearchCondition>(SearchCondition.class);
         SearchCondition postSearchCondition = postParamForm.bindFromRequest().get();
         Project project = ProjectApp.getProject(userName, projectName);
 
         return ok(postList.render(
                 "menu.board",
                 project,
                 Post.findOnePage(project.owner, project.name, postSearchCondition.pageNum,
                         Direction.getValue(postSearchCondition.order), postSearchCondition.key), postSearchCondition));
     }
 
     public static Result newPost(String userName, String projectName) {
         Project project = ProjectApp.getProject(userName, projectName);
         return ok(newPost.render("board.post.new", new Form<Post>(Post.class), project));
     }
 
     public static Result savePost(String userName, String projectName) {
         Form<Post> postForm = new Form<Post>(Post.class).bindFromRequest();
         Project project = ProjectApp.getProject(userName, projectName);
         if (postForm.hasErrors()) {
             flash(Constants.WARNING, "board.post.empty");
             
             return redirect(routes.BoardApp.newPost(userName, projectName));
         } else {
             Post post = postForm.get();
             post.authorId = UserApp.currentUser().id;
             post.authorName = UserApp.currentUser().name;
             post.commentCount = 0;
             post.filePath = saveFile(request());
             post.project = project;
             Post.write(post);
         }
 
         return redirect(routes.BoardApp.posts(project.owner, project.name));
     }
 
     public static Result post(String userName, String projectName, Long postId) {
         Post post = Post.findById(postId);
         Project project = ProjectApp.getProject(userName, projectName);
         if (post == null) {
             flash(Constants.WARNING, "board.post.notExist");
             return redirect(routes.BoardApp.posts(project.owner, project.name));
         } else {
             Form<Comment> commentForm = new Form<Comment>(Comment.class);
             return ok(views.html.board.post.render(post, commentForm, project));
         }
     }
 
     public static Result saveComment(String userName, String projectName, Long postId) {
         Form<Comment> commentForm = new Form<Comment>(Comment.class).bindFromRequest();
 
         Project project = ProjectApp.getProject(userName, projectName);
         if (commentForm.hasErrors()) {
             flash(Constants.WARNING, "board.comment.empty");
             return redirect(routes.BoardApp.post(project.owner, project.name, postId));
         } else {
             Comment comment = commentForm.get();
             comment.post = Post.findById(postId);
             comment.authorId = UserApp.currentUser().id;
             comment.authorName = UserApp.currentUser().name;
             comment.filePath = saveFile(request());
 
             Comment.write(comment);
             Post.countUpCommentCounter(postId);
 
             return redirect(routes.BoardApp.post(project.owner, project.name, postId));
         }
     }
 
     public static Result deletePost(String userName, String projectName, Long postId) {
         Project project = ProjectApp.getProject(userName, projectName);
         Post.delete(postId);
         return redirect(routes.BoardApp.posts(project.owner, project.name));
     }
 
     public static Result editPost(String userName, String projectName, Long postId) {
         Post existPost = Post.findById(postId);
         Form<Post> editForm = new Form<Post>(Post.class).fill(existPost);
         Project project = ProjectApp.getProject(userName, projectName);
 
         
        if (AccessControl.isAllowed(UserApp.currentUser().id, project.id, Resource.BOARD_POST, Operation.EDIT, postId)) {
             return ok(editPost.render("board.post.modify", editForm, postId, project));
         } else {
             flash(Constants.WARNING, "board.notAuthor");
             return redirect(routes.BoardApp.post(project.owner, project.name, postId));
         }
     }
 
     public static Result updatePost(String userName, String projectName, Long postId) {
         Form<Post> postForm = new Form<Post>(Post.class).bindFromRequest();
         Project project = ProjectApp.getProject(userName, projectName);
 
         if (postForm.hasErrors()) {
             flash(Constants.WARNING, "board.post.empty");
             return redirect(routes.BoardApp.editPost(userName, projectName, postId));
         } else {
 
             Post post = postForm.get();
             post.authorId = UserApp.currentUser().id;
             post.authorName = UserApp.currentUser().name;
             post.id = postId;
             post.filePath = saveFile(request());
             post.project = project;
 
             Post.edit(post);
         }
 
         return redirect(routes.BoardApp.posts(project.owner, project.name));
         
     }
     
     public static Result deleteComment(String userName, String projectName, Long postId, Long commentId) {
         Comment.delete(commentId);
         Post.countDownCommentCounter(postId);
         return redirect(routes.BoardApp.post(userName, projectName, postId));
     }
 
     private static String saveFile(Request request) {
         MultipartFormData body = request.body().asMultipartFormData();
 
         FilePart filePart = body.getFile("filePath");
 
         if (filePart != null) {
             File saveFile = new File("public/uploadFiles/" + filePart.getFilename());
             filePart.getFile().renameTo(saveFile);
             return filePart.getFilename();
         }
         return null;
     }
     
 }
