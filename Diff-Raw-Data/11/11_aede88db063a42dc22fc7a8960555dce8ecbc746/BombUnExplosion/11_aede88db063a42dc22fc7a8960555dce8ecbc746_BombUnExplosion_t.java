 package Standard;

 import java.util.TimerTask;
 
 public class BombUnExplosion extends TimerTask {
 	private Bomberman bm;
 	/** Bomb-Objekt */
 	private Bomb bomb;
 	/** Spielfeld-Referenz */
 	private Spielfeld sp;
 
 	/**
 	 * 
 	 * @param b
 	 *            Übergabe von zu behandelnder Bombe
 	 * @param sp
 	 *            Übergabe des Spielfelds
 	 */
 	public BombUnExplosion(Bomb b, Bomberman bm, Spielfeld sp) {
 		this.bm = bm;
 		this.bomb = b;
 		this.sp = sp;
 	}
 
 	@Override
 	/**
 	 * Bombenexplosion ausschalten, Bombe unsichtbar setzen, Raster auf begehbar schalten
 	 */
 	public void run() {
 		bomb.setExploded(false);
 		bomb.setVisible(false);
 		sp.raster[bomb.getPosX()][bomb.getPosY()] = 0;
 
 		if (bm.bombs.contains(bomb)) {
 			this.bomb.getOwner().decBombs();
 			System.out.println("BUE:2:" + bm.getCounter());
 			this.bm.bombs.remove(bomb);
 		}
 		sp.repaint();
 	}
}
