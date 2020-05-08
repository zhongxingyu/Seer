 package org.wingx.plaf.css;
 
 import org.wingx.XRichTextEditor;
 import org.wings.io.Device;
 import org.wings.plaf.Update;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.css.AbstractUpdate;
 import org.wings.plaf.css.UpdateHandler;
 import org.wings.plaf.css.AbstractComponentCG;
 import org.wings.SDimension;
 
 import java.io.IOException;
 
 public class RichTextEditorCG extends AbstractComponentCG<XRichTextEditor> implements org.wingx.plaf.RichTextEditorCG<XRichTextEditor> {
     int horizontalOversize = 2;
 
     public int getHorizontalOversize() {
         return horizontalOversize;
     }
 
     public void setHorizontalOversize(int horizontalOversize) {
         this.horizontalOversize = horizontalOversize;
     }
 
     @Override
     public void installCG(XRichTextEditor component) {
         if (isMSIE(component))
             component.putClientProperty("horizontalOversize", new Integer(horizontalOversize));
 
         super.installCG(component);
     }
 
     public String getText(XRichTextEditor component) {
         String text = component.getText();
         text = text.replaceAll("\n", "<BR />");
         text = text.replaceAll("'", "\'");
 
         return text;
     }
 
     public void writeInternal(Device device, final XRichTextEditor component) throws IOException {
         SDimension preferredSize = component.getPreferredSize();
         boolean tableWrapping = Utils.isMSIE(component) && preferredSize != null && "%".equals(preferredSize.getWidthUnit());
         String actualWidth = null;
         if (tableWrapping) {
             actualWidth = preferredSize.getWidth();
             Utils.setPreferredSize(component, "100%", preferredSize.getHeight());
             device.print("<table style=\"table-layout: fixed; width: " + actualWidth + "\"><tr>");
             device.print("<td style=\"padding-right: " + Utils.calculateHorizontalOversize(component, true) + "px\">");
         }
 
         device.print("<textarea");
         Utils.optAttribute(device, "id", component.getName());
         Utils.optAttribute(device, "eid", component.getName());
         Utils.optAttribute(device, "name", component.getName());
         Utils.optAttribute(device, "tabindex", component.getFocusTraversalIndex());
         device.print(">");
         device.print(getText(component));
         device.print("</textarea>");
 
         if(tableWrapping) {
             device.print("</td></tr></table>");
         }
     }
 
     public Update getTextUpdate(XRichTextEditor component) {
         return new TextUpdate(component);
     }
 
     private class TextUpdate extends AbstractUpdate {
         XRichTextEditor editor;
 
         public TextUpdate(XRichTextEditor component) {
             super(component);
 
             this.editor = component;
         }
 
         public Handler getHandler() {
             UpdateHandler update = new UpdateHandler("runScript");
             String name = "editor_" + component.getName();
             update.addParameter("wingS.global.onHeadersLoaded(function() { window." + name + ".setEditorHTML('" + getText(editor) + "'); });");
             // TODO: update size, border, etc.
             return update;
         }
     }
 }
