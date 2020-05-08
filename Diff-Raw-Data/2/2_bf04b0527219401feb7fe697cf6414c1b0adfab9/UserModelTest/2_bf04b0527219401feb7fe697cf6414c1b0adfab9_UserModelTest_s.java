 /*
  * Copyright 2008 Davy Verstappen.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.wintersleep.codeviz.uml.diagram;
 
 import org.hibernate.cfg.Configuration;
 import org.hibernate.mapping.PersistentClass;
 import org.wintersleep.codeviz.uml.model.CodeModel;
 import org.wintersleep.codeviz.uml.model.HibernateModelFactory;
 import org.wintersleep.test.util.FileTestUtil;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import static org.wintersleep.test.util.FileTestUtil.assertCreated;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/org/wintersleep/graphviz/diagram/clazz/hibernate/testApplicationContext.xml"})
 public class UserModelTest {
 
     private final File outputDir = FileTestUtil.makeOutputDir(UserModelTest.class);
 
     @Autowired
     private ApplicationContext applicationContext;
 
     @Test
     public void test() throws IOException {
         LocalSessionFactoryBean sessionFactoryBean = (LocalSessionFactoryBean) applicationContext.getBean("&sessionFactory");
         Configuration configuration = sessionFactoryBean.getConfiguration();
         assertNotNull(configuration);
 
         CodeModel model = new HibernateModelFactory(configuration, "User Model").create();
 
         ClassDiagram diagram = new ClassDiagram("UserDiagram", model);
         diagram.getSettings()
                 .enableDrawingAttributes()
                 //.enableDrawingOperations()
                 ;
         Iterator<PersistentClass> pci = configuration.getClassMappings();
         while (pci.hasNext()) {
             PersistentClass persistentClass = pci.next();
             Class clazz = persistentClass.getMappedClass();
             assertNotNull(clazz);
             diagram.add(clazz);
         }
 
         assertCreated(diagram.createGraph().makeImageFile(outputDir, "png", true));
     }
 
 }
