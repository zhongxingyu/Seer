 package at.ac.tuwien.sepm.service.impl;
 
 import at.ac.tuwien.sepm.entity.*;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.IntelligentSemesterPlaner;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 
 public class IntelligentSemesterPlanerImpl implements IntelligentSemesterPlaner {
 	private ArrayList<MetaLVA> forced = new ArrayList<MetaLVA>();
 	private ArrayList<MetaLVA> pool = new ArrayList<MetaLVA>();
 	private DependenceTree tree;
     Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
     private boolean[][] intersecting;
     private long startedPlanning;
     private int planningTimeTolerance =10000;
     private List<Integer> typesToIntersect;
     private float intersectingTolerance = 0;
     private boolean canceled;
     private int timeBetween=0;
 
     @Override
     public void setLVAs(List<MetaLVA> forced, List<MetaLVA> pool){
         logger.debug("setting LVAs..");
         long timeStarted= System.currentTimeMillis();
 
         if(forced==null){
             forced = new ArrayList<MetaLVA>(0);
         }
         if(pool==null){
             pool = new ArrayList<MetaLVA>(0);
         }
         this.pool.clear();
         this.forced.addAll(forced);
         for(MetaLVA lva:pool){
             if(!lva.isCompleted()){
                 this.pool.add(lva);
             }
         }
         for(MetaLVA lva:forced){
             if(!this.pool.contains(lva)){
                 this.pool.add(lva);
             }
         }
         Collections.shuffle(this.forced);
         Collections.sort(this.forced);
         logger.debug("forced set:\n"+ LVAUtil.formatShortMetaLVA(this.forced, 2));
         logger.debug("pool set:\n"+LVAUtil.formatShortMetaLVA(this.pool, 2));
         logger.debug("finished setting LVAs. Time passed: "+(System.currentTimeMillis()-timeStarted)/1000f +" secounds");
     }
 
     @Override
     public ArrayList<MetaLVA> planSemester(float goalECTS,int year,Semester sem){
         logger.debug("start planning..");
         startedPlanning = System.currentTimeMillis();
         if(typesToIntersect==null){
             typesToIntersect=new ArrayList<Integer>();
             typesToIntersect.add(LVAUtil.LECTURE_TIMES);
             typesToIntersect.add(LVAUtil.EXERCISES_TIMES);
             typesToIntersect.add(LVAUtil.EXAM_TIMES);
         }
 		tree = new DependenceTree(new ArrayList<MetaLVA>(pool));
 		ArrayList<MetaLVA> roots = tree.getRoots();
 		ArrayList<MetaLVA> toPlan = new ArrayList<MetaLVA>(roots.size());
 		for(MetaLVA mLVA :roots){
 			if(mLVA.containsLVA(year, sem)){
 				toPlan.add(mLVA);
 			}
 		}
         Collections.shuffle(toPlan);
         Collections.sort(toPlan);
         ArrayList chosen = new ArrayList<Integer>();
         int actualECTS = 0;
         for(MetaLVA mLVA :forced){
             if(!toPlan.contains(mLVA) && mLVA.containsLVA(year, sem)){
                 toPlan.add(mLVA);
             }
             if(mLVA.containsLVA(year, sem)){
                 chosen.add(toPlan.indexOf(mLVA));
                 actualECTS+=mLVA.getECTS();
             }
         }
         computeSolution(toPlan,chosen,goalECTS,actualECTS);
         intersectAll(toPlan,year,sem);
 
         canceled = false;
 		recPlanning(toPlan,0,chosen,goalECTS,actualECTS);
         logger.debug("finished planning. Time passed: "+(System.currentTimeMillis()-startedPlanning)/1000f +" secounds");
         if(bestSolution!=null){
 		    return bestSolution;
         }
         return new ArrayList<MetaLVA>();
 	}
     private ArrayList<MetaLVA> bestSolution=null;
     private float bestSolutionECTS=0;
 
 	private float solutionValue=Float.NEGATIVE_INFINITY;
 	private void recPlanning(ArrayList<MetaLVA> all,int index,ArrayList<Integer> chosen,float goalECTS,float actualECTS){
         if(System.currentTimeMillis()-startedPlanning> planningTimeTolerance){
             if(!canceled){
                 logger.debug("Time ran out. Providing best solution so far.");
             }
             canceled=true;
             return;
         }
         for(int i=index;i<all.size();i++){
 
             if(actualECTS+all.get(i).getECTS()>(goalECTS+2)){
                 continue;
             }
 			boolean intersect=false;
 			for(Integer j:chosen){
 
 				if(intersect(i,j)){
 					intersect=true;
 					break;
 				}
 			}
 			if(!intersect){
 				chosen.add(i);
 				if(goalECTS-actualECTS<3 || goalECTS-bestSolutionECTS>3){
                     computeSolution(all,chosen,goalECTS,actualECTS+all.get(i).getECTS());
                 }else{
                     //logger.debug("skipping calculation. GoalECTS: "+goalECTS+", actualECTS: "+actualECTS+", bestSolutionECTS: "+bestSolutionECTS);
                 }
 
 
                 recPlanning(all,i+1,chosen, goalECTS,actualECTS+all.get(i).getECTS());
                 
                 chosen.remove(chosen.size()-1);
             }else{
                 //logger.debug("intersecting: "+all.get(lastJ).toShortString()+" with: "+all.get(i).toShortString());
             }
 		}
 	}
     private void intersectAll(List<MetaLVA> metaLVAs,int year, Semester sem){
         ArrayList<LVA> lvas = new ArrayList<LVA>();
         for(MetaLVA m : metaLVAs){
             lvas.add(m.getLVA(year,sem));
         }
         intersecting = LVAUtil.intersectToArray(lvas,timeBetween,typesToIntersect,intersectingTolerance,true);
         String debug = "";
         String separator=" ";
         if(intersecting.length>100){
             separator="";
         }
         for(int y=0;y<lvas.size();y++){
             for(int x=0;x<lvas.size();x++){
                if(intersect(x,y)){
                    debug+="1";
                }else{
                    debug+="0";
                }
                 debug+=separator;
             }
             debug+="\n";
         }
         logger.debug("intersecting array:\n"+debug);
 
     }
     private boolean intersect(int a, int b){
         if(a>b){
             return intersecting[b][a-b];
         }else if(a==b){
             return false;
         }
         return intersecting[a][b-a];
     }
 	private void computeSolution(ArrayList<MetaLVA> all,ArrayList<Integer> chosen,float goalECTS,float actualECTS) {
         float value=0;
 		for(Integer i:chosen){
 			value+=all.get(i).getECTS()*tree.getPriority(all.get(i))/actualECTS;
 		}
 		//value-=(Math.pow(Math.abs(goalECTS-actualECTS),1.5);
         value-=15f*(Math.abs(goalECTS-actualECTS)/Math.max(goalECTS,0.01));
         if(value>solutionValue){
 			ArrayList<MetaLVA> newSolution = new ArrayList<MetaLVA>();
 			for(Integer i:chosen){
 				newSolution.add(all.get(i));
 			}
 			bestSolution=newSolution;
 			solutionValue=value;
             logger.debug("new Solution found: \n" +LVAUtil.formatMetaLVA(newSolution, 2)+"\n\t\tsolution value: "+value+", with : "+actualECTS+" ECTS");
 		    bestSolutionECTS=actualECTS;
         }else{
             //active for detailed debugging
 			/*ArrayList<MetaLVA> toDiscard = new ArrayList<MetaLVA>();
 			for(Integer i:chosen){
 				toDiscard.add(all.get(i));
 			}
 			logger.debug("discarding set: " +toDiscard+"\nsolution value: "+value);*/
 
 		}
 	}
 
 
     @Override
     public void setTypesToIntersect(List<Integer> typesToIntersect) {
         this.typesToIntersect = typesToIntersect;
     }
 
     @Override
     public void setIntersectingTolerance(float intersectingTolerance) {
         this.intersectingTolerance = intersectingTolerance;
     }
 
     @Override
     public void setAllowedTimeBetween(int time) {
         timeBetween = time;
     }
 
     public static class DependenceTree implements Iterable<MetaLVA>{
 
 
         ArrayList<Node> pool = new ArrayList<Node>();
         ArrayList<Node> roots = new ArrayList<Node>();
         ArrayList<Node> leaves= new ArrayList<Node>();
         HashMap<String,Node> nodes;
         ArrayList<Node> nodesList;
         Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
 
         public DependenceTree(List<MetaLVA> lvas){
             nodesList = new ArrayList<Node>();
             nodes = new HashMap<String,Node>(lvas.size()+(int)(lvas.size()*0.2));
             for(MetaLVA lva: lvas){
                 Node tempNode =new Node(lva);
                 nodes.put(lva.getNr(), tempNode);
                 nodesList.add(tempNode);
             }
             for(MetaLVA lva: lvas){
                 Node tempNode=nodes.get(lva.getNr());
                 List<MetaLVA> tempPres =tempNode.getLVA().getPrecursor();
                 for(MetaLVA pre : tempPres){
                     if(nodes.containsKey(pre.getNr())){
                         tempNode.addChild(nodes.get(pre.getNr()));
                     }
                 }
                 if(tempNode.getChildren().isEmpty()){
                     roots.add(tempNode);
                 }
             }
             for(MetaLVA lva: lvas){
                 Node tempNode=nodes.get(lva.getNr());
                 if(tempNode.getParents().isEmpty()){
                     leaves.add(tempNode);
                 }
             }
         }
         public ArrayList<MetaLVA> getRoots(){
             Collections.sort(roots);
             ArrayList<MetaLVA> toReturn = new ArrayList<MetaLVA>(roots.size());
             for(Node n:roots){
                 toReturn.add(n.getLVA());
             }
             return toReturn;
         }
         public ArrayList<MetaLVA> getLeaves(){
             Collections.sort(leaves);
             ArrayList<MetaLVA> toReturn = new ArrayList<MetaLVA>(leaves.size());
             for(Node n:leaves){
                 toReturn.add(n.getLVA());
             }
             return toReturn;
         }
         public void changePriorityByParents(float rate){
             for(Node n:roots){
                 n.changePriorityByParents(rate);
             }
         }
         public void changePriorityBySemester(float delta){
             for(Node n:nodesList){
                 logger.debug(n.getLVA());
                 n.changePriorityBySemester(delta);
             }
         }
         public float getPriority(MetaLVA lva){
             return nodes.get(lva.getNr()).getPriority();
         }
         private class Node implements Comparable<Node>{
             MetaLVA lva;
             float priority;
             ArrayList<Node> children = new ArrayList<Node>();
             ArrayList<Node> parents = new ArrayList<Node>();
             private Node(MetaLVA lva){
                 this.lva=lva;
                 this.priority=lva.getPriority();
             }
             public MetaLVA getLVA(){
                 return lva;
             }
             private void addChild(Node other){
                 children.add(other);
                 other.parents.add(this);
             }
             private ArrayList<Node> getChildren(){
                 return children;
             }
             private ArrayList<Node> getParents(){
                 return parents;
             }
             public float changePriorityByParents(float rate){
                 for(Node n:parents){
                     priority+=n.changePriorityByParents(rate);
                 }
                 return priority*rate;
             }
             public void changePriorityBySemester(float delta){
                 Semester temp = lva.getSemestersOffered();
                 switch(temp){
                 case S:
                 case W:
                     priority+=delta;
                     break;
                 case W_S:
                     priority-=delta;
                 }
             }
             public float getPriority(){
                 return priority;
             }
             @Override
             public int compareTo(Node o) {
                 return (int) ((priority - o.priority)*128);
             }
         }
         @Override
         public Iterator<MetaLVA> iterator() {
             return new MetaLVAIterator();
         }
         private class MetaLVAIterator implements Iterator<MetaLVA>{
             Iterator<Node> rootsIter;
             private MetaLVAIterator(){
                 Collections.sort(roots);
                 rootsIter = roots.iterator();
             }
             @Override
             public boolean hasNext() {
                 return rootsIter.hasNext();
             }
 
             @Override
             public MetaLVA next() {
                 return rootsIter.next().getLVA();
             }
 
             @Override
             public void remove() {
             }
 
         }
     }
 }
