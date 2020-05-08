 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.control.AcceptQuestionListFormCommand;
 import br.org.indt.ndg.lwuit.control.BackInterviewFormCommand;
 import br.org.indt.ndg.lwuit.control.ExclusiveChoiceFieldController;
 import br.org.indt.ndg.lwuit.control.OpenFileBrowserCommand;
 import br.org.indt.ndg.lwuit.control.PersistenceManager;
 import br.org.indt.ndg.lwuit.control.RemovePhotoCommand;
 import br.org.indt.ndg.lwuit.control.ShowPhotoCommand;
 import br.org.indt.ndg.lwuit.control.SurveysControl;
 import br.org.indt.ndg.lwuit.control.TakePhotoCommand;
 import br.org.indt.ndg.lwuit.extended.CheckBox;
 import br.org.indt.ndg.lwuit.extended.DateField;
 import br.org.indt.ndg.lwuit.extended.DescriptiveField;
 import br.org.indt.ndg.lwuit.extended.FilterProxyListModel;
 import br.org.indt.ndg.lwuit.extended.NumericField;
 import br.org.indt.ndg.lwuit.extended.ExclusiveChoiceList;
 import br.org.indt.ndg.lwuit.extended.ExclusiveChoiceList.ExclusiveChoiceListListener;
 import br.org.indt.ndg.lwuit.extended.ListBGPainter;
 import br.org.indt.ndg.lwuit.extended.ListFocusBGPainter;
 import br.org.indt.ndg.lwuit.extended.PointerListener;
 import br.org.indt.ndg.lwuit.extended.RadioButton;
 import br.org.indt.ndg.lwuit.extended.TimeField;
 import br.org.indt.ndg.lwuit.model.NDGAnswer;
 import br.org.indt.ndg.lwuit.model.Category;
 import br.org.indt.ndg.lwuit.model.CategoryAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceQuestion;
 import br.org.indt.ndg.lwuit.model.DateAnswer;
 import br.org.indt.ndg.lwuit.model.DateQuestion;
 import br.org.indt.ndg.lwuit.model.DecimalQuestion;
 import br.org.indt.ndg.lwuit.model.DescriptiveQuestion;
 import br.org.indt.ndg.lwuit.model.ImageAnswer;
 import br.org.indt.ndg.lwuit.model.ImageData;
 import br.org.indt.ndg.lwuit.model.ImageQuestion;
 import br.org.indt.ndg.lwuit.model.NumericAnswer;
 import br.org.indt.ndg.lwuit.model.NumericQuestion;
 import br.org.indt.ndg.lwuit.model.NDGQuestion;
 import br.org.indt.ndg.lwuit.model.TimeAnswer;
 import br.org.indt.ndg.lwuit.model.TimeQuestion;
 import br.org.indt.ndg.lwuit.ui.camera.NDGCameraManager;
 import br.org.indt.ndg.lwuit.ui.camera.CameraManagerListener;
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import br.org.indt.ndg.mobile.Resources;
 import com.sun.lwuit.Button;
 import com.sun.lwuit.ButtonGroup;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Container;
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Image;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.List;
 import com.sun.lwuit.TextArea;
 import com.sun.lwuit.events.ActionEvent;
 import com.sun.lwuit.events.ActionListener;
 import com.sun.lwuit.events.DataChangedListener;
 import com.sun.lwuit.events.FocusListener;
 import com.sun.lwuit.layouts.BoxLayout;
 import com.sun.lwuit.layouts.FlowLayout;
 import com.sun.lwuit.list.DefaultListModel;
 import com.sun.lwuit.list.ListModel;
 import com.sun.lwuit.plaf.Border;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 public class InterviewForm extends Screen implements ActionListener {
     private String title1;
     private String title2;
 
     private NDGQuestion currentQuestion;
     private NDGAnswer currentAnswer;
     private Vector vContainers;
     private boolean answerChanged = false;
 
     protected void loadData() {
         answerChanged = false;
         vContainers = new Vector();
         title1 = SurveysControl.getInstance().getSurveyTitle();
         title2 = Resources.NEW_INTERVIEW;
     }
 
     protected void customize() {
         form.removeAllCommands();
         form.removeAll();
 
         form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
         form.getContentPane().getStyle().setBorder(Border.createEmpty(), false);
         form.setScrollAnimationSpeed(100);
         form.setFocusScrolling(true);
 
         Vector questions = SurveysControl.getInstance().getSelectedCategory().getQuestions();
 
         ContainerUI lastUI = null;
         for ( int j=0; j< questions.size(); j++ ){
             currentQuestion = (NDGQuestion)questions.elementAt(j);
 
             int categoryIndex = SurveysControl.getInstance().getSelectedCategoryIndex();
             String catIndex  = String.valueOf(categoryIndex+1);
             int subCategory = SurveysControl.getInstance().getSelectedSubCategoryIndex();
             CategoryAnswer categoryanswer = SurveysControl.getInstance().getResult().getCategoryAnswers(catIndex);
             Hashtable table = categoryanswer.getSubCategoryAnswers(subCategory);
             currentAnswer = (NDGAnswer)table.get( String.valueOf( currentQuestion.getIdNumber()));
 
             if (currentQuestion instanceof DescriptiveQuestion) {
                  DescriptiveFieldUI df = new DescriptiveFieldUI( currentQuestion, currentAnswer );
                  df.registerQuestion();
                  form.addComponent(df);
                  lastUI = df;
             }
             else if (currentQuestion instanceof NumericQuestion) {
                 NumericFieldUI  nf = new NumericFieldUI(currentQuestion, currentAnswer);
                 nf.registerQuestion();
                 form.addComponent(nf);
                 lastUI = nf;
             }
             else if (currentQuestion instanceof DateQuestion) {
                DateFieldUI df = new DateFieldUI(currentQuestion, currentAnswer);
                df.registerQuestion();
                form.addComponent(df);
                lastUI = df;
             }
             else if (currentQuestion instanceof ChoiceQuestion) {
                 if (((ChoiceQuestion) currentQuestion).isExclusive()) {
                     if (((ChoiceQuestion) currentQuestion).getChoices().size() <= 4) {
                         ExclusiveChoiceFieldUI ecf = new ExclusiveChoiceFieldUI(currentQuestion, currentAnswer);
                         ecf.registerQuestion();
                         form.addComponent(ecf);
                         lastUI = ecf;
                     } else { //if there are more than 4 choices (radiobutton)
                         ExclusiveChoiceFieldAutoCompleteUI ecfa = new ExclusiveChoiceFieldAutoCompleteUI(currentQuestion, currentAnswer);
                         ecfa.registerQuestion();
                         form.addComponent(ecfa);
                         lastUI = ecfa;
                     }
                     ChoiceAnswer choiceAnswer = (ChoiceAnswer)currentAnswer;
                     if( choiceAnswer.getSelectedIndexes().size() > 0 ){
                         int selected = Integer.parseInt( (String)choiceAnswer.getSelectedIndexes().elementAt(0) );
                         updateSkippedQuestion((ChoiceQuestion)currentQuestion, selected);
                     }
                 } else {
                     ChoiceFieldUI cf = new ChoiceFieldUI(currentQuestion, currentAnswer);
                     cf.registerQuestion();
                     form.addComponent(cf);
                     lastUI = cf;
                 }
             }
             else if(currentQuestion instanceof ImageQuestion){
                 ImageFieldUI imgf = new ImageFieldUI(currentQuestion, currentAnswer);
                 imgf.registerQuestion();
                 form.addComponent(imgf);
                 lastUI = imgf;
             } else if (currentQuestion instanceof TimeQuestion) {
                 if (((TimeQuestion) currentQuestion).getConvention() == 24) {
                     TimeFieldUI tf = new TimeFieldUI(currentQuestion, currentAnswer);
                     tf.registerQuestion();
                     form.addComponent(tf);
                     lastUI = tf;
                 } else if (((TimeQuestion) currentQuestion).getConvention() == 12) {
                     if(currentQuestion.getFirstTime()) {
                         ((TimeQuestion) currentQuestion).setAm_pm(-1); // clear
                     }
                     TimeField12UI tf = new TimeField12UI(currentQuestion, currentAnswer);
                     tf.registerQuestion();
                     form.addComponent(tf);
                     lastUI = tf;
                 }
             }
 
             if( currentQuestion.getSkiped())
             {
                 lastUI.setEnabled(false);
             }
             vContainers.addElement(lastUI);
         }
 
         form.addCommand(BackInterviewFormCommand.getInstance().getCommand());
         form.addCommand(AcceptQuestionListFormCommand.getInstance().getCommand());
         try{
             form.removeCommandListener(this);
         } catch (NullPointerException npe ) {
             //during first initialisation remove throws exception.
             //this ensure that we have registered listener once
         }
         form.addCommandListener(this);
 
         if (PersistenceManager.getInstance().isEditing()) {
             title2 = Resources.EDITING;
         }
         setTitle(title1, title2);
         if( vContainers!= null && vContainers.size() > 0 ) {
             ((ContainerUI)vContainers.elementAt(0)).requestFocus();
         }
     }
 
     private void setModifiedInterview(boolean _val) {
         answerChanged = _val;
     }
 
     public boolean isModifiedInterview() {
         return answerChanged;
     }
 
     private void commitAllAnswers() {
         for ( int i = 0; i < vContainers.size(); i++) {
             ((ContainerUI)vContainers.elementAt(i)).commitValue();
         }
     }
 
     public boolean validateAllAnswersAndResetModifiedFlag() {
         boolean result = validateAllAnswers();
         if (result){
             commitAllAnswers();
             setModifiedInterview(false);
         }
         return result;
     }
 
     private boolean validateAllAnswers() {
         for ( int i = 0; i< vContainers.size(); i++)
         {
             if(!((ContainerUI)vContainers.elementAt(i)).validate())
             {
                 ((ContainerUI)vContainers.elementAt(i)).requestFocus();
                 return false;
             }
         }
         return true;
     }
 
     private void updateSkippedQuestion( ChoiceQuestion aQuestion, int selectedChoiceItem ) {
         try
         {
             boolean sentence1 = ((selectedChoiceItem != ((ChoiceQuestion) aQuestion).getChoiceItem()) && (((ChoiceQuestion) aQuestion).isInverse()));
             boolean sentence2 = ((selectedChoiceItem == ((ChoiceQuestion) aQuestion).getChoiceItem()) && (!((ChoiceQuestion) aQuestion).isInverse()));
 
             Vector questions = SurveysControl.getInstance().getQuestionsFlat();
             int start = questions.indexOf( aQuestion );
             int endCat = aQuestion.getCatTo();
             int endQuestion = aQuestion.getSkipTo();
 
             if( endCat > 0 && endCat <= SurveysControl.getInstance().getSurvey().getCategories().size() ) {
                 Category category = (Category)SurveysControl.getInstance().getSurvey().getCategories().elementAt(endCat-1);
                 if( endQuestion > 0 && endQuestion <= category.getQuestions().size() ) {
                     int endQuestionLinear = questions.indexOf(category.getQuestions().elementAt(endQuestion-1));
                     for( int i = start+1; i<endQuestionLinear; i++) {
                         ((NDGQuestion)questions.elementAt(i)).setSkiped(sentence1 || sentence2);
                     }
                 }
 
                 for( int i = 0; i< vContainers.size(); i++ ) {
                     ContainerUI container = (ContainerUI)vContainers.elementAt(i);
                     boolean skip = container.getQuestion().getSkiped();
                     container.setEnabled(!skip);
                 }
             }
         }
         catch(Exception ex){/*do nothing*/}
     }
 
     public void actionPerformed(ActionEvent evt) {
         Object cmd = evt.getSource();
         if (cmd == BackInterviewFormCommand.getInstance().getCommand() ) {
             BackInterviewFormCommand.getInstance().execute(this);
         } else if ( cmd == AcceptQuestionListFormCommand.getInstance().getCommand() ){
             AcceptQuestionListFormCommand.getInstance().execute(this);
         } else if( cmd == OpenFileBrowserCommand.getInstance().getCommand() ) {
             OpenFileBrowserCommand.getInstance().execute(null);
         } else if ( cmd == TakePhotoCommand.getInstance().getCommand() ) {
             TakePhotoCommand.getInstance().execute(null);
         } else if ( cmd == ShowPhotoCommand.getInstance().getCommand() ) {
             ShowPhotoCommand.getInstance().execute(null);
         } else if ( cmd == RemovePhotoCommand.getInstance().getCommand() ) {
             RemovePhotoCommand.getInstance().execute(null);
         }
     }
 
 abstract class ContainerUI extends Container implements FocusListener {
 
     protected TextArea mQuestionTextArea;
     protected NDGQuestion mQuestion;
     protected NDGAnswer mAnswer;
 
     public abstract void commitValue();
     public abstract void setEnabled(boolean enabled);
     public abstract boolean validate();
 
 
     public void handleMoreDetails( Object cmd )
     {
     }
 
     public ContainerUI( NDGQuestion aQuestion, NDGAnswer aAnswer)
     {
         getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor ));
         mQuestion  = aQuestion;
         mAnswer = aAnswer;
         mQuestionTextArea = UIUtils.createQuestionName( mQuestion.getName() );
     }
 
     public NDGQuestion getQuestion() {
         return mQuestion;
     }
 
     public NDGAnswer getAnswer() {
         return mAnswer;
     }
 
     public void focusGained(Component cmpnt){
         getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusGainColor,
                                                         NDGStyleToolbox.getInstance().focusGainColor,
                                                         NDGStyleToolbox.getInstance().focusGainColor,
                                                         NDGStyleToolbox.getInstance().focusGainColor ), false);
         refreshTheme();
     }
 
     public void focusLost(Component cmpnt) {
        if(!cmpnt.getComponentForm().isVisible()){
             return;
         }
 
         if ( !validate()) {
             cmpnt.requestFocus();
             return;
         }
         getStyle().setBorder(Border.createBevelLowered( NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor,
                                                         NDGStyleToolbox.getInstance().focusLostColor ), false);
         refreshTheme();
     }
 }
 
 
     class DescriptiveFieldUI extends ContainerUI{
         private DescriptiveField mDescriptionTextField;
 
         public DescriptiveFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             mDescriptionTextField = new DescriptiveField(((DescriptiveQuestion) mQuestion).getLength());
             mDescriptionTextField.setText((String)mAnswer.getValue());
             mDescriptionTextField.setInputMode("Abc");
             mDescriptionTextField.setEditable(true);
             mDescriptionTextField.setFocusable(true);
             addComponent(mDescriptionTextField);
             mDescriptionTextField.addFocusListener(this);
             mDescriptionTextField.addDataChangeListener(new DataChangedListener() {
                 public void dataChanged(int type, int index) {
                     revalidate();
                 }
             });
 
             if (((DescriptiveQuestion)mQuestion).getChoices().size() >= 1) {
                 Vector vChoices = ((DescriptiveQuestion)mQuestion).getChoices();
                 int totalChoices = vChoices.size();
                 String[] choices = new String[totalChoices];
 
                 final ListModel underlyingModel = new DefaultListModel();
                 for (int i = 0; i < totalChoices; i++) {
                     choices[i] = (String) vChoices.elementAt(i);
                     underlyingModel.addItem(choices[i]);
                 }
 
                 final FilterProxyListModel proxyModel = new FilterProxyListModel(underlyingModel);
                 proxyModel.setMaxDisplay(4);
                 final List choice = new List(proxyModel);
 
                 choice.addActionListener(new ActionListener() {
 
                 public void actionPerformed(ActionEvent ae) {
                     mDescriptionTextField.setText(((List) ae.getSource()).getSelectedItem().toString());
                     }
                 });
 
                 mDescriptionTextField.addDataChangeListener(new DataChangedListener() {
 
                 public void dataChanged(int arg0, int arg1) {
                   //  skipGameKey = true;
                     proxyModel.filter(mDescriptionTextField.getText());
                     //setModifiedInterview(true);
                     }
                 });
 
                 addComponent(choice);
             } else {
                 mDescriptionTextField.addDataChangeListener(new HandleInterviewAnswersModified());
             }
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue() {
             mAnswer.setValue( mDescriptionTextField.getText() );
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled( boolean enabled ) {
             mQuestionTextArea.setEnabled(enabled);
             mDescriptionTextField.setEnabled(enabled);
         }
 
         public boolean validate() {
             return true;
         }
      }
     ///////////////////////////// Numeric Question /////////////////////////////
     class NumericFieldUI extends ContainerUI{
         private NumericField mNumberTextField;
 
         public NumericFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ){
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion( ) {
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             mNumberTextField = new NumericField( ((NumericQuestion) mQuestion).getLength(),
                                                  mQuestion instanceof DecimalQuestion );
             mNumberTextField.setFocusable(true);
             String value =((NumericAnswer)mAnswer).getValueString();
             mNumberTextField.setText(value);
             mNumberTextField.addFocusListener(this);
             mNumberTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mNumberTextField);
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue( ) {
             mAnswer.setValue( mNumberTextField.getText() );
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mNumberTextField.setEnabled(enabled);
         }
 
         public boolean validate() {
             return ((NumericQuestion)mQuestion).passConstraints(mNumberTextField.getText());
         }
     }
 
     ////////////////////////////// Date Question ///////////////////////////////
     class DateFieldUI extends ContainerUI{
         private DateField mDateTextField;
 
         public DateFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion() {
             setLayout( new BoxLayout(BoxLayout.Y_AXIS));
 
             addComponent(mQuestionTextArea);
 
             mDateTextField = new DateField(AppMIDlet.getInstance().getSettings().getStructure().getDateFormatId());
             long datelong = ((DateAnswer)mAnswer).getDate();
             mDateTextField.setDate(new Date(datelong));
             mDateTextField.setEditable(true);
             mDateTextField.addFocusListener(this);
             mDateTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mDateTextField);
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue() {
             Date date = mDateTextField.getDate();
             Long datelong = new Long(date.getTime());
             ((DateAnswer)mAnswer).setDate(datelong.longValue());
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mDateTextField.setEnabled(enabled);
         }
 
         public boolean validate() {
             return ((DateQuestion)mQuestion).passConstraints(mDateTextField.getDate().getTime());
         }
     }
 
     abstract class ExclusiveChoiceFieldBaseUI extends ContainerUI {
 
         protected RadioChoiceModel mDataModel;
         protected ExclusiveChoiceList mChoiceList;
         protected int mMaxQuestionLength = 20;
         protected ActionListener mSelectionListener = new ActionListener() {
 
             public void actionPerformed( ActionEvent evt ) {
 
                 if (evt.getKeyEvent() == Display.GAME_RIGHT) {
                     handleMoreDetails(evt);
                     mChoiceList.setHandlesInput(true);
                 } else if (evt.getKeyEvent() == Display.GAME_LEFT) {
                     mChoiceList.setHandlesInput(true);
                 } else {
                     handleSelection(mChoiceList);
                     mChoiceList.setHandlesInput(true);
                     setModifiedInterview(true);
                 }
             }
         };
         protected ExclusiveChoiceListListener mListListener = new ExclusiveChoiceListListener() {
 
             public void detailsRequested() {
                 handleMoreDetails(mChoiceList);
             }
 
         };
 
         public ExclusiveChoiceFieldBaseUI(NDGQuestion aQuestion, NDGAnswer aAnswer) {
             super(aQuestion, aAnswer);
         }
 
         public void registerQuestion() {
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             Vector vChoices = ((ChoiceQuestion) mQuestion).getChoices();
             Vector vOthers = ((ChoiceQuestion) mQuestion).getOthers();
             int totalChoices = vChoices.size();
             mDataModel = new RadioChoiceModel();
             mMaxQuestionLength = 0;
             for (int i = 0; i < totalChoices; i++) {
                 String choice = (String) vChoices.elementAt(i);
                 RadioChoiceItem item = new RadioChoiceItem(choice, mDataModel);
                 boolean hasMoreDetails = ((String) vOthers.elementAt(i)).equals("1");
                 if ( hasMoreDetails ) {
                     item.setMoreDetailsText("");
                 }
                 mDataModel.addItem(item);
                 mMaxQuestionLength = (choice.length() > mMaxQuestionLength) ? choice.length() : mMaxQuestionLength;
             }
 
             Vector vSelectedIndexes = ((ChoiceAnswer) mAnswer).getSelectedIndexes();
             for (int i = 0; i < vSelectedIndexes.size(); i++) {
                 int index = Integer.parseInt((String) vSelectedIndexes.elementAt(i));
                 mDataModel.setItemChecked(index);
                 RadioChoiceItem item = (RadioChoiceItem) mDataModel.getItemAt(index);
                 String moreDetails = (String) ((ChoiceAnswer) mAnswer).getOtherText((String) vSelectedIndexes.elementAt(i));
                 if (moreDetails != null) {
                     item.setMoreDetailsText(moreDetails);
                 }
             }
             ExclusiveChoiceFieldController controller = ExclusiveChoiceFieldController.getInstance();
             controller.setData( mDataModel, mQuestion.getName(), mMaxQuestionLength);
             mChoiceList = controller.getListForModel();
             mChoiceList.addActionListener(mSelectionListener);
             mChoiceList.addExclusiveChoiceListListener(mListListener);
             mChoiceList.getStyle().setBorder(Border.createEmpty());
         }
 
         public void commitValue() {
             Vector checkedIndexes = new Vector();
             Hashtable checkedItemsMoreDetails = new Hashtable();
             for (int i = 0; i < mDataModel.getSize(); i++) {
                 RadioChoiceItem item = (RadioChoiceItem) mDataModel.getItemAt(i);
                 if (item.isChecked()) {
                     checkedIndexes.addElement(Integer.toString(i));
                     if (item.hasMoreDetails()) {
                         checkedItemsMoreDetails.put(Integer.toString(i), item.getMoreDetails());
                     }
                     break; // this is radio choice so only 1 option is checked
                 }
             }
             ((ChoiceAnswer) mAnswer).setSelectedIndex(checkedIndexes);
             ((ChoiceAnswer) mAnswer).setOtherText(checkedItemsMoreDetails);
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void handleSelection(Object obj) {
             List list = (List) obj;
             Object selectedItem = list.getSelectedItem();
             int selectedItemIdx = list.getSelectedIndex();
             if (selectedItem != null) {
                 RadioChoiceItem item = (RadioChoiceItem) list.getSelectedItem();
                 boolean checkedNew = !item.isChecked();
                 item.setChecked(checkedNew);
                 if ( checkedNew && item.hasMoreDetails() && item.getMoreDetails().length() == 0 )
                     handleMoreDetails(obj);
                 updateSkippedQuestion((ChoiceQuestion) getQuestion(), selectedItemIdx);
             }
         }
 
         public void handleMoreDetails(Object obj) {
             super.handleMoreDetails(obj);
             RadioChoiceItem item = (RadioChoiceItem)mDataModel.getItemAt(mDataModel.getSelectedIndex());
             if ( item.hasMoreDetails() ) {
                 if ( item.isChecked() ) {
                     DetailsForm.show(item.getValue(), item.getMoreDetails());
                     item.setMoreDetailsText( SurveysControl.getInstance().getItemOtherText() );
                     setModifiedInterview(true);
                 }
             }
             commitValue(); // TODO remove?
         }
 
         public void focusGained(Component cmpnt) {
             super.focusGained(cmpnt);
             if (cmpnt instanceof List) {
                 form.addGameKeyListener(Display.GAME_RIGHT, mSelectionListener);
                 form.addGameKeyListener(Display.GAME_LEFT, mSelectionListener);
             }
         }
 
         public void focusLost(Component cmpnt) {
             super.focusLost(cmpnt);
             if (cmpnt instanceof List) {
                 form.removeGameKeyListener(Display.GAME_RIGHT, mSelectionListener);
                 form.removeGameKeyListener(Display.GAME_LEFT, mSelectionListener);
             }
         }
 
         public boolean validate(){
             return true;
         }
 
         public int getSelectedItemIdx(){
             return mChoiceList.getSelectedIndex();
         }
 
     }
 
     class ExclusiveChoiceFieldUI extends ExclusiveChoiceFieldBaseUI {
 
         public ExclusiveChoiceFieldUI(NDGQuestion aQuestion, NDGAnswer aAnswer) {
             super(aQuestion, aAnswer);
         }
 
         public void registerQuestion() {
             super.registerQuestion();
             mChoiceList.addFocusListener(this);
             mChoiceList.setNextFocusLeft(mChoiceList);
             mChoiceList.setNextFocusRight(mChoiceList);
             addComponent(mChoiceList);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mChoiceList.setEnabled(enabled);
         }
     }
 
     class ExclusiveChoiceFieldAutoCompleteUI extends ExclusiveChoiceFieldBaseUI {
 
         private static final String mEmpty = "----";
         private final Button mShowChoiceDialogButton = new Button("-> " + Resources.SHOW_CHOICES + " <-");
         private final TextArea mSelectedChoice = UIUtils.createTextArea( mEmpty, NDGStyleToolbox.fontSmall );
         private final ActionListener mChoiceDialogListener = new ActionListener() {
 
             public void actionPerformed(ActionEvent evt) {
                 Object source = evt.getSource();
                 ExclusiveChoiceFieldController controller = ExclusiveChoiceFieldController.getInstance();
                 controller.setData(mChoiceList, mDataModel, mQuestion.getName(), mMaxQuestionLength);
                 if (source == mShowChoiceDialogButton) {
                     controller.addActionListener(mChoiceDialogListener);
                     AppMIDlet.getInstance().setDisplayable(ExclusiveChoiceFieldView.class);
                 } else if (source == controller) {
                     switch ( evt.getKeyEvent() ) {
                         case ExclusiveChoiceFieldController.FINALIZE:
                             updateSelectedChoice();
                             controller.removeActionListener(mChoiceDialogListener);
                             form.showBack();
                             break;
                         case ExclusiveChoiceFieldController.MORE_DETAILS:
                             handleMoreDetails(source);
                             break;
                         default:
                             throw new IllegalArgumentException("Unknown action for ExclusiveChoiceField");
                     }
                 }
             }
         };
 
         public ExclusiveChoiceFieldAutoCompleteUI(NDGQuestion aQuestion, NDGAnswer aAnswer) {
             super(aQuestion, aAnswer);
         }
 
         public void registerQuestion() {
             super.registerQuestion();
             // TODO make default button style in NDGLookAndFeel
             mShowChoiceDialogButton.getSelectedStyle().setFont(NDGStyleToolbox.getInstance().listStyle.selectedFont);
             mShowChoiceDialogButton.getSelectedStyle().setFgColor( NDGStyleToolbox.getInstance().listStyle.selectedFontColor );
             mShowChoiceDialogButton.getSelectedStyle().setBgPainter(new ListFocusBGPainter(mShowChoiceDialogButton));
             mShowChoiceDialogButton.getSelectedStyle().setBorder(Border.createEmpty());
             mShowChoiceDialogButton.getSelectedStyle().setMargin(10, 10, 10, 10);
             mShowChoiceDialogButton.getUnselectedStyle().setFont(NDGStyleToolbox.getInstance().listStyle.unselectedFont);
             mShowChoiceDialogButton.getUnselectedStyle().setFgColor( NDGStyleToolbox.getInstance().listStyle.unselectedFontColor );
             mShowChoiceDialogButton.getUnselectedStyle().setBgPainter(new ListBGPainter(mShowChoiceDialogButton));
             mShowChoiceDialogButton.getUnselectedStyle().setBorder(Border.createEmpty());
             mShowChoiceDialogButton.getUnselectedStyle().setMargin(10, 10, 10, 10);
             mShowChoiceDialogButton.setAlignment(CENTER);
             mShowChoiceDialogButton.addActionListener(mChoiceDialogListener);
 
             addComponent(mSelectedChoice);
             addComponent(mShowChoiceDialogButton);
 
             updateSelectedChoice();
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mShowChoiceDialogButton.setEnabled(enabled);
             mChoiceList.setEnabled(enabled);
         }
 
         public void updateSelectedChoice() {
             RadioChoiceItem selectedItem = (RadioChoiceItem)mDataModel.getCheckedItem();
             mSelectedChoice.setText( selectedItem.toString().length() == 0 ? mEmpty : selectedItem.toString());
         }
     }
 
     ////////////////////////// Choice Multiple Question ////////////////////////
     class ChoiceFieldUI extends ContainerUI{
         private Vector mGroupButton;
         private CheckBox mCurrentlyFocused = null;
         private ActionListener mActionListener = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 if (evt.getSource() == form && evt.getKeyEvent() == Display.GAME_RIGHT) {
                     handleMoreDetails(mCurrentlyFocused);
                 } else if ( evt.getSource() instanceof CheckBox ) {
                     CheckBox checkbox = (CheckBox)evt.getSource();
                     if (checkbox.isSelected() && checkbox.hasOther() && checkbox.getOtherText().length() == 0){
                         handleMoreDetails(checkbox);
                     }
                     setModifiedInterview(true);
                 }
             }
         };
         private PointerListener mPointerListener = new PointerListener() {
             public void pointerPressed(int x, int y) {}
             public void pointerReleased(int x, int y) {
                 handleMoreDetails(mCurrentlyFocused);
             }
         };
 
         public ChoiceFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             mGroupButton = new Vector();
             Vector vChoices = ((ChoiceQuestion)mQuestion).getChoices();
             Vector vOthers = ((ChoiceQuestion)mQuestion).getOthers();
 
             int totalChoices = vChoices.size();
             String[] choices = new String[totalChoices];
             for (int i = 0; i < totalChoices; i++) {
                 choices[i] = (String) vChoices.elementAt(i);
                 boolean canHaveOther = ((String) vOthers.elementAt(i)).equals("1");
                 PointerListener pointerListener = null;
                 if ( canHaveOther )
                     pointerListener = mPointerListener;
                 CheckBox cb = new CheckBox(choices[i], pointerListener);
                 cb.setOther(canHaveOther);
                 cb.setOtherText("");
                 cb.addFocusListener(this); // Controls when changing to a new question
                 cb.addActionListener(mActionListener);
                 cb.setNextFocusRight(cb);
                 cb.setNextFocusLeft(cb);
                 mGroupButton.addElement(cb);
                 addComponent(cb);
             }
 
             Vector vSelectedIndexes = ((ChoiceAnswer)mAnswer).getSelectedIndexes();
             for ( int i=0; i< vSelectedIndexes.size(); i++ ) {
 
                 int index = Integer.parseInt( (String)vSelectedIndexes.elementAt( i ) );
                 if(index < mGroupButton.size()){
                     ((CheckBox) mGroupButton.elementAt(index)).setSelected(true);
                 }
 
                 if ( ((ChoiceAnswer)mAnswer).getOtherText( String.valueOf(i) )!= null ) {
                     ((CheckBox) mGroupButton.elementAt(i)).setOtherText((String)((ChoiceAnswer)mAnswer).getOtherText( String.valueOf( i ) ) );
                 }
             }
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
        }
 
         public void focusGained(Component cmpnt) {
             super.focusGained(cmpnt);
             if ( cmpnt instanceof CheckBox ) {
                 mCurrentlyFocused = (CheckBox)cmpnt;
                 form.addGameKeyListener(Display.GAME_RIGHT, mActionListener);
             }
         }
         public void focusLost(Component cmpnt) {
             super.focusLost(cmpnt);
             if ( cmpnt instanceof CheckBox ) {
                 form.removeGameKeyListener(Display.GAME_RIGHT, mActionListener);
                 mCurrentlyFocused = null;
             }
         }
 
         public void commitValue() {
             Vector selectedIndexes = new Vector();
             Hashtable othersText = new Hashtable();
 
             selectedIndexes.removeAllElements();
             for ( int i = 0; i < mGroupButton.size(); i++ ) {
                 CheckBox cb = (CheckBox) mGroupButton.elementAt(i);
                 if ( cb.isSelected() ) {
                     selectedIndexes.addElement( Integer.toString(i) );
                     if ( cb.hasOther() ) {
                         othersText.put( Integer.toString(i), cb.getOtherText() );
                     }
                 }
             }
             ((ChoiceAnswer)mAnswer).setSelectedIndex( selectedIndexes );
             ((ChoiceAnswer)mAnswer).setOtherText( othersText );
 
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             for ( int i = 0; i< mGroupButton.size(); i++) {
                 ((CheckBox)mGroupButton.elementAt(i)).setEnabled(enabled);
             }
         }
 
         public void handleMoreDetails( Object obj ) {
             if (obj == null)
                 return;
             CheckBox choiceCheckbox = (CheckBox)obj;
             if ( (choiceCheckbox.hasOther()) && (choiceCheckbox.isSelected()) ) {
                DetailsForm.show(choiceCheckbox.getText(), choiceCheckbox.getOtherText());
                choiceCheckbox.setOtherText(SurveysControl.getInstance().getItemOtherText());
                setModifiedInterview(true);
             }
         }
 
         public boolean validate() {
             return true;
         }
    }
 
     class ImageFieldUI extends ContainerUI implements ActionListener, CameraManagerListener {
 
         private Container mImageContainer;
         private Label maxPhotoCount;
 
         public ImageFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void update(){
            ImageAnswer imgAnswer = (ImageAnswer)mAnswer;
            if(mImageContainer.getComponentCount() <= imgAnswer.getImages().size()) {
                addCameraIconButton();
                setModifiedInterview(true);
            }
 
            form.showBack();
            //focus last button
            Component comp = mImageContainer.getComponentAt( mImageContainer.getComponentCount() - 1 );
            comp.requestFocus();
            rebuildOptionsMenu( comp );
         }
 
         public void registerQuestion(){
             AppMIDlet.getInstance().setCurrentCameraManager(NDGCameraManager.getInstance());
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             maxPhotoCount = new Label( Resources.MAX_IMG_NO + String.valueOf(((ImageQuestion)mQuestion).getMaxCount()) );
             maxPhotoCount.getStyle().setFont( NDGStyleToolbox.fontSmall );
             addComponent(maxPhotoCount);
             ImageAnswer imgAnswer = (ImageAnswer)mAnswer;
 
             mImageContainer = new Container( new FlowLayout() );
 
             ImageData imgData = null;
             if(imgAnswer.getImages().size() > 0){
                 for(int idx = 0; idx < imgAnswer.getImages().size(); idx++){
                     imgData = (ImageData)imgAnswer.getImages().elementAt(idx);
                     addButton(imgData.getThumbnail());
                 }
             }
             addCameraIconButton();
             addComponent(mImageContainer);
             addFocusListener(this);
         }
 
         public void addCameraIconButton(){
             ImageAnswer imgAnswer = (ImageAnswer)mAnswer;
             if(imgAnswer.getImages().size() < ((ImageQuestion)mQuestion).getMaxCount()){
                 Image img = Screen.getRes().getImage("camera-icon");
                 addButton(img);
             }
         }
 
         private void addButton(Image img){
             Button button = new Button();
             button.setIcon(img);
             button.addActionListener(this);
             button.setAlignment(Component.LEFT);
             button.setFocusable(true);
             button.addFocusListener(this);
             mImageContainer.addComponent(button);
         }
 
         public void focusGained(Component cmpnt) {
             super.focusGained(cmpnt);
             rebuildOptionsMenu(cmpnt);
 
             NDGCameraManager.getInstance().sendPostProcessData( this, cmpnt,
                                                                (ImageAnswer)mAnswer,
                                                                 mImageContainer);
         }
 
         private void rebuildOptionsMenu( Component cmpnt ) {
             form.removeCommand(OpenFileBrowserCommand.getInstance().getCommand());
             form.removeCommand(TakePhotoCommand.getInstance().getCommand());
             form.removeCommand(ShowPhotoCommand.getInstance().getCommand());
             form.removeCommand(RemovePhotoCommand.getInstance().getCommand());
             if (mImageContainer.getComponentIndex(cmpnt) < ((ImageAnswer)mAnswer).getImages().size()) {
                 form.addCommand(RemovePhotoCommand.getInstance().getCommand());
                 form.addCommand(ShowPhotoCommand.getInstance().getCommand());
             }
             form.addCommand(OpenFileBrowserCommand.getInstance().getCommand());
             form.addCommand(TakePhotoCommand.getInstance().getCommand());
         }
 
         public void focusLost(Component cmpnt) {
             form.removeCommand(OpenFileBrowserCommand.getInstance().getCommand());
             form.removeCommand(TakePhotoCommand.getInstance().getCommand());
             form.removeCommand(ShowPhotoCommand.getInstance().getCommand());
             form.removeCommand(RemovePhotoCommand.getInstance().getCommand());
             super.focusLost(cmpnt);
         }
 
         public void commitValue() {
             mQuestion.setVisited(true);
             mAnswer.setVisited(true);
         }
 
         public void actionPerformed(ActionEvent cmd) {
             if ( cmd.getSource() instanceof Button ) {
                 NDGCameraManager.getInstance().sendPostProcessData( this,
                                                                     (Button)cmd.getSource(),
                                                                     (ImageAnswer)mAnswer,
                                                                     mImageContainer);
 
                 int index = mImageContainer.getComponentIndex((Button)cmd.getSource());
 
                 if( index  < ((ImageAnswer)mAnswer).getImages().size() ){
                     new ImageQuestionContextMenu(0, ImageQuestionContextMenu.FOUR_ACTIONS_CONTEXT_MENU).show();
                 } else {
                     new ImageQuestionContextMenu(0, ImageQuestionContextMenu.TWO_ACTIONS_CONTEXT_MENU).show();
                 }
             }
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             maxPhotoCount.setEnabled(enabled);
             if ( mImageContainer != null ) {
                 for( int i = 0; i< mImageContainer.getComponentCount(); i++ ) {
                     mImageContainer.getComponentAt(i).setEnabled(enabled);
                 }
                 mImageContainer.setEnabled(enabled);
             }
         }
 
         public boolean validate() {
             return true;
         }
     }
 
     class TimeFieldUI extends ContainerUI {
         private TimeField mTimeTextField;
 
         public TimeFieldUI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
              super( aQuestion, aAnswer );
         }
 
         public void registerQuestion() {
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             mTimeTextField = new TimeField(TimeField.HHMM1);
 
             long datelong =  ((TimeAnswer)mAnswer).getTime();
 
             mTimeTextField.setTime(new Date(datelong));
             mTimeTextField.setEditable(true);
             mTimeTextField.addFocusListener(this);
             mTimeTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mTimeTextField);
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue() {
             Date time = mTimeTextField.getTime();
             Long timelong = new Long(time.getTime());
             ((TimeAnswer)mAnswer).setTime( timelong.longValue() );
             mAnswer.setVisited(true);
             mQuestion.setVisited(true);
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mTimeTextField.setEnabled(enabled);
         }
 
         public boolean validate() {
             return true;
         }
     }
 
     class TimeField12UI extends ContainerUI {
         private TimeField mTimeTextField;
         private ButtonGroup mAmPmGroupButton;
 
         public TimeField12UI( NDGQuestion aQuestion, NDGAnswer aAnswer ) {
             super( aQuestion, aAnswer );
         }
 
         public void registerQuestion(){
             setLayout(new BoxLayout(BoxLayout.Y_AXIS));
             addComponent(mQuestionTextArea);
 
             mTimeTextField = new TimeField(TimeField.HHMM);
             mTimeTextField.addFocusListener(this);
 
             mAmPmGroupButton = new ButtonGroup();
             final RadioButton am = new RadioButton("am");
             final RadioButton pm = new RadioButton("pm");
             am.addFocusListener(this);
             pm.addFocusListener(this);
 
             mAmPmGroupButton.add(am);
             mAmPmGroupButton.add(pm);
             am.addActionListener(new HandleMoreDetails());
             pm.addActionListener(new HandleMoreDetails());
 
             long datelong = ((TimeAnswer)mAnswer).getTime();
             Date date = new Date(datelong);
 
             if (((TimeQuestion)mQuestion).getAm_pm() == 1) {
                 mAmPmGroupButton.setSelected(am);
             } else if (((TimeQuestion)mQuestion).getAm_pm() == 2) {
                 mAmPmGroupButton.setSelected(pm);
             } else {
                 Calendar cal = Calendar.getInstance();
                 cal.setTime(date);
                 if( cal.get(Calendar.AM_PM) == Calendar.AM ) {
                     mAmPmGroupButton.setSelected(am);
                     ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.AM);
                 } else {
                     mAmPmGroupButton.setSelected(pm);
                     ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.PM);
                 }
             }
 
             mTimeTextField.setTime(date);
             mTimeTextField.setEditable(true);
             mTimeTextField.addFocusListener(this);
             mTimeTextField.addDataChangeListener(new HandleInterviewAnswersModified());
 
             addComponent(mTimeTextField);
             addComponent(am);
             addComponent(pm);
 
             Label spacer = new Label("");
             spacer.setFocusable(false);
             addComponent(spacer);
         }
 
         public void commitValue() {
             for (int i = 0; i < mAmPmGroupButton.getButtonCount(); i++)
             {
                 RadioButton rb = (RadioButton)mAmPmGroupButton.getRadioButton(i);
                 if (rb.isSelected())
                 {
                     ((TimeQuestion)mQuestion).setAm_pm(i == 0? TimeQuestion.AM : TimeQuestion.PM);
                     Date time = mTimeTextField.getTime();
                     Long timelong = new Long(time.getTime());
                     ((TimeAnswer)mAnswer).setTime( timelong.longValue() );
                     ((TimeAnswer)mAnswer).setAmPm24(i == 0? TimeQuestion.AM : TimeQuestion.PM);
                     mAnswer.setVisited(true);
                     mQuestion.setVisited(true);
                     return;
                 }
             }
         }
 
         public void setEnabled(boolean enabled) {
             mQuestionTextArea.setEnabled(enabled);
             mTimeTextField.setEnabled(enabled);
             for( int i = 0; i < mAmPmGroupButton.getButtonCount(); i++)
             {
                 mAmPmGroupButton.getRadioButton(i).setEnabled(enabled);
             }
         }
 
         public void handleMoreDetails( Object obj )
         {
             if ( ((TimeQuestion)getQuestion()).getConvention() != 24) {
             RadioButton rb = (RadioButton)mAmPmGroupButton.getRadioButton(mAmPmGroupButton.getSelectedIndex());
                 if ( rb.getText().equals("am")) {
                        ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.AM);
                 } else {
                      if ( rb.getText().equals("pm")) {
                          ((TimeQuestion)getQuestion()).setAm_pm(TimeQuestion.PM);
                      }
                 }
             }
         }
 
         public boolean validate() {
             return true;
         }
      }
 
     class HandleChoiceAnswersModified implements ActionListener{
         public void actionPerformed(ActionEvent ae) {
             setModifiedInterview(true);
         }
     }
 
     class HandleInterviewAnswersModified implements DataChangedListener {
 
         public void dataChanged(int arg0, int arg1) {
             // Modification in InterviewForm content
             setModifiedInterview(true);
         }
     }
 
     class HandleMoreDetails implements ActionListener {
 
         public void actionPerformed(ActionEvent evt) {
                 setModifiedInterview(true);
                 Object cmd = evt.getSource();
                 if (cmd instanceof RadioButton)
                 {
                     ContainerUI parent = (ContainerUI)((RadioButton)cmd).getParent();
                     parent.handleMoreDetails(cmd);
                 }
         }
     }
 
     class MultipleChoiceItem { // TODO use it in ChoiceField
 
         private boolean mSelected = false;
         private String mValue;
 
         public MultipleChoiceItem(String value) {
             this.mValue = value;
         }
 
         public boolean toggleSelection() {
             this.mSelected = !mSelected;
             return mSelected;
         }
 
         public String toString() {
             return mValue;
         }
 
         public boolean getSelected() {
             return mSelected;
         }
     }
 
     public static class RadioChoiceModel extends DefaultListModel {
 
         int mCheckedIndex = -1;
 
         public void addItem(Object item) {
             String value = (String) item;
             RadioChoiceItem radioItem = new RadioChoiceItem(value, this);
             super.addItem(radioItem);
         }
 
         public void addItem(RadioChoiceItem radioItem) {
             super.addItem(radioItem);
         }
 
         public void setItemChecked(RadioChoiceItem checkedItem, boolean checked) {
             for (int index = 0; index < getSize(); index++) {
                 RadioChoiceItem item = (RadioChoiceItem) getItemAt(index);
                 if ( item == checkedItem ) {
                     if ( checked )
                         mCheckedIndex = index;
                     else
                         mCheckedIndex = -1;
                     item.updateChecked(checked);
                 } else {
                     item.updateChecked(false);
                 }
             }
         }
 
         public void setItemChecked(int index) {
             RadioChoiceItem item = (RadioChoiceItem) getItemAt(index);
             setItemChecked(item, true);
         }
 
         public RadioChoiceItem getCheckedItem() {
             if ( mCheckedIndex >= 0 )
                 return (RadioChoiceItem)getItemAt(mCheckedIndex);
             else
                 return new RadioChoiceItem("", null);
         }
 
     }
 
     public static class RadioChoiceItem {
 
         private boolean mIsChecked = false;
         private final String mValue;
         private final RadioChoiceModel mModel;
         private String mDetailedValue = null;
 
         public RadioChoiceItem(String value, RadioChoiceModel model) {
             mValue = value;
             mModel = model;
         }
 
         public String toString() {
             String result = mValue;
             if ( hasMoreDetails() && getMoreDetails().length() != 0 ) {
                 result = result + " : " + getMoreDetails();
             }
             return result;
         }
 
         public String getValue() {
             return mValue;
         }
 
         public boolean isChecked() {
             return mIsChecked;
         }
 
         public void setChecked(boolean selected) {
             mIsChecked = selected;
             mModel.setItemChecked(this, mIsChecked);
         }
 
         public boolean toggleSelection() {
             setChecked(!mIsChecked);
             return mIsChecked;
         }
 
         /**
          * Does not update model
          */
         public void updateChecked(boolean selected) {
             mIsChecked = selected;
         }
 
         public boolean hasMoreDetails() {
             return (mDetailedValue != null);
         }
 
         public void setMoreDetailsText(String moreDetails) {
             mDetailedValue = moreDetails;
         }
 
         public String getMoreDetails() {
             return mDetailedValue;
         }
 
         public boolean equals(Object obj) {
             return mValue.equals(obj);
         }
 
         public int hashCode() {
             return mValue.hashCode();
         }
     }
 }
