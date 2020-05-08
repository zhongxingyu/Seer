 // ----------------------------------------------------------------------------
 // Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.asSemantics.Unifier;
 import jason.asSyntax.parser.as2j;
 
 import java.io.StringReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
 * Represents and solve arithmetic expressions like "10 + 30".
  */
 public class ArithExpr extends DefaultTerm implements NumberTerm {
 
 	private static final long serialVersionUID = 1L;
     private static Logger logger = Logger.getLogger(ArithExpr.class.getName());
 	
 	public enum ArithmeticOp {
         none {
             double eval(double x, double y) {
                 return 0;
             }
 
             public String toString() {
                 return "";
             }
         },
         plus {
             double eval(double x, double y) {
                 return x + y;
             }
 
             public String toString() {
                 return "+";
             }
         },
         minus {
             double eval(double x, double y) {
                 return x - y;
             }
 
             public String toString() {
                 return "-";
             }
         },
         times {
             double eval(double x, double y) {
                 return x * y;
             }
 
             public String toString() {
                 return "*";
             }
         },
         div {
             double eval(double x, double y) {
                 return x / y;
             }
 
             public String toString() {
                 return "/";
             }
         },
         mod {
             double eval(double x, double y) {
                 return x % y;
             }
 
             public String toString() {
                 return " mod ";
             }
         },
         pow {
             double eval(double x, double y) {
                 return Math.pow(x, y);
             }
 
             public String toString() {
                 return "**";
             }
         },
         intdiv {
             double eval(double x, double y) {
                 return (int) x / (int) y;
             }
 
             public String toString() {
                 return " div ";
             }
         };
 
         abstract double eval(double x, double y);
     }
 
     private NumberTerm    lhs, rhs;
     private ArithmeticOp  op     = ArithmeticOp.none;
     private NumberTerm    fValue = null; // value, when evaluated	
 
     private ArithExpr() {
         super();
     }
 
     public ArithExpr(NumberTerm t1, ArithmeticOp oper, NumberTerm t2) {
         lhs = t1;
         op = oper;
         rhs = t2;
     }
 
     public ArithExpr(ArithmeticOp oper, NumberTerm t1) {
         op = oper;
         lhs = t1;
     }
 
     /** returns some Term that can be evaluated as Number */
     public static NumberTerm parseExpr(String sExpr) {
         as2j parser = new as2j(new StringReader(sExpr));
         try {
             return (NumberTerm) parser.arithm_expr();
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Error parsing expression " + sExpr, e);
             return null;
         }
     }
 
     /** returns true if the expression was already evaluated */
     public boolean isEvaluated() {
         return lhs == null;
     }
     
     /** 
      *  Set the value of this expression by calling solve(). After this method execution,
      *  the object behaviour is like a contant number.
      */
     public void evaluate() {
     	fValue = new NumberTermImpl(solve());
         lhs = null;
         rhs = null;
         super.resetHashCodeCache();
     }
     
     public boolean apply(Unifier u) {
     	if (isEvaluated()) return false;
     	
         getLHS().apply(u);
         if (!isUnary()) {
             getRHS().apply(u);
         }
         evaluate();
         
     	return true;
     }
     
     
     /** make a hard copy of the terms */
     public Object clone() {
         if (isEvaluated()) {
             return fValue;
         } else {
             ArithExpr t = new ArithExpr();
             if (lhs != null) {
                 t.lhs = (NumberTerm) lhs.clone();
             }
 
             t.op = this.op;
 
             if (rhs != null) {
                 t.rhs = (NumberTerm) rhs.clone();
             }
             return t;
         }
     }
 
     @Override
     public boolean equals(Object t) {
         if (t == null) return false;
         if (isEvaluated()) return fValue.equals(t);
         if (t instanceof ArithExpr) {
             ArithExpr eprt = (ArithExpr) t;
             if (lhs == null && eprt.lhs != null) return false;
             if (lhs != null && !lhs.equals(eprt.lhs)) return false;
             if (op != eprt.op) return false;
             if (rhs == null && eprt.rhs != null) return false;
             if (rhs != null && !rhs.equals(eprt.rhs)) return false;
             return true;
         }
         return false;
     }
 
     @Override
     public int compareTo(Term o) {
         try {
             NumberTerm st = (NumberTerm)o;
             if (solve() > st.solve()) return 1;
             if (solve() < st.solve()) return -1;
         } catch (Exception e) {}
         return 0;    
     }
 
     @Override
     protected int calcHashCode() {
         if (isEvaluated()) return fValue.hashCode();
         
         final int PRIME = 31;
         int code = PRIME * op.hashCode();
         if (lhs != null) code = PRIME * code + lhs.hashCode();
         if (rhs != null) code = PRIME * code + rhs.hashCode();
         return code;
     }
     
     
     /** gets the Operation of this Expression */
     public ArithmeticOp getOp() {
         return op;
     }
 
     /** gets the LHS of this Expression */
     public NumberTerm getLHS() {
         return lhs;
     }
 
     /** gets the RHS of this Expression */
     public NumberTerm getRHS() {
         return rhs;
     }
 
 	@Override
 	public boolean isNumeric() {
 		return true;
 	}
 
     @Override
     public boolean isArithExpr() {
         return !isEvaluated();
     }
 
     public boolean isUnary() {
         return rhs == null;
     }
 
     @Override
     public boolean isGround() {
         return isEvaluated() || (lhs.isGround() && (rhs == null || rhs.isGround()));
     }
 
     public double solve() {
         if (isEvaluated()) {
             // this expr already has a value
             return fValue.solve();
         }
         double l = lhs.solve();
         if (rhs == null && op == ArithmeticOp.minus) {
             return -l;
         } else if (rhs != null) {
             double r = rhs.solve();
             return op.eval(l, r);
         }
         logger.log(Level.SEVERE, "ERROR IN EXPRESION!");
         return 0;
     }
 
     @Override
     public String toString() {
         if (isEvaluated()) {
             return fValue.toString();
         } else {
             if (rhs == null) {
                 return "(" + op + lhs + ")";
             } else {
                 return "(" + lhs + op + rhs + ")";
             }
         }
     }
 
     /** get as XML */
     public Element getAsDOM(Document document) {
         if (isEvaluated()) {
             return fValue.getAsDOM(document);
         } else {
             Element u = (Element) document.createElement("expression");
             u.setAttribute("type", "arithmetic");
             u.setAttribute("operator", op.toString());
             if (isUnary()) {
                 Element r = (Element) document.createElement("right");
                 r.appendChild(lhs.getAsDOM(document)); // put the left argument indeed!
                 u.appendChild(r);
             } else {
                 Element l = (Element) document.createElement("left");
                 l.appendChild(lhs.getAsDOM(document));
                 u.appendChild(l);
                 Element r = (Element) document.createElement("right");
                 r.appendChild(rhs.getAsDOM(document));
                 u.appendChild(r);
             }
             return u;
         }
     }
 }
