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
<<<<<<< HEAD
<<<<<<< HEAD
 		try {
 			_scheduler.addTMNT(SerialIO.readTournament(file));
=======
		System.out.println(_tmnt.toString());
		try {
			_scheduler.addTMNT(SerialIO.readTournament(file));
			System.out.println(_tmnt.toString());
>>>>>>> 4dce8d0a0c92439693b102ec9c5a1897aa753074
 			return true;
 		} catch (BackupException e) {
 			return false;
 		}
<<<<<<< HEAD
=======
		return false;
>>>>>>> 55df286c4cdb3d6cc03cc13bd49f3ca598666edf
=======
>>>>>>> 4dce8d0a0c92439693b102ec9c5a1897aa753074
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
 			CSVIO.writeGrouping(file, (Grouping<? extends Unit>) g);
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
