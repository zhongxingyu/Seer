 package elemenTerra.entity;
 
 import elemenTerra.world.Board;
 
 public class SeekerBrain extends Brain {
   protected Entity target;
 
   public SeekerBrain(Ai body, Board board) {
     super(body, board);
     target = body;
   }
 
   public SeekerBrain(Ai body, Entity target, Board board) {
     super(body, board);
     this.target = target;
   }
 
   public char goToXY(int targetx, int targety, Board b) {
     int ix = body.getX();
     int iy = body.getY();
     //System.out.println("AI");
     //System.out.println(ix);
     //System.out.println(iy);
 
     int tx = targetx;
     int ty = targety;
     //System.out.println(tx);
     //System.out.println("Target");
     //System.out.println(ty);
 
     int distancex = tx - ix;
     int distancey = ty - iy;
     //System.out.println("Distance");
     //System.out.println(distancex);
     //System.out.println(distancey);
 
     //set up the dx/dy
     int attemptx = 0;
     int attempty = 0;
     char aXchar = 'Z';
     char aYchar = 'Z';
 
     if (distancex > 0) {
       attemptx = 1;
       aXchar = 'd';
       //System.out.println("Right attempt");
     } else if (distancex < 0) {
       attemptx = -1;
       aXchar = 'a';
       //System.out.println("Left attempt");
    } else {
	System.out.println("x's equivalent");
	boolean lr = random.nextBoolean();
	if (lr){
	    attemptx = 1;
	    aXchar = 'd';
	}else{
	    attemptx = -1;
	    aXchar = 'a';
	}
     }
     if (distancey > 0) {
       attempty = 1;
       aYchar = 's';
 
       //System.out.println("Down attempt");
     } else if (distancey < 0) {
       attempty = -1;
       aYchar = 'w';
       //System.out.println("Up attempt");
    } else {
	System.out.println("y's equivalent");
	boolean ud = random.nextBoolean();
	if (ud){
	    attempty = 1;
	    aYchar = 's';
	}else{
	    attempty = -1;
	    aYchar = 'w';
	}
     }
 
     int dx = ix + attemptx;
     int dy = iy + attempty;
 
     char attempt = 'Z';
     //farther in x than y distance (squared for absolute value), x dominates
     if (distancex*distancex > distancey*distancey) {
       //System.out.println("distance larger: y");
       if (board.checkTile(dx, iy)) {
         //System.out.println("check x valid");
         attempt = aXchar;
       } else {
 	  if (!board.getTile(dx, iy).getEntity().toString().equals(body.getIdentity())) {
           attempt = aYchar;
           //System.out.println(board.getTile(dx, iy).getEntity().toString());
         }
       }
     } else if (distancex*distancex < distancey*distancey) {
       //System.out.println("distance larger: y");
       if (board.checkTile(ix, dy)) {
         //System.out.println("check y valid");
         attempt = aYchar;
       } else {
 	  if (!board.getTile(ix, dy).getEntity().toString().equals(body.getIdentity())) {
           attempt = aXchar;
           //System.out.println(board.getTile(ix, dy).getEntity().toString());
         }
       }
     } else {
	System.out.print("diagonal");
 	boolean xory = random.nextBoolean();
 	if (xory){
 	    attempt = aXchar;
 	}else {
 	    attempt = aXchar;//aYchar
 	}
     }
     return attempt;
   }
 
   public char goToTarget() {
     int targetx = target.getX();
     int targety = target.getY();
     char attempt = goToXY(targetx, targety, board);
     //System.out.println("Ai attempted move");
     return attempt;
   }
 
   @Override
   public void tick() {
     //System.out.println("SeekerBrain ticked");
     //System.out.println(board.testString);
     wait++;
     if (wait == delay) {
       wait = 0;
       char output = goToTarget();
       //System.out.println(output);
       game.handleMove(output, body);
     }
   }
 }
