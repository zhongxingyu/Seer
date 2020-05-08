 package filmeUtils.torrentSites;
 
 import filmeUtils.OutputListener;
 
 public interface TorrentSearcher {
 
	public abstract String getMagnetLinkForTermOrNull(final String term,OutputListener outputListener);
 
 }
