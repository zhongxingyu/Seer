 package com.iver.cit.gvsig.gui.cad.tools;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
 import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.GeneralPathX;
 import com.iver.cit.gvsig.fmap.core.Handler;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.ShapeFactory;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.edition.UtilFunctions;
 import com.iver.cit.gvsig.fmap.layers.FBitSet;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.operations.strategies.DefaultStrategy;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.gui.cad.tools.PolylineCADTool;
 import com.iver.cit.gvsig.gui.cad.tools.smc.PolylineCADToolContext.PolylineCADToolState;
 import com.vividsolutions.jts.geom.Geometry;
 
 public class AutoCompletePolygon extends PolylineCADTool {
 
 	@Override
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
         IGeometry geom=getGeometry();
         if (geom.getHandlers(IGeometry.SELECTHANDLER).length ==0 && firstPoint!=null) {
         	GeneralPathX gpx = new GeneralPathX();
         	gpx.moveTo(firstPoint.getX(), firstPoint.getY());
         	gpx.lineTo(x, y);
         	ShapeFactory.createPolyline2D(gpx).draw((Graphics2D) g,
         			getCadToolAdapter().getMapControl().getViewPort(),
         			DefaultCADTool.geometrySelectSymbol);
         }
         else if (geom.getHandlers(IGeometry.SELECTHANDLER).length > 1) {
         	GeneralPathX gpxGeom=new GeneralPathX();
         	gpxGeom.moveTo(x,y);
         	gpxGeom.append(geom.getPathIterator(null,FConverter.FLATNESS), true);
 
         	gpxGeom.closePath();
         	IGeometry newGeom = ShapeFactory.createPolygon2D(gpxGeom);
         	newGeom.draw((Graphics2D) g,
         			getCadToolAdapter().getMapControl().getViewPort(),
         			DefaultCADTool.geometrySelectSymbol);
         }
     }
 
 
 	private IGeometry autoComplete(IGeometry digitizedGeom) {
 		Geometry jtsGeom = digitizedGeom.toJTSGeometry();
 		FLyrVect lyrVect = (FLyrVect) getVLE().getLayer();
 		// Se supone que debe ser rpido, ya que est indexado
 		try {
 			FBitSet selected = lyrVect.queryByShape(digitizedGeom,
 					DefaultStrategy.INTERSECTS);
 			for (int i = selected.nextSetBit(0); i >= 0; i = selected
 					.nextSetBit(i + 1)) {
 				IGeometry aux = lyrVect.getSource().getShape(i);
 				Geometry jtsAux = aux.toJTSGeometry();
 				jtsGeom = jtsGeom.difference(jtsAux);
 			}
 
 		} catch (ReadDriverException e) {
 			NotificationManager.showMessageError(
 					PluginServices.getText(this, "Error_in_Autocomplete_Polygon_Tool_")
 					+ " " + e.getLocalizedMessage(),
 					e);
 		} catch (VisitorException e) {
 			NotificationManager.showMessageError(
 					PluginServices.getText(this, "Error_in_Autocomplete_Polygon_Tool_")
 					+ " " + e.getLocalizedMessage(),
 					e);
 		} catch (com.vividsolutions.jts.geom.TopologyException e) {
 			NotificationManager.showMessageError(
 					PluginServices.getText(this, "Error_in_Autocomplete_Polygon_Tool_")
 					+ " " + e.getLocalizedMessage(),
 					e);
 		} catch (Exception e) {
 			NotificationManager.showMessageError(
 					PluginServices.getText(this, "Error_in_Autocomplete_Polygon_Tool_")
 					+ " " + e.getLocalizedMessage(),
 					e);
 		}

 		return FConverter.jts_to_igeometry(jtsGeom);
 	}
 
 	@Override
 	public void addGeometry(IGeometry geometry) {
 		IGeometry newGeom = autoComplete(geometry);
 		super.addGeometry(newGeom);
 	}
	 public boolean isApplicable(int shapeType) {
	        switch (shapeType) {
	        case FShape.POLYGON:
	            return true;
	        }
	        return false;
	    }
 }
