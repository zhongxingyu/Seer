 /*
  * Guest.java
  *
  * Created on July 24, 2006, 6:41 PM
  */
 
 package com.zwarg.guestlist.data;
 
 import java.beans.*;
 import java.io.Serializable;
 
 /**
  * @author davidz
  */
 public class Guest extends Object implements Serializable, Comparable {
   /*
          Column         |          Type          |       Modifiers
 ------------------------+------------------------+------------------------
  GuestID                | smallint               | not null
  Title                  | character varying(5)   | not null
  ProperName             | character varying(100) | not null
  SigOtherTitle          | character varying(5)   |
  SigOtherProperName     | character varying(100) |
  InnerEnvelopeName      | character varying(100) | not null
  Address1               | character varying(100) | not null
  Address2               | character varying(100) |
  City                   | character varying(100) | not null
  State                  | character varying(25)  | not null
  Zip                    | integer                | not null
  ZipPlusFour            | smallint               |
  SaveDateSent           | boolean                | not null default false
  InviteSent             | boolean                | not null default false
  Attending              | boolean                | not null default false
  Replied                | boolean                | not null default false
  ThankYouSent           | boolean                | not null default false
  GiftSource             | character varying(100) |
  GiftItem               | character varying(100) |
  UnderEighteen1         | character varying(100) |
  UnderEighteen2         | character varying(100) |
  UnderEighteen3         | character varying(100) |
  UnderEighteen4         | character varying(100) |
  UnderEighteenAttending | smallint               | not null default 0
  Lodging                | character varying(50)  |
  TravelType             | character varying(50)  |
  TravelDestination      | character varying(50)  |
  ArrivalDate            | date                   |
  ArrivalTime            | time without time zone |
  DepartureDate          | date                   |
  DepartureTime          | time without time zone |
  Telephone              | character varying(25)  |
  Email                  | character varying(50)  |
  HostID                 | smallint               | not null
 */
   public static final String PROP_ID = "id";
   public static final String PROP_TITLE = "title";
   public static final String PROP_PROPERNAME = "propername";
   public static final String PROP_INNER_NAME = "innerenvelopename";
   public static final String PROP_SIGOTHERTITLE = "significantothertitle";
   public static final String PROP_SIGOTHERPROPERNAME = "significantotherpropername";
   public static final String PROP_SIGOTHERINNER_NAME = "significantotherinnerenvelopename";
   public static final String PROP_ADDRESS1 = "address1";
   public static final String PROP_ADDRESS2 = "address2";
   public static final String PROP_CITY = "city";
   public static final String PROP_STATE = "state";
   public static final String PROP_ZIP = "zip";
   public static final String PROP_ZIP4 = "zip4";
   public static final String PROP_SAVETHEDATESENT = "savethedatesent";
   public static final String PROP_INVITATIONSENT = "invitesent";
   public static final String PROP_ATTENDING = "attending";
   public static final String PROP_REPLIED = "replied";
   public static final String PROP_THANK_YOU = "thank_you";
   public static final String PROP_GIFT_SOURCE = "gift_source";
   public static final String PROP_GIFT_DESCRIPTION = "gift_description";
   public static final String PROP_UNDER_18_1 = "under_18_1";
   public static final String PROP_UNDER_18_2 = "under_18_2";
   public static final String PROP_UNDER_18_3 = "under_18_3";
   public static final String PROP_UNDER_18_4 = "under_18_4";
   public static final String PROP_UNDER_18_ATTENDING = "under_18_attending";
   public static final String PROP_LODGING = "lodging";
   public static final String PROP_TRAVEL_TYPE = "travel_type";
   public static final String PROP_TRAVEL_DEST = "travel_dest";
   public static final String PROP_ARRIVAL_DATE = "arrival_date";
   public static final String PROP_ARRIVAL_TIME = "arrival_time";
   public static final String PROP_DEPARTURE_DATE = "departure_date";
   public static final String PROP_DEPARTURE_TIME = "departure_time";
   public static final String PROP_INVITED_BY = "invitedby";
   public static final String PROP_TELEPHONE = "telephone";
   public static final String PROP_EMAIL = "email";
 
   private int intId;
   private String strTitle;
   private String strProperName;
   private String strInnerEnvelopeName;
   private String strSigOtherTitle;
   private String strSigOtherProperName;
   private String strSigOtherInnerEnvelopeName;
   private String strAddress1;
   private String strAddress2;
   private String strCity;
   private String strState;
   private int intZip;
   private int intZipFour;
   private boolean bolSaveDateSent;
   private boolean bolInviteSent;
   private boolean bolAttending;
   private boolean bolReplied;
   private boolean bolThankYou;
   private String strGiftSource;
   private String strGiftItem;
   private String strUnderEighteen1;
   private String strUnderEighteen2;
   private String strUnderEighteen3;
   private String strUnderEighteen4;
   private int intEighteenAttending;
   private String strLodging;
   private String strTravelType;
   private String strTravelDest;
   private java.sql.Date dteArrival;
   private java.sql.Time tmeArrival;
   private java.sql.Date dteDeparture;
   private java.sql.Time tmeDeparture;
   private int intInvitedBy;
   private String strTelephone;
   private String strEmail;
     
   private PropertyChangeSupport propertySupport;
   
   public Guest() {
     propertySupport = new PropertyChangeSupport(this);
   }
   
   public int getId()
   {
     return intId;
   }
   
   public void setId( int value )
   {
     int oldValue = intId;
     intId = value;
     propertySupport.firePropertyChange( PROP_ID, oldValue, intId );
   }
   
   public String getTitle()
   {
     return strTitle;
   }
   
   public void setTitle( String value )
   {
     String oldValue = strTitle;
     strTitle = value;
     propertySupport.firePropertyChange( PROP_TITLE, oldValue, strTitle );
   }
 
   public String getProperName()
   {
     return strProperName;
   }
   
   public void setProperName( String value )
   {
     String oldValue = strProperName;
     strProperName = value;
     propertySupport.firePropertyChange( PROP_PROPERNAME, oldValue, strProperName );
   }
   
   public String getSigOtherTitle()
   {
     return strSigOtherTitle;
   }
   
   public void setSigOtherTitle( String value )
   {
     String oldValue = strSigOtherTitle;
     strSigOtherTitle = value;
     propertySupport.firePropertyChange( PROP_SIGOTHERTITLE, oldValue, strSigOtherTitle );
   }
   
   public String getSigOtherProperName()
   {
     return strSigOtherProperName;
   }
   
   public void setSigOtherProperName( String value )
   {
     String oldValue = strSigOtherProperName;
     strSigOtherProperName = value;
     propertySupport.firePropertyChange( PROP_SIGOTHERPROPERNAME, oldValue, strSigOtherProperName );
   }
 
   public String getInnerEnvelopeName()
   {
     return strInnerEnvelopeName;
   }
   
   public void setInnerEnvelopeName( String value )
   {
     String oldValue = strInnerEnvelopeName;
     strInnerEnvelopeName = value;
     propertySupport.firePropertyChange( PROP_INNER_NAME, oldValue, strInnerEnvelopeName );
   }
 
   public String getSigOtherInnerEnvelopeName()
   {
     return strSigOtherInnerEnvelopeName;
   }
   
   public void setSigOtherInnerEnvelopeName( String value )
   {
     String oldValue = strSigOtherInnerEnvelopeName;
     strSigOtherInnerEnvelopeName = value;
     propertySupport.firePropertyChange( PROP_SIGOTHERINNER_NAME, oldValue, strSigOtherInnerEnvelopeName );
   }
   
   
   public String getAddress1()
   {
     return strAddress1;
   }
   
   public void setAddress1( String value )
   {
     String oldValue = strAddress1;
     strAddress1 = value;
     propertySupport.firePropertyChange( PROP_ADDRESS1, oldValue, strAddress1 );
   }
 
   public String getAddress2()
   {
     return strAddress2;
   }
   
   public void setAddress2( String value )
   {
     String oldValue = strAddress2;
     strAddress2 = value;
     propertySupport.firePropertyChange( PROP_ADDRESS2, oldValue, strAddress2 );
   }
   
   public String getCity()
   {
     return strCity;
   }
   
   public void setCity( String value )
   {
     String oldValue = strCity;
     strCity = value;
     propertySupport.firePropertyChange( PROP_CITY, oldValue, strCity );
   }
 
   public String getState()
   {
     return strState;
   }
   
   public void setState( String value )
   {
     String oldValue = strState;
     strState = value;
     propertySupport.firePropertyChange( PROP_STATE, oldValue, strState );
   }
 
   public int getZip()
   {
     return intZip;
   }
   
   public void setZip( int value )
   {
     int oldValue = intZip;
     intZip = value;
     propertySupport.firePropertyChange( PROP_ZIP, oldValue, intZip );
   }
 
   public int getZipFour()
   {
     return intZipFour;
   }
   
   public void setZipFour( int value )
   {
     int oldValue = intZipFour;
     intZipFour = value;
     propertySupport.firePropertyChange( PROP_ZIP4, oldValue, intZipFour );
   }
   
   public boolean getSaveTheDateSent()
   {
     return bolSaveDateSent;
   }
   
   public void setSaveTheDateSent( boolean value )
   {
     boolean oldValue = bolSaveDateSent;
     bolSaveDateSent = value;
     propertySupport.firePropertyChange( PROP_SAVETHEDATESENT, oldValue, bolSaveDateSent );
   }
   
   public boolean getInvitationSent()
   {
     return bolInviteSent;
   }
   
   public void setInvitationSent( boolean value )
   {
     boolean oldValue = bolInviteSent;
     bolInviteSent = value;
     propertySupport.firePropertyChange( PROP_INVITATIONSENT, oldValue, bolInviteSent );
   }
   
   public boolean getHasReplied()
   {
     return bolReplied;
   }
   
   public void setHasReplied( boolean value )
   {
     boolean oldValue = bolReplied;
     bolReplied = value;
     propertySupport.firePropertyChange( PROP_REPLIED, oldValue, bolReplied );
   }
   
   public boolean getReply()
   {
     return bolAttending;
   }
   
   public void setReply( boolean value )
   {
     boolean oldValue = bolAttending;
     bolAttending = value;
     propertySupport.firePropertyChange( PROP_ATTENDING, oldValue, bolAttending );
   }
 
   public boolean getThankYou()
   {
     return bolThankYou;
   }
   
   public void setThankYou( boolean value )
   {
     boolean oldValue = bolThankYou;
     bolThankYou = value;
     propertySupport.firePropertyChange( PROP_THANK_YOU, oldValue, bolThankYou );
   }
   
   public String getUnderEighteen1()
   {
     return strUnderEighteen1;
   }
   
   public void setUnderEighteen1( String value )
   {
     String oldValue = strUnderEighteen1;
     strUnderEighteen1 = value;
     propertySupport.firePropertyChange( PROP_UNDER_18_1, oldValue, strUnderEighteen1 );
   }
   
   public String getUnderEighteen2()
   {
     return strUnderEighteen2;
   }
   
   public void setUnderEighteen2( String value )
   {
     String oldValue = strUnderEighteen2;
     strUnderEighteen2 = value;
     propertySupport.firePropertyChange( PROP_UNDER_18_2, oldValue, strUnderEighteen2 );
   }
   
   public String getUnderEighteen3()
   {
     return strUnderEighteen3;
   }
   
   public void setUnderEighteen3( String value )
   {
     String oldValue = strUnderEighteen3;
     strUnderEighteen3 = value;
     propertySupport.firePropertyChange( PROP_UNDER_18_3, oldValue, strUnderEighteen3 );
   }
   
   public String getUnderEighteen4()
   {
     return strUnderEighteen4;
   }
   
   public void setUnderEighteen4( String value )
   {
     String oldValue = strUnderEighteen4;
     strUnderEighteen4 = value;
     propertySupport.firePropertyChange( PROP_UNDER_18_4, oldValue, strUnderEighteen4 );
   }
   
   public int getUnderEighteenAttending()
   {
     return intEighteenAttending;
   }
   
   public void setUnderEighteenAttending( int value )
   {
     int oldValue = intEighteenAttending;
     intEighteenAttending = value;
     propertySupport.firePropertyChange( PROP_UNDER_18_ATTENDING, oldValue, intEighteenAttending );
   }
   
   public String getLodging()
   {
     return strLodging;
   }
   
   public void setLodging( String value )
   {
     String oldValue = strLodging;
     strLodging = value;
     propertySupport.firePropertyChange( PROP_LODGING, oldValue, strLodging );
   }
   
   public String getTravelType()
   {
     return strTravelType;
   }
   
   public void setTravelType( String value )
   {
     String oldValue = strTravelType;
     strTravelType = value;
     propertySupport.firePropertyChange( PROP_TRAVEL_TYPE, oldValue, strTravelType );
   }
   
   public String getTravelDestination()
   {
     return strTravelDest;
   }
   
   public void setTravelDestination( String value )
   {
     String oldValue = strTravelDest;
     strTravelDest = value;
     propertySupport.firePropertyChange( PROP_TRAVEL_DEST, oldValue, strTravelDest );
   }
   
   public java.sql.Date getArrivalDate()
   {
     return dteArrival;
   }
   
   public void setArrivalDate( java.sql.Date value )
   {
     java.sql.Date oldValue = dteArrival;
     dteArrival = value;
     propertySupport.firePropertyChange( PROP_ARRIVAL_DATE, oldValue, dteArrival );
   }
   
   public java.sql.Time getArrivalTime()
   {
     return tmeArrival;
   }
   
   public void setArrivalTime( java.sql.Time value )
   {
     java.sql.Time oldValue = tmeArrival;
     tmeArrival = value;
     propertySupport.firePropertyChange( PROP_ARRIVAL_TIME, oldValue, tmeArrival);
   }
 
   public java.sql.Date getDepartureDate()
   {
     return dteDeparture;
   }
   
   public void setDepartureDate( java.sql.Date value )
   {
     java.sql.Date oldValue = dteDeparture;
     dteDeparture = value;
     propertySupport.firePropertyChange( PROP_DEPARTURE_DATE, oldValue, dteDeparture );
   }
   
   public java.sql.Time getDepartureTime()
   {
     return tmeDeparture;
   }
   
   public void setDepartureTime( java.sql.Time value )
   {
     java.sql.Time oldValue = tmeDeparture;
     tmeDeparture = value;
     propertySupport.firePropertyChange( PROP_DEPARTURE_TIME, oldValue, tmeDeparture);
   }
 
   public String getGiftSource()
   {
     return strGiftSource;
   }
   
   public void setGiftSource( String value )
   {
     String oldValue = strGiftSource;
     strGiftSource = value;
     propertySupport.firePropertyChange( PROP_GIFT_SOURCE, oldValue, strGiftSource );
   }
   
   public String getGiftItem()
   {
     return strGiftItem;
   }
   
   public void setGiftItem( String value )
   {
     String oldValue = strGiftItem;
     strGiftItem = value;
     propertySupport.firePropertyChange( PROP_GIFT_DESCRIPTION, oldValue, strGiftItem );
   }
   
   public int getInvitedBy()
   {
     return intInvitedBy;
   }
   
   public void setInvitedBy( int value )
   {
     int oldValue = intInvitedBy;
     intInvitedBy = value;
     propertySupport.firePropertyChange( PROP_INVITED_BY, oldValue, intInvitedBy );
   }
   
   public String getTelephone()
   {
     return strTelephone;
   }
   
   public void setTelephone( String value )
   {
     String oldValue = strTelephone;
     strTelephone = value;
     propertySupport.firePropertyChange( PROP_TELEPHONE, oldValue, strTelephone );
   }
   
   public String getEmail()
   {
     return strEmail;
   }
   
   public void setEmail( String value )
   {
     String oldValue = strEmail;
     strEmail = value;
     propertySupport.firePropertyChange( PROP_EMAIL, oldValue, strEmail );
   }
   
   public void addPropertyChangeListener(PropertyChangeListener listener) {
     propertySupport.addPropertyChangeListener(listener);
   }
   
   public void removePropertyChangeListener(PropertyChangeListener listener) {
     propertySupport.removePropertyChangeListener(listener);
   }
   
   public static Guest validate( javax.servlet.http.HttpServletRequest request )
   {
     String strParam = request.getParameter("hostid");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     int intParam;
     boolean bolParam;
     java.util.Date dateParam;
     java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance( java.text.DateFormat.SHORT );
     java.text.DateFormat timeFormat = java.text.DateFormat.getTimeInstance( java.text.DateFormat.SHORT );
     
     try
     {
       intParam = Integer.parseInt( strParam );
     }
     catch (NumberFormatException nfe)
     {
       return null;
     }
     
     Guest theGuest = new Guest();
     theGuest.setInvitedBy( intParam );
     
     strParam = request.getParameter("id");
     if ( strParam != null && strParam.length() > 0 )
     {
       try
       {
         intParam = Integer.parseInt( strParam );
         theGuest.setId( intParam );
       }
       catch (NumberFormatException nfe)
       {
         return null;
       }
     }
 
     strParam = request.getParameter("title");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     theGuest.setTitle( strParam );
     
     strParam = request.getParameter("propername");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     theGuest.setProperName( strParam );
     
    /*
     strParam = request.getParameter( "innername" );
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     theGuest.setInnerEnvelopeName( strParam );
    */
     
     strParam = request.getParameter("sigothertitle");
     if ( strParam != null )
       theGuest.setSigOtherTitle( strParam );
     
     strParam = request.getParameter( "sigotherpropername" );
     if ( strParam != null )
       theGuest.setSigOtherProperName( strParam );
     
    /*
     strParam = request.getParameter( "sigotherinnername" );
     if ( strParam != null )
       theGuest.setSigOtherInnerEnvelopeName( strParam);
    */
     
     strParam = request.getParameter("address1");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     theGuest.setAddress1( strParam );
     
     strParam = request.getParameter("address2");
     if ( strParam != null )
     {
       theGuest.setAddress2( strParam );
     }
     
     strParam = request.getParameter("city");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     theGuest.setCity( strParam );
     
     strParam = request.getParameter("state");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     theGuest.setState( strParam );
     
     strParam = request.getParameter("zip");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     try {
       intParam = Integer.parseInt( strParam );
       theGuest.setZip( intParam );
     }
     catch (NumberFormatException nfe) {
       return null;
     }
     
     strParam = request.getParameter("zip4");
     if ( strParam != null && strParam.length() > 0 )
     {
       try {
         intParam = Integer.parseInt( strParam );
         theGuest.setZipFour( intParam );
       }
       catch (NumberFormatException nfe) {
         theGuest.setZipFour( 0 );
       }
     }
     
     strParam = request.getParameter("minor1");
     if ( strParam != null )
     {
       theGuest.setUnderEighteen1( strParam );
     }
        
     strParam = request.getParameter("minor2");
     if ( strParam != null )
     {
       theGuest.setUnderEighteen2( strParam );
     }
     
     strParam = request.getParameter("minor3");
     if ( strParam != null )
     {
       theGuest.setUnderEighteen3( strParam );
     }
      
     strParam = request.getParameter("minor4");
     if ( strParam != null )
     {
       theGuest.setUnderEighteen4( strParam );
     }
     
     strParam = request.getParameter("minorAttend");
     if ( strParam == null || strParam.length() == 0 )
       return null;
     
     try {
       intParam = Integer.parseInt( strParam );
       theGuest.setUnderEighteenAttending( intParam );
     }
     catch (NumberFormatException nfe) {
       return null;
     }
     
     strParam = request.getParameter("phone");
     if ( strParam != null )
     {
       theGuest.setTelephone( strParam );
     }
     
     strParam = request.getParameter("email");
     if ( strParam != null )
     {
       theGuest.setEmail( strParam );
     }
     
     strParam = request.getParameter("saveTheDate");
     if ( strParam != null && strParam.length() > 0 )
     {
       bolParam = new Boolean( strParam ).booleanValue();
       theGuest.setSaveTheDateSent( bolParam );
     }
     else
     {
       theGuest.setSaveTheDateSent( false );
     }
     
     strParam = request.getParameter("saveTheDate");
     if ( strParam != null && strParam.length() > 0 )
     {
       bolParam = new Boolean( strParam ).booleanValue();
       theGuest.setSaveTheDateSent( bolParam );
     }
     else
     {
       theGuest.setSaveTheDateSent( false );
     }
     
     strParam = request.getParameter("inviteSent");
     if ( strParam != null && strParam.length() > 0 )
     {
       bolParam = new Boolean( strParam ).booleanValue();
       theGuest.setInvitationSent( bolParam );
     }
     else
     {
       theGuest.setInvitationSent( false );
     }
     
     strParam = request.getParameter("hasreplied");
     if ( strParam != null && strParam.length() > 0 )
     {
       bolParam = new Boolean( strParam ).booleanValue();
       theGuest.setHasReplied( bolParam );
     }
     else
     {
       theGuest.setHasReplied( false );
     }
     
     strParam = request.getParameter("reply");
     if ( strParam != null && strParam.length() > 0 )
     {
       bolParam = new Boolean( strParam ).booleanValue();
       theGuest.setReply( bolParam );
     }
     else
     {
       theGuest.setReply( false );
     }
     
     strParam = request.getParameter("thankYou");
     if ( strParam != null && strParam.length() > 0 )
     {
       bolParam = new Boolean( strParam ).booleanValue();
       theGuest.setThankYou( bolParam );
     }
     else
     {
       theGuest.setThankYou( false );
     }
     
     strParam = request.getParameter("lodging");
     if ( strParam != null )
     {
       theGuest.setLodging( strParam );
     }
     
     strParam = request.getParameter( "travelType" );
     if ( strParam != null )
     {
       theGuest.setTravelType( strParam );
     }
     
     strParam = request.getParameter( "travelDest" );
     if ( strParam != null )
     {
       theGuest.setTravelDestination( strParam );
     }
     
     strParam = request.getParameter( "arrivalDate" );
     if ( strParam != null )
     {
       try
       {
         dateParam = dateFormat.parse( strParam );
         //Date.valueOf( strParam );
         theGuest.setArrivalDate( new java.sql.Date( dateParam.getTime() ) );
       }
       catch (java.text.ParseException pe){
         theGuest.setArrivalDate( null );
       }
     }
     
     strParam = request.getParameter( "arrivalTime" );
     if ( strParam != null )
     {
       try
       {
         dateParam = timeFormat.parse( strParam );
         //Time.valueOf( strParam );
         theGuest.setArrivalTime( new java.sql.Time( dateParam.getTime() ) );
       }
       catch (java.text.ParseException pe) {
         theGuest.setArrivalTime( null );
       }
     }
     
     strParam = request.getParameter("departureDate");
     if ( strParam != null )
     {
       try
       {
         dateParam = dateFormat.parse( strParam );
         theGuest.setDepartureDate( new java.sql.Date( dateParam.getTime() ) );
       }
       catch (java.text.ParseException pe){
         theGuest.setDepartureDate( null );
       }
     }
     
     strParam = request.getParameter( "departureTime" );
     if ( strParam != null )
     {
       try
       {
         dateParam = timeFormat.parse( strParam );
         //Time.valueOf( strParam );
         theGuest.setDepartureTime( new java.sql.Time( dateParam.getTime() ) );
       }
       catch (java.text.ParseException pe) {
         theGuest.setDepartureTime( null );
       }
     }
 
     strParam = request.getParameter("giftSource");
     if ( strParam != null )
     {
       theGuest.setGiftSource( strParam );
     }
     
     strParam = request.getParameter("giftItem");
     if ( strParam != null )
     {
       theGuest.setGiftItem( strParam );
     }
 
     return theGuest;
   }
   
   /**
    * Get the name of a guest in an easy to read format.
    *
    * If a guest is a married couple:
    * Zwarg, Peter and Mary Anne
    *
    * If a guest has different names:
    * Galprin, William and Christina Zwarg
    *
    * If a guest is NOT married:
    * Ficker, Jamie and Joseph Zwick
    */
   public String getFormattedName()
   {
     String formatted = "";
     
     int lastNameStart = strProperName.lastIndexOf( " " );
     
     String lastName = strProperName.substring( lastNameStart + 1 );
     String firstNames = strProperName.substring( 0, lastNameStart );
     
     formatted = lastName + ", " + firstNames;
     
     if ( countIndividuals() > 1 && strSigOtherProperName.length() > 0 )
     {
       int soLastNameStart = strSigOtherProperName.lastIndexOf( " " );
       
      if ( soLastNameStart == -1 )
        return formatted;
      
       String soLastName = strSigOtherProperName.substring( soLastNameStart + 1 );
       String soFirstNames = strSigOtherProperName.substring( 0, soLastNameStart );
       
       formatted += " & " + soFirstNames;
       
       if ( !lastName.equals( soLastName ) )
       {
         formatted += " " + soLastName;
       }
     }
     
     return formatted;
   }
   
   public int countIndividuals()
   {
     int folks = 1;
     
     if ( getSigOtherProperName() != null && getSigOtherProperName().length() > 0 )
       folks++;
     
     folks += getUnderEighteenAttending();
     
     return folks;
   }
   
   public static int summarizeGuests( java.util.Vector guests )
   {
     int total = 0;
     
     if ( guests != null )
     {
       for ( int index = 0; index < guests.size(); index++ )
       {
         Object gObj = guests.get( index );
 
         if ( gObj instanceof Guest )
         {
           Guest g = (Guest)gObj;
 
           total += g.countIndividuals();
         }
       }
     }
     
     return total;
   }
 
   /**
    * Compare this guest to another guest.
    */
   public int compareTo(Object o) 
   {
     if ( o instanceof Guest )
     {
       Guest g = (Guest)o;
       
       String yourFormatted = g.getFormattedName();
       
       return getFormattedName().compareTo( yourFormatted );
     }
     
     throw new ClassCastException( "The object to compare is not a guest!" );
   }
 }
