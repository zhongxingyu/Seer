 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.grapher;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import de.cosmocode.palava.core.Framework;
 import de.cosmocode.palava.core.Palava;
 
 
 /**
  * Tests {@link DefaultGrapherService}.
  *
  * @author Willi Schoenborn
  */
 public final class GrapherServiceTest {
 
     /**
      * Tests {@link DefaultGrapherService#execute()}.
      * 
      * @throws IOException should not happen 
      */
     @Test
     public void execute() throws IOException {
         final File file = new File("graph.dot");
        file.delete();
         Assert.assertFalse(file.exists());
         final Framework framework = Palava.newFramework();
         framework.start();
         framework.getInstance(GrapherService.class).execute();
         framework.stop();
         Assert.assertTrue(file.exists());
         Assert.assertTrue(file.length() > 0);
     }
 
 }
