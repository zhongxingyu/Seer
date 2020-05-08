 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
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
  *
  * de.jwic.controls.AbstractSelectListControl
  */
 package de.jwic.controls;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import de.jwic.base.IControlContainer;
 import de.jwic.base.Range;
 import de.jwic.data.DataLabel;
 import de.jwic.data.ISelectElement;
 import de.jwic.data.SelectElement;
 import de.jwic.data.SelectElementBaseLabelProvider;
 import de.jwic.data.SelectElementContentProvider;
 
 /**
  * This version of the AbstractListControl implements a simple list of ISelectElment's
  * so that child controls can use simple title:key elements.
  * 
  * @author lordsam
  */
 public class AbstractSelectListControl extends AbstractListControl<ISelectElement> {
 
 	protected List<ISelectElement> elements = null;
 
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public AbstractSelectListControl(IControlContainer container, String name) {
 		super(container, name);
 		baseLabelProvider = new SelectElementBaseLabelProvider();
 
 	}
 
 	/**
 	 * Returns a list of ListEntry elements for rendering. It is faster to prepare the
 	 * list this way than to do this in the velocity template.
 	 * @return
 	 */
 	public List<ListEntry> buildEntryList() {
		List<ListEntry> allEntries = new ArrayList<ListEntry>();
 		for (Iterator<ISelectElement> it = contentProvider.getContentIterator(new Range()); it.hasNext() ; ) {
 			ISelectElement elm = it.next();
 			DataLabel label = baseLabelProvider.getBaseLabel(elm);
 			String key = contentProvider.getUniqueKey(elm);
 			allEntries.add(new ListEntry(label.text, key));
 		}
 		return allEntries;
 	}
 
 	/**
 	 * Returns true if the given key is selected.
 	 * @param key
 	 * @return
 	 */
 	public boolean isKeySelected(String key) {
 		if (key != null) {
 			String[] keys = getSelectedKeys();
 			if (keys != null) {
 				for (String k : keys) {
 					if (key.equals(k)) {
 						return true;
 					}
 				}
 			}
 		} 
 		return false;
 	}
 
 	/**
 	 * Add an element.
 	 * @param element
 	 */
 	public void addElement(ISelectElement element) {
 		if (elements == null) {
 			elements = new ArrayList<ISelectElement>();
 			setContentProvider(new SelectElementContentProvider(elements));
 		}
 		elements.add(element);
 		requireRedraw();
 	}
 
 	/**
 	 * Add an element. The key will automatically be assigned.
 	 * @param title
 	 */
 	public ISelectElement addElement(String title) {
 		SelectElement elm = new SelectElement(title);
 		addElement(elm);
 		return elm;
 	}
 
 	/**
 	 * Add the element with a custom key.
 	 * @param title
 	 * @param key
 	 */
 	public ISelectElement addElement(String title, String key) {
 		SelectElement elm = new SelectElement(title, key);
 		addElement(elm);
 		return elm;
 	}
 
 	/**
 	 * Remove an element.
 	 * @param element
 	 */
 	public void removeElement(ISelectElement element) {
 		if (elements != null) {
 			elements.remove(element);
 			requireRedraw();
 		}
 	}
 
 	/**
 	 * Remove an element by its key.
 	 * @param key
 	 */
 	public void removeElementByKey(String key) {
 		if (contentProvider != null) {
 			ISelectElement obj = contentProvider.getObjectFromKey(key);
 			if (obj != null) {
 				removeElement(obj);
 			}
 		}
 	}
 
 	/**
 	 * Select the element with the specified key. Works only with elements that 
 	 * do have a key.
 	 * @param key
 	 */
 	public void selectedByKey(String key) {
 		if (key != null && elements != null) {
 			for (ISelectElement se : elements) {
 				if (key.equals(se.getKey())) {
 					setSelectedElement(se);
 					return;
 				}
 			}
 		}
 		setSelectedKey(null);
 		requireRedraw();
 	}
 
 	/**
 	 * 
 	 */
 	public void clear() {
 		if(elements != null){
 			elements.clear();
 		}
 		setSelectedElement(null);
 		requireRedraw();
 	}
 
 }
