 package br.ufsm.brunodea.tcc.map;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import br.ufsm.brunodea.tcc.map.EventsItemizedOverlay.BalloonType;
 import br.ufsm.brunodea.tcc.model.EventItem;
 import br.ufsm.brunodea.tcc.model.EventType;
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 /**
  * Classe para gerenciar os diferentes tipos de EventsItemizedOverlays.
  * Como serão diversos tipos, tendo em vista que cada EventType tem seu
  * próprio EventsItemizedOverlay achou-se por bem criar uma classe para gerenciar
  * estes dados.
  * 
  * @author bruno
  *
  */
 public class EventOverlayManager {
 	
 	private Context mContext;
 	private MapView mMapView;
 	private List<Overlay> mMapOverlays;
 	
 	private HashMap<EventType, EventsItemizedOverlay> mEIOMap;
 	
 	public EventOverlayManager(Context context, MapView mapview, 
 			List<Overlay> overlays) {
 		mContext = context;
 		mMapView = mapview;
 		mEIOMap = new HashMap<EventType, EventsItemizedOverlay>();
 		mMapOverlays = overlays;
 	}
 	
 	/**
 	 * Retorna o EventsItemizedOverlay correspondente ao tipo passado como parâmetro.
 	 * 
 	 * @param type Tipo que determina qual EventsItemizedOverlay vai ser retornado.
 	 * @param infocitymap Referência à activity que EventsItemizedOverlay necessita.
 	 * @return EventsItemizedOverlay correspondente ao tipo type.
 	 */
 	public EventsItemizedOverlay getEventOverlay(EventType type, InfoCityMap infocitymap) {
 		EventsItemizedOverlay eio = null;
 		if(mEIOMap.containsKey(type)) {
 			eio = mEIOMap.get(type);
 		} else {
 			BalloonType balloon_type = null;
 			boolean draggable = false;
 			int position = 0;
 			switch(type.getName()) {
 			case ADD:
 				balloon_type = BalloonType.ADD;
 				draggable = true;
 				position = mEIOMap.size();
 				break;
 			default:
 				balloon_type = BalloonType.INFO;
 				break;
 			}
 			eio = new EventsItemizedOverlay(mContext,
 					mMapView, balloon_type, type, infocitymap);
 			eio.setDraggable(draggable);
 			
 			mEIOMap.put(type, eio);
 			mMapOverlays.add(position, eio);
 		}
 		
 		return eio;
 	}
 	
 	/**
 	 * Método de manipulação dos dados dos EventsItemizedOverlays.
 	 */
 	public void addEventItem(EventItem item, InfoCityMap infocitymap) {
 		getEventOverlay(item.getType(), infocitymap).addEventItem(item);
 	}
 	public void removeEventItem(EventItem item) {
 		getEventOverlay(item.getType(), null).removeEventItemByPk(item.getPrimaryKey());
 	}
 	public void clearItemizedOverlays() {
		for(EventType type : mEIOMap.keySet()) {
			clearItemizedOverlaysOfType(type);
		}
 	}
 	public void clearItemizedOverlaysOfType(EventType type) {
 		if(mEIOMap.containsKey(type)) {
 			mEIOMap.get(type).setFocus(null);
 			mEIOMap.get(type).clearOverlays();
 			if(mMapOverlays.contains(mEIOMap.get(type))) {
 				mMapOverlays.remove(mEIOMap.get(type));
 			}
 			mEIOMap.remove(type);
 		}
 	}
 	public void clearItemizedOverlaysExcept(EventType except_type) {
		for(EventType type : mEIOMap.keySet()) {
 			if(type != except_type) {
				clearItemizedOverlaysOfType(type);
 			}
 		}
 	}
 	public ArrayList<Integer> getAllPks() {
 		ArrayList<Integer> pks = new ArrayList<Integer>();
 		for(EventsItemizedOverlay eio : mEIOMap.values()) {
 			pks.addAll(eio.getAllPks());
 		}
 		return pks;
 	}
 	
 	public Collection<EventsItemizedOverlay> getItemizedOverlays() {
 		return mEIOMap.values();
 	}
 }
