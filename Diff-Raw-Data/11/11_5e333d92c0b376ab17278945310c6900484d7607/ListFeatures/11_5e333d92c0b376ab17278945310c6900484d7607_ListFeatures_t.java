 package eu.ist.fears.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import eu.ist.fears.client.interfaceweb.FeatureResumeWidget;
 import eu.ist.fears.common.FearsAsyncCallback;
 import eu.ist.fears.common.State;
 import eu.ist.fears.common.communication.Communication;
 import eu.ist.fears.common.views.ViewFeatureDetailed;
 import eu.ist.fears.common.views.ViewFeatureResume;
 
 public class ListFeatures extends Composite {
 
     protected Communication com;
     protected VerticalPanel sugPanel;
     protected String projectID;
     protected HorizontalPanel search;
     protected HorizontalPanel filter;
     protected HorizontalPanel searchAlertBox;
     protected VerticalPanel featuresList;
     protected ListBox lb;
     protected TextBox sBox;
     protected String actualFilter;
     protected Hyperlink[] filterLinks;
     protected List<FeatureResumeWidget> featuresViews;
 
     public ListFeatures(String projectID, String filter) {
 	com = new Communication("service");
 	sugPanel = new VerticalPanel();
 	search = new HorizontalPanel();
 	this.filter = new HorizontalPanel();
 	featuresList = new VerticalPanel();
 	featuresViews = new ArrayList<FeatureResumeWidget>();
 	this.projectID = projectID;
 	sugPanel.setStyleName("listMain");
 	actualFilter = filter;
 	initWidget(sugPanel);
 
 	HorizontalPanel _searchBox = new HorizontalPanel();
 	_searchBox.setStyleName("searchBox");
 	sugPanel.add(_searchBox);
 	search.setStyleName("search");
 	_searchBox.add(search);
 	HorizontalPanel filterBox = new HorizontalPanel();
 	filterBox.setStyleName("filterBox");
 	filterBox.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 	this.filter.setStyleName("filter");
 	filterBox.add(this.filter);
 	sugPanel.add(filterBox);
 	searchAlertBox = new HorizontalPanel();
 	sugPanel.add(searchAlertBox);
 	searchAlertBox.setStyleName("searchAlertBox");
 	featuresList.setStyleName("featuresList");
 	sugPanel.add(featuresList);
 
 	init();
     }
 
     private void init() {
 	lb = new ListBox();
 	sBox = new TextBox();
 
 	Fears.getPath().setProject("", projectID);
 
 	search.clear();
 
 	search.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
 
 	sBox.setVisibleLength(50);
 	sBox.setStyleName("searchTextBox");
 	search.add(sBox);
 	sBox.addKeyPressHandler(new KeyPressHandler() {
 	    public void onKeyPress(KeyPressEvent event) {
 		if (event.getCharCode() == KeyCodes.KEY_ENTER) {
 		    for (int i = 1; i < filterLinks.length; i++)
 			filterLinks[i].setStyleName("filters");
 		    filterLinks[0].setStyleName("currentFilter");
 		    actualFilter = "";
 		    History.newItem("Project" + projectID, false);
 		    com
 			    .search(projectID, sBox.getText(), lb.getSelectedIndex(), 0, "", Cookies.getCookie("Fears"),
 				    getFeaturesCB);
 		}
 	    }
 	});
 
 	PushButton searchButton = new PushButton(new Image("button01.gif", 0, 0, 105, 32), new Image("button01.gif", -2, -2, 105,
 		32));
 	search.add(searchButton);
 	searchButton.setStyleName("searchButton");
 	searchButton.addClickHandler(new ClickHandler() {
 	    public void onClick(ClickEvent sender) {
 		for (int i = 1; i < filterLinks.length; i++)
 		    filterLinks[i].setStyleName("filters");
 		filterLinks[0].setStyleName("currentFilter");
 		actualFilter = "";
 		History.newItem("Project" + projectID, false);
 		com.search(projectID, sBox.getText(), lb.getSelectedIndex(), 0, "", Cookies.getCookie("Fears"), getFeaturesCB);
 	    }
 	});
 
 	PushButton addButton = new PushButton(new Image("button02.gif", 0, 0, 135, 32),
 		new Image("button02.gif", -2, -2, 135, 32));
 	addButton.setStyleName("addButton");
 	search.add(addButton);
 	addButton.addClickHandler(new ClickHandler() {
 	    public void onClick(ClickEvent sender) {
 		History.newItem("Project" + projectID + "&" + "addFeature");
 	    }
 	});
 
 	filter.clear();
 	filter.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
 	HorizontalPanel filtersTab = new HorizontalPanel();
 	filterLinks = new Hyperlink[5];
 
 	filterLinks[0] = new Hyperlink("Todos", "Project" + projectID);
 	if (actualFilter.equals(""))
 	    filterLinks[0].setStyleName("currentFilter");
 	else
 	    filterLinks[0].setStyleName("filters");
 	filtersTab.add(filterLinks[0]);
 	filtersTab.add(new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;"));
 
 	int i = 1;
 	for (State s : State.values()) {
 	    if (i == 5) { break; }
 	    if (!s.toString().equals(State.Implementacao.toString())) {
 		filterLinks[i] = new Hyperlink(s.getHTML() + "s", "Project" + projectID + "&filter" + s.toString());
 		if (actualFilter.equals(s.toString()))
 		    filterLinks[i].setStyleName("currentFilter");
 		else
 		    filterLinks[i].setStyleName("filters");
 		filtersTab.add(filterLinks[i]);
 	    } else {
 		filterLinks[i] = new Hyperlink(State.Implementacao.getHTML(), true, "Project" + projectID + "&filter"
 			+ State.Implementacao.toString());
 		if (actualFilter.equals(State.Implementacao.toString()))
 		    filterLinks[i].setStyleName("currentFilter");
 		else
 		    filterLinks[i].setStyleName("filters");
 		filtersTab.add(filterLinks[i]);
 	    }
 
 	    if (i != 5)
 		filtersTab.add(new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;"));
 
 	    i++;
 	}
 
 	filter.add(filtersTab);
 	filter.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 
 	lb.addItem("Ordenar por Último Comentário");
 	lb.addItem("Ordenar por Votos");
 	lb.addItem("Ordenar por Data de Criação");
 	lb.setVisibleItemCount(1);
	lb.addChangeHandler(new ChangeHandler() {
 	    
	    @Override
	    public void onChange(ChangeEvent event) {
 		com.search(projectID, sBox.getText(), lb.getSelectedIndex(), 0, actualFilter, Cookies.getCookie("Fears"),
 			getFeaturesCB);
 	    }
 	});
 	filter.add(lb);
 
     }
 
     public void update() {
 	com.search(projectID, "", lb.getSelectedIndex(), 0, actualFilter, Cookies.getCookie("Fears"), getFeaturesCB);
 	com.getProjectName(projectID, getProjectName);
     }
 
     protected void updateProjectName(String name) {
 	Fears.getPath().setProject(name, projectID);
     }
 
     public void updateFeatures(List<ViewFeatureResume> features) {
 	featuresList.clear();
 	featuresViews.clear();
 	searchAlertBox.clear();
 
 	HTML searchAlert;
 
 	if (features == null || features.size() == 0) {
 	    searchAlert = new HTML("N&atilde;o foram encontradas sugest&otilde;es.");
 	    searchAlert.setStylePrimaryName("searchAlert");
 	    searchAlertBox.add(searchAlert);
 	    return;
 	}
 
 	if (actualFilter.length() == 0)
 	    if (sBox.getText().length() == 0)
 		searchAlert = new HTML("O projecto tem " + features.size() + " sugest&otilde;es");
 	    else
 		searchAlert = new HTML("Foram encontradas " + features.size() + " sugest&otilde;es, sobre a pesquisa: \""
 			+ sBox.getText() + "\"");
 	else
 	    searchAlert = new HTML("Foram encontradas " + features.size() + " sugest&otilde;es, com o Estado: " + actualFilter);
 
 	searchAlert.setStylePrimaryName("searchAlert");
 	searchAlertBox.add(searchAlert);
 
 	for (int i = 0; i < features.size(); i++) {
 	    FeatureResumeWidget feature = new FeatureResumeWidget(features.get(i), new VoteOnListCB());
 	    feature.setStylePrimaryName("feature");
 	    featuresList.add(feature);
 	    featuresViews.add(feature);
 	}
     }
 
     public FeatureResumeWidget getFeature(String webID) {
 
 	if (featuresViews == null || featuresViews.size() == 0) {
 	    return null;
 	}
 
 	for (FeatureResumeWidget f : featuresViews) {
 	    if (f.getWebID().equals(webID))
 		return f;
 	}
 	return null;
     }
 
     AsyncCallback<Object> getFeaturesCB = new FearsAsyncCallback<Object>() {
 	@SuppressWarnings("unchecked")
 	public void onSuccess(Object result) {
 	    updateFeatures((List<ViewFeatureResume>) result);
 	}
     };
 
     AsyncCallback<Object> getProjectName = new FearsAsyncCallback<Object>() {
 	public void onSuccess(Object result) {
 	    updateProjectName((String) result);
 	}
     };
 
     protected class VoteOnListCB extends FearsAsyncCallback<Object> {
 
 	protected VoteOnListCB() {
 	}
 
 	public void onSuccess(Object result) {
 	    ViewFeatureDetailed view = (ViewFeatureDetailed) result;
 	    FeatureResumeWidget f = getFeature(new Integer(view.getFeatureID()).toString());
 	    if (f != null) {
 		f.update(view, false);
 	    }
 	    if (featuresViews != null && featuresViews.size() > 0) {
 		Fears.getHeader().update(projectID, featuresViews);
 	    }
 	}
 
     }
 
 }
