 
 public abstract class GameAction {
     abstract void performAction(Pokemon p);
     
     public static final GameAction eatRareCandy = new GameAction() {
         void performAction(Pokemon p) { p.eatRareCandy(); }
     };
     public static final GameAction eatHPUp = new GameAction() {
         void performAction(Pokemon p) { p.eatHPUp(); }
     };
     public static final GameAction eatIron = new GameAction() {
         void performAction(Pokemon p) { p.eatIron(); }
     };
     public static final GameAction eatProtein = new GameAction() {
         void performAction(Pokemon p) { p.eatProtein(); }
     };
     public static final GameAction eatCalcium = new GameAction() {
         void performAction(Pokemon p) { p.eatCalcium(); }
     };
     public static final GameAction eatCarbos = new GameAction() {
         void performAction(Pokemon p) { p.eatCarbos(); }
     };
     
     //badges
     public static final GameAction getBoulderBadge = new GameAction() {
         void performAction(Pokemon p) { p.setAtkBadge(true); }
     };
     public static final GameAction getSoulBadge = new GameAction() {
         void performAction(Pokemon p) { p.setSpdBadge(true); } //gen 1 is buggy as fuck
     };
     public static final GameAction getVolcanoBadge = new GameAction() {
         void performAction(Pokemon p) { p.setSpcBadge(true); }
     };
     public static final GameAction getThunderBadge = new GameAction() {
         void performAction(Pokemon p) { p.setDefBadge(true); }
     };
     
     
     //not really a game action, but it's a nice hack?
     public static final GameAction printAllStats = new GameAction() {
         void performAction(Pokemon p) { Main.appendln(p.statsWithBoost()); }
     };
     public static final GameAction printAllStatsNoBoost = new GameAction() {
         void performAction(Pokemon p) { Main.appendln(p.statsWithoutBoost()); }
     };
     public static final GameAction printStatRanges = new GameAction() {
         void performAction(Pokemon p) { Main.appendln(p.statRanges(true)); }
     };
     public static final GameAction printStatRangesNoBoost = new GameAction() {
         void performAction(Pokemon p) { Main.appendln(p.statRanges(false)); }
     };
 
 }
 
 class LearnMove extends GameAction {
     private Move move;
     LearnMove(Move m) { move = m; }
     LearnMove(String s) { move = Move.getMoveByName(s); }
     public Move getMove() { return move; }
     @Override
     void performAction(Pokemon p) { p.getMoveset().addMove(move); }
 }
 
 
 class UnlearnMove extends GameAction {
     private Move move;
     UnlearnMove(Move m) { move = m; }
     UnlearnMove(String s) { move = Move.getMoveByName(s); }
     public Move getMove() { return move; }
     @Override
     void performAction(Pokemon p) { p.getMoveset().delMove(move); }
 }
 
 class Evolve extends GameAction {
     private Species target;
     Evolve(Species s) { target = s; }
     Evolve(String s) { target = PokemonNames.getSpeciesFromName(s); }
     @Override
    void performAction(Pokemon p) {
        p.evolve(target);
        p.calculateStats();}
 }
