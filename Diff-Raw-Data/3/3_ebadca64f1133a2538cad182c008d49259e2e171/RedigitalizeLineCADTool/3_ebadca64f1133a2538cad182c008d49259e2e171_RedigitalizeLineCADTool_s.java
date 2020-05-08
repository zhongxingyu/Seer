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
 import java.awt.geom.AffineTransform;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.Handler;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.core.v02.FGraphicUtilities;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.gui.cad.exception.CommandException;
 import com.iver.cit.gvsig.gui.cad.tools.smc.RedigitalizeLineCADToolContext;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LineSegment;
 
 /**
  * Tool for redigitalizing lines
  *
  * @author Jose Ignacio Lamas Fonte [LBD]
  * @author Nacho Varela [Cartolab]
  * @author Pablo Sanxiao [CartoLab]
  *
  */
 public class RedigitalizeLineCADTool extends DefaultCADTool{
 
 	private RedigitalizeLineCADToolContext _fsm;
 	private IGeometry geometry; //saving the geometry which contains the first point
 	private Point2D firstPoint;
 	private Point2D secondPoint;
 	private ArrayList oldPoints;
 	private ArrayList newPoints;
 	private int firstPointIndex = -1;
 	private int secondPointIndex = -1;
 	private boolean firstPointContentVertex = true;
 	private boolean secondPointContentVertex = true;
 	private IRowEdited selectedEntity;
 	private int multiSelected;
 
 	double PROXIMITY_THRESHOLD = 0.000001;
 	
 	public void init() {
 	// clear();
 	if (_fsm == null) {
 		_fsm = new RedigitalizeLineCADToolContext(this);
 	}
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
 //		_fsm.addValue(d);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.iver.cit.gvsig.gui.cad.CADTool#transition(com.iver.cit.gvsig.fmap.layers.FBitSet, java.lang.String)
 	 */
 	public void transition(String s) throws CommandException {
 		if (!super.changeCommand(s)){
 			_fsm.addOption(s);
 		}
 	}
 
 	public void transition(InputEvent event){
 		if(newPoints!=null){
 			_fsm.removePoint(event, newPoints.size());
 		}else{
 			_fsm.removePoint(event, 0);
 		}
 	}
 
 	public void addPoint(double x, double y, InputEvent event) {
 		newPoints.add(new Point2D.Double(x,y));
 	}
 
 	public void addValue(double d) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void addOption(String s) {
 		// TODO Auto-generated method stub
 	}
 
 	public void drawOperation(Graphics g, ArrayList pointsList) {
 		Point2D pointAux = null;
 		int sizePixels = 12;
 		int half = sizePixels / 2;
 		if(firstPoint!=null){
 			pointAux = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(firstPoint);
 			g.drawRect((int) (pointAux.getX() - (half-2)),
 					(int) (pointAux.getY() - (half-2)),
 					sizePixels-4, sizePixels-4);
 			g.drawRect((int) (pointAux.getX() - half),
 					(int) (pointAux.getY() - half),
 					sizePixels, sizePixels);
 
 			if(secondPoint==null){
 				GeneralPathX gpx=new GeneralPathX();
 				for(int i = 0; i<oldPoints.size(); i++){
 					Point2D punto = (Point2D) oldPoints.get(i);
 					if(i==0){
 						gpx.moveTo(punto.getX(),punto.getY());
 					}else{
 						gpx.lineTo(punto.getX(),punto.getY());
 					}
 				}
 				IGeometry geom=ShapeFactory.createPolyline2D(gpx);
 				geom.draw((Graphics2D)g,CADExtension.getEditionManager().getMapControl().getViewPort(),DefaultCADTool.drawingSymbol);
 
 //				Painting vertex
 				AffineTransform at = CADExtension.getEditionManager().getMapControl().getViewPort().getAffineTransform();
 				Handler[] h = geom.getHandlers(IGeometry.SELECTHANDLER);
 				FGraphicUtilities.DrawHandlers((Graphics2D) g, at,h,DefaultCADTool.drawingSymbol);
 			}
 		}
 		if(secondPoint!=null){
 			pointAux = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(secondPoint);
 			g.drawRect((int) (pointAux.getX() - (half-2)),
 					(int) (pointAux.getY() - (half-2)),
 					sizePixels-4, sizePixels-4);
 			g.drawRect((int) (pointAux.getX() - half),
 					(int) (pointAux.getY() - half),
 					sizePixels, sizePixels);
 
 			GeneralPathX gpx=new GeneralPathX();
 			int firstCutIndex = firstPointIndex;
 			int secondCutIndex = secondPointIndex;
 			if(firstCutIndex>secondCutIndex){
 				int aux = firstCutIndex;
 				firstCutIndex = secondCutIndex;
 				secondCutIndex = aux;
 
 				boolean firstPointMetido=false;
 				if(firstCutIndex!=0){
 					for(int i = 0;i<=firstCutIndex;i++){
 						Point2D punto = (Point2D) oldPoints.get(i);
 						if(!firstPointMetido){
 							gpx.moveTo(punto.getX(),punto.getY());
 							firstPointMetido=true;
 						}else{
 							gpx.lineTo(punto.getX(),punto.getY());
 						}
 					}
 				}
 
 				if(pointsList!=null){
 					for(int i=pointsList.size()-1; i>=0;i--){
 						Point2D point = (Point2D) pointsList.get(i);
 						if(!firstPointMetido){
 							gpx.moveTo(point.getX(),point.getY());
 							firstPointMetido=true;
 						}else{
 							gpx.lineTo(point.getX(),point.getY());
 						}
 						if(i<pointsList.size()-1){
 							Point2D actual = null;
 							actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(point);
 							int sizePixelsSnapper = 10;
 							int halfSnapper = sizePixelsSnapper / 2;
 							g.drawRect((int) (actual.getX() - halfSnapper),
 									(int) (actual.getY() - halfSnapper),
 									sizePixelsSnapper, sizePixelsSnapper);
 						}
 					}
 				}
 
 //				Points digitalized by the user
 				if(newPoints!=null && newPoints.size()>0){
 					for(int i=newPoints.size()-1; i>=0;i--){
 						Point2D point = (Point2D) newPoints.get(i);
 						if(!firstPointMetido){
 							gpx.moveTo(point.getX(),point.getY());
 							firstPointMetido=true;
 						}else{
 							gpx.lineTo(point.getX(),point.getY());
 						}
 					}
 				}
 
 				for(int i=secondCutIndex;i<oldPoints.size();i++){
 					Point2D point = (Point2D) oldPoints.get(i);
 					if(!firstPointMetido){
 						gpx.moveTo(point.getX(),point.getY());
 						firstPointMetido=true;
 					}else{
 						gpx.lineTo(point.getX(),point.getY());
 					}
 				}
 			
 			}else{
 				
 				for(int i = 0; i<=firstCutIndex; i++){
 					Point2D point = (Point2D) oldPoints.get(i);
 					if(i==0){
 						gpx.moveTo(point.getX(),point.getY());
 					}else{
 						gpx.lineTo(point.getX(),point.getY());
 					}
 				}
 //				Points digitalized by the user
 				if(newPoints!=null && newPoints.size()>0){
 					for(int i=0; i<newPoints.size();i++){
 						Point2D point = (Point2D) newPoints.get(i);
 						gpx.lineTo(point.getX(),point.getY());
 					}
 				}
 
 //				Snapper points
 				if(pointsList!=null){
 					for(int i=0; i<pointsList.size();i++){
 						Point2D point = (Point2D) pointsList.get(i);
 						gpx.lineTo(point.getX(),point.getY());
 						if(i<pointsList.size()-1){
 							Point2D actual = null;
 							actual = CADExtension.getEditionManager().getMapControl().getViewPort().fromMapPoint(point);
 							int sizePixelsSnapper = 8;
 							int halfSnapper = sizePixelsSnapper / 2;
 							g.drawRect((int) (actual.getX() - halfSnapper),
 									(int) (actual.getY() - halfSnapper),
 									sizePixelsSnapper, sizePixelsSnapper);
 						}
 					}
 				}
 
 				if(secondCutIndex!=oldPoints.size()-1){
 					for(int i = secondCutIndex;i<oldPoints.size();i++){
 						Point2D point = (Point2D) oldPoints.get(i);
 						gpx.lineTo(point.getX(),point.getY());
 					}
 				}
 			}
 
 
 			IGeometry geom=ShapeFactory.createPolyline2D(gpx);
 			geom.draw((Graphics2D)g,CADExtension.getEditionManager().getMapControl().getViewPort(),DefaultCADTool.drawingSymbol);
 
 //			Painting vertex
 			AffineTransform at = CADExtension.getEditionManager().getMapControl().getViewPort().getAffineTransform();
 			Handler[] h = geom.getHandlers(IGeometry.SELECTHANDLER);
 			FGraphicUtilities.DrawHandlers((Graphics2D) g, at,h,DefaultCADTool.drawingSymbol);
 		}
 //		Cleaning the last point of the snapeers
 		cleanSnapper();
 	}
 
 
 	public String getName() {
 		return PluginServices.getText(this,"redigitalize_line_");
 	}
 
 	/**
 	 * It detects if the point is inside the outline of the selected geometry at this
 	 * time and store the information related to the situation and the rest of te points
 	 * of the geometry.
 	 *
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean pointInsideFeature(double x, double y){
 		boolean retorno = false;
 		System.out.println("------>>>>>> calling pointInsideFeature "+x+", "+y);
 
 		VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension.getEditionManager()
 		.getActiveLayerEdited();
 
 		vle.selectWithPoint(x,y,false);
 
 		ArrayList selectedRows=getSelectedRows();
         IRowEdited row=null;
         IGeometry ig=null;
         if (selectedRows.size()>0){
 			row=(DefaultRowEdited) selectedRows.get(0);
 				//row = getCadToolAdapter().getVectorialAdapter().getRow(selection.nextSetBit(0));
 			ig=((IFeature)row.getLinkedRow()).getGeometry();
 			geometry = ig;
 			selectedEntity = row;
 
 			firstPoint = new Point2D.Double(x,y);
 
 
 //			Filling the points list
         	Coordinate c = new Coordinate(x, y);
 
         	PathIterator theIterator = geometry.getPathIterator(null, FConverter.FLATNESS);
         	double[] theData = new double[6];
     		Coordinate from = null, first = null;
     		int index = 0;
     		boolean terminate = false;
     		int numberMultiActual=0;
     		while (!theIterator.isDone()&&!terminate) {
     			int theType = theIterator.currentSegment(theData);
 
     			switch (theType) {
     				case PathIterator.SEG_MOVETO:
     					from = new Coordinate(theData[0], theData[1]);
     					first = from;
     					numberMultiActual++;
     					if(multiSelected!=0){
     						terminate = true;
     					}else{
     						oldPoints = new ArrayList();
     						index = 0;
     						oldPoints.add(index,new Point2D.Double(theData[0], theData[1]));
     					}
     					if(c.equals(from)){
        						firstPointContentVertex = true;
     						firstPointIndex = index;
     						multiSelected = numberMultiActual;
     						retorno = true;
     					}
     					break;
 
     				case PathIterator.SEG_LINETO:
 
     					// System.out.println("SEG_LINETO");
     					Coordinate to = new Coordinate(theData[0], theData[1]);
     					LineSegment line = new LineSegment(from, to);
     					Coordinate closestPoint = line.closestPoint(c);
     					double dist = c.distance(closestPoint);
     					if(c.equals(to)){
        						firstPointContentVertex = true;
     						firstPointIndex = index;
        						multiSelected = numberMultiActual;
     						retorno = true;
     					}else if((dist < PROXIMITY_THRESHOLD)&&(!c.equals(from))){
     						firstPointContentVertex = false;
     						firstPointIndex=index;
     						oldPoints.add(index, new Point2D.Double(x,y));
     						index++;
        						multiSelected = numberMultiActual;
     						retorno = true;
     					}
     					oldPoints.add(index,new Point2D.Double(theData[0], theData[1]));
     					from = to;
     					break;
     				case PathIterator.SEG_CLOSE:
     					line = new LineSegment(from, first);
 
     					closestPoint = line.closestPoint(c);
     					dist = c.distance(closestPoint);
     					if(c.equals(first)){
        						firstPointContentVertex = true;
     						firstPointIndex = index;
        						multiSelected = numberMultiActual;
     						retorno = true;
     					}else if((dist < PROXIMITY_THRESHOLD)&&(!c.equals(from))){
     						firstPointContentVertex = false;
     						firstPointIndex=index;
     						oldPoints.add(index, new Point2D.Double(x,y));
     						index++;
        						multiSelected = numberMultiActual;
     						retorno = true;
     					}
     					oldPoints.add(index,new Point2D.Double(first.x, first.y));
     					from = first;
     					break;
 
     			} //end switch
     			index++;
     			theIterator.next();
     		}
         }
     	getCadToolAdapter().setPreviousPoint((double[])null);
 		return retorno;
 	}
 
 	/**
 	 * It detects if the point is inside the outline of the selected geometry at this
 	 * time and store the information related to the situation and the rest of te points
 	 * of the geometry.
 	 * This method must be executed when the first point was established.
 	 *
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	public boolean secondPointInsideFeature(double x, double y){
 		System.out.println("------>>>>>> calling secondPointInsideFeature "+x+", "+y);
 		boolean retorno = false;
 
 		VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension.getEditionManager()
 		.getActiveLayerEdited();
 
 		vle.selectWithPoint(x,y,false);
 
 		ArrayList selectedRows=getSelectedRows();
         IRowEdited row=null;
         IGeometry ig=null;
         if (selectedRows.size()>0){
 //        	In this case the pressed point is inside of a feature of the selected layer. Now
 //        	we have to check if it is also inside of the feature that contains the first point pressed.
 
         	Coordinate c = new Coordinate(x, y);
 
         	PathIterator theIterator = geometry.getPathIterator(null, FConverter.FLATNESS);
         	double[] theData = new double[6];
     		Coordinate from = null, first = null;
     		boolean found= false;
     		int index = 0;
     		boolean terminepoints = (oldPoints==null || oldPoints.size()==0);
     		if(!terminepoints){
     			Point2D point = (Point2D) oldPoints.get(0);
     			from = new Coordinate(point.getX(), point.getY());
     		}
     		while (!terminepoints && !found) {
     			//while not done
     			Point2D point = (Point2D) oldPoints.get(index);
     			Coordinate to = new Coordinate(point.getX(), point.getY());
     			LineSegment line = new LineSegment(from, to);
     			Coordinate closestPoint = line.closestPoint(c);
     			double dist = c.distance(closestPoint);
     			if(c.equals(to)){
     				secondPointContentVertex = true;
     				secondPointIndex = index;
     				retorno = true;
     				found=true;
     			}else if((dist < PROXIMITY_THRESHOLD)&&(!c.equals(from))){
     				secondPointContentVertex = false;
     				secondPointIndex=index;
     				oldPoints.add(index, new Point2D.Double(x,y));
     				index++;
     				retorno = true;
     				found=true;
     			}
     			from = to;
 
     			index++;
     			if(index==oldPoints.size()){
     				terminepoints=true;
     			}
     		}
         }
 
 		if(retorno){
 			secondPoint = new Point2D.Double(x,y);
 //			actualizamos bien los indexs segun corresponda
 			if((firstPointIndex >= secondPointIndex)&&(!secondPointContentVertex)){
 				firstPointIndex++;
 			}
 			newPoints= new ArrayList();
 			this.setMultiTransition(true);
 		}
 		getCadToolAdapter().setPreviousPoint((double[])null);
 		return retorno;
 	}
 
 
 	public String toString() {
 		// TODO Auto-generated method stub
 		return "_redigitalize_line";
 	}
 
 	public boolean isApplicable(int shapeType) {
 		switch (shapeType) {
 		case FShape.LINE:
 		case FShape.MULTI:
 			return true;
 		}
 		return false;
 	}
 
 	public void clear(){
 		geometry = null;
 		firstPoint = null;
 		secondPoint = null;
 		oldPoints = null;
 		newPoints = null;
 		firstPointIndex = -1;
 		secondPointIndex = -1;
 		firstPointContentVertex = true;
 		secondPointContentVertex = true;
 		this.setMultiTransition(false);
 		getCadToolAdapter().setPreviousPoint((double[])null);
 		selectedEntity=null;
 		multiSelected = 0;
 	}

 	/**
 	 * It deletes a point of the new redigitalized
 	 * @param event
 	 */
 	public void removePoint(InputEvent event) {
 		newPoints.remove(newPoints.size()-1);
 		if(newPoints.size()>0){
 			getCadToolAdapter().setPreviousPoint((Point2D)newPoints.get(newPoints.size()-1));
 		}else{
 			getCadToolAdapter().setPreviousPoint((double[])null);
 		}
 	}
 
 	/**
 	 * It deletes the second point selected in the geometry
 	 * @param event
 	 */
 	public void removeSecondPoint(InputEvent event) {
 		if(!secondPointContentVertex){
 			oldPoints.remove(secondPointIndex);
 		}
 		secondPointIndex = -1;
 		secondPointContentVertex = true;
 		secondPoint = null;
 		this.setMultiTransition(false);
 		getCadToolAdapter().setPreviousPoint((double[])null);
 	}
 
 	/**
 	 * It deletes the first point selected in the geometry
 	 * @param event
 	 */
 	public void removeFirstPoint(InputEvent event) {
 		clear();
 	}
 
 	public void drawOperation(Graphics g, double x, double y) {
 		ArrayList list = new ArrayList();
 		list.add(new Point2D.Double(x,y));
 		drawOperation(g,list);
 
 	}
 
 	/**
 	 * It deletes the memory of the snappers in order to click in two points of the
 	 * same geometry without following the ones that there are between them.
 	 */
 	public void cleanSnapper(){
 		if(newPoints==null || newPoints.size()==0){
 			getCadToolAdapter().setPreviousPoint((double[])null);
 		}
 	}
 
 	/**
 	 * It stores the changes made to the geometry.
 	 */
 	public void saveChanges(){
 		if(selectedEntity!=null){
 			System.out.println("--->>> Saving changes to the geometry");
 			((IFeature)selectedEntity.getLinkedRow()).setGeometry(getgeometryResultante());
 			modifyFeature(selectedEntity.getIndex(),(IFeature)selectedEntity.getLinkedRow());
 		}
 	}
 
 	/**
 	 * Return the geometry redigitalized
 	 * @return
 	 */
 	public IGeometry getgeometryResultante() {
 
 		GeneralPathX gpx=new GeneralPathX();
 		int firstCutIndex = firstPointIndex;
 		int secondCutIndex = secondPointIndex;
 
     	PathIterator theIterator = geometry.getPathIterator(null, FConverter.FLATNESS);
     	double[] theData = new double[6];
 		int numberMultiActual=0;
 		while (!theIterator.isDone()) {
 			int theType = theIterator.currentSegment(theData);
 
 			switch (theType) {
 				case PathIterator.SEG_MOVETO:
 					double x = theData[0];
 					double y = theData[1];
 					numberMultiActual++;
 					if(multiSelected == numberMultiActual)
 
 						if(firstCutIndex>secondCutIndex){
 							int aux = firstCutIndex;
 							firstCutIndex = secondCutIndex;
 							secondCutIndex = aux;
 							
 							boolean firstPointMetido=false;
 							if(firstCutIndex!=0){
 								for(int i = 0;i<=firstCutIndex;i++){
 									Point2D point = (Point2D) oldPoints.get(i);
 									if(!firstPointMetido){
 										gpx.moveTo(point.getX(),point.getY());
 										firstPointMetido=true;
 									}else{
 										gpx.lineTo(point.getX(),point.getY());
 									}
 								}
 							}
 
 //							Points digitalized by the user
 							if(newPoints!=null && newPoints.size()>0){
 								for(int i=newPoints.size()-1; i>=0;i--){
 									Point2D point = (Point2D) newPoints.get(i);
 									if(!firstPointMetido){
 										gpx.moveTo(point.getX(),point.getY());
 										firstPointMetido=true;
 									}else{
 										gpx.lineTo(point.getX(),point.getY());
 									}
 								}
 							}
 
 							for(int i=secondCutIndex;i<oldPoints.size();i++){
 								Point2D point = (Point2D) oldPoints.get(i);
 								if(!firstPointMetido){
 									gpx.moveTo(point.getX(),point.getY());
 									firstPointMetido=true;
 								}else{
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 
 						}else{
 							for(int i = 0; i<=firstCutIndex; i++){
 								Point2D point = (Point2D) oldPoints.get(i);
 								if(i==0){
 									gpx.moveTo(point.getX(),point.getY());
 								}else{
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 //							Points digitalized by the userahora iran los points del trozo digitalizado por el usuario
 							if(newPoints!=null && newPoints.size()>0){
 								for(int i=0; i<newPoints.size();i++){
 									Point2D point = (Point2D) newPoints.get(i);
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 
 							if(secondCutIndex!=oldPoints.size()-1){
 								for(int i = secondCutIndex;i<oldPoints.size();i++){
 									Point2D point = (Point2D) oldPoints.get(i);
 									gpx.lineTo(point.getX(),point.getY());
 								}
 							}
 
 					}else{
 						gpx.moveTo(x,y);
 					}
 					break;
 
 				case PathIterator.SEG_LINETO:
 
 					x = theData[0];
 					y = theData[1];
 					if(multiSelected == numberMultiActual){
 
 					}else{
 						gpx.lineTo(x,y);
 					}
 					break;
 				case PathIterator.SEG_CLOSE:
 					if(multiSelected == numberMultiActual){
 
 					}else{
 						gpx.closePath();
 					}
 					break;
 
 			} //end switch
 			theIterator.next();
 		}
 		IGeometry geom=ShapeFactory.createPolyline2D(gpx);
 		return geom;
 	}
 
 	/**
 	 * Return the geoid of the selected entity
 	 * @return
 	 */
 //	public String getCurrentGeoid(){
 //		String geoid = ((VectorialEditableDBAdapter)getVLE().getVEA()).
 //			getFIDFromIndex(selectedEntity.getIndex()).toString();
 //
 //		return geoid;
 //	}
 
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
 
 }
