 package mod.industrialscience.modules.research.backend.model;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import mod.industrialscience.ResearchManager;
 public class Researchfactory{
 	private boolean defaultvalues=false;
 	private String Category;
 	private RecipeLocker Locker;
 	private Researchchecker Checker;
 	public Researchfactory(String category, RecipeLocker locker, Researchchecker checker) {
 		Category = category;
 		Locker = locker;
 		Checker=checker;
 		defaultvalues=true;
 	}
 	public Researchfactory() {
 	}
	public Research getResearch( String Name, ArrayList<Research> NeededResearches, String Category, Object Data, Researchstep[] Steps, RecipeLocker Locker, Researchchecker Checker){
		return new Research( Name, NeededResearches, Category, Data, Steps, Locker, Checker);
 	}
	public Research getResearch(String Name, ArrayList<Research> NeededResearches, Object Data, Researchstep[] Steps){
 		if(defaultvalues)
		return new Research(Name, NeededResearches, Category, Data, Steps, Locker, Checker);
 		return null;
 	}
	public Research getResearch( String Name, ArrayList<Research> NeededResearches, Object Data, Researchstep[] Steps, RecipeLocker Locker){
 		if(defaultvalues)
		return new Research(Name, NeededResearches, Category, Data, Steps, Locker, Checker);
 		return null;
 	}
 	public Research getResearch(String name, Hashtable<Integer, Boolean> steps){
 		Research research = ResearchManager.getInstance().getAllResearches().get(name);
 		research.enableSteps(steps);
 		return research;
 	}
 }
