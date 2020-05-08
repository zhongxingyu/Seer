 package org.fao.fi.gis.metadata.util;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
 import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
 import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public final class FeatureTypeUtils {
 
 	private static Logger LOGGER = LoggerFactory.getLogger(FeatureTypeUtils.class);
 	
 	/**
 	 * 
 	 * @param settings
 	 * @param code
 	 * @param buffer
 	 * @return
 	 */
 	public static Map<FeatureTypeProperty, Object> computeFeatureTypeProperties(
 			GeographicServerSettings settings,
 			String code, double buffer) {
 
 		Map<FeatureTypeProperty, Object> map = null;
 
 		// bbox coordinates
 		double bboxMinX = 0;
 		double bboxMinY = 0;
 		double bboxMaxX = 0;
 		double bboxMaxY = 0;
 
 		// feature max longitude absolute values
 		// (possibly asumes that all polygons are still simple Polygons and not
 		// MultiPolygons)
 		// **USELESS** double negLimitX = -90;
 		// **USELESS** double posLimitX = 90;
 
 		double maxNegX = -180;
 		double maxPosX = 180;
 
 		try {
 
 			URL url = new URL(settings.getUrl() + "/" + settings.getSourceWorkspace()
 					+ "/ows?service=wfs&version=1.0.0&request=GetFeature"
 					+ "&typeName=" + settings.getSourceLayer() + "&cql_filter=" + settings.getSourceAttribute()
 					+ "='" + code + "'");
 			
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 
 			NodeList nList = null;
 			Document doc = dBuilder.parse(url.openStream());
 			doc.getDocumentElement().normalize();
 			nList = doc.getElementsByTagName("gml:featureMember");
 			if (nList != null) {
 				LOGGER.info(nList.getLength() + " features");
 				if (nList.getLength() > 0) {
 
 					// ADD COUNT
 					map = new HashMap<FeatureTypeProperty, Object>();
 					map.put(FeatureTypeProperty.COUNT, nList.getLength());
 
 					for (int temp = 0; temp < nList.getLength(); temp++) {
 						Node nNode = nList.item(temp);
 						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 							Element featureMember = (Element) nNode;
 							Node bbox = (Node) featureMember
 									.getElementsByTagName("gml:Box").item(0);
 
 							// coords
 							Element coords = (Element) bbox.getFirstChild();
 							String[] gmlBounds = coords.getTextContent().split(
 									" ");
 
 							String[] min = gmlBounds[0].split(",");
 							String[] max = gmlBounds[1].split(",");
 
 							double minX = Double.parseDouble(min[0]);
 							double minY = Double.parseDouble(min[1]);
 							double maxX = Double.parseDouble(max[0]);
 							double maxY = Double.parseDouble(max[1]);
 
 							// first bbox recalculation
 							if (temp == 0) {
 								bboxMinX = minX;
 								bboxMinY = minY;
 								bboxMaxX = maxX;
 								bboxMaxY = maxY;
 
 								// adjust limits with first limit
 								// **USELESS** if(minX > posLimitX) posLimitX =
 								// minX;
 								// **USELESS** if(maxX < negLimitX) negLimitX =
 								// maxX;
 
 							} else {
 								// classic bbox expansion rule
 								if (minX < bboxMinX)
 									bboxMinX = minX;
 								if (minY < bboxMinY)
 									bboxMinY = minY;
 								if (maxX > bboxMaxX)
 									bboxMaxX = maxX;
 								if (maxY > bboxMaxY)
 									bboxMaxY = maxY;
 
 								// adjust for limit spatial distributions
 								// **USELESS** if(maxX > -180 & maxX <
 								// negLimitX) negLimitX = maxX;
 								// **USELESS** if(minX > 90 & minX < posLimitX)
 								// posLimitX = minX;
 
 							}
 
 							// ** new adjustments
 							// assign maxNegX and maxPosX
 							if (maxX > maxNegX & maxX < 0)
 								maxNegX = maxX;
 							if (minX < maxPosX & minX > 0)
 								maxPosX = minX;
 						}
 
 					}
 
 					// final adjustment
 					// ***************
 					// in case maxNegX & maxPosX unchanged
 					if (maxNegX == -180)
 						maxNegX = -90;
 					if (maxPosX == 180)
 						maxPosX = 90;
 
 					// for date-limit geographic distributions
 					if (maxNegX < -90 & maxPosX > 90) {
 						bboxMinX = maxPosX;
 						bboxMaxX = 360 - Math.abs(maxNegX);
 					}
 
 					// control for globally distributed layers
 					if (bboxMinX < -175.0 && bboxMaxX > 175.0) {
 						bboxMinX = -180.0;
 						bboxMaxX = 180.0;
 						bboxMinY = -90.0;
 						bboxMaxY = 90.0;
 					}
 
 					// NEW: control for overlimit latitude
 					if (bboxMinY < -90)
 						bboxMinY = -90;
 					if (bboxMaxY > 90)
 						bboxMaxY = 90;
 
 					// set null if the connection was lost (coords = 0)
 					// the main app will have to recalculate the bbox until
 					// while bbox == null
 					ReferencedEnvelope bounds = null;
 					if (!(bboxMinX == 0 & bboxMaxX == 0 & bboxMinY == 0 & bboxMaxY == 0)) {
 
 						// apply buffer
 						if (!(bboxMinX < -180 + buffer)
 								&& !(bboxMaxX > 180 - buffer)) {
 							bboxMinX = bboxMinX - buffer;
 							bboxMaxX = bboxMaxX + buffer;
 						}
 
 						if (!(bboxMinY < -90 + buffer)) {
 							bboxMinY = bboxMinY - buffer;
 						}
 						if (!(bboxMaxY > 90 - buffer)) {
 							bboxMaxY = bboxMaxY + buffer;
 
 						}
 
 						// build envelope
 						bounds = new ReferencedEnvelope(bboxMinX, bboxMaxX,
 								bboxMinY, bboxMaxY, DefaultGeographicCRS.WGS84);
 						map.put(FeatureTypeProperty.BBOX, bounds);
						LOGGER.info("Calculated Bounding Box");
						LOGGER.info("min X = "+String.valueOf(bboxMinX));
						LOGGER.info("max X = "+String.valueOf(bboxMaxX));
						LOGGER.info("min Y = "+String.valueOf(bboxMinY));
						LOGGER.info("max Y = "+String.valueOf(bboxMaxY));
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			LOGGER.warn("error during computation - Re-attempt bounding box computation");
 		}
 
 		return map;
 	}
 
 }
