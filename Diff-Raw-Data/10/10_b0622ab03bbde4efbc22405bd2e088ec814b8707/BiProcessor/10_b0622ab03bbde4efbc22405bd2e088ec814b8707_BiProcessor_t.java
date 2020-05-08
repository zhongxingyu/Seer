 package dem.processing;
 
 import dem.quanta.Event;
 import dem.quanta.Handler;
 import dem.quanta.Source;
 import dem.stuff.MutableSource;
 
 /**
  * @author Devgru &lt;java@devg.ru&gt;
  * @since 0.182
  */
 public abstract class BiProcessor<L extends Event, R extends Event> {
 
    private final BiConnector<R, L> left;
    private final BiConnector<L, R> right;
 
    protected BiProcessor(final BiConnector<R, L> left,
                        final BiConnector<L, R> right) {
 
         CommonProcessor<L> leftProcessor = new CommonProcessor<L>(left) {
             public void handle(L event) {
                 if (alive && fireLeft(event)) fire(event);
             }
         };
 
         CommonProcessor<R> rightProcessor = new CommonProcessor<R>(right) {
             public void handle(R event) {
                 if (alive && fireRight(event)) fire(event);
             }
         };
 
         left.setTarget(new Link<R>(rightProcessor, leftProcessor));
         right.setTarget(new Link<L>(leftProcessor, rightProcessor));
         this.left = left;
         this.right = right;
     }
 
     public boolean check() {
         boolean leftClear = left.getTarget() == null;
         boolean rightClear = right.getTarget() == null;
         if (leftClear != rightClear) {
             left.setTarget(null);
             right.setTarget(null);
             return false;
         } else {
             return leftClear;
         }
     }
 
     public boolean hangup() {
         if (check()) {
             left.setTarget(null);
             right.setTarget(null);
             return true;
         }
         return false;
     }
 
     protected boolean fireLeft(L event) {
         return true;
     }
 
     protected boolean fireRight(R event) {
         return true;
     }
 
     private abstract static class CommonProcessor<E extends Event>
             extends Source<E> implements Handler<E> {
 
        boolean alive = true;
 
         protected CommonProcessor(Handler<? super E> target) {
             super(target);
         }
 
     }
 
     private class Link<E extends Event> extends MutableSource<E> implements Handler<E> {
         public final CommonProcessor<? super E> main;
         public final CommonProcessor<?> pair;
 
         private Link(CommonProcessor<? super E> main, CommonProcessor<?> pair) {
             setTarget(main);
             this.main = main;
             this.pair = pair;
         }
 
         public void handle(E event) {
             fire(event);
         }
 
         protected void finalize() throws Throwable {
             main.alive = false;
             pair.alive = false;
             super.finalize();
         }
     }
 
 }
