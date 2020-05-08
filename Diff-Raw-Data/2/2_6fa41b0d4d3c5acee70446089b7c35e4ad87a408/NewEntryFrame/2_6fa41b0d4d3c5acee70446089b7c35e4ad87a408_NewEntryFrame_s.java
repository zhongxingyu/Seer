 package de.ronnyfriedland.time.ui.dialog;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.ScrollPaneConstants;
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 
 import org.apache.commons.lang.StringUtils;
 
 import de.ronnyfriedland.time.config.Messages;
 import de.ronnyfriedland.time.entity.Entry;
 import de.ronnyfriedland.time.entity.Project;
 import de.ronnyfriedland.time.logic.EntityController;
 import de.ronnyfriedland.time.sort.SortParam;
 import de.ronnyfriedland.time.sort.SortParam.SortOrder;
 
 /**
  * @author ronnyfriedland
  */
 public class NewEntryFrame extends AbstractFrame {
     private static final Logger LOG = Logger.getLogger(NewEntryFrame.class.getName());
 
     private static final long serialVersionUID = -8738367859388084898L;
 
     private final JLabel labelDate = new JLabel(Messages.DATE.getText());
     private final JTextField date = new JTextField();
     private final JLabel labelDescription = new JLabel(Messages.DESCRIPTION.getText());
     private final JTextField description = new JTextField();
     private final JLabel labelDuration = new JLabel(Messages.DURATION.getText());
     private final JTextField duration = new JTextField();
     private final JScrollPane scrollPaneProjects = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
     private final JList projects = new JList();
     private final JButton save = new JButton(Messages.SAVE.getText());
 
     private String uuid = null;
 
     public NewEntryFrame() {
         super(Messages.CREATE_NEW_ENTRY.getText(), 320, 250);
         createUI();
     }
 
     public NewEntryFrame(final Entry entry) {
         this();
         uuid = entry.getUuid();
 
         date.setText(entry.getDateString());
         description.setText(entry.getDescription());
         duration.setText(entry.getDuration());
         ListModel model = projects.getModel();
         for (int i = 0; i < model.getSize(); i++) {
             String item = (String) model.getElementAt(i);
             if (entry.getProject().getName().equals(item)) {
                 projects.setSelectedIndex(i);
             }
         }
     }
 
     /**
      * (non-Javadoc)
      * 
      * @see de.ronnyfriedland.time.ui.dialog.AbstractFrame#createUI()
      */
     @Override
     protected void createUI() {
         // configure
         labelDate.setBounds(10, 10, 100, 24);
 
         date.setName("date");
         date.setBounds(110, 10, 200, 24);
         date.setInputVerifier(new InputVerifier() {
             /**
              * (non-Jsdoc)
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
 
         labelDescription.setBounds(10, 35, 100, 24);
 
         description.setName("description");
         description.setBounds(110, 35, 200, 24);
         description.setInputVerifier(new InputVerifier() {
             /**
              * (non-Javadoc)
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
         duration.setBounds(110, 60, 200, 24);
         duration.setInputVerifier(new InputVerifier() {
             /**
              * (non-Javadoc)
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
 
         Collection<Project> projectList = EntityController.getInstance().findAll(Project.class,
                 new SortParam(Project.PARAM_NAME, SortOrder.ASC));
         String[] projectNameList = new String[projectList.size()];
         int i = 0;
         for (Project project : projectList) {
             projectNameList[i] = project.getName();
             i++;
         }
 
         projects.setName("projects");
         projects.setListData(projectNameList);
         projects.setInputVerifier(new InputVerifier() {
             /**
              * (non-Javadoc)
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
             public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                     Map<String, Object> parameters = new HashMap<String, Object>();
                     parameters.put(Project.PARAM_NAME, projects.getSelectedValue());
                     Project project = EntityController.getInstance().findSingleResultByParameter(Project.class,
                             Project.QUERY_FINDBYNAME, parameters);
 
                     new NewProjectFrame(project).setVisible(true);
                     setVisible(false);
                 }
             }
         });
 
         scrollPaneProjects.setViewportView(projects);
         scrollPaneProjects.setBounds(10, 85, 300, 100);
 
         save.setBounds(10, 190, 300, 24);
 
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 Entry entry;
                 if (null == uuid) {
                     entry = new Entry();
                 } else {
                     entry = new Entry(uuid);
                 }
                 if (!StringUtils.isBlank(date.getText())) {
                     entry.setDateString(date.getText());
                 }
                 if (!StringUtils.isBlank(description.getText())) {
                     entry.setDescription(description.getText());
                 }
                 if (!StringUtils.isBlank(duration.getText())) {
                     entry.setDuration(duration.getText());
                 }
                 String projectName = (String) projects.getSelectedValue();
                 if (!StringUtils.isBlank(projectName)) {
                     Map<String, Object> parameters = new HashMap<String, Object>();
                     parameters.put(Project.PARAM_NAME, projectName);
                     Project selectedProject = EntityController.getInstance().findSingleResultByParameter(Project.class,
                             Project.QUERY_FINDBYNAME, parameters);
                     entry.setProject(selectedProject);
                 }
                 try {
                     if (null != uuid) {
                         EntityController.getInstance().update(entry);
                     } else {
                         EntityController.getInstance().create(entry);
                     }
                     setVisible(false);
                 } catch (PersistenceException ex) {
                     LOG.log(Level.SEVERE, "Error saving new entry", ex);
                     formatOk(save);
                 } catch (ConstraintViolationException ex) {
                     LOG.log(Level.SEVERE, "Error saving new entry", ex);
                     formatError(save);
                 }
             }
         });
 
         formatOk(date, description, duration, scrollPaneProjects);
 
         getContentPane().add(labelDate);
         getContentPane().add(date);
         getContentPane().add(labelDescription);
         getContentPane().add(description);
         getContentPane().add(labelDuration);
         getContentPane().add(duration);
         getContentPane().add(scrollPaneProjects);
         getContentPane().add(save);
     }
 
     public static void main(String[] args) {
         new NewEntryFrame().setVisible(true);
     }
 }
