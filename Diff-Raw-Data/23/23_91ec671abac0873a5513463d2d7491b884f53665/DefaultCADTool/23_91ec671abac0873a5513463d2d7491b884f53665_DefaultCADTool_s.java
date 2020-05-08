 /* gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
  *
  * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ibez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 package com.iver.cit.gvsig.gui.cad;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.hardcode.gdbms.engine.values.ValueFactory;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
 import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
 import com.iver.cit.gvsig.fmap.ViewPort;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.Handler;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
 import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
 import com.iver.cit.gvsig.fmap.core.v02.FGraphicUtilities;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.fmap.layers.FBitSet;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SpatialCache;
 import com.iver.cit.gvsig.gui.cad.exception.CommandException;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 import com.iver.utiles.console.JConsole;
 
 /**
  * DOCUMENT ME!
  *
  * @author Vicente Caballero Navarro
  */
 public abstract class DefaultCADTool implements CADTool {
 	public static ISymbol selectionSymbol = SymbologyFactory.
 	createDefaultSymbolByShapeType(FShape.MULTI, new Color(255, 0,0, 100)); // Le ponemos una transparencia
 	public static ISymbol axisReferencesSymbol = SymbologyFactory.
 	createDefaultSymbolByShapeType(FShape.MULTI, new Color(100, 100, 100, 100));
 	public static ISymbol geometrySelectSymbol = SymbologyFactory.
 	createDefaultSymbolByShapeType(FShape.MULTI, Color.RED);
 	public static ISymbol handlerSymbol = SymbologyFactory.
 	createDefaultSymbolByShapeType(FShape.MULTI, Color.ORANGE);
 	private static Logger logger = Logger.getLogger(DefaultCADTool.class
 			.getName());
 	private CADToolAdapter cadToolAdapter;
 
 	private String question;
 
 	private String[] currentdescriptions;
 
 	private String tool = "selection";
 
 	private DefaultCADTool previousTool;
 
 	private ArrayList temporalCache = new ArrayList();
 
 	public void addTemporalCache(IGeometry geom) {
 		temporalCache.add(geom);
 		insertSpatialCache(geom);
 	}
 	public void clearTemporalCache() {
 		IGeometry[] geoms=(IGeometry[])temporalCache.toArray(new IGeometry[0]);
 		for (int i=0;i<geoms.length;i++) {
 			removeSpatialCache(geoms[i]);
 		}
 		temporalCache.clear();
 	}
 	protected void insertSpatialCache(IGeometry geom) {
 		VectorialLayerEdited vle = getVLE();
 		SpatialCache spatialCache=((FLyrVect)vle.getLayer()).getSpatialCache();
 		Rectangle2D r=geom.getBounds2D();
 		if (geom.getGeometryType()==FShape.POINT) {
 			r = new Rectangle2D.Double(r.getX(),r.getY(),1,1);
 		}
 		spatialCache.insert(r,geom);
 
 	}
 	private void removeSpatialCache(IGeometry geom) {
 		VectorialLayerEdited vle = getVLE();
 		SpatialCache spatialCache=((FLyrVect)vle.getLayer()).getSpatialCache();
 		Rectangle2D r=null;
 		if (geom.getGeometryType()==FShape.POINT) {
 			r = new Rectangle2D.Double(r.getX(),r.getY(),1,1);
 		}else {
 			r=geom.getBounds2D();
 		}
 		spatialCache.remove(r,geom);
 
 	}
 	/**
 	 * DOCUMENT ME!
 	 */
 	public void draw(IGeometry geometry) {
 		if (geometry != null) {
 			BufferedImage img = getCadToolAdapter().getMapControl().getImage();
 			Graphics2D gImag = (Graphics2D) img.getGraphics();
 			ViewPort vp = getCadToolAdapter().getMapControl().getViewPort();
 			geometry.draw(gImag, vp, DefaultCADTool.selectionSymbol);
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param cta
 	 *            DOCUMENT ME!
 	 */
 	public void setCadToolAdapter(CADToolAdapter cta) {
 		cadToolAdapter = cta;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public CADToolAdapter getCadToolAdapter() {
 		return cadToolAdapter;
 	}
 
 	public VectorialLayerEdited getVLE() {
 		return (VectorialLayerEdited) CADExtension.getEditionManager()
 				.getActiveLayerEdited();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param g
 	 *            DOCUMENT ME!
 	 * @param firstPoint
 	 *            DOCUMENT ME!
 	 * @param endPoint
 	 *            DOCUMENT ME!
 	 */
 	public void drawLine(Graphics2D g, Point2D firstPoint, Point2D endPoint, ISymbol symbol) {
 		GeneralPathX elShape = new GeneralPathX(GeneralPathX.WIND_EVEN_ODD, 2);
 		elShape.moveTo(firstPoint.getX(), firstPoint.getY());
 		elShape.lineTo(endPoint.getX(), endPoint.getY());
 		ShapeFactory.createPolyline2D(elShape).draw(g,
 				getCadToolAdapter().getMapControl().getViewPort(),
 					symbol);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param geometry
 	 *            DOCUMENT ME!
 	 */
 	public void addGeometry(IGeometry geometry) {
 		VectorialEditableAdapter vea = getVLE().getVEA();
 		try {
 			// Deberamos comprobar que lo que escribimos es correcto:
 			// Lo hacemos en el VectorialAdapter, justo antes de
 			// aadir, borrar o modificar una feature
 
 			int numAttr = vea.getRecordset().getFieldCount();
 			Value[] values = new Value[numAttr];
 			for (int i = 0; i < numAttr; i++) {
 				values[i] = ValueFactory.createNullValue();
 			}
 			String newFID = vea.getNewFID();
 			DefaultFeature df = new DefaultFeature(geometry, values, newFID);
 			int index = vea.addRow(df, getName(), EditionEvent.GRAPHIC);
 			VectorialLayerEdited vle = getVLE();
 			clearSelection();
 			//ArrayList selectedRow = vle.getSelectedRow();
 
 
 			ViewPort vp = vle.getLayer().getMapContext().getViewPort();
 			BufferedImage selectionImage = new BufferedImage(vp
 					.getImageWidth(), vp.getImageHeight(),
 					BufferedImage.TYPE_INT_ARGB);
 			Graphics2D gs = selectionImage.createGraphics();
 			int inversedIndex=vea.getInversedIndex(index);
 			vle.addSelectionCache(new DefaultRowEdited(df,
 					IRowEdited.STATUS_ADDED, inversedIndex ));
 			vea.getSelection().set(inversedIndex);
 			IGeometry geom = df.getGeometry();
 			geom.cloneGeometry().draw(gs, vp, DefaultCADTool.selectionSymbol);
 			vle.drawHandlers(geom.cloneGeometry(), gs, vp);
 			vea.setSelectionImage(selectionImage);
 			insertSpatialCache(geom);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 			return;
 		} catch (ValidateRowException e) {
 			NotificationManager.addError(e.getMessage(),e);
 			return;
 		}
 		draw(geometry.cloneGeometry());
 	}
 
 
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param geometry
 	 *            DOCUMENT ME!
 	 */
 	public void modifyFeature(int index, IFeature row) {
 		try {
 			getVLE().getVEA().modifyRow(index, row, getName(),
 					EditionEvent.GRAPHIC);
 		} catch (ValidateRowException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (ExpansionFileWriteException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		}
 		draw(row.getGeometry().cloneGeometry());
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param geometry
 	 *            DOCUMENT ME!
 	 * @param values
 	 *            DOCUMENT ME!
 	 */
 	public int addGeometry(IGeometry geometry, Value[] values) {
 		int index = 0;
 		VectorialEditableAdapter vea = getVLE().getVEA();
 		try {
 			String newFID = vea.getNewFID();
 			DefaultFeature df = new DefaultFeature(geometry, values, newFID);
 			index = vea.addRow(df, getName(), EditionEvent.GRAPHIC);
 			insertSpatialCache(geometry);
 		} catch (ValidateRowException e) {
 			NotificationManager.addError(e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e);
 		}
 		return vea.getInversedIndex(index);
 	}
 
 	/**
 	 * Devuelve la cadena que corresponde al estado en el que nos encontramos.
 	 *
 	 * @return Cadena para mostrar por consola.
 	 */
 	public String getQuestion() {
 		return question;
 	}
 
 	/**
 	 * Actualiza la cadena que corresponde al estado actual.
 	 *
 	 * @param s
 	 *            Cadena que aparecer en consola.
 	 */
 	public void setQuestion(String s) {
 		question = s;
 		// ConsoleToken.addQuestion(s);
 	}
 
 	/**
 	 * Provoca un repintado "soft" de la capa activa en edicin. Las capas por
 	 * debajo de ella no se dibujan de verdad, solo se dibuja la que est en
 	 * edicin y las que estn por encima de ella en el TOC.
 	 */
 	public void refresh() {
 		// getCadToolAdapter().getMapControl().drawMap(false);
 //		getVLE().getLayer().setDirty(true);
 
 		getCadToolAdapter().getMapControl().rePaintDirtyLayers();
 	}
 
 	/*
 	 * public void drawHandlers(Graphics g, FBitSet sel, AffineTransform at)
 	 * throws DriverIOException { for (int i = sel.nextSetBit(0); i >= 0; i =
 	 * sel.nextSetBit(i + 1)) { IGeometry ig =
 	 * getCadToolAdapter().getVectorialAdapter() .getShape(i).cloneGeometry();
 	 * if (ig == null) continue; Handler[] handlers =
 	 * ig.getHandlers(IGeometry.SELECTHANDLER);
 	 * FGraphicUtilities.DrawHandlers((Graphics2D) g, at, handlers); } }
 	 */
 	public void drawHandlers(Graphics g, ArrayList selectedRows,
 			AffineTransform at) {
 		for (int i = 0; i < selectedRows.size(); i++) {
 			IRowEdited edRow = (IRowEdited) selectedRows.get(i);
 			IFeature feat = (IFeature) edRow.getLinkedRow();
 			// IFeature feat = (IFeature) selectedRows.get(i);
 			IGeometry ig = feat.getGeometry().cloneGeometry();
 			if (ig == null)
 				continue;
 			Handler[] handlers = ig.getHandlers(IGeometry.SELECTHANDLER);
 			FGraphicUtilities.DrawHandlers((Graphics2D) g, at, handlers,DefaultCADTool.handlerSymbol);
 		}
 	}
 
 	public void setDescription(String[] currentdescriptions) {
 		this.currentdescriptions = currentdescriptions;
 	}
 
 	public String[] getDescriptions() {
 		return currentdescriptions;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#end()
 	 */
 	public void end() {
 		CADExtension.setCADTool("_selection", true);
 		PluginServices.getMainFrame().setSelectedTool("_selection");
 		CADTool cadtool=CADExtension.getCADTool();
 		cadtool.setPreviosTool(this);
 	}
 
 	public void init() {
 // jaume, should not be necessary
 //		CADTool.drawingSymbol.setOutlined(true);
 //		CADTool.drawingSymbol.setOutlineColor(Color.GREEN);
 
 	}
 
 	protected ArrayList getSelectedRows() {
 		VectorialLayerEdited vle = getVLE();
 		ArrayList selectedRow = vle.getSelectedRow();
 		return selectedRow;
 	}
 
 	protected ArrayList getSelectedHandlers() {
 		VectorialLayerEdited vle = getVLE();
 		ArrayList selectedHandlers = vle.getSelectedHandler();
 		return selectedHandlers;
 	}
 
 	public void clearSelection() throws ReadDriverException {
 		VectorialLayerEdited vle = getVLE();
 		ArrayList selectedRow = vle.getSelectedRow();
 		ArrayList selectedHandlers = vle.getSelectedHandler();
 		selectedRow.clear();
 		selectedHandlers.clear();
 		VectorialEditableAdapter vea = vle.getVEA();
 		FBitSet selection = vea.getSelection();
 		selection.clear();
 		vea.setSelectionImage(null);
 		vea.setHandlersImage(null);
 
 	}
 
 	public String getNextTool() {
 		return tool;
 	}
 
 	public void setNextTool(String tool) {
 		this.tool = tool;
 	}
 
 	public boolean changeCommand(String name) throws CommandException {
 		CADTool[] cadtools = CADExtension.getCADTools();
 		for (int i = 0; i < cadtools.length; i++) {
 			CADTool ct = cadtools[i];
 			if (name.equalsIgnoreCase(ct.getName())
 					|| name.equalsIgnoreCase(ct.toString())) {
 				int type = FShape.POINT;
 				try {
 					type = ((FLyrVect) getVLE().getLayer()).getShapeType();
 				} catch (ReadDriverException e) {
 					throw new CommandException(e);
 				}
 				if (ct.isApplicable(type)) {
 					getCadToolAdapter().setCadTool(ct);
 					ct.init();
 					View vista = (View) PluginServices.getMDIManager()
 							.getActiveWindow();
 					vista.getConsolePanel().addText("\n" + ct.getName(),
 							JConsole.COMMAND);
 					String question=ct.getQuestion();
 					vista.getConsolePanel().addText(
 							"\n" + "#" + question + " > ", JConsole.MESSAGE);
 					return true;
 				}
 				throw new CommandException(name);
 			}
 		}
 		return false;
 	}
 
 	public boolean isApplicable(int shapeType) {
 		return true;
 	}
 
 	public abstract String toString();
 
 	public void throwValueException(String s, double d) {
		View vista = (View) PluginServices.getMDIManager().getActiveWindow();
		vista.getConsolePanel().addText(s + " : " + d, JConsole.ERROR);
 	}
 
 	public void throwOptionException(String s, String o) {
		View vista = (View) PluginServices.getMDIManager().getActiveWindow();
		vista.getConsolePanel().addText(s + " : " + o, JConsole.ERROR);
 	}
 
 	public void throwPointException(String s, double x, double y) {
		View vista = (View) PluginServices.getMDIManager().getActiveWindow();
		vista.getConsolePanel().addText(s + " : " + " X = " + x + ", Y = " + y,
 				JConsole.ERROR);
 	}
 
 	public void setPreviosTool(DefaultCADTool tool) {
 		previousTool=tool;
 	}
 	public void restorePreviousTool() {
 		CADExtension.setCADTool(previousTool.toString(), true);
 		PluginServices.getMainFrame().setSelectedTool(previousTool.toString());
 	}
 	public void endTransition(double x, double y, MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
