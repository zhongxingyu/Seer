 package org.cotrix.web.importwizard.client.step.selection;
 
 import org.cotrix.web.importwizard.client.ImportServiceAsync;
 import org.cotrix.web.importwizard.client.event.CodeListSelectedEvent;
 import org.cotrix.web.importwizard.client.event.ImportBus;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent.ResetWizardHandler;
 import org.cotrix.web.importwizard.client.step.AbstractWizardStep;
 
 import org.cotrix.web.importwizard.shared.AssetDetails;
 import org.cotrix.web.importwizard.shared.AssetInfo;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 import static org.cotrix.web.importwizard.client.wizard.NavigationButtonConfiguration.*;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SelectionStepPresenterImpl extends AbstractWizardStep implements SelectionStepPresenter, ResetWizardHandler {
 
 	protected final SelectionStepView view;
 	
 	@Inject
 	protected ImportServiceAsync importService;
 	
 	protected EventBus importEventBus;
 	
 	protected AssetInfo assetInfo;
 	
 	@Inject
 	public SelectionStepPresenterImpl(SelectionStepView view, @ImportBus EventBus importEventBus) {
		super("selection", "Acquire", "Upload it", DEFAULT_BACKWARD, DEFAULT_FORWARD);
 		this.view = view;
 		this.view.setPresenter(this);
 		this.importEventBus = importEventBus;
 		importEventBus.addHandler(ResetWizardEvent.TYPE, this);
 	}
 
 	public void go(HasWidgets container) {
 		container.add(view.asWidget());
 	}
 
 	public boolean isComplete() {
 		return assetInfo!=null;
 	}
 
 	@Override
 	public void assetSelected(AssetInfo asset) {
 		Log.trace("Asset selected "+asset);
 		this.assetInfo = asset;
 		importService.setAsset(asset.getId(), new AsyncCallback<Void>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Failed setting the selected asset", caught);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				importEventBus.fireEvent(new CodeListSelectedEvent());
 			}
 		});
 	}
 
 	@Override
 	public void assetDetails(AssetInfo asset) {
 		Log.trace("getting asset details for "+asset);
 		importService.getAssetDetails(asset.getId(), new AsyncCallback<AssetDetails>() {
 			
 			@Override
 			public void onSuccess(AssetDetails result) {
 				view.showAssetDetails(result);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.error("Failed loading asset details", caught);
 			}
 		});
 	}
 
 	@Override
 	public void onResetWizard(ResetWizardEvent event) {
 		view.reset();
 	}
 }
