 package irc;
 
 import java.util.Vector;
 
 class MessageParser {
 	
 	public static Message parse( String msg ) {
 
 		if (msg.length() == 0)
 			throw new RuntimeException("Empty message");
 
 
 		Message message = new Message();
 
 		message.setRaw(msg);
 
 		Vector<String> args = new Vector<String>(2);
 
 		String trailing = null;
 		String prefix = null;
 
 		int pos = -1;
 
 		//check for a leading :, which indicates the meessage has a source
 		if ( msg.charAt(0) == ':' ) {
 			pos = msg.indexOf(' ',1);
 			prefix = msg.substring(1, pos);
 			msg = msg.substring(pos+1);
 		}
 
 		//check for a " :" delimiter, which separates the message "header" from the body.
 		if ( (pos = msg.indexOf(" :")) != -1 ) {
 			trailing = msg.substring(pos+2);
 			
 			//when there is a "header/body",
 			//we only care about parsing 
 			//the header.
 			msg = msg.substring(0,pos); 	
 		}
 	
 		for (String arg : msg.split(" "))
 			args.add(arg);
 
 		if ( prefix != null ) 
 			message.setSource( new MessageTarget( prefix ) );
 		else 
 			message.setSource( new MessageTarget() );
 
 		if ( args.size() >= 2 ) {
 			String t = args.get(1);
 
 			if (t.equals("=") || t.equals("*"))
 				t = args.get(2);
 
 			message.setTarget( new MessageTarget( t ) );
 		}
 	
 		else 
 			message.setTarget( new MessageTarget() );
 
 		if (trailing != null) {
 			args.add(trailing);
 			
 			message.setMessage( trailing );
 		}
 		else
 			message.setMessage( msg );
 
 		String command;
 
 		message.setCommand( command = args.get(0) );
 
 		message.setArgs( args.toArray(new String[args.size()]) );
 
 		MessageCode code = MessageCode.get( command );
 
 		message.setCode( code );
 
 		MessageType type;
 
 		Priority priority = Priority.MEDIUM;
 
 		if ( code != null )
 			type = code.getType();
 
 		else {
			if ( msg.startsWith("\u0001ACTION\u0001") )
 				type = MessageType.ACTION;
 
 			else if ( command.equals("PRIVMSG") ) {
 				
				if ( msg.startsWith("\u0001") ) {
 					type = MessageType.CTCP;
 					priority = Priority.LOW;
 				} 
 
 				else if (args.size() >= 2 && args.get(1).startsWith("#")) 
 					type = MessageType.CHANNEL;
 
 				else 
 					type = MessageType.QUERY;
 			}
 
 			else if ( command.equals("NOTICE") )
 				type = MessageType.NOTICE;
 
 			else if ( command.equals("INVITE") )
 				type = MessageType.INVITE;
 
 			else if ( command.equals("JOIN") ) {
 				type = MessageType.JOIN;
 				
 				//make the channel the target for JOIN
 				//VIA rfc2812, servers shouldn't use a CSV list
 				//when sending JOINs to clients (e.g. this is valid)
 				message.setTarget( new MessageTarget( message.getMessage() ) );
 			}
 
 			else if ( command.equals("TOPIC") )
 				type = MessageType.TOPICCHANGE;
 
 			else if ( command.equals("NICK") ) {
 				type = MessageType.NICKCHANGE;
 	
 				//similar to JOIN
 				message.setTarget( new MessageTarget( message.getMessage() ) );
 			}
 
 			else if ( command.equals("KICK") )
 				type = MessageType.PART;
 
 			else if ( command.equals("PART") )
 				type = MessageType.PART;
 
 			else if ( command.equals("MODE") )
 				type = MessageType.MODECHANGE;
 
 			else if ( command.equals("QUIT") )
 				type = MessageType.QUIT;
 
 			else if ( command.equals("ERROR") )
 				type = MessageType.ERROR;
 
 			else 
 				type = MessageType.UNKNOWN;
 		}
 
 		if ( type == MessageType.ERROR )
 			priority = Priority.HIGH;
 
 		else if ( type == MessageType.PING )
 			priority = Priority.CRITICAL;
 
 		else if ( type ==  MessageType.UNKNOWN )
 			priority = Priority.LOW;
 
 		message.setPriority(priority);
 		message.setType(type);
 
 		return message;
 	}
 }
