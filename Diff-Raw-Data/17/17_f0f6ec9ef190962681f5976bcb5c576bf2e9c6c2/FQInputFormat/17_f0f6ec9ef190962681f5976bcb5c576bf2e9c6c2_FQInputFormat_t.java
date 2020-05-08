 package sandbox;
 
 import genelab.Conf;
 import java.io.IOException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.util.LineReader;
 
 /**
  *
  * @author yukuwang
  */
 public class FQInputFormat extends TextInputFormat {
 
     @Override
     public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
         return new NLinesRecordReader();
     }
 
     private static class NLinesRecordReader extends RecordReader<LongWritable, Text> {
 
         private final int NLINESTOPROCESS = Conf.N_LINES_PER_CHUNKS;
         private LineReader in;
         private LongWritable key;
         private Text value = new Text();
         private long start = 0;
         private long end = 0;
         private long pos = 0;
        private long keyValue = 1;
        private int maxLineLength = Conf.MAX_LINE_LENGTH;
 
         @Override
         public void close() throws IOException {
             if (in != null) {
                 in.close();
             }
         }
 
         @Override
         public LongWritable getCurrentKey() throws IOException, InterruptedException {
             return key;
         }
 
         @Override
         public Text getCurrentValue() throws IOException, InterruptedException {
             return value;
         }
 
         @Override
         public float getProgress() throws IOException, InterruptedException {
             if (start == end) {
                 return 0.0f;
             } else {
                 return Math.min(1.0f, (pos - start) / (float) (end - start));
             }
         }
 
         @Override
         public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException, InterruptedException {
             FileSplit split = (FileSplit) genericSplit;
             final Path file = split.getPath();
             Configuration conf = context.getConfiguration();
             FileSystem fs = file.getFileSystem(conf);
             start = split.getStart();
             end = start + split.getLength();
             boolean skipFirstLine = false;
             FSDataInputStream filein = fs.open(split.getPath());
 
             if (start != 0) {
                 skipFirstLine = true;
                 --start;
                 filein.seek(start);
             }
             in = new LineReader(filein, conf);
             if (skipFirstLine) {
                 start += in.readLine(new Text(), 0, (int) Math.min((long) Integer.MAX_VALUE, end - start));
             }
             this.pos = start;
         }
 
         @Override
         public boolean nextKeyValue() throws IOException, InterruptedException {
             if (key == null) {
                 key = new LongWritable();
             }
             key.set(keyValue);
             if (value == null) {
                 value = new Text();
             }
             value.clear();
             final Text endline = new Text("\n");
             int newSize = 0;
            for (int i = 0; i < NLINESTOPROCESS - 1; i++) {
                 Text v = new Text();
                 while (pos < end) {
                     newSize = in.readLine(v, maxLineLength, Math.max((int) Math.min(Integer.MAX_VALUE, end - pos), maxLineLength));
                     value.append(v.getBytes(), 0, v.getLength());
                     value.append(endline.getBytes(), 0, endline.getLength());
                     if (newSize == 0) {
                         break;
                     }
                     pos += newSize;
                     if (newSize < maxLineLength) {
                         break;
                     }
                 }
             }
            Text v = new Text();
            newSize = in.readLine(v, maxLineLength, Math.max((int) Math.min(Integer.MAX_VALUE, end - pos), maxLineLength));
            value.append(v.getBytes(), 0, v.getLength());
             if (newSize == 0) {
                 key = null;
                 value = null;
                 return false;
             } else {
                 keyValue++;
                 return true;
             }
         }
     }
}
