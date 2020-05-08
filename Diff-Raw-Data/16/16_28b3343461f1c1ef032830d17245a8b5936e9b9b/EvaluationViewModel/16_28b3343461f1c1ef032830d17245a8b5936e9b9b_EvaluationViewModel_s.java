 /*
 	This file is part of GeneCommittee.
 
 	GeneCommittee is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	GeneCommittee is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with GeneCommittee.  If not, see <http://www.gnu.org/licenses/>.
 */
 package es.uvigo.ei.sing.gc.view.models.committee.evaluation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
 import java.util.UUID;
 
 import org.hibernate.Session;
 import org.zkoss.bind.BindContext;
 import org.zkoss.bind.BindUtils;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.ContextParam;
 import org.zkoss.bind.annotation.ContextType;
 import org.zkoss.bind.annotation.DependsOn;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.bind.annotation.Init;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.util.Clients;
 import org.zkoss.zul.Messagebox;
 
 import es.uvigo.ei.sing.datatypes.data.Data;
 import es.uvigo.ei.sing.ensembles.evaluation.ExpertsEvaluator;
 import es.uvigo.ei.sing.ensembles.performance.BaseModelOutputEvaluator;
 import es.uvigo.ei.sing.ensembles.training.BaseClassificationProblemGenerator;
 import es.uvigo.ei.sing.ensembles.training.IBaseClassificationProblem;
 import es.uvigo.ei.sing.ensembles.weka.basemodels.WekaBaseModelFactory;
 import es.uvigo.ei.sing.gc.execution.ExecutionEngine;
 import es.uvigo.ei.sing.gc.execution.GlobalEvents;
 import es.uvigo.ei.sing.gc.execution.PerformanceFunction;
 import es.uvigo.ei.sing.gc.execution.Task;
 import es.uvigo.ei.sing.gc.model.entities.ClassifierBuilderMetaData;
 import es.uvigo.ei.sing.gc.model.entities.Committee;
 import es.uvigo.ei.sing.gc.model.entities.CommitteeStatus;
 import es.uvigo.ei.sing.gc.model.entities.ExperimentMetaData;
 import es.uvigo.ei.sing.gc.model.entities.ExperimentStatus;
 import es.uvigo.ei.sing.gc.model.entities.ExpertResult;
 import es.uvigo.ei.sing.gc.model.entities.GeneSetMetaData;
 import es.uvigo.ei.sing.gc.model.entities.User;
 import es.uvigo.ei.sing.gc.utils.HibernateUtil;
 import es.uvigo.ei.sing.gc.view.ZKUtils;
 import es.uvigo.ei.sing.gc.view.models.StatusViewModel;
 import es.uvigo.ei.sing.gc.view.models.committee.CommitteeViewModel;
 import es.uvigo.ei.sing.genomics.genensembles.GFMSubspaceGenerator;
 import es.uvigo.ei.sing.genomics.genensembles.GeneFeatureMatrix;
 
 public class EvaluationViewModel extends CommitteeViewModel {
 	private static final String GC_EXPERT_EXECUTED = "expertExecuted";
 	private static final String GC_EVALUATION_FINISHED = "evaluationFinished";
 	private static final String GC_EVALUATION_ABORTED = "evaluationAborted";
 
 	static {
 		GlobalEvents.registerGlobalCommand(
 			GlobalEvents.EVENT_EXECUTION_SUBTASK_FINISHED, 
 			EvaluationViewModel.GC_EXPERT_EXECUTED
 		);
 		GlobalEvents.registerGlobalCommand(
 			GlobalEvents.EVENT_EXECUTION_FINISHED, 
 			EvaluationViewModel.GC_EVALUATION_FINISHED
 		);
 		GlobalEvents.registerGlobalCommand(
 			GlobalEvents.EVENT_EXECUTION_ABORTED,
 			EvaluationViewModel.GC_EVALUATION_ABORTED
 		);
 	}
 	
 	private PerformanceFunction currentPerformanceFunction;
 	private String currentClass;
 	
 	@Init
 	@Override
 	public void init() {
 		super.init();
 		
 		this.currentPerformanceFunction = PerformanceFunction.KAPPA;
 		this.currentClass = PerformanceFunction.ALL_CLASSES_LABEL;
 		
 		final Committee committee = this.getCommittee(false);
 		
 		final ExperimentMetaData experiment = committee.getExperiment();
 		if (experiment == null) {
 			final Session session = HibernateUtil.currentSession();
 
 			final ExperimentMetaData newExperiment = new ExperimentMetaData(
 				this.getUserId() + " - " + UUID.randomUUID().toString(),
 				committee
 			);
 			
 			session.persist(newExperiment);
 			session.flush();
 			
 			committee.setExperiment(newExperiment);
 			
 			session.update(committee);
 		} else if (experiment.getStatus() == ExperimentStatus.Finished &&
 			experiment.hasExpertResults() && !experiment.hasAnyExpertSelected()
 		) {
 			Clients.showNotification(
 				"Select the experts of your committee clicking on their performance", 
 				Clients.NOTIFICATION_TYPE_INFO, 
 				null, 
 				"middle_center", 
 				0
 			);
 		}
 		
 		
 		StatusViewModel.changeInitialStatus("Execute the evaluation of the classifiers using selected gene sets");	
 	}
 	
 	public boolean getHasExpertResults() {
 		return this.getCommittee().hasExpertResults();
 	}
 	
 	public PerformanceFunction getCurrentPerformanceFunction() {
 		return currentPerformanceFunction;
 	}
 
 	public void setCurrentPerformanceFunction(PerformanceFunction currentPerformanceFunction) {
 		this.currentPerformanceFunction = currentPerformanceFunction;
 		
 		if (!this.currentPerformanceFunction.hasAll() && this.getCurrentClass().equals(PerformanceFunction.ALL_CLASSES_LABEL)) {
 			this.setCurrentClass(this.getClasses().get(1));
 			BindUtils.postNotifyChange(null, null, EvaluationViewModel.this, "currentClass");
 		}
 	}
 
 	public String getCurrentClass() {
 		return currentClass;
 	}
 
 	public void setCurrentClass(String currentClass) {
 		this.currentClass = currentClass;
 	}
 	
 	public List<ClassifierBuilderMetaData> getClassifierBuilders() {
 		final Committee committee = this.getCommittee();
 		
 		if (committee.hasClassifiers()) {
 			return new ArrayList<ClassifierBuilderMetaData>(this.getCommittee().getClassifiers());
 		} else {
 			return Collections.emptyList();
 		}
 	}
 	
 	public ExperimentMetaData getExperiment() {
 		return this.getCommittee().getExperiment();
 	}
 	
 	public boolean isExperimentRunning() {
 		final ExperimentMetaData experiment = this.getExperiment();
 		
 		return experiment != null && experiment.getStatus() == ExperimentStatus.Running;
 	}
 	
 	public boolean isExperimentFinished() {
 		final ExperimentMetaData experiment = this.getExperiment();
 		
 		return experiment != null && experiment.getStatus() == ExperimentStatus.Finished;
 	}
 
 	public List<PerformanceFunction> getPerformanceFunctions() {
 		return Arrays.asList(PerformanceFunction.values());
 	}
 	
 	public List<String> getClasses() {
 		final LinkedList<String> classes = new LinkedList<String>(
 			this.getCommittee().getDataSet().getClassNames()
 		);
 		Collections.sort(classes);
 		classes.push(PerformanceFunction.ALL_CLASSES_LABEL);
 		
 		return classes;
 	}
 	
 	@Command("expertSelected")
 	public void expertSelected(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
 		final Event event = ctx.getTriggerEvent();
 		
 		if (event.getData() instanceof ExpertResult) {
 			final ExpertResult result = (ExpertResult) event.getData();
 			
			HibernateUtil.currentSession().update(result);
 			BindUtils.postNotifyChange(null, null, EvaluationViewModel.this, "evaluationCompleted");
 		}
 	}
 	
 	@Command("execute")
 	@NotifyChange({ "experiment", "experimentRunning", "evaluationCompleted", "experimentFinished"  })
 	public void execute() throws Exception {
 		ZKUtils.safeActivate(Executions.getCurrent().getDesktop());
 		
 		final Committee committee = this.getCommittee();
 		final Integer committeeId = committee.getId();
 		final User user = committee.getUser();
 		
 		if (committee.hasExpertResults()) {
 			Messagebox.show(
 				"The committee has already been evaluated",
 				"Error",
 				Messagebox.OK,
 				Messagebox.ERROR
 			);
 		} else if (this.isUserTaskRunning()) {
 			Messagebox.show(
 				"You already have running tasks",
 				"Error",
 				Messagebox.OK,
 				Messagebox.ERROR
 			);
 		} else {
 			final Integer experimentId = committee.getExperiment().getId();
 			
 			final ExpertsEvaluator<?> evaluator = committee.getEvaluator().build();
 			evaluator.setModelOutputEvaluator(new BaseModelOutputEvaluator());
 			
 			final Data data = committee.getDataSet().loadData();
 			final BaseClassificationProblemGenerator generator = new BaseClassificationProblemGenerator(
 				// Gene id is used as name
 				new GFMSubspaceGenerator(new GeneFeatureMatrix(committee.getGeneMatrix(), data)),
 				// Classifier builders are sorted
 				WekaBaseModelFactory.createMultipleWekaModelFactory(committee.getClassifierBuilders())
 			);
 			final Map<String, String> geneSetIdToName = new HashMap<String, String>();
 			for (GeneSetMetaData gsMetaData : committee.getSelectedGeneSets()) {
 				geneSetIdToName.put(gsMetaData.getGeneSetId(), gsMetaData.getName());
 			}
			final SortedSet<ClassifierBuilderMetaData> classifiers = new TreeSet<ClassifierBuilderMetaData>(
				new ClassifierBuilderMetaData.ClassifierBuilderMetaDataComparator()
			);
			classifiers.addAll(committee.getClassifiers());
 			final String[] classifierNames = new String[classifiers.size()];
 			int i = 0;
 			for (ClassifierBuilderMetaData cbMetaData : classifiers) {
 				classifierNames[i++] = cbMetaData.getName();
 			}
 			
 			final List<ExpertEvaluationSubtask> tasks = 
 				new LinkedList<ExpertEvaluationSubtask>();
 			i = 0;
 			for (final IBaseClassificationProblem problem : generator.generate(data)) {
 				final String builderName = classifierNames[i++%classifierNames.length];
 				
 				tasks.add(new ExpertEvaluationSubtask(builderName, experimentId, evaluator, geneSetIdToName, problem));
 			}
 			
 			final Task<ExpertResult> task = new ExperimentExecutionTask(
 				user.getEmail(), 
 				committeeId, 
 				experimentId, 
 				tasks
 			);
 			
 			ExecutionEngine.getSingleton().execute(task);
 		}
 	}
 	
 	@GlobalCommand(EvaluationViewModel.GC_EVALUATION_FINISHED)
 	@NotifyChange({ "experiment", "experimentRunning", "experimentFinished" })
 	public void evaluationFinished() {
 		Clients.showNotification(
 			"Select the experts of your committee clicking on their performance", 
 			Clients.NOTIFICATION_TYPE_INFO, 
 			null, 
 			"middle_center", 
 			0
 		);
 	}
 	
 	@GlobalCommand(EvaluationViewModel.GC_EVALUATION_ABORTED)
 	@NotifyChange({ "experiment", "experimentRunning", "experimentFinished" })
 	public void evaluationAborted() {}
 	
 	@GlobalCommand(EvaluationViewModel.GC_EXPERT_EXECUTED)
 	@NotifyChange({ "experiment", "experimentRunning" })
 	public void expertExecuted() {}
 	
 	@Override
 	public String getCurrentStep() {
 		return CommitteeViewModel.EVALUATION_STEP;
 	}
 
 	@Override
 	public String getNextHref() {
 		return CommitteeViewModel.SUMMARY_PATH;
 	}
 	
 	@Override
 	public String getPreviousHref() {
 		return CommitteeViewModel.CLASSIFIERS_PATH;
 	}
 	
 	@Override
 	@DependsOn("evaluationCompleted")
 	public boolean getHasNext() {
 		return this.isEvaluationCompleted();
 	}
 
 	@Override
 	protected CommitteeStatus stepStatus() {
 		return CommitteeStatus.EXPERTS_SELECTED;
 	}
 }
