 package org.smartsnip.core;
 
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PersistenceTest {
 	@Before
 	public void setUp() throws Exception {
 		// TODO: Disable persistence layer!!
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * This test creates some users and checks if the users are actually created
 	 */
 	@Test
 	public void createUsers() {
 		List<String> addedUsers = new ArrayList<String>();
 		try {
 
 			final int usercount = 10;
 
 			for (int i = 0; i < usercount; i++) {
 				String name = "user" + i;
 				User user = User.createNewUser(name, "password", "email@mail.com");
 				if (User.getUser(name) != user) {
					fail("Userget: " + name);
 				}
 				addedUsers.add(name);
 			}
 			if (User.totalCount() != usercount) {
 				fail("Usercount delta " + (User.totalCount() - usercount));
 			}
 
 		} finally {
 			// Cleanup
 			for (String user : addedUsers) {
 				User.deleteUser(user);
 			}
 
 			if (User.totalCount() != 0) {
 				fail("User database not empty");
 			}
 		}
 	}
 
 	/**
 	 * This test creates some categories and checks if they really are created
 	 */
 	@Test
 	public void createCategories() {
 		List<String> addedCategories = new ArrayList<String>();
 
 		try {
 			final int roots = 10;
 			final int children = 4;
 
 			for (int i = 0; i < roots; i++) {
 				String name = "Category" + i;
 				Category root = Category.createCategory(name, "Testcategory " + i + " root", null);
 				if (Category.getCategory(name) != root) {
 					fail("Category get: " + name);
 				}
 				addedCategories.add(name);
 				for (int x = 0; x < children; x++) {
 					name = "Category" + i + "." + x;
 					Category child = Category.createCategory(name, "Testcategory " + i + " children " + x + " - Desc",
 							root);
 					if (Category.getCategory(name) != child) {
 						fail("Category get: " + name);
 					}
 					addedCategories.add(name);
 				}
 
 			}
 			int count = roots * (children + 1);
 			// TODO: Check categories count
 			if (count != Category.totalCount()) {
 				fail("Category count failure. Delta = " + (count - Category.totalCount()));
 			}
 		} finally {
 			for (String name : addedCategories) {
 				Category.deleteCategory(name);
 			}
 			if (Category.totalCount() != 0) {
 				fail("Category cleanup failed. Delta = " + Category.totalCount());
 			}
 		}
 	}
 }
