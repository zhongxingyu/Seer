 /*
  * Copyright 2012 and beyond, Juan Luis Paz
  *
  * This file is part of Uaithne.
  *
  * Uaithne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Uaithne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Uaithne. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.uaithne.generator.commons;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import javax.lang.model.element.TypeElement;
 import org.uaithne.annotations.Doc;
 
 public class ExecutorModuleInfo {
     private String name;
     private String nameUpper;
     private String nameLower;
     private String operationPackage;
     private String realName;
     private String executorInterfaceName;
     private HashMap<String, OperationInfo> operationsByRealName = new HashMap<String, OperationInfo>();
     private ArrayList<OperationInfo> operations = new ArrayList<OperationInfo>();
     private HashMap<String, EntityInfo> entitiesByRealName = new HashMap<String, EntityInfo>(); 
     private ArrayList<EntityInfo> entities = new ArrayList<EntityInfo>();
     private TypeElement element;
     private boolean containOrderedOperations;
    private boolean containPagedOperations;
     private String[] documentation;
 
     public HashMap<String, OperationInfo> getOperationsByRealName() {
         return operationsByRealName;
     }
 
     public ArrayList<OperationInfo> getOperations() {
         return operations;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
         nameUpper = null;
         nameLower = null;
     }
 
     public String getNameUpper() {
         if (nameUpper == null) {
             nameUpper =  Utils.firstUpper(name);
         }
         return nameUpper;
     }
 
     public String getNameLower() {
         if (nameLower == null) {
             nameLower = Utils.firstLower(name);
         }
         return nameLower;
     }
 
     public String getOperationPackage() {
         return operationPackage;
     }
 
     public void setOperationPackage(String operationPackage) {
         this.operationPackage = operationPackage;
     }
 
     public String getRealName() {
         return realName;
     }
 
     public void setRealName(String realName) {
         this.realName = realName;
     }
 
     public String getExecutorInterfaceName() {
         if (executorInterfaceName == null) {
             executorInterfaceName = getNameUpper() + "Executor";
         }
         return executorInterfaceName;
     }
     
     public void addOperation(OperationInfo operation) {
         if (operation.getRealName() != null) {
             operationsByRealName.put(operation.getRealName(), operation);
         }
         operations.add(operation);
         if (operation.isOrdered()) {
             containOrderedOperations = true;
         }
        if (operation.getOperationKind() == OperationKind.SELECT_PAGE) {
            containPagedOperations = true;
        }
     }
 
     public HashMap<String, EntityInfo> getEntitiesByRealName() {
         return entitiesByRealName;
     }
 
     public ArrayList<EntityInfo> getEntities() {
         return entities;
     }
     
     public void addEntity(EntityInfo entityInfo) {
         entitiesByRealName.put(entityInfo.getRealName(), entityInfo);
         entities.add(entityInfo);
     }
     
     public void appendDefinitionImports(String currentPackage, HashSet<String> imports) {
         for (OperationInfo operation : operations) {
             operation.appendUseImports(currentPackage, imports);
         }
     }
     
     public void appendNotMannuallyDefinitionImports(String currentPackage, HashSet<String> imports) {
         for (OperationInfo operation : operations) {
             if (!operation.isManually()) {
                 operation.appendUseImports(currentPackage, imports);
             }
         }
     }
     
     public void appendPlainDefinitionImports(String currentPackage, HashSet<String> imports) {
         for (OperationInfo operation : operations) {
             operation.appendPlainDefinitionImports(currentPackage, imports);
         }
         DataTypeInfo.PAGE_INFO_DATA_TYPE.appendImports(currentPackage, imports);
         DataTypeInfo.LIST_DATA_TYPE.appendImports(currentPackage, imports);
     }
     
     public void appendPlainImplementationImports(String currentPackage, HashSet<String> imports) {
         for (OperationInfo operation : operations) {
             operation.appendPlainImplementationImports(currentPackage, imports);
         }
         DataTypeInfo.PAGE_INFO_DATA_TYPE.appendImports(currentPackage, imports);
         DataTypeInfo.LIST_DATA_TYPE.appendImports(currentPackage, imports);
     }
     
     public TypeElement getElement() {
         return element;
     }
     
     public <A extends Annotation> A getAnnotation(Class<A> type) {
         if (element == null) {
             return null;
         }
         return element.getAnnotation(type);
     }
 
     public boolean isContainOrderedOperations() {
         return containOrderedOperations;
     }
 
    public boolean isContainPagedOperations() {
        return containPagedOperations;
     }
 
     public String[] getDocumentation() {
         return documentation;
     }
 
     public void setDocumentation(String[] documentation) {
         this.documentation = documentation;
     }
     
     public ExecutorModuleInfo(TypeElement classElement) {
         element = classElement;
         name = Utils.dropFirstUnderscore(element.getSimpleName().toString());
         realName = classElement.getQualifiedName().toString();
         operationPackage = NamesGenerator.createOperationsPackageName(element);
         
         Doc doc = element.getAnnotation(Doc.class);
         if (doc != null) {
             documentation = doc.value();
         }
     }
 }
