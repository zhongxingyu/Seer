 package skilltimeline.core;
 
 import java.util.Calendar;
 import java.util.Collection;
 
 import skilltimeline.graph.GraphObject;
 import skilltimeline.graph.GraphObject.GCanvas;
 import skilltimeline.graph.GraphObject.GLabel;
 import skilltimeline.graph.GraphObject.GLine;
 import skilltimeline.graph.GraphObject.GRectangle;
 import skilltimeline.graph.GraphObject.Position;
 
 public class GraphBuilder {
 
 	public static GraphObject buildGraph(Collection<SkillEntry> entries) {
 		// Find extents of timeline and pad them on both sides by a few months
 		TimeRange extents = findExtents(entries);
 		Calendar ext_start = Calendar.getInstance(), ext_end = Calendar.getInstance();
 		ext_start.setTimeInMillis(extents.start);
 		ext_start.add(Calendar.MONTH, -3);
 		ext_end.setTimeInMillis(extents.end);
 		ext_end.add(Calendar.MONTH, 3);
 
 		// Support class to calculate times-to-pixels conversions
 		final TimelineData timeline = new TimelineData(ext_start.getTimeInMillis(), ext_end.getTimeInMillis(), Settings.TimelineWidthPixels);
 
		// Estimate the appropriate text width
		final int TEXTWIDTH = estimateLabelWidth(findLongestLabel(entries), Settings.SkillFontSize);
 
 		final int TIMELINEHEIGHT = entries.size() * Settings.SkillLineHeight;
 		GraphObject canvas = new GCanvas(null);
 
 		// Build vertical time indicators
 		for (int year = ext_start.get(Calendar.YEAR) + 1; year <= ext_end.get(Calendar.YEAR); year++) {
 			Calendar c = Calendar.getInstance();
 			c.set(year, 0, 1, 0, 0, 0);
 			int line_x = timeline.convertTimeToPixel(c.getTimeInMillis()) + TEXTWIDTH;
 			GLine gline = new GLine(canvas);
 			gline
 				.strokewidth(1)
 				.size(new Position(0, TIMELINEHEIGHT + 30))
 				.strokeColour(Settings.YearLineColour)
 				.position(new Position(line_x, 10));
 
 			int year_fontSize = (int) ((year % 5 == 0) ? (Settings.SkillFontSize * 1.5) : (Settings.SkillFontSize));
 			String year_colour = (year % 5 == 0) ? Settings.YearMajorColour : Settings.YearMinorColour;
 			GLabel lblYear = new GLabel(canvas);
 			lblYear
 				.text(String.valueOf(year))
 				.font(Settings.YearFontFace)
 				.fontsize(year_fontSize)
 				.fillColour(year_colour)
 				.position(new Position(line_x, TIMELINEHEIGHT + 40));
 		}
 
 		// Build timelines
 		int y_off = 30; // init first entry to 30px from the top
 		for (SkillEntry se : entries) {
 			GLabel lbl = new GLabel(canvas);
 			lbl
 				.text(se.Description)
 				.font(Settings.FontFace)
 				.fontsize(Settings.SkillFontSize)
 				.anchor(GLabel.END) // right-align
 				.fillColour(Settings.SkillFontColor)
 				.position(new Position(TEXTWIDTH, y_off));
 
 			for (TimeRange tr : se.getTimeRanges()) {
 				Position pos, size;
 				pos = new Position(timeline.convertTimeToPixel(tr.start) + TEXTWIDTH, y_off - Settings.SkillFontSize);
 				size = new Position(timeline.convertTimeToPixel(tr.end) - pos.x + TEXTWIDTH, Settings.SkillFontSize);
 				GRectangle rect = new GRectangle(canvas);
 				rect
 					.size(size)
 					.strokeColour(Settings.SkillTimelineColourStroke)
 					.fillColour(Settings.SkillTimelineColourFill)
 					.position(pos);
 			}
 			y_off += Settings.SkillLineHeight;
 		}
 
 		return canvas;
 	}
 
 	private static TimeRange findExtents(Collection<SkillEntry> entries) {
 		long min = Long.MAX_VALUE, max = 0;
 
 		for (SkillEntry se : entries) {
 			for (TimeRange t : se.getTimeRanges()) {
 				if (t.start < min)
 					min = t.start;
 				if (t.end > max)
 					max = t.end;
 			}
 		}
 
 		return new TimeRange(min, max);
 	}
 
	private static int findLongestLabel(Collection<SkillEntry> entries) {
		int max = 0;
		for (SkillEntry se : entries) {
			if (se.Description != null && se.Description.length() > max)
				max = se.Description.length();
		}
		return max;
	}

	private static int estimateLabelWidth(int charLen, int fontSize) {
		// HACK! HACK! HACK!
		// At font size 12, 29 letters takes ABOUT 185 pixels width.
		// Use that to estimate the width of a label.
		
		final double factor = ((double) 185 / (double) 29) / (double) 12;
		return (int) (charLen * fontSize * factor * 1.1);
	}
	
 	/**
 	 * Maintains the extents of the timeline and converts time data to a pixel
 	 * position.
 	 */
 	private static class TimelineData {
 		final long pixelWidth;
 		final long base;
 		final long end;
 
 		public TimelineData(long base, long end, long xscale) {
 			this.base = base;
 			this.end = end;
 			this.pixelWidth = xscale;
 		}
 
 		private long getTimeWidth() {
 			return end - base;
 		}
 
 		int convertTimeToPixel(long t) {
 			t -= base;
 			return (int) (((double) t / (double) getTimeWidth()) * pixelWidth);
 		}
 
 	}
 }
