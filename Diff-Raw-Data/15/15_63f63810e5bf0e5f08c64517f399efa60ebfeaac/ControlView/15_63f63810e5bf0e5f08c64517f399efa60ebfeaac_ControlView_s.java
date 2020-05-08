 package chalmers.dax021308.ecosystem.view;
 
 import javax.swing.JPanel;
 import javax.swing.JButton;
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.util.Log;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Font;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * The view that holds the controls for starting, stopping and pausing a simulation.
  * A panel that is part of the frame that holds the entire application.
  * 
  * @author Hanna 
  *
  */
 
 public class ControlView extends JPanel {
 	private static final long serialVersionUID = 5134812517352174052L;
 
 	/**
 	 * Create the panel.
 	 */
 	public ControlView(final EcoWorld ew) {  
 		
 		JButton btnStart = new JButton("Start");
 		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		btnStart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					ew.start();
 				} catch (IllegalStateException ex) {
 					Log.v("EcoWorld already stopped");
 				}
 			}
 		});
		setLayout(new MigLayout("", "[63px][61px][69px]", "[25px]"));
 		add(btnStart, "cell 0 0,alignx left,aligny top");
 		
 		JButton btnStop = new JButton("Stop");
 		btnStop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					ew.stop();
 				} catch (IllegalStateException ex) {
 					Log.v("EcoWorld already stopped");
 				}
 			}
 		});
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		add(btnStop, "cell 1 0,alignx left,aligny top");
 		
 		JButton btnPause = new JButton("Pause");
 		btnPause.setFont(new Font("Tahoma", Font.PLAIN, 14));
 		btnPause.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					ew.pause();
 				} catch (IllegalStateException ex) {
 					//Don't care.
 				}
				new NewSimulationView(ew);
 			}
 		});
		add(btnPause, "cell 2 0,alignx left,aligny top");
 		
 
 		JButton btnStartNew = new JButton("Start new Simulation");
 		btnStartNew.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					ew.stop();
 				} catch (IllegalStateException ex) {
 					//Don't care.
 				}
 				new NewSimulationView(ew);
 			}
 		});
 		btnStartNew.setFont(new Font("Tahoma", Font.PLAIN, 14));
		add(btnStartNew, "cell 1 0,alignx left,aligny top");
 
 	}
 
 }
