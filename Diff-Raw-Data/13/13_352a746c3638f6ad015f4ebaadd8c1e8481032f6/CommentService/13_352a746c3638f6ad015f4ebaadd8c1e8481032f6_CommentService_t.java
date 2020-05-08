 package com.thoughtworks.twu.service;
 
 import com.thoughtworks.twu.domain.Comment;
 import com.thoughtworks.twu.persistence.ICommentMapper;
 import org.apache.ibatis.session.SqlSession;
 import org.apache.ibatis.session.SqlSessionFactory;
 
 import java.util.List;
 
 public class CommentService implements ICommentService{
     private ICommentMapper commentMapper;
     private SqlSessionFactory sqlSessionFactory;
     private SqlSession session;
 
     public CommentService(){
         this.sqlSessionFactory = MyBatisConnectionFactory.getSqlSessionFactory();
         this.session = sqlSessionFactory.openSession(true);
     }
 
     @Override
     public Comment addNewComment(Integer boardID, String name, String comment) {
         try {
 
             if(boardID==null)
                 throw new RuntimeException("Select a valid Ideaboard");
 
             if(comment.isEmpty())
                 throw new RuntimeException("Please Enter a message");
 
             if(name.isEmpty())
                 name="anonymous";
 
             commentMapper = session.getMapper(ICommentMapper.class);
             Comment newComment = new Comment(boardID, name, comment);
             commentMapper.insertComment(newComment);
             Comment savedComment = commentMapper.selectComment(newComment);
             return savedComment;
         } finally {
 //            session.commit(true);
 //            session.close();
         }
 
     }
 
     @Override
     public void deleteComments(Integer boardID) {
         commentMapper = session.getMapper(ICommentMapper.class);
         commentMapper.deleteComments(boardID);
     }
 
     @Override
     public Integer getCommentsCount(Integer boardID) {
         commentMapper = session.getMapper(ICommentMapper.class);
         return commentMapper.getCommentCount(boardID);
     }
 
     @Override
     public List<Comment> getAllComments(Integer boardID) {
         commentMapper = session.getMapper(ICommentMapper.class);
         return commentMapper.getAllComments(boardID);
     }
 }
