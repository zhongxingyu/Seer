 package manager;
 import windows.SoundManagerGUI;
 public class KeyboardListener
 {
// Class is broken. Do not use. Will fix or deprecate later
	public static void volumeInteractEvent(int slider[sliderID], int volUpDown)
 	{
 		
		int newVolume = SoundManagerGUI.slider[sliderID].getValue() + volUpDown;
 		if (newVolume >= 0 && newVolume <= 101)
 		{
 			if(newVolume >= 100)
 			{
				SoundManagerGUI.slider[sliderID].setValue(100);
 			}
 			else
 			{
				SoundManagerGUI.slider[sliderID].setValue(newVolume);
 			}
 		}
 	}
 }
