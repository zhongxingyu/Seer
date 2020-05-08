 /*
  * Copyright 2013 Webster Smalley
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.webstersmalley.countdown.db;
 
 import com.webstersmalley.countdown.words.WordBean;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
 import org.springframework.jdbc.core.namedparam.SqlParameterSource;
 import org.springframework.stereotype.Service;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 @Service("countdownDao")
 public class JdbcCountdownDao implements CountdownDao {
     private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
 
     private class WordRowMapper implements RowMapper {
         public WordBean mapRow(ResultSet rs, int rowNum) throws SQLException {
             return new WordBean(rs.getString("theWord"));
         }
     }
 
    @Autowired
     public void setDataSource(DataSource dataSource) {
         this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
     }
 
     @Override
     public void addWord(WordBean word) {
         String sql = "insert into words (theWord, length, countA, countB, countC, countD, countE, countF, countG, countH, countI, countJ, countK, countL, countM, countN, countO, countP, countQ, countR, countS, countT, countU, countV, countW, countX, countY, countZ) values (:theWord, :length, :countA, :countB, :countC, :countD, :countE, :countF, :countG, :countH, :countI, :countJ, :countK, :countL, :countM, :countN, :countO, :countP, :countQ, :countR, :countS, :countT, :countU, :countV, :countW, :countX, :countY, :countZ)";
 
         SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(word);
 
        System.out.println(namedParameterJdbcTemplate);
         namedParameterJdbcTemplate.update(sql, namedParameters);
     }
 
     @Override
     public List<WordBean> getMatches(String input) {
         WordBean word = new WordBean(input);
         RowMapper mapper = new WordRowMapper();
         String sql = "select theWord from words where length <= :length and countA <= :countA and  countB <= :countB and countC <= :countC and countD <= :countD and countE <= :countE and countF <= :countF and countG <= :countG and countH <= :countH and countI <= :countI and countJ <= :countJ and countK <= :countK and countL <= :countL and countM <= :countM and countN <= :countN and countO <= :countO and countP <= :countP and countQ <= :countQ and countR <= :countR and countS <= :countS and countT <= :countT and countU <= :countU and countV <= :countV and countW <= :countW and countX <= :countX and countY <= :countY and countZ <= :countZ";
         SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(word);
        System.out.println(namedParameterJdbcTemplate);
         return namedParameterJdbcTemplate.query(sql, namedParameters, mapper);
     }
 }
