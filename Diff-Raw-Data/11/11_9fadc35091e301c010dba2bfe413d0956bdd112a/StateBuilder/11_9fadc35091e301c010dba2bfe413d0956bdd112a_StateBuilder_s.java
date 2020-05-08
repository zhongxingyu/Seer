 package edu.programming.hoover;
 
 import edu.programming.hoover.exception.BagOverflowHooverException;
 import edu.programming.hoover.exception.CellOverflowHooverException;
 import edu.programming.hoover.exception.EmptyBagHooverException;
 import edu.programming.hoover.exception.EmptyCellHooverException;
 import edu.programming.hoover.exception.OutOfBoundsHooverException;
 import edu.programming.hoover.model.Bag;
 import edu.programming.hoover.model.Bounds;
 import edu.programming.hoover.model.Content;
 import edu.programming.hoover.model.Direction;
 import edu.programming.hoover.model.Position;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: id967092
  * Date: 06/10/12
  * Time: 12:54
  * To change this template use File | Settings | File Templates.
  */
 public class StateBuilder {
     private int x = -1;
     private int y = -1;
     private int width = 0;
     private int height = 0;
     private int capacity = 0;
     private Map<Position, Integer> field = new HashMap<Position, Integer>();
     private Bag bag = new Bag(Integer.MAX_VALUE);
 
     public StateBuilder setHooverX(int x) {
         this.x = x;
 
         return this;
     }
 
     public StateBuilder setHooverY(int y) {
         this.y = y;
 
         return this;
     }
 
     public StateBuilder setWidth(int width) {
         this.width = width;
 
         return this;
     }
 
     public StateBuilder setHeight(int height) {
         this.height = height;
 
         return this;
     }
 
     public StateBuilder setCapacity(int capacity) {
         this.capacity = capacity;
 
         return this;
     }
 
     public StateBuilder setField(Map<Position, Integer> field) {
         return clearField().mergeField(field);
     }
 
     public StateBuilder setHoover(Position hoover) {
         return setHoover(hoover.getX(), hoover.getY());
     }
 
     public StateBuilder setHoover(int x, int y) {
         return setHooverX(x).setHooverY(y);
     }
 
     public StateBuilder setBag(int capacity, int... items) {
         return setCapacity(capacity).clearBag().pushBag(items);
     }
 
     public StateBuilder setBounds(int width, int height) {
         return setWidth(width).setHeight(height);
     }
 
     public StateBuilder setBounds(Bounds bounds) {
         return setWidth(bounds.getWidth()).setHeight(bounds.getHeight());
     }
 
     public StateBuilder mergeField(Map<Position, Integer> field) {
         this.field.putAll(field);
 
         return this;
     }
 
     public StateBuilder clearField() {
         field.clear();
 
         return this;
     }
 
     public StateBuilder setFieldRow(int row, Integer... items) {
        for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
            Integer item = items[i];
 
            if (item != null) {
                field.put(new Position(i, row), item);
             }
         }
 
         return this;
     }
 
     public StateBuilder pushBag(int... items) {
         try {
             this.bag.pushAll(items);
         } catch (BagOverflowHooverException e) {
             throw new IllegalArgumentException("Too many values in bag");
         }
 
         return this;
     }
 
     public StateBuilder clearBag() {
         bag.clear();
 
         return this;
     }
 
     public State toState() throws OutOfBoundsHooverException, BagOverflowHooverException {
         Bounds bounds = new Bounds(width, height);
 
         for (Position position : this.field.keySet()) {
             if (!bounds.contains(position)) {
                 throw new OutOfBoundsHooverException(position, bounds);
             }
         }
 
         Bag bag = new Bag(capacity, this.bag);
         Map<Position, Integer> field = new HashMap<>(this.field);
 
         return new ImmutableState(bounds, new Position(x, y), bag, field);
     }
 
     private static class ImmutableState implements State {
         private final Bounds bounds;
         private final Position hoover;
         private final Bag bag;
         private final Map<Position, Integer> field;
 
         private ImmutableState(ImmutableState state, Position hoover) throws OutOfBoundsHooverException {
             this(state.bounds, hoover, state.bag, state.field);
         }
 
         private ImmutableState(ImmutableState state, Bag bag, Map<Position, Integer> field) {
             this.bounds = state.bounds;
             this.hoover = state.hoover;
             this.bag = bag;
             this.field = field;
         }
 
         private ImmutableState(Bounds bounds, Position hoover, Bag bag, Map<Position, Integer> field) throws OutOfBoundsHooverException {
             if (!bounds.contains(hoover)) {
                 throw new OutOfBoundsHooverException(hoover, bounds);
             }
 
             this.bounds = bounds;
             this.hoover = hoover;
             this.bag = bag;
             this.field = field;
         }
 
         @Override
         public State move(Direction direction) throws OutOfBoundsHooverException {
             Position hoover = this.hoover.move(direction);
 
             return new ImmutableState(this, hoover);
         }
 
         @Override
         public State take() throws BagOverflowHooverException, EmptyCellHooverException {
             Bag bag = new Bag(this.bag);
             Map<Position, Integer> field = new HashMap<>(this.field);
 
             Integer item = field.get(this.hoover);
 
             if (item == null) {
                 throw new EmptyCellHooverException(this.hoover);
             }
 
             bag.push(item);
 
             return new ImmutableState(this, bag, field);
         }
 
         @Override
         public State put() throws CellOverflowHooverException, EmptyBagHooverException {
             Bag bag = new Bag(this.bag);
             Map<Position, Integer> field = new HashMap<>(this.field);
 
             if (field.containsKey(this.hoover)) {
                 throw new CellOverflowHooverException(this.hoover);
             }
 
             field.put(this.hoover, bag.pop());
 
             return new ImmutableState(this, bag, field);
         }
 
         @Override
         public boolean match() throws EmptyCellHooverException, EmptyBagHooverException {
             Integer item = field.get(this.hoover);
 
             if (item == null) {
                 throw new EmptyCellHooverException(this.hoover);
             }
 
             return bag.peek() == item;
         }
 
         @Override
         public boolean canMove(Direction direction) {
             return bounds.contains(this.hoover.move(direction));
         }
 
         @Override
         public boolean isBagContent(Content content) {
             switch (content) {
                 case FULL:
                     return bag.isFull();
                 case EMPTY:
                     return bag.isEmpty();
                 default:
                     throw new IllegalArgumentException("Unknown content: " + content);
             }
         }
 
         @Override
         public boolean isCellContent(Content content) {
             switch (content) {
                 case FULL:
                     return field.containsKey(hoover);
                 case EMPTY:
                     return !field.containsKey(hoover);
                 default:
                     throw new IllegalArgumentException("Unknown content: " + content);
             }
         }
     }
 }
