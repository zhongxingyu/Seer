 /*****************************************************************************
  *                The Virtual Light Company Copyright (c) 1999
  *                               Java Source
  *
  * This code is licensed under the GNU Library GPL. Please read license.txt
  * for the full details. A copy of the LGPL may be found at
  *
  * http://www.gnu.org/copyleft/lgpl.html
  *
  * Project:    URI Class libs
  *
  * Version History
  * Date        TR/IWOR  Version  Programmer
  * ----------  -------  -------  ------------------------------------------
  *
  ****************************************************************************/
 
 package org.ietf.uri.event;
 
 import org.ietf.uri.ResourceConnection;
 
 /**
  * An event used to inform the client of the progress of a particular download.
  * <P>
  *
  * The event starts by issuing a <CODE>DOWNLOAD_START</CODE>. This indicates
  * that the stream has been established and that there is content to read.
  * Depending on the stream and length, an <CODE>UPDATE</CODE> event is sent
  * that indicates the number of bytes read so far and any message that may be
  * of relevance. Once the reading and conversion is complete, a
  * <CODE>DOWNLOAD_END</CODE> event is issued.
  * <P>
  *
  * It is possible, that for some connections that only start and end events
  * are generated. If the event does not need the value, it should be set to
  * a negative value.
  * <P>
  *
  * If some error has occurred, a <CODE>DOWNLOAD_ERROR</CODE> event is
  * generated. The value contains some error code (either those provided here
  * of connection specific) and a message string. Errors that occur through
  * this event should be considered terminal to the download progress. That is,
  * the <CODE>getContent()</CODE> method of the
  * {@link org.ietf.uri.ResourceConnection} class may return <CODE>null</CODE>
  * of a value with incomplete results. That is dependent on the underlying
  * content handlers.
  * <P>
  *
  * Events are also available for the setup part of establishing connections.
  * These events may or may not be generated depending on the underlying
  * protocol. For example, the connection established event is pointless when
  * reading from a local file, but perfect for a HTTP connection.
  * <P>
  *
  * For details on URIs see the IETF working group:
  * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
  * <P>
  *
  * This softare is released under the
  * <A HREF="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</A>
  * <P>
  *
  * DISCLAIMER:<BR>
  * This software is the under development, incomplete, and is
  * known to contain bugs. This software is made available for
  * review purposes only. Do not rely on this software for
  * production-quality applications or for mission-critical
  * applications.
  * <P>
  *
  * Portions of the APIs for some new features have not
  * been finalized and APIs may change. Some features are
  * not fully implemented in this release. Use at your own risk.
  * <P>
  *
  * @author  Justin Couch
  * @version 0.7 (27 August 1999)
  */
 public class ProgressEvent
 {
   // Event types
   /**
    * The connection has been established to the resource, but information
    * download has not yet commenced - ie, it is looking at fetching the
    * headers and other useful stuff.
    */
   public static final int CONNECTION_ESTABLISHED = 1;
 
   /** The header information is being downloaded and processed. The real
    * data has not got there yet.
    */
   public static final int HANDSHAKE_IN_PROGRESS = 2;
 
   /** The download has started */
   public static final int DOWNLOAD_START = 3;
 
   /** The download has finished */
   public static final int DOWNLOAD_END = 4;
 
   /** An update on the download progress */
   public static final int DOWNLOAD_UPDATE = 5;
 
   /** An error has occurred during the download */
   public static final int DOWNLOAD_ERROR = 6;
 
   // Error types
 
   /**
    * Connection terminated prematurely. Either the server closed the
    * connection or the network died. If used when referencing a file
    * that means the file didn't contain all the data it was supposed to.
    */
   public static final int CONNECTION_TERMINATED = 1;
 
   /**
    * The data is corrupted or not of a format that the content handler knows
    * about.
    */
   public static final int DATA_CORRUPTED = 2;
 
   /** No handlers can be found for the data type specified by the connection.*/
   public static final int NO_HANDLER = 3;
 
   // Variables etc
 
   /** The connection that was the source of this event */
   protected ResourceConnection source;
 
   /**
    * The value of the current event. If this is an update event, this is the
    * number of bytes that have been downloaded from the source. If it is an
    * error event, then this is the error value
    */
   protected int value;
 
   /** A message to go with the event. May be <CODE>null</CODE> if not set */
   protected String msg = null;
 
   /** The event type */
   protected int type;
 
   /**
    * Create a new progress event that is used to inform the listeners of an
    * update by the data source.
    *
    * @param src The resource connection that is generating the events
    * @param type The type of event to generate
    * @param msg The message to send with the event. May be <CODE>null</CODE>
    * @param val The value of the event.
    */
   public ProgressEvent(ResourceConnection src, int type, String msg, int val)
   {
     this.source = src;
     this.msg = msg;
    this.value = val;
     this.type = type;
   }
 
   /**
    * Get the source (resource connection, not the content handler) of this
    * event
    *
    * @return The ResourceConnection source
    */
   public ResourceConnection getSource()
   {
     return source;
   }
 
   /**
    * Fetch the value associated with this event.
    *
    * @return The value set for this event
    */
   public int getValue()
   {
     return value;
   }
 
   /**
    * Fetch the message associated with this event. There may not have been a
    * message set, so it may return <CODE>null</CODE>.
    *
    * @return The message string
    */
   public String getMessage()
   {
     return msg;
   }
 
   /**
    * Get the type of event that was generated
    *
    * @return The type ID of the event
    */
   public int getType()
   {
     return type;
   }
 }
