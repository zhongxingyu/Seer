 /**
  * 
  */
 package rfctool;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.prefs.Preferences;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.TitledBorder;
 import net.miginfocom.swing.MigLayout;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.Seconds;
 import org.joda.time.format.PeriodFormatter;
 import org.joda.time.format.PeriodFormatterBuilder;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel;
 
 /**
  * RFCTool - RFCTool
  * @version 1.0
  * @author <a href="mailto:Dessimat0r@ntlworld.com">Chris Dennett</a>
  */
 public class RFCTool {
 	public enum Status {
 		UNKNOWN("Unknown!"),
 		ALL_GOOD("Worked fine :)"),
 		SITE_DOWN("Site down!"),
 		PAGE_NOT_FOUND("Page not found!"),
 		API_KEY_INVALID("API key invalid!");
 
 		protected final String text;
 
 		Status(String text) {
 			this.text = text;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Enum#toString()
 		 */
 		@Override
 		public String toString() {
 			return text;
 		}
 	}
 
 	private JFrame frmRfctool;
 	private URI siteURL = URI.create("http://rfcpool.com");
 	private JLabel lblBlocksFoundVal;
 	private JLabel lblHashrateVal;
 	private JLabel lblRoundSharesVal;
 	private JLabel lblRoundTimeVal;
 	private JLabel lblUsrBalanceVal;
 	private JLabel lblUsrPayoutVal;
 	private JLabel lblUsr15MinsSharesVal;
 	private JLabel lblUsrAllTimeSharesVal;
 	private JLabel lblUsrRoundSharesVal;
 
 	protected volatile boolean updating = false;
 
 	protected volatile WorkingMgr wkr = null;
 
 	public static final PeriodFormatter DAYS_HOURS_MINS = new PeriodFormatterBuilder()
 		.appendDays()
 		.appendSuffix("d")
 		.appendSeparator(" ")
 		.appendHours()
 		.appendSuffix("h")
 		.appendSeparator(" ")
 		.appendMinutes()
 		.appendSuffix("m")
 		.appendSeparator(" ")
 		.appendSeconds()
 		.appendSuffix("s")
 		.toFormatter()
 	;
 
 	public static final Preferences PREFS = Preferences.userNodeForPackage(RFCTool.class);
 
 	protected String apiKey = PREFS.get("api_key", "");
 	private JLabel lblWorkersVal;
 
 	protected String hashrate_unit = "seconds";
 	protected double hashrate = 0;
 	protected int    roundTime = 0;
 	protected int    blocksFound = 0;
 	protected int    roundShares = 0;
 	protected int    workers = 0;
 
 	protected DateTime lastUpdated = null;
 
 	private JLabel lblPoolLastUpdVal;
 
 	protected Status poolStatus = Status.ALL_GOOD;
 	protected Status userStatus = Status.ALL_GOOD;
 
 	protected final Object stateLock = new Object();
 
 	protected static final String[] UPD_ARR = new String[5];
 	private double userTotalEarnings;
 	private double userTotalDonated;
 	private double userEstimatedReward;
 	private double userBalanceUnconfirmed;
 	private double userBalanceConfirmed;
 	private double userPayoutsDone;
 	private double userPayoutsPending;
 
 	private JLabel lblUsrEstRewardVal;
 	private JLabel lblUsrTotalDonateVal;
 	private JLabel lblUsrTotalEarnVal;
 
 	static {
 		for (int i = 0; i < 5; i++) {
 			UPD_ARR[i] = gendots(i);
 		}
 	}
 
 	protected static String gendots(int dots) {
 		StringBuilder sb = new StringBuilder("Updating");
 		for (int i = 0; i < dots; i++) {
 			sb.append('.');
 		}
 		return sb.toString();
 	}
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					setUpSubstanceLAF();
 					RFCTool window = new RFCTool();
 					window.frmRfctool.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public RFCTool() {
 		initialize();
 		ExecutorService es = Executors.newSingleThreadExecutor();
 		es.execute(new Runnable() {
 			@Override
 			public void run() {		
 				updateAPI();
 			}
 		});
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmRfctool = new JFrame();
 		frmRfctool.setMinimumSize(new Dimension(650, 610));
 		frmRfctool.setSize(new Dimension(650, 610));
 		frmRfctool.setResizable(false);
 		frmRfctool.setTitle("RFCTool");
 		frmRfctool.setBounds(100, 100, 531, 329);
 		frmRfctool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmRfctool.getContentPane().setLayout(new MigLayout("fill, insets 0", "[grow,fill]", "[top, shrink][top,shrink]push[pref!][bottom]"));
 		JPanel panel = new JPanel();
 		frmRfctool.getContentPane().add(panel, "cell 0 0 1 2,growx,aligny top");
 		panel.setLayout(new MigLayout("fill", "[push,grow,fill]", "[top][top][top]"));
 
 		final JPanel panel_1 = new JPanel();
 		panel_1.setBorder(new TitledBorder(null, "Pool Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.add(panel_1, "cell 0 0,growx,aligny top");
 		panel_1.setLayout(new MigLayout("", "[170]10px[200px]20px[170]10px[200px]", "[][][]"));
 
 		final JLabel lblNewLabel = new JLabel("Blocks found:");
 		lblNewLabel.setFont(lblNewLabel.getFont().deriveFont(lblNewLabel.getFont().getStyle() | Font.BOLD));
 		panel_1.add(lblNewLabel, "cell 0 0");
 
 		lblBlocksFoundVal = new JLabel("N/A");
 		panel_1.add(lblBlocksFoundVal, "cell 1 0");
 
 		final JLabel lblHashrate = new JLabel("Hashrate:");
 		lblHashrate.setFont(lblNewLabel.getFont().deriveFont(lblNewLabel.getFont().getStyle() | Font.BOLD));
 		panel_1.add(lblHashrate, "cell 2 0");
 
 		lblHashrateVal = new JLabel("N/A");
 		panel_1.add(lblHashrateVal, "cell 3 0");
 
 		final JLabel lblRoundShares = new JLabel("Round shares:");
 		lblRoundShares.setFont(lblNewLabel.getFont().deriveFont(lblNewLabel.getFont().getStyle() | Font.BOLD));
 		panel_1.add(lblRoundShares, "cell 0 1");
 
 		lblRoundSharesVal = new JLabel("N/A");
 		panel_1.add(lblRoundSharesVal, "cell 1 1");
 
 		final JLabel lblRoundTime = new JLabel("Round time:");
 		lblRoundTime.setFont(lblNewLabel.getFont().deriveFont(lblNewLabel.getFont().getStyle() | Font.BOLD));
 		panel_1.add(lblRoundTime, "cell 2 1");
 
 		lblRoundTimeVal = new JLabel("N/A");
 		panel_1.add(lblRoundTimeVal, "cell 3 1");
 
 		final JLabel lblWorkers = new JLabel("Workers:");
 		lblWorkers.setFont(lblWorkers.getFont().deriveFont(lblWorkers.getFont().getStyle() | Font.BOLD));
 		panel_1.add(lblWorkers, "cell 0 2");
 
 		lblWorkersVal = new JLabel("N/A");
 		panel_1.add(lblWorkersVal, "cell 1 2");
 
 		final JPanel panel_2 = new JPanel();
 		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "User Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.add(panel_2, "cell 0 1,growx,aligny top");
 		panel_2.setLayout(new MigLayout("", "[170]10px[200px]20px[170]10px[200px]", "[][][][][]"));
 
 		final JLabel lblBalance = new JLabel("Balance:");
 		lblBalance.setFont(lblBalance.getFont().deriveFont(lblBalance.getFont().getStyle() | Font.BOLD));
 		panel_2.add(lblBalance, "cell 0 0");
 
 		lblUsrBalanceVal = new JLabel("N/A");
 		panel_2.add(lblUsrBalanceVal, "cell 1 0");
 
 		final JLabel lblPayout = new JLabel("Payout:");
 		lblPayout.setFont(lblPayout.getFont().deriveFont(lblPayout.getFont().getStyle() | Font.BOLD));
 		panel_2.add(lblPayout, "cell 2 0");
 
 		lblUsrPayoutVal = new JLabel("N/A");
 		panel_2.add(lblUsrPayoutVal, "cell 3 0");
 
 		final JLabel lblFf = new JLabel("Shares:");
 		lblFf.setFont(lblFf.getFont().deriveFont(lblFf.getFont().getStyle() | Font.BOLD));
 		panel_2.add(lblFf, "cell 0 1");
 
 		final JLabel lblEarnings = new JLabel("Est. reward:");
 		lblEarnings.setFont(lblEarnings.getFont().deriveFont(lblEarnings.getFont().getStyle() | Font.BOLD));
 		panel_2.add(lblEarnings, "cell 2 1");
 
 		lblUsrEstRewardVal = new JLabel("N/A");
 		panel_2.add(lblUsrEstRewardVal, "cell 3 1");
 
 		final JLabel lblConfirmed = new JLabel("Last 15 mins:");
 		lblConfirmed.setFont(lblConfirmed.getFont().deriveFont(lblConfirmed.getFont().getStyle() | Font.BOLD | Font.ITALIC));
 		panel_2.add(lblConfirmed, "cell 0 2,gapx 10px");
 
 		lblUsr15MinsSharesVal = new JLabel("N/A");
 		panel_2.add(lblUsr15MinsSharesVal, "cell 1 2");
 
 		final JLabel lblTotalEarnings = new JLabel("Total earnings:");
 		lblTotalEarnings.setFont(lblTotalEarnings.getFont().deriveFont(lblTotalEarnings.getFont().getStyle() | Font.BOLD));
 		panel_2.add(lblTotalEarnings, "cell 2 2");
 
 		lblUsrTotalEarnVal = new JLabel("N/A");
 		panel_2.add(lblUsrTotalEarnVal, "cell 3 2");
 
 		final JLabel lblUnconfirmed = new JLabel("This round:");
 		lblUnconfirmed.setFont(lblConfirmed.getFont().deriveFont(lblConfirmed.getFont().getStyle() | Font.BOLD | Font.ITALIC));
 		panel_2.add(lblUnconfirmed, "cell 0 3,gapx 10px");
 
 		lblUsrRoundSharesVal = new JLabel("N/A");
 		panel_2.add(lblUsrRoundSharesVal, "cell 1 3");
 
 		final JLabel lblTotalDonation = new JLabel("Total donation:");
 		lblTotalDonation.setFont(lblTotalDonation.getFont().deriveFont(lblTotalDonation.getFont().getStyle() | Font.BOLD));
 		panel_2.add(lblTotalDonation, "cell 2 3");
 
 		lblUsrTotalDonateVal = new JLabel("N/A");
 		panel_2.add(lblUsrTotalDonateVal, "cell 3 3");
 
 		final JLabel lblAllTime = new JLabel("All time:");
 		lblAllTime.setFont(lblConfirmed.getFont().deriveFont(lblConfirmed.getFont().getStyle() | Font.BOLD | Font.ITALIC));
 		panel_2.add(lblAllTime, "cell 0 4,gapx 10px");
 
 		lblUsrAllTimeSharesVal = new JLabel("N/A");
 		panel_2.add(lblUsrAllTimeSharesVal, "cell 1 4");
 
 		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBorder(new TitledBorder(null, "Worker Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.add(tabbedPane, "cell 0 2,growx,aligny top");
 
 		final JPanel panel_3 = new JPanel();
 		panel_3.setName("");
 		tabbedPane.addTab("worker-1", null, panel_3, null);
 		panel_3.setLayout(new MigLayout("", "[170]10px[200px]20px[170]10px[200px]", "[][][][]"));
 
 		final JLabel label_1 = new JLabel("Shares:");
 		label_1.setFont(label_1.getFont().deriveFont(label_1.getFont().getStyle() | Font.BOLD));
 		panel_3.add(label_1, "cell 0 0");
 
 		final JLabel label_2 = new JLabel("Last 15 mins:");
 		label_2.setFont(label_2.getFont().deriveFont(label_2.getFont().getStyle() | Font.BOLD | Font.ITALIC));
 		panel_3.add(label_2, "cell 0 1, gapbefore 15px");
 
 		final JLabel label_5 = new JLabel("N/A");
 		panel_3.add(label_5, "cell 1 1");
 
 		final JLabel label_3 = new JLabel("This round:");
 		label_3.setFont(label_3.getFont().deriveFont(label_3.getFont().getStyle() | Font.BOLD | Font.ITALIC));
 		panel_3.add(label_3, "cell 0 2, gapbefore 15px");
 
 		final JLabel label_6 = new JLabel("N/A");
 		panel_3.add(label_6, "cell 1 2");
 
 		final JLabel label_4 = new JLabel("All time:");
 		label_4.setFont(label_4.getFont().deriveFont(label_4.getFont().getStyle() | Font.BOLD | Font.ITALIC));
 		panel_3.add(label_4, "cell 0 3, gapbefore 15px");
 
 		final JLabel label_7 = new JLabel("N/A");
 		panel_3.add(label_7, "cell 1 3");
 
 		final JPanel panel_4 = new JPanel();
 		tabbedPane.addTab("worker-2", null, panel_4, null);
 
 		final JPanel panel_5 = new JPanel();
 		tabbedPane.addTab("worker-3", null, panel_5, null);
 
 		final JSeparator separator = new JSeparator();
 		frmRfctool.getContentPane().add(separator, "cell 0 2");
 
 		JPanel panel2 = new JPanel();
 		frmRfctool.getContentPane().add(panel2, "cell 0 3,growx,aligny bottom");
 		panel2.setLayout(new MigLayout("", "[][]push[]push[][][]", "[]"));
 
 		lblPoolLastUpdVal = new JLabel("N/A");
 		panel2.add(lblPoolLastUpdVal, "cell 2 0");
 		lblPoolLastUpdVal.setForeground(Color.DARK_GRAY);
 
 		final JButton btnHelpButton = new JButton("Help");
 		panel2.add(btnHelpButton, "flowx,cell 3 0");
 
 		final JComboBox comboRefreshDelay = new JComboBox();
 		comboRefreshDelay.setModel(new DefaultComboBoxModel(RefTime.values()));
 		panel2.add(comboRefreshDelay, "cell 0 0,growx");
 
 		final JButton btnNewButton_1 = new JButton("Refresh");
 		btnNewButton_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ExecutorService es = Executors.newSingleThreadExecutor();
 				es.execute(new Runnable() {
 					@Override
 					public void run() {		
 						updateAPI();
 					}
 				});
 			}
 		});
 		panel2.add(btnNewButton_1, "cell 1 0");
 
 		final JButton btnNewButton = new JButton("About");
 		panel2.add(btnNewButton, "flowx,cell 4 0");
 
 		final JButton btnApiKey = new JButton("API Key...");
 		btnApiKey.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				apiKey = (String)JOptionPane.showInputDialog(frmRfctool, "Enter API key:", "API Key", JOptionPane.PLAIN_MESSAGE, null, null, apiKey);
 				if (apiKey != null) {
 					PREFS.put("api_key", apiKey);
 				}
 				ExecutorService es = Executors.newSingleThreadExecutor();
 				es.execute(new Runnable() {
 					@Override
 					public void run() {
 						updateAPI();
 					}
 				});
 			}
 		});
 		panel2.add(btnApiKey, "cell 5 0");
 	}
 
 	public static final void setUpSubstanceLAF() {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			final Runnable r = new Runnable() {
 
 				@Override
 				public void run() {
 					setUpSubstanceLAF();
 				}
 			};
 			try {
 				SwingUtilities.invokeAndWait(r);
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			return;
 		}
 
 		// do setup
 		try {
 			UIManager.setLookAndFeel(new SubstanceRavenLookAndFeel()
 				// new SubstanceModerateLookAndFeel()
 			);
 			JFrame.setDefaultLookAndFeelDecorated(true);
 		} catch (UnsupportedLookAndFeelException e) {}
 	}
 
 	public void updateAPI() {
 		WorkingMgr mgr = null;
 		synchronized (stateLock) {
 			if (updating) return;
 			updating = true;
 			mgr = new WorkingMgr();
 		}
 		updatePoolStats();
 		updateUserStats();
 		updateUI();
 
 		synchronized (stateLock) {
 			mgr.stop();
 			updating = false;
 		}
 	}
 
 	public void updatePoolStats() {
 		if (SwingUtilities.isEventDispatchThread()) {
 			ExecutorService es = Executors.newSingleThreadExecutor();
 			es.execute(new Runnable() {
 				@Override
 				public void run() {
 					updatePoolStats();
 				}
 			});
 			return;
 		}
 		try {			
 			URI poolStatsURI = URI.create("http://www.rfcpool.com/api/pool/stats");
 			URL poolStatsURL = poolStatsURI.toURL();
 
 			InputStream content = (InputStream) poolStatsURL.getContent();
 			String input = readInputStreamAsString(content);
 			System.out.println("Got: " + input);
 
 			if (input == null || input.isEmpty()) {
 				synchronized (stateLock) {
 					poolStatus = Status.SITE_DOWN;
 				}
 				return;
 			}
 			try {
 				JSONObject jso = new JSONObject(input);
 				System.out.println("Got JSO: " + jso);
 				// "hashrate_unit":"GH/s","hashrate":24.92,"
 
 				JSONObject poolstats = jso.getJSONObject("poolstats");
 
 				synchronized (stateLock) {
 					hashrate_unit   = poolstats.getString("hashrate_unit");
 					hashrate        = poolstats.getDouble("hashrate");
 					roundTime       = poolstats.getInt("round_time");
 					blocksFound     = poolstats.getInt("blocks_found");
 					roundShares     = poolstats.getInt("round_shares");
 					workers         = poolstats.getInt("workers");
 					poolStatus      = Status.ALL_GOOD;
 				}
 				return;
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 
 	public void updateUI() {
 		if (!SwingUtilities.isEventDispatchThread()) {
 			Runnable r = new Runnable() {
 				@Override
 				public void run() {
 					updateUI();
 				}
 			};
 			SwingUtilities.invokeLater(r);
 			return;
 		}
 		synchronized (stateLock) {
 			lblHashrateVal.setText(hashrate + " " + hashrate_unit);
 			lblRoundSharesVal.setText(roundShares + "");
 			lblBlocksFoundVal.setText(blocksFound + "");
 			lblWorkersVal.setText(workers + "");
 
 			Seconds s = Seconds.seconds(roundTime);
 			Period period = new Period(s).normalizedStandard();
 			String periodStr = period.toString(DAYS_HOURS_MINS);
 
 			lblRoundTimeVal.setText(periodStr);
 
 			lblUsrEstRewardVal.setText(userEstimatedReward + "");
 			lblUsrTotalDonateVal.setText(userTotalDonated + "");
 			lblUsrTotalEarnVal.setText(userTotalEarnings + "");
 		}
 	}
 
 	public void updateUserStats() {
 		if (SwingUtilities.isEventDispatchThread()) {
 			ExecutorService es = Executors.newSingleThreadExecutor();
 			es.execute(new Runnable() {
 				@Override
 				public void run() {
 					updateUserStats();
 				}
 			});
 			return;
 		}
 		try {
 			URI userStatsURI = URI.create(
 				"http://www.rfcpool.com/api/user/account?key=" + apiKey
 					);
 			URL userStatsURL = userStatsURI.toURL();
 			HttpURLConnection conn = (HttpURLConnection) userStatsURL.openConnection();
 			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
 				synchronized (stateLock) {
 					userStatus = Status.API_KEY_INVALID;
 				}
 				return;				
 			}
 
 			InputStream content = (InputStream) userStatsURL.getContent();
 			String input = readInputStreamAsString(content);
 			System.out.println("Got: " + input);
 			if (input == null || input.isEmpty()) {
 				synchronized (stateLock) {
 					userStatus = Status.SITE_DOWN;
 				}
 				return;
 			}
 			try {
 				JSONObject jso = new JSONObject(input);
 				System.out.println("Got JSO: " + jso);
 				// "hashrate_unit":"GH/s","hashrate":24.92,"
 
 				JSONObject poolstats = jso.getJSONObject("account");
 				JSONObject  payouts   = poolstats.getJSONObject("payouts");
 				JSONObject  balances  = poolstats.getJSONObject("balances");
 
 				synchronized (stateLock) {
 					userTotalEarnings = poolstats.getDouble("total_earnings");
 					userTotalDonated = poolstats.getDouble("total_donated");
 					userEstimatedReward = poolstats.getDouble("estimated_reward");
 					userStatus = Status.ALL_GOOD;
 				}
 				return;
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static String readInputStreamAsString(InputStream in) throws IOException {
 		BufferedInputStream bis = new BufferedInputStream(in);
 		ByteArrayOutputStream buf = new ByteArrayOutputStream();
 		int result = bis.read();
 		while(result != -1) {
 			byte b = (byte)result;
 			buf.write(b);
 			result = bis.read();
 		}        
 		return buf.toString();
 	}
 
 	class WorkingMgr implements Runnable {
 		public static final long TIME_MS = 150;
 
 		public final long timeStartedMS = System.currentTimeMillis();
 
 		public volatile boolean stopped = false;
 
 		class SwingRun implements Runnable {
 			/* (non-Javadoc)
 			 * @see java.lang.Runnable#run()
 			 */
 			@Override
 			public void run() {
 				updateText();
 			}
 		}
 
 		protected final SwingRun swingRun = new SwingRun();
 
 		/**
 		 * 
 		 */
 		public WorkingMgr() {
 			Thread t = new Thread(this);
 			t.start();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Runnable#run()
 		 */
 		@Override
 		public void run() {
 			synchronized (stateLock) {
 				try {
 					while (!stopped) {
 						SwingUtilities.invokeLater(swingRun);
 						stateLock.wait(50);					
 					}
 				} catch (InterruptedException e) {
 					return;
 				}
 				lastUpdated = new DateTime();
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						synchronized (stateLock) {
 							lblPoolLastUpdVal.setText(lastUpdated.toString("HH:mm:ss aa"));
 
 							if (userStatus == Status.ALL_GOOD) {
 								lblPoolLastUpdVal.setText("upd @ " + lastUpdated.toString("HH:mm:ss aa"));
 							} else {
 								lblPoolLastUpdVal.setText(userStatus.toString());
 							} 
 						}
 					}
 				});
 			}
 		}
 
 		protected void updateText() {
 			synchronized (stateLock) {
 				if (!stopped) {
 					lblPoolLastUpdVal.setText(UPD_ARR[dots()]);
 				}
 			}
 		}
 
 		protected long timeDiff() {
 			return System.currentTimeMillis() - timeStartedMS;
 		}
 
 		protected int dots() {
 			return (int)(timeDiff() / 50d) % 5;
 		}
 
 		public void stop() {
 			synchronized (stateLock) {
 				stopped = true;
 				stateLock.notifyAll();
 			}
 		}
 	}


 }
