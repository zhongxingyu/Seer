 /*
  * Copyright (C) 2009 - 2012 SMVP.NET
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package net.smvp.mvp.client.core.factory;
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.activity.shared.ActivityMapper;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.place.shared.*;
 import com.google.gwt.user.client.ui.SimplePanel;
 import net.smvp.factory.client.utils.ClassUtils;
 import net.smvp.mvp.client.core.eventbus.Event;
 import net.smvp.mvp.client.core.eventbus.annotation.EventHandler;
 import net.smvp.mvp.client.core.eventbus.annotation.HistoryHandler;
 import net.smvp.mvp.client.core.mapper.ActivityMapperImpl;
 import net.smvp.mvp.client.core.mapper.PlaceHistoryMapperImpl;
 import net.smvp.mvp.client.core.place.AbstractPlace;
 import net.smvp.mvp.client.core.place.DefaultPlace;
 import net.smvp.mvp.client.core.presenter.Presenter;
 import net.smvp.mvp.client.core.view.View;
 import net.smvp.mvp.client.widget.MenuLink;
 import net.smvp.reflection.client.field.FieldType;
 import net.smvp.reflection.client.method.MethodType;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Class ClientFactoryImpl.
  *
  * @author Nguyen Duc Dung
  * @since 11/17/11, 5:16 PM
  */
 public class ClientFactoryImpl implements ClientFactory {
 
     protected List<FactoryModel> factoryModels = new ArrayList<FactoryModel>();
     protected List<Presenter<? extends View>> presenters =
             new ArrayList<Presenter<? extends View>>();
 
     private EventBus eventBus = new SimpleEventBus();
     private PlaceController placeController = new PlaceController(eventBus);
 
     @Override
     public ActivityMapper createActivityMapper() {
         return new ActivityMapperImpl(factoryModels, this);
     }
 
     @Override
     public PlaceHistoryMapper createHistoryMapper() {
         return new PlaceHistoryMapperImpl(factoryModels, this);
     }
 
     @Override
     public void createAndHandleHistory() {
         ActivityManager activityManager = new ActivityManager(createActivityMapper(), eventBus);
         activityManager.setDisplay(new SimplePanel());
         PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(createHistoryMapper());
         placeHistoryHandler.register(placeController, eventBus, Place.NOWHERE);
         placeHistoryHandler.handleCurrentHistory();
     }
 
     protected <V extends View> void configurePresenter(Presenter<V> presenter, V view, AbstractPlace place) {
         presenter.setView(view);
         presenter.setPlace(place);
         presenter.setPlaceController(placeController);
         presenter.bind();
         presenters.add(presenter);
     }
 
     protected void configureHandler(final Object object) {
         if (object != null) {
             for (final MethodType method : ClassUtils.getMethods(object.getClass())) {
                 final EventHandler eventHandler = method.getAnnotation(EventHandler.class);
                 if (eventHandler != null) {
                     eventBus.addHandler(Event.TYPE, new net.smvp.mvp.client.core.eventbus.EventHandler() {
                         @Override
                         public boolean isMath(String eventName) {
                             return eventHandler.eventName().equals(eventName);
                         }
 
                         @Override
                         public void doHandle() {
                             method.invoke(object);
                         }
                     });
                 }
             }
 
             for (final FieldType fieldType : ClassUtils.getFields(object.getClass())) {
                 HistoryHandler historyHandler = fieldType.getAnnotation(HistoryHandler.class);
                 if (historyHandler != null) {
                     eventBus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
                         @Override
                         public void onPlaceChange(PlaceChangeEvent event) {
                             Object field = fieldType.get(object);
                             if (field instanceof MenuLink) {
                                 AbstractPlace place = (AbstractPlace) event.getNewPlace();
                                 if (place != null && ((MenuLink) field).
                                         getTargetHistoryToken().equals(place.getToken())) {
                                     ((MenuLink) field).setActive(true);
                                 } else {
                                     ((MenuLink) field).setActive(false);
                                 }
                             }
                         }
                     });
                 }
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <T extends Presenter<? extends View>> T getExitsPresenter(Class<T> presenterClass) {
         for (Presenter<? extends View> presenter : presenters) {
             if (ClassUtils.getRealClass(presenter) == presenterClass) {
                 return (T) presenter;
             }
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <P extends AbstractPlace> P getExitsPlace(Class<P> placeClass) {
         for (Presenter<? extends View> presenter : presenters) {
             if (ClassUtils.getRealClass(presenter.getPlace()) == placeClass) {
                 return (P) presenter.getPlace();
             }
         }
         return null;
     }
 
     @Override
     public void createDefaultPresenter() {
         for (FactoryModel model : factoryModels) {
             if (model.getPlaceClass() == DefaultPlace.class) {
                 Presenter presenter = createPresenter(model);
                 presenter.start(null, eventBus);
             }
         }
     }
 
     @Override
     public Presenter createPresenter(FactoryModel model) {
         Presenter presenter = instantiate(model.getPresenterClass());
         if (presenter != null) {
             View view = createView(model);
             AbstractPlace place = createPlace(model);
             configurePresenter(presenter, view, place);
             configureHandler(presenter);
         }
         return presenter;
     }
 
     protected View createView(FactoryModel model) {
         View view = instantiate(model.getViewClass());
         if (view != null) {
             view.setParentDomId(model.getViewParentDomID());
         }
         configureHandler(view);
         return view;
     }
 
     @Override
     public AbstractPlace createPlace(FactoryModel model) {
         AbstractPlace place = instantiate(model.getPlaceClass());
         if (place != null) {
             place.setToken(model.getToken());
         }
         return place;
     }
 
     @Override
     public void configure() {
         createDefaultPresenter();
        createAndHandleHistory();
     }
 
     protected <T, V extends T> T instantiate(Class<V> clazz) {
         return ClassUtils.instantiate(clazz);
     }
 }
