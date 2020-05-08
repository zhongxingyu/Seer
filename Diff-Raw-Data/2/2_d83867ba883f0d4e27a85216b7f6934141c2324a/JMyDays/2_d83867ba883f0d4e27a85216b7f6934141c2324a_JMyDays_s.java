 /**
  * GoalsJournal (before: JMyDays) is a Daily habit tracker inspired on www.joesgoals.com
  * No warranty expressed or implied. Use at your own risk.
  *
  * @author Carlos G. Marin
  * @contact ing.cma AT gmail DOT com
  *
  *   Copyright 2011 Carlos Marin
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 /*
  * TODO:
  * +Fix Not working in linux because of temp file.
  * +Fix change year bug ... =( DONE... BUT FiX day shown name when different years shown in same screen.
  * +Fix BUG, adding new Label, won't show in future days... =( MAYBE DONE.
  * +Add backup DB function...
  * +Add logging txt file, instead of Sysouts...
  * +Import labels from previous day.
  * +Display labels catalog.
  * +Show option to select existing objectives, labels.
  * 
  * 
  * *Cipher security 
  * 		-Add password ON mode.
  * 		-Add password  
  * 		-Add password to crypt messages, 
  * 		-Request pass at startup to uncrypt msgs.
  * 		-Add password reminder field.
  * 		-Aller user password is not recoverable
  * 		-NEXT TODO: add option to change password and reset password in all old messages.
  * +Also catche screen position X,Y wise.
  * +Si abres un dia que no tiene nada guardado, que se carguen las actividades del ultimo CACHE Config...
  * -Fix: Si le pones palomita a un d�a SIN guardar, lo siguientes dias SIN guardar apareceran IGUAL (con palomita).
  * -Fix: label association on update.
  * -FIX: Call normaliza or missMatch method AFTER tracking component type change for a given label.
  * -Resaltar dia actual
  * -Bugs on switching checkboxes and stuff.
  * -Fix bug? or feature?: on label change, previous days label is shown as to be equal to current, until app restart. CHECK with registered days.
  * -Fix bug: On first time use, save /close with no actv. labels will show one on next time open.
  * -Consider changing to HSQLDB instead of SQLite... (to allow password protection, etc.)
  * -Optimice extraction (by not usgin hashmap?)
  *
  *
  * NICE TO HAVE:
  * -Password protection option (at least simple one to open program an access db).
  * -Actv label Right click menu option to select another label from the existing base.
  * -Refactor "cbs" and vars in DB.
  * -Scheduler... (i.e.: Set option to add activities with day recurrence, or lasting only a certain period (2 months), etc... ) (Then Show remaining days till expiration?).
  * -bugtracker �?
  * -Implment System to Specify objectives (quit smoking, loose weight, etc) in a way that templates can be exported (and imported) and uploaded to a website by users.
  * -USER AUTHENTICATION (or info cipher by key)
  * -Improved SQL Utility ( DBUtil.exequery(connection, query, ParamBin...).
  * -Save screen resolution onExit()...
  * -Shift+C copies al textPane content on Clipboard: use: java.awt.datatransfer...
  * -Remove "temp line"'s
  *
  *
  * DONE:
  * +Applied tipos from FindBugs(TM).
  * +Fix restore labels from cache if day has no info.
  * +!!!!!Avoid two instants of the app to run on the same DB.
  * +Send message on text export finish
  * -Fix: On date changed, contiguous day gets same component ID assign. (DONE, using unItiated day case in DB extraction method).
  * -FiX tracking changes SAVE when day changed. (Done by saving all visible days on change) (includes scroll buttons and date Button in  textfield observer)
  * -Hacer catch a todo char para UNDO por CHAR.
  * -Implementar funcion que haga almacene y restaure de cache el ultimo tama�o ajustado de la ventana.
  * -Improve UNDO by also saving initial state, not until Xth typed char.
  * -Fix size window restrictions, save resizes to cache ON-exit. (done: saving dimension object)
  * -Typed History, clean STACKS in MAPS when days changed? Clean REDO-Stack. (done, plus check for stack overflows)
  * -Implment UNDO in multiple TextPanes... (done usgin hashmaps)
  * -Implment an "UNDO" function using CONTROL+Z in the TextPanes. (copy last to clip board and scrool map history of last X chars?? ) (used stack to save every 5 chars typed)
  * -Change New label key shortcut....FROM 'Alt+3' TO 'CONTROL+N'.
  * -Add text resize (key shortcut: CONTRL+'+' , CONTROL+'-' or CONTROL+MouseScroll when in textField) option to make it possible to write with no-one being able to see what you write.
  * -Detect if activity labels are repeated... (DONE at insertion or specification of label with: "isActvIdRepeatToday()")
  * -Fix NOT saving label change bug :( (DONE: updated change in DB)
  * -Clean components values for new row when creating a new label. (Using "updateScreenResetRowValues").
  * -Show alert message when trying to increase shown labels num.
  * -Change "colorization" functions to hide component (or other option?, probably best to hide).
  * -Check if componentType differs from shown type. (For previous and post days) (Use actvId). (Using "allActvIds" array).
  * -Remove: TEMP FiX 1
  * -Indicar cuando un d�a tiene CBs que no se estan mostrando... (cambiar de color, poner: asteristco, "?", o algo). (Usar arreglo blobal para marcar id's por dia).
  * -FIX: ActvCompValueCopyEvents: * BUG: Parece que si vas a cerrar programa, ultimo focusLost NO se toma =(... (Fixed by doing a final copy of ALL components values to the text field before saving everything to db)
  * -Compare labels and don't load them from db if they are equal to currently loaded ones (using cache)
  * -ON Label change, check if exists, if true, get label, else, create new.
  * -Buscar forma de ejecutar metodo al hacer click en tache para cerrar para hacer que dicho metodo guarde todo antes de salir... =) (Usando: windowClosing Event Listener... )
  * -make swing components final, inicialize them in constructor (done with some).
  * -Keyboard shortcuts (arrows, menus, etc)
  * -Middle button to jumpo to today (Used HOME key)
  * -Rename Configs to Cache
  * -Funcion export to files :) by date rage... o ALL...
  * -Save checkBoxes on close or day change... NOT on focus for each... (DONE: failed..., reverted to prev. (focus by component).)
  * -Cache Object for actv labels.
  * -Keep cache of recent labels.
  * -Save last labels into cache
  *
  */
 
 /**
  * ********************* Mini documentation *********************
  * Database:
  * registros.cbs=Valor de la etiqueta.
  * registros.id_cbs=Identificador de la etiqueta mostrada.
  * cbs y id_cbs se asocian por posici�n en arreglo separado por PIPES ("|").
  * 
  * actvs_cat.compT = Component type.
  */
 
 package jmydays;
 
 import jmydays.beans.RegistrosBean;
 import jmydays.datapicker.DatePicker;
 import jmydays.util.CryptoUtils;
 import jmydays.util.Database;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 //import java.awt.Dialog;
 //import java.awt.event.ItemEvent;
 //import java.awt.event.WindowFocusListener;
 //import javax.swing.JComponent;
 //import javax.swing.JFrame;
 //import javax.swing.JSlider;
 //import java.awt.event.ItemEvent;
 //import java.awt.event.ItemListener;
 
 /**
  *
  * @author Carlos Guillermo Marin Amador
  * Contact ing.cma AT gmail.com
  */
 public class JMyDays implements JMyDaysConstants, Runnable {
 
     //Componentes UI
 //    private java.awt.Canvas canvas1;
 //    private javax.swing.JPanel jPanel2;
 
     private javax.swing.JPanel jPanel1;
     private javax.swing.JFrame jFrame1;
     private javax.swing.JButton jDateBTN;
     private javax.swing.JButton rightBTN;
     private javax.swing.JButton leftBTN;
     private javax.swing.JMenu jMenuFile;
     private javax.swing.JMenu jMenuTools;
     private javax.swing.JMenu jMenuAbout;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItemF1;
     private javax.swing.JMenuItem jMenuItemF2;
     private javax.swing.JMenuItem jMenuItemF4;
     private javax.swing.JCheckBoxMenuItem jMenuItemF5;
     private javax.swing.JMenuItem jMenuItemFExit;
     private javax.swing.JMenuItem jMenuItemA1;
     private javax.swing.JMenuItem jMenuItemT1;
     private javax.swing.JMenuItem jMenuItemT2;
 
     //Dias y Mes
     private JLabel jLblPrevDay1 = new JLabel();
     private JLabel jLblToday = new JLabel();
     private JLabel jLblPostDay1 = new JLabel();
     private JLabel jLblMonth = new JLabel();
 
     private ObservingTextField selectedDateTextField;
 
     //Otros
     private final ResourceBundle recursosTexto;
     private final ResourceBundle recursosConfig;
     private Locale selectedLocale;
     private JMyDaysCache cache;
     private int currentlyShownActivityRowsNum = ACTV_ROW_NUM_MIN;
     private final Dimension dimPant;
     private final java.awt.Dimension appDimensions;
     private java.awt.Color actvDefColor;
     private int allActvIds[][] = new int[ACTV_DISP_DAYS][ACTV_ROW_NUM_MAX];
     private final int todayTextPaneArrPos;
     private int currentTextPaneFontSizeGlobal;
     
     //Typing History
     private final java.util.Map<String, java.util.Stack<String>> typedHistoryByPanelsMap = new java.util.HashMap<String, java.util.Stack<String>>();
 //    private java.util.Map<String, Integer> typedCharsCountMap = new java.util.HashMap<String, Integer>();
     //private int typedCharsCount = 0;
 
     //Componen Arrays
     private JTextField[] jTextFieldsArr;
     private MyActvJLabel[] jLabelsActvArr;
     private MyJCheckBox[] jCheckBoxesCol1;
     private MyJCheckBox[] jCheckBoxesCol2;
     private MyJCheckBox[] jCheckBoxesCol3;
     private MyJTextField[] jTextFieldsCol1;
     private MyJTextField[] jTextFieldsCol2;
     private MyJTextField[] jTextFieldsCol3;
     private MyJSlider[] jSlidersCol1;
     private MyJSlider[] jSlidersCol2;
     private MyJSlider[] jSlidersCol3;
     private MyJTextPane[] jTextPanesArr;
     private JScrollPane[] jScrollPanesArr;
 
 
     //Fechas
     private Date nowDate;
     private Date selectedDate;
     private Calendar calend;
     private String today;
     private SimpleDateFormat todayFormat;
     private SimpleDateFormat monthFormat;
     private SimpleDateFormat dbPKeyFormat;
     private DateFormat textFiledFormat;
     private DatePicker dp;
     private java.awt.Font fuente1;
     private java.awt.Font fuente1b;
 //    private java.awt.Font fuente1i;
 
 	public static final String LINE_SEPARATOR;
 	static{
 		String temp = System.getProperty("line.separator");
 		if( temp != null && !EMPTY_STRING.equals(temp) ){
 			LINE_SEPARATOR = temp;
 		}else{
 			LINE_SEPARATOR = "\n"; //Default Windows
 		}
 	}//static
     
     //Database
     private java.sql.Connection dbConnection;
     private java.sql.PreparedStatement prepStatement;
     private java.sql.ResultSet resultSet;
 
     //password
     private static boolean isUsingPass = false;
     
     public static void main(String[] args) throws Exception {
     	//TODO: Refactor as: System.getEnv("startDelay")....
         for (String str: args) {
         	if( str.indexOf("=") > -1 ){
         		String argPair[] = str.split("=");
         		if( "startDelay".equalsIgnoreCase(argPair[0]) ){
         			int delayAmount = 0;
         			try{
         				delayAmount = Integer.parseInt(argPair[1]);
         			}catch(NumberFormatException nfe){
         				System.out.println("Error getting 'starDelay' time: " + nfe.getMessage());
         			}
         			delayAmount *= 1000;
         			Thread.sleep(delayAmount);
         		}
         	}
         }
         
         java.awt.EventQueue.invokeLater( new JMyDays() );
     }
 
     public void run(){
         //Finally set visible...
         jFrame1.setVisible(true);
     }
 
     public JMyDays() throws Exception{
 
         unSerilizeCache(); //first step to restore cached settings.
 
         /* ############ Final Configs Stuff... ############ */
         //Inicia recursos
         recursosConfig  = java.util.ResourceBundle.getBundle("config");
         //Take locale from file or from cache... first check "localeOverride" to see if taking from RESOURCE-FILE or from CACHE...
         //Test both sources, if invalid then use DEFAULT LOCALE.
         if( ("").equals( recursosConfig.getString("localeOvrrPreReq") ) ){
             selectedLocale = ("").equals( cache.defSavedLang ) ? Locale.getDefault(): new Locale( cache.defSavedLang );
         }else{
             String locOverr = recursosConfig.getString("localeOvrrSpecif").trim();
             selectedLocale = locOverr.equals("") ? Locale.getDefault() : new Locale(locOverr);
         }
         
         //dbFile
         Database.changeDBFilePath(recursosConfig.getString("dbFileName"));
         
         recursosTexto = java.util.ResourceBundle.getBundle("texts", selectedLocale);
         dimPant = Toolkit.getDefaultToolkit().getScreenSize();
         appDimensions = new java.awt.Dimension();
         /* ############ Final Configs Stuff... ############ */
         
         initObjs();
         
         todayTextPaneArrPos = (int)java.lang.Math.floor(jTextPanesArr.length/2);
 
         initConfigs();
 
         restoreCache();
 
         updateScreenSetComponentTextSize(currentTextPaneFontSizeGlobal, jTextPanesArr);
 
         addEventListeners();
         
         //password usage
         isUsingPass = DB_TRUE_CHAR.equals( getFromDBParameterByName("isUsingPass") ) ? true : false;
         if( isUsingPass ){
         	showPasswordValidationDialog(PASS_REQUEST);
         }
 
         //password menu
         jMenuItemF5.setState(isUsingPass);
         
         updateScreenAllByDate(selectedDate);
 
     }
     
     private void initObjs(){
 
         //Object Packaging...
         jLabelsActvArr = initMyActvJLabelsArray(ACTV_ROW_NUM_MAX);
         jTextFieldsArr = initMyJTextFieldsArray(ACTV_ROW_NUM_MAX, DAYS_TO_ROLL_NONE);
         //Central Activity Rows
         jCheckBoxesCol1 = initMyJCheckBoxesArray(ACTV_ROW_NUM_MAX, ROLL_1_DAY_BACK);
         jCheckBoxesCol2 = initMyJCheckBoxesArray(ACTV_ROW_NUM_MAX, DAYS_TO_ROLL_TODAY);
         jCheckBoxesCol3 = initMyJCheckBoxesArray(ACTV_ROW_NUM_MAX, ROLL_1_DAY_FRWR);
 
         jSlidersCol1 = initMyJSlidersArray(ACTV_ROW_NUM_MAX, ROLL_1_DAY_BACK);
         jSlidersCol2 = initMyJSlidersArray(ACTV_ROW_NUM_MAX, DAYS_TO_ROLL_TODAY);
         jSlidersCol3 = initMyJSlidersArray(ACTV_ROW_NUM_MAX, ROLL_1_DAY_FRWR);
 
         jTextFieldsCol1 = initMyJTextFieldsArray(ACTV_ROW_NUM_MAX, ROLL_1_DAY_BACK);
         jTextFieldsCol2 = initMyJTextFieldsArray(ACTV_ROW_NUM_MAX, DAYS_TO_ROLL_TODAY);
         jTextFieldsCol3 = initMyJTextFieldsArray(ACTV_ROW_NUM_MAX, ROLL_1_DAY_FRWR);
 
 
         final MyJTextPane jTextPane1 = new MyJTextPane();
         final MyJTextPane jTextPane2 = new MyJTextPane();
         final MyJTextPane jTextPane3 = new MyJTextPane();
         jTextPanesArr = new MyJTextPane[]{jTextPane1, jTextPane2, jTextPane3};
 
         final javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
         final javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
         final javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
         jScrollPanesArr = new JScrollPane[]{jScrollPane1, jScrollPane2, jScrollPane3};
 
         //Look and Feel
         final String LAF = recursosConfig.getString("LAF");
         if( !LAF_NONE.equals(LAF) ){
             setLookAndFeel( LAF );
         }
 
         //Fechas
         nowDate = new java.util.Date();
         selectedDate = nowDate;
         textFiledFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, selectedLocale);
         today = textFiledFormat.format( selectedDate );
         todayFormat = new SimpleDateFormat(SDF_DAY_PATTERN_1, selectedLocale);
         monthFormat = new SimpleDateFormat(SDF_MONTH_PATTERN_1, selectedLocale);
         dbPKeyFormat = new SimpleDateFormat(DB_DATE_PKEY_PATTERN, selectedLocale); //"yyyy-MM-dd"
         calend = Calendar.getInstance(selectedLocale);
 
         currentTextPaneFontSizeGlobal = 12;
         fuente1 = new java.awt.Font("Arial", Font.PLAIN, 12);
         fuente1b = new java.awt.Font("Arial", Font.BOLD, 12);
 //        fuente1i = new java.awt.Font("Arial", Font.ITALIC, 12);
         final java.awt.Font fuente2 = new java.awt.Font("Arial", Font.PLAIN, 24);
 
         jFrame1 = new javax.swing.JFrame();
         jPanel1 = new javax.swing.JPanel();
 
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenuFile = new javax.swing.JMenu();
         jMenuTools = new javax.swing.JMenu();
         jMenuAbout = new javax.swing.JMenu();
 
         jMenuItemF1 = new javax.swing.JMenuItem();
         jMenuItemF2 = new javax.swing.JMenuItem();
         jMenuItemF4 = new javax.swing.JMenuItem();
         jMenuItemF5 = new javax.swing.JCheckBoxMenuItem();
         jMenuItemFExit = new javax.swing.JMenuItem();
         jMenuItemA1 = new javax.swing.JMenuItem();
         jMenuItemT1 = new javax.swing.JMenuItem();
         jMenuItemT2 = new javax.swing.JMenuItem();
 
         jDateBTN = new javax.swing.JButton();
         rightBTN = new javax.swing.JButton();
         leftBTN = new javax.swing.JButton();
 
         selectedDateTextField = new ObservingTextField();
 
 //        final java.awt.Dimension tempDimensions = new java.awt.Dimension();
         final java.awt.Insets tempInset = new java.awt.Insets(0, 0, 0, 0);
         final java.awt.GridBagConstraints gbc = new GridBagConstraints();
 
         //Frame
         jFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         jFrame1.setFont(fuente1);
         jFrame1.setTitle( recursosTexto.getString("appTitle"));
 
         jPanel1.setLayout(new GridBagLayout());
         jPanel1.setOpaque(true);
 
         //Panel Layout
         jPanel1.setLayout(new GridBagLayout());
 
         //Basic layout Constraints
         gbc.fill = GridBagConstraints.NONE;
         gbc.anchor = GridBagConstraints.CENTER;
 
         //Vars locales de despliegue
         final int posXBotonFecha = 4;
         final int posYBotonFecha = 0;
 
         final int posXFlechas = 4;
 
         final int posYMes = 1;
 
         final int posXLabelsTop = 4;
         final int posYLabelsTop = 2;
         final int gridWidthLabelsTop = 3;
 
         //Labels actividades
         final int posXLabelAct = 0;
         final int gridWidthLabelAct = 3;
 
         final int posYLabelActA = 3;
 
         //CheckBoxes
         final int posXCheckBoxes = 4;
         final int gridWidthCheckBoxes = 3;
 
         final int posYCheckBoxesA = 3;
 
         final int posXTextPanes = 4;
         final int posYTextPanes = 12;
         final int gridWidthTextPanes = 3;
 
         //Fila 1
         gbc.gridy = posYBotonFecha;
         gbc.insets = tempInset;
 
         //Texfields
         selectedDateTextField.setColumns(TEXTFIELD_COL_SIZE);
         selectedDateTextField.setToolTipText(recursosTexto.getString("dateTipL"));
         selectedDateTextField.setText(today);
         selectedDateTextField.setFocusable(false);
         gbc.anchor = GridBagConstraints.EAST;
         gbc.gridx = posXBotonFecha+7;
         gbc.gridwidth = 2;
         jPanel1.add(selectedDateTextField, gbc);
 
     //Buttons
         //Date button
         jDateBTN.setText(recursosTexto.getString("dateBtnL"));
         gbc.anchor = GridBagConstraints.WEST;
         gbc.gridx = posXBotonFecha+9;
         gbc.gridwidth = 1;
         jPanel1.add(jDateBTN, gbc);
 
         
         //Next y Prev
         tempInset.set(0, 0, INSETS_SEP_A_FULL, 0);
         gbc.insets = tempInset;
         gbc.gridy = posYLabelsTop;
 
         //Next
 //        tempDimensions.setSize(DIM_BTN_DEF_X, DIM_BTN_DEF_Y);
         rightBTN.setIcon( new javax.swing.ImageIcon(getClass().getResource("/nextmon.gif"), ">") );
         gbc.anchor = GridBagConstraints.EAST;
         gbc.gridx = posXFlechas+8;
         jPanel1.add(rightBTN, gbc);
 
         //Prev
         leftBTN.setIcon( new javax.swing.ImageIcon(getClass().getResource("/premon.gif"), "<") );
         gbc.gridx = posXFlechas+1;
         jPanel1.add(leftBTN, gbc);
 
 
     //Labels
         //Mes
         tempInset.set(0, 0, 0, INSETS_SEP_A_FULL);
         gbc.insets = tempInset;
         gbc.weightx = 0;
         gbc.anchor = GridBagConstraints.EAST;
         gbc.gridwidth = 3;
         gbc.gridx = posXFlechas+7;
         gbc.gridy = posYMes;
         jLblMonth.setFont(fuente2);
         jLblMonth.setText( monthFormat.format( nowDate ) );
         jPanel1.add(jLblMonth, gbc);
 
         //Dias
         tempInset.set(0, INSETS_SEP_A_HALF, INSETS_SEP_A_HALF, INSETS_SEP_A_FULL);
         gbc.insets = tempInset;
         gbc.weightx = 0;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.gridwidth = gridWidthLabelsTop;
         gbc.gridy = posYLabelsTop;
 
         //Today
 //        calend.roll(Calendar.DAY_OF_YEAR, 1);
         jLblToday.setText(todayFormat.format( calend.getTime()));
         gbc.gridx = posXLabelsTop + (gridWidthTextPanes);
         jPanel1.add(jLblToday, gbc);
 
         
         calend.roll(Calendar.DAY_OF_YEAR, ROLL_1_DAY_FRWR); //Tommorrow
         //Special case if has been rolled to first day of year... also roll year
         if( calend.get(Calendar.DAY_OF_YEAR) == calend.getActualMinimum(Calendar.DAY_OF_YEAR) ){
         	calend.roll(Calendar.YEAR, 1); //Forward one day
         }
         jLblPostDay1.setText(todayFormat.format( calend.getTime()));
         gbc.gridx = posXLabelsTop + (gridWidthTextPanes*2);
         jPanel1.add(jLblPostDay1, gbc);
 
         //Special case if has been rolled to first day of year... also roll year
         if( calend.get(Calendar.DAY_OF_YEAR) == calend.getActualMinimum(Calendar.DAY_OF_YEAR) ){
         	calend.roll(Calendar.YEAR, -1); //Back one year
         }        
         calend.roll(Calendar.DAY_OF_YEAR, ROLL_1_DAY_BACK-1); //Yesterday
         jLblPrevDay1.setText(todayFormat.format( calend.getTime()));
         gbc.gridx = posXLabelsTop;
         jPanel1.add(jLblPrevDay1, gbc);
 
 
 //TODO: Programatically add components (i.e.: in a for loop, add new components, to array and then work from the array
 
     //Labels y TextFields Actividades
 
         //Contenidos
         tempInset.set(0, 0, 0, 0);
         gbc.insets = tempInset;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.weightx = 0;
         gbc.gridwidth = gridWidthLabelAct;
         gbc.gridx = posXLabelAct;
 
         gbc.gridy = posYLabelActA;
         addActivityRowComponentsColumnWise(jPanel1, jLabelsActvArr, gbc); //Labels
         addActivityRowComponentsColumnWise(jPanel1, jTextFieldsArr, gbc); //TextFields
 
     //Checkboxes
         tempInset.set(0, 0, 0, INSETS_SEP_A_FULL);
         gbc.insets = tempInset;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.weightx = 0;
         gbc.gridwidth = gridWidthCheckBoxes;
 
         gbc.gridy = posYCheckBoxesA;
         gbc.gridx = posXCheckBoxes;
         addActivityRowComponentsColumnWise(jPanel1, jCheckBoxesCol1, gbc);
         addActivityRowComponentsColumnWise(jPanel1, jSlidersCol1, gbc);
         addActivityRowComponentsColumnWise(jPanel1, jTextFieldsCol1, gbc);
 
         gbc.gridx = posXCheckBoxes+(gridWidthCheckBoxes);
         addActivityRowComponentsColumnWise(jPanel1, jCheckBoxesCol2, gbc);
         addActivityRowComponentsColumnWise(jPanel1, jSlidersCol2, gbc);
         addActivityRowComponentsColumnWise(jPanel1, jTextFieldsCol2, gbc);
 
         gbc.gridx = posXCheckBoxes+(gridWidthCheckBoxes*2);
         addActivityRowComponentsColumnWise(jPanel1, jCheckBoxesCol3, gbc);
         addActivityRowComponentsColumnWise(jPanel1, jSlidersCol3, gbc);
         addActivityRowComponentsColumnWise(jPanel1, jTextFieldsCol3, gbc);
 
 
     /* ### TextPanes ###  */
 
         //JTextPane Name indicates days to roll from the (middle) actual-selected date....
 
         //Aplica para todos
         tempInset.set(INSETS_SEP_A_FULL, 0, 0, INSETS_SEP_A_FULL);
         gbc.insets = tempInset;
         gbc.gridwidth = gridWidthTextPanes;
 
         //1
         jTextPane1.setName(DAYS_TO_ROLL_PREV1_STR);
         jTextPane1.setText( recursosTexto.getString("YesterdayTPTxt") );
         jScrollPane1.setViewportView(jTextPane1);
         gbc.gridx = posXTextPanes;
         gbc.gridy = posYTextPanes;
         jPanel1.add(jScrollPane1, gbc);
 
         //2
         jTextPane2.setName(DAYS_TO_ROLL_TODAY_STR);
         jTextPane2.setText( recursosTexto.getString("TodayTPTxt") );
         jScrollPane2.setViewportView(jTextPane2);
         gbc.gridx = posXTextPanes+(gridWidthTextPanes);
         gbc.gridy = posYTextPanes;
         jPanel1.add(jScrollPane2, gbc);
 
         //3
         jTextPane3.setName(DAYS_TO_ROLL_FRWR1_STR);
         jTextPane3.setText( recursosTexto.getString("TomorrowTPTxt") );
         jScrollPane3.setViewportView(jTextPane3);
         gbc.gridx = posXTextPanes+(gridWidthTextPanes*2);
         gbc.gridy = posYTextPanes;
         jPanel1.add(jScrollPane3, gbc);
 
         //Menus
         //Listeners added in listeners method
         //IMPORTANT!!! Menus NOT added in menuVar name order...
         jMenuFile.setText( recursosTexto.getString("fileMenu") );
         jMenuFile.setFont(fuente1);
 
         jMenuTools.setText( recursosTexto.getString("toolsMenu") );
         jMenuTools.setFont(fuente1);
 
         jMenuAbout.setText( recursosTexto.getString("aboutMenu") );
         jMenuAbout.setFont(fuente1);
 
         jMenuItemF1.setText(recursosTexto.getString("fileMenuItem1"));
         jMenuItemF1.setMnemonic(KeyEvent.VK_N);
         jMenuItemF1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
         jMenuItemF1.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("fileMenuAccDesc1") );
 
         jMenuItemF2.setText(recursosTexto.getString("fileMenuItem2"));
         jMenuItemF2.setMnemonic(KeyEvent.VK_1);
         jMenuItemF2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
         jMenuItemF2.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("fileMenuAccDesc2") );
 
         jMenuItemF4.setText(recursosTexto.getString("fileMenuItem4"));
         jMenuItemF4.setMnemonic(KeyEvent.VK_L);
         jMenuItemF4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
         jMenuItemF4.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("fileMenuAccDesc4") );
 
         jMenuItemF5.setText(recursosTexto.getString("msgPassEnable"));
         jMenuItemF5.setMnemonic(KeyEvent.VK_P);
         jMenuItemF5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
         jMenuItemF5.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("msgPassEnableDesc") );
         
         jMenuItemFExit.setText(recursosTexto.getString("fileMenuItemExit"));
         jMenuItemFExit.setMnemonic(KeyEvent.VK_X);
         jMenuItemFExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
         jMenuItemFExit.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("fileMenuAccDescExit") );
 
         jMenuItemT1.setText(recursosTexto.getString("toolsMenuItem1"));
         jMenuItemT1.setMnemonic(KeyEvent.VK_E);
         jMenuItemT1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
         jMenuItemT1.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("toolsMenuItem1AccDesc") );
 
         jMenuItemT2.setText(recursosTexto.getString("toolsMenuItem2"));
         jMenuItemT2.setMnemonic(KeyEvent.VK_C);
         jMenuItemT2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
         jMenuItemT2.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("toolsMenuItem2AccDesc") );
 
         jMenuItemA1.setText(recursosTexto.getString("aboutMenuItem1"));
         jMenuItemA1.setMnemonic(KeyEvent.VK_F1);
         jMenuItemA1.setAccelerator(KeyStroke.getKeyStroke("F1"));
         jMenuItemA1.getAccessibleContext().setAccessibleDescription( recursosTexto.getString("aboutMenuItem1") );
 
 
         //NOT added in menuVar name order...
         jMenuFile.add(jMenuItemF1);
         jMenuFile.add(jMenuItemF2);
         jMenuFile.add(jMenuItemF4);
         jMenuFile.add(jMenuItemF5);
         jMenuFile.add(jMenuItemFExit);
 
         jMenuAbout.add(jMenuItemA1);
 
         jMenuTools.add(jMenuItemT1);
         jMenuTools.add(jMenuItemT2);
 
         jMenuBar1.add(jMenuFile);
         jMenuBar1.add(jMenuTools);
         jMenuBar1.add(jMenuAbout);
 
         //Icon Images
 //        jMenuItemF1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/nextmon.gif")));
 
         jFrame1.setJMenuBar(jMenuBar1);
 
         //Size and Position
         if( dimPant.width >= APP_SIZE_RECOMMENDED_MIN_WIDTH && dimPant.height >= APP_SIZE_RECOMMENDED_MIN_HEIGHT ){
 
             if( cache.getAppDimensions().getWidth() == APP_SIZE_NOT_INITIALIZED ){
                 appDimensions.setSize( APP_SIZE_RECOMMENDED_MIN_WIDTH, APP_SIZE_RECOMMENDED_MIN_HEIGHT );
             }else{
                 appDimensions.setSize( cache.getAppDimensions() );
             }
 
             jPanel1.setSize(appDimensions);
             jPanel1.setPreferredSize(appDimensions);
 
             jFrame1.setSize(appDimensions);
             jFrame1.setPreferredSize(appDimensions);
         }else{
             appDimensions.setSize(APP_SIZE_WIDTH_MIN, APP_SIZE_HEIGHT_MIN);
             jFrame1.setMinimumSize( appDimensions );
 //            jFrame1.setSize(dimPant.width/2, dimPant.height/2);
         }
 
         jFrame1.setLocation( (dimPant.width-jFrame1.getWidth())/2, (dimPant.height-jFrame1.getHeight())/2 );
 
         //Focus panel to catch panel key events
         jPanel1.setFocusable(true);
 
     //END
         jFrame1.add(jPanel1, BorderLayout.CENTER);
         jFrame1.setContentPane(jPanel1);
         jFrame1.setVisible(false); //Commet to DEBUG
 
         //instance execution lock
         if( !Database.createInstanceLock() ){
         	Database.removeInstanceLock(); //si se va la luz y no se elimina el archivo nunca podra entrar de nuevo...
         	JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrInstanteLockL"), recursosTexto.getString("msgErrorTitle"), JOptionPane.ERROR_MESSAGE);
         	System.exit(-1);
         }
     }
 
     private MyActvJLabel[] initMyActvJLabelsArray(final int amount){
         MyActvJLabel jLbls[] = new MyActvJLabel[amount];
         for (int i = 0; i < amount; i++) {
             jLbls[i] = new MyActvJLabel();
         }
         return jLbls;
     }
 
     private MyJCheckBox[] initMyJCheckBoxesArray(final int amount, final int daysToRoll){
         MyJCheckBox cbsArr[] = new MyJCheckBox[amount];
         for (int i = 0; i < amount; i++) {
             cbsArr[i] = new MyJCheckBox();
             cbsArr[i].setCompRelationY(i);
             cbsArr[i].setCompRelationX(daysToRoll);
         }
         return cbsArr;
     }
 
     private MyJSlider[] initMyJSlidersArray(final int amount, final int daysToRoll){
         MyJSlider jSliders[] = new MyJSlider[amount];
         for (int i = 0; i < amount; i++) {
             jSliders[i] = new MyJSlider();
             jSliders[i].setCompRelationY(i);
             jSliders[i].setCompRelationX(daysToRoll);
         }
         return jSliders;
     }
 
     private MyJTextField[] initMyJTextFieldsArray(final int amount, final int daysToRoll){
         MyJTextField jTxtFlds[] = new MyJTextField[amount];
         for (int i = 0; i < amount; i++) {
             jTxtFlds[i] = new MyJTextField();
             jTxtFlds[i].setCompRelationY(i);
             jTxtFlds[i].setCompRelationX(daysToRoll);
         }
         return jTxtFlds;
     }
 
     private void addActivityRowComponentsColumnWise(final JPanel jPanel, final Component[] comps, final GridBagConstraints gbc){
         final int gbcY = gbc.gridy;
         for(Component comp: comps) {
             jPanel.add(comp, gbc);
             gbc.gridy += 1; //increment y
         }
         gbc.gridy = gbcY; //Reestablece Y original (para evitar efectos secundarios)
     }
 
     private void initConfigs(){
         Dimension configsDim;
 
         configsDim = new Dimension(DIM_ACTV_LABEL_DEFAULT_X, DIM_ACTV_LABEL_DEFAULT_Y);
         reSizeComponents(jLabelsActvArr, configsDim);
         reSizeComponents(jTextFieldsArr, configsDim);
 
         reNameSecuencialComponents(jLabelsActvArr);
         reNameSecuencialComponents(jTextFieldsArr);
         
         visibleComponents(jTextFieldsArr, false);
 
         visibleComponents(jTextFieldsCol1, false);
         visibleComponents(jTextFieldsCol2, false);
         visibleComponents(jTextFieldsCol3, false);
 
         visibleComponents(jCheckBoxesCol1, false);
         visibleComponents(jCheckBoxesCol2, false);
         visibleComponents(jCheckBoxesCol3, false);
 
         visibleComponents(jSlidersCol1, false);
         visibleComponents(jSlidersCol2, false);
         visibleComponents(jSlidersCol3, false);
         
         configsDim = new Dimension(DIM_ACTV_TXTFLD_DEFAULT_X, DIM_ACTV_TXTFLD_DEFAULT_Y);
         reSizeComponents(jTextFieldsCol1, configsDim);
         reSizeComponents(jTextFieldsCol2, configsDim);
         reSizeComponents(jTextFieldsCol3, configsDim);
 
         configsDim = new Dimension(DIM_ACTV_SLIDER_DEFAULT_X, DIM_ACTV_SLIDER_DEFAULT_Y);
         reSizeComponents(jSlidersCol1, configsDim);
         reSizeComponents(jSlidersCol2, configsDim);
         reSizeComponents(jSlidersCol3, configsDim);
 
         configTextFields(jTextFieldsCol1);
         configTextFields(jTextFieldsCol2);
         configTextFields(jTextFieldsCol3);
 
         configSliders(jSlidersCol1);
         configSliders(jSlidersCol2);
         configSliders(jSlidersCol3);
 
         configsDim = new Dimension(DIM_TXTPANE_DEF_X, DIM_TXTPANE_DEF_Y);
         reSizeComponents(jScrollPanesArr, configsDim);
 
         initTypedHistoryMaps();
 
     }
 
     private void reNameSecuencialComponents(final Component[] comps){
         for (int i = 0; i < comps.length; i++) {
             comps[i].setName( String.valueOf(i) );
         }
     }
 
     private void reSizeComponents(final Component[] comps, final Dimension prefSize){
         for (int i = 0; i < comps.length; i++) {
             comps[i].setSize(prefSize);
             comps[i].setPreferredSize(prefSize);
         }
     }
 
     private void configTextFields(final javax.swing.JTextField textFields[] ){
         for (int i = 0; i < textFields.length; i++) {
             textFields[i].setDocument( new JTextFieldLimit(ACTV_TEXTFIELD_MAX_INPUT, true) );
         }
     }
 
     private void configSliders(final javax.swing.JSlider sliders[] ){
         for (int i = 0; i < sliders.length; i++) {
             sliders[i].setMinimum(ACTV_SLIDER_MIN_VAL);
             sliders[i].setMaximum(ACTV_SLIDER_MAX_VAL);
             sliders[i].setMinorTickSpacing(ACTV_SLIDER_MIN_TICK_SPACE);
             sliders[i].setMajorTickSpacing(ACTV_SLIDER_MAX_TICK_SPACE);
             sliders[i].setPaintTicks(true);
             sliders[i].setPaintTrack(true);
             sliders[i].setSnapToTicks(true);
             sliders[i].setPaintLabels(false);
 //            sliders[i].setExtent(10);
         }
     }
 
     private void visibleComponents(final Component[] comps, final boolean visible){
 //        for (int i = 0; i < comps.length && i < currentlyShownActivityRowsNum; i++) {
         for (int i = 0; i < comps.length; i++) {
             comps[i].setVisible(visible);
         }
     }
 
     private void setLookAndFeel(final String laf) {
         try {
             final int lafNum = Integer.parseInt(laf);
             switch(lafNum){
                 default:
                 case -2: //Nimbus
                 	boolean isLAFFound = false;
                     for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                         if ("Nimbus".equals(info.getName())) {
                         	javax.swing.UIManager.setLookAndFeel(info.getClassName());
                         	isLAFFound = true;
                             break;
                         }
                     }
                 	if( isLAFFound ){
                 		break;
                 	}
                 	
                 case 0: //Java LAF
                     javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
                     break;
 
                 case 1: //System LAF
                     javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                     break;
 
                 case 2:
                     javax.swing.UIManager.setLookAndFeel( recursosConfig.getString("LAFClass") );
                     break;
             }
         } catch (Exception e) {
             System.err.println("Error setting Look and Feel: " + e.getMessage());
             try{
                 javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
             }catch(Exception e2){
                 System.err.println("Error setting Look and Feel (1st and only retry): " + e2.getMessage());
             }
         }
 
         JFrame.setDefaultLookAndFeelDecorated(true);
     }
 
     /**
      * Creates stacks for each text pane and puts them in a map.
      * Does the same thing for size counters.
      */
     private void initTypedHistoryMaps(){
         for(MyJTextPane mjtp : jTextPanesArr){
             String mjtpName = mjtp.getName();
             typedHistoryByPanelsMap.put(mjtpName, new java.util.Stack<String>() );
             typedHistoryByPanelsMap.put(mjtpName+TYPED_HISTORY_REDO_POSTFIX, new java.util.Stack<String>() );
         }
     }
 
     /**
      * Method that shows or hides rows of labels and checkboxes according to the currently shown Day.
      */
     private void showActvRows(final int numLabelsToShow){
 
         //TEMP: Pending Efficiency Improvement... don't do if redundant... find who increases...
         if( numLabelsToShow == ACTV_ROW_NUM_INIT_VAL ){ //|| (numLabelsToShow != ACTV_ROWS_NUM_MIN && numLabelsToShow == currentlyShownActivityRowsNum) ){
             return;
         }
 
         boolean isVisible = false;
         int tempInt = 0;
         int toShowMissing = 0;
 
         if( numLabelsToShow > currentlyShownActivityRowsNum ){
             tempInt = currentlyShownActivityRowsNum;
             toShowMissing = numLabelsToShow - currentlyShownActivityRowsNum;
             isVisible = true;
         }else{
             tempInt = numLabelsToShow;
         }
 
         for (int i = tempInt; i < ACTV_ROW_NUM_MAX; i++) {
             jLabelsActvArr[i].setVisible(isVisible);
             
             jTextFieldsCol1[i].setVisible(isVisible);
             jTextFieldsCol2[i].setVisible(isVisible);
             jTextFieldsCol3[i].setVisible(isVisible);
 
             jCheckBoxesCol1[i].setVisible(isVisible);
             jCheckBoxesCol2[i].setVisible(isVisible);
             jCheckBoxesCol3[i].setVisible(isVisible);
 
             jSlidersCol1[i].setVisible(isVisible);
             jSlidersCol2[i].setVisible(isVisible);
             jSlidersCol3[i].setVisible(isVisible);
 
             if( --toShowMissing == 0 ){
                 break;
             }
         }
 
         currentlyShownActivityRowsNum = numLabelsToShow;
         
         updateScreenNormalize();
     }
 
 
     private void showPasswordValidationDialog(final int INTENT){
     	try{
 
 			String passSHA12 = EMPTY_PASS_STRING;
 			String inputPass = JOptionPane.showInternalInputDialog(jPanel1, recursosTexto.getString("msgPassInput"), recursosTexto.getString("msgPassInputTitle"), JOptionPane.INFORMATION_MESSAGE);
 			//JPasswordField
 			if( inputPass != null ){
 				if( inputPass.trim().length() < PASS_LENGTH_MIN ){
 					if( inputPass.equals(EMPTY_STRING) ){
 						JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgPassDisabled"), recursosTexto.getString("msgPassDisabled"), JOptionPane.ERROR_MESSAGE);
 					}else{
 						JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgPassWrongInput"), recursosTexto.getString("msgErrorTitle"), JOptionPane.ERROR_MESSAGE);
 					}
 					isUsingPass = false;
 					jMenuItemF5.setState(isUsingPass);
 					return;
 				}
 				passSHA12 = CryptoUtils.SHA1( CryptoUtils.SHA1(inputPass) );
 				
 				String pasSHA12DB = getFromDBParameterByName("passSha12");
 				if( pasSHA12DB == null || EMPTY_STRING.equals(pasSHA12DB) ){
 					if( INTENT == PASS_REQUEST ){
 						JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgPassNewSet"), recursosTexto.getString("msgPassEnable"), JOptionPane.ERROR_MESSAGE);
 					}
 					
 					isUsingPass = true;
 					updateInDBPasswordValue(passSHA12); //set new password value
 				}else if( passSHA12.equals(pasSHA12DB) ){
 					isUsingPass = true;
 				}else{
 					JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgPassWrongInput"), recursosTexto.getString("msgErrorTitle"), JOptionPane.ERROR_MESSAGE);
 					//System.exit(-1); //Allow to continue without password
 					isUsingPass = false;
 					jMenuItemF5.setState(isUsingPass);
 				}
 				
 				if( isUsingPass ){
 					CryptoUtils.setCurrentPassword(inputPass);
 					jMenuItemF5.setState(isUsingPass);
 				}
 				
 			}else{
 				JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgPassDisabled"), recursosTexto.getString("msgPassDisabled"), JOptionPane.ERROR_MESSAGE);
 				isUsingPass = false;
 				jMenuItemF5.setState(isUsingPass);
 			}
 			
     	}catch(Exception e){
     		System.err.println("Maybe db password parameter not set.\n" + e.getMessage());
 		}
     	
     }
     
     /**
      * Method that checks if the today is the selected date.
      * @return TRUE only if Today == selectedDate.
      */
     private boolean isTodayTheSelectedDate(){
         return todayFormat.format(selectedDate).equals( todayFormat.format(new Date()));
     }
 
     private void serilizeTodaysCache(){
         
         if( !isTodayTheSelectedDate() ){
             updateScreenAllByDate( new Date() );
         }
 
         //Cache labels
         //Fix: Using currently shown labels to avoid saving emty stuff...
         for (int i = 0; i < currentlyShownActivityRowsNum && i < cache.labelsActArr.length; i++) {
             cache.labelsActArr[i] = jLabelsActvArr[i].getText();
             cache.labelsActIdArr[i] = jLabelsActvArr[i].getId();
         }
         cache.currentTextPaneFontSize = currentTextPaneFontSizeGlobal;
 
         cache.setAppDimensions( jFrame1.getSize() ); //last frame size
         
         cache.setScreenDimensions(dimPant);
         
         cache.setAppMainScreenLocation( jFrame1.getLocationOnScreen() );
         
         cache.setUsingPassword( cache.isUsingPassword() );
 
         serilizeCache();
     }
     
     /**
      * Method that uses catched object (previously unresializaed) to restore some settings.
      */
     private void restoreCache(){
         //font size
         currentTextPaneFontSizeGlobal = cache.currentTextPaneFontSize;
 		currentlyShownActivityRowsNum = ACTV_ROW_NUM_MIN;
 		
         //actv labels
         for (int i = 0; i < cache.labelsActArr.length; i++) {
         	if( cache.labelsActArr[i] == null ){
         		break;
         	}
         	
             jLabelsActvArr[i].setText( cache.labelsActArr[i] );
             jLabelsActvArr[i].setId( cache.labelsActIdArr[i] );
             currentlyShownActivityRowsNum++;
         }
         
         //If same size screen as cached, restore to last location
         if( dimPant.equals( cache.getScreenDimensions() )  ){
         	java.awt.Point appMainScreenLocation = cache.getAppMainScreenLocation();
         	if( !(new java.awt.Point()).equals( appMainScreenLocation ) ){
 	        	jFrame1.setLocation( appMainScreenLocation );
         	}
     	}
     	
     }
 
     /**
      * Serializes cache object.
      */
     private void serilizeCache(){
         jmydays.util.Serializer.serialize(cache, CONFIGS_FILENAME);
     }
 
     private void unSerilizeCache(){
         cache = (JMyDaysCache)jmydays.util.Serializer.deserialize(CONFIGS_FILENAME);
         if( cache == null ){
             cache = new JMyDaysCache();
         }
     }
 
     private boolean aLabelHasText(JLabel[] jLabels){
         for (int i = 0; i < jLabels.length; i++) {
             if( jLabels[i].getText() != null && jLabels[i].getText().trim().length() > 0){
                 return true;
             }
         }
         return false;
     }
 
     private void registerDaysActivityIds(final String actv_ids, final String textPaneName){
         
         if( actv_ids == null || actv_ids.equals("") ){
             return;
         }
 
         final int actvIdArrPos = Integer.parseInt( textPaneName )+ 1; //While minimum value is -1. (while ACTV_DISP_DAYS = 3 ).
 
         final String actv_idsArr[] = actv_ids.split(ACTV_VAL_DELIM_STR_REGEX);
         for (int i = 0; i < actv_idsArr.length; i++) {
             allActvIds[actvIdArrPos][i] = Integer.parseInt( actv_idsArr[i] );
         }
     }
 
     /**
      * Method that updates screen labels, and ids for labels.
      */
     private boolean updateScreenTodaysLabelsAndIds(final String id_actv, final int daysShownActivityRowsNum){
 
         //Update viewable labels
         showActvRows(daysShownActivityRowsNum);
 
         if( id_actv != null && !id_actv.equals("") ){
 
             //Fix: Case only ONE (1) activity label.
             if( id_actv.indexOf(ACTV_VALUE_DELIM_STR) == -1 ){
                try{
                     jLabelsActvArr[0].setText( getFromDBActvLabelById( Integer.parseInt( id_actv ) ) );
                     jLabelsActvArr[0].setId( Integer.parseInt( id_actv ) ); //Activity ID
                }catch(NumberFormatException nfe){
                    jLabelsActvArr[0].setText( getFromDBActvLabelById( 0 ) );
                    jLabelsActvArr[0].setId( 0 );
                    return false;
                }
             }
 
             //Check if id_actv hasn't changed...
             if( id_actv.equals( getScreenActvCatIds(daysShownActivityRowsNum) ) && aLabelHasText(jLabelsActvArr) ){
                 return true;
             }
 
             final String[] id_actv_ValsArr = id_actv.split(ACTV_VAL_DELIM_STR_REGEX);
             for (int i = 0; i < id_actv_ValsArr.length; i++) {
                 jLabelsActvArr[i].setText( getFromDBActvLabelById( Integer.parseInt(id_actv_ValsArr[i]) ) );
                 jLabelsActvArr[i].setId( Integer.parseInt(id_actv_ValsArr[i]) ); //Activity ID
             }
 
         }
         //Estan ocultas...
 //        else{
 //            for (int i = 0; i < jLabelsActArr.length; i++) {
 //                jLabelsActArr[i].setText("");
 //                jLabelsActArr[i].setId(-1);
 //            }
 //            return false;
 //        }
 
         return true;
     }
 
     private void updateScreenTxtFldsStat(final String txtFldValsStr, final MyJTextPane myJTextPane){
         final int daysToRooll = Integer.parseInt( myJTextPane.getName() );
         final MyJTextField[] txtFlds = getTextFieldArrayByDay(daysToRooll);
 
         if( txtFldValsStr != null){
             final String[] txtFldsValArr = txtFldValsStr.split(ACTV_VAL_DELIM_STR_REGEX);
             
             //SUPER BUG!! (STR.SPLIT() ALWAYS RETURNS AT LEAST 1!, even for str="")
             if( !"".equals(txtFldValsStr) ){
             	myJTextPane.setNumOfActivityRows(txtFldsValArr.length);
             }
 
             for (int i = 0; i < txtFldsValArr.length; i++) {
                 txtFlds[i].setText( txtFldsValArr[i].equals(ACTV_TXTFLD_UNINIT_VAL) ? ACTV_TXTFLD_EMPTY_VAL : txtFldsValArr[i] );
             }
 
 
 //            //TEMP FiX 1: To set empty values in components that shouldn't be shown...
 //            if( txtFldsValArr.length < ACTV_ROWS_NUM_MAX){
 //                for (int i = txtFldsValArr.length; i < ACTV_ROWS_NUM_MAX; i++) {
 //                    txtFlds[i].setText( ACTV_TXTFLD_EMPTY_VAL );
 //                }
 //            }
 
         }else{
             if( txtFlds.length > 1 ){
                 for (int i = 0; i < txtFlds.length; i++) {
                     txtFlds[i].setText( ACTV_TXTFLD_EMPTY_VAL );
                 }
             }
         }
     }
 /*
   	//Never acutally used
     private void updateScreenCheckBoxesStat(final String cbsStat, final MyJTextPane myJTextPane){
         
         final int daysToRooll = Integer.parseInt( myJTextPane.getName() );
         final JCheckBox[] jcbs = getCheckBoxArrayByDay(daysToRooll);
 
         if( cbsStat != null){
             final String[] cbsStatArr = cbsStat.split(ACTV_VAL_DELIM_STR_REGEX);
 
             myJTextPane.setNumOfActivityRows(cbsStatArr.length);
 
             for (int i = 0; i < cbsStatArr.length; i++) {
                 jcbs[i].setSelected( cbsStatArr[i].equals(ACTV_CB_UNCHECKED_VAL) ? false : true );
             }
         }else{
             //Pon checkboxes vacias
             if( jcbs.length > 1 ){
                 for (int i = 0; i < jcbs.length; i++) {
                     jcbs[i].setSelected( false );
                 }
             }
         }
 
     }
 */
 
     private void updateScreenDayInLabel(final JLabel jLabel, final Date date){
         jLabel.setText( todayFormat.format(date)  );
     }
 
     private void updateScreenMonthInLabel(final JLabel jLabel, final Date date){
         jLabel.setText( monthFormat.format(date)  );
     }
 
     private void updateScreenDaysInLabels(final Date date){
         updateScreenDayInLabel(jLblToday, date);
 
         calend.setTime(date);
         calend.roll(Calendar.DAY_OF_YEAR, 1);
         //Special case if has been rolled to first day of year... also roll year
         if( calend.get(Calendar.DAY_OF_YEAR) == calend.getActualMinimum(Calendar.DAY_OF_YEAR) ){
         	calend.roll(Calendar.YEAR, 1); //Forward one year
         }
         updateScreenDayInLabel(jLblPostDay1, calend.getTime());
 
         //Special case if has been rolled to first day of year... also roll year (now back)
         if( calend.get(Calendar.DAY_OF_YEAR) == calend.getActualMinimum(Calendar.DAY_OF_YEAR) ){
         	calend.roll(Calendar.YEAR, -1); //Back one year
         }
         calend.roll(Calendar.DAY_OF_YEAR, -2);
         updateScreenDayInLabel(jLblPrevDay1, calend.getTime());
     }
 
     /**
      * Method that obtains the IDs of the Activity Labels being displayed at the moment and puts them into a String.
      * @return cbsIds : String containing CURRENT (for this date) Label ids (e.g.: "1|2|3|4|5|6|7|8|9" )
      */
     private String getScreenActvCatIds(final int daysShownActivityRowsNum){
         final MyActvJLabel[] jLabels = this.jLabelsActvArr;
         String acvtIds = "";
 
         StringBuilder  strbuf = new StringBuilder();
         for (int i = 0; i < daysShownActivityRowsNum; i++) {
         	//acvtIds += jLabels[i].getId() + ACTV_VALUE_DELIM_STR;
         	strbuf.append(jLabels[i].getId());
         	strbuf.append(ACTV_VALUE_DELIM_STR);
         }
         acvtIds = strbuf.toString();
 
         if( daysShownActivityRowsNum > 0){
             acvtIds = acvtIds.substring(0, acvtIds.length()-1); //Quitamos ultimo PIPE ("|").
         }
 
         return acvtIds;
     }
 
     private String getScreenTxtFldsValues(final int daysToRoll, final int daysShownActivityRowsNum){
         final MyJTextField[] jTxtsFlds = getTextFieldArrayByDay(daysToRoll);
         String txtFldVals = "";
 
         for (int i = 0; i < daysShownActivityRowsNum; i++) {
             final String txtVal = jTxtsFlds[i].getText();
             txtFldVals += ( (txtVal != null && !txtVal.equals(ACTV_TXTFLD_UNINIT_VAL) ) ? txtVal : ACTV_TXTFLD_EMPTY_VAL ) + ACTV_VALUE_DELIM_STR;
         }
 
         if( daysShownActivityRowsNum > 0){
             txtFldVals = txtFldVals.substring(0, txtFldVals.length()-1); //Quitamos ultimo PIPE ("|").
         }
 
         return txtFldVals;
     }
 
     private MyJTextField[] getTextFieldArrayByDay(final int day){
         MyJTextField[] jTxtsFlds = {};
         switch(day){
             case ROLL_1_DAY_BACK:
                 jTxtsFlds = jTextFieldsCol1;
             break;
 
             case DAYS_TO_ROLL_TODAY:
                 jTxtsFlds = jTextFieldsCol2;
             break;
 
             case ROLL_1_DAY_FRWR:
                 jTxtsFlds = jTextFieldsCol3;
             break;
         }
         return jTxtsFlds;
     }
 
     private MyJSlider[] getSliderArrayByDay(final int day){
         MyJSlider[] jSliders = {};
         switch(day){
             case ROLL_1_DAY_BACK:
                 jSliders = jSlidersCol1;
             break;
 
             case DAYS_TO_ROLL_TODAY:
                 jSliders = jSlidersCol2;
             break;
 
             case ROLL_1_DAY_FRWR:
                 jSliders = jSlidersCol3;
             break;
         }
         return jSliders;
     }
 
     private JCheckBox[] getCheckBoxArrayByDay(final int day){
         JCheckBox[] jcbs = {};
         switch(day){
             case ROLL_1_DAY_BACK:
                 jcbs = jCheckBoxesCol1;
             break;
 
             case DAYS_TO_ROLL_TODAY:
                 jcbs = jCheckBoxesCol2;
             break;
 
             case ROLL_1_DAY_FRWR:
                 jcbs = jCheckBoxesCol3;
             break;
         }
         return jcbs;
     }
 
     /**
      * Inserts in DB the specified component's text, according to data provided by component and currecntly selected date.
      ** compDayRoll: Indica numero de dias separados de la fecha actual: Prop�sito, facilitar el calculo de la fecha del JTextPane que se envia.
      ** jTextPanes, arreglo, en caso de que la funcion reciba mas de uno.
      * @return
      */
     private boolean updateInDBDayEntry(final String compName){
         final Date middleDate = selectedDate;
         final int daysToRoll = Integer.parseInt( compName );
         final int selectedTextPane = todayTextPaneArrPos + daysToRoll;
 
         Calendar calendar = Calendar.getInstance(selectedLocale);
         calendar.setTime(middleDate);
         calendar.roll(Calendar.DAY_OF_YEAR, daysToRoll);
         
         //Special case if has been rolled to first day of year... also roll year
         if( calendar.get(Calendar.DAY_OF_YEAR) == calendar.getActualMinimum(Calendar.DAY_OF_YEAR) ){
         	calendar.roll(Calendar.YEAR, 1); //Forward one year
         }
         
         final String id_fecha = dbPKeyFormat.format( calendar.getTime() );  //(getDaysArrFromLenAndMidDay(numTextPanes))[selectedTextPane];
 
         final MyJTextPane myJTextPane = jTextPanesArr[selectedTextPane];
         int daysShownActivityRowsNum = myJTextPane.getNumOfActivityRows();
 
         //Fix: when the day's info is empty... use currents settings...
         if( daysShownActivityRowsNum == ACTV_ROW_NUM_INIT_VAL ){
             daysShownActivityRowsNum = currentlyShownActivityRowsNum;
         }
 
         try{
             dbConnection = Database.generaConexion();
 
             String query = "UPDATE registros SET cbs=?, id_cbs=?, texto=? WHERE id_fecha='" + id_fecha + "';";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.setString( 1, getScreenTxtFldsValues(daysToRoll, daysShownActivityRowsNum) );
             prepStatement.setString( 2, getScreenActvCatIds(daysShownActivityRowsNum) );
             if( isUsingPass ){
             	prepStatement.setString( 3, CryptoUtils.encrypt(myJTextPane.getText()) );
             }else{
             	prepStatement.setString( 3, myJTextPane.getText() );
             }
 
             prepStatement.executeUpdate();
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
             return false;
         }
 
         return true;
     }
     
     private boolean updateInDBPasswordSet(final boolean isUsingPass){
         
         try{
             dbConnection = Database.generaConexion();
 
             String query = "UPDATE parametros SET value='" + (isUsingPass ? DB_TRUE_CHAR : DB_FALSE_CHAR) + "' WHERE name='isUsingPass'; ";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.executeUpdate();
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
             if( sqle.getMessage().contains("no such table") ){
                 System.out.println("Creating DB table...");
                 createDBParamTable();
                 return updateInDBPasswordSet(isUsingPass);
             }
             return false;
         }
 
         return true;
     }
 
     private boolean updateInDBPasswordValue(final String password){
         
         try{
             dbConnection = Database.generaConexion();
 
             String query = "UPDATE parametros SET value=? WHERE name='passSha12'; ";
 
             prepStatement = dbConnection.prepareStatement(query);
             prepStatement.setString(1, password);
 
             prepStatement.executeUpdate();
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
             if( sqle.getMessage().contains("no such table") ){
                 System.out.println("Creating DB table...");
                 createDBParamTable();
                 return updateInDBPasswordValue(password);
             }
             return false;
         }
 
         return true;
     }
     
     /**
      * @param name String: The name of the value to get.
      * @return String with password sha1 value, null if not set.
      */
     private String getFromDBParameterByName(final String name){
         String value = null;
         try{
             dbConnection = Database.generaConexion();
 
             final String query = "SELECT value FROM parametros WHERE name=?; ";
 
             prepStatement = dbConnection.prepareStatement(query);
             prepStatement.setString(1, name);
             resultSet = prepStatement.executeQuery();
 
             if( resultSet.next() ){
             	value = resultSet.getString("value");
             }
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
             return null;
         }
 
         return value;
     }
     
     private void updateInDBActvLabelCompType(final int actvId, final int compType){
 
         if( actvId != ACTV_CAT_UNSPECIFIED_ID ){
             try{
                 dbConnection = Database.generaConexion();
                 prepStatement = dbConnection.prepareStatement("UPDATE actvs_cat SET compT=? WHERE id=?;");
                 prepStatement.setInt(1, compType);
                 prepStatement.setInt(2, actvId);
                 prepStatement.executeUpdate();
 
                 dbClosePrepStmtConn();
 
             }catch(java.sql.SQLException sqle){
                 System.err.println("java.sql.SQLException message:" + sqle.getMessage() );
             }
         }
 
     }
 
     /**
      * Method that inserts a new label in the DB and return the ID for the given Label.
      * @param labelText
      * @return labelId (0 if error).
      */
     private int updateInDBActvLabelDesc(final String labelText, final int compType){
 //        int labelId = ACTV_CAT_UNSPECIFIED_ID;
 
         //Check if it already exists in DB
 //        labelId = getFromDBActvLabelId(labelText); //necesary, since insert method checks first that it doesn't already exist.
 
         //Doesn't exist... insert it...
 //        if( labelId == ACTV_CAT_UNSPECIFIED_ID ){
 
             try{
                 dbConnection = Database.generaConexion();
                 prepStatement = dbConnection.prepareStatement("INSERT INTO actvs_cat(desc,compT) VALUES(?,?);");
                 prepStatement.setString(1, labelText);
                 prepStatement.setInt(2, compType);
                 prepStatement.executeUpdate();
                 
                 dbClosePrepStmtConn();
 
             }catch(java.sql.SQLException sqle){
 //                labelId = ACTV_CAT_UNSPECIFIED_ID;
                 System.err.println("java.sql.SQLException message:" + sqle.getMessage() );
                 return ACTV_CAT_UNSPECIFIED_ID;
             }
 
 //            labelId = getFromDBActvLabelId(labelText);
 //        }
 //        return labelId;
         return getFromDBActvLabelId(labelText);
     }
 
     private int getFromDBActvLabelId(final String labelText){
         int labelId = ACTV_CAT_UNSPECIFIED_ID;
 
         try{
             dbConnection = Database.generaConexion();
             prepStatement = dbConnection.prepareStatement("SELECT id FROM actvs_cat WHERE desc=?;");
             prepStatement.setString(1, labelText);
             resultSet = prepStatement.executeQuery();
 
             if( resultSet.next() ){
                 labelId = resultSet.getInt("id");
             }
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException message: " + sqle.getMessage());
             labelId = ACTV_CAT_UNSPECIFIED_ID;
         }
 
         return labelId;
     }
 
     private int getFromDBActvCompTypeById(final int actvId) {
         int compType = COMPONENT_TYPE_DEFAULT;
 
         try {
             dbConnection = Database.generaConexion();
             prepStatement = dbConnection.prepareStatement("SELECT compT FROM actvs_cat WHERE id=?;");
             prepStatement.setInt(1, actvId);
             resultSet = prepStatement.executeQuery();
 
             if (resultSet.next()) {
                 compType = resultSet.getInt("compT");
             }
 
             dbClosePrepStmtConn();
 
         } catch (java.sql.SQLException sqle) {
             System.err.println("java.sql.SQLException message: " + sqle.getMessage());
 //            compType = COMPONENT_TYPE_DEFAULT;
         }
 
         return compType;
     }
 
     private void setComponentTypeInLabels(){
         for(MyActvJLabel mAJL: jLabelsActvArr){
             mAJL.setCompType( getFromDBActvCompTypeById( mAJL.getId() ) );
         }
     }
 
     private void updateScreenTexFieldDate(Date date) {
         selectedDateTextField.setText(textFiledFormat.format(date));
     }
 
     private void updateScreenComponentTextSizeChoose(final java.awt.event.InputEvent iev, java.awt.Component comp[]) {
         if( iev instanceof KeyEvent ){
             
             switch( ((KeyEvent)iev).getKeyCode() ){
                 case KeyEvent.VK_PLUS:
                     updateScreenComponentTextSizeIncrease(+1, comp);
                 break;
 
                 case KeyEvent.VK_MINUS:
                     updateScreenComponentTextSizeIncrease(-1, comp);
                 break;
             }
 
         }else if( iev instanceof MouseWheelEvent ){
             updateScreenComponentTextSizeIncrease( ((MouseWheelEvent)iev).getWheelRotation()*(-1), comp ); //cast and use INVERTED wheel rotation method...            
         }
 
     }
 
     private void updateScreenSetComponentTextSize(int fontSize, java.awt.Component comps[]) {
         if( comps.length > 0){
             
             java.awt.Font currentFont = comps[0].getFont();
             java.awt.Font newFont = null;
             newFont = new java.awt.Font(currentFont.getName(),  currentFont.getStyle(), fontSize);
             for(java.awt.Component theComponent: comps){
                 theComponent.setFont( newFont );
             }
 
         }
     }
 
     private void updateScreenComponentTextSizeIncrease(int increase, java.awt.Component comp[]) {
 
         if( comp.length > 0 ){
 
             java.awt.Font currentFont = comp[0].getFont();
 //            int currentFontSize = currentFont.getSize();
             java.awt.Font newFont = null;
 
             if( increase > 0 ){
 //                currentFontSize++;
                 currentTextPaneFontSizeGlobal++;
 //            }else if( currentFontSize > 1 ){
             }else if( currentTextPaneFontSizeGlobal > 1 ){
 //                currentFontSize--;
                 currentTextPaneFontSizeGlobal--;
             } else {
                 return;
             }
 
 
 //            newFont = currentFont.deriveFont( currentFontSize ); //not function right?? =(
             newFont = new java.awt.Font(currentFont.getName(),  currentFont.getStyle(), currentTextPaneFontSizeGlobal);
             for(java.awt.Component theComponent: comp){
                 theComponent.setFont( newFont );
             }
         }
     }
 
 
     /**
      * Method that creates an array of dates with a given format.
      * @param arrayLength
      * @param middleDate (1 for 3, 2 for 5... Math.floor()... )
      * @param dateFormat
      * @return Array of string dates, e.g.: {"2009-01-01", "2009-01-02", "2009-01-03"}
      */
     private String[] getDaysArrFromLenAndMidDay(final int arrayLength, final Date middleDate, final SimpleDateFormat dateFormat){
         final String[] fechasStrArr = new String[arrayLength];
 
         //Fechas, calendario...
         Calendar calendar = Calendar.getInstance(selectedLocale);
         calendar.setTime(middleDate);
 
         //Tomamos dia inicial (el dia recibido debe ser el dia medio).
         calendar.roll( Calendar.DAY_OF_YEAR, - ((int)(java.lang.Math.floor((arrayLength/2D)))) );
 
         for (int i = 0; i < arrayLength; i++) {
             fechasStrArr[i] = dateFormat.format( calendar.getTime() );
 	        
             calendar.roll(Calendar.DAY_OF_YEAR, 1); //Forward one day
             //Special case if has been rolled to first day of year... also roll year
             if( calendar.get(Calendar.DAY_OF_YEAR) == calendar.getActualMinimum(Calendar.DAY_OF_YEAR) ){
             	calendar.roll(Calendar.YEAR, 1); //Forward one year
             }
         }
 
         return fechasStrArr;
     }
 
 
     /**
      * Method that tries to obtain 3 days from the database, if one is missing it calls a method to inicialize that entry.
      * It also calls the methods to display checkbox contents, labels (only for middle day) and texpane contents.
      * @param updateDate : Date object for the middle day.
      * @param myJTextPane : Var-arg with the JTextPanes used to know how many days to consult from DB.
      * @return
      */
     private boolean updateFromDBAllByTextPane(Date updateDate, MyJTextPane[] myJTextPane){
         final int numTextPanes = myJTextPane.length;
         final String selectedDatePKFrmt = dbPKeyFormat.format(updateDate);
         RegistrosBean regsB;
         java.util.Map<String, RegistrosBean> dbResMap = new java.util.HashMap<String, RegistrosBean>();
 
         final String[] fechasStrArr = getDaysArrFromLenAndMidDay(numTextPanes, updateDate, dbPKeyFormat);
 
         try{
             dbConnection = Database.generaConexion();
 
             String query = "SELECT id_fecha, cbs, id_cbs, texto FROM registros " +
                            "WHERE id_fecha>='" + fechasStrArr[0] + "' AND id_fecha<='" + fechasStrArr[fechasStrArr.length-1] + "'; ";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             resultSet = prepStatement.executeQuery();
 
             while( resultSet.next() ){
                 regsB = new RegistrosBean();
                 regsB.setId_fecha( resultSet.getString("id_fecha") );
                 regsB.setCbs( resultSet.getString("cbs") );
                 regsB.setId_cbs( resultSet.getString("id_cbs") );
 
                 String texto = resultSet.getString("texto");
                 if( texto != null ){
                 
                 	if( CryptoUtils.isStrCiphered(texto) && isUsingPass ){ 
                 		regsB.setDesc( CryptoUtils.decrypt( texto ) );
                 	}else{
                 		regsB.setDesc( texto );
                 	}
                 	
                 }
                 
                 dbResMap.put(regsB.getId_fecha(), regsB);
             }
 
             dbCloseResPrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
             dbCloseResPrepStmtConn();
 
             if( sqle.getMessage().contains("no such table") ){
                 System.out.println("Creating DB tables...");
                 createDBRegTable();
                 createDBActvTable();
                 createDBParamTable();
 
                 //Fix: to have existing regs
                 for (int i = 0; i < fechasStrArr.length; i++) {
                     initDBEntryByDay( fechasStrArr[i] );
                 }
             }
 
             return false;
             
         }catch(NullPointerException npe){
             System.err.println("Problem with database file.");
             System.exit(-1); //Error
         }
 
 
         if( dbResMap.size() != numTextPanes ){
             System.out.println("WARN: No corresponden resultados DB con DIA!"); //Resultado DB incompleto o mayor
         }
 
         for (int i = 0; i < numTextPanes; i++) {
 
             //Checa si el reg de la fecha corresponde con el d�a
             regsB = dbResMap.get( fechasStrArr[i] );
             if( regsB != null ){
                 myJTextPane[i].setText( regsB.getDesc() );
 
                 updateScreenTxtFldsStat( regsB.getCbs(), myJTextPane[i] );
 
                 //Set Activity Labels for TODAYs Date...
                 if( fechasStrArr[i].equals( selectedDatePKFrmt ) ){
                     updateScreenTodaysLabelsAndIds( regsB.getId_cbs(), myJTextPane[i].getNumOfActivityRows() );
                 }else{
                     //Register the days activity ids...
                     registerDaysActivityIds( regsB.getId_cbs(), myJTextPane[i].getName() );
                 }
 
             }else{
                 //DB Inicializa con Key y vacio... fecha NO encontrada en mapa.
                 //TODO: si pasa esto, hay que actualizar los componentes en a INIT_VALs (menos ACTV_IDs del d�a)
                 initDBEntryByDay( fechasStrArr[i] );
                 myJTextPane[i].setText("");
                 
                 if( currentlyShownActivityRowsNum > 0 ){
 	                final int daysToRoll = Integer.parseInt( myJTextPane[i].getName() );
 	                final MyJTextField[] txtFlds = getTextFieldArrayByDay(daysToRoll);
 	                for(int j = 0; j < currentlyShownActivityRowsNum; j++){
 	                	txtFlds[j].setText("0"); //set init vals.
 	                }
                 }
                 
             }
 
         }
 
         return true;
     }
 
     private void createDBRegTable(){
 
         try{
             dbConnection = Database.generaConexion();
 
             String query = "CREATE TABLE 'registros' ( " +
                                "'id_fecha' VARCHAR(12) NOT NULL PRIMARY KEY, " +
                                "'cbs' VARCHAR(20), " +
                                "'id_cbs' VARCHAR(20), " +
                                "'texto' TEXT" +
                            ");";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.execute();
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
         }
 
     }
 
     private void createDBActvTable(){
 
         try{
             dbConnection = Database.generaConexion();
 
             String query = "CREATE TABLE 'actvs_cat'( " +
                                "'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "'desc' VARCHAR(35) NOT NULL, " +
                                "'compT' INTEGER DEFAULT 1, " +
                                "'stamp' TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP " +
                            ");";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.execute();
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
         }
 
     }
     
     private void createDBParamTable(){
 
         try{
             dbConnection = Database.generaConexion();
 
             String query = "CREATE TABLE 'parametros' ( " +
                                "'name' VARCHAR(30) NOT NULL PRIMARY KEY, " +
                                "'value' TEXT NULL" +
                            ");";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.execute();
 
             dbClosePrepStmtConn();
             
             // -------------------------------
             dbConnection = Database.generaConexion();
 
             query = "INSERT INTO parametros(name, value) VALUES('isUsingPass', 'N');";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.execute();
 
             dbClosePrepStmtConn();
             // -------------------------------
             dbConnection = Database.generaConexion();
 
             query = "INSERT INTO parametros(name) VALUES('passSha12');";
 
             prepStatement = dbConnection.prepareStatement(query);
 
             prepStatement.execute();
 
             dbClosePrepStmtConn();
             
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle.getMessage() );
         }
 
     }
 
     /**
      * Inserts Date into corresponding DB day.
      * @param dbPK
      * @return
      */
     private boolean initDBEntryByDay(final String dbPK){
 
         try{
             dbConnection = Database.generaConexion();
 
             prepStatement = dbConnection.prepareStatement("INSERT INTO registros(id_fecha) VALUES('" + dbPK + "');");
             prepStatement.executeUpdate();
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle);
         }
 
         return true;
     }
 
     private String getFromDBActvLabelById(int labelId){
         //TEMP msg
         String labelStr = "Activ. #" + labelId;
 
         //Temp solution
         if( labelId == ACTV_CAT_UNSPECIFIED_ID ){
             return recursosTexto.getString("unspecifiedLabel");
         }
 
         try{
             dbConnection = Database.generaConexion();
 
             prepStatement = dbConnection.prepareStatement("SELECT desc FROM actvs_cat WHERE id=?;");
             prepStatement.setInt(1, labelId);
             resultSet = prepStatement.executeQuery();
 
             if( resultSet.next() ){
                 labelStr = resultSet.getString("desc");
             }
 
             dbClosePrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException Message: " + sqle);
         }
 
         return labelStr;
     }
 
     /**
      * Method that attempts to update all
      * @return reVal : true only if all updates where succesfull.
      */
     private boolean updateInDBAll(JTextPane[] jTextPanes){
         boolean retVal = true;
 
         for (int i = 0; i < jTextPanes.length; i++) {
             if( !updateInDBDayEntry( jTextPanes[i].getName() ) ){
                 retVal = false;
             }
         }
 
         return retVal;
     }
     /**
      * General screen update method. Takes a date and calls function to update the whole screen based on the date.
      * @param date
      */
     private void updateScreenAllByDate(Date date){
         selectedDate = date;
         updateScreenTexFieldDate( selectedDate );
         updateScreenDaysInLabels( selectedDate );
         updateScreenMonthInLabel(jLblMonth, selectedDate);
         
         if( updateFromDBAllByTextPane(selectedDate, jTextPanesArr) ){
             //Screen updates ONLY if succesfull DB retrieval...
             showActvRows(currentlyShownActivityRowsNum);
         }else{
             showActvRows(ACTV_ROW_NUM_MIN);
         }
 
         //clean typed History stacks
         typedHistoryCleanStacks();
         updateScreenActvLabelColors();
     }
 
     public void updateScreenActvLabelColors(){
     	final String txtFldsValues[] = getScreenTxtFldsValues(0, currentlyShownActivityRowsNum).split(ACTV_VAL_DELIM_STR_REGEX);
     	for(int i = 0; i < currentlyShownActivityRowsNum; i++ ){
     		if( Integer.parseInt(txtFldsValues[i]) > ACTV_SLIDER_MIN_VAL ){
     			jLabelsActvArr[i].setForeground(NON_ZERO_ADVANDE_COLOR);
     		}else{
     			jLabelsActvArr[i].setForeground(ZERO_ADVANDE_COLOR);
     		}
     	}
     }
     
     public void dbClosePrepStmtConn(){
         try{
             prepStatement.close();
             //dbConnection.close();
         }catch(Exception e){
             System.err.println("Exception when closing prepared statement: " + e.getMessage());
         }
     }
 
     public void dbCloseResPrepStmtConn(){
         try{
             resultSet.close();
             prepStatement.close();
             //dbConnection.close();
         }catch(Exception e){
             System.err.println("Exception when closing result set and prepared statement: " + e.getMessage());
         }
     }
 
     private void addEventListeners() throws Exception{
 
         //Keys and Focus
         final EventListeners evlsts = new EventListeners();
         final TextPaneEvents tpel = new TextPaneEvents();
         for (JTextPane jtp : jTextPanesArr) {
             jtp.addKeyListener(evlsts);
             jtp.addKeyListener(tpel);
             jtp.addMouseWheelListener(tpel);
             jtp.addFocusListener(evlsts);
         }
 
         jFrame1.addWindowListener(new WindowAdapter(){
             @Override
             public void windowClosing(WindowEvent e){
                 exitApplication();
             }
         });
 
         jDateBTN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jDateBTNActionPerformed(evt);
             }
         });
 
         rightBTN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rightBTNActionPerformed(evt);
             }
         });
 
         leftBTN.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 leftBTNActionPerformed(evt);
             }
         });
 
         jMenuItemF1.addActionListener( new java.awt.event.ActionListener(){
             public void actionPerformed(java.awt.event.ActionEvent e) {
 
                 String newActvLabelStr;
                 boolean isInvalidInput = false;
                 do{
                     newActvLabelStr = JOptionPane.showInternalInputDialog(jPanel1, recursosTexto.getString("msgInpNuActvLbl"), recursosTexto.getString("msgInpNuActvLblTitle"), JOptionPane.QUESTION_MESSAGE);
 
                     if( newActvLabelStr == null ){ //cancel button pressed.
                         return;
                     }else if( newActvLabelStr.trim().length() == 0 ){
                         JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrInpNuACtvLbl"), recursosTexto.getString("msgErrInpTitle1"), JOptionPane.ERROR_MESSAGE);
                         isInvalidInput = true;
                     }else{
                         isInvalidInput = false;
                     }
 
                 }while( isInvalidInput );
 
                 setNewTextToLastVisibleLabel(newActvLabelStr);
 
             }
         });
 
         jMenuItemF2.addActionListener( new java.awt.event.ActionListener(){
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 //TODO: Decrease number of exit points...
 
                 int numActvRowsToShowInt = ACTV_ROW_NUM_INIT_VAL;
                 String numLabelsToShowStr;
                 boolean isInvalidInput = false;
                 do{
                     isInvalidInput = false;
                     try{
                         numLabelsToShowStr = JOptionPane.showInternalInputDialog(jPanel1, recursosTexto.getString("msgInpNumActRows"), recursosTexto.getString("msgInpNumActRowsTitle"), JOptionPane.QUESTION_MESSAGE);
 
                         //Pressed CANCEL button.
                         if( numLabelsToShowStr == null ){
                             return;
                         }
 
                         numActvRowsToShowInt = Integer.parseInt( numLabelsToShowStr );
                     }catch(NumberFormatException nfe){
                         JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrInpNumActRows")+" "+ACTV_ROW_NUM_MAX, recursosTexto.getString("msgErrInpTitle1"), JOptionPane.ERROR_MESSAGE);
                         isInvalidInput = true;
                         numActvRowsToShowInt = ACTV_ROW_NUM_INIT_VAL;
                     }
 
                     if( numActvRowsToShowInt < ACTV_ROW_NUM_MIN || numActvRowsToShowInt > ACTV_ROW_NUM_MAX ){
                         JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrInpNumActRows")+" "+ACTV_ROW_NUM_MAX, recursosTexto.getString("msgErrInpTitle1"), JOptionPane.ERROR_MESSAGE);
                         isInvalidInput = true;
                     }else{
 
                         if( numActvRowsToShowInt > currentlyShownActivityRowsNum ){
                             JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrInpNumActRowsCurr"), recursosTexto.getString("msgErrInpTitle1"), JOptionPane.ERROR_MESSAGE);
                             return;
                         }
                         //else carry on validity
 
                     }
 
                 }while( isInvalidInput );
 
                 //Cero confirmation meesage
                 //OK -> 0, Cancel -> 2
                 if( numActvRowsToShowInt == ACTV_ROW_NUM_MIN ){
 
                     if( JOptionPane.showInternalConfirmDialog(jPanel1, recursosTexto.getString("msgConf0ActRows"),recursosTexto.getString("msgConf0ActRowsTitle") , JOptionPane.WARNING_MESSAGE) == 0 ){
                         showActvRows( numActvRowsToShowInt );
                     }else{
                         return;
                     }
 
                 }else{
                     showActvRows( numActvRowsToShowInt );
                 }
 
                 setTodaysActivityRowsNum(numActvRowsToShowInt);
             }
 
         });
 
         jMenuItemF4.addActionListener( new java.awt.event.ActionListener(){
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 Object objTemp[] = recursosConfig.getString("langsAvail").split(DELIM_LANGS);
 
                 //Just so that I can put a 'cancel' button in correct language
                 Object btnArr[] = {recursosTexto.getString("cancelBtn")};
                 Object langOpts[] = new Object[objTemp.length+btnArr.length];
                 System.arraycopy(objTemp, 0, langOpts, 0, objTemp.length);
                 System.arraycopy( btnArr, 0, langOpts, objTemp.length, btnArr.length);
 
                 String langOptsVals[] = recursosConfig.getString("langsAvailVals").split(DELIM_LANGS);
                 int opt = JOptionPane.showInternalOptionDialog( jPanel1,
                                                                 recursosTexto.getString("msgInpSelLang"),
                                                                 recursosTexto.getString("msgInpSelLangTitle"),
                                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null, // new javax.swing.ImageIcon(getClass().getResource("/nextmon.gif"), ">"),
                                                                 langOpts,
                                                                 langOpts[0]
                                                                );
 
                 if( opt == JOptionPane.YES_OPTION ){
                     cache.defSavedLang = langOptsVals[0];
                 }else if( opt == JOptionPane.NO_OPTION ){
                     cache.defSavedLang = langOptsVals[1];
                 }
 
                 if( opt != JOptionPane.CANCEL_OPTION ){
                     JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgInpSelLangNote"), recursosTexto.getString("msgInpSelLangNoteTitle"), JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         });
 
         jMenuItemF5.addActionListener( new java.awt.event.ActionListener(){
         	
             public void actionPerformed(java.awt.event.ActionEvent ae) {
                 JCheckBoxMenuItem eventSource = (JCheckBoxMenuItem)ae.getSource();
                 
                 final boolean isCheckBoxSet = eventSource.getState();
                 
     	    	updateInDBPasswordSet( isCheckBoxSet ); //State is true or false accordingly
     	    	
     	    	showPasswordValidationDialog(PASS_STATE_CHANGE);
         	    /*
         	    final String dbPass = getFromDBParameterByName("passSha12"); 
         	    if( dbPass == null || dbPass.length() == 0 ){
         	    	try{
         	    		
 	        			String inputPass = JOptionPane.showInternalInputDialog(jPanel1, recursosTexto.getString("msgPassInput"), recursosTexto.getString("msgPassInputTitle"), JOptionPane.INFORMATION_MESSAGE);
 	        			if( inputPass != null && inputPass.trim().length() >= PASS_LENGTH_MIN ){
 	        				//CryptoUtils.SHA1( CryptoUtils.SHA1(inputPass) );
 	        				CryptoUtils.setCurrentPassword(inputPass);
 	        				updateInDBPasswordValue( CryptoUtils.SHA1( CryptoUtils.SHA1(inputPass) ) );
 	        			}
 	        			
         	    	}catch(Exception e){
         	    		System.err.println("Exception... " + e.getMessage());
         	    	}
         	    }
         	    */
     	    	
         	    if( isCheckBoxSet ){
         	    	updateScreenAllByDate(selectedDate);
         	    }
         	    
         	    //isUsingPass = isCheckBoxSet; //update var according to checkbox state
     	    
             }
         });
 
         jMenuItemT1.addActionListener( new java.awt.event.ActionListener(){
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 showExportRegistersToTextFiles(jPanel1);
             }
         });
 
         jMenuItemT2.addActionListener( new java.awt.event.ActionListener(){
         	public void actionPerformed(java.awt.event.ActionEvent e) {
         		showExistingAllLabels(jPanel1);
         	}
         });
 
         jMenuItemFExit.addActionListener( new java.awt.event.ActionListener(){
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 jMenuItemFExitAction(e);
             }
         });
 
         jMenuItemA1.addActionListener( new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 showAboutDialog(jPanel1);
             }
         });
 
         jPanel1.addMouseListener(new MouseAdapter(){
             @Override
             public void mouseClicked(MouseEvent e){
                 jPanel1.requestFocus();
                 super.mouseClicked(e);
             }
         });
 
     //Listeners (Key, event)
         //Key
         final JPanelEvents jpke = new JPanelEvents();
         jPanel1.addKeyListener(jpke);
         leftBTN.addKeyListener(jpke);
         rightBTN.addKeyListener(jpke);
 
         //Listener (Checkboxes, Selected).
         addCheckBoxesListeners(evlsts);
 
         final MouseListener mlev = new LabelEvents();
         addEventListenerToComponents(jLabelsActvArr, mlev);
 
         final java.util.EventListener actvTxtFldLblLi = new TextFieldEvents();
         addEventListenerToComponents(jTextFieldsArr, actvTxtFldLblLi);
 
         //Listeners to Sync values of Activity components with Textfields
         final java.util.EventListener cBsAndSlidersLi = new ActvCompValueCopyEvents();
         addEventListenerToComponents(jCheckBoxesCol1, cBsAndSlidersLi);
         addEventListenerToComponents(jCheckBoxesCol2, cBsAndSlidersLi);
         addEventListenerToComponents(jCheckBoxesCol3, cBsAndSlidersLi);
         addEventListenerToComponents(jSlidersCol1, cBsAndSlidersLi);
         addEventListenerToComponents(jSlidersCol2, cBsAndSlidersLi);
         addEventListenerToComponents(jSlidersCol3, cBsAndSlidersLi);
         
         final java.util.EventListener txtsAndCBsAndSlidersLi = new ActvCompActionEvents();
         addEventListenerToComponents(jCheckBoxesCol1, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jCheckBoxesCol2, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jCheckBoxesCol3, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jSlidersCol1, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jSlidersCol2, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jSlidersCol3, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jTextFieldsCol1, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jTextFieldsCol2, txtsAndCBsAndSlidersLi);
         addEventListenerToComponents(jTextFieldsCol3, txtsAndCBsAndSlidersLi);
     }
 
     private void addEventListenerToComponents(Component[] comp, java.util.EventListener evLi) throws Exception{
         if (evLi instanceof java.awt.event.MouseListener ){
             for (int i = 0; i < comp.length; i++) {
                 comp[i].addMouseListener( (MouseListener)evLi );
             }
             // return; //to add a single listener? ...
         }else if( evLi instanceof java.awt.event.KeyListener ){
             for (int i = 0; i < comp.length; i++) {
                 comp[i].addKeyListener( (KeyListener)evLi );
             }
         }else if( evLi instanceof java.awt.event.FocusListener ){
             for (int i = 0; i < comp.length; i++) {
                 comp[i].addFocusListener( (FocusListener)evLi );
             }
         }else{
         	throw new Exception("Unsoported EventListener instance passed.");
         }
 
     }
 
     private boolean addCheckBoxesListeners(java.awt.event.FocusListener cbFocusListener){
         if( jCheckBoxesCol1.length == jCheckBoxesCol2.length && jCheckBoxesCol1.length == jCheckBoxesCol3.length ){
             for (int i = 0; i < jCheckBoxesCol1.length; i++) {
                 jCheckBoxesCol1[i].setName(DAYS_TO_ROLL_PREV1_STR);
                 jCheckBoxesCol1[i].addFocusListener(cbFocusListener);
 
                 jCheckBoxesCol2[i].setName(DAYS_TO_ROLL_TODAY_STR);
                 jCheckBoxesCol2[i].addFocusListener(cbFocusListener);
 
                 jCheckBoxesCol3[i].setName(DAYS_TO_ROLL_FRWR1_STR);
                 jCheckBoxesCol3[i].addFocusListener(cbFocusListener);
             }
             return true;
         }else{
             return false;
         }
     }
 
     /* ### Acciones ### */
     private void jDateBTNActionPerformed(java.awt.event.ActionEvent evt) {
         // instantiate the DatePicker
         dp = new DatePicker(selectedDateTextField, selectedLocale);
 
         // intially selected date
         Date selected = dp.parseDate(selectedDateTextField.getText());
         dp.setSelectedDate(selected);
         dp.start(selectedDateTextField);
     }
 
     private void rightBTNActionPerformed(java.awt.event.ActionEvent evt) {
         //Actualiza fecha, avanzar un dia.
         rotarFecha(true, Calendar.DAY_OF_YEAR);
     }
 
     private void leftBTNActionPerformed(java.awt.event.ActionEvent evt) {
         //Actualiza fecha, regresar un dia.
         rotarFecha(false, Calendar.DAY_OF_YEAR);
     }
 
     private void jMenuItemFExitAction(java.awt.event.ActionEvent aevt) {
         exitApplication();
     }
 
     /**
      * Method execute when the application is closed.
      */
     private void exitApplication(){
 
         try{
             updateScreenActvCopyAllComponentValsToTxtFlds();
             updateInDBAll(jTextPanesArr);
             serilizeTodaysCache();
         }catch(java.lang.Exception e){
             System.err.println("Error in Exit Application: java.lang.Exception Message:" + e.getMessage());
             e.printStackTrace();
         }
         
         try{
         	dbConnection.close();
         }catch(java.sql.SQLException sqle){
         	System.err.println("Problem closing DB Connection!");
         }
         
         System.out.println("Bye!");
         System.exit(0);
     }
 
     /**
      * Method that checks for a given label ID if it is already being displayed in the current shown labels.
      * @param actvId
      * @return
      */
     private boolean isActvIdRepeatToday(final int actvId){
         for (int i = 0; i < currentlyShownActivityRowsNum; i++) {
             //Checking only TODAY's ( allActvIds[1][*] )actv. ids. (turns out it's UNUPDATED!!!!)
 //            if( actvId == allActvIds[1][i] ){
             if( actvId == jLabelsActvArr[i].getId() ){
                 return true;
             }
         }
         return false;
     }
 
     private void setTodaysActivityRowsNum(final int daysShownActivityRowsNum){
         final MyJTextPane myJTextPane = jTextPanesArr[todayTextPaneArrPos];
         myJTextPane.setNumOfActivityRows( daysShownActivityRowsNum );
 
         //Temp line: verify if required...
 //        updateInDBDayEntry( myJTextPane.getName() );
     }
 
     /**
      * Register String in stack if typed chars.
      */
     private void typedHistoryRegister(final String componentName, final String currentStr){
 
         java.util.Stack<String> typedHistoryUndoStack = typedHistoryByPanelsMap.get(componentName);
         if( typedHistoryUndoStack == null ){
             typedHistoryUndoStack = new java.util.Stack<String>();
             typedHistoryUndoStack.add(currentStr);
         }else{
             typedHistoryUndoStack.add(currentStr);
             if( typedHistoryUndoStack.size() >= TYPED_HISTORY_REG_NUM_MAX){
                 typedHistoryUndoStack.removeElementAt(0);
             }
         }
         typedHistoryByPanelsMap.put(componentName, typedHistoryUndoStack);
 
         //reset char Count
         //typedCharsCount = 0;
     }
 
     /**
      * Goes back or advances through history, registers current string for redo.
      * @param currentStr : last string before undo or redo.
      * undo (true=undo, false=redo)
      */
     private String typedHistoryChange(final String componentName, final String currentStr, final boolean undoCommand){
         String popedStr = currentStr; //just for more clarity
 
         java.util.Stack<String> typedHistoryUndoStack = typedHistoryByPanelsMap.get(componentName);
         java.util.Stack<String> typedHistoryRedoStack = typedHistoryByPanelsMap.get(componentName+TYPED_HISTORY_REDO_POSTFIX);
         if( typedHistoryUndoStack == null || typedHistoryRedoStack == null ){
             return currentStr;
         }
 
         //operate stacks according to action
         if( undoCommand ) {
 
             if( !typedHistoryUndoStack.empty() ){
                     typedHistoryRedoStack.add(currentStr);
                     popedStr = typedHistoryUndoStack.pop();
 
                     //avoid stack overflow
                     if( typedHistoryRedoStack.size() >= TYPED_HISTORY_REG_NUM_MAX){
                         typedHistoryRedoStack.removeElementAt(0);
                     }
             }
             
         }else if( !typedHistoryRedoStack.empty() ){
 
             typedHistoryUndoStack.add(currentStr);
             popedStr = typedHistoryRedoStack.pop();
 
             //avoid stack overflow
             if( typedHistoryUndoStack.size() >= TYPED_HISTORY_REG_NUM_MAX){
                 typedHistoryUndoStack.removeElementAt(0);
             }
             
         }
 
         return popedStr;
     }
 
     /**
      * Clean stacks in typed history maps.
      * To remove previous panel's text data.
      */
     private void typedHistoryCleanStacks(){
 
         for( java.util.Stack<String> typedHistoryXStack : typedHistoryByPanelsMap.values() ){
              if( typedHistoryXStack != null && !typedHistoryXStack.empty() ){
                  typedHistoryXStack.clear();
              }
         }
 
     }
 
     /**
      * If currentlyShownActivityRowsNum < ACTV_ROWS_NUM_MAX, create new label after last, else replace last label with new value.
      */
     private void setNewTextToLastVisibleLabel(final String newActvLabelStr) {
         int actvLabelId = getFromDBActvLabelId(newActvLabelStr);
         if( isActvIdRepeatToday(actvLabelId) ){
             JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrIsRepActvLbl"), recursosTexto.getString("msgErrIsRepActvLblT"), JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         MyActvJLabel myjl;
         if( currentlyShownActivityRowsNum < ACTV_ROW_NUM_MAX ){
             myjl = jLabelsActvArr[ currentlyShownActivityRowsNum ];
         }else{
             //TODO: show dialog to confirm replace last label...
             if( JOptionPane.showInternalConfirmDialog(jPanel1, recursosTexto.getString("msgConfActRowReplace"),recursosTexto.getString("msgConfActRowReplaceT") , JOptionPane.WARNING_MESSAGE) == 0 ){
                 myjl = jLabelsActvArr[ currentlyShownActivityRowsNum-1 ];
             }else{
                 return;
             }
         }
 
         if ( actvLabelId == ACTV_CAT_UNSPECIFIED_ID ){
             updateScreenResetRowValues(currentlyShownActivityRowsNum); //Rset values of default component ONLY if NEW label, else use DBs value...
             actvLabelId = updateInDBActvLabelDesc(newActvLabelStr, COMPONENT_TYPE_DEFAULT);
         }
         
         myjl.setId( actvLabelId );
         myjl.setText(newActvLabelStr);
 
         if (currentlyShownActivityRowsNum < ACTV_ROW_NUM_MAX) {
             showActvRows( currentlyShownActivityRowsNum+1 );
 
             //Increment value of show
             setTodaysActivityRowsNum( currentlyShownActivityRowsNum );
         }
 
         //update probable missmatch
         //updateScreenActvIdDayMissMatch();
 		//updateScreenNormalize();
 
         //Fix to save label changes to DB
         updateInDBDayEntry( DAYS_TO_ROLL_TODAY_STR );
     }
 
     /**
      * Method that 'rolls' (advances or reverses).
      * @param avanzar
      * @param field
      */
     private void rotarFecha(final boolean avanzar, final int field){
 
         //Fix: missing save tracking changes.
         updateInDBAll(jTextPanesArr);
         
         calend.setTime(selectedDate);
         //If the date is max or min, change year
         if( avanzar ){
 	        if( calend.get(field) == calend.getActualMaximum(field) ){
 	        	calend.roll(Calendar.YEAR, ROLL_1_DAY_FRWR);
 	        }
 	        calend.roll(field, ROLL_1_DAY_FRWR);
         }else{
         	if( calend.get(field) == calend.getActualMinimum(field) ){
         		calend.roll(Calendar.YEAR, ROLL_1_DAY_BACK);
         	}
         	calend.roll(field, ROLL_1_DAY_BACK);
         }
 
         updateScreenAllByDate( calend.getTime() );
     }
 
     /**
      * Method to forward or reverse the focus among components.
      * The focus cicles on jTextPanesArr elements.
      * @param avanzarFoco : Indicates where or not to move focus forward.
      * @param comp The Component that has the focus.
      */
     private void rotarFocus(final boolean avanzarFoco, final java.awt.Component comp){
         if( avanzarFoco ){
             if(  jTextPanesArr[jTextPanesArr.length-1].hasFocus() ){
                 jTextPanesArr[0].requestFocus();
             }else{
                 comp.transferFocus();
             }
         }else{
             if( jTextPanesArr[0].hasFocus() ){
                 jTextPanesArr[jTextPanesArr.length-1].requestFocus();
             }else{
                 comp.transferFocusBackward();
             }
         }
     }
 
     /**
      * Method that hides one component and shows another, depending on component type.
      * @param comp
      */
     private void switchActvLabelTxtFieldVisibility(final Component comp, final boolean saveLabelChange){
         final boolean visible = true;
 
         if( comp instanceof JLabel ){
             JLabel jl = (JLabel)comp;
             jl.setVisible( !visible );
 
             final JTextField jtf = jTextFieldsArr[ Integer.parseInt(comp.getName()) ];
             jtf.setText( jl.getText() );
             jtf.setVisible( visible );
             jtf.requestFocus();
         }
 
         if( comp instanceof JTextField ){
             //If not visible, exit.
             if( !comp.isVisible() ) {
                 return;
             }
 
             final JTextField jtxtf = (JTextField)comp;
             jtxtf.setVisible(!visible);
 
             final MyActvJLabel myjl = jLabelsActvArr[Integer.parseInt(comp.getName())];
             myjl.setVisible(visible);
 
             //Set text
             if (saveLabelChange) {
                 final String jTextFieldTxt = jtxtf.getText();
                 if (jTextFieldTxt != null && !"".equals(jTextFieldTxt)) {
 
                     //check if text changed...
                     if (jTextFieldTxt.equals(myjl.getText())) {
                         return;
                     }
 
                     int actvLabelId = getFromDBActvLabelId(jTextFieldTxt);
                     if (isActvIdRepeatToday(actvLabelId)) {
                         JOptionPane.showInternalMessageDialog(jPanel1, recursosTexto.getString("msgErrIsRepActvLbl"), recursosTexto.getString("msgErrIsRepActvLblT"), JOptionPane.ERROR_MESSAGE);
                         return;
                     }
 
                     //First check if the label's text already exists in the DB.
                     if (actvLabelId == ACTV_CAT_UNSPECIFIED_ID) {
                         updateScreenResetRowValues(currentlyShownActivityRowsNum); //Reset values of default component ONLY if NEW label, else use DBs value...
                         actvLabelId = updateInDBActvLabelDesc(jTextFieldTxt, COMPONENT_TYPE_DEFAULT);
                     }
 
                     myjl.setId(actvLabelId);
                     myjl.setText(jTextFieldTxt);
 
                     //update probable missmatch
                     //updateScreenActvIdDayMissMatch();
 
                     //Fix to save label changes to DB
                     updateInDBDayEntry( DAYS_TO_ROLL_TODAY_STR );
 
                 }
             }
 
 
         }
 
     }
 
     private void keyEventProcess(final KeyEvent ke){
 
         if( ke.isShiftDown() ){
             final java.awt.Component comp = ke.getComponent();
             switch( ke.getKeyCode() ){
                 case KeyEvent.VK_RIGHT:
                     rotarFocus(true, comp);
                     break;
                 case KeyEvent.VK_LEFT:
                     rotarFocus(false, comp);
                     break;
             }
             return;
         }
 
         switch( ke.getKeyCode() ){
             case KeyEvent.VK_RIGHT:
                 rotarFecha(true, Calendar.DAY_OF_YEAR);
                 break;
             case KeyEvent.VK_LEFT:
                 rotarFecha(false, Calendar.DAY_OF_YEAR);
                 break;
             case KeyEvent.VK_PAGE_UP:
                 rotarFecha(true, Calendar.WEEK_OF_YEAR);
                 break;
             case KeyEvent.VK_PAGE_DOWN:
                 rotarFecha(false, Calendar.WEEK_OF_YEAR);
                 break;
             case KeyEvent.VK_HOME:
                 updateScreenAllByDate( new java.util.Date() );
                 break;
         }
     }
 
     private void addKeyboardShortCuts(final javax.swing.RootPaneContainer rootPaneContainer){
 
         final javax.swing.JRootPane rootPane = rootPaneContainer.getRootPane();
         final javax.swing.InputMap iMap = rootPane.getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
         final javax.swing.ActionMap aMap = rootPane.getActionMap();
 
         //Escape
         iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
         aMap.put("escape", new javax.swing.AbstractAction() {
 			private static final long serialVersionUID = 1L;
 			public void actionPerformed(ActionEvent e) {
                 System.out.println("Disposed by Escape Key.");
                 if( rootPaneContainer instanceof JDialog) {
                     ((JDialog)rootPaneContainer).dispose();
                 }
             }
         });
 
     }
 
     public void showActvPopUpMenu(final java.awt.event.MouseEvent me){
         final javax.swing.JPopupMenu popMenu = new javax.swing.JPopupMenu(recursosTexto.getString("actvConfMenuIt1"));
         final javax.swing.JMenuItem menuItem1 = new javax.swing.JMenuItem(recursosTexto.getString("actvConfMenuIt1"));
         //TODO: Add option to remove activities
         //final javax.swing.JMenuItem menuItem2 = new javax.swing.JMenuItem(recursosTexto.getString("actvConfMenuIt2"));
         final Component clickedComp = me.getComponent();
         menuItem1.addActionListener( new ActionListener(){
             public void actionPerformed(ActionEvent ae){
                 showActvRowConfigDialog(ae, me.getComponent());
                 me.getButton();
             }
         });
         popMenu.add(menuItem1);
 
         popMenu.show(clickedComp, me.getX(), me.getY());
     }
 
     public void showActvRowConfigDialog(final ActionEvent aEvt, final Component clickedComp) {
         if( !(clickedComp instanceof JLabel) ){
             return; //error
         }
 
         final javax.swing.JDialog configActvsDialog = new javax.swing.JDialog(jFrame1, JDialog.DEFAULT_MODALITY_TYPE);
         configActvsDialog.setDefaultCloseOperation( javax.swing.WindowConstants.DISPOSE_ON_CLOSE );
         configActvsDialog.setTitle(recursosTexto.getString("actvDlg1Title"));
 
         final Dimension actvConfDim = new Dimension(DIM_ACTV_CONF_DIALOG1_X, DIM_ACTV_CONF_DIALOG1_Y);
         configActvsDialog.setSize( actvConfDim );
         configActvsDialog.setPreferredSize( actvConfDim );
 //        configActvsDialog.setResizable(false);
         configActvsDialog.setLocation( (dimPant.width-configActvsDialog.getWidth())/2, (dimPant.height-configActvsDialog.getHeight())/2 );
 
 
         //COMPONENTS....
 //        JLabel actvJLabel = (JLabel)clickedComp; //(Seems to cause conflict)
         configActvsDialog.add(new JLabel(recursosTexto.getString("actvDlg1Txt1")));
         final JLabel actvJLabel = new JLabel("\"" + ((JLabel)clickedComp).getText() + "\"");
         actvJLabel.setFont(fuente1b);
 
         final JRadioButton jRdBtns[] = new JRadioButton[ACTV_DISP_DAYS];
         final ButtonGroup rBgp1 = new javax.swing.ButtonGroup();
         for (int i = 0; i < jRdBtns.length; i++) {
             jRdBtns[i] = new JRadioButton( recursosTexto.getString("actvDlg1RadLbl"+i) );
             jRdBtns[i].setName( String.valueOf(i) );
 //            jRdBtns[i].addActionListener( new ActionListener(){
 //                public void actionPerformed(ActionEvent ae){
 //System.out.println("Selected radioButton: #" + ((JRadioButton)ae.getSource()).getName() + ", text: " + ((JRadioButton)ae.getSource()).getText() );
 //                }
 //            });
             rBgp1.add(jRdBtns[i]);
         }
         //Mnemonics
         if( jRdBtns.length >= 3 ){
             jRdBtns[0].setMnemonic(KeyEvent.VK_T);
             jRdBtns[1].setMnemonic(KeyEvent.VK_C);
             jRdBtns[2].setMnemonic(KeyEvent.VK_S);
         }
         rBgp1.setSelected(jRdBtns[0].getModel(), true); //Select button 0
 
         addKeyboardShortCuts( configActvsDialog );
 
         //Buttons
         final JButton okBtn = new JButton();
         okBtn.setText( recursosTexto.getString("okBtn") );
         okBtn.addActionListener( new ActionListener(){
             public void actionPerformed(ActionEvent ae){
 
                 final int actvY;
                 try{
                     actvY = Integer.parseInt(clickedComp.getName());
                 }catch(Exception e){
                     System.out.println("Exception obtaining actvName, invalid name (must be a valid int): " + e.getMessage() );
                     return;
                 }
 
                 final int actvCompType = getSelectedBtnFromBtnGrp(rBgp1, jRdBtns);
                 jLabelsActvArr[actvY].setCompType(actvCompType);
                 updateScreenActvComponent(actvY, actvCompType);
                 updateInDBActvLabelCompType(jLabelsActvArr[actvY].getId(), actvCompType);
 
                 //FiX: Update showed activity components.
                 updateScreenNormalize();
 //                updateScreenActvCopyRowValFromTxtFld(actvY);
 //                updateScreenActvComponentsCurrentlyShown();
 
                 //close dialog....
                 configActvsDialog.dispose();
 
             }
         });
         okBtn.setMnemonic( KeyEvent.VK_ENTER );
 
         final JButton cancelBtn = new JButton();
         cancelBtn.setText( recursosTexto.getString("cancelBtn") );
         cancelBtn.addActionListener( new ActionListener(){
             public void actionPerformed(ActionEvent ae){
                 configActvsDialog.dispose();
             }
         });
 
 
         //LAYOUT... (Testing MiG)
       // configActvsDialog.setLayout( new net.miginfocom.swing.MigLayout("insets 15 15 15 15") );
         configActvsDialog.add(actvJLabel, "wrap 5mm");
         configActvsDialog.add( new JLabel( recursosTexto.getString("actvDlg1SelComp") ), "span 2 1, wrap" );
         for (int i = 0; i < jRdBtns.length; i++) {
             configActvsDialog.add( jRdBtns[i], "span 2 1, gapleft 15mm, wrap" );
         }
         configActvsDialog.add( okBtn , "gaptop 5mm, growx" );
         configActvsDialog.add( cancelBtn, "gaptop 5mm, growx" );
 
         configActvsDialog.setVisible(true);
     }
 
     /**
      * For a given button group and a button array, this method returns the selected button number in the button array.
      * If no button is selected, -1 is returned.
      */
     public int getSelectedBtnFromBtnGrp(final javax.swing.ButtonGroup btnGpo, final javax.swing.JRadioButton jRBtn[]){
         for (int i = 0; i < jRBtn.length; i++) {
             if( jRBtn[i].getModel().equals( btnGpo.getSelection() ) ){
                 return i;
             }
         }
         return -1;
     }
 
     public void updateScreenNormalize(){
         setComponentTypeInLabels();
         updateScreenAllActvComponents();
         updateScreenActvCopyAllValsFromTxtFlds();
         updateScreenActvComponentsCurrentlyShown();
         //updateScreenActvIdDayMissMatch();
     }
 
     /**
      * Uses ids of everyday to colorize activity component when the Id doesn't correspond to the current day's Id.
      * NOT IN USE.
      */
 /*    
     private void updateScreenActvIdDayMissMatch(){
     
         //allActvIds
 
         //prev day
         for (int i = 0; i < currentlyShownActivityRowsNum; i++) {
             //Compare same activitie row for diferrent days.
 //            if( allActvIds[0][i] != allActvIds[1][i] ){
             if( allActvIds[0][i] != jLabelsActvArr[i].getId() ){
                 updateScreenActvHideSpecial(-1, i); //prev day (daysToRoll = -1)
             }
         }
 
         //post day
         for (int i = 0; i < currentlyShownActivityRowsNum; i++) {
             //Compare same activitie row for diferrent days.
 //            if( allActvIds[2][i] != allActvIds[1][i] ){
             if( allActvIds[2][i] != jLabelsActvArr[i].getId() ){
                 updateScreenActvHideSpecial(1, i); //post day (daysToRoll = 1)
             }
         }
 
     }
 */
     /**
      * Hides activity individually according to day and row.
      * @param daysToRoll
      * @param actvRowNum
      */
     private void updateScreenActvHideSpecial(final int daysToRoll, final int actvRowNum){
 
         switch( jLabelsActvArr[actvRowNum].getCompType() ){
             case COMPONENT_TYPE_TEXTFIELD:
                 getTextFieldArrayByDay(daysToRoll)[actvRowNum].setVisible(false);
                 break;
             case COMPONENT_TYPE_CHECKBOX:
                 getCheckBoxArrayByDay(daysToRoll)[actvRowNum].setVisible(false);
                 break;
             case COMPONENT_TYPE_SLIDER:
                 getSliderArrayByDay(daysToRoll)[actvRowNum].setVisible(false);
                 break;
         }
 
     }
 
 
     /**
      * Method that hiddes activities components when the configuration of THEIR DAY isn't supposed to show them.
      */
     private void updateScreenActvComponentsCurrentlyShown() {
         int numOfActvRows;
 
         numOfActvRows = jTextPanesArr[0].getNumOfActivityRows();
         if( numOfActvRows > ACTV_ROW_NUM_INIT_VAL && numOfActvRows < ACTV_ROW_NUM_MAX ){
             for (int i = numOfActvRows; i < ACTV_ROW_NUM_MAX; i++) {
                 switch( jLabelsActvArr[i].getCompType() ){
                     case COMPONENT_TYPE_TEXTFIELD:
                         jTextFieldsCol1[i].setVisible(false);
                         break;
                     case COMPONENT_TYPE_CHECKBOX:
                         jCheckBoxesCol1[i].setVisible(false);
                         break;
                     case COMPONENT_TYPE_SLIDER:
                         jSlidersCol1[i].setVisible(false);
                         break;
                 }
             }
         }
 
         //Current day shouldn't have a problem.
         numOfActvRows = jTextPanesArr[2].getNumOfActivityRows();
         if( numOfActvRows > ACTV_ROW_NUM_INIT_VAL && numOfActvRows < ACTV_ROW_NUM_MAX ){
             for (int i = numOfActvRows; i < ACTV_ROW_NUM_MAX; i++) {
                 final int compType = jLabelsActvArr[i].getCompType();
                 switch( compType ){
                     case COMPONENT_TYPE_TEXTFIELD:
                         jTextFieldsCol3[i].setVisible(false);
                         break;
                     case COMPONENT_TYPE_CHECKBOX:
                         jCheckBoxesCol3[i].setVisible(false);
                         break;
                     case COMPONENT_TYPE_SLIDER:
                         jSlidersCol3[i].setVisible(false);
                         break;
                 }
             }
         }
 
     }
 
     /**
      * Method that changes ALL the currently shown component types
      * to the ones specified by the currently shown label activities.
      */
     private void updateScreenAllActvComponents() {
         for (int i = 0; i < currentlyShownActivityRowsNum; i++) {
             updateScreenActvComponent( i, jLabelsActvArr[i].getCompType() );
         }
     }
 
     /**
      * Method that changes the currently shown component type for a given activity.
      * Does a: "normalization".
      * @param actvY
      * @param actvCompType
      */
     public void updateScreenActvComponent(final int actvY, final int actvCompType){
 
         //Validations
         if( !(jCheckBoxesCol1.length > actvY) ||
             jCheckBoxesCol1.length != jCheckBoxesCol2.length ||
             jCheckBoxesCol2.length != jCheckBoxesCol3.length
           ){
             System.err.println("Invalid acvtY OR invalid array length.");
             return;
         }
 
         //Hidde all... then show used. :( ...need more efficient way but haven't figured it out yet... �isShowing()?
         jTextFieldsCol1[actvY].setVisible(false);
         jTextFieldsCol2[actvY].setVisible(false);
         jTextFieldsCol3[actvY].setVisible(false);
 
         jCheckBoxesCol1[actvY].setVisible(false);
         jCheckBoxesCol2[actvY].setVisible(false);
         jCheckBoxesCol3[actvY].setVisible(false);
 
         jSlidersCol1[actvY].setVisible(false);
         jSlidersCol2[actvY].setVisible(false);
         jSlidersCol3[actvY].setVisible(false);
 
         switch ( actvCompType ){
             case COMPONENT_TYPE_TEXTFIELD:
                 jTextFieldsCol1[actvY].setVisible(true);
                 jTextFieldsCol2[actvY].setVisible(true);
                 jTextFieldsCol3[actvY].setVisible(true);
                 break;
 
             case COMPONENT_TYPE_CHECKBOX:
                 jCheckBoxesCol1[actvY].setVisible(true);
                 jCheckBoxesCol2[actvY].setVisible(true);
                 jCheckBoxesCol3[actvY].setVisible(true);
                 break;
 
             case COMPONENT_TYPE_SLIDER:
                 jSlidersCol1[actvY].setVisible(true);
                 jSlidersCol2[actvY].setVisible(true);
                 jSlidersCol3[actvY].setVisible(true);
                 break;
         }
 
     }
 
     /**
      * Sets initial values to a given row, requires only setting values to textFields since they the other components get their values from this one.
      * @param rowNum
      */
     private void updateScreenResetRowValues(final int rowNum){
         jTextFieldsCol1[rowNum].setText(ACTV_TXTFLD_UNINIT_VAL);
         jTextFieldsCol2[rowNum].setText(ACTV_TXTFLD_UNINIT_VAL);
         jTextFieldsCol3[rowNum].setText(ACTV_TXTFLD_UNINIT_VAL);
     }
 
     private void updateScreenActvCopyRowValFromTxtFld(final int actvY){
 
         int actvCompType = jLabelsActvArr[actvY].getCompType();
 
         if( actvCompType != COMPONENT_TYPE_TEXTFIELD ){
 
             String txtFldVal1 = jTextFieldsCol1[actvY].getText();
             if( txtFldVal1 == null || txtFldVal1.equals(ACTV_TXTFLD_UNINIT_VAL) ){
                 txtFldVal1 = ACTV_TXTFLD_EMPTY_VAL;
             }
 
             String txtFldVal2 = jTextFieldsCol2[actvY].getText();
             if( txtFldVal2 == null || txtFldVal2.equals(ACTV_TXTFLD_UNINIT_VAL) ){
                 txtFldVal2 = ACTV_TXTFLD_EMPTY_VAL;
             }
 
             String txtFldVal3 = jTextFieldsCol3[actvY].getText();
             if( txtFldVal3 == null || txtFldVal3.equals(ACTV_TXTFLD_UNINIT_VAL) ){
                 txtFldVal3 = ACTV_TXTFLD_EMPTY_VAL;
             }
 
             if( actvCompType == COMPONENT_TYPE_CHECKBOX ){
                 jCheckBoxesCol1[actvY].setSelected( txtFldVal1.equals(ACTV_TXTFLD_EMPTY_VAL) ? false : true );
                 jCheckBoxesCol2[actvY].setSelected( txtFldVal2.equals(ACTV_TXTFLD_EMPTY_VAL) ? false : true );
                 jCheckBoxesCol3[actvY].setSelected( txtFldVal3.equals(ACTV_TXTFLD_EMPTY_VAL) ? false : true );
             }else if(actvCompType == COMPONENT_TYPE_SLIDER ){
                 int sliderVal1 = Integer.parseInt(txtFldVal1);
                 int sliderVal2 = Integer.parseInt(txtFldVal2);
                 int sliderVal3 = Integer.parseInt(txtFldVal3);
                 jSlidersCol1[actvY].setValue( (sliderVal1 > ACTV_SLIDER_MIN_VAL) ? sliderVal1 : ACTV_SLIDER_MIN_VAL );
                 jSlidersCol2[actvY].setValue( (sliderVal2 > ACTV_SLIDER_MIN_VAL) ? sliderVal2 : ACTV_SLIDER_MIN_VAL );
                 jSlidersCol3[actvY].setValue( (sliderVal3 > ACTV_SLIDER_MIN_VAL) ? sliderVal3 : ACTV_SLIDER_MIN_VAL );
             }
 
         }
 
     }
 
     private void updateScreenActvCopyAllValsFromTxtFlds(){
 
         for (int i = 0; i < currentlyShownActivityRowsNum; i++) {
             int actvCompType = jLabelsActvArr[i].getCompType();
             if( actvCompType != COMPONENT_TYPE_TEXTFIELD ){
 
                 String txtFldVal1 = jTextFieldsCol1[i].getText();
                 if( txtFldVal1 == null || txtFldVal1.equals(ACTV_TXTFLD_UNINIT_VAL) ){
                     txtFldVal1 = ACTV_TXTFLD_EMPTY_VAL;
                 }
 
                 String txtFldVal2 = jTextFieldsCol2[i].getText();
                 if( txtFldVal2 == null || txtFldVal2.equals(ACTV_TXTFLD_UNINIT_VAL) ){
                     txtFldVal2 = ACTV_TXTFLD_EMPTY_VAL;
                 }
 
                 String txtFldVal3 = jTextFieldsCol3[i].getText();
                 if( txtFldVal3 == null || txtFldVal3.equals(ACTV_TXTFLD_UNINIT_VAL) ){
                     txtFldVal3 = ACTV_TXTFLD_EMPTY_VAL;
                 }
 
                 if( actvCompType == COMPONENT_TYPE_CHECKBOX ){
                     jCheckBoxesCol1[i].setSelected( txtFldVal1.equals(ACTV_TXTFLD_EMPTY_VAL) ? false : true );
                     jCheckBoxesCol2[i].setSelected( txtFldVal2.equals(ACTV_TXTFLD_EMPTY_VAL) ? false : true );
                     jCheckBoxesCol3[i].setSelected( txtFldVal3.equals(ACTV_TXTFLD_EMPTY_VAL) ? false : true );
                 }else if( actvCompType == COMPONENT_TYPE_SLIDER ){
                     int sliderVal1 = Integer.parseInt(txtFldVal1);
                     int sliderVal2 = Integer.parseInt(txtFldVal2);
                     int sliderVal3= Integer.parseInt(txtFldVal3);
                     jSlidersCol1[i].setValue( (sliderVal1 > ACTV_SLIDER_MIN_VAL) ? sliderVal1 : ACTV_SLIDER_MIN_VAL );
                     jSlidersCol2[i].setValue( (sliderVal2 > ACTV_SLIDER_MIN_VAL) ? sliderVal2 : ACTV_SLIDER_MIN_VAL );
                     jSlidersCol3[i].setValue( (sliderVal3 > ACTV_SLIDER_MIN_VAL) ? sliderVal3 : ACTV_SLIDER_MIN_VAL );
                 }
 
             }
         }
 
     }
 
     private void updateScreenActvCopyAllComponentValsToTxtFlds(){
 
         for (int i = 0; i < currentlyShownActivityRowsNum; i++) {
             int actvCompType = jLabelsActvArr[i].getCompType();
             if( actvCompType != COMPONENT_TYPE_TEXTFIELD ){
 
                 String txtFldVal1;
                 String txtFldVal2;
                 String txtFldVal3;
 
                 if( actvCompType == COMPONENT_TYPE_CHECKBOX ){
                     txtFldVal1 = jCheckBoxesCol1[i].isSelected() ? ACTV_CB_CHECKED_VAL : ACTV_CB_UNCHECKED_VAL;
                     txtFldVal2 = jCheckBoxesCol2[i].isSelected() ? ACTV_CB_CHECKED_VAL : ACTV_CB_UNCHECKED_VAL;
                     txtFldVal3 = jCheckBoxesCol3[i].isSelected() ? ACTV_CB_CHECKED_VAL : ACTV_CB_UNCHECKED_VAL;
                 }else if( actvCompType == COMPONENT_TYPE_SLIDER ){
                     txtFldVal1 = String.valueOf( jSlidersCol1[i].getValue() );
                     txtFldVal2 = String.valueOf( jSlidersCol2[i].getValue() );
                     txtFldVal3 = String.valueOf( jSlidersCol3[i].getValue() );
                 }else{
                     //never gonna happen?
                     continue;
                 }
                 
                 jTextFieldsCol1[i].setText( txtFldVal1 );
                 jTextFieldsCol2[i].setText( txtFldVal2 );
                 jTextFieldsCol3[i].setText( txtFldVal3 );
 
             }
         }
 
     }
 
     /**
      * Generates a panel an shows all existing labels
      * @param parentComp
      */
     public void showExistingAllLabels(final Component parentComp){
     	
     	java.util.Map<String, String> labelsMap = getFromDBAllLabelsDesc();
     	java.util.Set<java.util.Map.Entry<String,String>> labelsSet = labelsMap.entrySet();
     	
     	StringBuilder strB = new StringBuilder();
     	int i = 0;
     	for (Iterator<java.util.Map.Entry<String,String>> iterator = labelsSet.iterator(); iterator.hasNext(); i++) {
     		java.util.Map.Entry<String,String> mapEntry = iterator.next();
     		String value = mapEntry.getValue();
     		if( i < LABELS_PER_LINE ){
     			strB.append(value + ", ");
     		}else{
     			strB.append(value+LINE_SEPARATOR);
     			i = 0;
     		}
 		}
     	
     	strB.append(LINE_SEPARATOR);
     	strB.append(LINE_SEPARATOR);
 
     	strB.append(recursosTexto.getString("currentActivities"));
     	strB.append(LINE_SEPARATOR);
 
     	for (int j = 0, k = 0; j < cache.labelsActArr.length; j++, k++) {
     		if( k < LABELS_PER_LINE ){
     			strB.append(cache.labelsActArr[j] + ", ");
     		}else{
     			strB.append(cache.labelsActArr[j]+LINE_SEPARATOR);
     			k = 0;
     		}
 		}
     	
     	String DEL_STR = "null,";
     	int pos = strB.indexOf(DEL_STR);
     	while( pos != -1 ){
     		strB.delete(pos, pos+DEL_STR.length());
     		pos = strB.indexOf(DEL_STR);
     	}
     	
     	if( strB.length()-1 == strB.indexOf(",", strB.length()-2)+1 ){
     		strB.delete(strB.length()-2, strB.length()-1);
     	}
     	
     	javax.swing.JOptionPane.showMessageDialog(parentComp, strB.toString(), recursosTexto.getString("toolsMenuItem2Title"), JOptionPane.INFORMATION_MESSAGE);
     }
     	
     	
     /**
      * Method that shows dialogs to obtain user parameters to make DB consult and obtain content which is exported to text files.
      * @param parentComp
      */
     public void showExportRegistersToTextFiles(final Component parentComp){
         String startDate, endDate;
         boolean validInput = false;
         do{
             startDate = JOptionPane.showInternalInputDialog(parentComp,
                                                             recursosTexto.getString("txtInpDateS") + DATE_RANGE_WILDCARD + recursosTexto.getString("txtInpDateS2"),
                                                             recursosTexto.getString("txtInpDateSTitle"),
                                                             JOptionPane.INFORMATION_MESSAGE);
             if(startDate == null){
                 return;
             }
             validInput = true;
             if( !validateExportDate(startDate) ){
                 validInput = false;
                 javax.swing.JOptionPane.showMessageDialog(parentComp, recursosTexto.getString("txtInpBadFrmt"), recursosTexto.getString("txtInpBadFrmtTitle"), JOptionPane.WARNING_MESSAGE);
             }
         }while( !validInput );
 
         do{
             endDate = JOptionPane.showInternalInputDialog(parentComp,
                                                           recursosTexto.getString("txtInpDateE") + DATE_RANGE_WILDCARD + recursosTexto.getString("txtInpDateE2"),
                                                           recursosTexto.getString("txtInpDateETitle"),
                                                           JOptionPane.INFORMATION_MESSAGE);
             if(endDate == null){
                 return;
             }
             validInput = true;
             if( !validateExportDate(endDate) ){
                 validInput = false;
                 javax.swing.JOptionPane.showMessageDialog(parentComp, recursosTexto.getString("txtInpBadFrmt"), recursosTexto.getString("txtInpBadFrmtTitle"), JOptionPane.WARNING_MESSAGE);
             }
         }while( !validInput );
 
         //Check alternative
         if( DATE_RANGE_WILDCARD.equals(startDate) ){
             startDate = "0";
         }
         if( DATE_RANGE_WILDCARD.equals(endDate) ){
             endDate = "9999-12-31"; //ROFLMAO: I don't think this app will ever make it to this year, but just in case.
         }
 
 
         if( DATE_RANGE_CURRENT_DATE.equals(startDate) ){
             startDate = dbPKeyFormat.format(selectedDate);
         }
         
         if( DATE_RANGE_CURRENT_DATE.equals(endDate) ){
             endDate = dbPKeyFormat.format(selectedDate);
         }
 
         //Todo... start thread with operations at the time this popsup?
         doExport(startDate, endDate, parentComp);
         
         /* Crashed when tested.
         final String startDateF = startDate; 
         final String endDateF = endDate;
 		Thread export2TextThread = new Thread( new Runnable(){
 			public void run(){
 				//try{
 					
 					doExport(startDateF, endDateF, parentComp);
 					
 				//}catch(InterruptedException ie){};				
 			};
 		}); 
 		export2TextThread.start();
 		*/
 		
 
 
     }
 
     private void doExport(final String startDate, final String endDate, final Component parentComp){
 
     	//Warning message
         javax.swing.JOptionPane.showMessageDialog(parentComp, recursosTexto.getString("txtDelayWarn"), recursosTexto.getString("msgTitleWarn"), JOptionPane.WARNING_MESSAGE);
 
         //db cosult...
         java.util.Map<String, String> labelsMap = getFromDBAllLabelsDesc();
         
         boolean terminoConExito = true;
     	try{
             dbConnection = Database.generaConexion();
             prepStatement = dbConnection.prepareStatement("SELECT * FROM registros WHERE id_fecha >='" + startDate + "' AND id_fecha <='" + endDate + "';");
 
             resultSet = prepStatement.executeQuery();
             String[] labels;
             while( resultSet.next() ){
 
                 //getLabels
                 if( resultSet.getString("id_cbs") != null ){
                     labels = resultSet.getString("id_cbs").split(ACTV_VAL_DELIM_STR_REGEX);
                 }else{
                     labels = new String[0];
                 }
                 //pasa del id al valor...
                 for (int i = 0; i < labels.length; i++) {
                     labels[i] = labelsMap.get(labels[i]); //if null... luego vemos
                 }
 
                 String texto = resultSet.getString("texto");
                 if( texto != null ){
                 	if( CryptoUtils.isStrCiphered(texto) && isUsingPass ){
                 		texto = CryptoUtils.decrypt( texto );
                 	}
                 }
 
                 //export2FormatedTxtFile
                 if( !jmydays.util.Util.export2FormatedJsonFile( resultSet.getString("id_fecha"),
                                                          labels,
                                                          ( resultSet.getString("cbs") != null ? resultSet.getString("cbs").split(ACTV_VAL_DELIM_STR_REGEX) : new String[0]),
                                                          texto
                                                         )
                 	){
                 	//Si falla creacion de un archivo, fallan los demas.
                 	terminoConExito = false;
                 	break;
             	}
 
             }
             
             dbCloseResPrepStmtConn();
 
         }catch(java.sql.SQLException sqle){
         	javax.swing.JOptionPane.showMessageDialog(parentComp, sqle.getMessage(), recursosTexto.getString("msgTitleWarn"), JOptionPane.WARNING_MESSAGE);
             System.err.println("java.sql.SQLException message: " + sqle.getMessage());
             terminoConExito = false;
         }
         
         if( terminoConExito ){
         	javax.swing.JOptionPane.showMessageDialog(parentComp, recursosTexto.getString("txtExportFinishedOk"), recursosTexto.getString("msgTitleWarn"), JOptionPane.WARNING_MESSAGE);
     	}else{
     		javax.swing.JOptionPane.showMessageDialog(parentComp, recursosTexto.getString("txtExportFinishedErr"), recursosTexto.getString("msgTitleWarn"), JOptionPane.WARNING_MESSAGE);
     	}
         	
 
     }
     
     private void showAboutDialog(javax.swing.JPanel jPanel){
         JOptionPane.showInternalMessageDialog(jPanel, ABOUT_MSG + recursosTexto.getString("currAppVersion"), recursosTexto.getString("aboutMenuItem1"), JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("/ima.gif")));
     }
 
     private java.util.Map<String, String> getFromDBAllLabelsDesc(){
 
         java.util.Map<String, String> labelsMap = new java.util.HashMap<String, String>();
 
         try{
             dbConnection = Database.generaConexion();
             prepStatement = dbConnection.prepareStatement("SELECT id, desc FROM actvs_cat;");
             resultSet = prepStatement.executeQuery();
 
             while( resultSet.next() ){
                 labelsMap.put(resultSet.getString("id"), resultSet.getString("desc"));
             }
 
             dbCloseResPrepStmtConn();
         }catch(java.sql.SQLException sqle){
             System.err.println("java.sql.SQLException message:" + sqle.getMessage() );
         }
 
         return labelsMap;
     }
 
     private boolean validateExportDate(final String date){
         if( DATE_RANGE_WILDCARD.equals(date) || DATE_RANGE_CURRENT_DATE.equals(date) ){
             return true;
         }
 
 
         if(date == null || date.length() != 10){
             return false;
         }
         
         final String DATE_FORMAT_REGEX = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}";
 		if( date.matches(DATE_FORMAT_REGEX) ){
 			return true;
 		}else{
 			return false;
 		}
 		
 		/*
         String nums[] = date.split("-");
         if( nums.length != 3 ){
             return false;
         }
 
         try{
             for (int i = 0; i < nums.length; i++) {
                Integer.parseInt(nums[i]);
             }
 
             return true;
 
         }catch(NumberFormatException nfe){
             return false;
         }
 		*/
 
     }
 
     private class ObservingTextField extends javax.swing.JTextField implements java.util.Observer {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
         public void update(java.util.Observable o, Object arg) {
             
             //first save previous changes... in all visible days
             updateInDBAll(jTextPanesArr);
 
             Calendar calendar = (Calendar) arg;
             DatePicker dp = (DatePicker) o;
             setText(dp.formatDate(calendar));
 
             //TODO:
             //UpdateLabels
             selectedDate = calendar.getTime();
             updateScreenAllByDate( selectedDate );
 
         }
     }
 
     private class JPanelEvents implements KeyListener {
 
         @Override
         public void keyReleased(KeyEvent e) {
             keyEventProcess(e);
         }
 
         @Override
         public void keyPressed(KeyEvent e) {
         }
 
         @Override
         public void keyTyped(KeyEvent e) {
         }
 
     }
 
     /**
      * Class used to add event listeners to: JTextPanes and JCheckBoxes (just focus)
      */
     private class EventListeners implements KeyListener, FocusListener {
 
         @Override
         public void focusGained(FocusEvent fe){
         }
 
         @Override
         public void focusLost(FocusEvent fe){
             updateInDBDayEntry( fe.getComponent().getName() );
         }
 
         @Override
         public void keyReleased(KeyEvent ke) {
             if( ke.isAltDown() ){
                 //Para que tambi�n guarde... dado que se salta el evento de focusLost...
                 updateInDBDayEntry( ke.getComponent().getName() );
                 keyEventProcess(ke);
             }
         }
 
         @Override
         public void keyPressed(KeyEvent ke) {
         }
 
         @Override
         public void keyTyped(KeyEvent ke) {
 //            keyReleased(e);
         }
 
     }
 
     class LabelEvents extends java.awt.event.MouseAdapter implements FocusListener{
 
         @Override
         public void mouseClicked(java.awt.event.MouseEvent me){
             
             if (me.isPopupTrigger() || me.getButton() == MouseEvent.BUTTON3) {
                 showActvPopUpMenu(me);
                 final Component clkdComp = me.getComponent();
                 clkdComp.requestFocus();
 //                clkdComp.repaint(5000, 0, 0, clkdComp.getWidth(), clkdComp.getHeight());
             }
 
             if( me.getClickCount() == MOUSE_DOUBLE_CLICK && me.getButton() == MouseEvent.BUTTON1 ){
                 switchActvLabelTxtFieldVisibility(me.getComponent(), true);
             }
         }
 
         @Override
         public void focusLost(FocusEvent fe){
             final Component comp = fe.getComponent();
             comp.setForeground( actvDefColor );
             comp.repaint();
         }
 
         @Override
         public void focusGained(FocusEvent fe){
             final Component comp = fe.getComponent();
             actvDefColor = comp.getForeground();
             comp.setForeground( CURRENTLY_ACTIVE_COLOR );
         }
 
     }
 
     /**
      * Class for TextField listener events. (Include mouse clicks)
      */
     class TextFieldEvents extends java.awt.event.MouseAdapter implements FocusListener, KeyListener{
 
         @Override
         public void mouseClicked(java.awt.event.MouseEvent me){
             if( me.getClickCount() == MOUSE_DOUBLE_CLICK ){
                 switchActvLabelTxtFieldVisibility(me.getComponent(), true);
             }
         }
 
         @Override
         public void keyReleased(KeyEvent ke) {
 
             switch( ke.getKeyCode() ){
                 case KeyEvent.VK_ENTER:
                     switchActvLabelTxtFieldVisibility(ke.getComponent(), true);
                     break;
 
                 case KeyEvent.VK_ESCAPE:
                     switchActvLabelTxtFieldVisibility(ke.getComponent(), true);
                     break;
             }
 
         }
 
         @Override
         public void keyPressed(KeyEvent ke) {
         }
 
         @Override
         public void keyTyped(KeyEvent ke) {
         }
 
 
         @Override
         public void focusGained(FocusEvent fe){
         }
 
         @Override
         public void focusLost(FocusEvent fe){
             switchActvLabelTxtFieldVisibility(fe.getComponent(), true);
         }
 
     }
 
     /**
      * Class for aditional Text Pane listener events.
      */
     class TextPaneEvents implements KeyListener, java.awt.event.MouseWheelListener {
 //    class TextPaneEvents extends java.awt.event.MouseAdapter implements KeyListener {
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent mwe) {
             if( mwe.isControlDown() ){
                 updateScreenComponentTextSizeChoose(mwe, jTextPanesArr);
             }else{
                 //way to keep mouse scroll functioning =)
                 mwe.getComponent().getParent().dispatchEvent(mwe);
             }
         }
 
         @Override
         public void keyReleased(KeyEvent ke) {
 
             if( ke.isControlDown() ){
                 if( ke.getKeyCode() == KeyEvent.VK_Z ){
                     final JTextPane txtPane = ((JTextPane)ke.getComponent());
                     txtPane.setText( typedHistoryChange( txtPane.getName(), txtPane.getText(), !ke.isShiftDown() ) );
                 }
             }else{
 //                if( typedCharsCount > TYPED_HISTORY_REG_CHAR_INTERVAL){
             	if( ke.getKeyCode() == KeyEvent.VK_SPACE ){
                     final JTextPane txtPane = ((JTextPane)ke.getComponent());
 //                    if( txtPane.getText().length() < TYPED_HISTORY_REG_CHAR_MAX_LENGTH ){
                         typedHistoryRegister( txtPane.getName(), txtPane.getText() );
 //                    }
 //                    else{
 //                        typedCharsCount = 0;
 //                    }
 //                }else{
 //                    typedCharsCount++;
 //                }
             	}//if key == space
             }
 
         }
 
         @Override
         public void keyPressed(KeyEvent ke) {
             final int keyCode = ke.getKeyCode();
             if( ke.isControlDown() && ( keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_MINUS ) ){
                 //text resize TextField Text
                 updateScreenComponentTextSizeChoose(ke, jTextPanesArr);
             }
         }
 
         @Override
         public void keyTyped(KeyEvent ke) {
         }
 
 
     }
 
     /**
      * Class with extra field: "id", used to store the ACTIVITY ID.
      * "Name" field is used to relate to the hiddenTextField.
      */
     static class MyActvJLabel extends javax.swing.JLabel{
 
         /**
          * Keeps track of the ACTIVITY ID related to this field.
          */
         private int id;
 
         private int compType;
 
         /**
          * Returns the activity Id corresponding to this label.
          * @return An activity Id.
          */
         public int getId() {
             return id;
         }
 
         /**
          * Sets the activity Id corresponding to this label.
          * @param id An activity Id.
          */
         public void setId(int id) {
             this.id = id;
         }
 
         /**
          * @return compType The Component type.
          */
         public int getCompType() {
             return compType;
         }
 
         /**
          * @param compType the component type to set
          */
         public void setCompType(int compType) {
             this.compType = compType;
         }
 
     }
 
     static class MyJSlider extends javax.swing.JSlider implements MyComponent{
         private int compRelationX;
         private int compRelationY;
 
         public int getCompRelationX() {
             return compRelationX;
         }
 
         public void setCompRelationX(int compRelationX) {
             this.compRelationX = compRelationX;
         }
 
         public int getCompRelationY() {
             return compRelationY;
         }
 
         public void setCompRelationY(int compRelationY) {
             this.compRelationY = compRelationY;
         }
     }
 
     static class MyJTextField extends javax.swing.JTextField implements MyComponent{
         private int compRelationX;
         private int compRelationY;
 
         public int getCompRelationX() {
             return compRelationX;
         }
 
         public void setCompRelationX(int compRelationX) {
             this.compRelationX = compRelationX;
         }
 
         public int getCompRelationY() {
             return compRelationY;
         }
 
         public void setCompRelationY(int compRelationY) {
             this.compRelationY = compRelationY;
         }
     }
 
     static class MyJCheckBox extends javax.swing.JCheckBox implements MyComponent{
         private int compRelationX;
         private int compRelationY;
 
         public int getCompRelationX() {
             return compRelationX;
         }
 
         public void setCompRelationX(int compRelationX) {
             this.compRelationX = compRelationX;
         }
 
         public int getCompRelationY() {
             return compRelationY;
         }
 
         public void setCompRelationY(int compRelationY) {
             this.compRelationY = compRelationY;
         }
     }
 
     static class MyJTextPane extends javax.swing.JTextPane implements JMyDaysConstants{
 		private static final long serialVersionUID = 1L;
 		
 		/**
          * Contains the number of Activity Rows (currentlyShownActivityRowsNum)
          * corresponding to the DAY's DATE of the Components related to the
          * COLUMN of THIS JTextPane.
          */
         private int numOfActivityRows = ACTV_ROW_NUM_INIT_VAL; //Initial value.
 
         public int getNumOfActivityRows() {
             return numOfActivityRows;
         }
 
         public void setNumOfActivityRows(int numOfActivityRows) {
             this.numOfActivityRows = numOfActivityRows;
         }
     }
 
     interface MyComponent{
         /**
          * Keep track of the X relation of this component to other components.
          * Usually will refer to a DAY.
          */
         public abstract int getCompRelationY();
 
         /**
          * Keep track of the Y relation of this component to other components.
          *
          */
         public abstract int getCompRelationX();
         public abstract void setCompRelationY(int compRelationY);
         public abstract void setCompRelationX(int compRelationX);
     }
 
     /**
      * Class used to limit/restrict input in a text field.
      */
     public static class JTextFieldLimit extends javax.swing.text.PlainDocument {
 
         private int limit;
         private boolean onlyIntegers = false;
         private boolean toUppercase = false;
 
         JTextFieldLimit() {
             this(250, false);
         }
 
         JTextFieldLimit(int limit) {
             this(limit, false);
         }
 
         JTextFieldLimit(int limit, boolean onlyIntegers) {
             super();
             this.limit = limit;
             this.onlyIntegers = onlyIntegers;
         }
 
         JTextFieldLimit(int limit, boolean onlyIntegers, boolean toUppoercase) {
             super();
             this.limit = limit;
             this.onlyIntegers = onlyIntegers;
             this.toUppercase = toUppoercase;
         }
 
         public void insertString(int offset, String str, javax.swing.text.AttributeSet attr)
                 throws javax.swing.text.BadLocationException {
             if (str == null) {
                 return;
             }
 
             if ( (getLength() + str.length()) <= limit) {
 
                 if(onlyIntegers){
                     try{
                         Integer.parseInt(str);
                     }catch(Exception e){
                     	e.printStackTrace();
                     	return;
                     }
                 }
 
                 if (toUppercase) {
                     str = str.toUpperCase();
                 }
 
                 super.insertString(offset, str, attr);
             }
         }
     }
 
     /**
      * Class with action listener for all Slidders, Textboxes and Checkboxes of activities.
      * Focus listener is used for special cases when input wasn't supplied from a mouse click.
      */
     class ActvCompActionEvents implements java.awt.event.FocusListener, java.awt.event.MouseListener {
 
     	@Override
     	public void focusLost(FocusEvent e) {
     		updateScreenActvLabelColors(); // on focus lost, for all CheckBoxes or Sliders, update colors
     	}
     	
     	@Override public void focusGained(FocusEvent e) {}
 
         @Override
         public void mouseReleased(MouseEvent e) {
         	updateScreenActvLabelColors(); // on focus lost, for all CheckBoxes or Sliders, update colors
         }
 
 		@Override public void mouseClicked(MouseEvent e) { }
 
 		@Override public void mousePressed(MouseEvent e) { }
 
 		@Override public void mouseEntered(MouseEvent e) { }
 
 		@Override public void mouseExited(MouseEvent e) { }
 
     }
     
     /**
      * Class to be used ONLY to add a Focus listener ONLY TO CHECKBOXES OR SLIDERS.
      * Focus listener is used for special cases when input wasn't supplied from a mouse click.
      */
     class ActvCompValueCopyEvents implements java.awt.event.FocusListener, java.awt.event.MouseListener {
 
         @Override public void focusGained(java.awt.event.FocusEvent fe){}
 
         @Override
         public void focusLost(java.awt.event.FocusEvent fe) { 
         	copyCheckboxAndSlidderValuesToTextfield(fe.getComponent());
         }//focusLost
 
         @Override
         public void mouseReleased(MouseEvent me) { 
         	copyCheckboxAndSlidderValuesToTextfield(me.getComponent());
         }
 
 		@Override public void mouseClicked(MouseEvent e) { }
 
 		@Override public void mousePressed(MouseEvent e) { }
 
 		@Override public void mouseEntered(MouseEvent e) { }
 
 		@Override public void mouseExited(MouseEvent e) { }
 
 		
 		
     }
 
     /**
      * Copies values from CheckBoxes or Sliders into the TextField.
      * @param comp
      */
     private void copyCheckboxAndSlidderValuesToTextfield(final Component comp){
     	
         //final Component comp = event.getComponent();
         if( comp instanceof MyComponent ){
             final MyComponent myComp = (MyComponent)comp;
             final int actvY = myComp.getCompRelationY(); //First time use!! LOL! (I was thinking I wouln'd use this.)
             final String txtFldVal1;
             final String txtFldVal2;
             final String txtFldVal3;
             if( comp instanceof JCheckBox){
                 txtFldVal1 = jCheckBoxesCol1[actvY].isSelected() ? ACTV_CB_CHECKED_VAL : ACTV_CB_UNCHECKED_VAL;
                 txtFldVal2 = jCheckBoxesCol2[actvY].isSelected() ? ACTV_CB_CHECKED_VAL : ACTV_CB_UNCHECKED_VAL;
                 txtFldVal3 = jCheckBoxesCol3[actvY].isSelected() ? ACTV_CB_CHECKED_VAL : ACTV_CB_UNCHECKED_VAL;
             }else if( comp instanceof JSlider ){
                 txtFldVal1 = String.valueOf( jSlidersCol1[actvY].getValue() );
                 txtFldVal2 = String.valueOf( jSlidersCol2[actvY].getValue() );
                 txtFldVal3 = String.valueOf( jSlidersCol3[actvY].getValue() );
             }else if( comp instanceof JTextField ){
             	//do nothing
             	txtFldVal1 = "";
             	txtFldVal2 = "";
             	txtFldVal3 = "";
             }else{
                 throw new RuntimeException("Invalid component!");
                 //return;
             }
             
             jTextFieldsCol1[actvY].setText( txtFldVal1 );
             jTextFieldsCol2[actvY].setText( txtFldVal2 );
             jTextFieldsCol3[actvY].setText( txtFldVal3 );
         }
     }
     
 
     //Failed Attemp to save all when exiting...
     /*
     @Override
     public void finalize(){
     }
     */
 
     /*
 	//HACK!!! To allow updates before running this fuction
 	Thread hackThread = new Thread( new Runnable(){
 		public void run() {
 			try {
 				Thread.currentThread().sleep(100);
 				//Do something...
 			} catch (InterruptedException ie) { 
 				System.err.println("Thread interrupted! " + ie.getMessage());
 			}
 		}
 	});
 	hackThread.start();
      */
     
 }
