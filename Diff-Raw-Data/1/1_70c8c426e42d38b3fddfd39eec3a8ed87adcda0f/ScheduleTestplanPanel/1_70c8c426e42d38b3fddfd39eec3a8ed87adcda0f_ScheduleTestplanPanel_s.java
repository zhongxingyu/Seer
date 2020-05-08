 package org.tcrun.slickij.webgroup.plans;
 
 import com.google.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.TestplanResource;
 import org.tcrun.slickij.api.data.Build;
 import org.tcrun.slickij.api.data.ConfigurationOverride;
 import org.tcrun.slickij.api.data.Project;
 import org.tcrun.slickij.api.data.Release;
 import org.tcrun.slickij.api.data.Testplan;
 import org.tcrun.slickij.api.data.TestplanRunParameters;
 import org.tcrun.slickij.api.data.Testrun;
 import org.tcrun.slickij.api.data.dao.ConfigurationDAO;
 import org.tcrun.slickij.api.data.dao.TestplanDAO;
 import org.tcrun.slickij.webbase.SlickijSession;
 import org.tcrun.slickij.webgroup.reports.ReportByTestrunPage;
 
 /**
  *
  * @author jcorbett
  */
 public class ScheduleTestplanPanel extends Panel
 {
 	private ObjectId testplanId;
 	private TestplanRunParametersWithCombinedBuildRelease parameters;
 	
 	@Inject
 	private ConfigurationDAO configDAO;
 
 	@Inject
 	private TestplanResource testplanRestResource;
 
 	@Inject
 	private TestplanDAO tpDao;
 	
 	public ScheduleTestplanPanel(String id, ObjectId testplan)
 	{
 		super(id);
 		this.testplanId = testplan;
 		SlickijSession session = SlickijSession.get();
 		add(new FeedbackPanel("feedback"));
 		add(new Label("testplanname", new PropertyModel<Testplan>(new LoadableDetatchableTestplanModel(tpDao, testplan), "name")));
 
 		parameters = new TestplanRunParametersWithCombinedBuildRelease();
 		Project curProject = session.getCurrentProject();
 		Release defRelease = curProject.findRelease(curProject.getDefaultRelease());
 		Build defBuild = defRelease.findBuild(defRelease.getDefaultBuild());
 		BuildReleaseReferenceCombo defaultReleaseAndBuild = new BuildReleaseReferenceCombo(defBuild.createReference(), defRelease.createReference());
 		parameters.setBuildRelease(defaultReleaseAndBuild);
 		List<BuildReleaseReferenceCombo> builds = new ArrayList<BuildReleaseReferenceCombo>();
 		for(Release release : curProject.getReleases())
 		{
 			for(Build build : release.getBuilds())
 			{
 				builds.add(new BuildReleaseReferenceCombo(build.createReference(), release.createReference()));
 			}
 		}
 		
 		final TextField<String> key = new TextField<String>("overridekey", new Model<String>());
 		final TextField<String> value = new TextField<String>("overridevalue", new Model<String>());
 
 		Form<TestplanRunParameters> scheduleTestplanForm = new Form<TestplanRunParameters>("scheduletestplanform", new Model<TestplanRunParameters>(parameters))
 		{
 			@Override
 			protected void onSubmit()
 			{
 				if(key.getValue() != null && !key.getValue().equals(""))
 				{
 					ConfigurationOverride keyvaluepair = new ConfigurationOverride();
 					keyvaluepair.setKey(key.getValue());
 					keyvaluepair.setValue(value.getValue());
 					parameters.setOverrides(new ArrayList<ConfigurationOverride>());
 					parameters.getOverrides().add(keyvaluepair);
 				}
 				Testrun run = testplanRestResource.runTestPlan(testplanId.toString(), parameters);
 				PageParameters reportParams = new PageParameters();
 				reportParams.add("testrunid", run.getId());
 				setResponsePage(ReportByTestrunPage.class, reportParams);
 			}
 		};
 
 
 		DropDownChoice environments = new DropDownChoice("selectenvironment", new PropertyModel(scheduleTestplanForm.getModel(), "config"), new LoadableDetatchableConfigurationReferenceModel(configDAO), new ChoiceRenderer("name", "configId"));
 		environments.setRequired(true);
 		environments.setLabel(new Model("Environment"));
 
 		DropDownChoice selectBuilds = new DropDownChoice("selectbuild", new PropertyModel(scheduleTestplanForm.getModel(), "buildRelease"), builds, new ChoiceRenderer("name", "id"));
 		selectBuilds.setRequired(true);
 		selectBuilds.setLabel(new Model("Build"));
 
 		scheduleTestplanForm.add(environments);
 		scheduleTestplanForm.add(selectBuilds);
 		scheduleTestplanForm.add(key);
 		scheduleTestplanForm.add(value);
 		scheduleTestplanForm.add(new Button("submitscheduletestplanform"));
 		add(scheduleTestplanForm);
 	}
 	
 }
