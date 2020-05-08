 package tourist.storm;
 
 import backtype.storm.Config;
 import backtype.storm.LocalCluster;
 import backtype.storm.topology.TopologyBuilder;
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.service.IoConnector;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  * TouristTopology Tester.
  */
 public class TouristTopologyTest {
 
 
     @Before
     public void before() throws Exception {
     }
 
     @After
     public void after() throws Exception {
 
     }
 
     /**
      * Method: main(String[] args)
      */
     @Test
     public void testMain() throws Exception {
         TopologyBuilder builder = tourist2.storm.TouristTopology.getTopologyBuilder();
         Config conf = new Config();
         conf.setDebug(true);
 
         LocalCluster cluster = new LocalCluster();
         cluster.submitTopology("tourist-count", conf, builder.createTopology());
 
         Thread.sleep(4 * 1000);
         Sender sender = new Sender(5001);
 //        sender.send("tourist 1", "2013-01-04 08:00:00", "", "tourist");
 //        sender.send("tourist 1", "2013-01-04 08:00:00", "", "tourist");
         sender.send("worker 1", "2013-01-04 00:29:00", "", "out");
 
         sender.send("worker 1", "2013-01-04 00:53:00", "", "out");
 //
         sender.send("worker 1", "2013-01-04 08:00:00", "", "tourist");
         sender.send("worker 1", "2013-01-04 11:02:00", "", "out");
 //
 //
 //        sender.send("worker 1", "2013-01-06 08:00:00", "", "tourist");
 //        sender.send("worker 1", "2013-01-06 11:01:00", "", "out");
 ////
 //        sender.send("worker 1", "2013-01-07 08:00:00", "", "tourist");
 //        sender.send("worker 1", "2013-01-07 11:01:00", "", "out");
 //
 //        sender.send("worker 1", "2013-01-08 08:00:00", "", "tourist");
 //        sender.send("worker 1", "2013-01-08 11:01:00", "", "out");
 //
 //        sender.send("worker 1", "2013-01-09 08:00:00", "", "tourist");
 //        sender.send("worker 1", "2013-01-09 11:01:00", "", "out");
 
 
         Thread.sleep(1 * 1000);
         sender.close();
         Thread.sleep(1 * 1000);
         cluster.shutdown();
     }
 
     @Test
     public void testMain2() throws Exception {
         TopologyBuilder builder = TouristTopology.getTopologyBuilder();
         Config conf = new Config();
         conf.setDebug(true);
 
         LocalCluster cluster = new LocalCluster();
         cluster.submitTopology("tourist-count", conf, builder.createTopology());
 
         Thread.sleep(4 * 1000);
         Sender sender = new Sender(5001);
         sender.send("100001019584781", "2013-01-01 00:43:35", "chp", "tourist");
         sender.send("100001019584781", "2013-01-01 00:52:49", "xc", "tourist");
 
         Thread.sleep(1 * 1000);
         sender.close();
         Thread.sleep(1 * 1000);
         cluster.shutdown();
     }
 
     @Test
     public void testMain3() throws Exception {
         TopologyBuilder builder = TouristTopology.getTopologyBuilder();
         Config conf = new Config();
         conf.setDebug(true);
 
         LocalCluster cluster = new LocalCluster();
         cluster.submitTopology("tourist-count", conf, builder.createTopology());
 
         Thread.sleep(4 * 1000);
         Sender sender = new Sender(5001);
         BufferedReader reader =null;
         try {
 //            reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("a.csv")));
             reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("data.csv")));
             for(String line= reader.readLine();line!=null;line=reader.readLine()){
                 String signal = line.substring(0, line.lastIndexOf(","));
                 sender.send(signal);
             }
         } finally {
             if(reader!=null){
                 reader.close();
             }
         }
         Thread.sleep(1000);
         sender.close();
 
         cluster.shutdown();
     }
 
     private static class Sender extends IoHandlerAdapter {
         private final IoConnector connector;
         private final IoSession session;
 
         private Sender(int port) {
             connector = new NioSocketConnector();
             connector.setHandler(this);
             connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
             ConnectFuture future = connector.connect(new InetSocketAddress(port));
             future.awaitUninterruptibly();
             session = future.getSession();
         }
 
         @Override
         public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);    //To change body of overridden methods use File | Settings | File Templates.
         }
 
         public void send(String imsi, String time, String loc, String cell) throws ParseException {
             StringBuilder sb = new StringBuilder();
             session.write(sb.append(imsi).append(",").append(getTime(time)).append(",").append(loc).append(",").append(cell));
         }
 
         public void send(String imsi, long time, String loc, String cell) throws ParseException {
             StringBuilder sb = new StringBuilder();
             session.write(sb.append(imsi).append(",").append(time).append(",").append(loc).append(",").append(cell));
         }
 
         public void send(String signal) throws ParseException, InterruptedException {
             session.write(signal).await();
         }
 
         public void close() throws InterruptedException {
             session.close(false).await();
             connector.dispose();
         }
     }
 
     private static long getTime(String s) throws ParseException {
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(s + " +0000").getTime();
     }
 
     private static String getTime(long s) throws ParseException {
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(s - TimeZone.getDefault().getRawOffset()));
     }
 
     public static void main(String[] args) throws ParseException {
         System.out.println(getTime(1356999187197L));
         System.out.println(getTime(1357001569894L));
     }
 }
