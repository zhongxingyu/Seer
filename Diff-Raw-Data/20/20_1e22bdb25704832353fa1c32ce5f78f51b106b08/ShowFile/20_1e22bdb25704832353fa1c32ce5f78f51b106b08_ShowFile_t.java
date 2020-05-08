 //=============================================================================
// ShowFile.java
 //=============================================================================
 
 //===============================
 // Imports
 //===============================
import java.io.File;
 
public class ShowFile extends TFile
 {
     //===============================================================
     //      Constants
     //===============================================================
 
 
     //===============================================================
     //      Instance Variables
     //===============================================================
     private int    seasonNumber;
     private int    episodeNumber;
    private String showTitle;
 
     //===============================================================
     //      Constructors
     //===============================================================
 
    public ShowFile(File file, String name, String extension, int season, int episode, String title)
     {
         super(file, name, extension);
 
         seasonNumber  = season;
         episodeNumber = episode;
         showTitle     = title;
     }
 
     //===============================================================
     //      getters / setters
     //===============================================================
 
     //=================================
     //      seasonNumber
     //=================================
 
     public int getSeasonNum()
     {
         return seasonNumber;
     }
 
     public void setSeasonNum(int season)
     {
         seasonNumber = season;
     }
 
     //=================================
     //      episodeNumber
     //=================================
 
    public int getEpisodeNum()
     {
        return episodeNumber;
     }
 
 
     public void setEpisodeNum(int episode)
     {
         episodeNumber = episode;
     }
     
     //=================================
     //      showTitle
     //=================================
 
     public String getShowTitle()
     {
         return showTitle;
     }
 
 
     public void setShowTitle(String title)
     {
         showTitle = title;
     }
     
     //===============================================================
     //      conditionals
     //===============================================================
 
     public boolean isShow() { return true; }
 
 
 }
