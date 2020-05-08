 package cn.sunjiachao.s7blog.modules.blog.dao.imp;
 
 import cn.sunjiachao.s7blog.modules.blog.dao.IBlogDao;
 import cn.sunjiachao.s7common.model.Blog;
 import cn.sunjiachao.s7common.model.dto.BlogBodyDto;
 import cn.sunjiachao.s7common.model.dto.BlogShortBodyDto;
 import cn.sunjiachao.s7common.model.rowmapper.BlogBodyDtoRowMapper;
 import cn.sunjiachao.s7common.model.rowmapper.BlogShortBodyDtoRowMapper;
 import cn.sunjiachao.s7common.model.web.Page;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
 import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.util.HashMap;
 import java.util.List;
 
 @Repository(value = "blogDao")
 public class BlogDaoImp implements IBlogDao {
 
     private NamedParameterJdbcTemplate jdbcTemplate;
     private SimpleJdbcInsert jdbcInsert;
 
     @Autowired
     public void setDataSource(DataSource dataSource) {
         this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
         this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("s7_blog").usingGeneratedKeyColumns("blogId");
     }
 
     @Override
     public int saveBlog(Blog blog) throws RuntimeException {
         SqlParameterSource params = new BeanPropertySqlParameterSource(blog);
         Number id = jdbcInsert.executeAndReturnKey(params);
         return id.intValue();
     }
 
     @Override
     public List<BlogShortBodyDto> getAllBlogList() {
         String sql = "SELECT b.blogId,b.title,b.shortBody,b.createTime,u.loginName FROM s7_blog AS b INNER JOIN s7_user AS u WHERE b.createUser = 1;";
         List<BlogShortBodyDto> blogs = jdbcTemplate.query(sql, new HashMap<String, Object>(), new BlogShortBodyDtoRowMapper());
         return blogs;
     }
 
     @Override
     public BlogBodyDto getBlog(int id) {
         String sql = "SELECT u.loginName,u.createTime,b.title,b.body,b.blogId FROM s7_user AS u," +
                 "(SELECT blogId,title,body,createTime,createUser FROM s7_blog WHERE blogId=:blogId) AS b " +
                 "WHERE b.createUser = u.uid;";
         SqlParameterSource p = new MapSqlParameterSource("blogId", id);
         return jdbcTemplate.queryForObject(sql, p, new BlogBodyDtoRowMapper());
     }
 
     @Override
     public Page<BlogShortBodyDto> getBlogByPage(int currentPage, int numsPerPage) {
         String sql_rows = "SELECT count(*) FROM s7_blog WHERE createUser = 1;";
         int totalRows = jdbcTemplate.queryForInt(sql_rows, new HashMap<String, String>());//取得行数
         
         currentPage = (currentPage < 1) ? 1 : currentPage; 
         numsPerPage = (numsPerPage < 1) ? 1 : numsPerPage;
         int startIndex = (currentPage - 1) * numsPerPage;
         String sql_page = "select u.loginName,b.shortBody,b.createTime,b.title,b.blogId from s7_blog as b inner join "
         		+ "(select uid,loginName from s7_user where uid = 1) as u limit :start,:end";
         MapSqlParameterSource p = new MapSqlParameterSource();
         p.addValue("start", startIndex).addValue("end", numsPerPage);
         List<BlogShortBodyDto> list = jdbcTemplate.query(sql_page, p, new BlogShortBodyDtoRowMapper());
        Page<BlogShortBodyDto> page = new Page<BlogShortBodyDto>(list, totalRows, currentPage, numsPerPage);
         return page;
     }
 }
