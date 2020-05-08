 import java.util.LinkedList;
 import java.util.List;
 
 class Physics {
 
 	private Game game;
 
 	public Physics(Game game) {
 		this.game = game;
 	}
 
 	@SuppressWarnings("unused")
 	private void gravity(Ball b) {
 		b.accelerate(new Vektor(b.position, game.getCenter(), 9 / game.getCenter().sub(b.position).getLength()));
 	}
 
 	private void indirectGravity(Ball b) {
 		Vektor difference = b.position.sub(game.getCenter());
 		double length = difference.getLength();
		difference.setLength(length * 0.0001 + 0.2);
 		b.accelerate(difference);
 	}
 
 	private void jointSpring(Joint j) {
 		Vektor real_diff = j.a.position.sub(j.b.position);
 		double real_length = real_diff.getLength();
 		double wish_length = j.getLength();
 		Vektor wish_diff = Vektor.polar(real_diff.getAngle(), wish_length);
 		Vektor force = wish_diff.sub(real_diff);
 		force = force.mul((real_length / wish_length) * j.getStrength());
 		j.a.accelerate(force);
 		j.b.accelerate(force.mul(-1));
 	}
 
 	private void collide(Collision c) {
 		if ((c.a == game.active || c.b == game.active) && c.a.color.equals(c.b.color))
 			game.replaceBall(c.a, c.b);
 		Vektor diff2 = c.diff.mul(0.05);
 		c.a.accelerate(diff2.mul(-1));
 		c.b.accelerate(diff2);
 	}
 
 	private void moveBall(Ball b) {
 		b.speed = b.speed.add(b.acceleration);
 		b.position = b.position.add(b.speed);
 	}
 
 	private void friction(Ball b) {
 		b.accelerate(b.speed.mul(-0.2));
 	}
 
 	private void moveActiveBall() {
 		game.active.position = game.getPointer();
 		game.active.accelerate(game.active.position.sub(game.getPointer()));
 	}
 
 	public void physik() {
 		List<Collision> collisions = new LinkedList<Collision>();
 		// collisions = Collections.synchronizedList(collisions);
 		int balls_count = game.balls.size();
 		if (balls_count > 1) {
 			for (int i = 0; i < balls_count - 1; i++) {
 				for (int j = i + 1; j < balls_count; j++) {
 
 					Collision collision = Collision.test(game.balls.get(i), game.balls.get(j));
 					if (collision != null)
 						collisions.add(collision);
 				}
 			}
 		}
 		if (game.active != null)
 			moveActiveBall();
 
 		for (Ball b : game.balls) {
 			b.acceleration = new Vektor();
 			// gravity(b);
 			indirectGravity(b);
 			friction(b);
 		}
 		for (Collision c : collisions)
 			collide(c);
 		for (Joint j : game.joints)
 			jointSpring(j);
 		for (Ball b : game.balls)
 			moveBall(b);
 	}
 }
