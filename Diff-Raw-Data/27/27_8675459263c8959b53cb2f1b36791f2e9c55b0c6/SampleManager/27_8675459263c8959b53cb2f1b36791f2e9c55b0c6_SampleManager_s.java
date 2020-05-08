 /*
  * This file is part of Beads. See http://www.beadsproject.net for all information.
  */
 package net.beadsproject.beads.data;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * SampleManager provides a static repository for {@link Sample} data and provides methods to organise samples into groups.
  */
 public class SampleManager {
 
 	/*
 	 * TODO perhaps SampleManager should run the thread that takes care of the data removal that is currently
 	 * running in Sample.
 	 */
 	
 	/** List of all Samples, indexed by name. */
 	private final static Map<String, Sample> samples = new TreeMap<String, Sample>();
 	
 	/** List of Sample groups, indexed by group name. */
 	private final static Map<String, ArrayList<Sample>> groups = new TreeMap<String, ArrayList<Sample>>();
 
 	private static boolean verbose = true;
 	
 	/** The regime to use when loading the next sample. */
 	private static Sample.Regime nextBufferingRegime = null;
 		
 	/**
 	 * Returns a new Sample from the given filename. If the Sample has already
 	 * been loaded, it will not be loaded again, but will simply be retrieved
 	 * from the static repository.
 	 * 
 	 * @param fn the file path.
 	 * 
 	 * @return the sample.
 	 */
 	public static Sample sample(String fn) {
 		Sample sample = samples.get(fn);
 		if (sample == null) {
 			try {
 				if (nextBufferingRegime!=null)
 					sample = new Sample(fn,nextBufferingRegime);
 				else
 					sample = new Sample(fn);
 				samples.put(fn, sample);
 				if(verbose) System.out.println("Loaded " + fn);
 			} catch (UnsupportedAudioFileException e) {
 				 e.printStackTrace();
 			} catch (IOException e) {
 				 e.printStackTrace();
 			}
 		}
 		return sample;
 	}
 
 	/**
 	 * Adds a sample by name to the sample list. This lets you load samples with a different buffering regime.
 	 * @param name
 	 * @param sample
 	 */
 	public static void sample(String name, Sample sample) {
 		if (samples.get(name) == null) {
 			samples.put(name, sample);
 		}
 	}
 	
 	/**
 	 * Like {@link SampleManager#sample(String)} but with the option to specify the name with which this {@link Sample} is indexed.
 	 * 
 	 * @param ref the name with which to index this Sample.
 	 * @param fn the file path.
 	 * 
 	 * @return the sample.
 	 * @throws IOException 
 	 * @throws UnsupportedAudioFileException 
 	 */
 	public static Sample sample(String ref, String fn) throws UnsupportedAudioFileException, IOException {
 		Sample sample = samples.get(ref);
 		if (sample == null) {
 			if (nextBufferingRegime!=null)
 				sample = new Sample(fn,nextBufferingRegime);
 			else
 				sample = new Sample(fn);
 			samples.put(ref, sample);
 			if(verbose) System.out.println("Loaded " + fn);
 		}
 		return sample;
 	}
 
 	/**
 	 * Generates a new group with the given group name and list of Samples to be
 	 * added to the group.
 	 * 
 	 * @param groupName the group name.
 	 * @param sampleList the sample list.
 	 */
 	public static void group(String groupName, Sample[] sampleList) {
 		ArrayList<Sample> group;
 		if (!groups.keySet().contains(groupName))
 			group = new ArrayList<Sample>();
 		else
 			group = groups.get(groupName);
 		for (int i = 0; i < sampleList.length; i++) {
 			if (!group.contains(sampleList[i]))
 				group.add(sampleList[i]);
 		}
 	}
 
 	/**
 	 * Generates a new group with the given group name and a string that
 	 * specifies where to load samples to be added to the group. The string is interpreted firstly as a URL, and if that fails, as a folder path.
 	 * 
 	 * @param groupName the group name.
 	 * @param folderName the folder address (URL or file path).
 	 */
 	public static void group(String groupName, String folderName) {
 		//first try interpreting the folderName as a system resource
 		File theDirectory = null;
 		try {
 			URL url = ClassLoader.getSystemResource(folderName);
 			if(url != null) {
 				theDirectory = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		//failing that, try it as a plain file path
 		if(theDirectory == null || !theDirectory.exists()) {
 			theDirectory = new File(folderName);
 		}
 		String[] fileNameList = theDirectory.list();
 		for (int i = 0; i < fileNameList.length; i++) {
 			fileNameList[i] = theDirectory.getAbsolutePath() + "/" + fileNameList[i];
 		}
 		group(groupName, fileNameList);
 	}
 
 	/**
 	 * Generates a new group with the given group name and a list of file names
 	 * to be added to the group.
 	 * 
 	 * @param groupName the group name.
 	 * @param fileNameList the file name list.
 	 */
 	public static void group(String groupName, String[] fileNameList) {
 		ArrayList<Sample> group;
 		if (!groups.keySet().contains(groupName)) {
 			group = new ArrayList<Sample>();
 			groups.put(groupName, group);
 		} else
 			group = groups.get(groupName);
 		for (int i = 0; i < fileNameList.length; i++) {
 			String simpleName = new File(fileNameList[i]).getName();
 			try {
 				Sample sample = sample(simpleName, fileNameList[i]);
 				if (!group.contains(simpleName) && sample != null) {
 					group.add(sample);
 				}
 			} catch(Exception e) {
 				//snuff the exception
 			}
 		}
 	}
 	
 	/**
 	 * Gets the set of group names.
 	 * @return Set of Strings representing group names.
 	 */
 	public static Set<String> groups() {
 		return groups.keySet();
 	}
 
 	/**
 	 * Gets the specified group in the form ArrayList&lt;Sample&gt;.
 	 * 
 	 * @param groupName the group name.
 	 * 
 	 * @return the group.
 	 */
 	public static ArrayList<Sample> getGroup(String groupName) {
 		return groups.get(groupName);
 	}
 	
 	/**
 	 * Gets a random sample from the specified group.
 	 * 
 	 * @param groupName the group.
 	 * 
 	 * @return a random Sample.
 	 */
 	public static Sample randomFromGroup(String groupName) {
 		ArrayList<Sample> group = groups.get(groupName);
 		return group.get((int)(Math.random() * group.size()));
 	}
 
 	/**
	 * Gets the Sample at the specified index from the specified group.
 	 * 
 	 * @param groupName the group name.
 	 * @param index the index.
 	 * 
 	 * @return the Sample.
 	 */
 	public static Sample fromGroup(String groupName, int index) {
 		ArrayList<Sample> group = groups.get(groupName);
 		return group.get(index % group.size());
 	}
 	
 	/**
 	 * Removes the named {@link Sample}.
 	 * 
 	 * @param sampleName the sample name.
 	 */
 	public static void removeSample(String sampleName) {
 		samples.remove(sampleName);
 	}
 
 	/**
 	 * Removes the {@link Sample}.
 	 * 
 	 * @param sample the Sample.
 	 */
 	public static void removeSample(Sample sample) {
 		for (String str : samples.keySet()) {
 			if (samples.get(str).equals(sample)) {
 				removeSample(str);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Removes the specified group, without removing the samples.
 	 * 
 	 * @param groupName the group name.
 	 */
 	public static void removeGroup(String groupName) {
 		groups.remove(groupName);
 	}
 
 	/**
 	 * Removes the specified group, and removes all of the samples found in the
 	 * group from the sample repository.
 	 * 
 	 * @param groupName the group name.
 	 */
 	public static void destroyGroup(String groupName) {
 		ArrayList<Sample> group = groups.get(groupName);
 		for (int i = 0; i < group.size(); i++) {
 			removeSample(group.get(i));
 		}
 		removeGroup(groupName);
 	}
 
 	
 	/**
 	 * Prints a list of all {@link Sample}s to System.out.
 	 */
 	public static void printSampleList() {
 		for(String s : samples.keySet()) {
 			System.out.println(s + " " + samples.get(s));
 		}
 	}
 	
 	/**
 	 * Returns an ArrayList containing all of the Sample names.
 	 * 
 	 * @return ArrayList of Sample names.
 	 */
 	public static List<String> getSampleNameList() {
 		return new ArrayList<String>(samples.keySet());
 	}
 	
 	/**
 	 * Set the buffering regime to use when loading all future samples.
 	 * By default the regime is null, and reverts to the default regime used in Sample (which is TOTAL).
 	 * 
 	 * @param r The regime.
 	 */
 	public static void setBufferingRegime(Sample.Regime r)
 	{
 		nextBufferingRegime = r;		
 	}
 
 	/**
 	 * Determines if SampleManager is being verbose.
 	 * 
 	 * @return true if verbose.
 	 */
 	public static boolean isVerbose() {
 		return verbose;
 	}
 
 	/**
 	 * Tells SampleManager to produce verbose output.
 	 * 
 	 * @param verbose true for verbose output.
 	 */
 	public static void setVerbose(boolean verbose) {
 		SampleManager.verbose = verbose;
 	}
 	
 	
 }
