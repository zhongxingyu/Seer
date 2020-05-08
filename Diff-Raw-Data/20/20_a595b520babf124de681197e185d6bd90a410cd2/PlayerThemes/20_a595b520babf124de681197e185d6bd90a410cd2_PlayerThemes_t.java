 package com.android.icecave.general;
 
 import com.android.icecave.gui.ITheme;
 
 import com.android.icecave.R;
 import com.android.icecave.utils.ThemeHolder;
 
 public class PlayerThemes implements ITheme
 {	
 	ThemeHolder[] mHolders;
 	
 	public PlayerThemes() {
		mHolders = new ThemeHolder[] {new ThemeHolder(R.drawable.default_player, "Debug Theme"), 
									  new ThemeHolder(R.drawable.xenosaga_kos_mos_sprite_by_farheit, "Xenosaga Theme")};
 	}
 	
 	@Override
 	public int getThemeId(int index)
 	{
 		return mHolders[index].getThemeId();
 	}
 
 	@Override
 	public String getThemeName(int index)
 	{
 		return mHolders[index].getThemeName();
 	}
 
 	@Override
 	public String[] getThemeNames()
 	{
 		String[] result = new String[mHolders.length];
 		
 		// Get all names
 		for (int i = 0; i < mHolders.length; i++) {
 			result[i] = mHolders[i].getThemeName();
 		}
 		
 		return result;
 	}
 }
