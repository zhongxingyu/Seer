 package eu32k.ludumdare.ld26.effects;
 
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.Color;
 
 import eu32k.ludumdare.ld26.events.EventQueue;
 import eu32k.ludumdare.ld26.events.IEvent;
 import eu32k.ludumdare.ld26.events.IEventHandler;
 
 public class EffectsManager implements IEventHandler {
    public final static int SONG_BITBREAK = 1;
    public final static int PART_BITBREAK_INTRO = 0;
    public final static int PART_BITBREAK_BODY = 1;
    public final static int PART_BITBREAK_OUTRO = 2;
 
    public final static String TRACK_BITBREAK_INTRO = "sound/bitbreak_intro.ogg";
    public final static String TRACK_BITBREAK_BODY = "sound/bitbreak_body.ogg";
    public final static String TRACK_BITBREAK_OUTRO = "sound/bitbreak_outro.ogg";
 
    private ColorPulseManager colors;
    private EventQueue events;
    private Music music;
 
    private int currentSong;
    private int currentPart;
 
    public EffectsManager() {
       events = new EventQueue();
       colors = new ColorPulseManager();
       colors.setMainColor(new Color(85 / 255f, 163 / 255f, 215 / 255f, 1f));
       events.addHandler(this);
    }
 
    public EventQueue getEvents() {
       return events;
    }
 
    public void initBitbreak() {
       events.enqueue(new PlayPartEvent(0, SONG_BITBREAK, PART_BITBREAK_INTRO));
    }
 
    public void update(float delta) {
       events.tick(delta);
       if (music != null && music.isPlaying()) {
 
          colors.setTime(music.getPosition());
       }
    }
 
    @Override
    public void handle(IEvent ev) {
       if (ev instanceof PlayPartEvent) {
          handlePlayPartEvent((PlayPartEvent) ev);
       }
    }
 
    private void handlePlayPartEvent(PlayPartEvent ev) {
       this.currentSong = ev.song;
       this.currentPart = ev.part;
       switch (ev.song) {
       default:
          stop();
          break;
       case SONG_BITBREAK:
          playBitbreakPart(ev);
          break;
       }
    }
 
    private void playBitbreakPart(PlayPartEvent ev) {
       switch (ev.part) {
       default:
          stop();
          break;
       case PART_BITBREAK_INTRO:
          colors.init(ColorPulseManager.INTENSITY_EMPTY, ColorPulseManager.INTENSITY_BITBREAK_INTRO);
          play(TRACK_BITBREAK_INTRO, false);
          events.enqueue(new PlayPartEvent(6.1f, SONG_BITBREAK, PART_BITBREAK_BODY));
          colors.setMinSongIntensity(0f);
          break;
       case PART_BITBREAK_BODY:
          colors.init(ColorPulseManager.INTENSITY_BEAT, ColorPulseManager.INTENSITY_BITBREAK_BODY);
          play(TRACK_BITBREAK_BODY, false);
          colors.setMinSongIntensity(0.5f);
          events.enqueue(new PlayPartEvent(72, SONG_BITBREAK, PART_BITBREAK_BODY));
          break;
       case PART_BITBREAK_OUTRO:
         colors.init(ColorPulseManager.INTENSITY_BEAT, ColorPulseManager.INTENSITY_BITBREAK_OUTRO);
          play(TRACK_BITBREAK_OUTRO, false);
          colors.setMinSongIntensity(0f);
          events.enqueue(new PlayPartEvent(72, 0, 0));
          break;
       }
    }
 
    private void play(String track, boolean loop) {
       if (music != null && music.isPlaying()) {
          music.stop();
       }
       music = Gdx.audio.newMusic(Gdx.files.getFileHandle(track, FileType.Internal));
       music.setLooping(loop);
       music.setVolume(0.5f);
       music.play();
 
    }
 
    private void stop() {
       colors.stop();
      colors.init(ColorPulseManager.INTENSITY_EMPTY, ColorPulseManager.INTENSITY_EMPTY);
       music.stop();
       currentSong = 0;
       currentPart = 0;
    }
 
    public Color getCurrentColor() {
       return colors.getCurrentColor();
    }
 
    public void stopSong(Integer nextSong) {
      float barLength = 6;
       float position = music.getPosition() % barLength;
       float timeLeft = barLength - position;
 
       events.enqueue(new PlayPartEvent(timeLeft, currentSong, getOutroPart(currentSong)));
      if (nextSong == null) {
         events.enqueue(new PlayPartEvent(timeLeft + getOutroTime(currentSong), 0, 0));                  
      }
      else
      {
         events.enqueue(new PlayPartEvent(timeLeft + getOutroTime(currentSong), nextSong, 0));         
       }
    }
 
    private int getOutroPart(int song) {
       switch (song) {
       default:
          return -1;
       case SONG_BITBREAK:
          return PART_BITBREAK_OUTRO;
       }
    }
 
    private float getOutroTime(int song) {
       switch (song) {
       default:
          return 0;
       case SONG_BITBREAK:
         return 6;
       }
    }
 
 }
