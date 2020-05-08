 package cs4120.der34dlc287lg342.xi.cfg;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import cs4120.der34dlc287lg342.xi.assembly.Assembly;
 import cs4120.der34dlc287lg342.xi.assembly.LABEL;
 import cs4120.der34dlc287lg342.xi.ir.Arg;
 import cs4120.der34dlc287lg342.xi.ir.Cjump;
 import cs4120.der34dlc287lg342.xi.ir.Exp;
 import cs4120.der34dlc287lg342.xi.ir.Expr;
 import cs4120.der34dlc287lg342.xi.ir.Jump;
 import cs4120.der34dlc287lg342.xi.ir.LabelNode;
 import cs4120.der34dlc287lg342.xi.ir.Mem;
 import cs4120.der34dlc287lg342.xi.ir.Move;
 import cs4120.der34dlc287lg342.xi.ir.Stmt;
 import cs4120.der34dlc287lg342.xi.ir.Temp;
 import cs4120.der34dlc287lg342.xi.ir.context.Label;
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 
 public class CFG {
 	public ArrayList<CFG> parents;
 	public CFG child1, child2;
 	public static int guid = 0;
 	public int id;
 	
 	HashSet<TempRegister> use, def;
 	HashSet<TempRegister> in_wl;
 	public Assembly asm;
 	public CFG(Assembly asm){
 		parents = new ArrayList<CFG>();
 		child1 = null;
 		child2 = null;
 		in_wl = new HashSet<TempRegister>();
 		
 		this.use = asm.use();//use(asm);
 		this.def = asm.def();//def(asm);
 		this.asm = asm;
 		id = guid++;
 	}
 	
 	public ArrayList<CFG> succ(){
 		ArrayList<CFG> succ = new ArrayList<CFG>();
 		if (child1 != null)
 			succ.add(child1);
 		if (child2 != null)
 			 succ.add(child2);
 		return succ;
 	}
 	
 	public ArrayList<CFG> pred(){
 		return parents;
 	}
 	
 	public static HashSet<TempRegister> def(Stmt ir){
 		HashSet<TempRegister> in_def = new HashSet<TempRegister>();
 		
 		if (ir instanceof Move){
 			Move mov = (Move)ir;
 			if (mov.dest instanceof Temp){
 				in_def.add(((Temp)mov.dest).temp);
 			}
 		} else if (ir instanceof Arg){
 			Arg arg = (Arg)ir;
 			in_def.add(arg.r);
 		}
 		
 		return in_def;
 	}
 	
 	public static HashSet<TempRegister> use(Stmt ir){
 		HashSet<TempRegister> in_use = new HashSet<TempRegister>();
 		
 		if (ir instanceof Move){
 			Move mov = (Move)ir;
 			if (mov.dest instanceof Mem){
 				Expr e1 = ((Mem)mov.dest).expr;
 				Expr e2 = mov.val;
 				in_use.addAll(use(e1));
 				in_use.addAll(use(e2));
 			} else {
 				in_use.addAll(use(mov.val));
 			}
 		} else if (ir instanceof Cjump){
 			Cjump cjump = (Cjump)ir;
 			in_use.addAll(use(cjump.condition));
 		} else if (ir instanceof Exp){
 			Exp exp = (Exp)ir;
 			in_use.addAll(use(exp.expr));
 		}
 		
 		return in_use;
 	}
 	
 	public static HashSet<TempRegister> use(Expr ir){
 		HashSet<TempRegister> in_use = new HashSet<TempRegister>();
 		
 		for (Field f : ir.getClass().getDeclaredFields()){
 			try{
 				f.setAccessible(true);
 				Object o = f.get(ir);
 				if (o instanceof TempRegister){
 					in_use.add((TempRegister) o);
 				}
 			} catch (Exception e){}
 		}
 		
 		for (VisualizableTreeNode e : ir.children()){
 			in_use.addAll(use((Expr)e));
 		}
 		
 		return in_use;
 	}
 	
 	public String simpleName(CFG node){
 		if (node == null)
 			return "";
 		else if (node.asm instanceof LABEL){
 			LABEL label = (LABEL)node.asm;
 			return ""+label.label;
 		}
 		return node.asm.getClass().getSimpleName();
 	}
 	
 	public String toString_(HashSet<CFG> seen){
 		if (seen.contains(this))
 			return "";
 		else
 			seen.add(this);
 		
 		String prev = "";
 		for (CFG parent : parents){
 			prev += parent.asm.getClass().getSimpleName()+", ";
 		}
 		String next = simpleName(child1) + ", "+simpleName(child2);
 		
 		String str = "["+asm.assem+"]\n\tparents: "+prev+"\n\tchildren: "+next+"\n\tuses: "+use+"\n\tdefs: "+def+"\n\tin_wl: " + in_wl +"\n";
 		
 		return str+(child1 == null ? "":child1.toString_(seen))+(child2 == null ? "" :child2.toString_(seen));
 	}
 	
 	public String toString(){
 		return toString_(new HashSet<CFG>());
 	}
 	
 	public String asm_(HashSet<CFG> seen, Hashtable<TempRegister, Integer> coloring){
 		if (seen.contains(this))
 			return "";
 		else
 			seen.add(this);
 		String str = "";
 		str += asm.simple_assem(coloring) + "\n";
 		return str+(child2 == null ? "":child2.asm_(seen, coloring))+(child1 == null ? "" :child1.asm_(seen, coloring));
 	}
 	
 	public String asm(Hashtable<TempRegister, Integer> coloring){
 		return asm_(new HashSet<CFG>(), coloring);
 	}
 	
 	public String asm(){
 		return asm(new Hashtable<TempRegister, Integer>());
 	}
 	
 	public String dot_edge(HashSet<CFG> seen, Hashtable<TempRegister, Integer> coloring){
 		if (seen.contains(this))
 			return "";
 		else
 			seen.add(this);
 		String str = "";
 		if (pred().isEmpty()){
 			str += "\tstart -> n"+id+"\n";
 		}
 		str += "\t"+"n"+id+" [label=\""+asm.simple_assem(coloring)+"\\nuse: "+use+"\\ndef: "+def+"\\nin_wl: "+in_wl+"\"]\n";
 
 		for (CFG child : succ()){
 			str += "\t"+"n"+id+" -> "+"n"+child.id+"\n";
 		}
 		
 		if (succ().isEmpty()){
 			str += "\tn"+id+" -> return\n";
 		}
 
 		return str+(child1 == null ? "":child1.dot_edge(seen, coloring))+(child2 == null ? "" :child2.dot_edge(seen, coloring));
 	}
 	
 	public String dot_edge(HashSet<CFG> seen){
 		return dot_edge(seen, new Hashtable<TempRegister, Integer>());
 	}
 	
 	public static CFG cfg_first_pass(ArrayList<Assembly> children, HashMap<Label, CFG> jumps){
 		CFG prev = null, current = null;
 		CFG root = null;
 		
 		//HashMap<Label, CFG> jumps = new HashMap<Label, CFG>();
 		
 		for (Assembly stmt : children){
 			if (root == null){
 				root = new CFG(stmt);
 				current = root;
 			} else {
 				current = new CFG(stmt);
 			}
 			
 			if (prev != null){
 				current.parents.add(prev);
 				prev.child1 = current;
 			}
 			
 //			if (stmt instanceof Jump){
 //				jumps.put(((Jump)stmt).label, current);
 //			} else if (stmt instanceof Cjump){
 //				jumps.put(((Cjump)stmt).to, current);
 //			}
 			if (stmt instanceof LABEL){
 				jumps.put(((LABEL)stmt).label, current);
 			}
 
 			prev = current;
 		}
 		
 		return root;
 	}
 	
 	public static CFG traverse(CFG node, HashMap<Label, CFG> jumps, HashSet<CFG> memoize){
 		if (node == null){
 			return null;
 		}
 		
 		// dynamic memoization
 		if (memoize.contains(node))
 			return node;
 		else
 			memoize.add(node);
 		
 		if (node.asm.is_jump()) {
 			Label to = node.asm.jump();
 			CFG next = jumps.get(to);
 			
 			// remove child1.parent that is equal to current
 			node.child1.parents.remove(node);
 			
 			// make child1 next
 			next.parents.add(node);
 			node.child1 = traverse(next, jumps, memoize);
 		} else if (node.asm.is_cjump()) {
 			Label to = node.asm.jump();
 			CFG next = jumps.get(to);
 			
 			// make child2 next
			if (next != null)
				next.parents.add(node);
				node.child2 = traverse(next, jumps, memoize);
				
 			// update child1 as well
 			traverse(node.child1, jumps, memoize);
 		} else {
 			traverse(node.child1, jumps, memoize);
 		}
 		
 		return node;
 	}
 	
 	public static CFG cfg(ArrayList<Assembly> instrs){
 		HashMap<Label, CFG> jumps = new HashMap<Label, CFG>();
 		HashSet<CFG> memoize = new HashSet<CFG>();
 		CFG first_pass = cfg_first_pass(instrs, jumps);
 		
 		// single DPS to traverse and alter the connection of the graphs
 		CFG second_pass = traverse(first_pass, jumps, memoize);
 		
 		return second_pass;
 	}
 
 
 }
