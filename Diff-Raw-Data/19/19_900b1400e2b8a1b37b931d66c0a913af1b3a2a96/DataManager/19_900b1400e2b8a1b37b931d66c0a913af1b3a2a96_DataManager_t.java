 package com.app.augmentedbizz.application.data;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.app.augmentedbizz.R;
 import com.app.augmentedbizz.application.ApplicationFacade;
 import com.app.augmentedbizz.application.data.cache.CacheResponseListener;
 import com.app.augmentedbizz.application.data.cache.CacheStorageManager;
 import com.app.augmentedbizz.application.status.ApplicationState;
 import com.app.augmentedbizz.logging.DebugLog;
 import com.app.augmentedbizz.services.ServiceManager;
 import com.app.augmentedbizz.services.entity.ServiceTransferEntity;
 import com.app.augmentedbizz.services.entity.transfer.IndicatorServiceEntity.TargetIndicator;
 import com.app.augmentedbizz.services.handler.ServiceHandlingException;
 import com.app.augmentedbizz.services.response.ServiceResponseListener;
 import com.app.augmentedbizz.services.service.BaseHttpService;
 import com.app.augmentedbizz.services.service.repository.IndicatorHttpService;
 import com.app.augmentedbizz.services.service.repository.ModelHttpService;
 import com.app.augmentedbizz.services.service.repository.TargetHttpService;
 import com.app.augmentedbizz.ui.renderer.OpenGLModelConfiguration;
 
 /**
  * The data manager presents an interface between the application facade, the cache and the service manager
  * and acts as the central point for dataflows inside the application.
  * 
  * @author Vladi
  *
  */
 public class DataManager implements ServiceResponseListener,
 ModelDataListener,
 TargetDataListener,
 IndicatorDataListener,
 CacheResponseListener {
 	
 	private int currentTargetId = -1;
 	private Target currentTarget = null;
 	private List<TargetIndicator> indicators = new ArrayList<TargetIndicator>();
 	private OpenGLModelConfiguration openGLModelConfiguration = null;
 	private ServiceManager serviceManager;
 	private EntityConverter entityConverter;
 	private ApplicationFacade facade;
 	private CacheStorageManager cacheManager;
 	
 	//private boolean modelNotInCache = false;
 	
 	private List<ModelDataListener> modelDataListeners = new ArrayList<ModelDataListener>();
 	private List<TargetDataListener> targetDataListeners = new ArrayList<TargetDataListener>();
 	private List<IndicatorDataListener> indicatorDataListeners = new ArrayList<IndicatorDataListener>();
 	
 	public DataManager(ApplicationFacade facade) {
 		this.serviceManager = new ServiceManager(facade);
 		this.entityConverter = new EntityConverter();
 		this.facade = facade;
 		this.cacheManager = new CacheStorageManager(facade.getContext());
 		
 		this.addModelDataListener(this);
 		this.addTargetDataListener(this);
 		this.addIndicatorDataListener(this);
 	}
 	
 	/**
 	 * @return the application facade
 	 */
 	public ApplicationFacade getApplicationFacade() {
 		return facade;
 	}
 
 	public void addModelDataListener(ModelDataListener listener) {
 		this.modelDataListeners.add(0, listener);
 	}
 	
 	public void removeModelDataListener(ModelDataListener listener) {
 		this.modelDataListeners.remove(listener);
 	}
 	
 	public void addTargetDataListener(TargetDataListener listener) {
 		this.targetDataListeners.add(0, listener);
 	}
 	
 	public void removeTargetDataListener(TargetDataListener listener) {
 		this.targetDataListeners.remove(listener);
 	}
 	
 	public void addIndicatorDataListener(IndicatorDataListener listener) {
 		this.indicatorDataListeners.add(0, listener);
 	}
 	
 	public void removeIndicatorDataListener(IndicatorDataListener listener) {
 		this.indicatorDataListeners.remove(listener);
 	}
 	
 	private void fireOnModelDataEvent(OpenGLModelConfiguration openGLModelConfiguration, boolean retrievingNewerVersion) {
 		//continue if LOADING state not lost
 		if(!facade.getApplicationStateManager()
 				.getApplicationState().equals(ApplicationState.LOADING)) {
 			return;
 		}
 		
 		Iterator<ModelDataListener> it = this.modelDataListeners.iterator();
 		
 		while(it.hasNext()) {
 			it.next().onModelData(openGLModelConfiguration, retrievingNewerVersion);
 		}
 	}
 	
 	private void fireOnModelErrorEvent(Exception e) {
 		//continue if LOADING state not lost
 		if(!facade.getApplicationStateManager()
 				.getApplicationState().equals(ApplicationState.LOADING)) {
 			return;
 		}
 		
 		Iterator<ModelDataListener> it = this.modelDataListeners.iterator();
 		
 		while(it.hasNext()) {
 			it.next().onModelError(e);
 		}
 	}
 	
 	private void fireOnTargetDataEvent(Target target) {
 		//continue if LOADING state not lost
 		if(!facade.getApplicationStateManager()
 				.getApplicationState().equals(ApplicationState.LOADING)) {
 			return;
 		}
 		
 		Iterator<TargetDataListener> it = this.targetDataListeners.iterator();
 		
 		while(it.hasNext()) {
 			it.next().onTargetData(target);
 		}
 	}
 	
 	private void fireOnTargetErrorEvent(Exception e) {
 		//continue if LOADING state not lost
 		if(!facade.getApplicationStateManager()
 				.getApplicationState().equals(ApplicationState.LOADING)) {
 			return;
 		}
 		
 		Iterator<TargetDataListener> it = this.targetDataListeners.iterator();
 		
 		while(it.hasNext()) {
 			it.next().onTargetError(e);
 		}
 	}
 	
 	private void fireOnIndicatorDataEvent(List<TargetIndicator> indicators) {
 		//continue if LOADING_INDICATORS state not lost
 		if(!facade.getApplicationStateManager()
 				.getApplicationState().equals(ApplicationState.LOADING_INDICATORS)) {
 			return;
 		}
 		
 		Iterator<IndicatorDataListener> it = this.indicatorDataListeners.iterator();
 		
 		while(it.hasNext()) {
 			it.next().onIndicatorData(indicators);
 		}
 	}
 	
 	private void fireOnIndicatorErrorEvent(Exception e) {
 		//continue if LOADING_INDICATORS state not lost
 		if(!facade.getApplicationStateManager()
 				.getApplicationState().equals(ApplicationState.LOADING_INDICATORS)) {
 			return;
 		}
 		
 		Iterator<IndicatorDataListener> it = this.indicatorDataListeners.iterator();
 		
 		while(it.hasNext()) {
 			it.next().onIndicatorError(e);
 		}
 	}
 	
 	public void loadModel(int modelId) {
 		//check for local buffer
 		if(this.openGLModelConfiguration != null) {
 			this.fireOnModelDataEvent(this.openGLModelConfiguration, false);
 		} else {
 			cacheManager.readModelAsync(modelId, this);
 		}
 	}
 	
 	public void loadTarget(int targetId) {
 		getApplicationFacade().getApplicationStateManager().setApplicationState(ApplicationState.LOADING);
 		currentTargetId = targetId;
 		
 		//check the local buffer
 		if(this.currentTarget != null) {
 			this.fireOnTargetDataEvent(this.currentTarget);
 		} else {
 			this.serviceManager.callTargetInformationService(targetId, this);
 		}
 	}
 	
 	public void loadIndicators(int targetId) {
 		getApplicationFacade().getApplicationStateManager().setApplicationState(ApplicationState.LOADING_INDICATORS);
 		
 		//check the local buffer
 		if(this.indicators != null) {
 			this.fireOnIndicatorDataEvent(this.indicators);
 		} else {
 			this.serviceManager.callIndicatorInformationService(targetId, this);
 		}
 	}
 	
 	private void handleModelResponse(ServiceTransferEntity stEntity, BaseHttpService calledService) {
 		//convert service entity to model
 		OpenGLModelConfiguration model = entityConverter.toOpenGLModelFrom(stEntity, calledService, this.currentTarget.getLatestModelVersion());
 		cacheManager.insertOrUpdateModelAsync(model);
 		this.fireOnModelDataEvent(model, false);
 	}
 	
 	private void handleTargetResponse(ServiceTransferEntity stEntity) {
 		this.fireOnTargetDataEvent(this.entityConverter.toTargetFrom(stEntity));
 	}
 	
 	private void handleIndicatorResponse(ServiceTransferEntity stEntity) {
 		this.fireOnIndicatorDataEvent(this.entityConverter.toIndicatorListFrom(stEntity));
 	}
 
 	@Override
 	public void onServiceResponse(ServiceTransferEntity stEntity,
 			BaseHttpService calledService) {
 		if(calledService instanceof ModelHttpService) {
 			this.handleModelResponse(stEntity, calledService);
 		} else
 		if(calledService instanceof TargetHttpService) {
 			this.handleTargetResponse(stEntity);
 		} else {
 			this.handleIndicatorResponse(stEntity);
 		}
 	}
 
 	@Override
 	public void onServiceFailed(ServiceHandlingException exception,
 			BaseHttpService calledService) {
 		DebugLog.loge("Unable to call service.", exception);
 		
 		if(calledService instanceof ModelHttpService) {
 			this.fireOnModelErrorEvent(exception);
 		} else if(calledService instanceof TargetHttpService) {
 			this.fireOnTargetErrorEvent(exception);
 		} else if(calledService instanceof IndicatorHttpService) {
 			this.fireOnIndicatorErrorEvent(exception);
 		}
 	}
 
 	@Override
 	public void onModelData(OpenGLModelConfiguration openGLModelConfiguration, boolean retievingNewerVersion) {
 		this.openGLModelConfiguration = openGLModelConfiguration;
 		DebugLog.logi("Model data received.");
 		
 		loadIndicators(currentTargetId);
 	}
 	
 	@Override
 	public void onModelError(Exception e) {
 		getApplicationFacade().getUIManager().showWarningToast(R.string.errorModelRetrieval);
 		clearLocalBuffer();
 		getApplicationFacade().getApplicationStateManager().setApplicationState(ApplicationState.TRACKING);
 		DebugLog.logi("An error ocurred while the model was being loaded.");
 	}
 
 	@Override
 	public void onIndicatorData(List<TargetIndicator> targetIndicators) {
 		this.indicators = targetIndicators;
		DebugLog.logi("Indicator data received (" + targetIndicators.size() + ").");
 	}
 	
 	@Override
 	public void onIndicatorError(Exception e) {
 		getApplicationFacade().getUIManager().showWarningToast(R.string.errorServiceUnreachable);
 		clearLocalBuffer();
 		getApplicationFacade().getApplicationStateManager().setApplicationState(ApplicationState.TRACKING);
 		DebugLog.logi("An error ocurred while the indicators were being loaded.");
 	}
 	
 	@Override
 	public void onTargetData(Target target) {
 		this.currentTarget = target;
 		DebugLog.logi("Target data received.");
 		
 		loadModel(target.getModelId());
 	}
 
 	@Override
 	public void onTargetError(Exception e) {
 		getApplicationFacade().getUIManager().showWarningToast(R.string.errorServiceUnreachable);
 		clearLocalBuffer();
 		getApplicationFacade().getApplicationStateManager().setApplicationState(ApplicationState.TRACKING);
 		DebugLog.logi("An error ocurred while the target was being loaded.");
 	}
 	
 	@Override
 	public void onModelConfigFromCache(OpenGLModelConfiguration model) {
 		//retrieved the model successfully from cache
 		boolean hasNewer = getCurrentTarget().getLatestModelVersion() > 
 						   model.getOpenGLModel().getModelVersion();
 		this.fireOnModelDataEvent(model, hasNewer);
 	}
 
 	@Override
 	public void onCacheFailure(int modelId) {
 		// try to get the data from the service manager
 		serviceManager.callModelInformationService(modelId, this);
 	}
 	
 	/**
 	 * Clears the local buffer data by deleting the target, model and indicators
 	 */
 	private void clearLocalBuffer() {
 		currentTarget = null;
 		currentTargetId = -1;
 		openGLModelConfiguration = null;
 		if(indicators != null) {
 			indicators.clear();
 		}
 	}
 
 	/**
 	 * @return the current target from the local buffer
 	 */
 	public Target getCurrentTarget() {
 		return currentTarget;
 	}
 
 	/**
 	 * @return the indicators
 	 */
 	public List<TargetIndicator> getCurrentIndicators() {
 		return indicators;
 	}
 	
 }
