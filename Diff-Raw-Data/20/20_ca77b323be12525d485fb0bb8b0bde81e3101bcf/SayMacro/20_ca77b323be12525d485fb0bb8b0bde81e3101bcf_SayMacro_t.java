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
 package net.rptools.maptool.client.macro.impl;
 
 import java.awt.Color;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolMacroContext;
 import net.rptools.maptool.client.macro.MacroContext;
 import net.rptools.maptool.client.macro.MacroDefinition;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 
 @MacroDefinition(
 	name = "say",
 	aliases = { "s" },
 	description = "Broadcast a message to all connected players."
 )
 public class SayMacro extends AbstractMacro {
 
     public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
     	macro = processText(macro);
     	
         StringBuilder sb = new StringBuilder();
 
         String identity = MapTool.getFrame().getCommandPanel().getIdentity();
         sb.append("<table cellpadding=0><tr>");
         
        if (MapTool.getFrame().getCommandPanel().isImpersonating() && AppPreferences.getShowAvatarInChat()) {
         	Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokenByName(identity);
         	if (token != null) {
         		MD5Key imageId = token.getPortraitImage();
         		if (imageId == null) {
         			imageId = token.getImageAssetId();
         		}
 	        	sb.append("<td valign=top width=40 style=\"padding-right:5px\"><img src=\"asset://").append(imageId).append("-40\" ></td>");
         	}
         }
         
         sb.append("<td valign=top style=\"margin-right: 5px\">");
         if (executionContext != null && MapTool.getParser().isMacroPathTrusted() && !MapTool.getPlayer().isGM()) {
         	sb.append("<span class='trustedPrefix' ").append("title='").append(executionContext.getName());
         	sb.append("@").append(executionContext.getSouce()).append("'>");
         }
         sb.append(identity).append(": ");
         if (executionContext != null && MapTool.getParser().isMacroPathTrusted() && !MapTool.getPlayer().isGM()) {
        	sb.append("</span>");
         }
 
         sb.append("</td><td valign=top>");
 
         Color color = MapTool.getFrame().getCommandPanel().getTextColorWell().getColor();
         if (color != null) {
         	sb.append("<span style='color:#").append(String.format("%06X", (color.getRGB() & 0xFFFFFF))).append("'>");
         }
         sb.append(macro);
         if (color != null) {
         	sb.append("</span>");
         }
         sb.append("</td>");
         
         sb.append("</tr></table>");
         
         MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), sb.toString()));
     }
 }
