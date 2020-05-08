 
 package pnlp;
 
 import javax.microedition.lcdui.*;
 import pnlp.Configuration.*;
 import pnlp.Constants.*;
 import pnlp.HelpForm.*;
 import pnlp.MalariaStockOutsReport.*;
 
 /**
  * J2ME Stock Outs Form
  * Displays all fields required for over 5yo section
  * Checks that those are all filled up
  * Checks for data errors
  * Saves into the DB.
  * @author rgaudin
  */
 public class MalariaStockOutsForm extends Form implements CommandListener {
 
     private static final Command CMD_EXIT = new Command ("Retour", Command.BACK, 1);
    private static final Command CMD_SAVE = new Command ("Enreg.", Command.OK, 1);
     private static final Command CMD_HELP = new Command ("Aide", Command.HELP, 2);
 
     public PNLPMIDlet midlet;
 
     private Configuration config;
 
     private String[] choices = {" --- ", "Oui", "Non"};
     private ChoiceGroup stockout_act_children;
     private ChoiceGroup stockout_act_youth;
     private ChoiceGroup stockout_act_adult;
     private ChoiceGroup stockout_arthemeter;
     private ChoiceGroup stockout_quinine;
     private ChoiceGroup stockout_serum;
     private ChoiceGroup stockout_bednet;
     private ChoiceGroup stockout_rdt;
     private ChoiceGroup stockout_sp;
 
 public MalariaStockOutsForm(PNLPMIDlet midlet) {
     super("Ruptures de Stock.");
     this.midlet = midlet;
 
     config = new Configuration();
 
     // creating al fields (blank)
     stockout_act_children = new ChoiceGroup("CTA Nourisson - Enfant", ChoiceGroup.POPUP, choices, null);
     stockout_act_youth = new ChoiceGroup("CTA Adolescent", ChoiceGroup.POPUP, choices, null);
     stockout_act_adult = new ChoiceGroup("CTA Adulte", ChoiceGroup.POPUP, choices, null);
     stockout_arthemeter = new ChoiceGroup("Arthemether injectable", ChoiceGroup.POPUP, choices, null);
     stockout_quinine = new ChoiceGroup("Quinine Injectable", ChoiceGroup.POPUP, choices, null);
     stockout_serum = new ChoiceGroup("Serum", ChoiceGroup.POPUP, choices, null);
     stockout_bednet = new ChoiceGroup("MILD", ChoiceGroup.POPUP, choices, null);
     stockout_rdt = new ChoiceGroup("TDR", ChoiceGroup.POPUP, choices, null);
     stockout_sp = new ChoiceGroup("SP", ChoiceGroup.POPUP, choices, null);
 
     // if user requested to continue an existing report
     if (config.get("has_data").equalsIgnoreCase("true")) {
         // create an report object from store
         MalariaStockOutsReport report = new MalariaStockOutsReport();
         report.loadFromStore();
 
         // assign stored value to each fields.
         stockout_act_children.setSelectedIndex(valueForField(report.stockout_act_children), true);
         stockout_act_youth.setSelectedIndex(valueForField(report.stockout_act_youth), true);
         stockout_act_adult.setSelectedIndex(valueForField(report.stockout_act_adult), true);
         stockout_arthemeter.setSelectedIndex(valueForField(report.stockout_arthemeter), true);
         stockout_quinine.setSelectedIndex(valueForField(report.stockout_quinine), true);
         stockout_serum.setSelectedIndex(valueForField(report.stockout_serum), true);
         stockout_bednet.setSelectedIndex(valueForField(report.stockout_bednet), true);
         stockout_rdt.setSelectedIndex(valueForField(report.stockout_rdt), true);
         stockout_sp.setSelectedIndex(valueForField(report.stockout_sp), true);
     }
 
     // add fields to forms
     append(stockout_act_children);
     append(stockout_act_youth);
     append(stockout_act_adult);
     append(stockout_arthemeter);
     append(stockout_quinine);
     append(stockout_serum);
     append(stockout_bednet);
     append(stockout_rdt);
     append(stockout_sp);
 
     addCommand(CMD_EXIT);
     addCommand(CMD_SAVE);
     addCommand(CMD_HELP);
     this.setCommandListener (this);
 }
 
     /*
      * Provide int value of a <code>ChoiceGroup</code> stock out field.
      * @param field ChoiceGroup field which you want to convert value
      * @return 1 if value is YES (stock outs during month)
      * 0 if value is NO, -1 otherwise.
      */
     public int fieldValue(ChoiceGroup field) {
         // mapping is:
         // 0 = --- = -1
         // 1 = YES = 1
         // 2 = NO = 0
        if (stockout_sp.getSelectedIndex() == 1) {
             return 1;
        } else if (stockout_sp.getSelectedIndex() == 2)
             return 0;
         else {
             return -1;
         }
     }
 
     /*
      * Provide ChoiceGroup Index for a Stock Out value.
      * @param value int representing if stock outs or not (1 or 0)
      * @return Index in <code>choices</true> representing the value
      */
     public int valueForField(int value) {
         // mapping is:
         // -1 = --- = 0
         // 0 = NO = 2
         // 1 = YES = 1
         if (value == 1) {
             return 1;
         } else if (value == 0) {
             return 2;
         }
         return 0;
     }
 
     /*
      * Whether all required fields are filled
      * @return <code>true</code> is all fields are filled
      * <code>false</code> otherwise.
      */
     public boolean isComplete() {
         // all fields are required to be filled.
         // stock outs have default values and thus are always complete.
         return true;
     }
 
     public void commandAction(Command c, Displayable d) {
         // help command displays Help Form.
         if (c == CMD_HELP) {
             HelpForm h = new HelpForm(this.midlet, this, "stock_outs");
             this.midlet.display.setCurrent(h);
         }
 
         // exit commands comes back to main menu.
         if (c == CMD_EXIT) {
             this.midlet.display.setCurrent(this.midlet.mainMenu);
         }
 
         // save command
         if (c == CMD_SAVE) {
 
             Alert alert;
 
             // check whether all fields have been completed
             // if not, we alert and don't do anything else.
             if (!this.isComplete()) {
                 alert = new Alert("Données manquantes", "Tous les champs doivent etre remplis!", null, AlertType.ERROR);
                 alert.setTimeout(Alert.FOREVER);
                 this.midlet.display.setCurrent (alert, this);
                 return;
             }
 
             // create a report object from values
             MalariaStockOutsReport stock_outs = new MalariaStockOutsReport();
             stock_outs.setAll(fieldValue(stockout_act_children),
                               fieldValue(stockout_act_youth),
                               fieldValue(stockout_act_adult),
                               fieldValue(stockout_arthemeter),
                               fieldValue(stockout_quinine),
                               fieldValue(stockout_serum),
                               fieldValue(stockout_bednet),
                               fieldValue(stockout_rdt),
                               fieldValue(stockout_sp));
             // check for errors and display first error
             if (!stock_outs.dataIsValid()) {
                 alert = new Alert("Données incorectes!", stock_outs.errorMessage(), null, AlertType.ERROR);
                 alert.setTimeout(Alert.FOREVER);
                 this.midlet.display.setCurrent (alert, this);
                 return;
             }
 
             // data appears to be valid now. Let's save it.
             stock_outs.saveInStore();
             // refresh menu as we've changed data.
             this.midlet.refreshMenu();
 
             // mark report in progress
             config.set("has_data", "true");
 
             // Confirm data is OK and go to main menu
             alert = new Alert("Enregitré", "Les données de ruptures de stock ont été enregistrées", null, AlertType.CONFIRMATION);
             alert.setTimeout(Alert.FOREVER);
             this.midlet.display.setCurrent (alert, this.midlet.mainMenu);
         }
     }
 }
