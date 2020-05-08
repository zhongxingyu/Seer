 package plugins.adufour.ezplug;
 
 import icy.file.FileUtil;
 import icy.system.thread.ThreadUtil;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.SwingUtilities;
 
 /**
  * Class defining a variable of type String, embarking a button triggering a file dialog as
  * graphical component
  * 
  * @author Alexandre Dufour
  * 
  */
 public class EzVarFile extends EzVar<File> implements EzVar.Storable<String>
 {
 	private static final long	serialVersionUID	= 1L;
 
 	private JButton			jButtonFile;
 	
 	protected JFileChooser	jFileChooser;
 	
 	private File			selectedFile;
 	
 	/**
 	 * Constructs a new input variable with given name and default file dialog path
 	 * 
 	 * @param varName
 	 *            the name of the variable (as it will appear in the interface)
 	 * @param path
 	 *            the default path to show in the file dialog
 	 */
 	public EzVarFile(String varName, final String path)
 	{
 		super(varName);
 		
 		ThreadUtil.invoke(new Runnable()
 		{
 			
 			@Override
 			public void run()
 			{
 				jButtonFile = new JButton("Select file...");
 				
 				jFileChooser = new JFileChooser(path);
 				jFileChooser.setMultiSelectionEnabled(false);
 				
 				jButtonFile.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						if (jFileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
 							return;
 						
 						setValue(jFileChooser.getSelectedFile());
 					}
 				});
 				
 				setComponent(jButtonFile);
 			}
 		}, !SwingUtilities.isEventDispatchThread());
 	}
 	
 	public File getValue()
 	{
 		return getValue(true);
 	}
 	
 	public File getValue(boolean throwExceptionIfNull)
 	{
		if (selectedFile == null && throwExceptionIfNull)
 			throw new EzException(name + ": no file selected", true);
 		
 		return selectedFile;
 	}
 	
 	/**
 	 * Replaces the button text by the given string
 	 * 
 	 * @param text
 	 */
 	public void setButtonText(String text)
 	{
 		jButtonFile.setText(text);
 	}
 	
 	public void setValue(File value)
 	{
 		selectedFile = value;
 		
 		// this line is necessary if the method is called outside the interface
 		jFileChooser.setSelectedFile(value);
 		
 		String fullPath = selectedFile.getAbsolutePath();
 		jButtonFile.setText(FileUtil.getFileName(fullPath, true));
 		
 		jButtonFile.setToolTipText(fullPath);
 		
 		fireVariableChanged(selectedFile);
 		
 		if (getUI() != null)
 			getUI().repack(false);
 	}
 	
 	@Override
 	public String getXMLValue()
 	{
 		return selectedFile == null ? "" : selectedFile.getAbsolutePath();
 	}
 	
 	@Override
 	public void setXMLValue(String value)
 	{
 		if (value.length() > 0)
 			setValue(new File(value));
 	}
 	
 }
