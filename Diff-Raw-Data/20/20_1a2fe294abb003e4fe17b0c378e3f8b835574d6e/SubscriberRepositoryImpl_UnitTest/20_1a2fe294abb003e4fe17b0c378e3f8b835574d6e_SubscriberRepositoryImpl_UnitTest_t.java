 package com.raitongorganicsfarm.app.mb.repository;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
import java.util.Collections;
import java.util.List;
 import java.util.Random;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.raitongorganicsfarm.app.mb.entity.Subscriber;
 
 public class SubscriberRepositoryImpl_UnitTest {
 
 	private SubscriberRepositoryImpl subscriberRepository;
 	
 	@Before
 	public void before() {
 		this.subscriberRepository = new SubscriberRepositoryImpl();
 	}
 	
 	@Test
 	public void first_customer_should_get_customer_no_CSA0001() {
 		MongoTemplate mongoTemplate = mock(MongoTemplate.class);
 		Query query = SubscriberQueryHelper.lastestSubscriberQuery();
 		when(mongoTemplate.findOne(query, Subscriber.class)).thenReturn(null);
 		this.subscriberRepository.setMongoTemplate(mongoTemplate);
 		String c = this.subscriberRepository.nextCustomerNumber();
 		assertEquals("CSA0001", c);
 	}
 	
 	@Test
 	public void next_customer_should_get_customer_no_that_increase_from_lastest_by_one() {
 		MongoTemplate mongoTemplate = mock(MongoTemplate.class);
 		Query query = SubscriberQueryHelper.lastestSubscriberQuery();
 		Subscriber s = new Subscriber();
 		
		int randInt = Math.abs(new Random().nextInt()) % 10000;
 		String seedCustomerNo = String.format("CSA%1$04d", randInt);
 		
 		s.setCustomerNo(seedCustomerNo);
		List<Subscriber> list = Collections.singletonList(s);
		when(mongoTemplate.find(query, Subscriber.class)).thenReturn(list);
 		this.subscriberRepository.setMongoTemplate(mongoTemplate);
 		String c = this.subscriberRepository.nextCustomerNumber();
 		String expectCustomerNo = String.format("CSA%1$04d", randInt + 1);
 		assertEquals(expectCustomerNo, c);
 	}
 }
