 package supersql.codegenerator;
 
 import java.util.Hashtable;
 
 import supersql.common.Log;
 import supersql.extendclass.ExtList;
 
 public class Function extends Operand {
 
 	protected String Name; 
 	protected ExtList<FuncArg> Args;     
 	protected Hashtable<String, FuncArg> ArgHash = new Hashtable<String, FuncArg>();
 
 	public Function() {
 		super();
 		Name = "";
 		Args = new ExtList<FuncArg>();
 	}
 
 	public void setFname(String name) {
 		Name = name;
 	}
 
 	public void addArg(FuncArg fa) {
 		Args.add(fa);
 	}
 
 	public void debugout(int count) {
 
 		Debug dbgout = new Debug();
 		dbgout.prt(count, "<Function Name=" + Name + " argitems=" + Args.size()
 				+ " decoitems=" + decos.size() + " id=" + id + ">");
 
 		for (int i = 0; i < Args.size(); i++) {
 			Args.get(i).debugout(count + 1);
 		}
 
 		decos.debugout(count + 1);
 		dbgout.prt(count, "</Function>");
 	}
 
 	public ExtList makesch() {
 		ExtList outsch = new ExtList();
 		ExtList outsch1 = new ExtList();
 
 		for (int i = 0; i < Args.size(); i++) {
 			outsch1 = ((FuncArg) Args.get(i)).makesch();
 			if (!outsch1.isEmpty()) {
 				outsch.addAll(outsch1);
 			}
 		}
 		
 		return outsch;
 	}
 
 	public ExtList makele0() {
 
 		ExtList le0 = new ExtList();
 		ExtList le0_1 = new ExtList();
 
 		for (int i = 0; i < Args.size(); i++) {
 			le0_1 = ((FuncArg) Args.get(i)).makele0();
 			if (!le0_1.isEmpty()) {
 				le0.addAll(le0_1);
 			}
 		}
 
 		Log.out("Fnc le0:" + le0);
 
 		return le0;
 	}
 
 	public void work(ExtList<ExtList<String>> data_info) {
 		Log.out("*Function");
 	}
 
 	public int countconnectitem() {
 		int items = 0;
 
 		for (int i = 0; i < Args.size(); i++) {
 			items += ((FuncArg) Args.get(i)).countconnectitem();
 		}
 
 		return items;
 	}
 
 	public void setDataList(ExtList<ExtList<String>> data) {
 		int dindex = 0;
 		int ci;
 		FuncArg fa;
 		for (int i = 0; i < Args.size(); i++) {
 			fa = Args.get(i);
 			ci = fa.countconnectitem();
 			fa.setData(data.ExtsubList(dindex, dindex + ci));
 			if (Name.equalsIgnoreCase("foreach")){
 				ArgHash.put(Integer.toString(i), fa);
 			} else {
 				String argName = fa.getKey();
 				if(!argName.equals("default")){
 					ArgHash.put(argName, fa);
 					Args.remove(i);
 				}
 					
 			}
 			dindex += ci;
 		}
 
 	}
 
 	public String getAtt(String Key) {
 		return getAtt(Key, "");
 	}
 
 	public String getAtt(String key, String default_str) {
 		FuncArg fa = (FuncArg) ArgHash.get(key);
		ArgHash.remove(key);
 		if (fa == null) {
 			return default_str;
 		}
 		String ret = fa.getStr();
 		if (ret == null) {
 			return default_str;
 		} else {
 			return ret;
 		}
 	}
 
 	public String getFuncName() {
 		return Name;
 	}
 
 	public void workAtt(String Key) {
 		FuncArg fa = ArgHash.get(Key);
 
 		if (fa != null) {
 			fa.workAtt();
 		}
 		return;
 	}
 	
 	public Object createNodeAtt(String key){
 		FuncArg fa = ArgHash.get(key);
 
 		if (fa != null) {
 			return fa.createNode();
 		}
 		return null;
 	}
 
 	public String getClassName() {
 
 		FuncArg fa;
 		String result = null;
 
 		for (int i = 0; i < Args.size(); i++) {
 			fa = Args.get(i);
 			if (fa.getKey().equalsIgnoreCase("class")) {
 			    result = fa.getStr();
 			}
 		}
 		
 		Log.out("getClassName = "+result);
 		return result;
 
 	}
 	
 	public ExtList makeschImage() {
 		ExtList outsch = new ExtList();
 		ExtList outsch1 = new ExtList();
 
 		for (int i = 0; i < Args.size(); i++) {
 			outsch1 = Args.get(i).makeschImage();
 			if (!outsch1.isEmpty()) {
 				outsch.addAll(outsch1);
 			}
 		}
 
 		return outsch;
 	}
 
 	@Override
 	public Object createNode(ExtList<ExtList<String>> data_info) {
 		return null;
 	}
 	
 }
