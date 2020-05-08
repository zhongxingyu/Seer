 package eu32k.vJoy.curveEditor.audio;
 
 import com.badlogic.gdx.audio.io.Mpg123Decoder;
 import com.badlogic.gdx.files.FileHandle;
 
 public class AudioTrack {
    private int rate;
    private boolean mono;
 
    private short[] all;
    private short[] channel1;
    private short[] channel2;
    private short[] combined;
 
    public AudioTrack(FileHandle fileHandle) {
       Mpg123Decoder decoder = new Mpg123Decoder(fileHandle);
       rate = decoder.getRate();
       mono = decoder.getChannels() == 1;
       all = decoder.readAllSamples();
       decoder.dispose();
 
       if (mono) {
          channel1 = all;
          channel2 = all;
          combined = all;
       } else {
          channel1 = new short[all.length / 2];
          channel2 = new short[all.length / 2];
          combined = new short[all.length / 2];
       }
 
       for (int i = 0; i < channel1.length; i++) {
          short c1 = all[i * 2];
          short c2 = all[i * 2 + 1];
          channel1[i] = c1;
          channel2[i] = c2;
          combined[i] = (short) Math.max(c1, c2);
       }
      System.out.println("size " + combined.length);
 
    }
 
    public boolean isMono() {
       return mono;
    }
 
    public int getRate() {
       return rate;
    }
 
    public short[] getAll() {
       return all;
    }
 
    public short[] getChannel1() {
       return channel1;
    }
 
    public short[] getChannel2() {
       return channel2;
    }
 
    public short[] getCombined() {
       return combined;
    }
 
    public double getLengthInSeconds() {
       return (double) channel1.length / (double) rate;
    }
 
    public static short[] getRange(short[] array, int from, int to, int size) {
       int realSize = to - from;
       short[] range = new short[size];
       System.arraycopy(array, from, range, 0, realSize);
       return range;
    }
 
    public short[] getRange(int from, int to, int size) {
       return getRange(combined, from, to, size);
    }
 
 }
