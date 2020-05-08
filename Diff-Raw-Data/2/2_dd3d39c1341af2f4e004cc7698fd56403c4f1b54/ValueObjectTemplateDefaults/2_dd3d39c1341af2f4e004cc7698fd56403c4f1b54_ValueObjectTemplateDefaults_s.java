 /*
                         QueryJ
 
     Copyright (C) 2002  Jose San Leandro Armendariz
                         jsanleandro@yahoo.es
                         chousz@yahoo.com
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jsanleandro@yahoo.es
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Defines the default subtemplates used to generate value
  *              objects according to database metadata.
  *
  * Last modified by: $Author$ at $Date$
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *
  * $Id$
  *
  */
 package org.acmsl.queryj.tools.templates.valueobject;
 
 /**
  * Defines the default subtemplates used to generate value objects according
  * to database metadata.
  * @author <a href="mailto:jsanleandro@yahoo.es"
  *         >Jose San Leandro</a>
  * @version $Revision$
  */
 public interface ValueObjectTemplateDefaults
 {
     /**
      * The default header.
      */
     public static final String DEFAULT_HEADER =
           "/*\n"
         + "                        QueryJ\n"
         + "\n"
         + "    Copyright (C) 2002  Jose San Leandro Armendariz\n"
         + "                        jsanleandro@yahoo.es\n"
         + "                        chousz@yahoo.com\n"
         + "\n"
         + "    This library is free software; you can redistribute it and/or\n"
         + "    modify it under the terms of the GNU General Public\n"
         + "    License as published by the Free Software Foundation; either\n"
         + "    version 2 of the License, or any later "
         + "version.\n"
         + "\n"
         + "    This library is distributed in the hope that it will be "
         + "useful,\n"
         + "    but WITHOUT ANY WARRANTY; without even the implied warranty "
         + "of\n"
         + "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
         + "GNU\n"
         + "    General Public License for more details.\n"
         + "\n"
         + "    You should have received a copy of the GNU General Public\n"
         + "    License along with this library; if not, write to the Free "
         + "Software\n"
         + "    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  "
         + "02111-1307  USA\n"
         + "\n"
         + "    Thanks to ACM S.L. for distributing this library under the GPL "
         + "license.\n"
         + "    Contact info: jsanleandro@yahoo.es\n"
         + "    Postal Address: c/Playa de Lagoa, 1\n"
         + "                    Urb. Valdecabanas\n"
         + "                    Boadilla del monte\n"
         + "                    28660 Madrid\n"
         + "                    Spain\n"
         + "\n"
         + " *****************************************************************"
         + "*************\n"
         + " *\n"
         + " * Filename: $" + "RCSfile: $\n"
         + " *\n"
         + " * Author: QueryJ\n"
         + " *\n"
         + " * Description: Represents the \"{0}\" information stored in the\n"
         + " *              persistence domain.\n"
         + " *\n"
         + " * Last modified by: $" + "Author: $ at $" + "Date: $\n"
         + " *\n"
         + " * File version: $" + "Revision: $\n"
         + " *\n"
         + " * Project version: $" + "Name: $\n"
         + " *\n"
         + " * $" + "Id: $\n"
         + " *\n"
         + " */\n";
 
     /**
      * The package declaration.
      */
     public static final String PACKAGE_DECLARATION = "package {0};\n\n"; // package
 
     /**
      * The ACM-SL imports.
      */
     public static final String ACMSL_IMPORTS =
           "/*\n"
         + " * Importing some ACM-SL classes.\n"
         + " */\n\n";
 
     /**
      * The JDK imports.
      */
     public static final String JDK_IMPORTS =
           "/*\n"
         + " * Importing some JDK classes.\n"
         + " */\n"
         + "import java.math.BigDecimal;\n"
         + "import java.util.Calendar;\n"
         + "import java.util.Date;\n\n";
 
     /**
      * The default class Javadoc.
      */
     public static final String DEFAULT_JAVADOC =
           "/**\n"
         + " * Represents the <i>{0}</i> information in the persistence domain.\n" // table
         + " * @author <a href=\"http://maven.acm-sl.org/queryj\">QueryJ</a>\n"
         + " * @version $" + "Revision: $\n"
         + " */\n";
 
     /**
      * The class definition.
      */
     public static final String CLASS_DEFINITION =
            "public class {0}ValueObject\n"; // table
 
     /**
      * The class start.
      */
     public static final String DEFAULT_CLASS_START = "{\n";
 
     /**
      * The field declaration.
      */
     public static final String DEFAULT_FIELD_DECLARATION =
           "    /**\n"
         + "     * The <i>{0}</i> information.\n" // field
         + "     */\n"
         + "    private {1} {2};\n\n"; // field type - field
 
     /**
      * The class constructor.
      */
     public static final String DEFAULT_CONSTRUCTOR =
           "    /**\n"
        + "     * Creates a {0]ValueObject with given information.\n"
         + "{1}" // constructor field javadoc
         + "     */\n"
         + "    protected {0}ValueObject(" // table
         + "{2})\n" // constructor field declaration
         + "    '{'"
         + "{3}\n"  // constructor field value setter.
         + "    '}'\n";
 
     /**
      * The default constructor field Javadoc.
      */
     public static final String DEFAULT_CONSTRUCTOR_FIELD_JAVADOC =
         "     * @param {0} the {1} information.\n"; // field - FIELD;
 
     /**
      * The default constructor field declaration.
      */
     public static final String DEFAULT_CONSTRUCTOR_FIELD_DECLARATION =
         "\n        final {0} {1}"; // field type - field;
 
     /**
      * The default constructor field value setter.
      */
     public static final String DEFAULT_CONSTRUCTOR_FIELD_VALUE_SETTER =
         "\n        immutableSet{0}({1});"; // Field - field;
 
     /**
      * The default field setter method.
      */
     public static final String DEFAULT_FIELD_VALUE_SETTER_METHOD =
           "\n"
         + "    /**\n"
         + "     * Specifies the {0} information.\n" // field
         + "     * @param {0} the new {0} value.\n"
         + "     */\n"
         + "    private void immutableSet{2}(final {1} {0})\n" // capitalized field - field type
         + "    '{'\n"
         + "        this.{0} = {0};\n" // field
         + "    '}'\n\n"
         + "    /**\n"
         + "     * Specifies the {0} information.\n" // field
         + "     * @param {0} the new {0} value.\n"
         + "     */\n"
         + "    protected void set{2}({1} {0})\n" // capitalized field - field type
         + "    '{'\n"
         + "        immutableSet{2}({0});\n" // field
         + "    '}'\n";
 
     /**
      * The default field getter method.
      */
     public static final String DEFAULT_FIELD_VALUE_GETTER_METHOD =
           "\n"
         + "    /**\n"
         + "     * Retrieves the {0} information.\n" // field
         + "     * @return such value.\n"
         + "     */\n"
         + "    public {1} get{2}()\n" // field type - capitalized field
         + "    '{'\n"
         + "        return {0};\n" // field
         + "    '}'\n";
 
     /**
      * The default class end.
      */
     public static final String DEFAULT_CLASS_END = "}";
 }
