 package uk.ac.shef.dcs.oak.musicview;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * The model represents a set of events and associated metadata
  * 
  * @author sat
  * 
  */
 public class Model
 {
    /** Mapping from note names to their corresponding pitch number */
    private static Map<String, Double> noteMap = new TreeMap<String, Double>();
 
    /** The average length of a bar in seconds */
    private double avgBarLength;
 
    /** The length of a bar in seconds */
    private double barLength = 1;
 
    private final Map<Integer, Double> barStarts = new TreeMap<Integer, Double>();
 
    /** The bottom pitch */
    private double bottomPitch = 0.0;
 
    /** The collection of events that this model represents */
    private final List<Event> events = new LinkedList<Event>();
 
    /** Model listeners */
    private final Collection<ModelListener> listeners = new LinkedList<ModelListener>();
 
    /** The lower bound to zoom to */
    private double lowerBound = 1;
 
    /** The maximum bar number seen overall */
    private double maxBar = -1;
 
    /** The minimum bar number */
    private double minBar = Double.MAX_VALUE;
 
    /** The lowest recorded time */
    private double minTime = Double.MAX_VALUE;
 
    /** The selected subject */
    private Integer selectedSubject = -1;
 
    /** The selected trial */
    private Integer selectedTrial = -1;
 
    /** The list of all the subjects */
    private final Set<Integer> subjects = new TreeSet<Integer>();
 
    /** The list of all the trials */
    private final Set<Integer> trials = new TreeSet<Integer>();
 
    /** The upper bound to zoom to */
    private double upperBound = -1;
 
    /**
     * Adds a listener for models
     * 
     * @param listener
     *           The listener to add
     */
    public final void addListener(final ModelListener listener)
    {
       listeners.add(listener);
       listener.newModelLoaded(this);
    }
 
    /**
     * Forces an update on the listeners
     */
    public final void forceUpdate()
    {
       updateListeners();
    }
 
    /**
     * Gets all the available subjects for this file
     * 
     * @return A {@link Collection} of Integers representing the subjects
     */
    public final Collection<Integer> getAllSubjects()
    {
       return subjects;
    }
 
    /**
     * Gets all the available trials for this file and trial
     * 
     * @return A {@link Collection} of Integers representing the trials
     */
    public final Collection<Integer> getAllTrials()
    {
       return trials;
    }
 
    /**
     * Gets the average length of a bar
     * 
     * @return The average length of a bar as a double (in seconds)
     */
    public final double getAverageBarLength()
    {
       return avgBarLength;
    }
 
    /**
     * Gets the times in seconds of each bar
     * 
     * @return A Collection of times for each bar
     */
    public final Collection<Double> getBarTimes()
    {
       Collection<Double> barTimes = new LinkedList<Double>();
 
       if (events.size() > 0 && events.get(0).getTargetOnset() < 0)
          for (int i = (int) lowerBound; i <= upperBound; i++)
             barTimes.add(i * barLength);
       else
          for (int i = (int) lowerBound; i <= upperBound; i++)
             if (barStarts.containsKey(i))
                barTimes.add(barStarts.get(i));
             else
                barTimes.add(0.0);
       return barTimes;
 
    }
 
    /**
     * Gets the set of events that the model represents
     * 
     * @return {@link Collection} of events
     */
    public final Collection<Event> getEvents()
    {
       return events;
    }
 
    /**
     * Gets all the listeners to this model
     * 
     * @return A {@link Collection} of {@link ModelListener}s
     */
    public final Collection<ModelListener> getListeners()
    {
       return listeners;
    }
 
    /**
     * Gets the maximum bar number in the piece
     * 
     * @return The max bar number as a double
     */
    public final double getMaxBar()
    {
       return maxBar;
    }
 
    /**
     * Gets the maximum velocity of all the notes
     * 
     * @param voice
     *           the voice to get the velocity for
     * @return The maximum velocity as a double
     */
    public final double getMaxVelocity()
    {
       double maxVel = 0;
       for (Event ev : getEvents())
          maxVel = Math.max(ev.getVelocity(), maxVel);
       return maxVel;
    }
 
    /**
     * Gets the minimum velocity of all the notes
     * 
     * @param voice
     *           the voice to get the velocity for
     * @return The minimum velocity as a double
     */
    public final double getMinVelocity()
    {
       double minVel = Double.MAX_VALUE;
       for (Event ev : getEvents())
          minVel = Math.min(ev.getVelocity(), minVel);
       return minVel;
    }
 
    /**
     * Gets the number of bars in the recording
     * 
     * @return THe number of bars (double but whole number)
     */
    public final double getNumberOfBars()
    {
       return upperBound - lowerBound;
    }
 
    public final int getNumberOfVoices()
    {
       return getVoices().size();
    }
 
    /**
     * Gets the time offset for the given zoom level
     * 
     * @return The time offset for the given zoom level as a double
     */
    public final double getOffset()
    {
       return lowerBound * barLength;
    }
 
    /**
     * Gets the pitch as a percentage of the range
     * 
     * @param ev
     *           THe event to get the pitch perc for
     * @return The pitch as a percentage of the range ([0,1])
     */
    public final double getPitchPerc(final Event ev)
    {
       double pitchRange = getPitchRange();
       return (ev.getPitch() - bottomPitch) / pitchRange;
    }
 
    /**
     * Gets the pitch range of all the events
     * 
     * @return double pitch range (top - bottom pitch)
     */
    public final double getPitchRange()
    {
       double topPitch = events.get(0).getPitch();
       bottomPitch = events.get(0).getPitch();
 
       for (int i = 1; i < events.size(); i++)
       {
          topPitch = Math.max(topPitch, events.get(i).getPitch());
          bottomPitch = Math.min(bottomPitch, events.get(i).getPitch());
       }
 
       return topPitch - bottomPitch;
    }
 
    /**
     * Gets the score time of the event
     * 
     * @param ev
     *           The event to get time for
     * @return The time in seconds that this event should have occured
     */
    public final double getScoreTime(final Event ev)
    {
       return ev.getBar() * barLength - (avgBarLength * minBar);
    }
 
    /**
     * Gets the chosen subject
     * 
     * @return chosen subject
     */
    public final Integer getSelectedSubject()
    {
       return selectedSubject;
    }
 
    /**
     * Gets the chosen trial
     * 
     * @return chosen trial
     */
    public final Integer getSelectedTrial()
    {
       return selectedTrial;
    }
 
    /**
     * Computes the total length of the piece
     * 
     * @return double length of the piece in seconds - guaranteed to be higher
     *         that the number of notes
     */
    public final double getTotalLength()
    {
       return getNumberOfBars() * barLength;
    }
 
    /**
     * Gets the velocity as a normalised percentage
     * 
     * @param ev
     *           The event to get percentage for
     * @return The velocity as a double in the range [0,1]
     */
    public final double getVelocityPerc(final Event ev)
    {
 
       return (ev.getVelocity() - getMinVelocity()) / (getMaxVelocity() - getMinVelocity());
    }
 
    public final Set<Double> getVoices()
    {
       Set<Double> voices = new TreeSet<Double>();
       for (Event ev : events)
          voices.add(ev.getPitch());
       return voices;
    }
 
    /**
     * Sets the length of a bar
     * 
     * @param val
     *           The length of a bar in seconds
     */
    public final void setBarLength(final double val)
    {
       barLength = val;
       updateListeners();
    }
 
    /**
     * Updates the listeners that we have a new model
     */
    private void updateListeners()
    {
       // Prevents concurrent modifications elsewhere (hack)
       List<ModelListener> tempListeners = new LinkedList<ModelListener>(listeners);
       for (ModelListener listener : tempListeners)
          listener.newModelLoaded(this);
    }
 
    /**
     * Zooms in the model to a given range
     * 
     * @param lower
     *           THe lower range to zoom to
     * @param upper
     *           THe upper range to zoom to
     */
    public final void zoom(final double lower, final double upper)
    {
       lowerBound = lower;
       upperBound = upper;
    }
 
    /**
     * Helper function to get the pitch value from the string
     * 
     * @param pitchName
     *           The string name of the pitch value (e.g. A3)
     * @return The corresponding pitch value
     */
    private static double convertPitch(final String pitchName)
    {
       if (noteMap.size() == 0)
          loadNoteMap();
       return noteMap.get(pitchName);
    }
 
    /**
     * Generates a model given a file and other parameters
     * 
     * @param f
     *           File to read
     * @param subject
     *           THe subject to consider
     * @param trial
     *           The trial to consider
     * @param lower
     *           the Lower bound of zoom
     * @param upper
     *           the Upper bound of zoom
     * @param bLength
     *           The length of a bar
     * @return A valid model for this subject and trial
     * @throws IOException
     *            if something goes wrong reading the file
     */
    public static final Model generateModel(final File f, final int subject, final int trial,
          final double lower, final double upper, final double bLength) throws IOException
    {
       Model mod = new Model();
       mod.selectedSubject = subject;
       mod.selectedTrial = trial;
       mod.lowerBound = lower - 1;
       mod.upperBound = upper;
       mod.barLength = bLength;
 
       CSVReader reader = new CSVReader(new FileReader(f));
       String[] headers = reader.readNext();
       int onsetPos, scoreTime, velocity, voice, subj, tri;
       onsetPos = getHeader(headers, "Onset");
       scoreTime = getHeader(headers, "ScoreTime");
       velocity = getHeader(headers, "Velocity");
       voice = getHeader(headers, "Voice");
       subj = getHeader(headers, "Subject");
       tri = getHeader(headers, "Trial");
       int pitch = getHeader(headers, "Pitch");
       int targetons = getHeader(headers, "TargetOnset");
       int targetvel = getHeader(headers, "TargetVelocity");
 
       // Is the data pitched or voiced?
       boolean pitched = (pitch != -1);
 
       Map<Integer, Double> barTimeMap = new TreeMap<Integer, Double>();
       Map<Integer, List<Double>> targBarTimeMap = new TreeMap<Integer, List<Double>>();
 
       // Two types of data
       if (subj == -1)
          for (String[] nextLine = reader.readNext(); nextLine != null; nextLine = reader.readNext())
          {
             double tScoreTime = Double.parseDouble(nextLine[scoreTime]);
             double targetOnset = -1;
             if (targetons >= 0)
                targetOnset = Double.parseDouble(nextLine[targetons]);
 
             double targetVel = -1;
             if (targetvel >= 0)
                targetOnset = Double.parseDouble(nextLine[targetvel]);
 
             if (tScoreTime >= lower && tScoreTime <= upper + 1)
             {
 
                Event ev = new Event(Double.parseDouble(nextLine[velocity]),
                      Double.parseDouble(nextLine[onsetPos]), Double.parseDouble(nextLine[voice]),
                      Double.parseDouble(nextLine[scoreTime]), targetOnset, targetVel);
                if (pitched)
                   ev = new Event(Double.parseDouble(nextLine[velocity]),
                         Double.parseDouble(nextLine[onsetPos]), convertPitch(nextLine[pitch]),
                         Double.parseDouble(nextLine[scoreTime]), targetOnset, targetVel);
 
                mod.events.add(ev);
 
                if (!nextLine[scoreTime].contains("."))
                {
                   // Fill up the targ Bar TimeMap
                   if (!targBarTimeMap.containsKey((int) ev.getBar()))
                      targBarTimeMap.put((int) ev.getBar(), new LinkedList<Double>());
                   targBarTimeMap.get((int) ev.getBar()).add(targetOnset);
 
                   barTimeMap.put(Integer.parseInt(nextLine[scoreTime]),
                         Double.parseDouble(nextLine[onsetPos]));
                }
             }
             System.out.println(nextLine[scoreTime]);
             mod.maxBar = Math.max(mod.maxBar, Double.parseDouble(nextLine[scoreTime]));
             mod.minTime = Math.min(Double.parseDouble(nextLine[onsetPos]), mod.minTime);
             mod.minBar = Math.min(mod.minBar, Double.parseDouble(nextLine[scoreTime]));
          }
       else
          for (String[] nextLine = reader.readNext(); nextLine != null; nextLine = reader.readNext())
          {
 
             if (Integer.parseInt(nextLine[subj]) == subject)
             {
                if (Integer.parseInt(nextLine[tri]) == trial)
                {
                   double tScoreTime = Double.parseDouble(nextLine[scoreTime]);
                   double targetOnset = -1;
                   if (targetons >= 0)
                      targetOnset = Double.parseDouble(nextLine[targetons]);
 
                   double targetVel = -1;
                   if (targetvel >= 0)
                      targetVel = Double.parseDouble(nextLine[targetvel]);
                   Event ev = null;
                   if (tScoreTime >= lower && tScoreTime <= upper + 1)
                   {
                      ev = new Event(Double.parseDouble(nextLine[velocity]),
                            Double.parseDouble(nextLine[onsetPos]),
                            Double.parseDouble(nextLine[voice]),
                            Double.parseDouble(nextLine[scoreTime]), targetOnset, targetVel);
                      mod.events.add(ev);
                   }
                   mod.maxBar = Math.max(mod.maxBar, Double.parseDouble(nextLine[scoreTime]));
                   mod.minTime = Math.min(Double.parseDouble(nextLine[onsetPos]), mod.minTime);
                   mod.minBar = Math.min(mod.minBar, Double.parseDouble(nextLine[scoreTime]));
 
                   if (!nextLine[scoreTime].contains("."))
                   {
                      // Fill up the targ Bar TimeMap
                      if (ev != null)
                      {
                         if (!targBarTimeMap.containsKey((int) ev.getBar()))
                            targBarTimeMap.put((int) ev.getBar(), new LinkedList<Double>());
                         targBarTimeMap.get((int) ev.getBar()).add(targetOnset);
                      }
                      barTimeMap.put(Integer.parseInt(nextLine[scoreTime]),
                            Double.parseDouble(nextLine[onsetPos]));
                   }
                }
 
                mod.trials.add(Integer.parseInt(nextLine[tri]));
             }
             mod.subjects.add(Integer.parseInt(nextLine[subj]));
 
          }
 
       // Offset all the times to start from zero
       for (Event ev : mod.events)
          ev.offsetOnsetTime(mod.minTime);
 
       // Set the target bar times
       for (Integer key : targBarTimeMap.keySet())
       {
          Double sum = 0.0;
          for (Double val : targBarTimeMap.get(key))
             sum += val;
          mod.barStarts.put(key, sum / targBarTimeMap.get(key).size());
       }
 
       // Set the average bar time
       if (bLength < 0)
       {
          double sum = 0;
          double count = 0;
          for (int i = 1; i < mod.getMaxBar() - 1; i += 1)
             if (barTimeMap.containsKey(i) && barTimeMap.containsKey(i + 1))
             {
                sum += barTimeMap.get(i + 1) - barTimeMap.get(i);
                count++;
             }
          mod.avgBarLength = sum / count;
          mod.barLength = mod.avgBarLength;
       }
       else
       {
          mod.avgBarLength = bLength;
          mod.barLength = bLength;
       }
 
       return mod;
    }
 
    /**
     * Helper function to get the index of a string in the array
     * 
     * @param list
     *           THe list of headers
     * @param key
     *           THe key to search for
     * @return The index of the key within the list
     */
    private static int getHeader(final String[] list, final String key)
    {
       for (int i = 0; i < list.length; i++)
          if (list[i].equals(key))
             return i;
       return -1;
    }
 
    /**
     * Loads the note map
     */
    private static void loadNoteMap()
    {
       double noteVal = 21;
       try
       {
          InputStream is = Model.class.getResourceAsStream("/etc/midi.txt");
          if (is == null)
             is = new FileInputStream("src/main/resources/etc/midi.txt");
          BufferedReader reader = new BufferedReader(new InputStreamReader(is));
          for (String line = reader.readLine(); line != null; line = reader.readLine())
          {
             noteMap.put(line.trim(), noteVal);
             noteVal = noteVal + 1;
          }
          reader.close();
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 }
