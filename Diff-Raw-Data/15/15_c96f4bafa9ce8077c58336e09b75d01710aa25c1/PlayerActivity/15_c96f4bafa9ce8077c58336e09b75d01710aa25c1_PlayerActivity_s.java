 
 package br.com.player;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.MediaController;
 import android.widget.TextView;
 import br.com.logica.ListMusic;
 import br.com.logica.PlayerController;
 
 import com.example.tagedition.R;
  
 public class PlayerActivity extends Activity {
 	
 	private Button btPlay, btPausar, btStop, btPrevious, btNext;
 //	private MediaPlayer Player;
 	private boolean Click;
 //	private int musicIndex = 0;
 	private boolean stopped = false;
 	private boolean paused = false;
 	private TextView nameField;
 	private List<String> musicas;
 	
 	private List<String> convertPath(){
 		List<String> retorno = new ArrayList<String>();
 		List<File> files = ListMusic.getInstance().getListMusic();
 		
 		for (int i = 0; i < files.size(); i++) {
 			retorno.add(files.get(i).getPath());
 		}
 		return retorno;
 	}
 	
 	private MediaPlayer returnPlayer(int index){
 		
 		MediaPlayer player = null;
 		try {
 			player = new MediaPlayer();
 			player.setDataSource(musicas.get(index));
 		} catch (IllegalArgumentException e1) {
 			e1.printStackTrace();
 			System.exit(1);
 		} catch (IllegalStateException e1) {
 			e1.printStackTrace();
 			System.exit(1);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			System.exit(1);
 		}
 		return player;
 		
 	}
 	
 	private void buildListener() {
 		
 		PlayerController.Player.setOnCompletionListener(new OnCompletionListener(){
 
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				
 				if(musicas.size() >  PlayerController.musicIndex + 1){
 					PlayerController.musicIndex++;
 					PlayerController.Player.release();
 					PlayerController.Player = returnPlayer(PlayerController.musicIndex);
 					try {
 						PlayerController.Player.prepare();
 					} catch (IllegalStateException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					PlayerController.Player.start();
 					nameField.setText(getFileNameFromPath(musicas.get(PlayerController.musicIndex)));
 					buildListener();
 				}
 				
 			}
 			
 		});
 		
 	}
 	
 	private String getFileNameFromPath(String path){
 		String[] array = path.split("/");
 		return array[array.length - 1];
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_player);
 		
 		btPlay = (Button) findViewById(R.id.botaoPlay);
 		btPausar = (Button) findViewById(R.id.botaoPause);
 		btStop = (Button) findViewById(R.id.botaoStop);
 		btPrevious = (Button) findViewById(R.id.botaoPrevious);
 		btNext = (Button) findViewById(R.id.botaoNext);
 		nameField = (TextView) findViewById(R.id.textNomeMusica);
 		
 		
 		
 		//Player = MediaPlayer.create(this, R.raw.music);
 		musicas = convertPath();
 		System.out.println("MUSICAA: " + musicas.size() );
 		//musicas.add("/sdcard/09 Blaze of Glory.mp3");
 		//musicas.add("/sdcard/01 Under the bridge.mp3");
 		
 		
 		
 		if(PlayerController.Player != null){
 			nameField.setText(getFileNameFromPath(musicas.get(PlayerController.musicIndex)));
 			
 			try {
 				if(! PlayerController.Player.isPlaying()){
 					PlayerController.Player = new MediaPlayer();
 					
 				}
 			} catch (Exception e) {
 				PlayerController.Player = new MediaPlayer();
 			}
 			
 			
 			
 			
 		}else{
 			PlayerController.Player = new MediaPlayer();
 		}
 		
 		
 		
 		
 		if(musicas.size() > 0 && !PlayerController.Player.isPlaying()){
 			PlayerController.Player = returnPlayer(PlayerController.musicIndex);
 			
 		}
 			
 		
		
		
 		 
 		btPlay.setOnClickListener(new View.OnClickListener() {
 		 
 		@Override
 		public void onClick(View arg0) {
 		paused = false;
 		if(musicas.size() > 0){
 			
 			if(stopped){
 				
 				PlayerController.Player = returnPlayer(PlayerController.musicIndex);
 				stopped = false;
 				paused = false;
 				
 			}
 				
 			
 			if(! PlayerController.Player.isPlaying()){
 				try {
 					PlayerController.Player.prepare();
 				} catch (Exception e) {
 				}	
 				
 				PlayerController.Player.start();
 				nameField.setText(getFileNameFromPath(musicas.get(PlayerController.musicIndex)));
 //				if(paused == false){
 //					buildThread();
 //				}
 				
 				
 				buildListener();	
 				
 				
 			}
 			
 			
 			
 		}
 		
 		 
 		}
 
 
 		});
 		
 		
 		btStop.setOnClickListener(new View.OnClickListener() {
 		
 			@Override
 			public void onClick(View arg0) {
 				
 				
 				if(!stopped){
 					PlayerController.Player.stop();
 					PlayerController.Player.release();
 					stopped = true;
 				}
 				
 			}
 			
 		});
 		
 		btPrevious.setOnClickListener(new View.OnClickListener() {
 		
 			@Override
 			public void onClick(View arg0) {
 				
 				if(musicas.size() > 0){
 					
 					if(stopped == false || paused ==  true){
 						PlayerController.Player.stop();
 						PlayerController.Player.release();
 					}
 					
 					
 					if(PlayerController.musicIndex == 0) {
 						PlayerController.musicIndex = musicas.size() - 1;
 					}else{
 						PlayerController.musicIndex--;
 					}
 					
 					PlayerController.Player = returnPlayer(PlayerController.musicIndex);
 					stopped = false;
 					paused = false;
 					try {
 						PlayerController.Player.prepare();
 					}catch (Exception e) {
 					}
 					
 					PlayerController.Player.start();
 					nameField.setText(getFileNameFromPath(musicas.get(PlayerController.musicIndex)));
 					buildListener();
 				}
 		
 			}
 			
 		});
 		
 		btNext.setOnClickListener(new View.OnClickListener() {
 		
 			@Override
 			public void onClick(View arg0) {
 				
 				if(musicas.size() > 0){
 					
 					if(stopped == false || paused == true){
 						PlayerController.Player.stop();
 						PlayerController.Player.release();
 					}
 					if(PlayerController.musicIndex == musicas.size() - 1) {
 						PlayerController.musicIndex = 0;
 					}else{
 						PlayerController.musicIndex++;
 					}
 					
 					PlayerController.Player = returnPlayer(PlayerController.musicIndex);
 					stopped = false;
 					paused = false;
 					try {
 						PlayerController.Player.prepare();
 					}catch (Exception e) {
 					}
 					
 					PlayerController.Player.start();
 					nameField.setText(getFileNameFromPath(musicas.get(PlayerController.musicIndex)));
 					buildListener();
 					
 				}
 				
 				
 		
 			}
 			
 		});
 		
 		 
 		btPausar.setOnClickListener(new View.OnClickListener() {
 		 
 		@Override
 		public void onClick(View arg0) {
 			
 			
 			if(paused == false && stopped == false){
 				PlayerController.Player.pause();
 				paused = true;
 			}
 			
 		 
 		}
 	});
 	 
 			
 	
 	}
 
  
 }
