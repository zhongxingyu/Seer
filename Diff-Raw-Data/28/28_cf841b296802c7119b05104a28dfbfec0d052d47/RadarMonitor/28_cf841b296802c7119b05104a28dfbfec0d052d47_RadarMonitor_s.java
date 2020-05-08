 package vms;
 
 import java.util.*;
 
 import vms.Alert.AlertType;
 
 import common.Coord;
 import common.UpdateData;
 import common.Vessel;
 
 
 public class RadarMonitor implements ConnectionServer.Observer {
 	
 	private Coord lowerRange;
 	private Coord upperRange;
 	private ArrayList<Vessel> _Vessels = new ArrayList<Vessel>();
 	private List<Observer> _Observers = new ArrayList<Observer>();
 	
 	public interface Observer {
 		public void refresh(List<Alert> alerts, List<Vessel> vessels);
 	}
 	
 	public void setRange(Coord lowerRange, Coord upperRange) {
 		this.lowerRange = lowerRange;
 		this.upperRange = upperRange;
 	}
 	
 	public int getVesselCount() {
 		return _Vessels.size();
 	}
 	
 	public List<Vessel> getVessels() {
 		return _Vessels;
 	}
 	
 	public void registerObserver(Observer o) {
 		if (!_Observers.contains(o)) _Observers.add(o);
 	}
 	
 	public void unregisterObserver(Observer o) {
 		_Observers.remove(o);
 	}
 
 	@Override
 	public void update(UpdateData data) {
 		boolean isInList = false;
 		for(int i = 0; i<_Vessels.size(); i++){
 			if(_Vessels.get(i).getId().equals(data.Id) && _Vessels.get(i).getType() == data.Type){
 				_Vessels.get(i).update(data);
 				isInList = true;
 			}
 		}
 		
 		if(!isInList){
 			Vessel newVessel = new Vessel(data.Id, data.Type);
 			newVessel.update(data);
 			_Vessels.add(newVessel);
 		}
		
 	}
 
 	@Override
 	public void refresh(Calendar timestamp) {
 
 		ArrayList<Alert> _Alerts = new ArrayList<Alert>();
 		for(int i=0; i< _Vessels.size(); i++){
 			for(int j=i+1; j<_Vessels.size(); j++){
 				String risk = "none";
 				Alert newAlert;
 				
				try{
					Coord v1Coords = _Vessels.get(i).getCoord(timestamp);
					Coord v2Coords = _Vessels.get(j).getCoord(timestamp);
 					
 					double deltaX = v1Coords.x() - v2Coords.x();
 					double deltaY = v1Coords.y() - v2Coords.y();
 					double distance = Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaY, 2.0));
 					
 					if (distance < 50){
 						risk = "high";
						newAlert = new Alert(AlertType.HIGHRISK, _Vessels);
 						_Alerts.add(newAlert);
 					}
 					else if (distance < 200 && risk != "high"){
 						risk = "low";
						newAlert = new Alert(AlertType.LOWRISK, _Vessels);
 						_Alerts.add(newAlert);
 					}
 				}catch (IllegalStateException e) {
 					System.out.println(e.getMessage());
 				}
 			}
 		}
 		
 		for (int i=0; i < _Observers.size(); i++) {
 			_Observers.get(i).refresh(_Alerts,_Vessels);
 		}
 	}
 }
