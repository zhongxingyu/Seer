 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dawnsci.plotting.jreality.util;
 
 import java.awt.Color;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 import de.jreality.geometry.IndexedLineSetFactory;
 import de.jreality.scene.Appearance;
 import de.jreality.scene.SceneGraphComponent;
 import de.jreality.shader.CommonAttributes;
 import de.jreality.shader.DefaultGeometryShader;
 import de.jreality.shader.DefaultLineShader;
 import de.jreality.shader.ShaderUtility;
 import de.jreality.util.SceneGraphUtility;
 
 public class ErrorHelpers {
 
 	private static void buildErrorBar(double[] coords, int[] indexes, int barIndex, double pointX, double pointY,
 			double pointZ, double errorAmount, double barSize, double[] errorDirection, double[] barSizeDirection) {
 
 		double[] barLength = new double[3];
 		double[] barWidth = new double[3];
 
 		for (int i = 0; i < 3; i++) {
 			barLength[i] = errorDirection[i] * errorAmount;
 			barWidth[i] = barSizeDirection[i] * barSize;
 		}
 
 		double[] point = new double[] { pointX, pointY, pointZ };
 
 		buildErrorBar(coords, indexes, barIndex, point, barLength, barWidth);
 
 	}
 
 	private static void buildErrorBar(double[] coords, int[] indexes, int barIndex, double[] point, double[] barLength,
 			double[] barWidth) {
 
 		// Firsty add the vertices
 		int offset = barIndex * 6 * 3;
 
 		// Bar ends
 		for (int i = 0; i < 3; i++) {
 			coords[offset++] = point[i] + barLength[i];
 		}
 
 		for (int i = 0; i < 3; i++) {
 			coords[offset++] = point[i] - barLength[i];
 		}
 
 		// Bar top
 		for (int i = 0; i < 3; i++) {
 			coords[offset++] = point[i] + barLength[i] + barWidth[i];
 		}
 
 		for (int i = 0; i < 3; i++) {
 			coords[offset++] = point[i] + barLength[i] - barWidth[i];
 		}
 
 		// Bar Bottom
 		for (int i = 0; i < 3; i++) {
 			coords[offset++] = point[i] - barLength[i] + barWidth[i];
 		}
 
 		for (int i = 0; i < 3; i++) {
 			coords[offset++] = point[i] - barLength[i] - barWidth[i];
 		}
 
 		// now the indexes
 		offset = barIndex * 3 * 2;
 		indexes[offset++] = (barIndex * 6 + 0); // top vertex
 		indexes[offset++] = (barIndex * 6 + 1); // bottom vertes
 		indexes[offset++] = (barIndex * 6 + 2); // top left
 		indexes[offset++] = (barIndex * 6 + 3); // top right
 		indexes[offset++] = (barIndex * 6 + 4); // bottom left
 		indexes[offset++] = (barIndex * 6 + 5); // bottom right
 
 	}
 
 	/**
 	 * 
 	 * @param xPoints an array(n) of all the x points to be dealt with
 	 * @param yPoints an array(n) of all the y points to be dealt with
 	 * @param zPoints an array(n) of all the z points to be dealt with
 	 * @param errorPoints an array(n) of all the error values for the points
 	 * @param errorBarWidth the width of all the error bars
 	 * @param errorBarDirection the vector(3) describing a unit vector of the direction of the error bar
 	 * @param errorBarWidthDirection the vector(3) describing a unit vector of the direction of the error bar top and bottom
 	 * @return the created Scene graph component for the appropriate error part
 	 */
 	public static SceneGraphComponent createErrorNode(double[] xPoints, double[] yPoints, double[] zPoints,
 			double[] errorPoints, double errorBarWidth, double[] errorBarDirection, double[] errorBarWidthDirection) {
 
 		// Get some basic information about the size of the dataset to deal with
 		int numberOfPoints = xPoints.length;
 
 		// Create a new factory to build the geometry for us
 		IndexedLineSetFactory lFactory = new IndexedLineSetFactory();
 
 		// This needs some arrays of data, these need to be specific sizes
 		int vertexCount = numberOfPoints * 6; // 6 points per point
 		int lineCount = numberOfPoints * 3; // 3 lines per point
 
 		// These arrays need to be presetup in the factory and then the arrays themselves allocated
 		lFactory.setVertexCount(vertexCount);
 		double[] coords = new double[vertexCount * 3];
 		lFactory.setEdgeCount(lineCount);
 		int[] lineIndicies = new int[lineCount * 2];
 
 		// Work out the geometry per point
 		for (int i = 0; i < numberOfPoints; i++) {
 			if(errorPoints[i] > 0) {
 				buildErrorBar(coords, lineIndicies, i, xPoints[i], yPoints[i], zPoints[i], errorPoints[i], errorBarWidth,
 						errorBarDirection, errorBarWidthDirection);
 			}
 		}
 
 		lFactory.setVertexCoordinates(coords);
 		lFactory.setEdgeIndices(lineIndicies);
 
 		lFactory.update();
 
 		// now the line factory is complete and has all the lines in it, create the scene graph node
 		SceneGraphComponent errorGroupNode = SceneGraphUtility.createFullSceneGraphComponent("AxisNode");
 		errorGroupNode.setGeometry(lFactory.getIndexedLineSet());
 
 
 		// now the geometry data is in place, we need to set up the appearance
 		Appearance graphAppearance = new Appearance();
 		errorGroupNode.setAppearance(graphAppearance);
 
 		graphAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."	+ CommonAttributes.TUBES_DRAW, false);
 		graphAppearance.setAttribute(CommonAttributes.POINT_SHADER + "." + CommonAttributes.SPHERES_DRAW, false);
 		graphAppearance.setAttribute(CommonAttributes.ATTENUATE_POINT_SIZE,false);
 		graphAppearance.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
 
 		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(graphAppearance, true);
 		dgs.setShowFaces(false);
 		dgs.setShowLines(true);
 		dgs.setShowPoints(false);
 
 		DefaultLineShader dls = (DefaultLineShader) dgs.createLineShader("default");
 		dls.setDiffuseColor(Color.BLACK);
 		dls.setTubeDraw(false);
 		dls.setLineWidth(1.0);
 
 		return errorGroupNode;
 	}
 	
 	
 	public static void updateErrorNode(SceneGraphComponent sgc, double[] xPoints, double[] yPoints, double[] zPoints,
 			double[] errorPoints, double errorBarWidth, double[] errorBarDirection, double[] errorBarWidthDirection) {
 
 		// Get some basic information about the size of the dataset to deal with
 		int numberOfPoints = xPoints.length;
 
 		// Create a new factory to build the geometry for us
 		IndexedLineSetFactory lFactory = new IndexedLineSetFactory();
 
 		// This needs some arrays of data, these need to be specific sizes
 		int vertexCount = numberOfPoints * 6; // 6 points per point
 		int lineCount = numberOfPoints * 3; // 3 lines per point
 
 		// These arrays need to be presetup in the factory and then the arrays themselves allocated
 		lFactory.setVertexCount(vertexCount);
 		double[] coords = new double[vertexCount * 3];
 		lFactory.setEdgeCount(lineCount);
 		int[] lineIndicies = new int[lineCount * 2];
 
 		// Work out the geometry per point
 		for (int i = 0; i < numberOfPoints; i++) {
 			if(errorPoints[i] > 0) {
 				buildErrorBar(coords, lineIndicies, i, xPoints[i], yPoints[i], zPoints[i], errorPoints[i], errorBarWidth,
 						errorBarDirection, errorBarWidthDirection);
 			}
 		}
 
 		lFactory.setVertexCoordinates(coords);
 		lFactory.setEdgeIndices(lineIndicies);
 
 		lFactory.update();
 
 		// now the line factory is complete and has all the lines in it, update the node
 		sgc.setGeometry(lFactory.getIndexedLineSet());
 	}
 	
 
 
 	public static double[] extractAndScale(IDataset dataset, double graphmin, double worldmax, double graphmax) {
 		// check that its a 1D dataset
 		if(dataset.getShape().length > 1) {
 			throw new IllegalArgumentException("Only 1D datasets can be processed by extract and scale");
 		}
 		double[] data = new double[dataset.getShape()[0]];
 
 		for(int i = 0; i < data.length; i++) {
 			data[i] = (dataset.getDouble(i) - graphmin) * (worldmax/(graphmax - graphmin));
 		}
 
 		return data;
 	}
 	
 	public static double[] extractAndScaleError(IDataset dataset, double graphmin, double worldmax, double graphmax) {
 		// check that its a 1D dataset
 		if(dataset.getShape().length > 1) {
 			throw new IllegalArgumentException("Only 1D datasets can be processed by extract and scale");
 		}
 		double[] data = new double[dataset.getShape()[0]];
 
 		AbstractDataset ds = (AbstractDataset) dataset; 
 		
 		for(int i = 0; i < data.length; i++) {
			data[i] = (ds.getErrorDouble(i)) * (worldmax/(graphmax - graphmin));
 		}
 
 		return data;
 	}
 	
 	public static double[] constantPoints(double value, int size) {
 		double[] result = new double[size];
 		for(int i = 0 ;i < size; i++) {
 			result[i] = value;
 		}
 		return result;
 	}
 
 }
