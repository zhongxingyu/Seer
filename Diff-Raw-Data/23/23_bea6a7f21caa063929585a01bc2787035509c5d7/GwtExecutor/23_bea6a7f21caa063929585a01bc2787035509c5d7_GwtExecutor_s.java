 package ru.finam.bustard.gwt;
 
 import com.google.gwt.user.client.Timer;
 import ru.finam.bustard.Executor;
 
 public class GwtExecutor implements Executor {
 
     @Override
    public void execute(Runnable runnable) {
        new RunnableTimer(runnable).schedule(0);
    }

    private class RunnableTimer extends Timer {

        final Runnable runnable;

        private RunnableTimer(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
     }
 }
