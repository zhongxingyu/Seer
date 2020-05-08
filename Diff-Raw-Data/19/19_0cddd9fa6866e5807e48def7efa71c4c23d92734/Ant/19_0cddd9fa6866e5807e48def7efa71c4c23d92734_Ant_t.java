 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 
 public class Ant {
 
 	DirectionVector direction = new DirectionVector();
 	
 	public void run(Field field, long iterations) {
 		Point position = new Point(0,0);
 		
 		for (long i=0;i<iterations;i++) {
 			if (field.isCellBlack(position)) {
 				direction.turnLeft();
 			}
 			else {
 				direction.turnRight();
 			}
 			field.invertCell(position);
 			position.setX(position.getX()+direction.getCurrentDirection().getX());
 			position.setY(position.getY()+direction.getCurrentDirection().getY());
			//System.out.println(i);
 		}
 	}
 		
 	public static void main(String[] args) {
 		Field field = new Field();
 		Ant ant = new Ant();
 		//ant.run(field, new BigDecimal(BigInteger.ONE, -18).longValueExact());
//		for (int i=12000;i<=13000;i++) {
//			field.clear();
//			ant.run(field,i);
//			System.out.println(i+","+field.countBlackCells());
//		}
		ant.run(field, 100000);
 		System.out.println(field.countBlackCells());
 	}
 
 }
