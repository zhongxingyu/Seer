 /*
     LTL trace validation using MapReduce
     Copyright (C) 2012 Sylvain Hallé
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package ca.uqac.dim.mapreduce.ltl;
 
 import java.util.Set;
 import java.util.HashSet;
 import ca.uqac.dim.mapreduce.*;
 
 public class LTLReducer implements Reducer<Operator,LTLTupleValue>
 {
 	protected Set<Operator> m_subformulas;
 	protected int m_traceLength;
 	
 	public LTLReducer(Set<Operator> subformulas, int trace_len)
 	{
 		super();
 		m_subformulas = subformulas;
 		m_traceLength = trace_len;
 	}
 	
 	public LTLReducer()
 	{
 		this(new HashSet<Operator>(), 0);
 	}
 
 
 	@Override
 	public void reduce(OutCollector<Operator, LTLTupleValue> out, Operator key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		// Make sure someone has not started enumerating the input contents
 		in.rewind();
 		// Evaluate according to the operator standing for the key
 		if (key.getClass() == OperatorAnd.class)
 			reduce(out, (OperatorAnd) key, in);
 		else if (key.getClass() == OperatorOr.class)
 			reduce(out, (OperatorOr) key, in);
 		else if (key.getClass() == OperatorImplies.class)
 			reduce(out, (OperatorImplies) key, in);
 		else if (key.getClass() == OperatorNot.class)
 			reduce(out, (OperatorNot) key, in);
		else if (key.getClass() == OperatorNot.class)
			reduce(out, (OperatorNot) key, in);
 		else if (key.getClass() == OperatorF.class)
 			reduce(out, (OperatorF) key, in);
 		else if (key.getClass() == OperatorG.class)
 			reduce(out, (OperatorG) key, in);
 		else if (key.getClass() == OperatorX.class)
 			reduce(out, (OperatorX) key, in);
 		else if (key instanceof Atom)
 			reduce(out, (Atom) key, in);
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * &phi;&nbsp;&and;&nbsp;&psi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorAnd key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		Set<Integer> contain_left = new HashSet<Integer>();
 		Set<Integer> contain_right = new HashSet<Integer>();
 		Operator left_op = key.getLeft();
 		Operator right_op = key.getRight(); 
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			// We must survive tuples whose subformula will be used at a later iteration
 			if (key.getDepth() > v.getIteration())
 			{
 				out.collect(t);
 				while (in.hasNext())
 					out.collect(in.next());
 				return;
 			}
 			Operator op = v.getOperator();
 			assert op != null;
 			int n = v.getStateNumber();
 			LTLTuple out_t = null;
 			if (op.equals(left_op))
 			{
 				if (contain_right.contains(new Integer(n)))
 				{
 					out_t = new LTLTuple(key, new LTLTupleValue(null, n, v.getIteration()));
 				}
 				else
 				{
 					contain_left.add(new Integer(n));
 				}
 			}
 			else if (op.equals(right_op))
 			{
 				if (contain_left.contains(new Integer(n)))
 				{
 					out_t = new LTLTuple(key, new LTLTupleValue(null, n, v.getIteration()));
 				}
 				else
 				{
 					contain_right.add(new Integer(n));
 				}
 			}
 			if (out_t != null)
 				out.collect(out_t);
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * &phi;&nbsp;&or;&nbsp;&psi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorOr key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, v.getStateNumber(), v.getIteration()));
 			out.collect(out_t);
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * &phi;&nbsp;&rarr;&nbsp;&psi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorImplies key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		boolean[] sats = new boolean[m_traceLength];
 		for (int i = 0; i < m_traceLength; i++)
 			sats[i] = false;
 		Operator right_op = key.getRight();
 		int iteration = 0;
 		// We emit one output tuple for each state where &psi; is true 
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			// We must survive tuples whose subformula will be used at a later iteration
 			LTLTupleValue v = t.getValue();
 			if (key.getDepth() > v.getIteration())
 			{
 				out.collect(t);
 				while (in.hasNext())
 					out.collect(in.next());
 				return;
 			}
 			int state_no =  v.getStateNumber();
 			sats[state_no] = true;
 			iteration = v.getIteration();
 			Operator op = v.getOperator();
 			if (right_op.equals(op))
 			{
 				Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, state_no, v.getIteration()));
 				out.collect(out_t);
 			}
 		}
 		// We emit one output tuple for each state where we haven't seen neither
 		// &psi; nor &phi;: this leaves all states for which &phi; is false
 		for (int i = 0; i < m_traceLength; i++)
 		{
 			if (!sats[i])
 			{
 				Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, i, iteration));
 				out.collect(out_t);
 			}
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is an Atom.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, Atom key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, v.getStateNumber(), v.getIteration()));
 			out.collect(out_t);
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * <b>F</b>&nbsp;&phi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorF key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		int max_seen = -1;
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			int state_num = v.getStateNumber();
 			if (state_num <= max_seen)
 				continue;
 			for (int i = max_seen + 1; i <= state_num; i++)
 			{
 				Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, i, v.getIteration()));
 				out.collect(out_t);				
 			}
 			max_seen = state_num;
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * <b>X</b>&nbsp;&phi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorX key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			int state_num = v.getStateNumber();
 			int new_state_num = state_num - 1;
 			if (new_state_num >= 0)
 			{
 				Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, new_state_num, v.getIteration()));
 				out.collect(out_t);				
 			}
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * <b>G</b>&nbsp;&phi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorG key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		boolean[] sats = new boolean[m_traceLength];
 		for (int i = 0; i < m_traceLength; i++)
 			sats[i] = false;
 		int iteration = 0;
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			iteration = v.getIteration();
 			int state_num = v.getStateNumber();
 			sats[state_num] = true;
 		}
 		for (int i = m_traceLength - 1; i >= 0; i--)
 		{
 			if (!sats[i])
 				break;
 			Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, i, iteration));
 			out.collect(out_t);				
 		}
 	}
 	
 	/**
 	 * Implementation of Reduce, when the tuple's key is of the form
 	 * <b>G</b>&nbsp;&phi;.
 	 * @param out The output collector
 	 * @param key The top-level formula used as the key for the Reduce job
 	 * @param in The input collector
 	 */
 	private void reduce(OutCollector<Operator, LTLTupleValue> out, OperatorNot key,
 			InCollector<Operator, LTLTupleValue> in)
 	{
 		boolean[] sats = new boolean[m_traceLength];
 		for (int i = 0; i < m_traceLength; i++)
 			sats[i] = false;
 		int iteration = 0;
 		while (in.hasNext())
 		{
 			Tuple<Operator,LTLTupleValue> t = in.next();
 			LTLTupleValue v = t.getValue();
 			iteration = v.getIteration();
 			int state_num = v.getStateNumber();
 			sats[state_num] = true;
 		}
 		for (int i = 0; i < m_traceLength; i++)
 		{
 			if (!sats[i])
 			{
 				Tuple<Operator,LTLTupleValue> out_t = new Tuple<Operator,LTLTupleValue>(key, new LTLTupleValue(null, i, iteration));
 				out.collect(out_t);
 			}
 		}
 	}
 }
