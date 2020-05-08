 package amu.licence.edt.view.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.TitledBorder;
 
 import amu.licence.edt.model.beans.Teacher;
 import amu.licence.edt.presenter.Presenter;
 import amu.licence.edt.view.ViewComponent;
 import amu.licence.edt.view.renderers.ListCellStrRenderer;
 
 public class TeacherManagmentForm extends ViewComponent {
 
     Frame owner;
 
     private JComboBox<Teacher> cbbTeachers;
     private JPanel pnlPnlsManage;
 
     private JPanel pnlUnavailability;
     private JPanel pnlNbAdminHours;
 
     private JPanel pnlUnavForm;
     private JSpinner spinnUnavStartDate;
     private JTextField txtUnavDuration;
     private JButton btnAddUnavailability;
 
     private JPanel pnlNbAdminHoursForm;
     private JSpinner spinnNbAdminHours;
     private JButton btnChangeNbAdminHours;
 
     public TeacherManagmentForm(Presenter presenter, Frame owner) {
         super(presenter);
         this.owner = owner;
     }
 
     @Override
     protected Component createComponent() {
         JDialog dialog = new JDialog(owner, true);
         dialog.setLayout(new BorderLayout());
         dialog.setTitle("Administration enseignants");
         dialog.addWindowListener(new WindowAdapter() {
             @Override
             public void windowOpened(WindowEvent e) {
                  component.setLocation(owner.getLocation());
             }
         });
 
         pnlPnlsManage = new JPanel(new BorderLayout());
 
         List<Teacher> teachers = presenter.teacherManagmentFormCreating();
         cbbTeachers = new JComboBox<Teacher>(teachers.toArray(new Teacher[0]));
         cbbTeachers.setRenderer(new ListCellStrRenderer(presenter.getClassBasedDDR()));
         cbbTeachers.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 cbbTeachersSelectionChanged(e);
             }
         });
 
         /* Panel Unavailability */
 
         pnlUnavailability = new JPanel(new GridLayout(0, 1));
         pnlUnavailability.setBorder(new TitledBorder(null, "Indisponibilitees",
                                                      TitledBorder.LEADING, TitledBorder.TOP,
                                                      null, Color.RED));
 
         pnlUnavForm = new JPanel(new GridLayout(0, 2));
 
         spinnUnavStartDate = new JSpinner();
         spinnUnavStartDate.setModel(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_YEAR));
         spinnUnavStartDate.setEditor(new JSpinner.DateEditor(spinnUnavStartDate, "d MMMM yyyy - H:m"));
 
         txtUnavDuration = new JTextField();
 
         btnAddUnavailability = new JButton("Ajouter");
         btnAddUnavailability.addActionListener(null);
 
         pnlUnavForm.add(new JLabel("Date de début"));
         pnlUnavForm.add(spinnUnavStartDate);
         pnlUnavForm.add(new JLabel("Durée"));
         pnlUnavForm.add(txtUnavDuration);
 
         pnlUnavailability.add(pnlUnavForm);
         pnlUnavailability.add(btnAddUnavailability);
 
         /* Panel Nb Admin Hours */
 
         pnlNbAdminHours = new JPanel(new GridLayout(0, 1));
         pnlNbAdminHours.setBorder(new TitledBorder(null, "Heures d'administration",
                                                    TitledBorder.LEADING, TitledBorder.TOP,
                                                    null, Color.RED));
 
         pnlNbAdminHoursForm = new JPanel(new GridLayout(0, 2));
 
         spinnNbAdminHours = new JSpinner();
        spinnNbAdminHours.setModel(new SpinnerNumberModel(((Teacher)cbbTeachers.getSelectedItem()).getAdminHours(), 0, 9000, 1));
 
         btnChangeNbAdminHours = new JButton("Modifier");
         btnChangeNbAdminHours.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 btnChangeNbAdminHoursActionListener();
             }
         });
 
         pnlNbAdminHoursForm.add(new JLabel("Heures effectués"));
         pnlNbAdminHoursForm.add(spinnNbAdminHours);
 
         pnlNbAdminHours.add(pnlNbAdminHoursForm);
         pnlNbAdminHours.add(btnChangeNbAdminHours);
 
         /* Constructing panel of manager panels */
 
         pnlPnlsManage.add(pnlUnavailability, BorderLayout.WEST);
         pnlPnlsManage.add(pnlNbAdminHours, BorderLayout.EAST);
 
         dialog.add(cbbTeachers, BorderLayout.NORTH);
         dialog.add(pnlPnlsManage, BorderLayout.SOUTH);
 
         dialog.pack();
         return dialog;
     }
 
     protected void btnChangeNbAdminHoursActionListener() {
         presenter.changeNbAdminHoursButtonPressed((Teacher)cbbTeachers.getSelectedItem(),
                                                   (Integer)spinnNbAdminHours.getModel().getValue());
     }
 
     protected void cbbTeachersSelectionChanged(ActionEvent e) {
         spinnNbAdminHours.getModel().setValue(((Teacher)cbbTeachers.getSelectedItem()).getAdminHours());
     }
 
 }
