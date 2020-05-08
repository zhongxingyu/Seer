 package cn.uc.udac.zjj.bolts;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Increment;
 import org.apache.log4j.Logger;
 
 import cn.uc.udac.mqs.UCMessageQueue;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.tuple.Tuple;
 
 
 public class BoltSiteDatePv extends BaseBasicBolt {
 
 	static public Logger LOG = Logger.getLogger(BoltSiteDatePv.class);
 	private int _count = 0;
 	private OutputCollector _collector;
 	private HTable _t_site_date_pv;
 
     public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
         _collector = collector;
         Configuration hbconf = HBaseConfiguration.create();
         try {
         	_t_site_date_pv = new HTable(hbconf, "t_site_date_pv");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
 	public void execute(Tuple input, BasicOutputCollector collector) {
 		if (input.size() != 5)
 			return;
 		if (_count++ % 1000 == 0)
 			LOG.info(String.format("BoltSiteDatePv.count = %d", _count));
 		String time = input.getString(0);
 		String url = input.getString(4);
 		try {
 			String date = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time));
 			String site = new URL(url).getHost();
			_t_site_date_pv.incrementColumnValue(site.getBytes(), "pv".getBytes(), "date".getBytes(), 1);
 		}
 		catch (Exception e) {
 			LOG.info("BoltSiteDatePv.exception = ", e);
 		}
 	}
 
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 	}
 
 }
