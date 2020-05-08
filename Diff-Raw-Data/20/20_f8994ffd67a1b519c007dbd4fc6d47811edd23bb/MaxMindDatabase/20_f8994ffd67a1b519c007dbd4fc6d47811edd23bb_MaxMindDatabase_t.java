 /*
  * @author Daniel Einspanjer and Doug Moran
  * @since  April-16-2008
  * 
  */package com.maxmind.geoip;
 
 import java.io.IOException;
 
 import org.pentaho.di.core.Const;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.core.row.ValueMeta;
 import org.pentaho.di.core.row.ValueMetaInterface;
 
 
 import com.maxmind.geoip.Country;
 import com.maxmind.geoip.Location;
 import com.maxmind.geoip.LookupService;
 import com.maxmind.geoip.regionName;
 import com.maxmind.geoip.timeZone;
 
 /*
  * Encapsulates metadata about the individual MaxMind database files.  
  * 
  * The main thing I'm trying to do is keep selectedFields in each sub class as an array of the 
  * enum type in order to have the running code be fast and type safe for the individual subclass.
  * 
  * Also wanted to simply capture the metadata about each field in 1 place only.  It adds a little design 
  * complexity initially, but should make adding new databases and fields easy.
  * 
  */
 public abstract class MaxMindDatabase {
   LookupService lookupService = null;
 
   // TODO: Default values needs to handle non string types for long and lat on city DB.  Convert this to object and 
   // have the setter verify the type and do the conversion so it is ready at runtime.
   String[] defaultValues = null;
   String[] overrideFieldNames = null;
 
   /*
    * Returns info about all database fields in the database
    */
   public abstract IMaxmindMetaInterface[] getAllFields();
 
   /**
    * Called by the Kettle Engine to add a new row to the stream.  This baby should execute as fast as possible.
    * 
    * @param outputRow One row of data, the size of the array is assumed to be set and the implementation 
    * of this method is assumed to not violate that size
    * @param firstNewFieldIndex The index of the outputRow (column) to start adding the data to
    * @param ip The ip address to lookup
    */
   public abstract void getRowData(Object[] outputRow, int firstNewFieldIndex, String ip);
 
   /**
    * Sets the fields (columns) and metadata that will be returned when getRowData() is called.  Each of these arrays 
    * should be the same size and positionally reference the same field.
    * 
    * @param selectedFieldNames The list of field names - should match the name of the enum it represents
    * @param overrideFieldNames The field names to return for each database field.  Null or empty values 
    * will return the name of the enum as the field name.
    * @param defaultValues value to return if the IP addr is not found or the data is empty
    */
   public void setSelectedFields(String[] selectedFieldNames, String[] overrideFieldNames, String[] defaultValues) {
     this.defaultValues = defaultValues;
     this.overrideFieldNames = overrideFieldNames;
     setSelectedFields(selectedFieldNames);
   }
 
   /**
    * Used by subclasses to set the selected fields.  Needed an override so the subclasses can use the actual enum type
    * to store the selected values.
    * 
    * @param selectedFieldNames
    */
   abstract void setSelectedFields(String[] selectedFieldNames);
 
   /**
    * returns metadata about the selected fields.
    * @return array of IMaxmindMetaInterface class for each selected field
    */
   public abstract IMaxmindMetaInterface[] getSelectedFields();
 
   /**
    * returns String array of fieldnames available fro this database.
    * @return Text version Field Names
    */
   public final String[] getFieldNames() {
     IMaxmindMetaInterface[] fields = getAllFields();
     String[] fNames = new String[fields.length];
 
     for (int i = 0; i < fields.length; ++i) {
       fNames[i] = fields[i].toString();
     }
 
     return (fNames);
   }
 
   /**
    * Called by Kettle engine to set up metadata about the data row that will be returned
    * @param r Row Metadata
    * @param origin Step Origin 
    */
   public final void getFields(RowMetaInterface r, String origin) {
     IMaxmindMetaInterface[] selectedFelds = getSelectedFields();
     for ( int i = 0; i < selectedFelds.length; ++i ) {
       ValueMetaInterface v = selectedFelds[i].getValueMetadata();
      v.setName( Const.isEmpty( overrideFieldNames[i] ) ? selectedFelds[i].toString(): overrideFieldNames[i] );
       v.setOrigin(origin);
       r.addValueMeta(v);
     }
   }
 
   /**
    * Sets that location on the filesystem of the MaxMind Database
    * 
    * @param dbLocation String representing the path of the database file used by File class
    * @throws IOException
    */
   public void setDbLocation(String dbLocation) throws IOException {
     lookupService = MaxMindGeoIP.initLookupService(dbLocation);
   }
 
   /**
    * TODO: I'd like to add a user option for "Strict IP lookup" using InetAddr in case the input 
    * data is not perfect.  This method assumes a well formed ip address.
    * 
    * This is a heavily optimized method for extracting the long representation of the IP
    * address without creating several Regex or String objects for each call.
    * @param ip
    * @return
    */
   public final long getAddressFromIpV4(String ip) {
     if (ip == null)
       return 0;
     int length = ip.length();
     long result = 0;
     short blockNumber = 0;
     long block = 0;
     for (int i = 0; i < length; i++) {
       char c = ip.charAt(i);
       if (c == '.') {
         result += block << ((3 - blockNumber) * 8);
         blockNumber++;
         block = 0;
       } else {
         block = block * 10 + c - '0';
       }
     }
     result += block << ((3 - blockNumber) * 8);
     return result;
   }
 }
 
 /*
  * Used to get metadata out of the enums
  */
 interface IMaxmindMetaInterface {
   public ValueMetaInterface getValueMetadata();
 }
 
 /**
  * 
  * The following enums encapsulate metadata about the maxmind datasources.  The design was to have the 
  * fastest and safest runtime experience.  To make it easy to add fields and databases as the databases change
  * 
  * Not sure if these should be in different files.  Since they are only used here, I thought this was easiest to
  * mintain.
  *
  */
 
 /***********************************************************************************************
  * Maxmind City Database
  */
 final class MaxMindCityData extends MaxMindDatabase {
   enum CityFields implements IMaxmindMetaInterface {
     country_code(ValueMeta.TYPE_STRING, 2, 0) {
       Object getVal(Location l) {
         return (l.countryCode);
       }
     },
     country_name(ValueMeta.TYPE_STRING, 50, 0) {
       Object getVal(Location l) {
         return (l.countryName);
       }
     },
     region_code(ValueMeta.TYPE_STRING, 2, 0) {
       Object getVal(Location l) {
         return (l.region);
       }
     },
     region_name(ValueMeta.TYPE_STRING, 50, 0) {
       Object getVal(Location l) {
         return (regionName.regionNameByCode(l.countryCode, l.region));
       }
     },
     city_name(ValueMeta.TYPE_STRING, 255, 0) {
       Object getVal(Location l) {
         return (l.city);
       }
     },
     latitude(ValueMeta.TYPE_NUMBER, 10, 4) {
       Object getVal(Location l) {
         return ((double) l.latitude);
       }
     },
     longitude(ValueMeta.TYPE_NUMBER, 10, 4) {
       Object getVal(Location l) {
         return ((double) 
             l.longitude);
       }
     },
     timezone(ValueMeta.TYPE_STRING, 255, 0) {
       Object getVal(Location l) {
         return (timeZone.timeZoneByCountryAndRegion(l.countryCode, l.region));
       }
     };
 
     ValueMetaInterface valueMeta;
 
     private CityFields(int type, int length, int precision) {
       this.valueMeta = new ValueMeta(this.name(), type, length, precision);
     }
 
     public ValueMetaInterface getValueMetadata() {
       return (valueMeta);
     }
 
     abstract Object getVal(Location l);
   }
 
   CityFields[] selectedFields = new CityFields[0];
 
   @Override
   public void setSelectedFields(String[] fieldNames) {
     selectedFields = (fieldNames == null) ? new CityFields[0] : new CityFields[fieldNames.length];
     for (int i = 0; i < selectedFields.length; ++i) {
       selectedFields[i] = CityFields.valueOf(fieldNames[i]);
     }
   }
 
   @Override
   public IMaxmindMetaInterface[] getSelectedFields() {
     return (selectedFields);
   }
 
   @Override
   public IMaxmindMetaInterface[] getAllFields() {
     return (CityFields.values());
   }
 
   @Override
   public void getRowData(Object[] outputRow, int firstNewFieldIndex, String ip) {
     Location location = lookupService.getLocation(getAddressFromIpV4(ip));
     if (location != null) {
       Object o;
       for ( int i = 0; i < selectedFields.length; ++i) {
         o = selectedFields[i].getVal(location);
         outputRow[firstNewFieldIndex++] = ( o == null ) ? defaultValues[i] : o ;
       }
     } else {
       for (String field : defaultValues) {
         outputRow[firstNewFieldIndex++] = field;
       }
     }
   }
 }
 
 /***********************************************************************************************
  * Maxmind Country Database
  */
 final class MaxMindCountryData extends MaxMindDatabase {
   enum CountryFields implements IMaxmindMetaInterface {
     country_code(ValueMeta.TYPE_STRING, 2, 0) {
       Object getVal(Country co) {
         return (co.getCode());
       }
     },
     country_name(ValueMeta.TYPE_STRING, 50, 0) {
       Object getVal(Country co) {
         return (co.getName());
       }
     };
 
     ValueMetaInterface valueMeta;
 
     private CountryFields(int type, int length, int precision) {
       this.valueMeta = new ValueMeta(this.name(), type, length, precision);
     }
 
     public ValueMetaInterface getValueMetadata() {
       return (valueMeta);
     }
 
     abstract Object getVal(Country co);
   }
 
   CountryFields[] selectedFields = new CountryFields[0];
 
   @Override
   public void setSelectedFields(String[] fieldNames) {
     selectedFields = (fieldNames == null) ? new CountryFields[0] : new CountryFields[fieldNames.length];
     for (int i = 0; i < selectedFields.length; ++i) {
       selectedFields[i] = CountryFields.valueOf(fieldNames[i]);
     }
   }
 
   @Override
   public IMaxmindMetaInterface[] getSelectedFields() {
     return (selectedFields);
   }
 
   @Override
   public IMaxmindMetaInterface[] getAllFields() {
     return (CountryFields.values());
   }
 
   @Override
   public void getRowData(Object[] outputRow, int firstNewFieldIndex, String ip) {
     Country co = lookupService.getCountry(getAddressFromIpV4(ip));
     if (co != null) {
       Object o;
       for ( int i = 0; i < selectedFields.length; ++i) {
         o = selectedFields[i].getVal(co);
         outputRow[firstNewFieldIndex++] = ( o == null ) ? defaultValues[i] : o ;
       }
     } else {
       for (String field : defaultValues) {
         outputRow[firstNewFieldIndex++] = field;
       }
     }
   }
 }
 
 /***********************************************************************************************
  * Maxmind ISP Database
  */
 final class MaxMindIspData extends MaxMindDatabase {
   enum IspFields implements IMaxmindMetaInterface {
     isp_name(ValueMeta.TYPE_STRING, 255, 0) {
       Object getVal(LookupService ls, long ipNum) {
         return (ls.getOrg(ipNum));
       } // yes, this is correct, they use Org for both ISP and Org DB
     };
 
     ValueMetaInterface valueMeta;
 
     private IspFields(int type, int length, int precision) {
       this.valueMeta = new ValueMeta(this.name(), type, length, precision);
     }
 
     public ValueMetaInterface getValueMetadata() {
       return (valueMeta);
     }
 
     abstract Object getVal(LookupService ls, long ipNum);
   }
 
   IspFields[] selectedFields = new IspFields[0];
 
   @Override
   public void setSelectedFields(String[] fieldNames) {
     selectedFields = (fieldNames == null) ? new IspFields[0] : new IspFields[fieldNames.length];
     for (int i = 0; i < selectedFields.length; ++i) {
       selectedFields[i] = IspFields.valueOf(fieldNames[i]);
     }
   }
 
   @Override
   public IMaxmindMetaInterface[] getSelectedFields() {
     return (selectedFields);
   }
 
   @Override
   public IMaxmindMetaInterface[] getAllFields() {
     return (IspFields.values());
   }
 
   @Override
   public void getRowData(Object[] outputRow, int firstNewFieldIndex, String ip) {
     Object o;
     for ( int i = 0; i < selectedFields.length; ++i) {
       o = selectedFields[i].getVal(lookupService, getAddressFromIpV4(ip));
       outputRow[firstNewFieldIndex++] = (o == null) ? defaultValues[i] : o;
     }
   }
 }
 
 /***********************************************************************************************
  * Maxmind Organization Database
  */
 final class MaxMindOrgData extends MaxMindDatabase {
   enum OrgFields implements IMaxmindMetaInterface {
     organization_name(ValueMeta.TYPE_STRING, 255, 0) {
       Object getVal(LookupService ls, long ipNum) {
         return (ls.getOrg(ipNum));
       }
     };
 
     ValueMetaInterface valueMeta;
 
     private OrgFields(int type, int length, int precision) {
       this.valueMeta = new ValueMeta(this.name(), type, length, precision);
     }
 
     public ValueMetaInterface getValueMetadata() {
       return (valueMeta);
     }
 
     abstract Object getVal(LookupService ls, long ipNum);
   }
 
   OrgFields[] selectedFields = new OrgFields[0];
 
   @Override
   public void setSelectedFields(String[] fieldNames) {
     selectedFields = (fieldNames == null) ? new OrgFields[0] : new OrgFields[fieldNames.length];
     for (int i = 0; i < selectedFields.length; ++i) {
       selectedFields[i] = OrgFields.valueOf(fieldNames[i]);
     }
   }
 
   @Override
   public IMaxmindMetaInterface[] getSelectedFields() {
     return (selectedFields);
   }
 
   @Override
   public IMaxmindMetaInterface[] getAllFields() {
     return (OrgFields.values());
   }
 
   @Override
   public void getRowData(Object[] outputRow, int firstNewFieldIndex, String ip) {
     Object o;
     for ( int i = 0; i < selectedFields.length; ++i) {
       o = selectedFields[i].getVal(lookupService, getAddressFromIpV4(ip));
       outputRow[firstNewFieldIndex++] = (o == null) ? defaultValues[i] : o;
     }
   }
 }
