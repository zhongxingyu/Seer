 package nl.helixsoft.recordstream;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 
 /**
  * I implemented this utility method because the SPARQL GROUP BY implementation of virtuoso sucks.
  * (AVG and SUM cast double to int, CONCAT just takes the first value, etc...)
  * Instead, just do the query without a group clause, but with output sorted on the grouping variable.
  * Then pass the result to this function to aggregate the results by group.
  * @throws SQLException 
  * @throws IllegalAccessException 
  * @throws InstantiationException 
  */
 public class Reducer extends AbstractRecordStream
 {
 	private final RecordStream parent;
 	private final Map<String, GroupFunc> accumulator;
 	private Object prevValue = null;
 	private Record row;
 	private int idxGroupVar;
 	private final RecordMetaData rmd;
 	
 	public Reducer (RecordStream parent, String groupVar, Map<String, GroupFunc> accumulator) throws RecordStreamException
 	{
 		this.parent = parent;
 		this.accumulator = accumulator; //TODO: should really make a defensive copy
 		
 		List<String> outHeaders = new ArrayList<String>();
 		outHeaders.add (groupVar);
 		for (String header : accumulator.keySet())
 		{
 			outHeaders.add(header);
 		}
 		idxGroupVar = parent.getColumnIndex(groupVar);
 		
 		row = parent.getNext();
 		prevValue = row.getValue(idxGroupVar);
 		// Reset the accumulator at start
 		resetAccumulator();
 		rmd = new DefaultRecordMetaData(outHeaders);
 	}
 	
 	@Override
 	public int getNumCols() 
 	{
 		return rmd.getNumCols();
 	}
 
 	@Override
 	public String getColumnName(int i) 
 	{
 		return rmd.getColumnName(i);
 	}
 
 	private Record writeAccumulator() 
 	{
 		Object[] vals = new Object[getNumCols()];
 		vals[0] = prevValue;
 		for (int i = 1; i < rmd.getNumCols(); ++i)
 		{
 			String colName = rmd.getColumnName(i);
 			vals[i] = accumulator.get(colName).getResult();
 		}
 		return new DefaultRecord(rmd, vals);
 	}
 
 	private void resetAccumulator() 
 	{
 		for (String h : accumulator.keySet())
 		{
 			accumulator.get(h).clear();
 		}
 	}
 
 	private void accumulate()
 	{
 		// accumulate a row
 		for (String h : accumulator.keySet())
 		{
 			accumulator.get(h).accumulate(row);
 		}
 	}
 	
 	@Override
 	public Record getNext() throws RecordStreamException 
 	{
 		if (row == null) return null; // nothing more to return
 		
 		while (true)
 		{
 			accumulate();
 
 			row = parent.getNext();
 
 			// are we at the end?
 			if (row == null)
 			{
 				return writeAccumulator();
 			}
 			
 			// has groupVar changed?
 			if (!(row.getValue(idxGroupVar).toString().equals(prevValue.toString())))
 			{
 				Record result = writeAccumulator();
 				// start with a fresh accumulator
 				prevValue = row.getValue(idxGroupVar);
 				resetAccumulator();
 				return result;
 			}
 
 		} 		
 	}
 
 	public static class Count implements GroupFunc
 	{
 		private int count = 0;
 		public void accumulate (Record val) { count++; }
 		public Object getResult() { return count; }
 		public void clear() { count = 0; }
 	}
 
 	public static class LogAverageFloat extends AbstractGroupFunc
 	{
 		public LogAverageFloat(String col) { super(col); }
 		private int count = 0;
 		private float sum = 0;
 		public void accumulate (Record val) { int i = getIdx(val); count++; sum += (Float)val.getValue(i); }
 		public Object getResult() { 
 			double result = Math.log (sum / count); 
 			return result;
 		}
 		public void clear() { count = 0; sum = 0; }
 	}
 
 	public interface GroupFunc
 	{
 		public void accumulate(Record val);
 		public Object getResult();
 		public void clear();
 	}
 
 	public static class AverageFloat extends AbstractGroupFunc
 	{
 		public AverageFloat(String col) { super(col); }
 		private int count = 0;
 		private float sum = 0;
 		public void accumulate (Record val) { int i = getIdx(val); count++; sum += (Float)val.getValue(i); }
 		public Object getResult() { return sum / count; }
 		public void clear() { count = 0; sum = 0; }
 	}
 
 	public static class SumFloat extends AbstractGroupFunc
 	{
 		public SumFloat(String col) { super(col); }
 		private float sum = 0;
 		public void accumulate (Record val) { int i = getIdx(val); sum += (Float)val.getValue(i); }
 		public Object getResult() { return sum; }
 		public void clear() { sum = 0; }
 	}
 
 	public static class Concatenate extends AbstractGroupFunc
 	{
 		private final String sep;
 		public Concatenate(String colName, String sep) { super(colName); this.sep = sep; }
 		private boolean first = true;
 		private StringBuilder builder = new StringBuilder();
 		public void accumulate (Record val) { int i = getIdx(val); if (first) first = false; else builder.append(sep); builder.append(val.getValue(i).toString()); }
 		public Object getResult() { return builder.toString(); }			
 		public void clear() { first = true; builder = new StringBuilder(); }
 	}
 	
 	public static class AsList extends AbstractGroupFunc
 	{
 		public AsList(String col) { super(col); }
 		private List<Object> list;
 		public void accumulate (Record val) { int i = getIdx(val); list.add (val.getValue(i)); }
 		public Object getResult() { return list; }
 		public void clear() { list = new ArrayList<Object>(); }
 	}
 
 	public static class AsSet extends AbstractGroupFunc
 	{
		private Set<Object> set;		
 		public AsSet(String col) { super(col); }
 		public void accumulate (Record val) { int i = getIdx(val); set.add (val.getValue(i)); }
 		public Object getResult() { return set; }
		public void clear() { set = new HashSet<Object>(); }
 	}
 
 	public static abstract class AbstractGroupFunc implements GroupFunc
 	{
 		protected int idx = -1;
 		private final String colName;
 		public AbstractGroupFunc (String col) { this.colName = col; }
 		
 		protected int getIdx (Record val)
 		{
 			if (idx < 0)
 			{
 				// lazy initialization of idx
 				RecordMetaData rs = val.getMetaData();
 				for (int col = 0; col < rs.getNumCols(); ++col)
 					if (rs.getColumnName(col).equals(colName))
 					{
 						idx = col;
 						break;
 					}
 			}
 			return idx;
 		}
 	}
 
 	@Override
 	public int getColumnIndex(String name) 
 	{
 		return rmd.getColumnIndex(name);
 	}
 }
