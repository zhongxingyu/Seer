 package com.taobao.alexander.net;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.Map;
 
 import com.taobao.alexander.protocol.MySQLMessage;
 import com.taobao.alexander.protocol.MySQLPacket;
 import com.taobao.alexander.protocol.SelectVersionComment;
 import com.taobao.alexander.protocol.mysql.ErrorPacket;
 import com.taobao.alexander.sequence.SRange;
 import com.taobao.alexander.sequence.impl.ClustedSequenceService;
 import com.taobao.gecko.core.core.Session;
 
 /**
  * @description
  * @author <a href="junyu@taobao.com">junyu</a>
  * @date 2013-3-705:25:19
  */
 public class SequenceDataHandler implements DataHandler {
 	public final ClustedSequenceService sequence;
 
 	public SequenceDataHandler(ClustedSequenceService sequence) {
 		this.sequence = sequence;
 	}
 
 	public static final int ER_UNKNOWN_CHARACTER_SET = 1115;
 	public static final int ER_NOT_ALLOWED_COMMAND = 1148;
 	public static final int ER_UNKNOWN_COM_ERROR = 1047;
 
 	public void handle(byte[] data, Session source) {
 		switch (data[4]) {
 		case MySQLPacket.COM_QUERY:
 			MySQLMessage mm = new MySQLMessage(data);
 			mm.position(5);
 			String sql = null;
 			try {
 				sql = mm.readString(Charset.defaultCharset().name());
 			} catch (UnsupportedEncodingException e) {
 				ErrorPacket err = new ErrorPacket();
 				err.packetId = (byte) 1;
 				err.errno = ER_UNKNOWN_CHARACTER_SET;
 				err.message = ("Unknown charset '"
 						+ Charset.defaultCharset().name() + "'").getBytes();
 				source.write(err.encode().flip());
 				return;
 			}
 			if (sql == null || sql.length() == 0) {
 				ErrorPacket err = new ErrorPacket();
 				err.packetId = (byte) 1;
 				err.errno = ER_NOT_ALLOWED_COMMAND;
 				err.message = ("Empty SQL").getBytes();
 				source.write(err.encode().flip());
 				return;
 			}
 
 			this.doQuery(sql, source);
 			break;
 		default:
 			ErrorPacket err = new ErrorPacket();
 			err.packetId = (byte) 1;
 			err.errno = ER_UNKNOWN_COM_ERROR;
 			err.message = ("Unknown command").getBytes();
 			source.write(err.encode());
 		}
 	}
 
 	public void doQuery(String sql, Session session) {
 		if (sql.trim().toLowerCase().startsWith("select next_val()")) {
 			handleNextVal(sql, 17, session);
 		} else if (sql.trim().toLowerCase().startsWith("select next_range()")) {
 			handleNextRange(sql, 19, session);
 		} else if (sql.startsWith("select @@version_comment")) {
 			SelectVersionComment.response(session);
 		} else {
 			writeErrMessage(session, (byte) 1, ER_UNKNOWN_COM_ERROR,
 					"only support 'select next_val()' or 'select next_range()'");
 		}
 	}
 
 	private void handleNextVal(String sql, int offset, Session source) {
 		String value = sql.substring(offset);
 		String cluster;
 		String slice;
 		try {
 			if (value == null || "".equals(value)) {
 				cluster = null;
 				slice = null;
 			} else {
 				int start1 = sql.indexOf(" cluster");
 				int end1 = sql.indexOf(" and ");
 				cluster = sql.substring(start1 + " cluster='".length(),
 						end1 - 1);
 
 				int start2 = sql.indexOf(" slice");
 				slice = sql.substring(start2 + " slice='".length(),
 						sql.length() - 1);
 			}
 
 			if (cluster == null || "".equals(cluster)) {
 				cluster = this.sequence.getDefaultCluster();
 			}
 
 			if (slice == null || "".equals(slice)) {
 				slice = this.sequence.getDefaultSlice();
 			}
 
 			Map<String, String> au = this.sequence.getAuths().get(slice);
 			if (au == null) {
 				throw new RuntimeException("Access denied,no sequence named "
 						+ slice);
 			} else {
 				String user = (String) source
 						.getAttribute(FrontConnectionHandler.AUTH_USER);
 				String pass = au.get(user);
 				if (pass == null) {
 					throw new RuntimeException("Access denied,sequence "
 							+ slice + " not allow " + user + " access!");
 				}
 			}
 			
 			long seq = this.sequence.nextVal(cluster, slice);
 			NextVal.send(source, cluster, slice, seq);
 		} catch (Exception e) {
 			writeErrMessage(source, (byte) 1, ER_UNKNOWN_COM_ERROR,
 					e.getMessage());
 		}
 	}
 
 	private void handleNextRange(String sql, int offset, Session source) {
 		String value = sql.substring(offset);
 		String cluster;
 		String slice;
 		try {
 			if (value == null || "".equals(value)) {
 				cluster = null;
 				slice = null;
 			} else {
 				int start1 = sql.indexOf(" cluster");
 				int end1 = sql.indexOf(" and ");
				cluster = sql.substring(start1 + " cluster='".length(),
						end1 - 1);
 
 				int start2 = sql.indexOf(" slice");
				slice = sql.substring(start2 + " slice='".length(),
						sql.length() - 1);
 			}
 
 			if (cluster == null || "".equals(cluster)) {
 				cluster = this.sequence.getDefaultCluster();
 			}
 
 			if (slice == null || "".equals(slice)) {
 				slice = this.sequence.getDefaultSlice();
 			}
 
			Map<String, String> au = this.sequence.getAuths().get(slice);
			if (au == null) {
				throw new RuntimeException("Access denied,no sequence named "
						+ slice);
			} else {
				String user = (String) source
						.getAttribute(FrontConnectionHandler.AUTH_USER);
				String pass = au.get(user);
				if (pass == null) {
					throw new RuntimeException("Access denied,sequence "
							+ slice + " not allow " + user + " access!");
				}
			}
			
 			SRange seq = this.sequence.nextRange(cluster, slice);
 			NextRange.send(source, cluster, slice, seq);
 		} catch (Exception e) {
 			writeErrMessage(source, (byte) 1, ER_UNKNOWN_COM_ERROR,
 					e.getMessage());
 		}
 	}
 
 	private void writeErrMessage(Session source, byte id, int errno, String msg) {
 		ErrorPacket err = new ErrorPacket();
 		err.packetId = id;
 		err.errno = errno;
 		err.message = msg.getBytes();
 		source.write(err.encode().flip());
 	}
 }
