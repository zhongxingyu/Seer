 /*
  * 	Mario Sangiorgio - mariosangiorgio AT gmail DOT com
  *
  *  This file is part of lyrics.
  * 
  *  lyrics is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  lyrics is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with lyrics.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package libraryExplorer;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 
 import utils.AlternateNames;
 import crawler.Crawler;
 import crawler.LyricsNotFoundException;
 import crawler.MetroLyricsCrawler;
 import crawler.SongLyricsCrawler;
 
 /**
  * Class that searches the library for audio files, reads their tags and finally
  * looks on the web to retrieve the lyrics
  * 
  * @author mariosangiorgio
  * 
  */
 public class LibraryExplorer {
 	private static Logger logger = Logger.getLogger("LibraryExplorer");
 	private String path;
 	private Vector<Crawler> crawlers = new Vector<Crawler>();
 	private boolean override = false;
 	private AlternateNames alternateNames = AlternateNames
 			.getAlternateNames("/resources/AlternateNames");
 
	private String[] badSentences = {
			"Unfortunately, we are not licensed to display the full lyrics for this song at the moment.",
			"Impossibile trovare testo: nel titolo del brano sono presenti caratteri non-ASCII." };

 	private Collection<OutputListener> outputListeners = new Vector<OutputListener>();
 
 	/**
 	 * Creates a new instance of the class specifying to lookup at the desired
 	 * directory
 	 * 
 	 * @param path
 	 *            the directory to explore
 	 */
 	public LibraryExplorer(String path) {
 		this.path = path;
 		crawlers.add(new MetroLyricsCrawler());
 		crawlers.add(new SongLyricsCrawler());
 	}
 
 	/**
 	 * Creates a new instance of the class, specifying the directory and the
 	 * proxy settings
 	 * 
 	 * @param path
 	 *            the directory to explore
 	 * @param proxyHostname
 	 *            the proxy hostname
 	 * @param proxyPort
 	 *            the proxy port
 	 */
 	public LibraryExplorer(String path, String proxyHostname, int proxyPort) {
 		this.path = path;
 		crawlers.add(new MetroLyricsCrawler(proxyHostname, proxyPort));
 		crawlers.add(new SongLyricsCrawler(proxyHostname, proxyPort));
 	}
 
 	/**
 	 * Creates a new instance of the class specifying to lookup at the desired
 	 * directory and if it has to search for lyrics or just to fix the library
 	 * 
 	 * @param path
 	 *            the directory to explore
 	 * @param override
 	 *            whether the application as to search for lyrics or fix the
 	 *            library
 	 */
 	public LibraryExplorer(String path, boolean override) {
 		this(path);
 		this.override = override;
 	}
 
 	/**
 	 * Creates a new instance of the class specifying to lookup at the desired
 	 * directory, the proxy settings and if it has to search for lyrics or just
 	 * to fix the library
 	 * 
 	 * @param path
 	 *            the directory to explore
 	 * @param proxyHostname
 	 *            the proxy hostname
 	 * @param proxyPort
 	 *            the proxy port
 	 * @param override
 	 *            whether the application as to search for lyrics or fix the
 	 *            library
 	 */
 	public LibraryExplorer(String path, String proxyHostname, int proxyPort,
 			boolean override) {
 		this(path, proxyHostname, proxyPort);
 		this.override = override;
 	}
 
 	/**
 	 * explores the directory specified in the constructor and performs the
 	 * desired operations
 	 */
 	public void explore() {
 		explore(path);
 	}
 
 	/**
 	 * explores the directory specified in the parameter and performs the
 	 * desired operations
 	 * 
 	 * @param path
 	 *            the path where the method has to search audio files
 	 */
 	public void explore(String path) {
 		File current = new File(path);
 		if (current.isFile()) {
 			if (path.toLowerCase().endsWith(".mp3")) {
 				try {
 					AudioFile audioFile = AudioFileIO.read(current);
 					Tag tag = audioFile.getTag();
 					if (tag == null) {
 						logger.warning("The file " + current
 								+ " doesn't have any tag");
 					}
 					String lyrics = tag.getFirst(FieldKey.LYRICS);
 
 					// This option is used when we want to override the lyrics
 					// in order to fix their format
 					if (override == true) {
 						tag.setField(FieldKey.LYRICS, lyrics);
 						audioFile.commit();
 					}
 
 					// We want to look for the lyrics in the web
 					else {
 						String artist = tag.getFirst(FieldKey.ARTIST);
 						String title = tag.getFirst(FieldKey.TITLE);
 						for (String artistName : alternateNames
 								.getAlternateNameList(artist)) {
 
							if (lyricsAlreadyInTheFile(lyrics)) {
 								break;
 							}
 
 							for (Crawler crawler : crawlers) {
 								logger.info("Searching lyrics for " + title
 										+ " by " + artistName + " with "
 										+ crawler.getClass());
 								try {
 									lyrics = crawler.getLyrics(artistName,
 											title);
 									tag.setField(FieldKey.LYRICS, lyrics);
 									audioFile.commit();
 									notifySuccess(artist, title);
 									break;
 								} catch (LyricsNotFoundException ex) {
 								}
 							}
 						}
						if (!lyricsAlreadyInTheFile(lyrics)) {
 							notifyFailure(artist, title);
 						}
 					}
 				} catch (Exception e) {
 					logger.severe("Error getting the lyrics for " + current);
 				}
 			} else {
 				logger.warning("Unrecognized file type: " + current);
 			}
 		} else {
 			for (String f : current.list()) {
 				if (!f.startsWith(".")) {
 					explore(current.getAbsolutePath() + "/" + f);
 				}
 			}
 		}
 	}
 
	private boolean lyricsAlreadyInTheFile(String lyrics) {
		if (lyrics.equals(""))
			return false;
		for (String sentence : badSentences) {
			if (lyrics.contains(sentence)) {
				return false;
			}
		}
		return true;
	}

 	private void notifySuccess(String artist, String title) {
 		for (OutputListener listener : outputListeners) {
 			listener.displaySuccessfulOperation("Lyrics found for " + title
 					+ " by " + artist);
 		}
 	}
 
 	private void notifyFailure(String artist, String title) {
 		for (OutputListener listener : outputListeners) {
 			listener.displayUnsuccessfulOperation("Lyrics not found for "
 					+ title + " by " + artist);
 		}
 	}
 
 	/**
 	 * Adds the specified object to the list of elements that has to be notified
 	 * about what is going on
 	 * 
 	 * @param outputListener
 	 *            the object to notify
 	 */
 	public void addOutputListener(OutputListener outputListener) {
 		outputListeners.add(outputListener);
 	}
 }
