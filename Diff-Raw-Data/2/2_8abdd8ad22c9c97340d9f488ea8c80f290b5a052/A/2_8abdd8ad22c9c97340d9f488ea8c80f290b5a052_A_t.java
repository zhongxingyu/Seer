 package net.timendum.denis.message.resources;
 
 import java.io.IOException;
 
 import net.timendum.denis.DomainNameCompressor;
 import net.timendum.denis.io.ArrayDataInputStream;
 import net.timendum.denis.io.ArrayDataOutputStream;
 
 
 public class A extends Resource {
 	private static final int RDLENGTH = 4;
 	private int addr;
 
 	A() {
 	}
 
 	@Override
 	public A newResource() {
 		return new A();
 	}
 
 	@Override
 	public void readData(ArrayDataInputStream dis, DomainNameCompressor dnc, int rdLength) throws IOException {
 		addr = dis.readInt();
 	}
 
 	@Override
 	public void writeData(ArrayDataOutputStream dos) {
 		dos.writeInt(addr);
 
 	}
 
 	private static final int fromArray(byte[] array) {
 		return (((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF));
 	}
 
 	private static final short[] toArray(int addr) {
 		short[] a = new short[4];
 		a[0] = (short) ((addr >>> 24) & 0xFF);
 		a[1] = (short) ((addr >>> 16) & 0xFF);
 		a[2] = (short) ((addr >>> 8) & 0xFF);
 		a[3] = (short) (addr & 0xFF);
 		return a;
 	}
 
 	@Override
 	public StringBuilder toString(StringBuilder sb) {
 		sb.append("AResource[");
 		super.toString(sb);
 		sb.append(",ip=");
 		short[] array = toArray(addr);
 		sb.append(array[0]).append('.').append(array[1]).append('.').append(array[2]).append('.').append(array[3]);
 		sb.append(']');
 		return sb;
 	}
 
 	@Override
 	public String toConsoleString() {
 		short[] array = toArray(addr);
 		return "Address:\t\t" + array[0] + "." + array[1] + "." + array[2] + "." + array[3];
 	}
 
 	@Override
 	public int getRDLength() {
 		return RDLENGTH;
 	}
 
 	@Override
 	public Resource clone() {
 		A a = new A();
 		a.addr = addr;
 		clone(a);
 		return a;
 	}
 
 }
