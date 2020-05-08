 /*
  * Copyright (C) 2011 rbr
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package zbeans.cowgraph.model;
 
 import java.util.ArrayList;
import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author rbr
  */
 public class CompositeGraphElementImpl extends GraphElementImpl implements CompositeGraphElement {
     
     public static final String PROP_ELEMENTS = "elements";
 
     private List<GraphElement> elements = new ArrayList<GraphElement>();
 
     public CompositeGraphElementImpl(GraphElementType type) {
         super(type);
     }
        
     @Override
     public List<GraphElement> getElements() {
         return elements;
     }
     
     public void addGraphElement(GraphElement element) {        
         elements.add(element);        
         firePropertyChange(PROP_ELEMENTS, element, null);
     }  
     
     public void removeGraphElement(GraphElement element) {
         elements.remove(element);
         firePropertyChange(PROP_ELEMENTS, element, null);
     }
     
    public CompositeGraphElementImpl clone() {
       CompositeGraphElementImpl clone = (CompositeGraphElementImpl)super.clone();

       clone.elements = new LinkedList<GraphElement>();

       for (GraphElement element1 : this.elements) {
            clone.elements.add(element1.clone());
        }
        
        return clone;
    }
 }
