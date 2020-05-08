 package net.marioosh.spring.springonly.model.impl;
 
 import java.io.IOException;
 import java.net.URL;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.sql.DataSource;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Source;
 import net.marioosh.spring.springonly.model.dao.LinkDAO;
 import net.marioosh.spring.springonly.model.entities.Link;
 import net.marioosh.spring.springonly.model.helpers.BrowseParams;
 import net.marioosh.spring.springonly.model.helpers.LinkRowMapper;
 import net.marioosh.spring.springonly.model.helpers.Range;
 import net.marioosh.spring.springonly.utils.WebUtils;
 import org.apache.log4j.Logger;
 import org.springframework.asm.Type;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.SqlParameter;
 import org.springframework.jdbc.object.MappingSqlQuery;
 import org.springframework.jdbc.object.SqlQuery;
 import org.springframework.stereotype.Repository;
 
 @Repository("linkDAO")
 public class LinkDAOImpl implements LinkDAO {
 
 	private Logger log = Logger.getLogger(getClass());
 	private JdbcTemplate jdbcTemplate;
 	
 	/**
 	 * @Autowired - autowired by Spring's dependency injection facilities
 	 * dataSourca from matching bean in the Spring container
 	 */
 	@Autowired
 	public void setDataSource(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
 	/**
 	 * BeanPropertyRowMapper - mapuje nazwy kolumn w bazie na nazwy pol w beanie
 	 */
 	public Link get(Integer id) {
 		String sql = "select * from tlink where id = ?";
 		try {
 			Link link = (Link)jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<Link>(Link.class));
 			return link;
 		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
 			return null;
 		}
 	}	
 	
 	public List<Link> findAll(String search) {
 		BrowseParams b = new BrowseParams();
 		b.setSearch(search);
 		b.setRange(new Range(0,20));
 		return findAll(b);
 		
 		/*
 		String s = search != null ? "where address like '%"+search+"%' or name like '%"+search+"%'" : "";
 		SqlQuery<Link> query = new MappingSqlQuery<Link>(jdbcTemplate.getDataSource(), "select * from tlink "+s+" order by id desc"){
 			@Override
 			protected Link mapRow(ResultSet resultset, int i)
 					throws SQLException {
 				Link link = new Link(resultset.getInt("id"), resultset.getString("address"), resultset.getString("name"));
 				link.setLdate(resultset.getDate("ldate"));
 				link.setDescription(resultset.getString("description"));
 				link.setClicks(resultset.getInt("clicks"));
 				return link;
 			}
 		};
 		return query.execute();
 		*/ 
 	}
 
 	public void add(Link link) {
 		if(link.getName().isEmpty()) {
 			Map<String, String> m = WebUtils.pageInfo(link.getAddress());
            if(link.getDescription() == null) {
                link.setDescription("");
            }
 			if(m.get("title") != null && (link.getName() == null || link.getName().isEmpty())) {
 				link.setName(m.get("title"));
 			}
 			if(m.get("description") != null && (link.getDescription() == null || link.getDescription().isEmpty())) {
 				link.setDescription(m.get("description"));
 			}
             if(link.getName() == null || link.getName().isEmpty()) {
                 link.setName(link.getAddress());
             }
 		}
 		jdbcTemplate.update("insert into tlink (address, name, description, ldate, clicks) values(?, ?, ?, ?, 0)", link.getAddress(), link.getName(), link.getDescription(), link.getLdate());
 	}
 
 	public void delete(Integer id) {
 		jdbcTemplate.update("delete from tlink where id = "+id);
 	}
 
 	public List<Link> findAll(BrowseParams browseParams) {
 
 		String sort = "id desc";
 		if(browseParams.getSort() != null) {
 			sort = browseParams.getSort();
 		}
 		String limit = "";
 		if(browseParams.getRange() != null) {
 			 limit = "limit " + browseParams.getRange().getMax() + " offset " + browseParams.getRange().getStart(); 
 		}
 		String s = browseParams.getSearch() != null ? "where address like '%"+browseParams.getSearch()+"%' or name like '%"+browseParams.getSearch()+"%'" : "";
 		String sql = "select * from tlink "+s+" order by "+sort + " " + limit;
 		
 		return jdbcTemplate.query(sql, new LinkRowMapper());
 		
 	}
 
 	public void click(Integer id) {
 		String sql = "update tlink set clicks = clicks + 1 where id = ?";
 		jdbcTemplate.update(sql, new Object[]{id});
 	}
 
 	public int countAll(BrowseParams browseParams) {
 		String sql = "select count(*) from tlink " + (browseParams.getSearch() != null ? "where address like '%"+browseParams.getSearch()+"%' or name like '%"+browseParams.getSearch()+"%'" : "");
 		return jdbcTemplate.queryForInt(sql);
 	}
 
 	public int countAll(String search) {
 		String sql = "select count(*) from tlink " + (search != null ? "where address like '%"+search+"%' or name like '%"+search+"%'" : "");
 		return jdbcTemplate.queryForInt(sql);
 	}
 
 	public int update(Link link) {
 		Object[] params = {link.getAddress(), link.getName(), link.getDescription(), link.getClicks(), link.getId()};
 		int[] types = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.SMALLINT};
 		int rows = jdbcTemplate.update("update tlink set address = ?, name = ?, description = ?, clicks = ? where id = ?", params, types);
 		log.debug("Updated "+rows +" rows.");
 		return rows;
 	}
 	
 }
