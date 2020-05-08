 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms.kvp;
 
 import org.geoserver.ows.KvpParser;
 import org.vfny.geoserver.config.PaletteManager;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.responses.palette.InverseColorMapOp;
 
 import java.awt.image.IndexColorModel;
 
 /**
  * 
 * @author Simone Giannecchini, GeoSolutions
  * 
  */
 public class PaletteKvpParser extends KvpParser {
 	public PaletteKvpParser() {
 		super("palette", IndexColorModel.class);
 	}
 
 	public Object parse(String value) throws Exception {
 		// palette
 		try {
 			final InverseColorMapOp model = PaletteManager.getPalette(value);
 			if (model == null) {
 				throw new WmsException("Palette " + value
 						+ " could not be found "
 						+ "in $GEOSERVER_DATA_DIR/palettes directory");
 			}
 
 			return model;
 		} catch (Exception e) {
 			throw new WmsException(e, "Palette " + value
 					+ " could not be loaded", null);
 		}
 	}
 }
