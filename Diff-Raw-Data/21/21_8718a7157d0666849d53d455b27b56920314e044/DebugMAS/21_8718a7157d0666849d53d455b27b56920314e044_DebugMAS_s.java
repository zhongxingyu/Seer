 //----------------------------------------------------------------------------
 // ----------------------------------------------------------------------------
 // Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 //
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
 //   Revision 1.4  2005/08/12 21:08:23  jomifred
 //   add cvs keywords
 //
 //----------------------------------------------------------------------------
 
 package jIDE;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 
 /** runs an MAS in debug mode */
 class DebugMAS extends AbstractAction {
 
 	JasonID jasonID;
 
 	DebugMAS(JasonID jID) {
 		super("Debug MAS...", new ImageIcon(JasonID.class.getResource("/images/startDebugger.gif")));
 		jasonID = jID; 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		try {
 			jasonID.fMAS2jThread.fParserMAS2J.debugOn();
 			jasonID.runMASAct.actionPerformed(e);
 		} finally {
 			jasonID.fMAS2jThread.fParserMAS2J.debugOff();
 		}
 	}
 
 }
 
