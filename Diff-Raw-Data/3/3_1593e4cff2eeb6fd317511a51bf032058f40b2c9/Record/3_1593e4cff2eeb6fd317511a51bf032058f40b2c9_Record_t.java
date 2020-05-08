 package uk.co.brotherlogic.mdb.record;
 
 /**
  * Class to represent a record
  * @author Simon Tucker
  */
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import uk.co.brotherlogic.mdb.User;
 import uk.co.brotherlogic.mdb.artist.Artist;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.groop.Groop;
 import uk.co.brotherlogic.mdb.groop.LineUp;
 import uk.co.brotherlogic.mdb.label.Label;
 
 /**
  * Core of the record class
  * 
  * @author simon
  * 
  */
 public class Record implements Comparable<Record>
 {
 
    /** The ratio of groups required to author a record */
    private static final double GROOP_RATIO = 0.8;
 
    /** Replacement string used to represent spaces when sanitizing */
    public static final String REPLACE = "~!@$%^&*()~";
 
    /**
 	 * 
 	 */
    private static final long serialVersionUID = -5625039435654063418L;
 
    public static void main(String[] args) throws Exception
    {
       Record r = GetRecords.create().getRecord(3373);
       System.out.println(r.getFileAdd());
    }
 
    /** The author of the record */
    private String author;
 
    /** The date the record was bought */
    private Calendar boughtDate;
 
    /** The category to which this record belongs */
    private Category category;
 
    /** Catalogue numbers */
    private Collection<String> catnos;
 
    /** The children of this record */
    private Collection<Record> children = null;
 
    /** Any compilers of this if compilation */
    private Collection<Artist> compilers;
 
    /** The number of the record on discogs */
    private int discogsNum = -1;
 
    /** The format of the record */
    private Format format;
 
    /** Labels which released the record */
    private Collection<Label> labels;
 
    /** Any notes applied to the record */
    private String notes;
 
    /** The record id number */
    private int number = -1;
 
    /** The id number of the owner of the record */
    private int owner;
 
    /** Parent of the record (for multi-item boxsets) */
    private Integer parent;
 
    /** Price paid for the record */
    private double price;
 
    /** The month the record was released */
    private int releaseMonth;
 
    /** Single or album release */
    private int releaseType;
 
    /** The place where the record has been ripped to */
    private String riploc;
 
    /** The location of the record on it's respective shelf */
    private int shelfpos;
 
    /** Price of sold record */
    private double soldPrice;
 
    /** The name of the record */
    private String title;
 
    /** The set of all tracks */
    private Collection<Track> tracks;
 
    /** Flag to indicate that the record needs to be updated */
    private boolean updated = false;
 
    /** THe year of release */
    private Integer year;
 
    /**
     * Empty Constructor
     */
    public Record()
    {
       title = "";
       notes = " ";
       year = -1;
       parent = -1;
 
       boughtDate = Calendar.getInstance();
       labels = new LinkedList<Label>();
       tracks = new LinkedList<Track>();
       catnos = new LinkedList<String>();
 
       price = 0.0;
    }
 
    public Record(String title, Format format, Calendar boughtDate, int shelfpos)
    {
       this();
       this.title = title;
       this.format = format;
       this.boughtDate = boughtDate;
       this.shelfpos = shelfpos;
    }
 
    public void addCatNo(String catNo)
    {
       catnos.add(catNo);
    }
 
    private String addD(String in)
    {
       if (in.endsWith("."))
          return in + "d";
       return in;
    }
 
    public void addLabel(Label label)
    {
       labels.add(label);
    }
 
    public void addPersonnel(int trackNumber, Collection<Artist> pers)
    {
       Track intTrack = getTrack(trackNumber);
       intTrack.addPersonnel(pers);
    }
 
    public void addScore(User user, int score) throws SQLException
    {
       RecordScore.add(this, user, score);
    }
 
    public void addTrack(Track trk)
    {
       updated = true;
       tracks.add(trk);
    }
 
    public void addTracks(int addPoint, int noToAdd)
    {
       // Work through the tracks
       Iterator<Track> tIt = tracks.iterator();
       Collection<LineUp> groops = new Vector<LineUp>();
       Collection<Artist> pers = new Vector<Artist>();
 
       while (tIt.hasNext())
       {
          // Get the current track
          Track currTrack = tIt.next();
 
          // If the track is beyond the addition point - move it along
          if (currTrack.getTrackNumber() > addPoint)
             // Update the trackNumber
             currTrack.setTrackNumber(currTrack.getTrackNumber() + noToAdd);
          else if (currTrack.getTrackNumber() == addPoint)
          {
             // Collect the information from the previous track
             groops = currTrack.getLineUps();
             pers = currTrack.getPersonnel();
             // currTrack.setTrackNumber(currTrack.getTrackNumber() +
             // noToAdd);
          }
       }
 
       // Now add the new tracks using the new information collected above
       for (int i = addPoint + 1; i < addPoint + noToAdd + 1; i++)
          tracks.add(new Track("", 0, groops, pers, i, -1, i, this.getNumber()));
    }
 
    @Override
    public int compareTo(Record o)
    {
       return (title.toLowerCase() + number).compareTo(o.getTitle().toLowerCase() + (o.getNumber()));
    }
 
    public void createTracks(int noTracks)
    {
       for (int i = 0; i < noTracks; i++)
       {
          Track t = new Track(i + 1);
          tracks.add(t);
       }
    }
 
    @Override
    public boolean equals(Object o)
    {
       if (o instanceof Record)
          return this.compareTo((Record) o) == 0;
       else
          return false;
    }
 
    public Collection<LineUp> getAllLineUps()
    {
       Collection<LineUp> allGroops = new Vector<LineUp>();
 
       Iterator<Track> tIt = tracks.iterator();
       while (tIt.hasNext())
          allGroops.addAll((tIt.next()).getLineUps());
 
       return allGroops;
    }
 
    public String getAuthor()
    {
       return author;
    }
 
    public Category getCategory()
    {
       return category;
    }
 
    public Collection<String> getCatNos() throws SQLException
    {
       if (catnos == null || catnos.size() == 0)
          catnos = GetRecords.create().getCatNos(number);
 
       return catnos;
    }
 
    public String getCatNoString() throws SQLException
    {
       StringBuffer ret = new StringBuffer("");
       for (String catNo : getCatNos())
          ret.append(catNo);
 
       return ret.toString();
    }
 
    public Collection<Record> getChildren() throws SQLException
    {
       if (children == null)
          // Get the children
          children = GetRecords.create().getChildren(this);
 
       return children;
    }
 
    public Collection<Artist> getCompilers() throws SQLException
    {
       if (compilers == null)
          compilers = GetRecords.create().getCompilers(this);
 
       return compilers;
    }
 
    public Calendar getDate()
    {
       return boughtDate;
    }
 
    public int getDiscogsNum()
    {
       return discogsNum;
    }
 
    public int getDiscogsURI()
    {
       return discogsNum;
    }
 
    public String getDisplayTitle()
    {
       return author + " - " + title;
    }
 
    public String getFileAdd() throws SQLException
    {
       try
       {
          return addD(sanitize(getAuthor())) + File.separator + sanitize(getRepTitle());
       }
       catch (UnsupportedEncodingException e)
       {
          e.printStackTrace();
       }
       return null;
    }
 
    public Format getFormat()
    {
       return format;
    }
 
    public String getFormTrackArtist(int formTrackNumber) throws SQLException
    {
       List<Track> trackRepTracks = new LinkedList<Track>();
       for (Track t : getTracks())
          if (t.getFormTrackNumber() == formTrackNumber)
             trackRepTracks.add(t);
 
       Set<Groop> grpset = new TreeSet<Groop>();
       grpset.addAll(trackRepTracks.get(0).getGroops());
       for (Track tck : trackRepTracks.subList(1, trackRepTracks.size()))
          grpset.addAll(tck.getGroops());
 
       List<Groop> grps = new LinkedList<Groop>(grpset);
       StringBuffer grpString = new StringBuffer(grps.get(0).getShowName());
       for (Groop grp : grps.subList(1, grps.size()))
          grpString.append(", " + grp.getShowName());
 
       return grpString.toString();
    }
 
    public String getFormTrackTitle(int formTrackNumber)
    {
       List<Track> trackRepTracks = new LinkedList<Track>();
       for (Track t : tracks)
          if (t.getFormTrackNumber() == formTrackNumber)
             trackRepTracks.add(t);
 
       if (trackRepTracks.size() == 1)
          return trackRepTracks.get(0).getTitle();
 
       StringBuffer title = new StringBuffer(trackRepTracks.get(0).getTitle());
       for (Track tck : trackRepTracks.subList(1, trackRepTracks.size()))
          title.append(" / " + tck.getTitle());
 
       return title.toString();
    }
 
    public int getGenre()
    {
       return category.getMp3Number();
    }
 
    public String getGroopString()
    {
       // Construct the groop string
       Collection<String> main = getMainGroops();
       Iterator<String> gIt = main.iterator();
       StringBuffer groop = new StringBuffer("");
       while (gIt.hasNext())
          groop.append(gIt.next() + " & ");
 
       // Remove the trailing & or replace with various
       String grp = null;
       if (groop.length() > 0)
          grp = groop.substring(0, groop.length() - 3);
       else
          grp = "Various";
 
       return grp;
 
    }
 
    public Collection<Label> getLabels() throws SQLException
    {
       if (labels == null || labels.size() == 0)
          labels = GetRecords.create().getLabels(number);
 
       return labels;
    }
 
    public Collection<String> getMainGroops()
    {
       // A Map of groopName --> Count
       Map<String, Integer> mainGroopMap = new TreeMap<String, Integer>();
       Collection<String> mainGroops = new Vector<String>();
 
       Iterator<Track> tIt = tracks.iterator();
       while (tIt.hasNext())
       {
          // Increment the count for each groop
          Collection<LineUp> groops = (tIt.next()).getLineUps();
          Iterator<LineUp> gIt = groops.iterator();
          while (gIt.hasNext())
          {
             Groop grp = gIt.next().getGroop();
             String groopName = grp.getSortName();
 
             Integer intVal;
             if (mainGroopMap.containsKey(groopName))
             {
                intVal = mainGroopMap.get(groopName);
                intVal = intVal.intValue() + 1;
             }
             else
                intVal = 1;
 
             mainGroopMap.put(groopName, intVal);
          }
       }
 
       // Select only groops who appear on the right number of tracks
       for (Entry<String, Integer> ent : mainGroopMap.entrySet())
          if (((ent.getValue()).doubleValue() / tracks.size()) > GROOP_RATIO)
             mainGroops.add(ent.getKey());
 
       return mainGroops;
 
    }
 
    public String getNotes()
    {
       return notes;
    }
 
    public int getNumber()
    {
       return number;
    }
 
    public int getNumberOfFormatTracks() throws SQLException
    {
       int tNumber = -1;
       for (Track trck : getTracks())
          tNumber = Math.max(trck.getFormTrackNumber(), tNumber);
       return tNumber;
    }
 
    public int getOwner()
    {
       return owner;
    }
 
    public Integer getParent()
    {
       return parent;
    }
 
    public double getPrice()
    {
       return price;
    }
 
    /**
     * @return Returns the releaseMonth.
     */
    public int getReleaseMonth()
    {
       return releaseMonth;
    }
 
    /**
     * @return Returns the releaseType.
     */
    public int getReleaseType()
    {
       return releaseType;
    }
 
    public Integer getReleaseYear()
    {
       return year;
    }
 
    public String getRepTitle() throws SQLException
    {
       if (!getFormat().getBaseFormat().equals("CD"))
          return getTitle();
 
       // Check the database for other titles
       List<Record> recs = GetRecords.create().getRecords(this.getTitle());
       int count = 0;
       for (Record rec : recs)
          if (rec.getFormat().getBaseFormat().equals("CD")
                && rec.getAuthor().equals(this.getAuthor()))
             count++;
       if (count == 1)
          return getTitle();
       else
          return getTitle() + " " + getCatNoString();
    }
 
    public String getRiploc()
    {
       return riploc;
    }
 
    public double getScore() throws SQLException
    {
       return RecordScore.get(this);
    }
 
    public double getScore(User user) throws SQLException
    {
       return RecordScore.get(this, user);
    }
 
    public int getScoreCount(User user) throws SQLException
    {
       return RecordScore.getCount(this, user);
    }
 
    public Integer getShelfPos()
    {
       return shelfpos;
    }
 
    public double getSoldPrice()
    {
       return soldPrice;
    }
 
    public String getTitle()
    {
       return title;
    }
 
    public String getTitleWithCat() throws SQLException
    {
       return getTitle() + getCatNoString();
    }
 
    public Track getTrack(int trackNumber)
    {
       Track ret = new Track();
 
       // Search all the tracks
       boolean found = false;
       Iterator<Track> tIt = tracks.iterator();
       while (tIt.hasNext() && !found)
       {
          Track currTrack = tIt.next();
          if (currTrack.getTrackNumber() == trackNumber)
          {
             ret = currTrack;
             found = true;
          }
       }
       return ret;
    }
 
    public String getTrackRep(int formTrackNumber) throws SQLException
    {
       try
       {
          return numberize(formTrackNumber) + "~" + sanitize(getFormTrackArtist(formTrackNumber))
                + "~" + sanitize(getFormTrackTitle(formTrackNumber));
       }
       catch (UnsupportedEncodingException e)
       {
          e.printStackTrace();
       }
 
       return "";
    }
 
    public String getTrackRep(Track trck)
    {
       try
       {
          return numberize(trck.getFormTrackNumber()) + "~" + sanitize(trck.getTrackAuthor()) + "~"
                + sanitize(trck.getTitle());
       }
       catch (UnsupportedEncodingException e)
       {
          e.printStackTrace();
       }
 
       return null;
    }
 
    public Collection<Track> getTracks() throws SQLException
    {
 
       if (tracks == null || tracks.size() == 0)
          tracks = GetRecords.create().getTracks(number);
 
       return tracks;
    }
 
    public Collection<String> getTrackTitles()
    {
       Collection<String> retSet = new Vector<String>();
       Iterator<Track> tIt = tracks.iterator();
       while (tIt.hasNext())
          retSet.add((tIt.next()).getTitle());
 
       return retSet;
    }
 
    public Integer getYear()
    {
       return year;
    }
 
    @Override
    public int hashCode()
    {
       return number;
    }
 
    public String numberize(int number)
    {
       if (number >= 100)
          return "" + number;
       else if (number >= 10)
          return "0" + number;
       else
          return "00" + number;
    }
 
    public void reset()
    {
       updated = false;
    }
 
    private void resetShelfPos()
    {
       if (shelfpos > 0)
          shelfpos = 0;
    }
 
    private String sanitize(String str) throws UnsupportedEncodingException
    {
 
       // Commas are fine for us
       return URLEncoder.encode(str.replace(" ", REPLACE), "UTF-8")
             .replace(URLEncoder.encode(REPLACE, "UTF-8"), " ").replace("%2C", ",");
    }
 
    public void save() throws SQLException
    {
       if (number == -1)
          number = GetRecords.create().addRecord(this);
       else if (updated)
       {
          GetRecords.create().updateRecord(this);
          updated = false;
       }
    }
 
    /**
     * Fixes the author of the record
     * 
     * @param in
     *           The {@link String} to set the author to
     */
    public final void setAuthor(final String in)
    {
       author = in;
       updated = true;
    }
 
    public void setCategory(Category cat)
    {
       category = cat;
    }
 
    public void setCatNo(String cat)
    {
       // Remove and add
       catnos.clear();
       catnos.add(cat);
    }
 
    public void setCatNos(Collection<String> cats)
    {
       // Remove and add
       catnos.clear();
       catnos.addAll(cats);
    }
 
    public void setCompilers(Collection<Artist> compilers)
    {
       this.compilers = new LinkedList<Artist>(compilers);
    }
 
    public void setDate(Date dat)
    {
       boughtDate.setTime(dat);
       updated = true;
    }
 
    public void setDiscogsNum(int dNum)
    {
      updated = true;
       discogsNum = dNum;
    }
 
    public void setFormat(Format form)
    {
       // Reset the shelfpos
       resetShelfPos();
       format = form;
    }
 
    public void setGroops(int trackNumber, Collection<LineUp> lineups)
    {
       Track intTrack = getTrack(trackNumber);
       for (LineUp lineUp : lineups)
          intTrack.addLineUp(lineUp);
    }
 
    public void setLabel(Label lab)
    {
       labels.clear();
       labels.add(lab);
    }
 
    public void setLabels(Collection<Label> labs)
    {
       // Remove and add
       labels.clear();
       labels.addAll(labs);
    }
 
    public void setNotes(String in)
    {
       notes = in;
    }
 
    public void setNumber(int num)
    {
       number = num;
    }
 
    public void setOwner(int in)
    {
       owner = in;
    }
 
    public void setParent(Integer parent)
    {
       this.parent = parent;
      updated = true;
    }
 
    public void setPersonnel(int trackNumber, Collection<Artist> pers)
    {
       Track intTrack = getTrack(trackNumber);
       intTrack.addPersonnel(pers);
    }
 
    public void setPrice(double price)
    {
       this.price = price;
    }
 
    /**
     * @param releaseMonth
     *           The releaseMonth to set.
     */
    public void setReleaseMonth(int releaseMonth)
    {
       this.releaseMonth = releaseMonth;
    }
 
    /**
     * @param releaseType
     *           The releaseType to set.
     */
    public void setReleaseType(int releaseType)
    {
       this.releaseType = releaseType;
    }
 
    public void setRiploc(String riploc)
    {
       this.riploc = riploc;
       updated = true;
    }
 
    public void setSoldPrice(double soldPrice)
    {
       this.soldPrice = soldPrice;
    }
 
    public void setTitle(String tit)
    {
       title = tit;
    }
 
    public void setTrackLength(int trackNumber, int lengthInSeconds) throws SQLException
    {
       Track t = null;
       for (Track track : getTracks())
          if (track.getFormTrackNumber() == trackNumber)
             if (t == null)
                t = track;
             else
                return;
 
       t.setLengthInSeconds(lengthInSeconds);
       updated = true;
    }
 
    public void setTracks(Collection<Track> tracksIn)
    {
       tracks.clear();
       tracks.addAll(tracksIn);
    }
 
    public void setTracks(int maxNumber)
    {
       // Only include relevant tracks
       Collection<Track> newTracks = new LinkedList<Track>();
       Iterator<Track> trIt = tracks.iterator();
       while (trIt.hasNext())
       {
          Track currTrack = trIt.next();
          if (currTrack.getTrackNumber() <= maxNumber)
             newTracks.add(currTrack);
       }
 
       // Replace the tracks
       tracks = newTracks;
 
    }
 
    public void setYear(int in)
    {
       year = in;
    }
 
    @Override
    public String toString()
    {
       StringBuffer ret = new StringBuffer("TITLE: " + getTitle() + "\n");
       try
       {
          ret.append("LABEL: " + getLabels() + "\n");
          ret.append("FORMAT: " + getFormat() + "\n");
          ret.append("TYPE: " + getReleaseType() + "\n");
          ret.append("CATNO: " + getCatNos() + "\n");
          DateFormat df = DateFormat.getDateInstance();
          ret.append("DATE: " + df.format(getDate().getTime()) + "\n");
          ret.append("YEAR: " + getReleaseYear() + "\n");
          ret.append("MONTH: " + getReleaseMonth() + "\n");
          ret.append("CATEGORY: " + getCategory() + "\n");
          ret.append("NOTE: " + getNotes() + "\n");
          ret.append("OWNER: " + getOwner() + "\n");
          ret.append("COMPILER: " + getCompilers() + "\n");
          ret.append("PRICE: " + getPrice() + "\n");
          ret.append("AUTHOR: " + getAuthor() + "\n");
          for (Track tr : tracks)
          {
             ret.append("TRACK: " + tr.getTrackNumber() + "\n");
             ret.append("ARTIST: " + tr.getLineUps() + "\n");
             ret.append("TITLE: " + tr.getTitle() + "\n");
             ret.append("PERSONNEL: " + tr.getPersonnel() + "\n");
             ret.append("LENGTH: " + tr.getLengthInSeconds() + "\n");
          }
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
       return ret.toString();
    }
 }
