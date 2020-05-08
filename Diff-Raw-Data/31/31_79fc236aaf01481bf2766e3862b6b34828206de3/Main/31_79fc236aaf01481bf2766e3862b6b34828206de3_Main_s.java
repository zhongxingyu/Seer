 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Stack;
 
 public class Main {
 
 	public static Map<String, LinkedList<ValueObject>> db = new HashMap<String, LinkedList<ValueObject>>();
 
 	public static Map<Integer, Integer> valMap = new HashMap<Integer, Integer>();
 
 	public static Map<Long, HashSet<String>> transactionMap = new HashMap<Long, HashSet<String>>();
 
 	public enum Command {
		SET, UNSET, GET, NUMEQUALTO, BEGIN, ROLLBACK, COMMIT
 	}
 
 	public class TransVariables {
 		Long tid;
 		HashSet<String> varSet;
 
 		public TransVariables() {
 		}
 
 		public TransVariables(Long tid, HashSet<String> varSet) {
 			this.tid = tid;
 			this.varSet = varSet;
 		}
 		
 		public String toString(){
 			return this.tid +" -> "+this.varSet;
 		}
 	}
 
 	public static Stack<TransVariables> tStk = new Stack<TransVariables>();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		Scanner sc = new Scanner(System.in);
 		System.out.println("Read input: ");
 		String input = null;
 		Main main = new Main();
 
 		while (!(input = sc.nextLine()).equals("END")) {
 			String[] tokens = input.split("\\s+");
 
 			switch (Command.valueOf(tokens[0].toUpperCase())) {
 			case SET:
 				main.set(tokens[1], Integer.parseInt(tokens[2]));
 				break;
 			case UNSET:
 				main.unset(tokens[1]);
 				break;
 
 			case GET:
 				Integer val = main.get(tokens[1]);
 				if (val == null) {
 					System.out.println("NULL");
 				} else
 					System.out.println(val);
 				break;
 			case NUMEQUALTO:
 				Integer num = main.numequalto(Integer.parseInt(tokens[1]));
 				break;
 			case BEGIN:
 				 main.beginTransaction();
 				
 				break;
 			case ROLLBACK:
 				main.rollback();
 				break;
 			case COMMIT:
 				main.commit();
 				break;
 			default:
 				System.err.println("ERROR in input");
 				return;
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param varName
 	 * @param value
 	 */
	private void set(String varName, int value) {
 		// get current tid
 		
 		if(tStk.isEmpty()){
 			//no transaction has been started but assign a tid anyway
 			this.beginTransaction();
 			
 		}
 		TransVariables transVar = tStk.peek();
 		
 		ValueObject vo = new ValueObject(value, transVar.tid);
 		LinkedList<ValueObject> valList = db.get(varName);
 		if (valList == null) {
 			valList = new LinkedList<ValueObject>();
 			db.put(varName, valList);
 		}
 		// check if the last VO in valList belongs to a different transaciton.
 		// if different transaction, then you can add directly
 		if (valList.isEmpty()) {
 			valList.add(vo);
 			
 		}else{
 		ValueObject voLast = valList.getLast();
 
 		if (voLast.tid == transVar.tid) {
 			valList.removeLast();
 		}
 		valList.add(vo);
 		}
 		// add to valMap to keep track of number of values
 		if (valMap.containsKey(value)) {
 			valMap.put(value, valMap.get(value) + 1);
 		} else {
 			valMap.put(value, 1);
 		}
 
 		// add to tStk
 		tStk.peek().varSet.add(varName);
 
 	}
 
 	/**
 	 * 
 	 * @param var
 	 */
	private void unset(String var) {
 
 		this.set(var, -1);
 		// Integer tid=tStk.peek();
 
 	}
 
	private Integer get(String var) {
 		LinkedList<ValueObject> valList = db.get(var);
 		if (valList == null)
 			return null;
 
 		ValueObject val = valList.getLast();
 		return val.value;
 
 	}
 
	private Integer numequalto(int value) {
 		// just check in valMap how many variables are set to value
 		if (valMap.containsKey(value)) {
 			return valMap.get(value);
 		} else {
 			return 0;
 		}
 
 	}
 
	private void beginTransaction() {
 
 		tStk.push(new TransVariables( System.currentTimeMillis(), new HashSet<String>()));;
 
 	}
 
	private void rollback() {
 		// go thru set of variables that were written or modfied in this
 		// transaction
 		TransVariables transVar = tStk.pop();
 		HashSet<String> varSet = (HashSet<String>) transVar.varSet;
 		for (Iterator iterator = varSet.iterator(); iterator.hasNext();) {
 			String var = (String) iterator.next();
 			// for each var remove last elem in corresponding LinkedList in db
 			ValueObject vo = db.get(var).removeLast();
 			// also adjust valMap accordingly
 			// remove the count of the removed value
 			valMap.put(vo.value, valMap.get(vo.value) - 1);
 
 			// add the count of the current last value
 			valMap.put(db.get(var).getLast().value,
 					valMap.get(db.get(var).getLast().value) + 1);
 
 		}
 
 	}
 
	private void commit() {
 
 		while (!tStk.isEmpty()) {
 			TransVariables transVar = tStk.pop();
 			HashSet<String> varSet = transVar.varSet;
 			for (Iterator iterator = varSet.iterator(); iterator.hasNext();) {
 				String var = (String) iterator.next();
 				ValueObject vo = db.get(var).removeLast();
 				db.get(var).removeAll(db.get(var));
 				db.get(var).add(vo);
 				// newDB.put(var,newDB.get(var).add(vo));
 
 			}
 		}
 
 	}
 
 }
