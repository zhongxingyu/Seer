 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  *
  * This file is part of Universal Task Executor.
  *
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.gui.components;
 
 import com.google.common.eventbus.Subscribe;
 import net.lmxm.ute.beans.jobs.Job;
 import net.lmxm.ute.event.StatusChangeEvent;
 import net.lmxm.ute.event.StatusChangeEventType;
import net.lmxm.ute.exceptions.GuiException;
 import net.lmxm.ute.executers.jobs.JobStatusEvent;
 import net.lmxm.ute.gui.workers.ExecuteJobWorker;
 import net.lmxm.ute.resources.types.ButtonResourceType;
import net.lmxm.ute.resources.types.ExceptionResourceType;
 import net.lmxm.ute.resources.types.ToolbarButtonResourceType;
 import net.miginfocom.swing.MigLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import javax.swing.text.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import static net.lmxm.ute.executers.jobs.JobStatusEvent.JobStatusEventType.*;
 
 /**
  * The Class StatusOutputPanel.
  */
 @SuppressWarnings("serial")
 public final class StatusOutputPanel extends JPanel {
 
     /**
      * The Constant LOGGER.
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(StatusOutputPanel.class);
 
     /**
      * The Constant serialVersionUID.
      */
     private static final long serialVersionUID = -1701575052922365046L;
 
     /**
      * The clear output button.
      */
     private JButton clearOutputButton = null;
 
     /**
      * The job progress bar.
      */
     private JProgressBar jobProgressBar = null;
 
     /**
      * The job worker.
      */
     private ExecuteJobWorker jobWorker = null;
 
     /**
      * The job worker mutex.
      */
     private final Object jobWorkerMutex = new Object();
 
     /**
      * The output button tool bar.
      */
     private JToolBar outputButtonToolBar = null;
 
     /**
      * The output pane.
      */
     private JTextPane outputPane = null;
 
     /**
      * The output scroll pane.
      */
     private JScrollPane outputScrollPane = null;
 
     /**
      * The stop job button.
      */
     private JButton stopJobButton = null;
 
     /**
      * The style context.
      */
     private final StyleContext styleContext = createStyleContext();
 
     /**
      * Instantiates a new status output panel.
      *
      * @param job the job
      */
     public StatusOutputPanel(final Job job) {
         super();
 
         setLayout(new MigLayout());
         add(getOutputButtonToolBar(), "dock north");
         add(getOutputScrollPane(), "dock center");
 
         getJobProgressBar().setMaximum(job.getTasks().size());
     }
 
     /**
      * Creates the style context.
      *
      * @return the style context
      */
     protected static StyleContext createStyleContext() {
         final StyleContext styleContext = new StyleContext();
         final Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
 
         final Style errorStyle = styleContext.addStyle(StatusChangeEventType.ERROR.name(), defaultStyle);
         StyleConstants.setForeground(errorStyle, Color.RED);
 
         final Style fatalStyle = styleContext.addStyle(StatusChangeEventType.FATAL.name(), defaultStyle);
         StyleConstants.setBold(fatalStyle, true);
         StyleConstants.setForeground(fatalStyle, Color.RED);
 
         final Style headingStyle = styleContext.addStyle(StatusChangeEventType.HEADING.name(), defaultStyle);
         StyleConstants.setBold(headingStyle, true);
         StyleConstants.setFontSize(headingStyle, 16);
         StyleConstants.setSpaceAbove(headingStyle, 20);
         StyleConstants.setSpaceBelow(headingStyle, 10);
 
         final Style importantStyle = styleContext.addStyle(StatusChangeEventType.IMPORTANT.name(), defaultStyle);
         StyleConstants.setBold(importantStyle, true);
 
         final Style infoStyle = styleContext.addStyle(StatusChangeEventType.INFO.name(), defaultStyle);
         StyleConstants.setLeftIndent(infoStyle, 10);
 
         return styleContext;
     }
 
     /**
      * Gets the clear output button.
      *
      * @return the clear output button
      */
     private JButton getClearOutputButton() {
         if (clearOutputButton == null) {
             clearOutputButton = GuiComponentFactory.createToolbarButton(ToolbarButtonResourceType.CLEAR, new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             getOutputPane().setText("");
                         }
                     });
                 }
             });
         }
         return clearOutputButton;
     }
 
     /**
      * Gets the job progress bar.
      *
      * @return the job progress bar
      */
     private JProgressBar getJobProgressBar() {
         if (jobProgressBar == null) {
             jobProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
         }
 
         return jobProgressBar;
     }
 
     /**
      * Gets the output button tool bar.
      *
      * @return the output button tool bar
      */
     private JToolBar getOutputButtonToolBar() {
         if (outputButtonToolBar == null) {
             outputButtonToolBar = new JToolBar() {
                 {
                     setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
 
                     add(getStopJobButton());
                     add(getClearOutputButton());
                     add(getJobProgressBar());
                 }
             };
         }
         return outputButtonToolBar;
     }
 
     /**
      * Instantiates a new status output pane.
      *
      * @return the output pane
      */
     public JTextPane getOutputPane() {
         if (outputPane == null) {
             outputPane = new JTextPane(new DefaultStyledDocument(styleContext)) {
                 {
                     setEditable(false);
                 }
             };
         }
 
         return outputPane;
     }
 
     /**
      * Gets the output scroll pane.
      *
      * @return the output scroll pane
      */
     private JScrollPane getOutputScrollPane() {
         if (outputScrollPane == null) {
             outputScrollPane = new JScrollPane() {
                 {
                     setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                     setViewportView(getOutputPane());
                     setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                 }
             };
         }
 
         return outputScrollPane;
     }
 
     /**
      * Gets the stop job button.
      *
      * @return the stop job button
      */
     private JButton getStopJobButton() {
         if (stopJobButton == null) {
             stopJobButton = GuiComponentFactory.createToolbarButton(ToolbarButtonResourceType.STOP_JOB, new ActionListener() {
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             synchronized (jobWorkerMutex) {
                                 if (jobWorker != null) {
                                     final String prefix = "getStopJobButton().actionPerformed() :";
                                     LOGGER.debug("{} Sending cancel to job worker thread", prefix);
                                     jobWorker.cancel(true);
                                 }
                             }
                         }
                     });
                 }
             });
         }
 
         return stopJobButton;
     }
 
     @Subscribe
     public void handleJobStatusChange(final JobStatusEvent jobStatusEvent) {
         final JobStatusEvent.JobStatusEventType eventType = jobStatusEvent.getEventType();
         if (eventType == JobAborted || eventType == JobCompleted || eventType == JobStopped) {
             jobIsNolongerRunning();
         }
         else if (eventType == TaskCompleted || eventType == TaskSkipped) {
             incrementProgressBar();
         }
     }
 
     @Subscribe
     public void handleStatusChange(final StatusChangeEvent statusChangeEvent) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                final JTextPane theOutputPane = getOutputPane();
                final Document document = theOutputPane.getDocument();
 
                 try {
                     final Style style = styleContext.getStyle(statusChangeEvent.getEventType().name());
                     document.insertString(document.getLength(), statusChangeEvent.getMessage() + "\n", style);
                 }
                 catch (final BadLocationException e) {
                    throw new GuiException(ExceptionResourceType.UNEXPECTED_ERROR, e);
                 }
 
                theOutputPane.setCaretPosition(document.getLength());
             }
         });
     }
 
     private void incrementProgressBar() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 getJobProgressBar().setValue(getJobProgressBar().getValue() + 1);
             }
         });
     }
 
     /**
      * Job is nolonger running.
      */
     private void jobIsNolongerRunning() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 synchronized (jobWorkerMutex) {
                     // Clear connection to worker thread
                     jobWorker = null;
 
                     // Update GUI
                     getStopJobButton().setEnabled(false);
                     getJobProgressBar().setVisible(false);
                 }
             }
         });
     }
 
     /**
      * Sets the job worker.
      *
      * @param jobWorker the new job worker
      */
     public void setJobWorker(final ExecuteJobWorker jobWorker) {
         synchronized (jobWorkerMutex) {
             this.jobWorker = jobWorker;
         }
     }
 }
