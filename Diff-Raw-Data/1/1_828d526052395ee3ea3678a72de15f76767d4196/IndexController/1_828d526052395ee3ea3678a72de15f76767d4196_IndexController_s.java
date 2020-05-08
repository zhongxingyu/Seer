 /*
  * Modificado de IndexController.java -
  * 	http://vraptor3.googlecode.com/files/vraptor-blank-project-3.3.1.zip
  */
 package br.usp.cata.web.controller;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 import br.usp.cata.model.Rule;
 import br.usp.cata.model.User;
 import br.usp.cata.service.NewUserService;
 import br.usp.cata.service.NewUserService.SignupResult;
 import br.usp.cata.service.RuleService;
 import br.usp.cata.service.UserService;
 import br.usp.cata.web.interceptor.IrrestrictAccess;
 import br.usp.cata.web.interceptor.Transactional;
 
 
 @Resource
 @IrrestrictAccess
 public class IndexController {
 
 	private final Result result;
 	private final Validator validator;
 	private final UserService userService;
 	private final NewUserService newUserService;
 	private final RuleService ruleService;
 	private final int PASSWORD_MIN_LENGTH = 6;
 	private final int PASSWORD_MAX_LENGTH = 32;
 	
 	public IndexController(Result result, Validator validator,
 			UserService userService, NewUserService newUserService, RuleService ruleService) {
 		this.result = result;
 		this.validator = validator;
 		this.userService = userService;
 		this.newUserService = newUserService;
 		this.ruleService = ruleService;
 	}
 
 	@Get
 	@Path("/")
 	public void index() {
 		if(userService.isAuthenticatedUser())
 			result.redirectTo(HomeController.class).index();
 	}
     
     @Post
     @Path("/login")
     @Transactional
     public void login(User user) {
     	
     	if(user.getEmail().equals(""))
     		validator.add(new ValidationMessage(
     				"O campo não pode ser vazio", "E-mail"));
     	if(user.getPassword().equals(""))
     		validator.add(new ValidationMessage(
     				"O campo não pode ser vazio", "Senha"));
     	
     	validator.onErrorRedirectTo(IndexController.class).index();
 
         final boolean success = userService.authenticate(user.getEmail(), user.getPassword());
 
         if(!success) {
         	validator.add(new ValidationMessage("valores inválidos", "E-mail ou senha"));
             validator.onErrorRedirectTo(IndexController.class).index();
         }
         
         result.redirectTo(HomeController.class).index();
     }
 
 	@Post
 	@Path("/advice")
 	public void advice(UploadedFile file) {
 		if(file == null)
 			validator.add(new ValidationMessage(
     				"Selecione um arquivo no formato .txt", "Nenhum arquivo selecionado"));
 		else if(!file.getContentType().equals("text/plain")) {
 			validator.add(new ValidationMessage(
 					"O arquivo deve estar no formato .txt", "Formato do arquivo"));
 		}
 		validator.onErrorUsePageOf(IndexController.class).index();
 		
 		result.forwardTo(SuggestionsController.class).results(file);
 	}
 	
 	@Get
 	@Path("/rules")
 	public void rules() {
 		result.include("rules", ruleService.findAll());
 	}
 	
 	@Get
 	@Path("/about")
 	public void about() {
 	}
 	
 	@Get
 	@Path("/rules/viewrule/{rule.ruleID}")
 	public void viewrule(Rule rule) {
 		// TODO caso em que não há regra com o id passado
 		
 		result.include("rule", ruleService.findByID(rule.getRuleID()));
 	}
     
     @Get
     @Path("/signup")
     public void signup() {
 		if(userService.isAuthenticatedUser())
 			result.redirectTo(HomeController.class).index();
     }
     
     @Post
     @Path("/signup")
     @Transactional
     public void signup(User newUser, String password)
     {
     	if(newUser.getName().equals(""))
     		validator.add(new ValidationMessage(
     				"O campo não pode ser vazio", "Nome"));
     	if(newUser.getEmail().equals(""))
     		validator.add(new ValidationMessage(
     				"O campo não pode ser vazio", "E-mail"));
     	if(newUser.getPassword().length() < PASSWORD_MIN_LENGTH)
     		validator.add(new ValidationMessage(
     				"A senha deve ter 6 caracteres no mínimo", "Senha"));
     	if(newUser.getPassword().length() > PASSWORD_MAX_LENGTH)
     		validator.add(new ValidationMessage(
     				"A senha deve ter 32 caracteres no máximo", "Senha"));
     	if(!newUser.getPassword().equals(password))
         	validator.add(new ValidationMessage(
         			"As senhas digitadas não são idênticas", "Senhas"));
         
     	validator.onErrorRedirectTo(IndexController.class).signup();
         
     	SignupResult signupResult = newUserService.register(newUser);
     	
     	switch(signupResult) {
     		case SUCCESS:
     			result.include("messages", "Sua conta foi criada. " +
     					"Um e-mail de ativação foi enviado para o endereço " + newUser.getEmail() + ".");
     			break;
     		case USER_ALREADY_REGISTERED_ACTIVE:
     			validator.add(new ValidationMessage(
             			"Já existe um usuário cadastrado com este e-mail no sistema", "E-mail"));
     			break;
     		case USER_ALREADY_REGISTERED_INACTIVE:
     			result.include("messages",
     					"Já existe um usuário cadastrado com este e-mail no sistema - mas está inativo. " + 
     					"Um e-mail de ativação foi enviado para o endereço " + newUser.getEmail() + ".");
     			break;
     		case NO_EMAIL_SENT:
     			validator.add(new ValidationMessage(
     					"Não foi possível enviar o e-mail de ativação de conta para o endereço " + newUser.getEmail() + ". " +
     							"Tente novamente mais tarde ou use outro endereço de e-mail.", "E-mail de ativação"));
     			break;
     		default:
     			 throw new IllegalStateException("Unexpected signup result");
     	}    	
     	validator.onErrorRedirectTo(IndexController.class).signup();
     	
     	result.redirectTo(IndexController.class).index();
     }
     
     @Get
     @Path("/signup/activate/{activationKey}")
     @Transactional
     public void activate(String activationKey)
     {
     	SignupResult activationResult = newUserService.activate(activationKey);
     	
     	switch(activationResult) {
     		case SUCCESS:
     			result.include("messages", "Sua conta foi ativada com sucesso.");
     			break;
     		case USER_ALREADY_REGISTERED_ACTIVE:
     			result.include("messages", "Sua conta já está ativada.");
     			break;
     		case ACTIVATION_KEY_NOT_FOUND:
     			validator.add(new ValidationMessage(
         				"Não ocorreu ativação de nenhuma conta porque o link é inválido", "Link inválido"));
     			break;
     		default:
     			throw new IllegalStateException("Unexpected activation result");
     	}  	
     	validator.onErrorRedirectTo(IndexController.class).index();
     	
     	result.redirectTo(IndexController.class).index();
     }
     
     @Get
     @Path("/recover")
     public void recover() {
     	if(userService.isAuthenticatedUser())
 			result.redirectTo(HomeController.class).index();
     }
     
     @Post
     @Path("/recover")
     public void recover(String email) {
     }
    }
     
 }
