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
 
 import java.util.regex.Pattern;
 
 import wicket.Application;
 import wicket.contrib.tinymce.settings.TinyMCESettings;
 import wicket.markup.ComponentTag;
 import wicket.markup.MarkupStream;
 import wicket.markup.html.PackageResource;
 import wicket.markup.html.WebComponent;
 import wicket.markup.html.panel.Panel;
 import wicket.markup.html.resources.JavaScriptReference;
 
 /**
  * Reusable wicket component for TinyMCE editor. If you want to create a
  * custom TinyMCE editor take a look at TinyMCESettings class.
  * 
  * @author Iulian-Corneliu Costan (iulian.costan@gmail.com)
  * @author Frank Bille Jensen (fbille@avaleo.net)
  * @see TinyMCESettings
  */
 public class TinyMCEPanel extends Panel
 {
 	private static final long serialVersionUID = 1L;
 
 	private static final String TINY_MCE = ".*(\\.js|\\.gif|\\.html|\\.htm|\\.css)$";
 
 	/** settings for tinymce editor */
 	private TinyMCESettings settings;
 
 	/**
 	 * Construct TinyMCE component with default settings.
 	 * 
 	 * @param id
 	 *            wicket component id
 	 */
 	public TinyMCEPanel(final String id)
 	{
 		this(id, new TinyMCESettings());
 	}
 
 	/**
 	 * Construct TinyMCE component passing customs settings.
 	 * 
 	 * @param id
 	 * @param settings
 	 *            settings for tinymce component
 	 */
 	public TinyMCEPanel(final String id, final TinyMCESettings settings)
 	{
 		super(id);
 
 		// add tinymce init script and startup js
 		add(new JavaScriptReference("tinymce", TinyMCEPanel.class, "tiny_mce/tiny_mce_src.js"));
 		add(new WebComponent("initScript")
 		{
 			private static final long serialVersionUID = 1L;
 
 			protected void onComponentTagBody(final MarkupStream markupStream,
 					final ComponentTag openTag)
 			{
 				StringBuffer buffer = new StringBuffer();
 
 				buffer.append("\ntinyMCE.init({" + settings.toJavaScript() + "\n});\n");
 				buffer.append(settings.getLoadPluginJavaScript());
 				buffer.append(settings.getAdditionalPluginJavaScript());
 
 				replaceComponentTagBody(markupStream, openTag, buffer.toString());
 			}
 		});
 	}
 }
