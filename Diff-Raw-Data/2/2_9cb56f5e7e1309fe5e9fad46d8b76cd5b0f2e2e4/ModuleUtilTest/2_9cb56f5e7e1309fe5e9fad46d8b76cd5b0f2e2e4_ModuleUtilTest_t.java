 package afc.ant.modular;
 
 import junit.framework.TestCase;
 
 public class ModuleUtilTest extends TestCase
 {
     public void testIsModule_NullValue()
     {
         assertFalse(ModuleUtil.isModule(null));
     }
     
     public void testIsModule_NotModule()
     {
         assertFalse(ModuleUtil.isModule(new Object()));
     }
     
     public void testIsModule_ModuleInfo()
     {
        assertFalse(ModuleUtil.isModule(new ModuleInfo("foo")));
     }
     
     // TODO add test isModule_Module_DifferentClassLoader()
     public void testIsModule_Module()
     {
         assertTrue(ModuleUtil.isModule(new Module("foo")));
     }
 }
