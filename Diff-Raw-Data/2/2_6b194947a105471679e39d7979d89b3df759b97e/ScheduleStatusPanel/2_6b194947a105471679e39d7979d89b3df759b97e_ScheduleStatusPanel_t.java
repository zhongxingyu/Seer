 package amu.licence.edt.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import amu.licence.edt.presenter.Presenter;
 
 public class ScheduleStatusPanel extends ViewComponent {
 
     private final String DFLT_LBL_TXT = "vide";
 
     private JButton btnLeft;
     private JButton btnRight;
 
     private JPanel pnlStatus;
     private JLabel label;
     private JLabel lblDate;
     private SimpleDateFormat dateFormat;
 
     public ScheduleStatusPanel(Presenter presenter) {
         super(presenter);
     }
 
     @Override
     protected JComponent createComponent() {
         JPanel panel = new JPanel(new FlowLayout());
 
         btnLeft = new JButton("left");
         btnLeft.addActionListener(null);
 
         btnRight = new JButton("right");
         btnRight.addActionListener(null);
 
         label = new JLabel(DFLT_LBL_TXT);
 
        dateFormat = new SimpleDateFormat("EEE d MMMM yyyy");
         Date d = presenter.scheduleStatusCreating();
         lblDate = new JLabel((d != null) ? dateFormat.format(d) : DFLT_LBL_TXT);
 
         pnlStatus = new JPanel(new BorderLayout());
         pnlStatus.add(label, BorderLayout.CENTER);
         pnlStatus.add(lblDate, BorderLayout.SOUTH);
 
         panel.setLayout(new FlowLayout());
         panel.add(btnLeft);
         panel.add(pnlStatus);
         panel.add(btnRight);
 
         return panel;
     }
 
     public void setLabelText(String text) {
         label.setText(text != null ? text : DFLT_LBL_TXT);
     }
 
 }
