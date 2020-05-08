 /*
  *  lemonjuice - Java Template Engine.
  *  Copyright (C) 2009 Manuel Tomis support@pagegoblin.com
  *
  *  This library is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.codegremlins.lemonjuice.engine;
 
 import java.io.Writer;
 import java.util.List;
 
 import com.codegremlins.lemonjuice.TemplateContext;
 
 class SequenceElement extends Element {
     private Element[] elements;
 
     public SequenceElement(List<Element> elements) {
         this.elements = elements.toArray(new Element[elements.size()]);
     }
 
     public void print(Writer out, TemplateContext model) throws Exception {
         for (Element v : elements) {
             v.print(out, (TemplateContext)model);
         }
     }
 
     @Override
     public Object evaluate(TemplateContext model) throws Exception {
         Object last = null;
         for (Element v : elements) {
             last = v.evaluate((TemplateContext)model);
         }
         
         return last;
     }
 
     @Override
     public void visit(TemplateElementVisitor context) throws Exception {
         context.visit(this);
     }

    public Element[] getElements() {
        return elements;
    }
 }
