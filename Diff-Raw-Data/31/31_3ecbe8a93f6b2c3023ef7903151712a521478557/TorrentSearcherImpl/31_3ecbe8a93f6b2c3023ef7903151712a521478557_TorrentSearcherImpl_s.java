 package filmeUtils.torrentSites;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import filmeUtils.OutputListener;
 import filmeUtils.http.SimpleHttpClient;
 
 public class TorrentSearcherImpl implements TorrentSearcher {
 
 	private final List<TorrentSite> sites;
 
 	public TorrentSearcherImpl(final SimpleHttpClient httpclient) {
 		sites = new ArrayList<TorrentSite>();
 		sites.add(new PirateBaySe(httpclient));
 		sites.add(new Rarbg(httpclient));
 		sites.add(new BitSnoop(httpclient));
 	}
 	
	public String getMagnetLinkForTermOrNull(final String exactFileName,final OutputListener outputListener) throws SiteOfflineException{
 		for (final TorrentSite site : sites) {
 			outputListener.outVerbose("Procurando magnet link em "+site.getSiteName());
			final String magnetLinkFirstResultOrNull = site.getMagnetLinkFirstResultOrNull(exactFileName);
 			if(magnetLinkFirstResultOrNull != null){
 				return magnetLinkFirstResultOrNull;
 			}
 			outputListener.outVerbose("Nenhum magnet link em "+site.getSiteName());
 		}
 		return null;
 	}
 	
 }
