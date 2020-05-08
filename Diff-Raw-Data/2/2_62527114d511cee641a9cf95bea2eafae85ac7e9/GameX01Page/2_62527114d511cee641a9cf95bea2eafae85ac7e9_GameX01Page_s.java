 package org.opendarts.prototype.ui.x01.editor;
 
 import java.text.DecimalFormat;
 import java.text.MessageFormat;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.events.IExpansionListener;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.menus.IMenuService;
 import org.opendarts.prototype.ProtoPlugin;
 import org.opendarts.prototype.internal.model.dart.x01.WinningX01DartsThrow;
 import org.opendarts.prototype.internal.model.game.x01.GameX01;
 import org.opendarts.prototype.internal.model.game.x01.GameX01Entry;
 import org.opendarts.prototype.model.dart.IDartsThrow;
 import org.opendarts.prototype.model.game.GameEvent;
 import org.opendarts.prototype.model.game.IGameEntry;
 import org.opendarts.prototype.model.game.IGameListener;
 import org.opendarts.prototype.model.player.IPlayer;
 import org.opendarts.prototype.service.game.IGameService;
 import org.opendarts.prototype.ui.ISharedImages;
 import org.opendarts.prototype.ui.dialog.ThreeDartsComputerDialog;
 import org.opendarts.prototype.ui.utils.FixHeightListener;
 import org.opendarts.prototype.ui.utils.OpenDartsFormsToolkit;
 import org.opendarts.prototype.ui.x01.dialog.DartsComputerX01Dialog;
 import org.opendarts.prototype.ui.x01.label.ScoreLabelProvider;
 import org.opendarts.prototype.ui.x01.label.ToGoLabelProvider;
 import org.opendarts.prototype.ui.x01.label.TurnLabelProvider;
 import org.opendarts.prototype.ui.x01.utils.PlayerStatusComposite;
 import org.opendarts.prototype.ui.x01.utils.TextInputListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class GameX01Page.
  */
 public class GameX01Page extends FormPage implements IFormPage, IGameListener,
 		IExpansionListener {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(GameX01Page.class);
 
 	/** The Constant SCORE_FORMAT. */
 	private static final NumberFormat SCORE_FORMAT = DecimalFormat
 			.getIntegerInstance();
 
 	/** The toolkit. */
 	private OpenDartsFormsToolkit toolkit;
 
 	/** The game. */
 	private final GameX01 game;
 
 	/** The player score. */
 	private final Map<IPlayer, Text> playerScoreLeft;
 
 	/** The player score input. */
 	private final Map<IPlayer, Text> playerScoreInput;
 
 	/** The player score. */
 	private final Map<IPlayer, TableViewerColumn> playerColumn;
 
 	/** The score viewer. */
 	private final Map<IPlayer, TableViewer> scoreViewers;
 
 	/** The game service. */
 	private final IGameService gameService;
 
 	/** The body. */
 	private Composite body;
 
 	/** The managed form. */
 	private IManagedForm mForm;
 
 	/** The dirty. */
 	private boolean dirty;
 
 	/**
 	 * Instantiates a new game page.
 	 *
 	 * @param gameEditor the game editor
 	 * @param game the game
 	 * @param index the index
 	 */
 	public GameX01Page(SetX01Editor gameEditor, GameX01 game, int index) {
 		super(gameEditor, String.valueOf(index), "Game #" + index);
 		this.game = game;
 		this.playerScoreLeft = new HashMap<IPlayer, Text>();
 		this.playerScoreInput = new HashMap<IPlayer, Text>();
 		this.playerColumn = new HashMap<IPlayer, TableViewerColumn>();
 		this.gameService = gameEditor.getSet().getGameService();
 		this.scoreViewers = new HashMap<IPlayer, TableViewer>();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		this.mForm = managedForm;
 		// form
 		ScrolledForm form = managedForm.getForm();
 		this.toolkit = OpenDartsFormsToolkit.getToolkit();
 		form.setText(this.game.getName());
 		this.toolkit.decorateFormHeading(form.getForm());
 
 		GridDataFactory playerData;
 		GridDataFactory scoreData;
 		scoreData = GridDataFactory.fillDefaults().grab(true, false).span(2, 1);
 
 		List<IPlayer> players = this.game.getPlayers();
 		boolean twoPlayer = (players.size() == 2);
 		// body
 		int nbCol;
 		int tableSpan;
 		if (twoPlayer) {
 			nbCol = 4;
 			tableSpan = 2;
 			playerData = GridDataFactory.fillDefaults().grab(false, true);
 			scoreData = GridDataFactory.fillDefaults().grab(true, false)
 					.span(2, 1);
 		} else {
 			nbCol = players.size();
 			tableSpan = nbCol;
 			playerData = GridDataFactory.fillDefaults().grab(false, true);
 			scoreData = GridDataFactory.fillDefaults().grab(true, false);
 		}
 		this.body = form.getBody();
 		GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(nbCol)
 				.equalWidth(true).applyTo(this.body);
 
 		if (twoPlayer) {
 			// First Player Status
 			Composite cmpPlayerOne = this.createPlayerComposite(this.body,
 					this.game.getFirstPlayer());
 			playerData.copy().applyTo(cmpPlayerOne);
 		} else {
 			// create multi player stats
 		}
 
 		// Score
 		Composite cmpScore = this.createScoreTableComposite(this.body);
 		GridDataFactory.fillDefaults().grab(true, true).span(tableSpan, 1)
 				.applyTo(cmpScore);
 
 		if (twoPlayer) {
 			// Second Player Status
 			Composite cmpPlayerTwo = this.createPlayerComposite(this.body,
 					this.game.getSecondPlayer());
 			playerData.copy().applyTo(cmpPlayerTwo);
 		}
 
 		// Left score
 		Composite leftScoreMain = this.createLeftScoreComposite(nbCol,
 				scoreData);
 		GridDataFactory.fillDefaults().grab(true, false).span(nbCol, 1)
 				.applyTo(leftScoreMain);
 
 		// Toolbar
 		ToolBarManager manager = (ToolBarManager) form.getToolBarManager();
 		IMenuService menuService = (IMenuService) this.getSite().getService(
 				IMenuService.class);
 		menuService.populateContributionManager(manager,
 				"toolbar:openwis.editor.game.toolbar");
 		manager.update(true);
 
 		// Register listener
 		this.game.addListener(this);
 
 		// initialize game
 		for (TableViewer viewer : this.scoreViewers.values()) {
 			viewer.setInput(this.game);
 		}
 		this.handlePlayer(this.game.getCurrentPlayer(),
 				this.game.getCurrentEntry());
 	}
 
 	/**
 	 * Creates the left score composite.
 	 *
 	 * @param nbCol the nb col
 	 * @param scoreData the score data
 	 * @return the composite
 	 */
 	private Composite createLeftScoreComposite(int nbCol,
 			GridDataFactory scoreData) {
 		ExpandableComposite leftScoreMain = this.toolkit
 				.createExpandableComposite(this.body,
 						ExpandableComposite.TWISTIE
 								| ExpandableComposite.EXPANDED
 								| ExpandableComposite.NO_TITLE);
 		GridLayoutFactory.fillDefaults().applyTo(leftScoreMain);
 
 		Composite leftScoreBody = this.toolkit.createComposite(leftScoreMain);
 		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 6, 0)
 				.numColumns(nbCol).equalWidth(true).applyTo(leftScoreBody);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftScoreBody);
 
 		Composite cmpPlayerLeftScore;
 		for (IPlayer player : this.game.getPlayers()) {
 			cmpPlayerLeftScore = this.createPlayerScoreLeftComposite(
 					leftScoreBody, player);
 			scoreData.copy().applyTo(cmpPlayerLeftScore);
 		}
 
 		this.toolkit.paintBordersFor(leftScoreBody);
 		leftScoreMain.setClient(leftScoreBody);
 		leftScoreMain.addExpansionListener(this);
 		return leftScoreMain;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#setFocus()
 	 */
 	@Override
 	public void setFocus() {
 		Text text = this.playerScoreInput.get(this.game.getCurrentPlayer());
 		if (text != null) {
 			text.setFocus();
 		} else {
 			super.setFocus();
 		}
 	}
 
 	/**
 	 * Creates the score composite.
 	 *
 	 * @param parent the parent
 	 * @return the composite
 	 */
 	private Composite createScoreTableComposite(Composite parent) {
 		Composite main = this.toolkit.createComposite(parent);
 
 		List<IPlayer> players = this.game.getPlayers();
 		int nbPlayers = players.size();
 		boolean twoPlayer = (nbPlayers == 2);
 
 		if (twoPlayer) {
 			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(main);
 		} else {
 			GridLayoutFactory.fillDefaults().numColumns(nbPlayers)
 					.applyTo(main);
 		}
 
 		TableViewer viewer;
 		Table table;
 		if (twoPlayer) {
 			// Table
			table = this.toolkit.createTable(main, SWT.V_SCROLL);
 			GridDataFactory.fillDefaults().span(2, 4).grab(true, true)
 					.applyTo(table);
 			table.setHeaderVisible(true);
 			table.setLinesVisible(true);
 
 			// resize the row height using a MeasureItem listener
 			table.addListener(SWT.MeasureItem, new FixHeightListener(24));
 			viewer = new TableViewer(table);
 			viewer.setContentProvider(new GameX01ContentProvider());
 			this.addColumns(null, viewer);
 			this.scoreViewers.put(this.game.getFirstPlayer(), viewer);
 			this.scoreViewers.put(this.game.getSecondPlayer(), viewer);
 
 		} else {
 			Section section;
 			Composite client;
 			for (IPlayer player : players) {
 				// Section
 				section = this.toolkit.createSection(main,
 						ExpandableComposite.TITLE_BAR);
 				GridDataFactory.fillDefaults().grab(true, true)
 						.applyTo(section);
 				section.setText(MessageFormat.format("{0} - {1}", player,
 						this.game.getParentSet().getWinningGames(player)));
 
 				// Section body
 				client = this.toolkit.createComposite(section, SWT.WRAP);
 				GridLayoutFactory.fillDefaults().applyTo(client);
 
 				this.addPlayerStatSection(client, player);
 
 				// Table
 				table = this.toolkit.createTable(client, SWT.V_SCROLL);
 				GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
 				table.setHeaderVisible(true);
 				table.setLinesVisible(true);
 
 				// resize the row height using a MeasureItem listener
 				table.addListener(SWT.MeasureItem, new FixHeightListener(24));
 				viewer = new TableViewer(table);
 				viewer.setContentProvider(new GameX01ContentProvider());
 				this.addColumns(player, viewer);
 				this.scoreViewers.put(player, viewer);
 
 				// End section definition
 				this.toolkit.paintBordersFor(client);
 				section.setClient(client);
 			}
 		}
 		this.toolkit.paintBordersFor(main);
 
 		// Score input
 		for (IPlayer player : players) {
 			this.createInputScoreText(main, player);
 		}
 		return main;
 	}
 
 	/**
 	 * Adds the player stat section.
 	 *
 	 * @param parent the parent
 	 * @param player the player
 	 */
 	private void addPlayerStatSection(Composite parent, IPlayer player) {
 		// Section
 		Section section = this.toolkit.createSection(parent,
 				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
 		section.setText("Statistics");
 
 		// Section body
 		Composite client = this.toolkit.createComposite(section, SWT.WRAP);
 		GridLayoutFactory.fillDefaults().applyTo(client);
 
 		// Player Status
 		Composite cmpPlayerTwo = this.createPlayerComposite(client, player);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(cmpPlayerTwo);
 
 		// End section definition
 		this.toolkit.paintBordersFor(client);
 		section.setClient(client);
 	}
 
 	/**
 	 * Creates the input score text.
 	 *
 	 * @param main the main
 	 * @param player the player
 	 */
 	private void createInputScoreText(Composite main, IPlayer player) {
 		Text inputScoreText = this.toolkit.createText(main, "", SWT.CENTER
 				| SWT.BORDER);
 		inputScoreText.setFont(OpenDartsFormsToolkit
 				.getFont(OpenDartsFormsToolkit.FONT_SCORE_INPUT));
 		inputScoreText.setEnabled(false);
 		this.playerScoreInput.put(player, inputScoreText);
 
 		// layout
 		int indent = FieldDecorationRegistry.getDefault()
 				.getMaximumDecorationWidth() + 2;
 		GridDataFactory.fillDefaults().grab(true, false)
 				.indent(indent, SWT.DEFAULT).hint(SWT.DEFAULT, 80)
 				.applyTo(inputScoreText);
 
 		// decoration
 		ControlDecoration dec = new ControlDecoration(inputScoreText, SWT.TOP
 				| SWT.LEFT);
 
 		// listener
 		TextInputListener listener = new TextInputListener(this.getSite()
 				.getShell(), inputScoreText, this.game, player, dec);
 		inputScoreText.addKeyListener(listener);
 
 		inputScoreText.addTraverseListener(new TraverseListener() {
 			@Override
 			public void keyTraversed(TraverseEvent e) {
 				e.doit = false;
 			}
 		});
 	}
 
 	/**
 	 * Adds the columns.
 	 * @param viewer 
 	 * @param player2 
 	 */
 	private void addColumns(IPlayer player, TableViewer viewer) {
 		int turnWidth = 100;
 		if (player == null) {
 			// Two player
 			this.createPlayerColumns(viewer, this.game.getFirstPlayer());
 			this.toolkit.createTableColumn("", viewer, turnWidth, SWT.CENTER,
 					new TurnLabelProvider());
 			this.createPlayerColumns(viewer, this.game.getSecondPlayer());
 		} else {
 			this.toolkit.createTableColumn("", viewer, turnWidth, SWT.CENTER,
 					new TurnLabelProvider());
 			this.createPlayerColumns(viewer, player);
 		}
 	}
 
 	/**
 	 * Creates the player columns.
 	 * @param viewer 
 	 *
 	 * @param player the player
 	 */
 	private void createPlayerColumns(TableViewer viewer, IPlayer player) {
 		Shell shell = this.getSite().getShell();
 		int width = 94;
 		int style = SWT.CENTER;
 		TableViewerColumn column;
 		column = this.toolkit.createTableColumn("Scored", viewer, width, style,
 				new ScoreLabelProvider(player), new ScoreX01EditingSupport(
 						shell, this.game, player, viewer));
 		this.playerColumn.put(player, column);
 
 		this.toolkit.createTableColumn("To Go", viewer, width, style,
 				new ToGoLabelProvider(player));
 	}
 
 	/**
 	 * Creates the player composite.
 	 *
 	 * @param parent the parent
 	 * @param iPlayer the i player
 	 * @return the composite
 	 */
 	private Composite createPlayerComposite(Composite parent, IPlayer player) {
 		Composite main = this.toolkit.createComposite(parent);
 		GridLayoutFactory.fillDefaults().applyTo(main);
 
 		Section secPlayer = this.toolkit.createSection(main,
 				ExpandableComposite.TITLE_BAR
 						| ExpandableComposite.CLIENT_INDENT);
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(secPlayer);
 		secPlayer.setText(player.getName());
 
 		Composite client = this.toolkit.createComposite(secPlayer, SWT.WRAP);
 		GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(client);
 
 		PlayerStatusComposite cmpStatus = new PlayerStatusComposite(client,
 				player, this.game);
 		GridDataFactory.fillDefaults().grab(true, false)
 				.applyTo(cmpStatus.getControl());
 
 		this.toolkit.paintBordersFor(client);
 		secPlayer.setClient(client);
 
 		return main;
 	}
 
 	/**
 	 * Creates the player score sheet composite.
 	 *
 	 * @param parent the parent
 	 * @param iPlayer the i player
 	 * @return the composite
 	 */
 	private Composite createPlayerScoreLeftComposite(Composite parent,
 			IPlayer player) {
 		Composite main = this.toolkit.createComposite(parent);
 		GridLayoutFactory.fillDefaults().applyTo(main);
 
 		Text txtScore = this.toolkit.createText(main,
 				this.getPlayerCurrentScore(player), SWT.READ_ONLY | SWT.CENTER
 						| SWT.BORDER);
 		txtScore.setFont(OpenDartsFormsToolkit
 				.getFont(OpenDartsFormsToolkit.FONT_SCORE_LEFT));
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(txtScore);
 		this.playerScoreLeft.put(player, txtScore);
 
 		return main;
 	}
 
 	/**
 	 * Gets the player current score.
 	 *
 	 * @param player the player
 	 * @return the player current score
 	 */
 	private String getPlayerCurrentScore(IPlayer player) {
 		String result = "";
 		Integer score = this.game.getScore(player);
 		if (score == null) {
 			result = "";
 		} else {
 			result = SCORE_FORMAT.format(score);
 		}
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.prototype.model.game.IGameListener#notifyGameEvent(org.opendarts.prototype.model.game.GameEvent)
 	 */
 	@Override
 	public void notifyGameEvent(final GameEvent event) {
 		if (event.getGame().equals(this.game)) {
 			LOG.trace("New Game Event: {}", event);
 			switch (event.getType()) {
 				case GAME_INITIALIZED:
 					this.handleGameInitialized();
 					break;
 				case GAME_ENTRY_CREATED:
 					this.handleNewEntry(event.getEntry());
 					break;
 				case GAME_ENTRY_UPDATED:
 					this.handleEntryUpdated(event.getPlayer(), event.getEntry());
 					break;
 				case NEW_CURRENT_PLAYER:
 					this.handlePlayer(event.getPlayer(), event.getEntry());
 					break;
 				case GAME_FINISHED:
 					this.handleGameFinished(event.getPlayer());
 					break;
 				case GAME_CANCELED:
 					// TODO cleanup
 					this.dirty = false;
 					this.mForm.dirtyStateChanged();
 			}
 		}
 	}
 
 	/**
 	 * Handle game initialized.
 	 */
 	private void handleGameInitialized() {
 		Text txt;
 		for (IPlayer p : this.game.getPlayers()) {
 			txt = this.playerScoreLeft.get(p);
 			txt.setText(this.getPlayerCurrentScore(p));
 		}
 
 		for (TableViewer tw : this.scoreViewers.values()) {
 			tw.setInput(this.game.getGameEntries());
 		}
 		this.isDirty();
 		this.mForm.dirtyStateChanged();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#isDirty()
 	 */
 	@Override
 	public boolean isDirty() {
 		return this.dirty;
 	}
 
 	/**
 	 * Handle new entry.
 	 *
 	 * @param entry the entry
 	 */
 	private void handleNewEntry(IGameEntry entry) {
 		boolean twoPlayer = (this.game.getPlayers().size() == 2);
 		if (twoPlayer) {
 			// only one table
 			TableViewer tw = this.scoreViewers.get(this.game.getFirstPlayer());
 			tw.add(entry);
 			tw.reveal(entry);
 		} else {
 			for (TableViewer tw : this.scoreViewers.values()) {
 				tw.add(entry);
 				tw.reveal(entry);
 			}
 		}
 	}
 
 	/**
 	 * Handle entry updated.
 	 *
 	 * @param entry the entry
 	 */
 	private void handleEntryUpdated(IPlayer player, IGameEntry entry) {
 		Text txt;
 		TableViewer scoreViewer = this.scoreViewers.get(player);
 		scoreViewer.update(entry, null);
 		if (player != null) {
 			txt = this.playerScoreLeft.get(player);
 			txt.setText(this.getPlayerCurrentScore(player));
 		}
 	}
 
 	/**
 	 * Handle game finished.
 	 *
 	 * @param player the player
 	 */
 	private void handleGameFinished(IPlayer player) {
 		TableViewerColumn column;
 		column = this.playerColumn.get(player);
 		column.getColumn().setImage(
 				ProtoPlugin.getImage(ISharedImages.IMG_TICK_DECO));
 		// remove edition
 		for (Text inputTxt : this.playerScoreInput.values()) {
 			inputTxt.setEnabled(false);
 		}
 		for (TableViewerColumn col : this.playerColumn.values()) {
 			col.setEditingSupport(null);
 		}
 
 		// End Game dialog
 		if (!this.game.getParentSet().isFinished()) {
 			String title = MessageFormat.format("{0} finished", this.game);
 			String message = this.game.getWinningMessage();
 			Shell shell = this.getSite().getShell();
 			MessageDialog.open(MessageDialog.INFORMATION, shell, title,
 					message, SWT.SHEET);
 		}
 		this.dirty = false;
 		this.mForm.dirtyStateChanged();
 	}
 
 	/**
 	 * Handle player.
 	 *
 	 * @param player the player
 	 */
 	private void handlePlayer(IPlayer player, IGameEntry entry) {
 		if (player != null) {
 			TableViewerColumn column;
 			// mark column
 			column = this.playerColumn.get(player);
 			TableViewerColumn c;
 			for (IPlayer p : this.game.getPlayers()) {
 				c = this.playerColumn.get(p);
 				if (c.equals(column)) {
 					c.getColumn().setImage(
 							ProtoPlugin.getImage(ISharedImages.IMG_ARROW_DECO));
 				} else {
 					c.getColumn().setImage(null);
 				}
 			}
 			// enable/disable inputs & focus
 			Text playerInputTxt = this.playerScoreInput.get(player);
 			for (Text inputTxt : this.playerScoreInput.values()) {
 				if (playerInputTxt.equals(inputTxt)) {
 					inputTxt.setEnabled(true);
 					inputTxt.setBackground(OpenDartsFormsToolkit.getToolkit()
 							.getColors()
 							.getColor(OpenDartsFormsToolkit.COLOR_ACTIVE));
 					inputTxt.setFocus();
 				} else {
 					inputTxt.setEnabled(false);
 					inputTxt.setText("");
 					inputTxt.setBackground(OpenDartsFormsToolkit.getToolkit()
 							.getColors()
 							.getColor(OpenDartsFormsToolkit.COLOR_INACTIVE));
 				}
 			}
 
 			// IA playing
 			if (player.isComputer()) {
 				ThreeDartsComputerDialog computerThrow = new DartsComputerX01Dialog(
 						this.getSite().getShell(), player, this.game,
 						(GameX01Entry) entry);
 				computerThrow.open();
 
 				IDartsThrow dartThrow = computerThrow.getComputerThrow();
 				if (dartThrow instanceof WinningX01DartsThrow) {
 					this.gameService.addWinningPlayerThrow(this.game, player,
 							dartThrow);
 				} else {
 					this.gameService.addPlayerThrow(this.game, player,
 							dartThrow);
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent)
 	 */
 	@Override
 	public void expansionStateChanged(ExpansionEvent e) {
 		this.body.layout(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.events.IExpansionListener#expansionStateChanging(org.eclipse.ui.forms.events.ExpansionEvent)
 	 */
 	@Override
 	public void expansionStateChanging(ExpansionEvent e) {
 		// Nothing to do
 	}
 }
