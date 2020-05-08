 package TRANS.MR.Median.Reducer;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.apache.hadoop.io.DoubleWritable;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.Counters.Counter;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Reducer.Context;
 import org.jdom2.JDOMException;
 
 import TRANS.Array.ArrayID;
 import TRANS.Array.DataChunk;
 import TRANS.Array.OptimusArray;
 import TRANS.Array.OptimusZone;
 import TRANS.Array.PID;
 import TRANS.Array.Partition;
 import TRANS.Array.RID;
 import TRANS.Array.ZoneID;
 import TRANS.Client.ZoneClient;
 import TRANS.Exceptions.WrongArgumentException;
 import TRANS.MR.Median.StrideResult;
 import TRANS.MR.Median.StripeMedianResult;
 import TRANS.Protocol.OptimusCatalogProtocol;
 import TRANS.Protocol.OptimusDataProtocol;
 import TRANS.util.Host;
 import TRANS.util.OptimusConfiguration;
 import TRANS.util.TRANSDataIterator;
 
 public class MedianReducer extends 	Reducer<IntWritable, StripeMedianResult, IntWritable, DoubleWritable> {
 	
 	private int []stride = null;
 	private int []parshape = null;
 	private int []rangeOff = null;
 	private int []resultShape = null;
	private Object []result = null;
 	private int cur = 0;
 	
 	private String confDir=null;
 	boolean isFirst = true;
 	private DataChunk localChunk = null;
 	private int zid = 0;
 	private int arrayid = 0;
 	private int rnum = 0;
 	private Counter c = null;
 	@Override
 	protected void setup(Context context) throws IOException,
 			InterruptedException {
 		JobConf conf = (JobConf)context.getConfiguration();
 		String s = conf.get("TRANS.range.stride");
 
 		String roff = conf.get("TRANS.range.offset");
 		String pshape = conf.get("TRANS.output.array.pshape");
 		this.arrayid = conf.getInt("TRANS.output.array.id", -1);
 		this.zid = conf.getInt("TRANS.output.array.zoneid", -1);
 		this.rnum = conf.getInt("TRANS.output.array.rnum", -1);
 		if(this.arrayid < 0 || this.zid < 0 || this.rnum < 0)
 		{
 			throw new IOException("Wrong Array ID Get");
 		}
 		confDir = conf.get("TRANS.conf.dir");
 	
 		if(confDir==null)
 		{
 			confDir = System.getenv("OPTIMUS_CONF");
 		}
 		String []st = s.split(",");
 		String []pstr = pshape.split(",");
 		String []offs = roff.split(",");
 	
 		
 		stride = new int[st.length];
 		parshape = new int[st.length];
 		rangeOff = new int[st.length];
 		
 		resultShape = new int [st.length];
 		
 		for(int i = 0; i < st.length; i++)
 		{
 			stride[i] = Integer.parseInt(st[i]);
 			parshape[i]=Integer.parseInt(pstr[i]);
 			rangeOff[i]=Integer.parseInt(offs[i]);
 			
 			resultShape[i] = rangeOff[i]/stride[i];
 		}	
 			
 		localChunk = new DataChunk(resultShape, parshape);
 		c = (Counter)context.getCounter("TRANS_WRITE","REDUCE_WRITE_SIZE");
 		super.setup(context);
 	}
 
 	public void reduce(IntWritable key, Iterable<StripeMedianResult> values,
 			Context context) throws InterruptedException, IOException {
 		if(this.isFirst){
 			this.isFirst = false;
 			localChunk.getChunkByOff(key.get());
 			int[] s = localChunk.getChunkSize();
 			int len = 1;
 			for(int i = 0; i < s.length ; i++)
 			{
 				len *= s[i];
 			}
			this.result = new Object[len];
 		}
 		Iterator<StripeMedianResult> it = values.iterator();
 		StripeMedianResult result = it.next();
 		if(result.isFull())
 		{
 			this.result[this.cur++] = result.getResult();
 			//context.write(key, new DoubleWritable(result.getResult()));
 		}else{
 			while(it.hasNext())
 			{
 				result.add(it.next());
 			}
 			this.result[this.cur++] = result.getResult();
 		}
 	}
 
 	@Override
 	protected void cleanup(org.apache.hadoop.mapreduce.Reducer.Context context)
 			throws IOException, InterruptedException {
 		
 		TRANSDataIterator itr = new TRANSDataIterator(this.result,localChunk.getStart(),localChunk.getChunkSize());
 		
 		Partition p = new Partition(new ZoneID(zid), new ArrayID(arrayid),
 				new PID(localChunk.getChunkNum()), new RID(rnum - 1));
 		ZoneClient client = null;
 		try {
 			client = new ZoneClient(new OptimusConfiguration(confDir));
 		} catch (WrongArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JDOMException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Host h = null;
 		try {
 			h = client.getCi().getReplicateHost(p, p.getRid());
 		} catch (WrongArgumentException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			throw new IOException("Reading Replicate Host Error");
 		}
 		OptimusDataProtocol dp = h.getDataProtocol();
 		try{
 		if(!dp.putPartitionData(p, itr).get())
 		{
 			throw new IOException("Ouput Result to TRANS FAILURE");
 		}}catch(Exception e)
 		{
 			throw new IOException("Ouput Result to TRANS FAILURE");
 		}
 		
 		
 		c.increment(itr.getData().length *8 + 16);
 		super.cleanup(context);
 	}
 	
 }
