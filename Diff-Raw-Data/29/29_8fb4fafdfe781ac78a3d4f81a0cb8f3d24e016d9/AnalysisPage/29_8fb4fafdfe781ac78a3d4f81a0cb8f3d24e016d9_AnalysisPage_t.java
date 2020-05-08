 /*
  * Sweeper - Duplicate file cleaner
  * Copyright (C) 2012 Bogdan Ciprian Pistol
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gg.pistol.sweeper.gui;
 
 import com.google.common.base.Preconditions;
 import gg.pistol.lumberjack.JackLogger;
 import gg.pistol.lumberjack.JackLoggerFactory;
 import gg.pistol.sweeper.core.Sweeper;
 import gg.pistol.sweeper.core.SweeperAbortException;
 import gg.pistol.sweeper.core.SweeperException;
 import gg.pistol.sweeper.core.SweeperOperation;
 import gg.pistol.sweeper.core.SweeperOperationListener;
 import gg.pistol.sweeper.core.SweeperPoll;
 import gg.pistol.sweeper.core.Target;
 import gg.pistol.sweeper.core.resource.Resource;
 import gg.pistol.sweeper.gui.component.ConfirmationDialog;
 import gg.pistol.sweeper.i18n.I18n;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Collection;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingDeque;
 
 // package private
 class AnalysisPage extends WizardPage {
 
     private static final JackLogger LOG = JackLoggerFactory.getLogger(LoggerFactory.getLogger(AnalysisPage.class));
     private static final int PROGRESS_UPDATE_FREQUENCY = 40; // millis
 
     private final WizardPage previousPage;
 
     private final Collection<? extends Resource> resources;
     private final SweeperOperationListener operationListener;
     private final ExecutorService executor;
 
     private final long startTime;
     private volatile long endTime;
     private volatile boolean analysisFinished;
     private volatile boolean analysisCanceled;
 
     private volatile int totalProgressPercent;
     private volatile long operationProgress;
     private volatile long operationMaxProgress;
     @Nullable private volatile SweeperOperation operation;
     @Nullable private volatile Target currentTarget;
 
     private final BlockingQueue<SweeperException> errorQueue;
    private final StringBuilder errorContent;
     private int errorCounter;
 
     @Nullable private JProgressBar totalProgressBar;
     @Nullable private JLabel timeLabel;
     @Nullable private JLabel remainingTimeLabel;
     @Nullable private JLabel operationLabel;
     @Nullable private JProgressBar operationProgressBar;
     @Nullable private JLabel currentTargetLabel;
     @Nullable private JTextArea errorTextArea;
 
 
     AnalysisPage(WizardPage previousPage, I18n i18n, WizardPageListener listener, Sweeper sweeper, Collection<? extends Resource> resources) {
         super(Preconditions.checkNotNull(i18n), Preconditions.checkNotNull(listener), Preconditions.checkNotNull(sweeper));
         Preconditions.checkNotNull(previousPage);
 
         this.previousPage = previousPage;
         this.resources = resources;
         operationListener = buildOperationListener();
         executor = Executors.newFixedThreadPool(2);
 
         startTime = System.currentTimeMillis();
         endTime = -1;
         errorQueue = new LinkedBlockingDeque<SweeperException>();
        errorContent = new StringBuilder(i18n.getString(I18n.PAGE_ANALYSIS_ERROR_COUNTER_ID, "0") + "\n");
     }
 
     @Override
     protected void addComponents(JPanel contentPanel) {
         Preconditions.checkNotNull(contentPanel);
         super.addComponents(contentPanel);
         contentPanel.add(alignLeft(new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_INTRODUCTION_ID))));
         contentPanel.add(createVerticalStrut(15));
 
         JPanel grid = new JPanel();
         grid.setComponentOrientation(ComponentOrientation.getOrientation(i18n.getLocale()));
         grid.setLayout(new GridBagLayout());
         contentPanel.add(alignLeft(grid));
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_TOTAL_PROGRESS_ID)), false, false, true);
         totalProgressBar = createProgressBar();
         addGridComponent(grid, totalProgressBar, true, false, true);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_TOTAL_ELAPSED_TIME_ID)), false, false, false);
         timeLabel = new JLabel();
         addGridComponent(grid, timeLabel, true, false, false);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_TOTAL_REMAINING_TIME_ID)), false, false, false);
         remainingTimeLabel = new JLabel();
         addGridComponent(grid, remainingTimeLabel, true, false, false);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_LABEL_ID)), false, false, true);
         operationLabel = new JLabel();
         addGridComponent(grid, operationLabel, true, false, true);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_PROGRESS_ID)), false, false, false);
         operationProgressBar = createProgressBar();
         addGridComponent(grid, operationProgressBar, true, false, false);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_TARGET_LABEL_ID)), false, false, true);
         currentTargetLabel = new JLabel();
         addGridComponent(grid, currentTargetLabel, true, false, true);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_ERROR_LABEL_ID)), false, false, true);
         errorTextArea = new JTextArea();
         errorTextArea.setEditable(false);
         addCopyMenu(errorTextArea);
         addGridComponent(grid, new JScrollPane(errorTextArea), true, true, true);
 
         updateProgress0();
     }
 
     private void addGridComponent(JPanel panel, JComponent component, boolean fillHorizontally, boolean fillVertically, boolean newGridSection) {
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.FIRST_LINE_START;
         int topInset = newGridSection ? 15 : 7;
         int horizontalInset = 3;
         if (fillHorizontally) {
             constraints.weightx = 1;
             constraints.fill = GridBagConstraints.HORIZONTAL;
             constraints.gridwidth = GridBagConstraints.REMAINDER;
             constraints.insets = new Insets(topInset, horizontalInset, 0, 0);
         } else {
             constraints.gridwidth = GridBagConstraints.RELATIVE;
             constraints.insets = new Insets(topInset, 0, 0, horizontalInset);
         }
         if (fillVertically) {
             constraints.weighty = 1;
             constraints.fill = fillHorizontally ? GridBagConstraints.BOTH : GridBagConstraints.VERTICAL;
             constraints.insets.bottom = 10;
         }
         panel.add(alignLeft(component), constraints);
     }
 
     private JProgressBar createProgressBar() {
         JProgressBar bar = new JProgressBar();
         bar.setComponentOrientation(ComponentOrientation.getOrientation(i18n.getLocale()));
         bar.setStringPainted(true);
         return bar;
     }
 
     void startAnalysis() {
         executor.submit(new Runnable() {
             @Override
             public void run() {
                 runAnalysis();
             }
         });
         executor.submit(new Runnable() {
             @Override
             public void run() {
                 continuouslyUpdateProgress();
             }
         });
     }
 
     private void runAnalysis() {
         WindowListener windowListener = new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 if (!analysisFinished && !analysisCanceled) {
                     sweeper.abortAnalysis();
                 }
             }
         };
         getParentWindow().addWindowListener(windowListener);
 
         try {
             sweeper.analyze(resources, operationListener);
             analysisFinished = true;
         } catch (SweeperAbortException e) {
             analysisCanceled = true;
         } catch (Exception e) {
             analysisCanceled = true;
             LOG.error("Error occurred while analyzing.", e);
         }
         endTime = System.currentTimeMillis();
         currentTarget = null;
         getParentWindow().removeWindowListener(windowListener);
 
         updateProgress();
         executor.shutdown();
     }
 
     private SweeperOperationListener buildOperationListener() {
         return new SweeperOperationListener() {
             @Override
             public void updateOperation(SweeperOperation operation) {
                 AnalysisPage.this.operation = operation;
                 updateProgress();
             }
 
             @Override
             public void updateOperationProgress(long progress, long maxProgress, int percentGlobal) {
                 operationProgress = progress;
                 operationMaxProgress = maxProgress;
                 totalProgressPercent = percentGlobal;
                 if (progress == 0 || progress == maxProgress) {
                     updateProgress();
                 }
             }
 
             @Override
             public void updateTarget(Target target) {
                 currentTarget = target;
             }
 
             @Override
             public void updateException(Target target, SweeperException e) {
                 try {
                     errorQueue.put(e);
                 } catch (InterruptedException ex) {
                     // ignore
                 }
             }
         };
     }
 
     private void updateProgress() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 updateProgress0();
             }
         });
     }
 
     private void updateProgress0() {
         // get the values of volatiles to be sure a value stays the same when using it multiple times
         SweeperOperation oper = operation;
         long maxProgress = operationMaxProgress;
         long progress = operationProgress;
         int progressGlobal = totalProgressPercent;
         Target target = currentTarget;
         long time = endTime;
 
         operationLabel.setText(getOperationDescription(oper));
         totalProgressBar.setValue(progressGlobal);
         if (maxProgress > 0) {
             operationProgressBar.setValue((int) (100 * progress / maxProgress));
         }
         currentTargetLabel.setText(target != null ? target.getName() : "");
 
         long elapsedTime = (time != -1 ? time : System.currentTimeMillis()) - startTime;
         timeLabel.setText(formatTime(elapsedTime));
         if (oper != null && time == -1 && progressGlobal > 0) {
             long remainingTime = elapsedTime * (100 - progressGlobal) / progressGlobal;
             remainingTimeLabel.setText(formatTime(remainingTime));
         } else {
             remainingTimeLabel.setText("");
         }
 
         updateErrors();
 
         if (time != -1) {
             errorTextArea.setCaretPosition(0);
             listener.onButtonStateChange();
         }
     }
 
     private void updateErrors() {
        int newErrors = 0;
         SweeperException e;
         while ((e = errorQueue.poll()) != null) {
            newErrors++;
            errorContent.append(e.getMessage());
            errorContent.append('\n');
         }
 
        if (newErrors > 0) {
            errorCounter += newErrors;
            int firstLineEnd = errorContent.indexOf("\n") + 1;
             String errorCounterStr = i18n.getString(I18n.PAGE_ANALYSIS_ERROR_COUNTER_ID, Integer.toString(errorCounter)) + "\n";
            errorContent.replace(0, firstLineEnd, errorCounterStr);
        }
 
        if (newErrors > 0 || errorTextArea.getText().isEmpty()) {
            errorTextArea.setText(errorContent.toString());
         }
     }
 
     private void continuouslyUpdateProgress() {
         while (endTime == -1) {
             try {
                 Thread.sleep(PROGRESS_UPDATE_FREQUENCY);
             } catch (InterruptedException e) {
                 break;
             }
             updateProgress();
         }
     }
 
     private String getOperationDescription(SweeperOperation operation) {
         if (operation == null) {
             return "";
         }
         switch (operation) {
             case RESOURCE_TRAVERSING:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_RESOURCE_TRAVERSAL_ID);
             case SIZE_COMPUTATION:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_SIZE_COMPUTATION_ID);
             case HASH_COMPUTATION:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_HASH_COMPUTATION_ID);
             case RESOURCE_DELETION:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_RESOURCE_DELETION_ID);
         }
         return null;
     }
 
     private String formatTime(long time) {
         int hours = (int) (time / 3600000);
         time %= 3600000;
         int minutes = (int) (time / 60000);
         time %= 60000;
         int seconds = (int) (time / 1000);
         if (hours > 0) {
             return i18n.getString(I18n.TIME_DESCRIPTION_HOURS_ID, Integer.toString(hours), Integer.toString(minutes), Integer.toString(seconds));
         } else if (minutes > 0) {
             return i18n.getString(I18n.TIME_DESCRIPTION_MINUTES_ID, Integer.toString(minutes), Integer.toString(seconds));
         } else {
             return i18n.getString(I18n.TIME_DESCRIPTION_SECONDS_ID, Integer.toString(seconds));
         }
     }
 
     @Override
     protected String getPageHeader() {
         return i18n.getString(I18n.PAGE_ANALYSIS_HEADER_ID);
     }
 
     @Override
     boolean isCancelButtonVisible() {
         return true;
     }
 
     @Override
     boolean isCancelButtonEnabled() {
         return !analysisCanceled && !analysisFinished;
     }
 
     @Override
     boolean isBackButtonEnabled() {
         return true;
     }
 
     @Override
     boolean isNextButtonEnabled() {
         return analysisFinished;
     }
 
     @Override
     boolean isFinishButtonEnabled() {
         return false;
     }
 
     @Override
     boolean isLastPage() {
         return false;
     }
 
     @Override
     boolean isLanguageSelectorVisible() {
         return true;
     }
 
     @Override
     void cancel() {
         if (back() != null) {
             listener.onButtonStateChange();
         }
     }
 
     @Override
     @Nullable
     WizardPage back() {
         if (analysisCanceled) {
             return previousPage;
         }
         if (new ConfirmationDialog(getParentWindow(), i18n, i18n.getString(I18n.LABEL_CONFIRMATION_ID),
                 i18n.getString(I18n.PAGE_ANALYSIS_CANCEL_CONFIRMATION_MESSAGE_ID)).isConfirmed()) {
             analysisCanceled = true;
             sweeper.abortAnalysis();
             return previousPage;
         }
         return null;
     }
 
     @Override
     WizardPage next() {
         WizardPage ret;
         SweeperPoll poll = sweeper.getCurrentPoll();
         if (poll == null) {
             poll = sweeper.nextPoll();
         }
         if (poll != null) {
             ret = new PollPage(this, i18n, listener, sweeper);
         } else {
             ret = new NoDuplicatePage(i18n, listener, sweeper);
         }
         ret.setParentWindow(getParentWindow());
         return ret;
     }
 
     @Override
     @Nullable
     WizardPage finish() {
         return null;
     }
 
 }
