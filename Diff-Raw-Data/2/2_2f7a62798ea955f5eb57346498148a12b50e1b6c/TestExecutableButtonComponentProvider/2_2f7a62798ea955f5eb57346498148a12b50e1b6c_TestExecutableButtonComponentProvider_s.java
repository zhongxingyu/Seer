 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.jsc.mct.executables.buttons;
 
 import gov.nasa.arc.mct.gui.MenuItemInfo;
 import gov.nasa.arc.mct.platform.spi.Platform;
 import gov.nasa.arc.mct.platform.spi.PlatformAccess;
 import gov.nasa.arc.mct.platform.spi.RoleService;
 import gov.nasa.arc.mct.services.internal.component.User;
 import gov.nasa.jsc.mct.executable.buttons.ExecutableButtonComponent;
 import gov.nasa.jsc.mct.executable.buttons.ExecutableButtonComponentProvider;
 import gov.nasa.jsc.mct.executables.buttons.actions.ExecutableButtonAction;
 
 import java.util.Collection;
 
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 public class TestExecutableButtonComponentProvider {
 	private ExecutableButtonComponentProvider provider;
 	
 	@Mock RoleService roleService;
 	@Mock User user;
 	@Mock Platform mockPlatform;
 	
 	@BeforeMethod
 	public void testSetup() {
 		provider = new ExecutableButtonComponentProvider();
 		MockitoAnnotations.initMocks(this);
 		(new PlatformAccess()).setPlatform(mockPlatform);
 		Mockito.when(mockPlatform.getCurrentUser()).thenReturn(user);
 	}
 	
 	@AfterMethod
 	public void tearDown() {
 	}
 	
 	@Test
 	public void testComponentTypes() {
 		Mockito.when(roleService.hasRole(Mockito.any(User.class), Mockito.anyString())).thenReturn(false);
 		Assert.assertEquals(provider.getComponentTypes().iterator().next().getComponentClass(), ExecutableButtonComponent.class);
		Assert.assertFalse(provider.getComponentTypes().iterator().next().isCreatable());
 		Assert.assertEquals(provider.getComponentTypes().size(), 1);
 	}
 	
 	@Test
 	public void testComponentCreation() {
 		Mockito.when(roleService.hasRole(Mockito.any(User.class), Mockito.eq("GA"))).thenReturn(true);
 		Mockito.when(roleService.hasRole(Mockito.any(User.class), Mockito.eq("DTR"))).thenReturn(true);
 		Assert.assertEquals(provider.getComponentTypes().iterator().next().getComponentClass(), ExecutableButtonComponent.class);
 		Assert.assertTrue(provider.getComponentTypes().iterator().next().isCreatable());
 		Assert.assertEquals(provider.getComponentTypes().size(), 1);
 	}
 	
 	@Test
 	public void testViews() {
 		Assert.assertEquals(provider.getViews(ExecutableButtonComponent.class.getName()).size(), 2);
 		Assert.assertTrue(provider.getViews("abc").isEmpty());
 	}
 	
 	@Test
 	public void testMenuItemInfos() {
 		Collection<MenuItemInfo> menuItems = provider.getMenuItemInfos();
 		Assert.assertEquals(menuItems.size(), 2);
 		Assert.assertEquals(menuItems.iterator().next().getActionClass(), ExecutableButtonAction.class);
 	}
 	
 	@Test
 	public void testPolicyInfos() {
 		Assert.assertTrue(provider.getPolicyInfos().size() > 0);
 	}
 }
