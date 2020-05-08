 package gov.usgs.gdp.bean;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import org.geotools.data.FeatureReader;
 
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeType;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 @XStreamAlias("shape-set")
 public class ShapeFileSetBean implements XmlBean {
 	
 	@XStreamAlias("shape-set-name")
 	@XStreamAsAttribute
 	private String name;
 	
 	@XStreamAlias("prj-file")
 	private File projectionFile;
 	
 	@XStreamAlias("shp-file")
 	private File shapeFile;
 	
 	@XStreamAlias("db-file")
 	private File dbfFile;
 	
 	@XStreamAlias("idx-file")
 	private File shapeFileIndexFile;
 	
 	@XStreamAlias("attributes")
 	@XStreamImplicit
 	private List<String> attributeList;
 	
 	@XStreamAlias("features")
 	@XStreamImplicit
 	private List<String> featureList;
 	
 	@XStreamOmitField
 	private String chosenDataset;
 	@XStreamOmitField
 	private String chosenAttribute;
 	@XStreamOmitField
 	private String chosenFeature;
 
 
 	private static final long serialVersionUID = 1L;
 	
 	public static List<String> getAttributeListFromBean(ShapeFileSetBean shapeFileSetBean) throws IOException {
 		FileDataStore shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFileSetBean.getShapeFile());
         FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = shapeFileDataStore.getFeatureReader();
         try {
             SimpleFeatureType featureType = featureReader.getFeatureType();
             List<AttributeType> attribTypes = featureType.getTypes();
 
             List<String> result = new ArrayList<String>();
             for (AttributeType attribType : attribTypes) {
                 result.add(attribType.getName().toString());
             }
 
             // Iterate over the features in the shape file set so that the FileDataStore releases its lock on it.
             // If this is not done, some operating systems (namely Windows) will not be able to delete the PRJ file of
             // the shape file set until the JVM terminates.
             // TODO: File a Geotools bug report about this.
             while (featureReader.hasNext()) {
                 featureReader.next();
             }
 
             return result;
         } finally {
             if (featureReader != null) {
                 featureReader.close();
             }
             if (shapeFileDataStore != null) {
                 shapeFileDataStore.dispose();
             }
         }
 	}
 	
 	/**
 	 * Pull a feature list from, a ShapeFileSetBean
 	 * 
 	 * @param shapeFileSetBean
 	 * @return
 	 * @throws IOException 
 	 */
     public static List<String> getFeatureListFromBean(ShapeFileSetBean shapeFileSetBean) throws IOException {
 		FileDataStore shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFileSetBean.getShapeFile());
         FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = shapeFileDataStore.getFeatureReader();
         try {
             Set<String> attribValList = new TreeSet<String>();  // Will sort attrib values and nuke dupes.
 
             while (featureReader.hasNext()) {
                 SimpleFeature feature = featureReader.next();
                 String attribTypeIdentifer = shapeFileSetBean.getChosenAttribute().trim();
                 Object featureAttributeObject = feature.getAttribute(attribTypeIdentifer);
 
                 if (featureAttributeObject != null) {
                     attribValList.add(feature.getAttribute(attribTypeIdentifer).toString());
                 }
             }
 
             return new ArrayList<String>(attribValList);
         } finally {
             if (featureReader != null) {
                 featureReader.close();
             }
             if (shapeFileDataStore != null) {
                 shapeFileDataStore.dispose();
             }
         }
     }
 		
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 	
 	public List<String> getAttributeList() {
 		if (this.attributeList == null) {
             this.attributeList = new ArrayList<String>();
         }
 		return this.attributeList;
 	}
 
 	public String getChosenAttribute() {
 		return this.chosenAttribute;
 	}
 
 	public String getChosenFeature() {
 		return this.chosenFeature;
 	}
 
 	public File getDbfFile() {
 		return this.dbfFile;
 	}
 
 	public List<String> getFeatureList() {
 		if (this.featureList == null) {
             this.featureList = new ArrayList<String>();
         }
 		return this.featureList;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public File getProjectionFile() {
 		return this.projectionFile;
 	}
 
 	public File getShapeFile() {
 		return this.shapeFile;
 	}
 
 	public File getShapeFileIndexFile() {
 		return this.shapeFileIndexFile;
 	}
 
 	public void setAttributeList(List<String> localAttributeList) {
 		this.attributeList = localAttributeList;
 	}
 
 	public void setChosenAttribute(String localChosenAttribute) {
 		this.chosenAttribute = localChosenAttribute;
 	}
 
 	public void setChosenFeature(String chosenfeature) {
 		this.chosenFeature = chosenfeature;
 	}
 
 	public void setDbfFile(File localDbfFile) {
 		this.dbfFile = localDbfFile;
 	}
 	
 	public void setFeatureList(List<String> localFeatureList) {
 		this.featureList = localFeatureList;
 	}
 
 	public void setName(String localName) {
 		this.name = localName;
 	}
 
 	public void setProjectionFile(File localProjectionFile) {
 		this.projectionFile = localProjectionFile;
 	}
 
 	public void setShapeFile(File localShapeFile) {
 		this.shapeFile = localShapeFile;
 	}
 
 	public void setShapeFileIndexFile(File localShapeFileIndexFile) {
 		this.shapeFileIndexFile = localShapeFileIndexFile;
 	}
 
 	public String getChosenDataset() {
 		return this.chosenDataset;
 	}
 
 	public void setChosenDataset(String localChosenDataset) {
 		this.chosenDataset = localChosenDataset;
 	}
 
 	
 	/**
 	 * If a FilesBean contains the proper filetypes to create a ShapeFile set,
 	 * method creates a ShapeFileSetBean 
 	 * 
 	 * @param exampleFilesBean
 	 * @return
 	 */
     public static ShapeFileSetBean getShapeFileSetBeanFromFilesBean(
             FilesBean exampleFilesBean) {
         ShapeFileSetBean result = null;
         File projectionFile = null;
         File shapeFile = null;
         File dbFile = null;
         File shapeFileIndexFile = null;
         for (File file : exampleFilesBean.getFiles()) {
             if (file.getName().toLowerCase().contains(".shp")) {
                 shapeFile = file;
             }
             if (file.getName().toLowerCase().contains(".prj")) {
                 projectionFile = file;
             }
             if (file.getName().toLowerCase().contains(".dbf")) {
                 dbFile = file;
             }
             if (file.getName().toLowerCase().contains(".shx")) {
                 shapeFileIndexFile = file;
             }
         }
 
        if (projectionFile != null && shapeFile != null && dbFile != null && shapeFileIndexFile != null) {
             result = new ShapeFileSetBean();
             result.setName(shapeFile.getName().substring(0, shapeFile.getName().lastIndexOf(".")));
             result.setDbfFile(dbFile);
             result.setShapeFile(shapeFile);
             result.setProjectionFile(projectionFile);
             result.setShapeFileIndexFile(shapeFileIndexFile);
         }
         return result;
     }
 
     /**
      * Extract a ShapeFileSetBean from a List of type FilesBean that matches a FileSet Name
      * 
      * @param filesBeanList
      * @param fileSetName
      * @return ShapeFileSetBean if available, null if not
      */
 	public static ShapeFileSetBean getShapeFileSetBeanFromFilesBeanList(List<FilesBean> filesBeanList, String fileSetName) {
 		
 		for (FilesBean filesBean : filesBeanList) {
 			if (filesBean.getName().equals(fileSetName)) return ShapeFileSetBean.getShapeFileSetBeanFromFilesBean(filesBean);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public String toXml() {
 		XStream xstream = new XStream();
 		xstream.processAnnotations(ShapeFileSetBean.class);
 		StringBuffer sb = new StringBuffer();
 		String result = "";
 		sb.append(xstream.toXML(this));
 		result = sb.toString();
 		return result;
 	}
 }
