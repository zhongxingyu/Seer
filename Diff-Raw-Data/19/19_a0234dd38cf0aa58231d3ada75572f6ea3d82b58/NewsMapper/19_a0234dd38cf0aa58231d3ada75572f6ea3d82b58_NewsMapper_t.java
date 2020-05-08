 package com.ml.bus.mapper;
 
 import java.util.List;
 
 import org.apache.ibatis.annotations.Insert;
 import org.apache.ibatis.annotations.Param;
 import org.apache.ibatis.annotations.Select;
 import org.springframework.stereotype.Repository;
 
 import com.ml.bus.model.News;
 import com.ml.util.Pagination;
 
 @Repository
 public interface NewsMapper {
 	
 	final String SELECT_ALL = "SELECT * FROM news";
 	final String SELECT_BY_CATEGORYID = "SELECT * FROM news WHERE categoryId=#{categoryId}";
 	final String SELECT_BY_ID = "SELECT * FROM news WHERE cluster_name=#{clusterName}";
	final String SELECT_BY_PAGER = "SELECT * FROM news ORDER BY date DESC LIMIT #{startIndex}, #{limitSize}";
	final String SELECT_BY_CATEGORYID_PAGER = "SELECT * FROM news WHERE categoryId=#{categoryId} ORDER BY date DESC LIMIT #{pager.startIndex}, #{pager.limitSize}";
 	final String INSERT = "INSERT INTO news(title, content, author, date, categoryId, url, img, source, originalCategory) "
 			+ "VALUES(#{title}, #{content}, #{author}, #{date}, #{categoryId}, #{url}, #{img}, #{source}, #{originalCategory})";
 	
 	final String SELECT_COUNT = "SELECT count(*) FROM news";
 	final String SELECT_COUNT_CATEGORY = "SELECT count(*) FROM news WHERE categoryId=#{categoryId}";
 
 	@Select(SELECT_ALL)
 	List<News> findAll();
 	
 	@Select(SELECT_BY_CATEGORYID)
 	List<News> findByCategory(String categoryId);
 	
 	@Select(SELECT_BY_PAGER)
 	List<News> findByPage(Pagination pager);
 	
 	@Select(SELECT_BY_CATEGORYID_PAGER)
 	List<News> findByCategoryAndPage(@Param("categoryId") String categoryId, @Param("pager") Pagination pager);
 	
 	@Select(SELECT_COUNT)
 	int count();
 	
 	@Select(SELECT_COUNT_CATEGORY)
 	int countCategory(String categoryId);
 	
 	@Insert(INSERT)
 	void save(News news);
 	
 	
 }
