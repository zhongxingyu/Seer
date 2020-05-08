 package com.tort.trade.model;
 
 import org.testng.annotations.Test;
 
 @Test
 public class SalesAliasEntityTest  extends EntityTest {
 	public void save(){
		Sales sales = new Sales(1L, "testSales");
 		
 		_session.save(sales);
 		
 		SalesAlias alias = new SalesAlias();
 		alias.setId("$");
 		alias.setSales(sales);
 		
 		_session.save(alias);
 		_session.flush();
 	}
 }
