 /**
  *
  */
 package de.saumya.gwt.translation.common.client.widget;
 
 import java.util.Map;
 
 import com.google.gwt.user.client.ui.Widget;
 
 import de.saumya.gwt.persistence.client.Resource;
 import de.saumya.gwt.persistence.client.ResourceCollection;
 import de.saumya.gwt.persistence.client.ResourceFactory;
 import de.saumya.gwt.persistence.client.ResourcesChangeListener;
 import de.saumya.gwt.session.client.Session;
 import de.saumya.gwt.translation.common.client.route.HasPathFactory;
 
 public class ResourceScreen<E extends Resource<E>> extends
         AbstractResourceScreen<E> implements ResourceCollectionResetable<E> {
 
    final ResourceFactory<E>                 factory;
    final NotificationListeners              listeners;
    private final ResourcesChangeListener<E> listener;
 
     protected <S extends Widget & AllowReadOnly<E>, T extends Widget & ResourceCollectionResetable<E> & HasPathFactory> ResourceScreen(
             final LoadingNotice loadingNotice,
             final ResourceFactory<E> factory, final Session session,
             final S display, final T displayCollection,
             final AbstractResourceActionPanel<E> actions,
             final NotificationListeners listeners,
             final HyperlinkFactory hyperlinkFactory) {
         super(loadingNotice,
                 factory,
                 session,
                 display,
                 displayCollection,
                 displayCollection,
                 displayCollection,
                 actions,
                 listeners,
                 hyperlinkFactory);
         this.factory = factory;
         this.listeners = listeners;
        this.listener = new ResourcesChangeListener<E>() {

            @Override
            public void onLoaded(final ResourceCollection<E> resources) {
                reset(resources);
                ResourceScreen.this.loading.setVisible(false);
            }
        };
     }
 
     @Override
     public void reset(final ResourceCollection<E> resources) {
         this.displayCollectionResetable.reset(resources);
         this.actions.reset(resources);
         this.deck.showWidget(RESOURCES);
         this.loading.setVisible(false);
         setVisible(true);
     }
 
     @Override
     public void showNew() {
         this.loading.setVisible(true);
         this.displayAllowReadOnly.setReadOnly(false);
         reset(this.factory.newResource(0));
     }
 
     @Override
     public void showRead(final int key) {
         this.displayAllowReadOnly.setReadOnly(true);
         show(key);
     }
 
     @Override
     public void showEdit(final int key) {
         this.displayAllowReadOnly.setReadOnly(false);
         show(key);
     }
 
     protected void show(final int key) {
         final E resource = this.factory.get(key, this.resourceChangeListener);
         reset(resource);
         this.loading.setVisible(true);
     }
 
     @Override
     public void showAll(final Map<String, String> query) {
         final ResourceCollection<E> resources = this.factory.all(query,
                                                                 this.listener);
         // TODO should be set be onChange and onError. maybe make an application
         // wide notification widget
         reset(resources);
         this.loading.setVisible(true);
     }
 }
