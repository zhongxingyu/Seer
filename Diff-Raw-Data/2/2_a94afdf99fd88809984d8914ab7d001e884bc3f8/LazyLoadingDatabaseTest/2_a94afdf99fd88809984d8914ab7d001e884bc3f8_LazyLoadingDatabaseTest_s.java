 package org.noorg.orientdb.test;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.noorg.orientdb.test.domain.Item;
 import org.noorg.orientdb.test.domain.ItemContainer;
 
 import com.orientechnologies.orient.core.db.object.ODatabaseObjectTx;
 import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
 
 public class LazyLoadingDatabaseTest {
 
 	@BeforeClass
 	public static void setup() throws IOException {
 		Database.setupDatabase();
 
 		// create first item container
 		ItemRepository itemRepository = new ItemRepository();
 		ItemContainer container = itemRepository.createItemContainer("container");
 		container.getItems().add(itemRepository.createItem("foo"));
 		container.getItems().add(itemRepository.createItem("bar"));
 		container.getItems().add(new Item("baz"));
 
 		ODatabaseObjectTx db = Database.get();
 		db.save(container);
 		db.close();
 
		// create second item container with repository
 		ItemContainer container2 = itemRepository.createItemContainer("container 2");
 		container2.getItems().add(itemRepository.createItem("a"));
 		container2.getItems().add(itemRepository.createItem("b"));
 		container2.getItems().add(itemRepository.createItem("c"));
 		
 		db = Database.get();
 		db.save(container2);
 		db.close();
 	}
 
 	@Test
 	public void testFindFlat() {
 		ODatabaseObjectTx db = Database.get();
 		List<ItemContainer> result = db.query(new OSQLSynchQuery<ItemContainer>("select from ItemContainer"));
 		db.close();
 		Assert.assertEquals(2, result.size());
 		for (ItemContainer c : result) {
 			System.out.println(c.getTitle());
 		}
 	}
 
 	@Test
 	public void testFindDeep() {
 		ODatabaseObjectTx db = Database.get();
 		List<ItemContainer> result = db.query(new OSQLSynchQuery<ItemContainer>("select from ItemContainer").setFetchPlan("*:-1"));
 		db.close();
 		Assert.assertEquals(2, result.size());
 		for (ItemContainer c : result) {
 			for (Item i : c.getItems()) {
 				System.out.println("title: " + i.getTitle());
 			}
 		}
 	}
 
 	@Test
 	public void testFindViaRepository() {
 		ItemRepository itemRepository = new ItemRepository();
 		ItemContainer container = itemRepository.findItemContainerByTitle("container 2");
 		Assert.assertEquals(3, container.getItems().size());
 		for (Item i : container.getItems()) {
 			System.out.println("title: " + i.getTitle());
 		}
 	}
 }
