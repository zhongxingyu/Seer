 import java.util.ArrayList;
 import java.util.List;
 
 // CLASS CLSINTF pk=kindcontext pt=typecontext {$c = new Cls($CLSINTF.text, $pk.kc, $pt.tc);} 
 // (EXTENDS t=type {$c.add($t.t);} LBRACE (s=stat {$c.add($s.s);})* (SUPER LPAREN ex=exprs RPAREN SEMICOLON {$c.add($ex.cu);})? (FUN VAR ts=typescheme s=stat {$c.add($VAR.text, $ts.ts, $s.s);})* RBRACE)?;
 public class Cls extends CuClass {
 	String clsintf;
 	List<String> kc;
 	List<CuVvt> tc;
 	String type;
 	List<CuStat> ss = new ArrayList<CuStat>();
 	List<CuExpr> es = new ArrayList<CuExpr>();
 	List<String> fun = new ArrayList<String>();
 	
 	public Cls(String clsintf, List<String> kc, List<CuVvt> tc) {
 		this.clsintf = clsintf;
 		this.kc = kc;
 		this.tc = tc;
 	}
	@Override public void add (CuType t) {
 		type = t.toString();
 	}
 	
	@Override public void add (CuStat s) {
 		ss.add(s);
 	}
 	
	@Override public void add (CuExpr s) {
 		es.add(s);
 	}
 	
	@Override public void add(String v, CuTypeScheme ts, CuStat s) {
 		String t = String.format("fun %s %s %s", v, ts.toString(), s.toString());
 		fun.add(t);
 	}
 	
 	@Override public String toString() {
 		return String.format("class %s %s %s extends %s { %s super ( %s ) ; %s }", 
 				clsintf, listKindContext(kc), listTypeContext(tc), type, listStat(ss), listExpr(es), listFun(fun));
 	}
 	
 	private String listTypeContext(List<CuVvt> es) {
 		String s = "( ";
 		for (CuVvt e : es) {
 			s += e.toString() + " , ";
 		}
 		int l = s.lastIndexOf(", ");
 		if (l > 0) s = s.substring(0, l);
 		s += ")";
 		return s;
 	}
 	
 	private String listExpr(List<CuExpr> es) {
 		String s = "( ";
 		for (CuExpr e : es) {
 			s += e.toString() + " , ";
 		}
 		int l = s.lastIndexOf(", ");
 		if (l > 0) s = s.substring(0, l);
 		s += ")";
 		return s;
 	}
 	
 	private String listStat(List<CuStat> es) {
 		String s = "";
 		for (CuStat e : es) {
 			s += e.toString() + " ";
 		}
 		return s;
 	}
 	
 	private String listFun(List<String> es) {
 		String s = "";
 		for (String e : es) {
 			s += e.toString() + " ";
 		}
 		return s;
 	}
 	
 	private String listKindContext(List<String> es) {
 		String s = "< ";
 		for (String e : es) {
 			s += e.toString() + " , ";
 		}
 		int l = s.lastIndexOf(", ");
 		if (l > 0) s = s.substring(0, l);
 		s += ">";
 		return s;
 	}
 }
