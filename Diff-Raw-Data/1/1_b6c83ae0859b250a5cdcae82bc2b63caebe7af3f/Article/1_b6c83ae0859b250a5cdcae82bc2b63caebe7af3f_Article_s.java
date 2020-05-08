 package kr.swmaestro.hsb.domain;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.Size;
 
import kr.devin.domain.UserInfo;
 import kr.swmaestro.hsb.data.DataModel;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 
 /**
  * @author 심영재
  */
 @RooJavaBean
 @RooToString
 @RooEntity
 public class Article implements DataModel {
 	
 	@ManyToOne
 	@JoinColumn(name = "targetUserId")
 	private UserInfo targetUser;
 
 	@NotEmpty
 	@Size(max = 3000)
 	@Column(length = 3000, nullable = false)
 	private String content;
 
 	@ManyToOne
 	@JoinColumn(name = "writerUserId", nullable = false)
 	private UserInfo writerUser;
 
 	@Column(nullable = false)
 	private Date writeDate;
 
 	@OneToMany(mappedBy = "article", cascade = { CascadeType.ALL }, orphanRemoval = true)
 	private List<Comment> commentList = new ArrayList<Comment>();
 	
 }
