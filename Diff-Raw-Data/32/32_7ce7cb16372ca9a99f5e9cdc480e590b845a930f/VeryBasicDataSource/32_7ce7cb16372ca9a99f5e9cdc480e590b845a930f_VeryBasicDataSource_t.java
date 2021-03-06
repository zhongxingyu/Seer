 package org.geotools.datasource;
 
 import org.geotools.featuretable.*;
 import java.util.*;
 import java.io.*;
 import com.vividsolutions.jts.geom.*;
 
 
 public class VeryBasicDataSource implements DataSource {
     String sFilename = null;
     String [] sColumnNames = null;
     boolean stopped = false;
    GeometryFactory geomFac = new GeometryFactory();
     
     public VeryBasicDataSource(String filename) {
         sFilename = filename;
     }
     
     /** Loads Feature rows for the given Extent from the datasource
      */
     public void importFeatures(FeatureTable ft,Extent ex) throws DataSourceException {
         System.out.println("VeryBasicDataSource.load() called");
         
         Vector Features = new Vector();
         
         // Open file
         try {
             File f = new File(sFilename);
             FileInputStream fi = new FileInputStream(f);
             StringBuffer sb = new StringBuffer();
             int o=0;
             byte b[] = new byte[100];
             while (o>-1 && !stopped) {
                 o = fi.read(b, 0, 100);
                 if (o>-1) {
                     String s = new String(b, 0, o);
                     sb.append(s);
                 }
             }
             if (stopped) return;
             // Split up the string into rows
             StringTokenizer st = new StringTokenizer(sb.toString(), "\n");
             Vector rows = new Vector();
             while (st.hasMoreElements()) {
                 String sRow = (String)st.nextElement();
                 sRow = sRow.trim();
                 System.out.println("Reading row : "+sRow);
                 // Split up into columns
                 Vector columns = new Vector();
                 columns.addElement("PRIMARY");	// The primary position
                 StringTokenizer stc = new StringTokenizer(sRow, ",");
                 while (stc.hasMoreElements())
                     columns.addElement(stc.nextElement());
                 rows.addElement((String[])columns.toArray(new String[columns.size()]));
                 System.out.println("read row:"+rows.size()+" with "+columns.size()+" elements");
             }
             // Get the first row (column names)
             sColumnNames = (String[])rows.elementAt(0);
             
             // Get each Feature - as a GeoPoint + attribs
             for (int i=1;i<rows.size() && !stopped;i++) {
                 Coordinate p = new Coordinate();
                 Object [] objrow = (Object[])rows.elementAt(i);
                 // Create now Object[] for the row
                 Object [] row = new Object[objrow.length];
                 for (int t=0;t<row.length;t++)
                     row[t] = objrow[t];
                 for (int j=0;j<sColumnNames.length;j++) {
                     if (sColumnNames[j].equals("LONGITUDE"))
                         p.x = (new Double(row[j].toString())).doubleValue();
                     if (sColumnNames[j].equals("LATITUDE"))
                         p.y = (new Double(row[j].toString())).doubleValue();
                 }
                DefaultFeature feat = new DefaultFeature(); 
                System.out.println("adding P "+p);
                row[0] = geomFac.createPoint(p);
                System.out.println("as Point "+(Point)row[0]);
                 feat.setAttributes(row,sColumnNames);
                feat.setGeometry((Point) row[0]);
                 // Filter Feature Feature Filter
                 if (ex.containsFeature(feat))
                     ft.addFeature(feat);
             }
             
         }
         catch(Exception exp) {
             System.out.println("Exception loading data");
             throw new DataSourceException("Exception loading data : "+exp.getMessage());
         }
     }
     
     public void exportFeatures(FeatureTable ft,Extent ex) throws DataSourceException{
         throw new DataSourceException("Very Basic Data Source does not support exporting");
     }
     
     /** Saves the given features to the datasource
      */
     public void save(List features) throws DataSourceException {
     }
     
     /** gets the Column names (used by FeatureTable) for this DataSource
      */
     public String [] getColumnNames() {
 
         System.out.println("getColumnNames returning "+sColumnNames);
         return sColumnNames;
     }
     
     /** Stops this DataSource from loading
      */
     public void stopLoading() {
         stopped=true;
         System.out.println("Stopped called on VBdatasource");
     }
     
    /** gets the extent of this data source using the default speed of
     * this datasource as set by the implementer.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return null;
    }
    
    /** gets the extent of this data source using the speed of
     * this datasource as set by the parameter.
     * @param speed if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        return null;
    }
    
 }
 
