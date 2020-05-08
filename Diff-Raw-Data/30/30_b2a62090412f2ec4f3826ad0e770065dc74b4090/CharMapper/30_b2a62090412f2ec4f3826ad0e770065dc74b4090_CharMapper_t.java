 package charCount;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 public class CharMapper extends Mapper<Text, Text, Text, LongWritable> {
 	private int size = 4;
 	private static final LongWritable ONE = new LongWritable(1L);
	private StringBuilder builder = new StringBuilder(size);
	private char[] cycle = new char[size];
 
 	public void map(Text key, Text val, Context context) {
 		String line = val.toString();
 
 		// ~ Check if this line is worth spending effort on
 		if (line.length() >= this.size) {
 			int i = 0;
 
 			// ~ Fill the buffer
 			int useless = 0;
 			for (int j = 0; (j < this.size + useless) && (j < line.length()); j++) {
 				char currChar = Character.toLowerCase(line.charAt(j));
 
 				// Only a-z
				if (isValid(currChar)) {
					this.cycle[i] = currChar;
 					i++;
 				} else {
 					useless++;
 				}
 			}
 
 			// ~ Start looping through the rest of the string
 			for (int j = (this.size - 1); j < line.length(); j++) {
				char currChar = Character.toLowerCase(line.charAt(j));
				if (isValid(currChar)) {
 					i = i % 3;
					this.cycle[i] = currChar;
 
 					// Build the output from the cycle
					builder.delete(0, size);
					builder.append(this.cycle, i, (this.cycle.length - i));
					builder.append(this.cycle, 0, i);
 
 					i++;
 
 					try {
 						context.write(new Text(builder.toString()), ONE);
 					} catch (Exception e) {
 					}
 				}
 			}
 		}
 	}

	private boolean isValid(char c) {
		return ('a' <= c && c <= 'z') || c == ' ';
	}
 }
