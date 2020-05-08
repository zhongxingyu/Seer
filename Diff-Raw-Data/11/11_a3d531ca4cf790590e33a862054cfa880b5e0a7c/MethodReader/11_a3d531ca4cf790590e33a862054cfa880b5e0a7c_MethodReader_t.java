 /*
  * Copyright (C) 2009 - 2012 SMVP4G.COM
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
 
 package com.smvp4g.reflection.scan.reader;
 
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JMethod;
 import com.google.gwt.core.ext.typeinfo.JParameter;
 import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
 import com.smvp4g.generator.scan.model.MethodScanModel;
 import com.smvp4g.generator.scan.reader.Reader;
 import com.smvp4g.generator.scan.utils.ScanUtils;
 import org.apache.commons.lang.StringUtils;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Class MethodReader.
  *
  * @author Nguyen Duc Dung
  * @since 12/4/11, 4:06 PM
  */
 public class MethodReader implements Reader<MethodScanModel> {
 
     private List<MethodScanModel> models = new ArrayList<MethodScanModel>();
 
     @Override
     public void read(JClassType classType, GeneratorContext context) {
         for (JMethod method : classType.getMethods()) {
             if (method.isPublic() || method.isDefaultAccess()) {
                 MethodScanModel model = new MethodScanModel();
                 model.setName(method.getName());
                 if (method.getReturnType().isPrimitive() != null) {
                     JPrimitiveType returnType = method.getReturnType().isPrimitive();
                     model.setReturnType(returnType.getQualifiedBoxedSourceName());
                     if (returnType == JPrimitiveType.VOID) {
                         model.setVoidMethod(true);
                     }
                 } else {
                     model.setReturnType(method.getReturnType().getQualifiedSourceName());
                 }
                 model.setAnnotationScanModels(ScanUtils.getAnnotations(method));
                 model.setPramsLength(method.getParameters().length);
                 String params = StringUtils.EMPTY;
                 int i = 0;
                 for (JParameter parameter : method.getParameters()) {
                     if (i > 0) {
                         params = params + ",";
                     }
                    params += "(" ;
                    if (parameter.getType().isPrimitive() == null) {
                        params += parameter.getType().
                                                    getQualifiedSourceName();
                    } else {
                        params += parameter.getType().isPrimitive().
                                                    getQualifiedBoxedSourceName();
                    }
                    params += ")params[" + i + "]";
                     i++;
                 }
                 model.setParams(params);
                 models.add(model);
             }
         }
     }
 
     @Override
     public boolean isMath(Annotation checkAnnotation) {
         return true;
     }
 
     @Override
     public List<MethodScanModel> getData() {
         return models;
     }
 }
