 package net.alpha01.jwtest.pages.requirement;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import net.alpha01.jwtest.JWTestSession;
 import net.alpha01.jwtest.beans.Project;
 import net.alpha01.jwtest.beans.Requirement;
 import net.alpha01.jwtest.beans.TestCase;
 import net.alpha01.jwtest.component.BookmarkablePageLinkSecure;
 import net.alpha01.jwtest.component.HtmlLabel;
 import net.alpha01.jwtest.dao.ProjectMapper;
 import net.alpha01.jwtest.dao.RequirementMapper;
 import net.alpha01.jwtest.dao.RequirementMapper.RequirementSelectSort;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper.TestCaseSelectSort;
 import net.alpha01.jwtest.jfreechart.BarChartImageResource;
 import net.alpha01.jwtest.pages.LayoutPage;
 import net.alpha01.jwtest.pages.dot.TestCaseDotPage;
 import net.alpha01.jwtest.pages.testcase.AddTestCasePage;
 import net.alpha01.jwtest.panels.ChartPanel;
 import net.alpha01.jwtest.panels.CloseablePanel;
 import net.alpha01.jwtest.panels.attachment.AttachmentPanel;
 import net.alpha01.jwtest.panels.testcase.TestCasesTablePanel;
 import net.alpha01.jwtest.util.JWTestUtil;
 import net.alpha01.jwtest.util.RequirementUtil;
 import net.alpha01.jwtest.util.TestCaseUtil;
 
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.SubmitLink;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.DynamicImageResource;
 
 public class RequirementPage extends LayoutPage {
 	private static final long serialVersionUID = 1L;
 
 	private Requirement req;
 
 	private Model<Requirement> destinationReqModel = new Model<Requirement>();
 	private HashMap<TestCase, Model<Boolean>> selectedTests = new HashMap<TestCase, Model<Boolean>>();
 	private Model<Project> destCopyMovePrjModel=new Model<Project>();
 	
 	public RequirementPage(PageParameters params) {
 		super(params);
 		SqlSessionMapper<RequirementMapper> sesReqMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 		req = sesReqMapper.getMapper().get(BigInteger.valueOf(params.get("idReq").toLong()));
 
 		// LABELS
 		add(new Label("requirementName", new PropertyModel<String>(req, "name")));
 		add(new HtmlLabel("requirementDescription", new PropertyModel<String>(req, "description")));
 
 		// LINK
 		PageParameters reqParams = new PageParameters();
 		reqParams.add("idReq", req.getId().toString());
 		add(new BookmarkablePageLinkSecure<String>("addTestLnk", AddTestCasePage.class, reqParams,Roles.ADMIN,"PROJECT_ADMIN","MANAGER").add(new ContextImage("addTestImg", "images/add_test.png")));
 		add(new BookmarkablePageLinkSecure<String>("delRequirementLnk", DeleteRequirementPage.class, reqParams,Roles.ADMIN,"PROJECT_ADMIN","MANAGER").add(new ContextImage("deleteRequirementImg", "images/delete_requirement.png")));
 		add(new BookmarkablePageLinkSecure<String>("updRequirementLnk", UpdateRequirementPage.class, reqParams,Roles.ADMIN,"PROJECT_ADMIN","MANAGER").add(new ContextImage("updateRequirementImg", "images/update_requirement.png")));
 		
 		//Copy/Move Form
 		Form<Void> copyFrm=new Form<Void>("copyFrm");
 		//Retrieve all projects less current
 		List<Project> allPrj = sesReqMapper.getSqlSession().getMapper(ProjectMapper.class).getAll();
 		allPrj.remove(JWTestSession.getProject());
 		DropDownChoice<Project> prjList = new DropDownChoice<Project>("prjList",destCopyMovePrjModel,allPrj);
 		copyFrm.add(prjList);
 		copyFrm.add(new SubmitLink("copyBtn"){
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void onSubmit() {
 				SqlSessionMapper<RequirementMapper> sesMapper = SqlConnection.getSessionMapper(RequirementMapper.class);
 				if (RequirementUtil.copyRequirement(req, destCopyMovePrjModel.getObject(), sesMapper)){
 					sesMapper.commit();
 				}else{
 					error("SQL Error");
 					sesMapper.rollback();
 				}
 				
 			}
 		});
 		add(copyFrm);
 		
 		// GRAPHS
 		HashMap<String, BigDecimal> dataValues = new HashMap<String, BigDecimal>();
 		TestCaseMapper testMapper = sesReqMapper.getSqlSession().getMapper(TestCaseMapper.class);
 		Iterator<TestCase> itc = testMapper.getAllStat(new TestCaseSelectSort(req.getId(), "name", true)).iterator();
 		while (itc.hasNext()) {
 			TestCase testC = itc.next();
 			if (testC.getNresults().intValue() > 0) {
 				dataValues.put(testC.getName(), testC.getPercSuccess());
 			}
 		}
 		if (dataValues.size() > 0) {
 			final DynamicImageResource resource = new BarChartImageResource("", dataValues, "TestCases", "Success", 600, 300);
 			add (new CloseablePanel("chartPanel","Graph", false){
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public Panel getContentPanel(String id) {
 					return new ChartPanel(id, resource);
 				}
 			});
 			
 		} else {
 			add(new EmptyPanel("chartPanel"));
 		}
 
 		final Model<Boolean> isAuthorized = JWTestUtil.isAuthorized(Roles.ADMIN, "PROJECT_ADMIN", "MANAGER");
 		//ATTACHMENTS TABLE
 		final Model<AttachmentPanel> attachPanelModel=new Model<AttachmentPanel>();
 		CloseablePanel attachmentsPanel;
 		add(attachmentsPanel = new CloseablePanel("attachmentsPanel",JWTestUtil.translate("attachments",this),false){
 			private static final long serialVersionUID = 1L;
 			@Override
 			public Panel getContentPanel(String id) {
 				attachPanelModel.setObject(new AttachmentPanel(id, req, false, isAuthorized.getObject(), isAuthorized.getObject()));
 				return attachPanelModel.getObject();
 			}
 		});
 		if (attachPanelModel.getObject().getSize()==0){
 			attachmentsPanel.setVisible(false);
 		}
 		
 		// TEST TABLE
 		TestCasesTablePanel testsTable;
 		if (isAuthorized.getObject()) {
 			testsTable = new TestCasesTablePanel("testTable", req, 15, new Model<HashMap<TestCase, Model<Boolean>>>(selectedTests));
 		} else {
 			testsTable = new TestCasesTablePanel("testTable", req, 15);
 		}
 
 		// SELECTION FORM
 		Form<String> testsForm = new Form<String>("testsForm");
 		testsForm.add(testsTable);
 
 		// DELETE BUTTON
 		testsForm.add(new Button("deleteBtn") {
 			private static final long serialVersionUID = -1664877195014185574L;
 
 			@Override
 			public boolean isVisible() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public boolean isEnabled() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public void onSubmit() {
 				SqlSessionMapper<TestCaseMapper> sesTestMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 				Iterator<Entry<TestCase, Model<Boolean>>> itT = selectedTests.entrySet().iterator();
 				boolean sqlError = false;
 				try {
 					int nSelTest =0;
 					while (itT.hasNext() && !sqlError) {
 						Entry<TestCase, Model<Boolean>> testBool = itT.next();
 						if (testBool.getValue().getObject().booleanValue()) {
 							nSelTest++;
 							// delete test
 							if (sesTestMapper.getMapper().delete(testBool.getKey()).equals(1)) {
 								info("TestCase " + testBool.getKey().getName() + " deleted");
 							} else {
 								// ERROR
 								error("SQL ERROR in " + testBool.getKey().getName());
 								sqlError = true;
 							}
 						}
 					}
 					if (nSelTest==0){
 						warn(JWTestUtil.translate("testcase.not.selected",this));
 					}
 					if (sqlError) {
 						sesTestMapper.rollback();
 					} else {
 						sesTestMapper.commit();
 					}
 				} catch (PersistenceException e) {
 					sesTestMapper.rollback();
 				}
 				selectedTests.clear();
 			}
 			
 		});
 
 		// MOVE BUTTON
 		List<Requirement> reqs = sesReqMapper.getMapper().getAll(new RequirementSelectSort(getSession().getCurrentProject().getId(), "name", true));
 		sesReqMapper.close();
 
 		testsForm.add(new Button("moveBtn") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isVisible() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public boolean isEnabled() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public void onSubmit() {
 				if (destinationReqModel.getObject() == null) {
 					error("Destination Requirement not selected");
 					return;
 				}
 				SqlSessionMapper<TestCaseMapper> sesTestMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 				Iterator<Entry<TestCase, Model<Boolean>>> itT = selectedTests.entrySet().iterator();
 				boolean sqlOk = true;
 				try {
 					while (itT.hasNext() && sqlOk) {
 						Entry<TestCase, Model<Boolean>> testBool = itT.next();
 						if (testBool.getValue().getObject().booleanValue()) {
 							// move test
 							sqlOk&=TestCaseUtil.moveTestCase(testBool.getKey(), destinationReqModel.getObject(), sesTestMapper);
 						}
 					}
 					if (!sqlOk) {
 						error("SQL Error");
 						sesTestMapper.rollback();
 					} else {
 						sesTestMapper.commit();
 					}
 				} catch (PersistenceException e) {
 					sesTestMapper.rollback();
 				}
 				selectedTests.clear();
 			}
 		});
 		
 		//COPY BUTTON
 		testsForm.add(new Button("copyBtn") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isVisible() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public boolean isEnabled() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public void onSubmit() {
 				if (destinationReqModel.getObject() == null) {
 					error("Destination Requirement not selected");
 					return;
 				}
 				SqlSessionMapper<TestCaseMapper> sesTestMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 				
 				Iterator<Entry<TestCase, Model<Boolean>>> itT = selectedTests.entrySet().iterator();
 				boolean sqlOk = true;
 				try {
 					while (itT.hasNext() && sqlOk) {
 						Entry<TestCase, Model<Boolean>> testBool = itT.next();
 						if (testBool.getValue().getObject().booleanValue()) {
 							// copy test
 							sqlOk&=TestCaseUtil.copyTestCase(testBool.getKey(), destinationReqModel.getObject(), sesTestMapper);
 						}
 					}
 					if (!sqlOk) {
 						sesTestMapper.rollback();
 					} else {
 						sesTestMapper.commit();
 					}
 				} catch (PersistenceException e) {
 					sesTestMapper.rollback();
 				}
 				selectedTests.clear();
 			}
 		});
 
 		reqs.remove(req);
 		DropDownChoice<Requirement> requirementsMoveList = new DropDownChoice<Requirement>("requirementsMoveList", destinationReqModel, reqs){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isVisible() {
 				return isAuthorized.getObject();
 			}
 
 			@Override
 			public boolean isEnabled() {
 				return isAuthorized.getObject();
 			}
 		};
 		testsForm.add(requirementsMoveList);
 		
 		
 		//DOT LINK
 		testsForm.add(new BookmarkablePageLink<String>("dotGraphLnk",TestCaseDotPage.class,new PageParameters().add("idReq",req.getId())));
 		add(testsForm);
 		
 	}
 
 
 }
