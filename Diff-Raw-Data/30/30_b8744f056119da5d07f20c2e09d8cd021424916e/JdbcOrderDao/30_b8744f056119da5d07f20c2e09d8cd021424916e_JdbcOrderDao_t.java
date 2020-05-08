 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package jp.dip.komusubi.lunch.module.dao.jdbc;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.sql.DataSource;
 
 import jp.dip.komusubi.lunch.LunchException;
 import jp.dip.komusubi.lunch.model.Order;
 import jp.dip.komusubi.lunch.model.OrderLine;
 import jp.dip.komusubi.lunch.model.OrderLine.OrderLineKey;
 import jp.dip.komusubi.lunch.module.dao.GroupDao;
 import jp.dip.komusubi.lunch.module.dao.OrderDao;
 import jp.dip.komusubi.lunch.module.dao.OrderLineDao;
 import jp.dip.komusubi.lunch.module.dao.ShopDao;
 import jp.dip.komusubi.lunch.module.dao.UserDao;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.jdbc.support.GeneratedKeyHolder;
 
 /**
  * jdbc order dao.
  * @author jun.ozeki
  * @since 2011/11/26
  */
 public class JdbcOrderDao implements OrderDao {
 	
 	private static final Logger logger = LoggerFactory.getLogger(JdbcOrderDao.class);
 	private static String COLUMNS = "id, userId, groupId, shopId, amount, geoId, summary, cancel, datetime";
 	private static final String INSERT_QUERY = "insert into orders ( " + COLUMNS + " ) values " +
 			"(:id, :userId, :groupId, :shopId, :amount, :geoId, :summary, :cancel, :datetime)";
 	private static final String SELECT_QUERY_BY_USER = "select " + COLUMNS + " from orders where userId = :userId";
 	private static final String SELECT_QUERY_BY_USER_AND_DATE = "select " + COLUMNS + " from orders where userId = :userId and date(datetime) = :datetime";
 	private static final String SELECT_QUERY_BY_GROUP_AND_DATE = "select " + COLUMNS + " from orders " +
 			"where groupId = :groupId and date(datetime) = :datetime and summary = :summary";
 	private static final String UPDATE_QUERY = "update order set userId = :userId, groupId = :groupId, " +
 			"shopId = :shopId, amount = :amount, geoId = :geoId, summary = :summary, cancel = :cancel, datetime = :datetime where id = :id";
 	private NamedParameterJdbcTemplate template;
 	@Inject	private OrderLineDao orderLineDao;
 	@Inject	private ShopDao shopDao;
 	@Inject	private UserDao userDao;
 	@Inject private GroupDao groupDao;
 	
 	@Inject
 	public JdbcOrderDao(DataSource dataSource) {
 	    // get auto increment value 
 	    this.template = new NamedParameterJdbcTemplate(dataSource);
 	}
 	
 	public Order find(Integer pk) {
 		throw new UnsupportedOperationException("find(pk)");
 	}
 
 	public List<Order> findAll() {
 		throw new UnsupportedOperationException("findAll()");
 	}
 	
 	public Integer persist(Order instance) {
 	    GeneratedKeyHolder holder = new GeneratedKeyHolder();
 		try {
 		    MapSqlParameterSource sqlParameter = buildSqlParameterSource(instance); 
 		    template.update(INSERT_QUERY, sqlParameter, holder); 
 		    
 		    int i = 1;
 			for (OrderLine o: instance) {
 			    OrderLineKey primaryKey = new OrderLineKey(holder.getKey().intValue(), i++);
 			    o.setOrderLineKey(primaryKey);
 				orderLineDao.persist(o);
 			}
 			logger.info("persisted: {}", instance);
 		} catch (DataAccessException e) {
 			throw new LunchException(e);
 		}
 		// return to auto boxing 
 		return holder.getKey().intValue();
 	}
 
 	public void remove(Order instance) {
 		throw new UnsupportedOperationException("#remove not supported");		
 	}
 
	/**
	 * update order.
	 */
 	public void update(Order instance) {
 	    MapSqlParameterSource sqlParameter = buildSqlParameterSource(instance);
	    for (OrderLine orderLine: instance) {
    	    orderLineDao.update(orderLine);
        }
 	    int affect = template.update(UPDATE_QUERY, sqlParameter);
 	    logger.info("JdbcOrderDao#update affect:{}", affect);
 	}
 
 	private MapSqlParameterSource buildSqlParameterSource(Order instance) {
         MapSqlParameterSource sqlParameter = new MapSqlParameterSource()
                                                     .addValue("id", instance.getId())
                                                     .addValue("userId", instance.getUser().getId())
                                                     .addValue("groupId", instance.getUser().getGroupId())
                                                     .addValue("shopId", instance.getShop().getId())
                                                     .addValue("amount", instance.getAmount())
                                                     .addValue("geoId", null)
                                                     .addValue("summary", instance.isSummary())
                                                     .addValue("cancel", instance.isCancel())
                                                     .addValue("datetime", instance.getDatetime());
         return sqlParameter;
 	}
 	
 //	public List<Order> findByDate(Date date) {
 //		List<Order> list;
 //		list = template.query(SELECT_QUERY_ORDER_DATE, orderRowMap, date);
 //		logger.info("findByDate({}): {}", date, list.size());
 //		return list;
 //	}
 
 //	@Override
 //	public Order findByUnique(String groupId, String shopId, Date orderDate) {
 //		Order order = null;
 //		try {
 //			if (orderDate == null)
 //				order = template.queryForObject(SELECT_QUERY_BY_NOT_ORDRED, orderRowMap, groupId, shopId);
 //			else
 //				order = template.queryForObject(SELECT_QUERY_BY_UNIQUE, orderRowMap, groupId, shopId, orderDate); 
 //		} catch (EmptyResultDataAccessException e) {
 //			logger.info("not found order by groupId:{}, shopId:{}, orderDate:{}", 
 //					new Object[]{groupId, shopId, orderDate});
 //		}
 //		
 //		return order;
 //	}
 	
 	@Override
 	public List<Order> findByUserAndDate(Integer userId, Date date) {
         MapSqlParameterSource sqlParameter = new MapSqlParameterSource()
                                         .addValue("userId", userId)
                                         .addValue("datetime", JdbcDateConverter.toSqlDate(date));
 	    
 	    List<Order> orders = template.query(SELECT_QUERY_BY_USER_AND_DATE, sqlParameter, orderRowMapper);
 	    logger.info("findByUserAndDate count:{}", orders.size());
 	    return orders;
 	}
 
 	@Override
 	public List<Order> findByUser(Integer userId) {
 //		List<Order> order = template.query(SELECT_QUERY_BY_USER, orderRowMapper, userId);
 	    List<Order> orders = template.query(SELECT_QUERY_BY_USER, new MapSqlParameterSource().addValue("userId", userId), orderRowMapper);
 		logger.info("findByUser userId:{}, count:{}", userId, orders.size());
 		return orders;
 	}
 
 	@Override
 	public List<Order> findByProduct(String productId) {
 		throw new UnsupportedOperationException("JdbcOrderDao#findByProduct");
 	}
 
 	@Override
 	public List<Order> findByUserAndProductAndDate(Integer userId, String productId, Date date) {
 		throw new UnsupportedOperationException("JdbcOrderDao#findByUserAndProductAndDate");
 	}
 
 	@Override
 	public List<Order> findByGroupId(String groupId) {
 		throw new UnsupportedOperationException("JdbcOrderDao#findByGroupId");
 	}
 
 	@Override
 	public List<Order> findByGroupIdAndDate(Integer groupId, Date orderDate, boolean summary) {
 	    MapSqlParameterSource sqlParameter = new MapSqlParameterSource()
 	                                        .addValue("groupId", groupId)
 	                                        .addValue("datetime", orderDate)
 	                                        .addValue("summary", summary);
 	    List<Order> orders = template.query(SELECT_QUERY_BY_GROUP_AND_DATE, sqlParameter, orderRowMapper);
 	    logger.info("findByGroupIdAndDate count:{}", orders.size());
 	    return orders;
 	}
 	
 	private RowMapper<Order> orderRowMapper = new RowMapper<Order>() {
 		
 		@Override
 		public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
 			Order order = new Order(rs.getInt("id"))
                         			.setUser(userDao.find(rs.getInt("userId")))
                         			.setGroup(groupDao.find(rs.getInt("groupID")))
                         			.setShop(shopDao.find(rs.getString("shopId")))
                         			.setAmount(rs.getInt("amount"))
                         			.addLines(orderLineDao.findByOrderId(rs.getInt("id")))
                         			.setSummary(rs.getBoolean("summary"))
                         			.setCancel(rs.getBoolean("cancel"))
                         			.setDatetime(rs.getDate("datetime"));
 			return order;
 		}
 		
 	};
 	
 }
