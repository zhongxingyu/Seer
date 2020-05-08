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
 
 package com.pagegoblin.lemonjuice.engine;
 
 import java.io.Writer;
 
 import com.pagegoblin.lemonjuice.Template;
 import com.pagegoblin.lemonjuice.TemplateContext;
 
 class InlineElement extends Element {
     private String key;
     private Template parent;
 
     public InlineElement(Template parent, String key) {
         this.parent = parent;
         this.key = key;
     }
 
     @Override
     public Object evaluate(TemplateContext model) throws Exception {
         String name = parent.getLocation() + key;
         return model.find(name);
     }
 
     @Override
     public void print(Writer out, TemplateContext model) throws Exception {
         String name = parent.getLocation() + key;
         Template template = model.find(name);
         if (template != null) {
            template.print(out, pushContext(template, model));
         }
     }
 }
