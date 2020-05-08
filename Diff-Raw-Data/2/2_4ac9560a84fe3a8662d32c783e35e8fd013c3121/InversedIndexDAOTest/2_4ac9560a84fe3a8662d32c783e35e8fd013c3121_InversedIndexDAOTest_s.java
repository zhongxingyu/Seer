 package com.dt.analyzer.dao.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.dt.analyzer.dao.InversedIndexDAO;
 import com.dt.analyzer.entity.TokenProfile;
 
 public class InversedIndexDAOTest {
 
 	InversedIndexDAO iid = null;
 	
 	@Before
 	public void init(){
 		iid = new InversedIndexDAO();
 	}
 	
 	@Test
 	public void testCreateIndex(){
 		iid.createIndex("南大附近有什么好吃的", "123123");
 	}
 	
 	@Test
 	public void testGetTokenProfile(){
 		iid.createIndex("南大附近有什么好吃的", "123123");
 		iid.createIndex("小粉桥附近有什么好吃的", "123123");
 		iid.createIndex("南大有没有漂亮妹子？", "123123");
 		TokenProfile profile = iid.getDocs("南大");
 		assertNotNull(profile);
 		assertEquals(profile.getFreq(), 2);
 	}
 	
 	@Test
 	public void testRemoveToken(){
 		iid.createIndex("南大附近有什么好吃的", "123123");
 		iid.removeToken("南大");
		iid.
 	}
 	
 	@After
 	public void clear(){
 		iid.clearIndex();
 		iid.release();
 	}
 	
 }
