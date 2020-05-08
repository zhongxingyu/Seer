 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package it.geosolutions.geobatch.destination;
 
 import it.geosolutions.geobatch.actions.ds2ds.dao.FeatureConfiguration;
 import it.geosolutions.geobatch.catalog.Identifiable;
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 /**
  * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it"
  *
  */
 public class TargetTest extends TestCase {
 
 	@Test
 	public void testImportTarget() throws IOException {
 		String input = "D:\\Develop\\GEOBATCH_CONFIG\\temp\\importBersagliVettoriali\\20130321-140630-066\\0_Ds2dsGeneratorService\\output.xml";
 		FeatureConfiguration cfg = FeatureConfiguration.fromXML(new FileInputStream(input));
 		VectorTarget target = new VectorTarget(cfg.getTypeName(), new ProgressListenerForwarder(new Identifiable() {
 			
 			@Override
 			public void setId(String arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public String getId() {
 				return "id";
 			}
 		}));
 		
		//target.importTarget(cfg.getDataStore(), null);
 		
 	}
 }
