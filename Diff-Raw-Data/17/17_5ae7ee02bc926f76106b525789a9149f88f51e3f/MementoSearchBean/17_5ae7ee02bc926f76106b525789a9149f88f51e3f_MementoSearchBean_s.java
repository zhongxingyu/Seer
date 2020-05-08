 /**
  * 
  */
 package models.memento;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.joda.time.Instant;
 import org.joda.time.Period;
 import org.joda.time.PeriodType;
 import org.joda.time.format.PeriodFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dev.memento.Memento;
 import dev.memento.MementoClient;
 
 /**
  * @author Andrew Jackson <Andrew.Jackson@bl.uk>
  *
  */
 public class MementoSearchBean implements Serializable {
 
 	private static Logger logger = LoggerFactory.getLogger(MementoSearchBean.class);
 	
 	private static final long serialVersionUID = -8531283933701096508L;
 	
 	private MementoClient mc = new MementoClient();
 	
 	private String url = "http://";
 	
 	private String archive = "";	
 
 	private ArrayList<MementoBean> mementos;
 	
 	private HashMap<String,Integer> hostCount;
 
 	private ArrayList<String> hosts;
 
 	private int totalCount;
 
 	private String errorMessage = null;
 
 	//private MementoList mementoList;
 	
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getArchive() {
 		return archive;
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 * @param archive
 	 */
 	public void setUrlAndArchive(String url, String archive) {
 		this.archive = archive;
 		this.setUrl(url);
 	}
 
 
 	/**
 	 * @param url the url to set
 	 */
 	private void setUrl(String url) {
 		if( url != null && !"".equals(url) && !url.matches("^https?://.*$")) {
 			url = "http://"+url;
 		}
 		this.url = url;
 		this.doSearch();
 	}
 	
 	/**
 	 * @return
 	 */
 	public ArrayList<MementoBean> getMementos() {
 		return this.mementos;
 	}
 	
 	/**
 	 * @return
 	 */
 	public List<String> getHosts() {
 		return this.hosts;
 	}
 	
 	/**
 	 * @return
 	 */
 	public HashMap<String,Integer> getHostCounts() {
 		return this.hostCount;
 	}
 	
 	/**
 	 * @return
 	 */
 	public int getTotalCount() {
 		return this.totalCount;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getNumHosts() {
 		if( this.getHosts() == null ) return 0;
 		return this.getHosts().size();
 	}
 	
 	/**
 	 * @return
 	 */
 	public MementoBean getFirstMemento() {
 		if( this.mementos != null ) {
 			return this.mementos.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	public MementoBean getLastMemento() {
 		if( this.mementos != null ) {
 			return this.mementos.get( this.mementos.size() - 1 );
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
     public MementoBean getMidpointMemento() {
 		if( this.mementos != null ) {
 			return mementos.get( this.mementos.size()/2 );
 		}
 		return null;
     }
 
     public String getTimeSinceLastMemento() {
     	Date last = this.getLastMemento().getDateTime().getDate();
     	Period diff = new Period( new Instant(last), new Instant(), PeriodType.yearMonthDay() );
     	return PeriodFormat.getDefault().print(diff)+" ago";
     }
     
 	/**
 	 */
 	private void doSearch() {
 		// Set TimeGate:
		mc.setTimegateUri("http://www.webarchive.org.uk/wayback/memento/timegate/");
 		// Query:
     	mc.setTargetURI(this.getUrl());
     	// Get results:
     	this.mementos = new ArrayList<MementoBean>();
     	this.hostCount = new HashMap<String,Integer>();
     	this.totalCount = 0;
     	// Look for error:
     	if( mc.getErrorMessage() != null ) {
     		logger.error("Got Error: "+mc.getErrorMessage());
     		return;
     	}
     	// Get results:
     	if( mc.getMementos() != null ) {
     		//mc.getMementos().displayAll();
     		for( Memento m : mc.getMementos() ) {
     			logger.debug("Checking Memento:"+m.getUrl()+" "+m.getDateTimeString());
     			MementoBean mb = new MementoBean(m);
     			String host = mb.getArchiveHost();
     			// Strip out non-matching archives:
     			if( "".equals(this.archive) || this.archive.equals(host)) {
     				this.mementos.add( mb );
     				// Count archival copies:
     				if( host != null ) {
     					int count = hostCount.containsKey(host) ? hostCount.get(host) : 0;
     					hostCount.put(host, count + 1);
     				}
     				this.totalCount++;
     			}
     		}
     	}
     	this.hosts = new ArrayList<String>(hostCount.keySet());
     	// Add an error if there were no matches:
 		if( this.getMementos() == null || this.getMementos().size() == 0 ) {
 			this.setErrorMessage("No Mementos matched your query.");
 		}
     	
     }
 	
 	/**
 	 * @return the errorMessage
 	 */
 	public String getErrorMessage() {
 		if( this.errorMessage != null ) return this.errorMessage;
 		return mc.getErrorMessage();
 	}
 	
 	/**
 	 * @param errorMessage
 	 */
 	public void setErrorMessage(String errorMessage) {
 		this.errorMessage = errorMessage;
 	}
 
 }
