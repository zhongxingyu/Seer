 package com.adagio.io.lilypond;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import org.modelcc.types.IntegerModel;
 
 import com.adagio.events.channels.ChannelIdentifierEvent;
 import com.adagio.events.channels.ChannelInstrumentEvent;
 import com.adagio.events.channels.ChannelRhythmEvent;
 import com.adagio.events.channels.ChannelVolumeEvent;
 import com.adagio.events.chords.ChordEvent;
 import com.adagio.events.definitions.ChorDefinitionEvent;
 import com.adagio.events.definitions.InstrumentDefinitionEvent;
 import com.adagio.events.definitions.RhythmDefinitionEvent;
 import com.adagio.events.definitions.TempoDefinitionEvent;
 import com.adagio.events.statements.DefinedTempoStatementEvent;
 import com.adagio.events.statements.MelodyLyricsEvent;
 import com.adagio.events.statements.MusicPlayStatementEvent;
 import com.adagio.events.statements.RelativeStatementEvent;
 import com.adagio.events.statements.TimeStatementEvent;
 import com.adagio.events.statements.UndefinedTempoStatementEvent;
 import com.adagio.io.MusicPieceWriter;
 import com.adagio.language.MusicPiece;
 import com.adagio.language.bars.Bar;
 import com.adagio.language.bars.MelodyBar;
 import com.adagio.language.bars.MelodyBarComponent;
 import com.adagio.language.bars.chords.Chord;
 import com.adagio.language.bars.chords.ChordIdentifier;
 import com.adagio.language.bars.chords.intervals.Interval;
 import com.adagio.language.channels.ChannelIdentifier;
 import com.adagio.language.figures.Figure;
 import com.adagio.language.instruments.InstrumentIdentifier;
 import com.adagio.language.instruments.LimitedPolyphonicType;
 import com.adagio.language.instruments.MonophonicType;
 import com.adagio.language.musicnotes.AbsoluteMusicNote;
 import com.adagio.language.musicnotes.notealterations.Alteration;
 import com.adagio.language.rhythm.RhythmIdentifier;
 import com.adagio.language.statements.simple.SubVerse;
 import com.adagio.language.statements.simple.Verse;
 import com.adagio.language.tempos.Tempo;
 import com.adagio.language.tempos.TempoIdentifier;
 import com.adagio.language.times.Time;
 import com.adagio.structures.Channel;
 import com.adagio.structures.Duration;
 import com.adagio.structures.Rhythm;
 import com.adagio.structures.instruments.Instrument;
 import com.adagio.structures.instruments.LimitedPolyphonicInstrument;
 import com.adagio.structures.instruments.MonophonicInstrument;
 import com.adagio.structures.instruments.PolyphonicInstrument;
 
 //TODO Ajustar el canal por defecto
 
 public class LilyPondMusicPieceWriter extends MusicPieceWriter {
 
 	//Mode relative	
 	private AbsoluteMusicNote relative;
 
 	//Clef (bass,treble...)
 	private String clef;
 
 	//Time (4/4, 6/8,...)
 	private Time time;
 
 	//Tempo 4=90bpm...
 	private Tempo tempo;
 
 	//Data Base of defined chords
 	private MusicDataBase<ChordIdentifier, List<Interval>> chordsDB;
 
 	//Data base of tempos
 	private MusicDataBase<TempoIdentifier, Tempo>temposDB;
 
 	//Data base of instruments
 	private MusicDataBase<InstrumentIdentifier, Instrument> instrumentsDB;
 
 	//Data base of rhythms
 	private MusicDataBase<RhythmIdentifier, Rhythm> rhythmDB;
 
 	//Data base of channels
 	private ChannelsDataBase channelsDB;
 
 
 	private boolean hasTempoChanged;
 	private boolean hasTimeChanged;
 	private boolean hasClefChanged;
 
 	private int melodyBarNum = 0;
 	private int lyricsNum = 0;
 
 	public static final ChannelIdentifier DEFAULT_CHANNEL_IDENTIFIER = new ChannelIdentifier("defaultChannelIdentifier");
 	public static final ChannelIdentifier SILENCES_PATTERN_CHANNEL_IDENTIFIER = new ChannelIdentifier("silencesPatternChannelIdentifier");
 	public static final RhythmIdentifier DEFAULT_RHYTHM_IDENTIFIER = new RhythmIdentifier("defaultRhythm");
 	private final String MELODY_BAR_NAME_PATTERN = "MelodyBar";
 
 
 	private static final Map<String, String> instrumentsTranslation;
 	static
 	{
 		instrumentsTranslation = new HashMap<String, String>();
 		instrumentsTranslation.put("voice", "lead 6 (voice)");
 		instrumentsTranslation.put("piano", "acoustic grand");
 		instrumentsTranslation.put("brightacoustic", "bright acoustic");
 		instrumentsTranslation.put("electricgrand", "electric grand");
 		instrumentsTranslation.put("electricpiano", "electric piano");
 		instrumentsTranslation.put("musicbox", "music box");
 		instrumentsTranslation.put("tubularbells", "tubullar bells");
 		instrumentsTranslation.put("drawbarorgan", "drawbar organ");
 		instrumentsTranslation.put("percussiveorgan", "percussive organ");
 		instrumentsTranslation.put("rockorgan", "rock organ");
 		instrumentsTranslation.put("reedorgan", "reed organ");
 		instrumentsTranslation.put("orchestralharp", "orchestralharp");
 		instrumentsTranslation.put("harmonicsguitar", "guitar harmonics");
 		instrumentsTranslation.put("acousticguitar", "acoustic guitar (nylon)");
 		instrumentsTranslation.put("acousticguitar2", "acoustic guitar (steel)");
 		instrumentsTranslation.put("guitar", "acoustic guitar (nylon)");
 		instrumentsTranslation.put("guitar2", "acoustic guitar (steel)");
 		instrumentsTranslation.put("electricguitar", "electric guitar (jazz)");
 		instrumentsTranslation.put("electricguitar2", "electric guitar (clean)");
 		instrumentsTranslation.put("electricguitar3", "electric guitar (muted)");
 		instrumentsTranslation.put("acousticbass", "acoustic bass");
 		instrumentsTranslation.put("bass", "acoustic bass");
 		instrumentsTranslation.put("electricbass", "electric bass (finger)");
 		instrumentsTranslation.put("electricbass2", "electric bass (pick)");
 		instrumentsTranslation.put("panflute", "pan flute");
 		instrumentsTranslation.put("altosax", "alto sax");
 		instrumentsTranslation.put("sopranosax", "soprano sax");
 		instrumentsTranslation.put("tenorbass", "tenor bass");
 		instrumentsTranslation.put("baritonebass", "baritone bass");
 	}
 
 	public LilyPondMusicPieceWriter(){
 		relative = new AbsoluteMusicNote(2, "C");
 		clef = "treble";
 		time = new Time(new IntegerModel(4), new Figure(4,0));
 		tempo = new Tempo(new Figure(4,0),90);
 		chordsDB = new MusicDataBase<ChordIdentifier, List<Interval>>();
 		temposDB = new MusicDataBase<TempoIdentifier, Tempo>();
 		instrumentsDB = new MusicDataBase<InstrumentIdentifier, Instrument>();
 		rhythmDB = new MusicDataBase<RhythmIdentifier, Rhythm>();
 		channelsDB = new ChannelsDataBase();
 		hasTempoChanged = hasTimeChanged = hasClefChanged = true;
 
 		//add the default channel
 		channelsDB.addElement(DEFAULT_CHANNEL_IDENTIFIER);
 		//add the pattern channel, disabled
 		channelsDB.addElement(SILENCES_PATTERN_CHANNEL_IDENTIFIER);
 		channelsDB.disable(SILENCES_PATTERN_CHANNEL_IDENTIFIER);
 	}
 
 	public static void writeMusicPiece(MusicPiece m,PrintWriter out){
 		LilyPondMusicPieceWriter writer = new LilyPondMusicPieceWriter();
 		writer.write(m,out);
 	}
 
 	@Override
 	/**
 	 * Makes the constructor's work
 	 */
 	public void write(MusicPiece m, PrintWriter out) {
 
 		String composition = "";
 
 		m.run(this);
 
 		composition = this.translate();
 		//System.out.print(composition);
 		out.print(composition);
 
 	}
 
 	/**
 	 * Translates all the main program
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	private String translate(){
 		String composition = "";
 		Map.Entry e = null;
 		Iterator<Entry<ChannelIdentifier, Channel>> it;
 
 		composition += this.getVersion() + "\n";
 		composition += "\\score {\n";
 		composition += " <<\n";
 
 		it = this.channelsDB.getChannelMap().entrySet().iterator();
 
 		while (it.hasNext()) {
 			e = (Map.Entry) it.next();
 			Channel currentChannel = (Channel) e.getValue();
 			//Only print the channel if has been used.
 			if(currentChannel.isUsed()){
 				composition += "\\new Staff{\n";
 				composition += currentChannel.getMusic();
 				composition += "\n}\n";
 				composition += currentChannel.getLyrics();
 				composition += "\n";
 			}
 		}
 
 		composition += ">> \n";
 		composition += this.getMidiTail();
 		return composition;
 	}
 
 
 
 
 	/**
 	 * Receives and AbsoluteNote and translates it to lilyPond syntax.
 	 * @param aNote Note to be translated
 	 * @return A String that contains the translation
 	 */
 	private String translateAbsoluteMusicNote(AbsoluteMusicNote aNote){
 		String composition = aNote.getBasicNoteNameString().toLowerCase();
 		int intOctave = aNote.getOctave().intValue();
 		Alteration alteration = aNote.getMusicNoteName().getAlteration();
 
 		if(alteration != null){
 			composition += translateAlteration(alteration);
 		}
 
 		if(!aNote.isSilence()){
 			for(int i = 0; i < Math.abs(intOctave); i++){
 				if(intOctave > 0){
 					composition += "'";
 				}
 				else{
 					composition += ",";
 				}
 			}
 		}
 		if(aNote.getDuration() != null){
 			composition += translateFigure(aNote.getDuration().getFigure());
 		}
 		return composition;
 	}
 
 	/**
 	 * Receive an alteration and translates it to LilyPond syntax.
 	 * @param alteration Alteration to be translated
 	 * @return A string with the translation
 	 */
 	private String translateAlteration(Alteration alteration){
 		String composition = "";
 		String value = alteration.getValue();
 
 		if(value.equals("#")){
 			composition = "is";
 		}
 		else if(value.equals("x")){
 			composition = "isis";
 		}
 		else if (value.equals("b")){
 			composition = "es";
 		}
 		else if (value.equals("bb")){
 			composition = "eses";
 		}
 		return composition;
 	}
 
 	public String translateSilence(AbsoluteMusicNote silence){
 		String composition = "";
 
 		if(silence.isSilence()){
 			composition = "s";
 		}
 		else{
 			System.err.println("Error 17: The note is not a silence");
 			System.exit(-17);
 		}
 
 		return composition;
 	}
 
 	public String translateFigure(Figure figure){
 		String composition = "";
 		composition += Integer.toString(figure.getShape().intValue());
 		if(figure.getDots() != null){
 			for(int i = 0; i < figure.getDots().length; i++){
 				composition += ".";
 			}
 		}
 		return composition;
 	}
 
 	public String translateTime(Time time){
 		String composition = "";
 		composition += "\\time ";
 		composition += time.getBeats().intValue();
 		composition += "/";
 		composition += translateFigure(time.getFigure());
 
 		return composition;
 	}
 
 	public String translateClef(String clef){
 		String composition = "\\clef " + clef;
 		return composition;
 	}
 
 
 	public String translateInstrument(Instrument instrument){
 
 		String composition = "\\set Staff.midiInstrument = #\"";
 		String timbre = instrument.getTimbre().getValue();
 		String translation = timbre;
 
 		if(instrumentsTranslation.containsKey(timbre)){
 			translation = instrumentsTranslation.get(timbre);
 		}
 
 		composition += translation;
 		composition += "\"";
 		return composition;
 	}
 
 
 	public String translateVolume(int volume){
 		String composition = "\\set Staff.midiMinimumVolume = #" + 0 + "\n";
 		composition += "\\set Staff.midiMaximumVolume = #" + ((double)volume)/100;
 		return composition;
 	}
 
 	public String translateTempo(Tempo tempo){
 		String composition = "\\tempo ";
 		composition += translateFigure(tempo.getFigure());
 		composition += "=" + this.tempo.getBps().intValue();
 		return composition;
 	}
 
 	/**
 	 * Traslate a voices represented as a list of absolute notes
 	 * @param voice Voice to translate
 	 * @param channel Channel where the voice belongs
 	 * @return a String with the form { note1 note2 ... }
 	 */
 	public String translateVoice(List<AbsoluteMusicNote> voice, Channel channel){
 		String composition = "";
 
 		composition += "{";
 		boolean applied = false;
 		for(AbsoluteMusicNote current: voice){
 			composition += " ";
 			composition += translateAbsoluteMusicNote(current);
 
 			if (!current.isSilence() && !applied && channel.hasVolumeChanged()){
 				composition += "\\mf";
 				applied = true;
 			}
 			if(current.isLigatured()){
 				composition += "~";
 			}
 		}
 		composition += " }";
 
 		return composition;
 	}
 
 	public String translateMelodyBar(MelodyBar mBar, Channel channel){
 		String composition = "";
 		List<MelodyBarComponent> mComponents = new ArrayList<MelodyBarComponent>(Arrays.asList(mBar.getMComponents()));
 		List<MelodyBarComponent> aMComponents = new ArrayList<MelodyBarComponent>();
 		AbsoluteMusicNote aNote;
 		boolean applied = false;

 		for(MelodyBarComponent current: mComponents){
 			aNote = current.getNote().toAbsoluteMusicNote(relative);
 			relative = aNote;
			aMComponents.add(new MelodyBarComponent(aNote, current.getFigure()));
 		}

 		for(MelodyBarComponent current: aMComponents){
 			composition += translateAbsoluteMusicNote((AbsoluteMusicNote)current.getNote());
 			composition += translateFigure(current.getFigure());
 			if (!current.getNote().isSilence() && !applied && channel.hasVolumeChanged()){
 				composition += "\\mf";
 				applied = true;
 			}
 			composition += " ";
 		}
 		return composition;
 	}
 
 	public String translateVerse(Verse verse){
 		String composition = "";
 
 		for(SubVerse current: verse.getSubVerses()){
 			composition += current.toString() + " ";
 		}
 		return composition;
 	}
 
 	/**
 	 * @return String-tail needed to create the midi file
 	 */
 	private String getMidiTail(){
 		String composition = "";
 
 		composition += "\\layout{ }\n";
 		composition += "\\midi {\n";
 		composition += "\\context {\n";
 		composition += "\\Score \n";
 		composition += "tempoWholesPerMinute = #(ly:make-moment 72 2)\n";
 		composition += "}\n";
 		composition += "}\n";
 		composition += "}";
 
 		return composition;
 	}
 
 
 	private String getChannelHeaders(Channel actualChannel){
 		String composition = "";
 		// To format composition...
 		if(this.hasClefChanged 
 				|| this.hasTempoChanged || this.hasTimeChanged
 				|| actualChannel.hasVolumeChanged()
 				|| actualChannel.hasInstrumentChanged())
 		{
 			composition += "\n";
 		}
 
 		// \Tempo...
 		if(this.hasTempoChanged){
 			composition += this.translateTempo(this.tempo) + "\n";
 			this.hasTempoChanged = false;
 		}
 		// \Clef...
 		if(this.hasClefChanged){
 			composition += this.translateClef(clef) + "\n";
 			this.hasClefChanged = false;
 		}
 		// \Time...
 		if(this.hasTimeChanged){
 			composition += this.translateTime(this.time) + "\n";
 			this.hasTimeChanged = false;
 		}
 		// \set midiMaximunVolumen...
 		if(actualChannel.hasVolumeChanged()){
 			composition += this.translateVolume(actualChannel.getVolume()) + "\n";
 		}
 		// \set Staff.midiInstrument...
 		if(actualChannel.hasInstrumentChanged()){
 			composition += this.translateInstrument(actualChannel.getInstrument()) + "\n"; 
 			actualChannel.setInstrumentChanged(false);
 		}
 		return composition;
 	}
 
 	private String getVersion(){
 		return "\\version \"2.16.2\"";
 	}
 
 	/** ----- EVENTS ----- **/
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public void harmonyPlay(MusicPlayStatementEvent e) {
 
 		List<AbsoluteMusicNote> chordDisplayed, chordInstrument;
 		List<List<AbsoluteMusicNote>> listChordsDisplayed, listChordsInstrument, listChordsVoices;
 		Instrument channelInstrument;
 		Rhythm actualRhythm;
 		Channel currentChannel;
 		final int numBarsAdded = 1;
 		Vector<Chord> absoluteChords;
 		Vector<Chord> chords;
 		String composition;
 		Bar [] bars = e.getBars();
 		
 		//Descomentar para hacer que el play tenga mismos relative
 //		AbsoluteMusicNote auxRelative = this.relative.clone();
 
 		for(Bar currentBar: bars){
 			composition = "";
 			absoluteChords = new Vector<Chord>();
 			chords = new Vector<Chord>(Arrays.asList(currentBar.getBarChords()));
 
 			// Translates to absoluteMusicNote
 			for(Chord current: chords){
 				if(chordsDB.exists(current.getIdentifier())){
 					Chord aChord = current.toAbsoluteChord(relative);
 					absoluteChords.add(aChord);
 					if(!aChord.isSilence()){
 						relative = (AbsoluteMusicNote) aChord.getNote().clone();
 					}
 
 				}
 				else{
 					System.err.println("Error 1: The chord identifier \""
 							+ current.getIdentifier().getValue()
 							+ "\" is not defined");
 					System.exit(1);
 				}
 			}
 
 
 
 			//Display the chords
 			AbsoluteMusicNote relativeToDisplay;
 			listChordsDisplayed = new ArrayList<List<AbsoluteMusicNote>>();
 			for(Chord current: absoluteChords){
 				relativeToDisplay = (AbsoluteMusicNote) current.getNote();
 				chordDisplayed = new ArrayList<AbsoluteMusicNote>();
 				chordDisplayed = this.displayChord(current, relativeToDisplay);
 				listChordsDisplayed.add(chordDisplayed);
 			}
 
 			// (Erase this if don't want to reset relative after play)
 			//we recover the relative
 			//this.relative = relativeBeforePlay;
 
 			composition = "";
 
 
 			Map.Entry x = null;
 			Iterator<Entry<ChannelIdentifier, Channel>> it;
 
 			it = this.channelsDB.getChannelMap().entrySet().iterator();
 
 			//For each channel
 			while (it.hasNext()) {
 				x = (Map.Entry) it.next();
 
 
 				currentChannel = ((Channel) x.getValue());
 
 				//If the channel is enabled and is an harmony channel
 				if (currentChannel.isEnable() && currentChannel.isHarmony()) {
 
 					composition += getChannelHeaders(currentChannel);
 
 					//The channel is used
 					currentChannel.setUsed(true);
 
 
 					//Apply the instrument to the notes chords displayed
 					channelInstrument = currentChannel.getInstrument();
 					listChordsInstrument = new ArrayList<List<AbsoluteMusicNote>>();
 					for(List<AbsoluteMusicNote> current: listChordsDisplayed){
 						chordInstrument = new ArrayList<AbsoluteMusicNote>();
 						chordInstrument = channelInstrument.apply(current);
 						listChordsInstrument.add(chordInstrument);
 					}
 
 
 					//Obtains and applies the rhythm
 					actualRhythm = currentChannel.getRhythm();
 					if(actualRhythm == null){
 						actualRhythm = rhythmDB.getElement(DEFAULT_RHYTHM_IDENTIFIER);
 					}
 					listChordsVoices = actualRhythm.apply(listChordsInstrument, channelInstrument, time, relative);
 
 
 					//Translates the voices
 					composition += "<< ";
 					for(List<AbsoluteMusicNote> voice: listChordsVoices){
 						composition += translateVoice(voice, currentChannel) + " \\\\ ";
 					}
 					composition += ">>\n";
 					currentChannel.setVolumeChanged(false);
 
 					//Adds the translation to the channel 
 					this.channelsDB.addMusic((ChannelIdentifier) x.getKey(),composition, numBarsAdded);
 					composition = "";
 
 				}
 			}
 
 			//Fill the other channels until actual max duration
 			this.fillAllChannelsWithSilences();
 		}
 //		this.relative = auxRelative.clone();
 	}
 
 
 	@Override
 	public void melodyPlay(MelodyLyricsEvent e) {
 		String melodyComposition = "";
 		MelodyBar [] mBars = e.getMelody().getMBars();
 		ChannelIdentifier id = e.getMelody().getIdentifier();
 		Channel currentChannel;
 		AbsoluteMusicNote auxRelative = this.relative.clone();
 
 		if(!channelsDB.exists(id)){
 			createChannel(new ChannelIdentifierEvent(this, id));
 			melodyChannel(new ChannelIdentifierEvent(this, id));
 		}
 
 		currentChannel = channelsDB.getElement(id);
 
 		if(currentChannel.isMelody()){
 			channelsDB.getElement(id).setUsed(true);
 
 			melodyComposition += getChannelHeaders(currentChannel);
 
 			for(MelodyBar current: mBars){
 				melodyComposition += translateMelodyBar(current, currentChannel);
 				currentChannel.setVolumeChanged(false);
 			}
 			melodyComposition += "\n";
 			channelsDB.addMusic(id, melodyComposition, mBars.length);
 		}
 		else{
 			System.err.println("Error: Can't write melody in a Harmony channel");
 			System.exit(-33);
 		}
 
 		this.relative = auxRelative.clone();
 	}
 
 	@Override
 	public void melodyLyricsPlay(MelodyLyricsEvent e) {
 		MelodyBar [] mBars = e.getMelody().getMBars();
 		Verse [] verses = e.getLyrics().getVerses();
 		MelodyBar currentMBar = null;
 		Verse currentVerse = null;
 		ChannelIdentifier id = e.getMelody().getIdentifier();
 		Channel currentChannel;
 		AbsoluteMusicNote auxRelative = this.relative.clone();
 
 		String melodyComposition = "";
 		String lyricsComposition = "";
 
 		if(!channelsDB.exists(id)){
 			createChannel(new ChannelIdentifierEvent(this, id));
 			melodyChannel(new ChannelIdentifierEvent(this, id));
 		}
 
 		currentChannel = channelsDB.getElement(id);
 
 		if(currentChannel.isMelody()){		
 
 			melodyComposition += getChannelHeaders(currentChannel);
 			//The channel is used
 			currentChannel.setUsed(true);
 
 			for(int i = 0; i < mBars.length; i++){
 				currentMBar = mBars[i];
 				if(i < verses.length){
 					currentVerse = verses[i];
 				}
 
 				if(currentVerse != null){
 					melodyComposition += "\\new Voice = \""+ MELODY_BAR_NAME_PATTERN + melodyBarNum + "\" {\n";
 					melodyComposition += "\t" + translateMelodyBar(currentMBar, currentChannel);
 					currentChannel.setVolumeChanged(false);
 					melodyComposition += "\n}\n";
 
 					channelsDB.addMusic(id, melodyComposition, 1);
 					melodyComposition = "";
 
 					lyricsComposition += "\\context Lyrics = \"lyrics"+ lyricsNum + "\" {\n"
 							+ "\t" + "\\lyricsto \""+ MELODY_BAR_NAME_PATTERN + melodyBarNum + "\" { ";
 					lyricsComposition += translateVerse(currentVerse);
 					lyricsComposition += " }\n";
 					lyricsComposition += "}\n";
 
 					channelsDB.addLyrics(id, lyricsComposition);
 					lyricsComposition = "";
 					melodyBarNum++;
 					currentVerse = null;
 				}
 				else{
 					melodyComposition += translateMelodyBar(currentMBar, currentChannel) + "\n";
 					currentChannel.setVolumeChanged(false);
 					channelsDB.addMusic(id, melodyComposition, 1);
 				}
 			}
 			this.lyricsNum++;
 		}
 		else{
 			System.err.println("Error: Can't write melody in a Harmony channel");
 			System.exit(11);
 		}
 
 		this.relative = auxRelative.clone();
 	}
 
 
 	/**
 	 * Event that occurs when a channel is going to be created.
 	 * @post DISABLES the default channel.
 	 */
 	@Override
 	public void createChannel(ChannelIdentifierEvent e) {
 		boolean newChannelAdded = !this.channelsDB.exists(e.getId());
 		boolean existsPattern = this.channelsDB.exists(LilyPondMusicPieceWriter.SILENCES_PATTERN_CHANNEL_IDENTIFIER);
 
 		this.channelsDB.addElement(e.getId());
 
 		if(!e.getId().getValue().equals(DEFAULT_CHANNEL_IDENTIFIER.getValue())){
 			this.channelsDB.disable(DEFAULT_CHANNEL_IDENTIFIER);
 		}
 
 		//if the channel added is new in the DB, copy the music and duration
 		// silences pattern
 		if(existsPattern && newChannelAdded){
 			this.channelsDB.getElement(e.getId())
 			.setMusic(this.channelsDB.getChannelMap()
 					.get(LilyPondMusicPieceWriter.SILENCES_PATTERN_CHANNEL_IDENTIFIER)
 					.getMusic());
 			this.channelsDB.getElement(e.getId())
 			.setNumBars(this.channelsDB.getChannelMap()
 					.get(LilyPondMusicPieceWriter.SILENCES_PATTERN_CHANNEL_IDENTIFIER)
 					.getNumBars());
 		}
 	}
 
 	/**
 	 * Fills a channel with silences until reaches the maxDuration of channelsDB
 	 * @param id Identifier of the channel
 	 */
 	private void fillChannelWithSilences(ChannelIdentifier id) {
 		int maxDuration = this.channelsDB.maxNumBars();
 		int auxDuration = 0;
 		int difference = 0;
 		List<AbsoluteMusicNote> silences;
 
 		String composition = "";
 
 		if (this.channelsDB.exists(id)) {
 			auxDuration = this.channelsDB.getElement(id).getNumBars();
 			difference = maxDuration - auxDuration;
 
 			if (difference > 0) {
 				for (int i = difference; i > 0; i--) {
 					silences = Rhythm.genLigaturedNotes(AbsoluteMusicNote.genSilence(), time.duration());
 					for(AbsoluteMusicNote currentSilence: silences){
 						composition += translateAbsoluteMusicNote(currentSilence);
 					}
 				}
 				composition += "\n";
 				this.channelsDB.addMusic(id, composition, difference);
 			}
 		} else {
 			System.err.println("Error 10: Channel \"" + id.toString()
 					+ "\" doesn't exist. " + "Can't be filled with zeros.");
 		}
 	}
 
 	/**
 	 * Fills all the channels with silences until reach the maxDuration
 	 * @param id Identifier of the DB
 	 */
 	@SuppressWarnings("rawtypes")
 	private void fillAllChannelsWithSilences(){
 		Map.Entry e = null;
 		Iterator<Entry<ChannelIdentifier, Channel>> it;
 
 		it = this.channelsDB.getChannelMap().entrySet().iterator();
 		if (it.hasNext()) {
 
 			while (it.hasNext()) {
 				e = (Map.Entry) it.next();
 				this.fillChannelWithSilences((ChannelIdentifier)e.getKey());
 			}
 		}
 	}
 
 	/**
 	 * Event that occurs when a channel is going to be destroyed.
 	 * Destroy the channel, and if is needed, activates the default one.
 	 */
 	@Override
 	public void destroyChannel(ChannelIdentifierEvent e) {
 		this.channelsDB.destroy(e.getId());
 		if(this.channelsDB.isDefaultChannelNeeded() && !e.getId().getValue().equals(DEFAULT_CHANNEL_IDENTIFIER.getValue())){
 			this.channelsDB.enable(DEFAULT_CHANNEL_IDENTIFIER);
 		}
 	}
 
 
 	//	/**
 	//	 * Event that occurs when a translation to AbsoluteMusicNote is needed
 	//	 */
 	//	@Override
 	//	public AbsoluteMusicNote toAbsolute(MusicNoteToAbsoluteEvent e) {
 	//		return e.getNote().toAbsoluteMusicNote(this);
 	//	}
 
 	/**
 	 * Event that looks for a chord in chordsDB
 	 * @return true if is defined. False in other case
 	 */
 	@Override
 	public boolean isChordDefined(ChordEvent e) {
 		return this.chordsDB.exists(e.getChord().getIdentifier());
 
 	}
 
 	/**
 	 * Event that happens when it's needed to know if a channel has existed anytime in DB
 	 * @return true if has existed. False in other case.
 	 */
 	@Override
 	public boolean existsChannel(ChannelIdentifierEvent e) {
 		return this.channelsDB.exists(e.getId());
 	}
 
 	/**
 	 * Event that happens when it's needed to know if a channel has been erased (logically destroyed)
 	 * @return true if is erased. False in other case.
 	 */
 	@Override
 	public boolean isErasedChannel(ChannelIdentifierEvent e) {
 		return this.channelsDB.isErased(e.getId());
 	}
 
 	/**
 	 * Event that happens when it's needed to recover a previously destroyed channel
 	 * Disable the default channel
 	 */
 	@Override
 	public void recoverChannel(ChannelIdentifierEvent e) {
 		this.channelsDB.getElement(e.getId()).setErased(false);
 		if (!e.getId().getValue().equals(DEFAULT_CHANNEL_IDENTIFIER.getValue()) && !channelsDB.isDefaultChannelNeeded()) {
 			this.channelsDB.disable(DEFAULT_CHANNEL_IDENTIFIER);
 		}
 	}
 
 	/**
 	 * Event that happens when it's needed to add a chord in ChordsDB
 	 */
 	@Override
 	public void chordDefinition(ChorDefinitionEvent e) {
 		this.chordsDB.addElement(e.getId(), e.getIntervals());
 	}
 
 	/**
 	 * Event that happens when a channel is going to be enabled. 
 	 * DISABLES the default channel (unless it was the enabled one).
 	 */
 	@Override
 	public void enableChannel(ChannelIdentifierEvent e) {
 		this.channelsDB.enable(e.getId());
 		if (!e.getId().getValue().equals(DEFAULT_CHANNEL_IDENTIFIER.getValue()) && !channelsDB.isDefaultChannelNeeded()) {
 			this.channelsDB.disable(DEFAULT_CHANNEL_IDENTIFIER);
 		}
 	}
 
 	/**
 	 * Event that happens when it's needed to disable a channel.
 	 * Disable the channel, and if is needed, activates the default one.
 	 */
 	@Override
 	public void disableChannel(ChannelIdentifierEvent e) {
 		this.channelsDB.disable(e.getId());
 		if(this.channelsDB.isDefaultChannelNeeded() && !e.getId().getValue().equals(DEFAULT_CHANNEL_IDENTIFIER.getValue())){
 			this.channelsDB.enable(DEFAULT_CHANNEL_IDENTIFIER);
 		}
 	}
 
 
 	@Override
 	public void melodyChannel(ChannelIdentifierEvent e) {
 		ChannelIdentifier id = e.getId();
 		Channel channel = channelsDB.getElement(id);
 		if(channel != null){
 			if(!channel.isUsed()){
 				channel.setMelody();
 				if(channelsDB.isDefaultChannelNeeded()){
 					this.channelsDB.enable(DEFAULT_CHANNEL_IDENTIFIER);
 				}
 			}
 			else{
 				if(channel.isHarmony()){
 					System.err.println("Error: The Channel " + id.toString() 
 							+ " has been used yet as a Harmony Channel. It can't be changed");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void harmonyChannel(ChannelIdentifierEvent e) {
 		ChannelIdentifier id = e.getId();
 		Channel channel = channelsDB.getElement(id);
 		if(channel != null){
 			if(!channel.isUsed()){
 				channel.setHarmony();
 			}
 			else{
 				if(channel.isMelody()){
 					System.err.println("Error: The Channel " + id.toString() 
 							+ " has been used yet as a Melody Channel. It can't be changed");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Event that happens when it's needed change the volume of a channel
 	 */
 	@Override
 	public void setChannelVolume(ChannelVolumeEvent e) {
 		this.channelsDB.setVolume(e.getId(), e.getVolume());
 		this.channelsDB.getElement(e.getId()).setVolumeChanged(true);
 	}
 
 	/**
 	 * Event that happens when it's needed to change the instrument of a channel
 	 */
 	@Override
 	public void setChannelInstrument(ChannelInstrumentEvent e) {
 
 		if(this.instrumentsDB.exists(e.getInstrumentID())){
 			this.channelsDB.setInstrument(e.getId(), this.instrumentsDB.getElement(e.getInstrumentID()));
 			this.channelsDB.getElement(e.getId()).setInstrumentChanged(true);
 		}
 		else{
 			System.err.println("Error 11: The Instrument identifier \""
 					+ e.getInstrumentID().getValue()
 					+ "\" is not defined");
 			System.exit(11);
 		}
 	}
 
 	@Override
 	public void setChannelRhythm(ChannelRhythmEvent e) {
 		ChannelIdentifier channelID = e.getChannelID();
 		RhythmIdentifier rhythmID = e.getRhythmID();
 		Rhythm rhythm;
 
 		if(this.rhythmDB.exists(rhythmID)){
 			rhythm = rhythmDB.getElement(rhythmID);
 			this.channelsDB.setRhythm(channelID, rhythm);
 			this.channelsDB.getElement(channelID).setRhythmChanged(true);
 		}
 		else{
 			System.err.println("Error 11: The Rhythm identifier \""
 					+ rhythmID.getValue()
 					+ "\" is not defined");
 			System.exit(11);
 		}
 
 	}
 
 	/**
 	 * Event that happens when it's needed to change the relative note
 	 */
 	@Override
 	public void setRelative(RelativeStatementEvent e) {
 		this.relative = (e.getaNote());
 	}
 
 	@Override
 	public void setTime(TimeStatementEvent e) {
 		this.time = (e.getTime());
 		this.hasTimeChanged = true;
 	}
 
 	@Override
 	public void setTempo(DefinedTempoStatementEvent e) {
 		if(this.temposDB.exists(e.getIdentifier())){
 			this.tempo = this.temposDB.getElement(e.getIdentifier()).clone();
 		}
 		this.hasTempoChanged = true;
 	}
 
 	@Override
 	public void setTempo(UndefinedTempoStatementEvent e) {
 		this.tempo = e.getTempo().clone();
 		this.hasTempoChanged = true;
 	}	
 
 	/**
 	 * Event that happens when a tempo is defined.
 	 */
 	@Override
 	public void tempoDefinition(TempoDefinitionEvent e) {
 		this.temposDB.addElement(e.getId(), e.getTempo());
 	}
 
 
 	/**
 	 * Event that happens when an instrument is defined.
 	 */
 	@Override
 	public void instrumentDefinition(InstrumentDefinitionEvent e) {
 
 		Instrument instrument;
 
 
 		if(e.getPhType().getClass().equals(MonophonicType.class)){
 			if(e.getRegisters() != null && e.getRegisters().length > 0){
 				instrument = new MonophonicInstrument(e.getTimbre(), e.getRegisters());
 			}
 			else{
 				instrument = new MonophonicInstrument(e.getTimbre());
 			}
 		}
 		else if(e.getPhType().getClass().equals(LimitedPolyphonicType.class)){
 			if(e.getRegisters() != null && e.getRegisters().length > 0){
 				instrument = new LimitedPolyphonicInstrument(e.getTimbre(), e.getRegisters());
 			}
 			else{
 				instrument = new LimitedPolyphonicInstrument(e.getTimbre());
 			}
 		}
 		else{
 			if(e.getRegisters() != null && e.getRegisters().length > 0){
 				instrument = new PolyphonicInstrument(e.getTimbre(), e.getRegisters());
 			}
 			else{
 				instrument = new PolyphonicInstrument(e.getTimbre());
 			}
 		}
 
 		this.instrumentsDB.addElement(e.getIdentifier(), instrument);
 	}
 
 	/**
 	 * Event that is launched when an instrument is defined.
 	 */
 	@Override
 	public void rhythmDefinition(RhythmDefinitionEvent e) {
 		Rhythm rhythm = new Rhythm(e.getComponents());
 		this.rhythmDB.addElement(e.getIdentifier(), rhythm);
 	}
 
 
 
 	/** ----- GETTERS & SETTERS  ----- **/
 
 
 	public MusicDataBase<ChordIdentifier, List<Interval>> getChordsDB() {
 		return chordsDB;
 	}
 
 	public ChannelsDataBase getChannelsDB() {
 		return channelsDB;
 	}
 
 
 	/** ----- OTHERS ----- **/
 
 	/**
 	 * Display the chord, applying all the intervals to the base note.
 	 * @param chord Chord to be displayed
 	 * @return List of AbsoluteMusicNotes that are the result of apply the
 	 * intervals of the chord. List of one silence-AbsoluteMusicNote if
 	 * the the chord is a silence-Chord
 	 */
 	public List<AbsoluteMusicNote> displayChord(Chord chord, AbsoluteMusicNote relativeD){
 
 		List<AbsoluteMusicNote> aNotes = new ArrayList<AbsoluteMusicNote>();
 
 		if(chord.isSilence()){
 			aNotes.add(AbsoluteMusicNote.genSilence(new Duration(new Figure(time.duration()))));
 		}
 		else{
 
 			List<Interval> intervals = this.chordsDB.getElement(chord.getIdentifier());
 			AbsoluteMusicNote bassNote = (AbsoluteMusicNote) chord.getBassNote();
 
 			//Recollects the notes result to apply the interval to the fundamental note
 			for(int i = 0; i < intervals.size();i++){
 
 				aNotes.add(intervals.get(i).apply(chord.getNote(), relativeD));
 				if(chord.getDuration() != null){
 					aNotes.get(i).setDuration(chord.getDuration().clone());
 				}
 			}
 
 			if (bassNote != null) {
 				// If the bass note is higher than any other, reduces its octave
 				for (int i = 0; i < aNotes.size(); i++) {
 					if (bassNote.isHigher(aNotes.get(i))) {
 						bassNote.setOctave(bassNote.getOctave().intValue() - 1);
 					}
 				}
 
 				// If bassNote NoteName is equal than any in the interval, this last
 				// is removed.
 				for (int i = 0; i < aNotes.size(); i++) {
 					if (bassNote.getMusicNoteName().equals(
 							aNotes.get(i).getMusicNoteName())) {
 						aNotes.remove(i);
 					}
 				}
 				aNotes.add(0,bassNote);
 			}
 		}
 		return aNotes;
 	}
 
 }
