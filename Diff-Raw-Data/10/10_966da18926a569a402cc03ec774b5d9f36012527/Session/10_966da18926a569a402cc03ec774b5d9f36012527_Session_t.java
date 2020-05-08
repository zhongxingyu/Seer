package cudl.utils;
 
 
 public class Session {
 	private static String session = 
 			"connection =  new Object();"+
 			"session.connection  = connection;"+
 			"connection.local  =  new Object();"+
 			"connection.remote  =  new Object();"+
 			"connection.protocol  =  new Object();"+
 			"connection.protocol.name  =  'isdnvn6';"+
 			"connection.protocol.isdnvn6 =  new Object();"+
 			"connection.protocol.version  =  '1.0';"+
 			"connection.local.uri  =  'tel:0892683613';"+
 			"connection.remote.uri  =  'tel:0400000400';"+
 			"connection.connectionid  =  'CON1642389';"+
 			"connection.protocol.isdnvn6['channel']  =  'vipB07_1';"+
 			"connection.protocol.isdnvn6['evt']  =  't3.incoming';"+
 			"connection.protocol.isdnvn6['infos'] = '153908500 255 2 1 255';"+
 			"connection.protocol.isdnvn6['lastsup'] = '4700830E8307839098720051018A01338800';"+
 			"connection.protocol.isdnvn6['local']  =  '140522806';"+
 			"connection.protocol.isdnvn6['remote']  =  '153908500';"+
 			"connection.protocol.isdnvn6['remote2']  =  '153908500';"+
 			"connection.protocol.isdnvn6['state']  =  'incoming';"+
 			"connection.protocol.isdnvn6.transferresult= undefined;"+
 			"connection.channel = '1';";
 
 	public static String getSessionScript() {
 		return session;
 	}
 }
