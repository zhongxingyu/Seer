 package ch.creasystem.tennis.client;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import ch.creasystem.tennis.client.login.LoginInfo;
 import ch.creasystem.tennis.client.login.LoginService;
 import ch.creasystem.tennis.client.login.LoginServiceAsync;
 import ch.creasystem.tennis.client.login.NotLoggedInException;
 import ch.creasystem.tennis.client.match.MatchService;
 import ch.creasystem.tennis.client.match.MatchServiceAsync;
 import ch.creasystem.tennis.client.match.MatchValidationException;
 import ch.creasystem.tennis.client.player.PlayerService;
 import ch.creasystem.tennis.client.player.PlayerServiceAsync;
 import ch.creasystem.tennis.client.ranking.RankingService;
 import ch.creasystem.tennis.client.ranking.RankingServiceAsync;
 import ch.creasystem.tennis.shared.match.Match;
 import ch.creasystem.tennis.shared.player.Player;
 import ch.creasystem.tennis.shared.ranking.OponentScore;
 import ch.creasystem.tennis.shared.ranking.PlayerRanking;
 import ch.creasystem.tennis.shared.ranking.ResultRankingList;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.datepicker.client.DateBox;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class TennisCadollesGWT implements EntryPoint {
 
 	private static final int WINNER1 = 0;
 	private static final int WINNER2 = 1;
 	private static final int LOOSER1 = 2;
 	private static final int LOOSER2 = 3;
 
 	// Ranking
 	private Label titelLabel = new Label();
 	private FlexTable rankingFlexTable = new FlexTable();
 
 	// Match
 	private FlexTable matchFlexTable = null;
 
 	// Player
 	private FlexTable playerFlexTable = null;
 	private TextBox firstNamePlayer = new TextBox();
 	private TextBox lastNamePlayer = new TextBox();
 	private TextBox nickNamePlayer = new TextBox();
 	private TextBox emailPlayer = new TextBox();
 	private TextBox userIdPlayer = new TextBox();
 	private Button addPlayerButton = new Button("Ajouter");
 
 	private LoginInfo loginInfo = null;
 	private VerticalPanel loginPanel = new VerticalPanel();
 	private Label loginLabel = new Label("Login Google");
 	private Anchor signInLink = new Anchor("S'identifier");
 	private Anchor signOutLink = new Anchor("Se déconnecter");
 
 	private ArrayList<PlayerRanking> rankingList = null;
 	private final RankingServiceAsync rankingService = GWT
 			.create(RankingService.class);
 	private ArrayList<Player> playerList = null;
 	private HashMap<Long, Player> playerMap = null;
 	private ArrayList<Match> matchList = null;
 	private final PlayerServiceAsync playerService = GWT
 			.create(PlayerService.class);
 	private final MatchServiceAsync matchService = GWT
 			.create(MatchService.class);
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		initNavigation();
 		loadResultRankingList();
 		doLogin();
 
 	}
 
 	private void doLogin() {
 		LoginServiceAsync loginService = GWT.create(LoginService.class);
 		loginService.login(GWT.getHostPageBaseURL(),
 				new AsyncCallback<LoginInfo>() {
 					public void onFailure(Throwable error) {
 						handleError(error);
 					}
 
 					public void onSuccess(LoginInfo result) {
 						loginInfo = result;
 						loadLogin();
 					}
 				});
 	}
 
 	private void initNavigation() {
 		HorizontalPanel hz = new HorizontalPanel();
 		hz.setWidth("600px");
 		Button rankingButton = new Button("Classement", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				loadRankingList();
 			}
 		});
 		Button matchButton = new Button("Matches", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (!checkAuthorised()) {
 					return;
 				}
 				loadServiceMatchList();
 			}
 		});
 		Button playerButton = new Button("Joueurs", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (!checkAuthorised()) {
 					return;
 				}
 				loadServicePlayerList();
 			}
 		});
 		rankingButton.addStyleName("navigationButton");
 		matchButton.addStyleName("navigationButton");
 		playerButton.addStyleName("navigationButton");
 		hz.add(rankingButton);
 		hz.add(matchButton);
 		hz.add(playerButton);
 
 		RootPanel.get("navigation").add(hz);
 	}
 
 	private void loadLogin() {
 		// Assemble login panel.
 		if (loginInfo.isLoggedIn()) {
 			signOutLink.setHref(loginInfo.getLogoutUrl());
 			Label welcome = new Label("Bienvenue " + loginInfo.getNickname()
 					+ " !");
 			loginPanel.add(welcome);
 			loginPanel.add(signOutLink);
 		} else {
 			signInLink.setHref(loginInfo.getLoginUrl());
 			loginLabel.setWidth("150px");
 			loginPanel.setWidth("100%");
 			loginPanel
 					.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			loginLabel.addStyleName("loginPanel");
 
 			loginPanel.add(loginLabel);
 			loginPanel.add(signInLink);
 		}
 		RootPanel.get("login").add(loginPanel);
 	}
 
 	private void loadMatchList() {
 
 		Label matchTitel = new Label("Liste des matches");
 		matchTitel.addStyleName("rankingTitle");
 		VerticalPanel matchPanel = new VerticalPanel();
 		matchPanel.add(matchTitel);
 		matchFlexTable = new FlexTable();
 		matchFlexTable.setText(0, 0, "Date");
 		matchFlexTable.setText(0, 1, "Joueurs");
 		matchFlexTable.setText(0, 2, "Scores");
 		matchFlexTable.getFlexCellFormatter().setColSpan(0, 2, 3);
 		matchFlexTable.setText(0, 3, "Match terminé");
 		matchFlexTable
 				.setHTML(0, 3, new HTML("Match termin&eacute;").getHTML());
 		matchFlexTable.setText(0, 4, "Commentaire");
 		matchFlexTable.setText(0, 5, "Action");
 
 		if (matchList != null) {
 			for (Match match : matchList) {
 				final int row = matchFlexTable.getRowCount();
 				insertMatchRow(match, row);
 			}
 		}
 
 		final int row = matchFlexTable.getRowCount();
 
 		addMatchRow(row);
 
 		matchFlexTable.addStyleName("rankinglisttable");
 		matchPanel.add(matchFlexTable);
 
 		setMainPanel(matchPanel);
 	}
 
 	private void addMatchRow(final int row) {
 		DateBox dateBox = new DateBox();
 		DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");
 		dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
 		dateBox.setValue(new Date());
 		dateBox.setPixelSize(80, 12);
 		matchFlexTable.setWidget(row, 0, dateBox);
 
 		VerticalPanel vPlayer = new VerticalPanel();
 
 		ListBox winner1 = new ListBox();
 		ListBox winner2 = new ListBox();
 		ListBox looser1 = new ListBox();
 		ListBox looser2 = new ListBox();
 
 		HorizontalPanel hzWinner = new HorizontalPanel();
 		winner1.addItem("");
 		for (Player player : playerList) {
 			winner1.addItem(player.getFirstName());
 		}
 		winner2.addItem("");
 		for (Player player : playerList) {
 			winner2.addItem(player.getFirstName());
 		}
 		hzWinner.add(winner1);
 		hzWinner.add(winner2);
 		vPlayer.add(hzWinner);
 
 		HorizontalPanel hzLooser = new HorizontalPanel();
 		looser1.addItem("");
 		for (Player player : playerList) {
 			looser1.addItem(player.getFirstName());
 		}
 		looser2.addItem("");
 		for (Player player : playerList) {
 			looser2.addItem(player.getFirstName());
 		}
 		hzLooser.add(looser1);
 		hzLooser.add(looser2);
 		vPlayer.add(hzLooser);
 
 		matchFlexTable.setWidget(row, 1, vPlayer);
 
 		TextBox[] scoreArray = new TextBox[6];
 		for (int i = 0; i < scoreArray.length; i++) {
 			scoreArray[i] = new TextBox();
 			scoreArray[i].setMaxLength(1);
 			scoreArray[i].setVisibleLength(1);
 		}
 
 		VerticalPanel set1 = new VerticalPanel();
 		set1.add(scoreArray[0]);
 		set1.add(scoreArray[1]);
 		matchFlexTable.setWidget(row, 2, set1);
 
 		VerticalPanel set2 = new VerticalPanel();
 		set2.add(scoreArray[2]);
 		set2.add(scoreArray[3]);
 		matchFlexTable.setWidget(row, 3, set2);
 
 		VerticalPanel set3 = new VerticalPanel();
 		set3.add(scoreArray[4]);
 		set3.add(scoreArray[5]);
 		matchFlexTable.setWidget(row, 4, set3);
 
 		CheckBox completedmatch = new CheckBox();
 		completedmatch
 				.setTitle("Cocher si le match c'est terminé en 2 sets gagnant");
 		TextArea commentaireMatch = new TextArea();
 
 		completedmatch.setValue(true);
 		matchFlexTable.setWidget(row, 5, completedmatch);
 		matchFlexTable.setWidget(row, 6, commentaireMatch);
 		CheckBox sendNotification = new CheckBox();
 		sendNotification.setValue(true);
 		sendNotification
 				.setTitle("Envoi d'une notification aux autres joueurs");
 		matchFlexTable.setWidget(row, 7, sendNotification);
 
 		Button addMatchButton = new Button("ajouter");
 		addMatchButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				addNewMatch();
 
 			}
 		});
 		matchFlexTable.setWidget(row, 8, addMatchButton);
 	}
 
 	private void insertMatchRow(final Match match, int row) {
 		matchFlexTable.insertRow(row);
 		final Long id = new Long(match.getId());
 
 		String winner1;
 		String winner2 = "";
 		String looser1;
 		String looser2 = "";
 
 		if (playerMap.get(match.getWinner().getId()) != null) {
 			winner1 = playerMap.get(match.getWinner().getId()).getFirstName();
 		} else {
 			winner1 = "inconnu(" + match.getWinner().getId() + ")";
 		}
 
 		if (playerMap.get(match.getLooser().getId()) != null) {
 			looser1 = playerMap.get(match.getLooser().getId()).getFirstName();
 		} else {
 			looser1 = "inconnu(" + match.getLooser().getId() + ")";
 		}
 
 		String players;
 		if (match.isDoubleMatch()) {
 			if (playerMap.get(match.getWinner2().getId()) != null) {
 				winner2 = playerMap.get(match.getWinner2().getId())
 						.getFirstName();
 			} else {
 				winner2 = "inconnu(" + match.getWinner2().getId() + ")";
 			}
 			if (playerMap.get(match.getLooser2().getId()) != null) {
 				looser2 = playerMap.get(match.getLooser2().getId())
 						.getFirstName();
 			} else {
 				looser2 = "inconnu(" + match.getLooser2().getId() + ")";
 			}
 			players = winner1 + " " + winner2 + "<br>" + looser1 + " "
 					+ looser2;
 		} else {
 			players = winner1 + "<br>" + looser1;
 		}
 
 		final DateTimeFormat dateFormat = DateTimeFormat
 				.getFormat("dd.MM.yyyy");
 		matchFlexTable.setText(row, 0, dateFormat.format(match.getDate()));
 
 		matchFlexTable.setHTML(row, 1, players);
 		String scores = "";
 		if (match.getScore() != null) {
 			scores = match.getScore().replace("-", "<br/>");
 		}
 		String[] splittedScore = scores.split(" ");
 		if (splittedScore.length != 0 && splittedScore.length <= 3) {
 			for (int i = 0; i < splittedScore.length; i++) {
 				matchFlexTable.setHTML(row, i + 2, splittedScore[i]);
 			}
 
 		} else {
 			matchFlexTable.setHTML(row, 2, match.getScore());
 			matchFlexTable.setHTML(row, 3, "");
 			matchFlexTable.setHTML(row, 4, "");
 		}
 
 		if (match.getValeurMatch() != null && match.getValeurMatch() < 1) {
 			matchFlexTable.setText(row, 5, " ");
 		} else {
 			matchFlexTable.setText(row, 5, "X");
 		}
 		matchFlexTable.setHTML(row, 6, match.getCommentaire());
 
 		Button editMatchButton = new Button("Edit");
 		editMatchButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				displayEditMatch(match);
 			}
 
 		});
 
 		matchFlexTable.setWidget(row, 7, editMatchButton);
 		Button removeStockButton = new Button("x");
 		removeStockButton.addStyleDependentName("remove");
 		removeStockButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (Window.confirm("Veux-tu vraiment effacer le match du "
 						+ dateFormat.format(match.getDate()) + " ?")) {
 					removeMatch(id);
 				}
 
 			}
 		});
 		matchFlexTable.setWidget(row, 8, removeStockButton);
 	}
 
 	private void displayEditMatch(final Match match) {
 		int row = matchList.indexOf(match) + 1;
 
 		// todo initialiser date
 		DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.yyyy");
 		DateBox editDateBox = new DateBox();
 		editDateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
 		editDateBox.setValue(match.getDate());
 		editDateBox.setPixelSize(80, 12);
 		matchFlexTable.setWidget(row, 0, editDateBox);
 
 		VerticalPanel vPlayer = new VerticalPanel();
 
 		HorizontalPanel hzWinner = new HorizontalPanel();
 		ListBox editWinner1 = new ListBox();
 		ListBox editWinner2 = new ListBox();
 		ListBox editLooser1 = new ListBox();
 		ListBox editLooser2 = new ListBox();
 
 		editWinner1.addItem("");
 		for (Player player : playerList) {
 			editWinner1.addItem(player.getFirstName());
 			if (match.getWinner() != null
 					&& match.getWinner().getId() == player.getId()) {
 				editWinner1.setSelectedIndex(playerList.indexOf(player) + 1);
 			}
 		}
 		editWinner2.addItem("");
 		for (Player player : playerList) {
 			editWinner2.addItem(player.getFirstName());
 			if (match.getWinner2() != null
 					&& match.getWinner2().getId() == player.getId()) {
 				editWinner2.setSelectedIndex(playerList.indexOf(player) + 1);
 			}
 		}
 		hzWinner.add(editWinner1);
 		hzWinner.add(editWinner2);
 		vPlayer.add(hzWinner);
 
 		HorizontalPanel hzLooser = new HorizontalPanel();
 		editLooser1.addItem("");
 		for (Player player : playerList) {
 			editLooser1.addItem(player.getFirstName());
 			if (match.getLooser() != null
 					&& match.getLooser().getId() == player.getId()) {
 				editLooser1.setSelectedIndex(playerList.indexOf(player) + 1);
 			}
 		}
 		editLooser2.addItem("");
 		for (Player player : playerList) {
 			editLooser2.addItem(player.getFirstName());
 			if (match.getLooser2() != null
 					&& match.getLooser2().getId() == player.getId()) {
 				editLooser2.setSelectedIndex(playerList.indexOf(player) + 1);
 			}
 		}
 		hzLooser.add(editLooser1);
 		hzLooser.add(editLooser2);
 		vPlayer.add(hzLooser);
 
 		matchFlexTable.setWidget(row, 1, vPlayer);
 
 		TextBox[] editScoreArray = new TextBox[6];
 		for (int i = 0; i < editScoreArray.length; i++) {
 			editScoreArray[i] = new TextBox();
 			editScoreArray[i].setMaxLength(1);
 			editScoreArray[i].setVisibleLength(1);
 		}
 
 		VerticalPanel set1 = new VerticalPanel();
 		set1.add(editScoreArray[0]);
 		set1.add(editScoreArray[1]);
 		matchFlexTable.setWidget(row, 2, set1);
 
 		VerticalPanel set2 = new VerticalPanel();
 		set2.add(editScoreArray[2]);
 
 		set2.add(editScoreArray[3]);
 		matchFlexTable.setWidget(row, 3, set2);
 
 		VerticalPanel set3 = new VerticalPanel();
 		set3.add(editScoreArray[4]);
 		set3.add(editScoreArray[5]);
 		matchFlexTable.setWidget(row, 4, set3);
 
 		// fill up the score
 		String scores = match.getScore();
 		String[] splittedScore = scores.split(" ");
 		if (splittedScore.length != 0 && splittedScore.length <= 3) {
 			int index = 0;
 			for (int i = 0; i < splittedScore.length; i++) {
 				String[] set = splittedScore[i].split("-");
 				editScoreArray[index + i].setValue(set[0]);
 				editScoreArray[index + i + 1].setValue(set[1]);
 				index++;
 			}
 
 		} else {
 			for (int i = 0; i < 6; i++) {
 				editScoreArray[i].setValue("Error");
 			}
 		}
 
 		CheckBox editCompletedMatch = new CheckBox();
 		if (match.getPoint() < 1f) {
 			editCompletedMatch.setValue(false);
 		} else {
 			editCompletedMatch.setValue(true);
 		}
 		matchFlexTable.setWidget(row, 5, editCompletedMatch);
 
 		TextArea editCommentaire = new TextArea();
 		editCommentaire.setValue(match.getCommentaire());
 		matchFlexTable.setWidget(row, 6, editCommentaire);
 		CheckBox sendNotification = new CheckBox();
 		sendNotification
 				.setTitle("Envoi d'une notification aux autres joueurs");
 		matchFlexTable.setWidget(row, 7, sendNotification);
 
 		Button saveMatchButton = new Button("sauver");
 		saveMatchButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				saveEditMatch(match);
 			}
 
 		});
 
 		matchFlexTable.setWidget(row, 8, saveMatchButton);
 
 		Button cancelMatchButton = new Button("annuler");
 		cancelMatchButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				cancelEdiMatch(match);
 			}
 
 		});
 		matchFlexTable.setWidget(row, 9, cancelMatchButton);
 
 	}
 
 	private void cancelEdiMatch(Match match) {
 		int row = matchList.indexOf(match) + 1;
 		this.matchFlexTable.removeRow(row);
 		this.insertMatchRow(match, row);
 
 	}
 
 	private void saveEditMatch(Match match) {
 		try {
 			final int row = matchList.indexOf(match) + 1;
 			// validate Match Fields
 			boolean isDouble = false;
 			Date date = validateDate(row);
 			Player[] playerArray = validatePlayers(row);
 			if (playerArray[WINNER2] != null) {
 				isDouble = true;
 			}
 			String score = validateScore(row);
 			float valeurMatch = validateCompletedMatch(row);
 			String commentaire = validateCommentaire(row);
 			boolean notif = validateSendNotification(row);
 
 			// create the new Match
 			matchService.createMatch(date, playerArray[WINNER1],
 					playerArray[LOOSER1], playerArray[WINNER2],
 					playerArray[LOOSER2], score, valeurMatch, valeurMatch,
 					isDouble, commentaire, notif, new AsyncCallback<Match>() {
 						public void onFailure(Throwable error) {
 							handleError(error);
 						}
 
 						public void onSuccess(Match match) {
 							insertMatchRow(match, row);
 
 							matchList.set(row - 1, match);
 						}
 
 					});
 			matchFlexTable.removeRow(row);
 
 		} catch (MatchValidationException e) {
 			handleError(e);
 			return;
 		}
 
 	}
 
 	private void addNewMatch() {
 		try {
 			final int row = matchFlexTable.getRowCount() - 1;
 			// validate Match Fields
 			boolean isDouble = false;
 			Date date = validateDate(row);
 			Player[] playerArray = validatePlayers(row);
 			if (playerArray[WINNER2] != null) {
 				isDouble = true;
 			}
 			String score = validateScore(row);
 			float valeurMatch = validateCompletedMatch(row);
 			String commentaire = validateCommentaire(row);
 			boolean notif = validateSendNotification(row);
 
 			// create the new Match
 			matchService.createMatch(date, playerArray[WINNER1],
 					playerArray[LOOSER1], playerArray[WINNER2],
 					playerArray[LOOSER2], score, valeurMatch, valeurMatch,
 					isDouble, commentaire, notif, new AsyncCallback<Match>() {
 						public void onFailure(Throwable error) {
 							handleError(error);
 						}
 
 						public void onSuccess(Match match) {
 							final int row = matchFlexTable.getRowCount();
 							insertMatchRow(match, row - 1);
 							matchList.add(match);
 						}
 
 					});
 			matchFlexTable.removeRow(row);
 			addMatchRow(row);
 
 		} catch (MatchValidationException e) {
 			handleError(e);
 			return;
 		}
 
 	}
 
 	private Date validateDate(int row) throws MatchValidationException {
 		// Date validation
 		DateBox addDateBox = (DateBox) matchFlexTable.getWidget(row, 0);
 		if (addDateBox.getValue() == null) {
 			throw new MatchValidationException("Date incorrecte");
 		}
 		return addDateBox.getValue();
 	}
 
 	private Player[] validatePlayers(int row) throws MatchValidationException {
 		Player[] playerArray = new Player[4];
 		boolean isDouble = false;
 		VerticalPanel players = (VerticalPanel) matchFlexTable
 				.getWidget(row, 1);
 		HorizontalPanel hz = (HorizontalPanel) players.getWidget(0);
 		ListBox addWinner1 = (ListBox) hz.getWidget(0);
 		ListBox addWinner2 = (ListBox) hz.getWidget(1);
 		hz = (HorizontalPanel) players.getWidget(1);
 		ListBox addLooser1 = (ListBox) hz.getWidget(0);
 		ListBox addLooser2 = (ListBox) hz.getWidget(1);
 
 		if (addWinner1.getSelectedIndex() == 0) {
 			throw new MatchValidationException("Veuillez choisir le gagnant");
 		}
 
 		// Player validation
 		if (addWinner1.getSelectedIndex() == 0) {
 			throw new MatchValidationException("Veuillez choisir le gagnant");
 		}
 		if (addLooser1.getSelectedIndex() == 0) {
 			throw new MatchValidationException("Veuillez choisir le perdant");
 		}
 		if (addWinner2.getSelectedIndex() == 0) {
 			if (addLooser2.getSelectedIndex() != 0) {
 				throw new MatchValidationException(
 						"Veuiller entrer le 2e gagnant du double");
 			}
 		} else {
 			isDouble = true;
 			if (addLooser2.getSelectedIndex() == 0) {
 				throw new MatchValidationException(
 						"Veuiller entrer le 2e perdant du double");
 			}
 		}
 
 		playerArray[WINNER1] = playerList
 				.get(addWinner1.getSelectedIndex() - 1);
 		playerArray[LOOSER1] = playerList
 				.get(addLooser1.getSelectedIndex() - 1);
 		if (isDouble) {
 			if (addWinner1.getSelectedIndex() == addWinner2.getSelectedIndex()) {
 				throw new MatchValidationException(
 						"Les gagnants du double sont identiques");
 			}
 			if (addLooser1.getSelectedIndex() == addLooser2.getSelectedIndex()) {
 				throw new MatchValidationException(
 						"Les perdants du double sont identiques");
 			}
 			playerArray[WINNER2] = playerList
 					.get(addWinner2.getSelectedIndex() - 1);
 			playerArray[LOOSER2] = playerList
 					.get(addLooser2.getSelectedIndex() - 1);
 		}
 		return playerArray;
 	}
 
 	private float validateCompletedMatch(int row) {
 		CheckBox completedmatch = (CheckBox) matchFlexTable.getWidget(row, 5);
 		float valeurMatch = 0.5f;
 		if (completedmatch.getValue()) {
 			valeurMatch = 1f;
 		}
 		return valeurMatch;
 	}
 
 	private String validateScore(int row) throws MatchValidationException {
 		String score;
 		// score validation
 		TextBox[] addScoreArray = new TextBox[6];
 
 		VerticalPanel scorePanel = (VerticalPanel) matchFlexTable.getWidget(
 				row, 2);
 		addScoreArray[0] = (TextBox) scorePanel.getWidget(0);
 		addScoreArray[1] = (TextBox) scorePanel.getWidget(1);
 		scorePanel = (VerticalPanel) matchFlexTable.getWidget(row, 3);
 		addScoreArray[2] = (TextBox) scorePanel.getWidget(0);
 		addScoreArray[3] = (TextBox) scorePanel.getWidget(1);
 		scorePanel = (VerticalPanel) matchFlexTable.getWidget(row, 4);
 		addScoreArray[4] = (TextBox) scorePanel.getWidget(0);
 		addScoreArray[5] = (TextBox) scorePanel.getWidget(1);
 
 		for (int i = 0; i < addScoreArray.length; i++) {
 			if (!addScoreArray[i].getValue().isEmpty()) {
 				score = addScoreArray[i].getValue();
 				try {
 					Integer.parseInt(score);
 				} catch (NumberFormatException e) {
 					throw new MatchValidationException(
 							"Le score doit être numérique");
 				}
 			}
 		}
 		// first set
 		if (addScoreArray[0].getValue().isEmpty()
 				|| addScoreArray[1].getValue().isEmpty()) {
 			throw new MatchValidationException("Résulat du 1er set incorrect");
 		}
 
 		// second set
 		if (addScoreArray[2].getValue().isEmpty()
 				&& !addScoreArray[3].getValue().isEmpty()) {
 			throw new MatchValidationException("Résulat du 2e set incorrect");
 		}
 		if (!addScoreArray[2].getValue().isEmpty()
 				&& addScoreArray[3].getValue().isEmpty()) {
 			throw new MatchValidationException("Résulat du 2e set incorrect");
 		}
 		// third set
 		if (addScoreArray[4].getValue().isEmpty()
 				&& !addScoreArray[5].getValue().isEmpty()) {
 			throw new MatchValidationException("Résulat du 3e set incorrect");
 		}
 		if (!addScoreArray[4].getValue().isEmpty()
 				&& addScoreArray[5].getValue().isEmpty()) {
 			throw new MatchValidationException("Résulat du 3e set incorrect");
 		}
 
 		// si le match est terminé, il faut au moins 2 sets
 		CheckBox completedmatch = (CheckBox) matchFlexTable.getWidget(row, 5);
 		if (completedmatch.getValue()) {
 
 			if (addScoreArray[2].getValue().isEmpty()
 					|| addScoreArray[3].getValue().isEmpty()) {
 				throw new MatchValidationException(
 						"Le match est défini comme terminé, mais le résultat du 2e set n'est pas complet.");
 			}
 		}
 
 		score = addScoreArray[0].getValue() + "-" + addScoreArray[1].getValue();
 		if (!addScoreArray[2].getValue().isEmpty()) {
 			score = score + " " + addScoreArray[2].getValue() + "-"
 					+ addScoreArray[3].getValue();
 		}
 		if (!addScoreArray[4].getValue().isEmpty()) {
 			score = score + " " + addScoreArray[4].getValue() + "-"
 					+ addScoreArray[5].getValue();
 		}
 		return score;
 	}
 
 	private String validateCommentaire(int row) {
 		TextArea commentaireMatch = (TextArea) matchFlexTable.getWidget(row, 6);
 		return commentaireMatch.getValue();
 	}
 
 	private boolean validateSendNotification(int row) {
 		CheckBox notification = (CheckBox) matchFlexTable.getWidget(row, 7);
 		return notification.getValue();
 	}
 
 	private void displayPlayerList() {
 		VerticalPanel playerPanel = new VerticalPanel();
 		Label playerTitel = new Label("Liste des joueurs");
 		playerTitel.addStyleName("rankingTitle");
 		playerPanel.add(playerTitel);
 
 		playerFlexTable = new FlexTable();
 		playerFlexTable.setText(0, 0, "Prénom");
 		playerFlexTable.setText(0, 1, "Nom");
 		playerFlexTable.setText(0, 2, "Surnom");
 		playerFlexTable.setText(0, 3, "Google Account");
 		playerFlexTable.setText(0, 4, "Notification Mail");
 
 		for (Player player : playerList) {
 			final int row = playerFlexTable.getRowCount();
 			addPlayer(player, row);
 		}
 
 		firstNamePlayer.setFocus(true);
 		final int row = playerFlexTable.getRowCount();
 	//	nickNamePlayer.setWidth("80px");
 		playerFlexTable.setWidget(row, 0, firstNamePlayer);
 		playerFlexTable.setWidget(row, 1, lastNamePlayer);
 		playerFlexTable.setWidget(row, 2, nickNamePlayer);
 		playerFlexTable.setWidget(row, 3, emailPlayer);
 		playerFlexTable.setWidget(row, 4, userIdPlayer);
 		playerFlexTable.setWidget(row, 5, addPlayerButton);
 
 		// Listen for mouse events on the Add button.
 		addPlayerButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				createPlayer();
 			}
 
 		});
 
 		playerFlexTable.addStyleName("rankinglisttable");
 		playerPanel.add(playerFlexTable);
 
 		setMainPanel(playerPanel);
 	}
 
 	private void createPlayer() {
 
 		if ((firstNamePlayer.getValue().isEmpty())
 				|| (lastNamePlayer.getValue().isEmpty())
 				|| (nickNamePlayer.getValue().isEmpty())
 				|| (emailPlayer.getValue().isEmpty())) {
 			Window.alert("Les données du joueur sont incomplètes");
 			return;
 		}
 		playerService.createAPlayer(firstNamePlayer.getValue(),
 				lastNamePlayer.getValue(), nickNamePlayer.getValue(),
 				emailPlayer.getValue(), userIdPlayer.getValue(),
 				new AsyncCallback<Player>() {
 					public void onFailure(Throwable error) {
 						handleError(error);
 					}
 
 					public void onSuccess(Player player) {
 						playerList.add(player);
 						playerMap.put(player.getId(), player);
 						resetAddPlayerField();
 						final int row = playerFlexTable.getRowCount();
 						addPlayer(player, row - 1);
 					}
 
 				});
 	}
 
 	private void resetAddPlayerField() {
 		this.firstNamePlayer.setValue("");
 		this.lastNamePlayer.setValue("");
 		this.nickNamePlayer.setValue("");
 		this.emailPlayer.setValue("");
 		this.userIdPlayer.setValue("");
 
 	}
 
 	private void addPlayer(final Player player, int row) {
 		final Long thePlayerId = new Long(player.getId());
 
 		playerFlexTable.insertRow(row);
 		playerFlexTable.setText(row, 0, player.getFirstName());
 		playerFlexTable.setHTML(row, 1, player.getLastName());
 		playerFlexTable.setHTML(row, 2, player.getNickName());
 		playerFlexTable.setHTML(row, 3, player.getGoogleAccount());
 		playerFlexTable.setHTML(row, 4, player.getNotificationMail());
 
 		Button editPlayerButton = new Button("Edit");
 		editPlayerButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				displayEditPlayer(player);
 			}
 
 		});
 		playerFlexTable.setWidget(row, 5, editPlayerButton);
 
 		Button deletePlayerButton = new Button("x");
 		deletePlayerButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (Window.confirm("Veux-tu vraiment effacer "
 						+ player.getNickName() + " ?")) {
 					removePlayer(thePlayerId);
 				}
 			}
 
 		});
 		playerFlexTable.setWidget(row, 6, deletePlayerButton);
 	}
 
 	private void displayEditPlayer(final Player player) {
 
 		int row = playerList.indexOf(player) + 1;
 
 		TextBox firstname = new TextBox();
 		firstname.setValue(playerFlexTable.getText(row, 0));
 		playerFlexTable.setWidget(row, 0, firstname);
 
 		TextBox lastname = new TextBox();
 		lastname.setValue(playerFlexTable.getText(row, 1));
 		playerFlexTable.setWidget(row, 1, lastname);
 
 		TextBox nickname = new TextBox();
 		nickname.setWidth("80px");
 		nickname.setValue(playerFlexTable.getText(row, 2));
 		playerFlexTable.setWidget(row, 2, nickname);
 
 		TextBox email = new TextBox();
 		email.setValue(playerFlexTable.getText(row, 3));
 		playerFlexTable.setWidget(row, 3, email);
 
 		TextBox userId = new TextBox();
 		userId.setValue(playerFlexTable.getText(row, 4));
 		playerFlexTable.setWidget(row, 4, userId);
 
 		Button savePlayerButton = new Button("sauver");
 		savePlayerButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				savePlayer(player);
 			}
 
 		});
 
 		playerFlexTable.setWidget(row, 5, savePlayerButton);
 
 		Button cancelPlayerButton = new Button("annuler");
 		cancelPlayerButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				cancelEditPlayer(player);
 			}
 
 		});
 		playerFlexTable.setWidget(row, 6, cancelPlayerButton);
 
 	}
 
 	private void cancelEditPlayer(final Player player) {
 		int row = findPlayerInList(player) + 1;
 		playerFlexTable.removeRow(row);
 		addPlayer(player, row);
 	}
 
 	private int findPlayerInList(Player playerToFind) {
 		int count = 0;
 		for (Player player : this.playerList) {
 			if (player.getId().equals(playerToFind.getId())) {
 				return count;
 			}
 			count++;
 		}
 		Window.alert("joueur pas trouvé dans la liste");
 		return 0;
 	}
 
 	private void savePlayer(final Player player) {
 
 		int row = playerList.indexOf(player) + 1;
 
 		player.setFirstName(((TextBox) playerFlexTable.getWidget(row, 0))
 				.getValue());
 		player.setLastName(((TextBox) playerFlexTable.getWidget(row, 1))
 				.getValue());
 		player.setNickName(((TextBox) playerFlexTable.getWidget(row, 2))
 				.getValue());
 		player.setGoogleAccount(((TextBox) playerFlexTable.getWidget(row, 3))
 				.getValue());
 		player.setNotificationMail(((TextBox) playerFlexTable.getWidget(row, 4))
 				.getValue());
 
 		if (player.getFirstName() == null || player.getFirstName().isEmpty()) {
 			displayError("Le prénom est incorrect");
 			return;
 		}
 
 		if (player.getLastName() == null || player.getLastName().isEmpty()) {
 			displayError("Le nom est incorrect");
 			return;
 		}
 
 		if (player.getNickName() == null || player.getNickName().isEmpty()) {
 			displayError("Le surnom est incorrect");
 			return;
 		}
 
 		if (player.getGoogleAccount() == null
 				|| player.getGoogleAccount().isEmpty()) {
 			displayError("L'accompte google est incorrect");
 			return;
 		}
 
 		if (!isValidEmailAddress(player.getGoogleAccount())) {
 			displayError("L'adresse email de l'accompte google est incorrecte");
 			return;
 
 		}
 		if (!isValidEmailAddress(player.getNotificationMail())) {
 			displayError("L'adresse email de notification est incorrecte");
 			return;
 		}
 
 		playerService.updatePlayer(player, new AsyncCallback<Player>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			@Override
 			public void onSuccess(Player result) {
 				int index = playerList.indexOf(player);
 				playerList.set(index, result);
 				cancelEditPlayer(result);
 			}
 
 		});
 	}
 
 	private void removePlayer(Long playerId) {
 
 		final Long thePlayerId = new Long(playerId);
 		playerService.deletePlayer(playerId, new AsyncCallback<Void>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				Player thePlayerToDelete = null;
 
 				for (Player player : playerList) {
 					if (player.getId().equals(thePlayerId)) {
 						thePlayerToDelete = player;
 						break;
 					}
 				}
 				if (thePlayerToDelete != null) {
 					int index = playerList.indexOf(thePlayerToDelete);
 					playerList.remove(index);
 					playerMap.remove(thePlayerToDelete.getId());
 					playerFlexTable.removeRow(index + 1);
 				}
 			}
 		});
 	}
 
 	private void removeMatch(final Long matchId) {
 		matchService.deleteMatch(matchId, new AsyncCallback<Void>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			@Override
 			public void onSuccess(Void result) {
 				Match theMatchToDelete = null;
 
 				for (Match match : matchList) {
 					if (match.getId().equals(matchId)) {
 						theMatchToDelete = match;
 						break;
 					}
 				}
 
 				if (theMatchToDelete != null) {
 					int index = matchList.indexOf(theMatchToDelete);
 					matchList.remove(index);
 					matchFlexTable.removeRow(index + 1);
 				}
 			}
 		});
 	}
 
 	private void initRankingList() {
 		int rowIndex = 2;
 		rankingFlexTable = new FlexTable();
 		rankingFlexTable.setWidth("100%");
 		for (PlayerRanking player : rankingList) {
 			rankingFlexTable.setText(rowIndex, 0, player.getName());
 			rankingFlexTable.getFlexCellFormatter().addStyleName(rowIndex, 0,
 					"rankingColumnTitle");
 
 			int columnIndex = 1;
 
 			for (OponentScore score : player.getOpponentResult().values()) {
 
 				if (score.getPlayerId() == player.getPlayerId()) {
 					rankingFlexTable.setText(rowIndex, columnIndex, "");
 					rankingFlexTable.getFlexCellFormatter().setColSpan(
 							rowIndex, columnIndex, 3);
 					rankingFlexTable.getFlexCellFormatter().addStyleName(
 							rowIndex, columnIndex, "blackcell");
 					columnIndex++;
 
 				} else {
 
 					rankingFlexTable.setText(rowIndex, columnIndex++,
 							score.getTotalPointsStr());
 					rankingFlexTable.setText(rowIndex, columnIndex++,
 							score.getTotalMatchesStr());
 					rankingFlexTable.setText(rowIndex, columnIndex++,
 							score.getTotalPourcentStr() + "%");
 
 				}
 			}
 			rankingFlexTable.getFlexCellFormatter().addStyleName(rowIndex,
 					columnIndex, "total");
 
 			rankingFlexTable.setText(rowIndex, columnIndex++,
 					player.getTotalPourcentStr() + "%");
			String imageName = player.getName().toLowerCase().replace('é', 'e');
			Image image = new Image("/images/"+imageName+"-50.jpg");
 			rankingFlexTable.setWidget(rowIndex, columnIndex, image);
 			rankingFlexTable.getFlexCellFormatter().addStyleName(rowIndex,
 					columnIndex, "imagePro");
 
 			rowIndex++;
 		}
 		int playerColumnIndex = 1;
 		int columnIndex = playerColumnIndex;
 		for (PlayerRanking player : rankingList) {
 			rankingFlexTable.setText(0, playerColumnIndex, player.getName());
 			rankingFlexTable.getFlexCellFormatter().addStyleName(0,
 					playerColumnIndex, "rankingColumnTitle");
 			rankingFlexTable.getFlexCellFormatter().setColSpan(0,
 					playerColumnIndex++, 3);
 			rankingFlexTable.getFlexCellFormatter().addStyleName(1,
 					columnIndex, "rankingColumnSubTitle");
 			rankingFlexTable.setText(1, columnIndex++, "Nbre points");
 			rankingFlexTable.getFlexCellFormatter().addStyleName(1,
 					columnIndex, "rankingColumnSubTitle");
 
 			rankingFlexTable.setText(1, columnIndex++, "Nbre matches");
 			rankingFlexTable.getFlexCellFormatter().addStyleName(1,
 					columnIndex, "rankingColumnSubTitle");
 			rankingFlexTable.setText(1, columnIndex++, "points/matchs");
 
 		}
 		rankingFlexTable.setText(0, playerColumnIndex, "Total");
 		rankingFlexTable.getFlexCellFormatter().addStyleName(0,
 				playerColumnIndex, "rankingColumnTitle");
 		rankingFlexTable.setText(1, columnIndex, "");
 		rankingFlexTable.addStyleName("rankinglisttable");
 
 		VerticalPanel rankingPanel = new VerticalPanel();
 		rankingPanel.setWidth("100%");
 		titelLabel.setText("Classement");
 		titelLabel.addStyleName("rankingTitle");
 		rankingPanel.add(titelLabel);
 		rankingPanel.add(rankingFlexTable);
 
 		setMainPanel(rankingPanel);
 	}
 
 	private void loadRankingList() {
 		rankingService.getPlayerRanking(null,
 				new AsyncCallback<ArrayList<PlayerRanking>>() {
 					public void onFailure(Throwable error) {
 						handleError(error);
 					}
 
 					public void onSuccess(ArrayList<PlayerRanking> ranking) {
 						rankingList = ranking;
 						initRankingList();
 
 					}
 				});
 	}
 
 	private void loadResultRankingList() {
 		rankingService.getResultRanking(null,
 				new AsyncCallback<ResultRankingList>() {
 					public void onFailure(Throwable error) {
 						handleError(error);
 					}
 
 					public void onSuccess(ResultRankingList result) {
 						rankingList = result.getPlayerRanking();
 						playerList = result.getPlayerList();
 						playerMap = new HashMap<Long, Player>();
 						for (Player player : playerList) {
 							playerMap.put(player.getId(), player);
 						}
 
 						initRankingList();
 					}
 				});
 	}
 
 	private void loadServicePlayerList() {
 		playerService.getPlayerList(new AsyncCallback<ArrayList<Player>>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			public void onSuccess(ArrayList<Player> players) {
 				playerList = players;
 				playerMap = new HashMap<Long, Player>();
 				for (Player player : playerList) {
 					playerMap.put(player.getId(), player);
 				}
 
 				displayPlayerList();
 			}
 		});
 	}
 
 	private void loadServiceMatchList() {
 		matchService.getMatchList(null, new AsyncCallback<ArrayList<Match>>() {
 			public void onFailure(Throwable error) {
 				handleError(error);
 			}
 
 			public void onSuccess(ArrayList<Match> matches) {
 				matchList = matches;
 				loadMatchList();
 			}
 		});
 	}
 
 	private boolean isValidEmailAddress(String email) {
 		if (email == null)
 			return false;
 		if (email.matches(".+@.+\\.[a-z]+"))
 			return true;
 		else
 			return false;
 	}
 
 	private void handleError(Throwable error) {
 		Window.alert(error.getMessage());
 		if (error instanceof NotLoggedInException) {
 			Window.Location.replace(loginInfo.getLoginUrl());
 		}
 	}
 
 	private void displayError(String message) {
 		Window.alert(message);
 	}
 
 	private void setMainPanel(Panel panel) {
 		RootPanel main = RootPanel.get("main");
 		main.clear();
 		main.add(panel);
 	}
 
 	private boolean checkAuthorised() {
 		if (!loginInfo.isLoggedIn()) {
 			Window.Location.replace(loginInfo.getLoginUrl());
 			return false;
 		}
 		if (!loginInfo.isAuthorised()) {
 			Window.alert("Fonction non disponible pour l'utilisateur "
 					+ loginInfo.getNickname());
 			return false;
 		}
 		return true;
 
 	}
 }
