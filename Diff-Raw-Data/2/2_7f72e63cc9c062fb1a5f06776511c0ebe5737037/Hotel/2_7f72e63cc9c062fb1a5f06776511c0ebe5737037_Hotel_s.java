 /**
  * @(#)Hotel.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.product.hotel.db;
 
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
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import com.tourapp.tour.product.base.db.*;
 import com.tourapp.tour.base.db.*;
 import java.util.*;
 import com.tourapp.tour.acctpay.db.*;
 import java.text.*;
 import com.tourapp.thin.app.booking.entry.search.*;
 import com.tourapp.tour.message.hotel.request.*;
 import org.jbundle.thin.base.message.*;
 import org.jbundle.base.message.core.trx.*;
 import com.tourapp.tour.message.base.response.*;
 import com.tourapp.tour.product.tour.db.*;
 import org.jbundle.thin.base.screen.*;
 import org.jbundle.main.msg.db.*;
 import com.tourapp.tour.message.base.request.*;
 import com.tourapp.tour.message.hotel.request.data.*;
 import com.tourapp.tour.message.hotel.response.data.*;
 import com.tourapp.tour.message.hotel.response.*;
 import com.tourapp.tour.message.base.request.data.*;
 import com.tourapp.tour.message.base.response.data.*;
 import org.jbundle.main.db.base.*;
 import org.jbundle.model.message.*;
 import com.tourapp.model.tour.booking.detail.db.*;
 import com.tourapp.model.tour.booking.inventory.db.*;
 import com.tourapp.model.tour.booking.db.*;
 import com.tourapp.model.tour.product.hotel.db.*;
 
 /**
  *  Hotel - Hotel.
  */
 public class Hotel extends Product
      implements HotelModel
 {
     private static final long serialVersionUID = 1L;
 
     protected HotelMealPricing m_recHotelMealPricing = null;
     public static final int MEAL_PRICING_GRID_SCREEN = ScreenConstants.DETAIL_MODE | ScreenConstants.LAST_MODE * 512;
     /**
      * Default constructor.
      */
     public Hotel()
     {
         super();
     }
     /**
      * Constructor.
      */
     public Hotel(RecordOwner screen)
     {
         this();
         this.init(screen);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwner screen)
     {
         m_recHotelMealPricing = null;
         super.init(screen);
     }
     /**
      * Get the table name.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return (m_tableName == null) ? Record.formatTableNames(HOTEL_FILE, bAddQuotes) : super.getTableNames(bAddQuotes);
     }
     /**
      * Get the name of a single record.
      */
     public String getRecordName()
     {
         return "Hotel";
     }
     /**
      * Get the Database Name.
      */
     public String getDatabaseName()
     {
         return "product";
     }
     /**
      * Is this a local (vs remote) file?.
      */
     public int getDatabaseType()
     {
         return DBConstants.LOCAL | DBConstants.USER_DATA;
     }
     /**
      * MakeScreen Method.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         ScreenParent screen = null;
         if ((iDocMode & Hotel.PRICING_GRID_SCREEN) == Hotel.PRICING_GRID_SCREEN)
             screen = Record.makeNewScreen(HotelPricing.HOTEL_PRICING_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Hotel.INVENTORY_GRID_SCREEN) == Hotel.INVENTORY_GRID_SCREEN)
             screen = Record.makeNewScreen(HotelInventoryModel.HOTEL_INVENTORY_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Hotel.INVENTORY_SCREEN) == Hotel.INVENTORY_SCREEN)
             screen = Record.makeNewScreen(HotelInventoryModel.HOTEL_INVENTORY_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Hotel.MEAL_PRICING_GRID_SCREEN) == Hotel.MEAL_PRICING_GRID_SCREEN)
             screen = Record.makeNewScreen(HotelMealPricing.HOTEL_MEAL_PRICING_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & Hotel.RANGE_ADJUST_SCREEN) == Hotel.RANGE_ADJUST_SCREEN)
             screen = Record.makeNewScreen(HotelInventoryModel.HOTEL_INVENTORY_RANGE_ADJUST_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MAINT_MODE) == ScreenConstants.MAINT_MODE)
             screen = Record.makeNewScreen(HOTEL_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
        else if ((iDocMode & ScreenConstants.DISPLAY_MODE) == ScreenConstants.DISPLAY_MODE)
             screen = Record.makeNewScreen(HOTEL_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else
             screen = super.makeScreen(itsLocation, parentScreen, iDocMode, properties);
         return screen;
     }
     /**
      * Add this field in the Record's field sequence.
      */
     public BaseField setupField(int iFieldSeq)
     {
         BaseField field = null;
         //if (iFieldSeq == 0)
         //{
         //  field = new CounterField(this, ID, 8, null, null);
         //  field.setHidden(true);
         //}
         //if (iFieldSeq == 1)
         //{
         //  field = new RecordChangedField(this, LAST_CHANGED, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //  field.setHidden(true);
         //}
         //if (iFieldSeq == 2)
         //{
         //  field = new BooleanField(this, DELETED, Constants.DEFAULT_FIELD_LENGTH, null, new Boolean(false));
         //  field.setHidden(true);
         //}
         if (iFieldSeq == 3)
             field = new ProductDesc(this, DESCRIPTION, 50, null, null);
         if (iFieldSeq == 4)
             field = new StringField(this, CODE, 10, null, null);
         if (iFieldSeq == 5)
         {
             field = new VendorField(this, VENDOR_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setNullable(false);
             field.addListener(new InitOnceFieldHandler(null));
         }
         //if (iFieldSeq == 6)
         //  field = new StringField(this, OPERATORS_CODE, 20, null, null);
         if (iFieldSeq == 7)
             field = new HotelChainField(this, PRODUCT_CHAIN_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 8)
         {
             field = new CityField(this, CITY_ID, 3, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         //if (iFieldSeq == 9)
         //  field = new TimeField(this, ETD, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 10)
         //  field = new ShortField(this, ACK_DAYS, 2, null, null);
         //if (iFieldSeq == 11)
         //  field = new MemoField(this, COMMENTS, 32767, null, null);
         //if (iFieldSeq == 12)
         //  field = new PropertiesField(this, PROPERTIES, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 13)
         //  field = new XmlField(this, ITINERARY_DESC, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //if (iFieldSeq == 14)
         //  field = new ProductDescSort(this, DESC_SORT, 10, null, null);
         if (iFieldSeq == 15)
         {
             field = new ProductTypeAutoField(this, PRODUCT_TYPE, 15, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 16)
         {
             field = new FullCurrencyField(this, PRODUCT_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 17)
         {
             field = new CurrencyField(this, PRODUCT_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         //if (iFieldSeq == 18)
         //  field = new MessageTransportSelect(this, PRODUCT_MESSAGE_TRANSPORT_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 19)
         {
             field = new InventoryStatusField(this, DISPLAY_INVENTORY_STATUS_ID, Constants.DEFAULT_FIELD_LENGTH, null, new Integer(BaseStatus.NO_STATUS));
             field.setVirtual(true);
         }
         if (iFieldSeq == 20)
         {
             field = new ShortField(this, INVENTORY_AVAILABILITY, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 21)
         {
             field = new StringField(this, CURRENCY_CODE, 3, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 22)
         {
             field = new StringField(this, CURRENCY_CODE_LOCAL, 3, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 23)
         {
             field = new StringField(this, VENDOR_NAME, 30, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 24)
         {
             field = new CostStatusField(this, DISPLAY_COST_STATUS_ID, Constants.DEFAULT_FIELD_LENGTH, null, new Integer(BaseStatus.NULL_STATUS));
             field.setVirtual(true);
         }
         if (iFieldSeq == 25)
         {
             field = new FullCurrencyField(this, PP_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 26)
         {
             field = new CurrencyField(this, PP_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 27)
         {
             field = new BaseRateField(this, RATE_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 28)
         {
             field = new BaseClassField(this, CLASS_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 29)
         {
             field = new CurrencyField(this, PRODUCT_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 30)
         {
             field = new CurrencyField(this, PP_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 31)
             field = new TimeField(this, CHECK_OUT, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 32)
         {
             field = new BooleanField(this, SAME_AS_VENDOR, 1, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 33)
             field = new StringField(this, CONTACT, 30, null, null);
         if (iFieldSeq == 34)
             field = new StringField(this, CONTACT_TITLE, 30, null, null);
         if (iFieldSeq == 35)
             field = new StringField(this, ADDRESS_LINE_1, 40, null, null);
         if (iFieldSeq == 36)
             field = new StringField(this, ADDRESS_LINE_2, 40, null, null);
         if (iFieldSeq == 37)
             field = new StringField(this, CITY_OR_TOWN, 15, null, null);
         if (iFieldSeq == 38)
             field = new StringField(this, STATE_OR_REGION, 15, null, null);
         if (iFieldSeq == 39)
             field = new StringField(this, POSTAL_CODE, 10, null, null);
         if (iFieldSeq == 40)
             field = new StringField(this, COUNTRY, 15, null, null);
         if (iFieldSeq == 41)
             field = new PhoneField(this, TEL, 20, null, null);
         if (iFieldSeq == 42)
             field = new FaxField(this, FAX, 20, null, null);
         if (iFieldSeq == 43)
             field = new EMailField(this, EMAIL, 40, null, null);
         if (iFieldSeq == 44)
             field = new ShortField(this, ROOMS, 4, null, null);
         if (iFieldSeq == 45)
             field = new StringField(this, GENERAL_MANAGER, 20, null, null);
         if (iFieldSeq == 46)
             field = new StringField(this, SALES_MANAGER, 20, null, null);
         if (iFieldSeq == 47)
             field = new StringField(this, LOCAL_CONTACT, 20, null, null);
         if (iFieldSeq == 48)
             field = new PhoneField(this, LOCAL_PHONE, 20, null, null);
         if (iFieldSeq == 49)
             field = new PhoneField(this, TOLL_FREE_PHONE, 20, null, null);
         if (iFieldSeq == 50)
             field = new PhoneField(this, ALT_PHONE, 20, null, null);
         if (iFieldSeq == 51)
             field = new ShortField(this, ONE_FREE, 2, null, new Short((short)15));
         if (iFieldSeq == 52)
             field = new HotelFreeField(this, FREE_TYPE, 1, null, "S");
         if (iFieldSeq == 53)
         {
             field = new ShortField(this, CHILD_AGE, 2, null, null);
             field.addListener(new InitOnceFieldHandler(null));
         }
         if (iFieldSeq == 54)
         {
             field = new FullCurrencyField(this, SINGLE_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 55)
         {
             field = new FullCurrencyField(this, DOUBLE_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 56)
         {
             field = new FullCurrencyField(this, TRIPLE_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 57)
         {
             field = new FullCurrencyField(this, QUAD_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 58)
         {
             field = new FullCurrencyField(this, ROOM_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 59)
         {
             field = new FullCurrencyField(this, MEAL_COST, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 60)
         {
             field = new CurrencyField(this, SINGLE_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 61)
         {
             field = new CurrencyField(this, DOUBLE_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 62)
         {
             field = new CurrencyField(this, TRIPLE_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 63)
         {
             field = new CurrencyField(this, QUAD_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 64)
         {
             field = new CurrencyField(this, ROOM_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 65)
         {
             field = new CurrencyField(this, MEAL_COST_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 66)
         {
             field = new CurrencyField(this, SINGLE_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 67)
         {
             field = new CurrencyField(this, DOUBLE_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 68)
         {
             field = new CurrencyField(this, TRIPLE_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 69)
         {
             field = new CurrencyField(this, QUAD_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 70)
         {
             field = new CurrencyField(this, ROOM_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (iFieldSeq == 71)
         {
             field = new CurrencyField(this, MEAL_PRICE_LOCAL, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setVirtual(true);
         }
         if (field == null)
             field = super.setupField(iFieldSeq);
         return field;
     }
     /**
      * Add this key area description to the Record.
      */
     public KeyArea setupKey(int iKeyArea)
     {
         KeyArea keyArea = null;
         if (iKeyArea == 0)
         {
             keyArea = this.makeIndex(DBConstants.UNIQUE, "ID");
             keyArea.addKeyField(ID, DBConstants.ASCENDING);
         }
         if (iKeyArea == 1)
         {
             keyArea = this.makeIndex(DBConstants.SECONDARY_KEY, "Code");
             keyArea.addKeyField(CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 2)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "DescSort");
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 3)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "VendorID");
             keyArea.addKeyField(VENDOR_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 4)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "CityID");
             keyArea.addKeyField(CITY_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (iKeyArea == 5)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "OperatorsCode");
             keyArea.addKeyField(OPERATORS_CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 6)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "ProductChainID");
             keyArea.addKeyField(PRODUCT_CHAIN_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_SORT, DBConstants.ASCENDING);
         }
         if (keyArea == null)
             keyArea = super.setupKey(iKeyArea);     
         return keyArea;
     }
     /**
      * Free Method.
      */
     public void free()
     {
         if (m_recHotelMealPricing != null)
         {
             m_recHotelMealPricing.free();
             m_recHotelMealPricing = null;
         }
         super.free();
     }
     /**
      * Read the locally stored product cost (Override).
      */
     public Message processCostRequestInMessage(Message messageIn, Message messageReply)
     {
         ProductRequest productRequest = (ProductRequest)((BaseMessage)messageIn).getMessageDataDesc(null);
         String NO_ROOM_RATE = "No room rate";
         BaseApplication application = null;
         if (this.getRecordOwner() != null)
             if (this.getRecordOwner().getTask() != null)
                 application = (BaseApplication)this.getRecordOwner().getTask().getApplication();
         if (application == null)
             application = (BaseApplication)BaseApplet.getSharedInstance().getApplication();
         int iCostStatus = BaseStatus.VALID;
         String strErrorMessage = DBConstants.BLANK;
         HotelMessageData productMessageData = (HotelMessageData)productRequest.getMessageDataDesc(ProductRequest.PRODUCT_MESSAGE);
         PassengerMessageData passengerMessageData  = (PassengerMessageData)productRequest.getMessageDataDesc(ProductRequest.PASSENGER_MESSAGE);
         
         Date dateTarget = productMessageData.getTargetDate();
         int iRateType = productMessageData.getRateTypeID();
         int iRateClass = productMessageData.getRateClassID();
         short sNights = productMessageData.getNights();
         
         HotelRateResponse responseMessage = null;
         if (messageReply == null)
         {
             messageReply = new TreeMessage(null, null);
             responseMessage =  new HotelRateResponse((BaseMessage)messageReply, null);
         }
         else
             responseMessage = (HotelRateResponse)((BaseMessage)messageReply).getMessageDataDesc(null);
         responseMessage.moveRequestInfoToReply(messageIn);
         HotelRateResponseMessageData responseMessageData = (HotelRateResponseMessageData)responseMessage.getMessageDataDesc(ProductRateResponse.PRODUCT_RESPONSE_MESSAGE);
         
         // First, calculate the room cost
         double dTotalRoomCost = 0;
         double dTotalLocalRoomPrice = 0;
         for (int iRoomCategory = PaxCategory.SINGLE_ID, iFieldSeq = this.getFieldSeq(Hotel.SINGLE_COST), iPriceFieldSeq = this.getFieldSeq(Hotel.SINGLE_PRICE_LOCAL); iRoomCategory <= PaxCategory.CHILD_ID; iRoomCategory++, iFieldSeq++, iPriceFieldSeq++)
         {
             double dRoomCost = this.getHotelCost(dateTarget, iRateType, iRateClass, (short)1, iRoomCategory, false);
             responseMessageData.setRoomCost(iRoomCategory, dRoomCost);
             dRoomCost = this.getHotelCost(dateTarget, iRateType, iRateClass, sNights, iRoomCategory, false);
             double dRoomPriceLocal = this.getHotelCost(dateTarget, iRateType, iRateClass, sNights, iRoomCategory, true);
             if (iFieldSeq <= this.getFieldSeq(Hotel.QUAD_COST))
             {
                 this.getField(iFieldSeq).setValue(dRoomCost);
                 this.getField(iPriceFieldSeq).setValue(dRoomPriceLocal);
             }
             int iPaxInRoom = passengerMessageData.getPaxInRoom(iRoomCategory);
             int iRoomCapacity = iRoomCategory;
             if (iRoomCategory == PaxCategory.CHILD_ID)
                 iRoomCapacity = 1;
             if ((iPaxInRoom > 0) && (dRoomCost == 0))
             {
                 iCostStatus = BaseStatus.NOT_VALID;
                 strErrorMessage = NO_ROOM_RATE;
                 if (application != null)
                     strErrorMessage = application.getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(NO_ROOM_RATE);
             }
             dTotalRoomCost += dRoomCost * iPaxInRoom / iRoomCapacity;
             dTotalLocalRoomPrice += dRoomPriceLocal * iPaxInRoom / iRoomCapacity;
         }
         responseMessageData.setTotalRoomCost(dTotalRoomCost);
         
         short iTotalPax = passengerMessageData.getTargetPax();
         // Now, calculate the meal costs
         double dTotalMealCost = 0;
         double dTotalMealPriceLocal = 0;
         for (int iFieldSeq = 1; iFieldSeq <= 4; iFieldSeq++)
         {
             int iMealPlanID = productMessageData.getMealPlanID(iFieldSeq);
             if (iMealPlanID > 0)
             {
                 int iMeals = productMessageData.getMealQuantity(iFieldSeq);
                 int iMealDays = productMessageData.getMealDays(iFieldSeq);
                 Date dateMeal = new Date(dateTarget.getTime());
                 for (int iDay = 0; ; iDay++)
                 {
                     dateMeal.setTime(dateMeal.getTime() + (iDay * DBConstants.KMS_IN_A_DAY));    // Next day
                     if (iMeals == 0)
                         break;  // All done
                     if (iMealDays == 0)
                         iMeals--;
                     else
                     {
                         if (((1 << iDay) & iMealDays) == 0)
                             iMeals--;
                         else
                             continue;
                         iMealDays = (~(1 << iDay)) & iMealDays;
                     }
                     double dMealCost = this.getMealCost(dateMeal, iMealPlanID, false);
                     double dMealPriceLocal = this.getMealCost(dateMeal, iMealPlanID, true);
                     if (dMealCost == 0)
                     {
                         iCostStatus = BaseStatus.NOT_VALID;
                         String NO_MEAL_RATE = "No meal";
                         strErrorMessage = NO_MEAL_RATE;
                         if (application != null)
                             strErrorMessage = MessageFormat.format(application.getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(NO_MEAL_RATE), iFieldSeq);
                     }
                     dTotalMealCost += dMealCost * iTotalPax;
                     dTotalMealPriceLocal += dMealPriceLocal * iTotalPax;
                 }
             }
         }
         responseMessageData.setTotalMealCost(dTotalMealCost);
         
         double dTotalCost = dTotalRoomCost + dTotalMealCost;
         double dTotalPriceLocal = dTotalLocalRoomPrice + dTotalMealPriceLocal;
         dTotalCost = Math.floor(dTotalCost * 100.00 + 0.5) / 100.00;
         
         this.getField(Product.PRODUCT_COST).setValue(dTotalCost);
         this.getField(Product.PRODUCT_PRICE_LOCAL).setValue(dTotalPriceLocal);
         responseMessageData.setProductCost(dTotalCost);
         
         if (dTotalCost == 0)
         {
             iCostStatus = BaseStatus.NOT_VALID;
             strErrorMessage = NO_ROOM_RATE;
             if (application != null)
                 strErrorMessage = application.getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(NO_ROOM_RATE);
         }
         this.getField(Product.DISPLAY_COST_STATUS_ID).setValue(iCostStatus);
         responseMessage.setMessageDataStatus(iCostStatus);
         if (iCostStatus != BaseStatus.VALID)
             responseMessage.setMessageDataError(strErrorMessage);
         return messageReply;
     }
     /**
      * Check the inventory for this detail.
      * @param message Contains all the update data for this check
      * @param fldTrxID If null, just check the inventory, if not null, update the inventory using this BookingDetail trxID.
      */
     public Message processAvailabilityRequestInMessage(Message messageIn, Message messageReply, Field fldTrxID)
     {
         ProductRequest productRequest = (ProductRequest)((BaseMessage)messageIn).getMessageDataDesc(null);
         ProductMessageData productMessageData = (ProductMessageData)productRequest.getMessageDataDesc(ProductRequest.PRODUCT_MESSAGE);
         PassengerMessageData passengerMessageData = (PassengerMessageData)productRequest.getMessageDataDesc(ProductRequest.PASSENGER_MESSAGE);
         Date dateTarget = productMessageData.getTargetDate();
         int iRateID = productMessageData.getRateTypeID();
         int iClassID = productMessageData.getRateClassID();
         Object objOtherID = productMessageData.get(Product.OTHER_ID_PARAM);
         if (objOtherID == null)
             objOtherID = InventoryModel.NO_OTHER;
         int iOtherID = Integer.parseInt(objOtherID.toString());
         String strErrorMessage = null;
         int iInventoryStatus = InventoryStatus.VALID;
         
         BaseProductResponse responseMessage = null;
         if (messageReply == null)
             messageReply = (BaseMessage)this.getMessageProcessInfo().createReplyMessage((BaseMessage)productRequest.getMessage());
         responseMessage = (BaseProductResponse)((BaseMessage)messageReply).getMessageDataDesc(null);
         responseMessage.moveRequestInfoToReply(messageIn);
         
         //       First, calculate the room cost
         int iAvailability = 0;
         int iRooms = 0;
         int iNights = 0;
         if (productMessageData instanceof HotelMessageData)    // Could be CancelRequest (then nights = 0)
             iNights = ((HotelMessageData)productMessageData).getNights();
         int iErrorCode = DBConstants.NORMAL_RETURN;
         int iRoomCategory = PaxCategory.SINGLE_ID;
         Set<Integer> setSurvey = this.getInventory().surveyInventory(fldTrxID); 
         for (int iFieldSeq = this.getFieldSeq(Hotel.SINGLE_COST); iRoomCategory <= PaxCategory.CHILD_ID; iRoomCategory++, iFieldSeq++)
         {
             dateTarget = productMessageData.getTargetDate();
             int iPaxInRoom = passengerMessageData.getPaxInRoom(iRoomCategory);
             if (productRequest instanceof CancelRequest)    // CancelRequest (then pax = 0)
                 iPaxInRoom = 0;
             int iRoomCapacity = iRoomCategory;
             if (iRoomCategory == PaxCategory.CHILD_ID)
                 iRoomCapacity = 1;
             iRooms = iPaxInRoom / iRoomCapacity;
             if (iRooms > 0)
             {
                 for (int day = 0; day < iNights; day++)
                 {
                     InventoryModel recInventory = (InventoryModel)this.getInventory().getAvailability(this, dateTarget, iRateID, iClassID, iOtherID);
                     iAvailability = InventoryModel.NO_INVENTORY;
                     if (recInventory != null)
                         iAvailability = (int)recInventory.getField(InventoryModel.AVAILABLE).getValue();
                     if ((recInventory != null) && (fldTrxID != null)) // If in update mode, update the availability
                     { // No need to check avail, updateAvailability checks the correct availability (taking into account previous the previous usage) 
                         boolean bIsDeleted = false;   // todo (don) Fix this.
                         iErrorCode = recInventory.updateAvailability(iRooms, fldTrxID, iRoomCategory, bIsDeleted, setSurvey);
                         if (iErrorCode != DBConstants.NORMAL_RETURN)
                             break;
                     }
                     else if (iAvailability < iRooms)
                         break;
                     else if (iAvailability == InventoryModel.NO_INVENTORY)
                         break;
                     DateConverter.initGlobals();
                     DateConverter.gCalendar.setTime(dateTarget);
                     DateConverter.gCalendar.add(Calendar.DATE, +1);
                     dateTarget = DateConverter.gCalendar.getTime();
                 }
             }
             if ((iAvailability < iRooms) || (iAvailability == InventoryModel.NO_INVENTORY)
                     || (iErrorCode != DBConstants.NORMAL_RETURN))
                 break;
         }
         if (fldTrxID != null) // If in update mode, fix the availability
         {
             if ((iAvailability < iRooms) || (iAvailability == InventoryModel.NO_INVENTORY)
                 || (iErrorCode != DBConstants.NORMAL_RETURN))
             { // Have to back-out the changes and return.
                 int iErrorCode2 = this.getInventory().removeInventory(fldTrxID);
                 if (iErrorCode2 != DBConstants.NORMAL_RETURN)
                     iErrorCode = iErrorCode2;
                 iInventoryStatus = BaseDataStatus.NOT_VALID;
                 if (strErrorMessage == null)
                 {
                     if ((this.getRecordOwner() != null)
                         && (this.getRecordOwner().getTask() != null))
                             strErrorMessage = this.getRecordOwner().getTask().getLastError(iErrorCode);
                     else
                         strErrorMessage = "Inventory not available";
                 }
             }
             else
             {
                 this.getInventory().removeTrxs(fldTrxID, setSurvey);  // Remove any transactions that are no longer used
             }
         }
         if (responseMessage instanceof ProductAvailabilityResponse)
         {
             ProductResponseMessageData responseMessageData = (ProductResponseMessageData)responseMessage.getMessageDataDesc(HotelRateResponse.PRODUCT_RESPONSE_MESSAGE);
             responseMessageData.setAvailability(iAvailability);
         }
         
         if (iAvailability < iRooms)
         {
             strErrorMessage = InventoryStatus.NO_INVENTORY_ERROR_MESSAGE;
             if (this.getRecordOwner() != null)
                 if (this.getRecordOwner().getTask() != null)
             {
                 strErrorMessage = this.getRecordOwner().getTask().getApplication().getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(strErrorMessage);
                 strErrorMessage = MessageFormat.format(strErrorMessage, iAvailability, dateTarget);
             }
             iInventoryStatus = InventoryStatus.NOT_VALID;
         }
         else if (iAvailability == InventoryModel.NO_INVENTORY)
         {
             strErrorMessage = InventoryStatus.LOOKUP_ERROR_MESSAGE;
             if (this.getRecordOwner() != null)
                 if (this.getRecordOwner().getTask() != null)
                     strErrorMessage = this.getRecordOwner().getTask().getApplication().getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(strErrorMessage);
             iInventoryStatus = InventoryStatus.ERROR;
         }
         this.getField(Product.DISPLAY_INVENTORY_STATUS_ID).setValue(iInventoryStatus);
         responseMessage.setMessageDataStatus(iInventoryStatus);
         if (strErrorMessage != null)
             responseMessage.setMessageDataError(strErrorMessage);
         return messageReply;
     }
     /**
      * This is for products that can be externally booked.
      * @return the booking reply message with the proper params.
      */
     public Message processBookingRequestInMessage(Message messageIn, Message messageReply)
     {
         return super.processBookingRequestInMessage(messageIn, messageReply);
     }
     /**
      * GetProductBookingResponse Method.
      */
     public ProductBookingResponse getProductBookingResponse(String strRequestType, Message message, String strKey)
     {
         if (RequestType.BOOKING_ADD.equalsIgnoreCase(strRequestType))
             return new HotelBookingResponse((BaseMessage)message, strKey);
         else if (RequestType.BOOKING_CHANGE.equalsIgnoreCase(strRequestType))
             return new HotelBookingChangeResponse((BaseMessage)message, strKey);
         else
             return super.getProductBookingResponse(strRequestType, message, strKey);
     }
     /**
      * Create the booking detail for this product type.
      */
     public BookingDetailModel getBookingDetail(RecordOwner recordOwner)
     {
         return (BookingDetailModel)Record.makeRecordFromClassName(BookingHotelModel.THICK_CLASS, recordOwner);
     }
     /**
      * Calc the hotel cost given parameters.
      */
     public double getHotelCost(Date dateTarget, int iRateType, int iRateClass, short sNights, int iRoomType, boolean bGetPrice)
     {
         double dCost = 0;
         if (dateTarget == null)
             return 0;
         int iHotelID = (int)this.getField(Hotel.ID).getValue();
         while (sNights > 0)
         {
             HotelPricing recProductCostLookup = ((HotelPricing)this.getProductPricing()).getHotelCost(iHotelID, dateTarget, iRateType, iRateClass, iRoomType);
             if (recProductCostLookup != null)
             {
                 if (!bGetPrice)
                     dCost += recProductCostLookup.getCost(HotelPricing.ROOM_COST, this.getProductTerms());
                 else
                     dCost += recProductCostLookup.getField(HotelPricing.ROOM_PRICE).getValue();
             }
             else
                 return 0;   // No cost for this day = error
             dateTarget = new Date(dateTarget.getTime() + DBConstants.KMS_IN_A_DAY);
             sNights--;
         }
         return dCost;
     }
     /**
      * Get the cost for meals.
      */
     public double getMealCost(Date dateTarget, int iMealPlanID, boolean bGetPrice)
     {
         double dCost = 0;
         if (m_recHotelMealPricing == null)
         {
             m_recHotelMealPricing = new HotelMealPricing(this.findRecordOwner());
             if (m_recHotelMealPricing.getRecordOwner() != null)
                 m_recHotelMealPricing.getRecordOwner().removeRecord(m_recHotelMealPricing);
         }
         int iHotelID = (int)this.getField(Hotel.ID).getValue();
         HotelMealPricing recProductCostLookup = ((HotelMealPricing)m_recHotelMealPricing).getMealCost(iHotelID, dateTarget, iMealPlanID);
         if (recProductCostLookup != null)
         {
             if (!bGetPrice)
                 dCost += recProductCostLookup.getCost(HotelMealPricing.COST, this.getProductTerms());
             else
                 dCost += recProductCostLookup.getField(HotelMealPricing.PRICE).getValue();
         }
         return dCost;
     }
     /**
      * GetMealDesc Method.
      */
     public String getMealDesc(Date dateTarget, int iRateType, int iRateClass, boolean bDetailedDesc, MealPlan recMealPlan, ProductPricing recProductCost)
     {
         String strMealDesc = DBConstants.BLANK;
         int iHotelID = (int)this.getField(Hotel.ID).getValue();
         if (recProductCost == null)
             recProductCost = this.getProductPricing();
         recProductCost = ((HotelPricing)recProductCost).getHotelCost(iHotelID, dateTarget, iRateType, iRateClass, PaxCategory.DOUBLE_ID);
         if (recProductCost != null)
         {
             MealPlan recMealPlanNew = null;
             if (recMealPlan == null)
                 recMealPlan = recMealPlanNew = new MealPlan(this.findRecordOwner());
             strMealDesc += recMealPlan.getMealDesc(recProductCost.getField(HotelPricing.MEAL_PLAN_ID), bDetailedDesc);
             if (recMealPlanNew != null)
                 recMealPlanNew.free();
         }
         return strMealDesc;
     }
     /**
      * Using this booking detail, create or change the pricing
      * using the price amount in the ProductPricing record.
      * @param recBookingLine The line file
      * @param recBookingDetail The detail for this type of product
      * @return NORMAL_RETURN if price exists and was added
      * @return ERROR_RETURN if no pricing (or a zero price) was added.
      */
     public int updateBookingPricing(BookingLineModel recBookingLine, BookingDetailModel recBookingDetail, int iChangeType)
     {
         int iErrorCode = DBConstants.ERROR_RETURN;  // Error just means that there were no line items added
         BookingModel recBooking = recBookingDetail.getBooking(true);
         
         int iHotelID = (int)this.getField(Product.ID).getValue();
         Date dateTarget = ((DateTimeField)recBookingDetail.getField(BookingDetailModel.DETAIL_DATE)).getDateTime();
         int iRateID = (int)recBookingDetail.getField(BookingDetailModel.RATE_ID).getValue();
         int iClassID = (int)recBookingDetail.getField(BookingDetailModel.CLASS_ID).getValue();
         
         for (int iPaxCategory = PaxCategory.SINGLE_ID; iPaxCategory <= PaxCategory.CHILD_ID; iPaxCategory++)
         {
             short sTargetPax = (short)recBooking.getField(((Record)recBooking).getFieldSeq(BookingModel.SINGLE_PAX) + iPaxCategory - PaxCategory.SINGLE_ID).getValue();
             if (sTargetPax == 0)
                 continue;
             HotelPricing recProductPricing = null;
             double dAmount = 0;
             short sNights = (short)recBookingDetail.getField(BookingHotelModel.NIGHTS).getValue();
             while (sNights > 0)
             {
                 recProductPricing = ((HotelPricing)this.getProductPricing()).getHotelCost(iHotelID, dateTarget, iRateID, iClassID, iPaxCategory);
                 if (recProductPricing != null)
                     dAmount = dAmount + recProductPricing.getField(ProductPricing.PRICE).getValue();
                 dateTarget = new Date(dateTarget.getTime() + DBConstants.KMS_IN_A_DAY);
                 sNights--;
             }
             if (recProductPricing != null)
                 if (dAmount != 0)
             {
                 int iPricingType = PricingType.COMPONENT_PRICING;
                 int iQuantity = sTargetPax;
                 boolean bCommissionable = recProductPricing.getField(ProductPricing.COMMISSIONABLE).getState();
                 double dCommissionRate = recProductPricing.getField(ProductPricing.COMMISSION_RATE).getValue();
                 String strPayAt = recProductPricing.getField(ProductPricing.PAY_AT).toString();
                 int iErrorCode2 = recBookingDetail.updateBookingLine(recBookingLine, iPricingType, iPaxCategory, iQuantity, dAmount, bCommissionable, dCommissionRate, strPayAt, PricingStatus.OKAY, iChangeType);
                 if (iErrorCode2 == DBConstants.NORMAL_RETURN)
                     iErrorCode = DBConstants.NORMAL_RETURN;   // Some Valid pricing was added
             }
         }
         return iErrorCode;   // For now
     }
     /**
      * CreateProductPricing Method.
      */
     public ProductPricing createProductPricing(RecordOwner recordOwner)
     {
         return new HotelPricing(recordOwner);
     }
     /**
      * MarkupPriceFromCost Method.
      */
     public void markupPriceFromCost(double dMarkup, boolean bMarkupOnlyIfNoPrice)
     {
         super.markupPriceFromCost(dMarkup, bMarkupOnlyIfNoPrice);
         if (dMarkup == 0.00)
         {
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.SINGLE_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.SINGLE_PRICE_LOCAL).setData(null);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.DOUBLE_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.DOUBLE_PRICE_LOCAL).setData(null);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.TRIPLE_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.TRIPLE_PRICE_LOCAL).setData(null);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.QUAD_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.QUAD_PRICE_LOCAL).setData(null);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.ROOM_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.ROOM_PRICE_LOCAL).setData(null);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.MEAL_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.MEAL_PRICE_LOCAL).setData(null);
         }
         else
         {
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.SINGLE_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.SINGLE_PRICE_LOCAL).setValue(Math.floor(this.getField(Hotel.SINGLE_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.DOUBLE_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.DOUBLE_PRICE_LOCAL).setValue(Math.floor(this.getField(Hotel.DOUBLE_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.TRIPLE_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.TRIPLE_PRICE_LOCAL).setValue(Math.floor(this.getField(Hotel.TRIPLE_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.QUAD_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.QUAD_PRICE_LOCAL).setValue(Math.floor(this.getField(Hotel.QUAD_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.ROOM_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.ROOM_PRICE_LOCAL).setValue(Math.floor(this.getField(Hotel.ROOM_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
             if ((!bMarkupOnlyIfNoPrice) || (this.getField(Hotel.MEAL_PRICE_LOCAL).getValue() == 0))
                 this.getField(Hotel.MEAL_PRICE_LOCAL).setValue(Math.floor(this.getField(Hotel.MEAL_COST_LOCAL).getValue() * (1 + dMarkup) * 100 + 0.5) / 100);
         }
     }
 
 }
