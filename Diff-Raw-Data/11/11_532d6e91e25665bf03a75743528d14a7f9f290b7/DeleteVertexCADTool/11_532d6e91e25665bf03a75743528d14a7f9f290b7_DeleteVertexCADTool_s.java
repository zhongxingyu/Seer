 /*
  * 
  * Copyright 2009 Deputacin Provincial de Pontevedra
  * Copyright 2010 CartoLab, Universidad de A Corua
  *
  * This file is part of openCADTools, developed by the Cartography
  * Engineering Laboratory of the University of A Corua (CartoLab).
  * http://www.cartolab.es
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
  */
 package com.iver.cit.gvsig.gui.cad.tools;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.InputEvent;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;

 import java.util.ArrayList;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
 import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
 import com.iver.cit.gvsig.fmap.ViewPort;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
 import com.iver.cit.gvsig.fmap.core.FPoint2D;
 import com.iver.cit.gvsig.fmap.core.FPolygon2D;
 import com.iver.cit.gvsig.fmap.core.FPolyline2D;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.Handler;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.IRow;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.core.v02.FGraphicUtilities;
 import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.gui.cad.exception.CommandException;
 import com.iver.cit.gvsig.gui.cad.tools.smc.DeleteVertexCADToolContext;
 import com.iver.cit.gvsig.gui.cad.tools.smc.DeleteVertexCADToolContext.DeleteVertexCADToolState;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Polygon;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Vicente Caballero Navarro
  * @author Francisco Puga <fpuga (at) cartolab.es>
  */
 public class DeleteVertexCADTool extends DefaultCADTool {
 	private DeleteVertexCADToolContext _fsm;
 	private int numSelect=0;
 	private int numHandlers;
 	private boolean addVertex=false;
 
 	/**
 	 * Crea un nuevo PolylineCADTool.
 	 */
 	public DeleteVertexCADTool() {
 	}
 
 	/**
 	 * Mtodo de incio, para poner el cdigo de todo lo que se requiera de una
 	 * carga previa a la utilizacin de la herramienta.
 	 */
 	public void init() {
 		_fsm = new DeleteVertexCADToolContext(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, double, double)
 	 */
 	public void transition(double x, double y, InputEvent event) {
 		_fsm.addPoint(x, y, event);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, double)
 	 */
 	public void transition(double d) {
 		_fsm.addValue(d);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, java.lang.String)
 	 */
 	public void transition(String s) throws CommandException {
 		if (!super.changeCommand(s)){
 			_fsm.addOption(s);
 		}
 	}
 
 	@Override
 	public void transition(InputEvent event) {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	public void selection() {
 
 		ArrayList selectedRow=getSelectedRows();
 		if (selectedRow.size() == 0 && !CADExtension.getCADTool().getClass().getName().equals("com.iver.cit.gvsig.gui.cad.tools.SelectionCADTool")) {
 			CADExtension.setCADTool("_selection",false);
 			((SelectionCADTool) CADExtension.getCADTool()).setNextTool("_deleteVertex");
 		}
 
 	}
 
 	/**
 	 * Equivale al transition del prototipo pero sin pasarle como parmetro el
 	 * editableFeatureSource que ya estar creado.
 	 *
 	 * @param x parmetro x del punto que se pase en esta transicin.
 	 * @param y parmetro y del punto que se pase en esta transicin.
 	 */
 	public void addPoint(double x, double y,InputEvent event) {
 
 		if (!isHandler(x,y)) {
 			CADExtension.setCADTool("_selection", false);
 			((SelectionCADTool) CADExtension.getCADTool()).setNextTool("_deleteVertex");
 			CADExtension.getCADTool().transition(x, y, event);
 		} else {
 			selectHandler(x,y);
 			addVertex=false;
 		}
 
 	}
 
 	private IGeometry getSelectedGeometry() {
 
 		ArrayList selectedRows=getSelectedRows();
 		if (selectedRows.size()==1){
 			DefaultRowEdited row = (DefaultRowEdited) selectedRows.get(0);
 			IGeometry ig = ((IFeature)row.getLinkedRow()).getGeometry().cloneGeometry();
 			return ig;
 		}
 		return null;
 
 	}
 
 	/**
 	 * Mtodo para dibujar la lo necesario para el estado en el que nos
 	 * encontremos.
 	 *
 	 * @param g Graphics sobre el que dibujar.
 	 * @param x parmetro x del punto que se pase para dibujar.
 	 * @param y parmetro x del punto que se pase para dibujar.
 	 */
 	public void drawOperation(Graphics g, double x, double y) {
 
 		try {
 			drawVertex(g,getCadToolAdapter().getMapControl().getViewPort());
 		} catch (DriverIOException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		}
 //
 //		IGeometry ig = getSelectedGeometry();
 //		if (ig != null) {
 //			Handler[] h = ig.getHandlers(IGeometry.SELECTHANDLER);
 //			FGraphicUtilities.DrawHandlers((Graphics2D) g,
 //										new AffineTransform(), h, DefaultCADTool.drawingSymbol);
 //		} else {
 //			System.out.println("Selected Geometry == null");
 //		}
 
 	}
 
 	/**
 	 * Add a diferent option.
 	 *
 	 * @param s Diferent option.
 	 */
 	public void addOption(String s) {
 
 		DeleteVertexCADToolState actualState = (DeleteVertexCADToolState) _fsm.getPreviousState();
 		String status = actualState.getName();
 		VectorialLayerEdited vle=getVLE();
 		VectorialEditableAdapter vea = vle.getVEA();
 		ArrayList selectedRows=vle.getSelectedRow();
 		IRowEdited row=null;
 		IGeometry ig=null;
 		Handler[] handlers=null;
 		if (selectedRows.size()==1){
 			row =  (DefaultRowEdited) selectedRows.get(0);
 			ig=((IFeature)row.getLinkedRow()).getGeometry().cloneGeometry();
 			handlers=ig.getHandlers(IGeometry.SELECTHANDLER);
 			numHandlers=handlers.length;
 			if (numHandlers ==0){
 
 					try {
 						vea.removeRow(row.getIndex(),getName(),EditionEvent.GRAPHIC);
 					} catch (ExpansionFileReadException e) {
 						NotificationManager.addError(e.getMessage(),e);
 					} catch (ReadDriverException e) {
 						NotificationManager.addError(e.getMessage(),e);
 					}
 
 			}
 		}
 		int dif=1;//En el caso de ser polgono.
 		if (ig instanceof FGeometryCollection){
 			dif=2;
 		}
 		if (status.equals("DeleteVertex.SelectVertexOrDelete")){
 			if(s.equals("s") || s.equals("S") || s.equals(PluginServices.getText(this,"next"))){
 				numSelect=numSelect-dif;
 				if (numSelect<0){
 					numSelect=numHandlers-1+(numSelect+1);
 				}
 			}else if(s.equals("a") || s.equals("A") || s.equals(PluginServices.getText(this,"previous"))){
 				numSelect=numSelect+dif;
 				if (numSelect>(numHandlers-1)){
 					numSelect=numSelect-(numHandlers);
 				}
 
 			}else
 if(s.equals("e") || s.equals("E") || s.equals(PluginServices.getText(this,"del"))){
 				if (handlers!=null){
 					IGeometry newGeometry=null;
 					if (ig instanceof FGeometryCollection) {
 						newGeometry=removeVertexGC((FGeometryCollection)ig,handlers[numSelect]);
 					}else {
 						newGeometry=removeVertex(ig,handlers,numSelect);
 					}
 					//numSelect=0;
 
 					IRow newRow=new DefaultFeature(newGeometry,row.getAttributes(),row.getID());
 
 						try {
 							vea.modifyRow(row.getIndex(),newRow,getName(),EditionEvent.GRAPHIC);
 							clearSelection();
 						} catch (ExpansionFileWriteException e) {
 							NotificationManager.addError(e.getMessage(),e);
 						} catch (ExpansionFileReadException e) {
 							NotificationManager.addError(e.getMessage(),e);
 						} catch (ValidateRowException e) {
 							NotificationManager.addError(e.getMessage(),e);
 						} catch (ReadDriverException e) {
 							NotificationManager.addError(e.getMessage(),e);
 						}
 
 					selectedRows.add(new DefaultRowEdited(newRow, IRowEdited.STATUS_MODIFIED, row.getIndex()));
 //					vle.refreshSelectionCache(new Point2D.Double(0,0),getCadToolAdapter());
 //					refresh();
 
 				}
 			}else if(s.equals("i") || s.equals("I") || s.equals(PluginServices.getText(this,"add"))){
 				addVertex=true;
 			}
 		}
 	}
 
 	private void drawVertex(Graphics g,ViewPort vp) throws DriverIOException{
 
 		ArrayList selectedRows=getSelectedRows();
 		for (int i = 0; i<selectedRows.size(); i++) {
 			DefaultFeature fea = (DefaultFeature) ((DefaultRowEdited) selectedRows
 					.get(i)).getLinkedRow();
 			IGeometry ig = fea.getGeometry().cloneGeometry();
 			ig.drawInts((Graphics2D)g, vp, DefaultCADTool.drawingSymbol);
 			if (ig == null) {
 				continue;
 			}
 			Handler[] handlers = ig.getHandlers(IGeometry.SELECTHANDLER);
 			if (numSelect>=handlers.length) {
 				numSelect = 0;
 			}
 			//FGraphicUtilities.DrawVertex((Graphics2D)g,vp.getAffineTransform(),handlers[numSelect]);
 			FGraphicUtilities.DrawHandlers((Graphics2D)g,vp.getAffineTransform(),handlers, DefaultCADTool.drawingSymbol);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#addvalue(double)
 	 */
 	public void addValue(double d) {
 	}
 
 
 	private IGeometry removeVertex(IGeometry gp,Handler[] handlers,int numHandler) {
 
 		boolean isPolygon = false;
 		//TODO CHECK PROPERLY THIS (support multigeometries)
 		System.out.println(gp.toJTSGeometry().getNumPoints() + "  type: "+ gp.getGeometryType() + "  numGeoms: " + gp.toJTSGeometry().getNumGeometries() + "  numCoords: " + gp.toJTSGeometry().getCoordinates().length);
 		if (gp.getGeometryType() == FShape.POLYGON){
 			isPolygon = true;
 			if (gp.toJTSGeometry().buffer(0).getNumPoints() < 5){
 				System.out.println("Polygon has 3 vertex. Operation delete_vertex not permited.");
 				return gp;
 			}
 		}
 
 		if (gp.getGeometryType() == FShape.LINE){
 			if (gp.toJTSGeometry().getNumPoints() < 3){
 				System.out.println("Line has 3 vertex. Operation delete_vertex not permited.");
 				return gp;
 			}
 		}
 
 
 		GeneralPathX newGp = new GeneralPathX();
 		double[] theData = new double[6];
 
 		PathIterator theIterator;
 		int theType;
 		int numParts = 0;
 
 		Point2D ptSrc = new Point2D.Double();
 		boolean bFirst = false;
 
 		int numGeom = gp.toJTSGeometry().getNumGeometries();
 
 		if (numGeom>1) {
 			//multigeometry
 			int changedGeom = -1;
 			int totalHandlers = 0;
 			IGeometry ig = null;
 			Handler[] auxHandlers = null;
 			for (int i=0; i<numGeom; i++) {
 				Geometry g = gp.toJTSGeometry().getGeometryN(i);
 				if (isPolygon) {
 					g = g.buffer(0);
 				}
 				int numHandlers = g.getNumPoints();
 				if (numHandler < totalHandlers + numHandlers) {
 					//the point to be removed is in the geometry
 					//get the handlers and recall this function to get out
 					//the point of this geometry
 					auxHandlers = new Handler[numHandlers];
 					for (int j=0; j<numHandlers; j++) {
 						auxHandlers[j] = handlers[totalHandlers + j];
 					}
 					int auxNumHandler = numHandler - (totalHandlers);
 					ig = removeVertex(FConverter.jts_to_igeometry(g), auxHandlers, auxNumHandler);
 					changedGeom = i;
 					break;
 				}
 				totalHandlers += numHandlers;
 
 			}
 
 			//get the new geometry by building a multi geometry with
 			//separated subgeometries.
 			Geometry geom = gp.toJTSGeometry();
 			Geometry final_geom;
 			if (isPolygon) {
 				//multipolygon
 				Polygon[] polygons = new Polygon[geom.getNumGeometries()];
 				for (int i=0; i<geom.getNumGeometries(); i++) {
 					if (i!=changedGeom) {
 						polygons[i] = (Polygon) geom.getGeometryN(i);
 					} else {
 						polygons[i] = (Polygon) ig.toJTSGeometry().buffer(0);
 					}
 				}
 				final_geom = new MultiPolygon(polygons, new GeometryFactory());
 
 			} else {
 				//multiline
 				LineString[] lines = new LineString[geom.getNumGeometries()];
 				for (int i=0; i<geom.getNumGeometries(); i++) {
 					if (i!=changedGeom) {
 						lines[i] = (LineString) geom.getGeometryN(i);
 					} else {
 						lines[i] = (LineString) ((MultiLineString) ig.toJTSGeometry()).getGeometryN(0);
 					}
 				}
 				final_geom = new MultiLineString(lines, new GeometryFactory());
 			}
 			return FConverter.jts_to_igeometry(final_geom);
 
 
 		}
 
 		theIterator = gp.getPathIterator(null, FConverter.FLATNESS);
 		int numSegmentsAdded = 0;
 		while (!theIterator.isDone()) {
 			theType = theIterator.currentSegment(theData);
 			if (bFirst){
 				newGp.moveTo(theData[0], theData[1]);
 				numSegmentsAdded++;
 				bFirst=false;
 				theIterator.next();
 				continue;
 			}
 			switch (theType) {
 
 			case PathIterator.SEG_MOVETO:
 				numParts++;
 				ptSrc.setLocation(theData[0], theData[1]);
 				if (ptSrc.equals(handlers[numHandler].getPoint())){
 					numParts--;
 					bFirst=true;
 					break;
 				}
 				newGp.moveTo(ptSrc.getX(), ptSrc.getY());
 				numSegmentsAdded++;
 				bFirst = false;
 				break;
 
 			case PathIterator.SEG_LINETO:
 				ptSrc.setLocation(theData[0], theData[1]);
 				if (ptSrc.equals(handlers[numHandler].getPoint())){
 					break;
 				}
 				newGp.lineTo(ptSrc.getX(), ptSrc.getY());
 				bFirst = false;
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_QUADTO:
 				newGp.quadTo(theData[0], theData[1], theData[2], theData[3]);
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_CUBICTO:
 				newGp.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
 				numSegmentsAdded++;
 				break;
 
 			case PathIterator.SEG_CLOSE:
 				if (numSegmentsAdded < 3)
 					newGp.lineTo(theData[0], theData[1]);
 				newGp.closePath();
 
 				break;
 			} //end switch
 
 			theIterator.next();
 		} //end while loop
 		FShape shp = null;
 		switch (gp.getGeometryType())
 		{
 		case FShape.POINT: //Tipo punto
 		case FShape.POINT + FShape.Z:
 			shp = new FPoint2D(ptSrc.getX(), ptSrc.getY());
 			break;
 
 		case FShape.LINE:
 		case FShape.LINE + FShape.Z:
 			shp = new FPolyline2D(newGp);
 			break;
 		case FShape.POLYGON:
 		case FShape.POLYGON + FShape.Z:
 			//if first/last point is removed, we must ensure there's new first point has the
 			//same coordinates than the new last point.
			if (numHandler == 0 || numHandler==handlers.length-1) {
				newGp.lineTo(handlers[1].getPoint().getX(), handlers[1].getPoint().getY());
 				newGp.closePath();
 			}
 			shp = new FPolygon2D(newGp);
 			break;
 		}
 
 		IGeometry ig=ShapeFactory.createGeometry(shp);
 		int dif=1;//En el caso de ser polgono.
 		numSelect=numSelect-dif;
 		if (numSelect<0){
 			numSelect=numHandlers-1+(numSelect+1);
 		}
 		return ig;
 	}
 
 	private IGeometry removeVertexGC(FGeometryCollection gc,Handler handler) {
 
 		IGeometry[] geoms=gc.getGeometries();
 		ArrayList geomsAux=new ArrayList();
 		int pos=-1;
 		for (int i=0;i<geoms.length;i++) {
 			Handler[] handlers=geoms[i].getHandlers(IGeometry.SELECTHANDLER);
 			for (int j=0;j<handlers.length;j++) {
 				if (handlers[j].equalsPoint(handler)) {
 					geomsAux.add(geoms[i]);
 					if (pos==-1)
 						pos=i;
 				}
 			}
 		}
 		int numGeomsAux=geomsAux.size();
 		GeneralPathX gpx=new GeneralPathX();
 		for (int i=0;i<numGeomsAux;i++) {
 			Handler[] handlers=((IGeometry)geomsAux.get(i)).getHandlers(IGeometry.SELECTHANDLER);
 			if (numGeomsAux == 2) {
 				for (int j = 0; j < handlers.length; j++) {
 					if (handlers[j].equalsPoint(handler)) {
 						if (j == (handlers.length - 1)) {
 							Point2D ph = handlers[0].getPoint();
 							gpx.moveTo(ph.getX(), ph.getY());
 						} else {
 							Point2D ph = handlers[handlers.length - 1]
 							                      .getPoint();
 							gpx.lineTo(ph.getX(), ph.getY());
 						}
 					}
 				}
 			}
 		}
 
 		ArrayList newGeoms=new ArrayList();
 		for (int i=0;i<pos;i++) {
 			newGeoms.add(geoms[i]);
 		}
 		newGeoms.add(ShapeFactory.createPolyline2D(gpx));
 		for (int i=pos+numGeomsAux;i<geoms.length;i++) {
 			newGeoms.add(geoms[i]);
 		}
 
 		return new FGeometryCollection((IGeometry[])newGeoms.toArray(new IGeometry[0]));
 	}
 
 
 //	private IGeometry addVertex(IGeometry geome,Point2D p,Rectangle2D rect) {
 //		IGeometry geometryCloned=geome.cloneGeometry();
 //		IGeometry geom1=null;
 //		GeneralPathX gpxAux;
 //		boolean finish=false;
 //		//FGeometry geom2=null;
 //
 //		//if (geometryCloned.getGeometryType() == FShape.POLYGON){
 //		/////////////////
 //
 //		GeneralPathX newGp = new GeneralPathX();
 //		double[] theData = new double[6];
 //
 //		PathIterator theIterator;
 //		int theType;
 //		int numParts = 0;
 //		Point2D pLast=new Point2D.Double();
 //		Point2D pAnt = new Point2D.Double();
 //		Point2D firstPoint=null;
 //		theIterator = geome.getPathIterator(null,FConverter.FLATNESS); //, flatness);
 //		int numSegmentsAdded = 0;
 //		while (!theIterator.isDone()) {
 //			theType = theIterator.currentSegment(theData);
 //			switch (theType) {
 //			case PathIterator.SEG_MOVETO:
 //				pLast.setLocation(theData[0], theData[1]);
 //				if (numParts==0)
 //					firstPoint=(Point2D)pLast.clone();
 //				numParts++;
 //
 //				gpxAux=new GeneralPathX();
 //				gpxAux.moveTo(pAnt.getX(),pAnt.getY());
 //				gpxAux.lineTo(pLast.getX(),pLast.getY());
 //				geom1=ShapeFactory.createPolyline2D(gpxAux);
 //				if (geom1.intersects(rect)){
 //					finish=true;
 //					newGp.moveTo(pLast.getX(), pLast.getY());
 //					//newGp.lineTo(pLast.getX(),pLast.getY());
 //				}else{
 //					newGp.moveTo(pLast.getX(), pLast.getY());
 //				}
 //				pAnt.setLocation(pLast.getX(), pLast.getY());
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_LINETO:
 //				pLast.setLocation(theData[0], theData[1]);
 //				gpxAux=new GeneralPathX();
 //				gpxAux.moveTo(pAnt.getX(),pAnt.getY());
 //				gpxAux.lineTo(pLast.getX(),pLast.getY());
 //				geom1=ShapeFactory.createPolyline2D(gpxAux);
 //				if (geom1.intersects(rect)){
 //					newGp.lineTo(p.getX(), p.getY());
 //					newGp.lineTo(pLast.getX(),pLast.getY());
 //				}else{
 //					newGp.lineTo(pLast.getX(), pLast.getY());
 //				}
 //				pAnt.setLocation(pLast.getX(), pLast.getY());
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_QUADTO:
 //				newGp.quadTo(theData[0], theData[1], theData[2], theData[3]);
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_CUBICTO:
 //				newGp.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
 //				numSegmentsAdded++;
 //				break;
 //
 //			case PathIterator.SEG_CLOSE:
 //				//if (numSegmentsAdded < 3){
 //					gpxAux=new GeneralPathX();
 //					gpxAux.moveTo(pAnt.getX(),pAnt.getY());
 //					gpxAux.lineTo(firstPoint.getX(),firstPoint.getY());
 //					geom1=ShapeFactory.createPolyline2D(gpxAux);
 //					if (geom1.intersects(rect)|| finish){
 //						newGp.lineTo(p.getX(), p.getY());
 //						newGp.lineTo(pLast.getX(),pLast.getY());
 //					}else{
 //						newGp.lineTo(pLast.getX(), pLast.getY());
 //					}
 //					//}
 //					newGp.closePath();
 //					break;
 //			} //end switch
 //
 //			theIterator.next();
 //		} //end while loop
 //		FShape shp = null;
 //		switch (geometryCloned.getGeometryType())
 //		{
 //		case FShape.POINT: //Tipo punto
 //		case FShape.POINT + FShape.Z:
 //			shp = new FPoint2D(pLast.getX(), pLast.getY());
 //			break;
 //
 //		case FShape.LINE:
 //		case FShape.LINE + FShape.Z:
 //			shp = new FPolyline2D(newGp);
 //			break;
 //		case FShape.POLYGON:
 //		case FShape.POLYGON + FShape.Z:
 //		case FShape.CIRCLE:
 //		case FShape.ELLIPSE:
 //			shp = new FPolygon2D(newGp);
 //			break;
 //		}
 //		return ShapeFactory.createGeometry(shp);
 //
 //	}
 
 //	private IGeometry addVertexGC(FGeometryCollection gc,Point2D p,Rectangle2D rect) {
 //		IGeometry[] geoms=gc.getGeometries();
 //		int pos=-1;
 //		for (int i=0;i<geoms.length;i++) {
 //			if (geoms[i].intersects(rect)) {
 //				pos=i;
 //			}
 //		}
 //		ArrayList newGeoms=new ArrayList();
 //		for (int i=0;i<pos;i++) {
 //			newGeoms.add(geoms[i]);
 //		}
 //		if (pos!=-1) {
 //			GeneralPathX gpx1=new GeneralPathX();
 //			GeneralPathX gpx2=new GeneralPathX();
 //			Handler[] handlers=geoms[pos].getHandlers(IGeometry.SELECTHANDLER);
 //			Point2D p1=handlers[0].getPoint();
 //			Point2D p2=p;
 //			Point2D p3=handlers[handlers.length-1].getPoint();
 //			gpx1.moveTo(p1.getX(),p1.getY());
 //			gpx1.lineTo(p2.getX(),p2.getY());
 //			gpx2.moveTo(p2.getX(),p2.getY());
 //			gpx2.lineTo(p3.getX(),p3.getY());
 //			newGeoms.add(ShapeFactory.createPolyline2D(gpx1));
 //			newGeoms.add(ShapeFactory.createPolyline2D(gpx2));
 //			for (int i=pos+1;i<geoms.length;i++) {
 //				newGeoms.add(geoms[i]);
 //			}
 //			return new FGeometryCollection((IGeometry[])newGeoms.toArray(new IGeometry[0]));
 //		}else {
 //			return null;
 //		}
 //	}
 
 
 	public String getName() {
 		return PluginServices.getText(this,"delete_vertex_");
 	}
 
 	/**
 	 * returns
 	 * @param x
 	 * @param y
 	 * @return true if theres a handler on the selected geometry in coords x,y; false otherwise.
 	 */
 	private boolean isHandler(double x, double y) {
 
 		Point2D firstPoint = new Point2D.Double(x, y);
 		double tam = getCadToolAdapter().getMapControl().getViewPort().toMapDistance(SelectionCADTool.tolerance);
 
 		IGeometry geometry = getSelectedGeometry();
 
 		if (geometry == null) {
 			return false;
 		}
 
 		Handler[] handlers = geometry
 		.getHandlers(IGeometry.SELECTHANDLER);
 		for (int h = 0; h < handlers.length; h++) {
 			if (handlers[h].getPoint().distance(firstPoint) < tam) {
 				return true;
 			}
 		}
 
 		return false;
 
 	}
 
 
 	private void selectHandler(double x, double y) {
 
 		Point2D firstPoint = new Point2D.Double(x, y);
 		VectorialLayerEdited vle = getVLE();
 		VectorialEditableAdapter vea=vle.getVEA();
 		ArrayList selectedRows = vle.getSelectedRow();
 		double tam = getCadToolAdapter().getMapControl().getViewPort().toMapDistance(SelectionCADTool.tolerance);
 
 		Rectangle2D rect = new Rectangle2D.Double(firstPoint.getX() - tam,
 				firstPoint.getY() - tam, tam * 2, tam * 2);
 
 		if (selectedRows.size() < 1) {
 			return;
 		}
 
 		boolean isSelectedHandler = false;
 		IGeometry geometry = getSelectedGeometry();
 
 		if (geometry == null) {
 			return;
 		}
 
 		Handler[] handlers = geometry
 		.getHandlers(IGeometry.SELECTHANDLER);
 		for (int h = 0; h < handlers.length; h++) {
 			if (handlers[h].getPoint().distance(firstPoint) < tam) {
 				numSelect = h;
 				isSelectedHandler = true;
 			}
 		}
 
 		if (!isSelectedHandler) {
 			System.out.println("Handler not selected!!!");
 		} else {
 			System.out.println(">>>>>>> Handler selected: " + numSelect);
 
 
 			// Copied from Delete option of editVertexExtension
 			selectedRows = getSelectedRows();
 			DefaultRowEdited row = null;
 			IGeometry ig=null;
 			IGeometry newGeometry = null;
 
 			if (selectedRows.size()==1){
 				row =  (DefaultRowEdited) selectedRows.get(0);
 				ig=((IFeature)row.getLinkedRow()).getGeometry().cloneGeometry();
 				handlers=ig.getHandlers(IGeometry.SELECTHANDLER);
 				numHandlers=handlers.length;
 				if (numHandlers ==0){
 
 						try {
 							vea.removeRow(row.getIndex(),getName(),EditionEvent.GRAPHIC);
 						} catch (ExpansionFileReadException e) {
 							NotificationManager.addError(e.getMessage(),e);
 						} catch (ReadDriverException e) {
 							NotificationManager.addError(e.getMessage(),e);
 						}
 				}
 			}
 
 			if (ig == null){
 				return;
 			}
 
 
 
 			if (ig instanceof FGeometryCollection) {
 				newGeometry=removeVertexGC((FGeometryCollection)ig,handlers[numSelect]);
 			}else {
 				newGeometry=removeVertex(ig,handlers,numSelect);
 			}
 
 
 			IRow newRow=new DefaultFeature(newGeometry,row.getAttributes(),row.getID());
 
 				try {
 					vea.modifyRow(row.getIndex(),newRow,getName(),EditionEvent.GRAPHIC);
 					clearSelection();
 				} catch (ExpansionFileWriteException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				} catch (ExpansionFileReadException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				} catch (ValidateRowException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				} catch (ReadDriverException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				}
 
 			selectedRows.add(new DefaultRowEdited(newRow, IRowEdited.STATUS_MODIFIED, row.getIndex()));
 
 //			vle.refreshSelectionCache(new Point2D.Double(0,0),getCadToolAdapter());
 //			refresh();
 			System.out.println(">>>>>>> Vertex Removed !!!!!!! ");
 		}
 
 	}
 
 	public String toString() {
 		return "_deleteVertex";
 	}
 
 	public boolean isApplicable(int shapeType) {
 		switch (shapeType) {
 		case FShape.POINT:
 		case FShape.MULTIPOINT:
 			return false;
 		}
 		return true;
 	}
 
 	public void drawOperation(Graphics g, ArrayList pointList) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public boolean isMultiTransition() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void setMultiTransition(boolean condicion) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setPreviousTool(DefaultCADTool tool) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
