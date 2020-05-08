 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.control.Event;
 import br.org.indt.ndg.lwuit.control.ExclusiveChoiceFieldController;
 import br.org.indt.ndg.lwuit.extended.DescriptiveField;
 import br.org.indt.ndg.lwuit.extended.ExclusiveChoiceList;
 import br.org.indt.ndg.lwuit.extended.FilterProxyListModel;
 import br.org.indt.ndg.mobile.Resources;
 import com.sun.lwuit.Command;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.TextArea;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.events.DataChangedListener;
 import com.sun.lwuit.events.FocusListener;
 import com.sun.lwuit.layouts.BoxLayout;
 import com.sun.lwuit.list.ListModel;
 
 public class ExclusiveChoiceFieldView extends Screen implements ActionListener {
 
     private DescriptiveField mExclusiveChoiceTextField;
     private TextArea mQuestionTextArea;
     private Command cmdOk;
     private ExclusiveChoiceList mChoiceList;
     private ListModel mDataModel;
 
     public ExclusiveChoiceFieldView() {
         super();
         cmdOk = new Command( Resources.OK );
         form.addCommand(cmdOk);
         form.addCommandListener(this);
         form.addGameKeyListener(Display.GAME_RIGHT, this);
         form.addGameKeyListener(Display.GAME_DOWN, this);
 
         form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
         form.setScrollable(false);
     }
 
     protected void loadData() {
         ExclusiveChoiceFieldController controller = ExclusiveChoiceFieldController.getInstance();
         mExclusiveChoiceTextField = new DescriptiveField(controller.getMaxQuestionLength());
         mQuestionTextArea = UIUtils.createQuestionName(controller.getQuestion());
         mDataModel = controller.getModel();
         mChoiceList = controller.getListForModel();
     }
 
     protected void customize() {
         setTitle(Resources.NEWUI_NOKIA_DATA_GATHERING, "");
        mQuestionTextArea.setFocusable(false);
         // pass model trough proxy
         final FilterProxyListModel proxyModel = new FilterProxyListModel(mDataModel);
         proxyModel.setMaxDisplay(-1); // Unlimited
         mChoiceList.setModel(proxyModel);
 
         mExclusiveChoiceTextField.addActionListener(this);
         mExclusiveChoiceTextField.addDataChangeListener( new DataChangedListener() {
             public void dataChanged(int arg0, int arg1) {
                 proxyModel.filter(mExclusiveChoiceTextField.getText());
             }
         });
 
         // using Label as a workaround to avoid strange focus behaviour
         Label spacer = new Label("");
         spacer.setFocusable(true);
         spacer.addFocusListener( new FocusListener() {
             public void focusGained(Component cmp) {
                mChoiceList.requestFocus();
             }
             public void focusLost(Component cmp) {}
         });
 
         mChoiceList.setNextFocusDown(spacer);
         mChoiceList.setNextFocusLeft(mExclusiveChoiceTextField);
         mChoiceList.setNextFocusRight(mChoiceList);
         mChoiceList.addActionListener(this);
        mExclusiveChoiceTextField.setNextFocusUp(mExclusiveChoiceTextField);
 
         form.removeAll();
         form.addComponent(mQuestionTextArea);
         form.addComponent(mExclusiveChoiceTextField);
         form.addComponent(mChoiceList);
         form.addComponent(spacer);
     }
 
     public void actionPerformed(ActionEvent evt) {
         Command cmd = evt.getCommand();
         if ( cmd == cmdOk ) {
             backToInterview();
         } else if ( evt.getSource() == form) {
             if (mChoiceList.hasFocus() && evt.getKeyEvent() == Display.GAME_RIGHT ) {
                 handleMoreDetails();
                 mChoiceList.setHandlesInput(true);
             } else if ( mChoiceList.hasFocus() && evt.getKeyEvent() == Display.GAME_FIRE ) {
                 mChoiceList.setHandlesInput(true);
            } else if ( mExclusiveChoiceTextField.hasFocus() &&
                        evt.getKeyEvent() == Display.GAME_DOWN &&
                        mChoiceList.size() > 0 )
            { // pass focus to list only when non-empty
                mExclusiveChoiceTextField.setHandlesInput(false);
                mChoiceList.requestFocus();
             }
         }
     }
 
     private void backToInterview() {
         ExclusiveChoiceFieldController.getInstance().finalizeSelection();
     }
 
     private void handleMoreDetails() {
         ExclusiveChoiceFieldController.getInstance().handleMoreDetails();
     }
 
     class OnShowEvent extends Event {
 
         protected void doAction(Object parameter) {
             mExclusiveChoiceTextField.requestFocus();
         }
 
     }
 
 }
