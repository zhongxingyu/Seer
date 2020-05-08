 package eu32k.ludumdare.ld26.effects;
 
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.Color;
 
import eu32k.libgdx.common.Assets;
 import eu32k.ludumdare.ld26.events.IEvent;
 import eu32k.ludumdare.ld26.events.IEventHandler;
 import eu32k.ludumdare.ld26.events.messages.GenericEvent;
 import eu32k.ludumdare.ld26.state.GlobalState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class EffectsManager implements IEventHandler {
    public final static int SONG_BITBREAK = 1;
    public static final int SONG_OTGY = 2;
 
    public final static int PART_BITBREAK_INTRO = 0;
    public final static int PART_BITBREAK_BODY = 1;
    public final static int PART_BITBREAK_OUTRO = 2;
 
    public final static String TRACK_BITBREAK_INTRO = "sound/bitbreak_intro.ogg";
    public final static String TRACK_BITBREAK_BODY = "sound/bitbreak_body.ogg";
    public final static String TRACK_BITBREAK_OUTRO = "sound/bitbreak_outro.ogg";
 
    public static final int PART_OTGY_INTRO = 0;
    public static final int PART_OTGY_BODY = 1;
    public static final int PART_OTGY_OUTRO = 2;
 
    public final static String TRACK_OTGY_INTRO = "sound/otgy_intro.ogg";
    public final static String TRACK_OTGY_BODY = "sound/otgy_body.ogg";
    public final static String TRACK_OTGY_OUTRO = "sound/otgy_outro.ogg";
 
    private ColorPulseManager colors;
    private Music music;
 
    private int currentSong;
    private int currentPart;
 
    private GlobalState state;
 
    private float musicVolume = 0.5f;
 
    public EffectsManager() {
       colors = new ColorPulseManager();
       addColor(12, 255, 0); // GREEN
       addColor(99, 255, 174); // Clear BLUE
       addColor(20, 110, 255); // BLUE
       addColor(67, 25, 255); // Dark BLUE
       addColor(211, 20, 255); // VIOLET
       addColor(255, 15, 83); // Light VIOLET
       addColor(255, 21, 0); // Red
       addColor(255, 153, 0); // Orange
       colors.setColorIntensity(ColorPulseManager.INTENSITY_COLOR_01);
       state = StateMachine.instance().getState(GlobalState.class);
       state.getEvents().addHandler(this);
    }
 
    public void addColor(int r, int g, int b) {
       addColor(r, g, b, 255);
    }
 
    public void addColor(int r, int g, int b, int a) {
       colors.addColor(new Color(r / 255f, g / 255f, b / 255f, a / 255f));
    }
 
    public void initBitbreak(int delay) {
       state.getEvents().enqueue(state.pool().events().playPartEvent(1, SONG_BITBREAK, PART_BITBREAK_INTRO));
    }
 
    public void initOtgy(int delay) {
       state.getEvents().enqueue(state.pool().events().playPartEvent(1, SONG_OTGY, PART_OTGY_INTRO));
    }
 
    public void update(float delta) {
       if (music != null && music.isPlaying()) {
          colors.update(delta);
       }
    }
 
    @Override
    public void handle(IEvent ev) {
       if (ev instanceof GenericEvent) {
          GenericEvent event = (GenericEvent) ev;
          switch (event.type) {
          case MUSICEVENT:
             boolean playMusic = event.booleanA;
             if (playMusic) {
                musicVolume = 0.5f;
             } else {
                musicVolume = 0.0f;
             }
             if (music != null) {
                music.setVolume(musicVolume);
             }
             break;
          case PLAY_PART:
             handlePlayPartEvent(event);
             break;
          }
       }
    }
 
    private void handlePlayPartEvent(GenericEvent ev) {
       currentSong = ev.intA;
       currentPart = ev.intB;
       switch (ev.intA) {
       default:
          stop();
          break;
       case SONG_BITBREAK:
          playBitbreakPart(ev);
          break;
       case SONG_OTGY:
          playOtgyPart(ev);
          break;
       }
    }
 
    private void playBitbreakPart(GenericEvent ev) {
       switch (ev.intB) {
       default:
          stop();
          break;
       case PART_BITBREAK_INTRO:
          colors.setBeatIntensity(ColorPulseManager.INTENSITY_EMPTY);
          colors.setSongIntensity(ColorPulseManager.INTENSITY_BITBREAK_INTRO);
          colors.init();
          play(TRACK_BITBREAK_INTRO, false);
          state.getEvents().enqueue(state.pool().events().playPartEvent(6f, SONG_BITBREAK, PART_BITBREAK_BODY));
          colors.setMinSongIntensity(0f);
          break;
       case PART_BITBREAK_BODY:
          colors.setBeatIntensity(ColorPulseManager.INTENSITY_BEAT);
          colors.setSongIntensity(ColorPulseManager.INTENSITY_BITBREAK_BODY);
          colors.init();
          play(TRACK_BITBREAK_BODY, false);
          colors.setMinSongIntensity(0.5f);
          state.getEvents().enqueue(state.pool().events().playPartEvent(72, SONG_BITBREAK, PART_BITBREAK_BODY));
          break;
       case PART_BITBREAK_OUTRO:
          colors.setBeatIntensity(ColorPulseManager.INTENSITY_BEAT);
          colors.setSongIntensity(ColorPulseManager.INTENSITY_BITBREAK_OUTRO);
          colors.init();
          play(TRACK_BITBREAK_OUTRO, false);
          colors.setMinSongIntensity(0f);
          break;
       }
    }
 
    private void playOtgyPart(GenericEvent ev) {
       switch (ev.intB) {
       default:
          stop();
          break;
       case PART_OTGY_INTRO:
          colors.setBeatIntensity(ColorPulseManager.INTENSITY_EMPTY);
          colors.setSongIntensity(ColorPulseManager.INTENSITY_OTGY_INTRO);
          colors.init();
          play(TRACK_OTGY_INTRO, false);
          state.getEvents().enqueue(state.pool().events().playPartEvent(12f, SONG_OTGY, PART_OTGY_BODY));
          colors.setMinSongIntensity(0f);
          break;
       case PART_OTGY_BODY:
          colors.setBeatIntensity(ColorPulseManager.INTENSISTY_OTGY_BEAT_BODY);
          colors.setSongIntensity(ColorPulseManager.INTENSITY_FULL);
          colors.init();
          play(TRACK_OTGY_BODY, false);
          colors.setMinSongIntensity(0.5f);
          state.getEvents().enqueue(state.pool().events().playPartEvent(144f, SONG_OTGY, PART_OTGY_BODY));
          break;
       case PART_OTGY_OUTRO:
          colors.setBeatIntensity(ColorPulseManager.INTENSITY_BEAT);
          colors.setSongIntensity(ColorPulseManager.INTENSITY_FULL);
          colors.init();
          play(TRACK_OTGY_OUTRO, false);
          colors.setMinSongIntensity(0f);
          break;
       }
    }
 
    private void play(String track, boolean loop) {
       if (music != null && music.isPlaying()) {
          music.stop();
       }
      music = Assets.MANAGER.get(track, Music.class);
       music.setLooping(loop);
       music.setVolume(musicVolume);
       music.play();
 
    }
 
    private void stop() {
       colors.stop();
       colors.setBeatIntensity(ColorPulseManager.INTENSITY_EMPTY);
       colors.setSongIntensity(ColorPulseManager.INTENSITY_EMPTY);
       colors.init();
 
       if (music != null) {
          music.stop();
       }
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
 
       state.getEvents().enqueue(state.pool().events().playPartEvent(timeLeft, currentSong, getOutroPart(currentSong)));
       if (nextSong == null) {
          state.getEvents().enqueue(state.pool().events().playPartEvent(timeLeft + getOutroTime(currentSong), 0, 0));
       } else {
          state.getEvents().enqueue(state.pool().events().playPartEvent(timeLeft + getOutroTime(currentSong), nextSong, 0));
       }
    }
 
    private int getOutroPart(int song) {
       switch (song) {
       default:
          return -1;
       case SONG_BITBREAK:
          return PART_BITBREAK_OUTRO;
       case SONG_OTGY:
          return PART_OTGY_OUTRO;
       }
    }
 
    private float getOutroTime(int song) {
       switch (song) {
       default:
          return 0;
       case SONG_BITBREAK:
          return 6;
       case SONG_OTGY:
          return 15;
       }
    }
 
 }
