 package net.sf.anathema.character.core.model;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.disy.commons.core.model.listener.IChangeListener;
 import net.sf.anathema.character.core.character.ICharacterId;
 import net.sf.anathema.character.core.character.IModel;
 import net.sf.anathema.character.core.character.IModelIdentifier;
 import net.sf.anathema.character.core.character.ModelIdentifier;
 import net.sf.anathema.character.core.model.cache.NotifyOfModelCreation;
 import net.sf.anathema.character.core.template.CharacterTemplateProvider;
 import net.sf.anathema.lib.control.GenericControl;
 
 public class ModelCache implements IModelCache {
 
   private static final ModelCache instance = new ModelCache();
   private final Map<IModelIdentifier, IModel> modelsByIdentifier = new HashMap<IModelIdentifier, IModel>();
   private final Map<IModel, IModelIdentifier> identifiersByModel = new HashMap<IModel, IModelIdentifier>();
   private final DependenciesHandler dependenciesHandler = new DependenciesHandler(new CharacterTemplateProvider());
   private final Map<IModelIdentifier, IChangeListener> changeListeners = new HashMap<IModelIdentifier, IChangeListener>();
   private final GenericControl<IModelChangeListener> modelChangeListeners = new GenericControl<IModelChangeListener>();
 
   private ModelCache() {
     // nothing to do
   }
 
   public static ModelCache getInstance() {
     return instance;
   }
 
   public synchronized IModel getModel(final IModelIdentifier identifier) {
     IModel model = modelsByIdentifier.get(identifier);
    if (model == null && identifier != null) {
       ModelExtensionPoint modelExtensionPoint = new ModelExtensionPoint();
       IModelInitializer initializer = modelExtensionPoint.createModel(identifier);
       model = initializer.getModel();
       if (model != null) {
         storeModel(identifier, model);
         initializer.initialize();
         loadDependencies(identifier, model);
         IChangeListener overallChangeListener = new OverallModelChangeListener(identifier, modelChangeListeners);
         changeListeners.put(identifier, overallChangeListener);
         model.addChangeListener(overallChangeListener);
         modelChangeListeners.forAllDo(new NotifyOfModelCreation(identifier));
       }
     }
     return model;
   }
 
   protected void storeModel(IModelIdentifier identifier, IModel model) {
     modelsByIdentifier.put(identifier, model);
     identifiersByModel.put(model, identifier);
   }
 
   private void loadDependencies(IModelIdentifier identifier, final IModel dependentModel) {
     for (String neededId : dependenciesHandler.getNeededIds(identifier)) {
       IModel neededModel = getModel(new ModelIdentifier(identifier.getCharacterId(), neededId));
       neededModel.addChangeListener(new IChangeListener() {
         @Override
         public void stateChanged() {
           dependentModel.updateToDependencies();
         }
       });
     }
   }
 
   public synchronized void revert(IModel item) {
     IModelIdentifier modelIdentifier = identifiersByModel.get(item);
     removeFromCache(item, modelIdentifier);
     getModel(modelIdentifier);
   }
 
   private void removeFromCache(IModel model, IModelIdentifier identifier) {
     IChangeListener modelChangeListener = changeListeners.get(identifier);
     identifiersByModel.remove(model);
     modelsByIdentifier.remove(identifier);
     changeListeners.remove(modelChangeListener);
     model.removeChangeListener(modelChangeListener);
   }
 
   @Override
   public boolean contains(IModelIdentifier identifier) {
     return modelsByIdentifier.containsKey(identifier);
   }
 
   public void clear() {
     modelsByIdentifier.clear();
     identifiersByModel.clear();
   }
 
   @Override
   public void clearAllModels(ICharacterId id) {
     HashMap<IModelIdentifier, IModel> clone = new HashMap<IModelIdentifier, IModel>(modelsByIdentifier);
     for (Entry<IModelIdentifier, IModel> entry : clone.entrySet()) {
       if (entry.getKey().getCharacterId().equals(id)) {
         removeFromCache(entry.getValue(), entry.getKey());
       }
     }
   }
 
   public void addModelChangeListener(IModelChangeListener listener) {
     modelChangeListeners.addListener(listener);
   }
 
   public void removeModelChangeListener(IModelChangeListener listener) {
     modelChangeListeners.removeListener(listener);
   }
 }
