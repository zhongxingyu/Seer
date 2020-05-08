 package petrinet.logic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mxgraph.view.mxGraph;
 
 /**
  * This contains all of the properties of a petrinet and creates
  * the framework to allow objects to be added.
  */
 
 
 public class Petrinet {
 
     List<Place> places              = new ArrayList<Place>();
     List<Transition> transitions    = new ArrayList<Transition>();
     List<Arc> arcs                  = new ArrayList<Arc>();
     String filepath;
    public mxGraph graph;
     private String name;
     
     // Default constructor
     public Petrinet(String name) {
         super();
         this.graph = new mxGraph();
         this.name = name;
     }
 
     /** 
      * @param item A PetrinetObject to be added to the petrinet
      */
     public void add(PetrinetObject item) {
         if (item instanceof Arc) {
             arcs.add((Arc) item);
         } else if (item instanceof Place) {
             places.add((Place) item);
         } else if (item instanceof Transition) {
             transitions.add((Transition) item);
         }
     }
     
     /**
      * @return A list of transitions that are ready to fire
      */
     public List<Transition> getTransitionsAbleToFire() {
         ArrayList<Transition> list = new ArrayList<Transition>();
         for (Transition t : transitions) {
             if (t.canFire()) {
                 list.add(t);
             }
         }
         return list;
     }
     
     /**
      * @param name The name of the new transition
      * @return The new transition that was created and added to the petrinet
      */
     public Transition addTransition(String name) {
         Transition t = new Transition(name, graph);
         transitions.add(t);
         return t;
     }
     
     /**
      * @param name The name of the new place
      * @return The new place that was added to the petrinet with 0 tokens
      */
     public Place addPlace(String name) {
         return addPlace(name, 0);
     }
     
     /**
      * @param name The name for the new place to be added to the petrinet
      * @param tokens How many initial tokens the new place will have
      * @return The new place that was added to the petrinet
      */
     public Place addPlace(String name, int tokens) {
         Place p = new Place(name, tokens, graph);
         places.add(p);
         return p;
     }
     
     /**
      * @param name The name of the new arc
      * @param place A place that the arc will originate at
      * @param tran A transition that the arc will end at
      * @return A new arc that was added to the petrinet
      */
     public Arc addArc(String name, Place place, Transition tran) {
         Arc arc = new Arc(name, place, tran, graph);
         arcs.add(arc);
         return arc;
     }
     
     /**
      * @param name The name of the new arc
      * @param tran A transition that the arc will originate at
      * @param place A place that the arc will end at
      * @return A new arc that was added to the petrinet
      */
     public Arc addArc(String name, Transition tran, Place place) {
         Arc arc = new Arc(name, tran, place, graph);
         arcs.add(arc);
         return arc;
     }
         
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("Petrinet ");
         sb.append(super.toString() + "\n");
         sb.append("---Transitions---\n");
         for (Transition t : transitions) {
             sb.append(t).append("\n");
         }
         sb.append("---Places---\n");
         for (Place p : places) {
             sb.append(p).append("\n");
         }
         return sb.toString();
     }
 
     /**
      * Searches through the places in the petrinet to find one by name
      * @param name The name of a place to search for
      * @return If a place is found it is returned, otherwise null
      */
 	public Place getPlace(String name) {
 		for (Place p : places) {
 			if (p.getName().equalsIgnoreCase(name)) {
             	return p;
 			}
 		}
 		return null;
 	}
 	
     public List<Place> getPlaces() {
         return places;
     }
 
     /**
      * Searches through the transitions in the petrinet to find one by name
      * @param name The name of a transition to search for
      * @return If a transition is found it is returned, otherwise null
      */
 	public Transition getTransition(String name) {
 		for (Transition t : transitions) {
 			if (t.getName().equals(name)) {
 				return t;
 			}
 		}
 		return null;
 	}
 	
     public List<Transition> getTransitions() {
         return transitions;
     }
 
     /**
      * Searches through the arcs in the petrinet to find one by name
      * @param name The name of a arc to search for
      * @return If an arc is found it is returned, otherwise null
      */	public Arc getArc(String name) {
 		for (Arc a : arcs) {
 			if (a.getName().equals(name)) {
 				return a;
 			}
 		}
 		return null;
 	}
 
     public List<Arc> getArcs() {
         return arcs;
     }
 
     
     public String getFilepath(){
     	return this.filepath;
     }
     
     public void setFilepath(String newPath){
     	this.filepath = newPath;
 
     	String[] title = newPath.replace("\\","/").split("/");
 		title[title.length - 1] = title[title.length - 1].replace(".xml", "");
 		title[title.length - 1] = title[title.length - 1].replace("_", " ");
 		setName(title[title.length - 1]);
     }
     
     public void clear(){
     	arcs.clear();
     	places.clear();
     	transitions.clear();
    	graph = new mxGraph();
     }
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
