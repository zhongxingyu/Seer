 /**
  * GeoTools Example
  * 
  *  (C) 2011 LISAsoft
  *  
  *  This library is free software; you can redistribute it and/or modify it under
  *  the terms of the GNU Lesser General Public License as published by the Free
  *  Software Foundation; version 2.1 of the License.
  *  
  *  This library is distributed in the hope that it will be useful, but WITHOUT
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  */
 package com.lisasoft.face.data;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.event.EventListenerList;
 
 import org.geotools.data.csv.CSVDataStore;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.opengis.feature.simple.SimpleFeature;
 
 import com.csvreader.CsvReader;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 
 /**
  * This is a little mock up of a traditional data acecss object.
  * <p>
  * Internally this just reads a CSV file and creates objects. This is used to show how we can wrap
  * up an object as a feature for visual display.
  * 
  * @author Jody Garnett (LISAsoft)
  */
 public class FaceDAO {
 
     /**
      * Change this to match the EPSG code for your spatial reference system.
      * 
      * See: http://spatialreference.org/ref/?search=ch1903
      */
     public static String EPSG_CODE = "EPSG:2056";
 
     static GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
 
     /**
      * This represents the contents - use CopyOnWriteArrayList to account for concurrent access
      */
     private CopyOnWriteArrayList<FaceImpl> contents;
 
 
     /**
      * This is a global list allowing outside party to listen to all of the java beans.
      */
     EventListenerList listeners = null;
 
     /**
      * Used to watch the contents of the FaceImpls; the resulting event can be used to update a
      * DataStore or MapComponent in the event something changes.
      */
     private PropertyChangeListener watcher = new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent evt) {
             firePropertyChange(evt);
         }
     };
 
     public FaceDAO(List<FaceImpl> faceList) throws IOException {
         contents = new CopyOnWriteArrayList<FaceImpl>(faceList);
     }
 
     /** Hook up watcher to beans contained; any changes
      * should be passed on as feature events to the GIS; or listeners etc...
      */
     protected void listen( boolean listen ){
         if( listen ){
             for( FaceImpl face : contents ){
                 face.addPropertyChangeListener(watcher);
             }
         }
         else {
             for( FaceImpl face : contents ){
                 face.removePropertyChangeListener(watcher);
             }
         }        
     }
     /**
      * Listen to all of the java beans managed by this data access object.
      * @param listener
      */
     synchronized public void addPropertyChangeListener(PropertyChangeListener listener) {
         if (listeners == null) {
             listeners = new EventListenerList();
             listen( true );
         }
         listeners.add(PropertyChangeListener.class, listener);
     }
     /**
      * Remove a listener from the java beans managed by this data access object.
      * @param listener
      */
     synchronized public void removePropertyChangeListener(PropertyChangeListener listener) {
         if (listeners == null)
             return;
         listeners.remove(PropertyChangeListener.class, listener);
         if( listeners.getListenerCount(PropertyChangeListener.class) == 0 ){
             listen( false );
         }
     }
 
     /**
      * Used to notify any PropertyChange listeners that a specific face has changed.
      * <p>
      * event.getSource() is the Face that has changed
      * 
      * @param evt
      */
     synchronized protected void firePropertyChange(PropertyChangeEvent evt) {
         for (PropertyChangeListener listener : listeners.getListeners(PropertyChangeListener.class)) {
             try {
                 listener.propertyChange(evt);
             } catch (Throwable t) {
                 System.err.println("Unable to deliver property change event to " + t);
                 t.printStackTrace(System.err);
             }
         }
     }
 
     public FaceDAO(File csvFile) throws IOException {
         contents = new CopyOnWriteArrayList<FaceImpl>(load(csvFile));
     }
 
     static Point getLocation(Face face) {
         double x = face.getWestOstKoordinate().doubleValue();
         double y = face.getSuedNordKoordinate().doubleValue();
         Coordinate coordinate = new Coordinate(x, y);
         return gf.createPoint(coordinate);
     }
 
     /** Thread-safe access to the data objects */
     public CopyOnWriteArrayList<FaceImpl> contents() {
         return contents;
     }
 
     /**
      * Look up face with the provided nummber/identifier. This is used to sort out selection
      * when a selection is made from the GIS.
      * 
      * @param nummber
      * @return Face, or null if not found
      */
     FaceImpl lookup(long nummber) {
         for (FaceImpl face : contents) {
             if (nummber == face.getNummer()) {
                 return face; // found!
             }
         }
         return null;
     }
     /**
      * Look up face with the provided nummber/identifier. This is used to sort out selection
      * when a selection is made from the GIS.
      * 
      * @param nummber
      * @return Face, or null if not found
      */
     List<FaceImpl> lookup(long nummbers[]) {
         Set<Long> lookup = new HashSet<Long>();
         for( Long nummber : nummbers ){
             lookup.add( nummber );
         }
         List<FaceImpl> found = new ArrayList<FaceImpl>();
         
         for (FaceImpl face : contents) {
             if (lookup.contains( face.getNummer() )) {
                 found.add( face );
             }
         }
         return found;
     }
 
     
     /**
      * Used to access the bean info; this information is used to dynamically generate the
      * FeatureType and Features.
      * 
      * @return bean info for Face interface
      */
     public BeanInfo getBeanInfo() {
         BeanInfo info;
         try {
             info = Introspector.getBeanInfo(Face.class);
         } catch (IntrospectionException e) {
             return null;
         }
         return info;
 
     }
 
     /**
      * Used to read in from a CSV file; created as a static method for ease of testing.
      * 
      * @param csvFile
      * @return
      * @throws FileNotFoundException
      */
     public static List<FaceImpl> load(File csvFile) throws IOException {
         CsvReader reader = new CsvReader(new FileReader(csvFile));
         boolean header = reader.readHeaders();
         if (!header) {
             throw new IOException("Unable to read csv header");
         }
         List<FaceImpl> faceList = new ArrayList<FaceImpl>();
 
         while (reader.readRecord()) {
             // we have content let us make a Face
             // double latitude = Double.parseDouble(tokens[0]);
             // double longitude = Double.parseDouble(tokens[1]);
             //
             // String name = tokens[2].trim();
             // int number = Integer.parseInt(tokens[3].trim());
 
             // Nummer
             // int identifier = Integer.parseInt(tokens[0].trim());
             long identifier = Long.parseLong(reader.get(0));
             FaceImpl face = new FaceImpl(identifier);
 
             face.setType(reader.get(1));
 
             face.setFaceFormat(reader.get(2));
             face.setProductFormat(reader.get(3));
             face.setStatus(reader.get(4));
             face.setInstalled(reader.get(5));
             face.setPeriod(reader.get(6));
            face.setPosting(reader.get(7));
            face.setArea(reader.get(8));
            face.setStreet(reader.get(9));
            face.setNumber(reader.get(10));
 
            double x = Double.parseDouble(reader.get(11));
             face.setWestOstKoordinate(new BigDecimal(x));
 
            double y = Double.parseDouble(reader.get(12));
             face.setSuedNordKoordinate(new BigDecimal(y));
            face.setAngle(reader.get(13));
            face.setCategory(reader.get(14));
 
             faceList.add(face);
         }
         return faceList;
     }
 
 }
