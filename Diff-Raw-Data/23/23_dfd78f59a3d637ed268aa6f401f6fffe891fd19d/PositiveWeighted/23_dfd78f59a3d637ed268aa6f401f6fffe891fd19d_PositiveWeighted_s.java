 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package isad.w6.graph.Path;
 
 import isad.w6.graph.Edge;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author jappie
  */
 public class PositiveWeighted <T> extends PathStrategy<T>{
 	public PositiveWeighted(PathGraph<T> dataHolder){
 		super(dataHolder);
 	}
 
 	@Override
 	public PathGraph<T> Find(T from) {
 		PathGraph<T> data = getData();
 		
 		PathVertex<T> current = (PathVertex<T>)data.getVertex(from);
 		current.setDistance((double)0);
 		DijkstrasAlgoritm(data.listVertexes());
 		return data;
 	}
 	/**
 	 * selects a vertex to use dijkstras algoritm on
 	 * @param vertexes 
 	 */
 	private void DijkstrasAlgoritm(List<PathVertex<T>> vertexes){
 		Iterator<PathVertex<T>> i = vertexes.iterator();
 		PathVertex<T> smallest = null;
 		while(i.hasNext()){
 			PathVertex<T> current = i.next();
 			
 			if(current.getStatus() != PathVertex.Status.Used){
 				if(smallest == null){
 					smallest = current;
 				}else if(smallest.getDistance() > current.getDistance()){
 					smallest = current;
 				}
 			}
 		}
 		if(smallest != null){
 			DijkstrasAlgoritm(smallest);
 			DijkstrasAlgoritm(vertexes);
 		}
 		
		// reseting status for the to string
		i = vertexes.iterator();
		while(i.hasNext()){
			PathVertex<T> current = i.next();
			current.setStatus(PathVertex.Status.Default);
		}
 	}
 	/**
 	 * the core of dijkstras algoritm, without the select vertex part
 	 * @param target 
 	 */
 	protected void DijkstrasAlgoritm(PathVertex<T> target){
 		target.setStatus(PathVertex.Status.Used);
 		Iterator<Edge> i = target.getConnections().iterator();
 		while(i.hasNext()){
 			Edge e = i.next();
 			PathVertex<T> next = (PathVertex<T>) e.getTo();
 			Double calced = e.getWeight() + target.getDistance();
 			if(next.getDistance() > calced){
 				onOverwrite();
 				next.setDistance(calced);
 				next.setPrevious(target);
 			}
 			onLoopEnd(next);
 		}
 	}
 	
 	protected void onOverwrite(){}
 	protected void onLoopEnd(PathVertex<T> target){}
 }
