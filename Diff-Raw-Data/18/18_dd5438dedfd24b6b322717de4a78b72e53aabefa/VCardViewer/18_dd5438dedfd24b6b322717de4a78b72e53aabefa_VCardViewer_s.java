 /**
  * $Revision: $
  * $Date: $
  *
  * Copyright (C) 2006 Jive Software. All rights reserved.
  *
  * This software is published under the terms of the GNU Lesser Public License (LGPL),
  * a copy of which is included in this distribution.
  */
 
 package org.jivesoftware.spark.ui;
 
 import org.jdesktop.jdic.desktop.Desktop;
 import org.jdesktop.jdic.desktop.DesktopException;
 import org.jdesktop.jdic.desktop.Message;
 import org.jivesoftware.resource.SparkRes;
 import org.jivesoftware.smackx.packet.VCard;
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.UserManager;
 import org.jivesoftware.spark.util.GraphicUtils;
 import org.jivesoftware.spark.util.ModelUtil;
 import org.jivesoftware.spark.util.SwingWorker;
 import org.jivesoftware.spark.util.log.Log;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * UI to display VCard Information in Wizards, Dialogs, Chat Rooms and any other container.
  *
  * @author Derek DeMoro
  */
 public class VCardViewer extends JPanel {
 
     private Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
     private Cursor LINK_CURSOR = new Cursor(Cursor.HAND_CURSOR);
 
     private final String jid;
     private final JLabel avatarImage;
 
     private static SimpleDateFormat utcFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
     private String emailAddress = "";
 
     /**
      * Generate a VCard Panel using the specified jid.
      *
      * @param jid the jid to use when retrieving the vcard information.
      */
     public VCardViewer(final String jid) {
         setLayout(new GridBagLayout());
         setOpaque(false);
 
         this.jid = jid;
         avatarImage = new JLabel();
         add(avatarImage, new GridBagConstraints(0, 0, 1, 3, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
 
 
         Image aImage = SparkRes.getImageIcon(SparkRes.BLANK_24x24).getImage();
         aImage = aImage.getScaledInstance(-1, 64, Image.SCALE_SMOOTH);
         ImageIcon ico = new ImageIcon(aImage);
 
         avatarImage.setIcon(ico);
 
         final SwingWorker vcardLoader = new SwingWorker() {
             VCard vcard = null;
 
             public Object construct() {
                 vcard = SparkManager.getVCardManager().getVCard(jid);
                 return vcard;
             }
 
             public void finished() {
                 if (vcard == null) {
                     // Do nothing.
                     return;
                 }
 
                 ImageIcon icon = null;
 
                 byte[] bytes = vcard.getAvatar();
                 if (bytes != null) {
                     try {
                         icon = new ImageIcon(bytes);
                         Image aImage = icon.getImage();
                         aImage = aImage.getScaledInstance(-1, 48, Image.SCALE_SMOOTH);
                         icon = new ImageIcon(aImage);
                     }
                     catch (Exception e) {
                         Log.error(e);
                     }
                 }
                 else {
                     icon = SparkRes.getImageIcon(SparkRes.DEFAULT_AVATAR_32x32_IMAGE);
                 }
 
                 if (icon != null && icon.getIconWidth() > 0) {
                     avatarImage.setIcon(icon);
                     avatarImage.setBorder(BorderFactory.createBevelBorder(0, Color.white, Color.lightGray));
                 }
 
                 vcard.setJabberId(jid);
                 buildUI(vcard);
             }
         };
 
         vcardLoader.start();
     }
 
     private void buildUI(final VCard vcard) {
 
         avatarImage.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent mouseEvent) {
                 if (mouseEvent.getClickCount() == 2) {
                     SparkManager.getVCardManager().viewProfile(vcard.getJabberId(), avatarImage);
                 }
             }
         });
 
         String firstName = vcard.getFirstName();
         if (firstName == null) {
             firstName = "";
         }
 
         String lastName = vcard.getLastName();
         if (lastName == null) {
             lastName = "";
         }
 
 
         final JLabel usernameLabel = new JLabel();
         usernameLabel.setHorizontalTextPosition(JLabel.LEFT);
         usernameLabel.setFont(new Font("Dialog", Font.BOLD, 15));
 
         usernameLabel.setForeground(Color.GRAY);
         if (ModelUtil.hasLength(firstName) && ModelUtil.hasLength(lastName)) {
             usernameLabel.setText(firstName + " " + lastName);
         }
         else {
             String nickname = SparkManager.getUserManager().getUserNicknameFromJID(jid);
             usernameLabel.setText(UserManager.unescapeJID(nickname));
         }
 
 
         final Icon icon = SparkManager.getChatManager().getIconForContactHandler(vcard.getJabberId());
         if (icon != null) {
             usernameLabel.setIcon(icon);
         }
 
 
         add(usernameLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 0), 0, 0));
 
         String title = vcard.getField("TITLE");
         if (ModelUtil.hasLength(title)) {
             final JLabel titleLabel = new JLabel(title);
             titleLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
             add(titleLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 0, 0), 0, 0));
         }
 
 
         if (ModelUtil.hasLength(vcard.getEmailHome())) {
             emailAddress = vcard.getEmailHome();
         }
 
         final Color linkColor = new Color(49, 89, 151);
         final String unselectedText = "<html><body><font color=" + GraphicUtils.toHTMLColor(linkColor) + "><u>" + emailAddress + "</u></font></body></html>";
         final String hoverText = "<html><body><font color=red><u>" + emailAddress + "</u></font></body></html>";
         final JLabel emailTime = new JLabel(unselectedText);
         emailTime.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 startEmailClient(emailAddress);
             }
 
             public void mouseEntered(MouseEvent e) {
                 emailTime.setText(hoverText);
                 setCursor(LINK_CURSOR);
 
             }
 
             public void mouseExited(MouseEvent e) {
                 emailTime.setText(unselectedText);
                 setCursor(DEFAULT_CURSOR);
             }
         });
 
 
         add(emailTime, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 10, 0), 0, 0));
 
         // Add JID Label
        final String jid = vcard.getJabberId();
        final JLabel jidLabel = new JLabel("<html><body>JID: <font color=" + GraphicUtils.toHTMLColor(linkColor) + "><u>" + emailAddress + "</u></font></body></html>");
         jidLabel.setToolTipText("Click to copy jid to clipboard.");
         jidLabel.addMouseListener(new MouseAdapter() {
             public void mouseEntered(MouseEvent mouseEvent) {
                 jidLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
             }
 
             public void mouseExited(MouseEvent mouseEvent) {
                 jidLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 
             }
 
             public void mouseClicked(MouseEvent mouseEvent) {
                 SparkManager.setClipboard(jid);
             }
         });
 
         add(jidLabel, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 2, 0), 0, 0));
 
         // Add Home Phone
         String homeNumber = vcard.getPhoneHome("VOICE");
         if (!ModelUtil.hasLength(homeNumber)) {
             homeNumber = "n/a";
         }
         final JLabel homePhoneLabel = new JLabel("Home: " + homeNumber);
         add(homePhoneLabel, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 2, 0), 0, 0));
 
         // Add Work Phone
         String workNumber = vcard.getPhoneWork("VOICE");
         if (!ModelUtil.hasLength(workNumber)) {
             workNumber = "n/a";
         }
         final JLabel workPhoneLabel = new JLabel("Work: " + workNumber);
         add(workPhoneLabel, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 2, 0), 0, 0));
 
         // Add Cell Phone
         String cellNumber = vcard.getPhoneWork("CELL");
         if (!ModelUtil.hasLength(cellNumber)) {
             cellNumber = "n/a";
         }
 
         final JLabel cellPhoneLabel = new JLabel("Cell: " + cellNumber);
         add(cellPhoneLabel, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 2, 0), 0, 0));
 
         // Add Company
         String company = vcard.getOrganization();
         final JLabel orgLabel = new JLabel("Company: " + company);
         add(orgLabel, new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 7, 2, 0), 0, 0));
 
     }
 
 
     private void startEmailClient(String emailAddress) {
         final Message message = new Message();
 
         final List<String> list = new ArrayList<String>();
         list.add(emailAddress);
 
         message.setToAddrs(list);
 
         SwingWorker worker = new SwingWorker() {
             public Object construct() {
                 try {
                     Desktop.mail(message);
                 }
                 catch (DesktopException e) {
                     e.printStackTrace();
                 }
                 return true;
             }
         };
 
         worker.start();
     }
 
 
 }
