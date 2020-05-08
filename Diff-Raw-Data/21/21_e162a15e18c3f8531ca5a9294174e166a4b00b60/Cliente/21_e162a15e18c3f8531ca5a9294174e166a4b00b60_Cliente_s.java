 package controllers;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.List;
 import java.util.Locale;
 
 import others.Util;
 
 import Daos.ProcessoDao;
 
 import play.mvc.*;
 
 import models.*;
 import play.data.validation.Validation;
 import Daos.ClienteDao;
 import Daos.UsuarioDao;
 
 public class Cliente extends BaseController {
 
 	/** 
 	 * Locale Brasileiro 
 	 */  
 	private static final Locale BRAZIL = new Locale("pt","BR");  
 	/** 
 	 * Símbolos especificos do Real Brasileiro 
 	 */  
 	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);  
 	/** 
 	 * Mascara de dinheiro para Real Brasileiro 
 	 */  
 	public static final DecimalFormat DINHEIRO_REAL = new DecimalFormat("¤ ###,###,##0.00",REAL);	
 	/**
 	 * 
 	 * Carrega os dados do cliente para a tela
 	 * 
 	 * @param nome
 	 * @param endereco
 	 * @param email
 	 * @param telResidencial
 	 * @param celular
 	 * @param rg
 	 * @param cpfCnpj
 	 * 
 	 */
 	public static void index(long id, String tipoPessoa, String nome, String endereco, String email, String telResidencial, String celular, String rg, String cpfCnpj, double rendaMensal, String profissao) {
 		render(id, tipoPessoa, nome, endereco, email, telResidencial,celular,rg,cpfCnpj, rendaMensal, profissao);
 	}
 
 	public static void editar(long id, String tipoPessoa, String nome, String endereco, String email, String telResidencial, String celular, String rg, String cpfCnpj, double rendaMensal, String profissao) {
 		render(id, tipoPessoa, nome, endereco, email, telResidencial,celular,rg,cpfCnpj, rendaMensal, profissao);
 	}	
	
 	public static void detalhar(long id) {
 		ClienteModel cliente = ClienteDao.getInstance().buscarCliente(id);
		DecimalFormat twoDForm = new DecimalFormat("#########.##");
 
		String rendaMensal = Util.mascaraDinheiro(cliente.getRendaMensal(), DINHEIRO_REAL);
 		
		cliente.setRendaMensalApresentacao(rendaMensal);
 		render(cliente);
 	}		
 
 
 	public static void salvar(long id, String tipoPessoa, String nome, String endereco, String email, String telResidencial, String celular, String rg, String cpfCnpj, String rendaMensal, String profissao) {
 
 		String newRenda = rendaMensal.replace(".", "").replace(",", ".");
 		if(id == 0){
 			/* Valido unique se for inserção*/
 			int unique = verificaUnique(cpfCnpj, email);
 			if(unique == 1)
 			{
 				validation.addError("cpfCnpj", "validation.duplicated.cpfCnpj");
 				params.flash(); // add http parameters to the flash scope
 				validation.keep(); // keep the errors for the next request
 				index(0, tipoPessoa, nome,endereco,email, telResidencial,celular,rg,cpfCnpj, Double.parseDouble(newRenda), profissao);		
 			}
 			else if(unique == 2)
 			{
 				validation.addError("email", "validation.duplicated.email");
 				params.flash(); // add http parameters to the flash scope
 				validation.keep(); // keep the errors for the next request
 				index(0, tipoPessoa, nome,endereco,email, telResidencial,celular,rg,cpfCnpj, Double.parseDouble(newRenda), profissao);				
 			}		
 			else
 			{
 				inserir(tipoPessoa, nome, endereco, email, telResidencial, celular, rg, cpfCnpj, Double.parseDouble(newRenda), profissao);
 			}
 		}
 		else{
 			ClienteModel cliente = ClienteDao.getInstance().buscarCliente(id);
 			salvarEdit(cliente.id, cliente.getUsuario().id, tipoPessoa, nome, endereco, email, telResidencial, celular, rg, cpfCnpj, Double.parseDouble(newRenda), profissao);
 		}
 
 	}
 
 	public static void inserir(String tipoPessoa, String nome, String endereco, String email, String telResidencial, String celular, String rg, String cpfCnpj, double rendaMensal, String profissao) {
 
 		validation.required(nome);
 		validation.required(endereco);
 		validation.required(email);
 		validation.required(telResidencial);
 		validation.required(celular);
 		if(tipoPessoa == "F"){
 			validation.required(rg);
 		}
 		validation.required(cpfCnpj);
 
 		if(validation.hasErrors()){
 			params.flash(); // add http parameters to the flash scope
 			validation.keep(); // keep the errors for the next request			
 			index(0, tipoPessoa, nome,endereco,email, telResidencial,celular,rg,cpfCnpj, rendaMensal, profissao);
 		}
 		else{
 			UsuarioModel u = fresh(tipoPessoa, nome, endereco, email, telResidencial, celular, rg, cpfCnpj);			
 			ClienteModel c = new ClienteModel();
 			c.setProfissao(profissao);
 			c.setRendaMensal(rendaMensal);
 			c.salvaCliente(u, Consultor.getConsultorLogado());
 			Application.consultor();
 		}
 
 	}
 
 	public static void editarCliente(long idCliente){
 		ClienteModel cliente = ClienteDao.getInstance().buscarCliente(idCliente);
 		editar(cliente.getId(), cliente.usuario.getTipoPessoa(), cliente.usuario.getNome(), cliente.usuario.getEndereco(), cliente.usuario.getEmail(), cliente.usuario.getTelResidencial(), cliente.usuario.getCelular(), cliente.usuario.getRg(), cliente.usuario.getCpfCnpj(), cliente.getRendaMensal(), cliente.getProfissao());
 	}
 
 	public static void excluir(long idCliente, long idUsuario){
 		ClienteDao.getInstance().excluir(idCliente);
 		UsuarioDao.getInstance().excluir(idUsuario);
 		Application a = new Application();
 		a.consultor();
 
 	}
 
 	public static void salvarEdit(long id, long idUsuario, String tipoPessoa, String nome, String endereco, String email, String telResidencial, String celular, String rg, String cpfCnpj, double rendaMensal, String profissao) 
 	{
 		validation.required(nome);
 		validation.required(endereco);
 		validation.required(email);
 		validation.required(telResidencial);
 		validation.required(celular);
 		validation.required(rg);
 		validation.required(cpfCnpj);
 
 		if(validation.hasErrors()){
 			params.flash(); // add http parameters to the flash scope
 			validation.keep(); // keep the errors for the next request			
 			editar(idUsuario, tipoPessoa, nome,endereco,email, telResidencial,celular,rg,cpfCnpj, rendaMensal, profissao);
 		}
 		else{		
 			ClienteModel c = ClienteDao.getInstance().buscarCliente(id);
 			UsuarioModel u = UsuarioDao.getInstance().buscarUsuario(idUsuario);
			
 			int unique = 0;
 			/* Valido chave unica se for diferente do que está no banco */
 			if(!u.getCpfCnpj().equals(cpfCnpj))
 			{
 				unique = verificaUnique(cpfCnpj, "");
 			}
 			else if(!u.getEmail().equals(email))
 			{
 				unique = verificaUnique("", email);
 			}
 
 			if(unique == 1)
 			{
 				validation.addError("cpfCnpj", "validation.duplicated.cpfCnpj");
 				params.flash(); // add http parameters to the flash scope
 				validation.keep(); // keep the errors for the next request
 				index(0, tipoPessoa, nome,endereco,email, telResidencial,celular,rg,cpfCnpj, rendaMensal, profissao);		
 			}
 			else if(unique == 2)
 			{
 				validation.addError("email", "validation.duplicated.email");
 				params.flash(); // add http parameters to the flash scope
 				validation.keep(); // keep the errors for the next request
 				index(0, tipoPessoa, nome,endereco,email, telResidencial,celular,rg,cpfCnpj, rendaMensal, profissao);				
 			}	
 			else
 			{
 				c.setRendaMensal(rendaMensal);
 				c.setProfissao(profissao);
 				u.setNome(nome);
 				u.setEmail(email);
 				u.setCpfCnpj(cpfCnpj);
 				u.setTelResidencial(telResidencial);
 				u.setRg(rg);
 				u.setCelular(celular);
 				u.setEndereco(endereco);
 				u.save();
 			}
 			Application.consultor();
 		}
 	}
 
 	public static UsuarioModel fresh(String tpPessoa, String nome, String endereco, String email, String telResidencial, String celular, String rg, String cpfCnpj){
 		UsuarioModel u = new UsuarioModel();
 		u.setTipoPessoa(tpPessoa);
 		u.setNome(nome);
 		u.setEmail(email);
 		u.setCpfCnpj(cpfCnpj);
 		u.setTelResidencial(telResidencial);
 		u.setRg(rg);
 		u.setCelular(celular);
 		u.setEndereco(endereco);
 		return u;
 	}
 
 	public static void cancelar()
 	{
 		Application.consultor();
 	}
 
 	/* Método que verifica se o post não quebrará nenhuma unique */
 	/* 1 = cpfCnpj repetido, 2 = Email repetido */
 	private static int verificaUnique(String cpfCnpj, String email)
 	{
 		List<UsuarioModel> usuarios;
 		if(!cpfCnpj.isEmpty())
 		{
 			usuarios = UsuarioDao.getInstance().ListarUsuarioByCpfCnpj(cpfCnpj);
 			if(usuarios.size() > 0)
 			{
 				return 1;
 			}
 		}
 
 		if(!email.isEmpty())
 		{
 			usuarios = UsuarioDao.getInstance().ListarUsuarioByEmail(email);
 			if(usuarios.size() > 0)
 			{
 				return 2;
 			}
 		}
 
 		return 0;
 	}
 }
