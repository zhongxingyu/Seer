 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.plaf.css.script.OnPageRenderedScript;
 import org.wings.session.ScriptManager;
 
 import java.io.IOException;
 
 public class DialogCG extends WindowCG implements org.wings.plaf.DialogCG {
 
     public void writeInternal(Device device, SComponent component) throws IOException {
 
         SDialog dialog = (SDialog) component;
         SRootContainer owner = dialog.getOwner();
 
         String name = dialog.getName();
 
         device.print("\n\n" +
                 "<div id=\"" + name + "\">\n" +
                 "  <div class=\"hd\">" + (dialog.getTitle() != null ? dialog.getTitle() : "&#160;") + "</div>\n" +
                "  <div class=\"bd\" style=\"padding:0;\">");
 
         super.writeInternal(device, dialog);
 
         device.print("  </div>\n" +
                 "</div>\n");
 
         StringBuilder sb = new StringBuilder();
         sb.append("dialog_").append(name).append(" = new wingS.dialog.SDialog(\"").append(name).append("\"")
                 .append(", {");
 
         if (dialog.getX() > -1 && dialog.getY() > -1) {
         	sb.append("x:").append(dialog.getX()).append(",")
         	  .append("y:").append(dialog.getY()).append(",");
         }
         else {
         	sb.append("fixedcenter:true").append(",");
         }
 
         if (!(owner instanceof SFrame))
             sb.append("viewportelement:\"").append(owner.getName()).append("\",");
 
         sb.append("visible:").append(dialog.isVisible()).append(",")
           .append("modal:").append(dialog.isModal()).append(",")
           .append("draggable:").append(dialog.isDraggable()).append(",")
           .append("close:").append(dialog.isClosable()).append(",")
           .append("constraintoviewport:true").append("});\n")
           .append("dialog_").append(name).append(".render();\n");
 
         //sb.append("var resize = new YAHOO.util.Resize(\"").append(name).append("\");");
 
         ScriptManager.getInstance().addScriptListener(new OnPageRenderedScript(sb.toString()));
     }
 }
