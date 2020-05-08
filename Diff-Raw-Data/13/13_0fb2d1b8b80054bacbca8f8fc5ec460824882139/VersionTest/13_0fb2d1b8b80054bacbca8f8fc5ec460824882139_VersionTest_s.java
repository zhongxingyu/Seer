 /*
  * Copyright 2011 Olivier Lagache
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
 
 package com.github.dbourdette.glass.configuration;
 
import junit.framework.TestCase;
 import org.junit.Assert;
import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
import javax.inject.Inject;
import java.text.SimpleDateFormat;
 
 
 /**
  * @author Olivier Lagache
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"/com/github/dbourdette/glass/spring-context.xml"})
 
 public class VersionTest extends TestCase {
 
     private static final String COMPILATION_DATE_FORMAT = "yyyyMMdd_HHmmss";
 
     @Inject
     private Version version;
 

     @Test
     public void testGetCompilationDate() throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat(COMPILATION_DATE_FORMAT);
         String compilationDateAsString = sdf.format(version.getCompilationDate());
 
         Assert.assertEquals("20110914_215541", compilationDateAsString);
     }
 
     @Test
     public void testGetApplicationVersion() throws Exception {
         Assert.assertEquals("0.99", version.getApplicationVersion());
 
     }
 }
