 package HAS_Tools;
 // Kindlet imports
 import com.amazon.kindle.kindlet.AbstractKindlet;
 import com.amazon.kindle.kindlet.KindletContext;
 import com.amazon.kindle.kindlet.ui.KindletUIResources.*;
 import com.amazon.kindle.kindlet.ui.KMenu;
 import com.amazon.kindle.kindlet.ui.KMenuItem;
 import com.amazon.kindle.kindlet.ui.KRepaintManager;
 import com.amazon.kindle.kindlet.input.Gestures;
 import com.amazon.kindle.kindlet.ui.KOptionPane;
 import com.amazon.kindle.kindlet.input.keyboard.*;
 // Utilities
 import java.util.Calendar;
 import java.util.ArrayList;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 // Ui Imports
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.border.LineBorder;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.ActionMap;
 import javax.swing.Action;
 import javax.swing.AbstractAction;
 import javax.swing.SwingUtilities;
 // awt imports
 import java.awt.FlowLayout;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.CardLayout;
 //import java.awt.event.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 //import java.awt.event.TextListener;
 //import java.awt.event.TextEvent;
 import java.awt.Point;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.Color;
 import java.awt.Dimension;
 // Process
 import java.lang.Process;
 import java.lang.Runtime;
 
 public class Logbook extends AbstractKindlet {
 	public KindletContext ctx;
 	public Container root;
 
 	// Data
 	public String date;
 	public String save_date;
 	public static String dir = "/mnt/us/developer/APP Logbook/work/";
 	public String filename;
 	public FlightModel fmodel;
 	public Flight flight; // Flight being edited or Added
 	public String flight_backup;
 	public char[] def_crew = {'S',' ','A'};
 	// UI Components
 	public KRepaintManager screenManager;
 	public CardLayout card;
 	public JLabel logs_day_info;
 	public JTable logs_cells;
 	public JTextField name_input;
 	public JPanel name_buttons;
 	public JPanel alt_buttons;
 	public CardLayout card_notes;
 	public JPanel notes;
 	public JPanel notes_tandem;
 	public JPanel notes_solo;
 	public JPanel work_tug;
 	public JButton work_tug_but;
 	public JPanel work_crew;
 	public JButton work_crew_but;
 	public JPanel work_tandem;
 	public JButton work_tandem_but;
 
 	public void create(KindletContext context) {
 		this.ctx = context;
 		this.root = ctx.getRootContainer();
 		screenManager = KRepaintManager.getInstance();
 		get_date();
 		String[] crew_list = load_list(dir.concat("config/crew.csv"));
 		Flight.init(crew_list);
 		fmodel = new FlightModel(filename, save_date);
 		try {
 			// Set Fonts to Use
 			Font menu_font = ctx.getUIResources().getFont(KFontFamilyName.MONOSPACE,14);
 			Font table_font = ctx.getUIResources().getFont(KFontFamilyName.MONOSPACE,10);
 			Font edit_font = ctx.getUIResources().getFont(KFontFamilyName.MONOSPACE,12);
 			// Setup menu Once
 			KMenu menu = new KMenu();
 			menu.add("Save",new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					fmodel.save();
 					KOptionPane.showMessageDialog(root,"Data Saved");
 				}
 			});
 			menu.add("Sync",new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					fmodel.save();
 					if(python("sync.py"))
 						KOptionPane.showMessageDialog(root,"Sync Completed");
 					else KOptionPane.showMessageDialog(root,"Sync Failed");
 				}
 			});
 			menu.add("Update",new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					fmodel.save();
 					if(python("update.py"))
 						KOptionPane.showMessageDialog(root,"Update Completed");
 					else KOptionPane.showMessageDialog(root,"Update Failed");
 				}
 			});
 			ctx.setMenu(menu);
 			// Setup Log UI
 			logs_day_info = new JLabel(date.concat("   Flights: ") // Info
 					.concat(Integer.toString(fmodel.getFlightCount())));
 			logs_day_info.setFont(menu_font);
 			logs_day_info.setBorder(new LineBorder(Color.black));
 			JPanel log = new JPanel(new BorderLayout()); // Log
 			logs_cells = new JTable(fmodel);
 			logs_cells.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
 			logs_cells.setRowSelectionAllowed(true);
 			logs_cells.setFont(table_font);
 			logs_cells.setRowHeight(29);
 			logs_cells.getColumnModel().getColumn(0).setPreferredWidth(50); // #
 			logs_cells.getColumnModel().getColumn(1).setPreferredWidth(306); // Name
 			logs_cells.getColumnModel().getColumn(2).setPreferredWidth(130); // Notes
 			logs_cells.getColumnModel().getColumn(3).setPreferredWidth(50); // Alt
 			logs_cells.getColumnModel().getColumn(4).setPreferredWidth(50); // Work
 			logs_cells.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent e) {
 					switch(e.getButton()) {
 						case Gestures.BUTTON_FLICK_NORTH :
 						case Gestures.BUTTON_FLICK_WEST :
 							fmodel.next_page();
 							break;
 						case Gestures.BUTTON_FLICK_SOUTH :
 						case Gestures.BUTTON_FLICK_EAST :
 							fmodel.prev_page();
 							break;
 						case Gestures.BUTTON_HOLD : // Enter Edit Mode
 							Point p = e.getPoint();
 							int row = logs_cells.rowAtPoint(p);
 							int col = logs_cells.columnAtPoint(p);
 							int flight_num = Integer.parseInt((String)logs_cells.getValueAt(row,0));
 							flight = (Flight)fmodel.fdata.get(flight_num-1); // Set Flight
 							flight_backup = flight.toString();
 							switch(col) {
 								case 0 : // # -> Delete Dialog
 									StringBuffer to_del = new StringBuffer();
 									to_del.append(Integer.toString(flight_num)).append(" ?\n");
 									to_del.append(' ').append(flight.name).append('\n');
 									to_del.append(' ').append(flight.notes).append('\n');
 									to_del.append(' ').append(flight.getAltstr()).append('\n');
 									to_del.append(' ').append(new String(flight.work));
 									int result = KOptionPane.showConfirmDialog(root,
 											"Save A copy of flight #".concat(to_del.toString()),
 											"Duplication Confirmation",
 											KOptionPane.CANCEL_SAVE_OPTIONS);
 									if(result!=KOptionPane.CANCEL_OPTION) {
 										fmodel.addFlight((Flight)fmodel.fdata.get(flight_num-1));
 										screenManager.repaint(root,false);
 										break;
 									}
 									result = KOptionPane.showConfirmDialog(root,
 											"**Delete** flight #".concat(to_del.toString()),
 											"Deletion Confirmation",
 											KOptionPane.NO_YES_OPTIONS);
 									if(result==KOptionPane.YES_OPTION) {
 										fmodel.remove(flight_num);
 										update_log();
 									}
 									break;
 								case 1 : // Name
 									get_name(false);
 									break;
 								case 2 : // Notes
 								case 3 : // Altitude
 								case 4 : // Work Logs
 									get_other(false);
 									break;
 								default :
 							}
 							break;
 						case Gestures.BUTTON_GROW :
 						case Gestures.BUTTON_SHRINK :
 						case Gestures.BUTTON_TAP :
 					}
 				}
 				public void mouseReleased(MouseEvent e) { }
 				public void mousePressed(MouseEvent e) { }
 				public void mouseExited(MouseEvent e) { }
 				public void mouseEntered(MouseEvent e) { }
 			});
 			logs_cells.getTableHeader().setBorder(new LineBorder(Color.black));
 			log.add(logs_cells.getTableHeader(),BorderLayout.NORTH);
 			log.add(logs_cells,BorderLayout.CENTER);
 			JButton tandem = new JButton("Add Tandem"); // Tandem
 			tandem.setFont(menu_font);
 			tandem.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent e) {
 					int row = fmodel.fdata.indexOf(flight);
 					switch(e.getButton()) {
 						case Gestures.BUTTON_HOLD : // Duplicate Flight
 							int pos = fmodel.fdata.size()-1;
 							while(pos>=0) { // Get next similar flight
 								Flight cur = (Flight)fmodel.fdata.get(pos--);
 								if(cur.solo==false) {
 									fmodel.addFlight(new Flight(cur.toString()));
 									screenManager.repaint(root,false);
 									return;
 								}
 							}
 						case Gestures.BUTTON_TAP : // Add Flight
 							flight_backup = null;
 							flight = fmodel.createFlight(false,def_crew);
 							get_name(false);
 							break;
 					}
 				}
 				public void mouseReleased(MouseEvent e) { }
 				public void mousePressed(MouseEvent e) { }
 				public void mouseExited(MouseEvent e) { }
 				public void mouseEntered(MouseEvent e) { }
 			});
 			JButton solo = new JButton("Add Solo"); // Solo
 			solo.setFont(menu_font);
 			solo.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent e) {
 					int row = fmodel.fdata.indexOf(flight);
 					switch(e.getButton()) {
 						case Gestures.BUTTON_HOLD : // Duplicate Flight
 							int pos = fmodel.fdata.size()-1;
 							while(pos>=0) { // Get next similar flight
 								Flight cur = (Flight)fmodel.fdata.get(pos--);
 								if(cur.solo==true) {
 									fmodel.addFlight(new Flight(cur.toString()));
 									screenManager.repaint(root,false);
 									return;
 								}
 							}
 						case Gestures.BUTTON_TAP : // Add Flight
 							flight_backup = null;
 							flight = fmodel.createFlight(true,def_crew);
 							get_name(true);
 							break;
 					}
 				}
 				public void mouseReleased(MouseEvent e) { }
 				public void mousePressed(MouseEvent e) { }
 				public void mouseExited(MouseEvent e) { }
 				public void mouseEntered(MouseEvent e) { }
 			});
 			JPanel log_buttons = new JPanel(new GridLayout(1,2)); // Log Buttons
 			log_buttons.add(tandem);
 			log_buttons.add(solo);
 
 			// Name Card
 			ActionListener name_listener = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					String name = e.getSource() instanceof JTextField ?
 						name_input.getText() : ((JButton)e.getSource()).getText();
 					if( name.equals("") ) return;
 					flight.name = name;
 					if(flight_backup==null) get_other(true); // Add Flight
 					else { // Edit Flight
 						card.show(root, "logs");
 					}
 				}
 			};
 			name_input = new JTextField();
 			name_input.setFont(edit_font);
 			name_input.addActionListener(name_listener);
 			name_input.getDocument().addDocumentListener(new DocumentListener() {
 				public void changedUpdate(DocumentEvent e) { textValueChanged(e); }
 				public void insertUpdate(DocumentEvent e) { textValueChanged(e); }
 				public void removeUpdate(DocumentEvent e) { textValueChanged(e); }
 				public void textValueChanged(DocumentEvent e) {
 					Process match = null;
 					BufferedReader matches = null;
 					if(name_input.getText().equals("")) {
 						mod_recent_names();
 						return;
 					}
 					String[] cmd = {"/bin/sh", dir.concat(".dropbox/match_")
 						.concat(flight.solo?"solo":"tandem"),name_input.getText()};
 					try { 
 						match = Runtime.getRuntime().exec(cmd);
 						matches = new BufferedReader(new InputStreamReader(match.getInputStream()));
 						String line; int i=0; Component[] buts = name_buttons.getComponents();
 						while((line=matches.readLine())!=null) {
 							buts[i].setEnabled(true);
 							((JButton)buts[i]).setText(line);
 							i++;
 						}
 						matches.close();
 						while(i<buts.length) {
 							buts[i++].setEnabled(false);
 						}
 						screenManager.repaint(name_buttons,false);
 					} 
 					catch(IOException x) { x.printStackTrace();}
 					//catch(InterruptedException x) { match.destroy(); matches.close(); }
 				}
 			});
 			OnscreenKeyboardUtil.configure(name_input, OnscreenKeyboardUtil.KEYBOARD_MODE_INIT_CAP_ONCE);
 			JButton name_cancel = new JButton("Cancel");
 			name_cancel.setFont(edit_font);
 			name_cancel.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					card.show(root, "logs");
 				}
 			});
 			JPanel name_head = new JPanel(new BorderLayout());
 			name_head.add(name_input, BorderLayout.CENTER);
 			name_head.add(name_cancel, BorderLayout.EAST);
 			name_buttons = new JPanel(new GridLayout(10,1));
 			for(int i=0;i<10;i++) {
 				JButton but = new JButton();
 				but.setFont(edit_font);
 				but.addActionListener(name_listener);
 				name_buttons.add(but);
 			}
 			JLabel name_keyboard_space = new JLabel("Keyboard Space",JLabel.CENTER);
 			name_keyboard_space.setPreferredSize(ctx.getOnscreenKeyboardManager().getSize());
 
 			// Other Card
 			String[] alt_button_text = {"X","Pat","2500","3500","Mile","10k"}; // Alt
 			alt_buttons = new JPanel();
 			for(int i=0;i<alt_button_text.length;i++){
 				JButton but = new JButton(alt_button_text[i]);
 				but.setFont(edit_font);
 				but.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						int alt = flight.alt;
 						alt_buttons.getComponent(alt).setEnabled(true);
 						JButton but = (JButton)e.getSource(); 
 						mod_alt_button(but.getText().charAt(0),false);
 					}
 				});
 				alt_buttons.add(but);
 			}
 			String[] tandem_list = load_list(dir.concat("config/notes.tandem.csv"));
 			String[] solo_list = load_list(dir.concat("config/notes.solo.csv"));
 			notes_tandem = new JPanel(); // Notes
 			notes_solo = new JPanel();
 			for(int i=0;i<tandem_list.length;i++) {
 				JCheckBox but = new JCheckBox(tandem_list[i],false);
 				but.setFont(edit_font);
 				but.setPreferredSize(new Dimension(200,50));
 				notes_tandem.add(but);
 			}
 			for(int i=0;i<solo_list.length;i++) {
 				JCheckBox but = new JCheckBox(solo_list[i],false);
 				but.setFont(edit_font);
 				but.setPreferredSize(new Dimension(200,50));
 				notes_solo.add(but);
 			}
 			card_notes = new CardLayout();
 			notes = new JPanel();
 			notes.setLayout(card_notes);
 			notes.add(notes_tandem, "tandem");
 			notes.add(notes_solo, "solo");
 			//String[] crew_list = load_list(dir.concat("config/crew.csv"));
 			work_tug = new JPanel(new GridLayout(crew_list.length,1));// Work Logs
 			work_tug.add(new JLabel("Tug"));
 			work_tug.add(new JLabel());
 			work_crew = new JPanel(new GridLayout(crew_list.length,1));
 			work_crew.add(new JLabel("Crew"));
 			work_tandem = new JPanel(new GridLayout(crew_list.length,1));
 			work_tandem.add(new JLabel("Tan"));
 			work_tandem.add(new JLabel());
 			for(int i=1;i<crew_list.length;i++) {
 				JButton worker;
 				String initial = crew_list[i].substring(0,1);
 				char def_char = crew_list[i].charAt(2);
 				if(def_char=='@') { // Default Tandem Pilot
 					def_crew[2]=initial.charAt(0);
 				}
 				else if(def_char=='*') { // Default Tug Pilot
 					def_crew[0]=initial.charAt(0);
 				}
 				int index = crew_list[i].lastIndexOf(",");
 				if(crew_list[i].charAt(index+1)=='1') {
 					worker = new JButton(initial); worker.setFont(edit_font); work_crew.add(worker);
 					worker.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							work_crew_but.setEnabled(true);
 							work_crew_but = (JButton)e.getSource();
 							flight.work[1] = work_crew_but.getText().charAt(0);
 							work_crew_but.setEnabled(false);
 						}
 					});
 				}
 				if(crew_list[i].charAt(index-3)=='1') {
 					worker = new JButton(initial); worker.setFont(edit_font); work_tug.add(worker);
 					worker.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							work_tug_but.setEnabled(true);
 							work_tug_but = (JButton)e.getSource();
 							flight.work[0] = work_tug_but.getText().charAt(0);
 							work_tug_but.setEnabled(false);
 						}
 					});
 				}
 				if(crew_list[i].charAt(index-1)=='1') {
 					worker = new JButton(initial); worker.setFont(edit_font); work_tandem.add(worker);
 					worker.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							work_tandem_but.setEnabled(true);
 							work_tandem_but = (JButton)e.getSource();
 							flight.work[2] = work_tandem_but.getText().charAt(0);
 							work_tandem_but.setEnabled(false);
 						}
 					});
 				}
 			}
 			JPanel work_logs = new JPanel(new FlowLayout(FlowLayout.CENTER,30,0));
 			work_logs.add(work_tug);
 			work_logs.add(work_crew);
 			work_logs.add(work_tandem);
 			JPanel other_center = new JPanel(new GridLayout(1,2)); // Other Center
 			other_center.add(notes);
 			other_center.add(work_logs);
 			JButton other_save = new JButton("Save (Hold to Cancel)"); // Other Buttons
 			other_save.setFont(menu_font);
 			other_save.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent e) {
 					int row = fmodel.fdata.indexOf(flight);
 					switch(e.getButton()) {
 						case Gestures.BUTTON_HOLD : // Cancel Add/Eddit
 							if(flight_backup!=null)
 								fmodel.fdata.set(row,new Flight(flight_backup));
 							break;
 						case Gestures.BUTTON_TAP : // Save Changes
 							StringBuffer new_notes = new StringBuffer();
 							Component[] buts = flight.solo ?
 								notes_solo.getComponents():
 								notes_tandem.getComponents();
 							for(int i=0;i<buts.length;i++) {
 								JCheckBox but = (JCheckBox)buts[i];
 								if(but.isSelected())
 									new_notes.append(but.getText()).append("/");
 							}
 							if(new_notes.length() > 0)
 								flight.notes = new_notes.deleteCharAt(new_notes.length()-1).toString();
							else flight.notes = "";
 							if(flight_backup==null) {
 								fmodel.addFlight(flight);
 								update_log();
 							} else fmodel.update_row(row);
 							break;
 					}
 					card.show(root,"logs");
 					alt_buttons.getComponent(flight.alt).setEnabled(true);
 					work_tug_but.setEnabled(true);
 					work_crew_but.setEnabled(true);
 					if(!flight.solo) work_tandem_but.setEnabled(true);
 				}
 				public void mouseReleased(MouseEvent e) { }
 				public void mousePressed(MouseEvent e) { }
 				public void mouseExited(MouseEvent e) { }
 				public void mouseEntered(MouseEvent e) { }
 			});
 
 			JPanel log_card = new JPanel(new BorderLayout());
 			log_card.add(logs_day_info, BorderLayout.NORTH);
 			log_card.add(log, BorderLayout.CENTER);
 			log_card.add(log_buttons, BorderLayout.SOUTH);
 
 			JPanel name_card = new JPanel(new BorderLayout());
 			name_card.add(name_head, BorderLayout.NORTH);
 			name_card.add(name_buttons, BorderLayout.CENTER);
 			name_card.add(name_keyboard_space, BorderLayout.SOUTH);
 
 			JPanel other_card = new JPanel(new BorderLayout());
 			other_card.add(alt_buttons, BorderLayout.NORTH);
 			other_card.add(other_center, BorderLayout.CENTER);
 			other_card.add(other_save, BorderLayout.SOUTH);
 
 			card = new CardLayout();
 			root.setLayout(card);
 			root.add(log_card,"logs");
 			root.add(name_card,"name");
 			root.add(other_card,"other");
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 
 	public void start() {
 
 	}
 	public void stop() { // On Sleep, USB, Exit
 		fmodel.save(); // Write Info to file 
 	}
 	public void destroy() {
 
 	}
 
 	private String week_toString(int day) {
 		switch(day){
 			case Calendar.MONDAY : return "Mon";
 			case Calendar.TUESDAY : return "Tue";
 			case Calendar.WEDNESDAY : return "Wed";
 			case Calendar.THURSDAY : return "Thu";
 			case Calendar.FRIDAY : return "Fri";
 			case Calendar.SATURDAY : return "Sat";
 			case Calendar.SUNDAY : return "Sun";
 			default : return "";
 		}
 	}
 	private String month_toString(int month) {
 		switch(month) {
 			case Calendar.JANUARY : return  "Jan";
 			case Calendar.FEBRUARY : return  "Feb";
 			case Calendar.MARCH : return  "Mar";
 			case Calendar.APRIL : return  "Apr";
 			case Calendar.MAY : return  "May";
 			case Calendar.JUNE : return  "Jun";
 			case Calendar.JULY : return  "Jul";
 			case Calendar.AUGUST : return  "Aug";
 			case Calendar.SEPTEMBER : return  "Sep";
 			case Calendar.OCTOBER : return  "Oct";
 			case Calendar.NOVEMBER : return  "Nov";
 			case Calendar.DECEMBER : return  "Dec";
 			default : return "";
 		}
 	}
 	private void get_date() {
 		Calendar today = Calendar.getInstance();
 		date = new StringBuffer()
 			.append("  ")
 			.append( week_toString(today.get(Calendar.DAY_OF_WEEK)))
 			.append(", ")
 			.append(month_toString(today.get(Calendar.MONTH)))
 			.append(" ")
 			.append(Integer.toString(today.get(Calendar.DAY_OF_MONTH)))
 			.toString();
 		save_date = new StringBuffer()
 			.append(Integer.toString(today.get(Calendar.DAY_OF_MONTH)))
 			.append('-')
 			.append(month_toString(today.get(Calendar.MONTH)))
 			.toString();
 		filename = new StringBuffer(dir)
 			.append(Integer.toString(today.get(Calendar.YEAR)))
 			.append( (today.get(Calendar.MONTH)+1)<10 ?"-0":"-")
 			.append(Integer.toString(today.get(Calendar.MONTH)+1))
 			.append("-")
 			.append(Integer.toString(today.get(Calendar.DAY_OF_MONTH)))
 			.append(".csv")
 			.toString();
 	}
 	private void update_log() {
 		logs_day_info.setText(date.concat("   Flights: ")
 					.concat(Integer.toString(fmodel.getFlightCount())));
 	}
 	private void get_name(boolean add) {
 		if(add) {
 			mod_recent_names();
 		} else {
 			Component[] buts = name_buttons.getComponents();
 			int pos = fmodel.fdata.size()-1;
 			for(int i=0;i<buts.length;i++) {
 				JButton but = (JButton) buts[i];
 				if(pos<0) {
 					but.setText("");
 					but.setEnabled(false);
 				}
 			}
 			name_input.setText(flight.name);
 		}
 		// initiate grep
 		card.show(root, "name");
 		name_input.requestFocus();
 	}
 	private void mod_recent_names() {
 		Component[] buts = name_buttons.getComponents();
 		int pos = fmodel.fdata.size()-1;
 		name_input.setText("");
 		for(int i=0;i<buts.length;i++) {
 			JButton but = (JButton) buts[i];
 			next_similar:
 			while(pos>=0) { // Get next similar flight
 				Flight cur = (Flight)fmodel.fdata.get(pos--);
 				if(flight.solo!=cur.solo) continue;
 				for(int j=0;j<i;j++) {
 					if(cur.name.equals(((JButton)buts[j]).getText()))
 						continue next_similar;
 				}
 				but.setEnabled(true);
 				but.setText(cur.name);
 				break;
 			}
 			if(pos<0) {
 				but.setText("");
 				but.setEnabled(false);
 			}
 		}
 	}
 	private void get_other(boolean add) {
 		mod_alt_button(' ',false);
 		mod_notes_list();
 		mod_work_log();
 		if(flight.solo) work_tandem.setVisible(false);
 		else work_tandem.setVisible(true);
 		card.show(root, "other");
 	}
 	private void mod_alt_button(char alt, boolean enable) {
 		switch(alt) {
 			case 'X' : flight.alt = 0; break;
 			case 'P' : flight.alt = 1; break;
 			case '2' : flight.alt = 2; break;
 			case '3' : flight.alt = 3; break;
 			case 'M' : flight.alt = 4; break;
 			case '1' : flight.alt = 5; break;
 			case ' ' : default : break;
 		}
 		alt_buttons.getComponent(flight.alt).setEnabled(enable);
 	}
 	private void mod_notes_list() {
 		Component[] buts = flight.solo ?
 			notes_solo.getComponents():
 			notes_tandem.getComponents();
 		for(int i=0;i<buts.length;i++) {
 			JCheckBox but = (JCheckBox)buts[i];
 			but.setSelected( flight.notes.indexOf(but.getText())!=-1 );
 		}
 		card_notes.show(notes,flight.solo ? "solo" : "tandem");
 	}
 	private void mod_work_log() {
 		Component[] buts = work_tug.getComponents();
 		for(int i=2;i<buts.length;i++) {
 			if(((JButton)buts[i]).getText().charAt(0)==flight.work[0]) {
 				work_tug_but = (JButton)buts[i]; work_tug_but.setEnabled(false); break;
 			}
 		}
 		buts = work_crew.getComponents();
 		for(int i=1;i<buts.length;i++) {
 			if(((JButton)buts[i]).getText().charAt(0)==flight.work[1]) {
 				work_crew_but = (JButton)buts[i]; work_crew_but.setEnabled(false); break;
 			}
 		}
 		if(flight.solo) return;
 		buts = work_tandem.getComponents();
 		for(int i=2;i<buts.length;i++) {
 			if(((JButton)buts[i]).getText().charAt(0)==flight.work[2]) {
 				work_tandem_but = (JButton)buts[i]; work_tandem_but.setEnabled(false); break;
 			}
 		}
 	}
 	private String[] load_list(String filename) {
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(filename));
 			String line;
 			ArrayList list = new ArrayList(10);
 			while((line=in.readLine())!=null) {
 				list.add(line);
 			}
 			in.close();
 			return (String[]) list.toArray(new String[0]);
 		} catch(IOException e) {
 			String[] failed = {"Failed","To","Get","File",filename.substring(filename.lastIndexOf('/'))};
 			return failed;
 		}
 	}
 	private boolean python(String script) {
 		if(!ctx.getConnectivity().isConnected()) return false;
 		String[] cmd = {"/mnt/us/python/usr/bin/python2.7",dir.concat(".dropbox/").concat(script)};
 		Process python=null;
 		try {
 			python = Runtime.getRuntime().exec(cmd);
 			try {
 				screenManager.repaint(root,true);
 			} catch(IllegalStateException e) { }
 			python.waitFor();
 		}
 		catch(IOException e) { e.printStackTrace(); return false; }
 		catch(InterruptedException e) { if(python!=null)python.destroy(); return false; }
 		return true;
 	}
 }
 
 class FlightModel extends AbstractTableModel {
 	private String[] col_names = {"#","Pilot","Notes","Alt","Wrk"};
 	public ArrayList fdata;
 	public String filename;
 	public String save_date;
 	public int page;
 	public int page_size = 20;
 
 	public FlightModel(String filename, String save_date) {
 		this.page = 0;
 		this.filename = filename;
 		this.save_date = save_date;
 		this.fdata = new ArrayList(30);
 		try{ 
 			BufferedReader in = new BufferedReader(new FileReader(filename));
 			String line;
 			while((line = in.readLine()) != null) {
 				this.fdata.add(new Flight(line));
 			}
 			in.close();
 		} catch(IOException e) {
 			System.err.println(e);
 		}
 	}
 	public void save() { this.save(this.filename); }
 	public void save(String filename) {
 		// Create String to Record
 		StringBuffer save = new StringBuffer();
 		int index = 0;
 		while(index < fdata.size()) {
 			save.append(save_date).append(this.fdata.get(index++).toString()).append('\n');
 		}
 		// Write to file
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
 			out.write(save.toString());
 			out.close();
 		} catch(IOException e) {
 			System.err.println(e);
 		}
 	}
 	public int getColumnCount() { return col_names.length; }
 	public int getRowCount() { return fdata.size()-this.page*this.page_size; }
 	public int getFlightCount() { return fdata.size(); }
 	public String getColumnName(int col) { return col_names[col]; }
 	public Object getValueAt(int row, int col) {
 		switch(col) {
 			case 0 : return Integer.toString(row+1+(this.page*this.page_size));
 			case 1 : return ((Flight)fdata.get(row+this.page*this.page_size)).name;
 			case 2 : return ((Flight)fdata.get(row+this.page*this.page_size)).notes;
 			case 3 : return ((Flight)fdata.get(row+this.page*this.page_size)).getAltstr();
 			case 4 : return new String(((Flight)fdata.get(row+this.page*this.page_size)).work);
 			default: return "E";
 		}
 	}
 	public Class getColumnClass(int col) { return "".getClass(); }
 	public void prev_page() {
 		if(this.page>0) {
 			this.page--;
 			fireTableDataChanged();
 		}
 	}
 	public void next_page() {
 		if(this.fdata.size() > (this.page+1) * this.page_size) {
 			this.page++;
 			fireTableDataChanged();
 		}
 	}
 	public void setValueAt(Object value,int row,int col) {
 		((Flight)this.fdata.get(row+this.page*this.page_size)).setCol(col,(String)value);
 		fireTableCellUpdated(row-(this.page*this.page_size),col);
 	}
 	public void update_row(int urow) {
 		fireTableRowsUpdated(urow,urow);
 	}
 	public void remove(int flight_num) {
 		fdata.remove(flight_num-1);
 		int page_index = flight_num%this.page_size - 1;
 		if(flight_num - this.page*this.page_size > 0) { // Last Page
 			if(flight_num-1==this.fdata.size()) // Last Row
 				if(page_index == 0) prev_page();
 				else fireTableRowsDeleted(page_index,page_index);
 			else {
 				fireTableRowsDeleted(page_index,page_index);
 				fireTableRowsUpdated(page_index,this.fdata.size()-1-this.page*this.page_size);
 			}
 		} else fireTableRowsUpdated(page_index,19); // Current Page
 	}
 	public void addFlight(Flight newf) {
 		this.fdata.add(newf);
 		int size = this.fdata.size();
 		this.page = size!=0 ? (size-1)/this.page_size : 0;
 		if(size%this.page_size==1) {
 			fireTableDataChanged();
 		} else fireTableRowsInserted(this.fdata.size(),this.fdata.size());
 	}
 	public Flight createFlight(boolean solo,char[] def_crew) {
 		Flight newf = null;
 		for(int i=fdata.size()-1;i>=0;i--) { // Search for matching
 			newf = (Flight)fdata.get(i); 
 			if(newf.solo==solo) break;
 			else newf = null;
 		}
 		if(newf!=null) {
 			newf = new Flight("","",newf.alt,newf.work);
 		} else {
 			if(solo) {
 				def_crew[2]=' ';
 				newf = new Flight("","",2,def_crew);
 			}
 			else newf = new Flight("","",2,def_crew);
 		}
 		return newf;
 	}
 }
 
 class Flight {
 	public String name;
 	public String notes;
 	public int alt;
 	public char[] work = new char[3];
 	public boolean solo;
 	private static char[] initials;
 	private static String[] names;
 	public static void init(String[] crew_list) {
 		initials = new char[crew_list.length];
 		names = new String[crew_list.length];
 		crew_list[1] = crew_list[1].substring(0,2)
 			.concat(crew_list[1].substring(crew_list[1].indexOf(',',2)));
 		for(int i=1;i<crew_list.length;i++) {
 			String line = crew_list[i];
 			initials[i] = line.charAt(0);
 			char first = line.charAt(2);
 			int s_i = first=='@'||first=='*' ? 3 : 2;
 			names[i] = line.substring(s_i,line.indexOf(',',s_i));
 		}
 	}
 	private static String i_name(char initial) {
 		for(int i=0;i<initials.length;i++)
 			if(initial==initials[i]) return names[i];
 		return "";
 	}
 	private static char name_i(String name) {
 		for(int i=0;i<names.length;i++)
 			if(name.equals(names[i])) return initials[i];
 		return ' ';
 	}
 	public Flight(String name, String notes, int alt, char[] wrk) {
 		this.name = name;
 		this.notes = notes;
 		this.alt = alt; // 0,1,2,3,4
 		this.work[0] = wrk[0];
 		this.work[1] = wrk[1];
 		this.work[2] = wrk[2];
 		this.solo = this.work[2]==' ';
 	}
 	public Flight( String data ) {
 		int prev_i = data.indexOf(','), next_i = data.indexOf(',',++prev_i);
 		this.name = data.substring(prev_i,next_i); // Name
 		prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
 		this.alt = Flight.alt_parse(data.substring(prev_i,next_i)); // Tandem Alt
 		prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
 		if(this.alt == -1) this.alt = Flight.alt_parse(data.substring(prev_i,next_i)); // Solo Alt
 		prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
 		switch(data.charAt(prev_i)) { // Tug Pilot
 			case ',' : this.work[0]=' '; break;
 			case '*' : prev_i++;
 			default : 
 				this.work[0]= name_i(data.substring(prev_i,next_i));
 				prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
 		} 
 		switch(data.charAt(prev_i)) { // Crew
 			case ',' : this.work[1]=' '; break;
 			default : 
 				this.work[1]= name_i(data.substring(prev_i,next_i));
 				prev_i = next_i + 1; next_i = data.indexOf(',',prev_i);
 		} 
 		switch(data.charAt(prev_i)) { // Tandem Pilot
 			case ',' : this.work[2]=' '; break;
 			case '@' : prev_i++;
 			default : 
 				this.work[2]= name_i(data.substring(prev_i,next_i));
 		} 
 		this.notes = data.substring(data.lastIndexOf(',')+1); // Notes
 		this.solo = this.work[2]==' ';
 	}
 	public void setCol(int col,String val) {
 		switch(col){
 			case 1 : // Name
 				this.name = val;
 				break;
 			case 2 : // Notes
 				this.notes = val;
 				break;
 			case 3 : // Alt
 				this.alt = Integer.parseInt(val);
 				break;
 			case 4 : // Work
 				this.work[0] = val.charAt(0);
 				this.work[1] = val.charAt(1);
 				this.work[2] = val.charAt(2);
 				break;
 		}
 	}
 	public String toString() {
 		String alt_str = "";
 		switch(this.alt) {
 			case 0 : alt_str = "0"; break;
 			case 1 : alt_str = "0.5"; break;
 			case 2 : alt_str = "1"; break;
 			case 3 : alt_str = "2"; break;
 			case 4 : alt_str = "4"; break;
 		}
 		StringBuffer str = new StringBuffer();
 		str.append(',').append(this.name).append(','); // Name
 		if(this.work[2]!=' ') {
 			str.append(alt_str).append(',').append(','); // Tandem Alt #
 		} else {
 			str.append(',').append(alt_str).append(','); // Solo Alt #
 		}
 		str.append(i_name(this.work[0])).append(','); // Tug Pilot
 		if(this.work[1]!=' ') str.append(i_name(this.work[1])); // Crew
 		str.append(',');
 		if(this.work[2]!=' ') str.append(i_name(this.work[2])); // Tandem Pilot
 		str.append(',');
 		str.append(this.notes); // Rental/Notes
 		return str.toString();
 	}
 	public String getAltstr(){
 		switch(this.alt) {
 			case 0 : return "X";
 			case 1 : return "Pat";
 			case 2 : return "25";
 			case 3 : return "35";
 			case 4 : return "1m";
 			case 5 : return "10k";
 		}
 		return "Err";
 	}
 	private static int alt_parse(String alt) {
 		if(alt.length()==0) { return -1; };
 		if(alt.length()>1)
 			switch(alt.charAt(0)) {
 				case '0': return 1; // 0.5
 				case '1': return 3; // 1.5 - 3500
 			}
 		switch(alt.charAt(0)) {
 			case '0' : return 0; // 0 
 			case '1' : return 2; // 1
 			case '2' : return 4; // 2
 			case '4' : return 5; // 4
 		}
 		return -1;
 	}
 }
 /* --ToDo--
  *  -- Code --
  * fix reload all after update.py
  *  -- Scripts --
  * Sync
  *   Do dropbox conservative syncing/updating
  *   improve sync to properly sync all data
  * match_*
  *   case "s s"
  */
