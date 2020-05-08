 /*
  * Copyright (C) 2009 - 2012 SMVP.NET
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
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
 
 package net.smvp.factory.generator.clazz;
 
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JPackage;
 import com.google.gwt.core.ext.typeinfo.TypeOracle;
 import net.smvp.factory.client.clazz.ClassCreator;
 import net.smvp.factory.scan.reader.ClassReflectionReader;
 import net.smvp.generator.generator.AbstractGenerator;
 import net.smvp.generator.scan.ClassScanner;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The Class ClassCreatorGenerator.
  *
  * @author Nguyen Duc Dung
  * @since 12/13/11, 11:15 AM
  */
 public class ClassCreatorGenerator extends AbstractGenerator<ClassCreatorTemplateData> {
 
     private static final String TEMPLATE_FILE = "ClassCreatorGenerated.ftl";
     private static final String DATA_TEMPLATE = "data";
 
     @Override
     protected Map<String, ClassCreatorTemplateData> scan() {
         ClassReflectionReader classReflectionReader = new ClassReflectionReader();
         ClassScanner scanner = new ClassScanner();
         scanner.addReader(classReflectionReader);
         TypeOracle typeOracle = context.getTypeOracle();
         for (JPackage jPackage : typeOracle.getPackages()) {
             for (JClassType jClassType : jPackage.getTypes()) {
                 scanner.scan(jClassType, context);
             }
         }
         Map<String, ClassCreatorTemplateData> dataTemplateMap = new HashMap<String, ClassCreatorTemplateData>();
         ClassCreatorTemplateData data = new ClassCreatorTemplateData(getClassName(), getPackageName());
         data.setClassScanModels(classReflectionReader.getData());
         dataTemplateMap.put(DATA_TEMPLATE, data);
         return dataTemplateMap;
     }
 
     @Override
     protected String getTemplateFileName() {
         return TEMPLATE_FILE;
     }
 
     @Override
     protected Class<?> getResourceClass() {
         return ClassCreator.class;
     }
 }
