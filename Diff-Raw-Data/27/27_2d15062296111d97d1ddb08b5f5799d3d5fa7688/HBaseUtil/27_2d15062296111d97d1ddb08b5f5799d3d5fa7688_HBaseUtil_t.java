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
 package fr.imag.exschema.hbase;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 
 import fr.imag.exschema.DeclareVisitor;
 import fr.imag.exschema.SchemaFinder;
 import fr.imag.exschema.UpdateVisitor;
 import fr.imag.exschema.Util;
 import fr.imag.exschema.exporter.RooModel;
 import fr.imag.exschema.model.Attribute;
 import fr.imag.exschema.model.Set;
 import fr.imag.exschema.model.Struct;
 
 /**
  * HBase utility methods.
  * 
  * @author jccastrejon
  * 
  */
 public class HBaseUtil implements SchemaFinder {
 
     /**
      * Class logger.
      */
     private static Logger logger = Logger.getLogger(HBaseUtil.class.getName());
 
     @Override
     public List<Set> discoverSchemas(final IJavaProject project) throws JavaModelException {
         String tableName;
         Set currentTableSet;
         List<Set> returnValue;
         String tableDescriptor;
         TablePutVisitor putVisitor;
         List<String> tableDescriptors;
         TableCreateVisitor createVisitor;
         List<ASTNode> tableDescriptorsRoots;
         TableIncrementVisitor incrementVisitor;
         List<Integer> tableDescriptorNameIndex;
         TableDeclareVisitor tableDeclareVisitor;
         DescriptorDeclareVisitor descriptorDeclareVisitor;
 
         // Identify when tables are created
         createVisitor = new TableCreateVisitor();
         Util.analyzeJavaProject(project, createVisitor);
 
         // Identify when tables are declared
         tableDeclareVisitor = new TableDeclareVisitor();
         Util.analyzeJavaProject(project, tableDeclareVisitor);
 
         // Identify when data is inserted in a table
         putVisitor = new TablePutVisitor();
         Util.analyzeJavaProject(project, putVisitor);
 
         // Identify when columns are added to a column family
         incrementVisitor = new TableIncrementVisitor();
         Util.analyzeJavaProject(project, incrementVisitor);
 
         // Join tables' creations and declarations
         tableDescriptors = new ArrayList<String>(createVisitor.getUpdateInvocations().size()
                 + tableDeclareVisitor.getVariableDeclarations().size());
         tableDescriptorsRoots = new ArrayList<ASTNode>(tableDescriptors.size());
         tableDescriptorNameIndex = new ArrayList<Integer>(tableDescriptors.size());
         for (MethodInvocation createInvocation : createVisitor.getUpdateInvocations()) {
             tableDescriptors.add(createInvocation.arguments().get(0).toString());
             tableDescriptorsRoots.add(createInvocation.getRoot());
 
             // The table name is the first argument
             tableDescriptorNameIndex.add(0);
         }
 
         for (VariableDeclarationFragment variableDeclaration : tableDeclareVisitor.getVariableDeclarations()) {
             tableDescriptors.add(variableDeclaration.getName().toString());
             tableDescriptorsRoots.add(variableDeclaration.getRoot());
 
             // The table name is the second argument
             tableDescriptorNameIndex.add(1);
         }
 
         // Get tables data
         returnValue = new ArrayList<Set>();
         HBaseUtil.logger.log(Util.LOGGING_LEVEL, "\n\nHBase tables:");
         for (int i = 0; i < tableDescriptors.size(); i++) {
             tableDescriptor = tableDescriptors.get(i);
 
             // Table name
             descriptorDeclareVisitor = new DescriptorDeclareVisitor();
             descriptorDeclareVisitor.setVariableName(tableDescriptor);
             tableName = HBaseUtil.getHBaseName(tableDescriptorsRoots.get(i), descriptorDeclareVisitor,
                     tableDescriptorNameIndex.get(i));
             if (tableName != null) {
                 currentTableSet = new Set();
                 returnValue.add(currentTableSet);
                 currentTableSet.addAttribute(new Attribute("name", tableName));
                 currentTableSet.addAttribute(new Attribute("implementation", RooModel.HBASE.toString()));
                 HBaseUtil.logger.log(Util.LOGGING_LEVEL, "\n--Table: " + tableName);
 
                 // Analyze increment operations (HTableDescriptor.familyAdd)
                 HBaseUtil.analyzeIncrementOperations(incrementVisitor, putVisitor, tableDescriptorsRoots.get(i),
                         tableName, tableDescriptor, currentTableSet);
 
                 // Analyze put operations (HTable.put)
                 HBaseUtil.analyzePutOperations(putVisitor, tableName, tableDescriptorsRoots.get(i), currentTableSet);
             }
         }
 
         return returnValue;
     }
 
     /**
      * Analyze HTable.put operations.
      * 
      * @param tableName
      * @param rootNode
      * @param updateInvocations
      * @param currentTableSet
      */
     private static void analyzePutOperations(final TablePutVisitor putVisitor, final String tableName,
             final ASTNode rootNode, final Set currentTableSet) {
         Struct currentFamilyStruct;
         Struct currentColumnStruct;
         java.util.Set<String> columns;
         java.util.Set<String> familyNames;
 
         familyNames = HBaseUtil.getColumnFamilyNames(tableName, rootNode, new PutAddVisitor(),
                 putVisitor.getUpdateInvocations());
 
         for (String familyName : familyNames) {
             currentFamilyStruct = new Struct();
             currentTableSet.addStruct(currentFamilyStruct);
             currentFamilyStruct.addAttribute(new Attribute("name", familyName));
             HBaseUtil.logger.log(Util.LOGGING_LEVEL, "\n----Family: " + familyName);
 
            // Put.add(family, qualifier, value)
             columns = new HashSet<String>();
             columns.addAll(HBaseUtil.getHBaseColumns(tableName, rootNode, new PutAddVisitor(), familyName,
                     putVisitor.getUpdateInvocations()));
 
             for (String column : columns) {
                 currentColumnStruct = new Struct();
                 currentFamilyStruct.addStruct(currentColumnStruct);
                 currentColumnStruct.addAttribute(new Attribute("name", column));
                 HBaseUtil.logger.log(Util.LOGGING_LEVEL, "\n------Column: " + column);
             }
         }
 
     }
 
     /**
      * Get the column family names based on to the put operations associated to
      * a particular HBase table.
      * 
      * @param tableName
      * @param rootNode
      * @param updateVisitor
      * @param updateInvocations
      * @return
      */
     private static java.util.Set<String> getColumnFamilyNames(final String tableName, final ASTNode rootNode,
             UpdateVisitor updateVisitor, final List<MethodInvocation> updateInvocations) {
         String familyName;
         List<String> arguments;
         java.util.Set<String> returnValue;
 
         returnValue = new HashSet<String>();
         for (MethodInvocation putInvocation : updateInvocations) {
             if (HBaseUtil.isMatchingTable(tableName, putInvocation)) {
                 familyName = putInvocation.arguments().get(0).toString();
                 if (Util.isVariableName(familyName)) {
                     updateVisitor.setVariableName(familyName);
                     rootNode.accept(updateVisitor);
                     for (MethodInvocation addInvocation : updateVisitor.getUpdateInvocations()) {
                         arguments = HBaseUtil.getHBaseAddArguments(addInvocation);
                        if (arguments.size() >= 1) {
                            returnValue.add(arguments.get(0).replace('"', ' ').trim());
                        }
                     }
                 }
             }
         }
 
         return returnValue;
     }
 
     /**
      * Analyze HTableDescriptor.familyAdd operations.
      * 
      * @param incrementVisitor
      * @param putVisitor
      * @param rootNode
      * @param tableName
      * @param tableDescriptor
      * @param currentTableSet
      */
     private static void analyzeIncrementOperations(final TableIncrementVisitor incrementVisitor,
             final TablePutVisitor putVisitor, final ASTNode rootNode, final String tableName,
             final String tableDescriptor, final Set currentTableSet) {
         String columnFamilyName;
         Struct currentFamilyStruct;
         Struct currentColumnStruct;
         FamilyAddVisitor addVisitor;
         java.util.Set<String> columns;
         java.util.Set<String> familyNames;
         ColumnFamilyDeclareVisitor columnFamilyDeclareVisitor;
 
         familyNames = new HashSet<String>();
         addVisitor = new FamilyAddVisitor();
         addVisitor.setVariableName(tableDescriptor);
         rootNode.accept(addVisitor);
         for (MethodInvocation invocation : addVisitor.getUpdateInvocations()) {
             columnFamilyName = invocation.arguments().get(0).toString();
             columnFamilyDeclareVisitor = new ColumnFamilyDeclareVisitor();
             columnFamilyDeclareVisitor.setVariableName(columnFamilyName);
             columnFamilyName = HBaseUtil.getHBaseName(rootNode, columnFamilyDeclareVisitor);
             if ((columnFamilyName != null) && (!familyNames.contains(columnFamilyName))) {
                 familyNames.add(columnFamilyName);
                 currentFamilyStruct = new Struct();
                 currentTableSet.addStruct(currentFamilyStruct);
                 currentFamilyStruct.addAttribute(new Attribute("name", columnFamilyName));
                 HBaseUtil.logger.log(Util.LOGGING_LEVEL, "\n----Family: " + columnFamilyName);
 
                 // Columns
                 if ((tableName != null) && (columnFamilyName != null)) {
                    // Put.add(family, qualifier, value)
                     columns = new HashSet<String>();
                     columns.addAll(HBaseUtil.getHBaseColumns(tableName, rootNode, new PutAddVisitor(),
                             columnFamilyName, putVisitor.getUpdateInvocations()));
 
                     // addColumn(family, qualifier, amount)
                     columns.addAll(HBaseUtil.getHBaseColumns(tableName, rootNode, new AddColumnVisitor(),
                             columnFamilyName, incrementVisitor.getUpdateInvocations()));
 
                     for (String column : columns) {
                         currentColumnStruct = new Struct();
                         currentFamilyStruct.addStruct(currentColumnStruct);
                         currentColumnStruct.addAttribute(new Attribute("name", column));
                         HBaseUtil.logger.log(Util.LOGGING_LEVEL, "\n------Column: " + column);
                     }
                 }
             }
         }
     }
 
     /**
      * Get the HBase columns associated to the given update invocations.
      * 
      * @param tableName
      * @param rootNode
      * @param updateVisitor
      * @param columnFamilyName
      * @param updateInvocations
      * @return
      */
     private static List<String> getHBaseColumns(final String tableName, final ASTNode rootNode,
             UpdateVisitor updateVisitor, final String columnFamilyName, final List<MethodInvocation> updateInvocations) {
         String putName;
         List<String> arguments;
         List<String> returnValue;
 
         returnValue = new ArrayList<String>();
         for (MethodInvocation putInvocation : updateInvocations) {
             if (HBaseUtil.isMatchingTable(tableName, putInvocation)) {
                 putName = putInvocation.arguments().get(0).toString();
                 if (Util.isVariableName(putName)) {
                     updateVisitor.setVariableName(putName);
                     rootNode.accept(updateVisitor);
                     for (MethodInvocation addInvocation : updateVisitor.getUpdateInvocations()) {
                         arguments = HBaseUtil.getHBaseAddArguments(addInvocation);
                        if (arguments.size() >= 2) {
                            if (arguments.get(0).replace('"', ' ').trim().equals(columnFamilyName)) {
                                returnValue.add(arguments.get(1).replace('"', ' ').trim());
                            }
                         }
                     }
                 }
             }
         }
 
         return returnValue;
     }
 
     /**
      * Determine if the update invocation refers to the expected HBase table.
      * 
      * @param expectedTable
      * @param updateInvocation
      * @return
      */
     private static boolean isMatchingTable(final String expectedTable, final MethodInvocation updateInvocation) {
         boolean returnValue;
         Expression expression;
         MethodInvocation templateExecution;
 
         // HBase API
         returnValue = false;
         expression = updateInvocation.getExpression();
         if (expression != null) {
             if (expression.toString().equals(expectedTable)) {
                 returnValue = true;
             }
         }
 
         // Spring data
         if (!returnValue) {
             templateExecution = HBaseUtil.getSpringTemplateExecution(updateInvocation);
             if (templateExecution != null) {
                 returnValue = (templateExecution.arguments().get(0).toString().equals(expectedTable));
             }
         }
 
         return returnValue;
     }
 
     /**
      * Get Spring's HBase template method execution.
      * 
      * @param node
      * @return
      */
     private static MethodInvocation getSpringTemplateExecution(final ASTNode node) {
         MethodInvocation returnValue;
         MethodInvocation updateInvocation;
 
         returnValue = null;
         if (node != null) {
             if (MethodInvocation.class.isAssignableFrom(node.getClass())) {
                 updateInvocation = (MethodInvocation) node;
                 if ((updateInvocation.getName().toString().equals("execute"))
                         && (updateInvocation.getExpression().resolveTypeBinding().getQualifiedName()
                                 .equals("org.springframework.data.hadoop.hbase.HbaseTemplate"))) {
                     returnValue = updateInvocation;
                 }
             }
 
             if (returnValue == null) {
                 if (node.getParent() != null) {
                     returnValue = HBaseUtil.getSpringTemplateExecution(node.getParent());
                 }
             }
         }
 
         return returnValue;
     }
 
     /**
      * Get the list of arguments used in an HBase add operation.
      * 
      * @param putInvocation
      * @return
      */
     private static List<String> getHBaseAddArguments(final MethodInvocation putInvocation) {
         String currentArgument;
         List<String> returnValue;
 
         returnValue = new ArrayList<String>(putInvocation.arguments().size());
         for (Object argument : putInvocation.arguments()) {
             currentArgument = ((Expression) argument).toString();
             // TODO: Consider operations that don't rely on variables
             // TODO: Consider operations that don't rely on method invocations
             if (currentArgument.contains("(")) {
                 returnValue.add(currentArgument.substring(currentArgument.indexOf('(') + 1,
                         currentArgument.indexOf(')')));
             }
         }
 
         return returnValue;
     }
 
     /**
      * Get the name of the HBase table by analyzing the given declaration
      * definitions, assuming the table name is the first argument.
      * 
      * @param rootNode
      * @param declareVisitor
      * @return
      */
     private static String getHBaseName(final ASTNode rootNode, final DeclareVisitor declareVisitor) {
         return HBaseUtil.getHBaseName(rootNode, declareVisitor, 0);
     }
 
     /**
      * Get the name of the HBase table by analyzing the given declaration
      * definitions.
      * 
      * @param rootNode
      * @param declareVisitor
      * @param expectedArgumentIndex
      * @return
      */
     private static String getHBaseName(final ASTNode rootNode, final DeclareVisitor declareVisitor,
             final int expectedArgumentIndex) {
         String returnValue;
         int argumentIndex;
         VariableDeclarationFragment declaration;
 
         returnValue = null;
         rootNode.accept(declareVisitor);
         if (declareVisitor.getVariableDeclarations().size() > 0) {
             declaration = declareVisitor.getVariableDeclarations().get(0);
             // TODO: Support more cases, not only instantiation
             if (ClassInstanceCreation.class.isAssignableFrom(declaration.getInitializer().getClass())) {
                 // Check if we can satisfy the expected argument index
                 argumentIndex = 0;
                 if (((ClassInstanceCreation) declaration.getInitializer()).arguments().size() >= 2) {
                     argumentIndex = expectedArgumentIndex;
                 }
 
                 // TODO: Consider operations that don't rely on variables
                 // Only work with variables
                 returnValue = ((ClassInstanceCreation) declaration.getInitializer()).arguments().get(argumentIndex)
                         .toString();
                 if (!Util.isVariableName(returnValue)) {
                    // Support names specified with quotes. In this case we use
                    // the variable name, instead of the table name.
                    // Ex: HTable table = new HTable(conf, "testtable");
                    if (returnValue.contains("\"")) {
                        returnValue = declaration.getName().toString();
                    }
                 }
             }
         }
 
         return returnValue;
     }
 }
