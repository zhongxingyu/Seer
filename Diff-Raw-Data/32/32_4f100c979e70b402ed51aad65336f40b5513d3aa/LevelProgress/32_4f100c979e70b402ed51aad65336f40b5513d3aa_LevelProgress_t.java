 package jig.ironLegends;
 
 public class LevelProgress {
 	double m_introRemainingMs;
 	boolean m_bExitActivated;
 	boolean m_bExitComplete;
 	protected int m_levelScore;
 	protected int m_tanksDestroyed = 0;
 	protected int m_tanksToDestroy = 0;
	protected boolean m_addNewTank = false;
	
 	public LevelProgress() {
 		reset();
 	}
 
 	public double introRemaining() {
 		return m_introRemainingMs;
 	}
 
 	public boolean isIntro() {
 		return m_introRemainingMs > 0;
 	}
 
 	public void setIntro(double introMs) {
 		m_introRemainingMs = introMs;
 	}
 
 	public void setExitComplete(boolean bComplete) {
 		m_bExitComplete = bComplete;
 	}
 
 	public void setExit(boolean bExit) {
 		if (bExit) {
 			m_bExitActivated = true;
 			m_bExitComplete = false;
 		} else {
 			m_bExitActivated = false;
 			m_bExitComplete = true;
 		}
 	}
 
 	public boolean isExitActivated() {
 		return m_bExitActivated;
 	}
 
 	public boolean isExitComplete() {
 		return m_bExitComplete;
 	}
 
 	public boolean didWin() {
 		
 		if (getTanksRemaining() <= 0)
 			return true;
 		return false;
 	}
 	
 	public void update(double deltaMs) {
 		if (m_introRemainingMs > 0) {
 			m_introRemainingMs -= deltaMs;
 		}
 	}
 
 	public void reset() {
 		m_levelScore = 0;
 		m_introRemainingMs = 0;
 		m_bExitActivated = false;
 		m_bExitComplete = false;
 		m_tanksDestroyed = 0;
 		m_tanksToDestroy = 0;
		setAddNewTank(false);
 	}
 
 	public int getScore() {
 		return m_levelScore;
 	}
 	
 	public void setScore(int score) {
 		m_levelScore = score;
 	}
 
 	public int getTanksRemaining() {
 		return m_tanksToDestroy-m_tanksDestroyed;
 	}
 
 	public void tankDestroyed() {
 		m_tanksDestroyed++;
 		
 	}
 
 	public void setTanksToDestroy(int i) {
 		m_tanksToDestroy = i;
 		
 	}
 
 	public void setTanksDestroyed(int i) {
 		m_tanksDestroyed = i;		
 	}

	public void setAddNewTank(boolean m_addNewTank) {
		this.m_addNewTank = m_addNewTank;
	}

	public boolean isAddNewTank() {
		return m_addNewTank;
	}
 	
 }
