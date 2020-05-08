 /*
  * Copyright 2012 EMBL - European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.fg.annotare2.magetab.checks.idf;
 
 import uk.ac.ebi.fg.annotare2.magetab.checker.GlobalCheck;
 import uk.ac.ebi.fg.annotare2.magetab.checker.MageTabCheck;
 import uk.ac.ebi.fg.annotare2.magetab.model.idf.Person;
 import uk.ac.ebi.fg.annotare2.magetab.model.idf.TermList;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 
 /**
  * @author Olga Melnichuk
  */
 @MageTabCheck("At least one contact must have a role specified")
 public class AtLeastOneContactWithRolesRequired implements GlobalCheck<Person> {
 
     private int roleCount;
 
     @Override
     public void visit(Person person) {
         TermList roles = person.getRoles();
        if (roles != null && roles.isEmpty()) {
             roleCount++;
         }
     }
 
     @Override
     public void check() {
         assertThat(roleCount, greaterThan(0));
     }
 }
