 package fr.istic.synthlab.controller.synthesizer;
 
 import java.awt.Color;
 
 import fr.istic.synthlab.abstraction.module.IModule;
 import fr.istic.synthlab.abstraction.synthesizer.Synthesizer;
 import fr.istic.synthlab.abstraction.wire.IWire;
 import fr.istic.synthlab.controller.module.ICModule;
 import fr.istic.synthlab.controller.wire.ICWire;
 import fr.istic.synthlab.factory.impl.PACFactory;
 import fr.istic.synthlab.presentation.synthesizer.IPSynthesizer;
 
 public class CSynthesizer extends Synthesizer implements ICSynthesizer {
 
 	private IPSynthesizer pres;
	private Color currentWireColor = Color.GRAY;
 
 	private static ICSynthesizer instance;
 	/**
 	 * @return the synthesizer's instance
 	 */
 	public static ICSynthesizer getInstance() {
 		if (instance == null) {
 			instance = (ICSynthesizer) PACFactory.getFactory().newSynthesizer();
 		}
 		return instance;
 	}
 	
 	public CSynthesizer() {
 		super();
 		this.pres = PACFactory.getPFactory().newSynthesizer();
 		instance = this;
 	}
 
 	@Override
 	public void start() {
 		super.start();
 		if (isRunning()) {
 			pres.c2pStart();
 		}
 	}
 
 	@Override
 	public void stop() {
 		super.stop();
 		if (!isRunning()) {
 			pres.c2pStop();
 		}
 	}
 
 	@Override
 	public IPSynthesizer getPresentation() {
 		return pres;
 	}
 
 	@Override
 	public void add(IModule module) {
 		super.add(module);
 		pres.c2pAddModule(((ICModule) module).getPresentation());
 	}
 
 	@Override
 	public void add(IWire wire) {
 		super.add(wire);
 		pres.c2pAddWire(((ICWire) wire).getPresentation());
 	}
 
 	@Override
 	public void setCurrentWire(IWire wire) {
 		if (wire != null) {
 			ICWire cWire = (ICWire) wire;
 			pres.c2pAddWire(cWire.getPresentation());
 		}
 		super.setCurrentWire(wire);
 	}
 
 	@Override
 	public void p2cStart() {
 		this.start();
 	}
 
 	@Override
 	public void p2cStop() {
 		this.stop();
 	}
 
 	@Override
 	public void p2cAddModule(ICModule module) {
 		this.add(module);
 		pres.c2pAddModuleOk(module.getPresentation());
 	}
 
 	@Override
 	public void p2cRemoveModule(ICModule module) {
 		this.remove(module);
 		pres.c2pRemoveModuleOk(module.getPresentation());
 	}
 
 	@Override
 	public void p2cDisconnectCurrentWire() {
 		if(getCurrentWire() != null)
 			getCurrentWire().disconnect();
 	}
 
 	@Override
 	public void setCurrentWireColor(Color currentWireColor) {
 		this.currentWireColor = currentWireColor;
 	}
 
 	@Override
 	public Color getCurrentWireColor() {
 		return currentWireColor;
 	}
 }
