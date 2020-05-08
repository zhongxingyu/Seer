 package manager;
 import windows.SoundManagerGUI;
 public class KeyboardListener
 {

	public static void volumeInteract(int volUpDown)
 	{
 		
		int newVolume = SoundManagerGUI.slider0.getValue() + volUpDown;
 		if (newVolume >= 0 && newVolume <= 101)
 		{
 			if(newVolume >= 100)
 			{
				SoundManagerGUI.slider0.setValue(100);
 			}
 			else
 			{
				SoundManagerGUI.slider0.setValue(newVolume);
 			}
 		}
 	}
 }
