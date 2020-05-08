 package cn.uc.udac.zjj.bolts;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 public class BoltCrawler extends BaseRichBolt {
 
 	static public Logger LOG = Logger.getLogger(BoltCrawler.class);
 	private OutputCollector _collector;
 	private HTable _t_pages;
 	private long _count = 0;
 
 	@Override
 	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
 		_collector = collector;
 		Configuration hbconf = HBaseConfiguration.create();
 		try {
 			_t_pages = new HTable(hbconf, "t_pages");
     	}
     	catch (Exception e) {
     		LOG.info("BoltCrawler.prepare.exception:", e);
     	}
     }
     
 	private String getKey(String str) throws Exception {
 		String key = "";
 		URI uri = new URI(str);
 		String scheme = uri.getScheme();
 		String host = uri.getHost();
 		String fragment = uri.getFragment();
 		if (scheme.length() > 0)
 			key = str.substring(scheme.length()+3);
 		key = key.substring(host.length());
 		String[] ps = host.split("\\.");
 		if (ps.length > 0)
 			key = ps[0] + key;
 		for (int i = 1; i < ps.length; i+=1)
 			key = ps[i] + "." + key;
 		if (fragment != null)
 			key = key.substring(0, key.length()-fragment.length()-1);
 		return key;
 	}
 	
 	private String[] urlOpen(String url) throws Exception {
 		String title = "";
 		String text = "";
 		String key = getKey(url);
 		Get row = new Get(key.getBytes());
 		row.addColumn("m".getBytes(), "Data".getBytes());
 		Result r = _t_pages.get(row);
 		if (!r.isEmpty()) {
			byte[] bs = r.getValue("m".getBytes(), "Data".getBytes());
			String page = new String(bs, "UTF-8");
			String sgbk = new String(bs, "GBK");
			if (sgbk.length() < page.length())
				page = sgbk;
			if (page.length() < bs.length) {
				Document doc = Jsoup.parse(page);
				Elements ps = doc.getElementsByTag("p");
				title = doc.title();
				for (Element p : ps)
					text += p.text() + "\n";
			}
 		}
 		String[] a = new String[2];
 		a[0] = title;
 		a[1] = text;
 		return a;
 	}
 	
 	@Override
 	public void execute(Tuple input) {
 		if (input.size() != 2)
 			return;
 		try {
 			String date = input.getString(0);
 			String url = input.getString(1);
 			String site = new URL(url).getHost();
 			String[] a = urlOpen(url);
 			String title = a[0];
 			String text = a[1];
 			if (_count++ % 1000 == 0)
 				LOG.info(String.format("BoltCrawler.execute(%d): date=%s, url=%s, title=%s, text=%s", _count, date, url, title, text));
 			if (site.length()>0 && date.length()>0 && text.length()>0)
 				_collector.emit(input, new Values(site, date, title, text));
 		}
 		catch (Exception e) {
 			LOG.info("BoltCrawler.execute.exception:", e);
 		}
 	}
 
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		declarer.declare(new Fields("site", "date", "title", "text"));
 	}
 
 }
