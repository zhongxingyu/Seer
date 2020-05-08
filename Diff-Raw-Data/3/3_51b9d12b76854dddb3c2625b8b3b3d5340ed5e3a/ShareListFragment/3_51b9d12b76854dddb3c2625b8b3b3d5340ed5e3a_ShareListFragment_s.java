 package br.unb.mobileMedia.core.view;
 
 import org.brickred.socialauth.android.DialogListener;
 import org.brickred.socialauth.android.SocialAuthAdapter;
 import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
 import org.brickred.socialauth.android.SocialAuthError;
 
 import br.unb.mobileMedia.R;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 public class ShareListFragment extends Fragment{
 
 	private SocialAuthAdapter adapterTopArtists,adapterTopSongs,adapterShareApp;
 	private Button btnTopArtists,btnTopSongs,btnShareApp;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		//setContentView(br.unb.mobileMedia.R.layout.activity_share_list);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		getActivity().setTitle(R.string.title_activity_play_list);
 		return inflater.inflate(R.layout.activity_share_list, container, false);
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
 		super.onActivityCreated(savedInstanceState);
 		
 		configureUI();
 	}
 	
 	private void configureUI() {
 		btnTopArtists = (Button)getActivity().findViewById(br.unb.mobileMedia.R.id.btn_top_artists);
 		btnTopSongs = (Button)getActivity().findViewById(br.unb.mobileMedia.R.id.btn_top_songs);
 		btnShareApp = (Button)getActivity().findViewById(br.unb.mobileMedia.R.id.btn_share_app);
 /*		btTopArtists.setText(br.unb.mobileMedia.R.string.btn_top_artists);
 		btTopArtists.setTextColor(Color.WHITE);
 		btTopArtists.setBackgroundResource(br.unb.mobileMedia.R.drawable.button_gradient);
 		
 //		Log.d(getLocalClassName(), "ESTOI AKI");
 		ResponseListener listenerTopArtists = new ResponseListener("MM UnB Totalmente Funcional via App");
 		adapterTopArtists = new SocialAuthAdapter(listenerTopArtists);
 //		Log.d(getLocalClassName(), "QUERENDO-TE");
 		
 		adapterTopArtists.addProvider(Provider.FACEBOOK, br.unb.mobileMedia.R.drawable.facebook);
 		adapterTopArtists.addProvider(Provider.TWITTER, br.unb.mobileMedia.R.drawable.twitter);
 		
 		adapterTopArtists.enable(btnTopArtists);
 */
 		ResponseListener listener = new ResponseListener("default",adapterTopArtists);
 		String conteudo = "DEFAULT";
 		//TopArtists
 		adapterTopArtists = new SocialAuthAdapter(listener);
 		/*
 		 * Pegar as informacoes dos Top Artistas e passar para a string conteudo
 		 * */
 		configAdapter(adapterTopArtists,conteudo,btnTopArtists);
 		
 		//TopSongs
 		adapterTopSongs = new SocialAuthAdapter(listener);
 		/*
 		 * Pegar as informacoes dos Top Songs e passar para a string conteudo
 		 * */
 		configAdapter(adapterTopSongs,conteudo,btnTopSongs);
 		
 		//ShareApp
 		adapterShareApp = new SocialAuthAdapter(listener);
 		/*
 		 * Pegar as informacoes dos ShareAPP e passar para a string conteudo
 		 * */
 		configAdapter(adapterShareApp,conteudo,btnShareApp);
 	}
 	
 	
 	private void configAdapter(SocialAuthAdapter adapter, String conteudo, Button btn){
 		ResponseListener listener = new ResponseListener(conteudo,adapter);
 		adapter.setListener(listener);
 		
 		adapter.addProvider(Provider.FACEBOOK, br.unb.mobileMedia.R.drawable.facebook);
 		adapter.addProvider(Provider.TWITTER, br.unb.mobileMedia.R.drawable.twitter);
 		
 		adapter.enable(btn);
 	}
 	
 	private final class ResponseListener implements DialogListener {
 		
 		private SocialAuthAdapter adapter;
 		private String conteudo;
 		
 		public ResponseListener(String conteudo,SocialAuthAdapter adapter){
 			this.conteudo = conteudo;
 			this.adapter = adapter;
 		}
 
 		public void onCancel() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void onComplete(Bundle arg0) {
 			adapter.updateStatus(conteudo);
 		}
 
 		public void onError(SocialAuthError arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void onBack() {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 }
 
