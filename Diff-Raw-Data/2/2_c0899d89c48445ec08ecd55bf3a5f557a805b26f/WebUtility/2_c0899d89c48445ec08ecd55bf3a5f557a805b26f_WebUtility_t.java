 package webview.util;
 
 import java.io.IOException;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 
 import persistence.dto.CodiceCaixa;
 import persistence.dto.DTO;
 import persistence.dto.Documento;
 import persistence.dto.PalavraChave;
 import persistence.dto.Profile;
 import persistence.dto.TemaPalavraChave;
 import persistence.dto.TipoDocumento;
 import business.DAO.document.CodiceCaixaDAO;
 import business.DAO.document.TipoDocumentoDAO;
 import business.EJB.documents.CodiceCaixaEJB;
 import business.EJB.documents.KeyWordEJB;
 import business.EJB.documents.ThemeEJB;
 import business.EJB.user.AuthBean;
 import business.EJB.user.UserBean;
 import business.exceptions.documents.CodiceCaixaNotFoundException;
 import business.exceptions.documents.DocumentTypeNotFoundException;
 import business.exceptions.documents.KeywordNotFoundException;
 import business.exceptions.documents.ThemeNotFoundException;
 import business.exceptions.login.UnreachableDataBaseException;
 import business.exceptions.login.UserNotFoundException;
 
 public final class WebUtility {
 
 	public static final String cookie_email = "email_graopara";
 	public static final String cookie_session = "sessao_graopara";
 	public static final String cookie_nome = "nome_graopara";
 	public static final String cookie_status = "status_graopara";
 	public static final String[] meses = {
 		"Janeiro", "Fevereiro", "Março", "" +
 		"Abril", "Maio", "Junho", 
 		"Julho", "Agosto", "Setembro", 
 		"Outubro", "Novembro", "Dezembro"
 	}; 
 
 	public static Profile admin_profile = new Profile("admin");
 	public static Profile user_profile_lvl_1 = new Profile("user1");
 	public static Profile user_profile_lvl_2 = new Profile("user2");
 	public static Profile default_profile = new Profile("default");
 
 	public static final int cookie_expire = -1; //1 sessão dias
 	
 	/**
 	 * 
 	 * @param cookie_list
 	 * @return
 	 */
 	public static UserBean cookieLogin(Cookie[] cookie_list) {
 		if(cookie_list == null) return null;
 
 		String email = null;
 		String password = null;
 		for(int i = 0; i < cookie_list.length; i++) {
 			Cookie cookie = cookie_list[i];
 			String cName = cookie.getName();
 			if(cName.equals(WebUtility.cookie_email))
 				email = cookie.getValue();
 			if(cName.equals(WebUtility.cookie_session))
 				password = cookie.getValue();
 		}
 		if(email != null && password != null) {
 			try {
 				UserBean result = AuthBean.validarLogin(email, password, AuthBean.HashedPwd);
 				return result;
 			} catch (UnreachableDataBaseException e) {
 				e.printStackTrace();
 				return null;
 			} catch (UserNotFoundException e) {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param c_list
 	 * @param c_name
 	 * @return
 	 */
 	public static Cookie selectCookie(Cookie[] c_list, String c_name) {
 		if(c_list == null) return null;
 
 		for(int i = 0; i < c_list.length; i++) {
 			Cookie cookie = c_list[i];
 			String cName = cookie.getName();
 			if(cName.equals(c_name)) return cookie;
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param str1
 	 * @param str2
 	 * @return
 	 */
 	public static String strDiff(String str1, String str2) {
 		int idx = str1.lastIndexOf(str2);
 	    if (idx > -1) {
 	      return str1.substring(str2.length());
 	    }
 	    else return str1;
 	}
 
 	/**
 	 * 
 	 * @param doc
 	 * @return
 	 */
 	public static String docToString(Documento doc) {
 		return
 				doc.getTitulo() + "\n"
 				+ doc.getAutor() + " "
 				+ doc.getDestinatario() + " "
 				+ doc.getLocal() + " "
 				+ doc.getResumo() + " "
 				+ doc.getData().toString()
 				+ " --- "
 				+ doc.getUploader().getEmail()
 				+ " --- "
 				+ doc.getCod() + " "
 				+ doc.getCodiceCaixa().getTitulo() + " "
 				+ doc.getCodiceCaixa().getCod() + " ";
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 * @param out
 	 * @throws IOException
 	 */
 	public static void printName(HttpServletRequest request, JspWriter out) throws IOException {
 		Cookie name = selectCookie(request.getCookies(), WebUtility.cookie_nome);
 		if(name != null) out.write("<label>" + name.getValue() + "!</label>");
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param parameter
 	 * @return
 	 * @throws IOException
 	 */
 	public static String printLabel(HttpServletRequest request, String parameter) throws IOException {
 		String label = request.getParameter(parameter);
 		if(label == null) return "";
 		else return label;
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws IOException
 	 * @throws UnreachableDataBaseException
 	 */
 	public static String printCodCodiceCaixa(HttpServletRequest request) throws IOException, UnreachableDataBaseException {
 		String output = null;
 
 		CodiceCaixaDAO codiceCaixa = null;
 
 		try {
 			codiceCaixa = new CodiceCaixaDAO();
 			List<DTO> list = codiceCaixa.findAllCodiceCaixa();
 			List<String> aux = new ArrayList<String>();
 			output = "\n	<option selected value=\"\">Selecione...</option> ";
 			
 			aux.add(output);
 
 			for(DTO d : list){
 				String tipoCodiceOuCaixa = ((CodiceCaixa) d).getCod();
 				if(!aux.contains(tipoCodiceOuCaixa)){
 					output += "\n	<option value=\""+ tipoCodiceOuCaixa + " \">"+ tipoCodiceOuCaixa + "</option> ";
 					aux.add(tipoCodiceOuCaixa);
 				}
 			}
 		} catch (CodiceCaixaNotFoundException e) {
 			//e.printStackTrace();
 		}
 		return output;
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws UnreachableDataBaseException
 	 */
 	public static String printSelectTituloCodiceCaixa(HttpServletRequest request, boolean withContent) throws UnreachableDataBaseException {
 		String output = null;
 		String selected = request.getParameter("codigo");
 		CodiceCaixaEJB codiceCaixa = null;
 		List<DTO> list = null; 
 
 		try {
 			codiceCaixa = new CodiceCaixaEJB();
 			if(withContent) {
 				list = codiceCaixa.getAllEntriesWithContent();
 			}
 			else {
 				list = codiceCaixa.getAllEntries();
 			}
 
 			output = "\n	<option selected value=\"\">Selecione...</option> ";
 			if(list != null){
 				
 				HashSet<String> added = new HashSet<String>();
 	
 				for(DTO d : list){
 					CodiceCaixa c = (CodiceCaixa) d;
 					if(!added.contains(c.getTitulo())) {
 						if(!c.getCod().equals(selected))
 							output += "\n	<option value=\""+ c.getCod() +"-"+ c.getTitulo() + " \">" + c.getCod().replace("-", " - ") + ": "+ c.getTitulo() + " (" + c.getAnoInicio() + " - " + c.getAnoFim() + ")" + "</option> ";
 						else
 							output += "\n	<option selected value=\""+ c.getCod() +"-"+ c.getTitulo() + " \">" + c.getCod().replace("-", " - ") + ": " + c.getTitulo()  + " (" + c.getAnoInicio() + " - " + c.getAnoFim() + ")" + "</option> ";
 						added.add(c.getTitulo());
 					}
 				}
 			}
 		} catch (CodiceCaixaNotFoundException e) {
 			//e.printStackTrace();
 		}
 		return output;
 	}
 	
 	public static String printSelectTituloCodiceCaixa(HttpServletRequest request) throws UnreachableDataBaseException {
 		String output = null;
 		String selected = request.getParameter("codigo");
 		CodiceCaixaEJB codiceCaixa = null;
 		List<DTO> list = null; 
 
 		try {
 			codiceCaixa = new CodiceCaixaEJB();
 			list = codiceCaixa.getAllEntriesWithContent();
 			if(list != null){
 
 				output = "\n	<option selected value=\"\">Selecione...</option> ";
 				HashSet<String> added = new HashSet<String>();
 	
 				for(DTO d : list){
 					CodiceCaixa c = (CodiceCaixa) d;
 					if(!added.contains(c.getTitulo())) {
 						if(!c.getCod().equals(selected))
							output += "\n	<option value=\"" + c.getTitulo() + "\">" + c.getTitulo() + "</option> ";
 						else
 							output += "\n	<option selected value=\""+ c.getTitulo() + " \">" + c.getTitulo() + "</option> ";
 						added.add(c.getTitulo());
 					}
 				}
 			}
 			else{
 				output = "\n	<option selected value=\"\">Nenhum Códice/Caixa com conteúdo</option> ";
 			}
 		} catch (CodiceCaixaNotFoundException e) {
 			//e.printStackTrace();
 		}
 		return output;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 * @throws UnreachableDataBaseException
 	 */
 	public static String printTituloCodCaixa(HttpServletRequest request) {
 		String output = null;
 		String selected = request.getParameter("codigo");
 		CodiceCaixaEJB codiceCaixa = new CodiceCaixaEJB();
 		List<DTO> list = null;
 		
 		try {
 			list = codiceCaixa.findByCod(selected);
 			output = ((CodiceCaixa)list.get(0)).getTitulo();
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		} catch (CodiceCaixaNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return output;
 	}
 	
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws IOException
 	 * @throws UnreachableDataBaseException
 	 */
 	public static String printEpocasCodiceCaixa(HttpServletRequest request) throws IOException, UnreachableDataBaseException {
 		String output = null;
 
 		CodiceCaixaDAO codiceCaixa = null;
 
 		try {
 			codiceCaixa = new CodiceCaixaDAO();
 			List<DTO> list = codiceCaixa.findAllCodiceCaixa();
 			List<String> aux = new ArrayList<String>();
 			output = "\n	<option selected value=\"\">Selecione...</option> ";
 			
 			aux.add(output);
 
 			for(DTO d : list){
 				int epocaInicial = ((CodiceCaixa) d).getAnoInicio();
 				int epocaFinal = ((CodiceCaixa) d).getAnoFim(); 
 				
 				if(!aux.contains(epocaInicial)){
 					output += "\n	<option value=\""
 							+ epocaInicial +"_"+ epocaFinal + " \">"
 							+ epocaInicial +"-"+ epocaFinal + "</option> ";
 					aux.add(String.valueOf(epocaInicial));
 				}
 			}
 		} catch (CodiceCaixaNotFoundException e) {
 			//e.printStackTrace();
 		}
 		return output;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws IOException
 	 * @throws UnreachableDataBaseException
 	 */
 	public static String printCadastroOrigem(HttpServletRequest request) throws IOException, UnreachableDataBaseException {
 		String output = null;
 		String identificacao = request.getParameter("identificacao");
 		String codigo = request.getParameter("codigo");
 		String titulo = request.getParameter("titulo");
 
 		CodiceCaixaDAO codiceCasixaDAO = null;
 
 		try {
 			codiceCasixaDAO = new CodiceCaixaDAO();
 			List<DTO> list = codiceCasixaDAO.findAllCodiceCaixa();
 			if(identificacao != null && codigo != null && titulo != null)
 				output = "\n	<option selected value=\"" 
 						+ identificacao + "-" + codigo + "-" + titulo + "\">" 
 						+ identificacao + " - " + codigo + " - " + titulo + "</option> ";
 
 			else	output = "\n	<option selected value=\"\">Selecione...</option> ";
 			for(DTO d : list){
 				String codOrigem =  ((CodiceCaixa) d).getCod();
 				String tituloOrigem = ((CodiceCaixa) d).getTitulo();
 				if(!codOrigem.equals(codigo))
 					output += "\n	<option value=\"" 
 						+ "-" + codOrigem + "-" + tituloOrigem + " \">" 
 						+ " - " + codOrigem + " - " + tituloOrigem + "</option> ";
 			}
 		} catch (CodiceCaixaNotFoundException e) {
 			//e.printStackTrace();
 		}
 
 		return output;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 * @throws IOException
 	 */
 	public static String printCodDocumento(HttpServletRequest request) throws IOException {
 		String output = null;
 		String parameter = request.getParameter("tipoAPEP_SEQ");
 		if(parameter == null)
 		{
 			output = "<option value=\"\">\"Erro\"</option>";
 		}
 		else if(parameter.equals("APEP"))
 		{
 			output = 
 					"<option selected value=\"APEP\">APEP</option> " +
 					"<option value=\"SEQ\">Sequencial</option>";
 		}
 		else if(parameter.equals("SEQ"))
 		{
 			output = 
 					"<option value=\"APEP\">APEP</option> " +
 					"<option selected value=\"SEQ\">Sequencial</option>";
 		}
 		return output;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 */
 	public static String printSelectDia(HttpServletRequest request) {
 		String dia = request.getParameter("dia");
 		return "<option selected value=\"" + dia + "\">" + dia + "</option>" +
 				"<option value=\"\">--------</option>";
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 */
 	public static String printSelectMes(HttpServletRequest request) {
 		String mes = request.getParameter("mes");
 		int num_mes = Integer.parseInt(mes);
 		return "<option selected value=\"" + mes + "\">" + meses[num_mes - 1] + "</option>" +
 				"<option value=\"\">--------</option>";
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @return
 	 */
 	public static String printSelectTipoDoc(HttpServletRequest request) {
 		TipoDocumentoDAO dtd = new TipoDocumentoDAO();
 		String result = "";
 		String tipoDoc = null;
 		try {
 			List<DTO> list = dtd.findAllDocumentTypes();
 			result = "<option value=\"\">Selecione...</option> ";
 			for(DTO d : list){
 				tipoDoc = ((TipoDocumento) d).getNome();
 				if(tipoDoc.equals(request.getParameter("tipoDoc")))
 					result += "<option selected value=\"" + tipoDoc + "\">" + tipoDoc + " - " + ((TipoDocumento) d).getDescricao() + "</option> ";
 				else
 					result += "<option value=\"" + tipoDoc + "\">" + tipoDoc + " - " + ((TipoDocumento) d).getDescricao() + "</option> ";
 			}
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		} catch (DocumentTypeNotFoundException e) {
 			//e.printStackTrace();
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param key_pos
 	 * @return
 	 */
 	public static String printInputKeyWords(HttpServletRequest request, String key_pos) {
 		KeyWordEJB busca = new KeyWordEJB();
 		String result = "";
 		String key = request.getParameter(key_pos);
 		try {
 			PalavraChave palavra = busca.findByString(key);
 			return "<input type=\"text\" size=\"12\" maxlength=\"32\" class=\"inputPalavraChave\" id=\""+ key_pos +"\" name=\""+ key_pos +"\" value=\"" + palavra.getPalavra() + "\"/>";
 
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		} catch (KeywordNotFoundException e) {
 			return "<input type=\"text\" size=\"12\" maxlength=\"32\" class=\"inputPalavraChave\" id=\""+ key_pos +"\" name=\""+ key_pos +"\" value=\"\"></input> ";
 		} catch (IllegalArgumentException e){
 			return "<input type=\"text\" size=\"12\" maxlength=\"32\" class=\"inputPalavraChave\" id=\""+ key_pos +"\" name=\""+ key_pos +"\" value=\"\"></input> ";
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param key_pos
 	 * @return
 	 */
 	public static String printSelectKeyWords(HttpServletRequest request, String key_pos, String tema) {
 		KeyWordEJB word = new KeyWordEJB();
 		String result = "";
 		String key = null;
 		try {
 			List<DTO> list = word.findByTheme(tema);
 
 			for(DTO d : list){
 				key = ((PalavraChave) d).getPalavra();
 				if(key.equals(request.getParameter(key_pos)))
 					result += "<option selected value=\"" + key + "\">" + key + "</option> ";
 				else
 					result += "<option value=\"" + key + "\">" + key + "</option> ";
 			}
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		} catch (KeywordNotFoundException e) {
 		}
 		return result;
 	}
 
 	/**
 	 * @param request
 	 * @return
 	 * @throws CodiceCaixaNotFoundException
 	 */
 	public static String printOriginTitle(HttpServletRequest request) throws CodiceCaixaNotFoundException {
 		CodiceCaixaEJB od = new CodiceCaixaEJB();
 		String result = "";
 		try {
 			CodiceCaixa ori = od.findExactEntry(request.getParameter("codigo"), request.getParameter("titulo"));
 			result = ori.getTitulo().trim();
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	/**
 	 * @param request
 	 * @return
 	 */
 	public static String printSelectKeyWordThemes() {
 		ThemeEJB tb = new ThemeEJB();
 		String result = "";
 		String data = "";
 		try {
 			List<DTO> list = tb.searchAllThemes();
 			for(DTO d : list){
 				data = ((TemaPalavraChave) d).getTema();
 				result += "<option value=\"" + data + "\">" + data + "</option> ";
 			}
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		} catch (ThemeNotFoundException e) {
 			//
 		}
 		
 		return result;
 	}
 	
 
 	/**
 	 * @param str
 	 * @return
 	 */
 	public static String removeAccents(String str) {
 	    str = Normalizer.normalize(str, Normalizer.Form.NFD);
 	    str = str.replaceAll("[^\\p{ASCII}]", "");
 	    return str;
 	}
 }
