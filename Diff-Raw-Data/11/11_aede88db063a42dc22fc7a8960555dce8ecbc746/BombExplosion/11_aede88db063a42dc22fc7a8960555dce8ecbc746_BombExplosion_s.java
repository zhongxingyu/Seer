 package Standard;
 import java.util.TimerTask;
 
 public class BombExplosion extends TimerTask {
 	/** Bombe */
 	private Bomb bomb;
 	/** Spielfeld */
 	private Spielfeld sp;
 	/** Bombermänner */
 	Bomberman bm, bm2;
 
 	/**
 	 * 
 	 * @param b
 	 * @param sp
 	 * @param man
 	 */
 	public BombExplosion(Bomb b, Spielfeld sp, Bomberman man) {
 		this.bomb = b;
 		this.sp = sp;
 		this.bm = man;
 	}
 
 	/**
 	 * 
 	 * @param b
 	 * @param sp
 	 * @param man
 	 * @param man2
 	 */
 	public BombExplosion(Bomb b, Spielfeld sp, Bomberman man, Bomberman man2) {
 		this.bomb = b;
 		this.sp = sp;
 		this.bm = man;
 		this.bm2 = man2;
 	}
 
 	/**
 	 * Bombe explodieren lassen
 	 */
 	public void run() {
 		bomb.setExploded(true);
 		this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY()] = 0;
 		System.out.println("BE:1:" + bm.getCounter());
 
 		// Zerstoerung der Mauern + Bombermans töten
 		// oben & unten#
 
 		// oben
 		for (int i = 1; i * this.bomb.getRadius() <= this.bomb
 				.getExplosionRadius(); i++) {
 			switch (this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY() - i
 					* this.bomb.getRadius()]) {
 			case -2: { // PowerUp zerstören
 
 			}
 				break;
 			case 1: { // unzerstoerbare mauer stoppt explosion
 				i = 100;
 			}
 				break;
 			case 2: { // zerstörbare mauer kapputt machen
 				this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY() - i
 						* this.bomb.getRadius()] = 0;
 			}
 				break;
 			case 3: { // Kettenreaktion
 				if (bm2 != null) {
 					for (int j = 0; j < Math.max(bm.getCounter(),
 							bm2.getCounter()); j++) {
 						kettenreaktion(bm, bm2, 1, i, j);
 					}
 				} else {
 					for (int j = 0; j < bm.getCounter(); j++) {
 						kettenreaktion(bm, 1, i, j);
 					}
 				}
 			}
 				break;
 			case 4: { // zerstörbare mauer kapputt machen, exit setzen
 				this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY() - i
 						* this.bomb.getRadius()] = -1;
 			}
 				break;
 			default: {
 
 			}
 			}
 			if (i < 100) {
 				if (bm2 == null)
 					kill(bm, 1, i);
 				else
 					kill(bm, bm2, 1, i);
 			}
 
 		}
 		// unten
 		for (int i = 1; i * this.bomb.getRadius() <= this.bomb
 				.getExplosionRadius(); i++) {
 			switch (this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY() + i
 					* this.bomb.getRadius()]) {
 			case -2: {
 				// PowerUp zerstören
 			}
 				break;
 			case 1: { // unzerstoerbare mauer stoppt explosion
 				i = 100;
 			}
 				break;
 			case 2: { // zerstörbare mauer kapputt machen
 				this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY() + i
 						* this.bomb.getRadius()] = 0;
 			}
 				break;
 			case 3: { // Kettenreaktion
 				if (bm2 != null) {
 					for (int j = 0; j < Math.max(bm.getCounter(),
 							bm2.getCounter()); j++) {
 						kettenreaktion(bm, bm2, 3, i, j);
 					}
 				} else {
 					for (int j = 0; j < bm.getCounter(); j++) {
 						kettenreaktion(bm, 3, i, j);
 					}
 				}
 			}
 				break;
 			case 4: { // zerstörbare mauer kapputt machen, exit setzen
 				this.sp.raster[this.bomb.getPosX()][this.bomb.getPosY() + i
 						* this.bomb.getRadius()] = -1;
 			}
 				break;
 			default: {
 			}
 			}
 			if (i < 100) {
 				if (bm2 == null)
 					kill(bm, 3, i);
 				else
 					kill(bm, bm2, 3, i);
 			}
 
 		}
 
 		// links&rechts
 
 		// links
 		for (int i = 1; i * this.bomb.getRadius() <= this.bomb
 				.getExplosionRadius(); i++) {
 			switch (this.sp.raster[this.bomb.getPosX() - i
 					* this.bomb.getRadius()][this.bomb.getPosY()]) {
 			case -2: {
 				// PowerUp zerstören
 			}
 				break;
 			case 1: { // unzerstoerbare mauer stoppt explosion
 				i = 100;
 			}
 				break;
 			case 2: {
 				this.sp.raster[this.bomb.getPosX() - i * this.bomb.getRadius()][this.bomb
 						.getPosY()] = 0;
 			}
 				break;
 			case 3: {
 				if (bm2 != null) {
 					for (int j = 0; j < Math.max(bm.getCounter(),
 							bm2.getCounter()); j++) {
 						kettenreaktion(bm, bm2, 4, i, j);
 					}
 				} else {
 					for (int j = 0; j < bm.getCounter(); j++) {
 						kettenreaktion(bm, 4, i, j);
 					}
 				}
 			}
 				break;
 			case 4: {
 				this.sp.raster[this.bomb.getPosX() - i * this.bomb.getRadius()][this.bomb
 						.getPosY()] = -1;
 			}
 			}
 			if (i < 100) {
 				if (bm2 == null)
 					kill(bm, 4, i);
 				else
 					kill(bm, bm2, 4, i);
 			}
 		}
 		// rechts
 		for (int i = 1; i * this.bomb.getRadius() <= this.bomb
 				.getExplosionRadius(); i++) {
 			switch (this.sp.raster[this.bomb.getPosX() + i
 					* this.bomb.getRadius()][this.bomb.getPosY()]) {
 			case -2: {
 				// PowerUp zerstören
 			}
 				break;
 			case 1: { // unzerstoerbare mauer stoppt explosion
 				i = 100;
 			}
 				break;
 			case 2: {
 				this.sp.raster[this.bomb.getPosX() + i * this.bomb.getRadius()][this.bomb
 						.getPosY()] = 0;
 			}
 				break;
 			case 3: {
 				if (bm2 != null) {
 					for (int j = 0; j < Math.max(bm.getCounter(),
 							bm2.getCounter()); j++) {
 						kettenreaktion(bm, bm2, 2, i, j);
 					}
 				} else {
 					for (int j = 0; j < bm.getCounter(); j++) {
 						kettenreaktion(bm, 2, i, j);
 					}
 				}
 			}
 			case 4: {
 				this.sp.raster[this.bomb.getPosX() + i * this.bomb.getRadius()][this.bomb
 						.getPosY()] = -1;
 			}
 			}
 			if (i < 100) {
 				if (bm2 == null)
 					kill(bm, 2, i);
 				else
 					kill(bm, bm2, 2, i);
 			}
 		}
 		/** Bomberman steht AUF der Bombe */
 		if ((bm.getPosX() == this.bomb.getPosX())
 				&& (bm.getPosY() == this.bomb.getPosY())) {
 			this.sp.repaint();
 			Main.f.dispose(
 					"Deine eigene Bombe ist explodiert und hat dich mitgerissen!",
 					true);
 
 			Main.f.restart(1);
 		} else if (bm2 != null)
 			if ((bm.getPosX() == this.bomb.getPosX())
 					&& (bm.getPosY() == this.bomb.getPosY())
 					|| (bm2.getPosX() == this.bomb.getPosX())
 					&& (bm2.getPosY() == this.bomb.getPosY())) {
 				this.sp.repaint();
 				Main.f.dispose(
 						"Deine eigene Bombe ist explodiert und hat dich mitgerissen!",
 						true);
 
 				Main.f.restart(2);
 			}
 		this.sp.repaint();
 	}
 
 	//
 	// töten mit 1 spieler
 	//
 
 	private void kill(Bomberman man, int direction, int i) {
 		switch (direction) {
 		case 1: { // oben
 			if ((man.getPosX() == this.bomb.getPosX())
 					&& (man.getPosY() == this.bomb.getPosY() - i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				Main.f.dispose(
 						"Deine eigene Bombe ist explodiert und hat dich mitgerissen!",
 						true);
 
 				Main.f.restart(1);
 			}
 		}
 			break;
 		case 2: { // rechts
 			if ((man.getPosY() == this.bomb.getPosY())
 					&& (man.getPosX() == this.bomb.getPosX() + i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				Main.f.dispose(
 						"Deine eigene Bombe ist explodiert und hat dich mitgerissen!",
 						true);
 
 				Main.f.restart(1);
 			}
 		}
 			break;
 		case 3: { // unten
 			if ((man.getPosX() == this.bomb.getPosX())
 					&& (man.getPosY() == this.bomb.getPosY() + i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				Main.f.dispose(
 						"Deine eigene Bombe ist explodiert und hat dich mitgerissen!",
 						true);
 
 				Main.f.restart(1);
 			}
 		}
 			break;
 		case 4: { // links
 			if ((man.getPosY() == this.bomb.getPosY())
 					&& (man.getPosX() == this.bomb.getPosX() - i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				Main.f.dispose(
 						"Deine eigene Bombe ist explodiert und hat dich mitgerissen!",
 						true);
 
 				Main.f.restart(1);
 			}
 		}
 			break;
 		}
 		/*
 		 * if ((this.bm.getPosX() == this.bomb.getPosX()) && (this.bm.getPosY()
 		 * == this.bomb.getPosY())) { this.sp.repaint();
 		 * System.out.println("Lalala"); Main.f.dispose(
 		 * "Deine eigene Bombe ist explodiert und hat dich mitgerissen!", true);
 		 * 
 		 * Main.f.restart(1); }
 		 */
 	}
 
 	//
 	// Behandlung für Bomberman im Zweispielermodus -----------------------
 	//
 
 	private void kill(Bomberman man, Bomberman man2, int direction, int i) {
 		switch (direction) {
 		case 1: { // oben
 			if ((man.getPosX() == this.bomb.getPosX())
 					&& (man.getPosY() == this.bomb.getPosY() - i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man2.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			} else if ((man2.getPosX() == this.bomb.getPosX())
 					&& (man2.getPosY() == this.bomb.getPosY() - i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man2.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			}
 		}
 			break;
 		case 2: { // rechts
 			if ((man.getPosY() == this.bomb.getPosY())
 					&& (man.getPosX() == this.bomb.getPosX() + i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man2.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			} else if ((man2.getPosY() == this.bomb.getPosY())
 					&& (man2.getPosX() == this.bomb.getPosX() + i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man2.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			}
 		}
 			break;
 		case 3: { // unten
 			if ((man.getPosX() == this.bomb.getPosX())
 					&& (man.getPosY() == this.bomb.getPosY() + i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man2.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			} else if ((man2.getPosX() == this.bomb.getPosX())
 					&& (man2.getPosY() == this.bomb.getPosY() + i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man2.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			}
 		}
 			break;
 		case 4: { // links
 			if ((man.getPosY() == this.bomb.getPosY())
 					&& (man.getPosX() == this.bomb.getPosX() - i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man2.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			} else if ((man2.getPosY() == this.bomb.getPosY())
 					&& (man2.getPosX() == this.bomb.getPosX() - i
 							* this.bomb.getRadius())) {
 				this.sp.repaint();
 				/** Andere Explosionen abbrechen */
 				for (int f = 0; f < man.bombs.size(); f++) {
 					man.bombs.get(f).tExp.cancel();
 					man.bombs.get(f).tUnExp.cancel();
 				}
 				for (int g = 0; g < man2.bombs.size(); g++) {
 					man2.bombs.get(g).tExp.cancel();
 					man2.bombs.get(g).tUnExp.cancel();
 				}
 				if (man2.equals(this.bomb.getOwner())) {
 
 					Main.f.dispose("Du hast dich selbst getötet!", true);
 					Main.f.restart(2);
 				} else {
 					man.addScore(3);
 					Main.f.dispose("Dein Gegenspieler hat dich getötet!", true);
 					Main.f.restart(2);
 				}
 			}
 		}
 			break;
 		}
 		/*
 		 * if (((man.getPosX() == this.bomb.getPosX()) && (man.getPosY() ==
 		 * this.bomb .getPosY())) || ((man2.getPosX() == this.bomb.getPosX()) &&
 		 * (man2.getPosY() == this.bomb .getPosY()))) { this.sp.repaint();
 		 * System.out.println("Lalala"); Main.f.dispose(
 		 * "Deine eigene Bombe ist explodiert und hat dich mitgerissen!", true);
 		 * 
 		 * Main.f.restart(2); }
 		 */
 
 	}
 
 	//
 	// Kettenreaktion 1 spieler ----------------------------------------
 	//
 
 	private void kettenreaktion(Bomberman man, int direction, int i, int j) {
 		ExplosionThread t = new ExplosionThread(sp, bomb, man, direction, i, j);
 		t.run();
 	}
 
 	//
 	// Kettenreaktion 2 spieler -----------------------------------------
 	//
 
 	private void kettenreaktion(Bomberman man, Bomberman man2, int direction,
 			int i, int j) {
 		if ((man.getCounter() < j) || (man.getCounter() == 0)) {
 			kettenreaktion(man2, direction, i, j);
 		} else if ((man2.getCounter() < j) || (man2.getCounter() == 0)) {
 			kettenreaktion(man, direction, i, j);
 		} else {
 			switch (direction) {
 			case 1: { // oben
 				if (((man.bombs.get(j).getPosY() == this.bomb.getPosY() - i
 						* this.bomb.getRadius()) && (man.bombs.get(j).getPosX() == this.bomb
 						.getPosX()))) {
 					System.out.println("Bombe gefunden");
 					man.bombs.get(j).tExp.schedule(
 							new BombExplosion(man.bombs.get(j), sp, man), 0);
 					man.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man.bombs.get(j), man, sp), 500); // 500
 																// ist
 																// Differenz
 																// zwischen
 																// den
 																// TImern
 																// !!
 				}
 				if (((man2.bombs.get(j).getPosY() == this.bomb.getPosY() - i
 						* this.bomb.getRadius()) && (man2.bombs.get(j)
 						.getPosX() == this.bomb.getPosX()))) {
 					System.out.println("Bombe gefunden");
 					man2.bombs.get(j).tExp.schedule(new BombExplosion(
 							man2.bombs.get(j), sp, man2), 0);
 					switch (direction) {
 					case 1: { // oben
 						if ((man.bombs.get(j).getPosY() == this.bomb.getPosY()
 								- i * this.bomb.getRadius())
 								&& (man.bombs.get(j).getPosX() == this.bomb
 										.getPosX())) {
 							System.out.println("Bombe gefunden");
 							man.bombs.get(j).tExp.schedule(new BombExplosion(
 									man.bombs.get(j), sp, man), 0);
 							man.bombs.get(j).tUnExp.schedule(
 									new BombUnExplosion(man.bombs.get(j), man,
 											sp), 500); // 500
 														// ist
 														// Differenz
 														// zwischen
 														// den
 														// TImern
 														// !!
 						}
 					}
 						break;
 					case 2: { // rechts
 						if ((man.bombs.get(j).getPosX() == this.bomb.getPosX()
 								+ i * this.bomb.getRadius())
 								&& (man.bombs.get(j).getPosY() == this.bomb
 										.getPosY())) {
 							System.out.println("Bombe gefunden");
 							man.bombs.get(j).tExp.schedule(new BombExplosion(
 									man.bombs.get(j), sp, man), 0);
 							man.bombs.get(j).tUnExp.schedule(
 									new BombUnExplosion(man.bombs.get(j), man,
 											sp), 500); // 500
 														// ist
 														// Differenz
 														// zwischen
 														// den
 														// TImern
 														// !!
 						}
 					}
 						break;
 					case 3: { // unten
 						if ((man.bombs.get(j).getPosY() == this.bomb.getPosY()
 								+ i * this.bomb.getRadius())
 								&& (man.bombs.get(j).getPosX() == this.bomb
 										.getPosX())) {
 							System.out.println("Bombe gefunden");
 							man.bombs.get(j).tExp.schedule(new BombExplosion(
 									man.bombs.get(j), sp, man), 0);
 							man.bombs.get(j).tUnExp.schedule(
 									new BombUnExplosion(man.bombs.get(j), man,
 											sp), 500); // 500
 														// ist
 														// Differenz
 														// zwischen
 														// den
 														// TImern
 														// !!
 						}
 					}
 						break;
 					case 4: { // links
 						if ((man.bombs.get(j).getPosX() == this.bomb.getPosX()
 								- i * this.bomb.getRadius())
 								&& (man.bombs.get(j).getPosY() == this.bomb
 										.getPosY())) {
 							System.out.println("Bombe gefunden");
 							man.bombs.get(j).tExp.schedule(new BombExplosion(
 									man.bombs.get(j), sp, man), 0);
 							man.bombs.get(j).tUnExp.schedule(
 									new BombUnExplosion(man.bombs.get(j), man,
 											sp), 500); // 500
 														// ist
 														// Differenz
 														// zwischen
 														// den
 														// TImern
 														// !!
 						}
 					}
 					}
 					man2.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man2.bombs.get(j), man2, sp), 500); // 500 ist
 					// Differenz
 					// zwischen
 					// den
 					// TImern
 					// !!
 				}
 			}
 				break;
 			case 2: { // rechts
 				if (((man.bombs.get(j).getPosY() == this.bomb.getPosY()) && (man.bombs
 						.get(j).getPosX() == this.bomb.getPosX() + i
 						* this.bomb.getRadius()))) {
 					System.out.println("Bombe gefunden");
 					man.bombs.get(j).tExp.schedule(
 							new BombExplosion(man.bombs.get(j), sp, man), 0);
 					man.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man.bombs.get(j), man, sp), 500); // 500
 																// ist
 																// Differenz
 																// zwischen
 																// den
 																// TImern
 																// !!
 				}
 				if (((man2.bombs.get(j).getPosY() == this.bomb.getPosY()) && (man2.bombs
 						.get(j).getPosX() == this.bomb.getPosX() + i
 						* this.bomb.getRadius()))) {
 					System.out.println("Bombe gefunden");
 					man2.bombs.get(j).tExp.schedule(new BombExplosion(
 							man2.bombs.get(j), sp, man2), 0);
 					man2.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man2.bombs.get(j), man2, sp), 500); // 500 ist
 					// Differenz
 					// zwischen
 					// den
 					// TImern
 					// !!
 				}
 			}
 				break;
 			case 3: { // unten
 				if (((man.bombs.get(j).getPosY() == this.bomb.getPosY() + i
 						* this.bomb.getRadius()) && (man.bombs.get(j).getPosX() == this.bomb
 						.getPosX()))) {
 					System.out.println("Bombe gefunden");
 					man.bombs.get(j).tExp.schedule(
 							new BombExplosion(man.bombs.get(j), sp, man), 0);
 					man.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man.bombs.get(j), man, sp), 500); // 500
 																// ist
 																// Differenz
 																// zwischen
 																// den
 																// TImern
 																// !!
 				}
 				if (((man2.bombs.get(j).getPosY() == this.bomb.getPosY() + i
 						* this.bomb.getRadius()) && (man2.bombs.get(j)
 						.getPosX() == this.bomb.getPosX()))) {
 					System.out.println("Bombe gefunden");
 					man2.bombs.get(j).tExp.schedule(new BombExplosion(
 							man2.bombs.get(j), sp, man2), 0);
 					man2.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man2.bombs.get(j), man2, sp), 500); // 500 ist
 					// Differenz
 					// zwischen
 					// den
 					// TImern
 					// !!
 				}
 			}
 				break;
 			case 4: { // links
 				if (((man.bombs.get(j).getPosY() == this.bomb.getPosY()) && (man.bombs
 						.get(j).getPosX() == this.bomb.getPosX() - i
 						* this.bomb.getRadius()))) {
 					System.out.println("Bombe gefunden");
 					man.bombs.get(j).tExp.schedule(
 							new BombExplosion(man.bombs.get(j), sp, man), 0);
 					man.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man.bombs.get(j), man, sp), 500); // 500
 																// ist
 																// Differenz
 																// zwischen
 																// den
 																// TImern
 																// !!
 				}
 				if (((man2.bombs.get(j).getPosY() == this.bomb.getPosY()) && (man2.bombs
 						.get(j).getPosX() == this.bomb.getPosX() - i
 						* this.bomb.getRadius()))) {
 					System.out.println("Bombe gefunden");
 					man2.bombs.get(j).tExp.schedule(new BombExplosion(
 							man2.bombs.get(j), sp, man2), 0);
 					man2.bombs.get(j).tUnExp.schedule(new BombUnExplosion(
 							man2.bombs.get(j), man2, sp), 500); // 500 ist
 					// Differenz
 					// zwischen
 					// den
 					// TImern
 					// !!
 				}
 			}
 			}
 		}
 	}
 }
