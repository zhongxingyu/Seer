 package sta.andswtch.extensionLead;
 
 import java.util.List;
 
 import sta.andswtch.R;
 import android.util.Log;
 
 public class ResponseProcessor {
 
 	private static final String TAG = ResponseProcessor.class.getName();
 	
 
 	private ExtensionLead extLead;
 	private List<PowerPoint> powerPoints;
 	private final int OFFSET = 5; //at position 6 starts the powerpoint information  
 
 	public ResponseProcessor(List<PowerPoint> powerPoints, ExtensionLead extLead) {
 		this.powerPoints = powerPoints;
 		this.extLead = extLead;
 	}
 
 	public void processResponse(String response) {
 		// NET-PwrCtrl:(Name):(I.P):(M.A.S.K):(G.a.t.e.w.a.y):(M.A.C):
 		// (Name der Steckdose Nr. 1 , Schaltzustand {1=on;0=off}): ...
 		// :Seg_Dis:PORT(0x0D)(0x0A)
 		// z.B.:
 		// NET-PwrCtrl:NET-CONTROL:192.168.178.21:255.255.255.0:192.168.178.1:0.4.163.10.10.73:
 		// Nr. 1,0:Nr. 2,0:Nr. 3,0:Nr.4,0:Nr. 5,0:Nr. 6,0:Nr. 7,0:Nr.
 		// 8,0:248:80\r\n
 
 		String[] reParts = response.split(":");
 
 		Log.d(TAG, "ResponseProcessor received: " + response);
 		Log.d(TAG, "this response consist of " + reParts.length + " parts.");
 
 		if (reParts.length == 16) {
 			// Extension Lead Name
 			this.extLead.setName(reParts[1]);
 			// the powerpoints are at the positions 7 - 14
 			// -> array starts with 0
 			// -> reParts[6] - reParts[13]
 			for (int i = 1; i <= this.powerPoints.size(); i++) {
 				Log.d(TAG, "PowerPoint " + String.valueOf(i)
 						+ " has the value " + reParts[i + OFFSET]);
 				// 0 - off - false
 				// 1 - on - true
 				String wholeName = new String(reParts[i + OFFSET]);
 				if (wholeName.endsWith("0")) {
 					this.setState(i, false);
 				} else if(wholeName.endsWith("1")) {
 					this.setState(i, true);
 				} else {
 					Log.w(TAG, "error while parsing respose: powerpoint wrong");
 					this.extLead.errorOccured(R.string.errorWrongPowerpoint);
 				}
 				// set powerpoint name
 				String ppName = wholeName.substring(0, wholeName.length() - 2);
 				this.setName(i, ppName);
 			}
 
 			Integer seg_dis = Integer.parseInt(reParts[14]);		
 			
 			String binaryString = Integer.toBinaryString(seg_dis);
			
			StringBuffer fillZero = new StringBuffer();
			//fill with 0 from right
			if(binaryString.length() < powerPoints.size()){
				for (int i = 0; i < this.powerPoints.size() - binaryString.length(); i++) {
					fillZero.append("0");
				}
				fillZero.append(binaryString);
				binaryString = fillZero.toString();
			}
 
 			Log.d(TAG, "Segment Disabled Value is " + seg_dis + " represents "
 					+ binaryString);
 
 			for (int i = 1; i <= this.powerPoints.size(); i++) {
 				// 0 - enabled - true
 				// 1 - disabled - false
 				if (binaryString.subSequence(8 - i, 9 - i).equals("0")) {
 					this.setEnabled(i, true);
 				} else {
 					this.setEnabled(i, false);
 				}
 			}
 
 		} else {
 			if (reParts[reParts.length - 1].trim().equalsIgnoreCase("Err")) {
 				this.extLead.errorOccured(R.string.errorUserPassword);
 				Log.e(TAG, "Error occured within the response");
 			} else {
 				this.extLead.errorOccured(R.string.errorPowerpointNames);
 				Log.e(TAG, "To many parts received, please check the names of the power points");
 			}
 		}
 
 	}
 
 	private void setState(int id, boolean on) {
 		this.powerPoints.get(id - 1).setState(on);
 	}
 	
 	private void setName(int id, String name) {
 		this.powerPoints.get(id - 1).setName(name);
 	}
 	
 	private void setEnabled(int id, boolean enabled) {
 		this.powerPoints.get(id - 1).setEnable(enabled);
 	}
 
 }
