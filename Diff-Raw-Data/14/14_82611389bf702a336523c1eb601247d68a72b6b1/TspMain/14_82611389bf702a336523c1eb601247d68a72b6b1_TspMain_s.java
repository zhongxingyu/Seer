 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
import java.util.Set;
 
 
 
 public class TspMain {
 	private static final boolean DEBUG = true;
 	int[][] distanceMatrix;
 	TspNode[] nodes;
 	int numNodes;
 	Visulizer graph;
 	int cost;
 	boolean visulize = false;
 	private float maxX = Float.MIN_VALUE;
 	private float minX = Float.MAX_VALUE;
 	private float maxY = Float.MIN_VALUE;
 	private float minY = Float.MAX_VALUE;
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new TspMain(args.length != 0);
 	}
 	
 	public TspMain(boolean visulize){
 		this.visulize = visulize;
 		try {
 			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 			numNodes = Integer.parseInt(in.readLine());
 			nodes = new TspNode[numNodes];
 			distanceMatrix = new int[numNodes][numNodes];
 			for(int i=0;i<numNodes;i++){
 				String line = in.readLine();
 				String[] xy = line.split(" ");
 				nodes[i] = new TspNode(i, Float.parseFloat(xy[0]), Float.parseFloat(xy[1]));
 				// if ClarkWright
 				minX = nodes[i].xPos < minX ? nodes[i].xPos : minX;
 				maxX = nodes[i].xPos > maxX ? nodes[i].xPos : maxX;
 				minY = nodes[i].yPos < minY ? nodes[i].yPos : minY;
 				maxY = nodes[i].yPos > maxY ? nodes[i].yPos : maxY;
 				if(DEBUG){
 					System.out.println("minX="+minX+", maxX="+maxX+", minY="+minY+", maxY="+maxY);
 				}
 			}
 			in.close();
 			for(int i=0;i<numNodes;i++){
 				for(int j=i+1;j<numNodes;j++){
 					distanceMatrix[i][j] = calcDistance(nodes[i], nodes[j]);
 					distanceMatrix[j][i] = distanceMatrix[i][j];
 				}
 			}
 			if(visulize){
 				graph = new Visulizer(nodes);
 				/*
 				 * test tar bara noderna i ordning
 				
 				
 				int[] order = new int[numNodes];
 				int cost = 0;
 				for(int i=0;i<numNodes;i++){
 					if(i == numNodes-1){
 						cost += distanceMatrix[0][numNodes-1];
 					}else{
 						cost += distanceMatrix[i][i+1];
 					}
 					order[i] = i;
 				}
 				graph.drawEdges(order, cost);
 				 */
 				
 			}
 			TspNode[] tour = clarkWright();
 			if(visulize) graph.drawEdges(tour, cost);
 			
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public TspNode[] clarkWright(){
 		TspNode middle = new TspNode(-1, (maxX-minX)/2, (maxY-minY)/2);
 		if(DEBUG){
 			System.out.println("centerX="+middle.xPos+", centerY="+middle.yPos);
 		}
 		int centerDistance = calcDistance(middle, nodes[0]);
 		TspNode hub = nodes[0]; 
 		for(int i=1;i<numNodes;i++){
 			int newDist = calcDistance(middle, nodes[i]);
 			if(centerDistance > newDist){
 				hub = nodes[i];
 				centerDistance = newDist;
 			}
 		}
		if(DEBUG){
			System.out.println("centerNodeX="+hub.xPos+", centerNodeY="+hub.yPos);
		}
 		
 		if(visulize){
 			int[] order = new int[2*numNodes-2];
 			LinkedList<Integer> tmp = new LinkedList<Integer>();
 			for(int i=0;i<numNodes;i++){
 				if(i != hub.nodeNumber) tmp.add(i);
 			}
 			int ith = 0;
 			while(!tmp.isEmpty()){
 				order[ith] = tmp.poll();
 				order[ith+1] = hub.nodeNumber;
 				ith += 2;
 			}
 			graph.drawEdges(order, 0);
 			
 		}
 		if(DEBUG) System.out.println("Savnings... begin");
 		LinkedList<Savings> queue = new LinkedList<Savings>(); 
 		for(int i=0;i<numNodes;i++){
 			if(i == hub.nodeNumber) continue;
 			for(int j=i+1;j<numNodes;j++){
 				if(j == hub.nodeNumber) continue;
 				int saving = distanceMatrix[hub.nodeNumber][i] + distanceMatrix[hub.nodeNumber][j] - distanceMatrix[j][i];
 				queue.add(new Savings(nodes[i], nodes[j], saving));
 			}
 		}
 		
 		Collections.sort(queue);
 		if(DEBUG) System.out.println("Savnings... DONE!");
 		ArrayList<TspNode> result = new ArrayList<TspNode>();
 		boolean[] visited = new boolean[numNodes];
 		Savings edge = queue.pollLast();
 		result.add(edge.from);
 		result.add(edge.to);
 		visited[edge.from.nodeNumber] = true;
 		visited[edge.to.nodeNumber] = true;
 		
 		cost = distanceMatrix[edge.to.nodeNumber][edge.from.nodeNumber];
 		
 		while(result.size() < numNodes-1){
 			for(int i=queue.size()-1;i>=0;i--){
 				edge = queue.get(i);
 				if(visited[edge.from.nodeNumber] ^ visited[edge.to.nodeNumber]){
 					if(result.get(0).equals(edge.from)){
 						result.add(0,edge.to);
 						visited[edge.to.nodeNumber] = true;
 						cost += distanceMatrix[edge.to.nodeNumber][edge.from.nodeNumber];
 						queue.remove(i);
 						break;
 					} else if(result.get(0).equals(edge.to)){
						result.add(0,edge.to);
 						visited[edge.from.nodeNumber] = true;
 						cost += distanceMatrix[edge.to.nodeNumber][edge.from.nodeNumber];
 						queue.remove(i);
 						break;
 					} else if(result.get(result.size()-1).equals(edge.from)){
 						result.add(edge.to);
 						visited[edge.to.nodeNumber] = true;
 						cost += distanceMatrix[edge.to.nodeNumber][edge.from.nodeNumber];
 						queue.remove(i);
 						break;
 					} else if(result.get(result.size()-1).equals(edge.to)){
 						result.add(edge.from);
 						visited[edge.from.nodeNumber] = true;
 						cost += distanceMatrix[edge.to.nodeNumber][edge.from.nodeNumber];
 						queue.remove(i);
 						break;
 					}
 				}
 			}
 			if(DEBUG) System.out.println(result.size()+" edges found");
 		}
 		result.add(hub);
 		cost += distanceMatrix[result.get(result.size()-1).nodeNumber][hub.nodeNumber];
 		cost += distanceMatrix[result.get(0).nodeNumber][hub.nodeNumber];
 		return result.toArray(new TspNode[]{});
 	}
 	
 	public TspNode[] nearestNeighbor(){
 		TspNode[] result = new TspNode[numNodes];
 		HashSet<TspNode> tmp = new HashSet<TspNode>(Arrays.asList(nodes));
 		result[0] = nodes[0];
 		tmp.remove(nodes[0]);
 		for(int i=0;i<result.length;i++){
 			
 		}
 		return result;
 	}
 	
 	public TspNode[] twoOpt(TspNode[] tour){
 		boolean change = true;
 		while(change){
 			for(int i=0;i<numNodes;i++){
 				for(int j=0;j<numNodes;j++){
 					
 				}
 			}
 		}
 		return tour;
 	}
 	
 	public int calcDistance(TspNode a, TspNode b){
 		float dx = Math.abs(a.xPos - b.xPos);
 		float dy = Math.abs(a.yPos - b.yPos);
 		return (int) Math.round(Math.hypot(dx, dy));
 	}
 }
