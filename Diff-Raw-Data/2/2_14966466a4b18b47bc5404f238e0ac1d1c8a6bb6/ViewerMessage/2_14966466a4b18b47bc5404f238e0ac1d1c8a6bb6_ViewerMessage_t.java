 package de.ptb.epics.eve.viewer.messages;
 
 import gov.aps.jca.dbr.TimeStamp;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import de.ptb.epics.eve.ecp1.client.model.Error;
 import de.ptb.epics.eve.viewer.MessageSource;
 
 /**
  * <code>ViewerMessage</code> represents a message displayed in the Messages 
  * View of the Viewer.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class ViewerMessage {
 	
 		// the time of the message
 		private final Calendar messageDateTime;
 		// another representation of the time
 		private final TimeStamp timestamp;
 		// the source of the message
 		private final MessageSource messageSource;
 		// the type of the message
 		private final MessageTypes messageType;
 		// the contents of the message
 		private final String message;
 		
 		/**
 		 * Constructor 1
 		 * 
 		 * @param messageSource
 		 * @param messageType
 		 * @param message
 		 */
 		public ViewerMessage( final MessageSource messageSource, final MessageTypes messageType, final String message ) {
 			this.messageDateTime = new GregorianCalendar();
 			this.timestamp = new TimeStamp();
 			this.messageSource = messageSource;
 			this.messageType = messageType;
 			this.message = message;
 		}
 		
 		/**
 		 * Constructor 2
 		 * 
 		 * @param messageType
 		 * @param message
 		 */
 		public ViewerMessage( final MessageTypes messageType, final String message ) {
 			this.messageDateTime = new GregorianCalendar();
 			this.timestamp = new TimeStamp();
 			this.messageSource = MessageSource.VIEWER;
 			this.messageType = messageType;
 			this.message = message;
 		}
 
 		/**
 		 * Constructor 3
 		 * 
 		 * @param messageDateTime
 		 * @param messageSource
 		 * @param messageType
 		 * @param message
 		 */
 		public ViewerMessage( final Calendar messageDateTime, final MessageSource messageSource, final MessageTypes messageType, final String message ) {
 			this.messageDateTime = messageDateTime;
 			// some dirty trick (subtract milliseconds 01/01/2031-01/01/2011)
 			this.timestamp = new TimeStamp(messageDateTime.getTimeInMillis()-631152000000.0);
 			this.messageSource = messageSource;
 			this.messageType = messageType;
 			this.message = message;
 		}
 		
 		/**
 		 * Constructor 4
 		 * 
 		 * @param error
 		 */
 		public ViewerMessage(final Error error) {
 			
 			this.messageDateTime = new GregorianCalendar( 1990, 0, 1, 0, 0 );
 			this.messageDateTime.add( Calendar.SECOND, error.getGerenalTimeStamp() );
 			this.messageDateTime.add(Calendar.MILLISECOND, error.getNanoseconds() / 1000000);
			this.timestamp = new TimeStamp(error.getGerenalTimeStamp()-631152000, error.getNanoseconds());
 			this.messageSource = MessageSource.convertFromErrorFacility( error.getErrorFacility() );
 			this.messageType = MessageTypes.convertFromErrorSeverity( error.getErrorSeverity() );
 			this.message = error.getText();
 			
 		}
 
 		/**
 		 * Returns the contents of the message.
 		 * 
 		 * @return the message contents
 		 */
 		public String getMessage() {
 			return this.message;
 		}
 
 		/**
 		 * Returns the source of the message.
 		 * 
 		 * @return the message' source
 		 */
 		public MessageSource getMessageSource() {
 			return this.messageSource;
 		}
 		
 		/**
 		 * Returns the date of the message.
 		 * 
 		 * @return the message' date
 		 */
 		public Calendar getMessageDateTime() {
 			return this.messageDateTime;
 		}
 
 		/**
 		 * Returns the type of the message.
 		 * 
 		 * @return the message' type
 		 */
 		public MessageTypes getMessageType() {
 			return this.messageType;
 		}		
 		
 		/**
 		 * Returns the {@link gov.aps.jca.dbr.TimeStamp} of the message.
 		 * 
 		 * @return the time stamp
 		 */
 		public TimeStamp getTimeStamp() {
 			return this.timestamp;
 		}
 }
