 
 package pnlp;
 import javax.microedition.lcdui.*;
 import pnlp.PNLPMIDlet.*;
 import pnlp.Configuration.*;
 import pnlp.SMSSender.*;
 import pnlp.MalariaReport.*;
 
 /**
  * Request per-report meta data and fire SMS
  * @author rgaudin
  */
 public class SendReportForm extends Form implements CommandListener {
 
     private static final Command CMD_EXIT = new Command ("Retour", Command.BACK, 1);
     private static final Command CMD_SEND = new Command ("Envoyer", Command.OK, 1);
     private static final Command CMD_HELP = new Command ("Aide", Command.HELP, 2);
     private static final int MAX_SIZE = 8; // max no. of chars per field.
 
     private PNLPMIDlet midlet;
 
     private static final String[] monthList= {" --- ", "Janvier (01)", "Février (02)", "Mars (03)", "Avril (04)", "Mai (05)", "Juin (06)", "Juillet (07)", "Aout (08)", "Septembre (09)", "Octobre (10)", "Novembre (11)", "Décembre (12)"};
     private static final String[] yearList = {" --- ", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020"};
 
    private static final StringItem intro = new StringItem(null, "Indiquez la période concernée et donnez vos identifiants pour envoyer le rapport.");
     private ChoiceGroup monthField;
     private ChoiceGroup yearField;
     private TextField usernameField;
     private TextField passwordField;
 
     private Configuration config;
 
     public SendReportForm(PNLPMIDlet midlet) {
         super("Envoi du rapport mensuel");
         this.midlet = midlet;
 
         config = new Configuration();
 
         monthField = new ChoiceGroup("Mois:", ChoiceGroup.POPUP, monthList, null);
         yearField = new ChoiceGroup("Année:", ChoiceGroup.POPUP, yearList, null);
         usernameField = new TextField("Identifiant", null, MAX_SIZE, TextField.NON_PREDICTIVE);
         passwordField = new TextField("Mot de passe", null, MAX_SIZE, TextField.SENSITIVE);
 
        append(intro);
        append(monthField);
        append(yearField);
        append(usernameField);
        append(passwordField);
 
         addCommand(CMD_SEND);
         addCommand(CMD_EXIT);
         addCommand(CMD_HELP);
         this.setCommandListener(this);
     }
 
     /*
      *
      * @return <code>true</code> if fields are properly field for sending
      * <code>false</code> otherwise.
      */
     public boolean canSubmit() {
         if (usernameField.getString().length() >= 4 &&
             passwordField.getString().length() >= 6) {
             return true;
         }
         return false;
     }
 
     public void commandAction(Command c, Displayable d) {
         // Help command displays Help Form
         if (c == CMD_HELP) {
             HelpForm h = new HelpForm(this.midlet, this, "send_report");
             this.midlet.display.setCurrent(h);
         }
 
         // exit command goes back to main Menu.
         if (c == CMD_EXIT) {
             this.midlet.display.setCurrent(this.midlet.mainMenu);
         }
 
         // send command sends the SMS or displays errors.
         if (c == CMD_SEND) {
 
             Alert alert;
 
             // create the report
             MalariaReport report = new MalariaReport();
             report.username = usernameField.getString();
             report.password = passwordField.getString();
             report.month = monthField.getSelectedIndex();
             try {
                 report.year = Integer.parseInt(yearField.getString(yearField.getSelectedIndex()));
             } catch (java.lang.NumberFormatException e) {
                 report.year = -1;
             }
 
             System.out.println(report.toSMSFormat());
 
             // check validity and exit if it fails
             if (!(report.dataIsValid())) {
                 alert = new Alert ("Informations incorrectes.", report.errorMessage(), null, AlertType.ERROR);
                 this.midlet.display.setCurrent (alert, this);
                 return;
             }
 
             // save username is DB
             config.set("username", report.username);
 
             // sends the sms and reply feedback
             SMSSender sms = new SMSSender();
             String number = config.get("server_number");
             if (sms.send(number, report.toSMSFormat())) {
                 alert = new Alert ("Demande envoyée !", "Vous allez recevoir une confirmation du serveur.", null, AlertType.CONFIRMATION);
                 this.midlet.display.setCurrent (alert, this.midlet.mainMenu);
             } else {
                 alert = new Alert ("Echec d'envoi SMS", "Impossible d'envoyer la demande par SMS.", null, AlertType.WARNING);
                 this.midlet.display.setCurrent (alert, this);
             }
             
         }
     }
 }
