 package technobotts.rescue;
 
 import java.io.*;
 
 import lejos.nxt.addon.ColorSensor;
 
 public class RawColor
 {
 	public static RawColor fromHTSensor(ColorSensor cs)
 	{
		return new RawColor(cs.getRawRed(), cs.getRawGreen(), cs.getRawBlue());
 	}
 
 	private int	_r;
 	private int	_g;
 	private int	_b;
 
 	public RawColor(int r, int g, int b)
 	{
 		_r = r;
 		_g = g;
 		_b = b;
 	}
 
 	public RawColor(int[] rgb)
 	{
 		this(rgb[0], rgb[1], rgb[2]);
 	}
 
 	public long distanceTo(RawColor that)
 	{
 		long deltaR = this._r - that._r;
 		long deltaG = this._g - that._g;
 		long deltaB = this._b - that._b;
 
 		return deltaR * deltaR + deltaG * deltaG + deltaB * deltaB;
 	}
 
 	public int getR()
 	{
 		return _r;
 	}
 
 	public int getG()
 	{
 		return _g;
 	}
 
 	public int getB()
 	{
 		return _b;
 	}
 
 	public String toString()
 	{
 		return _r + "," + _g + "," + _b;
 	}
 
 	public void writeObject(OutputStream out) throws IOException
 	{
 		DataOutputStream data = new DataOutputStream(out);
 		data.writeInt(_r);
 		data.writeInt(_g);
 		data.writeInt(_b);
 	}
 
 	public static RawColor readObject(InputStream in) throws IOException
 	{
 		DataInputStream data = new DataInputStream(in);
 		int r = data.readInt();
 		int g = data.readInt();
 		int b = data.readInt();
 		return new RawColor(r, g, b);
 	}
 
 	public RawColor add(RawColor that)
 	{
 		return new RawColor(this.getR() + that.getR(),
 		                    this.getG() + that.getG(),
 		                    this.getB() + that.getB());
 	}
 	
 	public RawColor divide(int divisor)
 	{
 		return new RawColor(this.getR()/divisor,
 		                    this.getG()/divisor,
 		                    this.getB()/divisor);
 	}
 }
