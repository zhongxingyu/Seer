 /*
  * $Id$
  */
 
 package edu.jas.poly;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import java.io.StreamTokenizer;
 import java.io.Reader;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 
 import edu.jas.structure.RingElem;
 import edu.jas.structure.RingFactory;
 
 import edu.jas.arith.BigRational;
 import edu.jas.arith.ModInteger;
 import edu.jas.arith.BigInteger;
 import edu.jas.arith.BigComplex;
 import edu.jas.arith.BigQuaternion;
 
 import edu.jas.poly.GenPolynomial;
 import edu.jas.poly.GenPolynomialRing;
 import edu.jas.poly.PolynomialList;
 import edu.jas.poly.OrderedPolynomialList;
 
 import edu.jas.poly.GenSolvablePolynomial;
 import edu.jas.poly.GenSolvablePolynomialRing;
 
 import edu.jas.module.ModuleList;
 import edu.jas.module.OrderedModuleList;
 
 /**
  * GenPolynomial Tokenizer. 
  * Used to read rational polynomials and lists from input streams.
  * @author Heinz Kredel
  */
 
 public class GenPolynomialTokenizer  {
 
     private static final Logger logger 
             = Logger.getLogger(GenPolynomialTokenizer.class);
 
     private String[] vars;
     private int nvars = 1;
     private TermOrder tord;
     private RelationTable table;
     private Reader in;
     private StreamTokenizer tok;
     private boolean debug = false; // even more debugging
 
 
     private RingFactory                                fac;
     //private RingFactory<AlgebraicNumber<BigRational>> anfac;
     //private RingFactory<AlgebraicNumber<ModInteger>>  gffac;
     private enum coeffType { BigRat, BigInt, ModInt, BigC, BigQ, AN, GF };
     private coeffType parsedCoeff = coeffType.BigRat;
 
 
     private GenPolynomialRing                pfac;
     private enum polyType { PolBigRat, PolBigInt, PolModInt, PolBigC, 
                             PolBigQ, PolAN, PolGF };
     private polyType parsedPoly = polyType.PolBigRat;
 
     private GenSolvablePolynomialRing        spfac;
 
 
 
     /**
      * noargs constructor
      */
 
     public GenPolynomialTokenizer() {
         this( new BufferedReader( new InputStreamReader( System.in ) ) );
     }
 
 
     /**
      * constructor with Ring and Reader
      */
     public GenPolynomialTokenizer(GenPolynomialRing rf, Reader r) {
         this(r);
         if ( rf == null ) {
             return;
         }
         if ( rf instanceof GenSolvablePolynomialRing ) {
           pfac = null;
            spfac = (GenSolvablePolynomialRing)rf;
         } else {
           pfac = rf;
           spfac = null;
         }
         fac = rf.coFac;
         vars = rf.vars;
         if ( vars != null ) {
             nvars = vars.length;
         }
         tord = rf.tord;
         // relation table
         if ( spfac != null ) {
             table = spfac.table;
         } else {
             table = null;
         }
     }
 
 
     /**
      * constructor with Reader
      */
     public GenPolynomialTokenizer(Reader r) {
         vars = null;
         tord = new TermOrder();
         in = r;
         // table = rt;
         nvars = 1;
         if ( vars != null ) {
             nvars = vars.length;
         }
         fac = null;
         fac = new BigRational(1);
         
         pfac = null;
         pfac = new GenPolynomialRing<BigRational>(fac,nvars,tord,vars);
 
         spfac = null;
         spfac = new GenSolvablePolynomialRing<BigRational>(fac,nvars,tord,vars);
 
         tok = new StreamTokenizer( r );
         tok.resetSyntax();
         // tok.eolIsSignificant(true); no more
         tok.eolIsSignificant(false);
         tok.wordChars('0','9');
         tok.wordChars('a', 'z');
         tok.wordChars('A', 'Z');
         tok.wordChars('/', '/'); // wg. rational numbers
         tok.wordChars(128 + 32, 255);
         tok.whitespaceChars(0, ' ');
         tok.commentChar('#');
         tok.quoteChar('"');
         tok.quoteChar('\'');
         //tok.slashStarComments(true); does not work
 
     }
 
 
     /**
      * initialize coefficient and polynomial factories
      */
     public void initFactory( RingFactory rf, coeffType ct) {
         fac = rf;
         parsedCoeff = ct;
 
         switch ( ct ) {
         case BigRat: 
              pfac  = new GenPolynomialRing<BigRational>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigRat;
              break;
         case BigInt: 
              pfac  = new GenPolynomialRing<BigInteger>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigInt;
              break;
         case ModInt: 
              pfac = new GenPolynomialRing<ModInteger>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolModInt;
              break;
         case BigC: 
              pfac  = new GenPolynomialRing<BigComplex>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigC;
              break;
         case BigQ: 
              pfac  = new GenPolynomialRing<BigQuaternion>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigQ;
              break;
         default: 
              pfac  = new GenPolynomialRing<BigRational>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigRat;
         }
     }
 
 
     /**
      * initialize coefficient and solvable polynomial factories
      */
     public void initSolvableFactory( RingFactory rf, coeffType ct) {
         fac = rf;
         parsedCoeff = ct;
 
         switch ( ct ) {
         case BigRat: 
              spfac  = new GenSolvablePolynomialRing<BigRational>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigRat;
              break;
         case BigInt: 
              spfac  = new GenSolvablePolynomialRing<BigInteger>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigInt;
              break;
         case ModInt: 
              spfac = new GenSolvablePolynomialRing<ModInteger>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolModInt;
              break;
         case BigC: 
              spfac  = new GenSolvablePolynomialRing<BigComplex>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigC;
              break;
         case BigQ: 
              spfac  = new GenSolvablePolynomialRing<BigQuaternion>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigQ;
              break;
         default: 
              spfac  = new GenSolvablePolynomialRing<BigRational>(fac,nvars,tord,vars);
              parsedPoly = polyType.PolBigRat;
         }
     }
 
 
     /**
      * parsing method for GenPolynomial
      * syntax ? (simple)
      */
     public GenPolynomial nextPolynomial() throws IOException {
         logger.debug("torder = " + tord);
         GenPolynomial a  = pfac.getZERO();
         GenPolynomial a1 = pfac.getONE();
         ExpVector leer = pfac.evzero;
 
         if (debug) logger.debug("a = " + a);
         logger.debug("a1 = " + a1);
         GenPolynomial b = a1;
         GenPolynomial c;
         int tt, oldtt;
         String rat = "";
         char first;
         RingElem r;
         ExpVector e;
         int ix;
         long ie;
         while ( true ) {
             // next input. determine next action
             tt = tok.nextToken();
             logger.debug("while tt = " + tok);
             if ( tt == StreamTokenizer.TT_EOF ) break;
             switch ( tt ) {
             case ')': 
             case ',': 
                  return a;
             case '-': 
                  b = b.negate(); 
             case '+': 
             case '*': 
                  tt = tok.nextToken();
                  break;
             default: // skip
             }
             // read coefficient, monic monomial and polynomial
             if ( tt == StreamTokenizer.TT_EOF ) break;
             switch ( tt ) {
             case StreamTokenizer.TT_WORD: 
                  //System.out.println("TT_WORD: " + tok.sval);
                  if ( tok.sval == null || tok.sval.length() == 0 ) break;
                  // read coefficient
                  first = tok.sval.charAt(0);
                  if ( digit(first) ) {
                      r = fac.parse( tok.sval );
                      // ie = nextExponent();
                      // r = r^ie;
                      // c = new GenPolynomial<BigRational>(r,leer);
                      b = b.multiply(r,leer); 
                      tt = tok.nextToken();
                      if (debug) logger.debug("tt,digit = " + tok);
                  } 
                  if ( tt == StreamTokenizer.TT_EOF ) break;
                  // read polynomial (not jet implemented)
                  if ( tok.sval == null ) break;
                  // read monomial 
                  first = tok.sval.charAt(0);
                  if ( letter(first) ) {
                      ix = indexVar( tok.sval );
                      if ( ix < 0 ) break;
                      //  System.out.println("ix: " + ix);
                      ie = nextExponent();
                      //  System.out.println("ie: " + ie);
                      // r = BigRational.RNONE;
                      e = new ExpVector( vars.length, ix, ie);
                      //c = new GenPolynomial<BigRational>(r,e);
                      b = b.multiply(e); 
                      tt = tok.nextToken();
                      if (debug) logger.debug("tt,letter = " + tok);
                  }
                  break;
             default: //skip 
             }
             if ( tt == StreamTokenizer.TT_EOF ) break;
             // complete polynomial
             tok.pushBack();
             switch ( tt ) {
             case '-': 
             case '+': 
             case '*': 
             case ')': 
             case ',': 
                  logger.debug("b, = " + b);
                  a = a.add(b); 
                  b = a1;
                  break;
             case '\n':
                  tt = tok.nextToken();
                  if (debug) logger.debug("tt,nl = " + tt);
             default: // skip ?
                  if (debug) logger.debug("default: " + tok);
             }
         }
         if (debug) logger.debug("b = " + b);
         a = a.add(b); 
         logger.debug("a = " + a);
         // b = a1;
         return a;
     }
 
 
     /**
      * parsing method for exponent (of variable)
      * syntax: ^long | **long
      */
     public long nextExponent() throws IOException {
         long e = 1;
         char first;
         int tt;
         tt = tok.nextToken();
         if ( tt == '^' ) {
            if (debug) logger.debug("exponent ^");
            tt = tok.nextToken();
            if ( tok.sval != null ) {
               first = tok.sval.charAt(0);
               if ( digit(first) ) {
                   e = Long.parseLong( tok.sval );
                   return e;
               }
            }
         }
         if ( tt == '*' ) {
            tt = tok.nextToken();
            if ( tt == '*' ) {
               if (debug) logger.debug("exponent **");
               tt = tok.nextToken();
               if ( tok.sval != null ) {
                  first = tok.sval.charAt(0);
                  if ( digit(first) ) {
                     e = Long.parseLong( tok.sval );
                     return e;
                  }
               }
            }
            tok.pushBack();
         }
         tok.pushBack();
         return e;
     }
 
 
     /**
      * parsing method for comments 
      * syntax: (* comment *) | /_* comment *_/ without _
      * @false does not work with this pushBack()
      */
     public String nextComment() throws IOException {
         // syntax: (* comment *) | /* comment */ 
         StringBuffer c = new StringBuffer();
         int tt;
         if (debug) logger.debug("comment: " + tok);
         tt = tok.nextToken();
         if (debug) logger.debug("comment: " + tok);
         if ( tt == '(' ) {
            tt = tok.nextToken();
            if (debug) logger.debug("comment: " + tok);
            if ( tt == '*' ) {
               if (debug) logger.debug("comment: ");
               while (true) { 
                  tt = tok.nextToken();
                  if ( tt == '*' ) {
                     tt = tok.nextToken();
                     if ( tt == ')' ) {
                         return c.toString();
                     } 
                     tok.pushBack();
                  }
                  c.append(tok.sval);
               }
            } 
            tok.pushBack();
            if (debug) logger.debug("comment: " + tok);
         }
         tok.pushBack();
         if (debug) logger.debug("comment: " + tok);
         return c.toString();
     }
 
 
 
     /**
      * parsing method for variable list
      * syntax: (a, b c, de) gives [ "a", "b", "c", "de" ]
      */
     public String[] nextVariableList() throws IOException {
         List<String> l = new ArrayList<String>();
         int tt;
         tt = tok.nextToken();
         if ( tt == '(' ) {
            logger.debug("variable list");
            tt = tok.nextToken();
            while ( true ) {
                  if ( tt == StreamTokenizer.TT_EOF ) break;
                  if ( tt == ')' ) break;
                  if ( tt == StreamTokenizer.TT_WORD ) {
                      //System.out.println("TT_WORD: " + tok.sval);
                      l.add( tok.sval );
                  }
                  tt = tok.nextToken();
            }
         }
         Object[] ol = l.toArray();
         String[] v = new String[ol.length];
         for (int i=0; i < v.length; i++ ) {
             v[i] = (String) ol[i];
         }
         return v;
     }
 
     /**
      * parsing method for coefficient ring
      * syntax: Rat | Q | Int | Z | Mod modul | Complex | C | Quat 
      */
     public RingFactory nextCoefficientRing() throws IOException {
         RingFactory coeff = null;
         coeffType ct = null;
         int tt;
         tt = tok.nextToken();
         if ( tok.sval != null ) {
            if ( tok.sval.equalsIgnoreCase("Q") ) {
               coeff = new BigRational(0);
               ct = coeffType.BigRat;
            }
            if ( tok.sval.equalsIgnoreCase("Rat") ) {
               coeff = new BigRational(0);
               ct = coeffType.BigRat;
            }
            if ( tok.sval.equalsIgnoreCase("Z") ) {
               coeff = new BigInteger(0);
               ct = coeffType.BigInt;
            }
            if ( tok.sval.equalsIgnoreCase("Int") ) {
               coeff = new BigInteger(0);
               ct = coeffType.BigInt;
            }
            if ( tok.sval.equalsIgnoreCase("Mod") ) {
                tt = tok.nextToken();
                if ( tok.sval != null && tok.sval.length() > 0 ) {
                    if ( digit( tok.sval.charAt(0) ) ) {
                       coeff = new ModInteger(tok.sval,"0");
                       ct = coeffType.ModInt;
                    } else {
                       tok.pushBack();
                    }
                } else {
                  tok.pushBack();
                }
            }
            if ( tok.sval.equalsIgnoreCase("C") ) {
               coeff = new BigComplex(0);
               ct = coeffType.BigC;
            }
            if ( tok.sval.equalsIgnoreCase("Complex") ) {
               coeff = new BigComplex(0);
               ct = coeffType.BigC;
            }
            if ( tok.sval.equalsIgnoreCase("Quat") ) {
               coeff = new BigQuaternion(0);
               ct = coeffType.BigQ;
            }
            if ( tok.sval.equalsIgnoreCase("AN") ) {
               logger.error("AlgebraicNumber not jet implemented");
            }
            if ( tok.sval.equalsIgnoreCase("GF") ) {
               logger.error("GaloisField not jet implemented");
            }
         } 
         if ( coeff == null ) {
            tok.pushBack();
            coeff = new BigRational();
            ct = coeffType.BigRat;
         }
         parsedCoeff = ct;
         return coeff;
     }
 
 
     /**
      * parsing method for weight list
      * syntax: (w1, w2, w3, ..., wn)
      */
     public long[] nextWeightList() throws IOException {
         List<Long> l = new ArrayList<Long>();
         long[] w = null;
         long e;
         char first;
         int tt;
         tt = tok.nextToken();
         if ( tt == '(' ) {
            logger.debug("weight list");
            tt = tok.nextToken();
            while ( true ) {
                  if ( tt == StreamTokenizer.TT_EOF ) break;
                  if ( tt == ')' ) break;
                  if ( tok.sval != null ) {
                     first = tok.sval.charAt(0);
                     if ( digit(first) ) {
                        e = Long.parseLong( tok.sval );
                        l.add( new Long(e) );
                        //System.out.println("w: " + e);
                     }
                  }
                  tt = tok.nextToken(); // also comma
            }
         }
         Object[] ol = l.toArray();
         w = new long[ ol.length ];
         for ( int i=0; i < w.length; i++ ) {
             w[i] = ((Long)ol[ ol.length-i-1 ]).longValue();
         }
         return w;
     }
 
     /**
      * parsing method for split index
      * syntax: |i|
      */
     public int nextSplitIndex() throws IOException {
         int e = -1; // =unknown
         char first;
         int tt;
         tt = tok.nextToken();
         if ( tt == '|' ) {
            logger.debug("split index");
            tt = tok.nextToken();
            if ( tt == StreamTokenizer.TT_EOF ) {
               return e;
            }
            if ( tok.sval != null ) {
               first = tok.sval.charAt(0);
               if ( digit(first) ) {
                  e = Integer.parseInt( tok.sval );
                  //System.out.println("w: " + i);
               }
               tt = tok.nextToken();
               if ( tt != '|' ) {
                  tok.pushBack();
               }
            }
         } else {
           tok.pushBack();
         }
         return e;
     }
 
 
     /**
      * parsing method for term order name
      * syntax: termOrderName = L, IL, LEX, G, IG, GRLEX
      *         W(weights)
      */
     public TermOrder nextTermOrder() throws IOException {
         int evord = TermOrder.DEFAULT_EVORD;
         int tt;
         tt = tok.nextToken();
         if ( tt == StreamTokenizer.TT_EOF ) { /* nop */
         }
         if ( tt == StreamTokenizer.TT_WORD ) {
            // System.out.println("TT_WORD: " + tok.sval);
            if ( tok.sval != null ) {
               if ( tok.sval.equalsIgnoreCase("L") ) {
                  evord = TermOrder.INVLEX;
               }
               if ( tok.sval.equalsIgnoreCase("IL") ) {
                  evord = TermOrder.INVLEX;
               }
               if ( tok.sval.equalsIgnoreCase("LEX") ) {
                  evord = TermOrder.LEX;
               }
               if ( tok.sval.equalsIgnoreCase("G") ) {
                  evord = TermOrder.IGRLEX;
               }
               if ( tok.sval.equalsIgnoreCase("IG") ) {
                  evord = TermOrder.IGRLEX;
               }
               if ( tok.sval.equalsIgnoreCase("GRLEX") ) {
                  evord = TermOrder.GRLEX;
               }
               if ( tok.sval.equalsIgnoreCase("W") ) {
                  long[] w = nextWeightList();
                  int s = nextSplitIndex();
                  if ( s <= 0 ) {
                     return new TermOrder( w );
                  } else {
                     return new TermOrder( w, s );
                  }
               }
            }
         }
         int s = nextSplitIndex();
         if ( s <= 0 ) {
            return new TermOrder( evord );
         } else {
            return new TermOrder( evord, evord, vars.length, s );
         }
     }
 
     /**
      * parsing method for polynomial list
      * syntax: ( p1, p2, p3, ..., pn )
      */
     public List<GenPolynomial> nextPolynomialList() throws IOException {
         GenPolynomial a;
         List<GenPolynomial> L = new ArrayList<GenPolynomial>();
         int tt;
         tt = tok.nextToken();
         if ( tt == StreamTokenizer.TT_EOF ) return L;
         if ( tt != '(' ) return L;
         logger.debug("polynomial list");
         while ( true ) {
                tt = tok.nextToken();
                if ( tok.ttype == ',' ) continue;
                if ( tt == '(' ) {
                   a = nextPolynomial();
                   tt = tok.nextToken();
                   if ( tok.ttype != ')' ) tok.pushBack();
                } else { tok.pushBack();
                   a = nextPolynomial();
                }
                logger.info("next pol = " + a); 
                L.add( a );
                if ( tok.ttype == StreamTokenizer.TT_EOF ) break;
                if ( tok.ttype == ')' ) break;
          }
          return L;
     }
 
 
     /**
      * parsing method for submodule list
      * syntax: ( ( p11, p12, p13, ..., p1n ), 
                  ..., 
                  ( pm1, pm2, pm3, ..., pmn ) )
      */
     public List<List<GenPolynomial>> nextSubModuleList() throws IOException {
         List<List<GenPolynomial>> L = new ArrayList<List<GenPolynomial>>();
         int tt;
         tt = tok.nextToken();
         if ( tt == StreamTokenizer.TT_EOF ) return L;
         if ( tt != '(' ) return L;
         logger.debug("module list");
         List<GenPolynomial> v = null;
         while ( true ) {
                tt = tok.nextToken();
                if ( tok.ttype == ',' ) continue;
                if ( tok.ttype == ')' ) break;
                if ( tok.ttype == StreamTokenizer.TT_EOF ) break;
                if ( tt == '(' ) {
                   tok.pushBack();
                   v = nextPolynomialList();
                   logger.info("next vect = " + v); 
                   L.add( v );
                }
          }
          return L;
     }
 
 
     /**
      * parsing method for solvable polynomial relation table
      * syntax: ( p_1, p_2, p_3, ..., p_n+3 )
      * semantics: p_n+1 * p_n+2 = p_n+3 
      */
     public void nextRelationTable() throws IOException {
         if ( spfac == null ) {
             return;
         }
         RelationTable table = spfac.table;
         List<GenPolynomial> rels = null;
         GenPolynomial p;
         GenSolvablePolynomial sp;
         int tt;
         tt = tok.nextToken();
         if ( tok.sval != null ) {
            if ( tok.sval.equalsIgnoreCase("RelationTable") ) {
               rels = nextPolynomialList();
            }
         } 
         if ( rels == null ) {
            tok.pushBack();
            return;
         } 
         for ( Iterator<GenPolynomial> it = rels.iterator(); it.hasNext(); ) {
             p = it.next();
             ExpVector e = p.leadingExpVector();
             if ( it.hasNext() ) {
                p = it.next();
                ExpVector f = p.leadingExpVector();
                if ( it.hasNext() ) {
                   p = it.next();
                   sp = new GenSolvablePolynomial(spfac,p.val);
                   table.update( e, f, sp );
                }
             }
         }
         if ( debug ) {
            logger.info("table = " + table);
         }
         return;
     }
 
 
     /**
      * parsing method for polynomial set
      * syntax: coeffRing varList termOrderName polyList
      */
     public PolynomialList nextPolynomialSet() throws IOException {
         //String comments = "";
         //comments += nextComment();
         //if (debug) logger.debug("comment = " + comments);
 
         RingFactory coeff = nextCoefficientRing();
         logger.info("coeff = " + coeff); 
 
         vars = nextVariableList();
         nvars = vars.length;
              String dd = "vars ="; 
              for (int i = 0; i < vars.length ;i++) {
                  dd+= " "+vars[i]; 
              }
              logger.info(dd); 
         if ( vars != null ) {
            nvars = vars.length;
         }
 
         tord = nextTermOrder();
         logger.info("tord = " + tord); 
 
         initFactory(coeff,parsedCoeff); // global: nvars, tord, vars
         List< GenPolynomial > s = null;
         s = nextPolynomialList();
         logger.info("s = " + s); 
         // comments += nextComment();
         return new PolynomialList(pfac,s);
     }
 
 
     /**
      * parsing method for module set
      * syntax: coeffRing varList termOrderName moduleList
      */
     public ModuleList nextSubModuleSet() throws IOException {
         //String comments = "";
         //comments += nextComment();
         //if (debug) logger.debug("comment = " + comments);
 
         RingFactory coeff = nextCoefficientRing();
         logger.info("coeff = " + coeff); 
 
         vars = nextVariableList();
              String dd = "vars ="; 
              for (int i = 0; i < vars.length ;i++) {
                  dd+= " "+vars[i]; 
              }
              logger.info(dd); 
         if ( vars != null ) {
            nvars = vars.length;
         }
 
         tord = nextTermOrder();
              logger.info("tord = " + tord); 
 
         initFactory(coeff,parsedCoeff); // global: nvars, tord, vars
         List< List< GenPolynomial > > m = null;
         m = nextSubModuleList();
              logger.info("m = " + m); 
         // comments += nextComment();
 
         return new ModuleList(pfac,m);
     }
 
 
     /**
      * parsing method for solvable polynomial list
      * syntax: ( p1, p2, p3, ..., pn )
      */
     public List<GenSolvablePolynomial> nextSolvablePolynomialList() 
            throws IOException {
         List<GenPolynomial> s = nextPolynomialList();
              logger.info("s = " + s); 
         // comments += nextComment();
 
         GenPolynomial p;
         GenSolvablePolynomial ps;
         List<GenSolvablePolynomial> sp 
             = new ArrayList<GenSolvablePolynomial>( s.size() );
         for ( Iterator<GenPolynomial> it = s.iterator(); it.hasNext(); ) {
             p = it.next();
             ps = new GenSolvablePolynomial(spfac,p.val);
             //System.out.println("ps = " + ps);
             sp.add( ps );
         }
         return sp;
     }
 
 
     /**
      * parsing method for solvable polynomial
      * syntax: p
      */
     public GenSolvablePolynomial nextSolvablePolynomial() 
            throws IOException {
         GenPolynomial p = nextPolynomial();
              logger.info("p = " + p); 
         // comments += nextComment();
 
         GenSolvablePolynomial ps
            = new GenSolvablePolynomial(spfac,p.val);
              //System.out.println("ps = " + ps);
         return ps;
     }
 
 
     /**
      * parsing method for solvable polynomial set
      * syntax: varList termOrderName relationTable polyList
      */
 
     public PolynomialList nextSolvablePolynomialSet() throws IOException {
         //String comments = "";
         //comments += nextComment();
         //if (debug) logger.debug("comment = " + comments);
 
         RingFactory coeff = nextCoefficientRing();
         logger.info("coeff = " + coeff); 
 
         vars = nextVariableList();
              String dd = "vars ="; 
              for (int i = 0; i < vars.length ;i++) {
                  dd+= " "+vars[i]; 
              }
              logger.info(dd); 
         if ( vars != null ) {
            nvars = vars.length;
         }
 
         tord = nextTermOrder();
              logger.info("tord = " + tord); 
 
         initFactory(coeff,parsedCoeff);  // must be because of symmetric read
         initSolvableFactory(coeff,parsedCoeff); // global: nvars, tord, vars
 
         //System.out.println("pfac = " + pfac);
         //System.out.println("spfac = " + spfac);
 
         nextRelationTable();
         if ( logger.isInfoEnabled() ) {
            logger.info("table = " + table); 
         }
 
         List< GenSolvablePolynomial > s = null;
         s = nextSolvablePolynomialList();
              logger.info("s = " + s); 
         // comments += nextComment();
         return new OrderedPolynomialList(spfac,s); // Ordered
     }
 
 
     /**
      * parsing method for solvable submodule list
      * syntax: ( ( p11, p12, p13, ..., p1n ), 
                  ..., 
                  ( pm1, pm2, pm3, ..., pmn ) )
      */
     public List<List<GenSolvablePolynomial>> nextSolvableSubModuleList() 
            throws IOException {
         List<List<GenSolvablePolynomial>> L 
              = new ArrayList<List<GenSolvablePolynomial>>();
         int tt;
         tt = tok.nextToken();
         if ( tt == StreamTokenizer.TT_EOF ) return L;
         if ( tt != '(' ) return L;
         logger.debug("module list");
         List<GenSolvablePolynomial> v = null;
         while ( true ) {
                tt = tok.nextToken();
                if ( tok.ttype == ',' ) continue;
                if ( tok.ttype == ')' ) break;
                if ( tok.ttype == StreamTokenizer.TT_EOF ) break;
                if ( tt == '(' ) {
                   tok.pushBack();
                   v = nextSolvablePolynomialList();
                   logger.info("next vect = " + v); 
                   L.add( v );
                }
          }
          return L;
     }
 
     /**
      * parsing method for solvable module set
      * syntax: varList termOrderName relationTable moduleList
      */
     public ModuleList nextSolvableSubModuleSet() throws IOException {
         //String comments = "";
         //comments += nextComment();
         //if (debug) logger.debug("comment = " + comments);
 
         RingFactory coeff = nextCoefficientRing();
         logger.info("coeff = " + coeff); 
 
         vars = nextVariableList();
              String dd = "vars ="; 
              for (int i = 0; i < vars.length ;i++) {
                  dd+= " "+vars[i]; 
              }
              logger.info(dd); 
         if ( vars != null ) {
            nvars = vars.length;
         }
 
         tord = nextTermOrder();
              logger.info("tord = " + tord); 
 
         initFactory(coeff,parsedCoeff);  // must be because of symmetric read
         initSolvableFactory(coeff,parsedCoeff); // global: nvars, tord, vars
 
         //System.out.println("spfac = " + spfac);
 
         nextRelationTable();
         if ( logger.isInfoEnabled() ) {
            logger.info("table = " + table); 
         }
 
         List<List<GenSolvablePolynomial>> s = null;
         s = nextSolvableSubModuleList();
              logger.info("s = " + s); 
         // comments += nextComment();
 
         return new OrderedModuleList(spfac,s); // Ordered
     }
 
     private boolean digit(char x) {
         return '0' <= x && x <= '9';
     }
 
     private boolean letter(char x) {
         return ( 'a' <= x && x <= 'z' ) || ( 'A' <= x && x <= 'Z' );
     }
 
     private int indexVar(String x) {
         for ( int i = 0; i < vars.length; i++ ) { 
             if ( x.equals( vars[i] ) ) { 
                return vars.length-i-1;
             }
         }
         return -1; // not found
     }
 
     // unused
     public void nextComma() throws IOException {
         int tt;
         if ( tok.ttype == ',' ) {
            if (debug) logger.debug("comma: ");
            tt = tok.nextToken();
         }
     }
 
 }
