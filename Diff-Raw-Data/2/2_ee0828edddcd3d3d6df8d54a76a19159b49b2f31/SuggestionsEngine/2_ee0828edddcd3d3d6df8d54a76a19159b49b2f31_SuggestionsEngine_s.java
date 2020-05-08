 /*
  * Weazzer Android Application
  * 
  */
 package weazzer.wear;
 
 import java.util.ArrayList;
 
 import weazzer.wear.ClothingArticle.UserSex;
 import weazzer.weather.WeatherData;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /**
  * The Class SuggestionEngine that provides the best match for a clothing
  * article, given the weather conditions. For each of the four clothing
  * categories (Pants, Shirt, Overcoat and Accessories), the SuggestionEngine
  * should keep different variables/parameters for the user's preferences.
  */
 public class SuggestionsEngine {
 	
 	private final Float TEMPERATURE_OFFSET = 20.f;
 	
 	/** The pants heat factor. */
 	private float bottomHF;
 	
 	/** The shirts heat factor. */
 	private float topHF;
 	
 	/** The overcoat heat factor. */
 	private float overcoatHF;
 	
 	/** The accessories heat factor. */
 	private float accessoriesHF;
 	
 	/** The context. */
 	private Context context;
 	
 	public SuggestionsEngine(Context context)
 	{
 		this.context=context;
 		loadUserPreferences();
 	}
 
 	private int getBestFit(WeatherData weather, ArrayList<ClothingArticle> suggestions, float factor)
 	{
 		int bestIndex = suggestions.size()-1;
 		float bestValue = 99999.f;
 		
 		float temp = weather.feelsLike + TEMPERATURE_OFFSET;
 		for (int i = 0; i < suggestions.size(); ++i) {
 			ClothingArticle a = suggestions.get(i);
 			float f = (a.getHeatFactor() + TEMPERATURE_OFFSET) * factor;
 			if (Math.abs(f-temp) < bestValue) {
 				bestIndex = i;
 				bestValue = Math.abs(f-temp);
 			}
 		}
 		
 		return bestIndex;
 	}
 	
 	/**
 	 * Gets the suggestion for a specific type of article, based on the given
 	 * articles list and the weather information.
 	 *
 	 * @param weather the weather
 	 * @param gender the gender
 	 * @return the suggestion
 	 */
 	public ClothesSuggestion getSuggestion(WeatherData weather, UserSex gender) {
 		ClothesProvider clothesProvider = new ClothesProvider();
 		ClothesSuggestion CS = new ClothesSuggestion();
 		
 		CS.setTopSuggestions(clothesProvider.getShirts(gender));		
 		CS.setBottomSuggestions(clothesProvider.getPants(gender));
 		CS.setOvercoatSuggestions(clothesProvider.getOvercoats(gender));
 		
 		CS.setTopIndex(getBestFit(weather, clothesProvider.getShirts(gender), topHF));
 		CS.setBottomIndex(getBestFit(weather, clothesProvider.getPants(gender), bottomHF));
 		CS.setOvercoatIndex(getBestFit(weather, clothesProvider.getOvercoats(gender), overcoatHF));
 		
 		CS.setAccessoriesSuggestions(clothesProvider.getAccessories(gender));
 		
 		ArrayList<Boolean> accesoriesSelect = new ArrayList<Boolean>();
 		accesoriesSelect.add(true);
 		accesoriesSelect.add(false);
 		accesoriesSelect.add(true);
 		CS.setAccessoriesSelect(accesoriesSelect);
 		return CS;
 	}
 
 	/**
 	 * Updates the SuggestionEngine's internal variables according to the user
 	 * choice for the weather.
 	 *
 	 * @param currentWeatherData the current weather data
 	 * @param clothesSuggestion the clothes suggestion
 	 */
 	public void updateUserChoice(WeatherData currentWeatherData, ClothesSuggestion clothesSuggestion) {
 		
 		//Update the factors
 		this.topHF = getNewHF(currentWeatherData, clothesSuggestion.getTopSuggestions().get(
 				clothesSuggestion.getTopIndex()), topHF);
 		this.bottomHF = getNewHF(currentWeatherData, clothesSuggestion.getBottomSuggestions().get(
 				clothesSuggestion.getBottomIndex()), bottomHF);
 		this.overcoatHF = getNewHF(currentWeatherData, clothesSuggestion.getOvercoatSuggestions().get(
 				clothesSuggestion.getOvercoatIndex()), overcoatHF);
 		
 		//Save the preferences
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		prefs.edit().putFloat("userPreferenceTopHF", this.topHF);
 		prefs.edit().putFloat("userPreferenceBottomHF", this.bottomHF);
 		prefs.edit().putFloat("userPreferenceOvercoatHF", this.overcoatHF);
 		prefs.edit().putFloat("userPreferenceAccessoriesHF", this.accessoriesHF);
 	}
 	
 	/**
 	 * Gets the new heat factor according to the user choice for the weather.
 	 *
 	 * @param currentWeatherData the current weather data
 	 * @param article the article
 	 * @return the new heat factor
 	 */
 	private float getNewHF(WeatherData currentWeatherData, ClothingArticle article, float currentHF)
 	{
		float raport=currentWeatherData.feelsLike/article.getHeatFactor();
 		return currentHF*0.2f + raport*0.8f;
 	}
 	
 	/**
 	 * Resets the AI internal parameters for clothing suggestions to factory settings.
 	 */
 	public void resetFactorySettings()
 	{
 		bottomHF=topHF=overcoatHF=accessoriesHF=1.0f;
 	}
 	
 	/**
 	 * Loads the heat factors from the user preferences.
 	 */
 	private void loadUserPreferences()
 	{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		bottomHF=prefs.getFloat("userPreferenceBottomHF", 1.0f);
 		topHF=prefs.getFloat("userPreferenceShirtHF", 1.0f);
 		overcoatHF=prefs.getFloat("userPreferenceOvercoatHF", 1.0f);
 		accessoriesHF=prefs.getFloat("userPreferenceAccessoriesHF", 1.0f);
 	}
 
 }
