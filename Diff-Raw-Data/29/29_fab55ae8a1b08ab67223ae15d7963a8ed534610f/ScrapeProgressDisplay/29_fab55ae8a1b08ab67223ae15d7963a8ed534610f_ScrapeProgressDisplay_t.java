 package RefScraper.ui;
 
 /**
 * Interface to model display of progress of scince festival page scrape.
  * @author al
  */
 public interface ScrapeProgressDisplay {
     public void setText(String theText);
     public String getText();
     public void setProgress(int i);
 }
