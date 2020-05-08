 package edu.nouri.photoReX.crawler;
 
 import java.util.ArrayList;
 
 import edu.nouri.photoReX.dataProvider.FiveHundredPXDataProvider;
 import edu.nouri.photoReX.picture.FiveHundredPXPhotoCollection;
 import edu.nouri.photoReX.picture.PictureInfo;
 
 public class FHPFeaturedCrawler extends Crawler {
 
 	private final int howMany = 100; //How many pictures to fetch each time we are called 
 	private String feature; //what feature we use to fetch data (editors choice, most viewed... look at 500px api for more info)
 	
 
 	public FHPFeaturedCrawler(String f)
 	{
 		feature = f ; 
		redisSetName = "crawler:FHPFeatured:" + f; 
 	}
 	
 	@Override
 	public  ArrayList<PictureInfo> getFromWebsite()
 	{
 		FiveHundredPXDataProvider dp = new FiveHundredPXDataProvider();
 		ArrayList<PictureInfo> result = new ArrayList<PictureInfo>(); 
 
 		int page = 1; 
 		int h = this.howMany; 
 		while(h > 0)
 		{
 			int perPage = h>100?100:h; 
 			FiveHundredPXPhotoCollection p =  dp.getPictures(feature,perPage,  page++); 
 			h -= perPage; 
 	
 			for(int i=0; i< p.pics.size(); i++)
 			{
 				result.add(p.pics.get(i)); 
 			}
 		}
 		
 		return result;
 	}
 
 }
