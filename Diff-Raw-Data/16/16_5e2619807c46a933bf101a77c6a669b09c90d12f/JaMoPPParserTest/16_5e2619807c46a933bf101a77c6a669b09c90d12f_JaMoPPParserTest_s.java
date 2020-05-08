 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.core.jamopp;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.emftext.language.java.classifiers.ConcreteClassifier;
 import org.emftext.language.java.containers.CompilationUnit;
 import org.fuin.srcgen4j.commons.DefaultContext;
 import org.fuin.srcgen4j.commons.JaxbHelper;
 import org.fuin.srcgen4j.commons.ParserConfig;
 import org.fuin.srcgen4j.commons.SrcGen4JConfig;
 import org.fuin.srcgen4j.core.base.SrcGen4JFile;
 import org.junit.Test;
 
 //CHECKSTYLE:OFF
 public class JaMoPPParserTest {
 
     @Test
     public void testParse() throws Exception {
 
         // PREPARE
         final DefaultContext context = new DefaultContext();
         final SrcGen4JFile dir = new SrcGen4JFile("src/test/jamopp");
         final SrcGen4JFile file = new SrcGen4JFile(dir, "test-config.xml");
         final JAXBContext jaxbContext = JAXBContext.newInstance(SrcGen4JConfig.class,
                 JaMoPPParserConfig.class, SrcGen4JFile.class);
         final SrcGen4JConfig srcGen4JConfig = new JaxbHelper().create(file.toFile(), jaxbContext);
         srcGen4JConfig.init(context, new File("."));
        final ParserConfig config = srcGen4JConfig.getParsers().get(0);
         assertThat(config.getConfig()).isNotNull();
         assertThat(config.getConfig().getConfig()).isNotNull();
         assertThat(config.getConfig().getConfig()).isInstanceOf(JaMoPPParserConfig.class);
         final JaMoPPParserConfig cfg = (JaMoPPParserConfig) config.getConfig().getConfig();
         assertThat(cfg.getBinDirs()).hasSize(1);
         assertThat(cfg.getJarFiles()).hasSize(1);
         assertThat(cfg.getSrcDirs()).hasSize(1);
 
         final List<SrcGen4JFile> srcDirs = new ArrayList<SrcGen4JFile>();
         srcDirs.add(dir);
 
         final JaMoPPParser testee = new JaMoPPParser();
         testee.initialize(context, config);
 
         // TEST
         final ResourceSet resourceSet = testee.parse();
 
         // VERIFY
         assertThat(resourceSet).isNotNull();
 
         // src/test/jamopp/src/MyClass.java
         final ConcreteClassifier myClass = find(resourceSet, "a.b.c.MyClass");
         assertThat(myClass).isNotNull();
 
         // src/test/jamopp/bin/a/b/c/MyClass2.class
         final ConcreteClassifier myClass2 = find(resourceSet, "a.b.c.MyClass2");
         assertThat(myClass2).isNotNull();
     }
 
     /**
      * Tries to find a concrete classifier in a resource set.
      * 
      * @param resourceSet
      *            Resource set to use.
      * @param fullQualifiedName
      *            Full qualified name of the class.
      * 
      * @return Concrete classifier or NULL if no concrete classifier with that
      *         name was found.
      */
     public static ConcreteClassifier find(final ResourceSet resourceSet,
             final String fullQualifiedName) {
         final TreeIterator<Notifier> it = resourceSet.getAllContents();
         while (it.hasNext()) {
             final Notifier notifier = it.next();
             if (notifier instanceof CompilationUnit) {
                 final CompilationUnit compilationUnit = (CompilationUnit) notifier;
                 final ConcreteClassifier cc = compilationUnit
                         .getConcreteClassifier(fullQualifiedName);
                 if (cc != null) {
                     return cc;
                 }
             }
         }
         return null;
     }
 
 }
 // CHECKSTYLE:ON
