 import java.util.ArrayList;
 import java.util.List;
 
 //{$c = new ClassCtxt();} (COMMA k=(INTERFACE | CLASS) CLSINTF p=kindcontext {$c.add($k.text, $CLSINTF.text, $p.kc);}  (EXTENDS t=type {$c.add($t.t);} LBRACE (VAR ts=typescheme SEMICOLON {$c.add($VAR.text, $ts.ts);})* RBRACE)? {$c.finish($k.text);})*;
 
 public class ClassCtxt extends CuClassC {
 	String clsintf, keyword;
 	List<Cls> clsCtxt = new ArrayList<Cls>();
 	Cls curCls;
 	List<Intf> intfCtxt = new ArrayList<Intf>();
 	Intf curIntf;
 	List<String> kc;
 	CuType t;
 	List<String> VvTypeScheme = new ArrayList<String>(); // temporal
 	public ClassCtxt() {}
 	
 	@Override public void add(String k, String ci, List<String> kc) {
 		if (k=="interface") {
 			//to be done
 		}
 		else {
 			curCls = new Cls(ci, kc, null);
 		}
 		this.keyword = k;
 		this.clsintf = ci;
 		this.kc = kc;
 		text += String.format(" , %s %s %s extends", keyword, clsintf, CuMethod.printList("<", kc, ">", ","));
 	}
 	
 	@Override public void add (CuType t) {
 		curCls.add(t);
 		this.t = t;
 		text += " " +t.toString();
 	}
 	
 	@Override public void add (CuVvc vv, CuTypeScheme ts) {
 		curCls.add(vv, ts, null);
 		String s = String.format("%s %s ;", vv.toString(), ts.toString());
 		VvTypeScheme.add(s);
 		text +=  " " + CuMethod.printList("{", VvTypeScheme, "}", ";");
 	}
 	
 	//add the current object
	public void finish (String id) {
 		if (id == "interface") {
 			intfCtxt.add(curIntf);
 		}
		else if (id == "class") {
 			clsCtxt.add(curCls);
 		}
		
 	}
 	
 }
