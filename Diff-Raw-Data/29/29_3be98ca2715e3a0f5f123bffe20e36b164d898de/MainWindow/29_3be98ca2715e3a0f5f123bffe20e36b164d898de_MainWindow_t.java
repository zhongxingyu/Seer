 package nl.ttva66.client;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingWorker;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 
 import nl.ttva66.client.output.Generator;
 import nl.ttva66.client.output.PDF;
 import nl.ttva66.dto.DagDto;
 import nl.ttva66.dto.DienstDto;
 import nl.ttva66.dto.OpenDto;
 import nl.ttva66.dto.ZaaldienstDto;
 import nl.ttva66.interfaces.DagRequest;
 import nl.ttva66.interfaces.Item;
 import nl.ttva66.interfaces.Request;
 import nl.ttva66.interfaces.ZaalDienstRequest;
 import nl.ttva66.libary.Collectie;
 
 public class MainWindow {
 
 	public JFrame frame;
 	private JTable table;
 	private JLabel lblMonth;
 	private DefaultTableModel mtblCalendar;
 	private JProgressBar progressBar;
 	public static ZaaldienstDto ik;
 
 	private int realYear, realMonth, realDay, currentYear, currentMonth;
 
 	private Stack<Request> queue;
 	private Stack<Request> wait;
 
 	private int size = 0;
 
 	private int[][] data;
 	private ArrayList<DagRequest> dagdata = new ArrayList<DagRequest>();
 	private Color[][] kleur;
 
 	private Thread t;
 	public static MainWindow window;
 
 	/**
 	 * Create the application.
 	 */
 	public MainWindow() {
 
 		initialize();
 
 		t = new Thread(new runQueue());
 		t.start();
 
 		refreshCalendar(realMonth, realYear); // Refresh calendar
 
 	}
 
 	private void addQueue(Request d) {
 		if (wait.contains(d) || queue.contains(d)) {
 			return;
 		}
 
 		queue.add(d);
 
 		interruptThread();
 
 	}
 
 	private void interruptThread() {
 		synchronized (t) {
 			t.interrupt();
 		}
 	}
 
 	public class runQueue implements Runnable {
 
 		public void run() {
 
 			while (true) {
 				if (queue.size() != 0) {
 					size += queue.size();
 
 					progressBar.setMaximum(size);
 					progressBar.setStringPainted(true);
 
 					Iterator<Request> itr = queue.iterator();
 
 					while (itr.hasNext()) {
 
 						Request request = queue.pop();
 						wait.push(request);
 
 						Task t = new Task(request);
 						t.execute();
 					}
 
 				} else {
 					try {
 						synchronized (this) {
 							this.wait();
 						}
 
 					} catch (InterruptedException e1) {
 
 					}
 
 				}
 			}
 		}
 	}
 
 	class Task extends SwingWorker<Void, Void> {
 		private Request task;
 
 		public Task(Request d) {
 			task = d;
 		}
 
 		/*
 		 * Main task. Executed in background thread.
 		 */
 		@Override
 		public Void doInBackground() {
 			Item add = null;
 
 			if (task instanceof DagRequest) {
 
 				add = API.getDag((DagRequest) task);
 			} else if (task instanceof ZaalDienstRequest) {
 				add = API.getZaalDienst((ZaalDienstRequest) task);
 			} else // Don't remove this else! (It will cause a infitite loop)
 			{
 				throw new RuntimeException("Task not defined");
 			}
 
 			if (add == null) {
 				System.out.println("Got null?");
 				return null;
 			} else {
 				System.out.println("Got valid data: " + add.getId() + " "
 						+ add.getClass());
 			}
 
 			API.items.add(add);
 			wait.remove(task);
 
 			return null;
 		}
 
 		/*
 		 * Executed in event dispatching thread
 		 */
 		@Override
 		public void done() {
 			int progress = progressBar.getValue() + 1;
 			progressBar.setValue(progress);
 
 			size--;
 
 			if (size < 0) {
 				size = 0;
 			}
 
 			if (size == 0) {
 				refreshCalendar(currentMonth, currentYear);
 			}
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		API.items = new Collectie();
 
 		queue = new Stack<Request>();
 		wait = new Stack<Request>();
 
 		frame = new JFrame();
 		frame.setTitle("TTV Alexandria zaalbeheer");
 
 		// frame.setBounds(100, 100, 450, 300);
 		frame.pack();
 		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowClosing(WindowEvent winEvt) {
 				// Perhaps ask user if they want to save any unsaved files
 				// first.
 				askSave();
 			}
 		});
 
 		mtblCalendar = new DefaultTableModel() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1038831398502625463L;
 
 			public boolean isCellEditable(int rowIndex, int mColIndex) {
 				return false;
 			}
 		};
 		table = new JTable(mtblCalendar);
 		JScrollPane stblCalendar = new JScrollPane(table);
 
 		frame.getContentPane().add(stblCalendar, BorderLayout.CENTER);
 
 		// Get real month/year
 		GregorianCalendar cal = new GregorianCalendar(); // Create calendar
 		realDay = cal.get(GregorianCalendar.DAY_OF_MONTH); // Get day
 		realMonth = cal.get(GregorianCalendar.MONTH); // Get month
 		realYear = cal.get(GregorianCalendar.YEAR); // Get year
 		currentMonth = realMonth; // Match month and year
 		currentYear = realYear;
 
 		// Add headers
 		String[] headers = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
 		for (int i = 0; i < 7; i++) {
 			mtblCalendar.addColumn(headers[i]);
 		}
 
 		table.getParent().setBackground(table.getBackground()); // Setbackground
 
 		table.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent e) {
 				int row = table.rowAtPoint(e.getPoint());
 				int col = table.columnAtPoint(e.getPoint());
 
 				// Going to check if value is a day.
 
 				if (data[row][col] != -1) {
 					// Here should we open a new frame.
 
 					EditDay frame = new EditDay(dagdata.get(data[row][col]),
 							window);
 					frame.setVisible(true);
 
 				} else {
 					return;
 				}
 			}
 		});
 
 		// No resize/reorder
 		table.getTableHeader().setResizingAllowed(false);
 		table.getTableHeader().setReorderingAllowed(false);
 
 		// Single cell selection
 		table.setColumnSelectionAllowed(true);
 		table.setRowSelectionAllowed(true);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		// Set row/column count
 		table.setRowHeight(200);
 		mtblCalendar.setColumnCount(7);
 		mtblCalendar.setRowCount(6);
 
 		data = new int[7][7];
 		kleur = new Color[7][7];
 
 		JButton button_1 = new JButton(">");
 		frame.getContentPane().add(button_1, BorderLayout.EAST);
 
 		JButton button_2 = new JButton("<");
 		frame.getContentPane().add(button_2, BorderLayout.WEST);
 
 		button_2.addActionListener(new btnPrev_Action());
 		button_1.addActionListener(new btnNext_Action());
 
 		progressBar = new JProgressBar();
 		frame.getContentPane().add(progressBar, BorderLayout.SOUTH);
 
 		lblMonth = new JLabel("");
 		lblMonth.setHorizontalAlignment(SwingConstants.CENTER);
 		frame.getContentPane().add(lblMonth, BorderLayout.NORTH);
 
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		JMenu mnBestand = new JMenu("Bestand");
 		menuBar.add(mnBestand);
 
 		JMenuItem mntmVoegPersoonToe = new JMenuItem("Voeg persoon toe");
 		mnBestand.add(mntmVoegPersoonToe);
 		mntmVoegPersoonToe.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				AddZaalWacht w = new AddZaalWacht(null);
 				w.setVisible(true);
 
 			}
 		});
 
 		JMenuItem mntmWijzig = new JMenuItem("Wijzig persoon");
 		mnBestand.add(mntmWijzig);

 		mntmWijzig.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				EditZaalwacht w = new EditZaalwacht();
 				w.setVisible(true);
 			}
 		});
 
 		JMenuItem mntmMijzelf = new JMenuItem("Mijn account");
 		mnBestand.add(mntmMijzelf);
 
 		mntmMijzelf.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				AddZaalWacht w = new AddZaalWacht(ik);
 				w.setVisible(true);
 
 			}
 		});
 
 		JMenuItem mntmSave = new JMenuItem("Opslaan");
 		mnBestand.add(mntmSave);
 		mntmSave.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				save();
 			}
 		});
 
 		JMenuItem mntmSet = new JMenuItem("Instellingen");
 		mnBestand.add(mntmSet);
 		mntmSet.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Settings st = new Settings();
 				st.setVisible(true);
 			}
 		});
 
 		JMenuItem mntmRefresh = new JMenuItem("Vernieuwen");
 		mnBestand.add(mntmRefresh);
 		mntmRefresh.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				refresh();
 			}
 		});
 
 		JMenuItem mntmAfsluiten = new JMenuItem("Afsluiten");
 		mntmAfsluiten.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				askSave();
 			}
 		});
 
 		mnBestand.add(mntmAfsluiten);
 
 		JMenu mnUitvoer = new JMenu("Uitvoer");
 		menuBar.add(mnUitvoer);
 
 		JMenuItem mntmScherm = new JMenuItem("Scherm");
 		mnUitvoer.add(mntmScherm);
 
 		JMenuItem mntmIcal = new JMenuItem("iCal");
 		mnUitvoer.add(mntmIcal);
 
 		JMenuItem mntmExcel = new JMenuItem("Excel");
 		mnUitvoer.add(mntmExcel);
 
 		JMenuItem mntmPdf = new JMenuItem("PDF");
 		mnUitvoer.add(mntmPdf);
 
 		JMenu menuEmail = new JMenu("Email");
 
 		JMenuItem mntmIedereen = new JMenuItem("Iedereen");
 		menuEmail.add(mntmIedereen);
 		JMenuItem mntmBestuur = new JMenuItem("Bestuur");
 		menuEmail.add(mntmBestuur);
 		JMenuItem mntmEnkel = new JMenuItem("Persoon");
 		menuEmail.add(mntmEnkel);
 
 		mnUitvoer.add(menuEmail);
 
 		mntmIedereen.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 
 				new SendAllMail(new DagRequest(-1, currentMonth, currentYear),
 						window);
 
 			}
 		});
 
 		mntmPdf.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					PDF p = new PDF(frame);
 					Generator g = new Generator(frame, p, new DagRequest(-1,
 							currentMonth, currentYear), null);
 
 					if (g.genereer()) {
 						JOptionPane.showMessageDialog(frame,
 								"PDF is opgeslagen op " + p.getFile(),
 								"PDF Opgeslagen.",
 								JOptionPane.INFORMATION_MESSAGE);
 					}
 				} catch (Exception e) {
 
 				}
 			}
 
 		});
 
 		menuBar.add(mntmRefresh);
 
 	}
 
 	protected void refresh() {
 		queue.removeAllElements();
 		wait.removeAllElements();
 		API.items.removeAll();
 		refreshCalendar(currentMonth, currentYear);
 
 	}
 
 	int count = 0;
 
 	private void refreshCalendar(int month, int year) {
 		count++;
 
 		// Variables
 		String[] months = { "januari", "februari", "maart", "april", "mei",
 				"juni", "juli", "augustus", "september", "oktober", "november",
 				"december" };
 		int nod, column; // Number Of Days, Start Of Month
 
 		String st = months[month] + " " + currentYear;
 
 		lblMonth.setText(st); // Refresh the month label (at the top)
 
 		// Clear table
 		for (int i = 0; i < 6; i++) {
 			for (int j = 0; j < 7; j++) {
 				mtblCalendar.setValueAt(null, i, j);
 				data[i][j] = -1;
 				kleur[i][j] = new Color(255, 255, 255);
 			}
 		}
 
 		// Get first day of month and number of days
 		GregorianCalendar cal = new GregorianCalendar(year, month, 1);
 		nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
 		int nod2 = nod + 7;
 		column = cal.get(GregorianCalendar.DAY_OF_WEEK);
 
 		if (column == 1) {
 			column = 6;
 		} else {
 			column -= 2;
 		}
 
 		int row = 0;
 
 		int mt = this.currentMonth;
 		int yr = this.currentYear;
 
 		// Draw calendar
 		for (int i = 1, ct = 1; i <= nod2; i++, ct++) {
 			try {
 
 				if (i == nod) {
 					if (mt == 11) {
 						mt = -1;
 						yr++;
 					}
 					mt++;
 					ct = 1;
 				}
 				String dt = Integer.toString(ct) + " " + months[mt] + " " + yr;
 
 				mtblCalendar.setValueAt("<html>" + dt + "</html>", row, column);
 				data[row][column] = i - 1;
 				dagdata.add(new DagRequest(ct, mt, yr));
 
 				Color kl;
 
 				if (column == 5 || column == 6) { // Week-end
 					kl = new Color(255, 220, 220);
 				} else { // Week
 					kl = new Color(255, 255, 255);
 				}
 
 				if (i != 0) {
 					System.out.println("Doing request for " + ct);
 
 					DagRequest d = new DagRequest(ct, mt, yr);
 
 					DagDto dag = (DagDto) API.items.get(d);
 
 					if (dag == null) {
 						kl = new Color(255, 255, 0);
 						addQueue(d);
 					} else {
 						String vl = "<html>" + dt;
 
 						vl += "<br />";
 
 						Set<OpenDto> delen = API.sortOpen(dag.getOpens());
 						System.out.println("DTO size open: " + delen.size());
 
						open = false;
						definitief = true;
						zaaldienst = false;

 						for (OpenDto deel : delen) {
 							vl += addDeel(dag, deel);
 						}
 
 						if (open) {
 							if (!zaaldienst) {
 								kl = new Color(255, 0, 0);
 							} else if (!definitief) {
 								kl = new Color(0, 0, 255);
 							} else {
 								kl = new Color(0, 255, 0);
 							}
 						} else {
 							kl = new Color(0xd3, 0xd3, 0xd3);
 						}
 						open = false;
 						zaaldienst = true;
 
 						mtblCalendar.setValueAt(vl, row, column);
 					}
 
 					if (i == realDay && mt == realMonth && yr == realYear) { // Today
 						kl = new Color(220, 220, 255);
 					}
 
 				}
 
 				kleur[row][column] = kl;
 
 				column++;
 
 				if (column == 7) {
 					column = 0;
 					row++;
 				}
 
 			} catch (ArrayIndexOutOfBoundsException e) {
 
 			}
 		}
 		// Apply renderers
 		table.setDefaultRenderer(table.getColumnClass(0),
 				new tblCalendarRenderer());
 
 	}
 
 	private boolean open = false;
 	private boolean definitief = true;
 	private boolean zaaldienst = false;
 
 	private String addDeel(DagDto dag, OpenDto opn) {
 
 		String vl = opn.getType().getNaam() + ": ";
 
 		vl += (opn.isOpen()) ? "Open" : "Gesloten";
 		vl += "<br />";
 
 		if (opn.isOpen()) {
 			open = true;
 
 			Set<DienstDto> dt = new HashSet<DienstDto>();
 
 			for (DienstDto d : dag.getDienst()) {
 				if (d.getType().getId() == opn.getType().getId()) {
 					dt.add(d);
 				}
 			}
 
 			if (dt.size() == 0) {
 				// Geen zaaldienst toegewezen.
 
 				vl += "Geen zaaldienst toegewezen<br />";
 				zaaldienst = false;
 			} else {
 				String result = "";
 
 				int i = 0;
 				for (DienstDto dienst : dt) {
 					if (dienst == null) {
 						// Skip it.
 						continue;
 					}
 
 					if (!dienst.isDefinitief()) {
 						definitief = false;
 					}

					if (!dienst.isBackup()) {
 						zaaldienst = true;
 					}
 
 					ZaalDienstRequest r = new ZaalDienstRequest(
 							dienst.getZaaldienst());
 					ZaaldienstDto zt = (ZaaldienstDto) API.items.get(r);
 
 					if (i != 0) {
 						result += ", ";
 					}
 					i++;
 
 					if (zt == null) {
 						addQueue(r);
 						result += "Ophalen";
 					} else {
 						result += zt.getNaam();
 
						if (dienst.isBackup() || !dienst.isDefinitief()) {
							result += "(";
							
							if (dienst.isBackup()) {
								result += "backup";
							}

							if (!dienst.isDefinitief()) {
								if (dienst.isBackup())
								{
									result += ", ";
								}
								result += "niet bevestigd";
							}
							result += ")";
 						}
 					}
 				}
 
 				vl += "Zaaldienst: " + result + "<br />";
 			}
 		}
 		return vl;
 	}
 
 	private void askSave() {
 		if (API.items.changed()) {
 
 			// Custom button text
 			Object[] options = { "Save", "Discard", "Cancel" };
 			int n = JOptionPane
 					.showOptionDialog(
 							frame,
 							"Er zijn nog niet opgeslagen dagen, weet je zeker dat je wilt afsluiten?",
 							"Afsluiten", JOptionPane.YES_NO_CANCEL_OPTION,
 							JOptionPane.QUESTION_MESSAGE, null, options,
 							options[2]);
 
 			switch (n) {
 			case 0:
 				save();
 			case 1:
 				System.exit(0);
 			case 2:
 			default:
 
 				return;
 			}
 		}
 
 		System.exit(0);
 	}
 
 	private void save() {
 		// Server srv = null;
 
 		// srv = API.getServer();
 
 		while (API.items.changed()) {
 			Item upd = API.items.getChanged();
 
 			/*
 			 * try { if (upd instanceof Dag) { srv.saveDag((Dag) upd); } else if
 			 * (upd instanceof Zaaldienst) { srv.saveZaalDienst((Zaaldienst)
 			 * upd); } else { //upd.setChanged(false); } } catch
 			 * (ServerException e) { // Should not happen? throw new
 			 * RuntimeException( "There is no result found at the server?"); }
 			 */
 
 			API.items.remove(upd);
 		}
 	}
 
 	class tblCalendarRenderer extends DefaultTableCellRenderer {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 2563920070160037133L;
 
 		public Component getTableCellRendererComponent(JTable table, Object v,
 				boolean selected, boolean focused, int row, int column) {
 			super.getTableCellRendererComponent(table, v, selected, focused,
 					row, column);
 
 			setBackground(kleur[row][column]);
 			setBorder(null);
 			setForeground(Color.black);
 			return this;
 		}
 
 	}
 
 	class btnPrev_Action implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (currentMonth == 0) { // Back one year
 				currentMonth = 11;
 				currentYear -= 1;
 			} else { // Back one month
 				currentMonth -= 1;
 			}
 			refreshCalendar(currentMonth, currentYear);
 		}
 	}
 
 	class btnNext_Action implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (currentMonth == 11) { // Foward one year
 				currentMonth = 0;
 				currentYear += 1;
 			} else { // Foward one month
 				currentMonth += 1;
 			}
 			refreshCalendar(currentMonth, currentYear);
 		}
 	}
 }
