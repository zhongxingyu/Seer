 package yapto.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.sql.SQLException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executors;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.WindowConstants;
 
 import org.apache.log4j.BasicConfigurator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import yapto.picturebank.IPicture;
 import yapto.picturebank.IPictureBank;
 import yapto.picturebank.PictureAddException;
 import yapto.picturebank.sqlfile.SQLFilePictureBank;
 import yapto.picturebank.sqlfile.config.GlobalSQLFilePictureBankConfigurationImpl;
 import yapto.picturebank.sqlfile.config.IGlobalSQLFilePictureBankConfiguration;
 import yapto.picturebank.sqlfile.config.ISQLFilePictureBankConfiguration;
 import yapto.picturebank.sqlfile.config.SQLFilePictureBankConfigurationImpl;
 
 import com.google.common.eventbus.AsyncEventBus;
 import com.google.common.eventbus.EventBus;
 import common.config.InvalidConfigurationException;
 
 /**
  * {@link JFrame} used to display an picture and it's information. Main frame of
  * the application and starting class.
  * 
  * @author benobiwan
  * 
  */
 public final class PictureDisplayFrame extends JFrame implements ActionListener
 {
 	/**
 	 * serialVersionUID for Serialization.
 	 */
 	private static final long serialVersionUID = -4401831166047624407L;
 
 	/**
 	 * Logger object.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(PictureDisplayFrame.class);
 
 	/**
 	 * Action command for the add picture command.
 	 */
 	private static final String ADD_PICTURE_ACTION_COMMAND = "addPicture";
 
 	/**
 	 * Action command for the add directory command.
 	 */
 	private static final String ADD_DIRECTORY_ACTION_COMMAND = "addDirectory";
 
 	/**
 	 * Action command for the quit command.
 	 */
 	private static final String QUIT_ACTION_COMMAND = "quit";
 
 	/**
 	 * {@link JFileChooser} used to select an unique file to add.
 	 */
 	private final JFileChooser _individualPictureChooser = new JFileChooser();
 
 	/**
 	 * {@link JFileChooser} used to select a directory to add.
 	 */
 	private final JFileChooser _directoryChooser = new JFileChooser();
 
 	/**
 	 * {@link IPictureBank} used to load the pictures.
 	 */
 	protected final IPictureBank<?> _pictureBank;
 
 	/**
 	 * Creates a new PictureDisplayFrame.
 	 * 
 	 * @param pictureBank
 	 *            the {@link IPictureBank} used as source for the
 	 *            {@link IPicture}.
 	 */
 	public PictureDisplayFrame(final IPictureBank<?> pictureBank)
 	{
 		super("yapto");
 		_pictureBank = pictureBank;
 
 		_directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowListener()
 		{
 
 			@Override
 			public void windowClosing(final WindowEvent e)
 			{
 				stop();
 			}
 
 			@Override
 			public void windowClosed(final WindowEvent e)
 			{
 				_pictureBank.close();
 				System.exit(0);
 			}
 
 			@Override
 			public void windowOpened(final WindowEvent e)
 			{
 				// nothing to do
 			}
 
 			@Override
 			public void windowIconified(final WindowEvent e)
 			{
 				// nothing to do
 			}
 
 			@Override
 			public void windowDeiconified(final WindowEvent e)
 			{
 				// nothing to do
 			}
 
 			@Override
 			public void windowActivated(final WindowEvent e)
 			{
 				// nothing to do
 			}
 
 			@Override
 			public void windowDeactivated(final WindowEvent e)
 			{
 				// nothing to do
 			}
 		});
 
 		MainPictureDisplayPanel contentPane = null;
 		try
 		{
 			contentPane = new MainPictureDisplayPanel(this,
					_pictureBank.getRandomPictureList(40));
 		}
 		catch (final ExecutionException e1)
 		{
 			logException(e1);
 		}
 		setJMenuBar(createMenuBar());
 		setContentPane(contentPane);
 	}
 
 	/**
 	 * Create the menu bar.
 	 * 
 	 * @return the menu bar for this application.
 	 */
 	private JMenuBar createMenuBar()
 	{
 		final JMenuBar menuBar = new JMenuBar();
 
 		final JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		menuBar.add(fileMenu);
 
 		JMenuItem menuItem = new JMenuItem("add picture");
 		menuItem.setActionCommand(ADD_PICTURE_ACTION_COMMAND);
 		menuItem.addActionListener(this);
 		fileMenu.add(menuItem);
 
 		menuItem = new JMenuItem("add directory");
 		menuItem.setActionCommand(ADD_DIRECTORY_ACTION_COMMAND);
 		menuItem.addActionListener(this);
 		fileMenu.add(menuItem);
 
 		menuItem = new JMenuItem("Quit");
 		menuItem.setMnemonic(KeyEvent.VK_Q);
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 				ActionEvent.ALT_MASK));
 		menuItem.setActionCommand(QUIT_ACTION_COMMAND);
 		menuItem.addActionListener(this);
 		fileMenu.add(menuItem);
 
 		return menuBar;
 	}
 
 	/**
 	 * Stop the application.
 	 */
 	public void stop()
 	{
 		final int iAnswer = JOptionPane.showConfirmDialog(this,
 				"Are you sure you want to exit?", "Exit",
 				JOptionPane.YES_NO_OPTION);
 		switch (iAnswer)
 		{
 		case JOptionPane.YES_OPTION:
 			dispose();
 			break;
 		case JOptionPane.NO_OPTION:
 		case JOptionPane.CLOSED_OPTION:
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent ae)
 	{
 		switch (ae.getActionCommand())
 		{
 		case ADD_PICTURE_ACTION_COMMAND:
 			if (_individualPictureChooser
 					.showOpenDialog(PictureDisplayFrame.this) == JFileChooser.APPROVE_OPTION)
 			{
 				final File file = _individualPictureChooser.getSelectedFile();
 				if (LOGGER.isDebugEnabled())
 				{
 					LOGGER.debug("Opening: " + file.getName() + ".");
 				}
 				try
 				{
 					_pictureBank.addPicture(file.toPath());
 				}
 				catch (final PictureAddException e)
 				{
 					switch (e.getExceptionType())
 					{
 					case FILE_ALREADY_EXISTS:
 						final String strId = e.getPictureId();
 						if (strId != null)
 						{
 							// TODO add a dialog to compare the two pictures.
 							logException(e);
 						}
 						else
 						{
 							logException(e);
 						}
 						break;
 					default:
 						logException(e);
 					}
 				}
 			}
 			break;
 		case ADD_DIRECTORY_ACTION_COMMAND:
 			if (_directoryChooser.showOpenDialog(PictureDisplayFrame.this) == JFileChooser.APPROVE_OPTION)
 			{
 				final File file = _directoryChooser.getSelectedFile();
 				if (LOGGER.isDebugEnabled())
 				{
 					LOGGER.debug("Opening directory: " + file.getName() + ".");
 				}
 				try
 				{
 					_pictureBank.addDirectory(file.toPath());
 					// TODO handle return object
 				}
 				catch (final PictureAddException e)
 				{
 					logException(e);
 				}
 			}
 			break;
 		case QUIT_ACTION_COMMAND:
 			stop();
 			break;
 		default:
 			logError("Action command " + ae.getActionCommand() + " unknown.");
 			break;
 		}
 	}
 
 	/**
 	 * Method used to log an error and display it on a dialog.
 	 * 
 	 * @param strError
 	 *            the error to display.
 	 */
 	private void logError(final String strError)
 	{
 		JOptionPane.showMessageDialog(this, strError, "Error",
 				JOptionPane.ERROR_MESSAGE);
 		LOGGER.error(strError);
 	}
 
 	/**
 	 * Method used to log an exception and display it's message on a dialog.
 	 * 
 	 * @param e
 	 *            the exception to log.
 	 */
 	private void logException(final Exception e)
 	{
 		JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
 				JOptionPane.ERROR_MESSAGE);
 		LOGGER.error(e.getMessage(), e);
 	}
 
 	/**
 	 * Main function.
 	 * 
 	 * @param args
 	 *            command line parameters.
 	 * @throws IOException
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 * @throws InvalidConfigurationException
 	 */
 	public static void main(final String[] args) throws ClassNotFoundException,
 			SQLException, IOException, InvalidConfigurationException
 	{
 		BasicConfigurator.configure();
 
 		final IGlobalSQLFilePictureBankConfiguration globalConf = new GlobalSQLFilePictureBankConfigurationImpl(
 				null, ManagementFactory.getPlatformMBeanServer(),
 				Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(3));
 
 		final ISQLFilePictureBankConfiguration conf = new SQLFilePictureBankConfigurationImpl(
 				null, ManagementFactory.getPlatformMBeanServer(),
 				Integer.valueOf(1), "local",
 				"/home/benobiwan/images/photoDB/photoDB.sqlite",
 				"/home/benobiwan/images/photoDB/photos/",
 				"/home/benobiwan/images/photoDB/thumbnails/",
 				"/home/benobiwan/images/photoDB/index/");
 
 		final EventBus bus = new AsyncEventBus(Executors.newFixedThreadPool(10));
 		final SQLFilePictureBank pictureBank = new SQLFilePictureBank(
 				globalConf, conf, bus);
 
 		final PictureDisplayFrame main = new PictureDisplayFrame(pictureBank);
 		main.pack();
 		main.setVisible(true);
 	}
 }
