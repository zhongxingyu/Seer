 package movieTheater.GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.text.ParseException;
import java.text.SimpleDateFormat;
 import java.util.Map;
 import java.util.concurrent.CountDownLatch;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.MaskFormatter;
 
 import movieTheater.Movie.Actor;
 import movieTheater.Movie.Cast;
 import movieTheater.Movie.Director;
 import movieTheater.Movie.Genre;
 import movieTheater.Movie.Movie;
 import movieTheater.main.MovieController;
 
 /**
  * GUI used to create, show or edit a movie. 
  * @author Henrik
  *
  */
 @SuppressWarnings("serial")
 public class CreateMovie extends JFrame implements WindowListener{
 
 	private JPanel contentPane;
 	private JPanel panel;
 	private JTextField tfTitel;
 	private JTextField tfOriginalTitel;
 	private JLabel lblTitel;
 	private JLabel lblOriginalTitel;
 	private JLabel lblInstruktr;
 	private JLabel lblPrmiere;
 	private JLabel lblSpilletid;
 	private JFormattedTextField ftfPremier;
 	private JFormattedTextField ftfPlayingTime;
 	private JLabel lblGenre;
 	private JLabel lblUdlbsdato;
 	private JFormattedTextField ftfOffday;
 	private JButton btnAddActor;
 	private JToggleButton tglbtnNewToggleButton;
 	private JButton btnAbort;
 	private JButton btnCreateMovie;
 	private JLabel lblOpretFilm;
 	private MaskFormatter maskFormatDate;
 	public final CountDownLatch latch = new CountDownLatch(1); //venter p brugerens input. 
 	private int playingTime; 
 	private Genre genre;
 	private JComboBox<Genre> comboBoxGenres;
 	private JComboBox<Director> comboBoxDirectors;
 	private JComboBox<Actor> comboBoxActors;
 	private Director director;
 	private Cast cast;
 	private JButton btnAddDirector;
 	private JButton btnAddGenre;
 	private JPanel panel_1;
 	private JPanel panel_2;
 	private boolean areChangesMade;
 	private Movie movie;
 	private boolean isCancelChosen;
 	private JLabel lblRating;
 	private JLabel lblRatingStars;
 	private JButton btnViewRatings;
	private SimpleDateFormat dateFormat;
 
 	/**
 	 * Create the frame.
 	 */
 	public CreateMovie(final Movie movie, boolean isEditAllowed) 
 	{
 		this.movie = movie;
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		addWindowListener(this);
 		areChangesMade = false;
 		isCancelChosen = false;
		dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
 		if(movie.getMovieID() == 0)			//Meaning an new "empty" movie. 
 		{	
 			setTitle("Opret film");
 			cast = new Cast();
 		}
 		else								//A loaded movie which is beeing editet. 
 		{
 			setTitle("Rediger film");		
 			cast = movie.getCast();
 		}
 
 		areChangesMade = false;
 
 		try
 		{
 			maskFormatDate = new MaskFormatter("##-##-####");
 		} 
 		catch (ParseException e1)
 		{
 			e1.printStackTrace();
 		}
 
 		setBounds(100, 100, 715, 319);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 
 		panel = new JPanel();
 		contentPane.add(panel, BorderLayout.CENTER);
 		panel.setLayout(null);
 
 		lblOpretFilm = new JLabel("opret film");
 		if(movie.getMovieID() != 0)
 		{	
 			lblOpretFilm.setText("Rediger film");
 		}
 		if(!isEditAllowed)
 		{
 			lblOpretFilm.setText("Vis film");	
 		}
 		lblOpretFilm.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		lblOpretFilm.setBounds(28, 11, 158, 31);
 		panel.add(lblOpretFilm);
 		
 		tfTitel = new JTextField();
 		tfTitel.setBounds(112, 53, 142, 20);
 		panel.add(tfTitel);
 		tfTitel.setText(movie.getTitle());
 		tfTitel.setColumns(10);
 
 		tfOriginalTitel = new JTextField();
 		tfOriginalTitel.setColumns(10);
 		tfOriginalTitel.setBounds(112, 84, 142, 20);
 		tfOriginalTitel.setText(movie.getOriginalTitle());
 		panel.add(tfOriginalTitel);
 
 		lblTitel = new JLabel("Titel");
 		lblTitel.setBounds(28, 57, 46, 14);
 		panel.add(lblTitel);
 
 		lblOriginalTitel = new JLabel("Original titel");
 		lblOriginalTitel.setBounds(28, 88, 74, 14);
 		panel.add(lblOriginalTitel);
 
 		lblInstruktr = new JLabel("Instrukt\u00F8r");
 		lblInstruktr.setBounds(28, 119, 74, 14);
 		panel.add(lblInstruktr);
 
 		lblPrmiere = new JLabel("Pr\u00E6miere");
 		lblPrmiere.setBounds(28, 151, 74, 14);
 		panel.add(lblPrmiere);
 
 		lblSpilletid = new JLabel("Spilletid");
 		lblSpilletid.setBounds(28, 182, 74, 14);
 		panel.add(lblSpilletid);
 
 		ftfPremier = new JFormattedTextField(maskFormatDate);
 		ftfPremier.setBounds(112, 146, 142, 20);
 		if (movie.getReleaseDate() != null)
 		{
			ftfPremier.setText(dateFormat.format(movie.getReleaseDate()));
 		}
 		panel.add(ftfPremier);
 
 		ftfPlayingTime = new JFormattedTextField();
 		ftfPlayingTime.setBounds(112, 178, 142, 20);
 		if (movie.getLength() != 0)
 		{
 			ftfPlayingTime.setText(movie.getLength()+"");
 		}
 		panel.add(ftfPlayingTime);
 
 		lblGenre = new JLabel("Genre");
 		lblGenre.setBounds(28, 212, 46, 14);
 		panel.add(lblGenre);
 
 		lblUdlbsdato = new JLabel("Udl\u00F8bsdato");
 		lblUdlbsdato.setBounds(28, 240, 64, 14);
 		panel.add(lblUdlbsdato);
 
 		ftfOffday = new JFormattedTextField(maskFormatDate);
 		ftfOffday.setBounds(112, 239, 142, 20);
 		if (movie.getTimeEnd() != null)
 		{
			ftfOffday.setText(dateFormat.format(movie.getTimeEnd()));
 		}
 		panel.add(ftfOffday);
 
 		if(isEditAllowed)
 		{
 			btnAddActor = new JButton("Opret skuespiller");
 			btnAddActor.addActionListener(new ActionListener() 
 			{
 				public void actionPerformed(ActionEvent e) 
 				{
 					final CreateActor createActor = new CreateActor();
 					createActor.setVisible(true);
 
 					createActor.addComponentListener(new ComponentListener()
 					{
 						@Override
 						public void componentHidden(ComponentEvent arg0)
 						{
 							Actor actor = createActor.getActor();
 							MovieController.actors.add(actor);
 							MovieController.newActors.add(actor);
 							createActor.dispose();
 							comboBoxActors.addItem(actor);
 							comboBoxActors.setSelectedItem(actor);
 						}
 						@Override
 						public void componentMoved(ComponentEvent arg0)
 						{	}
 						@Override
 						public void componentResized(ComponentEvent arg0)
 						{	}
 						@Override
 						public void componentShown(ComponentEvent arg0)
 						{	}
 					});
 				}
 			});
 			btnAddActor.setBounds(549, 52, 130, 23);
 			panel.add(btnAddActor);
 			
 			if(movie.getDirector() == null)
 			{	
 				btnCreateMovie = new JButton("Opret film");
 			}
 			else
 			{
 				btnCreateMovie = new JButton("Gem");	
 			}		
 			btnCreateMovie.addActionListener(new ActionListener() 
 			{
 				public void actionPerformed(ActionEvent e) 
 				{
 					if (comboBoxGenres.getSelectedItem() instanceof Genre)
 					{
 						genre = (Genre)comboBoxGenres.getSelectedItem();
 					}
 					if (comboBoxDirectors.getSelectedItem() instanceof Director)
 					{
 						director = (Director)comboBoxDirectors.getSelectedItem();
 					}
 
 					movie.setTitle(tfTitel.getText());
 					movie.setOriginalTitle(tfOriginalTitel.getText());
 					movie.setLength(playingTime);
 					movie.setDirector(director);
 					movie.setGenre(genre);
 					movie.setCast(cast);
 					movie.setIs3D(tglbtnNewToggleButton.isSelected());
 
 					areChangesMade = true;
 					latch.countDown();
 				} 
 
 			});
 			btnCreateMovie.setBackground(Color.GREEN);
 			btnCreateMovie.setBounds(579, 236, 100, 23);
 			panel.add(btnCreateMovie);
 			
 			btnAddDirector = new JButton("Opret instrukt\u00F8r");
 			btnAddDirector.addActionListener(new ActionListener() 
 			{
 				public void actionPerformed(ActionEvent e) 
 				{
 					final CreateDirector createDirector = new CreateDirector();
 					createDirector.setVisible(true);
 
 					createDirector.addComponentListener(new ComponentListener()
 					{
 						@Override
 						public void componentHidden(ComponentEvent arg0)
 						{
 							Director director = createDirector.getDirector();
 							MovieController.directors.add(director);
 							MovieController.newDirectors.add(director);
 							createDirector.dispose();
 							comboBoxDirectors.addItem(director);
 							comboBoxDirectors.setSelectedItem(director);
 						}
 						@Override
 						public void componentMoved(ComponentEvent arg0)
 						{	}
 						@Override
 						public void componentResized(ComponentEvent arg0)
 						{	}
 						@Override
 						public void componentShown(ComponentEvent arg0)
 						{	}
 					});
 				}
 			});
 			btnAddDirector.setBounds(549, 83, 130, 23);
 			panel.add(btnAddDirector);
 			
 			btnAddGenre = new JButton("Opret genre");
 			btnAddGenre.addActionListener(new ActionListener() 
 			{
 				public void actionPerformed(ActionEvent e) 
 				{
 					String genreName = JOptionPane.showInputDialog(CreateMovie.this, "Genretekst" , "Opret ny genre", JOptionPane.DEFAULT_OPTION);
 					if (genreName != null)
 					{
 						Genre newGenre = new Genre(genreName);
 						MovieController.genres.add(newGenre);
 						MovieController.newGenres.add(newGenre);
 						comboBoxGenres.addItem(newGenre);
 						comboBoxGenres.setSelectedItem(newGenre);
 					}
 				}
 			});
 			btnAddGenre.setBounds(549, 115, 130, 23);
 			panel.add(btnAddGenre);
 		}
 
 		tglbtnNewToggleButton = new JToggleButton("3D");
 		tglbtnNewToggleButton.setBounds(289, 236, 121, 23);
 		panel.add(tglbtnNewToggleButton);
 
 		btnAbort = new JButton("Annuller");
 		btnAbort.setBackground(Color.RED);
 		if(!isEditAllowed)
 		{
 			btnAbort.setText("ok");
 			btnAbort.setBackground(Color.GREEN);
 		}
 		btnAbort.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				areChangesMade = false;
 				isCancelChosen = true;
 				latch.countDown();
 			} 
 		});
 		btnAbort.setBounds(579, 208, 100, 23);
 		panel.add(btnAbort);
 		
 		panel_1 = new JPanel();
 		panel_1.setBounds(289, 84, 121, 142);
 		panel.add(panel_1);
 		panel_1.setBackground(Color.WHITE);
 		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
 
 		panel_2 = new JPanel();
 		panel_2.setBounds(414, 84, 121, 142);
 		panel.add(panel_2);
 		panel_2.setBackground(Color.WHITE);
 		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
 
 		if (movie.getCast() != null)
 		{
 			setPanels();
 		}
 		comboBoxGenres = new JComboBox(MovieController.genres.toArray());
 		comboBoxGenres.setBounds(112, 209, 142, 20);
 		comboBoxGenres.setSelectedItem(movie.getGenre());
 		panel.add(comboBoxGenres);
 
 		comboBoxDirectors = new JComboBox(MovieController.directors.toArray());
 		comboBoxDirectors.setBounds(112, 116, 142, 20);
 		comboBoxDirectors.setSelectedItem(movie.getDirector());
 		panel.add(comboBoxDirectors);
 
 		comboBoxActors = new JComboBox(MovieController.actors.toArray());
 		comboBoxActors.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				Actor selectedActor = (Actor)comboBoxActors.getSelectedItem();
 				String actorName = (selectedActor.getLName() + ", " + selectedActor.getFName()); 
 
 				String roleName = JOptionPane.showInputDialog(comboBoxActors, actorName + " spiller" , "Tilfj til rolleliste", JOptionPane.DEFAULT_OPTION);
 				if (roleName != null)
 				{
 
 					cast.putActor(selectedActor, roleName);
 					MovieController.addToCast.put(selectedActor, roleName);
 					setPanels();
 				}
 			} 
 		});
 		comboBoxActors.setBounds(289, 53, 125, 23);
 		panel.add(comboBoxActors);
 
 		lblRating = new JLabel("Ratings:");
 		lblRating.setBounds(424, 240, 46, 14);
 		panel.add(lblRating);
 
 		lblRatingStars = new JLabel("Ingen");
 		lblRatingStars.setBounds(471, 240, 64, 14);
 		panel.add(lblRatingStars);
 		double stars = movie.getStarsAvarage();
 		if (stars != 0)
 		{
 			lblRatingStars.setText(""+stars);
 		}
 		
 		String ratingsString = "" + movie.getRatingsString();
 		if (!ratingsString.equals(""))
 		{
 		btnViewRatings = new JButton("se");
 		btnViewRatings.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				final ShowRatings showRatings = new ShowRatings(movie.getRatingsString());
 				showRatings.setVisible(true);
 
 				showRatings.addComponentListener(new ComponentListener()
 				{
 					@Override
 					public void componentHidden(ComponentEvent arg0)
 					{
 						showRatings.dispose();
 					}
 					@Override
 					public void componentMoved(ComponentEvent arg0)
 					{	}
 					@Override
 					public void componentResized(ComponentEvent arg0)
 					{	}
 					@Override
 					public void componentShown(ComponentEvent arg0)
 					{	}
 				});
 			}
 		});
 		btnViewRatings.setBounds(506, 236, 46, 23);
 		panel.add(btnViewRatings);
 		}
 	}
 
 	private void setPanels()
 	{
 		panel_1.removeAll();
 		panel_2.removeAll();
 
 		Map<Actor, String> castMap = cast.getCast();
 		for (Actor actor: castMap.keySet())
 		{
 			panel_1.add(new JLabel(actor.getLName() + ", " + actor.getFName()));
 			panel_2.add(new JLabel(castMap.get(actor)));
 		}	
 		panel_1.validate();
 		panel_1.repaint();
 		panel_2.validate();
 		panel_2.repaint();
 	}
 
 	public Movie getMovie()
 	{
 		return movie;
 	}
 	public String getPremierDate()
 	{
 		return ftfPremier.getText();
 	}
 	public String getOffDate()
 	{
 		return ftfOffday.getText();
 	}
 	public String getPlayingTime()
 	{
 		return ftfPlayingTime.getText(); 
 	}
 	public boolean areChangesMade()
 	{
 		return areChangesMade;
 	}
 	public boolean isCancelChosen()
 	{
 		return isCancelChosen;
 	}
 	public void showMessage(String text)
 	{
 		JOptionPane.showMessageDialog(null,
 				text,
 				"Advarsel",
 				JOptionPane.PLAIN_MESSAGE);
 	}
 	public int showYesNoDialog(String text)
 	{
 		int choise = JOptionPane.showConfirmDialog((Component) null, 
 				text,
 				"Advarsel", 
 				JOptionPane.YES_NO_OPTION);
 
 		return choise;
 	}
	/**
	 * Shows an JOptionPane with the text send as parameter. 
	 * @param text
	 * @return int choise. 0 if yes i chosen. 
	 */
	public static int showOKCancelDialog(String text)
 	{
 		int choise = JOptionPane.showConfirmDialog((Component) null, 
 				text,
 				"Advarsel", 
 				JOptionPane.OK_CANCEL_OPTION);
 
 		return choise;
 	}
 	public void setCastMap()
 	{	}
 	@Override
 	public void windowActivated(WindowEvent e)
 	{	}
 	@Override
 	public void windowClosed(WindowEvent e)
 	{	}
 	@Override
 	public void windowClosing(WindowEvent e)
 	{
 		movie = null;
 		areChangesMade = false;
 		latch.countDown();
 	}
 	@Override
 	public void windowDeactivated(WindowEvent e)
 	{ }
 	@Override
 	public void windowDeiconified(WindowEvent e)
 	{ }
 	@Override
 	public void windowIconified(WindowEvent e)
 	{ }
 	@Override
 	public void windowOpened(WindowEvent e)
 	{ }
 }
