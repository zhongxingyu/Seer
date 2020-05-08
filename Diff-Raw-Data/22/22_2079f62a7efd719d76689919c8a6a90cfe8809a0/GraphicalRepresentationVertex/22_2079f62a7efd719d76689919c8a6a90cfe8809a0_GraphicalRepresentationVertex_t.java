 /*
  * Copyright (c) 2011 Dominik Dingel
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the
  * Software without restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
  * Software, and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all 
  * copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
  * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package de.unisiegen.informatik.bs.alvis.graph.datatypes;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.GraphicalRepresentation;
 
 /**
  * GraphicalRepresentation of Vertex, should be implemented by every Vertex
  * 
  * @author Dominik Dingel
  * 
  */
 
 public interface GraphicalRepresentationVertex extends GraphicalRepresentation {
 
 	/**
 	 * Vertex body color
 	 * 
	 * @return String color, no localization but case independent
 	 */
 	public String getColorText();
 
 	/**
 	 * Vertex body color
 	 * 
 	 * @param new color as case independent string
 	 */
 	public void setColor(String color);
 
 	/**
 	 * Vertex label
 	 * 
 	 * @return String label
 	 */
 	public String getLabel();
 
 	/**
 	 * Vertex label
 	 * 
 	 * @param new label
 	 */
 	public void setLabel(String label);
 }
