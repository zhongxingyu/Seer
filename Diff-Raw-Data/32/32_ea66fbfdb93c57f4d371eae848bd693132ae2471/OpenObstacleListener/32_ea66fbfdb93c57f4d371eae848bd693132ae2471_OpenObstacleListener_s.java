 package Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.LoadXMLFile;
 import Model.Obstacle;
 import View.MainFrame;
 
 public class OpenObstacleListener implements ActionListener, AirportObserver{
 
 	Airport airport;
 	List<AirportObserver> airportObservers;
 
 	public OpenObstacleListener(Airport airport, List<AirportObserver> airportObserver){
 		this.airportObservers = airportObserver;
 		this.airport = airport;
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		if(airport.getPhysicalRunways().size() == 0){
 			JOptionPane.showMessageDialog(null, "Airport does not contain any physical runways\r\nPlease add one by going to Edit > Airport", "", JOptionPane.ERROR_MESSAGE);
 		} else if (airport.getCurrentPhysicalRunway() == null){
 			JOptionPane.showMessageDialog(null, "Please select a physical runway", "", JOptionPane.ERROR_MESSAGE);
 		} else if (airport.getCurrentPhysicalRunway().getObstacle() == null){
 			LoadXMLFile lf = new LoadXMLFile();
 			try {
 				Obstacle o = lf.loadObstacle();
 				airport.getCurrentPhysicalRunway().setObstacle(o);
 
 				try {
 					MainFrame.saveRecentFile(o, LocalPathTrimmer.trimLocalPath(lf.getFile()));
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 				System.out.println("Obstacle Opened");
 				notifyAirportObservers();
 

 
 
 			} catch (Exception e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 
 		}
 
 		@Override
 		public void updateAirport(Airport airport) {
 			this.airport = airport;
 		}
 
 		void notifyAirportObservers() {
 			for(AirportObserver ao: airportObservers){
 				ao.updateAirport(airport);
 			}
 		}
 	}
