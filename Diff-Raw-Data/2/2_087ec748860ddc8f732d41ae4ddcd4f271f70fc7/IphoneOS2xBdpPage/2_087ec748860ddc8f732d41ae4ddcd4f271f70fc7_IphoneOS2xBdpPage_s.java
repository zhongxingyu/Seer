 package au.com.sensis.mobile.crf.showcase.selenium.fixture;
 
 import com.thoughtworks.selenium.Selenium;
 
 /**
  * Assertions and actions for the BDP Page.
  *
  * @author Adrian.Koh2@sensis.com.au (based on Heather's work in Whereis Mobile)
  */
 public class IphoneOS2xBdpPage extends IphoneBdpPage {
 
     /**
      * Default constructor.
      *
      * @param selenium Selenium instance to use.
      */
     public IphoneOS2xBdpPage(final Selenium selenium) {
         super(selenium);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doAssertImg() {
         assertImgFoundInNonDefaultGroupPngFormat();
 
         assertScaledYellowPagesImage(320, 128, "png");
         assertScaledYellowPagesImagePath(320, 128, "png");
         assertScaledSearchImage(55, 65, "png", "default");
     }
 
     private void assertImgFoundInNonDefaultGroupPngFormat() {
         assertImg("unmetered img not found", "unmeteredImg", "Unmetered", "Unmetered",
                "iphone-ipod-os2/selenium/common/unmetered.png", 155 / getImageDimensionsDivisor(),
                 21 / getImageDimensionsDivisor());
     }
 
     /**
      * @return number of expected img elements.
      */
     @Override
     protected int getExpectedNumImgElements() {
         return super.getExpectedNumImgElements() + 3;
     }
 
 }
