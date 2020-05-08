 package com.silentmatt.dss.parser;
 
import com.silentmatt.dss.bool.NotExpression;
 import java.util.*;
 import com.silentmatt.dss.*;
 import com.silentmatt.dss.directive.*;
 import com.silentmatt.dss.term.*;
 import com.silentmatt.dss.bool.*;
 import com.silentmatt.dss.calc.*;
 
 public class Parser {
 	public static final int _EOF = 0;
 	public static final int _ident = 1;
 	public static final int _integer = 2;
 	public static final int _decimal = 3;
 	public static final int _s = 4;
 	public static final int _stringLit = 5;
 	public static final int _url = 6;
 	public static final int maxT = 68;
 
 	static final boolean T = true;
 	static final boolean x = false;
 	static final int minErrDist = 2;
 
 	Token t;    // last recognized token
 	Token la;   // lookahead token
 	int errDist = minErrDist;
 	
 	public Scanner scanner;
 	public ErrorReporter errors;
 
 	public CSSDocument CSSDoc;
 
         boolean partOfHex(String value) {
             if (value.length() == 7) { return false; }
             if (value.length() + la.val.length() > 7) { return false; }
             List<String> hexes = Arrays.asList(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "a", "b", "c", "d", "e", "f" });
             for (int i = 0; i < la.val.length(); i++) {
                 char c = la.val.charAt(i);
                 if (!hexes.contains(String.valueOf(c))) {
                     return false;
                 }
             }
             return true;
         }
         boolean isUnit() {
             if (la.kind != 1) { return false; }
             List<String> units = Arrays.asList(new String[] { "em", "ex", "px", "gd", "rem", "vw", "vh", "vm", "ch", "mm", "cm", "in", "pt", "pc", "deg", "grad", "rad", "turn", "ms", "s", "hz", "khz" });
             return units.contains(la.val);
         }
 
 /*------------------------------------------------------------------------*
  *----- SCANNER DESCRIPTION ----------------------------------------------*
  *------------------------------------------------------------------------*/
 
 
 
 	public Parser(Scanner scanner) {
 		this.scanner = scanner;
 		errors = new ListErrorReporter();
 	}
 
 	void SynErr(int n) {
 		if (errDist >= minErrDist) {
             errors.SynErr(la.line, la.col, n);
         }
 		errDist = 0;
 	}
 
 	public void SemErr(String msg) {
 		if (errDist >= minErrDist) {
             errors.SemErr(t.line, t.col, msg);
         }
 		errDist = 0;
 	}
 	
 	void Get () {
 		for (;;) {
 			t = la;
 			la = scanner.Scan();
 			if (la.kind <= maxT) {
 				++errDist;
 				break;
 			}
 
 			la = t;
 		}
 	}
 	
 	void Expect (int n) {
 		if (la.kind==n) {
             Get();
         }
         else {
             SynErr(n);
         }
 	}
 	
 	boolean StartOf (int s) {
 		return set[s][la.kind];
 	}
 	
 	void ExpectWeak (int n, int follow) {
 		if (la.kind == n) {
             Get();
         }
 		else {
 			SynErr(n);
 			while (!StartOf(follow)) {
                 Get();
             }
 		}
 	}
 	
 	boolean WeakSeparator (int n, int syFol, int repFol) {
 		int kind = la.kind;
 		if (kind == n) { Get(); return true; }
 		else if (StartOf(repFol)) { return false; }
 		else {
 			SynErr(n);
 			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
 				Get();
 				kind = la.kind;
 			}
 			return StartOf(syFol);
 		}
 	}
 	
 	void CSS3() {
 		CSSDoc = new CSSDocument(); 
 		while (la.kind == 4 || la.kind == 7 || la.kind == 8) {
 			if (la.kind == 7) {
 				Get();
 			} else if (la.kind == 8) {
 				Get();
 			} else {
 				Get();
 			}
 		}
 		while (StartOf(1)) {
 			if (StartOf(2)) {
 				RuleSet rset = ruleset();
 				CSSDoc.addRule(rset); 
 			} else {
 				Rule dir = directive();
 				CSSDoc.addRule(dir); 
 			}
 			while (la.kind == 4 || la.kind == 7 || la.kind == 8) {
 				if (la.kind == 7) {
 					Get();
 				} else if (la.kind == 8) {
 					Get();
 				} else {
 					Get();
 				}
 			}
 		}
 	}
 
 	RuleSet  ruleset() {
 		RuleSet  rset;
 		rset = new RuleSet();
 		Selector sel;
 		Declaration dec;
 		Rule dir;
 		
 		sel = selector();
 		rset.getSelectors().add(sel); 
 		while (la.kind == 40) {
 			Get();
 			sel = selector();
 			rset.getSelectors().add(sel); 
 		}
 		Expect(22);
 		while (StartOf(3)) {
 			if (StartOf(4)) {
 				dec = declaration();
 				Expect(29);
 				rset.addDeclaration(dec); 
 			} else if (la.kind == 27) {
 				dir = classDirective();
 				rset.addRule(dir); 
 			} else {
 				dir = defineDirective();
 				rset.addRule(dir); 
 			}
 		}
 		Expect(23);
 		return rset;
 	}
 
 	Rule  directive() {
 		Rule  dir;
 		dir = null; 
 		switch (la.kind) {
 		case 24: {
 			dir = ifDirective();
 			break;
 		}
 		case 21: {
 			dir = mediaDirective();
 			break;
 		}
 		case 27: {
 			dir = classDirective();
 			break;
 		}
 		case 31: {
 			dir = defineDirective();
 			break;
 		}
 		case 33: {
 			dir = fontFaceDirective();
 			break;
 		}
 		case 35: {
 			dir = importDirective();
 			break;
 		}
 		case 36: {
 			dir = includeDirective();
 			break;
 		}
 		case 37: {
 			dir = charsetDirective();
 			break;
 		}
 		case 34: {
 			dir = pageDirective();
 			break;
 		}
 		case 38: {
 			dir = namespaceDirective();
 			break;
 		}
 		case 39: {
 			dir = genericDirective();
 			break;
 		}
 		default: SynErr(69); break;
 		}
 		return dir;
 	}
 
 	String  QuotedString() {
 		String  qs;
 		Expect(5);
 		qs = t.val; 
 		return qs;
 	}
 
 	String  URI() {
 		String  url;
 		Expect(6);
 		url = t.val.substring(4, t.val.length() - 1); 
 		return url;
 	}
 
 	Medium  medium() {
 		Medium  m;
 		m = Medium.all; 
 		switch (la.kind) {
 		case 9: {
 			Get();
 			m = Medium.all; 
 			break;
 		}
 		case 10: {
 			Get();
 			m = Medium.aural; 
 			break;
 		}
 		case 11: {
 			Get();
 			m = Medium.braille; 
 			break;
 		}
 		case 12: {
 			Get();
 			m = Medium.embossed; 
 			break;
 		}
 		case 13: {
 			Get();
 			m = Medium.handheld; 
 			break;
 		}
 		case 14: {
 			Get();
 			m = Medium.print; 
 			break;
 		}
 		case 15: {
 			Get();
 			m = Medium.projection; 
 			break;
 		}
 		case 16: {
 			Get();
 			m = Medium.screen; 
 			break;
 		}
 		case 17: {
 			Get();
 			m = Medium.tty; 
 			break;
 		}
 		case 18: {
 			Get();
 			m = Medium.tv; 
 			break;
 		}
 		default: SynErr(70); break;
 		}
 		return m;
 	}
 
 	String  identity() {
 		String  ident;
 		switch (la.kind) {
 		case 1: {
 			Get();
 			break;
 		}
 		case 19: {
 			Get();
 			break;
 		}
 		case 20: {
 			Get();
 			break;
 		}
 		case 9: {
 			Get();
 			break;
 		}
 		case 10: {
 			Get();
 			break;
 		}
 		case 11: {
 			Get();
 			break;
 		}
 		case 12: {
 			Get();
 			break;
 		}
 		case 13: {
 			Get();
 			break;
 		}
 		case 14: {
 			Get();
 			break;
 		}
 		case 15: {
 			Get();
 			break;
 		}
 		case 16: {
 			Get();
 			break;
 		}
 		case 17: {
 			Get();
 			break;
 		}
 		case 18: {
 			Get();
 			break;
 		}
 		default: SynErr(71); break;
 		}
 		ident = t.val; 
 		return ident;
 	}
 
 	MediaDirective  mediaDirective() {
 		MediaDirective  mdir;
 		List<Medium> media = new ArrayList<Medium>();
 		List<Rule> rules = new ArrayList<Rule>();
 		
 		Expect(21);
 		Medium m = medium();
 		media.add(m); 
 		Expect(22);
 		while (StartOf(5)) {
 			if (StartOf(2)) {
 				RuleSet rset = ruleset();
 				rules.add(rset); 
 			} else if (la.kind == 27) {
 				ClassDirective cdir = classDirective();
 				rules.add(cdir); 
 			} else if (la.kind == 31) {
 				DefineDirective ddir = defineDirective();
 				rules.add(ddir); 
 			} else {
 				IncludeDirective idir = includeDirective();
 				rules.add(idir); 
 			}
 		}
 		Expect(23);
 		mdir = new MediaDirective(media, rules); 
 		return mdir;
 	}
 
 	ClassDirective  classDirective() {
 		ClassDirective  dir;
 		String ident;
 		List<Declaration> parameters = new ArrayList<Declaration>();
 		List<Declaration> declarations = new ArrayList<Declaration>();
 		Declaration param;
 		
 		Expect(27);
 		ident = identity();
 		if (la.kind == 28) {
 			Get();
 			if (StartOf(4)) {
 				param = parameter();
 				parameters.add(param); 
 				while (la.kind == 29) {
 					Get();
 					param = parameter();
 					parameters.add(param); 
 				}
 			}
 			Expect(30);
 		}
 		Expect(22);
 		while (StartOf(4)) {
 			Declaration dec = declaration();
 			Expect(29);
 			declarations.add(dec); 
 		}
 		Expect(23);
 		dir = new ClassDirective(ident, parameters, declarations); 
 		return dir;
 	}
 
 	DefineDirective  defineDirective() {
 		DefineDirective  dir;
 		List<Declaration> declarations = new ArrayList<Declaration>();
 		boolean global = false;
 		
 		Expect(31);
 		if (la.kind == 32) {
 			Get();
 			global = true; 
 		}
 		Expect(22);
 		while (StartOf(4)) {
 			Declaration dec = declaration();
 			Expect(29);
 			declarations.add(dec); 
 		}
 		Expect(23);
 		dir = new DefineDirective(declarations, global); 
 		return dir;
 	}
 
 	IncludeDirective  includeDirective() {
 		IncludeDirective  dir;
 		Expect(36);
 		String url = URI();
 		dir = new IncludeDirective(new UrlTerm(url)); 
 		Expect(29);
 		return dir;
 	}
 
 	IfDirective  ifDirective() {
 		IfDirective  idir;
 		BooleanExpression expr;
 		List<Rule> ifrules = new ArrayList<Rule>();
 		List<Rule> elserules = null;
 		
 		Expect(24);
 		expr = booleanExpression();
 		Expect(22);
 		while (StartOf(1)) {
 			if (StartOf(2)) {
 				RuleSet rset = ruleset();
 				ifrules.add(rset); 
 			} else {
 				Rule dir = directive();
 				ifrules.add(dir); 
 			}
 		}
 		Expect(23);
 		if (la.kind == 25) {
 			Get();
 			elserules = new ArrayList<Rule>(); 
 			Expect(22);
 			while (StartOf(1)) {
 				if (StartOf(2)) {
 					RuleSet rset = ruleset();
 					elserules.add(rset); 
 				} else {
 					Rule dir = directive();
 					elserules.add(dir); 
 				}
 			}
 			Expect(23);
 		}
 		idir = new IfDirective(expr, ifrules, elserules); 
 		return idir;
 	}
 
 	BooleanExpression  booleanExpression() {
 		BooleanExpression  expr;
 		expr = orExpression();
 		return expr;
 	}
 
 	Declaration  parameter() {
 		Declaration  dec;
 		dec = new Declaration(); 
 		String ident = identity();
 		dec.setName(ident); 
 		if (la.kind == 26) {
 			Get();
 			Expression exp = expr();
 			dec.setExpression(exp); 
 		}
 		return dec;
 	}
 
 	Expression  expr() {
 		Expression  exp;
 		exp = new Expression();
 		Character sep = null;
 		Term trm;
 		
 		trm = term();
 		exp.getTerms().add(trm); 
 		while (StartOf(6)) {
 			if (la.kind == 40 || la.kind == 58) {
 				if (la.kind == 58) {
 					Get();
 					sep = '/'; 
 				} else {
 					Get();
 					sep = ','; 
 				}
 			}
 			trm = term();
 			if (sep != null) { trm.setSeperator(sep); }
 			exp.getTerms().add(trm);
 			sep = null;
 			
 		}
 		return exp;
 	}
 
 	Declaration  declaration() {
 		Declaration  dec;
 		dec = new Declaration(); 
 		String ident = identity();
 		dec.setName(ident); 
 		Expect(26);
 		Expression exp = expr();
 		dec.setExpression(exp); 
 		if (la.kind == 56) {
 			Get();
 			Expect(57);
 			dec.setImportant(true); 
 		}
 		return dec;
 	}
 
 	FontFaceDirective  fontFaceDirective() {
 		FontFaceDirective  dir;
 		List<Declaration> declarations = new ArrayList<Declaration>(); 
 		Expect(33);
 		Expect(22);
 		while (StartOf(4)) {
 			Declaration dec = declaration();
 			Expect(29);
 			declarations.add(dec); 
 		}
 		Expect(23);
 		dir = new FontFaceDirective(declarations); 
 		return dir;
 	}
 
 	PageDirective  pageDirective() {
 		PageDirective  dir;
 		List<Declaration> declarations = new ArrayList<Declaration>();
 		SimpleSelector ss = null;
 		
 		Expect(34);
 		if (la.kind == 26) {
 			String psd = pseudo();
 			ss = new SimpleSelector();
 			ss.setPseudo(psd);
 			
 		}
 		Expect(22);
 		while (StartOf(4)) {
 			Declaration dec = declaration();
 			Expect(29);
 			declarations.add(dec); 
 		}
 		Expect(23);
 		dir = new PageDirective(ss, declarations); 
 		return dir;
 	}
 
 	String  pseudo() {
 		String  pseudo;
 		StringBuilder sb = new StringBuilder(); 
 		Expect(26);
 		if (la.kind == 26) {
 			Get();
 		}
 		String ident = identity();
 		sb.append(ident); 
 		if (la.kind == 54) {
 			Get();
 			Expression exp = expr();
 			sb.append("(").append(exp).append(")"); 
 			Expect(55);
 		}
 		pseudo = sb.toString(); 
 		return pseudo;
 	}
 
 	ImportDirective  importDirective() {
 		ImportDirective  dir;
 		Medium m = Medium.all;
 		UrlTerm trm;
 		
 		Expect(35);
 		String url = URI();
 		trm = new UrlTerm(url); 
 		if (StartOf(7)) {
 			m = medium();
 		}
 		Expect(29);
 		dir = new ImportDirective(trm, m); 
 		return dir;
 	}
 
 	CharsetDirective  charsetDirective() {
 		CharsetDirective  dir;
 		Expect(37);
 		Term trm = term();
 		dir = new CharsetDirective(trm); 
 		Expect(29);
 		return dir;
 	}
 
 	Term  term() {
 		Term  trm;
 		String val = "";
 		Expression exp;
 		String ident;
 		CalcExpression expression;
 		trm = null;
 		
 		switch (la.kind) {
 		case 5: {
 			val = QuotedString();
 			trm = new StringTerm(val); 
 			break;
 		}
 		case 6: {
 			val = URI();
 			trm = new UrlTerm(val); 
 			break;
 		}
 		case 64: {
 			Get();
 			Expect(54);
 			ident = identity();
 			Expect(55);
 			trm = new ConstTerm(ident); 
 			break;
 		}
 		case 65: {
 			Get();
 			Expect(54);
 			ident = identity();
 			Expect(55);
 			trm = new ParamTerm(ident); 
 			break;
 		}
 		case 66: {
 			Get();
 			ident = identity();
 			trm = new UnicodeTerm("U\\" + ident); 
 			break;
 		}
 		case 44: {
 			val = HexValue();
 			trm = new HexTerm(val); 
 			break;
 		}
 		case 63: {
 			expression = calculation();
 			trm = new CalculationTerm(expression); 
 			break;
 		}
 		case 1: case 9: case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19: case 20: {
 			ident = identity();
 			trm = new StringTerm(ident); 
 			while (la.kind == 26 || la.kind == 45 || la.kind == 47) {
 				if (la.kind == 26) {
 					Get();
 					((StringTerm) trm).setValue(trm.toString() + t.val); 
 					if (la.kind == 26) {
 						Get();
 						((StringTerm) trm).setValue(trm.toString() + t.val); 
 					}
 					ident = identity();
 					((StringTerm) trm).setValue(trm.toString() + ident); 
 				} else if (la.kind == 45) {
 					Get();
 					((StringTerm) trm).setValue(trm.toString() + t.val); 
 					ident = identity();
 					((StringTerm) trm).setValue(trm.toString() + ident); 
 				} else {
 					Get();
 					((StringTerm) trm).setValue(trm.toString() + t.val); 
 					if (StartOf(4)) {
 						ident = identity();
 						((StringTerm) trm).setValue(trm.toString() + ident); 
 					} else if (la.kind == 2) {
 						Get();
 						((StringTerm) trm).setValue(trm.toString() + t.val); 
 					} else SynErr(72);
 				}
 			}
 			if (la.kind == 28 || la.kind == 54) {
 				if (la.kind == 54) {
 					Get();
 					exp = expr();
 					FunctionTerm func = new FunctionTerm();
 					func.setName(trm.toString());
 					func.setExpression(exp);
 					trm = func;
 					
 					Expect(55);
 				} else {
 					Get();
 					ClassReferenceTerm cls = new ClassReferenceTerm(trm.toString());
 					Declaration dec;
 					trm = cls;
 					
 					if (StartOf(4)) {
 						dec = declaration();
 						cls.addArgument(dec); 
 						while (la.kind == 29) {
 							Get();
 							dec = declaration();
 							cls.addArgument(dec); 
 						}
 					}
 					Expect(30);
 				}
 			}
 			break;
 		}
 		case 2: case 3: case 41: case 62: {
 			if (la.kind == 41 || la.kind == 62) {
 				if (la.kind == 62) {
 					Get();
 					val = "-"; 
 				} else {
 					Get();
 					val = "+"; 
 				}
 			}
 			if (la.kind == 2) {
 				Get();
 			} else if (la.kind == 3) {
 				Get();
 			} else SynErr(73);
 			val += t.val; trm = new NumberTerm(Double.parseDouble(val)); 
 			if (la.val.equalsIgnoreCase("n")) {
 				Expect(19);
 				val += t.val; 
 				if (la.kind == 41 || la.kind == 62) {
 					if (la.kind == 41) {
 						Get();
 						val += '+'; 
 					} else {
 						Get();
 						val += '-'; 
 					}
 					Expect(2);
 					val += t.val; 
 				}
 				trm = new StringTerm(val); val = ""; 
 			} else if (la.kind == 67) {
 				Get();
 				((NumberTerm) trm).setUnit(Unit.Percent); 
 			} else if (StartOf(8)) {
 				if (isUnit()) {
 					ident = identity();
 					try {
 					   // TODO: What if trm isn't a NumberTerm?
 					   ((NumberTerm) trm).setUnit(Unit.parse(ident));
 					} catch (IllegalArgumentException ex) {
 					    errors.SemErr(t.line, t.col, "Unrecognized unit '" + ident + "'");
 					}
 					
 				}
 			} else SynErr(74);
 			if (trm instanceof NumberTerm) {
 			   ((NumberTerm) trm).setValue(Double.parseDouble(val));
 			}
 			else if (trm instanceof StringTerm) {
 			    StringTerm strTrm = (StringTerm) trm;
 			    strTrm.setValue(strTrm.getValue() + val);
 			}
 			
 			break;
 		}
 		default: SynErr(75); break;
 		}
 		return trm;
 	}
 
 	NamespaceDirective  namespaceDirective() {
 		NamespaceDirective  dir;
 		String ident = null;
 		String url = null;
 		
 		Expect(38);
 		if (StartOf(4)) {
 			ident = identity();
 		}
 		if (la.kind == 6) {
 			url = URI();
 		} else if (la.kind == 5) {
 			url = QuotedString();
 		} else SynErr(76);
 		Expect(29);
 		dir = new NamespaceDirective(ident, new UrlTerm(url)); 
 		return dir;
 	}
 
 	GenericDirective  genericDirective() {
 		GenericDirective  dir;
 		Expect(39);
 		String ident = identity();
 		dir = new GenericDirective();
 		dir.setName("@" + ident);
 		
 		if (StartOf(9)) {
 			if (StartOf(9)) {
 				Expression exp = expr();
 				dir.setExpression(exp); 
 			} else {
 				Medium m = medium();
 				dir.addMedium(m); 
 			}
 		}
 		if (la.kind == 22) {
 			Get();
 			while (StartOf(1)) {
 				if (StartOf(4)) {
 					Declaration dec = declaration();
 					Expect(29);
 					dir.addDeclaration(dec); 
 				} else if (StartOf(2)) {
 					RuleSet rset = ruleset();
 					dir.addRule(rset); 
 				} else {
 					Rule dr = directive();
 					dir.addRule(dr); 
 				}
 			}
 			Expect(23);
 		} else if (la.kind == 29) {
 			Get();
 		} else SynErr(77);
 		return dir;
 	}
 
 	Selector  selector() {
 		Selector  sel;
 		sel = new Selector();
 		SimpleSelector ss;
 		Combinator cb = null;
 		
 		ss = simpleselector();
 		sel.getSimpleSelectors().add(ss); 
 		while (StartOf(10)) {
 			if (la.kind == 30 || la.kind == 41 || la.kind == 42) {
 				if (la.kind == 41) {
 					Get();
 					cb = Combinator.PrecededImmediatelyBy; 
 				} else if (la.kind == 30) {
 					Get();
 					cb = Combinator.ChildOf; 
 				} else {
 					Get();
 					cb = Combinator.PrecededBy; 
 				}
 			}
 			ss = simpleselector();
 			if (cb != null) { ss.setCombinator(cb); }
 			sel.getSimpleSelectors().add(ss);
 			cb = null;
 			
 		}
 		return sel;
 	}
 
 	SimpleSelector  simpleselector() {
 		SimpleSelector  ss;
 		ss = new SimpleSelector();
 		SimpleSelector parent = ss;
 		String ident;
 		
 		if (StartOf(4)) {
 			ident = identity();
 			ss.setElementName(ident); 
 		} else if (la.kind == 43) {
 			Get();
 			ss.setElementName("*"); 
 		} else if (StartOf(11)) {
 			if (la.kind == 44) {
 				Get();
 				ident = identity();
 				ss.setID(ident); 
 			} else if (la.kind == 45) {
 				Get();
 				ident = identity();
 				ss.setClassName(ident); 
 			} else if (la.kind == 46) {
 				Attribute atb = attrib();
 				ss.setAttribute(atb); 
 			} else {
 				String psd = pseudo();
 				ss.setPseudo(psd); 
 			}
 		} else SynErr(78);
 		while (StartOf(11)) {
 			SimpleSelector child = new SimpleSelector(); 
 			if (la.kind == 44) {
 				Get();
 				ident = identity();
 				child.setID(ident); 
 			} else if (la.kind == 45) {
 				Get();
 				ident = identity();
 				child.setClassName(ident); 
 			} else if (la.kind == 46) {
 				Attribute atb = attrib();
 				child.setAttribute(atb); 
 			} else {
 				String psd = pseudo();
 				child.setPseudo(psd); 
 			}
 			parent.setChild(child);
 			parent = child;
 			
 		}
 		return ss;
 	}
 
 	Attribute  attrib() {
 		Attribute  atb;
 		atb = new Attribute();
 		String quote;
 		String ident;
 		
 		Expect(46);
 		ident = identity();
 		atb.setOperand(ident); 
 		if (StartOf(12)) {
 			switch (la.kind) {
 			case 47: {
 				Get();
 				atb.setOperator(AttributeOperator.Equals); 
 				break;
 			}
 			case 48: {
 				Get();
 				atb.setOperator(AttributeOperator.InList); 
 				break;
 			}
 			case 49: {
 				Get();
 				atb.setOperator(AttributeOperator.Hyphenated); 
 				break;
 			}
 			case 50: {
 				Get();
 				atb.setOperator(AttributeOperator.EndsWith); 
 				break;
 			}
 			case 51: {
 				Get();
 				atb.setOperator(AttributeOperator.BeginsWith); 
 				break;
 			}
 			case 52: {
 				Get();
 				atb.setOperator(AttributeOperator.Contains); 
 				break;
 			}
 			}
 			if (StartOf(4)) {
 				ident = identity();
 				atb.setValue(ident); 
 			} else if (la.kind == 5) {
 				quote = QuotedString();
 				atb.setValue(quote); 
 			} else SynErr(79);
 		}
 		Expect(53);
 		return atb;
 	}
 
 	BooleanExpression  orExpression() {
 		BooleanExpression  expr;
 		BooleanExpression left, right; BooleanOperation op; 
 		left = andExpression();
 		expr = left; 
 		while (la.kind == 59 || la.kind == 60) {
 			op = orop();
 			right = andExpression();
 			expr = new BinaryBooleanExpression(op, expr, right); 
 		}
 		return expr;
 	}
 
 	BooleanOperation  orop() {
 		BooleanOperation  op;
 		op = null; 
 		if (la.kind == 59) {
 			Get();
 			op = BooleanOperation.OR; 
 		} else if (la.kind == 60) {
 			Get();
 			op = BooleanOperation.XOR; 
 		} else SynErr(80);
 		return op;
 	}
 
 	BooleanExpression  andExpression() {
 		BooleanExpression  expr;
 		BooleanExpression left, right; BooleanOperation op; 
 		left = notExpression();
 		expr = left; 
 		while (la.kind == 61) {
 			Get();
 			right = notExpression();
 			expr = new BinaryBooleanExpression(BooleanOperation.AND, expr, right); 
 		}
 		return expr;
 	}
 
 	BooleanExpression  notExpression() {
 		BooleanExpression  expr;
 		BooleanExpression exp; expr = null; 
 		if (StartOf(13)) {
 			exp = primaryBooleanExpression();
 			expr = exp; 
 		} else if (la.kind == 56) {
 			Get();
 			exp = notExpression();
 			expr = new NotExpression(exp); 
 		} else SynErr(81);
 		return expr;
 	}
 
 	BooleanExpression  primaryBooleanExpression() {
 		BooleanExpression  expr;
 		expr = null; Term trm; BooleanExpression exp; 
 		if (la.kind == 54) {
 			Get();
 			exp = booleanExpression();
 			Expect(55);
 			expr = exp; 
 		} else if (StartOf(9)) {
 			trm = term();
 			expr = new TermBooleanExpression(trm); 
 		} else SynErr(82);
 		return expr;
 	}
 
 	CalcExpression  lengthExpression() {
 		CalcExpression  expr;
 		expr = additiveExpression();
 		return expr;
 	}
 
 	CalcExpression  additiveExpression() {
 		CalcExpression  expr;
 		CalcExpression left, right; Operation op; 
 		left = multiplicativeExpression();
 		expr = left; 
 		while (la.kind == 41 || la.kind == 62) {
 			op = addop();
 			right = multiplicativeExpression();
 			expr = new BinaryExpression(op, expr, right); 
 		}
 		return expr;
 	}
 
 	Operation  addop() {
 		Operation  op;
 		op = null; 
 		if (la.kind == 41) {
 			Get();
 			op = Operation.Add; 
 		} else if (la.kind == 62) {
 			Get();
 			op = Operation.Subtract; 
 		} else SynErr(83);
 		return op;
 	}
 
 	Operation  mulop() {
 		Operation  op;
 		op = null; 
 		if (la.kind == 43) {
 			Get();
 			op = Operation.Multiply; 
 		} else if (la.kind == 58) {
 			Get();
 			op = Operation.Divide; 
 		} else SynErr(84);
 		return op;
 	}
 
 	CalcExpression  multiplicativeExpression() {
 		CalcExpression  expr;
 		CalcExpression left, right; Operation op; 
 		left = termExpression();
 		expr = left; 
 		while (la.kind == 43 || la.kind == 58) {
 			op = mulop();
 			right = termExpression();
 			expr = new BinaryExpression(op, expr, right); 
 		}
 		return expr;
 	}
 
 	CalcExpression  termExpression() {
 		CalcExpression  expr;
 		expr = null; Term trm; CalcExpression exp; 
 		if (la.kind == 54) {
 			Get();
 			exp = lengthExpression();
 			Expect(55);
 			expr = exp; 
 		} else if (StartOf(9)) {
 			trm = term();
 			expr = new TermExpression(trm); 
 		} else SynErr(85);
 		return expr;
 	}
 
 	CalcExpression  calculation() {
 		CalcExpression  expr;
 		Expect(63);
 		Expect(54);
 		expr = lengthExpression();
 		Expect(55);
 		return expr;
 	}
 
 	String  HexValue() {
 		String  val;
 		StringBuilder sb = new StringBuilder();
 		boolean found = false;
 		
 		Expect(44);
 		sb.append(t.val); 
 		if (la.kind == 2) {
 			Get();
 			sb.append(t.val); 
 		} else if (la.kind == 1) {
 			Get();
 			sb.append(t.val); found = true; 
 		} else SynErr(86);
 		if (!found && partOfHex(sb.toString())) {
 			Expect(1);
 			sb.append(t.val); 
 		}
 		val = sb.toString(); 
 		return val;
 	}
 
 
 
 	public void Parse() {
 		la = new Token();
 		la.val = "";		
 		Get();
 		CSS3();
 
 		Expect(0);
 	}
 
 	private static final boolean[][] set = {
 		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,T,x,x, T,x,T,T, x,x,x,T, x,T,T,T, T,T,T,T, x,x,x,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,T, x,x,x,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,T,T, x,x,x,T, x,x,x,x, T,x,x,x, x,x,x,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,T,T, x,T,T,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,T,T, T,T,T,x, x,x},
 		{x,x,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,T,T, x,T,T,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,T,x, x,x,x,x, x,T,T,x, x,x,x,x, x,x,x,x, T,T,x,T, T,x,x,x, x,x,x,x, x,x,x,T, T,x,T,T, T,T,T,T, T,T,T,x, x,x},
 		{x,T,T,T, x,T,T,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,x,x, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,x, x,x},
 		{x,T,x,x, x,x,x,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,T,x, x,x,T,x, x,x,x,x, x,x,x,x, x,T,T,T, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x},
 		{x,T,T,T, x,T,T,x, x,T,T,T, T,T,T,T, T,T,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,x,x, T,x,x,x, x,x,x,x, x,x,T,x, x,x,x,x, x,x,T,T, T,T,T,x, x,x}
 
 	};
 
 	public static final String getErrorMessage(int n) {
 		String s;
 		switch (n) {
 			case 0: s = "EOF expected"; break;
 			case 1: s = "ident expected"; break;
 			case 2: s = "integer expected"; break;
 			case 3: s = "decimal expected"; break;
 			case 4: s = "s expected"; break;
 			case 5: s = "stringLit expected"; break;
 			case 6: s = "url expected"; break;
 			case 7: s = "\"<!--\" expected"; break;
 			case 8: s = "\"-->\" expected"; break;
 			case 9: s = "\"all\" expected"; break;
 			case 10: s = "\"aural\" expected"; break;
 			case 11: s = "\"braille\" expected"; break;
 			case 12: s = "\"embossed\" expected"; break;
 			case 13: s = "\"handheld\" expected"; break;
 			case 14: s = "\"print\" expected"; break;
 			case 15: s = "\"projection\" expected"; break;
 			case 16: s = "\"screen\" expected"; break;
 			case 17: s = "\"tty\" expected"; break;
 			case 18: s = "\"tv\" expected"; break;
 			case 19: s = "\"n\" expected"; break;
 			case 20: s = "\"url\" expected"; break;
 			case 21: s = "\"@media\" expected"; break;
 			case 22: s = "\"{\" expected"; break;
 			case 23: s = "\"}\" expected"; break;
 			case 24: s = "\"@if\" expected"; break;
 			case 25: s = "\"@else\" expected"; break;
 			case 26: s = "\":\" expected"; break;
 			case 27: s = "\"@class\" expected"; break;
 			case 28: s = "\"<\" expected"; break;
 			case 29: s = "\";\" expected"; break;
 			case 30: s = "\">\" expected"; break;
 			case 31: s = "\"@define\" expected"; break;
 			case 32: s = "\"global\" expected"; break;
 			case 33: s = "\"@font-face\" expected"; break;
 			case 34: s = "\"@page\" expected"; break;
 			case 35: s = "\"@import\" expected"; break;
 			case 36: s = "\"@include\" expected"; break;
 			case 37: s = "\"@charset\" expected"; break;
 			case 38: s = "\"@namespace\" expected"; break;
 			case 39: s = "\"@\" expected"; break;
 			case 40: s = "\",\" expected"; break;
 			case 41: s = "\"+\" expected"; break;
 			case 42: s = "\"~\" expected"; break;
 			case 43: s = "\"*\" expected"; break;
 			case 44: s = "\"#\" expected"; break;
 			case 45: s = "\".\" expected"; break;
 			case 46: s = "\"[\" expected"; break;
 			case 47: s = "\"=\" expected"; break;
 			case 48: s = "\"~=\" expected"; break;
 			case 49: s = "\"|=\" expected"; break;
 			case 50: s = "\"$=\" expected"; break;
 			case 51: s = "\"^=\" expected"; break;
 			case 52: s = "\"*=\" expected"; break;
 			case 53: s = "\"]\" expected"; break;
 			case 54: s = "\"(\" expected"; break;
 			case 55: s = "\")\" expected"; break;
 			case 56: s = "\"!\" expected"; break;
 			case 57: s = "\"important\" expected"; break;
 			case 58: s = "\"/\" expected"; break;
 			case 59: s = "\"||\" expected"; break;
 			case 60: s = "\"^\" expected"; break;
 			case 61: s = "\"&&\" expected"; break;
 			case 62: s = "\"-\" expected"; break;
 			case 63: s = "\"calc\" expected"; break;
 			case 64: s = "\"const\" expected"; break;
 			case 65: s = "\"param\" expected"; break;
 			case 66: s = "\"U\\\\\" expected"; break;
 			case 67: s = "\"%\" expected"; break;
 			case 68: s = "??? expected"; break;
 			case 69: s = "invalid directive"; break;
 			case 70: s = "invalid medium"; break;
 			case 71: s = "invalid identity"; break;
 			case 72: s = "invalid term"; break;
 			case 73: s = "invalid term"; break;
 			case 74: s = "invalid term"; break;
 			case 75: s = "invalid term"; break;
 			case 76: s = "invalid namespaceDirective"; break;
 			case 77: s = "invalid genericDirective"; break;
 			case 78: s = "invalid simpleselector"; break;
 			case 79: s = "invalid attrib"; break;
 			case 80: s = "invalid orop"; break;
 			case 81: s = "invalid notExpression"; break;
 			case 82: s = "invalid primaryBooleanExpression"; break;
 			case 83: s = "invalid addop"; break;
 			case 84: s = "invalid mulop"; break;
 			case 85: s = "invalid termExpression"; break;
 			case 86: s = "invalid HexValue"; break;
 			default: s = "error " + n; break;
 		}
 		return s;
 	}
 } // end Parser
 
 
 class FatalError extends RuntimeException {
 	public static final long serialVersionUID = 1L;
 	public FatalError(String s) { super(s); }
 }
 
