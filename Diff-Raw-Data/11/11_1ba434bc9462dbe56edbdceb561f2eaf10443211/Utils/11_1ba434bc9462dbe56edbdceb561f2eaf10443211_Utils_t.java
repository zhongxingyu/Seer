 package edu.nkuresearch.securitychecker.fragments;
 
 import java.util.Comparator;
 
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class Utils{
 
 	public static class PackInCompWord implements Comparator<PackageInfo>{
 		
 		private PackageManager mPackMan;
 		
 		public PackInCompWord(PackageManager packMan){
 			mPackMan = packMan;
 		}
 		
 		@Override
 		public int compare(PackageInfo lhs, PackageInfo rhs) {
 			String lhsName = (String) lhs.applicationInfo.loadLabel(mPackMan);
 			String rhsName = (String) rhs.applicationInfo.loadLabel(mPackMan);
 			return lhsName.compareToIgnoreCase(rhsName);
 		}
 	}
 	
 	public static class PackInCompNumAsc implements Comparator<PackageInfo>{
 		
 		private PackageManager mPackMan;
 		
 		public PackInCompNumAsc(PackageManager packMan){
 			mPackMan = packMan;
 		}
 		
 		@Override
 		public int compare(PackageInfo lhs, PackageInfo rhs) {
 			String[] permsL = lhs.requestedPermissions;
 			String[] permsR = rhs.requestedPermissions;
 			if(permsL != null && permsR != null)
 				if(permsL.length != permsR.length)
 					return permsL.length - permsR.length;
 				else{
 					String lhsName = (String) lhs.applicationInfo.loadLabel(mPackMan);
 					String rhsName = (String) rhs.applicationInfo.loadLabel(mPackMan);
 					return lhsName.compareToIgnoreCase(rhsName);
 				}			
			else if(permsL == null && permsR == null){
				String lhsName = (String) lhs.applicationInfo.loadLabel(mPackMan);
				String rhsName = (String) rhs.applicationInfo.loadLabel(mPackMan);
				return lhsName.compareToIgnoreCase(rhsName);
			}
 			else if(permsL == null)
 				return -1;
 			else
 				return 1;
 		}
 	}
 	
 	public static class PackInCompNumDes implements Comparator<PackageInfo>{
 	
 		private PackageManager mPackMan;
 	
 		public PackInCompNumDes(PackageManager packMan){
 			mPackMan = packMan;
 		}
 	
 		@Override
 		public int compare(PackageInfo lhs, PackageInfo rhs) {
 			String[] permsL = lhs.requestedPermissions;
 			String[] permsR = rhs.requestedPermissions;
 			if(permsL != null && permsR != null){
 				if(permsR.length != permsL.length)
 					return permsR.length - permsL.length;
 				else{
 					String lhsName = (String) lhs.applicationInfo.loadLabel(mPackMan);
 					String rhsName = (String) rhs.applicationInfo.loadLabel(mPackMan);
 					return lhsName.compareToIgnoreCase(rhsName);
 				}
 			}
			else if(permsL == null && permsR == null){
				String lhsName = (String) lhs.applicationInfo.loadLabel(mPackMan);
				String rhsName = (String) rhs.applicationInfo.loadLabel(mPackMan);
				return lhsName.compareToIgnoreCase(rhsName);
			}
 			else if(permsL == null)
 				return 1;
 			else
 				return -1;
 		}
 	}
 }
