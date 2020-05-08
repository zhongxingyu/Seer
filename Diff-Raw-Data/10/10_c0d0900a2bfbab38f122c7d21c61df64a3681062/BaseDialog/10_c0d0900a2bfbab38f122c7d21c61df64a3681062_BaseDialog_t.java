 package jDistsim.ui.dialog;
 
 import jDistsim.application.designer.common.UIConfiguration;
 
 import javax.swing.*;
 import javax.swing.border.MatteBorder;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 2.3.13
  * Time: 13:24
  */
 public abstract class BaseDialog extends JDialog {
 
     public enum Result {OK, CANCEL, HELP}
 
     protected GridBagConstraints constraints;
     private IDialogComponentFactory componentFactory;
     private Result dialogResult;
 
     private JPanel contentPane;
     private GridBagLayout gridBag;
     private JButton okButton;
     private JButton cancelButton;
     private String title;
 
    public BaseDialog(JDialog parent, String title) {
        this(parent, new DialogComponentFactory(), title);
    }

    public BaseDialog(JDialog parent, IDialogComponentFactory componentFactory, String title) {
        super(parent);
        this.componentFactory = componentFactory;
        this.title = title;
        this.dialogResult = Result.CANCEL;
    }
 
     public BaseDialog(JFrame parent, String title) {
         this(parent, new DialogComponentFactory(), title);
     }
 
     public BaseDialog(JFrame parent, IDialogComponentFactory componentFactory, String title) {
         super(parent);
         this.componentFactory = componentFactory;
         this.title = title;
         this.dialogResult = Result.CANCEL;
     }
 
     protected void build(JComponent component) {
         gridBag.setConstraints(component, constraints);
         contentPane.add(component);
     }
 
     protected IDialogComponentFactory getComponentFactory() {
         return componentFactory;
     }
 
     private void buildUI() {
         if (getTitle().trim().equals(""))
             setTitle("jDistSim dialog");
 
         setLayout(new BorderLayout());
         setResizable(false);
         setIconImage(null);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setSize(new Dimension(300, 240));
         setModal(true);
         setLocationRelativeTo(getParent());
 
         gridBag = new GridBagLayout();
         contentPane = new JPanel(gridBag);
         contentPane.setBackground(Color.WHITE);
         constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
         gridPanel.setBackground(Color.white);
         gridPanel.add(contentPane);
         add(gridPanel, BorderLayout.CENTER);
 
         buildWindowHeader();
         buildWindowBottom();
         buildWindowBody();
     }
 
     protected abstract void initializeUI();
 
     protected abstract void buildWindowBody();
 
     private void buildWindowBottom() {
         JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
         bottom.setPreferredSize(new Dimension(getWidth(), 30));
         bottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(184, 184, 184)));
 
         okButton = componentFactory.makeButton("Ok");
         cancelButton = componentFactory.makeButton("Cancel");
 
         okButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 onOkButtonClick(mouseEvent);
             }
         });
 
         cancelButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 onCancelButtonMouseClick(mouseEvent);
             }
         });
 
         bottom.add(okButton);
         bottom.add(cancelButton);
         add(bottom, BorderLayout.SOUTH);
     }
 
     private void onCancelButtonMouseClick(MouseEvent mouseEvent) {
         dialogResult = Result.CANCEL;
         hideDialog();
     }
 
     private void onOkButtonClick(MouseEvent mouseEvent) {
         dialogResult = Result.OK;
         if (!okButtonLogic()) return;
         hideDialog();
     }
 
     public Result getDialogResult() {
         return dialogResult;
     }
 
     protected abstract boolean okButtonLogic();
 
     private void buildWindowHeader() {
         JLabel label = new JLabel(title);
         label.setFont(UIConfiguration.getInstance().getDefaultFont(13));
 
         JPanel titlePanel = new JPanel() {
             @Override
             protected void paintComponent(Graphics graphics) {
                 super.paintComponent(graphics);
                 Graphics2D graphics2D = (Graphics2D) graphics;
                 graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
                 GradientPaint gradientPaint = new GradientPaint(0, 0, new Color(246, 246, 246), 0, getHeight(), new Color(220, 220, 220));
                 graphics2D.setPaint(gradientPaint);
                 graphics2D.fillRect(0, 0, getWidth(), getHeight());
             }
         };
         titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 8));
         titlePanel.setPreferredSize(new Dimension(getWidth(), 30));
         titlePanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(184, 184, 184)));
         titlePanel.add(label);
         add(titlePanel, BorderLayout.NORTH);
     }
 
     public void showDialog() {
         buildUI();
         initializeUI();
         setVisible(true);
     }
 
     public void hideDialog() {
         setVisible(false);
         dispose();
     }
 }
