package tools;
 
 
 import java.util.List;
 
 import dao.CantonDAO;
 import dao.StationDAO;
 import beans.Station;
 
 
 public class AlgoDivTroncCanton {
 	
 	private CantonDAO canton_dao=new CantonDAO();
 	private StationDAO station_dao=new StationDAO();
 
 	//private static Logger logger = Logger.getLogger(AlgoDivTroncCanton.class);
 	
 	public AlgoDivTroncCanton() {
         
 	}
 	
 	public void decoupage(int[][] listeStation,int[] stationList) {
         
 		System.out.println("1");
 		double distance=0;
         
 		for(int i=0;i<stationList.length;i++){
 System.out.println("2");
         
 			for(int y=0;y<listeStation.length-1;y++){
 				
 				System.out.println("3");
 //                System.out.println("i : " + y+ "y : " + listeStation.get(y).getIdStation() + "  y+1 : "+ listeStation.get(y+1).getIdStation()); 
 //				Station station1 = station_dao.getStationByID(listeStation.get(y).getIdStation());
 //				Station station2 = station_dao.getStationByID(listeStation.get(y+1).getIdStation()); 
 				System.out.println("i : " + y+ "y : " + listeStation[y][0]+ "  y+1 : "+ listeStation[y][1]); 
 				Station station1 = station_dao.getStationByID(listeStation[y][0]);
 				Station station2 = station_dao.getStationByID(listeStation[y][1]); 
 				
 				System.out.println("4");
 				//System.out.println("station1 " +station1.getIdStation() + "station2 " +station2.getIdStation());
 				double longS1= station1.getLongitude();
 				double latS1=station1.getLatitude();
 				
 				double longS2=station2.getLongitude();
 				double latS2=station2.getLatitude();
 				
 
 				
 				distance= getDistancePol(latS1,latS2,longS1 , longS2);
 				
 
 				Double nbCantons= distance/200;
 				
 				int modulo=(int) (nbCantons%200);
 				System.out.println("modulo==>" +modulo);
 				if (modulo !=0){
 					
 					System.out.println(nbCantons);
 					for (int j = 0; j < nbCantons.intValue(); j++) {
 						if (j==nbCantons.intValue()-1){
 						    int idcanton=canton_dao.createCantonParamStation(200+modulo,station1,station2);  
 						}
 						else
 						{
 						    int idcanton=canton_dao.createCantonParamStation(200,station1,station2);
 						}
 						
 					} //fin boucle nbCantons
 					
 				}//finModulo
 				else{
 					for (int j = 0; j < nbCantons.intValue(); j++) {
 					
 						    int idcanton=canton_dao.createCantonParamStation(200,station1,station2);				
 					} //fin boucle nbCantons
 					
 				}//fin else
 
                 
 			}
 			
 			
 			
 		}
 		
 
         
 	}//fin decoupage
 	
 	
 	// === CALCUL LA DISTANCE ENTRE DEUX POINT VIA LES COORDONNEES POLAIRES === 
 	public double  getDistancePol(double latitudeA,double latitudeB,double longitudeA,double longitudeB) {
 		double distance = (6356.752*Math.acos(
                                               Math.sin(Math.toRadians(latitudeA))
                                               *Math.sin(Math.toRadians(latitudeB))
                                               +Math.cos(Math.toRadians(latitudeA))
                                               *Math.cos(Math.toRadians(latitudeB))
                                               *Math.cos(Math.toRadians(longitudeA)- Math.toRadians(longitudeB)))*1000
                            );
 		return(distance);
 	}
     
 }//fin classe AlgoDivTroncCanton
