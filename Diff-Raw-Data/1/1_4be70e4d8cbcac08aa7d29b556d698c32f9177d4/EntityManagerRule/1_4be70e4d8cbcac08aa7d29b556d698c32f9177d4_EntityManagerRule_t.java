 /**
  * Copyright 2013 Peter Daum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.coderskitchen.junitrules.em;
 
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceContext;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 /**
  * This rule provides simplified access and usage of entity mangers in unit tests.
  * <p>
  * It scans the test class for occurrences of @PersistenceContext annotations.
  * <br>
  * For every detected persistence context annotation it creates an entity manager and injects it into the annotated field.
  * The entity managers uses the persistence unit declared by the unitName property of the PersistenceContext annotation.
  * </p>
  * <p>
  * The usage of this property is mandatory.
  * </p>
  *
  * @author peter
  */
 public class EntityManagerRule implements TestRule {
   private final Object testClass;
   private final ArrayList<EntityManager> entityManagers;
 
   /**
    * Constructor that starts the injection process.
    *
    * @param testClass Class that represents the current unit test
    */
   public EntityManagerRule(Object testClass) {
     this.testClass = testClass;
     ArrayList<Field> fields = findFieldsRequestingEntityManager();
     entityManagers = createAndInjectEntityManagers(fields);
   }
 
   public Statement apply(Statement base, Description description) {
     return new EntityManagerStatement(base, entityManagers);
   }
 
   private ArrayList<Field> findFieldsRequestingEntityManager() {
     Field[] declaredFields = testClass.getClass().getDeclaredFields();
     ArrayList<Field> annotatedFields = new ArrayList<Field>();
     if (declaredFields == null)
       return annotatedFields;
 
     Boolean faultFound = false;
     for (Field field : declaredFields) {
       PersistenceContext annotation = field.getAnnotation(PersistenceContext.class);
       if (annotation == null)
         continue;
       final String unitName = annotation.unitName();
       if (unitName.isEmpty()) {
         System.err.println("Field [" + field.getName() + "] annotated with PersistenceContext must specify unitName");
         faultFound = true;
       }
 
       annotatedFields.add(field);
       System.out.println("Field [" + field.getName() + "] requests PU [" + unitName + "]");
 
     }
 
     if (faultFound)
       throw new RuntimeException("PersistenceContext annotated field(s) found with errors");
 
     return annotatedFields;
   }
 
   private ArrayList<EntityManager> createAndInjectEntityManagers(ArrayList<Field> fields) {
     ArrayList<EntityManager> managers = new ArrayList<EntityManager>();
     for (Field field : fields) {
       try {
         PersistenceContext annotation = field.getAnnotation(PersistenceContext.class);
         EntityManager entityManager = Persistence.createEntityManagerFactory(annotation.unitName()).createEntityManager();
         boolean accessible = field.isAccessible();
         field.setAccessible(true);
         field.set(testClass, entityManager);
         field.setAccessible(accessible);
        managers.add(entityManager);
       } catch (Exception ex) {
         throw new RuntimeException(ex);
       }
     }
 
     return managers;
   }
 
 }
