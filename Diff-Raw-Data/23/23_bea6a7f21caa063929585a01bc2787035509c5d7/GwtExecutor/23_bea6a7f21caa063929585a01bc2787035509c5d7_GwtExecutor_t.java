 package ru.finam.bustard.gwt;
 
 import com.google.gwt.user.client.Timer;
 import ru.finam.bustard.Executor;
 
 public class GwtExecutor implements Executor {
 
     @Override
    public void execute(final Runnable runnable) {
        new Timer() {
            @Override
            public void run() {
                runnable.run();
            }
        }.schedule(0);
     }
 }
