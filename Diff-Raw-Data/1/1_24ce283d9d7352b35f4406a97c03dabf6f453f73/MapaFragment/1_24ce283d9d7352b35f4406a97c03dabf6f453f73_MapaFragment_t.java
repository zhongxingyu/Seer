 package br.com.dps.fragment;
 
 import br.com.dps.map.AtualizadorDeLocalizacao;
 import br.com.dps.task.ColocaAlunosNoMapaTask;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 
 public class MapaFragment extends SupportMapFragment {
 	
 	private AtualizadorDeLocalizacao updater;
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		updater = new AtualizadorDeLocalizacao(getActivity(), this);
 		
 		new ColocaAlunosNoMapaTask(this).execute(this);
 	}
 
 	public void centralizaNo(LatLng local) {
 		GoogleMap map = this.getMap();
 		map.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 17));
 		
 	}
 	
 	@Override
 	public void onDestroy() {
		super.onDestroy();
 		updater.cancela();
 	}
 
 }
