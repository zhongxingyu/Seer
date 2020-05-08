 package com.github.colorlines.domainimpl;
 
 import com.github.colorlines.domain.Area;
 import com.github.colorlines.domain.Position;
 import com.github.colorlines.domain.Turn;
 import com.github.colorlines.domain.TurnValidator;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 import javax.annotation.Nullable;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Predicates.and;
 import static com.google.common.collect.ImmutableSet.of;
 import static com.google.common.collect.Sets.*;
 import static java.lang.Math.ceil;
 
 /**
  * @author Stanislav Kurilin
  */
 public class TurnValidatorImpl implements TurnValidator {
     @Override
     public boolean isValid(Area area, Turn turn) {
         if (!area.contains(turn.original().position())) {
             return false;
         }
         if (area.contains(turn.moveTo())) {
             return false;
         }
 
         final PriorityQueue<PathStep> pathSteps = new PriorityQueue<PathStep>(
                 (int) ceil(distance(turn.moveTo(), turn.original().position())), predictor(turn.moveTo()));
         final Set<Position> visited = newHashSet();
         pathSteps.add(new PathStep(0, turn.original().position()));
         do {
             final PathStep current = pathSteps.poll();
             if (current.current.equals(turn.moveTo())) {
                return true;
             }
             final Set<Position> validNextSteps =
                     filter(neiborhoods(current.current), and(empty(area), nonVisited(visited)));
             for (Position each : validNextSteps) {
                 pathSteps.add(new PathStep(current.stepsDone + 1, each));
             }
             visited.addAll(validNextSteps);
         } while (!pathSteps.isEmpty());
 
         return false;
     }
 
     private Predicate<Position> empty(final Area area) {
         return new Predicate<Position>() {
             @Override
             public boolean apply(@Nullable Position input) {
                 return !area.contains(input);
             }
         };
     }
 
     private Predicate<Position> nonVisited(final Set<Position> visited) {
         return new Predicate<Position>() {
             @Override
             public boolean apply(@Nullable Position input) {
                 return !visited.contains(input);
             }
         };
     }
 
     private static double distance(Position left, Position right) {
         return Math.abs(left.getX() - right.getX()) + Math.abs(left.getY() - right.getY());
     }
 
     private static Comparator<PathStep> predictor(final Position target) {
         return new Comparator<PathStep>() {
             @Override
             public int compare(PathStep left, PathStep right) {
                 return Double.compare(left.stepsDone + distance(left.current, target),
                         right.stepsDone + distance(right.current, target));
             }
         };
     }
 
     private Set<Position> neiborhoods(Position left) {
         return newHashSet(Collections2.transform(filter(cartesianProduct(of(left.getX(), left.getX() + 1, left.getX() - 1),
                 of(left.getY(), left.getY() + 1, left.getY() - 1)), new Predicate<List<Integer>>() {
             @Override
             public boolean apply(List<Integer> input) {
                 checkArgument(input.size() == 2);
                 final Integer x = input.get(0);
                 final Integer y = input.get(1);
                 return Position.WIDTH_RANGE.contains(x) && Position.HEIGHT_RANGE.contains(y);
             }
         }), new Function<List<Integer>, Position>() {
             @Override
             public Position apply(List<Integer> input) {
                 checkArgument(input.size() == 2);
                 final Integer x = input.get(0);
                 final Integer y = input.get(1);
                 return Position.create(x, y);
             }
         }));
     }
 
     private static class PathStep {
         final int stepsDone;
         final Position current;
 
         private PathStep(int stepsDone, Position current) {
             this.stepsDone = stepsDone;
             this.current = current;
         }
     }
 }
