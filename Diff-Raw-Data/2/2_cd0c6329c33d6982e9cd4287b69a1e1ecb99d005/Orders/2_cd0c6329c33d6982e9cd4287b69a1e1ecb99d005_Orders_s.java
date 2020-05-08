 
 
 
 package overwatch.db;
 
 import java.util.ArrayList;
 import overwatch.gui.NameRefPair;
 import overwatch.gui.NameRefPairList;
 
 
 
 
 
 public class Orders
 {
 	
 	/**
 	 * Create a new order and an associated message.
 	 * @param subject
 	 * @param body
 	 * @param sentBy
 	 * @param sentTo
 	 * @return orderNo
 	 */
 	public static Integer create( String subject, String body, Integer sentBy, Integer sentTo )
 	{
 		Integer messageNo = Messages.create( subject, body, sentBy, sentTo );
 		
 		EnhancedPreparedStatement eps = new EnhancedPreparedStatement(
 		  	"insert into Orders " +
 		  	"values( default,   " +
 		  	"		 <<msg>>,   " +
 		  	"		 false,     " +
		  	"		 false,     " +
 		  	");"
 		);
 		
 		try {
 			eps.set( "msg", messageNo );
 			eps.update();
 			return eps.getGeneratedKey();
 		}
 		finally {
 			eps.close();
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get orders sent to this person
 	 * Note: the string part is for display only
 	 * @param personNo
 	 * @return ArrayList<NameRefPair<Integer>>
 	 */
 	public static ArrayList<NameRefPair<Integer>> getOrdersAndSubjectsSentTo( Integer personNo ) {
 		return getOrdersAndSubjects( "sentTo", personNo );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get orders sent by this person
 	 * Note: the string part is for display only
 	 * @param personNo
 	 * @return ArrayList<NameRefPair<Integer>>
 	 */
 	public static ArrayList<NameRefPair<Integer>> getOrdersAndSubjectsSentBy( Integer personNo ) {
 		return getOrdersAndSubjects( "sentBy", personNo );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get message contents for a given order.
 	 * @param orderNo
 	 * @return EnhancedResultSet
 	 */
 	public static EnhancedResultSet getMessageContents( Integer orderNo )
 	{
 		return Database.query(
 			"select sentDate,                " +
 			"       subject,                 " +
 			"       body,                    " +
 			"       sentBy,                  " +
 			"       sentTo                   " +
 			"                                " +
 			"from Orders   o,                " +
 			"     Messages m                 " +
 			"                                " +
 			"where o.messageNo = m.messageNo " +
 			"  and o.orderNo   = " + orderNo  + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Mark an order as done.
 	 * Implicitly marks it as read as well.
 	 * @param orderNo
 	 * @return succeeded
 	 */
 	public static boolean markAsDone( Integer orderNo )
 	{
 		return 0 != Database.update(
 			"update Orders     " +
 			"set isDone = true," +
 			"    isRead = true " +
 			"where orderNo =   " + orderNo + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Mark an order as read.
 	 * @param orderNo
 	 * @return succeeded
 	 */
 	public static boolean markAsRead( Integer orderNo )
 	{
 		return 0 != Database.update(
 			"update Orders     " +
 			"set isRead = true " +
 			"where orderNo =   " + orderNo + ";"
 		);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private static ArrayList<NameRefPair<Integer>> getOrdersAndSubjects( String sentByOrTo, Integer personNo )
 	{
 		EnhancedResultSet ers = Database.query(
 			"select orderNo, subject, isDone, isRead " +
 			"                                        " +
 			"from Orders   o,                        " +
 			"     Messages m                         " +
 			"                                        " +
 			"where o.messageNo = m.messageNo         " +
 			"  and m." + sentByOrTo + " = " + personNo +
 			"                                        " +
 			"order by sentDate desc;                 "
 		);
 		
 		Integer[] orderNo = ers.getColumnAs( "orderNo", Integer[].class );
 		String [] subject = ers.getColumnAs( "subject", String [].class );
 		Boolean[] isDone  = ers.getColumnAs( "isDone",  Boolean[].class );
 		Boolean[] isRead  = ers.getColumnAs( "isRead",  Boolean[].class );
 		String [] display = new String[ subject.length ];
 		
 		for (int i=0; i<orderNo.length; i++)
 		{
 			String  pre = "";
 			boolean forSender = sentByOrTo.equals( "sentBy" );
 			
 			if ( ! isRead[i]) {
 				pre = (!forSender)  ?  "[*New]"  :  "[Not read]";
 			} else {
 				if ( ! isDone[i])
 					 pre = (!forSender)  ?  "[Todo]"  :  "[Not done]";
 			    else pre = "[Done]";
 			}
 			
 			display[i] = new String( pre + " " + subject[i] );
 		}
 		
 		return new NameRefPairList<Integer>( orderNo, display );
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
