 package house.neko.media.slave;
 
 import house.neko.media.common.Media;
 import house.neko.media.common.MediaLocation;
 import house.neko.media.common.MimeType;
 import house.neko.media.common.MediaLibrary;
 import house.neko.media.common.ConfigurationManager;
 
 import java.io.File;
 import java.util.Vector;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JTabbedPane;
 
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Container;
 import java.awt.Component;
 
 import org.apache.commons.configuration.HierarchicalConfiguration;
 
 import org.apache.commons.logging.Log;
 
 public class SlaveCDImporter
 {
 	private MediaLibrary library;
 	private Log log;
 	private HierarchicalConfiguration config;
 	private MimeType flacMimeType;
 	
 	public SlaveCDImporter(MediaLibrary library)
 	{
 		this.library = library;
 		this.log = ConfigurationManager.getLog(getClass());
 		flacMimeType = new MimeType();
 		flacMimeType.setFileExtension("flac");
 		flacMimeType.setMimeType("audio/flac");
 	}
 
 	public void importCD()
 	{
 		if(log.isDebugEnabled())
 		{	log.debug("Starting CD import"); }
 		try
 		{
 			if(log.isDebugEnabled()) { log.debug("getting CDDB info"); }
			Process cddbExec = Runtime.getRuntime().exec("cd-info --no-cddb-cache");
 			java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(cddbExec.getInputStream()));
 			READLOOP:
 			{
 				String year = "";
 				String discArtist = "";
 				String discTitle = "";
 				String trackCount = "";
 				String trackNumber = "";
 				String trackLength = "";
 				String frameOffset = "";
 				String trackArtist = "";
 				String trackTitle = "";
 				Vector<Vector<Media>> discs = new Vector<Vector<Media>>();
 				String line = r.readLine();
 				while(line != null)
 				{
 					line = line.trim();
 					if(line.startsWith("Year:"))
 					{
 						year = line.substring(6);
 						if(log.isTraceEnabled()) { log.trace("CD year: " + year); }
 					} else if(line.startsWith("Artist:")) {
 						discArtist = line.substring(8).replaceAll("^'(.*)'$", "$1");
 						if(log.isTraceEnabled()) { log.trace("Artist: " + discArtist); }
 					} else if(line.startsWith("Title:")) {
 						discTitle = line.substring(7).replaceAll("^'(.*)'$", "$1");
 						if(log.isTraceEnabled()) { log.trace("Disc title: " + discTitle); }
 					} else if(line.startsWith("Number of tracks:")) {
 						discs.add(new Vector<Media>());
 						trackCount = line.substring(18).replaceAll("^'(.*)'$", "$1");
 						if(log.isTraceEnabled()) { log.trace("Track count: " + trackCount); }
 					} else if(line.startsWith("number:")) {
 						trackNumber = line.substring(8);
 						if(log.isTraceEnabled()) { log.trace("Track number: " + trackNumber); }
 					} else if(line.startsWith("frame offset:")) {
 						frameOffset = line.substring(14);
 						if(log.isTraceEnabled()) { log.trace("Frame offset: " + frameOffset); }
 					} else if(line.startsWith("length:")) {
 						trackLength= line.substring(8).replaceAll("^(.*) seconds$", "$1");
 						if(log.isTraceEnabled()) { log.trace("length: " + trackLength); }
 					} else if(line.startsWith("artist:")) {
 						trackArtist= line.substring(8).replaceAll("^'(.*)'$", "$1");
 						if(log.isTraceEnabled()) { log.trace("Track artist: " + trackArtist); }
 					} else if(line.startsWith("title:")) {
 						trackTitle = line.substring(7).replaceAll("^'(.*)'$", "$1");
 						if(log.isTraceEnabled()) { log.trace("Track title: " + trackTitle); }
 					} else if(line.startsWith("Track ") && trackTitle.length() > 0) {
 						// start next track
 						Media m = new Media();
 						m.setArtist(trackArtist);
 						m.setName(trackTitle);
 						m.setAlbum(discTitle);
 						try { m.setTrackNumber(Integer.parseInt(trackNumber)); } catch(NumberFormatException nfe) { }
 						discs.lastElement().add(m);
 					}
 					line = r.readLine();
 				}
 				if(trackTitle.length() > 0)
 				{
 					Media m = new Media();
 					m.setArtist(trackArtist);
 					m.setName(trackTitle);
 					m.setAlbum(discTitle);
 					try { m.setTrackNumber(Integer.parseInt(trackNumber)); } catch(NumberFormatException nfe) { }
 					discs.lastElement().add(m);
 				}
 				if(log.isInfoEnabled()) { log.info("Found " + discs.size() + " possible matches"); }
				if(discs.size() > 0)
 				{	// pick input
 					pickDisc(discs);
 				}
 				if(discs.size() < 1)
 				{
 					// open input to type
 					getInput(discs);
 				}
 				while(discs.size() > 1)
 				{	discs.remove(discs.size()-1); }
 				if(discs.size() == 1 )
 				{	// auto load!
 					if(log.isInfoEnabled()) { log.info("auto-importing disc with " + discs.lastElement().size() + " tracks"); }
 					for(Media m : discs.lastElement().toArray(new Media[discs.lastElement().size()]))
 					{
 						if(log.isInfoEnabled()) { log.info("importing track " + m.getTrackNumber() + ": " + m); }
 						MediaLocation l = new MediaLocation();
 						l.setMimeType(flacMimeType);
 						m.setLocalLocation(l);
 						File encodedFile = library.getDefaultMediaFile(m);
 						l.setLocationURLString(encodedFile.toURI().toURL().toString());
 						if(encodedFile.exists())
 						{
 							if(log.isInfoEnabled()) { log.info("file already exists, skipping " + encodedFile.getAbsolutePath()); }
 							continue;
 						}
 						File tmpFile = File.createTempFile("tmpMediaFlow_", "_" + m.getTrackNumber() + ".wav");
 						if(log.isDebugEnabled()) { log.debug("ripping track to " + tmpFile.getAbsolutePath()); }
 						String[] cmdRip = { "cdparanoia", "-q", Integer.toString(m.getTrackNumber()), tmpFile.getAbsolutePath() };
 						Process execRip = Runtime.getRuntime().exec(cmdRip);
 						if(execRip.waitFor() == 0)
 						{	// encode
 							if(log.isDebugEnabled()) { log.debug("encoding track to " + encodedFile.getAbsolutePath()); }
 							if(!encodedFile.getParentFile().mkdirs())
 							{	if(log.isDebugEnabled()) { log.debug("unable to create directory  " + encodedFile.getParentFile().getAbsolutePath()); } }
 							String[] cmdEncode = 
 								{
 									"flac", 
 									"--totally-silent",
 									"-8",
 									"--tag=ALBUM=" + m.getAlbum(),
 									"--tag=TITLE=" + m.getName(),
 									"--tag=ARTIST=" + m.getArtist(),
 									"--tag=TRACKNUMBER=" + m.getTrackNumber(),
 									"-o", encodedFile.getAbsolutePath(),
 									tmpFile.getAbsolutePath() 
 								};
 							Process execEncode = Runtime.getRuntime().exec(cmdEncode);
 							if(execEncode.waitFor() == 0)
 							{	// add to library
 								l.setSize(encodedFile.length());
 								m.setID(Media.generateID());
 								if(log.isDebugEnabled()) { log.debug("adding track " + m.getTrackNumber() + ": " + m); }
 								library.add(m);
 							}
 						}
 						tmpFile.delete();
 					}
 				}
 			}
 			if(cddbExec.waitFor() != 0)
 			{	log.error("Unable to query CDDB"); }
 			
 		} catch(Exception e) {
 			log.error("unable to finish CD import", e);
 		}
 		if(log.isDebugEnabled())
 		{	log.debug("Done with CD import"); }
 	}
 	
 	private void pickDisc(Vector<Vector<Media>> discs)
 	{
 		try
 		{
 			JTabbedPane tp = new JTabbedPane();
			for(int k = 0; k < discs.size(); k++)
 			{
				Vector<Media> md = discs.elementAt(k);
 				JPanel p = new JPanel();
 				GridBagLayout gridbag = new GridBagLayout();
 				GridBagConstraints gc = new GridBagConstraints();
 				gc.weightx = 100;
 				gc.weighty = 100;
 				p.setLayout(gridbag);
 				gc.anchor = GridBagConstraints.NORTHWEST;
 				gc.ipadx = 7;
 				int y = 0;
 				JTextField[] title = new JTextField[md.size()];
 				JTextField[] artist = new JTextField[md.size()];
 				JTextField[] album = new JTextField[md.size()];
 				addLabel(p, new JLabel("Album"), gridbag, gc, 2, y);
 				addLabel(p, new JLabel("Artist"), gridbag, gc, 4, y);
 				addLabel(p, new JLabel("Title"), gridbag, gc, 6, y);
 				for(int i = 0; i < md.size(); i++)
 				{
 					title[i] = new JTextField(20);
 					artist[i] = new JTextField(20);
 					album[i] = new JTextField(20);
 					title[i].setText(md.elementAt(i).getName());
 					artist[i].setText(md.elementAt(i).getArtist());
 					album[i].setText(md.elementAt(i).getAlbum());
 					addLabel(p, new JLabel("Track " + (i+1)), gridbag, gc, 0, ++y);
 					addField(p, album[i], gridbag, gc, 2, y);
 					addField(p, artist[i], gridbag, gc, 4, y);
 					addField(p, title[i], gridbag, gc, 6, y);
 				}
 				tp.addTab(md.elementAt(0).getArtist() + " - " + md.elementAt(0).getAlbum(), p);
 			}
 			if(JOptionPane.showConfirmDialog(null, tp, "Pick Disc", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
 			{	// pop all off except choice
				log.info("Picked disc #" + tp.getSelectedIndex());
 				for(int selectedIndex = tp.getSelectedIndex(); selectedIndex > 0; selectedIndex--)
 				{	discs.remove(0); }
				while(discs.size() > 1)
 				{	discs.remove(1); }
 			} else {
 				// pop all off
				log.info("Canceled picking, clearing list");
 				discs.clear();
 			}
 		} catch(Exception e) {
 			log.error("unable to pick titles for CD import", e);
 		}
 	}
 	
 	private void getInput(Vector<Vector<Media>> discs)
 	{
 		try
 		{
 			String[] cmdEncode = { "cdparanoia", "-Q" };
 			Process execQueryCD = Runtime.getRuntime().exec(cmdEncode);
 			java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(execQueryCD.getErrorStream()));
 			int count = 0;
 			READLOOP:
 			{
 				Pattern p = Pattern.compile("^\\s*(\\d+)\\..*$");
 				String line = r.readLine();
 				while(line != null)
 				{
 					log.debug("!"+ line + "!");
 					Matcher m = p.matcher(line);
 					if(m.matches())
 					{	count = Integer.parseInt(m.group(1)); }
 					line = r.readLine();
 				}
 				if(log.isDebugEnabled())
 				{	log.debug("Found " + count + " tracks"); }
 				if(execQueryCD.waitFor() != 0)
 				{	log.error("Unable to query CD"); }
 			}
 			if(count == 0)
 			{	return; }
 			// ask for input now
 			{
 				JPanel p = new JPanel();
 				GridBagLayout gridbag = new GridBagLayout();
 				GridBagConstraints gc = new GridBagConstraints();
 				gc.weightx = 100;
 				gc.weighty = 100;
 				p.setLayout(gridbag);
 				gc.anchor = GridBagConstraints.NORTHWEST;
 				gc.ipadx = 7;
 				int y = 0;
 				JTextField[] title = new JTextField[count];
 				JTextField[] artist = new JTextField[count];
 				JTextField[] album = new JTextField[count];
 				addLabel(p, new JLabel("Album"), gridbag, gc, 2, y);
 				addLabel(p, new JLabel("Artist"), gridbag, gc, 4, y);
 				addLabel(p, new JLabel("Title"), gridbag, gc, 6, y);
 				for(int i = 0; i < count; i++)
 				{
 					title[i] = new JTextField(20);
 					artist[i] = new JTextField(20);
 					album[i] = new JTextField(20);
 					addLabel(p, new JLabel("Track " + (i+1)), gridbag, gc, 0, ++y);
 					addField(p, album[i], gridbag, gc, 2, y);
 					addField(p, artist[i], gridbag, gc, 4, y);
 					addField(p, title[i], gridbag, gc, 6, y);
 				}
 				discs.add(new Vector<Media>());
 				if(JOptionPane.showConfirmDialog(null, p, "Input CD", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
 				{
 					for(int i = 0; i < count; i++)
 					{
 						Media m = new Media();
 						m.setArtist(artist[i].getText());
 						m.setName(title[i].getText());
 						m.setAlbum(album[i].getText());
 						m.setTrackNumber(i+1);
 						discs.lastElement().add(m);
 					}
				} else {
					discs.clear();
 				}
 			}
 		} catch(Exception e) {
 			log.error("unable to get input for CD import", e);
 		}
 	}
 	
 	private void addComponent(Container p, Component c, GridBagLayout gridbag, GridBagConstraints gc, int x, int y)
 	{
 		gc.gridx = x;
 		gc.gridy = y;
 		gc.gridwidth = 1;
 		gc.gridheight = 1;
 		gridbag.setConstraints(c, gc);
 		p.add(c);
 	}
 	
 	private void addLabel(Container p, JLabel label, GridBagLayout gridbag, GridBagConstraints gc, int x, int y)
 	{
 		//gc.gridwidth = GridBagConstraints.RELATIVE;
 		addComponent(p, label, gridbag, gc, x, y);
 	}
 	
 	private void addField(Container p, JTextField field, GridBagLayout gridbag, GridBagConstraints gc, int x, int y)
 	{
 		//gc.gridwidth = GridBagConstraints.RELATIVE;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		addComponent(p, field, gridbag, gc, x, y);
 	}
 }
