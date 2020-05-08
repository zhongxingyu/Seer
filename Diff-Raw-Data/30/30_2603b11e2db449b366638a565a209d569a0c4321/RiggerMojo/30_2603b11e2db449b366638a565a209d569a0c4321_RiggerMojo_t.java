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
 package rigger.maven;
 
 import org.apache.bcel.classfile.ClassParser;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.generic.ClassGen;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import rigger.bce.FieldFinder;
 import rigger.bce.MethodFinder;
 import rigger.bce.RiggerML;
 import rigger.extend.ContextCreator;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 /**
  * Jury rig java classfiles with the Apache bcel library.
  *
  * @goal jury-rig
  * @phase compile
  */
 public class RiggerMojo extends AbstractMojo {
 
     private JAXBContext context;
     private Unmarshaller um;
     private RiggerML ref;
     private Log logger = getLog();
    private rigger.Log ml = new rigger.Log() {
         public void info(String message) {
             logger.info(message);
         }
 
         public void error(String message) {
             logger.error(message);
         }
     };
 
     public RiggerMojo() throws MojoExecutionException {
         super();
         try {
 //            context = JAXBContext.newInstance(RiggerML.class, FieldFinder.class, MethodFinder.class);
             context = new ContextCreator(ml).createContext();
             um = context.createUnmarshaller();
         } catch (JAXBException e) {
             throw new MojoExecutionException("Could not create JAXB Unmarshaller", e);
         }
     }
 
     /**
      * Location of the class files to match.
      *
      * @parameter expression="${project.build.directory}/classes"
      * @required
      */
     private File classDirectory;
 
     /**
      * Location of the RiggerML file.
      *
      * @parameter expression="${project.basedir}/src/main/rigger/config.xml"
      * @required
      */
     private File riggerML;
 
     public void execute()
             throws MojoExecutionException {
 
 
         if (!riggerML.exists()) {
             throw new MojoExecutionException(String.format("RiggerML file: %s could not be found.", riggerML.getAbsolutePath()));
         }
         try {
             ref = (RiggerML) um.unmarshal(riggerML);
         } catch (JAXBException e) {
             throw new MojoExecutionException(String.format("Could not parse the: %s RiggerML file.", riggerML.getAbsolutePath()), e);
         }
         logger.info(String.format("Checking: %s", classDirectory));
         FilenameFilter ff = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 File current = new File(dir, name);
                 if (current.isDirectory()) {
                     current.list(this);
                 } else if (current.getName().endsWith(".class")) {
                     try {
                         ClassParser cp = new ClassParser(current.getPath());
                         JavaClass jc = cp.parse();
                         ClassGen cg = new ClassGen(jc);
                         if (ref.process(cg, jc, ml)) {
                             FileOutputStream fos = new FileOutputStream(current);
                             fos.write(cg.getJavaClass().getBytes());
                             fos.close();
                             logger.info(String.format("Class Jury Rigger wrote class: %s", jc.getClassName()));
                         } else {
                             logger.info(String.format("Class Jury Rigger ignoring class: %s", jc.getClassName()));
                         }
                     } catch (IOException e) {
                         logger.error("", e);
                     }
                 }
                 return false;
             }
         };
         classDirectory.list(ff);
 
     }
 }
