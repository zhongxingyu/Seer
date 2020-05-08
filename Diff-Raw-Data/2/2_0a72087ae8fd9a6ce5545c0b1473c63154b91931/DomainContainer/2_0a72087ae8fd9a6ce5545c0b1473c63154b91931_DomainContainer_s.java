 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
  *   The vaadin-framework Infrastructure is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework.data.reflect;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 import pt.ist.fenixframework.plugins.luceneIndexing.DomainIndexer;
 import pt.ist.fenixframework.plugins.luceneIndexing.queryBuilder.dsl.BuildingState;
 import pt.ist.fenixframework.plugins.luceneIndexing.queryBuilder.dsl.DSLState;
 import pt.ist.fenixframework.pstm.AbstractDomainObject;
 import pt.ist.vaadinframework.VaadinFrameworkLogger;
 import pt.ist.vaadinframework.data.AbstractBufferedContainer;
 import pt.ist.vaadinframework.data.LuceneContainer;
 import pt.ist.vaadinframework.data.metamodel.MetaModel;
 import pt.ist.vaadinframework.data.metamodel.PropertyDescriptor;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.util.ItemSorter;
 
 public class DomainContainer<Type extends AbstractDomainObject> extends AbstractBufferedContainer<Type, Object, DomainItem<Type>>
 	implements LuceneContainer {
     private final int maxHits = 1000000;
 
     public DomainContainer(Property wrapped, Class<? extends Type> elementType, Hint... hints) {
 	super(wrapped, elementType, hints);
     }
 
     public DomainContainer(Class<? extends Type> elementType, Hint... hints) {
 	super(elementType, hints);
     }
 
     public DomainContainer(Collection<Type> elements, Class<? extends Type> elementType, Hint... hints) {
 	super(new ArrayList<Type>(elements), elementType, hints);
     }
 
     @Override
     protected DomainItem<Type> makeItem(Type itemId) {
	return new DomainItem<Type>(itemId);
     }
 
     @Override
     protected DomainItem<Type> makeItem(Class<? extends Type> type) {
 	return new DomainItem<Type>(type);
     }
 
     public void setContainerProperties(String... propertyIds) {
 	for (String propertyId : propertyIds) {
 	    PropertyDescriptor propertyDescriptor = MetaModel.findMetaModelForType(getElementType()).getPropertyDescriptor(
 		    propertyId);
 	    addContainerProperty(propertyId, propertyDescriptor.getPropertyType(), propertyDescriptor.getDefaultValue());
 	}
     }
 
     @Override
     public void setItemSorter(ItemSorter itemSorter) {
 	super.setItemSorter(itemSorter);
     }
 
     @Override
     public void search(String filterText) {
 	removeAllItems();
 	final DSLState expr = createFilterExpression(filterText);
 	DateTime start = new DateTime();
 	final List<Type> searchResult = (List<Type>) DomainIndexer.getInstance().search(getElementType(), expr, maxHits);
 	DateTime check1 = new DateTime();
 	addItemBatch(searchResult);
 	DateTime check2 = new DateTime();
 	VaadinFrameworkLogger.getLogger().debug(
 		"container search: " + expr.toString() + " took: " + new Interval(start, check1).toDuration() + " "
 			+ new Interval(check1, check2).toDuration() + "(" + new Interval(start, check2).toDuration() + ")");
     }
 
     protected DSLState createFilterExpression(String filterText) {
 	return new BuildingState().matches(filterText);
     }
 }
