 package de.ronnyfriedland.time.ui.dialog;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 import javax.swing.DefaultListSelectionModel;
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
 import de.ronnyfriedland.time.ui.adapter.TimeTableKeyAdapter;
 
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
 	private final JList projects = new JList();
 	private final JButton refresh = new JButton(Messages.REFRESH_PROJECT.getMessage());
 	private final JButton save = new JButton(Messages.SAVE.getMessage());
 	private final JButton delete = new JButton(Messages.DELETE.getMessage());
 
 	private class ProjectData {
 		private final String projectName;
 		private final Integer entryCount;
 
 		public ProjectData(final String projectName, final Integer entryCount) {
 			this.projectName = projectName;
 			this.entryCount = entryCount;
 		}
 
 		@Override
 		public String toString() {
 			return projectName + " (" + entryCount + ")";
 		}
 	}
 
 	private String uuid = null;
 
 	public NewEntryFrame() {
 		super(Messages.CREATE_NEW_ENTRY.getMessage(), 320, 275);
 		createUI();
 
 		loadProjectListData();
 
 	}
 
 	public NewEntryFrame(final Entry entry) {
 		this();
 		uuid = entry.getUuid();
 
 		date.setText(entry.getDateString());
 		description.setText(entry.getDescription());
 		duration.setText(entry.getDuration());
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
 		date.setText(new SimpleDateFormat(Entry.DATESTRINGFORMAT).format(new Date()));
 		date.addKeyListener(new TimeTableKeyAdapter());
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
 		description.addKeyListener(new TimeTableKeyAdapter());
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
 		duration.addKeyListener(new TimeTableKeyAdapter());
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
 
 		projects.setName("projects");
 		projects.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
 		projects.addKeyListener(new TimeTableKeyAdapter());
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
 			public void mouseClicked(final MouseEvent e) {
 				if ((e.getClickCount() == 2) && (null != projects.getSelectedValue())) {
 					Map<String, Object> parameters = new HashMap<String, Object>();
 					parameters.put(Project.PARAM_NAME, ((ProjectData) projects.getSelectedValue()).projectName);
 					Project project = EntityController.getInstance().findSingleResultByParameter(Project.class,
 					        Project.QUERY_FINDBYNAME, parameters);
 
 					new NewProjectFrame(project).setVisible(true);
 					if (StringUtils.isBlank(date.getText()) || StringUtils.isBlank(description.getText())
 					        || StringUtils.isBlank(duration.getText())) {
 						setVisible(false);
 					}
 				}
 			}
 		});
 
 		scrollPaneProjects.setViewportView(projects);
 		scrollPaneProjects.setBounds(10, 85, 300, 100);
 
 		refresh.setBounds(10, 190, 300, 24);
 		refresh.addKeyListener(new TimeTableKeyAdapter());
 		refresh.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent arg0) {
 				loadProjectListData();
 			}
 		});
 
 		save.setBounds(10, 220, 145, 24);
 		save.addKeyListener(new TimeTableKeyAdapter());
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
 					entry.setDuration(duration.getText().replaceAll(",", ".").trim());
 				}
 				ProjectData projectData = (ProjectData) projects.getSelectedValue();
 				if (null != projectData) {
 					Map<String, Object> parameters = new HashMap<String, Object>();
 					parameters.put(Project.PARAM_NAME, projectData.projectName);
 					Project selectedProject = EntityController.getInstance().findSingleResultByParameter(Project.class,
 					        Project.QUERY_FINDBYNAME, parameters);
					selectedProject.addEntry(entry);
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
 
 		delete.setBounds(165, 220, 145, 24);
 		delete.addKeyListener(new TimeTableKeyAdapter());
 		delete.setEnabled(false);
 		delete.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (null != uuid) {
 					try {
 						Entry entry = EntityController.getInstance().findById(Entry.class, uuid);
 						EntityController.getInstance().delete(entry);
 						setVisible(false);
 					} catch (PersistenceException ex) {
 						LOG.log(Level.SEVERE, "Error removing project", ex);
 						formatError(delete);
 					}
 				}
 			}
 		});
 
 		formatOk(date, description, duration, projects);
 
 		getContentPane().add(labelDate);
 		getContentPane().add(date);
 		getContentPane().add(labelDescription);
 		getContentPane().add(description);
 		getContentPane().add(labelDuration);
 		getContentPane().add(duration);
 		getContentPane().add(scrollPaneProjects);
 		getContentPane().add(refresh);
 		getContentPane().add(save);
 		getContentPane().add(delete);
 	}
 
 	private void loadProjectListData() {
 		Collection<Project> projectList = EntityController.getInstance().findAll(Project.class,
 		        new SortParam(Project.PARAM_NAME, SortOrder.ASC));
 		ProjectData[] projectNameList = new ProjectData[projectList.size()];
 		int i = 0;
 		for (Project project : projectList) {
 			projectNameList[i] = new ProjectData(project.getName(), project.getEntries().size());
 			i++;
 		}
 		projects.setListData(projectNameList);
 	}
 
 	public static void main(final String[] args) {
 		new NewEntryFrame().setVisible(true);
 	}
 }
