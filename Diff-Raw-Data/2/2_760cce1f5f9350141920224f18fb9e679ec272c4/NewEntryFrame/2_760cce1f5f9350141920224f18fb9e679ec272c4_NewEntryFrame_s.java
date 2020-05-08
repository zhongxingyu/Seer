 package de.ronnyfriedland.time.ui.dialog;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 
 import org.apache.commons.lang.StringUtils;
 import org.jfree.ui.DateChooserPanel;
 
 import de.ronnyfriedland.time.config.Const;
 import de.ronnyfriedland.time.config.Messages;
 import de.ronnyfriedland.time.entity.Entry;
 import de.ronnyfriedland.time.entity.EntryState;
 import de.ronnyfriedland.time.entity.EntryState.State;
 import de.ronnyfriedland.time.entity.Project;
 import de.ronnyfriedland.time.logic.EntityController;
 import de.ronnyfriedland.time.sort.SortParam;
 import de.ronnyfriedland.time.sort.SortParam.SortOrder;
 import de.ronnyfriedland.time.ui.adapter.TimeTrackKeyAdapter;
 
 /**
  * @author Ronny Friedland
  */
 public class NewEntryFrame extends AbstractFrame {
     private static final Logger LOG = Logger.getLogger(NewEntryFrame.class.getName());
 
     private static final long serialVersionUID = -8738367859388084898L;
 
     private final JLabel labelDate = new JLabel(Messages.DATE.getMessage());
     private final JTextField date = new JTextField();
     private final JLabel labelDescription = new JLabel(Messages.DESCRIPTION.getMessage());
     private final JTextField description = new JTextField();
     private final JLabel labelDuration = new JLabel(Messages.DURATION.getMessage());
     private final JTextField duration = new JTextField();
     private final JScrollPane scrollPaneProjects = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
     private final JCheckBox showDisabledProjects = new JCheckBox(Messages.SHOW_DISABLED_PROJECT.getMessage());
     private final JButton start = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader()
             .getResource("images/start.png")));
     private final JButton stop = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader()
             .getResource("images/stop.png")));
     private final JButton refresh = new JButton(Messages.REFRESH_PROJECT.getMessage());
     private final JButton save = new JButton(Messages.SAVE.getMessage());
     private final JButton delete = new JButton(Messages.DELETE.getMessage());
 
     private final JFrame datechooserFrame = new JFrame();
     private final DateChooserPanel datechooser = new DateChooserPanel();
     private final JButton applyButton = new JButton(Messages.APPLY.getMessage());
 
     private class MyListCellThing extends JLabel implements ListCellRenderer {
         private static final long serialVersionUID = 1L;
 
         public MyListCellThing() {
             setOpaque(true);
         }
 
         @Override
         public Component getListCellRendererComponent(final JList list, final Object value, final int index,
                 final boolean isSelected, final boolean cellHasFocus) {
             ProjectData data = (ProjectData) value;
             if (data.enabled) {
                 setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
                 setForeground(Color.DARK_GRAY);
             } else {
                 setFont(new Font(getFont().getName(), Font.ITALIC, getFont().getSize()));
                 setForeground(Color.GRAY);
             }
             if (isSelected) {
                 setBackground(new Color(184, 207, 229));
             } else {
                 setBackground(Color.WHITE);
             }
             setText(data.projectName + " (" + data.entryCount + ")");
             setToolTipText(data.projectName + " (" + data.description + ")");
             return this;
         }
     }
 
     private class ProjectData {
         public final String projectName;
         public final String description;
         public final Integer entryCount;
         public final Boolean enabled;
 
         public ProjectData(final String projectName, final String description, final boolean enabled,
                 final Integer entryCount) {
             this.projectName = projectName;
             this.enabled = enabled;
             this.entryCount = entryCount;
             if (!StringUtils.isBlank(description)) {
                 this.description = description;
             } else {
                 this.description = StringUtils.EMPTY;
             }
         }
     }
 
     private final JList projects = new JList();
     private String uuid = null;
 
     /**
      * Erzeugt eine neue {@link NewEntryFrame} Instanz.
      */
     public NewEntryFrame() {
         super(Messages.CREATE_NEW_ENTRY.getMessage(), 585, 410, false);
         getContentPane().setBackground(Const.COLOR_BACKGROUND);
         createUI();
         loadProjectListData(false);
     }
 
     public NewEntryFrame(final Entry entry) {
         this();
         uuid = entry.getUuid();
         date.setText(entry.getDateString());
         description.setText(entry.getDescription());
         duration.setText(entry.getCalculatedDuration(new Date()));
         switch (entry.getState().getState()) {
         case OK:
         case WARN:
             duration.setEditable(false);
             start.setEnabled(false);
             break;
         case FIXED:
         case STOPPED:
         default:
             start.setEnabled(false);
             stop.setEnabled(false);
             break;
         }
         ListModel model = projects.getModel();
         for (int i = 0; i < model.getSize(); i++) {
             ProjectData item = (ProjectData) model.getElementAt(i);
             if (entry.getProject().getName().equals(item.projectName)) {
                 projects.setSelectedIndex(i);
             }
         }
         delete.setEnabled(true);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see de.ronnyfriedland.time.ui.dialog.AbstractFrame#createUI()
      */
     @Override
     protected void createUI() {
         getContentPane().addKeyListener(new TimeTrackKeyAdapter());
 
         datechooserFrame.setLayout(new FlowLayout());
         datechooserFrame.setResizable(false);
         datechooserFrame.add(datechooser);
         datechooserFrame.add(applyButton);
 
         datechooser.setChosenDateButtonColor(Const.COLOR_SELECTION);
 
         applyButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 Date selectedDate = datechooser.getDate();
                 date.setText(new SimpleDateFormat(Entry.DATESTRINGFORMAT).format(selectedDate));
                 datechooserFrame.setVisible(false);
             }
         });
 
         labelDate.setBounds(10, 10, 100, 24);
 
         date.setName("date");
         date.setBounds(110, 10, 200, 24);
         date.setText(new SimpleDateFormat(Entry.DATESTRINGFORMAT).format(new Date()));
         date.addKeyListener(new TimeTrackKeyAdapter());
         date.setInputVerifier(new InputVerifier() {
             /**
              * {@inheritDoc}
              * 
              * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
              */
             @Override
             public boolean verify(final JComponent arg0) {
                 ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                 Validator validator = factory.getValidator();
                 Set<ConstraintViolation<Entry>> violations = validator.validateValue(Entry.class, "dateString",
                         ((JTextField) arg0).getText());
                 boolean valid = violations.isEmpty();
                 if (valid) {
                     formatOk(arg0);
                 } else {
                     formatError(arg0);
                 }
                 return valid;
             }
         });
         date.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (2 == e.getClickCount()) {
                    datechooserFrame.setBounds(e.getXOnScreen(), e.getYOnScreen(), 200, 250);
                     datechooserFrame.setVisible(true);
                 }
             }
         });
 
         labelDescription.setBounds(10, 35, 100, 24);
 
         description.setName("description");
         description.setBounds(110, 35, 200, 24);
         description.addKeyListener(new TimeTrackKeyAdapter());
         description.setInputVerifier(new InputVerifier() {
             /**
              * {@inheritDoc}
              * 
              * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
              */
             @Override
             public boolean verify(final JComponent arg0) {
                 ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                 Validator validator = factory.getValidator();
                 Set<ConstraintViolation<Entry>> violations = validator.validateValue(Entry.class, "description",
                         ((JTextField) arg0).getText());
                 boolean valid = violations.isEmpty();
                 if (valid) {
                     formatOk(arg0);
                 } else {
                     formatError(arg0);
                 }
                 return valid;
             }
         });
 
         labelDuration.setBounds(10, 60, 100, 24);
 
         duration.setName("duration");
         duration.setBounds(110, 60, 150, 24);
         duration.setText("0");
         duration.addKeyListener(new TimeTrackKeyAdapter());
         duration.setInputVerifier(new InputVerifier() {
             /**
              * {@inheritDoc}
              * 
              * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
              */
             @Override
             public boolean verify(final JComponent arg0) {
                 ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                 Validator validator = factory.getValidator();
                 Set<ConstraintViolation<Entry>> violations = validator.validateValue(Entry.class, "duration",
                         ((JTextField) arg0).getText());
                 boolean valid = violations.isEmpty();
                 if (valid) {
                     formatOk(arg0);
                 } else {
                     formatError(arg0);
                 }
                 return valid;
             }
         });
         scrollPaneProjects.setBounds(10, 85, 300, 100);
         scrollPaneProjects.setViewportView(projects);
         projects.setCellRenderer(new MyListCellThing());
 
         projects.setName("projects");
         projects.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
         projects.addKeyListener(new TimeTrackKeyAdapter());
         projects.setInputVerifier(new InputVerifier() {
             /**
              * {@inheritDoc}
              * 
              * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
              */
             @Override
             public boolean verify(final JComponent arg0) {
                 ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                 Validator validator = factory.getValidator();
                 Set<ConstraintViolation<Entry>> violations = validator.validateValue(Entry.class, "project",
                         ((JList) arg0).getSelectedValue());
                 boolean valid = violations.isEmpty();
                 if (valid) {
                     formatOk(arg0);
                 } else {
                     formatError(arg0);
                 }
                 return valid;
             }
         });
         projects.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(final MouseEvent e) {
                 if ((e.getClickCount() == 2) && (null != projects.getSelectedValue())) {
                     Map<String, Object> parameters = new HashMap<String, Object>();
                     parameters.put(Project.PARAM_NAME, ((ProjectData) projects.getSelectedValue()).projectName);
                     Project project = EntityController.getInstance().findSingleResultByParameter(Project.class,
                             Project.QUERY_FINDBYNAME, parameters);
                     new NewProjectFrame(project).setVisible(true);
                 }
             }
         });
 
         formatOk(date, description, duration, projects);
         delete.setEnabled(false);
 
         delete.setBounds(165, 245, 145, 24);
         delete.addKeyListener(new TimeTrackKeyAdapter());
         delete.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 if (null != uuid) {
                     try {
                         Entry entry = EntityController.getInstance().findById(Entry.class, uuid);
                         Project project = entry.getProject();
                         project.getEntries().remove(entry);
                         EntityController.getInstance().delete(entry);
                         EntityController.getInstance().update(project);
                         setVisible(false);
                     } catch (PersistenceException ex) {
                         LOG.log(Level.SEVERE, "Error removing project", ex);
                         formatError(delete);
                     }
                 }
             }
         });
 
         save.setBounds(10, 245, 145, 24);
         save.addKeyListener(new TimeTrackKeyAdapter());
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 Entry entry = null;
                 if (null == uuid) {
                     entry = createEntry(date.getText(), description.getText(), duration.getText(), State.FIXED,
                             (ProjectData) projects.getSelectedValue());
                 } else {
                     entry = EntityController.getInstance().findById(Entry.class, uuid);
                     entry = updateEntry(entry, date.getText(), description.getText(), duration.getText(),
                             (ProjectData) projects.getSelectedValue());
                 }
                 if (null != entry) {
                     setVisible(false);
                 }
             }
         });
         start.addKeyListener(new TimeTrackKeyAdapter());
         start.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0) {
                 Entry entry;
                 if (null == uuid) {
                     entry = createEntry(date.getText(), description.getText(), duration.getText(), State.OK,
                             (ProjectData) projects.getSelectedValue());
                     if (null != entry) {
                         setVisible(false);
                     }
                 }
             }
         });
         stop.addKeyListener(new TimeTrackKeyAdapter());
         stop.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0) {
                 Entry entry = null;
                 if (null != uuid) {
                     entry = EntityController.getInstance().findById(Entry.class, uuid);
                     EntryState entryState = entry.getState();
                     if (!State.STOPPED.equals(entryState.getState()) && !State.FIXED.equals(entryState.getState())) {
                         // update entrystate
                         entryState.setEnd(Calendar.getInstance().getTime());
                         entryState.setState(State.STOPPED);
                         // update entry
                         entry.setDuration(EntryState.getDuration(entryState.getStart(), entryState.getEnd(),
                                 entry.getDuration()));
                         EntityController.getInstance().update(entry);
 
                         setVisible(false);
                     }
                 }
             }
         });
         showDisabledProjects.setHorizontalAlignment(SwingConstants.RIGHT);
         showDisabledProjects.setBackground(Const.COLOR_BACKGROUND);
 
         showDisabledProjects.setBounds(10, 185, 300, 24);
 
         refresh.setBounds(10, 215, 300, 24);
         refresh.addKeyListener(new TimeTrackKeyAdapter());
         refresh.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent arg0) {
                 loadProjectListData(showDisabledProjects.isSelected());
             }
         });
         GroupLayout groupLayout = new GroupLayout(getContentPane());
         groupLayout
                 .setHorizontalGroup(groupLayout
                         .createParallelGroup(Alignment.LEADING)
                         .addGroup(
                                 groupLayout
                                         .createSequentialGroup()
                                         .addGap(18)
                                         .addGroup(
                                                 groupLayout
                                                         .createParallelGroup(Alignment.LEADING)
                                                         .addGroup(
                                                                 groupLayout
                                                                         .createSequentialGroup()
                                                                         .addGroup(
                                                                                 groupLayout
                                                                                         .createParallelGroup(
                                                                                                 Alignment.LEADING)
                                                                                         .addGroup(
                                                                                                 groupLayout
                                                                                                         .createSequentialGroup()
                                                                                                         .addComponent(
                                                                                                                 save,
                                                                                                                 GroupLayout.PREFERRED_SIZE,
                                                                                                                 192,
                                                                                                                 GroupLayout.PREFERRED_SIZE)
                                                                                                         .addGap(16)
                                                                                                         .addComponent(
                                                                                                                 start,
                                                                                                                 GroupLayout.PREFERRED_SIZE,
                                                                                                                 24,
                                                                                                                 GroupLayout.PREFERRED_SIZE)
                                                                                                         .addPreferredGap(
                                                                                                                 ComponentPlacement.RELATED,
                                                                                                                 21,
                                                                                                                 Short.MAX_VALUE)
                                                                                                         .addComponent(
                                                                                                                 stop,
                                                                                                                 GroupLayout.PREFERRED_SIZE,
                                                                                                                 24,
                                                                                                                 GroupLayout.PREFERRED_SIZE))
                                                                                         .addComponent(
                                                                                                 delete,
                                                                                                 Alignment.TRAILING,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 277, Short.MAX_VALUE))
                                                                         .addGap(18)
                                                                         .addGroup(
                                                                                 groupLayout
                                                                                         .createParallelGroup(
                                                                                                 Alignment.TRAILING)
                                                                                         .addComponent(
                                                                                                 refresh,
                                                                                                 GroupLayout.PREFERRED_SIZE,
                                                                                                 269,
                                                                                                 GroupLayout.PREFERRED_SIZE)
                                                                                         .addComponent(
                                                                                                 showDisabledProjects,
                                                                                                 GroupLayout.PREFERRED_SIZE,
                                                                                                 263,
                                                                                                 GroupLayout.PREFERRED_SIZE)))
                                                         .addComponent(scrollPaneProjects, GroupLayout.DEFAULT_SIZE,
                                                                 564, Short.MAX_VALUE)
                                                         .addGroup(
                                                                 groupLayout
                                                                         .createSequentialGroup()
                                                                         .addGroup(
                                                                                 groupLayout
                                                                                         .createParallelGroup(
                                                                                                 Alignment.TRAILING,
                                                                                                 false)
                                                                                         .addComponent(
                                                                                                 labelDescription,
                                                                                                 Alignment.LEADING,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 Short.MAX_VALUE)
                                                                                         .addComponent(
                                                                                                 labelDuration,
                                                                                                 Alignment.LEADING,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 265, Short.MAX_VALUE)
                                                                                         .addComponent(
                                                                                                 labelDate,
                                                                                                 Alignment.LEADING,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 Short.MAX_VALUE))
                                                                         .addGap(18)
                                                                         .addGroup(
                                                                                 groupLayout
                                                                                         .createParallelGroup(
                                                                                                 Alignment.TRAILING)
                                                                                         .addComponent(description,
                                                                                                 Alignment.LEADING, 276,
                                                                                                 281, Short.MAX_VALUE)
                                                                                         .addComponent(
                                                                                                 date,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 281, Short.MAX_VALUE)
                                                                                         .addComponent(
                                                                                                 duration,
                                                                                                 Alignment.LEADING,
                                                                                                 GroupLayout.DEFAULT_SIZE,
                                                                                                 281, Short.MAX_VALUE))))
                                         .addContainerGap()));
         groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                 groupLayout
                         .createSequentialGroup()
                         .addGap(20)
                         .addGroup(
                                 groupLayout
                                         .createParallelGroup(Alignment.BASELINE)
                                         .addComponent(labelDate, GroupLayout.PREFERRED_SIZE, 20,
                                                 GroupLayout.PREFERRED_SIZE)
                                         .addComponent(date, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                 GroupLayout.PREFERRED_SIZE))
                         .addGap(18)
                         .addGroup(
                                 groupLayout
                                         .createParallelGroup(Alignment.BASELINE)
                                         .addComponent(description, GroupLayout.PREFERRED_SIZE,
                                                 GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                         .addComponent(labelDescription, GroupLayout.PREFERRED_SIZE, 20,
                                                 GroupLayout.PREFERRED_SIZE))
                         .addGap(18)
                         .addGroup(
                                 groupLayout
                                         .createParallelGroup(Alignment.BASELINE)
                                         .addComponent(labelDuration, GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                                         .addComponent(duration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                 GroupLayout.PREFERRED_SIZE))
                         .addGap(18)
                         .addComponent(scrollPaneProjects, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
                         .addGap(13)
                         .addGroup(
                                 groupLayout
                                         .createParallelGroup(Alignment.LEADING)
                                         .addComponent(start, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                         .addGroup(
                                                 groupLayout
                                                         .createParallelGroup(Alignment.BASELINE, false)
                                                         .addComponent(save, GroupLayout.PREFERRED_SIZE, 25,
                                                                 Short.MAX_VALUE)
                                                         .addComponent(showDisabledProjects, GroupLayout.PREFERRED_SIZE,
                                                                 22, GroupLayout.PREFERRED_SIZE))
                                         .addComponent(stop, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE))
                         .addGap(9)
                         .addGroup(
                                 groupLayout
                                         .createParallelGroup(Alignment.BASELINE)
                                         .addComponent(delete, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)
                                         .addComponent(refresh, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                 Short.MAX_VALUE)).addGap(35)));
         getContentPane().setLayout(groupLayout);
     }
 
     private void loadProjectListData(final boolean includeDisabledProjects) {
         Collection<Project> projectList = EntityController.getInstance().findAll(Project.class,
                 new SortParam(Project.PARAM_NAME, SortOrder.ASC), includeDisabledProjects);
         ProjectData[] projectNameList = new ProjectData[projectList.size()];
         int i = 0;
         for (Project project : projectList) {
             projectNameList[i] = new ProjectData(project.getName(), project.getDescription(), project.getEnabled(),
                     project.getEntries().size());
             i++;
         }
         projects.setListData(projectNameList);
     }
 
     private Entry createEntry(final String date, final String description, final String duration, final State state,
             final ProjectData projectData) {
         Entry result = null;
         try {
             Entry entry = new Entry();
             if (!StringUtils.isBlank(date)) {
                 entry.setDateString(date);
             }
             if (!StringUtils.isBlank(description)) {
                 entry.setDescription(description);
             }
             if (!StringUtils.isBlank(duration)) {
                 entry.setDuration(duration);
             }
             if (null != projectData) {
                 Map<String, Object> parameters = new HashMap<String, Object>();
                 parameters.put(Project.PARAM_NAME, projectData.projectName);
                 Project selectedProject = EntityController.getInstance().findSingleResultByParameter(Project.class,
                         Project.QUERY_FINDBYNAME, parameters);
                 selectedProject.addEntry(entry);
                 entry.setProject(selectedProject);
                 entry.setState(new EntryState(Calendar.getInstance().getTime(), state));
             }
             EntityController.getInstance().create(entry);
             result = entry;
         } catch (PersistenceException ex) {
             LOG.log(Level.SEVERE, "Error creating new entry", ex);
             formatError(getRootPane());
         } catch (ConstraintViolationException ex) {
             LOG.log(Level.SEVERE, "Error creating new entry", ex);
             formatError(getRootPane());
         }
         return result;
     }
 
     private Entry updateEntry(final Entry entry, final String date, final String description, final String duration,
             final ProjectData projectData) {
         Entry result = null;
         try {
             if (!StringUtils.isBlank(date)) {
                 entry.setDateString(date);
             }
             if (!StringUtils.isBlank(description)) {
                 entry.setDescription(description);
             }
             if (!StringUtils.isBlank(duration)) {
                 entry.setDuration(duration);
             }
             if (null != projectData) {
                 Map<String, Object> parameters = new HashMap<String, Object>();
                 parameters.put(Project.PARAM_NAME, projectData.projectName);
                 Project selectedProject = EntityController.getInstance().findSingleResultByParameter(Project.class,
                         Project.QUERY_FINDBYNAME, parameters);
                 if (!entry.getProject().equals(selectedProject)) {
                     entry.getProject().getEntries().remove(entry);
                     selectedProject.addEntry(entry);
                     entry.setProject(selectedProject);
                     EntityController.getInstance().update(entry.getProject());
                 }
             }
             EntityController.getInstance().update(entry);
             result = entry;
         } catch (PersistenceException ex) {
             LOG.log(Level.SEVERE, "Error updating entry", ex);
             formatError(getRootPane());
         } catch (ConstraintViolationException ex) {
             LOG.log(Level.SEVERE, "Error updating entry", ex);
             formatError(getRootPane());
         }
         return result;
     }
 
     public static void main(final String[] args) {
         new NewEntryFrame().setVisible(true);
     }
 }
