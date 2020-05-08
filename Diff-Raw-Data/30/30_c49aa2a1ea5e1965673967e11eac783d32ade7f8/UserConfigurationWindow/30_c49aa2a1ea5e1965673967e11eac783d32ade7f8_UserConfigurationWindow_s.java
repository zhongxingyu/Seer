 package com.hcalendar.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 import javax.swing.border.Border;
 
 import com.hcalendar.HCalendarConstants;
 import com.hcalendar.data.DataServices;
 import com.hcalendar.data.IDateEntity;
 import com.hcalendar.data.IHCCallback;
 import com.hcalendar.data.calculator.Calculator;
 import com.hcalendar.data.crud.CRUDManager;
 import com.hcalendar.data.orm.IORMClient;
 import com.hcalendar.data.orm.exception.ORMException;
 import com.hcalendar.data.orm.impl.ORMHelper;
 import com.hcalendar.data.utils.DateHelper;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration;
 import com.hcalendar.data.xml.workedhours.AnualHours;
 import com.hcalendar.ui.helper.ModalWindowUtils;
 import com.hcalendar.ui.widgets.ICalendarActionProvider;
 import com.hcalendar.ui.widgets.ICalendarActionProvider.LIST_TYPE;
 import com.hcalendar.ui.widgets.impl.JUserCalendarPanel;
 
 public class UserConfigurationWindow extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final String WINDOW_TITLE ="Creacin de perfil";
 	
 	private static final String ERROR_APPLY_CHANGE ="Error al aadir el cambio solicitado";
 	
 	private static final String ASK_SAVEORLOSE_DATA ="Si cambias el ao, se borraran los das libres que has definido hasta ahora. Quieres seguir?";
 	private static final String ASK_OVEWRITE_PROFILE_DATA ="El perfil que intenta guardar la existe, quiere sobreescribirlo?";
 	private static final String WARNING_CANNOT_CHANGE_YEAR ="Si quieres introducir das libres de otro ao, primero debes cambiar el ao del perfl";
 	
 	private static final String WINDOW_ITEM_NAME_TITLE ="Nombre : ";
 	private static final String WINDOW_ITEM_YEAR_TITLE ="Ao : ";
 	private static final String WINDOW_ITEM_YEAR_TOTAL_HOURS_TITLE ="Total horas convenio: ";
 	
 	private static final String WINDOW_PANEL_BORDER_FREEDAYS_TITLE ="Lista de dias libres";
 	private static final String WINDOW_PANEL_BORDER_SELECT_WORKINGDAYS_TITLE ="Seleccione dias laborales";
 	private static final String WINDOW_PANEL_DAY_HOURS_TITLE ="Horas por da";
 	
 	JTextArea diasLibresTextField;
 	JTextField nameTextField;
 	JTextField anualHours;
 	JComboBox yearCombo;
 
 	JCheckBox lunes;
 	JCheckBox martes;
 	JCheckBox miercoles;
 	JCheckBox jueves;
 	JCheckBox viernes;
 	JCheckBox sabado;
 	JCheckBox domingo;
 
 	JTextField horasLunes;
 	JTextField horasMartes;
 	JTextField horasMiercoles;
 	JTextField horasJueves;
 	JTextField horasViernes;
 	JTextField horasSabado;
 	JTextField horasDomingo;
 
 	JUserCalendarPanel jCalendarPanel;
 	private IORMClient orm;
 	private IHCCallback callback;
 	
 	private final String DATE_SEPARATOR = "\n";
 
 	public UserConfigurationWindow(IORMClient orm, IHCCallback callback) {
 		this.callback = callback;
 		this.orm = orm;
 		this.setSize(1000, 600);
 		this.setMaximumSize(new Dimension(1500, 900));
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		this.setTitle(WINDOW_TITLE);
 		Container content = this.getContentPane();
 		JPanel panel = new JPanel(new GridLayout(2, 2));
 		createTextFields(panel);
 		createDaysChecks(panel);
 		createCalendar(panel);
 		content.add(panel);
 		createButtons(content);
 		this.setVisible(true);
 		pack();
 	}
 
 	private void createCalendar(Container panel) {
 		// Calendar
 		jCalendarPanel = new JUserCalendarPanel(new ICalendarEventListener() {
 
 			@Override
 			public void onDateChanged(Date date, Boolean selected, ICalendarActionProvider actionProvider) {
 				calendarOnDateChangedActions(date, selected, actionProvider);
 			}
 
 			@Override
 			public void onDataInput(IDateEntity entity) {
 				try {
 					orm.addChange(null, entity);
 				} catch (ORMException e) {
 					ModalWindowUtils.showErrorPanel(UserConfigurationWindow.this,ERROR_APPLY_CHANGE
 							);
 				}
 			}
 		}, false, true);
 		panel.add(jCalendarPanel);
 
 		JPanel diasLibresPanel = new JPanel(new GridLayout(1, 2));
 		Border hoursBorder = BorderFactory.createTitledBorder(WINDOW_PANEL_BORDER_FREEDAYS_TITLE);
 		diasLibresPanel.setBorder(hoursBorder);
 		diasLibresTextField = new JTextArea();
 		diasLibresTextField.setEditable(false);
 		JScrollPane scroll = new JScrollPane(diasLibresTextField);
 		diasLibresPanel.add(scroll, BorderLayout.CENTER);
 		panel.add(diasLibresPanel);
 	}
 
 	private void createDaysChecks(Container panel) {
 		JPanel genPanel = new JPanel(new GridLayout(1, 2));
 		// Dias laborales
 		JPanel daysPanel = new JPanel(new GridLayout(7, 1));
 		Border daysBorder = BorderFactory.createTitledBorder(WINDOW_PANEL_BORDER_SELECT_WORKINGDAYS_TITLE);
 		daysPanel.setBorder(daysBorder);
 		lunes = new JCheckBox(DateHelper.daysOfWeek[0]);
 		lunes.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (lunes.isSelected())
 					horasLunes.setEditable(true);
 				else {
 					horasLunes.setText(null);
 					horasLunes.setEditable(false);
 				}
 			}
 		});
 		martes = new JCheckBox(DateHelper.daysOfWeek[1]);
 		martes.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (martes.isSelected())
 					horasMartes.setEditable(true);
 				else {
 					horasMartes.setText(null);
 					horasMartes.setEditable(false);
 				}
 			}
 		});
 		miercoles = new JCheckBox(DateHelper.daysOfWeek[2]);
 		miercoles.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (miercoles.isSelected())
 					horasMiercoles.setEditable(true);
 				else {
 					horasMiercoles.setText(null);
 					horasMiercoles.setEditable(false);
 				}
 			}
 		});
 		jueves = new JCheckBox(DateHelper.daysOfWeek[3]);
 		jueves.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (jueves.isSelected())
 					horasJueves.setEditable(true);
 				else {
 					horasJueves.setText(null);
 					horasJueves.setEditable(false);
 				}
 			}
 		});
 		viernes = new JCheckBox(DateHelper.daysOfWeek[4]);
 		viernes.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (viernes.isSelected())
 					horasViernes.setEditable(true);
 				else {
 					horasViernes.setText(null);
 					horasViernes.setEditable(false);
 				}
 			}
 		});
 		sabado = new JCheckBox(DateHelper.daysOfWeek[5]);
 		sabado.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (sabado.isSelected())
 					horasSabado.setEditable(true);
 				else {
 					horasSabado.setText(null);
 					horasSabado.setEditable(false);
 				}
 			}
 		});
 		domingo = new JCheckBox(DateHelper.daysOfWeek[6]);
 		domingo.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (domingo.isSelected())
 					horasDomingo.setEditable(true);
 				else {
 					horasDomingo.setText(null);
 				}
 			}
 		});
 		daysPanel.add(lunes);
 		daysPanel.add(martes);
 		daysPanel.add(miercoles);
 		daysPanel.add(jueves);
 		daysPanel.add(viernes);
 		daysPanel.add(sabado);
 		daysPanel.add(domingo);
 
 		JPanel hoursPanel = new JPanel(new GridLayout(7, 1));
 		Border hoursBorder = BorderFactory.createTitledBorder(WINDOW_PANEL_DAY_HOURS_TITLE);
 		hoursPanel.setBorder(hoursBorder);
 		horasLunes = new JTextField();
 		horasLunes.setEditable(false);
 		horasMartes = new JTextField();
 		horasMartes.setEditable(false);
 		horasMiercoles = new JTextField();
 		horasMiercoles.setEditable(false);
 		horasJueves = new JTextField();
 		horasJueves.setEditable(false);
 		horasViernes = new JTextField();
 		horasViernes.setEditable(false);
 		horasSabado = new JTextField();
 		horasSabado.setEditable(false);
 		horasDomingo = new JTextField();
 		horasDomingo.setEditable(false);
 		hoursPanel.add(horasLunes);
 		hoursPanel.add(horasMartes);
 		hoursPanel.add(horasMiercoles);
 		hoursPanel.add(horasJueves);
 		hoursPanel.add(horasViernes);
 		hoursPanel.add(horasSabado);
 		hoursPanel.add(horasDomingo);
 
 		genPanel.add(daysPanel);
 		genPanel.add(hoursPanel);
 		panel.add(genPanel);
 	}
 
 	private void createTextFields(Container panel) {
 		JPanel namePanel = new JPanel(new GridLayout(3, 2));
 		JLabel nameLabel = new JLabel(WINDOW_ITEM_NAME_TITLE);
 		nameTextField = new JTextField();
 		nameLabel.setLabelFor(nameTextField);
 		namePanel.add(nameLabel, BorderLayout.WEST);
 		namePanel.add(nameTextField, BorderLayout.CENTER);
 
 		JLabel convHours = new JLabel(WINDOW_ITEM_YEAR_TOTAL_HOURS_TITLE);
 		anualHours = new JTextField();
 		convHours.setLabelFor(anualHours);
 		namePanel.add(convHours, BorderLayout.WEST);
 		namePanel.add(anualHours, BorderLayout.CENTER);
 
 		Integer[] years = new Integer[50];
 		int k = 0;
 		for (int i = 2011; i < 2061; i++) {
 			years[k] = i;
 			k++;
 		}
 
 		JLabel year = new JLabel(WINDOW_ITEM_YEAR_TITLE);
 		yearCombo = new JComboBox(years);
 		yearCombo.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (diasLibresTextField.getText() != null && diasLibresTextField.getText().length() > 0)
 					if (ModalWindowUtils
 							.showOptionPanel(
 									UserConfigurationWindow.this,ASK_SAVEORLOSE_DATA
 									,
 									new String[] { HCalendarConstants.ACTION_BUTTON_CONTINUE_TITLE, HCalendarConstants.ACTION_BUTTON_CANCEL_TITLE }) == 0) {
 						diasLibresTextField.setText(null);

 					}
 			}
 		});
 		year.setLabelFor(yearCombo);
 		namePanel.add(year, BorderLayout.WEST);
 		namePanel.add(yearCombo, BorderLayout.CENTER);
 
 		panel.add(namePanel);
 	}
 
 	private void createButtons(Container panel) {
 		JPanel buttonPane = new JPanel();
 		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
 		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		buttonPane.add(Box.createHorizontalGlue());
 		// BOTON GUARDAR
 		JButton saveButton = new JButton(HCalendarConstants.ACTION_BUTTON_SAVE_TITLE);
 		saveButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveUserConfiguration();
 				UserConfigurationWindow.this.callback.itemChanged();
 			}
 		});
 		buttonPane.add(saveButton);
 		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
 		// BOTON CANCELAR
 		JButton cancelButton = new JButton(HCalendarConstants.ACTION_BUTTON_CANCEL_TITLE);
 		cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				UserConfigurationWindow.this.dispose();
 			}
 		});
 
 		buttonPane.add(cancelButton);
 		panel.add(buttonPane, BorderLayout.PAGE_END);
 	}
 
 	protected void saveUserConfiguration() {
 		try {
 			// name
 			String name = nameTextField.getText();
 			// calendarHours
 			String aHours = anualHours.getText();
 			// year
 			Integer year = (Integer) yearCombo.getSelectedItem();
 			// workingsdays
 			Map<Integer, String> listaDiasLaborales = new HashMap<Integer, String>();
 			if (horasLunes.getText() != null && horasLunes.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.MONDAY, horasLunes.getText());
 			if (horasMartes.getText() != null && horasMartes.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.TUESDAY, horasMartes.getText());
 			if (horasMiercoles.getText() != null && horasMiercoles.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.WEDNESDAY, horasMiercoles.getText());
 			if (horasJueves.getText() != null && horasJueves.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.THURSDAY, horasJueves.getText());
 			if (horasViernes.getText() != null && horasViernes.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.FRIDAY, horasViernes.getText());
 			if (horasSabado.getText() != null && horasSabado.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.SATURDAY, horasSabado.getText());
 			if (horasDomingo.getText() != null && horasDomingo.getText().length() > 0)
 				listaDiasLaborales.put(Calendar.SUNDAY, horasDomingo.getText());
 			// freedays
 			List<String> dLibresList = new ArrayList<String>();
 			String dLibres = diasLibresTextField.getText();
 			String[] splitted = dLibres.split(DATE_SEPARATOR);
 			for (String dateStr : splitted) {
 				if (dateStr != null && dateStr.length() > 0)
 					dLibresList.add(dateStr);
 			}
 			// Verify if profile exists
 			boolean ovewriteProfile = false;
 			if (ORMHelper.getCurrentProfiles(orm.getAnualConfiguration()).contains(name)) {
 				int result = ModalWindowUtils.showOptionPanel(this,ASK_OVEWRITE_PROFILE_DATA
 						, new String[] {
 						HCalendarConstants.ACTION_BUTTON_CONTINUE_TITLE, HCalendarConstants.ACTION_BUTTON_CANCEL_TITLE });
 				if (result == 1)
 					return;
 				ovewriteProfile = true;
 			}
 			UserConfiguration userConfig = DataServices.createAnualConfigurationProfile(orm, name, year,
 					aHours, listaDiasLaborales, dLibresList, ovewriteProfile);
 			AnualHours anualHours = Calculator.calculatePlannedHoursOfYear(orm, name, year,
 					listaDiasLaborales, dLibresList, ovewriteProfile);
 			CRUDManager.saveAnualHours(anualHours);
 			CRUDManager.saveAnualConfiguration(userConfig);
 			ModalWindowUtils.showSuccesPanel(this, HCalendarConstants.SUCCES_SAVE_DATA);
 		} catch (Exception e) {
 			ModalWindowUtils.showErrorPanel(this, HCalendarConstants.ERROR_SAVE_DATA);
 		}
 	}
 
 	// Acciones ha realizar cuando cambia la fecha seleccionada
 	@SuppressWarnings("deprecation")
 	private void calendarOnDateChangedActions(Date date, Boolean selected,
 			ICalendarActionProvider actionProvider) {
 		// Verify if year isEqual
 		if (date.getYear() + 1900 != ((Integer) yearCombo.getSelectedItem()).intValue()) {
 			ModalWindowUtils.showErrorPanel(UserConfigurationWindow.this,WARNING_CANNOT_CHANGE_YEAR);
 			return;
 		}
 		// Add color to the date onCalendar
 		if (!selected)
 			jCalendarPanel.addDayToList(date, LIST_TYPE.USER_NOT_WORKINGDAY);
 		else
 			jCalendarPanel.removeDayFromList(date, LIST_TYPE.USER_NOT_WORKINGDAY);
 
 		String formattedDate = DateHelper.DATE_FORMAT.format(date);
 		// Lista vacia
 		if (diasLibresTextField.getText() == null || diasLibresTextField.getText().length() == 0)
 			diasLibresTextField.setText(formattedDate);
 		// El dia existe en la lista
 		else if (diasLibresTextField.getText().contains(formattedDate) && selected) {
 			StringBuffer strBuff = new StringBuffer();
 			String text = diasLibresTextField.getText();
 			String[] splitted = text.split(DATE_SEPARATOR);
 			for (String dateStr : splitted) {
 				if ((dateStr != null && dateStr.length() > 0) && (!dateStr.equals(formattedDate)))
 					strBuff.append(dateStr).append(DATE_SEPARATOR);
 			}
 			// int startIndex = text.indexOf(formattedDate);
 			// String finalStr =
 			// text.substring(0,startIndex).concat(text.substring(startIndex+formattedDate.length()));
 			diasLibresTextField.setText(strBuff.toString());
 			// El dia no existe en la lista
 		} else if (!diasLibresTextField.getText().contains(formattedDate))
 			diasLibresTextField.setText(diasLibresTextField.getText() + DATE_SEPARATOR + formattedDate);
 
 	}
 }
