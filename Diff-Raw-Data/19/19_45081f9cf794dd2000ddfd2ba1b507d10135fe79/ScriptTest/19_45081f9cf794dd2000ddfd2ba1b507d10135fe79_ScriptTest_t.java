 /**
  * Copyright 2010 Marko Lavikainen
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
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.contextfw.web.application.component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.google.gson.Gson;
 
 public class ScriptTest extends BaseComponentTest {
 
     private Gson gson = new Gson();
 
     private Script getScript(final String script, final Object... args) {
         return new Script() {
             public String getScript(ScriptContext scriptContext) {
                 return script;
             }
 
             public Object[] getArguments(ScriptContext scriptContext) {
                 return args;
             }
         };
     }
 
     @Test
     public void test1() {
         Script script = getScript("foo()", (Object[]) null);
         script.build(domBuilder.descend("Script"), gson, scriptContext);
         assertDom("//WebApplication/Script").hasText("foo()");
     }
 
     @Test
     public void test2() {
         Script script = getScript("foo({0})", new Object[] { true });
         script.build(domBuilder.descend("Script"), gson, scriptContext);
         assertDom("//WebApplication/Script").hasText("foo(true)");
     }
 
     @Test
     public void test3() {
         List<String> strs = new ArrayList<String>();
         strs.add("1");
         strs.add("2");
         Script script = getScript("foo({0}, {1})", new Object[] { strs, "3" });
         script.build(domBuilder.descend("Script"), gson, scriptContext);
         assertDom("//WebApplication/Script").hasText("foo([\"1\",\"2\"], \"3\")");
     }
 
     @Test
     public void test4() {
         List<String> strs = new ArrayList<String>();
         strs.add("1");
         strs.add("2");
         FunctionCall script = new FunctionCall("foo", strs, "3");
         script.build(domBuilder.descend("Script"), gson, scriptContext);
         assertDom("//WebApplication/Script").hasText("foo([\"1\",\"2\"],\"3\");\n");
     }
 
     @Test
     public void test5() {
         List<String> strs = new ArrayList<String>();
         strs.add("1");
         strs.add("2");
         FunctionCall script = new FunctionCall("foo");
         script.build(domBuilder.descend("Script"), gson, scriptContext);
         assertDom("//WebApplication/Script").hasText("foo();\n");
     }
    
    @Test
    public void test6() {
        List<String> strs = new ArrayList<String>();
        strs.add("1");
        strs.add("2");
        FunctionCall script = new FunctionCall("foo", 4, null);
        script.build(domBuilder.descend("Script"), gson, scriptContext);
        assertDom("//WebApplication/Script").hasText("foo(4,null);\n");
    }
 
     public static class A extends Component {
     }
 }
