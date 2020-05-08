 package org.freecode.irc.event.internal;
 
 import org.freecode.irc.CtcpResponse;
 import org.freecode.irc.IrcConnection;
 import org.freecode.irc.Notice;
 import org.freecode.irc.event.CtcpResponseListener;
 import org.freecode.irc.event.NoticeListener;
 
 /**
  * User: Shivam
  * Date: 29/07/13
  * Time: 15:51
  */
public class RawNoticeProcessor extends RawLineProcessor {
 
 	private IrcConnection connection;
 
 	public RawNoticeProcessor(IrcConnection connection) {
        super(connection);
 	}
 
 	public boolean qualifies(String rawLine) {
 		final String[] parts = rawLine.split(" ", 4);
 		return parts.length == 4 && parts[1].equals("NOTICE");
 	}
 
 	public void execute(String rawLine) {
 		if (CtcpResponse.isCtcpResponse(rawLine)) {
 			CtcpResponse ctcpResponse = new CtcpResponse(rawLine, connection);
 			for(CtcpResponseListener listener : connection.getDelegates(CtcpResponseListener.class)) {
 				listener.onCtcpResponse(ctcpResponse);
 			}
 		} else {
 			Notice n = new Notice(rawLine,connection);
 			for(NoticeListener listener : connection.getDelegates(NoticeListener.class)) {
 				listener.onNotice(n);
 			}
 		}
 	}
 }
