 package org.mule.galaxy.web.client;
 
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.ToolbarButton;
 import org.mule.galaxy.web.client.util.ToolbarButtonEvent;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.WidgetComponent;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 
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
 
     public static FlexTable createTitledRowTable(Panel panel, String title) {
         panel.add(createPrimaryTitle(title));
         FlexTable table = createRowTable();
         panel.add(table);
         return table;
     }
 
     public static FlexTable createTitledColumnTable(Panel panel, String title) {
         panel.add(createTitle(title));
         FlexTable table = createColumnTable();
         panel.add(table);
         return table;
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
 
 
     public static ContentPanel createAccodionWrapperPanel() {
         ContentPanel accordionPanel = new ContentPanel(new AccordionLayout());
         accordionPanel.setCollapsible(false);
         accordionPanel.setHeaderVisible(false);
         accordionPanel.setAutoHeight(true);
         accordionPanel.setAutoWidth(true);
         return accordionPanel;
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
 
     public static WidgetComponent restoreImage(String tooltip) {
         Image i = new Image("images/recycle_icon.gif");
         WidgetComponent w = new WidgetComponent(i);
         w.setToolTip(tooltip == null ? "Restore" : tooltip);
         return w;
     }
 
     // can be used as a spacer or as a tooltip for grid cells, etc.
 
     public static WidgetComponent clearPixel(String height, String width, String tooltip) {
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
         return deleteImage(tooltip, null);
     }
 
     public static WidgetComponent deleteImage(String tooltip, ClickHandler handler) {
         Image i = new Image("images/delete_config.gif");
         if (handler != null) {
             i.addClickHandler(handler);
         }
         WidgetComponent w = new WidgetComponent(i);
         w.setToolTip(tooltip == null ? "Click to Remove Item" : tooltip);
         return w;
     }
 
     public static TableData colspan(int value) {
         TableData td = new TableData();
         td.setColspan(value);
         return td;
     }
 
 
 }
