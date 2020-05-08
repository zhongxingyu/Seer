 /**
  * HBCIImporter.java
  * Created on 26.07.2008
  * (c) 2005 by "Wolschon Softwaredesign und Beratung".
  *
  * Permission is granted to use, modify,
  * publish and sub-license this code as specified in the contract. If nothing
  * else is specified these rights are given non-exclusively with no restrictions
  * solely to the contractor(s). If no specified otherwise I reserve the right to
  * use, modify, publish and sub-license this code to other parties myself.
  * -----------------------------------------------------------
  * major Changes:
  * 26.07.2008 - initial version ...
  */
 package biz.wolschon.finance.jgnucash.HBCIImporterNew;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 import javax.xml.bind.JAXBException;
 
 import org.kapott.hbci.callback.HBCICallback;
 import org.kapott.hbci.structures.Saldo;
 
 import biz.wolschon.fileformats.gnucash.GnucashWritableAccount;
 import biz.wolschon.fileformats.gnucash.GnucashWritableFile;
 import biz.wolschon.finance.jgnucash.AbstractScriptableImporter.AbstractScriptableImporter;
 import biz.wolschon.finance.jgnucash.plugin.PluginConfigHelper;
 import biz.wolschon.numbers.FixedPointNumber;
 
 /**
  * Use HBCI4Java to import transactions via HBCI from a bank into a given
  * account.
  * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
  */
 public class HBCIImporter extends AbstractScriptableImporter {
 
     /**
      * Internally HBCI4Java represents monetary values as cent,
      * so we need to divity by this constant on 100.
      */
     private static final BigDecimal CENTPEREURO = new BigDecimal(100);
 
 
     /**
      * Automatically created logger for debug and error-output.
      */
     private static final Logger LOG = Logger.getLogger(HBCIImporter.class.getName());
 
     /**
      * Keys to the settings that absolutele must be set and have another value
      * then in the default-properties.
      */
     private static final String[] ALLSETTINGS = new String[] {
             HBCIImporter.SETTINGS_ACCOUNT,
             HBCIImporter.SETTINGS_GNUCASHACCOUNT,
             HBCIImporter.SETTINGS_PIN ,
             HBCIImporter.SETTINGS_BANKCODE,
             HBCIImporter.SETTINGS_COUNTRY,
             HBCIImporter.SETTINGS_SERVER
     };
 
     /**
      * Keys to the settings that absolutele must be set and have another value
      * then in the default-properties.
      */
     private static final String[] REQUIREDSETTINGS = new String[] {
             HBCIImporter.SETTINGS_ACCOUNT,
             HBCIImporter.SETTINGS_GNUCASHACCOUNT,
             // not required HBCIImporter.SETTINGS_PIN ,
             HBCIImporter.SETTINGS_BANKCODE, HBCIImporter.SETTINGS_COUNTRY,
             HBCIImporter.SETTINGS_ACCOUNT, HBCIImporter.SETTINGS_SERVER // ,
     // not required HBCIImporter.SETTINGS_DEFAULTTARGETACCOUNT
     };
 
     /**
      * Names to the settings that absolutele must be set and have another value
      * then in the default-properties.
      */
     private static final String[] REQUIREDSETTINGNAMES = new String[] {
             "Your account-number with your bank.",
             "Your bank-account name in Gnucash.",
             // not required HBCIImporter.SETTINGS_PIN ,
             "Your bank-code/BLZ", "Your country, e.g. 'DE'",
             "Your bank-account-number/KNR",
             "Your HBCI-URL (without leading https://)" // ,
     // not required HBCIImporter.SETTINGS_DEFAULTTARGETACCOUNT
     };
 
 
     /**
      * The key for the setting of the pin-code.
      * @see #myProperties
      */
     public static final String SETTINGS_PIN = "hbci.pin";
 
     /**
      * The key for the setting of the bank-number (German: BLZ) .
      * @see #myProperties
      */
     public static final String SETTINGS_BANKCODE = "hbci.bankcode";
 
     /**
      * The key for the setting of the country (defaults to "DE") .
      * @see #myProperties
      */
     public static final String SETTINGS_COUNTRY = "hbci.country";
 
     /**
      * The key for the setting of the account-number .
      * @see #myProperties
      */
     public static final String SETTINGS_GNUCASHACCOUNT = "hbci.gnucashaccountid";
 
     /**
      * The key for the setting of the account-number .
      * @see #myProperties
      */
     public static final String SETTINGS_ACCOUNT = "hbci.account";
 
     /**
      * The key for the setting of the hbci-url (without leading "https://") .
      * @see #myProperties
      */
     public static final String SETTINGS_SERVER = "hbci.server";
 
 
 
 
 
     /**
      * Import all transactions that are not yet in the account.
      * @see #myAccount
      */
     public void synchronizeAllTransactions() {
 
         File pintanfile = createPinTanFile();
 
         try {
             HBCICallback callback = new PropertiesHBCICallback(this
                     .getMyProperties());
 
             org.kapott.hbci.manager.HBCIUtils.init(null, null, callback);
 
             org.kapott.hbci.manager.HBCIUtils.setParam("log.loglevel.default",
                     "" + org.kapott.hbci.manager.HBCIUtils.LOG_WARN);
 
             org.kapott.hbci.manager.HBCIUtils.setParam("client.passport.default", "PinTan");
 
             org.kapott.hbci.manager.HBCIUtils.setParam(
                     "client.passport.PinTan.filename", pintanfile.getAbsolutePath());
             org.kapott.hbci.manager.HBCIUtils.setParam(
                     "client.passport.PinTan.init", "1");
 
             org.kapott.hbci.passport.HBCIPassport passport = org.kapott.hbci.passport.AbstractHBCIPassport
                     .getInstance();
 
             String pversion = passport.getHBCIVersion();
             if (pversion == null || pversion.length() < 1) {
                 pversion = "plus";
             }
 
             LOG.fine("hbci-version=" + pversion);
 
             org.kapott.hbci.manager.HBCIHandler handler = new org.kapott.hbci.manager.HBCIHandler(
                     pversion, passport);
 
             org.kapott.hbci.structures.Konto[] accs = passport.getAccounts();
             if (accs == null) {
                 LOG.fine("passport.getAccounts()=null");
             } else {
                 LOG.fine("passport.getAccounts()="
                         + java.util.Arrays.toString(accs));
             }
             org.kapott.hbci.GV.HBCIJob job = handler.newJob("KUmsAll");
 
             /*
              * job.setParam("src",src); job.setParam("dst",dst);
              * job.setParam("name",applet.dstname.getText());
              * job.setParam("btg",btg)/;
              */
 
             job.addToQueue();
             // handler.addJob(job);
 
             org.kapott.hbci.status.HBCIExecStatus status = handler.execute();
             if (!status.isOK()) {
                 LOG.severe("Error synchronizing transactions from HBCI."
                         + " Status=" + status.getErrorString());
                 return;
             }
             org.kapott.hbci.GV_Result.HBCIJobResult result = job.getJobResult();
 
             if (accs == null) {
                 LOG.info("no result on fetching HBCI-transactions");
             } else {
 
                 if (result instanceof org.kapott.hbci.GV_Result.GVRKUms) {
                     org.kapott.hbci.GV_Result.GVRKUms r = (org.kapott.hbci.GV_Result.GVRKUms) result;
                     org.kapott.hbci.GV_Result.GVRKUms.UmsLine[] flatData = r
                             .getFlatData();
 
                     Date lastImportedDate = null;
                     StringBuilder finalMessage = new StringBuilder();
                     for (int i = 0; i < flatData.length; i++) {
 
                         StringBuilder message = new StringBuilder();
                         for (String element : flatData[i].usage) {
                             message.append('\n').append(element);
                         }
                         message.replace(0, 1, "");
 
                         Date valutaDate = flatData[i].valuta;
                         FixedPointNumber value = new FixedPointNumber(flatData[i].value
                                 .getLongValue())
                                 .divideBy(CENTPEREURO);
                         if (isTransactionPresent(valutaDate, value, message.toString())) {
                             continue;
                         }
 
                         LOG.info("--------------importing------------------------------(" + flatData[i].valuta + ")\n"
                         +        "value=" + value + "\n"
                         +        "saldo=" + flatData[i].saldo.value.getLongValue() + "\n"
 //                        System.err.println("saldo at " + flatData[i].saldo.timestamp);
 //                        System.err.println("saldo=" + flatData[i].saldo.value.getLongValue() + " (" + flatData[i].saldo.value.getCurr()  + ")=" + flatData[i].saldo.value.getDoubleValue());
                         +        "date=" + flatData[i].valuta + "\n"
                         +        "UmsLine[" + i + "] Message="
                                 + message.toString());
                         // import this transaction
                         importTransaction(flatData[i].valuta,
                                     (new FixedPointNumber(flatData[i].value
                                             .getLongValue()))
                                             .divideBy(CENTPEREURO),
                                     message.toString());
 
                         finalMessage.append(flatData[i].valuta).append(" ")
                                     .append(value.toString())
                                     .append(" - ").append(message.toString())
                                     .append("\n");
 
                             // create a saldo-transaction for the last imported
                             // transaction of each day
                         if (lastImportedDate != null
                             && !lastImportedDate.equals(flatData[i].valuta)) {
                             Saldo saldo = flatData[i - 1].saldo;
                             long saldoInCent = saldo.value
                                                   .getLongValue();
                             createSaldoEntry((new FixedPointNumber(saldoInCent)).divideBy(CENTPEREURO),
                                                   saldo.timestamp);
                             markNonExistingTransactions(lastImportedDate);
                         }
                         lastImportedDate = flatData[i].valuta;
                     }
 
                     // create a saldo-transaction after the last imported
                     // transaction
                     if (lastImportedDate != null) {
                         Saldo saldo = flatData[flatData.length - 1].saldo;
                         long saldoInCent = saldo.value
                                               .getLongValue();
                         createSaldoEntry((new FixedPointNumber(saldoInCent)).divideBy(CENTPEREURO),
                                               saldo.timestamp);
                         markNonExistingTransactions(lastImportedDate);
                     }
                     if (finalMessage.length() > 0) {
                         JOptionPane.showMessageDialog(null,
                                 "Imorted Transactions:\n"
                                         + finalMessage.toString());
                     }
                 } else {
                     LOG.warning("result is no on org.kapott.hbci.GV_Result.GVRKUms but a  ["
                                     + result.getClass().getName()
                                     + "] = "
                                     + result);
                 }
             } // accs != null
 
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Error synchronizing transactions from HBCI.", e);
         } finally {
             pintanfile.delete();
         }
     }
 
 
 
     /**
      * Create a temporary PinTan-file for HBCI4Java
      * @return a file
      */
     private File createPinTanFile() {
         File pintanfile;
         try {
             pintanfile = File.createTempFile("pintan", "hbci");
             pintanfile.deleteOnExit();
             pintanfile.delete(); // the file MUST not exist
         } catch (IOException e1) {
             LOG.log(Level.SEVERE, "[IOException] Problem in "
                        + getClass().getName(),
                          e1);
             // fall back to a default-filename
             pintanfile = new File("/tmp/pintan." + Math.random());
         }
         return pintanfile;
     }
 
 
 
     @Override
     public String runImport(final GnucashWritableFile aWritableModel,
                             final GnucashWritableAccount aCurrentAccount)
                                                                    throws Exception {
         Properties defaultSettings = new Properties();
         defaultSettings
                 .load(getClass()
                         .getClassLoader()
                         .getResourceAsStream(
                                 "biz/wolschon/finance/jgnucash/HBCIImporterNew/default_hbci.properties"));
 
         // get all accounts that contain contact-information
         // for an HBCI online-banking server of the respective
         // bank
         Collection<GnucashWritableAccount> hbciAccounts = PluginConfigHelper.getAllAccountsWithKey(aWritableModel, "hbci");
         if (hbciAccounts != null && hbciAccounts.size() > 0) {
             for (GnucashWritableAccount hbciAccount : hbciAccounts) {
                 LOG.info("synchronizing HBCI-account " + hbciAccount.getId() + "=\"" + hbciAccount.getName() + "\"");
                 setMyProperties(hbciAccount);
                 setMyAccount(hbciAccount);
                 boolean ok = askRequiresSettings(defaultSettings,
                         hbciAccount, aWritableModel);
                 if (ok) {
                     synchronizeAllTransactions();
                 }
             }
             return null;
         }
 
 
         GnucashWritableAccount hbciAccount = PluginConfigHelper.getOrConfigureAccountWithKey(aWritableModel, "hbci", "yes",
                 "Please select the account representing\n"
                 + " your bank-account in Gnucash.\n"
                 + "You can add additional accounts later by setting the user-defined poperty hbci=yes on them.");
         if (hbciAccount == null) {
             return null;
         }
 
 
         ////////////////////////////////
         // try to import an old config-file
         // into the new format of account-properties
         Properties settings = new Properties();
         File configfile = getConfigFile();
         if (configfile.exists()) {
             settings.load(new FileReader(configfile));
             for (String key : ALLSETTINGS) {
                 String value = settings.getProperty(key, "");
                 hbciAccount.setUserDefinedAttribute(key, value);
             }
         }
 
 
         // ask all missing settings
         boolean ok = askRequiresSettings(defaultSettings,
                 hbciAccount, aWritableModel);
 
         if (ok) {
             // run the actual import.
             setMyAccount(hbciAccount);
             setMyProperties(hbciAccount);
             synchronizeAllTransactions();
         }
 
         return null;
     }
 
     /**
      * Clear our settings and add only the attributes
      * of the given account to the settings.
      * @param aHbciAccount the account
      */
     private void setMyProperties(final GnucashWritableAccount aHbciAccount) {
        Properties prop = new Properties() {
              @Override
             public Object setProperty(String key,
                          String value) {
                try {
                   aHbciAccount.setUserDefinedAttribute(key, value);
                } catch (javax.xml.bind.JAXBException e) {
                   throw new RuntimeException("cannot set property in account", e);
                }
                return super.setProperty(key, value);
             }
        };
 
         LOG.log(Level.INFO, "Loading properties from gnucash-account " + aHbciAccount.getQualifiedName());
         Collection<String> keys = aHbciAccount.getUserDefinedAttributeKeys();
         for (String key : keys) {
             LOG.log(Level.INFO, "Loading from gnucash-account key: "
                     + key);
             String value = aHbciAccount.getUserDefinedAttribute(key);
             prop.setProperty(key, value);
         }
 
         // migration
         try {
             // set all properties from old config file
             File oldfile = getConfigFile();
             if (oldfile != null) {
                 LOG.log(Level.INFO, "Importing old config file:"
                         + oldfile.getAbsolutePath() + " to new config");
                 Properties oldprop = new Properties();
                 oldprop.load(new FileReader(oldfile));
                 for (Object keyo : oldprop.keySet()) {
                     String key = keyo.toString();
                     if (prop.contains(key)) {
                         continue;
                     }
                     LOG.log(Level.INFO, "Importing old config key "
                             + key + " to new config in gnucash-account");
                     String value = oldprop.getProperty(key);
                     prop.setProperty(key, value);
                     aHbciAccount.setUserDefinedAttribute(key, value);
                 }
                 oldfile.renameTo(new File(oldfile.getAbsolutePath() + ".migrated"));
             } else {
                 LOG.log(Level.INFO, "No old config file to import into new config");
             }
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Problem in "
                     + getClass().getName() + " while loading old config file",
                     e);
         }
 
         super.setMyProperties(prop);
     }
 
 
 
     /**
      * @param settings
      *            settings currently in effect
      * @param defaultSettings
      *            default-values for settings
      * @param aCurrentAccount
      *            the currently selected account (may be null)
      * @param aWritableModel the current file we are working on
      * @return true if all is ready for action
      * @throws IOException
      *             if we cannot write the config-file
      * @throws JAXBException on issues with the backend
      */
     private boolean askRequiresSettings(final Properties defaultSettings,
                                         final GnucashWritableAccount aCurrentAccount,
                                         final GnucashWritableFile aWritableModel)
                                                                                      throws IOException, JAXBException {
         boolean ok = true;
         for (int i = 0; i < REQUIREDSETTINGS.length; i++) {
             String key = REQUIREDSETTINGS[i];
             String value = aCurrentAccount.getUserDefinedAttribute(key);
             if (value == null
                     || value.trim().length() == 0
                     || value
                     .equalsIgnoreCase(
                             defaultSettings.getProperty(key))) {
 
                 if (value == null) {
                     value = defaultSettings.getProperty(key);
                 }
                 String input = JOptionPane.showInputDialog("Please enter\n"
                         + REQUIREDSETTINGNAMES[i]
                         + "\nYou can later edit these values the properties of the account\n"
                         + aCurrentAccount.getName()
                         + value);
                 if (input != null && input.trim().length() > 0) {
                     aCurrentAccount.setUserDefinedAttribute(key, input);
                 } else {
                     ok = false;
                 }
             }
         } // for
 
         return ok;
     }
 
 
 
     @Override
     public String getPluginName() {
         return "hbci";
     }
 
 }
