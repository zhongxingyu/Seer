 package net.beadsproject.beads.play;
 
 import java.awt.Color;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.events.PauseTrigger;
 import net.beadsproject.beads.gui.BeadsColors;
 import net.beadsproject.beads.gui.BeadsPanel;
 import net.beadsproject.beads.gui.LevelMeter;
 import net.beadsproject.beads.gui.Slider;
 import net.beadsproject.beads.gui.Slider2D;
 import net.beadsproject.beads.ugens.Clock;
 import net.beadsproject.beads.ugens.Envelope;
 import net.beadsproject.beads.ugens.Function;
 import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Spatial;
 
 /**
  * The Class SongPart. 
  */
 public abstract class SongPart extends Gain implements InterfaceElement {
 
 	/** The params. */
 	protected ArrayList<InterfaceElement> interfaceElements;
 	
 	/** The panel. */
 	protected JComponent panel;
 	
 	protected int state;
 	
 	protected Clock clock;
 	
 	protected Gain controllableGain;
 	
 	protected Environment environment;
 	
 	private Color color;
 	
 	public SongPart(String name, Environment environment) {
 		this(name, environment, environment.ac.getAudioFormat().getChannels(), 2);
 	}
 	
 	/**
 	 * Instantiates a new song part.
 	 * 
 	 * @param environment
 	 *            the environment
 	 * @param inouts
 	 *            the inouts
 	 */
 	protected SongPart(String name, Environment environment, int destChannels, int sourceChannels) {
 		super(environment.ac, destChannels);
 		this.environment = environment;
 		setName(name);
 		pause(true);
 		state = 0;
 		//set up interface
 		color = BeadsColors.nextColor();
 		interfaceElements = new ArrayList<InterfaceElement>();
 		interfaceElements.add(new LevelMeter(this));
 		Slider s = new Slider(context, "gain", 0f, 2f, 0.2f, true);
 		interfaceElements.add(s);
 		//set up mixing stuff (more interface elements in setupPanner())
 		controllableGain = new Gain(context, sourceChannels);
 		controllableGain.setGainEnvelope(s);
 		if(destChannels == sourceChannels) {
 			addInput(controllableGain);
 		} else {
 			setupPanner(destChannels, sourceChannels);
 		}
 		setGainEnvelope(new Envelope(context, 0f));
 	}
 	
 	public void setupPanner(int destChannels, int sourceChannels) {
 		if(destChannels == 4) {
			Spatial mixer = new Spatial(context, 2);
 			addInput(mixer);
 			UGen[][] positionControllers = new UGen[sourceChannels][2];
 			if(sourceChannels == 2) {
 				//special case - both channels have same y-axis and are 0.3 on either side of x-axis
 				Slider x = new Slider(context, "x", 0f, 1f, 0.5f);
 				Slider y = new Slider(context, "y", 0f, 1f, 0.5f);
 				Slider2D s2d = new Slider2D(x, y);
 				positionControllers[0][0] = new Function(x) {
 					@Override
 					public float calculate() {
 						return x[0] - 0.3f;
 					}
 				};
 				positionControllers[1][0] = new Function(x) {
 					@Override
 					public float calculate() {
 						return x[0] + 0.3f;
 					}
 				};
 				positionControllers[0][1] = y;
 				positionControllers[1][1] = y;
 				interfaceElements.add(s2d);
 			} else {
 				for(int i = 0; i < sourceChannels; i++) {
 					Slider x = new Slider(context, "x", 0f, 1f, 0.5f);
 					Slider y = new Slider(context, "y", 0f, 1f, 0.5f);
 					positionControllers[i][0] = x;
 					positionControllers[i][1] = y;
 					Slider2D s2d = new Slider2D(x, y);
 					interfaceElements.add(s2d);
 				}
 			}
 			mixer.addInput(controllableGain, positionControllers);
 		} else if(destChannels == 8) {
 			System.err.println("Shizza, I don't know how to deal with 8 channels :-)");
 		}
 	}
 	
 	public void setState(int state) {
 		this.state = state;
 	}
 	
 	/**
 	 * Setup panel.
 	 */
 	private void setupPanel() {
 		panel = new BeadsPanel();
 		((BeadsPanel)panel).coloredLineBorder(getColor(), 5);
 		for(InterfaceElement p : interfaceElements) {
 			panel.add(p.getComponent());
 		}
 		if(this instanceof KeyListener) {
 			System.out.println("KEY LISTENER");
 			panel.addKeyListener((KeyListener)this);
 		}
 	}
 	
 	/**
 	 * Gets the panel.
 	 * 
 	 * @return the panel
 	 */
 	public JComponent getComponent() {
 		if(panel == null) setupPanel();
 		return panel;
 	}
 	
 	public void enter() {
 		((Envelope)getGainEnvelope()).lock(false);
 		getGainEnvelope().setValue(1f);
 	}
 
 	public void exit() {
 		((Envelope)getGainEnvelope()).clear();
 		((Envelope)getGainEnvelope()).addSegment(0f, 500f, new PauseTrigger(this));
 		((Envelope)getGainEnvelope()).lock(true);
 	}
 	
 	public final String toString() {
 		return getName() + " (" + getClass().getSimpleName() + ")";
 	}
 	
 	public Clock getClock() {
 		return clock;
 	}
 
 	
 	public void setClock(Clock clock) {
 		this.clock = clock;
 	}
 
 	public Color getColor() {
 		return color;
 	}
 
 	public void setColor(Color color) {
 		this.color = color;
 	}
 	
 }
