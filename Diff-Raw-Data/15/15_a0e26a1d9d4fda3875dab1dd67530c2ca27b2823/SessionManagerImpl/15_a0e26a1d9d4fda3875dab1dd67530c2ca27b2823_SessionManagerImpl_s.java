 package org.dada.core;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.dada.slf4j.Logger;
 import org.dada.slf4j.LoggerFactory;
 
 public class SessionManagerImpl implements SessionManager {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	private final MetaModel metamodel;
 	private final ServiceFactory<Model<Object, Object>> serviceFactory;
 	private final Set<String> exportedModelNames = new HashSet<String>();
 
	public SessionManagerImpl(MetaModel metamodel, ServiceFactory<Model<Object, Object>> serviceFactory) {
 		this.metamodel = metamodel;
 		this.serviceFactory = serviceFactory;
 		exportedModelNames.add(this.metamodel.getName());
 	}
 
 	@Override
 	public Model<Object, Object> getModel(String name) {
 		return metamodel.getModel(name);
 	}
 
 	@Override
 	public Data<Object> deregisterView(String name, View<Object> view) {
 		Model<Object, Object> model = metamodel.getModel(name);
 		logger.info("deregistering View ({}) from Model ({})", view, model);
 		return model.deregisterView(view);
 		// TODO - what about tidying up ServiceFactory resources ? Their allocation should be done
 		// on a first-in-turns-on-lights, last-out-turns-off-lights basis...
 	}
 
 	@Override
 	public Data<Object> registerView(String modelName, View<Object> view) {
 		Model<Object, Object> model = metamodel.getModel(modelName);
 		try {
 			synchronized (exportedModelNames) { // TODO keep a ref-count
 				if (!exportedModelNames.contains(modelName)) {
 					logger.info("exporting Model: {}", model);
 					serviceFactory.server(model, modelName); // should allocate a topic
 					exportedModelNames.add(modelName);
 				}
 			}
 			logger.info("registering View ({}) with Model ({})", view, model);
 			return model.registerView(view); // TODO: should register TopicView _ONCE_
 		} catch (Exception e) {
 			logger.error("unable to export Model: {}", e, modelName);
 			return null;
 		}
 	}
 
 	@Override
 	public String getName() {
		return metamodel.getName();
 	}
 
 	@Override
 	public Metadata<Object, Object> getMetadata(String modelName) {
 		return metamodel.getModel(modelName).getMetadata();
 	}
 
 	@Override
 	public Data<Object> registerQueryView(String query, View<Object> view) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	@Override
 	public Data<Object> deregisterQueryView(String query, View<Object> view) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 }
