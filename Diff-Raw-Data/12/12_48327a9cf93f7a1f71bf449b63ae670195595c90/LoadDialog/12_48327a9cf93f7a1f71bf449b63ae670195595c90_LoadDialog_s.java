 package de.jardas.drakensang.gui.load;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 
 import org.jdesktop.swingworker.SwingWorker;
 
 import de.jardas.drakensang.dao.SavegameDao;
 import de.jardas.drakensang.dao.SavegameDao.Progress;
 import de.jardas.drakensang.model.savegame.Savegame;
 import de.jardas.drakensang.shared.DrakensangException;
 import de.jardas.drakensang.shared.Launcher;
 import de.jardas.drakensang.shared.db.Messages;
 
 public class LoadDialog extends JDialog implements SavegameListener {
 	private final SavegameListener savegameListener;
 	private final SavegameProgress progress = new SavegameProgress();
 	private final JProgressBar progressBar = new JProgressBar(0, 100);
 	private final LoadProgress loadProgress = new LoadProgress();
 	private final Frame owner;
 	private SavegameListPanel list;
 	private JToolBar toolbar;
 	private JButton abortButton;
 
 	public LoadDialog(final Frame owner, SavegameListener savegameListener) {
 		super(owner, Messages.get("LoadGame"), true);
 		this.savegameListener = savegameListener;
 		this.owner = owner;
 	}
 
 	public void showDialog() {
 		loadProgress.showDialog();
 	}
 
 	private void init(final List<Savegame> savegames) {
 		setLayout(new BorderLayout());
 
 		final SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {
 			@Override
 			protected Object doInBackground() throws Exception {
 				list = new SavegameListPanel(savegames);
 
 				abortButton = new JButton(new AbstractAction(Messages
 						.get("Cancel")) {
 					public void actionPerformed(ActionEvent e) {
 						setVisible(false);
 					}
 				});
 
 				toolbar = new JToolBar();
 				toolbar.setFloatable(false);
 				toolbar.add(abortButton);
 
 				add(new JScrollPane(list), BorderLayout.CENTER);
 				add(toolbar, BorderLayout.SOUTH);
 
 				return null;
 			}
 
 			@Override
 			protected void done() {
 				loadProgress.setVisible(false);
 
 				setSize(500, 500);
 				setLocationRelativeTo(owner);
 				setVisible(true);
 			}
 		};
 
 		worker.execute();
 	}
 
 	public void loadSavegame(Savegame savegame) {
 		setVisible(false);
 		savegameListener.loadSavegame(savegame);
 	}
 
 	private class SavegameProgress implements Progress {
 		private int done = 0;
 
 		public void setTotalNumberOfSavegames(int total) {
 			progressBar.setMinimum(0);
 			progressBar.setMaximum(total * 2);
 			progressBar.setValue(done);
 		}
 
 		public void onSavegameLoaded(Savegame savegame) {
 			step();
 		}
 
 		public void onSavegameFailed(File file) {
 			step();
 		}
 
 		public void onSavegameRendered() {
 			step();
 		}
 
 		private void step() {
 			done++;
 			progressBar.setValue(done);
 		}
 	}
 
 	private class LoadProgress extends JDialog {
 		public LoadProgress() {
 			super(owner, Messages.get("LoadingGames"), true);
 
 			setLayout(new BorderLayout());
 			progressBar.setPreferredSize(new Dimension(300, 20));
 
 			add(progressBar, BorderLayout.CENTER);
 		}
 
 		public void showDialog() {
 			// Let's start loading the savegames.
 			final SwingWorker<List<Savegame>, Object> worker = new SwingWorker<List<Savegame>, Object>() {
 				@Override
 				protected List<Savegame> doInBackground() throws Exception {
					return SavegameDao.getSavegames(progress);
 				}
 
 				@Override
 				protected void done() {
 					try {
 						init(get());
 					} catch (InterruptedException e) {
 						// ignore
 					} catch (ExecutionException e) {
 						Launcher.handleException(new DrakensangException(
								"Error loading save games:" + e, e));
 					}
 				}
 			};
 
 			worker.execute();
 
 			pack();
 			setLocationRelativeTo(owner);
 			setVisible(true);
 		}
 	}
 
 	private class SavegameListPanel extends JPanel {
 		public SavegameListPanel(final List<Savegame> savegames) {
 			super();
 			setLayout(new GridBagLayout());
 
 			int row = 0;
 
 			for (final Savegame savegame : savegames) {
 				SavegameListItem item = new SavegameListItem(savegame,
 						LoadDialog.this);
 				add(item, new GridBagConstraints(0, row++, 1, 1, 1, 0,
 						GridBagConstraints.NORTHWEST,
 						GridBagConstraints.HORIZONTAL, new Insets(3, 6, 3, 6),
 						0, 0));
 				progress.onSavegameRendered();
 			}
 		}
 	}
 }
