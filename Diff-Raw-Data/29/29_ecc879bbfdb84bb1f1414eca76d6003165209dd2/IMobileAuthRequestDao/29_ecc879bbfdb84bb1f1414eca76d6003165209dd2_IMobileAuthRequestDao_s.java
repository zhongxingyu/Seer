 package org.siraya.rent.user.dao;
 
 import org.apache.ibatis.annotations.Insert;
 import org.apache.ibatis.annotations.Param;
 import org.apache.ibatis.annotations.ResultMap;
 import org.apache.ibatis.annotations.Select;
 import org.siraya.rent.pojo.MobileAuthRequest;
 import org.springframework.stereotype.Repository;
 import java.util.List;
 @Repository("mobileAuthRequestDao")
 public interface IMobileAuthRequestDao {
 	
 	
	@Insert("INSERT INTO MOBILE_AUTH_REQUEST (REQUEST_ID,FORCE_REAUTH,DONE,MOBILE_PHONE,AUTH_USER_ID,CALLBACK,REQUEST_TIME,REQUEST_FROM,USER_ID) values (#{requestId},#{forceReauth}" +
			",#{done},#{mobilePhone},#{authUserId},#{callback},#{requestTime},#{requestFrom},#{userId})")
     public void newRequest(MobileAuthRequest request);
 
 	@Select("select * from MOBILE_AUTH_REQUEST where REQUEST_ID=#{requestId}")
     @ResultMap("rent.mapper.MobileAuthRequestResultMap")
 	public MobileAuthRequest get(@Param("requestId")String requestId);
 
 	@Select("select * from MOBILE_AUTH_REQUEST where REQUEST_FROM=#{requestFrom} order by REQUEST_TIME desc limit #{limit} offset #{offset}")
     @ResultMap("rent.mapper.MobileAuthRequestResultMap")
 	public List<MobileAuthRequest> getRequestsByFrom(@Param("requestFrom")String requestFrom, @Param("limit")int limit, @Param("offset")int offset);
 
 	@Select("select * from MOBILE_AUTH_REQUEST where AUTH_USER_ID=#{authUserId} order by REQUEST_TIME desc limit #{limit} offset #{offset}")
     @ResultMap("rent.mapper.MobileAuthRequestResultMap")
 	public List<MobileAuthRequest> getRequestsByAuthUser(@Param("authUserId")String authUserId, @Param("limit")int limit, @Param("offset")int offset);
 
 }
