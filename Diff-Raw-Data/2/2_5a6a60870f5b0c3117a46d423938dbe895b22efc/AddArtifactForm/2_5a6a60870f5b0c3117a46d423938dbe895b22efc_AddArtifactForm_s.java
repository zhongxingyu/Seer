 package org.mule.galaxy.repository.client.item;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mule.galaxy.repository.rpc.ItemInfo;
 import org.mule.galaxy.repository.rpc.RegistryServiceAsync;
 import org.mule.galaxy.repository.rpc.WType;
 import org.mule.galaxy.web.client.util.AbstractErrorHandlingPopup;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.ItemExistsException;
 import org.mule.galaxy.web.rpc.ItemNotFoundException;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FileUploadField;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.google.gwt.core.client.GWT;
 
 public class AddArtifactForm extends AbstractErrorHandlingPopup {
 
     private String fileId;
     private List<WType> types;
     private Button closeBtn;
     private Button submitBtn;
 
     //private ProgressBar bar;
     private FormData formData;
     private TextField<String> fname;
     private TextField<String> fversion;
     private final ItemInfo parent;
     private final boolean parentIsWorkspace;
     private FileUploadField file;
     private final ChildItemsPanel itemsPanel;
     private final RegistryServiceAsync registryService;
 
     public AddArtifactForm(final RegistryServiceAsync registryService,
                            ItemInfo parent,
                            boolean parentIsWorkspace, 
                            ChildItemsPanel itemsPanel) {
         super();
         this.registryService = registryService;
         this.parent = parent;
         this.parentIsWorkspace = parentIsWorkspace;
         this.itemsPanel = itemsPanel;
         
         formData = new FormData("-20");
         fpanel.setAction(GWT.getModuleBaseURL() + "../artifactUpload.form");
 
         FieldSet fieldSet = new FieldSet();
         fieldSet.setHeading("Add New Artifact");
 
         FormLayout layout = new FormLayout();
         layout.setLabelWidth(75);
         fieldSet.setLayout(layout);
 
         file = new FileUploadField();
         file.setAllowBlank(false);
         file.setFieldLabel("File");
         file.setName("file");
         fieldSet.add(file, formData);
 
         fpanel.add(fieldSet);
 
         if (parentIsWorkspace) {
             fname = new TextField<String>();
             fname.setFieldLabel("Name");
             fname.setAllowBlank(true);
 //            fname.setName(this.getTypeByName("Artifact").getId());
             fieldSet.add(fname, formData);
         }
         
         fversion = new TextField<String>();
         fversion.setFieldLabel("Version");
         fversion.setAllowBlank(false);
 //        fversion.setName(this.getTypeByName("Artifact Version").getId());
         fversion.setValue("1");
         fieldSet.add(fversion, formData);
 
         fpanel.add(fieldSet);
         
         closeBtn = new Button("Close");
         closeBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
                 hide();
             }
         });
 
         submitBtn = new Button("Add");
         submitBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
             @Override
             public void componentSelected(ButtonEvent ce) {
 
                 if (!fpanel.isValid()) {
                     return;
                 }
 
                 submitBtn.setText("Uploading...");
                 setEnabled(false);
 
                 // upload the artifact first
                 fpanel.submit();
             }
         });
 
         // pre submit - start the progress bar....
         /*
         fpanel.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
 
             public void handleEvent(FormEvent fe) {
 
                 // begin upload progress bar...
                 box = MessageBox.progress("Please wait", "Uploading item...", "Initializing...");
                 bar = box.getProgressBar();
 
                 // we need to query the size put on the server...
                 // use a timer as a placeholder for now.
                 final Timer t = new Timer() {
                     float i;
 
                     @Override
                     public void run() {
                         bar.updateProgress(i / 100, (int) i + "% Complete");
                         i += 50;
                         if (i > 500) {
                             cancel();
                             box.close();
                         }
                     }
                 };
                 t.scheduleRepeating(500);
             }
 
         });
         */
 
         // post submit processing
         fpanel.addListener(Events.Submit, new Listener<FormEvent>() {
             public void handleEvent(final FormEvent fe) {
 
                 registryService.getTypes(new AbstractCallback<List<WType>>(AddArtifactForm.this) {
                     public void onSuccess(List<WType> types) {
                         AddArtifactForm.this.submit(fe, types);
                     }
                 });
             }
         });
 
         fpanel.addButton(submitBtn);
         fpanel.addButton(closeBtn);
     }
 
     protected void submit(FormEvent fe, List<WType> types) {
         this.types = types;
         
         // hardcode the types we are using
         WType artifactType = getTypeByName("Artifact");
         WType avType = getTypeByName("Artifact Version");
 
         // determine the filed
         fileId = determineFileId(fe.getResultHtml());
 
 
         Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
         versionProperties.put("artifact", fileId);
         
         AbstractCallback callback = new AbstractCallback(this) {
             public void onSuccess(Object id) {
                 itemsPanel.refresh();
                 hide();
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 setEnabled(true);
                 if (caught instanceof ItemExistsException) {
                     AddArtifactForm.this.setMessage("An item with that name already exists.");
                 } else if (caught instanceof ItemNotFoundException) {
                     AddArtifactForm.this.setMessage("A workspace with that name could not be found.");
                 } else {
                     super.onFailure(caught);
                 }
             }
         };
         
         if (parentIsWorkspace) {
             String parentPath = parent != null ? parent.getPath() : "/";
 
             String name = fname.getRawValue();
 
            if (name == null || name.isEmpty()) {
                 name = file.getValue();
 
                 if (name.contains("/")) {
                     String[] split = name.split("/");
 
                     name = split[split.length - 1];
                 }
 
                 if (name.contains("\\")) {
                     String[] split = name.split("\\\\");
 
                     name = split[split.length - 1];
                 }
             }
 
             registryService.addVersionedItem(parentPath,
                                              name,
                                              fversion.getRawValue(),
                                              null,
                                              artifactType.getId(),
                                              avType.getId(),
                                              new HashMap<String, Serializable>(),
                                              versionProperties, 
                                              callback);
         } else {
             String parentId = parent != null ? parent.getPath() : null;
             registryService.addItem(parentId, fversion.getRawValue(), null, 
                                     avType.getId(), versionProperties, callback);
         }
         
     }
 
     private WType getTypeByName(String name) {
         WType artifact = null;
         for (WType type : types) {
             if (name.equals(type.getName())) {
                 artifact = type;
             }
         }
         return artifact;
     }
 
     private String determineFileId(String msg) {
         // some platforms insert css info into the pre-tag -- just remove it all
         msg = msg.replaceAll("\\<.*?\\>", "");
 
         String theFileId = null;
 
         // This is our 200 OK response
         // eg:  OK 9c495a52-4a07-4697-ba73-f94f95cd3020
         if (msg.startsWith("OK ")) {
             theFileId = msg.substring(3);
         } else {
             this.setMessage(msg);
         }
         return theFileId;
     }
 
 
     private void setEnabled(boolean enabled) {
         submitBtn.setEnabled(enabled);
         closeBtn.setEnabled(enabled);
         if (enabled) {
             submitBtn.setText("Add");
         }
     }
 
 }
