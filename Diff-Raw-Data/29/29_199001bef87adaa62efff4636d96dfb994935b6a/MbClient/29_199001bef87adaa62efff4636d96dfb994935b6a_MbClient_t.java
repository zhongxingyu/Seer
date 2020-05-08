 package org.jcoderz.mb;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jcoderz.commons.util.IoUtil;
 import org.jcoderz.commons.util.JaxbUtil;
 import org.jcoderz.commons.util.StringUtil;
 import org.jcoderz.mb.type.ArtistCredit;
 import org.jcoderz.mb.type.Includes;
 import org.jcoderz.mb.type.Medium;
 import org.jcoderz.mb.type.Medium.TrackList.Track;
 import org.jcoderz.mb.type.Metadata;
 import org.jcoderz.mb.type.NameCredit;
 import org.jcoderz.mb.type.Recording;
 import org.jcoderz.mb.type.RecordingList;
 import org.jcoderz.mb.type.Release;
 import org.jcoderz.mb.type.ResourceType;
 import org.jcoderz.mb.type.TrackData;
 import org.xml.sax.InputSource;
 
 /**
  * Basic class for accessing the musicbrainz server.
  * 
  * Currently the user of this interface is responsible not to sent more than one request
  * per second towards the official musicbrainz server. See http://musicbrainz.org/doc/XML_Web_Service/Version_2
  * 
  * @author amandel
  *
  */
 public final class MbClient
 {
     private static final Logger LOGGER = Logger.getLogger(MbClient.class.getName()); 
     public static final String SERVER_URL 
         = System.getProperty(
             "org.jcoderz.mb.musicbrainzServerUrl",
             "http://www.musicbrainz.org");
     public static final String WS_BASE 
         = "/ws/2";
     public static final String VARIOUS_ARTIST_ID = "89ad4ac3-39f7-470e-963a-56509c546377";
 
     // used in error in the ngs release
     public static final String VARIOUS_ARTIST_ID_2 = "11cbacaf-d336-4de8-ac41-87ea24a10fe9";
     
     
     
     private final HttpClient mClient;
     private final String mMbServerUrl;
 
     /** If set, all query results are recorded in the given directory. */
     private File mRecordDir;
     
     public MbClient ()
     {
         mClient = new DefaultHttpClient();
         mClient.getParams().setParameter("User-Agent", "mb-ngs-java/0.9; www.jcoderz.org");
         mMbServerUrl = SERVER_URL;
     }
     
     public MbClient (String serverUrl)
     {
         mClient = new DefaultHttpClient();
         mMbServerUrl = serverUrl;
     }
     
     public Release getRelease(String id, Set<Includes> inc)
     {
         return get(ResourceType.RELEASE, id, inc).getRelease();
     }
 
     public Release getRelease(String id)
     {
         final Set<Includes> includes = new HashSet<Includes>();
         includes.add(Includes.RECORDINGS);
         includes.add(Includes.ARTISTS);
         includes.add(Includes.ARTIST_CREDITS);
         includes.add(Includes.RELEASE_GROUPS);
         includes.add(Includes.MEDIA);
         // release-rels ? -> ASIN, IMAGES??? 
         
         return get(ResourceType.RELEASE, id, includes).getRelease();
     }
     
     public RecordingList getRecordingsByPuid(String puid, Set<Includes> inc)
     {
         RecordingList result;
         final Metadata metadata = get(ResourceType.PUID, puid, inc);
         if (metadata.getPuid() != null && metadata.getPuid().getRecordingList() != null)
         {
             result = metadata.getPuid().getRecordingList();
         }
         else
         {
             result = new RecordingList();
         }
         return result;
     }
 
     public RecordingList getRecordingsByPuid(String puid)
     {
         final Set<Includes> includes = new HashSet<Includes>();
         includes.add(Includes.RELEASES);
         includes.add(Includes.ARTISTS);
         includes.add(Includes.RELEASE_GROUPS);
         return get(ResourceType.PUID, puid, includes).getPuid().getRecordingList();
     }
 
     public Release getReleaseFull(String id)
     {
         final Set<Includes> includes = new HashSet<Includes>();
         includes.add(Includes.ARTISTS);
         includes.add(Includes.LABELS);
         includes.add(Includes.RECORDINGS);
         includes.add(Includes.RELEASE_GROUPS);
         includes.add(Includes.DISCIDS);
 //        - discids           include discids for all media in the releases
         includes.add(Includes.MEDIA);
 //        - media             include media for all releases, this includes the # of tracks on each medium.
         includes.add(Includes.PUIDS);
 //        - puids             include puids for all recordings
         includes.add(Includes.ISRCS);
 //        - isrcs             include isrcs for all recordings
         includes.add(Includes.ARTIST_CREDITS);
 //        - artist-credits    include artists credits for all releases and recordings
 //        includes.add(Includes.VARIOUS_ARTISTS);
 //        - various-artists   include only those releases where the artist appears on one of the tracks, 
         includes.add(Includes.ALIASES);
 //        - aliases                   include artist, label or work aliases
         includes.add(Includes.TAGS);
         includes.add(Includes.RATINGS);
 //        - tags, ratings             include tags and/or ratings for the entity (not valid on releases)
 //        includes.add(Includes.USER_TAGS);
 //        includes.add(Includes.USER_RATINGS);
 //        - user-tags, user-ratings        
         return get(ResourceType.RELEASE, id, includes).getRelease();
     }
     
     public Metadata get(ResourceType type, String id, Set<Includes> includes) 
     {
         final String inc = buildInc(includes);
         final String path 
             = WS_BASE + "/" + type + "/" + id + (inc == null ? "" : "?inc=" + inc);
         LOGGER.log(Level.FINE, ">>>REQ>>> {0}", path);
 
         final String responseBody;
         if (mMbServerUrl.startsWith("file"))
         {
             responseBody = readFile(path);
         }
         else
         {
             responseBody = readHttp(path);
         }
         
         writeResponse(path, responseBody);
         
         
         LOGGER.log(Level.FINE, "<<<RSP<<< {0}", responseBody);
         return parse(path, responseBody);
     }
 
     private void writeResponse (final String path, final String responseBody)
     {
         if (getRecordDir() != null)
         {
             FileOutputStream os = null;
             OutputStreamWriter osw = null;
             try
             {
                 final File out 
                     = new File(getRecordDir(), 
                         path.replace('/', '#').replace('?', '#') + ".xml");
                 if (!out.exists())
                 {
                     os = new FileOutputStream(out);
                     osw = new OutputStreamWriter(os, "UTF-8");
                     osw.write(responseBody);
                 }
             }
             catch (IOException ex)
             {
                 LOGGER.log(Level.INFO, "Could not write result got exception " + ex, ex);
             }
             finally
             {
                 IoUtil.close(osw);
                 IoUtil.close(os);
             }
                 
         }
     }
 
     /**
      * Get the data from the file system.
      * @param path the request query path.
      * @return the read xml document.
      */
     private String readFile (String path)
     {
         String result = "";
         FileInputStream in = null; 
         InputStreamReader isr = null;
         try
         {
             in = new FileInputStream(
                 mMbServerUrl.replaceFirst("file:///", "") + "/"
                 + path.replace('/', '#').replace('?', '#') + ".xml");
             isr = new InputStreamReader(in, "UTF-8");
             result = IoUtil.readFully(isr);
         }
         catch (IOException ex)
         {
             LOGGER.log(Level.INFO, "Could not read result got exception " + ex, ex);
             result = "<metadata xmlns='http://musicbrainz.org/ns/mmd-2.0#'/>";
         }
         finally
         {
             IoUtil.close(isr);
             IoUtil.close(in);
         }
         return result;
     }
 
     /**
      * Get the data from the mb http server.
      * @param path the request query path.
      * @return the read xml document.
      */
     private String readHttp (final String path)
     {
         String responseBody = "";
         final HttpGet get 
             = new HttpGet(mMbServerUrl + path );
         final ResponseHandler<String> responseHandler 
             = new BasicResponseHandler();
         
         try
         {
             responseBody = mClient.execute(get, responseHandler);
         }
         catch (HttpResponseException e)
         {
             if (e.getStatusCode() == 404)
             {
                 responseBody = "<metadata xmlns='http://musicbrainz.org/ns/mmd-2.0#'/>"; // not found
             }
             else
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 throw new RuntimeException("Missing Error Handling! Requesting " + path, e);
             }
         }
         catch (ClientProtocolException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new RuntimeException("Missing Error Handling!", e);
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             throw new RuntimeException("Missing Error Handling!", e);
         }
         return responseBody;
     }
     
     private String buildInc (Set<Includes> includes)
     {
         String result = null;
         if (includes != null && !includes.isEmpty()) 
         {
             final StringBuilder sb = new StringBuilder();
             final SortedSet<String> sorted = new TreeSet<String>();
             for (Includes inc : includes)
             {
                 sorted.add(inc.toString());
             }
             for (String inc : sorted)
             {
                 if (sb.length() != 0) 
                 {
                     sb.append('+');
                 }
                 sb.append(inc);
             }
             result = sb.toString();
         }
         return result;
     }
 
     /**
      * Pick the medium from the current album that contains the given song.
      */
     public Medium getMedium (String currentAlbum, String fileId)
     {
         final Set<Includes> includes = new HashSet<Includes>();
         includes.add(Includes.RECORDINGS);
         final Release album = getRelease(currentAlbum, includes);
         Medium result = null;
 out:
         for (Medium m : album.getMediumList().getMedium())
         {
             for (Track t : m.getTrackList().getDefTrack())
             {
                 if (fileId.equals(t.getRecording().getId()))
                 {
                     result = m;
                     break out;
                 }
             }
         }
         return result;
     }
 
     public Recording getRecording (String id)
     {
         final Set<Includes> includes = new HashSet<Includes>();
         includes.add(Includes.ARTISTS);
         includes.add(Includes.ARTIST_CREDITS);
         includes.add(Includes.RELEASES);
         includes.add(Includes.MEDIA);
         return getRecording(id, includes);
     }
 
     public Recording getRecording (String id, Set<Includes> includes)
     {
         return get(ResourceType.RECORDING, id, includes).getRecording();
     }
     
     public Recording getRecording (String id, Includes...includes )
     {
         final Set<Includes> inc = new HashSet<Includes>();
         inc.addAll(Arrays.asList(includes));
         return get(ResourceType.RECORDING, id, inc).getRecording();
     }
     
     public TrackData getTrackData (String currentAlbum, String fileId)
     {
         final Release album = getRelease(currentAlbum);
         Medium medium = null;
         Track track = null;
 out:
         for (Medium m : album.getMediumList().getMedium())
         {
             for (Track t : m.getTrackList().getDefTrack())
             {
                 if (fileId.equals(t.getRecording().getId()))
                 {
                     medium = m;
                     track = t;
                     break out;
                 }
             }
         }
         return new TrackData(album,  medium, track);
     }
 
     public TrackData getTrackData (Release album, String fileId)
     {
         Medium medium = null;
         Track track = null;
         
         // Check this...
         final Release theAlbum;
         if (album.getMediumList() == null || album.getMediumList().getMedium() == null)
         {
             theAlbum = getRelease(album.getId());
         }
         else
         {
             theAlbum = album;
         }
 out:
         for (Medium m : theAlbum.getMediumList().getMedium())
         {
             for (Track t : m.getTrackList().getDefTrack())
             {
                 if (fileId.equals(t.getRecording().getId()))
                 {
                     medium = m;
                     track = t;
                     break out;
                 }
             }
         }
         return new TrackData(theAlbum,  medium, track);
     }
     
     public Metadata parse(String uri, String data)
     {
         final InputSource in = new InputSource();
         in.setSystemId(uri);
         in.setCharacterStream(new StringReader(data));
 
         final Metadata parsedData;
         try
         {
             JAXBContext ctx = JaxbUtil.getJaxbContext("org.jcoderz.mb.type");
             final Unmarshaller unmarsh = ctx.createUnmarshaller();
             parsedData = (Metadata) unmarsh.unmarshal(in);
         }
         catch (JAXBException e)
         {
             e.printStackTrace();
             throw new RuntimeException("Missing Error Handling! " + data, e);
         }
         return parsedData;
     }
 
     public static boolean containsVa(ArtistCredit ac)
     {
         boolean result = false;
         if (ac != null)
         {
             for (NameCredit nc : ac.getNameCredit())
             {
                 if (VARIOUS_ARTIST_ID.equals(nc.getArtist().getId()))
                 {
                     result = true;
                     break;
                 }
             }
         }
         return result;
     }
     
     public static int getReleaseYear (Release rel)
     {
         final String stringVal 
             = rel.getDate() == null ? null : rel.getDate().getValue().toString();
         int result = 9999;
         if (!StringUtil.isEmptyOrNull(stringVal))
         {
             result = Integer.parseInt(stringVal.substring(0, 4));
         }
         return result;
     }
 
     public static String getArtist (ArtistCredit ac)
     {
         final StringBuilder result = new StringBuilder();
         for (NameCredit nc : ac.getNameCredit())
         {
             result.append(nc.getArtist().getName());
             if (!StringUtil.isNullOrEmpty(nc.getJoinphrase()))
             {
                 result.append(nc.getJoinphrase());
             }
         }
         return result.toString();
     }
 
     public static String getArtistSortName (ArtistCredit ac)
     {
         final StringBuilder result = new StringBuilder();
         for (NameCredit nc : ac.getNameCredit())
         {
             result.append(nc.getArtist().getSortName());
             if (!StringUtil.isNullOrEmpty(nc.getJoinphrase()))
             {
                 result.append(nc.getJoinphrase());
             }
         }
         return result.toString();
     }
 
     /**
      * Return the directory where all requests are stored. If null the
      * requests are are not stored locally.
      * 
      * @return the directory where all requests are stored.
      */
     public File getRecordDir ()
     {
         return mRecordDir;
     }
 
     /**
      * Set the directory where to store raw request results use
      * <code>null</code> to switch of recording.
      * 
      * @param recordDir the directory where to store data.
      */
     public void setRecordDir (File recordDir)
     {
         mRecordDir = recordDir;
     }
 }
