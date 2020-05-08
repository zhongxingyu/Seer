 /* ExampleApplet.java - an example applet
  * Copyright (C) 2001 Fredrik Ehnbom
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 import java.applet.Applet;
 import java.awt.*;
 
 import org.gjt.fredde.silence.*;
 import org.gjt.fredde.silence.format.*;
 
 
 /**
  * An example applet for silence.
  *
  * @author Fredrik Ehnbom
 * @version $Id: ExampleApplet.java,v 1.2 2001/01/27 18:18:01 fredde Exp $
  */
 public class ExampleApplet
 	extends Applet
 {
 	private Silence silence = new Silence();
 	private AudioFormat format;
 
 	private String[] text = new String[10];
 	private int textPos = 0;
 
 	public void init() {
 		try {
 			int dform = 0;
 
 			String file = getParameter("file");
 			if (file == null) {
 				throw new Exception("No file specified. Device not created");
 			}
 			text[textPos++] = "playing \"" + file + "\"";
 
 			String khz = getParameter("kHz");
 			if (khz == null) khz = "";
 
 			if (khz.equals("22")) {
 				dform = Silence.FORMAT_PCM22K16S;
 			} else if (khz.equals("11")) {
 				dform = Silence.FORMAT_PCM11K16S;
 			} else {
 				dform = Silence.FORMAT_PCM44K16S;
 			}
 
 			// initialize silence
			silence.init(dform, this);
 			text[textPos++] = "using device \"" + silence.getDevice() + "\"";
 
 			// load the specified file
 			format = silence.load(file);
 			text[textPos++] = "with audioformat \"" + format + "\"";
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			text[textPos++] = e.getMessage();
 		}
 	}
 
 	public void start() {
 		try {
 			// play the file
 	        	silence.play(format);
 		} catch (Exception e) {
 			System.err.println(e.toString());
 			text[textPos++] = e.getMessage();
 		}
 	}
 
 	public void stop() {
 		silence.stop();
 	}
 
 	public void destroy() {
 		format.close();
 	}
 
 	public void paint(Graphics g) {
		g.setColor(new Color(224, 224, 224));
		Dimension d = getSize();
		g.fillRect(0, 0, d.width, d.height);

		g.setColor(Color.black);
 		for (int i = 0; i < text.length; i++) {
 			if (text[i] == null) continue;
 
 		        g.drawString(text[i], 2, 10 + i * 15);
 		}
 	}
 }
 /*
  * ChangeLog:
  * $Log: ExampleApplet.java,v $
 * Revision 1.2  2001/01/27 18:18:01  fredde
 * some fixes
 *
  * Revision 1.1  2001/01/11 20:28:26  fredde
  * initial commit
  *
  */
