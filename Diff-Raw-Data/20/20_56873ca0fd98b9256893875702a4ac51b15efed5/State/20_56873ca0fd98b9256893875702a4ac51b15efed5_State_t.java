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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * 
  * 
  * @author Mark Hobson
  */
 public class State
 {
 	// TODO: is it necessary that State API needs to expose details of its implementation - i.e. properties and events?
 	// TODO: rename to BeanState?
 	// TODO: make immutable?
 
 	// types ------------------------------------------------------------------
 	
 	private static class MultiKey
 	{
 		// fields -----------------------------------------------------------------
 		
 		private final Object a;
 		
 		private final Object b;
 		
 		// constructors -----------------------------------------------------------
 		
 		public MultiKey(Object a, Object b)
 		{
 			this.a = a;
 			this.b = b;
 		}
 		
 		// Object methods ---------------------------------------------------------
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public int hashCode()
 		{
 			return a.hashCode() * 37 + b.hashCode();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean equals(Object object)
 		{
 			if (object instanceof MultiKey)
 			{
 				MultiKey key = (MultiKey) object;
 				
 				return a.equals(key.a) && b.equals(key.b);
 			}
 			
 			return false;
 		}
 	}
 	
 	// fields -----------------------------------------------------------------
 	
 	private Map<MultiKey, PropertyState> properties;
 	
 	private List<EventSetState> events;
 	
 	private Map<String, Object> parametersByName;
 	
 	// constructors -----------------------------------------------------------
 	
 	public State()
 	{
 		properties = new LinkedHashMap<MultiKey, PropertyState>();
 		
 		events = new ArrayList<EventSetState>();
 		
 		parametersByName = new HashMap<String, Object>();
 	}
 	
 	public State(State state)
 	{
 		this();
 		
 		// TODO: use add(State) when parameters supported
		this.properties = new LinkedHashMap<MultiKey, PropertyState>(state.properties);
		this.events = new ArrayList<EventSetState>(state.events);
		this.parametersByName = new HashMap<String, Object>(state.parametersByName);
 	}
 	
 	// public methods ---------------------------------------------------------
 	
 	public void add(State state)
 	{
 		for (PropertyState property : state.getProperties())
 		{
 			addProperty(property);
 		}
 		
 		for (EventSetState event : state.getEvents())
 		{
 			addEvent(event);
 		}
 		
 		// TODO: add parameters?
 	}
 	
 	public void addProperty(PropertyState property)
 	{
 		MultiKey key = new MultiKey(property.getBean(), property.getDescriptor());
 		
 		properties.put(key, property);
 	}
 	
 	public void removeProperty(PropertyState property)
 	{
 		MultiKey key = new MultiKey(property.getBean(), property.getDescriptor());
 		
 		properties.remove(key);
 	}
 	
 	public void addEvent(EventSetState event)
 	{
 		events.add(event);
 	}
 	
 	public void removeEvent(EventSetState event)
 	{
 		events.remove(event);
 	}
 	
 	public List<PropertyState> getProperties()
 	{
 		return Collections.unmodifiableList(new ArrayList<PropertyState>(properties.values()));
 	}
 	
 	public List<PropertyState> getProperties(Object bean)
 	{
 		List<PropertyState> properties = new ArrayList<PropertyState>();
 		
 		for (PropertyState property : this.properties.values())
 		{
 			if (bean.equals(property.getBean()))
 			{
 				properties.add(property);
 			}
 		}
 		
 		return properties;
 	}
 	
 	public List<EventSetState> getEvents()
 	{
 		return Collections.unmodifiableList(events);
 	}
 	
 	public List<EventSetState> getEvents(Object bean)
 	{
 		List<EventSetState> events = new ArrayList<EventSetState>();
 		
 		for (EventSetState event : this.events)
 		{
 			if (bean.equals(event.getBean()))
 			{
 				events.add(event);
 			}
 		}
 		
 		return events;
 	}
 	
 	public Object getParameter(String name)
 	{
 		return parametersByName.get(name);
 	}
 	
 	public void setParameter(String name, Object value)
 	{
 		parametersByName.put(name, value);
 	}
 	
 	// Object methods ---------------------------------------------------------
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode()
 	{
 		int hashCode = getProperties().hashCode();
 		hashCode = (hashCode * 31) + events.hashCode();
 		hashCode = (hashCode * 31) + parametersByName.hashCode();
 		
 		return hashCode;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object object)
 	{
 		if (!(object instanceof State))
 		{
 			return false;
 		}
 		
 		State state = (State) object;
 		
 		return getProperties().equals(state.getProperties())
 			&& events.equals(state.getEvents())
 			&& parametersByName.equals(state.parametersByName);
 	}
 }
