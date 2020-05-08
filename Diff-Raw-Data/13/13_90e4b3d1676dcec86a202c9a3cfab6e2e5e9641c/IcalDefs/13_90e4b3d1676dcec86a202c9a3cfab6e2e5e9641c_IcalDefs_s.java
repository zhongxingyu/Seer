 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.cmt.calendar;
 
 /** Possibly temporary home for definitions that need to be global.
  *
  * @author douglm
  *
  */
 public class IcalDefs {
   /* Integer values associated with each type of calendar entity. Used as a
   code for db entries.
    */
 
   /** */
   public static final int entityTypeEvent = 0;
 
   /** */
   public static final int entityTypeAlarm = 1;
 
   /** */
   public static final int entityTypeTodo = 2;
 
   /** */
   public static final int entityTypeJournal = 3;
 
   /** */
   public static final int entityTypeFreeAndBusy = 4;
 
   /** */
   public static final int entityTypeVavailability = 5;
 
   /** */
   public static final int entityTypeAvailable = 6;
 
   /** */
   public static final String[] entityTypeNames = {"event",
                                                   "alarm",
                                                   "todo",
                                                   "journal",
                                                   "freeAndBusy",
                                                   "vavailability",
                                                   "available"
   };
 
   /** */
   public static final String[] entityTypeIcalNames = {"VEVENT",
                                                       "VALARM",
                                                       "VTODO",
                                                       "VJOURNAL",
                                                       "VFREEBUSY",
                                                       "VAVAILABILITY",
                                                       "AVAILABLE"
   };
 
   /** Used to identify types of components within a calendar object */
   public enum IcalComponentType {
     /** */
     none,
 
     /** */
     event,
 
     /** */
     todo,
 
     /** */
     journal,
 
     /** */
     freebusy,
 
     /** */
     vavailability,
 
     /** */
     mixed}
 
   /* ====================================================================
    *        status we can set for scheduling requests
    * ==================================================================== */
 
   /** Deferred till later e.g. needs mailing */
   public static final String requestStatusDeferred = "1.0;Deferred";
 
   /**  */
   public static final String requestStatusOK = "2.0;Success";
 
   /** Append property name */
   public static final String requestStatusInvalidProperty =
            "3.0;Invalid Property Name:";
 
   /**  */
   public static final String requestStatusUnsupportedCapability =
            "3.14;Unsupported capability";
 
   /** */
   public static final String requestStatusNoAccess =
            "4.2;No Access";
 
   /* ====================================================================
    *                      Transparency
    * ==================================================================== */
   /** Transparency is used in free/busy time calculation
    *      transp     = "TRANSP" tranparam ":" transvalue CRLF
 
      tranparam  = *(";" xparam)
 
      transvalue = "OPAQUE"      ;Blocks or opaque on busy time searches.
                 / "TRANSPARENT" ;Transparent on busy time searches.
         ;Default value is OPAQUE
    */
 
   /** */
   public final static String transparencyOpaque = "OPAQUE";
   /** */
   public final static String transparencyTransparent = "TRANSPARENT";
 
   /* ====================================================================
    *                      Alarm trigger relationship
    * ==================================================================== */
 
   /** Trigger related to start */
   public static final String alarmTriggerRelatedStart = "START";
 
   /** Trigger related to end */
   public static final String alarmTriggerRelatedEnd = "END";
 
   /* ====================================================================
    *                      Attendee partstat
    * ==================================================================== */
 
   /** none set */
   public static final int partstatNone = -2;
 
   /** x-name or iana-token */
   public static final int partstatOther = -1;
 
   /** Event, to-do, journal */
   public static final int partstatNeedsAction = 0;
   /** Event, to-do, journal */
   public static final int partstatAccepted = 1;
   /** Event, to-do, journal */
   public static final int partstatDeclined = 2;
   /** Event, to-do */
   public static final int partstatTentative = 3;
   /** Event, to-do */
   public static final int partstatDelegated = 4;
   /** to-do */
   public static final int partstatCompleted = 5;
   /** to-do */
   public static final int partstatInProcess = 6;
 
   /** Event, to-do, journal */
   public static final String partstatValNeedsAction = "NEEDS-ACTION";
   /** Event, to-do, journal */
   public static final String partstatValAccepted = "ACCEPTED";
   /** Event, to-do, journal */
   public static final String partstatValDeclined = "DECLINED";
   /** Event, to-do */
   public static final String partstatValTentative = "TENTATIVE";
   /** Event, to-do */
   public static final String partstatValDelegated = "DELEGATED";
   /** to-do */
   public static final String partstatValCompleted = "COMPLETED";
   /** to-do */
   public static final String partstatValInProcess = "IN-PROCESS";
 
   /** Defined partstat values */
   public final static String[] partstats = {
     partstatValNeedsAction,
     partstatValAccepted,
     partstatValDeclined,
     partstatValTentative,
     partstatValDelegated,
     partstatValCompleted,
     partstatValInProcess
   };
 
   /** Return an index for the partstat
    *
    *  @param  val   String partstat
    *  @return int index, <0 for undefined
    */
   public static int checkPartstat(final String val) {
     if (val == null) {
       return partstatNone;
     }
 
     for (int i = 0; i < partstats.length; i++) {
       if (partstats[i].equals(val)) {
         return i;
       }
     }
 
     return partstatOther;
   }
 
   /*    Parameter Name:  SCHEDULE-AGENT
 
    Purpose:  Indicates what agent is expected to handle scheduling for
       the corresponding Attendee.
 
    Format Definition:  This property parameter is defined by the
       following notation:
 
       scheduleagentparam = "SCHEDULE-AGENT" "="
                         ("SERVER"       ; The server handles scheduling
                        / "CLIENT"       ; The client handles scheduling
                        / "NONE"         ; No automatic scheduling
                        / x-name         ; Experimental type
                        / iana-token)    ; Other IANA registered type
                                         ; Default is SERVER
   */
 
   /** x-name or iana-token */
   public static final int scheduleAgentOther = -1;
 
   /** Server handles it - default */
   public static final int scheduleAgentServer = 0;
 
   /** Client handles attendee */
   public static final int scheduleAgentClient = 1;
 
   /** Nobody handles this one */
   public static final int scheduleAgentNone = 2;
 
   /** */
   public static final String[] scheduleAgents = {"SERVER",
     "CLIENT",
     "NONE"};
 
   /* Delivery status - set in attendee SCHEDULE-STATUS parameter
    +----------+--------------------------------------------------------+
    | Delivery | Description                                            |
    | Status   |                                                        |
    | Code     |                                                        |
    +----------+--------------------------------------------------------+
    | 1.0      | The scheduling message is pending. i.e. the server is  |
    |          | still in the process of sending the message.  The      |
    |          | status code value can be expected to change once the   |
    |          | server has completed its sending and delivery          |
    |          | attempts.                                              |
    |          |                                                        |
    | 1.1      | The scheduling message has been successfully sent.     |
    |          | However, the server does not have explicit information |
    |          | about whether the scheduling message was successfully  |
    |          | delivered to the recipient.  This state can occur with |
    |          | "store and forward" style scheduling protocols such as |
    |          | iMIP [RFC2447] (iTIP using email).                     |
    |          |                                                        |
    | 1.2      | The scheduling message has been successfully           |
    |          | delivered.                                             |
    |          |                                                        |
    | 3.7      | The scheduling message was not delivered because the   |
    |          | server did not recognize the calendar user address of  |
    |          | the recipient as being a supported URI.                |
    |          |                                                        |
    | 3.8      | The scheduling message was not delivered because       |
    |          | access privileges do not allow it.                     |
    |          |                                                        |
    | 5.1      | The scheduling message was not delivered because the   |
    |          | server could not complete delivery of the message.     |
    |          | This is likely due to a temporary failure, and the     |
    |          | originator can try to send the message again at a      |
    |          | later time.                                            |
    |          |                                                        |
    | 5.2      | The scheduling message was not delivered because the   |
    |          | server was not able to find a suitable way to deliver  |
    |          | the message.  This is likely a permanent failure, and  |
    |          | the originator should not try to send the message      |
    |          | again, at least without verifying/correcting the       |
    |          | calendar user address of the recipient.                |
    |          |                                                        |
    | 5.3      | The scheduling message was not delivered and was       |
    |          | rejected because scheduling with that recipient is not |
    |          | allowed.  This is likely a permanent failure, and the  |
    |          | originator should not try to send the message again.   |
    +----------+--------------------------------------------------------+
    */
 
   /** */
   public static final String deliveryStatusPending = "1.0";
 
   /** */
   public static final String deliveryStatusSent = "1.1";
 
   /** */
   public static final String deliveryStatusDelivered = "1.2";
 
   /** */
   public static final String deliveryStatusSuccess = "2.0";
 
   /** */
   public static final String deliveryStatusInvalidCUA = "3.7";
 
   /** */
   public static final String deliveryStatusNoAccess = "3.8";
 
   /** */
   public static final String deliveryStatusTempFailure = "5.1";
 
   /** */
   public static final String deliveryStatusFailed = "5.2";
 
   /** */
   public static final String deliveryStatusRejected = "5.3";
 
   /** For RFC5545 request status */
   public static class RequestStatus {
     private String code;
     private String description;
 
     RequestStatus(final String code,
                   final String description) {
       this.code = code;
       this.description = description;
     }
 
     /**
      * @return String
      */
     public String getCode() {
       return code;
     }
 
     /**
      * @return String or null
      */
     public String getDescription() {
       return description;
     }
   }
 
   /** */
   public static final RequestStatus requestStatusSuccess =
     new RequestStatus("2.0", "Success");
 }
