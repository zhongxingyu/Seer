 /*
  * This file is part of the search package.
  *
  * Copyright (C) 2012, Eric Fritz
  * Copyright (C) 2012, Reed Johnson
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
  * and associated documentation files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
  * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
  * Software is furnished to do so, subject to the following conditions: 
  * 
  * The above copyright notice and this permission notice shall be included in all copies or 
  * substantial portions of the Software. 
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 package edu.uwm.ai.search.agent;
 
 import java.util.List;
 
 import processing.core.PApplet;
 import edu.uwm.ai.search.World;
 import edu.uwm.ai.search.search.SearchAlgorithm;
 import edu.uwm.ai.search.search.SearchResult;
 import edu.uwm.ai.search.util.Point;
 
 /**
  * @author Eric Fritz
  * @author Reed Johnson
  */
 public class SearchEntity extends Entity
 {
 	private PApplet parent;
 
 	private int c;
 	private Entity e;
 	private SearchAlgorithm algorithm;
 	private List<Point> path;
 	private Point lastGoal;
 
 	private int pCost;
 	private int tCost;
 	private double pTime;
 	private double tTime;
 	private int totalSearches;
 
 	// TODO - store costs and draw them
 
 	public SearchEntity(PApplet parent, World w, Point p, int c, Entity e, SearchAlgorithm algorithm)
 	{
 		super(parent, w, p, c);
 
 		this.parent = parent;
 		this.c = c;
 		this.e = e;
 		this.algorithm = algorithm;
 	}
 
 	@Override
 	public void draw()
 	{
 		super.draw();
 
 		parent.fill(0);
 		parent.stroke(c);
 
 		int dimW = getWorld().getBlockWidth();
 		int dimH = getWorld().getBlockHeight();
 
 		int halfW = dimW / 2;
 		int halfH = dimH / 2;
 
 		if (path != null && !path.isEmpty()) {
 			Point a = getPoint();
 
 			int i = 0;
 			while (i < path.size()) {
 				Point b = path.get(i);
 
 				parent.line(a.getX() * dimW + halfW, a.getY() * dimH + halfH, b.getX() * dimW + halfW, b.getY() * dimH + halfH);
 
 				i++;
 				a = b;
 			}
 		}
 	}
 
 	public String getResults()
 	{
 		return String.format("[%3s] %8.2fms, %8.2fms total, %8.2fms avg, %6d nodes expanded, %6d total, %6d avg", algorithm, pTime, tTime, totalSearches == 0 ? 0 : (tTime / totalSearches), pCost, tCost, totalSearches == 0 ? 0 : (tCost / totalSearches));
 	}
 
 	public void update()
 	{
 		if (getPoint().equals(e.getPoint())) {
			path.clear();
 			return;
 		}
 
 		if (path == null || path.isEmpty() || (lastGoal != null && !lastGoal.equals(e.getPoint()))) {
 			lastGoal = new Point(e.getPoint());
 
 			long st = System.nanoTime();
 			SearchResult result = algorithm.search(getPoint(), e.getPoint());
 			pTime = (System.nanoTime() - st) / 1e6;
 			pCost = result.getNumberNodesExpanded();
 
 			path = result.getPath();
 
 			if (!path.isEmpty()) {
 				tTime += pTime;
 				tCost += pCost;
 				totalSearches++;
 
 				if (!path.isEmpty()) {
 					path.remove(0);
 				}
 			}
 		}
 
 		if (!path.isEmpty()) {
 			moveTo(path.remove(0));
 		}
 	}
 }
