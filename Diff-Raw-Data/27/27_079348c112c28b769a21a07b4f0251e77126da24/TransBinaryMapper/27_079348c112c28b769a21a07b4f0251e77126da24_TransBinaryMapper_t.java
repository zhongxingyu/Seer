 package TRANS.MR.Binary.Mapper;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapred.Counters.Counter;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.jdom2.JDOMException;
 
 import TRANS.Array.ArrayID;
 import TRANS.Array.DataChunk;
 import TRANS.Array.OptimusShape;
 import TRANS.Array.OptimusZone;
 import TRANS.Array.PID;
 import TRANS.Array.Partition;
 import TRANS.Array.RID;
 import TRANS.Array.ZoneID;
 import TRANS.Client.ZoneClient;
 import TRANS.Client.Reader.PartitionReader;
 import TRANS.Exceptions.WrongArgumentException;
 import TRANS.MR.Binary.TransBinaryInputSplit;
 import TRANS.MR.Binary.TransBinaryMapInputValue;
 import TRANS.Protocol.OptimusCalculatorProtocol;
 import TRANS.Protocol.OptimusCatalogProtocol;
 import TRANS.Protocol.OptimusDataProtocol;
 import TRANS.util.Host;
 import TRANS.util.OptimusConfiguration;
 import TRANS.util.OptimusData;
 import TRANS.util.TRANSDataIterator;
 import TRANS.util.TransHostList;
 import TRANS.util.UTILS;
 
 public class TransBinaryMapper
 		extends
 		Mapper<Object, TransBinaryMapInputValue, IntWritable, TRANSDataIterator> {
 	public static enum InvalidCell {
 		INVALID_CELL_COUNT
 	};
 
 	Vector<Integer> fullIds = new Vector<Integer>();
 	DataChunk chunk = null;
 	private Counter cl = null;
 	private Counter cr = null;
 	private Counter co = null;
	private Object [] colocatedRead(TransBinaryInputSplit split, Host h, OptimusZone zone1, 
 			OptimusZone zone2,OptimusZone zone3)throws Exception
 	{
 		System.out.println("colocated Read");
 		OptimusCalculatorProtocol cp = h.getCalculateProtocol();
 		Partition p1 = new Partition(zone1.getId(),new ArrayID(split.getAid1()),
 				new PID(split.getPnum1()),new RID(0));
 		Partition p2 = new Partition(zone2.getId(),new ArrayID(split.getAid1()),
 				new PID(split.getPnum1()),new RID(0));
 		Partition p3 = new Partition(zone3.getId(),new ArrayID(split.getAid3()),
 				new PID(0),new RID(0));
 		int []start1 = split.getStart1();
 		int []start2 = split.getStart2();
 		int []cstart1 = split.getCstart();
 		int []rstart1 = new int[start1.length];
 		int []rstart2 = new int[start1.length];
 		for(int i =0; i < start1.length ; i++ )
 		{
 			rstart1[i] = start1[i] - cstart1[i];
 			rstart2[i] = start2[i] - cstart1[i];
 		}
 		return cp.JoinArray(p1, p2,new OptimusShape(split.getPs1()),
 				new OptimusShape(rstart1), new OptimusShape(split.getRoff()), split.getCal()).getData();
 	}
 	
 	/**
 	 * Reduces values for a given key
 	 * 
 	 * @param key
 	 *            the Key for the given value being passed in
 	 * @param value
 	 *            an Array to process that corresponds to the given key
 	 * @param context
 	 *            the Context object for the currently executing job
 	 */
 	public void map(Object key, TransBinaryMapInputValue value, Context context)
 			throws IOException, InterruptedException {
 		TransBinaryInputSplit split = value.getSplit();
 		ZoneClient zclient = null;
 		OptimusZone zone1 = null;
 		OptimusZone zone2 = null;
 		OptimusZone zone3 = null;
 		cl = (Counter) context.getCounter("TRANS_READ", "MAPPER_LOCAL_READ");
 		cr = (Counter) context.getCounter("TRANS_READ", "MAPPER_REMOTE_READ");
 		co = (Counter) context.getCounter("TRANS_WRITE", "MAPPER_WRITE");
 		
 		OptimusCatalogProtocol ci = null;
 		boolean earlybird = split.isEarlybird();
 		PartitionReader reader = null;
 		try {
 			OptimusConfiguration conf =new OptimusConfiguration(
 					split.getConfDir()); 
 			zclient = new ZoneClient(conf);
 			ci = zclient.getCi();
 			zone1 = ci.openZone(new ZoneID(split.getZid1()));
 			zone2 = ci.openZone(new ZoneID(split.getZid2()));
 			zone3 = ci.openZone(new ZoneID(split.getZid3()));
 			reader = new PartitionReader(conf);
 		} catch (WrongArgumentException e) {
 			throw new IOException("Create Client Failure");
 		} catch (JDOMException e) {
 			throw new IOException("Create Client Failure");
 		}
 		String [] host = split.getLocations();
 		Host h = UTILS.RandomHost(host,split.getHosts().getHosts());
		Object []data2 = null;
		Object [] data1 = null;
 		DataChunk chunk =  new DataChunk(zone3.getSize().getShape(), zone3.getPstep().getShape());
 		
 		
 		try {
 			if(split.getZid1() == split.getZid2() &&Arrays.equals(split.getStart1(), split.getStart2()))
 			{
 				
 				data1 = this.colocatedRead(split, h, zone1, zone2, zone3);
 				cl.increment(data1.length);
 			}else{
 				OptimusDataProtocol dp = h.getDataProtocol();
 				int []start1 = split.getStart1();
 				int []cstart1 = split.getCstart();
 				int []rstart1 = new int[start1.length];
 				for(int i =0; i < start1.length ; i++ )
 				{
 					rstart1[i] = start1[i] - cstart1[i];
 				}
 				data1 = (Double [])dp.readData(new ArrayID(split.getAid1()), new PID(split.getPnum1()),
 						new OptimusShape(split.getPs1()), new OptimusShape(rstart1), new OptimusShape(split.getRoff())).getData();
 				data2 = (Double [])reader.readData(zone2, split.getAid2(), split.getStart2(), split.getOff());
 				//data1 = reader.readData(zone1, split.getAid1(), split.getStart1(), split.getOff());
 				cl.increment(data1.length );
 				cr.increment(data2.length);
 				data1 = split.getCal().calcArray(data1, data2);
 			}
 			
 		} catch (WrongArgumentException e) {
 			e.printStackTrace();
 			throw new IOException("Reading from the second aray failure");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new IOException("Reading from the second aray failure");
 		}
 		
 
 		int []off = split.getRoff();
 		int []rstart = split.getRstart();
 		
 		TRANSDataIterator itr1 = new TRANSDataIterator(data1,rstart,split.getRoff());
 		ZoneID zid = zone3.getId();
 		ArrayID aid = new ArrayID(split.getAid3());
 		RID rid = new RID(zone3.getStrategy().getShapes().size() - 2);
 		Set<DataChunk> chunks = chunk.getAdjacentChunks(split.getRstart(), split.getOff());
 		
 		for(DataChunk c:chunks)
 		{
 			int [] cstart = c.getStart();
 			int [] coff = c.getChunkSize();
 			int []nstart = new int[cstart.length];
 			int []noff = new int [cstart.length];
 			int len = 1;
 			for( int i = 0 ; i < cstart.length ; i ++)
 			{
 				nstart[i] = cstart[i] > rstart[i] ? cstart[i]:rstart[i];
 				noff[i] = Math.min(cstart[i]+coff[i], rstart[i]+off[i]);
 				noff[i] -= nstart[i];
 				len *= noff[i];
 			}
 			
			Object []tmp = new Object[len];	
 			TRANSDataIterator itr = new TRANSDataIterator(tmp,nstart,noff);
 			
 			
 			if(!itr.init(itr1.getStart(), itr1.getShape()))
 			{
 				continue;
 			}
 			itr1.init(itr.getStart(), itr.getShape());
 			while(itr.next()&&itr1.next())
 			{
 				itr.set(itr1.get());
 			}
 			if(earlybird){
 				Partition p = new Partition(zid, aid, new PID(c.getChunkNum()),rid);
 				
 				Host ho = null;
 				try {
 					ho = ci.getReplicateHost(p, rid);
 				} catch (WrongArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					throw new IOException("Get Replicate host failure");
 				}
 				OptimusDataProtocol dp  = ho.getDataProtocol();
 				dp.putPartitionData(p, itr);
 				co.increment(itr.getSize());
 			}else{
 				context.write(new IntWritable(c.getChunkNum()), itr);
 			}
 		}
 	}
 
 }
