 package net.frontlinesms.plugins.learn.ui.gradebook;
 
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 
 import thinlet.Thinlet;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.events.DatabaseEntityNotification;
 import net.frontlinesms.data.events.EntityDeleteWarning;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.events.EventObserver;
 import net.frontlinesms.events.FrontlineEventNotification;
 import net.frontlinesms.plugins.learn.data.domain.*;
 import net.frontlinesms.plugins.learn.data.gradebook.GradebookService;
 import net.frontlinesms.plugins.learn.data.repository.*;
 import net.frontlinesms.ui.FrontlineUI;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiDestroyEvent;
 import net.frontlinesms.ui.events.FrontlineUiUpdateJob;
 import net.frontlinesms.ui.handler.contacts.GroupSelecterDialog;
 import net.frontlinesms.ui.handler.contacts.SingleGroupSelecterDialogOwner;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 import static net.frontlinesms.ui.i18n.InternationalisationUtils.getI18nString;
 
 public class GradebookTabHandler implements ThinletUiEventHandler, SingleGroupSelecterDialogOwner, EventObserver {
 	private static final String XML_LAYOUT = "/ui/plugins/learn/gradebook/list.xml";
 
 	private final FrontlineUI ui;
 	private final Object tab;
 	private final GroupDao groupDao;
 	private final GradebookService gradebookService;
 	private final TopicDao topicDao;
 	private final AssessmentDao assessmentDao;
 	private final EventBus eventBus;
 
 	private Group selectedGroup;
 	private Topic selectedTopic;
 
 	public GradebookTabHandler(FrontlineUI ui, ApplicationContext ctx) {
 		this.ui = ui;
 		tab = ui.loadComponentFromFile(XML_LAYOUT, this);
 		groupDao = (GroupDao) ctx.getBean("groupDao");
 		gradebookService = (GradebookService) ctx.getBean("gradebookService");
 		topicDao = (TopicDao) ctx.getBean("topicDao");
 		assessmentDao = (AssessmentDao) ctx.getBean("assessmentDao");
 		eventBus = (EventBus) ctx.getBean("eventBus");
 		eventBus.registerObserver(this);
 		
 		// initialise topic chooser combobox
 		refreshTopics();
 	}
 
 	public Object getTab() {
 		return tab;
 	}
 	
 //> UI EVENT METHODS
 	public void selectGroup() {
 		GroupSelecterDialog dialog = new GroupSelecterDialog(ui, this, groupDao);
 		dialog.init(new Group(null, null));
 		dialog.show();
 	}
 	
 	public void assessmentSelected() {
 		Object cbAssessment = find("cbAssessment");
 		Assessment selectedAssessment = ui.getAttachedObject(ui.getSelectedItem(cbAssessment), Assessment.class);
 		setSelectedAssessment(selectedAssessment);
 	}
 	
 	public void topicSelected() {
 		Object cbTopic = find("cbTopic");
 		selectedTopic = ui.getAttachedObject(ui.getSelectedItem(cbTopic), Topic.class);
 		
 		if(selectedTopic == null) {
 			setGroup(selectedGroup);
 		} else {
 			refreshAssessments();
 		}
 	}
 
 //> GROUP SELECTION METHODS
 	public void groupSelectionCompleted(Group group) {
 		setGroup(group);
 	}
 	
 //> EVENT BUS HANDLER
 	public void notify(FrontlineEventNotification n) {
 		if(n instanceof UiDestroyEvent) {
 			if(((UiDestroyEvent) n).isFor(ui)) {
 				eventBus.unregisterObserver(this);
 			}
 		} else if(n instanceof DatabaseEntityNotification<?>) {
 			if(n instanceof EntityDeleteWarning<?>) return;
 			Object entity = ((DatabaseEntityNotification<?>) n).getDatabaseEntity();
 			if(entity instanceof Topic) refreshTopics_threadSafe();
 		}
 	}
 	
 //> UI HELPER METHODS
 	private void refreshAssessments() {
 		List<Assessment> assessments = assessmentDao.findAllByGroupAndTopic(selectedGroup, selectedTopic);
 		Object cbAssessment = find("cbAssessment");
 		ui.removeAll(cbAssessment);
 		if(assessments.size() == 0) {
 			ui.setText(cbAssessment, getI18nString("plugins.learn.assessment.none"));
 			ui.setEnabled(cbAssessment, false);
 			updateTable(null);
 		} else {
 			ui.setEnabled(cbAssessment, true);
 			for(Assessment a : assessments) {
 				ui.add(cbAssessment, createComboboxChoice(a));
 			}
 
 			Assessment selectedAssessment = assessments.get(0);
 			ui.setText(cbAssessment, getDescription(selectedAssessment));
 			setSelectedAssessment(selectedAssessment);
 		}
 	}
 	
 	private void refreshTopics() {
 		Object cbTopic = find("cbTopic");
 		ui.removeAll(cbTopic);
 		ui.add(cbTopic, ui.createComboboxChoice(getI18nString("plugins.learn.topic.all"), null));
 		for(Topic t : topicDao.list()) {
 			ui.add(cbTopic, ui.createComboboxChoice(t.getName(), t));
 		}
 	}
 	
 	private void refreshTopics_threadSafe() {
 		new FrontlineUiUpdateJob() {
 			public void run() {
 				refreshTopics();
 			}
 		}.execute();
 	}
 	
 	private void setSelectedAssessment(Assessment assessment) {
 		AssessmentGradebook gradebook = gradebookService.getForAssessment(assessment);
 		updateTable(gradebook);
 	}
 
 	private void updateTable(AssessmentGradebook gradebook) {
 		// update grade table
 		Object table = find("tbGrades");
 		// update table headers
 		Object header = Thinlet.get(table, Thinlet.HEADER);
 		clearHeader(header);
 		if(gradebook != null) {
 			for(int i=0; i<gradebook.getQuestionCount(); ++i) {
 				ui.add(header, ui.createColumn("Q" + (1+i), null));
 			}
 			ui.add(header, ui.createColumn(getI18nString("plugins.learn.gradebook.score"), null));
 		}
 		// update table content
 		ui.removeAll(table);
 		if(gradebook != null) {
 			// add result entries
 			for(StudentTopicResult r : gradebook.getResults()) {
 				ui.add(table, createRow(r));
 			}
 			// add average entry
 			ui.add(table, createAverageRow(gradebook));
 		}
 	}
 	
 	private Object find(String componentName) {
 		return Thinlet.find(tab, componentName);
 	}
 	
 	private void setGroup(Group g) {
 		selectedGroup = g;
 		
 		// disable and reset assessment selecter
 		Object cbAssessment = find("cbAssessment");
 		ui.setEnabled(cbAssessment, false);
 		ui.setText(cbAssessment, "");
 		
 		// enable and reset topic selecter
 		Object cbTopic = find("cbTopic");
 		ui.setEnabled(cbTopic, true);
 		ui.setText(cbTopic, getI18nString("plugins.learn.topic.all"));
 		
 		// Update group selecter
 		ui.setText(find("tfClass"), g.getName());
 		ClassGradebook gradebook = gradebookService.getForClass(g);
 		
 		// Update table
 		Object table = find("tbGrades");
 
 		// Update table header
 		Object header = Thinlet.get(table, Thinlet.HEADER);
 		clearHeader(header);
 		if(gradebook != null) {
 			for(Topic t : gradebook.getTopics()) {
 				ui.add(header, ui.createColumn(t.getName(), null));
 			}
 			ui.add(header, ui.createColumn(getI18nString("plugins.learn.gradebook.average"), null));
 			
 			// Update table contents
 			for(StudentGrades r : gradebook.getResults()) {
 				ui.add(table, createRow(r));
 			}
 			
 			ui.add(table, createRow(gradebook.getTopicAverages()));
 		}
 	}
 	
 	private Object createRow(int[] averages) {
 		String[] cellText = new String[averages.length + 1];
 		cellText[0] = getI18nString("plugins.learn.gradebook.average");
 		for (int i = 0; i < averages.length; i++) {
 			String grade = averages[i] + "%";
 			cellText[i + 1] = grade;
 		}
 		return ui.createTableRow(null, cellText);
 		
 	}
 
 	private Object createRow(StudentGrades r) {
 		Integer[] grades = r.getGrades();
 		String[] cellText = new String[grades.length + 2];
 		cellText[0] = r.getStudent().getName();
 		for (int i = 0; i < grades.length; i++) {
 			String grade = grades[i] == null
 					? getI18nString("plugins.learn.gradebook.result.none")
 					: grades[i] + "%";
 			cellText[i + 1] = grade;
 		}
 		cellText[cellText.length - 1] = r.getAverage() + "%";
 		return ui.createTableRow(null, cellText);
 	}
 	
 	private Object createAverageRow(AssessmentGradebook gradebook) {
 		String[] text = new String[2 + gradebook.getQuestionCount()];
 		text[0] = getI18nString("plugins.learn.gradebook.average");
 		
 		int[] averages = gradebook.getAverages();
 		for (int i = 0; i < averages.length; i++) {
 			text[i+1] = averages[i] + "%";
 		}
 		return ui.createTableRow(null, text);
 	}
 
 	private Object createRow(StudentTopicResult r) {
 		AssessmentMessageResponse[] responses = r.getResponses();
 		String[] text = new String[responses.length + 2];
 		text[0] = r.getContact().getName();
 		for (int i = 0; i < responses.length; i++) {
 			AssessmentMessageResponse res = responses[i];
 			text[i + 1] = res==null? "": ""+(char)('A' + res.getAnswer());
 		}
 		text[text.length - 1] = r.getScore() + "%";
 		return ui.createTableRow(null, text);
 	}
 	
 	private Object createComboboxChoice(Assessment a) {
 		return ui.createComboboxChoice(getDescription(a), a);
 	}
 
 	private String getDescription(Assessment selectedAssessment) {
 		return formatDate(selectedAssessment.getStartDate()) + " - " +
 				formatDate(selectedAssessment.getEndDate());
 	}
 	
 	private String formatDate(Long date) {
 		if(date == null) return "?";
 		else return InternationalisationUtils.formatDate(date);
 	}
 	
 	private void clearHeader(Object header) {
 		Object[] items = ui.getItems(header);
 		for(int i=1; i<items.length; ++i) {
 			ui.remove(items[i]);
 		}
 	}
 }
