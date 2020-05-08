 package mod.industrialscience.modules.research.backend.model;
 
 import java.util.ArrayList;
 
 import mod.industrialscience.ResearchManager;
 import net.minecraft.nbt.NBTTagCompound;
 
 public class Researchfactory{
 	private String Category;
 	private RecipeLocker Locker;
 	private Researchchecker Checker;
 	public Researchfactory(String category, RecipeLocker locker, Researchchecker checker) {
 		super();
 		Category = category;
 		Locker = locker;
 		Checker=checker;
 	}
 	public Research getResearch(String ProgrammingDate, String Name, ArrayList<Research> NeededResearches, String Category, Researchstep[] Steps, RecipeLocker Locker, Researchchecker Checker){
 		return new Research(ProgrammingDate, Name, NeededResearches, Category, Steps, Locker, Checker);
 	}
 	public Research getResearch(String ProgrammingDate, String Name, ArrayList<Research> NeededResearches, Researchstep[] Steps){
 		return new Research(ProgrammingDate ,Name, NeededResearches, Category, Steps, Locker, Checker);
 	}
 	public Research getResearch(String ProgrammingDate, String Name, ArrayList<Research> NeededResearches, Researchstep[] Steps, RecipeLocker Locker){
 		return new Research(ProgrammingDate, Name, NeededResearches, Category, Steps, Locker, Checker);
 	}
	public Research getResearch(NBTTagCompound nbttc, String name){
		Research research = ResearchManager.getInstance().getAllResearches().get(name);
 		Researchstep[] cleansteps= research.getSteps();
 		Researchstep[] steps = new Researchstep[cleansteps.length];
 		Researchstep modedresearchstep;
 		for (Researchstep researchstep : cleansteps) {
 		modedresearchstep=researchstep;
 		modedresearchstep.setEnabled(nbttc.getBoolean(String.valueOf(researchstep.getID())));
 		steps[researchstep.getID()]=modedresearchstep;
 		}
 		research.setSteps(steps);
 		return research;
 	}
 }
