 import java.io.BufferedReader;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.FileSplit;
 import org.apache.hadoop.mapred.InputSplit;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.RecordReader;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.mapred.TextOutputFormat;
 
 public class NGramHighScore {
 
   public static class FullFileInputFormat extends FileInputFormat<Text, Text> {
 
     @Override
     public boolean isSplitable(FileSystem fs, Path filename) {
       return false;
     }
 
     @Override
     public RecordReader<Text, Text> getRecordReader(InputSplit split,
         JobConf job, Reporter reporter) throws IOException {
       return new FullFileRecordReader((FileSplit) split, job);
     }
 
   }
 
   public static class FullFileRecordReader implements RecordReader<Text, Text> {
 
     private long indexOfNextToProcess;
     private String fileText;
     private FileSplit fileSplit;
     private Configuration conf;
     Pattern p = null;
     Matcher m = null;
 
     public FullFileRecordReader(FileSplit split, JobConf job) {
       indexOfNextToProcess = 0;
       this.fileSplit = split;
       this.conf = job;
 
       byte[] contents = new byte[(int) fileSplit.getLength()];
       Path file = fileSplit.getPath();
 
       FSDataInputStream in = null;
       try {
         FileSystem fs = file.getFileSystem(conf);
         in = fs.open(file);
         IOUtils.readFully(in, contents, 0, contents.length);
         if (contents.length > 0) {
           fileText = new String(contents);
           p = Pattern.compile("<title>([^<]*)</title>");
           m = p.matcher(fileText);
           m.find();
         }
       } catch (IOException e) {
         e.printStackTrace();
       } finally {
         IOUtils.closeStream(in);
       }
     }
 
     @Override
     public boolean next(Text key, Text value) throws IOException {
 
       if (fileText == null || fileText.length() == 0 || indexOfNextToProcess == fileText.length()) {
         return false;
       }
 
       int pageStartIndex = m.end();
       int pageEndIndex;
 
       String title = m.group(1);
       if (m.find()) {
         pageEndIndex = m.start();
       } else {
         pageEndIndex = fileText.length();
       }
 
       indexOfNextToProcess = pageEndIndex;
 
       String pageText = fileText.substring(pageStartIndex, pageEndIndex);
 
       key.set(title);
       value.set(pageText);
 
       return true;
     }
 
     @Override
     public long getPos() throws IOException {
       return indexOfNextToProcess;
     }
 
     @Override
     public float getProgress() throws IOException {
       if (fileText == null || fileText.length() == 0) {
         return 1.0f;
       }
       return ((float)indexOfNextToProcess)/fileText.length();
     }
 
     @Override
     public void close() throws IOException {}
 
     @Override
     public Text createKey() {
       return new Text();
     }
 
     @Override
     public Text createValue() {
       return new Text();
     }
 
   }
 
 
 
   public static class Map extends MapReduceBase implements Mapper<Text, Text, Text, PageScorePair> {
 
     private static final String DUMMY_KEY = "dummy";
 
     private int nGramSize;
     private String queryText;
 
 
     public void configure(JobConf conf) {
       nGramSize = conf.getInt("nGramSize", 1);
       queryText = conf.get("query text");
     }
 
     public void map(Text key, Text value, OutputCollector<Text, PageScorePair> output, Reporter reporter) throws IOException {
 
       Set<String> nGramsForQuery = createNGramsFromString(queryText);
 
       String title = key.toString();
       String pageText = value.toString();
       Tokenizer tokenizer = new Tokenizer(pageText);
 
       int count = 0;
       int i = 0;
       String justProcessedNGram = "";
       while (tokenizer.hasNext()) {
         String word = tokenizer.next();
         if (i != nGramSize) {
           i++;
           if (i == nGramSize) {
             justProcessedNGram += word;
             if (nGramsForQuery.contains(justProcessedNGram)) {
               count++;
             }
           } else {
             justProcessedNGram += word + " ";
           }
         } else {
           justProcessedNGram = justProcessedNGram.substring(justProcessedNGram.indexOf(" ") + 1) + " " + word;
           if (nGramsForQuery.contains(justProcessedNGram)) {
             count++;
           }
         }        
       }
       output.collect(new Text(DUMMY_KEY), new PageScorePair(title, count));
     }
 
     private Set<String> createNGramsFromString(String text) {
       Set<String> nGrams = new HashSet<String>();
 
       int i = 0;
       String justAddedNGram = "";
 
       Tokenizer tokenizer = new Tokenizer(text);
       while (tokenizer.hasNext()) {
         if (nGrams.size() == 0) {
           i++;
           if (i != nGramSize) {
             justAddedNGram += tokenizer.next() + " ";
           } else {
             justAddedNGram += tokenizer.next();
             nGrams.add(justAddedNGram);
           }
         } else {
           justAddedNGram = justAddedNGram.substring(justAddedNGram.indexOf(" ") + 1) + " " + tokenizer.next();
           nGrams.add(justAddedNGram);
         }        
       }
       return nGrams;
     }
   }
 
   public static class Reduce extends MapReduceBase implements Reducer<Text, PageScorePair, Text, PageScorePair> { 
 
     private static final String DUMMY_KEY = "dummy";
 
     public void reduce(Text key, Iterator<PageScorePair> values, OutputCollector<Text, PageScorePair> output, Reporter reporter) throws IOException {
       PageScorePair top = null;
       PageScorePair tracker = null;
       while (values.hasNext()) {
         tracker = values.next();
         if (top == null) {
           top = new PageScorePair(tracker.getPageTitle(), tracker.getScore());
           continue;
         }
         if (top.getScore() < tracker.getScore()) {
           top = new PageScorePair(tracker.getPageTitle(), tracker.getScore());
         } else if (top.getScore() == tracker.getScore()) {
           if (top.getPageTitle().compareTo(tracker.getPageTitle()) < 0) {
             top = new PageScorePair(tracker.getPageTitle(), tracker.getScore());
           }
         }
       }
       if (top != null)
         output.collect(new Text(DUMMY_KEY), top);
     }
 
   }
 
   public static class PageScorePair implements Writable{
     private String pageTitle;
     private int score;
 
     public PageScorePair() {}
 
     public PageScorePair(String pageTitle, int score) {
       this.pageTitle = pageTitle;
       this.score = score;
     }
 
     public String getPageTitle() {
       return pageTitle;
     }
 
     public int getScore() {
       return score;
     }
 
     @Override
     public void readFields(DataInput in) throws IOException {
       pageTitle = in.readUTF();
       score = in.readInt();
     }
 
     @Override
     public void write(DataOutput out) throws IOException {
       out.writeUTF(pageTitle);
       out.writeInt(score);
     }
 
     public static PageScorePair read(DataInput in) throws IOException {
       PageScorePair psp = new PageScorePair();
       psp.readFields(in);
       return psp;
     }
 
     @Override
     public String toString() {
       return "PageScorePair [pageTitle=" + pageTitle + ", score=" + score + "]";
     }
   }
 
 
   public static void main(String[] args) throws Exception {
     File file = new File(args[1]);
     int nGramSize = Integer.parseInt(args[0]);
 
     JobConf conf = new JobConf(NGramHighScore.class);
     conf.setJobName("NGramHighScore");
 
     String queryText = getQueryText(file);
     conf.set("query text", queryText);
     conf.setInt("nGramSize", nGramSize);
 
     conf.setOutputKeyClass(Text.class);
     conf.setOutputValueClass(PageScorePair.class);
 
     conf.setMapperClass(Map.class);
     conf.setCombinerClass(Reduce.class);
     conf.setReducerClass(Reduce.class);
 
     conf.setInputFormat(FullFileInputFormat.class);
     conf.setOutputFormat(TextOutputFormat.class);
 
     FileInputFormat.setInputPaths(conf, new Path(args[2]));
     FileOutputFormat.setOutputPath(conf, new Path(args[3]));
 
     JobClient.runJob(conf);
   }
 
   private static String getQueryText(File file) {
     BufferedReader reader = null;
     StringBuilder sb = null;
     try {
       reader = new BufferedReader(new FileReader(file));
       sb = new StringBuilder();
       String text = null;
       while ((text = reader.readLine()) != null) {
         sb.append(text);
       }
     } catch (IOException e) {
       e.printStackTrace();
     } finally {
       try {
         if (reader != null) {
           reader.close();
         }
       } catch (IOException e) {
         e.printStackTrace();
       }
     }
     return sb.toString();
   }
 
 }
