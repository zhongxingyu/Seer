 package com.dev.campus.util;
 
 
 import java.util.HashSet;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.HomeActivity;
 import com.dev.campus.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 /**
  * Custom AlertDialog class which handles subscriptions management
  * @author elyas-bhy
  *
  */
 public class SubscribeDialog extends AlertDialog {
 	
 	private AlertDialog mSubscribeDialog;
 	private Activity mContext;
 	
 	public SubscribeDialog(Activity context) {
 		super(context);
 		mContext = context;
 	}
 	
 	/**
 	 * Creates and shows an instance of SubscribeDialog
 	 * @param redirectionEnabled flag that enables redirecting
 	 * to home page after validating choices.
 	 */
 	public void showDialog(boolean redirectionEnabled) {
 		SubscribeDialogBuilder builder = new SubscribeDialogBuilder(mContext, redirectionEnabled);
 		mSubscribeDialog = builder.create();
 		mSubscribeDialog.show();
 		initializePositiveButton();
 	}
 	
 	/**
 	 * Check that positive button is enabled only if 
 	 * at least one establishment is selected
 	 */
 	private void initializePositiveButton() {
 		if (!CampusUB1App.persistence.isSubscribedUB1()
 		 && !CampusUB1App.persistence.isSubscribedLabri())
 			mSubscribeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
 		else
 			mSubscribeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
 	}
 
 	/**
 	 * Custom AlertDialog builder class which provides subscriptions management
 	 * @author elyas-bhy
 	 *
 	 */
 	private class SubscribeDialogBuilder extends AlertDialog.Builder {
 		
 		private final Integer UB1_INDEX = 0;
 		private final Integer LABRI_INDEX = 1;
 		
 		private HashSet<Integer> mSelectedItems;
 		
 		public SubscribeDialogBuilder(Activity context, final boolean redirectionEnabled) {
 			super(context);
 			
 			mSelectedItems = new HashSet<Integer>();
 			boolean[] checkedItems = {CampusUB1App.persistence.isSubscribedUB1(), 
 									  CampusUB1App.persistence.isSubscribedLabri()};
 			if (CampusUB1App.persistence.isSubscribedUB1())
 				mSelectedItems.add(UB1_INDEX);
 			if (CampusUB1App.persistence.isSubscribedLabri())
 				mSelectedItems.add(LABRI_INDEX);
			
 			setTitle(R.string.select_establishment);
 			setMultiChoiceItems(R.array.establishments_array, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which,
 						boolean isChecked) {
 					if (isChecked) {
 						mSelectedItems.add(which);
 					} else if (mSelectedItems.contains(which)) {
 						mSelectedItems.remove(which);
 					}
 					
 					if (mSelectedItems.size() == 0)
 						((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
 					else
 						((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
 				}
 			});
 			
 			setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					boolean sub_ub1 = mSelectedItems.contains(UB1_INDEX);
 					boolean sub_labri = mSelectedItems.contains(LABRI_INDEX);
 					
 					CampusUB1App.persistence.setSubscribeUB(sub_ub1);
 					CampusUB1App.persistence.setSubscribeLabri(sub_labri);
 					
 					if (redirectionEnabled) {
 						//Manually update filters, as the SharedPreferences
 						//listener is not yet registered
 						CampusUB1App.persistence.setFilterUB(sub_ub1);
 						CampusUB1App.persistence.setFilterLabri(sub_labri);
 						mContext.startActivity(new Intent(mContext, HomeActivity.class));
 						mContext.finish();
 					}
 				}
 			});
 			
 			setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					mSelectedItems.clear();
 				}
 			});
 		}
 	}
 
 }
