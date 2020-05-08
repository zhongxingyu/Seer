 package smartpool.data;
 
 import org.apache.ibatis.annotations.*;
 import org.apache.ibatis.type.JdbcType;
 import org.joda.time.LocalTime;
 import smartpool.data.typeHandler.BuddyTypeHandler;
 import smartpool.data.typeHandler.LocalTimeTypeHandler;
 import smartpool.domain.Buddy;
 import smartpool.domain.CarpoolBuddy;
 
 import java.util.ArrayList;
 
 public interface CarpoolBuddyMapper {
    String SELECT_PICKUP_POINT_AND_TIME = "select buddy_username, pickup_point, pickup_time from carpoolbuddy where carpool_name = #{carpoolName} order by pickup_time";
     String DELETE_BUDDY_FROM_CARPOOL = "delete from carpoolbuddy where buddy_username = #{userName} and carpool_name = #{carpoolName};";
 
     @Select(SELECT_PICKUP_POINT_AND_TIME)
             @Results(value = {
                     @Result(property = "buddy",column = "buddy_username", javaType = Buddy.class, jdbcType = JdbcType.VARCHAR, typeHandler = BuddyTypeHandler.class),
                     @Result(property = "pickupPoint", column = "pickup_point"),
                     @Result(property = "pickupTime", column = "pickup_time", javaType = LocalTime.class, jdbcType = JdbcType.TIME, typeHandler = LocalTimeTypeHandler.class)
             })
     ArrayList<CarpoolBuddy> getByCarpoolName(@Param("carpoolName") String carpoolName);
 
     @Insert("insert into carpoolbuddy values(#{buddyUserName},#{carpoolName},#{pickupPoint},#{pickupTime})")
     void insert(@Param("buddyUserName")String userName,@Param("carpoolName") String carpoolName,@Param("pickupPoint") String pickupPoint,@Param("pickupTime") String pickupTime);
 
     @Delete(DELETE_BUDDY_FROM_CARPOOL)
     void remove(@Param("userName") String userName, @Param("carpoolName") String carpoolName);
 }
