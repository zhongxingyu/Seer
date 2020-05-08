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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import net.contextfw.web.application.lifecycle.AfterBuild;
 import net.contextfw.web.application.lifecycle.BeforeBuild;
 
 import org.junit.Test;
 
 public class ComponentBuilderTest extends BaseComponentTest {
 
     public static class Aa extends Component {
 
         public String order = "";
         
         @Attribute
         public String foo = "bar";
         
         @Element 
         public String bar="foo";
         
         @Element(wrap=false)
         public FieldEmbed fieldEmbedded = new FieldEmbed();
         
         @Element(wrap=false)
         public MethodEmbed methodEmbedded() {
             return new MethodEmbed();
         }
         
         @Element
         public List<MethodEmbed> listOfEmbeddeds() {
             List<MethodEmbed> embeds = new ArrayList<MethodEmbed>();
             embeds.add(new MethodEmbed());
             embeds.add(new MethodEmbed());
             return embeds;
         }
         
         @Element
         public MethodEmbed[] arrayOfEmbeddeds() {
             return new MethodEmbed[] { new MethodEmbed(), new MethodEmbed() };
         }
         
         @CustomBuild
         public void custom(DOMBuilder b) {
             order = order + ".custom";
             b.descend("barFoo").attr("fooBar", true);
         }
         
         @CustomBuild(wrap=false)
         public void custom2(DOMBuilder b) {
             b.descend("barFoo1").attr("fooBar1", true);
         }
         
         @BeforeBuild
         public void before() {
             order = order + "before";
         }
         
         @AfterBuild
         public void after() {
             order = order + ".after";
         }
         
         @ScriptElement
         public Script init() {
         	return new FunctionCall("init", "a");
         }
         
         @ScriptElement
         public Collection<Script> listOfInits() {
             List<Script> inits = new ArrayList<Script>();
             inits.add(new FunctionCall("init", "a"));
             inits.add(new FunctionCall("init", "a"));
             return inits;
         }
         
         @ScriptElement
         public Script[] arrayOfInits() {
             return new Script[] {
               new FunctionCall("init", "a", "b"),
               new FunctionCall("init", "a") };
         }
 
         @ScriptElement
         public Script init2 = new FunctionCall("init2", "a");
     }
     
     public static class Bee extends Component {
         
         @Element
         private Aa comp1;
         
         @Element
         private Aa hidden;
         
         public Bee(Aa comp1) {
             this.comp1 = this.registerChild(comp1);
             this.hidden = this.registerChild(new Aa());
             this.hidden.setEnabled(false);
         }
         
         @CustomBuild
         public void custom(DOMBuilder b) {
             b.descend("anotherInner").child(comp1);
         }
     }
     
     public static class Cee extends Component {
         @Element
         public Aa aa1;
         @Element
         public Aa aa2;
         @Element
         public Ee ee = new Ee();
         
         @Attribute
         public String nullAttribute = null;
         
         @Element
         public Dee dee = new Dee();
         
         @Element
         public Faa faa = new Faa();
     }
     
     @Buildable
     public static class Dee {
         @Attribute
         public String foo = "bar";
         @Element
         public Aa aa;
     }
     
     @Buildable 
     public static class FieldEmbed {
         
     }
     
     @Buildable 
     public static class MethodEmbed {
         
     }
     
     public static class Ee {
         public String toString() {
             return "Ee.toString()";
         }
     }
     
     @Buildable(wrap=false)
     public static class Faa {
         @Attribute
         public String foo = "bar";
     }
     
     @Buildable(name="Eeg")
     public static class Gee extends Component {
         @Attribute
         public String foo = "bar";
     }
     
     @Test
     public void testName() {
         Gee comp = new Gee();
         webApplicationComponent.registerChild(comp);
        assertEquals("c1", comp.getId());
         webApplicationComponent.buildChild(domBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Eeg").exists();
     }
     
     @Test
     public void testId() {
         Aa comp = new Aa();
         assertNull(comp.getId());
         webApplicationComponent.registerChild(comp);
        assertEquals("c1", comp.getId());
         webApplicationComponent.buildChild(domBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Aa/FieldEmbed").exists();
         assertDom("//WebApplication/Aa/MethodEmbed").exists();
         assertDom("//WebApplication/Aa/Script[1]").hasText("init2(\"a\");\n");
         assertDom("//WebApplication/Aa/Script[2]").hasText("init(\"a\");\n");
        assertDom("//WebApplication/Aa").hasAttribute("id", "c1");
         assertDom("//WebApplication/Aa/custom/barFoo").hasAttribute("fooBar", "true");
         assertDom("//WebApplication/Aa/barFoo1").hasAttribute("fooBar1", "true");
         assertDom("//WebApplication/Aa/listOfEmbeddeds//MethodEmbed").exists();
         assertDom("//WebApplication/Aa/arrayOfEmbeddeds//MethodEmbed").exists();
         assertEquals("before.custom.after", comp.order);
     }
     
     @Test
     public void testInnerComp() {
         Bee comp = new Bee(new Aa());
         webApplicationComponent.registerChild(comp);
         webApplicationComponent.buildChild(domBuilder);
         logXML(domBuilder);
        assertDom("//WebApplication/Bee/comp1/Aa").attributeStartsWith("id", "c");
         assertDom("//WebApplication/Bee/hidden/Aa").notExists();
     }
     
     @Test
     public void testEmptyUpdate() {
         Aa comp = new Aa();
         assertNull(comp.getId());
         webApplicationComponent.registerChild(comp);
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Aa").notExists();
     }
     
     @Test
     public void testAaUpdate() {
         Aa comp = new Aa();
         webApplicationComponent.registerChild(comp);
         comp.refresh();
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Aa.update").exists();
         assertDom("//WebApplication/Aa.update/Script[1]").hasText("init2(\"a\");\n");
         assertDom("//WebApplication/Aa.update/Script[2]").hasText("init(\"a\");\n");
     }
     
     @Test
     public void testPartialAaUpdate() {
         Aa comp = new Aa();
         webApplicationComponent.registerChild(comp);
         comp.partialRefresh("scriptUpdate", "init");
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Aa.scriptUpdate").exists();
         assertDom("//WebApplication/Aa.scriptUpdate/Script[1]").hasText("init(\"a\");\n");
     }
     
     @Test
     public void testBeeUpdate() {
         Bee comp = new Bee(new Aa());
         webApplicationComponent.registerChild(comp);
         logXML(domBuilder);
         comp.refresh();
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Bee.update").exists();
         assertDom("//WebApplication/Bee.update/comp1/Aa").exists();
     }
     
     @Test
     public void testBee_Aa_Update() {
         Aa aa = new Aa();
         Bee comp = new Bee(aa);
         webApplicationComponent.registerChild(comp);
         aa.refresh();
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Bee").notExists();
         assertDom("//WebApplication/Bee.update").notExists();
         assertDom("//WebApplication//Aa.update").exists();
     }
     
     @Test 
     public void testCee() {
         Cee cee = new Cee();
         Aa aa = new Aa();
         cee.dee.aa = aa;
         webApplicationComponent.registerChild(cee);
         webApplicationComponent.buildChild(domBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Cee/ee/text()").hasText("Ee.toString()");
         assertDom("//WebApplication/Cee").hasNoAttribute("nullAttribute");
         assertDom("//WebApplication/Cee/dee/Dee").exists();
         assertDom("//WebApplication/Cee/dee/Dee/aa/Aa/bar/text()").hasText("foo");
         assertDom("//WebApplication/Cee/faa").hasAttribute("foo", "bar");
     }
     
     @Test 
     public void testCeeUpdate() {
         Cee cee = new Cee();
         cee.aa1 = cee.registerChild(new Aa());
         cee.aa2 = cee.registerChild(new Aa());
         webApplicationComponent.registerChild(cee);
         cee.aa1.refresh();
         cee.aa2.refresh();
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication//Aa.update[1]").exists();
         assertDom("//WebApplication//Aa.update[2]").exists();
     }
     
     @Test 
     public void testCeePartialUpdate() {
         Cee cee = new Cee();
         cee.aa1 = cee.registerChild(new Aa());
         cee.aa2 = cee.registerChild(new Aa());
         webApplicationComponent.registerChild(cee);
         cee.partialRefresh("aa1Update", "aa1");
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
        assertDom("//WebApplication/Cee.aa1Update").hasAttribute("id", "c1");
         assertDom("//WebApplication/Cee.aa1Update/aa1/Aa").exists();
         assertDom("//WebApplication/Cee.aa1Update/aa2/Aa").notExists();
     }
     
     @Test 
     public void testCeePartialUpdate2() {
         Cee cee = new Cee();
         cee.aa1 = cee.registerChild(new Aa());
         cee.aa2 = cee.registerChild(new Aa());
         webApplicationComponent.registerChild(cee);
         cee.partialRefresh("aa1Update", "aa1");
         cee.aa2.refresh();
         webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
         logXML(domBuilder);
        assertDom("//WebApplication/Cee.aa1Update").hasAttribute("id", "c1");
         assertDom("//WebApplication/Cee.aa1Update/aa1/Aa").exists();
         assertDom("//WebApplication/Cee.aa1Update/aa2/Aa").notExists();
         assertDom("//WebApplication/Aa.update").exists();
     }
 }
