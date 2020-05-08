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
 import java.sql.Timestamp;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.sql.DataSource;
 
 import jp.dip.komusubi.lunch.LunchException;
 import jp.dip.komusubi.lunch.model.Product;
 import jp.dip.komusubi.lunch.module.dao.ProductDao;
 import jp.dip.komusubi.lunch.module.dao.ShopDao;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 /**
  * product data access object jdbc implement.
  * @author jun.ozeki
  * @since 2011/12/29
  */
 public class JdbcProductDao implements ProductDao {
 
 	private static final Logger logger = LoggerFactory.getLogger(JdbcProductDao.class);
 	private static final String COLUMNS = "id, refId, shopId, name, amount, start, finish";
 	private static final String SELECT_RECORDS_SHOPID = "select " + COLUMNS + " from products where shopId = ?";
	private static final String SELECT_RECORD_PK = "select " + COLUMNS + " from products where id = ?";
 	private static final String SELECT_RECORDS_SHOPID_SALABLE = "select " + COLUMNS 
 											+ " from products where shopId = ? and start <= ? and finish >= ?";
 	private static final String SELECT_RECORDS_SHOPID_FINISH_DATE = "select " + COLUMNS
 											+ " from products where shopId = ? and date(finish) = ?";
 	private static final String SELECT_RECORDS_SHOPID_FINISH_DATETIME = "select " + COLUMNS
 											+ " from products where shopId = ? and date(finish) = ? and finish >= ?";
 	private static final String INSERT_QUERY = "insert into products ( " + COLUMNS + " )"
 											+ " values (?, ?, ?, ?, ?, ?, ?)";
 	@Inject private ShopDao shopDao;
 	private SimpleJdbcTemplate template;
 	
 	@Inject
 	public JdbcProductDao(DataSource dataSource) {
 		this.template = new SimpleJdbcTemplate(dataSource);
 	}
 	
 	@Override
 	public Product find(String pk) {
 		Product product = null;
 		product = template.queryForObject(SELECT_RECORD_PK, productRowMapper, pk);
 		return product;
 	}
 
 	@Override
 	public List<Product> findAll() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String persist(Product instance) {
 		try {
 			if (Product.DEFAULT_ID.equals(instance.getId()))
 				throw new IllegalArgumentException("product id is " + Product.DEFAULT_ID);
 			template.update(INSERT_QUERY, instance.getId(),
 											instance.getRefId(),
 											instance.getShopId(),
 											instance.getName(),
 											instance.getAmount(),
 											instance.getStart(),
 											instance.getFinish());
 		} catch (DataAccessException e) {
 			throw new LunchException(e);
 		}
 		return instance.getId();
 	}
 
 	@Override
 	public void remove(Product instance) {
 		throw new UnsupportedOperationException("#remove");
 	}
 
 	@Override
 	public void update(Product instance) {
 		throw new UnsupportedOperationException("#update");
 	}
 
 	@Override
 	public List<Product> findByShopId(String shopId) {
 		List<Product> list;
 		try {
 			list = template.query(SELECT_RECORDS_SHOPID, productRowMapper, shopId);
 		} catch (EmptyResultDataAccessException e) {
 			list = Collections.emptyList();
 		}
 		return list;
 	}
 	
 	@Override
 	public List<Product> findBySalable(String shopId, Date date) {
 		List<Product> list;
 		try {
 			Timestamp datetime = new Timestamp(date.getTime());
 			list = template.query(SELECT_RECORDS_SHOPID_SALABLE, productRowMapper, shopId, datetime, datetime);
 		} catch (EmptyResultDataAccessException e) {
 			list = Collections.emptyList();
 		}
 		return list;
 	}
 	
 	@Override
 	public List<Product> findByShopIdAndFinishDate(String shopId, Date finishDate) {
 		List<Product> list = template.query(SELECT_RECORDS_SHOPID_FINISH_DATE,
 								productRowMapper, shopId, JdbcDateConverter.toSqlDate(finishDate));
 		logger.info("shopId:{}, finishDay:{}, count:{}", new Object[]{shopId, finishDate, list.size()});
 		return list;
 	}
 	
 	@Override
 	public List<Product> findByShopIdAndFinishDatetime(String shopId, Date finishDate) {
 		List<Product> list = template.query(SELECT_RECORDS_SHOPID_FINISH_DATETIME,
 								productRowMapper, shopId,
 								JdbcDateConverter.toSqlDate(finishDate),
 								JdbcDateConverter.toTimestamp(finishDate));
 		logger.info("shopId:{}, finishDate:{}, count:{}", new Object[]{shopId, finishDate, list.size()});
 		return list;
 	}
 	
 	private RowMapper<Product> productRowMapper = new RowMapper<Product>() {
 		@Override
 		public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
 
 			Product product = new Product(rs.getString("id"))
 											.setRefId(rs.getString("refId"))
 //											.setShopId(rs.getString("shopId"))
 											.setShop(shopDao.find(rs.getString("shopId")))
 											.setName(rs.getString("name"))
 											.setAmount(rs.getInt("amount"))
 											.setStart(rs.getDate("start"))
 											.setFinish(rs.getDate("finish"));
 											
 			return product;
 		}
 	};
 
 }
