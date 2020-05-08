 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * LZX Classes
  */
 
 package org.openlaszlo.compiler;
 import java.util.*;
 import org.jdom.Element;
 import org.openlaszlo.sc.Method;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.sc.ScriptClass;
 import org.openlaszlo.xml.internal.MissingAttributeException;
 
 public class ClassModel implements Comparable {
     protected final ViewSchema schema;
     /** This is really the LZX tag name */
     public final String tagName;
     /** Classes that are created for single instances */
     public final boolean anonymous;
     /** And this is the actual class name */
     public String className;
     final CompilationEnvironment env;
     protected boolean builtin = false;
     // This is null for the root class
     protected ClassModel superModel;
     
     // This is null for the root class
     public final Element definition;
     protected String kind;
     protected NodeModel nodeModel;
     protected boolean modelOnly;
     
     /** Set of tags that can legally be nested in this element */
     protected Set mCanContainTags = new HashSet();
 
       /** Set of forbidden child tags of this element */
     protected Set mForbiddenTags = new HashSet();
 
     /* If superclass is a predefined system class, just store its name. */
     protected String superTagName = null;
     protected String mixinNames[] = null;
     protected boolean hasInputText = false;
     protected boolean isInputText = false;
     public Set requiredAttributes = new HashSet();
 
     /* Class or superclass has an <attribute type="text"/>  */
     protected boolean supportsTextAttribute = false;
     /** Map attribute name to type */
     public final Map attributeSpecs = new LinkedHashMap();
     protected final Map classAttributeSpecs = new LinkedHashMap();
 
     public boolean inline = false;
     protected String sortkey = null;
 
     // True when the super and mixin names have been resolved to class
     // models
     protected boolean resolved = false;
 
     public String toString() {
         return "ClassModel: tagName="+tagName + ", " + 
             "superclass=" + superModel + ", " + 
             "superTagName=" + superTagName + ", " + 
             "hasInputText=" + hasInputText + ", " + 
             "isInputText=" + isInputText + ", " + 
             "definition=" + definition;
     }
 
     static final String DEFAULT_SUPERCLASS_NAME = "view";
 
     // Construct a builtin class
     public ClassModel(String tagName, ViewSchema schema, CompilationEnvironment env) {
         this(tagName, true, schema, null, env);
     }
 
     // Construct a user-defined class
     public ClassModel(String tagName, boolean publish,
                       ViewSchema schema, Element definition, CompilationEnvironment env) {
         if (definition != null) {
           // class, interface, mixin; OR anonymous instance class
           kind = definition.getName();
           if ("class".equals(kind) || "interface".equals(kind) || "mixin".equals(kind)) {
             assert tagName.equals(definition.getAttributeValue("name"));
             superTagName = definition.getAttributeValue("extends");
             if (superTagName == null) {
                 superTagName = DEFAULT_SUPERCLASS_NAME;
             }
             String mixinSpec = definition.getAttributeValue("with");
             if (mixinSpec != null) {
                 mixinNames = mixinSpec.trim().split("\\s*,\\s*");
                 for (int i = mixinNames.length - 1; i >= 0; i--) {
                     String mixinName = mixinNames[i];
                     ClassModel mixinModel =  schema.getClassModelUnresolved(mixinName);
                     if (mixinModel == null) {
                          throw new CompilationError(
                              "Undefined mixin " + mixinName + " for class " + tagName,
                              definition);
                     }
                     String interstitialName = mixinName + "$" + superTagName;
 
                     // Avoid adding the same mixin to the schema twice - LPP-8234
                     if (schema.getClassModelUnresolved(interstitialName) == null) {
                         // We duplicate the mixin definition, but turn it into
                         // a class definition, inheriting from the previous
                         // superTagName and implementing the mixin
                         Element interstitial = (Element)mixinModel.definition.clone();
                         interstitial.setName("class");
                         interstitial.setAttribute("name", interstitialName);
                         interstitial.setAttribute("extends", superTagName);
 
                         // TODO: [2008-11-10 ptw] Add "implements"
                         // interstitial.setAttribute("implements", mixinName);
                         // Insert this element into the DOM before us
                         Element parent = (Element)((org.jdom.Parent)definition).getParent();
                         int index = parent.indexOf(definition);
                         parent.addContent(index, interstitial);
 
                         // Add it to the schema
                         schema.addElement(interstitial, interstitialName, env);
                     }
 
                     // Update the superTagName
                     superTagName = interstitialName;
                 }
                 // Now adjust this DOM element to refer to the
                 // interstitial superclass
                 definition.removeAttribute("with");
                 definition.setAttribute("extends", superTagName);
             }
           } else {
             // Instance classes are not published
             assert (! publish);
             assert tagName.equals(definition.getName());
             kind = "instance class";
             // The superclass of an instance class is the tag that
             // creates the instance
             superTagName = tagName;
             // Invalid name, just for debugging
             tagName = "anonymous extends='" + superTagName + "'";
           }
         } else {
           // The root class
           resolved = true;
         }
         this.tagName = tagName;
         this.anonymous = (! publish);
         // NOTE: [2009-01-31 ptw] If the class is in an import, or
         // external to the library you are linking, modelOnly is set to true to prevent class
         // models that were created to compute the schema and
         // inheritance from being emitted.  Classes that are actually
         // in the library or application being compiled will be
         // emitted because the are compiled with the `force` option,
         // which overrides `modelOnly`.  See ClassCompiler.compile
         this.modelOnly = env.getBooleanProperty(CompilationEnvironment._EXTERNAL_LIBRARY);
        assert ("_plainfloatshadow".equals(tagName) ? modelOnly : true) : "Missing external: " + env.getImportedLibraryFiles();
         this.env = env;
 
         this.definition = definition;
         this.schema = schema;
         if ((!anonymous) && (tagName != null)) {
           this.className = LZXTag2JSClass(tagName);
         }
 
         this.sortkey = ((!anonymous) && (tagName != null)) ? tagName : "anonymous";
         if (superTagName != null) {
             this.sortkey = superTagName + "." + this.sortkey;
         }
     }
 
 
     public int compareTo(Object other) throws ClassCastException {
       ClassModel o = (ClassModel)other;
       int order = this.sortkey.startsWith(o.sortkey) ? +1 : this.sortkey.compareTo(o.sortkey);
       return order;
     }
 
     /**
      * Check that the 'allocation' attribute of a tag is either "instance" or "class".
      * The return value defaults to "instance".
      * @param element a method or attribute element
      * @return an AttributeSpec allocation type (either 'instance' or 'class')
      */
     String getAllocation(Element element) {
         // allocation type defaults to 'instance'
         String allocation = element.getAttributeValue("allocation");
         if (allocation == null) {
             allocation = NodeModel.ALLOCATION_INSTANCE;
         } else if (!(allocation.equals(NodeModel.ALLOCATION_INSTANCE) ||
                      allocation.equals(NodeModel.ALLOCATION_CLASS))) {
           throw new CompilationError(
               "the value of the 'allocation' attribute must be either 'instance', or 'class'" , element);
         }
         return allocation;
 
     }
 
     public ClassModel resolve() {
         if (resolved) { return this; }
 
         // Find superclass and mixins
         if (superTagName != null) {
             superModel = schema.getClassModel(superTagName);
             if (superModel == null) {
                 throw new CompilationError(
                     /* (non-Javadoc)
                      * @i18n.test
                      * @org-mes="undefined superclass " + p[0] + " for class " + p[1]
                      */
                     org.openlaszlo.i18n.LaszloMessages.getMessage(
                         ViewSchema.class.getName(),"051018-417", new Object[] {superTagName, tagName})
                                            );
             }
             isInputText = superModel.isInputText;
             hasInputText = superModel.hasInputText;
             supportsTextAttribute = superModel.supportsTextAttribute;
             // merge in superclass requiredAttributes list to make scanning the set more efficient
             requiredAttributes.addAll(superModel.requiredAttributes);
         }
 
         // Process the definition if it is to be published (Note that
         // the root class does not have a definition).
         if ((! anonymous) && (definition != null)) {
           // Loop over containsElements tags, adding to containment table in classmodel
           for (Iterator iterator = definition.getChildren().iterator(); iterator.hasNext(); ) {
             Element child = (Element) iterator.next();
             if (child.getName().equals("containsElements")) {
               // look for <element>tagname</element>
               Iterator iter1 = child.getChildren().iterator();
               while (iter1.hasNext()) {
                 Element etag = (Element) iter1.next();
                 if (etag.getName().equals("element")) {
                   String tagname = etag.getText();
                   addContainsElement(tagname);
                 } else {
                   throw new CompilationError(
                     "containsElement block must only contain <element> tags", etag);
                 }
               }
             } else if (child.getName().equals("forbiddenElements")) {
               // look for <element>tagname</element>
               Iterator iter1 = child.getChildren().iterator();
               while (iter1.hasNext()) {
                 Element etag = (Element) iter1.next();
                 if (etag.getName().equals("element")) {
                   String tagname = etag.getText();
                   addForbiddenElement(tagname);
                 } else {
                   throw new CompilationError(
                     "containsElement block must only contain <element> tags", etag);
                 }
               }
             }
           }
 
           // Collect up the attribute defs, if any, of this class
           List attributeDefs = new ArrayList();
           for (Iterator iterator = definition.getContent().iterator(); iterator.hasNext(); ) {
             Object o = iterator.next();
             if (o instanceof Element) {
               Element child = (Element) o;
               if (child.getName().equals("method")) {
                 String attrName = child.getAttributeValue("name");
                 String attrEvent = child.getAttributeValue("event");
                 if (attrEvent == null) {
                   if (schema.enforceValidIdentifier) {
                     try {
                       attrName = ElementCompiler.requireIdentifierAttributeValue(child, "name");
                     } catch (MissingAttributeException e) {
                       throw new CompilationError(
                         "'name' is a required attribute of <" + child.getName() + "> and must be a valid identifier", child);
                     }
                   }
                   String allocation = getAllocation(child);
                   ViewSchema.Type attrType = ViewSchema.METHOD_TYPE;
                   AttributeSpec attrSpec = 
                     new AttributeSpec(attrName, attrType, null, null, child);
                   attrSpec.isfinal = child.getAttributeValue("final");
                   attrSpec.allocation = allocation;
                   attributeDefs.add(attrSpec);
                 }
               } else if (child.getName().equals("setter")) {
                 String attrName = child.getAttributeValue("name");
                 if (schema.enforceValidIdentifier) {
                   try {
                     attrName = ElementCompiler.requireIdentifierAttributeValue(child, "name");
                   } catch (MissingAttributeException e) {
                     throw new CompilationError(
                       "'name' is a required attribute of <" + child.getName() + "> and must be a valid identifier", child);
                   }
                 }
                 // Setter is shorthand for a specially-named method
                 attrName = "$lzc$set_" + attrName;
                 String allocation = getAllocation(child);
                 ViewSchema.Type attrType = ViewSchema.METHOD_TYPE;
                 AttributeSpec attrSpec =
                   new AttributeSpec(attrName, attrType, null, null, child);
                 attrSpec.allocation = allocation;
                 attributeDefs.add(attrSpec);
               } else if (child.getName().equals("attribute")) {
                 // Is this an element named ATTRIBUTE which is a
                 // direct child of this CLASS or INTERFACE tag?
 
                 String attrName = child.getAttributeValue("name");
                 if (schema.enforceValidIdentifier) {
                   try {
                     attrName = ElementCompiler.requireIdentifierAttributeValue(child, "name");
                   } catch (MissingAttributeException e) {
                     throw new CompilationError(
                       /* (non-Javadoc)
                        * @i18n.test
                        * @org-mes="'name' is a required attribute of <" + p[0] + "> and must be a valid identifier"
                        */
                       org.openlaszlo.i18n.LaszloMessages.getMessage(
                         ClassCompiler.class.getName(),"051018-131", new Object[] {child.getName()})
                       , child);
                   }
                 }
 
                 String attrTypeName = child.getAttributeValue("type");
                 String attrDefault = child.getAttributeValue("value");
                 String attrSetter = child.getAttributeValue("setter");
                 String attrRequired = child.getAttributeValue("required");
                 String allocation = getAllocation(child);
 
                 if (attrDefault != null && attrRequired != null &&
                     attrRequired.equals("true") && !attrDefault.equals("null")) {
                   env.warn("An attribute cannot both be declared required and also have a non-null default value", child);
                 }
 
                 ViewSchema.Type attrType;
                 if (attrTypeName == null) {
                   // Check if this attribute exists in ancestor classes,
                   // and if so, default to that type.
                   attrType = superModel.getAttributeType(attrName, allocation);
                   if (attrType == null) {
                     // The default attribute type
                     attrType = ViewSchema.EXPRESSION_TYPE;
                   }
                 } else {
                   attrType = schema.getTypeForName(attrTypeName);
                 }
 
                 if (attrType == null) {
                   throw new CompilationError(
                     /* (non-Javadoc)
                      * @i18n.test
                      * @org-mes="In class " + p[0] + " type '" + p[1] + "', declared for attribute '" + p[2] + "' is not a known data type."
                      */
                     org.openlaszlo.i18n.LaszloMessages.getMessage(
                       ClassCompiler.class.getName(),"051018-160", new Object[] {tagName, attrTypeName, attrName})
                     , definition);
                 }
 
                 AttributeSpec attrSpec =
                   new AttributeSpec(attrName, attrType, attrDefault,
                                     attrSetter, "true".equals(attrRequired), child);
                 attrSpec.allocation = allocation;
                 attrSpec.isfinal = child.getAttributeValue("final");
                 if (attrName.equals("text") && attrTypeName != null) {
                   if ("text".equals(attrTypeName))
                     attrSpec.contentType = attrSpec.TEXT_CONTENT;
                   else if ("html".equals(attrTypeName))
                     attrSpec.contentType = attrSpec.HTML_CONTENT;
                 }
                 attributeDefs.add(attrSpec);
               } else if (child.getName().equals("event")) {
                 String attrName = child.getAttributeValue("name");
                 if (schema.enforceValidIdentifier) {
                   try {
                     attrName = ElementCompiler.requireIdentifierAttributeValue(child, "name");
                   } catch (MissingAttributeException e) {
                     throw new CompilationError(
                       "'name' is a required attribute of <" + child.getName() + "> and must be a valid identifier", child);
                   }
                 }
 
                 ViewSchema.Type attrType = ViewSchema.EVENT_HANDLER_TYPE;
                 AttributeSpec attrSpec =
                   new AttributeSpec(attrName, attrType, null, null, child);
                 attributeDefs.add(attrSpec);
               } else if (child.getName().equals("doc")) {
                 // Ignore documentation nodes
               } else {
                 // We'd like to warn about unknown attributes, but
                 // we can't tell if a child is a view here...
               }
             }
           }
           // Add in the attribute declarations.
           schema.addAttributeDefs(definition, tagName, attributeDefs, env);
         }
         resolved = true;
         return this;
     }
 
   public String toLZX() {
     return toLZX("");
   }
 
   public String toLZX(String indent) {
     String lzx = indent + "<interface name='" + tagName + "'" +
       ((superModel != null)?(" extends='" + superModel.tagName +"'"):"") + ">";
     for (Iterator i = attributeSpecs.values().iterator(); i.hasNext(); ) {
       AttributeSpec spec = (AttributeSpec)i.next();
       String specLZX = spec.toLZX(indent + "  ", superModel);
       if (specLZX != null) {
         lzx += "\n";
         lzx += specLZX;
       }
     }
 
     lzx += "\n" + indent + "</interface>";
     return lzx;
   }
 
   // Map of LFC tag names
   static HashMap LFCTag2JSClass = new HashMap();
   static {
     LFCTag2JSClass.put("node", "LzNode");
     LFCTag2JSClass.put("view", "LzView");
     LFCTag2JSClass.put("text", "LzText");
     LFCTag2JSClass.put("inputtext", "LzInputText");
     LFCTag2JSClass.put("canvas", "LzCanvas");
     LFCTag2JSClass.put("script", "LzScript");
     LFCTag2JSClass.put("animatorgroup", "LzAnimatorGroup");
     LFCTag2JSClass.put("animator", "LzAnimator");
     LFCTag2JSClass.put("layout", "LzLayout");
     LFCTag2JSClass.put("state", "LzState");
     LFCTag2JSClass.put("command", "LzCommand");
     LFCTag2JSClass.put("selectionmanager", "LzSelectionManager");
     LFCTag2JSClass.put("dataselectionmanager", "LzDataSelectionManager");
     LFCTag2JSClass.put("datapointer", "LzDatapointer");
     LFCTag2JSClass.put("dataprovider", "LzDataProvider");
     LFCTag2JSClass.put("datapath", "LzDatapath");
     LFCTag2JSClass.put("dataset", "LzDataset");
     LFCTag2JSClass.put("datasource", "LzDatasource");
     LFCTag2JSClass.put("lzhttpdataprovider", "LzHTTPDataProvider");
     LFCTag2JSClass.put("import", "LzLibrary");
     LFCTag2JSClass.put("contextmenu", "LzContextMenu");
     LFCTag2JSClass.put("contextmenuitem", "LzContextMenuItem");
   }
 
   public static String LZXTag2JSClass(String s) {
     if (LFCTag2JSClass.containsKey(s)) {
       return (String)(LFCTag2JSClass.get(s));
     }
     String lzcPackagePrefix = "$lzc$class_";
     return lzcPackagePrefix + s;
   }
 
 
   protected boolean declarationEmitted = false;
   /**
    * Emits a class model as a JS2 class declaration.  This is used
    * both by the class compiler and the instance compiler (when an
    * instance has methods, either explicit or implicit).
    */
   void emitClassDeclaration(CompilationEnvironment env) {
     // Last chance for resolution
     resolve();
     declarationEmitted = true;
     // Should the package prefix be in the model?  Should the
     // model store class and tagname separately?
     assert superModel != null : "Unknown superclass " + superTagName + " for " + kind + " " + tagName;
     // Allow forward references.
     if (! superModel.isCompiled()) {
       superModel.compile(env);
     }
     String superClassName = superModel.className;
     if (className == null) {
       className = LZXTag2JSClass(CompilerUtils.encodeJavaScriptIdentifier(nodeModel.getNodePath()));
     }
     // className will be a global
     env.addId(className, definition);
 
     // TODO: [2008-06-02 ptw] This should be moved to the JS2 back-end
     // Build the constructor trampoline
     if (env.isAS3()) {
       String body = "";
       body += "super($lzc$parent, $lzc$attrs, $lzc$children, $lzc$async);\n";
       nodeModel.setAttribute(
         className,
         new Method(
           className,
           // All nodes get these args when constructed
           // Apparently AS3 does not allow defaulting of
           // primitive args
           "$lzc$parent:LzNode? = null, $lzc$attrs:Object? = null, $lzc$children:Array? = null, $lzc$async:Boolean = false",
           body));
     }
 
     // Build the class body
     String classBody = "";
     if (nodeModel.passthroughBlock != null) {
       classBody += "#passthrough (toplevel:true) {\n" +
                    nodeModel.passthroughBlock +
                    "\n}#\n";
     }
 
     if (nodeModel.debug && (tagName != null)) {
       // Set the tag name
       nodeModel.setClassAttribute("tagname",  ScriptCompiler.quote(tagName));
     }
 
     // TODO: [2008-06-02 ptw] This should only be done for LZX classes that are
     // subclasses of LzNode
     //
     // Before you output this, see if it is necessary:  will this node
     // end up with children at all?
     boolean hasChildren = (! nodeModel.getChildren().isEmpty());
     boolean inheritsChildren = inheritsChildren();
     if (hasChildren || inheritsChildren) {
       String children = ScriptCompiler.objectAsJavascript(nodeModel.childrenMaps());
       if (inheritsChildren) {
         // NOTE: [2009-04-01 ptw] We don't compute the merged children
         // at compile time, because we may not know them (superclass
         // may be loaded from a library).  It might be possible to
         // optimize this when we know the superclass.
         nodeModel.setClassAttribute("children", "LzNode.mergeChildren(" + children + ", " + superClassName + "['children'])");
       } else {
         nodeModel.setClassAttribute("children", children);
       }
     }
 
     // Declare all instance vars and methods, save initialization
     // in <class>.attributes
     Map attrs = nodeModel.getAttrs();
     Map decls = new LinkedHashMap();
     Map inits = new LinkedHashMap();
     boolean isstate = isSubclassOf(schema.getClassModel("state"));
     for (Iterator i = attrs.entrySet().iterator(); i.hasNext(); ) {
       Map.Entry entry = (Map.Entry) i.next();
       String key = (String) entry.getKey();
       Object value = entry.getValue();
       boolean redeclared = (superModel.getAttribute(key, NodeModel.ALLOCATION_INSTANCE) != null);
       if ((value instanceof NodeModel.BindingExpr)) {
         // Bindings always have to be installed as an init
         if (! redeclared) {
           decls.put(key, null);
         }
         inits.put(key, ((NodeModel.BindingExpr)value).getExpr());
       } else if (value instanceof Method &&
                  ((! isstate) ||
                   className.equals(key))) {
         // Methods are just decls.  Except in states, because they
         // have to be applied to the parent, except for the
         // constructor!
         decls.put(key, value);
       } else if (value != null) {
         // If there is a setter for this attribute, or this is a
         // state, or this is an Array or Map argument that needs
         // magic merging, the value has to be installed as an init,
         // otherwise it should be installed as a decl
         //
         // TODO: [2008-03-15 ptw] This won't work until we know (in
         // the classModel) the setters for all the superclasses
         // (built-in and in libraries), so we install as an init for
         // now and this is fixed up in LzNode by installing inits that
         // have no setters when the arguments are merged
         if (true) { // (! (value instanceof String))  || setters.containsKey(key) || isstate) {
           // If this is a re-declared attribute, we just init it,
           // don't re-declare it
           if (! redeclared) {
             decls.put(key, null);
           }
           inits.put(key, value);
         } else {
           if (! redeclared) {
             decls.put(key, value);
             // If there is a property that would have been shadowed,
             // you have to hide that from applyArgs, or you will get
             // clobbered!
             inits.put(key, "LzNode._ignoreAttribute");
           } else {
             inits.put(key, value);
           }
         }
       } else {
         // Just a declaration
         if (! redeclared) {
           decls.put(key, value);
         }
       }
     }
     // Create inits list, merged with superclass inits
     nodeModel.setClassAttribute("attributes", "new LzInheritedHash(" + superClassName + ".attributes)");
     // NOTE: [2008-06-02 ptw] As an optimization, we don't do this if
     // this class has no inits of its own.  (Unlike LFC classes, an LZX
     // class should not be manipulating its attributes directly, so we
     // ought not to have to make the copy above, but that mysteriously
     // breaks things.)
     if (! inits.isEmpty()) {
       classBody += "LzNode.mergeAttributes(" +
         ScriptCompiler.objectAsJavascript(inits) +
         ", " + env.getGlobalPrefix() + className + ".attributes);\n";
     }
     // Emit the class decl
     ScriptClass scriptClass =
       new ScriptClass(className,
                       superClassName,
                       decls,
                       nodeModel.getClassAttrs(),
                       classBody);
     env.compileScript(scriptClass.toString(), definition);
     if ((! anonymous) && (tagName != null)) {
       env.addTag(tagName, className);
     }
   }
 
   /**
    * Output a class.  Called after schema processing, but may be
    * compiled out of order, so that forward references to classes work
    */
   public void compile(CompilationEnvironment env) {
     this.compile(env, false);
   }
 
   /**
    * This may be called to emit a class defintion, or to resolve
    * either a forward or external (to this compilation unit)
    * reference.  In all cases, we create the node model, which is used
    * for calculating other compile-time optimizations.  For external
    * references, we do not generate any code.
    */
   public void compile(CompilationEnvironment env, boolean force) {
     if (! hasNodeModel()) {
       // We compile a class declaration just like a view, and then
       // add attribute declarations and perhaps some other stuff that
       // the runtime wants.
       NodeModel model = NodeModel.elementAsModel(definition, schema, env);
       model = model.expandClassDefinitions();
       // Establish class root
       model.assignClassRoot(0);
       setNodeModel(model);
     }
     assert (force ? (! modelOnly) : true) : "Forcing compile of model-only class " + tagName;
     if (force || ((! isCompiled()) && (! modelOnly))) {
       emitClassDeclaration(env);
     }
   }
 
     /** Returns true if this is equal to or a subclass of
      * superclass. */
     boolean isSubclassOf(ClassModel superclass) {
         if (this == superclass) return true;
         if (this.superModel == null) return false;
         return this.superModel.isSubclassOf(superclass);
     }
     
   void setIsBuiltin(boolean value) {
     builtin = value;
   }
 
     boolean isBuiltin() {
       return builtin;
     }
     
     boolean hasNodeModel() {
         // Classes that have generated code will have a nodeModel
         return nodeModel != null;
     }
 
   boolean isCompiled() {
     // Classes that are builtin or have been compiled
     // Or and interface:  for now, we generate nothing for an LZX
     // interface
     return declarationEmitted || isBuiltin() || "interface".equals(kind);
   }
 
   public ClassModel getSuperclassModel() {
       return superModel;
   }
 
   // A class needs to merge its children if its superclass has
   // children, or its superclass is in a library (in which case, we
   // just don't know)
   boolean inheritsChildren() {
     // No LFC class has children
     if (superModel.builtin) { return false; }
     // If we don't know, we have to assume true
     if (superModel.nodeModel == null) { return true; }
     // Otherwise ask them
     return ((! superModel.nodeModel.getChildren().isEmpty()) ||
             superModel.inheritsChildren());
   }
 
   private Map mergedMethods;
 
   // NOTE: [2009-03-31 ptw] This information is incomplete.  It only
   // knows the methods the tag compiler has added.  It does _not_ know
   // methods that may have come from a library (only the schema knows
   // those, assuming it has parsed the interface).
   Map getMergedMethods() {
     if (mergedMethods != null) { return mergedMethods; }
     if (nodeModel == null) { return mergedMethods = new LinkedHashMap(); }
     Map merged = mergedMethods = new LinkedHashMap(superModel.getMergedMethods());
     // Merge in the our methods
     for (Iterator i = nodeModel.getAttrs().entrySet().iterator(); i.hasNext(); ) {
       Map.Entry entry = (Map.Entry) i.next();
       String key = (String) entry.getKey();
       Object value = entry.getValue();
       if (value instanceof Method) {
         merged.put(key, value);
       }
     }
     return merged;
   }
 
     /** This is really the LZX tag name */
     public String getClassName () {
      return this.tagName;
     }
     
     /** This is really the LZX tag name */
     public String getSuperTagName() {
         if (superTagName != null) {
             return superTagName; 
         } else if (superModel == null) {
             return null;
         }  else {
             return superModel.tagName;
         }
     }
     
     public void setSuperTagName(String name) {
         this.superTagName = name;
     }
     
     void setSuperclassModel(ClassModel superclass) {
         this.superModel = superclass;
     }
     
     /** Return the AttributeSpec for the attribute named attrName.
         Only returns locally defined attribute, does not follow up the
         class hierarchy.
     */
     AttributeSpec getLocalAttribute(String attrName, String allocation) {
       if (allocation.equals(NodeModel.ALLOCATION_INSTANCE)) {
         return (AttributeSpec) attributeSpecs.get(attrName);
       } else {
         return (AttributeSpec) classAttributeSpecs.get(attrName);
       }
     }
 
 
     /** Return the AttributeSpec for the attribute named attrName.  If
      * the attribute is not defined on this class, look up the
      * superclass chain.
      */
   AttributeSpec getAttribute(String attrName, String allocation) {
         Map attrtable = allocation.equals(NodeModel.ALLOCATION_INSTANCE) ? attributeSpecs : classAttributeSpecs;
         AttributeSpec attr = (AttributeSpec) attrtable.get(attrName);
         if (attr != null) {
             return attr;
         } else if (superModel != null) {
           return(superModel.getAttribute(attrName, allocation));
         } else {
             return null;
         }
     }
 
     /** Find an attribute name which is similar to attrName, or return
      * null.  Used in compiler warnings. */
     AttributeSpec findSimilarAttribute(String attrName) {
         for (Iterator iter = attributeSpecs.values().iterator(); iter.hasNext();) {
             AttributeSpec attr = (AttributeSpec) iter.next();
             if ((attrName.toLowerCase().equals(attr.name.toLowerCase())) ||
                 (attrName.toLowerCase().startsWith(attr.name.toLowerCase())) ||
                 (attrName.toLowerCase().endsWith(attr.name.toLowerCase())) ||
                 (attr.name.toLowerCase().startsWith(attrName.toLowerCase())) ||
                 (attr.name.toLowerCase().endsWith(attrName.toLowerCase()))) {
                 return attr;
             }
         }
         // if that didn't work, try the supeclass
         if (superModel == null) {
             return null;
         } else {
             return superModel.findSimilarAttribute(attrName);
         }
     }
 
   ViewSchema.Type getAttributeTypeOrException(String attrName, String allocation)
         throws UnknownAttributeException
     {
       AttributeSpec attr = getAttribute(attrName, allocation);
         if (attr != null) {
             return attr.type;
         }  
         // If there is no superclass attribute, use the default static
         // attribute map
         ViewSchema.Type type = ViewSchema.getAttributeType(attrName);
         // Last resort, use default of 'expression' type
         if (type == null) {
             throw new UnknownAttributeException();
         }
         return type;
     }
     
     ViewSchema.Type getAttributeType(String attrName, String allocation) {
         AttributeSpec attr = getAttribute(attrName, allocation);
         if (attr != null) {
             return attr.type;
         }  
         // If there is no superclass attribute, use the default static
         // attribute map
         ViewSchema.Type type = ViewSchema.getAttributeType(attrName);
         // Last resort, use default of 'expression' type
         if (type == null) {
             type = ViewSchema.EXPRESSION_TYPE;
         }
         return type;
     }
     
     void setNodeModel(NodeModel model) {
         this.nodeModel = model;
     }
 
     boolean getInline() {
         return inline && nodeModel != null;
     }
     
     void setInline(boolean inline) {
         this.inline = inline;
     }
     
     public static class InlineClassError extends CompilationError {
         public InlineClassError(ClassModel cm, NodeModel im, String message) {
             super(
                 "The class " + cm.tagName + " has been declared " +
                 "inline-only but cannot be inlined.  " + message + ". " +
                 "Remove " + cm.tagName + " from the <?lzc class=\"" +
                 cm.tagName + "\"> or " + "<?lzc classes=\"" + cm.tagName
                 + "\"> processing instruction to remove this error.",
                 im.element);
         }
     }
     
     protected boolean descendantDefinesAttribute(NodeModel model, String name) {
         for (Iterator iter = model.getChildren().iterator(); iter.hasNext(); ) {
             NodeModel child = (NodeModel) iter.next();
             if (child.hasAttribute(name) || descendantDefinesAttribute(child, name))
                 return true;
         }
         return false;
     }
         
     public Collection getLocalAttributes () {
         return Collections.unmodifiableCollection(attributeSpecs.values());
     }
     
     NodeModel applyClass(NodeModel instance) {
         final String DEFAULTPLACEMENT_ATTR_NAME = "defaultPlacement";
         final String PLACEMENT_ATTR_NAME = "placement";
         if (nodeModel == null) throw new RuntimeException("no nodeModel for " + tagName);
         if (nodeModel.hasAttribute(DEFAULTPLACEMENT_ATTR_NAME))
             throw new InlineClassError(this, instance, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The class has a " + p[0] + " attribute"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ClassModel.class.getName(),"051018-196", new Object[] {DEFAULTPLACEMENT_ATTR_NAME})
 );
         if (instance.hasAttribute(DEFAULTPLACEMENT_ATTR_NAME))
             throw new InlineClassError(this, instance, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The instance has a " + p[0] + " attribute"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ClassModel.class.getName(),"051018-205", new Object[] {DEFAULTPLACEMENT_ATTR_NAME})
 );
         if (descendantDefinesAttribute(instance, PLACEMENT_ATTR_NAME))
             throw new InlineClassError(this, instance, 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="An element within the instance has a " + p[0] + " attribute"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 ClassModel.class.getName(),"051018-214", new Object[] {PLACEMENT_ATTR_NAME})
 );
         
         try {
             // Replace this node by the class model.
             NodeModel model = (NodeModel) nodeModel.clone();
             // Set $classrootdepth on children of the class (but not the
             // instance that it's applied to)
             setChildrenClassRootDepth(model, 1);
             model.updateMembers(instance);
             model.setClassName(getSuperTagName());
             return model;
         } catch (CompilationError e) {
             throw new InlineClassError(this, instance, e.getMessage());
         }
     }
     
     protected void setChildrenClassRootDepth(NodeModel model, int depth) {
         final String CLASSROOTDEPTH_ATTRIBUTE_NAME = "$classrootdepth";
         for (Iterator iter = model.getChildren().iterator(); iter.hasNext(); ) {
             NodeModel child = (NodeModel) iter.next();
             // If it has already been set, this child is the result of
             // a previous inline class expansion with a different
             // classroot.
             if (child.hasAttribute(CLASSROOTDEPTH_ATTRIBUTE_NAME))
                 continue;
             child.setAttribute(CLASSROOTDEPTH_ATTRIBUTE_NAME,
                                new Integer(depth));
             int childDepth = depth;
             ClassModel childModel = child.getClassModel();
             // If this is an undefined class, childModel will be null.
             // This is an error, and other code signals a compiler
             // warning. This test keeps it from resulting in a stack
             // trace too.
             if (childModel != null && childModel.isSubclassOf(schema.getClassModel("state")))
                 childDepth++;
             setChildrenClassRootDepth(child, childDepth);
         }
     }
 
 
       /** Add an entry to the table of legally containable tags for a
      * given tag */
     public void addContainsElement (String childtag) {
       mCanContainTags.add(childtag);
     }
 
     public Set getContainsSet () {
       return mCanContainTags;
     }
 
       /** Add an entry to the table of forbidden tags for a
        * given tag */
     public void addForbiddenElement (String childtag) {
       mForbiddenTags.add(childtag);
     }
 
     public Set getForbiddenSet () {
       return mForbiddenTags;
     }
 
 }
 
 /**
  * @copyright Copyright 2001-2009 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
