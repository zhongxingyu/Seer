 package org.iucn.sis.client.panels.references;
 
 import java.util.ArrayList;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.shared.api.acl.InsufficientRightsException;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.citations.ReferenceUtils;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.event.WindowEvent;
 import com.extjs.gxt.ui.client.event.WindowListener;
 import com.extjs.gxt.ui.client.widget.Dialog;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.google.gwt.user.client.ui.HTML;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class ReferenceViewTabPanel extends TabPanel implements ReferenceViewAPI {
 	
 	private Referenceable curReferenceable;
 	private GenericCallback<Object> addCallback, removeCallback;
 	
 	private final GenericCallback<Object> defaultCallback = new GenericCallback<Object>() {
 		public void onFailure(Throwable caught) {
 			WindowUtils.errorAlert("Error!", "Error committing changes to the "
 					+ "server. Ensure you are connected to the server, then try " + "the process again.");
 		}
 
 		public void onSuccess(Object result) {
 			WindowUtils.infoAlert("Success!", "Successfully committed reference " + "changes.");
 		}
 	};
 	
 	private BibliographyViewTab bibliography;
 	private ReferenceSearchViewTab search;
 	
 	public ReferenceViewTabPanel() {
 		super();
 		
 		final TabItem bibTab = new TabItem();
 		bibTab.setLayout(new FillLayout());
 		bibTab.setText("Bibliography");
 		bibTab.add(bibliography = new BibliographyViewTab(this));
 		
 		add(bibTab);
 		
 		final TabItem searchTab = new TabItem();
 		searchTab.setLayout(new FillLayout());
 		searchTab.setText("Reference Search");
 		searchTab.add(search = new ReferenceSearchViewTab(this));
 		
 		add(searchTab);
 	}
 	
 	public void showSearchTab() {
 		setSelection(getItem(1));
 	}
 	
 	@Override
 	public Referenceable getReferenceable() {
 		return curReferenceable;
 	}
 	
 	/**
 	 * To override, called when user clicks "Add Selected" button. Treat as a
 	 * callback
 	 * 
 	 * @param selectedValues
 	 *            the selected values from the table.
 	 */
 	public void onAddSelected(ArrayList<Reference> selectedValues) {
 		if (curReferenceable != null) {
 			curReferenceable.addReferences(selectedValues, new GenericCallback<Object>() {
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Error!", "Error committing changes to the "
 							+ "server. Ensure you are connected to the server, then try " + "the process again.");
 					
 					bibliography.setReferences(curReferenceable);
 					search.refreshView();
 				}
 				public void onSuccess(Object result) {
 					WindowUtils.infoAlert("Success!", "Successfully committed reference changes.");
 					
 					bibliography.setReferences(curReferenceable);
 					search.refreshView();
 				}
 			});
 		}
 	}
 	
 	public void onRemoveSelected(ArrayList<Reference> selectedValues) {
 		if (curReferenceable != null) {
 			curReferenceable.removeReferences(selectedValues, removeCallback);
 			setReferences(curReferenceable, addCallback, removeCallback);
 		}
 	}
 	
 	public void setReferences(Referenceable referenceable) {
 		setReferences(referenceable, null, null);
 	}
 	
 	public void setReferences(Referenceable referenceable, GenericCallback<Object> addCallback,
 			GenericCallback<Object> removeCallback) {
 		curReferenceable = referenceable;
 
 		this.addCallback = addCallback == null ? defaultCallback : addCallback;
 		this.removeCallback = removeCallback == null ? defaultCallback : removeCallback;
 		
 		if (curReferenceable == null) {
 			getItem(0).setEnabled(false);
 			setSelection(getItem(1));
 		}
 		else {
 			getItem(0).setEnabled(true);
 			setSelection(getItem(0));
 			bibliography.setReferences(referenceable);
 		}
 	}
 	
 	
 	/**
 	 * Opens a ReferenceEditor instance.
 	 * 
 	 * @param reference
 	 *            the reference to edit, or null to create a new one
 	 * @param promptToReplace TODO
 	 */
 	public static void openEditor(final ReferenceModel reference, final ComplexListener<ReferenceModel> saveListener, final boolean promptToReplace, final boolean local) {
 		openEditor(reference, saveListener, null, promptToReplace, local);
 	}
 	
 	public static void openEditor(final ReferenceModel reference, final ComplexListener<ReferenceModel> saveListener, final SimpleListener deleteListener, final boolean promptToReplace, final boolean local) {
 		final MyReferenceEditor editor = new MyReferenceEditor(reference, promptToReplace, local);
 		editor.setHeading(reference == null ? "New Reference" : "Edit Reference");
 		editor.setDeleteListener(deleteListener);
 		editor.setSaveListener(saveListener);
 		
 		//editor.show();
 	}
 	
 	private static class MyReferenceEditor extends ReferenceEditor {
 		
 		private final ReferenceModel reference;
 		private final boolean promptToReplace;
 		private final boolean local;
 		
 		private SimpleListener deleteListener;
 		private ComplexListener<ReferenceModel> saveListener;
 		
 		public MyReferenceEditor(ReferenceModel reference, boolean promptToReplace, boolean local) {
 			super(reference == null ? null : reference.ref);
 			this.reference = reference;
 			this.promptToReplace = promptToReplace;
 			this.local = local;
 		}
 		
 		public void setDeleteListener(SimpleListener deleteListener) {
 			this.deleteListener = deleteListener;
 		}
 		
 		public void setSaveListener(ComplexListener<ReferenceModel> saveListener) {
 			this.saveListener = saveListener;
 		}
 
 		@Override
 		protected void afterDelete() {
 			super.afterDelete();
 			if (deleteListener != null)
 				deleteListener.handleEvent();
 		}
 		
 		private void afterSave(final ReferenceModel reference) {
 			hide();
 			
 			if (saveListener != null)
 				saveListener.handleEvent(reference);
 		}
 		
 		private void doReplace(final ReferenceModel reference, final Reference returnedRef, final Integer assessmentID, String assessmentType) {
 			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
 			ndoc.post(UriBase.getInstance().getReferenceBase() + "/reference/replace", ReferenceUtils.seralizeReplaceRequest(
 					reference.ref, returnedRef, assessmentID, assessmentType), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					WindowUtils.hideLoadingAlert();
 					reference.ref.setReferenceID(returnedRef.getReferenceID());
 
 					afterSave(reference);
 				}
 				public void onSuccess(String result) {
 					WindowUtils.hideLoadingAlert();
 					
 					reference.ref.setReferenceID(returnedRef.getReferenceID());
 					String message = "";
 					
 					if( assessmentID != null )
 						message += "Replaced the reference on your current assessment.<br/><br/>";
 						
 					message += XMLUtils.cleanFromXML(ndoc.getDocumentElement().getTextContent());
 
 					final Dialog dialog = new Dialog();
 					dialog.setButtons(Dialog.OK);
 					dialog.setClosable(true);
 					dialog.setHideOnButtonClick(true);
 					dialog.addWindowListener(new WindowListener() {
 						@Override
 						public void windowHide(WindowEvent we) {
 							afterSave(reference);
 						}
 					});
 					dialog.setSize(350, 300);
 					dialog.setScrollMode(Scroll.AUTOY);
 					dialog.add(new HTML(message));
 					dialog.show();
 				}
 			});
 		}
 
 		@Override
 		public void onSaveSuccessful(final Reference returnedRef) {
 			if (reference == null)
 				afterSave(null);
 			else if (promptToReplace && reference.ref.getReferenceID() != returnedRef.getReferenceID()
 					&& AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.REFERENCE_REPLACE_FEATURE)) {
 				promptToReplace(reference, returnedRef);
 			} else {
 				reference.ref.setId(returnedRef.getReferenceID());
 				afterSave(reference);
 			}
 		}
 
 		private void promptToReplace(final ReferenceModel reference, final Reference returnedRef) {
 			String message = (local ? "Apply the changes you made to this " +
 				"assessment's reference to all other assessments that use it?" :
 				"Your edits have been saved as a new reference. Would you like to replace " +
 				"the old reference on all assessments with your new one? If you select no, " +
 				"this reference will not be attached to any assessments by default.");
 			
 			WindowUtils.confirmAlert("Reference Changed", message, new Listener<MessageBoxEvent>() {
 				
 				public void handleEvent(MessageBoxEvent we) {
 					if( we.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
 						Integer assessmentID = null;
 						String assessmentType = null;
 						
 						if (local) { //It's already replaced locally.
 							WindowUtils.showLoadingAlert("Performing replace. This could take a few minutes...");
 							assessmentID = AssessmentCache.impl.getCurrentAssessment().getId();
 							assessmentType = AssessmentCache.impl.getCurrentAssessment().getType();
 							
 							doReplace(reference, returnedRef, assessmentID, assessmentType);
 						} else { 
 							if( AssessmentCache.impl.getCurrentAssessment() != null ) {
 								WindowUtils.showLoadingAlert("Performing replace. This could take a few minutes...");
 								boolean found = false;
 								
 //								HashMap<String, ArrayList<Reference>> curRefs = AssessmentCache.impl.getCurrentAssessment().getReferences();
 								for( Field curField : AssessmentCache.impl.getCurrentAssessment().getField() ) {
 //								for( Entry<String, ArrayList<Reference>> curEntry : curRefs.entrySet() ) {
 									if( curField.getReference().contains(reference.ref) ) {
 										curField.getReference().remove(reference.ref);
 										curField.getReference().add(returnedRef);
 										returnedRef.getField().add(curField);
 										
 										found = true;
 									}
 								}
 								
 								if( AssessmentCache.impl.getCurrentAssessment().getReference().contains(reference.ref) ) {
 									AssessmentCache.impl.getCurrentAssessment().getReference().remove(reference.ref);
 									AssessmentCache.impl.getCurrentAssessment().getReference().add(returnedRef);
 									returnedRef.getAssessment().add(AssessmentCache.impl.getCurrentAssessment());
 									
 									found = true;
 								}
 								
 								if( found ) {
 									assessmentID = AssessmentCache.impl.getCurrentAssessment().getId();
 									assessmentType = AssessmentCache.impl.getCurrentAssessment().getType();
 
 									try {
 										AssessmentClientSaveUtils.saveAssessment(AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
 											public void onFailure(Throwable caught) {
 												doReplace(reference, returnedRef, null, null);
 											}
 											public void onSuccess(Object result) {
 												doReplace(reference, returnedRef, 
 														AssessmentCache.impl.getCurrentAssessment().getId(), 
 														AssessmentCache.impl.getCurrentAssessment().getType());
 											}
 										});
 									} catch (InsufficientRightsException e) {
 										//AssessmentCache.impl.resetCurrentAssessment();
										StateManager.impl.setState(null);
 										doReplace(reference, returnedRef, null, null);
 									}
 									
 								} else {
 									doReplace(reference, returnedRef, assessmentID, assessmentType);
 								}
 							}
 							else
 								doReplace(reference, returnedRef, null, null);
 						}
 					} else {
 						reference.ref.setReferenceID(returnedRef.getReferenceID());
 						afterSave(reference);
 					}
 				}
 			});
 		}		
 	}
 
 }
