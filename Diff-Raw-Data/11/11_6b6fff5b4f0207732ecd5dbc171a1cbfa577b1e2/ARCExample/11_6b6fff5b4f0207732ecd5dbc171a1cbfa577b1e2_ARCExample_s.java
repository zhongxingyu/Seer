 import java.util.Arrays;
 
 public class ARCExample {
 	public static void main(String[] args) throws Exception{
 
 		double Rc1 = 20.0;
 		double Rc2 = 60.0;
 		double Rs = 5.0;
 		double P = 5.0;
 		double C0 = 0.9;
 		double l = 100.0;
 		int bit = 32;
 		int turn = 1;
 		
 		
		//إSINK M ARC NODE
 		Sink sink = new Sink(Rc1,Rc2);
		RS nodes[] = new RS[1000];
 		
		//l
 		for(int i=0;i<nodes.length;i++){
 			nodes[i] = new RS(l*Math.random(),l*Math.random(),Rs,Rc1,Rc2,P,C0,i,l,l,nodes.length,bit);
 			nodes[i].self = nodes[i];
 			nodes[i].power.owner = nodes[i];
 		}
 
		//إ߾FY
 		sink.ALLNodes = nodes;
 		sink.searchNeighborNode();
 		for(int i=0;i<nodes.length;i++){
 			nodes[i].searchNeighborNode(nodes);
 		}
 		
 		//PHASE 1
 		sink.CHNSelection();
 		Arrays.sort(nodes);
 		for(int i=0;i<nodes.length;i++){
 			nodes[i].CHN();
 		}
 		for(int i=0;i<nodes.length;i++)
 			((RS)nodes[i]).JOIN();
 		
 		//PHASE 2
 		sink.routeSetup();
 		sink.HOP();
 		{
 			int i=0,CHNAmount=0;
 			for(int j=0;j<nodes.length;j++){
 				if(nodes[j].nodeState == ARCNode.CHN)
 					CHNAmount++;
 			}
 			while(true){
 				Arrays.sort(nodes);
 				for(;i<CHNAmount;i++){
 					if(nodes[i].deliverHOPTime == 100.0)
 						break;
 					nodes[i].HOP();
 				}
 				if(i == CHNAmount)
 					break;
 			}
 		}
 		
 		//PHASE 3
 		sink.NCHNActivation();
 		for(int i=0;i<nodes.length;i++){
 			nodes[i].Threshold();
 		}
 		
 		//PAHSE 4
 		sink.steadyPhase();
 		for(int i=0;i<nodes.length;i++){
 			if(nodes[i].nodeState == ARCNode.CHN || nodes[i].nodeState == ARCNode.ACTIVE){
 				for(int j=0;j<turn;j++){
 					nodes[i].reportData();
 				}
 			}
 		}
 
 	}
 }
