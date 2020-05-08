 /**
  * AbstractScriptableImporter.java
  * created: 09.10.2008 07:03:41
  * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
  * This file is part of jGnucashLib-GPL by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
  * You can purchase support for a sensible hourly rate or
  * a commercial license of this file (unless modified by others) by contacting him directly.
  *
  *  jGnucashLib-GPL is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  jGnucashLib-GPL is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with jGnucashLib-GPL.  If not, see <http://www.gnu.org/licenses/>.
  *
  ***********************************
  * Editing this file:
  *  -For consistent code-quality this file should be checked with the
  *   checkstyle-ruleset enclosed in this project.
  *  -After the design of this file has settled it should get it's own
  *   JUnit-Test that shall be executed regularly. It is best to write
  *   the test-case BEFORE writing this class and to run it on every build
  *   as a regression-test.
  */
 package biz.wolschon.finance.jgnucash.AbstractScriptableImporter;
 
 //other imports
 import java.awt.Dialog.ModalityType;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.script.ScriptContext;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.xml.bind.JAXBException;
 
 import biz.wolschon.fileformats.gnucash.GnucashTransactionSplit;
 import biz.wolschon.fileformats.gnucash.GnucashWritableAccount;
 import biz.wolschon.fileformats.gnucash.GnucashWritableFile;
 import biz.wolschon.fileformats.gnucash.GnucashWritableTransaction;
 import biz.wolschon.fileformats.gnucash.GnucashWritableTransactionSplit;
 import biz.wolschon.finance.jgnucash.AbstractScriptablePlugin.ScriptEditorPanel;
 import biz.wolschon.finance.jgnucash.plugin.ImporterPlugin;
 import biz.wolschon.numbers.FixedPointNumber;
 
 
 /**
  * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
  * Project: jGnucashLib-GPL<br/>
  * AbstractScriptableImporter.java<br/>
  * created: 09.10.2008 07:03:41 <br/>
  *<br/><br/>
  * <b>Abstract class to import transactions to an account</b>
  * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
  */
 public abstract class AbstractScriptableImporter extends org.java.plugin.Plugin implements ImporterPlugin {
 
     /**
      * Automatically created logger for debug and error-output.
      */
     private static final Logger LOG = Logger.getLogger(AbstractScriptableImporter.class.getName());
 
     /**
      * The account we are importing to.
      */
     private GnucashWritableAccount myAccount;
 
     /**
      * The settings like account-number, pin-code, bank.url, ....
      * @see #SETTINGS_ACCOUNT
      * @see #SETTINGS_BANKCODE
      * @see #SETTINGS_COUNTRY
      * @see #SETTINGS_PIN
      * @see #SETTINGS_SERVER
      */
     private Properties myProperties;
 
     /**
      * The key-prefix for the setting of a script-file-name.<br/>
      * To add 3 scripts to the config add 3 settings:<br/>
      * SETTINGS_PREFIX_IMPORTSCRIPT + "0",<br/>
      * SETTINGS_PREFIX_IMPORTSCRIPT + "1" ans <br/>
      * SETTINGS_PREFIX_IMPORTSCRIPT + "2",<br/>
      * The value of this property is either the file-name or a name to be
      * loaded via {@link ClassLoader#getResourceAsStream(String)}.
      * @see #myProperties
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT_ACCOUNT
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT_LANGUAGE
      */
     public static final String SETTINGS_PREFIX_IMPORTSCRIPT = ".import.import.scriptfile";
 
     /**
      * The key-prefix for the setting of a regexp.<br/>
      * Each added script must have a regexp. If the text of the transaction
      * matches the regexp this and no other script will be executed.
      * @see #myProperties
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT_ACCOUNT
      */
     public static final String SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP = ".import.import.regexp";
 
     /**
      * The JSR223 language-name of the script. (defaults to "Javascript".
      * @see #myProperties
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT_ACCOUNT
      */
     public static final String SETTINGS_PREFIX_IMPORTSCRIPT_LANGUAGE = ".import.import.scriptlanguage";
 
     /**
      * Instead of a full script, you can overside the accountid of
      * {@link #SETTINGS_DEFAULTTARGETACCOUNT}.
      * @see #myProperties
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP
      * @see #SETTINGS_PREFIX_IMPORTSCRIPT
      */
     public static final String SETTINGS_PREFIX_IMPORTSCRIPT_ACCOUNT = ".import.import.accountidoverride";
 
     /**
      * Setting of "true" or "false" if we are to ask the user if he wants to
      * create a new script when no script is matching.
      */
     private static final String SETTINGS_ASKTOCREATEMISSINGSCRIPTS = ".ask_to_create_missing_scripts";
 
     /**
      * The key for the setting of the id of the account that imported
      * transactions will be booked to/from by default. .
      * @see #myProperties
      */
     public static final String SETTINGS_DEFAULTTARGETACCOUNT = ".import.defaultTargetAccountID";
 
 
     public abstract String getPluginName();
 
     /**
      * Just an overridden ToString to return this classe's name
      * and hashCode.
      * @return className and hashCode
      */
     @Override
     public String toString() {
         return "AbstractScriptableImporter@" + hashCode();
     }
     /**
      * @param text
      *            the description-text ("usage"-field on a paper-form) of the
      *            transaction.
      * @param value
      *            Wert in der Währung des Kontos
      * @param myAccountSplit
      *            a split for the transaction-part involving the bank's account.
      * @param scriptnum
      *            number of the script (only used for debug+error -output)
      * @param language
      *            the language of the script (e.g. "JavaScript")
      * @param scriptPath
      *            path to the script (only used for debug+error -output)
      * @param reader
      *            where to get the source-code of the script to run from
      */
     protected void runImportScript(final FixedPointNumber value,
                                           final String text,
                                           final GnucashWritableTransactionSplit myAccountSplit,
                                           final int scriptnum,
                                           final String language,
                                           final String scriptPath,
                                           final Reader reader) {
         ScriptEngineManager mgr = new ScriptEngineManager();
 
         ScriptEngine jsEngine = mgr.getEngineByName(language);
 
         jsEngine.put("text", text);
         jsEngine.put("value", value);
         jsEngine.put("transaction", myAccountSplit.getTransaction());
         jsEngine.put("myAccountSplit", myAccountSplit);
         jsEngine.put("file", myAccountSplit.getWritableGnucashFile());
         FixedPointNumber ustValue = ((FixedPointNumber) value.clone())
                 .divideBy(new FixedPointNumber("-1,19")).multiply(
                         new FixedPointNumber("0,19")); // TODO: get tax-% from
         // tax-config
         FixedPointNumber nettoValue = ((FixedPointNumber) value.clone())
                 .negate().subtract(ustValue);
         jsEngine.put("USt", ustValue);
         jsEngine.put("Netto", nettoValue);
         jsEngine.put("Helper", new ScriptHelper());
         jsEngine.getContext().setAttribute(ScriptEngine.FILENAME, scriptPath,
                 ScriptContext.GLOBAL_SCOPE);
 
         try {
             LOG.info("importing transaction using script: " + scriptPath);
             jsEngine.eval(reader);
         } catch (Exception ex) {
             LOG.log(Level.SEVERE, "Error executing script number " + scriptnum
                     + " from " + scriptPath, ex);
             JOptionPane.showMessageDialog(null,
                     "Error executing user HBCI-Import-Script #" + scriptnum
                             + " '" + scriptPath + "':\n " + ex.getMessage()
                             + "\ntransaction (value is " + value + ") \n"
                             + text, scriptPath, JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Import the given transaction into the account.
      * @param text
      *            the description-text ("usage"-field on a paper-form) of the
      *            transaction.
      * @param date
      *            effective date of the transaction
      * @param value
      *            Wert in der W�hrung des Kontos
      * @return the transaction that involves this account.
      * @see #myAccount
      */
     public GnucashWritableTransaction importTransaction(final Date date,
                                    final FixedPointNumber value,
                                    final String text) {
 
         try {
             GnucashWritableTransaction transaction = getMyAccount()
                     .getWritableGnucashFile().createWritableTransaction();
             transaction.setDescription(text.replace('\n', ' '));
             transaction.setDatePosted(date);
             transaction.setCurrencyNameSpace(getMyAccount()
                     .getCurrencyNameSpace());
             transaction.setCurrencyID(getMyAccount().getCurrencyID());
 
             // we always need the split on out account's side
             GnucashWritableTransactionSplit myAccountSplit = transaction
                     .createWritingSplit(getMyAccount());
             myAccountSplit.setValue(value);
             myAccountSplit.setQuantity(value);
             myAccountSplit.setUserDefinedAttribute(getPluginName() + ".orig_description",
                     text);
             myAccountSplit.setDescription(text.replace('\n', ' '));
 
             GnucashWritableAccount otherAccount = getDefaultAccount();
             String otherAccountText = "automatically added";
 
             boolean scriptMatched = false;
             try {
                 // look if there is a script matching,
                 int scriptnum = 0;
                 LOG.log(Level.FINEST, "Testing scripts for a match");
                 while (getMyProperties().containsKey(
                         getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP + scriptnum)) {
                     String scriptFilenameKey = getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT + scriptnum;
                     String scriptAccountKey = getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT_ACCOUNT + scriptnum;
                     LOG.log(Level.FINEST, "Testing script number " + scriptnum + " for a match");
 
                     String regexp = getMyProperties().getProperty(
                             getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP + scriptnum);
                     if (text.matches(regexp)) {
                         scriptMatched = true;
 
                         // does a full script exist or only an alternate
                         // accountid?
                         if (getMyProperties().containsKey(scriptFilenameKey)) {
                             importViaScript(value, text, myAccountSplit, scriptnum);
                         } else {
 
                             String alternateAccountID = getMyProperties()
                                     .getProperty(scriptAccountKey);
                             otherAccount = getMyAccount().getWritableGnucashFile()
                                     .getAccountByID(alternateAccountID);
 
                             if (otherAccount == null) {
                                 LOG.log(Level.SEVERE, "Error  Account " + otherAccount
                                         + " given in user-" + getPluginName() + "-rule #"
                                         + scriptnum + " not found");
                                 JOptionPane
                                         .showMessageDialog(
                                                 null,
                                                 "Error  Account "
                                                         + otherAccount
                                                         + " given in user-" + getPluginName() + "-rule #"
                                                         + scriptnum
                                                         + " not found",
                                                 "User-supplied target-account not found.",
                                                 JOptionPane.ERROR_MESSAGE);
                             } else {
                                 LOG.log(Level.INFO, "importing transaction using alternate target-account: "
                                                 + alternateAccountID
                                                 + "="
                                                 + otherAccount
                                                         .getQualifiedName());
                                 otherAccountText = "";
                             }
 
                         }
 
                         break;
                     }
 
                     scriptnum++;
                 }
             } catch (Exception e) {
                 LOG.log(Level.SEVERE,
                                 "Exception while importing a transaction via a script-handler.",
                                 e);
             } catch (java.lang.NoClassDefFoundError e) {
                 LOG.log(Level.SEVERE,
                                 "NoClassDefFoundError while importing a transaction. Maybe you are using JDK1.5 instead of 1.6+?",
                                 e);
             }
 
             if (!scriptMatched) {
                 Integer newScriptnum = createMissingScript(date, value, text);
                 if (newScriptnum != null) {
                     importViaScript(value, text, myAccountSplit, newScriptnum
                             .intValue());
                 }
             }
 
             // default-case and make sure that all transactions are balanced
             if (!transaction.isBalanced()) {
 
                 if (transaction.getSplitsCount() > 1) {
                     JOptionPane.showMessageDialog(null,
                             "Imported transaction is not balanced."
                                     + "\ntransaction (value is " + value
                                     + " but " + transaction.getBalance()
                                     + " is missing ) \n" + text,
                             "not balanced", JOptionPane.WARNING_MESSAGE);
                 }
 
                 GnucashWritableTransactionSplit otherSplit = transaction
                         .createWritingSplit(otherAccount);
                 FixedPointNumber negatedValue = transaction.getNegatedBalance();
                 otherSplit.setValue(negatedValue);
                 otherSplit.setQuantity(negatedValue);
                 otherSplit.setDescription(otherAccountText);
             }
             return transaction;
         } catch (JAXBException e) {
             LOG.log(Level.SEVERE, "Exception while importing a transaction", e);
         }
         return null;
     }
 
 
     /**
      * Run the given import-script on the given transaction to import
      * @param text
      *            the description-text ("usage"-field on a paper-form) of the
      *            transaction.
      * @param value
      *            value in the currency of the account
      * @param myAccountSplit
      *            a split for the transaction-part involving the bank's account.
      * @param scriptnum
      *            the number of the script to run
      */
     private void importViaScript(final FixedPointNumber value,
                                  final String text,
                                  final GnucashWritableTransactionSplit myAccountSplit,
                                  final int scriptnum) {
         String scriptLanguageKey = getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT_LANGUAGE + scriptnum;
         String scriptPathKey = getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT + scriptnum;
         String language = getMyProperties().getProperty(scriptLanguageKey, "JavaScript");
         String scriptPath = getMyProperties().getProperty(scriptPathKey);
         InputStream is = null;
         try {
             if (new File(scriptPath).exists()) {
                 is = new FileInputStream(scriptPath);
             } else if (new File(getConfigFileDirectory(), scriptPath)
                     .exists()) {
                 is = new FileInputStream(new File(
                         getConfigFileDirectory(), scriptPath));
             } else if (new File(System.getProperty("user.dir"), scriptPath)
                     .exists()) {
                 is = new FileInputStream(new File(
                         System.getProperty("user.dir"), scriptPath));
             } else {
                 is = this.getClass().getClassLoader().getResourceAsStream(
                         scriptPath);
             }
         } catch (FileNotFoundException e) {
             LOG.log(Level.SEVERE, "Error loading script number " + scriptnum, e);
             LOG.log(Level.SEVERE, "was not found in current path: " + new File(scriptPath).getAbsolutePath());
             LOG.log(Level.SEVERE, "was not found in config dir: " + (new File(getConfigFileDirectory(), scriptPath)).getAbsolutePath());
             LOG.log(Level.SEVERE, "was not found in home dir: " + (new File(System.getProperty("user.dir"), scriptPath)).getAbsolutePath());
             LOG.log(Level.SEVERE, "was not found in class loader");
             JOptionPane.showMessageDialog(null,
                     "Error, user- HBCI-Import-Script #" + scriptnum + " at '"
                             + scriptPath + "' cannot be loaded:\n"
                             + e.getMessage() + "\ntransaction (value is "
                             + value + ") \n" + text, scriptPath,
                     JOptionPane.ERROR_MESSAGE);
         }
 
         if (is != null) {
             Reader reader = new InputStreamReader(is);
             runImportScript(value, text, myAccountSplit, scriptnum, language,
                     scriptPath, reader);
         } else {
             LOG.log(Level.SEVERE, "Error  script number " + scriptnum + " at "
                     + scriptPath + " not found"
                     + "\ncurrent directory is " + System.getProperty("user.dir")
                     + "\nconfig directory is " + getConfigFileDirectory().getAbsolutePath());
             JOptionPane.showMessageDialog(null,
                     "Error, user- HBCI-Import-Script #" + scriptnum + " at '"
                             + scriptPath + "' not found"
                             + "\ntransaction (value is " + value + ") \n"
                             + text
                             + "\ncurrent directory is " + System.getProperty("user.dir")
                             + "\nconfig directory is " + getConfigFileDirectory().getAbsolutePath(),
                             scriptPath, JOptionPane.ERROR_MESSAGE);
         }
     }
 
 
     @Override
     protected void doStart() throws Exception {
         // ignore
 
     }
 
     @Override
     protected void doStop() throws Exception {
         // ignore
 
     }
     /**
      * We do not have a script for this transaction.<br/>
      * Ask the user to create one.<br/>
      * If no, return null. If yes create the script and, add it to the config
      * and save the config.
      * @param text
      *            the description-text ("usage"-field on a paper-form) of the
      *            transaction.
      * @param date
      *            effective date of the transaction
      * @param value
      *            Wert in der W�hrung des Kontos
      * @return The scriptnum of a newly created script or null.
      */
     private Integer createMissingScript(final Date date,
                                         final FixedPointNumber value,
                                         final String text) {
         if (!myProperties.getProperty(SETTINGS_ASKTOCREATEMISSINGSCRIPTS,
                 "true").equalsIgnoreCase("true")) {
             return null;
         }
 
         int answer = JOptionPane.showConfirmDialog(null,
                 "No script exists for the following transaction. Do you want to create one?\n"
                         + "Value: " + value.toPlainString() + "\n" + text
                         + "\n" + "(Never ask again by setting "
                         + SETTINGS_ASKTOCREATEMISSINGSCRIPTS + "=false)",
                 "Create script?", JOptionPane.YES_NO_OPTION);
         if (answer != JOptionPane.YES_OPTION) {
             return null;
         }
 
         int maxScriptnum = 0;
         while (getMyProperties().containsKey(
                 getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP + maxScriptnum)) {
             maxScriptnum++;
         }
 
         JDialog f = new JDialog(null,
                 "new Script (" + getPluginName() + "-import waiting to resume)",
                 ModalityType.APPLICATION_MODAL);
         ScriptEditorPanel editor = new ImportScriptEditorPanel(maxScriptnum, text,
                                                                date, value,
                                                                getMyProperties(), this);
         f.getContentPane().add(editor);
         f.pack();
         f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         f.setVisible(true);
 
         // if the script was saved, return it to be run
         if (!editor.wasCanceled()
                 && getMyProperties().containsKey(
                         getPluginName() + SETTINGS_PREFIX_IMPORTSCRIPT_REGEXP + maxScriptnum)) {
             return maxScriptnum;
         }
         return null;
     }
 
     /**
      * Mark all transactions of the given date that are not matched by an
      * HBCI-transaction and have a split-value != 0, so they can be quickly
      * identified by the user.
      * @param date
      */
     public void markNonExistingTransactions(final Date date) {
         GregorianCalendar input = new GregorianCalendar();
         input.setTimeInMillis(date.getTime());
         GregorianCalendar day = new GregorianCalendar(input
                 .get(GregorianCalendar.YEAR), input
                 .get(GregorianCalendar.MONTH), input
                 .get(GregorianCalendar.DAY_OF_MONTH));
         long from = day.getTimeInMillis();
         day.add(GregorianCalendar.DAY_OF_MONTH, 1);
         long to = day.getTimeInMillis();
 
         FixedPointNumber zero = new FixedPointNumber();
         final String WARNING = "[did not happen in " + getPluginName().toUpperCase() + "-account";
 
         // TODO: ein getTransactionSplits(fromDate, toDate) w�re praktisch
         List<? extends GnucashTransactionSplit> splits = getMyAccount()
                 .getTransactionSplits();
         for (GnucashTransactionSplit split : splits) {
             if (split.getTransaction().getDatePosted().getTime() < from) {
                 continue;
             }
             if (split.getTransaction().getDatePosted().getTime() >= to) {
                 continue;
             }
             if (!split.getQuantity().equals(zero)
                     && split.getUserDefinedAttribute( getPluginName() + ".orig_description") == null) {
                 String description = split.getTransaction().getDescription();
                 if (description.indexOf(WARNING) < 0) {
                     ((GnucashWritableTransaction) split.getTransaction())
                             .setDescription(description
                                     + WARNING
                                     + " on "
                                     + DateFormat.getDateInstance().format(
                                             new Date()) + "]");
                 }
             }
             ;
         }
     }
 
 
     /**
      * @return the configuration-file (need not exist yet)
      */
     protected File getConfigFile() {
         getConfigFileDirectory().mkdirs();
         return new File(getConfigFileDirectory(), "." + getPluginName() + ".properties");
     }
 
     /**
      * @return The directory where we store config-files.
      */
     protected File getConfigFileDirectory() {
         File testFirst = new File(System.getProperty("user.dir"), ".jgnucash");
         if (testFirst.exists()) {
             return testFirst;
         } else {
             LOG.log(Level.FINE, "getConfigFileDirectory() does not exist: " + testFirst.getAbsolutePath());
         }
         testFirst = new File(new File(System.getProperty("user.dir")).getParentFile().getAbsolutePath(), ".jgnucash");
         if (testFirst.exists()) {
             return testFirst;
         } else {
             LOG.log(Level.FINE, "getConfigFileDirectory() does not exist: " + testFirst.getAbsolutePath());
         }
         return new File(System.getProperty("user.home", "~"), ".jgnucash");
     }
 
     /**
      * Create a transaction that simply acts as a comment and shows the current
      * Saldo. This helps in finding missing or doubled transactions manually.
      * @param timestamp
      *            the date
      * @param aValue
      *            the saldo on that date
      */
     protected void createSaldoEntry(final FixedPointNumber aValue, final Date timestamp) {
 
         try {
             FixedPointNumber saldo = aValue;
 
             String saldoOKStr = "OK";
             if (!getMyAccount().getBalance(timestamp).equals(saldo)) {
                 saldoOKStr = "NAK ("
                         + getMyAccount().getBalance(timestamp).subtract(
                                 saldo) + ")";
             }
 
             GnucashWritableTransaction transaction = getMyAccount()
                     .getWritableGnucashFile().createWritableTransaction();
             transaction.setDescription("Saldo: " + aValue + " "
                     + saldoOKStr + " (imported via HBCI on " + DateFormat.getDateInstance().format(new Date()) + ")");
             transaction.setDatePosted(timestamp);
             transaction.setCurrencyNameSpace(getMyAccount()
                     .getCurrencyNameSpace());
             transaction.setCurrencyID(getMyAccount().getCurrencyID());
 
             // we always need the split on out account's side
             GnucashWritableTransactionSplit myAccountSplit = transaction
                     .createWritingSplit(getMyAccount());
             myAccountSplit.setValue(new FixedPointNumber()); // 0
             myAccountSplit.setQuantity(new FixedPointNumber()); // 0
 
             // dummy-split on the other side
             GnucashWritableAccount otherAccount = getDefaultAccount();
             GnucashWritableTransactionSplit otherAccountSplit = transaction
                     .createWritingSplit(otherAccount);
             otherAccountSplit.setValue(new FixedPointNumber()); // 0
             otherAccountSplit.setQuantity(new FixedPointNumber()); // 0
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Exception while creating Saldo-transaction.", e);
         }
 
     }
 
     /**
      * Get the default-account (something like "expenses-other").<br/>
      * If it does not exist, create one.
      * @return the default-accout
      * @throws JAXBException
      *             if account-creation fails-
      */
     protected GnucashWritableAccount getDefaultAccount() throws JAXBException {
         GnucashWritableFile writableFile = getMyAccount().getWritableGnucashFile();
         String accountID = getMyProperties().getProperty(
                 getPluginName() + SETTINGS_DEFAULTTARGETACCOUNT);
         GnucashWritableAccount account = accountID == null ? null
                 : (GnucashWritableAccount) writableFile.getAccountByIDorName(
                         accountID, accountID);
         if (account == null) {
             account = writableFile.getAccountByName("Ausgleichskonto-EUR");
         }
         if (account == null) {
             account = writableFile.createWritableAccount();
             account.setName("Ausgleichskonto-EUR");
         }
         return account;
     }
 
     /**
      * Determine if a transaction of this value has been made on or around the
      * given date. (Not very precise.)
      * @param date
      *            effective date of the transaction
      * @param value
      *            Wert in der Waehrung des Kontos
      * @see #myAccount
      * @return true if such a transaction exists
      */
     protected boolean isTransactionPresent(final Date date,
                                            final FixedPointNumber value,
                                            final String description) {
         final long dateDeltaMillis = 4 * 24 * 60 * 60 * 1000;
         long from = date.getTime() - dateDeltaMillis;
         long to = date.getTime() + 2 * dateDeltaMillis;
 
         // TODO: ein getTransactionSplits(fromDate, toDate) w�re praktisch
         List<? extends GnucashTransactionSplit> splits = getMyAccount()
                 .getTransactionSplits();
         int considered = 0;
         int notconsidered = 0;
         for (GnucashTransactionSplit split : splits) {
             if (split.getTransaction().getDatePosted().getTime() < from) {
                 notconsidered++;
                 continue;
             }
             if (split.getTransaction().getDatePosted().getTime() > to) {
                 notconsidered++;
                 continue;
             }
             considered++;
             if (split.getQuantity().equals(value)) {
                 String origDescr = split
                         .getUserDefinedAttribute(getPluginName() + ".orig_description");
                 if (origDescr != null
                         && !origDescr.equalsIgnoreCase(description)) {
                     // this transactions belongs to another HBCI-transaction
                     LOG.log(Level.FINE, "isTransactionPresent(date=" + date + ") not matching\n"
                             + "origDescr  =" + origDescr + "\n"
                             + "description=" + description);
                     continue;
                 }
                 if (origDescr == null) {
                     try {
                         ((GnucashWritableTransactionSplit) split)
                                 .setUserDefinedAttribute(
                                         getPluginName() + ".orig_description", description);
                         ((GnucashWritableTransaction) split.getTransaction())
                                 .setDatePosted(date);
                     } catch (JAXBException e) {
                         LOG.log(Level.WARNING, "[JAXBException] Problem in "
                                 + getClass().getName(), e);
                     }
                 }
                 return true;
             }
         }
 
         LOG.log(Level.FINE, "isTransactionPresent(date=" + date + ") = false considered " + considered + " transactions, ignored " + notconsidered + " not between " + new Date(from) + " and " + new Date(to) + " in account " + getMyAccount().getQualifiedName());
         return false;
     }
 
 
     /**
      * @return The account we are importing to.
      */
     public GnucashWritableAccount getMyAccount() {
         return myAccount;
     }
 
     /**
      * @param aAccount
      *            The account we are importing to.
      */
     public void setMyAccount(final GnucashWritableAccount aAccount) {
         if (aAccount == null) {
             throw new IllegalArgumentException("Null als Gnucash-Konto �bergeben");
         }
         myAccount = aAccount;
     }
 
     /**
      * @return The settings like account-number, pin-code, bank.url, ....
      * @see #SETTINGS_ACCOUNT
      * @see #SETTINGS_BANKCODE
      * @see #SETTINGS_COUNTRY
      * @see #SETTINGS_PIN
      * @see #SETTINGS_SERVER
      */
     public Properties getMyProperties() {
         if (myProperties == null) {
             myProperties = new Properties();
         }
         return myProperties;
     }
 
     /**
      * @param myProperties
      *            The settings like account-number, pin-code, bank.url, ....
      * @see #SETTINGS_ACCOUNT
      * @see #SETTINGS_BANKCODE
      * @see #SETTINGS_COUNTRY
      * @see #SETTINGS_PIN
      * @see #SETTINGS_SERVER
      */
     public void setMyProperties(final Properties myProperties) {
         this.myProperties = myProperties;
     }
 
 }
 
 
