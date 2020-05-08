 package net.premereur.mvp.core;
 
 /**
  * The interface views need to implements if they want their managing presenter injected.
  * 
 * Try to avoid using this, but make the presenter pass itself for GUI event call backs and such.
  * 
  * @author gpremer
  * 
 * @param
 * <P>
  */
 public interface NeedsPresenter<P extends Presenter<? extends View, ?>> {
 
     void setPresenter(P p);
 }
