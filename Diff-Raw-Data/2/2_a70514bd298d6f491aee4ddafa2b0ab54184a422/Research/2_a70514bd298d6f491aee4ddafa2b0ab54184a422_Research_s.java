 package mod.industrialscience.modules.research.backend.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class Research implements Comparable<Research>{
 private String Name;
 private ArrayList<Research> NeededResearches;
 private String Category;
 private Researchstep[] Steps;
 private RecipeLocker Locker;
 private Researchchecker Checker;
 
 public Research(String name, ArrayList<Research> neededResearches, String category, Researchstep[] steps, RecipeLocker locker, Researchchecker checker) {
 	Name = name;
 	NeededResearches = neededResearches;
 	Category = category;
 	Steps = steps;
 	Arrays.sort(Steps);
 	Locker = locker;
 	Checker=checker;
 }
 public boolean research(ResearchObject object){
 	int id=-1;
 	for (int i = 0; i < Steps.length; i++) {
 		if(!Steps[i].isEnabled()){
 			if(i!=Steps[i].getID()){
 				throw new RuntimeException();
 			}
 			id=i;
 		}
 	}
 	return Steps[id].research(object);
 	
 }	
 public boolean check(){
 	return Checker.check();
 }
public boolean isRearched(){
 		for(Researchstep rs : Steps){
 			if(!rs.isEnabled())
 				return false;
 		}
 		return true;
 	}
 	public int compareTo(Research o) {
 		return Name.compareTo(o.getName());
 	}
 	
 	public synchronized void unlock(){
 		Locker.unlock();
 	}
 	public synchronized void lock(){
 		Locker.lock();
 	}
 	
 
 	public String getName() {
 		return Name;
 	}
 	public ArrayList<Research> getNeededResearches() {
 		return NeededResearches;
 	}
 	public String getCategory() {
 		return Category;
 	}
 	public synchronized Researchstep[] getSteps() {
 		return Steps;
 	}
 	public String toString(){
 		return "Researchname: "+Name+", ResearchCategory: "+Category;
 		
 	}
 
 }
