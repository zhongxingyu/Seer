 package gov.nysenate.billbuzz.model;
 
 import gov.nysenate.billbuzz.disqus.DisqusPost;
 
 public class BillBuzzApproval implements Comparable<BillBuzzApproval>
 {
     private BillBuzzUpdate update;
     private Long updateId;
     private DisqusPost post;
     private String postId;
     private BillBuzzThread thread;
     private String threadId;
     private BillBuzzAuthor author;
     private String authorId;
 
     public BillBuzzApproval()
     {
 
     }
 
     public BillBuzzApproval(Long updateId, String postId, String authorId, String threadId)
     {
         this.setUpdateId(updateId);
         this.setPostId(postId);
         this.setAuthorId(authorId);
         this.setThreadId(threadId);
     }
 
     public BillBuzzUpdate getUpdate()
     {
         return update;
     }
     public void setUpdate(BillBuzzUpdate update)
     {
         this.update = update;
     }
 
     public Long getUpdateId()
     {
         return updateId;
     }
     public void setUpdateId(Long updateId)
     {
         this.updateId = updateId;
     }
 
     public DisqusPost getPost()
     {
         return post;
     }
     public void setPost(DisqusPost post)
     {
         this.post = post;
     }
 
     public String getPostId()
     {
         return postId;
     }
     public void setPostId(String postId)
     {
         this.postId = postId;
     }
 
     public BillBuzzThread getThread()
     {
         return thread;
     }
     public void setThread(BillBuzzThread thread)
     {
         this.thread = thread;
     }
 
     public String getThreadId()
     {
         return threadId;
     }
     public void setThreadId(String threadId)
     {
         this.threadId = threadId;
     }
 
     public BillBuzzAuthor getAuthor()
     {
         return author;
     }
     public void setAuthor(BillBuzzAuthor author)
     {
         this.author = author;
     }
 
     public String getAuthorId()
     {
         return authorId;
     }
     public void setAuthorId(String authorId)
     {
         this.authorId = authorId;
     }
 
     @Override
     public int compareTo(BillBuzzApproval other)
     {
        return this.getPostId().compareTo(other.getPostId());
     }
 }
