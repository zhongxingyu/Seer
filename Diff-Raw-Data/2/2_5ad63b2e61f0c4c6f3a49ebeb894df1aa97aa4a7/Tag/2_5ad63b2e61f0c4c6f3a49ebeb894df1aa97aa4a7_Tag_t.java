 package com.martindengler.proj.FIXSimple.spec;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.EnumSet;
 
 
 public enum Tag {
     // Header tags
         BEGINSTRING(8),
         BODYLENGTH(9),
         MSGTYPE(35, "Always unencrypted, must be third field in message"),
         APPLVERID(1128, "Indicates application version using a service pack identifier. The ApplVerID applies to a specific message occurrence."),
         APPLEXTID(1156, "See FIXT 1.1 spec"),
         CSTMAPPLVERID(1129, "Used to support bilaterally agreed custom functionality"),
         SENDERCOMPID(49, "Always unencrypted"),
         TARGETCOMPID(56, "Always unencrypted"),
         ONBEHALFOFCOMPID(115, "Trading partner company ID used when sending messages via a third party (Can be embedded within encrypted data section.)"),
         DELIVERTOCOMPID(128, "Trading partner company ID used when sending messages via a third party (Can be embedded within encrypted data section.)"),
         SECUREDATALEN(90, "Required to identify length of encrypted section of message. (Always unencrypted)"),
         SECUREDATA(91, "Required when message body is encrypted. immediately follows SecureDataLen field."),
         MSGSEQNUM(34, "Can be embedded within encrypted data section."),
         SENDERSUBID(50, "Can be embedded within encrypted data section."),
         SENDERLOCATIONID(142, "Sender's LocationID (i.e. geographic location and/or desk) (Can be embedded within encrypted data section.)"),
         TARGETSUBID(57, "ADMIN reserved for administrative messages not intended for a specific user. (Can be embedded within encrypted data section.)"),
         TARGETLOCATIONID(143, "Trading partner LocationID (i.e. geographic location and/or desk) (Can be embedded within encrypted data section.)"),
         ONBEHALFOFSUBID(116, "Trading partner SubID used when delivering messages via a third party. (Can be embedded within encrypted data section.)"),
         ONBEHALFOFLOCATIONID(144, "Trading partner LocationID (i.e. geographic location and/or desk) used when delivering messages via a third party. (Can be embedded within encrypted data section.)"),
         DELIVERTOSUBID(129, "Trading partner SubID used when delivering messages via a third party. (Can be embedded within encrypted data section.)"),
         DELIVERTOLOCATIONID(145, "Trading partner LocationID (i.e. geographic location and/or desk) used when delivering messages via a third party. (Can be embedded within encrypted data section.)"),
         POSSDUPFLAG(43, "Always required for retransmitted messages, whether prompted by the sending system or as the result of a resend request. (Can be embedded within encrypted data section.)"),
         POSSRESEND(97, "Required when message may be duplicate of another message sent under a different sequence number. (Can be embedded within encrypted data section.)"),
         SENDINGTIME(52, "Can be embedded within encrypted data section."),
         ORIGSENDINGTIME(122, "Required for message resent as a result of a ResendRequest. If data is not available set to same value as SendingTime (Can be embedded within encrypted data section.)"),
         XMLDATALEN(212, "Required when specifying XmlData to identify the length of a XmlData message block. (Can be embedded within encrypted data section.)"),
         XMLDATA(213, "Can contain a XML formatted message block (e.g. FIXML). Always immediately follows XmlDataLen field. (Can be embedded within encrypted data section.) See Volume 1: FIXML Support"),
         MESSAGEENCODING(347, "Type of message encoding (non-ASCII characters) used in a message's \"Encoded\" fields. Required if any \"Encoding\" fields are used."),
         LASTMSGSEQNUMPROCESSED(369, "The last MsgSeqNum value received by the FIX engine and processed by downstream application, such as trading system or order routing system. Can be specified on every message sent. Useful for detecting a backlog with a counterparty."),
 
        // Trailer tags
         SIGNATURELENGTH(93, "Required when trailer contains signature. Note: Not to be included within SecureData field"),
         SIGNATURE(89, "Note: Not to be included within SecureData field"),
         CHECKSUM(10, "Always unencrypted, always last field in message"),
 
         //TODO: HopGroup component block tags
 
         ;
 
     private static final Map<Integer, Tag> lookup
         = new HashMap<Integer, Tag>();
 
     static {
         for(Tag t : EnumSet.allOf(Tag.class))
             lookup.put(t.getCode(), t);
     }
 
     private Integer code;
     private String note;
 
     public static Tag fromCode(Integer code) {
         return lookup.get(code);
     }
 
     private Tag() {
         this(-1, "unknown tag note");
     }
 
     private Tag(Integer code) {
         this(code, "unknown tag note");
     }
 
     private Tag(Integer code, String note) {
         this.code = code;
         this.note = note;
     }
 
     public Integer getCode() {
         return this.code;
     }
 
     public String toString() {
         return this.code.toString();
     }
 
     public String toString(Boolean verbose) {
         if (verbose)
             return this.toString() + "_" + this.name();
         else
             return this.toString();
     }
 
 }
