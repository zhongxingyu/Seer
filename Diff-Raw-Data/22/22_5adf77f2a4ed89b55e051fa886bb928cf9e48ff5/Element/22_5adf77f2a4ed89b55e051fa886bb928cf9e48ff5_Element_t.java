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
 import java.util.Map;
 
 import com.pagegoblin.lemonjuice.Printer;
 import com.pagegoblin.lemonjuice.Template;
 import com.pagegoblin.lemonjuice.TemplateContext;
 import com.pagegoblin.lemonjuice.TemplateException;
 import com.pagegoblin.lemonjuice.TemplateFunction;
 import com.pagegoblin.lemonjuice.property.Properties;
 
 @SuppressWarnings("unchecked")
 abstract public class Element {
     private int column;
     private int line;
     private String templateName;
     
     abstract public Object evaluate(TemplateContext model) throws Exception;
     
     public void print(Writer out, TemplateContext model) throws Exception {
         Object value = evaluate(model);

         if (value instanceof Printer) {
             ((Printer)value).print(out);
         } else if (value instanceof Template) {
             Template template = (Template)value;
             template.print(out, pushContext(template, model));
         } else if (value instanceof TemplateFunction) {
             TemplateFunction template = (TemplateFunction)value;
             Object result = template.evaluate(new Object[0]);
             if (result != null) {
                 out.write(result.toString());
             }            
         } else if (value != null) {
             out.write(value.toString());
         }
     }
     
     void bless(String templateName, int line, int column) {
         this.templateName = templateName;
         this.line = line;
         this.column = column;
     }
     
     protected Object error(String text) {
         throw new TemplateException("" + templateName + ": line " + line + "," + column + ": " + text);
     }    
     
     protected Object getValue(Object model, String name) {
         Object result = null;
         try {
             if (model instanceof Map) {
                 result = ((Map)model).get(name);
             } else if (model instanceof TemplateContext) {
                 result = ((TemplateContext)model).get(name);
             } else if (model == null) {
                 return false;
             } else {
                 result = Properties.getProperty(model.getClass(), name).getValue(model);
             }
         } catch (Throwable ex) {
             error("Cannot find property `" + name + "' in object " + model);
         }
         
         return result;
     }
     
    protected TemplateContext pushContext(Template template, TemplateContext model) throws Exception {
         TemplateContext context = model.child();
         String[] keys = template.getParameters();
 
         if (template.getDefaults() != null) {
             Element[] defaults = template.getDefaults();
             for (int i = 0; i < defaults.length; i++) {
                 if (defaults[i] != null) {
                     context.set(keys[i], defaults[i].evaluate(model));
                 }
             }
         }
         
         return context;
     }
 }
