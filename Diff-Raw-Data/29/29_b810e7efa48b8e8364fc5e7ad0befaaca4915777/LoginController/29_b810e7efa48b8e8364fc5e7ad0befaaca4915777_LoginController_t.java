 package br.org.aappe.erp.controller;
 
 import javax.servlet.http.HttpServletRequest;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.validator.Validations;
 
 import br.org.aappe.erp.bean.Funcionario;
 import br.org.aappe.erp.enums.Role;
 import br.org.aappe.erp.repository.FuncionarioRepository;
 import br.org.aappe.erp.session.EmployeeSession;
 
 /**
  * @author Phelipe Melanias
  */
 @Resource
 public class LoginController extends MainController {
     private final FuncionarioRepository repository;
 
    public LoginController(Result result, Validator validator, HttpServletRequest request, EmployeeSession employeeSession, FuncionarioRepository repository) {
         super(result, validator, request, employeeSession);
         this.repository = repository;
     }
 
     @Get("/login")
     public void frmLogin() {
         if (employeeSession.isLogged())
             result.redirectTo(IndexController.class).index();
 
         result.include("title", "AAPPE - Acesso Restrito");
     }
 
     @Post("/login")
     public void login(final String login, final String senha) {
         final Funcionario f = repository.authenticate(login.trim(), senha.trim());
 
         validator.checking(new Validations(){{
             if (that(!login.isEmpty(), "login", "login") & that(!senha.isEmpty(), "senha", "senha"))
                 that(f != null, "", "login.erro");
         }});
         validator.onErrorRedirectTo(this).frmLogin();
 
         //Efetuar login
         employeeSession.login(f);
 
         //Redirecionar o funcionário de acordo com seu perfil
         if (employeeSession.getPerfil().equals(Role.ADMINISTRADOR))
             result.redirectTo(IndexController.class).admin();
         else
             result.redirectTo(IndexController.class).index();
     }
 
     @Get("/logout")
     public void logout() {
         //Tive  que mudar para HttpServletRequest para
         //remover todas as sessões usadas pelo sistema.
         request.getSession().invalidate();
 
         result.redirectTo(this).frmLogin();
     }
 }
