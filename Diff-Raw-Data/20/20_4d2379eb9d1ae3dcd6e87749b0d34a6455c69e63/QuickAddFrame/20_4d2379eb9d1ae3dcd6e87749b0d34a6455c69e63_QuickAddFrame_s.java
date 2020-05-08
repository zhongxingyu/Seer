 package com.fteams.pinger.ui;
 
 import com.fteams.pinger.PropertyLoader;
 import com.fteams.pinger.structures.ProxyInformation;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import static com.fteams.pinger.app.Main.CONFIG_PATH;
 
 class QuickAddFrame extends JFrame implements ActionListener{
 
     private final MainFrame parent;
     private final JTextField codeField;
     public QuickAddFrame(MainFrame parent)
     {
         this.parent = parent;
 
 
         JLabel codeLabel = new JLabel("Codes: ");
         codeField = new JTextField();
 
         JButton addButton = new JButton("Add");
         JButton removeButton = new JButton("Remove");
         JButton closeButton = new JButton("Close");
 
         JPanel buttonPanel = new JPanel();
         JPanel textPanel = new JPanel();
 
         addButton.addActionListener(this);
         removeButton.addActionListener(this);
         closeButton.addActionListener(this);
 
         buttonPanel.setLayout(new GridLayout(1,2));
         textPanel.setLayout(new GridLayout(1,2));
 
         buttonPanel.add(addButton);
         buttonPanel.add(removeButton);
         buttonPanel.add(closeButton);
 
         textPanel.add(codeLabel);
         textPanel.add(codeField);
 
         add(textPanel, BorderLayout.CENTER);
         add(buttonPanel, BorderLayout.SOUTH);
 
         setTitle("Quick Add");
         pack();
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().toLowerCase().equals("close"))
         {
             setVisible(false);
         }
         else if (e.getActionCommand().toLowerCase().equals("add"))
         {
             String addedItem = codeField.getText();
             if (addedItem.equals("")) {
                 return;
             }
            parent.updateConsole(addedItem);
 
             PropertyLoader loader = new PropertyLoader();
             File config = new File(CONFIG_PATH);
             try {
                 loader.loadProperties(config);
             }
             catch (IOException e1)
             {
                 // Silence...
                 // oh wait..
                 return;
             }
 
            Properties properties = prepareProperties(loader.getCodes()+","+addedItem, loader);
 
             codeField.setText("");
             try {
                 properties.store(new FileOutputStream(config), "");
             }
             catch (FileNotFoundException e1)
             {
                 // Silence...
             }
             catch (IOException e1)
             {
                 // I'll kill you!
             }
         }
         else if (e.getActionCommand().toLowerCase().equals("remove"))
         {
 
             String removedItem = codeField.getText();
             if (removedItem.equals(""))
             {
                 return;
             }
            parent.updateConsole(removedItem);
             PropertyLoader loader = new PropertyLoader();
             File config = new File(CONFIG_PATH);
             try {
                 loader.loadProperties(config);
             }
             catch (IOException e1)
             {
                 return;
             }
             String preparedString = prepareString(removedItem, loader);

            parent.updateConsole(preparedString);

             Properties properties = prepareProperties(preparedString, loader);
             codeField.setText("");
             try {
                 properties.store(new FileOutputStream(config), "");
             }
             catch (FileNotFoundException e1)
             {
                 // Silence...
             }
             catch (IOException e1) {
                 // I'll kill you!
             }
         }
     }
 
     private String prepareString(String removedItem, PropertyLoader loader) {
         String codeString  = loader.getCodes();
         String [] codes = codeString.split(",");
         List<String> outputList = new LinkedList<String>();
         for (String code : codes)
         {
             if (code.equals(removedItem) || code.equals(""))
             {
                 continue;
             }
             outputList.add(code);
         }
         String out = "";
         for (String code : outputList)
         {
             if (out.length()==0)
                 out += code;
             else
                 out += ","+code;
         }
         return out;
     }
 
     private Properties prepareProperties(String codes, PropertyLoader loader) {
         Properties properties = new Properties();
         properties.setProperty("ping.timer", Long.toString(loader.getPingTimer()));
         properties.setProperty("ping.url.base", loader.getBaseUrl());
         properties.setProperty("ping.url.codes", codes);
         properties.setProperty("ping.url.search_string", loader.getSearchString());
         ProxyInformation pInfo = loader.getProxyInformation();
         properties.setProperty("ping.use_proxy", pInfo.proxy_enabled ? "true" : "false");
         properties.setProperty("ping.proxy.host", pInfo.host);
         properties.setProperty("ping.proxy.port", pInfo.port);
         properties.setProperty("ping.proxy.user_auth", pInfo.user_auth ? "true" : "false");
         properties.setProperty("ping.proxy.username", pInfo.p_username);
         properties.setProperty("ping.proxy.password", pInfo.p_password);
         return properties;
     }
 
 }
