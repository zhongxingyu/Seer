 /*
  * Copyright 2011 Tomas Schlosser
  *
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
 package org.anadix.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.anadix.Element;
 import org.anadix.ElementFactory;
 import org.drools.runtime.ClassObjectFilter;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.WorkingMemoryEntryPoint;
 
 
 /**
  * Abstract class for all ElementFactory implementations
  *
  * @author tomason
  * @version $Id: $
  */
 public abstract class AbstractElementFactory implements ElementFactory {
 	private final StatefulKnowledgeSession ksession;
 
 	/**
	 * Creates a new instance of ElementFactory of given class.
 	 *
	 * @param clazz class of ElementFactory to construct
	 * @param session session to use within this ElementFactory
	 * @param <T> a class extending ElementFactory
	 * @return a new instance of ElementFactory of given type
 	 */
 	public static <T extends ElementFactory> T createFactory(Class<T> clazz, StatefulKnowledgeSession session) {
 		if (clazz == null) {
 			throw new NullPointerException("clazz cannot be null");
 		}
 		if (session == null) {
 			throw new NullPointerException("session must not be null");
 		}
 		try {
 			return clazz.getConstructor(StatefulKnowledgeSession.class).newInstance(session);
 		} catch (Exception ex) {
 			throw new RuntimeException("Unable to instantiate ElementFactory", ex);
 		}
 	}
 
 	/**
 	 * Constructor
 	 *
 	 * @param ksession - StatefulKnowledgeSession to be used to insert elements to
 	 */
 	protected AbstractElementFactory(StatefulKnowledgeSession ksession) {
 		if (ksession == null) {
 			throw new NullPointerException("ksession can't be null");
 		}
 
 		this.ksession = ksession;
 	}
 
 	/**
 	 * Gets the session
 	 *
 	 * @return session used throughout this factory
 	 */
 	protected StatefulKnowledgeSession getSession() {
 		return ksession;
 	}
 
 	/** {@inheritDoc} */
 	public void insertElement(Element element) {
 		getSession().insert(element);
 	}
 
 	/**
	 * Gets all Element instances in the underlying session
 	 *
 	 * @return collection of Elements in session
 	 */
 	public Collection<Element> getElements() {
 		Collection<Element> result = new ArrayList<Element>();
 
 		for (Object o : getSession().getObjects(new ClassObjectFilter(Element.class))) {
 			result.add( (Element)o );
 		}
 
 		return result;
 	}
 
 	/** {@inheritDoc} */
 	public void setGlobal(String name, Object instance) {
 		if (name == null) {
 			throw new NullPointerException("name cannot be null");
 		}
 		if (instance == null) {
 			throw new NullPointerException("instance cannot be null");
 		}
 		getSession().setGlobal(name, instance);
 	}
 
 	/** {@inheritDoc} */
 	public void setAsGlobal(String name) {
 		setGlobal(name, this);
 	}
 
 	/** {@inheritDoc} */
 	public void insertEvent(String entrypoint, Object event) {
 		if (entrypoint == null) {
 			throw new NullPointerException("entrypoint cannot be null");
 		}
 		if (event == null) {
 			throw new NullPointerException("event cannot be null");
 		}
 
 		WorkingMemoryEntryPoint ep = getSession().getWorkingMemoryEntryPoint(entrypoint);
 		if (ep != null) {
 			ep.insert(event);
 		} else {
 			throw new IllegalArgumentException("Entry point " + entrypoint + " is not defined");
 		}
 
 	}
 }
