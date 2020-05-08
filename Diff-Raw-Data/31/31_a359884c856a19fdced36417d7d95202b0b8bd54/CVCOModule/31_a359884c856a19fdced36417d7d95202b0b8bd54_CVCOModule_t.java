 package group1.project.synthlab.ihm.module.vco;
 
 import group1.project.synthlab.ihm.factory.CFactory;
 import group1.project.synthlab.ihm.module.IPModuleObserver;
 import group1.project.synthlab.module.vco.VCOModule;
 import group1.project.synthlab.port.IPort;
 
 public class CVCOModule extends VCOModule implements ICVCOModule {
 	protected IPVCOModule presentation;
 
 	public CVCOModule(CFactory factory) {
 		super(factory);
 		this.presentation = new PVCOModule(this);
 	}
 
 	public IPVCOModule getPresentation() {
 		return presentation;
 	}
 
 	@Override
 	public void cableConnected(IPort port) {		
 		super.cableConnected(port);
 		presentation.register((IPModuleObserver) port.getCable());
 	}
 
 	@Override
 	public void cableDisconnected(IPort port) {		
 		presentation.unregister((IPModuleObserver) port.getCable());
 		super.cableDisconnected(port);
 	}
 	
 	
 	
 	 
 }
