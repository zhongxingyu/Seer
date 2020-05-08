 package uk.ac.sussex.asegr3.tracker.server.dao;
 
 
 import org.skife.jdbi.v2.sqlobject.Bind;
 import org.skife.jdbi.v2.sqlobject.SqlUpdate;
 
 public interface CommentsDao{
 
	@SqlUpdate("insert into comments (fK_use_id,fK_loc_id, comments, image) values (:fk_user_id,:fk_loc_id,:comments,:image)")
 	public void insert(@Bind("fk_user_id") int user_Id, @Bind("fk_loc_id") int loc_id, @Bind("comments") String comments, @Bind("image") byte[] image );
 }
 
 
 
