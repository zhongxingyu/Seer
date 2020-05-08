 /*
  * Copyright 2008 Deputacin Provincial de A Corua
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
 import java.util.ArrayList;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.gui.cad.CADTool;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.gui.cad.exception.CommandException;
 import com.iver.cit.gvsig.gui.cad.tools.smc.MultiPolylineCADToolContext;
 import com.iver.cit.gvsig.gui.cad.tools.smc.MultiPolylineCADToolContext.MultiLineaCADToolState;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Isabel Prez-Urria Lage [LBD]
  * @author Javier Estvez [Cartolab]
  */
 public class MultiPolylineCADTool extends InsertionCADTool {
 	private MultiPolylineCADToolContext _fsm;
 	private Point2D firstPoint;
 	private Point2D antPoint;
 	private ArrayList list = new ArrayList();
 	private ArrayList points = new ArrayList();
 	private int numLines;
 
 	/**
 	 * Index of the last feature introduced in VEA.
 	 */
 	private Integer virtualIndex;
 
 	/**
 	 * Mtodo de incio, para poner el cdigo de todo lo que se requiera de una
 	 * carga previa a la utilizacin de la herramienta.
 	 */
 	public void init() {
 	// clear();
 	if (_fsm == null) {
 	    _fsm = new MultiPolylineCADToolContext(this);
 	}
 //		con esto limpio el ultimo punto pulsado para reinicializar el seguimiento de
 //		los snappers
 		getCadToolAdapter().setPreviousPoint((double[])null);
 	}
 
 	public void clear() {
 		super.init();
 		this.setMultiTransition(true);
 		points.clear();
 		list.clear();
 		firstPoint = null;
 		antPoint = null;
 		numLines = 0;
 	virtualIndex = null;
 	_fsm = new MultiPolylineCADToolContext(this);
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
 			if(s.equals(PluginServices.getText(this, "removePoint"))) {
 				_fsm.removePoint(null, points.size());
 			} else {
 				_fsm.addOption(s);
 			}
 		}
 	}
 
 	public void transition(InputEvent event){
 		_fsm.removePoint(event, points.size());
 	}
 
 
 
 	public void saveTempGeometry(){
 		IGeometry[] geoms = (IGeometry[]) list.toArray(new IGeometry[0]);
 		FGeometryCollection fgc = new FGeometryCollection(geoms);
 		VectorialEditableAdapter vea = getVLE().getVEA();
 		GeneralPathX gp = new GeneralPathX();
 		gp.append(fgc.getPathIterator(null,FConverter.FLATNESS), true);
 		IRowEdited row = null;
 
 		try {
 			//Ya se ha insertado alguna lnea en memoria
 			if(virtualIndex != null){
 				if(numLines != 0){
 				    row = vea.getRow(virtualIndex.intValue());
 					IFeature feat = (IFeature) row.getLinkedRow().cloneRow();
 					IGeometry geometry = feat.getGeometry();
 					GeneralPathX currentGp = getCurrentPath(geometry);
 					currentGp.append(gp.getPathIterator(null,FConverter.FLATNESS), false);
 					feat.setGeometry(ShapeFactory.createPolyline2D(currentGp));
 					modifyFeature(virtualIndex.intValue(), feat);
 				}
 				//Es la primera linea que se inserta en memoria
 				}else{
 					addGeometry(ShapeFactory.createPolyline2D(gp));
 					virtualIndex = new Integer(vea.getRowCount()-1);
 			}
 			numLines++;
 		} catch (ExpansionFileReadException e) {
 			NotificationManager.addError(e.getMessage(), e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(), e);
 		}
 
 	}
 
 
 	/**
 	 * Obtiene la geometria actual a partir de los puntos introducidos
 	 * */
 	public IGeometry getCurrentGeom(){
 		VectorialEditableAdapter vea = getVLE().getVEA();
 		IGeometry[] geoms = (IGeometry[]) list.toArray(new IGeometry[0]);
 		FGeometryCollection fgc = new FGeometryCollection(geoms);
 		GeneralPathX gp = new GeneralPathX();
 		gp.append(fgc.getPathIterator(null,FConverter.FLATNESS), true);
 
 		IGeometry geom = null;
 		try {
 			if(virtualIndex != null){
 				if(numLines != 0){
 					IRowEdited row;
 					row = vea.getRow(virtualIndex.intValue());
 					IGeometry geometry =  ((IFeature)row.getLinkedRow().cloneRow()).getGeometry();
 					GeneralPathX currentGp = getCurrentPath(geometry);
 					currentGp.append(gp.getPathIterator(null,FConverter.FLATNESS), false);
 					geom = ShapeFactory.createPolyline2D(currentGp);
 				}
 			}else{
 				geom = ShapeFactory.createPolyline2D(gp);
 			}
 		} catch (ExpansionFileReadException e) {
 			NotificationManager.addError(e.getMessage(), e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(), e);
 		}
 
 		return geom;
 	}
 
 
 	/**
 	 * Accin que se ejecuta cuando cancelamos el formulario de insercin.
 	 * Borra la ltima fila aadida al VectorialEditableAdapter. Si slo
 	 * hay una lnea se elimina la fila completa del VectorialEditableAdapter.
 	 * */
 	public void cancelInsertion(){
 		VectorialLayerEdited vle=getVLE();
 		VectorialEditableAdapter vea = vle.getVEA();
 		IRowEdited row=null;
 		try {
 			if(numLines > 1){
 			    row = vea.getRow(virtualIndex.intValue());
 				IFeature feat = (IFeature) row.getLinkedRow().cloneRow();
 				IGeometry geometry = feat.getGeometry();
 				geometry = removeLastShape(geometry);
 				feat.setGeometry(geometry);
 				modifyFeature(virtualIndex.intValue(), feat);
 			}else{
 				getCadToolAdapter().delete(virtualIndex.intValue());
 				virtualIndex = null;
 			}
 			numLines--;
 		} catch (ExpansionFileReadException e) {
 			NotificationManager.addError(e.getMessage(), e);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(), e);
 		}
 
 //		con esto limpio el ultimo punto pulsado para reinicializar el seguimiento de
 //		los snappers
 		getCadToolAdapter().setPreviousPoint((double[])null);
 	}
 
 
 
 
 	/**
 	 * Equivale al transition del prototipo pero sin pasarle como parmetro el
 	 * editableFeatureSource que ya estar creado.
 	 *
 	 * @param sel Bitset con las geometras que estn seleccionadas.
 	 * @param x parmetro x del punto que se pase en esta transicin.
 	 * @param y parmetro y del punto que se pase en esta transicin.
 	 */
 	public void addPoint(double x, double y,InputEvent event) {
 		MultiLineaCADToolState actualState = (MultiLineaCADToolState) _fsm.getPreviousState();
 		String status = actualState.getName();
 
 		//System.out.println("Accin addPoint, estado= " + status);
 		if (status.equals("MultiLinea.NextPoint") || status.equals("MultiLinea.FirstPoint")||
 				status.equals("MultiLinea.SecondPoint")) {
 
 			//Primer punto
 			if (firstPoint == null) {
 				firstPoint = new Point2D.Double(x, y);
 			}
 			Point2D point = new Point2D.Double(x, y);
 
 			//No es el primer punto
 			if (antPoint != null) {
 				GeneralPathX elShape = new GeneralPathX(GeneralPathX.WIND_EVEN_ODD,
 						2);
 				elShape.moveTo(antPoint.getX(), antPoint.getY());
 				elShape.lineTo(point.getX(), point.getY());
 				list.add(ShapeFactory.createPolyline2D(elShape));
 
 			}
 			//Reasigno las referencias
 			//Primer punto
 			if (antPoint==null)
 				antPoint = (Point2D)firstPoint.clone();
 
 			antPoint = point;
 
 			points.add(point);
 		}
 	}
 
 
 	public void removePoint(InputEvent event) {
 		MultiLineaCADToolState actualState = (MultiLineaCADToolState) _fsm.getPreviousState();
 		String status = actualState.getName();
 
 		//System.out.println("Accin removePoint, estado= " + status);
 		if (status.equals("MultiLinea.SecondPoint")){
 			if(virtualIndex == null){
 //				prueba para actualizar el ultimo punto pulsado
 				getCadToolAdapter().setPreviousPoint((double[])null);
 				cancel();
 			}
 			else
 				clearPoints();
 		}else if(status.equals("MultiLinea.NextPoint")){
 //			System.out.println("Numero de coordenadas antes de borrar: " + points.size());
 			if(points.size() == 2){
 				getCadToolAdapter().setPreviousPoint((Point2D)points.get(points.size()-2));
 				list.remove(list.size()-1);
 				points.remove(points.size()-1);
 				antPoint =(Point2D)points.get(points.size()-1);
 			}else{
 //				prueba para actualizar el ultimo punto pulsado
 				getCadToolAdapter().setPreviousPoint((Point2D)points.get(points.size()-2));
 
 				list.remove(list.size()-1);
 				points.remove(points.size()-1);
 				antPoint = (Point2D)points.get(points.size()-1);
 			}
 		}
 
 	}
 
 
 	/**
 	 * Inicializa las constantes correspondientes a la lnea
 	 * que se est digitalizandi actualmente
 	 * */
 	public void clearPoints(){
 		points.clear();
 		list.clear();
 		firstPoint = antPoint = null;
 //		con esto limpio el ultimo punto pulsado para reinicializar el seguimiento de
 //		los snappers
 		getCadToolAdapter().setPreviousPoint((double[])null);
 	}
 
 
 	/**
 	 * Accin que abre el formulario de edicin de las propiedades para
 	 * el punto introducido
 	 */
 //	public void openForm(){
 ////		keys = openInsertEntityForm();
 //		if (keys.size() == 0){
 //			setFormState(InsertionCADTool.FORM_CANCELLED);
 //		}else{
 //			setFormState(InsertionCADTool.FORM_ACCEPTED);
 //		}
 //	}
 
 	/**
 	 * Accin que guarda en base de datos la geometra
 	 * digitalizada
 	 */
 	public void save(){
 //		insertGeometry(keys);
 //		_fsm = new MultiLineaCADToolContext(this);
 		initialize();
 	}
 
 
 	public void cancel(){
 		//Se ha insertado en vea una geometria
 		if((virtualIndex != null) && (numLines > 0)){
 			getCadToolAdapter().delete(virtualIndex.intValue());
 		}
 	// initialize();
 	clear();
 	}
 
 
 	private void initialize(){
 		list.clear();
 		points.clear();
 //		keys.clear();
 		antPoint=firstPoint=null;
 //		initializeFormState();
 		virtualIndex = null;
 		numLines = 0;
 //		con esto limpio el ultimo punto pulsado para reinicializar el seguimiento de
 //		los snappers
 		getCadToolAdapter().setPreviousPoint((double[])null);
 	}
 
 
 	//Obtiene el GeneralPathX a partir de la IGeometry
 	private GeneralPathX getCurrentPath(IGeometry gp) {
 
 		GeneralPathX newGp = new GeneralPathX();
 		double[] theData = new double[6];
 
 		PathIterator theIterator;
 		int theType;
 		int numParts = 0;
 
 
 		theIterator = gp.getPathIterator(null, FConverter.FLATNESS);
 		while (!theIterator.isDone()) {
 			theType = theIterator.currentSegment(theData);
 			switch (theType) {
 
 			case PathIterator.SEG_MOVETO:
 				numParts++;
 				newGp.moveTo(theData[0], theData[1]);
 				break;
 
 			case PathIterator.SEG_LINETO:
 				newGp.lineTo(theData[0], theData[1]);
 				break;
 
 			case PathIterator.SEG_QUADTO:
 				newGp.quadTo(theData[0], theData[1], theData[2], theData[3]);
 				break;
 
 			case PathIterator.SEG_CUBICTO:
 				newGp.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
 				break;
 
 			case PathIterator.SEG_CLOSE:
 				newGp.closePath();
 				break;
 			} //end switch
 
 			theIterator.next();
 		} //end while loop
 
 
 		return newGp;
 
 	}
 
 	private IGeometry removeLastShape(IGeometry gp) {
 
 		GeneralPathX newGp = new GeneralPathX();
 		double[] theData = new double[6];
 
 		PathIterator theIterator;
 		int theType;
 		int numParts = 0;
 		boolean endGeom = false;
 
 
 		theIterator = gp.getPathIterator(null, FConverter.FLATNESS);
 		while (!theIterator.isDone()) {
 			if(endGeom)
 				break;
 			theType = theIterator.currentSegment(theData);
 
 			switch (theType) {
 
 			case PathIterator.SEG_MOVETO:
 				numParts++;
 				if(numParts == numLines){
 					endGeom = true;
 					break;
 				}
 				newGp.moveTo(theData[0], theData[1]);
 				break;
 
 			case PathIterator.SEG_LINETO:
 				newGp.lineTo(theData[0], theData[1]);
 				break;
 
 			case PathIterator.SEG_QUADTO:
 				newGp.quadTo(theData[0], theData[1], theData[2], theData[3]);
 				break;
 
 			case PathIterator.SEG_CUBICTO:
 				newGp.curveTo(theData[0], theData[1], theData[2], theData[3], theData[4], theData[5]);
 				break;
 
 			case PathIterator.SEG_CLOSE:
 				newGp.closePath();
 				break;
 			} //end switch
 
 			theIterator.next();
 		} //end while loop
 
 		return  ShapeFactory.createPolyline2D(newGp);
 
 	}
 
 
 	public void drawOperation(Graphics g, ArrayList listaPuntos) {
 
 
 //		if (status.equals("MultiLinea.FirstPoint") || status.equals("MultiLinea.SecondPoint") ||
 //		status.equals("MultiLinea.NextPoint")) {
 		for (int i = 0; i < list.size(); i++) {
 			((IGeometry) list.get(i)).cloneGeometry().draw((Graphics2D) g,
 					getCadToolAdapter().getMapControl().getViewPort(),
 					CADTool.drawingSymbol);
 		}
 //		ahora debemos pintar las lineas que vienen en la lista
 		if(listaPuntos!=null&&listaPuntos.size()>1){
 			if (antPoint!=null){
 				Point2D puntoInicial = antPoint;
 				for(int i = 0; i<listaPuntos.size();i++){
 					Point2D puntoFinal = (Point2D) listaPuntos.get(i);
 					drawLine((Graphics2D) g, puntoInicial, puntoFinal);
 					puntoInicial = puntoFinal;
 //					pintamos los puntos para que se note el snapping
 					if(i<listaPuntos.size()-1){
 						Point2D actual = null;
 						actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(puntoInicial);
 						int sizePixels = 12;
 						int half = sizePixels / 2;
 						g.drawRect((int) (actual.getX() - half),
 								(int) (actual.getY() - half),
 								sizePixels, sizePixels);
 					}
 				}
 			}else{
 				Point2D puntoInicial = (Point2D) listaPuntos.get(0);
 				for(int i = 1; i<listaPuntos.size();i++){
 					Point2D puntoFinal = (Point2D) listaPuntos.get(i);
 					drawLine((Graphics2D) g, puntoInicial, puntoFinal);
 					puntoInicial = puntoFinal;
 
 //					pintamos los puntos para que se note el snapping
 					if(i<listaPuntos.size()-1){
 						Point2D actual = null;
 						actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(puntoInicial);
 						int sizePixels = 12;
 						int half = sizePixels / 2;
 						g.drawRect((int) (actual.getX() - half),
 								(int) (actual.getY() - half),
 								sizePixels, sizePixels);
 					}
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * Mtodo para dibujar la lo necesario para el estado en el que nos
 	 * encontremos.
 	 *
 	 * @param g Graphics sobre el que dibujar.
 	 * @param selectedGeometries BitSet con las geometras seleccionadas.
 	 * @param x parmetro x del punto que se pase para dibujar.
 	 * @param y parmetro x del punto que se pase para dibujar.
 	 */
 	public void drawOperation(Graphics g, double x,
 			double y) {
 //		MultiLineaCADToolState actualState = ((MultiLineaCADToolContext)_fsm).getState();
 //		String status = actualState.getName();
 
 
 //		if (status.equals("MultiLinea.FirstPoint") || status.equals("MultiLinea.SecondPoint") ||
 //		status.equals("MultiLinea.NextPoint")) {
 		for (int i = 0; i < list.size(); i++) {
 			((IGeometry) list.get(i)).cloneGeometry().draw((Graphics2D) g,
 					getCadToolAdapter().getMapControl().getViewPort(),
 					CADTool.drawingSymbol);
 		}
 		if (antPoint!=null) {
 			drawLine((Graphics2D) g, antPoint, new Point2D.Double(x, y));
 		}
 
 	}
 
 
 	/**
 	 * Add a diferent option.
 	 *
 	 * @param sel DOCUMENT ME!
 	 * @param s Diferent option.
 	 */
 	public void addOption(String s) {
 		MultiLineaCADToolState actualState = (MultiLineaCADToolState) _fsm.getPreviousState();
 
 		if (s.equals("C") || s.equals("c")) {
 			GeneralPathX elShape = new GeneralPathX(GeneralPathX.WIND_EVEN_ODD, 2);
 			elShape.moveTo(antPoint.getX(), antPoint.getY());
 			elShape.lineTo(firstPoint.getX(), firstPoint.getY());
 			list.add(ShapeFactory.createPolyline2D(elShape));
 			//closeGeometry();
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#addvalue(double)
 	 */
 	public void addValue(double d) {
 	}
 
 
 
 	public void end() {
 		/* CADExtension.setCADTool("polyline");
         PluginServices.getMainFrame().setSelectedTool("POLYLINE"); */
 	}
 
 	public String getName() {
 	    return PluginServices.getText(this,"multipolyline_");
 	}
 
 	public String toString() {
 		return "_multilinea";
 	}
 	public boolean isApplicable(int shapeType) {
 		switch (shapeType) {
 		case FShape.LINE:
 		case FShape.MULTI://GeometryTypes.MULTILINESTRING:
 			return true;
 		}
 		return false;
 	}
 
 
 	public boolean isMultiTransition() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 
 	public void setMultiTransition(boolean condicion) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	public void setPreviousTool(DefaultCADTool tool) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public int getPointsCount() {
 		return points.size();
 	}
 }
