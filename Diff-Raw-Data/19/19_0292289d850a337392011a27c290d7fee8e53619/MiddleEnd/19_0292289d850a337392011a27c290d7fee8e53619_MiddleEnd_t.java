 package middleend;
 
 import exception.BackupException;
 import exception.CSVException;
 import fileio.CSVIO;
 import fileio.SerialIO;
 import gui.*;
 import backbone.*;
 import java.io.File;
 
 public class MiddleEnd extends Thread {
 	
 	TMNTScheduler _scheduler;
 	Tournament _tmnt;
 	App _app;
	boolean _continue;
 	
 	public MiddleEnd(Tournament t, TMNTScheduler s) {
 		_tmnt = t;
 		_scheduler = s;
		_continue = true;
 		_app = new App(this);
 	}
 	
 	public void repaintAll() {
 		_app.repaintAll();
 	}
 	
 	public void openNewMiddleEnd() {
 		_scheduler.addTMNT(_tmnt.getNew());
 	}
 	
 	public void openNewMiddleEnd(Tournament t) {
 		_scheduler.addTMNT(t);
 	}
 	
	public void closeThisMiddleEnd() {
		_scheduler.removeTMNT(_tmnt);
		this.quit();
	}
	
	public void quit() {
		_continue = false;
	}
	
 	public Tournament getTournament() {
 		return _tmnt;
 	}
 	
 	public void setTournament(Tournament t) {
 		_tmnt = t;
 	}
 	
 	public boolean openTournament(File file) {
 		try {
 			_scheduler.addTMNT(SerialIO.readTournament(file));
 			return true;
 		} catch (BackupException e) {
 			return false;
 		}
 	}
 	
 	public boolean saveTournament(File file) {
 		try {
 			SerialIO.writeTournament(file, _tmnt);
 			return true;
 		} catch (BackupException e) {
 			return false;
 		}
 	}
 	
 	public boolean importCategory(File file) {
 		try {
 			CSVIO.loadGrouping(file, _tmnt);
 			return true;
 		} catch (CSVException e) {
 			return false;
 		}
 	}
 	
 	public boolean exportCategory(Grouping g, File file) {
 		try {
 			CSVIO.writeGrouping(file, g);
 			return true;
 		} catch (CSVException e) {
 			return false;
 		}
 	}
	
	public void run() {
		while (_continue)
		{}
	}
 }
