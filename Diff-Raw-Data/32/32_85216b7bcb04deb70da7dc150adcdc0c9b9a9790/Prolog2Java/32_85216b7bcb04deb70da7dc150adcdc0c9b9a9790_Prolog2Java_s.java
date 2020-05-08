 package envJavaSpace;
 
import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import oopl.DistributedOOPL;
 import net.jini.core.entry.Entry;
import tuplespace.Cargo;
import tuplespace.Cell;
import tuplespace.Coin;
import tuplespace.Investigate;
import tuplespace.Position;
import tuplespace.Reading;
import apapl.data.Literal;
 
 public class Prolog2Java {
 	
	//public DistributedOOPL oopl; // norm interpreter
 	public static String TYPE_STATUS="status", TYPE_PROHIBITION="prohibition", 
 		TYPE_OBLIGATION="obligation", TYPE_READINGREQ = "readingRequest",TYPE_READING = "reading",TYPE_INVESTIGATE = "investigate",TYPE_CARGO = "cargo",TYPE_COIN = "coin",TYPE_POINTS = "points",
 			TYPE_OBJECT="object", TYPE_INVENTORY="inventory", NULL="null"; // for matching string with class type
 	public int[] ar_true, ar_null, ar_state_change, ar_false; // precalculated IntProlog data 
 	public int INT_TUPLE=0, INT_POINT=0, INT_NULL=0;
 	
 	public Entry parseTerm(int[] call, DistributedOOPL oopl) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SecurityException, NoSuchMethodException {
 		int type = call[4];
		//System.out.println("!!!!! from org !!!!!!");
 		
 		String tuple = oopl.prolog.strStorage.getString(call[4]);
 		//System.out.println(tuple);
 		int l = call.length;
 		//if (!tuple.startsWith("position")) return null;
 		//System.out.println(l);
 		
 		String[] params = new String[10];
 		int x = 0;
 		for (int i = 7; i < l-3; i = i + 3 ) {
 			//System.out.println(i);
 			if(call[i]!=INT_NULL) {
 				//System.out.println(oopl.prolog.strStorage.getString(call[i]));
 				
 				if (oopl.prolog.strStorage.getString(call[i]).startsWith("null"))
 					params[x] = null;
 				else
 					params[x] = oopl.prolog.strStorage.getString(call[i]);
 			}
 			else
 				params[x] = null;
 			x++;
 		}
 		
 		//System.out.println(params.toString());
 		Entry e;
 		String output = tuple.substring(0, 1).toUpperCase() + tuple.substring(1);
 		Class<?> clazz = Class.forName("tuplespace."+ output);
 		Constructor<?> ctor = clazz.getConstructor(String[].class);
 		Object object = ctor.newInstance(new Object[] { params });
 		
 		return (Entry) object;
 		/*
 		if (tuple.startsWith("position")) {
 			//System.out.println("org reads position");
 			String name = null;
 			if(call[7]!=INT_NULL) name = oopl.prolog.strStorage.getString(call[7]);
 			Cell c = null;
 			if(call[10]!=INT_NULL) c = new Cell(get_number(call,13),get_number(call,16)); 
 			//c = new Cell(3,3); 
 			//System.out.println(c.toString());
 
 			return new Position(name,c);
 		}
 		else if (tuple.startsWith("cargo")) {
 			//System.out.println("org reads cargo");
 			return new Cargo();
 		}
 		else if (tuple.startsWith("coin")) {
 			//System.out.println("org reads coin");
 			
 		
 			return new Coin();
 		}
 		else if (tuple.startsWith("reading")) {
 			//System.out.println("org reads reading");
 			
 			return new Reading();
 		}
 		else if (tuple.startsWith("investigate")) {
 			//System.out.println("org reads investigate");
 		
 			return new Investigate();
 		}
 		else if(tuple == ""){
 			
 		}
 		
 /*		for (int i = 0;  i<call.length; i++){
 			
 			String recipient = oopl.prolog.strStorage.getString(call[i]);
 			System.out.println("create entry test "+ i + recipient);
 		}
 		else if(type == INT_TUPLE){ // I have to make this more accessable 
 			String name = null;
 			if(call[7]!=INT_NULL) name = oopl.prolog.strStorage.getString(call[7]);
 			Point p = null;
 
 			if(call[10]!=INT_NULL) p = new Point(get_number(call,13),get_number(call,16)); 
 
 			Integer i = null;
 			int c = oopl.prolog.harvester.scanElement(call, 9, false, false);
 			if(call[c+1]!=INT_NULL) i = get_number(call,c+1);
 			return new Tuple(name,p,i);
 		}
 
 		return null;*/
 	}
 	/*
 	 * Gets the int value of a number out of an integer array.
 	 * Note that normally this is a double.
 	 */
 	public int get_number(int[] call, int cursor){
 		long l1 = ((long)call[cursor]<<32)>>>32;
 		long l2 = ((long)call[cursor+1]<<32)>>>32;
 		return (int)Double.longBitsToDouble((l1<<32)|l2);
 	} 
 
 	/*
 	 * Convert a regular integer to a Prolog store value. Each number is encoded with 
 	 * two integers (64 bit double format), so you can use getInt(i,true) and getInt(i,false) 
 	 * to get both i's number representation parts.
 	 */
 	public int getInt(int i, boolean a){
 		long l = Double.doubleToLongBits(i);
 		if(a) return (int)((l>>>32));
 		else return (int)((l<<32)>>>32);
 	}
 }
