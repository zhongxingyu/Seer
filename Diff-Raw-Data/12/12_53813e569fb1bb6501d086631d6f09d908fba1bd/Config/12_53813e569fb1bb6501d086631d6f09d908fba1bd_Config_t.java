 package application;
 
 import java.awt.Dimension;
 
 /**
  * This class contains project wide configuration settings.
  * @author Roland van der Linden
  *
  */
 public class Config
 {
	public final static String appname = "Serious Gaming SmartCity - Happyzone";
 	public final static Dimension appsize = new Dimension(1100, 800);
 	public final static int outerBorderSize = 2;
 	
 	public final static int fundsGainPerAcceptedProduct = 2;
 	public final static int initialDistrictHappiness = 30;
 	public final static int happinessGainAcceptedNormal = 4;
 	public final static int happinessGainAcceptedSpecial = 5;
 	public final static int happinessLossNeglected = 5;
 	public final static int happinessLossRejected = 2;
 	
 	public final static String endOfRoundImagePath = "";
 	public final static String endOfRoundImageName = "End of Year ";
 	
 	//Startup data
 	public final static String[] teamNames = { "CircaCorp", "HexaTech", "QuadCore Inc" };
 	public final static String[] districtNames = { "Estermondt", "Grote Beek", "De Hoven" };
 	public final static int centrumDistrictIndex = 0;
 	public final static String[] technologyNames = { "CircaCorp Technology", "HexaTech Technology", "QuadCore Technology" }; 
 	public final static String[] improvementNames = { "Userfriendlyness", "Moneysaver" };
 	
 	
 }
