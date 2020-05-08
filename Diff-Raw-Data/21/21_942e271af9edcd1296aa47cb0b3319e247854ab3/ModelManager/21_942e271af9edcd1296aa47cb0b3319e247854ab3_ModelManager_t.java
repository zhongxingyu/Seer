 package org.integratedmodelling.thinklab.client.modelling;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import org.integratedmodelling.exceptions.ThinklabException;
 import org.integratedmodelling.exceptions.ThinklabValidationException;
 import org.integratedmodelling.thinklab.api.factories.IModelManager;
 import org.integratedmodelling.thinklab.api.knowledge.IExpression;
 import org.integratedmodelling.thinklab.api.lang.IModelParser;
 import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
 import org.integratedmodelling.thinklab.api.modelling.IContext;
 import org.integratedmodelling.thinklab.api.modelling.IModel;
 import org.integratedmodelling.thinklab.api.modelling.IModelObject;
 import org.integratedmodelling.thinklab.api.modelling.INamespace;
 import org.integratedmodelling.thinklab.api.modelling.IObservation;
 import org.integratedmodelling.thinklab.api.modelling.IScenario;
 import org.integratedmodelling.thinklab.api.project.IProject;
 import org.integratedmodelling.thinklab.client.utils.MiscUtilities;
 
 /**
  * A model manager that can parse the Thinklab language and build a model map, without actually being 
  * capable of running the model objects.
  * 
  * @author Ferd
  *
  */
 public class ModelManager implements IModelManager {
 
 	private static ModelManager _this = null;
 	
 	HashMap<String, IModelParser> interpreters = new HashMap<String, IModelParser>();
 	HashMap<String, INamespace> namespaces = new HashMap<String, INamespace>();
 	
 	public static ModelManager get() {
 		if (_this == null) {
 			_this = new ModelManager();
 		}
 		return _this;
 	}
 	
 	public void addInterpreter(String extension, IModelParser modelParser) {
 		interpreters.put(extension, modelParser);
 	}
 	
 	@Override
 	public IAgentModel getAgentModel(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<IScenario> getApplicableScenarios(IModel arg0,
 			IContext arg1, boolean arg2) throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IContext getContext(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<IModelObject> getDependencies(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IModel getModel(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IModelObject getModelObject(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * Override this to return a new subclass of Resolver to intercept
 	 * errors and warnings.
 	 * 
 	 * @param project
 	 * @return
 	 */
 	public Resolver createResolver(IProject project) {
 		return new Resolver(project);
 	}
 
 	@Override
 	public INamespace getNamespace(String arg0) {
 		return namespaces.get(arg0);
 	}
 
 	@Override
 	public Collection<INamespace> getNamespaces() {
 
 		ArrayList<INamespace> ret = new ArrayList<INamespace>();
 		for (INamespace n : namespaces.values()) {
 			ret.add(n);
 		}
 		
 		/*
 		 * sort namespace list alphabetically
 		 */
 		Collections.sort(ret, new Comparator<INamespace>() {
 			@Override
 			public int compare(INamespace arg0, INamespace arg1) {
 				return arg0.getId().compareTo(arg1.getId());
 			}
 		});
 		
 		return ret;
 		
 	}
 
 	@Override
 	public IScenario getScenario(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getSource(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public INamespace loadFile(String file, String namespaceId, IProject project) throws ThinklabException {
 		
 		String extension = MiscUtilities.getFileExtension(file);
 		IModelParser parser = interpreters.get(extension);
 		Namespace ret = null;
 		
 		if (parser == null) {
 			throw new ThinklabValidationException("don't know how to parse a " + extension + " model file");
 		}
 		
 		ret = (Namespace) parser.parse(file, createResolver(project));
		
		if (ret != null) {
		
			ret.setProject(project);
			ret.setResourceUrl(file);
			ret.synchronizeKnowledge();
			namespaces.put(ret.getId(), ret);
		}
 		
 		return ret;
 	}
 
 	@Override
 	public void releaseNamespace(String arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public Collection<INamespace> load(IProject project)
 			throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean canParseExtension(String fileExtension) {
 		return interpreters.containsKey(fileExtension);
 	}
 
 	public INamespace loadNamespace(String namespaceId, String resource, String resourceType)
 			throws ThinklabException {
 		
 		String extension = resourceType;
 		if (extension == null)
 			extension = MiscUtilities.getFileExtension(resource);
 		
 		IModelParser parser = interpreters.get(extension);
 		Namespace ret = null;
 		
 		if (parser == null) {
 			throw new ThinklabValidationException("don't know how to parse a " + extension + " resource");
 		}
 		
 		ret = (Namespace) parser.parse(resource, createResolver(null));
 
 		if (ret != null) {
 			ret.setId(namespaceId);
 			ret.synchronizeKnowledge();
 			namespaces.put(ret.getId(), ret);
 		}
 		
 		return ret;
 	}
 
 	@Override
 	public IContext getCoverage(IModel model) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IExpression resolveFunction(String functionId,
 			Collection<String> parameterNames) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<INamespace> loadSourceDirectory(File sourcedir, IProject project)
 			throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IObservation observe(Object object, IContext context)
 			throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
