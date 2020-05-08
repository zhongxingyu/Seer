 
 import models.SurferDB;
 import play.Application;
 import play.GlobalSettings;
 import views.formdata.SurferFormData;
 
 
 
 /**
  * Overrides the startup.
  * @author Owner
  *
  */
 public class Global extends GlobalSettings {
   
   /**
    * Initializes the surfers upon startup.
    * @param app The application.
    */
   public void onStart(Application app) {
     
     String kellyBio = "In addition to the ASP tour, Slater has also competed in the X-Games (in 2003 and 2004). After " 
         + "earlier being awarded the title prematurely as a result of a miscalculation by the ASP, on November 6, 2011" 
         + " Slater officially won his eleventh ASP world title at the Rip Curl Pro Search San Francisco, by winning his"
         + " 4th round heat. In May 2005, in the final heat of the Billabong Tahiti Pro contest at Teahupo'o, Slater "
         + "became the first surfer ever to be awarded two perfect scores for a total 20 out of 20 points under the ASP "
         + "two-wave scoring system. (The corresponding honor under the previous three-wave system belongs to fellow "
         + "American Shane Beschen who achieved the feat in 1996.) He did it again on June 2013 at the quarter finals "
         + "at the Volcom Fiji Pro with two perfect ten waves, only the fourth person in history to do so.[3] Since "
         + "1990, Slater has been sponsored primarily by surfwear industry giant Quiksilver.[4] He exclusively rides "
         + "Channel Islands surfboards equipped with his own signature series of FCS fins.";
     
     String daizeBio = "The two time world champion longboard surfer was featured in Maxim and two issues of ReadyGo " 
         + "Magazine Japan in which she had 16 full-page color photos of her modelling alongside Britney Spears and "
         + "others. She has also been featured in numerous other international magazines. Daize said she was born "
         + "April 25, 1978 in Ukiah, CA. She said, 'My full name is Sarah Rose Shayne, but Daize is the nickname that's "
         + "stuck'. She is currently married.";
     
     String johnBio = "John John Florence , the oldest of three children, grew up in Hale'iwa, in an ocean-side house "
         + "at the Banzai Pipeline. He resides with his mother, Alexandra, and his two younger brothers Nathan and "
         + "Ivan. He was introduced to surfing by Alexandra, who is a surfer herself. Florence claims he rode a surf " 
         + "board at the age of six months, wearing a life vest on his father John's board; he was riding on his own "
         + "by the age of three.";
     
     
     SurferDB.addSurfer(new SurferFormData(
         "Daize Shayne",
         "Uklah, CA",
         "2004 World Longboard Champion, 1999 World Longboard Champion",
        "http://www.cgweekly.com/archives/1/sabine/sabine/9DaizeShayne00010.jpg",
        "http://userserve-ak.last.fm/serve/500/73287524/Daize+Shayne.jpg",
         daizeBio,
         "daizeshayne",
         "Female"));
     
     SurferDB.addSurfer(new SurferFormData(
         "John Florence",
         "Honolulu, HI",
         "Vans World Cup of Surfing (1st Place, 2011)",
         "http://stwww.surfingmagazine.com/wp-content/blogs.dir/1/files/2012/10/john-john-florence-domenic-mosqueira.jpg"
         ,
         "http://media.outsideonline.com/images/john-john-florence_fe.jpg",
         johnBio,
         "johnflorence",
         "Grom"));
     
    SurferDB.addSurfer(new SurferFormData(
        "Kelly Slater",
        "Cocoa Beach, Florida",
        "ASP World Tour Champion (11 times), Boost Mobile Pro (1st Place, 2007)",
        "http://costaricasurfing.org/wp-content/uploads/2013/07/Kelly-Slater-1.jpg",
        "http://www.athletepromotions.com/blog/wp-content/uploads/2013/08/kelly-slater.jpg",
        kellyBio,
        "kellyslater",
        "Male"));
   }
 
 }
