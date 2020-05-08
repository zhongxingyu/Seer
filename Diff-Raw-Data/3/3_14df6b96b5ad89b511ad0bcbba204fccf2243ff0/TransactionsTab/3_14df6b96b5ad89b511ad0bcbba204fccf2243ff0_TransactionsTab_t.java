 package emcshop.gui;
 
 import static emcshop.util.GuiUtils.busyCursor;
 import static emcshop.util.NumberFormatter.formatRupeesWithColor;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyVetoException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import com.michaelbaranov.microba.calendar.DatePicker;
 
 import emcshop.QueryExporter;
 import emcshop.db.DbDao;
 import emcshop.db.ItemGroup;
 import emcshop.db.PlayerGroup;
 import emcshop.gui.images.ImageManager;
 import emcshop.util.FilterList;
 import emcshop.util.Settings;
 
 @SuppressWarnings("serial")
 public class TransactionsTab extends JPanel {
 	private final MainFrame owner;
 	private final DbDao dao;
 	private final ProfileImageLoader profileImageLoader;
 	private final Settings settings;
 	private final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 
 	private final JCheckBox showSinceLastUpdate;
 	private final JCheckBox entireHistory;
 	private final JLabel toDatePickerLabel;
 	private final DatePicker toDatePicker;
 	private final JLabel fromDatePickerLabel;
 	private final DatePicker fromDatePicker;
 	private final JButton showItems;
 	private final JButton showPlayers;
 
 	private final JLabel exportLabel;
 	private final ExportComboBox export;
 	private final JLabel filterByItemLabel;
 	private final FilterTextField filterByItem;
 	private final JLabel filterByPlayerLabel;
 	private final FilterTextField filterByPlayer;
 	private final JLabel sortByLabel;
 	private final SortComboBox sortBy;
 
 	private final JLabel dateRangeQueried;
 	private final JPanel tablePanel;
 	private final JLabel netTotalLabelLabel;
 	private final JLabel netTotalLabel;
 	private final JLabel customersLabel;
 	private final JLabel customers;
 
 	private ItemsTable itemsTable;
 	private MyJScrollPane itemsTableScrollPane;
 
 	private PlayersPanel playersPanel;
 
 	private int netTotal;
 
 	public TransactionsTab(MainFrame owner, DbDao dao, ProfileImageLoader profileImageLoader, Settings settings) {
 		this.owner = owner;
 		this.dao = dao;
 		this.profileImageLoader = profileImageLoader;
 		this.settings = settings;
 
 		entireHistory = new JCheckBox();
 		entireHistory.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (entireHistory.isSelected()) {
 					showSinceLastUpdate.setSelected(false);
 				}
 
 				boolean enableDatePickers = !entireHistory.isSelected();
 				fromDatePickerLabel.setEnabled(enableDatePickers);
 				fromDatePicker.setEnabled(enableDatePickers);
 				toDatePickerLabel.setEnabled(enableDatePickers);
 				toDatePicker.setEnabled(enableDatePickers);
 			}
 		});
 
 		showSinceLastUpdate = new JCheckBox();
 		showSinceLastUpdate.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (showSinceLastUpdate.isSelected()) {
 					entireHistory.setSelected(false);
 				}
 
 				boolean enableDatePickers = !showSinceLastUpdate.isSelected();
 				fromDatePickerLabel.setEnabled(enableDatePickers);
 				fromDatePicker.setEnabled(enableDatePickers);
 				toDatePickerLabel.setEnabled(enableDatePickers);
 				toDatePicker.setEnabled(enableDatePickers);
 			}
 		});
 
 		fromDatePickerLabel = new JLabel("Start:");
 		fromDatePicker = new DatePicker();
 		fromDatePicker.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
 		fromDatePicker.setShowNoneButton(true);
 		fromDatePicker.setShowTodayButton(true);
 		fromDatePicker.setStripTime(true);
 
 		toDatePickerLabel = new JLabel("End:");
 		toDatePicker = new DatePicker();
 		toDatePicker.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
 		toDatePicker.setShowNoneButton(true);
 		toDatePicker.setShowTodayButton(true);
 		toDatePicker.setStripTime(true);
 
 		showItems = new JButton("By Item", ImageManager.getSearch());
 		showItems.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!checkDateRange()) {
 					return;
 				}
 				showTransactions();
 			}
 		});
 
 		showPlayers = new JButton("By Player", ImageManager.getSearch());
 		showPlayers.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (!checkDateRange()) {
 					return;
 				}
 				showPlayers();
 			}
 		});
 
 		tablePanel = new JPanel(new MigLayout("width 100%, height 100%, fillx, insets 0"));
 
 		exportLabel = new JLabel("Export:");
 		export = new ExportComboBoxImpl();
 
 		filterByItemLabel = new HelpLabel("Filter by item(s):", "<b>Filters the table by item.</b>\n<b>Example</b>: <code>wool,\"book\"</code>\n\nMultiple item names can be entered, separated by commas.\n\nExact name matches will be peformed on names that are enclosed in double quotes.  Otherwise, partial name matches will be performed.\n\nAfter entering the item name(s), press [<code>Enter</code>] to perform the filtering operation.");
 
 		filterByItem = new FilterTextField();
 		filterByItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				FilterList filterList = filterByItem.getNames();
 
 				if (itemsTable != null) {
 					itemsTable.filter(filterList);
 					itemsTableScrollPane.scrollToTop();
 				}
 				if (playersPanel != null) {
 					playersPanel.filterByItems(filterList);
 				}
 
 				updateNetTotal();
 				updateCustomers();
 			}
 		});
 
 		filterByPlayerLabel = new HelpLabel("Filter by player(s):", "<b>Filters the table by player.</b>\n<b>Example</b>: <code>aikar,max</code>\n\nMultiple player names can be entered, separated by commas.\n\nExact name matches will be peformed on names that are enclosed in double quotes.  Otherwise, partial name matches will be performed.\n\nAfter entering the player name(s), press [<code>Enter</code>] to perform the filtering operation.");
 
 		filterByPlayer = new FilterTextField();
 		filterByPlayer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				FilterList filterList = filterByPlayer.getNames();
 
 				if (playersPanel != null) {
 					playersPanel.filterByPlayers(filterList);
 				}
 
 				updateNetTotal();
 				updateCustomers();
 			}
 		});
 
 		sortByLabel = new JLabel("Sort by:");
 		sortBy = new SortComboBox();
 
 		dateRangeQueried = new JLabel();
 
 		netTotalLabelLabel = new JLabel("<html><font size=5>Net Total:</font></html>");
 		netTotalLabel = new JLabel();
 
 		customersLabel = new JLabel("<html><font size=5>Customers:</font></html>");
 		customers = new JLabel();
 
 		///////////////////////////////////////
 
 		setLayout(new MigLayout("fillx, insets 5"));
 
 		JPanel left = new JPanel(new MigLayout("insets 0"));
 
 		left.add(entireHistory, "wrap");
 		left.add(showSinceLastUpdate, "wrap");
 		left.add(fromDatePickerLabel, "split 4");
 		left.add(fromDatePicker);
 		left.add(toDatePickerLabel);
 		left.add(toDatePicker, "wrap");
 
 		left.add(showItems, "split 2");
 		left.add(showPlayers);
 
 		add(left, "w 100%"); //width of 100% forces the right panel to be compact
 
 		JPanel right = new JPanel(new MigLayout("fillx, insets 0"));
 
 		right.add(exportLabel, "align right");
 		right.add(export, "w 185!, wrap");
 
 		right.add(filterByItemLabel, "align right");
 		right.add(filterByItem, "split 2, w 150!");
 		right.add(filterByItem.getClearButton(), "w 25!, h 20!, wrap");
 
 		right.add(filterByPlayerLabel, "align right");
 		right.add(filterByPlayer, "split 2, w 150!");
 		right.add(filterByPlayer.getClearButton(), "w 25!, h 20!, wrap");
 
 		right.add(sortByLabel, "align right");
 		right.add(sortBy, "w 185!");
 
 		add(right, "span 1 2, align right, wrap");
 
 		add(dateRangeQueried, "gaptop 20, w 100%, wrap"); //putting this label here allows the left panel to be vertically aligned to the top of the tab
 
 		add(tablePanel, "span 2, grow, h 100%, wrap");
 
 		add(customersLabel, "span 2, split 4, align right");
 		add(customers, "gapright 20");
 		add(netTotalLabelLabel);
 		add(netTotalLabel);
 
 		///////////////////////////////////////
 
 		if (settings.getPreviousUpdate() == null) {
 			//user has to perform an update first for this option to work
 			showSinceLastUpdate.setVisible(false);
 		}
 
 		exportLabel.setEnabled(false);
 		export.setEnabled(false);
 		filterByItemLabel.setEnabled(false);
 		filterByItem.setEnabled(false);
 		filterByPlayerLabel.setEnabled(false);
 		filterByPlayer.setEnabled(false);
 		sortByLabel.setEnabled(false);
 		sortBy.setEnabled(false);
 
 		customersLabel.setVisible(false);
 		customers.setVisible(false);
 
 		updateNetTotal();
 		updateCustomers();
 		updateSinceLastUpdateCheckbox();
 		updateEntireHistoryCheckbox();
 	}
 
 	public void clear() {
 		try {
 			fromDatePicker.setDate(new Date());
 			toDatePicker.setDate(new Date());
 		} catch (PropertyVetoException e) {
 			throw new RuntimeException(e);
 		}
 
 		itemsTable = null;
 		itemsTableScrollPane = null;
 		playersPanel = null;
 		netTotal = 0;
 
 		if (settings.getPreviousUpdate() == null) {
 			//user has to perform an update first for this option to work
 			showSinceLastUpdate.setVisible(false);
 		}
 
 		exportLabel.setEnabled(false);
 		export.setEnabled(false);
 		filterByItemLabel.setEnabled(false);
 		filterByItem.setEnabled(false);
 		filterByItem.setText("");
 		filterByPlayerLabel.setEnabled(false);
 		filterByPlayer.setEnabled(false);
 		filterByPlayer.setText("");
 		sortByLabel.setEnabled(false);
 		sortBy.setEnabled(false);
		dateRangeQueried.setText("");
 
 		tablePanel.removeAll();
 
 		updateNetTotal();
 		updateCustomers();
 		updateSinceLastUpdateCheckbox();
 		updateEntireHistoryCheckbox();
 
 		validate();
		repaint(); //the table was still visible in Linux
 	}
 
 	public void updateComplete(boolean showResults, boolean firstUpdate) {
 		if (firstUpdate || settings.getPreviousUpdate() != null) {
 			//user has to perform an update first for this option to work
 			showSinceLastUpdate.setVisible(true);
 		}
 		updateSinceLastUpdateCheckbox();
 		if (firstUpdate) {
 			updateEntireHistoryCheckbox();
 		}
 
 		if (showResults) {
 			if (showSinceLastUpdate.isVisible()) {
 				showSinceLastUpdate.doClick();
 				if (!showSinceLastUpdate.isSelected()) {
 					showSinceLastUpdate.setSelected(true);
 				}
 			}
 
 			showTransactions();
 		}
 	}
 
 	private void updateSinceLastUpdateCheckbox() {
 		String text = "since previous update";
 		Date date = settings.getPreviousUpdate();
 		if (date != null) {
 			text += " (" + df.format(date) + ")";
 		}
 
 		showSinceLastUpdate.setText(text);
 	}
 
 	private void updateEntireHistoryCheckbox() {
 		Date earliestTransactionDate = null;
 		try {
 			earliestTransactionDate = dao.getEarliestTransactionDate();
 		} catch (SQLException e) {
 			//ignore
 		}
 
 		String text = "entire history";
 		if (earliestTransactionDate != null) {
 			text += " (since " + df.format(earliestTransactionDate) + ")";
 		}
 		entireHistory.setText(text);
 	}
 
 	private void showTransactions() {
 		Date range[] = getQueryDateRange();
 		showTransactions(range[0], range[1]);
 	}
 
 	public void showTransactions(final Date from, final Date to) {
 		busyCursor(owner, true);
 
 		tablePanel.removeAll();
 		tablePanel.validate();
 
 		playersPanel = null;
 
 		exportLabel.setEnabled(true);
 		export.setEnabled(true);
 		filterByItemLabel.setEnabled(true);
 		filterByItem.setEnabled(true);
 		filterByItem.setText("");
 		filterByPlayerLabel.setEnabled(false);
 		filterByPlayer.setEnabled(false);
 		filterByPlayer.setText("");
 		sortByLabel.setEnabled(false);
 		sortBy.setEnabled(false);
 
 		final LoadingDialog loading = new LoadingDialog(owner, "Loading", "Querying . . .");
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				try {
 					//query database
 					final List<ItemGroup> itemGroupsList;
 					{
 						//query database
 						Map<String, ItemGroup> itemGroups = dao.getItemGroups(from, to);
 						itemGroupsList = new ArrayList<ItemGroup>(itemGroups.values());
 
 						//sort by item name
 						Collections.sort(itemGroupsList, new Comparator<ItemGroup>() {
 							@Override
 							public int compare(ItemGroup a, ItemGroup b) {
 								return a.getItem().compareToIgnoreCase(b.getItem());
 							}
 						});
 					}
 
 					//render table
 					itemsTable = new ItemsTable(itemGroupsList, settings.isShowQuantitiesInStacks());
 					itemsTable.setFillsViewportHeight(true);
 					itemsTableScrollPane = new MyJScrollPane(itemsTable);
 					tablePanel.add(itemsTableScrollPane, "grow, w 100%, h 100%, wrap");
 					tablePanel.validate();
 
 					updateDateRangeLabel(from, to);
 					updateNetTotal();
 					updateCustomers();
 				} catch (SQLException e) {
 					ErrorDialog.show(owner, "An error occurred querying the database.", e);
 				} finally {
 					loading.dispose();
 					busyCursor(owner, false);
 				}
 			}
 		};
 		t.start();
 		loading.setVisible(true);
 	}
 
 	private void showPlayers() {
 		Date range[] = getQueryDateRange();
 		showPlayers(range[0], range[1]);
 	}
 
 	public void showPlayers(final Date from, final Date to) {
 		busyCursor(owner, true);
 
 		tablePanel.removeAll();
 		tablePanel.validate();
 
 		itemsTable = null;
 		itemsTableScrollPane = null;
 
 		exportLabel.setEnabled(true);
 		export.setEnabled(true);
 		filterByItemLabel.setEnabled(true);
 		filterByItem.setEnabled(true);
 		filterByItem.setText("");
 		filterByPlayerLabel.setEnabled(true);
 		filterByPlayer.setEnabled(true);
 		filterByPlayer.setText("");
 		sortBy.setEnabled(true);
 		sortBy.setSelectedIndex(0);
 		sortByLabel.setEnabled(true);
 
 		final LoadingDialog loading = new LoadingDialog(owner, "Loading", "Querying . . .");
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				try {
 					//query database
 					Collection<PlayerGroup> playerGroups = dao.getPlayerGroups(from, to).values();
 
 					//render table
 					playersPanel = new PlayersPanel(playerGroups, profileImageLoader, settings.isShowQuantitiesInStacks());
 					tablePanel.add(playersPanel, "grow, w 100%, h 100%, wrap");
 					tablePanel.validate();
 
 					updateDateRangeLabel(from, to);
 					updateNetTotal();
 					updateCustomers();
 				} catch (SQLException e) {
 					loading.dispose();
 					ErrorDialog.show(owner, "An error occurred querying the database.", e);
 				} finally {
 					loading.dispose();
 					busyCursor(owner, false);
 				}
 			}
 		};
 		t.start();
 		loading.setVisible(true);
 	}
 
 	private void updateDateRangeLabel(Date from, Date to) {
 		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 		String dateRangeStr;
 		final String startFont = "<b><i><font color=navy>";
 		final String endFont = "</font></i></b>";
 		if (from == null && to == null) {
 			dateRangeStr = startFont + "entire history" + endFont;
 		} else if (from == null) {
 			dateRangeStr = "up to " + startFont + df.format(to) + endFont;
 		} else if (to == null) {
 			dateRangeStr = startFont + df.format(from) + endFont + " to " + startFont + "now" + endFont;
 		} else if (from.equals(to)) {
 			dateRangeStr = startFont + df.format(from) + endFont;
 		} else {
 			dateRangeStr = startFont + df.format(from) + endFont + " to " + startFont + df.format(to) + endFont;
 		}
 
 		dateRangeQueried.setText("<html>" + dateRangeStr + "</html>");
 	}
 
 	private void updateNetTotal() {
 		netTotal = 0;
 
 		if (itemsTable != null) {
 			for (ItemGroup item : itemsTable.getDisplayedItemGroups()) {
 				netTotal += item.getNetAmount();
 			}
 		} else if (playersPanel != null) {
 			for (List<ItemGroup> playerItems : playersPanel.getDisplayedItems().values()) {
 				for (ItemGroup item : playerItems) {
 					netTotal += item.getNetAmount();
 				}
 			}
 		}
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("<html><font size=5><code>");
 		sb.append(formatRupeesWithColor(netTotal));
 		sb.append("</code></font></html>");
 		netTotalLabel.setText(sb.toString());
 	}
 
 	private void updateCustomers() {
 		boolean visible = false;
 
 		if (playersPanel != null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("<html><font size=5><code>");
 			sb.append(playersPanel.getDisplayedPlayers().size());
 			sb.append("</code></font></html>");
 
 			customers.setText(sb.toString());
 			visible = true;
 		}
 
 		customersLabel.setVisible(visible);
 		customers.setVisible(visible);
 	}
 
 	private boolean checkDateRange() {
 		if (showSinceLastUpdate.isSelected() || entireHistory.isSelected()) {
 			return true;
 		}
 
 		Date from = fromDatePicker.getDate();
 		Date to = toDatePicker.getDate();
 		if (from.compareTo(to) > 0) {
 			JOptionPane.showMessageDialog(this, "Invalid date range: \"Start\" date must come before \"End\" date.", "Invalid date range", JOptionPane.INFORMATION_MESSAGE);
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Calculates the date range that the query should search over from the
 	 * various input elements on the panel.
 	 * @return the date range
 	 */
 	private Date[] getQueryDateRange() {
 		Date from, to;
 		if (showSinceLastUpdate.isSelected()) {
 			from = settings.getPreviousUpdate();
 			to = null;
 		} else if (entireHistory.isSelected()) {
 			from = to = null;
 		} else {
 			from = fromDatePicker.getDate();
 
 			to = toDatePicker.getDate();
 			if (to != null) {
 				Calendar c = Calendar.getInstance();
 				c.setTime(to);
 				c.add(Calendar.DATE, 1);
 				to = c.getTime();
 			}
 		}
 
 		return new Date[] { from, to };
 	}
 
 	public void setShowQuantitiesInStacks(boolean stacks) {
 		if (itemsTable != null) {
 			itemsTable.setShowQuantitiesInStacks(stacks);
 		}
 
 		if (playersPanel != null) {
 			playersPanel.setShowQuantitiesInStacks(stacks);
 		}
 	}
 
 	private class ExportComboBoxImpl extends ExportComboBox {
 		public ExportComboBoxImpl() {
 			super(owner);
 		}
 
 		@Override
 		public String bbCode() {
 			Date from = fromDatePicker.getDate();
 			Date to = toDatePicker.getDate();
 
 			if (itemsTable != null) {
 				return QueryExporter.generateItemsBBCode(itemsTable.getDisplayedItemGroups(), netTotal, from, to);
 			}
 
 			if (playersPanel != null) {
 				List<PlayerGroup> players = playersPanel.getDisplayedPlayers();
 				Map<PlayerGroup, List<ItemGroup>> items = playersPanel.getDisplayedItems();
 				return QueryExporter.generatePlayersBBCode(players, items, from, to);
 			}
 
 			return null;
 		}
 
 		@Override
 		public String csv() {
 			Date from = fromDatePicker.getDate();
 			Date to = toDatePicker.getDate();
 
 			if (itemsTable != null) {
 				return QueryExporter.generateItemsCsv(itemsTable.getDisplayedItemGroups(), netTotal, from, to);
 			}
 
 			if (playersPanel != null) {
 				List<PlayerGroup> players = playersPanel.getDisplayedPlayers();
 				Map<PlayerGroup, List<ItemGroup>> items = playersPanel.getDisplayedItems();
 				return QueryExporter.generatePlayersCsv(players, items, from, to);
 			}
 
 			return null;
 		}
 	}
 
 	private class SortComboBox extends JComboBox implements ActionListener {
 		private final String playerName = "Player name";
 		private final String bestCustomers = "Best Customers";
 		private final String bestSuppliers = "Best Suppliers";
 		private String currentSelection;
 
 		public SortComboBox() {
 			addItem(playerName);
 			addItem(bestCustomers);
 			addItem(bestSuppliers);
 			addActionListener(this);
 			currentSelection = playerName;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if (playersPanel == null) {
 				return;
 			}
 
 			String selected = (String) getSelectedItem();
 			if (selected == currentSelection) {
 				return;
 			}
 
 			busyCursor(owner, true);
 			try {
 				if (selected == playerName) {
 					playersPanel.sortByPlayerName();
 				} else if (selected == bestCustomers) {
 					playersPanel.sortByCustomers();
 				} else if (selected == bestSuppliers) {
 					playersPanel.sortBySuppliers();
 				}
 				currentSelection = selected;
 			} finally {
 				busyCursor(owner, false);
 			}
 		}
 	}
 }
