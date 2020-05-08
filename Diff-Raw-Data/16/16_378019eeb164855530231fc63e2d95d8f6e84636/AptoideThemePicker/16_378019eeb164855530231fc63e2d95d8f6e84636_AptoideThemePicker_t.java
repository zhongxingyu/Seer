 package cm.aptoide.pt;
 
 import java.util.Locale;
 
 import android.content.Context;
 
 public class AptoideThemePicker {
 
 	public static void setAptoideTheme(Context activity) {
 
 		String theme_string = "APTOIDE_THEME_"
 				+ ApplicationAptoide.APTOIDETHEME.toUpperCase(Locale.ENGLISH);
 
 		try {
 			EnumAptoideThemes theme = EnumAptoideThemes.valueOf(theme_string);
 			switch (theme) {
 			case APTOIDE_THEME_DEFAULT:
 				activity.setTheme(R.style.Aptoide_Theme_Default);
 				break;
 			case APTOIDE_THEME_MIDNIGHT:
 				activity.setTheme(R.style.Aptoide_Theme_Midnight);
 				break;
 			case APTOIDE_THEME_MAGALHAES:
 				activity.setTheme(R.style.Aptoide_Theme_Magalhaes);
 				break;
 			case APTOIDE_THEME_MAROON:
 				activity.setTheme(R.style.Aptoide_Theme_Maroon);
 				break;
 			case APTOIDE_THEME_GOLD:
 				activity.setTheme(R.style.Aptoide_Theme_Gold);
 				break;
 			case APTOIDE_THEME_ORANGE:
 				activity.setTheme(R.style.Aptoide_Theme_Orange);
 				break;
 			case APTOIDE_THEME_SPRINGGREEN:
 				activity.setTheme(R.style.Aptoide_Theme_SpringGreen);
 				break;
 			case APTOIDE_THEME_LIGHTSKY:
 				activity.setTheme(R.style.Aptoide_Theme_LightSky);
 				break;
 			case APTOIDE_THEME_PINK:
 				activity.setTheme(R.style.Aptoide_Theme_Pink);
 				break;
 			case APTOIDE_THEME_BLUE:
 				activity.setTheme(R.style.Aptoide_Theme_Blue);
 				break;
 			case APTOIDE_THEME_RED:
 				activity.setTheme(R.style.Aptoide_Theme_Red);
 				break;
 			case APTOIDE_THEME_MAGENTA:
 				activity.setTheme(R.style.Aptoide_Theme_Magenta);
 				break;
 			case APTOIDE_THEME_SLATEGRAY:
 				activity.setTheme(R.style.Aptoide_Theme_SlateGray);
 				break;
 			case APTOIDE_THEME_SEAGREEN:
 				activity.setTheme(R.style.Aptoide_Theme_SeaGreen);
 				break;
 			case APTOIDE_THEME_SILVER:
 				activity.setTheme(R.style.Aptoide_Theme_Silver);
 				break;
 			case APTOIDE_THEME_DIMGRAY:
 				activity.setTheme(R.style.Aptoide_Theme_DimGray);
 				break;
 			case APTOIDE_THEME_TIMWE:
 				activity.setTheme(R.style.Aptoide_Theme_TimWe);
 				break;
 			case APTOIDE_THEME_DIGITALLYDIFFERENT:
 				activity.setTheme(R.style.Aptoide_Theme_DigitallyDifferent);
 				break;
 			case APTOIDE_THEME_EOCEAN:
 				activity.setTheme(R.style.Aptoide_Theme_EOcean);
 				break;
 			case APTOIDE_THEME_LAZERPLAY:
 				activity.setTheme(R.style.Aptoide_Theme_LazerPlay);
 				break;
			case APTOIDE_THEME_JBLOW:
				activity.setTheme(R.style.Aptoide_Theme_JBlow);
				break;
 			default:
 				activity.setTheme(R.style.Aptoide_Theme_Default);
 				break;
 			}
 		} catch (Exception e) {
 			activity.setTheme(R.style.Aptoide_Theme_Default);
 		}
 
 	}
 
 }
