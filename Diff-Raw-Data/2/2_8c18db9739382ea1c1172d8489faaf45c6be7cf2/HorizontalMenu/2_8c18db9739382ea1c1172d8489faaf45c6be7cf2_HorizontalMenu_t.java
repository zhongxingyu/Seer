 package com.codingcrayons.tnt.component;
 
 import com.codingcrayons.tnt.core.DependenciesManager;
 import java.io.IOException;
 
 import javax.faces.component.FacesComponent;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UISelectItem;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 @FacesComponent(value = "tnt.HorizontalMenu")
 public class HorizontalMenu extends UIOutput {
 
	public static final String NAV_CLASS = "tnt-horizontal-menu";
 	public static final String SELECT_CLASS = "mini-menu";
 
 	protected boolean mini = false;
 
 	public HorizontalMenu() {
 		this(FacesContext.getCurrentInstance());
 	}
 
 	public HorizontalMenu(FacesContext context) {
 		// a constructor only for testing
 		super();
 		DependenciesManager.getInstance(context).insertMetaViewport();
 		DependenciesManager.getInstance(context).insertTNTCSS();
 	}
 
 	public boolean isMini() {
 		return mini;
 	}
 
 	public void setMini(boolean mini) {
 		this.mini = mini;
 	}
 
 	@Override
 	public void encodeAll(FacesContext context) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 
 		writer.startElement("nav", null);
 		writer.writeAttribute("class", NAV_CLASS, null);
 
 		writeSelect(writer);
 		writeMenu(writer, context);
 
 		writer.endElement("nav");
 	}
 
 	protected void writeSelect(ResponseWriter writer) throws IOException {
 
 		if (!mini) {
 			return;
 		}
 
 		writer.startElement("div", null);
 		writer.writeAttribute("class", SELECT_CLASS, null);
 
 		writer.startElement("select", null);
 		writer.writeAttribute("onchange", "var url = this.options[this.selectedIndex].value; if (url) window.location.assign(url)", null);
 
 		for (UIComponent child : getChildren()) {
 			if (child instanceof UISelectItem) {
 				UISelectItem item = (UISelectItem)child;
 				writer.startElement("option", null);
 				writer.writeAttribute("value", item.getItemValue(), null);
 				writer.writeText(item.getItemLabel(), null);
 				writer.endElement("option");
 			}
 		}
 
 		writer.endElement("select");
 
 		writer.endElement("div");
 	}
 
 	protected void writeMenu(ResponseWriter writer, FacesContext context) throws IOException {
 		writer.startElement("ul", null);
 
 		if (mini) {
 			writer.writeAttribute("class", "hidden", null);
 		}
 
 		for (UIComponent child : getChildren()) {
 			child.encodeAll(context);
 		}
 
 		writer.endElement("ul");
 	}
 }
