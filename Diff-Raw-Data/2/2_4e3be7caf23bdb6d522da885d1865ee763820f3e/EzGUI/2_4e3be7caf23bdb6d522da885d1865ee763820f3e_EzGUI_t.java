 package plugins.adufour.ezplug;
 
 import icy.gui.component.button.IcyButton;
 import icy.network.NetworkUtil;
 import icy.resource.ResourceUtil;
 import icy.resource.icon.IcyIcon;
 import icy.system.thread.ThreadUtil;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.filechooser.FileSystemView;
 
 import plugins.adufour.vars.lang.Var;
 import plugins.adufour.vars.lang.VarDouble;
 import plugins.adufour.vars.util.VarListener;
 
 public class EzGUI extends EzDialog implements ActionListener
 {
     public static final int           LOGO_HEIGHT               = 32;
     
     private EzPlug                    ezPlug;
     
     private Thread                    executionThread;
     
     private JPanel                    jPanelBottom;
     
     private JPanel                    jPanelButtons;
     
     private JButton                   jButtonRun;
     
     private JButton                   jButtonStop;
     
     private JButton                   jButtonSaveParameters;
     
     private JButton                   jButtonLoadParameters;
     
     private JButton                   jButtonHelp;
     
     private boolean                   jButtonsParametersVisible = true;
     
     private JProgressBar              jProgressBar;
     
     private VarDouble                 progressBarValue          = new VarDouble("Progress", 0.0);
     
     private final VarListener<Double> progressListener          = new VarListener<Double>()
                                                                 {
                                                                     @Override
                                                                     public void valueChanged(Var<Double> source, Double oldValue, final Double newValue)
                                                                     { // TODO replace inner class by
                                                                       // local private class
                                                                         ThreadUtil.invokeLater(new Runnable()
                                                                         {
                                                                             public void run()
                                                                             {
                                                                                boolean inderterminate = newValue < 0 || newValue > 1;
                                                                                 jProgressBar.setIndeterminate(inderterminate);
                                                                                 
                                                                                 if (!inderterminate) jProgressBar.setValue((int) (Math.max(0, Math.min(1.0, newValue)) * 100));
                                                                             }
                                                                         });
                                                                     }
                                                                     
                                                                     @Override
                                                                     public void referenceChanged(Var<Double> source, Var<? extends Double> oldReference, Var<? extends Double> newReference)
                                                                     {
                                                                         
                                                                     }
                                                                 };
     
     public EzGUI(final EzPlug ezPlug)
     {
         super(ezPlug.getName(), false);
         
         this.ezPlug = ezPlug;
         
         jPanelBottom = new JPanel(new GridLayout(2, 1));
         
         jPanelButtons = new JPanel(new GridLayout(1, 5));
         jPanelBottom.add(jPanelButtons);
         
         jButtonRun = new IcyButton(new IcyIcon(ResourceUtil.getAlphaIconAsImage("playback_play.png")));//new JButton("Start");
         jButtonRun.setToolTipText("Start the plug-in...");
         jButtonRun.addActionListener(this);
         jPanelButtons.add(jButtonRun);
         
         if (ezPlug instanceof EzStoppable)
         {
             jButtonStop = new IcyButton(new IcyIcon(ResourceUtil.getAlphaIconAsImage("playback_stop.png")));//new JButton("Stop");
             jButtonStop.setToolTipText("Stop the plug-in...");
             jButtonStop.setEnabled(false);
             jButtonStop.addActionListener(this);
             jPanelButtons.add(jButtonStop);
         }
         
         jButtonSaveParameters = new IcyButton(new IcyIcon(ResourceUtil.ICON_SAVE));//new JButton("Save");
         jButtonSaveParameters.setToolTipText("Save the parameters to a file...");
         jButtonSaveParameters.addActionListener(this);
         jPanelButtons.add(jButtonSaveParameters);
         
         jButtonLoadParameters = new IcyButton(new IcyIcon(ResourceUtil.ICON_LOAD));//new JButton("Load");
         jButtonLoadParameters.setToolTipText("Load the parameters from a file...");
         jButtonLoadParameters.addActionListener(this);
         jPanelButtons.add(jButtonLoadParameters);
         
         jButtonHelp = new IcyButton(new IcyIcon(ResourceUtil.ICON_HELP));
         jButtonHelp.setToolTipText("Access the online help for this plug-in...");
         jButtonHelp.addActionListener(this);
         jPanelButtons.add(jButtonHelp);
         
         jProgressBar = new JProgressBar();
         jProgressBar.setString("Running...");
         jPanelBottom.add(jProgressBar);
         
         progressBarValue.addListener(progressListener);
         
         getContentPane().add(jPanelBottom, BorderLayout.SOUTH);
         
         pack();
         
         setOptimalLocation();
     }
     
     @Override
     public void addEzComponent(EzComponent component, boolean isSingle)
     {
         // if the component is a variable, register it
         if (component instanceof EzVar<?>) ezPlug.registerVariable((EzVar<?>) component);
         
         super.addEzComponent(component, isSingle);
     }
     
     /**
      * Sets the state of the "Run" button on the interface
      */
     public void setRunButtonEnabled(final boolean runnable)
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             public void run()
             {
                 jButtonRun.setEnabled(runnable);
             }
         });
     }
     
     /**
      * Sets the text of the run button
      */
     public void setRunButtonText(final String text)
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             public void run()
             {
                 jButtonRun.setText(text);
             }
         });
     }
     
     /**
      * Sets the text of the run button
      */
     public void setStopButtonText(final String text)
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             public void run()
             {
                 jButtonStop.setText(text);
             }
         });
     }
     
     void setRunningState(final boolean running)
     {
         synchronized (executionThread)
         {
             if (executionThread.isInterrupted()) return;
         }
         
         ThreadUtil.invokeNow(new Runnable()
         {
             public void run()
             {
                 jButtonRun.setEnabled(!running);
                 if (ezPlug instanceof EzStoppable) jButtonStop.setEnabled(running);
                 
                 // Note: Printing a string on a progress bar is not supported on Mac OS look'n'feel.
                 // jButtonRun.setText(running ? "Running..." : "Run");
                 jProgressBar.setString(running ? "Running..." : "");
                 jProgressBar.setStringPainted(running);
                 
                 jProgressBar.setValue(0);
                 jProgressBar.setIndeterminate(running);
                 
                 // Repack the frame to ensure good behavior of some components
                 repack(false);
             }
         });
     }
     
     /**
      * Sets whether the action panel (buttons and progress bar) are visible or not
      * 
      * @param visible
      *            the new visibility state of the action panel
      */
     public void setActionPanelVisible(final boolean visible)
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             public void run()
             {
                 jPanelBottom.setVisible(visible);
             }
         });
     }
     
     /**
      * Returns the variable used by the interface's progress bar. Any change to this variable will
      * automatically affect the corresponding progress bar
      * 
      * @return the variable controlling the progress bar
      */
     public VarDouble getProgressBarValue()
     {
         return progressBarValue;
     }
     
     /**
      * 
      * @param value
      *            A value between 0 and 1 (any other value will set an infinitely active state)
      */
     public void setProgressBarValue(final double value)
     {
         progressBarValue.setValue(value);
     }
     
     public void setProgressBarVisible(final boolean visible)
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             public void run()
             {
                 jProgressBar.setVisible(visible);
             }
         });
     }
     
     public void setParametersIOVisible(final boolean visible)
     {
         if (visible == jButtonsParametersVisible) return;
         
         ThreadUtil.invokeLater(new Runnable()
         {
             public void run()
             {
                 if (visible)
                 {
                     jPanelButtons.add(jButtonLoadParameters);
                     jPanelButtons.add(jButtonSaveParameters);
                 }
                 else
                 {
                     jPanelButtons.remove(jButtonLoadParameters);
                     jPanelButtons.remove(jButtonSaveParameters);
                 }
             }
         });
         
         jButtonsParametersVisible = visible;
     }
     
     /**
      * Simulates a click on the run button (useful to execute the plug-in right after
      * initialization)
      */
     public void clickRun()
     {
         executionThread = new Thread(ezPlug, ezPlug.getName());
         executionThread.start();
     }
     
     // ActionListener //
     
     public void actionPerformed(ActionEvent e)
     {
         if (e.getSource().equals(jButtonRun))
         {
             clickRun();
         }
         else if (e.getSource().equals(jButtonStop))
         {
             if (ezPlug instanceof EzStoppable) ((EzStoppable) ezPlug).stopExecution();
         }
         else if (e.getSource().equals(jButtonLoadParameters))
         {
             JFileChooser jfc = currentParametersPath == null ? new JFileChooser(FileSystemView.getFileSystemView()) : new JFileChooser(currentParametersPath);
             
             if (jfc.showOpenDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
             
             currentParametersPath = jfc.getCurrentDirectory();
             
             ezPlug.loadParameters(jfc.getSelectedFile());
             
         }
         else if (e.getSource().equals(jButtonSaveParameters))
         {
             JFileChooser jfc = currentParametersPath == null ? new JFileChooser(FileSystemView.getFileSystemView()) : new JFileChooser(currentParametersPath);
             
             if (jfc.showSaveDialog(getContentPane()) != JFileChooser.APPROVE_OPTION) return;
             
             currentParametersPath = jfc.getCurrentDirectory();
             
             ezPlug.saveParameters(jfc.getSelectedFile());
         }
         else if (e.getSource().equals(jButtonHelp))
         {
             NetworkUtil.openBrowser(ezPlug.getDescriptor().getWeb() + "#documentation");
         }
         else
         {
             throw new UnsupportedOperationException("Action event not recognized for source " + e.getSource());
         }
     }
     
     @SuppressWarnings("deprecation")
     public void onClosed()
     {
         super.onClosed();
         
         if (ezPlug == null) return;
         
         if (executionThread != null && executionThread.isAlive())
         {
             // stop the execution if it was still running
             if (ezPlug instanceof EzStoppable)
             {
                 ((EzStoppable) ezPlug).stopExecution();
                 ThreadUtil.sleep(100);
             }
             else
             {
                 // special case: process needs to be force-killed.
                 // => use the dedicated interruption handler
                 executionThread.setUncaughtExceptionHandler(new EzStoppable.ForcedInterruptionHandler(ezPlug.getDescriptor()));
                 executionThread.stop();
             }
         }
         
         ezPlug.cleanFromUI();
         
         // remove all listeners
         
         jButtonRun.removeActionListener(this);
         if (jButtonStop != null) jButtonStop.removeActionListener(this);
         jButtonLoadParameters.removeActionListener(this);
         jButtonSaveParameters.removeActionListener(this);
         progressBarValue.addListener(progressListener);
         
         ezPlug = null;
     }
     
     public void setProgressBarMessage(final String string)
     {
         ThreadUtil.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
                 jProgressBar.setString(string);
                 jProgressBar.setStringPainted(!string.trim().equals(""));
             }
         });
     }
     
 }
