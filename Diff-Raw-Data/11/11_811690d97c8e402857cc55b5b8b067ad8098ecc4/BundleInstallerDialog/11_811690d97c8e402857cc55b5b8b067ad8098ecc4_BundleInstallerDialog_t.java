 package kkckkc.jsourcepad.bundleeditor.installer;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.io.Files;
 import com.google.common.io.InputSupplier;
 import kkckkc.jsourcepad.Dialog;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.model.settings.ProxySettings;
 import kkckkc.jsourcepad.util.Config;
 import kkckkc.jsourcepad.util.Cygwin;
 import kkckkc.jsourcepad.util.Network;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import kkckkc.jsourcepad.util.io.SystemEnvironmentHelper;
 import kkckkc.utils.DomUtil;
 import kkckkc.utils.Pair;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.*;
 import java.util.List;
 
 public class BundleInstallerDialog implements Dialog<BundleInstallerDialogView> {
     private BundleInstallerDialogView view;
     private List<BundleTableModel.Entry> bundles;
 
     @PostConstruct
     public void init() {
         view.getInstallButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 install();
             }
         });
 
         view.getCancelButton().addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 close();
             }
         });
 
         view.getLabel().setText("Getting list of bundles...");
 
 
 
     }
 
     public void show() {
         final BundleManager bundleManager = Application.get().getBundleManager();
         bundles = Lists.newArrayList();
 
         try {
             new SwingWorker<Void, Object>() {
 
                 @Override
                 protected Void doInBackground() throws Exception {
                     Set<String> preSelectedBundles = Sets.newHashSet();
                     if (bundleManager.getBundles() == null || bundleManager.getBundles().isEmpty()) {
                         preSelectedBundles.addAll(Arrays.asList(
                                "bundle-development", "c", "css", "diff", "html", "java", "javadoc", "javascript", "json", "markdown",
                                "php", "perl", "python", "ruby", "sql", "shellscript", "source", "subversion", "text",
                                 "textmate", "todo", "xml"));
                     }
 
                     URL url = new URL("http://github.com/api/v2/xml/repos/show/textmate");
                     final URLConnection conn = url.openConnection();
                     conn.connect();
 
                     Document document = DomUtil.parse(new InputSource(conn.getInputStream()));
                     for (Element e : DomUtil.getChildren(document.getDocumentElement())) {
                         Element nameE = DomUtil.getChild(e, "name");
                         String name = DomUtil.getText(nameE);
 
                         if (! name.endsWith(".tmbundle")) continue;
 
                         bundles.add(new BundleTableModel.Entry(
                                 bundleManager.getBundleByDirName(name) != null ||
                                     preSelectedBundles.contains(name.substring(0, name.lastIndexOf("."))),
                                 bundleManager.getBundleByDirName(name) != null, name, DomUtil.getChildText(e, "url")));
                     }
 
                     Collections.sort(bundles);
 
                     return null;
                 }
 
                 @Override
                 protected void done() {
                     view.setModel(new BundleTableModel(bundles));
                     view.getLabel().setText("Available bundles:");
 
                     if (bundleManager.getBundles() == null || bundleManager.getBundles().isEmpty()) {
                         JOptionPane.showMessageDialog(view.getJDialog(),
                                 "As this is the first time you are installing bundles, a selection \n" +
                                 "of common bundles have been pre-selected for you");
                     }
                 }
 
             }.execute();
 
         } catch (Exception ioe) {
             throw new RuntimeException(ioe);
         }
 
         view.getJDialog().setVisible(true);
     }
 
     private boolean verifyGit() {
         try {
             StatusCallback statusCallback = new StatusCallback();
 
             ScriptExecutor se = new ScriptExecutor("git --version", Application.get().getThreadPool());
             ScriptExecutor.Execution execution = se.execute(statusCallback, new StringReader(""), SystemEnvironmentHelper.getSystemEnvironment());
             execution.waitForCompletion();
 
             if (! statusCallback.isSuccessful()) {
                 return false;
             }
         } catch (Exception ioe) {
             throw new RuntimeException(ioe);
         }
         
         return true;
     }
 
     @Override
     public void close() {
         view.getJDialog().dispose();
     }
 
     @Override
     @Autowired
     public void setView(BundleInstallerDialogView view) {
         this.view = view;
     }
 
 
     private void install() {
         final Collection<BundleTableModel.Entry> entriesToInstall = Collections2.filter(bundles, new Predicate<BundleTableModel.Entry>() {
             @Override
             public boolean apply(BundleTableModel.Entry entry) {
                 return entry.isEnabled() && entry.isSelected();
             }
         });
 
         if (view.getDownloadMethod().getSelectedItem().toString().startsWith("SCM")) {
             if (! verifyGit()) {
                 JOptionPane.showMessageDialog(null,
                         "Git is not found. Please make sure git is installed (in cygwin if running on windows).",
                         "Git is not found",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
 
         final ProgressMonitor progressMonitor = new ProgressMonitor(view.getJDialog(),
                                       "Installing bundles",
                                       "", 0, entriesToInstall.size());
         progressMonitor.setMillisToPopup(100);
 
         new SwingWorker<Void, BundleTableModel.Entry>() {
             @Override
             protected Void doInBackground() throws Exception {
                 int i = 0;
 
                 for (BundleTableModel.Entry entry : entriesToInstall) {
                     setProgress(i++);
                     publish(entry);
 
                     if (view.getDownloadMethod().getSelectedItem().toString().startsWith("SCM")) {
                         installBundleUsingGit(entry);
                     } else {
                         installBundleUsingHttp(entry);
                     }
 
                     if (progressMonitor.isCanceled()) {
                         return null;
                     }
                 }
 
                 return null;
             }
 
             @Override
             protected void process(List<BundleTableModel.Entry> chunks) {
                 for (BundleTableModel.Entry s : chunks) {
                     progressMonitor.setProgress(getProgress());
                     progressMonitor.setNote(s.getName());
                 }
             }
 
             @Override
             protected void done() {
                 progressMonitor.close();
                 close();
             }
 
             private void installBundleUsingHttp(final BundleTableModel.Entry entry) {
                 try {
                     URL url = new URL(entry.getUrl() + "/tarball/master");
                     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
                     while (connection.getResponseCode() == 302) {
                         connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection();    
                     }
 
                     System.out.println(connection.getResponseCode());
                     System.out.println(connection.getHeaderField("Location"));
 
                     File tmp = File.createTempFile(entry.getName(), "tgz");
                     tmp.deleteOnExit();
 
                     final InputStream is = connection.getInputStream();
                     Files.copy(new InputSupplier<InputStream>() {
 
                         @Override
                         public InputStream getInput() throws IOException {
                             return is;
                         }
                     }, tmp);
 
 
                     StatusCallback statusCallback = new StatusCallback();
 
                     File folder = new File(Config.getBundlesFolder(), entry.getName());
                     folder.mkdir();
 
                     ScriptExecutor se = new ScriptExecutor("gzip -d " + Cygwin.makePathForDirectUsage(tmp.getCanonicalPath()) + " | tar --extract --strip-components=1 -f -", Application.get().getThreadPool());
                     se.setDirectory(folder);
                     final ScriptExecutor.Execution execution = se.execute(statusCallback, new StringReader(""),
                             SystemEnvironmentHelper.getSystemEnvironment());
                     execution.waitForCompletion();
 
                     if (! statusCallback.isSuccessful()) {
                         EventQueue.invokeAndWait(new Runnable() {
                             @Override
                             public void run() {
                                 JOptionPane.showMessageDialog(view.getJDialog(), "Cannot install bundle " + entry.getName(),
                                         "Error installing bundle", JOptionPane.ERROR_MESSAGE);
                             }
                         });
                     }
 
                     tmp.deleteOnExit();
 
                     Application.get().getBundleManager().addBundle(new File(Config.getBundlesFolder(), entry.getName()));
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
 
             private void installBundleUsingGit(final BundleTableModel.Entry entry) {
                 try {
                     // TODO: Show progress
                     StatusCallback statusCallback = new StatusCallback();
 
                     Map<String, String> env = Maps.newHashMap();
                     env.putAll(SystemEnvironmentHelper.getSystemEnvironment());
 
                     StringBuilder command = new StringBuilder();
                     command.append("git ");
 
                     ProxySettings proxySettings = Application.get().getSettingsManager().get(ProxySettings.class);
                     if (proxySettings.getProxyType() != ProxySettings.ProxyType.NO_PROXY) {
                         Pair<String, Integer> proxy = Network.getProxy("http://github.com");
                         if (proxy != null) {
                             env.put("http_proxy", proxy.getFirst() + ":" + proxy.getSecond());
                         }
                     }
 
                     command.append("clone ");
                     command.append(entry.getUrl().replaceAll("https:", "http:")).append(".git");
 
                     ScriptExecutor se = new ScriptExecutor(command.toString(), Application.get().getThreadPool());
                     se.setDirectory(Config.getBundlesFolder());
                     final ScriptExecutor.Execution execution = se.execute(statusCallback, new StringReader(""), env);
                     execution.waitForCompletion();
                         
                     if (! statusCallback.isSuccessful()) {
                         EventQueue.invokeAndWait(new Runnable() {
                             @Override
                             public void run() {
                                 JOptionPane.showMessageDialog(view.getJDialog(), "Cannot install bundle " + entry.getName(),
                                         "Error installing bundle", JOptionPane.ERROR_MESSAGE);
                             }
                         });
                     }
 
                     Application.get().getBundleManager().addBundle(new File(Config.getBundlesFolder(), entry.getName()));
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
         }.execute();
 
     }
 
     public static class StatusCallback extends ScriptExecutor.CallbackAdapter {
         boolean successful = false;
 
         @Override
         public void onFailure(ScriptExecutor.Execution execution) {
             successful = false;
         }
 
         @Override
         public void onSuccess(ScriptExecutor.Execution execution) {
             successful = true;
         }
 
         public boolean isSuccessful() {
             return successful;
         }
     }
 }
