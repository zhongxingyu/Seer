 package mod.industrialscience.modules.research.backend.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import net.minecraft.nbt.NBTTagCompound;
 
 public class Research implements Comparable<Research>{
 private String Name;
 private ArrayList<Research> NeededResearches;
 private String Category;
 private Researchstep[] Steps;
 private RecipeLocker Locker;
 private Researchchecker Checker;
 private String ProgrammingDate;
 
 public Research(String ProgrammingDate ,String name, ArrayList<Research> neededResearches, String category, Researchstep[] steps, RecipeLocker locker, Researchchecker checker) {
 	Name = name;
 	NeededResearches = neededResearches;
 	Category = category;
 	Steps = steps;
 	Arrays.sort(Steps);
 	Locker = locker;
 	Checker=checker;
 	this.ProgrammingDate=ProgrammingDate;
 }
 public boolean research(ResearchObject object){
 	if(!isResearched()){
 	int id=-1;
 	for (int i = 0; i < Steps.length; i++) {
 		if(!Steps[i].isEnabled()){
 			if(i!=Steps[i].getID()){
 				throw new RuntimeException();
 			}
 			id=i;
 		}
 	}
 	boolean toreturn = false;
 	if(id!=-1){
 	toreturn= Steps[id].research(object);}
 	if(isResearched())unlock();
 	return toreturn;
 	}
 	return false;
 }	
 public boolean check(){
 	return Checker.check();
 }
 public boolean isResearched(){
 		for(Researchstep rs : Steps){
 			if(!rs.isEnabled())
 				return false;
 		}
 		return true;
 	}
 	public int compareTo(Research o) {
 		return ProgrammingDate.compareTo(o.getProgrammingDate());
 	}
 	
 	private synchronized void unlock(){
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
 	public String getProgrammingDate() {
 		return ProgrammingDate;
 	}
 	public void writeToNBT(NBTTagCompound tagCompound){
	   tagCompound.setCompoundTag(String.valueOf(id), getStepNBTTag());
 	}
 	private NBTTagCompound getStepNBTTag() {
 		NBTTagCompound steps = new NBTTagCompound();
 		for(Researchstep r  : Steps){
 			NBTTagCompound enabled = new NBTTagCompound();
 			enabled.setBoolean("enabled", r.isEnabled());
 			steps.setCompoundTag(String.valueOf(r.getID()),enabled);
 		}
 		return steps;
 	}
 	public void setSteps(Researchstep[] steps) {
 		Steps = steps;
 	}
 
 }
