 package ua.krem.agent.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.inject.Inject;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import ua.krem.agent.model.Brand;
 import ua.krem.agent.model.Document;
 import ua.krem.agent.model.Filter;
 import ua.krem.agent.model.Item;
 import ua.krem.agent.model.Product;
 
 @Repository
 public class ProductDAO {
 	private JdbcTemplate jdbcTemplate;
 	
 	@Inject
 	public ProductDAO(JdbcTemplate jdbcTemplate){
 		this.jdbcTemplate = jdbcTemplate;
 	}
 
 	public List<Product> getProducts(Filter filter, List<Item> itemListOriginal){
 		List<Item> itemList = null;
 		if(itemListOriginal != null){
 			System.out.println("all ids");
 			itemList = new ArrayList<Item>();
 			for(Item i : itemListOriginal){
 				System.out.println(i.id + ":" + i.amount);
 				itemList.add(i);
 			}
 		}
 		
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT x.prod_id id, x.name AS PName, x.code PCode, y.name GName, z.name SGName, q.name BName ");
 		sql.append("FROM product_group y, counteragent q, product x ");
 		sql.append("LEFT OUTER JOIN  product_sub_group z ON x.sub_group_id=z.sub_group_id ");
 		sql.append("WHERE x.group_id= y.group_id And x.brand_id=q.counteragent_id ");
 		if(filter != null){
 			if(filter.getProdName() != null && !filter.getProdName().trim().isEmpty()){
 				sql.append(" AND x.name LIKE '%").append(filter.getProdName()).append("%' ");
 				System.out.println("prodName: " + filter.getProdName());
 			}
 			if(filter.getBrand() != null && !filter.getBrand().trim().isEmpty()){
 				sql.append(" AND q.counteragent_id = ").append(filter.getBrand());
 			}
 		}
 		System.out.println(sql.toString());
 		List<Product> productList = new ArrayList<Product>();
 		List<Product> result = new ArrayList<Product>();
 		try{
 			List <Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString());
 			if(list != null && !list.isEmpty()){
	qwerty: 	for(int i=0; i<list.size(); i++)
 				{
 					Map<String, Object> elem = list.get(i);
 					Product item = new Product();
 					item.setId((Integer) elem.get("id"));
 					item.setName((String) elem.get("PName"));
 					item.setCode((String) elem.get("PCode"));
 					item.setGroup((String) elem.get("GName"));
 					item.setSubgroup((String) elem.get("SGName"));
 					item.setBrand((String) elem.get("BName"));
 					if(itemList != null && !itemList.isEmpty()){
 						for(int j = 0; j < itemList.size(); ++j){
 							if(itemList.get(j).id == item.getId()){
								continue qwerty;
//								item.setAmount(itemList.get(j).amount);
//								System.out.println("remove " + itemList.get(j).id);
//								itemList.remove(j);
 							}
 						}
 					}
 					
 					productList.add(item);
 				}	
 			}
 		}catch(EmptyResultDataAccessException e){
 			e.printStackTrace();
 		}
 		
 		if(itemList != null && !itemList.isEmpty()){
 			StringBuilder buildId = new StringBuilder();
 			for(Item i : itemList){
 				buildId.append(i.id).append(", ");
 			}
 			String ids = buildId.substring(0, buildId.length() - 2);
 			System.out.println("ids: " + ids);
 			String select = "SELECT x.prod_id id, x.name AS PName FROM product x WHERE x.prod_id IN (" + ids + ")";
 			System.out.println(select);
 			try{
 				List <Map<String, Object>> list = jdbcTemplate.queryForList(select);
 				if(list != null && !list.isEmpty()){
 					System.out.println("list.size = " + list.size());
 					for(int i=0; i<list.size(); i++)
 					{
 						Map<String, Object> elem = list.get(i);
 						Product item = new Product();
 						item.setId((Integer) elem.get("id"));
 						item.setName((String) elem.get("PName"));
 						System.out.println(item.getId() + ":" + item.getName());
 						if(itemList != null && !itemList.isEmpty()){
 							for(int j = 0; j < itemList.size(); ++j){
 								if(itemList.get(j).id == item.getId()){
 									item.setAmount(itemList.get(j).amount);
 									System.out.println("$remove " + itemList.get(j).id);
 									itemList.remove(j);
 									break;
 								}
 							}
 						}
 						
 						result.add(item);
 						System.out.println("productList.add({" + item.getId() + ":" + item.getAmount() + "})");
 					}	
 				}
 			}catch(EmptyResultDataAccessException e){
 				e.printStackTrace();
 			}
 		}
 		result.addAll(productList);
 		return result;
 	}
 
 	public List<Brand> getBrands(){
 		String sql = "SELECT counteragent_id id, name FROM counteragent WHERE brand_flag = 1";
 		List<Brand> brandList = new ArrayList<Brand>();
 		try{
 			List <Map<String, Object>> list = jdbcTemplate.queryForList(sql);
 			if(list != null && !list.isEmpty()){
 				for(int i=0; i<list.size(); i++)
 				{
 					Map<String, Object> elem = list.get(i);
 					Brand item = new Brand();
 					item.setId((Integer) elem.get("id"));
 					item.setName((String) elem.get("name"));
 					brandList.add(item);
 				}	
 			}
 		}catch(EmptyResultDataAccessException e){
 			e.printStackTrace();
 		}
 		return brandList;
 	}
 	
 	public void addDocument(Document doc){
 		String sql = "INSERT INTO doc (date, warehouse_id, shop_id, user_id) VALUES (NOW(), ?, ?, ?)";
 		try{
 			if(jdbcTemplate.update(sql, 1, doc.getShopId(), doc.getUserId()) != 0){
 				
 				sql = "SELECT max(doc_id) id FROM doc";
 				doc.setId((Integer) jdbcTemplate.queryForInt(sql));
 				
 				sql = "INSERT INTO doc_element (amount, doc_id, prod_id) VALUES (?, ?, ?)";
 				for(int i = 0; i < doc.getAmount().length; ++i){
 					if(doc.getAmount()[i] != null && !doc.getAmount()[i].isEmpty()){
 						try{
 							Integer.parseInt(doc.getAmount()[i]);
 							jdbcTemplate.update(sql, doc.getAmount()[i], doc.getId(), doc.getProdId()[i]);
 						}catch(NumberFormatException e){
 							e.printStackTrace();
 						}
 					}
 				}
 				
 			}
 			
 		}catch(DataAccessException e){
 			e.printStackTrace();
 		}
 		
 	}
 }
