 package net.coobird.paint.driver;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSplitPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import net.coobird.paint.BlendingMode;
 import net.coobird.paint.application.ApplicationUtils;
 import net.coobird.paint.application.BrushListCellRenderer;
 import net.coobird.paint.application.ImageLayerListCellRenderer;
 import net.coobird.paint.brush.Brush;
 import net.coobird.paint.brush.BrushController;
 import net.coobird.paint.brush.BrushRenderProgressListener;
 import net.coobird.paint.brush.RandomSplashBrush;
 import net.coobird.paint.brush.RegularCircularBrush;
 import net.coobird.paint.brush.RegularEllipticalBrush;
 import net.coobird.paint.brush.RegularEllipticalEraser;
 import net.coobird.paint.brush.SolidCircularBrush;
 import net.coobird.paint.brush.StagedBrush;
 import net.coobird.paint.filter.ImageFilter;
 import net.coobird.paint.filter.ImageFilterThreadingWrapper;
 import net.coobird.paint.filter.MatrixImageFilter;
 import net.coobird.paint.filter.RepeatableMatrixFilter;
 import net.coobird.paint.filter.ResizeFilter;
 import net.coobird.paint.image.Canvas;
 import net.coobird.paint.image.ClippableImageRenderer;
 import net.coobird.paint.image.ImageLayer;
 import net.coobird.paint.image.ImageLayerUtils;
 import net.coobird.paint.image.PartialImageRenderer;
 import net.coobird.paint.io.FormatManager;
 import net.coobird.paint.io.ImageInput;
 import net.coobird.paint.io.ImageInputOutputException;
 import net.coobird.paint.io.ImageOutput;
 
 public class DemoApp2
 {
 	class ActionMenuItem extends JMenuItem implements ActionListener
 	{
 		public ActionMenuItem(String s)
 		{
 			addActionListener(this);
 			setText(s);
 		}
 		
 		public void actionPerformed(ActionEvent e) {};
 	}
 	
 
 	private void makeGUI()
 	{
 		final JFrame f = new JFrame("Paint Dot Jar Demonstration 2");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.getContentPane().setLayout(new BorderLayout());
 		
 		final JPanel statusBar = new JPanel(new BorderLayout());
 		final JLabel statusLabel = new JLabel("Ready.");
 		final JProgressBar progressBar = new JProgressBar();
 		progressBar.setStringPainted(true);
 		
 		statusBar.add(statusLabel, BorderLayout.WEST);
 		statusBar.add(progressBar, BorderLayout.EAST);
 		
 		f.getContentPane().add(statusBar, BorderLayout.SOUTH);
 		
 		ApplicationUtils.setMainComponent(f);
 		
 		JPanel listPanels = new JPanel(new GridLayout(0, 1));
 		
 		final JList brushList = new JList();
 		brushList.setCellRenderer(new BrushListCellRenderer());
 		final DefaultListModel brushListModel = new DefaultListModel();
 		brushList.setModel(brushListModel);
 		brushListModel.addElement(new RegularCircularBrush(null, 80, new Color(0,0,255,32)));
 		brushListModel.addElement(new SolidCircularBrush(null, 80, Color.red));
 		brushListModel.addElement(new SolidCircularBrush(null, 40, Color.orange));
 		brushListModel.addElement(new SolidCircularBrush(null, 120, Color.white));
 		brushListModel.addElement(new RegularCircularBrush("Small black brush", 40, new Color(0,0,0,32)));
 		brushListModel.addElement(new RegularCircularBrush("Thin black brush", 80, new Color(0,0,0,4)));
 		brushListModel.addElement(new RegularCircularBrush("Emerald green 40 px brush", 40, new Color(0,255,96,32)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 60, Math.PI * 0.67, 0.3, new Color(0,0,128,4)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 60, Math.PI * 0.67, 0.5, new Color(0,0,128,4)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 60, Math.PI * 0.8, 0.8, new Color(0,0,128,4)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 120, Math.PI * 0.4, 0.8, new Color(0,0,0,16)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 240, Math.PI * 0.4, 0.8, new Color(255,0,0,16)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 240, Math.PI * 0.2, 0.6, new Color(255,0,0,4)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 40, Math.PI * 0.9, 0.2, new Color(0,255,128,4)));
 		brushListModel.addElement(new RegularEllipticalBrush(null, 60, Math.PI * 0.5, 0.4, new Color(0,0,128,16)));
 		brushListModel.addElement(new RegularEllipticalEraser(null, 40, 0, 1, 1f));
 		brushListModel.addElement(new RegularEllipticalEraser(null, 80, 0, 1, 1f));
 		brushListModel.addElement(new RegularEllipticalEraser(null, 80, Math.PI * 0.25, 0.5, 0.5f));
 		brushListModel.addElement(new SolidCircularBrush(null, 4));
 		brushListModel.addElement(new RegularCircularBrush(null, 4, Color.black));
 		brushListModel.addElement(new RegularEllipticalEraser(null, 4, 0, 1, 1f));
 		brushListModel.addElement(new StagedBrush(null, 10, Color.black));
 		brushListModel.addElement(new RandomSplashBrush(null, 400, 50, Color.black));
 		
 		final JPopupMenu brushPopupMenu = new JPopupMenu();
 		brushPopupMenu.add(new ActionMenuItem("Change Color...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				Brush b = (Brush)brushList.getSelectedValue();
 				
 				if (b == null)
 				{
 					return;
 				}
 				
 				Color c = JColorChooser.showDialog(f, "Choose Color...", b.getColor());
 				if (c == null)
 				{
 					return;
 				}
 				
 				b.setColor(c);
 				brushList.repaint();
 			}
 		});
 		brushPopupMenu.add(new ActionMenuItem("Change Size...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				Brush b = (Brush)brushList.getSelectedValue();
 				
 				String s = JOptionPane.showInputDialog(f, "Size:");
 				
 				b.setSize(Integer.parseInt(s));
 
 				brushList.repaint();
 			}
 		});
 		brushPopupMenu.add(new ActionMenuItem("Properties...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				Brush b = (Brush)brushList.getSelectedValue();
 				
 				JPanel p = new JPanel(new BorderLayout());
 				JSlider brushSize = new JSlider(1, 200, b.getSize());
 				JSlider alpha = new JSlider(0, 100, Math.round(b.getAlpha() * 100));
 				JCheckBox rotatable = new JCheckBox("Rotatable brush");
 				rotatable.setSelected(b.isRotatable());
 				JColorChooser color = new JColorChooser(b.getColor());
 				
 				JPanel ip = new JPanel(new GridLayout(0,1));
 				
 				ip.add(new JLabel("Brush size:"));
 				ip.add(brushSize);
 				ip.add(new JLabel("Alpha:"));
 				ip.add(alpha);
 				ip.add(rotatable);
 				
 				p.add(ip, BorderLayout.CENTER);
 				p.add(color, BorderLayout.SOUTH);
 				p.validate();
 				
 				int option = JOptionPane.showConfirmDialog(f, p, "Properties", JOptionPane.OK_CANCEL_OPTION);
 				
 				if (option == JOptionPane.CANCEL_OPTION)
 				{
 					return;
 				}
 				
 				b.setSize(brushSize.getValue());
 				b.setAlpha(((float)alpha.getValue()/100f));
 				b.setColor(color.getColor());
 				b.setRotatable(rotatable.isSelected());
 				
 				brushList.repaint();
 			}
 		});
 		
 		brushList.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e)
 			{
 				if (e.isPopupTrigger())
 				{
 					brushPopupMenu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 			public void mouseReleased(MouseEvent e)
 			{
 				if (e.isPopupTrigger())
 				{
 					brushPopupMenu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 
 		final JScrollPane brushListSp = new JScrollPane(brushList);
 		listPanels.add(brushListSp);
 		
 		final BrushController bc = new BrushController();
 		
 		final JList ilList = new JList();
 		ilList.setCellRenderer(new ImageLayerListCellRenderer());
 		final DefaultListModel ilListModel = new DefaultListModel();
 		ilList.setModel(ilListModel);
 		
 		final int SIZE = 800;
 		
 		final CanvasHolder ch = new CanvasHolder();
 		Canvas c = new Canvas(SIZE, SIZE);
 		c.addLayer();
 		c.addLayer();
 		c.addLayer();
 		ch.setCanvas(c);
 
 		for (ImageLayer il : c.getLayers())
 		{
 			ilListModel.addElement(il);
 		}
 		
 		final JScrollPane ilListSp = new JScrollPane(ilList);
 		listPanels.add(ilListSp);
 		
 //		final ImageRenderer renderer = ImageRendererFactory.getInstance();
 		final PartialImageRenderer renderer = new ClippableImageRenderer();
 //		final ProgressiveImageRenderer renderer = new ProgressiveImageRenderer();
 		
 		final CanvasViewPanel p = new CanvasViewPanel(renderer, c);
 		
 		class DrawEventHandler extends MouseAdapter
 			implements BrushRenderProgressListener
 		{
 			private int maxSteps = 0;
 			
 			public void mouseDragged(MouseEvent e)
 			{
 				ImageLayer il = (ImageLayer)ilList.getSelectedValue();
 				Brush b = (Brush)brushList.getSelectedValue();
 				
 				if (il == null || b == null)
 				{
 					return;
 				}
 				
 				int offsetX = il.getX();
 				int offsetY = il.getY();
 				
 				double zoom = ch.getCanvas().getZoom();
 				int sx = (int)Math.round((e.getX() - offsetX) / zoom); 
 				int sy = (int)Math.round((e.getY() - offsetY) / zoom); 
 				
 				bc.drawBrush(
 						il,
 						b,
 						sx,
 						sy
 				);
 				
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run()
 					{
 						p.repaint();
 					}
 				});
 			}
 			
 			public void mouseReleased(MouseEvent e)
 			{
 				if (ilList.getSelectedValue() == null)
 				{
 					return;
 				}
 				
 				((ImageLayer)ilList.getSelectedValue()).update();
 				ilList.repaint();
 				
 				bc.releaseBrush();
 				
 				p.repaint();
 			}
 
 			public void mousePressed(MouseEvent e)
 			{
 				ImageLayer il = (ImageLayer)ilList.getSelectedValue();
 				Brush b = (Brush)brushList.getSelectedValue();
 				
 				if (il == null || b == null)
 				{
 					return;
 				}
 				
 				int offsetX = il.getX();
 				int offsetY = il.getY();
 				
 				double zoom = ch.getCanvas().getZoom();
 				int sx = (int)Math.round((e.getX() - offsetX) / zoom); 
 				int sy = (int)Math.round((e.getY() - offsetY) / zoom); 
 				
 				bc.drawBrush(
 						il,
 						b,
 						sx,
 						sy
 				);
 			}
 
 			@Override
 			public void drawComplete()
 			{
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run()
 					{
 						p.repaint();
 						
 						((ImageLayer)ilList.getSelectedValue()).update();
 						ilList.repaint();
 						
 						statusLabel.setText("Done.");
 						progressBar.setValue(0);
 						maxSteps = 0;
 					}
 				});
 			}
 
 			@Override
 			public void drawProgress(final int stepsLeft)
 			{
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run()
 					{
 						if (stepsLeft > maxSteps)
 						{
 							maxSteps = stepsLeft;
 							progressBar.setMaximum(maxSteps);
 						}
 						
 						statusLabel.setText("Rendering brush...");
 						progressBar.setValue(maxSteps - stepsLeft);
 					}
 				});
 			}
 		};
 		
 		DrawEventHandler ma = new DrawEventHandler();
 		bc.addBrushRenderProgressListener(ma);
 		
 		p.addMouseListener(ma);
 		p.addMouseMotionListener(ma);
 
 		ilList.setDropTarget(new DropTarget()
 		{
 			/* (non-Javadoc)
 			 * @see java.awt.dnd.DropTarget#drop(java.awt.dnd.DropTargetDropEvent)
 			 */
 			@Override
 			public synchronized void drop(DropTargetDropEvent e)
 			{
 				super.drop(e);
 				Point pt = e.getLocation();
 				int fromIndex = ilList.getSelectedIndex();
 				int toIndex = ilList.locationToIndex(pt);
 				
 				ch.getCanvas().moveLayer(fromIndex, toIndex);
 
 				ilListModel.removeAllElements();
 				for (ImageLayer il : ch.getCanvas().getLayers())
 				{
 					ilListModel.addElement(il);
 				}
 				p.repaint();
 			}
 		});
 		ilList.setDragEnabled(true);
 		
 		final JPopupMenu ilPopupMenu = new JPopupMenu();
 		
 		ilPopupMenu.add(new ActionMenuItem("Change Name...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = JOptionPane.showInputDialog(f, "Name:");
 				((ImageLayer)ilList.getSelectedValue()).setCaption(s);
 				p.repaint();
 			}
 		});
 		ilPopupMenu.add(new ActionMenuItem("Change Alpha...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = JOptionPane.showInputDialog(f, "Alpha:");
 				((ImageLayer)ilList.getSelectedValue()).setAlpha(Float.parseFloat(s));
 				p.repaint();
 			}
 		});
 		ilPopupMenu.add(new ActionMenuItem("Change Location...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = JOptionPane.showInputDialog(f, "Location (x,y):");
 				String[] t = s.split(",");
 				int x = Integer.parseInt(t[0]);
 				int y = Integer.parseInt(t[1]);
 				
 				((ImageLayer)ilList.getSelectedValue()).setLocation(x, y);
 				p.repaint();
 			}
 		});
 		ilPopupMenu.add(new ActionMenuItem("Change Mode...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageLayer il = (ImageLayer)ilList.getSelectedValue();
 				JComboBox cb = new JComboBox(BlendingMode.Layer.values());
 				cb.setSelectedItem(il.getMode());
 				JOptionPane.showMessageDialog(f, cb);
 				il.setMode((BlendingMode.Layer)cb.getSelectedItem());
 				p.repaint();
 			}
 		});
 		ilPopupMenu.addSeparator();
 
 		ilPopupMenu.add(new ActionMenuItem("New Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageLayer il = new ImageLayer(
 						ch.getCanvas().getWidth(),
 						ch.getCanvas().getHeight()
 				);
 				il.setCaption(ch.getCanvas().getNextLayerName());
 				
 				ch.getCanvas().addLayer(il);
 				
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 		
 		ilPopupMenu.add(new ActionMenuItem("Delete Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageLayer il = (ImageLayer)ilList.getSelectedValue();
 				ch.getCanvas().removeLayer(il);
 
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 		
 		ilPopupMenu.add(new ActionMenuItem("Merge Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				int index = ilList.getSelectedIndex();
 				if (index >= ilListModel.size() - 1)
 				{
 					return;
 				}
 				
 				ImageLayer topIl = (ImageLayer)ilListModel.get(index);
 				ImageLayer bottomIl = (ImageLayer)ilListModel.get(index + 1);
 				
 				ch.getCanvas().removeLayer(topIl);
 				ch.getCanvas().removeLayer(bottomIl);
 				
 				ch.getCanvas().addLayer(ImageLayerUtils.mergeLayers(topIl, bottomIl), index);
 				
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 		
 		ilList.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e)
 			{
 				if (e.isPopupTrigger())
 				{
 					ilPopupMenu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 			public void mouseReleased(MouseEvent e)
 			{
 				if (e.isPopupTrigger())
 				{
 					ilPopupMenu.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 		
 		final JMenuBar menubar = new JMenuBar();
 		final JMenu fileMenu = new JMenu("File");
 		final JMenu layerMenu = new JMenu("Layer");
 		final JMenu filterMenu = new JMenu("Filter");
 		
 		fileMenu.add(new ActionMenuItem("New...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				System.out.println("new");
 				
 				String s = JOptionPane.showInputDialog(f, "Enter dimensions in (w,h):");
 				
 				if (s == null)
 				{
 					return;
 				}
 				
 				int width = SIZE;
 				int height = SIZE;
 				
 				String[] t = s.split(",");
 				
 				try
 				{
 					width = Integer.parseInt(t[0]);
 					height = Integer.parseInt(t[1]);
 				}
 				catch (Exception ex)
 				{
 					JOptionPane.showMessageDialog(f, "Invalid dimensions.\n" + ex);
 				}
 				
 				Canvas c = new Canvas(width, height);
 				c.addLayer(new ImageLayer(width, height));
 				ch.setCanvas(c);
 				
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 
 		fileMenu.addSeparator();
 		
 		fileMenu.add(new ActionMenuItem("Open...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser fc = new JFileChooser();
 				for (FileFilter filter : FormatManager.getInputFileFilters())
 				{
 					fc.addChoosableFileFilter(filter);
 				}
 				
 				while (true)
 				{
 					int option = fc.showOpenDialog(f);
 					
 					if (option != JFileChooser.APPROVE_OPTION)
 						return;
 					
 					File inFile = fc.getSelectedFile();
 					
 					ImageInput input = FormatManager.getImageInput(inFile); 
 					
 					if (input != null)
 					{
 						try
 						{
 							ch.setCanvas(input.read(inFile));
 						}
 						catch (ImageInputOutputException e1)
 						{
 							ApplicationUtils.showExceptionMessage(e1);
 						}
 						updateGUI(ilListModel, ch, p);
 						return;
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(f, "Could not open file: " + inFile);
 					}
 				}
 			}
 		});
 
 		fileMenu.add(new ActionMenuItem("Save As...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser fc = new JFileChooser();
 				for (FileFilter filter : FormatManager.getOutputFileFilters())
 				{
 					fc.addChoosableFileFilter(filter);
 				}
 				fc.setFileFilter(fc.getAcceptAllFileFilter());
 				
 				while (true)
 				{
 					int option = fc.showSaveDialog(f);
 					
 					if (option != JFileChooser.APPROVE_OPTION)
 					{
 						return;
 					}
 					
 					File outFile = fc.getSelectedFile();
 					ImageOutput output = FormatManager.getImageOutput(outFile);
 					
 					// fc.getFileFilter();
 					
 					if (output != null)
 					{
 						try
 						{
 							output.write(ch.getCanvas(), outFile);
 						}
 						catch (ImageInputOutputException e1)
 						{
 							ApplicationUtils.showExceptionMessage(e1);
 						}
 						return;
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(f, "Could not write file" + outFile);
 					}
 				}
 			}
 		});
 		
 		fileMenu.addSeparator();
 		
 		fileMenu.add(new ActionMenuItem("Import Layer...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser fc = new JFileChooser();
 				for (FileFilter filter : FormatManager.getInputFileFilters())
 				{
 					fc.addChoosableFileFilter(filter);
 				}
 				
 				while (true)
 				{
 					int option = fc.showOpenDialog(f);
 					
 					if (option != JFileChooser.APPROVE_OPTION)
 						return;
 					
 					File inFile = fc.getSelectedFile();
 					
 					ImageInput input = FormatManager.getImageInput(inFile);
 					
 					if (input != null)
 					{
 						
 						Canvas c;
 						try
 						{
 							c = input.read(inFile);
 
 							for (ImageLayer layer : c.getLayers())
 							{
 								ch.getCanvas().addLayer(layer);
 							}
 			
 							updateGUI(ilListModel, ch, p);
 						}
 						catch (ImageInputOutputException e1)
 						{
 							ApplicationUtils.showExceptionMessage(e1);
 						}
 						return;
 					}
 					else
 					{
 						JOptionPane.showMessageDialog(f, "ZCould not open file: " + inFile);
 					}
 				}
 			}
 		});
 		
 		fileMenu.addSeparator();
 
 		fileMenu.add(new ActionMenuItem("Exit") {
 			public void actionPerformed(ActionEvent arg0)
 			{
 				f.setVisible(false);
 			}
 		});
 		
 		////////////////
 
 		layerMenu.add(new ActionMenuItem("New Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ch.getCanvas().addLayer(new ImageLayer(ch.getCanvas().getWidth(), ch.getCanvas().getHeight()));
 				
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 		
 		layerMenu.add(new ActionMenuItem("Delete Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				if (ilList.getSelectedIndex() == -1)
 				{
 					return;
 				}
 				
 				ImageLayer il = (ImageLayer)ilList.getSelectedValue();
 				ch.getCanvas().removeLayer(il);
 
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 		
 		layerMenu.add(new ActionMenuItem("Merge Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				if (ilList.getSelectedIndex() == -1)
 				{
 					return;
 				}
 				
 				int index = ilList.getSelectedIndex();
 				if (index >= ilListModel.size() - 1)
 				{
 					return;
 				}
 				
 				ImageLayer topIl = (ImageLayer)ilListModel.get(index);
 				ImageLayer bottomIl = (ImageLayer)ilListModel.get(index + 1);
 				
 				ch.getCanvas().removeLayer(topIl);
 				ch.getCanvas().removeLayer(bottomIl);
 				
 				ch.getCanvas().addLayer(ImageLayerUtils.mergeLayers(topIl, bottomIl), index);
 				
 				updateGUI(ilListModel, ch, p);
 			}
 		});
 		
 		layerMenu.addSeparator();
 		
 		layerMenu.add(new ActionMenuItem("Size Canvas to Largest Layer") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ch.getCanvas().pack();
 
 				ilList.repaint();
 				p.repaint();
 			}
 		});
 
 		layerMenu.add(new ActionMenuItem("Grow Layer to Canvas") {
 			public void actionPerformed(ActionEvent e)
 			{
 			}
 		});
 		
 		layerMenu.add(new ActionMenuItem("Resize Canvas...") {
 			public void actionPerformed(ActionEvent e)
 			{
 			}
 		});
 		
 		////////////////
 		
 		class FilterWithProgressBar
 		{
 			final ImageFilter filter;
 			final JProgressBar pb;
 			final JDialog d;
 			
 			public FilterWithProgressBar(final ImageFilter filter)
 			{
 				pb = new JProgressBar();
 				d = new JDialog(f);
 				JPanel panel = new JPanel(new BorderLayout());
 				pb.setValue(0);
 				pb.setStringPainted(true);
 				panel.add(new JLabel("Processing filter..."), BorderLayout.NORTH);
 				panel.add(pb, BorderLayout.CENTER);
 				d.getContentPane().add(panel);
 				d.pack();
 				d.setVisible(true);
 				
 				this.filter = filter;
 			}
 			
 			public void run()
 			{
 				new Thread(new Runnable() {
 					public void run()
 					{
 						long st = System.currentTimeMillis();
 						
 						final int inc = 100 / ch.getCanvas().getLayers().size();
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							il.setImage(filter.processImage(il.getImage()));
 							
 							try
 							{
 								SwingUtilities.invokeAndWait(new Runnable() {
 									public void run()
 									{
 										pb.setValue(pb.getValue() + inc);	
 									}
 								});
 							}
 							catch (InterruptedException e)
 							{
 								e.printStackTrace();
 							}
 							catch (InvocationTargetException e)
 							{
 								e.printStackTrace();
 							}
 						}
 						
 						long tp = System.currentTimeMillis() - st;
 						System.out.println("complete in: " + tp);
 						
 						pb.setValue(100);
 						d.dispose();
 						p.repaint();
 						ilList.repaint();
 					}
 				}).start();
 			}
 		}
 	
 		filterMenu.add(new ActionMenuItem("Blur") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new MatrixImageFilter(3, 3, new float[]{
 						0.0f,	0.1f,	 0.0f,
 						0.1f,	0.6f,	 0.1f,
 						0.0f,	0.1f,	 0.0f
 				});
 				
 				new FilterWithProgressBar(filter).run();						
 			}
 		});
 
 		filterMenu.add(new ActionMenuItem("Blur More") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new RepeatableMatrixFilter(
 						"Blur More",
 						3,
 						3,
 						10,
 						new float[]{
 						0.0f,	0.1f,	 0.0f,
 						0.1f,	0.6f,	 0.1f,
 						0.0f,	0.1f,	 0.0f
 				});
 				
 				new FilterWithProgressBar(filter).run();						
 			}
 		});
 
 		filterMenu.add(new ActionMenuItem("Blur More Concurrent") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new ImageFilterThreadingWrapper(
 						new RepeatableMatrixFilter(
 							3,
 							3,
 							10,
 							new float[] {
 									0.0f,	0.1f,	 0.0f,
 									0.1f,	0.6f,	 0.1f,
 									0.0f,	0.1f,	 0.0f
 							}
 						)
 				);
 				
 				new FilterWithProgressBar(filter).run();
 			}
 		});
 		
 		filterMenu.add(new ActionMenuItem("Blur More Concurrent (1/9 all)") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new ImageFilterThreadingWrapper(
 						new RepeatableMatrixFilter(
 							3,
 							3,
 							10,
 							new float[] {
 									1f/9f,	1f/9f,	 1f/9f,
 									1f/9f,	1f/9f,	 1f/9f,
 									1f/9f,	1f/9f,	 1f/9f
 							}
 						)
 				);
 				
 				new FilterWithProgressBar(filter).run();
 			}
 		});
 		
 		filterMenu.add(new ActionMenuItem("Blur More Concurrent 7x7") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new ImageFilterThreadingWrapper(
 						new RepeatableMatrixFilter(
 								7,
 								7,
 								1,
 								new float[] {
 										0.0f,	0.0f,	0.0f,	 0.05f,	0.0f,	0.0f,	0.0f,
 										0.0f,	0.0f,	0.0f,	 0.05f,	0.0f,	0.0f,	0.0f,
 										0.0f,	0.0f,	0.0f,	 0.1f,	0.0f,	0.0f,	0.0f,
 										0.05f,	0.05f,	0.1f,	 0.2f,	0.1f,	0.05f,	0.05f,
 										0.0f,	0.0f,	0.0f,	 0.1f,	0.0f,	0.0f,	0.0f,
 										0.0f,	0.0f,	0.0f,	 0.05f,	0.0f,	0.0f,	0.0f,
 										0.0f,	0.0f,	0.0f,	 0.05f,	0.0f,	0.0f,	0.0f
 								}
 						),
 						10
 				);
 				
 				new FilterWithProgressBar(filter).run();
 			}
 		});
 
 		filterMenu.add(new ActionMenuItem("No Effect") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new MatrixImageFilter(
 						"No Effect",
 						3, 3, new float[]{
 						0.0f,	0.0f,	 0.0f,
 						0.0f,	1.0f,	 0.0f,
 						0.0f,	0.0f,	 0.0f
 				});
 				
 				new FilterWithProgressBar(filter).run();						
 			}
 		});
 
 		filterMenu.add(new ActionMenuItem("Saturate") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new MatrixImageFilter(3, 3, new float[]{
 						0.0f,	0.0f,	 0.0f,
 						0.0f,	2.0f,	 0.0f,
 						0.0f,	0.0f,	 0.0f
 				});
 				
 				new FilterWithProgressBar(filter).run();						
 			}
 		});
 
 		filterMenu.add(new ActionMenuItem("Blur + Lighten") {
 			public void actionPerformed(ActionEvent e)
 			{
 				ImageFilter filter = new MatrixImageFilter(5, 5, new float[]{
 						0.0f,	0.0f,	 0.25f,	0.0f,	0.0f,
 						0.0f,	0.0f,	 0.0f,	0.0f,	0.0f,
 						0.25f,	0.0f,	 1.0f,	0.0f,	0.25f,
 						0.0f,	0.0f,	 0.0f,	0.0f,	0.0f,
 						0.0f,	0.0f,	 0.25f,	0.0f,	0.0f
 				});
 				
 				new FilterWithProgressBar(filter).run();						
 			}
 		});
 		
 		filterMenu.add(new ActionMenuItem("Resize...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = JOptionPane.showInputDialog(f, "Resize scale:");
 				final double scale = Double.parseDouble(s);
 
 				final JProgressBar pb = new JProgressBar();
 				final JDialog d = new JDialog(f);
 				JPanel panel = new JPanel(new BorderLayout());
 				pb.setValue(0);
 				pb.setStringPainted(true);
 				panel.add(new JLabel("Processing filter..."), BorderLayout.NORTH);
 				panel.add(pb, BorderLayout.CENTER);
 				d.getContentPane().add(panel);
 				d.pack();
 				d.setVisible(true);
 	
 				new Thread(new Runnable() {
 					public void run()
 					{
 						long st = System.currentTimeMillis();
 						
 						ImageFilter filter = new ResizeFilter("Resize", scale);
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							il.setImage(filter.processImage(il.getImage()));
 						}
 						
 						final int inc = 100 / ch.getCanvas().getLayers().size();
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							il.setImage(filter.processImage(il.getImage()));
 							
 							try
 							{
 								SwingUtilities.invokeAndWait(new Runnable() {
 									public void run()
 									{
 										pb.setValue(pb.getValue() + inc);	
 									}
 								});
 							}
 							catch (InterruptedException e)
 							{
 								e.printStackTrace();
 							}
 							catch (InvocationTargetException e)
 							{
 								e.printStackTrace();
 							}
 						}
 						
 						long tp = System.currentTimeMillis() - st;
 						System.out.println("complete in: " + tp);
 						
 						pb.setValue(100);
 						d.dispose();
 						
 						Rectangle r = new Rectangle();
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							r.add(new Rectangle(
 									il.getX(),
 									il.getY(),
 									il.getWidth(),
 									il.getHeight()
 							));
 						}
 						
 						ch.getCanvas().setSize(r.width, r.height);
 						
 						p.repaint();
 						ilList.repaint();
 						p.revalidate();
						filter = null;
 					}
 				}).start();
 			}
 		});
 		
 		filterMenu.add(new ActionMenuItem("Resize Concurrent...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = JOptionPane.showInputDialog(f, "Resize scale:");
 				final double scale = Double.parseDouble(s);
 				
 				final JProgressBar pb = new JProgressBar();
 				final JDialog d = new JDialog(f);
 				JPanel panel = new JPanel(new BorderLayout());
 				pb.setValue(0);
 				pb.setStringPainted(true);
 				panel.add(new JLabel("Processing filter..."), BorderLayout.NORTH);
 				panel.add(pb, BorderLayout.CENTER);
 				d.getContentPane().add(panel);
 				d.pack();
 				d.setVisible(true);
 				
 				new Thread(new Runnable() {
 					public void run()
 					{
 						long st = System.currentTimeMillis();
 						
 						ImageFilter filter = new ImageFilterThreadingWrapper(new ResizeFilter("Resize Concurrent", scale));
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							il.setImage(filter.processImage(il.getImage()));
 						}
 						
 						final int inc = 100 / ch.getCanvas().getLayers().size();
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							il.setImage(filter.processImage(il.getImage()));
 							
 							try
 							{
 								SwingUtilities.invokeAndWait(new Runnable() {
 									public void run()
 									{
 										pb.setValue(pb.getValue() + inc);	
 									}
 								});
 							}
 							catch (InterruptedException e)
 							{
 								e.printStackTrace();
 							}
 							catch (InvocationTargetException e)
 							{
 								e.printStackTrace();
 							}
 						}
 						
 						long tp = System.currentTimeMillis() - st;
 						System.out.println("complete in: " + tp);
 						
 						pb.setValue(100);
 						d.dispose();
 						
 						Rectangle r = new Rectangle();
 						
 						for (ImageLayer il : ch.getCanvas().getLayers())
 						{
 							r.add(new Rectangle(
 									il.getX(),
 									il.getY(),
 									il.getWidth(),
 									il.getHeight()
 							));
 						}
 						
 						ch.getCanvas().setSize(r.width, r.height);
 						
 						p.repaint();
 						ilList.repaint();
 						p.revalidate();
						filter = null;
 					}
 				}).start();
 			}
 		});
 		
 		layerMenu.addSeparator();
 		
 		layerMenu.add(new ActionMenuItem("Throw Exception") {
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					class CatchMeIfYouCanException extends Exception
 					{
 						private CatchMeIfYouCanException(String s)
 						{
 							super(s);
 						}
 					}
 					
 					throw new CatchMeIfYouCanException("Ha! Catch me if you can!");
 				}
 				catch (Exception ex)
 				{
 					ApplicationUtils.showExceptionMessage(ex);
 				}
 			}
 		});
 		
 		layerMenu.addSeparator();
 
 		
 		final JScrollPane sp = new JScrollPane(p);
 		sp.getViewport().addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e)
 			{
 				sp.repaint();
 			}
 		});
 		
 		layerMenu.add(new ActionMenuItem("Zoom...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String s = JOptionPane.showInputDialog(f, "Zoom:");
 				
 				ch.getCanvas().setZoom(Double.parseDouble(s));
 				
 				p.revalidate();
 				p.repaint();
 			}
 		});
 		
 		JMenu helpMenu = new JMenu("Help");
 
 		helpMenu.add(new ActionMenuItem("About...") {
 			public void actionPerformed(ActionEvent e)
 			{
 				String msg = "Coobird's Paint dot Jar Demonstration 2";
 				JOptionPane.showMessageDialog(f, msg, "About Paint dot Jar", JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 
 		menubar.add(fileMenu);
 		menubar.add(layerMenu);
 		menubar.add(filterMenu);
 		menubar.add(helpMenu);
 		f.setJMenuBar(menubar);
 
 		f.setSize(640, 480);
 		f.setLocation(1, 1);
 		JSplitPane splitPane = new JSplitPane(
 				JSplitPane.HORIZONTAL_SPLIT,
 				true,
 				sp,
 				listPanels
 		);
 		f.getContentPane().add(splitPane, BorderLayout.CENTER);
 		
 		f.validate();
 		f.setVisible(true);
 		splitPane.setDividerLocation(0.7);
 	}
 
 	private void updateGUI(final DefaultListModel ilListModel,
 			final CanvasHolder ch, final JPanel p)
 	{
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			public void run()
 			{
 				ilListModel.removeAllElements();
 				for (ImageLayer il : ch.getCanvas().getLayers())
 				{
 					ilListModel.addElement(il);
 				}
 				
 				p.repaint();
 				p.revalidate();
 			}
 		});
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (UnsupportedLookAndFeelException e)
 		{
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InstantiationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 		
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				new DemoApp2().makeGUI();
 			}
 		});
 	}
 	
 	private static class CanvasHolder
 	{
 		Canvas c;
 		
 		public Canvas getCanvas()
 		{
 			return c;
 		}
 		
 		public void setCanvas(Canvas c)
 		{
 			this.c = c;
 		}
 	}
 }
