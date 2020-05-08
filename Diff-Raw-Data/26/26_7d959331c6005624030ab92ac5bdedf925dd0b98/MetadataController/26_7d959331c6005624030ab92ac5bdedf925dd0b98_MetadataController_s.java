 package gov.usgs;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class MetadataController {
 	
 	private Logger logger = LoggerFactory.getLogger(getClass());
	private DataSource dataSource;
 	
 	@RequestMapping("/aquifers")
 	@ResponseBody
 	public List<Map<String,Object>> aquifers() {
		
		JdbcTemplate template = new JdbcTemplate(dataSource);
		
 		List<Map<String,Object>> result = template.queryForList(
 				"select nat_aquifer_cd aquifer, count(*) count\n" + 
 				"from gw_data_portal.well_registry\n" + 
 				"group by nat_aquifer_cd");
 		
 		logger.debug("Found {} aquifers", result.size());
 		
 		return result;
 	}
 	
 	@RequestMapping("/agencies")
 	@ResponseBody
 	public List<Map<String,Object>> agencies() {
		JdbcTemplate template = new JdbcTemplate(dataSource);
 		
 		List<Map<String,Object>> result = template.queryForList(
				"select agency_cd, count(*) count\n" + 
 				"from gw_data_portal.well_registry\n" + 
				"group by agency_cd");
 		
 		logger.debug("Found {} agencies", result.size());
 
 		return result;
 	}
 
 	@RequestMapping("/states")
 	@ResponseBody
 	public List<Map<String,Object>> states() {
		JdbcTemplate template = new JdbcTemplate(dataSource);
 		
 		List<Map<String,Object>> result = template.queryForList(
				"select state_cd, count(*) count\n" + 
 				"from gw_data_portal.well_registry\n" + 
				"group by state_cd");
 		
 		logger.debug("Found {} states", result.size());
 
 		return result;
 	}
 	
 	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
 	}
 
 }
