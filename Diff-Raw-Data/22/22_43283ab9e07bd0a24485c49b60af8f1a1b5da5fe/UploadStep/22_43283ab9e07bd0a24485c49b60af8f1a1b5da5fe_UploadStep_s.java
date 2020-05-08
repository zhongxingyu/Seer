 /* 
  * @(#)UploadStep.java
  *
  * Copyright (c) 2006 by Pokkari, Inc.
  * 239 Centre St, 3rd Floor
  * New York, NY 10013
  * All rights reserved.
  *
  * This software is the confidential and
  * proprietary information of Pokkari, Inc.
  */
 
 package com.blipnetworks.upperblip;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 
 import com.blipnetworks.util.*;
 
 import org.pietschy.wizard.AbstractWizardStep;
 import org.pietschy.wizard.WizardModel;
 
 /**
  *
  *
  * @author Jared Klett
 * @version $Id: UploadStep.java,v 1.27 2006/11/28 20:53:54 jklett Exp $
  */
 
 public class UploadStep extends AbstractWizardStep implements Runnable {
 
 // CVS info ////////////////////////////////////////////////////////////////////
 
	public static final String CVS_REV = "$Revision: 1.27 $";
 
 // Static variables ////////////////////////////////////////////////////////////
 
     private static final int INIT_INTERVAL = 5000;
     private static final int STATUS_INTERVAL = 3000;
     private static final int WAIT_INTERVAL = STATUS_INTERVAL * 2;
 
 	/** blah */
 	private static final String TITLE_KEY = "upload.title";
 	/** blah */
 	private static final String SUMMARY_KEY = "upload.summary";
     /** blah */
     private static final String BORDER_LABEL_KEY = "upload.border.label";
     /** blah */
     private static final String RATE_LABEL_KEY = "upload.rate.label";
     /** blah */
     private static final String TIME_LABEL_KEY = "upload.time.label";
 	/** blah */
 	public static final String PROP_BASE_URL = "base.url";
     /** blah */
     public static final String ERROR_TITLE = "upload.error.title";
     public static final String DONE_LABEL_KEY = "upload.done.msg";
 
 // Instance variables //////////////////////////////////////////////////////////
 
 	/** */
 	private boolean running;
     /** */
     private String statusURL;
     /** */
     private String filename;
     /** */
     private RandomGUID guid;
 	/** */
 	private JPanel view;
 	/** */
 	private UpperBlipModel model;
 	/** */
 	private JLabel rateLabel;
 	/** */
 	private JLabel timeLabel;
 	/** */
 	private JProgressBar progress;
     private List postList;
     private boolean done;
 
     private Runnable timer = new Runnable() {
         public void run() {
             progress.setMinimum(0);
             try { Thread.sleep(INIT_INTERVAL); } catch (Exception e) { /* ignored */ }
             while (running) {
                 UploadStatus status = UploadStatus.getStatus(statusURL, guid.toString());
                 if (status != null) {
                     long start = status.getStart();
                     long update = status.getUpdate();
                     int read = status.getRead();
                     int total = status.getTotal();
                     progress.setIndeterminate(false);
                     progress.setValue(read);
                     progress.setMaximum(total);
                     long delta = update - start;
                     if (delta == 0)
                         continue;
                     float bps = read / delta;
                     int time = (int) (total / bps);
                     String kbpstr = Float.toString(bps / 1000);
                     // TODO: externalize
                     rateLabel.setText("Uploading \"" + filename + "\" at " + kbpstr.substring(0, kbpstr.indexOf(".") + 2) + " KB/s");
 
                     String str = " (" + parseSize(read) + " of " + parseSize(total) + " uploaded)";
                     // TODO: externalize
                     if (time < 60) {
                         timeLabel.setText("Time remaining: less than a minute" + str);
                     } else {
                         int minutes = time / 60;
                         if (minutes > 1)
                             timeLabel.setText("Time remaining: about " + minutes + " minutes" + str);
                         else if (minutes > 0)
                             timeLabel.setText("Time remaining: about a minute" + str);
                         else
                             timeLabel.setText("Time remaining: less than a minute" + str);
                     }
                 }
                 try { Thread.sleep(STATUS_INTERVAL); } catch (Exception e) { /* ignored */ }
             }
         }
     };
 
 // Constructor ////////////////////////////////////////////////////////////////
 
 	public UploadStep() {
 		super(I18n.getString(TITLE_KEY), I18n.getString(SUMMARY_KEY));
 
         done = false;
         setIcon(Icons.uploadIcon);
         postList = new ArrayList();
         // Create and layout components
 		rateLabel = new JLabel(I18n.getString(RATE_LABEL_KEY));
 		timeLabel = new JLabel(I18n.getString(TIME_LABEL_KEY));
 		progress = new JProgressBar();
 		progress.setIndeterminate(true);
 
 		GridBagLayout gbl = new GridBagLayout();
 		GridBagConstraints gbc = new GridBagConstraints();
 		JPanel panel = new JPanel();
 		panel.setLayout(gbl);
 		// Set up a border
 		panel.setBorder(
 			BorderFactory.createCompoundBorder(
 				BorderFactory.createCompoundBorder(
 					BorderFactory.createTitledBorder(I18n.getString(BORDER_LABEL_KEY)),
 					BorderFactory.createEmptyBorder(5, 5, 5, 5)
 				),
 				panel.getBorder()
 			)
 		);
 
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbl.setConstraints(rateLabel, gbc);
 		panel.add(rateLabel);
 
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbl.setConstraints(progress, gbc);
 		panel.add(progress);
 
 		gbc.gridx = 0;
 		gbc.gridy = 2;
 		gbl.setConstraints(timeLabel, gbc);
 		panel.add(timeLabel);
 
 		view = new JPanel();
 		view.add(panel);
 	}
 
 // Runnable implementation ////////////////////////////////////////////////////
 
 	public void run() {
 		setBusy(true);
 		String url = Main.appProperties.getProperty(PROP_BASE_URL) + "/file/post?form_cookie=";
 		statusURL = Main.appProperties.getProperty(PROP_BASE_URL) + "/upload/status?skin=xmlhttprequest&form_cookie=";
 		Uploader uploader = new Uploader(url);
         // TODO: safeguards?
         uploader.setAuthCookie(Authenticator.authCookie);
         File[] files = model.getFiles();
 		//progress.setMinimum(0);
 		//progress.setMaximum(files.length);
 		String[] titles = model.getTitles();
 		String[] tags = model.getTags();
 		String[] descriptions = model.getDescriptions();
         String[] licenses = model.getLicenses();
         String[] categories = model.getCategories();
         String[][] blogs = model.getCrossposts();
         String[][] destinations = model.getCrossuploads();
         File[] thumbnails = model.getThumbnails();
         // Create a properties object
 		Properties props = new Properties();
 		props.put(Uploader.USER_PARAM_KEY, model.getUsername());
 		props.put(Uploader.PASS_PARAM_KEY, model.getPassword());
 		for (int i = 0; i < files.length; i++) {
             filename = files[i].getName();
             // Generate a new GUID
 			guid = new RandomGUID(false);
 			uploader.setGuid(guid.toString());
 			// put in the current data
 			props.put(Uploader.TITLE_PARAM_KEY, titles[i]);
 			props.put(Uploader.TAGS_PARAM_KEY, tags[i]);
 			props.put(Uploader.DESC_PARAM_KEY, descriptions[i]);
             props.put(Uploader.LICENSE_PARAM_KEY, licenses[i]);
             props.put(Uploader.CAT_PARAM_KEY, categories[i]);
             List list = new ArrayList();
             boolean blogsFound = false;
             for (int j = 0; j < blogs[i].length; j++) {
                 if (blogs[i][j] != null) {
                     list.add(blogs[i][j]);
                     blogsFound = true;
                 }
             }
             for (int j = 0; j < destinations[i].length; j++)
                 if (destinations[i][j] != null)
                     props.put(Uploader.IA_PARAM_KEY, destinations[i][j]);
 
             // calculate the approximate transfer time for the next file
 			// do the upload
 			Thread thread = new Thread(timer);
 			running = true;
 			thread.start();
 			boolean success;
             if (blogsFound)
                 success = uploader.uploadFile(files[i], thumbnails[i], props, list);
             else
                 success = uploader.uploadFile(files[i], thumbnails[i], props);
             String msg = "An unknown error occurred.";
            int err = -1;
             if (!success) {
                 err = uploader.getErrorCode();
                 if (err == Uploader.ERROR_BAD_AUTH) {
                      // TODO: externalize
                     msg = "Uh-oh, it looks like you supplied a bad username and/or password.\nPlease exit and try again.";
                 } else {
                      // TODO: externalize
                     msg = "It seems that a problem arose while uploading.\nDo you wish to continue attempting to upload?";
                 }
             }
             // Halt the timer thread
 			running = false;
 			thread.interrupt();
             try {
                 thread.join(WAIT_INTERVAL);
             }
             catch (InterruptedException e) {
                 //thread.stop();
             }
             if (success) {
                 postList.add(uploader.getPostURL());
             } else {
                int choice;
                if (err == Uploader.ERROR_UNKNOWN) {
                    choice = JOptionPane.showConfirmDialog(
                             Main.getMainInstance().getMainFrame(),
                             msg,
                             I18n.getString(ERROR_TITLE),
                             JOptionPane.YES_NO_OPTION,
                             JOptionPane.ERROR_MESSAGE
                     );
                     if (choice == JOptionPane.NO_OPTION)
                         break;
                } else {
                    JOptionPane.showMessageDialog(
                            Main.getMainInstance().getMainFrame(),
                            msg,
                            I18n.getString(ERROR_TITLE),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
             }
 			// take out the old data
 			props.remove(Uploader.TITLE_PARAM_KEY);
 			props.remove(Uploader.DESC_PARAM_KEY);
             props.remove(Uploader.LICENSE_PARAM_KEY);
             props.remove(Uploader.CAT_PARAM_KEY);
             props.remove(Uploader.TAGS_PARAM_KEY);
 			//if (progress.isIndeterminate())
 				//progress.setIndeterminate(false);
 			//progress.setValue(progress.getValue() + 1);
 		}
         progress.setValue(progress.getMaximum());
         timeLabel.setVisible(false);
         rateLabel.setText(I18n.getString(DONE_LABEL_KEY));
         setBusy(false);
 		setComplete(true);
         done = true;
         model.setPostURLs((String[])postList.toArray(new String[0]));
         model.nextStep();
     }
 
 // WizardStep implementation //////////////////////////////////////////////////
 
 	public void init(WizardModel model) {
 		this.model = (UpperBlipModel)model;
 	}
 
 	public void prepare() {
 		setView(view);
         if (!done)
             new Thread(this).start();
     }
 
 	public void applyState() {
 		// this is called when the user clicks "Next"
 		//FontItem fi = (FontItem)dropdown.getSelectedItem();
 		//model.setFontSize(fi.getFont().getSize());
 	}
 
 	public Dimension getPreferredSize() {
 		return view.getPreferredSize();
 	}
 
 // Class methods //////////////////////////////////////////////////////////////
 
     public static String parseSize(int bytes) {
         StringBuffer buffer = new StringBuffer();
         // is it smaller than 1K?
         // TODO: externalize
         if (bytes < 1024)
             buffer.append(bytes).append(" bytes");
         // is it smaller than 1M?
         else if(bytes < 1024000)
             buffer.append(bytes / 1024).append(" KB");
         // is it smaller than 1G?
         else if (bytes < 1024000000)
             buffer.append(bytes / 1024000).append(" MB");
         //else if (bytes < 1024000000000)
             //buffer.append(bytes / 1024000000).append(" GB");
         return buffer.toString();
     }
 
     public static String parseTime(long time) {
 		long t;
 		StringBuffer buffer = new StringBuffer();
 
         // TODO: externalize
 		if(time < 60000) {
 			t = time / 1000;
 			buffer.append(t).append(" second");
 		} else if(time > 60000 && time < 3600000) {
 			t = time / 60000;
 			buffer.append(t).append(" minute");
 		} else if(time > 3600000 && time < 86400000) {
 			t = time / 3600000;
 			buffer.append(t).append(" hour");
 		} else if(time > 86400000 && time < 604800000L) {
 			t = time / 86400000;
 			buffer.append(t).append(" day");
 		} else if(time > 604800000L && time < 2419200000L) {
 			t = time / 604800000L;
 			buffer.append(t).append(" week");
 		} else if(time > 2419200000L && time < 29030400000L) {
 			t = time / 2419200000L;
 			buffer.append(t).append(" month");
 		} else {
 			t = time / 29030400000L;
 			buffer.append(t).append(" year");
 		}
 
 		if(t > 1)
 			buffer.append("s");
 
 		return buffer.toString();
 	}
 
 } // class UploadStep
