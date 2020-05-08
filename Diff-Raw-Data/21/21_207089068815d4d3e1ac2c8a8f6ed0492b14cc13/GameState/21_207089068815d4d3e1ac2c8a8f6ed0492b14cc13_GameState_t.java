 package artificial_player;
 
 import java.util.*;
 
 
 public class GameState {
     public static final Set<Bone2> allBones;
     private static final Comparator<Map.Entry<Choice, GameState>> comparator;
     private static final Comparator<Map.Entry<GameState, Choice>> inverseComparator;
 
     public void setStatus(Status status) {
         this.status = status;
     }
 
     public static enum Action {
         PLACED_RIGHT, PLACED_LEFT, PICKED_UP, PASS
     }
 
     public static enum Status {
         NOT_YET_CALCULATED, HAS_CHILD_STATES, IS_LEAF
     }
 
     static {
         // Enumerate all bones
         Set<Bone2> tempAllBones = new HashSet<Bone2>();
 
         for (int i = 0; i < 7; ++i) {
             for (int j = 0; j < 7; ++j) {
                 tempAllBones.add(new Bone2(i, j));
             }
         }
         assert tempAllBones.size() == 28;
         allBones = Collections.unmodifiableSet(tempAllBones);
 
         // Setup the comparators
         comparator = new Comparator<Map.Entry<Choice, GameState>>() {
             @Override
             public int compare(Map.Entry<Choice, GameState> o1, Map.Entry<Choice, GameState> o2) {
                 return compareStates(o1.getValue(), o2.getValue());
             }
         };
 
         inverseComparator = new Comparator<Map.Entry<GameState, Choice>>() {
 
             @Override
             public int compare(Map.Entry<GameState, Choice> o1, Map.Entry<GameState, Choice> o2) {
                 return compareStates(o1.getKey(), o2.getKey());
             }
         };
     }
 
     private static int compareStates(GameState s1, GameState s2) {
         return Double.compare(s1.getValue(), s2.getValue());
     }
 
     public static Set<Bone2> getAllBones() {
         Set<Bone2> tempAllBones = new HashSet<Bone2>();
 
         for (Bone2 bone : allBones) {
             tempAllBones.add(new Bone2(bone));
         }
 
         return tempAllBones;
     }
 
     private final AIContainer aiContainer;
     private final int sizeOfOpponentHand;
     private final int sizeOfBoneyard;
     private final double value;
     private final Set<Bone2> myBones;
     private final LinkedList<Bone2> layout;
     private final boolean isMyTurn;
     private final int moveNumber;
     private final Memo memo;
     private final GameState previous;
     private final Choice choiceTaken;
     private final Map<Choice,GameState> validChoices = new HashMap<Choice, GameState>();
 
     private Status status = Status.NOT_YET_CALCULATED;
     private int ply;
 
     public GameState(AIContainer aiContainer, Set<Bone2> myBones, boolean isMyTurn) {
         this.isMyTurn = isMyTurn;
         this.myBones = myBones;
         this.aiContainer = aiContainer;
         layout = new LinkedList<Bone2>();
         sizeOfOpponentHand = myBones.size();
         sizeOfBoneyard = allBones.size() - 2 * sizeOfOpponentHand;
         previous = null;
         moveNumber = 0;
         memo = new Memo();
         choiceTaken = null;
 
         value = aiContainer.getHandEvaluator().evaluateInitialValue(this);
         ply = aiContainer.getPlyManager().getInitialPly();
     }
 
     public Set<Bone2> getMyBones() {
         return Collections.unmodifiableSet(myBones);
     }
 
     private GameState createNextState(Choice choice) {
         return new GameState(this, choice);
     }
 
     public Map<Choice,GameState> getValidChoices() {
         lazyChoicesInitialisation();
         return Collections.unmodifiableMap(validChoices);
     }
 
     public int getPly() {
         return ply;
     }
 
     public void setPly(int ply) {
         this.ply = ply;
     }
 
     private GameState(GameState previous, Choice choiceTaken) {
         this.myBones = new HashSet<Bone2>(previous.myBones);
         this.layout = new LinkedList<Bone2>(previous.layout);
         this.choiceTaken = choiceTaken;
         this.isMyTurn = !previous.isMyTurn;
         this.moveNumber = previous.moveNumber + 1;
         this.memo = previous.memo;
         this.previous = previous;
         this.ply = previous.ply;
         this.aiContainer = previous.aiContainer;
 
         this.value = aiContainer.getHandEvaluator().addedValueFromChoice(choiceTaken, previous);
         ply = previous.getPly();
 
         // Set sizeOfOpponentHand, sizeOfBoneyard, myBones, and layout
 
         if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT) {
             sizeOfBoneyard = previous.getSizeOfBoneyard();
             if (choiceTaken.getAction() == Action.PLACED_RIGHT)
                 layout.addLast(choiceTaken.getBone());
             else
                 layout.addFirst(choiceTaken.getBone());
         } else if (choiceTaken.getAction() == Action.PICKED_UP) {
             sizeOfBoneyard = previous.getSizeOfBoneyard() - 1;
        } else if (choiceTaken.getAction() == Action.PASS)
            sizeOfBoneyard = previous.sizeOfBoneyard;
        else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
 
         if (previous.isMyTurn()) {
             sizeOfOpponentHand = previous.getSizeOfOpponentHand();
             if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                 myBones.remove(choiceTaken.getBone());
             else if (choiceTaken.getAction() == Action.PICKED_UP)
                 myBones.add(choiceTaken.getBone());
            else if (choiceTaken.getAction() != Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
         } else {
             if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                 sizeOfOpponentHand = previous.getSizeOfOpponentHand() - 1;
             else if (choiceTaken.getAction() == Action.PICKED_UP)
                 sizeOfOpponentHand = previous.getSizeOfOpponentHand() + 1;
            else if (choiceTaken.getAction() == Action.PASS)
                sizeOfOpponentHand = previous.getSizeOfOpponentHand();
            else
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
         }
     }
 
     public Choice getBestChoice() {
         // TODO: allow good validChoices to get higher ply.  Number of good validChoices to allow forward should depend on probThatOpponentHasBone()
 
         return getBestNChoices(3).get(0);
     }
 
     public List<Choice> getBestNChoices(int N) {
         List<Choice> list_of_best_choices = new LinkedList<Choice>();
 
         for (Map.Entry<Choice,GameState> e : getNBestChoicesAndFinalStates(N)) {
             list_of_best_choices.add(e.getKey());
         }
 
         return list_of_best_choices;
     }
 
     public List<GameState> getBestNFinalStates(int N) {
         List<GameState> list_of_best_final_states = new LinkedList<GameState>();
 
         for (Map.Entry<Choice, GameState> e : getNBestChoicesAndFinalStates(N)) {
             list_of_best_final_states.add(e.getValue());
         }
 
         return list_of_best_final_states;
     }
 
     public void printBestAfterSelectivelyIncreasingPly(int N) {
         List<GameState> bestFinalStates;
         int[] plyIncreases;
         int i;
 
         for (int n = 0; n < N; ++n) {
             bestFinalStates = getBestNFinalStates(5);
 
             double[] bestFinalStateValues = new double[5];
             int j = 0;
             for (GameState finalState : bestFinalStates)
                 bestFinalStateValues[j++] = finalState.getValue();
 
             plyIncreases = aiContainer.getPlyManager().getPlyIncreases(bestFinalStateValues);
 
             i = 0;
             for (GameState state : bestFinalStates) {
                 state.setPly(state.getPly() + plyIncreases[i++]);
             }
         }
 
         printBestN(5);
     }
 
     public Status getStatus() {
         return status;
     }
 
     public boolean isMyTurn() {
         return isMyTurn;
     }
 
     public List<Map.Entry<Choice,GameState>> getNBestChoicesAndFinalStates(int N) {
         // TODO: split this method into two: getAllBestChoicesAndFinalStates() and getBestChoiceAndFinalState()
 
         Map<GameState,Choice> choices_by_final_state = new HashMap<GameState, Choice>();
 
         for (Map.Entry<Choice,GameState> e : getValidChoices().entrySet()) {
             // For each choice
             Choice choice = e.getKey();
             GameState next_state = e.getValue();
 
            next_state.lazyChoicesInitialisation();
 
             if (next_state.getStatus() == Status.HAS_CHILD_STATES) {
                 // For each of the best final states given this choice
 
                 // Changed N to 1 here, because finding more than just the best route given this choice
                 // means taking less-optimal routes (eg. the opponent doesn't chose the most devistating routes)
                 // which is not as likely.
                 // The point of this method is to get the most desirable next_states to pursue further.
                 for (Map.Entry<Choice, GameState> e_child : next_state.getNBestChoicesAndFinalStates(1)) {
                     GameState final_state = e_child.getValue();
                     // Store what final state is achievable given this choice
                     assert ! choices_by_final_state.containsKey(final_state);
                     choices_by_final_state.put(final_state, choice);
                 }
             } else {
                 // If the next_state is a final state (ie. a leaf or too unimportant to calculate)
                 choices_by_final_state.put(next_state, choice);
             }
         }
 
         // Find at most N best final states, but no more than were found.
         N = Math.min(N, choices_by_final_state.size());
         Map<Choice,GameState> best_choices = new HashMap<Choice, GameState>();
 
         for (int i = 0; i < N; ++i) {
             // Find the entry with the best final state (which is maximum value if my turn, and minimum value if not)
             Map.Entry<GameState, Choice> best_choice;
             if (isMyTurn())
                 best_choice = Collections.max(choices_by_final_state.entrySet(), inverseComparator);
             else
                 best_choice = Collections.min(choices_by_final_state.entrySet(), inverseComparator);
 
             // Add the entry to best_choices (but inverted)
             assert ! best_choices.containsKey(best_choice.getValue());
             best_choices.put(best_choice.getValue(), best_choice.getKey());
 
             // Remove the entry from the original 'choices_by_final_state' so the next iteration will find the (i+1)th
             // best final state.
             choices_by_final_state.remove(best_choice.getKey());
         }
 
         // Create a list of these best options
         List<Map.Entry<Choice, GameState>> list_of_best_choices = new LinkedList<Map.Entry<Choice, GameState>>(best_choices.entrySet());
 
         // Best option should be at front of list.
         Collections.sort(list_of_best_choices, comparator);
         if (isMyTurn())
             Collections.reverse(list_of_best_choices);
 
         return list_of_best_choices;
 
     }
 
     private Status getDesiredStatus() {
         if (status == Status.IS_LEAF)
             return Status.IS_LEAF;
         if (memo.getMovesPlayed() + ply > moveNumber)
             return Status.HAS_CHILD_STATES;
         if (memo.getMovesPlayed() + ply <= moveNumber)
             return Status.NOT_YET_CALCULATED;
 
         throw new RuntimeException("getDesiredStatus broke");
     }
 
     public void lazyChoicesInitialisation() {
         Status desired_status = getDesiredStatus();
         if (desired_status == status)
             return;
 
         if (desired_status == Status.HAS_CHILD_STATES) {
             Set<Choice> validChoicesList = new HashSet<Choice>();
 
             if (isMyTurn) {
                 if (!myBones.isEmpty())
                     validChoicesList = aiContainer.getStateEnumerator()
                             .getMyValidChoices(layout, myBones, getPossibleOpponentBones());
             } else {
                 if (sizeOfOpponentHand != 0)
                     validChoicesList = aiContainer.getStateEnumerator()
                             .getOpponentValidChoices(layout, getPossibleOpponentBones());
             }
 
             for (Choice choice : validChoicesList)
                 validChoices.put(choice, createNextState(choice));
 
             if (validChoices.isEmpty())
                 setStatus(Status.IS_LEAF);
             else if (choiceTaken != null
                     && choiceTaken.getAction() == Action.PASS
                     && validChoices.size() == 1
                     && validChoices.containsKey(new Choice(Action.PASS, null)))
                 setStatus(Status.IS_LEAF);
             else
                 setStatus(Status.HAS_CHILD_STATES);
         }
     }
 
     public GameState choose(Choice choice) {
         GameState next_state = getValidChoices().get(choice);
         if (next_state == null)
             throw new RuntimeException("Choice was not valid: " + choice);
         memo.incrementMovesPlayed();
         return next_state;
     }
 
     public double getValue() {
         return value;
     }
 
     public int getSizeOfOpponentHand() {
         return sizeOfOpponentHand;
     }
 
     public int getSizeOfBoneyard() {
         return sizeOfBoneyard;
     }
 
     public double probThatOpponentHasBone() {
         // TODO: if opponent picks up with 1s on left and right, prob of having a 1 bone = 0
         int total_possible_opponent_bones = sizeOfBoneyard + sizeOfOpponentHand;
 
         if (total_possible_opponent_bones == 0)
             return 0;
         else
             return ( (double) sizeOfOpponentHand) / total_possible_opponent_bones;
     }
 
     public double probThatBoneYardHasBone() {
         return 1 - probThatOpponentHasBone();
     }
 
     public Set<Bone2> getPossibleOpponentBones() {
         Set<Bone2> possible_opponent_bones = new HashSet<Bone2>(getAllBones());
         possible_opponent_bones.removeAll(myBones);
         possible_opponent_bones.removeAll(layout);
         return possible_opponent_bones;
     }
 
     @Override
     public String toString() {
         return "GameState{" +
                 "sizeOfOpponentHand=" + sizeOfOpponentHand +
                 ", sizeOfBoneyard=" + sizeOfBoneyard +
                 ", isMyTurn=" + isMyTurn +
                 ", moveNumber=" + moveNumber +
                 '}';
     }
 
     public void printMovesUpToFinalState(GameState finalState) {
 
         StringBuilder sbuilder = new StringBuilder(String.format("%n--- Choices (value = %.1f -> %.1f) ----%n", value, finalState.value));
 
         for (GameState next_state : getFutureStates(finalState)) {
 
             sbuilder.append(String.format("Move %d: %s %s , now value = %.1f%n\t%s%n",
                     next_state.moveNumber,
                     (next_state.isMyTurn ? "opponent" : "I"),
                     next_state.getChoiceTaken(), next_state.getValue(),
                     next_state.layout.toString()));
         }
 
         System.out.println(sbuilder.toString());
     }
 
     public void printBestN(int N) {
         for (GameState final_state : getBestNFinalStates(N)) {
             printMovesUpToFinalState(final_state);
         }
     }
 
     public Choice getChoiceTaken() {
         return choiceTaken;
     }
 
     private List<GameState> getFutureStates(GameState finalState) {
         LinkedList<GameState> list = new LinkedList<GameState>();
 
         do {
             list.addFirst(finalState);
             finalState = finalState.previous;
         } while(finalState.moveNumber != memo.getMovesPlayed());
 
         return list;
     }
 
 }
