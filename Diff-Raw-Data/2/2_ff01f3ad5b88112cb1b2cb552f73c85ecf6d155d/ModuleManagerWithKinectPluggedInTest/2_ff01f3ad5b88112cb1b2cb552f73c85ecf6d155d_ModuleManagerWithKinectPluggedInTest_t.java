 package edu.mines.acmX.exhibit.module_management;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.print.DocFlavor.URL;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.mines.acmX.exhibit.input_services.hardware.BadDeviceFunctionalityRequestException;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManager;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
 import edu.mines.acmX.exhibit.module_management.loaders.ManifestLoadException;
 import edu.mines.acmX.exhibit.module_management.loaders.ModuleLoadException;
 import edu.mines.acmX.exhibit.module_management.metas.DependencyType;
 import edu.mines.acmX.exhibit.module_management.metas.ModuleManagerMetaData;
 import edu.mines.acmX.exhibit.module_management.metas.ModuleMetaData;
 import edu.mines.acmX.exhibit.module_management.metas.ModuleMetaDataBuilder;
 import edu.mines.acmX.exhibit.module_management.modules.CommandlineModule;
 import edu.mines.acmX.exhibit.module_management.modules.ModuleInterface;
 
 /**
  * Unit test for ModuleManager
  */
 public class ModuleManagerWithKinectPluggedInTest {
 
 	@Before
 	public void resetModuleManager() {
 		ModuleManager.removeInstance();
 		ModuleManager.createEmptyInstance();
 	}
 
 	/**
 	 * Test that module manager loads when hardware is there and setup correctly
 	 * @throws ManifestLoadException 
 	 * @throws BadDeviceFunctionalityRequestException 
 	 * @throws HardwareManagerManifestException 
 	 * @throws ModuleLoadException 
 	 * @throws SecurityException 
 	 * @throws NoSuchFieldException 
 	 * @throws IllegalAccessException 
 	 * @throws IllegalArgumentException 
 	 * @throws NoSuchMethodException 
 	 * @throws InvocationTargetException 
 	 */
 	@Test
 	public void testNoRevertAndOkayOnGoodDriverRequest() throws ManifestLoadException, ModuleLoadException, HardwareManagerManifestException, BadDeviceFunctionalityRequestException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
 		ModuleManager.removeInstance();
 		ModuleManager.configure("src/test/resources/module_manager/BadHardwareRequestModuleManagerManifest.xml");
 		ModuleManager m = ModuleManager.getInstance();
 		
 		// The next three lines are to give the hardware manager support
 		Map<String, String> configStore = new HashMap<String, String>();
		configStore.put("kinectopenni", "src/test/resources/openni_config.xml");
 		HardwareManager.getInstance().setConfigurationFileStore(configStore);
 		
 		// pretend the next module was set (to skip that logic)
 		// default = false
 		Field loadDefault = ModuleManager.class.getDeclaredField("loadDefault");
 		loadDefault.setAccessible(true);
 		loadDefault.set(m, false);
 		// set the nextModuleMetaData
 		// TODO change this to instead use a generated ModuleMetaData so we can skip some of the logic for ModuleManager
 		ModuleMetaData badMetaData = m.getModuleMetaDataMap().get("edu.mines.ademaria.goodmodules.goodrequireddriver");
 		Field nextModuleMeta = ModuleManager.class.getDeclaredField("nextModuleMetaData");
 		nextModuleMeta.setAccessible(true);
 		nextModuleMeta.set(m, badMetaData);
 		
 		// call the setup function
 		Method setupDefaultRuntime = ModuleManager.class.getDeclaredMethod("setupPreRuntime");
 		setupDefaultRuntime.setAccessible(true);
 		setupDefaultRuntime.invoke(m);
 		
 		// check that the module was reverted to default
 		Field currentMeta = ModuleManager.class.getDeclaredField("currentModuleMetaData");
 		currentMeta.setAccessible(true);
 		ModuleMetaData actual = (ModuleMetaData) currentMeta.get(m);
 		assertEquals("edu.mines.ademaria.goodmodules.goodrequireddriver", actual.getPackageName());
 		
 	}
  	
  	@Test
 	public void testDefaultCheckPermissionsPassForRuntime()
 			throws ManifestLoadException, ModuleLoadException,
 			HardwareManagerManifestException,
 			BadDeviceFunctionalityRequestException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
 		
 		ModuleManager.removeInstance();
 		ModuleManager.configure("src/test/resources/module_manager/ExampleModuleManagerManifest.xml");
 		ModuleManager m = ModuleManager.getInstance();
 		
 		m.setDefault(true);
 		
 		ModuleMetaDataBuilder builder = new ModuleMetaDataBuilder();
 		builder.addInputType("rgbimage", DependencyType.REQUIRED);
 		builder.setPackageName("com.austindiviness.cltest");
 		builder.setClassName("Launch");
 		
 		ModuleMetaData mmd = builder.build();
 		mmd.setJarFileName("cltest.jar");
 		m.setDefaultModuleMetaData(mmd);
 		
 		Method preDefaultRT = ModuleManager.class.getDeclaredMethod("setupPreRuntime");
 		preDefaultRT.setAccessible(true);
 		preDefaultRT.invoke(m);
 	}
 }	
