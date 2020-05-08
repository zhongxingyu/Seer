 /**
  *  A Maven plugin to jury rig java class files with the Apache BCEL library.
  *  Copyright (C) 2012 NigelB
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 package rigger.agent;
 
 
 import org.apache.bcel.classfile.ClassParser;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.generic.ClassGen;
 import rigger.Log;
 import rigger.bce.RiggerML;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.lang.instrument.Instrumentation;
 import java.security.ProtectionDomain;
 
 public class RiggerAgent implements ClassFileTransformer {
 
     private String className;
     private Instrumentation i;
     private JAXBContext context;
     private Unmarshaller um;
     private RiggerML ref;
 
     public RiggerAgent(String className, Instrumentation i) throws JAXBException {
         this.className = className;
         this.i = i;
         context = JAXBContext.newInstance(RiggerML.class);
         um = context.createUnmarshaller();
         ref = (RiggerML) um.unmarshal(new File(className));
     }
 
     public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
         try {
             ClassParser cp =
                     new ClassParser(
                             new ByteArrayInputStream(classfileBuffer),
                             className + ".java");
             JavaClass jc = cp.parse();
             ClassGen cg = new ClassGen(jc);
             ref.process(cg, jc, new Log() {
                 public void info(String message) {
                     System.out.printf("[RiggerAgent] %s%n", message);
                 }
             });
            return cg.getJavaClass().getBytes();
         } catch (IOException e) {
 
         }
         return classfileBuffer;
     }
 
     public static void premain(String className,
                                Instrumentation i)
             throws ClassNotFoundException,
             InstantiationException,
             IllegalAccessException, JAXBException {
         i.addTransformer(new RiggerAgent(className, i));
     }
 
 }
