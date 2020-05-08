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
 
 package edu.uwm.ai.search;
 
 import java.util.Random;
 
 import edu.uwm.ai.search.util.Point;
 
 import processing.core.PApplet;
 
 /**
  * @author Eric Fritz
  * @author ReedJohnson
  */
 public class World
 {
 	private PApplet parent;
 
 	private final int w;
 	private final int h;
 	private final boolean[] obstacles;
 	private final Random random = new Random();
 
 	public World(PApplet parent, int w, int h)
 	{
 		this.parent = parent;
 
 		this.w = w;
 		this.h = h;
 		this.obstacles = new boolean[w * h];
 
 		for (int k = 0; k < 20; k++) {
 			int rw = (int) (Math.random() * 5) + 2;
 			int rh = (int) (Math.random() * 5) + 2;
 
 			int i = (int) (Math.random() * (w - rw / 2));
 			int j = (int) (Math.random() * (h - rh / 2));
 
 			for (int dw = 0; dw < rw; dw++) {
 				for (int dh = 0; dh < rh; dh++) {
 					setObstacle(i + dw, j + dh);
 				}
 			}
 		}
 	}
 
 	public Point getRandomFreePoint()
 	{
 		Point p;
 
 		do {
 			p = new Point((int) (random.nextDouble() * w), (int) (random.nextDouble() * h));
 		} while (!isValidPosition(p));
 
 		return p;
 	}
 
 	public int getWidth()
 	{
 		return w;
 	}
 
 	public int getHeight()
 	{
 		return h;
 	}
 
 	public boolean isValidPosition(Point p)
 	{
 		return isValidPosition(p.getX(), p.getY());
 	}
 
 	public boolean isValidPosition(int i, int j)
 	{
 		return i >= 0 && j >= 0 && i < w && j < h && !hasObstacle(i, j);
 	}
 
 	public boolean isAccessableThrough(Point dest, Point origin)
 	{
 		if (!isValidPosition(dest) || !isValidPosition(origin)) {
 			throw new IllegalArgumentException("Points are not valid.");
 		}
 
 		int xDist = Math.abs(dest.getX() - origin.getX());
 		int yDist = Math.abs(dest.getY() - origin.getY());
 
 		if (xDist > 1 || yDist > 1) {
 			throw new IllegalArgumentException("Points are not adjacent.");
 		}
 
 		// If points are touching there cannot be an obstacle between them.
 
 		if ((xDist == 0 && yDist == 1) || (xDist == 1 && yDist == 0)) {
 			return true;
 		}
 
 		// Otherwise ensure that the diagonal move doesn't go through a corner where two separate
 		// walls are joined.
 
 		boolean ob1 = isValidPosition(new Point(origin.getX() + xDist, origin.getY()));
 		boolean ob2 = isValidPosition(new Point(origin.getX(), origin.getY() + yDist));
 
		return ob1 && ob2;
 	}
 
 	public boolean hasObstacle(Point p)
 	{
 		return hasObstacle(p.getX(), p.getY());
 	}
 
 	public boolean hasObstacle(int i, int j)
 	{
 		return obstacles[getIndex(i, j)];
 	}
 
 	public void setObstacle(int i, int j)
 	{
 		obstacles[getIndex(i, j)] = true;
 	}
 
 	public int getBlockWidth()
 	{
 		return Search.displayWidth / w;
 	}
 
 	public int getBlockHeight()
 	{
 		return Search.displayHeight / h;
 	}
 
 	public void draw()
 	{
 		parent.fill(200, 200, 200);
 		parent.stroke(0);
 
 		int dimW = getBlockWidth();
 		int dimH = getBlockHeight();
 
 		for (int i = 0; i < w; i++) {
 			for (int j = 0; j < h; j++) {
 				if (hasObstacle(i, j)) {
 					parent.rect(i * dimW, j * dimH, dimW, dimH);
 				}
 			}
 		}
 	}
 
 	private int getIndex(int i, int j)
 	{
 		return j * h + i;
 	}
 }
