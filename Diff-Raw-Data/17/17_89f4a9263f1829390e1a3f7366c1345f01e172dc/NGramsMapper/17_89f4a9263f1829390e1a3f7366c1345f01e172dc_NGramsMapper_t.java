 package mx.itam.metodos.mr;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import com.google.common.base.Joiner;
 
 public final class NGramsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
 
   private int n;
 
   @Override
   public void map(LongWritable id, Text value, Context context) throws IOException, InterruptedException {
    String[] values = value.toString().split("\t");
    if (values.length > 1) {
      tokenize(values[1], context);
    }
  }
    private void tokenize(String text, Context context) throws IOException, InterruptedException {
     String[] array = text.split("\\s");
     Joiner joiner = Joiner.on(",");
     Text key = new Text();
     IntWritable count = new IntWritable(1);
     for (int i = 0; i < array.length - n ; i++) {
       String[] sub = Arrays.copyOfRange(array, i, i + n);
       String ngram = joiner.join(sub);
       key.set(ngram);
       context.write(key, count);
     }
   }
   
   @Override
   public void setup(Context context) {
     this.n = context.getConfiguration().getInt(CountNGrams.N, 3);
   }
 }
