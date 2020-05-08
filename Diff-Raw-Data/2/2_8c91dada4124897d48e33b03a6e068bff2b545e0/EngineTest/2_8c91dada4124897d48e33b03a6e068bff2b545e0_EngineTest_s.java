 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
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
 package net.oneandone.jasmin.model;
 
 import net.oneandone.jasmin.descriptor.Base;
 import net.oneandone.sushi.fs.World;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 
 public class EngineTest {
     private Engine engine;
 
     @Before
     public void before() throws IOException {
         World world;
         Resolver resolver;
 
         world = new World();
        resolver = new Resolver(world, true);
         resolver.add(Base.CLASSPATH, world.guessProjectHome(EngineTest.class).join("src/test/resources"));
         engine = new Engine(Repository.load(resolver));
     }
 
     @Test
     public void normal() throws IOException {
         assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n",
                 engine.request("foo/js/lead"));
     }
 
     @Test
     public void twoFilesNormal() throws IOException {
         assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n" + "\n"
                 + "//###\n" + "var special = 0;\n", engine.request("two/js/lead"));
     }
 
     @Test
     public void variantSpecial() throws IOException {
         assertEq("//###\n" + "var special = 0;\n", engine.request("foo/js/special"));
     }
 
     @Test
     public void variantLead() throws IOException {
         assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n",
                 engine.request("foo/js/lead"));
     }
 
     @Test
     public void variantUnknown() throws IOException {
         assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n",
                 engine.request("foo/js/unknown"));
     }
 
     @Test
     public void min() throws IOException {
         assertEquals("var str=\"äöü\";var a=0;var b=2;", engine.request("foo/js-min/lead"));
     }
 
     private void assertEq(String expected, String found) {
         assertEquals(expected, found.replaceAll("//###.*\n", "//###\n"));
     }
 }
