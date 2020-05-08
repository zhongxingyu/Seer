 package charCount;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class CharMapper extends Mapper<Text, Text, Text, LongWritable> {
 	private int size = 4;
 	private static final LongWritable ONE = new LongWritable(1L);
	private StringBuilder builder;
 
 	public void map(Text key, Text val, Context context) {
 		String line = val.toString();
 
 		// ~ Check if this line is worth spending effort on
 		if (line.length() >= this.size) {
			char[] cycle = new char[this.size];
 			int i = 0;
 
 			// ~ Fill the buffer
 			int useless = 0;
 			for (int j = 0; (j < this.size + useless) && (j < line.length()); j++) {
 				char currChar = Character.toLowerCase(line.charAt(j));
 
 				// Only a-z
				if (('a' <= currChar && currChar <= 'z') || currChar == ' ') {
					cycle[i] = currChar;
 					i++;
 				} else {
 					useless++;
 				}
 			}
 
 			// ~ Start looping through the rest of the string
 			for (int j = (this.size - 1); j < line.length(); j++) {
				char currChar = line.charAt(j);
				if (('a' <= currChar && currChar <= 'z') || currChar == ' ') {
 					i = i % 3;
					cycle[i] = currChar;
 
 					// Build the output from the cycle
					builder = new StringBuilder(cycle.length);
					builder.append(cycle, i, (cycle.length - i));
					builder.append(cycle, 0, i);
 
 					i++;
 
 					try {
 						context.write(new Text(builder.toString()), ONE);
 					} catch (Exception e) {
 					}
 				}
 			}
 		}
 	}
 }
