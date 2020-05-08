 package sorganiser.data;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 /**
  * VideoFile class is a simple extension of the File class. Only video files will be recognised by this program
  * @author Peter Pretorius
  *
  */
 public class VideoFile extends File implements Comparable<File> {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	//The extension of this video file
 	private String extension;
 	
 	//season and episode numbers of this video file
 	private int season, episode;
 	
 	//The comparator for this class assists with list sorting
 	private static Comparator<VideoFile> comparator;
 
 	public VideoFile(String path) {
 		super(path);
 		
 		comparator = new VideoComparator();
 		
 		//grab extension from filename
 		extension = path.substring(path.lastIndexOf("."));
 		
 		determineNumbers();
 	}
 	
 	/**
 	 * Attempts to find the season and episode numbers present in this filename
 	 */
 	public void determineNumbers()
 	{
 		ArrayList<String> numbers = getNumbers(getName());
 		
 		//No season or episode number was found in the file name
 		if (numbers.isEmpty())
 		{
 			episode = 0;
 			season = 0;
 		}
 		
 		//single number was found
 		else if (numbers.size() == 1)
 		{
 			//3 digit number, must be in [XYY] format e.g [101]
 			if (numbers.get(0).length() == 3)
 			{
 				season = Integer.parseInt(""+numbers.get(0).charAt(0));
 				episode = Integer.parseInt(""+numbers.get(0).substring(1));
 			}
 			//single digit, must be episode number
 			else
 				episode = Integer.parseInt(numbers.get(0));
 		}
 		
		//any other number of number groups, simply take the first 2 numbers as season and episode UNLESS the first number is 3 digits
		//in which case use that as the basis for season/episode
 		else
 		{
			
			if (Integer.parseInt(numbers.get(0)) > 100)
			{
				season = Integer.parseInt(""+numbers.get(0).charAt(0));
				episode = Integer.parseInt(""+numbers.get(0).substring(1));
			}
			
			else
			{
				season = Integer.parseInt(numbers.get(0));
				episode = Integer.parseInt(numbers.get(1));
			}
 		}	
 	}
 	
 	/**
 	 * Finds groups of integers present in a filename
 	 * @param n
 	 * @return
 	 */
 	public ArrayList<String> getNumbers(String n)
 	{
 		char[] chars = n.toCharArray();
 		ArrayList<String> int_groups = new ArrayList<String>(); //The collection of all groups of integers present in a filename
 		String int_group = ""; //The current group of integers
 		for (int j = 0; j < chars.length-1; j++)
 		{
 			if (Character.isDigit(chars[j])) //search for a digit
 			{
 				int_group += (chars[j]+"");
 				int curIndex = j+1;
 				while (Character.isDigit(chars[curIndex])) //grab all consecutive digits
 				{
 					int_group += (chars[curIndex++]+"");
 					j++;
 				}
 				int_groups.add(int_group);
 				int_group = "";
 			}
 		}
 		return int_groups;
 	}
 	
 	public String getExtension()
 	{
 		return extension;
 	}
 	
 	public int getSeason()
 	{
 		return season;
 	}
 	
 	public int getEpisode()
 	{
 		return episode;
 	}
 	
 	public static Comparator<VideoFile> getComparator()
 	{
 		return comparator;
 	}
 	
 
 	private class VideoComparator implements Comparator<VideoFile>
 	{
 
 		@Override
 		public int compare(VideoFile vf1, VideoFile vf2) {
 						
 			return vf1.getName().compareTo(vf2.getName());
 		}
 		
 	}
 
 }
