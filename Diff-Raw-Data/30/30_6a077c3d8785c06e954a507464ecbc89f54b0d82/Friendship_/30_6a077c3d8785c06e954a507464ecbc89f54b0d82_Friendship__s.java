 package phasebook.friendship;
 
 import java.sql.Timestamp;
 import javax.annotation.Generated;
 import javax.persistence.metamodel.SingularAttribute;
 import javax.persistence.metamodel.StaticMetamodel;
 
@Generated(value="Dali", date="2011-11-30T23:07:00.734+0000")
 @StaticMetamodel(Friendship.class)
 public class Friendship_ {
 	public static volatile SingularAttribute<Friendship, Integer> id;
 	public static volatile SingularAttribute<Friendship, Boolean> accepted_;
 	public static volatile SingularAttribute<Friendship, Timestamp> createdAt;
 	public static volatile SingularAttribute<Friendship, Timestamp> deletedAt;
 	public static volatile SingularAttribute<Friendship, Boolean> read_;
	public static volatile SingularAttribute<Friendship, Integer> hostUserId;
	public static volatile SingularAttribute<Friendship, Integer> invitedUserId;
 }
