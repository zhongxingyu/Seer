 package com.forum.repository;
 
 import com.forum.domain.Tag;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.util.List;
 
 
 @Repository
 public class TagRepository {
     private JdbcTemplate jdbcTemplate;
 
     @Autowired
     public TagRepository(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public List<Tag> getTagsByTerm(String term) {
        return retrieveTags("SELECT * , 1 as count FROM TAG WHERE NAME LIKE ?", "%"+term+"%");
     }
 
     public Integer createTag(Tag tag) {
         return jdbcTemplate.update("INSERT INTO TAG (NAME) VALUES (?)",
                 new Object[]{tag.getValue()});
     }
 
     public Tag getTagByName(String tag) {
        String query = "SELECT *, 1 as count FROM TAG WHERE NAME=?" ;
 
         return (Tag)jdbcTemplate.queryForObject(query,
                 new Object[]{tag}, new TagRowMapper());
     }
 
     public List<Tag> allTags() {
        return retrieveTags("select id,name, count(*) as count from tag,question_tag where tag_id=id group by id");
     }
 
     @SuppressWarnings("unchecked")
     private List<Tag> retrieveTags(String query, Object... params) {
         return jdbcTemplate.query(query, params, new TagRowMapper());
     }
 
     public List<Tag> getTagByQuestionId(Integer questionId) {
        return jdbcTemplate.query("SELECT ID, NAME ,1 as count FROM QUESTION_TAG, TAG WHERE QUESTION_TAG.TAG_ID=TAG.ID AND QUESTION_TAG.QUESTION_ID=?",
                 new Object[]{questionId},new TagRowMapper());
     }
 
 }
