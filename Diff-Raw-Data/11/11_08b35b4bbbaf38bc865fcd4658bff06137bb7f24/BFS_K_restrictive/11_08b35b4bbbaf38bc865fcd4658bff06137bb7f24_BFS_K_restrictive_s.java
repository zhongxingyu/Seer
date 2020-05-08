 /**
  * @author Avinash Malik (avimalik@ie.ibm.com)
  * @date 2011-11-24
  */
 package org.IBM.heuristics.lib;
 
 import net.sourceforge.gxl.*;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 import org.xml.sax.*;
 import org.IBM.*;
 import org.IBM.stateGraph.state;
 import org.IBM.stateGraph.stateEdge;
 import org.IBM.stateGraph.stateGraph;
 import org.IBM.heuristics.lib.*;
 
 public class BFS_K_restrictive {
     private static HashMap<String,state> joinNodeMap = new HashMap<String,state>();
     private static Queue<state> workList = new LinkedList<state>();
     private static Queue<Queue<state>> doneList = new LinkedList<Queue<state>>();
     private static Queue<Queue<state>> tempList = new LinkedList<Queue<state>>();
     
     //This holds all the current guard objects.
     private static HashMap<String,Guard> map = new HashMap<String,Guard>();
     private static GXLIDGenerator gen = null;
     
     private static state root = null;
     private static stateGraph SG = null;
     
     private static long sTime = 0;
     private static String fName = "";
     
     /**@field K determines the number of search paths that will be
      * used in the heuristic search
      
      K = 1, do a depth first search using the minimum costs, can be used
      to obtain a bound very fast.  
      
      K = 0, search the complete state space exhaustively.
      
      1 <= K <= N, search that many minimum cost paths only, throw the
      rest away. A *path* in this case corresponds to a macro node being
      thrown away.
      
      */
    private static int K = 2; //0 or less means search all paths else,
 			      //only search the paths specified
     
     public BFS_K_restrictive (File f, List<state> startingStates, long sTime) throws Exception{
 	//Make the root node in here
 	root = new state("rootNode");
 	fName = f.getName().replaceFirst("\\.xml","");
 	SG = new stateGraph("__stateGraph__"+fName,root);
 	SG.setEdgeMode("directed");
 	SG.add(root);
 	//The GXL document
 	GXLDocument doc = new GXLDocument();
 	doc.getDocumentElement().add(SG);
 	gen = new GXLIDGenerator(doc);
 	    
 	//Put the starting states onto the workList
 	for(state s : startingStates)
 	    workList.offer(s);
 	
 	this.sTime = sTime;
 	    
 	//Do the BFS
 	bfs();
 	    
 	    
 	//write the state graph onto disk for debugging
 	SG.getDocument().write(new File("./output","__state_graph"+fName));	
     }
     private static void setJoinNodeName(String name, Queue<state> q) throws Exception{
 	String sName = "";
 	for(state s : q){
 	    sName += s.getID();
 	}
 	
 	Queue<state> graphStates = null;
 	if((graphStates = SGHash.get(sName)) != null){
 	    for(state s : graphStates)
 		s.addJoinNode(name);
 	}
 	else {
 	    //DEBUG
 	    //flush the tree onto the drive
 	    //write the state graph onto disk for debugging
 	    // SG.getDocument().write(new File("./output","__state_graph"+fName));
 	    throw new RuntimeException("Cannot find states: "+sName+" in the graph");
 	}
     }
     
     private static state getNewJoinNode(state update, state leaf){
 
 	state seqJoinNode = new state(update.getID()+"_"+gen.generateNodeID());
 	//set it's parent to the leaf
 	seqJoinNode.addParent(leaf);
 	//set the alloc counter
 	seqJoinNode.incAllocCounter();
 	//set the guard names
 	for(String s : update.getGuards()){
 	    seqJoinNode.setGuardName(s);
 	}
 	return seqJoinNode;
     }
     
     private static boolean allParentInQ(state a, Queue<state> q){
 	int counter=0;
 	for(state p : a.getParents()){
 	    for(state s : q){
 		if(p == s){
 		    ++ counter; break;
 		}
 	    }
 	}
 	//XXX: Check if this is incorrect
 	// if(counter == a.getParents().size()) return true;
 	if(counter == a.getNumJoinParents()) return true;
 	else return false;
     }
     
     private static void colorPath(state sNode){
 	if(sNode.ifVisited()) return;
 	sNode.setVisited();
 	sNode.setAttr("color",new GXLString("green"));
 	sNode.setAttr("style",new GXLString("filled"));
     	for(int e=0;e<sNode.getConnectionCount();++e){
     	    if(sNode.getConnectionAt(e).getDirection().equals(GXL.OUT)){
     		GXLEdge le = (GXLEdge)sNode.getConnectionAt(e).getLocalConnection();
 		state node = (state)le.getSource();
 		colorPath(node);
     	    }
     	}
     }
     
     private static void bfs() throws Exception{
 	
 	while(true){
 	    while(!workList.isEmpty()){
 		
 		//DEBUG
 		;// System.out.println("----------------");
 		;//System.out.println("WORKLIST HAS");
 		for(state rt : workList){
 		    if(!rt.getDone())
 			;//System.out.println(rt.getID());
 		}
 		;//System.out.println("----------------");
 
 		
 		state s = workList.poll();
 
 		//If worklist is empty than break it
 		// XXX: check if this is correct!!
 		if(s.getDone()){
 		    s.setGuardValue(true);
 		    s.setDone(false);
 		    state t =null;
 		    while((t = workList.poll()) != null){
 			t.setGuardValue(true);
 			t.setDone(false);
 		    }
 		    workList.clear(); break;
 		}
 
 		//remove the guard and set its value to zero
 		//also set the name of the guard
 		//and put it in the map
 		for(String guard : s.getGuards()){
 		    Guard g = new Guard(guard);
 		    g.setGuardValue(false);
 		    map.put(guard,g);
 		}
 
 		//build all possible combinations
 		Queue<state> list = new LinkedList<state>();
 		for(state sw : workList){
 		    list.offer(sw);
 		}
 		//DEBUG
 		if(s.getPartners() != null){
 		    ;//System.out.println("Before combi: "+workList.size());
 		}
 		buildCombinations(s,list);
 		//DEBUG
 		if(s.getPartners() != null){
 		    ;//System.out.println("After combi: "+workList.size());
 		}
 		//empty the map
 		map.clear();
 	    }
 	    if(doneList.isEmpty()) {
 		//Tell what is the minimum makespan and then break
 		long fTime = System.currentTimeMillis()-sTime;
 		
 		//calculating the minimum makespan
 		state terminal = null;
 		float makespan = -1f;
 		Set<String> keys = SGHash.keySet();
 		Iterator<String> iter = keys.iterator();
 		while(iter.hasNext()){
 		    String str = iter.next();
 		    if(str.startsWith("dummyTerminalNode")){
 			LinkedList<state> dl = (LinkedList<state>)SGHash.get(str);
 			state dstate = dl.get(dl.size()-1);
 			if(terminal != null){
 			    if(new Float(((GXLString)dstate.getAttr("cost").getValue()).getValue()).floatValue() 
 			       < makespan){
 				terminal = dstate;
 				makespan = 
 				    new Float(((GXLString)dstate.getAttr("cost").getValue()).getValue()).floatValue();
 			    }
 			}
 			else{
 			    //For the very first time
 			    terminal = dstate;
 			    makespan = new Float(((GXLString)dstate.getAttr("cost").getValue()).getValue()).floatValue();
 			}
 		    }
 		}
 		
 		//color the path from the terminal node all the way to the rootNode
 		clearVisited(root);
 		colorPath(terminal);
 		clearVisited(root);
 		
 		System.out.println("Minimum optimal makespan: "+makespan);
 		System.out.println("Total time for BFS: "+fTime+" ms");
 		break;
 	    }
 	    else{
 		//push the update nodes onto the work list
 		//using the macro nodes
 		
 		//DEBUG
 		// ;//System.out.println("Calling doneList when not empty");
 
 		
 		Queue<state> q = doneList.poll();
 		state leaf = getLeaf(q); //get the leaf from the
 		//hashmap, which represents the
 		//SG graph
 		HashMap<String,state> halfScheduled = new HashMap<String,state>();
 		Queue<state> tempQ = (Queue<state>)((LinkedList<state>)q).clone();
 		//DEBUG
 		;//System.out.println("*********");
 		;//System.out.println("Macro node is: ");
 		for(state tutu : q){
 		    ;//System.out.println(tutu.getID());
 		}
 		;//System.out.println("*********");
 		QM: while(!q.isEmpty()){
 		    state curr = q.poll();
 		    for(state update : curr.getUpdateStates()){
 			//See if it's a join node
 			if(update.getIsJoinNode()){
 			    //yes it is a join node
 			    //Check if this join node can be processed??
 			    
 			    //DEBUG
 			    ;//System.out.println("Trying to update the join node: "+update.getID());
 			    
 			    //Check if this state has a partner
 			    ArrayList<state> partner = new ArrayList<state>();
 			    if(update.getPartners() == null){
 				//This is the sequential (easy) case
 				
 				//Do i have a join node allocated??
 				// just look at the leaf, because that
 				// should be enough.
 				if(leaf.getJoinNodes().isEmpty()){
 				    //No I don't have any join-nodes
 				    //allocated allocate a new Join node
 				    //with the a new name
 				    
 				    state seqJoinNode = getNewJoinNode(update,leaf);
 				    //DEBUG
 				    ;//System.out.println("Adding new join node to the partners named: "
 						       // +seqJoinNode.getID()+"-->"+curr.getID());
 
 				    //add it to the map
 				    joinNodeMap.put(seqJoinNode.getID(),seqJoinNode);
 				    //now set all the node in the macro
 				    //node of the graph with the
 				    //seqJoinNode.getID() name
 				    setJoinNodeName(seqJoinNode.getID(),tempQ);
 				}
 				else{
 				    //This means I got some joinNode
 				    //names in me
 				    
 				    boolean found = false;
 				    for(String s : leaf.getJoinNodes()){
 					if(s.split("_")[0].equals(update.getID())){
 					    state jNode = joinNodeMap.get(s);
 					    //increment its allocation number
 					    jNode.incAllocCounter();
 					    //set its parent as the leaf
 					    jNode.setParent(0,leaf);
 					    //Check if this node can be allocated
 					    //DEBUG
 					    ;//System.out.println("I "+curr.getID()+" have a join node already named "+
 							       // jNode.getID());
 
 					    if(jNode.getAllocCounter() == update.getNumJoinParents()){
 						//yes it can be allocated
 						//DEBUG
 						;//System.out.println("I "+curr.getID()+" am adding my join node"+
 								   // update.getID()+" to the worklist");
 						
 						update.clearParents(); //XXX
 						update.addParent(leaf);
 						workList.add(update);
 					    }
 					    found = true;
 					    break;
 
 					}
 				    }
 				    if(!found){
 					//Check if this is not a wrong
 					//allocation completely
 					
 					//get the guards from update
 					ArrayList<String> updateGuards = update.getGuards();
 					    
 					boolean getOut = false;
 					
 					//check with all the joinNode
 					//names allocated to me
 					M: for(String js : leaf.getJoinNodes()){
 					    state jN = joinNodeMap.get(js);
 					    for(String gs : jN.getGuards()){
 						String t1 = gs.split("_")[0];
 						//DEBUG
 						// ;//System.out.println("Guard is: "+t1+": "+jN.getID());
 						for(String ugs : updateGuards){
 						    String t2 = ugs.split("_")[0];
 						    if(t1.equals(t2)){
 							getOut = true;
 							break M;
 						    }
 						}
 					    }
 					}
 					if(getOut){
 					    //This means that this
 					    //scheudle and
 					    //allocation is just
 					    //simply incorrect, get
 					    //out of this hell hole
 					    //and look at the next
 					    //macro-node in the
 					    //doneList.
 						
 					    //empty the worklist
 					    workList.clear();
 						
 					    //remove all from q
 					    q.clear();
 					    
 					    halfScheduled.clear();
 					    
 					    break QM;
 						
 					}
 					else{
 					    //Make a new node and do
 					    //the normal stuff
 					    state seqJoinNode = getNewJoinNode(update,leaf);
 					    //add it to the map
 					    joinNodeMap.put(seqJoinNode.getID(),seqJoinNode);
 					    //now set all the node in the macro
 					    //node of the graph with the
 					    //seqJoinNode.getID() name
 					    setJoinNodeName(seqJoinNode.getID(),tempQ);
 					}
 				    }
 				}
 			    }
 			    else if(update.getPartners() != null){
 				
 				partner.addAll(update.getPartners());
 				//This is the case where this state
 				//transition has a parallel node
 				//check if the partner is also in the
 				//updates of any of the curr states
 				
 				//Check all my partners will be updated!!
 				int counter=0;
 				for(state p : partner){
 				    for(state cs : tempQ){
 					for(state up : cs.getUpdateStates()){
 					    if(up == p) {
 						//TODO: Check that is p is a
 						//join node then it
 						//actually can be
 						//scheduled.
 						//else break out 
 
 						++counter; 
 						//Add any more partners
 						//if present, make sure
 						//you don't add update
 						//again to the list.
 						List<state> addPs = new ArrayList<state>();
 						addPs.addAll(p.getPartners());
 						for(int e=0;e<addPs.size();++e){
 						    if(addPs.get(e) == update){
 							addPs.remove(e); break;
 						    }
 						}
 						for(state q1 : partner){
 						    for(int e=0;e<addPs.size();++e){
 							if(addPs.get(e) == q1){
 							    addPs.remove(e);
 							    --e;
 							}
 						    }
 						}
 						partner.addAll(addPs);
 						break;
 					    }
 					}
 				    }
 				}
 				if(counter == partner.size()){
 				    
 				    //This means that all my partners
 				    //might be updated!!
 				    
 				    //I haven't yet checked that if my
 				    //partner is a join node then will
 				    //it still be updated, because it
 				    //might not be updated at all!!
 				    
 				    //check if I am in the halfScheduled
 				    //list
 				    
 				    //If the half scheduled list does
 				    //not contain me, then check that
 				    //all my parents are allocating me
 				    //on the same processor together
 				    //right now!!
 				    
 				    //This can be determined by: 1.)
 				    //Making sure that all my parents
 				    //are actually in the q
 				    if(!halfScheduled.containsKey(update.getID())){
 					//Put myself on the
 					//halfScheduled list if all my
 					//parents are in the q
 					if(allParentInQ(update,q)){
 					    //set my parent after
 					    //building a new node
 					    state jNode = new state(update.getID());
 					    jNode.setParent(0,leaf);
 					    jNode.setIsJoinNode(true);
 					    for(state psp : update.getPartners())
 						jNode.setPartnerState(psp);
 					    halfScheduled.put(update.getID(),jNode);
 					    //I haven't yet put it on
 					    //the workList. This will
 					    //happen at the end
 					}
 					else{
 					    //Only if the leaf node
 					    //already is allocating me
 					    //on this exact same
 					    //processor then I can go
 					    //onto the halfScheduled
 					    //list
 					    
 					    if(!leaf.getJoinNodes().isEmpty()){
 						boolean found = false;
 						for(String s : leaf.getJoinNodes()){
 						    if(s.split("_")[0].equals(update.getID())){
 							state jNode = joinNodeMap.get(s);
 							//increment its allocation number
 							jNode.incAllocCounter();
 							//set its parent as the leaf
 							jNode.setParent(0,leaf);
 							//Check if this node can be allocated
 							if(jNode.getAllocCounter() == update.getNumJoinParents()){
 							    //yes it can be allocated
 							    // jNode = new state(update.getID());
 							    // jNode.setParent(0,leaf);
 							    // jNode.setIsJoinNode(true);
 							    // for(state psp : update.getPartners())
 							    // 	jNode.setPartnerState(psp);
 							    halfScheduled.put(update.getID(),update);
 							}
 							found = true;
 							break;
 
 						    }
 						}
 						if(!found){
 						    //Check if this is not a wrong
 						    //allocation completely
 					
 						    //get the guards from update
 						    ArrayList<String> updateGuards = update.getGuards();
 					    
 						    boolean getOut = false;
 					
 						    //check with all the joinNode
 						    //names allocated to me
 						    M: for(String js : leaf.getJoinNodes()){
 							state jN = joinNodeMap.get(js);
 							for(String gs : jN.getGuards()){
 							    String t1 = gs.split("_")[0];
 							    //DEBUG
 							    // ;//System.out.println("Guard is: "+t1+": "+jN.getID());
 							    for(String ugs : updateGuards){
 								String t2 = ugs.split("_")[0];
 								if(t1.equals(t2)){
 								    getOut = true;
 								    break M;
 								}
 							    }
 							}
 						    }
 						    if(getOut){
 							//This means that this
 							//scheudle and
 							//allocation is just
 							//simply incorrect, get
 							//out of this hell hole
 							//and look at the next
 							//macro-node in the
 							//doneList.
 						
 							//empty the worklist
 							workList.clear();
 						
 							//remove all from q
 							q.clear();
 							
 							halfScheduled.clear();
 					    
 							break QM;
 						
 						    }
 						}
 					    }
 					}
 				    }
 				}
 			    }
 			}//this should always be in this else
 			else{
 			    boolean add = false;
 			    //Check if this state has a partner
 			    ArrayList<state> partner = new ArrayList<state>();
 			    int counter = 0;
 			    boolean partnerIsJoin = false;
 			    if(update.getPartners() != null){
 				partner.addAll(update.getPartners());
 				//check if the partner is also in the
 				//updates of any of the curr states
 				counter=0;
 				//DEBUG
 				for(int yu=0;yu<partner.size();++yu){
 				    state p = partner.get(yu);
 				    //DEBUG
 				    // ;//System.out.println("!!! yu is !!!! "+yu);
 				    //q does not have the curr state!!
 				    YU: for(state cs : tempQ){
 					for(state up : cs.getUpdateStates()){
 					    if(up == p){
 						//TODO: Check that is p is a
 						//join node then it
 						//actually can be
 						//scheduled.
 						//else break out 
 						
 						//DEBUG
 						;//System.out.println("Yipee!!, my partner "+p.getID()
 								   // +" is gonna be updated at same time\n as me "
 								   // +update.getID()+" partner size: "+partner.size());
 
 						if(isJoinNode(p))
 						    partnerIsJoin = true;
 
 						++counter;
 						//Add any more partners
 						//if present, make sure
 						//you don't add update
 						//again to the list.
 						List<state> addPs = new ArrayList<state>();
 						addPs.addAll(p.getPartners());
 						for(int e=0;e<addPs.size();++e){
 						    if(addPs.get(e) == update){
 							addPs.remove(e); break;
 						    }
 						}
 						//This loop also removes
 						//the multiple join
 						//nodes
 						for(state q1 : partner){
 						    for(int e=0;e<addPs.size();++e){
 							if(addPs.get(e) == q1){
 							    addPs.remove(e);
 							    --e;
 							}
 						    }
 						}
 						//DEBUG
 						// if(!addPs.isEmpty())
 						    ;//System.out.println("Adding to the partner list");
 						// for(state ti : addPs)
 						    ;//System.out.println(ti.getID());
 						partner.addAll(addPs);
 						break YU;
 					    }
 					}
 				    }
 				}
 				if(counter == partner.size()){ 
 				    //DEBUG
 				    ;//System.out.println("Adding node: "+update.getID()+" to worklist");
 
 				    add = true;
 				}
 			    }
 			    else add = true; //this is not a rendezvous state
 			    if(add && !partnerIsJoin){
 				//reset the parent to point to the leaf
 				//instead of the real-parent
 				//shouldn't this be clearing parents as well
 				update.clearParents(); //XXX
 				update.addParent(leaf);
 				workList.add(update);
 			    }
 			    else if (add && partnerIsJoin){
 				update.clearParents(); //XXX
 				update.addParent(leaf);
 				halfScheduled.put(update.getID(),update);
 			    }
 			}
 		    }
 		}
 		//Half done par list needs to be done here this list has
 		//half scheduled nodes.  Remove all the join_nodes and
 		//check for all other par nodes that there join-nodes
 		//are already in the work_list
 		
 		Iterator iter = halfScheduled.keySet().iterator();
 		while(!halfScheduled.isEmpty()){
 		    state hsp  = halfScheduled.remove(iter.next());
 		    for(state ps : hsp.getPartners()){
 			int counter = 0;
 			Iterator iter2 = halfScheduled.keySet().iterator();
 			while(iter2.hasNext()){
 			    state tutu = halfScheduled.get(iter2.next());
 			    //there should be all ps's
 			    if(ps.getID().equals(tutu.getID())){
 				++counter; break;
 			    }
 			}
 			if(counter == hsp.getPartners().size())
 			    workList.add(hsp);
 		    }//XXX: this might give a null
 		    //XXX: Optimization that might be applicable, if we
 		    //add one then we add all the partners as well.
 		}
 		
 		//The half done list should be emptied!!
 	    }
 	}
     }
 
     private static void buildCombinations(state s,Queue<state> workList)throws Exception{
 	//Put it in the temp list
 	Queue<state> list = new LinkedList<state>();
 	
 	//The rendezvous nodes always have to go in order....
 	//from sender->sender-receiver->.......->receiver
 	//XXX: IMP
 	if(s.getPartners() != null){
 	    //If this is a rendezvous state
 	    //get all partner rendezvous states
 	    
 	    //One might not be able to get the partner from the partner
 	    //list if it is a join node that has already been scheduled
 	    //in the state graph (special case, since we are doing an
 	    //optimization --> building a DAG instead of a tree)
 	    
 	    ArrayList<state> list1 = new ArrayList<state>(s.getPartners().size());
 	    for(state t : s.getPartners())
 		list1.add(t);
 	    Queue<state> rendezvousList = new LinkedList<state>();
 	    //DEBUG
 	    ;//System.out.println("Adding the node "+s.getID()+" to rendezvousList"+" list1 size: "+list1.size());
 
 	    rendezvousList.offer(s);
 	    while(!list1.isEmpty()){
 		state partner = list1.remove(0); 
 		boolean add = true;
 		for(state rq : rendezvousList){
 		    if(partner == rq){
 			add = false; break;
 		    }
 		}
 		if(add){
 		    if(!((LinkedList<state>)BFS_K_restrictive.workList).remove(partner))
 			throw new RuntimeException("Partner not found in the list "+partner.getID());
 		    //DEBUG
 		    ;//System.out.println("Found my partner in the worklist called: "+s.getID()+"->\n"
 				       // +partner.getID()+" partner's partner size: "+partner.getPartners().size());
 		    rendezvousList.offer(partner);
 		    list1.addAll(((LinkedList<state>)rendezvousList).get(rendezvousList.size()-1).getPartners());
 		}
 	    }
 	    
 	    //Now order these things from sender to receiver. Only the
 	    //sender and receiver should be first and last, all others
 	    //can be in any order (because of the property of max-plus
 	    //algebra proven in the DAC'2012 paper)
 	    state sender = null;
 	    state receiver = null;
 	    //DEBUG
 	    ;//System.out.println("Size of rendezvous list: "+rendezvousList.size());
 
 	    for(int i=0;i<rendezvousList.size();++i){
 		state st = ((LinkedList<state>)rendezvousList).get(i);
 		if(st.getTypes().size()==1){
 		    if(st.getTypes().get(0).equals("sender")){
 			sender = st;
 			//DEBUG
 			;//System.out.println("Sender id: "+sender.getID());
 
 			rendezvousList.remove(sender);
 			--i;
 		    }
 		    else if(st.getTypes().get(0).equals("receiver")){
 			receiver = st;
 			//DEBUG
 			;//System.out.println("Recevier id: "+receiver.getID());
 			rendezvousList.remove(receiver);
 			--i;
 		    }
 		}
 		else if(st.getTypes().size() == 2){
 		    //DEBUG
 		    ;//System.out.println("sender-receiver type: "+st.getParents().size());
 		}
 		else throw new RuntimeException("Wrong type for node "+st.getID());
 	    }
 	    //DEBUG
 	    ;//System.out.println("Rendezvous list size: "+rendezvousList.size());
 
 	    if(sender != null)
 		((LinkedList<state>)rendezvousList).add(0,sender);
 	    else throw new RuntimeException("Did not find a sender");
 	    if(receiver != null)
 		((LinkedList<state>)rendezvousList).add(rendezvousList.size(),receiver);
 	    else throw new RuntimeException("Did not find a receiver");
 	    
 	    //Now just put these in a macro node
 	    tempList.offer(rendezvousList);
 	    
 	    //DEBUG
 	    // for(state st : rendezvousList)
 	    // 	;//System.out.println(st.getID());
 
 	    
 	    // for(state st : rendezvousList)
 	    // 	//Set the done value
 	    // 	st.setDone(true);
 	
 	    //Attach the macro node to the root Node
 	    attachToRoot(sender);//the first node will always be the sender
 	    
 	    //This does not need to go to the back of the workList,
 	    //because it cannot combine with any other partners but
 	    //those in the rendezvousList
 	    
 	    //we are also not resetting the setDone variable, which
 	    //means this will not be called ever again.
 	}
 	else{
 	    Queue<state> macroNode = new LinkedList<state>();
 	    tempList.offer(macroNode); //added to the tempList
 	    macroNode.offer(s);
 	    while(!workList.isEmpty()){
 		state ps = workList.poll(); //removed
 		//Taking care of rendzevous
 		if(ps.getPartners()!=null) continue;
 		//get the guards for ps
 		for(String g : ps.getGuards()){
 		
 		    //Check if the guard is in the map if it is then what is
 		    //it's value.  if the value is false then that means I
 		    //cannot run this thing with the current node
 		    if(!map.containsKey(g)){
 			//This is the best
 			//possible case, but requires a lot of work
 		    
 			/**
 			   TODO: (DONE)
 			   1.) How to take care of multiple accesses to join nodes
 			*/
 			for(String sg : ps.getGuards()){
 			    Guard gn = new Guard(sg);
 			    gn.setGuardValue(false);
 			    map.put(sg,gn);
 			}
 			
 			//add to the macroNode
 			macroNode.offer(ps);
 			break;
 		    }
 		    else if(map.containsKey(g) && map.get(g).getGuardValue() == false){
 			//This means it has the same guard as me, so lets
 			//just throw it out.
 		    
 			//Only put it in the list if the s.getGuards and
 			//ps.getGuards are not exactly the same.
 			boolean add = true;
 			for(String h : ps.getGuards()){
 			    for(String k : s.getGuards()){
 				if(h.equals(k)){
 				    add = false; break;
 				}
 			    }
 			    if(!add) break;
 			}
 			if(add)
 			    list.offer(ps);
 			break;
 		    }
 		    else if(map.containsKey(g) && map.get(g).getGuardValue() == true)
 			throw new RuntimeException("The guard "+g+" for state" +s.getID()
 						   +" is in map, but its value is true");
 		}
 	    }
 	    //DEBUG
 	    // for(state rt : macroNode){
 	    // 	if(rt.getParents().size() != 0)
 	    // 	    ;//System.out.println("before building others: "+rt.getID()+" with parent: "
 	    // 			       // +rt.getParents().get(0).getID());
 	    // }
 	    while(!list.isEmpty()){
 		//Build all possible combinations using the list
 		state ls = list.poll();
 		//DEBUG
 		;//System.out.println(ls.getID()+" guard size: "+ls.getGuards().size());
 		ArrayList<Queue<state>> toAdd = new ArrayList<Queue<state>>();
 		for(Queue<state> mns : tempList){
 		    Queue<state> macroNodeN = new LinkedList<state>();
 		    boolean replace = false;
 		    for(state ms : mns){
 			if(!replace){
 			    MU: for(String gls : ls.getGuards()){
 				for(String gms : ms.getGuards()){
 				    //DEBUG
 				    ;//System.out.println(gls+" "+gms);
 				    if(gls.equals(gms)){
 					replace = true;
 					break MU;
 				    }
 				}
 			    }
 			    if(replace){
 				;//System.out.println("Replaced "+ms.getID()+" with "+ls.getID());
 				macroNodeN.offer(ls);
 			    }
 			    else macroNodeN.offer(ms);
 			}
 			else macroNodeN.offer(ms);
 		    }
 		    toAdd.add(macroNodeN);
 		}
 		//DEBUG
 		for(Queue<state> rt : toAdd){
 		    ;//System.out.println("others built are: ");
 		    for(state rtt : rt){
 			if(rtt.getParents().size() != 0)
 			    ;//System.out.println(rtt.getID()+"-->"+rtt.getParents().get(0).getID());
 		    }
 		    tempList.offer(rt);
 		}
 		// tempList.offer(macroNodeN);
 	    }
 	    //Set the done value
 	    s.setDone(true);
 	
 	    //Attach the macro node to the root Node
 	    attachToRoot(s);
 	
 	    BFS_K_restrictive.workList.offer(s); //put it at the back of the main list
 	}
     }
     
     private static HashMap<String,Queue<state>> SGHash = new HashMap<String,Queue<state>>();
     private static HashMap<String,Queue<state>> doneListMap = new HashMap<String,Queue<state>>();
 
     private static boolean updateDoneList(String nMNS){
 	boolean ret = false;
 	//get the macroNode in the done list with this macronodes names
 	if(doneListMap.containsKey(nMNS)){
 	    Queue<state> q = doneListMap.remove(nMNS);
 	    if(!doneList.remove(q)){
 		throw new RuntimeException("doneListMap has node: "+nMNS+", but not the doneList");
 	    }
 	    else ret = true;
 	}
 	else throw new RuntimeException("Tree hash map has node: "+nMNS+", but not the doneListMap");
 	return ret;
     }
     
     private static state getLeaf(Queue<state> q){
 	String name = "";
 	//DEBUG
 	// ;//System.out.println(q.size());
 
 	for(state s : q)
 	    name += s.getID();
 	//XXX: the cast from Queue to linked list might give an error
 	LinkedList<state> l = (LinkedList<state>)SGHash.get(name);
 	if(l == null)
 	    throw new RuntimeException("The map does not contain the key: "+name);
 	return l.get(l.size()-1);
     }
     
     private static ArrayList<String> makeNameCombis(ArrayList<String> names){
 	ArrayList<String> ret = new ArrayList<String>();
 	for(String g : names){
 	    String temp = g;
 	    for(String h : names){
 		if(!g.equals(h)){
 		    temp+=h;
 		}
 	    }
 	    ret.add(temp);
 	}
 	return ret;
     }
 
     private static boolean updateLists(Queue<state> nMN, ArrayList<String> names){
 	boolean ret = true;
 	
 	ArrayList<String> allCombis = makeNameCombis(names);
 	
 	for(String nMNS : allCombis){
 	    //DEBUG
 	    ;//System.out.println("Trying to search: "+nMNS);
 	    if(SGHash.containsKey(nMNS)){
 		//DEBUG
 		;//System.out.println("Found the node: "+nMNS);
 
 		//Then check what is the cost of the node
 		Queue<state> SGnMN = SGHash.get(nMNS);
 		float leafCost = ((LinkedList<state>)SGnMN).get(SGnMN.size()-1).getCurrentCost();
 		float cost = ((LinkedList<state>)nMN).get(nMN.size()-1).getCurrentCost();
 		if(leafCost <= cost){
 		    //DEBUG
 		    ;//System.out.println("Removing myself from the map");
 
 		    ret= false; //don't add this node to the SGHash
 		    break;
 		}
 		else{
 		    //DEBUG
 		    ;//System.out.println("Removing the other node from the map");
 
 		    //We need to remove this node from the HashMap
 		    SGHash.remove(nMNS); //removed
 		    //we also need to remove the already present node from
 		    //the doneList, which has the same states macronode
 		    if(!updateDoneList(nMNS))
 			throw new RuntimeException("Node "+nMNS+" is in the SGHashMap, but not on the doneList");
 		}
 	    
 	    }
 	}
 	
 	return ret;
     }
     
     private static void attachToRoot(state s) throws Exception{
 	if(s.getID().startsWith("dummyStartNode")){
 	    //attach it to the root node
 	    s.addParent(root);
 	}
 
 	while(!tempList.isEmpty()){
 	    Queue<state> q = tempList.poll();
 	    int counter = 0;
 	    Queue<state> nMN = new LinkedList<state>();
 	    Queue<state> nMMN = new LinkedList<state>();
 	    String nMNS = "";
 	    String nMNGS = "";
 	    ArrayList<state> parents = new ArrayList<state>();
 	    ArrayList<String> nAMNS = new ArrayList<String>();
 
 	    //DEBUG
 	    for(state lq : q)
 		;//System.out.println("?????"+lq.getID()+"????? with parent: "+lq.getParents().get(0).getID());
 
 
 	    for(state sm : q){
 		if(counter == 0){
 		    if(!sm.getID().equals(s.getID())) 
 			throw new RuntimeException("Something went wrong in building the macro action nodes "
 						   +s.getID()+"!="+sm.getID());
 		    
 		    parents.addAll(sm.getParents()); //This should be correct!!
 		    //DEBUG
 		    // ;//System.out.println(sm.getParents().size()+","+sm.getParents().get(0).getID());
 
 		    // if(parents.size() != sm.getParents().size())
 		    // 	throw new RuntimeException("I cannot find all my parents and yet I have been scheduled "+
 		    // 				   sm.getID());
 		}
 		//Build a new state with the same name as sm
 		
 		state snew = null;
 		if(parents.size() > 1 || parents.isEmpty())
 		    //XXX This (throw) will be removed later (once the
 		    //join node algorithm is completely decided) -->
 		    //it's been decided, this remains the same, because
 		    //it's a tree not a DAG
 		    throw new RuntimeException("More than one parent :-( "+sm.getID() +
 					       "or no parent at all!!");
 		for(state parent : parents){
 
 		    //XXX
 		    if(counter == 0)
 			nMMN.offer(parent);
 
 		    //We need the generator, else GXL will complain
 		    //about having same named nodes
 		    snew = new state(sm.getID()+"_"+gen.generateNodeID());
 		    //DEBUG
 		    // ;//System.out.println("attachRoot ID: "+sm.getID());
 
 		    nMNS += sm.getID(); //we need this as well, because
 					//we use q.getID() (in getleaf()
 					//method to look into the
 					//SGHashMap)
 		    nAMNS.add(sm.getID());
 		    
 		    //Add the guards to nMNGS
 		    nMNGS += getGuards(sm);
 
 		    //Add to the Queue nMN
 		    nMN.offer(snew);
 		    nMMN.offer(snew);
 		    
 		    //XXX: Add this to the tree only if you have to
 		    // SG.add(snew);
 		    // stateEdge edge = new stateEdge(parent,snew);
 		    // SG.add(edge);
 
 		    //This added node might be dangling in the graph,
 		    //because although it is added, it does not
 		    //necessarily mean that we will proceed further with
 		    //this path --> good for debugging
 		    
 		    //set the costs
 		    //First copy the cost from sm to snew
 		    for(float f : sm.getCost())
 			snew.setCost(f);
 		    for(String type : sm.getTypes())
 			snew.setType(type);
 		    // adding join nodes strings from parents as long as
 		    // this itself is not a join node
 		    for(String js : parent.getJoinNodes() )
 			if(!isJoinNode(sm)) snew.addJoinNode(js);
 		    //Check if this is a normal (non-rendezvous node)
 		    if(sm.getPartners()==null)
 			snew.updateCurrentCost(parent.getCurrentCost());
 
 		    //See what these need to be. XXX: IMP
 		    else if(sm.getPartners()!=null){
 			for(String type : sm.getTypes()){
 			    if(sm.getTypes().size()==1 && type.equals("sender"))
 				snew.updateSenderCost(parent.getCurrentCost());
 			    else if(sm.getTypes().size()==2 && type.equals("receiver")){
 				//set up the parent
 				snew.addParent(parent);
 				snew.updateSenderReceiverCost(parent.getCurrentCost());
 			    }
 			    else if(sm.getTypes().size() == 1 && type.equals("receiver")){
 				snew.addParent(parent);
 				snew.updateReceiverCost(parent.getCurrentCost());
 			    }
 			}
 		    }
 		    snew.setAttr("cost",new GXLString(""+snew.getCurrentCost()));
 		    
 		    //set the update guards high
 		}
 		
 		parents.clear();
 		parents.add(snew);
 		
 		++counter;
 
 	    }
 	    //Add to the SGHash
 	    
 	    //Before checking for the equivalent states also check for
 	    //the K value and path pruning using the K value
 
 	    //Add to the doneList, provided this one is not already
 	    //there with a lesser cost in the HashMap
 	    if(updateLists(nMN,nAMNS)){
 		if(updateKLists(((LinkedList<state>)nMN),nAMNS,nMNGS)){
 		    SGHash.put(nMNS,nMN);
 		    //We need to remove the already present macroNode
 		    //from doneList with the same nodes as q's
 		    //macroNodes.
 		    //DEBUG
 		    // ;//System.out.println("in attachroot adding to doneList q");
 
 		    //XXX: Add this to the tree only if you have to.
 		    //Needed to reduce the overall computation time
 		    state parent = nMMN.poll();
 		    while(!nMMN.isEmpty()){
 			state snew = nMMN.poll();
 			SG.add(snew);
 			stateEdge edge = new stateEdge(parent,snew);
 			SG.add(edge);
 			parent = snew;
 		    }
 
 		    doneList.offer(q);
 		    
 		    //also add it to the doneListMap
 		    doneListMap.put(nMNS,q);
 		}
 	    }
 	}
     }
     
     private static String getGuards(state node){
 	
 	String ret = "";
 	for(String g : node.getGuards())
 	    ret += g.split("_")[0];
 
 	return ret;
     }
     
     private static HashMap<String,List<Queue<state>>> guardMap = new HashMap<String,List<Queue<state>>>();
     
     /**
      TODO:
      1.) Do the join nodes later on (check, if this will work)
      */
     private static boolean updateKLists(LinkedList<state> nMN, ArrayList<String> names,
 					String nMNGS){
 	
 	if(K == 0)
 	    return true;
 	else{
 	    List<Queue<state>> ll  = null;
 	    if((ll = guardMap.get(nMNGS)) != null){
 		//Yes it does contain the key
 		
 		//how many need to be removed??
 		int toRemove = (1+ll.size())-K;
 		// //DEBUG
 		// System.out.println("It contains the key: "+nMNGS+" to remove: "+toRemove);
 		
 		//toRemove can only be 1 or 0
 		if(toRemove > 0){
 		    //find out, which one needs to be removed
 		    //the last one actually
 		    LinkedList<state> toR = (LinkedList<state>)ll.get(ll.size()-1);
 		    if(toR.get(toR.size()-1).getCurrentCost() > nMN.get(nMN.size()-1).getCurrentCost()){
 			toR = (LinkedList<state>)ll.remove(ll.size()-1); //removed the last one
 			//add the new one into the list
 			ll.add(ll.size(),nMN);
 			
 			//remove toR from the  SGHash as well
 			//Make the names for the removal
 			String tnMN = "";
 			for(state tor : toR)
 			    tnMN += tor.getID();
 
			if(!SGHash.containsValue(toR))
			    throw new RuntimeException("node: "+tnMN+" present in the guardMap, but not in the tree");
 			
 			ArrayList<String> toRem = new ArrayList<String>();
 			
 			//XXX: Expensive operation O(n) time
 			Set<Map.Entry<String,Queue<state>>> mes = SGHash.entrySet();
 			Iterator iter = mes.iterator();
 			while(iter.hasNext()){
 			    Map.Entry<String,Queue<state>> me = (Map.Entry<String,Queue<state>>)iter.next();
 			    if(me.getValue().equals(toR))
 				toRem.add(me.getKey());
 				// SGHash.remove(me.getKey());
 			}
 			
 			for(String g : toRem){
 			    SGHash.remove(g);
 			    //Have to also remove all these nodes from the donelist
 			    if(!updateDoneList(g))
 				throw new RuntimeException("Node "+g+" is in the SGHashMap, but not on the doneList");
 			}
 			
 			return true;
 
 		    }
 		    else
 			return false;
 		}
 		else{
 		    //we do not need to remove anything. Add the
 		    //Queue<state> in a sorted order
 		    
 		    float myCost = nMN.get(nMN.size()-1).getCurrentCost();
 		    int counter=0;
 		    for(Queue<state> tutu : ll){
 			if(myCost <= ((LinkedList<state>)tutu).get(tutu.size()-1).getCurrentCost()){
 			    ll.add(counter,nMN);
 			    break;
 			}
 			++counter;
 		    }
 		    return true;
 		    
 		}
 	    }
 	    else{
 		//This means that K is atleast 1
 		ll = new LinkedList<Queue<state>>();
 		ll.add(nMN);
 		guardMap.put(nMNGS,ll);
 		return true;
 	    }
 	    
 	}
 	
     }
     
     private static boolean isJoinNode(state s){
 	//Is "s" a join node??
 	return s.getIsJoinNode();
     }
 
     // private static ArrayList<state> getParent(state sNode,state s){
     // 	ArrayList<state> ret= null;
     // 	if(sNode.ifVisited()) return ret; 
     // 	sNode.setVisited();
 	
     // 	//Get the real parent, instead of some wrong parent with the
     // 	//same name.
     // 	if(sNode == s){
     // 	    ret = new ArrayList<state>();
     // 	    ret.add(sNode);
     // 	}
     // 	if(ret!=null) return ret;
     // 	for(int e=0;e<sNode.getConnectionCount();++e){
     // 	    if(sNode.getConnectionAt(e).getDirection().equals(GXL.IN)){
     // 		GXLEdge le = (GXLEdge)sNode.getConnectionAt(e).getLocalConnection();
     // 		if(le.getAttr("parallelEdge")==null){
     // 		    state node = (state)le.getTarget();
     // 		    ret = getParent(node,s);
     // 		}
     // 	    }
     // 	}
     // 	return ret;
     // }
 
     private static void clearVisited(state sNode){
     	sNode.setVisited(false);
     	for(int e=0;e<sNode.getConnectionCount();++e){
     	    if(sNode.getConnectionAt(e).getDirection().equals(GXL.IN)){
     		GXLEdge le = (GXLEdge)sNode.getConnectionAt(e).getLocalConnection();
     		if(le.getAttr("parallelEdge")==null){
     		    state node = (state)le.getTarget();
     		    clearVisited(node);
     		}
     	    }
     	}
     }
 }
