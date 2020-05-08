 package me.ixin.prince.post.dao;
 
import java.util.List;

 import org.nutz.dao.QueryResult;
 
 import me.ixin.prince.post.model.Post;
 import me.ixin.prince.post.web.command.PostCommand;
 
 public interface PostDao {
 	/**
 	 * 插入一条post
 	 * @param post
 	 * @return
 	 */
 	public Post savePost(Post post);
 	/**
 	 * 更新一条post
 	 * @param post
 	 * @return
 	 */
 	public int updatePost(Post post);
 	/**
 	 * 按id删除一条post
 	 * @param id
 	 * @return
 	 */
 	public int delPost(Long id);
 	/**
 	 * 按id查找一条post
 	 * @param id
 	 * @return
 	 */
 	public Post findPostByID(Long id);
 	/**
 	 * 分页 取post
 	 * @param command
 	 * @param pageNumber
 	 * @param pageSize
 	 * @return
 	 */
 	public QueryResult getPosts(PostCommand command, int pageNumber, int pageSize);
 }
