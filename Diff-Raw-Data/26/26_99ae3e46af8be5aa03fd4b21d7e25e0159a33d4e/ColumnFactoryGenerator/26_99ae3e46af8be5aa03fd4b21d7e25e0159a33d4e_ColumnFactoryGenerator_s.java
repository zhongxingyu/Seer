 /* 
  * Copyright 2012 Matthias Huebner
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.gwtao.ui.cellview.generator;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.core.ext.Generator;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.TreeLogger.Type;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JField;
 import com.google.gwt.core.ext.typeinfo.JMethod;
 import com.google.gwt.core.ext.typeinfo.NotFoundException;
 import com.google.gwt.core.ext.typeinfo.TypeOracle;
 import com.google.gwt.editor.rebind.model.ModelUtils;
 import com.google.gwt.user.cellview.client.AbstractCellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
 import com.google.gwt.user.rebind.SourceWriter;
 import com.gwtao.ui.cellview.client.BooleanCell;
 import com.gwtao.ui.cellview.client.ColumnFactory.Align;
 import com.gwtao.ui.cellview.client.ColumnFactory.Default;
 import com.gwtao.ui.cellview.client.ColumnFactory.GenColumn;
 import com.gwtao.ui.cellview.client.TextCellEx;
 import com.gwtao.ui.util.generator.TypeUtils;
 
 public class ColumnFactoryGenerator extends Generator {
 
   public ColumnFactoryGenerator() {
   }
 
   @Override
   public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
     try {
       JClassType classType = context.getTypeOracle().getType(typeName);
       String packageName = classType.getPackage().getName();
       String simpleName = classType.getName().replace('.', '_') + "Impl";
       PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
       if (printWriter != null) {
         generate(context, printWriter, logger, classType, packageName, simpleName);
       }
       return packageName + "." + simpleName;
     }
     catch (NotFoundException e) {
       logger.log(Type.ERROR, e.getMessage(), e);
       throw new UnableToCompleteException();
     }
   }
 
   private void generate(GeneratorContext context, PrintWriter printWriter, TreeLogger logger, JClassType classType, String packageName, String simpleName) throws UnableToCompleteException, NotFoundException {
     JClassType[] interfaces = classType.getImplementedInterfaces();
     if (interfaces.length != 1) {
       logger.log(Type.ERROR, "too many interfaces in " + classType);
       throw new UnableToCompleteException();
     }
 
     JClassType intfType = context.getTypeOracle().getType(com.gwtao.ui.cellview.client.ColumnFactory.class.getName()).isInterface();
     JClassType[] parameters = ModelUtils.findParameterizationOf(intfType, classType);
     assert parameters.length == 1 : "Unexpected number of type parameters";
     JClassType viewType = parameters[0];
 
     JClassType columnType = context.getTypeOracle().getType(Column.class.getName());
 
     ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName, simpleName);
 
     composer.addImplementedInterface(classType.getQualifiedSourceName());
 
     composer.addImport(List.class.getName());
     composer.addImport(ArrayList.class.getName());
     composer.addImport(Column.class.getName());
     composer.addImport(viewType.getQualifiedSourceName());
 
     SourceWriter src = composer.createSourceWriter(context, printWriter);
     String viewName = viewType.getSimpleSourceName();
     JField[] fields = viewType.getFields();
 
     src.println("public void generateColumns( %s view, %s grid ) {", viewName, AbstractCellTable.class.getName());
 
     for (JField field : fields) {
       GenColumn info = field.getAnnotation(com.gwtao.ui.cellview.client.ColumnFactory.GenColumn.class);
       if (info != null) {
         generateAdapterClasses(src, logger, field, context.getTypeOracle(), columnType, info);
       }
     }
 
     src.println("  if( grid != null ) {");
 
     for (JField field : fields) {
       GenColumn info = field.getAnnotation(com.gwtao.ui.cellview.client.ColumnFactory.GenColumn.class);
       if (info != null) {
         generateInitGrid(src, logger, field, context.getTypeOracle(), columnType, info);
       }
     }
 
     src.println("  }");
     src.println("}");
     src.commit(logger);
   }
 
   private void generateAdapterClasses(SourceWriter src, TreeLogger logger, JField uiField, TypeOracle typeOracle, JClassType columnType, GenColumn info) throws UnableToCompleteException, NotFoundException {
     JClassType[] parameters = ModelUtils.findParameterizationOf(columnType, uiField.getType().isClassOrInterface());
     assert parameters.length == 2 : "Unexpected number of type parameters";
     JClassType modelType = parameters[0];
     JClassType valueType = parameters[1];
 
     String uiFieldName = uiField.getName();
     String modelFieldName;
     if (uiFieldName.endsWith("Col"))
       modelFieldName = uiFieldName.substring(0, uiFieldName.length() - 3);
     else
       modelFieldName = uiFieldName;
 
     JMethod getter = TypeUtils.getGetter(modelType, modelFieldName);
     JMethod setter = null;
 
     if (info.editable())
       setter = TypeUtils.getSetter(modelType, modelFieldName, getter.getReturnType());
 
     String cellName = "cell_" + uiField.getName();
     generateCell(src, cellName, valueType, info.cellType());
 
     src.println("  view.%s = new %s( %s ) {", uiField.getName(), uiField.getType().getParameterizedQualifiedSourceName(), cellName);
     src.println("    @Override");
     src.println("    public %s getValue(%s object) {", valueType.getQualifiedSourceName(), modelType.getQualifiedSourceName());
     src.println("      return object.%s();", getter.getName());
     src.println("    }");
     src.println("  };");
 
     if (info.sortable())
       src.println("  view.%s.setSortable( true );", uiField.getName());
 
     if (!info.dataStoreName().isEmpty())
       src.println("  view.%s.setDataStoreName( %s );", uiField.getName(), info.dataStoreName());
 
     if (info.align() != Align.DEFAULT)
      src.println("  view.%s.setHorizontalAlignment(HorizontalAlignmentConstant.%s );", uiField.getName(), mapAlign(info.align()));
 
     if (info.editable()) {
       src.println("  view.%s.setFieldUpdater( new FieldUpdater<%s,%s>( ) {", uiField.getName(), modelType.getQualifiedSourceName(), valueType.getQualifiedSourceName());
       src.println("    void update(int index, %s object, %s value) {", modelType.getQualifiedSourceName(), valueType.getQualifiedSourceName());
       src.println("      object.%s(value);", setter.getName());
       src.println("    }");
       src.println("  };");
     }
   }
 
   private String mapAlign(Align align) {
     switch (align) {
     case DEFAULT:
     case LEFT:
       return "ALIGN_LEFT";
     case CENTER:
       return "ALIGN_CENTER";
     case RIGHT:
       return "ALIGN_RIGHT";
     }
     throw new IllegalArgumentException("Unexpected type of Align=" + align);
   }
 
   private void generateInitGrid(SourceWriter src, TreeLogger logger, JField uiField, TypeOracle typeOracle, JClassType columnType, GenColumn info) throws UnableToCompleteException, NotFoundException {
     if (info.title().isEmpty())
       src.println("    grid.addColumn( view.%s );", uiField.getName());
     else
       src.println("    grid.addColumn( view.%s, \"%s\" );", uiField.getName(), info.title());
 
     if (info.width() > 0.0) {
      src.println("  grid.setColumnWidth( view.%s, %s, Unit.%s);", uiField.getName(), info.width(), info.unit().getType());
     }
 
   }
 
   private void generateCell(SourceWriter src, String cellName, JClassType valueType, Class<? extends Cell<?>> cellType) throws NotFoundException {
     if (!cellType.equals(Default.class)) {
       String cellClass = cellType.getName().replace('$', '.');
       if (cellType.getName().indexOf('$') != -1)
         src.println("  %s %s = view.new %s();", cellClass, cellName, cellType.getSimpleName());
       else
         src.println("  %s %s = new %s();", cellType.getName(), cellName, cellType.getName());
     }
     else if (String.class.getName().equals(valueType.getQualifiedSourceName())) {
       src.println("  %s %s = new %s();", TextCellEx.class.getName(), cellName, TextCellEx.class.getName());
     }
     else if (Date.class.getName().equals(valueType.getQualifiedSourceName())) {
       src.println("  com.google.gwt.i18n.client.DateTimeFormat fmt_%s = com.google.gwt.i18n.client.DateTimeFormat.getFormat(com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_MEDIUM);", cellName);
       src.println("  %s %s = new %s(fmt_%s);", DateCell.class.getName(), cellName, DateCell.class.getName(), cellName);
     }
     else if (Number.class.getName().equals(valueType.getQualifiedSourceName())) {
       src.println("  %s %s = new %s();", NumberCell.class.getName(), cellName, NumberCell.class.getName());
     }
     else if ("boolean".equals(valueType.getQualifiedSourceName()) || Boolean.class.getName().equals(valueType.getQualifiedSourceName())) {
       src.println("  %s %s = new %s();", BooleanCell.class.getName(), cellName, BooleanCell.class.getName());
     }
     else
       throw new NotFoundException("Unable to compute cell type for: " + valueType.getQualifiedSourceName());
   }
 }
