 package supersql.codegenerator;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import supersql.common.Log;
 import supersql.extendclass.ExtHashSet;
 import supersql.extendclass.ExtList;
 import supersql.parser.Preprocessor;
 
 public class Attribute extends Operand {
 
 	int AttNo;
 
 	int AttCounts = 0;
 
 	protected String AttName;
 	protected String AttName1;
 	protected String AttName2;
 	private int AttNo1;
 	protected ArrayList<String> AttNames = new ArrayList<String>();
 
 	private String condition;
 
 	int AttType;
 
 	String ValKey;
 
 	protected ExtList<AttributeItem> Items = new ExtList<AttributeItem>();
 	
 	boolean conditional;
 
 	public Attribute() {
 		super();
 		conditional = false;
 	}
 	
 	public Attribute(Boolean b) {
 		super();
 		conditional = b;
 	}
 
 	public int setItem(int no, String nm, String attimg, String key,
 			Hashtable<Integer, AttributeItem> attp) {
 
 		if(conditional){
 			AttNames.add(nm);
 		}else
 		{
 			AttNo = no;
 			AttName = nm;
 			AttNames.add(nm);
 		}
 		ValKey = key;
 
 		//tk/////////////////////////////////////////////////////////////////
 		StringTokenizer st0 = new StringTokenizer(attimg, "\"+", true);		
 		//StringTokenizer st0 = new StringTokenizer(attimg, "\\\"+", true);
 		//tk//////////////////////////////////////////////////////////////////
 		String ch1, buf;
 		AttributeItem item;
 		while (st0.hasMoreTokens()) {
 			ch1 = st0.nextToken();
 			if (ch1.equals("+")) {
 				continue;
 			}
 			if (ch1.equals("\"")) {
 				// quoted str
 				buf = "";
 				while (st0.hasMoreTokens()) {
 					ch1 = st0.nextToken();
 					if (ch1.equals("\\")) {
 						buf += ch1;
 						buf += st0.nextToken();
 					} else if (ch1.equals("\"")) {
 						Items.add(new AttributeItem(buf));
 						break;
 					}
 					buf += ch1;
 				}
 			} else {
 				item = new AttributeItem(ch1, no);
 				Items.add(item);
 				attp.put(new Integer(no), item);
 				no++;
 			}
 		}
 		Log.out("[set Attribute] Attribute Items : " + Items);
 		Log.out("[set Attribute] Sch: " + this.makesch());
 
 		AttCounts = no - AttNo - AttNo1;
 		return no;
 
 	}
 
 	public void addDeco(String key, Object val) {
 		if(key.equals("insert")
 				||key.equals("update")
 				||key.equals("delete")
 				||key.equals("login")){
 			decos.put(key, AttName);
 			return;
 		}
 		decos.put(key, val);
 	}
 
 	public void debugout() {
 		debugout(0);
 	}
 
 	public void debugout(int count) {
 
 		Debug dbgout = new Debug();
 		dbgout.prt(count, "<Attribute No=" + AttNo + " AttName=" + AttName
 				+ " AttType=" + AttType + " decoitems=" + decos.size() + " id=" + id + ">");
 		if (ValKey != null) {
 			dbgout.prt(count + 1, "<ValKey>");
 			dbgout.prt(count + 2, ValKey);
 			dbgout.prt(count + 1, "</ValKey>");
 		}
 		dbgout.prt(count + 1, "<AttributeItems>");
 		for (int i = 0; i < Items.size(); i++) {
 			Items.get(i).debugout(count + 2);
 			decos.put("attributeName", Items.get(i).toString());//add by chie
 		}
 		dbgout.prt(count + 1, "</AttributeItems>");
 
 		decos.debugout(count + 1);
 
 		dbgout.prt(count, "</Attribute>");
 	}
 
 	public ExtList<Integer> makesch() {
 		ExtList<Integer> outsch = new ExtList<Integer>();
 
 		for (int i = 0; i < Items.size(); i++) {
 			outsch.addAll((Items.get(i)).makesch());
 		}
 
 		if (orderFlag) {
 			Preprocessor.putOrderByTable(order, outsch);
 			orderFlag = false;
 		} 
 		
 		if (aggregateFlag) {
 			Preprocessor.putAggregateList(outsch, aggregate);
 			aggregateFlag = false;
 		}
 
 		return outsch;
 	}
 
 	public ExtList makele0() {
 
 		ExtList attno = new ExtList();
 		//  attno.add("Att");
 
 		for (int i = 0; i < Items.size(); i++) {
 			attno.addAll(Items.get(i).makele0());
 		}
 
 		Log.out("Att le0:" + attno);
 
 		return attno;
 	}
 
 	public void work(ExtList data_info) {
 		Log.out("Attribute : " + data_info.toString());
 	}
 
 	public <T> String getStr(ExtList<T> data_info) {
 		
 		String str = "";
 		
 		if(conditional){
			int stringItemsNumber = 0; 
			Iterator iterator = Items.iterator();
			while(iterator.hasNext()){
				if(((AttributeItem)iterator.next()).IsStr)
					stringItemsNumber++;
			}
			String toCompare = (data_info.get(Items.size()-1-decos.getConditionsSize() - stringItemsNumber)).toString();
 			if(toCompare.equals("t") || toCompare.equals("1")){
				str = Items.get(0).getStr(data_info, AttNo-decos.getConditionsSize());
 			}
 			else if(toCompare.equals("f") || toCompare.equals("0")){
 				if(Items.size()-decos.getConditionsSize() == 3)
					str = Items.get(1).getStr(data_info, AttNo-decos.getConditionsSize());
 				else
 					str = "";
 			}
 			else throw new IllegalStateException();
 			return str;
 		}
 		else{
 			for (int i = 0; i < Items.size()-decos.getConditionsSize(); i++) {
 				str += (Items.get(i).getStr(data_info, AttNo-decos.getConditionsSize()));
 			}
 			return str;
 		}
 		
 		
 	}
 	
 
 	public int countconnectitem() {
 		int itemcount = 0;
 		for (int i = 0; i < Items.size(); i++) {
 			itemcount += Items.get(i).countconnectitem();
 		}
 		return itemcount;
 	}
 
 	public DecorateList get_DecorateList() {
 		return decos;
 	}
 
 	public ExtHashSet getUseTablesAll() {
 		ExtHashSet rs = new ExtHashSet();
 		for (int i = 0; i < Items.size(); i++) {
 			rs.add(Items.get(i).getUseTables());
 		}
 		return rs;
 	}
 
 	@Override
 	public String toString() {
 		if(AttNames.size() > 1)
 			return AttNames.toString();
 		else
 			return AttName;			
 	}
 
 	public String getKey() {
 		return this.ValKey;
 	}
 	
 	//added by ria 20110913 start
 	public ExtList makeschImage() {
 		ExtList outsch = new ExtList();
 
 		for (int i = 0; i < Items.size(); i++) {
 			outsch.addAll(Items.get(i).makeschImage());
 		}
 		
 		return outsch;
 	}
 	//added by ria 20110913 end
 
 	public String getCondition() {
 		return condition;
 	}
 
 	public void setCondition(String condition) {
 		this.condition = condition;
 	}
 
 	public void addDeco(String key, String val, String condition) {
 		if(key.equals("insert")||key.equals("update")||key.equals("delete")||key.equals("login")){
 			decos.put(key, AttName, condition);
 			return;
 		}
 		decos.put(key, val, condition);
 	}
 
 	@Override
 	public Object createNode(ExtList<ExtList<String>> data_info) {
 		return null;
 	}
 
 }
