 package pi.rotary.physics;
 
 public class Mass {
 	
 	float radius;
 	float time;
 	float frequency; //angular velocity
 	long updated; //last updated
 	float angle;
 	float mass;
 	float slide; //radial acceleration
 	float pitch, roll; //for effect not accurate inertia
 	float baring; //set to PI for non radials
 	float forcetx, forcerx; //for rolling wheels
 	
 	void updatet(float torque) { //calculate torque update
 		float inertia = mass * radius * radius;
 		float theta_acc = torque / inertia;
 		updated = System.currentTimeMillis() - updated;
 		time = (float)(updated / 1000.0);
 		frequency += theta_acc * time;
 		angle += frequency * time;
 		angle = (float)Math.IEEEremainder(angle, 2 * Math.PI);
 		slide = frequency * frequency * radius;
 	}
 	
 	void updatep(float forcet, float forcer) { //apply force update
 		//tangent and radial
 		roll = forcet + forcetx;
 		float s = (float)Math.sin(baring);
 		float c = (float)Math.cos(baring);
		roll *= s;
		updatet(roll * radius);
 		float rad = (forcer + forcerx + slide) / mass;
		float pitcht = rad * s;
 		//wheel rolling effect of non lateral
		forcetx = pitcht * c;
		forcerx = roll * c;
		rad *= c;
		radius += rad * time * time;
		roll = -(roll * c - pitcht * s);
		pitch = -(pitcht * c - roll * s);
 	}
 	
 	void updatea(float forcens, float forceew) {
 		//apply absolute force directions
 		float s = (float)Math.sin(angle);
 		float c = (float)Math.cos(angle);
 		updatep(-forceew * c - forcens * s, forcens * c - forceew * s);
 	}
 	
 	float posns() {
 		return (float)(radius * Math.cos(angle));
 	}
 	
 	float posew() {
 		return (float)(radius * Math.sin(angle));
 	}
 	
 	float absYaw() {
 		return angle + baring;
 	}
 	
 	void update(float force) {
 		//update using drive force
 		float bearing = angle + baring;
 		float s = (float)Math.sin(bearing);
 		float c = (float)Math.cos(bearing);
 		updatea(force * c - force * s, -force * s - force * c);
 	}
 	
 	void setPos(float ns, float ew, float absYaw) {
 		radius = (float)Math.sqrt(ns * ns + ew * ew);
 		angle = (float)Math.atan2(ns, ew);
 		baring = absYaw - angle;
 	}
 }
