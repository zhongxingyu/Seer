 /*
  * BoltUsrLastUrl 
  * 
  * 1.0 记录usr最近访问的url
  *
  * zhouxg@ucweb.com 
  */
 
 
 package cn.uc.udac.bolts;
 
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import redis.clients.jedis.Jedis;
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.BasicOutputCollector;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseBasicBolt;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 
 public class BoltUsrLastUrl extends BaseBasicBolt {
 	
 	static public Logger LOG = Logger.getLogger(BoltUsrLastUrl.class);
 	private Map _conf;
 	private Set<String> _newsSites;
 	private Jedis[] _arrRedisUsrLastUrl;
 	private int _count = 0;
 
 	private void init(Map conf) {
 		try {
 			List<String> hosts = (List<String>)conf.get("usr_last_url_redis_hosts");
 			int port = ( (Long)conf.get("redis_port") ).intValue();
 			
 			_arrRedisUsrLastUrl = new Jedis[hosts.size()];
 			
 			for (int i = 0; i < hosts.size(); ++i) {
 				_arrRedisUsrLastUrl[i] = new Jedis(hosts.get(i), port);
 			}
 			
 			List<String> sites = (List<String>)conf.get("news_sites");
 			_newsSites = new HashSet<String>(sites);
 		} catch (Exception e) {
 			LOG.info("BoltUsrLastUrl.init.exception:", e);
 		}
 	}
 	
 	@Override
 	public void prepare(Map conf, TopologyContext context) {
 		_conf = conf;
 		init(_conf);
     }
     
 	private int hash(String key, int size) {
 		int h = 0;
 		
 		for (int i = 0; i < key.length(); ++i) {
 			h += key.codePointAt(i);
 		}
 		
 		return h % size;
 	}
 	
     @Override
 	public void execute(Tuple input, BasicOutputCollector collector) {
     	try {
     		String time = input.getString(0);
 	    	String usr = input.getString(3);
 	    	String url = input.getString(5);
 	    	String site = new URL(url).getHost();
 	    	if (!_newsSites.contains(site)) return;
 	    	String key = "UsrLastUrl`" + usr;
 	    	int h = hash(key, _arrRedisUsrLastUrl.length);
 	    	int seconds = 24 * 3600;
 	    	String refer =  _arrRedisUsrLastUrl[h].get(key);
 	    	
 	    	if (++_count % 1000 == 0) {
 	    		LOG.info(String.format("BoltUsrLastUrl %d: time=%s, usr=%s, url=%s, refer=%s, key=%s", 
 	    				_count, time, usr, url, refer, key));
 	    	}
 	    	
 	    	if (refer != null && !url.equals(refer)) {
 	    		collector.emit(new Values(time, refer, url));
 	    	}
 	    	
	    	_arrRedisUsrLastUrl[h].rpush(key, url);
 	    	_arrRedisUsrLastUrl[h].expire(key, seconds);
 		} catch (Exception e) {
 			LOG.info("BoltUsrLastUrl.execute.exception:", e);
 			init(_conf);
 		}
 	}
 
     @Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
     	declarer.declare(new Fields("time", "refer", "url"));
 	}
 
 }
