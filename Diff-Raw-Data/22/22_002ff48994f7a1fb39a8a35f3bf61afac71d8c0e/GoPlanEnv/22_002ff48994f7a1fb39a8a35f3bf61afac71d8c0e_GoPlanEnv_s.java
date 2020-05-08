 import jadex.bdi.runtime.IGoal;
 import jadex.bdi.runtime.Plan;
 import jadex.bdi.testcases.goals.DropGoalPlan;
 import jadex.extension.envsupport.environment.ISpaceAction;
 import jadex.extension.envsupport.environment.ISpaceObject;
 import jadex.extension.envsupport.environment.space2d.Grid2D;
 import jadex.extension.envsupport.environment.space2d.Space2D;
 import jadex.extension.envsupport.math.IVector1;
 import jadex.extension.envsupport.math.IVector2;
 import jadex.extension.envsupport.math.Vector1Double;
 import jadex.extension.envsupport.math.Vector2Int;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import EDU.oswego.cs.dl.util.concurrent.TimeoutException;
 import algorithms.ShortestPath;
 import algorithms.ShortestPath.Node;
 
 /**
  *  Go to a specified position.
  */
 public class GoPlanEnv extends Plan
 {
 	/**
 	 *  The plan body.
 	 */
 
 	public void body()
 	{
 		Grid2D env = (Grid2D)getBeliefbase().getBelief("env").getFact();
 
 		//Get the position of the final destination and homebase, specified in the XML
 		ISpaceObject fd = env.getSpaceObjectsByType("finaldestination")[0];
 		ISpaceObject hm = env.getSpaceObjectsByType("homebase")[0];
 
 		IVector2 target = (IVector2)fd.getProperty("position");
 		IVector2 ini = (IVector2)hm.getProperty("position");
 
 		ISpaceObject[] acidentes = env.getSpaceObjectsByType("accident");
 		ISpaceObject[] locations = env.getSpaceObjectsByType("pointofinterest");
 
 		ISpaceObject myself = (ISpaceObject)getBeliefbase().getBelief("myself").getFact();
 
 		/*posicao inicial*/
 		if(getParameter("pos").getValue() != null)
 			myself.setProperty("position", getParameter("pos").getValue());
 		else
 			myself.setProperty("position",ini);
 
 
 		//Calculate de the new direction
 		String dir = null;
 		boolean acidente = false;
 		boolean filled = false;
 		//Tem de passar por todos os pontos antes de terminar
 		while(!target.equals(myself.getProperty(Space2D.PROPERTY_POSITION))){//&& !acidente){
 
 			HashMap <Integer,ISpaceObject> shorter = new HashMap <Integer,ISpaceObject>();
 
 			/*verificar prxima visita, que ser a mais prxima do ponto inicial*/
 			for(int x=0; x < locations.length; x++){
 				if(locations[x].getProperty("status").equals("notvisited")&& 
 						(locations[x].getProperty("weather").equals("") || 
 								locations[x].getProperty("weather").equals(env.getProperty("weather")))
 						){
 
 					IVector2 actual = (IVector2)locations[x].getProperty("position");
 
 					List<Node> nodes = GetPath((IVector2) myself.getProperty(Space2D.PROPERTY_POSITION),actual);
 
 					if(nodes != null)
 						shorter.put(nodes.size(),locations[x]);					
 				}
 			}
 
 			List ll = new LinkedList(); // Collections.sort() recebe como parametro um list  
 			ll.addAll(shorter.keySet());// buscando os valores no Map  
 			Collections.sort(ll);
 
 
 			ISpaceObject next_visit = null;
 
 
 			//verifica se anda tem pontos para visitar
 			if(!ll.isEmpty()){
 
 				ISpaceObject aux = (ISpaceObject)shorter.get(ll.get(0));
 				IVector2 posicao = (IVector2)aux.getProperty("position");
 
 
 				//Caminho entre o ponto de interesse a visitar
 				//e o destino
 				List<Node> nodes = GetPath(posicao, target);
 				int total = nodes.size()+(Integer)ll.get(0);
 				System.out.println("tempo: "+myself.getProperty("time"));
 				/*verificao do tempo*/
 				if((Integer)myself.getProperty("time") >= total){
 
 					next_visit = (ISpaceObject)shorter.get(ll.get(0));
 					myself.setProperty("time", (Integer)myself.getProperty("time")-total);
 
 				}
 				else{
 					next_visit = fd;
 					System.out.println("Fiquei sem tempo, tenho de ir para o destino final");
 
 				}
 			}
 			else 
 				next_visit = fd;
 
 
 			if(!next_visit.equals(fd)){ //AINDA TEM PONTOS PARA VISITAR
 				if(next_visit.getProperty("status").equals("notvisited")){
 
 					System.out.println("Indo para "+next_visit.getProperty("type"));
 
 					while(!next_visit.getProperty("position").equals(myself.getProperty(Space2D.PROPERTY_POSITION)) )
 					{
 
 
 						IVector2 mypos = (IVector2)myself.getProperty(Space2D.PROPERTY_POSITION);
 						IVector2 actualtarget = (IVector2)next_visit.getProperty("position");
 
 						List<Node> nodes = GetPath((IVector2)myself.getProperty(Space2D.PROPERTY_POSITION),
 								actualtarget);
 
 						Node next = nodes.get(2);
 
 						//Verifica se o prximo node tem algum acidente
 						//Isto simula viso
 
 						for(int x=1;x<acidentes.length;x++){
 							//S se o acidente no estiver evitado
 							if(acidentes[x].getProperty("state").equals("notavoid")){// && !acidente){ 
 								IVector2 aci_pos = (IVector2)acidentes[x].getProperty("position");
 								Node aci = new Node(aci_pos.getXAsInteger(),aci_pos.getYAsInteger());
 
 								if(GoPlanEnv.equal(aci,next))
 								{
 									myself.setProperty("accident", acidentes[x]);
 									IGoal check = createGoal("check");
 									myself.setProperty("tipo","percurso");
 									check.getParameter("target").setValue(acidentes[x]);
 
 									acidente=true;
 
 
 								}
 							}
 
 						}	
 
 						/*------------ aqui  o dummy para enganar a drop condition-----------*/
 
 						ISpaceObject dummy_accident = (ISpaceObject)acidentes[0];
 						if((Integer)myself.getProperty("gas")-5 == (Integer)myself.getProperty("reserva"))
 						{
 							//dummy_accident.setProperty("dummy", "yes");
 							if(dummy_accident.getProperty("state").equals("avoid")){
 								dummy_accident.setProperty("state", "notavoid");
 							}
 							IGoal fill = createGoal("fill");
 
 							myself.setProperty("accident", dummy_accident);
 
 							fill.getParameter("target").setValue(dummy_accident);
 							myself.setProperty("gas", (Integer)myself.getProperty("gas")-5);
 
 
 						}
 						else{
 							/*---------------------------------------------------------------------*/
 
 							myself.setProperty("gas", (Integer)myself.getProperty("gas")-5);
 
 
 							if(!acidente){
 
 								int md= 0;
 								if(mypos.getYAsInteger() == next.y){ //HORIZONTAL
 									if(mypos.getXAsInteger() < next.x)
 										md = 1; // DIREITA
 									else if(mypos.getXAsInteger() > next.x)
 										md = -1; //ESQUERDA
 								}
 								else
 									md = 0;
 
 
 								switch(md){
 								case 1:
 									dir = GoAction.RIGHT;
 									break;
 								case -1:
 									dir = GoAction.LEFT;
 									break;
 								default:
 									if(mypos.getXAsInteger() == next.x){
 										if(mypos.getYAsInteger() < next.y) 
 											md = 1; //BAIXO
 										else if(mypos.getYAsInteger() > next.y)
 											md = -1; //CIMA
 									}
 									switch(md){
 									case 1:
 										dir = GoAction.DOWN;
 										break;
 									case -1:
 										dir = GoAction.UP;
 									}
 								}
 
 
 
 								//Inform what is the new direction and execute space action
 								Map params = new HashMap();
 								params.put(GoAction.DIRECTION, dir);
 								params.put(ISpaceAction.OBJECT_ID, env.getAvatar(getComponentDescription()).getId());
 								SyncResultListener srl	= new SyncResultListener();
 								env.performSpaceAction("go", params, srl); 
 								srl.waitForResult();
 
 							}
 
 
 
 						}
 					}
 					
 					next_visit.setProperty("status", "visited");
 					System.out.println("Visitei: "+ next_visit.getProperty("type"));
 
 				}
 			}
 			else /*Vai para o final*/
 
 				if(next_visit.equals(fd)){
 
 					System.out.println("Indo para o destino final");
 					while(!target.equals(myself.getProperty(Space2D.PROPERTY_POSITION)))
 					{
 						myself.setProperty("time", 0);
 						IVector2 mypos = (IVector2)myself.getProperty(Space2D.PROPERTY_POSITION);
 
 						List<Node> nodes = GetPath((IVector2) myself.getProperty(Space2D.PROPERTY_POSITION),target);
 
 						Node next = nodes.get(2);
 
 						for(int x=1;x<acidentes.length;x++){
 							//S se o acidente no estiver evitado
 							if(acidentes[x].getProperty("state").equals("notavoid")){// && !acidente){ 
 
 								IVector2 aci_pos = (IVector2)acidentes[x].getProperty("position");
 								Node aci = new Node(aci_pos.getXAsInteger(),aci_pos.getYAsInteger());
 
 								if(equal(aci,next))
 								{
 									myself.setProperty("accident", acidentes[x]);
 									IGoal check = createGoal("check");
 									myself.setProperty("tipo","percurso");
 									check.getParameter("target").setValue(acidentes[x]);
 
 									acidente=true;
 
 								}
 							}
 						}	
 
 
 
 						if(!acidente){
 							int md= 0;
 							if(mypos.getYAsInteger() == next.y){ //HORIZONTAL
 								if(mypos.getXAsInteger() < next.x)
 									md = 1; // DIREITA
 								else if(mypos.getXAsInteger() > next.x)
 									md = -1; //ESQUERDA
 							}
 							else
 								md = 0;
 
 							switch(md){
 							case 1:
 								dir = GoAction.RIGHT;
 								break;
 							case -1:
 								dir = GoAction.LEFT;
 								break;
 							default:
 								if(mypos.getXAsInteger() == next.x){
 									if(mypos.getYAsInteger() < next.y) 
 										md = 1; //BAIXO
 									else if(mypos.getYAsInteger() > next.y)
 										md = -1; //CIMA
 								}
 								switch(md){
 								case 1:
 									dir = GoAction.DOWN;
 									break;
 								case -1:
 									dir = GoAction.UP;
 								}
 							}
 
 							//Inform what is the new direction and execute space action
 							Map params = new HashMap();
 							params.put(GoAction.DIRECTION, dir);
 							params.put(ISpaceAction.OBJECT_ID, env.getAvatar(getComponentDescription()).getId());
 							SyncResultListener srl	= new SyncResultListener();
 							env.performSpaceAction("go", params, srl); 
 							srl.waitForResult();
 						}
 					}

 				}
 		}
 
 		System.out.println("\nAcabou percurso!\n");
 		for(int x=0;x<locations.length;x++)
 			if(locations[x].getProperty("status").equals("visited"))
 				System.out.println("Visitei: "+ locations[x].getProperty("type"));
 			else
 				System.out.println("No Visitei: "+ locations[x].getProperty("type"));
 				
 	}
 
 
 	public static boolean equal(Node node, Node end) {
 		return (node.x == end.x) && (node.y == end.y);
 	}
 
 	public List<Node> GetPath(IVector2 start,IVector2 end){
 
 		ShortestPath sp = new ShortestPath(BDIMap.map);
 		List<Node> nodes = sp.compute(new ShortestPath.Node(start.getXAsInteger(),start.getYAsInteger()),
 				new ShortestPath.Node(end.getXAsInteger(),end.getYAsInteger())
 				);
 		return nodes;		
 	}
 
 
 }
