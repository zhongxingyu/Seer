 package net.sf.ictalive.monitoring.domain;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import clojure.lang.Obj;
 
 import net.sf.ictalive.operetta.OM.Atom;
 import net.sf.ictalive.operetta.OM.Constant;
 import net.sf.ictalive.operetta.OM.Term;
 import net.sf.ictalive.operetta.OM.Variable;
 
 public class Formula
 {
 	private Formula					content;
 	private Set<Value>				grounding;
 	private Obj						logic;
 	
 	public Formula(Obj logic)
 	{
 		this.setLogic(logic);
 	}
 
 	public Object substitute(Set<Value> grounding)
 	{
 		Object				res, value;
 		Atom				a;
 		Proposition			p;
 		Term				t;
 		Iterator<Term>		it;
 		Iterator<Value>		itv;
 		int					i;
 		Constant			c;
 		Variable			v;
 		Value				vl;
 		Map<String,Object>	map;
 		
 		map = new TreeMap<String,Object>();
 		itv = grounding.iterator();
 		while(itv.hasNext())
 		{
 			vl = itv.next();
 			map.put(vl.getKey(), vl.getValue());
 		}
 		
 		res = this;
 		if(logic instanceof Atom)
 		{
 			a = (Atom)logic;
 			p = new Proposition(a.getPredicate());
 			it = a.getArguments().iterator();
 			i = 0;
 			while(it.hasNext())
 			{
 				t = it.next();
 				
 				if(t instanceof Constant)
 				{
 					c = (Constant)t;
 					p.getParams()[i] = c.getName();
 				}
 				else if(t instanceof Variable)
 				{
 					v = (Variable)t;
 					value = map.get(v.getName() + "");
 					if(value != null)
 					{
 						p.getParams()[i] = (String)value; // TODO: Check this cast
 					}
 				}
 				
 				i = i + 1;
 			}
 			
 			res = p;
 		}
 		return res;
 	}
 	
 	public void setContent(Formula content)
 	{
 		this.content = content;
 	}
 	public Formula getContent()
 	{
 		return content;
 	}
 	public void setGrounding(Set<Value> grounding)
 	{
 		this.grounding = grounding;
 	}
 	public Set<Value> getGrounding()
 	{
 		return grounding;
 	}
 
 	public void setLogic(Obj logic)
 	{
 		this.logic = logic;
 	}
 
 	public Obj getLogic()
 	{
 		return logic;
 	}
 }
