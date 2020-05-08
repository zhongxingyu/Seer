 package net.hi117.unique.swingui;
 
 import net.hi117.unique.AbstractEvent;
 import net.hi117.unique.Game;
 import net.hi117.unique.GameUserInterface;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 /**
  * @author Yanus Poluektovich (ypoluektovich@gmail.com)
  */
 class GameScreen extends JPanel {
 
 	private final Callback<Boolean> myEndGameCallback;
 
 	private final JScrollPane myConsoleScroll;
 
 	private final JTextArea myConsole;
 
 	private final JButton myIncreaseButton;
 
 	private final JButton mySkipButton;
 
 	private volatile boolean myInput;
 
 	private final CyclicBarrier myInputBarrier = new CyclicBarrier(2);
 
 	GameScreen(final Callback<Boolean> endGameCallback)
 			throws InvocationTargetException, InterruptedException {
 		super(new GridBagLayout());
 		myEndGameCallback = endGameCallback;
 		myConsole = createConsoleTextArea();
 		myConsoleScroll = createConsoleScrollPane(myConsole);
 		myIncreaseButton = createIncreaseButton();
 		mySkipButton = createSkipButton();
 		createLayout();
 		addEventHandlers();
 		lockInputControls();
 	}
 
 	private JScrollPane createConsoleScrollPane(final JTextArea console) {
		return new JScrollPane(console);
 	}
 
 	private JButton createIncreaseButton() {
 		final JButton button = new JButton("Increase");
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				myInput = true;
 				try {
 					myInputBarrier.await();
 				} catch (InterruptedException | BrokenBarrierException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		return button;
 	}
 
 	private JButton createSkipButton() {
 		final JButton button = new JButton("Skip");
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				myInput = false;
 				try {
 					myInputBarrier.await();
 				} catch (InterruptedException | BrokenBarrierException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		return button;
 	}
 
 	private JTextArea createConsoleTextArea() {
 		final JTextArea jTextArea = new JTextArea();
 		jTextArea.setAutoscrolls(true);
 		jTextArea.setLineWrap(true);
 		jTextArea.setWrapStyleWord(true);
 		jTextArea.setEditable(false);
 		return jTextArea;
 	}
 
 	private void createLayout() {
 		final GridBagConstraints constraints = new GridBagConstraints();
 
 		constraints.gridwidth = 2;
 		constraints.insets = new Insets(12, 12, 3, 12);
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		this.add(myConsoleScroll, constraints);
 
 		constraints.gridy = 1;
 		constraints.gridwidth = 1;
 		constraints.fill = GridBagConstraints.HORIZONTAL;
 		constraints.weightx = 0.5;
 		constraints.weighty = 0.0;
 		constraints.insets = new Insets(3, 12, 12, 3);
 		this.add(myIncreaseButton, constraints);
 
 		constraints.gridx = 1;
 		constraints.insets = new Insets(3, 3, 12, 12);
 		this.add(mySkipButton, constraints);
 	}
 
 
 	private void addEventHandlers() {
 		AbstractEvent.ourGameUserInterface = new GameUserInterface() {
 			@Override
 			public void init(final Game game) {
 				myConsole.setText("");
 			}
 
 			@Override
 			public void message(final String message) throws Exception {
 				printlnToConsole(message);
 			}
 
 			@Override
 			public boolean input() throws Exception {
 				return processUserInput();
 			}
 
 			@Override
 			public void endGame(final Boolean victory) {
 				myEndGameCallback.doWith(victory);
 			}
 		};
 	}
 
 
 	private void lockInputControls()
 			throws InvocationTargetException, InterruptedException {
 		if (SwingUtilities.isEventDispatchThread()) {
 			myIncreaseButton.setEnabled(false);
 			mySkipButton.setEnabled(false);
 		} else {
 			SwingUtilities.invokeAndWait(
 					new Runnable() {
 						@Override
 						public void run() {
 							myIncreaseButton.setEnabled(false);
 							mySkipButton.setEnabled(false);
 						}
 					}
 			);
 		}
 	}
 
 	private void unlockInputControls()
 			throws InvocationTargetException, InterruptedException {
 		if (SwingUtilities.isEventDispatchThread()) {
 			myIncreaseButton.setEnabled(true);
 			mySkipButton.setEnabled(true);
 		} else {
 			SwingUtilities.invokeAndWait(
 					new Runnable() {
 						@Override
 						public void run() {
 							myIncreaseButton.setEnabled(true);
 							mySkipButton.setEnabled(true);
 						}
 					}
 			);
 		}
 	}
 
 
 	private void printlnToConsole(final String message) throws Exception {
 		SwingUtilities.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				myConsole.append(message);
 				myConsole.append("\n");
 				myConsole.setCaretPosition(myConsole.getDocument().getLength());
 			}
 		});
 		myConsole.repaint();
 	}
 
 	private boolean processUserInput() throws Exception {
 		unlockInputControls();
 		myInputBarrier.await();
 		lockInputControls();
 		return myInput;
 	}
 
 }
