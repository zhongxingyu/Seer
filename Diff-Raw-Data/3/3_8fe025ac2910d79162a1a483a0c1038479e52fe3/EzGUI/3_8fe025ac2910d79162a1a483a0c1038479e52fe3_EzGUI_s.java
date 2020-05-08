 package plugins.adufour.ezplug;
 
 import icy.gui.component.IcyLogo;
 import icy.gui.frame.IcyInternalFrame;
 import icy.gui.util.GuiUtil;
 import icy.system.thread.ThreadUtil;
 
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.RoundRectangle2D;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.border.Border;
 import javax.swing.filechooser.FileSystemView;
 import javax.swing.plaf.UIResource;
 import javax.swing.plaf.basic.BasicInternalFrameUI;
 
 import org.pushingpixels.substance.api.ComponentState;
 import org.pushingpixels.substance.api.SubstanceColorScheme;
 import org.pushingpixels.substance.api.SubstanceLookAndFeel;
 import org.pushingpixels.substance.api.skin.SkinChangeListener;
 import org.pushingpixels.substance.internal.ui.SubstanceDesktopIconUI;
 import org.pushingpixels.substance.internal.utils.SubstanceInternalFrameTitlePane;
 
 /**
  * Graphical interface generator for the EzPlug framework.<br>
  * When instantiated, each EzPlug creates an EzGUI to allow developers to add graphical components
  * in a fast and intuitive manner, allowing to build rich interfaces with advanced user interaction
  * without any knowledge in graphical interface programming.
  * 
  * @author Alexandre Dufour
  */
 public final class EzGUI extends IcyInternalFrame implements ActionListener, SkinChangeListener
 {
 	private static final long		serialVersionUID			= 1L;
 	
 	private static final int		LOGO_HEIGHT					= 32;
 	
 	private static final int		FONT_SIZE					= 16;
 	
 	private static final boolean	USE_SKIN_COLOR_SCHEME		= true;
 	
 	private EzPlug					ezPlug;
 	
 	private Thread					executionThread;
 	
 	private Color					logoTitleColor;
 	
 	private JPanel					jPanelParameters;
 	
 	private final List<Component>	components					= new ArrayList<Component>();
 	
 	private JPanel					jPanelBottom;
 	
 	private JPanel					jPanelButtons;
 	
 	private JButton					jButtonRun;
 	
 	private JButton					jButtonStop;
 	
 	private JButton					jButtonSaveParameters;
 	
 	private JButton					jButtonLoadParameters;
 	
 	private boolean					jButtonsParametersVisible	= true;
 	
 	private JProgressBar			jProgressBar;
 	
 	EzGUI(final EzPlug ezPlug)
 	{
 		super(ezPlug.getName(), true, true, false, true);
 		setUI(new EzInternalFrameUI(this));
 		setOpaque(false);
 		
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		
 		ThreadUtil.invokeNow(new Runnable()
 		{
 			
 			@Override
 			public void run()
 			{
 				getContentPane().setLayout(new BorderLayout(0, 0));
 				
 				EzGUI.this.setBorder(new Border()
 				{
 					@Override
 					public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
 					{
 						// leave the border transparent
 						// we're just creating a "hot-zone" to capture mouse resize events
 					}
 					
 					@Override
 					public Insets getBorderInsets(Component c)
 					{
 						return new Insets(2, 2, 2, 2);
 					}
 					
 					@Override
 					public boolean isBorderOpaque()
 					{
 						return false;
 					}
 				});
 				
 				EzGUI.this.ezPlug = ezPlug;
				EzGUI.this.executionThread = new Thread(ezPlug);
 				
 				jPanelParameters = new JPanel();
 				
 				jPanelParameters.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
 				
 				jPanelParameters.setLayout(new GridBagLayout());
 				
 				jPanelBottom = new JPanel(new GridLayout(2, 1));
 				
 				jPanelButtons = new JPanel(new GridLayout(1, 4));
 				jPanelBottom.add(jPanelButtons);
 				
 				jButtonRun = new JButton("Run");
 				jButtonRun.addActionListener(EzGUI.this);
 				jPanelButtons.add(jButtonRun);
 				
 				if (ezPlug instanceof EzStoppable)
 				{
 					jButtonStop = new JButton("Stop");
 					jButtonStop.setEnabled(false);
 					jButtonStop.addActionListener(EzGUI.this);
 					jPanelButtons.add(jButtonStop);
 				}
 				
 				jButtonSaveParameters = new JButton("Save");
 				jButtonSaveParameters.addActionListener(EzGUI.this);
 				jPanelButtons.add(jButtonSaveParameters);
 				
 				jButtonLoadParameters = new JButton("Load");
 				jButtonLoadParameters.addActionListener(EzGUI.this);
 				jPanelButtons.add(jButtonLoadParameters);
 				
 				jProgressBar = new JProgressBar();
 				jProgressBar.setString("Running...");
 				jPanelBottom.add(jProgressBar);
 				
 				getContentPane().add(jPanelParameters, BorderLayout.CENTER);
 				getContentPane().add(jPanelBottom, BorderLayout.SOUTH);
 				
 				pack();
 				
 				// look and feel change listener
 				icy.gui.util.LookAndFeelUtil.addSkinChangeListener(EzGUI.this);
 			}
 			
 		});
 		
 	}
 	
 	void addComponent(Component component)
 	{
 		components.add(component);
 	}
 	
 	void addEzComponent(EzComponent component, boolean isSingle)
 	{
 		// Special case #1: if the component is a variable, register it
 		if (component instanceof EzVar<?>)
 		{
 			ezPlug.registerVariable((EzVar<?>) component);
 		}
 		// Special case #2: if the component is a group, add its internal components recursively
 		else if (component instanceof EzGroup)
 		{
 			for (EzComponent groupComponent : (EzGroup) component)
 				addEzComponent(groupComponent, false);
 		}
 		
 		// set the parent UI of this component
 		if (component instanceof EzComponent)
 			component.setUI(this);
 		if (isSingle)
 			components.add(component);
 	}
 	
 	/**
 	 * Re-packs the user interface. This method should be called if one of the components was
 	 * changed either in size or visibility state
 	 * 
 	 * @param updateParametersPanel
 	 *            Set to true if the visibility of some parameters has been changed and the panel
 	 *            should be redrawn before re-packing the frame
 	 */
 	public void repack(boolean updateParametersPanel)
 	{
 		setVisible(false);
 		
 		if (updateParametersPanel)
 		{
 			jPanelParameters.removeAll();
 			
 			for (Component component : components)
 				if (component instanceof EzComponent)
 				{
 					((EzComponent) component).addToContainer(jPanelParameters);
 				}
 				else
 				{
 					GridBagLayout gridbag = (GridBagLayout) jPanelParameters.getLayout();
 					
 					GridBagConstraints gbc = new GridBagConstraints();
 					gbc.insets = new Insets(2, 5, 2, 5);
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.gridwidth = GridBagConstraints.REMAINDER;
 					// resize behavior
 					gbc.weightx = 1;
 					gbc.weighty = 1;
 					
 					component.setFocusable(false);
 					gridbag.setConstraints(component, gbc);
 					
 					jPanelParameters.add(component);
 				}
 		}
 		
 		pack();
 		
 		setVisible(true);
 	}
 	
 	/**
 	 * Highlights the plug's title bar
 	 * 
 	 * @param state
 	 */
 	public void setHighlightedState(boolean state)
 	{
 		JPanel jPanel = (JPanel) getContentPane().getComponent(0);
 		
 		for (Component component : jPanel.getComponents())
 		{
 			if (!(component instanceof IcyLogo))
 				continue;
 			
 			IcyLogo logo = (IcyLogo) component;
 			Component logoTitle = logo.getComponent(1);
 			
 			if (state)
 			{
 				logoTitleColor = logoTitle.getForeground();
 				logoTitle.setForeground(Color.cyan);
 			}
 			else
 			{
 				logoTitle.setForeground(logoTitleColor);
 				
 			}
 			logoTitle.repaint();
 		}
 	}
 	
 	/**
 	 * Sets the state of the "Run" button on the interface
 	 */
 	public void setRunButtonEnabled(final boolean runnable)
 	{
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				jButtonRun.setEnabled(runnable);
 			}
 		});
 	}
 	
 	/**
 	 * Sets the text of the run button
 	 */
 	public void setRunButtonText(final String text)
 	{
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				jButtonRun.setText(text);
 			}
 		});
 	}
 	
 	void setRunningState(final boolean running)
 	{
 		ThreadUtil.invokeNow(new Runnable()
 		{
 			public void run()
 			{
 				jButtonRun.setEnabled(!running);
 				if (ezPlug instanceof EzStoppable)
 					jButtonStop.setEnabled(running);
 				
 				// Note: Printing a string on a progress bar is not supported on Mac OS look'n'feel.
 				// jButtonRun.setText(running ? "Running..." : "Run");
 				jProgressBar.setString(running ? "Running..." : "");
 				jProgressBar.setStringPainted(running);
 				
 				jProgressBar.setValue(0);
 				jProgressBar.setIndeterminate(running);
 				
 				// Repack the frame to ensure good behavior of some components
 				repack(false);
 			}
 		});
 	}
 	
 	/**
 	 * Sets whether the action panel (buttons and progress bar) are visible or not
 	 * 
 	 * @param visible
 	 *            the new visibility state of the action panel
 	 */
 	public void setActionPanelVisible(final boolean visible)
 	{
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				jPanelBottom.setVisible(visible);
 			}
 		});
 	}
 	
 	public void setProgressBarVisible(final boolean visible)
 	{
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				jProgressBar.setVisible(visible);
 			}
 		});
 	}
 	
 	public void setParametersIOVisible(final boolean visible)
 	{
 		if (visible == jButtonsParametersVisible)
 			return;
 		
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				if (visible)
 				{
 					jPanelButtons.add(jButtonLoadParameters);
 					jPanelButtons.add(jButtonSaveParameters);
 				}
 				else
 				{
 					jPanelButtons.remove(jButtonLoadParameters);
 					jPanelButtons.remove(jButtonSaveParameters);
 				}
 			}
 		});
 		
 		jButtonsParametersVisible = visible;
 	}
 	
 	/**
 	 * Specifies the value of the progress bar.
 	 * 
 	 * @param value
 	 *            a value between 0 and 1, or -1 for indeterminate state (permanent animation)
 	 */
 	public void setProgressBarValue(final double value)
 	{
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				if (value == -1.0)
 				{
 					jProgressBar.setIndeterminate(true);
 				}
 				else
 				{
 					jProgressBar.setIndeterminate(false);
 					jProgressBar.setValue((int) (Math.max(0, Math.min(1.0, value)) * 100));
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Displays a text message on the run button (useful to indicate the state of the plug). Due to
 	 * limitations of Mac OS look'n'feel, the message is printed on the main button instead of the
 	 * progress bar
 	 * 
 	 * @param message
 	 */
 	public void setProgressBarMessage(final String message)
 	{
 		ThreadUtil.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				// jButtonRun.setText(message);
 				// NOTE the line below works on all look'n'feels except Mac OS
 				jProgressBar.setString(message);
 			}
 		});
 	}
 	
 	public void dispose()
 	{
 		setVisible(false);
 		
 		super.dispose();
 		
 		if (ezPlug == null)
 			return;
 		
 		if (executionThread != null && executionThread.isAlive())
 		{
 			// stop the execution if it was still running
 			if (ezPlug instanceof EzStoppable)
 			{
 				((EzStoppable) ezPlug).stopExecution();
 				try
 				{
 					Thread.sleep(100);
 				}
 				catch (InterruptedException e)
 				{
 				}
 			}
 			else
 			{
 				executionThread.interrupt();
 				System.err.println("Plug " + ezPlug.getName() + " was still running and has been interrupted");
 			}
 		}
 		
 		ezPlug.cleanFromUI();
 		
 		// dispose all components
 		
 		for (Component component : components)
 			if (component instanceof EzComponent)
 				((EzComponent) component).dispose(); // FIXME
 				
 		components.clear();
 		
 		jPanelParameters.removeAll();
 		repack(false);
 		
 		// remove all listeners
 		
 		jButtonRun.removeActionListener(this);
 		if (jButtonStop != null)
 			jButtonStop.removeActionListener(this);
 		jButtonLoadParameters.removeActionListener(this);
 		jButtonSaveParameters.removeActionListener(this);
 		
 		icy.gui.util.LookAndFeelUtil.removeSkinChangeListener(this);
 		
 		ezPlug = null;
 	}
 	
 	// ActionListener //
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getSource().equals(jButtonRun))
 		{
 			executionThread.start();
 		}
 		else if (e.getSource().equals(jButtonStop))
 		{
 			if (ezPlug instanceof EzStoppable)
 				((EzStoppable) ezPlug).stopExecution();
 		}
 		else if (e.getSource().equals(jButtonLoadParameters))
 		{
 			JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView());
 			
 			if (jfc.showOpenDialog(getContentPane()) != JFileChooser.APPROVE_OPTION)
 				return;
 			
 			ezPlug.loadParameters(jfc.getSelectedFile());
 			
 		}
 		else if (e.getSource().equals(jButtonSaveParameters))
 		{
 			JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView());
 			
 			if (jfc.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION)
 				return;
 			
 			ezPlug.saveParameters(jfc.getSelectedFile());
 		}
 		else
 		{
 			throw new UnsupportedOperationException("Action event not recognized for source " + e.getSource());
 		}
 	}
 	
 	/**
 	 * Custom title pane with elegant logo and title
 	 * 
 	 * @author Alexandre Dufour
 	 * 
 	 */
 	private static class EzTitlePane extends SubstanceInternalFrameTitlePane
 	{
 		private static final long		serialVersionUID	= 1L;
 		
 		private final JInternalFrame	internalFrame;
 		
 		private final int				titleWidth;
 		private final int				titleHeight;
 		
 		public EzTitlePane(JInternalFrame f)
 		{
 			super(f);
 			this.internalFrame = f;
 			setFont(getFont().deriveFont(Font.BOLD + Font.ITALIC, FONT_SIZE));
 			
 			FontMetrics m = getFontMetrics(getFont());
 			
 			titleWidth = m.stringWidth(f.getTitle());
 			titleHeight = m.getHeight() - 6;
 			
 			setPreferredSize(new Dimension(titleWidth + 100, LOGO_HEIGHT));
 		}
 		
 		@Override
 		protected LayoutManager createLayout()
 		{
 			return new EzTitlePaneLayout();
 		}
 		
 		@Override
 		public void paintComponent(Graphics g)
 		{
 			Graphics2D graphics = (Graphics2D) g.create();
 			
 			int width = this.getWidth();
 			int height = this.getHeight() + 0;
 			
 			paintbg(getWidth(), getHeight(), g);
 			
 			if (USE_SKIN_COLOR_SCHEME)
 			{
 				SubstanceColorScheme colors = SubstanceLookAndFeel.getCurrentSkin().getColorScheme(new JButton(), ComponentState.PRESSED_SELECTED);
 				graphics.setColor(colors.isDark() ? Color.white : colors.getUltraDarkColor().darker().darker());
 			}
 			else
 			{
 				graphics.setColor(Color.white);
 			}
 			
 			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			graphics.drawString(internalFrame.getTitle(), (width - titleWidth) / 2, (height + titleHeight) / 2);
 			graphics.dispose();
 		}
 		
 		/**
 		 * Modified version of the {@link GuiUtil#paintBackGround(int, int, Graphics)} method for
 		 * the purpose of EzGUI. Changes include:<br>
 		 * <ul>
 		 * <li>Shade and lighting effects adjusted</li>
 		 * <li>Border outline removed</li>
 		 * <li>Bottom corners of the rounded rectangle masked to better stick to the rest of the
 		 * frame</li>
 		 * </ul>
 		 * 
 		 * @param width
 		 * @param height
 		 * @param g
 		 */
 		private void paintbg(int width, int height, Graphics g)
 		{
 			final Graphics2D g2 = (Graphics2D) g.create();
 			
 			float ray = Math.max(width, height) * 0.05f;
 			float finalRay = Math.min(ray * 2, 20);
 			
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			
 			Color brightColor, darkColor;
 			
 			if (USE_SKIN_COLOR_SCHEME)
 			{
 				SubstanceColorScheme colors = SubstanceLookAndFeel.getCurrentSkin().getColorScheme(new JButton(), ComponentState.PRESSED_SELECTED);
 				brightColor = colors.getUltraLightColor().brighter();
 				darkColor = colors.getDarkColor();
 			}
 			else
 			{
 				brightColor = new Color(72, 72, 72);
 				darkColor = new Color(4, 4, 4);
 			}
 			
 			// Fill a rounded rectangle with gradient paint (main title bar)
 			final RoundRectangle2D roundRect = new RoundRectangle2D.Double(0, 0, width, height, finalRay, finalRay);
 			g2.setPaint(new GradientPaint(0, 0, brightColor, 0, height, darkColor));
 			g2.fill(roundRect);
 			
 			// Fill a black rectangle to mask the bottom corners of the rounded rectangle
 			g2.fillRect(0, height / 2, width, height / 2);
 			
 			// add a bright oval portion to simulate a glass reflection
 			g2.setPaint(new GradientPaint(0, 0, brightColor, 0, height * 2, darkColor));
 			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
 			g2.fillOval(-width + (width / 2), height / 3, width * 2, height * 3);
 			
 			g2.dispose();
 		}
 		
 		/**
 		 * Layout manager for this title pane. Patched version of SubstanceTitlePaneLayout to adjust
 		 * the buttons position (they stick tighter in the upper right-hand corner)
 		 * 
 		 * @author Kirill Grouchnikov
 		 * @author Alexandre Dufour
 		 */
 		protected class EzTitlePaneLayout extends SubstanceInternalFrameTitlePane.SubstanceTitlePaneLayout
 		{
 			@Override
 			public void layoutContainer(Container c)
 			{
 				boolean leftToRight = internalFrame.getComponentOrientation().isLeftToRight();
 				
 				int w = getWidth();
 				int x = leftToRight ? w : 0;
 				int y = 2;
 				int spacing;
 				
 				// assumes all buttons have the same dimensions
 				// these dimensions include the borders
 				int buttonHeight = closeButton.getIcon().getIconHeight();
 				int buttonWidth = closeButton.getIcon().getIconWidth();
 				
 				// old version (patched by Alexandre Dufour)
 				// y = (getHeight() - buttonHeight) / 2;
 				y = 4;
 				
 				if (internalFrame.isClosable())
 				{
 					spacing = 4;
 					x += leftToRight ? -spacing - buttonWidth : spacing;
 					closeButton.setBounds(x, y, buttonWidth, buttonHeight);
 					if (!leftToRight)
 						x += buttonWidth;
 				}
 				
 				if (internalFrame.isMaximizable())
 				{
 					spacing = internalFrame.isClosable() ? 2 : 4;
 					x += leftToRight ? -spacing - buttonWidth : spacing;
 					maxButton.setBounds(x, y, buttonWidth, buttonHeight);
 					if (!leftToRight)
 						x += buttonWidth;
 				}
 				
 				if (internalFrame.isIconifiable())
 				{
 					spacing = internalFrame.isMaximizable() ? 2 : (internalFrame.isClosable() ? 2 : 4);
 					x += leftToRight ? -spacing - buttonWidth : spacing;
 					iconButton.setBounds(x, y, buttonWidth, buttonHeight);
 					if (!leftToRight)
 						x += buttonWidth;
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * This class is a fork of SubstanceInternalFrameUI because the "titlePane" field of the
 	 * original class has no set method and is declared private, yielding a NullPointerException
 	 * when closing the window (due to a confusion between SubstanceInternalFrameUI.titlePane and
 	 * BasicInternalFrameUI.northPane)
 	 * 
 	 * @author Alexandre Dufour
 	 * 
 	 */
 	private static class EzInternalFrameUI extends BasicInternalFrameUI
 	{
 		private EzTitlePane						titlePane;
 		
 		private final PropertyChangeListener	substancePropertyListener;
 		
 		public EzInternalFrameUI(JInternalFrame b)
 		{
 			super(b);
 			
 			substancePropertyListener = new PropertyChangeListener()
 			{
 				public void propertyChange(PropertyChangeEvent evt)
 				{
 					if (JInternalFrame.IS_CLOSED_PROPERTY.equals(evt.getPropertyName()))
 					{
 						titlePane.uninstall();
 						if (frame != null)
 						{
 							JDesktopIcon jdi = frame.getDesktopIcon();
 							SubstanceDesktopIconUI ui = (SubstanceDesktopIconUI) jdi.getUI();
 							ui.uninstallUI(jdi);
 						}
 					}
 					
 					if ("background".equals(evt.getPropertyName()))
 					{
 						Color newBackgr = (Color) evt.getNewValue();
 						if (!(newBackgr instanceof UIResource))
 						{
 							titlePane.setBackground(newBackgr);
 							frame.getDesktopIcon().setBackground(newBackgr);
 						}
 					}
 				}
 			};
 		}
 		
 		@Override
 		protected JComponent createNorthPane(JInternalFrame internalFrame)
 		{
 			this.titlePane = new EzTitlePane(internalFrame);
 			this.titlePane.setToolTipText(null);
 			return titlePane;
 		}
 		
 		protected void installListeners()
 		{
 			super.installListeners();
 			this.frame.addPropertyChangeListener(substancePropertyListener);
 		}
 		
 		protected void uninstallComponents()
 		{
 			this.titlePane.uninstall();
 			super.uninstallComponents();
 		}
 		
 		@Override
 		protected void uninstallListeners()
 		{
 			super.uninstallListeners();
 			this.frame.removePropertyChangeListener(substancePropertyListener);
 		}
 	}
 	
 	@Override
 	public void skinChanged()
 	{
 		setUI(new EzInternalFrameUI(EzGUI.this));
 	}
 }
