 package de.hohenheim.controller.events;
 
 import java.util.ArrayList;
 
 import de.hohenheim.modell.project.Project;
 import de.hohenheim.modell.timetable.Timetable;
 import de.hohenheim.modell.train.TrainData;
 import de.hohenheim.view.map.NodeMap;
 import de.hohenheim.view.mobile.TrainFigure;
 
 public class AnimationProcess {
 
 	public static AnimationPlay player = new AnimationPlay();
 
 	public static void startAnimations(Project p, NodeMap map) {
 		
 		for(TrainData train : p.getTraindataProjectList()){
 			//visible einbauen 
 			map.getMobileObjects().get(String.valueOf(train.getID())).startAnimation();		
 		}
 	}
 
 	public static void calculateSimulation(Project p, NodeMap map) {
 		sortedTrains(p.getTraindataProjectList(), p.getTimeTableProjectList());
 		int k = 0;
 		for (TrainData train : p.getTraindataProjectList()) {
 		     //Visible abfrage ?? 
 			Timetable tt = p.getTimeTableProjectList().get(k);
 			TrainFigure tf = (TrainFigure) map.getMobileObjects().get(String.valueOf(train.getID()));
 				if (tf != null && tt != null) {
 					if (tt.getVisits() == tt.getMiddlestations().size()){
 						
 						tt.setVisits(tt.getVisits() + 1);
 						AnimationEvents.walkTo(tf, map.getNodes().get(String.valueOf(tt.getEndstation())), map);
 						
 							
 					}else if(tt.getVisits()  < tt.getMiddlestations().size()){
 						
						tt.setVisits(tt.getVisits() + 1);
 						AnimationEvents.walkTo(tf,  map.getNodes().get(String.valueOf(tt.getMiddlestations().get(tt.getVisits()))), map);
 						
 					}else{
 					}
 				}
 				
 		    k++;
 		    }
 		}
 
 	private static void sortedTrains(
 			ArrayList<TrainData> traindataProjectList,
 			ArrayList<Timetable> timeTableProjectList) {
 		
 		boolean unsortiert=true;
 	    TrainData tdTemp;
 	    Timetable ttTemp;
 	    while (unsortiert){
 		    unsortiert = false;
 		    for (int i=0; i < traindataProjectList.size() - 1; i++){
 		    	int first = getValue(traindataProjectList.get(i).getPriority());
 		    	int second = getValue(traindataProjectList.get(i+1).getPriority());	
 		        if(first > second){
 			        tdTemp = traindataProjectList.get(i);
 			        traindataProjectList.set(i,traindataProjectList.get(i+1));
 			        traindataProjectList.set(i +1, tdTemp);
 			        
 			        ttTemp = timeTableProjectList.get(i);
 			        timeTableProjectList.set(i,timeTableProjectList.get(i+1));
 			        timeTableProjectList.set(i +1, ttTemp);
 			        
 			        unsortiert = true;
 			
 		        }
 		    }    
 	    }
 		
 	}
 
 	private static int getValue(String priority) {
 		
 		if(priority.equalsIgnoreCase("Sehr Wichtig")){
 		    return 1;
 		}else if (priority.equalsIgnoreCase("Wichtig")){
 			return 2;
 		}else if(priority.equalsIgnoreCase("Normal")){
 		    return 3;
 		}else{
 			
 		    return 4;
 	    }
 		
 	}
 
 	public static void stopAnimations(NodeMap map, Project p) {
 		
 		for(TrainData train : p.getTraindataProjectList()){
 
 			map.getMobileObjects().get(String.valueOf(train.getID())).stopAnimation();		
 		}
 		
 	}
 		
 
 	
 }
