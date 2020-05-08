 /**
  * Created: 02.03.2013
  */
 
 package de.freese.jripper.core.cddb;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.text.WordUtils;
 
 import de.freese.jripper.core.JRipperUtils;
 import de.freese.jripper.core.model.Album;
 import de.freese.jripper.core.model.DiskID;
 
 /**
  * CDDB Provider für FreeDB.
  * 
  * @author Thomas Freese
  */
 public class FreeDBProvider implements ICDDBProvider
 {
 	// /**
 	// *
 	// */
 	// private static final Pattern RECORD_DGENRE = Pattern.compile("DGENRE=(.*)");
 	//
 	// /**
 	// *
 	// */
 	// private static final Pattern RECORD_DTITLE = Pattern.compile("DTITLE=(.*)");
 	//
 	// /**
 	// *
 	// */
 	// private static final Pattern RECORD_DYEAR = Pattern.compile("DYEAR=(.*)");
 	//
 	// /**
 	// *
 	// */
 	// private static final Pattern RECORD_EXTD = Pattern.compile("EXTD=(.*)");
 	//
 	// /**
 	// *
 	// */
 	// private static final Pattern RECORD_TTITLE = Pattern.compile("TTITLE(\\d+)=(.*)");
 
 	/**
 	 * 
 	 */
 	private static final String SERVER = "freedb.freedb.org";
 
 	/**
 	 * 
 	 */
 	private final String DEFAULT_REQUEST;
 
 	/**
 	 * Erstellt ein neues {@link FreeDBProvider} Object.
 	 */
 	public FreeDBProvider()
 	{
 		super();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("&hello=").append(USER);
 		sb.append("+").append(HOST);
 		sb.append("+jRipper+1.0.0&proto=6");
 		this.DEFAULT_REQUEST = sb.toString();
 	}
 
 /**
 	 * Normalisiert die Texte.<br>
 	 * <ul>
 	 * <li>trim -> toLowerCase -> capitalize
 	 * <li>' Cd ' durch ' CD ' ersetzen
 	 * <li>Dj durch DJ ersetzen
 	 * <li>'Feat ' durch 'Feat. ' ersetzen
 	 * <li>':' durch ' - ' ersetzen
 	 * <li>'<' durch '-' ersetzen
 	 * <li>'>' durch '-' ersetzen
 	 * <li>Mehrfache Spaces durch einen ersetzen
 	 * <li>nicht erlaubte Zeichen: < > ? " : | \ / *
 	 * </ul>
 	 * 
 	 * @param splits String[]
 	 * @return String[]
 	 */
 	private String[] normalize(final String[] splits)
 	{
 		for (int i = 0; i < splits.length; i++)
 		{
 			splits[i] = StringUtils.trimToEmpty(splits[i]);
 			splits[i] = splits[i].toLowerCase();
 			splits[i] = WordUtils.capitalize(splits[i]);
 			splits[i] = splits[i].replaceAll(" Cd ", " CD ");
 			splits[i] = splits[i].replaceAll("Dj ", "DJ ");
 			splits[i] = splits[i].replaceAll("Feat ", "Feat. ");
 			splits[i] = splits[i].replaceAll(":", " - ");
 			splits[i] = splits[i].replaceAll("<", "-");
 			splits[i] = splits[i].replaceAll(">", "-");
 			splits[i] = StringUtils.normalizeSpace(splits[i]);
 
 			// Nach '(', '-' auch Grossbuchstaben.
 			if (splits[i].contains("(") || splits[i].contains("-"))
 			{
 				StringBuilder sb = new StringBuilder();
 
 				for (int j = 0; j < splits[i].length(); j++)
 				{
 					char sign = splits[i].charAt(j);
 
 					if ((j > 0) && ((splits[i].charAt(j - 1) == '(') || (splits[i].charAt(j - 1) == '-')))
 					{
 						sign = Character.toUpperCase(sign);
 					}
 
 					sb.append(sign);
 				}
 
 				splits[i] = sb.toString();
 			}
 		}
 
 		return splits;
 	}
 
 	/**
 	 * Beispiel Query:<br>
 	 * http://freedb.freedb.org/~cddb/cddb.cgi?cmd=cddb+query+7c0b8b0b+11+150+23115+
 	 * 42165+60015+79512+101560+118757+136605+159492+176067+198875+2957&hello=user+ hostname+program+version&proto=3(6)
 	 * 
 	 * @see de.freese.jripper.core.cddb.ICDDBProvider#query(de.freese.jripper.core.model.DiskID)
 	 */
 	@Override
 	public List<String> query(final DiskID diskID) throws Exception
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("/~cddb/cddb.cgi?cmd=cddb+query");
 
 		String[] splits = diskID.toString().split("[ ]");
 
 		for (String split : splits)
 		{
 			sb.append("+").append(split);
 		}
 
 		sb.append(this.DEFAULT_REQUEST);
 
 		URL url = new URL("http", SERVER, PORT, sb.toString());
 		URLConnection connection = url.openConnection();
 		Set<String> genres = new TreeSet<>();
 		CDDBResponse response = null;
 
 		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")))
 		{
 			String line = null;
 
 			while ((line = reader.readLine()) != null)
 			{
 				JRipperUtils.LOGGER.debug(line);
 
 				if (line.startsWith("."))
 				{
 					continue;
 				}
 
 				splits = line.split("[ ]");
 
 				if (splits.length < 2)
 				{
 					continue;
 				}
 
 				if (response == null)
 				{
 					if (splits[0].equals("200"))
 					{
 						// Nur ein Genre gefunden.
 						response = CDDBResponse.MATCH;
 					}
					else if (splits[0].equals("210"))
 					{
 						// Mehrere Genres gefunden
 						response = CDDBResponse.EXACT_MATCHES;
 					}
 					else if (splits[0].equals("211"))
 					{
						// Nicht exakte Treffer -> ungleiche DiskID.
 						response = CDDBResponse.INEXACT_MATCHES;
 					}
 
 					continue;
 				}
 
 				String genre = null;
 
 				switch (response)
 				{
 					case EXACT_MATCHES:
 					case INEXACT_MATCHES:
 						genre = StringUtils.trim(splits[0]);
 						break;
 					case MATCH:
 						genre = StringUtils.trim(splits[1]);
 						break;
 					default:
 						break;
 				}
 
 				genres.add(genre);
 
 				if (CDDBResponse.INEXACT_MATCHES.equals(response))
 				{
 					// Erstes Genre nehmen, DiskID aktualisieren und Abbruch.
 					diskID.setID(StringUtils.trim(splits[1]));
 					break;
 				}
 			}
 		}
 
 		return new ArrayList<>(genres);
 	}
 
 	/**
 	 * @see de.freese.jripper.core.cddb.ICDDBProvider#read(de.freese.jripper.core.model.DiskID, java.lang.String)
 	 */
 	@Override
 	public Album read(final DiskID diskID, final String genre) throws Exception
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("/~cddb/cddb.cgi?cmd=cddb+read");
 		sb.append("+").append(genre);
 
 		String[] splits = diskID.toString().split("[ ]");
 		sb.append("+").append(splits[0]);
 
 		sb.append(this.DEFAULT_REQUEST);
 
 		URL url = new URL("http", SERVER, PORT, sb.toString());
 		URLConnection connection = url.openConnection();
 
 		Album album = new Album();
 		album.setDiskID(diskID);
 
 		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")))
 		{
 			String line = null;
 
 			while ((line = reader.readLine()) != null)
 			{
 				JRipperUtils.LOGGER.debug(line);
 
 				if (line.startsWith("DTITLE"))
 				{
 					splits = line.split("[=]");
 					splits = splits[1].split("[/]");
 					splits = normalize(splits);
 
 					album.setArtist(splits[0]);
 					album.setTitle(splits[1]);
 				}
 				else if (line.startsWith("DYEAR"))
 				{
 					splits = line.split("[=]");
 					splits = normalize(splits);
 
 					album.setYear(Integer.parseInt(splits[1]));
 				}
 				else if (line.startsWith("DGENRE"))
 				{
 					// "Richtiges" Genre auslesen.
 					splits = line.split("[=]");
 					splits = normalize(splits);
 
 					album.setGenre(splits[1]);
 				}
 				else if (line.startsWith("TTITLE"))
 				{
 					splits = line.split("[=]");
 					// splits = normalize(splits);
 
 					String trackArtist = null;
 					String trackTitle = null;
 
 					if (splits[1].contains("/"))
 					{
 						// Annahme Compilation.
 						splits = splits[1].split("[/]");
 						splits = normalize(splits);
 
 						trackArtist = splits[0];
 						trackTitle = splits[1];
 					}
 					else
 					{
 						// Annahme Album.
 						splits = splits[1].split("[ ]");
 						splits = normalize(splits);
 
 						if (StringUtils.isNumeric(splits[0]))
 						{
 							splits[0] = "";
 						}
 
 						// splits[1].replaceAll("[\\d+]", "");
 						trackTitle = StringUtils.join(splits, " ");
 					}
 
 					album.addTrack(trackArtist, trackTitle);
 				}
 				else if (line.startsWith("EXTD"))
 				{
 					splits = line.split("[=]");
 
 					if (splits.length == 1)
 					{
 						// Kein Kommentar.
 						continue;
 					}
 
 					splits = normalize(splits);
 
 					album.setComment(splits[1]);
 				}
 			}
 		}
 
 		return album;
 	}
 }
