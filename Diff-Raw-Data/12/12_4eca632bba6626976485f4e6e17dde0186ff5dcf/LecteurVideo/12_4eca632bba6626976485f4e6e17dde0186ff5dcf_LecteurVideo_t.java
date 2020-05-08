 package mensonge.userinterface;
 
 import it.sauronsoftware.jave.EncoderException;
 import it.sauronsoftware.jave.InputFormatException;
 
 import java.io.File;
 import java.io.IOException;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JSlider;
 import javax.swing.ImageIcon;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 
 import mensonge.core.Extraction;
 import mensonge.core.BaseDeDonnees.BaseDeDonnees;
 
 import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
 import uk.co.caprica.vlcj.player.MediaPlayer;
 
 public class LecteurVideo extends JPanel implements ActionListener
 {
 	private static final long serialVersionUID = 5373991180139317820L;
 	private JButton boutonLecture;
 	private JButton boutonStop;
 	private JLabel labelDureeActuelle;
 	private JLabel labelDureeMax;
 	private JSlider slider;
 	private JSlider sliderVolume;
 	private ImageIcon imageIconStop;
 	private ImageIcon imageIconLecture;
 	private EmbeddedMediaPlayerComponent vidComp;
 	private JPanel panelDuree;
 	private JButton boutonMarqueur1;
 	private JButton boutonMarqueur2;
 	private JButton boutonExtract;
 	private long timeMarqueur1 = 0;
 	private long timeMarqueur2 = 1;
 	private BaseDeDonnees bdd;
 	private Marqueur t1;
 	private MediaPlayer mediaPlayer;
 	private String pathVideo = "";
 	private JFrame parent;
 
 	public LecteurVideo(final File fichierVideo, final String nom, BaseDeDonnees bdd, JFrame parent)
 	{
 		this.parent = parent;
 		this.bdd = bdd;
 		this.vidComp = new EmbeddedMediaPlayerComponent();
 		this.vidComp.setVisible(true);
 		this.mediaPlayer = this.vidComp.getMediaPlayer();
 
 		initialiserComposants();
 		this.mediaPlayer.addMediaPlayerEventListener(new PlayerEventListener(slider, boutonLecture, labelDureeMax,
 				labelDureeActuelle));
 
 		javax.swing.SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					mediaPlayer.setRepeat(true);
 					mediaPlayer.startMedia(fichierVideo.getCanonicalPath());
 					mediaPlayer.stop();
 					mediaPlayer.setVolume(sliderVolume.getValue());
 				}
 				catch (IOException e)
 				{
 					GraphicalUserInterface.popupErreur(e.getMessage(), "Erreur");
 				}
 			}
 		});
 		pathVideo = nom;
 	}
 
 	private void initialiserComposants()
 	{
 		this.imageIconStop = PlayerEventListener.IMG_ICON_STOP;
 		this.imageIconLecture = PlayerEventListener.IMG_ICON_LECTURE;
 
 		this.labelDureeActuelle = new JLabel("00:00:00");
 		this.labelDureeMax = new JLabel("00:00:00");
 
 		this.boutonLecture = new JButton();
 		this.boutonLecture.setToolTipText("Lancer");
 		this.boutonLecture.setIcon(imageIconLecture);
 		this.boutonLecture.addActionListener(this);
 		this.boutonLecture.setEnabled(true);
 
 		this.boutonMarqueur1 = new JButton();
 		this.boutonMarqueur1.setToolTipText("Placer Marqueur 1");
 		this.boutonMarqueur1.setText("Marqueur 1");
 		this.boutonMarqueur1.addActionListener(this);
 		this.boutonMarqueur1.setEnabled(true);
 
 		this.boutonMarqueur2 = new JButton();
 		this.boutonMarqueur2.setToolTipText("Placer Marqueur 2");
 		this.boutonMarqueur2.setText("Marqueur 2");
 		this.boutonMarqueur2.addActionListener(this);
 		this.boutonMarqueur2.setEnabled(true);
 
 		this.boutonExtract = new JButton();
 		this.boutonExtract.setToolTipText("Ajouter a la bdd");
 		this.boutonExtract.setText("Extraire");
 		this.boutonExtract.addActionListener(this);
 		this.boutonExtract.setEnabled(true);
 
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
 		this.sliderVolume.setValue(50);
 		this.sliderVolume.setToolTipText("Volume");
 		this.sliderVolume.setMinimumSize(new Dimension(150, 30));
 		this.sliderVolume.setMaximumSize(new Dimension(150, 30));
 		this.sliderVolume.setPreferredSize(new Dimension(150, 30));
 		this.sliderVolume.addChangeListener(new ChangeListener()
 		{
 			public void stateChanged(ChangeEvent e)
 			{
 				mediaPlayer.setVolume(sliderVolume.getValue());
 			}
 		});
 
 		this.slider = new JSlider(JSlider.HORIZONTAL);
 		this.slider.setPaintTicks(false);
 		this.slider.setPaintLabels(false);
 		this.slider.setMinimum(0);
 		this.slider.setValue(0);
 		this.slider.setMaximum((int) SliderPositionEventListener.SLIDER_POSITION_MAX);
 
 		SliderPositionEventListener sliderListener = new SliderPositionEventListener(this.slider, this.mediaPlayer);
 		this.slider.addMouseListener(sliderListener);
 		this.slider.addMouseMotionListener(sliderListener);
 
 		this.panelDuree = new JPanel();
 		panelDuree.setLayout(new BoxLayout(panelDuree, BoxLayout.X_AXIS));
 		panelDuree.add(Box.createHorizontalStrut(5));
 		panelDuree.add(labelDureeActuelle, BorderLayout.WEST);
 		panelDuree.add(Box.createHorizontalStrut(5));
 		panelDuree.add(slider, BorderLayout.CENTER);
 		panelDuree.add(Box.createHorizontalStrut(5));
 		panelDuree.add(labelDureeMax, BorderLayout.EAST);
 		panelDuree.add(Box.createHorizontalStrut(5));
 
 		JToolBar toolBar = new JToolBar();
 		toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
 		toolBar.setFloatable(false);
 		toolBar.add(boutonLecture);
 		toolBar.add(boutonStop);
 		toolBar.addSeparator();
 		toolBar.add(boutonMarqueur1);
 		toolBar.add(boutonMarqueur2);
 		toolBar.add(boutonExtract);
 		toolBar.add(Box.createHorizontalGlue());
 		toolBar.add(new JLabel(new ImageIcon("images/Volume.png")));
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(sliderVolume);
 		toolBar.add(Box.createHorizontalStrut(5));
 
 		JPanel panelControls = new JPanel(new GridLayout(2, 1));
 		panelControls.add(panelDuree);
 		panelControls.add(toolBar);
 
 		this.setLayout(new BorderLayout());
 		this.add(vidComp, BorderLayout.CENTER);
 		this.add(panelControls, BorderLayout.SOUTH);
 
 		ActionMap actionMap = this.getActionMap();
 		InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 
 		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space bar");
 		actionMap.put("space bar", new AbstractAction()
 		{
 			private static final long serialVersionUID = -7449791455625215682L;
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (mediaPlayer.isPlaying())
 				{
 					mediaPlayer.pause();
 				}
 				else
 				{
 					mediaPlayer.play();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Ferme le lecteur vidéo proprement en fermant les instances de mediaPlayer
 	 */
 	public void close()
 	{
 		this.mediaPlayer.stop();
 		this.mediaPlayer.release();
 		this.vidComp.release();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event)
 	{
 		if (event.getSource() == boutonLecture)
 		{
 			if (this.mediaPlayer.isPlaying())
 			{
 				this.mediaPlayer.pause();
 			}
 			else
 			{
 				this.mediaPlayer.play();
 			}
 		}
 		else if (event.getSource() == boutonStop)
 		{
 			this.mediaPlayer.stop();
 		}
 		else if (event.getSource() == boutonMarqueur1)
 		{
 			timeMarqueur1 = mediaPlayer.getTime();
 		}
 		else if (event.getSource() == boutonMarqueur2)
 		{
 			timeMarqueur2 = mediaPlayer.getTime();
 		}
 		else if (event.getSource() == boutonExtract)
 		{
 			this.mediaPlayer.pause();
 			Extraction extract = new Extraction();
 			byte[] tabOfByte = null;
 			try
 			{
				tabOfByte = extract.extraireIntervalle(pathVideo, timeMarqueur1, timeMarqueur2);
 			}
 			catch (IllegalArgumentException e)
 			{
 				GraphicalUserInterface.popupErreur("Extraction : "+e.getMessage());
 			}
 			catch (InputFormatException e)
 			{
 				GraphicalUserInterface.popupErreur("Extraction : "+e.getMessage());
 			}
 			catch (IOException e)
 			{
 				GraphicalUserInterface.popupErreur("Extraction : "+e.getMessage());
 			}
 			catch (EncoderException e)
 			{
 				GraphicalUserInterface.popupErreur("Extraction : "+e.getMessage());
 			}
			new DialogueAjouterEnregistrement(parent, "Ajouter enregistrement",
 					true, this.bdd, tabOfByte);
 			this.mediaPlayer.pause();
 		}
 	}
 }
