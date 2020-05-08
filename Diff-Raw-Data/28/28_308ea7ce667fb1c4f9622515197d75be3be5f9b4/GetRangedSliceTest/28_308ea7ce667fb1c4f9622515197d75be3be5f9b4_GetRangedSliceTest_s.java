 package com.mirantis.aminakov.bigdatacourse.dao1;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;

import junit.framework.Assert;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.factory.HFactory;
 import org.apache.log4j.BasicConfigurator;
 import org.junit.Test;
 
 import com.mirantis.aminakov.bigdatacourse.dao1.Book;
 import com.mirantis.aminakov.bigdatacourse.dao1.Constants;
 import com.mirantis.aminakov.bigdatacourse.dao1.DAOApp;
 import com.mirantis.aminakov.bigdatacourse.dao1.DAOException;
 
 public class GetRangedSliceTest {
 
 	@Test
 	public void getRangedSlicesTest(){
 		
 		BasicConfigurator.configure();
 		List<Book> before = new ArrayList<Book>();
 		List<Book> after = new ArrayList<Book>();
 		
 		Cluster clstr = HFactory.getOrCreateCluster(Constants.CLUSTER_NAME, Constants.HOST_DEF+":9160");
 		if(clstr.describeKeyspace(Constants.KEYSPACE_NAME) != null)
 			clstr.dropKeyspace(Constants.KEYSPACE_NAME, true);
 		DAOApp dao = new DAOApp();
 		Book beggining_state = new Book();
 		try {
 			for(int i = 0; i< 40; ++i){
 				
 				beggining_state.newBook(i, new String("CassandraTest" + String.valueOf(i)), new String("Test" + String.valueOf(i)), new String("Tester" + String.valueOf(i)), new FileInputStream("resources/testbook"));
 				dao.addBook(beggining_state);
 			}
 			after = dao.getAllBooks(1, 40);
			Assert.assertFalse(before.equals(after));
 			after = dao.getAllBooks(2, 20);
			Assert.assertFalse(before.equals(after));
 			after = dao.getAllBooks(3, 11);
			Assert.assertFalse(before.equals(after));
 		} catch (FileNotFoundException | DAOException e) {e.printStackTrace();}
 	}
 }
