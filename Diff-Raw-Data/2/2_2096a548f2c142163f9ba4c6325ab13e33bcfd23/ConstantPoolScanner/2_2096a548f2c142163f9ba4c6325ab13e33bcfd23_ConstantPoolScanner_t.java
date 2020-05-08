 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
  *
  * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  * Other names may be trademarks of their respective owners.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 package org.jenkinsci.constant_pool_scanner;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 import static org.jenkinsci.constant_pool_scanner.ConstantType.*;
 
 /**
  * Streaming parser of the constant pool in a Java class file.
  *
  * This might be used for dependency analysis, class loader optimizations, etc.
  * @see <a href="http://hg.netbeans.org/main-silver/raw-file/4a24ea1d4a94/nbbuild/antsrc/org/netbeans/nbbuild/VerifyClassLinkage.java">original sources</a>
  */
 public class ConstantPoolScanner {
 
     /**
      * Examines the constant pool of a class file and looks for references to other classes.
      * @param data a Java class file
      * @return a (sorted) set of binary class names (e.g. {@code some.pkg.Outer$Inner})
      * @throws IOException in case of malformed bytecode
      */
     public static Set<String> dependencies(byte[] data) throws IOException {
         return dependencies(new ByteArrayInputStream(data));
     }
 
     /**
      * Examines the constant pool of a class file and looks for references to other classes.
      * @param in Stream that reads a Java class file
      * @return a (sorted) set of binary class names (e.g. {@code some.pkg.Outer$Inner})
      * @throws IOException in case of malformed bytecode
      */
     public static Set<String> dependencies(InputStream in) throws IOException {
         ConstantPool pool = parse(in,CLASS,NAME_AND_TYPE);
 
         Set<String> result = new TreeSet<String>();
 
         for (ClassConstant cc : pool.list(ClassConstant.class)) {
             String s = cc.get();
             while (s.charAt(0) == '[') {
                 // array type
                 s = s.substring(1);
             }
             if (s.length() == 1) {
                 // primitive
                 continue;
             }
             String c;
             if (s.charAt(s.length() - 1) == ';' && s.charAt(0) == 'L') {
                 // Uncommon but seems sometimes this happens.
                 c = s.substring(1, s.length() - 1);
             } else {
                 c = s;
             }
             result.add(c.replace('/', '.'));
         }
 
         for (NameAndTypeConstant cc : pool.list(NameAndTypeConstant.class)) {
             String s = cc.getDescriptor();
             int idx = 0;
             while ((idx = s.indexOf('L', idx)) != -1) {
                 int semi = s.indexOf(';', idx);
                 if (semi == -1) {
                     throw new IOException("Invalid type or descriptor: " + s);
                 }
                 result.add(s.substring(idx + 1, semi).replace('/', '.'));
                 idx = semi;
             }
         }
 
         return result;
     }
 
     private ConstantPoolScanner() {
     }
 
     /**
      * Parses a class file and invokes the visitor with constants.
      */
     public static ConstantPool parse(byte[] source, ConstantType... types) throws IOException {
         return parse(new ByteArrayInputStream(source),types);
     }
 
     /**
      * Parses a class file and invokes the visitor with constants.
      */
     public static ConstantPool parse(InputStream source, ConstantType... types) throws IOException {
         return parse(new DataInputStream(source),Arrays.asList(types));
     }
 
     /**
      * Parses a class file and invokes the visitor with constants.
      */
     public static ConstantPool parse(DataInput s, Collection<ConstantType> _collect) throws IOException {
         skip(s,8); // magic, minor_version, major_version
         int size = s.readUnsignedShort() - 1; // constantPoolCount
         ConstantPool pool = new ConstantPool(size);
 
         // figure out all the types of constants we need to collect
         final EnumSet<ConstantType> collect = transitiveClosureOf(_collect);
 
         for (int i = 0; i < size; i++) {
             int tag = s.readByte();
             switch (tag) {
                 case 1: // CONSTANT_Utf8
                     if (collect.contains(UTF8))
                         pool.utf8At(i).actual = s.readUTF();
                     else
                         skip(s,s.readUnsignedShort());
                     break;
                 case 7: // CONSTANT_Class
                     if (collect.contains(CLASS))
                         pool.classAt(i).set(pool.utf8At(readIndex(s)));
                     else
                         skip(s,2);
                     break;
                 case 3: // CONSTANT_Integer
                     if (collect.contains(INTEGER))
                         pool.set(i,s.readInt());
                     else
                         skip(s,4);
                     break;
                 case 4: // CONSTANT_Float
                     if (collect.contains(FLOAT))
                         pool.set(i,s.readFloat());
                     else
                         skip(s,4);
                     break;
                 case 9: // CONSTANT_Fieldref
                     if (collect.contains(FIELD_REF))
                         pool.fieldRefAt(i).set(pool.classAt(readIndex(s)),pool.nameAndTypeAt(readIndex(s)));
                     else
                         skip(s,4);
                     break;
                 case 10: // CONSTANT_Methodref
                     if (collect.contains(METHOD_REF))
                         pool.methodRefAt(i).set(pool.classAt(readIndex(s)),pool.nameAndTypeAt(readIndex(s)));
                     else
                         skip(s,4);
                     break;
                 case 11: // CONSTANT_InterfaceMethodref
                     if (collect.contains(INTERFACE_METHOD_REF))
                         pool.interfaceMethodRefAt(i).set(pool.classAt(readIndex(s)),pool.nameAndTypeAt(readIndex(s)));
                     else
                         skip(s,4);
                     break;
                 case 12: // CONSTANT_NameAndType
                     if (collect.contains(NAME_AND_TYPE))
                         pool.nameAndTypeAt(i).set(pool.utf8At(readIndex(s)),pool.utf8At(readIndex(s)));
                     else
                         skip(s,4);
                     break;
                 case 8: // CONSTANT_String
                     if (collect.contains(STRING))
                         pool.set(i, new StringConstant(pool.utf8At(readIndex(s))));
                    else
                        skip(s,2);
                     break;
                 case 5: // CONSTANT_Long
                     if (collect.contains(LONG))
                         pool.set(i,s.readLong());
                     else
                         skip(s,8);
                     i++; // weirdness in spec
                     break;
                 case 6: // CONSTANT_Double
                     if (collect.contains(DOUBLE))
                         pool.set(i,s.readDouble());
                     else
                         skip(s,8);
                     i++; // weirdness in spec
                     break;
                 case 15:// CONSTANT_MethodHandle
                     skip(s,3);
                     break;
                 case 16:// CONSTANT_MethodType
                     skip(s,2);
                     break;
                 case 18:// CONSTANT_INVOKE_DYNAMIC
                     skip(s,4);
                     break;
                 default:
                     throw new IOException("Unrecognized constant pool tag " + tag + " at index " + i +
                             "; running constants: " + pool);
             }
         }
 
         return pool;
     }
 
     private static EnumSet<ConstantType> transitiveClosureOf(Collection<ConstantType> collect) {
         EnumSet<ConstantType> subject = EnumSet.copyOf(collect);
         for (ConstantType c : collect) {
             subject.addAll(c.implies);
         }
         return subject;
     }
 
     private static void skip(DataInput source, int bytes) throws IOException {
         // skipBytes cannot be used reliably because 0 is a valid return value
         // and we can end up looping forever
         source.readFully(new byte[bytes]);
     }
 
     private static int readIndex(DataInput source) throws IOException {
         return source.readUnsignedShort() - 1;
     }
 }
