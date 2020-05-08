 package dk.kea.si.movies.persistence.mappers;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import dk.kea.si.movies.domain.AlternateIds;
 import dk.kea.si.movies.domain.Cast;
 import dk.kea.si.movies.domain.Clip;
 import dk.kea.si.movies.domain.Directors;
 import dk.kea.si.movies.domain.DomainObject;
 import dk.kea.si.movies.domain.Links;
 import dk.kea.si.movies.domain.Movie;
 import dk.kea.si.movies.domain.Movie.Timeline;
 import dk.kea.si.movies.domain.Posters;
 import dk.kea.si.movies.domain.Ratings;
 import dk.kea.si.movies.domain.ReleaseDates;
 import dk.kea.si.movies.domain.Review;
 import dk.kea.si.movies.util.ApplicationException;
 
 public class MovieMapper extends AbstractMapper {
 
 	public static final String COLUMNS = "Movie.id, Movie.title, Movie.year,"
 			+ " Movie.timeline, Movie.runtime, Movie.mpaa_rating,"
 			+ " Movie.users_rating_score, Movie.studio, Movie.critics_consensus,"
 			+ " Movie.synopsis";
 
 	@Override
 	protected String findStatement() {
 		return "SELECT " + COLUMNS + " FROM Movie AS Movie"
				+ " WHERE Movie.id=?" + " LIMIT 1";
 	}
 
 	@Override
 	protected String findAllStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected String updateStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected String insertStatement() {
 		return "INSERT INTO Movie (id, title, year, timeline, runtime,"
 				+ " mpaa_rating, users_rating_score, studio, critics_consensus,"
 				+ " synopsis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
 	}
 
 	@Override
 	protected String deleteStatement() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected DomainObject doLoad(Long id, ResultSet rs) throws SQLException {
 		Movie movie = new Movie();
 		movie.setId(rs.getLong("Movie.id"));
 		movie.setTitle(rs.getString("Movie.title"));
 		movie.setYear(rs.getString("Movie.year"));
 		movie.setTimeline(Timeline.valueOf(rs.getString("Movie.timeline")));
 		movie.setRuntime("" + rs.getInt("Movie.runtime"));
 		movie.setMpaa_rating(rs.getString("Movie.mpaa_rating"));
 		movie.setUsersRatingScore(rs.getFloat("Movie.users_rating_score"));
 		movie.setStudio(rs.getString("Movie.studio"));
 		movie.setCritics_consensus(rs.getString("Movie.critics_consensus"));
 		movie.setSynopsis(rs.getString("Movie.synopsis"));
 		return movie;
 	}
 
 	@Override
 	protected void doUpdate(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void doInsert(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		Movie movie = (Movie) o;
 		s.setLong(1, movie.getId());
 		s.setString(2, movie.getTitle());
 		if(movie.getYear().equals("")) {
 			movie.setYear("0000");
 		}
 		s.setString(3, movie.getYear());
 		s.setString(4, movie.getTimeline().toString());
		System.out.println("runtime:"+movie.getRuntime());
 		if(!movie.getRuntime().matches("\\d+")) { // if string is not integer
 			movie.setRuntime("0");
 		}
 		s.setInt(5, Integer.parseInt(movie.getRuntime()));
 		s.setString(6, movie.getMpaa_rating());
 		s.setFloat(7, movie.getUsersRatingScore());
 		s.setString(8, movie.getStudio());
 		s.setString(9, movie.getCritics_consensus());
 		s.setString(10, movie.getSynopsis());
 	}
 
 	@Override
 	protected void doDelete(DomainObject o, PreparedStatement s)
 			throws SQLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public DomainObject find(long id) {
 		DomainObject result = (DomainObject) loadedMap.get(id);
 		if (result != null) {
 			return result;
 		}
 		PreparedStatement findStatement = null;
 		try {
 			findStatement = getConnection().prepareStatement(findStatement());
 			findStatement.setLong(1, id);
 			System.out.println(findStatement);
 			ResultSet rs = findStatement.executeQuery();
 			if (rs.next()) {
 				return load(rs);
 			} else {
 				return null;
 			}
 		} catch (SQLException e) {
 			throw new ApplicationException(e);
 		} finally {
 			closeStatement(findStatement);
 		}
 	}
 
 	public long insert(DomainObject obj) {
 		PreparedStatement insertStatement = null;
 		try {
 			startTransaction();
 
 			insertStatement = getConnection().prepareStatement(
 					insertStatement());
 			doInsert(obj, insertStatement);
 			System.out.println(insertStatement);
 			insertStatement.execute();
 
 			Movie movie = (Movie) obj;
 			Long lastInsertId = movie.getId();
 			obj.setId(lastInsertId);
 			loadedMap.put(lastInsertId, obj);
 
 			insertReleases(movie);
 			insertPosters(movie);
 			insertRatings(movie);
 			insertAlternateIds(movie);
 			insertLinks(movie);
 			insertGenres(movie);
 			insertDirectors(movie);
 			insertReviews(movie);
 			insertClips(movie);
 			insertCast(movie);
 
 			commitTransaction();
 			return lastInsertId;
 		} catch (Exception e) {
 			rollbackTransaction();
 			throw new ApplicationException(e);
 		} finally {
 			closeStatement(insertStatement);
 			endTransaction();
 		}
 	}
 
 	private void insertCast(Movie movie) {
 		CastMapper castMapper = (CastMapper) getMapper(Cast.class);
 		CharacterMapper characterMapper = new CharacterMapper();
 		for (int i = 0; i < movie.getFullCast().length; i++) {
 			Cast cast = movie.getFullCast()[i];
 			Cast storedCast = castMapper.findByName(cast.getName());
 			if (storedCast == null) {
 				castMapper.insert(cast);
 			} else {
 				cast = storedCast;
 			}
 			String[] characters = movie.getFullCast()[i].getCharacters();
 			for (int j = 0; j < characters.length; j++) {
 				characterMapper.insert(movie.getId(), cast.getId(),
 						characters[j]);
 			}
 		}
 	}
 
 	private void insertClips(Movie movie) {
 		ClipMapper clipMapper = (ClipMapper) getMapper(Clip.class);
 		for (int i = 0; i < movie.getClips().size(); i++) {
 			Clip clip = movie.getClips().get(i);
 			clip.setMovieId(movie.getId());
 			clipMapper.insert(clip);
 		}
 	}
 
 	private void insertReviews(Movie movie) {
 		ReviewMapper reviewMapper = (ReviewMapper) getMapper(Review.class);
 		for (int i = 0; i < movie.getReviews().size(); i++) {
 			Review review = movie.getReviews().get(i);
 			review.setMovieId(movie.getId());
 			reviewMapper.insert(review);
 		}
 	}
 
 	private void insertDirectors(Movie movie) {
 		DirectorsMapper directorsMapper = (DirectorsMapper) getMapper(Directors.class);
 		MovieDirectorMapper movieDirectorMapper = new MovieDirectorMapper();
 		Directors[] abridgedDirectors = movie.getAbridged_directors();
 		if (abridgedDirectors != null) {
 			for (int i = 0; i < abridgedDirectors.length; i++) {
 				Directors directors = abridgedDirectors[i];
 				Directors storedDirectors = directorsMapper
 						.findByName(directors.getName());
 				if (storedDirectors == null) {
 					long lastId = directorsMapper.insert(directors);
 					directors.setId(lastId);
 				} else {
 					directors = storedDirectors;
 				}
 				movieDirectorMapper.insert(movie.getId(), directors.getId());
 			}
 		}
 	}
 
 	private void insertGenres(Movie movie) {
 		GenreMapper genreMapper = new GenreMapper();
 		for (int i = 0; i < movie.getGenres().length; i++) {
 			genreMapper.insert(movie.getId(), movie.getGenres()[i]);
 		}
 	}
 
 	private void insertLinks(Movie movie) {
 		Links links = movie.getLinks();
 		links.setMovieId(movie.getId());
 		getMapper(Links.class).insert(links);
 	}
 
 	private void insertAlternateIds(Movie movie) {
 		AlternateIds ids = movie.getAlternate_ids();
 		if (ids != null) {
 			ids.setMovieId(movie.getId());
 			getMapper(AlternateIds.class).insert(ids);
 		}
 	}
 
 	private void insertRatings(Movie movie) {
 		Ratings ratings = movie.getRatings();
 		ratings.setMovieId(movie.getId());
 		getMapper(Ratings.class).insert(ratings);
 	}
 
 	private void insertPosters(Movie movie) {
 		Posters posters = movie.getPosters();
 		posters.setMovieId(movie.getId());
 		getMapper(Posters.class).insert(posters);
 	}
 
 	private void insertReleases(Movie movie) {
 		ReleaseDates releases = movie.getRelease_dates();
 		releases.setMovieId(movie.getId());
 		getMapper(ReleaseDates.class).insert(releases);
 	}
 
 	class GenreMapper {
 
 		protected String insertStatement() {
 			return "INSERT INTO `Genre` (movie_id, name) VALUES (?, ?);";
 		}
 
 		public long insert(long id, String genre) {
 			PreparedStatement insertStatement = null;
 			try {
 				insertStatement = getConnection().prepareStatement(
 						insertStatement(), Statement.RETURN_GENERATED_KEYS);
 				doInsert(id, genre, insertStatement);
 				System.out.println(insertStatement);
 				insertStatement.execute();
 				return findLastInsertId(insertStatement);
 			} catch (SQLException e) {
 				throw new ApplicationException(e);
 			} finally {
 				closeStatement(insertStatement);
 			}
 		}
 
 		protected void doInsert(long movieId, String genre, PreparedStatement s)
 				throws SQLException {
 			s.setLong(1, movieId);
 			s.setString(2, genre);
 		}
 	}
 
 	class MovieDirectorMapper {
 
 		protected String insertStatement() {
 			return "INSERT INTO `MovieDirector` (director_id, movie_id)"
 					+ " VALUES (?, ?);";
 		}
 
 		public long insert(long movieId, long directorId) {
 			PreparedStatement insertStatement = null;
 			try {
 				insertStatement = getConnection().prepareStatement(
 						insertStatement(), Statement.RETURN_GENERATED_KEYS);
 				doInsert(movieId, directorId, insertStatement);
 				System.out.println(insertStatement);
 				insertStatement.execute();
 				return findLastInsertId(insertStatement);
 			} catch (SQLException e) {
 				throw new ApplicationException(e);
 			} finally {
 				closeStatement(insertStatement);
 			}
 		}
 
 		protected void doInsert(long movieId, long directorId,
 				PreparedStatement s) throws SQLException {
 			s.setLong(1, directorId);
 			s.setLong(2, movieId);
 		}
 	}
 
 	class CharacterMapper {
 
 		protected String insertStatement() {
 			return "INSERT INTO `Character` (actor_id, movie_id, name)"
 					+ " VALUES (?, ?, ?);";
 		}
 
 		public long insert(long movieId, long actorId, String name) {
 			PreparedStatement insertStatement = null;
 			try {
 				insertStatement = getConnection().prepareStatement(
 						insertStatement(), Statement.RETURN_GENERATED_KEYS);
 				doInsert(movieId, actorId, name, insertStatement);
 				System.out.println(insertStatement);
 				insertStatement.execute();
 				return findLastInsertId(insertStatement);
 			} catch (SQLException e) {
 				throw new ApplicationException(e);
 			} finally {
 				closeStatement(insertStatement);
 			}
 		}
 
 		protected void doInsert(long movieId, long actorId, String name,
 				PreparedStatement s) throws SQLException {
 			s.setLong(1, actorId);
 			s.setLong(2, movieId);
 			s.setString(3, name);
 		}
 	}
 }
