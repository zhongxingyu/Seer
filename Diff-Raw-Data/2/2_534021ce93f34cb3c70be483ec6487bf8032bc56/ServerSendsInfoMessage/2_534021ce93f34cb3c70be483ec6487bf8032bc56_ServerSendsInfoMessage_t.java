 package Messages;
 
 public class ServerSendsInfoMessage extends Message {
 	String targetUsername = null;
 	public String targetIP = null;
 
	public static final long minSize = 143;
 
 	public ServerSendsInfoMessage(int _op, long _length, long _reserved,
 			String _options, byte[] body) {
 		super(_op, _length, _reserved, _options);
 		processBody(body);
 		if (op != 9) {
 			correct = false;
 		}
 	}
 
 	public ServerSendsInfoMessage(int _op, long _length, long _reserved,
 			String _options, String _targetUsername, String _targetIP) {
 		super(_op, _length, _reserved, _options);
 		targetUsername = _targetUsername;
 		targetIP = _targetIP;
 		if (op != 9) {
 			correct = false;
 		}
 	}
 
 	private void processBody(byte[] body) {
 		if (body.length != 143) {
 			correct = false;
 			return;
 		}
 
 		byte[] targetUserArray = new byte[128];
 		for (int i = 0; i < body.length && i < 128; i++) {
 			targetUserArray[i] = body[i];
 		}
 		targetUsername = new String(targetUserArray, 0, targetUserArray.length);
 		
 		int offset = 128;
 		
 		byte[] targetIPArray = new byte[15];
 		for (int i = 0; i < body.length && i < 15; i++) {
 			targetIPArray[i] = body[i + offset];
 		}
 
 		targetIP = new String(targetIPArray, 0, targetIPArray.length);
 		
 	}
 
 	public byte[] convert() {
 		byte[] upper = super.convert();
 		byte[] storage = new byte[(int) (upper.length + minSize)];
 		for (int i = 0; i < upper.length; i++) {
 			storage[i] = upper[i];
 		}
 
 		int total = upper.length;
 
 		byte[] tmp = null;
 
 		tmp = targetUsername.getBytes();
 		for (int i = 0; i < tmp.length; i++) {
 			storage[total + i] = tmp[i];
 		}
 
 		total += 128;
 
 		tmp = targetIP.getBytes();
 		for (int i = 0; i < tmp.length; i++) {
 			storage[total + i] = tmp[i];
 		}
 
 		total += 15;
 
 		return storage;
 	}
 }
