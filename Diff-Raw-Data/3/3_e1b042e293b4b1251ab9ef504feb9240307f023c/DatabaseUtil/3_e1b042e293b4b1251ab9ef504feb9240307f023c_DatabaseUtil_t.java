 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cty.util;
 
 import com.cty.object.base.*;
 import com.cty.object.group.Database;
 import com.cty.object.group.ObjectGroup;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.Map.Entry;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 
 /**
  *
  * @author Lex.Chen
  */
 public class DatabaseUtil {
 
     public final static int EXCLUDE_MATCH = 1;
     public final static int EXCLUDE_DIVISION = 2;
     public final static int EXCLUDE_EQUAL = 3;
     private static DatabaseUtil instance = null;
     public final static String DATABASE_FILEPATH = "data.aa";
     public final static String DATABASE_TMP_FILEPATH = "tmpdata.aa";
     private int dbHashCode;
     private Database database = null;
     private DataSerializer ser = null;
     private int currentHeadNo = 1;
 
     public int getCurrentHeadNo() {
         return currentHeadNo;
     }
 
     public void setCurrentHeadNo(int currentHeadNo) {
         this.currentHeadNo = currentHeadNo;
     }
 
     public static DatabaseUtil getInstance() {
         if (instance == null) {
             instance = new DatabaseUtil();
         }
         return instance;
     }
 
     public double getMinSpacerDecimal() {
         double minSpacerDecimal = 1;
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (Iterator<Entry<BaseObject, ObjectGroup>> it = ogMap.entrySet().iterator(); it.hasNext();) {
             Map.Entry<BaseObject, ObjectGroup> entry = it.next();
             BaseObject bo = entry.getKey();
             ObjectGroup og = entry.getValue();
             if (ObjectType.SPACER.equals(bo.getType())
                     && Double.compare(bo.getWidth(), Math.floor(bo.getWidth())) != 0
                     && og.getTotalAvailable() > 0) {
                 double tmpDec = bo.getDec();
                 if (Double.compare(minSpacerDecimal, tmpDec) > 0) {
                     minSpacerDecimal = tmpDec;
                 }
             }
         }
         return minSpacerDecimal;
     }
 
     public double getSecondaryMinSpacerDecimal() {
         double minSpacerDecimal = 1;
         double secondaryMinSpacerDecimal = 2;
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (Iterator<Entry<BaseObject, ObjectGroup>> it = ogMap.entrySet().iterator(); it.hasNext();) {
             Map.Entry<BaseObject, ObjectGroup> entry = it.next();
             BaseObject bo = entry.getKey();
             ObjectGroup og = entry.getValue();
             if (ObjectType.SPACER.equals(bo.getType())
                     && Double.compare(bo.getWidth(), Math.floor(bo.getWidth())) != 0
                     && og.getTotalAvailable() > 0) {
                 double tmpDec = bo.getDec();
                 if (Double.compare(minSpacerDecimal, tmpDec) > 0) {
                     secondaryMinSpacerDecimal = minSpacerDecimal;
                     minSpacerDecimal = tmpDec;
                 } else if (Double.compare(secondaryMinSpacerDecimal, tmpDec) > 0) {
                     secondaryMinSpacerDecimal = tmpDec;
                 }
             }
         }
         return secondaryMinSpacerDecimal;
     }
 
     public double getMinSpacerDecimal(double clearanceWidth) {
         int rate = 1;
         double tmpDouble = clearanceWidth;
         while (Double.compare(tmpDouble, 0.1) < 0) {
             rate *= 10;
             tmpDouble = Util.doubleMultiply(tmpDouble, rate);
         }
 
         double minSpacerDecimal = 1;
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (Iterator<Entry<BaseObject, ObjectGroup>> it = ogMap.entrySet().iterator(); it.hasNext();) {
             Map.Entry<BaseObject, ObjectGroup> entry = it.next();
             BaseObject bo = entry.getKey();
             ObjectGroup og = entry.getValue();
             if (ObjectType.SPACER.equals(bo.getType())
                     && Double.compare(bo.getWidth(), Math.floor(bo.getWidth())) != 0
                     && og.getTotalAvailable() > 0) {
                 double tmpDec = Util.doubleDivide(Util.getDecimal(Util.doubleMultiply(bo.getWidth(), rate)), rate);
                if (Double.compare(tmpDec, clearanceWidth) == 0) {
                    return tmpDec;
                }
                 if (Double.compare(tmpDec, 0) > 0 && Double.compare(minSpacerDecimal, tmpDec) > 0) {
                     minSpacerDecimal = tmpDec;
                 }
             }
         }
         return minSpacerDecimal;
     }
 
     public synchronized void init() throws IOException {
         if (ser == null) {
             ser = new DataSerializer();
         }
         if (database == null) {
             database = ser.doDeserialize(DATABASE_FILEPATH);
             currentHeadNo = 1;
             for (int i = 1; i < database.getJobList().length; i++) {
                 if (database.getJobList()[i] != null) {
                     currentHeadNo = i + 1;
                     break;
                 }
             }
             dbHashCode = database.hashCode();
         }
     }
 
     public void save2File() throws IOException {
         ser.doSerialize(database, DATABASE_FILEPATH);
         dbHashCode = database.hashCode();
     }
 
     public void readFromTmpFile() throws IOException {
         database = ser.doDeserialize(DATABASE_TMP_FILEPATH);
         new File(DATABASE_TMP_FILEPATH).delete();
     }
 
     public boolean isSaved() {
         return dbHashCode == database.hashCode();
     }
 
     public int getPaperSize() {
         return database.getPaperSize();
     }
 
     public void setPaperSize(int paperSize) {
         database.setPaperSize(paperSize);
     }
 
     public Integer[] getAvailableKnifeSetNoArr() {
         HashSet<Integer> ksArr = new HashSet<>();
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (BaseObject bo : ogMap.keySet()) {
             if (ObjectType.KNIFE.equals(bo.getType()) && ogMap.get(bo).getTotalAvailable() > 0) {
                 ksArr.add(bo.getSet());
             }
         }
         if (ksArr.size() > 0) {
             return ksArr.toArray(new Integer[]{});
         } else {
             return null;
         }
     }
 
     public Integer[] getKnifeRestNoArr() {
         database.getMachine().getKnifeRestAmount();
         Integer[] krnArr = new Integer[database.getMachine().getKnifeRestAmount()];
         for (int i = 0; i < krnArr.length; i++) {
             krnArr[i] = new Integer(i + 1);
         }
         return krnArr;
     }
 
     public ObjectColor[] getAvailableBSColorArr(int setNo, double gauge, boolean isFemale) {
         if (Double.compare(gauge, 0) <= 0) {
             return new ObjectColor[]{ObjectColor.UNKNOWN};
         }
         EnumSet<ObjectColor> bscSet = EnumSet.noneOf(ObjectColor.class);
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (BaseObject bo : ogMap.keySet()) {
             if (ObjectType.BONDEDSPACER.equals(bo.getType()) && ogMap.get(bo).getTotalAvailable() > 0) {
                 ColorPurpose cp = database.getCpMap().get(bo.getColor());
                 if (isFemale && (!SpacerSideType.FEMALE.equals(cp.getType())
                         || !cp.canHandle(gauge))) {
                     continue;
                 } else if (!isFemale && !SpacerSideType.MALE.equals(cp.getType())) {
                     continue;
                 }
                 bscSet.add(bo.getColor());
             }
         }
         if (bscSet.size() > 0) {
             return bscSet.toArray(new ObjectColor[]{});
         } else {
             return new ObjectColor[]{ObjectColor.UNKNOWN};
         }
     }
 
     public double getMinWidthOfBS(int setNo, ObjectColor oc) {
         double minWidth = 0;
 
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (BaseObject bo : ogMap.keySet()) {
             if (ObjectType.BONDEDSPACER.equals(bo.getType()) && bo.getColor().equals(oc)
                     && bo.getSet() == setNo) {
                 if (Double.compare(minWidth, 0) == 0 || Double.compare(bo.getWidth(), minWidth) < 0) {
                     minWidth = bo.getWidth();
                 }
             }
         }
 
         return minWidth;
     }
 
     public BaseObject[] getAvailableObjectWidthArr(int setNo, ObjectType oType) {
         HashSet<BaseObject> owSet = new HashSet<>();
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (BaseObject bo : ogMap.keySet()) {
             if (oType.equals(bo.getType()) && ogMap.get(bo).getTotalAvailable() > 0 && bo.getSet() == setNo) {
                 owSet.add(bo);
             }
         }
         if (owSet.size() > 0) {
             return owSet.toArray(new BaseObject[]{});
         } else {
             return null;
         }
     }
 
     public BaseObject[] getObjectWidthArr(int setNo, ObjectType oType) {
         HashSet<BaseObject> owSet = new HashSet<>();
         HashMap<BaseObject, ObjectGroup> ogMap = database.getObjGpMap();
         for (BaseObject bo : ogMap.keySet()) {
             if (oType.equals(bo.getType()) && bo.getSet() == setNo) {
                 owSet.add(bo);
             }
         }
         if (owSet.size() > 0) {
             return owSet.toArray(new BaseObject[]{});
         } else {
             return null;
         }
     }
 
     public Job createNewJob(int newHeadNo) {
         Job job = new Job();
         currentHeadNo = newHeadNo;
         job.setCreateTimestamp(new Date());
         job.setKnifeSetNo(NumberUtils.min(ArrayUtils.toPrimitive(getAvailableKnifeSetNoArr())));
         job.setHeadNo(currentHeadNo);
 //        job.setCoilWidth(650.0);
         job.setIsCenter(true);
         job.setIsEqualDivision(false);
         job.setScrapLeft(0);
         database.getJobList()[currentHeadNo - 1] = job;
         return job;
     }
 
     public Job getCurrentJob() {
         return database.getJobList()[currentHeadNo - 1];
     }
 
     public Job setCurrentJob(int currentHeadNo) {
         this.currentHeadNo = currentHeadNo;
         return database.getJobList()[currentHeadNo - 1];
     }
 
     public void getObject(AssignObject ao) {
         if (ObjectType.EMPTY.equals(ao.getBase().getType())) {
             return;
         }
         database.getObjGpMap().get(ao.getBase()).getEtsMap().get(ao.getError()).getOne();
     }
 
     public void putObject(AssignObject ao) {
         if (ObjectType.EMPTY.equals(ao.getBase().getType())) {
             return;
         }
         database.getObjGpMap().get(ao.getBase()).getEtsMap().get(ao.getError()).putOne();
     }
 
     public Map<BaseObject, ObjectGroup> getReadOnlyObjGpMap() {
         return Collections.unmodifiableMap(database.getObjGpMap());
     }
 
     public double getArborLength() {
         return database.getMachine().getArborLength();
     }
 
     public double getSepArborLength() {
         return database.getMachine().getSepArborLength();
     }
 
     public Job getJobByHeadNo(int headNo) {
         if (headNo < 1 || headNo > database.getMachine().getKnifeRestAmount()) {
             throw new IllegalArgumentException("Argument value wrong.");
         }
         return database.getJobList()[headNo - 1];
     }
 
     public Map<BaseObject, ObjectGroup> getAvailableObjs() {
 //        return Collections.unmodifiableMap(database.getObjGpMap());
         return database.getObjGpMap();
     }
 
     public Map<BaseObject, ObjectGroup> getScrapObjs() {
 //        return Collections.unmodifiableMap(database.getScrapMap());
         return database.getScrapMap();
     }
 
     public void releaseJob(int headNo) {
         if (database.getJobList()[headNo - 1] != null) {
             database.getJobList()[headNo - 1].clearArbor();
             database.getJobList()[headNo - 1] = null;
         }
     }
 
     public String generateObjGpMapAvailableStr() {
         StringBuilder builder = new StringBuilder();
         for (Iterator<Map.Entry<BaseObject, ObjectGroup>> itGM = database.getObjGpMap().entrySet().iterator(); itGM.hasNext();) {
             Map.Entry<BaseObject, ObjectGroup> gmEntry = itGM.next();
             BaseObject bo = gmEntry.getKey();
             ObjectGroup og = gmEntry.getValue();
             builder.append(bo.toFormatStr()).append(':').append(og.getTotalAvailable()).append(":");
             for (Iterator<Map.Entry<ErrorType, ErrorTypeStock>> itEM = og.getEtsMap().entrySet().iterator(); itEM.hasNext();) {
                 Map.Entry<ErrorType, ErrorTypeStock> emEntry = itEM.next();
                 ErrorType et = emEntry.getKey();
                 ErrorTypeStock ets = emEntry.getValue();
                 builder.append(et.getErrorFlag()).append(ets.getAvailable()).append("/");
             }
             builder.append("\n");
         }
         return builder.toString();
     }
 }
