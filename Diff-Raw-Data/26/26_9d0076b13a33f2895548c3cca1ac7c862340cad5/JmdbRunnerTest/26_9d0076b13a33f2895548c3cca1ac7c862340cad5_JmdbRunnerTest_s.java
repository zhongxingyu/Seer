 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package sk.mortality.util.imdb;
 
 import java.util.Set;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import sk.mortality.util.imdb.Jmdb.Actor;
 import sk.mortality.util.imdb.Jmdb.Certificate;
 import sk.mortality.util.imdb.Jmdb.Director;
 import sk.mortality.util.imdb.Jmdb.OfficialSite;
 import sk.mortality.util.imdb.Jmdb.Rating;
 import sk.mortality.util.imdb.Jmdb.Status;
 import sk.mortality.util.imdb.Jmdb.Writer;
 
 /**
  *
  * @author root
  */
 public class JmdbRunnerTest {
 
     public JmdbRunnerTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void run() throws Exception {
         Jmdb api = new Jmdb();
        api.search("\"Robot Chicken\"");
         if (api.getStatus() == Status.KO) {
             System.out.println("No matches found");
             System.exit(0);
         }
         System.out.println("Matches: ");
         for (String title : api.getMatchedTitles().values()) {
             System.out.println(title);
         }
         System.out.println(api.getMovieID());
         System.out.println(api.getTitle());
         System.out.println(api.getYear());
         System.out.println(api.getPlot());
 
         System.out.println();
         System.out.println("Actors:");
         Set<Actor> cast = api.getCast();
         if (cast != null) {
             for (Actor actor : cast) {
                 System.out.print(actor.getId()+" ");
                 System.out.print(actor.getName()+" ");
                 System.out.print(actor.getCharacter());
                 System.out.println();
             }
         }
         System.out.println();
         System.out.println("Directors:");
         Set<Director> directors = api.getDirectors();
         if (directors != null) {
             for (Director director : directors) {
                 System.out.print(director.getId()+" ");
                 System.out.print(director.getName()+" ");
                 System.out.println();
             }
         }
         System.out.println();
         System.out.println("Writers:");
         Set<Writer> writers = api.getWriters();
         if (writers != null) {
             for (Writer writer : writers) {
                 System.out.print(writer.getId()+" ");
                 System.out.print(writer.getName()+" ");
                 System.out.println();
             }
         }
         System.out.println();
         System.out.println("Cover: "+api.getCover());
         System.out.println();
         System.out.println("Language: ");
         String[] language = api.getLanguage();
         if (language != null) {
             for (String lang : language) {
                 System.out.println(lang);
             }
         }
         System.out.println();
         System.out.println("Country: ");
         String[] country = api.getCountry();
         if (country != null) {
             for (String cnt : country) {
                 System.out.println(cnt);
             }
         }
         System.out.println();
         Rating rating = api.getRating();
         if (rating != null) {
             System.out.println("Rating: "+rating.getRating()+"/10");
             System.out.println("Votes: "+rating.getVotes());
         }
         System.out.println();
         System.out.println("Genre: ");
         String[] genre = api.getGenre();
         if (genre != null) {
             for (String gnr : genre) {
                 System.out.println(gnr);
             }
         }
         System.out.println();
         System.out.println("Tagline: "+api.getTagline());
         System.out.println();
         System.out.println("AKAs: ");
         String[] aka = api.getAka();
         if (aka != null) {
             for (String ak : aka) {
                 System.out.println(ak);
             }
         }
         System.out.println();
         System.out.println("Certifications: ");
         Set<Certificate> certifications = api.getCertifications();
         if (certifications != null) {
             for (Certificate cert : certifications) {
                 System.out.print(cert.getCountry()+":");
                 System.out.print(cert.getCertification());
                 System.out.println();
             }
         }
         System.out.println();
         System.out.println("Runtime: "+api.getRuntime());
         System.out.println();
         System.out.println("Fullplot: "+api.getFullPlot());
         System.out.println();
         System.out.println("Trivia: "+api.getTrivia());
         System.out.println();
         System.out.println("Goofs: "+api.getGoofs());
         System.out.println();
         System.out.println("Awards: "+api.getAwards());
         System.out.println();
         System.out.println("Official sites: ");
         Set<OfficialSite> sites = api.getOfficialSites();
         if (sites != null) {
             for (OfficialSite site : sites) {
                 System.out.print(site.getTitle()+" - ");
                 System.out.print(site.getUrl());
                 System.out.println();
             }
         }
     }
 
 }
