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
 
 public class ClassModel implements Comparable {
     protected final ViewSchema schema;
     /** This is really the LZX tag name */
     public final String tagName;
     /** And this is the actual class name */
     public final String className;
     final CompilationEnvironment env;
     protected boolean builtin = false;
     // This is null for the root class
     protected ClassModel superclass;
     
     // This is null for the root class
     protected final Element definition;
     protected String kind;
     protected NodeModel nodeModel;
     
     /** Set of tags that can legally be nested in this element */
     protected Set mCanContainTags = new HashSet();
 
       /** Set of forbidden child tags of this element */
     protected Set mForbiddenTags = new HashSet();
 
     /* If superclass is a predefined system class, just store its name. */
     protected String superTagName = null;
     protected boolean hasInputText = false;
     protected boolean isInputText = false;
         
     public Set traitNames = new HashSet(2, 0.6f);
     
     /* Class or superclass has an <attribute type="text"/>  */
     protected boolean supportsTextAttribute = false;
     /** Map attribute name to type */
     public final Map attributeSpecs = new LinkedHashMap();
     protected final Map classAttributeSpecs = new LinkedHashMap();
 
     public boolean inline = false;
     protected String sortkey = null;
 
     public String toString() {
         return "ClassModel: tagName="+tagName + ", " + 
             "superclass=" + superclass + ", " + 
             "superTagName=" + superTagName + ", " + 
             "hasInputText=" + hasInputText + ", " + 
             "isInputText=" + isInputText + ", " + 
             "definition=" + definition;
     }
 
     // Construct a user-defined class
     public ClassModel(String tagName, ClassModel superclass,
                       ViewSchema schema, Element definition, CompilationEnvironment env) {
         this.tagName = tagName;
         this.env = env;
         if (tagName != null) {
           this.className = LZXTag2JSClass(tagName);
         } else {
           this.className = LZXTag2JSClass(env.methodNameGenerator.next());
         }
         this.superclass = superclass;
         this.definition = definition;
         if (definition != null) {
           // class, interface, mixin
           this.kind = definition.getName();
         }
         this.schema = schema;
         this.sortkey = tagName != null ? tagName : className;
         if (superclass != null) {
           this.sortkey = superclass.sortkey + "." + this.sortkey;
         }
     }
 
   // Construct a builtin class
   public ClassModel(String tagName, ViewSchema schema, CompilationEnvironment env) {
     this(tagName, null, schema, null, env);
   }
 
   public int compareTo(Object other) throws ClassCastException {
     ClassModel o = (ClassModel)other;
     int order = this.sortkey.startsWith(o.sortkey) ? +1 : this.sortkey.compareTo(o.sortkey);
     return order;
   }
 
   public String toLZX() {
     return toLZX("");
   }
 
   public String toLZX(String indent) {
     String lzx = indent + "<interface name='" + tagName + "'" +
       ((superclass != null)?(" extends='" + superclass.tagName +"'"):"") + ">";
     for (Iterator i = attributeSpecs.values().iterator(); i.hasNext(); ) {
       AttributeSpec spec = (AttributeSpec)i.next();
       String specLZX = spec.toLZX(indent + "  ", superclass);
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
   }
 
   public static String LZXTag2JSClass(String s) {
     if (LFCTag2JSClass.containsKey(s)) {
       return (String)(LFCTag2JSClass.get(s));
     }
     String lzcPackagePrefix = "$lzc$class_";
     return lzcPackagePrefix + s;
   }
 
 
   /**
    * Emits a class model as a JS2 class declaration.  This is used
    * both by the class compiler and the instance compiler (when an
    * instance has methods, either explicit or implicit).
    */
   void emitClassDeclaration(CompilationEnvironment env) {
     // className will be a global
     env.addId(className, definition);
     // Should the package prefix be in the model?  Should the
     // model store class and tagname separately?
     ClassModel superclassModel = getSuperclassModel();
     // Allow forward references.
     if (! superclassModel.isCompiled()) {
       superclassModel.compile(env);
     }
     String superClassName = superclassModel.className;
 
     // TODO: [2008-06-02 ptw] This should be moved to the JS2 back-end
     // Build the constructor trampoline
     if ("swf9".equals(env.getRuntime())) {
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
     if (tagName != null) {
       // Set the tag name
       nodeModel.setClassAttribute("tagname",  ScriptCompiler.quote(tagName));
     }
 
     // TODO: [2008-06-02 ptw] This should only be done for LZX classes that are
     // subclasses of LzNode
     //
     // Before you output this, see if it is necessary:  will this node
     // end up with children at all?
     if (getMergedChildren().size() > 0) {
       // TODO: [2008-05-30 ptw] We don't output the merged children,
       // on the belief that is is more efficient to build the merged
       // list at runtime, but this should be measured.
       String children = ScriptCompiler.objectAsJavascript(nodeModel.childrenMaps());
       // class#classChildren now class.children
       nodeModel.setClassAttribute("children", "LzNode.mergeChildren(" + children + ", " + superClassName + "['children'])");
     }
 
     // Declare all instance vars and methods, save initialization
     // in <class>.attributes
     Map attrs = nodeModel.getAttrs(); // classModel.getMergedAttributes();
     Map setters = getMergedSetters();
     Map decls = new LinkedHashMap();
     Map inits = new LinkedHashMap();
     boolean isstate = isSubclassOf(schema.getClassModel("state"));
     for (Iterator i = attrs.entrySet().iterator(); i.hasNext(); ) {
       Map.Entry entry = (Map.Entry) i.next();
       String key = (String) entry.getKey();
       Object value = entry.getValue();
       boolean redeclared = (superclassModel.getAttribute(key, NodeModel.ALLOCATION_INSTANCE) != null);
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
         // If this is a re-declared attribute, we just init it,
         // don't re-declare it
         if (superclassModel.getAttribute(key, NodeModel.ALLOCATION_INSTANCE) != null) {
           inits.put(key, value);
         }
         // If there is a setter for this attribute, or this is a
         // state, or this is an Array or Map argument that needs
         // magic merging, the value has to be installed as an init,
         // otherwise it should be installed as a decl
         //
         // TODO: [2008-03-15 ptw] This won't work until we know
         // (in the classModel) the setters for the built-in
         // classes, so we install as an init for now and this is
         // fixed up in LzNode by installing inits that have no
         // setters when the arguments are merged
         if (true) { // (! (value instanceof String))  || setters.containsKey(key) || isstate) {
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
     //
     // NOTE: [2008-06-02 ptw] As an optimization, we don't do this if
     // this class has no inits of its own.  Unlike LFC classes, an LZX
     // class should not be manipulating its attributes directly, so
     // this should be a safe optimization.
     // NOTE: [2008-06-06 ptw] This optimization does not work.  Why?
 //     if (! inits.isEmpty()) {
       nodeModel.setClassAttribute("attributes", "new LzInheritedHash(" + superClassName + ".attributes)");
       classBody += "LzNode.mergeAttributes(" +
         ScriptCompiler.objectAsJavascript(inits) +
         ", "+env.getGlobalPrefix() + className + ".attributes);\n";
 //     }
     // Emit the class decl
     ScriptClass scriptClass =
       new ScriptClass(className,
                       superClassName,
                       decls,
                       nodeModel.getClassAttrs(),
                       classBody);
     env.compileScript(scriptClass.toString(), definition);
     if (tagName != null) {
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
 
   public void compile(CompilationEnvironment env, boolean force) {
     if (force ||
         ((! isCompiled()) &&
          (! "false".equals(env.getProperty(env.LINK_PROPERTY))))) {
       // We compile a class declaration just like a view, and then
       // add attribute declarations and perhaps some other stuff that
       // the runtime wants.
       ViewCompiler.preprocess(definition, env);
       NodeModel model = NodeModel.elementAsModel(definition, schema, env);
       model = model.expandClassDefinitions();
       // Establish class root
       model.assignClassRoot(0);
       setNodeModel(model);
       emitClassDeclaration(env);
     }
   }
 
     /** Returns true if this is equal to or a subclass of
      * superclass. */
     boolean isSubclassOf(ClassModel superclass) {
         if (this == superclass) return true;
         if (this.superclass == null) return false;
         return this.superclass.isSubclassOf(superclass);
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
     return isBuiltin() || hasNodeModel() || "interface".equals(kind);
   }
 
   public ClassModel getSuperclassModel() {
       return superclass;
   }
 
   private List mergedChildren;
 
   List getMergedChildren() {
     if (mergedChildren != null) { return mergedChildren; }
     if (nodeModel == null) { return mergedChildren = new Vector(); }
     List merged = mergedChildren = new Vector(superclass.getMergedChildren());
     merged.addAll(nodeModel.getChildren());
     return merged;
   }
 
   private Map mergedAttributes;
 
   Map getMergedAttributes() {
     if (mergedAttributes != null) { return mergedAttributes; }
     if (nodeModel == null) { return mergedAttributes = new LinkedHashMap(); }
     Map merged = mergedAttributes = new LinkedHashMap(superclass.getMergedAttributes());
     // Merge in the our attributes, omitting methods
     for (Iterator i = nodeModel.getAttrs().entrySet().iterator(); i.hasNext(); ) {
       Map.Entry entry = (Map.Entry) i.next();
       String key = (String) entry.getKey();
       Object value = entry.getValue();
       if ("LzNode._ignoreAttribute".equals(value)) {
         merged.remove(key);
       } else if (! (value instanceof Method)) {
         merged.put(key, value);
       }
     }
     return merged;
   }
 
   private Map mergedMethods;
 
   Map getMergedMethods() {
     if (mergedMethods != null) { return mergedMethods; }
     if (nodeModel == null) { return mergedMethods = new LinkedHashMap(); }
     Map merged = mergedMethods = new LinkedHashMap(superclass.getMergedMethods());
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
 
   private Map mergedSetters;
 
   Map getMergedSetters() {
     if (mergedSetters != null) { return mergedSetters; }
     if (nodeModel == null) { return mergedSetters = new LinkedHashMap(); }
     Map merged = mergedSetters = new LinkedHashMap(superclass.getMergedSetters());
     // Merge in the our setters
     for (Iterator i = nodeModel.getSetters().entrySet().iterator(); i.hasNext(); ) {
       Map.Entry entry = (Map.Entry) i.next();
       String key = (String) entry.getKey();
       Object value = entry.getValue();
       merged.put(key, value);
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
         } else if (superclass == null) {
             return null;
         }  else {
             return superclass.tagName;
         }
     }
     
     public void setSuperTagName(String name) {
         this.superTagName = name;
     }
     
     void setSuperclassModel(ClassModel superclass) {
         this.superclass = superclass;
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
         } else if (superclass != null) {
           return(superclass.getAttribute(attrName, allocation));
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
         if (superclass == null) {
             return null;
         } else {
             return superclass.findSimilarAttribute(attrName);
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
  * @copyright Copyright 2001-2008 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
