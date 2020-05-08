 package com.jshooting.forms;
 
 import com.jshooting.logics.DateModifier;
 import com.jshooting.logics.PlacesGetter;
 import com.jshooting.logics.ShootingLogicsFactory;
 import com.jshooting.logics.SportsmansByTeamGetter;
 import com.jshooting.logics.TeamsGetter;
 import com.jshooting.logics.exceptions.ShootingLogicsException;
 import com.jshooting.model.Place;
 import com.jshooting.model.ShootingTrainingType;
 import com.jshooting.model.ShootingTrainingsFilter;
 import com.jshooting.model.Sportsman;
 import com.jshooting.model.Team;
 import java.awt.Window;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.TitledBorder;
 
 /**
  * Shooting trainings filter setup dialog. Using to find which trainings need to
  * use
  *
  * @author pgalex
  */
 public class ShootingTrainingsFilterDialog extends javax.swing.JDialog
 {
 	/**
 	 * Model for teams combo box
 	 */
 	private DefaultComboBoxModel teamsComboBoxModel;
 	/**
 	 * Model for sportsmans list
 	 */
 	private DefaultListModel sportsmansListModel;
 	/**
 	 * Model of places combo box
 	 */
 	private DefaultComboBoxModel placesComboBoxModel;
 	/**
 	 * Table of sportsmans
 	 */
 	private SportsmansByTeamGetter sportsmansGetter;
 	/**
 	 * Teams getter. Using to fill teams list
 	 */
 	private TeamsGetter teamsGetter;
 	/**
 	 * Places getter. Using to fill places list
 	 */
 	private PlacesGetter placesGetter;
 	/**
 	 * Is OK button was pressed
 	 */
 	private boolean okButtonPressed;
 
 	/**
 	 * Create new dialog
 	 *
 	 * @param parentWindow parent window
 	 * @param modalityType modality type of dialog
 	 * @param logicsFactory shooting logics factory
 	 * @throws IllegalArgumentException logicsFactory is null
 	 */
 	public ShootingTrainingsFilterDialog(Window parentWindow, ModalityType modalityType,
 					ShootingLogicsFactory logicsFactory) throws IllegalArgumentException
 	{
 		super(parentWindow, modalityType);
 
 		if (logicsFactory == null)
 		{
 			throw new IllegalArgumentException("logicsFactory is null");
 		}
 
 		okButtonPressed = false;
 		teamsGetter = logicsFactory.createTeamsGetter();
 		placesGetter = logicsFactory.createPlacesGetter();
 		sportsmansGetter = logicsFactory.createSportsmansByTeamGetter();
 		teamsComboBoxModel = new DefaultComboBoxModel();
 		sportsmansListModel = new DefaultListModel();
 		placesComboBoxModel = new DefaultComboBoxModel();
 
 		initComponents();
 
 		jListSportsmans.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		setSportsmanPanelTitleBySelectionMode();
 
 		jDateChooserDateFrom.setDate(DateModifier.createTodayBegin());
 		jDateChooserDateTo.setDate(DateModifier.createTodayEnd());
 
 		fillTeamsComboBox();
 		fillPlacesComboBox();
 		fillSportmansList();
 	}
 
 	/**
 	 * Fill places combo box model
 	 */
 	private void fillPlacesComboBox()
 	{
 		try
 		{
 			placesComboBoxModel.removeAllElements();
 			List<Place> allPlaces = placesGetter.getAllPlaces();
 			for (Place place : allPlaces)
 			{
 				placesComboBoxModel.addElement(place);
 			}
 		}
 		catch (ShootingLogicsException ex)
 		{
 			placesComboBoxModel.removeAllElements();
 		}
 	}
 
 	/**
 	 * Fill teams combo box model by teams table. Model will be empty if can not
 	 * get teams
 	 */
 	private void fillTeamsComboBox()
 	{
 		try
 		{
 			teamsComboBoxModel.removeAllElements();
 			List<Team> allTeams = teamsGetter.getAllTeams();
 			for (Team team : allTeams)
 			{
 				teamsComboBoxModel.addElement(team);
 			}
 		}
 		catch (ShootingLogicsException ex)
 		{
 			teamsComboBoxModel.removeAllElements();
 		}
 	}
 
 	/**
 	 * Fill sportmans list model by sportmans in getter's filtering team
 	 */
 	private void fillSportmansList()
 	{
 		try
 		{
 			sportsmansListModel.removeAllElements();
 			List<Sportsman> sportsmansInSelectedTeam = sportsmansGetter.getSportsmansInFilteringTeam();
 			for (Sportsman sportsman : sportsmansInSelectedTeam)
 			{
 				sportsmansListModel.addElement(sportsman);
 			}
 		}
 		catch (ShootingLogicsException ex)
 		{
 			sportsmansListModel.removeAllElements();
 		}
 	}
 
 	/**
 	 * Set title of sportsmans panel by sportsmans list selection mode
 	 */
 	private void setSportsmanPanelTitleBySelectionMode()
 	{
 		TitledBorder sportsmanPanelBorder = (TitledBorder) jPanelSportsmans.getBorder();
 		switch (jListSportsmans.getSelectionMode())
 		{
 			case ListSelectionModel.SINGLE_SELECTION:
 				sportsmanPanelBorder.setTitle("Спортсмены (только один)");
 				break;
 			default:
 				sportsmanPanelBorder.setTitle("Спортсмены");
 				break;
 		}
 	}
 
 	/**
 	 * Set that can select only one sportsman in dialog
 	 */
 	public void setOneSportsmanSelectingMode()
 	{
 		int[] selectedItemsIndexes = jListSportsmans.getSelectedIndices();
 		if (selectedItemsIndexes.length > 0)
 		{
 			jListSportsmans.setSelectedIndex(selectedItemsIndexes[0]);
 		}
 
 		jListSportsmans.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		setSportsmanPanelTitleBySelectionMode();
 	}
 
 	/**
 	 * Is OK button was pressed
 	 *
 	 * @return is OK button was pressed
 	 */
 	public boolean isOKButtonPressed()
 	{
 		return okButtonPressed;
 	}
 
 	/**
 	 * Get filter with parameters choosed in dialog
 	 *
 	 * @return filter created with dialog. null if cant create filter with
 	 * parameters from dialog
 	 */
 	public ShootingTrainingsFilter getFilter()
 	{
 		Object[] selectedSportsmans = jListSportsmans.getSelectedValues();
 		List<Sportsman> filteringSportsmans = new ArrayList<Sportsman>();
 		for (int i = 0; i < selectedSportsmans.length; i++)
 		{
 			filteringSportsmans.add((Sportsman) selectedSportsmans[i]);
 		}
 
 		List<ShootingTrainingType> filteringTrainingTypes = new ArrayList<ShootingTrainingType>();
 		if (jCheckBoxComplex.isSelected())
 		{
 			filteringTrainingTypes.add(ShootingTrainingType.COMPLEX);
 		}
 		if (jCheckBoxShooting.isSelected())
 		{
 			filteringTrainingTypes.add(ShootingTrainingType.SHOOTING);
 		}
 		if (jCheckBoxCompetition.isSelected())
 		{
 			filteringTrainingTypes.add(ShootingTrainingType.COMPETITION);
 		}
 
 		if (jRadioButtonDatesPeriod.isSelected())
 		{
 			return new ShootingTrainingsFilter(filteringSportsmans, jDateChooserDateFrom.getDate(), jDateChooserDateTo.getDate(), filteringTrainingTypes);
 		}
 		else
 		{
 			return new ShootingTrainingsFilter(filteringSportsmans, (Place) jComboBoxPlace.getSelectedItem(), filteringTrainingTypes);
 		}
 	}
 
 	/**
 	 * Set period choosers dates by selected place date from and date to
 	 */
 	private void setPeriodDatesBySelectedPlace()
 	{
 		if (jComboBoxPlace.getSelectedItem() != null)
 		{
 			Place selectedPlace = (Place) jComboBoxPlace.getSelectedItem();
 			jDateChooserDateFrom.setDate(selectedPlace.getBeginDate());
 			jDateChooserDateTo.setDate(selectedPlace.getEndDate());
 		}
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 	@SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {
 
     buttonGroupPeriodType = new javax.swing.ButtonGroup();
     jPanelSportsmans = new javax.swing.JPanel();
     jLabelTeam = new javax.swing.JLabel();
     jComboBoxTeams = new javax.swing.JComboBox();
     jScrollPane1 = new javax.swing.JScrollPane();
     jListSportsmans = new javax.swing.JList();
     jPanelPeriod = new javax.swing.JPanel();
     jLabel1 = new javax.swing.JLabel();
     jDateChooserDateFrom = new com.toedter.calendar.JDateChooser();
     jLabel2 = new javax.swing.JLabel();
     jDateChooserDateTo = new com.toedter.calendar.JDateChooser();
     jRadioButtonDatesPeriod = new javax.swing.JRadioButton();
     jRadioButtonPlacePeriod = new javax.swing.JRadioButton();
     jComboBoxPlace = new javax.swing.JComboBox();
     jPanelTrainingsType = new javax.swing.JPanel();
     jCheckBoxComplex = new javax.swing.JCheckBox();
     jCheckBoxShooting = new javax.swing.JCheckBox();
     jCheckBoxCompetition = new javax.swing.JCheckBox();
     jButtonOK = new javax.swing.JButton();
     jButtonCancel = new javax.swing.JButton();
 
     setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
     setTitle("Фильтр");
     setResizable(false);
     addWindowListener(new java.awt.event.WindowAdapter()
     {
       public void windowClosed(java.awt.event.WindowEvent evt)
       {
         formWindowClosed(evt);
       }
     });
 
     jPanelSportsmans.setBorder(javax.swing.BorderFactory.createTitledBorder("Спортсмены"));
 
     jLabelTeam.setText("Команда");
 
     jComboBoxTeams.setModel(teamsComboBoxModel);
     jComboBoxTeams.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jComboBoxTeamsActionPerformed(evt);
       }
     });
 
     jListSportsmans.setModel(sportsmansListModel);
     jScrollPane1.setViewportView(jListSportsmans);
 
     org.jdesktop.layout.GroupLayout jPanelSportsmansLayout = new org.jdesktop.layout.GroupLayout(jPanelSportsmans);
     jPanelSportsmans.setLayout(jPanelSportsmansLayout);
     jPanelSportsmansLayout.setHorizontalGroup(
       jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelSportsmansLayout.createSequentialGroup()
         .addContainerGap()
         .add(jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
           .add(jPanelSportsmansLayout.createSequentialGroup()
             .add(jLabelTeam)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jComboBoxTeams, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         .addContainerGap())
     );
     jPanelSportsmansLayout.setVerticalGroup(
       jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelSportsmansLayout.createSequentialGroup()
         .add(jPanelSportsmansLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
           .add(jComboBoxTeams, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
           .add(jLabelTeam))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
         .addContainerGap())
     );
 
     jPanelPeriod.setBorder(javax.swing.BorderFactory.createTitledBorder("Период"));
 
     jLabel1.setText("c");
 
     jLabel2.setText("по");
 
     buttonGroupPeriodType.add(jRadioButtonDatesPeriod);
     jRadioButtonDatesPeriod.setSelected(true);
     jRadioButtonDatesPeriod.setText("Даты (включительно)");
     jRadioButtonDatesPeriod.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jRadioButtonDatesPeriodActionPerformed(evt);
       }
     });
 
     buttonGroupPeriodType.add(jRadioButtonPlacePeriod);
     jRadioButtonPlacePeriod.setText("УТС");
     jRadioButtonPlacePeriod.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jRadioButtonPlacePeriodActionPerformed(evt);
       }
     });
 
     jComboBoxPlace.setModel(placesComboBoxModel);
     jComboBoxPlace.setEnabled(false);
     jComboBoxPlace.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jComboBoxPlaceActionPerformed(evt);
       }
     });
 
     org.jdesktop.layout.GroupLayout jPanelPeriodLayout = new org.jdesktop.layout.GroupLayout(jPanelPeriod);
     jPanelPeriod.setLayout(jPanelPeriodLayout);
     jPanelPeriodLayout.setHorizontalGroup(
       jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelPeriodLayout.createSequentialGroup()
         .addContainerGap()
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jPanelPeriodLayout.createSequentialGroup()
             .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jRadioButtonPlacePeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(jRadioButtonDatesPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
             .addContainerGap())
           .add(jPanelPeriodLayout.createSequentialGroup()
             .add(29, 29, 29)
             .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jComboBoxPlace, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(jPanelPeriodLayout.createSequentialGroup()
                 .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                   .add(jLabel2)
                   .add(jLabel1))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                   .add(jDateChooserDateFrom, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                   .add(jDateChooserDateTo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))))
     );
     jPanelPeriodLayout.setVerticalGroup(
       jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelPeriodLayout.createSequentialGroup()
         .add(jRadioButtonDatesPeriod)
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jDateChooserDateFrom, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
           .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jPanelPeriodLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jDateChooserDateTo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
           .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jRadioButtonPlacePeriod)
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jComboBoxPlace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         .addContainerGap(56, Short.MAX_VALUE))
     );
 
     jPanelTrainingsType.setBorder(javax.swing.BorderFactory.createTitledBorder("Тип тренировки"));
 
     jCheckBoxComplex.setSelected(true);
     jCheckBoxComplex.setText("Комплексная");
 
     jCheckBoxShooting.setSelected(true);
     jCheckBoxShooting.setText("Стрелковая");
 
     jCheckBoxCompetition.setSelected(true);
     jCheckBoxCompetition.setText("Соревнование");
 
     org.jdesktop.layout.GroupLayout jPanelTrainingsTypeLayout = new org.jdesktop.layout.GroupLayout(jPanelTrainingsType);
     jPanelTrainingsType.setLayout(jPanelTrainingsTypeLayout);
     jPanelTrainingsTypeLayout.setHorizontalGroup(
       jPanelTrainingsTypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelTrainingsTypeLayout.createSequentialGroup()
         .add(jPanelTrainingsTypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jCheckBoxComplex, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jCheckBoxShooting, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jCheckBoxCompetition, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addContainerGap())
     );
     jPanelTrainingsTypeLayout.setVerticalGroup(
       jPanelTrainingsTypeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(jPanelTrainingsTypeLayout.createSequentialGroup()
         .add(jCheckBoxComplex)
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jCheckBoxShooting)
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(jCheckBoxCompetition)
         .addContainerGap(128, Short.MAX_VALUE))
     );
 
     jButtonOK.setText("OK");
     jButtonOK.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jButtonOKActionPerformed(evt);
       }
     });
 
     jButtonCancel.setText("Отмена");
     jButtonCancel.addActionListener(new java.awt.event.ActionListener()
     {
       public void actionPerformed(java.awt.event.ActionEvent evt)
       {
         jButtonCancelActionPerformed(evt);
       }
     });
 
     org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
     getContentPane().setLayout(layout);
     layout.setHorizontalGroup(
       layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(layout.createSequentialGroup()
         .addContainerGap()
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(layout.createSequentialGroup()
             .add(jPanelSportsmans, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jPanelPeriod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jPanelTrainingsType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
           .add(layout.createSequentialGroup()
             .add(jButtonOK)
             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
             .add(jButtonCancel)
             .add(0, 0, Short.MAX_VALUE)))
         .addContainerGap())
     );
     layout.setVerticalGroup(
       layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
       .add(layout.createSequentialGroup()
         .addContainerGap()
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
           .add(jPanelTrainingsType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelPeriod, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
           .add(jPanelSportsmans, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
           .add(jButtonOK)
           .add(jButtonCancel))
         .addContainerGap())
     );
 
     pack();
   }// </editor-fold>//GEN-END:initComponents
 
   private void jComboBoxTeamsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBoxTeamsActionPerformed
   {//GEN-HEADEREND:event_jComboBoxTeamsActionPerformed
 		Object selectedItem = jComboBoxTeams.getSelectedItem();
 		if (selectedItem != null)
 		{
 			sportsmansGetter.setFilteringTeam((Team) selectedItem);
 		}
 		else
 		{
 			sportsmansGetter.setFilteringTeam(null);
 		}
 
 		fillSportmansList();
   }//GEN-LAST:event_jComboBoxTeamsActionPerformed
 
   private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOKActionPerformed
   {//GEN-HEADEREND:event_jButtonOKActionPerformed
 		if (jListSportsmans.getSelectedValues().length == 0)
 		{
 			JOptionPane.showMessageDialog(null, "Не выбран ни один спортсмен", "Ошибка", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		if (jRadioButtonPlacePeriod.isSelected() && jComboBoxPlace.getSelectedItem() == null)
 		{
 			JOptionPane.showMessageDialog(null, "УТС не выбран", "Ошибка", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 
 		if (jDateChooserDateFrom.getDate().after(jDateChooserDateTo.getDate()))
 		{
 			Date swapTemporary = jDateChooserDateFrom.getDate();
 			jDateChooserDateFrom.setDate(jDateChooserDateTo.getDate());
 			jDateChooserDateTo.setDate(swapTemporary);
 		}
 
 		okButtonPressed = true;
 		setVisible(false);
   }//GEN-LAST:event_jButtonOKActionPerformed
 
   private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCancelActionPerformed
   {//GEN-HEADEREND:event_jButtonCancelActionPerformed
 		okButtonPressed = false;
 		setVisible(false);
   }//GEN-LAST:event_jButtonCancelActionPerformed
 
   private void formWindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosed
   {//GEN-HEADEREND:event_formWindowClosed
 		okButtonPressed = false;
   }//GEN-LAST:event_formWindowClosed
 
   private void jRadioButtonPlacePeriodActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonPlacePeriodActionPerformed
   {//GEN-HEADEREND:event_jRadioButtonPlacePeriodActionPerformed
 		jComboBoxPlace.setEnabled(true);
 		jDateChooserDateFrom.setEnabled(false);
 		jDateChooserDateTo.setEnabled(false);
 		setPeriodDatesBySelectedPlace();
   }//GEN-LAST:event_jRadioButtonPlacePeriodActionPerformed
 
   private void jRadioButtonDatesPeriodActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonDatesPeriodActionPerformed
   {//GEN-HEADEREND:event_jRadioButtonDatesPeriodActionPerformed
 		jComboBoxPlace.setEnabled(false);
 		jDateChooserDateFrom.setEnabled(true);
 		jDateChooserDateTo.setEnabled(true);
   }//GEN-LAST:event_jRadioButtonDatesPeriodActionPerformed
 
   private void jComboBoxPlaceActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jComboBoxPlaceActionPerformed
   {//GEN-HEADEREND:event_jComboBoxPlaceActionPerformed
		if (jRadioButtonPlacePeriod.isSelected())
		{
			setPeriodDatesBySelectedPlace();
		}
   }//GEN-LAST:event_jComboBoxPlaceActionPerformed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.ButtonGroup buttonGroupPeriodType;
   private javax.swing.JButton jButtonCancel;
   private javax.swing.JButton jButtonOK;
   private javax.swing.JCheckBox jCheckBoxCompetition;
   private javax.swing.JCheckBox jCheckBoxComplex;
   private javax.swing.JCheckBox jCheckBoxShooting;
   private javax.swing.JComboBox jComboBoxPlace;
   private javax.swing.JComboBox jComboBoxTeams;
   private com.toedter.calendar.JDateChooser jDateChooserDateFrom;
   private com.toedter.calendar.JDateChooser jDateChooserDateTo;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabelTeam;
   private javax.swing.JList jListSportsmans;
   private javax.swing.JPanel jPanelPeriod;
   private javax.swing.JPanel jPanelSportsmans;
   private javax.swing.JPanel jPanelTrainingsType;
   private javax.swing.JRadioButton jRadioButtonDatesPeriod;
   private javax.swing.JRadioButton jRadioButtonPlacePeriod;
   private javax.swing.JScrollPane jScrollPane1;
   // End of variables declaration//GEN-END:variables
 }
