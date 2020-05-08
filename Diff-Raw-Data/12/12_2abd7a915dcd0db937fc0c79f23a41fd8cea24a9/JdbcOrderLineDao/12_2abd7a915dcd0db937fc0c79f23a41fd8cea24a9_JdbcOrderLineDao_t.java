 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.dip.komusubi.lunch.module.dao.jdbc;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.sql.DataSource;
 
 import jp.dip.komusubi.lunch.LunchException;
 import jp.dip.komusubi.lunch.model.OrderLine;
 import jp.dip.komusubi.lunch.model.OrderLine.OrderLineKey;
 import jp.dip.komusubi.lunch.module.dao.OrderLineDao;
 import jp.dip.komusubi.lunch.module.dao.ProductDao;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 
 /**
  * order line dao.
  * @author jun.ozeki
  * @since 2011/11/20
  */
 public class JdbcOrderLineDao implements OrderLineDao {
 
     private static final Logger logger = LoggerFactory.getLogger(JdbcOrderLineDao.class);
    private static final String TABLE_NAME = "orderLines";
     private static final String COLUMNS = "orderId, no, productId, quantity, amount, datetime, cancel";
     private static final String SELECT_QUERY_RECORD = "select " + COLUMNS
             + " from orderLines where orderId = ? and userId = ? and productId = ?";
     private static final String INSERT_QUERY_RECORD = "insert into " + TABLE_NAME
             + "( " + COLUMNS + " ) values ( ?, ?, ?, ?, ?, ?, ? )";
     private static final String SELECT_QUERY_BY_ORDERID = "select " + COLUMNS + " from orderLines where orderId = ?";
     private static final String UPDATE_QUERY_RECORD = "update " + TABLE_NAME
             + " set quantity = ?, amount = ? datetime = ? cancel = ? " + "where orderId = ? and no = ?";
     private SimpleJdbcTemplate template;
 //    private NamedParameterJdbcTemplate template;
     @Inject private ProductDao productDao;
 
     /**
      * create new instance.
      * @param dataSource
      */
     @Inject
     public JdbcOrderLineDao(DataSource dataSource) {
         template = new SimpleJdbcTemplate(dataSource);
 //        template = new NamedParameterJdbcTemplate(dataSource);
     }
     
     /**
      * create new instance.
      * @param dataSource
      * @param productDao
      */
     public JdbcOrderLineDao(DataSource dataSource, ProductDao productDao) {
         this(dataSource);
         this.productDao = productDao;
     }
 
     /**
      * find by primary key.
      */
     @Override
     public OrderLine find(OrderLineKey pk) {
         OrderLine orderLine = null;
         try {
             orderLine = template.queryForObject(SELECT_QUERY_RECORD, orderLineRowMapper, 
                                 pk.getNo(), 
                                 pk.getOrderId());
         } catch (EmptyResultDataAccessException e) {
             logger.info("not found order line: {}", pk);
         }
         return orderLine;
     }
 
     /**
      * find all.
      */
     @Override
     public List<OrderLine> findAll() {
         throw new UnsupportedOperationException("findAll");
     }
 
     /**
      * find by order id.
      */
     @Override
     public List<OrderLine> findByOrderId(int orderId) {
         List<OrderLine> orderLines = template.query(SELECT_QUERY_BY_ORDERID, orderLineRowMapper, orderId);
         logger.info("find order line, orderId[{}]:{}", orderId, orderLines.size());
         return orderLines;
     }
 
     /**
      * persist a order line.
      */
     @Override
     public OrderLineKey persist(OrderLine instance) {
         try {
             template.update(INSERT_QUERY_RECORD, instance.getPrimaryKey().getOrderId(),
                                                  instance.getPrimaryKey().getNo(), 
                                                  instance.getProduct().getId(), 
                                                  instance.getQuantity(), 
                                                  instance.getAmount(), 
                                                  instance.getDatetime(), 
                                                  instance.isCancel());
         } catch (DataAccessException e) {
             throw new LunchException(e);
         }
         return instance.getPrimaryKey();
     }
 
     /**
      * remove a order line.
      */
     @Override
     public void remove(OrderLine instance) {
         throw new UnsupportedOperationException("remove");
     }
 
     /**
      * update a order line.
      */
     @Override
     public void update(OrderLine instance) {
         try {
             template.update(UPDATE_QUERY_RECORD, instance.getPrimaryKey().getOrderId(), 
                                                  instance.getPrimaryKey().getNo(), 
                                                  instance.getProduct().getId(), 
                                                  instance.getQuantity(), 
                                                  instance.getAmount(), 
                                                  instance.getDatetime(), 
                                                  instance.isCancel());
         } catch (DataAccessException e) {
             throw new LunchException(e);
         }
     }
 
     /**
      * row mapper.
      */
     private final RowMapper<OrderLine> orderLineRowMapper = new RowMapper<OrderLine>() {
 
         @Override
         public OrderLine mapRow(ResultSet rs, int rowNum) throws SQLException {
             OrderLineKey primaryKey = new OrderLineKey(
                                             rs.getInt("no"),
                                             rs.getInt("orderId"));
             
             OrderLine orderLine = new OrderLine(primaryKey)
                                         .setProduct(productDao.find(rs.getString("productId"))) 
                                         .setQuantity(rs.getInt("quantity")) 
                                         .setDatetime(rs.getDate("datetime")) 
                                         .setCancel(rs.getBoolean("cancel"));
             return orderLine;
         }
     };
 
 }
