 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.poem.prepro;
 
 import java.util.*;
 import java.io.*;
 
 public class ReferenceFieldDef extends FieldDef {
 
   public ReferenceFieldDef(TableDef table, String name, int displayOrder,
                            String type, Vector qualifiers)
       throws IllegalityException {
     super(table, name, type, "Integer", displayOrder, qualifiers);
   }
 
   protected void generateColRawAccessors(Writer w) throws IOException {
     super.generateColRawAccessors(w);
 
     w.write(
       "\n" +
       "          public Object getRaw(Persistent g)\n" +
       "              throws AccessPoemException {\n" +
       "            return ((" + mainClass + ")g).get" + suffix + "Troid();\n" +
       "          }\n" +
       "\n" +
       "          public void setRaw(Persistent g, Object raw)\n" +
       "              throws AccessPoemException {\n" +
       "            ((" + mainClass + ")g).set" + suffix + "Troid((" +
                        rawType + ")raw);\n" +
       "          }\n");
   }
 
   private String targetCast() {
     TableDef targetTable = (TableDef)table.dsd.tableOfClass.get(type);
     return targetTable == null || targetTable.superclass == null ?
              "" : "(" + type + ")";
   }
 
   public void generateBaseMethods(Writer w) throws IOException {
     super.generateBaseMethods(w);
 
     // FIXME the definition of these is duplicated from TableDef
     String targetTableAccessorMethod = "get" + type + "Table";
     String targetSuffix = type;
 
     w.write("\n" +
 	    "  public Integer get" + suffix + "Troid()\n" +
             "      throws AccessPoemException {\n" +
 	    "    readLock();\n" +
             "    return get" + suffix + "_unsafe();\n" +
             "  }\n" +
             "\n" +
             "  public void set" + suffix + "Troid(Integer raw)\n" +
             "      throws AccessPoemException {\n" +
            "    _" + tableAccessorMethod + "().get" + suffix + "Column()." +
                      "getType().assertValidRaw(raw);\n" +
 	    "    writeLock();\n" +
             "    set" + suffix + "_unsafe(raw);\n" +
             "  }\n" +
             "\n" +
             "  public " + type + " get" + suffix + "()\n" +
             "      throws AccessPoemException, NoSuchRowPoemException {\n" +
             "    Integer troid = get" + suffix + "Troid();\n" +
             "    return troid == null ? null :\n" +
 	                 // This cast is necessary when the target table is
 	                 // an "extends"
 	    "        " + targetCast() +
                          "get" + table.dsd.databaseClass + "()." +
                          targetTableAccessorMethod + "()." +
                          "get" + targetSuffix + "Object(troid);\n" +
             "  }\n" +
             "\n" +
             "  public void set" + suffix + "(" + type + " cooked)\n" +
             "      throws AccessPoemException {\n" +
             "    set" + suffix + "Troid(cooked == null ? null : cooked.troid());\n" +
             "  }\n");
   }
 
   public void generateJavaDeclaration(Writer w) throws IOException {
     w.write("Integer " + name);
   }
 
   public String poemTypeJava() {
     // FIXME the definition of these is duplicated from TableDef
     String targetTableAccessorMethod = "get" + type + "Table";
     return
         "new ReferencePoemType(((" + table.dsd.databaseClass + ")getDatabase())." +
         targetTableAccessorMethod + "(), " + isNullable + ")";
   }
 }
