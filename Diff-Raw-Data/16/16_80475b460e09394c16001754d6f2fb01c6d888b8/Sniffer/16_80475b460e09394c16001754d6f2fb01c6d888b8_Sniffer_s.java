 package org.selfbus.sbtools.sniffer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.JViewport;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import org.apache.commons.lang3.Validate;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.SingleFrameApplication;
 import org.selfbus.sbtools.common.Config;
 import org.selfbus.sbtools.common.Environment;
 import org.selfbus.sbtools.common.gui.components.Dialogs;
 import org.selfbus.sbtools.common.gui.misc.LookAndFeelManager;
 import org.selfbus.sbtools.common.gui.window.XmlToolBarFactory;
 import org.selfbus.sbtools.sniffer.components.NewlineMode;
 import org.selfbus.sbtools.sniffer.components.PortToolBar;
 import org.selfbus.sbtools.sniffer.components.RecordsListCellRenderer;
 import org.selfbus.sbtools.sniffer.layout.MultiLineFlowLayout;
 import org.selfbus.sbtools.sniffer.misc.I18n;
 import org.selfbus.sbtools.sniffer.model.Direction;
 import org.selfbus.sbtools.sniffer.model.Record;
 import org.selfbus.sbtools.sniffer.model.SnifReader;
 import org.selfbus.sbtools.sniffer.model.SnifWriter;
 import org.selfbus.sbtools.sniffer.serial.Parity;
 import org.selfbus.sbtools.sniffer.serial.PortReader;
 import org.selfbus.sbtools.sniffer.serial.ReceivedByte;
 import org.selfbus.sbtools.sniffer.serial.Stop;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The sniffer's application class.
  */
 public final class Sniffer extends SingleFrameApplication
 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sniffer.class);
 
    private final PortToolBar portToolBar = new PortToolBar("ports");
    private final JCheckBox viewAsciiCheck = new JCheckBox(I18n.getMessage("ToolBar.viewAscii"));
    private final JCheckBox viewHexCheck = new JCheckBox(I18n.getMessage("ToolBar.viewHex"));
    private final JCheckBox autoScrollCheck = new JCheckBox(I18n.getMessage("ToolBar.autoScroll"));
    private final JComboBox<NewlineMode> newlineCombo = new JComboBox<NewlineMode>();
    private JTextField newlineTimeField = new JTextField();
 
    private final DefaultListModel<Record> recordsModel = new DefaultListModel<Record>();
    private final JList<Record> recordsList = new JList<Record>(recordsModel);
    private final JScrollPane recordsPane = new JScrollPane(recordsList);
    private RecordsListCellRenderer recordsCellRenderer;
 
    // The queue for receiving bytes for both send and receive direction
    private final Queue<ReceivedByte> readQueue = new ArrayBlockingQueue<ReceivedByte>(8192);
 
    // Refresh interval for the records when data arrives, in milliseconds
    private int refreshInterval = 100;
 
    // Mode for automatic newline
    private NewlineMode newlineMode = NewlineMode.NONE;
 
    // Milliseconds for automatic newline
    private int newlineTime = 0;
 
    // The port reader for sending
    private PortReader sendPortReader = new PortReader(Direction.SEND, readQueue);
 
    // The port reader for receiving
    private PortReader recvPortReader = new PortReader(Direction.RECV, readQueue);
 
    // The records
    private List<Record> records = Collections.synchronizedList(new ArrayList<Record>());
 
    private boolean running = true;
    private Font monoFont;
 
    private Config config = Config.getInstance();
    private String configFileName;
 
    /**
     * Start the sniffer.
     *
     * @param args - the command-line arguments.
     */
    public static void main(String[] args)
    {
       LookAndFeelManager.setDefaultLookAndFeel();
       Application.launch(Sniffer.class, args);
    }
 
    /**
     * @return The application object.
     */
    public static Sniffer getInstance()
    {
       return getInstance(Sniffer.class);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(String[] args)
    {
       try
       {
          super.initialize(args);
 
          Environment.setAppName(getClass().getSimpleName());
          File appDir = new File(Environment.getAppDir());
          getContext().getLocalStorage().setDirectory(appDir);
 
          loadConfig();
       }
       catch (Exception e)
       {
          Dialogs.showExceptionDialog(e, I18n.getMessage("Error.startup"));
          System.exit(1);
       }
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void startup()
    {
       try
       {
          FrameView mainView = getMainView();
 
          final JFrame mainFrame = mainView.getFrame();
          mainFrame.setName("sbtools-sniffer");
          mainFrame.setMinimumSize(new Dimension(600, 500));
          mainFrame.setTitle(I18n.getMessage("Sniffer.title"));
          mainFrame.setLayout(new BorderLayout());
          
          JPanel toolBarPanel = new JPanel();
          mainFrame.add(toolBarPanel, BorderLayout.NORTH);
          toolBarPanel.setLayout(new MultiLineFlowLayout(MultiLineFlowLayout.LEFT, 4, 2));
          toolBarPanel.add(portToolBar);
          toolBarPanel.add(createActionToolBar());
          toolBarPanel.add(createViewToolBar());
          toolBarPanel.add(createNewlineToolBar());
          toolBarPanel.add(createScrollToolBar());
 
          portToolBar.addConnectListener(new ActionListener()
          {
             @Override
             public void actionPerformed(ActionEvent ev)
             {
                connect();
             }
          });
          portToolBar.addDisconnectListener(new ActionListener()
          {
             @Override
             public void actionPerformed(ActionEvent ev)
             {
                disconnect();
             }
          });
 
          try
          {
             monoFont = new Font("monospaced", Font.PLAIN, toolBarPanel.getFont().getSize());         
          }
          catch (RuntimeException e)
          {
             LOGGER.error("failed to create font", e);
          }
 
          mainFrame.add(recordsPane, BorderLayout.CENTER);
 
          show(mainView);
       }
       catch (Exception e)
       {
          Dialogs.showExceptionDialog(e, I18n.getMessage("Error.startup"));
          System.exit(1);
       }
    }
 
    /**
     * Create the action tool bar.
     */
    protected JToolBar createActionToolBar()
    {
       XmlToolBarFactory toolBarFactory = new XmlToolBarFactory();
 
       String fileName = "org/selfbus/sbtools/sniffer/main-toolbar.xml";
       InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
       Validate.notNull(in, "Toolbar configuration not found: " + fileName);
 
       return toolBarFactory.createToolBar(in);
    }
 
    /**
     * Create the view tool bar.
     */
    protected JToolBar createViewToolBar()
    {
       JToolBar toolBar = new JToolBar("view");
 
       viewAsciiCheck.addActionListener(changeViewListener);
       toolBar.add(viewAsciiCheck);
 
       viewHexCheck.addActionListener(changeViewListener);
       toolBar.add(viewHexCheck);
 
       return toolBar;
    }
 
    /**
     * Create the newline tool bar.
     */
    protected JToolBar createNewlineToolBar()
    {
       JToolBar toolBar = new JToolBar("newline");
 
       toolBar.add(new JLabel(I18n.getMessage("ToolBar.newlineMode") + ' '));
       toolBar.add(newlineCombo);
 
       for (NewlineMode mode : NewlineMode.values())
          newlineCombo.addItem(mode);
 
       newlineCombo.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent ev)
          {
             newlineMode = (NewlineMode) newlineCombo.getSelectedItem();
          }
       });
 
       newlineTimeField.setColumns(3);
       newlineTimeField.setHorizontalAlignment(JTextField.RIGHT);
       newlineTimeField.getDocument().addDocumentListener(new DocumentListener()
       {
          @Override
          public void removeUpdate(DocumentEvent ev)
          {
             insertUpdate(ev);
          }
          
          @Override
          public void insertUpdate(DocumentEvent ev)
          {
             int newValue = -1;
             try
             {
                String str = newlineTimeField.getText().trim();
                newValue = str.isEmpty() ? 0 : Integer.parseInt(str);
             }
             catch (NumberFormatException e)
             {
             }
 
             if (newValue >= 0 && newValue <= 9999)
             {
                newlineTime = newValue;
             }
             else
             {
                SwingUtilities.invokeLater(new Runnable()
                {
                   @Override
                   public void run()
                   {
                      newlineTimeField.setText(Integer.toString(newlineTime));
                   }
                });
             }
          }
          
          @Override
          public void changedUpdate(DocumentEvent ev)
          {
          }
       });
 
       toolBar.add(new JLabel(' ' + I18n.getMessage("ToolBar.newlineTimed") + ' '));
       toolBar.add(newlineTimeField);
       toolBar.add(new JLabel(I18n.getMessage("ToolBar.newlineTimedMS")));
 
       return toolBar;
    }
 
    /**
     * Create the scroll tool bar.
     */
    protected JToolBar createScrollToolBar()
    {
       JToolBar toolBar = new JToolBar("scroll");
 
       autoScrollCheck.setToolTipText(I18n.getMessage("ToolBar.autoScrollTip"));
       toolBar.add(autoScrollCheck);
 
       autoScrollCheck.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent ev)
          {
             autoScroll();
          }
       });
 
       return toolBar;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void ready()
    {
       try
       {
          applyConfig();
          queueReaderThread.start();
 
 //         // Load example recording
 //         InputStream in = getClass().getClassLoader().getResourceAsStream("example1.snif");
 //         if (in != null)
 //         {
 //            SnifReader reader = new SnifReader();
 //            records = reader.read(in);
 //            setRecords(records);
 //         }
       }
       catch (RuntimeException e)
       {
          Dialogs.showExceptionDialog(e, I18n.getMessage("Error.startup"));
       }
    }
 
    /**
     * Apply the configuration.
     */
    protected void applyConfig()
    {
       portToolBar.readConfig(config);
 
       viewAsciiCheck.setSelected(config.getIntValue("view.ascii", 0) == 1);
       viewHexCheck.setSelected(config.getIntValue("view.hex", 1) == 1);
       changeViewListener.actionPerformed(null);
 
       newlineMode = NewlineMode.valueOf(config.getStringValue("newlineMode", "NONE"));
       newlineCombo.setSelectedItem(newlineMode);
 
       newlineTime = Math.max(0, Math.min(9999, config.getIntValue("newlineTime", 0)));
       newlineTimeField.setText(Integer.toString(newlineTime));
 
       autoScrollCheck.setSelected(config.getIntValue("autoScroll", 1) == 1);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdown()
    {
       portToolBar.writeConfig(config);
       config.put("connected", sendPortReader.isOpened() && sendPortReader.isOpened() ? 1 : 0);
       config.put("view.ascii", viewAsciiCheck.isSelected() ? 1 : 0);
       config.put("view.hex", viewHexCheck.isSelected() ? 1 : 0);
       config.put("newlineMode", newlineMode.name());
       config.put("newlineTime", newlineTime);
       config.put("autoScroll", autoScrollCheck.isSelected() ? 1 : 0);
 
       saveConfig();
       super.shutdown();
 
 //      getMainFrame().setVisible(false);
 
       running = false;
       sendPortReader.close();
       sendPortReader.close();
       queueReaderThread.interrupt();
    }
 
    /**
     * @return The application's configuration.
     */
    public Config getConfig()
    {
       return config;
    }
 
    /**
     * Save the configuration.
     */
    public void saveConfig()
    {
       try
       {
          config.save(configFileName);
       }
       catch (IOException e)
       {
          Dialogs.showErrorDialog(I18n.formatMessage("Error.write", configFileName));
       }
    }
 
    /**
     * Load the configuration. Automatically called during application initialization.
     */
    protected void loadConfig()
    {
       configFileName = getContext().getLocalStorage().getDirectory().getPath() + File.separator + "user.config";
       try
       {
          config.load(configFileName);
       }
       catch (FileNotFoundException e)
       {
       }
       catch (IOException e)
       {
          Dialogs.showErrorDialog(I18n.formatMessage("Error.read", configFileName));
       }
    }
 
    /**
     * Clear the records.
     */
    public void clear()
    {
       recordsModel.clear();
       records.clear();
    }
 
    /**
     * Set the records that are displayed.
     * 
     * @param records - the records to show.
     */
    public void setRecords(List<Record> records)
    {
       recordsModel.clear();
 
       for (Record record : records)
          recordsModel.addElement(record);
    }
 
    /**
     * Load a recording.
     *
     * @param file - the file to load
     */
    public void loadRecording(File file)
    {
       disconnect();
       portToolBar.initConnected(false);
 
       LOGGER.debug("Loading {}", file);
 
       try
       {
          SnifReader reader = new SnifReader();
          records = Collections.synchronizedList(reader.read(file));
          setRecords(records);
       }
       catch (RuntimeException | IOException e)
       {
          Dialogs.showExceptionDialog(e, I18n.formatMessage("Error.read", file.toString()));
       }
    }
 
    /**
     * Save the recorded data.
     *
     * @param file - the file to write to
     */
    public void saveRecording(File file)
    {
       LOGGER.debug("Saving {}", file);
 
       SnifWriter writer = new SnifWriter();
       try
       {
          writer.write(file, records);
       }
       catch (RuntimeException | IOException e)
       {
          Dialogs.showExceptionDialog(e, I18n.formatMessage("Error.write", file.toString()));
       }
    }
 
    /**
     * Open the connection.
     */
    public void connect()
    {
       try
       {
          getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
          Validate.isTrue(!sendPortReader.isOpened());
          Validate.isTrue(!recvPortReader.isOpened());
    
          int baudRate = portToolBar.getBaudRate();
          int dataBits = portToolBar.getDataBits();
          Stop stop = portToolBar.getStopBits();
          Parity parity = portToolBar.getParity();
    
          clear();
    
          try
          {
             String portName = portToolBar.getSendPort();
             Validate.notNull(portName, "No sender port selected");
             LOGGER.info("Opening send port {}: {}/{}/{}/{}", new Object[] { portName, baudRate, dataBits, stop, parity });
             sendPortReader.open(portName, baudRate, dataBits, stop, parity);
    
             portName = portToolBar.getRecvPort();
            Validate.notNull(portName, "No receiver port selected");
            LOGGER.info("Opening recv port {}: {}/{}/{}/{}", new Object[] { portName, baudRate, dataBits, stop, parity });
            recvPortReader.open(portName, baudRate, dataBits, stop, parity);
          }
          catch (IOException e)
          {
             sendPortReader.close();
             recvPortReader.close();
    
             throw new RuntimeException(e);
          }
       }
       finally
       {
          getMainFrame().setCursor(Cursor.getDefaultCursor());
       }
    }
 
    /**
     * Close the connection.
     */
    public void disconnect()
    {
       try
       {
          getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          LOGGER.info("Closing ports");
 
          sendPortReader.close();
          recvPortReader.close();
       }
       finally
       {
          getMainFrame().setCursor(Cursor.getDefaultCursor());
       }
    }
 
    /**
     * Handle auto scroll
     */
    public void autoScroll()
    {
       if (!running || !autoScrollCheck.isSelected())
          return;
 
       int lastIndex = recordsModel.getSize() - 1;
 
       JViewport viewport = recordsPane.getViewport();
       Rectangle rect = recordsList.getCellBounds(lastIndex - 1, lastIndex - 1);
       if (rect == null) return;
 
       // The location of the view-port relative to the table
       Point pt = viewport.getViewPosition(); 
 
       // Translate the cell location so that it is relative to the view, assuming
       // the northwest corner of the view is (0,0) 
       rect.setLocation(rect.x - pt.x, rect.y - pt.y);
       rect.x = 0;
       rect.width = 1;
 
       if (new Rectangle(viewport.getExtentSize()).intersects(rect))
          recordsList.ensureIndexIsVisible(lastIndex);
    }
 
    /**
     * Runnable for reading the received bytes from the read queue and adding them
     * to the model.
     */
    protected final Runnable queueReader = new Runnable()
    {
       private final int bufferSize = 256;
       private Record lastRecord = null;
       private boolean needUpdate = false;
       private boolean wantLineBreak;
       private long lastUpdate = 0;
 
       @Override
       public void run()
       {
          LOGGER.debug("receive queue thread started");
 
          while (running)
          {
             final ReceivedByte rec = readQueue.poll();
             if (rec == null)
             {
                if (needUpdate && lastRecord != null)
                {
                   long now = System.currentTimeMillis();
                   if (now - lastUpdate >= refreshInterval)
                   {
                      lastUpdate = now;
                      needUpdate = false;
 
                      try
                      {
                         SwingUtilities.invokeAndWait(new Runnable()
                         {
                            @Override
                            public void run()
                            {
                               recordsModel.setElementAt(lastRecord, recordsModel.getSize() - 1);
                               autoScroll();
                            }
                         });
                      }
                      catch (InvocationTargetException | InterruptedException e)
                      {
                         LOGGER.error("records update failed", e);
                      }
                   }
                }
                else
                {
                   try
                   {
                      Thread.sleep(50);
                   }
                   catch (InterruptedException e)
                   {
                   }
                }
                continue;
             }
 
             long when = rec.time;
             final boolean newRecord = (lastRecord == null ||
                                        lastRecord.direction != rec.direction ||
                                        (newlineTime > 0 && when - lastRecord.time >= newlineTime) ||
                                        lastRecord.length >= bufferSize);
             if (newRecord)
             {
                lastRecord = new Record(rec.time, rec.direction, new byte[bufferSize], 0);
                records.add(lastRecord);
             }
 
             lastRecord.data[lastRecord.length++] = (byte) rec.data;
             wantLineBreak = newlineMode.matches(lastRecord.data, lastRecord.length);
 
             if (newRecord || wantLineBreak || when - lastUpdate >= refreshInterval)
             {
                lastUpdate = when;
                needUpdate = false;
 
                try
                {
                   SwingUtilities.invokeAndWait(new Runnable()
                   {
                      @Override
                      public void run()
                      {
                         if (newRecord) recordsModel.addElement(lastRecord);
                         else recordsModel.setElementAt(lastRecord, recordsModel.getSize() - 1);
                         autoScroll();
                      }
                   });
                }
                catch (InvocationTargetException | InterruptedException e)
                {
                   LOGGER.error("list update failed", e);
                }
             }
             else needUpdate = true;
 
             if (wantLineBreak) lastRecord = null;
          }
 
          LOGGER.debug("receive queue thread terminated");
       }
    };
    protected final Thread queueReaderThread = new Thread(queueReader, "queue-reader");
 
    /**
     * Create an altered color.
     * 
     * @param c - the color to alter
     * @param rf - the factor for red
     * @param gf - the factor for green
     * @param bf - the factor for blue
     * @return The new color
     */
    protected Color createTintedColor(Color c, float rf, float gf, float bf)
    {
       int r = Math.max(0, Math.min(255, (int)(c.getRed() * rf)));
       int g = Math.max(0, Math.min(255, (int)(c.getGreen() * gf)));
       int b = Math.max(0, Math.min(255, (int)(c.getBlue() * bf)));
       return new Color(r, g, b);
    }
    
    /**
     * Listener for changing the view settings.
     */
    protected final ActionListener changeViewListener = new ActionListener()
    {
       @Override
       public void actionPerformed(ActionEvent ev)
       {
          recordsCellRenderer = new RecordsListCellRenderer(viewAsciiCheck.isSelected(), viewHexCheck.isSelected());
 
          Color bgColor = recordsList.getBackground();
          Color fgColor = recordsList.getForeground();
 
          Color dateColor = fgColor.brighter().brighter();
          Color sendColor = createTintedColor(bgColor, 0.95f, 0.95f, 1f);
          Color recvColor = createTintedColor(bgColor, 0.95f, 1f, 0.95f);
          Color ctrlColor = createTintedColor(bgColor, 0.8f, 0.8f, 1.8f);
          Color hexColor = createTintedColor(fgColor, 0.7f, 2.5f, 0.7f);
 
          recordsCellRenderer.setFont(monoFont);         
          recordsCellRenderer.setDateColor(dateColor);
          recordsCellRenderer.setSendColor(sendColor);
          recordsCellRenderer.setRecvColor(recvColor);
          recordsCellRenderer.setCtrlColor(ctrlColor);
 
          if (viewAsciiCheck.isSelected())
             recordsCellRenderer.setHexColor(hexColor);
 
          recordsList.setCellRenderer(recordsCellRenderer);
 
          recordsList.setCellRenderer(recordsCellRenderer);
          recordsList.setModel(recordsModel);
 
          autoScroll();
       }
    };
 }
