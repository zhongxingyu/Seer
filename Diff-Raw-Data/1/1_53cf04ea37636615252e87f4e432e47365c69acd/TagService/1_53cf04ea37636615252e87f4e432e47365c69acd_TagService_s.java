 package com.sound.service.sound.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.sound.dao.SoundDAO;
 import com.sound.dao.TagDAO;
import com.sound.exception.SoundException;
 import com.sound.model.Sound;
 import com.sound.model.Tag;
 
 public class TagService implements com.sound.service.sound.itf.TagService {
 	@Autowired
 	SoundDAO soundDAO;
 
 	@Autowired
 	TagDAO tagDAO;
 
 	@Override
 	public Tag getOrCreate(String label) {
 		Tag tag = tagDAO.findOne("label", label);
 
 		if (tag != null && tag.getId() != null) 
 		{
 			return tag;
 		}
 
 		tag = new Tag();
 		tag.setLabel(label);
 		tagDAO.save(tag);
 
 		return tag;
 	}
 
 	@Override
 	public List<Tag> listTagsContains(String pattern) {
 		return tagDAO.findByPattern("label", pattern, true);
 	}
 
 	@Override
 	public List<Tag> listAll(){
 		return tagDAO.find().asList();
 	}
 
 	@Override
 	public void attachToSound(String soundAlias, List<String> tagLabels)
 	{
 		List<Tag> tags = new ArrayList<Tag>();
 
 		Sound sound = soundDAO.findOne("profile.name", soundAlias);
 		for (String label : tagLabels) {
 			tags.add(this.getOrCreate(label));
 		}
 		sound.addTags(tags);
 		soundDAO.save(sound);
 	}
 
 	@Override
 	public void detachFromSound(String soundAlias, List<String> tagLabels)
 	{
 		List<Tag> tags = new ArrayList<Tag>();
 		for (String label : tagLabels) {
 			tags.add(this.getOrCreate(label));
 		}
 
 		Sound sound = soundDAO.findOne("profile.name", soundAlias);
 		sound.getTags().removeAll(tags);
 		soundDAO.save(sound);
 	}
 
 	@Override
 	public List<Sound> getSoundsWithTag(String label)
 	{
 		return soundDAO.find("tags.label", label);
 	}
 
 	public SoundDAO getSoundDAO() {
 		return soundDAO;
 	}
 
 	public void setSoundDAO(SoundDAO soundDAO) {
 		this.soundDAO = soundDAO;
 	}
 
 	public TagDAO getTagDAO() {
 		return tagDAO;
 	}
 
 	public void setTagDAO(TagDAO tagDAO) {
 		this.tagDAO = tagDAO;
 	}
 	
 }
