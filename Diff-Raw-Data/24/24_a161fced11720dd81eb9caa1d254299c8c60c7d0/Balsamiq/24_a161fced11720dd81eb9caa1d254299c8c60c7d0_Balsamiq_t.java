 package dk.xam.spy.handlers;
 
 import java.net.URLEncoder;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.part.PageBook;
 
 public abstract class Balsamiq {
 
 	static private Set<String> unknownControls = new HashSet<String>();
 
 	final String controltype;
 
 	public Balsamiq(String ctrl) {
 		controltype = ctrl;
 	}
 
 	@SuppressWarnings("deprecation")
 	public int process(int count, StringBuilder sb, int controlCount) {
 		StringBuilder spaces = new StringBuilder(count * 2);
 		for (int i = 0; i < count * 2; i++) {
 			spaces.append(' ');
 		}
 
 		if (isVisible()) {
 
 			if (isVisual()) {
 				sb.append(spaces + "<control ");
 				sb.append(attr("controlID", Integer.valueOf(controlCount++)));
 
 				sb.append(attr("controlTypeID", getControlType()));
 
 				Rectangle bounds = getDisplayLocation();
 
 				sb.append(attr("x", Integer.valueOf(bounds.x)));
 				sb.append(attr("y", Integer.valueOf(bounds.y)));
 				sb.append(attr("w", Integer.valueOf(bounds.width)));
 				sb.append(attr("h", Integer.valueOf(bounds.height)));
 				sb.append(attr("zOrder", Integer.valueOf(count)));
 				sb.append(">");
 				sb.append("\n");
 
 				if (getText() != null) {
 					sb.append(spaces + "<controlProperties>\n" + spaces
 							+ spaces + "<text>"
 							+ URLEncoder.encode(getText()).replace("+", "%20")
 							+ "</text>\n");
 					sb.append("" + spaces + spaces + "<size>" + getFontSize()
 							+ "</size>\n");
 
 					sb.append(spaces + "</controlProperties>\n");
 				}
 				sb.append(spaces + "</control>");
 				sb.append("\n");
 			}
 
 			controlCount = processChildren(count, sb, controlCount);
 		}
 		return controlCount;
 	}
 
 	abstract public int processChildren(int count, StringBuilder sb,
 			int controlCount);
 
 	abstract boolean isVisible();
 
 	abstract Rectangle getDisplayLocation();
 
 	abstract int getFontSize();
 
 	public String getControlType() {
 		return controltype;
 	}
 
 	abstract public boolean isVisual();
 
 	abstract public String getText();
 
 	static String attr(String attribute, Object contents) {
 		return " " + attribute + "=\"" + contents + "\" ";
 	}
 
	static Class<?> safeGetClass(Object o) {
	    Class<?> clazz = o.getClass();
        String canonicalName = clazz.getCanonicalName();

        while(canonicalName == null && clazz.getSuperclass() != null) {
            canonicalName = clazz.getSuperclass().getCanonicalName();
            clazz = clazz.getSuperclass();
        }

        return clazz;
	}

 	static BalsamiqControl getControlTypeID(Control c) {
 
	    Class<?> clazz = safeGetClass(c);

	    String simpleName = clazz.getSimpleName();
 
 		if(simpleName.equals("Link") || simpleName.equals("Hyperlink")) {
 			return new BalsamiqControl("com.balsamiq.mockups::Link",c);
 		}
 		if (simpleName.equals("ProgressBar")) {
 			return new BalsamiqControl("com.balsamiq.mockups::ProgressBar", c);
 		}
 		if (simpleName.equals("ProgressIndicator")) {
 			return new BalsamiqControl("com.balsamiq.mockups::ProgressBar", c);
 		}
 		if (simpleName.equals("FilteredTree")) {
 			return new BalsamiqControl("com.balsamiq.mockups::Tree", c);
 		}
 
 		if (simpleName.equals("Tree")) {
 			return new BalsamiqControl("com.balsamiq.mockups::Tree", c);
 		}
 		if (simpleName.equals("CLabel")) {
 			return new BalsamiqControl("com.balsamiq.mockups::Label", c);
 		}
 		if (simpleName.equals("Label")) {
 			return new BalsamiqControl("com.balsamiq.mockups::Label", c);
 		}
 		if (simpleName.equals("Text")) {
 			return new BalsamiqControl("com.balsamiq.mockups::TextInput", c);
 		}
 		if (simpleName.equals("Button")) {
 			if ((c.getStyle() & SWT.CHECK) == SWT.CHECK) {
 				return new BalsamiqControl("com.balsamiq.mockups::CheckBox", c);
 			} else if ((c.getStyle() & SWT.RADIO) == SWT.RADIO) {
 				return new BalsamiqControl("com.balsamiq.mockups::RadioButton",
 						c);
 			} else {
 				return new BalsamiqControl("com.balsamiq.mockups::Button", c);
 			}
 
 		}
 		if (simpleName.equals("Composite") || simpleName.equals("LayoutComposite")) {
 			return new BalsamiqControl("com.balsamiq.mockups::FieldSet", c);
 		}
 		if(simpleName.equals("PageBook")) {
 			PageBook pb = (PageBook)c;
 			// take first child that is visible
 			Control[] children = pb.getChildren();
 			for (int i = 0; i < children.length; i++) {
 				Control control = children[i];
 				if(control.isVisible()) {
 					return getControlTypeID(control);
 				}
 			}
 		}
 		if (simpleName.equals("StyledText")) {
 			return new BalsamiqControl("com.balsamiq.mockups::Paragraph", c);
 		}
 		if (simpleName.equals("ScrolledComposite")) {
 			return new BalsamiqControl("com.balsamiq.mockups::FieldSet", c);
 		}
 		if (simpleName.equals("ToolBar")) {
 			return new BalsamiqControl("com.balsamiq.mockups::ButtonBar", c);
 		}
 
 		if (simpleName.equals("Shell")) {
 			return new BalsamiqControl("com.balsamiq.mockups::TitleWindow", c);
 		} // topheight ?
 
 		if (simpleName.equals("Group")) {
 			return new BalsamiqControl("com.balsamiq.mockups::FieldSet", c);
 		}
 
 		if (simpleName.equals("Combo")) {
 			return new BalsamiqControl("com.balsamiq.mockups::ComboBox", c);
 		}
 		if (simpleName.equals("TabFolder") || simpleName.equals("CTabFolder")) {
 			return new BalsamiqControl("com.balsamiq.mockups::TabBar", c);
 		}
 		if(simpleName.equals("Table")) {
 			return new BalsamiqControl("com.balsamiq.mockups::DataGrid",c);
 		}
 		return new BalsamiqControl(c.getClass().getCanonicalName(), c);
 	}
 
 	public static Set<String> getUnknownControls() {
 		return unknownControls;
 	}
 }
