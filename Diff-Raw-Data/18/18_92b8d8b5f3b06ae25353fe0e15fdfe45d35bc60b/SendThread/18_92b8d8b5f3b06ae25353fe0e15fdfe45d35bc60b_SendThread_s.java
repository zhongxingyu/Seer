 package mpi;
 
 import p2pmpi.mpi.MPI;
 import air.SimWorld;
 import air.Simulator;
 
 public class SendThread extends Thread {
 	/**
 	 * The last lookahead value that was used
 	 */
 	long lookAhead = 0;
 	
 	long defaultLookAhead = MpiEvent.getDefaultLookAhead();
 	
 	Simulator simulator;
 	
 	public SendThread(Simulator simulator) {
 		this.simulator = simulator;
 	}
 	
 	public void run() {
 		while(true) {
 			MpiMessage data = SimWorld.getInstance().outgoingMessages.poll();
 			if (data != null) {
 				this.sendMessage(data);
 			} else {
 				long rt = System.currentTimeMillis();
 				long elapsedTime = (rt - this.simulator.getRtStartTime());
 				if (elapsedTime >= this.lookAhead) {
					//this.sendNullMessage(elapsedTime, this.defaultLookAhead, -1);
 				}
 			}
 		}
 	}
 	
 	private void sendMessage(MpiMessage data) {
 		int dest = SimWorld.getInstance().getRankFromString(data.getToAirport());
 		
 		MpiMessage[] dataBuf = new MpiMessage[1];
 		dataBuf[0] = data;
 		MPI.COMM_WORLD.Send(dataBuf, 0, 1, MPI.OBJECT, dest, 1);
 		
 		this.sendNullMessage(data.getTimeStamp(), data.getLookAhead(), dest);
 	}
 	
 	/**
 	 * Send null message to all processes except the excludeRank
 	 * @param timeStamp
 	 * @param lookAhead
 	 * @param excludeRank
 	 */
 	private void sendNullMessage(long timeStamp, Long lookAhead, int excludeRank) {
 		int LPcount = MPI.COMM_WORLD.SizeTotal();
 		int selfRank = MPI.COMM_WORLD.Rank();
 		
 		for (int lp = 0; lp < LPcount; lp++) {
 			if (lp != excludeRank && lp != selfRank) {
 				MpiMessage[] dataBuf = new MpiMessage[1];
 				dataBuf[0] = new MpiMessage(timeStamp, lookAhead);
				MPI.COMM_WORLD.Send(dataBuf, 0, 1, MPI.OBJECT, lp, 1);
 			}
 		}
 		
		this.lookAhead = lookAhead;
 	}
 }
