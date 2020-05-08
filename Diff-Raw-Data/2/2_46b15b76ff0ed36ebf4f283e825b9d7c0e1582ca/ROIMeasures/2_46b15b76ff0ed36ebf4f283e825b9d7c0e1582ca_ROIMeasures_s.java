 package plugins.adufour.roi;
 
 import icy.gui.main.MainEvent;
 import icy.gui.main.MainListener;
 import icy.main.Icy;
 import icy.roi.ROI2D;
 import icy.sequence.Sequence;
 import icy.sequence.SequenceEvent;
 import icy.sequence.SequenceEvent.SequenceEventSourceType;
 import icy.sequence.SequenceListener;
 import icy.type.collection.array.Array1DUtil;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.UUID;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 import javax.vecmath.Point3d;
 
 import jxl.Workbook;
 import jxl.format.Colour;
 import jxl.format.RGB;
 import jxl.write.Label;
 import jxl.write.Number;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 import plugins.adufour.ezplug.EzButton;
 import plugins.adufour.ezplug.EzException;
 import plugins.adufour.ezplug.EzPlug;
 import plugins.adufour.ezplug.EzVar;
 import plugins.adufour.ezplug.EzVarBoolean;
 import plugins.adufour.ezplug.EzVarListener;
 import plugins.adufour.ezplug.EzVarSequence;
 
 public class ROIMeasures extends EzPlug
 {
 	private static File					xlsFile;
 	
 	private static WritableWorkbook		workbook;
 	
 	// Create a static workbook instance to make sure a single file is opened
 	static
 	{
 		try
 		{
 			xlsFile = File.createTempFile("icy_ROI_Measures_" + UUID.randomUUID().toString(), "xls");
 			workbook = Workbook.createWorkbook(xlsFile);
 			
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			throw new EzException("ROI Meaasures: unable to initialize\n(see output console for details)", true);
 		}
 	}
 	
 	private final EzVarSequence			currentSeq			= new EzVarSequence("Current sequence");
 	
 	private final EzVarBoolean			liveUpdate			= new EzVarBoolean("Auto-update", true);
 	
 	private final ExcelTable			table				= new ExcelTable();
 	
 	private final HashMap<ROI2D, Color>	ROIColors			= new HashMap<ROI2D, Color>();
 	
 	private final SequenceListener		sequenceListener	= new SequenceListener()
 															{
 																@Override
 																public void sequenceClosed(Sequence sequence)
 																{
 																	sequence.removeListener(sequenceListener);
 																}
 																
 																@Override
 																public void sequenceChanged(SequenceEvent sequenceEvent)
 																{
 																	if (!liveUpdate.getValue())
 																		return;
 																	
 																	if (sequenceEvent.getSourceType() != SequenceEventSourceType.SEQUENCE_ROI)
 																		return;
 																	
 																	Sequence sequence = sequenceEvent.getSequence();
 																	
 																	Sequence selected = currentSeq.getValue(false);
 																	if (selected != sequence)
 																		return;
 																	
 																	Object roi = sequenceEvent.getSource();
 																	WritableSheet sheet = getOrCreateSheet(sequence);
 																	
 																	switch (sequenceEvent.getType())
 																	{
 																		case CHANGED:
 																			
 																			if (roi == null)
 																				update(sequence);
 																			else update(sequence, (ROI2D) roi);
 																		
 																		break;
 																		
 																		case REMOVED:
 																			int lastRow = sheet.getRows();
 																			sheet.removeRow(lastRow - 1);
 																			update(sequence);
 																	}
 																	
 																	// in any case, update the table
 																	// table.repaint();
 																	table.updateSheet(sheet);
 																}
 															};
 	
 	private final MainListener			mainListener		= new MainListener()
 															{
 																public void viewerOpened(MainEvent event)
 																{
 																}
 																
 																public void viewerFocused(MainEvent event)
 																{
 																}
 																
 																public void viewerClosed(MainEvent event)
 																{
 																}
 																
 																public void sequenceOpened(MainEvent event)
 																{
 																	((Sequence) event.getSource()).addListener(sequenceListener);
 																}
 																
 																public void sequenceFocused(MainEvent event)
 																{
 																}
 																
 																public void sequenceClosed(MainEvent event)
 																{
 																	((Sequence) event.getSource()).removeListener(sequenceListener);
 																}
 																
 																public void roiRemoved(MainEvent event)
 																{
 																}
 																
 																public void roiAdded(MainEvent event)
 																{
 																}
 																
 																public void pluginOpened(MainEvent event)
 																{
 																}
 																
 																public void pluginClosed(MainEvent event)
 																{
 																}
 																
 																public void painterRemoved(MainEvent event)
 																{
 																}
 																
 																public void painterAdded(MainEvent event)
 																{
 																}
 															};
 	
 	@Override
 	protected void initialize()
 	{
 		getUI().setParametersIOVisible(false);
 		getUI().setRunButtonText("Update");
 		getUI().setActionPanelVisible(!liveUpdate.getValue());
 		
 		addEzComponent(currentSeq);
 		
 		addEzComponent(liveUpdate);
 		
 		table.setPreferredSize(new Dimension(500, 100));
 		
 		addComponent(table);
 		
 		try
 		{
 			xlsFile = File.createTempFile("icy_ROI_Measures_" + UUID.randomUUID().toString(), "xls");
 			workbook = Workbook.createWorkbook(xlsFile);
 			
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			throw new EzException("ROI Meaasures: unable to initialize\n(see output console for details)", true);
 		}
 		
 		EzButton buttonExport = new EzButton("Export to .CSV file...", new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser jfc = new JFileChooser();
 				jfc.setFileFilter(new FileFilter()
 				{
 					@Override
 					public boolean accept(File f)
 					{
 						return f.isDirectory() || f.getAbsolutePath().toLowerCase().endsWith(".csv");
 					}
 					
 					@Override
 					public String getDescription()
 					{
 						return ".CSV (Comma Separated Values)";
 					}
 				});
 				
 				if (jfc.showSaveDialog(getUI()) == JFileChooser.APPROVE_OPTION)
 				{
 					try
 					{
 						ExportCSV.export(workbook, jfc.getSelectedFile(), false);
 					}
 					catch (IOException e1)
 					{
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		addEzComponent(buttonExport);
 		
 		// Listeners
 		
 		// add a listener to currently opened sequences
 		for (final Sequence sequence : Icy.getMainInterface().getSequences())
 			sequence.addListener(sequenceListener);
 		
 		// add a listener to detect newly opened sequences
 		Icy.getMainInterface().addListener(mainListener);
 		
 		currentSeq.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence sequence)
 			{
 				table.updateSheet(sequence == null ? null : getOrCreateSheet(sequence));
 				getUI().repack(false);
 			}
 		});
 		
 		liveUpdate.addVarChangeListener(new EzVarListener<Boolean>()
 		{
 			@Override
 			public void variableChanged(EzVar<Boolean> source, Boolean newValue)
 			{
 				getUI().setActionPanelVisible(!newValue);
 			}
 		});
 		
 		try
 		{
 			update(currentSeq.getValue());
 			table.repaint();
 		}
 		catch (EzException e)
 		{
 		}
 	}
 	
 	/**
 	 * Creates a new sheet for the given sequence, or returns the current existing sheet (if any)
 	 * 
 	 * @param sequence
 	 * @return
 	 */
 	private static synchronized WritableSheet getOrCreateSheet(Sequence sequence)
 	{
 		String sheetName = getSheetName(sequence);
 		
 		WritableSheet sheet = workbook.getSheet(sheetName);
 		
 		if (sheet == null)
 		{
 			sheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets() + 1);
 			
 			try
 			{
 				sheet.addCell(new Label(0, 0, "Name"));
 				sheet.addCell(new Label(1, 0, "Color"));
 				sheet.addCell(new Label(2, 0, "min. X"));
 				sheet.addCell(new Label(3, 0, "min. Y"));
 				sheet.addCell(new Label(4, 0, "max. X"));
 				sheet.addCell(new Label(5, 0, "max. Y"));
 				for (int c = 0; c < sequence.getSizeC(); c++)
 					sheet.addCell(new Label(5 + c + 1, 0, "Ch. " + c));
 				
 			}
 			catch (RowsExceededException e)
 			{
 				e.printStackTrace();
 			}
 			catch (WriteException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		return sheet;
 	}
 	
 	private static String getSheetName(Sequence s)
 	{
 		return "Sequence " + s.hashCode();
 	}
 	
 	@Override
 	protected void execute()
 	{
 		update(currentSeq.getValue());
 	}
 	
 	private void update(Sequence sequence)
 	{
 		for (ROI2D roi : sequence.getROI2Ds())
 			update(sequence, roi);
 	}
 	
 	private void update(Sequence sequence, ROI2D roi)
 	{
 		try
 		{
 			int row = sequence.getROI2Ds().indexOf(roi) + 1;
 			
			WritableSheet sheet = workbook.getSheet(getSheetName(sequence));
 			
 			// set the name
 			sheet.addCell(new Label(0, row, roi.getName()));
 			
 			// set the color (if it has changed)
 			if (roi.getColor() != ROIColors.get(roi))
 			{
 				ROIColors.put(roi, roi.getColor());
 				
 				// JXL is VERY nasty, won't allow custom (AWT) colors !!!
 				
 				// walk-around: look for the closest color match
 				Color col = roi.getColor();
 				Point3d cval = new Point3d(col.getRed(), col.getGreen(), col.getBlue());
 				
 				Colour colour = Colour.AUTOMATIC;
 				double minDistance = Double.MAX_VALUE;
 				for (Colour c : Colour.getAllColours())
 				{
 					RGB rgb = c.getDefaultRGB();
 					Point3d cval_jxl = new Point3d(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
 					
 					// compute (squared) distance (to go faster)
 					// I mean, this is lame anyway, isn't it !!
 					double dist = cval.distanceSquared(cval_jxl);
 					if (dist < minDistance)
 					{
 						minDistance = dist;
 						colour = c;
 					}
 				}
 				
 				// do you realize what we did JUST to get the color ?!!
 				final Colour finalColour = colour;
 				sheet.addCell(new Label(1, row, colour.getDescription(), new WritableCellFormat()
 				{
 					@Override
 					public Colour getBackgroundColour()
 					{
 						return finalColour;
 					}
 				}));
 			}
 			
 			// set x,y,w,h
 			Rectangle bounds = roi.getBounds();
 			sheet.addCell(new Number(2, row, bounds.x));
 			sheet.addCell(new Number(3, row, bounds.y));
 			sheet.addCell(new Number(4, row, bounds.x + bounds.width));
 			sheet.addCell(new Number(5, row, bounds.y + bounds.height));
 			
 			boolean[] mask = roi.getAsBooleanMask().mask;
 			Object[] z_xy = (Object[]) sequence.getFirstImage().getDataXYC();
 			boolean signed = sequence.isSignedDataType();
 			int width = sequence.getSizeX();
 			int height = sequence.getSizeY();
 			
 			double[] sum = new double[sequence.getSizeC()];
 			
 			int ioff = bounds.x + bounds.y * width;
 			int moff = 0;
 			
 			for (int iy = bounds.y, my = 0; my < bounds.height; my++, iy++, ioff += sequence.getSizeX() - bounds.width)
 				for (int ix = bounds.x, mx = 0; mx < bounds.width; mx++, ix++, ioff++, moff++)
 				{
 					if (iy >= 0 && ix >= 0 && iy < height && ix < width && mask[moff])
 						for (int c = 0; c < sum.length; c++)
 							sum[c] += Array1DUtil.getValue(z_xy[c], ioff, signed);
 				}
 			
 			for (int c = 0; c < sum.length; c++)
 				sheet.addCell(new Number(5 + c + 1, row, sum[c]));
 		}
 		catch (RowsExceededException e)
 		{
 			e.printStackTrace();
 		}
 		catch (WriteException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void clean()
 	{
 		ROIColors.clear();
 		
 		// remove listeners
 		for (final Sequence sequence : Icy.getMainInterface().getSequences())
 			sequence.removeListener(sequenceListener);
 		
 		Icy.getMainInterface().removeListener(mainListener);
 		
 		// clean static stuff (if this is the last instance)
 		if (getNbInstances() == 1)
 		{
 			try
 			{
 				workbook.close();
 				xlsFile.deleteOnExit();
 			}
 			catch (WriteException e)
 			{
 				e.printStackTrace();
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 }
