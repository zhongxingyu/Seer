 // PathVisio,
 // a tool for data visualization and analysis using Biological Pathways
 // Copyright 2006-2007 BiGCaT Bioinformatics
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); 
 // you may not use this file except in compliance with the License. 
 // You may obtain a copy of the License at 
 // 
 // http://www.apache.org/licenses/LICENSE-2.0 
 //  
 // Unless required by applicable law or agreed to in writing, software 
 // distributed under the License is distributed on an "AS IS" BASIS, 
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 // See the License for the specific language governing permissions and 
 // limitations under the License.
 //
 package org.pathvisio.view.swing;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.Action;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToolTip;
 import javax.swing.JWindow;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 
 import org.pathvisio.gui.swing.dnd.PathwayImportHandler;
 import org.pathvisio.model.Pathway;
 import org.pathvisio.model.PathwayElement;
 import org.pathvisio.view.VPathway;
 import org.pathvisio.view.VPathwayElement;
 import org.pathvisio.view.VPathwayEvent;
 import org.pathvisio.view.VPathwayListener;
 import org.pathvisio.view.VPathwayWrapper;
 
 public class VPathwaySwing extends JPanel implements VPathwayWrapper,
 		MouseMotionListener, MouseListener, KeyListener, VPathwayListener {
 	private static final long serialVersionUID = 1L;
 
 	VPathway child;
 
 	JScrollPane container;
 
 	public VPathwaySwing(JScrollPane parent) {
 		super();
 		if (parent == null)
 			throw new IllegalArgumentException("parent is null");
 		this.container = parent;
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addKeyListener(this);
 		
 		setFocusable(true);
 		setRequestFocusEnabled(true);
 		setTransferHandler(new PathwayImportHandler());
 	}
 
 	public void setChild(VPathway c) {
 		child = c;
 		child.addVPathwayListener(this);
 	}
 
 	public VPathway getChild() {
 		return child;
 	}
 	
 	public Rectangle getVBounds() {
 		return getBounds();
 	}
 
 	public Dimension getVSize() {
 		return getPreferredSize();
 	}
 
 	public Dimension getViewportSize() {
 		if (container instanceof JScrollPane) {
 			return ((JScrollPane) container).getViewport().getExtentSize();
 		}
 		return getSize();
 	}
 
 	public void redraw() {
 		repaint();
 	}
 
 	protected void paintComponent(Graphics g)
 	{
 		child.draw((Graphics2D) g);
 	}
 
 	public void redraw(Rectangle r) {
 		repaint(r);
 	}
 
 	public void setVSize(Dimension size) {
 		setPreferredSize(size);
 		revalidate();
 	}
 
 	public void setVSize(int w, int h) {
 		setVSize(new Dimension(w, h));
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		if(e.getClickCount() == 2) {
 			child.mouseDoubleClick(new SwingMouseEvent(e));
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		requestFocus();
 		child.mouseEnter(new SwingMouseEvent(e));
 	}
 
 	public void mouseExited(MouseEvent e) {
 		child.mouseExit(new SwingMouseEvent(e));
 
 	}
 
 	public void mousePressed(MouseEvent e) {
 		requestFocus();
 		child.mouseDown(new SwingMouseEvent(e));
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		child.mouseUp(new SwingMouseEvent(e));
 	}
 
 	public void keyPressed(KeyEvent e) {
 		child.keyPressed(new SwingKeyEvent(e));
 	}
 
 	public void keyReleased(KeyEvent e) {
 		child.keyReleased(new SwingKeyEvent(e));
 	}
 
 	public void keyTyped(KeyEvent e) {
 		// TODO: find out how to handle this one
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		child.mouseMove(new SwingMouseEvent(e));
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		child.mouseMove(new SwingMouseEvent(e));
 	}
 
 	public void registerKeyboardAction(KeyStroke k, Action a) {
 		super.registerKeyboardAction(a, k, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
 		//super.registerKeyboardAction(a, k, WHEN_IN_FOCUSED_WINDOW);
 	}
 	
 	public VPathway createVPathway() {
 		setChild(new VPathway(this));
 		return child;
 	}
 
 	public void vPathwayEvent(VPathwayEvent e) {
 		switch(e.getType()) {
 		case VPathwayEvent.MODEL_LOADED:
 			if(e.getSource() == child) {
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						container.setViewportView(VPathwaySwing.this);
 						container.getViewport().setBackground(Color.GRAY);
 						double[] mSize = child.getPathwayModel().getMappInfo().getMBoardSize();
 						int w = (int)child.vFromM(mSize[0]);
 						int h = (int)child.vFromM(mSize[1]);
 						setVSize(w, h);
						VPathwaySwing.this.requestFocus();
 					}
 				});
 			}
 			break;
 		case VPathwayEvent.ELEMENT_HOVER:
 			showToolTip(e);
 			break;
 		}
 	}
 
 	public void pasteFromClipboard() {
 		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
 		TransferHandler handler = getTransferHandler();
 		handler.importData(this, clip.getContents(this));
 	}
 	
 	List<PathwayElement> lastCopied;
 	
 	public void copyToClipboard(Pathway source, List<PathwayElement> copyElements) {
 		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
 		clip.setContents(new PathwayTransferable(source, copyElements), 
 				(PathwayImportHandler)getTransferHandler());
 		((PathwayImportHandler)getTransferHandler()).obtainedOwnership();
 	}
 	
 	Set<ToolTipProvider> toolTipProviders = new HashSet<ToolTipProvider>();
 	
 	public void addToolTipProvider(ToolTipProvider p) {
 		toolTipProviders.add(p);
 	}
 	
 	public void showToolTip(VPathwayEvent e) {
 		if(toolTipProviders.size() == 0) return;
 		
 		List<VPathwayElement> elements = e.getAffectedElements();
 		if(elements.size() > 0) {
 			PathwayToolTip tip = new PathwayToolTip(elements);
 			
 			if(tip.getComponentCount() == 0) return;
 			
 			final JWindow w = new JWindow();
 
 			w.addMouseListener(new MouseAdapter() {
 				public void mouseExited(MouseEvent e) {
 					w.dispose();
 				}
 			});
 			w.add(tip);
 			w.pack();
 			Point p = e.getMouseEvent().getLocation();
 			SwingUtilities.convertPointToScreen(p, this);
 			w.setLocation(p);
 			w.setVisible(true);
 		}
 	}
 	
 	class PathwayToolTip extends JToolTip {
 		private static final long serialVersionUID = 1L;
 
 		public PathwayToolTip(List<VPathwayElement> elements) {
 			for(ToolTipProvider p : toolTipProviders) {
 				Component c = p.createToolTipComponent(this, elements);
 				if(c != null) add(c);
 			}
 		}
 	}
 
 }
