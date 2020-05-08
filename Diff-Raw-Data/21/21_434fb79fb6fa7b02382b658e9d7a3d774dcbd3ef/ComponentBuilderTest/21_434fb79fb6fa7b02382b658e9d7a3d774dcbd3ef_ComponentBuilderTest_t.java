 package net.contextfw.web.application.component;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
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
         	return new ComponentFunction(this, "init", "a");
         }
 
         @ScriptElement
         private Script init2 = new ComponentFunction(this, "init2", "a");
     }
     
     public static class Bee extends Component {
         
         @Element
         private Aa comp1;
         
         public Bee(Aa comp1) {
             this.comp1 = this.registerChild(comp1);
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
     
     @Test
     public void testId() {
         Aa comp = new Aa();
         assertNull(comp.getId());
         webApplicationComponent.registerChild(comp);
         assertEquals("el1", comp.getId());
         webApplicationComponent.buildChild(domBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Aa/FieldEmbed").exists();
         assertDom("//WebApplication/Aa/MethodEmbed").exists();
         assertDom("//WebApplication/Aa/Script[1]").hasText("Aa(\"null\").init2(\"a\");\n");
         assertDom("//WebApplication/Aa/Script[2]").hasText("Aa(\"el1\").init(\"a\");\n");
         assertDom("//WebApplication/Aa").hasAttribute("id", "el1");
         assertDom("//WebApplication/Aa/custom/barFoo").hasAttribute("fooBar", "true");
         assertDom("//WebApplication/Aa/barFoo1").hasAttribute("fooBar1", "true");
         assertEquals("before.custom.after", comp.order);
     }
     
     @Test
     public void testInnerComp() {
         Bee comp = new Bee(new Aa());
         webApplicationComponent.registerChild(comp);
         webApplicationComponent.buildChild(domBuilder);
         logXML(domBuilder);
         assertDom("//WebApplication/Bee/comp1/Aa").hasAttribute("id", "el2");
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
         assertDom("//WebApplication/Aa.update/Script[1]").hasText("Aa(\"null\").init2(\"a\");\n");
         assertDom("//WebApplication/Aa.update/Script[2]").hasText("Aa(\"el1\").init(\"a\");\n");
     }
     
     @Test
    public void testPartialAaUpdate() {
        Aa comp = new Aa();
        webApplicationComponent.registerChild(comp);
        comp.partialRefresh("scriptUpdate", "init");
        webApplicationComponent.buildChildUpdate(domBuilder, componentBuilder);
        logXML(domBuilder);
        assertDom("//WebApplication/Aa.scriptUpdate").exists();
        assertDom("//WebApplication/Aa.scriptUpdate/Script[1]").hasText("Aa(\"el1\").init(\"a\");\n");
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
         assertDom("//WebApplication/Cee.aa1Update").hasAttribute("id", "el1");
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
         assertDom("//WebApplication/Cee.aa1Update").hasAttribute("id", "el1");
         assertDom("//WebApplication/Cee.aa1Update/aa1/Aa").exists();
         assertDom("//WebApplication/Cee.aa1Update/aa2/Aa").notExists();
         assertDom("//WebApplication/Aa.update").exists();
     }
 }
