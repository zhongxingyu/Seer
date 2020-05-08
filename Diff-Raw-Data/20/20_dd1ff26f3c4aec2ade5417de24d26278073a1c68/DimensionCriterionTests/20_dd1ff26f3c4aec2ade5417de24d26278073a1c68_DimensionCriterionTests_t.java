 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.data.tests.warehouse.query.jdbc.pivot;
 
 import junit.framework.TestCase;
 
 import org.hibernate.dialect.MySQL5Dialect;
 
 import com.evinceframework.data.tests.warehouse.TestData;
 import com.evinceframework.data.tests.warehouse.query.jdbc.pivot.PivotJdbcQueryCommandTests.TestPivotQueryCommand;
 import com.evinceframework.data.warehouse.query.DimensionCriterion;
 import com.evinceframework.data.warehouse.query.FactRangeCriterion;
 import com.evinceframework.data.warehouse.query.FactSelection;
 import com.evinceframework.data.warehouse.query.PivotQuery;
 import com.evinceframework.data.warehouse.query.QueryException;
 import com.evinceframework.data.warehouse.query.SummarizationAttribute;
 import com.evinceframework.data.warehouse.query.impl.FactSelectionImpl;
 
 public class DimensionCriterionTests extends TestCase {
 
 	public void test_QueryEngine() throws QueryException {
 		
 		TestPivotQueryCommand queryEngine = new TestPivotQueryCommand(new MySQL5Dialect());
 		
 		PivotQuery query = new PivotQuery(TestData.factTable, 
 			new FactSelection[] {
 				new FactSelectionImpl(TestData.simpleIntegerFact)
 			},
 			new DimensionCriterion[]{
 				new DimensionCriterion<String>(TestData.dimensionA, TestData.dimensionalAttrA1, "testValue")
 			},
 			new FactRangeCriterion[]{},
 			new SummarizationAttribute[]{}
 		);
 		query.setMaximumRowCount(null);
 		
 		String sql = queryEngine.generateSqlForTest(query);
 		
 		assertNotNull(sql);
		assertEquals("select fact.simpleInteger as fact_simpleInteger from fooTable fact inner join dimA dim_0 on dimA1_id=dim_0.dimA_id where dim_0.attr1 = ?", sql);
 	}
 	
 }
