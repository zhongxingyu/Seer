 package org.gquery.slides.client;
 
 import static com.google.gwt.query.client.GQuery.*;
 import static org.gquery.slides.client.Utils.hash;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.query.client.Predicate;
 import com.google.gwt.query.client.plugins.effects.PropertiesAnimation.Easing;
 import com.google.gwt.query.client.plugins.effects.PropertiesAnimation.EasingCurve;
 import com.google.gwt.user.client.Event;
 
 /**
  * Main class to execute a presentation
  */
 public class Slides {
 
   private static final String DISPLAY_PLAY_BUTTON = "displayPlayButton";
   private static final String CODE_SNIPPET =
     "<div class='jCode'>" +
     " <div class='jCode-scroll jCode-div'>" +
     "  <div class='jCode-lines'>" +
     "   <pre>%code%</pre>" +
     "</div></div></div>";
 
   private Easing easing = EasingCurve.easeInOutSine;// EasingCurve.custom.with(.31,-0.37,.47,1.5);
 
   private int currentPage = 1;
   private SlidesSource slidesSrc;
   private GQuery slides;
   private GQuery currentSlide = $();
 
 
   public Slides(SlidesSource presentation) {
     slidesSrc = presentation;
 
     slides = $(".slides > section")
     // build slide
     .each(new Function() {
       public void f(Element e) {
         buildSlide($(this));
       }
     })
     // remove empty slides
     .filter(new Predicate() {
       public boolean f(Element e, int index) {
         return (!$(e).html().trim().isEmpty());
       }
     });
 
     bindEvents();
 
     showCurrentSlide();
   }
 
   private void showCurrentSlide() {
     // compute current page based on hash
     String hash = hash();
     currentPage = hash.matches("\\d+") ? Integer.parseInt(hash) : 0;
 
     // update page elements
     console.clear();
     $("#play").hide();
     $("#marker").text("" + currentPage);
     currentSlide.trigger(SlidesSource.LEAVE_EVENT_NAME);
 
     // move slides to left out of the window view port
     // FIXME: gQuery animations seems not working with percentages, it should be -150% and 150%
     int w = $(window).width();
     slides.lt(currentPage).stop(true).animate($$("left: -" + w), 2000, easing);
     // move slides to right out of the window view port
     slides.gt(currentPage).stop(true).animate($$("left: +" + w), 2000, easing);
 
     // move current slide to the window view port
     currentSlide = slides.eq(currentPage).stop(true).animate($$("left: 0"), 2000, easing);
 
     // display the button to execute the snippet
     if (currentSlide.data(DISPLAY_PLAY_BUTTON, Boolean.class)) {
       // wait until the animation has finished, then show the button and move it.
       currentSlide.delay(0, movePlayButtonFunction).trigger(SlidesSource.ENTER_EVENT_NAME);
     }
   }
 
   private Function movePlayButtonFunction = new Function() {
     public void f() {
      GQuery currentCode = currentSlide.find(".code");
       int left = currentCode.offset().left + currentCode.width() - 50;
       int top = currentCode.offset().top;
       // TODO: gQuery.offset(top, left) does not work and sets negative values
       // although we are passing positive numbers.
       $("#play").css("top", top + "px").css("left", left + "px").fadeIn();
     }
   };
 
   private void bindEvents() {
     $(window)
     // handle key events to move slides back/forward
     .bind(Event.ONKEYDOWN, new Function() {
       public boolean f(Event e) {
         int code = e.getKeyCode();
         if (code == KeyCodes.KEY_RIGHT || code == ' ') {
           show(true);
         }
         if (code == KeyCodes.KEY_LEFT || code == KeyCodes.KEY_BACKSPACE) {
           show(false);
         }
         return false;
       }
     })
     // handle hash change to select the appropriate slide
     .bind("hashchange", new Function() {
       public void f() {
         showCurrentSlide();
       }
     })
     .bind("resize", movePlayButtonFunction);
 
     $("#play").click(new Function() {
       public void f() {
         slidesSrc.exec(currentSlide.id());
       }
     });
   }
 
   private void buildSlide(GQuery slide) {
     String html = slide.html();
     String id = slide.id().toLowerCase();
 
     String javadoc = slidesSrc.docs.get(id);
     String code = slidesSrc.snippets.get(id);
 
     if (javadoc != null){
       html += javadoc;
     }
 
     boolean displayPlayButton = true;
     if (code != null && code.trim().length() > 0) {
       html += CODE_SNIPPET.replace("%code%", Prettify.prettify(code));
     } else {
       displayPlayButton = false;
     }
 
     slide.data(DISPLAY_PLAY_BUTTON, displayPlayButton);
     slide.html(html);
   }
 
   private void show(boolean forward) {
     int incr = forward ? 1 : -1;
     int nextPage = Math.min(Math.max(currentPage + incr, 0), slides.size() - 1);
 
     if (nextPage != currentPage) {
       hash(nextPage);
     }
   }
 }
