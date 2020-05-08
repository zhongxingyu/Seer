 package org.codemonkeyism.mdidesktop;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyVetoException;
 import java.beans.VetoableChangeListener;
 
 import javax.swing.JComponent;
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 import javax.swing.JOptionPane;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 
 /**
  * An extended <code>JInternalFrame</code> that provides modality in a
  * child/parent hierarchy
  * 
  * @author Dean
  */
 public class MDIDesktopFrame extends JInternalFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	protected JDesktopPane desktopPane;
 	protected JComponent parent;
 	protected MDIDesktopFrame childFrame;
 	protected JComponent focusOwner;
 	private boolean wasCloseable;
 
 	protected Object returnValue;
 
 	public MDIDesktopFrame(JComponent parent) {
 		this(parent, null);
 	}
 
 	public MDIDesktopFrame(JComponent parent, String title) {
 		this(parent, title, false);
 	}
 
 	public MDIDesktopFrame(JComponent parent, String title, boolean resizable) {
 		this(parent, title, resizable, false);
 	}
 
 	public MDIDesktopFrame(JComponent parent, String title, boolean resizable,
 			boolean closeable) {
 		this(parent, title, resizable, closeable, false);
 	}
 
 	public MDIDesktopFrame(JComponent parent, String title, boolean resizable,
 			boolean closeable, boolean maximizable) {
 		this(parent, title, resizable, closeable, maximizable, false);
 	}
 
 	/**
 	 * Designated constructor
 	 * 
 	 * @param parent
 	 * @param title
 	 * @param resizable
 	 * @param closeable
 	 * @param maximizable
 	 * @param iconifiable
 	 */
 	public MDIDesktopFrame(JComponent parent, String title, boolean resizable,
 			boolean closeable, boolean maximizable, boolean iconifiable) {
 		super(title, resizable, closeable, maximizable, iconifiable);
 		setParentFrame(parent);
 		setFocusTraversalKeysEnabled(false);
 
 		if (parent != null && parent instanceof MDIDesktopFrame) {
 			((MDIDesktopFrame) parent).setChildFrame(MDIDesktopFrame.this);
 		}
 
 		// Add glass pane
 		ModalInternalGlassPane glassPane = new ModalInternalGlassPane(this);
 		setGlassPane(glassPane);
 
 		// Add frame listeners
 		addFrameListener();
 
 		// Add frame veto listener
 		addFrameVetoListener();
 	}
 
 	/**
 	 * Get adopted
 	 * 
 	 * @param parent
 	 */
 	protected void setParentFrame(JComponent parent) {
 		desktopPane = JOptionPane.getDesktopPaneForComponent(parent);
 		this.parent = parent == null ? JOptionPane
 				.getDesktopPaneForComponent(parent) : parent;
 	}
 
 	/**
 	 * Get parent
 	 * 
 	 * @return parent
 	 */
 	public JComponent getParentFrame() {
 		return parent;
 	}
 
 	/**
 	 * Give me a child
 	 * 
 	 * @param childFrame
 	 */
 	public void setChildFrame(MDIDesktopFrame childFrame) {
 		this.childFrame = childFrame;
 	}
 
 	/**
 	 * Get me my child
 	 * 
 	 * @return childFrame
 	 */
 	public MDIDesktopFrame getChildFrame() {
 		return childFrame;
 	}
 
 	/**
 	 * Do I have any children?
 	 * 
 	 * @return hasChildFrame
 	 */
 	public boolean hasChildFrame() {
 		return (childFrame != null);
 	}
 
 	protected void addFrameVetoListener() {
 		addVetoableChangeListener(new VetoableChangeListener() {
 
 			public void vetoableChange(PropertyChangeEvent evt)
 					throws PropertyVetoException {
 				if (evt.getPropertyName().equals(
 						JInternalFrame.IS_SELECTED_PROPERTY)
 						&& evt.getNewValue().equals(Boolean.TRUE)) {
 					if (isIcon) {
 						setIcon(false);
 					}
 					if (hasChildFrame()) {
 						childFrame.setSelected(true);
 						if (childFrame.isIcon()) {
 							childFrame.setIcon(false);
 						}
 						throw new PropertyVetoException("no!", evt);
 					}
 				} else if (evt.getPropertyName().equals(
 						JInternalFrame.IS_ICON_PROPERTY)
 						&& evt.getNewValue().equals(Boolean.TRUE)) {
 					if (getParentFrame() instanceof MDIDesktopFrame) {
 						((MDIDesktopFrame) getParentFrame()).setIcon(true);
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * Method to control the display of the glasspane, dependent on the frame
 	 * being active or not
 	 */
 	protected void addFrameListener() {
 		addInternalFrameListener(new InternalFrameAdapter() {
 
 			@Override
 			public void internalFrameIconified(InternalFrameEvent e) {
 				if (hasChildFrame()) {
 					try {
 						childFrame.setIcon(true);
 					} catch (PropertyVetoException e1) {
 						// Do nothing
 					}
 				}
 			}
 
 			@Override
 			public void internalFrameDeiconified(InternalFrameEvent e) {
 				if (getParentFrame() instanceof MDIDesktopFrame) {
 					try {
						((MDIDesktopFrame) getParentFrame()).setIcon(false);
 					} catch (PropertyVetoException e1) {
 						// Do nothing
 					}
 				}
 			}
 
 			@Override
 			public void internalFrameActivated(InternalFrameEvent e) {
 				if (hasChildFrame()) {
 					getGlassPane().setVisible(true);
 					grabFocus();
 				} else {
 					getGlassPane().setVisible(false);
 				}
 			}
 
 			@Override
 			public void internalFrameDeactivated(InternalFrameEvent e) {
 				if (hasChildFrame()) {
 					getGlassPane().setVisible(true);
 					grabFocus();
 				} else {
 					getGlassPane().setVisible(false);
 				}
 			}
 
 			@Override
 			public void internalFrameOpened(InternalFrameEvent e) {
 				getGlassPane().setVisible(false);
 			}
 
 			@Override
 			public void internalFrameClosing(InternalFrameEvent e) {
 				if (parent != null && parent instanceof MDIDesktopFrame) {
 					((MDIDesktopFrame) parent).childClosing();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Method to handle child frame closing and make this frame available for
 	 * user input again with no glasspane visible
 	 */
 	protected void childClosing() {
 		setClosable(wasCloseable);
 		getGlassPane().setVisible(false);
 		if (focusOwner != null) {
 			java.awt.EventQueue.invokeLater(new Runnable() {
 
 				public void run() {
 					try {
 						moveToFront();
 						setSelected(true);
 						focusOwner.grabFocus();
 					} catch (PropertyVetoException ex) {
 						// This should only happen in the event that one child
 						// frame is closing and another is being opened
 						// immediately
 						if (hasChildFrame()) {
 							try {
 								childFrame.setSelected(true);
 							} catch (PropertyVetoException e) {
 								// Do nothing
 							}
 							childFrame.moveToFront();
 						}
 					}
 				}
 			});
 			focusOwner.grabFocus();
 		}
 		getGlassPane().setCursor(
 				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		setChildFrame(null);
 	}
 
 	/**
 	 * Method to handle child opening and becoming visible.
 	 */
 	protected void childOpening() {
 		// record the present focused component
 		wasCloseable = isClosable();
 		setClosable(false);
 		focusOwner = (JComponent) getFocusOwner();
 		grabFocus();
 		getGlassPane().setVisible(true);
 		getGlassPane()
 				.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	}
 
 	@Override
 	public void show() {
 		if (parent != null && parent instanceof MDIDesktopFrame) {
 			// Need to inform parent its about to lose its focus due
 			// to child opening
 			((MDIDesktopFrame) parent).childOpening();
 		}
 		super.show();
 	}
 
 	/**
 	 * Set the location of this frame relative to the parent frame
 	 * 
 	 * @param parentView
 	 */
 	public void setLocationRelativeTo(MDIDesktopFrame parentView) {
 		Point parentLocation = parentView.getLocation();
 
 		int height = parentView.getHeight();
 		int width = parentView.getWidth();
 
 		int newY = parentLocation.y + ((height / 2) - (getHeight() / 2));
 		int newX = parentLocation.x + ((width / 2) - (getWidth() / 2));
 
 		setLocation(newX, newY);
 	}
 
 	/**
 	 * Glass pane to overlay. Listens for mouse clicks and sets selected on
 	 * associated modal frame. Also if modal frame has no children make class
 	 * pane invisible
 	 * 
 	 * @author Dean
 	 */
 	private class ModalInternalGlassPane extends JComponent {
 
 		private static final long serialVersionUID = 1L;
 
 		private MDIDesktopFrame modalFrame;
 
 		public ModalInternalGlassPane(MDIDesktopFrame frame) {
 			modalFrame = frame;
 			addMouseListener(new MouseAdapter() {
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					if (!modalFrame.isSelected()) {
 						try {
 							modalFrame.setSelected(true);
 							if (!modalFrame.hasChildFrame()) {
 								setVisible(false);
 							}
 						} catch (PropertyVetoException e1) {
 							// e1.printStackTrace();
 						}
 					}
 				}
 			});
 		}
 
 		@Override
 		public void paint(Graphics g) {
 			super.paint(g);
 			g.setColor(new Color(255, 255, 255, 100));
 			g.fillRect(0, 0, getWidth(), getHeight());
 		}
 	}
 
 }
