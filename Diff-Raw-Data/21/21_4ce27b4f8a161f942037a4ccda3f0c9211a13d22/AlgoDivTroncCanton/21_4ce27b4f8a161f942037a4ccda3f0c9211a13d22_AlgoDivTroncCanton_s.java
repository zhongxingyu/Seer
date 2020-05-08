 package railsimulator.tools;
 
 
 import java.util.List;
 import dao.CantonDAO;
 import dao.StationDAO;
 import beans.Station;
 
 
 public class AlgoDivTroncCanton {
 	
 	private CantonDAO canton_dao=new CantonDAO();
 	private StationDAO station_dao=new StationDAO();
 
 	
 	
 	public AlgoDivTroncCanton() {
         
 	}
 	
 	public void decoupage(List<Station> listeStation,int[] stationList) {
         
 
 		double distance=0;
         
		System.out.println("avant for stationlist");
		System.out.println("taille listeStation= " +listeStation.size());
 		for(int i=0;i<stationList.length;i++){
            System.out.println("dans stationlist et avant listestation");
 
         
 			for(int y=0;y<listeStation.size()-1;y++){
				System.out.println("ds listestation");
 				Station station1 = station_dao.getStationByID(listeStation.get(y).getIdStation());
 				Station station2 = station_dao.getStationByID(listeStation.get(y+1).getIdStation()); 
 				
 				//System.out.println("station1 " +station1.getIdStation() + "station2 " +station2.getIdStation());
 				double longS1= station1.getLongitude();
 				double latS1=station1.getLatitude();
 				
 				double longS2=station2.getLongitude();
 				double latS2=station2.getLatitude();
 				
				System.out.println("ancienne lat1=>" +latS1+ "lat2=>" +latS2);
 
 				
 				distance= getDistancePol(latS1,latS2,longS1 , longS2);
				System.out.println("distance==" +distance);
 
 				Double nbCantons= distance/200;
 				
 				int modulo=(int) (nbCantons%200);
 				System.out.println("modulo==>" +modulo);
 				if (modulo !=0){
					System.out.println("nbCantons.intValue()" +nbCantons.intValue());
 					System.out.println(nbCantons);
 					for (int j = 0; j < nbCantons.intValue(); j++) {
 						if (j==nbCantons.intValue()-1){
 						    int idcanton=canton_dao.createCantonParamStation(200+modulo,station1,station2);  
 						}
 						else
 						{
 						    int idcanton=canton_dao.createCantonParamStation(200,station1,station2);
						    System.out.println("idcanton ds else ==" +idcanton);
 						}
 						
 					} //fin boucle nbCantons
 					
 				}//finModulo
 				else{
 					for (int j = 0; j < nbCantons.intValue(); j++) {
 					
						    int idcanton=canton_dao.createCantonParamStation(200,station1,station2);
						    System.out.println("idcanton ds elsemodulo ==" +idcanton);					
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
