 /**
  * Copyright (C) 2013 Colorado School of Mines
  *
  * This file is part of the Interface Software Development Kit (SDK).
  *
  * The InterfaceSDK is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The InterfaceSDK is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the InterfaceSDK.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.mines.acmX.exhibit.module_management.modules;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 import edu.mines.acmX.exhibit.input_services.hardware.BadDeviceFunctionalityRequestException;
 import edu.mines.acmX.exhibit.input_services.hardware.HardwareManagerManifestException;
 import edu.mines.acmX.exhibit.module_management.ModuleManager;
 import edu.mines.acmX.exhibit.module_management.loaders.ManifestLoadException;
 import edu.mines.acmX.exhibit.module_management.loaders.ModuleLoadException;
 import edu.mines.acmX.exhibit.module_management.metas.DependencyType;
 import edu.mines.acmX.exhibit.module_management.metas.ModuleMetaData;
 import edu.mines.acmX.exhibit.module_management.modules.implementation.ModuleHelper;
 
 /**
  * Unit test for ModuleHelper.
  * 
  */
 public class ModuleHelperTest {
 
 	/**
 	 * All this tests is that ModuleHelper can create a new singleton instance
 	 * of ModuleManager and call the setNextModule function on it.
 	 * 
 	 * @throws ModuleLoadException
 	 * @throws ManifestLoadException
 	 * @throws BadDeviceFunctionalityRequestException 
 	 * @throws HardwareManagerManifestException 
 	 */
 	@Test
 	public void testNextValidModule() throws ManifestLoadException,
 			ModuleLoadException, HardwareManagerManifestException, BadDeviceFunctionalityRequestException {
 		ModuleManager.createEmptyInstance();
 		ModuleManager m = ModuleManager.getInstance();
 		m.createHardwareInstance();
 		ModuleMetaData before = new ModuleMetaData(null, null, null, null, null, null, null, null, null, null, true);
 		m.setCurrentModuleMetaData(before);
 		Map<String, ModuleMetaData> meta = new HashMap<String, ModuleMetaData>();
 		String nextToLoad = "should.change.to.this";
 		ModuleMetaData garblygook = new ModuleMetaData(nextToLoad, nextToLoad,
 				nextToLoad, nextToLoad, nextToLoad, nextToLoad, nextToLoad,
 				nextToLoad, new HashMap<String, DependencyType>(), new HashMap<String, DependencyType>(), false);
 		meta.put(nextToLoad, garblygook);
 		m.setModuleMetaDataMap(meta);
 
 
 		ModuleHelper mod = new ModuleHelper();
		assertTrue(mod.setNextModule(nextToLoad));
 		assertEquals(nextToLoad, m.getNextModuleName());
 	}
 
 }
