 /**
  * 
  */
 package org.cotrix.web.share.server.task;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.enterprise.inject.Default;
 import javax.inject.Singleton;
 
 import org.cotrix.action.Action;
 import org.cotrix.web.share.shared.feature.FeatureCarrier;
 import org.cotrix.web.share.shared.feature.UIFeature;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Default
 @Singleton
 public class ActionMapper {
 
 	protected Logger logger = LoggerFactory.getLogger(ActionMapper.class);
 
 	protected Map<Action, Set<UIFeature>> mappings = new HashMap<Action,Set<UIFeature>>();
 
 	public void fillFeatures(FeatureCarrier carrier, Collection<? extends Action> actions)
 	{
 		Set<UIFeature> applicationFeatures = mapActions(actions);
 		carrier.setApplicationFeatures(applicationFeatures);
 	}
 
 	public void fillFeatures(FeatureCarrier carrier, String instanceId, Collection<? extends Action> actions)
 	{
 
 		logger.trace("fillFeatures carrier: {} instanceId: {} actions: {}", carrier.getClass(), instanceId, actions);
 
 		Set<UIFeature> codelistFeatures = mapActions(actions);
 		carrier.setCodelistsFeatures(singletonMap(instanceId, codelistFeatures));
 	}
 
 	protected Map<String,Set<UIFeature>> singletonMap(String instanceId, Set<UIFeature> codelistFeatures)
 	{
 		Map<String,Set<UIFeature>> codelistsFeatures = new HashMap<String, Set<UIFeature>>();
 		codelistsFeatures.put(instanceId, codelistFeatures);
 		return codelistsFeatures;
 	}
 
 
 	protected Set<UIFeature> mapActions(Collection<? extends Action> actions)
 	{
 		logger.trace("mapMainActions actions {}", actions);
 		Set<UIFeature> features = new HashSet<UIFeature>();
 
 		for (Action action:actions) {
			Set<UIFeature> actionFeatures = mappings.get(action.on(Action.any));
 			if (actionFeatures!=null) {
 				logger.trace("mapping {} to {}", action, actionFeatures);
 				features.addAll(actionFeatures);
 			} else logger.warn("No mappings for action {} current mappings: {} ", action, mappings);
 		}
 
 		return features;
 	}
 	
 	protected void addMapping(Action action, UIFeature[] features)
 	{
 		Set<UIFeature> currentFeatures = mappings.get(action);
 		if (currentFeatures == null) {
 			currentFeatures = new HashSet<UIFeature>();
 			mappings.put(action, currentFeatures);
 		}
 		for (UIFeature feature:features) currentFeatures.add(feature);
 	}
 
 	public ActionMapBuilder map(Action action)
 	{
 		return new ActionMapBuilder(this, action.on(Action.any));
 	}
 }
