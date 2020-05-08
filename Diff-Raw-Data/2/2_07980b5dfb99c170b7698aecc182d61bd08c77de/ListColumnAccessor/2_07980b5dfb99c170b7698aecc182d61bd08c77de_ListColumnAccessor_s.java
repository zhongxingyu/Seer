 /*
    Copyright 2012 Przemysław Pastuszka
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package pl.rtshadow.jtriss.column.accessor;
 
 import static org.apache.commons.lang3.BooleanUtils.negate;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.tuple.Pair;
 
 import pl.rtshadow.jtriss.column.ColumnConstructor;
 import pl.rtshadow.jtriss.column.SortedColumn;
 import pl.rtshadow.jtriss.column.element.ColumnElement;
 import pl.rtshadow.jtriss.column.element.ModifiableColumnElement;
 import pl.rtshadow.jtriss.common.ValueRange;
 
 public class ListColumnAccessor<T extends Comparable<? super T>> extends AbstractColumnAccessor<T> {
   private ListColumnAccessor(Class<T> type, SortedColumn<T> column) {
     this.column = column;
     this.type = type;
   }
 
   private int startPoint = -1;
   private boolean mainColumn = false;
 
   @Override
   public ReconstructedObject<T> reconstruct(ColumnElement<T> element) {
     boolean hasAnyElementInsideColumn = false;
     int originalColumnId = element.getColumnId();
     setUpStartPoint(element);
 
     ArrayList<T> valuesList = new ArrayList<T>();
     while (element.getColumnId() == originalColumnId) {
       if (column.contains(element)) {
         if (mainColumn && isNotFirstListElementInColumn(element)) {
           return null;
         }
         hasAnyElementInsideColumn = true;
       }
 
       if (element.hasValue()) {
         valuesList.add(element.getValue());
       }
       element = element.getNextElementInTheRow();
     }
 
     if (hasAnyElementInsideColumn) {
       return new ReconstructedObject<T>(valuesList, element);
     }
     return null;
   }
 
   private boolean isNotFirstListElementInColumn(ColumnElement<T> element) {
     return element.getPositionInColumn() < startPoint;
   }
 
   private void setUpStartPoint(ColumnElement<T> element) {
     if (mainColumn && startPoint < 0) {
       startPoint = element.getPositionInColumn();
     }
   }
 
   @Override
   public ColumnAccessor<T> subColumn(ValueRange<T> range) {
     return new ListColumnAccessor<T>(type, column.getSubColumn(range));
   }
 
   public static <T extends Comparable<? super T>> ColumnAccessorGenerator<T> generator(Class<T> type, ColumnConstructor<T> constructor) {
     return new ListColumnAccessorGenerator<T>(type, constructor);
   }
 
   private static class ListColumnAccessorGenerator<T extends Comparable<? super T>> extends AbstractColumnAccessorGenerator<T> {
     public ListColumnAccessorGenerator(Class<T> type, ColumnConstructor<T> constructor) {
       this.constructor = constructor;
       this.type = type;
     }
 
     @Override
     public Pair<ModifiableColumnElement<T>, ModifiableColumnElement<T>>
         insert(Object object, ColumnElement<T> nextElement) {
       List<T> valuesList = retrieveListFrom(object);
       List<ModifiableColumnElement<T>> columnElements = mapValuesToColumnElements(valuesList);
 
       for (int i = 0; i < columnElements.size() - 1; ++i) {
         setNextElementAndAddToConstructor(columnElements.get(i), columnElements.get(i + 1));
       }
 
       ModifiableColumnElement<T> lastElement = columnElements.get(columnElements.size() - 1);
       setNextElementAndAddToConstructor(lastElement, nextElement);
 
       return Pair.of(columnElements.get(0), lastElement);
     }
 
     private void setNextElementAndAddToConstructor(ModifiableColumnElement<T> currentElement, ColumnElement<T> next) {
       currentElement.setNextElement(next);
       constructor.add(currentElement);
     }
 
     private List<ModifiableColumnElement<T>> mapValuesToColumnElements(List<T> valuesList) {
       List<ModifiableColumnElement<T>> columnElements = new ArrayList<ModifiableColumnElement<T>>(valuesList.size());
 
       if (valuesList.isEmpty()) {
        columnElements.add((ModifiableColumnElement<T>) factory.createEmptyListElement());
       }
 
       for (T singleValue : valuesList) {
         columnElements.add(factory.createElement(type, singleValue));
       }
       return columnElements;
     }
 
     private List<T> retrieveListFrom(Object value) {
       if (negate(value instanceof List)) {
         throw new IllegalArgumentException("Expected instance of java.util.List, got: " + value.getClass());
       }
 
       return (List<T>) value;
     }
 
     @Override
     public ColumnAccessor<T> prepareColumnAccessor() {
       return new ListColumnAccessor<T>(type, constructor.generate(factory.getEmptyListAwareComparator()));
     }
   }
 
   @Override
   public void prepareMainColumnForReconstruction() {
     startPoint = -1;
     mainColumn = true;
   }
 
   @Override
   public void finishReconstruction() {
     mainColumn = false;
   }
 }
