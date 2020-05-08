 package com.wedlum.styleprofile.domain.survey;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.wedlum.styleprofile.domain.photo.Photo;
 import com.wedlum.styleprofile.domain.photo.PhotoSource;
 
 public class StyleProfiler {
 
 	private final Profile profile;
 
 	public StyleProfiler(Profile profile, PhotoSource photoSource) {
 		this.profile = profile;
 		profile.photoSource = photoSource;
 	}
 
     private static String selectPhotoFeaturingColor(PhotoSource photoSource, String color) {
     	for (String photo : photoSource.getPhotos()) {
     		Photo newPhoto = new Photo(photo, photoSource.getMetadata(photo));
 			if (newPhoto.getFeaturedColors().contains(color))
                 return photo;
     	}
 
     	return "No photo found featuring color: " + color;
     }
 
 	public String style() {
 		return styleAdjective() +  " " + styleNoun();
 	}
 
 	// --- This code should be removed from here ---
 
 	private String styleAdjective() {
 		Map<String, String> adjectiveByPaletteName = new LinkedHashMap<String, String>();
 
 		adjectiveByPaletteName.put("Mono", "Reserved");
 		adjectiveByPaletteName.put("Analog", "Chic");
 		adjectiveByPaletteName.put("Comp", "Bold");
 		adjectiveByPaletteName.put("Triad", "Whimsical");
 		adjectiveByPaletteName.put("Neutral", "Flexible");
 		adjectiveByPaletteName.put("Free-Form", "Eclectic");
 
     	return adjectiveByPaletteName.get(highestRankedPalette());
 	}
 
 	private String highestRankedPalette() {
 		// TODO: get this palette based on profile info
 		return "paletteWithHighestScore";
 	}
 
     private String styleNoun() {
     	if (isHighSaturation() || isBold() || isWildTexture()) {
     		if (isHighContrast())
     			return "Modernist";
     		else
     			return "Sophisticate";
     	} else {
     		if (isDarkColorValue())
     			return "Traditional";
     		else
     			return "Romantic";
     	}
 	}
 
 	private boolean isDarkColorValue() {
 		int dCount = numberOfDarkColorLikes();
 		int lCount = numberOfLightColorLikes();
 
 		return (dCount - lCount) > 0 ? true : false;
 	}
 
     private int numberOfLightColorLikes() {
     	// TODO calculate based on profile info
 		return 0;
 	}
 
 	private int numberOfDarkColorLikes() {
 		// TODO calculate based on profile info
 		return 0;
 	}
 
 	private boolean isHighContrast() {
 		// TODO calculate based on profile info
     	return (highContrast() - lowContrast()) > 0;
 	}
 
 	private int lowContrast() {
 		// TODO calculate based on profile info
 		return 0;
 	}
 
 	private int highContrast() {
 		// TODO calculate based on profile info
 		return 10;
 	}
 
 	private boolean isHighSaturation() {
 		int highSaturation = highSaturation();
 
 		if (lowSaturation() > highSaturation) return false;
 		if (midSaturation() > highSaturation) return false;
 
 		return true;
 	}
 
 	private int lowSaturation() {
 		// TODO: calculate based on profile info
 		return 0;
 	}
 
 	private int midSaturation() {
 		// TODO: calculate based on profile info
 		return 5;
 	}
 
 
 	private int highSaturation() {
 		// TODO: calculate based on profile info
 		return 10;
 	}
 
 	private boolean isBold() {
 		int highBoldness = highBoldness();
 
 		if (lowBoldness() > highBoldness) return false;
 		if (midBoldness() > highBoldness) return false;
 
 		return true;
 	}
 
 	private int lowBoldness() {
 		// TODO: calculate based on profile info
 		return 0;
 	}
 
 	private int midBoldness() {
 		// TODO: calculate based on profile info
 		return 5;
 	}
 
 
 	private int highBoldness() {
 		// TODO: calculate based on profile info
 		return 10;
 	}
 
 	private boolean isWildTexture() {
 		int wildTexture = wildTexture();
 
 		if (softTexture() > wildTexture) return false;
 		if (geoTexture() > wildTexture) return false;
 
 		return true;
 	}
 
 	private int softTexture() {
 		// TODO: calculate based on profile info
 		return 0;
 	}
 
 	private int geoTexture() {
 		// TODO: calculate based on profile info
 		return 5;
 	}
 
 	private int wildTexture() {
 		// TODO: calculate based on profile info
 		return 10;
 	}
 
 	// --- End ---
 	
 	public Map<String, String> resolveAll() {
         LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
         addMiniPalettes(result);
         addPalettes(result);
         return result;
     }
 
     private void addMiniPalettes(LinkedHashMap<String, String> result) {
         int miniPaletteIndex = 0;
 
         List<String> allMiniPalettes = new ArrayList<String>();
         allMiniPalettes.addAll(miniPalettesFor("singleColorSession1", "_A"));
         allMiniPalettes.addAll(miniPalettesFor("singleColorSession2", "_A"));
         allMiniPalettes.addAll(miniPalettesFor("singleColorSession3", "_A"));
 
         allMiniPalettes.addAll(miniPalettesFor("singleColorSession1", "_C"));
         allMiniPalettes.addAll(miniPalettesFor("singleColorSession2", "_C"));
         allMiniPalettes.addAll(miniPalettesFor("singleColorSession3", "_C"));
 
         for (String miniPalette : allMiniPalettes)
             result.put("miniPalette" + ++miniPaletteIndex, miniPalette);
     }
 
     private void addPalettes(LinkedHashMap<String, String> result) {

        List<String> allColors = getColors(profile.getPhotos());
        List<String> likes = getColors(profile.getLikedPhotos());

         List<String> ranked = new ColorRank(profile.photoSource).rankColors(profile.allSessionsObject());
 
         int i = 1;
         for (String color : ranked)
             result.put("palette" + i++, selectPhotoFeaturingColor(profile.photoSource, color));
     }
 
    private List<String> getColors(List<Photo> photos) {
        List<String> colors = new ArrayList<String>();
        for(Photo photo : photos)
            colors.addAll(photo.getColors());

        return colors;
    }

     private List<String> miniPalettesFor(String sessionName, String suffix) {
         if (!profile.hasSession(sessionName))
             return new ArrayList<String>();
 
         List<String> result = new ArrayList<String>();
         for (String item : getSession(sessionName))
             result.add(item.replace(".png", suffix + ".png"));
 
         return result;
     }
 
 	private List<String> getSession(String sessionName) {
         return profile.getSession(sessionName);
     }
 
 }
