 package net.sf.anathema.character.attributes.points;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.disy.commons.core.exception.UnreachableCodeReachedException;
 import net.sf.anathema.basics.eclipse.extension.AbstractExecutableExtension;
 import net.sf.anathema.character.attributes.model.Attributes;
 import net.sf.anathema.character.core.character.ICharacterId;
 import net.sf.anathema.character.core.character.IModelCollection;
 import net.sf.anathema.character.core.character.ModelIdentifier;
 import net.sf.anathema.character.core.model.IModelResourceHandler;
 import net.sf.anathema.character.core.model.ModelCache;
 import net.sf.anathema.character.core.repository.ModelResourceHandler;
 import net.sf.anathema.character.points.configuration.IPointHandler;
 import net.sf.anathema.character.trait.collection.ITraitCollectionModel;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 
 public abstract class AbstractPointHandler extends AbstractExecutableExtension implements IPointHandler {
 
   private static final String MARKER_TYPE = "net.sf.anathema.markers.bonuspoints"; //$NON-NLS-1$
   private static final String ATTRIB_HANDLER_TYPE = "handlerType"; //$NON-NLS-1$
   private static final String ATTRIB_BONUS_POINTS = "bonusPoints"; //$NON-NLS-1$
   private final IModelCollection modelCollection;
   private final IModelResourceHandler resourceHandler;
   private final String handlerType;
 
   public AbstractPointHandler(String handlerType) {
     this(ModelCache.getInstance(), new ModelResourceHandler(), handlerType);
   }
 
   public AbstractPointHandler(
       IModelCollection modelCollection,
       IModelResourceHandler resourceHandler,
       String handlerType) {
     this.modelCollection = modelCollection;
     this.resourceHandler = resourceHandler;
     this.handlerType = handlerType;
   }
 
   @Override
   public final int getPoints(ICharacterId characterId) {
     if (characterId == null) {
       return 0;
     }
     ModelIdentifier identifier = new ModelIdentifier(characterId, Attributes.MODEL_ID);
     try {
       IResource resource = resourceHandler.getResource(identifier);
       if (modelCollection.contains(identifier)) {
         return calculatePoints(resource, identifier);
       }
       if (!resource.exists()) {
         return 0;
       }
       IMarker marker = findBonusPointMarker(resource);
       if (marker != null) {
         return ((Number) marker.getAttribute(ATTRIB_BONUS_POINTS)).intValue();
       }
       return calculatePoints(resource, identifier);
     }
     catch (CoreException e) {
       throw new UnreachableCodeReachedException(e);
     }
   }
 
   private IMarker findBonusPointMarker(IResource resource) throws CoreException {
     for (IMarker marker : resource.findMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO)) {
       if (marker.getAttribute(ATTRIB_HANDLER_TYPE).equals(handlerType)) {
         return marker;
       }
     }
     return null;
   }
 
   private int calculatePoints(IResource resource, ModelIdentifier identifier) throws CoreException {
     ITraitCollectionModel attributes = (ITraitCollectionModel) modelCollection.getModel(identifier);
     int bonusPoints = calculatePoints(attributes, identifier.getCharacterId());
    if (resource.exists()) {
      updateMarker(resource, bonusPoints);
    }
     return bonusPoints;
   }
 
   private void updateMarker(IResource resource, int bonusPoints) throws CoreException {
     IMarker marker = findBonusPointMarker(resource);
     if (marker == null) {
       marker = resource.createMarker(MARKER_TYPE);
     }
     if (Integer.valueOf(bonusPoints).equals(marker.getAttribute(ATTRIB_BONUS_POINTS))) {
       return;
     }
     Map<String, Object> markerAttributes = new HashMap<String, Object>();
     markerAttributes.put(ATTRIB_HANDLER_TYPE, handlerType);
     markerAttributes.put(ATTRIB_BONUS_POINTS, bonusPoints);
     marker.setAttributes(markerAttributes);
   }
 
   protected abstract int calculatePoints(ITraitCollectionModel attributes, ICharacterId characterId);
 }
