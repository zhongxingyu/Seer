 package webview.servlet.document;
 
 import static webview.util.WebUtility.isInit;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import persistence.exceptions.UpdateEntityException;
 import webview.util.AlertsUtility;
 import business.EJB.documents.DocumentEJB;
 import business.exceptions.documents.DocumentNotFoundException;
 import business.exceptions.login.UnreachableDataBaseException;
 import datatype.SimpleDate;
 
 /**
  * Servlet implementation class DocUpdateServlet
  */
 @WebServlet("/protected/admin/updateDoc")
 public class DocUpdateServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DocUpdateServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     	String tipoCodDocumentoAntigo = request.getParameter("pesquisa_APEP_SEQ");
 		String codDocumentoAntigo = request.getParameter("pesquisa_num_APEP_SEQ");
 		
 		String tituloDocumento = request.getParameter("tituloDocumento");
 		String tipoCodDocumento = request.getParameter("tipo_num");
 		String codDocumento = request.getParameter("numero");
 		String local = request.getParameter("local");
 		String url = request.getParameter("urlImagem");
 		String resumo = request.getParameter("resumo");
 		
 		String[] identificacao = request.getParameter("identificacao").split("-");
 		String tipoCodiceCaixa = identificacao[0];
 		String codCodiceCaixa = identificacao[1];
 		
 		String autor = request.getParameter("autor");
 		String ocupacaoAutor = request.getParameter("autorOcupacao");
 		
 		String destinatario = request.getParameter("destinatario");
 		String ocupacaoDestinatario = request.getParameter("destinatarioOcupacao");
 		
 		String tipoDocumento = request.getParameter("tipoDoc");
 		
 		String palavraChave1 = request.getParameter("chave1");
 		String palavraChave2 = request.getParameter("chave2");
 		String palavraChave3 = request.getParameter("chave3");
     			
 	    String ano = request.getParameter("ano");
 		String mes = request.getParameter("mes");
 		String dia = request.getParameter("dia");
 		String data = null;
 		SimpleDate dataDoc = null;
 		
 		if(isInit(ano)) {
 			data = ano;
 			if(isInit(mes) && !mes.equals("00"))  {
				data += "/" + mes;
 				if(isInit(dia) && !dia.equals("00")) {
					data += "/" + dia;
 				}
 			}
 			dataDoc = SimpleDate.parse(data);
 		}
 		
 		DocumentEJB CB = new DocumentEJB();
 		codCodiceCaixa = tipoCodiceCaixa+"-"+codCodiceCaixa;
 		/*codDocumento = tipoCodDocumento+"-"+codDocumento;*/
 		
 		try {
 			CB.modifyDocument(tipoCodDocumentoAntigo,
 					codDocumentoAntigo, 
 					tituloDocumento, 
 					tipoCodDocumento,
 					codDocumento, 
 					local, 
 					url, 
 					resumo, 
 					dataDoc, 
 					codCodiceCaixa, 
 					autor, 
 					ocupacaoAutor, 
 					destinatario, 
 					ocupacaoDestinatario, 
 					tipoDocumento, 
 					palavraChave1,
 					palavraChave2, 
 					palavraChave3);
 			AlertsUtility.alertAndRedirectPage(response, "Documento Atualizado com sucesso!", "index.jsp");
 		} catch (UnreachableDataBaseException e) {
 			AlertsUtility.alertAndRedirectHistory(response, "Erro no banco de dados! Contate o suporte e tente novamente mais tarde.");
 			e.printStackTrace();
 		} catch (DocumentNotFoundException e) {
 			AlertsUtility.alertAndRedirectHistory(response, "Erro interno, tente novamente mais tarde.");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			AlertsUtility.alertAndRedirectHistory(response, "Número APEP/Sequencial duplicado!");
 		} catch (UpdateEntityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 }
 
