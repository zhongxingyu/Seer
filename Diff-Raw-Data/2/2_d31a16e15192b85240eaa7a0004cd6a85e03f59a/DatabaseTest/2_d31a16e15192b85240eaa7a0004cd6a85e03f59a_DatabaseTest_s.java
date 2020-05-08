 package com.mima.db;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.mima.db.dao.StrasseDao;
 import com.mima.db.dao.exception.DAOException;
 import com.mima.db.daofactory.DAOFactory;
 import com.mima.db.model.Strasse;
 
 public class DatabaseTest {
 
 	private StrasseDao dao;
 
 	@Before
 	public void initialize() {
 		DAOFactory daof = DAOFactory.getInstance();
 		dao = daof.getStrasseDao();
 	}
 
 	@Test
 	public void testPaketStatus() throws DAOException {
		List<Strasse> retVal = dao.findStreetsByStartPoint(Long.valueOf(1), Long.valueOf(2));
 		assertNotNull(retVal);
 		assertTrue(retVal.size()>0);
 	}
 
 }
