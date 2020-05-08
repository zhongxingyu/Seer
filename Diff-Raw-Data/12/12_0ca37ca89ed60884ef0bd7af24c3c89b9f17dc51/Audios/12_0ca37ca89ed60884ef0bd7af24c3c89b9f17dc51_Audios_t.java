 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.sound.sampled.AudioInputStream;
 
 //Returns a HashMap with 1)audios path and 2)booelan: whether must be repeated or not 
 public class Audios {
 	// battle_theme.wav
 	//click_meny_buttons.wav
 	//corona_h_grammata.wav
 	//dice_roll.wav
 	//swordedited.wav
 	//toxo(2).wav
 	private ArrayList<AudiosPair> audiosList;
 
 	public Audios(){
		audiosList = new ArrayList<AudiosPair>(); //CREATING ARRAYLIST
 		
 		audiosList.add(new AudiosPair("battle_theme.wav", true));
 		audiosList.add(new AudiosPair("click_meny_buttons.wav", false));
 		audiosList.add(new AudiosPair("corona_h_grammata.wav", true));
 		audiosList.add(new AudiosPair("dice_roll.wav", false));
 		audiosList.add(new AudiosPair("swordedited.wav", false));
 		audiosList.add(new AudiosPair("toxo(2).wav", false));
 		
 	}
 
 	public ArrayList<AudiosPair> getAudios() {
 		return audiosList;
 	}
 
 	public void setAudios(ArrayList<AudiosPair> audios) {
 		this.audiosList = audios;
 	}
 
 
 }
