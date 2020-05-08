 package group1.project.synthlab.module.noise;
 
 import group1.project.synthlab.factory.Factory;
 import group1.project.synthlab.module.Module;
 import group1.project.synthlab.port.IPort;
 import group1.project.synthlab.port.out.IOutPort;
 import group1.project.synthlab.signal.Tools;
 import group1.project.synthlab.unitExtension.producer.noise.BrownianNoise;
 import com.jsyn.JSyn;
 import com.jsyn.Synthesizer;
 import com.jsyn.unitgen.LineOut;
 import com.jsyn.unitgen.Multiply;
 import com.jsyn.unitgen.PinkNoise;
 import com.jsyn.unitgen.WhiteNoise;
 
 /**
  * @author Groupe 1
  * Produit un bruit
  */
 public class NoiseModule extends Module implements INoiseModule {
 
 	private static final long serialVersionUID = -2115003852547928273L;
 
 	protected static int moduleCount = 0;
 	
 	/** Generateur de bruit blanc de JSyn */
 	protected transient WhiteNoise whiteNoise;
 	
 	/** Generateur de bruit Rouge/Brun de JSyn */
 	// TODO : Red Noise = Brown Noise ?
 	protected transient BrownianNoise brownianNoise;
 	
 	/** Generateur de bruit rose */
 	protected transient PinkNoise pinkNoise;
 	
 	/** Port de sortie pour le bruit blanc */
 	protected IOutPort outWhite;
 	
 	/** Port de sortie pour le bruit rouge */
 	protected IOutPort outBrownian;
     
 	/** Port de sortie pour le bruit rose */
 	protected IOutPort outPink;
 	
 	/** Pour le on/off du bruit blanc */
 	protected transient Multiply onoffWhite;
 	
 	/** Pour le on/off du bruit rose */
 	protected transient Multiply onoffPink;
 	
 	/** Pour le on/off du bruit rouge/brun */
 	protected transient Multiply onoffBrownian;
 	
 	public NoiseModule(Factory factory) {
 		super("Noise-" + ++moduleCount, factory);
 		
 		// Les generateurs de bruit
 		whiteNoise = new WhiteNoise();
 		circuit.add(whiteNoise);
 		brownianNoise = new BrownianNoise();
 		circuit.add(brownianNoise);
 		pinkNoise = new PinkNoise();
 		circuit.add(pinkNoise);
 		
 		// Le on/off du bruit blanc
 		onoffWhite = new Multiply();
 		circuit.add(onoffWhite);
 		onoffWhite.inputB.set(0);
 		whiteNoise.output.connect(onoffWhite.inputA);
 		
 		// Le on/off du bruit rose
 		onoffPink = new Multiply();
 		circuit.add(onoffPink);
 		onoffPink.inputB.set(0);
 		pinkNoise.output.connect(onoffPink.inputA);
 		
 		// Le on/off du bruit rouge/brun
 		onoffBrownian = new Multiply();
 		circuit.add(onoffBrownian);
 		onoffBrownian.inputB.set(0);
 		brownianNoise.output.connect(onoffBrownian.inputA);
 		
 		// Ports de sortie
 		outWhite = factory.createOutPort("white", onoffWhite.output, this);
 		outBrownian = factory.createOutPort("brownian", onoffBrownian.output, this);
 		outPink = factory.createOutPort("pink", onoffPink.output, this);
 
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IModule#refresh()
 	 */
 	@Override
 	public void refresh() {
 		//Rien à faire
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.IModule#start()
 	 */
 	public void start() {
 		super.start();
 		onoffWhite.inputB.set(1);
 		onoffPink.inputB.set(1);
 		onoffBrownian.inputB.set(1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.IModule#stop()
 	 */
 	public void stop() {
 		super.stop();
 		onoffWhite.inputB.set(0);
 		onoffPink.inputB.set(0);
 		onoffBrownian.inputB.set(0);
 	}
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.IModule#destruct()
 	 */
 	public void destruct() {
 		if (outWhite.isUsed())
 			outWhite.getCable().disconnect();
		if (outBrownian.isUsed())
			outBrownian.getCable().disconnect();
 		if (outBrownian.isUsed())
 			outBrownian.getCable().disconnect();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.INoiseModule#stop()
 	 */
 	public void cableConnected(IPort port) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.INoiseModule#stop()
 	 */
 	public void cableDisconnected(IPort port) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.IVCOModule#getOutWhite()
 	 */
 	public IOutPort getOutWhite() {
 		return outWhite;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.IVCOModule#getOutRed()
 	 */
 	public IOutPort getOutBrownian() {
 		return outBrownian;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see group1.project.synthlab.module.IVCOModule#getOutPink()
 	 */
 	public IOutPort getOutPink() {
 		return outPink;
 	}
 	
 	/* (non-Javadoc)
 	 * @see group1.project.synthlab.module.IModule#resetCounterInstance()
 	 */
 	@Override
 	public void resetCounterInstance() {
 		NoiseModule.moduleCount = 0;		
 	}
 	
 	// Tests fonctionnels
 	public static void main(String[] args){
 		// Factory
 		Factory factory = new Factory();
 
 		// On cree et demarre le Synthesizer
 		Synthesizer synth = JSyn.createSynthesizer();
 		synth.start();
 
 		// On cree notre Module Noise et on ajoute le circuit cree au Synthesizer
 		NoiseModule noise = (NoiseModule) factory.createNoiseModule();
 		synth.add(noise.getCircuit());
 		// LineOut remplace ici OutModule
 		LineOut out = new LineOut();
 		synth.add(out);
 		out.start();
 
 		System.out.println("\n\n\n****************************************************\n");
 		System.out.println("Tests fonctionnels du Module Noise");
 		System.out.println("\n****************************************************\n\n");
 		
 		System.out.println("Demarrage du Module Noise");
 		noise.start();
 		
 		System.out.println("5s de bruit blanc");
 		out.input.disconnectAll();
 		out.input.connect(noise.getOutWhite().getJSynPort());
 		Tools.wait(synth, 5);
 		
 		System.out.println("5s de bruit rose");
 		out.input.disconnectAll();
 		out.input.connect(noise.getOutPink().getJSynPort());
 		Tools.wait(synth, 5);
 		
 		System.out.println("5s de bruit rouge/brun");
 		out.input.disconnectAll();
 		out.input.connect(noise.getOutBrownian().getJSynPort());
 		Tools.wait(synth, 5);
 		
 		System.out.println("Arret du Module Noise");
 		noise.stop();
 	}
 }
