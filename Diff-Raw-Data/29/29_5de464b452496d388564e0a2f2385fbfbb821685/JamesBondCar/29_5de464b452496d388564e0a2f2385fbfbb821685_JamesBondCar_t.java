 public class JamesBondCar extends Cars { 
 /* Код extends написанный ранее говорит о том что этот класс расширяет класс Cars. */
    int speed = 180;
    public int drive (int howlong) {
        distance = howlong * speed;
        System.out.println("The car drove  " + distance + " km for " + howlong + " minutes.");
        return distance;
    }
 }
