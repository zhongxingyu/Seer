 package data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import display.FragChangeColor04;
 
 public class WorldCuller {//should be portrayed as a game component
 	public WorldCuller(World world){
 		w=world;
 		activeY=(int)FragChangeColor04.posy/50;
 		activeX=(int)FragChangeColor04.posx/50;
 		activeZonesx=new ArrayList<Integer>();
 		activeZonesy=new ArrayList<Integer>();
 		activeZonesx.add(activeX);
 		activeZonesx.add(activeX+1);
 		if(!(activeX<=0)){
 			activeZonesx.add(activeX-1);
 		}
 		activeZonesy.add(activeY);
 		activeZonesy.add(activeY+1);
 		if(!(activeY<=0)){
 			activeZonesy.add(activeY-1);
 		}
 		FragChangeColor04.masterEntityList.clear();
 		FragChangeColor04.masterEntityList.addAll(w.getNineZones(-activeY, -activeX));
 		update();
 	}
 	 public void update(){
 		int Y=(int)FragChangeColor04.posy/50;
		int X=((int)((FragChangeColor04.posx/FragChangeColor04.aspect)/50));
 		if(Y==activeY&&X==activeX){
 			return;
 		}else{
 			FragChangeColor04.masterEntityList.clear();
 			FragChangeColor04.masterEntityList.addAll(w.getNineZones(-Y, -X));
 			activeX=X;
 			activeY=Y;
 		}
 	 }
 	 public void updateEntity(){
 			int Y=(int)FragChangeColor04.posy/50;
			int X=((int)((FragChangeColor04.posx/FragChangeColor04.aspect)/50));
 			FragChangeColor04.masterEntityList.clear();
 			FragChangeColor04.masterEntityList.addAll(w.getNineZones(-Y, -X));
 	 }
 	 private World w;
 	 private List<Integer> activeZonesx,activeZonesy;
 	 private int activeX,activeY;
 }
