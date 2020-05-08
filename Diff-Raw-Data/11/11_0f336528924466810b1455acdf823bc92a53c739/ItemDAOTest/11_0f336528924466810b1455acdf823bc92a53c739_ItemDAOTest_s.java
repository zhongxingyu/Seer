 package com.tda.persistence;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.tda.model.Category;
 import com.tda.model.Item;
 import com.tda.model.MeasureUnit;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/META-INF/spring-persistence.xml"})
 public class ItemDAOTest {
 	@Autowired
 	ItemDAO itemDao;
 	
 	@Test
 	public void testItemSave() {
 		Item item = new Item();
 		item.setId(1L);
 		item.setCategory(Category.misc);
 		item.setMeasureUnit(MeasureUnit.lts);
 		item.setQuantity(10L);
 		item.setName("Gasoil");
 		
 		itemDao.save(item);
 		
 		Item aItem = itemDao.findById(item.getId());
 		
 		assertEquals(item.getId(), aItem.getId());
 		assertEquals(item.getName(), aItem.getName());
 		assertEquals(item.getCategory(), aItem.getCategory());
 	}
 }
