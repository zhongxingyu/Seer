 /**
  * 
  */
 package podsalinan;
 
 import java.util.Vector;
 
 /**
  * @author bugman
  *
  */
 public class CLPodcastMenu extends CLMenu{
 	private Vector<Podcast> podcasts;
 	
 	/**
 	 * @param settings 
 	 * @param urlDownloads 
 	 * 
 	 */
 	public CLPodcastMenu(ProgSettings parentMenuList, Vector<Podcast> newPodcasts, URLDownloadList urlDownloads) {
 		super(parentMenuList, "podcast");
 		String[] mainMenuList = {
 				"(A-Z) Enter Podcast letter to select Podcast.",
 				"",
 				"9. Return to Main Menu"};
 		setMainMenuList(mainMenuList);
 		setPodcasts(newPodcasts);
 		addSubmenu(new CLPodcastSelectedMenu(menuList, urlDownloads));
 	}
 	
 	public void printMainMenu(){
 		int podcastCount=1;
 		
 		for (Podcast podcast : podcasts){
 			if (!podcast.isRemoved())
 				System.out.println(getEncodingFromNumber(podcastCount)+". "+podcast.getName());
 			podcastCount++;
 		}
 		super.printMainMenu();
 	}
 
 	public Vector<Podcast> getPodcasts() {
 		return podcasts;
 	}
 
 	public void setPodcasts(Vector<Podcast> podcasts) {
 		this.podcasts = podcasts;
 	}
 	
 	public void process(int userInputInt) {
 		//System.out.println("Debug: CLPodcastMenu.process("+userInputInt+")");
 		if (menuList.size()==1)
 			switch (userInputInt){
 				case 9:
 					menuList.clear();
 					break;
 				case 99:
 					printMainMenu();
 			}
 		if (menuList.size()>1)
 			((CLPodcastSelectedMenu)findSubmenu("podcast_selected")).process(userInputInt);
 		if (menuList.size()==1){
 			userInputInt=-1000;
 			super.process(userInputInt);
 		}
 	}
 
 	public void process(String userInput){
 		//System.out.println("Debug: CLPodcastMenu.process(String)");
 		if (menuList.size()==1){
 			if (userInput.length()<3){
 				int podcastNumber=convertCharToNumber(userInput);
 				
 				if ((podcastNumber>podcasts.size())&&
 					(podcastNumber<0))
 					System.out.println("Error: Invalid Podcast");
 				else{
					int podcastCount=0;
 					boolean podcastFound=false;
 					while ((podcastCount<podcasts.size())&&(!podcastFound)){
 						Podcast podcast = podcasts.get(podcastCount);
 						if ((podcastCount==podcastNumber)&&(!podcast.isRemoved())){
 							menuList.addSetting("selectedPodcast", podcast.getDatafile());
 							((CLPodcastSelectedMenu)findSubmenu("podcast_selected")).setSelectedPodcast(podcast);
 							System.out.println("Selected Podcast: "+podcast.getName());
 							podcastFound=true;
 						}
 						podcastCount++;
 					}
 				}
 				userInput=null;
 			}
 		}
 		if (menuList.size()>1){
 			if (menuList.findSetting("selectedPodcast")!=null){
 				((CLPodcastSelectedMenu)findSubmenu("podcast_selected")).process(userInput);
 			}
 		} else {
 			//System.out.println("Debug: CLPodcastMenu.process(String)");
 		    super.process(userInput);
 		}
 	}
 
 	public void listPodcasts() {
 		if (podcasts.size()==0)
 			System.out.println("Error: No podcasts stored in the system.");
 		else
 			for (Podcast podcast : podcasts)
 				if (!podcast.isRemoved())
 					System.out.println("("+podcast.getDatafile()+") "+podcast.getName());
 	}
 }
