 package org.vaadin.risto.mockupcontainer;
 
 import java.util.Iterator;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.server.Page;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Tree;
 import com.vaadin.ui.TreeTable;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * @author Risto Yrjänä / Vaadin
  */
 public class MockupContainerDemoUI extends UI {
 
     private static final long serialVersionUID = -6202206077403997141L;
 
     private MockupContainer mockupContainer;
 
     private Table table;
 
     private Tree tree;
 
     // private Tree millerColumns;
 
     private TreeTable treeTable;
 
     protected boolean baconMode;
 
     @Override
     protected void init(VaadinRequest request) {
         Page.getCurrent().setTitle("MockupContainer demo");
         VerticalLayout mainLayout = new VerticalLayout();
         mainLayout.setSpacing(true);
         mainLayout.setMargin(true);
         setContent(mainLayout);
 
         mockupContainer = new MockupContainer();
 
         initComponents(mainLayout);
 
         if (request.getParameter("bacon") != null && !baconMode) {
             enterBaconMode();
         } else {
             exitBaconMode();
         }
     }
 
     protected void enterBaconMode() {
         MockupDataSet baconDataSet = new BaconDataSet();
         MockupFactory.setDefaultDataSet(baconDataSet);
         mockupContainer.setDataSet(baconDataSet);
         Notification.show("Bacon mode activated",
                 Notification.Type.TRAY_NOTIFICATION);
         updateComponents();
     }
 
     protected void exitBaconMode() {
         MockupDataSet defaultDataSet = new DefaultDataSet();
         MockupFactory.setDefaultDataSet(defaultDataSet);
         mockupContainer.setDataSet(defaultDataSet);
         updateComponents();
 
     }
 
     @SuppressWarnings("unchecked")
     protected void initComponents(VerticalLayout mainLayout) {
         Object captionPropertyId = mockupContainer.getContainerPropertyIds()
                 .iterator().next();
 
        Panel tablePanel = new Panel("Table (Vaadin 7.0 RC2)");
         VerticalLayout tablePanelContent = new VerticalLayout();
         tablePanel.setContent(tablePanelContent);
         tablePanel.setSizeUndefined();
         table = new Table();
         table.setContainerDataSource(mockupContainer);
         table.addValueChangeListener(new ValueListener(table));
         table.setImmediate(true);
         table.setSelectable(true);
         tablePanelContent.addComponent(table);
 
        Panel treePanel = new Panel("Tree (Vaadin 7.0 RC2)");
         VerticalLayout treePanelContent = new VerticalLayout();
         treePanel.setContent(treePanelContent);
         treePanel.setSizeUndefined();
         tree = new Tree();
         tree.setContainerDataSource(mockupContainer);
         tree.addValueChangeListener(new ValueListener(tree));
         tree.setImmediate(true);
         tree.setItemCaptionPropertyId(captionPropertyId);
         treePanelContent.addComponent(tree);
 
         Panel millerPanel = new Panel(
                 "Miller Columns (1.0.1) disabled until migrated to Vaadin 7");
         // millerColumns = new MillerColumns();
         // millerColumns.setContainerDataSource(mockupContainer);
         // millerColumns.addListener(new ValueListener(millerColumns));
         // millerColumns.setImmediate(true);
         // millerColumns.setItemCaptionPropertyId(captionPropertyId);
         // millerPanel.addComponent(millerColumns);
 
        Panel treeTablePanel = new Panel("TreeTable Vaadin 7.0 RC2");
         VerticalLayout treeTablePanelContent = new VerticalLayout();
         treeTablePanel.setContent(treeTablePanelContent);
         treeTablePanel.setSizeUndefined();
         treeTable = new TreeTable();
         treeTable.setContainerDataSource(mockupContainer);
         treeTable.addValueChangeListener(new ValueListener(treeTable));
         treeTable.setImmediate(true);
         treeTablePanelContent.addComponent(treeTable);
 
         HorizontalLayout top = new HorizontalLayout();
         top.setSpacing(true);
         top.setMargin(true);
 
         final TextField itemCountField = createIntegerField("Item count", "20");
 
         final TextField propertyCountField = createIntegerField(
                 "Property count", "5");
 
         final TextField childCountField = createIntegerField("Child count", "5");
 
         final TextField delayField = createIntegerField("Delay (ms)", "0");
 
         Button update = new Button("Update");
         update.addClickListener(new Button.ClickListener() {
 
             private static final long serialVersionUID = -5057468430709178935L;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 Integer delay = Integer.valueOf((delayField.getValue()));
                 mockupContainer.setItemDelay(delay);
                 Integer itemCount = Integer.valueOf((itemCountField.getValue()));
                 mockupContainer.setItemCount(itemCount);
                 Integer propertyCount = Integer.valueOf((propertyCountField
                         .getValue()));
                 mockupContainer.setPropertyCount(propertyCount);
                 Integer childCount = Integer.valueOf((childCountField
                         .getValue()));
                 mockupContainer.setNumberOfChildren(childCount);
 
                 table.setContainerDataSource(mockupContainer);
 
                 Iterator<String> iterator = mockupContainer
                         .getContainerPropertyIds().iterator();
                 if (iterator.hasNext()) {
                     String captionId = iterator.next();
                     tree.setItemCaptionPropertyId(captionId);
                     // millerColumns.setItemCaptionPropertyId(captionId);
                 }
             }
         });
 
         top.addComponent(itemCountField);
         top.addComponent(propertyCountField);
         top.addComponent(childCountField);
         top.addComponent(delayField);
         top.addComponent(update);
         top.setComponentAlignment(update, Alignment.BOTTOM_RIGHT);
 
         ComboBox containerCombo = new ComboBox();
         containerCombo.setImmediate(true);
         containerCombo.addContainerProperty("caption", String.class, null);
         containerCombo.setItemCaptionPropertyId("caption");
 
         containerCombo.addItem("default").getItemProperty("caption")
                 .setValue("Default container");
         containerCombo.addItem("slow").getItemProperty("caption")
                 .setValue("Slow container, default size");
         containerCombo.addItem("medium").getItemProperty("caption")
                 .setValue("Medium-sized container, 1000 items");
         containerCombo.addItem("large").getItemProperty("caption")
                 .setValue("Large container, 20000 items");
         // containerCombo.addItem("gigantic").getItemProperty("caption").setValue(
         // "Gigantic container, 500000 items");
 
         containerCombo
                 .addValueChangeListener(new Property.ValueChangeListener() {
 
                     private static final long serialVersionUID = -2244028093424731839L;
 
                     @Override
                     public void valueChange(ValueChangeEvent event) {
                         String value = (String) event.getProperty().getValue();
 
                         if ("default".equals(value)) {
                             mockupContainer = MockupFactory
                                     .getDefaultContainer();
                         } else if ("slow".equals(value)) {
                             mockupContainer = MockupFactory
                                     .getSlowDefaultContainer();
                         } else if ("medium".equals(value)) {
                             mockupContainer = MockupFactory
                                     .getMediumContainer();
                         } else if ("large".equals(value)) {
                             mockupContainer = MockupFactory.getLargeContainer();
                         } else if ("gigantic".equals(value)) {
                             mockupContainer = MockupFactory
                                     .getGiganticContainer();
                         }
 
                         updateComponents();
 
                         itemCountField.setValue(Integer
                                 .toString(mockupContainer.getItemCount()));
                         propertyCountField.setValue(Integer
                                 .toString(mockupContainer.getPropertyCount()));
                         childCountField.setValue(Integer
                                 .toString(mockupContainer.getNumberOfChildren()));
                         delayField.setValue(Integer.toString(mockupContainer
                                 .getGetItemDelay()));
                     }
                 });
 
         Panel topPanel = new Panel("MockupContainer properties");
         VerticalLayout topPanelContent = new VerticalLayout();
         topPanel.setContent(topPanelContent);
         topPanel.setSizeUndefined();
 
         topPanelContent
                 .addComponent(new Label(
                         "All the UI-components use the same container. The attributes of the container can be changed here."));
         topPanelContent.addComponent(top);
 
         topPanelContent
                 .addComponent(new Label(
                         "You can also change the container by selecting a pre-defined size from the drop-down."));
         topPanelContent.addComponent(containerCombo);
 
         mainLayout.addComponent(topPanel);
         mainLayout.addComponent(tablePanel);
         mainLayout.addComponent(treePanel);
         mainLayout.addComponent(millerPanel);
         mainLayout.addComponent(treeTablePanel);
 
        // ((AbstractOrderedLayout) getMainWindow().getContent())
        // .setComponentAlignment(topPanel, Alignment.TOP_CENTER);
        // ((AbstractOrderedLayout) getMainWindow().getContent())
        // .setComponentAlignment(tablePanel, Alignment.TOP_CENTER);
        // ((AbstractOrderedLayout) getMainWindow().getContent())
        // .setComponentAlignment(treePanel, Alignment.TOP_CENTER);
        // ((AbstractOrderedLayout) getMainWindow().getContent())
        // .setComponentAlignment(treeTablePanel, Alignment.TOP_CENTER);
     }
 
     private TextField createIntegerField(String caption, String value) {
        final TextField field = new TextField("Property count");
         field.setConverter(Integer.class);
         field.setConversionError("Integers only");
         field.setValue(value);
 
         return field;
     }
 
     private class ValueListener implements Property.ValueChangeListener {
 
         private final Container container;
 
         public ValueListener(Container container) {
             this.container = container;
 
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * com.vaadin.data.Property.ValueChangeListener#valueChange(com.vaadin
          * .data.Property.ValueChangeEvent)
          */
         private static final long serialVersionUID = -4751362452099946595L;
 
         @Override
         public void valueChange(ValueChangeEvent event) {
             Object itemId = event.getProperty().getValue();
             if (itemId != null) {
                 Item item = container.getItem(itemId);
                 StringBuffer sb = new StringBuffer();
                 sb.append("Item id: ");
                 sb.append(itemId);
                 sb.append(" Property ids: ");
                 sb.append(item.getItemPropertyIds());
 
                 for (Object propId : item.getItemPropertyIds()) {
                     sb.append(" [ ");
                     sb.append(" Property id: ");
                     sb.append(propId);
                     sb.append(" Value: ");
                     sb.append(container.getContainerProperty(itemId, propId)
                             .getValue());
                     sb.append(" ] ");
                 }
                 Notification.show("Selected", sb.toString(),
                         Notification.Type.HUMANIZED_MESSAGE);
             }
         }
     }
 
     private void updateComponents() {
         table.setContainerDataSource(mockupContainer);
         tree.setContainerDataSource(mockupContainer);
         // millerColumns.setContainerDataSource(mockupContainer);
         treeTable.setContainerDataSource(mockupContainer);
 
         Iterator<String> iterator = mockupContainer.getContainerPropertyIds()
                 .iterator();
         if (iterator.hasNext()) {
             Object captionId = iterator.next();
             tree.setItemCaptionPropertyId(captionId);
             // millerColumns.setItemCaptionPropertyId(captionId);
         }
     }
 
 }
