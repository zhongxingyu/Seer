 package org.iucn.sis.client.panels.permissions;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.utils.BasicWindow;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.utils.TaxonomyBrowserPanel;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class PermissionScopeTaxonomyBrowser extends BasicWindow {
 
 	private final TaxonomyBrowserPanel scopeBrowser;
 	private Button select;
 	private Taxon  currentlySelected;
 	
 	/**
 	 * Create a PermissionScopeTaxonomyBrowser Window. You must supply the PermissionEditor this
 	 * Window will be used by, so when a scope is chosen it can callback and update the editor.
 	 * 
 	 * @param editor the editor using this taxonomy browser
 	 */
 	public PermissionScopeTaxonomyBrowser(final ComplexListener<Taxon> listener) {
 		super("Select Taxon", null, true);
 		setLayout(new FillLayout());
 		setClosable(false);
 		setSize(600, 400);
 		currentlySelected = null;
 		
 		scopeBrowser = new TaxonomyBrowserPanel() {
 			protected void addViewButtonToFootprint() {
 				//Don't add anything
 			}
 		};
 		scopeBrowser.addListener(Events.Change, new Listener<TaxonomyBrowserPanel.TaxonChangeEvent>() {
 			public void handleEvent(TaxonomyBrowserPanel.TaxonChangeEvent be) {
 				updateCurrent(be.getTaxon());
 			}
 		});
 		
 		select = new Button("No Taxon Selected", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 				listener.handleEvent(currentlySelected);
 			}
 		});
 		select.setEnabled(false);
 		
 		add(scopeBrowser);
 		
 		addButton(select);
 		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
				hide();
 				listener.handleEvent(null);
 			}
 		}));
 		
 		addListener(Events.Show, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 				scopeBrowser.update();
 				layout();
 			};
 		});
 	}
 	
 	protected void updateCurrent(Taxon taxon) {
 		this.currentlySelected = taxon;
 		
 		if( taxon == null ) {
 			select.setEnabled(false);
 			select.setText("No Taxon Selected");
 		} else if( !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, taxon) ) {
 			select.setEnabled(false);
 			select.setText(taxon.getFullName());
 		} else {
 			select.setEnabled(true);
 			select.setText("Use " + taxon.getFullName());
 		}
 	}
 	
 	@Override
 	public void show() {
 		WindowUtils.showLoadingAlert("Loading...");
 		DeferredCommand.addCommand(new Command() {
 			public void execute() {
 				scopeBrowser.update(new GenericCallback<Object>() {
 					public void onSuccess(Object result) {
 						open();
 						WindowUtils.hideLoadingAlert();
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.hideLoadingAlert();
 					}
 				});
 			}
 		});
 	}
 	
 	private void open() {
 		super.show();
 	}
 	
 }
