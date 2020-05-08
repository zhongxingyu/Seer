 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.eclipse.jgit.api.errors.CanceledException;
 import org.eclipse.jgit.api.errors.DetachedHeadException;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.InvalidConfigurationException;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.NoHeadException;
 import org.eclipse.jgit.api.errors.RefNotFoundException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
 
 public class Upgrader {
 	public void upgrade(){
 		try {
 			UpgradeCheck n4 = new UpgradeCheck();
 			if(n4.hasNoTags()){
 				System.out.println(" local 저장소에 upgrade 버전 태그가 존재하지 않습니다.");
 				System.out.println(" upgrade를 종료합니다.");
 				return;
 			}
 			if(n4.isLatetestVersion()) {
 				System.out.println(" 최신 버전입니다.");
 			} 
 			else {
 				ArrayList<String> updatedTags = UpgradeCheck.getUpdatedTags(n4.getLocalTags(), n4.getRemoteTags());
 				System.out.println(" 최신 버전이 존재합니다.");
 				System.out.print(" 업그레이드 하시겠습니까?(y/n)  : ");
 				if(getYesNoKey()) {
 					n4.merge(updatedTags);
 					System.out.println(" 업그레이드 완료 되었습니다.");
 				}
 				else {
 					n4.deleteTags(n4.getLocalGit(), updatedTags);
 					System.out.println(" 업그레이드가 취소되었습니다.");
 				}
 			}
 				
 		} catch (WrongRepositoryStateException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		} catch (DetachedHeadException e) {
 			e.printStackTrace();
 		} catch (InvalidRemoteException e) {
 			e.printStackTrace();
 		} catch (CanceledException e) {
 			e.printStackTrace();
 		} catch (RefNotFoundException e) {
 			e.printStackTrace();
 		} catch (NoHeadException e) {
 			e.printStackTrace();
 		} catch (TransportException e) {
 			e.printStackTrace();
 		} catch (GitAPIException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void upgrade(String option) {
 		try {
 			UpgradeCheck n4 = new UpgradeCheck();
 			n4.setLocalTags();
 			if(n4.getLocalTags().contains(option)){
 				System.out.println("기존 버전 이하의 버전입니다.");
 			}
 			else {
 				n4.fetchGit();
 				n4.setRemoteTags();
 				ArrayList<String> updatedTags = UpgradeCheck.sortTags(UpgradeCheck.getUpdatedTags(n4.getLocalTags(), n4.getRemoteTags()));
 				if(n4.getRemoteTags().contains(option)) {
 					System.out.print(option+"버전으로 업그레이드 하시겠습니까?(y/n)  : ");
 					if(getYesNoKey()) {
 						while(!option.equals(updatedTags.get(0))){
 							UpgradeCheck.deleteTag(n4.getLocalGit(), updatedTags.get(0));
 							updatedTags.remove(0);
 						}
 						n4.merge(option);
 						System.out.println(" 업그레이드 완료 되었습니다.");
 					}
 					else {
 						n4.deleteTags(n4.getLocalGit(), updatedTags);
 						System.out.println(" 업그레이드가 취소되었습니다.");
 					}
 				}
 				else {
					n4.deleteTags(n4.getLocalGit(), updatedTags);
 					System.out.println("존재하지 않는 버전입니다.");
 				}
 			}
 		} catch (InvalidRemoteException e) {
 			e.printStackTrace();
 		} catch (TransportException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (GitAPIException e) {
 			e.printStackTrace();
 		}
 	}
	
	public static boolean getYesNoKey() {
	    int key;
	    Scanner oScanner = new java.util.Scanner(System.in);
	    do { 
	    	key = oScanner.findInLine(".").charAt(0);
	        if (key == 'y' || key == 'Y') return true;
	        if (key == 'n' || key == 'N') return false;
	    } while (key != 0);

	      return false; 
	  }

 }
