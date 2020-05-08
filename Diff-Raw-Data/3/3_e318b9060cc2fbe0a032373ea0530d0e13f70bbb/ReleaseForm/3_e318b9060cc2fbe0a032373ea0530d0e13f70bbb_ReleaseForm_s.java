 package net.codjo.tools.farow;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import net.codjo.gui.toolkit.i18n.InternationalizationUtil;
 import net.codjo.gui.toolkit.swing.SwingWorker;
 import net.codjo.gui.toolkit.util.ErrorDialog;
 import net.codjo.i18n.common.Language;
 import net.codjo.i18n.common.TranslationManager;
 import net.codjo.i18n.gui.TranslationNotifier;
 import net.codjo.tools.farow.command.ArtifactSorter;
 import net.codjo.tools.farow.command.ArtifactType;
 import net.codjo.tools.farow.command.CleanInhouseSnapshot;
 import net.codjo.tools.farow.command.CleanUpDirectoryCommand;
 import net.codjo.tools.farow.command.Command;
 import net.codjo.tools.farow.command.CommandPlayer;
 import net.codjo.tools.farow.command.CopyPomCommand;
 import net.codjo.tools.farow.command.GetItCommand;
 import net.codjo.tools.farow.command.IdeaCommand;
 import net.codjo.tools.farow.command.LockRepoCommand;
 import net.codjo.tools.farow.command.MavenCommand;
 import net.codjo.tools.farow.command.NotifyCodjoUsersCommand;
 import net.codjo.tools.farow.command.PrepareAPomToLoadSuperPomDependenciesCommand;
 import net.codjo.tools.farow.command.RebuildNexusPomMetaDataCommand;
 import net.codjo.tools.farow.command.SetNexusProxySettingsCommand;
 import net.codjo.tools.farow.step.ArtifactStep;
 import net.codjo.tools.farow.step.Build;
 import net.codjo.tools.farow.step.DeleteRepo;
 import net.codjo.tools.farow.step.Publish;
 import net.codjo.tools.farow.step.Step;
 import net.codjo.tools.farow.step.Step.State;
 import net.codjo.tools.farow.util.GitConfigUtil;
 /**
  *
  */
 public class ReleaseForm {
     private JButton cancelButton;
     private JPanel mainPanel;
     private JList buildList;
     private JButton upButton;
     private JButton downButton;
     private JButton releaseButton;
     private JButton addBuildButton;
     private JButton removeBuildButton;
     private JTabbedPane displayTabbedPane;
     private JTextArea auditArea;
     private JButton finalizeButton;
     private JButton broadcastButton;
     private JButton importButton;
     private JButton sendMailButton;
     private JButton prepareButton;
     private JLabel informationLabel;
     private JButton sortButton;
     private JButton publishButton;
     private JButton deleteRepoButton;
     private DefaultListModel model = new DefaultListModel();
     private static final File DEFAULT_BUILD_LIST_FILE = new File(System.getProperty("java.io.tmpdir"),
                                                                  "stabilisationBuildList.txt");
     private GitConfigUtil proxy;
     private String frameworkVersion;
     private Properties properties;
 
 
     public ReleaseForm(Properties properties) {
         this.properties = properties;
         appendAuditRow("Ouverture de farow");
         initProxyInformation();
 
         initializeBuildListPanel();
 
         prepareButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 warnPrepareAndRelaseSuperPom();
             }
         });
         releaseButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 buildList.clearSelection();
                 try {
                     sortModel();
                 }
                 catch (Exception error) {
                     //
                 }
 
                 runBuild(getFirstUndoneBuild(), "Stabilisations");
             }
         });
 
         publishButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 publish();
             }
         });
         deleteRepoButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 deleteRepo();
             }
         });
 
         finalizeButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 runFinalizer();
             }
         });
         sendMailButton.setMnemonic('M');
         sendMailButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 sendMail();
             }
         });
         cancelButton.setMnemonic('Q');
         cancelButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 System.exit(0);
             }
         });
 
         getMainPanel().setPreferredSize(new Dimension(1200, 900));
     }
 
 
     private void initProxyInformation() {
         try {
             proxy = new GitConfigUtil();
         }
         catch (IOException e) {
             ErrorDialog.show(getMainPanel(),
                              "Erreur de chargement du fichier de configuration de git ({user.home}/.gitconfig",
                              e);
         }
     }
 
 
     private void initializeBuildListPanel() {
         buildList.setModel(model);
         buildList.setCellRenderer(new BuildRenderer());
 
         buildList.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent event) {
                 removeBuildButton.setEnabled(buildList.getSelectedIndex() != -1);
 
                 upButton.setEnabled(buildList.getSelectedIndex() > 0);
                 downButton.setEnabled(buildList.getSelectedIndex() != -1
                                       && buildList.getSelectedIndex() != model.getSize() - 1);
             }
         });
         addBuildButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 addBuild(askForBuild());
             }
         });
         removeBuildButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 model.removeElement(buildList.getSelectedValue());
             }
         });
         upButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 moveSelectedBuild(-1);
             }
         });
 
         downButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 moveSelectedBuild(+1);
             }
         });
 
         // Alt + et Alt -
         addBuildButton.setMnemonic(KeyEvent.VK_ADD);
         removeBuildButton.setMnemonic(KeyEvent.VK_SUBTRACT);
 /*
         // Alt Insert et Shift Suppre
         addBuildButton.setMnemonic(KeyEvent.VK_INSERT);
         removeBuildButton.setMnemonic(KeyEvent.VK_DELETE);
 */
 
         sortButton.setMnemonic('T');
         sortButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 try {
                     sortModel();
                 }
                 catch (Exception error) {
                     JOptionPane.showMessageDialog(mainPanel,
                                                   error.getMessage(),
                                                   "Oupss",
                                                   JOptionPane.ERROR_MESSAGE);
                 }
             }
         });
 
         importButton.setMnemonic('I');
         importButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 try {
                     importBuildList();
                 }
                 catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
         });
 
         broadcastButton.setMnemonic('E');
         broadcastButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 try {
                     broadcastBuildList();
                 }
                 catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }
         });
     }
 
 
     private void warnPrepareAndRelaseSuperPom() {
         prepareButton.setEnabled(false);
         CommandPlayer player = new CommandPlayer();
 
         player.add(new CleanUpDirectoryCommand(ArtifactType.SUPER_POM, ArtifactType.SUPER_POM.name()));
         player.add(new GetItCommand(ArtifactType.SUPER_POM, ""));
 
         player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "codjo:send-announcement-to-teams",
                                     "-DisStarting=true"));
         player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "codjo:update-confluence-before-release"));
 
         //TODO rajouter la gestion de l'option dans l'IHM ReleaseType eg -DreleaseType=patch
         player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "codjo:release",
                                     "-DstabilisationFileName=" + DEFAULT_BUILD_LIST_FILE.getAbsolutePath()));
         player.add(new ImportArtifactsCommand());
         player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "release:prepare",
                                     "-Ddocumentation=disabled"));
         player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "release:perform",
                                     "-Ddocumentation=disabled",
                                     ArtifactStep.getGitScmAdditionalParameter(ArtifactType.SUPER_POM, "unused"),
                                     ArtifactStep.getCodjoDeploymentParameter()));
 
         StepInvoker invoker = new StepInvoker("Premires tapes", player);
         invoker.start();
     }
 
 
     private void publish() {
         try {
             int result = JOptionPane.showConfirmDialog(mainPanel,
                                                        "Your going to publish results on github");
             if (result != 0) {
                 throw new Exception("Github publishing has been canceled by user.");
             }
             buildPublishList();
             runBuild(getFirstUndoneBuild(), "Publications");
         }
         catch (Exception exc) {
             TranslationManager translationManager = new TranslationManager();
             TranslationNotifier translationNotifier = new TranslationNotifier(Language.FR,
                                                                               translationManager);
             InternationalizationUtil.registerBundlesIfNeeded(translationManager);
             ErrorDialog.setTranslationBackpack(translationManager, translationNotifier);
             ErrorDialog.show(getMainPanel(), "", exc);
         }
     }
 
 
     private void deleteRepo() {
         try {
             int result = JOptionPane.showConfirmDialog(mainPanel,
                                                        "Your going to delete repos on github");
             if (result != 0) {
                 throw new Exception("Github repo delete has been canceled by user.");
             }
             buildDeleteRepoList();
             runBuild(getFirstUndoneBuild(), "Delete repos");
         }
         catch (Exception exc) {
             TranslationManager translationManager = new TranslationManager();
             TranslationNotifier translationNotifier = new TranslationNotifier(Language.FR,
                                                                               translationManager);
             InternationalizationUtil.registerBundlesIfNeeded(translationManager);
             ErrorDialog.setTranslationBackpack(translationManager, translationNotifier);
             ErrorDialog.show(getMainPanel(), "", exc);
         }
     }
 
 
     private void runFinalizer() {
         frameworkVersion = JOptionPane.showInputDialog("Merci de preciser le numro de version du framework");
         if (frameworkVersion == null || frameworkVersion.trim().isEmpty()) {
             JOptionPane.showMessageDialog(getMainPanel(),
                                           "Le numro de version du framework ne peut etre null ou vide",
                                           "Impossible de finaliser la Stabilisation",
                                           JOptionPane.ERROR_MESSAGE);
         }
         else {
             CommandPlayer player = new CommandPlayer();
             player.add(new CleanUpDirectoryCommand("C:\\Dev\\platform\\tools\\maven\\local\\maven2\\net\\codjo\\pom"));
             player.add(new LockRepoCommand());
             player.add(new CleanInhouseSnapshot());
             player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "deploy"));
             player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "deploy", ArtifactStep.REMOTE_CODJO));
             // Rapatriement des librairies postes sur repo.codjo.net avec maven et nexus
             player.add(new SetNexusProxySettingsCommand(proxy.getProxyUserName(),
                                                         proxy.getProxyPassword(),
                                                         properties));
 
             String codjoPomAllDepsDirectory = "pom-alldeps";
             String codjoPomAllDepsPath = ArtifactType.LIB.toArtifactPath(codjoPomAllDepsDirectory);
             String temporaryLocalRepository = ArtifactType.LIB.toArtifactPath("repo");
 
             player.add(new CleanUpDirectoryCommand(codjoPomAllDepsPath));
             player.add(new CopyPomCommand(ArtifactType.SUPER_POM, "", codjoPomAllDepsPath));
             player.add(new PrepareAPomToLoadSuperPomDependenciesCommand(codjoPomAllDepsPath, frameworkVersion));
 
             player.add(new CleanUpDirectoryCommand(temporaryLocalRepository));
             player.add(new IdeaCommand(ArtifactType.LIB, codjoPomAllDepsDirectory, temporaryLocalRepository));
            player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "codjo:find-release-version"));
             player.add(new RebuildNexusPomMetaDataCommand(properties));
             player.add(new SetNexusProxySettingsCommand(null, null, properties));
 
             player.add(new MavenCommand(ArtifactType.SUPER_POM, "", "codjo:update-confluence-after-release"));
             StepInvoker invoker = new StepInvoker("Finalisation", player);
             invoker.start();
         }
     }
 
 
     private void sendMail() {
         //TODO recuperer la version du framework des traces maven ? ou deplacer dans codjo-maven-plugin
         String apiUserName = JOptionPane.showInputDialog("Merci de preciser le login teamLab "
                                                          + "(pour la notification Teamlab, parfois c'est une adresse mail):");
         String apiUserPassword = JOptionPane.showInputDialog("Merci de preciser le password teamLab "
                                                              + "(pour la notification Teamlab):");
         CommandPlayer player = new CommandPlayer();
         player.add(new MavenCommand(ArtifactType.SUPER_POM,
                                     "",
                                     "codjo:send-announcement-to-teams"));
         if (frameworkVersion != null && !frameworkVersion.trim().isEmpty()
             && apiUserName != null && !apiUserName.trim().isEmpty()
             && apiUserPassword != null && !apiUserPassword.trim().isEmpty()) {
             player.add(new NotifyCodjoUsersCommand(proxy, frameworkVersion, apiUserName, apiUserPassword));
         }
         StepInvoker invoker = new StepInvoker("Envoi du Mail", player);
         invoker.start();
     }
 
 
     private void buildPublishList() {
         if (!model.isEmpty() && Build.class.getName().equals(model.get(0).getClass().getName())) {
             buildList.clearSelection();
 
             Build[] modelAsArray = new Build[model.size()];
             model.copyInto(modelAsArray);
 
             boolean pomFound = false;
             for (Build tmpBuild : modelAsArray) {
                 if (ArtifactType.SUPER_POM.equals(tmpBuild.getType())) {
                     pomFound = true;
                 }
 
                 if (tmpBuild.getState() == State.TO_BE_PUBLISHED) {
                     model.removeElement(tmpBuild);
                     model.addElement(new Publish(tmpBuild, properties));
                 }
             }
             if (!pomFound) {
                 model.add(0, new Publish(new Build(ArtifactType.LIB, "pom"), properties));
             }
         }
     }
 
 
     private void buildDeleteRepoList() {
         if (!model.isEmpty() && Publish.class.getName().equals(model.get(0).getClass().getName())) {
             buildList.clearSelection();
 
             ArtifactStep[] modelAsArray = new ArtifactStep[model.size()];
             model.copyInto(modelAsArray);
 
             for (ArtifactStep tmpBuild : modelAsArray) {
                 if (tmpBuild.getState() == State.TO_BE_DELETED) {
                     model.removeElement(tmpBuild);
                     model.addElement(new DeleteRepo((Publish)tmpBuild));
                 }
             }
         }
     }
 
 
     private void importBuildList() throws IOException {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setSelectedFile(DEFAULT_BUILD_LIST_FILE);
         int result = fileChooser.showOpenDialog(mainPanel);
         if (result == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             if (selectedFile == null || !selectedFile.exists()) {
                 return;
             }
 
             loadBuildListFile(selectedFile);
         }
     }
 
 
     private void loadBuildListFile(File selectedFile) throws IOException {
         final List<Build> builds = new ArrayList<Build>();
         BufferedReader bufferedReader = new BufferedReader(new FileReader(selectedFile));
         String line = bufferedReader.readLine();
         while (line != null) {
             if ("".equals(line.trim())) {
                 break;
             }
 
             ArtifactType artifactType = null;
             String[] words = line.split(" ");
             String artifact = words[0];
             if ("lib".equalsIgnoreCase(artifact)) {
                 artifactType = ArtifactType.LIB;
             }
             else if ("plugin".equalsIgnoreCase(artifact)) {
                 artifactType = ArtifactType.PLUGIN;
             }
             else if ("libmaven".equalsIgnoreCase(artifact)) {
                 artifactType = ArtifactType.LIB_MAVEN;
             }
             else if ("ontology".equalsIgnoreCase(artifact)) {
                 artifactType = ArtifactType.ONTOLOGIE;
             }
 
             builds.add(new Build(artifactType, words[1]));
             line = bufferedReader.readLine();
         }
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 for (Build build : builds) {
                     model.addElement(build);
                 }
             }
         });
     }
 
 
     private void broadcastBuildList() throws IOException {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setSelectedFile(DEFAULT_BUILD_LIST_FILE);
         int result = fileChooser.showSaveDialog(mainPanel);
         if (result == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             boolean succeffullyCreated = selectedFile.createNewFile();
 
             if (succeffullyCreated) {
                 BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFile));
                 printBuildList(bufferedWriter);
                 bufferedWriter.close();
             }
         }
     }
 
 
     private void printBuildList(BufferedWriter out) throws IOException {
         for (int i = 0; i < model.getSize(); i++) {
             Build build = (Build)model.getElementAt(i);
             if (build.getState() != State.DONE) {
                 out.append(build.exportAsString());
                 out.newLine();
             }
         }
     }
 
 
     private void sortModel() throws IOException {
         String fileUrlKey = "farow.libFile.url";
         String property = System.getProperty(fileUrlKey);
         if (property == null) {
             throw new IOException("Pas de fichier de rfrence, la proprit '" + fileUrlKey + "' n'est pas dfinie");
         }
         URL fileUrl = new URL(property);
 
         Build[] modelAsArray = new Build[model.size()];
         model.copyInto(modelAsArray);
         Build[] result = ArtifactSorter.sortBuildList(modelAsArray, fileUrl);
 
         model.removeAllElements();
         for (Build tmpBuild : result) {
             model.addElement(tmpBuild);
         }
     }
 
 
     private Step askForBuild() {
         ArtifactType type = (ArtifactType)JOptionPane.showInputDialog(mainPanel,
                                                                       "Type d'artifact", "Choix",
                                                                       JOptionPane.PLAIN_MESSAGE,
                                                                       null,
                                                                       ArtifactType.values(),
                                                                       ArtifactType.LIB);
         if (type == null) {
             return null;
         }
 
         String name = JOptionPane.showInputDialog(mainPanel, "Nom de l'artifact", "agent");
         return new Build(type, name);
     }
 
 
     private String now() {
         return new SimpleDateFormat("HH:mm:ss").format(new Date());
     }
 
 
     private void moveSelectedBuild(int shift) {
         int selectedIndex = buildList.getSelectedIndex();
         Object selected = buildList.getSelectedValue();
         int index = model.indexOf(selected);
         model.removeElement(selected);
         model.insertElementAt(selected, index + shift);
         buildList.setSelectedIndex(selectedIndex + shift);
     }
 
 
     private void addBuild(Step build) {
         if (build == null) {
             return;
         }
         model.addElement(build);
         buildList.setSelectedValue(build, true);
     }
 
 
     private void runBuild(final ArtifactStep build, final String buildAuditMessage) {
         if (build == null) {
             displayTabbedPane.setSelectedIndex(0);
             appendAuditRow(buildAuditMessage + " termines");
             return;
         }
 
         build.setStateToRunning();
         buildList.repaint();
 
         String tabName = getBuildLabel(build);
         String auditMessage = build.getAuditMessage() + " de " + tabName;
 
         String tabPrefix = buildAuditMessage;
         if (buildAuditMessage.endsWith("s")) {
             tabPrefix = buildAuditMessage.substring(0, buildAuditMessage.length() - 1) + "-";
         }
         else {
             if (!"".equals(buildAuditMessage)) {
                 tabPrefix = buildAuditMessage + "-";
             }
         }
         final DisplayAdapter buildDisplay = initializeTreatment(auditMessage, tabName, tabPrefix);
 
         new SwingWorker() {
 
             @Override
             public Object construct() {
                 try {
                     build.run(buildDisplay);
                     return null;
                 }
                 catch (Exception e) {
                     return e;
                 }
             }
 
 
             @Override
             public void finished() {
                 buildList.repaint();
                 updateButtonState(true);
 
                 Exception error = (Exception)get();
                 if (error != null) {
                     JOptionPane.showMessageDialog(mainPanel,
                                                   error.getMessage(),
                                                   "Oupss",
                                                   JOptionPane.ERROR_MESSAGE);
                 }
                 else {
                     runBuild(getFirstUndoneBuild(), buildAuditMessage);
                 }
             }
         }.start();
     }
 
 
     private DisplayAdapter initializeTreatment(String auditMessage, String tabName, String tabPrefix) {
         appendAuditRow(auditMessage);
         updateButtonState(false);
         JTextArea logArea = new JTextArea();
         displayTabbedPane.addTab(tabPrefix + tabName, new JScrollPane(logArea));
         displayTabbedPane.setSelectedIndex(displayTabbedPane.getTabCount() - 1);
         return new DisplayAdapter(logArea, informationLabel);
     }
 
 
     private void updateButtonState(boolean enable) {
         releaseButton.setEnabled(enable);
         finalizeButton.setEnabled(enable);
         sendMailButton.setEnabled(enable);
         publishButton.setEnabled(enable);
         deleteRepoButton.setEnabled(enable);
     }
 
 
     private ArtifactStep getFirstUndoneBuild() {
         for (int i = 0; i < model.getSize(); i++) {
             ArtifactStep build = (ArtifactStep)model.getElementAt(i);
             if (build.getState() != Step.State.DONE
                 && build.getState() != State.TO_BE_PUBLISHED
                 && build.getState() != State.TO_BE_DELETED) {
                 return build;
             }
         }
         return null;
     }
 
 
     private void appendAuditRow(final String row) {
         appendRow(auditArea, now() + " - " + row);
     }
 
 
     private void appendRow(final JTextArea area, final String row) {
         if (SwingUtilities.isEventDispatchThread()) {
             area.append(row + "\n");
             //noinspection UseOfSystemOutOrSystemErr
             System.out.println(row);
         }
         else {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     appendRow(area, row);
                 }
             });
         }
     }
 
 
     public JPanel getMainPanel() {
         return mainPanel;
     }
 
 
     private static String getBuildLabel(ArtifactStep build) {
         String label = build.getName();
         if (build.getType() != ArtifactType.LIB) {
             label += " (" + build.getType().name().toLowerCase() + ")";
         }
         return label;
     }
 
 
     private class DisplayAdapter implements Display {
         private final JTextArea area;
         private JLabel informationLabel;
 
 
         DisplayAdapter(JTextArea area, JLabel informationLabel) {
             this.area = area;
             this.informationLabel = informationLabel;
         }
 
 
         public void line(String line) {
             appendRow(area, line);
         }
 
 
         public void information(String line) {
             informationLabel.setText(line);
         }
     }
 
     private static class BuildRenderer extends DefaultListCellRenderer {
 
         @Override
         public Component getListCellRendererComponent(JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean cellHasFocus) {
 
             ArtifactStep build = ((ArtifactStep)value);
 
             super.getListCellRendererComponent(list, getBuildLabel(build), index, isSelected, cellHasFocus);
 
             Step.State buildState = ((Step)value).getState();
             switch (buildState) {
                 case DONE:
                     setBackground(Color.GREEN);
                     break;
                 case FAILURE:
                     setBackground(Color.RED);
                     break;
                 case RUNNING:
                     setBackground(Color.YELLOW);
                     break;
                 case TODO:
                     setBackground(Color.LIGHT_GRAY);
                     break;
                 case TO_BE_PUBLISHED:
                     setBackground(Color.ORANGE);
                     break;
                 case TO_BE_DELETED:
                     setBackground(Color.MAGENTA);
                     break;
             }
 
             if (isSelected) {
                 setBackground(getBackground().darker());
             }
             return this;
         }
     }
 
     private class StepInvoker extends SwingWorker {
         private final DisplayAdapter buildDisplay;
         private Step step;
         private final String tabName;
         private final CommandPlayer commandPlayer;
 
 
         StepInvoker(String tabName, CommandPlayer commandPlayer) {
             this.tabName = tabName;
             this.commandPlayer = commandPlayer;
             buildDisplay = initializeTreatment(tabName + " : dmarrage",
                                                tabName, "");
         }
 
 
         @Override
         public Object construct() {
             try {
                 step = new Step(tabName, commandPlayer);
                 step.run(buildDisplay);
 
                 return null;
             }
             catch (Exception e) {
                 return e;
             }
         }
 
 
         @Override
         public void finished() {
             updateButtonState(true);
             if ((step.getState() == State.DONE) || (step.getState() == State.TO_BE_PUBLISHED)) {
                 displayTabbedPane.setSelectedIndex(0);
                 appendAuditRow(tabName + " : fin");
             }
             else {
                 JOptionPane.showMessageDialog(mainPanel,
                                               tabName + " : chec",
                                               "Oupss",
                                               JOptionPane.ERROR_MESSAGE);
                 appendAuditRow(tabName + " : chec");
             }
         }
     }
 
     private class ImportArtifactsCommand extends Command {
         private ImportArtifactsCommand() {
             super("Importation des artifacts  stabiliser");
         }
 
 
         @Override
         public void execute(Display display) throws Exception {
             //trie
             loadBuildListFile(DEFAULT_BUILD_LIST_FILE);
         }
     }
 }
