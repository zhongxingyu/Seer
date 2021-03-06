 /*
  * $Id$
  *
  * This file is part of McIDAS-V
  *
  * Copyright 2007-2009
  * Space Science and Engineering Center (SSEC)
  * University of Wisconsin - Madison
  * 1225 W. Dayton Street, Madison, WI 53706, USA
  * http://www.ssec.wisc.edu/mcidas
  * 
  * All Rights Reserved
  * 
  * McIDAS-V is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * McIDAS-V is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser Public License
  * along with this program.  If not, see http://www.gnu.org/licenses.
  */
 
 package edu.wisc.ssec.mcidasv.data;
 
 import edu.wisc.ssec.mcidas.*;
 import edu.wisc.ssec.mcidas.adde.AddeServerInfo;
 
 import edu.wisc.ssec.mcidasv.McIDASV;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.net.URL;
 
 import java.rmi.RemoteException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.swing.*;
 
 import ucar.nc2.iosp.mcidas.McIDASAreaProjection;
 
 import ucar.unidata.data.*;
 
 import ucar.unidata.data.grid.GridUtil;
 
 import ucar.unidata.data.imagery.*;
 
 import ucar.unidata.geoloc.*;
 import ucar.unidata.geoloc.projection.ProjectionAdapter;
 
 import ucar.unidata.idv.IntegratedDataViewer;
 
 import ucar.unidata.util.GuiUtils;
 import ucar.unidata.util.IOUtil;
 import ucar.unidata.util.LogUtil;
 import ucar.unidata.util.Misc;
 import ucar.unidata.util.PollingInfo;
 import ucar.unidata.util.Range;
 import ucar.unidata.util.StringUtil;
 import ucar.unidata.util.TwoFacedObject;
 
 import ucar.unidata.view.geoloc.ProjectionManager;
 
 import ucar.visad.data.AddeImageFlatField;
 
 import visad.*;
 import visad.data.*;
 import visad.data.mcidas.*;
 import visad.georef.MapProjection;
 import visad.meteorology.*;
 
 /**
  * Abstract DataSource class for images files.
  */
 public class Test2ImageDataSource extends ImageDataSource {
 
     /**
      * Public keys for server, group, dataset, user, project.
      */
     public final static String SIZE_KEY = "size";
     public final static String PLACE_KEY = "place";
     public final static String LATLON_KEY = "latlon";
     public final static String LINELE_KEY = "linele";
     public final static String MAG_KEY = "mag";
     public final static String BAND_KEY = "band";
     public final static String UNIT_KEY = "unit";
     public final static String PREVIEW_KEY = "preview";
 
     /** The first projection we find */
     protected ProjectionImpl sampleProjection;
     protected MapProjection sampleMapProjection;
 
     /** My composite */
     private CompositeDataChoice myCompositeDataChoice;
 
     /** children choices */
     private List myDataChoices = new ArrayList();
 
     /** sequence manager for displaying data */
     private ImageSequenceManager sequenceManager;
 
 
     /** list of twod categories */
     private List twoDCategories;
 
     /** list of 2D time series categories */
     private List twoDTimeSeriesCategories;
 
     /** list of twod categories */
     private List bandCategories;
 
     /** list of 2D time series categories */
     private List bandTimeSeriesCategories;
 
     /* ADDE request string */
     private String source;
     private String baseSource;
 
     /* properties for this data source */
     private Hashtable sourceProps;
     private Hashtable selectionProps;
 
     private int lineResolution;
     private int elementResolution;
     private int lineMag = 1;
     private int elementMag = 1;
 
     private LatLonRect lastLlr;
     private GeoSelection lastGeoSelection;
     private static DataChoice lastChoice;
     private Boolean showPreview = new Boolean(false);
     private FlatField previewImage;
     private MapProjection previewProjection;
     private Hashtable initProps;
 
     private boolean hasImagePreview = true;
     private AreaDirectory previewDir = null;
     private boolean haveDataSelectionComponents = false;
 
     private GeoPreviewSelection previewSel;
     private GeoLatLonSelection laloSel;
 
     private String displaySource;
 
     protected List<DataChoice> stashedChoices = null;
 
     public Test2ImageDataSource() {}
 
 
     /**
      * Create a new Test2ImageDataSource with a list of (String) images. These
      * can either be AREA files or ADDE URLs.
      *
      * @param descriptor       The descriptor for this data source.
      * @param images           Array of  file anmes or urls.
      * @param properties       The properties for this data source.
      */
     public Test2ImageDataSource(DataSourceDescriptor descriptor, String[] images,
                            Hashtable properties) {
         super(descriptor, images, properties);
 /*
         System.out.println("1 Test2ImageDataSource:");
         System.out.println("    descriptor=" + descriptor);
         System.out.println("    images=" + images);
         System.out.println("    properties=" + properties);
 */
     }
 
 
     /**
      * Create a new Test2ImageDataSource with a list of (String) images. These
      * can either be AREA files or ADDE URLs.
      *
      * @param descriptor       The descriptor for this data source.
      * @param images           Array of  file anmes or urls.
      * @param properties       The properties for this data source.
      */
     public Test2ImageDataSource(DataSourceDescriptor descriptor, List images,
                            Hashtable properties) {
         super(descriptor, images, properties);
 /*
         System.out.println("2 Test2ImageDataSource:");
         System.out.println("    descriptor=" + descriptor);
         System.out.println("    images=" + images);
         System.out.println("    properties=" + properties);
 */
     }
 
 
 
 
     /**
      * Create a new Test2ImageDataSource with the given {@link ImageDataset}.
      * The dataset may hold eight AREA file filepaths or ADDE URLs.
      *
      * @param descriptor    The descriptor for this data source.
      * @param ids           The dataset.
      * @param properties    The properties for this data source.
      */
     public Test2ImageDataSource(DataSourceDescriptor descriptor, ImageDataset ids,
                            Hashtable properties) {
         super(descriptor, ids, properties);
 /*
         System.out.println("3 Test2ImageDataSource:");
         System.out.println("    descriptor=" + descriptor);
         System.out.println("    ids=" + ids);
         System.out.println("    properties=" + properties);
 */
         if (!properties.containsKey("MAG"))
             properties.put("MAG","1 1");
         this.sourceProps = properties;
         this.showPreview = (Boolean)(sourceProps.get((Object)PREVIEW_KEY));
         List descs = ids.getImageDescriptors();
         AddeImageDescriptor aid = (AddeImageDescriptor)descs.get(0);
         this.source = aid.getSource();
         setMag();
         getAreaDirectory(properties);
     }
 
     /**
      *  Overwrite base class  method to return the name of this class.
      *
      *  @return The name.
      */
     public String getImageDataSourceName() {
         return "Adde Image Data Source";
     }
 
     private void setMag() {
         Object magKey = (Object)"mag";
         if (sourceProps.containsKey(magKey)) {
             String magVal = (String)(sourceProps.get(magKey));
             String[] magVals = magVal.split(" ");
             this.lineMag = new Integer(magVals[0]).intValue();
             this.elementMag = new Integer(magVals[1]).intValue();
         }
     }
 
     private void getAreaDirectory(Hashtable properties) {
         String addeCmdBuff = source;
         if (addeCmdBuff.contains("BAND=")) {
             String[] segs = addeCmdBuff.split("BAND=");
             String seg0 = segs[0];
             String seg1 = segs[1];
             int indx = seg1.indexOf("&");
             if (indx == 0) {
                 addeCmdBuff = seg0 + "BAND=1" + seg1;
             }
         }
         if (addeCmdBuff.contains("MAG=")) {
             String[] segs = addeCmdBuff.split("MAG=");
             String seg0 = segs[0];
             String seg1 = segs[1];
             int indx = seg1.indexOf("&");
             seg1 = seg1.substring(indx);
             String magString = lineMag + " " + elementMag;
             addeCmdBuff = seg0 + "MAG=" + magString + seg1;
         }
         try {
            AreaFile af = new AreaFile(addeCmdBuff);
             AreaDirectory ad = af.getAreaDirectory();
             this.lineResolution = ad.getValue(11);
             this.elementResolution = ad.getValue(12);
             McIDASAreaProjection map = new McIDASAreaProjection(af);
             AREACoordinateSystem acs = new AREACoordinateSystem(af);
             sampleMapProjection = (MapProjection)acs;
             sampleProjection = map;
             baseSource = addeCmdBuff;
         } catch (Exception e) {
            System.out.println("getAreaDirectory e=" + e);
         }
     }
 
 
     protected void initDataSelectionComponents(
                    List<DataSelectionComponent> components, final DataChoice dataChoice) {
 /*
         System.out.println("initDataSelectionComponents:");
         System.out.println("    components=" + components);
         System.out.println("    dataChoice=" + dataChoice);
         System.out.println("        Id=" + dataChoice.getId() + " " + dataChoice.getId().getClass());
         System.out.println("        Name=" + dataChoice.getName());
         System.out.println("        Description=" + dataChoice.getDescription());
         System.out.println("        Categories=" + dataChoice.getCategories());
         System.out.println("        Properties=" + dataChoice.getProperties());
 */
         getDataContext().getIdv().showWaitCursor();
         if (this.showPreview == null) this.showPreview = true;
         if (this.haveDataSelectionComponents && dataChoice.equals(lastChoice)) {
             try {
                 if (dataChoice.getDataSelection() == null) {
                     laloSel = new GeoLatLonSelection(this, 
                                      dataChoice, this.initProps, this.previewProjection,
                                      previewDir);
                     this.lineMag = laloSel.getLineMag();
                     this.elementMag = laloSel.getElementMag();
                 
                     previewSel = new GeoPreviewSelection(dataChoice, this.previewImage, 
                                      this.laloSel, this.previewProjection,
                                      this.lineMag, this.elementMag, this.showPreview);
                 }
                 components.add(previewSel);
                 components.add(laloSel);
             } catch (Exception e) {
                 System.out.println("error while repeating addition of selection components \n	e= "+e);
             }
         } else {
             makePreviewImage(dataChoice);
             lastChoice = dataChoice;
             if (hasImagePreview) {
                 try {
                     AreaAdapter aa = new AreaAdapter(baseSource, false);
                     //MAreaAdapter aa = new MAreaAdapter(baseSource, false);
                     this.previewImage = (FlatField)aa.getImage();
                     AreaFile af = new AreaFile(baseSource);
                     AreaDirectory ad = af.getAreaDirectory();
                     this.lineResolution = ad.getValue(11);
                     this.elementResolution = ad.getValue(12);
                     McIDASAreaProjection map = new McIDASAreaProjection(af);
                     AREACoordinateSystem acs = new AREACoordinateSystem(af);
                     this.initProps = new Hashtable();
                     Enumeration propEnum = sourceProps.keys();
                     for (int i=0; propEnum.hasMoreElements(); i++) {
                         String key = propEnum.nextElement().toString();
                         Object val = sourceProps.get(key);
                         key = key.toUpperCase();
                         if (val instanceof String) {
                             String str = (String)val;
                             val = (Object)(str.toUpperCase());
                         }
                         this.initProps.put(key,val);
                     }
                     this.previewProjection = (MapProjection)acs;
                     laloSel = new GeoLatLonSelection(this, 
                                   dataChoice, this.initProps, this.previewProjection,
                                   previewDir);
                     this.lineMag = laloSel.getLineMag();
                     this.elementMag = laloSel.getElementMag();
                     previewSel = new GeoPreviewSelection(dataChoice, this.previewImage, 
                                      this.laloSel, this.previewProjection,
                                      this.lineMag, this.elementMag, this.showPreview);
                     components.add(previewSel);
                     components.add(laloSel);
                     this.haveDataSelectionComponents = true;
                     replaceKey(MAG_KEY, (Object)(this.lineMag + " " + this.elementMag));
                 } catch (Exception e) {
                     System.out.println("Can't make selection components e="+e);
                 }
             }
         }
         getDataContext().getIdv().showNormalCursor();
     }
 
     private void makePreviewImage(DataChoice dataChoice) {
         BandInfo bi = (BandInfo) dataChoice.getId();
         source = replaceKey(source, BAND_KEY, (Object)(bi.getBandNumber()));
         String name = dataChoice.getName();
         int idx = name.lastIndexOf("_");
         String unit = name.substring(idx+1);
         if (getKey(source, UNIT_KEY).equals(""))
             source = replaceKey(source, UNIT_KEY, (Object)(unit));
         AddeImageDescriptor aid = new AddeImageDescriptor(this.source);
         previewDir = aid.getDirectory();
         int eMag = 1;
         int lMag = 1;
         int eSize = 1;
         int lSize = 1;
         try {
             double feSize = (double)previewDir.getElements();
             double flSize = (double)previewDir.getLines();
             double feMag = (double)this.elementMag;
             double flMag = (double)this.lineMag;
             if ((feSize > 525.0) && (flSize > 500.0)) {
                 if (feSize > flSize) {
                     feMag = feSize/525.0;
                     flMag = feMag * (double)this.lineMag/(double)this.elementMag;
                 } else {
                     flMag = flSize/500.0;
                     feMag = flMag * (double)this.elementMag/(double)this.lineMag;
                 }
                 eMag = (int)(feMag + 0.5);
                 lMag = (int)(flMag + 0.5);
             }
         } catch(Exception e) {
            System.out.println("Error in makePreviewImage  e=" + e);
         }
 
         eSize = 525;
         lSize = 500;
         replaceKey(LINELE_KEY, (Object)("1 1"));
         replaceKey(PLACE_KEY, (Object)("ULEFT"));
         replaceKey(SIZE_KEY, (Object)(lSize + " " + eSize));
         replaceKey(MAG_KEY, (Object)(lMag + " " + eMag));
         replaceKey(BAND_KEY, (Object)(bi.getBandNumber()));
         replaceKey(UNIT_KEY, (Object)(unit));
 
         aid = new AddeImageDescriptor(baseSource);
         previewDir = aid.getDirectory();
         hasImagePreview = true;
     }
 
     private String removeKey(String src, String key) {
         String returnString = src;
         if (returnString.contains(key)) {
             String[] segs = returnString.split(key);
             String seg0 = segs[0];
             String seg1 = segs[1];
             int indx = seg1.indexOf("&");
             if (indx > 0) {
                 seg1 = seg1.substring(indx);
             }
             returnString = seg0 + seg1;
         }
         return returnString;
     }
 
     private String replaceKey(String src, String key, Object val) {
         String returnString = src;
         key = key.toUpperCase() + "=";
         if (returnString.contains(key)) {
             String[] segs = returnString.split(key);
             String seg0 = segs[0];
             String seg1 = segs[1];
             int indx = seg1.indexOf("&");
             if (indx < 0) {
                 seg1 = "";
             } else if (indx > 0) {
                 seg1 = seg1.substring(indx);
             }
             returnString = seg0 + key + val + seg1;
         }
         else {
             returnString = returnString + "&" + key + val;
         } 
         return returnString;
     }
 
     private String replaceKey(String src, String oldKey, String newKey, Object val) {
         String returnString = src;
         oldKey = oldKey.toUpperCase() + "=";
         newKey = newKey.toUpperCase() + "=";
         if (returnString.contains(oldKey)) {
             String[] segs = returnString.split(oldKey);
             String seg0 = segs[0];
             String seg1 = segs[1];
             int indx = seg1.indexOf("&");
             if (indx < 0) {
                 seg1 = "";
             } else if (indx > 0) {
                 seg1 = seg1.substring(indx);
             }
             returnString = seg0 + newKey + val + seg1;
         }
         else {
             returnString = returnString + "&" + newKey + val;
         }
         return returnString;
     }
 
     private void replaceKey(String key, Object val) {
         baseSource = replaceKey(baseSource, key, val);
     }
 
     private String getKey(String src, String key) {
         String returnString = "";
         key = key.toUpperCase() + "=";
         if (src.contains(key)) {
             String[] segs = src.split(key);
             segs = segs[1].split("&");
             returnString = segs[0];
         }
         return returnString;
     }
 
 
     /**
      * A utility method that helps us deal with legacy bundles that used to
      * have String file names as the id of a data choice.
      *
      * @param object     May be an AddeImageDescriptor (for new bundles) or a
      *                   String that is converted to an image descriptor.
      * @return The image descriptor.
      */
     public AddeImageDescriptor getDescriptor(Object object) {
         if (object == null) {
             return null;
         }
         if (object instanceof DataChoice) {
             object = ((DataChoice) object).getId();
         }
         if (object instanceof ImageDataInfo) {
             int index = ((ImageDataInfo) object).getIndex();
             if (index < myDataChoices.size()) {
                 DataChoice dc        = (DataChoice) myDataChoices.get(index);
                 Object     tmpObject = dc.getId();
                 if (tmpObject instanceof ImageDataInfo) {
                     return ((ImageDataInfo) tmpObject).getAid();
                 }
             }
             return null;
         }
 
         if (object instanceof AddeImageDescriptor) {
             return (AddeImageDescriptor) object;
         }
         return new AddeImageDescriptor(object.toString());
     }
 
     /**
      * Create the set of {@link ucar.unidata.data.DataChoice} that represent
      * the data held by this data source.  We create one top-level
      * {@link ucar.unidata.data.CompositeDataChoice} that represents
      * all of the image time steps. We create a set of children
      * {@link ucar.unidata.data.DirectDataChoice}, one for each time step.
      */
     public void doMakeDataChoices() {
         String type = (String) getProperty(PROP_IMAGETYPE, TYPE_SATELLITE);
         List<BandInfo> bandInfos =
             (List<BandInfo>) getProperty(PROP_BANDINFO, (Object) null);
         Hashtable props = Misc.newHashtable(DataChoice.PROP_ICON,
                                             (type.equals(TYPE_RADAR)
                                              ? "/auxdata/ui/icons/Radar.gif"
                                              : "/auxdata/ui/icons/Satellite.gif"));
 
         List categories = (imageList.size() > 1)
                           ? getTwoDTimeSeriesCategories()
                           : getTwoDCategories();
 
         // This is historical an is not added into the list of choices
         // for selection by the users.
         myCompositeDataChoice = new CompositeDataChoice(this, imageList,
                 getName(), getDataName(), categories, props);
         myCompositeDataChoice.setUseDataSourceToFindTimes(true);
         doMakeDataChoices(myCompositeDataChoice);
 
         if ((bandInfos != null) && !bandInfos.isEmpty()) {
             List biCategories = (imageList.size() > 1)
                                 ? getBandTimeSeriesCategories()
                                 : getBandCategories();
             if (bandInfos.size() == 1) {
                 BandInfo test  = (BandInfo) bandInfos.get(0);
                 List     units = test.getCalibrationUnits();
                 if ((units == null) || units.isEmpty()) {
                     return;
                 }
             }
             for (Iterator<BandInfo> i = bandInfos.iterator(); i.hasNext(); ) {
                 BandInfo bi      = i.next();
                 String   name    = makeBandParam(bi);
                 String   catName = bi.getBandDescription();
                 List biSubCategories = Misc.newList(new DataCategory(catName,
                                            true));
                 biSubCategories.addAll(biCategories);
                
                 List l = bi.getCalibrationUnits();
                 if (l.isEmpty() || (l.size() == 1)) {
                     DataChoice choice = new DirectDataChoice(this, bi, name,
                                             bi.getBandDescription(),
                                             biCategories, props);
                     addDataChoice(choice);
                 } else {
                     for (int j = 0; j < l.size(); j++) {
                         Object   o           = l.get(j);
                         BandInfo bi2         = new BandInfo(bi);
                         String   calUnit     = o.toString();
                         String   calibration = TwoFacedObject.getIdString(o);
                         bi2.setPreferredUnit(calibration);
                         name = makeBandParam(bi2);
                         DataChoice subChoice = new DirectDataChoice(this,
                                                    bi2, name, calUnit,
                                                    biSubCategories, props);
                         addDataChoice(subChoice);
                     }
                 }
             }
         } else {
             addDataChoice(myCompositeDataChoice);
         }
 
         if (sourceProps.containsKey(BAND_KEY)) {
             int bandProp = new Integer((String)(sourceProps.get(BAND_KEY))).intValue();
             int bandIndex = BandInfo.findIndexByNumber(bandProp, bandInfos);
             BandInfo bi = (BandInfo)bandInfos.get(bandIndex);
             String   name    = makeBandParam(bi);
             if (stashedChoices != null) {
                 int numChoices = stashedChoices.size();
                 for (int i=0; i<numChoices; i++) {
                    DataChoice choice = (DataChoice)stashedChoices.get(i);
                    if (name.equals(choice.getName())) {
                        setProperty(PROP_DATACHOICENAME, choice.getDescription());
                        return;
                    }
                 }
             }
         }
 
     }
 
     /**
      * Insert the new DataChoice into the dataChoice list.
      *
      * @param choice   new choice to add
      */
     protected void addDataChoice(DataChoice choice) {
         super.addDataChoice(choice);
         if (stashedChoices == null) {
             stashedChoices = new ArrayList();
         }
         stashedChoices.add(choice);
     }
 
     /**
      * Make the data choices and add them to the given composite
      *
      * @param composite The parent data choice to add to
      */
     private void doMakeDataChoices(CompositeDataChoice composite) {
         int cnt = 0;
         imageTimes = new ArrayList();
         List timeChoices = new ArrayList();
         myDataChoices = new ArrayList();
 
         String type = (String) getProperty(PROP_IMAGETYPE, TYPE_SATELLITE);
         Hashtable props = Misc.newHashtable(DataChoice.PROP_ICON,
                                             (type.equals(TYPE_RADAR)
                                              ? "/auxdata/ui/icons/clock.gif"
                                              : "/auxdata/ui/icons/clock.gif"));
 
         for (Iterator iter = imageList.iterator(); iter.hasNext(); ) {
             Object              object     = iter.next();
             AddeImageDescriptor aid        = getDescriptor(object);
             String              name       = aid.toString();
             DataSelection       timeSelect = null;
             if ( !aid.getIsRelative()) {
                 DateTime imageTime = aid.getImageTime();
                 if (imageTime != null) {
                     imageTimes.add(imageTime);
                     //We will create the  data choice with an index, not with the actual time.
                     timeSelect =
                         new DataSelection(Misc.newList(new Integer(cnt)));
                 }
             } else {
                 imageTimes.add(getRelativeTimeObject(aid));
             }
             timeSelect = null;
             DataChoice choice = new DirectDataChoice(this,
                                     new ImageDataInfo(cnt, aid),
                                     composite.getName(), name,
                                     getTwoDCategories(), timeSelect, props);
             myDataChoices.add(choice);
             cnt++;
             timeChoices.add(choice);
         }
         //Sort the data choices.
         composite.replaceDataChoices(sortChoices(timeChoices));
     }
 
     /**
      * Return the list of {@link ucar.unidata.data.DataCategory} used for
      * single time step data.
      * 
      * @return A list of categories.
      */
     public List getTwoDCategories() {
         if (twoDCategories == null) {
             makeCategories();
         }
         return twoDCategories;
     }
 
     /**
      * Return the list of {@link ucar.unidata.data.DataCategory} used for
      * multiple time step data.
      * 
      * @return A list of categories.
      */
     public List getTwoDTimeSeriesCategories() {
         if (twoDCategories == null) {
             makeCategories();
         }
         return twoDTimeSeriesCategories;
     }
 
     /**
      * Initialize the {@link ucar.unidata.data.DataCategory} objects that
      * this data source uses. 
      */
     private void makeCategories() {
         twoDTimeSeriesCategories =
             DataCategory.parseCategories("IMAGE-2-2D-TIME;", false);
         twoDCategories = DataCategory.parseCategories("IMAGE-2-2D;", false);
         bandCategories = DataCategory.parseCategories("IMAGE-2-BAND;", false);
         bandTimeSeriesCategories =
             DataCategory.parseCategories("IMAGE-2-BAND-TIME;", false);
 
     }
 
     /**
      * Return the list of {@link ucar.unidata.data.DataCategory} used for
      * single time step data with band information.
      *
      * @return A list of categories.
      */
     public List getBandCategories() {
         if (bandCategories == null) {
             makeCategories();
         }
         return bandCategories;
     }
 
     /**
      * Return the list of {@link ucar.unidata.data.DataCategory} used for
      * multiple time step data with band information.
      *
      * @return A list of categories.
      */
     public List getBandTimeSeriesCategories() {
         if (bandTimeSeriesCategories == null) {
             makeCategories();
         }
         return bandTimeSeriesCategories;
     }
 
     /** _more_ */
     private Range[] sampleRanges = null;
 
     /**
      * Create the actual data represented by the given
      * {@link ucar.unidata.data.DataChoice}.
      *
      * @param dataChoice        Either the
      *                          {@link ucar.unidata.data.CompositeDataChoice}
      *                          representing all time steps or a
      *                          {@link ucar.unidata.data.DirectDataChoice}
      *                          representing a single time step.
      * @param category          Not really used.
      * @param dataSelection     Defines any time subsets.
      * @param requestProperties extra request properties
      *
      * @return The image or image sequence data.
      *
      * @throws RemoteException    Java RMI problem
      * @throws VisADException     VisAD problem
      */
     public Data getDataInner(DataChoice dataChoice, DataCategory category,
                                 DataSelection dataSelection,
                                 Hashtable requestProperties)
             throws VisADException, RemoteException {
 /*
         System.out.println("\ngetDataInner:");
         System.out.println("    dataChoice=" + dataChoice);
         System.out.println("    category=" + category);
         System.out.println("    dataSelection=" + dataSelection);
         System.out.println("    requestProperties=" + requestProperties);
         System.out.println("    dataSelection.properties=" + dataSelection.getProperties());
 */
         if (dataSelection == null) return null;
         GeoSelection geoSelection = dataSelection.getGeoSelection(true);
 
         GeoLocationInfo gli = geoSelection.getBoundingBox();
         LatLonPoint llp = gli.getUpperLeft();
         llp = gli.getLowerRight();
 
         if (geoSelection == null) return null;
         if (this.lastGeoSelection == null) this.lastGeoSelection = geoSelection;
         this.selectionProps = dataSelection.getProperties();
 
         if (this.selectionProps.containsKey("MAG")) {
             String str = (String)this.selectionProps.get("MAG");
             String[] strs = StringUtil.split(str, " ", 2);
             this.lineMag = new Integer(strs[0]).intValue();
             this.elementMag = new Integer(strs[1]).intValue();
         }
 
         sampleRanges = null;
 
         if (dataChoice instanceof CompositeDataChoice) {
             return makeImageSequence(myCompositeDataChoice, dataSelection);
         } else if (hasBandInfo(dataChoice)) {
             return makeImageSequence(dataChoice, dataSelection);
         }
         Data img = (Data) makeImage(dataChoice, dataSelection);
         return img;
     }
 
     /**
      * Check if the DataChoice has a BandInfo for it's Id
      *
      * @param dataChoice  choice to check
      *
      * @return true if the choice ID is a BandInfo
      */
     private boolean hasBandInfo(DataChoice dataChoice) {
         Object id = dataChoice.getId();
         return dataChoice.getId() instanceof BandInfo;
     }
 
     /** _more_ */
     String readLabel;
     AreaDirectory[][] currentDirs;
 
     /**
      * Create the  image sequence defined by the given dataChoice.
      *
      * @param dataChoice     The choice.
      * @param subset     any time subsets.
      * @return The image sequence.
      *
      * @throws RemoteException    Java RMI problem
      * @throws VisADException     VisAD problem
      */
     protected ImageSequence makeImageSequence(DataChoice dataChoice,
             DataSelection subset)
             throws VisADException, RemoteException {
         Hashtable subsetProperties = subset.getProperties();
         Enumeration propEnum = subsetProperties.keys();
         int numLines = 0;
         int numEles = 0;
         for (int i=0; propEnum.hasMoreElements(); i++) {
             String key = propEnum.nextElement().toString();
             if (key.compareToIgnoreCase(SIZE_KEY) == 0) {
                 String sizeStr = (String)(subsetProperties.get(key));
                 String[] vals = StringUtil.split(sizeStr, " ", 2);
                 Integer iVal = new Integer(vals[0]);
                 numLines = iVal.intValue();
                 iVal = new Integer(vals[1]);
                 numEles = iVal.intValue();
                 break;
             }
         }
         AREACoordinateSystem macs = (AREACoordinateSystem)sampleMapProjection;
         int[] dirBlk = macs.getDirBlock();
         if (numLines == 0) {
             double elelin[][] = new double[2][2];
             double latlon[][] = new double[2][2];
             GeoSelection gs = subset.getGeoSelection();
             GeoLocationInfo gli = gs.getBoundingBox();
             if ((gli == null) && (lastGeoSelection != null)) {
                 subset.setGeoSelection(lastGeoSelection);
                 gs = lastGeoSelection;
                 gli = gs.getBoundingBox();
             }
             LatLonPoint llp = gli.getUpperLeft();
             latlon[0][0] = llp.getLatitude();
             latlon[1][0] = llp.getLongitude();
             llp = gli.getLowerRight();
             latlon[0][1] = llp.getLatitude();
             latlon[1][1] = llp.getLongitude();
             elelin = macs.fromReference(latlon);
 /*
             System.out.println("\nlatlon[0][0]=" + latlon[0][0] + " latlon[1][0]=" + latlon[1][0] +
                               " latlon[0][1]=" + latlon[0][1] + " latlon[1][1]=" + latlon[1][1]);
             System.out.println("elelin[0][0]=" + elelin[0][0] + " elelin[1][0]=" + elelin[1][0] +
                               " elelin[0][1]=" + elelin[0][1] + " elelin[1][1]=" + elelin[1][1]);
 */
             int line = (int)(elelin[1][0]+0.5)*dirBlk[11];
             int ele = (int)(elelin[0][0]+0.5)*dirBlk[12];
             numLines = (int)(Math.abs(elelin[1][0] - elelin[1][1]))*dirBlk[11];
             numEles = (int)(Math.abs(elelin[0][1] - elelin[0][0]))*dirBlk[12];
         }
 
         String ulString = dirBlk[5] + " " + dirBlk[6] + " I";
         Hashtable props = subset.getProperties();
         if (props.containsKey("LINELE")) {
             ulString = (String)props.get("LINELE");
         }
 /*
         latlon =  macs.toReference(elelin);
 
         System.out.println("\nelelin[0][0]=" + elelin[0][0] + " elelin[1][0]=" + elelin[1][0] +
                           " elelin[0][1]=" + elelin[0][1] + " elelin[1][1]=" + elelin[1][1]);
         System.out.println("latlon[0][0]=" + latlon[0][0] + " latlon[1][0]=" + latlon[1][0] +
                           " latlon[0][1]=" + latlon[0][1] + " latlon[1][1]=" + latlon[1][1]);
 */
         try {
             List descriptorsToUse = new ArrayList();
             if (hasBandInfo(dataChoice)) {
                 descriptorsToUse = getDescriptors(dataChoice, subset);
             } else {
                 List choices = (dataChoice instanceof CompositeDataChoice)
                                ? getChoicesFromSubset(
                                    (CompositeDataChoice) dataChoice, subset)
                                : Arrays.asList(new DataChoice[] {
                                    dataChoice });
                 for (Iterator iter = choices.iterator(); iter.hasNext(); ) {
                     DataChoice          subChoice = (DataChoice) iter.next();
                     AddeImageDescriptor aid =
                         getDescriptor(subChoice.getId());
                     if (aid == null) {
                         continue;
                     }
                     DateTime dttm = aid.getImageTime();
                     if ((subset != null) && (dttm != null)) {
                         List times = getTimesFromDataSelection(subset,
                                          dataChoice);
                         if ((times != null) && (times.indexOf(dttm) == -1)) {
                             continue;
                         }
                     }
                     descriptorsToUse.add(aid);
                 }
             }
 
             if (descriptorsToUse.size() == 0) {
                 return null;
             }
             AddeImageInfo biggestPosition = null;
             int           pos             = 0;
             //Find the descriptor with the largets position
             for (Iterator iter =
                     descriptorsToUse.iterator(); iter.hasNext(); ) {
                 AddeImageDescriptor aid = (AddeImageDescriptor) iter.next();
                 AddeImageInfo       aii = aid.getImageInfo();
 
                 //Are we dealing with area files here?
                 if (aii == null) {
                     break;
                 }
 
                 //Check if this is absolute time
                 if ((aii.getStartDate() != null)
                         || (aii.getEndDate() != null)) {
                     biggestPosition = null;
                     break;
                 }
                 if (Math.abs(aii.getDatasetPosition()) > pos) {
                     pos             = Math.abs(aii.getDatasetPosition());
                     biggestPosition = aii;
                 }
             }
 
             if (getCacheDataToDisk() && (biggestPosition != null)) {
                 biggestPosition.setRequestType(AddeImageInfo.REQ_IMAGEDIR);
                 AreaDirectoryList adl =
                     new AreaDirectoryList(biggestPosition.getURLString());
                 biggestPosition.setRequestType(AddeImageInfo.REQ_IMAGEDATA);
                 currentDirs = adl.getSortedDirs();
             } else {
                 currentDirs = null;
             }
 
             if (sequenceManager == null) {
                 sequenceManager = new ImageSequenceManager();
             }
             sequenceManager.clearSequence();
             ImageSequence sequence = null;
             int           cnt      = 1;
             DataChoice    parent   = dataChoice.getParent();
             for (Iterator iter =
                     descriptorsToUse.iterator(); iter.hasNext(); ) {
                 AddeImageDescriptor aid = (AddeImageDescriptor) iter.next();
                 if (currentDirs != null) {
                     int idx =
                         Math.abs(aid.getImageInfo().getDatasetPosition());
                     if (idx >= currentDirs.length) {
                         continue;
                     }
                 }
 
                 String label = "";
                 if (parent != null) {
                     label = label + parent.toString() + " ";
                 } else {
                     DataCategory displayCategory =
                         dataChoice.getDisplayCategory();
                     if (displayCategory != null) {
                         label = label + displayCategory + " ";
                     }
                 }
                 label = label + dataChoice.toString();
                 readLabel = "Time: " + (cnt++) + "/"
                             + descriptorsToUse.size() + "  " + label;
 
                 String src = "";
                 try {
                     src = aid.getSource();
                     src = replaceKey(src, LINELE_KEY, (Object)("1 1"));
                     String sizeString = "10 10";
                     src = replaceKey(src, SIZE_KEY, (Object)(sizeString));
                     String name = dataChoice.getName();
                     int idx = name.lastIndexOf("_");
                     String unit = name.substring(idx+1);
                     if (getKey(src, UNIT_KEY).equals(""))
                         src = replaceKey(src, UNIT_KEY, (Object)(unit));
                     AreaFile af = new AreaFile(src);
                     AreaDirectory ad = af.getAreaDirectory();
                     int lMag = this.lineMag;
                     int eMag = this.elementMag;
                     int lSize = numLines;
                     if (lMag > 0) {
                         lSize /= lMag;
                     } else if (lMag < 0) {
                         lSize /= -lMag;
                     }
                     int eSize = numEles;
                     if (eMag > 0) {
                         eSize /= eMag;
                     } else if (eMag < 0) {
                         eSize /= -eMag;
                     }
                     sizeString = lSize + " " + eSize;
                     src = replaceKey(src, SIZE_KEY, (Object)(sizeString));
                     src = replaceKey(src, MAG_KEY, (Object)(this.lineMag + " " + this.elementMag));
                     aid.setSource(src);
                     SingleBandedImage image = makeImage(aid, true, readLabel, subset);
                     if (image != null) {
                         sequence = sequenceManager.addImageToSequence(image);
                     }
                 } catch (VisADException ve) {
                     LogUtil.printMessage(ve.toString());
                 }
             }
             return sequence;
         } catch (Exception exc) {
             throw new ucar.unidata.util.WrapperException(exc);
         }
     }
 
     /**
      * Create the single image defined by the given dataChoice.
      *
      * @param aid AddeImageDescriptor
      * @param fromSequence _more_
      *
      * @return The data.
      *
      * @throws RemoteException    Java RMI problem
      * @throws VisADException     VisAD problem
      */
     private SingleBandedImage makeImage(AddeImageDescriptor aid,
                                         boolean fromSequence, 
                                         String readLabel, DataSelection subset)
             throws VisADException, RemoteException {
 /*
         System.out.println("makeImage:");
         System.out.println("    aid=" + aid);
         System.out.println("    fromSequence=" + fromSequence);
         System.out.println("    readLabel=" + readLabel);
         System.out.println("    subset=" + subset);
 */
         if (aid == null) {
             return null;
         }
         String src = aid.getSource();
         Hashtable props = subset.getProperties();
         if (props.containsKey("PLACE")) 
             src = replaceKey(src, "PLACE", props.get("PLACE"));
         if (props.containsKey("LATLON")) {
             src = replaceKey(src, "LINELE", "LATLON", props.get("LATLON"));
         }
         if (props.containsKey("LINELE")) {
             src = removeKey(src, "LATLON");
             src = replaceKey(src, "LINELE", props.get("LINELE"));
         }
         if (props.containsKey("MAG"))
             src = replaceKey(src, "MAG", props.get("MAG"));
         aid.setSource(src);
         SingleBandedImage result = (SingleBandedImage) getCache(src);
         if (result != null) {
             aid.setSource(src);
             setDisplaySource(src, props);
             //System.out.println("1 " + src);
             return result;
         }
         //For now handle non adde urls here
         try {
             if ( !src.startsWith("adde:")) {
                 AreaAdapter aa = new AreaAdapter(src, false);
                 result = aa.getImage();
                 putCache(src, result);
                 aid.setSource(src);
                 setDisplaySource(src, props);
                 //System.out.println("2 " + src);
                 return result;
             }
             AddeImageInfo aii     = aid.getImageInfo();
 
             AreaDirectory areaDir = null;
             try {
                 if (getCacheDataToDisk()) {
                     if (currentDirs != null) {
                         int    pos        =
                             Math.abs(aii.getDatasetPosition());
                         int    band       = 0;
                         String bandString = aii.getBand();
                         if ((bandString != null)
                                 && !bandString.equals(aii.ALL)) {
                             band = new Integer(bandString).intValue();
                         }
                         //TODO: even though the band is non-zero we might only get back one band
                         band = 0;
                         areaDir =
                             currentDirs[currentDirs.length - pos - 1][band];
                     } else {
                         //If its absolute time then just use the AD from the descriptor
                         if ((aii.getStartDate() != null)
                                 || (aii.getEndDate() != null)) {
                             areaDir = aid.getDirectory();
                         } else {
                         }
                     }
                 }
             } catch (Exception exc) {
                 LogUtil.printMessage("out looking up area dir");
                 exc.printStackTrace();
                 return null;
             }
             if ( !fromSequence) {
                 areaDir = null;
             }
             if (areaDir != null) {
                 int hash = aii.getURLString().hashCode();
                 String filename = IOUtil.joinDir(getDataCachePath(),
                                       "image_" + hash + "_" + aii.getBand()
                                       + "_"
                                       + ((areaDir.getStartTime() != null)
                                          ? "" + areaDir.getStartTime()
                                              .getTime()
                                          : "") + ".dat");
                 AddeImageFlatField aiff = AddeImageFlatField.create(aid,
                                               areaDir, getCacheDataToDisk(),
                                               filename, getCacheClearDelay(),
                                               readLabel);
 
                 aiff.setReadLabel(readLabel);
                 result = aiff;
                 if (sampleRanges == null) {
                     sampleRanges = aiff.getRanges(true);
                     if ((sampleRanges != null) && (sampleRanges.length > 0)) {
                         for (int rangeIdx = 0; rangeIdx < sampleRanges.length;
                                 rangeIdx++) {
                             Range r = sampleRanges[rangeIdx];
                             if (Double.isInfinite(r.getMin())
                                     || Double.isInfinite(r.getMax())) {
                                 sampleRanges = null;
                                 break;
                             }
                         }
                     }
                 } else {
                     aiff.setSampleRanges(sampleRanges);
                 }
             } else {
                 src = aid.getSource();
                 AreaAdapter aa = new AreaAdapter(src, false);
                 try {
                     AreaDirectory aDir = aa.getAreaDirectory();
                     int lineRes = aDir.getValue(11);
                     int eleRes = aDir.getValue(12);
                     if ((lineRes > 1) || (eleRes > 1)) {
                         String sizeStr = getKey(src, SIZE_KEY);
                         String[] vals = StringUtil.split(sizeStr, " ", 2);
                         Integer iVal = new Integer(vals[0]);
                         int linSize = iVal.intValue()/lineRes;
                         linSize *= Math.abs(this.lineMag);
                         iVal = new Integer(vals[1]);
                         int eleSize = iVal.intValue()/eleRes;
                         eleSize *= Math.abs(this.elementMag);
                         sizeStr = linSize + " " + eleSize;
                         src = replaceKey(src, SIZE_KEY, sizeStr);
                         aa = new AreaAdapter(src, false);
                     }
                 } catch (Exception e) {
                     System.out.println("e=" + e);
                 }
                 result = aa.getImage();
             }
             putCache(src, result);
             aid.setSource(src);
             setDisplaySource(src, props);
             //System.out.println("3 " + src);
             return result;
         } catch (java.io.IOException ioe) {
             throw new VisADException("Creating AreaAdapter - " + ioe);
         }
     }
 
 
     /**
      * Make a parmeter name for the BandInfo
      *
      * @param bi    the BandInfo in question
      *
      * @return  a name for the parameter
      */
     private String makeBandParam(BandInfo bi) {
         StringBuffer buf = new StringBuffer();
         buf.append(bi.getSensor());
         buf.append("_Band");
         buf.append(bi.getBandNumber());
         buf.append("_");
         buf.append(bi.getPreferredUnit());
         return buf.toString();
     }
 
     /**
      * Get the object that we use to display relative time. Relative time is defined
      * using an integer index, 0...n. We don't want to show the actual integer.
      * Rather we want to show "Third most recent", "Fourth most recent", etc.
      *
      * @param aid The image descriptor
      * @return The object that represents the relative time index of the aid
      */
     private Object getRelativeTimeObject(AddeImageDescriptor aid) {
         return new TwoFacedObject(aid.toString(),
                                   new Integer(aid.getRelativeIndex()));
     }
 
     /**
      * Sort the list of data choices on their time
      *
      * @param choices The data choices
      *
      * @return The data choices sorted
      */
     private List sortChoices(List choices) {
         Object[]   choicesArray = choices.toArray();
         Comparator comp         = new Comparator() {
             public int compare(Object o1, Object o2) {
                 AddeImageDescriptor aid1 = getDescriptor(o1);
                 AddeImageDescriptor aid2 = getDescriptor(o2);
                 if ((aid1 == null) || (aid2 == null)) {
                     return -1;
                 }
                 if (aid1.getIsRelative()) {
                     if (aid1.getRelativeIndex() < aid2.getRelativeIndex()) {
                         return 0;
                     } else if (aid1.getRelativeIndex()
                                == aid2.getRelativeIndex()) {
                         return 1;
                     }
                     return -1;
                 }
                 return aid1.getImageTime().compareTo(aid2.getImageTime());
             }
         };
         Arrays.sort(choicesArray, comp);
         return new ArrayList(Arrays.asList(choicesArray));
 
     }
 
     /**
      * Get a list of descriptors from the choice and subset
      *
      * @param dataChoice  Data choice
      * @param subset  subsetting info
      *
      * @return  list of descriptors matching the selection
      */
     public List getDescriptors(DataChoice dataChoice, DataSelection subset) {
         int linRes = this.lineResolution;
         int eleRes = this.elementResolution;
         int newLinRes = linRes;
         int newEleRes = eleRes;
         List times = getTimesFromDataSelection(subset, dataChoice);
         if ((times == null) || times.isEmpty()) {
             times = imageTimes;
         }
         List descriptors = new ArrayList();
         for (Iterator iter = times.iterator(); iter.hasNext(); ) {
             Object              time  = iter.next();
             AddeImageDescriptor found = null;
             for (Iterator iter2 = imageList.iterator(); iter2.hasNext(); ) {
                 AddeImageDescriptor aid = getDescriptor(iter2.next());
                 if (aid != null) {
                     if (aid.getIsRelative()) {
                         Object id = (time instanceof TwoFacedObject)
                                     ? ((TwoFacedObject) time).getId()
                                     : time;
                         if ((id instanceof Integer)
                                 && ((Integer) id).intValue()
                                    == aid.getRelativeIndex()) {
                             found = aid;
                             break;
                         }
 
                     } else {
                         if (aid.getImageTime().equals(time)) {
                             found = aid;
                             break;
                         }
                     }
 
                 }
             }
             if (found != null) {
                 try {
                     AddeImageDescriptor desc = new AddeImageDescriptor(found);
                     //Sometimes we might have a null imageinfo
                     if(desc.getImageInfo()!=null) {
                         AddeImageInfo aii =
                             (AddeImageInfo) desc.getImageInfo().clone();
                         BandInfo bi = (BandInfo) dataChoice.getId();
                         List<BandInfo> bandInfos =
                             (List<BandInfo>) getProperty(PROP_BANDINFO, (Object) null);
                         boolean hasBand = true;
                         //If this data source has been changed after we have create a display 
                         //then the possibility exists that the bandinfo contained by the incoming
                         //data choice might not be valid. If it isn't then default to the first 
                         //one in the list
                         if(bandInfos!=null) {
                             hasBand = bandInfos.contains(bi);
                             if(!hasBand) {
                             }
                             if(!hasBand && bandInfos.size()>0) {
                                 bi = bandInfos.get(0);
                             } else {
                                 //Not sure what to do here.
                             }
                         }
                         aii.setBand("" + bi.getBandNumber());
                         aii.setPlaceValue("ULEFT");
 
                         try {
                             AddeImageDescriptor newAid = new AddeImageDescriptor(aii.getURLString());
                             AreaDirectory newAd = newAid.getDirectory();
                             newLinRes = newAd.getValue(11);
                             newEleRes = newAd.getValue(12);
                         } catch (Exception e) {
                             System.out.println("can't reset resolution.  e=" + e);
                         }
 
                         double[][] projCoords = new double[2][2];
                         try {
                             AreaDirectory ad = desc.getDirectory();
                             double lin = (double)ad.getValue(5);
                             double ele = (double)ad.getValue(6);
                             aii.setLocateKey("LINELE");
                             aii.setLocateValue((int)lin + " " + (int)ele);
                             projCoords[0][0] = lin;
                             projCoords[1][0] = ele;
                             lin += (double)ad.getValue(8);
                             ele += (double)ad.getValue(9);
                             projCoords[0][1] = lin;
                             projCoords[1][1] = ele;
                         } catch (Exception e) {
                             System.out.println("exception e=" + e);
                             return descriptors;
                         }
                         int lins = Math.abs((int)(projCoords[1][1] - projCoords[1][0]));
                         int eles = Math.abs((int)(projCoords[0][1] - projCoords[0][0]));
                         lins = lins*linRes/newLinRes;
                         if (this.lineMag > 0) {
                             lins *= this.lineMag;
                         } else {
                             lins /= -this.lineMag;
                         }
 
                         eles = eles*eleRes/newEleRes;
 
                         if (elementMag > 0) {
                             eles *= elementMag;
                         } else {
                             eles /= -elementMag;
                         }
 
                         aii.setLines(lins);
                         aii.setElements(eles);
                         desc.setImageInfo(aii);
                         desc.setSource(aii.getURLString());
                     }
                     descriptors.add(desc);
                 } catch (CloneNotSupportedException cnse) {}
             }
         }
         return descriptors;
     }
 
     /**
      * Get the subset of the composite based on the selection
      *
      * @param choice  composite choice
      * @param subset  time selection
      *
      * @return subset list
      */
     private List getChoicesFromSubset(CompositeDataChoice choice,
                                       DataSelection subset) {
         List choices = choice.getDataChoices();
         if (subset == null) {
             return choices;
         }
         List times = subset.getTimes();
         if (times == null) {
             return choices;
         }
         times = TwoFacedObject.getIdList(times);
         List   subChoices = new ArrayList();
         Object firstTime  = times.get(0);
         if (firstTime instanceof Integer) {
             for (Iterator iter = times.iterator(); iter.hasNext(); ) {
                 subChoices.add(
                     choices.get(((Integer) iter.next()).intValue()));
             }
         } else {  // TODO: what if they are DateTimes?
             subChoices.addAll(choices);
         }
         return subChoices;
     }
 
     public void setDisplaySource(String src, Hashtable props) {
          if (!props.isEmpty()) {
              Enumeration propEnum = props.keys();
              for (int i=0; propEnum.hasMoreElements(); i++) {
                  String key = propEnum.nextElement().toString();
                  Object val = props.get(key);
                  if (!getKey(src, key).equals("")) {
                      src = replaceKey(src, key, val);
                  }
              }
          }
          this.displaySource = src;
     }
 
     public String getDisplaySource() {
         return this.displaySource;
     }
 }
