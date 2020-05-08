 package csv;
 
 import java.io.PrintStream;
 import java.util.List;
 
 public class CSVWriter
 {
 	PrintStream out;
 
 	int columnCount;
 
 	int currentColumnCount;
 
 	public CSVWriter(PrintStream stream)
 	{
 		out = stream;
 
 		columnCount = -1;
 
 		currentColumnCount = 0;
 	}
 
 	public void write(Object value)
 	{
		if (columnCount != -1 && currentColumnCount >= columnCount)
 			throw new RuntimeException("First line consisted of " + columnCount + " columns, but now trying to one more.");
 
 		if (currentColumnCount++ > 0)
 			out.print(",");
 
 		out.print(escape(value.toString()));
 	}
 
 	public void writeln(List values)
 	{
 		for (int i = 0; i < values.size(); ++i)
 			write(values.get(i));
 
 		out.println();
 
 		if (columnCount == -1)
 			columnCount = currentColumnCount;
 
 		currentColumnCount = 0;
 	}
 
 	private String escape(String value)
 	{
 		return "\"" + value.replace("\"", "\"\"") + "\"";
 	}
 }
