 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package plotter.xy;
 
import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import junit.framework.TestCase;
 import plotter.CountingGraphics;
 import plotter.LineChecker;
 import plotter.xy.SlopeLine.Listener;
 
 public class JUnitSlopeLine extends TestCase {
 	private final class MockListener implements Listener {
 		SlopeLine line;
 
 		XYPlot plot;
 
 		int addCount;
 
 		int updateCount;
 
 		int removeCount;
 
 		double startX;
 
 		double startY;
 
 		double endX;
 
 		double endY;
 
 
 		public MockListener(SlopeLine line, XYPlot plot) {
 			this.line = line;
 			this.plot = plot;
 		}
 
 
 		@Override
 		public void slopeLineUpdated(SlopeLine line, XYPlot plot, double startX, double startY, double endX, double endY) {
 			assertEquals(this.line, line);
 			assertEquals(this.plot, plot);
 			updateCount++;
 			this.startX = startX;
 			this.startY = startY;
 			this.endX = endX;
 			this.endY = endY;
 		}
 
 
 		@Override
 		public void slopeLineRemoved(SlopeLine line, XYPlot plot) {
 			assertEquals(this.line, line);
 			assertEquals(this.plot, plot);
 			removeCount++;
 		}
 
 
 		@Override
 		public void slopeLineAdded(SlopeLine line, XYPlot plot, double startX, double startY) {
 			assertEquals(this.line, line);
 			assertEquals(this.plot, plot);
 			addCount++;
 			this.startX = startX;
 			this.startY = startY;
 		}
 	}
 
 
 	public void testSlopeLine() {
 		XYPlot plot = new XYPlot();
 		XYAxis xAxis = new LinearXYAxis(XYDimension.X);
 		XYAxis yAxis = new LinearXYAxis(XYDimension.Y);
 		plot.add(xAxis);
 		plot.add(yAxis);
 		plot.setXAxis(xAxis);
 		plot.setYAxis(yAxis);
 		xAxis.setStart(0);
 		xAxis.setEnd(1);
 		yAxis.setStart(0);
 		yAxis.setEnd(1);
 		xAxis.setSize(1000, 1);
 		yAxis.setSize(1, 1000);
 		XYPlotContents contents = new XYPlotContents();
 		plot.add(contents);
 		SlopeLine slopeLine = new SlopeLine();
 		slopeLine.attach(plot);
 		slopeLine.setSize(1000, 1000);
 
 		MockListener listener = new MockListener(slopeLine, plot);
 		slopeLine.addListenerForPlot(null, listener);
 
 		int x = 100;
 		int y = 100;
 		int xAbs = x + 10;
 		int yAbs = y + 10;
		MouseEvent press = new MouseEvent(contents, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, x, y,
 				xAbs, yAbs, 1, false, MouseEvent.BUTTON1);
 		slopeLine.mousePressed(press);
 		assertEquals(1, listener.addCount);
 		assertEquals(0, listener.updateCount);
 		assertEquals(0, listener.removeCount);
 		assertEquals(.1, listener.startX, .01);
 		assertEquals(.9, listener.startY, .01);
 
 		int x2 = 200;
 		int y2 = 300;
 		int xAbs2 = x2 + 10;
 		int yAbs2 = y2 + 10;
 		MouseEvent drag = new MouseEvent(contents, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, x2, y2,
 				xAbs2, yAbs2, 1, false, MouseEvent.BUTTON1);
 		slopeLine.mouseDragged(drag);
 		assertEquals(1, listener.addCount);
 		assertEquals(1, listener.updateCount);
 		assertEquals(0, listener.removeCount);
 		assertEquals(.1, listener.startX, .01);
 		assertEquals(.9, listener.startY, .01);
 		assertEquals(.2, listener.endX, .01);
 		assertEquals(.7, listener.endY, .01);
 
 		int x3 = 300;
 		int y3 = 200;
 		int xAbs3 = x3 + 10;
 		int yAbs3 = y3 + 10;
 		MouseEvent drag2 = new MouseEvent(contents, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, x3, y3,
 				xAbs3, yAbs3, 1, false, MouseEvent.BUTTON1);
 		slopeLine.mouseDragged(drag2);
 		assertEquals(1, listener.addCount);
 		assertEquals(2, listener.updateCount);
 		assertEquals(0, listener.removeCount);
 		assertEquals(.1, listener.startX, .01);
 		assertEquals(.9, listener.startY, .01);
 		assertEquals(.3, listener.endX, .01);
 		assertEquals(.8, listener.endY, .01);
 
 		CountingGraphics g = new CountingGraphics(new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR)
 				.createGraphics());
 		slopeLine.paintComponent(g);
 		LineChecker lines = new LineChecker();
 		lines.require(0, 50, 1000, 550);
 		lines.check(g.getLines());
 
 		MouseEvent release = new MouseEvent(contents, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, x3, y3,
 				xAbs3, yAbs3, 1, false, MouseEvent.BUTTON1);
 		slopeLine.mouseReleased(release);
 		assertEquals(1, listener.addCount);
 		assertEquals(2, listener.updateCount);
 		assertEquals(1, listener.removeCount);
 	}
 }
