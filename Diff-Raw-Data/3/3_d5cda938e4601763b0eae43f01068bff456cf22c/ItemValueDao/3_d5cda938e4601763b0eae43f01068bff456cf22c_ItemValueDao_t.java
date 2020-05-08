 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package infowall.domain.persistence.sql;
 
 import static org.springframework.dao.support.DataAccessUtils.singleResult;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 import org.joda.time.DateTime;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import infowall.domain.model.ItemRef;
 import infowall.domain.model.ItemValue;
 
 /**
  *
  */
 public class ItemValueDao {
 
     private final JdbcTemplate jdbcTemplate;
     private ObjectMapper mapper;
 
     public ItemValueDao(final DataSource dataSource) {
         jdbcTemplate = new JdbcTemplate(dataSource);
         mapper = new ObjectMapper();
     }
 
     public void insert(ItemValue itemValue) {
 
         jdbcTemplate.update(
                 "INSERT INTO item_value " +
                         "(creation,dashboard_id,data,item_name,last_update,update_count)" +
                         " VALUES (?,?,?,?,?,?)",
 
                 toTimestamp(itemValue.getCreation()),
                 itemValue.getItemRef().getDashboardId(),
                 serializeData(itemValue),
                 itemValue.getItemRef().getItemName(),
                 toTimestamp(itemValue.getLastUpdate()),
                 itemValue.getUpdateCount());
     }
 
     public void update(ItemValue itemValue) {
         jdbcTemplate.update(
                 "UPDATE item_value SET " +
                         "data = ?, last_update = ?, update_count = ? " +
                         "WHERE id = ?",
                 serializeData(itemValue),
                 toTimestamp(itemValue.getLastUpdate()),
                 itemValue.getUpdateCount(),
                 itemValue.getId()
         );
     }
 
     public ItemValue findMostRecent(ItemRef itemRef) {
         return singleResult(jdbcTemplate.query(
                 "select t.id,t.creation,t.dashboard_id,t.item_name,t.data,t.last_update,t.update_count from " +
                         "item_value t," +
                         "(select dashboard_id,item_name, max(last_update) as last_update " +
                         "   from item_value " +
                         // "   where dashboard_id = ? and item_name = ?" +
                         "   group by dashboard_id,item_name" +
                         ") v " +
                         "where " +
                         "   v.dashboard_id = t.dashboard_id " +
                         "   and v.item_name = t.item_name " +
                         "   and v.last_update = t.last_update " +
                        "   and v.dashboard_id = ? and v.item_name = ?" +
                        "   ORDER BY id DESC LIMIT 0,1",
                 new ItemValueRowMapper(),
                 itemRef.getDashboardId(), itemRef.getItemName()));
     }
 
     public List<ItemValue> findMostRecentItemValues(ItemRef itemRef, int itemCount) {
         return jdbcTemplate.query(
                 "SELECT id,creation,dashboard_id,item_name,data,last_update,update_count " +
                         "FROM item_value " +
                         "WHERE dashboard_id = ? and item_name = ? " +
                         "ORDER BY last_update DESC LIMIT 0,2",
                 new ItemValueRowMapper(),
                 itemRef.getDashboardId(), itemRef.getItemName()
         );
     }
 
     private String serializeData(ItemValue itemValue) {
         try {
             return mapper.writeValueAsString(itemValue.getData());
         } catch (IOException e) {
             throw new DaoException(e);
         }
     }
 
     private ObjectNode deserializeData(String str) {
         try {
             return mapper.readValue(str, ObjectNode.class);
         } catch (IOException e) {
             throw new DaoException(e);
         }
     }
 
     private static Timestamp toTimestamp(DateTime dateTime) {
         return new Timestamp(dateTime.getMillis());
     }
 
     private static DateTime toDateTime(Timestamp ts) {
         return new DateTime(ts.getTime());
     }
 
     private class ItemValueRowMapper implements RowMapper<ItemValue> {
 
         @Override
         public ItemValue mapRow(ResultSet rs, int rowNum) throws SQLException {
             ItemValue val = new ItemValue();
             val.setId(rs.getLong(1));
             val.setCreation(toDateTime(rs.getTimestamp(2)));
 
             ItemRef itemRef = new ItemRef(
                     rs.getString(3),
                     rs.getString(4)
             );
             val.setItemRef(itemRef);
 
             val.setData(deserializeData(rs.getString(5)));
             val.setLastUpdate(toDateTime(rs.getTimestamp(6)));
             val.setUpdateCount(rs.getInt(7));
 
             return val;
         }
     }
 }
