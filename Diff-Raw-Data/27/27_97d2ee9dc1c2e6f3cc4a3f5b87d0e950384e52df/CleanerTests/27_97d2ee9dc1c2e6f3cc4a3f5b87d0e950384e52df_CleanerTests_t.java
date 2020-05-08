 package com.github.colorlines;
 
 import com.github.colorlines.domain.Area;
 import com.github.colorlines.domain.Ball;
 import com.github.colorlines.domain.Color;
 import com.github.colorlines.domain.Position;
 import com.github.colorlines.domainimpl.PatternBasedCleaner;
 import org.testng.annotations.Test;
 
 import java.util.Set;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static org.testng.Assert.assertEquals;
 
 /**
  * User: Alex Lenkevich
  * Date: 27.11.11
  * Time: 16:57
  */
 public class CleanerTests {
 
 
     @Test
     public void testTest() {
         Area area = new MatrizArea(new int[][]{
                 {0, 0, 0, 0, 0, 0, 0, 0, 0},
                 {0, 1, 1, 1, 1, 1, 0, 0, 0},
                 {0, 0, 0, 0, 2, 2, 2, 2, 2},
                 {0, 4, 0, 0, 0, 2, 0, 2, 2},
                 {0, 4, 0, 3, 0, 0, 0, 0, 0},
                 {4, 4, 4, 4, 4, 0, 0, 2, 0},
                 {0, 4, 3, 3, 5, 3, 3, 2, 0},
                 {0, 4, 0, 0, 0, 0, 0, 2, 0},
                 {0, 0, 0, 0, 0, 0, 0, 2, 0}}
         );
         Set<Ball> etalonBalls = newHashSet();
         etalonBalls.add(area.take(Position.create(1, 1)));
         etalonBalls.add(area.take(Position.create(2, 1)));
         etalonBalls.add(area.take(Position.create(3, 1)));
         etalonBalls.add(area.take(Position.create(4, 1)));
         etalonBalls.add(area.take(Position.create(5, 1)));
 
         etalonBalls.add(area.take(Position.create(4, 2)));
         etalonBalls.add(area.take(Position.create(5, 2)));
         etalonBalls.add(area.take(Position.create(6, 2)));
         etalonBalls.add(area.take(Position.create(7, 2)));
         etalonBalls.add(area.take(Position.create(8, 2)));
 
         etalonBalls.add(area.take(Position.create(1, 3)));
         etalonBalls.add(area.take(Position.create(1, 4)));
         etalonBalls.add(area.take(Position.create(1, 5)));
         etalonBalls.add(area.take(Position.create(1, 6)));
         etalonBalls.add(area.take(Position.create(1, 7)));
         etalonBalls.add(area.take(Position.create(0, 5)));
         etalonBalls.add(area.take(Position.create(2, 5)));
         etalonBalls.add(area.take(Position.create(3, 5)));
         etalonBalls.add(area.take(Position.create(4, 5)));
 
         Set<Ball> cleanBalls = new PatternBasedCleaner().clean(area);
         assertEquals(cleanBalls, etalonBalls);
     }
 
     @Test
     public void testTestHV() {
         Area area = new MatrizArea(new int[][]{
                 {0, 0, 0, 0, 0, 0, 0, 0, 0},
                 {0, 0, 1, 0, 0, 0, 0, 0, 0},
                 {0, 0, 0, 1, 0, 0, 0, 4, 0},
                 {0, 0, 0, 0, 1, 0, 1, 0, 0},
                 {0, 0, 0, 0, 0, 1, 0, 0, 0},
                 {0, 0, 0, 0, 1, 0, 1, 0, 0},
                 {0, 0, 0, 1, 0, 0, 0, 2, 0},
                 {0, 0, 1, 0, 0, 0, 0, 0, 0},
                 {0, 0, 0, 0, 0, 0, 0, 0, 0}}
 
         );
         Set<Ball> etalonBalls = newHashSet();
 
         etalonBalls.add(area.take(Position.create(2, 1)));
         etalonBalls.add(area.take(Position.create(3, 2)));
         etalonBalls.add(area.take(Position.create(4, 3)));
         etalonBalls.add(area.take(Position.create(5, 4)));
         etalonBalls.add(area.take(Position.create(6, 5)));
 
         etalonBalls.add(area.take(Position.create(2, 7)));
         etalonBalls.add(area.take(Position.create(3, 6)));
         etalonBalls.add(area.take(Position.create(4, 5)));
         etalonBalls.add(area.take(Position.create(6, 3)));
 
 
         Set<Ball> cleanBalls = new PatternBasedCleaner().clean(area);
         assertEquals(cleanBalls, etalonBalls);
     }
 
    @Test
    public void testTest3() {
        Area area = new MatrizArea(new int[][]{
                {0, 0, 1, 1, 4, 3, 0, 0, 0},
                {0, 0, 0, 0, 0, 3, 0, 4, 0},
                {0, 0, 5, 0, 0, 0, 4, 1, 0},
                {0, 0, 0, 0, 0, 0, 3, 1, 0},
                {0, 0, 0, 5, 0, 0, 3, 3, 0},
                {0, 0, 0, 0, 0, 0, 0, 3, 0},
                {4, 1, 1, 0, 0, 2, 0, 3, 0},
                {0, 1, 0, 1, 0, 1, 0, 3, 0},
                {0, 0, 0, 0, 0, 0, 0, 3, 0}}

        );
        Set<Ball> etalonBalls = newHashSet();

        etalonBalls.add(area.take(Position.create(7, 4)));
        etalonBalls.add(area.take(Position.create(7, 5)));
        etalonBalls.add(area.take(Position.create(7, 6)));
        etalonBalls.add(area.take(Position.create(7, 7)));
        etalonBalls.add(area.take(Position.create(7, 8)));


        Set<Ball> cleanBalls = new PatternBasedCleaner().clean(area);
        assertEquals(cleanBalls, etalonBalls);
    }

 
 }
 
 class MatrizArea implements Area {
 
     private final int[][] balls;
 
     MatrizArea(int[][] balls) {
         this.balls = balls;
     }
 
     @Override
     public boolean contains(final Position location) {
         return balls[location.getY()][location.getX()] != 0;
     }
 
     @Override
     public Ball take(final Position location) {
         return new BallImpl(location);
     }
 
     class BallImpl implements Ball {
 
         final Position location;
 
         @Override
         public Color color() {
             return Color.values()[balls[location.getY()][location.getX()] - 1];
         }
 
         BallImpl(Position location) {
             this.location = location;
         }
 
         @Override
         public Position position() {
             return location;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             BallImpl ball = (BallImpl) o;
 
             if (location != null ? !location.equals(ball.location) : ball.location != null) return false;
             return true;
         }
 
         @Override
         public int hashCode() {
             return location != null ? location.hashCode() : 0;
         }
     }
 
 }
 
 
