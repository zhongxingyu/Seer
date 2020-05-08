 package fr.devoxx.selenium.ui;
 
 import fr.devoxx.selenium.PhantomJsTest;
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class DevoxxFr2013Test extends PhantomJsTest {
 
     @Override
     public String defaultUrl() {
         return "http://binout.github.com/devoxxfr-2013";
     }
 
     @Test
     public void planning_page_should_display_navbar() {
         goTo("/");
 
         assertThat(find(".brand").getText()).contains("Devoxxfr 2013");
     }
 
     @Test
     public void planning_page_should_display_search() {
         goTo("/");
 
         assertThat(find(".control-label").getText()).contains("Recherche");
     }
 
     @Test
     public void planning_page_should_display_table() {
         goTo("/");
 
         assertThat(find("table tr", 0).getText()).contains("Type Track Title Speaker Experience Language");
     }
 
     @Test
     public void planning_page_should_search_xavier_hanin() {
         goTo("/");
 
         find("#inputQuery").text("xavier hanin");
         assertThat(find("table tr", 1).getText()).contains("Xavier Hanin");
     }
 
 
     @Test
     public void speakers_page_should_display_table() {
         goTo("/speakers.html");
 
         assertThat(find("table tr", 0).getText()).contains("Name Company Twitter");
     }
 
     @Test
     public void speakers_page_should_search_xavier_hanin() {
         goTo("/speakers.html");
 
         find("#inputQuery").text("hanin");
         assertThat(find("table tr", 1).getText()).contains("Xavier Hanin 4SH FRANCE @xavierhanin");
     }
 
     @Test
     public void talk_page_should_display_xhanin_talk() {
        goTo("/talk.html#?id=2740");
 
         assertThat(find("h1").getText()).contains("IDE Java");
     }
 
     @Test
     public void speaker_page_should_display_xhanin_bio() {
         goTo("/speaker.html#?id=924");
 
         assertThat(find("h1").getText()).contains("Xavier Hanin");
     }
 }
