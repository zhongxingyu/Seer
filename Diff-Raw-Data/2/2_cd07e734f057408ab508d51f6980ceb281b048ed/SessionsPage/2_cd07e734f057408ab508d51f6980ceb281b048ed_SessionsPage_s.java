 package net.alpha01.jwtest.pages.session;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import net.alpha01.jwtest.beans.Plan;
 import net.alpha01.jwtest.beans.Profile;
 import net.alpha01.jwtest.beans.Session;
 import net.alpha01.jwtest.beans.TestCase;
 import net.alpha01.jwtest.component.AjaxLinkSecure;
 import net.alpha01.jwtest.component.BookmarkablePageLinkSecure;
 import net.alpha01.jwtest.component.HtmlLabel;
 import net.alpha01.jwtest.component.TmpFileDownloadModel;
 import net.alpha01.jwtest.dao.PlanMapper;
 import net.alpha01.jwtest.dao.ProfileMapper;
 import net.alpha01.jwtest.dao.SessionMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper;
 import net.alpha01.jwtest.pages.HomePage;
 import net.alpha01.jwtest.pages.LayoutPage;
 import net.alpha01.jwtest.pages.plan.PlanPage;
 import net.alpha01.jwtest.panels.result.ResultsTablePanel;
 import net.alpha01.jwtest.panels.testcase.TestCasesTablePanel;
 import net.alpha01.jwtest.reports.SessionReport;
 import net.alpha01.jwtest.util.JWTestUtil;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.DownloadLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.EmptyPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import com.itextpdf.text.DocumentException;
 
 public class SessionsPage extends LayoutPage {
 	private Model<Session> sesModel = new Model<Session>(getSession().getCurrentSession());
 	private Session currSession;
 
 	public SessionsPage(PageParameters params) {
 		super();
 		if (getSession().getCurrentProject() == null) {
 			error("Nessun progetto selezionato");
 			setResponsePage(HomePage.class);
 			return;
 		}
 		SqlSessionMapper<SessionMapper> sesMapper = SqlConnection.getSessionMapper(SessionMapper.class);
 		if (params.containsKey("idSession")){
 			//load session based on idSession parameter
 			currSession=sesMapper.getMapper().get(BigInteger.valueOf(params.getAsInteger("idSession")));
 		}
 		if (currSession==null){
 			//load session from WebSession
 			currSession = getSession().getCurrentSession();
 		}
 		
 		//PROFILE
 		if (currSession!=null && currSession.getId_profile()!=null){
 			Profile profile = sesMapper.getSqlSession().getMapper(ProfileMapper.class).get(currSession.getId_profile());
 			add(new HtmlLabel("profileDescription",profile.getDescription()));
 		}else{
 			add(new Label("profileDescription"));
 		}
 		
		if (currSession!=null && currSession.getId_profile()!=null){
 			//PLAN LNK
 			BookmarkablePageLink<String> planLnk=new BookmarkablePageLink<String>("planLnk", PlanPage.class,new PageParameters("idPlan="+currSession.getId_plan()));
 			Plan plan = sesMapper.getSqlSession().getMapper(PlanMapper.class).get(currSession.getId_plan());
 			planLnk.add(new Label("planName",plan.getName()));
 			add(planLnk);
 		}else{
 			Link<Void> planLnk=new Link<Void>("planLnk"){
 				private static final long serialVersionUID = 1L;
 				@Override
 				public void onClick() {	
 				}
 			};
 			planLnk.add(new Label("planName"));
 			add(planLnk);
 		}
 		
 		List<TestCase> testcases = null;
 		if (currSession == null) {
 			add(new EmptyPanel("testcasesTable"));
 			add(new EmptyPanel("resultsTable"));
 		} else {
 			TestCaseMapper testMapper = sesMapper.getSqlSession().getMapper(TestCaseMapper.class);
 			testcases = testMapper.getAllUncheckedBySession(currSession.getId());
 			add(new TestCasesTablePanel("testcasesTable", testcases, 20, currSession.isOpened()));
 			add(new ResultsTablePanel("resultsTable",JWTestUtil.translate("results", this), currSession.getId().intValue(), 10, currSession.isOpened() && JWTestUtil.isAuthorized(Roles.ADMIN, "PROJECT_ADMIN", "MANAGER").getObject()));
 		}
 
 		// Menu
 		AjaxLinkSecure<String> closeLnk = new AjaxLinkSecure<String>("closeLnk", Roles.ADMIN, "PROJECT_ADMIN", "MANAGER", "TESTER") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				SqlSessionMapper<SessionMapper> sesMapper = SqlConnection.getSessionMapper(SessionMapper.class);
 
 				currSession.setEnd_date(GregorianCalendar.getInstance().getTime());
 				if (sesMapper.getMapper().update(currSession).equals(1)) {
 					sesMapper.commit();
 					info("Session closed");
 				} else {
 					sesMapper.rollback();
 					error("SQL Error");
 				}
 				sesMapper.close();
 				setResponsePage(SessionsPage.class);
 			}
 		};
 		closeLnk.add(new ContextImage("closeSessionImg", "images/close_session.png"));
 
 		AjaxLinkSecure<String> reopenLnk = new AjaxLinkSecure<String>("reopenLnk", Roles.ADMIN, "PROJECT_ADMIN", "MANAGER", "TESTER") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				SqlSessionMapper<SessionMapper> sesMapper = SqlConnection.getSessionMapper(SessionMapper.class);
 				currSession.setEnd_date(null);
 				if (sesMapper.getMapper().update(currSession).equals(1)) {
 					sesMapper.commit();
 					info("Session closed");
 				} else {
 					sesMapper.rollback();
 					error("SQL Error");
 				}
 				sesMapper.close();
 				setResponsePage(SessionsPage.class);
 			}
 		};
 
 		reopenLnk.add(new ContextImage("reopenSessionImg", "images/reopen_session.png"));
 		if (currSession == null) {
 			reopenLnk.forceVisible(false);
 			closeLnk.forceVisible(false);
 		} else {
 			if (testcases != null && testcases.size() != 0) {
 				closeLnk.forceVisible(false);
 				reopenLnk.forceVisible(false);
 			}
 			if (!currSession.isOpened()) {
 				closeLnk.forceVisible(false);
 				reopenLnk.forceVisible(true);
 			} else {
 				reopenLnk.forceVisible(false);
 			}
 		}
 		add(reopenLnk);
 		add(closeLnk);
 
 		// Start Session Link
 		if (currSession != null) {
 			add(new BookmarkablePageLinkSecure<String>("startSessionLnk", StartSessionPage.class, Roles.ADMIN, "PROJECT_ADMIN", "MANAGER", "TESTER").add(new ContextImage("addSessionImg", "images/add_session.png")));
 		} else {
 			add((new BookmarkablePageLinkSecure<String>("startSessionLnk", StartSessionPage.class, Roles.ADMIN, "PROJECT_ADMIN", "MANAGER", "TESTER").add(new ContextImage("addSessionImg", "images/add_session.png"))).setVisible(false));
 		}
 
 		// Delete Session Link
 		if (currSession != null) {
 			PageParameters delParams = new PageParameters();
 			delParams.put("idSession", currSession.getId());
 			add(new BookmarkablePageLinkSecure<String>("delLnk", DeleteSessionPage.class, delParams, Roles.ADMIN, "PROJECT_ADMIN", "MANAGER", "TESTER").add(new ContextImage("delSessionImg", "images/delete_session.png")));
 		} else {
 			add((new BookmarkablePageLink<String>("delLnk", DeleteSessionPage.class).add(new ContextImage("delSessionImg", "images/delete_session.png"))).setVisible(false));
 		}
 
 		IModel<File> reportFileModel = new TmpFileDownloadModel() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			protected File getFile() {
 				try {
 					return SessionReport.generateReport(getSession().getCurrentSession());
 				} catch (DocumentException e) {
 					return null;
 				}
 			}
 		};
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
 		String reportFileName="";
 		if (currSession!=null){
 			String strEndDate="";
 			if (currSession.getEnd_date()!=null){
 				strEndDate = sdf.format(currSession.getEnd_date());
 			}
 			reportFileName=getSession().getCurrentProject().getName()+"-testResult-"+currSession.getVersion().replace('.', '_')+"-"+strEndDate+".pdf";
 		}
 		DownloadLink reportLnk = new DownloadLink("reportLnk", reportFileModel,reportFileName);
 		if (currSession == null || (currSession != null && currSession.isOpened())) {
 			reportLnk.setVisible(false);
 			reportLnk.setEnabled(false);
 		}
 
 		reportLnk.add(new ContextImage("reportImg", "images/report.png"));
 		add(reportLnk);
 
 		// FORM
 		Form<Session> selectSessionForm = new Form<Session>("selectSessionForm");
 		List<Session> sessions = sesMapper.getMapper().getAllByProject(getSession().getCurrentProject().getId());
 		DropDownChoice<Session> sessionFld = new DropDownChoice<Session>("sessionFld", sesModel, sessions) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected boolean wantOnSelectionChangedNotifications() {
 				return true;
 			}
 
 			@Override
 			protected void onSelectionChanged(Session newSelection) {
 				Logger.getLogger(getClass()).debug("Session changed:" + newSelection);
 				SessionsPage.this.getSession().setCurrentSession(newSelection);
 				currSession = newSelection;
 				setResponsePage(SessionsPage.class);
 			};
 		};
 
 		selectSessionForm.add(sessionFld);
 		add(selectSessionForm);
 
 		sesMapper.close();
 	}
 
 }
