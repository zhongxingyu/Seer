 package codesample.timeline;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestTimeline 
 {
 	
 	Timeline timeline;
 
 	@Before
 	public void setUp() throws Exception 
 	{
 		timeline = new Timeline();
 	}
 
 	@After
 	public void tearDown() throws Exception 
 	{
 		timeline = null;
 	}
 
 	@Test
 	public void testTimeline() {
 		
 		// Make sure the map was created
 		assertNotNull(timeline); 
 		
 		// Make sure that the map is empty
 		try{ 
 			assertTrue(timeline.isEmpty());
 		}
 		catch(NullPointerException e){}
 	}
 	
 	@Test
 	public void testAdd(){
 		
 		DateTime start = new DateTime(2004,12,25,0,0);
 		Event e = new NamedEvent("Event", start);
 		Event x = new NamedEvent("Another Event", start);
 		Event e3 = new NamedEvent("Event 3", new DateTime(2005,01,02, 0, 0));
 		Event e4 = new NamedEvent("Event 4", new DateTime(2005,01,02, 0, 0));
 		Event e5 = new NamedEvent("Event 5", new DateTime(2005,01,07, 0, 0));
 		
 		assertFalse(e.equals(x));
 		
 		assertTrue(timeline.add(e));
 //		System.out.println(timeline);
 		assertTrue(timeline.add(x));
 		assertTrue(timeline.add(e3));
 		assertTrue(timeline.add(e4));
 		assertTrue(timeline.add(e5));
 		System.out.println(timeline);
 	}
 	
 	@Test
 	public void testAddEndTime(){
 		DateTime startTime = new DateTime(2003,12,23,0,0);
 		DateTime endTime = new DateTime(2004,1,11,0,0);
 		DateTime startTime2 = new DateTime(2001,3,12,0,0);
 		
 		Event a = new NamedEvent("Developed game", startTime,endTime);
 		Event b = new NamedEvent("Worked at Raytheon", startTime2,endTime);
 		Event c = new NamedEvent("Email content", startTime);
 		
 		assertTrue(timeline.add(a));
 		assertTrue(timeline.add(b));
 		assertTrue(timeline.add(c));
 		System.out.println(timeline);
 	}
 	
 	@Test
 	public void testAddAll(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime start2 = new DateTime(2003,04,25,0,0);
 		DateTime endTime = new DateTime(2007,1,11,0,0);
 
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", start);
 		Event c = new NamedEvent("EventC", start,endTime);
 		Event d = new NamedEvent("EventD", start2,endTime);
 		
 		timeline.add(d);
 		System.out.println(timeline);
 		
 		ArrayList<Event> list = new ArrayList<Event>();
 //		list.add(a);
 //		list.add(b);
 //		list.add(c);
 		Collections.addAll(list,a,b,c);
 		
 		assertTrue(timeline.addAll(list));
 		System.out.println(timeline);
 	}
 	
 	@Test
 	public void testClear(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime time2 = new DateTime(2002,12,25,0,0);
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", time);
 		Event c = new NamedEvent("EventC", time2);
 		
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		
 		timeline.clear();
 		
 		assertTrue(timeline.isEmpty());
 	}
 	
 	@Test
 	public void testContains(){
 		
 		DateTime start = new DateTime(2004,12,25,0,0);
 		Event e = new NamedEvent("New Event",start);
 		timeline.add(e);
 		
 		assertTrue(timeline.contains(e));
 	}
 	
 	@Test
 	public void testContainsAll(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,11,25,0,0);
 		DateTime time2 = new DateTime(2007,10,25,0,0);
 		
 		Event a = new NamedEvent("EventA", time);
 		Event b = new NamedEvent("EventB", start);
 		Event c = new NamedEvent("EventC", start,time2);
 		
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		
 		ArrayList<Event> list = new ArrayList<Event>();
 		list.add(a);
 		list.add(b);
 		list.add(c);
 		
 		assertTrue(timeline.containsAll(list));
 	}
 	
 	
 	@Test
 	public void testisEmpty(){
 		assertTrue(timeline.isEmpty());
 	}
 	
 	@Test
 	public void testRemove(){
 		
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime endTime = new DateTime(2007,1,11,0,0);
 
 		Event e = new NamedEvent("EventE", time,endTime);
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", start,endTime);
 		
 		assertTrue(timeline.add(e));
 		assertTrue(timeline.add(a));
 		assertTrue(timeline.add(b));
 		
 		System.out.println(timeline);
 		timeline.remove(a);
 		System.out.println(timeline);
 		timeline.remove(b);
 		System.out.println(timeline);
 		timeline.remove(e);
 		System.out.println(timeline);
 		
 //		assertTrue(eventMap.contains(a));
 	}
 	
 	@Test
 	public void testRemoveAll(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime time2 = new DateTime(2003,9,25,0,0);
 		
 		Event e = new NamedEvent("Event", start);
 		Event a = new NamedEvent("EventA", time);
 		Event c = new NamedEvent("EventC", time2);
 	
 		ArrayList<Event> list = new ArrayList<Event>();
 		list.add(e);
 		list.add(a);
 		list.add(c);
 		
 		assertTrue(timeline.add(e));
 		assertTrue(timeline.add(a));
 		assertTrue(timeline.add(c));
 		System.out.println(timeline);
 		assertTrue(timeline.removeAll(list));
 		
 		System.out.println(timeline);
 //		assertFalse(eventMap.contains(c));
 //		assertTrue(eventMap.isEmpty());
 		
 	}
 	
 	@Test
 	public void testRetainAll(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime time2 = new DateTime(2003,9,25,0,0);
 		DateTime endTime = new DateTime(2009, 10,5,11,0,0);
 		
 		Event a = new NamedEvent("EventE", start);
 		Event b = new NamedEvent("EventA", time,endTime);
 		Event c = new NamedEvent("EventC", time);
 		Event d = new NamedEvent("EventD", time2);
 	
 		ArrayList<Event> list = new ArrayList<Event>();
 		list.add(a);
 		list.add(b);
 //		list.add(c);
 //		list.add(d);
 		
 		assertTrue(timeline.add(a));
 		assertTrue(timeline.add(b));
 		assertTrue(timeline.add(c));
 		System.out.println(timeline);
 		assertEquals(3,timeline.size());
 			
 		assertTrue(timeline.retainAll(list));
 //		assertFalse(timeline.retainAll(list));
 		
 		assertTrue(timeline.contains(a));
 		assertTrue(timeline.contains(b));
 		assertFalse(timeline.contains(c));
 		
 		System.out.println(timeline);
 	}
 	
 	@Test
 	public void testSize(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		Event e = new NamedEvent("Event", start);
 		Event a = new NamedEvent("EventA", start);
 		Event c = new NamedEvent("EventC", time);
 		
 		assertTrue(timeline.add(e));
 		assertTrue(timeline.add(a));
 		assertTrue(timeline.add(c));
 		
 		assertEquals(3,timeline.size());
 	}
 	
 	@Test
 	public void testToArray(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime time2 = new DateTime(2003,9,25,0,0);
 		
 		Event a = new NamedEvent("Event", start);
 		Event b = new NamedEvent("EventA", time);
 		Event c = new NamedEvent("EventC", time2);
 	
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		
 //		System.out.println(timeline.toArray());
 		System.out.println(java.util.Arrays.toString(timeline.toArray()));
 //		System.out.print(timeline.what());
 	}
 	
 	@Test
 	public <T> void testToArrayT(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime time2 = new DateTime(2003,9,25,0,0);
 		
 		Event a = new NamedEvent("Event", start);
 		Event b = new NamedEvent("EventA", time);
 		Event c = new NamedEvent("EventC", time2);
 	
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		
 		T[] result =(T[]) timeline.toArray();
 		
 //		System.out.println(java.util.Arrays.toString(result));
 	}
 	
 	////****Iterator Method Tests****\\\\
 	
 		////****Tests for hasNext()****\\\\
 	
 	@Test
 	public void testHasNextElement()
 	{
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(1993,8,14,0,0);
 		DateTime time2 = new DateTime(1985,9,12,0,0);
 		
 	
 		
 		//Case 1
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", start);
 		Event c = new NamedEvent("EventC", start);
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		Iterator<Event> i = timeline.iterator();
 //		while(i.hasNext())
 //		{
 //			System.out.println(i.next()+"\n");
 //		}
 		assertTrue(i.hasNext());
 		i.next();
 		i.next();
 		assertTrue(i.hasNext());
 		
 		
 //		
 				
 	}
 	@Test
 	public void testHasNextSet()
 	{
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(1993,8,14,0,0);
 		DateTime time2 = new DateTime(1985,9,12,0,0);
 		//Case 2
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", time);
 		Event c = new NamedEvent("EventC", time2);
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		Iterator<Event> i = timeline.iterator();
 //		while(i.hasNext())
 //		{
 //			System.out.println(i.next()+"\n");
 //		}
 		i.next();
 		i.next();
 		assertTrue(i.hasNext());
 	}
 	
 	@Test
 	public void testHasNextNoElement()
 	{
 		
 		DateTime start = new DateTime(2004,12,25,0,0);
 		//Case 3
 		Event a = new NamedEvent("EventA", start);
 		timeline.add(a);
 		Iterator<Event> i = timeline.iterator();
 		assertTrue(i.hasNext());
 		i.next();
 		assertFalse(i.hasNext());
 		
 		try{
 			i.next();
 			fail("fail");
 		}
 		catch(NoSuchElementException e){}
 	}
 
 	@Test 
 	public void testNext()
 	{
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		DateTime time2 = new DateTime(2003,9,25,0,0);
 		
 		Event a = new NamedEvent("Event", start);
 		Event b = new NamedEvent("EventA", time);
 		Event c = new NamedEvent("EventC", time2);
 	
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		
 		Iterator<Event> i = timeline.iterator();
 		i.next();
 		i.next();
 		i.next();
 		assertFalse(i.hasNext());
 	}
 	
 	
 		////****Tests for testHasPrevious()****\\\\
 	
 	@Test
 	public void testHasPreviousOnFirstEventIndex() //case3 //supposed to be a wrong situation.
 	{
 		DateTime start = new DateTime(2004,12,25,0,0);
 		Event a = new NamedEvent("Event", start);
 		timeline.add(a);
 		//<2004,12,25,0,0> : [Event]
 	
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		try
 		{
 			i.previous();
 			fail();
 		}
 		catch(NoSuchElementException e){}
 		assertTrue(i.hasNext());
 		assertFalse(i.hasPrevious());
 	}
 	
 	@Test
 	public void testHasPreviousEventIndex() //case1 //supposed to be a right situation.
 	{
 
 		DateTime time = new DateTime(2003,12,25,0,0);
 		Event b = new NamedEvent("EventB", time);
 		Event c = new NamedEvent("EventC", time);
 		timeline.add(b);
 		timeline.add(c);
 		//<2003,12,25,0,0> : [EventB][EventC]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		i.next();
 		i.next();
 		assertTrue(i.hasPrevious());
 	}
 	@Test
 	public void testHasPreviousEventIndex2() //case1: arranged differently.
 	{
 		DateTime time1 = new DateTime(2004,12,25,0,0);
 		DateTime time2 = new DateTime(2005,12,25,0,0);
 		DateTime time3 = new DateTime(2012,12,25,0,0);
 		Event b = new NamedEvent("EventB", time1);
 		Event c = new NamedEvent("EventC", time1);
 		Event d = new NamedEvent("EventD", time1);
 		Event e = new NamedEvent("EventE", time2);
 		Event f = new NamedEvent("EventF", time2);
 		Event g = new NamedEvent("EventG", time3);
 		Event h = new NamedEvent("EventH", time3);
 		timeline.add(b);
 		timeline.add(c);
 		timeline.add(d);
 		timeline.add(e);
 		timeline.add(f);
 		timeline.add(g);
 		timeline.add(h);
 		//<2004,12,25,0,0>: [EventB][EventC][EventD]
 		//<2005,12,25,0,0>: [EventE][EventF]
 		//<2012,12,25,0,0>: [EventG][EventH]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 //		while(i.hasNext())
 //			{
 //				System.out.println(i.next()+"\n");
 //			}
 		
 //		i.next();
 //		i.next();
 //		i.next();
 //		i.next();
 //		i.next();
 //		i.next();
 //		i.next();
 //		i.previous();
 //		i.previous();
 //		i.previous();
 //		i.previous();
 //		i.previous();
 //		i.previous();
 //		assertTrue(i.hasPrevious());
 		
 	}
 	
 	@Test
 	public void testHasPreviousEventSet() //case2 //supposed to be a right situation.
 	{
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		//DateTime time2 = new DateTime(2003,9,25,0,0);
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", time);
 		timeline.add(a);
 		timeline.add(b);
 		
 		//<2004,12,25,0,0> : [Event]
 		//<2003,12,25,0,0> : [EventA]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		i.next();
 		assertTrue(i.hasPrevious());
 	}
 	
 		////****Tests for testPrevious()****\\\\
 	
 	//Case 3: supposed to fail.
 	@Test
 	public void testPreviousFirstEvent(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		Event a = new NamedEvent("Event", start);
 		timeline.add(a);
 		//<2004,12,25,0,0> : [Event]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		try{
 			i.previous();
 			fail("fail");
 		}
 		catch (NoSuchElementException e){}
 	}
 	
 	//Case3: different data arrangement (Should also fail).
 	@Test
 	public void testPreviousFirstEvent2(){
 		DateTime time1 = new DateTime(2004,12,25,0,0);
 		Event a = new NamedEvent("EventA", time1);
 		Event b = new NamedEvent("EventB", time1);
 		Event c = new NamedEvent("EventC", time1);
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		//<2004,12,25,0,0>: [EventA][EventB][EventC]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		i.next(); //returns [EventA]
 		i.next(); //returns [EventB]
 		i.next(); //returns [EventC]
 		i.previous(); //returns [EventC]
 		i.previous(); //return [EventB]
 		i.previous(); //return [EventA]
 		
 		try{
 			i.previous(); 
 			fail("fail");
 		}
 		catch (NoSuchElementException e){}
 	}
 	
 	
 	//Case 1: supposed to not fail
 	@Test
 	public void testPreviousEventIndex(){ 
 		DateTime time = new DateTime(2003,12,25,0,0);
 		Event b = new NamedEvent("EventB", time);
 		Event c = new NamedEvent("EventC", time);
 		timeline.add(b);
 		timeline.add(c);
 		//<2003,12,25,0,0> : [EventA][EventC]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		i.next();
 	    i.previous();
 //		assertEquals(b,i.previous()); //should return [EventA].
 	}
 	
 	//Case 2: supposed to not fail
 	@Test
 	public void testPreviousEventSet(){
 		DateTime start = new DateTime(2004,12,25,0,0);
 		DateTime time = new DateTime(2003,12,25,0,0);
 		Event a = new NamedEvent("Event", start);
 		Event b = new NamedEvent("EventA", time);
 		timeline.add(a);
 		timeline.add(b);
 		//<2004,12,25,0,0> : [Event]
 		//<2003,12,25,0,0> : [EventA]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		i.next();
 		i.previous();
 	}
 	
 	//Case3: different data arrangement (Should also fail).
 	@Test
 	public void testPreviousFirstEventFromLastEvent(){
 		DateTime start = new DateTime(2003,12,25,0,0);
 		DateTime time1 = new DateTime(2004,12,25,0,0);
 		DateTime time2 = new DateTime(2005,12,25,0,0);
 		DateTime time3 = new DateTime(2006,12,25,0,0);
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", time1);
 		Event c = new NamedEvent("EventC", time1);
 		Event d = new NamedEvent("EventD", time2);
 		Event e = new NamedEvent("EventE", time2);
 		Event f = new NamedEvent("EventF", time2);
 		Event q = new NamedEvent("EventQ", time3);
 		Event u = new NamedEvent("EventU", time3);
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		timeline.add(d);
 		timeline.add(e);
 		timeline.add(f);
 		timeline.add(q);
 		timeline.add(u);
 		//<2003,12,25,0,0>: [EventA] 
 		//<2004,12,25,0,0>: [EventB][EventC]
 		//<2005,12,25,0,0>: [EventD][EventE][EventF]
 		//<2012,12,25,0,0>: [EventQ][EventU]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 
 		i.next(); //returns EventA
 		i.next(); //return EventB
 		i.next(); //return EventC
 		i.next(); //return EventD
 		i.next(); //return EventE
 		i.next(); //return EventF
 		i.next(); //return EventQ
 		i.next(); //return EventU
 	
 		i.previous(); //return EventU
 		i.previous(); //return EventQ
 		i.previous(); //return EventF
 		i.previous(); //return EventE
 		i.previous(); //return EventD
 		i.previous(); //return EventC
 		i.previous(); //return EventB
 		i.previous(); //return EventA
 		
 		try{
 			i.previous(); 
 			fail("fail");
 		}
 		catch (NoSuchElementException z){}	
 	}
 	
 		////****Tests for testNextIndex****\\\\
 	
 	//Case1
 	@Test
 	public void testNextIndexOnNextEventInSameSet()
 	{
 		DateTime start = new DateTime(2003,12,25,0,0);
 		Event a = new NamedEvent("EventA", start);
 		timeline.add(a);
 		//<2003,12,25,0,0>: [EventA] 
 		
 
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		assertEquals(1,i.nextIndex());
 	}
 	
 	//Case2
 	@Test
 	public void testNextIndexToNextSet()
 	{
 		DateTime start = new DateTime(2003,12,25,0,0);
 		DateTime time1 = new DateTime(2004,12,25,0,0);
 		Event a = new NamedEvent("EventA", start); 
 		Event b = new NamedEvent("EventB", time1);
 		timeline.add(a);
 		timeline.add(b);
 		//<2003,12,25,0,0>: [EventA] 
 		//<2004,12,25,0,0>: [EventB]
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		while(i.hasNext())
 		{
 			System.out.println("At position " + i.nextIndex() + " is: " +i.next());
 		}
 		
 	}
 	
 	
 	//Case3
 	@Test
 	public void testNextIndexOnLastEvent()
 	{
 		DateTime start = new DateTime(2003,12,25,0,0);
 		DateTime time1 = new DateTime(2004,12,25,0,0);
 		DateTime time2 = new DateTime(2005,12,12,0,0);
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", time1);
 		Event c = new NamedEvent("EventC", time1);
 		Event d = new NamedEvent("EventD", time2);
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		timeline.add(d);
 		//<2003,12,25,0,0>: [EventA] 
 		//<2004,12,25,0,0>: [EventB][EventC]
 		//                  [EventD]
 		System.out.println(timeline);
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		assertEquals(1,i.nextIndex());
 	}
 	
 	@Test
 	public void testNextAndPreviousIndex()
 	{
 		DateTime start = new DateTime(2003,12,25,0,0);
 		DateTime time1 = new DateTime(2004,12,25,0,0);
 		DateTime time2 = new DateTime(2005,12,25,0,0);
 		DateTime time3 = new DateTime(2006,12,25,0,0);
 		Event a = new NamedEvent("EventA", start);
 		Event b = new NamedEvent("EventB", time1);
 		Event c = new NamedEvent("EventC", time1);
 		Event d = new NamedEvent("EventD", time2);
 		Event e = new NamedEvent("EventE", time2);
 		Event f = new NamedEvent("EventF", time2);
 		Event q = new NamedEvent("EventQ", time3);
 		Event u = new NamedEvent("EventU", time3);
 		timeline.add(a);
 		timeline.add(b);
 		timeline.add(c);
 		timeline.add(d);
 		timeline.add(e);
 		timeline.add(f);
 		timeline.add(q);
 		timeline.add(u);
 		System.out.println(timeline);
 		
 		ListIterator<Event> i = (ListIterator<Event>) timeline.iterator();
 		
 		while(i.hasNext())
 		{
			i.next();
 		}
		
 		while (i.hasPrevious())
 		{
 			System.out.println("At position " + i.previousIndex() + " is: " +i.previous());
 		}
 	}
 }
