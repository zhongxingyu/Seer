 /*
  * Copyright 2000-2010 JetBrains s.r.o.
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
 
 package jetbrains.buildserver.metarunner;
 
 import jetbrains.buildServer.BaseTestCase;
 import jetbrains.buildServer.util.FileUtil;
 import jetbrains.buildserver.metarunner.xml.*;
 import org.testng.Assert;
 import org.testng.annotations.Test;
import java.util.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 /**
  * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
  *         08.12.10 19:43
  */
 public class ModelReaderTest extends BaseTestCase {

  @Test
  public void test_02() throws IOException {
    File f = createTempFile();
    FileUtil.copyResource(this.getClass(), "/meta-runner-02.xml", f);

    final RunnerSpec parse = new MetaRunnerSpecParser().parse(f);

    List<String> runners = new ArrayList<String>();
    for (RunnerStepSpec spec : parse.runners()) {
      runners.add(spec.runType());
    }
    List<String> params = new ArrayList<String>();
    for (ParameterDef d : parse.parameterDefs()) {
      params.add(d.key());
    }

    Assert.assertEquals(Arrays.asList("Ant1", "Ant2"), runners);
    Assert.assertEquals(Arrays.asList("key1", "key2"), params);
  }

   @Test
   public void test_01() throws IOException {
     File f = createTempFile();
     FileUtil.copyResource(this.getClass(), "/meta-runner-01.xml", f);
 
     final RunnerSpec parse = new MetaRunnerSpecParser().parse(f);
 
     Assert.assertEquals(parse.description(), "ZZZ");
     Assert.assertEquals(parse.shortName(), "meta");
     Assert.assertEquals(parse.runType(), "meta-001");
 
     System.out.println("parse.parameterDefs() = " + parse.parameterDefs());
     Assert.assertEquals(parse.parameterDefs().size(), 1);
     final ParameterDef pd = parse.parameterDefs().iterator().next();
     Assert.assertEquals(pd.defaultValue(), "555");
     Assert.assertEquals(pd.description(), "edewedasd");
     Assert.assertEquals(pd.key(), "keyt");
 //    Assert.assertEquals(((TextType) pd.parameterType()).useTextArea, false);
     Assert.assertEquals(pd.shortName(), "aaa");
 
     System.out.println("parse.runners() = " + parse.runners());
     Assert.assertEquals(parse.runners().size(), 1);
     final RunnerStepSpec r = parse.runners().iterator().next();
 
     Assert.assertEquals(r.runType(), "Ant");
     Assert.assertEquals(r.parameters().size(), 2);
     final Iterator<? extends RunnerStepParams> pit = r.parameters().iterator();
     final RunnerStepParams p1 = pit.next();
     final RunnerStepParams p2 = pit.next();
 
     Assert.assertNotNull(p1);
     Assert.assertNotNull(p2);
   }
 
 
   //TODO: add tests for broken config files.
 }
