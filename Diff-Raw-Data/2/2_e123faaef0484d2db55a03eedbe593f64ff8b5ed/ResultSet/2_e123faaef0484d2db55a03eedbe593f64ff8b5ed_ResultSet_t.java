 package uk.ac.nott.mrl.homework.server.model;
 
 import java.util.Iterator;
 
 public class ResultSet implements Iterable<String[]>
 {
 	private class ResultSetIterator implements Iterator<String[]>
 	{
 		private final String[] lines;
 		private int index = 1;
 		
 		public ResultSetIterator(final String[] lines)
 		{
 			this.lines = lines;
 			System.out.println(lines.length);
 		}
 
 		@Override
 		public boolean hasNext()
 		{
 			return (index + 1) < lines.length;
 		}
 
 		@Override
 		public String[] next()
 		{
 			index++;
 			System.out.println(index);			
 			return lines[index].split("<\\|>");			
 		}
 
 		@Override
 		public void remove()
 		{
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	private final String[] lines;
 	
 	public ResultSet(final String results)
 	{
 		lines = results.split("\n");
 	}
 	
 	public int getSize()
 	{
		return Math.max(0,  lines.length - 2);
 	}
 	
 	public Iterator<String[]> iterator()
 	{
 		return new ResultSetIterator(lines);
 	}
 	
 	public boolean isSuccessful()
 	{
 		return lines[0].startsWith("<|>0<|>SUCCESS<|>");
 	}
 }
