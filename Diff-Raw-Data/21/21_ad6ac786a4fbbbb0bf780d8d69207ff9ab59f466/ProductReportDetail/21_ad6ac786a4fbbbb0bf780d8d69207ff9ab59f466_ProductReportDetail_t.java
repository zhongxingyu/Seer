 /**
  * @(#)ProductReportDetail.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.booking.report.itinerary;
 
 import java.awt.*;
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import com.tourapp.tour.booking.detail.db.*;
 import com.tourapp.tour.product.base.db.*;
 import com.tourapp.tour.acctpay.db.*;
 import com.tourapp.tour.base.db.*;
 import com.tourapp.tour.product.hotel.db.*;
 
 /**
  *  ProductReportDetail - .
  */
 public class ProductReportDetail extends RecordReportDetail
 {
     /**
      * Default constructor.
      */
     public ProductReportDetail()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The main record for this screen.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?
      * @param properties Addition properties to pass to the screen.
      */
     public ProductReportDetail(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         this();
         this.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         super.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Override this to open the other files in the query.
      */
     public void openOtherRecords()
     {
         super.openOtherRecords();
         new Vendor(this);
         new City(this);
         City record = new City(this);
         try {
             record.hasNext();   // Make sure this is open before I change the name.
         } catch (DBException e) {
             e.printStackTrace();
         }
         record.setTableNames("To" + record.getTableNames(false));
     }
     /**
      * Number of Screen Fields in this screen.
      * @return screen field count.
      */
     public int getSFieldCount()
     {
         int iFieldCount = super.getSFieldCount();
         Record recBookingDetail = this.getRecord(BookingDetail.BOOKING_DETAIL_FILE);
         if (recBookingDetail != null)
             if ((recBookingDetail.getEditMode() == DBConstants.EDIT_CURRENT) || (recBookingDetail.getEditMode() == DBConstants.EDIT_IN_PROGRESS))
         {
             BaseTable currentTable = recBookingDetail.getTable().getCurrentTable();
             if (currentTable == null)
                 currentTable = recBookingDetail.getTable();    // First time only
             recBookingDetail = currentTable.getRecord();
             Record recProduct = ((ReferenceField)recBookingDetail.getField(BookingDetail.PRODUCT_ID)).getReferenceRecord();
             if (recProduct != null)
             {
                //iFieldCount = iFieldCount - (recProduct.getFieldSeq(Hotel.MEAL_PLAN_DAYS_PARAM) + 1) + recProduct.getFieldCount();
                 if ((DBConstants.TRUE.equalsIgnoreCase(this.getProperty(City.CITY_FILE))) || (DBConstants.YES.equalsIgnoreCase(this.getProperty(City.CITY_FILE))))
                 {
                     Record recCity = this.getRecord(City.CITY_FILE);
                     ((ReferenceField)recProduct.getField(Product.CITY_ID)).setReferenceRecord(recCity);
                     ((ReferenceField)recProduct.getField(Product.CITY_ID)).getReference();
                     ((ReferenceField)recProduct.getField(Product.CITY_ID)).setReferenceRecord(null);
                     Record recCity2 = this.getRecord("To" + City.CITY_FILE);
                     try {
                         recCity2.addNew();
                     } catch (DBException e) {
                         e.printStackTrace();
                     }
                     if (recProduct instanceof TransportProduct)
                     {
                         recCity2.setTableNames(City.CITY_FILE);
                         ((ReferenceField)recProduct.getField(TransportProduct.TO_CITY_ID)).setReferenceRecord(recCity2);
                         ((ReferenceField)recProduct.getField(TransportProduct.TO_CITY_ID)).getReference();
                         ((ReferenceField)recProduct.getField(TransportProduct.TO_CITY_ID)).setReferenceRecord(null);                   
                         recCity2.setTableNames("To" + recCity2.getTableNames(false));
                     }
                 }
                 if ((DBConstants.TRUE.equalsIgnoreCase(this.getProperty(Vendor.VENDOR_FILE))) || (DBConstants.YES.equalsIgnoreCase(this.getProperty(Vendor.VENDOR_FILE))))
                 {
                     Record recVendor = this.getRecord(Vendor.VENDOR_FILE);
                     ((ReferenceField)recProduct.getField(Product.VENDOR_ID)).setReferenceRecord(recVendor);
                     ((ReferenceField)recProduct.getField(Product.VENDOR_ID)).getReference();
                     ((ReferenceField)recProduct.getField(Product.VENDOR_ID)).setReferenceRecord(null);
                 }
             }
         }
         return iFieldCount;
     }
     /**
      * SetupSFields Method.
      */
     public void setupSFields()
     {
         if ((DBConstants.TRUE.equalsIgnoreCase(this.getProperty(City.CITY_FILE))) || (DBConstants.YES.equalsIgnoreCase(this.getProperty(City.CITY_FILE))))
         {
             Record recCity = this.getRecord(City.CITY_FILE);
             for (int iFieldSeq = 0; iFieldSeq < recCity.getFieldCount(); iFieldSeq++)
             {
                 this.addDetailXMLColumn(recCity, recCity.getField(iFieldSeq).getFieldName());
             }
         
             Record recCity2 = this.getRecord("To" + City.CITY_FILE);
             for (int iFieldSeq = 0; iFieldSeq < recCity2.getFieldCount(); iFieldSeq++)
             {
                 this.addDetailXMLColumn(recCity2, recCity2.getField(iFieldSeq).getFieldName());
             }
         }
         if ((DBConstants.TRUE.equalsIgnoreCase(this.getProperty(Vendor.VENDOR_FILE))) || (DBConstants.YES.equalsIgnoreCase(this.getProperty(Vendor.VENDOR_FILE))))
         {
             Record recVendor = this.getRecord(Vendor.VENDOR_FILE);
             for (int iFieldSeq = 0; iFieldSeq < recVendor.getFieldCount(); iFieldSeq++)
             {
                 this.addDetailXMLColumn(recVendor, recVendor.getField(iFieldSeq).getFieldName());
             }
         }
         
         Record recBookingDetail = this.getRecord(BookingDetail.BOOKING_DETAIL_FILE);
        for (int iFieldSeq = 0; iFieldSeq < 80; iFieldSeq++)    // Hotel.HOTEL_FIELDS is the largest possible value
         {
            int iProductID = recBookingDetail.getFieldSeq(BookingDetail.PRODUCT_ID);
            this.addDetailXMLColumn(recBookingDetail, iProductID, iFieldSeq);
         }
     }
 
 }
