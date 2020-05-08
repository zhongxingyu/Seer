 /*
  * Copyright (C) 2005 Iulian-Corneliu Costan
  * 
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package wicket.contrib.tinymce;
 
 import junit.framework.TestCase;
 import wicket.contrib.tinymce.TinyMCEPanel;
 import wicket.contrib.tinymce.settings.NoneditablePlugin;
 import wicket.contrib.tinymce.settings.Plugin;
 import wicket.contrib.tinymce.settings.TinyMCESettings;
 import wicket.markup.html.WebComponent;
 import wicket.markup.html.panel.Panel;
 import wicket.markup.html.resources.JavaScriptReference;
 import wicket.util.tester.TestPanelSource;
 import wicket.util.tester.WicketTester;
 
 /**
  * Tests of the TinyMCE panel.
  * 
  * @author Frank Bille Jensen (fbille@avaleo.net)
  */
 public class TinyMCEPanelTest extends TestCase
 {
 	WicketTester application;
 
 	/**
 	 * For each test case we provide a new WicketTester.
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	public void setUp()
 	{
 		application = new WicketTester();
 	}
 
 	/**
 	 * Test if basic rendering of this panel works.
 	 */
 	public void testRender()
 	{
 		application.startPanel(new TestPanelSource()
 		{
 			public Panel getTestPanel(String panelId)
 			{
 				TinyMCESettings settings = new TinyMCESettings();
 
 				return new TinyMCEPanel("panel", settings);
 			}
 		});
 
 		assertCommonComponents();
 
 		application.assertContains("mode : \"textareas\"");
 		application.assertContains("theme : \"simple\"");
 	}
 
 	/**
 	 * Test that the correct output is rendered when using the advanced theme.
 	 */
 	public void testRenderAdvanced()
 	{
 		application.startPanel(new TestPanelSource()
 		{
 			public Panel getTestPanel(String panelId)
 			{
 				TinyMCESettings settings = new TinyMCESettings(TinyMCESettings.Theme.advanced);
 				settings.register(new NoneditablePlugin());
 
 				return new TinyMCEPanel("panel", settings);
 			}
 		});
 
 		assertCommonComponents();
 
 		application.assertContains("mode : \"textareas\"");
 		application.assertContains("theme : \"advanced\"");
 		application.assertContains("plugins : \"noneditable\"");
 	}
 
 	/**
 	 * Ensure that the correct javascript is written, to load the plugins
 	 * needed.
 	 */
 	public void testRenderWithExternalPlugins()
 	{
 		// Define a mock plugin
 		final Plugin mockPlugin = new Plugin("mockplugin", "the/path/to/the/plugin")
 		{
 			private static final long serialVersionUID = 1L;
 		};
 
 		// Add the panel.
 		application.startPanel(new TestPanelSource()
 		{
 			public Panel getTestPanel(String panelId)
 			{
 				TinyMCESettings settings = new TinyMCESettings(TinyMCESettings.Theme.advanced);
 				settings.register(mockPlugin);
 
 				return new TinyMCEPanel("panel", settings);
 			}
 		});
 
 		assertCommonComponents();
 
 		application.assertContains("plugins : \"mockplugin\"");
 		application.assertContains("tinyMCE\\.loadPlugin\\('" + mockPlugin.getName() + "','"
 				+ mockPlugin.getPluginPath() + "'\\);");
 	}
 
 	/**
 	 * Ensure that the plugins additional javascript is actually rendered.
 	 * 
 	 */
 	public void testAdditionalPluginJavaScript()
 	{
 		// Define a mock plugin
 		final Plugin mockPlugin = new Plugin("mockplugin")
 		{
 			private static final long serialVersionUID = 1L;
 			
			protected void definePluginExtensions(StringBuffer buffer)
 			{
 				buffer.append("alert('Hello Mock World');");
 			}
 		};
 
 		// Add the panel.
 		application.startPanel(new TestPanelSource()
 		{
 			public Panel getTestPanel(String panelId)
 			{
 				TinyMCESettings settings = new TinyMCESettings();
 				settings.register(mockPlugin);
 
 				return new TinyMCEPanel("panel", settings);
 			}
 		});
 
 		assertCommonComponents();
 		
 		application.assertContains("tinyMCE.init\\(\\{[^\\}]+\\}\\);\nalert\\('Hello Mock World'\\);");
 	}
 
 	private void assertCommonComponents()
 	{
 		application.assertComponent("panel", TinyMCEPanel.class);
 		application.assertComponent("panel:tinymce", JavaScriptReference.class);
 		application.assertComponent("panel:initScript", WebComponent.class);
 
 		application.assertContains("tinyMCE\\.init\\(\\{");
 		application.assertContains("\\}\\);");
 	}
 }
