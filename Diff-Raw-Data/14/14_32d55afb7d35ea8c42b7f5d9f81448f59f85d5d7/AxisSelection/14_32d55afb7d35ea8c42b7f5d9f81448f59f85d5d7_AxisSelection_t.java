 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.inspector;
 
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.collections.Transformer;
 
 
 /**
  * Class to hold a list of axis names and dataset from which an axis can be selected
  */
 public class AxisSelection extends InspectorProperty {
 	class AxisSelData implements Comparable<AxisSelData> {
 		private boolean selected;
 		private int order; // possible order in a list of choices (0 signifies leave to end of list) 
 		private AxisChoice data;
 
 		public AxisSelData(AxisChoice axisData, boolean axisSelected) {
 			setData(axisData);
 			setSelected(axisSelected);
 		}
 
 		/**
 		 * @param selected The selected to set.
 		 */
 		public void setSelected(boolean selected) {
 			this.selected = selected;
 		}
 
 		/**
 		 * @return Returns the selected.
 		 */
 		public boolean isSelected() {
 			return selected;
 		}
 
 		/**
 		 * @param order The order to set.
 		 */
 		public void setOrder(int order) {
 			this.order = order;
 		}
 
 		/**
 		 * @return Returns the order.
 		 */
 		public int getOrder() {
 			return order;
 		}
 
 		/**
 		 * @param data The data to set.
 		 */
 		public void setData(AxisChoice data) {
 			this.data = data;
 		}
 
 		/**
 		 * @return Returns the data.
 		 */
 		public AxisChoice getData() {
 			return data;
 		}
 
 		@Override
 		public int compareTo(AxisSelData axisSelData) {
 			int cOrder = axisSelData.getOrder();
 			if (order == 0)
 				return cOrder == 0 ? 0 : 1;
 			if (cOrder == 0)
 				return -1;
 			return order - cOrder;
 		}
 		
 	}
 
 	private int length; // length of axis
 	private List<AxisSelData> asData;
 	private Set<String> names;
 
 	Transformer orderTransformer = new Transformer() {
 		@Override
 		public Object transform(Object o) {
 			if (o instanceof AxisSelData)
 				return ((AxisSelData) o).getOrder();
 			return null;
 		}
 	};
 
 	Predicate axisSelectionPredicate = new Predicate() {
 		@Override
 		public boolean evaluate(Object o) {
 			if (o instanceof AxisSelData)
 				return ((AxisSelData) o).isSelected();
 			return false;
 		}
 	};
 
 	class NamePredicate implements Predicate {
 		String name;
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		@Override
 		public boolean evaluate(Object o) {
 			return (o instanceof AxisSelData && ((AxisSelData) o).getData().getName().equals(name));
 		}
 	}
 
 	NamePredicate namePredicate = new NamePredicate();
 
 	/**
 	 * Create an axis selection that corresponds to a dataset dimension of given length
 	 * @param length 
 	 */
 	public AxisSelection(int length) {
 		this.length = length;
 		asData = new ArrayList<AxisSelData>();
 		names = new HashSet<String>();
 	}
 
 	/**
 	 * @return Returns the length
 	 */
 	public int getLength() {
 		return length;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder text = new StringBuilder();
 		text.append('(');
 		for (AxisSelData a : asData) {
 			text.append(a.getData().getName());
 			if (a.isSelected()) {
 				text.append('*');
 			}
 			text.append(", ");
 		}
 		if (text.length() > 0) {
 			text.deleteCharAt(text.length()-1);
 			text.deleteCharAt(text.length()-1);
 		}
 		text.append(')');
 		return text.toString();
 	}
 
 	/**
 	 * Add axis choice with given order
 	 * @param axis
 	 * @param order (can be zero to denote last)
 	 */
 	public void addChoice(AxisChoice axis, int order) {
 		String name = axis.getName();
 		AxisSelData a;
 		if (names.contains(name)) {
 			namePredicate.setName(name);
 			a = (AxisSelData) CollectionUtils.find(asData, namePredicate);
 			if (axis != a.getData())
 				a.setData(axis);
 		} else {
 			a = new AxisSelData(axis, false);
 			names.add(name);
 			asData.add(a);
 		}
 		a.setOrder(order);
 	}
 
 	/**
 	 * @param name
 	 * @return true if name is one of possible selections
 	 */
 	public boolean containsAxis(String name) {
 		return names.contains(name);
 	}
 
 	/**
 	 * Select an axis with given name
 	 * @param name
 	 * @param fire
 	 */
 	public void selectAxis(String name, boolean fire) {
 		if (!names.contains(name))
 			return;
 
 		String oldName = getSelectedName();
 		for (AxisSelData d: asData)
 			d.setSelected(false);
 
 		namePredicate.setName(name);
 		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, namePredicate);
 		a.setSelected(true);
 
 		if (fire)
 			fire(new PropertyChangeEvent(this, "axisselection", oldName, name));
 	}
 
 	/**
 	 * Select an axis with given index
 	 * @param index 
 	 */
 	public void selectAxis(int index) {
 		selectAxis(index, false);
 	}
 
 	/**
 	 * Select an axis with given index
 	 * @param index 
 	 * @param fire
 	 */
 	public void selectAxis(int index, boolean fire) {
 		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, axisSelectionPredicate);
		String oldName = null;
		if (a != null) {
 			a.setSelected(false);
			oldName = a.getData().getName();
		}
		a = asData.get(index);
		a.setSelected(true);
 		if (fire)
			fire(new PropertyChangeEvent(this, "axisselection", oldName, a.getData().getName()));
 	}
 
 	/**
 	 * @param index 
 	 * @return axis name of given index
 	 */
 	public String getName(int index) {
 		AxisSelData a = asData.get(index); 
 		return a == null ? null : a.getData().getName();
 	}
 
 	/**
 	 * @param index 
 	 * @return axis choice of given index
 	 */
 	public AxisChoice getAxis(int index) {
 		AxisSelData a = asData.get(index); 
 		return a == null ? null : a.getData();
 	}
 
 	/**
 	 * @param name
 	 * @return axis choice of given name
 	 */
 	public AxisChoice getAxis(String name) {
 		namePredicate.setName(name);
 		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, namePredicate);
 		return a == null ? null : a.getData();
 	}
 
 	/**
 	 * @return number of choices
 	 */
 	public int size() {
 		return names.size();
 	}
 
 	/**
 	 * @param index
 	 * @return selection status
 	 */
 	public boolean isSelected(int index) {
 		AxisSelData a = asData.get(index); 
 		return (a == null) ? false : a.isSelected();
 	}
 
 	/**
 	 * Get name of selected axis
 	 * @return name or null if nothing selected
 	 */
 	public String getSelectedName() {
 		AxisChoice choice = getSelectedAxis();
 		if (choice != null)
 			return choice.getName();	
 		return null;
 	}
 
 	/**
 	 * Get index of selected axis
 	 * @return index or -1 if nothing selected
 	 */
 	public int getSelectedIndex() {
 		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, axisSelectionPredicate);
 		return asData.indexOf(a);
 	}
 
 	/**
 	 * @return selected dimensions
 	 */
 	public int[] getSelectedIndexMapping() {
 		AxisChoice choice = getSelectedAxis();
 		if (choice != null)
 			return choice.getIndexMapping();	
 		return null;
 	}
 
 	/**
 	 * @return selected choice
 	 */
 	public AxisChoice getSelectedAxis() {
 		AxisSelData a = (AxisSelData) CollectionUtils.find(asData, axisSelectionPredicate);
 		return (a == null) ? null : a.getData();
 	}
 
 	/**
 	 * Call this once finished adding choices to 
 	 */
 	public void reorderChoices() {
 
 		Collections.sort(asData);
 
 	}
 
 	/**
 	 * @return maximum order
 	 */
 	@SuppressWarnings({ "unchecked" })
 	public int getMaxOrder() {
 		List<Integer> orders = (List<Integer>) CollectionUtils.collect(asData, orderTransformer);
 		return orders.size() > 0 ? (Integer) Collections.max(orders) : 0;
 	}
 
 	/**
 	 * @return true if names and axis datasets have same values
 	 */
 	@Override
 	public boolean equals(Object other) {
 		if (this == other)
 			return true;
 
 		if (other instanceof AxisSelection) {
 			AxisSelection that = (AxisSelection) other;
 			
 			if (!that.names.equals(names))
 				return false;
 			
 			if (!CollectionUtils.isEqualCollection(asData, that.asData))
 				return false;
 			
 			return true;
 			
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = length;
 		for (String n : names)
 				hash = hash * 17 + n.hashCode();
 		for (AxisSelData n : asData)
 				hash = hash * 17 + n.hashCode();
 		return hash;
 	}
 
 	/**
 	 * Clone everything but axis choice values
 	 */
 	@Override
 	public AxisSelection clone() throws CloneNotSupportedException {
 		AxisSelection selection = new AxisSelection(length);
 		for (AxisSelData a : asData) {
 			selection.addChoice(a.getData().clone(), a.getOrder());
 		}
 		return selection;
 	}
 }
