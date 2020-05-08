 package com.example.esbee;
 
 
 
 public class PresentPackage 
 {
 
 	/**
 	 * @param args
 	 * Present Packages hold information about the 4 sounds that should be loaded. This 
 	 * includes the package title, button labels, and the name of the mp3 files.
 	 */
 
 	//This is the title of the whole package. ie "Drum Set" if the package contains drum sounds.
	String packageTitle = "Initial Package Title";
 
 	// The button labels for the buttons. 
 	// one -> upper left, two -> upper right
 	// three -> lower left, four -> lower right
 	String buttonOneLabel = "Initial one";
 	String buttonTwoLabel = "Initial two";
 	String buttonThreeLabel = "Initial three";
 	String buttonFourLabel = "Initial four";
 
 	// The mediaPlayer objects that are used to play the mp3 files
 	int soundOne, soundTwo, soundThree, soundFour; 
 	
 	// Constructor method for PresentPackage
 	public PresentPackage()
 	{
 		
 		// The R id for the mp3 files. Default values
 		this.soundOne = R.raw.five;
 		this.soundTwo = R.raw.six;
 		this.soundThree = R.raw.seven;
 		this.soundFour = R.raw.eight;
 		
 		this.packageTitle = "Test Package Title";
 		
 		this.buttonOneLabel = "label 5";
 		this.buttonTwoLabel = "label 6";
 		this.buttonThreeLabel = "label 7";
 		this.buttonFourLabel = "label 8";
 	}
 	
 	public String getPkgTtl()
 	{
 		return this.packageTitle;
 	}
 	
 	public void setPkgTtl(String newPackageTitle)
 	{
 		this.packageTitle = newPackageTitle;
 	}
 	
 	public int getBtnSound(int buttonNumber)
 	{
 		switch (buttonNumber)
 		{
 			case 1:
 			return this.soundOne;
 		
 			case 2:
 			return this.soundTwo;
 			
 			case 3:
 			return this.soundThree;
 			
 			case 4:
 			return this.soundFour;
 			
 			default:
 			System.out.println("Error: buttonNumber out of range (getBtnSound)");
 			return R.raw.one; //should probably return an oops sound here
 		}
 	}
 	
 	public void setBtnSound(int sound1, int sound2, int sound3, int sound4)
 	{
 		this.soundOne = sound1;
 		this.soundTwo = sound2;
 		this.soundThree = sound3;
 		this.soundFour = sound4;
 	}
 	
 	//Argument is the number of the button, and you'll get the package button label.
 	public String getBtnLbl(int buttonNumber)
 	{
 		switch (buttonNumber)
 		{
 			case 1:
 			return this.buttonOneLabel;
 		
 			case 2:
 			return this.buttonTwoLabel;
 			
 			case 3:
 			return this.buttonThreeLabel;
 			
 			case 4:
 			return this.buttonFourLabel;
 			
 			default:
 			System.out.println("Error: buttonNumber out of range: " + buttonNumber + " (getBtnLbl method)");
 			return "Error: look at console for information";
 		}	
 	}
 	
 	public void setBtnLbl(String label1, String label2, String label3, String label4)
 	{
 		this.buttonOneLabel = label1;
 		this.buttonTwoLabel = label2;
 		this.buttonThreeLabel = label3;
 		this.buttonFourLabel = label4;
 	}
 	
 
 	
 }
