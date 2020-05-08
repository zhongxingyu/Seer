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
 
 import java.util.*;
 
 import ca.uqac.info.ltl.GenericVisitor;
 import ca.uqac.info.ltl.Operator;
 import ca.uqac.info.ltl.OperatorEquals;
 import ca.uqac.info.trace.*;
 import ca.uqac.info.util.Relation;
 
 /**
  * Converts an arbitrary trace and formula to an atomic (i.e. propositional)
  * problem.
  * <p>
  * The AtomicTranslator takes as input an arbitrary event trace &sigma; and
  * and converts it to an atomic trace &sigma', where every event of
  * the trace becomes a single ("atomic") symbol.
  * <p>
  * The translator does the same thing with the formula &phi;, yielding a
  * formula &phi;' where every ground
  * term is replaced by an atom (or a disjunction of atoms).
  * The translator does so in such a way
  * that &sigma;&nbsp;&#8871;&nbsp;&phi; if and only if
  * &sigma;'&nbsp;&#8871;&nbsp;&phi;'. 
  * <p>
  * Note that {@link translateFormula} must be called <strong>after</strong>
  * {@link translateTrace}, as it reuses information from reading the
  * trace to generate the formula.
  * <p>
  * <strong>Example usage</strong>
  * <p>
  * Consider the following trace of four events:
  * <pre>
  * &lt;Event&gt;
  *   &lt;a&gt;1&lt;/a&gt;
  *   &lt;b&gt;2&lt;/b&gt;
  *   &lt;c&gt;3&lt;/c&gt;
  * &lt;/Event&gt;
  * &lt;Event&gt;
  *   &lt;a&gt;2&lt;/a&gt;
  *   &lt;b&gt;6&lt;/b&gt;
  *   &lt;c&gt;1&lt;/c&gt;
  * &lt;/Event&gt;
  *  &lt;Event&gt;
  *   &lt;a&gt;1&lt;/a&gt;
  *   &lt;b&gt;3&lt;/b&gt;
  *   &lt;c&gt;1&lt;/c&gt;
  * &lt;/Event&gt;
  *  &lt;Event&gt;
  *   &lt;a&gt;2&lt;/a&gt;
  *   &lt;b&gt;6&lt;/b&gt;
  *   &lt;c&gt;4&lt;/c&gt;
  * &lt;/Event&gt;
  * </pre>
  * Suppose we want to create an atomic trace that distinguishes the values
  * of parameters a and b.
  * <ol>
  * <li>One first calls <code>translator.setParameters(s)</code>, where <code>s</code>
  * is a set containing a single String "a".</li>
  * <li>One then calls <code>translator.translateTrace(t)</code>, where <code>t</code>
  * is the trace shown above. The resulting trace will be
  *   <blockquote>
  *   e0 e1 e2 e1
  *   </blockquote>
  * that is, the 2nd and 4th events have the same token, since they have the same
  * combination of values for parameters a and b (and we discard the other parameters).
  * <li>Suppose we want to convert the LTL formula <b>F</b>&nbsp;a=1. One calls
  * <code>translator.translateFormula(f)</code>, where <code>f</code> is an
  * {@link Operator} containing the formula. The method returns the string
  *   <blockquote>
  *   F (e0 | e2)
  *   </blockquote>
  * that is, the condition a=1 is fulfilled by an event if its token is either
  * e0 or e2.</li>
  * </ol>
  * One can see how a problem on a trace/formula with parameters has been
  * converted into a situation where both the trace and the formula contain
  * only atomic events.
  * @author sylvain
  *
  */
 public class AtomicTranslator implements Translator
 {
 	
 	protected EventTrace m_trace;
 	protected Operator m_formula;
 	protected Vector<String> m_parameters;
 	protected Map<Event,String> m_tokens;
 	
 	/**
 	 * The symbol used in the token name to separate parameter names
 	 * from their values
 	 */
 	protected static final String m_separator = "_";
 	
 	/**
 	 * Empty constructor.
 	 */
 	public AtomicTranslator()
 	{
 		super();
 		m_parameters = null;
 	}
 
 	@Override
   public String translateFormula(Operator o)
   {
 		assert m_tokens != null; // If null, means that translateTrace() hasn't been called first
 		AtomicFormulaTranslator aft = new AtomicFormulaTranslator();
 		o.accept(aft);
 		return aft.getFormula();
   }
 	
 	/**
 	 * Translates an arbitrary LTL formula into a propositional LTL
 	 * formula. The visitor replaces every ground term of the form
 	 * p<sub><i>n</i></sub>&nbsp;=&nbsp;c into a term of the form
 	 * <i>a</i><sub>1</sub> &or; <i>a</i><sub>2</sub> &or; ...
 	 * <i>a</i><sub><i>n</i></sub>, where the <i>a</i><sub><i>i</i></sub>
 	 * are the tokens for all the events that satisfy the equality.
 	 * <p>
 	 * The method supposes that:
 	 * <ul>
 	 * <li>{@link translateFormula} has already
 	 * been called on the translator</li>
 	 * <li>The left-hand side of an equality is
 	 * a parameter name, and the right-hand side is the value</li>
 	 * <li>A multi-valued message satisfies the equality as soon
 	 * as one occurrence of the parameter contains the value
 	 * (hence the equality is implicitly existentially quantified)</li>
 	 * </ul>
 	 * @author sylvain
 	 *
 	 */
 	protected class AtomicFormulaTranslator extends GenericVisitor
 	{
     public AtomicFormulaTranslator()
     {
       super();
     }
 
     public String getFormula()
     {
       StringBuffer out = m_pieces.peek();
       return out.toString();
     }
     
 		@Override
     public void visit(OperatorEquals o)
     {
 			// Check all events that satisfy the said equality
 			final Map<Event,String> tokens = AtomicTranslator.this.m_tokens;
 			Set<Event> events = tokens.keySet();
 			String left = o.getLeft().toString();
 			String right = o.getRight().toString();
 			Set<String> disjunct_tokens = new HashSet<String>();
 			for (Event e : events)
 			{
 				// Check if equality applies to this event
 				Relation<String,String> p_dom = e.getParameterDomain();
 				Set<String> vals = p_dom.get(left);
 				if (vals != null && vals.contains(right))
 				{
 					// Yes: then the associated token is a possible one
 					String token = tokens.get(e);
 					disjunct_tokens.add(token);
 				}
 			}
 			// Creates the disjunct associated with that set of tokens
 			StringBuffer out = new StringBuffer();
 			boolean first = true;
 			for (String token : disjunct_tokens)
 			{
 				if (!first)
 				{
 					out.append(" | ");
 				}
 				first = false;
 				out.append(token);	
 			}
 	    m_pieces.push(out);
     }
 	}
 	
 
 	@Override
   public String translateTrace(EventTrace t)
   {
 		// TODO: this whole process could be done in one pass
 		m_tokens = new HashMap<Event,String>();
 	  StringBuilder out = new StringBuilder();
 	  // First pass on the trace: compute projection over
 	  // parameters to retain, and give each event
 	  // an atomic token
 	  for (Event e : t)
 	  {
 	  	String token = createTokenFromEvent(e);
 	  	m_tokens.put(e, token);
 	  }
 	  // Replace all tokens with atomic letters
 	  m_tokens = sanitizeMap(m_tokens);
 	  // Return trace with atomic events
 	  for (Event e : t)
 	  {
 	  	String simple_token = m_tokens.get(e);
 	  	out.append(simple_token).append(" ");
 	  }
 	  return out.toString();
   }
 	
 	protected Map<Event,String> sanitizeMap(Map<Event,String> tokens)
 	{
 		Map<Event,String> simple_tokens = new HashMap<Event,String>();
 		Map<String,String> associations = new HashMap<String,String>();
 		TokenGenerator t_gen = new TokenGenerator();
 		Set<Event> events = tokens.keySet();
 		for (Event e : events)
 		{
 			String complex_token = tokens.get(e);
 			String simple_token = associations.get(complex_token);
 			if (simple_token == null)
 			{
 				simple_token = t_gen.next();
 				associations.put(complex_token, simple_token);
 			}
 			simple_tokens.put(e, simple_token);
 		}
 		return simple_tokens;
 	}
 	
 	/**
 	 * Creates an "atomic" token from an arbitrary event.
 	 * The process goes as follows: given an event of the following
 	 * form:
 	 * <pre>
 	 * &lt;Event&gt;
 	 *   &lt;p<sub>0</sub>&gt;123&lt;/p<sub>0</sub>&gt;
 	 *   &lt;p<sub>1</sub>&gt;456&lt;/p<sub>1</sub>&gt;
 	 *   &lt;p<sub>2</sub>&gt;789&lt;/p<sub>2</sub>&gt;
 	 * &lt;/Event&gt;
 	 * </pre>
 	 * Suppose the user has provided p<sub>0</sub> and
 	 * p<sub>1</sub> as the parameters to take into account when
 	 * forming the atomic symbol. The tokenizer will
 	 * create an event token p<sub>0</sub>_123_p<sub>1</sub>_456.
 	 * @param e The event to process
 	 * @return A string rendition of the atomic token
 	 */
 	protected String createTokenFromEvent(Event e)
 	{
   	Relation<String,String> dom = e.getParameterDomain();
   	Vector<String> pars = m_parameters;
   	if (m_parameters == null)
   	{
   		// If the user didn't specify any interesting parameters,
   		// we take them all
   		pars = new Vector<String>();
   		pars.addAll(dom.keySet());
   	}
   	StringBuilder token = new StringBuilder();
   	for (String p_name : pars)
   	{
   		token.append(p_name).append(m_separator);
   		Set<String> p_values = dom.get(p_name);
   		if (p_values != null)
   		{
 	  		for (String v: p_values)
 	  		{
 	  			token.append(v).append(m_separator);
 	  		}
   		}
   	}
   	return token.toString();
 	}
 	
 	public void setTrace(EventTrace t)
 	{
 		m_trace = t;
 	}
 	
 	public void setFormula(Operator o)
 	{
 		m_formula = o;
 	}
 	
 	public void setParameters(Vector<String> parameters)
 	{
 		m_parameters = parameters;
 	}
 	
 	/**
 	 * The TokenGenerator simply produces a list of event tokens,
 	 * each of the form e0, e1, e2, ...
 	 * @author sylvain
 	 */
 	protected class TokenGenerator implements Iterator<String>
 	{
 		int m_counter = 0;
 		protected static final String m_prefix = "e";
 
 		@Override
     public boolean hasNext()
     {
 	    return true;
     }
 
 		@Override
     public String next()
     {
 			String out = m_prefix + m_counter;
 			m_counter++;
 	    return out;
     }
 
 		@Override
     public void remove()
     {
 	    // We do nothing
     }
 		
 	}
 
 
 	@Override
 	public String getSignature(EventTrace t) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
