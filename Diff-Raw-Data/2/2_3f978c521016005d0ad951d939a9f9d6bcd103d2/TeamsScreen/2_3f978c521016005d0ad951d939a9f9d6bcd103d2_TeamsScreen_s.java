 package com.pedrero.eclihand.ui.panel;
 
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.context.annotation.ScopedProxyMode;
 import org.springframework.stereotype.Component;
 
 import com.pedrero.eclihand.controller.panel.TeamsPanelController;
 import com.pedrero.eclihand.model.dto.TeamDto;
 import com.pedrero.eclihand.navigation.EclihandPlace;
 import com.pedrero.eclihand.navigation.EclihandViewImpl;
 import com.pedrero.eclihand.navigation.places.TeamsPlace;
 import com.pedrero.eclihand.ui.table.GenericTable;
 import com.pedrero.eclihand.utils.text.MessageResolver;
 import com.pedrero.eclihand.utils.ui.EclihandLayoutFactory;
 import com.pedrero.eclihand.utils.ui.EclihandUiFactory;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Layout;
 
 @Component
 @Scope(value = BeanDefinition.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
 public class TeamsScreen extends EclihandViewImpl {
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(TeamsScreen.class);
 
 	@Resource
 	private MessageResolver messageResolver;
 
 	@Resource
 	private TeamsPanelController teamsPanelController;
 
 	@Resource(name = "teamTableForTeamsPanel")
 	private GenericTable<TeamDto> teamTable;
 
 	@Resource
 	private EclihandLayoutFactory eclihandLayoutFactory;
 
 	@Resource
 	private EclihandUiFactory eclihandUiFactory;
 
 	@Resource
 	private TeamsPlace teamsPlace;
 
 	private Button createNewTeamButton;
 
 	@Value(value = "${main.panel.width}")
 	private String panelWidth;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5954828103989095039L;
 
 	@PostConstruct
 	protected void postConstruct() {
 		LOGGER.info("initializing TeamsPanel");
 		this.setCaption(messageResolver.getMessage("teams.panel.title"));
 		Layout layout = eclihandLayoutFactory.createCommonVerticalLayout();
 		layout.setWidth(panelWidth);
 
 		this.setContent(layout);
 
 		// this.createNewTeamButton = eclihandUiFactory.createButton();
 		// this.createNewTeamButton.setCaption(messageResolver
 		// .getMessage("players.create.new"));
 		//
 		// this.createNewTeamButton.addClickListener(new ClickListener() {
 		//
 		// /**
 		// *
 		// */
 		// private static final long serialVersionUID = -7117656998497854385L;
 		//
 		// @Override
 		// public void buttonClick(ClickEvent event) {
 		// teamsPanelController.openNewTeamForm();
 		//
 		// }
 		// });
 		//
		// // FIXME : Ajout de la table des quipes
 		// // layout.addComponent(teamTable);
 		// layout.addComponent(createNewTeamButton);
 		//
 		// teamTable.feed(teamsPanelController.searchTeamsToDisplay());
 
 		layout.addComponent(new Label("Toto"));
 		this.setCaption(messageResolver.getMessage("home.caption"));
 	}
 
 	public GenericTable<TeamDto> getTeamsTable() {
 		return teamTable;
 	}
 
 	public void refreshTeams(List<TeamDto> teams) {
 		teamTable.removeAllDataObjects();
 	}
 
 	@Override
 	public EclihandPlace retrieveAssociatedPlace() {
 		return teamsPlace;
 	}
 }
