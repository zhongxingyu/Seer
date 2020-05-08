 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package de.weltraumschaf.juberblog;
 
 import java.io.IOException;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 import org.junit.Before;
 
 /**
  * Tests for {@link BlogConfiguration}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class BlogConfigurationTest {
 
     private static final String DIR_SEP = Constants.DIR_SEP.toString();
     private static final String PACKAGE = "src.main.resources."
             + Constants.SCAFFOLD_PACKAGE.toString() + ".configuration";
 
     private BlogConfiguration sut;
 
     @Before
     public void createSut() throws IOException {
         final String baseDir = System.getProperty("user.dir");
         final String fileName = baseDir + DIR_SEP
                 + PACKAGE.replace(".", DIR_SEP)
                + DIR_SEP + "configuration.properties";
         sut = new BlogConfiguration(fileName);
         sut.load();
     }
 
     @Test
     public void getProperties() {
         assertThat(sut.getHeadline(), is(equalTo("Das Weltraumschaf")));
         assertThat(sut.getDescription(), is(equalTo("The Music Making Space Animal")));
         assertThat(sut.getSiteUri(), is(equalTo("http://uberblog.local/")));
         assertThat(sut.getLanguage(), is(equalTo("en")));
         assertThat(sut.getDataDir(), is(equalTo("/data")));
         assertThat(sut.getTemplateDir(), is(equalTo("/templates")));
         assertThat(sut.getHtdocs(), is(equalTo("/public")));
         assertThat(sut.getTwitterConsumerKey(), is(equalTo("foo")));
         assertThat(sut.getTwitterConsumerSecret(), is(equalTo("bar")));
         assertThat(sut.getTwitterOAuthToken(), is(equalTo("baz")));
         assertThat(sut.getTwitterOAuthTokenSecret(), is(equalTo("snafu")));
         assertThat(sut.getBitlyUsername(), is(equalTo("foo")));
         assertThat(sut.getBitlyApikey(), is(equalTo("bar")));
         assertThat(sut.getApiUri(), is(equalTo("http://uberblog.local/api/")));
         assertThat(sut.getFeatureRating(), is(true));
         assertThat(sut.getFeatureComments(), is(false));
     }
 
 }
