 package no.kantega.blog.dao;
 
 import no.kantega.blog.model.Blog;
 import no.kantega.blog.model.BlogPost;
 import no.kantega.blog.model.BlogPostComment;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.List;
 
 /**
  *
  */
 public class BlogDao {
 
     private final JdbcTemplate template;
 
     public BlogDao(DataSource dataSource) {
         this.template = new JdbcTemplate(dataSource);
     }
 
     public List<Blog> getAllBlogs() {
         return getBlogsWhere("");
     }
 
     private List<Blog> getBlogsWhere(String wherePart, Object... params) {
         return template.query("select * from blog " + wherePart +" order by blogname", new RowMapper<Blog>() {
             @Override
             public Blog mapRow(ResultSet resultSet, int i) throws SQLException {
                 return getBlogFromResultSet(resultSet);
             }
         }, params);
     }
 
     private Blog getBlogFromResultSet(ResultSet resultSet) throws SQLException {
         Blog blog = new Blog();
         blog.setId(resultSet.getLong("blogid"));
         blog.setName(resultSet.getString("blogname"));
         blog.setColor(resultSet.getString("color"));
         return blog;
     }
 
     public void saveOrUpdate(Blog blog) {
 
         if (blog.isNew()) {
             Connection con;
             try {
                 con = template.getDataSource().getConnection();
                 PreparedStatement statement = con.prepareStatement("INSERT INTO blog (blogname, color) VALUES (?, ?)");
                 statement.setString(1, blog.getName());
                 statement.setString(2, blog.getColor());
                 statement.executeUpdate();
                 con.close();
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         } else {
             template.update("update blog set blogname=?, color=? where blogid=?", blog.getName(), blog.getColor(), blog.getId());
         }
 
     }
 
     public Blog getBlogByName(String blogName) {
         List<Blog> blogs = getBlogsWhere(" where blogname=?", blogName);
         if(blogs.isEmpty()) {
             throw new IllegalArgumentException("Blog not found " + blogName);
         }
         return blogs.iterator().next();
     }
 
     public void deleteBlogByName(String blogName) {
         Blog blog = getBlogByName(blogName);
         template.update("DELETE FROM blog WHERE blogname=?", blog.getName());
     }
 
     public void saveOrUpdate(BlogPost post) {
         if (post.isNew()) {
             template.update("INSERT INTO blogpost (blogid, posttitle, postcontent, publishdate) VALUES (?, ?, ?, ?)",
                     post.getBlog().getId(),
                     post.getTitle(),
                     post.getContent(),
                     new Timestamp(System.currentTimeMillis()));
         } else {
             template.update("update blog set posttitle=?, postcontent=? where blogpostid=?",
                     post.getTitle(),
                     post.getBlogPostId(),
                     post.getBlogPostId());
         }
 
     }
 
     public List<BlogPost> getBlogPosts(Blog blog) {
         return getBlogPosts("where blogpost.blogid=?", blog.getId());
     }
 
     private List<BlogPost> getBlogPosts(String whereClaus, Object... parameters) {
         boolean fast = false;
         if(fast) {
             String sql = "select blog.blogid, blog.blogname, blog.color, blogpost.blogpostid, blogpost.posttitle, blogpost.postcontent, blogpost.publishdate, " +
                     " (select count(*) from blogpostcomment where blogpostcomment.blogpostcommentid=blogpost.blogpostid) as commentcount " +
                     " from blogpost left join blog on blog.blogid=blogpost.blogid " + whereClaus + " order by  blogpost.blogpostid desc";
 
             return template.query(sql, new RowMapper<BlogPost>() {
                 @Override
                 public BlogPost mapRow(ResultSet rs, int i) throws SQLException {
                     Blog blog = getBlogFromResultSet(rs);
                     BlogPost post = new BlogPost(blog);
                     post.setBlogPostId(rs.getInt("blogpostid"));
                     post.setTitle(rs.getString("posttitle"));
                     post.setContent(rs.getString("postcontent"));
                     post.setPublishDate(rs.getTimestamp("publishdate"));
                     post.setCommentCount(rs.getInt("commentcount"));
                     return post;
                 }
             }, parameters);
 
         } else {
             String sql = "select blogpost.blogid, blogpost.blogpostid, blogpost.posttitle, blogpost.postcontent, blogpost.publishdate, " +
                     " (select count(*) from blogpostcomment where blogpostcomment.blogpostcommentid=blogpost.blogpostid) as commentcount " +
                     " from blogpost " + whereClaus + " order by  blogpost.blogpostid desc";
 
             return template.query(sql, new RowMapper<BlogPost>() {
                 @Override
                 public BlogPost mapRow(ResultSet rs, int i) throws SQLException {
                     Blog blog = getBlogsWhere("where blogid=?", rs.getInt("blogid")).iterator().next();
                     BlogPost post = new BlogPost(blog);
                     post.setBlogPostId(rs.getInt("blogpostid"));
                     post.setTitle(rs.getString("posttitle"));
                     post.setContent(rs.getString("postcontent"));
                     post.setPublishDate(rs.getTimestamp("publishdate"));
                     post.setCommentCount(rs.getInt("commentcount"));
                     return post;
                 }
             }, parameters);
 
         }
     }
 
     public BlogPost getBlogPost(long blogPostId) {
         return getBlogPosts("where blogpost.blogpostid=?", blogPostId).iterator().next();
     }
 
     public void saveOrUpdate(BlogPostComment comment) {
         if(comment.isNew()) {
             template.update("INSERT INTO blogpostcomment (blogpostid, commentauthor, commentcontent, commentpublishdate) VALUES (?, ?, ?, ?)",
                     comment.getBlogPost().getBlogPostId(),
                     comment.getAuthor(),
                     comment.getContent(),
                     new Timestamp(System.currentTimeMillis()));
         } else {
             template.update("update blogpostcomment set commentauthor=?, commentcontent=?,commentpublishdate=? where blogpostcommentid=?",
                     comment.getAuthor(),
                     comment.getContent(),
                     comment.getPublishDate(),
                     comment.getBlogPostCommentId());
         }
 
     }
 
     public BlogPost getBlogPost(Blog blog, String postName) {
        return getBlogPosts("where blog.blogid=? and blogpost.posttitle=?", blog.getId(), postName).iterator().next();
     }
 
     public List<BlogPostComment> getComments(final BlogPost post) {
         String sql = "select blogpostcommentid, commentauthor, commentcontent, commentpublishdate from blogpostcomment where blogpostid=?";
         return template.query(sql, new RowMapper<BlogPostComment>() {
             @Override
             public BlogPostComment mapRow(ResultSet rs, int i) throws SQLException {
                 BlogPostComment comment = new BlogPostComment(post);
                 comment.setAuthor(rs.getString("commentauthor"));
                 comment.setContent(rs.getString("commentcontent"));
                 comment.setPublishDate(rs.getTimestamp("commentpublishdate"));
                 return comment;
             }
         }, post.getBlogPostId());
 
     }
 }
