 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2011 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.components;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.wicket.request.resource.ContextRelativeResource;
 import org.apache.wicket.request.resource.IResource;
 import org.apache.wicket.request.resource.ResourceReference;
 
 import wicket.contrib.tinymce.settings.Button;
 import wicket.contrib.tinymce.settings.MediaPlugin;
 import wicket.contrib.tinymce.settings.TinyMCESettings;
 
 import com.tysanclan.site.projectewok.TysanSession;
 import com.tysanclan.site.projectewok.util.MemberUtil;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public class TysanTinyMCESettings extends TinyMCESettings {
 	private static final long serialVersionUID = 1L;
 
 	public TysanTinyMCESettings() {
 		super(Theme.advanced, Language.en);
 
 		Toolbar toolbar = Toolbar.first;
 		List<Button> buttons = new LinkedList<Button>();
 
 		buttons.add(Button.bold);
 		buttons.add(Button.italic);
 		buttons.add(Button.underline);
 		buttons.add(Button.link);
 		buttons.add(Button.unlink);
 		buttons.add(Button.bullist);
 		buttons.add(Button.numlist);
 		buttons.add(Button.image);
 
 		TysanSession ts = TysanSession.get();
 		if (ts != null && MemberUtil.isMember(ts.getUser())) {
 			MediaPlugin mediaPlugin = new MediaPlugin();
 
 			register(mediaPlugin);
 
 			buttons.add(mediaPlugin.getMediaButton());
 		}
 
 		buttons.add(BlockQuoteButton.blockquote);
 		buttons.add(Button.undo);
 		setToolbarButtons(toolbar, buttons);
 		setToolbarButtons(Toolbar.second, new LinkedList<Button>());
 		setToolbarButtons(Toolbar.third, new LinkedList<Button>());
 		setToolbarButtons(Toolbar.fourth, new LinkedList<Button>());
 
 		setConvertUrls(false);
		setStatusbarLocation(null);
		addCustomSetting("theme_advanced_statusbar_location : \"none\"");
 		setContentCss(new ResourceReference("mceCss") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public IResource getResource() {
 				return new ContextRelativeResource("css/mce.css");
 			}
 
 		});
 	}
 
 	private static class BlockQuoteButton extends Button {
 		private static final long serialVersionUID = 1L;
 
 		public static final Button blockquote = new BlockQuoteButton();
 
 		private BlockQuoteButton() {
 			super("blockquote");
 		}
 	}
 }
