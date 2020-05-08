 package ua.krem.agent.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import ua.krem.agent.model.DocHead;
 import ua.krem.agent.model.DocHeadFilter;
 
 @Repository
 public class DocumentDAO {
 
 private JdbcTemplate jdbcTemplate;
 	
 	@Inject
 	public DocumentDAO(JdbcTemplate jdbcTemplate){
 		this.jdbcTemplate = jdbcTemplate;
 	}
 	
 	public List<DocHead> selectDoc(DocHeadFilter filter){
 		List<DocHead> docList = new ArrayList<DocHead>();
 		if(filter == null){
 			String sql = "SELECT doc_id id, date, add1, add2, type FROM doc ORDER BY id limit 50";
 			try{
 				List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
 				for(Map<String, Object> map : mapList){
 					docList.add(extractDocHead(map));
 				}
 			}catch(EmptyResultDataAccessException e){
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println("filter.userId = " + filter.getUserId());
 			
 			StringBuilder sql = new StringBuilder();
 			sql.append("SELECT doc_id id, date, add1, add2, type FROM doc ");
 			if(filter.getUserId() != null){
 				sql.append(" WHERE user_id = ").append(filter.getUserId());
 			}
 			
 			if(filter.getAdd1() != null && !filter.getAdd1().isEmpty()){
 				if(sql.indexOf("WHERE") != -1){
 					sql.append(" AND ");
 				} else {
 					sql.append(" WHERE ");
 				}
 				sql.append(" add1 LIKE '%").append(filter.getAdd1()).append("%' ");
 			}
 
 			if(filter.getAdd2() != null && !filter.getAdd2().isEmpty()){
 				if(sql.indexOf("WHERE") != -1){
 					sql.append(" AND ");
 				} else {
 					sql.append(" WHERE ");
 				}
				sql.append(" add2 LIKE '%").append(filter.getAdd2()).append("%' ");
 			}
 
 			if(filter.getDocType() != null){
 				if(sql.indexOf("WHERE") != -1){
 					sql.append(" AND ");
 				} else {
 					sql.append(" WHERE ");
 				}
 				sql.append(" type = ").append(filter.getDocType());
 			}
 
 			if(filter.getFrom() != null && filter.getTo() != null && !filter.getFrom().isEmpty() && !filter.getTo().isEmpty()){
 				if(sql.indexOf("WHERE") != -1){
 					sql.append(" AND ");
 				} else {
 					sql.append(" WHERE ");
 				}
 				sql.append(" date BETWEEN ").append(filter.getFrom()).append(" AND ").append(filter.getTo());
 			}
 			
			//if user comes without session
 			if(sql.indexOf("WHERE") != -1 && sql.indexOf("=") == -1){
 				sql = new StringBuilder(sql.substring(0, sql.indexOf("WHERE")));
 			}
 			
			if(sql.indexOf("BETWEEN") == -1){
				if(sql.indexOf("WHERE") != -1){
					sql.append(" AND YEAR(date) = YEAR(NOW()) AND MONTH(date) = MONTH(NOW()) AND DAY(date) = DAY(NOW())");
				} else {
					sql.append(" WHERE YEAR(date) = YEAR(NOW()) AND MONTH(date) = MONTH(NOW()) AND DAY(date) = DAY(NOW())");
				}
			}
			
 			sql.append(" ORDER BY id limit 50");
 			System.out.println(sql.toString());
 			try{
 				List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql.toString());
 				for(Map<String, Object> map : mapList){
 					docList.add(extractDocHead(map));
 				}
 			}catch(EmptyResultDataAccessException e){
 				e.printStackTrace();
 			}
 			
 			
 		}
 		
 		return docList;
 	}
 	
 	private DocHead extractDocHead(Map<String, Object> map) {
 		DocHead docHead = new DocHead();
 		if(map != null && !map.isEmpty()){
 			docHead.setId((Integer)map.get("id"));
 			docHead.setDate((Date)map.get("date"));
 			docHead.setAdd1((String)map.get("add1"));
 			docHead.setAdd2((String)map.get("add2"));
 			if((Integer)map.get("type") == 1){
 				docHead.setType("Возврат");
 			} else {
 				docHead.setType("Реализация");
 			}
 		}
 		return docHead;
 	}
 	
 }
