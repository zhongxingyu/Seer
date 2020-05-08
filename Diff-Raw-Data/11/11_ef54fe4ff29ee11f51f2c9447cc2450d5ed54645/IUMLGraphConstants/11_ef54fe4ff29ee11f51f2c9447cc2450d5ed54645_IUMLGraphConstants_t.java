 package org.lightuml.ui;
 /**
  * <p>
  * 	An interface for holding UMLGraph constants, etc. 
  *  Note: descriptions for parameters mostly from UMLGraph documentation.
  * </p>
  * @author Antti Hakala
  *
  */
 public interface IUMLGraphConstants {
     public String[][] UMLGRAPH_JAVADOC_TAGS = { 
             { "assoc", "Add an association relationship. @assoc takes four arguments: 1. The source adornments (role, multiplicity, and visibility). 2. The relationship name. 3. The target adornments (role, multiplicity, and visibility). 4. The target class. E.g. @assoc 1 - 1 org.foo.Bar"},
             { "composed","Add a composition relationship. @composed takes four arguments: 1. The source adornments (role, multiplicity, and visibility). 2. The relationship name. 3. The target adornments (role, multiplicity, and visibility). 4. The target class. E.g. @composed 1 - 1 org.foo.Bar"}, 
             { "depend","Add a dependency relationship. @depend takes four arguments: 1. The source adornments (role, multiplicity, and visibility). 2. The relationship name. 3. The target adornments (role, multiplicity, and visibility). 4. The target class. E.g. @depend 1 - 1 org.foo.Bar"},
             { "extends", "Add a generalization relationship. @extends takes one argument, the class extended. E.g. @extends org.foo.Bar"}, 
             { "has","Add an aggregation relationship. @has takes four arguments: 1. The source adornments (role, multiplicity, and visibility). 2. The relationship name. 3. The target adornments (role, multiplicity, and visibility). 4. The target class. E.g. @has 1 - 2 org.foo.Bar"},
             { "hidden","Hide from the diagram. No arguments."}, 
             { "navassoc","Add a navigatable (directed) association. @navassoc takes four arguments: 1. The source adornments (role, multiplicity, and visibility). 2. The relationship name. 3. The target adornments (role, multiplicity, and visibility). 4. The target class. E.g. @navassoc 1 - 1 org.foo.Bar"}, 
             { "opt ","Specify a UMLGraph option. Takes one option as an argument. E.g. @opt attributes."},
             { "stereotype","Indicate a stereotype. Takes the stereotype as an argument. E.g. @stereotype container"},
             { "tagvalue", "Add a tagged value. Takes two arguments, the tag and the value. E.g. @tagvalue version 1.0"},
             
             { "view", "Marks a special class used to describe a single class diagram. Similarly to UMLOptions, the view can define its own general options, but allows to define overrides that allow to adopt different options for different classes based on regular expressions matching."},
             { "match class ", "Apply following options to classes that match the regular expression."}
             };
 
     public String[][] VERSION_DOCLET_NAME = {
            { "UMLGraph version 5.6 (and possibly newer) - works with java 1.6",
            		"org.umlgraph.doclet.UmlGraph" },
            { "UMLGraph version 4.4 - works with java 1.5",
                     "gr.spinellis.umlgraph.doclet.UmlGraph" },
             { "UMLGraph version 2.10 - works with java 1.4", "UmlGraph" }
 
     };
 
     public String P_UMLGRAPH_PARAM_ALL = "umlgraph-param-all";
     public String P_UMLGRAPH_PARAM_ATTRIBUTES = "umlgraph-param-attributes";
     public String P_UMLGRAPH_PARAM_CONSTRUCTORS = "umlgraph-param-constructors";
     public String P_UMLGRAPH_PARAM_HORIZONTAL = "umlgraph-param-horizontal";
     public String P_UMLGRAPH_PARAM_OPERATIONS = "umlgraph-param-operations";
     public String P_UMLGRAPH_PARAM_QUALIFY = "umlgraph-param-qualify";
     public String P_UMLGRAPH_PARAM_TYPES = "umlgraph-param-types";
     public String P_UMLGRAPH_PARAM_VISIBILITY = "umlgraph-param-visibility";
     public String P_UMLGRAPH_PARAM_NOGUILLEMOT = "umlgraph-param-noguillemot";
     
     public String P_UMLGRAPH_PARAM_CMDLINE = "umlgraph-param-cmdline";
     
     // LightUML >= 1.2.6:
     public String P_UMLGRAPH_PARAM_ENUMERATIONS = "umlgraph-param-enumerations";
     public String P_UMLGRAPH_PARAM_ENUMCONSTANTS = "umlgraph-param-enumconstants";
     public String P_UMLGRAPH_PARAM_VIEWS = "umlgraph-param-views";
     public String P_UMLGRAPH_PARAM_INFERDEP = "umlgraph-param-inferdep";
     public String P_UMLGRAPH_PARAM_INFERREL = "umlgraph-param-inferrel";
     public String P_UMLGRAPH_PARAM_USEIMPORTS = "umlgraph-param-useimports";
     public String P_UMLGRAPH_PARAM_POSTFIXPACKAGE = "umlgraph-param-postfixpackage";
     public String P_UMLGRAPH_PARAM_INFERDEPINPACKAGE = "umlgraph-param-inferdepinpackage";
     public String P_UMLGRAPH_PARAM_COMPACT = "umlgraph-param-compact";
     public String P_UMLGRAPH_PARAM_LINK = "umlgraph-param-link";
     // ---
     
     public String[][] UMLGRAPH_PARAMS_INFERENCE = {
             { "inferdep", "[-inferdep] Try to automatically infer dependencies between classes by inspecting methods and fields.", P_UMLGRAPH_PARAM_INFERDEP},
             { "inferdepinpackage", "[-inferdepinpackage] Enable or disable dependency inference among classes in the same package.\nThis option is disabled by default, because classes in the same package are supposed to be related anyway,\nand also because there's no working mechanism to actually detect all of these dependencies since imports\nare not required to use classes in the same package.", P_UMLGRAPH_PARAM_INFERDEPINPACKAGE},
             { "inferrel", "[-inferrel] Try to automatically infer relationships between classes by inspecting field values.", P_UMLGRAPH_PARAM_INFERREL},
             { "useimports", "[-useimports] Will also use imports to infer dependencies.", P_UMLGRAPH_PARAM_USEIMPORTS },
     };
     
     public String[][] UMLGRAPH_PARAMS_OTHER = {
     		{ "all", "[-all] Same as -attributes -operations -visibility -types -enumerations -enumconstants", P_UMLGRAPH_PARAM_ALL},
             { "attributes", "[-attributes] Show class attributes", P_UMLGRAPH_PARAM_ATTRIBUTES },
             { "compact", "[-compact] Generate compact dot files, that is, print html labels in a single line instead of \"pretty printing\" them.\nUseful if the dot file has to be manipulated by an automated tool (e.g., the UMLGraph regression test suite).", P_UMLGRAPH_PARAM_COMPACT },
             { "constructors", "[-constructors] Show class constructors", P_UMLGRAPH_PARAM_CONSTRUCTORS },
             { "enumerations", "[-enumerations] Show enumarations as separate stereotyped primitive types.", P_UMLGRAPH_PARAM_ENUMERATIONS },
             { "enumconstants", "[-enumconstants] When showing enumerations, also show the values they can take.", P_UMLGRAPH_PARAM_ENUMCONSTANTS },
             { "horizontal", "[-horizontal] Layout in horizontal direction", P_UMLGRAPH_PARAM_HORIZONTAL },            
             { "link", "[-link] A clone of the standard doclet -link option, allows UMLGraph to generate links from class symbols to\ntheir external javadoc documentation (image maps are automatically generated in UMLGraphDoc,\nyou'll have to generate them manually with graphviz if using UMLGraph).", P_UMLGRAPH_PARAM_LINK},
             { "noguillemot", "[-noguillemot] Don't use guillemot characters", P_UMLGRAPH_PARAM_NOGUILLEMOT },
             { "operations", "[-operations] Show class operations.", P_UMLGRAPH_PARAM_OPERATIONS },
             { "postfixpackage", "[-postfixpackage] When using qualified class names, put the package name in the line after the class\nname, in order to reduce the width of class nodes.", P_UMLGRAPH_PARAM_POSTFIXPACKAGE },
             { "qualify", "[-qualify] Produce fully-qualified class names", P_UMLGRAPH_PARAM_QUALIFY },
             { "types", "[-types] Type information on attributes and operations", P_UMLGRAPH_PARAM_TYPES },
             { "views", "[-views] Generate a class diagram for every view found in the source path.", P_UMLGRAPH_PARAM_VIEWS},
             { "visibility","[-visibility] Adorn class elements according to their visibility", P_UMLGRAPH_PARAM_VISIBILITY }      
     };
          
     public String[][] UMLGRAPH_PARAMS_OLD = {
             { "all", "[-all] Same as -attributes -operations -visibility -types -enumerations -enumconstants", P_UMLGRAPH_PARAM_ALL},
             { "attributes", "[-attributes] Show class attributes", P_UMLGRAPH_PARAM_ATTRIBUTES },
             { "constructors", "[-constructors] Show class constructors", P_UMLGRAPH_PARAM_CONSTRUCTORS },
             { "horizontal", "[-horizontal] Layout in horizontal direction", P_UMLGRAPH_PARAM_HORIZONTAL },
             { "operations", "[-operations] Show class operations", P_UMLGRAPH_PARAM_OPERATIONS },
             { "qualify", "[-qualify] Produce fully-qualified class names", P_UMLGRAPH_PARAM_QUALIFY },
             { "types", "[-types] Type information on attributes and operations", P_UMLGRAPH_PARAM_TYPES },
             { "visibility","[-visibility] Adorn class elements according to their visibility", P_UMLGRAPH_PARAM_VISIBILITY },
             { "noguillemot", "[-noguillemot] Don't use guillemot characters", P_UMLGRAPH_PARAM_NOGUILLEMOT }
     };
     
     // params that don't have UI-component but can be autocompleted in javadoc
     public String[][] UMLGRAPH_PARAMS_NOUI = {
             { "outputencoding", "Specify the output encoding character set (default ISO-8859-1). When using dot to generate SVG diagrams you should specify UTF-8 as the output encoding, to have guillemots correctly appearing in the resulting SVG"},
             { "nodefontname", "Specify the font name to use inside nodes."},
             { "nodefontabstractname", "Specify the font name to use inside abstract class nodes."},
             { "nodefontsize", "Specify the font size to use inside nodes."},
             { "nodefontclassname", "Specify the font name to use for the class names."},
             { "nodefontclassabstractname", "Specify the font name use for the class name of abstract classes."},
             { "nodefontclasssize", "Specify the font size to use for the class names."},
             { "nodefonttagname", "Specify the font name to use for the tag names."},
             { "nodefonttagsize", "Specify the font size to use for the tag names."},
             { "nodefontpackagename", "Specify the font name to use for the package names (used only when the package name is postfixed, see -postfixpackage)."},
             { "nodefontpackagesize", "Specify the font size to use for the package names (used only when it package name is postfixed, see -postfixpackage)."},
             
             { "apidocroot", "Specify the URL that should be used as the \"root\" for local classes. This URL will be used as a prefix, to which the page name for the local class or package will be appended (following the JavaDoc convention). For example, if the value http://www.acme.org/apidocs is provided, the class org.acme.util.MyClass will be mapped to the URL http://www.acme.org/apidocs/org/acme/util/MyClass.html. This URL will then be added to .dot diagram and can be surfaced in the final class diagram by setting the output to SVG, or by creating an HTML page that associates the diagram static image (a .gif or .png) with a client-side image map."},
             { "apidocmap", "Specify the file name of the URL mapping table. The is a standard Java property file, where the property name is a regular expression (as defined in the java.util.regex package) and the property value is an URL \"root\" as described above. This table is used to resolved external class names (class names that do not belong to the current package being processed by UMLGraph). If no file is provided, external classes will just be mapped to the on-line Java API documentation."},
             { "collpackages", "Specify the classes that will be treated as containers for one to many relationships when inference is enabled. Matching is done using a non-anchored regular match. Empty by default."},
             { "edgefontname", "Specify the font name to use for edge labels."},
             { "edgefontsize", "Specify the font size to use for edge labels."},
             { "view", "Specify the fully qualified name of a class that contains a view definition. Only the class diagram specified by this view will be generated."},
             { "inferdepvis","Specifies the lowest visibility level of elements used to infer dependencies among classes. Possible values are private, package, protected, public, in this order. The default value is private. Use higher levels to limit the number of inferred dependencies."},
             { "inferreltype","The type of relationship inferred when -inferrel is activated. Defaults to \"navassoc\"."},
             { "collpackages", "Specify the classes that will be treated as containers for one to many relationships when inference is enabled. Matching is done using a non-anchored regular match."},
             { "apidocroot","Specify the URL that should be used as the \"root\" for local classes. This URL will be used as a prefix, to which the page name for the local class or package will be appended (following the JavaDoc convention). For example, if the value http://www.acme.org/apidocs is provided, the class org.acme.util.MyClass will be mapped to the URL http://www.acme.org/apidocs/org/acme/util/MyClass.html. This URL will then be added to .dot diagram and can be surfaced in the final class diagram by setting the output to SVG, or by creating an HTML page that associates the diagram static image (a .gif or .png) with a client-side image map."},
             { "apidocmap", "Specify the file name of the URL mapping table. The is a standard Java property file, where the property name is a regular expression (as defined in the java.util.regex package) and the property value is an URL \"root\" as described above. This table is used to resolved external class names (class names that do not belong to the current package being processed by UMLGraph). If no file is provided, external classes will just be mapped to the on-line Java API documentation."},
             { "hide", "Specify entities to hide from the graph. Matching is done using a non-anchored regular match. " },
     };
     public String[][] UMLGRAPH_PARAMS_NOUI_OLD = {
             { "outputencoding", "Specify the output encoding character set (default ISO-8859-1). When using dot to generate SVG diagrams you should specify UTF-8 as the output encoding, to have guillemots correctly appearing in the resulting SVG"},
             { "nodefontname", "Specify the font name to use inside nodes."},
             { "nodefontabstractname", "Specify the font name to use inside abstract class nodes."},
             { "nodefontsize", "Specify the font size to use inside nodes."},
             { "edgefontname", "Specify the font name to use for edge labels."},
             { "edgefontsize", "Specify the font size to use for edge labels."},
             { "apidocroot","Specify the URL that should be used as the \"root\" for local classes. This URL will be used as a prefix, to which the page name for the local class or package will be appended (following the JavaDoc convention). For example, if the value http://www.acme.org/apidocs is provided, the class org.acme.util.MyClass will be mapped to the URL http://www.acme.org/apidocs/org/acme/util/MyClass.html. This URL will then be added to .dot diagram and can be surfaced in the final class diagram by setting the output to SVG, or by creating an HTML page that associates the diagram static image (a .gif or .png) with a client-side image map."},
             { "apidocmap", "Specify the file name of the URL mapping table. The is a standard Java property file, where the property name is a regular expression (as defined in the java.util.regex package) and the property value is an URL \"root\" as described above. This table is used to resolved external class names (class names that do not belong to the current package being processed by UMLGraph). If no file is provided, external classes will just be mapped to the on-line Java API documentation."},
             { "hide", "Specify entities to hide from the graph. Matching is done using a non-anchored regular match. " },
     };
     
     public String P_UMLGRAPH_COLOR_NODEFILL = "umlgraph-color-nodefill";
     public String P_UMLGRAPH_COLOR_NODEFONT = "umlgraph-color-nodefont";
     public String P_UMLGRAPH_COLOR_EDGE = "umlgraph-color-edge";
     public String P_UMLGRAPH_COLOR_EDGEFONT = "umlgraph-color-edgefont";
     public String P_UMLGRAPH_COLOR_BG = "umlgraph-color-bg";
     
     // commandline switch (without -), description, default (r,g,b), property
     public String[][] UMLGRAPH_COLOR_PARAMS = {
             { "nodefillcolor", "Node Fill Color", "255,255,255", P_UMLGRAPH_COLOR_NODEFILL },
             { "nodefontcolor", "Node Font Color", "0,0,0", P_UMLGRAPH_COLOR_NODEFONT },
             { "edgecolor", "Edge Color", "0,0,0", P_UMLGRAPH_COLOR_EDGE },
             { "edgefontcolor", "Edge Font Color", "0,0,0", P_UMLGRAPH_COLOR_EDGEFONT },
             { "bgcolor", "Background Color", "255,255,255", P_UMLGRAPH_COLOR_BG  } };
 }
