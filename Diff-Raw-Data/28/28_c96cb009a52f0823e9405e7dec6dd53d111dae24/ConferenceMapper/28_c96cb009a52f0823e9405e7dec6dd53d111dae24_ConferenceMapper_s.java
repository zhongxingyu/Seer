 package lt.nfq.conference.service.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import lt.nfq.conference.domain.Conference;
import lt.nfq.conference.domain.User;
 
 import org.apache.ibatis.annotations.Delete;
 import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
 import org.apache.ibatis.annotations.Options;
 import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
 import org.apache.ibatis.annotations.Select;
 import org.apache.ibatis.annotations.SelectKey;
 import org.apache.ibatis.annotations.Update;
 
 public interface ConferenceMapper {
 
     @Select("SELECT * FROM Conference WHERE conferenceId=#{id}")
     public Conference getConference(@Param("id") int id);
 
     @Select("SELECT * FROM Conference")
     public List<Conference> getConferences();
 
     @Select("SELECT * FROM Conference WHERE conferenceFrom > #{start} and conferenceFrom < #{end} and conferenceTill < #{end} and conferenceTill > #{start}")
     public List<Conference> getConferencesByDates(@Param("start") Date start, @Param("end") Date end);
 
     @Options(flushCache=true)
     @Update("UPDATE Conference SET"
     		+ " conferenceTypeId = #{conferenceTypeId}, title = #{title},"
     		+ " conferenceFrom = #{conferenceFrom},"
     		+ " conferenceTill = #{conferenceTill},"
     		+ " teaser = #{teaser},"
     		+ " address = #{address},"
     		+ " description = #{description},"
     		+ " isPublished = #{isPublished}"
     		+ " WHERE conferenceId=#{conferenceId}")
     public int updateConference(Conference conference);
 
     @Options(flushCache=true)
     @Insert("INSERT INTO Conference (conferenceTypeId, title, conferenceFrom, conferenceTill, teaser, address, description, isPublished)"
     		+ " VALUES (#{conferenceTypeId}, #{title}, #{conferenceFrom}, #{conferenceTill}, #{teaser}, #{address}, #{description}, #{isPublished})")
     @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="conferenceId", before=false, resultType=int.class)
     public int insertConference(Conference conference);
     
     @Options(flushCache=true)
     @Delete("DELETE FROM Conference WHERE conferenceId = #{id}")
     public int deleteConference(@Param("id") int id);
 }
