 package net.premereur.mvp.core;
 
 /**
  * The interface views need to implements if they want their managing presenter injected.
  * 
 * Try to avoid using this, but make the presenter pass itself for GUI event call backs and such. Also, when using Guice, injection is handled by Guice and this
 * interface is not needed.
  * 
  * @author gpremer
  * 
 * @param <P> The presenter that is required.
  */
 public interface NeedsPresenter<P extends Presenter<? extends View, ?>> {
 
    /**
     * Injector method for presenter managing the view.
     * @param p the presenter
     */
     void setPresenter(P p);
 }
