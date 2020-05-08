 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem.prepro;
 
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.Arrays;
 import java.io.StreamTokenizer;
 import java.io.Writer;
 import java.io.IOException;
 
 /**
  * A Table Definition holding information from a DSD.
  */
 public class TableDef {
 
   DSD dsd;
   /** Mixed case name. */
   final String suffix;
   /** Lowercase name. */
   final String name;
   String displayName;
   String description;
   String category;
   int displayOrder;
   boolean seqCached;
   int cacheSize = CacheSizeTableQualifier.DEFAULT;
   private Vector fields = new Vector();
   boolean isAbstract;
   boolean definesColumns;
   TableNamingInfo naming = null;
 
   int nextFieldDisplayOrder = 0;
   // Note we have to store the imports and process them at
   // the end to avoid referring to a table that has yet to be processed.
   private final Hashtable imports = new Hashtable();
   private final Vector tableBaseImports = new Vector();
   private final Vector persistentBaseImports = new Vector();
 
   /**
    * Constructor.
    *
    * @param dsd
    *        the {@link DSD} this is a member of
    * @param tokens
    *        a stream of tokens
    * @param displayOrder
    *        the ordering within the DSD
    * @param isAbstract
    *        whether this is an abstract table
    * @param nameStore
    *        where to put our names
    * @throws ParsingDSDException
    *         if an unexpected token is encountered
    * @throws IllegalityException
    *         if a semantic incoherence is detected
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public TableDef(DSD dsd, StreamTokenizer tokens, int displayOrder,
                   boolean isAbstract, TableNamingStore nameStore)
       throws ParsingDSDException,
       IOException,
       IllegalityException {
     this.dsd = dsd;
     this.displayOrder = displayOrder;
     this.isAbstract = isAbstract;
     if (tokens.ttype != StreamTokenizer.TT_WORD)
       throw new ParsingDSDException("<table name>", tokens);
     suffix = tokens.sval;
     name = suffix.toLowerCase();
 
     String superclass = null;
 
     if (tokens.nextToken() == StreamTokenizer.TT_WORD) {
       if (!tokens.sval.equals("extends"))
         throw new ParsingDSDException("{", tokens);
       tokens.wordChars('.', '.');
       try {
         if (tokens.nextToken() != StreamTokenizer.TT_WORD)
           throw new ParsingDSDException("<class name>", tokens);
       } finally {
         tokens.ordinaryChar('.');
       }
       superclass = tokens.sval;
     } else
       tokens.pushBack();
 
     naming = nameStore.add(dsd.packageName, dsd.getProjectName(), suffix, superclass);
 
     while (tokens.nextToken() == '(') {
       tokens.nextToken();
       TableQualifier.from(tokens).apply(this);
       DSD.expect(tokens, ')');
     }
 
     DSD.expect(tokens, '{');
     while (tokens.nextToken() != '}')
       fields.addElement(FieldDef.from(this, tokens, nextFieldDisplayOrder++));
 
     tokens.nextToken();
 
   }
 
   /**
    * @param importName name of import
    * @param destination "persistent", "table" or "both"
    */
   void addImport(String importName, String destination) {
     if (!destination.equals("table") && !destination.equals("persistent")
         && !destination.equals("both"))
       throw new RuntimeException(
                                  "Destination other than 'table', 'persistent' or 'both' used:"
                                      + destination);
 
     String existing = null;
     existing = (String) imports.put(importName, destination);
     if (existing != null && existing != destination)
       imports.put(importName, "both");
   }
 
   private final TableDef this_ = this;
 
   /**
    * @param w
    *        DatabaseBase
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generateTableDeclJava(Writer w)
       throws IOException {
     if (!isAbstract)
       w.write("  private " + naming.tableMainClassUnambiguous() + " tab_"
           + name + " = null;\n");
   }
 
   /**
    * @param w
    *        DatabaseBase
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generateTableDefnJava(Writer w)
       throws IOException {
     if (!isAbstract)
       w.write("    redefineTable(tab_" + name + " = " + "new "
           + naming.tableMainClassUnambiguous() + "(this, \"" + name + "\", "
           + "DefinitionSource.dsd));\n");
   }
 
   /**
    * @param w
    *        DatabaseBase
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generateTableAccessorJava(Writer w)
       throws IOException {
 
     // if we subclass a table with the same name we need to cast the table to
     // have the same return type as the root superclass
     String requiredReturnClass = naming.tableMainClassRootReturnClass();
 
     if (!isAbstract) {
       w.write("\n /**\n" + "  * Retrieves the "
           + naming.tableMainClassShortName() + " table.\n" + "  *\n"
           + "  * @generator " + "org.melati.poem.prepro.TableDef"
           + "#generateTableAccessorJava \n" + "  * @return the "
           + requiredReturnClass + " from this database\n" + "  */\n");
       w.write("  public " + requiredReturnClass + " get"
           + naming.tableMainClassShortName() + "() {\n" + "    return ");
       if (!requiredReturnClass.equals(naming.tableMainClassUnambiguous()))
         w.write("(" + requiredReturnClass + ")");
       w.write("tab_" + name + ";\n  }\n");
     }
   }
 
   /**
    * @param w
    *        DatabaseTablesBase
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generateTableAccessorDefnJava(Writer w)
       throws IOException {
     if (!isAbstract) {
       w.write("\n /**\n"
           + "  * Retrieves the <code>"
           + naming.tableMainClassShortName()
           + "</code> table"
           + (naming.tableMainClassRootReturnClass().equals(
               naming.tableMainClassShortName()) ? ".\n" : ("as a  <code>"
               + naming.tableMainClassRootReturnClass() + "</code>.\n"))
           + "  * \n" + "  * @generator " + "org.melati.poem.prepro.TableDef"
           + "#generateTableAccessorDefnJava \n" + "  * @return the "
           + naming.tableMainClassRootReturnClass() + " from this database\n"
           + "  */\n");
       w.write("  " + naming.tableMainClassRootReturnClass() + " get"
           + naming.tableMainClassShortName() + "();\n");
     }
   }
 
   /**
    * @param w
    *        Persistent Base writer
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generatePersistentBaseJava(Writer w)
       throws IOException {
 
     w.write("\n");
     for (Enumeration e = persistentBaseImports.elements(); e.hasMoreElements();) {
       w.write("import " + e.nextElement() + ";\n");
     }
     w.write("\n");
 
     // if we subclass a table with the same name we need to cast the table to
     // have the same return type as the root superclass
     String requiredReturnClass = naming.tableMainClassRootReturnClass();
 
     w.write("\n" + "/**\n"
         + " * Melati POEM generated abstract base class for a "
         + "<code>Persistent</code> \n" + 
         " * <code>" + suffix + "</code> Object.\n" + " *\n" + 
         " * @generator "
         + "org.melati.poem.prepro.TableDef" + "#generatePersistentBaseJava \n" + 
         " */\n");
     w.write("public abstract class " + naming.baseClassShortName()
         + " extends " + naming.superclassMainShortName() + " {\n" + "\n");
 
     w.write("\n /**\n" + 
             "  * Retrieves the Database object.\n" + "  * \n" + 
             "  * @generator " + "org.melati.poem.prepro.TableDef" + "#generatePersistentBaseJava \n" + 
             "  * @return the database\n" + "  */\n");
     w.write("  public " + dsd.databaseTablesClassName + " get"
         + dsd.databaseTablesClassName + "() {\n" + "    return ("
         + dsd.databaseTablesClassName + ")getDatabase();\n" + "  }\n" + "\n");
 
     w.write("\n /**\n" + "  * Retrieves the  <code>"
         + naming.tableMainClassShortName() + "</code> table \n"
         + "  * which this <code>Persistent</code> is from.\n" + "  * \n"
         + "  * @generator " + "org.melati.poem.prepro.TableDef"
         + "#generatePersistentBaseJava \n" 
         + "  * @return the " + requiredReturnClass
         + "\n" + "  */\n");
     w.write("  public " + requiredReturnClass + " "
         + naming.tableAccessorMethod() + "() {\n" + "    return ("
         + requiredReturnClass + ")getTable();\n" + "  }\n\n");
       
     if (!fields.elements().hasMoreElements()) {
       w.write("  // There are no Fields in this table, only in its ancestors \n");
     } else {
       w.write("  private " + naming.tableMainClassUnambiguous() + " _"
           + naming.tableAccessorMethod() + "() {\n" + "    return ("
           + naming.tableMainClassUnambiguous() + ")getTable();\n" + "  }\n\n");
 
       w.write("  // Fields in this table \n");
       for (Enumeration f = fields.elements(); f.hasMoreElements();) {
         FieldDef fd = (FieldDef) f.nextElement();
         w.write(" /**\n");
         w.write(DSD.javadocFormat(2, 1,
             ((fd.displayName != null) ? fd.displayName : fd.name)
                 + ((fd.description != null) ? " - " + fd.description : "")));
         w.write("  */\n");
         w.write("  protected ");
         fd.generateJavaDeclaration(w);
 
         w.write(";\n");
       }
 
       for (Enumeration f = fields.elements(); f.hasMoreElements();) {
         FieldDef field = (FieldDef) f.nextElement();
         w.write('\n');
         field.generateBaseMethods(w);
         w.write('\n');
         field.generateFieldCreator(w);
       }
     }
 
     w.write("}\n");
   }
 
   /**
    * @param w
    *        Persistent writer
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generatePersistentJava(Writer w)
       throws IOException {
 
     w.write("import " + dsd.packageName + ".generated."
         + naming.baseClassShortName() + ";\n");
     w.write("\n/**\n"
         + " * Melati POEM generated, programmer modifiable stub \n"
         + " * for a <code>Persistent</code> <code>"
         + naming.mainClassShortName() + "</code> object.\n");
     w.write(" * \n"
         + (description != null ? " * <p> \n"
             + " * Description: \n"
             + DSD.javadocFormat(1, 3, (description + ((description
                 .lastIndexOf(".") != description.length() - 1) ? "." : "")))
             + " * </p>\n" : ""));
     w.write(fieldSummaryTable());
     w.write(" * \n" + " * @generator " + "org.melati.poem.prepro.TableDef"
         + "#generatePersistentJava \n" + " */\n");
     w.write("public class " + naming.mainClassShortName() + " extends "
         + naming.baseClassShortName() + " {\n");
 
     w.write("\n /**\n"
             + "  * Constructor \n"
             + "  * for a <code>Persistent</code> <code>"
             + naming.mainClassShortName()
             + "</code> object.\n"
             + (description != null ? ("  * <p>\n"
                 + "  * Description: \n"
                 + DSD
                     .javadocFormat(2, 3, (description + ((description
                         .lastIndexOf(".") != description.length() - 1) ? "."
                         : ""))) + "  * </p>\n") : "") + "  * \n"
             + "  * @generator " + "org.melati.poem.prepro.TableDef"
             + "#generatePersistentJava \n" + "  */\n");
 
     w.write("  public " + naming.mainClassShortName() + "() { }\n" + "\n"
         + "  // programmer's domain-specific code here\n" + "}\n");
   }
 
   /**
    * @param w
    *        TableBase
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generateTableBaseJava(Writer w)
       throws IOException {
 
     for (Enumeration e = tableBaseImports.elements(); e.hasMoreElements();)
       w.write("import " + e.nextElement() + ";\n");
 
     w.write("\n");
     w.write("\n" + "/**\n" + " * Melati POEM generated base class for \n"
         + "<code>Table</code> <code>" + suffix + "</code>.\n");
     w.write(" *\n" 
         + " * @generator " + "org.melati.poem.prepro.TableDef"
         + "#generateTableBaseJava \n" + " */\n");
     w.write("\npublic class " + naming.tableBaseClassShortName() + " extends "
         + naming.superclassTableShortName() + " {\n" + "\n");
 
     for (Enumeration f = fields.elements(); f.hasMoreElements();) {
       w.write("  private ");
       ((FieldDef) f.nextElement()).generateColDecl(w);
       w.write(" = null;\n");
     }
 
     w.write("\n /**\n" + "  * Constructor. \n" 
         + "  * \n" 
         + "  * @generator " + "org.melati.poem.prepro.TableDef" + "#generateTableBaseJava \n"
         + "  * @param database          the POEM database we are using\n"
         + "  * @param name              the name of this <code>Table</code>\n"
         + "  * @param definitionSource  which definition is being used\n"
         + "  * @throws PoemException    if anything goes wrong\n" + "  */\n");
     w.write("\n" + "  public " + naming.tableBaseClassShortName() + "(\n"
         + "      Database database, String name,\n"
         + "      DefinitionSource definitionSource)"
         + " throws PoemException {\n"
         + "    super(database, name, definitionSource);\n" + "  }\n" + "\n");
 
     
     //w.write("\n /**\n" + "  * Constructor.\n" 
     //    + "  *\n" 
     //    + "  * @generator "
     //    + "org.melati.poem.prepro.TableDef" + "#generateTableBaseJava \n"
     //    + "  * @param database          the POEM database we are using\n"
     //    + "  * @param name              the name of this <code>Table</code>\n"
     //    + "  * @throws PoemException    if anything goes wrong\n" 
     //    + "  */\n");
     //w.write("  public " + naming.tableBaseClassShortName() + "(\n"
     //    + "      Database database, String name)" 
     //    + " throws PoemException {\n"
     //    + "    this(database, name, DefinitionSource.dsd);\n" + "  }\n" 
     //    + "\n");
     
     w.write("\n /**\n" + "  * Get the database tables.\n" + "  *\n"
         + "  * @generator " + "org.melati.poem.prepro.TableDef"
         + "#generateTableBaseJava \n" + "  * @return the database tables\n"
         + "  */\n");
     w.write("  public " + dsd.databaseTablesClassName + " get"+ dsd.databaseTablesClassName + "() {\n" + 
         "    return (" + dsd.databaseTablesClassName + ")getDatabase();\n" + 
         "  }\n" + 
         "\n" + 
         "  protected void init() throws PoemException {\n" + 
         "    super.init();\n");
 
     for (Enumeration f = fields.elements(); f.hasMoreElements();) {
       ((FieldDef) f.nextElement()).generateColDefinition(w);
       if (f.hasMoreElements())
         w.write('\n');
     }
 
     w.write("  }\n" + "\n");
 
     for (Enumeration f = fields.elements(); f.hasMoreElements();) {
       ((FieldDef) f.nextElement()).generateColAccessor(w);
       w.write('\n');
     }
 
     // if we subclass a table with the same name we need to cast the table to
     // have the same return type as the root superclass
     String requiredReturnClass = naming.mainClassRootReturnClass();
 
     w.write("\n /**\n" + "  * Retrieve the <code>"
         + naming.mainClassShortName() + "</code> as a <code>"
         + requiredReturnClass + "</code>.\n" 
         + "  *\n" 
         + "  * @generator "
         + "org.melati.poem.prepro.TableDef" + "#generateTableBaseJava \n"
         + "  * @param troid a Table Row Oject ID\n"
         + "  * @return the <code>Persistent</code> identified "
         + "by the <code>troid</code>\n" + "  */\n");
     w.write("  public " + requiredReturnClass + " get"
         + naming.mainClassShortName() + "Object(" + "Integer troid) {\n"
         + "    return (" + requiredReturnClass + ")getObject(troid);\n"
         + "  }\n" + "\n");
 
     w.write("\n /**\n" + "  * Retrieve the <code>"
         + naming.mainClassShortName() + "</code> \n" 
         + "  * as a <code>" + requiredReturnClass + "</code>.\n" 
         + "  *\n" 
         + "  * @generator "
         + "org.melati.poem.prepro.TableDef" + "#generateTableBaseJava \n"
         + "  * @param troid a Table Row Object ID\n"
         + "  * @return the <code>Persistent</code> identified " + "  */\n");
     w.write("  public " + requiredReturnClass + " get"
         + naming.mainClassShortName() + "Object(" + "int troid) {\n"
         + "    return (" + requiredReturnClass + ")getObject(troid);\n"
         + "  }\n");
 
     if (!isAbstract)
       w.write("\n" + "  protected JdbcPersistent _newPersistent() {\n"
           + "    return new " + naming.mainClassUnambiguous() + "();\n" + "  }"
           + "\n");
     
     if (displayName != null)
       w.write("  protected String defaultDisplayName() {\n" + "    return "
           + StringUtils.quoted(displayName, '"') + ";\n" + "  }\n" + "\n");
 
     if (description != null)
       w.write("  protected String defaultDescription() {\n" + "    return "
           + StringUtils.quoted(description, '"') + ";\n" + "  }\n" + "\n");
 
     if (seqCached)
       w.write("  protected boolean defaultRememberAllTroids() {\n"
           + "    return true;\n" + "  }\n" + "\n");
 
     if (cacheSize != CacheSizeTableQualifier.DEFAULT)
       w.write("  protected Integer defaultCacheLimit() {\n"
           + "    return new Integer("
           + (cacheSize == CacheSizeTableQualifier.UNLIMITED ? "999999999" : ""
               + cacheSize) + ");\n" + "  }\n" + "\n");
 
     if (category != null)
       w.write("  protected String defaultCategory() {\n" + "    return "
           + StringUtils.quoted(category, '"') + ";\n" + "  }\n" + "\n");
 
     w.write("  protected int defaultDisplayOrder() {\n" + "    return "
         + displayOrder + ";\n" + "  }\n");
 
     w.write("}\n");
   }
 
   /**
    * @param w
    *        Table
    * @throws IOException
    *         if a problem with the file system is encountered
    */
   public void generateTableJava(Writer w)
       throws IOException {
 
     w.write("import " + naming.tableBaseClassFQName() + ";\n");
     w.write("import org.melati.poem.DefinitionSource;\n");
     w.write("import org.melati.poem.Database;\n");
     w.write("import org.melati.poem.PoemException;\n");
 
     w.write("\n/**\n"
         + " * Melati POEM generated, programmer modifiable stub \n"
         + " * for a <code>"
         + naming.tableMainClassShortName()
         + "</code> object.\n"
         + (description != null ? " * <p>\n"
             + " * Description: \n"
             + DSD.javadocFormat(1, 3, (description + ((description
                 .lastIndexOf(".") != description.length() - 1) ? "." : "")))
             + " * </p>\n" : "") + " *\n");
     w.write(fieldSummaryTable());
     w.write(" * \n" 
         + " * @generator  " + "org.melati.poem.prepro.TableDef" + "#generateTableJava \n" + " */\n");
     w.write("public class " + naming.tableMainClassShortName() + " extends "
         + naming.tableBaseClassShortName() + " {\n");
     Object o = new Object() {
       public String toString() {
         return "\n /**\n"
             + "  * Constructor.\n"
             + "  * \n"
             + "  * @generator " + "org.melati.poem.prepro.TableDef" + "#generateTableJava \n"
             + "  * @param database          the POEM database we are using\n"
             + "  * @param name              the name of this <code>Table</code>\n"
             + "  * @param definitionSource  which definition is being used\n"
             + "  * @throws PoemException    if anything goes wrong\n"
             + "  */\n";
       }
     };
     w.write(o.toString());
     w.write("  public " + naming.tableMainClassShortName() + "(\n"
         + "      Database database, String name,\n"
         + "      DefinitionSource definitionSource)"
         + " throws PoemException {\n"
         + "    super(database, name, definitionSource);\n" + "  }\n" + "\n"
         + "  // programmer's domain-specific code here\n" + "}\n");
   }
 
   /**
    * Generate the 4 files.
    *
    * @throws IOException
    *         if a problem with the file system is encountered
    * @throws IllegalityException
    *         if a semantic incoherence is detected
    */
   public void generateJava()
       throws IOException, IllegalityException {
 
     boolean hasDisplayLevel = false;
     boolean hasSearchability = false;
     int fieldCount = 0;
     for (Enumeration e = fields.elements(); e.hasMoreElements();) {
       fieldCount++;
       FieldDef f = (FieldDef) e.nextElement();
       if (f.displayLevel != null)
         hasDisplayLevel = true;
       if (f.searchability != null)
         hasSearchability = true;
     }
     if (fieldCount == 0 && !isAbstract && naming.superclass == null)
       throw new NonAbstractEmptyTableException(name);
 
     if (!isAbstract)
       addImport("org.melati.poem.JdbcPersistent", "table");
     if (hasDisplayLevel)
       addImport("org.melati.poem.DisplayLevel", "table");
     if (hasSearchability)
       addImport("org.melati.poem.Searchability", "table");
     addImport(naming.tableFQName, "table");
     if (definesColumns) {
       addImport("org.melati.poem.Column", "both");
       addImport("org.melati.poem.Field", "both");
     }
     if (naming.superclassMainUnambiguous().equals("JdbcPersistent")) {
       addImport("org.melati.poem.JdbcPersistent", "persistent");
     } else {
       addImport(naming.superclassMainFQName(), "persistent");
     }
 
     // we may not have any fields in an in an overridden class
     // but we need the import for getTable
     addImport(naming.tableMainClassFQName(), "persistent");
     addImport(dsd.packageName + "." + dsd.databaseTablesClassName, "persistent");
 
     addImport("org.melati.poem.Database", "table");
     addImport("org.melati.poem.DefinitionSource", "table");
     addImport("org.melati.poem.PoemException", "table");
 
     if (!isAbstract)
       addImport("org.melati.poem.Persistent", "table");
 
     if (naming.superclassTableUnambiguous().equals("Table")) {
       addImport("org.melati.poem.Table", "table");
     } else {
       addImport(naming.superclassTableFQName(), "table");
     }
     addImport(dsd.packageName + "." + dsd.databaseTablesClassName, "table");
 
     // Sort out the imports
     for (Enumeration i = imports.keys(); i.hasMoreElements();) {
       String fqKey;
       String key = (String) i.nextElement();
       if (key.indexOf(".") == -1) {
         TableNamingInfo targetTable = (TableNamingInfo) dsd.nameStore.tablesByShortName
             .get(key);
         if (targetTable == null)
          throw new RuntimeException("No TableNamingInfo for " + key + 
                  ". This is probably a typo either in the table definition name or in a reference field.");
         fqKey = targetTable.tableFQName;
         String destination = (String) imports.get(key);
         imports.remove(key);
         addImport(fqKey, destination);
       }
     }
     for (Enumeration i = imports.keys(); i.hasMoreElements();) {
       String fqKey;
       String key = (String) i.nextElement();
 
       if (key.indexOf(".") == -1) {
         TableNamingInfo targetTable = 
             (TableNamingInfo)dsd.nameStore.tablesByShortName.get(key);
         fqKey = targetTable.tableFQName;
       } else {
         fqKey = key;
       }
       String destination = (String) imports.get(key);
       if (destination == "table") {
         tableBaseImports.addElement(fqKey);
       } else if (destination == "persistent") {
         persistentBaseImports.addElement(fqKey);
       } else {
         tableBaseImports.addElement(fqKey);
         persistentBaseImports.addElement(fqKey);
       }
     }
     Object[] t = tableBaseImports.toArray();
     Object[] p = persistentBaseImports.toArray();
     Arrays.sort(t);
     Arrays.sort(p);
     tableBaseImports.removeAllElements();
     persistentBaseImports.removeAllElements();
     for (int i = 0; i < t.length; i++)
       tableBaseImports.addElement(t[i]);
     for (int i = 0; i < p.length; i++)
       persistentBaseImports.addElement(p[i]);
 
     dsd.createJava(naming.baseClassShortName(), new Generator() {
       public void process(Writer w)
           throws IOException {
         this_.generatePersistentBaseJava(w);
       }
     }, true);
 
     dsd.createJava(naming.mainClassShortName(), new Generator() {
       public void process(Writer w)
           throws IOException {
         this_.generatePersistentJava(w);
       }
     }, false);
 
     dsd.createJava(naming.tableBaseClassShortName(), new Generator() {
       public void process(Writer w)
           throws IOException {
         this_.generateTableBaseJava(w);
       }
     }, true);
 
     dsd.createJava(naming.tableMainClassShortName(), new Generator() {
       public void process(Writer w)
           throws IOException {
         this_.generateTableJava(w);
       }
     }, false);
   }
 
   String fieldSummaryTable() {
     StringBuffer table = new StringBuffer();
     table.append(" * \n" + " * <table> \n" + " * <tr><th colspan='3'>\n"
         + " * Field summary for SQL table <code>" + suffix + "</code>\n"
         + " * </th></tr>\n"
         + " * <tr><th>Name</th><th>Type</th><th>Description</th></tr>\n");
     for (Enumeration f = fields.elements(); f.hasMoreElements();) {
       FieldDef fd = (FieldDef) f.nextElement();
       table.append(DSD.javadocFormat(1, 1, "<tr><td> " + fd.name
           + " </td><td> " + fd.type + " </td><td> "
           + ((fd.description != null) ? fd.description : "&nbsp;")
           + " </td></tr>"));
     }
     table.append(" * </table> \n");
     return table.toString();
   }
 }
