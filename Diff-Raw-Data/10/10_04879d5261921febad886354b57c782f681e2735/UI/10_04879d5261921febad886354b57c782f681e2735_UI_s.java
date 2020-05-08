 /**
  * 
  * Notië Imberissëo
  * 
  * Copyright 2012 Joaquín Gatica (Erutulco Eruntano)
  * 
  * Contact:
  * 		Twitter: <http://twitter.com/joaquingatica>
  * 		Email: <erutulco@quenya101.com>
  * 
  * This file is part of "Notië Imberissëo".
  * 
  * "Notië Imberissëo" is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * "Notië Imberissëo" is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with "Notië Imberissëo".  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package main;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JComboBox;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;
 
 import javax.swing.JLabel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import data.GregorianInfo;
 import data.ImladrisInfo;
 import fonts.FontManager;
 
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextPane;
 
 import java.awt.Font;
 import javax.swing.JCheckBox;
 import javax.swing.JTabbedPane;
 
 import java.awt.Cursor;
 import java.awt.Desktop;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Color;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.JScrollPane;
 import javax.swing.JEditorPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import lang.Lang;
 import lang.LangManager;
 
 public class UI implements HyperlinkListener {
 	
 	/* FRAME */
 	private JFrame frmNotiImberisso;
 	private JTabbedPane tabbedPane;
 	
 	/* GREGORIAN to IMLADRIS */
 	private JTextField year;
 	private JComboBox month;
 	private JComboBox day;
 	private JCheckBox afterSunset;
 	private JButton toImladris;
 	private JTextPane resImladris;
 	
 	/* IMLADRIS to GREGORIAN */
 	private JTextField loa;
 	private JComboBox yen;
 	private JComboBox period;
 	private JComboBox dayOfLoa;
 	private JCheckBox beforeMidnight;
 	private JButton toGregorian;
 	private JTextPane resGregorian;
 	
 	/* SETTINGS */
 	private JTextField city;
 	private JTextField country;
 	private JComboBox langCombo;
 	private JList timeZone;
 	private JLabel saveResult;
 	
 	/* ABOUT */
 	private JEditorPane aboutEditor;
 	
 	/* TODAY */
 	private JLabel locationInfo;
 	private JTextPane todayGregorian;
 	private JTextPane todayImladris;
 	
 	/* Frame */
 	public JFrame getFrame() {
 		return frmNotiImberisso;
 	}
 	public void setFrame(JFrame frame) {
 		this.frmNotiImberisso = frame;
 	}
 	public JTabbedPane getTabbedPane() {
 		return tabbedPane;
 	}
 	public void setTabbedPane(JTabbedPane tabbedPane) {
 		this.tabbedPane = tabbedPane;
 	}
 	
 	/* Gregorian to Imladris */
 	public JTextField getYear() {
 		return year;
 	}
 	public void setYear(JTextField year) {
 		this.year = year;
 	}
 	public JComboBox getMonth() {
 		return month;
 	}
 	public void setMonth(JComboBox month) {
 		this.month = month;
 	}
 	public JComboBox getDay() {
 		return day;
 	}
 	public void setDay(JComboBox day) {
 		this.day = day;
 	}
 	public JCheckBox getAfterSunset() {
 		return afterSunset;
 	}
 	public void setAfterSunset(JCheckBox afterSunset) {
 		this.afterSunset = afterSunset;
 	}
 	public JButton getToImladris() {
 		return toImladris;
 	}
 	public void setToImladris(JButton toImladris) {
 		this.toImladris = toImladris;
 	}
 	public JTextPane getResImladris() {
 		return resImladris;
 	}
 	public void setResImladris(JTextPane resImladris) {
 		this.resImladris = resImladris;
 	}
 	
 	/* Imladris to Gregorian */
 	public JTextField getLoa() {
 		return loa;
 	}
 	public void setLoa(JTextField loa) {
 		this.loa = loa;
 	}
 	public JComboBox getYen() {
 		return yen;
 	}
 	public void setYen(JComboBox yen) {
 		this.yen = yen;
 	}
 	public JComboBox getPeriod() {
 		return period;
 	}
 	public void setPeriod(JComboBox period) {
 		this.period = period;
 	}
 	public JComboBox getDayOfLoa() {
 		return dayOfLoa;
 	}
 	public void setDayOfLoa(JComboBox dayOfLoa) {
 		this.dayOfLoa = dayOfLoa;
 	}
 	public JCheckBox getBeforeMidnight() {
 		return beforeMidnight;
 	}
 	public void setBeforeMidnight(JCheckBox beforeMidnight) {
 		this.beforeMidnight = beforeMidnight;
 	}
 	public JButton getToGregorian() {
 		return toGregorian;
 	}
 	public void setToGregorian(JButton toGregorian) {
 		this.toGregorian = toGregorian;
 	}
 	public JTextPane getResGregorian() {
 		return resGregorian;
 	}
 	public void setResGregorian(JTextPane resGregorian) {
 		this.resGregorian = resGregorian;
 	}
 	
 	/* Settings */
 	public JTextField getCity() {
 		return city;
 	}
 	public void setCity(JTextField city) {
 		this.city = city;
 	}
 	public JTextField getCountry() {
 		return country;
 	}
 	public void setCountry(JTextField country) {
 		this.country = country;
 	}
 	public JComboBox getLangCombo() {
 		return langCombo;
 	}
 	public void setLangCombo(JComboBox langCombo) {
 		this.langCombo = langCombo;
 	}
 	public JList getTimeZone() {
 		return timeZone;
 	}
 	public void setTimeZone(JList timeZone) {
 		this.timeZone = timeZone;
 	}
 	public JLabel getSaveResult() {
 		return saveResult;
 	}
 	public void setSaveResult(JLabel saveResult) {
 		this.saveResult = saveResult;
 	}
 	
 	/* Today */
 	public JLabel getLocationInfo() {
 		return locationInfo;
 	}
 	public void setLocationInfo(JLabel locationInfo) {
 		this.locationInfo = locationInfo;
 	}
 	public JTextPane getTodayGregorian() {
 		return todayGregorian;
 	}
 	public void setTodayGregorian(JTextPane todayGregorian) {
 		this.todayGregorian = todayGregorian;
 	}
 	public JTextPane getTodayImladris() {
 		return todayImladris;
 	}
 	public void setTodayImladris(JTextPane todayImladris) {
 		this.todayImladris = todayImladris;
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	private static UI instance = null;
 	public static UI getInstance() {
 		if(instance == null) {
 			instance = new UI();
 		}
 		return instance;
 	}
 	public static void disposeInstance() {
 		instance = null;
 	}
 	private UI() {
 		initialize();
 	}
 	
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
         /* Set system Look & Feel */
 		try {
 	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 	    } 
 	    catch (UnsupportedLookAndFeelException e) {
 	    }
 	    catch (ClassNotFoundException e) {
 	    }
 	    catch (InstantiationException e) {
 	    }
 	    catch (IllegalAccessException e) {
 	    }
 		
 		/* Register fonts */
 		FontManager.registerAvailableFonts();
 		
 		/* Get app icon */
 		Image icon = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("res/images/icon.png"));
 		
 		frmNotiImberisso = new JFrame();
 		frmNotiImberisso.setTitle(Lang.common.app_title);
 		frmNotiImberisso.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmNotiImberisso.setSize(600, 380);
 		frmNotiImberisso.setLocation(300,200);
 		frmNotiImberisso.setIconImage(icon);
 		if(Lang.uses_tengwar) {
 			frmNotiImberisso.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		};
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{520, 0};
 		gridBagLayout.rowHeights = new int[]{227, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		frmNotiImberisso.getContentPane().setLayout(gridBagLayout);
 		
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
 		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
 		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
 		gbc_tabbedPane.gridx = 0;
 		gbc_tabbedPane.gridy = 0;
 		frmNotiImberisso.getContentPane().add(tabbedPane, gbc_tabbedPane);
 		
 		JPanel panel_1 = new JPanel();
 		tabbedPane.addTab(Lang.to_imladris_tab.title, null, panel_1, Lang.to_imladris_tab.tooltip);
 		if(Lang.uses_tengwar) {
 			tabbedPane.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.setBorder(null);
 		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("max(2dlu;default)"),
 				ColumnSpec.decode("172px"),
 				ColumnSpec.decode("32px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("55px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("32px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(20dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(14dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(5dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(12dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblYear = new JLabel(Lang.to_imladris_tab.year_label);
 		lblYear.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblYear.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(lblYear, "5, 2, 3, 1, center, default");
 		
 		JLabel lblMonth = new JLabel(Lang.to_imladris_tab.month_label);
 		lblMonth.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblMonth.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(lblMonth, "9, 2, 5, 1, center, default");
 		
 		JLabel lblDay = new JLabel(Lang.to_imladris_tab.day_label);
 		lblDay.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblDay.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(lblDay, "15, 2, 3, 1, center, default");
 		
 		JLabel lblChooseGregorianDate = new JLabel(Lang.to_imladris_tab.choose_label);
 		if(Lang.uses_tengwar) {
 			lblChooseGregorianDate.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(lblChooseGregorianDate, "2, 4, 2, 1, fill, fill");
 		
 		year = new JTextField();
 		year.setToolTipText(Lang.to_imladris_tab.year_tooltip);
 		year.addFocusListener(new FocusAdapter() {
 			public void focusLost(FocusEvent e) {
 				UIController.getInstance().updateDayComboGregorian();
 			}
 		});
 		if(Lang.uses_tengwar) {
 			year.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(year, "5, 4, 3, 1, fill, fill");
 		
 		month = new JComboBox();
 		month.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				UIController.getInstance().updateDayComboGregorian();
 			}
 		});
 		month.setModel(new DefaultComboBoxModel(GregorianInfo.getInstance().getMonthsArray()));
 		panel_1.add(month, "9, 4, 5, 1, fill, fill");
 		
 		day = new JComboBox();
 		day.setEnabled(false);
 		panel_1.add(day, "15, 4, 3, 1, fill, fill");
 		
 		toImladris = new JButton(">>");
 		toImladris.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				UIController.getInstance().convertToImladris();
 			}
 		});
 		toImladris.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		toImladris.setEnabled(false);
 		panel_1.add(toImladris, "11, 6, 7, 1, fill, fill");
 		
 		afterSunset = new JCheckBox(Lang.to_imladris_tab.after_sunset_label);
 		afterSunset.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			afterSunset.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		afterSunset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		panel_1.add(afterSunset, "5, 6, 5, 1");
 		
 		JLabel lblResultingImladrisDate = new JLabel(Lang.to_imladris_tab.resulting_label);
 		if(Lang.uses_tengwar) {
 			lblResultingImladrisDate.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(lblResultingImladrisDate, "2, 10, 2, 1, fill, fill");
 		
 		resImladris = new JTextPane();
 		resImladris.setEditable(false);
 		resImladris.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		panel_1.add(resImladris, "5, 10, 13, 1, fill, fill");
 		
 		JLabel lblFormat = new JLabel(Lang.punctuation.parenthesis_open+Lang.to_imladris_tab.resulting_format+Lang.punctuation.parenthesis_close);
 		lblFormat.setFont(new Font("Dialog", Font.PLAIN, 10));
 		if(Lang.uses_tengwar) {
 			lblFormat.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_1.add(lblFormat, "5, 12, 13, 1, fill, fill");
 		
 		JPanel panel_2 = new JPanel();
 		tabbedPane.addTab(Lang.from_imladris_tab.title, null, panel_2, Lang.from_imladris_tab.tooltip);
 		panel_2.setBorder(null);
 		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(87dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("left:max(13dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(12dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(8dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(9dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(5dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblYn = new JLabel(Lang.from_imladris_tab.yen_label);
 		lblYn.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblYn.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_2.add(lblYn, "6, 2, 3, 1, center, default");
 		
 		JLabel lblLoa = new JLabel(Lang.from_imladris_tab.loa_label);
 		lblLoa.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblLoa.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_2.add(lblLoa, "10, 2, 3, 1, center, default");
 		
 		JLabel lblPeriod = new JLabel(Lang.from_imladris_tab.period_label);
 		lblPeriod.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblPeriod.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_2.add(lblPeriod, "14, 2, 5, 1, center, default");
 		
 		JLabel lblNewLabel = new JLabel(Lang.from_imladris_tab.day_label);
 		lblNewLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblNewLabel.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_2.add(lblNewLabel, "20, 2, 3, 1, center, default");
 		
 		JLabel lblS = new JLabel(Lang.from_imladris_tab.choose_label);
 		if(Lang.uses_tengwar) {
 			lblS.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_2.add(lblS, "2, 4, 3, 1, fill, fill");
 		
 		yen = new JComboBox();
 		yen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				UIController.getInstance().updateDayComboImladris();
 			}
 		});
 		yen.setModel(new DefaultComboBoxModel(ImladrisInfo.getInstance().getYeniArray()));
 		panel_2.add(yen, "6, 4, 3, 1, fill, fill");
 		
 		loa = new JTextField();
 		loa.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				UIController.getInstance().updateDayComboImladris();
 			}
 		});
 		loa.setToolTipText(Lang.from_imladris_tab.loa_tooltip);
 		panel_2.add(loa, "10, 4, 3, 1, fill, fill");
 		loa.setColumns(10);
 		
 		period = new JComboBox();
 		period.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				UIController.getInstance().updateDayComboImladris();
 			}
 		});
 		period.setModel(new DefaultComboBoxModel(ImladrisInfo.getInstance().getPeriodsArray()));
 		panel_2.add(period, "14, 4, 5, 1, fill, fill");
 		
 		dayOfLoa = new JComboBox();
 		dayOfLoa.setEnabled(false);
 		panel_2.add(dayOfLoa, "20, 4, 3, 1, fill, fill");
 		
 		toGregorian = new JButton(">>");
 		toGregorian.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				UIController.getInstance().convertToGregorian();
 			}
 		});
 		toGregorian.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		toGregorian.setEnabled(false);
 		panel_2.add(toGregorian, "16, 6, 7, 1, fill, fill");
 		
 		beforeMidnight = new JCheckBox(Lang.from_imladris_tab.before_midnight_label);
 		beforeMidnight.setFont(new Font("Dialog", Font.PLAIN, 12));
 		beforeMidnight.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		panel_2.add(beforeMidnight, "6, 6, 9, 1, fill, fill");
 		
 		JLabel lblNewLabel_1 = new JLabel(Lang.from_imladris_tab.resulting_label);
 		panel_2.add(lblNewLabel_1, "2, 10, 3, 1, fill, fill");
 		
 		resGregorian = new JTextPane();
 		resGregorian.setEditable(false);
 		resGregorian.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		panel_2.add(resGregorian, "6, 10, 17, 1, fill, fill");
 		
 		JLabel lblNewLabel_2 = new JLabel(Lang.punctuation.parenthesis_open+Lang.from_imladris_tab.resulting_format+Lang.punctuation.parenthesis_close);
 		lblNewLabel_2.setFont(new Font("Dialog", Font.PLAIN, 10));
 		if(Lang.uses_tengwar) {
 			lblNewLabel_2.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_2.add(lblNewLabel_2, "6, 12, 17, 1");
 		
 		JPanel panel = new JPanel();
 		panel.setToolTipText("");
 		tabbedPane.addTab(Lang.settings_tab.title, null, panel, Lang.settings_tab.tooltip);
 		panel.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(61dlu;default):grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(112dlu;default)"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(16dlu;default):grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblLocation = new JLabel(Lang.settings_tab.location);
 		if(Lang.uses_tengwar) {
 			lblLocation.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel.add(lblLocation, "4, 2, center, center");
 		
 		JLabel lblTimezone = new JLabel(Lang.settings_tab.timezone);
 		if(Lang.uses_tengwar) {
 			lblTimezone.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel.add(lblTimezone, "8, 2, center, center");
 		
 		JLabel lblCity = new JLabel(Lang.settings_tab.city_label+Lang.punctuation.double_dot);
 		if(Lang.uses_tengwar) {
 			lblCity.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel.add(lblCity, "2, 4, right, fill");
 		
 		city = new JTextField();
 		panel.add(city, "4, 4, fill, fill");
 		city.setColumns(10);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		panel.add(scrollPane, "8, 4, 1, 11, fill, fill");
 		
 		timeZone = new JList();
 		scrollPane.setViewportView(timeZone);
 		timeZone.setBorder(null);
 		timeZone.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		JLabel lblCountry = new JLabel(Lang.settings_tab.country_label+Lang.punctuation.double_dot);
 		if(Lang.uses_tengwar) {
 			lblCountry.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel.add(lblCountry, "2, 6, right, fill");
 		
 		country = new JTextField();
 		panel.add(country, "4, 6, fill, fill");
 		country.setColumns(10);
 		
 		JLabel lblLanguage = new JLabel(Lang.settings_tab.language_label+Lang.punctuation.double_dot);
 		if(Lang.uses_tengwar) {
 			lblLanguage.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel.add(lblLanguage, "2, 10, right, default");
 		
 		langCombo = new JComboBox();
 		langCombo.setModel(new DefaultComboBoxModel(LangManager.getLanguagesPrintable()));
 		panel.add(langCombo, "4, 10, fill, default");
 		
 		saveResult = new JLabel("");
 		saveResult.setForeground(Color.GREEN);
 		if(Lang.uses_tengwar) {
 			saveResult.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel.add(saveResult, "2, 12, 3, 1, right, fill");
 		
 		JButton btnSave = new JButton(Lang.settings_tab.save);
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				UIController.getInstance().saveSettings();
 			}
 		});
 		if(Lang.uses_tengwar) {
 			btnSave.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		panel.add(btnSave, "4, 14, right, fill");
 		
 		JPanel panel_4 = new JPanel();
 		tabbedPane.addTab(Lang.about_tab.title, null, panel_4, null);
 		GridBagLayout gbl_panel_4 = new GridBagLayout();
 		gbl_panel_4.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panel_4.rowHeights = new int[]{23, 0, 0, 0};
 		gbl_panel_4.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_4.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		panel_4.setLayout(gbl_panel_4);
 		
 		JLabel lblNotiImberisso = new JLabel(Lang.about_tab.app_name+" "+Lang.common.app_version);
 		lblNotiImberisso.setFont(new Font("Dialog", Font.BOLD, 12));
 		if(Lang.uses_tengwar) {
 			lblNotiImberisso.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		GridBagConstraints gbc_lblNotiImberisso = new GridBagConstraints();
 		gbc_lblNotiImberisso.anchor = GridBagConstraints.SOUTH;
 		gbc_lblNotiImberisso.gridwidth = 19;
 		gbc_lblNotiImberisso.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNotiImberisso.gridx = 0;
 		gbc_lblNotiImberisso.gridy = 0;
 		panel_4.add(lblNotiImberisso, gbc_lblNotiImberisso);
 		
 		JLabel lbltvbwtrn = new JLabel(Lang.about_tab.tengwar_name);
 		Font tengwarFont = FontManager.getFont("tngan.ttf", "Tengwar Annatar", Font.BOLD, 14);
 		lbltvbwtrn.setFont(tengwarFont);
 		GridBagConstraints gbc_lbltvbwtrn = new GridBagConstraints();
 		gbc_lbltvbwtrn.gridwidth = 19;
 		gbc_lbltvbwtrn.insets = new Insets(0, 0, 5, 0);
 		gbc_lbltvbwtrn.gridx = 0;
 		gbc_lbltvbwtrn.gridy = 1;
 		panel_4.add(lbltvbwtrn, gbc_lbltvbwtrn);
 		
 		aboutEditor = new JEditorPane();
 		aboutEditor.setContentType("text/html");
 		aboutEditor.setEditable(false);
 		aboutEditor.setText(Lang.about_tab.info);
 		aboutEditor.addHyperlinkListener(this);
 		if(Lang.uses_tengwar) {
 			aboutEditor.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		GridBagConstraints gbc_dtrpnF = new GridBagConstraints();
 		gbc_dtrpnF.gridwidth = 19;
 		gbc_dtrpnF.insets = new Insets(0, 0, 0, 5);
 		gbc_dtrpnF.fill = GridBagConstraints.BOTH;
 		gbc_dtrpnF.gridx = 0;
 		gbc_dtrpnF.gridy = 2;
 		panel_4.add(aboutEditor, gbc_dtrpnF);
 		
 		JPanel panel_3 = new JPanel();
 		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
 		gbc_panel_3.fill = GridBagConstraints.BOTH;
 		gbc_panel_3.gridx = 0;
 		gbc_panel_3.gridy = 1;
 		frmNotiImberisso.getContentPane().add(panel_3, gbc_panel_3);
 		GridBagLayout gbl_panel_3 = new GridBagLayout();
 		gbl_panel_3.columnWidths = new int[]{0, 460, 0};
 		gbl_panel_3.rowHeights = new int[]{0, 24, 0, 0};
 		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_3.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		panel_3.setLayout(gbl_panel_3);
 		
 		JLabel lblTodaysDate = new JLabel(Lang.common.current_date_label);
 		GridBagConstraints gbc_lblTodaysDate = new GridBagConstraints();
 		gbc_lblTodaysDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTodaysDate.gridx = 0;
 		gbc_lblTodaysDate.gridy = 0;
 		if(Lang.uses_tengwar) {
 			lblTodaysDate.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		panel_3.add(lblTodaysDate, gbc_lblTodaysDate);
 		
 		locationInfo = new JLabel("");
 		locationInfo.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			locationInfo.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		GridBagConstraints gbc_locationInfo = new GridBagConstraints();
 		gbc_locationInfo.anchor = GridBagConstraints.EAST;
 		gbc_locationInfo.insets = new Insets(0, 0, 5, 0);
 		gbc_locationInfo.gridx = 1;
 		gbc_locationInfo.gridy = 0;
 		panel_3.add(locationInfo, gbc_locationInfo);
 		
 		JLabel lblGregorian = new JLabel(Lang.common.gregorian_label);
 		lblGregorian.setFont(new Font("Dialog", Font.PLAIN, 12));
 		GridBagConstraints gbc_lblGregorian = new GridBagConstraints();
 		gbc_lblGregorian.anchor = GridBagConstraints.EAST;
 		gbc_lblGregorian.insets = new Insets(0, 0, 5, 5);
 		gbc_lblGregorian.gridx = 0;
 		gbc_lblGregorian.gridy = 1;
 		panel_3.add(lblGregorian, gbc_lblGregorian);
 		
 		todayGregorian = new JTextPane();
 		todayGregorian.setEditable(false);
 		todayGregorian.setFont(new Font("Dialog", Font.BOLD, 12));
 		if(Lang.uses_tengwar) {
 			todayGregorian.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		todayGregorian.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		GridBagConstraints gbc_todayGregorian = new GridBagConstraints();
 		gbc_todayGregorian.insets = new Insets(0, 0, 5, 0);
 		gbc_todayGregorian.fill = GridBagConstraints.BOTH;
 		gbc_todayGregorian.gridx = 1;
 		gbc_todayGregorian.gridy = 1;
 		panel_3.add(todayGregorian, gbc_todayGregorian);
 		
 		JLabel lblImladris = new JLabel(Lang.common.imladris_label);
 		lblImladris.setFont(new Font("Dialog", Font.PLAIN, 12));
 		if(Lang.uses_tengwar) {
 			lblImladris.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		GridBagConstraints gbc_lblImladris = new GridBagConstraints();
 		gbc_lblImladris.anchor = GridBagConstraints.EAST;
 		gbc_lblImladris.insets = new Insets(0, 0, 0, 5);
 		gbc_lblImladris.gridx = 0;
 		gbc_lblImladris.gridy = 2;
 		panel_3.add(lblImladris, gbc_lblImladris);
 		
 		todayImladris = new JTextPane();
 		todayImladris.setEditable(false);
 		todayImladris.setFont(new Font("Dialog", Font.BOLD, 12));
 		if(Lang.uses_tengwar) {
 			todayImladris.setFont(FontManager.getTrueTypeFont("tngan.ttf"));
 		}
 		todayImladris.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 		GridBagConstraints gbc_todayImladris = new GridBagConstraints();
 		gbc_todayImladris.fill = GridBagConstraints.BOTH;
 		gbc_todayImladris.gridx = 1;
 		gbc_todayImladris.gridy = 2;
 		panel_3.add(todayImladris, gbc_todayImladris);
 	}
 	
 	
 	@Override
 	public void hyperlinkUpdate(HyperlinkEvent event) {
 		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
         	if (Desktop.isDesktopSupported()) {
                 Desktop desktop = Desktop.getDesktop();
                 try {
                     desktop.browse(event.getURL().toURI());
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
             }
 	    }
 	}
 	
 }
