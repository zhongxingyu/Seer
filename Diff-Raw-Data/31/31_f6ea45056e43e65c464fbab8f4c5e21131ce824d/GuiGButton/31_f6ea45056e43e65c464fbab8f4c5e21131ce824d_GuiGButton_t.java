 
 /**************************************************************
  *
  * - CeCILL-B license
  * - (bsd-like, check http://www.cecill.info/faq.en.html#bsd)
  * 
  * Copyright CNRS
  * Contributors:
  * David Gauchard <gauchard@laas.fr>	2013-01-01
  * 
  * This software is a computer program whose purpose is to
  * provide a "Graphical Access To Exterior" (GATE).  The goal
  * is to provide a generic GUI, within a javascript web
  * browser, through a TCP network using the websocket protocol. 
  * Plain text protocol (simple human readable graphic commands)
  * translators to websockets protocol are also provided to
  * connect user applications to the browser via a C library or
  * simple TCP server.
  * 
  * This software is governed by the CeCILL-B license under
  * French law and abiding by the rules of distribution of free
  * software.  You can use, modify and/ or redistribute the
  * software under the terms of the CeCILL-B license as
  * circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info".
  * 
  * As a counterpart to the access to the source code and rights
  * to copy, modify and redistribute granted by the license,
  * users are provided only with a limited warranty and the
  * software's author, the holder of the economic rights, and
  * the successive licensors have only limited liability.
  * 
  * In this respect, the user's attention is drawn to the risks
  * associated with loading, using, modifying and/or developing
  * or reproducing the software by the user in light of its
  * specific status of free software, that may mean that it is
  * complicated to manipulate, and that also therefore means
  * that it is reserved for developers and experienced
  * professionals having in-depth computer knowledge.  Users are
  * therefore encouraged to load and test the software's
  * suitability as regards their requirements in conditions
  * enabling the security of their systems and/or data to be
  * ensured and, more generally, to use and operate it in the
  * same conditions as regards security.
  * 
  * The fact that you are presently reading this means that you
  * have had knowledge of the CeCILL-B license and that you
  * accept its terms.
  * 
  *************************************************************/
 
 
 package fr.laas.gate;
 
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 
 import fr.laas.gate.GuiGFX.Gfx;
 
 //XXX todo: improve corners
 
 class GuiGButton extends GuiGPanel
 {	
 	private final String cmdtxtH = " add t text 50 50 80 100";
 	private final String cmdtxtV = " add t text 50 50 100 80";
 	private String cmdfg = " color t black";
 	private String cmdbg = " color p #ccc";
 	private String userText = "-";
 	private Gfx gText = null;
 	
 	public GuiGButton (IntfObject parent, String name) 
 	{
 		super(parent, name);
 		place.setGap(Gate.defaultGap);
 		setEnabled(true);
 	}
 	
 	public static String help ()
 	{
 		return
 					      "#\ttext <t>\tchange text in button"
 			+ Gate.endl + "#\tdisable"
 			+ Gate.endl + "#\tenable"
 			;
 	}
 	
 	private Gfx getText ()
 	{
 		if (gText == null && gfxPanel != null)
 			gText = gfxPanel.getGfx("t");
 		return gText;
 	}
 	
 	private String cmdtxt()
 	{
 		return getText() != null && getText().isVertical()? cmdtxtV: cmdtxtH; 
 	}
 	
 	private static final float z1 = 10f;
 	private static final float z2 = 100f - z1;
 	private static final float z3 = z1 * 0.293f; // (2-V2)/2
 	private static final float z4 = 100f - z3;
 	private static final String sp = " ";
 	protected String gfxCreateString ()
 	{
 		return   cmdtxt() + " " + userText 
 				+ " add p path "
 					+       z3   + sp + z3
 					+ sp +  z1   +     " 0 "
 					+       z2   +     " 0 "
 					+       z4   + sp + z3
 					+    " 100 " +      z1
 					+    " 100 " +      z2
 					+ sp +  z4   + sp + z4
 					+ sp +  z2   +   " 100 "
 					+       z1   +   " 100 "
 					+       z3   + sp + z4
 					+      " 0 " +      z2
 					+      " 0 " +      z1
 				+ cmdfg
 				+ cmdbg
 				;
 	}
 	
	private void blur ()
	{
		parse(name + "# update" + cmdfg + ",0.5" + cmdbg + ",0.5");		
	}
	
	private void unBlur ()
	{
		parse(name + "# update" + cmdfg + cmdbg);
	}
	
 	protected void gfxSetup ()
 	{
 		gfx.addMouseDownHandler(new MouseDownHandler()
 		{
 			public void onMouseDown (MouseDownEvent event)
 			{
 				if (isEnabled())
					blur();
 			}
 		});
 		
 		gfx.addMouseUpHandler(new MouseUpHandler()
 		{
 			public void onMouseUp (MouseUpEvent event)
 			{
				if (isEnabled())
					unBlur();
 			}
 		});
 		
 		gfx.addMouseOutHandler(new MouseOutHandler()
 		{
 			public void onMouseOut (MouseOutEvent event)
 			{
				if (isEnabled())
					unBlur();
 			}
 		});
 	}
 	
 	private void updateText ()
 	{
 		parse(name + "# update del t;" + name + "# update" + cmdtxt() + " " + userText + cmdfg + cmdbg);
 	}
 	
 	public boolean update (Words words) throws WordsException
 	{		
 		if (words.checkNextAndForward("text"))
 		{
 			userText = "'" + words.getString(Gate.cmdlineText) + "'";
 			updateText();
 		}
 		else if (words.checkNextAndForward("enable"))
		{
			unBlur();
 			setEnabled(true);
		}
 		else if (words.checkNextAndForward("disable"))
		{
			blur();
 			setEnabled(false);
		}
 		else if (words.checkNextAndForward("fg"))
 			parse(name + "# update" + (cmdfg = " color t " + words.getString(Gate.cmdlineColor)));
 		else if (words.checkNextAndForward("bg"))
 			parse(name + "# update" + (cmdbg = " color p " + words.getString(Gate.cmdlineColor)));
 		else
 			return false;		
 		
 		return true;
 	}
 	
 	public boolean redraw ()
 	{
 		if (super.redraw())
 		{
 			if (getText() != null)
 			{
 				if (gText.isVertical() && getOffsetWidth() >= getOffsetHeight())
 					gText.setVertical(false);
 				else if (!gText.isVertical() && getOffsetWidth() < getOffsetHeight())
 					gText.setVertical(true);
 			}
 
 			return true;
 		}
 		return false;
 	}
 		
 } // class GuiGButton
