 package edu.umich.cse.eecs485;
 
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
 public class InvertedIndex3 {
 
    public static double totalDoc = 0;
     public static class Map extends Mapper<LongWritable, Text, Text, Text> {
         public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException { 
             // value is a line 
             String line = value.toString();
 
             String[] splited_key_value = line.split("\\s+");
             String term = splited_key_value[0];
 
             String[] splited_df_list = splited_key_value[1].split(";");
             String df = splited_df_list[0];
             String list = splited_df_list[1];
 
             String[] docs = list.split(",");
 
             String[] splited_docid_tf;
             String docid;
             String tf;
             for (int i=0; i<docs.length; i++) {
                 splited_docid_tf = docs[i].split(":"); 
                 docid = splited_docid_tf[0];
                 tf = splited_docid_tf[1];
 
                 context.write(new Text(docid), new Text(String.format("%s:%s:%s", term, df, tf)));
                totalDoc++;
             }
         }
     }
 
     public static class Reduce extends Reducer<Text, Text, Text, Text> {
         public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
             StringBuilder list = new StringBuilder(); 
 
             double tfidf;
 
             String term, df, tf;
             String[] term_df_tf;
             
             for (Text value : values) {
                 term_df_tf = value.toString().split(":");
                 term = term_df_tf[0];
                 df = term_df_tf[1];
                 tf = term_df_tf[2];
                 double tfD = Double.parseDouble(tf);
                 double dfD = Double.parseDouble(df);
                double idf = Math.log10(totalDoc / dfD);
                tfidf = tfD * idf;
                 list.append(String.format("%s:%s:%f,", term, df, tfidf));
             }
     
             // remove trailing coma 
             list.deleteCharAt(list.length()-1);
             context.write(key, new Text(list.toString()));
         }
     }
 
     public static void main(String[] args) throws Exception
     {
         Configuration conf = new Configuration();
 
         Job job = new Job(conf, "InvertedIndex3");
 
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
 
         job.setMapperClass(Map.class);
         job.setReducerClass(Reduce.class);
 
         job.setInputFormatClass(TextInputFormat.class);
         job.setOutputFormatClass(TextOutputFormat.class);
 
         FileInputFormat.addInputPath(job, new Path(args[0]));
         FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
         job.waitForCompletion(true);
     }
 }
