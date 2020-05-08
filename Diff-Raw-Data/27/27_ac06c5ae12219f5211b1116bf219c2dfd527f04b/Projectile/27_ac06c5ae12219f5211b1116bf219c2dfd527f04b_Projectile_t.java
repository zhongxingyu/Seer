 package server;
 
 public class Projectile extends Entity {
 	private int damage;
 	private double radius;
 	private boolean isAlive;
 	private Player author;
 
 	private int x0, y0;
 	private int x1, y1;
 	private boolean deathSent;
 
 	public int getDamage() {
 		return damage;
 	}
 
 	public void setDamage(int damage) {
 		this.damage = damage;
 	}
 
 	public double getRadius() {
 		return radius;
 	}
 
 	public void setRadius(double radius) {
 		this.radius = radius;
 	}
 
 	public Projectile(int playerX, int playerY, int targetX, int targetY,
 			int damage, double radius, Player p) {
 		this.damage = damage;
 		this.radius = radius;
 
 		x0 = playerX;
 		y0 = playerY;
 
 		x1 = targetX;
 		y1 = targetY;
 
 		this.setCoord(x0, y0);
 		this.putParameter("move-delay", "0");
 		this.putParameter("type", "3");
 
 		Map.getInstance().getAllProjectiles().add(this);
 		isAlive = true;
 		author = p;
 		deathSent = false;
 	}
 
 	public void moveToTarget() {
 		if (!isAlive())
 			return;
 		TileContainer[][] tileMatrix = Map.getInstance().getTileMatrix();
 
 		int dx = Math.abs(x1 - getX());
 		int dy = Math.abs(y1 - getY());
 		double r = (dy + 0.) / (dx + 0.);
 		int mx = 0, my = 0;
 		if (r > Math.sqrt(3)) {
 			mx = 0;
 			my = (int) Math.signum(y1 - getY());	
 		} else if (r > 1. / Math.sqrt(3)) {
 			mx = (int) Math.signum(x1 - getX());
 			my = (int) Math.signum(y1 - getY());
 		} else {
 			mx = (int) Math.signum(x1 - getX());
 			my = 0;
 		}
 		int X = getX() + mx, Y = getY() + my;
 
 		if ((!tileMatrix[X][Y].getTile().isWalkable()
 				|| Math.hypot(X - x0, Y - y0) > radius 
 				|| (getX() == x1 && getY() == y1))) {
 			dissipate(X, Y);
 			return;
 		}
 		if (tileMatrix[X][Y].getCreatures().size() > 0 
 				&& !tileMatrix[X][Y].getCreatures().get(0).equals(author)) {
 			dissipate(X, Y);
 			return;
 		}
 		tryToMoveTo(X, Y);
 	}
 
 	private void dissipate(int X, int Y) {
 		TileContainer[][] tileMatrix = Map.getInstance().getTileMatrix();
 		for (Creature c : tileMatrix[X][Y].getCreatures()) {
 			if (!c.equals(author)) {
 				c.hit(damage);
 			}
 		}
 		isAlive = false;
 	}
 	
 	public String getParameterStrings() {
		if (getDeathSent()) return "";
		if (!isAlive()) {
			setDeathSent(true);
			this.putParameter("alive", "false");
 		}
		return super.getParameterStrings();
 	}
 
 	private void setDeathSent(boolean b) {
 		deathSent = b;
 	}
 
 	public boolean getDeathSent() {
 		return deathSent;
 	}
 
 	public boolean isAlive() {
 		return isAlive;
 	}
 
 	protected void changeCoord(int x, int y) {
 		TileContainer[][] tileMatrix = Map.getInstance().getTileMatrix();
 		tileMatrix[getX()][getY()].removeProjectile(this);
 		putParameter("x", x + "");
 		putParameter("y", y + "");
 		tileMatrix[getX()][getY()].addProjectile(this);
 	}
 
 	protected void setCoord(int x, int y) {
 		TileContainer[][] tileMatrix = Map.getInstance().getTileMatrix();
 		putParameter("x", x + "");
 		putParameter("y", y + "");
 		tileMatrix[getX()][getY()].addProjectile(this);
 	}
 }
