 package cobsScripts;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.zip.GZIPInputStream;
 
 import utils.Avevar;

 public final class ResultsFileLine
 {
 	//region1	region2	combinedType	score	averageDistance	minDistance
 	private final String parentFileName;
 	private final String region1;
 	private final String region2;
 	private final String combinedType;
 	private final double score;
 	private final double averageDistance;
 	private final double minDistance;
 	private double percentile;
 	
 	public ResultsFileLine(ResultsFileLine oldLine, double newScore)
 	{
 		this.parentFileName = oldLine.parentFileName;
 		this.region1 = oldLine.region1;
 		this.region2 = oldLine.region2;
 		this.combinedType = oldLine.combinedType;
 		this.score = newScore;
 		this.averageDistance = oldLine.averageDistance;
 		this.minDistance = oldLine.minDistance;
 	}
 	
 	public double getPercentile()
 	{
 		return percentile;
 	}
 	
 	public void setPercentile(double percentile)
 	{
 		this.percentile = percentile;
 	}
 	
 	public String getParentFileName()
 	{
 		return parentFileName;
 	}
 	
 	public String getRegion1()
 	{
 		return region1;
 	}
 
 	public String getRegion2()
 	{
 		return region2;
 	}
 
 	public String getCombinedType()
 	{
 		return combinedType;
 	}
 
 	public double getScore()
 	{
 		return score;
 	}
 
 	public double getAverageDistance()
 	{
 		return averageDistance;
 	}
 
 	public double getMinDistance()
 	{
 		return minDistance;
 	}
 	
 	private static class RegionParser
 	{
 		char chainStart;
 		char chainEnd;
 		int start;
 		int stop;
 		String originalString;
 		
 		@Override
 		public String toString()
 		{
 			return originalString;
 		}
 		
 		public RegionParser(String s) throws Exception
 		{
 			originalString = s;
 			s= s.substring(s.indexOf(":") + 1);
 			chainStart = s.charAt(0);
 			s=s.substring(1);
 			start = Integer.parseInt(new StringTokenizer(s,"-").nextToken());
 			s = s.substring(s.indexOf("-")+1);
 			chainEnd = s.charAt(0);
 			
 			if( chainStart != chainEnd)
 				throw new Exception("No " + chainStart + " " + chainEnd);
 			
 			s =s.substring(1);
 			stop= Integer.parseInt(s);
 		}
 	}
 	
 	
 	public static final class SortByAverageDistance implements Comparator<ResultsFileLine>
 	{
 		@Override
 		public int compare(ResultsFileLine arg0, ResultsFileLine arg1)
 		{
 			return Double.compare(arg0.getAverageDistance(), arg1.getAverageDistance());
 		}
 	}
 	
 	public static final class SortByScore implements Comparator<ResultsFileLine>
 	{
 		@Override
 		public int compare(ResultsFileLine arg0, ResultsFileLine arg1)
 		{
 			return Double.compare(arg1.getScore(), arg0.getScore());
 		}
 	}
 	
 	/*
 	 * As a side effect, sorts the list by average distance
 	 */
 	public static final double getMedianAverageDistance(List<ResultsFileLine> list)
 	{
 		Collections.sort(list, new SortByAverageDistance());
 		
 		if(list.size() % 2 == 1)
 			return list.get(list.size()/2).getAverageDistance();
 		
 		return (list.get(list.size()/2 -1).getAverageDistance() + list.get(list.size()/2).getAverageDistance()) / 2.0;
 	}
 
 	public static List<ResultsFileLine> getNormalizedList(List<ResultsFileLine> inList) throws Exception
 	{
 		List<ResultsFileLine> returnList = new ArrayList<ResultsFileLine>();
 		HashMap<String, Double> colAverages = breakByColumn(inList);
 		double sum =0;
 		int n =0;
 		for( ResultsFileLine rfl : inList )
 		{
 			if( ! Double.isInfinite(rfl.score) && ! Double.isNaN(rfl.score) )
 			{
 				sum += rfl.score;
 				n++;
 			}
 		}
 		
 		double average = sum / n;
 		
 		for( ResultsFileLine rfl : inList)
 		{
 			double newScore = rfl.getScore() 
 					- colAverages.get(rfl.region1) * colAverages.get(rfl.region2) / average;
 			
 			if( ! Double.isInfinite(newScore) && ! Double.isNaN(newScore) )
 			returnList.add(new ResultsFileLine(rfl,newScore));
 		}
 		
 		return returnList;
 	}
 	
 	/*
 	 * Made public for testing
 	 */
 	public static HashMap<String, Double> breakByColumn(List<ResultsFileLine> list)
 		throws Exception
 	{
 		HashMap<String, List<Double>> map = new HashMap<String, List<Double>>();
 		
 		for(ResultsFileLine rsf : list)
 		{
 			putIntoMap(rsf.region1, rsf.score , map);
 			putIntoMap(rsf.region2, rsf.score, map);
 		}
 		
 		HashMap<String, Double> returnMap = new HashMap<String, Double>();
 		
 		for(String s: map.keySet())
 			returnMap.put(s, new Avevar(map.get(s)).getAve());
 		
 		return returnMap;
 	}
 	
 	private static void putIntoMap(String key, double aDoub, HashMap<String, List<Double>> map) throws Exception
 	{
 		List<Double> list = map.get(key);
 		
 		if( list == null)
 		{
 			list= new ArrayList<Double>();
 			map.put(key,list);
 		}
 		
 		if( ! Double.isInfinite(aDoub) && ! Double.isNaN(aDoub) )
 			list.add(aDoub);
 		
 	}
 	
 	public ResultsFileLine(String s, String fileName) throws Exception
 	{
 		StringTokenizer sToken = new StringTokenizer(s, "\t");
 		
 		//System.out.println("Now parsing file: " + fileName);
 		
 		this.parentFileName = fileName;
 		this.region1 = new String( sToken.nextToken());
 		this.region2 = new String( sToken.nextToken());
 		this.combinedType = new String( sToken.nextToken());
 		this.score = Double.parseDouble(sToken.nextToken());
 		this.averageDistance = Double.parseDouble(sToken.nextToken());
 		this.minDistance = Double.parseDouble(sToken.nextToken());
 		
 		if( sToken.hasMoreTokens())
 			throw new Exception("No");
 	}
 	
 	public String getRegionKey()
 	{
 		List<String> list = new ArrayList<String>();
 		list.add(this.region1);
 		list.add(this.region2);
 		Collections.sort(list);
 		
 		return list.get(0) + "@" + list.get(1);
 	}
 	
 	public boolean twoRegionsAreEqual()
 	{
 		return this.region1.equals(region2);
 	}
 	
 	/*
 	 * A region can be an exact match or not in the set.
 	 * But if it overlaps with a known region it is not ok.
 	 * 
 	 * 
 	 */
 	private static boolean isOkForRegionSet( HashSet<String> regionSet, ResultsFileLine rfl)
 		throws Exception
 	{
 		if(regionSet.contains(rfl.region1) && regionSet.contains(rfl.region2))
 			return true;
 		
 		RegionParser rp1 = new RegionParser(rfl.region1);
 		
 		for( String r : regionSet )
 			if( overlaps( new RegionParser(r), rp1))
 				return false;
 		
 		regionSet.add(rfl.region1);
 		
 		RegionParser rp2 = new RegionParser(rfl.region2);
 		
 		for( String r : regionSet )
 			if( overlaps( new RegionParser(r), rp2))
 				return false;
 		
 		regionSet.add(rfl.region2);
 		
 		return true;
 		
 	}
 	
 	private static boolean overlaps(RegionParser rp1, RegionParser rp2)
 	{
 		
 		if(rp1.originalString.equals(rp2.originalString))
 			return false;
 		
 		if( rp1.chainEnd != rp2.chainEnd)
 			return false;
 		
 		if( rp1.chainStart != rp2.chainStart)
 			return false;
 		
 		if( rp1.start >= rp2.start && rp1.start <= rp2.stop )
 		{
 			System.out.println("Removing "+ rp1.toString() + " from "  + rp2.toString());
 			System.out.println(rp1.start + " " + rp1.stop + " " + rp2.start + " " + rp2.stop);
 			return true;
 		}
 			
 		if( rp1.stop >= rp2.start && rp1.stop <= rp2.stop )
 		{
 			System.out.println("Removing "+ rp1.toString() + " from "  + rp2.toString());
 			System.out.println(rp1.start + " " + rp1.stop + " " + rp2.start + " " + rp2.stop);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public static List<ResultsFileLine> parseResultsFile(File file) throws Exception
 	{
 		List<ResultsFileLine> list = new ArrayList<ResultsFileLine>();
 		
 		BufferedReader reader = file.getName().toLowerCase().endsWith("gz")
 				 ? new BufferedReader(new InputStreamReader( 
 							new GZIPInputStream( new FileInputStream( file )))) 
 					: new BufferedReader(new FileReader(file));
 		
 		reader.readLine();
 		
 		HashSet<String> done = new HashSet<String>();
 		HashSet<String> regionSet = new HashSet<String>();
 		
 		for(String s= reader.readLine(); s != null; s = reader.readLine())
 		{
 			ResultsFileLine rfl = new ResultsFileLine(s, file.getName());
 			
 			String key = rfl.getRegionKey();
 				
 			if( ! rfl.twoRegionsAreEqual() && ! done.contains(key) && isOkForRegionSet(regionSet,rfl))
 			{
 					done.add(key);
 					list.add(rfl);			
 			}
 		}
 		
 		reader.close();
 		return list;
 	}
 }
