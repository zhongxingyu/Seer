 /**
  * Copyright (C) 2013
  * by 52 North Initiative for Geospatial Open Source Software GmbH
  *
  * Contact: Andreas Wytzisk
  * 52 North Initiative for Geospatial Open Source Software GmbH
  * Martin-Luther-King-Weg 24
  * 48155 Muenster, Germany
  * info@52north.org
  *
  * This program is free software; you can redistribute and/or modify it under
  * the terms of the GNU General Public License version 2 as published by the
  * Free Software Foundation.
  *
  * This program is distributed WITHOUT ANY WARRANTY; even without the implied
  * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program (see gnu-gpl v2.txt). If not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
  * visit the Free Software Foundation web page, http://www.fsf.org.
  */
 package org.envirocar.tools;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.envirocar.geojson.Feature;
 import org.envirocar.geojson.FeatureCollection;
 import org.envirocar.geojson.Point;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class TrackToCSV {
 	
 	private static final String delimiter = "; ";
 
 	public TrackToCSV() {
 
 	}
 	
 	public InputStream convert(InputStream in) throws IOException {
 		FeatureCollection fc = 
 				new ObjectMapper().readValue(in, FeatureCollection.class);
 		
 		List<String> properties = new ArrayList<String>();
 		
 		for (Feature feature : fc.getFeatures()) {
 			for (String key : feature.getProperties().getPhenomenons().keySet()) {
 				if (!properties.contains(key)) {
 					properties.add(key);
 				}
 			}
 		}
 		
 		List<String> spaceTimeProperties = new ArrayList<String>();
 		spaceTimeProperties.add("longitude");
 		spaceTimeProperties.add("latitude");
 		spaceTimeProperties.add("time");
 		
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
 		
 		bw.append(createCSVHeader(properties, spaceTimeProperties));
 		bw.newLine();
 		
		int count = 0;
 		for (Feature feature : fc.getFeatures()) {
			if (count++ % 2 == 0) continue;
 			for (int i = 0; i < properties.size(); i++) {
 				String key = properties.get(i);
 				Map<?, ?> value = (Map<?, ?>) feature.getProperties().getPhenomenons().get(key);
 				bw.append(value != null ? value.get("value").toString() : Double.toString(Double.NaN));
 				bw.append(delimiter);
 			}
 			
 			Point coord = (Point) feature.getGeometry();
 			bw.append(Double.toString(coord.getCoordinates().getLongitude()));
 			bw.append(delimiter);
 			bw.append(Double.toString(coord.getCoordinates().getLatitude()));
 			bw.append(delimiter);
 			bw.append(feature.getProperties().getTime());
 			
 			bw.newLine();
 		}
 		
 		bw.flush();
 		bw.close();
 		
 		return new ByteArrayInputStream(out.toByteArray());
 	}
 
 	private CharSequence createCSVHeader(List<String> properties, List<String> spaceTimeproperties) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (String key : properties) {
 			sb.append(key);
 			sb.append(delimiter);
 		}
 		
 		for (String key : spaceTimeproperties) {
 			sb.append(key);
 			sb.append(delimiter);
 		}
 		
 		return sb.delete(sb.length() - delimiter.length(), sb.length());
 	}
 
 }
