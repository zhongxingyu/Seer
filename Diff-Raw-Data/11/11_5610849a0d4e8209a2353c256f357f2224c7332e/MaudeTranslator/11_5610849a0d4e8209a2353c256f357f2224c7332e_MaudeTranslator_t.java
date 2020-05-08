 package ca.uqac.info.trace.conversion;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 
 import ca.uqac.info.ltl.Atom;
 import ca.uqac.info.ltl.Operator;
 import ca.uqac.info.ltl.Operator.ParseException;
 import ca.uqac.info.ltl.BinaryOperator;
 import ca.uqac.info.ltl.OperatorAnd;
 import ca.uqac.info.ltl.OperatorEquals;
 import ca.uqac.info.ltl.OperatorEquiv;
 import ca.uqac.info.ltl.OperatorF;
 import ca.uqac.info.ltl.OperatorG;
 import ca.uqac.info.ltl.OperatorImplies;
 import ca.uqac.info.ltl.OperatorNot;
 import ca.uqac.info.ltl.OperatorOr;
 import ca.uqac.info.ltl.OperatorU;
 import ca.uqac.info.ltl.OperatorVisitor;
 import ca.uqac.info.ltl.OperatorX;
 import ca.uqac.info.ltl.UnaryOperator;
 import ca.uqac.info.ltl.XPathAtom;
 import ca.uqac.info.trace.EventTrace;
 import ca.uqac.info.util.Relation;
 
 
 public class MaudeTranslator extends Translator {
 
 	Vector<String> o_params;
 	AtomicTranslator at = new AtomicTranslator();
 	private EventTrace trace;
 
 	/**
 	 * Constructor
 	 */
 	public MaudeTranslator() {
 		super();
 		o_params = new Vector<String>();
 	}
 
 	public MaudeTranslator(EventTrace t) {
 		super();
 		o_params = new Vector<String>();
 		this.trace = t;
 	}
 
 	@Override
 	public String translateTrace(EventTrace t) {
 		String out = "", operandes = "", chaine = "",prop="";
 		StringBuffer out_Trace = new StringBuffer();
 		this.trace = t;
 		Vector<String> listParm = new Vector<String>();
 		Operator op = this.getFormula();
 		try {
//			getParamFormula(op.toString());
 
 			at.setParameters(o_params);
 			out = at.translateTrace(t);
 			prop = generateFormula(at.translateFormula(op));
 			
 
		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		String[] tab = out.split(" ");
 		for (int j = 0; j < tab.length; j++) {
 			String c = tab[j];
 			if (j == 0) {
 				chaine = chaine.concat(c);
 			} else {
 				chaine = chaine.concat(" , " + c);
 			}
 			if (!listParm.contains(c)) {
 				listParm.add(c);
 				operandes = operandes.concat(c + " ");
 			}
 		}
 
 		chaine = chaine.concat("    |= ").concat(prop);
 		// Start writing the Java program
 		out_Trace.append("in ltl.maude");
 		out_Trace.append("\n \n \n");
 		out_Trace.append("fmod MY-TRACE is").append("\t");
 		out_Trace.append("\n ");
 		out_Trace.append("extending LTL .").append("\t");
 		out_Trace.append("ops  ").append(operandes).append(" : -> Atom .")
 		.append("\t");
 		out_Trace.append("\n \n \n");
 		out_Trace.append("endfm \n ");
 		out_Trace.append("");
 		out_Trace.append("reduce").append(" ");
 		out_Trace.append(chaine);
 		return out_Trace.toString();
 	}
 
 	private Operator getFormula() {
 		return m_formula;
 	}
 	public Operator getParamFormula(String s) throws ParseException {
 
 		s = s.trim();
 		String c = s.substring(0, 1);
 		Operator out = null;
 		if (isUnaryOperator(c)) {
 			// Unary operator
 			s = s.substring(1).trim();
 			if (s.startsWith(("("))) {
 				// We trim surrounding parentheses, if any
 				s = s.substring(1, s.length() - 1).trim();
 			}
 			Operator in = getParamFormula(s);
 			UnaryOperator uo = null;
 			if (c.compareTo("F") == 0)
 				uo = new OperatorF();
 			else if (c.compareTo("X") == 0)
 				uo = new OperatorX();
 			else if (c.compareTo("G") == 0)
 				uo = new OperatorG();
 			else if (c.compareTo("!") == 0)
 				uo = new OperatorNot();
 			if (uo == null)
 				throw new ParseException();
 			uo.setOperand(in);
 			out = uo;
 		} else if (containsBinaryOperator(s)) {
 			// Here, we know s contains either a binary operator or is
 			// an atom. We discriminate by checking for the presence of
 			// a binary operator
 			String left = getLeft(s);
 			String right = getRight(s);
 			assert !left.isEmpty() && !right.isEmpty();
 			int pars_left = s.indexOf(left);
 			int pars_right = s.length() - s.lastIndexOf(right) - right.length();
 			assert pars_left >= 0 && pars_right >= 0;
 			String op = getOperator(s, left.length() + pars_left * 2,
 					right.length() + pars_right * 2);
 
 			if (op.compareTo("=") == 0) {
 
 				this.o_params.add(left);
 			}
 			Operator o_left = getParamFormula(left);
 
 			Operator o_right = getParamFormula(right);
 			if (o_left == null || o_right == null)
 				throw new ParseException();
 			BinaryOperator bo = null;
 			if (op.compareTo("&") == 0)
 				bo = new OperatorAnd();
 			else if (op.compareTo("|") == 0)
 				bo = new OperatorOr();
 			else if (op.compareTo("->") == 0)
 				bo = new OperatorImplies();
 			else if (op.compareTo("=") == 0)
 				bo = new OperatorEquals();
 			else if (op.compareTo("<->") == 0)
 				bo = new OperatorEquiv();
 			else if (op.compareTo("U") == 0)
 				bo = new OperatorU();
 
 			if (bo == null)
 				throw new ParseException();
 			bo.setLeft(o_left);
 			bo.setRight(o_right);
 			out = bo;
 		} else {
 			// Atom or XPathAtom, last remaining case
 			if (s.startsWith("{"))
 				out = new XPathAtom(s);
 			else {
 				out = new Atom(s);
 
 			}
 		}
 		if (out == null)
 			throw new ParseException();
 		return out;
 	}
 
 	private static boolean isUnaryOperator(String c) {
 		return c.compareTo("F") == 0 || c.compareTo("G") == 0
 				|| c.compareTo("X") == 0 || c.compareTo("!") == 0;
 	}
 
 	private static boolean containsBinaryOperator(String s) {
 		return s.indexOf("&") != -1 || s.indexOf("|") != -1
 				|| s.indexOf("->") != -1 || s.indexOf("=") != -1;
 	}
 
 	private static String getLeft(String s) {
 		if (s.startsWith("(")) {
 			// Find matching right parenthesis
 			int paren_level = 1;
 			for (int i = 1; i < s.length(); i++) {
 				String c = s.substring(i, i + 1);
 				if (c.compareTo("(") == 0)
 					paren_level++;
 				if (c.compareTo(")") == 0)
 					paren_level--;
 				if (paren_level == 0)
 					return s.substring(1, i);
 			}
 		} else {
 			// Loop until operator or space found
 			for (int i = 1; i < s.length(); i++) {
 				String c = s.substring(i, i + 1);
 				if (c.compareTo("(") == 0 || c.compareTo(")") == 0
 						|| c.compareTo("&") == 0 || c.compareTo("|") == 0
 						|| c.compareTo("=") == 0)
 					return s.substring(0, i);
 				if (i < s.length() - 1
 						&& s.substring(i, i + 2).compareTo("->") == 0)
 					return s.substring(0, i);
 			}
 		}
 		return "";
 	}
 
 	private static String getRight(String s) {
 		if (s.endsWith(")")) {
 			// Find matching left parenthesis
 			int paren_level = 1;
 			for (int i = s.length() - 1; i >= 0; i--) {
 				String c = s.substring(i, i + 1);
 				if (c.compareTo(")") == 0)
 					paren_level++;
 				if (c.compareTo("(") == 0)
 					paren_level--;
 				if (paren_level == 1)
 					return s.substring(i + 1, s.length() - 1);
 			}
 		} else {
 			// Loop until operator or space found
 			for (int i = s.length() - 1; i >= 0; i--) {
 				String c = s.substring(i, i + 1);
 				if (c.compareTo("(") == 0 || c.compareTo(")") == 0
 						|| c.compareTo("&") == 0 || c.compareTo("|") == 0
 						|| c.compareTo("=") == 0)
 					return s.substring(i + 1);
 				if (i < s.length() - 1
 						&& s.substring(i, i + 2).compareTo("->") == 0)
 					return s.substring(i + 2);
 			}
 		}
 		return "";
 	}
 
 	private static String getOperator(String s, int size_left, int size_right) {
 		assert size_left + size_right < s.length();
 		return s.substring(size_left, s.length() - size_right).trim();
 	}
 	public String translateFormula(Operator o) {
 		
 		StringBuffer out = new StringBuffer();
 		MaudeFormulaTranslator mft = new MaudeFormulaTranslator();
 		o.accept(mft);
 		out.append(mft.getFormula());
 		
 		return out.toString();
 	}
 
 	protected class MaudeFormulaTranslator implements OperatorVisitor {
 		Stack<StringBuffer> m_pieces;
 
 		public MaudeFormulaTranslator() {
 			super();
 			m_pieces = new Stack<StringBuffer>();
 		}
 
 		public String getFormula() {
 			StringBuffer out = m_pieces.peek();
 			return out.toString();
 		}
 
 		@Override
 		public void visit(OperatorAnd o) {
 			StringBuffer right = m_pieces.pop();
 			StringBuffer left = m_pieces.pop();
 			StringBuffer out = new StringBuffer("(").append(left).append(") ")
 					.append("/\\").append("(").append(right).append(")");
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(OperatorOr o) {
 			StringBuffer right = m_pieces.pop();
 			StringBuffer left = m_pieces.pop();
 			StringBuffer out = new StringBuffer("(").append(left).append(")")
 					.append("\\/").append("(").append(right).append(")");
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(OperatorImplies o) {
 			StringBuffer right = m_pieces.pop();
 			StringBuffer left = m_pieces.pop();
 			StringBuffer out = new StringBuffer("(").append(left)
 					.append(") -> (").append(right).append(")");
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(OperatorNot o) {
 			StringBuffer op = m_pieces.pop();
 			StringBuffer out = new StringBuffer("~ (").append(op).append(")");
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(OperatorF o) {
 			StringBuffer op = m_pieces.pop();
 
 			StringBuffer out = new StringBuffer("<> ( ").append(op).append(" )");
 			m_pieces.push(out);
 
 		}
 
 		@Override
 		public void visit(OperatorX o) {
 			StringBuffer op = m_pieces.pop();
 			StringBuffer out = new StringBuffer("o (").append(op).append(")");
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(OperatorG o) {
 			StringBuffer op = m_pieces.pop();
 			StringBuffer out = new StringBuffer("[] (").append(op).append(")");
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(OperatorEquals o) {
 			m_pieces.pop(); // Pop right-hand side
 			m_pieces.pop(); // Pop left-hand side
 			StringBuffer out = new StringBuffer(toMaudeIdentifier(o));
 			m_pieces.push(out);
 		}
 
 		@Override
 		public void visit(Atom o) {
 			m_pieces.push(new StringBuffer(o.getSymbol()));
 		}
 
 		@Override
 		public void visit(OperatorEquiv o) {
 		}
 
 		@Override
 		public void visit(OperatorU o) {
 		}
 
 	}
 
 	protected class MaudeEqualityGetter implements OperatorVisitor {
 
 		Set<OperatorEquals> m_equalities;
 
 		public MaudeEqualityGetter() {
 			super();
 			m_equalities = new HashSet<OperatorEquals>();
 		}
 
 		public Set<OperatorEquals> getEqualities() {
 			return m_equalities;
 		}
 
 		@Override
 		public void visit(OperatorEquals o) {
 			m_equalities.add(o);
 		}
 
 		@Override
 		public void visit(OperatorAnd o) {
 		}
 
 		@Override
 		public void visit(OperatorOr o) {
 		}
 
 		@Override
 		public void visit(OperatorImplies o) {
 		}
 
 		@Override
 		public void visit(OperatorNot o) {
 		}
 
 		@Override
 		public void visit(OperatorF o) {
 		}
 
 		@Override
 		public void visit(OperatorX o) {
 		}
 
 		@Override
 		public void visit(OperatorG o) {
 		}
 
 		@Override
 		public void visit(Atom o) {
 		}
 
 		@Override
 		public void visit(OperatorEquiv o) {
 		}
 
 		@Override
 		public void visit(OperatorU o) {
 
 		}
 
 	}
 
 	protected static String toMaudeIdentifier(OperatorEquals o) {
 		String left = o.getLeft().toString();
 		String right = o.getRight().toString();
 		return new StringBuffer(left).append(" = ").append(right).toString();
 	}
 
 	@Override
 	public String getSignature(EventTrace m_trace) {
 		Relation<String, String> param_domains = m_trace.getParameterDomain();
 		Set<String> params = param_domains.keySet();
 		Vector<String> vectParams = new Vector<String>();
 		vectParams.addAll(params);
 
 		String strTemp = vectParams.toString().replaceAll(",", " ");
 		strTemp = strTemp.replace("[", " ");
 		strTemp = strTemp.replace("]", " ");
 		return strTemp;
 	}
 
 	@Override
 	public String translateFormula() {
 		String prop = "";
 		Operator op = this.getFormula();
 		try {
 			getParamFormula(op.toString());
 
 			at.setParameters(o_params);
 			at.translateTrace(this.trace);
 			prop = at.translateFormula(op);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		return generateFormula(prop);
 	}
 
 	public String generateFormula(String chaine)
 	{
 		String res = "";
 		String [] tab = chaine.split(" ");
 		for(String c : tab)
 		{
 			if(c.equalsIgnoreCase("G"))
 			{
 				res = res.concat("[] ");
 			}else if (c.equalsIgnoreCase("F"))
 			{
 				res = res.concat("<> ");
 			}else if (c.equalsIgnoreCase("X"))
 			{
 				res = res.concat("o ");
 			}else if (c.equalsIgnoreCase("|"))
 			{
 				res = res.concat("\\/ ");
 			}else if (c.equalsIgnoreCase("& "))
 			{
 				res = res.concat("/\\ ");
 			}else if (c.equalsIgnoreCase("! "))
 			{
 				res = res.concat("~ ");
 			}else{
 				res = res.concat(c).concat(" ");
 			}
 			
 		}
 		res = res.concat(".");
 		return res ;
 	}
 	
 	@Override
 	public String translateTrace() {
 		return null;
 	}
 	public static void main (String [] args)
 	{
 		
 		MaudeTranslator md = new MaudeTranslator() ;
 		String s = "F ((e2 | e0) | (e3 |e4)| (e3 |e4)) ";
 		Operator op;
 		try {
 			op = Operator.parseFromString(s);
 			System.out.println(md.translateFormula(op));
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 }
