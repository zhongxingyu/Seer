 package fr.istic.synthlab.controller.module.mix;
 
 import fr.istic.synthlab.ISynthApp;
 import fr.istic.synthlab.abstraction.module.mix.ModuleMIX;
 import fr.istic.synthlab.abstraction.synthesizer.ISynthesizer;
 import fr.istic.synthlab.abstraction.wire.IWire;
 import fr.istic.synthlab.controller.module.ICModule;
import fr.istic.synthlab.controller.synthesizer.ICSynthesizer;
 import fr.istic.synthlab.factory.impl.PACFactory;
 import fr.istic.synthlab.presentation.module.mix.IPModuleMIX;
 import fr.istic.synthlab.presentation.synthesizer.IPSynthesizer;
 
 public class CModuleMIX extends ModuleMIX implements ICModuleMIX {
 
 	private IPModuleMIX pres;
	private ISynthesizer cSynthesizer;
 
 	public CModuleMIX(ISynthesizer synth) {
 		super(synth);
		this.cSynthesizer = synth;
 		this.pres = PACFactory.getPFactory().newMIX(this);
 	}
 
 	@Override
 	public IPModuleMIX getPresentation() {
 		return this.pres;
 	}
 
 	@Override
 	public void p2cClosing() {
 		for (IWire w : this.getWires()) {
 			w.disconnect();
 		}
 	}
 
 	private void changeGain1(double gain) {
 		setAttenuation1(gain);
 		pres.c2pSetGain1Value(getAttenuation1());
 	}
 
 	private void changeGain2(double gain) {
 		setAttenuation2(gain);
 		pres.c2pSetGain2Value(getAttenuation2());
 	}
 
 	private void changeGain3(double gain) {
 		setAttenuation3(gain);
 		pres.c2pSetGain3Value(getAttenuation3());
 	}
 
 	private void changeGain4(double gain) {
 		setAttenuation4(gain);
 		pres.c2pSetGain4Value(getAttenuation4());
 	}
 
 	@Override
 	public void p2cGain1Changed(double gain) {
 		changeGain1(gain);
 	}
 
 	@Override
 	public void p2cGain2Changed(double gain) {
 		changeGain2(gain);
 	}
 
 	@Override
 	public void p2cGain3Changed(double gain) {
 		changeGain3(gain);
 	}
 
 	@Override
 	public void p2cGain4Changed(double gain) {
 		changeGain4(gain);
 	}
 
 	@Override
 	public void setParameter(String key, Double value) {
 		if (key.equals("attenuation1")) {
 			changeGain1(value);
 		} else if (key.equals("attenuation2")) {
 			changeGain2(value);
 		} else if (key.equals("attenuation3")) {
 			changeGain3(value);
 		} else if (key.equals("attenuation4")) {
 			changeGain4(value);
 		}
 	}
 
 	@Override
 	public IPSynthesizer getSynthesizerPresentation() {
		return ((ICSynthesizer)cSynthesizer).getPresentation();
 	}
	
 
 	@Override
 	public void p2cRemoveModule(ICModule module) {
		((ICSynthesizer) cSynthesizer).p2cRemoveModule(module);
 	}
 }
