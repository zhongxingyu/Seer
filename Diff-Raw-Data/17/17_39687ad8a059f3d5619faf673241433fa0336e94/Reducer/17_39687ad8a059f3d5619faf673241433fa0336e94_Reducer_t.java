 package scomm;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import mpi.MPI;
 import mpi.MPIException;
 
 
 public class Reducer {
 	
 	public int rank;
 	public int size;
 	
 	// size of model
 	int modelSize;
 	
 	// k-ary d-fly butterfly network
 	// int k;
 	// int d;
 	
 	// for heterogeneous k. ks and kneighbours
 	int[] kk;
 	int[][] kN;
 	int d;
 
 	
 	// partition boundary
 	int[][] parts;
 	int[] binpos;
 	
 	// configurations
 	private LinkedList<IVec> scheduleVertexSets;
 	private LinkedList<IVec> maps;
 	
 	// vertex set
 	private IVec scatterVertexSet;
 	private IVec gatherVertexSet;
 		
 	// internal buffer
 	private float[] internalBuffer;
 	
 	// benchmarks
 	private long commTime = 0;
 	private long bufferTime = 0;
 	private long bytes = 0;
 	
 	private long realCommTime = 0;
 	private long bufferCommTime = 0;
 	
 	private long packingTime = 0;
 	private long barrierTime = 0;
 	
 	private long configMergeTime = 0;
 	private long configPartTime = 0;
 	private long configCommTime = 0;
 	
 	public Reducer( String[] args ) throws MPIException{
 		
 		MPI.Init(args);
 		rank = MPI.COMM_WORLD.Rank();
 		size = MPI.COMM_WORLD.Size();
 		modelSize = Integer.parseInt(args[0]);
 		
 		//k-ary d-fly butterfly network
 		//k = Integer.parseInt(args[1]);
 		//d = Integer.parseInt(args[2]);
 		
 		// for heterogeneous k
 		d = args.length - 1;
 		
 		kk = new int[d];
 		
 		int maxk = 0;
 		for(int i=0; i<d; i++){
 			kk[i] = Integer.parseInt(args[1+i]);
 			if(kk[i]>maxk){ maxk = kk[i]; }
 		}
 		
 		// neighbours at different level
 		// partition boundaries at different level
 		kN = new int[d][maxk];
 		parts = new int[d][maxk+1];
 		binpos = new int[d];
 		int minV = 0;
 		int maxV = modelSize - 1;
 		int bin = -1;
 
 		int intv = size;
 		for(int l=0; l<d; l++){
 			
 			intv = intv/kk[l];
 			bin = rank/intv % kk[l];
 			binpos[l] = bin;
 			
 			for(int i=0; i<kk[l]; i++){
 				
 				int pos = (rank + intv*i) % (intv*kk[l]);
 				int init = rank/(intv*kk[l])*(intv*kk[l]);
 				kN[l][i] = init + pos;
 				
 				int N = maxV - minV + 1;
				parts[l][i] = minV + i*(N/kk[l]);
 			}
 			parts[l][kk[l]] = maxV+1;
 			
 			minV = parts[l][bin];
 			maxV = parts[l][bin+1] - 1;
 			
 		}
		
		for(int l=0; l<d; l++){
			for(int i=0; i<kk[l]; i++){
				System.out.println(String.format("rank: %d, l: %d, i: %d, part: %d", rank, l, i, parts[l][i]));
			}
		}
 	}
 	
 		
 	public void init(){
 		scheduleVertexSets = new LinkedList<IVec>();
 		maps = new LinkedList<IVec>();
 	}
 	
 
 	public int getScatterOriginIndex(int i, int level){
 		// offset level
 		int kl = 0;
 		for( int j=0; j<level; j++){kl += kk[j];}
 		int offset = 2 * kl;
 		// offset dest
 		offset += kk[level];
 		return offset + i;
 	}
 	
 	public int getScatterDestIndex(int i, int level){
 		// offset level
 		int kl = 0;
 		for( int j=0; j<level; j++){kl += kk[j];}
 		int offset = 2 * kl;
 		return offset + i;
 	}
 	
 	public int getGatherOriginIndex(int i, int level){
 		// offset scatter
 		int kd = 0;
 		for( int j=0; j<d; j++){kd += kk[j];}
 		int offset = 2 * kd;
 		// offset level
 		int kl = 0;
 		for( int j=0; j<level; j++){kl += kk[j];}
 		offset += 2 * kl;
 		return offset + (kk[level]-i)%kk[level];
 	}
 	
 	public int getGatherDestIndex(int i, int level){
 		// offset scatter
 		int kd = 0;
 		for( int j=0; j<d; j++){kd += kk[j];}
 		int offset = 2 * kd;
 		// offset level
 		int kl = 0;
 		for( int j=0; j<level; j++){kl += kk[j];}
 		offset += 2 * kl;
 		// offset origin
 		offset += kk[level];
 		return offset + (kk[level]-i)%kk[level];
 	}
 	
 	
 	
 	public void makeScatterSendBuffer( float[] sendBuffer, int[] sendCounts, int[] sendDispls, int level){
 		
 		int bufferPointer = 0;
 		long sTime = System.nanoTime();
 		
 		for (int i = 0; i<kk[level]; i++){
 			
 			int index = getScatterDestIndex(i,level);
 			int[] map = maps.get(index+1).data;
 			
 			sendDispls[i] = bufferPointer;
 			for(int j=0; j<map.length; j++){
 				sendBuffer[bufferPointer++] = internalBuffer[map[j]];
 			}
 			sendCounts[i] = map.length;
 			
 		}
 		
 		long eTime = System.nanoTime();
 		packingTime += eTime - sTime;
 		
 	}
 	
 	public void makeGatherSendBuffer( float[] sendBuffer, int[] sendCounts, int[] sendDispls, int level){
 		
 		int bufferPointer = 0;
 		long sTime = System.nanoTime();
 		
 		for (int i = 0; i<kk[level]; i++){
 			
 			int index = getGatherDestIndex(i,level);
 			int[] map = maps.get(index+1).data;
 			
 			sendDispls[i] = bufferPointer;
 			for(int j=0; j<map.length; j++){
 				sendBuffer[bufferPointer++] = internalBuffer[map[j]];
 			}
 			sendCounts[i] = map.length;
 			
 		}
 		
 		long eTime = System.nanoTime();
 		packingTime += eTime - sTime;
 		
 		
 	}
 	
 	public void makeScatterConfigSendBuffer( int[] sendBuffer, int[] sendCounts, int[] sendDispls, int level){
 		
 		// sorting according to host rank
 		// Arrays.sort(outboundIndices);
 		int[] outboundIndices = scatterVertexSet.data;
 		
 
 		long sTime = System.nanoTime();
 		for( int i = 0; i < outboundIndices.length; i++ ){
 			//int host = getHost(outboundIndices[i]);
 			//int dest = getScatterDest(host, level);
 			//System.out.println(String.format("rank: %d, host: %d, level: %d dest: %d", rank, host, level, dest));
 			int dest = -1;
 			for(int j = 0; j<kk[level]; j++){
 				if(outboundIndices[i]>=parts[level][j] && outboundIndices[i]<parts[level][j+1]){
 					dest = (j + kk[level] - binpos[level]) % kk[level];
 				}
 			}
 			if(dest >= 0){sendCounts[dest] += 1;}
			System.out.println(String.format("vid: %d, dest: %d", outboundIndices[i], dest));
 		}
 		
 		for( int i = 0; i <= kk[level]; i++){
 			 for(int t = 0; t < i; t++){
 				 sendDispls[i] += sendCounts[t];
 			 }
 		}
 		
 		int[] pointers = new int[kk[level]];
 		
 		for( int i = 0; i < outboundIndices.length; i++ ){
 			//int host = getHost(outboundIndices[i]);
 			//int dest = getScatterDest(host, level);
 			int dest = -1;
 			for(int j = 0; j<kk[level]; j++){
 				if(outboundIndices[i]>=parts[level][j] && outboundIndices[i]<parts[level][j+1]){
 					dest = (j + kk[level] - binpos[level]) % kk[level];
 				}
 			}
 			if(dest >=0 ){
 				sendBuffer[sendDispls[dest] + pointers[dest]] = outboundIndices[i]; 			   
 				pointers[dest] += 1;
 			}
 		}
 		long eTime = System.nanoTime();
 		configPartTime += eTime - sTime;
 		// add to scatter dest
 		for( int i = 0; i<kk[level]; i++){
 			int[] data = new int[sendCounts[i]]; 
 			for( int j = 0; j<sendCounts[i]; j++){
 				data[j] = sendBuffer[sendDispls[i] + j];
 			}
 			scheduleVertexSets.add(new IVec(data));
 		}
 		
 		
 		
 	}
 	
 	public void makeGatherConfigSendBuffer( int[] sendBuffer, int[] sendCounts, int[] sendDispls, int level){
 		
 		int[] inboundIndices = gatherVertexSet.data;
 		
 		// sorting according to host rank
 		//Arrays.sort(inboundIndices);
 		long sTime = System.nanoTime();
 		
 		for( int i = 0; i < inboundIndices.length; i++ ){
 			//int host = getHost(inboundIndices[i]);
 			//int dest = getGatherDest(host, level);
 			int dest = -1;
 			for(int j = 0; j<kk[level]; j++){
 				if(inboundIndices[i]>=parts[level][j] && inboundIndices[i]<parts[level][j+1]){
 					dest = (j + kk[level] - binpos[level]) % kk[level];
 				}
 			}
 			if(dest >= 0){sendCounts[dest] += 1;}
 		}
 		
 		for( int i = 0; i <= kk[level]; i++){
 			 for(int t = 0; t < i; t++){
 				 sendDispls[i] += sendCounts[t];
 			 }
 		}
 		
 		int[] pointers = new int[kk[level]];
 		
 		for( int i = 0; i < inboundIndices.length; i++ ){
 			//int host = getHost(inboundIndices[i]);
 			//int dest = getGatherDest(host, level);
 			int dest = -1;
 			for(int j = 0; j<kk[level]; j++){
 				if(inboundIndices[i]>=parts[level][j] && inboundIndices[i]<parts[level][j+1]){
 					dest = (j + kk[level] - binpos[level]) % kk[level];
 				}
 			}
 			if(dest >= 0){
 				sendBuffer[sendDispls[dest] + pointers[dest]] = inboundIndices[i]; 
 				pointers[dest] += 1;
 			}
 		}
 		
 		long eTime = System.nanoTime();
 		configPartTime += eTime - sTime;
 		// add to gather origin
 		for( int i = 0; i<kk[level]; i++){
 			int[] data = new int[sendCounts[i]]; 
 			for( int j = 0; j<sendCounts[i]; j++){
 				data[j] = sendBuffer[sendDispls[i] + j];
 			}
 			scheduleVertexSets.add(new IVec(data));
 		}
 	}
 	
 	public void config( int[] outboundIndices, int[] inboundIndices) throws MPIException{
 		
 		scatterVertexSet = new IVec(outboundIndices);
 		gatherVertexSet = new IVec(inboundIndices);
 		
 		int [] sendBuffer;
 		int [] sendCounts;
 		int [] sendDispls;
 		
 		//MPI.COMM_WORLD.Barrier();
 		//System.out.println("scatter config");
 		for( int l = 0; l < d; l++){
 			
 		//	if(rank == 10)System.out.println(String.format("rank: %d, l: %d", rank, l));
 			sendBuffer = new int[scatterVertexSet.size()];
 			sendCounts = new int[kk[l]];
 			sendDispls = new int[kk[l] + 1];
 			
 			makeScatterConfigSendBuffer( sendBuffer, sendCounts, sendDispls, l);
 		
 		//	if(rank == 10)System.out.println(String.format("cp 1 rank: %d, l: %d", rank, l));
 			scatterConfig( sendBuffer, sendCounts, sendDispls, l);
 			
 		//	if(rank == 10)System.out.println(String.format("cp 2 rank: %d, l: %d", rank, l));
 
 			
 		}
 		
 		//System.out.println("gather config");
 		for( int l = 0; l<d; l++){
 			//if(rank == 10)System.out.println(String.format("rank: %d, l: %d", rank, l));
 			sendBuffer = new int[gatherVertexSet.size()];
 			sendCounts = new int[kk[l]];
 			sendDispls = new int[kk[l] + 1];
 			
 			makeGatherConfigSendBuffer( sendBuffer, sendCounts, sendDispls, l);
 			//if(rank == 10){
 			//System.out.println(String.format("sendbuffer level:%d", l));
 			//for(int vid: sendBuffer){System.out.print(String.format("%d ", vid));}
 			//System.out.println(String.format("sendcount level:%d", l));
 			//for(int vid: sendCounts){System.out.print(String.format("%d ", vid));}
 			//System.out.println(String.format("senddispls level:%d", l));
 			//for(int vid: sendDispls){System.out.print(String.format("%d ", vid));}
 			//}
 			gatherConfig( sendBuffer, sendCounts, sendDispls, l);
 			
 
 		}
 		
 		long sTime = System.nanoTime();
 		scheduleVertexSets.add(new IVec(outboundIndices));
 		scheduleVertexSets.add(new IVec(inboundIndices));
 		IVec master = IVec.merge(scatterVertexSet, gatherVertexSet);
 		
 		maps.add(master);
 		for (IVec iv : scheduleVertexSets) {
 			maps.add(IVec.mapInds(iv, master));
 		}	
 		
 		internalBuffer = new float[maps.get(0).size()];
 		long eTime = System.nanoTime();
 		configMergeTime += eTime - sTime;
 			
 	}
 	
 	public void reduce( float[] outboundValues, float[] inboundValues) throws MPIException{
 		
 		// benchmark
 		commTime = 0;
 		bufferTime = 0;
 		bytes = 0;
 		realCommTime = 0;
 		bufferCommTime = 0;
 		packingTime = 0;
 		barrierTime = 0;
 		
 		//MPI.COMM_WORLD.Barrier();
 		// get map for outbound vertices
 		int kd = 0;
 		for( int j=0; j<d; j++){kd += kk[j];}
 		int[] omap = maps.get(1 + 4 * kd).data;
 		for(int j = 0; j<omap.length; j++){
 			internalBuffer[omap[j]] = outboundValues[j];
 		}
 		
 		// scatter
 		float [] sendBuffer;
 		int [] sendCounts;
 		int [] sendDispls;
 		
 		for( int l = 0; l<d; l++){
 			
 			int bufferSize = 0;
 			for(int i = 0; i < kk[l]; i++){
 				int index = getScatterDestIndex(i,l);
 				int[] vertices = scheduleVertexSets.get(index).data;
 				bufferSize += vertices.length;
 			}
 			
 			//if(rank == 0){System.out.println(String.format("scatter level %d: bufferSize: %d", l, bufferSize));}
 			
 			sendBuffer = new float[bufferSize];
 			sendCounts = new int[kk[l]];
 			sendDispls = new int[kk[l] + 1];
 			
 			long sTime = System.nanoTime();
 			makeScatterSendBuffer( sendBuffer, sendCounts, sendDispls, l);
 			long bTime = System.nanoTime();
 			//MPI.COMM_WORLD.Barrier();
 			long eTime = System.nanoTime();
 			barrierTime += eTime - bTime;
 			bufferTime += bTime - sTime;
 			scatter( sendBuffer, sendCounts, sendDispls, l);
 			sTime = System.nanoTime();
 			commTime += sTime - eTime;
 		}
 		
 		
 		// gather
 		for( int l = d-1; l>=0; l--){
 			
 			int bufferSize = 0;
 			for(int i = 0; i < kk[l]; i++){
 				int index = getGatherDestIndex(i,l);
 				int[] vertices = scheduleVertexSets.get(index).data;
 				bufferSize += vertices.length;
 			}
 			
 			//if(rank == 0){System.out.println(String.format("gather level %d: bufferSize: %d", l, bufferSize));}
 			
 			sendBuffer = new float[bufferSize];
 			sendCounts = new int[kk[l]];
 			sendDispls = new int[kk[l] + 1];
 			
 			long sTime = System.nanoTime();
 			makeGatherSendBuffer( sendBuffer, sendCounts, sendDispls, l);
 			long bTime = System.nanoTime();
 			//MPI.COMM_WORLD.Barrier();
 			long eTime = System.nanoTime();
 			barrierTime += eTime - bTime;
 			bufferTime += bTime - sTime;
 			gather( sendBuffer, sendCounts, sendDispls, l);
 			sTime = System.nanoTime();
 			commTime += sTime - eTime;
 		}
 		
 		// get map for inbound vertices
 		int[] imap = maps.get(1 + 4 * kd + 1).data;
 		for(int j = 0; j<imap.length; j++){
 			inboundValues[j] = internalBuffer[imap[j]];
 		}
 		
 	}
 	
 	
 	public void scatterConfig( int[] sendBuffer, int[] sendCounts, int[] sendDispls, int level) throws MPIException{
 		
 		int [] recvCounts = new int[kk[level]];
 		int [] recvBuffer;
 
 		for(int i = 0; i < kk[level]; i++){
 			
 			int right = kN[level][i];
 			int left = kN[level][(kk[level]-i)%kk[level]];
 			
 			
 			long sTime = System.nanoTime();
 		//	if(rank==10)System.out.println(String.format("right: %d, left: %d, i: %d level: %d", right, left, i, level));	
 			MPI.COMM_WORLD.Sendrecv(sendCounts, i, 1, MPI.INT, right, 0, recvCounts, i, 1, MPI.INT, left, 0);	
 			recvBuffer = new int[recvCounts[i]];
 		    //System.out.println(String.format("sc: %d, rc: %d, i: %d", sendCounts[i], recvCounts[i], i));	
 			MPI.COMM_WORLD.Sendrecv(sendBuffer, sendDispls[i], sendCounts[i], MPI.INT, right, 0, recvBuffer, 0, recvCounts[i], MPI.INT, left, 0);
 
 			long eTime = System.nanoTime();
 			configCommTime += eTime - sTime;
 			scheduleVertexSets.add(new IVec(recvBuffer)); 
 			scatterVertexSet = IVec.merge(scatterVertexSet, new IVec(recvBuffer));
 			sTime = System.nanoTime();
 			configMergeTime += sTime - eTime;
 		}
 	}
 	
 	public void gatherConfig( int[] sendBuffer, int[] sendCounts, int[] sendDispls, int level) throws MPIException{
 		
 		int [] recvCounts = new int[kk[level]];
 		int [] recvBuffer;		
 
 		for(int i = 0; i < kk[level]; i++){
 				
 			int right = kN[level][i];
 			int left = kN[level][(kk[level]-i)%kk[level]];
 			
 				
 			long sTime = System.nanoTime();
 			
 			MPI.COMM_WORLD.Sendrecv(sendCounts, i, 1, MPI.INT, right, 0, recvCounts, i, 1, MPI.INT, left, 0);
 			
 			recvBuffer = new int[recvCounts[i]];
 			//System.out.println(String.format("sc: %d, rc: %d, i: %d", sendCounts[i], recvCounts[i], i));	
 			MPI.COMM_WORLD.Sendrecv(sendBuffer, sendDispls[i], sendCounts[i], MPI.INT, right, 0, recvBuffer, 0, recvCounts[i], MPI.INT, left, 0);
 
 			long eTime = System.nanoTime();
 			configCommTime += eTime - sTime;
 			
 			scheduleVertexSets.add(new IVec(recvBuffer));
 			gatherVertexSet = IVec.merge(gatherVertexSet, new IVec(recvBuffer));
 			sTime = System.nanoTime();
 			configMergeTime += sTime - eTime;
 				
 			
 		}			
 	}
 	
 	public void scatter( float[] sendBuffer, int[] sendCounts, int[] sendDispls, int level) throws MPIException{
 		
 		float [] buffer;
 		
 		for(int i = 0; i < kk[level]; i++){
 			
 			int right =  kN[level][i];
 			int left = kN[level][(kk[level]-i)%kk[level]];
 			
 			int index = getScatterOriginIndex(i, level);
 			int[] map = maps.get(index+1).data;
 			
 			if( i !=0 ){
 				
 				int bufferCount = map.length;
 				buffer = new float[bufferCount];
 				//System.out.println(String.format("sc: %d, rc: %d, i: %d", sendCounts[i], bufferCount, i));	
 				long sTime = System.nanoTime();
 				MPI.COMM_WORLD.Sendrecv(sendBuffer, sendDispls[i], sendCounts[i], MPI.FLOAT, right, 0, buffer, 0, bufferCount, MPI.FLOAT, left, 0);
 				long eTime = System.nanoTime();
 				realCommTime += eTime - sTime;
				//System.out.println(String.format("scatter source: %d, dest: %d, level: %d, time: %f, size: %d", rank, right, level, (eTime-sTime)/1000000000f, sendCounts[i]));
 	
 				for(int j = 0; j<bufferCount; j++){
 					internalBuffer[map[j]] += buffer[j];
 				}
 				sTime = System.nanoTime();
 				bufferCommTime += sTime - eTime;
 				bytes += 4*(sendCounts[i] + bufferCount);
 				
 			}else{
 				
 			}
 		};
 	}
 	
 	public void gather( float[] sendBuffer, int[] sendCounts, int[] sendDispls, int level) throws MPIException{
 		
 		float [] recvBuffer;
 	
 		for(int i = 0; i < kk[level]; i++){
 				
 			int right =  kN[level][i];
 			int left = kN[level][(kk[level]-i)%kk[level]];
 						
 			int index = getGatherOriginIndex(i, level);
 			int[] map = maps.get(index+1).data;
 			
 			if(i != 0){
 				int bufferCount = map.length;
 				recvBuffer = new float[bufferCount];
 			
 				//System.out.println(String.format("sc: %d, rc: %d, i: %d", sendCounts[i], bufferCount, i));
 				long sTime = System.nanoTime();
 				MPI.COMM_WORLD.Sendrecv(sendBuffer, sendDispls[i], sendCounts[i], MPI.FLOAT, right, 0, recvBuffer, 0, bufferCount, MPI.FLOAT, left, 0);
 				long eTime = System.nanoTime();
 				realCommTime += eTime - sTime;
				//System.out.println(String.format("gather source: %d, dest: %d, level: %d, time: %f, size: %d", rank, right, level, (eTime-sTime)/1000000000f, sendCounts[i]));
 				//System.out.println(eTime-sTime);
 				
 				for( int j = 0; j<bufferCount; j++){
 					internalBuffer[map[j]] = recvBuffer[j];
 				}
 				
 				sTime = System.nanoTime();
 				bufferCommTime += sTime - eTime;
 				bytes += 4*(sendCounts[i] + bufferCount);
 			}else{
 				
 				long sTime = System.nanoTime();
 				for( int j = 0; j<sendCounts[i]; j++){
 					internalBuffer[map[j]] = sendBuffer[sendDispls[i]+j];
 				}
 				long eTime = System.nanoTime();
 				bufferCommTime += eTime - sTime;
 			}
 					
 		}
 	}
 	
 	public float getCommTime(){
 		return commTime/1000000000f;
 	}
 	
 	public float getBufferTime(){
 		return bufferTime/1000000000f;
 	}
 	public float getThroughput(){
 		return (float)bytes / realCommTime;
 	}
 	
 	public float getRealCommTime(){
 		return realCommTime/1000000000f;
 	}
 	
 	public float getBufferCommTime(){
 		return bufferCommTime/1000000000f;
 	}
 	
 	public float getPackingTime(){
 		return packingTime/1000000000f;
 	}
 	
 	public float getBarrierTime(){
 		return barrierTime/1000000000f;
 	}
 	
 	public float getConfigCommTime(){
 		return configCommTime/1000000000f;
 	}
 	
 	public float getConfigMergeTime(){
 		return configMergeTime/1000000000f;
 	}
 	
 	public float getConfigPartTime(){
 		return configPartTime/1000000000f;
 	}
 	public void terminate() throws MPIException{
 		MPI.Finalize();
 	}
 
 }
 
 
