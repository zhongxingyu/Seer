 /**
  *  Copyright 2011 Chris Barrett
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.cbarrett.scotchservices.lcbo.db.mapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.cbarrett.scotchservices.lcbo.domain.Product;
 import org.cbarrett.scotchservices.lcbo.domain.Product.StockType;
 import org.joda.time.DateTime;
 import org.springframework.jdbc.core.ResultSetExtractor;
 
 public class LCBOProductExtractor implements ResultSetExtractor<Product> {
 	@Override
 	public Product extractData(ResultSet rs) throws SQLException {
 		Product prod = new Product();
 		prod.setId(rs.getString(1));
 		prod.setProducer_name(rs.getString(2));
 		prod.setName(rs.getString(3));
 		prod.setDescription(rs.getString(4));
 		prod.setAlcohol_content(rs.getInt(5));
 		prod.setIs_dead(rs.getBoolean(6));
 		prod.setIs_discontinued(rs.getBoolean(7));
		
		if (rs.getString(8) != null) {
			prod.setStock_type(StockType.valueOf(rs.getString(8)));
		}
 		prod.setPrice_in_cents(rs.getInt(9));
 		prod.setOrigin(rs.getString(10));
 		prod.setPrimary_category(rs.getString(11));
 		prod.setSecondary_category(rs.getString(12));
		
		if (rs.getTimestamp(13) != null) {
			prod.setReleasedOn(new DateTime((rs.getTimestamp(13)).getTime()));
		}
 		prod.setInventory_count(rs.getInt(14));
		if (rs.getTimestamp(15) != null) {
			prod.setUpdatedAt(new DateTime((rs.getTimestamp(15)).getTime()));
		}
 		
 		return prod;
 	}
 }
