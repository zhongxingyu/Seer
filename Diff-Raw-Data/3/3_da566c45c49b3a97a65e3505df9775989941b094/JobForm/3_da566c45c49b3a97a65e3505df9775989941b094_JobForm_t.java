 package info.mikaelsvensson.ftpbackup.gui;
 
 import info.mikaelsvensson.ftpbackup.conf.UserSettingParseException;
 import info.mikaelsvensson.ftpbackup.conf.UserSettingsPersistenceStrategyException;
 import info.mikaelsvensson.ftpbackup.gui.conf.GuiUserSettings;
 import info.mikaelsvensson.ftpbackup.log.*;
 import info.mikaelsvensson.ftpbackup.model.Destination;
 import info.mikaelsvensson.ftpbackup.model.DestinationType;
 import info.mikaelsvensson.ftpbackup.model.Job;
 import info.mikaelsvensson.ftpbackup.model.ReportType;
 import info.mikaelsvensson.ftpbackup.model.impl.*;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.*;
 
 public class JobForm extends FormWrapper<JobImpl> {
     private JPanel jobPanel;
     private JButton addDestinationButton;
     private JTabbedPane tabbedPane;
     private JScrollPane sourceFormContainer;
     private JPanel targetsPanel;
     private JButton addReportButton;
     private JPanel reportsPanel;
     private JTextField jobNameTextField;
     private JPanel destinationsTabPanel;
     private JPanel reportsTabPanel;
     private JList historyList;
     private JList<AbstractEntry> historyExecutionLogList;
     private JTextField uniqueIdentifierTextField;
     private JComboBox<JobFormLogFilter> comboHistoryFilterLevel;
     private JLabel labelHistoryFilterStatus;
     private JPanel historyExecutionPanel;
     private JSplitPane scrpnlHistory;
     private JPanel historyPanel;
    private JPanel historyListPanel;
     private JLabel labelHistoryNoJobSelected;
 
     private static class HistoryListItem {
         AbstractJobExecutionEntry firstEntry;
         AbstractJobExecutionEntry lastEntry;
 
         private HistoryListItem(final AbstractJobExecutionEntry firstEntry) {
             this.firstEntry = firstEntry;
         }
 
         @Override
         public String toString() {
             return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(firstEntry.getTimeStamp());
         }
     }
 
     public class ExecutionLogCellRenderer implements ListCellRenderer {
 
         private JPanel p;
         private JPanel iconPanel;
         private JLabel l;
         private JTextArea ta;
 
         public ExecutionLogCellRenderer() {
             p = new JPanel();
             p.setBackground(historyExecutionLogList.getBackground());
             p.setLayout(new BorderLayout());
 
             // icon
             iconPanel = new JPanel(new BorderLayout());
             iconPanel.setBackground(p.getBackground());
             l = GuiComponentFactory.label("");
             iconPanel.add(l, BorderLayout.NORTH);
             p.add(iconPanel, BorderLayout.WEST);
 
             // text
             ta = new JTextArea();
             ta.setLineWrap(true);
             ta.setWrapStyleWord(true);
             p.add(ta, BorderLayout.CENTER);
         }
 
         @Override
         public Component getListCellRendererComponent(final JList list,
                                                       final Object value, final int index, final boolean isSelected,
                                                       final boolean hasFocus) {
             if (value instanceof GenericEntry) {
                 GenericEntry entry = (GenericEntry) value;
                 switch (entry.getSeverity()) {
                     case INFORMATION:
                         l.setIcon(JobFormLogFilter.INFORMATION.getIcon());
                         break;
                     case CRITICAL_ERROR:
                         l.setIcon(JobFormLogFilter.CRITICAL_ERROR.getIcon());
                         break;
                     case DEBUG_INFORMATION:
                         l.setIcon(JobFormLogFilter.DEBUG_INFORMATION.getIcon());
                         break;
                     case NONCRITICAL_ERROR:
                         l.setIcon(JobFormLogFilter.NON_CRITICAL_ERROR.getIcon());
                         break;
                     case WARNING:
                         l.setIcon(JobFormLogFilter.WARNING.getIcon());
                         break;
                 }
             } else if (value instanceof DataMapEntry) {
                 l.setIcon(JobFormLogFilter.REPORTS.getIcon());
             } else {
                 l.setIcon(JobFormLogFilter.INFORMATION.getIcon());
             }
             ta.setText(value.toString());
             int width = list.getWidth() - l.getIcon().getIconWidth();
             // this is just to lure the ta's internal sizing mechanism into action
             if (width > 0) {
                 ta.setSize(width, Short.MAX_VALUE);
             }
             return p;
 
         }
     }
 
     public class CellRenderer extends DefaultListCellRenderer {
 
         private ImageIcon failed;
         private ImageIcon onlyStarted;
         private ImageIcon succeeded;
         private ImageIcon cancelled;
 
         public CellRenderer() {
             try {
                 failed = ImageResourceUtil.getIcon("images/cross.png", null);
                 onlyStarted = ImageResourceUtil.getIcon("images/question-small-white.png", null);
                 succeeded = ImageResourceUtil.getIcon("images/tick.png", null);
                 cancelled = ImageResourceUtil.getIcon("images/cross.png", null);
             } catch (IOException e) {
                 throw new IllegalArgumentException("Could not load folder icon", e);
             }
         }
 
         @Override
         public Component getListCellRendererComponent(final JList list,
                                                       final Object value, final int index, final boolean isSelected,
                                                       final boolean hasFocus) {
             Component component = super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
             if (value instanceof HistoryListItem) {
                 HistoryListItem item = (HistoryListItem) value;
 
                 if (item.lastEntry instanceof JobExecutionCancelledEntry) {
                     setIcon(cancelled);
                 } else if (item.lastEntry instanceof JobExecutionFailedEntry) {
                     setIcon(failed);
                 } else if (item.lastEntry instanceof JobExecutionFinishedEntry) {
                     setIcon(succeeded);
                 } else if (item.lastEntry == null || item.lastEntry instanceof JobExecutionStartedEntry) {
                     setIcon(onlyStarted);
                 } else {
                     setIcon(null);
                 }
             }
             return component;
         }
     }
 
     public JobForm() {
         addDestinationButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JPopupMenu menu = new JPopupMenu("AddDestination");
                 for (final DestinationType type : DestinationType.values()) {
                     menu.add(new JMenuItem(new POAction(type.toString()) {
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             Destination cfg = type.createDestinationConfiguration();
                             addDestinationPanel(cfg);
                         }
                     }));
 
                 }
                 menu.show(addDestinationButton, 0, addDestinationButton.getHeight());
             }
         });
         addReportButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 JPopupMenu menu = new JPopupMenu("AddDestination");
                 for (final ReportType type : ReportType.values()) {
                     menu.add(new JMenuItem(new POAction(type.toString()) {
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             ReportConfigurationImpl cfg = type.createReportConfiguration();
                             addReportPanel(cfg);
                         }
                     }));
 
                 }
                 menu.show(addReportButton, 0, addReportButton.getHeight());
             }
         });
         GuiComponentFactory.setDefaultPanelMargin(destinationsTabPanel);
         GuiComponentFactory.setDefaultPanelMargin(reportsTabPanel);
         GuiComponentFactory.setDefaultPanelMargin(historyPanel);
        GuiComponentFactory.setDefaultPanelMargin(historyListPanel);
         GuiComponentFactory.setDefaultPanelMargin(historyExecutionPanel);
         historyList.addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(final ListSelectionEvent e) {
                 if (!e.getValueIsAdjusting()) {
                     refreshHistoryExecutionLogList();
                 }
             }
         });
         historyExecutionLogList.setCellRenderer(new ExecutionLogCellRenderer());
         historyList.setCellRenderer(new CellRenderer());
 /*
         historyExecutionLogList.setCellRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 setText(value.toString());
                 if (value instanceof ExceptionEntry) {
                     setBackground(Color.RED);
                 } else if (value instanceof DataMapEntry) {
                     setBackground(Color.LIGHT_GRAY);
                 } else if (value instanceof GenericEntry) {
                 }
                 return this;
             }
         });
 */
         comboHistoryFilterLevel.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 JobFormLogFilter value = (JobFormLogFilter) comboHistoryFilterLevel.getSelectedItem();
                 refreshHistoryExecutionLogList(value);
                 try {
                     GuiUserSettings.getInstance().getJobLogFilter().setValue(value);
                     GuiUserSettings.getInstance().save(GuiUserSettings.getInstance().getJobLogFilter());
                 } catch (UserSettingParseException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (UserSettingsPersistenceStrategyException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         scrpnlHistory.setRightComponent(labelHistoryNoJobSelected);
     }
 
     void refreshHistoryExecutionLogList() {
         JobFormLogFilter filter = (JobFormLogFilter) comboHistoryFilterLevel.getSelectedItem();
         refreshHistoryExecutionLogList(filter);
     }
 
     private void refreshHistoryExecutionLogList(final JobFormLogFilter filter) {
         Object selectedValue = historyList.getSelectedValue();
         if (selectedValue instanceof HistoryListItem) {
             HistoryListItem item = (HistoryListItem) selectedValue;
             try {
                 if (historyExecutionLogList.getModel() instanceof DefaultListModel) {
                     DefaultListModel defaultListModel = (DefaultListModel) historyExecutionLogList.getModel();
                     defaultListModel.clear();
                 }
                 LogEntrySerializer serializer = new LogEntrySerializer();
                 Iterator<AbstractEntry> logReader = serializer.getLogReader(new DefaultLogIOFactory().getReader(getCurrentState(), item.firstEntry.getExecutionId()));
                 DefaultListModel<AbstractEntry> model = new DefaultListModel<>();
                 int countEntries = 0;
                 int countShown = 0;
                 while (logReader.hasNext()) {
                     AbstractEntry entry = logReader.next();
                     if (filter.accept(entry)) {
                         model.addElement(entry);
                         countShown++;
                     }
                     countEntries++;
                 }
                 historyExecutionLogList.setModel(model);
                 if (countEntries != countShown) {
                     labelHistoryFilterStatus.setText(text("JobForm.displaying.0.of.1.log.entries", countShown, countEntries));
                 } else {
                     labelHistoryFilterStatus.setText(text("JobForm.displaying.all.log.entries"));
                 }
                 scrpnlHistory.setRightComponent(historyExecutionPanel);
             } catch (FileNotFoundException ex) {
                 ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 scrpnlHistory.setRightComponent(labelHistoryNoJobSelected);
             } catch (IOException ex) {
                 ex.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 scrpnlHistory.setRightComponent(labelHistoryNoJobSelected);
             }
         } else {
             scrpnlHistory.setRightComponent(labelHistoryNoJobSelected);
         }
     }
 
     //TODO Refactor into factory singleton
     private void addDestinationPanel(Destination destination) {
         if (destination instanceof LocalFolderDestinationImpl) {
             addArchivingExpressionForm(targetsPanel, (LocalFolderDestinationImpl) destination, new LocalFolderDestinationForm(), true);
         } else if (destination instanceof FtpDestinationImpl) {
             addArchivingExpressionForm(targetsPanel, (FtpDestinationImpl) destination, new FtpDestinationForm(), true);
         }
     }
 
     //TODO Refactor into factory singleton
     private void addReportPanel(ReportConfigurationImpl destination) {
         if (destination instanceof ConsoleReportConfigurationImpl) {
             ConsoleReportConfigurationImpl cfg = (ConsoleReportConfigurationImpl) destination;
             addArchivingExpressionForm(reportsPanel, cfg, new ConsoleReportConfigurationForm(), true);
         } else if (destination instanceof EmailReportConfigurationImpl) {
             EmailReportConfigurationImpl cfg = (EmailReportConfigurationImpl) destination;
             addArchivingExpressionForm(reportsPanel, cfg, new EmailReportConfigurationForm(), true);
         } else if (destination instanceof RemoteActionsLogFileReportConfigurationImpl) {
             RemoteActionsLogFileReportConfigurationImpl cfg = (RemoteActionsLogFileReportConfigurationImpl) destination;
             addArchivingExpressionForm(reportsPanel, cfg, new RemoteActionsLogFileReportForm(), true);
         } else if (destination instanceof LocalFileSetReportConfigurationImpl) {
             LocalFileSetReportConfigurationImpl cfg = (LocalFileSetReportConfigurationImpl) destination;
             addArchivingExpressionForm(reportsPanel, cfg, new LocalFileSetReportForm(), true);
         }
     }
 
     private void createUIComponents() {
         targetsPanel = createFormLayoutWithRemoveColumn();
         reportsPanel = createFormLayoutWithRemoveColumn();
         comboHistoryFilterLevel = new JComboBox<JobFormLogFilter>(JobFormLogFilter.values());
         comboHistoryFilterLevel.setSelectedItem(GuiUserSettings.getInstance().getJobLogFilter().getValue());
         comboHistoryFilterLevel.setRenderer(new ListCellRenderer<JobFormLogFilter>() {
             @Override
             public Component getListCellRendererComponent(final JList list, final JobFormLogFilter value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                 DefaultListCellRenderer renderer = new DefaultListCellRenderer();
                 JLabel label = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 label.setIcon(value.getIcon());
                 label.setText(value.getLabel());
                 return label;
             }
         });
         labelHistoryNoJobSelected = GuiComponentFactory.label(text("JobForm.no.job.selected"));
         labelHistoryNoJobSelected.setHorizontalAlignment(SwingConstants.CENTER);
     }
 
     @Override
     public JPanel getRootPanel() {
         return jobPanel;
     }
 
     @Override
     public void loadBean(final JobImpl bean) {
         for (Destination destination : bean.getDestinations()) {
             addDestinationPanel(destination);
         }
         for (ReportConfigurationImpl reportConfiguration : bean.getReportConfigurations()) {
             addReportPanel(reportConfiguration);
         }
         FileSetForm form = new FileSetForm();
         loadForm(form, bean.getFileSet());
         GuiComponentFactory.setDefaultPanelMargin(form.getRootPanel());
         sourceFormContainer.setViewportView(form.getRootPanel());
         jobNameTextField.setText(bean.getName());
         uniqueIdentifierTextField.setText(bean.getId().toString());
 
         loadBeanJobHistory(bean);
     }
 
     void loadBeanJobHistory(Job job) {
         try {
             LogEntrySerializer serializer = new LogEntrySerializer();
             Iterator<AbstractEntry> logReader = serializer.getLogReader(new DefaultLogIOFactory().getReader(job));
             Map<String, HistoryListItem> historyItems = new LinkedHashMap<>();
             while (logReader.hasNext()) {
                 AbstractEntry entry = logReader.next();
                 if (entry instanceof AbstractJobExecutionEntry) {
                     AbstractJobExecutionEntry executionEntry = (AbstractJobExecutionEntry) entry;
                     HistoryListItem item = historyItems.get(executionEntry.getExecutionId());
                     if (item != null) {
                         item.lastEntry = executionEntry;
                     } else {
                         item = new HistoryListItem(executionEntry);
                         historyItems.put(executionEntry.getExecutionId(), item);
                     }
                 }
             }
             DefaultListModel<HistoryListItem> model = new DefaultListModel<>();
             for (HistoryListItem entry : historyItems.values()) {
                 model.addElement(entry);
             }
             historyList.setModel(model);
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Override
     public void fillBean(final JobImpl bean) {
         fillForms(targetsPanel, bean.getFtpDestinations(), FtpDestinationImpl.class);
         fillForms(targetsPanel, bean.getLocalFolderDestinations(), LocalFolderDestinationImpl.class);
         fillForms(reportsPanel, bean.getConsoleReportConfigurations(), ConsoleReportConfigurationImpl.class);
         fillForms(reportsPanel, bean.getEmailReportConfigurations(), EmailReportConfigurationImpl.class);
         fillForms(reportsPanel, bean.getLocalFileSetReportConfigurations(), LocalFileSetReportConfigurationImpl.class);
         fillForms(reportsPanel, bean.getRemoteActionsLogFileReportConfigurations(), RemoteActionsLogFileReportConfigurationImpl.class);
         FileSetImpl o = fillForm(sourceFormContainer.getViewport().getView());
         bean.setName(jobNameTextField.getText());
         bean.setId(UUID.fromString(uniqueIdentifierTextField.getText()));
         bean.setFileSet(o);
     }
 
     @Override
     protected JobImpl createModel() {
         return new JobImpl();
     }
 
 }
