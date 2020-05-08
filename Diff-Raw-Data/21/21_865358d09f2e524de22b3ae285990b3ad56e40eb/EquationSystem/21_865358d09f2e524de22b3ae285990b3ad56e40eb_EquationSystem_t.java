 package applets.Termumformungen$in$der$Technik_01_URI;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
import java.util.TreeMap;
 
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
 	
 	static class VariableSymbol implements Comparable<VariableSymbol> {
 		Unit unit;
 		String name;
 		VariableSymbol(String unit) { this.unit = new Unit(unit); }
 		VariableSymbol(String name, String unit) { this.name = name; this.unit = new Unit(unit); }
 		public int compareTo(VariableSymbol o) { if(this == o) return 0; return name.compareTo(o.name); }		
 	}
 	
 	Map<String, VariableSymbol> variableSymbols = new HashMap<String, VariableSymbol>();	
 	void registerVariableSymbol(VariableSymbol var) { variableSymbols.put(var.name, var); }	
 	void registerVariableSymbol(String name, String unit) { registerVariableSymbol(new VariableSymbol(name, unit)); }
 	
 	static class Equation implements Comparable<Equation> {
 		static class FracSum implements Comparable<FracSum> {
 			static class Frac implements Comparable<Frac> {
 				static class Sum implements Comparable<Sum> {
 					static class Prod implements Comparable<Prod> {
 						int fac = 1;
 						static class Pot implements Comparable<Pot> {
 							VariableSymbol sym;
 							int pot = 1;
 							@Override public String toString() { return sym.name + ((pot != 1) ? ("^" + pot) : ""); }		
 							Pot(VariableSymbol s) { sym = s; }
 							Pot(VariableSymbol s, int p) { sym = s; pot = p; }
 							public int compareTo(Pot o) { if(sym != o.sym) return sym.compareTo(o.sym); return pot - o.pot; }
 							@Override public int hashCode() {
 								int result = 1;
 								result = 31 * result + pot;
 								result = 31 * result + ((sym == null) ? 0 : sym.hashCode());
 								return result;
 							}
 							@Override public boolean equals(Object obj) {
 								if (this == obj) return true;
 								if (obj == null) return false;
 								if (getClass() != obj.getClass()) return false;
 								Pot other = (Pot) obj;
 								if (pot != other.pot) return false;
 								return sym == other.sym;
 							}
 						}						
 						List<Pot> facs = new LinkedList<Pot>();
 						public int compareTo(Prod o) {
 							int r = Utils.<Pot>orderOnCollection().compare(facs, o.facs);
 							if(r != 0) return r;
 							return fac - o.fac;
 						}
 						@Override public String toString() { return ((fac != 1) ? "" + fac + " ∙ " : "") + Utils.concat(facs, " ∙ "); }
 						Prod normalize() {
 							Prod prod = new Prod();
 							prod.fac = fac;
							Map<VariableSymbol,Integer> vars = new TreeMap<VariableSymbol,Integer>();
 							for(Pot p : facs) {
 								if(!vars.containsKey(p.sym)) vars.put(p.sym, 0);
 								vars.put(p.sym, vars.get(p.sym) + p.pot);
 							}
 							for(VariableSymbol sym : vars.keySet()) {
 								if(vars.get(sym) != 0)
 									prod.facs.add(new Pot(sym, vars.get(sym)));
 							}
 							return prod;
 						}
 						Prod minusOne() { return new Prod(-fac, facs); }
 						Prod() {}
 						Prod(int fac, List<Pot> facs) { this.fac = fac; this.facs = facs; }
 						Prod(Utils.OperatorTree ot, Map<String,VariableSymbol> vars) throws ParseError { parse(ot, vars); }
 						void parse(Utils.OperatorTree ot, Map<String,VariableSymbol> vars) throws ParseError {
 							if(ot.canBeInterpretedAsUnaryPrefixed() && ot.op.equals("-")) {
 								fac = -fac;
 								parse(ot.unaryPrefixedContent().asTree(), vars);
 							}
 							else if(ot.op.equals("∙") || ot.entities.size() <= 1) {
 								for(Utils.OperatorTree.Entity e : ot.entities) {
 									if(e instanceof Utils.OperatorTree.RawString) {
 										String s = ((Utils.OperatorTree.RawString) e).content;
 										try {
 											fac *= Integer.parseInt(s);
 										}
 										catch(NumberFormatException ex) {
 											VariableSymbol var = vars.get(s);
 											if(var != null)
 												facs.add(new Pot(var));
 											else
 												throw new ParseError("Variable '" + s + "' is unknown.");
 										}
 									}
 									else
 										parse( ((Utils.OperatorTree.Subtree) e).content, vars );
 								}
 							}
 							else
 								throw new ParseError("'" + ot + "' must be a product.");
 							
 							if(facs.isEmpty())
 								throw new ParseError("'" + ot + "' must contain at least one variable.");
 						}
 					}
 					List<Prod> entries = new LinkedList<Prod>();
 					@Override public String toString() { return ((entries.size() > 1) ? "(" : "") + Utils.concat(entries, " + ") + ((entries.size() > 1) ? ")" : ""); }
 					public int compareTo(Sum o) { return Utils.<Prod>orderOnCollection().compare(entries, o.entries); }
 					boolean isEmpty() { return entries.isEmpty(); }
 					Sum normalize() {
 						Sum sum = new Sum();
 						List<Prod> newEntries = new LinkedList<Prod>();
 						for(Prod prod : entries)
 							newEntries.add(prod.normalize());
 						Collections.sort(newEntries);
 						Prod lastProd = null;
 						for(Prod prod : newEntries) {
 							if(lastProd != null && lastProd.facs.equals(prod.facs))
 								lastProd.fac += prod.fac;
 							else {
 								lastProd = prod;
 								sum.entries.add(lastProd);
 							}
 						}
 						for(Iterator<Prod> pit = sum.entries.iterator(); pit.hasNext();) {
 							if(pit.next().fac == 0)
 								pit.remove();
 						}
 						return sum;
 					}
 					Sum minusOne() {
 						Sum sum = new Sum();
 						for(Prod prod : entries) sum.entries.add(prod.minusOne());
 						return sum;
 					}
 					Sum() {}
 					Sum(Utils.OperatorTree ot, Map<String,VariableSymbol> vars) throws ParseError {
 						if(ot.op.equals("+") || ot.entities.size() <= 1) {
 							for(Utils.OperatorTree.Entity e : ot.entities)
 								entries.add( new Prod(e.asTree(), vars) );
 						}
 						else
 							entries.add( new Prod(ot, vars) );
 					}
 				}
 				Sum numerator = new Sum(), denominator = null;
 				@Override public String toString() { return numerator.toString() + ((denominator != null) ? " / " + denominator.toString() : ""); }
 				boolean hasEqualDenominatorAs(Frac f) {
 					if(denominator == null && f.denominator == null) return true;
 					if(denominator != null && f.denominator == null) return false;
 					if(denominator == null && f.denominator != null) return false;
 					return denominator.equals(f.denominator);
 				}
 				public int compareTo(Frac o) {
 					if(denominator != null && o.denominator == null) return 1;
 					if(denominator == null && o.denominator != null) return -1;
 					if(denominator != null && o.denominator != null) {
 						int r = denominator.compareTo(o.denominator);
 						if(r != 0) return r;
 					}
 					return numerator.compareTo(o.numerator);					
 				}
				Frac normalize() { return new Frac(numerator.normalize(), (denominator != null) ? denominator.normalize() : null); }
 				Frac minusOne() { return new Frac(numerator.minusOne(), denominator); }
 				Frac() {}
 				Frac(Sum numerator, Sum denominator) { this.numerator = numerator; this.denominator = denominator; }
 				Frac(Utils.OperatorTree ot, Map<String,VariableSymbol> vars) throws ParseError {
 					if(!ot.op.equals("/"))
 						numerator = new Sum(ot, vars);
 					else {
 						if(ot.entities.size() != 2) throw new ParseError("The fraction '" + ot + "' must be of the form 'a / b'.");
 						numerator = new Sum(ot.entities.get(0).asTree(), vars);
 						denominator = new Sum(ot.entities.get(1).asTree(), vars);
 					}
 				}
 			}
 			List<Frac> entries = new LinkedList<Frac>();
			@Override public String toString() { return entries.isEmpty() ? "0" : Utils.concat(entries, " + "); }
 			public int compareTo(FracSum o) { return Utils.<Frac>orderOnCollection().compare(entries, o.entries); }
 			FracSum normalize() {
 				List<Frac> newEntries = new LinkedList<Frac>();
 				for(Frac f : entries) newEntries.add(f.normalize());
 				Collections.sort(newEntries);
 				FracSum sum = new FracSum();
 				Frac lastFrac = null;
 				for(Frac f : newEntries) {
 					if(lastFrac != null && lastFrac.hasEqualDenominatorAs(f)) {
 						lastFrac.numerator.entries.addAll(f.numerator.entries);
 						lastFrac.numerator = lastFrac.numerator.normalize();
 					}
 					else {
 						lastFrac = f;
 						sum.entries.add(lastFrac);
 					}
 				}
 				for(Iterator<Frac> fit = sum.entries.iterator(); fit.hasNext();) {
 					if(fit.next().numerator.isEmpty())
 						fit.remove();
 				}
 				return sum;
 			}
 			FracSum minusOne() {
 				FracSum sum = new FracSum();
 				for(Frac f : entries) sum.entries.add(f.minusOne());
 				return sum;
 			}
 			FracSum() {}
 			FracSum(Utils.OperatorTree ot, Map<String,VariableSymbol> vars) throws ParseError {
 				if(ot.op.equals("+") || ot.entities.size() <= 1) {
 					for(Utils.OperatorTree.Entity e : ot.entities)
 						entries.add( new Frac(e.asTree(), vars) );
 				}
 				else
 					entries.add( new Frac(ot, vars) );
 			}
 		}
 		FracSum left = new FracSum(), right = new FracSum();		
 		@Override public String toString() { return left.toString() + " = " + right.toString(); }
 		public int compareTo(Equation o) {
 			int r = left.compareTo(o.left); if(r != 0) return r;
 			return right.compareTo(o.right);
 		}
 		Equation normalize() {
 			Equation eq = new Equation();
 			eq.left.entries.addAll(left.entries);
 			eq.left.entries.addAll(right.minusOne().entries);
 			eq.left = eq.left.normalize();
 			return eq;
 		}
 		Equation() {}
 		Equation(Utils.OperatorTree ot, Map<String,VariableSymbol> vars) throws ParseError {
 			if(ot.entities.size() == 0) throw new ParseError("Please give me an equation.");
 			if(!ot.op.equals("=")) throw new ParseError("'=' at top level required.");
 			if(ot.entities.size() == 1) throw new ParseError("An equation with '=' needs two sides.");
 			if(ot.entities.size() > 2) throw new ParseError("Sorry, only two parts for '=' allowed.");
 			left = new FracSum(ot.entities.get(0).asTree(), vars);
 			right = new FracSum(ot.entities.get(1).asTree(), vars);			
 		}		
 
 		static class ParseError extends Exception {
 			private static final long serialVersionUID = 1L;
 			public ParseError(String msg) { super(msg); }			
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
 	
 	static void debugEquationParsing() {
 		
 	}
 
 	List<Equation> equations = new LinkedList<Equation>();
 	
 }
