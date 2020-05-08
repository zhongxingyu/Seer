 package group1.project.synthlab.module.vco;
 
 import group1.project.synthlab.cable.Cable;
 import group1.project.synthlab.cable.ICable;
 import group1.project.synthlab.factory.Factory;
 import group1.project.synthlab.module.Module;
 import group1.project.synthlab.module.out.OutModule;
 import group1.project.synthlab.module.out.OutModule.Distribution;
 import group1.project.synthlab.port.IPort;
 import group1.project.synthlab.port.IPortObserver;
 import group1.project.synthlab.port.in.IInPort;
 import group1.project.synthlab.port.out.IOutPort;
 
 import javax.swing.JFrame;
 
 import com.jsyn.JSyn;
 import com.jsyn.Synthesizer;
 import com.jsyn.scope.AudioScope;
 import com.jsyn.unitgen.Multiply;
 import com.jsyn.unitgen.PassThrough;
 import com.jsyn.unitgen.PowerOfTwo;
 import com.jsyn.unitgen.SineOscillator;
 import com.jsyn.unitgen.SquareOscillator;
 import com.jsyn.unitgen.TriangleOscillator;
 
 public class VCOModule extends Module implements IPortObserver, IVCOModule {
 
 	// Modulation de fréquence connectée ou pas
 	protected boolean fmConnected;
 	
 	// Amplitude par défaut
	public static final double amin = 0;
 	public static final double a0 = 0.1;
 	
 	// Fréquences max, min et de base par défaut :
 	public static final double fmin = 0;
 	public static final double fmax = 6000;
 	public static double f0 = 300;
 	
 	// Un oscillateur pour chaque forme d'onde
 	protected SineOscillator sineOsc;
 	protected SquareOscillator squareOsc;
 	protected TriangleOscillator triangleOsc;
 
 	// Port d'entrée : modulation de fréquence
 	protected IInPort fm;
 	
 	// Un PassThrough pour envoyer la modulation de fréquence vers l'entrée des 3 oscillateurs
 	private PassThrough passThrough;
 	
 	// Ports de sortie : 1 pour chaque forme d'ondes
 	protected IOutPort outSine;
 	protected IOutPort outSquare;
 	protected IOutPort outTriangle;
 	
 	// Réglage manuel de la fréquence
 	protected int octaveSetting; // entre 0 et 9 
 	protected double fineAdjustment; // entre 0 et 1
 	
 	// Est allumé
 	protected boolean isOn;
 	
 	// Constructeur
 	public VCOModule(Factory factory) {
 		super("VCO-" + moduleCount, factory);
 		
 		// Création des oscillateurs
 		sineOsc = new SineOscillator();
 		squareOsc = new SquareOscillator();
 		triangleOsc = new TriangleOscillator();
 		
 		circuit.add(sineOsc);
 		circuit.add(squareOsc);
 		circuit.add(triangleOsc);
 		
 		// Création du PassThrough : on applique la formule f0 * 2 ^ (5Vfm) au signal en entrée et on envoie la sortie dans un passThrough
 		// On doit multiplier Vfm par 5 car JSyn considère des amplitudes entre -1 et 1, et nous considérons des tensions entre -5V et +5V)
 		passThrough = new PassThrough();
 		Multiply multiply5 = new Multiply();
 		multiply5.inputB.set(5); 
 		PowerOfTwo poweroftwo = new PowerOfTwo();
 		multiply5.output.connect(poweroftwo.input);
 		Multiply multiplyf0 = new Multiply();
 		multiplyf0.inputB.set(f0);
 		multiplyf0.inputA.connect(poweroftwo.output);
 		passThrough.input.connect(multiplyf0.output);
 		
 		// Port d'entrée : 
 		fm = factory.createInPort("fm", multiply5.inputA, this);
 		// Ports de sortie
 		outSine = factory.createOutPort("outsine", sineOsc.output, this);
 		outSquare = factory.createOutPort("outsquare", squareOsc.output, this);
 		outTriangle = factory.createOutPort("outtriangle", triangleOsc.output, this);
 		
 		// Quand on crée notre VCO, il n'a pas de signal en entrée, donc la fréquence vaut f0
 		fmConnected = false;
 		
 		// On règle les fréquences et amplitudes des oscillateurs aux valeurs par défaut
		sineOsc.amplitude.set(amin);
		squareOsc.amplitude.set(amin);
		triangleOsc.amplitude.set(amin);
 		sineOsc.frequency.set(f0);
 		squareOsc.frequency.set(f0);
 		triangleOsc.frequency.set(f0);
 		
		
 	}
 	
 	// Fonction appelée lorsque les réglages sont modifiées sur l'IHM
 	private void changeFrequency(){
 		if(!fmConnected){
 			double newFrequency = (octaveSetting + fineAdjustment) * ((fmax - fmin) / 10);
 			f0 = newFrequency;
 			sineOsc.frequency.set(f0);
 			squareOsc.frequency.set(f0);
 			triangleOsc.frequency.set(f0);
 		}
 	}
 	
 	// Fonction qui gère la connexion à l'entrée FM, et donc le passage à la modulation de fréquence par un signal en entrée
 	// La fonction sera donc appelée lorsqu'on connectera un cable au port fm
 	public void cableConnected(IPort port) {
 		if(port == fm){ // Si un câble vient d'être connecté dans l'entrée fm, on "active" la modulation de fréquence
 			System.out.println("\nConnexion d'un câble dans l'entrée fm !");
 			fmConnected = true;
 			passThrough.output.connect(sineOsc.frequency);
 			passThrough.output.connect(squareOsc.frequency);
 			passThrough.output.connect(triangleOsc.frequency);
 		}
 	}
 
 	// Fonction qui gère la déconnexion à l'entrée FM
 	public void cableDisconnected(IPort port) {
 		if(port == fm){ // Si un câble vient d'être déconnecté de l'entrée fm, on remet la fréquence des oscillateurs à f0
 			fmConnected = false;
 			passThrough.output.disconnectAll();
 		}
 	}
 	
 	public void start() {
 		circuit.start();
		sineOsc.amplitude.set(a0);
		squareOsc.amplitude.set(a0);
		triangleOsc.amplitude.set(a0);
 		isOn = true;
 	}
 
 	public void stop() {
 		circuit.stop();
		sineOsc.amplitude.set(amin);
		squareOsc.amplitude.set(amin);
		triangleOsc.amplitude.set(amin);
 		isOn = false;
 	}
 	
 	public IInPort getFm() {
 		return fm;
 	}
 
 	public IOutPort getOutSine() {
 		return outSine;
 	}
 
 
 	public IOutPort getOutSquare() {
 		return outSquare;
 	}
 
 	public IOutPort getOutTriangle() {
 		return outTriangle;
 	}
 
 	
 	public int getOctave() {
 		return octaveSetting;
 	}
 
 	public void setOctave(int reglageoctave) {
 		this.octaveSetting = reglageoctave;
 		changeFrequency();
 	}
 
 	public double getFineAdjustment() {
 		return fineAdjustment;
 	}
 
 	public void setFineAdjustment(double reglagefin) {
 		this.fineAdjustment = reglagefin;
 		changeFrequency();
 	}
 
 	public boolean isStarted() {
 		return isOn;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public static void main(String[] args) throws Throwable{
 		//Factory
 		Factory factory = new Factory();
 		
 		// On crée et démarre le Synthesizer
 		Synthesizer synth = JSyn.createSynthesizer();
 		synth.start();
 		
 		// On crée notre VCO (pas de signal fm en entrée) et on ajoute le circuit créé au Synthesizer
 		VCOModule vco = (VCOModule) factory.createVCOModule();
 		synth.add(vco.getCircuit());
 
 		// LineOut sera remplacé par OutModule
 		OutModule out = new OutModule(factory);
 		out.setDistribution(Distribution.NORMAL);
 		synth.add(out.getCircuit());
 		out.start();
 		
 		// On connecte la sortie sinusoïdale de notre VCO au module de sortie
 		ICable c1 =factory.createCable();
 		c1.setOutPort(vco.getOutSine());
 		c1.setInPort(out.getLeftPort());
 		
 		// On crée un VCO dont on va utiliser la sortie sinusoïdale pour moduler la fréquence de notre premier vco
 		VCOModule fm = new VCOModule(factory);
 		synth.add(fm.getCircuit());
 		fm.triangleOsc.frequency.set(10); // une valeur petite permet de déformer l'onde de façon à ce que cetet déformation soit audible suffisament longtemps pour être humain
 		// Si l'on considère des tensions comprises entre -5V et +5V, cela correspond à une amplitude de 0,5JSyn, donc une amplitude crète à crète de 1JSyn (1 JSyn == 5V == amplitude max)
 		// Ainsi, quand on passe d'un sommet à un creux, on divise la fréquence par 2, et quand on passe d'un creux à un sommet on multiplie la fréquence par 2
 		fm.triangleOsc.amplitude.set(0.5);
 	
 		// Pour l'affichage des courbes
 		AudioScope scope= new AudioScope( synth );
 		scope.addProbe(vco.sineOsc.output);
 		scope.addProbe(fm.triangleOsc.output);
 		scope.setTriggerMode( AudioScope.TriggerMode.AUTO );
 		scope.getModel().getTriggerModel().getLevelModel().setDoubleValue( 0.0001 );
 		scope.getView().setShowControls( true );
 		scope.start();
 		JFrame frame = new JFrame();
 		frame.add(scope.getView());
 		frame.pack();
 		frame.setVisible(true);
 		
 		// Sans modulation de fréquence pendant 3s
 		try
 		{
 			double time = synth.getCurrentTime();
 			// Sleep for a few seconds.
 			synth.sleepUntil( time + 5.0 );
 		} catch( InterruptedException e )
 		{
 			e.printStackTrace();
 		}
 		
 		// Avec modulation de fréquence pendant quelques secondes
 		// On connecte la sortie de notre VCO fm vers le port d'entrée fm de notre VCO vco
 		ICable c2 = factory.createCable();
 		c2.setOutPort(fm.getOutTriangle());
 		c2.setInPort(vco.getFm());
 
 		// On vérifie que la fréquence est bien divisée par 2 ou multipliée par 2 quand on passe d'une crète à la suivante
 		int i = 0;
 		while(i<30) {
 			System.out.println("Fréquence = " + vco.sineOsc.frequency.getValue());
 			i++;
 			try {
 				synth.sleepUntil( synth.getCurrentTime() + 0.5 );
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		// Sans modulation de fréquence le reste du temps, avec une fréquence réglée un peu plus haut
 		//fm.squareosc.output.disconnectAll();
 		c2.disconnect();
 		vco.setOctave(1);
 		vco.setFineAdjustment(0.2);
 	}
 
 
 	
 }
