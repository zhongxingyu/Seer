 package com.arcaner.warlock.rcp.menu;
 
 import java.util.Collection;
 
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.ui.actions.CompoundContributionItem;
 
 import com.arcaner.warlock.configuration.Profile;
 import com.arcaner.warlock.configuration.SavedProfiles;
 import com.arcaner.warlock.rcp.actions.ProfileConnectAction;
 
 public class ProfileConnectContributionItem extends CompoundContributionItem {
 
 	public ProfileConnectContributionItem() {
 		super();
 	}
 	
 	public ProfileConnectContributionItem(String id) {
 		super(id);
 	}
 
 	@Override
 	protected IContributionItem[] getContributionItems() {
 		Collection<Profile> profiles = SavedProfiles.getAllProfiles();
 		IContributionItem[] items = new IContributionItem[profiles.size()];
 		int i = 0;
 		
 		for (Profile profile : profiles)
 		{
 			items[i] = new ActionContributionItem(new ProfileConnectAction(profile));
			i++;
 		}
 		
 		return items;
 	}
 }
