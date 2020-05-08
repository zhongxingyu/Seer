 package com.indicrowd.band;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.indicrowd.file.FileService;
 import com.indicrowd.file.ImageService;
 import com.indicrowd.tag.TagService;
 import com.indicrowd.user.model.UserInfo;
 
 @Service
 public class BandService {
 	
 	@Autowired
 	protected FileService fileService;
 
 	@Autowired
 	protected ImageService imageService;
 	@Autowired
 	protected TagService tagService;
 	
 	@Transactional
 	public BandInfo generateNewBand(UserInfo userInfo, BandInfo bandInfo) throws IOException{
 		bandInfo.persist();
 		
 		String category = bandInfo.getCategory();
 		category = category.replaceAll(",", "");
 		String[] tags = bandInfo.category.split(" ");
 		for(int i=0; i<tags.length; i++){
 			tagService.inputTag(tags[i]);
 		}
		
		if (bandInfo.getProfilePhoto() != null && bandInfo.getProfilePhoto().getSize() > 0) {
			fileService.save(bandInfo.getProfilePhoto(), "bandphoto/" + bandInfo.getId().toString(), true);
			fileService.save(imageService.generateThumb(bandInfo.getProfilePhoto()), "bandthumb/" + bandInfo.getId().toString(), true);
		} else {
			File file = fileService.load("img/blankconcert.jpg");
			fileService.save(file, "bandphoto/" + bandInfo.getId().toString(), true);
			fileService.save(imageService.generateThumb(file), "bandthumb/" + bandInfo.getId().toString(), true);
		}
		
 		BandMember bandMember = new BandMember();
 		bandMember.setUserInfo(userInfo);
 		bandMember.setBandInfo(bandInfo);
 		bandMember.setBandRoleType(BandRoleType.findBandRoleType(BandRoleType.BAND_ADMIN));
 		bandMember.merge();
 		
 		List<BandInfo> bandInfoList = userInfo.getUserBand();
 		if(bandInfoList == null){
 			bandInfoList = new ArrayList<BandInfo>();
 		}
 		bandInfoList.add(bandInfo);
 		
 		return bandInfo;
 	}
 }
