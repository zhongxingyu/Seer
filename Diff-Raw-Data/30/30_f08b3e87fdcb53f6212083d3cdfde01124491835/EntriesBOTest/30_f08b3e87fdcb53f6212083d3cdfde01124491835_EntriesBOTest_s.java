 package com.soebes.casestudy;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.google.common.base.Joiner;
 import com.soebes.casestudy.bo.EntriesBO;
 import com.soebes.casestudy.dao.DAOFactory;
 import com.soebes.casestudy.dao.IdDAO;
 
 public class EntriesBOTest extends BOTestBase {
 	private static Logger LOGGER = Logger.getLogger(EntriesBOTest.class);
 
 	@BeforeClass
 	public void beforeClass() {
 		LOGGER.debug("beforeClass()");
 
 		LOGGER.debug("beforeClass(done)");
 	}
 
 	@Test(enabled = true)
 	public void testGet() {
 		IdDAO<EntriesBO> dao = DAOFactory.getEntries();
 		List<EntriesBO> resultList = dao.get();
 		LOGGER.info("Number of entries:" + resultList.size());
 		for (EntriesBO entriesBO : resultList) {
 			StringBuilder sb = new StringBuilder("Id:" + entriesBO.getId()
					+ " time:" + entriesBO.getTimestamp() + " Title:"
					+ entriesBO.getTitle());
 			sb.append(" Categories:"
 					+ Joiner.on(',').join(entriesBO.getCategories()));
 
 			LOGGER.info(sb.toString());
 		}
 	}
 
 }
