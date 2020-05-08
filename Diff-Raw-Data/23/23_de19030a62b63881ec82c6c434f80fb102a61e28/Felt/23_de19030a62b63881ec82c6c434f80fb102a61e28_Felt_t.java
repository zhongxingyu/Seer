 
 import static java.lang.Math.abs;
 
 
 public class Felt extends SimplePlane {
 
 	public Felt(int width, int height) {
 
 		super(width, height); //init the plane
 
 	}
 
 
 	/* rub rubs the balls across the felt
 	 * and handles bouncing off the edges 
 	 * of the table 
 	 *
 	 * collision with other balls is handled 
 	 * elsewhere
 	 */
 
 	public void rub(Rack rack) {
 
                 for(int i=0; i < rack.set.length; i ++ )
                 {
 
                         double vx = rack.set[i].motion.vx();
                         double vy = rack.set[i].motion.vy();
 
                         double x = rack.set[i].location.x();
                         double y = rack.set[i].location.y();
 
                         //add a little fluxion of the vector to the location
                         //
 
 			//Newton currently values the fluxion at:
 			float fluxion=0.1f;
 
                         x += vx*fluxion;
                         y += vy*fluxion;
 
 			//does the location violate a border?
 			//
 			//				/* represent! */
 			//
 			//				   * in da house..
 			//
 			//				   remappper with
 			//				   all yall pool
 			//				   hall balls gone
 			//				   billiards while
 			//				   yous was eatin
 			//				   a banana split
 
 			if( ! checkx(rack.set[i] ) )
 			{
 				// reverse the flux!!
 				vx *= -1;			
 
 				// send 'er back
 				x += 2*vx*fluxion;
 
 
 			}
 
 			if( ! checky(rack.set[i] ) )
 			{
 				// reverse the flux!!
 				vy *= -1;			
 
 				// send 'er back
 				y += 2*vy*fluxion;
 
 			}
 
 
 			//note: nothing slowing them down
 			// at this point..
 			//
 
 			rack.set[i].motion.setvx(vx);
 			rack.set[i].motion.setvy(vy);
 
 			rack.set[i].location.setx(x);
 			rack.set[i].location.sety(y);
 
 			if( (abs(vx) < 0.05) && (abs(vy) < 0.05) )
 			{
 				rack.set[i].stop();
 			}
 
 
                 }
 
 	}
 
 	public boolean checkx(Ball xBall) {
 			
 			//define minimum bound
 			//
 			double leftBound = xBall.radius;
 			
 			//define maximum bound
 			//
 			double rightBound = this.width() - xBall.radius;
 
 
 			// there has to be a mathematical function for
 			//  this but it escapes me DRA
 
			//oops don't need the edges the bounds account for 
			// the radius already..
			//double ballLeftEdge = xBall.location.x() - xBall.radius;
			//double ballRightEdge = xBall.location.x() + xBall.radius;
 
 			if (
				xBall.location.x() > leftBound &&
				xBall.location.x() < rightBound
 			)
 				return true;
 
 			return false;
 	}
 
 	public boolean checky(Ball yBall) {
 			//define minimum bound
 			//
 			double bottomBound = yBall.radius;
 			
 			//define maximum bound
 			//
 			double topBound = this.height() - yBall.radius;
 
 
			// oops don't need edges the bounds already account
			//  for the radii..
			//double ballBottomEdge = yBall.location.y() - yBall.radius;
			//double ballTopEdge = yBall.location.y() + yBall.radius;
 
 			if (
				yBall.location.y() > bottomBound &&
				yBall.location.y() < topBound
 			)
 				return true;
 
 			return false;
 	}
 
 
 
 
 
 
 
 }
 
 
 
 
 
