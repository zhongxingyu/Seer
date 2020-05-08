 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.nxdetector.roi;
 
 import java.text.MessageFormat;
 
 
 public class ImutableRectangularIntegerROI implements RectangularROI<Integer> {
 
 	final private int xstart;
 	final private int xsize;
 	final private int ystart;
 	final private int ysize;
 	final private String name;
 
 	public static ImutableRectangularIntegerROI valueOf(uk.ac.diamond.scisoft.analysis.roi.RectangularROI scisoftRoi) {
 		String name = scisoftRoi.getName();
		int xstart = (int) scisoftRoi.getPointX();
		int ystart = (int) scisoftRoi.getPointY();
		int xsize = (int) scisoftRoi.getLength(0);
		int ysize = (int) scisoftRoi.getLength(1);
 		return new ImutableRectangularIntegerROI(xstart, ystart, xsize, ysize, name);
 	}
 	
 	public ImutableRectangularIntegerROI(Integer xstart, Integer ystart, Integer xsize, Integer ysize, String name) {
 		this.xstart = xstart;
 		this.ystart = ystart;
 		this.xsize = xsize;
 		this.ysize = ysize;
 		this.name = name;
 	}
 
 	@Override
 	public Integer getXstart() {
 		return xstart;
 	}
 
 	@Override
 	public Integer getXsize() {
 		return xsize;
 	}
 
 	@Override
 	public Integer getYstart() {
 		return ystart;
 	}
 
 	@Override
 	public Integer getYsize() {
 		return ysize;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public String toString() {
 		return MessageFormat.format(
 				//"xstart:{0} ystart:{1} xsize:{2} ysize:{3}  ''{4}''", xstart, ystart, xsize, ysize, name);
 				"start=({0}, {1}) size={2}x{3} *{4}*", xstart, ystart, xsize, ysize, name);
 	}
 }
