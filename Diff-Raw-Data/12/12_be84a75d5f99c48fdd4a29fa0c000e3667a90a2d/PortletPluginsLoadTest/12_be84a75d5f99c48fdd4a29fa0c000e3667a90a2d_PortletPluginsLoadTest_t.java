 package org.jboss.tools.portlet.core.test;
 
import org.eclipse.core.runtime.Platform;
 import org.jboss.tools.tests.AbstractPluginsLoadTest;
 
 public class PortletPluginsLoadTest extends AbstractPluginsLoadTest {
 	
	public void testOrgJbossPortletCorePluginIsActivated() {
		assertPluginResolved(Platform.getBundle("org.jboss.tools.portlet.core"));
 	}

	public void testOrgJbossPortletUiPluginIsActivated() {
		assertPluginResolved(Platform.getBundle("org.jboss.tools.portlet.ui"));
	}

 }
