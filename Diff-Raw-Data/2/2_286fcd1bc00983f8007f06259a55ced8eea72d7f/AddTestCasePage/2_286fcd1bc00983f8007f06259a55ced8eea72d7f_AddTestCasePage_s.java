 package net.alpha01.jwtest.pages.testcase;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.alpha01.jwtest.beans.Requirement;
 import net.alpha01.jwtest.beans.TestCase;
 import net.alpha01.jwtest.dao.RequirementMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper.Dependency;
 import net.alpha01.jwtest.dao.TestCaseMapper.TestCaseSelectSort;
 import net.alpha01.jwtest.pages.LayoutPage;
 import net.alpha01.jwtest.pages.project.ProjectPage;
 import net.alpha01.jwtest.pages.requirement.RequirementPage;
 import net.alpha01.jwtest.pages.step.AddStepPage;
 
 import org.apache.ibatis.exceptions.PersistenceException;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 @AuthorizeInstantiation(value={Roles.ADMIN,"PROJECT_ADMIN","MANAGER"})
 public class AddTestCasePage extends LayoutPage {
 	private TestCase testCase = new TestCase();
 	private ArrayList<TestCase> dependencies=new ArrayList<TestCase>();
 
 	public AddTestCasePage(final PageParameters params) {
 		super(params);
 		if (!params.containsKey("idReq")) {
 			error("Parametro idReq non trovato");
 			setResponsePage(ProjectPage.class);
 		}
 		RequirementMapper reqMapper = SqlConnection.getMapper(RequirementMapper.class);
 		Requirement req = reqMapper.get(BigInteger.valueOf(params.getInt("idReq")));
 		BookmarkablePageLink<String>requirementLnk=new BookmarkablePageLink<String>("requirementLnk",RequirementPage.class,params);
 		requirementLnk.add(new Label("requirementName", req.getName()));
 		add(requirementLnk);
 		testCase.setId_requirement(req.getId());
 		Form<TestCase> addForm = new Form<TestCase>("addForm");
		addForm.add(new TextField<String>("nameFld", new PropertyModel<String>(testCase, "name")));
 		addForm.add(new TextArea<String>("descriptionFld", new PropertyModel<String>(testCase, "description")));
 		addForm.add(new TextArea<String>("expectedResultFld", new PropertyModel<String>(testCase, "expected_result")));
 		
 		SqlSessionMapper<TestCaseMapper> sesTestMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 		
 		//Dependency
 		List<TestCase> allTests=sesTestMapper.getMapper().getAll(new TestCaseSelectSort(req.getId(), "name", true));
 		sesTestMapper.close();
 		addForm.add(new ListMultipleChoice<TestCase>("dependencyFld",new Model<ArrayList<TestCase>>(dependencies),allTests));
 		
 		addForm.add(new Button("addBtn"){
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void onSubmit() {
 				if(addTestCase()){
 					setResponsePage(AddTestCasePage.class, params);
 				}
 			}
 		});
 		addForm.add(new Button("addUpdBtn"){
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void onSubmit() {
 				if(addTestCase()){
 					setResponsePage(UpdateTestCasePage.class, new PageParameters("idTest="+testCase.getId()));
 				}
 			}
 		});
 		
 		addForm.add(new Button("addAndStepBtn"){
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void onSubmit() {
 				if(addTestCase()){
 					setResponsePage(AddStepPage.class, new PageParameters("idTest="+testCase.getId()));
 				}
 			}
 		});
 		
 		add(addForm);
 	}
 	
 	private boolean addTestCase(){
 		SqlSessionMapper<TestCaseMapper> sesMapper = SqlConnection.getSessionMapper(TestCaseMapper.class);
 		try {
 			if (sesMapper.getMapper().add(testCase).equals(1)) {
 				info("TestCase added");
 				Iterator<TestCase> itd = dependencies.iterator();
 				while (itd.hasNext()){
 					TestCase dep=itd.next();
 					sesMapper.getMapper().addDependency(new Dependency(testCase.getId(), dep.getId()));
 				}
 				sesMapper.commit();
 				sesMapper.close();
 				return true;
 			} else {
 				sesMapper.rollback();
 				sesMapper.close();
 				error("ERROR: TestCase not added");
 				return false;
 			}
 		} catch (PersistenceException e) {
 			e.printStackTrace();
 			error("ERROR: TestCase chiave duplicata");
 			return false;
 		}
 	}
 
 }
