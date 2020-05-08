 package com.swm.vg;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Handler;
 import android.os.Message;
 import android.speech.SpeechRecognizer;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.swm.vg.data.ActionInfo;
 import com.swm.vg.data.AnimalInfo;
 import com.swm.vg.data.RecognizedData;
 import com.swm.vg.voicetoactions.PatternMatcher2;
 import com.swm.vg.voicetoactions.VoiceRecognizer;
 import com.swm.vg.voicetoactions.VoiceRecognizerListener;
 
 public class RecognitionManager {
 	private VoiceRecognizerListener mRecogListener;
 	private VoiceRecognizerListener mRecogTeachListener;
 	private VoiceRecognizer mRecognizer;
 	private Activity parent;
 	
 	private final RecognizedData mData;
 	private final ArrayList<AnimalInfo> animalList;
 	
 	private final static int MAX_RECOG_TEACH = 5;
 	private final static int MAX_RECOG_NAME = 5;
 	
 	private int nowMode = MODE_NONE;
 
 	private final static int MODE_NONE = 0;
 	private final static int MODE_COMMU = 1;
 	private final static int MODE_TEACH_WAIT = 2;
 	private final static int MODE_TEACH_ACTION = 3;
 	private final static int MODE_TEACH_NAME = 4;
 	
 	private ActionInfo nowTeachAnimalInfo = new ActionInfo();
 	
 	//RECOG METHOD
 	private void analyzeCommunicateResult(String result) {
 		//먼저 동물이름 있는지 검사하자
 /*		ActionInfo action = PatternMatcher.patternMatch(result, animalList);
 		final int extra = -1;
 		
 		Log.d("Analyze recognition", "who="+action.animalId+"/action="+action.actionId);
 //		callbackHandler.sendMessage(Message.obtain(callbackHandler,CALLBACK_RECOG_RESULT,
 //				action.animalId, action.actionId, Integer.valueOf(extra)));
 		callbackOnVoiceRecognitionResult(action.animalId, action.actionId, -1); */
 		
 		ArrayList<ActionInfo> actionList = PatternMatcher2.search2(result, animalList);
 		final int extra = -1;
 		
 		for(ActionInfo action : actionList) {
 			Log.d("Analyze recognition", "who="+action.animalId+"/action="+action.actionId);
 //			callbackHandler.sendMessage(Message.obtain(callbackHandler,CALLBACK_RECOG_RESULT,
 //					action.animalId, action.actionId, Integer.valueOf(extra)));
 			callbackOnVoiceRecognitionResult(action.animalId, action.actionId, extra);
 		}
 	}
 	
 	private void analyzeCommunicateResults(ArrayList<String> results) {
 		ArrayList<ActionInfo> actionList = PatternMatcher2.searchAll(results, animalList);
 		final int extra = -1;
 		
 		for(ActionInfo action : actionList) {
 			Log.d("Analyze recognition", "who="+action.animalId+"/action="+action.actionId);
 			callbackOnVoiceRecognitionResult(action.animalId, action.actionId, extra);
 		}
 	}
 	
 	
 	//WITH COCOS2DX METHOD
 	public static final int NATIVECALL_RECOG_START = 100;
 	public static final int NATIVECALL_RECOG_STOP = 101;
 	public static final int NATIVECALL_TEACH_START = 102;
 	public static final int NATIVECALL_TEACH_STOP = 103;
 	public static final int NATIVECALL_TEACH_SPEECHING = 104;
 	public static final int NATIVECALL_TEACH_NAME = 105;
 	public static final int NATIVECALL_TEACH_CONFIRM = 106;
 	
 	private Handler manageHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case NATIVECALL_RECOG_START:
 				nowMode = MODE_COMMU;
 				mRecognizer = VoiceRecognizer.createVoiceRecognizer(parent, true);
 				mRecognizer.setVoiceRecognizerListener(new RecogListener());
 				mRecognizer.start();
 				break;
 			case NATIVECALL_RECOG_STOP:
 			case NATIVECALL_TEACH_STOP:
 				nowMode = MODE_NONE;
 				mRecognizer.closeVoiceRecognizer();
 				mRecognizer = null;
 				break;
 			case NATIVECALL_TEACH_START:
 				nowMode = MODE_TEACH_WAIT;
 				mRecognizer = VoiceRecognizer.createVoiceRecognizer(parent, false);
 				mRecognizer.setVoiceRecognizerListener(new TeachListener());
 				break;
 			case NATIVECALL_TEACH_SPEECHING:
 				nowMode = MODE_TEACH_ACTION;
 				nowTeachAnimalInfo.set(msg.arg1, msg.arg2);
 				mRecognizer.start();
 				Toast.makeText(parent, "가르칠 말을 말해 주세요.", Toast.LENGTH_SHORT).show();
 				break;
 			case NATIVECALL_TEACH_NAME:
 				nowMode = MODE_TEACH_NAME;
 				nowTeachAnimalInfo.set(msg.arg1, -2);
 				mRecognizer.start();
 				Toast.makeText(parent, "동물의 이름을 말해 주세요.", Toast.LENGTH_SHORT).show();
 				break;
 			case NATIVECALL_TEACH_CONFIRM:
 				boolean isSave = ((Boolean)msg.obj).booleanValue();
 				break;
 			}
 		}
 	};
 	
 	public static final int CALLBACK_RECOG_RESULT = 200;
 	public static final int CALLBACK_RECOG_READY = 201;
 	public static final int CALLBACK_RECOG_IDLE = 202;
 	public static final int CALLBACK_RECOG_VOLUME = 203;
 	public static final int CALLBACK_RECOG_ERROR = 204;
 	public static final int CALLBACK_TEACH_READY = 205;
 	public static final int CALLBACK_TEACH_RESULT = 206;
 	public static final int CALLBACK_TEACH_VOLUME = 207;
 	public static final int CALLBACK_TEST_TEXT = 208;
 	
 	private Handler callbackHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case CALLBACK_RECOG_RESULT:
 //				callbackOnVoiceRecognitionResult(msg.arg1, msg.arg2, -1);
 //				callbackOnVoiceRecognitionResult(msg.arg1, msg.arg2, ((Integer)msg.obj).intValue());
 				break;
 			case CALLBACK_RECOG_READY:
 				callbackOnRecognitionReady();
 				break;
 			case CALLBACK_RECOG_IDLE:
 				callbackOnRecognitionIdle();
 				break;
 			case CALLBACK_RECOG_ERROR:
 				callbackOnRecognitionError(msg.arg1);
 				break;
 			case CALLBACK_TEACH_READY:
 				callbackOnTeachingReady();
 				break;
 			case CALLBACK_TEACH_RESULT:
 				final ArrayList<String> results = (ArrayList<String>)msg.obj;
 				if(results.size()<=0) {
 					callbackOnTeachingResult(-1);
 					callbackSetLabel("No result");
 				} else {
 					if(animalList.size() <= 0) {
 						Toast.makeText(parent, "동물이 없습니다.", Toast.LENGTH_SHORT).show();
 						return;
 					}
 					if(nowMode == MODE_TEACH_ACTION) {
 						Log.d("Teach Action", "who=" + nowTeachAnimalInfo.animalId + "/action=" + nowTeachAnimalInfo.actionId);
 						new AlertDialog.Builder(parent).setTitle("가르치기 음성 인식 완료")
 							.setMessage("인식된 음성으로 액션 명령을 가르치시겠습니까?")
 							.setPositiveButton("예", new OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 									mData.getAnimalInfo(nowTeachAnimalInfo.animalId).addActionVoice(nowTeachAnimalInfo.actionId, results);
 									
 									Toast.makeText(parent, "저장 완료", Toast.LENGTH_SHORT).show();
 								}
 							}).setNegativeButton("아니요", null).show();
 					} else if (nowMode == MODE_TEACH_NAME) {
 						//TODO
 						Log.d("Teach Name", "who=" + nowTeachAnimalInfo.animalId);
 						final String name = mData.getAnimalInfo(nowTeachAnimalInfo.animalId).getName();;
 						new AlertDialog.Builder(parent).setTitle("동물 이름 인식 완료")
 							.setMessage("인식된 음성으로 "+name+"의 이름을 저장하시겠습니까?")
 							.setPositiveButton("예", new OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 									mData.getAnimalInfo(nowTeachAnimalInfo.animalId).addVoiceNames(results);
 									
 									Toast.makeText(parent, "저장 완료", Toast.LENGTH_SHORT).show();
 								}
 							}).setNegativeButton("아니요", null).show();
 					}
 					callbackOnTeachingResult(0);
 					callbackSetLabel(results.get(0));
 				}
 				nowMode = MODE_TEACH_WAIT;
 				break;
 			case CALLBACK_RECOG_VOLUME:
 				
 				callbackOnRecognitionVolumeChanged(msg.arg1);
 				break;
 			case CALLBACK_TEACH_VOLUME:
 				callbackOnTeachingVolumeChanged(msg.arg1);
 				break;
 			case CALLBACK_TEST_TEXT:
 				callbackSetLabel((String)msg.obj);
 				break;
 			}
 		}
 	};
 	
 	
 	//cpp -> java call methods
 	public void startVoiceRecognition() {
 		Log.d("native->java", "startVoiceRecognition");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_RECOG_START, -1, -1));
 	}
 	
 	public void stopVoiceRecognition() {
 		Log.d("native->java", "stopVoiceRecognition");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_RECOG_STOP, -1, -1));
 	}
 	
 	public void startTeachRecogntion() {
 		Log.d("native->java", "startTeachRecogntion");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_TEACH_START, -1, -1));
 	}
 	
 	public void stopTeachRecogntion() {
 		Log.d("native->java", "stopTeachRecogntion");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_TEACH_STOP, -1, -1));
 	}
 	
 	public void teachSpeeching(int who, int action) {
 		Log.d("native->java", "teachSpeeching");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_TEACH_SPEECHING, who, action));
 	}
 	
 	public void teachNameSpeeching(int who) {
 		Log.d("native->java", "teachNameSpeeching");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_TEACH_NAME, who, -2));
 	}
 	
 	public void teachConfirm(boolean isSave) {
 		Log.d("native->java", "teachConfirm");
 		manageHandler.sendMessage(Message.obtain(manageHandler, NATIVECALL_TEACH_CONFIRM, -1, -1,
 			Boolean.valueOf(isSave)));
 	}
 	
 	public int makeAnimal(String name) {
 		Log.d("native->java", "makeAnimal");
 		return mData.addAnimal(name);
 	}
 	
 	public void makeAnimal2(Object name) {
 		Log.d("native->java", "makeAnimal2");
 		mData.addAnimal((String)name);
 	}
 	
 	public void makeAnimal3() {
 		Log.d("native->java", "makeAnimal3");
 	}
 	
 	
 	//java -> cpp call functions
 	private native void callbackOnVoiceRecognitionResult(int who, int action, int extra);
 	private native void callbackOnRecognitionReady();
 	private native void callbackOnRecognitionIdle();
 	private native void callbackOnRecognitionVolumeChanged(int step);
 	private native void callbackOnRecognitionError(int error);
 	private native void callbackOnTeachingReady();
 	private native void callbackOnTeachingResult(int resultCode);
 	private native void callbackOnTeachingVolumeChanged(int step);
 	
 	private native void callbackSetLabel(String str);
 	
 	//method
 	public void resumeRecognitionManager() {
 		if (mRecognizer != null)
 			mRecognizer.resume();
 	}
 
 	public void pauseRecognitionManager() {
 		if (mRecognizer != null)
 			mRecognizer.pause();
 	}
 
 	public void destoryRecognitionManager() {
 		mRecognizer.closeVoiceRecognizer();
 		mRecogListener = null;
 		mRecogTeachListener = null;
 		mRecognizer = null;
 		sharedInstance = null;
 	}
 	
 	private void makeListener() {
 		mRecogListener = null;
 		mRecogTeachListener = null;
 	}
 
 	private RecognitionManager(Activity parent) {
 		this.parent = parent;
 		makeListener();
 		
 		mData = RecognizedData.sharedRecognizedData();
 		mData.loadAnimalList();
 		animalList = mData.getAnimalList();
		mData.loadConjunctions(parent);
 		
 		Log.i("Recognition Manager Init.", "Data Load Done.");
 	}
 	
 	private static RecognitionManager sharedInstance = null;
 	public static RecognitionManager sharedRecognitionManager(Activity parentActivity) {
 		if(sharedInstance == null) {
 			sharedInstance = new RecognitionManager(parentActivity);
 		} else {
 			sharedInstance.parent = parentActivity;
 		}
 		return sharedInstance;
 	}
 	
 	public static Object getNowManager() {
 		return (Object)sharedInstance;
 	}
 	
 	
 	/*** Listener ***/
 	private class RecogListener implements VoiceRecognizerListener {
 		@Override
 		public void onResults(ArrayList<String> results) {
 			if (results.size() <= 0) {
 				Log.d("VoiceRecognitionListener", "onResults - no result");
 //				callbackSetLabel("No result");
 				callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_TEST_TEXT, "No result"));
 			} else {
 				Log.d("VoiceRecognitionListener", "onResults - " + results.get(0));
 				callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_TEST_TEXT, (Object)results.get(0)));
 //				analyzeCommunicateResult(results.get(0));
 				analyzeCommunicateResults(results);
 			}
 		}
 
 		@Override
 		public void onVolumeChanged(int step) {
 //			Log.d("VoiceRecognitionListener", "onVolumeChanged");
 //			callbackOnRecognitionVolumeChanged(step);
 //			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_VOLUME, step, -1));
 		}
 
 		@Override
 		public void onStartRecognition() {
 			Log.d("VoiceRecognitionListener", "onStartRecognition");
 		}
 
 		@Override
 		public void onTimeoutRecognition() {
 			Log.d("VoiceRecognitionListener", "onTimeoutRecognition");
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 
 		@Override
 		public void onBeginningOfSppech() {
 			Log.d("VoiceRecognitionListener", "onBeginningOfSppech");
 		}
 
 		@Override
 		public void onReady() {
 			Log.d("VoiceRecognitionListener", "onReady");
 //			callbackOnRecognitionReady();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_READY));
 		}
 
 		@Override
 		public void onEndOfSpeech() {
 			Log.d("VoiceRecognitionListener", "onEndOfSpeech");
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 
 		@Override
 		public void onFinishRecognition() {
 			Log.d("VoiceRecognitionListener", "onFinishRecognition");
 		}
 
 		@Override
 		public void onCancelRecognition() {
 			Log.d("VoiceRecognitionListener", "onCancelRecognition");
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 
 		@Override
 		public void onClosing() {
 			Log.d("VoiceRecognitionListener", "onClosing");
 		}
 
 		@Override
 		public void onError(int error) {
 			showErrorMessage(error);
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 	};
 	
 	private class TeachListener implements VoiceRecognizerListener {
 		@Override
 		public void onResults(ArrayList<String> results) {
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_TEACH_RESULT,
 					(Object)results));
 
 		}
 
 		@Override
 		public void onVolumeChanged(int step) {
 //			Log.d("VoiceRecognitionListener", "onVolumeChanged");
 //			callbackOnRecognitionVolumeChanged(step);
 //			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_VOLUME, step, -1));
 		}
 
 		@Override
 		public void onStartRecognition() {
 			Log.d("VoiceRecognitionListener", "onStartRecognition");
 		}
 
 		@Override
 		public void onTimeoutRecognition() {
 			Log.d("VoiceRecognitionListener", "onTimeoutRecognition");
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 
 		@Override
 		public void onBeginningOfSppech() {
 			Log.d("VoiceRecognitionListener", "onBeginningOfSppech");
 		}
 
 		@Override
 		public void onReady() {
 			Log.d("VoiceRecognitionListener", "onReady");
 //			callbackOnRecognitionReady();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_READY));
 		}
 
 		@Override
 		public void onEndOfSpeech() {
 			Log.d("VoiceRecognitionListener", "onEndOfSpeech");
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 
 		@Override
 		public void onFinishRecognition() {
 			Log.d("VoiceRecognitionListener", "onFinishRecognition");
 		}
 
 		@Override
 		public void onCancelRecognition() {
 			Log.d("VoiceRecognitionListener", "onCancelRecognition");
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 
 		@Override
 		public void onClosing() {
 			Log.d("VoiceRecognitionListener", "onClosing");
 		}
 
 		@Override
 		public void onError(int error) {
 			showErrorMessage(error);
 //			callbackOnRecognitionIdle();
 			callbackHandler.sendMessage(Message.obtain(callbackHandler, CALLBACK_RECOG_IDLE));
 		}
 	};
 	
 	private void showErrorMessage(int error) {
 		String msg = null;
 
 		switch (error) {
 		case SpeechRecognizer.ERROR_AUDIO:
 			msg = "오디오 입력 중 오류가 발생했습니다.";
 			break;
 		case SpeechRecognizer.ERROR_CLIENT:
 			msg = "단말에서 오류가 발생했습니다.";
 			break;
 		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
 			msg = "권한이 없습니다.";
 			break;
 		case SpeechRecognizer.ERROR_NETWORK:
 		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
 			msg = "네트워크 오류가 발생했습니다.";
 			break;
 		case SpeechRecognizer.ERROR_NO_MATCH:
 			msg = "일치하는 항목이 없습니다.";
 			break;
 		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
 			msg = "음성인식 서비스가 과부하 되었습니다.";
 			break;
 		case SpeechRecognizer.ERROR_SERVER:
 			msg = "서버에서 오류가 발생했습니다.";
 			break;
 		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
 			msg = "입력이 없습니다.";
 			break;
 		default:
 			msg = "알 수 없는 에러가 발생했습니다.";
 			break;
 		}
 		
 		Toast.makeText(parent, msg, Toast.LENGTH_SHORT).show();
 	}
 }
