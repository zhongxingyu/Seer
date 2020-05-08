 package com.sk.domain.dao;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.sk.domain.Shopper;
 import com.sk.util.BaseIntegration;
 import com.sk.util.builder.ShopperBuilder;
 
 
 public class ShopperDaoTest extends BaseIntegration{
 
 	@Autowired private ShopperDao shopperDao;
 	
 	@Test
 	public void shouldFindByEmail(){
		Shopper persistShopper = new ShopperBuilder().email("default@default.com").persist(getSession());
 		flushAndClear();
 
		Shopper found = shopperDao.findByEmail("default@default.com");
 		
 		assertThat(found, equalTo(persistShopper));
 	}
 	
 }
