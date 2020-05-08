 package org.selfbus.sbtools.knxcom.gui.busmonitor;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 import org.selfbus.sbtools.common.Config;
 import org.selfbus.sbtools.common.HexString;
 import org.selfbus.sbtools.common.address.PhysicalAddress;
 import org.selfbus.sbtools.common.gui.actions.BasicAction;
 import org.selfbus.sbtools.common.gui.components.Dialogs;
 import org.selfbus.sbtools.common.gui.components.ToolBar;
 import org.selfbus.sbtools.common.gui.misc.ImageCache;
 import org.selfbus.sbtools.common.gui.models.FilteredListModel;
 import org.selfbus.sbtools.knxcom.application.AbstractMemory;
 import org.selfbus.sbtools.knxcom.application.Application;
 import org.selfbus.sbtools.knxcom.application.DeviceDescriptorResponse;
 import org.selfbus.sbtools.knxcom.application.MemoryRead;
 import org.selfbus.sbtools.knxcom.application.MemoryResponse;
 import org.selfbus.sbtools.knxcom.application.MemoryWrite;
 import org.selfbus.sbtools.knxcom.application.devicedescriptor.DeviceDescriptor0;
 import org.selfbus.sbtools.knxcom.application.memory.MemoryAddressMapper;
 import org.selfbus.sbtools.knxcom.application.memory.MemoryAddressMapperFactory;
 import org.selfbus.sbtools.knxcom.emi.EmiFrame;
 import org.selfbus.sbtools.knxcom.emi.EmiFrameFactory;
 import org.selfbus.sbtools.knxcom.emi.EmiTelegramFrame;
 import org.selfbus.sbtools.knxcom.emi.EmiVersion;
 import org.selfbus.sbtools.knxcom.gui.internal.I18n;
 import org.selfbus.sbtools.knxcom.telegram.Telegram;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A panel that displays bus traces.
  */
 public class BusMonitorPanel extends JPanel
 {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusMonitorPanel.class);
    private static final long serialVersionUID = 33873581372573211L;
 
    /**
     * The panel's tool bar.
     */
    protected final ToolBar toolBar = new ToolBar();
 
    private JButton filterButton, colorizeButton;
    private final JScrollPane listView;
    private final JList<BusMonitorItem> list;
    private final BusMonitorCellRenderer<BusMonitorItem> listRenderer = new BusMonitorCellRenderer<BusMonitorItem>();
 
    private final FrameFilter filter = new FrameFilter();
    private final DefaultListModel<BusMonitorItem> model = new DefaultListModel<BusMonitorItem>();
    private final FilteredListModel<BusMonitorItem> filteredModel = new FilteredListModel<BusMonitorItem>(model,
       new BusMonitorItemFilter(filter));
    private final Map<PhysicalAddress, MemoryAddressMapper> addrMappers = new HashMap<PhysicalAddress, MemoryAddressMapper>();
    private final String configPrefix = getClass().getSimpleName() + ".";
 
    private int sequence;
   private boolean autoScroll = true;
 
    /**
     * Create a bus monitor panel.
     */
    public BusMonitorPanel()
    {
       setLayout(new BorderLayout());
 
       toolBar.setFloatable(false);
       add(toolBar, BorderLayout.NORTH);
 
       try
       {
          filter.setEnabled(false);
          filter.fromConfig(Config.getInstance(), configPrefix + "filter");
          listRenderer.setColorize(Config.getInstance().getIntValue(configPrefix + "colorize") != 0);
       }
       catch (Exception e)
       {
          Dialogs.showExceptionDialog(e, I18n.getMessage("Error.loadFilter"));
          filter.reset();
       }
 
       list = new JList<BusMonitorItem>(filteredModel);
       list.setCellRenderer(listRenderer);
 
       listView = new JScrollPane(list);
       add(listView, BorderLayout.CENTER);
 
       updateButtons();
    }
 
    /**
     * Add the default buttons (filter, colorize) to the too bar.
     */
    protected void addDefaultToolBarButtons()
    {
       filterButton = toolBar.add(filterAction);
       filterButton.setSelected(filter.isEnabled());
 
       colorizeButton = toolBar.add(colorizeAction);
       colorizeButton.setSelected(listRenderer.isColorize());
    }
 
    /**
     * Update the tool button states.
     */
    protected void updateButtons()
    {
       boolean haveEntries = !model.isEmpty();
 
       saveAction.setEnabled(haveEntries);
       eraseAction.setEnabled(haveEntries);
    }
 
    /**
     * Clear the trace lines.
     */
    public void erase()
    {
       synchronized (model)
       {
          sequence = 0;
          model.clear();
       }
 
       updateButtons();
    }
 
    /**
     * Add an {@link EmiFrame} to the bus traces.
     * 
     * @param frame - The frame that the entry is about.
     */
    protected void addFrame(final EmiFrame frame)
    {
       addFrame(frame, new Date());
    }
 
    /**
     * Add an {@link EmiFrame} to the bus traces.
     * 
     * @param frame - The frame that the entry is about.
     * @param when - the timestamp when the frame occurred.
     */
    protected void addFrame(final EmiFrame frame, final Date when)
    {
       final int id = ++sequence;
 
       try
       {
          SwingUtilities.invokeLater(new Runnable()
          {
             @Override
             public void run()
             {
                synchronized (model)
                {
                   final boolean needUpdate = model.isEmpty();
 
                   model.addElement(new BusMonitorItem(id, when, frame));
 
                  if (autoScroll)
                  {
                     int idx = model.getSize() - 1;
                     Rectangle rect = list.getCellBounds(idx, idx);

                     if (rect != null)
                        list.scrollRectToVisible(rect);
                  }
 
                   if (needUpdate)
                      updateButtons();
                }
             }
          });
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
    }
 
    /**
     * Create an {@link EmiFrame} from a bus trace line.
     * 
     * @param line - the trace line to convert.
     * @return The frame of the trace line.
     * @throws IOException
     */
    protected EmiFrame createFrame(final String line) throws IOException
    {
       final EmiFrame frame = EmiFrameFactory.createFrame(HexString.valueOf(line), EmiVersion.EMI2);
 
       if (frame instanceof EmiTelegramFrame)
       {
          final Telegram telegram = ((EmiTelegramFrame) frame).getTelegram();
          final Application app = telegram.getApplication();
 
          if (app instanceof DeviceDescriptorResponse)
          {
             final DeviceDescriptorResponse ddr = (DeviceDescriptorResponse) app;
             if (ddr.getDescriptorType() == 0 && !addrMappers.containsKey(telegram.getFrom()))
             {
                final DeviceDescriptor0 dd = (DeviceDescriptor0) ddr.getDescriptor();
                addrMappers.put(telegram.getFrom(),
                   MemoryAddressMapperFactory.getMemoryAddressMapper(dd.getMaskVersion()));
             }
          }
          else if (app instanceof MemoryRead || app instanceof MemoryWrite)
          {
             ((AbstractMemory) app).setAddressMapper(addrMappers.get(telegram.getDest()));
          }
          else if (app instanceof MemoryResponse)
          {
             ((AbstractMemory) app).setAddressMapper(addrMappers.get(telegram.getFrom()));
          }
       }
 
       return frame;
    }
 
    /**
     * Load the bus trace file and display it's contents.
     * 
     * @param file - the file to open
     * @throws IOException if reading the file failed
     */
    public void loadBusTrace(File file) throws IOException
    {
       final BufferedReader in = new BufferedReader(new FileReader(file));

      boolean oldAutoScroll = autoScroll;
      autoScroll = false;

       try
       {
          final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
          Date when;
 
          synchronized (model)
          {
             model.clear();
             addrMappers.clear();
    
             sequence = 0;
             for (int lineNo = 1;; ++lineNo)
             {
                final String line = in.readLine();
                if (line == null)
                   break;
    
                int pos = line.indexOf('\t');
                if (pos < 0)
                   continue;
                
                try
                {
                   when = dateFormatter.parse(line.substring(0, pos));
                }
                catch (ParseException e)
                {
                   LOGGER.warn(file + " line " + lineNo + ": parser exception", e);
                   when = null;
                }
    
                addFrame(createFrame(line.substring(pos + 1)), when);
             }
    
             filteredModel.update();
          }
       }
       finally
       {
          in.close();
         autoScroll = oldAutoScroll;
       }
    }
 
    /**
     * Save the entries to a trace file.
     * 
     * @param file - the file to save into
     * 
     * @throws IOException
     */
    public void saveBusTrace(File file) throws IOException
    {
       final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
       FileWriter out = null;
 
       try
       {
          synchronized (model)
          {
             out = new FileWriter(file);
 
             for (Object obj : model.toArray())
             {
                final BusMonitorItem item = (BusMonitorItem) obj;
 
                out.write(dateFormatter.format(item.getWhen()));
                out.write("\t");
                out.write(HexString.toString(item.getFrame().toByteArray()));
                out.write("\n");
             }
 
             out.flush();
          }
       }
       finally
       {
          if (out != null)
             out.close();
       }
    }
 
    /**
     * Save the entries to a trace file. Opens a file dialog to select a save file.
     */
    protected void saveBusTrace()
    {
       final Config cfg = Config.getInstance();
       String lastDir = cfg.getStringValue(configPrefix + "lastDir");
 
       final JFileChooser dlg = new JFileChooser();
       dlg.setSelectedFile(new File(lastDir));
       final FileFilter fileFilter = new BusTraceFileFilter();
       dlg.addChoosableFileFilter(fileFilter);
       dlg.addChoosableFileFilter(dlg.getAcceptAllFileFilter());
       dlg.setFileFilter(fileFilter);
       dlg.setDialogTitle(I18n.getMessage("BusMonitor.SaveTraceFileTitle"));
       dlg.setDialogType(JFileChooser.SAVE_DIALOG);
 
       if (dlg.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
          return;
 
       final File file = dlg.getSelectedFile();
       if (file == null)
          return;
 
       cfg.put(configPrefix + "lastDir", file.getAbsolutePath());
       Config.getInstance().save();
 
       try
       {
          saveBusTrace(file);
       }
       catch (IOException e)
       {
          Dialogs.showExceptionDialog(e, I18n.getMessage("BusMonitor.ErrSaveTraceFile"));
       }
    }
 
    /**
     * Action: save the trace lines to a file.
     */
    protected final BasicAction saveAction = new BasicAction(I18n.getMessage("BusMonitor.Save.name"),
       I18n.getMessage("BusMonitor.Save.toolTip"),
       ImageCache.getIcon("icons/filesave"))
    {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void actionEvent(ActionEvent e)
       {
          saveBusTrace();
       }
    };
 
    /**
     * Action: erase all trace lines.
     */
    protected final BasicAction eraseAction = new BasicAction(I18n.getMessage("BusMonitor.Clear.name"),
       I18n.getMessage("BusMonitor.Clear.toolTip"),
       ImageCache.getIcon("icons/eraser"))
    {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void actionEvent(ActionEvent e)
       {
          erase();
       }
    };
 
    /**
     * Action: configure the filter.
     */
    protected final BasicAction filterAction = new BasicAction(I18n.getMessage("BusMonitor.Filter.name"),
       I18n.getMessage("BusMonitor.Filter.toolTip"),
       ImageCache.getIcon("icons/filter"))
    {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void actionEvent(ActionEvent e)
       {
          final FilterDialog dlg = new FilterDialog(null, ModalityType.APPLICATION_MODAL);
          dlg.setFilter(filter);
          dlg.setVisible(true);
 
          if (dlg.isAccepted())
          {
             filteredModel.update();
             try
             {
                filter.toConfig(Config.getInstance(), configPrefix + "filter");
                Config.getInstance().save();
             }
             catch (Exception ex)
             {
                Dialogs.showExceptionDialog(ex, I18n.getMessage("Error.saveFilter"));
             }
          }
          filterButton.setSelected(filter.isEnabled());
        }
    };
 
    /**
     * Action: toggle the colorizing of the trace lines.
     */
    protected final BasicAction colorizeAction = new BasicAction(I18n.getMessage("BusMonitor.Colorize.name"),
       I18n.getMessage("BusMonitor.Colorize.toolTip"),
       ImageCache.getIcon("icons/color-fill"))
    {
       private static final long serialVersionUID = 1L;
 
       @Override
       public void actionEvent(ActionEvent e)
       {
          boolean enable = !listRenderer.isColorize();
          Config.getInstance().put(configPrefix + "colorize", enable ? 1 : 0);
          listRenderer.setColorize(enable);
          colorizeButton.setSelected(enable);
          listView.repaint();
       }
    };
 }
