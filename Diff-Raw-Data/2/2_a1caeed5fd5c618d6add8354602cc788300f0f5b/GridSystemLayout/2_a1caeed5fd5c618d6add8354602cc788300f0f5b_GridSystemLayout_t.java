 package pt.ist.vaadinframework.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.vaadin.ui.Component;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.VerticalLayout;
 
 public class GridSystemLayout extends GridLayout {
 
     private final Map<String, Component> layout = new HashMap<String, Component>();
     private static final int MAX_COLUMNS = 16;
     private final int maxColumns;
     private int index = 0;
     private int currLn = 0;
 
     public GridSystemLayout(int maxColumns) {
 	super(maxColumns, 1);
 	this.maxColumns = maxColumns;
 	setWidth("100%");
 	setSpacing(true);
 	setMargin(true);
     }
 
     public GridSystemLayout() {
 	this(MAX_COLUMNS);
     }
 
     public Component setCell(String id, int cols) {
 	return setCell(id, cols, null);
     }
 
     public Component setCell(String id, int cols, Component sl) {
 	return setCell(id, 0, cols, 0, sl);
     }
 
     public Component setCell(String id, int prefix, int cols, int suffix) {
 	return setCell(id, prefix, cols, suffix, null);
     }
 
     public Component setCell(String id, int prefix, int cols, int suffix, Component sub) {
 
 	Component sl = sub != null ? sub : new VerticalLayout();
 	Component component = layout.get(id);
 
 	if (component != null) {
 	    replaceComponent(component, sl);
 	} else {
	    addComponent(prefix, cols, suffix, sl);
 	}
 
 	layout.put(id, sl);
 	return sl;
     }
 
     public Component addComponent(int cols, Component component) {
 	return addComponent(0, cols, 0, component);
     }
 
     public Component addComponent(int prefix, int cols, int suffix, Component component) {
 	if (prefix != 0) {
 	    index += prefix;
 	}
 
 	addComponent(component, index, currLn, index + cols - 1, currLn);
 
 	index += cols;
 
 	if (suffix != 0) {
 	    index += suffix;
 	}
 
 	if (index % maxColumns == 0) {
 	    setRows(++currLn + 1);
 	    index = 0;
 	}
 
 	return component;
     }
 
     public Component getCell(String id) {
 	return layout.get(id);
     }
 }
