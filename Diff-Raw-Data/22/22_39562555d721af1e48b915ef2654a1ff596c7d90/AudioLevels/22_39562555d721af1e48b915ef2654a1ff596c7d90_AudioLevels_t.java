 import java.io.IOException;
 import java.io.InputStream;
 import java.io.*;
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.*;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.sound.sampled.DataLine.Info;
 import javax.sound.sampled.FloatControl;
 import javax.swing.text.Position;
 
 /**
  * 
  * <Replace this with a short description of the class.>
  * 
  * @author Manu Bhadoria
  */
 public class AudioLevels {
 
     private File audioFile;
     private FileOutputStream rawFile ;
     private FileInputStream rawFileInput;
     private Position curPosition;
     private final int EXTERNAL_BUFFER_SIZE =  524288 ; // 128Kb
     private AudioInputStream audioInputStream;
     private AudioFormat audioFormat;
     private SourceDataLine dataLine ;
     private int rawBytesCount ;
     private float keyFrameThreshold;
     private int minFramesPerKeyFrame;
 
     enum Position {
 
 	LEFT, RIGHT, NORMAL
     };
 
     /**
      * CONSTRUCTOR
      */
     //public PlaySound(InputStream waveStream) {
     public AudioLevels(String audioFileName) {
 	this.audioFile = new File(audioFileName);
 	this.rawBytesCount = 0 ;
	this.keyFrameThreshold = (float)0.08 ;
 	this.minFramesPerKeyFrame = 24*5 ;  // 5 sec of video
 	curPosition = Position.NORMAL;
 
 	try {
 	    this.rawFile = new FileOutputStream("tempRaw.wav");
 	    this.rawFileInput = new FileInputStream("tempRaw.wav") ;
 	} catch (Exception e) {
 	    System.out.println(e.getMessage()) ;
 	}
 
 	this.audioInputStream = null;
 	try {
 	    audioInputStream = AudioSystem.getAudioInputStream(this.audioFile);
 	} catch (UnsupportedAudioFileException e1) {
 	    try {
 		throw new PlayWaveException(e1);
 	    } catch (PlayWaveException ex) {
 		Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 	    }
 	} catch (IOException e1) {
 	    try {
 		throw new PlayWaveException(e1);
 	    } catch (PlayWaveException ex) {
 		Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 	    }
 	}
 
 	// Obtain the information about the AudioInputStream
 	this.audioFormat = audioInputStream.getFormat();
 //	System.out.println(audioFormat.getFrameSize() + " ,frame rate " + audioFormat.getFrameRate()) ;
 	Info info = new Info(SourceDataLine.class, audioFormat);
     }
 
     public int getVideoFrameNo(int audioFrameNo){
 	float timeSeek = (float)(audioFrameNo / audioFormat.getFrameRate()) ;
 	return (int)timeSeek*24 ;
     }
 
     public int getAudioFrameNo(int videoFrameNo){
 	float timeSeek = (float)(videoFrameNo / 24 ) ;
 	return (int)(timeSeek*audioFormat.getFrameRate()) ;
     }
 
     public void writeToRawFile(int audioFrameNo, float time){
 	int noOfFrame = (int)(time * audioFormat.getFrameRate()) ;
 	int initialByte = audioFrameNo*audioFormat.getFrameSize() ;
 	int finalByte = initialByte + noOfFrame*audioFormat.getFrameSize() ;
 	rawBytesCount += finalByte - initialByte ;
 	int readBytes = 0;
 	byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
 	AudioInputStream stream ;
 	int tempBytes = finalByte - initialByte ;
 	stream = null ;
 	try {
 	    stream = AudioSystem.getAudioInputStream(this.audioFile);
 	} catch (UnsupportedAudioFileException e1) {
 	    try {
 		throw new PlayWaveException(e1);
 	    } catch (PlayWaveException ex) {
 		Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 	    }
 	} catch (IOException e1) {
 	    try {
 		throw new PlayWaveException(e1);
 	    } catch (PlayWaveException ex) {
 		Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 	    }
 	}
 
 	try {
 	    stream.skip(initialByte) ;
 	    while (tempBytes > 0) {
 		if (tempBytes > this.EXTERNAL_BUFFER_SIZE){
 		    readBytes = stream.read(audioBuffer, 0, audioBuffer.length);
 		    if (readBytes >= 0) {
 //			System.out.println(readBytes) ;
 			rawFile.write(audioBuffer, 0, this.EXTERNAL_BUFFER_SIZE) ;
 		    }
 		    tempBytes = tempBytes - this.EXTERNAL_BUFFER_SIZE ;
 		}
 		else{
 		    readBytes = stream.read(audioBuffer, 0, tempBytes);
 		    if (readBytes >= 0) {
 //			System.out.println(readBytes) ;
 //			System.out.println(audioBuffer[100]) ;
 			rawFile.write(audioBuffer,0,tempBytes) ;
 		    }
 		    tempBytes = 0 ;
 		}
 	    }
 	} catch (IOException e1) {
 	    try {
 		throw new PlayWaveException(e1);
 	    } catch (PlayWaveException ex) {
 		Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 	    }
 	} 
     }
 
     public void writeToWavFile(){
 	try{
 	    rawFile.flush() ;
 	    rawFile.close() ;
 	} catch (IOException e){
 	    System.out.println("Exception") ;
 	}
 	AudioInputStream stream = new AudioInputStream(rawFileInput, audioFormat, rawBytesCount/audioFormat.getFrameSize()) ;
 	try{
 	    AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File("out.wav"));
 	} catch(Exception e){
 	}
     }
 
     public int addKeyInList(LinkedHashMap<Integer, Integer> lh, int key){
 	boolean keyFound = false ;
 	for (Map.Entry<Integer, Integer> entry : lh.entrySet()) {
 	    if(entry.getKey() < key){
 		if(entry.getValue() + entry.getKey() > key){
 		    keyFound = true ;
 		    if(key + this.minFramesPerKeyFrame > entry.getKey() + entry.getValue()){
 			int newVal = this.minFramesPerKeyFrame - (entry.getKey()  - key) ;
 			int oldVal = entry.getValue() ;
 			lh.put(entry.getKey(), newVal ) ;
 //			System.out.println("Old key: " + entry.getKey() + " new Val: " + entry.getValue() + " key: " + key) ;
 			// RETUN THE NUMBER OF FRAMES ADDED
 			return (newVal - oldVal) ;
 		    }
 		    break ;
 		} 
 	    }
 	}
 	if(!keyFound){
 	    lh.put(key, this.minFramesPerKeyFrame) ;
 	    return this.minFramesPerKeyFrame;
 	}
 	return 0 ;
     }
 
     public void summarize(int percentage){
 	// Find the frames(based on percentage) needed in the summary
 	long totalFrames = videoToShots.numFrames * percentage / 100 ;
 	// Sort the shots based on weight
 	shotInfo.computeShotWeight() ;
 	shotInfo.sortShotHashMapOnWeight() ;
 	// Sort the key frames based on its weight
 	shotInfo.sortKeyFramesHashMapOnKey() ;
 	LinkedHashMap<Integer, Integer> finalKeyFrames = new LinkedHashMap<Integer, Integer>();
 	System.out.println("total Frames " + totalFrames) ;
 	
 	for (Map.Entry<Integer, shotInfo> entry : videoToShots.shotHashMap.entrySet()) {
 	    LinkedHashMap<Integer, Integer> localKeyFrames = new LinkedHashMap<Integer, Integer>();
 //	    if(totalFrames > 0){
 //		localKeyFrames.put(entry.getKey(), this.minFramesPerKeyFrame) ;
 //		totalFrames-= this.minFramesPerKeyFrame ;
 //	    }
 	    for (Map.Entry<Integer, Integer> keyEntry : entry.getValue().keyFrames.entrySet()) {
 		if(entry.getKey() != keyEntry.getKey() ){
 		    if(totalFrames > 0){
 			int addedFrames = addKeyInList(localKeyFrames, keyEntry.getKey()) ;
 			totalFrames-= addedFrames;
 		    }
 		    else
 			break ;
 		}
 	    } // end of keys for loop
 	    for (Map.Entry<Integer, Integer> entry1 : localKeyFrames.entrySet()) {
 		finalKeyFrames.put(entry1.getKey(), entry1.getValue()) ;
 	    }
 	    if (totalFrames == 0)
 		break ;
 	} // end of shots for loop
 
 	// Sort the final list on key indexes
 	Map<Integer, Integer> treeMap = new TreeMap<Integer, Integer>(finalKeyFrames);
 	finalKeyFrames.clear();
 	finalKeyFrames.putAll(treeMap);
 	for (Map.Entry<Integer, Integer> entry2 : finalKeyFrames.entrySet()) {
 	    System.out.println("Writing " + entry2.getKey() + " ,len " + entry2.getValue()) ;
 	    writeToRawFile(getAudioFrameNo(entry2.getKey()), entry2.getValue()/24 ) ;
 	    shotInfo.writeShotHashMapToFile(entry2.getKey(), entry2.getValue() ) ;
 	}
 	writeToWavFile() ;
     }
 
 
     public void shorten() {
 	int readBytes = 0;
 	byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
 
 	for (Map.Entry<Integer, shotInfo> entry : videoToShots.shotHashMap.entrySet()) {
 	    int videoFN = entry.getKey() ;
 	    int startFrameNo = getAudioFrameNo(videoFN) ;
 	    int lastFrameNo = getAudioFrameNo(videoFN + videoToShots.shotHashMap.get(videoFN).numFrames) ;
 	    AudioInputStream stream ;
 	    stream = null ;
 	    try {
 		stream = AudioSystem.getAudioInputStream(this.audioFile);
 	    } catch (UnsupportedAudioFileException e1) {
 		try {
 		    throw new PlayWaveException(e1);
 		} catch (PlayWaveException ex) {
 		    Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	    } catch (IOException e1) {
 		try {
 		    throw new PlayWaveException(e1);
 		} catch (PlayWaveException ex) {
 		    Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	    }
 
 	    int tempFrameNo = startFrameNo ;
 	    float tempVal = (float)0.0 ;
 	    float meanAudioVal = (float)0.0 ;
 	    try {
 		int tempBytes = (lastFrameNo - startFrameNo) * audioFormat.getFrameSize() ;
 		stream.skip(startFrameNo*audioFormat.getFrameSize()) ;
 		while (tempBytes > 0) {
 		    if (tempBytes > this.EXTERNAL_BUFFER_SIZE){
 			readBytes = stream.read(audioBuffer, 0, audioBuffer.length);
 			tempBytes = tempBytes - this.EXTERNAL_BUFFER_SIZE ;
 		    }
 		    else{
 			readBytes = stream.read(audioBuffer, 0, tempBytes);
 			tempBytes = 0 ;
 		    }
 		    for(int cn = 0 ; cn < readBytes - 1 ; cn+=2) {
 			float audioVal = (float)( ( ( audioBuffer[cn+1] << 8 ) | ( audioBuffer[cn] & 0xff ) ) / 32768.0 ); 
 			meanAudioVal += Math.abs(audioVal) ;
 //			    if( Math.abs(audioVal) > this.keyFrameThreshold){
 //				// Add tempFrameNo as key
 //				videoToShots.shotHashMap.get(videoFN).addKeyToKeyFramesHashMap(getVideoFrameNo(tempFrameNo) ) ;
 //			    }
 			++tempFrameNo;
 			if(tempFrameNo%(audioFormat.getFrameRate()) == 0){
 			    meanAudioVal = (float)meanAudioVal / audioFormat.getFrameRate() ;
 //			    System.out.println(meanAudioVal) ;
 			    if( meanAudioVal > this.keyFrameThreshold){
 				// Add tempFrameNo as key
 				videoToShots.shotHashMap.get(videoFN).addKeyToKeyFramesHashMap(getVideoFrameNo(tempFrameNo)-1 ) ;
 			    }
 			    meanAudioVal = (float)0.0 ;
 			}
 		    }
 		}
 	    } catch (IOException e1) {
 		try {
 		    throw new PlayWaveException(e1);
 		} catch (PlayWaveException ex) {
 		    Logger.getLogger(AudioLevels.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	    } 
 	}
     }
 } 
 
