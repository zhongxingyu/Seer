 package fr.istic.synthlab.presentation.synthesizer;
 
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.AWTEventListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 
 import fr.istic.synthlab.abstraction.port.IInputPort;
 import fr.istic.synthlab.abstraction.port.IOutputPort;
 import fr.istic.synthlab.controller.synthesizer.ICSynthesizer;
 import fr.istic.synthlab.controller.wire.ICWire;
 import fr.istic.synthlab.presentation.module.APModule;
 import fr.istic.synthlab.presentation.module.IPModule;
 import fr.istic.synthlab.presentation.port.PPort;
 import fr.istic.synthlab.presentation.wire.IPWire;
 import fr.istic.synthlab.presentation.wire.PWire;
 
 public class PSynthesizer extends JLayeredPane implements IPSynthesizer {
 
 	private static final long serialVersionUID = -1444696064954307756L;
 
 	private List<IPModule> modules;
 	private ICSynthesizer cSynthesizer;
 
 	public PSynthesizer(ICSynthesizer cSynthesizer) {
 		super();
 		this.cSynthesizer = cSynthesizer;
 		modules = new ArrayList<IPModule>();
 		configView();
 		defineCallbacks();
 	}
 
 	private void configView() {
 		this.setBackground(Color.DARK_GRAY);
 	}
 
 	private void defineCallbacks() {
 
 		/** Gestion de l'affichage du cable en cours */
 		Toolkit kit = Toolkit.getDefaultToolkit();
 		kit.addAWTEventListener(new AWTEventListener() {
 			@Override
 			public void eventDispatched(AWTEvent event) {
 				if (event instanceof MouseEvent) {
 					// Gestion du currentWire
 					if (cSynthesizer.getCurrentWire() != null) {
 						IInputPort input = cSynthesizer.getCurrentWire().getInput();
 						IOutputPort output = cSynthesizer.getCurrentWire().getOutput();
 	
 						Point mouse = ((PSynthesizer) cSynthesizer.getPresentation()).getMousePosition(true);
 						
 						if (input == null && output != null) {
 							((ICWire) cSynthesizer.getCurrentWire())
 									.getPresentation().setInputPoint(mouse);
 						}
 						if (output == null && input != null) {
 							((ICWire) cSynthesizer.getCurrentWire())
 									.getPresentation().setOutputPoint(mouse);
 						}
 					}
 				}
 			}
 
 		}, AWTEvent.MOUSE_MOTION_EVENT_MASK);
 
 		/** Gestion des cliques */
 		kit.addAWTEventListener(new AWTEventListener() {
 			@Override
 			public void eventDispatched(AWTEvent event) {
 				if (event instanceof MouseEvent) {
 					MouseEvent clickEvent = (MouseEvent) event;
 					
 					if(!(event.getSource() instanceof PPort)){
 						
 		                /** Suppression du cable avec le bouton droit ou gauche de la souris */
 						if( clickEvent.getButton() == MouseEvent.BUTTON1 || clickEvent.getButton() == MouseEvent.BUTTON3){
 							cSynthesizer.p2cDisconnectCurrentWire();
 						}
 					}
 				}
 			}
 		}, AWTEvent.MOUSE_EVENT_MASK);
 	}
 
 	@Override
 	public void start() {
 		cSynthesizer.p2cStart();
 	}
 
 	@Override
 	public void stop() {
 		cSynthesizer.p2cStop();
 	}
 
 	@Override
 	public void addModule(IPModule module) {
 		cSynthesizer.p2cAddModule(module.getControl());
 	}
 
 	@Override
 	public void removeModule(IPModule module) {
 		cSynthesizer.p2cRemoveModule(module.getControl());
 	}
 
 	@Override
 	public void c2pStart() {
 		validate();
 		repaint();
 	}
 
 	@Override
 	public void c2pStop() {
 		validate();
 		repaint();
 	}
 
 	@Override
 	public void c2pAddModule(IPModule module) {
 		((APModule) module).setVisible(true);
 		this.add((JPanel) module, 0);
 
 		System.out.println(module.getPosition());
 
 		int x = 0;
 		int y = 0;
 
 		if ((module.getPosition().x == 0) && (module.getPosition().y == 0)) {
 			// On place les module en fonction de leur nombre pour évité les recouvrements
 			x = (modules.size()+1) * 10;
 			y = ((modules.size()%15)+1) * 20;
 		} else {
 			// Placement depuis xml
 			x = module.getPosition().x;
 			y = module.getPosition().y;
 		}
 
 		((APModule) module).setBounds(x, y, module.getWidth(), module.getHeight());
 
 		modules.add(module);
 		((APModule) module).validate();
 		((APModule) module).repaint();
 
 		validate();
 		repaint();
 	}
 
 	@Override
 	public void c2pAddModuleOk(IPModule module) {
 
 	}
 
 	@Override
 	public void c2pAddWire(IPWire wire) {
 		this.add((PWire) wire);
		wire.updateDisplay();
 		validate();
 		repaint();
 	}
 
 	@Override
 	public void c2pRemoveModuleOk(IPModule module) {
 		this.remove((APModule) module);
 		modules.remove(module);
 		validate();
 		repaint();
 	}
 
 	@Override
 	public void removeWire(IPWire pres) {
 		this.remove((PWire) pres);
 		validate();
 		repaint();
 	}
 
 }
