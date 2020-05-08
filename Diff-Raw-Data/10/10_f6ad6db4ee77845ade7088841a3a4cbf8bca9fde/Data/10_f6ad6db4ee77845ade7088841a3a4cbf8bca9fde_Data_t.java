 package com.thoughtworks.thoughtferret.view.moodgraph.graph;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Point;
 import android.graphics.Rect;
 
 import com.thoughtworks.thoughtferret.MathUtils;
 import com.thoughtworks.thoughtferret.model.mood.RatingAverages;
 import com.thoughtworks.thoughtferret.model.mood.RatingPeriod;
 
 public class Data {
 
 	private final List<Point> points;
 	
 	public Data(RatingAverages averages, Chronology chronology, Rect surface) {
 		points = new ArrayList<Point>();
 		for (RatingPeriod period : averages.getAverages()) {
 			int x1 = chronology.getX(period.getStartDate());
 			int x2 = chronology.getX(period.getEndDate());
 			if (period.hasRatings()) {
 				int y = getY(period, surface);
 				points.add(new Point(x1, y));
 				points.add(new Point(x2, y));
 			}
 		}
 	}
 	
 	public List<Point> getPoints() {
 		return points;
 	}
 	
 	private int getY(RatingPeriod period, Rect surface) {
 		int bestY = surface.top + Timeline.HEIGHT;
 		int worstY = surface.bottom - Timeline.HEIGHT;
 		return (int) MathUtils.projectReversed(1, 5, worstY, bestY, period.getAverage().doubleValue());
 	}
 	
 }
