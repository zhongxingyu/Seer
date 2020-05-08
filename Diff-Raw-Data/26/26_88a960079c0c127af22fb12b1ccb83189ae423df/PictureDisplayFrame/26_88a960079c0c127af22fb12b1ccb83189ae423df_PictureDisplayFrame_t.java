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
 
 import yapto.datasource.IDataSource;
 import yapto.datasource.IPicture;
 import yapto.datasource.PictureAddException;
 import yapto.datasource.sqlfile.SQLFileDataSource;
 import yapto.datasource.sqlfile.config.ISQLFileDataSourceConfiguration;
 import yapto.datasource.sqlfile.config.SQLFileDataSourceConfigurationImpl;
 
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
 
 	private final JFileChooser _chooser = new JFileChooser();
 
 	private final IDataSource<?> _dataSource;
 
 	/**
 	 * Creates a new PictureDisplayFrame.
 	 * 
 	 * @param dataSource
 	 *            the {@link IDataSource} used as source for the
 	 *            {@link IPicture}.
 	 */
 	public PictureDisplayFrame(final IDataSource<?> dataSource)
 	{
 		super("yapto");
 		_dataSource = dataSource;
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
 				// TODO do this better
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
 
 		final MainPictureDisplayPanel contentPane = new MainPictureDisplayPanel(
 				_dataSource.getPictureIterator());
 
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
 			final int returnVal = _chooser
 					.showOpenDialog(PictureDisplayFrame.this);
 			if (returnVal == JFileChooser.APPROVE_OPTION)
 			{
 				final File file = _chooser.getSelectedFile();
 				LOGGER.info("Opening: " + file.getName() + ".");
 				try
 				{
 					_dataSource.addPicture(file);
 				}
				catch (final PictureAddException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			break;
 		case ADD_DIRECTORY_ACTION_COMMAND:
 			break;
 		case QUIT_ACTION_COMMAND:
 			stop();
 			break;
 		default:
 			LOGGER.error("Action command " + ae.getActionCommand()
 					+ " unknown.");
 			break;
 		}
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
 		final ISQLFileDataSourceConfiguration conf = new SQLFileDataSourceConfigurationImpl(
 				null, ManagementFactory.getPlatformMBeanServer(),
 				Integer.valueOf(1),
 				"/home/benobiwan/images/photoDB/photoDB.sqlite",
 				"/home/benobiwan/images/photoDB/");
 		final EventBus bus = new AsyncEventBus(Executors.newFixedThreadPool(10));
 		final SQLFileDataSource dataSource = new SQLFileDataSource(conf, bus);
 
 		// final IDataSource<DummyPicture> dataSource = new DummyDataSource(
 		// new AsyncEventBus(Executors.newFixedThreadPool(10)));
 
 		final PictureDisplayFrame main = new PictureDisplayFrame(dataSource);
 		main.pack();
 		main.setVisible(true);
 	}
 }
