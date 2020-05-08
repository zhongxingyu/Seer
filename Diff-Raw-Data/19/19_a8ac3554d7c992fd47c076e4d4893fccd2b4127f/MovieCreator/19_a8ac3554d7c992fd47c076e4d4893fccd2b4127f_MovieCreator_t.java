 package view.items.creators;
 
 import java.awt.HeadlessException;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.JFormattedTextField;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 
 import logger.MabisLogger;
 import model.BaseTable;
 import model.entity.Author;
 import model.entity.Genre;
 import model.entity.Movie;
 import model.enums.AuthorType;
 import model.enums.GenreType;
 import settings.GlobalPaths;
 import view.imagePanel.ImagePanel;
 import view.items.CreatorContentNullPointerException;
 import view.items.ItemCreator;
 import view.items.itemsprieview.ItemsPreview;
 import view.items.minipanels.AuthorMiniPanel;
 import view.items.minipanels.TagCloudMiniPanel;
 import controller.SQLStamentType;
 import controller.api.MovieAPI;
 import controller.api.MovieAPI.MovieApiTarget;
 import controller.entity.AuthorSQLFactory;
 import controller.entity.GenreSQLFactory;
 import controller.entity.MovieSQLFactory;
 
 /**
  * 
  * Klasa która definiuje strukturę okienka zapewniającego miejsce do
  * definiowanie nowego filmu.
  * 
  * @author kornicameister
  * 
  */
 public class MovieCreator extends ItemCreator {
 	private static final long serialVersionUID = -8029181092669910824L;
 	private JTextField titleField;
 	private ImagePanel coverPanel;
 	private TagCloudMiniPanel tagCloud;
 	private AuthorMiniPanel directorsPanel;
 	private JFormattedTextField durationField;
 	private Movie selectedMovie;
 	private JTextArea descriptionArea;
 	private JScrollPane descriptionScrollPane;
 	private LoadFromApi lfa;
 
 	public MovieCreator(String title) throws HeadlessException,
 			CreatorContentNullPointerException {
 		super(title);
 		this.setSize((int) this.getMinimumSize().getWidth() + 190, (int) this
 				.getMinimumSize().getHeight() + 50);
 	}
 
 	@Override
 	protected void layoutComponents() {
 		super.layoutComponents();
 		GroupLayout gl = new GroupLayout(this.contentPanel);
 		this.contentPanel.setLayout(gl);
 
 		gl.setAutoCreateGaps(true);
 		gl.setAutoCreateContainerGaps(true);
 
 		gl.setHorizontalGroup(gl
 				.createSequentialGroup()
 				.addComponent(this.coverPanel, 250, 250, 250)
 				.addGroup(
 						gl.createParallelGroup()
 								.addGroup(
 										gl.createSequentialGroup()
 												.addComponent(this.titleField)
 												.addComponent(
 														this.durationField))
 								.addComponent(this.directorsPanel)
 								.addComponent(this.tagCloud)
 								.addComponent(this.descriptionScrollPane)));
 		gl.setVerticalGroup(gl
 				.createParallelGroup()
 				.addComponent(this.coverPanel, 250, 250, 250)
 				.addGroup(
 						gl.createSequentialGroup()
 								.addGroup(
 										gl.createParallelGroup()
												.addComponent(this.titleField,40, 40, 40)
												.addComponent(this.durationField, 40,40, 40))
 								.addComponent(this.directorsPanel)
								.addComponent(this.tagCloud,90,90,90)
 								.addComponent(this.descriptionScrollPane)));
 
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				coverPanel.setImage(new File(GlobalPaths.DEFAULT_COVER_PATH
 						.toString()));
 			}
 		});
 		this.revalidate();
 		this.pack();
 	}
 
 	@Override
 	protected void initComponents() throws CreatorContentNullPointerException {
 		super.initComponents();
 		this.contentPanel = new JPanel(true);
 		this.titleField = new JTextField();
 		this.descriptionArea = new JTextArea();
		this.descriptionArea.setLineWrap(true);
 		this.descriptionScrollPane = new JScrollPane(this.descriptionArea);
 		this.descriptionScrollPane.setBorder(BorderFactory
 				.createTitledBorder("Plot"));
 		this.titleField.setBorder(BorderFactory.createTitledBorder("Title"));
 		this.durationField = new JFormattedTextField(new SimpleDateFormat(
 				"hh:mm"));
 		this.durationField.setBorder(BorderFactory
 				.createTitledBorder("Duration"));
 		this.coverPanel = new ImagePanel();
 
 		this.initializeAuthorsMiniPanel();
 		this.initializeTagCloud();
 
 		String arr[] = { "by title" };
 		this.searchPanel.setSearchCriteria(arr);
 	}
 
 	private void initializeAuthorsMiniPanel() {
 		try {
 			AuthorSQLFactory asf = new AuthorSQLFactory(SQLStamentType.SELECT, new Author());
 			asf.addWhereClause("type", AuthorType.MOVIE_DIRECTOR.toString());
 			asf.executeSQL(true);
 			this.directorsPanel = new AuthorMiniPanel(asf.getAuthors(), AuthorType.MOVIE_DIRECTOR);
 			this.directorsPanel.setBorder(BorderFactory.createTitledBorder("Authors"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void initializeTagCloud() {
 		// loading genres
 		try {
 			GenreSQLFactory gsf = new GenreSQLFactory(SQLStamentType.SELECT,
 					new Genre());
 			gsf.addWhereClause("type", GenreType.MOVIE.toString());
 			gsf.executeSQL(true);
 			this.tagCloud = new TagCloudMiniPanel(gsf.getGenres(),GenreType.MOVIE);
 			this.tagCloud.setBorder(BorderFactory
 					.createTitledBorder("Tag cloud"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void clearContentFields() {
 		this.titleField.setText("");
 		this.durationField.setText("");
 		this.descriptionArea.setText("");
 		this.coverPanel.setImage(new File(GlobalPaths.DEFAULT_COVER_PATH
 				.toString()));
 		this.directorsPanel.clear();
 		this.tagCloud.clear();
 	}
 
 	@Override
 	protected Boolean createItem() {
 		MovieSQLFactory msf = new MovieSQLFactory(SQLStamentType.INSERT,
 				this.selectedMovie);
 		try {
 			msf.executeSQL(true);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Podklasa SwingWorkera, pozwala na wykonanie całej łączności z API poprzez
 	 * wątek działający w tle. Wyspecjalizowana do łączenia się z API dla
 	 * filmów.
 	 * 
 	 * @author kornicameister
 	 * 
 	 */
 	class LoadFromApi extends SwingWorker<TreeSet<BaseTable>, Void> {
 		private String query;
 		private Integer taskSize;
 		private int step, value;
 
 		public void setQuery(String query) {
 			this.query = query;
 		}
 
 		@Override
 		protected void done() {
 			try {
 				ItemsPreview ip = new ItemsPreview("Collected movies",this.get());
 				ip.addPropertyChangeListener("selectedItem",
 						new PropertyChangeListener() {
 							@Override
 							public void propertyChange(PropertyChangeEvent e) {
 								fillWithResult((BaseTable) e.getNewValue());
 							}
 						});
 				ip.setVisible(true);
 			} catch (InterruptedException | ExecutionException e1) {
 				e1.printStackTrace();
 				MabisLogger.getLogger().log(Level.SEVERE,
 						"Failed to receive data from background thread \n {0}",
 						e1.getMessage());
 			}
 		}
 
 		@Override
 		protected TreeSet<BaseTable> doInBackground() throws Exception {
 			MovieAPI ma = new MovieAPI(MovieApiTarget.IMDB);
 			ma.getPcs().addPropertyChangeListener(new PropertyChangeListener() {
 
 				@Override
 				public void propertyChange(PropertyChangeEvent evt) {
 					String propertyName = evt.getPropertyName();
 					if (propertyName.equals("taskStarted")) {
 						taskSize = (Integer) evt.getNewValue();
 						step = (searchProgressBar.getMaximum() - searchProgressBar
 								.getMinimum()) / taskSize;
 						value = searchProgressBar.getMinimum() + step;
 						setProgress(value);
 						if (taskSize.equals(1)) {
 							value = 0;
 						}
 					} else if (propertyName.equals("taskStep")) {
 						value += step;
 						setProgress(value);
 					}
 				}
 			});
 
 			try {
 				setProgress(searchProgressBar.getMinimum());
 				TreeMap<String, String> params = new TreeMap<>();
 				params.put("movie", query);
 				ma.query(params);
 				setProgress(searchProgressBar.getMaximum());
 				return ma.getResult();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	@Override
 	protected void fetchFromAPI(String query, String criteria) {
 		if (this.collectedItems != null) {
 			this.collectedItems.clear();
 		}
 
 		lfa = new LoadFromApi();
 		lfa.setQuery(query);
 		lfa.addPropertyChangeListener(new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				if ("progress" == evt.getPropertyName()) {
 					searchProgressBar.setValue((Integer) evt.getNewValue());
 				}
 			}
 		});
 		lfa.execute();
 	}
 
 	@Override
 	protected void cancelAPISearch() {
 		while (!lfa.isCancelled()) {
 			lfa.cancel(true);
 		}
 		MabisLogger.getLogger().log(Level.INFO, "Terminated search operation");
 		this.searchProgressBar.setValue(0);
 	}
 
 	@Override
 	protected void fillWithResult(BaseTable table) {
 		this.selectedMovie = (Movie) table;
 		this.titleField.setText(this.selectedMovie.getTitle());
 		this.durationField.setText(this.selectedMovie.getDuration());
 		this.descriptionArea.setText(this.selectedMovie.getDescription());
 		this.coverPanel.setImage(this.selectedMovie.getCover().getImageFile());
 		this.directorsPanel.setAuthors(this.selectedMovie.getAuthors());
 		this.tagCloud.setTags(this.selectedMovie.getGenres());
 	}
 
 }
