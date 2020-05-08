 package org.mule.galaxy.web.client.property;
 
 import com.google.gwt.user.client.ui.*;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import org.mule.galaxy.web.client.AbstractComposite;
 import org.mule.galaxy.web.client.ErrorPanel;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.util.ConfirmDialog;
 import org.mule.galaxy.web.client.util.ConfirmDialogAdapter;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.client.util.LightBox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WProperty;
 
 /**
  * Encapsulates the rendering and editing of a property value.
  */
 public class EditPropertyPanel extends AbstractComposite {
 
     private Button save;
     protected Button cancel;
     private AbstractPropertyRenderer renderer;
     protected InlineFlowPanel panel;
     protected ErrorPanel errorPanel;
     protected String itemId;
     protected WProperty property;
     protected Galaxy galaxy;
     protected ClickListener saveListener;
     protected ClickListener deleteListener;
     protected ClickListener cancelListener;
 
     public EditPropertyPanel(AbstractPropertyRenderer renderer) {
         super();
         
         this.panel = new InlineFlowPanel();
 
         initWidget(panel);
         this.renderer = renderer;
     }
 
     public void initialize() {
         initializeRenderer();
     }
     
     public InlineFlowPanel createViewPanel() {
         Image editImg = new Image("images/page_edit.gif");
         editImg.setStyleName("icon-baseline");
         editImg.setTitle("Edit");
         editImg.addClickListener(new ClickListener() {
 
             public void onClick(Widget widget) {
                 showEdit();
             }
 
         });
 
 
         Image deleteImg = new Image("images/delete_config.gif");
         deleteImg.setStyleName("icon-baseline");
         deleteImg.setTitle("Delete");
         deleteImg.addClickListener(new ClickListener() {
 
             public void onClick(Widget widget) {
                 delete();
             }
 
         });
 
         InlineFlowPanel viewPanel = new InlineFlowPanel();
         viewPanel.add(renderer.createViewWidget());
 
         if (!property.isLocked()) {
             // interesting... spacer label has to be a new object ref, otherwise not honored...
             viewPanel.add(new Label(" "));
             viewPanel.add(editImg);
             viewPanel.add(new Label(" "));
             viewPanel.add(deleteImg);
         }
         return viewPanel;
     }
 
     protected FlowPanel createEditPanel() {
         FlowPanel editPanel = new FlowPanel();
         editPanel.setStyleName("add-property-inline");
 
         Widget editForm = renderer.createEditForm();
         editPanel.add(editForm);
         
         FlowPanel buttonPanel = new FlowPanel();
         cancel = new Button("Cancel");
         cancel.addClickListener(new ClickListener() {
 
             public void onClick(Widget arg0) {
                 cancel();
             }
             
         });
         
         if (cancelListener != null) {
             cancel.addClickListener(cancelListener);
         }
         
         save = new Button("Save");
         save.addClickListener(new ClickListener() {
 
             public void onClick(Widget arg0) {
                 cancel.setEnabled(false);
                 save.setEnabled(false);
                 
                 save();
             }
             
         });
         
         buttonPanel.add(save);
         buttonPanel.add(cancel);
 
         editPanel.add(buttonPanel);
         
         return editPanel;
     }
 
     protected void cancel() {
         initializeRenderer();
         showView();
     }
     
     public void showView() {
         panel.clear();
         panel.add(createViewPanel());
     }
     
     protected void delete() {
         final ConfirmDialog dialog = new ConfirmDialog(new ConfirmDialogAdapter() {
             public void onConfirm() {
                 doDelete();
             }
         }, "Are you sure you want to delete this property?");
         
         new LightBox(dialog).show();
     }
     
     protected void doDelete() {
         galaxy.getRegistryService().deleteProperty(itemId, property.getName(), new AbstractCallback(errorPanel) {
 
             public void onSuccess(Object arg0) {
                 deleteListener.onClick(null);
             }
             
         });
     }
     
     public void showEdit() {
         panel.clear();
 
         FlowPanel editPanel = createEditPanel();
         panel.add(editPanel);
     }   
 
     protected void save() {
         final Serializable value = (Serializable) renderer.getValueToSave();
         
         AbstractCallback saveCallback = getSaveCallback(value);
         
         setEnabled(false);
         
         renderer.save(itemId, property.getName(), value, saveCallback);
             
     }
 
     protected AbstractCallback getSaveCallback(final Serializable value) {
         AbstractCallback saveCallback = new AbstractCallback(errorPanel) {
 
             public void onFailure(Throwable caught) {
                 onSaveFailure(caught, this);
             }
 
             public void onSuccess(Object response) {
                 onSave(value, response);
             }
             
         };
         return saveCallback;
     }
 
     protected void onSave(final Serializable value, Object response) {
         setEnabled(true);
         property.setValue(value);
 
         initializeRenderer();
         
         showView();
         
         if (saveListener != null) {
             saveListener.onClick(save);
         }
     }
 
     private void initializeRenderer() {
         renderer.initialize(galaxy, errorPanel, property.getValue(), false);
     }
     
     protected void onSaveFailure(Throwable caught, AbstractCallback saveCallback) {
         saveCallback.onFailureDirect(caught);
     }
 
     public WProperty getProperty() {
         return property;
     }
 
     public void setProperty(WProperty property) {
         this.property = property;
     }
     
     public void setGalaxy(Galaxy galaxy) {
         this.galaxy = galaxy;
     }
 
     public void setErrorPanel(ErrorPanel errorPanel) {
         this.errorPanel = errorPanel;
     }
 
     public void setItemId(String entryid) {
         this.itemId = entryid;
     }
 
     public void setEnabled(boolean b) {
         if (cancel != null) {
             cancel.setEnabled(b);
         }
         if (save != null) {
             save.setEnabled(b);
         }
     }
 
     public void setSaveListener(ClickListener saveListener) {
         this.saveListener = saveListener;
     }
 
     public void setDeleteListener(ClickListener deleteListener) {
         this.deleteListener = deleteListener;
     }
     public void setCancelListener(ClickListener cancelListener) {
         this.cancelListener = cancelListener;
     }
 }
