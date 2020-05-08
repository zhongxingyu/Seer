 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.hobsoft.symmetry.state;
 
 import java.beans.EventSetDescriptor;
 import java.beans.PropertyDescriptor;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.EventObject;
 
 import org.hobsoft.symmetry.support.bean.EventSets;
 import org.hobsoft.symmetry.support.bean.Properties;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests {@code State}.
  * 
  * @author Mark Hobson
  * @see State
  */
 public class StateTest
 {
 	// tests ------------------------------------------------------------------
 	
 	@Test
 	public void getPropertiesWithBeanWhenEmpty()
 	{
 		State state = new State();
 		
 		assertEquals(Collections.emptyList(), state.getProperties(createBean()));
 	}
 	
 	@Test
 	public void getPropertiesWithBeanWhenOne()
 	{
 		Object bean = createBean();
 		PropertyState property = new PropertyState(bean, getPropertyDescriptor(), createValue());
 		
 		State state = new State();
 		state.addProperty(property);
 		
 		assertEquals(Collections.singletonList(property), state.getProperties(bean));
 	}
 	
 	@Test
 	public void getPropertiesWithBeanWhenMany()
 	{
 		Object bean = createBean();
 		PropertyState property1 = new PropertyState(bean, getPropertyDescriptor("foo"), createValue());
 		PropertyState property2 = new PropertyState(bean, getPropertyDescriptor("bar"), createValue());
 		
 		State state = new State();
 		state.addProperty(property1);
 		state.addProperty(property2);
 		
 		assertEquals(Arrays.asList(property1, property2), state.getProperties(bean));
 	}
 	
 	@Test
 	public void getPropertiesWithBeanWhenManyBeans()
 	{
 		Object bean1 = createBean();
 		PropertyState property1 = new PropertyState(bean1, getPropertyDescriptor(), createValue());
 		PropertyState property2 = new PropertyState(createBean(), getPropertyDescriptor(), createValue());
 		
 		State state = new State();
 		state.addProperty(property1);
 		state.addProperty(property2);
 		
 		assertEquals(Collections.singletonList(property1), state.getProperties(bean1));
 	}
 	
 	@Test
 	public void getEventsWithBeanWhenEmpty()
 	{
 		State state = new State();
 		
 		assertEquals(Collections.emptyList(), state.getEvents(createBean()));
 	}
 	
 	@Test
 	public void getEventsWithBeanWhenOne()
 	{
 		Object bean = createBean();
 		EventSetState event = new EventSetState(getEventSetDescriptor(), new EventObject(bean));
 		
 		State state = new State();
 		state.addEvent(event);
 		
 		assertEquals(Collections.singletonList(event), state.getEvents(bean));
 	}
 	
 	@Test
 	public void getEventsWithBeanWhenMany()
 	{
 		Object bean = createBean();
 		EventSetState event1 = new EventSetState(getEventSetDescriptor("foo"), new EventObject(bean));
 		EventSetState event2 = new EventSetState(getEventSetDescriptor("bar"), new EventObject(bean));
 		
 		State state = new State();
 		state.addEvent(event1);
 		state.addEvent(event2);
 		
 		assertEquals(Arrays.asList(event1, event2), state.getEvents(bean));
 	}
 	
 	@Test
 	public void getEventsWithBeanWhenManyBeans()
 	{
 		Object bean1 = createBean();
 		EventSetState event1 = new EventSetState(getEventSetDescriptor(), new EventObject(bean1));
 		EventSetState event2 = new EventSetState(getEventSetDescriptor(), createEventObject());
 		
 		State state = new State();
 		state.addEvent(event1);
 		state.addEvent(event2);
 		
 		assertEquals(Collections.singletonList(event1), state.getEvents(bean1));
 	}
 	
 	@Test
 	public void hashCodeWhenEqual()
 	{
 		PropertyState property = createPropertyState();
 		EventSetState event = createEventSetState();
 		
 		State state1 = new State();
 		state1.addProperty(property);
 		state1.addEvent(event);
 		
 		State state2 = new State();
 		state2.addProperty(property);
 		state2.addEvent(event);
 		
 		assertEquals(state1.hashCode(), state2.hashCode());
 	}
 	
 	@Test
 	public void equalsWhenEqual()
 	{
 		PropertyState property = createPropertyState();
 		EventSetState event = createEventSetState();
 		
 		State state1 = new State();
 		state1.addProperty(property);
 		state1.addEvent(event);
 		
 		State state2 = new State();
 		state2.addProperty(property);
 		state2.addEvent(event);
 		
 		assertTrue(state1.equals(state2));
 	}
 	
 	@Test
 	public void equalsWhenPropertiesNotEqual()
 	{
 		EventSetState event = createEventSetState();
 		
 		State state1 = new State();
 		state1.addProperty(createPropertyState());
 		state1.addEvent(event);
 		
 		State state2 = new State();
 		state2.addEvent(event);
 		
 		assertFalse(state1.equals(state2));
 	}
 	
 	@Test
 	public void equalsWhenEventsNotEqual()
 	{
 		PropertyState property = createPropertyState();
 		
 		State state1 = new State();
 		state1.addProperty(property);
 		state1.addEvent(createEventSetState());
 		
 		State state2 = new State();
 		state2.addProperty(property);
 		
 		assertFalse(state1.equals(state2));
 	}
 	
 	@Test
 	public void equalsWithDifferentClass()
 	{
 		State state = createState();
 		
 		assertFalse(state.equals(new Object()));
 	}
 	
 	@Test
 	public void equalsWithNull()
 	{
 		State state = createState();
 		
 		// workaround Checkstyle bug 2809655
 		// CHECKSTYLE:OFF
 		assertFalse(state.equals(null));
 		// CHECKSTYLE:ON
 	}
 	
 	// private methods --------------------------------------------------------
 	
 	private State createState()
 	{
 		State state = new State();
 		state.addProperty(createPropertyState());
 		state.addEvent(createEventSetState());
 		return state;
 	}
 
 	private static PropertyState createPropertyState()
 	{
 		return new PropertyState(createBean(), getPropertyDescriptor(), createValue());
 	}
 	
 	private static DummyBean createBean()
 	{
 		return new DummyBean();
 	}
 	
 	private static PropertyDescriptor getPropertyDescriptor()
 	{
 		return getPropertyDescriptor("foo");
 	}
 	
 	private static PropertyDescriptor getPropertyDescriptor(String propertyName)
 	{
 		return Properties.getDescriptor(DummyBean.class, propertyName);
 	}
 	
 	private static String createValue()
 	{
 		return "x";
 	}
 
 	private EventSetState createEventSetState()
 	{
 		return new EventSetState(getEventSetDescriptor(), createEventObject());
 	}
 	
 	private static EventSetDescriptor getEventSetDescriptor()
 	{
 		return getEventSetDescriptor("foo");
 	}
 
 	private static EventSetDescriptor getEventSetDescriptor(String eventSetName)
 	{
 		return EventSets.getDescriptor(DummyBean.class, eventSetName);
 	}
 	
 	private EventObject createEventObject()
 	{
 		return new EventObject(this);
 	}
 }
