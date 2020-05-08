 
 package eionet.meta;
 
 import java.util.Vector;
 
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.Util;
 
 public class DsTable implements Comparable {
 
     private String id = null;
     private String dsID = null;
     private String shortName = null;
     private String identifier = null;
     private String version = null;
 
     private String name = null;
     private String nsID = null;
     private String parentNS = null;
     private String datasetName = null;
     private String dstIdentifier = null;
     private String dstStatus = null;
     private String dstWorkingUser = null;
     private String dstDate = null;
     private boolean gis = false;
 
     private String workingUser = null;
     private String workingCopy = null;
 
     private String compStr = null;
 
     private Vector elements = new Vector();
     private Vector simpleAttrs = new Vector();
     private Vector complexAttrs = new Vector();
 
     private int dstVersion = -1;
 
     private String owner = null;
 
     private int positionInDataset;
 
     /**
      *
      * @param id
      * @param dsID
      * @param shortName
      */
     public DsTable(String id, String dsID, String shortName) {
         this.id = id;
         this.shortName = shortName;
         this.dsID = dsID;
     }
 
     public String getID() {
         return id;
     }
 
     public String getDatasetID() {
         return dsID;
     }
 
     public String getShortName() {
         return shortName;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void addElement(DataElement element) {
         elements.add(element);
     }
 
     public void setElements(Vector elements) {
         this.elements = elements;
     }
 
     public Vector getElements() {
         return elements;
     }
 
     public void setNamespace(String nsID) {
         this.nsID = nsID;
     }
 
     public String getNamespace() {
         return nsID;
     }
 
     public void setSimpleAttributes(Vector v) {
         this.simpleAttrs = v;
     }
 
     public Vector getSimpleAttributes() {
         return simpleAttrs;
     }
 
     public void setComplexAttributes(Vector v) {
         this.complexAttrs = v;
     }
 
     public Vector getComplexAttributes() {
         return complexAttrs;
     }
 
     public Vector simpleAttributesTable() {
         Vector v = new Vector();
         return v;
     }
 
     public Vector complexAttributesTable() {
         Vector v = new Vector();
         return v;
     }
 
     public Vector elementsTable() {
         Vector v = new Vector();
         return v;
     }
 
     public boolean isWorkingCopy() {
         if (workingCopy==null) {
             return false;
         } else if (workingCopy.equals("Y")) {
             return true;
         } else {
             return false;
         }
     }
 
     public void setWorkingCopy(String workingCopy) {
         this.workingCopy = workingCopy;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public String getVersion() {
         return this.version;
     }
 
     public void setDatasetName(String dsName) {
         this.datasetName = dsName;
     }
 
     public String getDatasetName() {
         return this.datasetName;
     }
 
     public void setParentNs(String nsid) {
         this.parentNS = nsid;
     }
 
     public String getParentNs() {
         return parentNS;
     }
 
     public void setIdentifier(String identifier) {
         this.identifier = identifier;
     }
 
     public String getIdentifier() {
         return this.identifier;
     }
 
     public void setWorkingUser(String workingUser) {
         this.workingUser = workingUser;
     }
 
     public String getWorkingUser() {
         return this.workingUser;
     }
 
 
     public Vector getVersioningAttributes() {
         if (simpleAttrs==null) {
             return null;
         }
 
         Vector set = new Vector();
         for (int i=0; i<simpleAttrs.size(); i++) {
             DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
             if (attr.effectsVersion()) {
                 set.add(attr);
             }
         }
 
         return set;
     }
 
     public String getAttributeValueByShortName(String name) {
 
         DElemAttribute attr = null;
         for (int i=0; i<simpleAttrs.size(); i++) {
             attr = (DElemAttribute)simpleAttrs.get(i);
             if (attr.getShortName().equalsIgnoreCase(name)) {
                 return attr.getValue();
             }
         }
 
         return null;
     }
 
     public DElemAttribute getAttributeByShortName(String name) {
 
         // look from simple attributes
         for (int i=0; i<simpleAttrs.size(); i++) {
             DElemAttribute attr = (DElemAttribute)simpleAttrs.get(i);
             if (attr.getShortName().equalsIgnoreCase(name)) {
                 return attr;
             }
         }
 
         // if it wasn't in the simple attributes, look from complex ones
         for (int i=0; i<complexAttrs.size(); i++) {
             DElemAttribute attr = (DElemAttribute)complexAttrs.get(i);
             if (attr.getShortName().equalsIgnoreCase(name)) {
                 return attr;
             }
         }
 
         return null;
     }
 
     public void setCompStr(String compStr) {
         this.compStr = compStr;
     }
 
     public String getCompStr() {
         return compStr;
     }
 
     public void setGIS(boolean gis) {
         this.gis = gis;
     }
 
     public boolean hasGIS() {
         return gis;
     }
 
     public String getDstIdentifier() {
         return dstIdentifier;
     }
 
     public void setDstIdentifier(String dstIdentifier) {
         this.dstIdentifier = dstIdentifier;
     }
 
     public String getOwner() {
         return owner;
     }
 
     public void setOwner(String owner) {
         this.owner = owner;
     }
 
     public int compareTo(Object o) {
 
         if (!o.getClass().getName().endsWith("DsTable")) {
             return 1;
         }
 
         DsTable oTbl = (DsTable)o;
         String oCompStr = oTbl.getCompStr();
         if (oCompStr==null && compStr==null) {
             return 0;
         } else if (oCompStr==null) {
             return 1;
         } else if (compStr==null) {
             return -1;
         }
 
         return compStr.compareToIgnoreCase(oCompStr);
     }
 
     public String getRelativeTargetNs() {
 
         if (Util.isEmpty(dstIdentifier)) {
             if (Util.isEmpty(parentNS)) {
                 return "";
             } else {
                 return "/namespaces/" + parentNS;
             }
         } else {
             return "/datasets/" + dstIdentifier;
         }
     }
 
     public String getRelativeCorrespNs() {
 
         if (Util.isEmpty(dstIdentifier)) {
             if (Util.isEmpty(nsID)) {
                 return "";
             } else {
                 return "/namespaces/" + nsID;
             }
         } else {
             return "/datasets/" + dstIdentifier + "/tables/" + identifier;
         }
     }
 
     /*
      *
      */
     public String getReferenceURL() {
 
         if (getIdentifier()==null) {
             return null;
         }
 
         StringBuffer buf = new StringBuffer();
 
         String jspUrlPrefix = Props.getProperty(PropsIF.JSP_URL_PREFIX);
         if (jspUrlPrefix!=null) {
             buf.append(jspUrlPrefix);
         }
 
        buf.append("/datasets/latest/").append(getDstIdentifier()).append("/tables/").append(getIdentifier());
 
         return buf.toString();
     }
 
     /**
      *
      * @return
      */
     public String getDstStatus() {
         return dstStatus;
     }
 
     /**
      *
      * @param dstStatus
      */
     public void setDstStatus(String dstStatus) {
         this.dstStatus = dstStatus;
     }
 
     /**
      *
      * @return
      */
     public String getDstWorkingUser() {
         return dstWorkingUser;
     }
 
     /**
      *
      * @param dstWorkingUser
      */
     public void setDstWorkingUser(String dstWorkingUser) {
         this.dstWorkingUser = dstWorkingUser;
     }
 
     /**
      *
      * @return
      */
     public String getDstDate() {
         return dstDate;
     }
 
     /**
      *
      * @param dstDate
      */
     public void setDstDate(String dstDate) {
         this.dstDate = dstDate;
     }
 
     /**
      * @return the positionInDataset
      */
     public int getPositionInDataset() {
         return positionInDataset;
     }
 
     /**
      * @param positionInDataset the positionInDataset to set
      */
     public void setPositionInDataset(int positionInDataset) {
         this.positionInDataset = positionInDataset;
     }
 }
