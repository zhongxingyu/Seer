 /**
  * 
  */
 package dk.xam.spy.handlers;
 
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 
 class BalsamiqControl extends Balsamiq {
 
 	
 	private final Control c;
 	
 	public BalsamiqControl(String ctrl, Control c) {
 		super(ctrl);
 		this.c = c;
 	}
 
 
 	@Override
 	public int processChildren(int count, StringBuilder sb, int controlCount) {
 		if (c instanceof Composite) {
 			for (Control child : ((Composite) c).getChildren()) {
 				BalsamiqControl ct = getControlTypeID(child);
 				controlCount = ct.process(count + 1, sb, controlCount);
 			}
 		}
 
 		if (c instanceof ToolBar) {
 			ToolBar tb = (ToolBar) c;
 			for (final ToolItem ti : tb.getItems()) {
 				Balsamiq ct = new Balsamiq("com.balsamiq.mockups::Button") {
 
 					@Override
 					Rectangle getDisplayLocation() {
 						Rectangle xy = ti.getBounds();
 						xy = c.getDisplay().map(c, null, xy);
 
 						Rectangle bounds = ti.getBounds(); // c.getDisplay().map(c, null,
 
 						bounds.width *= getSettings().getScaleFactor();
 						bounds.height *= getSettings().getScaleFactor();
 
 						return new Rectangle(xy.x, xy.y, bounds.width, bounds.height);
 					}
 
 					@Override
 					public String getText() {
 						return ti.getText();
 					}
 
 					@Override
 					boolean isVisible() {
 						return true;
 					}
 
 					@Override
 					public boolean isVisual() {
 						return true;
 					}
 
 					@Override
 					public int processChildren(int count, StringBuilder sb, int controlCount) {
 						return controlCount;
 					}
 
 					@Override
 					int getFontSize() {
 						return (int) ( c.getFont().getFontData()[0].height * getSettings().getFontFactor() );
 					}
 
 				};
 
 				controlCount = ct.process(count + 1, sb, controlCount);
 			}
 		}
 		return controlCount;
 	}
 
 	protected BalsamiqSettings getSettings() {
 		return BalsamiqSettings.getInstance();
 	}
 
 
 	@Override
 	boolean isVisible() {
 		return c.isVisible();
 	}
 
 	@Override
 	Rectangle getDisplayLocation() {
 
 		Point xy;
 		if (c instanceof Shell) {
 			xy = c.getLocation();
 		} else {
 			xy = c.getDisplay().map(c.getParent(), null, c.getLocation());
 		}
 
 		Rectangle bounds = c.getBounds(); // c.getDisplay().map(c, null,
 
 		double scaleFactor = getSettings().getScaleFactor();
 		return new Rectangle((int) (xy.x * scaleFactor), (int) (xy.y * scaleFactor), (int) (bounds.width * scaleFactor), (int) (bounds.height * scaleFactor));
 
 	}
 
 	@Override
 	int getFontSize() {
 		return (int) ( c.getFont().getFontData()[0].height * getSettings().getFontFactor());
 	}
 
 
 
 	@Override
 	public String getControlType() {
 		return controltype;
 	}
 
 	@Override
 	public boolean isVisual() {
 		if (isUnknown()) { // to skip unknown controls.
 			return false;
 		}
 
		if (c.getClass().getCanonicalName().equals(Composite.class.getCanonicalName())) {
 			return false;
 		}
 		if (c instanceof Label || c instanceof CLabel) { 
 			// if labels has no
 			// text they are
 			// most likely
 			// just fillers.
 			return getText() == null || getText().length() > 0;
 		}
 
 		if (c instanceof ToolBar) {
 			return false;
 		}
 
 		return true;
 	}
 
 
 	private boolean isUnknown() {
 		if(!controltype.startsWith("com.balsamiq")) {
 			getUnknownControls().add(controltype);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public String getText() {
 
 		if (c instanceof CTabFolder) {
 			CTabFolder tf = (CTabFolder) c;
 			StringBuffer s = new StringBuffer();
 
 			for (int i = 0; i < tf.getItemCount(); i++) {
 				CTabItem ti = tf.getItem(i);
 				s.append(ti.getText());
 				if (i + 1 < tf.getItemCount()) {
 					s.append(',');
 				}
 			}
 			return s.toString();
 		}
 
 		if (c instanceof Combo) {
 			return ((Combo) c).getText();
 		}
 
 		if (c instanceof Group) {
 			return ((Group) c).getText();
 		}
 		if (c instanceof Button) {
 			String string = ((Button) c).getText();
 			if (string != null) {
 				return string.replace("&", "");
 			} else {
 				return null;
 			}
 		}
 		if (c instanceof Text) {
 			return ((Text) c).getText();
 		}
 
 		if (c instanceof CLabel) {
 			String string = ((CLabel) c).getText();
 			if (string != null) {
 				return string.replace("&", "");
 			} else {
 				return null;
 			}
 		}
 		if (c instanceof Label) {
 			String string = ((Label) c).getText();
 			if (string != null) {
 				return string.replace("&", "");
 			} else {
 				return null;
 			}
 		}
 		if (c instanceof Tree) {
 			StringBuilder sb = new StringBuilder();
 			TreeItem[] items = ((Tree) c).getItems();
 			for (int i = 0; i < items.length; i++) {
 				TreeItem treeItem = items[i];
 				sb.append("F " + treeItem.getText() + "\n");
 			}
 			return sb.toString();
 		}
 
 		if (c instanceof Link) {
 			String text = ((Link)c).getText();
 			text = text.replace("<a href=\"\">", "");
 			text = text.replace("</a>", "");
 			return text;
 		}
 
  
 		if (c instanceof Hyperlink) {
 			String text = ((Hyperlink)c).getText();
 			return text;
 		}
 
 		if (c instanceof Table) {
 			StringBuilder sb = new StringBuilder();
 			Table t = (Table)c;
 			int[] columnOrder = t.getColumnOrder();
 			for (int columnidx = 0; columnidx < columnOrder.length; columnidx++) {
 				TableColumn column = t.getColumn(columnOrder[columnidx]);
 				sb.append(column.getText());
 				if(columnidx<columnOrder.length) sb.append(",");
 			}
 			if(columnOrder.length>0) sb.append("\n");
 			
 			TableItem[] items = ((Table) c).getItems();
 			for (int i = 0; i < items.length; i++) {
 				TableItem item = items[i];
 				for (int columnidx = 0; columnidx < columnOrder.length; columnidx++) {
 					sb.append(item.getText(columnOrder[columnidx]));
 					if(columnidx<columnOrder.length) sb.append(",");
 				}
 				sb.append(item.getText() + "\n");
 			}
 			return sb.toString();
 		}
 		
 		if (c instanceof Shell) {
 			return ((Shell) c).getText();
 		}
 
 		return null;
 	}
 
 	
 
 }
