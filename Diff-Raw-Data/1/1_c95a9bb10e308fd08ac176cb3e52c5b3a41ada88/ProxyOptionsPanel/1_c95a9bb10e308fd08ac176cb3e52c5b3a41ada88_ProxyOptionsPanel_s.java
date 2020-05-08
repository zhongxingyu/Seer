 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.lookupoptions.options;
 
 import org.apache.log4j.Logger;
 
 import org.jdom.Element;
 
 import org.openide.util.NbBundle;
 import org.openide.util.lookup.ServiceProvider;
 
 import de.cismet.lookupoptions.AbstractOptionsPanel;
 import de.cismet.lookupoptions.OptionsPanelController;
 
 import de.cismet.security.Proxy;
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.tools.PasswordEncrypter;
 
 import de.cismet.tools.configuration.NoWriteError;
 
 /**
  * OptionsPanel for the Proxy Options.
  *
  * <p>This panel allows to configure the proxy. Proxy-configuration affects the WebAccessManager and the proxy of the
  * java http protocol handler (System.setProperty("http.proxyHost") & System.setProperty("http.proxyPort")).</p>
  *
  * @author   jruiz
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = OptionsPanelController.class)
 public class ProxyOptionsPanel extends AbstractOptionsPanel implements OptionsPanelController {
 
     //~ Static fields/initializers ---------------------------------------------
 
 
     private static final transient Logger LOG = Logger.getLogger(ProxyOptionsPanel.class);
 
     private static final String OPTION_NAME = org.openide.util.NbBundle.getMessage(
             ProxyOptionsPanel.class,
             "ProxyOptionsPanel.OptionController.name");              // NOI18N
     private static final String CONFIGURATION = "ProxyOptionsPanel"; // NOI18N
     private static final String CONF_TYPE = "ProxyType";             // NOI18N
     private static final String CONF_HOST = "ProxyHost";             // NOI18N
     private static final String CONF_PORT = "ProxyPort";             // NOI18N
     private static final String CONF_USERNAME = "ProxyUsername";     // NOI18N
     private static final String CONF_PASSWORD = "ProxyPassword";     // NOI18N
     private static final String CONF_DOMAIN = "ProxyDomain";         // NOI18N
 
     //~ Enums ------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static enum ProxyTypes {
 
         //~ Enum constants -----------------------------------------------------
 
         NO, SYSTEM, MANUAL
     }
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean stillConfigured = false;
     private ProxyTypes proxyType;
     private String host;
     private int port;
     private transient String username;
     private transient String password;
     private transient String domain;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JLabel labHost;
     private javax.swing.JLabel labPort;
     private javax.swing.JLabel lblDomain;
     private javax.swing.JLabel lblPassword;
     private javax.swing.JLabel lblUsername;
     private javax.swing.JPasswordField pwdPassword;
     private javax.swing.JRadioButton rdoManualProxy;
     private javax.swing.JRadioButton rdoNoProxy;
     private javax.swing.JTextField txtDomain;
     private javax.swing.JTextField txtHost;
     private javax.swing.JTextField txtPort;
     private javax.swing.JTextField txtUsername;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form ProxyOptionsPanel.
      */
     public ProxyOptionsPanel() {
         super(OPTION_NAME, NetworkOptionsCategory.class);
         initComponents();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public int getOrder() {
         return 1;
     }
 
     @Override
     public void update() {
         // read proxy values
         final Proxy proxy = WebAccessManager.getInstance().getHttpProxy();
         if (proxy != null) {
             proxyType = ProxyTypes.MANUAL;
             host = proxy.getHost();
             port = proxy.getPort();
             username = proxy.getUsername();
             password = proxy.getPassword();
             domain = proxy.getDomain();
         } else if ((System.getProperty(Proxy.SYSTEM_PROXY_HOST) != null)
                     && (System.getProperty(Proxy.PROXY_PORT) != null)) {
             proxyType = ProxyTypes.SYSTEM;
         } else {
             proxyType = ProxyTypes.NO;
         }
 
         // update components
         switch (proxyType) {
             case MANUAL: {
                 rdoManualProxy.setSelected(true);
                 txtHost.setText(host);
                 if (port > 0) {
                     txtPort.setText(Integer.toString(port));
                 } else {
                     txtPort.setText(""); // NOI18N
                 }
                 txtUsername.setText(username);
                 pwdPassword.setText(password);
                 txtDomain.setText(domain);
                 break;
             }
             default: {
                 rdoNoProxy.setSelected(true);
             }
         }
     }
 
     @Override
     public void applyChanges() {
         boolean useProxy;
         if (rdoManualProxy.isSelected()) {
             useProxy = true;
             proxyType = ProxyTypes.MANUAL;
             host = txtHost.getText().trim();
             try {
                 port = Integer.valueOf(txtPort.getText().trim());
             } catch (final NumberFormatException ex) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("error while parsing port, setting port = 0", ex); // NOI18N
                 }
                 port = 0;
             }
             username = txtUsername.getText();
             password = String.valueOf(pwdPassword.getPassword());
             domain = txtDomain.getText();
         } else {
             proxyType = ProxyTypes.NO;
             useProxy = false;
         }
 
         // hier werden die Werte in dem Proxy gesetzt
         setProxy(useProxy, host, port, username, password, domain);
     }
 
     @Override
     public boolean isChanged() {
         int intPort;
         try {
             intPort = Integer.valueOf(txtPort.getText());
         } catch (final NumberFormatException ex) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("error while parsing port, assuming port = 0", ex); // NOI18N
             }
             intPort = 0;
         }
         return ((rdoNoProxy.isSelected() && (proxyType != ProxyTypes.NO))
                         || (rdoManualProxy.isSelected() && (proxyType != ProxyTypes.MANUAL))
                         || !txtHost.getText().equals(host)
                         || (intPort != port) || !txtUsername.getText().equals(username)
                         || !String.valueOf(pwdPassword.getPassword()).equals(password)
                         || !txtDomain.getText().equals(domain));
     }
 
     @Override
     public String getTooltip() {
         return org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.getTooltip().text");  //NOI18N
     }
 
     /**
      * Applies the proxy settings for the WebAccessManager and for the proxy of the java http protocol handler.
      *
      * @param  isActivated  Should the proxy be used
      * @param  host         Proxy Host
      * @param  port         Proxy Port
      * @param  username     Proxy Username
      * @param  password     Proxy Password
      * @param  domain       Proxy Domain
      */
     private void setProxy(final boolean isActivated,
             final String host,
             final int port,
             final String username,
             final String password,
             final String domain) {
             final Proxy newProxy = new Proxy(host, port, username, password, domain, true);
         if (isActivated) {
             Proxy.toPreferences(newProxy);
             WebAccessManager.getInstance().setHttpProxy(newProxy);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("set proxy in system-property: " + newProxy); // NOI18N
             }
             System.setProperty(Proxy.SYSTEM_PROXY_HOST, newProxy.getHost());
             System.setProperty(Proxy.SYSTEM_PROXY_PORT, String.valueOf(newProxy.getPort()));
             if (newProxy.getUsername() != null) {
                 System.setProperty(Proxy.SYSTEM_PROXY_USERNAME, newProxy.getUsername());
             }
             if (newProxy.getPassword() != null) {
                 System.setProperty(
                     Proxy.SYSTEM_PROXY_PASSWORD,
                     PasswordEncrypter.encryptString(newProxy.getPassword()));
             }
             if (newProxy.getDomain() != null) {
                 System.setProperty(Proxy.SYSTEM_PROXY_DOMAIN, newProxy.getDomain());
             }
         } else {
             Proxy.toPreferences(null);
             WebAccessManager.getInstance().setHttpProxy(null);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("clear proxy in system-property");            // NOI18N
             }
             System.clearProperty(Proxy.SYSTEM_PROXY_HOST);
             System.clearProperty(Proxy.SYSTEM_PROXY_PORT);
             System.clearProperty(Proxy.SYSTEM_PROXY_USERNAME);
             System.clearProperty(Proxy.SYSTEM_PROXY_PASSWORD);
             System.clearProperty(Proxy.SYSTEM_PROXY_DOMAIN);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Proxy getProxy() {
         applyChanges();
         return new Proxy(host, port, username, password, domain, ProxyTypes.MANUAL.equals(proxyType));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  proxy  DOCUMENT ME!
      */
     public void setProxy(final Proxy proxy) {
         if ((proxy == null) || (proxy.getHost() == null) || (proxy.getPort() < 1)) {
             rdoNoProxy.setSelected(true);
             txtHost.setText(null);
             txtPort.setText(null);
             txtUsername.setText(null);
             pwdPassword.setText(null);
             txtDomain.setText(null);
         } else {
             if (proxy.isEnabled()) {
                 rdoManualProxy.setSelected(true);
             } else {
                 rdoNoProxy.setSelected(true);
             }
             txtHost.setText(proxy.getHost());
             txtPort.setText(Integer.toString(proxy.getPort()));
             txtUsername.setText(proxy.getUsername());
             pwdPassword.setText(proxy.getPassword());
             txtDomain.setText(proxy.getDomain());
         }
     }
 
     @Override
     public void configure(final Element parent) {
         Proxy proxy = Proxy.fromPreferences();
         if (proxy != null) {
             setProxy(proxy);
             stillConfigured = true;
         }
         if (!stillConfigured) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Configure ProxyOptionPanels"); // NOI18N
             }
             try {
                 String elementProxyType = null;
                 String elementProxyHost = null;
                 String elementProxyPort = null;
                 String elementProxyUsername = null;
                 String elementProxyPassword = null;
                 String elementProxyDomain = null;
                 if (parent != null) {
                     final Element conf = parent.getChild(CONFIGURATION);
                     if (conf != null) {
                         elementProxyType = conf.getChildText(CONF_TYPE);
                         elementProxyHost = conf.getChildText(CONF_HOST);
                         elementProxyPort = conf.getChildText(CONF_PORT);
                         elementProxyUsername = conf.getChildText(CONF_USERNAME);
                         elementProxyPassword = conf.getChildText(CONF_PASSWORD);
                         elementProxyDomain = conf.getChildText(CONF_DOMAIN);
                     }
                 }
                 if ((elementProxyType != null) && elementProxyType.equals(ProxyTypes.MANUAL.toString())) {
                     proxyType = ProxyTypes.MANUAL;
                 } else {
                     proxyType = ProxyTypes.NO;
                 }
 
                 host = elementProxyHost;
                 username = elementProxyUsername;
                 password = PasswordEncrypter.decryptString(elementProxyPassword);
                 domain = elementProxyDomain;
 
                 try {
                     port = Integer.valueOf(elementProxyPort);
                 } catch (final NumberFormatException ex) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("cannot parse port from configuration element", ex); // NOI18N
                     }
                     port = 0;
                 }
             } catch (final Exception ex) {
                 LOG.error("error during ProxyOptionPanel configuration", ex);          // NOI18N
             }
 
             // hier werden die Werte in der GUI gesetzt
             txtHost.setText(host);
             txtUsername.setText(username);
             pwdPassword.setText(password);
             txtDomain.setText(domain);
             if (port > 0) {
                 txtPort.setText(Integer.toString(port));
             } else {
                 txtPort.setText(""); // NOI18N
             }
 
             switch (proxyType) {
                 case MANUAL: {
                     rdoManualProxy.setSelected(true);
                     break;
                 }
                 default: {
                     rdoNoProxy.setSelected(true);
                 }
             }
 
             stillConfigured = true;
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("skip Configure ProxyOptionPanels - still configured"); // NOI18N
             }
         }
 
         applyChanges();
     }
 
     @Override
     public Element getConfiguration() throws NoWriteError {
         if (LOG.isDebugEnabled()) {
             LOG.debug("ProxyOptionPanels - getConfiguration"); // NOI18N
         }
         final Element conf = new Element(CONFIGURATION);
 
         final Element proxyTypeElement = new Element(CONF_TYPE);
         final Element proxyHostElement = new Element(CONF_HOST);
         final Element proxyPortElement = new Element(CONF_PORT);
         final Element proxyUsernameElement = new Element(CONF_USERNAME);
         final Element proxyPasswordElement = new Element(CONF_PASSWORD);
         final Element proxyDomainElement = new Element(CONF_DOMAIN);
 
         final String pwEncrypted = PasswordEncrypter.encryptString(password);
         if (LOG.isDebugEnabled()) {
             LOG.debug("getConfiguration [type: " + proxyType.toString() // NOI18N
                         + " | host: " + host   // NOI18N
                         + " | port: " + port  // NOI18N
                         + " | username: " + username // NOI18N
                         + " | password: " + pwEncrypted // NOI18N
                         + " | domain: " + domain + " ]"); // NOI18N
         }
 
         proxyTypeElement.addContent(proxyType.toString());
         proxyHostElement.addContent(host);
         proxyPortElement.addContent(Integer.toString(port));
         proxyUsernameElement.addContent(username);
         proxyPasswordElement.addContent(pwEncrypted);
         proxyDomainElement.addContent(domain);
 
         conf.addContent(proxyTypeElement);
         conf.addContent(proxyHostElement);
         conf.addContent(proxyPortElement);
         conf.addContent(proxyUsernameElement);
         conf.addContent(proxyPasswordElement);
         conf.addContent(proxyDomainElement);
 
         return conf;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         labHost = new javax.swing.JLabel();
         txtHost = new javax.swing.JTextField();
         txtPort = new javax.swing.JTextField();
         labPort = new javax.swing.JLabel();
         rdoNoProxy = new javax.swing.JRadioButton();
         rdoManualProxy = new javax.swing.JRadioButton();
         jPanel1 = new javax.swing.JPanel();
         lblUsername = new javax.swing.JLabel();
         lblPassword = new javax.swing.JLabel();
         lblDomain = new javax.swing.JLabel();
         txtUsername = new javax.swing.JTextField();
         txtDomain = new javax.swing.JTextField();
         pwdPassword = new javax.swing.JPasswordField();
 
         labHost.setText(org.openide.util.NbBundle.getMessage(
                 ProxyOptionsPanel.class,
                 "ProxyOptionsPanel.labHost.text")); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 labHost,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 txtHost,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 txtPort,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         labPort.setText(org.openide.util.NbBundle.getMessage(
                 ProxyOptionsPanel.class,
                 "ProxyOptionsPanel.labPort.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 labPort,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         buttonGroup1.add(rdoNoProxy);
         rdoNoProxy.setText(org.openide.util.NbBundle.getMessage(
                 ProxyOptionsPanel.class,
                 "ProxyOptionsPanel.rdoNoProxy.text")); // NOI18N
 
         buttonGroup1.add(rdoManualProxy);
         rdoManualProxy.setText(org.openide.util.NbBundle.getMessage(
                 ProxyOptionsPanel.class,
                 "ProxyOptionsPanel.rdoManualProxy.text")); // NOI18N
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                 NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.jPanel1.border.title"))); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 jPanel1,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         lblUsername.setText(NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.lblUsername.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 lblUsername,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         lblPassword.setText(NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.lblPassword.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 lblPassword,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         lblDomain.setText(NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.lblDomain.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 lblDomain,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         txtUsername.setText(NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.txtUsername.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 txtUsername,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         txtDomain.setText(NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.txtDomain.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 txtDomain,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         pwdPassword.setText(NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.pwdPassword.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                 org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                 rdoManualProxy,
                 org.jdesktop.beansbinding.ELProperty.create("${selected}"),
                 pwdPassword,
                 org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(
                     jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                         lblUsername).addComponent(lblPassword).addComponent(lblDomain)).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                     jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(
                         txtDomain,
                         javax.swing.GroupLayout.Alignment.LEADING,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         252,
                         Short.MAX_VALUE).addComponent(
                         txtUsername,
                         javax.swing.GroupLayout.Alignment.LEADING,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         252,
                         Short.MAX_VALUE).addComponent(
                         pwdPassword,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         252,
                         Short.MAX_VALUE)).addContainerGap()));
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 jPanel1Layout.createSequentialGroup().addGroup(
                     jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                         lblUsername).addComponent(
                         txtUsername,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                     jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                         lblPassword).addComponent(
                         pwdPassword,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                     jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                         lblDomain).addComponent(
                         txtDomain,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         javax.swing.GroupLayout.PREFERRED_SIZE))));
 
         final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 layout.createSequentialGroup().addContainerGap().addGroup(
                     layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                         layout.createSequentialGroup().addComponent(
                             jPanel1,
                             javax.swing.GroupLayout.DEFAULT_SIZE,
                             javax.swing.GroupLayout.DEFAULT_SIZE,
                             Short.MAX_VALUE).addContainerGap()).addGroup(
                         layout.createSequentialGroup().addGroup(
                             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                                 rdoNoProxy).addComponent(rdoManualProxy)).addGap(179, 179, 179)).addGroup(
                         layout.createSequentialGroup().addComponent(labHost).addPreferredGap(
                             javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                             txtHost,
                             javax.swing.GroupLayout.DEFAULT_SIZE,
                             217,
                             Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(labPort).addPreferredGap(
                             javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                             txtPort,
                             javax.swing.GroupLayout.PREFERRED_SIZE,
                             70,
                             javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()))));
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                 layout.createSequentialGroup().addContainerGap().addComponent(rdoNoProxy).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(rdoManualProxy).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                     layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(labHost)
                                 .addComponent(
                                     txtHost,
                                     javax.swing.GroupLayout.PREFERRED_SIZE,
                                     javax.swing.GroupLayout.DEFAULT_SIZE,
                                     javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
                         txtPort,
                         javax.swing.GroupLayout.PREFERRED_SIZE,
                         javax.swing.GroupLayout.DEFAULT_SIZE,
                         javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(labPort)).addPreferredGap(
                     javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(
                     jPanel1,
                     javax.swing.GroupLayout.PREFERRED_SIZE,
                     javax.swing.GroupLayout.DEFAULT_SIZE,
                     javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(
                     javax.swing.GroupLayout.DEFAULT_SIZE,
                     Short.MAX_VALUE)));
 
         labHost.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         ProxyOptionsPanel.class,
                         "ProxyOptionsPanel.labHost.text"));        // NOI18N
         labPort.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         ProxyOptionsPanel.class,
                         "ProxyOptionsPanel.labPort.text"));        // NOI18N
         rdoNoProxy.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         ProxyOptionsPanel.class,
                         "ProxyOptionsPanel.rdoNoProxy.text"));     // NOI18N
         rdoManualProxy.getAccessibleContext()
                 .setAccessibleName(org.openide.util.NbBundle.getMessage(
                         ProxyOptionsPanel.class,
                         "ProxyOptionsPanel.rdoManualProxy.text")); // NOI18N
 
         bindingGroup.bind();
     } // </editor-fold>//GEN-END:initComponents
 }
