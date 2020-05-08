 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 331715: Support for rectangular grids in diagrams
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.util.draw2d;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.graphiti.internal.util.LookManager;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.util.DataTypeTransformation;
 import org.eclipse.graphiti.util.ILook;
 import org.eclipse.swt.graphics.Color;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GridLayer extends org.eclipse.gef.editparts.GridLayer {
 	private IConfigurationProvider cfgProvider;
 
 	public GridLayer(IConfigurationProvider cfgProvider) {
 		setCfgProvider(cfgProvider);
 		updateFromDiagram();
 	}
 
 	@Override
 	protected void paintGrid(Graphics g) {
 
 		Rectangle clip = g.getClip(Rectangle.SINGLETON);
 
		Diagram diagram = getDiagram();
		if (diagram == null) {
			// Editor is already disposed
			return;
		}
		GraphicsAlgorithm graphicsAlgorithm = diagram.getGraphicsAlgorithm();
 
 		org.eclipse.graphiti.mm.algorithms.styles.Color background = Graphiti.getGaService().getBackgroundColor(graphicsAlgorithm, true);
 		if (background.getBlue() != getBackgroundColor().getBlue() || background.getRed() != getBackgroundColor().getRed()
 				|| background.getGreen() != getBackgroundColor().getGreen()
 
 		) {
 			updateFromDiagram();
 		}
 
 		Color majorLineColor = getMajorLineColor();
 
 		Color minorLineColor = getMinorLineColor();
 
		gridX = diagram.getGridUnit();
		gridY = diagram.getVerticalGridUnit();
 		if (gridY == -1) {
 			// No vertical grid unit set (or old diagram before 0.8): use vertical grid unit
 			gridY = gridX;
 		}
 
 		g.setBackgroundColor(getBackgroundColor());
 		g.fillRectangle(clip);
 
 		if (gridX > 0) {
 
 			int i = clip.x;
 			while (i % gridX != 0)
 				i++;
 
 			for (; i < clip.x + clip.width; i += gridX) {
 				prepareG(g, majorLineColor, minorLineColor, i, gridX);
 				g.drawLine(i, clip.y, i, clip.y + clip.height);
 			}
 		}
 
 		if (gridY > 0) {
 			int i = clip.y;
 			while (i % gridY != 0)
 				i++;
 
 			for (; i < clip.y + clip.height; i += gridY) {
 				prepareG(g, majorLineColor, minorLineColor, i, gridY);
 				g.drawLine(clip.x, i, clip.x + clip.width, i);
 			}
 		}
 
 	}
 
 	// }
 
 	/**
 	 * Sets the color while drawing the background grid.
 	 * 
 	 * @param g
 	 *            The Graphics object to set the foreground color for
 	 * @param gridPosition
 	 *            The position in the grid
 	 * @return The modified Graphics object
 	 */
 	private void prepareG(Graphics g, Color gridColor, Color gridColorLight, int gridPosition, int gridSize) {
 
 		int p = getGridLineAlternation() * gridSize;
 		if (gridPosition % (p) == 0) {
 			g.setForegroundColor(gridColor);
 			// g.setLineStyle(SWT.LINE_SOLID);
 		} else {
 			g.setForegroundColor(gridColorLight);
 			// g.setLineStyle(SWT.LINE_DOT);
 		}
 
 	}
 
 	/**
 	 * @return Returns the diagram.
 	 */
 	private Diagram getDiagram() {
 		return getCfgProvider().getDiagram();
 	}
 
 	/**
 	 */
 	private void updateFromDiagram() {
 		GraphicsAlgorithm diagramGa = getDiagram().getGraphicsAlgorithm();
 
 		org.eclipse.graphiti.mm.algorithms.styles.Color background = Graphiti.getGaService().getBackgroundColor(diagramGa, true);
 		setBackgroundColor(DataTypeTransformation.toSwtColor(getDiagramEditor(), background));
 		org.eclipse.graphiti.mm.algorithms.styles.Color foreground = Graphiti.getGaService().getForegroundColor(diagramGa, true);
 		setForegroundColor(DataTypeTransformation.toSwtColor(getDiagramEditor(), foreground));
 	}
 
 	private int getGridLineAlternation() {
 		final ILook look = LookManager.getLook();
 		int i = look.getMajorGridLineDistance() / look.getMinorGridLineDistance();
 		return Math.max(1, i);
 	}
 
 	private Color getMinorLineColor() {
 		org.eclipse.graphiti.mm.algorithms.styles.Color foregroundColor = null;
 		final Diagram diagram = getDiagram();
 		if (diagram != null) {
 			GraphicsAlgorithm diagramGa = diagram.getGraphicsAlgorithm();
 			foregroundColor = Graphiti.getGaService().getForegroundColor(diagramGa, true);
 		}
 		if (foregroundColor == null) {
 			return DataTypeTransformation.toSwtColor(getDiagramEditor(), LookManager.getLook().getMinorGridLineColor());
 		}
 		return DataTypeTransformation.toSwtColor(getDiagramEditor(), foregroundColor);
 	}
 
 	private Color getMajorLineColor() {
 		return DataTypeTransformation.toSwtColor(getDiagramEditor(), LookManager.getLook().getMajorGridLineColor());
 	}
 
 	private DiagramEditor getDiagramEditor() {
 		return getCfgProvider().getDiagramEditor();
 	}
 
 	private IConfigurationProvider getCfgProvider() {
 		return cfgProvider;
 	}
 
 	private void setCfgProvider(IConfigurationProvider cfgProvider) {
 		this.cfgProvider = cfgProvider;
 	}
 }
