 package fr.liglab.adele.cube.extensions.cilia;
 
 import fr.liglab.adele.cilia.builder.Architecture;
 import fr.liglab.adele.cilia.builder.Builder;
 import fr.liglab.adele.cilia.exceptions.BuilderConfigurationException;
 import fr.liglab.adele.cilia.exceptions.BuilderException;
 import fr.liglab.adele.cilia.exceptions.CiliaException;
 import fr.liglab.adele.cilia.exceptions.CiliaIllegalParameterException;
 import fr.liglab.adele.cilia.exceptions.CiliaIllegalStateException;
 import fr.liglab.adele.cilia.model.Chain;
 import fr.liglab.adele.cube.agent.AgentExtensionConfig;
 import fr.liglab.adele.cube.agent.CInstance;
 import fr.liglab.adele.cube.agent.CubeAgent;
 import fr.liglab.adele.cube.agent.RuntimeModel;
 import fr.liglab.adele.cube.archetype.ManagedElement;
 import fr.liglab.adele.cube.extensions.AbstractExtension;
 import fr.liglab.adele.cube.extensions.IExtensionFactory;
 import fr.liglab.adele.cube.extensions.core.CoreExtensionFactory;
 import fr.liglab.adele.cube.extensions.core.model.Component;
 import fr.liglab.adele.cube.extensions.core.model.ComponentInstance;
 import fr.liglab.adele.cube.util.id.CInstanceUID;
 
 public class CiliaExtension extends AbstractExtension {
 
 	private final String chainId;
 	private final CiliaExtensionFactory cfactory;
 
 
 
 	public CiliaExtension(CubeAgent agent, IExtensionFactory factory,
 			AgentExtensionConfig config) {
 		super(agent, factory, config);
 		chainId = agent.getArchetype().getId();
 		this.cfactory =(CiliaExtensionFactory) getExtensionFactory();  
 	}
 
 	@Override
 	public void start() {
 		System.out.println("\n\n\nCILIA EXTENSION started.\n\n");
		getCubeAgent().getRuntimeModel().addListener(this);
 		// 1. create cilia chain
 		Builder ciliaBuilder = cfactory.getCiliaContext().getBuilder();
 		try {
 			Architecture chain = ciliaBuilder.create(chainId);
 			// 2. read the runtime model, and see if there exist already cube component instances created!
 			for (CInstance i : getCubeAgent().getRuntimeModel().getCInstances(CoreExtensionFactory.ID, Component.NAME, CInstance.VALID)) {
 				createMediator(i, chain);
 			}
 			ciliaBuilder.done();
 		} catch (CiliaException e) {
 			e.printStackTrace();
 		}
 		//3. Initialize the chain
 		try {
 			cfactory.getCiliaContext().getApplicationRuntime().startChain(chainId);
 		} catch (CiliaIllegalParameterException e) {
 			e.printStackTrace();
 		} catch (CiliaIllegalStateException e) {
 			e.printStackTrace();
 		}
 		System.out.println("\n\n\nCreating CUBE Chain.\n\n"  );
 	}
 
 	public void validatedInstance(CInstance coi) {
		System.out.println("Validating Instance");
 		if (coi != null) {			
 			if (coi.getCType().getNamespace().equalsIgnoreCase(CoreExtensionFactory.ID) && coi.getCType().getName().equalsIgnoreCase(Component.NAME)) {
 				Builder ciliaBuilder = cfactory.getCiliaContext().getBuilder();
 				Architecture chain;
 				try {
 					chain = ciliaBuilder.get(chainId);
 					createMediator(coi, chain);
 					ciliaBuilder.done();
 				} catch (CiliaException e) {
 					e.printStackTrace();
 				} 
 			}
 		}
 	}
 
 	/**
 	 * Create Cilia Mediator and add it to the cilia local chain
 	 * @param i
 	 * @throws BuilderException 
 	 * @throws BuilderConfigurationException 
 	 * @throws CiliaIllegalParameterException 
 	 */
 	private void createMediator(CInstance i, Architecture chain) throws CiliaException {	
		System.out.println("Creating Mediator Instance***");
 		String componentType = i.getCType().getId();
 		 
 		if (i.getCType().getProperty("kind") != null && i.getCType().getProperty("kind").equals("adapter")) {
 			chain.create().adapter().type(componentType).id(i.getLocalId());
 		} else {
 			chain.create().mediator().type(componentType).id(i.getLocalId());
 		}
 		// create mediator of componentType type and add it to the chain
		//were are the properties??
 		for (CInstanceUID instanceUID : ((ComponentInstance)i).getOutComponents()) {
 			// you can get the object instance of this UID from the runtime model
 			CInstance inst = getCubeAgent().getRuntimeModel().getCInstance(instanceUID);
 			Chain mchain = this.cfactory.getCiliaContext().getApplicationRuntime().getChain(chainId);
			if(mchain.getMediator(inst.getLocalId()) != null) {
 				chain.bind().from(i.getLocalId()+":unique").to(inst.getLocalId()+":unique");
 			}
 			
 		}
 		for (CInstanceUID instanceUID : ((ComponentInstance)i).getInComponents()) {
 			// you can get the object instance of this UID from the runtime model
 			CInstance inst = getCubeAgent().getRuntimeModel().getCInstance(instanceUID);
 			Chain mchain = this.cfactory.getCiliaContext().getApplicationRuntime().getChain(chainId);
			if(mchain.getMediator(inst.getLocalId()) != null) {
 				chain.bind().from(inst.getLocalId()+":unique").to(i.getLocalId()+":unique");
 			}
 		}
 	}
 
 	public synchronized void stop() {
 //		try {
 //			if (cfactory != null && cfactory.getCiliaContext()!=null && cfactory.getCiliaContext().getApplicationRuntime() != null){
 //				cfactory.getCiliaContext().getApplicationRuntime().stopChain(chainId);
 //				Builder b = cfactory.getCiliaContext().getBuilder();
 //				b.remove(chainId);
 //			}
 //		} catch (CiliaException e) {
 //		}
 		// remove chain
 	}
 
 	public void stateChanged(CInstance coi, int oldState, int newState) {
 		// not yet implemented in cube!!
 	}
 
 	@Override
 	public String toString() {
 		String out = "";
 		out += "+ CiliaExtension\n";
 
 		// TODO print what you want :)
 		// when we call the "ext" command, this toString method is called to show 
 		// some information about this extension internal work.
 
 		return out;
 	}
 
 
 }
