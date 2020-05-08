 package house.neko.media.common;
 
 import java.util.Observable;
 import java.util.Vector;
 
 import org.apache.commons.configuration.HierarchicalConfiguration;
 
 import org.apache.commons.logging.Log;
 
 /**
  *
  * @author andy
  */
 public class LibraryView extends Observable implements java.util.Observer
 {
 	private MediaLibrary library;
 	private LibrarySearchResult visibleMedia;
 	
 	private String[] columnHeaders = { };
 	private Class[] columnClasses = { };
 	
 	private HierarchicalConfiguration config;
 	
 	private Log log = null;
 	
 	/**
 	 *
 	 * @param r
 	 */
 	public LibraryView(MediaLibrary m, HierarchicalConfiguration config)
 	{
 		this.log = ConfigurationManager.getLog(getClass());
 		this.config = config;
 		this.library = m;
 		// setup default columns
 		String[] columns = this.config.getStringArray("Column");
 		if(columns == null || columns.length < 1)
 		{
 			columns = new String[2];
 			columns[0] = Media.ARTIST;
 			columns[1] = Media.NAME;
 		} else {
 			for(int i = 0; i < columns.length; i++)
 			{
 				if(Media.NAME.equalsIgnoreCase(columns[i]))
 				{
 					columns[i] = Media.NAME;
 				} else if(Media.ARTIST.equalsIgnoreCase(columns[i])) {
 					columns[i] = Media.ARTIST;
 				} else if(Media.ALBUM.equalsIgnoreCase(columns[i])) {
 					columns[i] = Media.ALBUM;
 				}
 				if(log.isTraceEnabled()) { log.trace("Adding column " + columns[i] + " to view"); }
 			}
 		}
 		setColumns(columns);
 		clearFilter();
 	}
 	
 	public String[] getColumnHeaders()
 	{	return columnHeaders; }
 	
 	public Class[] getColumnClasses()
 	{	return columnClasses; }
 	
 	public void setColumns(String[] names)
 	{
 		if(names == null || names.length < 1)
 		{	return; }
 		columnHeaders = new String[names.length];
 		columnClasses = new Class[names.length];
 		for(int i = 0; i < names.length; i++)
 		{
 			columnHeaders[i] = names[i];
 			columnClasses[i] = names[i].getClass();
 		}
 	}
 	
 	public LibrarySearchResult getVisibleMedia()
 	{
 		synchronized(this)
 		{
 			if(log.isTraceEnabled()) { log.trace("getting visible results" + this.visibleMedia); }
 		}
 		return this.visibleMedia;
 	}
 	
 	public void applySimpleFilter(String filter)
 	{
 		if(log.isDebugEnabled()) { log.debug("applySimpleFilter: " + filter); }
 		Media[] list = library.getAllMedia();
 		Vector<Media> found = new Vector<Media>(list.length);
 		for(Media m : list)
 		{
 			String s = m.getName();
 			if(s != null && s.toLowerCase().indexOf(filter) > -1)
 			{
 				found.add(m);
 				continue;
 			}
 			s = m.getArtist();
 			if(s != null && s.toLowerCase().indexOf(filter) > -1)
 			{
 				found.add(m);
 				continue;
 			}
 			s = m.getAlbum();
 			if(s != null && s.toLowerCase().indexOf(filter) > -1)
 			{
 				found.add(m);
 				continue;
 			}
 		}
 		synchronized(this)
 		{
 			this.visibleMedia = mapMediaToResult(found);
 		}
 		setChanged();
 		notifyObservers();
 	}
 	
 	private LibrarySearchResult mapMediaToResult(Vector<Media> found)
 	{
 		String[] cn = columnHeaders;
 		Class[] ct = columnClasses;
 		Object[][] o = new Object[found.size()][cn.length + 1];
 		for(int i = 0; i < o.length; i++)
 		{
 			Media m = found.elementAt(i);
 			for(int j = 0;j < cn.length; j++)
 			{
 				if(cn[j] == Media.NAME)
 				{
 					o[i][j] = m.getName();
 				} else if(cn[j] == Media.ARTIST) {
 					o[i][j] = m.getArtist();
 				} else if(cn[j] == Media.ALBUM) {
 					o[i][j] = m.getAlbum();
 				} else {
 					o[i][j] = "UNKNOWN";
 				}
 			}
 			o[i][cn.length] = m;
 		}
		return new LibrarySearchResult(cn, ct, o);
 	}
 	
 	public void clearFilter()
 	{
 		if(log.isDebugEnabled()) { log.debug("clearFilter"); }
 		Media[] list = library.getAllMedia();
 		Vector<Media> found = new Vector<Media>(list.length);
 		for(Media m : list)
 		{	found.add(m); }
 		synchronized(this)
 		{
 			this.visibleMedia = mapMediaToResult(found);
 		}
 		if(log.isDebugEnabled()) { log.debug("Clearing filter"); }
 		setChanged();
 		notifyObservers();
 	}
 	
 	public void update(java.util.Observable o, Object arg)
 	{
 		if(log.isTraceEnabled()) { log.trace("Updated : " + o + " -> " + arg); }
 		if(o.hasChanged())
 		{	if(log.isTraceEnabled()) { log.trace("noticed change to " + o); } }
 		setChanged();
 		notifyObservers(arg);
 	}
 }
