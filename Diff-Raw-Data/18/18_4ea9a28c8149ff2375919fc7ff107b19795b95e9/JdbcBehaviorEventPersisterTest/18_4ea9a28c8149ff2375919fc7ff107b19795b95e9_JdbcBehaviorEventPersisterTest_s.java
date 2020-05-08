 package com.mtgi.analytics;
 
 import static org.dbunit.dataset.filter.DefaultColumnFilter.excludedColumnsTable;
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.junit.Test;
 import org.unitils.spring.annotation.SpringBeanByType;
 
 public class JdbcBehaviorEventPersisterTest extends JdbcEventTestCase {
 
 	@SpringBeanByType
 	private JdbcBehaviorEventPersisterImpl persister;
 
 	@Test
 	public void testEmptyQueue() throws SQLException {
 		assertEquals("no events to persist", 0, persister.persist(new LinkedList<BehaviorEvent>()));
 		
 		ResultSet rs = stmt.executeQuery("select count(*) from BEHAVIOR_TRACKING_EVENT");
 		assertTrue(rs.next());
 		assertEquals("no events in db", 0, rs.getInt(1));
 		rs.close();
 	}
 	
 	@Test
 	public void testNestedEvents() throws SQLException, Exception  {
 		//we construct a dataload with important properties for the implementation:
 		// * null and non-null event data
 		// * events with properties and events without
 		// * events with children and events without
 		// * events with errors and events without
 
 		//create a tree of 27 events that cover the properties above.
		LinkedList<BehaviorEvent> events = new LinkedList<BehaviorEvent>();
 		int[] counter = { 0 };
 		for (int i = 0; i < 3; ++i)
 			events.add(createEvent(null, 1, 3, 3, counter));
 
		assertEquals("entire event tree persisted", 39, persister.persist(events));
 
 		//use the dbunit API to do a full check of data contents.  we can't use simple
 		//annotations because we have to exclude variable date columns from comparison.
 		IDatabaseConnection connection = new DatabaseConnection(conn);
         ITable actualTable = connection.createDataSet().getTable("BEHAVIOR_TRACKING_EVENT");
         actualTable = excludedColumnsTable(actualTable, new String[]{"START", "DURATION_MS"});
 
 		InputStream expectedData = JdbcBehaviorEventPersisterTest.class.getResourceAsStream("JdbcBehaviorEventPersisterTest.testNestedEvents-result.xml");
 		FlatXmlDataSet expectedDataSet = new FlatXmlDataSet(expectedData, true);
 		ITable expectedTable = expectedDataSet.getTable("BEHAVIOR_TRACKING_EVENT");
 		
 		expectedTable = excludedColumnsTable(expectedTable, new String[]{"START", "DURATION_MS"});
 
 		org.dbunit.Assertion.assertEquals(expectedTable, actualTable);
 		
 		//because the DBUnit dataset cannot contain static event times and durations,
 		//we do some extra verification of our own here.  we also do sanity checks, like
 		//making sure child events start after their parents and end before them.
 		for (BehaviorEvent e : events)
 			verifyEvent(e);
 	}
 
 	/**
 	 * Recursively verify that correct values for the given behavior event can be found
 	 * in the database.
 	 */
 	private void verifyEvent(BehaviorEvent event) throws SQLException {
 		assertNotNull("event was persisted", event.getId());
 		ResultSet data = getEventData(event.getId());
 
 		if (event.getParent() == null) {
 			assertEquals(0, data.getLong("PARENT_EVENT_ID"));
 			assertTrue("no parent for event " + event.getId(), data.wasNull());
 		} else {
			assertEquals(event.getParent().getId(), data.getString("PARENT_EVENT_ID"));
 		}
 		assertEquals(event.getType(), data.getString("TYPE"));
 		assertEquals(event.getName(), data.getString("NAME"));
 		assertEquals(event.getApplication(), data.getString("APPLICATION"));
		assertEquals(event.getStart().getTime(), data.getDate("START").getTime());
 		assertEquals(event.getDuration(), (Long)data.getLong("DURATION_MS"));
 		assertEquals(event.getError(), data.getString("ERROR"));
 		assertEquals(event.getUserId(), data.getString("USER_ID"));
 		assertEquals(event.getSessionId(), data.getString("SESSION_ID"));
 		
 		data.close();
 
 		assertEquals("event " + event.getId() + " has correct child count", event.getChildren().size(), countChildren(event.getId()));
 		long total = event.getDuration();
 		for (BehaviorEvent child : event.getChildren()) {
 			verifyEvent(child);
 			assertTrue("child " + event.getId() + " starts after parent", child.getStart().getTime() >= event.getStart().getTime());
 			assertTrue("child " + event.getId() + " cannot have duration longer than parent", child.getDuration() <= event.getDuration());
 			total -= child.getDuration();
 		}
 		assertTrue("sum of child events cannot exceed length of parent", total >= 0);
 	}
 
 	private int countChildren(Serializable id) throws SQLException {
 		ResultSet ret = stmt.executeQuery("select count(EVENT_ID) from BEHAVIOR_TRACKING_EVENT where PARENT_EVENT_ID = " + id);
 		assertTrue(ret.next());
 		int count = ret.getInt(1);
 		ret.close();
 		return count;
 	}
 
 	private ResultSet getEventData(Serializable id) throws SQLException {
 		ResultSet ret = stmt.executeQuery("select * from BEHAVIOR_TRACKING_EVENT where EVENT_ID = " + id);
 		assertTrue("found event with id " + id, ret.next());
 		return ret;
 	}
 
 	/**
 	 * Recursively create an event tree with variable data.
 	 * @param parent the parent of the new event, or null.
 	 * @param depth the depth of the subtree to create.
 	 * @param maxDepth the maximum depth of the tree
 	 * @param maxChildren the number of children for each nested event.
 	 * @param counter maintains a count of the total number of events created.
 	 */
	private BehaviorEvent createEvent(BehaviorEvent parent, int depth, int maxDepth, int maxChildren, int[] counter) 
 		throws InterruptedException
 	{
 		//our range of test event data.  the sizes are coprime so that
 		//we should generate a bunch of different combinations.
 		final String[] types = { "request", "app" };
 		final String[] names = { "hello", "goodbye", "world" };
 		final String[] apps = { "test" };
 		final String[] users = { "me", "you", "him", "her", null };
 		final String[] sessions = { "1", "2" };
 
 		int index = counter[0];
 		counter[0] = counter[0] + 1;
 
 		//one out of five events has null user/session.
 		String user = select(users, index);
 		String session = (user == null ? null : user + select(sessions, index));
 		
 		BehaviorEvent ret = new BehaviorEvent(parent, select(types, index), select(names, index), 
 				select(apps, index), user, session);
 		ret.start();
 		
 		//give the events some duration to make things more interesting.
 		Thread.sleep((long)(Math.random() * 25));
 		
 		//even numbered events have an error.
 		if ( (index % 2) == 0 )
 			ret.setError("error[" + index + "]");
 		//two out of every three events have a data element.
 		if ( ((index + 2) % 3) != 0 )
 			ret.addData().put("key", "value<" + index + ">");
 
 		//descend to create child events.
 		if (depth < maxDepth) {
 			for (int c = 0; c < maxChildren; ++c) {
 				createEvent(ret, depth + 1, maxDepth, maxChildren, counter);
 			}
 		}
 		
 		ret.stop();
 		return ret;
 	}
 	
 	private static final String select(String[] array, int index) {
 		return array[index % array.length];
 	}
 }
