 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of ExSchema.
  *
  * ExSchema is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * ExSchema is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ExSchema. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.exschema.model;
 
 import fr.imag.exschema.exporter.GraphvizExporter;
 import fr.imag.exschema.exporter.RooExporter;
 import fr.imag.exschema.exporter.RooModel;
 
 /**
  * Data model attribute.
  * 
  * @author jccastrejon
  * 
  */
 public class Attribute implements GraphvizExporter, RooExporter {
 
     /**
      * Attribute's name.
      */
     private String name;
 
     /**
      * Attribute's value.
      */
     private String value;
 
     /**
      * Full constructor.
      * 
      * @param name
      */
     public Attribute(final String name, final String value) {
         this.name = name;
         this.value = value;
     }
 
     @Override
     public String getDotNodes(final String parent) {
         String identifier;
         StringBuilder returnValue;
 
         identifier = Long.toString(System.nanoTime());
         returnValue = new StringBuilder(identifier + " [shape=\"box\", label=\"Attribute \\n " + name + " : " + value
                 + "\"]\n");
 
         if (parent != null) {
             returnValue.append(parent + " -> " + identifier + "\n");
         }
 
         return returnValue.toString();
     }
 
     @Override
     public String getRooCommands(final RooModel rooModel) {
         // TODO: Support additional data types
        return "field string --fieldName " + this.name + "\n";
     }
 
     // Getters-setters
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 }
