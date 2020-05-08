 /*******************************************************************************
  * Copyright © 2011 Alexey Kaloshin. All Rights Reserved. 
  * 
  *  Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  1. Redistributions of source code must retain the above copyright 
  * notice, this list of conditions and the following disclaimer.
  * 
  *  2. Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * 
  *  3. The name of the author may not be used to endorse or promote 
  * products derived from this software without specific prior written permission. 
  * 
  *  THIS SOFTWARE IS PROVIDED BY Alexey Kaloshin "AS IS" AND ANY EXPRESS 
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package cz.nocach.swingmvp.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jmock.Expectations;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import cz.nocach.swingmvp.BaseTest;
 import cz.nocach.swingmvp.model.listener.Add;
 import cz.nocach.swingmvp.model.listener.Listener;
 import cz.nocach.swingmvp.model.listener.ListenerException;
 import cz.nocach.swingmvp.model.listener.Property;
 import cz.nocach.swingmvp.model.listener.Remove;
 
 public class ModelWithCustomListenerTest extends BaseTest {
 	
 	private CustomChangeListenerModel customListenerModel;
 	private TestModelListener listener;
 	private TestModelListener listenerInSet;
 
 	@BeforeMethod
 	public void setUp(){
 		customListenerModel = new CustomChangeListenerModel();
 		listener = mockery.mock(TestModelListener.class, "listenerOne");
 		listenerInSet = mockery.mock(TestModelListener.class, "listenerTwo");
 	}
 	@Test
 	public void customListenerDisablesPropertySupportByDefault() {
 		assertFailAddingCommonPropertyChangeListener();
 		assertFailAddingPropertyChangeListenerForSingleProperty();
 	}
 
 	private void assertFailAddingCommonPropertyChangeListener() {
 		try {
 			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
 				@Override public void propertyChange(PropertyChangeEvent evt) { }
 			};
 			
 			customListenerModel.addPropertyChangeListener(propertyChangeListener);
 			
 			fail("custom listener did allow to set PropertyChangeListener ");
 		} catch (ListenerException e) {
 			// ok
 		}
 	}
 	
 	private void assertFailAddingPropertyChangeListenerForSingleProperty() {
 		try {
 			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
 				@Override public void propertyChange(PropertyChangeEvent evt) { }
 			};
 			
 			customListenerModel.addPropertyChangeListener("property", propertyChangeListener);
 			
 			fail("custom listener did allow to set PropertyChangeListener");
 		} catch (ListenerException e) {
 			// ok
 		}
 	}
 
 	@Test
 	public void simpleCustomListenerPropertySetFire() {
 		customListenerModel.listener = listener;
 		customListenerModel.listeners.add(listenerInSet);
 		
 		checkBothListenerAreCalledWhenSetting("new value");
 	}
 	
 	private void checkBothListenerAreCalledWhenSetting(final String value) {
 		mockery.checking(new Expectations() {{
 				oneOf(listener).onNameChange(value);
 				oneOf(listenerInSet).onNameChange(value);
 		}});
 
 		customListenerModel.setName(value);
 		
 		mockery.assertIsSatisfied();
 	}
 
 	@Test
 	public void simpleCustomCollectionListenerEventFire() {
 		ModelWithCollection model = new ModelWithCollection();
 		ModelWithCollectionListenerStub listener = new ModelWithCollectionListenerStub();
 		model.listener = listener;
 		String added = "added";
 		String removed = "removed";
 
 		model.addPersonName(added);
 		assertTrue(listener.added.contains(added));
 
 		model.removePersonName(removed);
 		assertTrue(listener.removed.contains(removed));
 	}
 
 	@Test
 	public void removedCustomListenerShouldNotBeCalled() {
 		customListenerModel.listener = listener;
 		customListenerModel.listeners.add(listenerInSet);
 		
 		checkBothListenerAreCalledWhenSetting("new value");
 
 		customListenerModel.listener = null;
 		customListenerModel.listeners.remove(listenerInSet);
 
 		checkNoListenersAreCalledWhenSetting("another value");
 	}
 	
 	/**
 	 * test exists to ensure, that setting of Listener field is not interpreted as
 	 * setting of ordinary property field
 	 */
 	@Test
 	public void settingListenerThroughMethod(){
		TestModelListener listenerInstance = mockery.mock(TestModelListener.class);
		customListenerModel.setListener(listenerInstance);
 	}
 	
 	private void checkNoListenersAreCalledWhenSetting(final String value) {
 		mockery.checking(new Expectations() {{
 				never(listener).onNameChange(value);
 				never(listenerInSet).onNameChange(value);
 		}});
 
 		customListenerModel.setName(value);
 
 		mockery.assertIsSatisfied();
 	}
 	
 	private static class CustomChangeListenerModel extends Model {
 		@Listener
 		private TestModelListener listener;
 		@Listener(type = TestModelListener.class)
 		private Set<TestModelListener> listeners = new HashSet<ModelWithCustomListenerTest.TestModelListener>();
 		private String name;
 		public String getName() {
 			return name;
 		}
 		public void setName(String name) {
 			this.name = name;
 		}
 		public TestModelListener getListener() {
 			return listener;
 		}
 		public void setListener(TestModelListener listener) {
 			this.listener = listener;
 		}
 		public Set<TestModelListener> getListeners() {
 			return listeners;
 		}
 		public void setListeners(Set<TestModelListener> listeners) {
 			this.listeners = listeners;
 		}
 	}
 
 	private static class ModelWithCollection extends Model {
 		private Set<String> names = new HashSet<String>();
 		@Listener
 		ModelWithCollectionListener listener;
 		
 		public void addPersonName(String newString) {
 			names.add(newString);
 		}
 		public void removePersonName(String string) {
 			names.remove(string);
 		}
 	}
 
 	private interface ModelWithCollectionListener {
 		@Add(propertyName = "personName", collection = "names")
 		public void onStringAdd(String newString);
 
 		@Remove(propertyName = "personName", collection = "names")
 		public void onStringRemoved(String newString);
 	}
 
 	private class ModelWithCollectionListenerStub implements
 			ModelWithCollectionListener {
 		private Set<String> added = new HashSet<String>();
 		private Set<String> removed = new HashSet<String>();
 
 		@Override
 		public void onStringAdd(String newString) {
 			added.add(newString);
 		}
 
 		@Override
 		public void onStringRemoved(String newString) {
 			removed.add(newString);
 		}
 	}
 
 	private interface TestModelListener {
 		@Property("name")
 		public void onNameChange(String newName);
 	}
 }
