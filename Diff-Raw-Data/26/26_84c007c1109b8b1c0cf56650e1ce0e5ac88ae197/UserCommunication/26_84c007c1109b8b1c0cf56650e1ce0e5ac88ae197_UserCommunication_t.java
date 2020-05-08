 package com.example.multimodal2;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import multimodal.Constraint;
 import multimodal.RoomFactory;
 import multimodal.schedule.Booking;
 import multimodal.schedule.Room;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Vibrator;
 import android.speech.RecognizerIntent;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.util.Log;
 import android.widget.Toast;
 
 public class UserCommunication {
 	
 	private static final String OUTPUT_TYPE_QUESTION = "http://imi.org/Question";
 	private static final String OUTPUT_TYPE_STATEMENT = "http://imi.org/Statement";
 	private static final String OUTPUT_TYPE_YES_NO_QUESTION = "http://imi.org/YesNoQuestion";
 	private static final String OUTPUT_TYPE_REMINDER = "http://imi.org/Reminder";
 	
 	private static final String MODALITY_SPEECH = "http://imi.org/Speech";	
 	private static final String MODALITY_TACTILE = "http://imi.org/Tactile";
 	private static final String MODALITY_MUSIC = "http://imi.org/Music";
 	private static final String MODALITY_LIGHT = "http://imi.org/Light";
 	private static final String MODALITY_SCREEN = "http://imi.org/Screen";
 	
 	private MainActivity ma;
 	
 	private LinkedList<Room> roomList;
 	public String currentRoom;
 	private boolean confirm;
 	private Booking currentBooking;
 	private UserInputInterpreter currentCommand;
 
 	public UserCommunication(MainActivity ma) {
 		
 		this.ma = ma;
 		
 			
 		roomList= RoomFactory.createRoomsFromRDF(this.ma.rdfModel.getModel());
 	}
 	
 	public void InputFromUser(String text) {
 		Log.d("SpeechRepeatActivity", text);
 		//Toast.makeText(this.ma, "You said: "+text, Toast.LENGTH_LONG).show();
 		//if(this.tts != null) {
 			//this.tts.speak("You said: "+text, TextToSpeech.QUEUE_FLUSH, null);
 		//}
 		if(this.currentCommand == null) {
 			this.currentCommand = new UserInputInterpreter(text, roomList);
 		}
 		if(currentCommand.command == UserInputInterpreter.CommandType.REMINDER) {
 			if(this.currentCommand.time != null){
 				final Date reminderTime = this.currentCommand.time.getExactStartTime();
 				final MainActivity mainActivity = this.ma;
 				mainActivity.runOnUiThread(new Runnable() {
 				    Date remindAtTime = reminderTime;
 					MainActivity activity = mainActivity;
 					@Override
 					public void run() {
 						while(remindAtTime.getTime()<new Date().getTime()){
 							synchronized (this) {
 								try {
 									this.wait(1000, 0);
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								}	
 							}
 						}
 						outputToUser("WAKE UP! THIS IS A REMINDER!", OUTPUT_TYPE_REMINDER);
 					}
 				});
 			}
 		} else
 		if(currentCommand.command == UserInputInterpreter.CommandType.WHEN) {
 		
 
 		} else 
 		if(currentCommand.command == UserInputInterpreter.CommandType.BOOK) {
 			if(this.confirm) {
 				if(text.contains("yes")) {
 					currentBooking.book();
 				}
 				this.confirm = false;
 				this.currentCommand = null;
 				this.currentBooking = null;
 			} else {
 				
 				Room associatedRoom = null;
 				if(currentCommand.associatedRoom != null){
 					associatedRoom = currentCommand.associatedRoom;
 				} else {
 					for(Room room : roomList ) {
 						//TODO constrain rooms based on RDF
 						associatedRoom = room;
 					}
 				}
 
 				Constraint constr = new Constraint();
 				if(currentCommand.time != null){
 					constr.fuzzyTimeConstrain(currentCommand.time);
 				}
 				LinkedList<Booking> possibleBookings = associatedRoom.getPossibleBookings(constr);
 				currentBooking = possibleBookings.getFirst();
 				
 		
 				outputToUser("Do you want to book a meeting in the "+associatedRoom.getSpeechName()+currentBooking.getSpeechStartTime()+"?", OUTPUT_TYPE_YES_NO_QUESTION);	
 				}
 			}
 		}
 
 	
 	public String getModalitiesForRoom(String type) {
 
 		HashMap<String, Integer> modalities = null;	
 		
 		for(Room room : this.roomList ) {
 			if(room.getName().equals(currentRoom)) {
 				modalities = this.ma.rdfModel.getModalityForRoom(room, OUTPUT_TYPE_QUESTION);				
 				break;
 			}
 		}	
 		String modality = null;
 		Integer modalityVal = -1;
 		for(Map.Entry<String, Integer>mod : modalities.entrySet()) {						
 			if(mod.getValue() > modalityVal && !mod.getKey().equals(MODALITY_MUSIC) && !mod.getKey().equals(MODALITY_LIGHT)) {
 				modality = mod.getKey();
 				modalityVal = mod.getValue();
 				Log.d(this.ma.LOG_TAG, "maybe: " + mod.getKey());
 			}
 		}
 		Log.d(this.ma.LOG_TAG, "chosen: " + modality);
 		return modality;
 	}
 	
 	public void setRoom(String room) {
 		this.currentRoom = room;
 	}
 	
 	public void outputToUser(String msg, String type) {
 		String preferredModality = getModalitiesForRoom(type);
 		if(preferredModality.equals(MODALITY_SPEECH)) {
 			if(type == OUTPUT_TYPE_YES_NO_QUESTION) {
 				this.ma.repeatTTS.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
 			        @Override
 			        public void onUtteranceCompleted(String utteranceId) {
 			        	askForUserSpeechInput();
 			            
 			        }
 			    });
 				this.confirm = true;
 			}	
 			outputToUserByVoice(msg);
 		} else if(preferredModality.equals(MODALITY_SCREEN)) {
 			if(type == OUTPUT_TYPE_YES_NO_QUESTION) {
 				//this.ma.setContentView(R.layout.bookingconfirmation);
 				Intent i = new Intent(this.ma, MeetingConfirmation.class);		
 				i.putExtra("booking", currentBooking);
 				this.ma.startActivityForResult(i,3); 
 				//this.ma.startActivity(new Intent("android.intent.action.CONFIRM"));
 				this.confirm = true;
 			}			
 		} else if(preferredModality.equals(MODALITY_TACTILE)){
 			Vibrator v = (Vibrator) this.ma.getSystemService(Context.VIBRATOR_SERVICE);
 			v.vibrate(1000);
 		}
 	}
 	
 	private void outputToUserByVoice(String msg) {		
 		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
 		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
 		this.ma.repeatTTS.speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
 		Toast.makeText(this.ma, msg, Toast.LENGTH_LONG).show();
 	}
 	
 	
 	public void askForUserSpeechInput() {
 		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
 		listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What do you want to do?");
 		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
 		this.ma.startActivityForResult(listenIntent, this.ma.VR_REQUEST);
 	}
 
	public void confirm(boolean b) {
		// TODO Auto-generated method stub
		
	}

 	
 	
 }
