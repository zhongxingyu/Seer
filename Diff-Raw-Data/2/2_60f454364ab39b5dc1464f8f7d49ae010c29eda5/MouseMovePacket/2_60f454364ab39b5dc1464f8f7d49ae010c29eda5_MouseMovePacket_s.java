 package marino39.agamepad.protocol;
 
 public class MouseMovePacket implements Packet {
 	
 	private byte op_id = Packet.OPERATION_MOUSE_MOVE; // 0x01
 	private byte len;
 	private float x;
 	private float y;
 	
 	public MouseMovePacket(byte[] b) {
 		setBytes(b);
 	}
 	
 	public MouseMovePacket(byte op_id, byte len, float x, float y) {
 		this.op_id = op_id;
 		this.len = len;
 		this.x = x;
 		this.y = y;
 	}
 	
 	@Override
 	public void setBytes(byte[] b) {
 		op_id = b[0];
 		len = b[1];
 		x = ConvertUtil.toFloat(new byte[] {b[2], b[3], b[3], b[4]});
 		y = ConvertUtil.toFloat(new byte[] {b[6], b[7], b[8], b[9]});
 	}
 
 	@Override
 	public byte[] getBytes() {
 		byte[] data = new byte[10];
 		data[0] = op_id;
 		data[1] = len;
 		byte[] xb = ConvertUtil.toByta(x);
 		data[2] = xb[0]; data[3] = xb[1]; data[4] = xb[2];
 		data[5] = xb[3]; 
		byte[] yb = ConvertUtil.toByta(x);
 		data[6] = yb[0]; data[7] = yb[1]; data[8] = yb[2];
 		data[9] = yb[3]; 
 		return data;
 	}
 	
 	public String toString() {
 		return "MouseMovePacket op_id: " + op_id + " len: " + len + " x: " + x + " y: " + y; 
 	}
 
 	public byte getOp_id() {
 		return op_id;
 	}
 
 	public void setOp_id(byte op_id) {
 		this.op_id = op_id;
 	}
 
 	public byte getLen() {
 		return len;
 	}
 
 	public void setLen(byte len) {
 		this.len = len;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 }
