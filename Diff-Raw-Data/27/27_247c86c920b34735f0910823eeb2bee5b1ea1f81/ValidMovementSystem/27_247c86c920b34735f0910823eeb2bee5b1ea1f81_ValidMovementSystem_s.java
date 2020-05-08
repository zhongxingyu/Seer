 package j3chess.systems;
 
 import j3chess.Field;
 import j3chess.J3ChessApp;
 import j3chess.components.Movement;
 import j3chess.components.PieceContext;
 import j3chess.components.Position;
 import j3chess.components.ValidMovement;
 import j3chess.controller.Player;
 import j3chess.motion.FieldDirection;
 import j3chess.motion.Motion;
 import j3chess.motion.MotionPattern;
 import j3chess.motion.Move;
 import j3chess.motion.MoveType;
 import j3chess.motion.PieceDirection;
 import j3chess.utility.Helper;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import artemis.Aspect;
 import artemis.ComponentMapper;
 import artemis.Entity;
 import artemis.annotations.Mapper;
 import artemis.systems.EntityProcessingSystem;
 
 /**
  * entity processing system which checks for every possible move of an entity.
  */
 public class ValidMovementSystem extends EntityProcessingSystem {
 
     /** @brief fast component mapper to retrieve position component */
     @Mapper
     private ComponentMapper<Position> mPositionMapper;
     /** @brief fast component mapper to retrieve movement component */
     @Mapper
     private ComponentMapper<Movement> mMovementMapper;
     /** @brief fast component mapper to retrieve valid movement component */
     @Mapper
     private ComponentMapper<ValidMovement> mValidMovementMapper;
     /** @brief fast component mapper to retrieve piece context component */
     @Mapper
     private ComponentMapper<PieceContext> mPieceContextMapper;
 
     /**
      * @brief the valid movement system interprets the movement capabilities of
      *        entities and creates a list of possible move targets
      */
     public ValidMovementSystem() {
         super(Aspect.getAspectForAll(Position.class, Movement.class,
                 ValidMovement.class));
     }
 
     /* @see artemis.EntitySystem#initialize() */
     @Override
     protected final void initialize() {
         super.initialize();
     }
 
     /**
      * @brief checks for valid moves of an movable entity.
      * @param entity
      *            the movable entity to process
      * @see artemis.systems.EntityProcessingSystem#process(artemis.Entity)
      */
     @Override
     protected final void process(final Entity entity) {
 
         // retrieve relevant components from the entity
         final Movement movement = mMovementMapper.get(entity); // not global
         final Position position = mPositionMapper.get(entity);
         mCurrentStartField = position.getField();
         mCurrentValidMovement = mValidMovementMapper.get(entity);
         // mCurrentValidMovement.setValidCaptureMoves(new ArrayList<Move>());
         // mCurrentValidMovement.setValidNonCaptureMoves(new ArrayList<Move>());
         mCurrentValidNonCaptureMoves = new ArrayList<Move>();
         mCurrentValidCaptureMoves = new ArrayList<Move>();
         mCurrentPlayer = requestPlayer(position.getField());
 
         // for each motion pattern do...
         for (final MotionPattern pattern : movement.getPatterns()) {
 
             // there are no motions in this pattern. skip loop.
             if (pattern.getMotions().isEmpty()) {
                 J3ChessApp.getLogger().error("no motions within this pattern");
                 continue;
             }
 
             // retrieve valid moves from the motion pattern recursively
             recurseMotionPattern(mCurrentStartField, null, pattern, 0, 0,
                     movement.getCrossedCenter());
         }
     }
 
     /* ------------------------------------------------------------------------- */
     /* Entity-Related Members */
     /* ------------------------------------------------------------------------- */
 
     /** @brief the player of the currently processed entity */
     private Player mCurrentPlayer;
     /** @brief the starting position of the currently processed entity */
     private Field mCurrentStartField;
     /** @brief valid movement component of the currently processed entity */
     private ValidMovement mCurrentValidMovement;
 
     private List<Move> mCurrentValidNonCaptureMoves;
     private List<Move> mCurrentValidCaptureMoves;
 
     /* ------------------------------------------------------------------------- */
     /* Recursion */
     /* ------------------------------------------------------------------------- */
 
     /**
      * @brief Recursively interprets a motion pattern and saves the results to a
      *        ValidMovement component
      * @param currentField
      *            the current field from where to recurse
      * @param lastPieceDirection
      *            the piece direction evaluated in the last iteration. might be
      *            null.
      * @param currentMotionPattern
      *            the motion pattern to evaluate
      * @param currentMotionIndex
      *            the current index of the motion in the motion pattern to
      *            evaluate
      * @param currentStep
      *            the current step in the current motion
      * @param currentCrossedCenter
      *            the current status whether we crossed the center or not while
      *            evaluating the motion pattern
      */
     private void recurseMotionPattern(final Field currentField,
             final PieceDirection lastPieceDirection,
             final MotionPattern currentMotionPattern,
             final int currentMotionIndex, final int currentStep,
             final boolean currentCrossedCenter) {
 
         // //////////////
         // Basic Data //
         // //////////////
 
         final List<Motion> currentMotionList = currentMotionPattern
                 .getMotions();
 
         final Motion currentMotion = currentMotionList.get(currentMotionIndex);
 
         // ///////////////////////
         // Context Information //
         // ///////////////////////
 
         // whether the current motion is the last motion of the motion list
         final boolean isLastMotionOfList = (currentMotionIndex == (currentMotionList
                 .size() - 1));
 
         // the current step is the last step of the current motion
         final boolean isLastStepOfMotion = (currentStep == (currentMotion
                 .getStepCount() - 1));
 
         // whether the current step is the last step of the last motion
         final boolean isLastStepOfLastMotion = (isLastStepOfMotion & isLastMotionOfList);
 
         // the current motion pattern is either a jump (unblockable) and at
         // it's last step or a common motion pattern
         //
         // (!isJump | (isLastStep & isJump)) which may be simplified to
         // (!isJump | isLastStep)
         final boolean isAbleToMove = (!currentMotionPattern.isJump() | isLastStepOfLastMotion);
 
         // /////////
         // Logic //
         // /////////
 
         // possible directions to move into from current position
         final EnumSet<PieceDirection> possibleDirections = getPossibleDirections(
                 currentMotionPattern, currentMotion, currentMotionIndex,
                 lastPieceDirection);
 
         // recurse into every possible direction
         for (final PieceDirection currentPieceDirection : possibleDirections) {
 
             final FieldDirection currentFieldDirection = Helper.Direction
                     .toFieldDirection(currentPieceDirection,
                             currentCrossedCenter);
 
             // the piece crosses the center with it about to be evaluated move
             final boolean isCrossingCenter = currentField
                     .getWhetherCrossingCenter(currentFieldDirection);
 
             // XOR used to toggle the current crossed center status
             final boolean nextCrossedCenter = currentCrossedCenter
                     ^ isCrossingCenter;
 
             final Field nextField = currentField
                     .getNeighbor(currentFieldDirection);
 
             if (nextField == null) {
                 // there is no next field into the current direction
                 continue; // skip this loop iteration
             }
 
            /*
             * ------------------------------------------------------------------
             * -------
             */

             // does not apply to anything but the last step of a jump as well as
             // non jumping movement
             if (isAbleToMove) {
                 // check for blocker
                 if (nextField.getPiece() != null) {
                     // there is something on the next field -> check for capture
                     if (mCurrentPlayer != requestPlayer(nextField)) {
                         // there is something capturable on the next field, thus
                         // it is a valid move
                         final Move nextCaptureMove = new Move(MoveType.Capture,
                                 mCurrentStartField, nextField, isCrossingCenter);
                         mCurrentValidCaptureMoves.add(nextCaptureMove);
                     }
                     // this motion direction is blocked
                     continue; // skip this loop iteration
                 }
                 // there is nothing on the next field, thus it is a valid move
                 final Move nextNonCaptureMove = new Move(
                         MoveType.Disambiguating, mCurrentStartField, nextField,
                         isCrossingCenter);
                 mCurrentValidNonCaptureMoves.add(nextNonCaptureMove);
             }
 
            /*
             * ------------------------------------------------------------------
             * -------
             */

             int nextMotionIndex = -1;
             int nextStep = -1;
 
             if (!isLastStepOfMotion) {
                 // same motion and next step
                 nextMotionIndex = currentMotionIndex;
                 nextStep = currentStep + 1;
             } else {
                 if (!isLastMotionOfList) {
                     // next motion and first step
                     nextMotionIndex = currentMotionIndex + 1;
                     nextStep = 0; // first step of new motion
                 } else {
                     // if it is the last step of this motion and the last motion
                     // of it's motion pattern then
                     // (nextMotionIndex = -1 && nextStep = -1)
                     continue; // skip this loop iteration
                 }
             }
 
            /*
             * ------------------------------------------------------------------
             * -------
             */

             // processing of the whole motion list is not done yet
             recurseMotionPattern(nextField, currentPieceDirection,
                     currentMotionPattern, nextMotionIndex, nextStep,
                     nextCrossedCenter);
         }
 
         // write the valid movement lists after a complete recursion
         mCurrentValidMovement
                 .setValidNonCaptureMoves(mCurrentValidNonCaptureMoves);
         mCurrentValidMovement.setValidCaptureMoves(mCurrentValidCaptureMoves);
     }
 
     /* ------------------------------------------------------------------------- */
     /* Possible Directions */
     /* ------------------------------------------------------------------------- */
 
     /**
      * @brief evaluate the currently possible directions of the motion
      * @param motionPattern
      *            the motion pattern to evaluate
      * @param motion
      *            the motion to evaluate
      * @param currentMotionIndex
      *            the index of the current motion required for the first motion
      *            of a list detection
      * @param lastPieceDirection
      *            the direction of the preceding motion
      * @return the possible directions of a motion
      */
     private EnumSet<PieceDirection> getPossibleDirections(
             final MotionPattern motionPattern, final Motion motion,
             final int currentMotionIndex,
             final PieceDirection lastPieceDirection) {
 
         EnumSet<PieceDirection> directions = EnumSet
                 .noneOf(PieceDirection.class);
 
         // the current step is not the first step of the current motion
         if (lastPieceDirection != null) {
             // go on into the same direction as in the step before...
             directions = EnumSet.of(lastPieceDirection);
         } else {
             // the current motion is the first step and the first motion of the
             // current motion pattern
             if (currentMotionIndex == 0) {
                 // mask the starting directions of the motion pattern
                 directions = motion
                         .getDirectionsMasked(motionPattern.getMask());
             } else {
                 // retrieve the possible directions from the motion
                 directions = motion.getDirections();
             }
         }
 
         if (directions.equals(EnumSet.noneOf(PieceDirection.class))) {
             // the set of directions is empty
             J3ChessApp.getLogger().debug(
                     "no possible directions in " + motionPattern.toString());
         }
 
         return directions;
     }
 
     /* ------------------------------------------------------------------------- */
     /* Convenience */
     /* ------------------------------------------------------------------------- */
 
     /**
      * @brief getter for the player of the currently processed entity. for
      *        convenience purposes only.
      * @param field
      *            the field to check
      * @return the player of the currently processed entity
      */
     private Player requestPlayer(final Field field) {
         return mPieceContextMapper.get(field.getPiece()).getPlayer();
     }
 
 }
