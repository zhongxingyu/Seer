 package com.tda.service;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.tda.model.Item;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring-service.xml", "classpath:spring-persistence.xml"})
 public class ItemServiceTest {
 	@Autowired
 	ItemService itemService;
 	
 	@Test
 	public void testItemServiceFindByName() {
 		Item item1 = new Item();
 		item1.setName("item1");
 		itemService.save(item1);
 		
 		Item item2 = new Item();
 		item2.setName("item2");
 		itemService.save(item2);
 		
 		Item item3 = new Item();
 		item3.setName("nadaquever");
 		itemService.save(item3);
 		
 		List<Item> items = itemService.findByName("item");
 		
 		assertEquals(items.size(), 2);
 	}
 }
