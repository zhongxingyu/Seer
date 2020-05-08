 package core;
 import java.util.LinkedList;
 import java.util.List;
 
 import types.PolyType;
 import types.PortKind;
 import types.PortType;
 import types.UnitType;
 
 public class Design {
 	
 	String name;
 	List<Component> components = new LinkedList<Component>();
 	List<Instance> instances = new LinkedList<Instance>();
 	List<Connection> connections = new LinkedList<Connection>();
 	List<String> types = new LinkedList<String>(); 
 	int instance_id = 0;
 	int openPorts = 0;
 	
 	public Design(String name) {
 		this.name = name;
 	}
 	
 	public List<OutPort> matchPort(InPort p) {
 		List<OutPort> list = new LinkedList<OutPort>();
 		for (Instance inst:instances) {
 			if (inst == p.inst)
 				continue;
 			for (OutPort p1 : inst.outports)
 				if (p1.type.equals(p.type))
 					list.add(p1);
 		}
 		return list;
 	}
 	
 	public List<InPort> matchPort(OutPort p) {
 		List<InPort> list = new LinkedList<InPort>();
 		for (Instance inst:instances) {
 			if (inst == p.inst)
 				continue;
 			for (InPort p1 : inst.inports)
 				if (p1.type.equals(p.type))
 					list.add(p1);
 		}
 		return list;
 	}
 	
 	 public Instance makeInstance(Component c, String [] param) {
 		 instance_id++;
 		 if (param == null)
 			 param = c.default_param_values;
 		 Instance inst = new Instance(c,param,""+instance_id);
 		 if (!(components.contains(c))) {
 			components.add(c);
 			for (PortType t : c.outtypes) {
 				if (t instanceof PolyType || t instanceof UnitType)
 					continue;
 				String s = t.type;
 				if (s.equals("Int") || s.equals("Float") || s.equals("String") || s.equals("Char") || s.equals("Bool"))
 					continue;
 				if (! (types.contains(s)))
 					types.add(s);
 			}
 			for (PortType t : c.intypes) {
 				if (t instanceof PolyType || t instanceof UnitType)
 					continue;
 				String s = t.type;
 				if (s.equals("Int") || s.equals("Float") || s.equals("String") || s.equals("Char") || s.equals("Bool"))
 					continue;
 				if (! (types.contains(s)))
 					types.add(s);
 			}
 		 }
 		 instances.add(inst);
 		 openPorts += inst.outports.length;
 		 return inst;
 	}
 	 
 	public void removeInstance(Instance inst) {
 		if (inst == null)
 			return;
 		for(int i=0; i < inst.outports.length; i++)
 			disconnect(inst.outports[i].conn);
 		instances.remove(inst);
 		openPorts -= inst.outports.length;
 		//TODO: remove types if appropriate (optional)
 	}
 	
 	public Connection connect(OutPort source, InPort target) {
 		if (source == null || target == null || source.conn != null)
 			return null;
 		
 		//type checking
 		if (! (source.type.equals(target.type)))
 			return null;
 		//type checking complete
 		
 		//check for loop
 		if (source.type.kind == PortKind.Pull && source.inst == target.inst)
 			return null;
 		
 		Connection c = new Connection(source, target);
 		//TODO: propagate types if necessary
 		source.conn = c;
 		connections.add(c);
 		openPorts--;
 		return c;
 	}
 	
 	public void disconnect(Connection c) {
 		if (c == null || c.source == null || c.target == null || c.source.conn != c)
 			return;
 		connections.remove(c);
 		c.source.conn = null;
 		openPorts++;
 		//TODO: remove propagated types if necessary
 	}
 	
 	public boolean setParameter(Instance inst, int index, String value) {
 		if (inst == null || value == null)
 			return false;
 		return inst.setParameter(index, value);
 	}
 	
 	public boolean isComplete() {
 		return openPorts == 0;
 	}
 	
 	public String compile() {
 		String s = "module "+name+" where\n\n";
 		for (String t : types)
 			s += "import " + s + "\n";
 		for (Component c : components)
 			s += "import " + c.name + "\n";
 		s += "\n";
 		//TODO: order instances (optional)
		s += "make :: Class ()\n"; 
 		s += "make id = class\n";
 		for(Instance i : instances)
 			s += "    " + i.compile();
 		s += "    result ()";
 		return s;
 	}
 	
 }
