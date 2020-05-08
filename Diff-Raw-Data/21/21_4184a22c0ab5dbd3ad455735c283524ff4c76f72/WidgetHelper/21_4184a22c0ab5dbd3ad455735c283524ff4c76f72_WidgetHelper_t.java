 package org.mule.galaxy.web.client.ui.panel;
 
 import org.mule.galaxy.web.client.ui.NavMenuItem;
 import org.mule.galaxy.web.client.ui.button.ToolbarButton;
 import org.mule.galaxy.web.client.ui.button.ToolbarButtonEvent;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.ListViewEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.Util;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.ListView;
 import com.extjs.gxt.ui.client.widget.WidgetComponent;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.List;
 
 public class WidgetHelper extends Composite {
 
     public static Label newSpacer() {
         Label spacer = new Label(" ");
         spacer.setStyleName("spacer");
         return spacer;
     }
 
     public static Label newSpacer(String width) {
         Label spacer = new Label(" ");
         spacer.setWidth(width);
         return spacer;
     }
 
     public static Label newSpacerPipe() {
         Label pipe = new Label(" | ");
         pipe.setStyleName("pipe-with-space");
         return pipe;
     }
 
     public static Widget asDiv(Widget w) {
         FlowPanel p = new FlowPanel();
         p.add(w);
         return p;
     }
 
     public static Label newLabel(String name, String style) {
         Label label = new Label(name);
         label.setStyleName(style);
         return label;
     }
 
     public static InlineFlowPanel asHorizontal(Widget w1, Widget w2) {
         InlineFlowPanel p = new InlineFlowPanel();
         p.add(w1);
         p.add(w2);
         return p;
     }
 
     public static InlineFlowPanel asHorizontal(Widget w1, Widget w2, Widget w3) {
         InlineFlowPanel p = new InlineFlowPanel();
         p.add(w1);
         p.add(w2);
         p.add(w3);
         return p;
     }
 
     public static FlexTable createRowTable() {
         FlexTable table = new FlexTable();
         table.getRowFormatter().setStyleName(0, "artifactTableHeader");
         table.setStyleName("artifactTableFull");
         table.setWidth("100%");
         table.setCellSpacing(0);
         table.setCellPadding(0);
 
         return table;
     }
 
     public static void styleHeaderColumn(FlexTable table) {
         for (int i = 0; i < table.getRowCount(); i++) {
             table.getCellFormatter().setStyleName(i, 0, "artifactTableHeader");
             table.getCellFormatter().setStyleName(i, 1, "artifactTableEntry");
         }
     }
 
     public static FlexTable createColumnTable() {
         FlexTable table = createTable();
         table.setStyleName("columnTable");
         table.setCellSpacing(0);
         table.setCellPadding(0);
 
         return table;
     }
 
     public static FlexTable createTable() {
         FlexTable table = new FlexTable();
         table.setStyleName("artifactTable");
         table.setCellSpacing(0);
         table.setCellPadding(0);
         return table;
     }
 
     public static Widget createPrimaryTitle(String title) {
         Label label = new Label(title);
         label.setStyleName("title");
         return label;
     }
 
     public static InlineFlowPanel createTitle(String title) {
         InlineFlowPanel titlePanel = new InlineFlowPanel();
         titlePanel.setStyleName("rightlinked-title-panel");
 
         Label label = new Label(title);
         label.setStyleName("rightlinked-title");
         titlePanel.add(label);
         return titlePanel;
     }
 
     public static Label createTitleText(String title) {
         Label label = new Label(title);
         label.setStyleName("right-title");
         return label;
     }
 
     public static InlineFlowPanel createTitleWithLink(String name, Widget rightWidget) {
         InlineFlowPanel commentTitlePanel = new InlineFlowPanel();
         commentTitlePanel.setStyleName("rightlinked-title-panel");
 
         commentTitlePanel.add(rightWidget);
 
         Label label = new Label(name);
         label.setStyleName("rightlinked-title");
         commentTitlePanel.add(label);
 
         rightWidget.setStyleName("rightlinked-title-link");
         return commentTitlePanel;
     }
 
 
     public static ContentPanel createAccodionWrapperPanel(boolean hideWidget) {
         AccordionLayout layout = new AccordionLayout();
         layout.setHideCollapseTool(hideWidget);
 
         ContentPanel accordionPanel = new ContentPanel(layout);
         accordionPanel.setCollapsible(false);
         accordionPanel.setHeaderVisible(false);
         accordionPanel.setAutoHeight(true);
         accordionPanel.setAutoWidth(true);
         accordionPanel.setBorders(false);
         accordionPanel.setBodyBorder(false);
         return accordionPanel;
     }
 
     public static ContentPanel createAccodionWrapperPanel() {
         return createAccodionWrapperPanel(false);
     }
 
     /**
      * Creates a simple button that links to a History item
      *
      * @param buttonLabel
      * @param token
      * @return
      */
     public static Button createHistoryButton(String buttonLabel, final String token) {
         Button newBtn = new Button(buttonLabel);
         newBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 History.newItem(token);
             }
         });
         return newBtn;
 
     }
 
 
     /**
      * Creates a simple toolbar button that links to a History item
      *
      * @param buttonLabel
      * @param token
      * @param style       - toolbar-btn_left, toolbar-btn_center, toolbar-btn_right
      * @return
      */
     public static ToolbarButton createToolbarHistoryButton(String buttonLabel,
                                                            final String token, String style, String toolTip) {
         ToolbarButton newBtn = new ToolbarButton(buttonLabel);
 
         newBtn.setStyleName(style);
         if (toolTip != null) {
             newBtn.setToolTip(toolTip);
         }
         newBtn.addSelectionListener(new SelectionListener<ToolbarButtonEvent>() {
             @Override
             public void componentSelected(ToolbarButtonEvent ce) {
                 History.newItem(token);
             }
         });
         return newBtn;
 
     }
 
     /*
      * Use base style - which is for a button by itself
      */
 
     public static ToolbarButton createToolbarHistoryButton(String buttonLabel, final String token, String tooltip) {
         return createToolbarHistoryButton(buttonLabel, token, "toolbar-btn", tooltip);
     }
 
     public static ToolbarButton createToolbarHistoryButton(String buttonLabel, final String token) {
         return createToolbarHistoryButton(buttonLabel, token, "toolbar-btn", null);
     }
 
     public static String createFauxLink(String value) {
         return createFauxLink(value, true);
     }
 
     /*
      * Make Labels, strings, etc appear to be links
      */
 
     public static String createFauxLink(String value, boolean hover) {
         String html = "";
         html += " <span style=\"text-decoration: none; cursor:pointer; color: #016c96;\" ";
         if (hover) {
             html += " onmouseover=\"this.style.textDecoration = 'underline'\" onmouseout=\"this.style.textDecoration = 'none'\" ";
         }
         html += ">" + value + "</span>";
         return html;
 
     }
 
     public WidgetHelper() {
         super();
     }
 
     public static Label columnLabel(String s) {
         Label l = new Label(s);
         l.addStyleName("bold-right-label");
         return l;
     }
 
     public static Label leftColumnLabel(String s) {
         Label l = new Label(s);
         l.addStyleName("bold-left-label");
         return l;
     }
 
     public static Label cellLabel(String s) {
         Label l = new Label(s);
         l.addStyleName("cell-alt");
         return l;
     }
 
     public static Label cellLabel(Long lg) {
         Label l = longLabel(lg);
         l.addStyleName("cell-alt");
         return l;
     }
 
     public static Label cellLabel(Boolean b) {
         Label l = new Label(b.toString());
         l.addStyleName("cell-alt");
         return l;
     }
 
     public static Label cellLabel(int i) {
         return cellLabel(Integer.toString(i));
     }
 
     public static Label longLabel(Long l) {
         return new Label(Long.toString(l));
     }
 
     public static String stringIsBold(String s, boolean isBold) {
         String w = (isBold) ? "bold" : "normal";
         return "<span style=\"font-weight:" + w + ";\">" + s + "</span>";
     }
 
 
     public static WidgetComponent clearPixel(String height, String width, String tooltip) {
         // can be used as a spacer or as a tooltip for grid cells, etc.
         Image i = new Image("images/clearpixel.gif");
         i.setHeight(height);
         i.setWidth(width);
         WidgetComponent w = new WidgetComponent(i);
         if (tooltip != null) {
             w.setToolTip(tooltip);
         }
         return w;
     }
 
     public static WidgetComponent deleteImage(String tooltip) {
         if (tooltip == null) {
             tooltip = "Remove this item";
         }
         return newImage("images/delete_config.gif", tooltip);
     }
 
     public static WidgetComponent restoreImage(String tooltip) {
         if (tooltip == null) {
             tooltip = "Restore";
         }
         return newImage("images/recycle_icon.gif", tooltip);
     }
 
     public static WidgetComponent newImage(String path, String tooltip) {
         Image i = new Image(path);
         WidgetComponent w = new WidgetComponent(i);
         if (tooltip != null) {
             w.setToolTip(tooltip);
         }
         return w;
     }
 
     public static TableData colspan(int value) {
         TableData td = new TableData();
         td.setColspan(value);
         return td;
     }
 
 
     public static TableData paddedCell(int value) {
         TableData td = new TableData();
         td.setPadding(value);
         return td;
     }
 
 
     /**
      * @param heading
      * @param items
      * @return
      */
     public static ContentPanel createPanelWithListView(String heading, List<NavMenuItem> items) {
         ContentPanel c = new ContentPanel();
         c.addStyleName("no-border");
        c.setBorders(false);
         c.setBodyBorder(false);
         c.setHeading(heading);
         c.setAutoHeight(true);
         c.setAutoHeight(true);
 
         // store for all menu items in container
         ListStore<NavMenuItem> ls = new ListStore<NavMenuItem>();
         ls.add(items);
 
         ListView<NavMenuItem> lv = new ListView<NavMenuItem>();
         lv.setStyleName("no-border");
         lv.setDisplayProperty("title"); // from item
         lv.setStore(ls);
 
         for (final NavMenuItem item : ls.getModels()) {
 
             lv.addListener(Events.Select, new Listener<BaseEvent>() {
                 public void handleEvent(BaseEvent be) {
                     ListViewEvent lve = (ListViewEvent) be;
                     NavMenuItem nmi = (NavMenuItem) lve.getModel();
                     History.newItem(nmi.getTokenBase());
                 }
             });
 
             // double click gives us the "add form"
             if (item.getFormPanel() != null) {
                 lv.addListener(Events.DoubleClick, new Listener<BaseEvent>() {
                     public void handleEvent(BaseEvent be) {
                         ListViewEvent lve = (ListViewEvent) be;
                         NavMenuItem nmi = (NavMenuItem) lve.getModel();
                         History.newItem(nmi.getTokenBase() + NavMenuItem.NEW);
                     }
                 });
             }
         }
         c.add(lv);
         return c;
     }
 
     public static String noneIfNull(String s) {
         if (Util.isEmptyString(s)) {
             return "None";
         }
         return s;
     }
 
     public static Label navSeparator() {
         return new Label(">");
     }
 
 }
