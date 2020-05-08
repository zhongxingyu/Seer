 package net.daum.devday12.service;
 
import org.springframework.beans.factory.annotation.Autowired;
 
 import net.daum.devday12.domain.Post;
 
import org.springframework.stereotype.Component;
 
@Component
public class PostServiceImpl implements PostService {
	@Autowired
	private RawDataService	rawDataService;
	
 	public Post findPost(String postId) {
 		return null;
 	}
 }
