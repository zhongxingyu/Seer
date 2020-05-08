 package org.mule.galaxy.web.client.artifact;
 
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Iterator;
 
 import org.mule.galaxy.web.client.AbstractComposite;
 import org.mule.galaxy.web.client.RegistryPanel;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.ExtendedArtifactInfo;
 import org.mule.galaxy.web.rpc.WProperty;
 
 public class ArtifactMetadataPanel extends AbstractComposite {
 
     private FlowPanel metadata;
     private RegistryPanel registryPanel;
     private ExtendedArtifactInfo info;
     private FlexTable table;
     
     public ArtifactMetadataPanel(final RegistryPanel registryPanel,
                                  final ExtendedArtifactInfo info) {
         super();
         this.info = info;
         this.registryPanel = registryPanel;
         
         metadata = new FlowPanel();
         metadata.setStyleName("metadata-panel");
         
         table = createColumnTable();
         
         Hyperlink addMetadata = new Hyperlink("Add", "add-metadata");
         final ArtifactMetadataPanel amPanel = this;
         addMetadata.addClickListener(new ClickListener() {
 
             public void onClick(Widget arg0) {
                 PropertyEditPanel edit = new PropertyEditPanel(registryPanel, 
                                                                info.getId(),
                                                                metadata,
                                                                amPanel,
                                                                table);
                 metadata.add(edit);   
             }
             
         });
         
         InlineFlowPanel metadataTitle = createTitleWithLink("Metadata", addMetadata);
         metadata.add(metadataTitle);
 
         
         int i = 0;
         for (Iterator itr = info.getProperties().iterator(); itr.hasNext();) {
             WProperty p = (WProperty) itr.next();
             
             createPropertyRow(i, p);
             
             i++;
         }
         styleHeaderColumn(table);
         metadata.add(table);
         initWidget(metadata);
     }
     
     private void createPropertyRow(final int row, 
                                    final WProperty p) {
         table.setText(row, 0, p.getDescription());
         final String name = p.getName();
         final String value = p.getValue();
         final boolean locked = p.isLocked();
         
         setRow(row, name, value, locked);
         
     }
 
     private void setRow(final int row, final String name, final String value, final boolean locked) {
         String txt = value;
         Widget w = null;
         if (locked) {
             if ("".equals(txt) || txt == null) {
                 txt = "[no value]";
             }
             txt += " ";
             InlineFlowPanel panel = new InlineFlowPanel();
             panel.add(new Label(txt));
             panel.add(new Image("./images/lockedstate.gif"));
             w = panel;
         } else {
             txt += " ";
             Hyperlink editHL = new Hyperlink("Edit", "edit-property");
             editHL.setStyleName("propertyLink");
             editHL.addClickListener(new ClickListener() {
 
                 public void onClick(Widget widget) {
                     edit(name, value, row);
                  }
                 
             });
             
             Hyperlink deleteHL = new Hyperlink("Delete", "delete-property");
             deleteHL.setStyleName("propertyLink");
             deleteHL.addClickListener(new ClickListener() {
 
                 public void onClick(Widget widget) {
                    delete(name, row);
                 }
                 
             });
             
             InlineFlowPanel valuePanel = new InlineFlowPanel();
             valuePanel.add(new Label(txt));
             valuePanel.add(editHL);
             valuePanel.add(deleteHL);
             w = valuePanel;
         }
         
         table.setWidget(row, 1, w);
        table.getCellFormatter().setStyleName(row, 0, "artifactTableHeader");
         table.getCellFormatter().setStyleName(row, 1, "artifactTableEntry");
     }
 
 
     protected void edit(final String name, final String value, final int row) {
         InlineFlowPanel editPanel = new InlineFlowPanel();
         
         final TextBox valueTB = new TextBox();
         valueTB.setText(value);
         valueTB.setVisibleLength(50);
         editPanel.add(valueTB);
         
         final Button cancel = new Button("Cancel");
         cancel.addClickListener(new ClickListener() {
 
             public void onClick(Widget arg0) {
                 table.clearCell(row, 1);
                 setRow(row, name, value, false);
             }
             
         });
         
         final Button save = new Button("Save");
         save.addClickListener(new ClickListener() {
 
             public void onClick(Widget arg0) {
                 cancel.setEnabled(false);
                 save.setEnabled(false);
                 
                 save(name, valueTB.getText(), row, cancel, save);
             }
             
         });
         editPanel.add(cancel);
         editPanel.add(save);
         
         table.setWidget(row, 1, editPanel);
     }
 
     protected void save(final String name, final String value, final int row, 
                         final Button cancel, final Button save) {
         registryPanel.getRegistryService().setProperty(info.getId(), name, value, new AbstractCallback(registryPanel) {
 
             public void onFailure(Throwable caught) {
                 cancel.setEnabled(true);
                 save.setEnabled(true);
                 
                 super.onFailure(caught);
             }
 
             public void onSuccess(Object arg0) {
                 table.clearCell(row, 1);
                 setRow(row, name, value, false);
             }
             
         });
     }
 
 
     protected void delete(String name, final int row) {
         registryPanel.getRegistryService().deleteProperty(info.getId(), name, new AbstractCallback(registryPanel) {
 
             public void onSuccess(Object arg0) {
                 table.removeRow(row);
             }
             
         });
     }
 
     public void addProperty(String name, String desc, String value) {
         int rows = table.getRowCount();
         table.setText(rows, 0, desc);
         
         setRow(rows, name, value, false);
     }
 }
