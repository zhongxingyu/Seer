 /**
  * 
  */
 package net.sourceforge.gjtapi.demo.gui;
 
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.telephony.Address;
 import javax.telephony.InvalidArgumentException;
 import javax.telephony.JtapiPeer;
 import javax.telephony.JtapiPeerFactory;
 import javax.telephony.JtapiPeerUnavailableException;
 import javax.telephony.Provider;
 
 import net.sourceforge.gjtapi.GenericJtapiPeer;
 
 /**
  * A selector for the provider and address.
  * @author Dirk Schnelle-Walka
  *
  */
 @SuppressWarnings("serial")
 class ProviderSelecion extends JDialog {
     /** The list of available providers. */
     private final JComboBox providers;
 
     /** Addresses for the currently selected provider. */
     private final JComboBox addresses;
 
     /** The OK button. */
     private final JButton ok;
 
     /** Display of an error message. */
     private final JLabel errorMessage;
 
     /** <code>true</code> if the user clicked the cancel button. */
     private boolean canceled;
 
     /** Loaded providers. */
     private final Map<String, Provider> loadedProviders;
 
     /**
      * Constructs a new object.
      */
     public ProviderSelecion() {
         setTitle("Provider Selection");
         setModal(true);
         final LayoutManager layout = new GridBagLayout();
         Container pane = getContentPane();
         setLayout(layout);
         JLabel providerlbl = new JLabel("Provider");
         pane.add(providerlbl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                 GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                 new Insets(10, 10, 0, 10), 0, 0));
         final String[] provs = getProviders();
         providers = new JComboBox(provs);
         pane.add(providers, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 
                 GridBagConstraints.FIRST_LINE_END, GridBagConstraints.HORIZONTAL, 
                 new Insets(10, 10, 0, 10), 0, 0));
         final JLabel addresslbl = new JLabel("Address");
         pane.add(addresslbl, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                 GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, 
                 new Insets(5, 10, 0, 10), 0, 0));
         addresses = new JComboBox();
         pane.add(addresses, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 
                 GridBagConstraints.LAST_LINE_END, GridBagConstraints.HORIZONTAL, 
                 new Insets(5, 10, 0, 10), 0, 0));
         ok = new JButton("OK");
         pane.add(ok, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 
                 GridBagConstraints.LAST_LINE_END, GridBagConstraints.CENTER, 
                 new Insets(10, 10, 10, 10), 0, 0));
         final JButton cancel = new JButton("Cancel");
         pane.add(cancel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 
                 GridBagConstraints.LAST_LINE_END, GridBagConstraints.CENTER, 
                 new Insets(10, 10, 10, 10), 0, 0));
 
         providers.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 providerSelectionChanged();
             }
         });
         ok.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
             }
         });
         cancel.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 canceled = true;
                 setVisible(false);
             }
         });
         JPanel statusBar = new JPanel();
         statusBar.setLayout(new FlowLayout());
         errorMessage = new JLabel();
         statusBar.add(errorMessage);
         pane.add(statusBar, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, 
                 GridBagConstraints.LINE_START, GridBagConstraints.BOTH, 
                 new Insets(10, 10, 10, 10), 0, 0));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         pack();
         loadedProviders = new java.util.HashMap<String, Provider>();
         providerSelectionChanged();
         setVisible(true);
     }
 
     /**
      * Displays an error message.
      * @param throwable the caught exception.
      */
     private void displayError(Throwable throwable) {
         errorMessage.setText(throwable.getClass().getCanonicalName());
         errorMessage.setToolTipText(throwable.getMessage());
     }
 
     /**
      * The user selected another provider.
      */
     private void providerSelectionChanged() {
         String name = (String) providers.getSelectedItem();
         if (name == null) {
             return;
         }
         addresses.removeAllItems();
         String[] provaddresses = getAddresses(name);
         if (provaddresses != null) {
             for (String address : provaddresses) {
                 addresses.addItem(address);
             }
         }
         checkOkButton();
     }
 
     private void checkOkButton() {
         String address = (String) addresses.getSelectedItem();
         ok.setEnabled(address != null);
     }
     /**
      * Determine all providers.
      * @return list of providers.
      */
     private String[] getProviders() {
         try {
             JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(
                     GenericJtapiPeer.class.getCanonicalName());
             return peer.getServices();
         } catch (JtapiPeerUnavailableException e) {
             throw null;
         }
     }
 
     private String[] getAddresses(String name) {
         Address[] addresses;
         try {
             Provider provider = getProvider();
             addresses = provider.getAddresses();
         } catch (Exception e) {
             return null;
         } catch (UnsatisfiedLinkError e) {
             return null;
         }
         if(addresses == null || addresses.length == 0) {
             return null;
         }
         String[] names = new String[addresses.length];
         for(int i=0; i<names.length; i++) {
             names[i] = addresses[i].getName();
         }
         Arrays.sort(names);
         return names;
     }
 
     /**
      * Checks if the user clicked the cancel button.
      * @return <code>true</code> if the user clicked the cancel button
      */
     public boolean isCanceled() {
         return canceled;
     }
 
     /**
      * Retrieves the provider class name.
      * @return class name of the provider.
      */
     public Provider getProvider() {
         if (canceled) {
             return null;
         }
         try {
             final JtapiPeer peer = JtapiPeerFactory.getJtapiPeer(
                     GenericJtapiPeer.class.getCanonicalName());
             final String name = (String) providers.getSelectedItem();
             Provider provider = loadedProviders.get(name);
             if (provider == null) {
                 provider = peer.getProvider(name);
                 loadedProviders.put(name, provider);
             }
             return provider;
         } catch (Exception e) {
             displayError(e);
             e.printStackTrace();
             return null;
         } catch (UnsatisfiedLinkError e) {
             displayError(e);
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Retrieves the selected address.
      * @return the selected address.
      */
     public Address getAddress() {
         final Provider provider = getProvider();
         if (provider == null) {
             return null;
         }
         final String address = (String) addresses.getSelectedItem();
         try {
             return provider.getAddress(address);
         } catch (InvalidArgumentException e) {
             return null;
         }
     }
 }
