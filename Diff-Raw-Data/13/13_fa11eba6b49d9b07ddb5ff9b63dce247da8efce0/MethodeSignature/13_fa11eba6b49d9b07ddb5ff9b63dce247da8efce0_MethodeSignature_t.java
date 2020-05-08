 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.tac.model;
 
 import fr.jamgotchian.abcd.core.type.JavaType;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class MethodeSignature {
 
     private final String methodName;
 
     private final JavaType returnType;
 
     private final List<JavaType> argumentTypes;
 
     public MethodeSignature(String methodName, JavaType returnType, List<JavaType> argumentTypes) {
         this.methodName = methodName;
         this.returnType = returnType;
         this.argumentTypes = argumentTypes;
     }
 
     public String getMethodName() {
         return methodName;
     }
 
     public JavaType getReturnType() {
         return returnType;
     }
 
     public List<JavaType> getArgumentTypes() {
         return argumentTypes;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
        builder.append(returnType).append("-").append(methodName);
        if (argumentTypes.size() > 0) {
            builder.append("-");
            for (Iterator<JavaType> it = argumentTypes.iterator(); it.hasNext();) {
                builder.append(it.next());
                if (it.hasNext()) {
                    builder.append("-");
                }
             }
         }
         return builder.toString();
     }
 }
