 
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
 
 import java.util.List;
 
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 
 class GuiImage extends Image implements IntfObject
 {	
 	///////////////////////////////////////////////////////
 	// IntfObject implementation
 
 	String				name			= null;
 	IntfObject			parent			= null;
 	Place				place			= null;
 		
 	public String			getName		()	{ return name; }
 	public IntfObject		getGOParent	()	{ return parent; }
 	public Widget			getWidget	()	{ return this; }
 	public Place			getPlace	()	{ return place; }
 	public List<IntfObject>	getSons		()	{ return null; }
 
 	
 	public GuiImage (final IntfObject parent, final String name)
 	{
 		setStyleName("");
 		this.name = name;
 		this.parent = parent;
 		place = new Place(this);
 		
 	    parent.addSon(this, name);
 	}
 		
 	public static String help ()
 	{
 		return "#\turl <url>\tload image";
 	}
 	
 	public boolean update (final Words words) throws WordsException
 	{
 		if (words == null)
 			return true;
 		
 		if (words.checkNextAndForward("url"))
 			setUrl(words.getString(Gate.cmdlineText));
 		else 
 			return false;
 		return true;
 	}
 		
 	public void	setSonTitle (final IntfObject son, final String title)
 	{
 		// this widget do not have son or title
 	}
 	
 	public boolean addSon (final IntfObject son, final String name)
 	{
 		// this widget is not a container
 		return false;
 	}	
 	
 	public void delSon (IntfObject son)
 	{
 		// this widget is not a container
 	}	
 	
 	public boolean setSonPosition (final IntfObject son)
 	{
 		// this widget do not have sons
 		return false;
 	}
 	
 	public boolean redraw ()
 	{
 		// ok
 		return true;
 	}
 
 } // class GuiButton
