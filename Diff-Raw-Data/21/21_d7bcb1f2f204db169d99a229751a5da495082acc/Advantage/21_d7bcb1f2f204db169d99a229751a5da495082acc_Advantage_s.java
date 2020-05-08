 package no.finntech.kundefront.kata;
 
 import com.google.common.base.Objects;
 
 public class Advantage implements Score {
 
 
    private final Player player;
 
    public Advantage(Player player) {
 
        this.player = player;
     }
 
     @Override
     public Score registerBallWinner(Player player) {
         return new Deuce();
 
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                .add("player", player)
                 .toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Advantage that = (Advantage) o;
 
        return player == that.player;
 
     }
 
     @Override
     public int hashCode() {
        return player != null ? player.hashCode() : 0;
     }
 }
