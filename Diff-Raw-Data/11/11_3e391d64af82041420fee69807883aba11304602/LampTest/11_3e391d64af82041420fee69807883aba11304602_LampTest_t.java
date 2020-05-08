 package ru.spbau.korovin.se.drunkman.dynamic;
 
 import org.junit.Before;
 import org.junit.Test;
 import ru.spbau.korovin.se.drunkman.Point;
 import ru.spbau.korovin.se.drunkman.PoliceDispatcher;
 import ru.spbau.korovin.se.drunkman.characters.dynamical.DrunkMan;
 import ru.spbau.korovin.se.drunkman.characters.dynamical.Lamp;
 import ru.spbau.korovin.se.drunkman.characters.dynamical.PoliceMan;
 import ru.spbau.korovin.se.drunkman.characters.statical.Bottle;
 import ru.spbau.korovin.se.drunkman.characters.statical.LyingDrunkMan;
 import ru.spbau.korovin.se.drunkman.characters.statical.Pillar;
 import ru.spbau.korovin.se.drunkman.characters.statical.SleepingDrunkMan;
 import ru.spbau.korovin.se.drunkman.field.Field;
 import ru.spbau.korovin.se.drunkman.random.MathRandom;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.spy;
 
 public class LampTest {
     private Field field;
     private Lamp lamp;
     private final int radius = 3;
     private Point lampPosition;
 
 
     @Before
     public void createEnvironment() {
         this.field = spy(new Field(15, 15));
         lampPosition = new Point(9, 3);
         this.lamp = new Lamp(field, lampPosition, radius);
         field.placeObject(lamp);
     }
 
     @Test
     public void soberDay() {
         lamp.act();
         assertEquals(0, PoliceDispatcher.getInstance().queueSize());
     }
 
     @Test
     public void drunkersEverywhere() {
         for (int i = 0; i < 15; i++) {
             for (int j = 0; j < 15; j++) {
                 field.placeObject(new LyingDrunkMan(field, new Point(i, j)));
             }
         }
         lamp.act();
 
         PoliceDispatcher policeDispatcher = PoliceDispatcher.getInstance();
        // Checking that all drunkers around are detected
         assertEquals((radius * 2 + 1) * (radius * 2 + 1) - 1,
                 policeDispatcher.queueSize());

        // Cleaning the queue for other tests
         while (policeDispatcher.queueSize() > 0) {
             policeDispatcher.markVialator();
             policeDispatcher.popVialator();
         }
     }
 
     @Test
     public void partyingHardUnderLamp() {
         field.placeObject(new DrunkMan(field, new Point(6, 0), new MathRandom()));
         field.placeObject(new SleepingDrunkMan(field, new Point(7, 0)));
         field.placeObject(new LyingDrunkMan(field, new Point(8, 0)));
         field.placeObject(new PoliceMan(field, field, new Point(9, 0), new Point(9, 0)));
         field.placeObject(new Lamp(field, new Point(10, 0), 1));
         field.placeObject(new Bottle(field, new Point(11, 0)));
         field.placeObject(new Pillar(field, new Point(12, 0)));
 
         lamp.act();
         PoliceDispatcher policeDispatcher = PoliceDispatcher.getInstance();
         assertEquals(1, policeDispatcher.queueSize());
         policeDispatcher.markVialator();
         policeDispatcher.popVialator();
         assertEquals(0, policeDispatcher.queueSize());
     }
 }
