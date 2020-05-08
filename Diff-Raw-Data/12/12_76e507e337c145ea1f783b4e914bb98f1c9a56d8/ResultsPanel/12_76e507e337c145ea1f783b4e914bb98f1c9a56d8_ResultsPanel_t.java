 package agregator.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JComponent;
 
 import agregator.core.Exclusions;
 import agregator.core.Result;
 
 public abstract class ResultsPanel<R extends Result> {
 
     private final List<ResultSelectionListener<R>> listeners =
             Collections.synchronizedList(new ArrayList<ResultSelectionListener<R>>());
 
     private final Exclusions excludedResults;
 
     public ResultsPanel(Exclusions excludedResults) {
       if (excludedResults==null) {
         throw new IllegalArgumentException("excludedResults cannot be null");
       }
       this.excludedResults = excludedResults;
     }
 
     public Exclusions getExclusions() {
       return excludedResults;
     }
 
     public void addListener(ResultSelectionListener<R> listener) {
         listeners.add(listener);
     }
 
     public abstract JComponent getComponent();
 
     public abstract void clear();
 
     public void searchStarted() { }
 
    public void searchStopped() { }

     public abstract void addResult(R r);
 
     public void fireResultSelected(R r) {
         for (ResultSelectionListener<R> l : listeners) {
             l.resultSelected(r);
         }
     }
 
 }
