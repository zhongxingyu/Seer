 /**
  * 
  */
 package wifilocator.signature;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.graphics.PointF;
 import android.net.wifi.ScanResult;
 
 /**
  * 
  * @author Eric
  * @version 0
  */
 public class Signature {
 
 	private List<SignatureForm> sigList;
 	// The map collection is used to help us get the intersection between Signature
 	private Map<String,Integer> bssId_level;
 	private long timeStamp;
 	
 	private PointF coordinate;//not implemented yet, need read file
 	
 	
 	public Signature(List<ScanResult> wifiList, long timeStamp)
 	{
 		sigList=new ArrayList<SignatureForm>();
 		bssId_level=new HashMap<String,Integer>();
 		setSigList_s(wifiList);
 		setHashMap();
 		this.timeStamp=timeStamp;
 	}
 	
 	public Signature()
 	{
 		sigList=new ArrayList<SignatureForm>();
 		bssId_level=new HashMap<String,Integer>();
 		this.timeStamp=0;
 	}
 	
 	/**
 	 * Clone a Signature
 	 * @param s ,the signature need to be cloned
 	 * @author Eric Wang
 	 */
 	public void clone(Signature s)
 	{
 		this.setSigList(s.sigList);
 		this.setTimeStamp(s.timeStamp);
 		this.setHashMap();
 	}
 	/**
 	 * @return the sigList
 	 */
 	public List<SignatureForm> getSigList() {
 		return sigList;
 	}
 	/**
 	 * @param sigList the sigList to set
 	 */
 	public void setSigList(List<SignatureForm> sigList) {
 		this.sigList = sigList;
 	}
 	
 	/**
 	 * Set List<'SignatureForm'> by List<'ScanResult'> 
 	 * @param  a list of scan results of the latest wifi access point scan
 	 * @author Eric Wang
 	 */
 	public void setSigList_s(List<ScanResult> wifiList)
 	{
		//Because SigList is retrieved from the MemoryQueue, so we need to clear the orginal sigList.
		if(sigList.size()!=0)
			sigList.clear();
 		for(int i=0;i<wifiList.size();i++)
 		{
 			SignatureForm tuple=new SignatureForm(wifiList.get(i).SSID,wifiList.get(i).BSSID,wifiList.get(i).level,wifiList.get(i).frequency);
 			sigList.add(tuple);
 		}
 	}
 	
 	public void setHashMap()
 	{
 		for(int i=0;i<sigList.size();i++)
 		{
 			bssId_level.put(sigList.get(i).getBSSID(), sigList.get(i).getLevel());
 		}
 	}
 	
 	public Map<String,Integer> getHashMap()
 	{
 		return bssId_level;
 	}
 	
 	/**
 	 * @return the timeStamp
 	 */
 	public long getTimeStamp() {
 		return timeStamp;
 	}
 	/**
 	 * @param timeStamp the timeStamp to set
 	 */
 	public void setTimeStamp(long timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 	
 	/**
 	 * Convert a Signature to String.
 	 * @author Eric Wang
 	 */
 	public String toString()
 	{
 		StringBuilder str=new StringBuilder();
 		str.append(timeStamp).append(",");
 		for(int i=0;i<sigList.size();i++)
 		{
 			str.append(sigList.get(i).toString());
 		}
 		str.append("\r\n");
 		return str.toString();
 	}
 
 	/**
 	 * @return the coordinate
 	 */
 	public PointF getCoordinate() {
 		return coordinate;
 	}
 
 	/**
 	 * @param coordinate the coordinate to set
 	 */
 	public void setCoordinate(PointF coordinate) {
 		this.coordinate = coordinate;
 	}
 	
 }
