 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.oletalk.mp3s.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 import javax.sql.DataSource;
 import net.oletalk.mp3s.data.TagInfo;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author colin
  */
 @Component
 public class TagDAO {
     DataSource dataSource;
     JdbcTemplate jdbcTemplate;
     
     @Autowired
     public void setDataSource(DataSource ds) {
         this.dataSource = ds;
         jdbcTemplate = new JdbcTemplate(ds);
     }
     
     public void updateTag(String taghash, Map <String, String> newvalues) {
         String newartist = newvalues.get("artist");
        String newtitle  = newvalues.get("title");
         String sql = "UPDATE mp3s_tags SET ";
         String suffix = " WHERE file_hash = ?";
         
         int rowsAffected = 0;
         if (newartist != null && newtitle != null) { // can't figure out any other way at the moment
             sql += "artist = ?, title = ?" + suffix;
            rowsAffected = jdbcTemplate.update(sql, new Object[]{newartist, newtitle, taghash});
         } else if (newartist != null) {
            sql += "artist = ?" + suffix;
            rowsAffected = jdbcTemplate.update(sql, new Object[]{newartist, taghash});
         } else if (newtitle != null) {
            sql += "title = ?" + suffix;
            rowsAffected = jdbcTemplate.update(sql, new Object[]{newtitle, taghash});
         } else {
             // do nothing, no new values received
         }
                 
     }
     
     public List<TagInfo> getEmptyTags() {
         String sql = "select song_filepath, file_hash, artist, title, secs from mp3s_tags where artist is NULL";
         //return this.jdbcTemplate.query(sql, this.getMapper(), new HashMap());
         return this.jdbcTemplate.query(sql, this.getMapper());
     }
     
     public TagInfo getTagInfo(final String filepath) {
         String sql = "select song_filepath, file_hash, artist, title, secs from mp3s_tags where song_filepath = ?";
         return this.jdbcTemplate.queryForObject(sql, this.getMapper(), filepath);
     }
     
     private ParameterizedRowMapper<TagInfo> getMapper() {
         return new ParameterizedRowMapper<TagInfo>() {
           public TagInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
               TagInfo taginfo = new TagInfo();
               taginfo.setSongFilepath(rs.getString("song_filepath"));
               taginfo.setFilehash(rs.getString("file_hash"));
               taginfo.setArtist(rs.getString("artist"));
               taginfo.setTitle(rs.getString("title"));
               taginfo.setSecs(rs.getInt("secs"));
               return taginfo;
           }  
         };
     }
 }
