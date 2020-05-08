 package org.opendarts.prototype.ui.x01.dialog;
 
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.progress.UIJob;
 import org.opendarts.prototype.ProtoPlugin;
 import org.opendarts.prototype.internal.model.dart.ThreeDartThrow;
 import org.opendarts.prototype.internal.model.dart.x01.BrokenX01DartsThrow;
 import org.opendarts.prototype.internal.model.dart.x01.WinningX01DartsThrow;
 import org.opendarts.prototype.internal.model.game.x01.GameX01;
 import org.opendarts.prototype.internal.model.game.x01.GameX01Entry;
 import org.opendarts.prototype.model.dart.DartZone;
 import org.opendarts.prototype.model.dart.IDart;
 import org.opendarts.prototype.model.dart.InvalidDartThrowException;
 import org.opendarts.prototype.model.game.IGameEntry;
 import org.opendarts.prototype.model.player.IPlayer;
 import org.opendarts.prototype.service.game.IGameService;
 import org.opendarts.prototype.service.player.IPlayerService;
 import org.opendarts.prototype.ui.dialog.ThreeDartsComputerDialog;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class ThreeDartsComputerDialog.
  */
 public class DartsComputerX01Dialog extends ThreeDartsComputerDialog {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(DartsComputerX01Dialog.class);
 
 	/** The Constant DELAY. */
 	private static final int DELAY = 1 * 1000; // 1s 
 
 	/** The entry. */
 	private final IGameEntry entry;
 
 	/** The player. */
 	private final IPlayer player;
 
 	/** The score. */
 	private int score;
 
 	/** The game service. */
 	private final IGameService gameService;
 
 	/** The player service. */
 	private final IPlayerService playerService;
 
 	/**
 	 * Instantiates a new computer throw.
 	 *
 	 * @param parentShell the parent shell
 	 * @param player the player
 	 * @param game the game
 	 * @param entry the entry
 	 */
 	public DartsComputerX01Dialog(Shell parentShell, IPlayer player,
 			GameX01 game, GameX01Entry entry) {
 		super(parentShell, player, game, entry);
 		this.player = player;
 		this.entry = entry;
 		this.gameService = ProtoPlugin.getService(IGameService.class);
 		this.playerService = ProtoPlugin.getService(IPlayerService.class);
 		this.score = ((GameX01) game).getScore(player);
 	}
 
 	/**
 	 * Gets the form title.
 	 *
 	 * @return the form title
 	 */
 	public String getFormTitle() {
 		return MessageFormat.format("#{1} round for {0} - Starting at {2}",
 				this.player, this.entry.getRound(), this.score);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.FormDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
 	 */
 	/**
 	 * Creates the button bar.
 	 *
 	 * @param parent the parent
 	 * @return the control
 	 */
 	@Override
 	protected Control createButtonBar(Composite parent) {
		return new Label(parent, SWT.NONE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.window.Window#getShellListener()
 	 */
 	/**
 	 * Gets the shell listener.
 	 *
 	 * @return the shell listener
 	 */
 	@Override
 	protected ShellListener getShellListener() {
 		return new ShellAdapter() {
 
 			/* (non-Javadoc)
 			 * @see org.eclipse.swt.events.ShellAdapter#shellClosed(org.eclipse.swt.events.ShellEvent)
 			 */
 			public void shellClosed(ShellEvent event) {
 				event.doit = false; // don't close now
 				if (canHandleShellCloseEvent()) {
 					handleShellCloseEvent();
 				}
 			}
 
 			/* (non-Javadoc)
 			 * @see org.eclipse.swt.events.ShellAdapter#shellActivated(org.eclipse.swt.events.ShellEvent)
 			 */
 			public void shellActivated(ShellEvent event) {
 				// Throw darts
 				ThrowDartsJob job;
 				for (int i = 0; i < getDarts().length; i++) {
 					job = new ThrowDartsJob(i);
 					job.schedule(DELAY * (i + 1));
 				}
 
 				// Close
 				UIJob closeJob = new UIJob("Closer") {
 					@Override
 					public IStatus runInUIThread(IProgressMonitor monitor) {
 						try {
 							if (getDartThrow() == null) {
 								setDartThrow(new ThreeDartThrow(getDarts()));
 							}
 							LOG.info("Throw: {}", getDartThrow());
 						} catch (InvalidDartThrowException e) {
 							LOG.error("WTF, computer is dumb?", e);
 						}
 						close();
 						return Status.OK_STATUS;
 					}
 				};
 				closeJob.schedule(5 * DELAY);
 			}
 		};
 	}
 
 	/**
 	 * The Class ThrowDartsJob.
 	 */
 	private class ThrowDartsJob extends UIJob {
 
 		/** The index. */
 		private final int index;
 
 		/**
 		 * Instantiates a new throw darts job.
 		 *
 		 * @param score the score
 		 * @param i the i
 		 */
 		public ThrowDartsJob(int i) {
 			super("Throw Dart");
 			this.index = i;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
 		 */
 		@Override
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			try {
 				if (getDartThrow() != null) {
 					return Status.CANCEL_STATUS;
 				}
 				IDart dart = throwDart(score, this.index);
 				score -= dart.getScore();
 				getDarts()[this.index] = dart;
 				displayDart(dart, score, this.index);
 			} catch (InvalidDartThrowException e) {
 				LOG.error("WTF, the computer is not allowed to cheat!", e);
 			}
 			return Status.OK_STATUS;
 		}
 
 	}
 
 	/**
 	 * Throw dart.
 	 *
 	 * @param score the score
 	 * @param index the index
 	 * @return the thrown dart
 	 * @throws InvalidDartThrowException the invalid dart throw exception
 	 */
 	private IDart throwDart(int score, int index)
 			throws InvalidDartThrowException {
 		IDart dart = this.getDart(score, index);
 		this.getDarts()[index] = dart;
 		if (score == dart.getScore()) {
 			if (DartZone.DOUBLE.equals(dart.getZone())) {
 				// win
 				this.setDartThrow(new WinningX01DartsThrow(this.getDarts()));
 			} else {
 				// broken
 				this.setDartThrow(new BrokenX01DartsThrow(this.getDarts()));
 			}
 		} else if ((score - dart.getScore()) < 2) {
 			// broken
 			this.setDartThrow(new BrokenX01DartsThrow(this.getDarts()));
 		}
 		return dart;
 	}
 
 	/**
 	 * Gets the dart.
 	 *
 	 * @param score the score
 	 * @param index the index
 	 * @return the first dart
 	 */
 	private IDart getDart(int score, int index) {
 		IDart wished = this.gameService.chooseBestDart(score,
 				this.getDarts().length - index);
 		this.displayWished(wished, index);
 
 		IDart done = this.playerService.getComputerDart(wished);
 		LOG.debug("Computer wish: {}, done, {}", wished, done);
 		return done;
 	}
 }
