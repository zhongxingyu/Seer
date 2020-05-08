 // Adapted from com.jcraft.jcterm.Emulator
 
 package annahack.telnetconnection;
 
 import java.io.InputStream;
 import java.io.IOException;
 import com.jcraft.jcterm.JCTermAWT;
 import java.awt.Color;
 
 public abstract class Emulator
 {
 
 	protected TerminalSymbol[][] screen;
 	protected Color fground;
 	protected Color bground;
 
 	// Added methods from JCTermAWT
 	public Object getColor(int index)
 	{
 		if (JCTermAWT.colors == null || index < 0
 				|| JCTermAWT.colors.length <= index)
 			return null;
 		return JCTermAWT.colors[index];
 	}
 
 	// End of added methods
 
 	InputStream in = null;
 
 	public Emulator(TerminalSymbol[][] screen, InputStream in)
 	{
 		this.screen = screen;
 		this.in = in;
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
 		region_y1 = 1;
 		region_y2 = term_height;
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
 
 	void pushChar(byte foo) throws java.io.IOException
 	{
 		// System.out.println("pushChar: "+new
 		// Character((char)foo)+"["+Integer.toHexString(foo&0xff)+"]");
 		buflen++;
 		buf[--bufs] = foo;
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
 		int foo = len;
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
 		return foo - len;
 	}
 
 	protected int term_width = 80;
 	protected int term_height = 24;
 
 	protected int x = 0;
 	protected int y = 0;
 	protected int x_ = 0;
 	protected int y_ = 0;
 
 	private int region_y2;
 	private int region_y1;
 
 	protected int tab = 8;
 
 	// Reverse scroll
 	protected void scroll_reverse()
 	{
 		for (int i = region_y2; i > region_y1; i--)
 		{
 			screen[i - 1] = screen[i - 2];
 		}
 		screen[region_y1 - 1] = new TerminalSymbol[term_width];
 	}
 
 	// Normal scroll one line
 	protected void scroll_forward()
 	{
 		for (int i = region_y1; i < region_y2; i++)
 		{
 			screen[i - 1] = screen[i];
 		}
 		screen[region_y2 - 1] = new TerminalSymbol[term_width];
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
 		// I don't think nethack uses this
 		throw new RuntimeException("ena_acs not supported");
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
 		// TODO
 		// System.out.println("turn off all attributes");
 		// term.resetAllAttributes();
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
 		// I don't think nethack uses this
 		throw new RuntimeException("Bold mode not supported");
 	}
 
 	protected void enter_underline_mode()
 	{
 		// I don't think nethack uses this
 		throw new RuntimeException("Underline mode not supported");
 	}
 
 	protected void enter_reverse_mode()
 	{
 		// I think I'll have to pass on this one
 		throw new RuntimeException("Reverse mode not supported");
 	}
 
 	protected void change_scroll_region(int y1, int y2)
 	{
 		region_y1 = y1;
 		region_y2 = y2;
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
 	}
 
 	protected void clr_bol()
 	{
 		for (int j = 0; j < y; j++)
 		{
 			screen[x][j] = null;
 		}
 	}
 
 	protected void clr_eos()
 	{
 		clr_eol();
 		for (int i = x; i < term_height; i++)
 		{
 			screen[i] = new TerminalSymbol[term_width];
 		}
 	}
 
 	protected void bell()
 	{
 		// like hell
 	}
 
 	protected void tab()
 	{
		x = ((x / tab + 1) * tab);
		if (x > term_width)
 		{
			x = 0;
 			cursor_down();
 		}
 	}
 
 	protected void carriage_return()
 	{
		x = 1;
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
 	private byte[] b1 = new byte[1];
 
 	protected void draw_text() throws java.io.IOException
 	{
 
 		int rx;
 		int ry;
 		int w;
 		int h;
 
 		check_region();
 
 		rx = x;
 		ry = y;
 
 		byte b = getChar();
 		// System.out.print(new
 		// Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
 		if ((b & 0x80) != 0)
 		{
 			b2[0] = b;
 			b2[1] = getChar();
 			
 			term.drawString(new String(b2, 0, 2, "EUC-JP"), x, y);
 			x += char_width;
 			x += char_width;
 			w = char_width * 2;
 			h = char_height;
 		} else
 		{
 			pushChar(b);
 			int foo = getASCII(term_width - (x / char_width));
 			if (foo != 0)
 			{
 				// System.out.println("foo="+foo+" "+x+", "+(y-char_height)+"
 				// "+(x+foo*char_width)+" "+y+" "+buf+" "+bufs+" "+b+"
 				// "+buf[bufs-foo]);
 				// System.out.println("foo="+foo+" ["+new String(buf, bufs-foo,
 				// foo));
 				term.clear_area(x, y - char_height, x + foo * char_width, y);
 				term.drawBytes(buf, bufs - foo, foo, x, y);
 			} else
 			{
 				foo = 1;
 				term.clear_area(x, y - char_height, x + foo * char_width, y);
 				b1[0] = getChar();
 				term.drawBytes(b1, 0, foo, x, y);
 				// System.out.print("["+Integer.toHexString(bar[0]&0xff)+"]");
 			}
 			x += (char_width * foo);
 			w = char_width * foo;
 			h = char_height;
 		}
 		term.redraw(rx, ry - char_height, w, h);
 		term.setCursor(x, y);
 		// no draw cursor!();
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
 
 		if (y > region_y2 * char_height)
 		{
 			while (y > region_y2 * char_height)
 			{
 				y -= char_height;
 			}
 			// no draw cursor!();
 			term.scroll_area(0, region_y1 * char_height, term_width
 					* char_width, (region_y2 - region_y1) * char_height, 0,
 					-char_height);
 			term.clear_area(0, y - char_height, term_width * char_width, y);
 			term.redraw(0, 0, term_width * char_width, region_y2 * char_height);
 			term.setCursor(x, y);
 			// no draw cursor!();
 		}
 	}
 }
