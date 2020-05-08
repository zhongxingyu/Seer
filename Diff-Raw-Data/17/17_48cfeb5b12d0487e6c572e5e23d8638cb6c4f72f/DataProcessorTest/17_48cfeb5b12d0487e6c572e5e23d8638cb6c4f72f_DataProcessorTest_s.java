 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.weltraumschaf.juberblog.cmd;
 
 import com.beust.jcommander.internal.Maps;
 import de.weltraumschaf.juberblog.MetaData;
 import de.weltraumschaf.juberblog.formatter.SiteFormatter;
 import de.weltraumschaf.juberblog.template.Configurations;
 import freemarker.template.TemplateException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Map;
 import org.junit.Test;
 import org.junit.Before;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 /**
  * Tests for {@link DataProcessor}.
  *
  * @author Sven.Strittmatter
  */
 public class DataProcessorTest {
 
     private DataProcessor sut;
 
     @Before
     public void createSut() throws IOException, URISyntaxException {
         sut = new DataProcessor(
             getClass().getResourceAsStream("/de/weltraumschaf/juberblog/cmd/data.md"),
             new SiteFormatter(Configurations.forTests(Configurations.SCAFFOLD_TEMPLATE_DIR)),
             "http://www.foobar.com/"
         );
     }
 
     @Test
     public void getHtml() throws IOException, TemplateException {
         assertThat(sut.getHtml(), is(equalTo("<!DOCTYPE html>\n"
                 + "<!--[if lt IE 7]>      <html class=\"no-js lt-ie9 lt-ie8 lt-ie7\"> <![endif]-->\n"
                 + "<!--[if IE 7]>         <html class=\"no-js lt-ie9 lt-ie8\"> <![endif]-->\n"
                 + "<!--[if IE 8]>         <html class=\"no-js lt-ie9\"> <![endif]-->\n"
                 + "<!--[if gt IE 8]><!--> <html class=\"no-js\"> <!--<![endif]-->\n"
                 + "    <head>\n"
                 + "        <title>The Headline</title>\n"
                 + "        <meta name=\"description\" content=\"My personal projects I'm working on\"/>\n"
                 + "        <meta name=\"keywords\" content=\"Projects, Jenkins, Darcs\"/>\n"
                 + "        <meta charset=\"UTF-8\"/>\n"
                 + "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\"/>\n"
                 + "        <meta name=\"viewport\" content=\"width=device-width\"/>\n"
                 + "\n"
                 + "        <link rel=\"stylesheet\" type=\"text/css\"\n"
                 + "              href=\"http://fonts.googleapis.com/css?family=Source+Sans+Pro:400,200,200italic,"
                 + "400italic,700,700italic|Source+Code+Pro\"/>\n"
                 + "        <link rel=\"stylesheet\" href=\"http://www.foobar.com/css/normalize.min.css\" "
                 + "type=\"text/css\"/>\n"
                 + "        <link rel=\"stylesheet\" href=\"http://www.foobar.com/css/main.css\" type=\"text/css\"/>\n"
                 + "        <link rel=\"stylesheet\" href=\"http://www.foobar.com/css/custom.css\" type=\"text/css\"/>\n"
                 + "        <link rel=\"shortcut icon\" href=\"http://www.foobar.com/img/favicon.ico\" "
                 + "type=\"image/x-icon\"/>\n"
                 + "        <style>\n"
                 + "\n"
                 + "        </style>\n"
                 + "    </head>\n"
                 + "    <body>\n"
                 + "        <article>\n"
                 + "  <h2>The Headline</h2><p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                 + "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et "
                 + "accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est "
                 + "Lorem ipsum dolor sit amet.</p><p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam "
                 + "nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero "
                 + "eos et accusam et justo duo dolores et ea rebum.</p>\n"
                 + "</article>\n"
                 + "    </body>\n"
                 + "</html>")));
     }
 
     @Test
     public void getSlug() throws IOException {
         assertThat(sut.getSlug(), is(equalTo("The-Headline")));
     }
 
     @Test
     public void getMetaData() throws IOException {
         final Map<String, String> data = Maps.newHashMap();
         data.put("Navi", "Projects");
         data.put("Description", "My personal projects I'm working on");
         data.put("Keywords", "Projects, Jenkins, Darcs");
         assertThat(sut.getMetaData(), is(equalTo(new MetaData(data))));
     }
 
     @Test
     public void getHeadline() throws IOException {
         assertThat(sut.getHeadline(), is(equalTo("The Headline")));
     }
 
}
