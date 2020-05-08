 package applets.Termumformungen$in$der$Technik_02_Kondensatoren;
 
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 
 public class EquationSystem {
 
 	Set<String> baseUnits = new HashSet<String>();
 	
 	static class Unit {
 		static class Pot {
 			String baseUnit; int pot;
 			Pot(String u) { baseUnit = u; pot = 1; }
 			Pot(String u, int p) { baseUnit = u; pot = p; }
 			@Override public String toString() { return baseUnit + ((pot != 1) ? ("^" + pot) : ""); }
 		}
 		List<Pot> facs = new LinkedList<Pot>();
 		void init(String str) {
 			int state = 0;
 			String curPot = "";
 			String curBaseUnit = "";
 			for(int i = 0; i <= str.length(); ++i) {
 				int c = (i < str.length()) ? str.charAt(i) : 0;
 				boolean finishCurrent = false;
 				if(c == '^') {
 					if(state != 0) throw new AssertionError("bad ^ at " + i);
 					if(curBaseUnit.isEmpty()) throw new AssertionError("missing unit, bad ^ at " + i);
 					state = 1;
 				}
 				else if(c >= '0' && c <= '9' || c == '-') {
 					if(state == 0) curBaseUnit += (char)c;
 					else curPot += (char)c;
 				}
 				else if(c == ' ' || c == 0) {
 					if(state == 0 && !curBaseUnit.isEmpty()) finishCurrent = true;
 					if(state == 1 && !curPot.isEmpty()) finishCurrent = true;
 				}
 				else {
 					if(state == 1) throw new AssertionError("invalid char " + (char)c + " at " + i);
 					curBaseUnit += (char)c;
 				}
 				if(finishCurrent) {
 					Pot p = new Pot(curBaseUnit);
 					if(state > 0) p.pot = Integer.parseInt(curPot);
 					facs.add(p);
 					state = 0; curBaseUnit = ""; curPot = "";
 				}
 			}			
 		}
 		void simplify() {
 			Map<String,Integer> pot = new HashMap<String, Integer>();
 			for(Pot p : this.facs) {
 				int oldPot = pot.containsKey(p.baseUnit) ? pot.get(p.baseUnit) : 0;
 				pot.put(p.baseUnit, oldPot + p.pot);
 			}
 			facs.clear();
 			for(String u : pot.keySet()) {
 				if(pot.get(u) != 0)
 					facs.add(new Pot(u, pot.get(u)));
 			}			
 		}
 		Unit(String str) { init(str); simplify(); }
 		Unit(String str, Set<String> allowedBaseUnits) {
 			init(str); simplify();
 			for(Pot p : facs)
 				if(!allowedBaseUnits.contains(p.baseUnit))
 					throw new AssertionError("unit " + p.baseUnit + " not allowed");
 		}
 		@Override public String toString() { return Utils.concat(facs, " "); }
 	}
 		
 	Set<String> variableSymbols = new HashSet<String>();	
 	void registerVariableSymbol(String var) { variableSymbols.add(var); }	
 		
 	static abstract class Expression {
 		<T> T castTo(Class<T> clazz) {
 			if(clazz.isAssignableFrom(getClass())) return clazz.cast(this);
 			if(clazz.isAssignableFrom(String.class)) return clazz.cast(asVariable());
 			if(clazz.isAssignableFrom(Integer.class)) return clazz.cast(asNumber());
 			try {
 				return clazz.getConstructor(getClass()).newInstance(this);
 			} catch (Exception e) {}
 			throw new AssertionError("unknown type: " + clazz);
 		}
 		String asVariable() { return castTo(Equation.Sum.Prod.Pot.class).asVariable(); } 
 		Integer asNumber() { return castTo(Equation.Sum.Prod.class).asNumber(); }
 
 		boolean equalsToNum(int num) {
 			Integer myNum = asNumber();
 			if(myNum != null) return myNum == num;
 			return false;
 		}
 		
 		abstract Iterable<? extends Expression> childs();
 		Iterable<String> vars() {
 			Iterable<Iterable<String>> varIters = Utils.map(childs(), new Utils.Function<Expression, Iterable<String>>() {
 				public Iterable<String> eval(Expression obj) {
 					return obj.vars();
 				}
 			});
 			return Utils.concatCollectionView(varIters);
 		}
 		
 		abstract String baseOp();
 		Utils.OperatorTree asOperatorTree() {
 			Utils.OperatorTree ot = new Utils.OperatorTree();
 			ot.op = baseOp();
 			for(Expression child : childs())
 				ot.entities.add(child.asOperatorTree().asEntity());
 			return ot;
 		}
 
 	}
 	
 	static class Equation /*extends Expression*/ implements Comparable<Equation> {
 		static class Sum extends Expression implements Comparable<Sum> {
 			static class Prod extends Expression implements Comparable<Prod> {
 				int fac = 0;
 				static class Pot extends Expression implements Comparable<Pot> {
 					String sym;
 					int pot = 1;
 					@Override public String toString() { return sym + ((pot != 1) ? ("^" + pot) : ""); }		
 					Pot(String s) { sym = s; }
 					Pot(String s, int p) { sym = s; pot = p; }
 					public int compareTo(Pot o) {
 						int c = sym.compareTo(o.sym);
 						if(c != 0) return c;
 						if(pot < o.pot) return -1;
 						if(pot > o.pot) return 1;
 						return 0;
 					}
 					@Override public int hashCode() {
 						int result = 1;
 						result = 31 * result + pot;
 						result = 31 * result + ((sym == null) ? 0 : sym.hashCode());
 						return result;
 					}
 					@Override public boolean equals(Object obj) {
 						if(!(obj instanceof Pot)) return false;
 						return compareTo((Pot) obj) == 0;
 					}
 					@Override Integer asNumber() {
 						if(pot == 0) return 1;
 						return null;
 					}
 					@Override String asVariable() {
 						if(pot == 1) return sym;
 						return null;
 					}
 					@Override Iterable<String> vars() { return Utils.listFromArgs(sym); }
 					@Override Iterable<? extends Expression> childs() { return Utils.listFromArgs(); }
 					@Override String baseOp() { return "∙"; }
 					@Override Utils.OperatorTree asOperatorTree() {
 						if(pot == 0) return new Utils.OperatorTree("", new Utils.OperatorTree.RawString("1"));
 						Utils.OperatorTree ot = new Utils.OperatorTree();
 						if(pot > 0) {
 							ot.op = (pot > 1) ? baseOp() : "";
 							for(int i = 0; i < pot; ++i)
 								ot.entities.add(new Utils.OperatorTree.RawString(sym));
 							return ot;
 						}
 						else {
 							ot.op = "/";
 							ot.entities.add(new Utils.OperatorTree.RawString("1"));
 							ot.entities.add(new Utils.OperatorTree.Subtree(new Pot(sym, -pot).asOperatorTree()));
 						}
 						return ot;
 					}
 				}						
 				List<Pot> facs = new LinkedList<Pot>();
 				public int compareTo(Prod o) {
 					int r = Utils.<Pot>orderOnCollection().compare(facs, o.facs);
 					if(r != 0) return r;
 					if(fac < o.fac) return -1;
 					if(fac > o.fac) return 1;
 					return 0;
 				}
 				@Override public boolean equals(Object obj) {
 					if(!(obj instanceof Prod)) return false;
 					return compareTo((Prod) obj) == 0;
 				}
 				@Override Integer asNumber() {
 					if(fac == 0) return 0;
 					if(facs.isEmpty()) return fac;
 					return null;
 				}
 				@Override <T> T castTo(Class<T> clazz) {
 					if(clazz.isAssignableFrom(Pot.class)) {
 						if(facs.size() == 1) return clazz.cast(facs.get(0));
 						return null;
 					}
 					return super.castTo(clazz);
 				}
 				boolean isZero() { return fac == 0; }
 				boolean isOne() { return fac == 1 && facs.isEmpty(); }
 				@Override public String toString() {
 					if(facs.isEmpty()) return "" + fac;
 					if(fac == 1) return Utils.concat(facs, " ∙ ");
 					if(fac == -1) return "-" + Utils.concat(facs, " ∙ ");
 					return fac + " ∙ " + Utils.concat(facs, " ∙ ");
 				}
 				Map<String,Integer> varMap() {
 					Map<String,Integer> vars = new TreeMap<String,Integer>();
 					for(Pot p : facs) {
 						if(!vars.containsKey(p.sym)) vars.put(p.sym, 0);
 						vars.put(p.sym, vars.get(p.sym) + p.pot);
 					}
 					return vars;
 				}
 				Prod normalize() {
 					Prod prod = new Prod();
 					prod.fac = fac;
 					Map<String,Integer> vars = varMap();
 					for(String sym : vars.keySet()) {
 						if(vars.get(sym) != 0)
 							prod.facs.add(new Pot(sym, vars.get(sym)));
 					}
 					return prod;
 				}
 				Prod minusOne() { return new Prod(-fac, facs); }
 				Prod divideAndRemove(String var) {
 					Prod prod = normalize();
 					for(Iterator<Pot> pit = prod.facs.iterator(); pit.hasNext(); ) {
 						Pot p = pit.next();
 						if(p.sym.equals(var)) {
 							if(p.pot == 1) {
 								pit.remove();
 								return prod;
 							}
 							break;
 						}
 					}
 					return null; // var not included or not pot=1
 				}
 				Prod divideIfReducing(String var) {
 					Prod prod = normalize();
 					for(Iterator<Pot> pit = prod.facs.iterator(); pit.hasNext(); ) {
 						Pot p = pit.next();
 						if(p.sym.equals(var)) {
 							if(p.pot > 1) {
 								p.pot--;
 								return prod;
 							}
 							if(p.pot == 1) {
 								pit.remove();
 								return prod;
 							}
 							break;
 						}
 					}
 					return null; // var not included or not pot=1
 				}
 				Prod mult(Prod other) {
 					Prod res = new Prod();
 					res.fac = fac * other.fac;
 					res.facs.addAll(facs);
 					res.facs.addAll(other.facs);
 					return res.normalize();
 				}
 				Prod divide(Prod other) {
 					if(!(other.fac != 0)) throw new AssertionError("other.fac != 0 failed");
 					if(!(fac % other.fac == 0)) throw new AssertionError("fac % other.fac == 0 failed");
 					Prod res = new Prod();
 					res.fac = fac / other.fac;
 					res.facs.addAll(facs);
 					for(Pot p : other.facs)
 						res.facs.add(new Pot(p.sym, -p.pot));
 					return res.normalize();
 				}
 				Prod commonBase(Prod other) {
 					Prod base = new Prod();
 					base.fac = BigInteger.valueOf(fac).gcd(BigInteger.valueOf(other.fac)).intValue();
 					Map<String,Integer> varMap1 = varMap();
 					Map<String,Integer> varMap2 = other.varMap();							
 					Set<String> commonVars = new HashSet<String>();
 					commonVars.addAll(varMap1.keySet());
 					commonVars.retainAll(varMap2.keySet());
 					for(String var : commonVars) {
 						Pot pot = new Pot(var);
 						pot.pot = Math.min(varMap1.get(var), varMap2.get(var));
 						if(pot.pot != 0)
 							base.facs.add(pot);
 					}
 					return base;
 				}
 				@Override Iterable<? extends Expression> childs() { return facs; }
 				Prod() {}
 				Prod(int num) { this.fac = num; }
 				Prod(String var) { fac = 1; facs.add(new Pot(var)); }
 				Prod(int fac, List<Pot> facs) { this.fac = fac; this.facs = facs; }
 				Prod(Iterable<String> vars) {
 					fac = 1;
 					for(String v : vars)
 						facs.add(new Pot(v));
 				}
 				Prod(Utils.OperatorTree ot) throws ParseError { fac = 1; parse(ot); }
 				void parse(Utils.OperatorTree ot) throws ParseError {
 					if(ot.canBeInterpretedAsUnaryPrefixed() && ot.op.equals("-")) {
 						fac = -fac;
 						parse(ot.unaryPrefixedContent().asTree());
 					}
 					else if(ot.op.equals("∙") || ot.entities.size() <= 1) {
 						for(Utils.OperatorTree.Entity e : ot.entities) {
 							if(e instanceof Utils.OperatorTree.RawString) {
 								String s = ((Utils.OperatorTree.RawString) e).content;
 								try {
 									fac *= Integer.parseInt(s);
 								}
 								catch(NumberFormatException ex) {
 									String var = s;
 									facs.add(new Pot(var));
 								}
 							}
 							else
 								parse( ((Utils.OperatorTree.Subtree) e).content );
 						}
 					}
 					else
 						throw new ParseError("'" + ot + "' must be a product.", "'" + ot + "' muss ein Produkt sein.");
 					
 					if(facs.isEmpty())
 						throw new ParseError("'" + ot + "' must contain at least one variable.", "'" + ot + "' muss mindestens eine Variable enthalten.");
 				}
 				@Override String baseOp() { return "∙"; }
 				@Override Utils.OperatorTree asOperatorTree() {
 					if(fac == 0) return Utils.OperatorTree.Zero();
 					if(fac == 1 && !facs.isEmpty()) return super.asOperatorTree();
 					if(fac == 1 && facs.isEmpty()) return Utils.OperatorTree.One();
 					if(fac == -1 && !facs.isEmpty()) return super.asOperatorTree().minusOne();
 					if(fac == -1 && facs.isEmpty()) return Utils.OperatorTree.One().minusOne();
 					return Utils.OperatorTree.Product(Utils.listFromArgs(
 							Utils.OperatorTree.Number(fac).asEntity(),
 							super.asOperatorTree().asEntity()
 							));
 				}
 			}
 			List<Prod> entries = new LinkedList<Prod>();
 			@Override public String toString() { return Utils.concat(entries, " + "); }
 			public int compareTo(Sum o) { return Utils.<Prod>orderOnCollection().compare(entries, o.entries); }
 			@Override public boolean equals(Object obj) {
 				if(!(obj instanceof Sum)) return false;
 				return compareTo((Sum) obj) == 0;
 			}
 			boolean isZero() { return entries.isEmpty(); }
 			boolean isOne() { return entries.size() == 1 && entries.get(0).isOne(); }
 			Sum normalize() {
 				List<Prod> newEntries = new LinkedList<Prod>();
 				for(Prod prod : entries)
 					newEntries.add(prod.normalize());
 				Collections.sort(newEntries);
 				Prod lastProd = null;
 
 				Sum sum = new Sum();
 				for(Prod prod : newEntries) {
 					if(lastProd != null && lastProd.facs.equals(prod.facs))
 						lastProd.fac += prod.fac;
 					else {
 						lastProd = prod;
 						sum.entries.add(lastProd);
 					}
 				}
 				for(Iterator<Prod> pit = sum.entries.iterator(); pit.hasNext();) {
 					if(pit.next().isZero())
 						pit.remove();
 				}
 				Collections.sort(sum.entries);
 				
 				if(!sum.entries.isEmpty() && sum.entries.get(0).fac < 0)
 					return sum.minusOne();
 				return sum;
 			}
 			Sum reduce() {
 				// IMPORTANT: assumes that we can simply divide by all variables
 				if(entries.size() <= 1) return this;
 				List<Prod.Pot> firstProd = new LinkedList<Prod.Pot>(entries.get(0).facs);
 				Sum sum = this;
 				for(Prod.Pot p : firstProd) {
 					for(int i = 0; i < p.pot; ++i) {
 						Sum newSum = sum.divideIfReducing(p.sym);
 						if(newSum == null) break;
 						sum = newSum;
 					}
 				}
 				return sum;
 			}
 			Sum minusOne() {
 				Sum sum = new Sum();
 				for(Prod prod : entries) sum.entries.add(prod.minusOne());
 				return sum;
 			}
 			Sum sum(Sum other) {
 				Sum sum = new Sum();
 				sum.entries.addAll(entries);
 				sum.entries.addAll(other.entries);
 				return sum;
 			}
 			Sum mult(Prod other) {
 				Sum sum = new Sum();
 				if(other.isZero()) return sum;
 				for(Prod p : entries)
 					sum.entries.add(p.mult(other));
 				return sum;
 			}
 			Sum mult(Sum other) {
 				Sum sum = new Sum();
 				for(Prod p : other.entries)
 					sum.entries.addAll( mult(p).entries );
 				return sum;
 			}
 			Sum divide(Prod other) {
 				Sum sum = new Sum();
 				for(Prod p : entries)
 					sum.entries.add(p.divide(other));
 				return sum;
 			}
 			Sum divide(Sum other) {
 				if(other.isZero()) throw new AssertionError("division by 0");
 				if(other.entries.size() > 1) throw new AssertionError("not supported right now");
 				return divide(other.entries.get(0));
 			}
 			Sum divideIfReducing(String var) {
 				Sum sum = new Sum();
 				for(Prod p : entries) {
 					p = p.divideIfReducing(var);
 					if(p == null) return null;
 					sum.entries.add(p);
 				}
 				return sum;
 			}
 			Sum commomBase(Sum other) {
 				if(isZero() || other.isZero()) return new Sum();
 				if(entries.size() > 1 || other.entries.size() > 1) return new Sum(1); // we should optimize this maybe later...
 				return new Sum(entries.get(0).commonBase(other.entries.get(0)));
 			}
 			@Override Iterable<? extends Expression> childs() { return entries; }
 			static class ExtractedVar {
 				String var;
 				Sum varMult = new Sum();
 				Sum independentPart = new Sum();
 				@Override public String toString() {
 					return "{var=" + var + ", varMult=" + varMult + ", independentPart=" + independentPart + "}"; 
 				}
 			}
 			ExtractedVar extractVar(String var) {
 				ExtractedVar extracted = new ExtractedVar();
 				extracted.var = var;
 				for(Prod p : entries) {
 					if(p.isZero()) continue;
 					Prod newP = p.divideAndRemove(var);
 					if(newP != null)
 						extracted.varMult.entries.add(newP);
 					else
 						extracted.independentPart.entries.add(p);
 				}
 				if(!extracted.varMult.entries.isEmpty())
 					return extracted;
 				return null;
 			}
 			Sum() {}
 			Sum(String var) { entries.add(new Prod(var)); }
 			Sum(Prod prod) { entries.add(prod); }
 			Sum(int num) { entries.add(new Prod(num)); }
 			Sum(Utils.OperatorTree left, Utils.OperatorTree right) throws ParseError {
 				this(Utils.OperatorTree.MergedEquation(left, right)
 					.transformMinusToPlus()
 					.simplify()
 					.transformMinusPushedDown()
 					.multiplyAllDivisions()
 					.pushdownAllMultiplications());
 			}
 			Sum(Utils.OperatorTree ot) throws ParseError { add(ot); }
 			void add(Utils.OperatorTree ot) throws ParseError {
 				if(ot.isZero()) return;
 				if(ot.entities.size() == 1) {
 					Utils.OperatorTree.Entity e = ot.entities.get(0); 
 					if(e instanceof Utils.OperatorTree.Subtree)
 						add(((Utils.OperatorTree.Subtree) e).content);
 					else
 						entries.add(new Prod(ot));
 					return;
 				}
 				
 				if(ot.canBeInterpretedAsUnaryPrefixed() && ot.op.equals("-")) {
 					Utils.OperatorTree.Entity e = ot.unaryPrefixedContent();
 					try {
 						entries.add(new Prod(e.asTree()).minusOne());
 					} catch(ParseError exc) {
 						throw new ParseError(
 								"Prefix '-' only allowed for single products: " + ot + "; " + exc.english,
 								"Prexif '-' nur für einzelne Produkte erlaubt: " + ot + "; " + exc.german);
 					}
 					return;
 				}
 				
 				if(ot.op.equals("+")) {
 					for(Utils.OperatorTree.Entity e : ot.entities)
 						add(e.asTree());
 				}
 				else if(ot.op.equals("∙")) {
 					entries.add(new Prod(ot));
 				}
 				else
 					// all necessary transformation should have been done already (e.g. in Sum(left,right))
 					throw new ParseError("'" + ot.op + "' not supported in " + ot, "'" + ot.op + "' nicht unterstützt in " + ot);
 			}
 			@Override String baseOp() { return "+"; }
 			boolean isTautology() { return normalize().isZero(); }
 			Equation asEquation() { return new Equation(asOperatorTree(), Utils.OperatorTree.Zero()); }
 		}
 
 		Utils.OperatorTree left, right;
 		@Override public String toString() { return left.toString() + " = " + right.toString(); }
 		public int compareTo(Equation o) {
 			int r = left.compareTo(o.left); if(r != 0) return r;
 			return right.compareTo(o.right);
 		}
 		@Override public boolean equals(Object obj) {
 			if(!(obj instanceof Equation)) return false;
 			return compareTo((Equation) obj) == 0;
 		}
 		Sum normalizedSum__throwExc() throws ParseError {
 			return new Sum(left, right).normalize();			
 		}
 		Sum normalizedSum() {
 			try {
 				return normalizedSum__throwExc();
 			} catch (ParseError e) {
 				// this should not happen; we should have checked that in the constructor
 				e.printStackTrace();
 				return null;
 			}			
 		}
 		Equation normalize() { return normalizedSum().asEquation(); }
 		boolean isTautology() { return normalizedSum().isZero(); }
 		boolean equalNorm(Equation other) {
 			Sum myNorm = normalizedSum();
 			Sum otherNorm = other.normalizedSum();
 			if(myNorm.equals(otherNorm)) return true;
 			if(myNorm.equals(otherNorm.minusOne())) return true;
 			return false;
 		}
 		Iterable<String> vars() { return Utils.concatCollectionView(left.vars(), right.vars()); }
 		Equation() { left = new Utils.OperatorTree(); right = new Utils.OperatorTree(); }
 		Equation(Utils.OperatorTree left, Utils.OperatorTree right) { this.left = left; this.right = right; }
 		Equation(Utils.OperatorTree ot) throws ParseError {
 			try {
 				if(ot.entities.size() == 0) throw new ParseError("Please give me an equation.", "Bitte Gleichung eingeben.");
 				if(!ot.op.equals("=")) throw new ParseError("'=' at top level required.", "'=' benötigt.");
 				if(ot.entities.size() == 1) throw new ParseError("An equation with '=' needs two sides.", "'=' muss genau 2 Seiten haben.");
 				if(ot.entities.size() > 2) throw new ParseError("Sorry, only two parts for '=' allowed.", "'=' darf nicht mehr als 2 Seiten haben.");
 				left = ot.entities.get(0).asTree();
 				right = ot.entities.get(1).asTree();
 				normalizedSum__throwExc(); // do some checks (bad ops etc)
 			}
 			catch(ParseError e) {
 				e.ot = ot;
 				throw e;
 			}
 		}
 		Equation(String str) throws ParseError { this(Utils.OperatorTree.parse(str)); }
 		Equation(Utils.OperatorTree ot, Set<String> allowedVars) throws ParseError { this(ot); assertValidVars(allowedVars); }
 		Equation(String str, Set<String> allowedVars) throws ParseError { this(str); assertValidVars(allowedVars); }
 		
 		void assertValidVars(Set<String> allowedVars) throws ParseError {
 			for(String var : vars()) {
 				if(!allowedVars.contains(var))
 					throw new ParseError("Variable '" + var + "' is unknown.", "Die Variable '" + var + "' ist unbekannt.");
 			}
 		}
 
 		static class ParseError extends Exception {
 			private static final long serialVersionUID = 1L;
 			String english, german;
 			Utils.OperatorTree ot;
 			public ParseError(String english, String german) { super(english); this.english = english; this.german = german; }
 			public ParseError(ParseError e) { super(e.english); this.english = e.english; this.german = e.german; }
 		}
 		
 	}
 
 	void debugEquation(Utils.OperatorTree ot) {
 		ot = ot.simplify().transformMinusToPlus();
 		System.out.print("normalised input: " + ot + ", ");
 		try {
 			Equation eq = new Equation(ot, variableSymbols);
 			System.out.println("parsed: " + eq + ", normalised eq: " + eq.normalize());
 		} catch (Equation.ParseError e) {
 			System.out.println("error while parsing " + ot + ": " + e.getMessage());
 		}
 	}
 	
 	Collection<Equation> equations = new LinkedList<Equation>();
 	
 	EquationSystem() {}
 	EquationSystem(Set<String> variableSymbols) { this.variableSymbols = variableSymbols; }
 	EquationSystem(Collection<Equation> equations, Set<String> variableSymbols) {
 		this.equations = equations;
 		this.variableSymbols = variableSymbols;
 	}
 	EquationSystem extendedSystem(Collection<Equation> additionalEquations) {
 		return new EquationSystem(
 				Utils.extendedCollectionView(equations, additionalEquations),
 				variableSymbols);
 	}
 	EquationSystem extendedSystem() { return extendedSystem(new LinkedList<Equation>()); }
 	EquationSystem normalized() {
 		return new EquationSystem(
 				Utils.map(equations, new Utils.Function<Equation,Equation>() {
 					public Equation eval(Equation obj) { return obj.normalize(); }
 				}),
 				variableSymbols);
 	}
 	Iterable<Equation.Sum> normalizedSums() {
 		return Utils.map(equations, new Utils.Function<Equation,Equation.Sum>() {
 			public Equation.Sum eval(Equation obj) { return obj.normalizedSum(); }
 		});
 	}
 	Iterable<Equation.Sum> normalizedAndReducedSums() {
 		return Utils.map(normalizedSums(), new Utils.Function<Equation.Sum,Equation.Sum>() {
 			public Equation.Sum eval(Equation.Sum obj) { return obj.reduce(); }
 		});
 	}
 	
 	EquationSystem linearIndependent() {
 		EquationSystem eqSys = new EquationSystem(variableSymbols);
 		for(Equation eq : equations)
 			if(!eqSys.canConcludeTo(eq))
 				eqSys.equations.add(eq);
 		return eqSys;
 	}
 	
 	Equation add(String equStr) throws Equation.ParseError {
 		Equation eq = new Equation(equStr, variableSymbols);
 		equations.add(eq);
 		return eq;
 	}
 
 	Equation addAuto(String equStr) {
 		try {
 			Equation eq = new Equation(equStr);
 			variableSymbols.addAll(Utils.collFromIter(eq.vars()));
 			equations.add(eq);
 			return eq;
 		} catch (Equation.ParseError e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	boolean contains(Equation eq) {
 		for(Equation e : equations) {
 			if(eq.equals(e))
 				return true;
 		}
 		return false;
 	}
 	
 	boolean containsNormed(Equation eq) {
 		Equation.Sum eqSum = eq.normalizedSum();
 		for(Equation.Sum s : normalizedSums()) {
 			if(eqSum.equals(s) || eqSum.minusOne().equals(s))
 				return true;
 		}
 		return false;		
 	}
 	
 	// IMPORTANT:
 	// This resolution algorithm currently assumes that we have all variables != 0.
 	// This is often the case for electronic circuits (because otherwise we would have short circuits etc).
 	// If it is ever needed to handle the more general case, probably the function to fix would be calcAllOneSideConclusions.
 	
 	private static int debugVerbose = 0;
 	private final static int DEPTH_LIMIT = 4;
 	
 	private static boolean _canConcludeTo(Collection<Equation.Sum> baseEquations, Equation.Sum eq, Set<Equation.Sum> usedEquationList, int depth) {
 		if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "canConcludeTo: " + eq);
 
 		Set<Equation.Sum> equations = new TreeSet<Equation.Sum>(baseEquations);
 		if(equations.contains(eq)) {
 			if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "YES: eq already included");
 			return true;
 		}

		if(depth > DEPTH_LIMIT) return false;		

 		for(Equation.Sum myEq : baseEquations) {
 			Collection<Equation.Sum> allConclusions = calcAllOneSideConclusions(eq, myEq);
 			if(allConclusions.isEmpty()) {
 				if(debugVerbose >= 3) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "no conclusions with " + myEq);				
 				continue;
 			}
 			
 			equations.remove(myEq);
 			for(Equation.Sum resultingEq : allConclusions) {
 				if(usedEquationList.contains(resultingEq)) continue;
 				usedEquationList.add(resultingEq);
 				
 				if(resultingEq.isTautology()) {
 					if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "YES: conclusion with " + myEq + " gives tautology " + resultingEq);
 					return true;
 				}
 				
 				if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "conclusion with " + myEq + " : " + resultingEq);
 				if(_canConcludeTo(equations, resultingEq, usedEquationList, depth + 1)) {
 					if(debugVerbose >= 1) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "YES");
 					return true;
 				}
 			}
 			equations.add(myEq);
 		}
 		if(debugVerbose >= 2) System.out.println(Utils.multiplyString(" ", Utils.countStackFrames("_canConcludeTo")) + "NO");		
 		return false;
 	}
 	
 	Set<Utils.OperatorTree> identitiesForVar(String var) {
 		Set<Utils.OperatorTree> eqs = new TreeSet<Utils.OperatorTree>();
 		for(Equation.Sum eq : normalizedSums()) {
 			Equation.Sum.ExtractedVar extractedVar = eq.extractVar(var);
 			if(extractedVar == null) continue; // either we don't have that var there or it is an higher order polynom
 			eqs.add(extractedVar.independentPart.asOperatorTree().divide(extractedVar.varMult.asOperatorTree()).minusOne());
 		}
 		return eqs;
 	}
 	
 	boolean canConcludeTo(Equation eq) {
 		return _canConcludeTo(new TreeSet<Equation.Sum>(Utils.collFromIter(normalizedAndReducedSums())), eq.normalizedSum(), new TreeSet<Equation.Sum>(), 0);
 //		return _canConcludeTo(new TreeSet<Equation.Sum>(Utils.collFromIter(normalizedAndReducedSums())), eq.normalizedSum().reduce(), new TreeSet<Equation.Sum>(), 0);
 	}
 	
 	private static Set<String> commonVars(Equation.Sum eq1, Equation.Sum eq2) {
 		Set<String> commonVars = new HashSet<String>(Utils.collFromIter(eq1.vars()));
 		commonVars.retainAll(new HashSet<String>(Utils.collFromIter(eq2.vars())));
 		return commonVars;
 	}
 
 	private static Set<Equation.Sum> calcAllOneSideConclusions(Equation.Sum fixedEq, Equation.Sum otherEq) {
 		Set<Equation.Sum> results = new TreeSet<Equation.Sum>();
 		
 		if(fixedEq.isTautology()) return results;
 		if(otherEq.isTautology()) return results;
 
 		Set<String> commonVars = commonVars(fixedEq, otherEq);
 		
 		for(String var : commonVars) {					
 			Equation.Sum.ExtractedVar extract1 = fixedEq.extractVar(var);
 			Equation.Sum.ExtractedVar extract2 = otherEq.extractVar(var);
 			if(extract1 == null) continue; // can happen if we have higher order polynoms
 			if(extract2 == null) continue; // can happen if we have higher order polynoms
 
 			Utils.OperatorTree fac = extract1.varMult.asOperatorTree().divide(extract2.varMult.asOperatorTree()).minusOne();
 			if(debugVerbose >= 2) System.out.print("var: " + var + " in " + otherEq);
 			if(debugVerbose >= 2) System.out.print(", fac: " + fac.debugStringDouble());
 			fac = fac.mergeDivisions().simplifyDivision();
 			if(debugVerbose >= 2) System.out.println(" -> " + fac.debugStringDouble());
 			Utils.OperatorTree newSum = otherEq.asOperatorTree().multiply(fac);
 			if(debugVerbose >= 2) System.out.println("-> " + newSum + "; in " + fixedEq + " and " + otherEq + ": extracting " + var + ": " + extract1 + " and " + extract2);
 			// NOTE: here would probably the starting place to allow vars=0.
 			//if(newSum.nextDivision() != null) { if(debugVerbose >= 3) System.out.println("newSum.nextDiv != null"); continue; }
 			
 			Utils.OperatorTree resultingEquation = fixedEq.asOperatorTree().sum(newSum);
 			Equation.Sum resultingSum;
 			try {
 				resultingSum = new Equation.Sum(resultingEquation, Utils.OperatorTree.Zero());
 			} catch (Equation.ParseError e) {
 				e.printStackTrace(); // should not happen
 				continue;
 			}
 			if(debugVerbose >= 3) System.out.println(".. result: " + resultingEquation + " // " + resultingSum + " // " + resultingSum.normalize());
 			results.add(resultingSum.normalize());
 		}
 		
 		return results;
 	}
 		
 	void dump() {
 		System.out.println("equations: [");
 		for(Equation e : equations)
 			System.out.println("  " + e + " ,");
 		System.out.println(" ]");
 	}
 
 	void assertCanConcludeTo(String eqStr) throws Equation.ParseError {
 		Equation eq = new Equation(eqStr, variableSymbols);
 		if(!canConcludeTo(eq)) {
 			System.out.println("Error: assertCanConcludeTo failed.");
 			System.out.println("system:");
 			for(Equation e : equations)
 				System.out.println("  " + e  + " // " + e.normalize() + " // " + e.normalizedSum());
 
 			System.out.println("equation:");
 			debugEquationParsing(eqStr);
 			debugVerbose = 3;
 			canConcludeTo(eq);			
 			debugVerbose = 0;
 
 			throw new AssertionError("must follow: " + eq + " ; (as normed sum: " + eq.normalizedSum() + ")");
 		}
 	}
 
 	void assertCanNotConcludeTo(String eqStr) throws Equation.ParseError {
 		Equation eq = new Equation(eqStr, variableSymbols);
 		if(canConcludeTo(eq)) {
 			System.out.println("Error: assertCanNotConcludeTo failed.");
 			System.out.println("normed system:");
 			for(Equation e : equations)
 				System.out.println("  " + e.normalize());
 			
 			System.out.println("equation:");
 			debugEquationParsing(eqStr);
 			debugVerbose = 3;
 			canConcludeTo(eq);			
 			debugVerbose = 0;
 
 			throw new AssertionError("must not follow: " + eq);
 		}
 	}
 
 	void assertAndAdd(String eqStr) throws Equation.ParseError {
 		assertCanConcludeTo(eqStr);
 		add(eqStr);
 	}
 		
 	static void debugEquationParsing(String equ) throws Equation.ParseError {
 		Utils.OperatorTree ot = Utils.OperatorTree.parse(equ);
 		if(!ot.op.equals("=")) throw new Equation.ParseError("'" + equ + "' must have '=' at the root.", "");
 		if(ot.entities.size() != 2) throw new Equation.ParseError("'" + equ + "' must have '=' with 2 sides.", "");		
 		Utils.OperatorTree left = ot.entities.get(0).asTree(), right = ot.entities.get(1).asTree();
 
 		System.out.println("equ (" + left.debugStringDouble() + ") = (" + right.debugStringDouble() + "): {");
 
 		ot = Utils.OperatorTree.MergedEquation(left, right);
 		System.out.println("  merge: " + ot.debugStringDouble());
 		
 		ot = ot.transformMinusToPlus();
 		System.out.println("  transformMinusToPlus: " + ot.debugStringDouble());
 
 		ot = ot.simplify();
 		System.out.println("  simplify: " + ot.debugStringDouble());
 		
 		ot = ot.transformMinusPushedDown();
 		System.out.println("  transformMinusPushedDown: " + ot.debugStringDouble());
 		
 		ot = ot.multiplyAllDivisions();
 		System.out.println("  multiplyAllDivisions: " + ot.debugStringDouble());
 		
 		ot = ot.pushdownAllMultiplications();
 		System.out.println("  pushdownAllMultiplications: " + ot.debugStringDouble());
 
 		System.out.println("}");
 	}
 	
 	static void assertEqual(Utils.OperatorTree a, Utils.OperatorTree b) {
 		if(!a.toString().equals(b.toString()))
 			throw new AssertionError("not equal: " + a + " and " + b);
 	}
 	
 	static void assertEqual(Utils.OperatorTree.Entity a, Utils.OperatorTree.Entity b) {
 		if(!a.equals(b))
 			throw new AssertionError("not equal: " + a + " and " + b);
 	}
 
 	static void assertEqual(Equation.Sum a, String b) {
 		if(!a.toString().equals(b))
 			throw new AssertionError("not equal: " + a + " and " + b);
 	}
 
 	static void debugSimplifications() throws Equation.ParseError {
 		assertEqual(new Equation("U1 / I1 = 1 / (1 / R2 + 1 / R3)").normalizedSum(), "I1 ∙ R2 ∙ R3 + -R2 ∙ U1 + -R3 ∙ U1");
 		assertEqual(Utils.OperatorTree.parse("-I1"), Utils.OperatorTree.parse("-I1"));
 		assertEqual(Utils.OperatorTree.parse("-I1").unaryPrefixedContent(), Utils.OperatorTree.parse("-I1").unaryPrefixedContent());
 		assertEqual(Utils.OperatorTree.parse("-I1 / -I1").mergeDivisions().simplifyDivision(), Utils.OperatorTree.One());
 		assertEqual(Utils.OperatorTree.parse("(-R1 + -R4) / (-R1 + -R4)").mergeDivisions().simplifyDivision(), Utils.OperatorTree.One());
 		assertEqual(Utils.OperatorTree.parse("-1 / -x").mergeDivisions().simplifyDivision(), Utils.OperatorTree.parse("1 / x"));
 		assertEqual(Utils.OperatorTree.parse("(I1 ∙ U3) / I1").mergeDivisions().simplifyDivision(), Utils.OperatorTree.parse("U3"));
 		assertEqual(Utils.OperatorTree.parse("(I2 ∙ I3) / -I2").mergeDivisions().simplifyDivision(), Utils.OperatorTree.parse("-I3"));
 		assertEqual(Utils.OperatorTree.parse("-R2 ∙ U3 / R2").mergeDivisions().simplifyDivision(), Utils.OperatorTree.parse("-U3"));
 		assertEqual(Utils.OperatorTree.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1").mergeDivisions(), Utils.OperatorTree.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1"));
 		assertEqual(Utils.OperatorTree.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1").mergeDivisions().simplifyDivision(), Utils.OperatorTree.parse("-I1 ∙ R1 + -I1 ∙ R4 + I2 ∙ R2 + -1"));
 		assertEqual(Utils.OperatorTree.parse("-I2 ∙ (U3 / -I2)").mergeDivisions().simplifyDivision(), Utils.OperatorTree.parse("U3"));		
 		assertEqual(Utils.OperatorTree.parse("(R1 ∙ R2 ∙ U3 + R2 ∙ R4 ∙ U3) / (-R2 ∙ U3)").simplifyDivision(), Utils.OperatorTree.parse("-R1 + -R4"));
 	}
 	
 	static void debug() {
 		EquationSystem sys = new EquationSystem();
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("x" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("U" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("R" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("I" + i);		
 		for(int i = 1; i <= 10; ++i) sys.registerVariableSymbol("C" + i);
 		for(Character c : Utils.iterableString("QUC")) sys.registerVariableSymbol("" + c);
 		try {
 			//debugEquationParsing("x3 - x4 - x5 = 0");
 			//debugEquationParsing("x1 * x5 = x2 * x5 + x3 * x5");
 			//debugEquationParsing("U3 = I1 * (R1 + R4)");
 			//debugEquationParsing("U3 / I3 = R2 - R2 * I1 / (I1 + I2)");
 			//debugEquationParsing("U1 / I1 = 1 / (1 / R2 + 1 / R3)");
 			debugSimplifications();
 			
 			sys.add("Q = C1 * U1");
 			sys.add("Q = C2 * U2");
 			sys.add("Q = C3 * U3");
 			sys.add("Q = C * U");
 			sys.add("U = U1 + U2 + U3");
 			sys.assertAndAdd("U1 = Q / C1");
 			sys.assertAndAdd("U2 = Q / C2");
 			sys.assertAndAdd("U3 = Q / C3");
 			sys.assertAndAdd("U = Q / C");
			sys.assertAndAdd("Q / C = U1 + U2 + U3");
			sys.assertAndAdd("Q / C = Q / C1 + U2 + U3");
			sys.assertAndAdd("Q / C = Q / C1 + Q / C2 + U3");
 			sys.assertAndAdd("Q / C = Q / C1 + Q / C2 + Q / C3");
 			sys.assertCanConcludeTo("1 / C = 1 / C1 + 1 / C2 + 1 / C3");
 			sys.equations.clear();
 
 			sys.add("U1 / I1 = U1 / (U1 / R2 + U1 / R3)");
 			sys.assertCanConcludeTo("U1 / I1 = 1 / (1 / R2 + 1 / R3)");
 			sys.equations.clear();
 			
 			sys.add("x3 - x4 - x5 = 0");
 			sys.add("-x2 + x5 = 0");
 			sys.add("-x1 + x2 = 0");
			sys.assertAndAdd("-x1 + x2 + x3 - x4 - x5 = 0"); // this step is needed if we have DEPTH_LIMIT=1
 			sys.assertCanConcludeTo("x1 - x3 + x4 = 0");
 			sys.assertCanNotConcludeTo("x1 = 0");
 			sys.equations.clear();
 			
 			sys.add("x1 * x2 = 0");
 			sys.add("x1 = x3");
 			sys.assertCanConcludeTo("x2 * x3 = 0");			
 			//sys.assertCanNotConcludeTo("x2 * x4 = 0"); // NOTE: this fails because the assumption that all vars!=0 does not hold!
 			sys.assertCanNotConcludeTo("x1 = x2");			
 			//sys.assertCanNotConcludeTo("x1 = 0"); // NOTE: this fails because the assumption that all vars!=0 does not hold!
 			sys.equations.clear();
 
 			sys.add("x1 = x2 + x3");
 			sys.assertCanConcludeTo("x1 / x5 = (x2 + x3) / x5");			
 			sys.assertCanConcludeTo("x1 * x5 = x2 * x5 + x3 * x5");
 			sys.equations.clear();
 
 			sys.add("x1 + x2 * x3 + x4 + x5 * x6 = 0");
 			sys.equations.clear();
 			
 			sys.add("x1 = x2 ∙ x3 - x4 - x5");
 			sys.assertCanConcludeTo("x1 / x2 = x3 - x4 / x2 - x5 / x2");
 			sys.equations.clear();
 
 			sys.add("x1 = x2 ∙ x3 - x4 - x5");
 			sys.add("x6 = x2");
 			sys.assertCanConcludeTo("x1 / x6 = x3 - x4 / x6 - x5 / x6");
 			sys.equations.clear();
 
 			sys.add("U3 = R2 ∙ I2");
 			sys.add("I3 = I4 + I2");
 			sys.add("U3 / I3 = R2 ∙ I2 / I3");
 			sys.assertCanConcludeTo("I2 = U3 / R2");
 			sys.assertCanConcludeTo("U3 / I3 = R2 ∙ I2 / (I4 + I2)");
 			sys.equations.clear();
 			
 			sys.add("I2 = I4");
 			sys.add("I4 = I3");
 			sys.assertCanConcludeTo("I3 = I2");
 			sys.equations.clear();
 			
 			sys.add("I1 = I3");
 			sys.add("I2 = I3");
 			sys.add("U2 = R1 ∙ I1 + R3 ∙ I3");
 			sys.assertCanConcludeTo("I1 = I2");
 			sys.equations.clear();
 			
 			sys.add("U3 / I3 = R2 - R2 / (1 + U3 / (R2 ∙ I1))"); // I1 ∙ R2 ∙ U3 + -I3 ∙ R2 ∙ U3 + U3^2
 			sys.add("I1 = U3 / (R1 + R4)"); // I1 ∙ R1 + I1 ∙ R4 + -U3
 			sys.assertCanConcludeTo("U3 / I3 = R2 - R2 / (1 + U3 / (R2 * U3 / (R1 + R4)))"); // -I3 ∙ R1 ∙ R2 ∙ U3 + -I3 ∙ R2 ∙ R4 ∙ U3 + R1 ∙ U3^2 + R2 ∙ U3^2 + R4 ∙ U3^2
 			sys.equations.clear();
 			
 			sys.add("U3 = R2 * I2");
 			sys.add("U3 = R4 * I4 + R1 * I1");
 			sys.add("I4 = I1");
 			sys.assertAndAdd("U3 = I1 * (R1 + R4)");
 			sys.assertAndAdd("I2 = U3 / R2");
 			sys.assertAndAdd("I1 = U3 / (R1 + R4)");
 			sys.assertAndAdd("I2 / I1 = (U3 / R2) / (U3 / (R1 + R4))");
 			sys.assertAndAdd("I2 / I1 = (R1 + R4) / R2");
 			sys.add("I1 + I2 = I3");
 			sys.assertAndAdd("U3 = R2 * (I3 - I1)");
 			sys.assertAndAdd("U3/I3 = R2 - R2 * I1 / I3");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 * I1 / (I1 + I2)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + I2 / I1)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + (U3 / R2) / I1)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + U3 / (R2 * I1))");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + U3 / (R2 * U3 / (R1 + R4)))");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 / (1 + (R1 + R4) / R2)");
 			sys.assertAndAdd("U3 / I3 = R2 - R2 * R2 / (R2 + R1 + R4)");
 			sys.assertAndAdd("U3 / I3 = (R2*R2 + R2*R1 + R2*R4 - R2*R2) / (R2 + R1 + R4)");
 			sys.assertAndAdd("U3 / I3 = (R2*R1 + R2*R4) / (R2 + R1 + R4)");
 			sys.assertAndAdd("U3 / I3 = (R2 * (R1 + R4)) / (R2 + (R1 + R4))");
 			sys.equations.clear();

			// same thing as above. just a test if we can conclude it directly. 
			sys.add("U3 = R2 * I2");
			sys.add("U3 = R4 * I4 + R1 * I1");
			sys.add("I4 = I1");
			sys.add("I1 + I2 = I3");
			sys.assertCanConcludeTo("U3 / I3 = (R2 * (R1 + R4)) / (R2 + (R1 + R4))");
			sys.equations.clear();

 			sys.add("x1 = x2");
 			sys.add("x2 * x3 = x4");
 			sys.assertCanConcludeTo("x1 = x4 / x3");
 			sys.equations.clear();
 
 			sys.add("x1 = x4 / x3"); // -> x1 * x3 = x4
 			sys.add("x2 * x3 = x4");
 			// NOTE: again, this fails because we assume that we always have all vars != 0
 			//sys.assertCanNotConcludeTo("x1 = x2"); // not because we also need x3 != 0
 			sys.equations.clear();
 
 		} catch (Equation.ParseError e) {
 			System.out.println("Error: " + e.english);
 			System.out.println(" in " + e.ot.debugStringDouble());
 			try {
 				debugEquationParsing(e.ot.toString());
 			} catch (Equation.ParseError e1) {}
 			e.printStackTrace(System.out);
 			
 		} catch (Throwable e) {
 			e.printStackTrace(System.out);
 		}		
 	}
 }
