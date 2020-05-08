 /******************************************************************************
   Event trace translator
   Copyright (C) 2012 Sylvain Halle
 
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 package ca.uqac.info.trace.conversion;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 
 import ca.uqac.info.ltl.*;
 import ca.uqac.info.ltl.Operator.ParseException;
 import ca.uqac.info.trace.EventTrace;
 import ca.uqac.info.trace.XmlTraceReader;
 
 /**
  * Translates an input trace and a formula for the Maude rewriting engine. The
  * translation follows the syntax described in the following paper:
  * <ul>
  * <li>Klaus Havelund, Grigore Ro&scedil;u. (2001). <cite>Monitoring Programs
  * using Rewriting.</cite> Automated Software Engineering 2001 (ASE'01).</li>
  * </ul>
  * 
  * @author Aouatef Mrad
  * 
  */
 public class MaudeTranslator extends Translator
 {
 
   // TODO: probably no longer necessary as the class receives
   // a trace already translated into atomic symbols
   protected AtomicTranslator m_atomicTranslator;
 
   /**
    * Whether to use the "alternate" method described in the paper
    */
   protected boolean m_useAlternateMethod = true;
 
   /**
    * Constructor
    */
   public MaudeTranslator()
   {
     super();
     m_atomicTranslator = new AtomicTranslator();
   }
 
   public MaudeTranslator(boolean alternate)
   {
     this();
     m_useAlternateMethod = alternate;
   }
 
   @Override
   public String translateTrace(EventTrace t)
   {
     setTrace(t);
     return translateTrace();
   }
 
   @Override
   public String translateFormula(Operator o)
   {
     StringBuilder out = new StringBuilder();
     MaudeFormulaTranslator mft = new MaudeFormulaTranslator();
     o.accept(mft);
     out.append(mft.getFormula());
     return out.toString();
   }
 
   protected class MaudeFormulaTranslator implements OperatorVisitor
   {
     Stack<StringBuilder> m_pieces;
 
     public MaudeFormulaTranslator()
     {
       super();
       m_pieces = new Stack<StringBuilder>();
     }
 
     public String getFormula()
     {
       StringBuilder out = m_pieces.peek();
       return out.toString();
     }
 
     @Override
     public void visit(OperatorAnd o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
       StringBuilder out = new StringBuilder("(").append(left).append(") ")
           .append("/\\").append(" (").append(right).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorOr o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
       StringBuilder out = new StringBuilder("(").append(left).append(") ")
           .append("\\/").append(" (").append(right).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorImplies o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
       StringBuilder out = new StringBuilder("(").append(left).append(") -> (")
           .append(right).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorNot o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("~(").append(op).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorF o)
     {
       StringBuilder op = m_pieces.pop();
 
       StringBuilder out = new StringBuilder("<> (").append(op).append(")");
       m_pieces.push(out);
 
     }
 
     @Override
     public void visit(OperatorX o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("o (").append(op).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorG o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("[] (").append(op).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorEquals o)
     {
       StringBuilder right = m_pieces.pop(); // Pop right-hand side
       StringBuilder left = m_pieces.pop(); // Pop left-hand side
       StringBuilder out = new StringBuilder();
       out.append(left).append(" = ").append(right);
       m_pieces.push(out);
     }
 
     @Override
     public void visit(Atom o)
     {
       if (o instanceof OperatorFalse)
         m_pieces.push(new StringBuilder("false"));
       else if (o instanceof OperatorTrue)
         m_pieces.push(new StringBuilder("true"));
       else
         m_pieces.push(new StringBuilder(o.getSymbol()));
     }
 
     @Override
     public void visit(OperatorEquiv o)
     {
     }
 
     @Override
     public void visit(OperatorU o)
     {
     }
 
     @Override
     public void visit(Exists o)
     {
       // Not supposed to happen!
       System.err.println("Error: quantifier found in Maude translator");
       assert false;
     }
 
     @Override
     public void visit(ForAll o)
     {
       // Not supposed to happen!
       System.err.println("Error: quantifier found in Maude translator");
       assert false;
     }
 
     @Override
     public void visit(XPathAtom p)
     {
       // false because no leading slash
       m_pieces.push(new StringBuilder(p.toString(false)));
     }
 
     @Override
     public void visit(OperatorTrue o)
     {
       m_pieces.push(new StringBuilder("true"));
     }
 
     @Override
     public void visit(OperatorFalse o)
     {
       m_pieces.push(new StringBuilder("false"));
 
     }
 
   }
 
   protected class MaudeEqualityGetter extends EmptyVisitor
   {
 
     Set<OperatorEquals> m_equalities;
 
     public MaudeEqualityGetter()
     {
       super();
       m_equalities = new HashSet<OperatorEquals>();
     }
 
     public Set<OperatorEquals> getEqualities()
     {
       return m_equalities;
     }
 
     @Override
     public void visit(OperatorEquals o)
     {
       // We only add equalities between an XPathAtom and something
       // Other equalities are between constants
       if (o.getLeft() instanceof XPathAtom || o.getRight() instanceof XPathAtom)
         m_equalities.add(o);
     }
 
   }
 
   @Override
   public String translateFormula()
   {
     // We assume translateTrace has been called first!
     String prop = m_atomicTranslator.translateFormula(m_formula);
     Operator op = null;
     try
     {
       op = Operator.parseFromString(prop);
     } catch (ParseException e)
     {
       System.err.println("Parse error in MaudeTranslator");
       return "";
     }
     MaudeFormulaTranslator mft = new MaudeFormulaTranslator();
     op.accept(mft);
     return mft.getFormula();
   }
 
   @Override
   public String translateTrace()
   {
     String sat_operator = " |= ";
     if (m_useAlternateMethod)
       sat_operator = " |- ";
     StringBuilder out_Trace = new StringBuilder();
     Set<String> listParm = new HashSet<String>();
     StringBuilder maude_trace = new StringBuilder();
     String atomic_trace = m_atomicTranslator.translateTrace(m_trace);
     boolean first = true;
     for (String c : atomic_trace.split(" "))
     {
       if (!first)
         maude_trace.append(",");
       maude_trace.append(c);
       listParm.add(c);
       first = false;
     }
     StringBuilder operandes = new StringBuilder();
     for (String c : listParm)
       operandes.append(c).append(" ");
     String prop = translateFormula();
     maude_trace = maude_trace.append(sat_operator).append(prop);
     out_Trace.append("in ltl.maude\n");
     out_Trace.append("fmod MY-TRACE is").append("\n");
    if (m_useAlternateMethod)
    	out_Trace.append("  extending LTL-REVISED .").append("\n");
    else
    	out_Trace.append("  extending LTL .").append("\n");
     out_Trace.append("  ops  ").append(operandes).append(" : -> Atom .")
         .append("\n");
     out_Trace.append("endfm\n");
     out_Trace.append("reduce").append(" ");
     out_Trace.append(maude_trace).append(".");
     out_Trace.append("\n quit");
     return out_Trace.toString();
   }
 
   public static void main(String[] args)
   {
     // File to read and property to verify
     String filename = "traces/traces_0.txt";
     String formula = "F (∃i ∈ /p0 : (i=0))";
 
     // Read trace
     java.io.File f = new java.io.File(filename);
     XmlTraceReader reader = new XmlTraceReader();
     EventTrace trace = null;
     try
     {
       trace = reader.parseEventTrace(new java.io.FileInputStream(f));
     } catch (java.io.FileNotFoundException ex)
     {
       ex.printStackTrace();
       System.exit(1);
     }
     assert trace != null;
 
     // Parse property
     Operator o = null;
     try
     {
       o = Operator.parseFromString(formula);
     } catch (ParseException e)
     {
       e.printStackTrace();
     }
 
     // Convert to propositional LTL
     PropositionalTranslator pt = new PropositionalTranslator();
     pt.setFormula(o);
     pt.setTrace(trace);
     String s = pt.translateFormula();
 
     // Reparse and convert to atomic LTL
     try
     {
       o = Operator.parseFromString(s);
     } catch (Operator.ParseException e)
     {
       System.out.println("Parse exception");
     }
     ConstantConverter cc = new ConstantConverter();
     o.accept(cc);
     o = cc.getFormula();
 
     // Pass trace and property to Maude translator
     MaudeTranslator md = new MaudeTranslator();
     md.setTrace(trace);
     md.setFormula(o);
 
     // Output Maude file
     System.out.println(md.translateTrace(trace));
 
   }
 
   @Override
   public boolean requiresFlat()
   {
     return true;
   }
 
   @Override
   public boolean requiresPropositional()
   {
     return true;
   }
 
   @Override
   public boolean requiresAtomic()
   {
     return true;
   }
 
   @Override
   public String getSignature()
   {
     /*
      * NOTE: what is this code doing here? Relation<String, String>
      * param_domains = m_trace.getParameterDomain(); Set<String> params =
      * param_domains.keySet(); Vector<String> vectParams = new Vector<String>();
      * vectParams.addAll(params);
      * 
      * String strTemp = vectParams.toString().replaceAll(",", " "); strTemp =
      * strTemp.replace("[", " "); strTemp = strTemp.replace("]", " "); return
      * strTemp;
      */
     return "";
   }
 
   @Override
   public String getTraceFile()
   {
     return m_outTrace;
   }
 
   @Override
   public String getFormulaFile()
   {
     return "";
   }
 
   @Override
   public String getSignatureFile()
   {
     return "";
   }
 
 }
