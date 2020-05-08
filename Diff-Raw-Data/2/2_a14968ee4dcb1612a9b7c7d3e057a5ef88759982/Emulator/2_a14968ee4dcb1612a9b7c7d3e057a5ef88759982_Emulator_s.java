 // Adapted from com.jcraft.jcterm.Emulator
 
 package annahack.telnetconnection;
 
 import java.io.InputStream;
 import java.io.IOException;
 import com.jcraft.jcterm.JCTermAWT;
 
 public abstract class Emulator
 {
 	protected long lastUpdate;
 	
 	protected TerminalSymbol[][] screen;
 	protected byte fground=7;
 	protected byte bground=0;
 	protected boolean light_colors=false;
 	protected boolean reverse_colors=false;
 
 	// Added methods from JCTermAWT
 	public byte getColor(byte index)
 	{
 		if (JCTermAWT.colors == null || index < 0
 				|| JCTermAWT.colors.length <= index)
 			return -1;
 		return index;
 	}
 
 	// End of added methods
 
 	InputStream in = null;
 
 	public Emulator(TerminalSymbol[][] screen, InputStream in)
 	{
 		this.screen = screen;
 		this.in = in;
 		lastUpdate=System.currentTimeMillis();
 	}
 
 	public abstract void start();
 
 	public abstract byte[] getCodeENTER();
 
 	public abstract byte[] getCodeUP();
 
 	public abstract byte[] getCodeDOWN();
 
 	public abstract byte[] getCodeRIGHT();
 
 	public abstract byte[] getCodeLEFT();
 
 	public abstract byte[] getCodeF1();
 
 	public abstract byte[] getCodeF2();
 
 	public abstract byte[] getCodeF3();
 
 	public abstract byte[] getCodeF4();
 
 	public abstract byte[] getCodeF5();
 
 	public abstract byte[] getCodeF6();
 
 	public abstract byte[] getCodeF7();
 
 	public abstract byte[] getCodeF8();
 
 	public abstract byte[] getCodeF9();
 
 	public abstract byte[] getCodeF10();
 
 	public abstract byte[] getCodeTAB();
 
 	public void reset()
 	{
 		term_width = screen[0].length;
 		term_height = screen.length;
 		region_x1 = 1;
 		region_x2 = term_height;
 	}
 
 	byte[] buf = new byte[1024];
 	int bufs = 0;
 	int buflen = 0;
 
 	byte getChar() throws java.io.IOException
 	{
 		if (buflen == 0)
 		{
 			fillBuf();
 		}
 		buflen--;
 
 		// System.out.println("getChar: "+new
 		// Character((char)buf[bufs])+"["+Integer.toHexString(buf[bufs]&0xff)+"]");
 
 		return buf[bufs++];
 	}
 
 	void fillBuf() throws java.io.IOException
 	{
 		buflen = bufs = 0;
 		buflen = in.read(buf, bufs, buf.length - bufs);
 		/*
 		 * System.out.println("fillBuf: "); for(int i=0; i<buflen; i++){ byte
 		 * b=buf[i]; System.out.print(new
 		 * Character((char)b)+"["+Integer.toHexString(b&0xff)+"], "); }
 		 * System.out.println("");
 		 */
 		if (buflen <= 0)
 		{
 			buflen = 0;
 			throw new IOException("fillBuf");
 		}
 	}
 
 	void pushChar(byte chr) throws java.io.IOException
 	{
 		// System.out.println("pushChar: "+new
 		// Character((char)foo)+"["+Integer.toHexString(foo&0xff)+"]");
 		buflen++;
 		buf[--bufs] = chr;
 	}
 
 	int getASCII(int len) throws java.io.IOException
 	{
 		// System.out.println("bufs="+bufs+", buflen="+buflen+", len="+len);
 		if (buflen == 0)
 		{
 			fillBuf();
 		}
 		if (len > buflen)
 			len = buflen;
 		int offset = len;
 		byte tmp;
 		while (len > 0)
 		{
 			tmp = buf[bufs++];
 			if (0x20 <= tmp && tmp <= 0x7f)
 			{
 				buflen--;
 				len--;
 				continue;
 			}
 			bufs--;
 			break;
 		}
 		// System.out.println(" return "+(foo-len));
 		return offset - len;
 	}
 
 	protected int term_width = 80;
 	protected int term_height = 24;
 
 	protected int x = 0;
 	protected int y = 0;
 	protected int x_ = 0;
 	protected int y_ = 0;
 
 	private int region_x2;
 	private int region_x1;
 
 	protected int tab = 8;
 
 	// Reverse scroll
 	protected void scroll_reverse()
 	{
 		for (int i = region_x2; i > region_x1; i--)
 		{
 			screen[i - 1] = screen[i - 2];
 		}
 		clearRow(region_x1 - 1);
 		lastUpdate=System.currentTimeMillis();
 	}
 
 	// Normal scroll one line
 	protected void scroll_forward()
 	{
 		for (int i = region_x1; i < region_x2; i++)
 		{
 			screen[i - 1] = screen[i];
 		}
 		clearRow(region_x2);
 		lastUpdate=System.currentTimeMillis();
 	}
 
 	// Save cursor position
 	protected void save_cursor()
 	{
 		x_ = x;
 		y_ = y;
 	}
 
 	// Enable alternate character set
 	protected void ena_acs()
 	{
 		// I don't know what this does, so do nothing
 	}
 
 	protected void exit_alt_charset_mode()
 	{
 		// does nothing since ena_acs not supported
 	}
 
 	protected void enter_alt_charset_mode()
 	{
 		// I don't think nethack uses this
 		throw new RuntimeException("alt charset mode not supported");
 	}
 
 	protected void reset_2string()
 	{
 		// TODO
 		// rs2(reset string)
 	}
 
 	protected void exit_attribute_mode()
 	{
 		light_colors=false;
 		reverse_colors=false;
 	}
 
 	protected void exit_standout_mode()
 	{
 		// TODO
 		// term.resetAllAttributes();
 	}
 
 	protected void exit_underline_mode()
 	{
 		// does nothing, not supported
 	}
 
 	protected void enter_bold_mode()
 	{
 		light_colors=true;
 	}
 
 	protected void enter_underline_mode()
 	{
 		// I don't think nethack uses this
 		throw new RuntimeException("Underline mode not supported");
 	}
 
 	protected void enter_reverse_mode()
 	{
 		reverse_colors=true;
 	}
 
 	protected void change_scroll_region(int y1, int y2)
 	{
 		region_x1 = y1;
 		region_x2 = y2;
 	}
 
 	protected void cursor_address(int r, int c)
 	{
 		y = c;
 		x = r;
 	}
 
 	protected void parm_up_cursor(int lines)
 	{
 		x = Math.max(x - lines, 1);
 	}
 
 	protected void parm_down_cursor(int lines)
 	{
 		x = Math.min(x + lines, term_height);
 	}
 
 	protected void parm_left_cursor(int chars)
 	{
 		y = Math.max(y - chars, 1);
 	}
 
 	protected void parm_right_cursor(int chars)
 	{
 		y = Math.min(y + chars, term_width);
 	}
 
 	protected void clr_eol()
 	{
 		for (int j = y - 1; j < term_width; j++)
 		{
 			screen[x][j] = null;
 		}
 		lastUpdate=System.currentTimeMillis();
 	}
 
 	protected void clr_bol()
 	{
 		for (int j = 0; j < y; j++)
 		{
 			screen[x][j] = null;
 		}
 		lastUpdate=System.currentTimeMillis();
 	}
 
 	protected void clr_eos()
 	{
 		clr_eol();
 		for (int i = x; i < term_height; i++)
 		{
 			clearRow(i);
 		}
 		lastUpdate=System.currentTimeMillis();
 	}
 
 	protected void bell()
 	{
 		// like hell
 	}
 
 	protected void tab()
 	{
 		y = ((y / tab + 1) * tab);
 		if (y > term_width)
 		{
 			y = 1;
 			cursor_down();
 		}
 	}
 
 	protected void carriage_return()
 	{
 		y = 1;
 	}
 
 	protected void cursor_left()
 	{
 		parm_left_cursor(1);
 	}
 
 	protected void cursor_down()
 	{
 		parm_down_cursor(1);
 	}
 
 	private byte[] b2 = new byte[2];
 
 	protected void draw_text() throws java.io.IOException
 	{
 		check_region();
 
 		byte b = getChar();
 		// System.out.print(new
 		// Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
 		if ((b & 0x80) != 0)
 		{
 			b2[0] = b;
 			b2[1] = getChar();
 			
 			writeCharacter(b2[0]);
 			writeCharacter(b2[1]);
 		} else
 		{
 			pushChar(b);
 			int asc = getASCII(term_width - x);
 			if (asc != 0)
 			{
 				// System.out.println("foo="+foo+" "+x+", "+(y-char_height)+"
 				// "+(x+foo*char_width)+" "+y+" "+buf+" "+bufs+" "+b+"
 				// "+buf[bufs-foo]);
 				// System.out.println("foo="+foo+" ["+new String(buf, bufs-foo,
 				// foo));
 				for (int c=bufs-asc; c<bufs; c++)
 				{
 					writeCharacter(buf[c]);
 				}
 			} else
 			{
 				writeCharacter(getChar());
 			}
 		}
 	}
 
 	private void check_region()
 	{
 		if (y >= term_width)
 		{
 			// System.out.println("!! "+new
 			// Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
 			y = 1;
 			x ++;
 			// System.out.println("@1: ry="+ry);
 		}
 
 		while (x > region_x2)
 		{
 			x--;
 			for (int i=region_x1; i<region_x2; i++)
 			{
 				screen[i-1]=screen[i];
 			}
 			clearRow(region_x2-1);
 		}
 		lastUpdate=System.currentTimeMillis();
 	}
 	
 	private void writeCharacter(byte c)
 	{
 		check_region();
		screen[x][y]=new TerminalSymbol(c,
 				(byte)(fground+(light_colors?8:0)), bground, reverse_colors);
 		y++;
 		check_region();
 		lastUpdate=System.currentTimeMillis();
 	}
 	
 	protected void clearRow(int row)
 	{
 		System.out.println("Clearing row "+row);
 		screen[row]=new TerminalSymbol[term_width];
 		for (int j=0; j<term_width; j++)
 			screen[row][j]=new TerminalSymbol();
 	}
 }
