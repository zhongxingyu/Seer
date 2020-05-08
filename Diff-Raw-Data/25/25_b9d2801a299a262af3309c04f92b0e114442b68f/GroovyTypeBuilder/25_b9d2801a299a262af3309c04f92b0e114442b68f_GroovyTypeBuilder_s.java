 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.scripting.groovy;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import groovy.util.BuilderSupport;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A {@link com.flexive.shared.structure.FxType FxType} groovy builder. By convention,
  * group names start with an uppercase letter, and properties with a lowercase letter.
  * An example, taken from the test cases:
  * <p/>
  * <pre>
  * // create the type "builderTest"
  * new GroovyTypeBuilder().builderTest {
  * // assign the caption property
  * myCaption(assignment: "ROOT/CAPTION")
  * // add some new properties
  * stringPropertyDefault()
  * numberProperty(FxDataType.Number)
  * descriptionProperty(description: new FxString("string property description"))
  * multilineProperty(multiline: true)
  * multilangProperty(multilang: true)
  * uniqueProperty(uniqueMode: UniqueMode.Global)
  * referenceProperty(FxDataType.Reference, referencedType: CacheAdmin.environment.getType("DOCUMENT"))
  * listProperty(FxDataType.SelectMany, referencedList: CacheAdmin.environment.getSelectLists().get(0))
  * <p/>
  * // create a new group
  * MultiGroup(description: new FxString("my group"), multiplicity: FxMultiplicity.MULT_0_N) {
  * // assign a property inside the group
  * nestedCaption(assignment: "ROOT/CAPTION")
  * // create a new property
  * groupNumberProperty(FxDataType.Number)
  * <p/>
  * // nest another group
  * NestedGroup(multiplicity: FxMultiplicity.MULT_1_N) {
  * nestedProperty()
  * }
  * }
  * }
  * </pre>
  * <p>
  * Type node arguments:
  * <table>
  * <tr>
  * <th>description</th>
  * <td>FxString</td>
  * <td>The type description</td>
  * </tr>
  * <tr>
  * <th>acl</th>
  * <td>ACL</td>
  * <td>The type ACL to be used</td>
  * </tr>
  * <tr>
  * <th>useInstancePermissions,<br/>
  * usePropertyPermissions,<br/>
  * useStepPermissions,<br/>
  * useTypePermissions</th>
  * <td>boolean</td>
  * <td>Enable or disable the given type permissions.</td>
  * </tr>
  * <tr>
  * <th>disablePermissions</th>
  * <td>boolean</td>
  * <td>Disable all permissions checks for the type (and contents of this type)</td>
  * </tr>
  * </table>
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class GroovyTypeBuilder extends BuilderSupport {
     private static final Map<Object, Object> EMPTYATTRIBUTES = Collections.unmodifiableMap(new HashMap<Object, Object>());
 
     /**
      * List of key that are not used for options and are "real" parameters
      */
     private final static String[] NONOPTION_KEYS = {
             "ACL",
             "NAME",
             "HINT",
             "ALIAS",
             "LABEL",
             "DESCRIPTION",
             "DATATYPE",
             "MULTIPLICITY",
             "DEFAULTMULTIPLICITY"
 
     };
 
     /**
      * Check if the given key is a non-option key (=not used for property or group options)
      *
      * @param key the key to check
      * @return if its a non-option key
      */
     private static boolean isNonOptionKey(String key) {
         String uKey = key.toUpperCase();
         for (String check : NONOPTION_KEYS)
             if (check.equals(uKey))
                 return true;
         return false;
     }
 
     private FxType type;
 
     private class Node<TElement> {
         protected GroupNode parent;
         protected final TElement element;
 
         public Node() {
             this(null);
         }
 
         public Node(TElement element) {
             this.element = element;
         }
 
         public String getXPath() {
             return "";
         }
 
         public String getName() {
             return "ROOT";
         }
 
         public Node getParent() {
             return parent;
         }
 
         public void setParent(GroupNode parent) {
             this.parent = parent;
         }
 
         public TElement getElement() {
             return element;
         }
     }
 
     private class StructureNode<T extends FxStructureElement> extends Node<T> {
 
         public StructureNode(T element) {
             super(element);
             FxSharedUtils.checkParameterEmpty(element, "element");
         }
 
         @Override
         public String getXPath() {
             return parent == null ? "/" + element.getName() : parent.getXPath() + "/" + element.getName();
         }
 
         @Override
         public String getName() {
             return element.getName();
         }
     }
 
     private class PropertyNode extends StructureNode<FxPropertyEdit> {
         private final String alias;
 
         public PropertyNode(FxPropertyEdit element, String alias) {
             super(element);
             this.alias = alias;
         }
 
         @Override
         public void setParent(GroupNode parent) {
             super.setParent(parent);
             try {
                 // attach property
                 EJBLookup.getAssignmentEngine().createProperty(type.getId(), getElement(),
                         getParent() == null ? "/" : getParent().getXPath(), alias);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
     }
 
     private class GroupNode extends StructureNode<FxGroupEdit> {
         private FxGroupAssignment assignment;
 
         public GroupNode(FxGroupEdit element) {
             super(element);
         }
 
         public FxGroupAssignment getAssignment() {
             return assignment;
         }
 
         @Override
         public void setParent(GroupNode parent) {
             super.setParent(parent);
             try {
                 final long id = EJBLookup.getAssignmentEngine().createGroup(type.getId(), getElement(),
                         getParent() == null ? "/" : getParent().getXPath());
                 assignment = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(id);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
     }
 
     private class PropertyAssignmentNode extends Node<FxPropertyAssignmentEdit> {
         public PropertyAssignmentNode(FxPropertyAssignmentEdit element) {
             super(element);
         }
 
         @Override
         public void setParent(GroupNode parent) {
             super.setParent(parent);
             try {
                 element.setXPath((parent == null ? "" : getParent().getXPath()) + element.getXPath());
                 element.setParentGroupAssignment(parent != null ? parent.getAssignment() : null);
                 EJBLookup.getAssignmentEngine().save(element, true);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
     }
 
     private class GroupAssignmentNode extends Node<FxGroupAssignmentEdit> {
         private GroupAssignmentNode(FxGroupAssignmentEdit element) {
             super(element);
         }
 
         @Override
         public void setParent(GroupNode parent) {
             super.setParent(parent);
             try {
                 element.setXPath((parent == null ? "" : getParent().getXPath()) + element.getXPath());
                 element.setParentGroupAssignment(parent != null ? parent.getAssignment() : null);
                 EJBLookup.getAssignmentEngine().save(element, true);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
     }
 
     public GroovyTypeBuilder() {
     }
 
     public GroovyTypeBuilder(FxType type) {
         this.type = type;
     }
 
     public GroovyTypeBuilder(String typeName) {
         this.type = CacheAdmin.getEnvironment().getType(typeName);
     }
 
     public GroovyTypeBuilder(long typeId) {
         this.type = CacheAdmin.getEnvironment().getType(typeId);
     }
 
     @Override
     protected void setParent(Object parent, Object child) {
         if (parent == null) {
             return;
         }
         final Node parentNode = (Node) parent;
         final Node childNode = (Node) child;
         if (StringUtils.isBlank(parentNode.getXPath())) {
             childNode.setParent(null); // attaching to the root node
             return;
         }
         if (!(parentNode instanceof GroupNode)) {
             throw new FxInvalidParameterException("parent", "ex.scripting.builder.type.parent",
                     childNode.getName(), parentNode.getName()).asRuntimeException();
         }
         childNode.setParent((GroupNode) parentNode);
     }
 
     @Override
     protected Object createNode(Object name) {
        return "doCall".equals(name) ? new Node() : createNode(name, EMPTYATTRIBUTES, null);
     }
 
     @Override
     protected Object createNode(Object name, Object value) {
         return createNode(name, EMPTYATTRIBUTES, value);
     }
 
     @Override
     protected Object createNode(Object name, Map attributes) {
         return createNode(name, attributes, null);
     }
 
     @SuppressWarnings({"unchecked"})
     @Override
     protected Object createNode(Object name, Map attributes, Object value) {
         final String structureName = (String) name;
         if (this.type == null) {
             // root node, create type
             final ACL acl = (ACL) FxSharedUtils.get(attributes, "acl", CacheAdmin.getEnvironment().getACL(ACL.Category.STRUCTURE.getDefaultId()));
             final FxString description = (FxString) FxSharedUtils.get(attributes, "description", new FxString(structureName));
             final String parentTypeName = (String) FxSharedUtils.get(attributes, "parentTypeName", null);
             final Boolean useInstancePermissions = (Boolean) FxSharedUtils.get(attributes, "useInstancePermissions", null);
             final Boolean usePropertyPermissions = (Boolean) FxSharedUtils.get(attributes, "usePropertyPermissions", null);
             final Boolean useStepPermissions = (Boolean) FxSharedUtils.get(attributes, "useStepPermissions", null);
             final Boolean useTypePermissions = (Boolean) FxSharedUtils.get(attributes, "useTypePermissions", null);
             final Boolean usePermissions = (Boolean) FxSharedUtils.get(attributes, "usePermissions", null);
             try {
                 final FxTypeEdit type = FxTypeEdit.createNew(structureName, description, acl,
                         parentTypeName != null ? CacheAdmin.getEnvironment().getType(parentTypeName) : null);
                 if (useInstancePermissions != null) {
                     type.setUseInstancePermissions(useInstancePermissions);
                 }
                 if (usePropertyPermissions != null) {
                     type.setUsePropertyPermissions(usePropertyPermissions);
                 }
                 if (useStepPermissions != null) {
                     type.setUseStepPermissions(useStepPermissions);
                 }
                 if (useTypePermissions != null) {
                     type.setUseTypePermissions(useTypePermissions);
                 }
                 if (usePermissions != null && !usePermissions) {
                     type.setPermissions((byte) 0);
                 }
                 final long typeId = EJBLookup.getTypeEngine().save(type);
                 this.type = CacheAdmin.getEnvironment().getType(typeId);
                 return new Node();
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         final FxDataType dataType = value != null ? (FxDataType) value : (FxDataType) FxSharedUtils.get(attributes, "dataType", FxDataType.String1024);
         final FxMultiplicity multiplicity = (FxMultiplicity) FxSharedUtils.get(attributes, "multiplicity", FxMultiplicity.MULT_0_1);
         final String elementName = (String) FxSharedUtils.get(attributes, "name", StringUtils.capitalize(structureName));
         final FxString description = (FxString) FxSharedUtils.get(attributes, "description", new FxString(elementName));
         final FxString hint = (FxString) FxSharedUtils.get(attributes, "hint", new FxString(""));
         final ACL acl = (ACL) FxSharedUtils.get(attributes, "acl", CacheAdmin.getEnvironment().getACL(ACL.Category.STRUCTURE.getDefaultId()));
         final String assignment = (String) FxSharedUtils.get(attributes, "assignment", null);
         final String alias = (String) FxSharedUtils.get(attributes, "alias", elementName);
         final int defaultMultiplicity = (Integer) FxSharedUtils.get(attributes, "defaultMultiplicity", 1);
         if (Character.isLowerCase(structureName.charAt(0))) {
             // name starts lowercase --> create property or property assignment
             if (assignment != null) {
                 // create a new property assignment
                 final FxAssignment fxAssignment = CacheAdmin.getEnvironment().getAssignment(assignment);
                 if (!(fxAssignment instanceof FxPropertyAssignment)) {
                     throw new FxInvalidParameterException("assignment", "ex.scripting.builder.assignment.property",
                             name, assignment).asRuntimeException();
                 }
                 try {
                     final FxPropertyAssignmentEdit pa = FxPropertyAssignmentEdit.createNew((FxPropertyAssignment) fxAssignment,
                             type, alias, "/").setDefaultMultiplicity(defaultMultiplicity);
                     return new PropertyAssignmentNode(pa);
                 } catch (FxApplicationException e) {
                     throw e.asRuntimeException();
                 }
             } else {
                 // create a new property
                 final FxPropertyEdit property = FxPropertyEdit.createNew(StringUtils.capitalize(elementName),
                         description, hint, multiplicity, acl, dataType);
                 property.setAutoUniquePropertyName(true);
                 property.setAssignmentDefaultMultiplicity(defaultMultiplicity);
                 for (Object key : attributes.keySet()) {
                     if (isNonOptionKey((String) key))
                         continue;
                     final Object optionValue = attributes.get(key);
                     final String optionKey = ((String) key).toUpperCase();
                     // set non-generic property options
                     if ("FULLTEXTINDEXED".equals(optionKey)) {
                         property.setFulltextIndexed((Boolean) optionValue);
                     } else if ("UNIQUEMODE".equals(optionKey)) {
                         property.setUniqueMode((UniqueMode) optionValue);
                     } else if ("AUTOUNIQUEPROPERTYNAME".equals(optionKey)) {
                         property.setAutoUniquePropertyName((Boolean) optionValue);
                     } else if ("REFERENCEDTYPE".equals(optionKey)) {
                         property.setReferencedType((FxType) optionValue);
                     } else if ("REFERENCEDLIST".equals(optionKey)) {
                         property.setReferencedList((FxSelectList) optionValue);
                     } else {
                         // set generic options
                         if (optionValue instanceof Boolean) {
                             property.setOption(optionKey, true, (Boolean) optionValue);
                         } else if (optionValue != null) {
                             property.setOption(optionKey, true, optionValue.toString());
                         }
                     }
                 }
                 return new PropertyNode(property, alias);
             }
         } else {
             //TODO: set options for group
             // name starts uppercase --> group
             if (assignment != null) {
                 // existing group assignment
                 final FxAssignment fxAssignment = CacheAdmin.getEnvironment().getAssignment(assignment);
                 if (!(fxAssignment instanceof FxGroupAssignment)) {
                     throw new FxInvalidParameterException("assignment", "ex.scripting.builder.assignment.group", name, assignment).asRuntimeException();
                 }
                 try {
                     final FxGroupAssignmentEdit ga = FxGroupAssignmentEdit.createNew((FxGroupAssignment) fxAssignment,
                             type, alias, "/");
                     ga.setDefaultMultiplicity(defaultMultiplicity);
                     return new GroupAssignmentNode(ga);
                 } catch (FxApplicationException e) {
                     throw e.asRuntimeException();
                 }
             } else {
                 // create a new group
                 return new GroupNode(FxGroupEdit.createNew(elementName, description, hint,
                         false, multiplicity).setAssignmentDefaultMultiplicity(defaultMultiplicity));
             }
         }
     }
 }
