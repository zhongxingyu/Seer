 package fr.istic.synthlab.presentation.module.audioscope;
 
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 
 import com.jsyn.scope.AudioScopeModel;
 import com.jsyn.scope.swing.AudioScopeView;
 
 import fr.istic.synthlab.controller.module.audioscope.ICModuleAudioScope;
 import fr.istic.synthlab.controller.port.ICInputPort;
 import fr.istic.synthlab.controller.port.ICOutputPort;
 import fr.istic.synthlab.presentation.module.APModule;
 import fr.istic.synthlab.presentation.port.PInputPort;
 import fr.istic.synthlab.presentation.port.POutputPort;
 
 /**
  * Presentation for a OUT module
  */
 public class PModuleAudioScope extends APModule implements IPModuleAudioScope {
 
 	private static final long serialVersionUID = -8519084219674310285L;
 
 	private ICModuleAudioScope ctrl;
 	private AudioScopeModel model;
 	
 	private PInputPort inputPort;
 	private POutputPort outputPort;
 
 	/**
 	 * @param control
 	 */
 	public PModuleAudioScope(ICModuleAudioScope control) {
 		super(control);
 		this.ctrl = control;
 		configView();
 		defineCallbacks();
 	}
 
 	private void configView() {
 		JPanel panelScope= new JPanel();
 		JPanel panelPort = new JPanel();
 		
 		panelScope.setOpaque(false);
 		panelPort.setOpaque(false);
 
 		AudioScopeView scope = new AudioScopeView();
 		model = ctrl.getModel();
 		scope.setModel(model);
 		scope.setControlsVisible(true);
 		panelScope.add(scope);
 
 		inputPort = (PInputPort) ((ICInputPort) ctrl.getInput()).getPresentation();
 		outputPort = (POutputPort) ((ICOutputPort) ctrl.getOutput()).getPresentation();
 		panelPort.add(inputPort);
 		panelPort.add(outputPort);
 
 		this.setAutoscrolls(true);
 		
 		super.setWidth(630);
		super.setHeigth(350);
 		
 		Dimension size = new Dimension(super.getWidth(), super.getHeight());
 		this.setSize(size);
 		this.setPreferredSize(size);
 		
 		this.addTitleBar();
		this.addPanel(panelScope, 630, 250);
 		this.addPanel(panelPort, 630, 60);
 
 	}
 
 	private void defineCallbacks() {
 	}
 	
 
 	@Override
 	public ICModuleAudioScope getControl() {
 		return ctrl;
 	}
 
 }
