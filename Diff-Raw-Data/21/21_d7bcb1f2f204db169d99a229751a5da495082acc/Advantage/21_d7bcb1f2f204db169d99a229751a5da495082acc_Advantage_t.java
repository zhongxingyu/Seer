 package no.finntech.kundefront.kata;
 
 import com.google.common.base.Objects;
 
 public class Advantage implements Score {
 
 
    private final Player playerWithAdvantage;
 
    public Advantage(Player playerWithAdvantage) {
 
        this.playerWithAdvantage = playerWithAdvantage;
     }
 
     @Override
     public Score registerBallWinner(Player player) {
        if (player.equals(playerWithAdvantage)) {
            return new Winner(playerWithAdvantage);
        }
         return new Deuce();
 
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                .add("player", playerWithAdvantage)
                 .toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Advantage that = (Advantage) o;
 
        return playerWithAdvantage == that.playerWithAdvantage;
 
     }
 
     @Override
     public int hashCode() {
        return playerWithAdvantage != null ? playerWithAdvantage.hashCode() : 0;
     }
 }
