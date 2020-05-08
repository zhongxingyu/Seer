 package cytoscape.dialogs;
 
 import javax.swing.*;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.*;
 
 import cytoscape.Cytoscape;
 
 /**
  * Options dialog for exporting to bitmap images.
  * @author Samad Lotia
  */
 public class ExportBitmapOptionsDialog extends JDialog
 {
 	private JFormattedTextField zoomField;
 	private JFormattedTextField widthInPixelsField;
 	private JFormattedTextField heightInPixelsField;
 	private JFormattedTextField widthInInchesField;
 	private JFormattedTextField heightInInchesField;
 	private JComboBox resolutionComboBox;
 	private JButton okButton;
 
 	private int originalWidth;
 	private int originalHeight;
 
 	/**
 	 * Creates the options dialog.
 	 * This dialog disposes itself when it is closed.
 	 * @param imageWidth The image width to be exported
 	 * @param imageHeight The image height to be exported
 	 * @param listener The action will be called when the "OK" button is clicked
 	 */
 	public ExportBitmapOptionsDialog(int imageWidth, int imageHeight)
 	{
 		super(Cytoscape.getDesktop(), "Export Bitmap Options");
 		this.originalWidth = imageWidth;
 		this.originalHeight = imageHeight;
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		Container content = getContentPane();
 
 		JPanel sizePanel = new JPanel();
 		sizePanel.setBorder(new TitledBorder(new EtchedBorder(), "Image Size"));
 		sizePanel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridwidth = 1;	c.gridheight = 1;
 		c.weightx = 1.0;	c.weighty = 0.0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 
 		JLabel zoomLabel = new JLabel("Zoom: ");
 		zoomField = new JFormattedTextField(new DecimalFormat());
 		zoomField.setColumns(3);
 		ZoomListener zoomListener = new ZoomListener(zoomField);
 		JLabel zoomPercentLabel = new JLabel("%");
 
 		JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		zoomPanel.add(zoomLabel);
 		zoomPanel.add(zoomField);
 		zoomPanel.add(zoomPercentLabel);
 		c.gridx = 0;		c.gridy = 0;
 		sizePanel.add(zoomPanel, c);
 
 		JSeparator separator0 = new JSeparator();
 		c.gridx = 0;		c.gridy = 1;
 		sizePanel.add(separator0, c);
 
 		JLabel widthInPixelsLabel = new JLabel("Width: ");
 		widthInPixelsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		widthInPixelsField.setColumns(4); 
 		new WidthInPixelsListener(widthInPixelsField);
 		JLabel widthPixelsLabel = new JLabel("pixels");
 		
 		JPanel widthInPixelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		widthInPixelsPanel.add(widthInPixelsLabel);
 		widthInPixelsPanel.add(widthInPixelsField);
 		widthInPixelsPanel.add(widthPixelsLabel);
 		c.gridx = 0;		c.gridy = 2;
 		sizePanel.add(widthInPixelsPanel, c);
 
 		JLabel heightInPixelsLabel = new JLabel("Height:");
 		heightInPixelsField = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		heightInPixelsField.setColumns(4);
 		new HeightInPixelsListener(heightInPixelsField);
 		JLabel heightPixelsLabel = new JLabel("pixels");
 
 		JPanel heightInPixelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		heightInPixelsPanel.add(heightInPixelsLabel);
 		heightInPixelsPanel.add(heightInPixelsField);
 		heightInPixelsPanel.add(heightPixelsLabel);
 		c.gridx = 0;		c.gridy = 3;
 		sizePanel.add(heightInPixelsPanel, c);
 
 		JSeparator separator1 = new JSeparator();
 		c.gridx = 0;		c.gridy = 4;
 		sizePanel.add(separator1, c);
 
 		JLabel widthInInchesLabel = new JLabel("Width: ");
 		widthInInchesField = new JFormattedTextField(new DecimalFormat());
 		widthInInchesField.setColumns(4); 
 		new WidthInInchesListener(widthInInchesField);
 		JLabel widthInchesLabel = new JLabel("inches");
 		
 		JPanel widthInInchesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		widthInInchesPanel.add(widthInInchesLabel);
 		widthInInchesPanel.add(widthInInchesField);
 		widthInInchesPanel.add(widthInchesLabel);
 		c.gridx = 0;		c.gridy = 5;
 		sizePanel.add(widthInInchesPanel, c);
 
 		JLabel heightInInchesLabel = new JLabel("Height:");
 		heightInInchesField = new JFormattedTextField(new DecimalFormat());
 		heightInInchesField.setColumns(4);
 		new HeightInInchesListener(heightInInchesField);
 		JLabel heightInchesLabel = new JLabel("inches");
 
 		JPanel heightInInchesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		heightInInchesPanel.add(heightInInchesLabel);
 		heightInInchesPanel.add(heightInInchesField);
 		heightInInchesPanel.add(heightInchesLabel);
 		c.gridx = 0;		c.gridy = 6;
 		sizePanel.add(heightInInchesPanel, c);
 
 		JLabel resolutionLabel = new JLabel("Resolution:");
 		Integer[] resolutions = { new Integer(72), new Integer(100), new Integer(150), new Integer(300) };
 		resolutionComboBox = new JComboBox(resolutions);
 		resolutionComboBox.addActionListener(zoomListener);
 		JLabel dpiLabel = new JLabel("DPI");
 
 		JPanel resolutionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		resolutionPanel.add(resolutionLabel);
 		resolutionPanel.add(resolutionComboBox);
 		resolutionPanel.add(dpiLabel);
 		c.gridx = 0;		c.gridy = 7;
 		sizePanel.add(resolutionPanel, c);
 
 		okButton = new JButton("OK");
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new CancelListener());
 
 		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		buttonsPanel.add(cancelButton);
 		buttonsPanel.add(okButton);
 
 		content.setLayout(new GridBagLayout());
 		c.gridx = 0;		c.gridy = 0;
 		c.weightx = 1.0;	c.weighty = 0.0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		content.add(sizePanel, c);
 		c.gridx = 0;		c.gridy = 1;
 		c.weightx = 1.0;	c.weighty = 1.0;
 		c.fill = GridBagConstraints.BOTH;
 		content.add(buttonsPanel, c);
 
 		updateOnZoom(1.0);
 		//setPreferredSize(new Dimension(250, 280));
 		setResizable(false);
 		pack();
 	}
 
 	public double getZoom()
 	{
 		return ((Number) zoomField.getValue()).doubleValue() / 100.0;
 	}
 
 	/**
 	 * Add an action for when the "OK" button is pressed.
 	 */
 	public void addActionListener(ActionListener l)
 	{
 		okButton.addActionListener(l);
 	}
 
 	private void updateOnZoom(double newZoom)
 	{
 		zoomField.setValue(new Double(newZoom * 100.0));
 		int newWidth = (int) (newZoom * originalWidth);
 		int newHeight = (int) (newZoom * originalHeight);
 		widthInPixelsField.setValue(new Integer(newWidth));
 		heightInPixelsField.setValue(new Integer(newHeight));
 		double dpi = ((Number) resolutionComboBox.getSelectedItem()).doubleValue();
 		double newWidthInches = newWidth / dpi;
 		double newHeightInches = newHeight / dpi;
 		widthInInchesField.setValue(new Double(newWidthInches));
 		heightInInchesField.setValue(new Double(newHeightInches));
 	}
 
 	private void updateOnWidthPixels(int newWidthPixels)
 	{
 		double newZoom = ((double) newWidthPixels) / ((double) originalWidth);
 		updateOnZoom(newZoom);
 	}
 
 	private void updateOnHeightPixels(int newHeightPixels)
 	{
 		double newZoom = ((double) newHeightPixels) / ((double) originalHeight);
 		updateOnZoom(newZoom);
 	}
 
 	private void updateOnWidthInches(double newWidthInches)
 	{
 		double dpi = ((Number) resolutionComboBox.getSelectedItem()).doubleValue();
 		updateOnWidthPixels((int) (newWidthInches * dpi));
 	}
 
 	private void updateOnHeightInches(double newHeightInches)
 	{
 		double dpi = ((Number) resolutionComboBox.getSelectedItem()).doubleValue();
 		updateOnHeightPixels((int) (newHeightInches * dpi));
 	}
 
 	private abstract class FormattedFieldListener extends FocusAdapter implements ActionListener
 	{
 		public abstract void update();
 
 		private JFormattedTextField field;
 		public FormattedFieldListener(JFormattedTextField field)
 		{
 			this.field = field;
 			field.addActionListener(this);
 			field.addFocusListener(this);
 		}
 
 		public void actionPerformed(ActionEvent e)
 		{
 			update();
 		}
 		
 		public void focusLost(FocusEvent l)
 		{
 			try
 			{
 				field.commitEdit();
 			}
 			catch (ParseException exp)
 			{
 				return;
 			}
 			update();
 		}
 	}
 
 	private class ZoomListener extends FormattedFieldListener
 	{
 		public ZoomListener(JFormattedTextField field)
 		{
 			super(field);
 		}
 
 		public void update()
 		{
 			double zoom = ((Number) zoomField.getValue()).doubleValue();
 			zoom /= 100.0;
 			updateOnZoom(zoom);
 		}
 	}
 
 	private class WidthInPixelsListener extends FormattedFieldListener
 	{
 		public WidthInPixelsListener(JFormattedTextField field)
 		{
 			super(field);
 		}
 
 		public void update()
 		{
 			int width = ((Number) widthInPixelsField.getValue()).intValue();
 			updateOnWidthPixels(width);
 		}
 	}
 
 	private class HeightInPixelsListener extends FormattedFieldListener
 	{
 		public HeightInPixelsListener(JFormattedTextField field)
 		{
 			super(field);
 		}
 
 		public void update()
 		{
 			int height = ((Number) heightInPixelsField.getValue()).intValue();
 			updateOnHeightPixels(height);
 		}
 	}
 
 	private class WidthInInchesListener extends FormattedFieldListener
 	{
 		public WidthInInchesListener(JFormattedTextField field)
 		{
 			super(field);
 		}
 
 		public void update()
 		{
 			double width = ((Number) widthInInchesField.getValue()).doubleValue();
 			updateOnWidthInches(width);
 		}
 	}
 
 	private class HeightInInchesListener extends FormattedFieldListener
 	{
 		public HeightInInchesListener(JFormattedTextField field)
 		{
 			super(field);
 		}
 
 		public void update()
 		{
 			double height = ((Number) heightInInchesField.getValue()).doubleValue();
 			updateOnHeightInches(height);
 		}
 	}
 
 	private class CancelListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			ExportBitmapOptionsDialog.this.dispose();
 		}
 	}
 }
