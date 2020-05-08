 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 public class Teachers implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public enum Type {MATH, READ, LA, HR, SP}
 	private String name;
 	// tracks the classes a teacher can teach. K-8:1-9
 	// preference: ordering most preferred in position 0. changes while scheduling.
 	private ArrayList<Integer> mathPreference;
 	private ArrayList<Integer> readPreference;
 	private ArrayList<Integer> laPreference;
 	// capable: order does not matter. does not change. 
 	public ArrayList<Integer> capableM;
 	public ArrayList<Integer> capableR;
 	public ArrayList<Integer> capableL;
 	private int clsIDM=-1;
 	private int clsIDR=-1;
 	private int clsIDL=-1;
 	private int clsIDH=-1;
 	private int clsIDS=-1;
 	private int clsLvlM=-1;
 	private int clsLvlR=-1;
 	private int clsLvlL=-1;
 	private int room=-1;
 	private ClassFactory clsFac;
 	
 	public Teachers(String name, ArrayList<Integer> math, ArrayList<Integer> read, ArrayList<Integer> la, ClassFactory cf){
 		clsFac = cf;
 		this.name = name;
 		this.mathPreference = math;
 		this.capableM = new ArrayList<Integer>(math);
 		this.readPreference = read;
 		this.capableR = new ArrayList<Integer>(read);
 		this.laPreference = la;
 		this.capableL = new ArrayList<Integer>(la);
 	}
 	
	public Teachers(String name, ClassFactory cf) {
		clsFac = cf;
		this.name = name;
	}
	
 	public ArrayList<Integer> teaches()
 	{
 		ArrayList<Integer> clss = new ArrayList<Integer>();
 		clss.add(this.clsIDM);
 		clss.add(this.clsIDR);
 		clss.add(this.clsIDL);
 		clss.add(this.clsIDH);
 		clss.add(this.clsIDS);
 		return clss;
 	}
 	
 	public Teachers(String name) {
 		this.name = name;
 	}
 	
 	public String getName(){
 		return this.name;
 	}
 	
 	public void setName(String n){
 		name = n;
 	}
 	
 	public Classes getCls(Type t)
 	{
 		switch (t) {
 		case MATH: {
 			for (Classes c : clsFac.mathClsLst) {
 				if (c.getClsID() == this.clsIDM) {
 					return c;
 				}
 			}
 			return null;
 		}
 		case READ:
 		{
 			for (Classes c : clsFac.readClsLst) {
 				if (c.getClsID() == this.clsIDR) {
 					return c;
 				}
 			}
 			return null;
 		}
 		case LA:
 		{
 			for (Classes c : clsFac.laClsLst) {
 				if (c.getClsID() == this.clsIDL) {
 					return c;
 				}
 			}
 			return null;
 		}
 	
 		case HR:
 		{
 			for (Classes c : clsFac.homeroomClsLst) {
 				if (c.getClsID() == this.clsIDH) {
 					return c;
 				}
 			}
 			return null;
 		}
 
 		case SP:
 		{
 			for (Classes c : clsFac.specialClsLst) {
 				if (c.getClsID() == this.clsIDS) {
 					return c;
 				}
 			}
 			return null;
 		}
 		default:
 			return null;
 		}
 	}
 	
 	public int getClsID(Type t){
 		switch(t) {
 		case MATH:
 			return this.clsIDM;
 		case READ:
 			return this.clsIDR;
 		case LA:
 			return this.clsIDL;	
 		case HR:
 			return this.clsIDH;
 		case SP:
 			return this.clsIDS;
 		default:
 			return -1;
 		}
 	}
 
 	public int getClsLvl(Type t){
 		switch(t) {
 		case MATH:
 			return this.clsLvlM;
 		case READ:
 			return this.clsLvlR;
 		case LA:
 			return this.clsLvlL;
 		default:
 			return -1;			
 		}	
 	}
 
 	public void setCls(Integer clsLvl, Integer clsID, Type t){
 		switch(t) {
 		case MATH:
 		{
 			this.clsLvlM = clsLvl;
 			this.clsIDM = clsID;
 			break;
 		}
 		case READ:
 		{
 			this.clsLvlR = clsLvl;
 			this.clsIDR = clsID;
 			break;
 		}	
 		case LA:
 		{
 			this.clsLvlL = clsLvl;
 			this.clsIDL = clsID;
 			break;
 		}
 		case HR:
 		{
 			this.clsIDH = clsID;
 			break;
 		}
 		case SP:
 		{
 			this.clsIDS = clsID;
 		}
 		}
 	}
 	
 	public int firstPref(Type t){
 		switch(t) {
 		case MATH:
 		{
 			if(this.mathPreference.size() <= 0)
 				return -1;
 			return this.mathPreference.get(0);
 		}
 		case READ:
 		{
 			if(this.readPreference.size() <=0)
 				return -1;
 			return this.readPreference.get(0);
 		}
 		case LA:
 		{
 			if(this.laPreference.size() <=0)
 				return -1;
 			return this.laPreference.get(0);
 		}
 		default:
 			return -1;
 		}
 	}
 	
 	// changes/removes preference;
 	public void changePref(Integer cls, Type t){
 		switch(t) {
 		case MATH:
 		{
 			this.mathPreference.remove(cls);
 			break;
 		}
 		case READ:
 		{
 			this.readPreference.remove(cls);
 			break;
 		}
 		case LA:
 		{
 			this.laPreference.remove(cls);
 			break;
 		}
 		}
 	}
 	
 	// number of classes available to teach
 	public int availability(Type t){
 		switch(t) {
 		case MATH:
 			return this.mathPreference.size();
 		case READ:
 			return this.readPreference.size();
 		case LA:
 			return this.laPreference.size();
 		default:
 			return -1;
 		}
 	}
 	
 	public boolean canTeach(Integer cls, Type t){
 		switch(t) {
 		case MATH:
 			return this.capableM.contains(cls);
 		case READ:
 			return this.capableR.contains(cls);
 		case LA:
 			return this.capableL.contains(cls);
 		default:
 			return false;
 		}
 	}
 	
 	public void setRoom(int num){
 		this.room = num;
 	}
 	
 	public int getRoom(){
 		return this.room;
 	}
 	
 	public ArrayList<Integer> getPreference(Type t) {
 		switch(t) {
 		case MATH:
 			return this.mathPreference;
 		case READ:
 			return this.readPreference;
 		case LA:
 			return this.laPreference;
 		default:
 			return null;
 		}
 	}
 	
 	public void setPreference(ArrayList<Integer> p, Type t) {
 		switch(t) {
 		case MATH:
 		{
 			this.mathPreference = p;
 			this.capableM = new ArrayList<Integer>(p);
 			break;
 		}
 		case READ:
 		{
 			this.readPreference = p;
 			this.capableR = new ArrayList<Integer>(p);
 			break;
 		}
 		case LA:
 		{
 			this.laPreference = p;
 			this.capableL = new ArrayList<Integer>(p);
 			break;
 		}
 		}
 	}
 	
 	// returns n > 0, if this.teacher has more available classes compare to teacher
 	public int compareTo(Teachers teacher, Type t) {
 		return this.availability(t)-teacher.availability(t);
 	}
 	
 	@Override
 	public String toString() {
 		return name + "  Room: " + room +
 		"\n\tMath: lv " + clsLvlM + ", ClassID: " + clsIDM +
 		"\n\tRead: lv " + clsLvlR + ", ClassID: " + clsIDR +
 		"\n\tLA  : lv " + clsLvlL + ", ClassID: " + clsIDL +
 		"\n\tHomeroom    ClassID: " + clsIDH + 
 		"\n\tSpecial     ClassID: " + clsIDS;
 	}
 
 }
