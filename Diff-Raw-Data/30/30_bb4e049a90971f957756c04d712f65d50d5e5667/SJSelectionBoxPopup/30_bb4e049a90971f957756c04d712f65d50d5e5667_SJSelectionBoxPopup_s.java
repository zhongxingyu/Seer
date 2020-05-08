 /**
  *
  * This file is part of the SJWidget library.
  * (c) 2005-2012 Bjorn Roche
  * Development of this library has been supported by Indaba Media (http://www.indabamusic.com)
  * and XO Audio (http://www.xoaudio.com)
  *
  * for copyright and sharing permissions, please see the COPYING.txt file which you should
  * have recieved with this file.
  *
  */
 
 package com.xowave.sjwidget;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.awt.event.WindowListener;
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JWindow;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import com.xowave.util.GlobalEventManager;
 
 
 /**
  * @author bjorn
  *
  */
 class SJSelectionBoxPopup extends SJPanel implements WindowListener, ActionListener {
 	private Window w;
 	private SJSelectionBox selectionBox;
 	private SJScrollPane scroller;
 
 	public SJSelectionBoxPopup(String id) {
 		super( null, id );
 		setFocusTraversalKeysEnabled(false);
 		setLayout( new BoxLayout(this,BoxLayout.Y_AXIS) );
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "done");
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "done");
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "next");
 		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "prev");
 		getActionMap().put("done", new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				w.dispose();
 			}
 		});
 		getActionMap().put("next", new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				w.dispose();
 				Container cont = selectionBox.getFocusCycleRootAncestor();
 				if( cont != null ) {
 					Component c = cont.getFocusTraversalPolicy().getComponentAfter(cont, selectionBox);
 					if( c != null )
 						c.requestFocusInWindow();
 				}
 			}
 		});
 		getActionMap().put("prev", new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				w.dispose();
 				Container cont = selectionBox.getFocusCycleRootAncestor();
 				if( cont != null ) {
 					Component c = cont.getFocusTraversalPolicy().getComponentBefore(cont, selectionBox);
 					if( c != null )
 						c.requestFocusInWindow();
 				}
 			}
 		});
 		getActionMap().put("up", new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				selectionBox.selectPrevious();
 			}
 		});
 		getActionMap().put("down", new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				selectionBox.selectNext();
 			}
 		});
 	}
 
	public void show( SJSelectionBox selBox, Component parent, int x, int y ) {
 		// we can't use regular pop-ups, b/c they don't support focus.
 		// create our window:
 		if( w != null )
 			w.dispose();
 		this.selectionBox = selBox;
 		//windowForComponent() seems to call up the parent in dialogs. Not what we want.
 		Window parentWindow = SwingUtilities.getWindowAncestor(parent);
 		Container cp;
 		if( parentWindow instanceof JDialog ) {
 			w = new JDialog( (JDialog) parentWindow );
 			((JDialog)w).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			((JDialog)w).setUndecorated(true);
 			cp = ((JDialog)w).getContentPane();
 		} else if( parentWindow instanceof JFrame ) {
 			// jframe causes wierdness with menus
 			// jwindow doesn't dissapear correctly.
 			w = new JDialog( (JFrame) parentWindow );
 			((JDialog)w).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			((JDialog)w).setUndecorated(true);
 			cp = ((JDialog)w).getContentPane();
 		} else {
 			w = new JDialog();
 			((JDialog)w).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			((JDialog)w).setUndecorated(true);
 			cp = ((JWindow)w).getContentPane();
 		}
 		
 		//figure our dimensions and stuff:
 		Dimension d = getPreferredSize();
		Point p = parent.getLocationOnScreen();
		p.y += y ;
		p.x += x ;
 		if( d.height > 300 ) {
 			scroller = new SJScrollPane( this, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, null );
 			d.height = 300;
 			scroller.setPreferredSize( d );
 			cp.add( scroller );
 		} else {
 			cp.add( this );
 		}
 //		w.setAlwaysOnTop(true);
 		w.pack();
 		w.setLocation(p.x, p.y);
 		w.setVisible(true);
 		GlobalEventManager.suspendWhileVisible(w);
 		w.addWindowFocusListener( new WindowFocusListener() {
 			@Override
 			public void windowGainedFocus(WindowEvent arg0) {
 				if( scroller != null )
 					scroller.getViewport().scrollRectToVisible( getComponent( selectionBox.getSelectedIndex() ).getBounds() );
 			}
 			@Override
 			public void windowLostFocus(WindowEvent arg0) {
 				w.setVisible(false);
 				w.dispose();
 			}
 		});
 
 		Window s = SwingUtilities.windowForComponent(parent);
 		if( s != null )
 			s.addWindowListener( this );
 	}
 	public void add( JMenuItem mi ) {
 		super.add(mi);
 		mi.addActionListener(this);
 	}
 	public void insertSeparator() {
 		add( new Separator() );
 	}
 	public void insertSeparator(int index) {
 		add( new Separator(), index );
 	}
 	private final class Separator extends JComponent {
 		@Override
 		protected void paintComponent(Graphics g) {
 			g.setColor( SJSelectionBoxPopup.this.getForeground() );
 			g.fillRect(0, 0, getWidth()/2, getHeight());
 		}
 
 		@Override
 		public boolean isOpaque() {
 			return false;
 		}
 
 		@Override
 		public Dimension getMinimumSize() {
 			return new Dimension( 0, 2 );
 		}
 
 		@Override
 		public Dimension getMaximumSize() {
 			return new Dimension( Integer.MAX_VALUE, 3 );
 		}
 
 		@Override
 		public Dimension getPreferredSize() {
 			return new Dimension( 1, 3 );
 		}
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 //		w.dispose();
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {}
 
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 		w.dispose();
 	}
 
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if( w != null )
 			w.dispose();
 	}
 }
