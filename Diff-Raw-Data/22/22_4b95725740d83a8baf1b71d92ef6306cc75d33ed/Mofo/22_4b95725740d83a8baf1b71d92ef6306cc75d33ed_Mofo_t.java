 package se.kth.ssvl.tslab.wsn.general.servlib.routing.prophet.queuing;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.bpf.exceptions.BPFDBException;
 
 public class Mofo extends ProphetQueuing {
 
 	private static final String TAG = "ForwardedTimes";
 
 	public int getLastBundle() {
 		ResultSet rs = null;		
 		int forwardColumn;
 		int fieldColumn;
 		int result = 0;
 		
 		try {
 			rs = BPF.getInstance()
 					.getBPFDB()
 					.query("bundles", null, null, null, null, null,
 							"forwarded_times Desc, id Desc", null);
 
 			forwardColumn = rs.findColumn("forwarded_times");
 			fieldColumn = rs.findColumn("id");
 			
 			if (!rs.first()) {
 				return -1;
 			}
 		
 			BPF.getInstance().getBPFLogger().info("Queuing",
 					"Deleting bundle ft: " + rs.getInt(forwardColumn));
 			result = rs.getInt(fieldColumn);
 			rs.close();
 
 		} catch (SQLException e) {
 			BPF.getInstance().getBPFLogger().error(TAG, "There was an SQL exception in getting the last bundle");
 		} catch (BPFDBException e) {
			BPF.getInstance().getBPFLogger().error(TAG, e.getMessage());
 		}
 
 		return result;
 	}
 }
