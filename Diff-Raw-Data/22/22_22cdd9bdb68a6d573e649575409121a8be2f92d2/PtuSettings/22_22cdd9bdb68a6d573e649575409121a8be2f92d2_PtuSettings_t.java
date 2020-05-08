 package ch.cern.atlas.apvs.client.settings;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import ch.cern.atlas.apvs.client.ui.CameraView;
 
 /**
  * 
  * @author duns
  * 
  */
 public class PtuSettings implements Serializable {
 
 	private static final long serialVersionUID = -5390424254145424045L;
	private static final String videoServer = "pcatlaswpss02";
 
 	private Map<String, Entry> entries = new HashMap<String, Entry>();
 
 	public static class Entry implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		Boolean enabled;
 		String dosimeterSerialNo;
 		String helmetUrl;
 		String handUrl;
 
 		public Entry() {
			// Serializable
		}

		public Entry(String ptuId) {
 			enabled = true;
 			dosimeterSerialNo = "";
			String id = Integer.toHexString(Integer.parseInt(ptuId.substring(ptuId.length()-1, ptuId.length())));
			helmetUrl = "http://"+videoServer+":8"+id+"90/worker"+id+".mjpg";
			handUrl = "http://"+videoServer+":8"+id+"91/worker"+id+".mjpg";
 		}
 
 		@Override
 		public String toString() {
 			return "PtuSetting: enabled=" + enabled + " dosimeterNo="
 					+ dosimeterSerialNo + " helmetUrl=" + helmetUrl
 					+ " handUrl=" + handUrl;
 		}
 
 	}
 
 	public PtuSettings() {
 	}
 
 	public HashMap<String, String> getDosimeterToPtuMap() {
 		HashMap<String, String> dosimeterToPtu = new HashMap<String, String>();
 
 		// takes the last proper value
 		for (Iterator<String> i = entries.keySet().iterator(); i.hasNext();) {
 			String ptuId = i.next();
 			String serialNo = entries.get(ptuId).dosimeterSerialNo;
 			if ((ptuId != null) && (serialNo != null)) {
 				dosimeterToPtu.put(serialNo, ptuId);
 			}
 		}
 
 		return dosimeterToPtu;
 	}
 
 	public Boolean isEnabled(String ptuId) {
 		Entry entry = entries.get(ptuId);
 		return entry != null ? entry.enabled : false;
 	}
 
 	public void setEnabled(String object, Boolean value) {
 		entries.get(object).enabled = value;
 	}
 
 	public String getDosimeterSerialNumber(String ptuId) {
 		Entry entry = entries.get(ptuId);
 		return entry != null ? entry.dosimeterSerialNo : "";
 	}
 
 	public void setDosimeterSerialNumber(String object, String value) {
 		entries.get(object).dosimeterSerialNo = value;
 	}
 
 	public String getCameraUrl(String ptuId, String type) {
 		Entry entry = entries.get(ptuId);
 		return entry != null ? type.equals(CameraView.HELMET) ? entry.helmetUrl
 				: entry.handUrl : "";
 	}
 
 	public void setCameraUrl(String ptuId, String type, String value) {
 		if (type.equals(CameraView.HELMET)) {
 			entries.get(ptuId).helmetUrl = value;
 		} else {
 			entries.get(ptuId).handUrl = value;
 		}
 	}
 
 	public List<String> getPtuIds() {
 		List<String> list = new ArrayList<String>();
 		list.addAll(entries.keySet());
 		return list;
 	}
 
 	public boolean add(String ptuId) {
 		if (!entries.containsKey(ptuId)) {
			entries.put(ptuId, new Entry(ptuId));
 			return true;
 		}
 		return false;
 	}
 
 	// Returns ptuId associated to dosimeterSerialNo
 	public String getPtuId(String dosimeterSerialNo) {
 		return getDosimeterToPtuMap().get(dosimeterSerialNo);
 	}
 }
