 package kr.swmaestro.hsb;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import kr.swmaestro.hsb.auth.AuthManager;
 import kr.swmaestro.hsb.auth.AuthUserInfo;
 import kr.swmaestro.hsb.model.Article;
 import kr.swmaestro.hsb.model.ErrorInfo;
 import kr.swmaestro.hsb.model.Follow;
 import kr.swmaestro.hsb.model.Result;
 import kr.swmaestro.hsb.model.SecureKeyModel;
 import kr.swmaestro.hsb.model.UserInfo;
 import kr.swmaestro.hsb.service.ArticleService;
 import kr.swmaestro.hsb.service.FollowService;
 import kr.swmaestro.hsb.service.UserService;
 import kr.swmaestro.hsb.util.PasswordEncoder;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @org.springframework.stereotype.Controller
 @RequestMapping()
 public class Controller {
 	
 	@Autowired
 	private AuthManager authManager;
 	
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private ArticleService articleService;
 	
 	@Autowired
 	private FollowService followerService;
 	
 	// 오류 체크
 	private boolean errorCheck(Result result, BindingResult bindingResult) {
 		if (bindingResult.hasErrors()) {
 			Set<ErrorInfo> errors = new HashSet<>();
 			for (ObjectError objectError : bindingResult.getAllErrors()) {
 				ErrorInfo error = new ErrorInfo();
 				if (objectError instanceof FieldError) {
 					error.setCode(objectError.getCode() + "." + objectError.getObjectName() + "." + ((FieldError) objectError).getField());
 				} else {
 					error.setCode(objectError.getCode() + "." + objectError.getObjectName());
 				}
 				error.setDefaultMessage(objectError.getDefaultMessage());
 				Set<Object> arguments = new HashSet<>();
 				if (objectError.getArguments() != null) {
 					for (int i = 1 ; i < objectError.getArguments().length ; i++) {
 						arguments.add(objectError.getArguments()[i]);
 					}
 				}
 				error.setArguments(arguments);
 				errors.add(error);
 			}
 			result.setErrors(errors);
 			result.setSuccess(false);
 			return false;
 		}
 		return true;
 	}
 	
 	// 결과값 반환
 	private void ret(Result result, Model model) {
 		result.setSingle(false);
 		model.addAttribute("result", result);
 	}
 	
 	// 결과값 반환
 	private void ret(Result result, SecureKeyModel data, Model model) {
 		data.setSecureKey(null); // 보안 키 제거
 		result.setSingle(true);
 		result.setData(data);
 		model.addAttribute("result", result);
 	}
 	
 	// 결과값 반환
 	private void ret(Result result, List<?> list, Model model) {
 		result.setSingle(false);
 		result.setList(list);
 		model.addAttribute("result", result);
 	}
 	
 	// 인증처리
 	private boolean authCheck(String secureKey, Model model) {
 		if (authManager.isAuthenticated(secureKey)) {
 			return true;
 		} else {
 			Result result = new Result();
 			ErrorInfo error = new ErrorInfo();
 			error.setCode("NeedAuth");
 			error.setDefaultMessage("인증이 필요합니다.");
 			Set<ErrorInfo> errors = new HashSet<>();
 			errors.add(error);
 			result.setSuccess(false);
 			result.setErrors(errors);
 			model.addAttribute("result", result);
 			return false;
 		}
 	}
 	
 	// 인증처리
 	private boolean authCheck(SecureKeyModel secureKeyModel, Model model) {
 		return authCheck(secureKeyModel.getSecureKey(), model);
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	public String main(Model model) {
 		return "main";
 	}
 	
 	@RequestMapping(value = "admin/test", method = RequestMethod.GET)
 	public void test(String secureKey, Model model) {
 		if (authCheck(secureKey, model)) {
 			System.out.println("TEST 페이지 실행");
 		}
 	}
 	
 	// 로그인
 	@RequestMapping(value = "user/auth", method = RequestMethod.POST) // 인증 생성
 	public void login(@Valid AuthUserInfo authUserInfo, BindingResult bindingResult, Model model, HttpServletRequest request) {
 		Result result = new Result();
 		
 		UserInfo userInfo = null;
 		
 		if (!UserInfo.existsUser(authUserInfo.getUsername())) {
 			bindingResult.rejectValue("username", "NotExists.authUserInfo.username", "존재하지 않는 아이디입니다.");
 		} else {
 			userInfo = UserInfo.findUserInfoByUsername(authUserInfo.getUsername());
 			if (!bindingResult.hasFieldErrors("password") && !userInfo.getPassword().equals(PasswordEncoder.encodePassword(authUserInfo.getPassword()))) {
 				bindingResult.rejectValue("password", "Wrong.authUserInfo.password", "잘못된 비밀번호입니다.");
 			}
 		}
 		if (errorCheck(result, bindingResult)) {
 			String secureKey = authManager.auth(userInfo);
 			
 			userInfo.setLastLoginDate(new Date());
 			userInfo.increaseLoginCount();
 			userService.saveUserInfo(userInfo);
 			
 			authUserInfo.setGeneratedSecureKey(secureKey);
 			
 			// 성공~!
 			result.setSuccess(true);
 		}
 		ret(result, authUserInfo, model);
 	}
 	
 	// 로그아웃
 	// 인증 필요
 	@RequestMapping(value = "user/auth", method = RequestMethod.DELETE) // 인증 제거
 	public void logout(Model model) {}
 	
 	// 회원가입
 	@RequestMapping(value = "user/account", method = RequestMethod.POST)
 	public void join(@Valid UserInfo userInfo, BindingResult bindingResult, Model model) {
 		Result result = new Result();
 		
 		if (!bindingResult.hasFieldErrors("password") && !userInfo.getPassword().equals(userInfo.getPasswordConfirm())) {
 			bindingResult.rejectValue("password", "Equals.userInfo.passwordConfirm", "비밀번호와 비밀번호 확인이 다릅니다.");
 		}
 		if (!bindingResult.hasFieldErrors("username") && UserInfo.existsUser(userInfo.getUsername())) {
 			bindingResult.rejectValue("username", "Exists.userInfo.username", "이미 존재하는 아이디입니다.");
 		}
 		if (!bindingResult.hasFieldErrors("nickname") && UserInfo.existsNickname(userInfo.getNickname())) {
 			bindingResult.rejectValue("nickname", "Exists.userInfo.nickname", "이미 존재하는 닉네임입니다.");
 		}
 		
 		if (errorCheck(result, bindingResult)) {
 			// 암호화
 			userInfo.setPassword(PasswordEncoder.encodePassword(userInfo.getPassword()));
 			userInfo.setJoinDate(new Date());
 			
 			userService.saveUserInfo(userInfo);
 			
 			// 성공~!
 			result.setSuccess(true);
 		}
 		ret(result, userInfo, model);
 	}
 	
 	// 회원 정보 수정
 	// 인증 필요
 	@RequestMapping(value = "user/account", method = RequestMethod.PUT)
 	public void updateAccount(@Valid UserInfo userInfo, BindingResult bindingResult, Model model) {
 		Result result = new Result();
 		String secureKey = userInfo.getSecureKey();
 		
 		if (authCheck(userInfo, model)) {
 			
 			UserInfo originUserInfo = UserInfo.findUserInfo(authManager.getUserId(secureKey));
 			String password = PasswordEncoder.encodePassword(userInfo.getPassword());
 			
 			if (!bindingResult.hasFieldErrors("password") && !originUserInfo.getPassword().equals(password) && !userInfo.getPassword().equals(userInfo.getPasswordConfirm())) {
 				bindingResult.rejectValue("password", "Equals.userInfo.passwordConfirm", "비밀번호와 비밀번호 확인이 다릅니다.");
 			}
 			if (!bindingResult.hasFieldErrors("username") && !originUserInfo.getUsername().equals(userInfo.getUsername()) && UserInfo.existsUser(userInfo.getUsername())) {
 				bindingResult.rejectValue("username", "Exists.userInfo.username", "이미 존재하는 아이디입니다.");
 			}
 			if (!bindingResult.hasFieldErrors("nickname") && !originUserInfo.getNickname().equals(userInfo.getNickname()) && UserInfo.existsNickname(userInfo.getNickname())) {
 				bindingResult.rejectValue("nickname", "Exists.userInfo.nickname", "이미 존재하는 닉네임입니다.");
 			}
 			
 			if (errorCheck(result, bindingResult)) {
 
 				originUserInfo.setPassword(password);
 				originUserInfo.setUsername(userInfo.getUsername());
 				originUserInfo.setNickname(userInfo.getNickname());
 				
 				userService.saveUserInfo(originUserInfo);
 				
 				// 로그인 정보에 재삽입
 				authManager.setUserInfo(secureKey, originUserInfo);
 				
 				// 성공~!
 				result.setSuccess(true);
 			}
 		}
 		
 		ret(result, userInfo, model);
 	}
 	
 	// 회원 정보 삭제 (탈퇴)
 	// 인증 필요
 	@RequestMapping(value = "user/account", method = RequestMethod.DELETE)
 	public void leave(String secureKey, Model model) {
 		Result result = new Result();
 		
 		if (authCheck(secureKey, model)) {
 			UserInfo userInfo = UserInfo.findUserInfo(authManager.getUserId(secureKey));
 			userService.deleteUserInfo(userInfo);
 			
 			result.setSuccess(true);
 		}
 		
 		ret(result, model);
 	}
 	
 	// 타임라인
 	// 인증 필요
 	@RequestMapping(value = "user/timeline", method = RequestMethod.GET)
 	public void timeline(String secureKey, @RequestParam(defaultValue = "0") long beforeArticleId, @RequestParam(defaultValue = "10") int count, Model model) {
 		Result result = new Result();
 		
 		if (count < 1 || count > 100) { // 최대 100개
 			count = 10; // 기본 10개
 		}
 		
 		if (authCheck(secureKey, model)) {
 			UserInfo userInfo = UserInfo.findUserInfo(authManager.getUserId(secureKey));
 			List<Article> articleList = articleService.timelineByWriterId(userInfo.getId(), 0l, count);
 			
 			result.setSuccess(true);
 			
 			ret(result, articleList, model);
 		}
 	}
 	
 	// 글 목록 보기
 	@RequestMapping(value = "{username}", method = RequestMethod.GET)
 	public String home(@PathVariable String username, @RequestParam(defaultValue = "0") long beforeArticleId, @RequestParam(defaultValue = "10") int count, Model model) {
 		Result result = new Result();
 		
 		if (count < 1 || count > 100) { // 최대 100개
 			count = 10; // 기본 10개
 		}
 		
 		UserInfo userInfo = UserInfo.findUserInfoByUsername(username);
 		List<Article> articleList = articleService.findArticlesByWriterId(userInfo.getId(), beforeArticleId, count);
 		
 		result.setSuccess(true);
 		
 		ret(result, articleList, model);
 		
 		return "home";
 	}
 	
 	// 유저 정보 보기
 	@RequestMapping(value = "{username}/info", method = RequestMethod.GET)
 	public String info(@PathVariable String username, Model model) {
 		Result result = new Result();
 		
 		UserInfo userInfo = UserInfo.findUserInfoByUsername(username);
 		
 		result.setSuccess(true);
 		
 		ret(result, userInfo, model);
 		
 		return "info";
 	}
 	
 	// 글쓰기
 	@RequestMapping(value = "{username}", method = RequestMethod.POST)
 	public String write(@PathVariable String username, @Valid Article article, BindingResult bindingResult, Model model) {
 		Result result = new Result();
 		
 		if (authCheck(article, model)) {
 			UserInfo userInfo = authManager.getUserInfo(article.getSecureKey());
 
 			article.setWriterId(userInfo.getId());
 			article.setWriterNickname(userInfo.getNickname());
 			article.setWriterUsername(userInfo.getUsername());
 			
 			System.out.println(article.getWriterId());
 			if (errorCheck(result, bindingResult)) {
 				article.setWriteDate(new Date());
 				
 				// 저장
 				articleService.saveArticle(article);
 				
 				// 성공~!
 				result.setSuccess(true);
 			}
 			ret(result, article, model);
 		}
 		
 		return "write";
 	}
 
 	// 팔로우하기
 	// 인증 필요
 	@RequestMapping(value = "{username}/follow", method = RequestMethod.POST) // 팔로우 생성
 	public String follow(@PathVariable String username, @Valid Follow follower, BindingResult bindingResult, Model model) {
 		Result result= new Result();
 		
 		if(authCheck(follower,model)){
 			UserInfo followingUser=authManager.getUserInfo(follower.getSecureKey());
 			UserInfo followedUser = UserInfo.findUserInfoByUsername(username);
 			
 			//본인의 계정은 팔로우 할 수 없도록 validation
 			if (followingUser.getId().equals(followedUser.getId()) ){
 				bindingResult.rejectValue("id", "Equals.follower.userid", "본인의 아이디는 팔로우 할 수 없습니다.");
 			}
 			if(Follow.isFollowing(followedUser.getId(),followingUser.getId())){
 				bindingResult.rejectValue("id", "Exists.follower.userid", "이미 팔로우한 아이디 입니다.");
 			}
 			if (errorCheck(result, bindingResult)) {
 				follower.setTargetUserId(followedUser.getId());
 				follower.setTargetUserNickname(followedUser.getNickname());
 				follower.setTargetUserUsername(followedUser.getUsername());
 				follower.setFollowerId(followingUser.getId());
 				follower.setFollowerNickname(followingUser.getNickname());
 				follower.setFollowerUsername(followingUser.getUsername());
 				follower.setFollowDate(new Date());
 			
 				followerService.saveFollower(follower);
 			
 				// 성공
 				result.setSuccess(true);
 			}
 			ret(result,follower,model);
 		}
 		
 		return "follow";
 		
 	}
 	
 	// 언팔로우
 	// 인증 필요
 	@RequestMapping(value = "{username}/follow", method = RequestMethod.DELETE) // 팔로우 제거
 	public String unfollow(@PathVariable String username,String secureKey,@Valid Follow follower,BindingResult bindingResult, Model model) {
 		Result result= new Result();
 		System.out.println("secureKey:"+secureKey);
 		if(authCheck(secureKey, model)){
 			UserInfo followingUser=authManager.getUserInfo(follower.getSecureKey());
 			UserInfo followedUser = UserInfo.findUserInfoByUsername(username);
 			if(!Follow.isFollowing(followedUser.getId(),followingUser.getId())){
 				bindingResult.rejectValue("id", "NonExists.follower.userid", "팔로우 관계가 아닙니다.");
 			}
 			if(errorCheck(result,bindingResult)){
 				follower.setTargetUserId(followedUser.getId());
 				follower.setFollowerId(followingUser.getId());
 				followerService.removeFollow(follower);
 				
 				// 성공
 				result.setSuccess(true);
 			}
 			ret(result,follower,model);
 		}
 		return "unfollow";
 	}
 	
 	// 팔로잉 목록
 	@RequestMapping(value = "{username}/following", method = RequestMethod.GET)
 	public String following(@PathVariable String username, Model model) {
 		Result result= new Result();
 		
 		UserInfo userInfo = UserInfo.findUserInfoByUsername(username);
 		List<UserInfo> userList=followerService.getFollowingListByUserInfo(userInfo);
 		result.setSuccess(true);
 		
 		ret(result, userList, model);
 		return "following";
 	}
 	
 	// 팔로어 목록
 	@RequestMapping(value = "{username}/followers", method = RequestMethod.GET)
 	public String followers(@PathVariable String username, Model model) {
 		Result result= new Result();
 		
 		UserInfo userInfo = UserInfo.findUserInfoByUsername(username);
 		List<UserInfo> userList=followerService.getFollowerListByUserInfo(userInfo);
 		result.setSuccess(true);
 		
 		ret(result, userList, model);
 		return "followers";
 	}
 	
 	// 글삭제
 	// 인증 필요
 	@RequestMapping(value = "article/{id}", method = RequestMethod.DELETE) // 글 제거
 	public String deleteArticle(@PathVariable Long id, Model model) {
 		return "deleteArticle";
 	}
 	
 	// 댓글 등록
 	// 인증 필요
 	@RequestMapping(value = "article/{articleId}/comment", method = RequestMethod.POST) // 댓글 등록
 	public String comment(@PathVariable Long articleId, Model model) {
 		return "comment";
 	}
 	
 	// 댓글 삭제
 	// 인증 필요
 	@RequestMapping(value = "comment/{id}", method = RequestMethod.DELETE) // 댓글 삭제
 	public String deleteComment(@PathVariable Long id, Model model) {
 		return "deleteComment";
 	}
 	
 	@RequestMapping(value = "delete/test", method = RequestMethod.DELETE) // DELETE 테스트
 	public void deleteTest(UserInfo userInfo, Model model, HttpServletRequest request) {
 		System.out.println(userInfo.getUsername());
 		model.addAttribute("result", "");
 	}
 	
 	@RequestMapping(value = "put/test", method = RequestMethod.PUT) // PUT 테스트
 	public void putTest(UserInfo userInfo, Model model, HttpServletRequest request) {
 		System.out.println(userInfo.getUsername());
 		model.addAttribute("result", "");
 	}
 	
 }
