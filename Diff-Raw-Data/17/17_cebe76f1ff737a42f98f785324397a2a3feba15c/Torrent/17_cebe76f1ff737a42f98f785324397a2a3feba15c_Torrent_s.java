 package torrent.linkStation.json;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gson.annotations.SerializedName;
 
 import common.interfaces.IFilterItem;
 
 public class Torrent implements IFilterItem
 {
 	@SerializedName("caption") 
 	private String Caption;
 	private String distributed_copies;
 	private int dl_rate;
	private int done;
 	private String hash;
 	private int Max_Connections;
 	private int Max_dl_rate;
 	private int Max_ul_rate;
 	private int Max_uploads;
 	private int Payload_download;
 	private int Payload_upload;
 	private int Peers_connected;
 	private int peers_total;
 	private String Private;
 	private int Seeds_connected;
 	private int Seeds_total;
	private int size;
 	private String State;
 	private int Stopped;
 	private int Ul_rate;
 	
 	public String getCaption()
 	{
 		return Caption;
 	}
 	public void setCaption(String caption)
 	{
 		Caption = caption;
 	}
 	public String getDistributed_copies()
 	{
 		return distributed_copies;
 	}
 	public void setDistributed_copies(String distributed_copies)
 	{
 		this.distributed_copies = distributed_copies;
 	}
 	public int getDl_rate()
 	{
 		return dl_rate;
 	}
 	public void setDl_rate(int dl_rate)
 	{
 		this.dl_rate = dl_rate;
 	}
	public int getDone()
 	{
 		return done;
 	}
	public void setDone(int done)
 	{
 		this.done = done;
 	}
 	public String getHash()
 	{
 		return hash;
 	}
 	public void setHash(String hash)
 	{
 		this.hash = hash;
 	}
 	public int getMax_Connections()
 	{
 		return Max_Connections;
 	}
 	public void setMax_Connections(int max_Connections)
 	{
 		Max_Connections = max_Connections;
 	}
 	public int getMax_dl_rate()
 	{
 		return Max_dl_rate;
 	}
 	public void setMax_dl_rate(int max_dl_rate)
 	{
 		Max_dl_rate = max_dl_rate;
 	}
 	public int getMax_ul_rate()
 	{
 		return Max_ul_rate;
 	}
 	public void setMax_ul_rate(int max_ul_rate)
 	{
 		Max_ul_rate = max_ul_rate;
 	}
 	public int getMax_uploads()
 	{
 		return Max_uploads;
 	}
 	public void setMax_uploads(int max_uploads)
 	{
 		Max_uploads = max_uploads;
 	}
 	public int getPayload_download()
 	{
 		return Payload_download;
 	}
 	public void setPayload_download(int payload_download)
 	{
 		Payload_download = payload_download;
 	}
 	public int getPayload_upload()
 	{
 		return Payload_upload;
 	}
 	public void setPayload_upload(int payload_upload)
 	{
 		Payload_upload = payload_upload;
 	}
 	public int getPeers_connected()
 	{
 		return Peers_connected;
 	}
 	public void setPeers_connected(int peers_connected)
 	{
 		Peers_connected = peers_connected;
 	}
 	public int getPeers_total()
 	{
 		return peers_total;
 	}
 	public void setPeers_total(int peers_total)
 	{
 		this.peers_total = peers_total;
 	}
 	public String getPrivate()
 	{
 		return Private;
 	}
 	public void setPrivate(String _private)
 	{
 		Private = _private;
 	}
 	public int getSeeds_connected()
 	{
 		return Seeds_connected;
 	}
 	public void setSeeds_connected(int seeds_connected)
 	{
 		Seeds_connected = seeds_connected;
 	}
 	public int getSeeds_total()
 	{
 		return Seeds_total;
 	}
 	public void setSeeds_total(int seeds_total)
 	{
 		Seeds_total = seeds_total;
 	}
	public int getSize()
 	{
 		return size;
 	}
	public void setSize(int size)
 	{
 		this.size = size;
 	}
 	public String getState()
 	{
 		return State;
 	}
 	public void setState(String state)
 	{
 		State = state;
 	}
 	public int getStopped()
 	{
 		return Stopped;
 	}
 	public void setStopped(int stopped)
 	{
 		Stopped = stopped;
 	}
 	public int getUl_rate()
 	{
 		return Ul_rate;
 	}
 	public void setUl_rate(int ul_rate)
 	{
 		Ul_rate = ul_rate;
 	}
 	@Override
 	public List<String> getFilterItems()
 	{
 		List<String> filterItems = new ArrayList<String>();
 		filterItems.add(this.Caption);
 		return filterItems;
 	}
 
 }
