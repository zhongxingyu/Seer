 package com.goodow.web.reader.client;
 
 import com.goodow.web.core.client.FlowView;
 import com.goodow.web.core.shared.WebPlace;
 import com.goodow.web.reader.shared.ReaderPlace;
 
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 import com.googlecode.mgwt.ui.client.widget.CellList;
 import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedEvent;
 import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedHandler;
 
 import java.util.List;
 
 public abstract class PlaceList extends FlowView {
 
   private CellList<WebPlace> cellListWithHeader;
 
   @Inject
   FlowPanel container;
 
   @Inject
   EventBus eventBus;
 
   @Inject
   ReaderPlace reader;
 
   @Inject
   PlaceController placeController;
 
   private int oldIndex;
 
   private List<WebPlace> places;
 
   public void addRightWidget(final Widget widget) {
     container.add(widget);
   }
 
   @Override
   public void setPlace(final WebPlace place) {
     super.setPlace(place);
     if (cellListWithHeader != null) {
      List<WebPlace> newPlaces = getPlaces();
      if (places.containsAll(newPlaces)) {
        return;
      }
      places = newPlaces;
      cellListWithHeader.render(places);
     }
   }
 
   protected abstract List<WebPlace> getPlaces();
 
   @Override
   protected void start() {
 
     cellListWithHeader = new CellList<WebPlace>(new BookCell<WebPlace>() {
       @Override
       public boolean canBeSelected(final WebPlace place) {
         return true;
       }
 
       @Override
       public String getDisplayString(final WebPlace place) {
         return place.getTitle();
       }
     });
 
     cellListWithHeader.addCellSelectedHandler(new CellSelectedHandler() {
       @Override
       public void onCellSelected(final CellSelectedEvent event) {
         int index = event.getIndex();
         cellListWithHeader.setSelectedIndex(oldIndex, false);
         cellListWithHeader.setSelectedIndex(index, true);
         oldIndex = index;
         WebPlace child = places.get(index);
         placeController.goTo(child);
       }
     });
     cellListWithHeader.setRound(true);
 
     // container = new HTMLPanel("");
     container.insert(cellListWithHeader, 0);
 
     main.add(container);
     // eventBus.addHandler(PlaceChangeEvent.TYPE, this);
     if (place != null) {
       cellListWithHeader.render(places = getPlaces());
     }
   }
 
 }
