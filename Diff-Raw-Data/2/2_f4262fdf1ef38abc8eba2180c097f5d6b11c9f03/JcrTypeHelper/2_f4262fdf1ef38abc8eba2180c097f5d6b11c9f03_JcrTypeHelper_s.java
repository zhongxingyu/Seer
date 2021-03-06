 /**
  * Copyright (C) 2010 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.xcmis.sp.jcr.exo;
 
 import org.xcmis.spi.BaseType;
 import org.xcmis.spi.CMIS;
 import org.xcmis.spi.ContentStreamAllowed;
 import org.xcmis.spi.DateResolution;
 import org.xcmis.spi.Precision;
 import org.xcmis.spi.PropertyDefinition;
 import org.xcmis.spi.PropertyType;
 import org.xcmis.spi.TypeDefinition;
 import org.xcmis.spi.Updatability;
 import org.xcmis.spi.impl.PropertyDefinitionImpl;
 import org.xcmis.spi.impl.TypeDefinitionImpl;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jcr.nodetype.NodeType;
 
 /**
  * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
  * @version $Id$
  */
 class JcrTypeHelper
 {
 
    /**
     * Get object type definition.
     *
     * @param nt JCR back-end node
     * @param includePropertyDefinition true if need include property definition
     *        false otherwise
     * @return object definition or <code>null</code> if specified JCR node-type
     *         has not corresponded CMIS type
     * @throws NotSupportedNodeTypeException if specified node-type is
     *         unsupported by xCMIS
     */
    public static TypeDefinition getTypeDefinition(NodeType nt, boolean includePropertyDefinition)
       throws NotSupportedNodeTypeException
    {
       if (nt.isNodeType(JcrCMIS.NT_FILE))
       {
          return getDocumentDefinition(nt, includePropertyDefinition);
       }
       else if (nt.isNodeType(JcrCMIS.NT_FOLDER) || nt.isNodeType(JcrCMIS.NT_UNSTRUCTURED))
       {
          return getFolderDefinition(nt, includePropertyDefinition);
       }
       else if (nt.isNodeType(JcrCMIS.CMIS_NT_RELATIONSHIP))
       {
          return getRelationshipDefinition(nt, includePropertyDefinition);
       }
       else if (nt.isNodeType(JcrCMIS.CMIS_NT_POLICY))
       {
          return getPolicyDefinition(nt, includePropertyDefinition);
       }
       else
       {
          throw new NotSupportedNodeTypeException("Type " + nt.getName() + " is unsupported for xCMIS.");
       }
    }
 
    /**
     * Get CMIS object type id by the JCR node type name.
     *
     * @param ntName the JCR node type name
     * @return CMIS object type id
     */
    public static String getCmisTypeId(String ntName)
    {
       if (ntName.equals(JcrCMIS.NT_FILE))
       {
          return BaseType.DOCUMENT.value();
       }
       if (ntName.equals(JcrCMIS.NT_FOLDER) || ntName.equals(JcrCMIS.NT_UNSTRUCTURED))
       {
          return BaseType.FOLDER.value();
       }
       return ntName;
    }
 
    /**
     * Get JCR node type name by the CMIS object type id.
     *
     * @param typeId the CMIS base object type id
     * @return JCR string node type
     */
    public static String getNodeTypeName(String typeId)
    {
       if (typeId.equals(BaseType.DOCUMENT.value()))
       {
          return JcrCMIS.NT_FILE;
       }
       if (typeId.equals(BaseType.FOLDER.value()))
       {
          return JcrCMIS.NT_FOLDER;
       }
       return typeId;
    }
 
    /**
     * Document type definition.
     *
     * @param nt node type
     * @param includePropertyDefinition true if need include property definition
     *        false otherwise
     * @return document type definition
     */
    protected static TypeDefinition getDocumentDefinition(NodeType nt, boolean includePropertyDefinition)
    {
       TypeDefinitionImpl def = new TypeDefinitionImpl();
       String localTypeName = nt.getName();
       String typeId = getCmisTypeId(localTypeName);
       def.setBaseId(BaseType.DOCUMENT);
       def.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
       def.setControllableACL(true);
       def.setControllablePolicy(true);
       def.setCreatable(true);
       def.setDescription("Cmis Document Type");
       def.setDisplayName(typeId);
       def.setFileable(true);
       def.setFulltextIndexed(true);
       def.setId(typeId);
       def.setIncludedInSupertypeQuery(true);
       def.setLocalName(localTypeName);
       def.setLocalNamespace(JcrCMIS.EXO_CMIS_NS_URI);
       if (typeId.equals(BaseType.DOCUMENT.value()))
       {
          def.setParentId(null); // no parents for root type
       }
       else
       {
          // Try determine parent type.
          NodeType[] superTypes = nt.getDeclaredSupertypes();
          for (NodeType superType : superTypes)
          {
             if (superType.isNodeType(JcrCMIS.NT_FILE))
             {
                // Take first type that is super for cmis:document or is cmis:document.
                def.setParentId(getCmisTypeId(superType.getName()));
                break;
             }
          }
       }
       def.setQueryable(true);
       def.setQueryName(typeId);
       def.setVersionable(true);
       if (includePropertyDefinition)
       {
          addPropertyDefinitions(def, nt);
       }
       return def;
    }
 
    /**
     * Folder type definition.
     *
     * @param nt node type
     * @param includePropertyDefinition true if need include property definition
     *        false otherwise
     * @return folder type definition
     */
    protected static TypeDefinition getFolderDefinition(NodeType nt, boolean includePropertyDefinition)
    {
       TypeDefinitionImpl def = new TypeDefinitionImpl();
       String localTypeName = nt.getName();
       String typeId = getCmisTypeId(localTypeName);
       def.setBaseId(BaseType.FOLDER);
       def.setControllableACL(true);
       def.setControllablePolicy(true);
       def.setCreatable(true);
       def.setDescription("Cmis Folder Type");
       def.setDisplayName(typeId);
       def.setFileable(true);
       def.setFulltextIndexed(false);
       def.setId(typeId);
       def.setIncludedInSupertypeQuery(true);
       def.setLocalName(localTypeName);
       def.setLocalNamespace(JcrCMIS.EXO_CMIS_NS_URI);
       if (typeId.equals(BaseType.FOLDER.value()))
       {
          def.setParentId(null); // no parents for root type
       }
       else
       {
          // Try determine parent type.
          NodeType[] superTypes = nt.getDeclaredSupertypes();
          for (NodeType superType : superTypes)
          {
             if (superType.isNodeType(JcrCMIS.NT_FOLDER))
             {
                // Take first type that is super for cmis:folder or is cmis:folder.
                def.setParentId(getCmisTypeId(superType.getName()));
                break;
             }
          }
       }
       def.setQueryable(true);
       def.setQueryName(typeId);
       if (includePropertyDefinition)
       {
          addPropertyDefinitions(def, nt);
       }
       return def;
    }
 
    /**
     * Get policy type definition.
     *
     * @param nt node type
     * @param includePropertyDefinition true if need include property definition
     *        false otherwise
     * @return type policy definition
     */
    protected static TypeDefinition getPolicyDefinition(NodeType nt, boolean includePropertyDefinition)
    {
       TypeDefinitionImpl def = new TypeDefinitionImpl();
       String localTypeName = nt.getName();
       String typeId = getCmisTypeId(localTypeName);
       def.setBaseId(BaseType.POLICY);
       def.setControllableACL(true);
       def.setControllablePolicy(true);
       def.setCreatable(true);
       def.setDescription("Cmis Policy Type");
       def.setDisplayName(typeId);
       def.setFileable(false);
       def.setFulltextIndexed(false);
       def.setId(typeId);
       def.setIncludedInSupertypeQuery(true);
       def.setLocalName(localTypeName);
       def.setLocalNamespace(JcrCMIS.EXO_CMIS_NS_URI);
       if (typeId.equals(BaseType.POLICY.value()))
       {
          def.setParentId(null); // no parents for root type
       }
       else
       {
          // Try determine parent type.
          NodeType[] superTypes = nt.getDeclaredSupertypes();
          for (NodeType superType : superTypes)
          {
             if (superType.isNodeType(JcrCMIS.CMIS_NT_POLICY))
             {
                // Take first type that is super for cmis:policy or is cmis:policy.
                def.setParentId(getCmisTypeId(superType.getName()));
                break;
             }
          }
       }
       def.setQueryable(false);
       def.setQueryName(typeId);
       if (includePropertyDefinition)
       {
          addPropertyDefinitions(def, nt);
       }
       return def;
    }
 
    /**
     * Get relationship type definition.
     *
     * @param nt node type
     * @param includePropertyDefinition true if need include property definition
     *        false otherwise
     * @return type relationship definition
     */
    protected static TypeDefinition getRelationshipDefinition(NodeType nt, boolean includePropertyDefinition)
    {
       TypeDefinitionImpl def = new TypeDefinitionImpl();
       String localTypeName = nt.getName();
       String typeId = getCmisTypeId(localTypeName);
       def.setBaseId(BaseType.RELATIONSHIP);
       def.setControllableACL(false);
       def.setControllablePolicy(false);
       def.setCreatable(true);
       def.setDescription("Cmis Relationship Type");
       def.setDisplayName(typeId);
       def.setFileable(false);
       def.setFulltextIndexed(false);
       def.setId(typeId);
       def.setIncludedInSupertypeQuery(false);
       def.setLocalName(localTypeName);
       def.setLocalNamespace(JcrCMIS.EXO_CMIS_NS_URI);
       if (typeId.equals(BaseType.RELATIONSHIP.value()))
       {
          def.setParentId(null); // no parents for root type
       }
       else
       {
          // Try determine parent type.
          NodeType[] superTypes = nt.getDeclaredSupertypes();
          for (NodeType superType : superTypes)
          {
             if (superType.isNodeType(JcrCMIS.CMIS_NT_RELATIONSHIP))
             {
                // Take first type that is super for cmis:relationship or is cmis:relationship.
                def.setParentId(getCmisTypeId(superType.getName()));
                break;
             }
          }
       }
       def.setQueryable(false);
       def.setQueryName(typeId);
       if (includePropertyDefinition)
       {
          addPropertyDefinitions(def, nt);
       }
       return def;
    }
 
    /**
     * Add property definitions.
     *
     * @param typeDefinition the object type definition
     * @param nt the JCR node type.
     */
    private static void addPropertyDefinitions(TypeDefinition typeDefinition, NodeType nt)
    {
       // Known described in spec. property definitions
       //      for (PropertyDefinition<?> propDef : PropertyDefinitionsMap.getAll(typeDefinition.getBaseId().value()))
       //         typeDefinition.getPropertyDefinitions().add(propDef);
 
       Map<String, PropertyDefinition<?>> pd =
          new HashMap<String, PropertyDefinition<?>>(PropertyDefinitions.getAll(typeDefinition.getBaseId().value()));
 
       Set<String> knownIds = PropertyDefinitions.getPropertyIds(typeDefinition.getBaseId().value());
 
       for (javax.jcr.nodetype.PropertyDefinition jcrPropertyDef : nt.getPropertyDefinitions())
       {
          String pdName = jcrPropertyDef.getName();
          // TODO : Do not use any constraint about prefixes, need discovery
          // hierarchy of JCR types or so on.
          if (pdName.startsWith("cmis:"))
          {
             // Do not process known properties
             if (!knownIds.contains(pdName))
             {
                PropertyDefinition<?> cmisPropDef = null;
                // TODO : default values.
                switch (jcrPropertyDef.getRequiredType())
                {
 
                   case javax.jcr.PropertyType.BOOLEAN :
                      PropertyDefinitionImpl<Boolean> boolDef =
                         new PropertyDefinitionImpl<Boolean>(pdName, pdName, pdName, null, pdName, null,
                            PropertyType.BOOLEAN, jcrPropertyDef.isProtected() ? Updatability.READONLY
                               : Updatability.READWRITE, false, jcrPropertyDef.isMandatory(), true, true, null,
                            jcrPropertyDef.isMultiple(), null, null);
 
                      cmisPropDef = boolDef;
                      break;
 
                   case javax.jcr.PropertyType.DATE :
                      PropertyDefinitionImpl<Calendar> dateDef =
                         new PropertyDefinitionImpl<Calendar>(pdName, pdName, pdName, null, pdName, null,
                            PropertyType.DATETIME, jcrPropertyDef.isProtected() ? Updatability.READONLY
                               : Updatability.READWRITE, false, jcrPropertyDef.isMandatory(), true, true, null,
                            jcrPropertyDef.isMultiple(), null, null);
 
                      dateDef.setDateResolution(DateResolution.TIME);
                      cmisPropDef = dateDef;
                      break;
 
                   case javax.jcr.PropertyType.DOUBLE :
                      PropertyDefinitionImpl<BigDecimal> decimalDef =
                         new PropertyDefinitionImpl<BigDecimal>(pdName, pdName, pdName, null, pdName, null,
                            PropertyType.DECIMAL, jcrPropertyDef.isProtected() ? Updatability.READONLY
                               : Updatability.READWRITE, false, jcrPropertyDef.isMandatory(), true, true, null,
                            jcrPropertyDef.isMultiple(), null, null);
 
                     decimalDef.setPrecision(Precision.Bit32);
                      decimalDef.setMaxDecimal(CMIS.MAX_DECIMAL_VALUE);
                      decimalDef.setMinDecimal(CMIS.MIN_DECIMAL_VALUE);
                      cmisPropDef = decimalDef;
                      break;
 
                   case javax.jcr.PropertyType.LONG :
                      PropertyDefinitionImpl<BigInteger> integerDef =
                         new PropertyDefinitionImpl<BigInteger>(pdName, pdName, pdName, null, pdName, null,
                            PropertyType.INTEGER, jcrPropertyDef.isProtected() ? Updatability.READONLY
                               : Updatability.READWRITE, false, jcrPropertyDef.isMandatory(), true, true, null,
                            jcrPropertyDef.isMultiple(), null, null);
 
                      integerDef.setMaxInteger(CMIS.MAX_INTEGER_VALUE);
                      integerDef.setMinInteger(CMIS.MIN_INTEGER_VALUE);
                      cmisPropDef = integerDef;
                      break;
 
                   case javax.jcr.PropertyType.NAME : // TODO
                      //                     CmisPropertyIdDefinitionType idDef = new CmisPropertyIdDefinitionType();
                      //                     idDef.setPropertyType(EnumPropertyType.ID);
                      //                     cmisPropDef = idDef;
                      //                     break;
                   case javax.jcr.PropertyType.REFERENCE :
                   case javax.jcr.PropertyType.STRING :
                   case javax.jcr.PropertyType.PATH :
                   case javax.jcr.PropertyType.BINARY :
                   case javax.jcr.PropertyType.UNDEFINED :
                      PropertyDefinitionImpl<String> stringDef =
                         new PropertyDefinitionImpl<String>(pdName, pdName, pdName, null, pdName, null,
                            PropertyType.STRING, jcrPropertyDef.isProtected() ? Updatability.READONLY
                               : Updatability.READWRITE, false, jcrPropertyDef.isMandatory(), true, true, null,
                            jcrPropertyDef.isMultiple(), null, null);
                      stringDef.setMaxLength(CMIS.MAX_STRING_LENGTH);
                      cmisPropDef = stringDef;
                      break;
 
                }
                pd.put(cmisPropDef.getId(), cmisPropDef);
             }
          }
       }
       ((TypeDefinitionImpl)typeDefinition).setPropertyDefinitions(pd);
    }
 
 }
