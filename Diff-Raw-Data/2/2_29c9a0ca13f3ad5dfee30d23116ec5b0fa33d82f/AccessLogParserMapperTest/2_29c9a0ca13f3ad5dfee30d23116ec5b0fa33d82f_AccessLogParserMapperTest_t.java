 package ru.develbureau.mrtesting.mapreduce;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mrunit.mapreduce.MapDriver;
 import org.apache.hadoop.mrunit.types.Pair;
 import org.testng.annotations.Test;
 import ru.develbureau.mrtesting.model.Counter;
 import ru.develbureau.mrtesting.model.LoggedRequest;
 import ru.develbureau.mrtesting.parser.ApacheLogParser;
 import ru.develbureau.mrtesting.parser.ParserException;
 
 import java.io.IOException;
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 /**
  * User: sergey.sheypak
  * Date: 23.03.13
  * Time: 16:09
  */
 public class AccessLogParserMapperTest {
 
     private final AccessLogParserMapper mapper = new AccessLogParserMapper();
    private final MapDriver<LongWritable, Text, LoggedRequest, IntWritable> mapDriver = new MapDriver<LongWritable, Text, LoggedRequest, IntWritable>();
     private final ApacheLogParser parser = new ApacheLogParser();
 
     private final LongWritable key = new LongWritable(1);
     private final IntWritable one = new IntWritable(1);
 
     @Test
     public void mapWithGoodRecord() throws ParserException{
         mapDriver.setMapper(mapper);
         mapDriver.withInput(key, new Text("205.189.154.54 - - [01/Jul/1995:00:00:29 -0400] \"GET /shuttle/countdown/count.gif HTTP/1.0\" 200 40310"))
                  .withOutput(new LoggedRequest(parser.parseTimestamp("01/Jul/1995:00:00:00"), "/shuttle/countdown/count.gif", 200), one)
                  .runTest();
     }
 
     @Test
     public void mapWithBadRecord() throws ParserException, IOException{
         mapDriver.setMapper(mapper);
         List<Pair<LoggedRequest, IntWritable>> pair = mapDriver
                  .withInput(key, new Text("205.189.154.54 - - [01/Jul/1995 -0400] \"GET /shuttle/countdown/count.gif HTTP/1.0\" 200 40310"))
                  .run();
         assertThat(pair.size(), equalTo(0));
         assertThat(mapDriver.getCounters().findCounter(Counter.CORRUPTED_RECORD).getValue(), equalTo(1l));
     }
 
 
     @Test
     public void mapWithReplyCode500() throws ParserException, IOException{
         mapDriver.setMapper(mapper);
         List<Pair<LoggedRequest, IntWritable>> pair = mapDriver
                 .withInput(key, new Text("63.205.1.45 - - [03/Jul/1995:10:49:40 -0400] \"GET /cgi-bin/geturlstats.pl HTTP/1.0\" 500 0"))
                 .run();
         assertThat(pair.size(), equalTo(0));
         assertThat(mapDriver.getCounters().findCounter(Counter.SERVER_ERROR_500).getValue(), equalTo(1l));
     }
 
 }
