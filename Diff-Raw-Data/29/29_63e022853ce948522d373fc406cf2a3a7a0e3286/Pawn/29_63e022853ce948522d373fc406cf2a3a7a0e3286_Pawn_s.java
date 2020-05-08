 
 public class Pawn extends Piece {
   protected boolean can_enpassant = false;
   
   public Pawn(String color, int rank, int file) {
     this.type = "p";
     this.color = color;
     this.file = file;
     this.rank = rank;
     this.times_moved = 0;
   }
   
   public void giveEnPassantAbility() {
     this.can_enpassant = true;
   }
   
   public void takeAwayEnPassantAbility() {
     this.can_enpassant = false;
   }
   
   @Override
   public boolean canAttack(int rank, int file) {
     // needs to be either to file left or file right
     if (Math.abs(file - this.file) != 1) return false;
     if (this.color.equals("white")) {
       return rank > this.rank; // can only advance with increasing rank
     } else {
       return rank < this.rank; // can only advance with decreasing rank
     }
   }
   
   @Override
   public boolean canMoveTo(int rank, int file) {
     if (rank == this.rank && file == this.file) return false;
     
     // general movement first, pawns can't move more than 2 spaces ever
     if (Math.abs(rank - this.rank) > 2) return false;
     
     // can we move 2? Only if we are in the starting line
     if (Math.abs(rank - this.rank) == 2) {
       if (this.color == "white" && this.rank == 1 && rank > this.rank) return true;
       if (this.color == "black" && this.rank == 6 && rank < this.rank) return true;
       
       return false; // otherwise
     }
     
     // can't possibly move more than one to the side, ever
     if (Math.abs(file - this.file) > 0) return false;
     
     if (this.color.equals("white")) {
       // can only advance with increasing rank
       return rank > this.rank;
     } else {
       // can only advance with decreasing rank
       return rank < this.rank;
     }
   }
 }
