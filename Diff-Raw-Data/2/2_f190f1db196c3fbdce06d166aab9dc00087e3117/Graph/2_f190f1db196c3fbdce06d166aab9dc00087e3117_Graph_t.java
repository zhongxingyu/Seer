 /*
 This file is part of leafdigital browserstats.
 
 browserstats is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 browserstats is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with browserstats.  If not, see <http://www.gnu.org/licenses/>.
 
 Copyright 2010 Samuel Marshall.
 */
 package com.leafdigital.browserstats.graph;
 
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 import java.util.regex.*;
 
 import com.leafdigital.browserstats.shared.*;
 
 /** Draws a graph based on .summary files. */
 public class Graph extends CommandLineTool
 {
 	private boolean overwrite, stdout, png, svg, csv,
 		startLabels = true, endLabels = true;
 	private boolean csvPercentage = true;
 	private Color background = Color.WHITE, foreground = Color.BLACK;
 	private File folder;
 	private int width=800, height=600, labelSize = 12, footnoteSize = 9,
 		titleSize = 18;
 	private String category, prefix, fontName, title;
 
 	/**
 	 * @param args Command-line arguments
 	 */
 	public static void main(String[] args)
 	{
 		// Set headless in case we're running on a server
 		System.setProperty("java.awt.headless", "true");
 
 		(new Graph()).run(args);
 	}
 
 	@Override
 	protected int processArg(String[] args, int i)
 		throws IllegalArgumentException
 	{
 		if(args[i].equals("-overwrite"))
 		{
 			overwrite = true;
 			return 1;
 		}
 		if(args[i].equals("-folder"))
 		{
 			checkArgs(args, i, 1);
 			folder = new File(args[i+1]);
 			if(!folder.exists() || !folder.isDirectory())
 			{
 				throw new IllegalArgumentException("Folder does not exist: " + folder);
 			}
 			return 2;
 		}
 		if(args[i].equals("-stdout"))
 		{
 			stdout = true;
 			return 1;
 		}
 		if(args[i].equals("-size"))
 		{
 			checkArgs(args, i, 2);
 			try
 			{
 				width = Integer.parseInt(args[i+1]);
 				height = Integer.parseInt(args[i+2]);
 			}
 			catch(NumberFormatException e)
 			{
 				throw new IllegalArgumentException("Invalid size (format: -size 800 600)");
 			}
 			if(width<200 || height < 100 || width > 16000 || height > 16000)
 			{
 				throw new IllegalArgumentException("Invalid size (out of range)");
 			}
 			return 3;
 		}
 		if(args[i].equals("-format"))
 		{
 			checkArgs(args, i, 1);
 			String format = args[i+1];
 			if(format.equals("png"))
 			{
 				png = true;
 			}
 			else if(format.equals("svg"))
 			{
 				svg = true;
 			}
 			else if(format.equals("csv"))
 			{
 				csv = true;
 			}
 			else
 			{
 				throw new IllegalArgumentException("-format unknown: " + format);
 			}
 			return 2;
 		}
 		if(args[i].equals("-csv"))
 		{
 			checkArgs(args, i, 1);
 			String type = args[i+1];
 			csv = true;
 			if(type.equals("percentage"))
 			{
 				csvPercentage = true;
 			}
 			else if(type.equals("count"))
 			{
 				csvPercentage = false;
 			}
 			else
 			{
 				throw new IllegalArgumentException("-csv unknown: " + csv);
 			}
 			return 2;
 		}
 		if(args[i].equals("-labels"))
 		{
 			checkArgs(args, i, 1);
 			String labels = args[i+1];
 			if(labels.equals("both"))
 			{
 				startLabels = true;
 				endLabels = true;
 			}
 			else if(labels.equals("none"))
 			{
 				startLabels = false;
 				endLabels = false;
 			}
 			else if(labels.equals("start"))
 			{
 				startLabels = true;
 				endLabels = false;
 			}
 			else if(labels.equals("end"))
 			{
 				startLabels = false;
 				endLabels = true;
 			}
 			else
 			{
 				throw new IllegalArgumentException("-labels unknown: " + labels);
 			}
 			return 2;
 		}
 		if(args[i].equals("-category"))
 		{
 			checkArgs(args, i, 1);
 			category = args[i+1];
 			return 2;
 		}
 		if(args[i].equals("-background"))
 		{
 			checkArgs(args, i, 1);
 			background = ColorUtils.fromString(args[i+1]);
 			return 2;
 		}
 		if(args[i].equals("-foreground"))
 		{
 			checkArgs(args, i, 1);
 			foreground = ColorUtils.fromString(args[i+1]);
 			return 2;
 		}
 		if(args[i].equals("-font"))
 		{
 			checkArgs(args, i, 1);
 			fontName = args[i+1];
 			boolean ok = false;
 			for(String available : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
 			{
 				if(available.equals(fontName))
 				{
 					ok = true;
 					break;
 				}
 			}
 			if(!ok)
 			{
 				throw new IllegalArgumentException("Couldn't find font: " + fontName);
 			}
 			return 2;
 		}
 		if(args[i].equals("-labelsize"))
 		{
 			labelSize = getSizeParameter(args, i);
 			return 2;
 		}
 		if(args[i].equals("-footnotesize"))
 		{
 			footnoteSize = getSizeParameter(args, i);
 			return 2;
 		}
 		if(args[i].equals("-titlesize"))
 		{
 			titleSize = getSizeParameter(args, i);
 			return 2;
 		}
 		if(args[i].equals("-prefix"))
 		{
 			checkArgs(args, i, 1);
 			prefix = args[i+1];
 			return 2;
 		}
 		if(args[i].equals("-title"))
 		{
 			checkArgs(args, i, 1);
 			title = args[i+1];
 			return 2;
 		}
 		return 0;
 	}
 
 	private int getSizeParameter(String[] args, int i)
 		throws IllegalArgumentException
 	{
 		checkArgs(args, i, 1);
 		try
 		{
 			int size = Integer.parseInt(args[i+1]);
 			if(size<1 || size > 999)
 			{
 				throw new IllegalArgumentException("Size out of range: " + args[i+1]);
 			}
 			return size;
 		}
 		catch(NumberFormatException e)
 		{
 			throw new IllegalArgumentException("Invalid size: " + args[i+1]);
 		}
 	}
 
 	/** @return True if user is on Mac */
 	public static boolean isMac()
 	{
 		// Code from http://developer.apple.com/technotes/tn2002/tn2110.html
 		String lcOSName=System.getProperty("os.name").toLowerCase();
 		return lcOSName.startsWith("mac os x");
 	}
 
 	@Override
 	protected void validateArgs() throws IllegalArgumentException
 	{
 		if(fontName == null)
 		{
 			if(isMac())
 			{
 				fontName = "Helvetica Neue";
 			}
 			else
 			{
 				fontName = "sans-serif";
 			}
 		}
 
 		// If format not specified, default to PNG
 		if(!(png || svg || csv))
 		{
 			png = true;
 		}
 
 		// If stdout or stdin is set, check only one format is specified
 		if( (stdout || getInputFiles() == null) &&
 			((csv ? 1 : 0) + (png ? 1 : 0) + (svg ? 1 : 0)) != 1)
 		{
 			throw new IllegalArgumentException(
 				"Cannot write multiple formats to stdout");
 		}
 	}
 
 	@Override
 	protected void go()
 	{
 		// Process and check input files
 		LinkedList<InputFile> inputFiles = new LinkedList<InputFile>();
 		File current = null;
 		try
 		{
 			File[] input = getInputFiles();
 			if(input == null)
 			{
 				inputFiles.add(new InputFile(null, category));
 			}
 			else
 			{
 				for(File file : input)
 				{
 					current = file;
 					inputFiles.add(new InputFile(file, category));
 				}
 			}
 		}
 		catch(IOException e)
 		{
 			String name = current == null ? "stdin" : current.toString();
 			System.err.println(name + ": " + e.getMessage());
 			return;
 		}
 
 		// Set up canvas(es)
 		Canvas[] canvases = new Canvas[png && svg ? 2 : png || svg ? 1 : 0];
 		if(png)
 		{
 			canvases[0] = new PngCanvas(width, height, background);
 		}
 		if(svg)
 		{
 			canvases[png ? 1 : 0] = new SvgCanvas(width, height, background);
 		}
 
 		// Store CSV data
 		String csvData = null;
 
 		try
 		{
 			// Different paths for one or multiple
 			InputFile lastInputFile;
 			if(inputFiles.size() == 1)
 			{
 				lastInputFile = inputFiles.getFirst();
 				updateLastDetails(lastInputFile);
 
 				for(Canvas canvas : canvases)
 				{
 					pieChart(lastInputFile, canvas);
 				}
 				if(csv)
 				{
 					csvData = singleCsv(lastInputFile);
 				}
 			}
 			else
 			{
 				// Check each file has a date
 				for(InputFile file : inputFiles)
 				{
 					if(file.getDate() == null)
 					{
 						throw new IOException(file.getName()
 							+ ": cannot determine date from filename");
 					}
 				}
 
 				// Sort files into order
 				InputFile[] inputFilesSorted =
 					inputFiles.toArray(new InputFile[inputFiles.size()]);
 				Arrays.sort(inputFilesSorted);
 				lastInputFile = inputFilesSorted[inputFilesSorted.length - 1];
 				updateLastDetails(lastInputFile);
 
 				// Do trend chart
 				for(Canvas canvas : canvases)
 				{
 					trendChart(inputFilesSorted, canvas);
 				}
 				if(csv)
 				{
 					csvData = trendCsv(inputFilesSorted);
 				}
 			}
 
 			// Save output
 			if(stdout || getInputFiles() == null)
 			{
 				if(canvases.length != 0)
 				{
 					System.out.write(canvases[0].save());
 				}
 				if(csv)
 				{
 					System.out.write(csvData.getBytes("UTF-8"));
 				}
 			}
 			else
 			{
 				for(Canvas canvas : canvases)
 				{
 					File file = new File(folder, prefix + canvas.getExtension());
 					if(file.exists() && !overwrite)
 					{
 						throw new IOException("File exists (use -overwrite): " + file);
 					}
 					FileOutputStream out = new FileOutputStream(file);
 					out.write(canvas.save());
 					out.close();
 				}
 				if(csv)
 				{
 					File file = new File(folder, prefix + ".csv");
 					if(file.exists() && !overwrite)
 					{
 						throw new IOException("File exists (use -overwrite): " + file);
 					}
 					FileOutputStream out = new FileOutputStream(file);
 					out.write(csvData.getBytes("UTF-8"));
 					out.close();
 				}
 			}
 		}
 		catch(IOException e)
 		{
 			System.err.println(e.getMessage());
 		}
 	}
 
 	private void updateLastDetails(InputFile lastInputFile)
 	{
 		if(prefix == null)
 		{
 			prefix = lastInputFile.getName().replaceAll("\\.summary$", "");
 		}
 		if(title == null)
 		{
 			title = prefix;
 		}
 		if(folder == null && lastInputFile.getFile() != null)
 		{
 			folder = lastInputFile.getFile().getParentFile();
 		}
 	}
 
 	private void pieChart(InputFile file, Canvas canvas)
 	{
 		// METRICS
 		//////////
 
 		int mTitleBottomPadding = 10, mLegendLeftPadding = 20,
 			mLegendEntryBottomPadding = 10, mLegendEntryRightPadding = 20,
 			mLegendRowPadding = 10, mLegendTopPadding = 20,
 			mSliceLabelPadding = 10;
 
 		// LAYOUT
 		/////////
 
 		// Do title and calculate height
 		double titleSpace = 0;
 		if(!title.isEmpty())
 		{
 			TextDrawable titleLabel = new TextDrawable(title, 0, 0, foreground, fontName, false, titleSize);
 			titleLabel.setVerticalTop(0);
 			canvas.add(titleLabel);
 			titleSpace = titleLabel.getHeight() + mTitleBottomPadding;
 		}
 
 		// Get all group names except excluded, also count total
 		int total = 0;
 		String[] allGroupNames = file.getGroupNames();
 		LinkedList<String> groupNamesList = new LinkedList<String>();
 		for(String groupName : allGroupNames)
 		{
 			if(SpecialNames.GROUP_EXCLUDED.equals(groupName))
 			{
 				continue;
 			}
 			groupNamesList.add(groupName);
 			total += file.getCount(groupName);
 		}
 		String[] groupNames =
 			groupNamesList.toArray(new String[groupNamesList.size()]);
 
 		// Set up colours
 		fillGroupColours(groupNames);
 
 		// Work out current graph position (before legend)
 		double graphX = 0, graphY = titleSpace,
 			graphWidth = canvas.getWidth(), graphHeight = canvas.getHeight() - graphY;
 
 		// Work out legend entries
 		LegendEntry[] legendEntries = new LegendEntry[groupNames.length];
 		for(int group=0; group<groupNames.length; group++)
 		{
 			String groupName = groupNames[group];
 			GroupColours groupColour = groupColours.get(groupName);
 			legendEntries[group] = new LegendEntry(group, groupColour.getMain(), groupName,
 				TrendData.getPercentageString(file.getCount(groupName), total),
 				fontName, foreground, labelSize);
 		}
 
 		// Draw legend to right or below?
 		if(graphWidth > graphHeight)
 		{
 			// Legend to right
 
 			// Get max width
 			double maxLegendWidth = 0;
 			for(LegendEntry entry : legendEntries)
 			{
 				maxLegendWidth = Math.max(maxLegendWidth, entry.getWidth());
 			}
 
 			// Update metrics
 			double legendX = graphX + graphWidth - maxLegendWidth;
 			graphWidth -= maxLegendWidth - mLegendLeftPadding;
 
 			// Draw legend entries
 			double legendY = graphY;
 			for(LegendEntry entry : legendEntries)
 			{
 				entry.move(legendX, legendY);
 				entry.addTo(canvas);
 				legendY += entry.getHeight() + mLegendEntryBottomPadding;
 			}
 		}
 		else
 		{
 			// Legend below
 
 			// First flow to count lines and get max height
 			double available = graphWidth;
 			int lines = 1;
 			double maxHeight = 0;
 			for(LegendEntry entry : legendEntries)
 			{
 				// Note: All heights should be the same really (or it will look silly)
 				// so this maximum counting is not really necessary, but just in case.
 				maxHeight = Math.max(maxHeight, entry.getHeight());
 				available -= entry.getWidth() + mLegendEntryRightPadding;
 				if(available < 0)
 				{
 					lines++;
 					available = graphWidth - entry.getWidth();
 				}
 			}
 
 			// Calculate height
 			double legendHeight = maxHeight * lines + mLegendRowPadding * (lines-1);
 			graphHeight -= legendHeight + mLegendTopPadding;
 
 			// Second flow to actually draw legend
 			double legendY = graphY + graphHeight + mLegendTopPadding;
 			double legendX = 0;
 			for(LegendEntry entry : legendEntries)
 			{
 				if(legendX + entry.getWidth() > graphWidth)
 				{
 					legendX = 0;
 					legendY += maxHeight + mLegendRowPadding;
 				}
 
 				entry.move(legendX, legendY);
 				entry.addTo(canvas);
 				legendX += entry.getWidth() + mLegendEntryRightPadding;
 			}
 		}
 
 		// Draw each group
 		double
 			middleX = graphX + graphWidth / 2.0,
 			middleY = graphY + graphHeight / 2.0,
 			radius = Math.min(graphWidth, graphHeight) / 2.0;
 		double angle = 0;
 		List<PieSliceDrawable> sliceList = new LinkedList<PieSliceDrawable>();
 		List<TextDrawable> labelList = new LinkedList<TextDrawable>();
 		for(int i=0; i<groupNames.length; i++)
 		{
 			// Calculate the pie slice.
 			String groupName = groupNames[i];
 			double count = file.getCount(groupName);
 			double degrees = (count / total) * 360.0;
 			GroupColours groupColour = groupColours.get(groupName);
 			PieSliceDrawable slice = new PieSliceDrawable(middleX, middleY, radius,
 				angle, degrees,	groupColour.getMain());
 			sliceList.add(slice);
 
 			// We don't actually draw the slice yet, only the borders for overprinting
 			if(i == 0)
 			{
 				canvas.add(slice.getOverprintRadius(false, 2.0));
 			}
 			if(i < groupNames.length - 1)
 			{
 				canvas.add(slice.getOverprintRadius(true, 2.0));
 			}
 
 			// Calculate the space for a label
 			TextDrawable name = new TextDrawable(groupName, 0, 0, groupColour.getText(),
 				fontName, true, labelSize);
 			Point2D labelPoint = slice.getLabelPosition(
 				name.getWidth(), name.getHeight(), mSliceLabelPadding);
 			if(labelPoint != null)
 			{
 				name.move(labelPoint.getX(),
 					labelPoint.getY() + name.getAscent());
 				labelList.add(name);
 			}
 
 			angle += degrees;
 		}
 
 		// Now draw the actual slices
 		for(PieSliceDrawable slice : sliceList)
 		{
 			canvas.add(slice);
 		}
 
 		// And draw the labels
 		for(TextDrawable label : labelList)
 		{
 			canvas.add(label);
 		}
 	}
 
 	private void trendChart(InputFile[] files, Canvas canvas)
 	{
 		// Get data
 		TrendData data = new TrendData(files);
 		String[] groupNames = data.getGroupNames();
 		fillGroupColours(data.getGroupNames());
 
 		// METRICS
 		//////////
 
 		int mLabelSidePadding = 10, mLargeCurveWidth = 20, mTitleBottomPadding = 10,
 			mLargeCurvePadding = 10, mFootnoteMargin = 20, mFootnoteVerticalSpacing = 10,
 			mDateMargin = 20, mDateTopPadding = 5, mLabelVerticalPadding = 4;
 		double mOverprint = 1;
 
 		// LAYOUT
 		/////////
 
 		// Do title and calculate height
 		TextDrawable titleLabel = new TextDrawable(title, 0, 0, foreground, fontName, false, titleSize);
 		titleLabel.setVerticalTop(0);
 		canvas.add(titleLabel);
 		double titleHeight = titleLabel.getHeight();
 		double graphY = titleHeight + mTitleBottomPadding;
 
 		// Find all end labels and calculate width
 		LinkedList<Label> endLabelsList = new LinkedList<Label>();
 		int numPoints = data.getNumPoints();
 		int lastPoint = numPoints - 1;
 		double endLabelsWidth = 0;
 		for(int group=0; group<data.getNumGroups(); group++)
 		{
 			if(data.getValue(group, lastPoint) > 0)
 			{
 				GroupColours colours = groupColours.get(groupNames[group]);
 				Label label = new Label(group, groupNames[group],
 					data.getPercentage(group, lastPoint), fontName,
 						colours.getText(), labelSize, mLabelVerticalPadding);
 				endLabelsList.add(label);
 				endLabelsWidth = Math.max(endLabelsWidth, label.getWidth());
 			}
 		}
 		Label[] endLabelsArray =
 			endLabelsList.toArray(new Label[endLabelsList.size()]);
 		endLabelsWidth += mLabelSidePadding * 2 + mLargeCurveWidth
 			+ mLargeCurvePadding;
 
 		// Find all start labels and calculate width
 		LinkedList<Label> startLabelsList = new LinkedList<Label>();
 		double startLabelsMinWidth = 0;
 		for(int group=0; group<data.getNumGroups(); group++)
 		{
 			if(data.getValue(group, 0) > 0)
 			{
 				GroupColours colours = groupColours.get(groupNames[group]);
 				Label label = new Label(group, groupNames[group],
					data.getPercentage(group, 0), fontName,
 					colours.getText(), labelSize, mLabelVerticalPadding);
 				startLabelsList.add(label);
 				startLabelsMinWidth = Math.max(startLabelsMinWidth, label.getWidth());
 			}
 		}
 		Label[] startLabelsArray =
 			startLabelsList.toArray(new Label[startLabelsList.size()]);
 		double startLabelsWidth = startLabelsMinWidth + mLabelSidePadding * 2
 			+ mLargeCurveWidth + mLargeCurvePadding;
 		double graphX = startLabels ? startLabelsWidth : 0;
 
 		// We now know the width of the graph
 		double graphWidth = canvas.getWidth() - graphX
 			- (endLabels ? endLabelsWidth : 0);
 
 		// Find all footnotes - these are groups which are not present in either
 		// the start or end of the graph.
 		int footnoteNumber = 1;
 		LinkedList<Footnote> footnotesList = new LinkedList<Footnote>();
 		for(int group=0; group<data.getNumGroups(); group++)
 		{
 			if(data.getValue(group, 0) == 0 && data.getValue(group, lastPoint) == 0)
 			{
 				GroupColours colours = groupColours.get(groupNames[group]);
 				Footnote footnote = new Footnote(group,
 					new TextDrawable(groupNames[group], 0, 0,
 						foreground, fontName, false, footnoteSize),
 					new TextDrawable(footnoteNumber + "", 0, 0,
 						colours.getText(), fontName, false, footnoteSize),
 					new TextDrawable(footnoteNumber + "", 0, 0,
 						colours.getText(), fontName, false, footnoteSize));
 				footnotesList.add(footnote);
 				footnoteNumber++;
 			}
 		}
 
 		// Calculate footnote wrapping.
 		// Note that line 0 is the date line, we don't put footnotes on there
 		// now, so it will always be skipped.
 		double lineAvailable = 0;
 		double x = 0;
 		int lineIndex = 0;
 		double rowHeight = 0;
 		for(Footnote footnote : footnotesList)
 		{
 			if(footnote.getWidth() > lineAvailable)
 			{
 				lineIndex++;
 				x = 0;
 				lineAvailable = canvas.getWidth() - (endLabels ? endLabelsWidth : 0);
 			}
 
 			footnote.setRoughPosition(x, lineIndex);
 			double width = footnote.getWidth() + mFootnoteMargin;
 			x += width;
 			lineAvailable -= width;
 			if(rowHeight == 0)
 			{
 				rowHeight = footnote.getHeight();
 			}
 		}
 		double footnotesHeight = (rowHeight * (lineIndex+1)) + (lineIndex * mFootnoteVerticalSpacing);
 
 		// If there are no footnotes, calculate height using the date row only
 		TextDrawable[] dateText = new TextDrawable[numPoints];
 		double dateHeight = 0;
 		for(int i=0; i<numPoints; i++)
 		{
 			dateText[i] = new TextDrawable(data.getPointDate(i), 0, 0,
 				foreground, fontName, false, footnoteSize);
 			dateHeight = Math.max(dateHeight, dateText[i].getHeight());
 		}
 
 		// This is the height of the bottom area below the graph
 		double bottomAreaHeight = Math.max(footnotesHeight, dateHeight);
 
 		// And this is the height of the main graph area
 		double graphHeight = canvas.getHeight() - bottomAreaHeight -
 			titleHeight - mTitleBottomPadding - mDateTopPadding;
 
 		// Work out the main graph positions for each point based on the timeline
 		int
 			minDate = data.getPointLinearDateStart(0),
 			maxDate = data.getPointLinearDateEnd(numPoints - 1);
 		double dateScale = graphWidth / (double)(maxDate - minDate);
 		PointPosition[] pointPositions = new PointPosition[numPoints];
 		for(int i=0; i<pointPositions.length; i++)
 		{
 			double start = (data.getPointLinearDateStart(i) - minDate) * dateScale;
 			double end = (data.getPointLinearDateEnd(i) - minDate) * dateScale;
 			pointPositions[i] = new PointPosition(start, end,
 				i == pointPositions.length - 1);
 		}
 
 		// Position last date
 		double dateY = canvas.getHeight() - bottomAreaHeight;
 		double dateTextY = dateY + dateText[0].getAscent();
 		double lastDateWidth = dateText[dateText.length-1].getWidth();
 		double lastDateMiddle = pointPositions[numPoints-1].getMiddle();
 		double lastDateX;
 		if(lastDateMiddle + (lastDateWidth/2) > graphWidth)
 		{
 			lastDateX = graphWidth - lastDateWidth;
 		}
 		else
 		{
 			lastDateX = lastDateMiddle - lastDateWidth/2;
 		}
 		dateText[dateText.length-1].move(lastDateX + graphX, dateTextY);
 
 		// Position first date, unless it overlaps
 		double firstDateWidth = dateText[0].getWidth();
 		double firstDateMiddle = pointPositions[0].getMiddle();
 		double firstDateX;
 		if(firstDateMiddle - (firstDateWidth/2) < 0)
 		{
 			firstDateX = 0;
 		}
 		else
 		{
 			firstDateX = firstDateMiddle - firstDateWidth/2;
 		}
 		if(firstDateX + firstDateWidth >= lastDateX - mDateMargin)
 		{
 			dateText[0] = null;
 		}
 		else
 		{
 			dateText[0].move(firstDateX + graphX, dateTextY);
 		}
 
 		// Position other dates when there is room
 		double nextAvailableX = firstDateX + firstDateWidth + mDateMargin;
 		for(int i=1; i<numPoints-1; i++)
 		{
 			double dateWidth = dateText[i].getWidth();
 			double dateMiddle = pointPositions[i].getMiddle();
 			double dateX = dateMiddle - (dateWidth/2);
 			if(dateX > nextAvailableX && dateX + dateWidth < lastDateX - mDateMargin)
 			{
 				dateText[i].move(dateX + graphX, dateTextY);
 				nextAvailableX = dateX + dateWidth + mDateMargin;
 			}
 			else
 			{
 				dateText[i] = null;
 			}
 		}
 
 		// Actually draw dates
 		for(int i=0; i<numPoints; i++)
 		{
 			if(dateText[i] != null)
 			{
 				canvas.add(dateText[i]);
 			}
 		}
 
 		// Position, move, and draw labels
 		if(startLabels)
 		{
 			// Position
 			Label.distributeLabelsVertically(data, 0, mLabelSidePadding, graphY, graphHeight,
 				startLabelsArray);
 
 			// Draw
 			double realAboveY = graphY;
 			for(int i=0; i<startLabelsArray.length; i++)
 			{
 				Label label = startLabelsArray[i];
 				double aboveY = label.getY();
 				Color colour = groupColours.get(groupNames[label.getGroup()]).getMain();
 
 				// Shape in background
 				ShapeDrawable shape = new ShapeDrawable(0, aboveY, colour);
 				double endX = startLabelsMinWidth + mLabelSidePadding*2;
 				shape.lineTo(endX, aboveY);
 				shape.flatCurveTo(endX + mLargeCurveWidth, realAboveY);
 				double belowY = aboveY + label.getAllocatedHeight();
 				double realBelowY = ((double)data.getValue(label.getGroup(), 0)
 					* graphHeight / data.getTotal(0)) + realAboveY;
 				shape.lineTo(endX + mLargeCurveWidth, realBelowY);
 				shape.flatCurveTo(endX, belowY);
 				shape.lineTo(0, belowY);
 				shape.finish();
 				canvas.add(shape);
 
 				// Line to overprint bottom
 				if(i != startLabelsArray.length-1)
 				{
 					LineDrawable line = new LineDrawable(0, belowY, mOverprint, colour);
 					line.lineTo(endX, belowY);
 					line.flatCurveTo(endX + mLargeCurveWidth, realBelowY);
 					line.finish();
 					canvas.add(line);
 				}
 
 				// Label
 				label.addTo(canvas);
 
 				realAboveY = realBelowY;
 			}
 		}
 
 		if(endLabels)
 		{
 			// Position
 			Label.distributeLabelsVertically(data, numPoints-1,
 				graphX + graphWidth + mLargeCurvePadding + mLargeCurveWidth +
 				mLabelSidePadding, 0, canvas.getHeight(),	endLabelsArray);
 
 			// Draw
 			double realAboveY = graphY;
 			for(int i=0; i<endLabelsArray.length; i++)
 			{
 				Label label = endLabelsArray[i];
 				double startX = graphX + graphWidth + mLargeCurvePadding;
 				Color colour = groupColours.get(groupNames[label.getGroup()]).getMain();
 
 				// Shape in background
 				ShapeDrawable shape = new ShapeDrawable(startX, realAboveY, colour);
 				double aboveY = label.getY();
 				shape.flatCurveTo(startX + mLargeCurveWidth, aboveY);
 				shape.lineTo(canvas.getWidth(), aboveY);
 				double belowY = aboveY + label.getAllocatedHeight();
 				double overprint = belowY > graphY + graphHeight - mOverprint ? 0 : mOverprint;
 				shape.lineTo(canvas.getWidth(), belowY + overprint);
 				shape.lineTo(startX + mLargeCurveWidth, belowY + overprint);
 				double realBelowY = ((double)data.getValue(label.getGroup(), numPoints-1)
 					* graphHeight / data.getTotal(numPoints-1)) + realAboveY;
 				shape.flatCurveTo(startX, realBelowY + overprint);
 				shape.finish();
 				canvas.add(shape);
 
 				// Line to overprint bottom
 				if(i != startLabelsArray.length-1)
 				{
 					LineDrawable line = new LineDrawable(startX, realBelowY, mOverprint, colour);
 					line.flatCurveTo(startX + mLargeCurveWidth, belowY);
 					line.lineTo(canvas.getWidth(), belowY);
 					line.finish();
 					canvas.add(line);
 				}
 
 				// Label
 				label.addTo(canvas);
 
 				realAboveY = realBelowY;
 			}
 		}
 
 		// Draw actual graph
 		for(int group=0; group<data.getNumGroups(); group++)
 		{
 			// See if there's a footnote for this group
 			Footnote footnote = null;
 			for(Footnote possible : footnotesList)
 			{
 				if(possible.getGroup() == group)
 				{
 					footnote = possible;
 					break;
 				}
 			}
 			GraphWorm shape = null;
 			for(int point=0; point<numPoints; point++)
 			{
 				PointPosition position = pointPositions[point];
 				PointPosition lastPosition = point==0 ? null : pointPositions[point-1];
 				double total = (double)data.getTotal(point);
 				double value = (double)data.getValue(group, point);
 				double above = (double)data.getCumulativeValueBefore(group, point);
 				double aboveY = (above * graphHeight / total) + graphY;
 				double belowY = (value * graphHeight / total) + aboveY;
 
 				if(value != 0)
 				{
 					if(shape == null)
 					{
 						Color color = groupColours.get(groupNames[group]).getMain();
 						shape = new GraphWorm(position.start + graphX, aboveY, belowY,
 							color, group==data.getNumGroups()-1);
 					}
 					// Draw curve from last to this
 					if(lastPosition != null && lastPosition.hasCurve())
 					{
 						shape.makeCurve(position.start + graphX, aboveY, belowY);
 					}
 					// Draw straight
 					shape.makeStraight(
 						(position.hasCurve()
 							? position.end - PointPosition.POINT_CURVE_SIZE
 							: position.end) + graphX,
 						aboveY, belowY);
 				}
 				else if(shape != null)
 				{
 					finishShape(canvas, footnote, shape);
 					shape = null;
 				}
 			}
 
 			if(shape != null)
 			{
 				finishShape(canvas, footnote, shape);
 				shape = null;
 			}
 		}
 
 		// Footnotes
 		for(Footnote footnote : footnotesList)
 		{
 			footnote.addTo(canvas, dateY, rowHeight + mFootnoteVerticalSpacing,
 				groupNames, groupColours, foreground);
 		}
 	}
 
 	/**
 	 * Finishes off a graph shape.
 	 * @param canvas Canvas
 	 * @param footnote Footnote to receive position
 	 * @param shape Shape to finish
 	 */
 	private void finishShape(Canvas canvas, Footnote footnote,
 		GraphWorm shape)
 	{
 		// Draw shape
 		shape.addTo(canvas);
 		// Add footnote
 		if(footnote != null)
 		{
 			footnote.setGraphPosition(shape.getFootnoteX(),
 				shape.getFootnoteY());
 		}
 	}
 
 	private String singleCsv(InputFile file)
 	{
 		StringBuilder out = new StringBuilder();
 		if(!title.isEmpty())
 		{
 			out.append(title);
 			out.append("\n\n");
 		}
 		out.append(","); // Empty cell
 		if(csvPercentage)
 		{
 			out.append("%\n");
 		}
 		else
 		{
 			out.append("Count\n");
 		}
 
 		// Get all group names except excluded, also count total
 		int total = 0;
 		String[] allGroupNames = file.getGroupNames();
 		LinkedList<String> groupNamesList = new LinkedList<String>();
 		for(String groupName : allGroupNames)
 		{
 			if(SpecialNames.GROUP_EXCLUDED.equals(groupName))
 			{
 				continue;
 			}
 			groupNamesList.add(groupName);
 			total += file.getCount(groupName);
 		}
 		String[] groupNames =
 			groupNamesList.toArray(new String[groupNamesList.size()]);
 
 		// Output results for each group
 		for(String groupName : groupNames)
 		{
 			out.append(groupName);
 			out.append(",");
 			int count = file.getCount(groupName);
 			if(csvPercentage)
 			{
 				out.append(TrendData.getPercentageString(count, total));
 			}
 			else
 			{
 				out.append(count);
 			}
 			out.append("\n");
 		}
 
 		return out.toString();
 	}
 
 	private String trendCsv(InputFile[] files)
 	{
 		StringBuilder out = new StringBuilder();
 		if(!title.isEmpty())
 		{
 			out.append(title);
 			out.append("\n\n");
 		}
 
 		TrendData data = new TrendData(files);
 		String[] groupNames = data.getGroupNames();
 
 		// Do header row, starting with empty cell
 		for(int point=0; point<data.getNumPoints(); point++)
 		{
 			out.append(',');
 			out.append(data.getPointDate(point));
 		}
 		out.append('\n');
 
 		// Do group rows
 		for(int group=0; group<data.getNumGroups(); group++)
 		{
 			out.append(groupNames[group]);
 
 			for(int point=0; point<data.getNumPoints(); point++)
 			{
 				out.append(',');
 				int count = data.getValue(group, point);
 				int total = data.getTotal(point);
 				if(csvPercentage)
 				{
 					out.append(TrendData.getPercentageString(count, total));
 				}
 				else
 				{
 					out.append(count);
 				}
 			}
 
 			out.append('\n');
 		}
 
 		return out.toString();
 	}
 
 	private Map<String, GroupColours> groupColours =
 		new HashMap<String, GroupColours>();
 
 	private final static int
 		COLOUR_SATURATION_MAX = 200, COLOUR_SATURATION_MIN = 35,
 		COLOUR_LIGHTNESS_BRIGHT = 230, COLOUR_LIGHTNESS_BRIGHT_MIN = 180,
 		COLOUR_LIGHTNESS_DARK = 60, COLOUR_LIGHTNESS_DARK_MAX = 110,
 		COLOUR_HUE_STEP = 30;
 
 	void fillGroupColours(final String[] groupNames)
 	{
 		// Organise into list of similar name
 		Map<String, List<Integer>> similar = new HashMap<String, List<Integer>>();
 		for(int group = 0; group < groupNames.length; group++)
 		{
 			String base = getNameBase(groupNames[group]);
 			List<Integer> list = similar.get(base);
 			if(list == null)
 			{
 				list = new LinkedList<Integer>();
 				similar.put(base, list);
 			}
 			list.add(group);
 		}
 
 		// Now go through in original order
 		int hue = 0;
 		boolean light = true;
 		for(int group = 0; group < groupNames.length; group++)
 		{
 			String base = getNameBase(groupNames[group]);
 			List<Integer> list = similar.get(base);
 			if(list == null)
 			{
 				// Already done this group
 				continue;
 			}
 
 			// If there's only one, colour it normally
 			if(list.size() == 1)
 			{
 				Color colour = ColorUtils.fromHsl(hue, COLOUR_SATURATION_MAX,
 					light ? COLOUR_LIGHTNESS_BRIGHT : COLOUR_LIGHTNESS_DARK);
 				if(!groupColours.containsKey(groupNames[group]))
 				{
 					groupColours.put(groupNames[group],
 						new GroupColours(colour, light ? Color.BLACK : Color.WHITE));
 				}
 			}
 			else
 			{
 				// Sort list into order
 				Collections.sort(list, new Comparator<Integer>()
 				{
 					@Override
 					public int compare(Integer o1, Integer o2)
 					{
 						// Work out double value for each group.
 						return (int)Math.signum(getGroupNumericOrder(o1, groupNames[o1])
 							- getGroupNumericOrder(o2, groupNames[o2]));
 					}
 				});
 
 				// Now assign each colour according to evenly divided-out saturations
 				for(int i=0; i<list.size(); i++)
 				{
 					int targetGroup = list.get(i);
 					float proportion = (float)i / (float)(list.size()-1);
 					int saturation = Math.round(COLOUR_SATURATION_MIN +
 						(COLOUR_SATURATION_MAX - COLOUR_SATURATION_MIN) * proportion);
 					int lightness;
 					if(light)
 					{
 						lightness = Math.round(COLOUR_LIGHTNESS_BRIGHT_MIN
 							+ (COLOUR_LIGHTNESS_BRIGHT - COLOUR_LIGHTNESS_BRIGHT_MIN) * (proportion));
 					}
 					else
 					{
 						lightness = Math.round(COLOUR_LIGHTNESS_DARK
 							+ (COLOUR_LIGHTNESS_DARK_MAX - COLOUR_LIGHTNESS_DARK) * (1.0f - proportion));
 					}
 					Color colour = ColorUtils.fromHsl(hue, saturation, lightness);
 					if(!groupColours.containsKey(groupNames[targetGroup]))
 					{
 						groupColours.put(groupNames[targetGroup],
 							new GroupColours(colour, light ? Color.BLACK : Color.WHITE));
 					}
 				}
 			}
 
 			// Mark it done
 			similar.remove(base);
 
 			// Next colour index
 			hue += COLOUR_HUE_STEP;
 			if(hue >= 360)
 			{
 				hue -= 360;
 			}
 			light = !light;
 		}
 	}
 
 	private final static Pattern NUMBER_REGEX =
 		Pattern.compile("[0-9]+(\\.[0-9]+)?");
 
 	private static double getGroupNumericOrder(int index, String name)
 	{
 		// See if we can find a number (possibly decimal) in the name
 		Matcher m = NUMBER_REGEX.matcher(name);
 		if(m.find())
 		{
 			double value = Double.parseDouble(m.group());
 
 			// Use index as a tie-breaker by subtracting a miniscule proportion of it
 			return value - 0.0000001 * (double)index;
 		}
 
 		// No number, use negative index (ensures that the first one is brightest)
 		return -(index+1);
 	}
 
 	/**
 	 * Get base name used for identifying similar groups. This is the name up to
 	 * the first characters that is not a letter or whitespace.
 	 * @param name Full name
 	 * @return Base name
 	 */
 	private static String getNameBase(String name)
 	{
 		for(int i=0; i<name.length(); i++)
 		{
 			char c = name.charAt(i);
 			if(!(Character.isLetter(c) || Character.isWhitespace(c)))
 			{
 				if(i==0)
 				{
 					// Don't try to do similarity if the name *starts* with one of these
 					return name;
 				}
 				return name.substring(0, i);
 			}
 		}
 		return name;
 	}
 }
