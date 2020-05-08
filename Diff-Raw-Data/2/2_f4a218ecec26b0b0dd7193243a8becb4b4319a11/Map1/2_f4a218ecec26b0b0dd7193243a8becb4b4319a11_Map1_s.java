 package step1;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import structures.MatrixUtilities;
 
 import constants.Constants;
 /**
  * 
  * @author chris d
  * Reads in float values. Checks whether each value is in range. If it is, it assigns it a 
  * int representing its position (p) and a column group (G).
  */
 public class Map1 extends Mapper<LongWritable, Text, IntWritable, IntWritable> {
 	private Text word = new Text();
 
 	@Override
 	public void map(LongWritable key, Text value, Context context)
 			throws IOException, InterruptedException {
 		
 		String line = value.toString();
 		StringTokenizer tokenizer = new StringTokenizer(line);
 		
 		while (tokenizer.hasMoreTokens()) {
 			//1. get the next word
 			word.set(tokenizer.nextToken());
 			
 			//2. Convert the word to a float
 			Float val = Float.parseFloat(word.toString());	
 			
 			//3. Test the float, add the vertex if it passes, do nothing otherwise
 			if (((val >= Constants.wMin) && (val < Constants.wLimit))){
 				
 				//a. find entry number
				int N = (int)key.get()/12;	
 				
 				//b. calculate (col,row) where this entry should be placed
 				int sq = (int)Math.sqrt(N); 
 				int diff = N - sq*sq;
 				int col,row;
 				
 				if (diff==0){
 				    col = sq - 1;
 				    row = sq - 1;
 				}
 				else if (diff%2 == 0){
 				    col = sq;
 				    row = (diff-1)/2;
 				}
 				else{
 				    col = (diff-1)/2;
 				    row = sq;
 				} 
 				
 				// c. Once you know where it should be placed, calculate
 				//    its column_group (G) and its absolute position (p)
 				int col_group_int = MatrixUtilities.getColumnGroup(Constants.M, Constants.g, col);
 				
 				IntWritable column_group = new IntWritable(col_group_int);
 				IntWritable position = new IntWritable((col*Constants.M)+(row+1));
 				
 				// d. write out (G,p)
 				context.write(column_group, position);
 				
 				// e. if N is on a group boundary, you must also add it to its neighboring group
 				int boundary = MatrixUtilities.isBoundary(position.get());
 				if(boundary!=0)
 					context.write(new IntWritable(col_group_int+boundary), position);
 			}
 		}
 	}
 }
