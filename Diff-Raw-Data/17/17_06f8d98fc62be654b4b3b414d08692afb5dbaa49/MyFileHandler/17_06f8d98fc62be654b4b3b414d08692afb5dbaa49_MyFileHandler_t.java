 import com.google.gson.Gson;
 import com.popsenteret.jsonparser.Data;
 import com.popsenteret.jsonparser.Track;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 public class MyFileHandler {
 
     public static void main(String[] args) throws MalformedURLException, IOException {
         URL url = new URL("http://www.nrk.no/urort/api/getlist.ashx?listname=recommended&udid=popsenteret&apikey=GaRblmzRuKL30z&signature=4e51212b14ce2d9aedca04a5b4594161");
         // Read all the text returned by the server
         BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF8"));
         String json;
         String outfile = "urortJsonFile.txt";
         while ((json = in.readLine()) != null) {
             Data data = new Gson().fromJson(json, Data.class);
             List<Track> tracks = data.getResult().getTracks();
             for (Track t : tracks) {
                 saveTrack(t);
             }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
            out.write(json);
            out.close();
         }
 
         in.close();
     }
 
     public static void saveTrack(Track track) throws MalformedURLException, IOException {
         String artist = track.getArtistName().replace(" ", "_");
         artist = artist.replace("&", "and");
        artist = artist.replace(".", "");
        String title = track.getTitle().replace(" ", "_");
        title = title.replace("\\", "_");
        title = title.replace("/", "_");
         // saveUrl(track.getImageName(track.getThumbURL()), track.getThumbURL());
         // saveUrl(track.getImageName(track.getThumb2URL()), track.getThumb2URL());
         // saveUrl(track.getImageName(track.getImageURL()), track.getImageURL());
         saveUrl(("FraNRK" + File.separator + artist + "_" + track.getImageName(track.getImageXLURL())), track.getImageXLURL());
        saveUrl(("TilPopit" + File.separator + artist + "-" + title + "_" + track.getImageName(track.getURL())), track.getURL());
     }
 
     public static void saveUrl(String filename, String urlString) throws MalformedURLException, IOException {
         BufferedInputStream in = null;
         FileOutputStream fout = null;
         try {
             in = new BufferedInputStream(new URL(urlString).openStream());
             fout = new FileOutputStream(filename);
 
             byte data[] = new byte[1024];
             int count;
             while ((count = in.read(data, 0, 1024)) != -1) {
                 fout.write(data, 0, count);
             }
         } finally {
             if (in != null)
                 in.close();
             if (fout != null)
                 fout.close();
         }
     }
 }
