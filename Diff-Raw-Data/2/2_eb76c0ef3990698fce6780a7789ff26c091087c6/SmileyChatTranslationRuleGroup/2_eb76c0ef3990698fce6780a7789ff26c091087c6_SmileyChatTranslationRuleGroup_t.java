 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.chat;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.MapTool;
 
 public class SmileyChatTranslationRuleGroup extends ChatTranslationRuleGroup {
 
 	private JPopupMenu emotePopup;
 	
 	public SmileyChatTranslationRuleGroup() {
 		super ("Smilies");
 		
 		initSmilies();
 	}
 
 	public JPopupMenu getEmotePopup() {
 		return emotePopup;
 	}
 	
 	@Override
 	public boolean isEnabled() {
 		return AppPreferences.getShowSmilies();
 	}
 	
 	private void initSmilies() {
 		
 		// Load the smilies
 		Properties smileyProps = new Properties();
 		try {
 			smileyProps.loadFromXML(ChatProcessor.class.getClassLoader().getResourceAsStream("net/rptools/maptool/client/ui/chat/smileyMap.xml"));
 		} catch (IOException ioe) {
 			System.err.println("Could not load smiley map: " + ioe);
 		}
 
 		// Wrap values with img tag
 		emotePopup = new JPopupMenu();
 		for (Enumeration e = smileyProps.propertyNames(); e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 
 			// This is an incredibly bad hack to avoid writing an xml parser for the smiley map. I'm feeling lazy today.
 			StringTokenizer strtok = new StringTokenizer(smileyProps.getProperty(key), "|");
 			String value = strtok.nextToken();
 			String example = strtok.nextToken();
 			
 			String imgValue = "<img src='cp://" + value + "'>"; 
 			smileyProps.setProperty(key, imgValue);
 			
 			JMenuItem item = new JMenuItem(new InsertEmoteAction(value, example)) {
 				{
 					setPreferredSize(new Dimension(25, 16));
 				}
 			};
 			
 			emotePopup.add(item);
 		}
 
 		// Install the translation rules
 		List<ChatTranslationRule> ruleList = new LinkedList<ChatTranslationRule>();
 		for (Enumeration e = smileyProps.propertyNames(); e.hasMoreElements();) {
 			String key = (String) e.nextElement();
 			String value = smileyProps.getProperty(key);
 			
 			/* Make sure we're not in roll output. 
 			 * Wouldn't let me do this using lookbehind :-/
 			 */
 			key = "^((?:[^\036]|\036[^\036]*\036)*)" + key;
			value = "$1" + value;
 			
 			addRule(new RegularExpressionTranslationRule(key, value));
 		}
 	}
 
 	////
 	// EMOTE 
 	private class InsertEmoteAction extends AbstractAction {
 
 		private String emoteImageSrc;
 		private String insert;
 		
 		public InsertEmoteAction(String emoteImageSrc, String insert) {
 			// This will force the image to be loaded into memory for use in the message panel
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil.getImage(emoteImageSrc)));
 				this.emoteImageSrc = emoteImageSrc;
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			this.insert = insert;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().getCommandTextArea().setText(MapTool.getFrame().getCommandPanel().getCommandTextArea().getText() + insert);
 			MapTool.getFrame().getCommandPanel().getCommandTextArea().requestFocusInWindow();
 		}
 	}
 	
 }
