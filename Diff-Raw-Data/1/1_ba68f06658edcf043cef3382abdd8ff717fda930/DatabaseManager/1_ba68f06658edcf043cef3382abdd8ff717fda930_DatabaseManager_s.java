 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui;
 
 import java.sql.SQLException;
 
 import javax.swing.JOptionPane;
 
 import eu.lp0.cursus.db.DatabaseVersionException;
 import eu.lp0.cursus.util.Constants;
 import eu.lp0.cursus.util.Messages;
 
 class DatabaseManager {
 	private final MainWindow win;
 
 	DatabaseManager(MainWindow win) {
 		this.win = win;
 	}
 
 	/**
 	 * Try and save the database if one is open
 	 * 
 	 * @param action
 	 *            text name of the action being performed
 	 * @return true if the database was saved or discarded
 	 */
 	boolean trySaveDatabase(String action) {
 		if (win.getMain().isOpen() && !win.getDatabase().isSaved()) {
 			switch (JOptionPane.showConfirmDialog(win, String.format(Messages.getString("warn.current-db-not-saved"), win.getDatabase().getName()), //$NON-NLS-1$
 					Constants.APP_NAME + Constants.EN_DASH + action, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
 			case JOptionPane.YES_OPTION:
 				if (saveDatabase()) {
 					return win.getMain().close();
 				}
 			case JOptionPane.NO_OPTION:
 				win.getDatabase().close(true);
 				break;
 			case JOptionPane.CANCEL_OPTION:
 				return false;
 			}
 		}
 		return true;
 	}
 
 	void newDatabase() {
 		if (trySaveDatabase(Messages.getString("menu.file.new"))) { //$NON-NLS-1$
 			boolean ok = true;
 			try {
 				ok = win.getMain().open();
 			} catch (SQLException e) {
 				// TODO log
 				ok = false;
 			} catch (DatabaseVersionException e) {
 				// TODO log
 				ok = false;
 			}
 			if (!ok) {
 				JOptionPane.showMessageDialog(win, Messages.getString("err.unable-to-create-new-db"), Constants.APP_NAME, JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
 			}
 		}
 	}
 
 	boolean openDatabase() {
 		// TODO open database
 		JOptionPane.showMessageDialog(win, Messages.getString("err.feat-not-impl"), //$NON-NLS-1$
 				Constants.APP_NAME + Constants.EN_DASH + Messages.getString("menu.file.open"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
 		return false;
 	}
 
 	boolean saveDatabase() {
 		// TODO save database to current or new file
 		JOptionPane.showMessageDialog(win, Messages.getString("err.feat-not-impl"), //$NON-NLS-1$
 				Constants.APP_NAME + Constants.EN_DASH + Messages.getString("menu.file.save"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
 		return false;
 	}
 
 	boolean saveAsDatabase() {
 		// TODO save database to new file
 		JOptionPane.showMessageDialog(win, Messages.getString("err.feat-not-impl"), //$NON-NLS-1$
 				Constants.APP_NAME + Constants.EN_DASH + Messages.getString("menu.file.save-as"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
 		return false;
 	}
 
 	void closeDatabase() {
 		if (trySaveDatabase(Messages.getString("menu.file.close"))) { //$NON-NLS-1$
 			win.getMain().close();
 		}
 	}
 }
