 package com.halcyonwaves.apps.meinemediathek.fragments;
 
 import org.acra.ACRA;
 
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.halcyonwaves.apps.meinemediathek.Consts;
 import com.halcyonwaves.apps.meinemediathek.R;
 import com.halcyonwaves.apps.meinemediathek.services.BackgroundDownloadService;
 
 public class MovieOverviewFragment extends Fragment {
 
 	private Button btnDownloadMoview = null;
 	private String downloadLink = "";
 	private ImageView ivPreviewImage = null;
 	private TextView tvMovieDescription = null;
 	private TextView tvMovieTitle = null;
 	private String previewImagePath = "";
 	private String uniqueId = "";
 
 	private static final String TAG = "MovieOverviewFragment";
 
 	private Messenger serviceMessanger = null;
 	private final ServiceConnection serviceConnection = new ServiceConnection() {
 
 		public void onServiceConnected( final ComponentName className, final IBinder service ) {
 			MovieOverviewFragment.this.serviceMessanger = new Messenger( service );
 			Log.d( MovieOverviewFragment.TAG, "Conntected to download service" );
 		}
 
 		public void onServiceDisconnected( final ComponentName className ) {
 			MovieOverviewFragment.this.serviceMessanger = null;
 			Log.d( MovieOverviewFragment.TAG, "Disconnected from download service" );
 		}
 	};
 
 	private void doBindService() {
 		if( null == this.serviceMessanger ) {
 			this.getActivity().bindService( new Intent( this.getActivity(), BackgroundDownloadService.class ), this.serviceConnection, Context.BIND_AUTO_CREATE );
 		}
 	}
 
 	private void doUnbindService() {
 		if( null != this.serviceMessanger ) {
 			this.getActivity().unbindService( this.serviceConnection );
 			this.serviceMessanger = null; // we have to do this because the onServiceDisconnected method gets just called if the service was killed
 		}
 	}
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 		this.doUnbindService();
 	}
 
 	@Override
 	public View onCreateView( final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState ) {
 		final View v = inflater.inflate( R.layout.fragment_movieoverview, container );
 
 		// get the information passed to the fragment
 		final Bundle passedInformation = this.getActivity().getIntent().getExtras();
 
 		// get the handles to the controls we need to access
 		this.tvMovieTitle = (TextView) v.findViewById( R.id.tv_movie_title_content );
 		this.tvMovieDescription = (TextView) v.findViewById( R.id.tv_movie_description_content );
 		this.ivPreviewImage = (ImageView) v.findViewById( R.id.iv_movie_preview_image );
 		this.btnDownloadMoview = (Button) v.findViewById( R.id.btn_download_movie );
 
 		// fetch some information we want to use later
		this.downloadLink = passedInformation.getString( Consts.EXTRA_NAME_MOVIE_DOWNLOADLINK );
 		this.previewImagePath = passedInformation.getString( Consts.EXTRA_NAME_MOVIE_PRVIEWIMAGEPATH );
 		this.uniqueId = passedInformation.getString( Consts.EXTRA_NAME_MOVIE_UNIQUE_ID );
 
 		// set the content for all of the fetched controls
 		this.tvMovieTitle.setText( passedInformation.getString( Consts.EXTRA_NAME_MOVIE_TITLE ) );
 		this.tvMovieDescription.setText( passedInformation.getString( Consts.EXTRA_NAME_MOVIE_DESCRIPTION ) );
 		this.ivPreviewImage.setImageBitmap( BitmapFactory.decodeFile( this.previewImagePath ) );
 
 		// tell the button what to do as soon as it gets clicked
 		this.btnDownloadMoview.setOnClickListener( new OnClickListener() {
 
 			@Override
 			public void onClick( final View v ) {
 				final ConnectivityManager cm = (ConnectivityManager) MovieOverviewFragment.this.getActivity().getSystemService( Context.CONNECTIVITY_SERVICE );
 				final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 				if( activeNetwork.getType() != ConnectivityManager.TYPE_WIFI ) { // TODO: we have to deal with WiMAX too
 					// prepare a dialog asking the user he or she really wants to do the download on a mobile connection
 					final AlertDialog.Builder builder = new AlertDialog.Builder( MovieOverviewFragment.this.getActivity() );
 					builder.setMessage( R.string.dlg_msg_download_on_mobile ).setTitle( R.string.dlg_title_download_on_mobile ).setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick( final DialogInterface dialog, final int id ) {
 							// the user decided to start the download even on the mobile network, so do it
 							MovieOverviewFragment.this.startEpisodeDownload();
 						}
 					} ).setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick( final DialogInterface dialog, final int id ) {
 							// the user said no, so do nothing here
 						}
 					} );
 
 					// show the dialog to the user
 					final AlertDialog askUserDialog = builder.create();
 					askUserDialog.show();
 				} else {
 					// as the user is using Wifi, we can start the download without asking again
 					MovieOverviewFragment.this.startEpisodeDownload();
 				}
 
 			}
 		} );
 
 		// be sure that we are connected to the download service
 		this.doBindService();
 
 		// return the created view
 		return v;
 	}
 
 	private void startEpisodeDownload() {
 		// prepare the information we want to send to the service
 		Bundle downloadExtras = new Bundle();
 		downloadExtras.putString( Consts.EXTRA_NAME_MOVIE_DOWNLOADLINK, this.downloadLink );
 		downloadExtras.putString( Consts.EXTRA_NAME_MOVIE_PRVIEWIMAGEPATH, this.previewImagePath );
 		downloadExtras.putString( Consts.EXTRA_NAME_MOVIE_TITLE, this.tvMovieTitle.getText().toString() );
 		downloadExtras.putString( Consts.EXTRA_NAME_MOVIE_UNIQUE_ID, this.uniqueId );
 
 		// prepare the download request
 		Message downloadRequest = new Message();
 		downloadRequest.setData( downloadExtras );
 		downloadRequest.what = BackgroundDownloadService.SERVICE_MSG_INITIATE_DOWNLOAD;
 		downloadRequest.replyTo = this.serviceMessanger;
 
 		// send the download request
 		try {
 			this.serviceMessanger.send( downloadRequest );
 		} catch( RemoteException e ) {
 			ACRA.getErrorReporter().handleException( e );
 		}
 	}
 }
