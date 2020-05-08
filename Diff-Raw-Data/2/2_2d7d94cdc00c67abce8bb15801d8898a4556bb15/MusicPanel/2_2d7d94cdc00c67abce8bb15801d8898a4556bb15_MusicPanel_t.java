 package crescendo.lesson;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.LineBorder;
 
 import crescendo.base.AudioPlayer;
 import crescendo.base.FlowController;
 import crescendo.base.NoteAction;
 import crescendo.base.NoteEvent;
 import crescendo.base.SongPlayer;
 import crescendo.base.SongValidator;
 import crescendo.base.EventDispatcher.EventDispatcher;
 import crescendo.base.song.Note;
 import crescendo.base.song.SongFactory;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.Track;
 import crescendo.sheetmusic.MusicEngine;
 
 public class MusicPanel extends JPanel implements ActionListener,FlowController {
 	private static final long serialVersionUID=1L;
 	
 	private Icon playIcon;
 	private Icon stopIcon;
 	private MusicEngine engine;
 	private JButton actionButton;
 	private JButton previewButton;
 	private JLabel scoreLabel;
 	private SongPlayer player;
 	private LessonGrader grader;
 	private AudioPlayer audio;
 	private MusicItem item;
 	private JComponent module;
 
 	public MusicPanel(MusicItem item,JComponent module) throws IOException {
 		final String PREVIEW_TEXT = "Preview";
 		this.item=item;
 		this.module=module;
 		
 		this.setBackground(Color.WHITE);
 		Font font=new Font(Font.SERIF,Font.BOLD,14);
 		JPanel panel=new JPanel();
 		panel.setBorder(new LineBorder(new Color(0x99,0x99,0x99),1));
 		panel.setBackground(Color.LIGHT_GRAY);
 		panel.setLayout(new GridBagLayout());
 		SongModel model=SongFactory.generateSongFromFile(item.getSource());
 		
 		this.grader=new LessonGrader();
 		this.player=new SongPlayer(model);
 		
 		List<Track> trackList=new ArrayList<Track>();
 		trackList.add(model.getTracks().get(item.getTrack()));
		this.engine=new MusicEngine(model,trackList,false);
 		this.add(this.engine);
 		this.playIcon=new ImageIcon(Toolkit.getDefaultToolkit().createImage("resources/icons/play.png"));
 		this.stopIcon=new ImageIcon(Toolkit.getDefaultToolkit().createImage("resources/icons/stop.png"));
 		this.actionButton=new JButton(playIcon);
 		this.previewButton=new JButton(PREVIEW_TEXT);
 		this.previewButton.setActionCommand("preview");
 		this.scoreLabel=new JLabel("Grade");
 		this.setScoreText();
 		this.scoreLabel.setFont(font);
 		JScrollPane music=new JScrollPane(this.engine,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		music.setPreferredSize(new Dimension(600,400));
 		GridBagConstraints c=new GridBagConstraints();
 		c.fill=GridBagConstraints.VERTICAL;
 		c.weightx=1;
 		c.anchor=GridBagConstraints.WEST;
 		c.ipadx=5;
 		c.ipady=5;
 		panel.add(actionButton,c);
 		c.weightx=10;
 		panel.add(previewButton,c);
 		c.weightx=1;
 		c.anchor=GridBagConstraints.CENTER;
 		JLabel titleLabel=new JLabel(model.getTitle());
 		titleLabel.setFont(font);
 		panel.add(titleLabel,c);
 		c.gridwidth=GridBagConstraints.REMAINDER;
 		c.anchor=GridBagConstraints.EAST;
 		panel.add(this.scoreLabel,c);
 		c.weightx=0;
 		panel.add(music,c);
 		this.add(panel);
 		
 		List<Track> activeTracks = new ArrayList<Track>();
 		activeTracks.add(model.getTracks().get(item.getTrack()));
 		SongValidator validator=new SongValidator(model,activeTracks,item.getHeuristics());
 		this.player.attach(validator,100);
 		List<Track> inactiveTracks=new LinkedList<Track>();
 		Track activeTrack=model.getTracks().get(item.getTrack());
 		for (Track track : model.getTracks())
 		{
 			if (track!=activeTrack)
 			{
 				inactiveTracks.add(track);
 			}
 		}
 		this.audio=new AudioPlayer(model,inactiveTracks);
 		this.player.attach(this.audio,(int)this.audio.getLatency());
 		this.player.attach(this);
 		validator.attach(this.grader);
 		validator.attach(this.engine);
 		EventDispatcher.getInstance().attach(validator);
 		
 		this.actionButton.addActionListener(this);
 		this.previewButton.addActionListener(this);
 	}
 	
 	private void setScoreText()
 	{
 		LessonGrade grade=this.item.getLessonData().getGrade(this.item.getCode());
 		if (!grade.isComplete())
 		{
 			this.scoreLabel.setText("Not yet performed");
 		}
 		else
 		{
 			double roundedScore=Math.round(grade.getGrade()*10)/10;
 			String letter=this.item.getScale().getGrade(roundedScore).label;
 			this.scoreLabel.setText("Highest grade: "+letter+" ("+roundedScore+"%)");
 		}
 		this.updateUI();
 		this.module.updateUI();
 	}
 	
 	private void playIntro(){
 		Note n = new Note(60, 1, 90, this.audio.getMetronomeTrack());
 		NoteEvent bne = new NoteEvent(n, NoteAction.BEGIN,0 );
 		NoteEvent ene = new NoteEvent(n, NoteAction.BEGIN,0 );
 		for(int i=0;i<this.player.getSongState().getTimeSignature().getBeatsPerMeasure();i++){
 			try {
 				this.audio.handleNoteEvent(bne);
 				Thread.sleep(10);
 				this.audio.handleNoteEvent(ene);
 				System.out.println();
 				Thread.sleep((int)1000/(this.player.getSongState().getBPM()/60));
 			} catch (InterruptedException e) {}
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("preview")){
 			//Preview the music snippet before practicing it
 			AudioPlayer temporaryAudio = new AudioPlayer(this.player.getSong(), null);
 			SongPlayer temporaryPlayer = new SongPlayer(this.player.getSong());
 			temporaryPlayer.attach(temporaryAudio,(int)temporaryAudio.getLatency());
 			temporaryPlayer.play();
 		} else if (this.actionButton.getIcon()==this.playIcon) {
 			this.playIntro();
 			this.grader.reset();
 			this.engine.play();
 			this.player.play();
 			this.actionButton.setIcon(this.stopIcon);
 		} else {
 			this.engine.stop();
 			this.player.stop();
 			this.actionButton.setIcon(this.playIcon);
 		}
 	}
 	
 	public void songEnd()
 	{
 		this.player.stop();
 		this.engine.stop();
 		double score=this.grader.getScore();
 		this.item.getLessonData().getGrade(this.item.getCode()).setGrade(score);
 		this.setScoreText();
 		System.out.println("Set score: "+score);
 	}
 	
 	public void pause() {}
 	public void resume() {}
 	public void stop() {}
 	public void suspend() {}
 }
