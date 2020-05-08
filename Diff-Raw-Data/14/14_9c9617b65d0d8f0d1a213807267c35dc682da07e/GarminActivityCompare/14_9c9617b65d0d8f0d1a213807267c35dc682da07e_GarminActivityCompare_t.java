 /**
  * @author Jason Gossit
  * @version 1.0, 04/30/13
  */
 package jgossit;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 public class GarminActivityCompare {
 
 	PrintWriter printWriter;
 	String title;
 	String activityDir = null;
 	String[] activityFilenames = { "", "" };
 	String[] activityNames = { "", "" };
 	String[] activityTimes = { "", "" };
 	SimpleDateFormat gpxTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
 	SimpleDateFormat filenameTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
 	{
 		gpxTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 		filenameTimeFormat.setTimeZone(TimeZone.getDefault());
 	}
 	// originals from http://www.benjaminkeen.com/google-maps-coloured-markers/
 	// converted with http://websemantics.co.uk/online_tools/image_to_data_uri_convertor/
 	String markerAImageData = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAiCAYAAABfqvm9AAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAATcSURBVHjaYmTADeSB2AWI9YGYDyr2BYgvAfFeIL6LTRNAADFiEeMC4nIgzmRl5xYVktJm4BGQZWBgZGT4+uEJw9unVxh+//zyDig/C4jbgPgzsmaAAEI3UBGI13DzSxrpOxcxqJpG/ucTkmZgZmQAq/v7n+H/5/fPGe6eX814fncPw5d3j68ChUOA+AbMAIAAQjZQBYj3y2q5ybglLfvPzy/M+Oc/0JB/qDYyATErM9DvXz8y7F4Qz3D/wsZXQCFnIL4CkgcIIJiBnEB8VFbT3dA/bxMDEyMbw18G/ABoJsP//38Zts4KZbh3fj3IheZA/AkggJig8mXcAtKGHimL/2Mz7MiqAob1fQ4Mn98+gIuB1DAyMzO4Jcz/zyeipAHk1oLEAQIIZJEkEE8z927kV9J2Zvz9H9UwkCG75kYC6Ydgvpy2B1zuP1AtJzsHIxMbF8P9S5tVQeEPEEAgFzqwsHLJqpqGMfz+h+m1exc2gGl9p3yG68cXYMiD9CgbBv1n5xIUBYUlQACBDDQWktJk4AXG5r//mAZe3DuBQVHfn0HDMoHh1/ePDDfQDAXp4eIWZhSW1gVxdQACCGSgMDc/OGlggDePLzB8fvcQbJiIrAHQUnmwBeiACWgKj6AMiMkPEEDgSGFkYsQakxf3QTSzcwkwPL11gEFa3YHhzZOLYIswcggjxAyAAGIB4vdf3j0FJVoU8PPbB3j4behzxLDIOR7hdZDWL++fgJifAAIIZOCZ9y+vAwWeMXDxSzHAzL1/cQM4zMKrz4O9CwPbpgeALbIJ/QB2OchdXz+9+//mySUQ8wpAAIG8fPD3z6/P7p5fx8DCjHAFKEZBYYZsGAjAIgdkIdiLQBPuX97E+PPb+7egnAYQQCADnwLx7DM7Oxm+f/v4H+ZdcFJxLsAIKyWDAHCsg8MRqPrnz8//T29rBUmBwuAOQADBYoMHiE8q6gdo+WSu/f+PAUcsoWc/Job/O+ZEM946tewekGsKxO8AAgjmyV+gMu7DyxtB71/e5lMzCQbnAnwA5NU9i9IYbxxf+AbI9QJicL4ECCCkUGMAhcGBd8+uRALTEbuipj1GSYNs2Pm9kxnObG/9AeT6APFpmBxAADGjqX0OiuCnNw+GSKraMwiJKmDkHpBhb55e+b9jdgTjv7+/84FCa5HlAQKICYsDVgJDe8nBFbkMv379+I8tMI+sLWf88+vbflChgi4HEEBMOIKoFljUf7x8cAYjK5IfQOx7l7YxPLyyDcStwaYRIIBwGQgK4FkX901i+PHjO9yVwOgH5pKJIOYWID6GTSNAADHhicjJn97e//Lg8lZGULgxg8Lu+TWGx9f3gOVwaQIIIHwGPgbiTTdPLYHEHtCZd86uASanf6BqdBcuTQABxEQg7a55ducww9ev7xn+Ab0LdC1IbBM+DQABRMjAwz++vPv4+tEFhq9f3gCTy0Vw+YBPA0AAsRAwEJQLLr95ct6GEWj1398/X0BbDjgBQACxEJFlzwKTkM3f36DcyXAZVFrhUwwQQMQYePXjqzvAIusTAyHXgQBAABFj4MPP7x4xfPv0ElxMElIMEEDEGPjm68fnsHrnESHFAAFEjIGf//399R/YVACZ+ImQYoAAYiLCwI9A/A2piMMLAAKIhUgDn0EbVK8JKQYIMABhubHIul5+4wAAAABJRU5ErkJggg==";
 	String markerBImageData = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAiCAYAAABfqvm9AAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAATkSURBVHjaYmTADeSB2AWI9YGYDyr2BYgvAfFeIL6LTRNAADFiEeMC4nIgzmTnYhOV0hBlEJDmY2AEqvzw7DPD0+uvGH5+/fUOKD8LiNuA+DOyZoAAQjdQEYjX8IvzGjlnmTOYBGn/F5YUABrGDFb3///f/+9ffmI4v/Ea466pxxneP/10FSgcAsQ3YAYABBCygSpAvF/LUUkmaVbQf34hfsb/DH8Z/v7/i+YlJgZmRhaGrx+/MMzP3sBwcfvNV0BhZyC+ApIHCCCYgZxAfFTTUckwd1k0AzMbM8M/NIPQARMjUM3f/wyzElcynN96A+RCcyD+BBBAMAPrBSR5G2oOZPznFeFmBBl2fPlFMEYGwnICDGGtbgyc/BxgPjPQ0O+ffv5vcpjO+Pbhhx6gUClAADEBCUkgTnbOsmTgF+FjhLns7eMPDLePPWRQs5EHY1ldcYaL224wVBtOglsACg4uPi5GrwJbEDceFAcAAcQCJBzYOFllTQO0gd78g+E1nzJ7OFvNWoFhRtwqhidXXjDI6EiAxUB6DH00/69t2C367eMPZ4AAArnQWEJNhEFIip/hHxDiAxe23YR4XVYALgbSwy3EzSilJQbi6gAEEMiFwsDwA8YeM8gTGIZkijSj8ENbEGEIjyAgFJQCp31+gABiAUc1IyNOV3mX2cHZF7feZFhds4tBRlcC6H151OQENQMggEAGvv/w/DPDfwbsyQQ5DEHsIqUucOyjGvif4f2zTyDGJ4AAAoXhmRe33jC8f/4J6G1GvGH4/eMPUDJh4OJnR0rojAyf3n/5//TqSxD3CkAAgVx48Oe3X8/Ob7ku5ZxqyfDn/28UQ/r8FsHZoKQEApaR+kgJnIXh0vabjMAYfgvKaQABBHLhUyCevWPSEYZvn779hydiYEyqWqGGk4GXOkPhxjh4kgGBn19+/N/WdwjEXADEdwACCOZHHiA+qe+lrpW5MOL/f8Z/+P0OBcCc8n922hrG02uv3ANyTYH4HUAAMUPlfoHKuJe33wa9uPuGz9RPBxjM/wkYxsKwsGAj44kVl94AuV5A/AAkDhBAzEhqQGFw4Nn115HAFMCuaaMKTLJ/cRq2d9Zxhu19R36AIh+IT8PkAAKIGU3tcyC+f/PYgxBVKzkGMXkRoDv/YRj29PrL/7NT1jD+/fMvHyi0FlkeIICYsDhgJdCUJSsrtjP8/PHzP7aktLZhF+Ov77/3A5nT0OUAAogJRxDVAov6j4fmnwGW1axwQRYg+9LumwxX9twBcWuwaQQIIFwGggJ41r5ZJxm+f/8Od+U/oNP3zTwJYm4B4mPYNAIEEBOeiJz89tGHL1d23WYEJV5QYfrsxkuG6wfvgeVwaQIIIHwGPgbiTSdXX4JmMWaGc5uuMfz/9x8ksAuXJoAAYiKQdtfcOf6I4euHL+By7/Lu2yCxTfg0AAQQIQMPf/3w/eOjyy8Yvrz9Ai6pgWAbPg0AAcRCwEBQLrj85PILG1C0/Pn19wW05YATAAQQCxFZ9iwwCdn8/gXONZeB+Cs+xQABRIyBV1/de8fw/fNPBkKuAwGAACLGwIfvnnxk+Pwa7LDrhBQDBBAxBr758PIzsCAFJ+5HhBQDBBAxBn7+9/vf/38M4OzyiZBigABiIsLAj0D8DamIwwsAAoiFSAOfQRtUrwkpBggwAK1GpdpHAecTAAAAAElFTkSuQmCC";
 	
 	public static void main(String[] args) throws IOException, ParseException
 	{
 		new GarminActivityCompare().go();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void go() throws IOException, ParseException
 	{
 		promptForActivities();
 		
 		System.out.println("Creating comparison file...");
 		ArrayList<Double>[] lats = new ArrayList[activityFilenames.length];
 		ArrayList<Double>[] lons = new ArrayList[activityFilenames.length];
 		ArrayList<Double>[] distancesTravelled = new ArrayList[activityFilenames.length];
 		ArrayList<String> htmlContent = new ArrayList<String>();
 		for (int i=0; i<activityFilenames.length; i++)
 		{
 			lats[i] = new ArrayList<Double>();
 			lons[i] = new ArrayList<Double>();
 			distancesTravelled[i] = new ArrayList<Double>();
 			BufferedReader bufferedReader = new BufferedReader(new FileReader(activityDir + activityFilenames[i]));
 			String line = null;
 			boolean reachedStart = false;
 			Date prevTime = null;
 			Double lat = null, prevLat = null;
 			Double lon = null, prevLon = null;
 			while ((line = bufferedReader.readLine()) != null)
 			{
 				if (line.contains("<trk>"))
 					reachedStart = true;
 				if (!reachedStart)
 					continue;
 				
 				if (line.contains("<time>"))
 				{
 					String time = line.substring(line.indexOf("<time>")+6,line.indexOf("</time>"));
 					Date thisTime = gpxTimeFormat.parse(time);
 					if (prevTime != null)
 					{
 						long diff = thisTime.getTime()-prevTime.getTime();
 						if (diff > 1000)
 						{
 							double latDiff = lat - prevLat;
 							double latDiffIncr = latDiff / (diff / 1000);
 							double lonDiff = lon - prevLon;
 							double lonDiffIncr = lonDiff / (diff / 1000);
 							for (int j=1;j<diff/1000;j++)
 							{
 								lats[i].add(prevLat + j*latDiffIncr);
 								lons[i].add(prevLon + j*lonDiffIncr);
 							}
 						}
 						lats[i].add(lat);
 						lons[i].add(lon);
 					}
 					prevTime = thisTime;
 				}
 				else if (line.contains("<trkpt"))
 				{
 					if (lon != null)
 					{
 						prevLon = lon;
 						prevLat = lat;
 					}
 					lon = Double.parseDouble(line.substring(line.indexOf("lon")+5, line.indexOf("lat")-2));
 					lat = Double.parseDouble(line.substring(line.indexOf("lat")+5, line.indexOf(">")-1));
 					if (prevTime == null)
 					{
 						lats[i].add(lat);
 						lons[i].add(lon);
 					}
 				}
 			}
 			bufferedReader.close();
 		}
 		
 		int[] activityOrder = (lats[0].size() >= lats[1].size() ? new int[]{0,1} : new int[]{1,0}); // longest activity first
 		File outputFile = new File(activityDir + activityNames[activityOrder[0]] + " - " + activityTimes[activityOrder[0]] + ".html");
 		if (outputFile.exists())
 			outputFile.delete();
 		printWriter = new PrintWriter(outputFile);
 		
 		for(int i=0;i<activityOrder.length;i++)
 		{
 			/*htmlContent.add("	  var coords" + (i+1) + " = [\n");
 			for (int j=0;j<lats[activityOrder[i]].size();j++)
 			{
 				htmlContent.add(String.format("\t\tnew google.maps.LatLng(%s, %s)%s\n",
 						lats[activityOrder[i]].get(j),
 						lons[activityOrder[i]].get(j),
 						j+1 < lats[activityOrder[i]].size() ? "," : "];\n"));
 			}*/
 			htmlContent.add("	  var lats" + (i+1) + " = [ ");
 			for (int j=0;j<lats[activityOrder[i]].size();j++)
 			{
 				htmlContent.add(String.format("%s%s%s",
 						lats[activityOrder[i]].get(j),
 						j+1 < lats[activityOrder[i]].size() ? "," : "];\n",
 						j > 0 && j % 10 == 0 ? "\n\t\t" : ""));
 			}
 			htmlContent.add("	  var lons" + (i+1) + " = [ ");
 			for (int j=0;j<lons[activityOrder[i]].size();j++)
 			{
 				htmlContent.add(String.format("%s%s%s",
 						lons[activityOrder[i]].get(j),
 						j+1 < lons[activityOrder[i]].size() ? "," : "];\n",
 						j > 0 && j % 10 == 0 ? "\n\t\t" : ""));
 			}
 			
 			htmlContent.add("	  var distance" + (i+1) + " = [ ");
 			double distance = 0;
 			for (int j=1;j<lats[activityOrder[i]].size();j++)
 			{
 				distance += GarminActivityCompare.distanceBetween(lats[activityOrder[i]].get(j), lons[activityOrder[i]].get(j),
 											lats[activityOrder[i]].get(j-1), lons[activityOrder[i]].get(j-1));
 				distancesTravelled[activityOrder[i]].add(distance);
 				htmlContent.add(String.format("%.3f%s%s",
 						distance,
 						j+1 < lats[activityOrder[i]].size() ? "," : "];\n",
 						j % 10 == 0 ? "\n\t\t" : ""));
 			}
 			htmlContent.add("	  var pace" + (i+1) + " = [ 0, ");
 			for (int j=0;j<distancesTravelled[activityOrder[i]].size();j++)
 			{
 				Double distanceNow = distancesTravelled[activityOrder[i]].get(j);
 				int timeFrame = j < 10 ? j+1 : 10;
 				Double distanceBefore = j < 10 ? 0 : distancesTravelled[activityOrder[i]].get(j - timeFrame);
 				Double pace = 3600 * ((distanceNow-distanceBefore)/timeFrame);
 				
 				htmlContent.add(String.format("%.1f%s%s",
 						pace,
 						j+1 < distancesTravelled[activityOrder[i]].size() ? "," : "];\n",
 						j > 0 && j % 10 == 0 ? "\n\t\t" : ""));
 			}
 		}
 		
 		// instead of comparing difference in A and B distance travelled at the same point in time, as the devices may vary,
 		// find the closest point in B and work out how far it has travelled from there to it's current point 
 		htmlContent.add("	  var gap = [ ");
 		int lastTrkPoint = 0; // remember where the last point was to avoid starting all over again or matching the wrong lap if there are several laps
 		int lookAhead = 0; // so it doesn't get tripped up on turnarounds/loops
 		for (int trkPointA=0; trkPointA<lats[activityOrder[0]].size(); trkPointA++)
 		{
 			double lat = lats[activityOrder[0]].get(trkPointA);
 			double lon = lons[activityOrder[0]].get(trkPointA);
 			double smallestDistance = 999999;
 			int lookedAhead = 0;
 			for (int trkPointB=lastTrkPoint; trkPointB<lons[activityOrder[1]].size(); trkPointB++)
 			{
 				double distance = GarminActivityCompare.distanceBetween(lat, lon, lats[activityOrder[1]].get(trkPointB), lons[activityOrder[1]].get(trkPointB));
 				if (smallestDistance == 999999)
 				{
 					smallestDistance = distance;
 					continue;
 				}
 				
 				if (distance < smallestDistance)
 				{
 					smallestDistance = distance;
 				}
 				else if (lookedAhead < lookAhead)
 				{
 					lookedAhead++;
 				}
 				else
 				{
 					if (trkPointB-1 == lastTrkPoint) // matched same point as last time, look ahead further next time in case it's getting stuck
 						lookAhead++;
 					else
 						lookAhead = 0;
 					lastTrkPoint = trkPointB-1;
 					break;
 				}
 			}
 			
 			// more trkPoint's in A than B = A took longer, B is probably at the finish line, use B's last trkPoint
 			// (although sampling frequency could differ...)
 			int currentTrkPointB = trkPointA >= distancesTravelled[activityOrder[1]].size() ? distancesTravelled[activityOrder[1]].size()-1 : trkPointA;
 			int closestTrkPointB = lastTrkPoint == distancesTravelled[activityOrder[1]].size() ? lastTrkPoint-1 : lastTrkPoint;
 			double distance = -1 * (distancesTravelled[activityOrder[1]].get(currentTrkPointB) - distancesTravelled[activityOrder[1]].get(closestTrkPointB));
 			htmlContent.add(String.format("%.3f%s%s",
 					distance,
 					trkPointA+1 < lats[activityOrder[0]].size() ? "," : "];\n",
 					trkPointA > 0 && trkPointA % 10 == 0 ? "\n\t\t" : ""));
 
 		}
 		
 		title = outputFile.getName().substring(0,outputFile.getName().length()-5);
 		printHeader(lats[activityOrder[0]].size());
 		for (String htmlLine : htmlContent)
 			printWriter.print(htmlLine);
 		printFooter();
 		printWriter.close();
 		
 		System.out.format("Comparison file created at '%s'", outputFile.getAbsolutePath());
 	}
 	
 	private void promptForActivities() throws IOException, ParseException
 	{
 		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
 		while (activityDir == null)
 		{
 			System.out.println("Enter the directory containing the Garmin Activity (.gpx) files:");
 			String activityDirInput = inputReader.readLine();
 			File testActivityDir = new File(activityDirInput);
 			if (!testActivityDir.exists())
 			{
 				System.err.println("'" + activityDirInput + "' does not exist");
 				continue;
 			}
 			else if (!testActivityDir.isDirectory())
 			{
 				System.err.println("'" + activityDirInput + "' is not a directory");
 				continue;
 			}
 			else
 			{
 				File[] activityFiles = testActivityDir.listFiles(new FilenameFilter() {
 					@Override
 					public boolean accept(File dir, String name) {
 						return name.toLowerCase().endsWith(".gpx");
 					}
 				});
 				if (activityFiles.length < 2)
 				{
 					System.err.println("'" + activityDirInput + "' does not contain at least 2 activity files");
 					continue;
 				}
 				
 				String[] names = new String[activityFiles.length];
 				String[] times = new String[activityFiles.length];
 				activityDir = activityDirInput + File.separator;
 				for (int i=0; i<activityFiles.length; i++)
 				{
 					String line = null;
 					BufferedReader bufferedReader = new BufferedReader(new FileReader(activityFiles[i]));
 					while ((line = bufferedReader.readLine()) != null)
 					{
 						if (line.contains("<time>"))
 						{
 							String time = line.substring(line.indexOf("<time>")+6,line.indexOf("</time>"));
 							Date date = gpxTimeFormat.parse(time);
 							times[i] = filenameTimeFormat.format(date);
 						}
 						if (line.contains("<name>"))
 						{
 							names[i] = line.substring(line.indexOf("<name>")+6, line.indexOf("</name>"));
 							System.out.format("[%s]: %s - %s (%s)\n", i,
 									activityFiles[i].getName(),
 									times[i],
 									names[i]);
 							break;
 						}
 						else
 							continue;
 					}
 					bufferedReader.close();
 				}
 				
 				int[] activityNumbers = {0,1};
 				while (true)
 				{
 					System.out.println("Enter the numbers of the activity files to compare: (default 0,1)");
 					String activityNumbersInput = inputReader.readLine();
 					if (activityNumbersInput.trim().equals(""))
 						break;
 					String[] activityNumbersArray = activityNumbersInput.split(",");
 					try
 					{
 						if (activityNumbersArray.length != 2)
 							throw new Exception();
 						if (Integer.parseInt(activityNumbersArray[0]) >= activityFiles.length ||
 							Integer.parseInt(activityNumbersArray[1]) >= activityFiles.length)
 							throw new Exception();
 						activityNumbers[0] = Integer.parseInt(activityNumbersArray[0]);
 						activityNumbers[1] = Integer.parseInt(activityNumbersArray[1]);
 						break;
 					}
 					catch(Exception e)
 					{
 						System.err.println("Invalid entry");
 						continue;
 					}
 				}
 				
 				for (int i=0;i<activityNumbers.length;i++)
 				{
 					activityFilenames[i] = activityFiles[activityNumbers[i]].getName();
 					activityNames[i] = names[activityNumbers[i]];
 					activityTimes[i] = times[activityNumbers[i]];
 				}
 			}
 		}
 	}
 	
 	private void printHeader(int numPoints)
 	{
 		printWriter.println("<!DOCTYPE html>");
 		printWriter.println("<html>");
 		printWriter.println("<title>" + title + "</title>");
 		printWriter.println("  <head>");
 		printWriter.println("    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>");
 		printWriter.println("    <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />");
 		printWriter.println("    <style type=\"text/css\">");
 		printWriter.println("      html { height: 100% }");
 		printWriter.println("      body { height: 100%; margin: 0; padding: 0 }");
 		printWriter.println("      #map-canvas { height: 100% }");
 		printWriter.println("	  #toolbar {");
 		printWriter.println("		padding: 4px;");
 		printWriter.println("		display: inline-block;");
 		printWriter.println("	  }");
 		printWriter.println("	  .small-font {");
 		printWriter.println("		font-family: 'Trebuchet MS','Helvetica','Arial','Verdana','sans-serif';");
 		printWriter.println("		font-size: 62.5%;");
 		printWriter.println("	  }");
 		printWriter.println("    </style>");
 		printWriter.println("    <link type=\"text/css\" href=\"http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css\" rel=\"Stylesheet\"/>");
 		printWriter.println("	<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery-1.9.1.js\"></script>");
 		printWriter.println("	<script type=\"text/javascript\" src=\"http://code.jquery.com/ui/1.10.2/jquery-ui.js\"></script>");
 		printWriter.println("	<script>");
 		printWriter.println("	$(function() {");
 		printWriter.println("		$( \"#play\" ).button({");
 		printWriter.println("		text: false,");
 		printWriter.println("		icons: {");
 		printWriter.println("		primary: \"ui-icon-play\"");
 		printWriter.println("		}");
 		printWriter.println("		})");
 		printWriter.println("		.click(function() {");
 		printWriter.println("		var options;");
 		printWriter.println("		if ( $( this ).text() === \"play\" ) {");
 		printWriter.println("		options = {");
 		printWriter.println("		label: \"pause\",");
 		printWriter.println("		icons: {");
 		printWriter.println("		primary: \"ui-icon-pause\"");
 		printWriter.println("		}");
 		printWriter.println("		};");
 		printWriter.println("		play();");
 		printWriter.println("		} else {");
 		printWriter.println("		options = {");
 		printWriter.println("		label: \"play\",");
 		printWriter.println("		icons: {");
 		printWriter.println("		primary: \"ui-icon-play\"");
 		printWriter.println("		}");
 		printWriter.println("		};");
 		printWriter.println("		pause();");
 		printWriter.println("		}");
 		printWriter.println("		$( this ).button( \"option\", options );");
 		printWriter.println("		});");
 		printWriter.println("		");
 		printWriter.println("		$( \"#stop\" ).button({");
 		printWriter.println("		text: false,");
 		printWriter.println("		icons: {");
 		printWriter.println("		primary: \"ui-icon-stop\"");
 		printWriter.println("		}");
 		printWriter.println("		})");
 		printWriter.println("		.click(function() {");
 		printWriter.println("		$( \"#play\" ).button( \"option\", {");
 		printWriter.println("		label: \"play\",");
 		printWriter.println("		icons: {");
 		printWriter.println("		primary: \"ui-icon-play\"");
 		printWriter.println("		}");
 		printWriter.println("		});");
 		printWriter.println("		stop();");
 		printWriter.println("		});");
 		printWriter.println("		");
 		printWriter.println("		$( \"#slider\" ).slider({  orientation: \"horizontal\", range: \"min\", animate: true, min: 0, max: " + (numPoints-1) + " });");
 		printWriter.println("		$( \"#slider\" ).on( \"slidestart\", function( event, ui ) {");
 		printWriter.println("			pauseFromSlide();");
 		printWriter.println("		} );");
 		printWriter.println("		$( \"#slider\" ).on( \"slidestop\", function( event, ui ) {");
 		printWriter.println("			playFromSlide();");
 		printWriter.println("		} );");
 		printWriter.println("		$( \"#slider\" ).on( \"slide\", function( event, ui ) {");
 		printWriter.println("			slide(ui.value);");
 		printWriter.println("		} );");
 		printWriter.println("		");
 		printWriter.println("		$( \"#speedSlider\" ).slider({  orientation: \"horizontal\", range: \"min\", animate: true, value: 50, min: 10, max: 90 });");
 		printWriter.println("		$( \"#speedSlider\" ).on( \"slide\", function( event, ui ) {");
 		printWriter.println("			changeSpeed(ui.value);");
 		printWriter.println("		} );");
 		printWriter.println("	});");
 		printWriter.println("	</script>");
 		printWriter.println("    <script type=\"text/javascript\"");
 		printWriter.println("      src=\"https://maps.googleapis.com/maps/api/js?sensor=false&libraries=geometry\">");
 		printWriter.println("    </script>");
 		printWriter.println("    <script type=\"text/javascript\"");
 		printWriter.println("      src=\"http://google-maps-utility-library-v3.googlecode.com/svn/tags/infobox/1.1.12/src/infobox.js\">");
 		printWriter.println("    </script>");
 		printWriter.println("    <script type=\"text/javascript\">");
 		printWriter.println("	  var map;");
 		printWriter.println("	  var updateDistance = 0;");
 		printWriter.println("	  var marker1;");
 		printWriter.println("	  var coords1;");
 		printWriter.println("	  var coords2;");
 		printWriter.println("	  var marker1;");
 		printWriter.println("	  var marker2;");
 		printWriter.println("	  var paused = true;");
 		printWriter.println("	  var resume = false;");
 		printWriter.println("	  var speed = 50;");
 		printWriter.println("	  var position1 = 0;");
 		printWriter.println("	  var position2 = 0;");
 		printWriter.println("	  var infoBox;");
		printWriter.println("	  var infoBoxClearance = 175;");
 		printWriter.println("	  var upArrow = '<font color=\"green\">&#x25B2</font>';");
 		printWriter.println("	  var neutral = '&#x25AC;';");
 		printWriter.println("	  var downArrow = '<font color=\"red\">&#x25BC;</font>';");
 		printWriter.println("	  var lastGap = 0;");
 	}
 	
 	private void printFooter()
 	{
 		printWriter.println("      function initialize()");
 		printWriter.println("	  {");
 		printWriter.println("        coords1 = new google.maps.MVCArray();");
 		printWriter.println("        for (i=0; i<lats1.length; i++)");
 		printWriter.println("        {");
 		printWriter.println("        	coords1.push(new google.maps.LatLng(lats1[i], lons1[i]));");
 		printWriter.println("        }");
 		printWriter.println("        coords2 = new google.maps.MVCArray();");
 		printWriter.println("        for (i=0; i<lats2.length; i++)");
 		printWriter.println("        {");
 		printWriter.println("        	coords2.push(new google.maps.LatLng(lats2[i], lons2[i]));");
 		printWriter.println("        }");
 		printWriter.println("        var mapOptions = {");
 		printWriter.println("          center: coords1.getAt(position1),");
 		printWriter.println("          zoom: 15,");
 		printWriter.println("          mapTypeId: google.maps.MapTypeId.ROADMAP");
 		printWriter.println("        };");
 		printWriter.println("        map = new google.maps.Map(document.getElementById(\"map-canvas\"),");
 		printWriter.println("            mapOptions);");
 		printWriter.println("		var polyline = new google.maps.Polyline({path:coords1});");
 		printWriter.println("		polyline.setMap(map);");
 		printWriter.println("");
 		printWriter.println("		reset();");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function play()");
 		printWriter.println("	  {");
 		printWriter.println("		if (position1+1 == coords1.getLength()) //finished");
 		printWriter.println("		{");
 		printWriter.println("			position1 = 0;");
 		printWriter.println("			position2 = 0;");
 		printWriter.println("		}");
 		printWriter.println("		paused = false;");
 		printWriter.println("		resume = true;");
 		printWriter.println("		setTimeout(function() {");
 		printWriter.println("			move(position1, marker1, coords1, distance1);");
 		printWriter.println("		}, 0);");
 		printWriter.println("		setTimeout(function() {");
 		printWriter.println("			move(position2, marker2, coords2, distance2);");
 		printWriter.println("		}, 0);");
 		printWriter.println("	  }");
 		printWriter.println("	  function playFromSlide()");
 		printWriter.println("	  {");
 		printWriter.println("		if (resume)");
 		printWriter.println("		  play();");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function pause()");
 		printWriter.println("	  {");
 		printWriter.println("		paused = true;");
 		printWriter.println("	  }");
 		printWriter.println("	  function pauseFromSlide()");
 		printWriter.println("	  {");
 		printWriter.println("		if (paused)");
 		printWriter.println("		  resume = false;");
 		printWriter.println("		pause();");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function stop()");
 		printWriter.println("	  {");
 		printWriter.println("		paused = false;");
 		printWriter.println("		resume = false;");
 		printWriter.println("		infoBox.close();");
 		printWriter.println("		marker1.setMap(null);");
 		printWriter.println("		marker2.setMap(null);");
 		printWriter.println("		position1 = 0;");
 		printWriter.println("		position2 = 0;");
 		printWriter.println("		lastGap = 0;");
 		printWriter.println("		$( \"#slider\" ).slider( \"option\", \"value\", position1 );");
 		printWriter.println("		reset();");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function reset()");
 		printWriter.println("	  {");
 		printWriter.println("		marker1 = new google.maps.Marker({anchorPoint:new google.maps.Point(0,0),map:map,position:coords1.getAt(position1),clickable:false,zIndex:0,icon:'" + markerAImageData + "'});");
 		printWriter.println("		marker2 = new google.maps.Marker({anchorPoint:new google.maps.Point(0,0),map:map,position:coords2.getAt(position2),clickable:false,zIndex:1,icon:'" + markerBImageData + "'});");
 		printWriter.println("		infoBox = new InfoBox({closeBoxURL:\"\",enableEventPropagation:false,alignBottom:true,maxWidth:0,pixelOffset: new google.maps.Size(10, -40),boxStyle:{border: '1px solid black',opacity: 0.8,background:'white',whiteSpace:'nowrap',padding:'5px'}});");
 		printWriter.println("		infoBox.open(map, marker1);");
 		printWriter.println("		infoBox.setContent('A distance 0.000km<br>&nbsp;&nbsp;&nbsp;pace 0.0kmh<br>B distance 0.000km<br>&nbsp;&nbsp;&nbsp;pace 0.0kmh<br>Gap 0.000km');");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function slide(position)");
 		printWriter.println("	  {");
 		printWriter.println("		var gapChangeText = neutral;");
 		printWriter.println("		if (gap[position-1] > gap[position1-1])");
 		printWriter.println("			gapChangeText = upArrow;");
 		printWriter.println("		else if (gap[position-1] < gap[position1-1])");
 		printWriter.println("			gapChangeText = downArrow;");
 		printWriter.println("		position1 = position;");
 		printWriter.println("		marker1.setPosition(coords1.getAt(position1));");
 		printWriter.println("		position2 = position < coords2.getLength() ? position : coords2.getLength()-1;");
 		printWriter.println("		marker2.setPosition(coords2.getAt(position2));");
 		printWriter.println("		panMap(coords1.getAt(position1));");
 		printWriter.println("		updateDistance = 0;");
 		printWriter.println("		lastGap = gap[position-1];");
 		printWriter.println("		infoBox.setContent('A distance ' + distance1[position1-1] + 'km<br>&nbsp;&nbsp;&nbsp;pace ' + pace1[position1] + 'kmh<br>B distance ' + distance2[position2-1] + 'km<br>&nbsp;&nbsp;&nbsp;pace ' + pace2[position2] + 'kmh<br>Gap ' + (position1 == 0 ? '0.000' : gap[position1-1]) + 'km ' + gapChangeText);");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function panMap(coord)");
 		printWriter.println("	  {");
 		printWriter.println("		var mapBounds = map.getBounds();");
 		printWriter.println("		if (mapBounds != null && coord != null)");
 		printWriter.println("		{");
 		printWriter.println("			var ne = mapBounds.getNorthEast();");
 		printWriter.println("			var sw = mapBounds.getSouthWest();");
		printWriter.println("			var mapCanvasProjection = infoBox.getProjection();");
		printWriter.println("			var coordPixelPoint = mapCanvasProjection.fromLatLngToContainerPixel(coord);");
		printWriter.println("			var nePixelPoint = mapCanvasProjection.fromLatLngToContainerPixel(ne);");
		printWriter.println("			if (coordPixelPoint.x+infoBoxClearance > nePixelPoint.x || coordPixelPoint.y-infoBoxClearance < nePixelPoint.y)");
		printWriter.println("			{");
		printWriter.println("				map.panTo(coord);");
		printWriter.println("			}");
		printWriter.println("			else if ( (coord.lat() < ne.lat() && coord.lat() < sw.lat()) ||");
 		printWriter.println("				 (coord.lat() > ne.lat() && coord.lat() > sw.lat()) ||");
 		printWriter.println("				 (coord.lng() < ne.lng() && coord.lng() < sw.lng()) ||");
 		printWriter.println("				 (coord.lng() > ne.lng() && coord.lng() > sw.lng()) )");
 		printWriter.println("			{");
		printWriter.println("				map.panTo(coord);");
 		printWriter.println("			}");
 		printWriter.println("		}");
 		printWriter.println("	  }");
 		printWriter.println("	  ");
 		printWriter.println("	  function changeSpeed(value)");
 		printWriter.println("	  {");
 		printWriter.println("		speed = value;");
 		printWriter.println("	  }");
 		printWriter.println("");
 		printWriter.println("	function move(pos, mark, coords, distance)");
 		printWriter.println("	{");
 		printWriter.println("		if (paused)");
 		printWriter.println("			return;");
 		printWriter.println("		panMap(coords.getAt(pos+1));");
 		printWriter.println("		mark.setPosition(coords.getAt(pos));");
 		printWriter.println("		if (mark == marker1)");
 		printWriter.println("		{");
 		printWriter.println("			updateDistance++;");
 		printWriter.println("			if (updateDistance == 20)");
 		printWriter.println("			{");
 		printWriter.println("				updateDistance = 0;");
 		printWriter.println("				var gapChangeText = neutral;");
 		printWriter.println("				if (gap[pos-1] > lastGap)");
 		printWriter.println("					gapChangeText = upArrow;");
 		printWriter.println("				else if (gap[pos-1] < lastGap)");
 		printWriter.println("					gapChangeText = downArrow;");
 		printWriter.println("				lastGap = gap[pos-1];");
 		printWriter.println("				infoBox.setContent('A distance ' + distance1[pos-1] + 'km<br>&nbsp;&nbsp;&nbsp;pace ' + pace1[pos] + 'kmh<br>B distance ' + distance2[pos-1 < distance2.length ? pos-1 : distance2.length-1] + 'km<br>&nbsp;&nbsp;&nbsp;pace ' + pace2[pos < pace2.length ? pos : pace2.length-1] + 'kmh<br>Gap ' + gap[pos-1] + 'km ' + gapChangeText);");
 		printWriter.println("			}");
 		printWriter.println("			position1 = pos+1;");
 		printWriter.println("			$( \"#slider\" ).slider( \"option\", \"value\", position1 );");
 		printWriter.println("		}");
 		printWriter.println("		else");
 		printWriter.println("			position2 = pos+1;");
 		printWriter.println("		pos++;");
 		printWriter.println("		if (pos+1 < coords.getLength())");
 		printWriter.println("			setTimeout(function() {");
 		printWriter.println("				move(pos, mark, coords, distance);");
 		printWriter.println("			}, 1000/speed);");
 		printWriter.println("	}");
 		printWriter.println("");
 		printWriter.println("      google.maps.event.addDomListener(window, 'load', initialize);");
 		printWriter.println("    </script>");
 		printWriter.println("  </head>");
 		printWriter.println("  <body>");
 		printWriter.println("	<div align=\"center\" class=\"small-font\" style=\"padding-top:5px; padding-bottom:5px\">");
 		printWriter.println("		<div id=\"toolbar\" class=\"ui-widget-header ui-corner-all\" style=\"margin-bottom:5px\">");
 		printWriter.println("			<button id=\"play\">play</button>");
 		printWriter.println("			<button id=\"stop\">stop</button>");
 		printWriter.println("		</div>");
 		printWriter.println("		<br><label>Time:</label>");
 		printWriter.println("		<div id=\"slider\" class=\"ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all small-font\" style=\"width: 50%;margin-bottom:5px;margin-top:2px\"></div>");
 		printWriter.println("		<label>Speed:</label>");
 		printWriter.println("		<div id=\"speedSlider\" class=\"ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all small-font\" style=\"width: 25%;margin-top:2px\">");
 		printWriter.println("		</div>");
 		printWriter.println("	</div>");
 		printWriter.println("    <div id=\"map-canvas\" style=\"font-family: 'Trebuchet MS','Helvetica','Arial','Verdana','sans-serif';\"/>");
 		printWriter.println("  </body>");
 		printWriter.println("</html>");
 	}
 
 	public static double distanceBetween(double lat1, double lng1, double lat2, double lng2)
 	{
 	    int earthRadius = 6371;
 	    double dLat = Math.toRadians(lat2-lat1);
 	    double dLng = Math.toRadians(lng2-lng1);
 	    double sindLat = Math.sin(dLat / 2);
 	    double sindLng = Math.sin(dLng / 2);
 	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
 	            * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
 	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
 	    double distance = earthRadius * c;
 
 	    return distance;
 	}
 }
