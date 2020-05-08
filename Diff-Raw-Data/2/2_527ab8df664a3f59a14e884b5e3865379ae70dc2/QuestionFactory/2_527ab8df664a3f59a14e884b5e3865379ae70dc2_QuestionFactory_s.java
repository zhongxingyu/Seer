 package my.triviagame.bll;
 
 import com.google.common.collect.Lists;
 import com.sun.tools.javac.util.Pair;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import my.triviagame.dal.IAlbumDescriptor;
 import my.triviagame.dal.IArtistDescriptor;
 
 import my.triviagame.dal.IDAL;
 import my.triviagame.dal.IDAL.DALException;
 import my.triviagame.dal.ITrackDescriptor;
 
 
 /**
  * TODO class-level documentation.
  */
 public class QuestionFactory {
 
     public static enum QuestionType {
         matchAlbumsToGenreQuestionType,               // DONE
         matchAlbumsToYearQuestionType,                // DONE
         matchTrackToAlbumQuestionType,                // DONE
         matchTrackToArtistQuestionType,               // DONE
         multipleAlbumHasNumberOfTracksQuestionType,   // DONE
         multipleArtistWithMostAlbumsQuestionType,     // DONE
         multipleCountAlbumsByArtistQuestionType,      // DONE
         multipleSongNotByArtistQuestionType,          // DONE
         multipleTrackToArtistQuestionType,            // DONE
         sortAlbumsByYearQuestionType,                 // DONE
         sortTracksByLengthQuestionType,               // DONE
         sortTrackByOrderInAlbumQuestionType,          // DONE
     }
 
     public QuestionFactory(IDAL databaseLayer) {
         this.databaseLayer = databaseLayer;
     }
 
     protected Question generateQuestion(QuestionType type) throws Throwable {
         /*
          * TODO @guyr
          * We wanted to make all the factory methods private, but it doesnt work with getMethod
          * It may work if we get the proper factory method by using a HashSet instead.
          * This is low priority therefore its on my TODO list
          */
         Method method = QuestionFactory.class.getMethod(type.toString());
         try {
             return (Question) (method.invoke(this));
         } catch (InvocationTargetException ex) {
             throw ex.getCause();
         }
     }
 
     public MatchQuestion matchAlbumsToGenreQuestionType() throws DALException {
         String question = "Match the following albums to genre:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentGenres();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(album.getTitle(), album.getGenre()));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MatchQuestion matchAlbumsToYearQuestionType() throws DALException {
         String question = "Match the following albums to year:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentYears();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(album.getTitle(), Integer.toString(album.getYear())));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MatchQuestion matchTrackToAlbumQuestionType() throws DALException {
         String question = "Match the following tracks to albums:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbums();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(album.getTitle(),
                     this.getRandomTrackFromAlbum(album).getTitle()));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MatchQuestion matchTrackToArtistQuestionType() throws DALException {
         String question = "Match the following tracks to artists:";
         Collection<ITrackDescriptor> tracks = getFourTracksByDifferentArtists();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         for (ITrackDescriptor track: tracks) {
             matches.add(new Pair<String, String>(track.getTitle(), track.getArtistName()));
         }
         MatchAnswer answer = new MatchAnswer(matches);
         return new MatchQuestion(question, answer);
     }
 
     public MultipleChoiceQuestion multipleAlbumHasNumberOfTracksQuestionType() throws DALException {
         String question = "Which of the following albums has %s tracks?";
         Collection<IAlbumDescriptor> artists = getFourDifferentAlbumsByDifferentNumberOfTracks();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         Iterator<IAlbumDescriptor> iterator = artists.iterator();
         IAlbumDescriptor winningAlbum = null;
         for (int i=0; i<4; i++) {
             IAlbumDescriptor album = iterator.next();
             if (winningAlbum == null) {
                 winningAlbum = album;
             }
             answers.add(new MultipleChoiceAnswer(album.getTitle()));
         }
         String formattedQuestion = String.format(
                 question, Integer.toString(databaseLayer.getAlbumTrackDescriptors(winningAlbum).size()));
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
 
     }
 
     public MultipleChoiceQuestion multipleArtistWithMostAlbumsQuestionType() throws DALException {
         String question = "Which of the following artists has the most albums?";
         Collection<IArtistDescriptor> artists = getFourDifferentArtistsWithDifferentNumberOfAlbums();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         Iterator<IArtistDescriptor> iterator = artists.iterator();
         IArtistDescriptor winningArtist = null;
         MultipleChoiceAnswer winningAnswer = null;
         for (int i=0; i<4; i++) {
             IArtistDescriptor artist = iterator.next();
             MultipleChoiceAnswer answer = new MultipleChoiceAnswer(artist.getName());
             if (winningArtist == null ||
                     databaseLayer.getArtistAlbumDescriptors(winningArtist).size() >
                     databaseLayer.getArtistAlbumDescriptors(artist).size()) {
                 winningAnswer = answer;
             }
             answers.add(answer);
         }
         return new MultipleChoiceQuestion(question, winningAnswer, answers);
 
     }
 
     public MultipleChoiceQuestion multipleCountAlbumsByArtistQuestionType() throws DALException {
         String question = "Which of the following artists has %s albums?";
         Collection<IArtistDescriptor> artists = getFourDifferentArtists();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         Iterator<IArtistDescriptor> iterator = artists.iterator();
         IArtistDescriptor winningArtist = null;
         for (int i=0; i<4; i++) {
             IArtistDescriptor artist = iterator.next();
             if (winningArtist == null) {
                 winningArtist = artist;
             }
             answers.add(new MultipleChoiceAnswer(artist.getName()));
         }
         Integer count = databaseLayer.getArtistTrackDescriptors(winningArtist).size();
         String formattedQuestion = String.format(question, count.toString());
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
     }
 
     public MultipleChoiceQuestion multipleSongNotByArtistQuestionType() throws DALException {
         String question = "Which of the following songs is not performed by %s?";
         List<IArtistDescriptor> artists = Lists.newArrayList(this.getRandomArtists(2));
         if (databaseLayer.getArtistTrackDescriptors(artists.get(0)).size() < 3) {
             // we need an artist with at least three songs
             return this.multipleSongNotByArtistQuestionType();
         }
         List<ITrackDescriptor> tracks = Lists.newArrayList(this.getThreeTracksFromArtist(artists.get(0)));
         List<String> trackNames = new ArrayList<String>(4);
         for (ITrackDescriptor track: tracks) {
             trackNames.add(track.getTitle());
         }
         while (trackNames.size()<4) {
             ITrackDescriptor differentTrack = getRandomTrackFromArtist(artists.get(1));
             if (trackNames.contains(differentTrack.getTitle())) {
                 continue;
             }
             // This is because the track from the 2nd artist can also be performed by the 1st artist
             trackNames.add(differentTrack.getTitle());
         }
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         IArtistDescriptor winningArtist = null;
         for (int i=0; i<4; i++) {
             answers.add(new MultipleChoiceAnswer(trackNames.get(i)));
         }
        String formattedQuestion = String.format(question, artists.get(1).getName());
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
     }
 
     /**
      * Implementation for the game question of tracks by different artists
      */
     public MultipleChoiceQuestion multipleTrackToArtistQuestionType() throws DALException {
         String question = "Which of the following tracks was performed by %s?";
         Collection<ITrackDescriptor> tracks = getFourTracksByDifferentArtists();
         List<MultipleChoiceAnswer> answers = new ArrayList<MultipleChoiceAnswer>(4);
         Iterator<ITrackDescriptor> iterator = tracks.iterator();
         ITrackDescriptor winningTrack = null;
         for (int i=0; i<4; i++) {
             ITrackDescriptor track = iterator.next();
             if (winningTrack == null) {
                 winningTrack = track;
             }
             answers.add(new MultipleChoiceAnswer(track.getTitle()));
         }
         String formattedQuestion = String.format(question, winningTrack.getArtistName());
         return new MultipleChoiceQuestion(formattedQuestion, answers.get(0), answers);
     }
 
     public OrderedQuestion sortAlbumsByYearQuestionType() throws DALException {
         String question = "Sort the following albums by year:";
         Collection<IAlbumDescriptor> albums = getFourDifferentAlbumsByDifferentYears();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         int index = 0;
         for (IAlbumDescriptor album: albums) {
             matches.add(new Pair<String, String>(Integer.toString(album.getYear()), album.getTitle()));
             index++;
         }
         OrderedAnswer answer = new OrderedAnswer(matches);
         return new OrderedQuestion(question, answer);
     }
 
     public OrderedQuestion sortTracksByLengthQuestionType() throws DALException {
         String question = "Sort the following tracks by length:";
         Collection<ITrackDescriptor> tracks = getFourRandomTracksByDifferentLength();
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         int index = 0;
         for (ITrackDescriptor track: tracks) {
             matches.add(new Pair<String, String>(Integer.toString(index), Integer.toString(track.getLengthInSeconds())));
             index++;
         }
         OrderedAnswer answer = new OrderedAnswer(matches);
         return new OrderedQuestion(question, answer);
     }
 
     public OrderedQuestion sortTrackByOrderInAlbumQuestionType() throws DALException {
         String question = "Sort the following tracks by their order in album %s:";
         IAlbumDescriptor album = Lists.newArrayList(this.getRandomAlbums(1)).get(0);
         Set<ITrackDescriptor> tracks = new HashSet<ITrackDescriptor>(4);
         if (databaseLayer.getAlbumTrackDescriptors(album).size() < 4) {
             // we need an album with at least 4 tracks
             return this.sortTrackByOrderInAlbumQuestionType();
         }
         while (tracks.size() < 4) {
             ITrackDescriptor track = this.getRandomTrackFromAlbum(album);
             if (tracks.contains(track)) {
                 continue;
             }
             tracks.add(track);
         }
         Collection<Pair<String,String>> matches = new HashSet<Pair<String,String>>(4);
         List<ITrackDescriptor> sortedTrackListByTrackNumber = this.sortTracksByTrackNumber(tracks);
         int index = 0;
         for (ITrackDescriptor track: sortedTrackListByTrackNumber) {
             matches.add(new Pair<String, String>(Integer.toString(index), track.getTitle()));
             index++;
         }
         OrderedAnswer answer = new OrderedAnswer(matches);
         String formattedQuestion = String.format(question, album.getTitle());
         return new OrderedQuestion(formattedQuestion, answer);
     }
 
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbums() throws DALException {
          Collection<IAlbumDescriptor> randomAlbums = getRandomAlbums(12);
          Set<IAlbumDescriptor> chosenAlbums = new HashSet<IAlbumDescriptor>(4);
          for (IAlbumDescriptor album: randomAlbums) {
              if (chosenAlbums.size() == 4) {
                  break;
              }
              if (this.databaseLayer.getAlbumTrackDescriptors(album).isEmpty()) {
                  continue;
              }
              chosenAlbums.add(album);
          }
          if (chosenAlbums.size() != 4) {
              return this.getFourDifferentAlbums();
          }
          return chosenAlbums;
     }
 
     private Collection<IArtistDescriptor> getFourDifferentArtists() throws DALException {
         return getRandomArtists(4);
     }
 
    /**
      * The current implementation fetches random tracks, until it gets four tracks by different artists (recursion).
      * Our of a +1M records, we assume that chances for entering recursion are not high.
      * Another implementation would be to choose four random artists, and then a random track from each artist.
      * This second implementation is not recursive, but it takes more than just one query.
      */
     private Collection<IAlbumDescriptor> getFourDifferentAlbumsByDifferentGenres() throws DALException {
         Collection<IAlbumDescriptor> albums = getRandomAlbums(12);
         Collection<IAlbumDescriptor> selectedAlbums = new HashSet<IAlbumDescriptor>(4);
         Set<String> genres = new HashSet<String>(4);
         for (IAlbumDescriptor album : albums) {
             if (genres.contains(album.getGenre())) {
                 continue;
             }
             genres.add(album.getGenre());
             selectedAlbums.add(album);
             if (selectedAlbums.size() == 4) {
                 return selectedAlbums;
             }
         }
         return getFourDifferentAlbumsByDifferentGenres();
     }
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbumsByDifferentNumberOfTracks() throws DALException {
         Collection<IAlbumDescriptor> albums = getRandomAlbums(12);
         Collection<IAlbumDescriptor> selectedAlbums = new HashSet<IAlbumDescriptor>(4);
         Set<Integer> numberOfTracks = new HashSet<Integer>(4);
         for (IAlbumDescriptor album : albums) {
             List<ITrackDescriptor> albumTracks = databaseLayer.getAlbumTrackDescriptors(album);
             if (numberOfTracks.contains(albumTracks.size())) {
                     continue;
             }
             numberOfTracks.add(albumTracks.size());
             selectedAlbums.add(album);
             if (numberOfTracks.size() == 4) {
                 return selectedAlbums;
             }
         }
         return getFourDifferentAlbumsByDifferentGenres();
     }
 
     private Collection<IAlbumDescriptor> getFourDifferentAlbumsByDifferentYears() throws DALException {
         Collection<IAlbumDescriptor> albums = getRandomAlbums(12);
         Collection<IAlbumDescriptor> selectedAlbums = new HashSet<IAlbumDescriptor>(4);
         Set<Short> years = new HashSet<Short>(4);
         for (IAlbumDescriptor album : albums) {
             if (years.contains(album.getYear())) {
                 continue;
             }
             if (album.getYear() < 10) {
                 continue;
             }
             years.add(album.getYear());
             selectedAlbums.add(album);
             if (years.size() == 4) {
                 return selectedAlbums;
             }
         }
         return getFourDifferentAlbumsByDifferentGenres();
     }
 
 
     private Collection<IArtistDescriptor> getFourDifferentArtistsWithDifferentNumberOfAlbums() throws DALException {
         Collection<IArtistDescriptor> artists = getRandomArtists(12);
         Collection<IArtistDescriptor> selectedArtists = new HashSet<IArtistDescriptor>(4);
         Set<Integer> numberOfAlbums = new HashSet<Integer>(4);
         for (IArtistDescriptor artist: artists) {
             List<IAlbumDescriptor> artistAlbums = databaseLayer.getArtistAlbumDescriptors(artist);
             Integer numberOfAlbumsByCurrentArist = artistAlbums.size();
             if (numberOfAlbums.contains(numberOfAlbumsByCurrentArist)) {
                 continue;
             }
             numberOfAlbums.add(numberOfAlbumsByCurrentArist);
             selectedArtists.add(artist);
             if (numberOfAlbums.size() == 4) {
                 return selectedArtists;
             }
         }
         return getFourDifferentArtistsWithDifferentNumberOfAlbums();
     }
 
     private Collection<ITrackDescriptor> getFourTracksByDifferentArtists() throws DALException {
         Collection<ITrackDescriptor> tracks = getRandomTracks(12);
         Collection<ITrackDescriptor> selectedTracks = new HashSet<ITrackDescriptor>(4);
         Set<String> artists = new HashSet<String>(4);
         for (ITrackDescriptor track : tracks) {
             if (artists.contains(track.getArtistName())) {
                 continue;
             }
             artists.add(track.getArtistName());
             selectedTracks.add(track);
             if (artists.size() == 4) {
                 return selectedTracks;
             }
         }
         return getFourTracksByDifferentArtists();
     }
 
     private Collection<ITrackDescriptor> getFourRandomTracksByDifferentLength() throws DALException {
         Collection<ITrackDescriptor> tracks = getRandomTracks(12);
         Collection<ITrackDescriptor> selectedTracks = new HashSet<ITrackDescriptor>(4);
         Set<Short> lengths = new HashSet<Short>(4);
         for (ITrackDescriptor track : tracks) {
             if (lengths.contains(track.getLengthInSeconds())) {
                 continue;
             }
             lengths.add(track.getLengthInSeconds());
             selectedTracks.add(track);
             if (lengths.size() == 4) {
                 return selectedTracks;
             }
         }
         return getFourRandomTracksByDifferentLength();
     }
 
     private Collection<IAlbumDescriptor> getRandomAlbums(int numberOfAlbums) throws DALException {
         Random random = new Random();
         int maxId = this.databaseLayer.getAlbumCount();
         Set<Integer> randomIds = new HashSet<Integer>(numberOfAlbums);
         while (randomIds.size() < numberOfAlbums) {
             randomIds.add(random.nextInt(maxId));
         }
         Collection<IAlbumDescriptor> collection = this.databaseLayer.getAlbumDescriptors(Lists.newArrayList(randomIds));
         if (collection.size() < numberOfAlbums) {
             return this.getRandomAlbums(numberOfAlbums);
         }
         return collection;
     }
 
     private Collection<IArtistDescriptor> getRandomArtists(int numberOfArtists) throws DALException {
         Random random = new Random();
         int maxId = this.databaseLayer.getArtistCount();
         Set<Integer> randomIds = new HashSet<Integer>(numberOfArtists);
         while (randomIds.size() < numberOfArtists) {
             randomIds.add(random.nextInt(maxId));
         }
         Collection<IArtistDescriptor> collection = this.databaseLayer.getArtistDescriptors(Lists.newArrayList(randomIds));
         if (collection.size() < numberOfArtists) {
             return this.getRandomArtists(numberOfArtists);
         }
         return collection;
     }
 
     private Collection<ITrackDescriptor> getRandomTracks(int numberOfTracks) throws DALException {
         int maxId = this.databaseLayer.getTrackCount();
         Random random = new Random();
         Set<Integer> randomIds = new HashSet<Integer>(numberOfTracks);
         while (randomIds.size() < numberOfTracks) {
             randomIds.add(random.nextInt(maxId));
         }
         Collection<ITrackDescriptor> collection =  this.databaseLayer.getTrackDescriptors(Lists.newArrayList(randomIds));
         if (collection.size() < numberOfTracks) {
             return this.getRandomTracks(numberOfTracks);
         }
         return collection;
     }
 
     private ITrackDescriptor getRandomTrackFromAlbum(IAlbumDescriptor album) throws DALException {
         Random random = new Random();
         List<ITrackDescriptor> albumTracks = databaseLayer.getAlbumTrackDescriptors(album);
         int maxId = albumTracks.size();
         List<ITrackDescriptor> tracks = Lists.newArrayList(albumTracks);
         return tracks.get(random.nextInt(maxId));
     }
 
     private ITrackDescriptor getRandomTrackFromArtist(IArtistDescriptor artist) throws DALException {
         Random random = new Random();
         List <ITrackDescriptor> artistTracks = databaseLayer.getArtistTrackDescriptors(artist);
         int maxId = artistTracks.size();
         List<ITrackDescriptor> tracks = Lists.newArrayList(artistTracks);
         return tracks.get(random.nextInt(maxId));
     }
 
     private Set<Integer> getSetOfRandomNumbers(int maxId, int count) {
         Random random = new Random();
         Set<Integer> randomIds = new HashSet<Integer>(count);
         while (randomIds.size() < count) {
             randomIds.add(random.nextInt(maxId));
         }
         return randomIds;
     }
 
     private Collection<ITrackDescriptor> getThreeTracksFromArtist(IArtistDescriptor artist) throws DALException {
         Collection<ITrackDescriptor> selectedtracks = new HashSet<ITrackDescriptor>(3);
         List<ITrackDescriptor> tracks = databaseLayer.getArtistTrackDescriptors(artist);
         Set<Integer> randomIds = this.getSetOfRandomNumbers(tracks.size(), 3);
         for (int index: randomIds) {
             selectedtracks.add(tracks.get(index));
         }
         return tracks;
     }
 
     private List<ITrackDescriptor> sortTracksByTrackNumber(Collection<ITrackDescriptor> tracks) {
         List<ITrackDescriptor> sortedTracks = new ArrayList<ITrackDescriptor>();
         for (ITrackDescriptor track: tracks) {
             if (sortedTracks.contains(track)) {
                 continue;
             }
             int i=0;
             for (i=0; i<sortedTracks.size(); i++) {
                 if (sortedTracks.get(i).getTrackNum() > track.getTrackNum()) {
                     break;
                 }
             }
             sortedTracks.add(i, track);
         }
         return sortedTracks;
     }
 
     private IDAL databaseLayer;
 }
