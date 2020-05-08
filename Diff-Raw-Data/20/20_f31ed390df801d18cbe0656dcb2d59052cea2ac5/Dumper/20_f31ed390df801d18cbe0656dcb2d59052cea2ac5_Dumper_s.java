 package maro.wrapper;
 
 // jason only
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ASSyntax;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Term;
 
 // java only
 import java.util.LinkedList;
 import java.util.List;
 
 // others
 import org.apache.commons.codec.binary.Base64;
 
 /* The perceive class should control the data that go in and go out of Jason platform.
 
    methods public: dump(serialize data) and undump(receive a serialize data and unserialize)
 
  */
 public class Dumper {
 	private String functor = null;
 	private String negated = null;
 	private String[] terms = null;
 	private Dumper[] annots = null;
 	private int arity = 0;
 
 	public Dumper() { }
 
 	public String getFunctorComplete() {
 		String ret = getFunctor().toLowerCase();
 		ret = ret.charAt(0) + getFunctor().substring(1);
 		return negated + ret;
 	}
 
 	public String getFunctor() {
 		return functor;
 	}
 
 	public void setFunctor(String f) {
 		functor = f;
 	}
 
 	public boolean getNegated() {
 		return !negated.isEmpty();
 	}
 
 	public String getNot() {
 		return negated;
 	}
 
 	public void setNot(String not) {
 		negated = not;
 	}
 
 	public String[] getTerms() {
 		return terms;
 	}
 
 	public int getArity() {
 		return arity;
 	}
 
 	public void setTerms(String[] t) {
 		String f = getFunctor().toLowerCase();
 		if (!f.equals("sameas"))
 			this.arity = t.length;
 		else
 			this.arity = 0;
 
 		terms = t;
 	}
 
 	public String getTermAsString(int position) {
 		String s = new String(terms[position]);
 		if (s.charAt(0) == '"')
 			s = s.substring(1, s.length()-1);
 		return s;
 	}
 
 	public Integer getTermAsInteger(int position) {
 		String s = getTermAsString(position);
 		return Integer.parseInt(s);
 	}
 
 	public Dumper[] getAnnots() {
 		return annots;
 	}
 
 	public void setAnnots(Dumper[] d) {
 		annots = d;
 	}
 
 	private int depth = 0;
 
 	public String toString() {
 		String ret = getFunctorComplete();
 		String terms = "";
 		String annots = "";
 		String pi = "";
 		boolean isDouble = false;
 
 		if (depth > 0 && functor.equals("source") == false)
 			pi = "\"";
 
 		for (String t: getTerms()) {
 			boolean empty = terms.isEmpty();
 
 			isDouble = false;
 			try {
 				Double.parseDouble(t);
 				isDouble = true;
 			} catch (Exception e) {
 			}
 
 			if (isDouble == false) {
 				if (empty)
 					terms += pi + t;
 				else terms += pi + ", " + pi + t;
 			} else {
 				if (empty)
 					terms += t;
 				else terms += ", " + t;
 			}
 		}
 
 		if (isDouble) pi = "";
 
 		if (!terms.isEmpty())
 			ret += "(" + terms + pi + ")";
 
 		for (Dumper d: getAnnots()) {
 			d.depth = this.depth + 1;
 			if (annots.isEmpty())
 				annots += d;
 			else annots += ", " + d;
 		}
 
 		if (!annots.isEmpty())
 			ret += "[" + annots + "]";
 
 		return ret;
 	}
 
 	static public Dumper[]
 	addOntology(Dumper[] d, String ontology) {
 		LinkedList<Dumper> sd = new LinkedList<Dumper>();
 		Dumper o = new Dumper();
 		o.setFunctor( "ontology" );
 		o.setNot("");
 		o.setTerms(new String[]  { ontology });
 		o.setAnnots(new Dumper[] { });
 
 		for (Dumper du : d) {
 			if (du.getFunctor().equals("ontology")) {
 				return d; // nao precisa fazer nada
 			}
 			sd.add(du);
 		}
 
 		sd.add(o);
 		return sd.toArray(new Dumper[0]);
 	}
 
 	static public String asXmlBean(Object o) {
 		try {
 			java.io.ByteArrayOutputStream ba = new java.io.ByteArrayOutputStream();
 			java.beans.XMLEncoder xe = new java.beans.XMLEncoder( ba );
 			xe.writeObject(o);
 			xe.close();
 			byte[] bs = ba.toByteArray();
 			return Base64.encodeBase64String(bs);
 		} catch (Exception e) {
 			System.err.println("Outch!");
 			System.exit(30);
 		}
 		return null;
 	}
 
 	static public Object fromXmlBean(String str) {
 		try {
 			byte[] decoded = Base64.decodeBase64(str);
 			java.io.ByteArrayInputStream ba = new java.io.ByteArrayInputStream(decoded);
 			java.beans.XMLDecoder xe = new java.beans.XMLDecoder( ba );
 			Object o = xe.readObject();
 			xe.close();
 			return o;
 		} catch (Exception e) {
 			System.err.println("Outch!");
 			System.exit(35);
 		}
 		return null;
 	}
 
 
 
 	static public Dumper dumpLiteral(Literal l) {
 		List<Dumper> las;
 		List<String> lts;
 		List<Term> lts_raw;
 		ListTerm las_raw;
 		Dumper ret;
 
 		if (l == null) return null;
 
 		l = l.makeVarsAnnon();
 		lts_raw = l.getTerms();
 		las_raw = l.getAnnots();
 
 		lts = new LinkedList<String>();
 		if (lts_raw != null) {
 			for (Term t: lts_raw) {
 				if (t.isAtom()) {
 					lts.add( t.toString() );
 				} else if (t.isString()) {
 					lts.add( ((jason.asSyntax.StringTerm)t).getString().toLowerCase() );
 				} else if (t.isNumeric()) {
 					lts.add( t.toString() );
 				} else if (t.isUnnamedVar()) {
 					System.err.println("Operation not suported variables!");
 					System.exit(19);
 				} else {
 					System.err.println("Operation suport only atom, string and numbers: " + t);
 					System.exit(21);
 				}
 			}
 		}
 
 		las = new LinkedList<Dumper>();
 		if (las_raw != null) {
 			for (Term t: las_raw) {
 				las.add( dumpLiteral((Literal) t) );
 			}
 		}
 
 		ret = new Dumper();
 		ret.setFunctor(l.getFunctor());
 		if (l.negated()) ret.setNot("~");
 		else ret.setNot("");
 
 		ret.setTerms(lts.toArray(new String[0]));
 		ret.setAnnots(las.toArray(new Dumper[0]));
 		return ret;
 	}
 
 	static public Literal
 	fromDumper(Dumper du, int e) {
 		return fromString(du.toString(), e);
 	}
 
 	static public Literal
 	fromString(String s, int e) {
 		Literal li = null;
 
 		try {
 			li = ASSyntax.parseLiteral(s);
 		} catch (Exception ex) {
 			System.out.println("Ocurred a problem on parsing: " + s);
 			ex.printStackTrace();
 			System.exit(e);
 		}
 
 		return li;
 	}
 
 	@Override
 	public int hashCode() {
 		int ret = arity;
 		int i;
 		ret += negated.hashCode();
 		ret += functor.hashCode();
 		i = 1;
 		for (String s: terms) {
 			ret += s.hashCode() * i;
 			i++;
 		}
 		i = 1;
 		for (Dumper d: annots) {
 			ret += d.hashCode() * i;
 			i++;
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean equals(Object d) {
 		return this.hashCode() == d.hashCode();
 	}
 }
