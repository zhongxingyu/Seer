 package dominoes.players.ai.algorithm.helper;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import static junit.framework.Assert.*;
 
 /**
  * User: Sam Wright
  * Date: 13/02/2013
  * Time: 17:40
  */
 public class BoneStateTest {
     private BoneState initialState, iPassed, iPickedUp, iPlaced, opponentPassed, opponentPickedUp, opponentPlaced, preGameState;
     private List<ImmutableBone> myBones;
     private List<ImmutableBone> unknownBones;
     private ImmutableBone opponentChosenBone, myChosenBone, placedBone;
 
     @Before
     public void setUp() throws Exception {
         List<ImmutableBone> all_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
         Collections.shuffle(all_bones);
 
         myBones = all_bones.subList(0, 7);
         unknownBones = new LinkedList<ImmutableBone>(all_bones.subList(7, 28));
 
         myChosenBone = myBones.get(0);
         opponentChosenBone = unknownBones.get(0);
 
 
         System.out.println("Setting up initial state");
         preGameState = new BoneStateImpl(myBones, 14);
 
         placedBone = myBones.remove(3);
         initialState = preGameState.createNext(new Choice(Choice.Action.PLACED_RIGHT, placedBone), true);
 
         System.out.println("Setting up iPassed");
         iPassed = initialState.createNext(new Choice(Choice.Action.PASS, null), true);
         System.out.println("Setting up opponentPassed");
         opponentPassed = initialState.createNext(new Choice(Choice.Action.PASS, null), false);
 
         System.out.println("Setting up iPickedUp");
         iPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, opponentChosenBone), true); //
         System.out.println("Setting up opponentPickedUp");
         opponentPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, opponentChosenBone), false);
 
         System.out.println("Setting up iPlaced");
         iPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_RIGHT, myChosenBone), true);
         System.out.println("Setting up opponentPlaced");
         opponentPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_LEFT, opponentChosenBone), false); //
 
         System.out.println(" === end of setup === ");
     }
 
     @Test
     public void testGetSizeOfBoneyard() throws Exception {
         assertEquals(14, preGameState.getSizeOfBoneyard());
         assertEquals(14, initialState.getSizeOfBoneyard());
 
         assertEquals(14, iPassed.getSizeOfBoneyard());
         assertEquals(14, opponentPassed.getSizeOfBoneyard());
 
         assertEquals(13, iPickedUp.getSizeOfBoneyard());
         assertEquals(13, opponentPickedUp.getSizeOfBoneyard());
 
         assertEquals(14, iPlaced.getSizeOfBoneyard());
         assertEquals(14, iPlaced.getSizeOfBoneyard());
     }
 
     @Test
     public void testImmutabilityOfParentState() throws Exception {
 
         iPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, unknownBones.get(1)), true);
         iPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, unknownBones.get(2)), true);
         iPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, unknownBones.get(1)), true);
         iPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, unknownBones.get(2)), true);
 
         iPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_RIGHT, myChosenBone), true);
         iPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_RIGHT, myChosenBone), true);
         iPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_RIGHT, myChosenBone), true);
     }
 
     @Test
     public void testGetSizeOfOpponentHand() throws Exception {
         assertEquals(7, initialState.getSizeOfOpponentHand());
 
         assertEquals(7, iPassed.getSizeOfOpponentHand());
         assertEquals(7, opponentPassed.getSizeOfOpponentHand());
 
         assertEquals(7, iPickedUp.getSizeOfOpponentHand());
         assertEquals(8, opponentPickedUp.getSizeOfOpponentHand());
 
         assertEquals(7, iPlaced.getSizeOfOpponentHand());
         assertEquals(6, opponentPlaced.getSizeOfOpponentHand());
     }
 
     @Test
     public void testCreateNext() throws Exception {
 
     }
 
     @Test
     public void testGetMyBones() throws Exception {
         assertEquals(myBones, initialState.getMyBones());
 
         assertEquals(myBones, iPassed.getMyBones());
         assertEquals(myBones, opponentPassed.getMyBones());
         assertEquals(myBones, opponentPickedUp.getMyBones());
         assertEquals(myBones, opponentPlaced.getMyBones());
 
         myBones.add(opponentChosenBone);
         assertEquals(myBones, iPickedUp.getMyBones());
 
         myBones.remove(opponentChosenBone);
         myBones.remove(myChosenBone);
         assertEquals(myBones, iPlaced.getMyBones());
 
     }
 
     @Test
     public void testGetUnknownBones() throws Exception {
         assertContentsEquals(unknownBones, initialState.getUnknownBones());
 
         assertContentsEquals(unknownBones, iPassed.getUnknownBones());
         assertContentsEquals(unknownBones, opponentPassed.getUnknownBones());
 
 //        System.out.println("My chosen bone is: " + myChosenBone);
 //        System.out.println("Opponent chosen bone is: " + opponentChosenBone);
 //        System.out.println("it is in unknown bones?: " + iPlaced.getUnknownBones().contains(myChosenBone));
 //        System.out.println("it opponentChosenBone in unknown bones?: " + iPlaced.getUnknownBones().contains(opponentChosenBone));
 //        iPickedUp = initialState.createNext(new Choice(Choice.Action.PICKED_UP, opponentChosenBone), true);
         System.out.println("Creating placed_left state...");
         assertContentsEquals(unknownBones, initialState.getUnknownBones());
         iPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_LEFT, myChosenBone), true);
         assertContentsEquals(unknownBones, iPlaced.getUnknownBones());
         assertContentsEquals(unknownBones, opponentPickedUp.getUnknownBones());
 
         unknownBones.remove(0);
         assertContentsEquals(unknownBones, opponentPlaced.getUnknownBones());
         assertContentsEquals(unknownBones, iPickedUp.getUnknownBones());
     }
 
     @Test
     public void testIPlacedUnknownBones() throws Exception {
         iPlaced = initialState.createNext(new Choice(Choice.Action.PLACED_LEFT, myChosenBone), true);
         assertContentsEquals(unknownBones, iPlaced.getUnknownBones());
     }
 
 
     private <T> void assertContentsEquals(List<T> list1, List<T> list2) {
         assertEquals(new HashSet<T>(list1), new HashSet<T>(list2));
     }
 
     @Test
     public void testInitialProbability() throws Exception {
         double initialProb = (double) 7 / 21;
         assertEquals(0.3333333, initialProb, 0.001);
         ImmutableBone boneToCheck = unknownBones.get(1);
 
         assertEquals(initialProb, initialState.getProbThatOpponentHasBone(boneToCheck), 0.001);
         assertEquals(initialProb, iPassed.getProbThatOpponentHasBone(boneToCheck), 0.001);
         assertEquals(initialProb, iPlaced.getProbThatOpponentHasBone(boneToCheck), 0.001);
     }
 
     @Test
     public void testProbabilityAfterPass() throws Exception {
         // After passing
          //BoneState opponentPassed = this.iPlaced.createNext(new Choice(Choice.Action.PASS, null), false);
         System.out.println(opponentPassed);
 
         // All unknown bones that matched what I place should have prob == 0:
         ImmutableBone boneToCheck = null;
        double probAfterPass = 7.0 / 21;
 
        for (ImmutableBone bone : opponentPassed.getUnknownBones())
            assertEquals(probAfterPass, opponentPassed.getProbThatOpponentHasBone(bone), 0.001);
 
     }
 
     @Test
     public void testProbabilityAfterIPickedUp() throws Exception {
         // After I pick up a bone, there will be one bone less in the boneyard:
         double probAfterPickup = (double) 7 / 20;
         ImmutableBone boneToCheck = unknownBones.get(1);
 
         assertEquals(probAfterPickup, iPickedUp.getProbThatOpponentHasBone(boneToCheck), 0.001);
 
     }
 
     /**
      * eg. unknownBones = A, B, C, D
      * opponent has 2 bones
      * prob(A) = prob(B) = prob(C) = prob(D) = 2 / 4
      *
      * Now I've placed something that only matches C, and the opponent must pick up.
      *
      * Before picking up:
      * prob(A) = prob(B) = prob(D) = 2 / 3
      * prob(C) = 0
      *
      * After picking up
      * prob(C) = 1 / (sizeOfBoneyard before pickup) = 1 / 2
      *
      * For sum(probs) to equal new sizeOfOpponentHand = 3:
      * prob(A) + prob(B) + prob(C) + prob(D) = 3        (where prob(C) = 1/2)
      *
      * So
      * prob(A) + prob(B) + prob(D) = 5/2
      *
      * Using symmetry (ie. dividing by the new sizeOfOpponentHand)
      * prob(A) = prob(B) = prob(C) = 5/6
      *
      * ========= now with my algorithm ==========
      * chances = {A:2, B:2, C:2, D:2}  (total = 8 , sizeOfOpponentHand = 2, sizeOfBoneyard = 2)
      *
      * Now I've placed something that only matches C, and the opponent must pick up.
      *
      * chances = {A:2, B:2, C:0, D:2}  (total = 6 , sizeOfOpponentHand = 2, sizeOfBoneyard = 2)
      *
      * After picking up
      * chances = {A:3, B:3, C:1, D:3}   (total = 10 , sizeOfOpponentHand = 3, sizeOfBoneyard = 1)
      *
      *
      *      Do it line-by-line (thus forcing order of selection):       (assume all bones start in the boneyard: so probabilities are {A:0, B:0, C:0, D:0}
      *      {A:3, B:3, C:1, D:3} => line 1 is {A:1, B:1, C:0, D:1}
      *                                              (sizeOfBonyard = 3)
      *                                                  (prob that it WAS in the BONEYARD * prob that it is picked up + prob that it WAS in the OPPONENT HAND)
      *                                                        {A:1, B:1, C:1, D:1}        *   {A:1, B:1, C:0, D:1}/sizeOfBoneyard + {A:0, B:0, C:0, D:0}
      *                                                      = {A:1, B:1, C:1, D:1}        *   {A:1/3, B:1/3, C:0, D:1/3}      + {A:0, B:0, C:0, D:0}
      *                                                      = {A:1/3, B:1/3, C:0, D:1/3}
      *                           => line 2 is {A:1, B:1, C:0, D:1}
      *                                              (sizeOfBonyard = 2)
      *                                                  (prob that it WAS in the BONEYARD * prob that it is picked up + prob that it WAS in the OPPONENT HAND)
      *                                                        {A:2/3, B:2/3, C:1, D:2/3}  *   {A:1, B:1, C:0, D:1} / sizeOfBoneyard + {A:1/3 B:1/3, C:0, D:1/3}
      *                                                      = {A:2/3, B:2/3, C:1, D:2/3}  *   {A:1/2, B:1/2, C:0, D:1/2} + {A:1/3 B:1/3, C:0, D:1/3}
      *                                                      = {A:1/3, B:1/3, C:0, D:1/3}  +  {A:1/3 B:1/3, C:0, D:1/3}
      *                                                      = {A:2/3, B:2/3, C:0, D:2/3}
      *
      *                           => line 2.5 is {A:1, B:1, C:0, D:1}
      *                                                  (sizeOfBonyard = 1)
      *                                                        {A:1/3, B:1/3, C:0, D:1/3}  *   {A:1, B:1, C:0, D:1}/sizeOfBoneyard + {A:2/3, B:2/3, C:0, D:2/3}
      *                                                      = {A:1/3, B:1/3, C:0, D:1/3}  *   {A:1, B:1, C:0, D:1} + {A:2/3, B:2/3, C:0, D:2/3}
      *                                                      = {A:1, B:1, C:0, D:1}          :-D
 
      *                           => line 3 is {A:1, B:1, C:1, D:1}
      *                                                  (sizeOfBonyard = 2)
      *                                                        {A:1/3, B:1/3, C:0, D:1/3}  *   {A:1, B:1, C:1, D:1}/sizeOfBoneyard + {A:2/3, B:2/3, C:0, D:2/3}
      *                                                      = {A:1/3, B:1/3, C:0, D:1/3}  *   {A:1/2, B:1/2, C:1/2, D:1/2} + {A:2/3, B:2/3, C:0, D:2/3}
      *                                                      = {A:5/6, B:5/6, C:1/2, D:5/6}          :-D
      *
      * @throws Exception
      */
     @Test
     public void testProbabilityAfterOpponentPickedUp() throws Exception {
         // After passing
         // BoneState opponentPickedUp = iPlaced.createNext(new Choice(Choice.Action.PICKED_UP, null), false);
         System.out.println(opponentPickedUp);
 
         List<ImmutableBone> unknownBonesMatchingLayout = new LinkedList<ImmutableBone>();
         List<ImmutableBone> unknownBonesNotMatchingLayout = new LinkedList<ImmutableBone>();
         for (ImmutableBone bone : initialState.getUnknownBones()) {
             if (bone.matches(placedBone.left()) || bone.matches(placedBone.right()))
                 unknownBonesMatchingLayout.add(bone);
             else
                 unknownBonesNotMatchingLayout.add(bone);
         }
 
        double probOfBoneNotMatchingLayoutBeingPickedUpOnFirstGo = 7.0 / unknownBonesNotMatchingLayout.size() + (1 - 7.0 / unknownBonesNotMatchingLayout.size()) * 1.0 / initialState.getSizeOfBoneyard();
        double probOfBoneMatchingLayoutBeingPickedUpOnFirstGo = 1.0 / initialState.getSizeOfBoneyard();
 
         for (ImmutableBone bone : opponentPickedUp.getUnknownBones()) {
             if (bone.matches(placedBone.right()) || bone.matches(placedBone.left())) {
                 assertEquals(probOfBoneMatchingLayoutBeingPickedUpOnFirstGo, opponentPickedUp.getProbThatOpponentHasBone(bone), 0.001);
             } else {
                 assertEquals(probOfBoneNotMatchingLayoutBeingPickedUpOnFirstGo, opponentPickedUp.getProbThatOpponentHasBone(bone), 0.001);
             }
         }
 
     }
 
     @Test
     public void testProbabilityAfterOpponentPickedUpThenPlaced() throws Exception {
         Choice choice = null;
 
         for (ImmutableBone bone : opponentPickedUp.getUnknownBones()) {
             if (bone.matches(placedBone.right()))
                 choice = new Choice(Choice.Action.PLACED_RIGHT, bone);
             else if (bone.matches(placedBone.left()))
                 choice = new Choice(Choice.Action.PLACED_LEFT, bone);
         }
 
         // Now choice is a possible placement choice.
         BoneState opponentPlacedAfterPickingUp = opponentPickedUp.createNext(choice, false);
 
         int bonesNotMatchingLayout = 0;
         for (ImmutableBone bone : opponentPickedUp.getUnknownBones())
             if (!bone.matches(placedBone.left()) && !bone.matches(placedBone.right()))
                 bonesNotMatchingLayout += 1;
 
         double probOpponentHasBone = 7.0 / bonesNotMatchingLayout;
 
         System.out.println(opponentPickedUp);
         System.out.println(opponentPlacedAfterPickingUp);
 
         for (ImmutableBone bone : opponentPlacedAfterPickingUp.getUnknownBones()) {
             if (bone.matches(placedBone.left()) || bone.matches(placedBone.right())) {
                 assertEquals(0.0, opponentPlacedAfterPickingUp.getProbThatOpponentHasBone(bone), 0.001);
             } else {
                 assertEquals(probOpponentHasBone, opponentPlacedAfterPickingUp.getProbThatOpponentHasBone(bone), 0.001);
             }
         }
     }
 
     @Test
     public void testGetInitialLayout() throws Exception {
         assertEquals(placedBone.left(), initialState.getLayoutLeft());
         assertEquals(placedBone.right(), initialState.getLayoutRight());
     }
 
     @Test
     public void testGetLayoutAfterPlacingRight() throws Exception {
         // Get matching bone
         ImmutableBone newlyPlacedBone = null;
         for (ImmutableBone bone : unknownBones) {
             if (bone.matches(placedBone.right())) {
                 newlyPlacedBone = bone;
                 break;
             }
         }
 
         if (newlyPlacedBone == null) throw new NullPointerException();
 
         BoneState thenPlacedRight = initialState.createNext(new Choice(Choice.Action.PLACED_RIGHT, newlyPlacedBone), false);
 
         assertEquals(placedBone.left(), thenPlacedRight.getLayoutLeft());
 
         int newRightValue = placedBone.right() == newlyPlacedBone.left() ? newlyPlacedBone.right() : newlyPlacedBone.left();
         assertEquals(newRightValue, thenPlacedRight.getLayoutRight());
     }
 
     @Test
     public void testGetLayoutAfterPlacingLeft() throws Exception {
         // Get matching bone
         ImmutableBone newlyPlacedBone = null;
         for (ImmutableBone bone : unknownBones) {
             if (bone.matches(placedBone.left())) {
                 newlyPlacedBone = bone;
                 break;
             }
         }
 
         if (newlyPlacedBone == null) throw new NullPointerException();
 
         BoneState thenPlacedLeft = initialState.createNext(new Choice(Choice.Action.PLACED_LEFT, newlyPlacedBone), false);
         System.out.format("Starting with bone %s , layout is [%d ... %d]", placedBone.toString(), initialState.getLayoutLeft(), initialState.getLayoutRight());
         System.out.format("Placed left bone %s , layout is [%d ... %d]", newlyPlacedBone.toString(), thenPlacedLeft.getLayoutLeft(), thenPlacedLeft.getLayoutRight());
         assertEquals(placedBone.right(), thenPlacedLeft.getLayoutRight());
 
         int newLeftValue = placedBone.left() == newlyPlacedBone.right() ? newlyPlacedBone.left() : newlyPlacedBone.right();
         assertEquals(newLeftValue, thenPlacedLeft.getLayoutLeft());
     }
 
     @Test
     public void testIsLayoutEmpty() throws Exception {
         assertTrue(preGameState.isLayoutEmpty());
         assertFalse(initialState.isLayoutEmpty());
     }
 }
