 package v1.Impl.UI;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import v1.Impl.Controller;
 import v1.Interface.Horloge;
 
 /**
  * @(#) IHM.java
  */
 
 public class IHM extends JFrame {
 
 	private Controller ctl;
 	private Buzzer bzzr;
 	
 	private StartButton startBtn;
 	private StopButton stopBtn;
 	
 	private DecButton decBtn;
 	private IncButton incBtn;
 	
 	private MoletteSlider moletteSldr;
 	
 	private Afficheur aff;
 	private Led ledTemps;
 	private Led ledMesure;
 	private JPanel barreDeBouttons;
 	
 	private int tempo;
 
 	public IHM(Controller ctl) {
 		this.ctl = ctl;
 		bzzr = new Buzzer();
 		stopBtn = new StopButton(this);
 		startBtn = new StartButton(this);
 		decBtn = new DecButton(this);
 		incBtn = new IncButton(this);
 		moletteSldr = new MoletteSlider(this);
 		aff = new Afficheur(this);
 		ledTemps = new Led(Color.GREEN);
 		ledMesure = new Led(Color.RED);
 		
 		tempoHasChanged(moletteSldr.getValue());
 		
 		this.setPreferredSize(new Dimension(300, 150));
 		this.setVisible(true);
 		this.setLayout(new BorderLayout());
 		
 		
 		barreDeBouttons = new JPanel();
 		barreDeBouttons.setLayout(new FlowLayout());
 		barreDeBouttons.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		barreDeBouttons.add(startBtn);
 		barreDeBouttons.add(stopBtn);
 		barreDeBouttons.add(incBtn);
 		barreDeBouttons.add(decBtn);
		
 		this.add(barreDeBouttons, BorderLayout.SOUTH);
 		
 		
 		this.add(moletteSldr, BorderLayout.NORTH);
 		
		this.add(ledTemps, BorderLayout.WEST);
		this.add(ledMesure, BorderLayout.EAST);
 		
		this.add(aff, BorderLayout.CENTER);
 		
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosed(WindowEvent e) {
 				super.windowClosed(e);
 				System.exit(0);
 			}
 		});
 		
 		this.pack();
 	}
 	
 	public void notifyTemps() {
 		ledTemps.update();
 		bzzr.update();
 	}
 	
 	public void notifyMesure() {
 		ledMesure.update();
 	}
 	
 	public void start() {
 		ctl.start();
 	}
 	
 	public void stop() {
 		ctl.stop();
 	}
 	
 	public void incMesure() {
 		ctl.inc();
 	}
 	
 	public void decMesure() {
 		ctl.dec();
 	}
 	
 	public void tempoHasChanged(int newTempo){
 		tempo = newTempo;
 		aff.setText(tempo + " bpm");
 		ctl.setTempo(tempo);
 	}
 
 	public int getTempo() {
 		return tempo;
 	}
 }
