 package userinterface;
 
 import com.xuggle.mediatool.IMediaReader;
 import com.xuggle.mediatool.MediaListenerAdapter;
 import com.xuggle.mediatool.ToolFactory;
 import com.xuggle.mediatool.event.IVideoPictureEvent;
 import com.xuggle.mediatool.event.VideoPictureEvent;
 import com.xuggle.mediatool.event.IAudioSamplesEvent;
 import com.xuggle.xuggler.Global;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IAudioSamples;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IStreamCoder;
 import com.xuggle.xuggler.ICodec;
 import com.xuggle.xuggler.IMetaData;
 import com.xuggle.xuggler.IVideoPicture;
 import com.xuggle.xuggler.IVideoResampler;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ShortBuffer;
 
 import java.awt.Color;
 import java.awt.BorderLayout;
 import java.awt.image.BufferedImage;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.concurrent.TimeUnit;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JSlider;
 import javax.swing.ImageIcon;
 import javax.swing.JToolBar;
 import javax.swing.plaf.metal.MetalSliderUI;
 
 public class LecteurVideo extends JPanel implements ActionListener
 {
 	private static final long serialVersionUID = 5373991180139317820L;
 	private File fichierVideo;
 	private String nom;
 	private JButton boutonLecture;
 	private JButton boutonStop;
 	private JPanel panelDuree;
 	private JLabel labelDureeActuelle;
 	private JLabel labelDureeMax;
 	private JSlider slider;
 	private JSlider sliderVolume;
 	private ImageIcon imageIconPause;
 	private ImageIcon imageIconStop;
 	private ImageIcon imageIconLecture;
 	private ImageComponent mediaPlayerComponent;
 	private IMediaReader reader;
 	private long duration;
 	private double volume; //Entre 0 et 1
 	private int videoStreamIndex;
 	private boolean pause;
 	private boolean stop;
 	private SourceDataLine mLine;
 
 	public LecteurVideo(File fichierVideo)
 	{
 		this.volume = 1d;
 		this.pause = true;
 		this.stop = true;
 		this.fichierVideo = fichierVideo;
 		try
 		{
 			this.nom = fichierVideo.getCanonicalPath();
 		}
 		catch (IOException e)
 		{
			GraphicalUserInterface.popupErreur(e.getMessage(),"Erreur");
 		}
 
 		this.videoStreamIndex = 0;
 		this.reader = ToolFactory.makeReader(this.nom);
  		this.reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
 
 		this.initialiserComposants();
 		this.ouvrirVideo();
 		this.ouvrirAudio();
 		this.ajoutListener();
 	}
 
 	private void initialiserComposants()
 	{
 		this.mediaPlayerComponent = new ImageComponent();
 
 		this.imageIconPause = new ImageIcon("images/Pause.png");
 		this.imageIconStop = new ImageIcon("images/Stop.png");
 		this.imageIconLecture = new ImageIcon("images/Lecture.png");
 
 		this.labelDureeActuelle = new JLabel("00:00:00");
 		this.labelDureeMax = new JLabel("00:00:00");
 
 		this.panelDuree = new JPanel(new BorderLayout());
 		this.panelDuree.add(labelDureeActuelle,BorderLayout.WEST);
 		this.panelDuree.add(labelDureeMax,BorderLayout.EAST);
 
 		this.boutonLecture = new JButton();
 		this.boutonLecture.setToolTipText("Lancer");
 		this.boutonLecture.setIcon(imageIconLecture);
 		this.boutonLecture.addActionListener(this);
 		this.boutonLecture.setEnabled(true);
 
 		this.boutonStop = new JButton();
 		this.boutonStop.setToolTipText("Stoper");
 		this.boutonStop.setIcon(imageIconStop);
 		this.boutonStop.addActionListener(this);
 		this.boutonStop.setEnabled(true);
 
 		this.sliderVolume = new JSlider(JSlider.HORIZONTAL);
 		this.sliderVolume.setPaintTicks(false);
 		this.sliderVolume.setPaintLabels(false);
 		this.sliderVolume.setMinimum(0);
 		this.sliderVolume.setMaximum(100);
 		this.sliderVolume.setValue(100);
 		this.sliderVolume.setToolTipText("Volume");
 		this.sliderVolume.addChangeListener(new ChangeListener()
 				{
 					public void stateChanged(ChangeEvent e)
 					{
 						volume = ((double)sliderVolume.getValue())/100;
 					}
 				});
 
 		this.slider = new JSlider(JSlider.HORIZONTAL);
 		this.slider.setPaintTicks(false);
 		this.slider.setPaintLabels(false);
 		this.slider.setMinimum(0);
 		this.slider.setValue(0);
 		this.slider.setUI(new MetalSliderUI()
 		{
 			protected void scrollDueToClickInTrack(int direction)
 			{
 				//On pourra récup les millisecondes pour l'extraction de cette façon aussi
 				//FIXME : segfault pour le moment
 				/*
 				 *int value = slider.getValue();
 				 *if (slider.getOrientation() == JSlider.HORIZONTAL)
 				 *{
 				 *    value = this.valueForXPosition(slider.getMousePosition().x);
 				 *}
 				 *else if (slider.getOrientation() == JSlider.VERTICAL)
 				 *{
 				 *    value = this.valueForYPosition(slider.getMousePosition().y);
 				 *}
 				 *reader.getContainer().seekKeyFrame(videoStreamIndex,(long) value,0);
 				 *slider.setValue(value);
 				 */
 			}
 		});
 
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		toolBar.add(boutonStop);
 		toolBar.add(boutonLecture);
 		toolBar.add(slider);
 		toolBar.add(panelDuree);
 		toolBar.add(sliderVolume);
 
 		this.setLayout(new BorderLayout());
 		this.add(mediaPlayerComponent, BorderLayout.CENTER);
 		this.add(toolBar, BorderLayout.SOUTH);
 	}
 	private void ouvrirVideo()
 	{
 		this.reader.open();
 		this.duration = reader.getContainer().getDuration()/1000;//C'est en microsecondes, on met en milisecondes
 		this.slider.setMaximum((int)this.duration);
 
 		long duree = duration/1000;
 		int heures = (int) (duree/3600);
 		int minutes = (int) ((duree%3600)/60);
 		int secondes = (int) ((duree%3600)%60);
 		labelDureeMax.setText(String.format("%02d:%02d:%02d", heures, minutes, secondes));
 	}
 	private void ajoutListener()
 	{
 		if(this.reader.isOpen())
 		{
     			MediaListenerAdapter adapter = new MediaListenerAdapter()
     			{
 				private IVideoResampler videoResampler = null;
 				private int width = mediaPlayerComponent.getWidth();
 				private int height = mediaPlayerComponent.getHeight();
       				public void onVideoPicture(IVideoPictureEvent event)
       				{
 					long millisecondes = event.getTimeStamp(TimeUnit.MICROSECONDS)/1000;
 					//videoStreamIndex = event.getStreamIndex();
 					slider.setValue((int) (millisecondes));
 					/*
 					 *IVideoPicture pic = event.getPicture();
 					 *videoResampler = IVideoResampler.make(width, height, pic.getPixelType(), pic.getWidth(),pic.getHeight(), pic.getPixelType());
 					 *if(videoResampler == null)
 					 *{
 					 */
 						mediaPlayerComponent.setImage(event.getImage());
 					/*
 					 *}
 					 *else
 					 *{
 					 *        IVideoPicture out = IVideoPicture.make(pic.getPixelType(), width, height);
 					 *        videoResampler.resample(out, pic);
 					 *        IVideoPictureEvent asc = new VideoPictureEvent(event.getSource(), out, event.getStreamIndex());
 					 *        mediaPlayerComponent.setImage(asc.getImage());
 					 *}
 					 */
 					long duree = millisecondes/1000;
 					int heures = (int) (duree/3600);
 					int minutes = (int) ((duree%3600)/60);
 					int secondes = (int) ((duree%3600)%60);
 					labelDureeActuelle.setText(String.format("%02d:%02d:%02d", heures, minutes, secondes));
       				}
 				public void onAudioSamples(IAudioSamplesEvent event)
 				{
 					IAudioSamples samples = event.getAudioSamples();
 					ShortBuffer buffer = samples.getByteBuffer().asShortBuffer();
 					for (int i = 0; i < buffer.limit(); ++i)
 						buffer.put(i, (short)(buffer.get(i) * volume));
 					byte[] rawBytes = samples.getData().getByteArray(0, samples.getSize());
 					mLine.write(rawBytes, 0, samples.getSize());
 				}
     			};
     			this.reader.addListener(adapter);
 		}
 	}
 	private void ouvrirAudio()
 	{
 		if(this.reader.isOpen())
 		{
 			IContainer container = reader.getContainer();
 			IStreamCoder audioCoder = null;
 			int numStreams = container.getNumStreams();
 			int audioStreamId = -1;
 			for(int i = 0; i < numStreams; i++)
 			{
 				IStream stream = container.getStream(i);
 				IStreamCoder coder = stream.getStreamCoder();
 				if(coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
 				{
 					audioStreamId = i;
 					audioCoder = coder;
 					break;
 				}
 			}
 			if(audioStreamId == -1)
 			{
 				throw new RuntimeException("could not find audio stream");
 			}
 			IMetaData options = IMetaData.make();
 			IMetaData unsetOptions = IMetaData.make();
 			if(audioCoder.open(options,unsetOptions) < 0)
 			{
 				throw new RuntimeException("could not open audio decoder");
 			}
 			AudioFormat audioFormat = new AudioFormat(audioCoder.getSampleRate(),
 					(int)IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()),
 					audioCoder.getChannels(),
 					true, /* xuggler defaults to signed 16 bit samples */
 					false);
 			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
 			try
 			{
 				this.mLine = (SourceDataLine) AudioSystem.getLine(info);
 				this.mLine.open(audioFormat);
 				this.mLine.start();
 			}
 			catch(LineUnavailableException e)
 			{
 				throw new RuntimeException("could not open audio line");
 			}
 		}
 	}
 
 	public void play()
 	{
 		this.pause = false;
 		this.stop = false;
 		this.boutonLecture.setIcon(imageIconPause);
 		this.boutonLecture.setToolTipText("Mettre en pause");
 		if(!this.reader.isOpen())
 		{
 			this.ouvrirVideo();
 			this.ouvrirAudio();
 		}
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				while (reader.readPacket() == null && !pause);
 			}
 		}).start();
 	}
 
 	public void pause()
 	{
 		this.pause = true;
 		this.boutonLecture.setIcon(imageIconLecture);
 		this.boutonLecture.setToolTipText("Lancer");
 	}
 
 	public void stop()
 	{
 		this.stop = true;
 		this.pause = true;
 		if(this.reader.isOpen())
 		{
 			this.reader.close();
 			this.mLine.close();
 		}
 		this.slider.setValue(0);
 		this.labelDureeActuelle.setText("00:00:00");
 		this.labelDureeMax.setText("00:00:00");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event)
 	{
 		if(event.getSource() == boutonLecture)
 		{
 			if(this.pause == true)
 			{
 				this.play();
 			}
 			else
 			{
 				this.pause();
 			}
 		}
 		else if(event.getSource() == boutonStop)
 		{
 			if(!this.stop)
 			{
 				if(this.pause == true)
 				{
 					this.boutonLecture.setIcon(imageIconPause);
 					this.boutonLecture.setToolTipText("Mettre en pause");
 				}
 				else
 				{
 					this.boutonLecture.setIcon(imageIconLecture);
 					this.boutonLecture.setToolTipText("Lancer");
 				}
 				this.stop();
 				mediaPlayerComponent.setImage(null);
 				mediaPlayerComponent.repaint();
 			}
 		}
 	}
 }
