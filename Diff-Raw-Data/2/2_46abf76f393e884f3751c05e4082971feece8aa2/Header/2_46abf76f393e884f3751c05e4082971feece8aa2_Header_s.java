 package eu.ist.fears.client.interfaceweb;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 
 import eu.ist.fears.client.DisplayFeatureDetailed;
 import eu.ist.fears.client.Fears;
 import eu.ist.fears.common.FearsAsyncCallback;
 import eu.ist.fears.common.FearsConfigClient;
 import eu.ist.fears.common.communication.Communication;
 import eu.ist.fears.common.views.ViewVoterResume;
 
 public class Header extends Composite {
 
     protected HTML sessionLink;
     protected HTML welcMessage;
     protected HorizontalPanel headerBox;
     protected HTML adminLink;
     protected Hyperlink adminAdministrators;
     protected Communication com;
     protected HTML votes;
 
     public Header() {
 	com = new Communication("service");
 	votes = new HTML();
 	headerBox = new HorizontalPanel();
 	headerBox.setStyleName("headerBox");
 	adminLink = new HTML("&nbsp;<a href=\"Admin.html\">Admin</a>&nbsp;|&nbsp;");
 	adminAdministrators = new Hyperlink("", "");
 
 	HorizontalPanel left = new HorizontalPanel();
 	HorizontalPanel right = new HorizontalPanel();
 	HorizontalPanel header = new HorizontalPanel();
 	sessionLink = new HTML();
 	header.setStyleName("header");
 	headerBox.add(header);
 	header.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 	header.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
 	header.add(left);
 	header.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 	header.add(right);
 	left.setStyleName("left");
 	left.add(new Image("ist_01.gif"));
 	left.add(new HTML("&nbsp;&nbsp;<a href=\"index.html\">FeaRS</a> |"));
 
 	left.add(adminLink);
 	left.add(adminAdministrators);
 
 	right.setStyleName("right");
 	welcMessage = new HTML("Votos de&nbsp;" + Fears.getNickname());
 	right.add(welcMessage);
 	right.add(new HTML("&nbsp;|&nbsp;"));
 	right.add(votes);
 	HorizontalPanel help = new HorizontalPanel();
 	help.add(new Hyperlink("Ajuda", "help"));
 	help.add(new HTML("&nbsp;|&nbsp;"));
 	right.add(help);
 	right.add(sessionLink);
 
 	initWidget(headerBox);
 	update(false, false);
     }
 
     public void update(boolean inProjectPage, boolean adminPage) {
 
 	if (!Fears.isLogedIn()) {
 	    votes.setText("");
 	    sessionLink.setHTML("<a href=\"redirectLogin?service=" + GWT.getHostPageBaseURL()
		    + (adminPage ? "Admin.html" : "index.html") + "\">login</a>");
 	    welcMessage.setHTML("Votos de&nbsp;" + Fears.getNickname());
 	} else {
 	    if (inProjectPage) {
 		votes.setHTML("Tem <b>" + Fears.getVotesLeft() + "</b> votos dispon&iacute;veis&nbsp;|&nbsp;");
 		welcMessage.setHTML("<a href=\"#Project" + Fears.getCurrentProject() + "&" + "viewUser" + Fears.getUserOID()
 			+ "\">" + "Votos de&nbsp;" + Fears.getNickname() + "</a>");
 	    } else {
 		votes.setText("");
 		welcMessage.setHTML("<a href=\"#" + "viewUser" + Fears.getUserOID() + "\">" + "Votos de&nbsp;"
 			+ Fears.getNickname() + "</a>");
 	    }
 	    sessionLink.setHTML("<a href=\"" + FearsConfigClient.getCasUrl() + "logout\">logout</a>");
 	}
 	
 	if(Fears.isAdminUser()){
 	    adminLink.setVisible(true);
 	}else{
 	    adminLink.setVisible(false);
 	}
 
 	if (adminPage && Fears.isAdminUser()) {
 	    adminAdministrators.setText("Administradores");
 	    adminAdministrators.setTargetHistoryToken("admins");
 	} else {
 	    adminAdministrators.setText("");
 	}
 
     }
 
     public void update(String projectID, DisplayFeatureDetailed displayFeatureDetail) {
 	com.getCurrentVoter(projectID, Cookies.getCookie("fears"), new GetCurrentVoter(displayFeatureDetail));
     }
 
     public void update(String projectID, List<FeatureResumeWidget> featureResumeWidgetList) {
 	com.getCurrentVoter(projectID, Cookies.getCookie("fears"), new GetCurrentVoter(featureResumeWidgetList));
     }
 
     public void update(String projectID) {
 	com.getCurrentVoter(projectID, Cookies.getCookie("fears"), new GetCurrentVoter());
     }
 
     protected class GetCurrentVoter extends FearsAsyncCallback<Object> {
 	DisplayFeatureDetailed _d;
 	List<FeatureResumeWidget> _f;
 
 	public GetCurrentVoter() {
 	}
 
 	public GetCurrentVoter(DisplayFeatureDetailed d) {
 	    _d = d;
 	}
 
 	public GetCurrentVoter(List<FeatureResumeWidget> f) {
 	    _f = f;
 	}
 
 	public void onSuccess(Object result) {
 	    ViewVoterResume voter = (ViewVoterResume) result;
 	    if (voter != null) {
 		Fears.validCookie = true;
 		Fears.setCurrentUser(voter);
 		Header.this.update(true, Fears.isAdminPage());
 
 		if (_d != null) {
 		    _d.updateUserInfo();
 		}
 
 		if (_f != null && _f.size() > 0) {
 		    for (FeatureResumeWidget f : _f) {
 			f.updateUserInfo();
 		    }
 
 		}
 	    } else {
 		Fears.validCookie = false;
 		Header.this.update(false, Fears.isAdminPage());
 	    }
 	}
 
     };
 
 }
