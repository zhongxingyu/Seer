 package controller.form;
 
 //<editor-fold defaultstate="collapsed" desc=" import ">
 import cococare.common.CCFormat;
 import cococare.framework.swing.CFSwingCtrl;
 import cococare.swing.CCSwing;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.Timer;
 import model.mdl.EnigmaSquareMain;
 import model.obj.Enum.AutoAnswer;
 import model.obj.Question;
 //</editor-fold>
 
 public class DlgQuestionCtrl extends CFSwingCtrl {
 
     private Question question;
     private JLabel lblQuestion;
     private JFormattedTextField txtAnswer;
     private JLabel lblTimer;
     private Timer tmrTimer;
     private JButton btnEnter;
     private JLabel lblCorrectAnswer;
 
     @Override
     protected Class _getEntity() {
         return Question.class;
     }
 
     @Override
     protected BaseFunction _getBaseFunction() {
         return BaseFunction.FORM_FUNCTION;
     }
 
     @Override
     protected ShowMode _getShowMode() {
         return ShowMode.DIALOG_MODE;
     }
 
     @Override
     protected void _initComponent() {
         super._initComponent();
         //
         question = (Question) objEntity;
         lblQuestion.setText(CCFormat.wordWrap(question.getQuestion()));
         lblTimer.setText(CCFormat.getString(EnigmaSquareMain.gameOption.getQuestionTime()));
         tmrTimer = new Timer(1000, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 int timer = CCFormat.parseInt(lblTimer.getText());
                 if (timer > 0) {
                     lblTimer.setText(CCFormat.getString(--timer));
                 } else {
                     AutoAnswer autoAnswer = AutoAnswer.values()[question.getAutoAnswerIndex()];
                     updateCaller = AutoAnswer.TRUE.equals(autoAnswer);
                     _doCloseScreen();
                 }
             }
         });
         tmrTimer.start();
         CCSwing.setFormatterFactory(txtAnswer, CCFormat.pack("", "*", question.getAnswer().length()), '*', "");
     }
 
     @Override
     protected void _initListener() {
         super._initListener();
         //
         ActionListener alEnter = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 _doEnter();
             }
         };
         CCSwing.addActionListener(txtAnswer, alEnter);
         CCSwing.addActionListener(btnEnter, alEnter);
     }
 
     protected void _doEnter() {
         updateCaller = txtAnswer.getText().equalsIgnoreCase(question.getAnswer());
         if (EnigmaSquareMain.gameOption.getQuestionAnswerShow()) {
             lblCorrectAnswer.setText(question.getAnswer());
         }
         _doCloseScreen();
     }
 
     @Override
    protected void _doClose() {
        _doCloseScreen();
    }

    @Override
     protected void _doCloseScreen() {
         super._doCloseScreen();
         //
         tmrTimer.stop();
     }
 
     @Override
     protected void _doUpdateEditor() {
         super._doUpdateEditor();
         //
         txtAnswer.setText("");
     }
 }
